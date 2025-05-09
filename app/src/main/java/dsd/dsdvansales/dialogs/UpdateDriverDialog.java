package dsd.dsdvansales.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import dsd.dsdvansales.R;
import dsd.dsdvansales.activities.LoginActivity;
import dsd.dsdvansales.activities.MainActivity;
import dsd.dsdvansales.db.DbAdapter;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.model.DescTypeInfo;
import dsd.dsdvansales.model.MakeZipFileInfo;
import dsd.dsdvansales.model.TransactionInfo;
import dsd.dsdvansales.model.UOrderStatusDInfo;
import dsd.dsdvansales.model.UOrderStatusHInfo;
import dsd.dsdvansales.model.UServicesInfo;
import dsd.dsdvansales.model.UTransactionsInfo;
import dsd.dsdvansales.views.AlertSingleChoiceAdapter;

public class UpdateDriverDialog extends Dialog {

	private Context mContext = null;
	private TextView mTxtDriver = null;
	private TextView mTxtCurrentDriver = null;

	private List<DescTypeInfo> mDescTypeList = new ArrayList<>();

	private String mCurrentDriveName = "";
	private boolean mIsFromLogin = false;

	private int mSelDriverIdx = 0;
	public MakeZipFileInfo makeZipFileInfo = null;

	public UpdateDriverDialog(Context context, boolean isFromLogin) {
		super(context, android.R.style.Theme_Translucent_NoTitleBar);
		this.mContext = context;
		this.mIsFromLogin = isFromLogin;
	}

