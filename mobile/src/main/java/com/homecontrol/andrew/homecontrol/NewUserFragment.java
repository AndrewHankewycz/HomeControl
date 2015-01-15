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
public class NewUserFragment extends Fragment {
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
        View newUserView = inflater.inflate(R.layout.new_user, container, false);

        Button b = (Button) newUserView.findViewById(R.id.new_user_ok_button);
        b.setOnClickListener(ok);

        EditText passcodeConfirm = (EditText) newUserView.findViewById(R.id.new_confirm_passcode);
        passcodeConfirm.setOnEditorActionListener(new TextView.OnEditorActionListener() {
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
            String passcode;
            String passcodeConfirm;

            EditText networkName = (EditText) activity.findViewById(R.id.new_network_name_edit_text);
            String network = networkName.getText().toString();
            EditText ipInput = (EditText) activity.findViewById(R.id.new_ip_edit_text);
            String ip = ipInput.getText().toString();
            EditText newPasscode = (EditText) activity.findViewById(R.id.new_passcode);
            EditText confirmPasscode = (EditText) activity.findViewById(R.id.new_confirm_passcode);


            if(!newPasscode.getText().toString().equals("") && !confirmPasscode.getText().toString().equals("")) {
                // if user does enter a passcode
                passcode = newPasscode.getText().toString();
                passcodeConfirm = confirmPasscode.getText().toString();
                try {
                    ip = checkIP(ip);
                    checkPasscode(passcode, passcodeConfirm);
                    Log.e(TAG, network + " " + ip);
                    activity.setAccountData(passcode);
                    activity.makeNewNetwork(network, ip);       // creates a new network
                    activity.switchToMainFragment();
                    activity.refresh();
                    // hides keyboard
                    InputMethodManager imm = (InputMethodManager) activity.getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(confirmPasscode.getWindowToken(), 0);
                } catch (InvalidPasscodeException ipe){
                    Log.e(TAG, ipe.toString());
                    Toast.makeText(activity, "Error: Passcodes do not match", Toast.LENGTH_LONG).show();
                } catch (IllegalIpAddressException iiae){
                    Log.e(TAG, iiae.toString());
                    Toast.makeText(activity, "Error: Check IP Address", Toast.LENGTH_LONG).show();
                }
            } else
                Toast.makeText(activity, "Password is required", Toast.LENGTH_LONG).show();
            /*else {
                // if user does not want to enter a passcoce
                try {
                    accountHandler.setNetworkName(network);
                    ip = checkIP(ip);
                    accountHandler.setIpAddress(ip);
                    accountHandler.setPasscode(-1);
                    accountHandler.saveNetworkPreferences();
                    accountHandler.addNetworkToList(network);   // adds new network to list of users entworks
                    accountHandler.saveAppPreferences();    // after adding item to list, I should save app preferences since they wont really change, at this point in the app
                    fragComm.refresh();
                    // hides keyboard
                    InputMethodManager imm = (InputMethodManager) activity.getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(confirmPasscode.getWindowToken(), 0);

                    Toast.makeText(activity, "New Account Created\nNetwork IP: " + MobileActivity.urlAddress, Toast.LENGTH_LONG).show();
                } catch (IllegalIpAddressException iiae){
                    Log.e(TAG, iiae.toString());
                    Toast.makeText(activity, "Error: Check IP Address", Toast.LENGTH_LONG).show();
                }
            }*/
        }
    };

    private void checkPasscode(String pc1, String pc2) throws InvalidPasscodeException{
        if(!pc1.equals(pc2)) {
            throw new InvalidPasscodeException("the passcodes do not match");
        }
        if(pc1.matches("\\[0-9]+") && pc1.length() >= 4) {
            String s = pc1 + " " + pc2;
            Log.d(TAG, s);
            throw new InvalidPasscodeException("that is an invalid passcode");
        }
    }

    private String checkIP(String ip) throws IllegalIpAddressException{
        String finalIp = null;
        if(ip.matches("http://" + "\\d{1,3}" + '.' + "\\d{1,3}" + '.' + "\\d{1,3}" + '.' + "\\d{1,3}")) // submitted http://111.111.111.111
            finalIp = ip;
        else if(ip.matches("http://" + "\\d{1,3}" + '.' + "\\d{1,3}" + '.' + "\\d{1,3}" + '.' + "\\d{1,3}" + ':' + "\\d{1,4}")) // submitted http://111.111.111.111:8080
            finalIp = ip;
        else if(ip.matches("\\d{1,3}" + '.' + "\\d{1,3}" + '.' + "\\d{1,3}" + '.' + "\\d{1,3}"))    /// submitted 111.111.111.111
            finalIp = "http://" + ip;
        else if(ip.matches("\\d{1,3}" + '.' + "\\d{1,3}" + '.' + "\\d{1,3}" + '.' + "\\d{1,3}" + ':' + "\\d{1,4}")) // submitted http://111.111.111.111:8080
            finalIp = "http://" + ip;
        else
            throw new IllegalIpAddressException("Illegal IP Address found");
        return finalIp;
    }

    private class IllegalIpAddressException extends Exception{
        public IllegalIpAddressException(String s){
            super(s);
        }
    }

    private class InvalidPasscodeException extends Exception {
        public InvalidPasscodeException(String s){
            super(s);
        }
    }
}
