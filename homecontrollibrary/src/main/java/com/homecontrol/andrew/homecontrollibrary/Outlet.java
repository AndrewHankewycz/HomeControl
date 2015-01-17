package com.homecontrol.andrew.homecontrollibrary;

/**
 * Created by andrew on 7/9/14.
 *
 *
 *
 * I was trying to make the modules update the json array so that when i load buttons from the array they match the changes made
 */
public class Outlet extends Module {
    private final String TAG = "Outlet";
    public final static String TYPE = "outlet";
    private String state;

    public Outlet(String address, String n, String status){
        super(address, n);
        state = status;
    }

    public Outlet(String address){  // used for creating uninitialized "modules"
        super(address, "New Module");
        state = "0";
    }

    public String getState(){
        return state;
    }

    @Override
    public void flipState(){
        if(state.equals("0"))
            state = "1";
        else state = "0";
    }

    @Override
    public void update(ModifyModuleInterface activity){
        // updates name and state field in database, the only fields that will change with an outlet
        String[] values = {"name", getName(), "state", state, getAddr()};
        activity.updateModuleData(values);
    }
}
