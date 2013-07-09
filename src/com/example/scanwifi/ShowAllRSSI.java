package com.example.scanwifi;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
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

	EditText ui_scenery;

	int m_frequency;// hz
	Long m_time;// ms
	long m_startTime = 0;
	String m_position = new String();

	String m_scenery = new String();

	long m_nowTime;

	JSONObject m_json = new JSONObject();

	boolean getEnd = false;
	public List<ScanResult> onceResult;

	public StartScan m_scanObject;
	public StoreInfo m_storeInfo=null, m_preStoreInfo=null;
	boolean threadFlag = false;

	String tag = "all";

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

		// ===================开始设置记录文职的编辑框不可见================
		ui_start.setVisibility(View.INVISIBLE);
		// ui_frequency.setText("");
		// ui_time.setText("");
		// ui_position.setText("");

		ui_time.setFocusable(false);
		ui_position.setFocusable(false);

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

					// InputMethodManager imm = (InputMethodManager)
					// getSystemService(Context.INPUT_METHOD_SERVICE);
					// imm.hideSoftInputFromWindow(ui_position.getWindowToken(),
					// 0);
					//
					// ui_start.setVisibility(View.VISIBLE);
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

					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(ui_position.getWindowToken(), 0);

					ui_start.setVisibility(View.VISIBLE);
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
				// catch (IOException e) {
				// // TODO Auto-generated catch block
				// e.printStackTrace();
				// }

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

				// 得到应答的字符串，这也是一个 JSON 格式保存的数据
//				String retSrc = EntityUtils.toString(httpResponse.getEntity());
				// 生成 JSON 对象

//				JSONObject result = new JSONObject(retSrc);
				// String token = result.get("hello").toString();
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
			int singletimes = 3600;
			// 总共要采集的次数。
			long times = m_time * m_frequency / 1000;
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
					onceResult = m_scanObject.scan();

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
				Message message = new Message();
				message.what = 2;
				handler.sendMessage(message);
				// selectSave();
			}
			m_storeInfo = new StoreInfo(m_frequency, singletimes / m_frequency,
					Calendar.getInstance(), m_position, m_scenery);
			int index = 0;
			for (int i = 0; i < remainder; i++) {
				index++;
				long start = Calendar.getInstance().getTimeInMillis();//
				// 记录显示所用的时间
				// index++;
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
				Message message = new Message();
				message.what = 2;
				handler.sendMessage(message);
				// selectSave();
			}

			// int index = 0;
			// while (m_nowTime - m_startTime < m_time
			// || index < m_time / (m_frequency * 1000)) {
			// while (index < m_time * m_frequency / 1000) {
			// index++;
			// onceResult = m_scanObject.scan();
			//
			// long start = Calendar.getInstance().getTimeInMillis();//
			// // 记录显示所用的时间
			// m_storeInfo.addResultToList(onceResult);
			//
			// // if (index % 60 == 0){
			// // ui_tv.setText("");
			// // }
			// Message message = new Message();
			// message.what = 1;
			// handler.sendMessage(message);
			// long end = Calendar.getInstance().getTimeInMillis();
			//
			// try {
			// long sleeptime = (1000 / m_frequency) - (end - start);
			// if (sleeptime > 0) {
			// Thread.sleep(sleeptime);
			// }
			//
			// } catch (InterruptedException e) {
			// Thread.currentThread().interrupt();
			// }
			// Log.v(tag, String.valueOf(index));
			// m_nowTime = Calendar.getInstance().getTimeInMillis();
			// }
			//
			// // 这段时间已经运行完，弹出是否要保存xml文件的对话框
			// Message message = new Message();
			// message.what = 2;
			// handler.sendMessage(message);
			// }
		}

		Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case 1:
					 if (ui_tv.length()>=2000) {
					 ui_tv.setText("");
					 }
					// ShowOneScanResult(onceResult);
					// ShowRSSI.scroll2Bottom(ui_scrollView, ui_tv);
					break;
				case 2:
					 selectSave();
					
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

		void selectSave() {
			try {
				if (m_preStoreInfo == null) {
					m_preStoreInfo = m_storeInfo;
					m_storeInfo = null;
				}
				
				String spath = m_preStoreInfo.writeJSON();
				// 向系统广播文件变更。
				if (spath != "") {
					Uri path = Uri.parse("file://" + spath);
					sendBroadcast(new Intent(
							Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, path));
				}
				m_preStoreInfo = null;
				// m_storeInfo.writeXml();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// AlertDialog.Builder builder = new Builder(ShowAllRSSI.this);
			// builder.setTitle("提示");
			// builder.setMessage("确定保存吗？");
			//
			// builder.setPositiveButton("确定",
			// new DialogInterface.OnClickListener() {
			//
			// @Override
			// public void onClick(DialogInterface arg0, int arg1) {
			// // TODO Auto-generated method stub
			// try {
			// String spath = m_storeInfo.writeJSON();
			// // 向系统广播文件变更。
			// if (spath != "") {
			// Uri path = Uri.parse("file://" + spath);
			// sendBroadcast(new Intent(
			// Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
			// path));
			// }
			// // m_storeInfo.writeXml();
			// } catch (IOException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
			// }
			//
			// });
			//
			// builder.setNegativeButton("取消",
			// new DialogInterface.OnClickListener() {
			//
			// @Override
			// public void onClick(DialogInterface arg0, int arg1) {
			// // TODO Auto-generated method stub
			// /**
			// * 向Web Service服务器端发送JSON数据。
			// */
			// try {
			// Thread mySendJSONThread = new Thread(
			// new mySendJSONThread());
			// mySendJSONThread.start();
			// } catch (Exception e) {
			// // TODO: handle exception
			// Toast.makeText(getApplicationContext(),
			// "发送数据失败", Toast.LENGTH_SHORT).show();
			// e.printStackTrace();
			// }
			//
			// }
			//
			// });
			// builder.show();
		}
	}
}
