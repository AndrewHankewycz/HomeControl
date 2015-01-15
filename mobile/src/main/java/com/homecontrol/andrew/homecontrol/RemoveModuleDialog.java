package com.homecontrol.andrew.homecontrol;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

/**
 * Created by andrew on 7/21/14.
 */
public class RemoveModuleDialog extends DialogFragment {
    MobileActivity activity;
    String[] addrs;
    String[] names;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            this.activity = (MobileActivity) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement ModifyModuleInterface");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        addrs = getArguments().getStringArray("addrs");
        names = getArguments().getStringArray("names");
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Select a Module to be Removed")
                .setItems(names, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        activity.removeModule("nodes", addrs[which]);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
