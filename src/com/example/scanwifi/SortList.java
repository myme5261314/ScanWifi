package com.example.scanwifi;

import java.util.Comparator;

import android.net.wifi.ScanResult;

public class SortList implements Comparator<Object> {

    @Override
    public int compare(Object arg0, Object arg1) {
        // TODO Auto-generated method stub
        ScanResult ap1 = (ScanResult) arg0;
        ScanResult ap2 = (ScanResult) arg1;

        int flag = 1;
        if (ap1.level > ap2.level)
            flag = -1;

        return flag;
    }
}
