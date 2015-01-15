package com.homecontrol.andrew.homecontrol;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.homecontrol.andrew.homecontrollibrary.Dimmer;
import com.homecontrol.andrew.homecontrollibrary.Module;
import com.homecontrol.andrew.homecontrollibrary.Outlet;

import java.util.ArrayList;


public class ButtonAdapter<Item> extends ArrayAdapter<Item> {
    private String TAG = "ButtonAdapter Class";
    Context context;
    DimmerCallBackInterface dCallBack;
    int outletLayout;
    int dimmerLayout;
    Bitmap on;
    Bitmap off;
    ArrayList<Item> data = new ArrayList<Item>();

    public ButtonAdapter(Context context, Fragment fragment, int layoutResourceId, int layoutResourceId2, ArrayList<Item> data) {
        super(context, layoutResourceId, data);
//        try {
//            dCallBack = (DimmerCallBackInterface) fragment;
//        } catch (ClassCastException e) {
//            throw new ClassCastException(context.toString()
//                    + " must implement DimmerCallBackInterface");
//        }
        outletLayout = layoutResourceId;
        dimmerLayout = layoutResourceId2;
        this.context = context;
        this.data = data;
        on = BitmapFactory.decodeResource(context.getResources(), R.drawable.outlet_on);
        off = BitmapFactory.decodeResource(context.getResources(), R.drawable.outlet_off);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        RecordHolder holder = null;
        Module m = (Module) data.get(position);

        if (row == null) {
            // inflate layout and instantiate recordholder
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            if(m instanceof Outlet) {
                row = inflater.inflate(outletLayout, parent, false);
                holder = new RecordHolder();
                holder.txtTitle = (TextView) row.findViewById(R.id.item_text);
                holder.imageItem = (ImageView) row.findViewById(R.id.item_image);
                holder.type = "outlet";
                row.setTag(holder);
            } else if (m instanceof Dimmer){
                row = inflater.inflate(dimmerLayout, parent, false);
                holder = new RecordHolder();
                holder.txtTitle = (TextView) row.findViewById(R.id.item_text_slider);
                holder.bar = (SeekBar) row.findViewById(R.id.value_slider);
                holder.onOffSwitch = (Switch) row.findViewById(R.id.on_off_switch);
                holder.type = "dimmer";
                row.setTag(holder);
            }
        } else {
            // this item has already been inflated, get its recordholder
            holder = (RecordHolder) row.getTag();
        }

        // during scrolling it seems to take the position and the view time to catch up with each other, to make sure I am not trying to populate
        // an dimmer as an outlet or vice versa I keep a String of what type the records are and compare them with the module. This issue only happens
        // during the initial scrolling and always seems to catch up with the correct values of position and grid
        if(m instanceof Outlet && holder.type.equals("outlet")) {
            Outlet o = (Outlet) m;
            holder.txtTitle.setText(o.getName());
            if (o.getState().equals("1")) {
                holder.imageItem.setImageBitmap(on);
            } else holder.imageItem.setImageBitmap(off);
        } else if(m instanceof Dimmer && holder.type.equals("dimmer")){
            Dimmer d = (Dimmer) m;
            holder.txtTitle.setText(d.getName());
            holder.bar.setOnSeekBarChangeListener(new MySeekBarListener(position));    // pass position in gridview to listener, so the listener is later able to call a method of the dimmer class through the mods ArrayList
            holder.bar.setProgress(d.getValue());
            if(d.getState().equals("1"))
                holder.onOffSwitch.setChecked(true);
            else holder.onOffSwitch.setChecked(false);
            holder.onOffSwitch.setOnClickListener(new SwitchOnClick(position));
        } else {
            Log.d(TAG, "non matching types in gridview");
            Log.d(TAG, "grid position: " + position + " type : " + holder.type);
        }

        return row;
    }

    private class RecordHolder {
        TextView txtTitle = null;
        ImageView imageItem = null;
        SeekBar bar = null;
        Switch onOffSwitch = null;
        String type = null;
    }

    public interface DimmerCallBackInterface{
        public void updateDimmerValue(int pos, int v);
        public void switchDimmerOnOff(int pos);
    }

    private class SwitchOnClick implements View.OnClickListener{
        int pos;

        public SwitchOnClick(int position){
            pos = position;
        }

        @Override
        public void onClick(View v) {
            //dCallBack.switchDimmerOnOff(pos);

        }
    }

    private class MySeekBarListener implements SeekBar.OnSeekBarChangeListener{
        int position;   // keeps a value of its position, its position in the grid corresponds to its position in the "mods" arrayList

        MySeekBarListener(int pos){
            position = pos;
        }
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            int pos = Math.round(progress / 10) * 10;   // cast to int to round to nearest integer
            seekBar.setProgress(pos);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            //dCallBack.updateDimmerValue(position, seekBar.getProgress());
        }
    }
}

