package rxsocket.rxsocket.util;

import android.Manifest;
import android.content.Context;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;
import android.util.Log;


import java.util.List;

import static android.content.Context.WIFI_SERVICE;

public class WifiUtil {

    private static final String TAG = "my";
    //private static final WifiUtil instance = new WifiUtil();

    private Context mContex;
    private WifiManager wifi;

    public WifiUtil(Context mContex, WifiManager wifi) {
        this.mContex = mContex;
        this.wifi = wifi;
    }
    public String getScanResultSecurity(ScanResult scanResult) {
        final String cap = scanResult.capabilities;
        final String[] securityModes = {"WEP", "PSK", "EAP"};
        for (int i = securityModes.length - 1; i >= 0; i--) {
            if (cap.contains(securityModes[i])) {
                return securityModes[i];
            }
        }
        return "OPEN";
    }

    public int connectToAP(Context context, String networkSSID, String networkPasskey) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);
        for (ScanResult result : wifiManager.getScanResults()) {
            if (result.SSID.equals(networkSSID)) {
                String securityMode = getScanResultSecurity(result);
                WifiConfiguration wifiConfiguration = createAPConfiguration(networkSSID, networkPasskey, securityMode);
                int res = wifiManager.addNetwork(wifiConfiguration);
                Log.i(TAG, "WifiUtil: # addNetwork returned " + res);
                boolean b = wifiManager.enableNetwork(res, true);
                Log.i(TAG, "WifiUtil: # enableNetwork returned " + b);
                wifiManager.setWifiEnabled(true);
                boolean changeHappen = wifiManager.saveConfiguration();
                if (res != -1 && changeHappen) {
                    Log.i(TAG, "WifiUtil: # Change happen: " + networkSSID);
                } else {
                    Log.i(TAG, "WifiUtil: # Change NOT happen");
                }
                return res;
            }
        }
        return -1;
    }

    private WifiConfiguration createAPConfiguration(String networkSSID, String networkPasskey, String securityMode) {
        WifiConfiguration wifiConfiguration = new WifiConfiguration();
        wifiConfiguration.SSID = "\"" + networkSSID + "\"";
        if (securityMode.equalsIgnoreCase("OPEN")) {
            wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        } else if (securityMode.equalsIgnoreCase("WEP")) {
            wifiConfiguration.wepKeys[0] = "\"" + networkPasskey + "\"";
            wifiConfiguration.wepTxKeyIndex = 0;
            wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
        } else if (securityMode.equalsIgnoreCase("PSK")) {
            wifiConfiguration.preSharedKey = "\"" + networkPasskey + "\"";
            wifiConfiguration.hiddenSSID = true;
            wifiConfiguration.status = WifiConfiguration.Status.ENABLED;
            wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        } else {
            Log.i(TAG, "WifiUtil: # Unsupported security mode: " + securityMode);
            return null;
        }
        return wifiConfiguration;
    }

    @RequiresPermission(Manifest.permission.ACCESS_WIFI_STATE)
    public static boolean isWifiConnected(@NonNull WifiManager wiFiManager) {
        //final WifiManager wiFiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wiFiManager != null && wiFiManager.isWifiEnabled()) {
            // Wi-Fi adapter is ON
            final WifiInfo wifiInfo = wiFiManager.getConnectionInfo();
            return wifiInfo != null && wifiInfo.getNetworkId() != -1;
        } else {
            // Wi-Fi adapter is OFF
            return false;
        }
    }

    public String getSupplicantStateText(SupplicantState supplicantState) {
        if (SupplicantState.FOUR_WAY_HANDSHAKE.equals(supplicantState)) {
            return "FOUR WAY HANDSHAKE";
        } else if (SupplicantState.ASSOCIATED.equals(supplicantState)) {
            return "ASSOCIATED";
        } else if (SupplicantState.ASSOCIATING.equals(supplicantState)) {
            return "ASSOCIATING";
        } else if (SupplicantState.COMPLETED.equals(supplicantState)) {
            return "COMPLETED";
        } else if (SupplicantState.DISCONNECTED.equals(supplicantState)) {
            return "DISCONNECTED";
        } else if (SupplicantState.DORMANT.equals(supplicantState)) {
            return "DORMANT";
        } else if (SupplicantState.GROUP_HANDSHAKE.equals(supplicantState)) {
            return "GROUP HANDSHAKE";
        } else if (SupplicantState.INACTIVE.equals(supplicantState)) {
            return "INACTIVE";
        } else if (SupplicantState.INVALID.equals(supplicantState)) {
            return "INVALID";
        } else if (SupplicantState.SCANNING.equals(supplicantState)) {
            return "SCANNING";
        } else if (SupplicantState.UNINITIALIZED.equals(supplicantState)) {
            return "UNINITIALIZED";
        } else {
            return "supplicant state is bad";
        }
    }

    public void connectWiFiToESP(String SSID, String password, String Security) {
        try {

            Log.i(TAG, "WifiUtil: Item clicked, SSID " + SSID + " Security : " + Security);

            String networkSSID = SSID;
            String networkPass = password;

            WifiConfiguration conf = new WifiConfiguration();
            conf.SSID = "\"" + networkSSID + "\"";   // Please note the quotes. String should contain ssid in quotes
            conf.status = WifiConfiguration.Status.ENABLED;
            conf.priority = 40;

            if (Security.toUpperCase().contains("WEP")) {
                Log.i(TAG, "WifiUtil: Configuring WEP");
                conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                conf.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                conf.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                conf.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                conf.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
                conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);

                if (networkPass.matches("^[0-9a-fA-F]+$")) {
                    conf.wepKeys[0] = networkPass;
                } else {
                    conf.wepKeys[0] = "\"".concat(networkPass).concat("\"");
                }

                conf.wepTxKeyIndex = 0;

            } else if (Security.toUpperCase().contains("WPA")) {
                Log.i(TAG, "WifiUtil: Configuring WPA");

                conf.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                conf.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);

                conf.preSharedKey = "\"" + networkPass + "\"";

            } else {
                Log.i(TAG, "WifiUtil: Configuring OPEN network");
                conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                conf.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                conf.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                conf.allowedAuthAlgorithms.clear();
                conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            }

            //WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            int networkId = wifi.addNetwork(conf);

            Log.i(TAG, "WifiUtil: Add result " + networkId);

            List<WifiConfiguration> list = wifi.getConfiguredNetworks();
            for (WifiConfiguration i : list) {
                if (i.SSID != null && i.SSID.equals("\"" + networkSSID + "\"")) {
                    Log.i(TAG, "WifiUtil: WifiConfiguration SSID " + i.SSID);

                    boolean isDisconnected = wifi.disconnect();
                    Log.i(TAG, "WifiUtil: isDisconnected : " + isDisconnected);

                    boolean isEnabled = wifi.enableNetwork(i.networkId, true);
                    Log.i(TAG, "WifiUtil: isEnabled : " + isEnabled);

                    boolean isReconnected = wifi.reconnect();
                    Log.i(TAG, "WifiUtil: isReconnected : " + isReconnected);

                    break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getSSID(Context mContex) {
        WifiManager wifiManager = (WifiManager) mContex.getSystemService(WIFI_SERVICE);
        if (wifiManager == null) {
            return null;
        }
        WifiInfo connectionInfo = wifiManager.getConnectionInfo();
        if (connectionInfo == null) {
            return null;
        }
        String ssid = connectionInfo.getSSID();
        if (ssid == null) {
            ssid = null;
        }
        return ssid;
    }

    public String getWifiName(Context mContex) {
        //wifi  = (WifiManager) mContex.getSystemService(Context.WIFI_SERVICE);
        if (wifi.isWifiEnabled()) {
            WifiInfo wifiInfo = wifi.getConnectionInfo();
            if (wifiInfo != null) {
                NetworkInfo.DetailedState state = WifiInfo.getDetailedStateOf(wifiInfo.getSupplicantState());
                if (state == NetworkInfo.DetailedState.CONNECTED || state == NetworkInfo.DetailedState.OBTAINING_IPADDR) {
                    return wifiInfo.getSSID();
                }
            }
        }
        return "no equals";
    }

}