package com.homecontrol.andrew.homecontrol;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

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
        return getJSONText(phpUrl);    // might still be null if there was an exception
    }

    @Override
    protected void onPostExecute(String result) {
        // close loading dialog
        if(pDialog.isShowing())
            pDialog.dismiss();
        // if we get a valid result from server
        if (result != null) {
            try {
                //create jsonArray and call createButtons
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

    private String getJSONText(String myUrl){
        InputStream is = null;  // input stream
        String content = null;
        //setUpCertificate();
        try{
            URL url = new URL(myUrl);
            Log.d(TAG, myUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();  // used for standard http, openConnetion() may throw IOException
            //urlConnection = (HttpsURLConnection) url.openConnection();  // used for https, openConnetion() may throw IOException
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.connect();
            Log.d(TAG, "connected");
            int response = conn.getResponseCode();
            Log.i(TAG, "The response is " + response);
            is = conn.getInputStream(); // create input stream from http connection
            content = readInput(is);    // read input stream and extract data as string
            //Log.d(TAG, content);  // trying to catch when the php script might return an error trying to access database
        } catch (MalformedURLException mue){
            Log.e(GETJSONTEXT_MESSAGE, mue.toString());
        } catch (IOException ioe){  // catch IOException of readInput
            Log.e(GETJSONTEXT_MESSAGE, ioe.toString());
        }
        finally{
            if(is != null) {
                try {
                    is.close();
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

/*
    private void setUpCertificate(){
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            InputStream caInput = new BufferedInputStream(new FileInputStream("/storage/sdcard0/documents/han.crt"));       // fill in certificate location here
            Certificate ca;
            try {
                ca = cf.generateCertificate(caInput);
                System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
            } finally {
                caInput.close();
            }

            // Create a KeyStore containing the trusted CA
            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);

            // Create a TrustManager that trusts the CA in the KeyStore
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);

            // Create an SSLContext that uses the TrustManager
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, tmf.getTrustManagers(), null);

            // Tell the URLConnection to use a SocketFactory from the SSLContext
            URL url = new URL(phpUrl + getString(R.string.requestData_ext));     // the url should already be specified in settings to be using https
            urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setSSLSocketFactory(context.getSocketFactory());
            //InputStream in = urlConnection.getInputStream();
            //copyStream(in, System.out);
        }catch(Exception e){
            Log.e(TAG, e.toString());
        }
    }
    public static void copyStream(InputStream input, OutputStream output)
            throws IOException
    {
        byte[] buffer = new byte[1024]; // Adjust if you want
        int bytesRead;
        while ((bytesRead = input.read(buffer)) != -1)
        {
            output.write(buffer, 0, bytesRead);
        }
    }
    */
}
