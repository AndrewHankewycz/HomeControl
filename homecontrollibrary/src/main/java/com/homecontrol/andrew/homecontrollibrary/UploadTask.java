package com.homecontrol.andrew.homecontrollibrary;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by andrew on 8/6/14.
 */
public class UploadTask extends AsyncTask<String, Void, Void> {
    private static final String TAG = "Upload Task";
    HANController activity;
    ProgressDialog pDialog;

    public UploadTask(Activity activity){
        this.activity =  (HANController) activity;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        pDialog = new ProgressDialog(activity);
        pDialog.setMessage("Making Changes...");
        pDialog.setCancelable(false);
        pDialog.show();
    }

    private String getPost(String... strings){
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(int i = 0; i < strings.length; i += 2) {
            if (first) {
                first = false;
                result.append("[");
            } else {
                result.append(", ");
            }
            result.append(strings[i]);
            result.append("=");
            result.append(strings[i + 1]);
        }
        result.append("]");
        return result.toString();
    }

    @Override
    protected Void doInBackground(String... strings) {
        // the string array should always have the addr as the last value

        // do stuff with data................................................
        // data should be formatted like so, "addr", "column name", "value", "column name", "value"...
        // address is always first, no need to put the column name

//        // ****** new
//        OutputStream os = null;
//        try{
//            URL url = new URL(activity.getNetworkAddress() + activity.getString(R.string.updateModule_ext));
//            Log.d(TAG, activity.getNetworkAddress() + activity.getString(R.string.updateModule_ext));
//            HttpURLConnection conn = (HttpURLConnection) url.openConnection();  // openConnetion() may throw IOException
//            conn.setConnectTimeout(5000);
//            conn.setReadTimeout(5000);
//            conn.setRequestMethod("POST");
//            conn.setDoOutput(true);
//            conn.setChunkedStreamingMode(0);
//
//            String s = getPost(strings);
//            Log.d(TAG, s);
//            os = conn.getOutputStream();
//            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
//            writer.write(s);
//            conn.connect();
//            Log.d(TAG, "connected");
//            int response = conn.getResponseCode();
//            Log.i(TAG, "The response is " + response);
//            //is = conn.getInputStream(); // create input stream from http connection
//            //content = readInput(is);    // read input stream and extract data as string
//        } catch (MalformedURLException mue){
//            Log.e(TAG, mue.toString());
//        } catch (IOException ioe){  // catch IOException of readInput
//            Log.e(TAG, ioe.toString());
//        }
//        finally{
//            if(os != null) {
//                try {
//                    os.close();
//                } catch (IOException e) {
//                    Log.e(TAG, activity.getString(R.string.err_close_is));
//                }
//            }
//            return null;    // nothing is read from an input stream so return null
//        }
//        // *****new


        HttpClient httpclient = new DefaultHttpClient();
        HttpParams connParams = httpclient.getParams();
        HttpConnectionParams.setConnectionTimeout(connParams, 5000);    // connection timeout, 5 seconds
        HttpConnectionParams.setSoTimeout(connParams, 5000);    // socket timeout, 5 seconds
        HttpPost httppost = new HttpPost("http://192.168.1.112" + "/homeauto/updateModule.php");

        try{
            List dataPairs = new ArrayList();
            for(int i = 0; i < strings.length; i++) {
                dataPairs.add(new BasicNameValuePair(Integer.toString(i), strings[i]));
            }
            httppost.setEntity(new UrlEncodedFormEntity(dataPairs));
            HttpResponse response = httpclient.execute(httppost);
            Log.d(TAG, "The response is " + response);
            Log.d(TAG, "sent to database");
        } catch (ClientProtocolException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
        finally{
            return null;
        }
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (pDialog.isShowing())
            pDialog.dismiss();

        Toast.makeText(activity, "Upload Successful", Toast.LENGTH_SHORT).show();   // i need to check whether it was really successful of not
    }
}