package com.homecontrol.andrew.homecontrol;


import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.Toast;

import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.homecontrol.andrew.homecontrollibrary.Dimmer;
import com.homecontrol.andrew.homecontrollibrary.HANServiceObserver;
import com.homecontrol.andrew.homecontrollibrary.MobileFacadeInterface;
import com.homecontrol.andrew.homecontrollibrary.ModifyModuleInterface;
import com.homecontrol.andrew.homecontrollibrary.Module;
import com.homecontrol.andrew.homecontrollibrary.Outlet;
import com.homecontrol.andrew.homecontrollibrary.ServiceFacade;
import com.homecontrol.andrew.homecontrollibrary.UploadTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;

public class MobileActivity extends ActionBarActivity implements FragmentCommunication, ModifyModuleInterface, MessageApi.MessageListener, MobileFacadeInterface, HANServiceObserver{
    private static final String TAG = "Main Activity";
    private ArrayList<Module> mods;   // when synced, these will point to the same thing
    private GestureDetectorCompat mDetect;
    private ServiceFacade serviceFacade;

    private Fragment mainTab;  // used for storing fragments so i dont need to constantly recreate them. will be storing either MainFragment or RetryFragment
    private HomeScreenFragment homeScreenFragment;
    private MainFragment mainFragment;
    private NewNetworkFragment newNetworkFragment;
    private RetryFragment retryFragment;
    private ScheduleFragment scheduleFragment;

    private String localNetworkName;
    private String localNetworkAddress;
    private HashSet<String> localNetworkList;
    public static boolean unlocked = false;

    // savedState variable KEYS
    private static final String NETWORK_NAME_KEY = "network_name";
    private static final String NETWORK_ADDRESS_KEY = "network_address";
    private static final String NETWORK_LIST_KEY = "network_list";
    private static final String FRAGMENT_TYPE_KEY = "fragment_type";

    // keys for storing what fragment was active during before Android state changed event
    private static final int LOGIN_FRAG_KEY = 0;
    private static final int MAIN_FRAG_KEY = 1;
    private static final int SETTINGS_FRAG_KEY = 2;
    private static final int RETRY_FRAG_KEY = 3;

    // TAGS for deserialization
    private static final String TAG_ADDR = "addr";
    private static final String TAG_NAME = "name";
    private static final String TAG_TYPE = "type";
    private static final String TAG_VALUE = "value";
    private static final String TAG_STATE = "state";

    private MobileActivity activity;        //*** temporary for testing messageApi to send message to watch

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d(TAG, "message received");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "OnCreate");
        if(savedInstanceState != null){
            Log.e(TAG, "there is data in the saved state");
            localNetworkName = savedInstanceState.getString(NETWORK_NAME_KEY);
            localNetworkAddress = savedInstanceState.getString(NETWORK_ADDRESS_KEY);
            localNetworkList = new HashSet<>(Arrays.asList(savedInstanceState.getStringArray(NETWORK_LIST_KEY)));
        }
        ActionBar actionBar = getActionBar();
        mDetect = new GestureDetectorCompat(this, new MyGestureListener());
        serviceFacade = ServiceFacade.getInstance(this.getApplicationContext(), this);
