package com.homecontrol.andrew.homecontrollibrary;

import android.os.Handler;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by andrew on 12/25/14.
 */
public class DownloadJSONThread implements Runnable {
    private static final String TAG = "ServerCommThread";
    private HANService service;
    private String phpUrl;
    private Handler handler = new Handler();

    public DownloadJSONThread(HANService service, String phpScript){
        this.service = service;
        phpUrl = phpScript;
    }
    @Override
    public void run() {
        try {
            final String response = NetworkRequest.request(phpUrl);

            handler.post(new Runnable() {
                public void run() {
                    service.returnJSONDownload(response);      // return data to service
                    }
            });
        } catch(IOException e) {
            //TODO: Handle this error properly by showing an error to the user
            Log.d(TAG, e.toString());
        }
    }
}
