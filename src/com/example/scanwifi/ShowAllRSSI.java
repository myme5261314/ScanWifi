package com.example.scanwifi;

import java.io.IOException;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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

public class ShowAllRSSI extends Activity{
	
	TextView ui_tv;
	ScrollView ui_scrollView;
	Button ui_start;
	Button ui_gotoMain;
	EditText ui_frequency;
	EditText ui_time;
	EditText ui_position;
	
	int m_frequency;//hz
	Long m_time;//ms
	long m_startTime = 0;
	String m_position = new String();
	long m_nowTime;
	
	boolean getEnd = false;
	public List<ScanResult> onceResult;
	
	public StartScan m_scanObject;
	public StoreInfo m_storeInfo;
	boolean threadFlag = false;
	
	String tag = "all";
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.showall);
		
		//===============初始化=================================
		ui_tv = (TextView)findViewById(R.id.rssText);
		ui_scrollView = (ScrollView)findViewById(R.id.scrollView1);
		ui_start = (Button)findViewById(R.id.start);
		ui_gotoMain = (Button)findViewById(R.id.gotomain);
		ui_frequency = (EditText)findViewById(R.id.frequency);
		ui_time = (EditText)findViewById(R.id.time);
		ui_position = (EditText)findViewById(R.id.position);
		
		//===================开始设置记录文职的编辑框不可见================
		ui_start.setVisibility(View.INVISIBLE);
		ui_frequency.setText("");
		ui_time.setText("");
		ui_position.setText("");
		
		ui_time.setFocusable(false);
		ui_position.setFocusable(false);
		
		//===================记录输入的频率和持续时间======================
		ui_frequency.setOnEditorActionListener(new OnEditorActionListener(){

			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				// TODO Auto-generated method stub
				if(actionId == EditorInfo.IME_ACTION_UNSPECIFIED){
					m_frequency = Integer.valueOf(ui_frequency.getText().toString());	
					ui_frequency.setText(String.valueOf(m_frequency));
					ui_frequency.setFocusable(false);

					ui_time.setFocusable(true);
					ui_time.setFocusableInTouchMode(true);
					ui_time.requestFocus();
				}
				return false;
			}
			
		});
		
		ui_time.setOnEditorActionListener(new OnEditorActionListener(){

			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				// TODO Auto-generated method stub
				if(actionId == EditorInfo.IME_ACTION_UNSPECIFIED){
					m_time = Long.valueOf(ui_time.getText().toString())*1000;
					ui_time.setText(String.valueOf(m_time/1000));
					ui_time.setFocusable(false);

					ui_position.setFocusable(true);
					ui_position.setFocusableInTouchMode(true);
					ui_position.requestFocus();
				}
				return false;
			}
			
		});
		
		ui_position.setOnEditorActionListener(new OnEditorActionListener(){

			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				// TODO Auto-generated method stub
				if(actionId == EditorInfo.IME_ACTION_UNSPECIFIED){
					m_position = ui_position.getText().toString();
					ui_position.setText(m_position);
					ui_position.setFocusable(false);
					
					InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(ui_position.getWindowToken(), 0);
					
					ui_start.setVisibility(View.VISIBLE);
				}
				return false;
			}
		});
		
		
		//==================回到主页面==============================
		ui_gotoMain.setOnClickListener(new MyButtonListener());
		//设置开始
		ui_start.setOnClickListener(new MyButtonListener());
		
		//==================界面执行逻辑代码===============================
		String connectivity_context = Context.WIFI_SERVICE;
		WifiManager wifi = (WifiManager)getSystemService(connectivity_context);
		m_scanObject = new StartScan(wifi);
		
		
		
	}
	
	class MyButtonListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch(v.getId()){
			case R.id.start:
				
				//记录开始时间
				try {
					m_storeInfo = new StoreInfo(m_frequency, m_time, Calendar.getInstance(), m_position);
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalStateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
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
	
	class myGetReSultThread implements Runnable {     
        public void run() {
        	m_nowTime = Calendar.getInstance().getTimeInMillis();
        	while (m_nowTime - m_startTime < m_time) {
        	
				onceResult = m_scanObject.scan();
				
				long start = Calendar.getInstance().getTimeInMillis();//记录显示所用的时间
				m_storeInfo.addResultToList(onceResult);
				
				Message message = new Message();
        		message.what = 1;
        		handler.sendMessage(message);	
				long end = Calendar.getInstance().getTimeInMillis();
				
	    		try {
	    			Thread.sleep(1000/m_frequency - (end-start));
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
	        	m_nowTime = Calendar.getInstance().getTimeInMillis();
        	}
        	
        	//这段时间已经运行完，弹出是否要保存xml文件的对话框
        	Message message = new Message();
    		message.what = 2;
    		handler.sendMessage(message);	
        }	
    }
	
	Handler handler = new Handler(){
		public void handleMessage(Message msg){
			 switch(msg.what){
			 case 1:
				 ShowOneScanResult(onceResult);
				 ShowRSSI.scroll2Bottom(ui_scrollView,ui_tv);
				 break;
			 case 2:
	        	selectSave();
	        	
	        	ui_frequency.setText("");
	    		ui_time.setText("");
	    		ui_position.setText("");
	    		
	    		ui_position.setFocusable(true);
				ui_position.setFocusableInTouchMode(true);
				ui_position.requestFocus();
	    		
	    		ui_time.setFocusable(true);
	    		ui_time.setFocusableInTouchMode(true);
	    		ui_time.requestFocus();

	    		ui_frequency.setFocusable(true);
	    		ui_frequency.setFocusableInTouchMode(true);
	    		ui_frequency.requestFocus();
	    		
	    		ui_start.setVisibility(View.INVISIBLE);
	    		
	    		break;
			 }
		}
	};
	
	void ShowOneScanResult(List<ScanResult> result){
		Iterator<ScanResult> iscan = result.iterator();
		while (iscan.hasNext()) {
			ScanResult next = iscan.next();
			ui_tv.append(next.SSID + " "+ next.BSSID + " " + String.valueOf(next.level)+"\r\n");
		}
		
		ui_tv.append("\r\n");
		ui_tv.append("\r\n");
	}
	
	void selectSave(){
    	AlertDialog.Builder builder = new Builder(ShowAllRSSI.this);
    	builder.setTitle("提示");
    	builder.setMessage("确定保存吗？");
    	
    	builder.setPositiveButton("确定", new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				try {
					m_storeInfo.writeXml();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
    		
    	});
    	
    	builder.setNegativeButton("取消", new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				
			}
    		
    	});
    	builder.show();
	}

}
