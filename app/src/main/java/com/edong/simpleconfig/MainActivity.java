package com.edong.simpleconfig;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.edong.simpleconfig.frag.EnterPasswordFrag;
import com.edong.simpleconfig.util.ConnectUtil;
import com.edong.simpleconfig.util.WifiUtil;
import com.edong.simpleconfig.wifi.WifiListPopu;


import java.util.List;

import skin.support.SkinCompatManager;
import skin.support.content.res.SkinCompatResources;
import skin.support.widget.SkinCompatSupportable;


public class MainActivity extends AppCompatActivity implements SkinCompatSupportable {


    private EnterPasswordFrag enterPasswordFrag;

    private InputMethodManager inputMethodManager;

    private Toolbar toolbar;
    private FrameLayout frameLayout;
    private boolean changeLayout = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ConnectUtil.init(this);
        setContentView(R.layout.activity_main);

        initToolbar();
        frameLayout = (FrameLayout)findViewById(R.id.wifi_list_layout);
        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
        }else {
//            Toast.makeText(MainActivity.this,"权限申请成功",Toast.LENGTH_LONG).show();
            init();
        }
//        ImageView imageView = (ImageView)findViewById(R.id.image_view);
//        Drawable drawable = imageView.getDrawable();
//        if (drawable instanceof Animatable){
//            ((Animatable)drawable).start();
//        }

    }







    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults){
        switch (requestCode){
            case 1:
                if (grantResults.length>0&&grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(MainActivity.this,"权限申请成功",Toast.LENGTH_LONG).show();
                    init();
                }else {
                    Toast.makeText(MainActivity.this,"权限被拒绝",Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
             default:
                break;
        }
    }
    private  void init(){
        enterPasswordFrag = (EnterPasswordFrag)getSupportFragmentManager().findFragmentById(R.id.enter_password_frag);
        enterPasswordFrag.init();
    }

    private void initToolbar(){
        toolbar = (Toolbar)findViewById(R.id.toolbar) ;
        toolbar.setTitle(R.string.app_name);
        toolbar.inflateMenu(R.menu.menu_main);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.light_mode:
                        SkinCompatManager.getInstance().loadSkin("light", SkinCompatManager.SKIN_LOADER_STRATEGY_BUILD_IN);
                        return true;
                    case R.id.night_mode:
                        SkinCompatManager.getInstance().restoreDefaultTheme();
                        return true;
                    default:
                        return true;
                }

            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev){
        int action = ev.getAction();
        float y = ev.getY();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (frameLayout!=null){
                    int x = frameLayout.getRight();
//                    Log.e("edong","x="+x+",ev.getX()="+ev.getX());
                    if (Math.abs(x-ev.getX())<20){
                        changeLayout = true;
                    }else {
                        changeLayout = false;
                    }
                }
                EditText editText = enterPasswordFrag.getEditText();
                int[] location1 = new int[2] ;
                editText.getLocationInWindow(location1); //获取在当前窗口内的绝对坐标
                if (ev.getX()<location1[0]||ev.getX()>location1[0]+editText.getWidth()
                        ||ev.getY()<location1[1]||ev.getY()>location1[1]+editText.getHeight()) {
                    inputMethodManager.hideSoftInputFromWindow(enterPasswordFrag.getEditText().getWindowToken(), 0);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (frameLayout!=null&&changeLayout){
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams((int) ev.getX(),
                            frameLayout.getBottom());
                    frameLayout.setLayoutParams(params);
                }
                break;
            case MotionEvent.ACTION_UP:

                break;

        }

        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
//        Log.e("edong","keyCode="+keyCode);

        return super.onKeyDown(keyCode, event);
    }
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
//        Log.e("edong","dispatchKeyEvent,keyCode="+event.getKeyCode()+",event.getAction()="+event.getAction());
        if (event.getKeyCode()==KeyEvent.KEYCODE_ENTER&&event.getAction()==KeyEvent.ACTION_DOWN){
            if (enterPasswordFrag!=null){
                enterPasswordFrag.onEnterPressed();
                return true;
            }
        }

        return super.dispatchKeyEvent(event);
    }
        @Override
    public void onResume() {


        super.onResume();
    }
    private long lastBackPressedTime = 0;
    @Override
    public void onBackPressed(){

        if (enterPasswordFrag==null||!enterPasswordFrag.onBackPressed()){
            if (System.currentTimeMillis()-lastBackPressedTime>2000){
                Toast.makeText(MainActivity.this,"再按一次退出应用",Toast.LENGTH_LONG).show();
                lastBackPressedTime = System.currentTimeMillis();
            }else {
                finish();
            }
        }

    }
    
    @Override
    public void applySkin() {

        if (toolbar!=null){
            Drawable drawable = SkinCompatResources.getDrawable(MainActivity.this,R.drawable.ic_menu_overflow_material);
            toolbar.setOverflowIcon(drawable);
        }
    }

}
