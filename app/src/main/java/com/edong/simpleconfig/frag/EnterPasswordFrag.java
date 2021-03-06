package com.edong.simpleconfig.frag;


import android.app.ActivityOptions;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.edong.simpleconfig.NoticeActivity;
import com.edong.simpleconfig.R;
import com.edong.simpleconfig.util.ConnectUtil;
import com.edong.simpleconfig.util.WifiUtil;
import com.edong.simpleconfig.wifi.WifiListPopu;


import java.util.List;


import static android.content.Context.WIFI_SERVICE;

public class EnterPasswordFrag extends Fragment {
    private boolean isTwoPane;
    private TextView wifiName;
    private EditText passWord;
    private Button nextBt;
    private WifiManager mWifiManager;
    private WifiListPopu wifiListPopu;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.frag_password_enter,container,false);
        wifiName = (TextView)view.findViewById(R.id.wifi_name);
        passWord = (EditText)view.findViewById(R.id.wifi_password);
        nextBt = (Button)view.findViewById(R.id.next_button);
        mWifiManager = (WifiManager)getActivity().getApplicationContext().getSystemService(WIFI_SERVICE);
        passWord.addTextChangedListener(textWatcher);
        register();
        return view;
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        if (getActivity().findViewById(R.id.wifi_list_layout)!=null){
            isTwoPane = true;
//            wifiName.setBackground(null);
        }else {
            isTwoPane = false;
        }

        setListener();
    }
    public  void init(){

        if(WifiUtil.getWifiStatus() != WifiManager.WIFI_STATE_ENABLED) {
            WifiUtil.wifiOpen();
            WifiUtil.startScan();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            init();
                        }
                    });

                }
            }).start();
        } else {
            if (wifiName.getText().toString().equals(getResources().getString(R.string.default_wifi_name))) {
                List<ScanResult> scanResults = WifiUtil.getScanResults();
                if (scanResults != null && scanResults.size() > 0) {
                    String lastSeleteWifi = getLastSelectWifi(scanResults);
                    if (!lastSeleteWifi.equals("")) {
                        wifiName.setText(lastSeleteWifi);
                        String password = getPassword(lastSeleteWifi);
                        if (password != null && !password.equals("")) {
                            passWord.setText(password);
                        }
                    } else {
                        wifiName.setText(scanResults.get(0).SSID);
                    }
                }
            }
        }

    }

    public View getNextButton(){
        return nextBt;
    }

    public EditText getEditText() {
        return passWord;
    }
    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if(s.toString().length()>=8){
                nextBt.setEnabled(true);
            }else{
                nextBt.setEnabled(false);
            }
        }
    };
    private void setListener(){



        wifiName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(WifiUtil.getWifiStatus() != WifiManager.WIFI_STATE_ENABLED) {
                    WifiUtil.wifiOpen();
                    WifiUtil.startScan();
                    return;
                }
                if (!isTwoPane) {
                    if (wifiListPopu==null) {
                        wifiListPopu = new WifiListPopu((AppCompatActivity) getActivity(), true);
                        wifiListPopu.setOnItemClickListener(new WifiListPopu.OnItemClickListener() {
                            @Override
                            public void onItemClick(ScanResult scanResult) {
                                itemClick(scanResult);
                            }
                        });

                    }
                    wifiListPopu.show(wifiName);
                }
            }
        });
        nextBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextPressed();
            }
        });
        if (isTwoPane){
            WifiListFrag wifiListFrag = (WifiListFrag)getFragmentManager().findFragmentById(R.id.wifi_list_frag);
            wifiListFrag.setOnItemClickListener(new WifiListFrag.OnItemClickListener() {
                @Override
                public void onItemClick(ScanResult scanResult) {
                    itemClick(scanResult);
                }
            });
            wifiListFrag.setDataChangeListener(new WifiListFrag.DataChangeListener() {
                @Override
                public void onChanged(List<ScanResult> list) {
                    init();
                }
            });
        }
    }

    private void nextPressed(){
        String ssid = wifiName.getText().toString();
        String pword = passWord.getText().toString();
        if (ssid.equals("")||pword.equals("")){
            Toast.makeText(getContext(),"请输入wifi密码",Toast.LENGTH_SHORT).show();
        }else {
            ConnectUtil.setSsid(ssid);
            ConnectUtil.setPassword(pword);
            setPassword(ssid,pword);
            if (WifiUtil.isWifiConnect(getContext())){
                startNoticeActivity();
            }else {
//                        Log.e("edong","conncetWifi");
                Toast.makeText(getActivity(),"正在连接WiFi请稍后……",Toast.LENGTH_SHORT).show();
                ConnectUtil.conncetWifi(mWifiManager);
                isNeedToNoticeActivity = true;
            }

        }
    }
    private void itemClick(ScanResult scanResult){
        wifiName.setText(scanResult.SSID);
        setLastSelectWifi(scanResult.SSID);
        String password = getPassword(scanResult.SSID);
        if (password != null ) {
            passWord.setText(password);
        }
    }
    private boolean isNeedToNoticeActivity = false;//true表示已经点击了下一步，等待连接网络跳转到NoticeActivity
    private void register(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        getActivity().registerReceiver(mReceiver, filter);
    }
    @Override
    public void onDestroyView() {

        super.onDestroyView();
        unregister();
    }
    private void unregister(){
        try {
            getActivity().unregisterReceiver(mReceiver);
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    private void startNoticeActivity(){
        Intent intent = new Intent(getContext(),NoticeActivity.class);
        if (Build.VERSION.SDK_INT<21){
            startActivity(intent);
        }else {
            getActivity().startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(getActivity(), nextBt, "button").toBundle());
        }
    }
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//			Log.d("edong", "======> getAction(): " + intent.getAction());
            if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
//                Log.e("edong","wifi,info.isConnected()="+info.isConnected());
                if (isNeedToNoticeActivity&&info.isConnected()){
                    isNeedToNoticeActivity = false;
                    startNoticeActivity();
                }
            }else if (intent.getAction().equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)) {

                int linkWifiResult = intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, 123);
