package com.homecontrol.andrew.homecontrol;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

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
        database_response = getJSONText(phpUrl);    // might still be null if there was an exception
        if(database_response == null) {
            Log.d(TAG, "server result is null");
            return false;
        } else return true;
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
            }else {
                Log.d(TAG, database_response);
                commInterface.createNewModuleDialog(database_response);
            }
        } else
            Toast.makeText(activity, "result was NULL", Toast.LENGTH_SHORT).show();   // so far so good. if we get an exception we catch it and we will have null here. so i need to deal with the null. change the toast message
    }

    private String getJSONText(String myUrl){
        InputStream is = null;  // input stream
        String content = null;
        try{
            URL url = new URL(myUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();  // openConnetion() may throw IOException
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(5000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.connect();
            int response = conn.getResponseCode();
            Log.d(TAG, "The response is " + response);
            is = conn.getInputStream(); // create input stream from http connection
            content = readInput(is);    // read input stream and extract data as string
            //Log.d(TAG, content);  // trying to catch when the php script might return an error trying to access database
        } catch (MalformedURLException mue){
            Log.e(GETJSONTEXT_MESSAGE, mue.toString());
        } catch (IOException ioe){  // catch IOException of readInput
            Log.e(GETJSONTEXT_MESSAGE, ioe.toString());
        }
        finally{
            if(is != null) {    // if its not null because otherwise we wouldnt have created an inputstream since we have nothing to read
                try {
                    is.close();
                    Log.d(TAG, "closing input stream");
                } catch (IOException e) {
                    Log.e(GETJSONTEXT_MESSAGE, activity.getString(R.string.err_close_is));
                }
            }
            return content; // return string read from input stream
        }
    }

    private String readInput(InputStream stream) throws IOException{
        Log.d(TAG, "reading inputStream");
        String result = "";
        BufferedReader reader = null;
        reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
        String buffer;
        while((buffer = reader.readLine()) != null){
            result += buffer;
        }
        return result;
    }

    public interface NewModuleTaskCommunication{
        public void createNewModuleDialog(String result);
    }
}
