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
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.model.DownloadItem;


/**
 * Created by silve on 5/6/2017.
 */

public class DownloadDbAdapter {

    private static final String TABLE_INFO			        = "DOWNLOAD_INFO";
    private static final String KEY_IDX						= "idx";
    private static final String KEY_ITEM_NO 				= "ItemNo";
    private static final String KEY_URL 				    = "Url";

    private static final String CREATE_TABLE_DOWNLOAD 	= "CREATE TABLE " 		+ TABLE_INFO + " (" +
            KEY_IDX				+ " integer primary key autoincrement, " +
            KEY_ITEM_NO 		+ " text, " +
            KEY_URL             + " text);";

    private static final int	DATABASE_VERSION			= 1;
    private static final String DATABASE_MASTER_TABLE		= "sqlite_master";
    private static final String DATABASE_DSD_VANSALES_DOWNLOAD	= "DSDVANSALES_DOWNLOAD";

    public DownloadDbAdapter() {
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        private SQLiteDatabase mMyDB = null;
        DatabaseHelper(Context mContext) {
            super(mContext, DATABASE_DSD_VANSALES_DOWNLOAD, null, DATABASE_VERSION);
            Utils.createDirectory(Global.PRODUCT_DOWNLOAD_DIR);
            mMyDB = SQLiteDatabase.openDatabase(Global.ROOT_DOWNLOAD_DATABASE_LOCATION, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase.OPEN_READWRITE | SQLiteDatabase.CREATE_IF_NECESSARY);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            mMyDB.execSQL("DROP TABLE IF EXISTS " + TABLE_INFO);

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
    public DownloadDbAdapter open() throws SQLException {
//        Global.gUploadProcessingDB.lock();
        nOpenCnt++;
        if(mDbHelper == null)
        {
            mDbHelper = new DatabaseHelper(mContext);
        }
        mDb = mDbHelper.getCustomDatabase();
        Cursor cursor;
        cursor = mDb.query(true, DATABASE_MASTER_TABLE, new String[] {"name"}, "name='" + TABLE_INFO + "'", null, null, null, null, null);
        if (cursor.getCount() == 0) {
            mDb.execSQL(CREATE_TABLE_DOWNLOAD);
        }
        cursor.close();

        return this;
    }

    public void beginTransacion()
    {
        if( mDb != null )
        {
            mDb.beginTransaction();
        }
    }

    public void endTransaction()
    {
        if(mDb != null )
        {
            mDb.setTransactionSuccessful();
            mDb.endTransaction();
        }
    }

    public void update()
    {
        if(mDbHelper != null)
        {
            mDbHelper.onUpgrade(mDb, 0, 1);
        }
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

    public long addDownloadItem(String strItemNo, String strUrl)
    {
        ContentValues values = new ContentValues();
        values.put(KEY_ITEM_NO, strItemNo);
        values.put(KEY_URL, strUrl);
        return mDb.insert(TABLE_INFO, null, values);
    }

    public void deleteDownloadItem(String strItemNo)
    {
        mDb.delete(TABLE_INFO, KEY_ITEM_NO + " = '" + strItemNo + "'", null);
    }

    public void deleteAllDownloadItem()
    {
        mDb.delete(TABLE_INFO, null, null);
    }

    public List<DownloadItem> getAllDownloadItem()
    {
        String sql = "SELECT * FROM " + TABLE_INFO;
        Cursor cursor = mDb.rawQuery(sql, null);
        DownloadItem info = null;
        List<DownloadItem> infos = new ArrayList<>();
        try
        {
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                for(int i = 0; i < cursor.getCount(); i++)
                {
                    info = new DownloadItem();
                    info.strItemNo = cursor.getString(cursor.getColumnIndex(KEY_ITEM_NO));
                    info.strUrl = cursor.getString(cursor.getColumnIndex(KEY_URL));
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
}
