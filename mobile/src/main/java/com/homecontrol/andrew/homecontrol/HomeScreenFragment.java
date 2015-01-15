package com.homecontrol.andrew.homecontrol;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by andrew on 12-31-14.
 */
public class HomeScreenFragment extends Fragment {
    private static final String TAG = "HomeScreenFragment";
    MobileActivity mobileActivity;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try{
            mobileActivity = (MobileActivity) activity;
        } catch (ClassCastException cce){
            throw new ClassCastException(activity.toString() + " activity must be of type MobileActivity");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View loginView = inflater.inflate(R.layout.home_screen_fragment, container, false);
        return loginView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume begin");
    }

    public void beginSwapTimer(){
        Log.d(TAG, "beginning timer");
        Timer t = new Timer();
        t.schedule(new MyTimer(), 1500);
    }

    private class MyTimer extends TimerTask{
        Handler handler = new Handler();
        @Override
        public void run() {
            handler.post(new Runnable(){
                @Override
                public void run() {
                    mobileActivity.switchToLogin();
                }
            });
        }
    }
}
