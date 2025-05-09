package dsd.dsdvansales.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.model.UploadFileInfo;

/**
 * Created by silve on 5/6/2017.
 */

public class EndTripDbAdapter {

    private static final String TABLE_END_TRIP  			= "END_TRIP_INFO";
    private static final String KEY_IDX						= "idx";
    private static final String KEY_TRIP_NUMBER				= "TripNumber";
    private static final String KEY_USER_NAME				= "UserName";
    private static final String KEY_PIN_NUMBER				= "PinNumber";
    private static final String KEY_STATUS                  = "Status";
    private static final String KEY_LOGIN_DATETIME          = "LoginDateTime";
    private static final String KEY_END_TRIP_DATETIME       = "EndTripDateTime";


    private static final String CREATE_TABLE_END_TRIP 	    = "CREATE TABLE " 		+ TABLE_END_TRIP + " (" +
                                KEY_IDX				+ " integer primary key autoincrement, " +
                                KEY_TRIP_NUMBER		+ " text, " +
                                KEY_USER_NAME		+ " text, " +
                                KEY_PIN_NUMBER      + " text, " +
                                KEY_STATUS          + " integer, " +
                                KEY_LOGIN_DATETIME  + " text, " +
                                KEY_END_TRIP_DATETIME + " text);";

    private static final int	DATABASE_VERSION			= 28;
    private static final String DATABASE_MASTER_TABLE		= "sqlite_master";
    private static final String DATABASE_VANSALES_UPLOAD = "DSD_APPLICATION_TRIP_STATUS";

    public EndTripDbAdapter(Context mContext) {
        this.mContext = mContext;
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        private SQLiteDatabase mMyDB = null;
        DatabaseHelper(Context mContext) {
            super(mContext, DATABASE_VANSALES_UPLOAD, null, DATABASE_VERSION);
            mMyDB = SQLiteDatabase.openDatabase(Global.ROOT_TRIP_STATUS_DATABASE_LOCATION, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase.OPEN_READWRITE | SQLiteDatabase.CREATE_IF_NECESSARY);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_END_TRIP);

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

    public EndTripDbAdapter open() throws SQLException {
//        Global.gUploadProcessingDB.lock();
        mDbHelper = new DatabaseHelper(mContext);
        mDb = mDbHelper.getCustomDatabase();
        Cursor cursor;
        cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[] {"name"}, "name='" + TABLE_END_TRIP + "'", null, null, null, null, null);
        if (cursor.getCount() == 0) {
            mDb.execSQL(CREATE_TABLE_END_TRIP);
        }
        cursor.close();
        return this;
    }

    public void close() {
        mDbHelper.close();
//        Global.gUploadProcessingDB.unlock();
    }

    public void insertTripData(String strTripNumber, String strPinNumber, String strUserName)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
        String strDateTime = sdf.format(new Date());
        ContentValues values = new ContentValues();
        values.put(KEY_TRIP_NUMBER, strTripNumber);
        values.put(KEY_PIN_NUMBER, strPinNumber);
        values.put(KEY_USER_NAME, strUserName);
        values.put(KEY_LOGIN_DATETIME, strDateTime);
        values.put(KEY_END_TRIP_DATETIME, "");
        values.put(KEY_STATUS, 0);

        mDb.insert(TABLE_END_TRIP, null, values);
    }

    public void updateTripData()
    {
        //Get Top Trip Data.
        String sql = "SELECT * FROM " + TABLE_END_TRIP + " ORDER BY " + KEY_IDX + " DESC LIMIT 1";
        Cursor cursor = mDb.rawQuery(sql, null);

        int idx = -1;
        try
        {
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                idx = cursor.getInt(cursor.getColumnIndex(KEY_IDX));
            }

        }catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        if( idx != -1 )
        {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
            String strDateTime = sdf.format(new Date());
            ContentValues values = new ContentValues();
            values.put(KEY_END_TRIP_DATETIME, strDateTime);
            values.put(KEY_STATUS, 1);
            mDb.update(TABLE_END_TRIP, values, KEY_IDX + " = " + idx, null);
        }

    }

}