//                Log.e("edong","wifi密码错误广播"+linkWifiResult);
                if (linkWifiResult == WifiManager.ERROR_AUTHENTICATING) {
                    Toast.makeText(getActivity(),"密码错误",Toast.LENGTH_LONG).show();
                }
            }
        }
    };
    public void onEnterPressed(){
        if (passWord.getText().toString().length()>=8) {
            nextBt.performClick();
        }
    }

    public boolean onBackPressed(){
        if (wifiListPopu!=null&&wifiListPopu.isShowing()){
            wifiListPopu.dismiss();
            return true;
        }
        return false;
    }
    public String getPassword(String ssid){
        SharedPreferences preferences = getActivity().getSharedPreferences("edong_config_password", Context.MODE_PRIVATE);
        return preferences.getString(ssid,"");
    }
    public void setPassword(String ssid,String password){
        SharedPreferences preferences = getActivity().getSharedPreferences("edong_config_password", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(ssid,password);
        editor.apply();
    }
    public void setLastSelectWifi(String ssid){
        SharedPreferences preferences = getActivity().getSharedPreferences("edong_config_preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("last_select_wifi",ssid);
        editor.apply();
    }
    public String getLastSelectWifi(List<ScanResult> scanResults){
        SharedPreferences preferences = getActivity().getSharedPreferences("edong_config_preferences", Context.MODE_PRIVATE);
        String ssid = preferences.getString("last_select_wifi","");
        if (!ssid.equals("")){
            for (ScanResult scanResult:scanResults){
                if (scanResult.SSID.equals(ssid)){
                    return ssid;
                }
            }
        }
        return "";
    }
}
