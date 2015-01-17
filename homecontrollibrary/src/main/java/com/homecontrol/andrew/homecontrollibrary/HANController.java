package com.homecontrol.andrew.homecontrollibrary;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by andrew on 12/27/14.
 */
public class HANController extends Activity{
    private static final String TAG = "HANController";
    private static final String TAG_ADDR = "addr";
    private static final String TAG_NAME = "name";
    private static final String TAG_TYPE = "type";
    private static final String TAG_VALUE = "value";
    private static final String TAG_STATE = "state";
    private ArrayList<Module> mods;     // list of modules the user can interact with
    private boolean mIsBound = false;   // to keep track if we are bound to the service of not

    private Messenger mService = null;  // Messenger object for communicating with the HANService
    final Messenger mMessenger = new Messenger(new IncomingHandler());  // "this" Messenger object to be given to the HANService for the service to make callbacks

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        Intent i = new Intent(this, HANService.class);
        this.getApplicationContext().startService(i);   // service is only started once no matter how many times startService() is called
        this.getApplicationContext().bindService(i, mConnection, 0);
    }

    private class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANServiceObserver.GET_MODULES_REPLY:
                    Log.d(TAG, "received Module list from HANService");
                    Bundle bundle = msg.getData();
                    String modulesString = bundle.getString("mods_string");     // get the module response string from server (JSON formatted)
                    Log.d(TAG, modulesString);
                    try{
                        JSONArray json = new JSONArray(modulesString);  // convert string response from server to a JSONArray object
                        deserialize(json);      // pass the JSONArray object to be deserialized into the list of Modules
                    }catch(JSONException e){
                        Log.e(TAG, e.toString());
                    }
                    break;
                case HANServiceObserver.GET_NETWORK_REPLY:
                    Log.d(TAG, "received Network list from HANService");
                    // do stuff with network list
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  We are communicating with our
            // service through an IDL interface, so get a client-side
            // representation of that from the raw service object.

            // create the messenger object from the binder we were just passed, in arguments
            mService = new Messenger(service);
            mIsBound = true;    // mark that we are bound

            Log.d(TAG, "service connected");
            // We want to monitor the service for as long as we are
            // connected to it.
            try {
                Log.d(TAG, "sending download message to service");
                Message msg = Message.obtain(null, HANService.DOWNLOAD_OP);
                msg.replyTo = mMessenger;   // attach an reference to self, for the service callback methods
                mService.send(msg);     // send message to service

                // Give it some value as an example.
                msg = Message.obtain(null, HANService.UPLOAD_OP, this.hashCode(), 0);
                msg.replyTo = mMessenger;
                mService.send(msg);
            } catch (RemoteException e) {
                // In this case the service has crashed before we could even
                // do anything with it; we can count on soon being
                // disconnected (and then reconnected if it can be restarted)
                // so there is no need to do anything here.
            }
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

            /*  maybe i can compile with java 1.7 and i can use a switch with string
                switch (type) {
                    case "outlet":
                        MobileActivity.modules[i] = new Outlet(activity, jsonObject.getString(TAG_ADDR), jsonObject.getString(TAG_NAME), jsonObject.getString(TAG_STATE));
                        break;
                    case "temp_outlet":
                        // new temp_outlet
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid module type found");
                }
                */
            }
        } catch (JSONException je){
            Log.e(TAG, je.toString());
        } catch (IllegalArgumentException iae){
            Log.e(TAG, iae.toString());
        }
        //fragComm.syncModuleArrays(mods);          // during revision im commenting this out. not sure if it will be used anymore
        Log.d(TAG, "deserialized");
    }
}
