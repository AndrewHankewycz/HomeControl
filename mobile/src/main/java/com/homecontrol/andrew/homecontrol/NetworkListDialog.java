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
public class NetworkListDialog extends DialogFragment {
    private static String TAG = "Network List Dialog";
    MobileActivity activity;
    String[] networks;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = (MobileActivity) activity;
    }



    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        networks = getArguments().getStringArray("networkList");

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Available Networks")
                .setItems(networks, new onClickListener())
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }

    private final class onClickListener implements DialogInterface.OnClickListener {
        public void onClick(DialogInterface dialog, int which) {
            Bundle bundle = new Bundle();
            bundle.putString("network", networks[which]);
            DialogFragment confirmDialog = new DeleteConfirmDialog();
            confirmDialog.setArguments(bundle);
            confirmDialog.show(activity.getSupportFragmentManager(), "My Delete Confirm Dialog");
        }
    }
}
