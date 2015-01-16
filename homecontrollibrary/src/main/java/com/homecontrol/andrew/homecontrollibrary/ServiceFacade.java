package com.homecontrol.andrew.homecontrollibrary;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by andrew on 12/31/14.
 */
public class ServiceFacade {
    private static final String TAG = "ServiceFacade";
    private static ServiceFacade serviceFacade;
    private Context context;
    private static MobileFacadeInterface mobileActivity;
    private static WearEventListener wearEventListener;
    private Messenger mService;
    final Messenger messengerSelf = new Messenger(new IncomingHandler());   // this is what I will give to services to make callbacks
    private boolean mIsBound;
    private int doingWhat;   // what request from Wear/Mobile we were handling
    private int madeRequest;

    private String enteredPasscode;

    // static tokens for designating what device is making the call
    public static final int MOBILE_DEVICE = 0;
    public static final int WEAR_DEVICE = 1;

    // static tokens for setting doingWhat
    private static final int VALIDATING_LOGIN = 0;
    private static final int LOADING_APP_DATA = 1;   // loading most recent account data
    private static final int LOADING_NETWORK_DATA = 2;  // switching account
    private static final int DOWNLOADING_MODULES = 3;

    // static tokens for messages being RECEIVED from HANService
    public static final int VALIDATION_REPLY = 0;
    public static final int LOAD_APP_DATA_REPLY = 1;     // specifies the reply from the HANService about loading most recent account data
    public static final int LOAD_NETWORK_DATA_REPLY = 2;    // specifies reply from HANService that the selected network data has been loaded
    public static final int DOWNLOAD_OP_REPLY = 3;  // specifies reply from HANService that the message is downloaded module data


    // String KEYs for data in bundles SENDING
    public static final String LOGIN_PASSCODE_KEY = "login_passcode";
    public static final String NEW_NETWORK_NAME_KEY = "update_network_name";
    public static final String NEW_NETWORK_ADDRESS_KEY = "update_network_address";
    public static final String MODULE_UPLOAD_STRING_KEY = "module_string";
    public static final String REMOVE_NETWORK_NAME_KEY = "remove_network_name";
    public static final String SET_PASSCODE_STRING_KEY = "entered_passcode";
    public static final String LOAD_NETWORK_DATA_NAME_KEY = "load_network_data_name";
    public static final String DEVICE_TYPE_KEY = "device_type";

    private ServiceFacade(Context ctx){
        this.context = ctx;
    }

    public static ServiceFacade getInstance(Context ctx, WearEventListener listener){
        if(serviceFacade == null){
            serviceFacade = new ServiceFacade(ctx);
        }
        if(wearEventListener == null){
            wearEventListener = listener;
        }
        return serviceFacade;
    }

    public static ServiceFacade getInstance(Context ctx, MobileFacadeInterface activity){
        if(serviceFacade == null){
            serviceFacade = new ServiceFacade(ctx);
        }
        mobileActivity = activity;  // always store the reference to the activity since the activity may change throughout the lifecycle
        return serviceFacade;
    }

    public void loadAppData(int deviceType){
        doingWhat = LOADING_APP_DATA;   // set what we are doing, so we can pick up later if needed
        madeRequest = deviceType;       // not really sure this is needed. I dont think the watch will ever make this request
        if(!mIsBound) {
            Intent i = new Intent(context, HANService.class);
            context.bindService(i, mConnection, Context.BIND_AUTO_CREATE);
        }else{
           sendLoadAppDataMsg();     // we have a binder, call this method to request HANService to load last used network data
            // this will respond if there was an account or not
        }
    }

    public void loadNetworkData(String networkName, int deviceType){
        doingWhat = LOADING_NETWORK_DATA;   // set what we are doing, so we can pick up later if needed
        madeRequest = deviceType;
        if(!mIsBound) {
            Intent i = new Intent(context, HANService.class);
            context.bindService(i, mConnection, Context.BIND_AUTO_CREATE);
        }else{
            sendLoadNetworkDataMsg(networkName);     // we have a binder, call this method to request HANService to load a different network's data
            // this will respond with the network name and url
        }
    }

