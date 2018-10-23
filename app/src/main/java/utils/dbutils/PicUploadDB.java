package utils.dbutils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.b1b.js.erpandroid_kf.entity.PicUploadInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 张建宇 on 2019/3/28.
 */
public class PicUploadDB extends SQLiteOpenHelper {

    public static final String name = "picupload.db";
    public static final String table = "uploadRecorder";

    public static final int VERSION = 1;

    public PicUploadDB(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public PicUploadDB(Context context) {
        super(context, name, null, VERSION);
        mDb = getWritableDatabase();
    }

    private SQLiteDatabase mDb;

    // pid, remoteName + ".jpg"      , insertPath, "CKTZ"
    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table if not exists uploadRecorder (localfilepath text primary key ," +
                "pid text, " +
                "remotepath text," +
                "cid text," +
                "did text," +
                "loginID text," +
                "remoteName text," +
                "insertPath text," +
                "ftpurl text," +
                "okcount tinyint," +
                "uid text," +
                "tag text" +
                ")";
        db.execSQL(sql);
        Log.e("zjy", "PicUploadDB->onCreate(): OncreateDb==" + db);
    }

    public int deleteData(String localPath) {

        try {
            long data = mDb.delete(table, "localfilepath=?", new String[]{localPath});
            return (int) data;
        } catch (Exception e) {
            Log.e("zjy", "PicUploadDB->getRecode(): Exception==" + e);
        } finally {
        }
        return -1;
    }

    public int insertRecord(PicUploadInfo info) {
        ContentValues values = new ContentValues();
        values.put("cid", info.cid);
        values.put("did", info.did);
        values.put("loginID", info.loginID);
        values.put("localfilepath", info.localfilepath);
        values.put("remotepath", info.remotepath);
        values.put("remoteName", info.remoteName);
        values.put("insertPath", info.insertPath);
        values.put("pid", info.pid);
        values.put("tag", info.tag);
        values.put("okcount", info.okcount);
        values.put("ftpurl", info.ftpurl);
        values.put("uid", info.uid);
        try {
            long data = mDb.insert(table, null, values);
            return (int) data;
        } catch (Exception e) {
            Log.e("zjy", "PicUploadDB->getRecode(): Exception==" + e);
        } finally {
        }
        return -1;
    }

    public List<PicUploadInfo> getAllRecoder() {
        String sql = "";
        List<PicUploadInfo> infos = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = mDb.query(table, null, null, null, null, null, null
            );
            while (cursor.moveToNext()) {
                String cid = cursor.getString(cursor.getColumnIndex("cid"));
                String did = cursor.getString(cursor.getColumnIndex("did"));
                String loginID = cursor.getString(cursor.getColumnIndex("loginID"));
                String localfilepath = cursor.getString(cursor.getColumnIndex("localfilepath"));
                String remoteName = cursor.getString(cursor.getColumnIndex("remoteName"));
                String remotepath = cursor.getString(cursor.getColumnIndex("remotepath"));
                String insertPath = cursor.getString(cursor.getColumnIndex("insertPath"));
                String spid = cursor.getString(cursor.getColumnIndex("pid"));
                String tag = cursor.getString(cursor.getColumnIndex("tag"));
                String ftpurl = cursor.getString(cursor.getColumnIndex("ftpurl"));
                String uid = cursor.getString(cursor.getColumnIndex("uid"));
                int okcount = cursor.getInt(cursor.getColumnIndex("okcount"));
                PicUploadInfo info = new PicUploadInfo(localfilepath, spid, tag, remotepath, cid, did,
                        loginID, remoteName, insertPath);
                info.okcount = okcount;
                info.ftpurl = ftpurl;
                info.uid = uid;
                infos.add(info);
            }
        } catch (Exception e) {
            Log.e("zjy", "PicUploadDB->getAllRecoder(): Exception==" + e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return infos;
    }

    public List<PicUploadInfo> getRecode(String pid) {
        String sql = "select * from uploadRecorder where pid=?";
        List<PicUploadInfo> infos = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = mDb.rawQuery(sql, new String[]{pid});
            while (cursor.moveToNext()) {
                //                values.put("pid", info.pid);
                //                values.put("tag", info.tag);
                String cid = cursor.getString(cursor.getColumnIndex("cid"));
                String did = cursor.getString(cursor.getColumnIndex("did"));
                String loginID = cursor.getString(cursor.getColumnIndex("loginID"));
                String localfilepath = cursor.getString(cursor.getColumnIndex("localfilepath"));
                String remoteName = cursor.getString(cursor.getColumnIndex("remoteName"));
                String remotepath = cursor.getString(cursor.getColumnIndex("remotepath"));
                String insertPath = cursor.getString(cursor.getColumnIndex("insertPath"));
                String spid = cursor.getString(cursor.getColumnIndex("pid"));
                String tag = cursor.getString(cursor.getColumnIndex("tag"));
                int okcount = cursor.getInt(cursor.getColumnIndex("okcount"));
                PicUploadInfo info = new PicUploadInfo(localfilepath, spid, tag, remotepath, cid, did,
                        loginID, remoteName, insertPath);
                info.okcount = okcount;
                infos.add(info);
            }
        } catch (Exception e) {
            Log.e("zjy", "PicUploadDB->getRecode(): Exception==" + e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return infos;
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
