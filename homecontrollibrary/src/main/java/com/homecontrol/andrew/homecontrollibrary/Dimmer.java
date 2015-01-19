package com.homecontrol.andrew.homecontrollibrary;

/**
 * Created by andrew on 8/19/14.
 */
public class Dimmer extends Module {
    private static final String TAG = "Dimmer";
    public final static String type = "dimmer";
    private int value;  // value 0 - 100
    private String state;   // for module being on/off, 0/1

    public Dimmer(String address, String n, String s, String v){
        super(address, n);
        state = s;
        value = Integer.parseInt(v);
    }

    public Dimmer(String address){  // used when creating uninitialized "modules"
        super(address, "New Module");
        value = 0;
        state = "0";
    }

    public void setValue(int v){
        if(v >= 0 && v <= 100){
            value = v;
        } else
            value = 0;
    }

    public int getValue(){
        return value;
    }

    public String getState(){
        return state;
    }

    @Override
    public void flipState(){    // for directly turning module on/off
        if(state.equals("0"))
            state = "1";
        else state = "0";
    }

    @Override
    public void update(ModifyModuleInterface activity){
        // updates name , state and value in the database
        // the last two arguments belong to the WHERE clause
        String[] values = {"name", getName(), "state", state, "value", Integer.toString(value), "addr", getAddr()};
        activity.updateModuleData(values);
    }
}