    public void saveAppData(){
        if(!mIsBound) {
            Intent i = new Intent(context, HANService.class);
            context.bindService(i, mConnection, Context.BIND_AUTO_CREATE);
        }else{
            sendSaveAppDataMsg();     // we have a binder, call this method to request HANService to load last used network data
            // saves app data, last network used...
        }
    }

    public void clearAppPreferences(){
        if(!mIsBound) {
            Intent i = new Intent(context, HANService.class);
            context.bindService(i, mConnection, Context.BIND_AUTO_CREATE);
        }else{
            sendClearAppPreferences();     // we have a binder, call this method to request HANService to clear all app preferences
            // future plans will send a response back for the NewUserFragment
        }
    }

    public void saveNetwork(){
        // saves the network preferences for the current network
        if(!mIsBound) {
            Intent i = new Intent(context, HANService.class);
            context.bindService(i, mConnection, Context.BIND_AUTO_CREATE);
        }else{
            sendSaveNetworkMsg();     // we have a binder, call this method to save the current networks preference
        }
        // no response is sent back
    }

    public void removeNetwork(String networkToRemove){
        // sends message to HANService to remove the specified network preferences
        if(!mIsBound) {
            Intent i = new Intent(context, HANService.class);
            context.bindService(i, mConnection, Context.BIND_AUTO_CREATE);
        }else{
            sendRemoveNetworkMsg(networkToRemove);     // we have a binder, call this method to remove the specified network preference
        }
        // doesnt get a response from HANService
    }

    public void validateLogin(String enteredPasscode){
        doingWhat = VALIDATING_LOGIN;   // set what we are doing, so we can pick up later if needed
        this.enteredPasscode = enteredPasscode;     // set the enteredPasscode, in case we have to start the service and look for it later
        if(!mIsBound) {
            Intent i = new Intent(context, HANService.class);
            context.bindService(i, mConnection, Context.BIND_AUTO_CREATE);
        }else{
            sendValidateLoginMsg(enteredPasscode);     // we have a binder, call this method to send the message to the HANService
        }
        // response is sent back, if the login is successful of not
    }

    public void updateNetworkData(String networkName, String networkAddress){
        if(!mIsBound) {
            Intent i = new Intent(context, HANService.class);
            context.bindService(i, mConnection, Context.BIND_AUTO_CREATE);
        }else{
            sendUpdateNetworkMsg(networkName, networkAddress);     // we have a binder, call this method to update the current networks data
        }
        // doesnt wait for response from HANService
    }

    public void createNewNetwork(String networkName, String networkAddress){
        if(!mIsBound) {
            Intent i = new Intent(context, HANService.class);
            context.bindService(i, mConnection, Context.BIND_AUTO_CREATE);
        }else {
            sendNewNetworkMsg(networkName, networkAddress);     // we have a binder, call this method to create a new network
            // doesnt wait for response from HANService
        }
    }

    public void getModulesString(int deviceType){
        madeRequest = deviceType;   // save where the request came from, Mobile or Wear, used for the reply
        doingWhat = DOWNLOADING_MODULES;   // set what we are doing, so we can pick up later if needed
        if(!mIsBound) {
            Intent i = new Intent(context, HANService.class);
            context.bindService(i, mConnection, Context.BIND_AUTO_CREATE);
        }else{
            sendDownloadModulesMsg();     // we have a binder, call this method to send the message to the HANService to download modules
        }
        // expects a reply from the HANService
    }

    public void modifyModuleData(String[] valuesArray){
        if(!mIsBound) {
            Intent i = new Intent(context, HANService.class);
            context.bindService(i, mConnection, Context.BIND_AUTO_CREATE);
        }else{
            sendModifyModuleDataMsg(valuesArray);     // we have a binder, call this method to send the message to the HANService to upload the module changes
        }
        // no reply expected
    }

    public void setAccountPasscode(String enteredPasscode){
        if(!mIsBound) {
            Intent i = new Intent(context, HANService.class);
            context.bindService(i, mConnection, Context.BIND_AUTO_CREATE);
        }else{
            sendSetPasscodeMsg(enteredPasscode);     // we have a binder, call this method to send the app passcode
        }
        // no reply expected
    }

