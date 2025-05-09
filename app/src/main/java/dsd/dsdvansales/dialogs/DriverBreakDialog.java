package dsd.dsdvansales.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import dsd.dsdvansales.R;
import dsd.dsdvansales.activities.MainActivity;
import dsd.dsdvansales.db.DbAdapter;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.model.DescTypeInfo;
import dsd.dsdvansales.model.MakeZipFileInfo;
import dsd.dsdvansales.model.TransactionInfo;
import dsd.dsdvansales.model.UGPSInfo;
import dsd.dsdvansales.model.UTransactionsInfo;
import dsd.dsdvansales.views.AlertSingleChoiceAdapter;

public class DriverBreakDialog extends Dialog {

	public interface Callback{
		void onReturn();
		void onSelect(DescTypeInfo info);
	}

	private MainActivity mContext = null;
	private TextView mTxtDriver = null;

	private List<DescTypeInfo> mDescTypeList = new ArrayList<>();

	private int mSelDriverIdx = 0;
	public MakeZipFileInfo makeZipFileInfo = null;
	Callback callback;
	public DriverBreakDialog(MainActivity context, Callback callback) {
		super(context, android.R.style.Theme_Translucent_NoTitleBar);
		this.mContext = context;
		this.callback = callback;
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

		setContentView(R.layout.dialog_driver_break);

		loadDescTypeList();
		if (mDescTypeList.size() == 0) {
			new Handler(Looper.getMainLooper()).post(() -> {
				Utils.printDebug(mContext, "No Driver Break List to update.");
				dismiss();
			});
			return;
		}

		mTxtDriver = (TextView)findViewById(R.id.txtDriverBreak);

		((TextView)findViewById(R.id.txtTitle)).setText(Str.DriverBreak[Global.LANG]);

		((Button)findViewById(R.id.btnReturn)).setText(Str.Return[Global.LANG]);
		((Button)findViewById(R.id.btnBreak)).setText(Str.Break[Global.LANG]);

		//Set Current Driver Name
		mTxtDriver.setText(mDescTypeList.get(0).strDescription);

		//Set Event
		mTxtDriver.setOnClickListener(view -> showDriverList());

		findViewById(R.id.btnReturn).setOnClickListener(view -> {
			dismiss();
			if(callback != null)
			{
				callback.onReturn();
			}
		});

		findViewById(R.id.btnBreak).setOnClickListener(view -> {
			if(ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
					ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
			{
				doProcessBreak(null);
				return;
			}

			LocationServices.getFusedLocationProviderClient(mContext).getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
				@Override
				public void onComplete(@NonNull Task<Location> task) {
					if (task.isSuccessful() && task.getResult() != null) {
						Location location = task.getResult();
						doProcessBreak(location);
					} else {
						doProcessBreak(null);
					}
				}
			});
		});
	}

	private void doProcessBreak(Location location)
	{
		if(location != null)
		{
			Utils.setDoubleSetting(mContext, Global.PREF_MY_LATITUDE, location.getLatitude());
			Utils.setDoubleSetting(mContext, Global.PREF_MY_LONGITUDE, location.getLongitude());
		}
		else
		{
			Utils.setDoubleSetting(mContext, Global.PREF_MY_LATITUDE, 0);
			Utils.setDoubleSetting(mContext, Global.PREF_MY_LONGITUDE, 0);
		}

		setGeneralUploadData("BBEG", 0, 0, -1, String.valueOf(mDescTypeList.get(mSelDriverIdx).nNumericKey));
		Utils.registerUploadFile(mContext, makeZipFileInfo);
		makeZipFileInfo = null;

		dismiss();
		if(callback != null)
		{
			callback.onSelect(mDescTypeList.get(mSelDriverIdx));
		}
	}

	public long setGeneralUploadData(String strDocType, int nChainNo, int nCustNo, long nTrxnNo, String strReference) {
		double nLat = Utils.getDoubleSetting(mContext, Global.PREF_MY_LATITUDE);
		double nLng = Utils.getDoubleSetting(mContext, Global.PREF_MY_LONGITUDE);
		Date mCurDate = new Date();
		long m_lTrxnNo;
		if( nTrxnNo != -1 )
			m_lTrxnNo = nTrxnNo;
		else
			m_lTrxnNo = System.currentTimeMillis();

		UTransactionsInfo uTransactionInfo = new UTransactionsInfo();
		uTransactionInfo.nTrxnNo = m_lTrxnNo;
		uTransactionInfo.strDocType = strDocType;
		uTransactionInfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(mContext, mCurDate));
		uTransactionInfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(mCurDate);
		uTransactionInfo.nChainNo = nChainNo;
		uTransactionInfo.strReference = strReference;
		uTransactionInfo.nCustNo = nCustNo;

		TransactionInfo transactionInfo = new TransactionInfo();
		transactionInfo.strType = strDocType;
		transactionInfo.strTime = new SimpleDateFormat("kk:mm").format(new Date(m_lTrxnNo));
		transactionInfo.strCustomerName = "";

		AddTrxnNo(nCustNo, nChainNo, m_lTrxnNo);

		//Make the GPS Records.
		nTrxnNo = System.currentTimeMillis();
		if(nTrxnNo == m_lTrxnNo)
		{
			m_lTrxnNo ++;
		}
		else
		{
			m_lTrxnNo = nTrxnNo;
		}

		UTransactionsInfo uGPSTransactionInfo = new UTransactionsInfo();
		uGPSTransactionInfo.nTrxnNo = m_lTrxnNo;
		uGPSTransactionInfo.strDocType = "GPS";
		uGPSTransactionInfo.strTrxnDate = uTransactionInfo.strTrxnDate;
		uGPSTransactionInfo.strTrxnTime = uTransactionInfo.strTrxnTime;
		uGPSTransactionInfo.nChainNo = nChainNo;
		uGPSTransactionInfo.strReference = "";
		uGPSTransactionInfo.nCustNo = nCustNo;

		TransactionInfo transactionGPSInfo = new TransactionInfo();
		transactionGPSInfo.strType = "GPS";
		transactionGPSInfo.strTime = transactionInfo.strTime;
		transactionGPSInfo.strCustomerName = "";

		UGPSInfo uGPSInfo = new UGPSInfo();
		uGPSInfo.nTrxnNo = m_lTrxnNo;
		uGPSInfo.nChainNo = nChainNo;
		uGPSInfo.nCustNo = nCustNo;
		uGPSInfo.nStartTrxnNo = uTransactionInfo.nTrxnNo;
		if (nLat != Global.DEFAULT_FLOAT && nLng != Global.DEFAULT_FLOAT) {
			uGPSInfo.strGPSData = String.format(Locale.ENGLISH, "%f, %f", nLat, nLng);
		} else {
			uGPSInfo.strGPSData = "ERROR: NO GPS";
		}

		uGPSInfo.strDocType = "GPS";
		uGPSInfo.strTrxnDate = uTransactionInfo.strTrxnDate;
		uGPSInfo.strTrxnTime = uTransactionInfo.strTrxnTime;


		uTransactionInfo.strTripId = Utils.getStringSetting(mContext, Global.PREF_TRIP_ID);
		uTransactionInfo.strDriverId = "";
		uTransactionInfo.strDriverName = Utils.getStringSetting(mContext, Global.PREF_USER_DESCRIPTION);
		uTransactionInfo.strDriverNumber = Utils.getStringSetting(mContext, Global.PREF_DRIVER_NUMBER);

		uGPSTransactionInfo.strTripId = Utils.getStringSetting(mContext, Global.PREF_TRIP_ID);
		uGPSTransactionInfo.strDriverId = "";
		uGPSTransactionInfo.strDriverName = Utils.getStringSetting(mContext, Global.PREF_USER_DESCRIPTION);
		uGPSTransactionInfo.strDriverNumber = Utils.getStringSetting(mContext, Global.PREF_DRIVER_NUMBER);

		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		dbAdapter.insertUTransactionsInfo(uTransactionInfo);
		dbAdapter.insertTransactionHistoryInfo(transactionInfo);

		dbAdapter.insertUGPSInfo(uGPSInfo);
		dbAdapter.insertUTransactionsInfo(uGPSTransactionInfo);
		dbAdapter.insertTransactionHistoryInfo(transactionGPSInfo);
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
		String strDescriptionTypeID = Global.DRIVER_BREAK_TYPE_ID;
		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		List<DescTypeInfo> list = dbAdapter.getDescTypeList(strDescriptionTypeID);
		dbAdapter.close();

		mDescTypeList.clear();
		for(DescTypeInfo descTypeInfo : list)
		{
			mDescTypeList.add(descTypeInfo);
		}
		Collections.sort(mDescTypeList, (descTypeInfo, t1) -> descTypeInfo.nNumericKey - t1.nNumericKey);
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
					.setTitle(Str.SelectDriverBreak[Global.LANG])
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