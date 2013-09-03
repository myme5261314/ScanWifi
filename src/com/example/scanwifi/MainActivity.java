package com.example.scanwifi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class MainActivity extends Activity {
    public ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>();
    public StartScan scanObject;
    public SimpleAdapter listItemAdapter;

    // private String tag = "main";

    Button startScan;
    Button showAll;
    Button sort;
    ListView list;
    TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        startScan = (Button) findViewById(R.id.button1);
        showAll = (Button) findViewById(R.id.button2);
        sort = (Button) findViewById(R.id.button3);

        list = (ListView) findViewById(R.id.listView1);

        String connectivity_context = Context.WIFI_SERVICE;
        WifiManager wifi = (WifiManager) getSystemService(connectivity_context);
        scanObject = new StartScan(wifi);

        // 生成适配器的Item和动态数组对应的元素
        listItemAdapter = new SimpleAdapter(this, listItem,// 数据源
                R.layout.vlist,// ListItem的XML实现
                // 动态数组与ImageItem对应的子项
                new String[] { "ItemSSID", "ItemBSSID", "ItemRSI" },
                // ImageItem的XML文件里面的一个ImageView,两个TextView ID
                new int[] { R.id.SSID, R.id.BSSID, R.id.RSI });

        // 添加并且显示
        list.setAdapter(listItemAdapter);

        startScan.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                Handler handler = new Handler();
                handler.post(run);
            }
        });

        // 添加点击
        list.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                    long arg3) {
                // TODO Auto-generated method stub
                @SuppressWarnings("unchecked")
                HashMap<String, Object> test = (HashMap<String, Object>) arg0
                        .getItemAtPosition(arg2);
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, ShowRSSI.class);
                intent.putExtra("msg", 2);
                intent.putExtra("SSID", String.valueOf(test.get("ItemSSID")));
                intent.putExtra("BSSID", String.valueOf(test.get("ItemBSSID")));

                MainActivity.this.startActivity(intent);
                MainActivity.this.finish();
            }
        });

        showAll.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                Intent intent = new Intent();

                // intent.setClass(MainActivity.this, ShowRSSI.class);
                // intent.putExtra("msg", 1);
                intent.setClass(MainActivity.this, ShowAllRSSI.class);

                MainActivity.this.startActivity(intent);
                MainActivity.this.finish();
            }

        });

        sort.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                List<ScanResult> result = scanObject.sortScanResultWithRSS();
                addListToAdapter(result);
            }

        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    Runnable run = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            List<ScanResult> result = scanObject.scan();
            addListToAdapter(result);
        }
    };

    void addListToAdapter(List<ScanResult> result) {
        // 生成动态数组，加入数据
        listItem.clear();

        Iterator<ScanResult> iscan = result.iterator();
        while (iscan.hasNext()) {
            ScanResult next = iscan.next();
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("ItemSSID", next.SSID);// 图像资源的ID
            map.put("ItemBSSID", next.BSSID);
            map.put("ItemRSI", next.level);
            listItem.add(map);
        }
        listItemAdapter.notifyDataSetChanged();
    }
}