//        serviceFacade.loadAppData(ServiceFacade.MOBILE_DEVICE);        // request for the service to load last used account data
        Log.d(TAG, "initializing new fragments");
        //mobileActivity = this;
        mainFragment = new MainFragment();
        homeScreenFragment = new HomeScreenFragment();

        //HANController controller = new HANController();


        //tabListener = new TabListener();
        //actionBar.addTab(actionBar.newTab().setText("Home").setTabListener(tabListener));
        //actionBar.addTab(actionBar.newTab().setText("Scheduler").setTabListener(tabListener));

        setContentView(R.layout.activity_main);

        if (findViewById(R.id.fragment_container) != null) {
            //Fragment fragment; commented out because I am setting the mainTab fragment instead
            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.

            if (savedInstanceState != null) {
                Log.e(TAG, "returning from previous state");
                switch (savedInstanceState.getInt(FRAGMENT_TYPE_KEY)) {
                    case LOGIN_FRAG_KEY:
                        switchToLogin();
                        break;
                    case MAIN_FRAG_KEY:
                        getNewMainFragment();
                        switchToMainFragment();
                        break;
                    case SETTINGS_FRAG_KEY:
                        mainTab = new AccountSettings();
                        break;
                    case RETRY_FRAG_KEY:
                        retryFragment = new RetryFragment();
                        switchToRetryFragment();
                        break;
                    default:
                        // nothing special
                        break;
                }
                return;
            }else{
                // no saved state, this must be opening a fresh activity
                serviceFacade.loadAppData(ServiceFacade.MOBILE_DEVICE);;   // look for last network used and a list of other networks
                Log.d(TAG, "setting up HomeScreen Fragment");
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragment_container, homeScreenFragment).commit();
            }

            HomeScreenFragment homeScreenFragment = new HomeScreenFragment();


//            if(readGlobalPreferences()){
//                // onCreate is called on a fresh start of the app, if there was a previous networkData login, prompt for password for that networkData
//                // by checking this unlocked variable that is false on a fresh start, we secure against the possibility of a very long timeout if the user were to
//                // reset the system clock via shutdown or other method
//                if(unlocked) {
//                    // *************** shouldnt t
//                    // this should never really happen because of the unlock = true. it could happen because the user chooses not to use a passcode, which should be retrieved during readpreferences
//                    Log.i(TAG, "account is unlocked");
//                    mainFragment = new MainFragment(); // should be creating new fragments because of the initial state, I need to add instead of replace like I do in my switch functions
//                    mainTab = mainFragment;
//                    //mainFragment = (MainFragment) fragment;
//                } else{
//                    // prompting for login on first start
//                    Log.i(TAG, "account is locked");
////                    loginFragment = new LoginFragment();
//                    mainTab = getLoginFragment();
//                }
//            }
//            else {
//                // this should be the case of a completely new user to the app
//                Log.i(TAG, "no saved preferences");
//                mainTab = new NewUserFragment();
//                // In case this mobileActivity was started with special instructions from an
//                // Intent, pass the Intent's extras to the fragment as arguments
//                mainTab.setArguments(getIntent().getExtras());
//            }
//            // Create a new Fragment to be placed in the mobileActivity layout
//            //NewUserFragment fragment = new NewUserFragment();

            // Add the fragment to the 'fragment_container' FrameLayout

            Log.d(TAG, "we set up the fragment");
        } else {
            // because of login security i dont really want this to happen
            Log.w(TAG, "we have a non-null fragment container");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "OnStart");


