package com.homecontrol.andrew.homecontrol;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.homecontrol.andrew.homecontrollibrary.GetClientNodesRunnable;
import com.homecontrol.andrew.homecontrollibrary.MyApiClientInterface;

import java.util.HashSet;

/**
 * Created by andrew on 12/25/14.
 * Class used to asynchronously get a list of connected "nodes" we can send messages to using the GoogleApiClient
 */
public class NodeAsyncTask extends AsyncTask<String, Void, Void> implements MyApiClientInterface{
    private static final String TAG = "Node Async Task";
    private WearActivity activity;
    private GoogleApiClient mGoogleApiClient;

    public NodeAsyncTask(Activity activity, GoogleApiClient client){
        this.activity =  (WearActivity) activity;
        mGoogleApiClient = client;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(String... strings) {
        Thread nodesThread = new Thread(new GetClientNodesRunnable(this, mGoogleApiClient));
        nodesThread.start();        // the GetClientNodesRunnable searches for nodes, then performs a callback on the setNodes() method of this activity
        try {
            nodesThread.join();     // wait for thread to finish, then we know the nodesList has been set
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        Log.d(TAG, "sending download modules msg to tablet");
        activity.sendMessageGetModules();     // all list are updated, call sendMessage() of the WearActivity
    }

    @Override
    public void setNodes(HashSet<String> nodes) {
        Log.d(TAG, "setNodes: nodesList has been set");
        activity.setNodesInMain(nodes);     // pass nodes to main
    }
}