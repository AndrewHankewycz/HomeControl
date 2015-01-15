package com.homecontrol.andrew.homecontrol;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Calendars;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by andrew on 7/9/14.
 */
public class GetCalendarTask extends AsyncTask<String, Void, Boolean> { // first String is what is passed in (.execute(url)), second is for progress update,
    // last if what is passed on doInBackground to onPostExecute. Nothing is returned to the calling method
    private static final String TAG = "DownloadJSONUninits";
    private MobileActivity activity;  // stores a MobileActivity object so I dont have to keep calling getActivity
    private ProgressDialog pDialog;

    public static final String[] EVENT_PROJECTION = new String[] {
            CalendarContract.Calendars._ID,                           // 0
            CalendarContract.Calendars.ACCOUNT_NAME,                  // 1
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,         // 2
            CalendarContract.Calendars.OWNER_ACCOUNT                  // 3
    };

    // The indices for the projection array above.
    private static final int PROJECTION_ID_INDEX = 0;
    private static final int PROJECTION_ACCOUNT_NAME_INDEX = 1;
    private static final int PROJECTION_DISPLAY_NAME_INDEX = 2;
    private static final int PROJECTION_OWNER_ACCOUNT_INDEX = 3;

    public GetCalendarTask(Activity activity){
        this.activity = (MobileActivity) activity;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    @Override
    protected Boolean doInBackground(String... s) {
        Log.d(TAG, "Querying for Calendars");
        // Run query
        Cursor cur = null;
        ContentResolver cr = activity.getContentResolver();
        Uri uri = Calendars.CONTENT_URI;
        String selection = "((" + Calendars.ACCOUNT_NAME + " = ?) AND ("
                + Calendars.ACCOUNT_TYPE + " = ?) AND ("
                + Calendars.OWNER_ACCOUNT + " = ?))";
        String[] selectionArgs = new String[] {"ahankewycz@gmail.com", "com.google",
                "ahankewycz@gmail.com"};
// Submit the query and get a Cursor object back.
        cur = cr.query(uri, EVENT_PROJECTION, selection, selectionArgs, null);

        while (cur.moveToNext()) {
            long calID = 0;
            String displayName = null;
            String accountName = null;
            String ownerName = null;

            // Get the field values
            calID = cur.getLong(PROJECTION_ID_INDEX);
            displayName = cur.getString(PROJECTION_DISPLAY_NAME_INDEX);
            accountName = cur.getString(PROJECTION_ACCOUNT_NAME_INDEX);
            ownerName = cur.getString(PROJECTION_OWNER_ACCOUNT_INDEX);
            Log.d(TAG, "callID " + calID);
            Log.d(TAG, "displayName " + displayName);
            Log.d(TAG, "accountName " + accountName);
            Log.d(TAG, "ownerName " + ownerName);
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        Toast.makeText(activity, "Done reading Calendar", Toast.LENGTH_SHORT).show();
    }
}
