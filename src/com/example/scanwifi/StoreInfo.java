package com.example.scanwifi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlSerializer;

import android.net.wifi.ScanResult;
import android.os.Environment;
import android.util.JsonWriter;
import android.util.Log;
import android.util.Xml;

public class StoreInfo {
    private List<List<ScanResult>> m_allResult;
    private int m_frequency;
    private long m_time;
    private Calendar m_startTime;
    private String m_position;

    private long m_index;

    private String m_scenery;

    private XmlSerializer m_serializer = Xml.newSerializer();

    private Map<String, String> m_bssid_ssid;// 存放mac地址和AP名称
    private Map<String, List<String>> m_bssid_rssi;// 存放mac地址和这个AP接收到的所有RSSI值，放在list里

    String tag = "store";

    public StoreInfo(int fren, long time, Calendar start, String position,
            String scenery) throws IllegalArgumentException,
            IllegalStateException {
        m_allResult = new ArrayList<List<ScanResult>>();
        m_frequency = fren;
        m_time = time;
        m_startTime = start;
        m_position = position;

        m_scenery = scenery;

        m_index = 0;
        m_bssid_ssid = new HashMap<String, String>();
        m_bssid_rssi = new HashMap<String, List<String>>();

    }

    public void addResultToList(List<ScanResult> l) {
        m_index++;
        m_allResult.add(l);
        getApAllRSSI();
    }

    public List<ScanResult> getListWithIndex(int i) {
        return m_allResult.get(i);
    }

    public int getListLen() {
        return m_allResult.size();
    }

    /**
     * 
     * @return the data collected
     * @throws JSONException
     */
    public JSONObject getJSONData() throws JSONException {
        JSONObject json = new JSONObject();
        try {
            json.put("startTime", WriteFile.tranTimeToString(m_startTime));
            json.put("duringTime", m_time);
            json.put("freq", m_frequency);
            json.put("location", m_position);
            json.put("scenery", m_scenery);
            Iterator<String> iter = m_bssid_ssid.keySet().iterator();
            JSONArray rssiJson = new JSONArray();
            while (iter.hasNext()) {
                String sBSSID = (String) iter.next();
                String sSSID = m_bssid_ssid.get(sBSSID);
                JSONObject jsontemp = new JSONObject();
                jsontemp.put("apName", sSSID);
                jsontemp.put("mac", sBSSID);
                JSONArray jsonarraytemp = new JSONArray();
                List<String> thisApRssi = m_bssid_rssi.get(sBSSID);
                Iterator<String> iRssi = thisApRssi.iterator();
                while (iRssi.hasNext()) {
                    jsonarraytemp.put(Integer.valueOf(iRssi.next()));
                }
                jsontemp.put("RSSI", jsonarraytemp);
                rssiJson.put(jsontemp);
            }
            json.put("RSSILists", rssiJson);

        } catch (JSONException e) {
            // TODO: handle exception
            Log.e("StoreInfo#getJSONData()", e.getMessage(), e);
        }
        m_bssid_ssid.clear();
        m_bssid_rssi.clear();
        return json;
    }

