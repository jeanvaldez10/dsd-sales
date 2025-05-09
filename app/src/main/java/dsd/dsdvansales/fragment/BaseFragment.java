package dsd.dsdvansales.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import dsd.dsdvansales.activities.MainActivity;
import dsd.dsdvansales.db.DbAdapter;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.model.MakeZipFileInfo;
import dsd.dsdvansales.model.ProdLocnInfo;
import dsd.dsdvansales.model.UCamerasInfo;
import dsd.dsdvansales.model.UFileTransactionInfo;
import dsd.dsdvansales.model.UGPSInfo;
import dsd.dsdvansales.model.UTransactionsInfo;

public abstract class BaseFragment extends Fragment {

	private static final int	RESULT_TAKE_PHOTO		= 100;
	private static final int	RESULT_FROM_GALLERY		= 101;
	public ProgressDialog mProgressDialog;

	public abstract void initView();
	public abstract void loadData();
	public abstract void refreshPage();
	public abstract void refreshLanguage();
	public abstract void receiveBroadcast(String strAction, Intent intent);

	public MainActivity mContext = null;
	public View mRootView = null;

	private EventReceiver mEventReceiver = null;
	
	public long m_lTrxnNo;
	public Date mCurDate;

	private String mCurrentPhotoPath = "";

	public MakeZipFileInfo makeZipFileInfo = null;
	public void commonInit() {
		mContext = (MainActivity) getActivity();

		unregisterReceiver();
		mEventReceiver = new EventReceiver();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			mContext.registerReceiver(mEventReceiver, new IntentFilter(Global.ACTION_REFRESH_LANGUAGE), Context.RECEIVER_EXPORTED);
			mContext.registerReceiver(mEventReceiver, new IntentFilter(Global.ACTION_SELECT_PRODUCT), Context.RECEIVER_EXPORTED);
			mContext.registerReceiver(mEventReceiver, new IntentFilter(Global.ACTION_FILTER_LOCATION), Context.RECEIVER_EXPORTED);
			mContext.registerReceiver(mEventReceiver, new IntentFilter(Global.ACTION_FILTER_BY), Context.RECEIVER_EXPORTED);
			mContext.registerReceiver(mEventReceiver, new IntentFilter(Global.ACTION_CAPTURE_SIGNATURE), Context.RECEIVER_EXPORTED);
			mContext.registerReceiver(mEventReceiver, new IntentFilter(Global.ACTION_REFRESH_CUSTOMER_LIST), Context.RECEIVER_EXPORTED);
			mContext.registerReceiver(mEventReceiver, new IntentFilter(Global.ACTION_REFRESH_CUSTOMER_DETAIL), Context.RECEIVER_EXPORTED);
			mContext.registerReceiver(mEventReceiver, new IntentFilter(Global.ACTION_MAP_REFRESH_CUSTOMER_LIST), Context.RECEIVER_EXPORTED);
			mContext.registerReceiver(mEventReceiver, new IntentFilter(Global.ACTION_ORDER_HISTORY), Context.RECEIVER_EXPORTED);
			mContext.registerReceiver(mEventReceiver, new IntentFilter(Global.ACTION_REFRESH_STOCK_VALUE), Context.RECEIVER_EXPORTED);
			mContext.registerReceiver(mEventReceiver, new IntentFilter(Global.ACTION_CAPTURE_SIGNATURE_EXIT), Context.RECEIVER_EXPORTED);
			mContext.registerReceiver(mEventReceiver, new IntentFilter(Global.ACTION_ADDITIONAL_DOWNLOAD_FINISHED), Context.RECEIVER_EXPORTED);
		} else {
			mContext.registerReceiver(mEventReceiver, new IntentFilter(Global.ACTION_REFRESH_LANGUAGE));
			mContext.registerReceiver(mEventReceiver, new IntentFilter(Global.ACTION_SELECT_PRODUCT));
			mContext.registerReceiver(mEventReceiver, new IntentFilter(Global.ACTION_FILTER_LOCATION));
			mContext.registerReceiver(mEventReceiver, new IntentFilter(Global.ACTION_FILTER_BY));
			mContext.registerReceiver(mEventReceiver, new IntentFilter(Global.ACTION_CAPTURE_SIGNATURE));
			mContext.registerReceiver(mEventReceiver, new IntentFilter(Global.ACTION_REFRESH_CUSTOMER_LIST));
			mContext.registerReceiver(mEventReceiver, new IntentFilter(Global.ACTION_REFRESH_CUSTOMER_DETAIL));
			mContext.registerReceiver(mEventReceiver, new IntentFilter(Global.ACTION_MAP_REFRESH_CUSTOMER_LIST));
			mContext.registerReceiver(mEventReceiver, new IntentFilter(Global.ACTION_ORDER_HISTORY));
			mContext.registerReceiver(mEventReceiver, new IntentFilter(Global.ACTION_REFRESH_STOCK_VALUE));
			mContext.registerReceiver(mEventReceiver, new IntentFilter(Global.ACTION_CAPTURE_SIGNATURE_EXIT));
			mContext.registerReceiver(mEventReceiver, new IntentFilter(Global.ACTION_ADDITIONAL_DOWNLOAD_FINISHED));
		}


