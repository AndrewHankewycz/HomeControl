package com.homecontrol.andrew.homecontrol;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by andrew on 7/17/14.
 */
public class RetryFragment extends Fragment {
    private static final String TAG = "Retry Fragment";
    MobileActivity activity;
    private ArrayList<String> networkList;
    private int selectedPos;

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
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View loginView = inflater.inflate(R.layout.retry_fragment, container, false);

        networkList = new ArrayList(Arrays.asList(activity.getNetworkListArray()));
        selectedPos = networkList.indexOf(activity.getNetworkName());

        Button retry = (Button) loginView.findViewById(R.id.retry_button);
        retry.setOnClickListener(retryOnClick);
        Log.d(TAG, "creating view");

        return loginView;
    }

    @Override
    public void onResume() {
        super.onResume();
        Spinner networkName = (Spinner) getActivity().findViewById(R.id.network_name_spinner);
        networkName.setAdapter(new ArrayAdapter<String>(getActivity(), R.layout.login_spinner_text, networkList));
        networkName.setOnItemSelectedListener(new MySpinnerActivity());
        networkName.setSelection(selectedPos);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        activity.getMenuInflater().inflate(R.menu.retry_fragment_menu, menu);
        // add items to menu
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // when item is selected
        switch (item.getItemId()) {
            case R.id.action_settings: {
                AccountSettings frag = new AccountSettings();
                frag.setArguments(activity.getIntent().getExtras());
                activity.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, frag).addToBackStack(null).commit();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class MySpinnerActivity extends Activity implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            Log.e(TAG, "item selected pos: " + pos);
            String network = activity.getNetworkListArray()[pos];
            if(activity.getNetworkName() != null) {
                if (!activity.getNetworkName().equals(network)) {
                    Log.d(TAG, "switching to " + network + " network");
                    activity.switchNetwork(network);
                    activity.getNewMainFragment();      // to clear any buttons that may be from the old network
                    activity.switchToMainFragment();        // since were in retry fragment, we will want to switch back to MainFragment
                }
            }
        }

        public void onNothingSelected(AdapterView<?> parent) {
            // Another interface callback
        }
    }

    public View.OnClickListener retryOnClick = new View.OnClickListener(){
        @Override
        public void onClick(View view){
            activity.refresh();
        }
    };
}
