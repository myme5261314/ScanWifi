package com.example.scanwifi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

public class StartScan {
    WifiManager m_wifi;
    List<ScanResult> m_result;

    public StartScan(WifiManager wifi) {
        m_wifi = wifi;
        if (!m_wifi.isWifiEnabled()) {
            if (m_wifi.getWifiState() != WifiManager.WIFI_STATE_ENABLING) {
                m_wifi.setWifiEnabled(true);
            }
        }
    }

    public List<ScanResult> scan() {
        m_wifi.startScan();
        m_result = m_wifi.getScanResults();
        return m_result;
    }

    public int getOnRSSI(String bssid) {
        List<ScanResult> result = scan();
        Iterator<ScanResult> iscan = result.iterator();
        while (iscan.hasNext()) {
            ScanResult nextResult = iscan.next();

            if (bssid.compareTo(nextResult.BSSID) == 0)
                return nextResult.level;
        }
        return -200;
    }

    public List<ScanResult> sortScanResultWithRSS() {
        scan();// 扫描得到周围的AP的RSS值

        SortList sort = new SortList();
        Collections.sort(m_result, sort);

        return m_result;
    }

    public List<ScanResult> getScanReslutWithSSID(String ssid) {

        List<ScanResult> result = new ArrayList<ScanResult>();
        scan();

        Iterator<ScanResult> iscan = m_result.iterator();
        while (iscan.hasNext()) {
            ScanResult nextResult = iscan.next();
            if (nextResult.SSID.compareTo(ssid) == 0) {
                result.add(nextResult);
            }
        }

        return result;
    }
}
