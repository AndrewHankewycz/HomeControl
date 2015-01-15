package com.homecontrol.andrew.homecontrol;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;

/**
 * Created by andrew on 7/24/14.
 */
public class DeleteConfirmDialog extends DialogFragment {
    private static String TAG = "Delete Confirm Dialog";
    MobileActivity activity;

    public void onAttach(Activity activity){
        super.onAttach(activity);
        try{
            this.activity = (MobileActivity) activity;
        } catch (ClassCastException e){
            throw new ClassCastException(activity.toString()
                    + " must implement AccountHandler");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        Bundle bundle = getArguments();
        final String network = bundle.getString("network");
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(activity.getString(R.string.confirm_acct_delete_1) + " \"" + network + "\" " + activity.getString(R.string.confirm_acct_delete_2))
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                activity.removeNetwork(network);
                                Log.d(TAG, "account deleted");
                            }
                        }
                )
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d(TAG, "account delete cancelled");
                            }
                        }
                );
        return builder.create();
    }
}