    public void writeXml() throws IOException {
        File v_xmlfile = new File(Environment.getExternalStorageDirectory()
                .getPath()
                + "SignalStrength/"
                + m_position
                + "_"
                + m_startTime.getTime().toString() + ".xml");
        boolean isExists = v_xmlfile.exists();
        if (!isExists) {
            v_xmlfile.createNewFile();
        } else {
            v_xmlfile.delete();
        }
        Log.v(tag, "json");

        OutputStream v_os = new FileOutputStream(v_xmlfile, true);

        m_serializer.setOutput(v_os, "UTF-8");
        m_serializer.startDocument("UTF-8", true);

        m_serializer.startTag(null, "scan");
        // =====写开始时间，频率，持续时间，所在位置==============
        m_serializer.startTag(null, "startTime");
        m_serializer.text(WriteFile.tranTimeToString(m_startTime));
        m_serializer.endTag(null, "startTime");

        m_serializer.startTag(null, "frequency");
        m_serializer.text(String.valueOf(m_frequency));
        m_serializer.endTag(null, "frequency");

        m_serializer.startTag(null, "duration");
        m_serializer.text(String.valueOf(m_time));
        m_serializer.endTag(null, "duration");

        m_serializer.startTag(null, "position");
        m_serializer.text(m_position);
        m_serializer.endTag(null, "position");

        m_serializer.startTag(null, "allapinfo");
        // ==========写每个RSSI的值
        getApAllRSSI();
        Iterator<String> iBssid = m_bssid_ssid.keySet().iterator();
        Log.v(tag, "rssi");
        while (iBssid.hasNext()) {
            String bssid = iBssid.next();

            m_serializer.startTag(null, "ap");
            // ============写AP名称========================
            m_serializer.startTag(null, "ssid");
            m_serializer.text(m_bssid_ssid.get(bssid));
            m_serializer.endTag(null, "ssid");

            // ============写AP mac地址========================
            m_serializer.startTag(null, "bssid");
            m_serializer.text(bssid);
            m_serializer.endTag(null, "bssid");

            Log.v(tag, "rssi");
            // ============写这个AP接收到的所有rssi值========================
            m_serializer.startTag(null, "rssi");
            List<String> thisApRssi = m_bssid_rssi.get(bssid);
            Iterator<String> iRssi = thisApRssi.iterator();
            while (iRssi.hasNext()) {
                m_serializer.text(iRssi.next() + "\r\n");
            }
            m_serializer.endTag(null, "rssi");

            m_serializer.endTag(null, "ap");
            Log.v(tag, "rssi");
        }
        m_serializer.endTag(null, "allapinfo");
        m_serializer.endTag(null, "scan");
        m_serializer.text("\r\n");
        m_serializer.endDocument();

        v_os.flush();
        v_os.close();
        // }
    }

    // ==========就每个AP的RSSI用list存起来，与AP的mac地址建立map========
    private void getApAllRSSI() {
        // m_bssid_ssid.clear();
        // m_bssid_rssi.clear();

        Iterator<List<ScanResult>> iList = m_allResult.iterator();

        // 本次遍历是统计并记录下所有出现的AP的名称和MAC地址。
        while (iList.hasNext()) {
            List<ScanResult> nextList = iList.next();

            Iterator<ScanResult> iscan = nextList.iterator();

            while (iscan.hasNext()) {
                ScanResult nextScan = iscan.next();

                boolean isHas = m_bssid_ssid.containsKey(nextScan.BSSID);
                if (isHas == false) {// 还不存在这个AP的数据
                    List<String> oneApRssi = new ArrayList<String>();
                    m_bssid_ssid.put(nextScan.BSSID, nextScan.SSID);

                    // oneApRssi.add(String.valueOf(nextScan.level));
                    m_bssid_rssi.put(nextScan.BSSID, oneApRssi);
                    // } else {// 这个AP已经出现过了
                    // List<String> thisApRssi =
                    // m_bssid_rssi.get(nextScan.BSSID);
                    // // thisApRssi.add(String.valueOf(nextScan.level));
                }
            }
        }

        // 再一次的遍历是将RSSI信息补充，之所以要分2次遍历来做是因为有些次的扫描，会没有扫描到有些AP的信息，导致整个数据条目数不同，因此RSSI列表的偏移情况没法再推断。
        // 所以本次遍历不仅要在遍历每次扫描结果时，将扫描到的RSSI信息补充，还要对未被扫描到的AP在同样位置置0以表示当前扫描次数时未扫描到此AP的信息。
        iList = m_allResult.iterator();
        while (iList.hasNext()) {
            List<ScanResult> nextList = iList.next();
            Iterator<ScanResult> iscan = nextList.iterator();

            while (iscan.hasNext()) {
                ScanResult nextScan = iscan.next();
                List<String> thisApRssi = m_bssid_rssi.get(nextScan.BSSID);
                thisApRssi.add(String.valueOf(nextScan.level));
            }

            Iterator<Entry<String, List<String>>> onceResult = m_bssid_rssi
                    .entrySet().iterator();
            while (onceResult.hasNext()) {
                Map.Entry<String, List<String>> entry = (Entry<String, List<String>>) onceResult
                        .next();
                if (entry.getValue().size() == m_index - 1) {
                    entry.getValue().add("0");
                } else if (entry.getValue().size() < m_index - 1) {
                    int i;
                    long len = m_index - entry.getValue().size();
                    Log.v("Ap times",
                            "Ap times not correct. " + String.valueOf(m_index)
                                    + " "
                                    + String.valueOf(entry.getValue().size()));
                    for (i = 0; i < len; i++) {
                        entry.getValue().add(0, "0");
                    }
                    if (entry.getValue().size() != m_index) {
                        Log.w("Ap times",
                                "Ap times not correct. "
                                        + String.valueOf(m_index)
                                        + " "
                                        + String.valueOf(entry.getValue()
                                                .size()));
                    }
                    Log.v(tag, "RSSIList Array Fill " + String.valueOf(i)
                            + " times ");
                    i = 0;
                } else if (entry.getValue().size() != m_index) {
                    Log.w("index", String.valueOf(m_index));
                    Log.w(tag + " AP times",
                            String.valueOf(entry.getValue().size()));
                    Log.w(tag, "RSSIList Array Size not correct!");
                }
            }
        }
        m_allResult.clear();
    }

