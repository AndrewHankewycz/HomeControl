package com.homecontrol.andrew.homecontrol;

import java.util.ArrayList;

/**
 * Created by andrew on 7/23/14.
 */
public interface FragmentCommunication {
    public void syncModuleArrays(ArrayList list);
    //public void refresh();
    //public void switchToMainTab();
    public void switchToNewUserFragment();
    public void switchToScheduleFragment();
    //public void switchToRetryFragment();
    public void newMainActivity();
}
