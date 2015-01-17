package com.homecontrol.andrew.homecontrol;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.Toast;

import com.homecontrol.andrew.homecontrollibrary.Dimmer;
import com.homecontrol.andrew.homecontrollibrary.Module;
import com.homecontrol.andrew.homecontrollibrary.Outlet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by andrew on 7/15/14.
 */
public class MainFragment extends Fragment implements DownloadJSONUninits.NewModuleTaskCommunication{
    private static final String TAG = "Main Fragment";
    private static final String TAG_ADDR = "addr";
    private static final String TAG_NAME = "name";
    private static final String TAG_TYPE = "type";
    private static final String TAG_VALUE = "value";
    private static final String TAG_STATE = "state";
    private ArrayList<String> networkList;
    private int selectedPos;
    JSONArray jsonArray;    // where we will store the jsonArray, others will point to this
    //private Module modules[];
    //ArrayList<Module> mods;
    ArrayList<Module> uninitMods;
    //private GestureDetectorCompat mDetect;

    MobileActivity activity;  // so I dont have to keep calling getActivity(), also if I assign during onAttach I know I have the pointer

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d(TAG, "onAttach");
        try{
            this.activity = (MobileActivity) activity;
        } catch (ClassCastException cce){
            throw new ClassCastException(activity.toString() + " activity must be of Class MobileActivity");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        setHasOptionsMenu(true);
        // initialize components that i want to save, when user will leave page
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView begin");
        View mainFragmentView = inflater.inflate(R.layout.main_fragment, container, false);

        networkList = new ArrayList(Arrays.asList(activity.getNetworkListArray()));
        selectedPos = networkList.indexOf(activity.getNetworkName());

        //mobileActivity.getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        //mDetect = new GestureDetectorCompat(mobileActivity, new MyGestureListener());

        /*
         this gesture detect will not work because the gridview is eating up the gesture
        mainFragmentView.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event){
                Log.d(TAG, "touched");
                return mDetect.onTouchEvent(event);
            }
        });
        */

        Log.d(TAG, "onCreateView finished");
        return mainFragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
        createButtons();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "on paused");
        // do things that i want to save, this is called when user leaves page
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        Spinner networkName = (Spinner) getActivity().findViewById(R.id.network_name_spinner);
        networkName.setAdapter(new ArrayAdapter<String>(getActivity(), R.layout.login_spinner_text, networkList));
        networkName.setOnItemSelectedListener(new MySpinnerActivity());
        networkName.setSelection(selectedPos);
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "onDetached");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        activity.getMenuInflater().inflate(R.menu.main_fragment_menu, menu);
        // add items to menu
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "state Saved");
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // when item is selected
        switch (item.getItemId()) {
            case R.id.add: {
                Log.d(TAG, "pressed menu add button");
                checkUninitModules();   // call check method, to start an asynctask and look for modules
                //DialogFragment dialog = new NewModuleDialog();
                //dialog.show(mobileActivity.getSupportFragmentManager(), "My NewModuleDialog");
                return true;
            }
            case R.id.remove: {
                ArrayList<Module> mods = activity.getModules();
                Log.d(TAG, "pressed menu remove button");
                if(mods != null) {  // if there are modules available
                    String[] addrArray = new String[mods.size()];    // parallel arrays for addr and name
                    String[] nameArray = new String[mods.size()];
                    for (int i = 0; i < mods.size(); i++) {
                        addrArray[i] = mods.get(i).getAddr();
                        nameArray[i] = mods.get(i).getName();
                    }
                    Bundle bundle = new Bundle();
                    bundle.putStringArray("addrs", addrArray);
                    bundle.putStringArray("names", nameArray);
                    DialogFragment dialog = new RemoveModuleDialog();
                    dialog.setArguments(bundle);
                    dialog.show(activity.getSupportFragmentManager(), "My RemoveDialog");
                } else {
                    Toast.makeText(activity, "No Modules Available", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
            case R.id.refresh: {
                activity.refresh();
                return true;
            }
            case R.id.action_settings: {
                Log.d(TAG, "pressed menu settings button");
                activity.switchToAccountSettingsFragment();
//                AccountSettings frag = new AccountSettings();
//                frag.setArguments(activity.getIntent().getExtras());
//                activity.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, frag).addToBackStack(null).commit();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    // this method should only be called upon completing the downloading of the module array
    // with the data it will then populate buttons in the main fragment
    public void createButtons(){
        final ArrayList<Module> mods = activity.getModules();
        if(mods != null) {
            Log.i(TAG, "creating " + mods.size() + " buttons");
            GridView gridView = (GridView) activity.findViewById(R.id.buttonGridView);
            ButtonAdapter adapter = new ButtonAdapter(activity, this, R.layout.row_grid, R.layout.row_grid_slider, mods);
            gridView.setAdapter(adapter);
            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                    Module m = mods.get(position);
                    m.flipState();
                    m.update(activity);
                    //createNotification();
                    createButtons(); // reloads page
                    Log.d(TAG, "clicked");
                }
            });
            gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    Log.d(TAG, "long clicked button " + position);
                    Module module = mods.get(position);
                    Bundle bundle = new Bundle();
                    bundle.putString("name", ((Module) mods.get(position)).getName());
                    bundle.putString("addr", ((Module) mods.get(position)).getAddr());
                    if (module instanceof Outlet)
                        bundle.putString("type", "Outlet");
                    else bundle.putString("type", "Unknown");
                    DialogFragment dialog = new EditModuleDialog();
                    dialog.setArguments(bundle);
                    dialog.show(activity.getSupportFragmentManager(), "My edit module dialog");
                    return true;    // tells the framework that the event has been handled and doesnt need further processing
                }
            });
        }else{
            Log.e(TAG, "mods is null");
        }
    }

    private void createNotification(){

        // standard notification, main notification seen on both devices
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(activity)
                .setSmallIcon(R.drawable.ic_launcher_bulb)
                .setContentTitle("HomeControl")
                .setContentText("This is mobile");
        //.setContentIntent(actionPendingIntent);


        int notificationId = 001;
        // Build intent for notification content
        Intent actionIntent = new Intent(activity, MobileActivity.class);
        actionIntent.putExtra("id", "9000");
        Intent viewIntent = new Intent(activity, MobileActivity.class);
        actionIntent.putExtra("id", "8000");

        // action tied to event, using the same for both!!
        PendingIntent actionPendingIntent = PendingIntent.getActivity(activity, 0, actionIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // wear notification tab 1
        NotificationCompat.Action action = new NotificationCompat.Action.Builder(R.drawable.ic_launcher_bulb,
                        "Action", actionPendingIntent).build();

        PendingIntent viewPendingIntent = PendingIntent.getActivity(activity, 0, viewIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // wear notification tab 2
        NotificationCompat.Action view = new NotificationCompat.Action.Builder(R.drawable.gear,
                "Settings", actionPendingIntent).build();

        NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender();
        wearableExtender.addAction(action);
        wearableExtender.addAction(view);

        //Notification notification = new NotificationCompat.Builder(mobileActivity)
                        //.setSmallIcon(R.drawable.ic_launcher_bulb)
                       // .setContentTitle("HomeControl")
                      //  .setContentText("This is wear")
                      //  .extend(wearableExtender)
                      //  .build();
                       //.setContentIntent(viewPendingIntent);

        notificationBuilder.extend(wearableExtender);

        // Get an instance of the NotificationManager service
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(activity);

        // Build the notification and issues it with notification manager.
        notificationManager.notify(notificationId, notificationBuilder.build());
    }

    public void checkUninitModules(){
        new DownloadJSONUninits(this, activity.getNetworkAddress() + getString(R.string.requestUninit_ext)).execute();
    }

    @Override
    public void createNewModuleDialog(String result){
        deserializeUninits(result);
        // should create a bundle to pass to dialog
        Bundle bundle = getModuleBundle(uninitMods);
        DialogFragment dialog = new NewModuleDialog();
        dialog.setArguments(bundle);
        dialog.show(activity.getSupportFragmentManager(), "My NewModuleDialog");
    }

    /*
    public Bundle getBundle(ArrayList<Module> list){
        ArrayList<String> stringList = new ArrayList<String>();

        for(Module module : list){
            stringList.add(module.addr);
            if(module instanceof Outlet){
                stringList.add("Outlet");
            } else
                stringList.add("Unknown");
        }
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("modules", stringList);
        return bundle;
    }
    */

    public Bundle getModuleBundle(ArrayList<Module> list){
        String[] type = new String[list.size()];
        String[] addr = new String[list.size()];
        Module module;

        for(int i = 0; i < list.size(); i++) {
            module = list.get(i);
            if (module instanceof Outlet) {
                type[i] = "outlet"; // set string array value, for displaying in the dialog window
            } else if(module instanceof Dimmer) {
                type[i] = "dimmer";
            } else {
                type[i] = "Unknown";
            }

            // set addr string value, for displaying in dialog window
            addr[i] = module.getAddr();
        }
        Bundle bundle = new Bundle();
        bundle.putStringArray("type", type);
        bundle.putStringArray("addr", addr);
        return bundle;
    }



    public void deserializeUninits(String result){
        //create jsonArray and call createButtons
        try{
            JSONArray uninitJsonArray = new JSONArray(result);
            uninitMods = new ArrayList<Module>();
            JSONObject jsonObject;
            String type, name;

            for (int i = 0; i < uninitJsonArray.length(); i++) {
                jsonObject = uninitJsonArray.getJSONObject(i);
                type = jsonObject.getString(TAG_TYPE);

                if(type.equals("outlet")) {
                    uninitMods.add(new Outlet(jsonObject.getString(TAG_ADDR)));
                } else if(type.equals("dimmer")){
                    uninitMods.add(new Dimmer(jsonObject.getString(TAG_ADDR)));
                } else if(type.equals("temp_outlet")) {
                    // new temp_outlet
                    uninitMods.add(new Outlet(jsonObject.getString(TAG_ADDR)));   // doing it anyway for now since i dont have a temp
                } else
                    throw new IllegalArgumentException("Invalid module type found");
            }
            Log.d(TAG, "deserialized");
        } catch (JSONException je){
            Log.e(TAG, je.toString());
        } catch (IllegalArgumentException iae){
            Log.e(TAG, iae.toString());
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



//    public void updateDimmerValue(int pos, int v){
//        if(mods.get(pos) instanceof Dimmer){
//            Dimmer d = (Dimmer) mods.get(pos);
//            d.setValue(v);
//            d.update(mobileActivity);
//        } else
//            Log.e(TAG, "trying to modify a dimmer value on a non Dimmer object");
//    }

//    public void switchDimmerOnOff(int pos){
//        if(mods.get(pos) instanceof Dimmer){
//            Dimmer d = (Dimmer) mods.get(pos);
//            d.flipState();
//            d.update(mobileActivity);
//        } else
//            Log.e(TAG, "trying to modify a dimmer state on a non Dimmer object");
//            // i probably dont need this since i cant see why this would be called if the view wasnt populated as a dimmer
//            // unless there ended up being a mixup with the mods positions and the gridview positions
//    }

    private class MyGestureListener extends GestureDetector.SimpleOnGestureListener{
        // with simpleOnGestureListener i only have to implement the events i want to
        private static final String DEBUG_TAG = "Gestures";

        @Override
        public boolean onDown(MotionEvent event) {
            Log.d(DEBUG_TAG,"onDown: " + event.toString());
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            Log.d(DEBUG_TAG,"onLongPressed: " + e.toString());
            super.onLongPress(e);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if(velocityX < 0) {
                Log.d(DEBUG_TAG,"onFling Right");
                activity.getActionBar().setSelectedNavigationItem(1);
            }
            else if(velocityX > 0) {
                Log.d(DEBUG_TAG,"onFling Left");
                activity.getActionBar().setSelectedNavigationItem(0);
            }
            return super.onFling(e1, e2, velocityX, velocityY);
        }
    }
}