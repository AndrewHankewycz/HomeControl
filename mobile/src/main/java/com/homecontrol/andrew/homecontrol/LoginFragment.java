package com.homecontrol.andrew.homecontrol;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by andrew on 7/17/14.
 */
public class LoginFragment extends Fragment {
    private static final String TAG = "Login Fragment";
    MobileActivity mobileActivity;
    private ArrayList<String> networkList;
    private int selectedPos;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try{
            mobileActivity = (MobileActivity) activity;
        } catch (ClassCastException cce){
            throw new ClassCastException(activity.toString() + " mobileActivity must be of type MobileActivity");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View loginView = inflater.inflate(R.layout.login_fragment, container, false);

//        networkList = new ArrayList<String>(Arrays.asList(mobileActivity.getNetworkListArray()));
//        selectedPos = networkList.indexOf(mobileActivity.getLastNetworkUsed());

        Button login = (Button) loginView.findViewById(R.id.login_button);
        login.setOnClickListener(loginOnClick);
        EditText passcodeField = (EditText) loginView.findViewById(R.id.login_passcode);
        passcodeField.setGravity(Gravity.CENTER_HORIZONTAL);
        passcodeField.requestFocus();
        passcodeField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {    // for when the user pressed keyboard done
                boolean handled = false;
                if(i == EditorInfo.IME_ACTION_DONE){
                    loginOnClick.onClick(textView);
                    handled = true;
                }
                return handled;
            }
        });
        return loginView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        getActivity().getMenuInflater().inflate(R.menu.login_menu, menu);
        // add items to menu
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // when item is selected
        switch (item.getItemId()) {
            case R.id.add_network: {
                Log.i(TAG, "selected menu add network");
                mobileActivity.switchToNewUserFragment();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume begin");
//        Spinner networkName = (Spinner) getActivity().findViewById(R.id.login_network_name);
//        networkName.setAdapter(new ArrayAdapter<String>(getActivity(), R.layout.login_spinner_text, networkList));
//        networkName.setOnItemSelectedListener(new MySpinnerActivity());
//        networkName.setSelection(selectedPos);
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
        hideKeyboard();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    private void checkPasscode(){
        String enteredPasscode;

        EditText enteredPasscodeText = (EditText) mobileActivity.findViewById(R.id.login_passcode);
        if(!enteredPasscodeText.getText().toString().equals("")) {
            enteredPasscode = enteredPasscodeText.getText().toString();
            mobileActivity.validatePasscode(enteredPasscode);    // calls method from Activity to check passcode
        } else {
            Toast.makeText(getActivity(), "Incorrect Passcode", Toast.LENGTH_SHORT).show();
        }
    }

    public void hideKeyboard(){
        // hides keyboard
        EditText enteredPasscodeText = (EditText) mobileActivity.findViewById(R.id.login_passcode);
        InputMethodManager imm = (InputMethodManager) mobileActivity.getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(enteredPasscodeText.getWindowToken(), 0);
    }

    public View.OnClickListener loginOnClick = new View.OnClickListener(){
        @Override
        public void onClick(View view){
//            if(mobileActivity.readPreferences(networkList.get(selectedPos))) {    // read preferences to get the password for the specified network
//                checkPasscode();
//            } else{
//                Toast.makeText(getActivity(), "That Network does not exist", Toast.LENGTH_SHORT).show();
//            }
            checkPasscode();    // does some basic passcode validation, then passes the request off to MobileActivity
        }
    };

//    private class MySpinnerActivity extends Activity implements AdapterView.OnItemSelectedListener {
//
//        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
//            selectedPos = pos;
//        }
//
//        public void onNothingSelected(AdapterView<?> parent) {
//            // Another interface callback
//        }
//    }
}