		Global .LANG = Utils.getIntSetting(mContext, Global.PREF_CUR_LANG);
		Global.gTripNumber = Utils.getStringSetting(mContext, Global.PREF_TRIP_NUMBER);
		Global.gRouteNumber = Utils.getStringSetting(mContext, Global.PREF_ROUTE_NUMBER);
		Global.gCurrencySymbol = Utils.getStringSetting(mContext, Global.PREF_CURRENCY_SYMBOL);
		Global.MULTIPLE_VALUE = 100000 / (int)(Math.pow(10, Utils.getIntSetting(mContext, Global.PREF_SCALING)));
		initView();
	}

	public void showKeyboard(EditText editText, boolean bShow) {
		InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (bShow == true) {
			editText.requestFocus();
			imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
			editText.setSelection(editText.getText().toString().length());
		} else {
			imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
		}
	}

	public String getOrderNo(){
		String strOrderNo = "";
		String strPattern = Utils.getStringSetting(mContext, Global.PREF_INVOICE_NUM_FORMAT);
		String strKK = Utils.getStringSetting(mContext, Global.PREF_KK_VALUE);

		int nPdfSequenceNo = Utils.getIntSetting(mContext, String.format(Locale.ENGLISH, Global.PREF_PDF_SEQUENCE_NO, Global.gTripNumber)) + 1;
		Utils.setIntSetting(mContext, String.format(Locale.ENGLISH, Global.PREF_PDF_SEQUENCE_NO, Global.gTripNumber), nPdfSequenceNo);
		String strRRR = Global.gRouteNumber;
		Date tripDate = Utils.getRoutectlTripDate(mContext, new Date());
		String strY = new SimpleDateFormat("yyyy").format(tripDate);
		Calendar cal = Calendar.getInstance();
		cal.setTime(tripDate);
		int nDayOfYear = cal.get(Calendar.DAY_OF_YEAR);
		String strJJJ = String.format(Locale.ENGLISH, "%d", nDayOfYear);
		String strSSS = String.format(Locale.ENGLISH, "%d", nPdfSequenceNo);

		strOrderNo = "";

		int nRIdx = 0;
		int nYIdx = 0;
		int nJIdx = 0;
		int nSIdx = 0;
		int nKIdx = 0;

		int nIdx;
		for(int i = strPattern.length() - 1; i >=0; i--)
		{
			char c = strPattern.charAt(i);
			if( c == 'R' )
			{
				nIdx = strRRR.length() - nRIdx - 1;
				if( nIdx >= 0 )
				{
					strOrderNo = strRRR.substring(nIdx, nIdx + 1) + strOrderNo;
				}
				else
				{
					strOrderNo = "0" + strOrderNo;
				}
				nRIdx++;
			}
			else if( c == 'Y' )
			{
				nIdx = strY.length() - nYIdx - 1;
				if( nIdx >= 0 )
				{
					strOrderNo = strY.substring(nIdx, nIdx + 1) + strOrderNo ;
				}
				else
				{
					strOrderNo = "0" + strOrderNo;
				}
				nYIdx++;
			}
			else if( c == 'J' )
			{
				nIdx = strJJJ.length() - nJIdx - 1;
				if( nIdx >= 0 )
				{
					strOrderNo = strJJJ.substring(nIdx, nIdx + 1) + strOrderNo;
				}
				else
				{
					strOrderNo = "0" + strOrderNo;
				}
				nJIdx++;
			}
			else if( c == 'S' )
			{
				nIdx = strSSS.length() - nSIdx - 1;
				if( nIdx >= 0 )
				{
					strOrderNo = strSSS.substring(nIdx, nIdx + 1) + strOrderNo;
				}
				else
				{
					strOrderNo = "0" + strOrderNo;
				}
				nSIdx++;
			}
			else if( c == 'K')
			{
				nIdx = strKK.length() - nKIdx - 1;
				if( nIdx >= 0 )
				{
					strOrderNo = strKK.substring(nIdx, nIdx + 1) + strOrderNo;
				}
				else
				{
					strOrderNo = "0" + strOrderNo;
				}
				nKIdx++;
			}
		}
		long lastDocNo = Utils.getLongSetting(mContext, Global.PREF_LAST_DOC_NO);
		if(lastDocNo > 0)
		{
			try {
				long curDocNo = Utils.parseLongFromString(strOrderNo);
				if(curDocNo < lastDocNo)
				{
					strOrderNo = String.valueOf(lastDocNo);
					lastDocNo++;
					Utils.setLongSetting(mContext, Global.PREF_LAST_DOC_NO, lastDocNo);
				}
			}catch (Exception e)
			{

			}
		}
		return strOrderNo;
	}
	
	public String getPDFFileName() {
		String strFileName = getOrderNo() + ".pdf";
		return strFileName;
	}

	public void initMakeZipInfo()
	{
		if( makeZipFileInfo != null ) {
			makeZipFileInfo.trxnList.clear();
			makeZipFileInfo = null;
		}
	}

	public long setGeneralUploadData(String strDocType, int nChainNo, int nCustNo, long nTrxnNo) {
//		double nLat = Utils.getDoubleSetting(mContext, Global.PREF_MY_LATITUDE);
//		double nLng = Utils.getDoubleSetting(mContext, Global.PREF_MY_LONGITUDE);

		if( nTrxnNo != -1 )
			m_lTrxnNo = nTrxnNo;
		else
			m_lTrxnNo = System.currentTimeMillis();
		mCurDate = new Date(m_lTrxnNo);
		UTransactionsInfo uTransactionInfo = new UTransactionsInfo();
		uTransactionInfo.nTrxnNo = m_lTrxnNo;
		uTransactionInfo.strDocType = strDocType;
		uTransactionInfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(mContext, mCurDate));
		uTransactionInfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(mCurDate);
		uTransactionInfo.nChainNo = nChainNo;
		uTransactionInfo.nCustNo = nCustNo;

		/*UGPSInfo uGPSInfo = new UGPSInfo();
		uGPSInfo.nTrxnNo = m_lTrxnNo;
		uGPSInfo.nChainNo = nChainNo;
		uGPSInfo.nCustNo = nCustNo;
		uGPSInfo.nStartTrxnNo = uTransactionInfo.nTrxnNo;
		if (nLat != Global.DEFAULT_FLOAT && nLng != Global.DEFAULT_FLOAT) {
			uGPSInfo.strGPSData = String.format(Locale.ENGLISH, "%f, %f", nLat, nLng);
		} else {
			uGPSInfo.strGPSData = "ERROR: NO GPS";
		}
		if(strDocType.equals("MAIT"))
			uGPSInfo.strDocType = "MAIT";
		else
			uGPSInfo.strDocType = "GPS";
		uGPSInfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(mCurDate);
		uGPSInfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(mCurDate);*/
		uTransactionInfo.strTripId = Utils.getStringSetting(mContext, Global.PREF_TRIP_ID);
		uTransactionInfo.strDriverId = "";
		uTransactionInfo.strDriverName = Utils.getStringSetting(mContext, Global.PREF_USER_DESCRIPTION);
		uTransactionInfo.strDriverNumber = Utils.getStringSetting(mContext, Global.PREF_DRIVER_NUMBER);

		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		dbAdapter.insertUTransactionsInfo(uTransactionInfo);
