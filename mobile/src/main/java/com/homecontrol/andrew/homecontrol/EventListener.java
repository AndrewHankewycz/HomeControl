package com.homecontrol.andrew.homecontrol;

import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

/**
 * Created by andrew on 12/28/14.
 */
public class EventListener extends WearableListenerService {
    private static final String TAG = "EventListener";

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.e(TAG, "something");
        //showToast(messageEvent.getPath());
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
