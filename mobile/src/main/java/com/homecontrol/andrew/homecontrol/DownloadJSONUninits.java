package com.homecontrol.andrew.homecontrol;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

import com.homecontrol.andrew.homecontrollibrary.NetworkRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by andrew on 7/9/14.
 */
public class DownloadJSONUninits extends AsyncTask<String, Void, Boolean> { // first String is what is passed in (.execute(url)), second is for progress update,
    // last if what is passed on doInBackground to onPostExecute. Nothing is returned to the calling method
    private static final String TAG = "DownloadJSONUninits";
    private static final String GETJSONTEXT_MESSAGE = "getJSONText";
    private NewModuleTaskCommunication commInterface;
    private MobileActivity activity;  // stores a MobileActivity object so I dont have to keep calling getActivity
    private ProgressDialog pDialog;
    private String phpUrl;  // the url for the phpscript is concatenated and passed in to the task
    private String database_response;

    public DownloadJSONUninits(Fragment fragment, String phpScript){
        commInterface = (NewModuleTaskCommunication) fragment;
        activity = (MobileActivity) fragment.getActivity();
        phpUrl = phpScript;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        // show progress dialog
        pDialog = new ProgressDialog(activity);
        pDialog.setMessage(activity.getString(R.string.please_wait));
        pDialog.setCancelable(false);
        pDialog.show();
    }

    @Override
    protected Boolean doInBackground(String... s) {
        try {
            database_response = NetworkRequest.request(phpUrl);
        } catch(IOException e) {
            //TODO: Handle this error properly by showing an error to the user
            Log.d(TAG, e.toString());
            database_response = null;
        }

        return database_response != null;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        // close loading dialog
        if(pDialog.isShowing())
            pDialog.dismiss();
        // if we get a valid result from server
        if (result == true) {
            //create jsonArray and call createButtons
            if(database_response.equals("[]")) {
                Log.d(TAG, "no new modules in database");
                Toast.makeText(activity, "No new modules available", Toast.LENGTH_LONG).show();
            } else {
                Log.d(TAG, database_response);
                commInterface.createNewModuleDialog(database_response);
            }
        } else
            Toast.makeText(activity, "result was NULL", Toast.LENGTH_SHORT).show();   // so far so good. if we get an exception we catch it and we will have null here. so i need to deal with the null. change the toast message
    }

    public interface NewModuleTaskCommunication{
        public void createNewModuleDialog(String result);
    }
}
