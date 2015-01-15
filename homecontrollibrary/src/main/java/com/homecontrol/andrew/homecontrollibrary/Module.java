package com.homecontrol.andrew.homecontrollibrary;

import android.app.Activity;

/**
 * Created by andrew on 7/9/14.
 */
public abstract class Module {
    public String addr;
    public String name;

    public String getAddr(){
        return addr;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    };

    public abstract void flipState();

    public abstract void update(ModifyModuleInterface activity);
}
