package com.homecontrol.andrew.homecontrol;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.ConfirmationActivity;
import android.support.wearable.view.CardFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by andrew on 12/25/14.
 */
public class MyCard extends CardFragment {
    private static final String TAG = "MyCard";
    private WearActivity activity;
    private String addr;
    private String title;
    private String text;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = (WearActivity) activity;
    }

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        ArrayList<String> strings = args.getStringArrayList("args");
        addr = strings.get(0);
        title = strings.get(1);
        text = strings.get(2);
        Log.d(TAG, "unpacked bundle for " + title);
    }

    @Override
    public View onCreateContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.my_custom_card, container, false);
        TextView cardTitle = (TextView) view.findViewById(R.id.title);
        cardTitle.setText(title);
        TextView cardText = (TextView) view.findViewById(R.id.text);
        cardText.setText(text);

        Button button = (Button) view.findViewById(R.id.card_button);
        button.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Intent intent = new Intent(activity, ConfirmationActivity.class);
                intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE,
                        ConfirmationActivity.SUCCESS_ANIMATION);
                intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE, "Event happening");
                startActivity(intent);
                return false;
            }
        });

        return view;
    }
}
