package com.edong.simpleconfig.util;

import android.app.Activity;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

import java.util.List;
import android.os.Handler;

import static com.edong.simpleconfig.sclib.ConfigLibrary.configLibrary;

public class ConnectUtil {

    private static String ssid = "";
    private static String password = "";



    public static void init(Activity activity){
        configLibrary.rtk_sc_init();
        configLibrary.WifiInit(activity);
    }
    public static void conncetWifi(WifiManager wifiManager){
        WifiConfiguration config = new WifiConfiguration();
        config.SSID = "\"" + ssid + "\"";
        config.preSharedKey = "\"" + password + "\"";
        int networkId = wifiManager.addNetwork(config);
        if (networkId!=-1) {
            wifiManager.updateNetwork(config);
            wifiManager.saveConfiguration();
            wifiManager.enableNetwork(networkId, true);
        }else {
            List<WifiConfiguration> configurations = configLibrary.getConfiguredNetworks();
            for (WifiConfiguration configuration:configurations){
                if (configuration.SSID.equals(ssid)){
                    wifiManager.enableNetwork(configuration.networkId,true);
                }
            }
        }
    }

    public static void setTreadMsgHandler(Handler handler){
        configLibrary.TreadMsgHandler = handler;
    }

    public static void setSsid(String s){
        ssid = s;
    }
    public static void setPassword(String pword){
        password = pword;
    }
    public static void startConfig(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                configLibrary.rtk_sc_reset();
                configLibrary.rtk_sc_set_ssid(ssid);
                configLibrary.rtk_sc_set_password(password);
                configLibrary.rtk_sc_start("","");
            }
        }).start();

    }
    public static void stopConfig(){
        configLibrary.rtk_sc_stop();
    }
}