    private void sendLoadAppDataMsg(){
        try {
            Message msg = Message.obtain(null, HANService.LOAD_APP_DATA);         // create message to send to HANService so it will check for a recently used account
            msg.replyTo = messengerSelf;       // add a reference to this, for HANService to reply to
            mService.send(msg);             // send the message to HANService
        } catch (RemoteException e) {
            // In this case the service has crashed before we could even
            // do anything with it; we do not restart the service by default
            // so I will potentially need to handle this
        }
    }

    private void sendLoadNetworkDataMsg(String netoworkName){
        try {
            Bundle bundle = new Bundle();
            bundle.putString(LOAD_NETWORK_DATA_NAME_KEY, netoworkName);      // attach the specified network to the bundle
            Message msg = Message.obtain(null, HANService.LOAD_NETWORK_DATA);         // create message to send to HANService so it will load a specified network's data
            msg.replyTo = messengerSelf;       // add a reference to this, for HANService to reply to
            msg.setData(bundle);
            mService.send(msg);             // send the message to HANService
        } catch (RemoteException e) {
            // In this case the service has crashed before we could even
            // do anything with it; we do not restart the service by default
            // so I will potentially need to handle this
        }
    }

    private void sendSaveAppDataMsg(){
        try {
            Message msg = Message.obtain(null, HANService.SAVE_APP_DATA);         // create message to send to HANService to save app preferences
            msg.replyTo = messengerSelf;       // add a reference to this, for HANService to reply to
            mService.send(msg);             // send the message to HANService
        } catch (RemoteException e) {
            // In this case the service has crashed before we could even
            // do anything with it; we do not restart the service by default
            // so I will potentially need to handle this
        }
    }

    private void sendClearAppPreferences(){
        try {
            Message msg = Message.obtain(null, HANService.CLEAR_APP_PREFERENCES);         // create message to send to HANService to request clearing app preferences
            msg.replyTo = messengerSelf;       // add a reference to this, for HANService to reply to
            mService.send(msg);             // send the message to HANService
        } catch (RemoteException e) {
            // In this case the service has crashed before we could even
            // do anything with it; we do not restart the service by default
            // so I will potentially need to handle this
        }
    }

    private void sendValidateLoginMsg(String enteredPasscode){
        try {
            Bundle bundle = new Bundle();
            bundle.putString(LOGIN_PASSCODE_KEY, enteredPasscode);      // attach the string the user entered to the bundle
            Message msg = Message.obtain(null, HANService.VALIDATE_LOGIN_OP);         // create message to send to HANService to request a login validation
            msg.replyTo = messengerSelf;       // add a reference to this, for HANService to reply to
            msg.setData(bundle);    // attach bundle to message
            mService.send(msg);             // send the message to HANService
        } catch (RemoteException e) {
            // In this case the service has crashed before we could even
            // do anything with it; we do not restart the service by default
            // so I will potentially need to handle this
        }
    }

    private void sendUpdateNetworkMsg(String networkName, String networkAddress){
        try {
            Log.d(TAG, "sending: " + networkName + " , " + networkAddress + " to HANService");
            Bundle bundle = new Bundle();
            bundle.putString(NEW_NETWORK_NAME_KEY, networkName);      // attach the new network name
            bundle.putString(NEW_NETWORK_ADDRESS_KEY, networkAddress);       // attach the new network address
            Message msg = Message.obtain(null, HANService.UPDATE_CURRENT_NETWORK);         // create message to send to HANService to update the current network values
            msg.replyTo = messengerSelf;       // add a reference to this, for HANService to reply to
            msg.setData(bundle);
            mService.send(msg);             // send the message to HANService
        } catch (RemoteException e) {
            // In this case the service has crashed before we could even
            // do anything with it; we do not restart the service by default
            // so I will potentially need to handle this
        }
    }

    private void sendNewNetworkMsg(String networkName, String networkAddress){
        try {
            Log.d(TAG, "sending new network: " + networkName + " , " + networkAddress + " to HANService");
            Bundle bundle = new Bundle();
            bundle.putString(NEW_NETWORK_NAME_KEY, networkName);      // attach the new network name
            bundle.putString(NEW_NETWORK_ADDRESS_KEY, networkAddress);       // attach the new network address
            Message msg = Message.obtain(null, HANService.CREATE_NEW_NETWORK);         // create message to send to HANService to create a new network
            msg.replyTo = messengerSelf;       // add a reference to this, for HANService to reply to
            msg.setData(bundle);
            mService.send(msg);             // send the message to HANService
        } catch (RemoteException e) {
            // In this case the service has crashed before we could even
            // do anything with it; we do not restart the service by default
            // so I will potentially need to handle this
        }
    }

