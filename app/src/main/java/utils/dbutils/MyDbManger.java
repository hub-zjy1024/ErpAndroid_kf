package utils.dbutils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.b1b.js.erpandroid_kf.MyApp;

import java.util.HashMap;

/**
 Created by 张建宇 on 2017/8/22. */

public class MyDbManger extends SQLiteOpenHelper {
    public static final int VERSION = 1;
    public static final int LINE_LIMIT = 200;
    public static final String YD_TABLE = "sfyundan";

    public MyDbManger(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public MyDbManger(Context context, String name) {
        super(context, name, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table sfyundan (pid text primary key ," +
                "sf_orderid text, " +
                "code text," +
                "goodInfos text," +
                "cardID text," +
                "payPart text," +
                "payType text," +
                "baojia float," +
                "serverType text," +
                "printName text," +
                "hasE text," +
                "yundanType text" +
                ")";
        db.execSQL(sql);

    }

    public void insertYundan(String pid, String orderID, String code, String goodInfos, String cardID, String payPart, String
            payType, String
            serverType, double baojia, String printName, String hasE, String yundanType) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("pid", pid);
        values.put("sf_orderid", orderID);
        values.put("code", code);
        values.put("goodInfos", goodInfos);
        values.put("cardID", cardID);
        values.put("serverType", serverType);
        values.put("payType", payType);
        values.put("payPart", payPart);
        values.put("baojia", baojia);
        values.put("printName", printName);
        values.put("hasE", hasE);
        values.put("yundanType", yundanType);
        HashMap<String, String> map = serachYundan(pid);
        if (map.size() == 0) {
            db.insert(YD_TABLE, null, values);
        } else {
            db.update(YD_TABLE, values, "pid=?", new String[]{pid});
            MyApp.myLogger.writeBug("order yundan again");
        }
    }

    public HashMap<String, String> serachYundan(String pid) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor result = db.query(YD_TABLE, new String[]{"sf_orderid", "code", "goodInfos", "serverType", "baojia",
                "printName", "hasE", "cardID","payType","payPart", "yundanType"}, "pid=?", new String[]{pid}, null, null, null);
        HashMap<String, String> map = new HashMap<>();
        Log.e("zjy", "MyDbManger->serachYundan(): matchcouts==" + result.getCount());
        while (result.moveToNext()) {
            String orderid = result.getString(result.getColumnIndex("sf_orderid"));
            String destcode = result.getString(result.getColumnIndex("code"));
            String goodInfos = result.getString(result.getColumnIndex("goodInfos"));
            String serverType = result.getString(result.getColumnIndex("serverType"));
            String baojia = result.getString(result.getColumnIndex("baojia"));
            String printName = result.getString(result.getColumnIndex("printName"));
            String hasE = result.getString(result.getColumnIndex("hasE"));
            String cardID = result.getString(result.getColumnIndex("cardID"));
            String yundanType = result.getString(result.getColumnIndex("yundanType"));
            String payPart = result.getString(result.getColumnIndex("payPart"));
            String payType = result.getString(result.getColumnIndex("payType"));
            map.put("orderid", orderid);
            map.put("destcode", destcode);
            map.put("goodInfos", goodInfos);
            map.put("serverType", serverType);
            map.put("cardID", cardID);
            map.put("baojia", baojia);
            map.put("payPart", payPart);
            map.put("payType", payType);
            map.put("printName", printName);
            map.put("hasE", hasE);
            map.put("yundanType", yundanType);
        }
        deletRecords(db, pid, LINE_LIMIT);
        result.close();
        return map;
    }

    public void deletRecords(SQLiteDatabase db, String pid, int couts) {
        Cursor result = db.query(YD_TABLE, new String[]{"pid"}, null, null, null, null, null);
        if (result.getCount() > couts) {
            db.delete(YD_TABLE, "pid<>?", new String[]{pid});
        }
        result.close();

    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
