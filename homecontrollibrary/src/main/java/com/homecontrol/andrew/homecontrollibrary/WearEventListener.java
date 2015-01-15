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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Stack;

/**
 * Created by andrew on 12/25/14.
 */
public class WearEventListener extends WearableListenerService implements MyApiClientInterface {
    private static final String TAG = "Wear Event Listener";
    public static final String GET_MODULES = "/start/get_modules";     // google client api uses strings as messages
    public static final String UPDATE_MODULE = "/start/update_module";
    public static final int GET_MODULES_REPLY = 1;
    private Context context;
    private Handler handler = new Handler();
    private GoogleApiClient mGoogleApiClient;
    //private Stack<String> nodeStack = new Stack<>();
    private MyStack nodeStack = MyStack.getInstance();
    private HashSet<String> nodesList;
    private MyApiClientInterface self;
    private ServiceFacade serviceFacade;

    private static final String TAG_ADDR = "addr";
    private static final String TAG_NAME = "name";
    private static final String TAG_TYPE = "type";
    private static final String TAG_VALUE = "value";
    private static final String TAG_STATE = "state";

    private int doingWhat = 0;


    // Messeger for communicating with service
    Messenger mService = null;
    // to indicate if we have commection
    boolean mIsBound;




    @Override
    public void onCreate() {
        super.onCreate();
        this.context = this.getApplicationContext();
        self = (MyApiClientInterface) this;
        serviceFacade  = ServiceFacade.getInstance(this.getApplicationContext(), this);
        setupGoogleClientApi();         // setup the ApiClient
        mGoogleApiClient.connect();     // connect
        Log.e(TAG, "onCreate: wearableListener is running");
        serviceFacade.loadAppData(ServiceFacade.WEAR_DEVICE);
    }

//    // for handling IPC messages from HANServer
//    private class IncomingHandler extends Handler {
//        @Override
//        public void handleMessage(final Message msg) {
//            Log.d(TAG, "getting reply");
//            Bundle bundle = msg.getData();
//            final String result = bundle.getString("mods_string");        // this is where I left off, the string is null for some reason
//            switch (msg.what) {
//                case GET_MODULES_REPLY:
//                    Log.d(TAG, "message is JSON response");
//                    new Thread(new Runnable() {
//                        @Override
//                        public void run() {
////
//                            Log.d(TAG, "unpacked bundle: " + result);
////                            Thread nodesThread = new Thread(new GetClientNodesRunnable(self, mGoogleApiClient));
////                            nodesThread.start();
////                            try {
////                                nodesThread.join();
////                            } catch (InterruptedException e) {
////                                e.printStackTrace();
////                            }
//                            String returnNode = nodeStack.pop();
//                            sendJSONBackToDevice(returnNode, result);
//                        }
//                    }).start();
//                    break;
//                default:
//                    super.handleMessage(msg);
//                    Log.d(TAG, "message is not recognizable");
//                    break;
//            }
//        }
//    }