    private void sendSaveNetworkMsg(){
        try {
            Message msg = Message.obtain(null, HANService.SAVE_NETWORK);         // create message to send to HANService to update the current network values
            msg.replyTo = messengerSelf;       // add a reference to this, for HANService to reply to
            mService.send(msg);             // send the message to HANService
        } catch (RemoteException e) {
            // In this case the service has crashed before we could even
            // do anything with it; we do not restart the service by default
            // so I will potentially need to handle this
        }
    }

    private void sendRemoveNetworkMsg(String networkName){
        try {
            Log.d(TAG, "sending remove ["+ networkName + "] network to HANService");
            Bundle bundle = new Bundle();
            bundle.putString(REMOVE_NETWORK_NAME_KEY, networkName);      // attach the new network name
            Message msg = Message.obtain(null, HANService.REMOVE_NETWORK);         // create message to send to HANService to remove the all data for the specified network
            msg.replyTo = messengerSelf;       // add a reference to this, for HANService to reply to
            msg.setData(bundle);
            mService.send(msg);             // send the message to HANService
        } catch (RemoteException e) {
            // In this case the service has crashed before we could even
            // do anything with it; we do not restart the service by default
            // so I will potentially need to handle this
        }
    }

    private void sendDownloadModulesMsg(){
        try {
            Bundle bundle = new Bundle();
            bundle.putInt(DEVICE_TYPE_KEY, madeRequest);
            Message msg = Message.obtain(null, HANService.DOWNLOAD_OP);         // create message to send to HANService to request downloading modules
            msg.replyTo = messengerSelf;       // add a reference to this, for HANService to reply to
            msg.setData(bundle);
            mService.send(msg);             // send the message to HANService
        } catch (RemoteException e) {
            // In this case the service has crashed before we could even
            // do anything with it; we do not restart the service by default
            // so I will potentially need to handle this
        }
    }

    private void sendModifyModuleDataMsg(String[] valuesArray){
        Bundle bundle = new Bundle();
        bundle.putStringArray(MODULE_UPLOAD_STRING_KEY, valuesArray);
        try {
            Message msg = Message.obtain(null, HANService.UPLOAD_OP);         // create message to send to HANService to request updating module data on the network server
            msg.replyTo = messengerSelf;       // add a reference to this, for HANService to reply to
            msg.setData(bundle);
            mService.send(msg);             // send the message to HANService
        } catch (RemoteException e) {
            // In this case the service has crashed before we could even
            // do anything with it; we do not restart the service by default
            // so I will potentially need to handle this
        }
    }

    private void sendModuleDataBackToClient(String moduleString){
        Log.d(TAG, "module string: " + moduleString);
        if(madeRequest == MOBILE_DEVICE){
            if(moduleString != null){
                mobileActivity.sendJSONBackToDevice(moduleString);
            }else{
                mobileActivity.createToast("No Response From Server");
                Log.d(TAG, "no response from server");
                mobileActivity.switchToRetryFragment();
            }
        }else if(madeRequest == WEAR_DEVICE){
            if(moduleString != null){
                wearEventListener.sendJSONBackToDevice(moduleString);
            }else{
                Log.d(TAG, "no response from server");
            }
        }
    }

    private void sendSetPasscodeMsg(String enteredPasscode){
        Bundle bundle = new Bundle();
        bundle.putString(SET_PASSCODE_STRING_KEY, enteredPasscode);
        try {
            Message msg = Message.obtain(null, HANService.SET_PASSCODE);         // create message to send to HANService to set the app passcode
            msg.replyTo = messengerSelf;       // add a reference to this, for HANService to reply to
            msg.setData(bundle);
            mService.send(msg);             // send the message to HANService
        } catch (RemoteException e) {
            // In this case the service has crashed before we could even
            // do anything with it; we can count on soon being
            // disconnected (and then reconnected if it can be restarted)
            // so there is no need to do anything here.

            // ****** i think i should handle this ****
        }
    }

