package com.homecontrol.andrew.homecontrol;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by andrew on 7/19/14.
 */
public class NewNetworkFragment extends Fragment {
    private static final String TAG = "New User Fragment";
    MobileActivity activity;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try{
            this.activity = (MobileActivity) activity;
        } catch (ClassCastException cce){
            throw new ClassCastException(activity.toString() + " activity must be of class MobileActivity");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View newUserView = inflater.inflate(R.layout.new_network, container, false);

        Button b = (Button) newUserView.findViewById(R.id.new_user_ok_button);
        b.setOnClickListener(ok);

        EditText networkIpConfirm = (EditText) newUserView.findViewById(R.id.new_ip_edit_text);
        networkIpConfirm.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {    // for when the user pressed keyboard done
                boolean handled = false;
                if(i == EditorInfo.IME_ACTION_DONE){
                    ok.onClick(textView);   // calls saveListener.onClick when user presses done from keyboard
                    handled = true;
                }
                return handled;
            }
        });
        return newUserView;
    }

    public View.OnClickListener ok = new View.OnClickListener(){
        @Override
        public void onClick(View view){

            EditText networkName = (EditText) activity.findViewById(R.id.new_network_name_edit_text);
            String network = networkName.getText().toString();
            EditText ipInput = (EditText) activity.findViewById(R.id.new_ip_edit_text);
            String ip = ipInput.getText().toString();


            try {
                if(checkNetwork(network)) {
                    ip = IPHelper.validateIP(ip);
                    Log.e(TAG, network + " " + ip);
                    activity.makeNewNetwork(network, ip);       // creates a new network
                    activity.getNewMainFragment();
                    activity.switchToMainFragment();
                    // hides keyboard
                    InputMethodManager imm = (InputMethodManager) activity.getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(ipInput.getWindowToken(), 0);
                }else{
                    Toast.makeText(activity, "Error: " + network + " already exist", Toast.LENGTH_LONG).show();
                }
            } catch (IllegalIpAddressException iiae){
                Log.e(TAG, iiae.toString());
                Toast.makeText(activity, "Error: Check IP Address", Toast.LENGTH_LONG).show();
            }
        }
    };

    private boolean checkNetwork(String network){
        boolean success = true;
        // loop over each network in the list checking if any of them match the new network name
        for(String name : activity.getNetworkListArray()){
            if(name.equals(network)){
                success = false;
            }
        }
        return success;
    }
}
