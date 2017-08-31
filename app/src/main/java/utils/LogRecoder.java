package utils;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 Created by 张建宇 on 2017/4/27. */

public class LogRecoder {
    private String logName;
    private String filepath;
    private OutputStreamWriter writer;
    private boolean canWrite = false;


    public LogRecoder(String logName, String filepath) {
        this.logName = logName;
        this.filepath = filepath;
        try {
            File rootFile = Environment.getExternalStorageDirectory();
            if (rootFile.length() > 0) {
                FileOutputStream stream;
                if (filepath == null) {
                    stream = new FileOutputStream(rootFile.getAbsolutePath() + "/" + logName, true);
                } else {
                    stream = new FileOutputStream(rootFile.getAbsolutePath() + "/" + filepath + "/" + logName, true);
                }
                try {
                    writer = new OutputStreamWriter(stream, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                canWrite = true;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e("zjy", "LogRecoder->LogRecoder(): file not found==");
        }
    }

    private static class Type {
        static final int TYPE_ERROR = 0;
        static final int TYPE_EXCEPTION = 1;
        static final int TYPE_BUG = 2;
        static final int TYPE_INFO = 3;
    }


    public synchronized boolean writeString(int type, String logs) {
        if (!canWrite) {
            Log.e("zjy", "LogRecoder->writeString(): can not write to log==");
            return false;
        }
        try {
            String tag = "";
            switch (type) {
                case Type.TYPE_BUG:
                    tag = "bug";
                    break;
                case Type.TYPE_ERROR:
                    tag = "error";
                    break;
                case Type.TYPE_EXCEPTION:
                    tag = "exception";
                    break;
                case Type.TYPE_INFO:
                    tag = "info";
                    break;
            }
            writer.write(getFormatDateString(new Date()) + ":" + tag + ":" + logs + "\n");
            writer.flush();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public synchronized boolean writeError(String logs) {
        return writeString(Type.TYPE_ERROR, logs);
    }

    public synchronized boolean writeBug(String logs) {
        return writeString(Type.TYPE_BUG, logs);
    }

    public synchronized boolean writeInfo(String logs) {
        return writeString(Type.TYPE_INFO, logs);
    }

    public synchronized void close() {
        try {
            if (canWrite) {
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getFormatDateString(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }

    public File getlogFile() {
        File root = MyFileUtils.getFileParent();
        if (root == null) {
            return null;
        } else {
            File file = new File(root, logName);
            return file;
        }
    }

}
