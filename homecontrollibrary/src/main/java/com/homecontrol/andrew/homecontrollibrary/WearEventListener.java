package com.homecontrol.andrew.homecontrollibrary;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
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
public class WearEventListener extends WearableListenerService implements MyApiClientInterface, HANServiceObserver {
    private static final String TAG = "Wear Event Listener";
    public static final String GET_MODULES = "/start/get_modules";     // google client api uses strings as messages
    public static final String UPDATE_MODULE = "/start/update_module";
    public static final int GET_MODULES_REPLY = 1;
    private Context context;
    private Handler handler = new Handler();
    private GoogleApiClient mGoogleApiClient;
    private Stack<String> nodeStack = new Stack<>();
    private HashSet<String> nodesList;
    private ServiceFacade serviceFacade;

    private static final String TAG_ADDR = "addr";
    private static final String TAG_NAME = "name";
    private static final String TAG_TYPE = "type";
    private static final String TAG_VALUE = "value";
    private static final String TAG_STATE = "state";

    private int doingWhat = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        this.context = this.getApplicationContext();
        serviceFacade  = ServiceFacade.getInstance(this.getApplicationContext());
        setupGoogleClientApi();         // setup the ApiClient
        mGoogleApiClient.connect();     // connect
        Log.d(TAG, "onCreate: wearableListener is running");
        serviceFacade.loadAppData(ServiceFacade.WEAR_DEVICE);
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

    // *** HANServiceObserver Implementation BEGIN ***

    @Override
    public void receiveJSONFromService(String moduleString) {
        final String returnNode = nodeStack.pop();
        final String data = moduleString;
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
    public void notifyRequestFailed() {
        Log.d(TAG, "no response from server");
    }

    // *** HANServiceObserver Implementation END ***

    // for GoogleApiClient, from device to device
    @Override
    public void onMessageReceived(final MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);
        Log.d(TAG, "Got the MESSAGE!!!!!!!!");
        String eventPath = messageEvent.getPath();
        Log.d(TAG, eventPath);
        if(eventPath.equals(GET_MODULES)){
            nodeStack.push(messageEvent.getSourceNodeId());
            serviceFacade.getModulesString(this);   // request modules string
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
                }
            });
        }
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
}
