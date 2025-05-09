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

/**
 * Created by silve on 5/6/2017.
 */

public class GPSRecordDbAdapter {

    private static final String TABLE_UPLOAD_LIST			= "UPLOAD_LIST_INFO";
    private static final String KEY_IDX						= "idx";
    private static final String KEY_TIME					= "GPS_TIME";


    private static final String CREATE_TABLE_UPLOAD_LIST 	= "CREATE TABLE " 		+ TABLE_UPLOAD_LIST + " (" +
            KEY_IDX				+ " integer primary key autoincrement, " +
            KEY_TIME            + " long);";

    private static final int	DATABASE_VERSION			= 28;
    private static final String DATABASE_MASTER_TABLE		= "sqlite_master";
    private static final String DATABASE_DSD_DELIVERY		= "DSD_DELIVERY_UPLOAD";

    public GPSRecordDbAdapter(Context mContext) {
        this.mContext = mContext;
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        private SQLiteDatabase mMyDB = null;
        DatabaseHelper(Context mContext) {
            super(mContext, DATABASE_DSD_DELIVERY, null, DATABASE_VERSION);
            mMyDB = SQLiteDatabase.openDatabase(Global.ROOT_GPS_RECORD_DATABASE_LOCATION, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase.OPEN_READWRITE | SQLiteDatabase.CREATE_IF_NECESSARY);
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


    private static DatabaseHelper mDbHelper = null;
    private SQLiteDatabase mDb = null;
    private Context mContext = null;
    private static int nOpenCnt = 0;
    public GPSRecordDbAdapter open() throws SQLException {
        nOpenCnt++;
        if(mDbHelper == null)
        {
            mDbHelper = new GPSRecordDbAdapter.DatabaseHelper(mContext);
        }
        mDb = mDbHelper.getCustomDatabase();
        Cursor cursor;
        try
        {
            cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[] {"name"}, "name='" + TABLE_UPLOAD_LIST + "'", null, null, null, null, null);
            if (cursor.getCount() == 0) {
                mDb.execSQL(CREATE_TABLE_UPLOAD_LIST);
            }
            cursor.close();
        }catch (Exception e)
        {
            e.printStackTrace();
        }

        return this;
    }

    public void close() {
        nOpenCnt--;
        if(nOpenCnt == 0)
        {
            try
            {
                if(mDbHelper != null)
                {
                    mDbHelper.close();
                }
            }catch (Exception e)
            {
                e.printStackTrace();
            }
            mDbHelper = null;
        }
//        Global.gUploadProcessingDB.unlock();
    }

    public void insertTime(long time)
    {
        ContentValues values = new ContentValues();
        values.put(KEY_TIME, time);
        mDb.insert(TABLE_UPLOAD_LIST, null, values);
    }

    public List<Long> getRecordTimeList(long startTime, long endTime)
    {
        List<Long> infos = new ArrayList<>();
        Cursor cursor = null;
        try
        {
            String sql = "SELECT * FROM " + TABLE_UPLOAD_LIST + " WHERE " + KEY_TIME + " >= " + startTime + " AND " + KEY_TIME + " <= " + endTime;
            cursor = mDb.rawQuery(sql, null);

            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                for(int i = 0; i < cursor.getCount(); i++)
                {
                    long time = cursor.getLong(cursor.getColumnIndex(KEY_TIME));
                    infos.add(time);
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

    public void deleteRecordTime(int nIdx)
    {
        mDb.delete(TABLE_UPLOAD_LIST, KEY_IDX + "=" + nIdx, null);
    }

    public void deleteAllRecordTime()
    {
        mDb.delete(TABLE_UPLOAD_LIST, null, null);
    }

}
