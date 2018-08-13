package com.edong.simpleconfig.frag;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.edong.simpleconfig.MainActivity;
import com.edong.simpleconfig.NoticeActivity;
import com.edong.simpleconfig.R;
import com.edong.simpleconfig.util.ConnectUtil;
import com.edong.simpleconfig.util.WifiUtil;
import com.edong.simpleconfig.wifi.WifiAdapter;
import com.edong.simpleconfig.wifi.WifiListPopu;

import java.util.List;

import static android.content.Context.WIFI_SERVICE;

public class EnterPasswordFrag extends Fragment {
    private boolean isTwoPane;
    private TextView wifiName;
    private EditText passWord;
    private Button nextBt;
    private WifiManager mWifiManager;

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
            WifiUtil.startScan();
            List<ScanResult> scanResults = WifiUtil.getScanResults();
            if (scanResults!=null&&scanResults.size()>0) {
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
                    WifiListPopu wifiListPopu = new WifiListPopu((AppCompatActivity) getActivity(), true);
                    wifiListPopu.show(wifiName);
                    wifiListPopu.setOnItemClickListener(new WifiListPopu.OnItemClickListener() {
                        @Override
                        public void onItemClick(ScanResult scanResult) {
                            itemClick(scanResult);
                        }
                    });
                }
            }
        });
        nextBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                        ConnectUtil.conncetWifi(mWifiManager);
                        isNeedToNoticeActivity = true;
                    }

                }
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
        startActivity(intent);
    }
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//			Log.d(TAG, "======> getAction(): " + intent.getAction());
            if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                if (isNeedToNoticeActivity&&WifiUtil.isWifiConnect(getContext())){
                    isNeedToNoticeActivity = false;
                    startNoticeActivity();
                }
            }
        }
    };

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
