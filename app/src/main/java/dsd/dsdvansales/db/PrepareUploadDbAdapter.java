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
import dsd.dsdvansales.model.MakeZipFileInfo;
import dsd.dsdvansales.model.UploadInfo;

/**
 * Created by silve on 5/6/2017.
 */

public class PrepareUploadDbAdapter {

    private static final String TABLE_UPLOAD_LIST			= "UPLOAD_LIST_INFO";
    private static final String KEY_IDX						= "idx";
    private static final String KEY_CONTENT 				= "Content";

    private static final String CREATE_TABLE_UPLOAD_LIST 	= "CREATE TABLE " 		+ TABLE_UPLOAD_LIST + " (" +
            KEY_IDX				+ " integer primary key autoincrement, " +
            KEY_CONTENT 		+ " text);";

    private static final int	DATABASE_VERSION			= 28;
    private static final String DATABASE_MASTER_TABLE		= "sqlite_master";
    private static final String DATABASE_DSD_DELIVERY		= "DSD_DELIVERY_UPLOAD";

    public PrepareUploadDbAdapter(Context mContext) {
        this.mContext = mContext;
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        private SQLiteDatabase mMyDB = null;
        DatabaseHelper(Context mContext) {
            super(mContext, DATABASE_DSD_DELIVERY, null, DATABASE_VERSION);
            mMyDB = SQLiteDatabase.openDatabase(Global.ROOT_PREPARE_UPDATE_DATABASE_LOCATION, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase.OPEN_READWRITE | SQLiteDatabase.CREATE_IF_NECESSARY);
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
    private static SQLiteDatabase mDb = null;
    private Context mContext = null;
    public PrepareUploadDbAdapter open() throws SQLException {

        while(mDb == null) {
            try {
                mDbHelper = new DatabaseHelper(mContext);
                mDb = mDbHelper.getCustomDatabase();
                Cursor cursor;

                cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[] {"name"}, "name='" + TABLE_UPLOAD_LIST + "'", null, null, null, null, null);
                if (cursor.getCount() == 0) {
                    mDb.execSQL(CREATE_TABLE_UPLOAD_LIST);
                }
                cursor.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return this;
    }

    public void close() {
        /*mDbHelper.close();
        mDbHelper = null;*/
    }

    public void closeAll()
    {
        if(mDbHelper != null)
        {
            mDbHelper.close();
            mDb = null;
            mDbHelper = null;
        }
    }

    public long insertUploadFile(MakeZipFileInfo info)
    {
        ContentValues values = new ContentValues();
        values.put(KEY_CONTENT, info.toJSON());
        return mDb.insert(TABLE_UPLOAD_LIST, null, values);
    }

    public List<UploadInfo> getMakeZipFileInfoList()
    {
        String sql = "SELECT * FROM " + TABLE_UPLOAD_LIST;
        Cursor cursor = mDb.rawQuery(sql, null);
        List<UploadInfo> infoList = new ArrayList<>();
        try
        {
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                for(int i = 0; i < cursor.getCount(); i++)
                {
                    UploadInfo uploadInfo = new UploadInfo();
                    uploadInfo.idx = cursor.getLong(cursor.getColumnIndex(KEY_IDX));
                    uploadInfo.makeZipFileInfo = MakeZipFileInfo.fromJSON(cursor.getString(cursor.getColumnIndex(KEY_CONTENT)));
                    infoList.add(uploadInfo);
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
        return infoList;
    }

    public UploadInfo getMakeZipFileInfo(long idx)
    {
        String sql = "SELECT * FROM " + TABLE_UPLOAD_LIST + " WHERE " + KEY_IDX + " = " + idx;
        Cursor cursor = mDb.rawQuery(sql, null);
        UploadInfo info = null;
        try
        {
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                info = new UploadInfo();
                info.idx = cursor.getLong(cursor.getColumnIndex(KEY_IDX));
                info.makeZipFileInfo = MakeZipFileInfo.fromJSON(cursor.getString(cursor.getColumnIndex(KEY_CONTENT)));
                cursor.moveToNext();
            }
        }catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return info;
    }

    public void deleteUploadFileInfo(long idx)
    {
        mDb.delete(TABLE_UPLOAD_LIST, KEY_IDX + "=" + idx, null);
    }

    public void deleteAllUploadFiles()
    {
        mDb.delete(TABLE_UPLOAD_LIST, null, null);
    }

}