//        if(!networkData.hasModuleList()) {   // the module array is null, download modules
//            Log.i(TAG, "mods array is null");
//            openInterface();
//        } else {
//            // will probably want to switch to main fragment
//            switchToMainTab();
//            //createButtons();
//        }

        /*
        SharedPreferences sharedPrefs = this.getPreferences(Context.MODE_PRIVATE);
        long last = sharedPrefs.getLong("lastActivity", -1);
        long now = SystemClock.elapsedRealtime();
        Log.d(TAG, Long.toString(lastActivity));
        if(last != -1 || last >= now){
            switchToLogin();
        }
        else switchToLogin();
        */
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "OnRestart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "OnResume");
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        // these must be done here instead of in on resume because during on resume we are not guaranteed that the activity has been restored at this point
        // this is important for state changes
        Fragment previousFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        //if(!(fragment instanceof LoginFragment) && !passcode.equals("-1")) {      // old method, passcodes are mandatory now
        if(!(previousFragment instanceof LoginFragment)) {
            // if we arent already at the login screen and the user is using a passcode
            SharedPreferences sharedPrefs = this.getPreferences(Context.MODE_PRIVATE);
            long last = sharedPrefs.getLong("lastActivity", -1);
            long now = SystemClock.elapsedRealtime();
            Log.d(TAG, "time since last mobileActivity: " + (now - last));
            if ((now - last) > 300000) {
                switchToLogin();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "OnPause");
        long lastActivity = SystemClock.elapsedRealtime();
        SharedPreferences sharedPrefs = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putLong("lastActivity", lastActivity);
        editor.commit();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(NETWORK_NAME_KEY, localNetworkName);
        outState.putString(NETWORK_ADDRESS_KEY, localNetworkAddress);
        outState.putStringArray(NETWORK_LIST_KEY, localNetworkList.toArray(new String[localNetworkList.size()]));

        if(mainTab instanceof LoginFragment) {
            outState.putInt(FRAGMENT_TYPE_KEY, LOGIN_FRAG_KEY);
        }else if(mainTab instanceof MainFragment){
            outState.putInt(FRAGMENT_TYPE_KEY, MAIN_FRAG_KEY);
        }else if (mainTab instanceof AccountSettings){
            outState.putInt(FRAGMENT_TYPE_KEY, SETTINGS_FRAG_KEY);
        }else if (mainTab instanceof RetryFragment){
            outState.putInt(FRAGMENT_TYPE_KEY, RETRY_FRAG_KEY);
        }
        Log.d(TAG, "state Saved");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "OnCreateOptionsMenu");
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent mobileActivity in AndroidManifest.xml.
        int id = item.getItemId();
        if(id == R.id.quit_button){
            /*
            SharedPreferences sharedPrefs = this.getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.remove("urlAddress");
            editor.commit();
            urlAddress = null;
            Toast.makeText(this, urlAddress, Toast.LENGTH_SHORT).show();
            */
            finish();   // closes app
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        //mDetect.onTouchEvent(event);      // i dont want to use this right now since I am not using tabs anymore
        return super.onTouchEvent(event);
    }

    // checks for networkData connection, then proceeds to create a new asyncTask to download content
    private void openInterface(){
        Log.d(TAG, "openInterface");
//        ConnectivityManager connMgr = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
//        Log.d(TAG, "openInterface");
//        if (networkInfo != null && networkInfo.isConnected()) {
//            Log.i(TAG, "Network Connection is available");
//            //new DownloadJSONTask(this, networkData.getNetworkAddress() + getString(R.string.requestData_ext)).execute();
//            //new Thread(new DownloadJSONThread(this, networkData.getNetworkAddress() + getString(R.string.requestData_ext))).start();
//        } else {
//            Log.i(TAG, "NO Network Connection available");
//            Toast.makeText(this, R.string.no_network, Toast.LENGTH_LONG).show();
//            switchToRetryFragment();
//        }

        serviceFacade.getModulesString(this);
        // calls method to download module string, the facade will perform a callback to updateButtons
    }