//		dbAdapter.insertUGPSInfo(uGPSInfo);
		dbAdapter.close();
		AddTrxnNo(nCustNo, nChainNo, m_lTrxnNo);
		return m_lTrxnNo;
	}

	public long setGeneralUploadData(String strDocType, int nChainNo, int nCustNo, long nTrxnNo, Date curDate) {
//		double nLat = Utils.getDoubleSetting(mContext, Global.PREF_MY_LATITUDE);
//		double nLng = Utils.getDoubleSetting(mContext, Global.PREF_MY_LONGITUDE);

		if( nTrxnNo != -1 )
			m_lTrxnNo = nTrxnNo;
		else
			m_lTrxnNo = System.currentTimeMillis();
		mCurDate = curDate;
		UTransactionsInfo uTransactionInfo = new UTransactionsInfo();
		uTransactionInfo.nTrxnNo = m_lTrxnNo;
		uTransactionInfo.strDocType = strDocType;
		uTransactionInfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(mContext, mCurDate));
		uTransactionInfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(mCurDate);
		uTransactionInfo.nChainNo = nChainNo;
		uTransactionInfo.nCustNo = nCustNo;

		/*UGPSInfo uGPSInfo = new UGPSInfo();
		uGPSInfo.nTrxnNo = m_lTrxnNo;
		uGPSInfo.nChainNo = nChainNo;
		uGPSInfo.nCustNo = nCustNo;
		uGPSInfo.nStartTrxnNo = uTransactionInfo.nTrxnNo;
		if (nLat != Global.DEFAULT_FLOAT && nLng != Global.DEFAULT_FLOAT) {
			uGPSInfo.strGPSData = String.format(Locale.ENGLISH, "%f, %f", nLat, nLng);
		} else {
			uGPSInfo.strGPSData = "ERROR: NO GPS";
		}
		if(strDocType.equals("MAIT"))
			uGPSInfo.strDocType = "MAIT";
		else
			uGPSInfo.strDocType = "GPS";
		uGPSInfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(mCurDate);
		uGPSInfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(mCurDate);*/

		uTransactionInfo.strTripId = Utils.getStringSetting(mContext, Global.PREF_TRIP_ID);
		uTransactionInfo.strDriverId = "";
		uTransactionInfo.strDriverName = Utils.getStringSetting(mContext, Global.PREF_USER_DESCRIPTION);
		uTransactionInfo.strDriverNumber = Utils.getStringSetting(mContext, Global.PREF_DRIVER_NUMBER);

		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		dbAdapter.insertUTransactionsInfo(uTransactionInfo);
