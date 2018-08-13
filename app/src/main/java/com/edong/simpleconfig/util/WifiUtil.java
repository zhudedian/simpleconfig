package com.edong.simpleconfig.util;

import android.app.Service;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;

import com.edong.simpleconfig.wifi.WifiSort;

import java.util.ArrayList;
import java.util.List;

import static com.edong.simpleconfig.sclib.ConfigLibrary.configLibrary;


public class WifiUtil {

    public static void wifiOpen(){
        configLibrary.WifiOpen();
    }
    public static void startScan(){
        configLibrary.WifiStartScan();
    }
    public static List<ScanResult> getScanResults(){
        List<ScanResult> list = new ArrayList<>();
        List<ScanResult> scanResults = configLibrary.getScanResults();
        for (ScanResult scanResult:scanResults){
            if (scanResult.SSID!=null&&!scanResult.SSID.equals("")){
                list.add(scanResult);
            }
        }
        WifiSort.sort(list);
        return list;
    }
    public static int getWifiStatus(){
        return configLibrary.WifiStatus();
    }

    public static boolean isWifiConnect(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context
                .getSystemService(Service.CONNECTIVITY_SERVICE);
        NetworkInfo wifiInfo = manager
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiInfo!=null) {
            return wifiInfo.isConnected() && wifiInfo.isAvailable();
        }
        return false;
    }
}
