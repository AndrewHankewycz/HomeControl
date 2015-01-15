package com.homecontrol.andrew.homecontrol;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.ConfirmationActivity;
import android.support.wearable.view.CardFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by andrew on 12/25/14.
 */
public class EventCard extends CardFragment {
    private static final String TAG = "EventCard";
    private WearActivity activity;
    private String addr;
    private String name;
    private String state;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = (WearActivity) activity;
    }

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        ArrayList<String> strings = args.getStringArrayList(MyGridPagerAdapter.MODULE_DATA_KEY);
        addr = strings.get(0);
        name = strings.get(1);
        state = strings.get(2);
    }

    @Override
    public View onCreateContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.outlet_card, container, false);

        TextView cardTitle = (TextView) view.findViewById(R.id.outlet_title);
        cardTitle.setText(name);

        TextView cardText = (TextView) view.findViewById(R.id.outlet_text);
        ImageButton button = (ImageButton) view.findViewById(R.id.outlet_button);
        if(state.equals("1")){
            cardText.setText(getString(R.string.turn_off_label));
            button.setImageDrawable(getResources().getDrawable(R.drawable.light_bulb_white_off));
        }else {
            cardText.setText(getString(R.string.turn_on_label));
            button.setImageDrawable(getResources().getDrawable(R.drawable.light_bulb_white));
        }

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.flipModuleState(addr);     // flip the state value in the module
                Intent intent = new Intent(activity, ConfirmationActivity.class);
                intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE,
                        ConfirmationActivity.SUCCESS_ANIMATION);
                if (state.equals("1")) {
                    intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE, getString(R.string.turning_off_msg));
                } else {
                    intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE, getString(R.string.turning_on_msg));
                }
                startActivity(intent);
                activity.finish();      // end the activity, close the program
            }
        });
        return view;    }
}