	public boolean m_bClicked = false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
		lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
		lpWindow.dimAmount = 0.9f;
		getWindow().setAttributes(lpWindow);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.dialog_update_driver);

		loadDescTypeList();
		if (mDescTypeList.size() == 0) {
			new Handler(Looper.getMainLooper()).post(new Runnable() {
				@Override
				public void run() {
					Utils.printDebug(mContext, "No Driver List to update.");
					dismiss();
				}
			});
			return;
		}

		mTxtDriver = (TextView)findViewById(R.id.txtDriver);
		mTxtCurrentDriver = (TextView)findViewById(R.id.txtCurrentDriver);

		((TextView)findViewById(R.id.lblCurrentDriver)).setText(Str.CurrentDriver[Global.LANG]);
		((TextView)findViewById(R.id.txtTitle)).setText(Str.UpdateDriver[Global.LANG]);
		((TextView)findViewById(R.id.lblChangeTo)).setText(Str.ChangeTo[Global.LANG]);

		((Button)findViewById(R.id.btnReturn)).setText(Str.Return[Global.LANG]);
		((Button)findViewById(R.id.btnUpdate)).setText(Str.Update[Global.LANG]);

		if (!mIsFromLogin) {
			((Button)findViewById(R.id.btnReturn)).setText(Str.Return[Global.LANG]);
		}
		else {
			((Button)findViewById(R.id.btnReturn)).setText(Str.Continue[Global.LANG]);
		}

		//Set Current Driver Name
		mTxtCurrentDriver.setText(mCurrentDriveName);
		mTxtDriver.setText(mDescTypeList.get(0).strDescription);

		//Set Event
		mTxtDriver.setOnClickListener(view -> showDriverList());

		findViewById(R.id.btnReturn).setOnClickListener(view -> {
			dismiss();

			if (mIsFromLogin) {
				// should login with the current driver option
				((LoginActivity)mContext).refreshDriverName();
			}
		});

		findViewById(R.id.btnUpdate).setOnClickListener(view -> {
			int nRouteNumber = mDescTypeList.get(mSelDriverIdx).nNumericKey;
			Utils.setStringSetting(mContext, Global.PREF_DRIVER_NUMBER, String.valueOf(nRouteNumber));
			Utils.setStringSetting(mContext, Global.PREF_USER_DESCRIPTION, mDescTypeList.get(mSelDriverIdx).strDescription);
			dismiss();

			long nTrxnNo = setGeneralUploadData("DRVR", 0, 0, -1, mDescTypeList.get(mSelDriverIdx).strDescription);

			TransactionInfo transactionInfo = new TransactionInfo();
			transactionInfo.strType = "DRVR";
			transactionInfo.strTime = new SimpleDateFormat("kk:mm").format(new Date(nTrxnNo));
			transactionInfo.strCustomerName = "";
			DbAdapter dbAdapter = new DbAdapter(mContext);
			dbAdapter.open();
			dbAdapter.insertTransactionHistoryInfo(transactionInfo);
			dbAdapter.close();
			Utils.setBooleanSetting(mContext, Global.PREF_ASKED_CHANGE_DRIVER_NAME, true);
			//Global.gUploadCustNoList.add(makeZipFileInfo);
			Utils.registerUploadFile(mContext, makeZipFileInfo);
			makeZipFileInfo = null;
			if(mContext instanceof MainActivity)
			{
				((MainActivity)mContext).refreshDriverName();
			}
			else if(mContext instanceof LoginActivity)
			{
				((LoginActivity)mContext).refreshDriverName();
			}
		});
	}

	public long setGeneralUploadData(String strDocType, int nChainNo, int nCustNo, long nTrxnNo, String strReference) {
//		double nLat = Utils.getDoubleSetting(mContext, Global.PREF_MY_LATITUDE);
//		double nLng = Utils.getDoubleSetting(mContext, Global.PREF_MY_LONGITUDE);
		Date mCurDate = new Date();
		long m_lTrxnNo;
		if( nTrxnNo != -1 )
			m_lTrxnNo = nTrxnNo;
		else
			m_lTrxnNo = System.currentTimeMillis();

		UTransactionsInfo uTransactionInfo = new UTransactionsInfo();
		uTransactionInfo.nTrxnNo = m_lTrxnNo;
		uTransactionInfo.strDocType = strDocType;
		uTransactionInfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(mContext, new Date()));
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

	private void loadDescTypeList() {
		String strDescriptionTypeID = Global.DRIVER_DESC_TYPE_ID;
		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		//List<DescTypeInfo> currentDriverList = dbAdapter.getDescTypeListWithNumericKey(strDescriptionTypeID, Utils.parseIntFromString(Global.gRouteNumber));
		List<DescTypeInfo> list = dbAdapter.getDescTypeList(strDescriptionTypeID);
		dbAdapter.close();

		/*if(currentDriverList != null && currentDriverList.size() > 0)
		{
			mCurrentDriveName = currentDriverList.get(0).strDescription;
		}*/
		mCurrentDriveName = Utils.getStringSetting(mContext, Global.PREF_USER_DESCRIPTION);

		String strValue1 = Utils.getStringSetting(mContext, Global.PREF_DEF_LOCN_NO);
		mDescTypeList.clear();
		for(DescTypeInfo descTypeInfo : list)
		{
			if((TextUtils.isEmpty(descTypeInfo.strValue1) || descTypeInfo.strValue1.equals(strValue1)) &&
					!descTypeInfo.strDescription.equals(Utils.getStringSetting(mContext, Global.PREF_USER_DESCRIPTION)))
			{
				mDescTypeList.add(descTypeInfo);
			}
		}
		Collections.sort(mDescTypeList, new Comparator<DescTypeInfo>() {
			@Override
			public int compare(DescTypeInfo descTypeInfo, DescTypeInfo t1) {
				return descTypeInfo.nNumericKey - t1.nNumericKey;
			}
		});
	}

	private void showDriverList() {
		final String strArray[] = new String[mDescTypeList.size()];
		for (int i=0; i<mDescTypeList.size(); i++) {
			strArray[i] = mDescTypeList.get(i).strDescription;
		}
		final int origIdx = mSelDriverIdx;
		final String origText = mTxtDriver.getText().toString();

		try
		{
			new AlertDialog.Builder(mContext)
					.setTitle(Str.SelectDriver[Global.LANG])
					.setSingleChoiceItems(new AlertSingleChoiceAdapter(mContext, strArray), origIdx, (dialog, nSelected) -> {
						mSelDriverIdx = nSelected;
						mTxtDriver.setText(strArray[nSelected]);
					})
					.setPositiveButton(Str.Ok[Global.LANG], (dialog, nSelected) -> {
					})
					.setNegativeButton(Str.Cancel[Global.LANG], (dialog, nSelected) -> {
						mSelDriverIdx = origIdx;
						mTxtDriver.setText(origText);
					})
					.create()
					.show();
		}catch (Exception e)
		{
			e.printStackTrace();
		}

	}
}