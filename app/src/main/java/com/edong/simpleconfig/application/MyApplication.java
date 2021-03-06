package com.edong.simpleconfig.application;

import android.app.Application;
import android.content.Context;
import skin.support.SkinCompatManager;
import skin.support.design.app.SkinMaterialViewInflater;

public class MyApplication extends Application {

    private static Context context;

    @Override
    public void onCreate(){
        super.onCreate();
        context = getApplicationContext();
        SkinCompatManager.withoutActivity(this)                         // 基础控件换肤初始化
                .addInflater(new SkinMaterialViewInflater())            // material design 控件换肤初始化[可选]
                .setSkinStatusBarColorEnable(true)                     // 关闭状态栏换肤，默认打开[可选]
                .setSkinWindowBackgroundEnable(true)                   // 关闭windowBackground换肤，默认打开[可选]
                .loadSkin();

    }
    public static Context getContext(){
        return context;
    }
}
