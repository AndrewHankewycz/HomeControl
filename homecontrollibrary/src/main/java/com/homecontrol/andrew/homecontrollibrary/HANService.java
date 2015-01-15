package com.homecontrol.andrew.homecontrollibrary;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Created by andrew on 12/26/14.
 */
public class HANService extends Service {
    private static final String TAG = "HAN Service";
    private Context context;
    private HANService hanService;
    private NetworkData networkData;
    private HashSet<Messenger> listOfObservers = new HashSet();
    // this gets passed to clients for them to make calls back
    private Messenger mMessenger = new Messenger(new IncomingMessageHandler());

    // keys for storing data to preferences
    private static final String APP_PREFERENCES_KEY = "app_preferences";
    private static final String LAST_NETWORK_USED_KEY = "lastNetworkUsed";
    private static final String NETWORK_LIST_KEY = "networkList";
    private static final String PASSCODE_KEY = "passcode";
    private static final String NETWORK_NAME_KEY = "networkName";
    private static final String URL_ADDRESS_KEY = "urlAddress";


    // constants for determining HANService requests
    public static final int DOWNLOAD_OP = 0;
    public static final int UPLOAD_OP = 1;
    public static final int DOWNLOAD_DONE = 2;
    public static final int VALIDATE_LOGIN_OP = 3;
    public static final int LOAD_APP_DATA = 4;
    public static final int LOAD_NETWORK_DATA = 5;
    public static final int SAVE_APP_DATA = 6;
    public static final int CLEAR_APP_PREFERENCES = 7;
    public static final int UPDATE_CURRENT_NETWORK = 8;
    public static final int CREATE_NEW_NETWORK = 9;
    public static final int SAVE_NETWORK = 10;
    public static final int REMOVE_NETWORK = 11;
    public static final int SET_PASSCODE = 12;


    // String KEYs for data in bundles RETURNING
    public static final String LOGIN_RESULT = "login_result";
    public static final String LOAD_APP_DATA_RESULT = "load_MRA_result";
    public static final String LOAD_APP_DATA_NETWORK_NAME = "load_MRA_network_name";
    public static final String LOAD_APP_DATA_NETWORK_ADDRESS = "load_MRA_network_address";
    public static final String LOAD_APP_DATA_NETWORK_LIST = "load_MRA_network_list";
    public static final String MODULE_DOWNLOAD_STRING = "module_string";
    public static final String LOAD_NETWORK_DATA_NETWORK_NAME = "load_network_data_name";
    public static final String LOAD_NETWORK_DATA_NETWORK_ADDRESS = "load_network_data_address";

