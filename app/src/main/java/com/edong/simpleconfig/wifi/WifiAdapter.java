package com.edong.simpleconfig.wifi;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.edong.simpleconfig.R;
import com.edong.simpleconfig.view.WifiSignalView;

import java.util.List;

public class WifiAdapter extends ArrayAdapter<ScanResult> {

    private List<ScanResult> wifis;
    public WifiAdapter(Context context, int textViewResourceId, List<ScanResult> objects){
        super(context,textViewResourceId,objects);
        this.wifis = objects;
    }
    @Override
    public View getView(int posetion, View convertView, ViewGroup parent){
        ViewHolder viewHolder;
        final ScanResult wifi = wifis.get(posetion);
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.item_list_wifi, null);
            viewHolder.wifiName = convertView.findViewById(R.id.wifi_name);
            viewHolder.signalView = convertView.findViewById(R.id.wifi_signal_icon);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView .getTag();
        }
        int level = WifiManager.calculateSignalLevel(wifi.level, 100);
        boolean isLocked = !wifi.capabilities.equals("[ESS]");
        viewHolder.signalView.setState(level,isLocked);
        viewHolder.wifiName.setText(wifi.SSID);
        return convertView;
    }

    class ViewHolder{
        TextView wifiName;
        WifiSignalView signalView;
    }

}
