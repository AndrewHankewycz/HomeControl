package com.homecontrol.andrew.homecontrol;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.support.wearable.view.FragmentGridPagerAdapter;
import android.util.Log;

import com.homecontrol.andrew.homecontrollibrary.Module;
import com.homecontrol.andrew.homecontrollibrary.Outlet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by andrew on 12/8/14.
 */
public class MyGridPagerAdapter extends FragmentGridPagerAdapter {
    private static final String TAG = "MyGridPagerAdapter";
    private Context mContext;
    private List<Row> mRows;
    private ArrayList<Module> mods;

    public static final String MODULE_DATA_KEY = "module_values";

    public MyGridPagerAdapter(Context ctx, FragmentManager fm, ArrayList<Module> modules) {
        super(fm);
        mContext = ctx;
        mods = modules;
        createPage();
    }

    private void createPage(){
        Log.d(TAG, "creating page");
        mRows = new ArrayList<MyGridPagerAdapter.Row>();        // clear rows if there was any old data
        Module tempMod = null;

        for(int i = 0; i < mods.size(); i++){
            tempMod = mods.get(i);      // get reference to the current module
            // at this time, only producing cards for Outlet modules
            if(tempMod instanceof Outlet) {
                EventCard card = new EventCard();
                ArrayList<String> strings = new ArrayList();
                strings.add(tempMod.getAddr());     // add address to ArrayList of data
                strings.add(tempMod.getName());
                strings.add(((Outlet) tempMod).getState());     // add string value of state, "1" or "0"
                Bundle bundle = new Bundle();
                bundle.putStringArrayList(MODULE_DATA_KEY, strings);     // attach ArrayList of module data to a bundle
                card.setArguments(bundle);          // attach bundle to card
                mRows.add(new Row(card));
            }else{
                Log.d(TAG, "skipped creating Non-Outlet card");
            }
        }
    }

    @Override
    public Fragment getFragment(int row, int col) {
        Log.d(TAG, "getting fragment row: " + row + " col: " + col);
        Row adapterRow = mRows.get(row);
        return adapterRow.getColumn(col);
    }

    // A convenient container for a row of fragments
    private class Row {
        final List<Fragment> columns = new ArrayList<Fragment>();

        public Row(Fragment... fragments) {
            for (Fragment f : fragments) {
                add(f);
            }
        }

        public void add(Fragment f) {
            columns.add(f);
        }

        Fragment getColumn(int i) {
            return columns.get(i);
        }

        public int getColumnCount() {
            return columns.size();
        }
    }

    @Override
    public int getRowCount() {
            return mRows.size();
    }

    @Override
    public int getColumnCount(int i) {
        int count;
        if(i < mRows.size()) {
            count = mRows.get(i).getColumnCount();
        }else{
            count = 0;
        }
        return count;
    }
}