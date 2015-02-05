package com.davisECS.virtualfrontview;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * ArrayAdapter made specifically for the ChoosePeerDialog. Used to just show the device names in
 * a simple_list_item_1 view.
 */
public class WifiP2pDeviceArrayAdapter extends ArrayAdapter<WifiP2pDevice> {
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        WifiP2pDevice device = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        }

        TextView deviceText = (TextView) convertView.findViewById(android.R.id.text1);
        deviceText.setText(device.deviceName + " - " + device.deviceAddress);
        return convertView;
    }

    public WifiP2pDeviceArrayAdapter(Context context, List objects) {
        super(context, android.R.layout.simple_list_item_1,  objects);
    }
}
