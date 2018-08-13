package com.edong.simpleconfig.wifi;


import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;


import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Created by Eric on 2017/9/8.
 */

public class WifiSort implements Comparator<ScanResult> {

    private Collator collator = Collator.getInstance(Locale.CHINA);

    public static void sort(List<ScanResult> list){
        Collections.sort(list,new WifiSort());
    }

    @Override
    public int compare(ScanResult wifi1 , ScanResult wifi2){
        int level1 = WifiManager.calculateSignalLevel(wifi1.level, 100);
        int level2 = WifiManager.calculateSignalLevel(wifi2.level, 100);
        if ( level1>level2){
            return -1;
        }else if (level1<level2){
            return 1;
        }else {
            int value2 = collator.compare(wifi1.SSID,wifi2.SSID);
            return value2;
        }
    }
}
