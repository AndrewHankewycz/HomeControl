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

/**
 * Created by andrew on 7/30/14.
 */
public class NameModuleDialog extends DialogFragment {
    private static String TAG = "Name Module Dialog";
    MobileActivity activity;
    String addr;
    String type;

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
        super.onCreateDialog(savedInstanceState);
        Log.d(TAG, "onCreateDialog");
        type = getArguments().getString("type");
        addr = getArguments().getString("addr");

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View nameDialogView = inflater.inflate(R.layout.name_module_dialog, null);
        TextView typeText = (TextView) nameDialogView.findViewById(R.id.new_mod_dialog_type);
        typeText.setText("Type : " + type);
        TextView addrText = (TextView) nameDialogView.findViewById(R.id.new_mod_dialog_addr);
        addrText.setText("Unique Address : " + addr);
        EditText name = (EditText) nameDialogView.findViewById(R.id.new_mod_dialog_name);
        name.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                boolean handled = false;
                if(i == EditorInfo.IME_ACTION_DONE){
                    submit(textView.getText().toString());  // validate input and proceed
                    handled = true;
                }
                return handled;
            }
        });

        builder.setView(nameDialogView)
                .setTitle("Give this Module a Name")
                .setPositiveButton("Ok", new okClickListener())
                .setNegativeButton("Cancel", new cancelClickListener());


        // Create the AlertDialog object and return it
        return builder.create();
    }

    private void submit(String name){
        if(name.equals(""))
            Toast.makeText(getActivity(), "Module must have a name", Toast.LENGTH_SHORT).show();
        else if(name.length() > 20)
            Toast.makeText(getActivity(), "Please use a shorter name", Toast.LENGTH_SHORT).show();
        else {
            activity.addModule(addr, type, name);
            activity.removeModule("uninit", addr); // both of theses methods call refresh()
            getDialog().dismiss();
        }
    }

    private final class okClickListener implements DialogInterface.OnClickListener {
        public void onClick(DialogInterface dialog, int which) {
            EditText nameText = (EditText) ((AlertDialog)dialog).findViewById(R.id.new_mod_dialog_name);
            String name = nameText.getText().toString();
            submit(name);   // validate input and proceed
        }
    }

    private final class cancelClickListener implements DialogInterface.OnClickListener {
        public void onClick(DialogInterface dialog, int which) {
            // close dialog
        }
    }
}
