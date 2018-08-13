package com.edong.simpleconfig.wifi;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.edong.simpleconfig.R;
import com.edong.simpleconfig.frag.WifiListFrag;
import com.edong.simpleconfig.util.WifiUtil;

import java.util.List;

public class WifiListPopu {

    private AppCompatActivity context;
    private PopupWindow popupWindow;
    private RelativeLayout outsideRelative;
    private LinearLayout innerLinear;
    private ListView listView;
    private WifiAdapter adapter;
    private List<ScanResult> scanResults;
    private boolean outsideTouchable;
    private boolean cancelable = true;

    public WifiListPopu(AppCompatActivity context, boolean outsideTouchable){
        this.context = context;
        this.outsideTouchable = outsideTouchable;

        View view = getView();
        popupWindow = new PopupWindow(view,-1,-1);
    }
    private View getView(){
        View view = View.inflate(context, R.layout.popu_list_wifi, null);
        outsideRelative = (RelativeLayout)view.findViewById(R.id.outside_relative);

        outsideRelative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (outsideTouchable){
                    dismiss();
                }
            }
        });
        WifiListFrag wifiListFrag = (WifiListFrag)context.getSupportFragmentManager().findFragmentById(R.id.wifi_list_frag);
        wifiListFrag.setOnItemClickListener(new WifiListFrag.OnItemClickListener() {
            @Override
            public void onItemClick(ScanResult scanResult) {
                if (itemClickListener!=null){
                    itemClickListener.onItemClick(scanResult);
                }
                WifiListPopu.this.dismiss();
            }
        });
        return view;
    }
    public boolean isShowing(){
        return popupWindow.isShowing();
    }

    public void dismiss(){
        WifiListFrag  fragment = (WifiListFrag) context.getSupportFragmentManager().findFragmentById(R.id.wifi_list_frag);
        if(fragment != null){
            context.getSupportFragmentManager().beginTransaction().remove(fragment).commit();
        }
        if (popupWindow!=null&&popupWindow.isShowing()) {
            popupWindow.dismiss();
            popupWindow = null;
        }
    }
    public boolean isCancelable(){
        return this.cancelable;
    }
    public void show(View parent){
        popupWindow.showAtLocation(parent, Gravity.CENTER,0,0);
    }

    private OnItemClickListener itemClickListener;
    public void setOnItemClickListener(OnItemClickListener listener){
        this.itemClickListener = listener;
    }
    public interface OnItemClickListener{
        void onItemClick(ScanResult scanResult);
    }
}
