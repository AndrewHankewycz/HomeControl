package com.homecontrol.andrew.homecontrollibrary;

/**
 * Created by andrew on 7/9/14.
 */
public abstract class Module {
    private String addr;
    private String name;

    public Module(String address, String name){
        addr = address;
        this.name = name;
    }

    public String getAddr(){
        return addr;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public abstract void flipState();

    public abstract void update(ModifyModuleInterface activity);
}
