package com.homecontrol.andrew.homecontrollibrary;

import android.net.Network;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by andrew on 12/22/14.
 */
public class NetworkData {
    private static final String TAG = "NetworkData";
//    private static NetworkData networkData;
    private String networkAddress;
    private String networkName;
    private String passcode;
    private HashSet<String> networkList;
    private String lastNetworkUsed;
    private ArrayList<Module> mods;

//    private NetworkData(){
//        // Singleton default constructor
//    }
//
//    public static NetworkData getInstance(){
//        if(networkData == null){
//            networkData = new NetworkData();
//        }
//        return networkData;
//    }

    public void setNetworkAddress(String addresss){
        networkAddress = addresss;
    }

    public String getNetworkAddress(){
        return networkAddress;
    }

    public ArrayList<Module> getModules(){
        return mods;
    }

    public void setNetworkName(String name){
        networkName = name;
    }

    public String getNetworkName(){
        return networkName;
    }

    public void setLastNetworkUsed(String network){
        lastNetworkUsed = network;
    }

    public String getLastNetworkUsed(){
        return lastNetworkUsed;
    }

    public void setPasscode(String code){
        passcode = code;
    }

    public String getPasscode(){
        return passcode;
    }

    public boolean hasModuleList(){
        boolean hasModules = false;
        if(mods != null){
            hasModules = true;
        }
        return hasModules;
    }

    public void loadNetworkList(HashSet<String> list){
        if(list == null){
            networkList = new HashSet<String>();    // if there were no prefereneces to read, make a new list
        }else {
            networkList = list;     // set network list to what was read
        }
    }

    public HashSet<String> getNetworkList(){
        return networkList;
    }

    public void addNetworkToList(String network){
        // just checking if its null
        if(networkList != null) {
            try {
                networkList.add(network);
                Log.d(TAG, network + " added to networkList");
            } catch (ClassCastException cce) {
                throw new ClassCastException(" String must be passed to addNetworkToList");
            }
        }
    }

    public void removeNetworkFromList(String network){
        // remove network with the given name
        networkList.remove(network);
    }

    public int getNetworkListSize(){
        return networkList.size();
    }


    public String[] getNetworkListArray(){
//        if(networkList == null)
//            Log.e(TAG, "networkList is null");
        // convert network
        String[] array = new String[networkList.size()];
        networkList.toArray(array);
        return array;
    }


}