//
//    private boolean readGlobalPreferences(){
//        // reads stored data for the app, data like the last network used and the list of networks available
//        Log.d(TAG, "reading App Preferences");
//        SharedPreferences sharedPrefs = this.getPreferences(Context.MODE_PRIVATE);
//        networkData.setLastNetworkUsed(sharedPrefs.getString("lastNetwork", null));    // "networkData" will store the last networkData logged into by the user
//        networkData.loadNetworkList((HashSet<String>) sharedPrefs.getStringSet("networkList", null));  // passes list of all users networks to networkData object
//        if(networkData.getLastNetworkUsed() != null) {
//            Log.d(TAG, "we have saved data");
//            // this is taken care of in the networkData class
////            if(networkList == null) {
////                Log.w(TAG, "networkList is null");  // networkList shouldnt be null if there are savedPreferences
////                networkList = new HashSet<String>();
////            }
//            return true;    // networkData will always store the last networkData used if there has been one
//        }
//        else {
//            Log.d(TAG, "we have no previously used networkData");
////            networkList = new HashSet<String>();    // no networkData list to load so create new list;
//            return false;
//        }
//    }

    private NewNetworkFragment getNewNetworkFragment(){
        if(newNetworkFragment == null){
            newNetworkFragment = new NewNetworkFragment();
        }
        return newNetworkFragment;
    }

    public void parseJSON(JSONArray json){
        deserialize(json);
    }

    public void deserialize(JSONArray jsonArray){
        mods = new ArrayList<Module>();
        JSONObject jsonObject;
        String type, name;

        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                type = jsonObject.getString(TAG_TYPE);

                if(type.equals("outlet")) {
                    mods.add(new Outlet(jsonObject.getString(TAG_ADDR), jsonObject.getString(TAG_NAME), jsonObject.getString(TAG_STATE)));
                } else if(type.equals("dimmer")) {
                    // new dimmer
                    mods.add((new Dimmer(jsonObject.getString(TAG_ADDR), jsonObject.getString(TAG_NAME), jsonObject.getString(TAG_STATE), jsonObject.getString(TAG_VALUE))));
                } else if(type.equals("temp_outlet")) {
                    // new temp_outlet
                    mods.add(new Outlet(jsonObject.getString(TAG_ADDR), jsonObject.getString(TAG_NAME), jsonObject.getString(TAG_STATE)));   // doing it anyway for now since i dont have a temp
                } else
                    throw new IllegalArgumentException("Invalid module type found");
            }
        } catch (JSONException je){
            Log.e(TAG, je.toString());
        } catch (IllegalArgumentException iae){
            Log.e(TAG, iae.toString());
        }
        Log.d(TAG, "deserialized");
        Log.d(TAG, "made " + mods.size() + " modules");
    }

    public void updateButtons(){
        // just in case the fragment was switched while waiting for a response from server. shouldnt happen but cant be too safe
        if(mainTab instanceof MainFragment){
            Log.d(TAG, "populating buttons");
            mainFragment.createButtons();
        }else if (mainTab instanceof RetryFragment) {
            switchToMainFragment();
            mainFragment.createButtons();
        }else{
                Log.d(TAG, "cannot populate buttons. Incorrect fragment class");
        }
    }

    private boolean okayToReplaceFragment(){
        boolean okayToReplace = true;
        Fragment previousFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if(previousFragment == null) {
            Log.e(TAG, "its null");
            okayToReplace = false;
        }
        return  okayToReplace;
    }
    public void switchToMainTab(){
        // switches to mainTab, whatever that was (retry/mainFragment) or creates a new MainFragment
        if(mainTab != null) {
            Log.d(TAG, "switching to main Tab");
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, mainTab).commit();
        } else{
            mainFragment = new MainFragment();
            mainTab = mainFragment;
            mainTab.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, mainTab).commit();
        }
    }

    public void getNewMainFragment(){
        mods = null;    // erase modules
        mainFragment = new MainFragment();
    }

    public void switchToMainFragment(){
        if(mainFragment == null){
            mainFragment = new MainFragment();
            mainTab = mainFragment;
            mainTab.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, mainTab).commit();
        }else{
            mainTab = mainFragment;
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, mainTab).commit();
        }
    }

    public void switchToLogin(){
        //Log.d(TAG, "system timeout... prompt for login");
        //Toast.makeText(this, "Your session has timed out", Toast.LENGTH_SHORT).show();
        mainTab = new LoginFragment();    // should always get a new loginFrag. this is so we never retain a passcode thats been typed in when we shouldnt
        if(okayToReplaceFragment()) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, mainTab).commit();
        }else{
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, mainTab).commit();
        }
    }

    public void switchToHomeScreenFragment(){
        if(homeScreenFragment == null){
            homeScreenFragment = new HomeScreenFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, mainTab).commit();
            homeScreenFragment.beginSwapTimer();
        }else{
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, homeScreenFragment).commit();
            homeScreenFragment.beginSwapTimer();
        }
    }

    public void switchToRetryFragment(){
        if(retryFragment == null){
            retryFragment = new RetryFragment();
            mainTab = retryFragment;
        }else {
            mainTab = retryFragment;
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, mainTab).commit();
    }

    public void switchToNewUserFragment(){
        mainTab = new NewUserFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, mainTab).addToBackStack(null).commit();
    }

    public void switchToNewNetworkFragment(){
        mainTab = getNewNetworkFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, mainTab).addToBackStack(null).commit();
    }

    @Override
    public void switchToScheduleFragment(){
        if(scheduleFragment != null) {
            Log.d(TAG, "switching to schedule fragment");
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, scheduleFragment).commit();
        } else{
            Log.d(TAG, "making new schedule fragment");
            scheduleFragment = new ScheduleFragment();
            scheduleFragment.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, scheduleFragment).commit();
        }
    }

    public void switchToAccountSettingsFragment(){
        Log.d(TAG, "making new Account Settings fragment");
        mainTab = new AccountSettings();

        getSupportFragmentManager().beginTransaction()
            .replace(R.id.fragment_container, mainTab).addToBackStack(null).commit();
    }

    public void refresh(){
        // refreshes the module data by calling open interface to redownload. The download then triggers buttons when ready
        openInterface();    // get new module data from server
    }


    ////// ********* getting network data from service ******8

    public void returnLoginResult(boolean success){
        if(success) {
            Log.i(TAG, "successful login");
            MobileActivity.unlocked = true;
            //mobileActivity.saveAppPreferences();    // on successful login, update lastUsedNetwork
            switchToMainFragment();
        } else {
            Log.i(TAG, "failed login");
            Toast.makeText(this, "Incorrect Passcode", Toast.LENGTH_SHORT).show();
        }
    }

    public ArrayList<Module> getModules(){
        Log.d(TAG, "getModules");
        if(mods == null) {   // the module array is null, download modules
            Log.i(TAG, "mods array is null");
            openInterface();    // if the module array isnt initialized, do so
        }
        return mods;    // return module list
    }

    @Override
    public void updateModuleData(String[] values){
        serviceFacade.modifyModuleData(values);
    }

    public String[] getNetworkListArray(){
        // returns a string array of networks used
        return localNetworkList.toArray(new String[localNetworkList.size()]);
    }

    public void setAccountData(String passcode){
        serviceFacade.setAccountPasscode(passcode);     // will also save app preferences by default to cut down on messages between processes
    }