    private void saveAppPreferences(){
        // saves data for the app, data like the last networkData the used, all networks available, and app passcode
        Log.d(TAG, "saving App Preferences");
        Log.d(TAG, "LRU network: " + networkData.getNetworkName());
        Log.d(TAG, "ip: " + networkData.getNetworkAddress());
        Log.d(TAG, "network list: " + networkData.getNetworkList().toString());
        //networkData.setLastNetworkUsed(networkData.getNetworkName());  // update last used networkData to current networkData
        SharedPreferences sharedPrefs = context.getSharedPreferences(APP_PREFERENCES_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.clear();     // clear existing data before writing
        editor.putString(LAST_NETWORK_USED_KEY, networkData.getNetworkName());    // store the current network
        editor.putStringSet(NETWORK_LIST_KEY, networkData.getNetworkList());      // store list of networks
        editor.putString(PASSCODE_KEY, networkData.getPasscode());            // store passcode for logging into the app
        editor.commit();
    }

    private void saveNetworkPreference(){
        // saves data to a sharedPreferences file associated with the networkData name. if a file does not exist one is created
        Log.d(TAG, "saving preferences for " + networkData.getNetworkName() + " network");
        SharedPreferences sharedPrefs = this.getSharedPreferences(networkData.getNetworkName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(NETWORK_NAME_KEY, networkData.getNetworkName());
        editor.putString(URL_ADDRESS_KEY, networkData.getNetworkAddress());
        editor.commit();
    }

    private void readNetworkPreference(String networkName){
        // reads preferences for a specific network, to get its address
        Log.d(TAG, "reading preferences for " + networkName);
        SharedPreferences sharedPrefs = this.getSharedPreferences(networkName, Context.MODE_PRIVATE);
        String name = sharedPrefs.getString(NETWORK_NAME_KEY, null);
        String ip = sharedPrefs.getString(URL_ADDRESS_KEY, null);
        networkData.setNetworkName(sharedPrefs.getString(NETWORK_NAME_KEY, null));
        networkData.setNetworkAddress(sharedPrefs.getString(URL_ADDRESS_KEY, null));
        Log.d(TAG, "Current Network data\nName: " + networkData.getNetworkName() +
                "\nAddress: " + networkData.getNetworkAddress() +
                "\nPasscode: " + networkData.getPasscode());
    }

    private void removeNetworkPreference(String networkName){
        Log.d(TAG, "removing: " + networkName + " from preferences");
        networkData.removeNetworkFromList(networkName);
        SharedPreferences sharedPrefs = this.getSharedPreferences(networkName, Context.MODE_PRIVATE);       // get preferences for the network we want to remove
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.clear(); // completely remove this shared preference data
        editor.commit();
    }

    private boolean readAppPreferences(){
        // reads stored data for the app, data like the last network used, all networks available, and app passcode
        boolean haveAccount = false;
        Log.d(TAG, "reading App Preferences");
        SharedPreferences sharedPrefs = this.getSharedPreferences(APP_PREFERENCES_KEY, Context.MODE_PRIVATE);
        networkData.setNetworkName(sharedPrefs.getString(LAST_NETWORK_USED_KEY, null));    // "networkData" will store the last networkData logged into by the user
        networkData.loadNetworkList((HashSet<String>) sharedPrefs.getStringSet(NETWORK_LIST_KEY, null));  // passes list of all users networks to networkData object
        networkData.setPasscode(sharedPrefs.getString(PASSCODE_KEY, null));

        Log.d(TAG, "Network List: " + networkData.getNetworkList().toString());

        if(networkData.getNetworkName() != null){
            // if there was an account to load
            readNetworkPreference(networkData.getNetworkName());      // read preferences for the network just set
            haveAccount = true;
        }else{
            Log.d(TAG, "No available networks");
        }
        Log.d(TAG, "finished reading App Preferences");
        return haveAccount;
    }



    private void clearAppPreferences(){
        // will erase shared preferences for the app, last used network, network list, and passcode
        // **** if this clears the network list I will also need to erase all network preferences as well ***
        for(String name : networkData.getNetworkList().toArray(new String[networkData.getNetworkList().size()])){
            removeNetworkPreference(name);
        }
        SharedPreferences sharedPrefs = this.getSharedPreferences(APP_PREFERENCES_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.clear();
        editor.commit();
        Log.d(TAG, "All App Preferences removed");
//        networkData.removeNetworkFromList(network);    // remove this networkData from the list of user's networks
//
//        // this is if the user deletes the account they were logged into
//        if(network.equals(networkData.getNetworkName())) {    // only do this if the user deletes the account they are currently using
//            unlocked = false;   // this must be set false since it is a class variable, starting a new mobileActivity would
//            Iterator<String> iterator = networkData.getNetworkList().iterator();
//            if (iterator.hasNext()) {
//                networkData.setLastNetworkUsed(iterator.next());
//                networkData.setNetworkName(networkData.getLastNetworkUsed());  // I do this because saveAppPreferences will assign networkData to lastUsedNetwork and save
//                Log.d(TAG, "last used networkData is now " + networkData.getLastNetworkUsed());
//            } else {
//                networkData.setLastNetworkUsed(null); // change all this data, so if the user leaves the app before logging into another account, it does not keep the data that should be deleted
//                networkData.setNetworkAddress(null);
//                networkData.setNetworkName(null);
//                networkData.setPasscode("-1");
//            }
//            newMainActivity();  // force "logout" and have user log in to another account
//        }
    }

    private void updateNetworkData(String newName, String newAddress){
        // this method is used for updating a network
//        Log.d(TAG, "Network name: " + networkData.getNetworkName());
//        Log.d(TAG, "Network Addr: " + networkData.getNetworkAddress());
        removeNetworkPreference(networkData.getNetworkName());      // remove current network
        networkData.removeNetworkFromList(networkData.getNetworkName());    // remove the current network from the list of networks
        networkData.setNetworkName(newName);        // update the current network name
        networkData.setNetworkAddress(newAddress);  // update the current network address
        networkData.addNetworkToList(newName);
        Log.d(TAG, "New Network name: " + networkData.getNetworkName());
        Log.d(TAG, "New Network Addr: " + networkData.getNetworkAddress());
        saveNetworkPreference();
    }

    private void createNewNetwork(String newName, String newAddress){
        // this method is used for creating a new network
        removeNetworkPreference(newName);      // try to remove the network in case the user tries to create duplicate networks
        networkData.removeNetworkFromList(newName);    // also remove the new network from the list of networks just in case
        networkData.setNetworkName(newName);        // update the current network name
        networkData.setNetworkAddress(newAddress);  // update the current network address
        networkData.addNetworkToList(newName);
        Log.d(TAG, "New Network name: " + networkData.getNetworkName());
        Log.d(TAG, "New Network Addr: " + networkData.getNetworkAddress());
        saveNetworkPreference();
        saveAppPreferences();
    }

    private boolean validateLogin(String enteredPasscode){
        boolean success = false;
        if(networkData.getPasscode().equals(enteredPasscode)){
            success = true;     // if the passcodes match set true
        }
        return success;
    }
//
//    private boolean loadMostRecentAccount(){
//        boolean haveAccount = false;
//        readAppPreferences();
//        if(networkData.getNetworkName() != null && !networkData.getNetworkName().equals("")){
//            // if there was an account to load
//            readNetworkPreference(networkData.getNetworkName());      // read preferences for the network just set
//            haveAccount = true;
//        }
//        Log.d(TAG, "Network Name: " + networkData.getNetworkName());
//        Log.d(TAG, "Network Address: " + networkData.getNetworkAddress());
//        return haveAccount;
//    }

    public void returnJSONDownload(String moduleString){
        Log.d(TAG, "return JSON Downlaod called");
        // this calls returnModulesToClients because this is the public method and it is the private method
        returnModulesToClients(moduleString);
    }

    private void returnModulesToClients(String moduleString){
        Log.d(TAG, "modules list: " + moduleString);
        Bundle bundle = new Bundle();
        bundle.putString(MODULE_DOWNLOAD_STRING, moduleString);      // attach the module string response from server to the bundle
        Iterator<Messenger> iterator = listOfObservers.iterator();
        while(iterator.hasNext()){
            Message jsonReply = Message.obtain(null, ServiceFacade.DOWNLOAD_OP_REPLY);
            jsonReply.setData(bundle);          // attach server response bundle to message
            Messenger replyTo = iterator.next();   // get the messenger from the set of Observers
            try {
                Log.d(TAG, "sending result back to ServerFacade");
                replyTo.send(jsonReply);    // send response string to this observer
            } catch (RemoteException e){
                Log.e(TAG, e.toString());
            }
        }
    }

    private void setAppPasscode(String enteredPasscode){
        Log.d(TAG, "passcode will be set to : " + enteredPasscode);
        networkData.setPasscode(enteredPasscode);
        saveAppPreferences();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // passes binder object back to client
        Log.d(TAG, "someone bound to HANService");
        return mMessenger.getBinder();
    }

    class IncomingMessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Log.i(TAG, "handling msg: " + msg.what);
            Bundle bundle;
            switch (msg.what){
                case VALIDATE_LOGIN_OP:
                    // we are not using any threading here so there is no need to store reply Messenger to the stack, we'll just reply immediately
                    String enteredPasscode = msg.getData().getString(ServiceFacade.LOGIN_PASSCODE_KEY);     // get enteredPasscode from bundle
                    boolean success = validateLogin(enteredPasscode);
                    bundle = new Bundle();
                    bundle.putBoolean(LOGIN_RESULT, success);  // attach login result to bundle
                    Message loginReply = Message.obtain(null, ServiceFacade.VALIDATION_REPLY);      // setup a message to send validation reply back to facade
                    loginReply.setData(bundle);          // attach login success bundle to message
                    try {
                        msg.replyTo.send(loginReply);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                case DOWNLOAD_OP:
                    ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                    bundle = msg.getData();
                    int deviceType = bundle.getInt(ServiceFacade.DEVICE_TYPE_KEY);
                    if(networkInfo != null && networkInfo.isConnected()) {
                        if (msg.replyTo == null) {
                            Log.d(TAG, "reply to Messenger is null");
                        } else {
                            listOfObservers.add(msg.replyTo);   // get Messenger reference to client observer and store in hashSet for posting updates later
                            Log.d(TAG, "stored reply to Messenger");
                        }
                        Log.d(TAG, "performing download op");
                        // start thread to download the data from the server
                        // when finished, the download thread performs a callback on returnJSONDownload()
                        if(deviceType == ServiceFacade.MOBILE_DEVICE) {
                            new Thread(new DownloadJSONThread(hanService, networkData.getNetworkAddress() + getString(R.string.requestData_ext))).start();
                        }else{
                            new Thread(new DownloadJSONThread(hanService, networkData.getNetworkAddress() + getString(R.string.requestSortedData_ext))).start();
                        }
                    }else {
                        Log.i(TAG, "NO Network Connection available");
                        // **** make callback to alert of no internet connection
                    }
                    break;
                case UPLOAD_OP:
                    Log.d(TAG, "doing upload op");
                    bundle = msg.getData();
                    // make an array list of the values so I can prepend the ip address and convert back to an array
                    ArrayList<String> valuesList = new ArrayList(Arrays.asList(bundle.getStringArray(ServiceFacade.MODULE_UPLOAD_STRING_KEY)));
                    valuesList.add(0, networkData.getNetworkAddress());     // prepend the ip address to the beginning of the list
                    String[] values = new String[valuesList.size()];
                    values = valuesList.toArray(values);        // pass the array to the array list, the array list converts itself to an array based on the one passed in and also returns a reference to it
                    new UploadTaskNoProgress().execute(values);
                    // send upload confirmation
                    break;
                case DOWNLOAD_DONE:
                    Log.d(TAG, "donwload is done");
                    break;
                case LOAD_APP_DATA:
                    bundle = new Bundle();
                    boolean weHaveAnAccount = readAppPreferences();
                    if(weHaveAnAccount){
                        Log.d(TAG, "loaded " + networkData.getNetworkName() + " network's data");
                        bundle.putBoolean(LOAD_APP_DATA_RESULT, true);   // no previous account attach false to bundle
                        bundle.putString(LOAD_APP_DATA_NETWORK_NAME, networkData.getNetworkName());   // attach current network name to bundle
                        bundle.putString(LOAD_APP_DATA_NETWORK_ADDRESS, networkData.getNetworkAddress());    // attach current network url
                        bundle.putStringArrayList(LOAD_APP_DATA_NETWORK_LIST, new ArrayList<String>(Arrays.asList(networkData.getNetworkListArray())));
                    }else{
                        Log.d(TAG, "no recent network to load");
                        bundle.putBoolean(LOAD_APP_DATA_RESULT, false);  // we have previous account attach true
                    }
                    Message loadMRAreply = Message.obtain(null, ServiceFacade.LOAD_APP_DATA_REPLY);      // setup a message to send result of loading MRA
                    loadMRAreply.setData(bundle);          // attach login success bundle to message
                    try {
                        msg.replyTo.send(loadMRAreply);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                case LOAD_NETWORK_DATA:
                    bundle = msg.getData();
                    String desiredNetwork = bundle.getString(ServiceFacade.LOAD_NETWORK_DATA_NAME_KEY);     // get the name of the network we want to load data for
                    readNetworkPreference(desiredNetwork);
                    Log.d(TAG, "loaded " + networkData.getNetworkName() + " data");
                    bundle.putString(LOAD_NETWORK_DATA_NETWORK_NAME, networkData.getNetworkName());   // attach current network name to bundle
                    bundle.putString(LOAD_NETWORK_DATA_NETWORK_ADDRESS, networkData.getNetworkAddress());    // attach current network url
                    Message loadNetworkDataReply = Message.obtain(null, ServiceFacade.LOAD_NETWORK_DATA_REPLY);      // setup a message to send result of loading MRA
                    loadNetworkDataReply.setData(bundle);
                    try {
                        msg.replyTo.send(loadNetworkDataReply);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    saveAppPreferences();       // save app preferences to store the keep most recent network up to date
                    break;
                case SAVE_APP_DATA:
                    saveAppPreferences();
                    break;
                case CLEAR_APP_PREFERENCES:
                    clearAppPreferences();      // clear app preferences
                    // no response is sent back at this point
                    /// *** maybe in the future I should send them back to the new user page
                    break;
                case UPDATE_CURRENT_NETWORK:
                    bundle = msg.getData();
                    String newName = bundle.getString(ServiceFacade.NEW_NETWORK_NAME_KEY);
                    String newAddress = bundle.getString(ServiceFacade.NEW_NETWORK_ADDRESS_KEY);
                    Log.d(TAG, "updating current data: " + networkData.getNetworkName() + " , " + networkData.getNetworkAddress());
                    updateNetworkData(newName, newAddress);
                    saveAppPreferences();
                    break;
                case CREATE_NEW_NETWORK:
                    bundle = msg.getData();
                    String newNetworkName = bundle.getString(ServiceFacade.NEW_NETWORK_NAME_KEY);
                    String newNetworkAddress = bundle.getString(ServiceFacade.NEW_NETWORK_ADDRESS_KEY);
                    Log.d(TAG, "creating new network: " + newNetworkName + " , " + newNetworkAddress);
                    createNewNetwork(newNetworkName, newNetworkAddress);
                    break;
                case SAVE_NETWORK:
                    Log.d(TAG, "save network msg received");
                    saveNetworkPreference();        // save the current network preferences
                    break;
                case REMOVE_NETWORK:
                    Log.d(TAG, "remove network msg received");
                    Log.d(TAG, "network list before: " + networkData.getNetworkList().toString());
                    bundle = msg.getData();
                    String removeNetworkName = bundle.getString(ServiceFacade.REMOVE_NETWORK_NAME_KEY);
                    removeNetworkPreference(removeNetworkName);
                    saveAppPreferences();       // save changes made to the list of networks
                    Log.d(TAG, "network list after: " + networkData.getNetworkList().toString());
                    break;
                case SET_PASSCODE:
                    Log.d(TAG, "set passcode msg received");
                    bundle = msg.getData();
                    String passcode = bundle.getString(ServiceFacade.SET_PASSCODE_STRING_KEY);
                    setAppPasscode(passcode);
                default:
                    // do nothing
                    break;
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        context = this;
        hanService = this;
        networkData = new NetworkData();
        //setupGoogleClientApi();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //mGoogleApiClient.connect();
        //Wearable.MessageApi.addListener(mGoogleApiClient, new WearEventListener());
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        // Tell the user we stopped.
        Log.d(TAG, "onDestroy");
    }
}
