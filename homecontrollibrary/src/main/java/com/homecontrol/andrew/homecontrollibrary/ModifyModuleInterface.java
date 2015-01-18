package com.homecontrol.andrew.homecontrollibrary;

/**
 * Created by andrew on 7/23/14.
 */
public interface ModifyModuleInterface {
    //public void addModule(String... addr);
    //public void removeModule(String table, String addr);
    public void updateModuleData(String[] values);
    public void renameModule(int listIndex, String newName);
}