//    public void addNetworkToList(String network){
//        networkData.addNetworkToList(network);
//    }

//    public void changeNetworkData(String newName, String newAddress){
//        serviceFacade.removeNetwork(networkData.getNetworkName());  // remove current network preference
//        networkData.removeNetworkFromList(networkData.getNetworkName());       // remove network from list
//        networkData.setNetworkName(newName);    // change current network name
//        networkData.setNetworkAddress(newAddress);  // change network address
//        networkData.addNetworkToList(newName);
//        serviceFacade.saveNetwork();
//        serviceFacade.saveAppData();    // save the new most recently used network
//        Toast.makeText(this, "Account Updated\nNetwork Name: " + networkData.getNetworkName()
//                + "\nNetwork IP: " + networkData.getNetworkAddress(), Toast.LENGTH_LONG).show();
//    }

    public void switchNetwork(String networkName){
        localNetworkName = networkName;
        serviceFacade.loadNetworkData(networkName, ServiceFacade.MOBILE_DEVICE);
    }

    public void removeNetwork(String networkName){
        Log.d(TAG, "removeNetwork");
        serviceFacade.removeNetwork(networkName);   // remove the specified network's preference
        removeNetworkFromLocalList(networkName); // remove network from local list

        // this is if the user deletes the account they were logged into
        if(networkName.equals(localNetworkName)) {    // only do this if the user deletes the account they are currently using
            //unlocked = false;   // this must be set false since it is a class variable, starting a new mobileActivity would
            Iterator<String> iterator = localNetworkList.iterator();
            if (iterator.hasNext()) {
                // the user has another account to switch to
                //networkData.setLastNetworkUsed(iterator.next());
                //networkData.setNetworkName(networkData.getLastNetworkUsed());  // I do this because saveAppPreferences will assign networkData to lastUsedNetwork and save
                localNetworkName = iterator.next();        // set network to next network in list
                Log.d(TAG, "current network is now " + localNetworkName);
                serviceFacade.loadNetworkData(localNetworkName, ServiceFacade.MOBILE_DEVICE);
                getNewMainFragment();   // clear anything that might be around from the old network
                switchToMainFragment();     // we will want to switch back to main fragment
            } else {
                Log.d(TAG, "there are no other accounts to use");
                // there are no other accounts
                clearAppPreferences();    // without an account I want to force the user to start from scratch
                switchToNewUserFragment();
            }
        }

    }

    public void makeNewNetwork(String newNetworkName, String newNetworkIp){
        localNetworkName = newNetworkName;
        localNetworkAddress = newNetworkIp;
        addNetworkToLocalList(newNetworkName);
        mods = null;
        serviceFacade.createNewNetwork(newNetworkName, newNetworkIp);       // this will save data by default to cut down on IPC
//        serviceFacade.saveNetwork();   // save preferences for this specific network
//        serviceFacade.saveAppData();    // after adding item to list, I should save app preferences since they wont really change, at this point in the app
        //Toast.makeText(activity, "New Account Created\nNetwork IP: " + networkData.getNetworkAddress(), Toast.LENGTH_LONG).show();
    }

    private void addNetworkToLocalList(String name){
        if(localNetworkList == null){
            localNetworkList = new HashSet<>();
        }
        localNetworkList.add(name);
    }

    private void removeNetworkFromLocalList(String name){
        if(localNetworkList != null){
            localNetworkList.remove(name);
        }
    }


    ////  ******** need to be updated to use service ****
    private void updateModuleByAddr(String... strings){
        new UploadTask(this).execute(strings);
    }

    private void addModuleByAddr(String... addr){
        new AddNewModuleTask(this).execute(addr);
    }

    private void removeModuleByAddr(String table, String addr){
        new RemoveModuleTask(this).execute(table, "addr", addr);
    }

    public void addModule(String... addr){
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if(fragment instanceof MainFragment){   // could do this with a try and catch / illegal class cast exception
            addModuleByAddr(addr);
            refresh();
        }
    }

    public void renameModule(String addr,String name){
        String[] s = {"name", name, "addr", addr};
        updateModuleByAddr(s);
    }

    public void removeModule(String table, String addr){
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if(fragment instanceof MainFragment){   // could do this with a try and catch / illegal class cast exception
            removeModuleByAddr(table, addr);
            refresh();
        }
    }

    public String getNetworkName(){
        return localNetworkName;
    }

    public String getNetworkAddress(){
        return localNetworkAddress;
    }

    public void setNetworkData(String name, String address){
        if(localNetworkName == null || localNetworkAddress == null || !localNetworkName.equals(name) || !localNetworkAddress.equals(address)){
            // the user has changed the network name or the network address update the data in the service
            serviceFacade.updateNetworkData(name, address);
        }
        localNetworkName = name;
        localNetworkAddress = address;
    }

    /// **** end methods that need updated to service





    ////////// **************** these have been partly deprecated by service ********888
