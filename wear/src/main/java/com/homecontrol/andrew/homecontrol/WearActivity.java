package com.homecontrol.andrew.homecontrol;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.homecontrol.andrew.homecontrollibrary.Dimmer;
import com.homecontrol.andrew.homecontrollibrary.Module;
import com.homecontrol.andrew.homecontrollibrary.Outlet;
import com.homecontrol.andrew.homecontrollibrary.WearEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;

public class WearActivity extends Activity implements MessageApi.MessageListener{
    private static String TAG = "WearActivity";
    private WearActivity activity;
    private GoogleApiClient mGoogleApiClient;
    private ArrayList<Module> mods;
    private HashSet<String> nodesList;
    private FragmentManager fragmentManager;

    private static final String TAG_ADDR = "addr";
    private static final String TAG_NAME = "name";
    private static final String TAG_TYPE = "type";
    private static final String TAG_VALUE = "value";
    private static final String TAG_STATE = "state";

    public static final String SERVER_DOWNLOAD_REPLY = "/start/download_reply";

    public void parseJSON(JSONArray json){
        deserialize(json);
    }


    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.e(TAG, "Got the MESSAGE!!!!!!!!");
        String eventPath = messageEvent.getPath();
        Log.d(TAG, eventPath);
        if(eventPath.equals(SERVER_DOWNLOAD_REPLY)){
            byte[] b = messageEvent.getData();
            String hiddenMsg = null;
            try {
                hiddenMsg = new String(b, "UTF-8");
                Log.d(TAG, hiddenMsg);
                try {
                    JSONArray json = new JSONArray(hiddenMsg);      // convert string to JSONArray
                    parseJSON(json);    // pass the json array to be parsed/deserialized into modules
                    createCards();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }else{
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate");
        setContentView(R.layout.activity_wear);

        fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        WaitingFragment waitingFragment = new WaitingFragment();
        //ModuleCardFragment cardFragment = new ModuleCardFragment();
        fragmentTransaction.add(R.id.fragment_container, waitingFragment).commit();   // add card to frame layout

        this.activity = this;

        setupGoogleClientApi();

//        FragmentManager fragmentManager = getFragmentManager();
//        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//        CardFragment card = CardFragment.create("Title", "this is what will happen");
//        fragmentTransaction.add(R.id.frame_layout, card);   // add card to frame layout
//        fragmentTransaction.commit();

    }



    private void createCards(){
        ModuleCardFragment moduleFragment = new ModuleCardFragment();
        fragmentManager.beginTransaction().replace(R.id.fragment_container, moduleFragment).commit();   // add card to frame layout


//        pager.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                Log.d(TAG, "touched");
////                Intent intent = new Intent(activity, ConfirmationActivity.class);
////                intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE,
////                        ConfirmationActivity.SUCCESS_ANIMATION);
////                intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE, "Event happening");
////                startActivity(intent);
//                return false;
//            }
//        });
    }

    public ArrayList<Module> getModuleList(){
        return mods;
    }

    public void setNodesInMain(HashSet<String> nodes) {
        nodesList = nodes;
        Log.d(TAG, "setNodes in main: nodesList has been set");
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(!mGoogleApiClient.isConnected()) {
            Log.d(TAG, "onStart: connecting");
            mGoogleApiClient.connect();
        }else{
            Log.d(TAG, "onStart: already connected");
        }
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        Wearable.MessageApi.removeListener(mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    private String getPrimaryNode(){
        String node = nodesList.iterator().next();  // get node from list
        return node;
    }

    public void deserialize(JSONArray jsonArray){
        mods = new ArrayList<Module>();
        JSONObject jsonObject;
        String type, name;

        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                type = jsonObject.getString(TAG_TYPE);

                if(type.equals("outlet")) {
                    mods.add(new Outlet(jsonObject.getString(TAG_ADDR), jsonObject.getString(TAG_NAME), jsonObject.getString(TAG_STATE)));
                } else if(type.equals("dimmer")) {
                    // new dimmer
                    mods.add((new Dimmer(jsonObject.getString(TAG_ADDR), jsonObject.getString(TAG_NAME), jsonObject.getString(TAG_STATE), jsonObject.getString(TAG_VALUE))));
                } else if(type.equals("temp_outlet")) {
                    // new temp_outlet
                    mods.add(new Outlet(jsonObject.getString(TAG_ADDR), jsonObject.getString(TAG_NAME), jsonObject.getString(TAG_STATE)));   // doing it anyway for now since i dont have a temp
                } else
                    throw new IllegalArgumentException("Invalid module type found");
            }
        } catch (JSONException je){
            Log.e(TAG, je.toString());
        } catch (IllegalArgumentException iae){
            Log.e(TAG, iae.toString());
        }
        Log.d(TAG, "deserialized");
        Log.d(TAG, "made " + mods.size() + " modules");
    }

    public void sendMessageGetModules(){
        String theNode = getPrimaryNode();
        Log.d(TAG, "sending message from main to : " + theNode);
        Wearable.MessageApi.sendMessage(mGoogleApiClient, theNode, WearEventListener.GET_MODULES, null);
    }

    // this is complete garbage but I just want to get it working
    public void flipModuleState(String addr){
//        ArrayList<Module> modsList = networkData.getModules();
        Module m;
        for(int i = 0; i < mods.size(); i++){
            m = mods.get(i);
            if(m.getAddr().equals(addr)){
                ((Outlet)m).flipState();
                sendMessageUpdateModule(i);
            }
        }
    }

    private void sendMessageUpdateModule(int moduleIndex){
        Outlet module = (Outlet) mods.get(moduleIndex);
        ArrayList<String> list = new ArrayList();
        list.add("addr");
        list.add(module.getAddr());
        list.add("name");
        list.add(module.getName());
        list.add("value");
        list.add(module.getState());
//        String test = "[{\"addr\":\"0012,02,7558\",\"name\":\"Outside Lights\",\"value\":\"0\"}]";
        String newState = "";
        if(module.getState().equals("1")){
            newState = "1";
        }else{
            newState = "0";
        }

        String values = "[{\"addr\":\"" + module.getAddr()
                + "\",\"name\":\"" + module.getName()
                + "\",\"state\":\"" + newState + "\"}]";

        Log.d(TAG, "sending: " + values);

        byte[] sendingBytes = null;
        try {
            sendingBytes = values.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String theNode = getPrimaryNode();
        Wearable.MessageApi.sendMessage(mGoogleApiClient, theNode, "/start/update_module", sendingBytes);
    };

    private void setupGoogleClientApi(){
        mGoogleApiClient = new GoogleApiClient.Builder(activity)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle connectionHint) {
                        Log.d(TAG, "onConnected: " + connectionHint);
                        // Now you can use the Data Layer API
                        // Connected to Google Play services!
                        // The good stuff goes here.
                        Log.e(TAG, "setting up listener");
                        Wearable.MessageApi.addListener(mGoogleApiClient, activity);        // connect the listener for responses from other devices
                        new NodeAsyncTask(activity, mGoogleApiClient).execute();            // call thread to initialize the nodesList HashSet and call the sendMessage to get Modules from phone
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