//		dbAdapter.insertUGPSInfo(uGPSInfo);
		dbAdapter.close();
		AddTrxnNo(nCustNo, nChainNo, m_lTrxnNo);
		return m_lTrxnNo;
	}

	public long setGeneralUploadData(String strDocType, int nChainNo, int nCustNo, long nTrxnNo, String strReference) {
//		double nLat = Utils.getDoubleSetting(mContext, Global.PREF_MY_LATITUDE);
//		double nLng = Utils.getDoubleSetting(mContext, Global.PREF_MY_LONGITUDE);

		if( nTrxnNo != -1 )
			m_lTrxnNo = nTrxnNo;
		else
			m_lTrxnNo = System.currentTimeMillis();
		mCurDate = new Date(m_lTrxnNo);
		UTransactionsInfo uTransactionInfo = new UTransactionsInfo();
		uTransactionInfo.nTrxnNo = m_lTrxnNo;
		uTransactionInfo.strDocType = strDocType;
		uTransactionInfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(mContext, mCurDate));
		uTransactionInfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(mCurDate);
		uTransactionInfo.nChainNo = nChainNo;
		uTransactionInfo.strReference = strReference;
		uTransactionInfo.nCustNo = nCustNo;

/*		UGPSInfo uGPSInfo = new UGPSInfo();
		uGPSInfo.nTrxnNo = m_lTrxnNo;
		uGPSInfo.nChainNo = nChainNo;
		uGPSInfo.nCustNo = nCustNo;
		uGPSInfo.nStartTrxnNo = uTransactionInfo.nTrxnNo;
		if (nLat != Global.DEFAULT_FLOAT && nLng != Global.DEFAULT_FLOAT) {
			uGPSInfo.strGPSData = String.format(Locale.ENGLISH, "%f, %f", nLat, nLng);
		} else {
			uGPSInfo.strGPSData = "ERROR: NO GPS";
		}
		if(strDocType.equals("MAIT"))
			uGPSInfo.strDocType = "MAIT";
		else
			uGPSInfo.strDocType = "GPS";
		uGPSInfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(mCurDate);
		uGPSInfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(mCurDate);*/

		uTransactionInfo.strTripId = Utils.getStringSetting(mContext, Global.PREF_TRIP_ID);
		uTransactionInfo.strDriverId = "";
		uTransactionInfo.strDriverName = Utils.getStringSetting(mContext, Global.PREF_USER_DESCRIPTION);
		uTransactionInfo.strDriverNumber = Utils.getStringSetting(mContext, Global.PREF_DRIVER_NUMBER);

		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		dbAdapter.insertUTransactionsInfo(uTransactionInfo);
//		dbAdapter.insertUGPSInfo(uGPSInfo);
		dbAdapter.close();

		AddTrxnNo(nCustNo, nChainNo, m_lTrxnNo);

		return m_lTrxnNo;
	}

	public long setGeneralUploadData(String strDocType, int nChainNo, int nCustNo, long nTrxnNo, String strReference, String strDocNo) {
//		double nLat = Utils.getDoubleSetting(mContext, Global.PREF_MY_LATITUDE);
//		double nLng = Utils.getDoubleSetting(mContext, Global.PREF_MY_LONGITUDE);

		if( nTrxnNo != -1 )
			m_lTrxnNo = nTrxnNo;
		else
			m_lTrxnNo = System.currentTimeMillis();

		mCurDate = new Date(m_lTrxnNo);

		UTransactionsInfo uTransactionInfo = new UTransactionsInfo();
		uTransactionInfo.nTrxnNo = m_lTrxnNo;
		uTransactionInfo.strDocType = strDocType;
		uTransactionInfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(mContext, mCurDate));
		uTransactionInfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(mCurDate);
		uTransactionInfo.nChainNo = nChainNo;
		uTransactionInfo.strReference = strReference;
		uTransactionInfo.nCustNo = nCustNo;
		uTransactionInfo.strDocNo = strDocNo;

/*		UGPSInfo uGPSInfo = new UGPSInfo();
		uGPSInfo.nTrxnNo = m_lTrxnNo;
		uGPSInfo.nChainNo = nChainNo;
		uGPSInfo.nCustNo = nCustNo;
		uGPSInfo.nStartTrxnNo = uTransactionInfo.nTrxnNo;
		if (nLat != Global.DEFAULT_FLOAT && nLng != Global.DEFAULT_FLOAT) {
			uGPSInfo.strGPSData = String.format(Locale.ENGLISH, "%f, %f", nLat, nLng);
		} else {
			uGPSInfo.strGPSData = "ERROR: NO GPS";
		}
		if(strDocType.equals("MAIT"))
			uGPSInfo.strDocType = "MAIT";
		else
			uGPSInfo.strDocType = "GPS";
		uGPSInfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(mCurDate);
		uGPSInfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(mCurDate);*/

		uTransactionInfo.strTripId = Utils.getStringSetting(mContext, Global.PREF_TRIP_ID);
		uTransactionInfo.strDriverId = "";
		uTransactionInfo.strDriverName = Utils.getStringSetting(mContext, Global.PREF_USER_DESCRIPTION);
		uTransactionInfo.strDriverNumber = Utils.getStringSetting(mContext, Global.PREF_DRIVER_NUMBER);

		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		dbAdapter.insertUTransactionsInfo(uTransactionInfo);
