package com.homecontrol.andrew.homecontrollibrary;

import java.util.ArrayList;

/**
 * Created by andrew on 12/31/14.
 */
public interface MobileFacadeInterface {
    public void returnLoginResult(boolean success);
    public void promptForNewUser();
    public void promptForLogin();
    public void setCurrentNetworkData(String networkName, String networkUrl);
    public void setNetworkList(ArrayList<String> networkList);
    public void refreshCallback();
}
