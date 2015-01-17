package com.homecontrol.andrew.homecontrol;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by andrew on 7/17/14.
 */
public class AccountSettings extends Fragment {
    private static final String TAG = "Account Settings Fragment";
    MobileActivity activity;
    boolean passcodeVisible = false;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try{
            this.activity = (MobileActivity) activity;
        } catch (ClassCastException cce){
            throw new ClassCastException(activity.toString() + " must be of type MobileActivity");
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View accountSettingsView = inflater.inflate(R.layout.account_settings, container, false);

        EditText networkEditText = (EditText) accountSettingsView.findViewById(R.id.settings_network_name_edit_text);
        networkEditText.setText(activity.getNetworkName());
        EditText addressEditText = (EditText) accountSettingsView.findViewById(R.id.settings_ip_edit_text);
        addressEditText.setText(activity.getNetworkAddress());

        Button saveChanges = (Button) accountSettingsView.findViewById(R.id.settings_ok_button);
        saveChanges.setOnClickListener(saveListener);
        Button addNetwork = (Button) accountSettingsView.findViewById(R.id.settings_add_account_button);
        addNetwork.setOnClickListener(addAccountListener);
        Button deleteNetwork = (Button) accountSettingsView.findViewById(R.id.settings_delete_account_button);
        deleteNetwork.setOnClickListener(deleteAccountListener);
        Button restoreButton = (Button) accountSettingsView.findViewById(R.id.settings_restore_app_button);
        restoreButton.setOnClickListener((restoreAppListener));

        Button dropdown = (Button) accountSettingsView.findViewById(R.id.dropdown);
        dropdown.setBackgroundResource(R.drawable.expander_down);
        dropdown.setOnClickListener(down);

        EditText passcodeConfirm = (EditText) accountSettingsView.findViewById(R.id.settings_confirm_passcode);
        passcodeConfirm.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {    // for when the user pressed keyboard done
                boolean handled = false;
                if(i == EditorInfo.IME_ACTION_DONE){
                    saveListener.onClick(textView);   // calls saveListener.onClick when user presses done from keyboard
                    handled = true;
                }
                return handled;
            }
        });

        return accountSettingsView;
    }

    @Override
    public void onResume() {
        super.onResume();
        EditText networkName = (EditText) activity.findViewById(R.id.settings_network_name_edit_text);
        networkName.setText(activity.getNetworkName());
        EditText ipInput = (EditText) activity.findViewById(R.id.settings_ip_edit_text);
        ipInput.setText(activity.getNetworkAddress());
    }

    public View.OnClickListener saveListener = new View.OnClickListener(){
        @Override
        public void onClick(View view){
            String passcode;
            String passcodeConfirm;

            EditText networkName = (EditText) activity.findViewById(R.id.settings_network_name_edit_text);
            String network = networkName.getText().toString();
            EditText ipInput = (EditText) activity.findViewById(R.id.settings_ip_edit_text);
            String ip = ipInput.getText().toString();
            EditText newPasscode = (EditText) activity.findViewById(R.id.settings_passcode);
            EditText confirmPasscode = (EditText) activity.findViewById(R.id.settings_confirm_passcode);

            if(newPasscode.getText().toString().equals("") && confirmPasscode.getText().toString().equals("")){  // when both passcodes are blank, dont change passcode
                Log.d(TAG, "not changing passcode");
                try {
                    ip = checkIP(ip);
                    if(!activity.getNetworkName().equals(network) || !activity.getNetworkAddress().equals(ip)) {
                        // if either the network name or ip has changed
                        activity.setNetworkData(network, ip);      // set the network name and address, if either has changed the activity will take care of updates
                        activity.switchToMainFragment();
                        if(!activity.getNetworkAddress().equals(ip)) {
                            // only refresh if the ip has changed
                            activity.refresh();     // if the network address changed we should reload the page
                        }
                    }
                    // hides keyboard
                    InputMethodManager imm = (InputMethodManager) activity.getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(confirmPasscode.getWindowToken(), 0);
                } catch (IllegalIpAddressException iiae){
                    Log.e(TAG, iiae.toString());
                    Toast.makeText(activity, "Error: Check IP Address", Toast.LENGTH_LONG).show();
                }
            } else if(!newPasscode.getText().toString().equals("") && !confirmPasscode.getText().toString().equals("")) {  // if both passcodes are not blank, check and make changes
                Log.d(TAG, "changing passcode");
                passcode = newPasscode.getText().toString();
                passcodeConfirm = confirmPasscode.getText().toString();
                try {
                    ip = IPHelper.validateIP(ip);
                    checkPasscode(passcode, passcodeConfirm);   // check if passwords match first
                    activity.setAccountData(passcode);
                    if(!activity.getNetworkName().equals(network) || !activity.getNetworkAddress().equals(ip)) {
                        // if either the network name or ip has changed
                        activity.setNetworkData(network, ip);      // set the network name and address, if either has changed the activity will take care of updates
                        if(!activity.getNetworkAddress().equals(ip)) {
                            // only refresh if the ip has changed
                            activity.refresh();     // if the network address changed we should reload the page
                        }
                    }
                    activity.switchToMainFragment();
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
            } else{ // if one of the fields is left blank
                Toast.makeText(activity, "Error: Blank Passcode Field", Toast.LENGTH_LONG).show();
            }
        }
    };

    public View.OnClickListener addAccountListener = new View.OnClickListener(){
        @Override
        public void onClick(View view){
            activity.switchToNewNetworkFragment();
        }
    };

    public View.OnClickListener deleteAccountListener = new View.OnClickListener(){
        @Override
        public void onClick(View view){
            Bundle bundle = new Bundle();
            bundle.putStringArray("networkList", activity.getNetworkListArray());
            DialogFragment dialog = new NetworkListDialog();
            dialog.setArguments(bundle);
            dialog.show(activity.getSupportFragmentManager(), "My Network List Dialog");
        }
    };

    public View.OnClickListener restoreAppListener = new View.OnClickListener(){
        @Override
        public void onClick(View view){
            Log.d(TAG, "restores app button pressed");
            DialogFragment dialog = new RestoreConfirmDialog2();
            dialog.show(activity.getSupportFragmentManager(), "My Restore App Dialog");
        }
    };

    public View.OnClickListener down = new View.OnClickListener(){
        @Override
        public void onClick(View view){
            if(!passcodeVisible) {
                passcodeVisible = true;
                LinearLayout passcodeLayout = (LinearLayout) activity.findViewById(R.id.passcode_layout);
                passcodeLayout.setVisibility(View.VISIBLE);
                Button arrow = (Button) activity.findViewById(R.id.dropdown);
                arrow.setBackgroundResource(R.drawable.expander_right);
            }else {
                passcodeVisible = false;
                LinearLayout passcodeLayout = (LinearLayout) activity.findViewById(R.id.passcode_layout);
                passcodeLayout.setVisibility(View.GONE);
                Button arrow = (Button) activity.findViewById(R.id.dropdown);
                arrow.setBackgroundResource(R.drawable.expander_down);
            }
        }
    };

    private void checkPasscode(String pc1, String pc2) throws InvalidPasscodeException{
        if(!pc1.equals(pc2))
            throw new InvalidPasscodeException("the passcodes do not match");
        if(pc1.matches("\\[0-9]+") && pc1.length() >= 4)
            throw new InvalidPasscodeException("that is an invalid passcode");
    }

    private class InvalidPasscodeException extends Exception {
        public InvalidPasscodeException(String s){
            super(s);
        }
    }

    private class RestoreConfirmDialog2 extends DialogFragment {
        private final String TAG = "Restore Confirm Dialog";
        MobileActivity activity;

        public void onAttach(Activity activity){
            super.onAttach(activity);
            try{
                this.activity = (MobileActivity) activity;
            } catch (ClassCastException e){
                throw new ClassCastException(activity.toString()
                        + " must be of type MobileActivity");
            }
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setMessage(activity.getString(R.string.restore_app_confirm_msg))
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Log.d(TAG, "restoring app");
                                    activity.clearAppPreferences();
                                    activity.switchToNewUserFragment();
                                }
                            }
                    )
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Log.d(TAG, "restore app cancelled");
                                }
                            }
                    );
            return builder.create();
        }
    }
}
