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


    private synchronized boolean writeString(int type, String logs) {
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

    public  boolean writeError(String logs) {
        return writeString(Type.TYPE_ERROR, logs);
    }

    public  boolean writeError(Throwable e) {
        String logs = getExStackTrace(e);
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

    public String getExStackTrace(Throwable ex) {
        Throwable tempT = ex;
        StringBuilder simpleMsg = new StringBuilder();
        String mainMsg = tempT.getMessage();
        if (mainMsg != null) {
            simpleMsg.append("mainEx:");
            simpleMsg.append(mainMsg);
            simpleMsg.append("\n");
        }
        int deep = 1;
        StackTraceElement[] mainStackTrace = tempT.getStackTrace();
        for (int j = 0; j < mainStackTrace.length; j++) {
            if (j <= deep) {
                simpleMsg.append("\tat ");
                simpleMsg.append(mainStackTrace[j].toString());
                simpleMsg.append("\n");
            } else {
                break;
            }
        }
        while ( tempT != null) {
            Throwable cause = tempT.getCause();
            if (cause != null) {
                String tempMsg = cause.getMessage();
                if (tempMsg != null) {
                    simpleMsg.append("caused by:");
                    simpleMsg.append(tempMsg);
                    simpleMsg.append("\n");
                }
                StackTraceElement[] stackTrace = cause.getStackTrace();
                for (int j = 0; j < stackTrace.length; j++) {
                    if (j <= deep) {
                        simpleMsg.append("\tat ");
                        simpleMsg.append(stackTrace[j].toString());
                        simpleMsg.append("\n");
                    } else {
                        break;
                    }
                }
            }
            tempT = cause;
        }
        return simpleMsg.toString();
    }

    public boolean writeError(Throwable e, String msg) {
        String logs = getExStackTrace(e);
        return writeError("Error Exception:description=" + msg + " \ndetail:" + logs);
    }

    public  boolean writeError(Class cla, String logs) {
        return writeString(Type.TYPE_INFO, cla.getSimpleName() + ":" + logs);
    }

    public  boolean writeBug(String logs) {
        return writeString(Type.TYPE_BUG, logs);
    }

    public  boolean writeInfo(String logs) {
        return writeString(Type.TYPE_INFO, logs);
    }

    public  boolean writeInfo(Class cla, String logs) {
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
