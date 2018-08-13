package com.edong.simpleconfig;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class NoticeActivity extends AppCompatActivity {

    private Button nextBt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar!=null){
            actionBar.hide();
        }
        nextBt = (Button)findViewById(R.id.next_button);
        nextBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startConfigActivity();
            }
        });
    }

    private void startConfigActivity(){
        Intent intent = new Intent(NoticeActivity.this,ConfigActivity.class);
        startActivity(intent);
        finish();
    }
    @Override
    public void onBackPressed(){
        Intent intent = new Intent(NoticeActivity.this,MainActivity.class);
        startActivity(intent);

    }
}
