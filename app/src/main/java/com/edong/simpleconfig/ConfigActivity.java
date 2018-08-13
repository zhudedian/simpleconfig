package com.edong.simpleconfig;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.edong.simpleconfig.util.ConnectUtil;
import com.edong.simpleconfig.view.ProgressIcon;

public class ConfigActivity extends AppCompatActivity {

    private ProgressIcon progressIcon;
    private Button endBt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar!=null){
            actionBar.hide();
        }
        progressIcon = (ProgressIcon)findViewById(R.id.fresh_view);
        endBt = (Button)findViewById(R.id.end_config_button);
        endBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ConnectUtil.stopConfig();
                startMainActivity();
                finish();
            }
        });
        startConfig();

    }

    private void startConfig(){
        ConnectUtil.setTreadMsgHandler(new MsgHandler());
        ConnectUtil.startConfig();
    }
    private void startMainActivity(){
        Intent intent = new Intent(ConfigActivity.this,MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed(){


    }

    class MsgHandler extends Handler{
        @Override
        public void handleMessage(Message msg){
            switch (msg.what) {
                case 5:
                    startMainActivity();
                    finish();
                    break;
                default:
                    break;
            }
        }
    }
    //    private void logSCParam(){
//        Log.e("edong","SC_SSID="+SCParam.SC_SSID+",SC_PASSWD="+SCParam.SC_PASSWD
//                +",\nSC_PIN="+SCParam.SC_PIN+",SC_BSSID="+SCParam.SC_BSSID
//                +",\nSC_PKT_TYPE="+SCParam.SC_PKT_TYPE+",SC_PKT_TYPE="+SCParam.SC_SOFTAP_MODE
//                +",\nTotalConfigTimeMs="+SCLibrary.TotalConfigTimeMs+",OldModeConfigTimeMs="+SCLibrary.OldModeConfigTimeMs
//                +",\nProfileSendRounds="+SCLibrary.ProfileSendRounds+",ProfileSendTimeIntervalMs="+SCLibrary.ProfileSendTimeIntervalMs
//                +",\nPacketSendTimeIntervalMs="+SCLibrary.PacketSendTimeIntervalMs
//                +",\nEachPacketSendCounts="+SCLibrary.EachPacketSendCounts
//                +",\nSC_HOSTIP="+SCParam.SC_HOSTIP+",SC_WIFI_Interface="+SCParam.SC_WIFI_Interface
//                +",\nSC_PHONE_MAC_ADDR="+SCParam.SC_PHONE_MAC_ADDR);
//    }
}
