package com.homecontrol.andrew.homecontrollibrary;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class NetworkRequest {
    private static final String TAG = "NetworkRequest";

    public static String request(String url) throws IOException {
        return request(urlConnection(url));
    }

    public static String request(HttpURLConnection conn){
        InputStream is = null;
        String content = null;

        try{
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(5000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.connect();

            is = conn.getInputStream(); // create input stream from http connection
            content = readInput(is);    // read input stream and extract data as string
        } catch (MalformedURLException mue){
            Log.e(TAG, mue.toString());
        } catch (IOException ioe){  // catch IOException of readInput
            Log.e(TAG, ioe.toString());
        }
        finally{
            if(is != null) {    // if its not null because otherwise we wouldn't have created an inputstream since we have nothing to read
                try {
                    is.close();
                    Log.d(TAG, "Closing input stream");
                } catch (IOException e) {
                    Log.e(TAG, e.toString());
                }
            }

            return content; // return string read from input stream
        }
    }

    private static HttpURLConnection urlConnection(String url) throws IOException {
        return (HttpURLConnection) new URL(url).openConnection();
    }

    private static String readInput(InputStream stream) throws IOException{
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
}
