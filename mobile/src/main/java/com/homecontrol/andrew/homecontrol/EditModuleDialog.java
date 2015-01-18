package com.homecontrol.andrew.homecontrol;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.homecontrol.andrew.homecontrollibrary.ModifyModuleInterface;

/**
 * Created by andrew on 7/30/14.
 */
public class EditModuleDialog extends DialogFragment {
    private static String TAG = "Edit Module Dialog";
    private ModifyModuleInterface activity;
    private String name;
    private String addr;
    private String type;
    private int index;

    // keys for attaching module data to bundle
    public static final String MODULE_ADDR = "addr";
    public static final String MODULE_NAME = "name";
    public static final String MODULE_TYPE = "type";
    public static final String MODULE_LIST_INDEX = "list_index";

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            this.activity = (ModifyModuleInterface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " activity must implement ModifyModuleInterface");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        Log.d(TAG, "onCreateDialog");
        name = getArguments().getString(MODULE_NAME);
        type = getArguments().getString(MODULE_TYPE);
        addr = getArguments().getString(MODULE_ADDR);
        index = getArguments().getInt(MODULE_LIST_INDEX);

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View nameDialogView = inflater.inflate(R.layout.edit_module_dialog, null);
        TextView typeText = (TextView) nameDialogView.findViewById(R.id.edit_mod_dialog_type);
        typeText.setText("Type : " + type);
        TextView addrText = (TextView) nameDialogView.findViewById(R.id.edit_mod_dialog_addr);
        addrText.setText("Unique Address : " + addr);
        EditText nameText = (EditText) nameDialogView.findViewById(R.id.edit_mod_dialog_name_edittext);
        nameText.setText(name);
        nameText.setSelection(name.length());
        nameText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                // for when the user presses the keyboard Done button
                boolean handled = false;
                if(i == EditorInfo.IME_ACTION_DONE){
                    submit(i, textView.getText().toString());  // validate input and proceed
                    handled = true;
                }
                return handled;
            }
        });

        builder.setView(nameDialogView)
                .setTitle("Edit Module")
                .setPositiveButton("Ok", new okClickListener())
                .setNegativeButton("Cancel", new cancelClickListener());

        // Create the AlertDialog object and return it
        return builder.create();
    }

    private void submit(int index, String name){
        if(name.equals(""))
            Toast.makeText(getActivity(), "Module must have a name", Toast.LENGTH_SHORT).show();
        else if(name.length() > 20)
            Toast.makeText(getActivity(), "Please use a shorter name", Toast.LENGTH_SHORT).show();
        else {
            activity.renameModule(index, name);   // somehow i need ot identify what module i am renaming
            getDialog().dismiss();
        }
    }

    private final class okClickListener implements DialogInterface.OnClickListener {
        public void onClick(DialogInterface dialog, int which) {
            EditText nameText = (EditText) ((AlertDialog)dialog).findViewById(R.id.edit_mod_dialog_name_edittext);
            String name = nameText.getText().toString();
            submit(index, name);   // validate input and proceed
        }
    }

    private final class cancelClickListener implements DialogInterface.OnClickListener {
        public void onClick(DialogInterface dialog, int which) {
            // close dialog
        }
    }
}