//		dbAdapter.insertUGPSInfo(uGPSInfo);
		dbAdapter.close();

		AddTrxnNo(nCustNo, nChainNo, m_lTrxnNo);

		return m_lTrxnNo;
	}

	public long setGeneralUploadData(String strDocType, int nChainNo, int nCustNo, long nTrxnNo, String strReference, String strDocNo, Date curDate) {
//		double nLat = Utils.getDoubleSetting(mContext, Global.PREF_MY_LATITUDE);
//		double nLng = Utils.getDoubleSetting(mContext, Global.PREF_MY_LONGITUDE);

		if( nTrxnNo != -1 )
			m_lTrxnNo = nTrxnNo;
		else
			m_lTrxnNo = System.currentTimeMillis();

		mCurDate = curDate;

		UTransactionsInfo uTransactionInfo = new UTransactionsInfo();
		uTransactionInfo.nTrxnNo = m_lTrxnNo;
		uTransactionInfo.strDocType = strDocType;
		uTransactionInfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(mContext, mCurDate));
		uTransactionInfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(mCurDate);
		uTransactionInfo.nChainNo = nChainNo;
		uTransactionInfo.strReference = strReference;
		uTransactionInfo.nCustNo = nCustNo;
		uTransactionInfo.strDocNo = strDocNo;

/*		UGPSInfo uGPSInfo = new UGPSInfo();
		uGPSInfo.nTrxnNo = m_lTrxnNo;
		uGPSInfo.nChainNo = nChainNo;
		uGPSInfo.nCustNo = nCustNo;
		uGPSInfo.nStartTrxnNo = uTransactionInfo.nTrxnNo;
		if (nLat != Global.DEFAULT_FLOAT && nLng != Global.DEFAULT_FLOAT) {
			uGPSInfo.strGPSData = String.format(Locale.ENGLISH, "%f, %f", nLat, nLng);
		} else {
			uGPSInfo.strGPSData = "ERROR: NO GPS";
		}
		if(strDocType.equals("MAIT"))
			uGPSInfo.strDocType = "MAIT";
		else
			uGPSInfo.strDocType = "GPS";
		uGPSInfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(mCurDate);
		uGPSInfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(mCurDate);*/

		uTransactionInfo.strTripId = Utils.getStringSetting(mContext, Global.PREF_TRIP_ID);
		uTransactionInfo.strDriverId = "";
		uTransactionInfo.strDriverName = Utils.getStringSetting(mContext, Global.PREF_USER_DESCRIPTION);
		uTransactionInfo.strDriverNumber = Utils.getStringSetting(mContext, Global.PREF_DRIVER_NUMBER);

		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		dbAdapter.insertUTransactionsInfo(uTransactionInfo);