    public void sendJSONBackToDevice(final String data){
        Log.e(TAG, "nodeStack size: " + nodeStack.size());
        Log.d(TAG, "pointer: " + System.identityHashCode(this));
        final String returnNode = nodeStack.pop();
        Log.d(TAG, "sending back to watch: " + data);
        Log.d(TAG, "to addr: " + returnNode);

        new Thread(new Runnable() {
            public void run() {
                byte[] returningBytes = null;
                try {
                    returningBytes = data.getBytes("UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(mGoogleApiClient, returnNode, "/start/download_reply", returningBytes).await();
                if (!result.getStatus().isSuccess()) {
                    Log.e(TAG, "ERROR: failed to send Message: " + result.getStatus());
                }
            }
        }).start();
    }

    @Override
    public void setNodes(HashSet<String> nodes) {
        nodesList = nodes;
        Log.d(TAG, "setNodes: nodesList has been set");
        getPrimaryNode();
    }

    private String getPrimaryNode(){
        String node = nodesList.iterator().next();  // get node from list
        Log.d(TAG, "sending message to : " + node);
        Wearable.MessageApi.sendMessage(mGoogleApiClient, node, "/start/download_reply", null);
        return node;
    }


    // for GoogleApiClient, from device to device
    @Override
    public void onMessageReceived(final MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);
        Log.e(TAG, "Got the MESSAGE!!!!!!!!");
        String eventPath = messageEvent.getPath();
        nodeStack.push(messageEvent.getSourceNodeId());
        Log.e(TAG, "nodeStack size: " + nodeStack.size());
        Log.d(TAG, eventPath);
        if(eventPath.equals(GET_MODULES)){
            serviceFacade.getModulesString(ServiceFacade.WEAR_DEVICE);   // request modules string
//            doingWhat = 0;
//            handler.post(new Runnable() {
//                public void run() {
//                    Log.d(TAG, "starting service");
//                    if(!mIsBound) {
//                        Intent i = new Intent(context, HANService.class);
//                        context.bindService(i, mConnection, Context.BIND_AUTO_CREATE);
//                    }else{
//                        requestDownload();
//                    }
//                    // do stuff
//                }
//            });
        }else if(eventPath.equals(UPDATE_MODULE)){
            doingWhat = 1;
            handler.post(new Runnable() {
                public void run() {
                    // do stuff
                    byte[] b = messageEvent.getData();
                    String values = null;
                    try {
                        values = new String(b, "UTF-8");
                        Log.d(TAG, values);
                        String[] valuesStringArray = parseUpdateJSON(values);
                        sendModifyModuleRequest(valuesStringArray);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    if(!mIsBound) {
                        //Intent i = new Intent(context, HANService.class);
                        //context.bindService(i, mConnection, Context.BIND_AUTO_CREATE);    *** theses will change to use the facade
                    }else{
                        //requestUpload();
                    }
                }
            });
        }
    }

    private String[] parseUpdateJSON(String values){
        JSONArray jsonArray = null;
        ArrayList<String> updateValues = new ArrayList<>();
        try {
            jsonArray = new JSONArray(values);      // convert string to JSONArray
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONObject jsonObject;
        String addr = "", name = "", value = "";

        if(jsonArray != null) {
            try {
                for (int i = 0; i < jsonArray.length(); i++) {
                    jsonObject = jsonArray.getJSONObject(i);
                    addr = jsonObject.getString(TAG_ADDR);
                    name = jsonObject.getString(TAG_NAME);
                    value = jsonObject.getString(TAG_STATE);
                }
            } catch (JSONException je) {
                Log.e(TAG, je.toString());
            } catch (IllegalArgumentException iae) {
                Log.e(TAG, iae.toString());
            }
            Log.d(TAG, "update message deserialized");

            updateValues.add("name");
            updateValues.add(name);
            updateValues.add("state");
            updateValues.add(value);
//            updateValues.add("addr");
            updateValues.add(addr);
            Log.d(TAG, "updateValue: " + updateValues);
        }
        String[] valuesStringArray = new String[updateValues.size()];
        for(int i = 0; i < updateValues.size(); i++){
            valuesStringArray[i] = updateValues.get(i);
        }
        return valuesStringArray;
    }

    private void sendModifyModuleRequest(String[] valueArray){
        serviceFacade.modifyModuleData(valueArray);
    }

    private void setupGoogleClientApi(){
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle connectionHint) {
                        Log.d(TAG, "GoogleApiClient onConnected: " + connectionHint);
                        // Now you can use the Data Layer API
                        // Connected to Google Play services!
                        // The good stuff goes here.
                    }
                    @Override
                    public void onConnectionSuspended(int cause) {
                        Log.d(TAG, "onConnectionSuspended: " + cause);
                        // The connection has been interrupted.
                        // Disable any UI components that depend on Google APIs
                        // until onConnected() is called.
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {
                        Log.d(TAG, "onConnectionFailed: " + result);
                        // This callback is important for handling errors that
                        // may occur while attempting to connect with Google.
                    }
                })
                        // Request access only to the Wearable API
                .addApi(Wearable.API)
                .build();
    }


//    private void requestDownload(){
//        try {
//            Message msg = Message.obtain(null, HANService.DOWNLOAD_OP);         // create message to send to HANService to request modules query
//            msg.replyTo = mMessenger;       // add a reference for HANService to reply to
//            mService.send(msg);             // send the message to HANService
//        } catch (RemoteException e) {
//            // In this case the service has crashed before we could even
//            // do anything with it; we can count on soon being
//            // disconnected (and then reconnected if it can be restarted)
//            // so there is no need to do anything here.
//        }
//    }
//    private void requestUpload(){
//        try {
//            int arraySize = updateValues.size();
//            String[] values = new String[arraySize];
//            for(int i = 0; i < arraySize; i++){
//                values[i] = updateValues.get(i);
//            }
//            Bundle bundle = new Bundle();
//            bundle.putStringArray("values", values);
//            Message msg = Message.obtain(null, HANService.UPLOAD_OP);         // create message to send to HANService to request modules query
//            msg.replyTo = mMessenger;       // add a reference for HANService to reply to
//            msg.setData(bundle);
//            mService.send(msg);             // send the message to HANService
//        } catch (RemoteException e) {
//            // In this case the service has crashed before we could even
//            // do anything with it; we can count on soon being
//            // disconnected (and then reconnected if it can be restarted)
//            // so there is no need to do anything here.
//        }
//    }


    //    // this is what I will give to the service to make calls back
//    final Messenger mMessenger = new Messenger(new IncomingHandler());

//    // for IPC between services/processes
//    private ServiceConnection mConnection = new ServiceConnection() {
//        public void onServiceConnected(ComponentName className, IBinder service) {
//            // This is called when the connection with the service has been
//            // established, giving us the service object we can use to
//            // interact with the service.  We are communicating with our
//            // service through an IDL interface, so get a client-side
//            // representation of that from the raw service object.
//
//            // create the messenger object from the binder we were just passed, in arguments
//            Log.d(TAG, "connected to HANService");
//            mService = new Messenger(service);
//            mIsBound = true;    // mark that we are bound
//
//            // We want to monitor the service for as long as we are
//            // connected to it.
//            if(doingWhat == 0) {
//                Log.d(TAG, "sending download request to HANService");
//                requestDownload();
//            }else if(doingWhat == 1){
//                Log.d(TAG, "sending upload request to HANService");
//                requestUpload();
//            }
//
//            Log.d(TAG, "message has been sent to HANService");
//            // now just wait for the service to send back and continue the process back to the wear device
//        }
//
//        public void onServiceDisconnected(ComponentName className) {
//            // This is called when the connection with the service has been
//            // unexpectedly disconnected -- that is, its process crashed.
//            // toss out binder
//            mService = null;
//            mIsBound = false;
//            Log.d(TAG, "disconnected from service");
//        }
//
//    };


//    private void tesReply(){
//        // works for debugging
//        String nodeToReturnTo = nodeStack.pop();
//        Log.d(TAG, "sending back to : " + nodeToReturnTo);
//        String test = "I want to go ice racing";
//        byte[] b = null;
//        try {
//            b = test.getBytes("UTF-8");
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//        Wearable.MessageApi.sendMessage(mGoogleApiClient, nodeToReturnTo, "/start/download_reply", b);
//    }

}
