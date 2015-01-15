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
public class AddNewModuleTask extends AsyncTask<String, Void, Void>{ // first String is what is passed in (.execute(url)), second is for progress update,
    // last if what is passed on doInBackground to onPostExecute. Nothing is returned to the calling method
    private static final String TAG = "AddNewModuleTask";
    private MobileActivity activity;  // stores a MobileActivity object so I dont have to keep calling getActivity
    private ProgressDialog pDialog;

    public AddNewModuleTask(Activity activity){
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
        String addr = strings[0];
        String type = strings[1];
        String name = strings[2];
        String value = "0";
        String state = "0";

        HttpClient httpclient = new DefaultHttpClient();

        HttpPost httppost = new HttpPost(activity.getNetworkAddress() + activity.getString(R.string.newModule_ext));
        try{
            Log.d(TAG, "addr=" + addr + " name=" + name + " type=" + type + " value=" + value + " state=" + state);
            List dataPairs = new ArrayList();
            dataPairs.add(new BasicNameValuePair("addr", addr));
            dataPairs.add(new BasicNameValuePair("name", name));
            dataPairs.add(new BasicNameValuePair("type", type));
            dataPairs.add(new BasicNameValuePair("value", value));
            dataPairs.add(new BasicNameValuePair("state", state));     // needs to be worked out to be boolean
            httppost.setEntity(new UrlEncodedFormEntity(dataPairs));
            HttpResponse response = httpclient.execute(httppost);
            Log.d(TAG, "The response is " + response);
            Log.d(type, "sent to database");
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

        Toast.makeText(activity, "New Module Added", Toast.LENGTH_SHORT).show();   // show that its complete
    }
}
