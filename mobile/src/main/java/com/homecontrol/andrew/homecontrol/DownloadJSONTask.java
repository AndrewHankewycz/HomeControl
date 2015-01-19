package com.homecontrol.andrew.homecontrol;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.homecontrol.andrew.homecontrollibrary.NetworkRequest;

import org.json.JSONArray;
import org.json.JSONException;

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
public class DownloadJSONTask extends AsyncTask<Void, Void, String> { // first String is what is passed in (.execute(url)), second is for progress update,
    // last if what is passed on doInBackground to onPostExecute. Nothing is returned to the calling method
    private static final String TAG = "DownloadJSONTask";
    private static final String GETJSONTEXT_MESSAGE = "getJSONText";
    private MobileActivity activity;  // stores a MobileActivity object so I dont have to keep calling getActivity
    private ProgressDialog pDialog;
    private JSONArray jsonArray;
    private String phpUrl;
    //private HttpsURLConnection urlConnection;

    public DownloadJSONTask(MobileActivity mainActivity, String phpScript){
        activity = mainActivity;
        Log.d(TAG, "beginning download");
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
        // close loading dialog
        if(pDialog.isShowing())
            pDialog.dismiss();
        // if we get a valid result from server
        if (result != null) {
            try {
                //create jsonArray and c12all createButtons
                Log.d(TAG, result);
                this.jsonArray = new JSONArray(result);
                Log.d(TAG, "assigned result to JSONArray");
                activity.parseJSON(jsonArray);
                activity.updateButtons();   // once the response has been parsed we can make buttons
            } catch (JSONException e){
                Log.e(TAG, e.toString());
            }
        } else {
            Toast.makeText(activity, "No response from Server", Toast.LENGTH_SHORT).show();   // so far so good. if we get an exception we catch it and we will have null here. so i need to deal with the null. change the toast message
            activity.switchToRetryFragment();   // if the server does not respond, go to retry fragment
        }
    }
}
