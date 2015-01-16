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
        final String response = getJSONText(phpUrl);
        handler.post(new Runnable() {
            public void run() {
                service.returnJSONDownload(response);      // return data to service
            }
        });
    }

    private String getJSONText(String myUrl){
        Log.d(TAG, "attemping to download stuff");
        InputStream is = null;  // input stream
        String content = null;
        try{
            URL url = new URL(myUrl);
            Log.d(TAG, myUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();  // used for http, openConnetion() may throw IOException
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
            Log.e(TAG, mue.toString());
        } catch (IOException ioe){  // catch IOException of readInput
            Log.e(TAG, ioe.toString());
        }
        finally{
            if(is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    Log.e(TAG, e.toString());
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
    // Set up to use trusted SSL certificate loaded to the device
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
}
*/
}
