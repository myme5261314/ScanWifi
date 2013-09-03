package com.example.scanwifi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

import android.os.Environment;

public class WriteFile {
    public String m_pathName;
    public String m_fileName;

    String tag = "write";

    public WriteFile(String path, String file) {
        m_pathName = path;
        m_fileName = file;
        // Log.v(tag, m_fileName);
    }

    public Boolean isAvaiable() {
        String sdStatus = Environment.getExternalStorageState();
        if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) {
            return false;
        } else
            return true;
    }

    public void write(String data) throws IOException {
        if (isAvaiable() == true) {
            // Log.v(tag, "avalable");
            // File path = new File(m_pathName);
            File file = new File(m_pathName + "/" + m_fileName);
            // if( !path.exists()) {
            // path.mkdir();
            // }
            if (!file.exists()) {
                file.createNewFile();
                // Log.v(tag, "Creat");
            }
            FileOutputStream stream = new FileOutputStream(file, true);
            byte[] buf = data.getBytes();
            stream.write(buf);
            stream.close();
        }
    }

    public void writeTime(Calendar c) throws IOException {
        String strTime = tranTimeToString(c);
        write(strTime);
    }

    public static String tranTimeToString(Calendar c) {

        String strTime = c.get(Calendar.YEAR) + "-"
                + (c.get(Calendar.MONTH) + 1) + "-"
                + c.get(Calendar.DAY_OF_MONTH) + "  "
                + c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE)
                + ":" + c.get(Calendar.SECOND);

        return strTime;
    }
}
