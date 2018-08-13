package com.edong.simpleconfig.sclib;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.util.Log;

import com.realtek.simpleconfiglib.SCJNI;
import com.realtek.simpleconfiglib.SCJNI.Args;
import com.realtek.simpleconfiglib.SCNetworkOps;
import com.realtek.simpleconfiglib.SCParam;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ConfigLibrary {

    private static final String TAG = "ConfigLibrary";

    public static ConfigLibrary configLibrary = new ConfigLibrary();

    public Handler TreadMsgHandler = new Handler();
    public static int TotalConfigTimeMs;
    public static int OldModeConfigTimeMs;
    public static byte ProfileSendRounds;
    public static int ProfileSendTimeIntervalMs;
    public static int PacketSendTimeIntervalMs;
    public static byte EachPacketSendCounts;
    public static boolean PackType;
    private SCNetworkOps SCNetOps;
    private SCJNI ScJni = new SCJNI();
    private Args ScArgs;
    private Thread SendThread;
    private Thread RecvThread;
    private boolean ConfigSuccess;
    public boolean SendEnable;
    private boolean SendInProgress;
    private boolean RecvEnable;

    static {
        System.loadLibrary("simpleconfiglib");
        TotalConfigTimeMs = 32000;
        OldModeConfigTimeMs = 0;
        ProfileSendRounds = 1;
        ProfileSendTimeIntervalMs = 1000;
        PacketSendTimeIntervalMs = 0;
        EachPacketSendCounts = 1;
        PackType = true;
        SCParam.SC_PIN = "57289961";
    }

    public ConfigLibrary() {
        this.ScJni.getClass();
        this.ScArgs = ScJni.new Args();
        this.ConfigSuccess = false;
        this.SendEnable = false;
        this.SendInProgress = false;
        this.RecvEnable = false;
    }

    public void WifiInit(Context context) {
        this.SCNetOps.WifiInit(context);
    }

    public void WifiOpen() {
        this.SCNetOps.WifiOpen();
    }

    public boolean IsWifiApEnabled(Context context) {
        return this.SCNetOps.isApEnabled(context);
    }

    public void WifiStartScan() {
        this.SCNetOps.WifiStartScan();
    }

    public List<ScanResult> getScanResults() {
        return this.SCNetOps.WifiGetScanResults();
    }

    public List<WifiConfiguration> getConfiguredNetworks(){
        return this.SCNetOps.WifiGetConfiguredNetworks();
    }
    public int WifiStatus() {
        return this.SCNetOps.WifiStatus();
    }

    public String WifiAvailable() {
        return this.SCNetOps.WifiAvailable();
    }

    public boolean isWifiConnected(String ssid) {
        return this.SCNetOps.isWifiConnected(ssid);
    }

    public String getConnectedWifiSSID() {
        return this.SCNetOps.getConnectedWifiSSID();
    }

    public String getConnectedWifiBSSID() {
        return this.SCNetOps.getConnectedWifiBSSID();
    }

    public String WifiGetMacStr() {
        return this.SCNetOps.WifiGetMacStr();
    }

    public int WifiGetIpInt() {
        return this.SCNetOps.WifiGetIpInt();
    }

    public String WifiGetIpString(int ipInt) {
        return this.SCNetOps.WifiGetIpString(ipInt);
    }

    private void RtkSCNetInit() {
        this.SCNetOps = new SCNetworkOps();
        SCParam.UDPBcast.SrcPort = 18864;
        SCParam.UDPBcast.DestPort = 18864;
        this.SCNetOps.BroadcastSocketCreate();
        SCParam.UDPUcast.SrcPort = 8864;
        SCParam.UDPUcast.DestPort = 8864;
        this.SCNetOps.UnicastSocketCreate();
    }

    public void rtk_sc_reset() {
        this.ConfigSuccess = false;
        SCParam.RecvACK.MaxCfgNum = 0;
        Arrays.fill(SCParam.RecvACK.Status, (byte)0);

        for(int i = 0; i < 32; ++i) {
            Arrays.fill(SCParam.RecvACK.Mac[i], (byte)0);
            Arrays.fill(SCParam.RecvACK.Type[i], (byte)0);
            Arrays.fill(SCParam.RecvACK.IPBuf[i], (byte)0);
            Arrays.fill(SCParam.RecvACK.NameBuf[i], (byte)0);
        }

        SCParam.RecvACK.IP = new String[32];
        SCParam.RecvACK.Name = new String[32];
    }

    public void rtk_sc_init() {
        this.RtkSCNetInit();
        this.rtk_sc_reset();
        this.RtkSCRecvThread();
    }

    public void rtk_sc_exit() {
        this.SCNetOps.BroadcastSocketDestroy();
        this.SCNetOps.UnicastSocketDestroy();
        this.SendEnable = false;
        this.ScJni.StopConfig();
        this.RecvEnable = false;
        this.ConfigSuccess = false;
        this.RecvThread.interrupt();
    }

    public void rtk_sc_set_ssid(String ssid) {
        if (ssid != null) {
            SCParam.SC_SSID = ssid;
        }
    }

    public void rtk_sc_set_password(String passwd) {
        if (passwd != null) {
            SCParam.SC_PASSWD = passwd;
        }
    }

    public void rtk_sc_set_ip(int ip) {
        SCParam.SC_IP = ip;
    }

    public void rtk_sc_set_bssid(String bssid) {
        if (bssid == null) {
            Log.e("ConfigLibrary", "BSSID is null\n");
        } else {
            SCParam.SC_BSSID = bssid;
        }
    }

    public void rtk_sc_set_default_pin(String pin) {
        if (pin != null && pin.length() > 0) {
            SCParam.Default_PIN = pin;
        } else {
            Log.e("ConfigLibrary", "Invalid PIN");
        }

    }

    public String rtk_sc_get_default_pin() {
        return SCParam.Default_PIN;
    }

    public void rtk_sc_set_pin(String pin) {
        if (pin != null && pin.length() > 0) {
            SCParam.SC_PIN = pin;
            this.ScArgs.Mode = 3;
        } else {
            SCParam.SC_PIN = SCParam.Default_PIN;
            this.ScArgs.Mode = 2;
        }

    }

    public void rtk_sc_build_profile() {
    }

    public void rtk_sc_start(final String ssid, final String passwd, final String pin, final String bssid, final boolean pkt_type, final boolean issoftap, final int total_time, final int old_mode_time, final byte profile_rounds, final int profile_interval, final int packet_interval, final byte packet_counts, final String hostip, final String wifi_interface, final String phoneMac) {
        Log.d("ConfigLibrary", "hostip : " + hostip);
        Log.d("ConfigLibrary", "wifi_interface : " + wifi_interface);
        Log.d("ConfigLibrary", "phoneMac : " + phoneMac);
        this.SendEnable = true;
        this.SendThread = new Thread(new Runnable() {
            public void run() {
                ConfigLibrary.this.SendInProgress = true;
                Process.setThreadPriority(0);
                long startTime = System.currentTimeMillis();
                long endTime = System.currentTimeMillis();
                long timeElasped = endTime - startTime;
                ConfigLibrary.this.ScArgs.SSID = ssid.getBytes();
                ConfigLibrary.this.ScArgs.SSIDLen = (byte)ssid.getBytes().length;
                ConfigLibrary.this.ScArgs.Passwd = passwd.getBytes();
                ConfigLibrary.this.ScArgs.PasswdLen = (byte)passwd.getBytes().length;
                ConfigLibrary.this.ScArgs.PIN = pin.getBytes();
                ConfigLibrary.this.ScArgs.PINLen = (byte)pin.getBytes().length;
                ConfigLibrary.this.ScArgs.BSSID = bssid.getBytes();
                ConfigLibrary.this.ScArgs.BSSIDLen = (byte)bssid.getBytes().length;
                ConfigLibrary.this.ScArgs.ProfileRounds = profile_rounds;
                ConfigLibrary.this.ScArgs.ProfileInterval = profile_interval;
                ConfigLibrary.this.ScArgs.PacketInterval = packet_interval;
                ConfigLibrary.this.ScArgs.PacketCnts = packet_counts;
                ConfigLibrary.this.ScArgs.Pack_type = pkt_type;
                ConfigLibrary.this.ScArgs.isSoftApMode = issoftap;
                ConfigLibrary.this.ScArgs.hostIP = hostip.getBytes();
                ConfigLibrary.this.ScArgs.HostIPLEN = (byte)hostip.getBytes().length;
                ConfigLibrary.this.ScArgs.wifiInterface = wifi_interface.getBytes();
                ConfigLibrary.this.ScArgs.WifiInterfaceLEN = (byte)wifi_interface.getBytes().length;
                ConfigLibrary.this.ScArgs.PhoneMac = phoneMac.getBytes();
                ConfigLibrary.this.ScArgs.PhoneMacLen = (byte)phoneMac.getBytes().length;
                ConfigLibrary.this.ScArgs.Length = ConfigLibrary.this.ScArgs.SSIDLen + ConfigLibrary.this.ScArgs.PasswdLen + 4 + 2;
                if (ConfigLibrary.this.ScArgs.SSIDLen > 0) {
                    ++ConfigLibrary.this.ScArgs.Length;
                }

                while(ConfigLibrary.this.SendEnable && !ConfigLibrary.this.ConfigSuccess && timeElasped < (long)old_mode_time) {
                    Log.i("ConfigLibrary", "Start old mode config...");
                    ConfigLibrary.this.ScArgs.ConfigTime = old_mode_time;
                    ConfigLibrary.this.ScArgs.SyncRounds = 1;
                    ConfigLibrary.this.ScJni.StartConfig(ConfigLibrary.this.ScArgs);
                    endTime = System.currentTimeMillis();
                    timeElasped = endTime - startTime;
                }

                while(ConfigLibrary.this.SendEnable && !ConfigLibrary.this.ConfigSuccess && timeElasped < (long)total_time) {
                    Log.i("ConfigLibrary", "Start new mode config...");
                    ConfigLibrary.this.ScArgs.ConfigTime = total_time - old_mode_time;
                    ConfigLibrary.this.ScArgs.SyncRounds = 16;
                    ConfigLibrary.this.ScArgs.Mode = 4;
                    ConfigLibrary.this.ScJni.StartConfig(ConfigLibrary.this.ScArgs);
                    endTime = System.currentTimeMillis();
                    timeElasped = endTime - startTime;
                }

                endTime = System.currentTimeMillis();
                timeElasped = endTime - startTime;
                Log.i("ConfigLibrary", "Total Config Time Elapsed: " + timeElasped + "ms");
                Message msg;
                if (!ConfigLibrary.this.ConfigSuccess && timeElasped > (long)total_time) {
                    msg = Message.obtain();
                    msg.obj = null;
                    msg.what = -1;
                    ConfigLibrary.this.TreadMsgHandler.sendMessage(msg);
                }

                try {
                    Thread.sleep(500L);
                } catch (InterruptedException var8) {
                    var8.printStackTrace();
                }

                msg = Message.obtain();
                msg.obj = Long.toString(timeElasped);
                msg.what = 5;
                ConfigLibrary.this.TreadMsgHandler.sendMessage(msg);
                ConfigLibrary.this.SendInProgress = false;
            }
        });
        if (!this.SendInProgress) {
            this.SendThread.start();
        } else {
            Log.w("ConfigLibrary", "Config already in progress!");
        }

    }

    public void rtk_sc_start(String in_wifiInterface, String in_hostip) {
        SCParam.SC_WIFI_Interface = in_wifiInterface;
        SCParam.SC_HOSTIP = in_hostip;
        SCParam.SC_PHONE_MAC_ADDR = this.SCNetOps.WifiGetMacStr();
        SCParam.SC_SOFTAP_MODE = false;
        this.rtk_sc_start(SCParam.SC_SSID, SCParam.SC_PASSWD, SCParam.SC_PIN, SCParam.SC_BSSID, SCParam.SC_PKT_TYPE, SCParam.SC_SOFTAP_MODE, TotalConfigTimeMs, OldModeConfigTimeMs, ProfileSendRounds, ProfileSendTimeIntervalMs, PacketSendTimeIntervalMs, EachPacketSendCounts, SCParam.SC_HOSTIP, SCParam.SC_WIFI_Interface, SCParam.SC_PHONE_MAC_ADDR);
    }

    public void rtk_sc_stop() {
        this.SendEnable = false;
        this.ScJni.StopConfig();
        this.ConfigSuccess = false;
    }

    public int HandleCfgACK(byte[] recv_buf) {
        int cfgIndex = 0;
        int MacEqualCnt = 0;
        int length = recv_buf[1] << 8 & '\uff00' | recv_buf[2] & 255;
        if (length < 13) {
            Log.e("ConfigLibrary", "Received format error\n");
            return -1;
        } else if (SCParam.RecvACK.MaxCfgNum > 32) {
            Log.e("ConfigLibrary", "Receive buf is full\n");
            return -1;
        } else {
            int i;
            if (SCParam.RecvACK.MaxCfgNum > 0) {
                for(cfgIndex = 0; cfgIndex < SCParam.RecvACK.MaxCfgNum; ++cfgIndex) {
                    for(i = 0; i < 6; ++i) {
                        if (recv_buf[3 + i] == SCParam.RecvACK.Mac[cfgIndex][i]) {
                            ++MacEqualCnt;
                        }
                    }

                    if (MacEqualCnt == 6) {
                        break;
                    }

                    MacEqualCnt = 0;
                }
            }

            byte[] ip_buf = new byte[4];
            System.arraycopy(recv_buf, 12, ip_buf, 0, 4);
            String IPStr = String.format("%d.%d.%d.%d", ip_buf[0] & 255, ip_buf[1] & 255, ip_buf[2] & 255, ip_buf[3] & 255);
            String MacStr;
            Message msg;
            if (SCParam.RecvACK.IP[cfgIndex] != null && SCParam.RecvACK.IP[cfgIndex].length() > 0 && !SCParam.RecvACK.IP[cfgIndex].equals(IPStr)) {
                System.arraycopy(recv_buf, 3, SCParam.RecvACK.Mac[cfgIndex], 0, 6);
                MacStr = new String();

                for(i = 0; i < 6; ++i) {
                    MacStr = MacStr + String.format("%02x", SCParam.RecvACK.Mac[cfgIndex][i]);
                    if (i < 5) {
                        MacStr = MacStr + ":";
                    }
                }

                System.arraycopy(recv_buf, 12, SCParam.RecvACK.IPBuf[cfgIndex], 0, 4);
                SCParam.RecvACK.IP[cfgIndex] = String.format("%d.%d.%d.%d", SCParam.RecvACK.IPBuf[cfgIndex][0] & 255, SCParam.RecvACK.IPBuf[cfgIndex][1] & 255, SCParam.RecvACK.IPBuf[cfgIndex][2] & 255, SCParam.RecvACK.IPBuf[cfgIndex][3] & 255);
                Log.i("ConfigLibrary", "Refresh IP: " + SCParam.RecvACK.IP[cfgIndex] + " of MAC: " + MacStr);
                this.rtk_sc_send_cfg_ack_packet();
                msg = Message.obtain();
                msg.obj = null;
                msg.what = 0;
                this.TreadMsgHandler.sendMessage(msg);
            }

            if (SCParam.RecvACK.MaxCfgNum > 0) {
                return 0;
            } else if (MacEqualCnt == 6) {
                return 0;
            } else {
                System.arraycopy(recv_buf, 3, SCParam.RecvACK.Mac[SCParam.RecvACK.MaxCfgNum], 0, 6);
                MacStr = new String();

                for(i = 0; i < 6; ++i) {
                    MacStr = MacStr + String.format("%02x", SCParam.RecvACK.Mac[SCParam.RecvACK.MaxCfgNum][i]);
                    if (i < 5) {
                        MacStr = MacStr + ":";
                    }
                }

                Log.i("ConfigLibrary", "Added MAC: " + MacStr);
                if (length >= 7) {
                    SCParam.RecvACK.Status[SCParam.RecvACK.MaxCfgNum] = recv_buf[9];
                }

                if (length >= 9) {
                    System.arraycopy(recv_buf, 10, SCParam.RecvACK.Type[SCParam.RecvACK.MaxCfgNum], 0, 2);
                }

                if (length >= 13) {
                    System.arraycopy(recv_buf, 12, SCParam.RecvACK.IPBuf[SCParam.RecvACK.MaxCfgNum], 0, 4);
                    SCParam.RecvACK.IP[SCParam.RecvACK.MaxCfgNum] = String.format("%d.%d.%d.%d", SCParam.RecvACK.IPBuf[SCParam.RecvACK.MaxCfgNum][0] & 255, SCParam.RecvACK.IPBuf[SCParam.RecvACK.MaxCfgNum][1] & 255, SCParam.RecvACK.IPBuf[SCParam.RecvACK.MaxCfgNum][2] & 255, SCParam.RecvACK.IPBuf[SCParam.RecvACK.MaxCfgNum][3] & 255);
                    Log.i("ConfigLibrary", "IP: " + SCParam.RecvACK.IP[SCParam.RecvACK.MaxCfgNum]);
                    this.rtk_sc_send_cfg_ack_packet();
                }

                if (length >= 77) {
                    System.arraycopy(recv_buf, 16, SCParam.RecvACK.NameBuf[SCParam.RecvACK.MaxCfgNum], 0, 64);
                    String name = null;

                    try {
                        name = (new String(SCParam.RecvACK.NameBuf[SCParam.RecvACK.MaxCfgNum], "UTF-8")).trim();
                    } catch (UnsupportedEncodingException var11) {
                        Log.e("ConfigLibrary", "Get device's name error");
                        var11.printStackTrace();
                    }

                    if (name.length() > 0) {
                        SCParam.RecvACK.Name[SCParam.RecvACK.MaxCfgNum] = name;
                    } else {
                        SCParam.RecvACK.Name[SCParam.RecvACK.MaxCfgNum] = null;
                    }

                    Log.i("ConfigLibrary", "Name: " + SCParam.RecvACK.Name[SCParam.RecvACK.MaxCfgNum]);
                }

                if (length >= 78) {
                    SCParam.RecvACK.UsePin[SCParam.RecvACK.MaxCfgNum] = recv_buf[80] > 0;
                }

                ++SCParam.RecvACK.MaxCfgNum;
                msg = Message.obtain();
                msg.obj = null;
                msg.what = 0;
                this.TreadMsgHandler.sendMessage(msg);
                return 0;
            }
        }
    }

    public void rtk_sc_send_cfg_ack_packet() {
        byte[] CmdBuf = new byte[92];
        Arrays.fill(CmdBuf, (byte)0);
        CmdBuf[0] = (byte)(CmdBuf[0] + 0);
        CmdBuf[0] = (byte)(CmdBuf[0] + 0);
        CmdBuf[0] = (byte)(CmdBuf[0] + 4);
        CmdBuf[1] = 0;
        CmdBuf[2] = 90;
        CmdBuf[3] = 0;
        int i;
        if (SCParam.RecvACK.IP[0] != null && SCParam.RecvACK.IP[0].length() > 0 && !SCParam.RecvACK.IP[0].equals("0.0.0.0")) {
            SCParam.UDPUcast.IPAddr = SCParam.RecvACK.IP[0];
            SCParam.UDPUcast.SendLen = CmdBuf.length;
            SCParam.UDPUcast.SendMsg = CmdBuf;

            for(i = 0; i < 8; ++i) {
                this.SCNetOps.UDPUnicastSend();
            }
        }

        SCParam.UDPBcast.IPAddr = "255.255.255.255";
        SCParam.UDPBcast.SendLen = CmdBuf.length;
        SCParam.UDPBcast.SendMsg = CmdBuf;

        for(i = 0; i < 8; ++i) {
            this.SCNetOps.UDPBroadcastSend();
        }

    }

    private int RtkSCParseResult() {
        int recv_len = SCParam.UDPUcast.RecvLen;
        byte[] recv_buf = new byte[recv_len];
        System.arraycopy(SCParam.UDPUcast.RecvBuf, 0, recv_buf, 0, recv_len);
        if (recv_len < 9) {
            Log.e("ConfigLibrary", "ACK too short\n");
            return -1;
        } else {
            byte flag = recv_buf[0];
            if ((flag & (SCParam.BIT(7) | SCParam.BIT(6))) != 0) {
                Log.e("ConfigLibrary", "ACK version not match\n");
                return -1;
            } else if ((flag & SCParam.BIT(5)) != 32) {
                return -1;
            } else {
                Message msg = Message.obtain();
                switch(flag & 31) {
                    case 0:
                        this.SendEnable = false;
                        this.ConfigSuccess = true;
                        this.ScJni.StopConfig();
                        int ack_ret = this.HandleCfgACK(recv_buf);
                        return ack_ret;
                    case 1:
                        msg.obj = recv_buf;
                        msg.what = 1;
                        this.TreadMsgHandler.sendMessage(msg);
                        break;
                    case 2:
                        msg.obj = recv_buf;
                        msg.what = 2;
                        this.TreadMsgHandler.sendMessage(msg);
                        break;
                    case 3:
                        msg.obj = recv_buf;
                        msg.what = 3;
                        this.TreadMsgHandler.sendMessage(msg);
                        break;
                    case 4:
                        msg.obj = recv_buf;
                        msg.what = 4;
                        this.TreadMsgHandler.sendMessage(msg);
                        break;
                    default:
                        Log.e("ConfigLibrary", "Unknow response");
                }

                return 0;
            }
        }
    }

    public void RtkSCRecvThread() {
        this.RecvEnable = true;
        this.RecvThread = new Thread(new Runnable() {
            public void run() {
                boolean ret = false;

                while(ConfigLibrary.this.RecvEnable) {
                    ret = false;

                    try {
                        ret = ConfigLibrary.this.SCNetOps.UDPUnicastRecv();
                    } catch (Exception var3) {
                        var3.printStackTrace();
                    }

                    if (ret) {
                        try {
                            ConfigLibrary.this.RtkSCParseResult();
                        } catch (Exception var4) {
                            var4.printStackTrace();
                            Log.e("ConfigLibrary", "Parse Result Error");
                            break;
                        }
                    }
                }

            }
        });
        this.RecvThread.start();
    }

    public int rtk_sc_get_connected_sta_num() {
        return SCParam.RecvACK.MaxCfgNum;
    }

    public int rtk_sc_get_connected_sta_info(List<HashMap<String, Object>> DevInfo) {
        for(int index = 0; index < SCParam.RecvACK.MaxCfgNum; ++index) {
            String tmpStr = new String();
            HashMap<String, Object> hmap = new HashMap();

            for(int i = 0; i < 6; ++i) {
                tmpStr = tmpStr + String.format("%02x", SCParam.RecvACK.Mac[index][i]);
                if (i < 5) {
                    tmpStr = tmpStr + ":";
                }
            }

            hmap.put("MAC", tmpStr);
            new String();
            switch(SCParam.RecvACK.Status[index]) {
                case 1:
                    tmpStr = "Connected";
                    break;
                case 2:
                    tmpStr = "Profile saved";
                    break;
                default:
                    tmpStr = "Unkown status";
            }

            hmap.put("Status", tmpStr);
            new String();
            short type = (short)((SCParam.RecvACK.Type[index][0] & '\uff00') + (SCParam.RecvACK.Type[index][1] & 255));
            switch(type) {
                case 0:
                    tmpStr = "Any type";
                    break;
                case 1:
                    tmpStr = "TV";
                    break;
                case 2:
                    tmpStr = "Air conditioner";
                    break;
                default:
                    tmpStr = "Unkown type";
            }

            hmap.put("Type", tmpStr);
            hmap.put("IP", SCParam.RecvACK.IP[index]);
            hmap.put("Name", SCParam.RecvACK.Name[index]);
            hmap.put("PIN", SCParam.RecvACK.UsePin[index]);
            DevInfo.add(hmap);
        }

        return 0;
    }

    public int rtk_sc_send_discover_packet(byte[] cmdbuf, String send_ip) {
        try {
            SCParam.UDPBcast.IPAddr = send_ip;
            SCParam.UDPBcast.SendLen = cmdbuf.length;
            SCParam.UDPBcast.SendMsg = cmdbuf;
            this.SCNetOps.UDPBroadcastSend();
        } catch (Exception var4) {
            var4.printStackTrace();
        }

        return 0;
    }

    public int rtk_sc_send_control_packet(byte[] cmdbuf, String send_ip) {
        try {
            SCParam.UDPUcast.IPAddr = send_ip;
            SCParam.UDPUcast.SendLen = cmdbuf.length;
            SCParam.UDPUcast.SendMsg = cmdbuf;
            this.SCNetOps.UDPUnicastSend();
        } catch (Exception var4) {
            var4.printStackTrace();
        }

        return 0;
    }

    public void rtk_sc_set_packet_type(boolean pkt_type) {
        SCParam.SC_PKT_TYPE = pkt_type;
    }

    public void rtk_sc_set_softap(boolean issoftap) {
        SCParam.SC_SOFTAP_MODE = issoftap;
    }

    public String rtk_sc_get_softap_ssid() {
        return this.SCNetOps.getWifiApSSID();
    }
}
