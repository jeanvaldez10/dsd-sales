package dsd.dsdvansales.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.model.MakeZipFileInfo;
import dsd.dsdvansales.model.UploadInfo;

/**
 * Created by silve on 5/6/2017.
 */

public class LoginDbAdapter {

    private static final String TABLE_LOGIN_INFO			= "UPLOAD_LIST_INFO";
    private static final String KEY_IDX						= "idx";
    private static final String KEY_USER_NAME 				= "UserName";
    private static final String KEY_TRIP_NUMBER				= "TripNumber";
    private static final String KEY_LOGIN_DATE				= "LoginDate";
    private static final String KEY_STATUS  				= "Status"; // 1 : Login, 2 : End Trip, 3 : Logout  ---- We will use case 1 & 2.

    private static final String CREATE_TABLE_LOGIN_INFO 	= "CREATE TABLE " 		+ TABLE_LOGIN_INFO + " (" +
            KEY_IDX				+ " integer primary key autoincrement, " +
            KEY_USER_NAME 		+ " text, " +
            KEY_TRIP_NUMBER     + " text, " +
            KEY_LOGIN_DATE      + " text, " +
            KEY_STATUS          + " integer);";

    private static final int	DATABASE_VERSION			= 1;
    private static final String DATABASE_MASTER_TABLE		= "sqlite_master";
    private static final String DATABASE_DSD_VANSALES_LOGIN	= "DSDVANSALES_LOGIN";

    public LoginDbAdapter(Context mContext) {
        this.mContext = mContext;
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        private SQLiteDatabase mMyDB = null;
        DatabaseHelper(Context mContext) {
            super(mContext, DATABASE_DSD_VANSALES_LOGIN, null, DATABASE_VERSION);
            Utils.createDirectory(Global.UPLOAD_DIR);
            mMyDB = SQLiteDatabase.openDatabase(Global.ROOT_LOGIN_LOG_DATABASE_LOCATION, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase.OPEN_READWRITE | SQLiteDatabase.CREATE_IF_NECESSARY);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_LOGIN_INFO);

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
    public LoginDbAdapter open() throws SQLException {
//        Global.gUploadProcessingDB.lock();
        nOpenCnt++;
        if(mDbHelper == null)
        {
            mDbHelper = new LoginDbAdapter.DatabaseHelper(mContext);
        }
        mDb = mDbHelper.getCustomDatabase();
        Cursor cursor;
        cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[] {"name"}, "name='" + TABLE_LOGIN_INFO + "'", null, null, null, null, null);
        if (cursor.getCount() == 0) {
            mDb.execSQL(CREATE_TABLE_LOGIN_INFO);
        }
        cursor.close();

        return this;
    }

    public void close() {
        nOpenCnt--;
        if(nOpenCnt <= 0)
        {
            mDbHelper.close();
            mDbHelper = null;
        }
//        Global.gUploadProcessingDB.unlock();
    }

    public long registerLogin(String strUserName, String strTrip, int nStatus)
    {
        ContentValues values = new ContentValues();
        values.put(KEY_USER_NAME, strUserName);
        values.put(KEY_TRIP_NUMBER, strTrip);
        values.put(KEY_LOGIN_DATE, new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        values.put(KEY_STATUS, nStatus);
        return mDb.insert(TABLE_LOGIN_INFO, null, values);
    }

    public int getLoginCount(String strDate, String strUserName, String strTrip, int nStatus)
    {
        String sql = "SELECT * FROM " + TABLE_LOGIN_INFO + " WHERE " + KEY_LOGIN_DATE + " = '" + strDate + "' AND " + KEY_USER_NAME + " = '" + strUserName + "' AND " + KEY_TRIP_NUMBER + " = '" + strTrip + "' AND " + KEY_STATUS + " = " + nStatus;
        Cursor cursor = mDb.rawQuery(sql, null);
        int nCount = 0;
        try
        {
            if (cursor != null && cursor.getCount() > 0) {
                nCount = cursor.getCount();
            }

        }catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return nCount;
    }

    public int getLoginCount(String strDate, String strTrip, int nStatus)
    {
        String sql = "SELECT * FROM " + TABLE_LOGIN_INFO + " WHERE " + KEY_LOGIN_DATE + " = '" + strDate + "' AND " + KEY_TRIP_NUMBER + " = '" + strTrip + "' AND " + KEY_STATUS + " = " + nStatus;
        Cursor cursor = mDb.rawQuery(sql, null);
        int nCount = 0;
        try
        {
            if (cursor != null && cursor.getCount() > 0) {
                nCount = cursor.getCount();
            }

        }catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return nCount;
    }
}