    public String writeJSON() throws IOException {
        // TODO Auto-generated method stub
        String outpath = "";
        try {
            JSONObject jsonObject = getJSONData();
            if (jsonObject.getJSONArray("RSSILists").length() == 0) {
                Log.e("StoreInfo#writeJSON()",
                        "jsonObject entity RSSILists empty!");
            }
            // 这里只能用横线作为分隔，如果用空格，会在构成文件名时使文件名有空格，会出各种问题。而且文件名中不能有冒号。
            SimpleDateFormat sDateFormat = new SimpleDateFormat(
                    "yyyy-MM-dd-HHmmss", Locale.US);
            String date = sDateFormat.format(new java.util.Date());
            File floder = new File(Environment.getExternalStorageDirectory()
                    .getPath() + "/SignalStrength/");
            if (!(floder.exists()) && !(floder.isDirectory())) {
                floder.mkdirs();
            }
            File v_jsonFile = new File(Environment
                    .getExternalStorageDirectory().getPath()
                    + "/SignalStrength/"
                    + jsonObject.getString("scenery")
                    + "_"
                    + jsonObject.getString("location")
                    + "_"
                    + date
                    + ".json");
            boolean isExists = v_jsonFile.exists();
            if (!isExists) {
                v_jsonFile.createNewFile();
            } else {
                v_jsonFile.delete();
            }
            Log.v(tag, "json file start writing.");
//            FileWriter ft = new FileWriter(v_jsonFile);
            OutputStreamWriter ot = new OutputStreamWriter(new FileOutputStream(v_jsonFile), "utf-8");
            JsonWriter js = new JsonWriter(ot);
            js.beginObject();
            js.name("startTime").value(jsonObject.getString("startTime"));
            js.name("duringTime").value(jsonObject.getString("duringTime"));
            js.name("scenery").value(jsonObject.getString("scenery"));
            js.name("location").value(jsonObject.getString("location"));
            js.name("freq").value(jsonObject.getString("freq"));

            js.name("RSSILists");
            js.beginArray();

            JSONArray lists = jsonObject.getJSONArray("RSSILists");
            for (int i = 0; i < lists.length(); i++) {
                js.beginObject();

                js.name("apName").value(
                        lists.getJSONObject(i).getString("apName"));
                js.name("mac").value(lists.getJSONObject(i).getString("mac"));
                js.name("RSSI");
                js.beginArray();
                JSONArray list = lists.getJSONObject(i).getJSONArray("RSSI");
                int len = list.length();
                for (int j = 0; j < len; j++) {
                    js.value(Integer.valueOf(list.getString(j)));
                }
                js.endArray();
                js.endObject();
            }
            js.endArray();

            js.endObject();

            outpath = v_jsonFile.getAbsolutePath();

            Log.v("writeJsonFileDone", outpath);

            ot.close();

            js.close();

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            Log.e("JSONException", e.getMessage(), e);
            outpath = "";
        } catch (IOException e) {
            // TODO: handle exception
            Log.e("IOException", e.getMessage(), e);
            outpath = "";
        } catch (NullPointerException e) {
            // TODO: handle exception
            Log.e("NullPointerException", e.getMessage(), e);
            outpath = "";
        } catch (Exception e) {
            // TODO: handle exception
            Log.e("Exception", e.getMessage(), e);
            outpath = "";
        }
        return outpath;
    }
}
