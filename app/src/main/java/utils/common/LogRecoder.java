package utils.common;

import android.content.Context;
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
import java.util.Locale;

/**
 * Created by 张建宇 on 2017/4/27.
 */

public class LogRecoder {
    private String filepath;
    private OutputStreamWriter writer;
    private Context mContext;

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

    public boolean writeError(String logs) {
        return writeString(Type.TYPE_ERROR, logs);
    }

    public boolean writeError(Throwable e) {
        String logs = getExStackTrace(e);
        return writeError("Error Exception:" + logs);
    }


    public String getAllStackInfo(Throwable e) {
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
            simpleMsg.append("exMsg=");
            simpleMsg.append(mainMsg);
            simpleMsg.append("\n");
        }
        Throwable mainThrowa= tempT;
        String mainStackMsg = getInnerEx(mainThrowa);
        simpleMsg.append("\tmainStack= ");
        simpleMsg.append(mainStackMsg);
        simpleMsg.append("\n");
        while (tempT != null) {
            simpleMsg.append("causedBy=");
            Throwable cause = tempT.getCause();
            String msg = getInnerEx(cause);
            //            Log.e("zjy", getClass() + "->getExStackTrace inner(): ==" + msg);
            simpleMsg.append(msg);
            tempT = cause;
        }
        return simpleMsg.toString();
    }

    String getInnerEx(Throwable tempT) {
        if (tempT == null) {
            return "no cause";
        }
       /* 获取栈信息的最大深度*/
        int deep = 100;
        StackTraceElement[] mainStackTrace = tempT.getStackTrace();
        int stackDeep = mainStackTrace.length;
        int msgLines = 0;
        StringBuilder simpleMsg = new StringBuilder();
        //倒序导出
        for (int j = stackDeep - 1; j >= 0; j--) {
            if (stackDeep - j <= deep) {
                StackTraceElement ele = mainStackTrace[j];
//                Log.e("zjy", getClass() + "->getExStackTrace():stack ==" + ele.toString());
                try {
                    String className = ele.getClassName();
                    Class cla = Class.forName(className);
                    Class loaderCla = cla.getClassLoader().getClass();
                    String loaderName = loaderCla.getName();
//                                        Log.e("zjy", "LogRecoder->getExStackTrace(): Loader==" + loaderName + "\t " +
//                                                "ele_cla_name=" + className);
                    //过滤系统class，只关注程序内部class
                    String filterLoader = "java.lang.BootClassLoader";
                    if (!loaderName.equals(filterLoader)) {
                        simpleMsg.append("\tat ");
                        simpleMsg.append(ele.toString());
                        simpleMsg.append("\n");
                        msgLines++;
                    }else{
                        simpleMsg.append("\tat ");
                        simpleMsg.append("not my code...");
                        simpleMsg.append("\n");
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            } else {
                break;
            }
        }
        if (msgLines <= 3) {
            simpleMsg = new StringBuilder();
            simpleMsg.append(getAllStackInfo(tempT));
        }
        return simpleMsg.toString();
    }

    public boolean writeError(Throwable e, String msg) {
        String logs = getExStackTrace(e);
        return writeError("Error Exception:description=" + msg + " \ndetail:" + logs);
    }

    public boolean writeError(Class cla, String logs) {
        return writeString(Type.TYPE_INFO, cla.getSimpleName() + ":" + logs);
    }

    public boolean writeBug(String logs) {
        return writeString(Type.TYPE_BUG, logs);
    }

    public boolean writeInfo(String logs) {
        return writeString(Type.TYPE_INFO, logs);
    }

    public boolean writeInfo(Class cla, String logs) {
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
//                Uri fileUri = Uri.fromFile(file);
//                mContext.getContentResolver().openOutputStream(Uri.fromFile(file), "rw");
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
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
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
