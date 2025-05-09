package dsd.dsdvansales.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


import java.util.ArrayList;
import java.util.List;

import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.model.UploadFileInfo;

/**
 * Created by silve on 5/6/2017.
 */

public class UploadDbAdapter {

    private static final String TABLE_UPLOAD_LIST			= "UPLOAD_LIST_INFO";
    private static final String KEY_IDX						= "idx";
    private static final String KEY_LOCAL_PATH				= "LocalPath";
    private static final String KEY_SERVER_PATH				= "ServerPath";

    private static final String CREATE_TABLE_UPLOAD_LIST 	= "CREATE TABLE " 		+ TABLE_UPLOAD_LIST + " (" +
            KEY_IDX				+ " integer primary key autoincrement, " +
            KEY_LOCAL_PATH		+ " text, " +
            KEY_SERVER_PATH		+ " text);";

    private static final int	DATABASE_VERSION			= 28;
    private static final String DATABASE_MASTER_TABLE		= "sqlite_master";
    private static final String DATABASE_VANSALES_UPLOAD = "DSD_VANSALES_UPLOAD";

    public UploadDbAdapter(Context mContext) {
        this.mContext = mContext;
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        private SQLiteDatabase mMyDB = null;
        DatabaseHelper(Context mContext) {
            super(mContext, DATABASE_VANSALES_UPLOAD, null, DATABASE_VERSION);
            mMyDB = SQLiteDatabase.openDatabase(Global.ROOT_UPLOAD_DATABASE_LOCATION, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase.OPEN_READWRITE | SQLiteDatabase.CREATE_IF_NECESSARY);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_UPLOAD_LIST);

            onCreate(db);
        }

        public SQLiteDatabase getCustomDatabase()
        {
            return mMyDB;
        }

        @Override
        public void close()
        {
            super.close();
            mMyDB.close();
        }
    }


    private DatabaseHelper mDbHelper = null;
    private SQLiteDatabase mDb = null;
    private Context mContext = null;

    public UploadDbAdapter open() throws SQLException {
//        Global.gUploadProcessingDB.lock();
        mDbHelper = new UploadDbAdapter.DatabaseHelper(mContext);
        mDb = mDbHelper.getCustomDatabase();
        Cursor cursor;
        cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[] {"name"}, "name='" + TABLE_UPLOAD_LIST + "'", null, null, null, null, null);
        if (cursor.getCount() == 0) {
            mDb.execSQL(CREATE_TABLE_UPLOAD_LIST);
        }
        cursor.close();
        return this;
    }

    public void close() {
        mDbHelper.close();
//        Global.gUploadProcessingDB.unlock();
    }

    public void insertUploadFile(String strLocalPath, String strServerPath)
    {
        ContentValues values = new ContentValues();
        values.put(KEY_LOCAL_PATH, strLocalPath);
        values.put(KEY_SERVER_PATH, strServerPath);
        mDb.insert(TABLE_UPLOAD_LIST, null, values);
    }

    public UploadFileInfo getUploadFile()
    {
        String sql = "SELECT * FROM " + TABLE_UPLOAD_LIST + " LIMIT 1";
        Cursor cursor = mDb.rawQuery(sql, null);
        UploadFileInfo infos = null;
        try
        {
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                infos = new UploadFileInfo();
                infos.idx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
                infos.localPath = cursor.getString(cursor.getColumnIndex(KEY_LOCAL_PATH));
                infos.serverPath = cursor.getString(cursor.getColumnIndex(KEY_SERVER_PATH));
            }

        }catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return infos;
    }

    public List<UploadFileInfo> getUploadFileList()
    {
        String sql = "SELECT * FROM " + TABLE_UPLOAD_LIST;
        Cursor cursor = mDb.rawQuery(sql, null);
        UploadFileInfo info = null;
        List<UploadFileInfo> infos = new ArrayList<>();
        try
        {
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                for(int i = 0; i < cursor.getCount(); i++)
                {
                    info = new UploadFileInfo();
                    info.idx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
                    info.localPath = cursor.getString(cursor.getColumnIndex(KEY_LOCAL_PATH));
                    info.serverPath = cursor.getString(cursor.getColumnIndex(KEY_SERVER_PATH));
                    infos.add(info);
                    cursor.moveToNext();
                }

            }

        }catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return infos;
    }

    public void deleteUploadFile(int nIdx)
    {
        mDb.delete(TABLE_UPLOAD_LIST, KEY_IDX + "=" + nIdx, null);
    }

    public void deleteAllUploadFiles()
    {
        mDb.delete(TABLE_UPLOAD_LIST, null, null);
    }

}
