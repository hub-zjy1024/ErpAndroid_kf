package utils;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by 张建宇 on 2017/4/27.
 */

public class LogRecoder {
    private String filepath;
    private OutputStreamWriter writer;


    public LogRecoder(String logPath) {
        this.filepath = logPath;
        init(true);
    }

    private static class Type {
        static final int TYPE_ERROR = 0;
        static final int TYPE_EXCEPTION = 1;
        static final int TYPE_BUG = 2;
        static final int TYPE_INFO = 3;
    }


    public synchronized boolean writeString(int type, String logs) {
        if (writer == null) {
            Log.e("zjy", "LogRecoder->writeString(): can not write to log==");
            return false;
        }
        String tag = "[default]";
        switch (type) {
            case Type.TYPE_BUG:
                tag = "[bug]";
                break;
            case Type.TYPE_ERROR:
                tag = "[error]";
                break;
            case Type.TYPE_EXCEPTION:
                tag = "[exception]";
                break;
            case Type.TYPE_INFO:
                tag = "[info]";
                break;
        }
        try {
            writer.write(getFormatDateString(new Date()) + ":" + tag + ":" + logs + "\r\n");
            writer.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public synchronized boolean writeError(String logs) {
        return writeString(Type.TYPE_ERROR, logs);
    }

    public synchronized boolean writeError(Throwable e) {
        String logs = getStacktrace(e);
        return writeError("Error Exception:" + logs);
    }

    private static String getStacktrace(Throwable e) {
        String result = "null Exception";
        if (e != null) {
            StringWriter sw = new StringWriter();
            PrintWriter writer = new PrintWriter(sw);
            e.printStackTrace(writer);
            writer.flush();
            result = sw.toString();
            return result;
        }
        return result;
    }

    public synchronized boolean writeError(Throwable e, String msg) {
        String logs = getStacktrace(e);
        return writeError("Error Exception:description=" + msg + " \ndetail:" + logs);
    }

    public synchronized boolean writeError(Class cla, String logs) {
        return writeString(Type.TYPE_INFO, cla.getSimpleName() + ":" + logs);
    }

    public synchronized boolean writeBug(String logs) {
        return writeString(Type.TYPE_BUG, logs);
    }

    public synchronized boolean writeInfo(String logs) {
        return writeString(Type.TYPE_INFO, logs);
    }

    public synchronized boolean writeInfo(Class cla, String logs) {
        return writeString(Type.TYPE_INFO, cla.getSimpleName() + ":" + logs);
    }

    public synchronized void close() {
        try {
            if (writer != null) {
                writer.close();
                writer = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void init(boolean overWrite) {
        try {
            File rootFile = Environment.getExternalStorageDirectory();
            if (rootFile.length() > 0) {
                File file = new File(rootFile, "/" + filepath);
                if (!file.exists()) {
                    file.getParentFile().mkdirs();
                }
                FileOutputStream stream = new FileOutputStream(file, overWrite);
                try {
                    writer = new OutputStreamWriter(stream, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public String getFormatDateString(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }

    public File getlogFile() {
        File root = Environment.getExternalStorageDirectory();
        if (root == null) {
            return null;
        } else {
            File file = new File(root, filepath);
            return file;
        }
    }

}