//    public void saveAppPreferences(){
//        // saves data for the app, data like the last networkData the user was using....
////        Log.d(TAG, "saving App Preferences");
////        networkData.setLastNetworkUsed(networkData.getNetworkName());  // update last used networkData to current networkData
////        SharedPreferences sharedPrefs = this.getPreferences(Context.MODE_PRIVATE);
////        SharedPreferences.Editor editor = sharedPrefs.edit();
////        editor.putString("lastNetwork", networkData.getLastNetworkUsed());    // store the last networkData the user was logged into
////        editor.putStringSet("networkList", networkData.getNetworkList());
////        editor.commit();
//        serviceFacade.saveAppData();        // save app data, last network used, network list and app passcode
//    }

//    public boolean readPreferences(String network){
//        Log.d(TAG, "reading shared preferences for " + network);
//        SharedPreferences sharedPrefs = this.getSharedPreferences(network, Context.MODE_PRIVATE);
//        String savedURL = sharedPrefs.getString("urlAddress", null);
//        if(savedURL != null){
//            networkData.setNetworkAddress(savedURL);
//            networkData.setNetworkName(sharedPrefs.getString("networkName", null));
//            networkData.setPasscode(sharedPrefs.getString("passcode", "-1"));
//            return true;
//        } else
//            return false;
//    }

    public void validatePasscode(String enteredPasscode){
        // this method verifies if the user is entering the correct passcode associated with their account
        Log.d(TAG, "validating login");
        serviceFacade.validateLogin(enteredPasscode);   // send request to Service to validate login
//        SharedPreferences sharedPrefs = this.getSharedPreferences(networkData.getNetworkName(), Context.MODE_PRIVATE);
//        Map<String, ?> allEntries = sharedPrefs.getAll();
//        Log.i(TAG, "Saved passcode : " + networkData.getPasscode());
//        Log.i(TAG, "Entered passcode : " + enteredPasscode);
//        if(networkData.getPasscode().equals(enteredPasscode))
//            return true;
//        else return false;
    }



    // *** FragmentCommunication Implementation BEGIN ***

    @Override   // override method of interface FragmentCommunication
    public void syncModuleArrays(ArrayList list){
        // i might want to make this a deep copy
        mods = list;
        Log.d(TAG, "modules have been synced");
    }

    @Override
    public void newMainActivity(){
        Intent intent = new Intent(this, MobileActivity.class);
        startActivity(intent);
        finish();
    }

    // *** FragmentCommunication END ***

    // *** AccountHandler Implementation BEGIN ***

    public void clearAppPreferences(){
        localNetworkName = null;    // clear all local data
        localNetworkAddress = null;
        localNetworkList = null;
        serviceFacade.clearAppPreferences();    // send request to clear all app preferences
    }

    public void createToast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    // *** MobileFacadeInterface Implementation BEGIN ***

    @Override
    public void promptForNewUser() {
        switchToNewUserFragment();
    }

    @Override
    public void promptForLogin() {
        switchToLogin();
    }

    @Override
    public void setCurrentNetworkData(String networkName, String networkAddress){
        localNetworkName = networkName;
        localNetworkAddress = networkAddress;
    }

    @Override
    public void setNetworkList(ArrayList<String> list){
        localNetworkList = new HashSet(list);
    }

    @Override
    public void refreshCallback(){
        refresh();
    }

    // *** MobileFacadeInterface Implementation END ***


    // *** HANServiceObserver Implementation BEGIN ***

    @Override
    public void receiveJSONFromService(String moduleString) {
        JSONArray json = null;      // convert string to JSONArray
        try {
            json = new JSONArray(moduleString);
            parseJSON(json);    // pass the json array to be parsed/deserialized into modules
            updateButtons();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void notifyRequestFailed() {
        createToast("No Response From Server");
        Log.d(TAG, "no response from server");
        switchToRetryFragment();
    }

    // *** HANServiceObserver Implementation END ***

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
            if(getSupportFragmentManager().findFragmentById(R.id.fragment_container) instanceof MainFragment) {
                if (velocityX < 0) {
                    Log.d(DEBUG_TAG, "onFling Right");
                    getActionBar().setSelectedNavigationItem(1);
                } else if (velocityX > 0) {
                    Log.d(DEBUG_TAG, "onFling Left");
                    getActionBar().setSelectedNavigationItem(0);
                }
            }
            return super.onFling(e1, e2, velocityX, velocityY);
        }
    }

    private class TabListener implements   ActionBar.TabListener{
        @Override
        public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
            // this method gets called each time a fragment is created b/c the tab is selected in the fragment oncreateview
            // if i would just test what fragment was selected, i would get an infinite loop. so i must reject the call if i already have that fragment in view

            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

            if(tab.getPosition() == 0 && !(fragment instanceof MainFragment)){
                switchToMainTab();
            }else if(tab.getPosition() == 1 && !(fragment instanceof ScheduleFragment)) {
                switchToScheduleFragment();
            }
        }

        @Override
        public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        }
        @Override
        public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        }
    };
}
