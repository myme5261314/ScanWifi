package com.example.scanwifi;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.ConnectException;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.R.integer;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class ShowAllRSSI extends Activity {

    TextView ui_tv;
    ScrollView ui_scrollView;
    Button ui_start;
    Button ui_gotoMain;
    EditText ui_frequency;
    EditText ui_time;
    EditText ui_position;
    EditText ui_writefileInterval;

    EditText ui_scenery;

    TextProgressBar ui_progressBar;

    int m_frequency;// hz
    Long m_time;// ms
    long m_startTime = 0;

    long m_progress_total = 0;
    long m_progress_current = 0;
    String m_position = new String();

    String m_scenery = new String();
    int m_writefileInterval = Integer.valueOf(R.string.WriteFile_DataTimes);

    long m_nowTime;

    JSONObject m_json = new JSONObject();

    boolean getEnd = false;
    public List<ScanResult> onceResult;

    public StartScan m_scanObject;
    public StoreInfo m_storeInfo = null, m_preStoreInfo = null;
    boolean threadFlag = false;

    String tag = "all";

    private MyHandler_All handler = new MyHandler_All(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.showall);

        // ===============初始化=================================
        ui_tv = (TextView) findViewById(R.id.rssText);
        ui_scrollView = (ScrollView) findViewById(R.id.scrollView1);
        ui_start = (Button) findViewById(R.id.start);
        ui_gotoMain = (Button) findViewById(R.id.gotomain);
        ui_frequency = (EditText) findViewById(R.id.frequency);
        ui_time = (EditText) findViewById(R.id.time);
        ui_position = (EditText) findViewById(R.id.position);

        ui_scenery = (EditText) findViewById(R.id.scenery);

        ui_writefileInterval = (EditText) findViewById(R.id.WriteFileInterval);
        ui_progressBar = (TextProgressBar) findViewById(R.id.progress);

        // ===================开始设置记录文职的编辑框不可见================
        ui_start.setVisibility(View.INVISIBLE);

        ui_time.setFocusable(false);
        ui_position.setFocusable(false);
        ui_scenery.setFocusable(false);
        ui_writefileInterval.setFocusable(false);

        // ===================记录输入的频率和持续时间======================
        ui_frequency.setOnEditorActionListener(new OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId,
                    KeyEvent event) {
                // TODO Auto-generated method stub
                if (actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
                    ui_frequency.setText(ui_frequency.getText().toString()
                            .replaceAll("\n", ""));
                    m_frequency = Integer.valueOf(ui_frequency.getText()
                            .toString());
                    ui_frequency.setText(String.valueOf(m_frequency));
                    ui_frequency.setFocusable(false);

                    ui_time.setFocusable(true);
                    ui_time.setFocusableInTouchMode(true);
                    ui_time.requestFocus();
                }
                return false;
            }

        });

        ui_time.setOnEditorActionListener(new OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId,
                    KeyEvent event) {
                // TODO Auto-generated method stub
                if (actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
                    ui_time.setText(ui_time.getText().toString()
                            .replaceAll("\n", ""));
                    m_time = Long.valueOf(ui_time.getText().toString()) * 1000;
                    ui_time.setText(String.valueOf(m_time / 1000));
                    ui_time.setFocusable(false);

                    ui_position.setFocusable(true);
                    ui_position.setFocusableInTouchMode(true);
                    ui_position.requestFocus();
                }
                return false;
            }

        });

        ui_position.setOnEditorActionListener(new OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId,
                    KeyEvent event) {
                // TODO Auto-generated method stub
                if (actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
                    ui_position.setText(ui_position.getText().toString()
                            .replaceAll("\n", ""));
                    m_position = ui_position.getText().toString();
                    ui_position.setText(m_position);
                    ui_position.setFocusable(false);

                    ui_scenery.setFocusable(true);
                    ui_scenery.setFocusableInTouchMode(true);
                    ui_scenery.requestFocus();
                }
                return false;
            }
        });

        ui_scenery.setOnEditorActionListener(new OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId,
                    KeyEvent event) {
                // TODO Auto-generated method stub
                if (actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
                    ui_scenery.setText(ui_scenery.getText().toString()
                            .replaceAll("\n", ""));
                    m_scenery = ui_scenery.getText().toString();
                    ui_scenery.setText(m_scenery);
                    ui_scenery.setFocusable(false);
                    
                    ui_writefileInterval.setFocusable(true);
                    ui_writefileInterval.setFocusableInTouchMode(true);
                    ui_writefileInterval.requestFocus();

                }
                return false;
            }
        });
        
        ui_writefileInterval.setOnEditorActionListener(new OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId,
                    KeyEvent event) {
                // TODO Auto-generated method stub
                if (actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
                    ui_writefileInterval.setText(ui_writefileInterval.getText().toString()
                            .replaceAll("\n", ""));
                    m_writefileInterval = Integer.valueOf(ui_writefileInterval.getText().toString());
                    ui_writefileInterval.setText(String.valueOf(m_writefileInterval));
                    ui_writefileInterval.setFocusable(false);

                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(ui_writefileInterval.getWindowToken(), 0);

                    ui_start.setVisibility(View.VISIBLE);
                    
                    ui_progressBar.setMax((int)(m_time * m_frequency / 1000));
                }
                return false;
            }
        });

        // ==================回到主页面==============================
        ui_gotoMain.setOnClickListener(new MyButtonListener());
        // 设置开始
        ui_start.setOnClickListener(new MyButtonListener());

        // ==================界面执行逻辑代码===============================
        String connectivity_context = Context.WIFI_SERVICE;
        WifiManager wifi = (WifiManager) getSystemService(connectivity_context);
        m_scanObject = new StartScan(wifi);

    }

    class MyButtonListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            switch (v.getId()) {
            case R.id.start:

                // 记录开始时间
                try {
                    if (m_storeInfo == null) {
                        m_storeInfo = new StoreInfo(m_frequency, m_time,
                                Calendar.getInstance(), m_position, m_scenery);
                    }
                } catch (IllegalArgumentException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IllegalStateException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                Log.v(tag, "start");
                m_startTime = Calendar.getInstance().getTimeInMillis();

                Log.v(tag, "start");
                Thread myGetThread = new Thread(new myGetReSultThread());
                myGetThread.start();

                break;

            case R.id.gotomain:
                Intent intent = new Intent();

                intent.setClass(ShowAllRSSI.this, MainActivity.class);

                ShowAllRSSI.this.startActivity(intent);
                ShowAllRSSI.this.finish();
                break;
            }
        }
    }

    class mySendJSONThread implements Runnable {
        String m_serverAddress = getString(R.string.RemoteWebServiceAddress);
        String m_serviceName = getString(R.string.WebServiceName);
        String m_methodName = getString(R.string.WebServiceFunctionName);

        public mySendJSONThread() {

        }

        public mySendJSONThread(String serverAddress, String serviceName,
                String methodName) {
            m_serverAddress = serverAddress;
            m_serviceName = serviceName;
            m_methodName = methodName;
        }

        @Override
        public void run() {
            // TODO Auto-generated method stub
            try {
                String url = m_serverAddress + m_serviceName + "."
                        + m_methodName;
                HttpPost request = new HttpPost(url);
                request.setHeader("Content-Type", "application/json");
                // 先封装一个 JSON 对象
                JSONObject param = m_storeInfo.getJSONData();
                // param.put("my_name", "Peng");
                // 绑定到请求 Entry
                StringEntity se = new StringEntity(param.toString());
                request.setEntity(se);
                // 发送请求
                HttpResponse httpResponse = new DefaultHttpClient()
                        .execute(request);

                if (httpResponse.getStatusLine().getStatusCode() == 200) {
                    // 获取返回的数据
                    Log.v(tag, "send json data successful.");
                    Toast.makeText(getApplicationContext(), "发送数据成功",
                            Toast.LENGTH_SHORT).show();

                } else {
                    Log.v(tag, "send json data failed.");
                    Toast.makeText(getApplicationContext(), "发送数据失败",
                            Toast.LENGTH_SHORT).show();

                }
                Log.v("success", "json data recieved");

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ConnectException e) {
                // TODO: handle exception
                e.printStackTrace();
            } catch (IOException e) {
                // TODO: handle exception
                e.printStackTrace();
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
        }

    }

    class myGetReSultThread implements Runnable {
        public void run() {
            m_nowTime = Calendar.getInstance().getTimeInMillis();
            // 每采集多少次就写一下文件，防止因为内存不足导致程序崩溃。
            int singletimes = m_writefileInterval;
            // 总共要采集的次数。
            long times = m_time * m_frequency / 1000;
            m_progress_total = times;
            m_progress_current = 0;
            // 每次单次写文件时说采集的时间。
            m_time = (long) singletimes / m_frequency;
            int quotient = (int) (times / singletimes);
            int remainder = (int) (times % singletimes);
            // 每次循环完之后就写文件。
            for (int i = 0; i < quotient; i++) {
                int index = 0;
                m_storeInfo = new StoreInfo(m_frequency, m_time,
                        Calendar.getInstance(), m_position, m_scenery);
                for (int j = 0; j < singletimes; j++) {
                    long start = Calendar.getInstance().getTimeInMillis();
                    // 记录显示所用的时间
                    index++;
                    m_progress_current++;
                    onceResult = m_scanObject.scan();
                    if (m_storeInfo == null) {
                        Log.e("ShowAllRSSI#myGetReSultThread#run()",
                                "m_storeInfo is null");
                    }
                    if (onceResult == null) {
                        Log.e("ShowAllRSSI#myGetReSultThread#run()",
                                "onceResult is null");
                    }
                    m_storeInfo.addResultToList(onceResult);

                    // 每隔60次扫描则清空TextView的显示内容，防止因为TextView内容一直累积导致内存不足。
                    // if (index % 60 == 0) {
                    // ui_tv.setText("");
                    // }
                    Message message = new Message();
                    message.what = 1;
                    handler.sendMessage(message);
                    long end = Calendar.getInstance().getTimeInMillis();

                    try {
                        long sleeptime = (1000 / m_frequency) - (end - start);
                        if (sleeptime > 0) {
                            Thread.sleep(sleeptime);
                        }

                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    Log.v(tag, String.valueOf(index));
                    m_nowTime = Calendar.getInstance().getTimeInMillis();

                }
                m_preStoreInfo = m_storeInfo;
                m_storeInfo = null;
                Message message = new Message();
                message.what = 2;
                handler.sendMessage(message);
                // selectSave();
            }
            m_storeInfo = new StoreInfo(m_frequency, remainder / m_frequency,
                    Calendar.getInstance(), m_position, m_scenery);
            int index = 0;
            for (int i = 0; i < remainder; i++) {
                index++;
                long start = Calendar.getInstance().getTimeInMillis();//
                // 记录显示所用的时间
                // index++;
                m_progress_current++;
                onceResult = m_scanObject.scan();

                m_storeInfo.addResultToList(onceResult);

                Message message = new Message();
                message.what = 1;
                handler.sendMessage(message);
                long end = Calendar.getInstance().getTimeInMillis();

                try {
                    long sleeptime = (1000 / m_frequency) - (end - start);
                    if (sleeptime > 0) {
                        Thread.sleep(sleeptime);
                    }

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                Log.v(tag, String.valueOf(index));
                m_nowTime = Calendar.getInstance().getTimeInMillis();
            }
            if (remainder > 0) {
                m_preStoreInfo = m_storeInfo;
                m_storeInfo = null;
                Message message = new Message();
                message.what = 2;
                handler.sendMessage(message);
                // selectSave();
            }
            Message message = new Message();
            message.what = 3;
            handler.sendMessage(message);
        }

        void ShowOneScanResult(List<ScanResult> result) {
            Iterator<ScanResult> iscan = result.iterator();
            while (iscan.hasNext()) {
                ScanResult next = iscan.next();
                ui_tv.append(next.SSID + " " + next.BSSID + " "
                        + String.valueOf(next.level) + "\r\n");
            }

            ui_tv.append("\r\n");
            ui_tv.append("\r\n");
        }
    }

    static class MyHandler_All extends Handler {
        private final WeakReference<ShowAllRSSI> mActivity;

        public MyHandler_All(ShowAllRSSI activity) {
            mActivity = new WeakReference<ShowAllRSSI>(activity);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
            case 1:
                if (mActivity.get().ui_progressBar.getMax() != mActivity.get().m_progress_total) {
                    mActivity.get().ui_progressBar
                            .setMax((int) mActivity.get().m_progress_total);
                }
                mActivity.get().ui_progressBar.setProgress((int) mActivity
                        .get().m_progress_current);
                // if (ui_tv.length()>=2000) {
                // ui_tv.setText("");
                // }
                // ShowOneScanResult(onceResult);
                // ShowRSSI.scroll2Bottom(ui_scrollView, ui_tv);
                break;
            case 2:
                if (mActivity.get() != null) {
                    mActivity.get().selectSave();
                }
                break;
            case 3:
                if (mActivity.get() != null) {


                    mActivity.get().ui_frequency.setFocusable(true);
                    mActivity.get().ui_frequency.setFocusableInTouchMode(true);
                    mActivity.get().ui_frequency.requestFocus();
                    
                    mActivity.get().ui_time.setFocusable(false);
                    mActivity.get().ui_time.setFocusableInTouchMode(false);
//                    mActivity.get().ui_time.requestFocus();
                    
                    mActivity.get().ui_position.setFocusable(false);
                    mActivity.get().ui_position.setFocusableInTouchMode(false);
//                    mActivity.get().ui_position.requestFocus();
                    
                    mActivity.get().ui_scenery.setFocusable(false);
                    mActivity.get().ui_scenery.setFocusableInTouchMode(false);
//                    mActivity.get().ui_scenery.requestFocus();
                    
                    mActivity.get().ui_writefileInterval.setFocusable(false);
                    mActivity.get().ui_writefileInterval.setFocusableInTouchMode(false);
//                    mActivity.get().ui_writefileInterval.requestFocus();

                    mActivity.get().ui_start.setVisibility(View.INVISIBLE);
                }
                break;
            }
        }
    }

    public void selectSave() {
        try {
            StoreInfo tempInfo = null;
            if (m_preStoreInfo != null) {
                tempInfo = m_preStoreInfo;
                m_preStoreInfo = null;
            }

            String spath = tempInfo.writeJSON();
            // 向系统广播文件变更。
            if (spath != "") {
                Uri path = Uri.parse("file://" + spath);
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                        path));
            }
            tempInfo = null;
            // m_storeInfo.writeXml();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            Log.e("ShowAllRSSI#selectSave()", e.getMessage(), e);
        }
    }
}