    // for handling IPC messages from HANServer
    private class IncomingHandler extends Handler {
        @Override
        public void handleMessage(final Message msg) {
            Log.d(TAG, "getting reply from HANService");
            Bundle bundle = msg.getData();

            switch (msg.what) {
                case VALIDATION_REPLY:
                    Log.d(TAG, "received login reply from HANService");
                    boolean success = bundle.getBoolean(HANService.LOGIN_RESULT);       // use key to get boolean value from bundle
                    if(success){
                        Log.d(TAG, "Login result: success");
                    }else{
                        Log.d(TAG, "Login result: failure");
                    }
                    // this request should only come from mobile device. so cast it and send result
                    ((MobileFacadeInterface) mobileActivity).returnLoginResult(success);
                    break;
                case LOAD_APP_DATA_REPLY:
                    Log.d(TAG, "received load most recent account reply from HANService");
                    boolean haveAccount = bundle.getBoolean(HANService.LOAD_APP_DATA_RESULT, false);
                    if(madeRequest == MOBILE_DEVICE) {
                        if (haveAccount) {
                            Log.d(TAG, "last used network data has been loaded");
                            String networkName = bundle.getString(HANService.LOAD_APP_DATA_NETWORK_NAME);   // load current network name from bundle
                            String networkAddress = bundle.getString(HANService.LOAD_APP_DATA_NETWORK_ADDRESS); // load current network url
                            ArrayList<String> networkList = bundle.getStringArrayList(HANService.LOAD_APP_DATA_NETWORK_LIST);
                            mobileActivity.setCurrentNetworkData(networkName, networkAddress);       // send current network name to MobileActivity
                            mobileActivity.setNetworkList(networkList);       // pass the network list
                            mobileActivity.promptForLogin();
                        } else {
                            Log.d(TAG, "no network data was found");
                            ((MobileFacadeInterface) mobileActivity).promptForNewUser();
                        }
                    }else if(madeRequest == WEAR_DEVICE){
                        Log.d(TAG, "skipping sending data back at this time since the request came from a wear device");
                    }
                    break;
                case LOAD_NETWORK_DATA_REPLY:
                    Log.d(TAG, "received load network data reply from HANService");
                    String networkName = bundle.getString(HANService.LOAD_NETWORK_DATA_NETWORK_NAME);   // load current network name from bundle
                    String networkAddress = bundle.getString(HANService.LOAD_NETWORK_DATA_NETWORK_ADDRESS); // load current network url

                    mobileActivity.setCurrentNetworkData(networkName, networkAddress);       // send current network name to MobileActivity
                    mobileActivity.refreshCallback();       // ****** I should probably save time by calling this within here or something. since it will be sending requests this way anyway
                    break;
                case DOWNLOAD_OP_REPLY:
                    Log.d(TAG, "received download module reply from HANService");
                    String modString = bundle.getString(HANService.MODULE_DOWNLOAD_STRING);
                    sendModuleDataBackToClient(modString);
                    break;
                default:
                    super.handleMessage(msg);
                    Log.d(TAG, "reply message from HANService is unrecognizable");
                    break;
            }
        }
    }

    // for IPC between services/processes
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  We are communicating with our
            // service through an IDL interface, so get a client-side
            // representation of that from the raw service object.

            // create the messenger object from the binder we were just passed, in arguments
            Log.d(TAG, "connected to HANService");
            mService = new Messenger(service);
            mIsBound = true;    // mark that we are bound

            if(doingWhat == VALIDATING_LOGIN) {
                Log.d(TAG, "sending validate login request to HANService");
                sendValidateLoginMsg(enteredPasscode);     // send the message to the HANService, with the passcode we stored earlier
            }else if(doingWhat == LOADING_APP_DATA){
                Log.d(TAG, "sending load most recent Account to HANService");
                sendLoadAppDataMsg();       // request HANService to load most recently used network data
            }

            Log.d(TAG, "message has been sent to HANService");
            // now just wait for the service to send back and continue the process back to the wear device
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // toss out binder
            mService = null;
            mIsBound = false;
            Log.d(TAG, "disconnected from service");
        }

    };
}
