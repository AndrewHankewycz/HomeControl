package com.homecontrol.andrew.homecontrol;

import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.view.CardFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by andrew on 12/25/14.
 * Basic fragment that displays a waiting message to the user while waiting for module data from the device to load
 */
public class WaitingFragment extends CardFragment {
    private static final String TAG = "MyCard";
    private WearActivity activity;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = (WearActivity) activity;
    }

    @Override
    public View onCreateContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // inflate fragment with waiting card view
        View view = inflater.inflate(R.layout.waiting_card_fragment, container, false);
        Log.d(TAG, "onCreateView");
        return view;
    }
}
