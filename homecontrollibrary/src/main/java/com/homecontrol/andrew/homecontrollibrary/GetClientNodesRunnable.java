package com.homecontrol.andrew.homecontrollibrary;

import android.app.ActionBar;
import android.app.Activity;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.HashSet;
import java.util.List;

/**
 * Created by andrew on 12/28/14.
 * Runnable gets a list of GoogleApiClient nodes. If the api returns at least one node, the thread calls back to the calling activity, to pass the nodes HashSet
 */
public class GetClientNodesRunnable implements Runnable {
    private static final String TAG = "GetClientNodesThread";
    private final MyApiClientInterface callingActivity;
    private final GoogleApiClient mGoogleApiClient;

    public GetClientNodesRunnable(MyApiClientInterface activity, GoogleApiClient client){
        callingActivity = activity;
        mGoogleApiClient = client;
        Log.d(TAG, "has been created");
    }
    @Override
    public void run() {
        HashSet<String> nodesSet = new HashSet<String>();        // hashset for storing available node strings

        NodeApi.GetConnectedNodesResult nodes =
                Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();       // use ClientApi to find available connected nodes
        NodeApi.GetLocalNodeResult selfNode = Wearable.NodeApi.getLocalNode(mGoogleApiClient).await();
        Log.d(TAG, "self node: " + selfNode.getNode().getId());
        if(nodes != null) {
            // if there were any connected nodes, collect them into the hashset
            List<Node> n = nodes.getNodes();
            if(n != null && n.size() > 0) {
                for (Node node : nodes.getNodes()) {
                    // iterate throught all nodes, adding their id's to the hashset
                    nodesSet.add(node.getId());
                }
                callingActivity.setNodes(nodesSet);     // after the set has been collected, send it back to calling activity with the setNodes() callback
            }else{
                Log.d(TAG, "no nodes were returned");
            }
        }else{
            Log.e(TAG, "Api request returned null");
        }
    }
}
