package com.homecontrol.andrew.homecontrol;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by andrew on 8/5/14.
 */
public class ScheduleFragment extends Fragment {
    private static final String TAG = "Schedule Fragment";
    MobileActivity activity;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try{
            this.activity = (MobileActivity) activity;
        } catch (ClassCastException cce){
            throw new ClassCastException(activity.toString() + " mobileActivity must implement AccountHandler");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView begin");
        View scheduleFragmentView = inflater.inflate(R.layout.schedule_fragment, container, false);

        Log.d(TAG, "onCreateView finished");
        return scheduleFragmentView;
    }
}
