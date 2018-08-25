package com.edong.simpleconfig;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

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
                Toast.makeText(ConfigActivity.this,"正在结束配网……",Toast.LENGTH_SHORT).show();
//                startMainActivity();
//                finish();
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
        if (Build.VERSION.SDK_INT<21){
            startActivity(intent);
        }else {
            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(ConfigActivity.this, endBt, "button").toBundle());
        }
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

}
