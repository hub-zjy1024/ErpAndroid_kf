package utils.dbutils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Created by 张建宇 on 2019/12/31.
 */
public class ActivityRecoderDB extends MyDbManger {
    private static String dbName = "ac_recorder.db";

    public ActivityRecoderDB(Context context, String name) {
        super(context, name);
    }

    public static ActivityRecoderDB newInstance(Context context) {
        return new ActivityRecoderDB(context, dbName);
    }

    private String mTable = "ac_created";

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table if not exists  " +
                "" + mTable +
                " (id INTEGER primary key AUTOINCREMENT," +
                "time INTEGER, " +
                "name text" +
                ")";
        db.execSQL(sql);
    }

    public void addRecord(Class cla) {
        String claName = cla.getName();
        long time = System.currentTimeMillis();
        SQLiteDatabase writableDatabase = getWritableDatabase();
        ContentValues mValues = new ContentValues();
        mValues.put("time",time );
        mValues.put("name",claName );
        long insert = writableDatabase.insert(mTable, null, mValues);
        if (insert == -1) {
            Log.e("zjy", "ActivityRecoderDB->addRecord(): error==" + insert);
        } else {
            Log.w("zjy", "ActivityRecoderDB->addRecord(): ok==" + claName + ",time=" + longToDateStr(time));
        }
        writableDatabase.close();
    }

    public void clearAll(){
        SQLiteDatabase writableDatabase = getWritableDatabase();
        int delete = writableDatabase.delete(mTable, null, null);
        Log.w("zjy", "ActivityRecoderDB->clearAll(): delet==" + delete);
    }

    public static class RecordInfo {
        int id;
        String name;
        long time;

        @NonNull
        @Override
        public String toString() {
            return String.format("id=%d,name=%s,time=%s", id, name, new Date(time).toString());
        }
    }


    private String longToDateStr(long time) {
        return new Date(time).toString();
    }

    public String getRecorderStrByDate(Date date) {
        List<RecordInfo> msgByDay = getMsgByDay(date);
        date.setHours(0);
        long time1 = date.getTime();
        date.setHours(22);
        long time2 = date.getTime();
        Log.w("zjy",
                "ActivityRecoderDB->getRecorderStrByDate(): time1==" + longToDateStr(time1) + "\t" + longToDateStr(time2));
        StringBuilder sb = new StringBuilder();
        StringBuilder infoSB = new StringBuilder();

        HashMap<String, Integer> map = new HashMap<>();

        if (msgByDay != null) {
            for (int i = 0; i < msgByDay.size(); i++) {
                RecordInfo minfo = msgByDay.get(i);
                long time = minfo.time;
                String name = minfo.name;
                if (map.containsKey(name)) {
                    Integer integer = map.get(name);
                    map.put(name, integer.intValue() + 1);
                }else {
                    map.put(name, 1);
                }
                if (time > time1 && time < time2) {
                    sb.append(minfo.toString());
                    sb.append("\n");
                }
            }
            Set<String> strings = map.keySet();
            for (String s : strings
            ) {
                Integer integer = map.get(s);
                infoSB.append(String.format("Ac %s,counts=%d", s, integer));
                infoSB.append("\n");
            }
        }
        Log.w("zjy", "ActivityRecoderDB->getRecorderStrByDate(): ==" + sb.toString());
        Log.w("zjy", "ActivityRecoderDB->getRecorderStrByDate(): infoSB==" + infoSB.toString());

        return sb.toString();
    }
    public List<RecordInfo> getMsgByDay(Date date) {
      SQLiteDatabase writableDatabase = getWritableDatabase();
//        Cursor query = writableDatabase.query(mTable, new String[]{"name", "time"}, "time between ? and ?",
//                new String[]{time1 + "", time2 + ""}
//                , null, null
//                , null);
        Cursor query = writableDatabase.query(mTable, new String[]{"id", "name", "time"}, null,
                null
                , null, null
                , null);
        List<RecordInfo> msgs = new ArrayList<>();

        if (query != null) {
            while (query.moveToNext()) {
                String name = getStringCol(query, "name");
                long time = getLongCol(query, "time");
                RecordInfo mINfo = new RecordInfo();
                mINfo.id = getIntCol(query, "id");
                mINfo.time = time;
                mINfo.name = name;
                msgs.add(mINfo);
            }
            query.close();
        } else {

        }
        //        String sql = "select *from " +
        //                "" + mTable +
        //                " where time<? and time>?";
        //        writableDatabase.execSQL(sql, new Object[]{time1, time2});
        return msgs;
    }
}
