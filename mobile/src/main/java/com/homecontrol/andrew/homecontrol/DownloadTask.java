package com.homecontrol.andrew.homecontrol;

import android.os.AsyncTask;
import android.util.Log;

import com.homecontrol.andrew.homecontrollibrary.HANService;
import com.homecontrol.andrew.homecontrollibrary.NetworkRequest;

import org.json.JSONArray;

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
public class DownloadTask extends AsyncTask<Void, Void, String> { // first String is what is passed in (.execute(url)), second is for progress update,
    // last if what is passed on doInBackground to onPostExecute. Nothing is returned to the calling method
    private static final String TAG = "DownloadJSONTask";
    private HANService hanService;  // stores a MobileActivity object so I dont have to keep calling getActivity
    private JSONArray jsonArray;
    private String phpUrl;

    public DownloadTask(HANService service, String phpScript){
        hanService = service;
        Log.d(TAG, "beginning download");
        phpUrl = phpScript;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(Void... voids) {
        try {
            return NetworkRequest.request(phpUrl);
        } catch(IOException e) {
            //TODO: Handle this error properly by showing an error to the user
            Log.d(TAG, e.toString());
            return null;
        }
    }

    @Override
    protected void onPostExecute(String result) {
        // if we get a valid result from server
        if (result != null) {
            //create jsonArray and return data to Service
            //this.jsonArray = new JSONArray(result);
            Log.d(TAG, "assigned result to JSONArray");
            hanService.returnJSONDownload(result);
        } else {
            Log.d(TAG, "No response from Server");   // so far so good. if we get an exception we catch it and we will have null here. so i need to deal with the null. change the toast message
        }
    }
}
