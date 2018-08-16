package com.edong.simpleconfig.frag;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.edong.simpleconfig.R;
import com.edong.simpleconfig.util.WifiUtil;
import com.edong.simpleconfig.wifi.WifiAdapter;
import com.edong.simpleconfig.wifi.WifiListPopu;

import java.util.List;

public class WifiListFrag extends Fragment {

    private ListView listView;
    private WifiAdapter adapter;
    private List<ScanResult> scanResults ;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.frag_list_wifi,container,false);

        listView = (ListView) view.findViewById(R.id.list_view);
        register();
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        scanResults = WifiUtil.getScanResults();
        adapter = new WifiAdapter(getContext(),R.layout.item_list_wifi, scanResults);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (itemClickListener!=null){
                    itemClickListener.onItemClick(scanResults.get(i));
                }
            }
        });
    }
    private void register() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_TIME_TICK);
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        getActivity().registerReceiver(millsReceiver,intentFilter);
    }
    private BroadcastReceiver millsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            WifiUtil.startScan();
            if (intent.getAction().equals(Intent.ACTION_TIME_TICK)) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int times = 6;
                        for (int i = 0; i < times; i++) {
                            getWifiList();
                            try {
                                Thread.sleep(60000 / times);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();
            }else if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)){
                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (info.isConnected()&&scanResults.size() == 0) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while (scanResults.size() == 0) {
                                getWifiList();
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }).start();
                }
            }
        }
    };
    private void getWifiList(){
        if (getActivity()==null){
            return;
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                List<ScanResult> list = WifiUtil.getScanResults();
                scanResults.clear();
                scanResults.addAll(list);
                adapter.notifyDataSetChanged();
                if (dataChangeListener!=null){
                    dataChangeListener.onChanged(scanResults);
                }
            }
        });
    }
    @Override
    public void onDestroyView() {

        super.onDestroyView();
        unregister();
    }
    private void unregister(){
        try {
            getActivity().unregisterReceiver(millsReceiver);
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    private OnItemClickListener itemClickListener;
    public void setOnItemClickListener(OnItemClickListener listener){
        this.itemClickListener = listener;
    }
    public interface OnItemClickListener{
        void onItemClick(ScanResult scanResult);
    }

    private DataChangeListener dataChangeListener;
    public void setDataChangeListener(DataChangeListener listener){
        this.dataChangeListener = listener;
    }

    public interface DataChangeListener{
        void onChanged(List<ScanResult> list);
    }

}
