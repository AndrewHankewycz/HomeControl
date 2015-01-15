package com.homecontrol.andrew.homecontrollibrary;

import android.app.Activity;

/**
 * Created by andrew on 7/9/14.
 *
 *
 *
 * I was trying to make the modules update the json array so that when i load buttons from the array they match the changes made
 */
public class Outlet extends Module {
    public String TAG = "Outlet";
    public final static String type = "outlet";
    private String state;

    public Outlet(String address, String n, String status){
        addr = address;
        name = n;
        state = status;
    }

    public Outlet(String address){  // used for creating uninitialized "modules"
        addr = address;
        state = "0";
    }

    @Override
    public void flipState(){
        if(state.equals("0"))
            state = "1";
        else state = "0";
    }

    public String getState(){
        return state;
    }

    @Override
    public void update(ModifyModuleInterface activity){
        // updates name and state field in database, the only fields that will change with an outlet
        String[] values = {"name", name, "state", state, addr};
        activity.updateModuleData(values);
    }
}
