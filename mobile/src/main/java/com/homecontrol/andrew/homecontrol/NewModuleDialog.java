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
public class NewModuleDialog extends DialogFragment {
    private static final String TAG = "New Module Dialog";
    String[] type;
    String[] addr;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        type = getArguments().getStringArray("type");
        addr = getArguments().getStringArray("addr");
        String[] displayedNames = new String[type.length];
        for(int i = 0; i < type.length; i++) {
            String name = "";
            if(type[i].equals("outlet")) {     // have to use if because of lowercase outlet in database
                name = "Outlet";
            } else if(type[i].equals("dimmer")) {
                name = "Dimmer";
            } else name = "Unknown";

            displayedNames[i] = generateDisplayName(name, addr[i]);
        }

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Select a Module")
                .setItems(displayedNames, new onClickListener())
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }

    private String generateDisplayName(String type, String address) {
        return "Type : " + type + "\t\t Unique Address : " + address;
    }

    private final class onClickListener implements DialogInterface.OnClickListener {
        public void onClick(DialogInterface dialog, int which) {
            Bundle bundle = new Bundle();
            bundle.putString("type", type[which]);
            bundle.putString("addr", addr[which]);
            DialogFragment nameDialog = new NameModuleDialog();
            nameDialog.setArguments(bundle);
            nameDialog.show(getActivity().getSupportFragmentManager(), "My Name Module Dialog");
        }
    }
}
