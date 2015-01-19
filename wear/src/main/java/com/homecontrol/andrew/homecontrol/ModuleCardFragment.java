package com.homecontrol.andrew.homecontrol;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.wearable.view.GridViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.homecontrol.andrew.homecontrollibrary.Module;

import java.util.ArrayList;

/**
 * Created by andrew on 12/25/14.
 */
public class ModuleCardFragment extends Fragment {
    private static final String TAG = "MyCard";
    private WearActivity activity;
    private ArrayList<Module> mods;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = (WearActivity) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // inflate fragment with pager fragment
        Log.d(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.wear_module_card_fragment, container, false);
        MyGridPagerAdapter adapter = new MyGridPagerAdapter(activity, getFragmentManager(), activity.getModuleList());
        final GridViewPager pager = (GridViewPager) view.findViewById(R.id.module_pager);
        pager.setAdapter(adapter);
        return view;
    }
}