//		dbAdapter.insertUGPSInfo(uGPSInfo);
		dbAdapter.close();

		AddTrxnNo(nCustNo, nChainNo, m_lTrxnNo);

		return m_lTrxnNo;
	}

	public void AddTrxnNo(int nCustNo, int nChainNo, long nTrxnNo)
	{
		if( makeZipFileInfo == null )
		{
			makeZipFileInfo = new MakeZipFileInfo();
			makeZipFileInfo.nCustNo = nCustNo;
			makeZipFileInfo.nChainNo = nChainNo;
		}
		boolean bAdd = true;
		for(Long trxnNo : makeZipFileInfo.trxnList)
		{
			if(trxnNo == nTrxnNo)
			{
				bAdd = false;
				break;
			}
		}
		if( bAdd )
		{
			makeZipFileInfo.trxnList.add(nTrxnNo);
		}
	}

	public String setUCameraData(String strDocType, String strPhotoPath, String strReference, UFileTransactionInfo fileTransactionInfo, int nIndex, boolean bDelete) {
		Date curDate = new Date();
		long nTrxnNo = System.currentTimeMillis();
		String strExtension = Utils.getFileExtension(strPhotoPath);
		if (strReference.length() == 0) {
			strReference = String.format(Locale.ENGLISH, "%s%s%12d%02d.%s", Global.gTripNumber, new SimpleDateFormat("yyMMddHHmmss").format(curDate), nTrxnNo, nIndex, strExtension);
		}

		UCamerasInfo uCameraInfo = new UCamerasInfo();
		if(Global.gCurCustomerInfo != null)
		{
			uCameraInfo.nChainNo = Global.gCurCustomerInfo.nChainNo;
			uCameraInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
		}
		else
		{
			uCameraInfo.nChainNo = 0;
			uCameraInfo.nCustNo = 0;
		}

		uCameraInfo.nTrxnNo = nTrxnNo;
		uCameraInfo.strDocType = strDocType;
		uCameraInfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(mContext, curDate));
		uCameraInfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(curDate);
		uCameraInfo.strReference = strReference;

		UTransactionsInfo uTransactionInfo = new UTransactionsInfo();
		uTransactionInfo.nTrxnNo = nTrxnNo;
		uTransactionInfo.strDocType = strDocType;
		uTransactionInfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(mContext, curDate));
		uTransactionInfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(curDate);
		uTransactionInfo.strReference = strReference;
		if(Global.gCurCustomerInfo != null)
		{
			uTransactionInfo.nChainNo = Global.gCurCustomerInfo.nChainNo;
			uTransactionInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
		}
		else
		{
			uTransactionInfo.nChainNo = 0;
			uTransactionInfo.nCustNo = 0;
		}


		uTransactionInfo.strTripId = Utils.getStringSetting(mContext, Global.PREF_TRIP_ID);
		uTransactionInfo.strDriverId = "";
		uTransactionInfo.strDriverName = Utils.getStringSetting(mContext, Global.PREF_USER_DESCRIPTION);
		uTransactionInfo.strDriverNumber = Utils.getStringSetting(mContext, Global.PREF_DRIVER_NUMBER);


		fileTransactionInfo.nFileTrxnNo = uTransactionInfo.nTrxnNo;

		try {
			if (Utils.createDirectory(Global.PHOTO_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/") == false) {
				Utils.printDebug(null, "Can't create the upload directory on SD Card.");
				return "";
			}
			boolean bRes = Utils.fileCopy(strPhotoPath, Global.PHOTO_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + strReference);
			Date createDateTime = Utils.getRoutectlTripDateTime(mContext, new Date());
			fileTransactionInfo.strFileCreateDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(createDateTime);
			fileTransactionInfo.strFileCreateTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(createDateTime);
			fileTransactionInfo.strTrxnDate = fileTransactionInfo.strFileCreateDate;
			fileTransactionInfo.strTrxnTime = fileTransactionInfo.strFileCreateTime;
			if (bRes == true) {
				DbAdapter dbAdapter = new DbAdapter(mContext);
				dbAdapter.open();
				dbAdapter.insertUCameraInfo(uCameraInfo);
				dbAdapter.insertUTransactionsInfo(uTransactionInfo);
//				dbAdapter.insertUFileTransactionsInfo(fileTransactionInfo);
				dbAdapter.close();
				if(bDelete)
				{
					Utils.fileDelete(strPhotoPath);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(Global.gCurCustomerInfo != null)
		{
			AddTrxnNo(Global.gCurCustomerInfo.nCustNo, Global.gCurCustomerInfo.nChainNo, nTrxnNo);
		}
		else
		{
			AddTrxnNo(0, 0, nTrxnNo);
		}

		return strReference;
	}

	public void setUImageFileTransactionInfo(int nChainNo, int nCustNo, long nFileTrxnNo,
											 String strFileDocNo, String strFileShortDescription, String strFileLongDescription,
											 String strFileCreateDate, String strFileCreateTime, String strFileFileName)
	{
		UFileTransactionInfo uTransactionInfo = new UFileTransactionInfo();
		uTransactionInfo.nTrxnNo = System.currentTimeMillis();
		uTransactionInfo.strDocType = "FARC";
		Date createDateTime = Utils.getRoutectlTripDateTime(mContext, new Date(uTransactionInfo.nTrxnNo));
		uTransactionInfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(mContext, createDateTime));
		uTransactionInfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(new Date(uTransactionInfo.nTrxnNo));
		uTransactionInfo.nChainNo = nChainNo;
		uTransactionInfo.nCustNo = nCustNo;
		uTransactionInfo.nTripNo = Utils.parseIntFromString(Global.gTripNumber);
		uTransactionInfo.nFileTrxnNo = nFileTrxnNo;
		uTransactionInfo.strFileDocNo = strFileDocNo;
		uTransactionInfo.strFileShortDescription = strFileShortDescription;
		uTransactionInfo.strFileLongDescription = strFileLongDescription;
		uTransactionInfo.strFileCreateDate = uTransactionInfo.strTrxnDate;
		uTransactionInfo.strFileCreateTime = uTransactionInfo.strTrxnTime;
		uTransactionInfo.strFileFileName = strFileFileName;

		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		dbAdapter.insertUFileTransactionsInfo(uTransactionInfo);
		dbAdapter.close();
		AddTrxnNo(nCustNo, nChainNo, uTransactionInfo.nTrxnNo);
	}

	public void setUFileTransactionInfo(long nTrxnNo, int nChainNo, int nCustNo, long nFileTrxnNo, String strFileDocNo, String strFileShortDesc, String strFileLongDesc, String strFileCreateDate, String strFileCreateTime, String strFileFileName)
	{
		UFileTransactionInfo uTransactionInfo = new UFileTransactionInfo();
		uTransactionInfo.nTrxnNo = nTrxnNo;
		uTransactionInfo.strDocType = "FARC";
		Date createDateTime = Utils.getRoutectlTripDateTime(mContext, new Date(uTransactionInfo.nTrxnNo));
		uTransactionInfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(mContext, createDateTime));
		uTransactionInfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(new Date(nTrxnNo));
		uTransactionInfo.nChainNo = nChainNo;
		uTransactionInfo.nCustNo = nCustNo;
		uTransactionInfo.nTripNo = Utils.parseIntFromString(Global.gTripNumber);
		uTransactionInfo.nFileTrxnNo = nFileTrxnNo;
		uTransactionInfo.strFileDocNo = strFileDocNo;
		uTransactionInfo.strFileShortDescription = strFileShortDesc;
		uTransactionInfo.strFileLongDescription = strFileLongDesc;
		uTransactionInfo.strFileCreateDate = uTransactionInfo.strTrxnDate;
		uTransactionInfo.strFileCreateTime = uTransactionInfo.strTrxnTime;
		uTransactionInfo.strFileFileName = strFileFileName;

		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		dbAdapter.insertUFileTransactionsInfo(uTransactionInfo);
		dbAdapter.close();
		AddTrxnNo(nCustNo, nChainNo, nTrxnNo);
	}

	public UTransactionsInfo setUTransactionsLastInfo(long nTrxnNo, String strDocType, String strReference){
		Date curDate = new Date();
		UTransactionsInfo uTransactionInfo = new UTransactionsInfo();
		uTransactionInfo.nTrxnNo = nTrxnNo;
		uTransactionInfo.strDocType = strDocType;
		uTransactionInfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(mContext, curDate));
		uTransactionInfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(curDate);
		uTransactionInfo.strReference = strReference;
		uTransactionInfo.nChainNo = 0;
		uTransactionInfo.nCustNo = 0;

		uTransactionInfo.strTripId = Utils.getStringSetting(mContext, Global.PREF_TRIP_ID);
		uTransactionInfo.strDriverId = "";
		uTransactionInfo.strDriverName = Utils.getStringSetting(mContext, Global.PREF_USER_DESCRIPTION);
		uTransactionInfo.strDriverNumber = Utils.getStringSetting(mContext, Global.PREF_DRIVER_NUMBER);

		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		dbAdapter.insertUTransactionsInfo(uTransactionInfo);
		dbAdapter.close();
		AddTrxnNo(uTransactionInfo.nCustNo, uTransactionInfo.nChainNo, nTrxnNo);
		return uTransactionInfo;
	}
	
	public UTransactionsInfo setUTransactionsInfo(int nChainNo, int nCustNo, long nTrxnNo, String strDocType, String strReference) {
		Date curDate = new Date(nTrxnNo);
		UTransactionsInfo uTransactionInfo = new UTransactionsInfo();
		uTransactionInfo.nTrxnNo = nTrxnNo;
		uTransactionInfo.strDocType = strDocType;
		uTransactionInfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(mContext, curDate));
		uTransactionInfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(curDate);
		uTransactionInfo.strReference = strReference;
		uTransactionInfo.nChainNo = nChainNo;
		uTransactionInfo.nCustNo = nCustNo;

		uTransactionInfo.strTripId = Utils.getStringSetting(mContext, Global.PREF_TRIP_ID);
		uTransactionInfo.strDriverId = "";
		uTransactionInfo.strDriverName = Utils.getStringSetting(mContext, Global.PREF_USER_DESCRIPTION);
		uTransactionInfo.strDriverNumber = Utils.getStringSetting(mContext, Global.PREF_DRIVER_NUMBER);

		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		dbAdapter.insertUTransactionsInfo(uTransactionInfo);
		dbAdapter.close();
		AddTrxnNo(nCustNo, nChainNo, nTrxnNo);
		return uTransactionInfo;
	}

	public void selectPhoto() {
		if(Build.VERSION.SDK_INT >= 24)
		{
			File photoFile = null;
			try {
				photoFile = createImageFile();
			} catch (IOException ex) {
			}
			if(photoFile != null) {
				try {
					Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					Uri photoURI = FileProvider.getUriForFile(mContext,
								"dsd.dsdvansales.fileprovider",
								photoFile);
					takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
					startActivityForResult(takePictureIntent, RESULT_TAKE_PHOTO);
				}
				catch (ActivityNotFoundException e) {
					Toast.makeText(mContext, Str.CameraAppNotFound[Global.LANG], Toast.LENGTH_LONG).show();
				}
			}
		}
		else
		{
			try {
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				File f = new File(Global.ROOT_LOCATION, "temp.jpg");
				intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
				startActivityForResult(intent, RESULT_TAKE_PHOTO);
			}
			catch (ActivityNotFoundException e) {
				Toast.makeText(mContext, Str.CameraAppNotFound[Global.LANG], Toast.LENGTH_LONG).show();
			}
		}
	}

	private File createImageFile() throws IOException {
		String imageFileName = "temp.jpg";
		File storageDir = mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
		File image = File.createTempFile(
				imageFileName,  /* prefix */
				".jpg",         /* suffix */
				storageDir      /* directory */
		);

		// Save a file: path for use with ACTION_VIEW intents
		mCurrentPhotoPath = image.getAbsolutePath();
		return image;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			String strPhotoPath = "";
			if (requestCode == RESULT_TAKE_PHOTO) {
				if(Build.VERSION.SDK_INT >= 24)
				{
					strPhotoPath = mCurrentPhotoPath;
				}
				else
				{
					File f = new File(Global.ROOT_LOCATION + "/temp.jpg");
					strPhotoPath = f.getAbsolutePath();
				}
			} else if (requestCode == RESULT_FROM_GALLERY) {
				Uri selectedImageUri = data.getData();
				String[] projection = {MediaStore.MediaColumns.DATA};
				Cursor cursor = mContext.managedQuery(selectedImageUri, projection, null, null, null);
				int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
				cursor.moveToFirst();
				strPhotoPath = cursor.getString(column_index);
			}
			if (strPhotoPath.length() > 0) {
				FileOutputStream fOut = null;
				try {
					Bitmap bmp = Utils.getSafeDecodeBitmap(strPhotoPath, 1000);
					if (bmp != null) {
						String strExtension = Utils.getFileExtension(strPhotoPath);
						strPhotoPath = String.format(Locale.ENGLISH, "%s%d.%s", Global.ROOT_LOCATION, System.currentTimeMillis(), strExtension);
						fOut = new FileOutputStream(strPhotoPath);
						bmp.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
						Intent intent = new Intent();
						intent.putExtra(Global.EXTRA_PHOTO_PATH, strPhotoPath);
						receiveBroadcast(Global.ACTION_TAKE_PHOTO, intent);
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					try {
						if (fOut != null) {
							fOut.close();
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				Utils.printDebug(null, "Captured photo = " + strPhotoPath);
			} else {
				Utils.printDebug(mContext, Str.Photo_selection_was_failed[Global.LANG]);
			}
		} else if (resultCode != Activity.RESULT_CANCELED) {
			Utils.printDebug(mContext, Str.Error_occurs[Global.LANG] + resultCode, false);
		}
	}

	private void unregisterReceiver() {
		try {
			if (mEventReceiver != null) {
				mContext.unregisterReceiver(mEventReceiver);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onDestroy() {
		unregisterReceiver();
		super.onDestroy();
	}

	private final class EventReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String strAction = intent.getAction();
			if (strAction.equals(Global.ACTION_REFRESH_LANGUAGE) == true) {
				refreshLanguage();
			} else {
				receiveBroadcast(strAction, intent);
			}
		}
	}

	public void focusAndHideKeyboard(EditText editText) {
		InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
		editText.requestFocus();
		editText.setSelection(editText.getText().toString().length());
		imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
	}

	public void refreshDriverName()
	{

	}

	public void showProgressDialog(String strText)
	{
		try {
			if(mProgressDialog == null)
			{
				mProgressDialog = new ProgressDialog(mContext);
				mProgressDialog.setCancelable(false);
			}
			mProgressDialog.setMessage(strText);
			mProgressDialog.show();
		}catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	public void hideProgressDialog()
	{
		try {
			if(mProgressDialog != null)
			{
				mProgressDialog.dismiss();
				mProgressDialog = null;
			}
		}catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	public float getProductPrice(String strItemNo)
	{
		if(Global.prodLocnInfoMap.size() == 0)
		{
			DbAdapter dbAdapter = new DbAdapter(mContext);
			dbAdapter.open();
			dbAdapter.getProdLocnInfoList(Global.prodLocnInfoMap);
			dbAdapter.close();
		}

		ProdLocnInfo info = Global.prodLocnInfoMap.get(strItemNo);
		if(info == null)
		{
			return 0;
		}
		else
		{
			return Utils.getDividedValue(info.nBasePrice);
		}
	}

//	public float getProductDriverPrice(String strItemNo) {
//		if(Global.prodLocnInfoMap.size() == 0)
//		{
//			DbAdapter dbAdapter = new DbAdapter(mContext);
//			dbAdapter.open();
//			dbAdapter.getProdLocnInfoList(Global.prodLocnInfoMap);
//			dbAdapter.close();
//		}
//
//		ProdLocnInfo info = Global.prodLocnInfoMap.get(strItemNo);
//		if(info == null)
//		{
//			return 0;
//		}
//		else
//		{
//			return Utils.getDividedValue(info.nDriverPrice);
//		}
//	}
}