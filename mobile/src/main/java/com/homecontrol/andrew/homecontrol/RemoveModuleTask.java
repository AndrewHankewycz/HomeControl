package com.homecontrol.andrew.homecontrol;

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by andrew on 7/9/14.
 */
public class RemoveModuleTask extends AsyncTask<String, Void, Void>{ // first String is what is passed in (.execute(url)), second is for progress update,
    // last if what is passed on doInBackground to onPostExecute. Nothing is returned to the calling method
    private static final String TAG = "AddNewModuleTask";
    private MobileActivity activity;  // stores a MobileActivity object so I dont have to keep calling getActivity
    private ProgressDialog pDialog;

    public RemoveModuleTask(Activity activity){
        this.activity = (MobileActivity) activity;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        // show progress dialog
        pDialog = new ProgressDialog(activity);
        pDialog.setMessage(activity.getString(R.string.wait_adding_module));
        pDialog.setCancelable(false);
        pDialog.show();
    }

    @Override
    protected Void doInBackground(String... strings) {
        // array of strings will be passed in and i will take that data as addr, name.....
        String table = strings[0];
        String column = strings[1];
        String value = strings[2];

        HttpClient httpclient = new DefaultHttpClient();

        HttpPost httppost = new HttpPost(activity.getNetworkAddress() + activity.getString(R.string.removeModule_ext));
        try{
            Log.d(TAG, "Removing from table where column=" + column + " value=" + value);
            List dataPairs = new ArrayList();
            dataPairs.add(new BasicNameValuePair("table", table));
            dataPairs.add(new BasicNameValuePair("column", column));
            dataPairs.add(new BasicNameValuePair("value", value));
            httppost.setEntity(new UrlEncodedFormEntity(dataPairs));
            HttpResponse response = httpclient.execute(httppost);
            Log.d(TAG, "The response is " + response);
            Log.d(TAG, "removed from database");
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

        Toast.makeText(activity, "Module Removed", Toast.LENGTH_SHORT).show();   // show that its complete
    }
}
