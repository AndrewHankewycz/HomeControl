package com.homecontrol.andrew.homecontrollibrary;

import android.os.AsyncTask;
import android.util.Log;

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
public class UploadTaskNoProgressDialog extends AsyncTask<String, Void, Void> {
    private static final String TAG = "Upload Task No Progress";
    private String phpUrl;

    public UploadTaskNoProgressDialog(String phpScript){
        phpUrl = phpScript;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
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
        HttpClient httpclient = new DefaultHttpClient();
        HttpParams connParams = httpclient.getParams();
        HttpConnectionParams.setConnectionTimeout(connParams, 5000);    // connection timeout, 5 seconds
        HttpConnectionParams.setSoTimeout(connParams, 5000);    // socket timeout, 5 seconds
        HttpPost httppost = new HttpPost(phpUrl);

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
    }
}