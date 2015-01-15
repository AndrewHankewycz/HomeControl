package com.homecontrol.andrew.homecontrollibrary;

/**
 * Created by andrew on 12/27/14.
 */
public interface HANServiceObserver {
    public static final int GET_MODULES_REPLY = 1;  // callback message for returning module data
    public static final int GET_NETWORK_REPLY = 2;  // callback message for returning network list
}
