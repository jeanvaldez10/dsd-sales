package dsd.dsdvansales.dialogs;

import android.app.Dialog;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.text.SimpleDateFormat;
import java.util.Date;
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

public class DriverBreakCountDownDialog extends Dialog {

	public interface Callback{
		void OnOK();
	}

	private MainActivity mContext = null;
	private DescTypeInfo descTypeInfo = null;
	private MakeZipFileInfo makeZipFileInfo = null;
	Callback callback;
	int nCurrentCount = 0;
	long lTime = 0;
	boolean isContinue = false;
	public DriverBreakCountDownDialog(MainActivity context, Callback callback, DescTypeInfo descTypeInfo, boolean isContinue) {
		super(context, android.R.style.Theme_Translucent_NoTitleBar);
		this.mContext = context;
		this.callback = callback;
		this.descTypeInfo = descTypeInfo;
		this.isContinue = isContinue;
	}

	public boolean bStopCounting = false;
	public boolean bFromUser = false;
	Thread mThread = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setCancelable(false);
		WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
		lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
		lpWindow.dimAmount = 0.9f;
		getWindow().setAttributes(lpWindow);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.dialog_driver_break_count_down);

		((TextView)findViewById(R.id.txtMessage)).setText(Str.BreakHasEnabled[Global.LANG]);

		findViewById(R.id.txtCountDownMessage).setOnLongClickListener(view -> {
			final MessageDialog msgDialog = new MessageDialog(mContext);
			msgDialog.setCancelable(false);
			msgDialog.mStrMessage = Str.SureToStopCountDown[Global.LANG];
			msgDialog.mStrLeft = Str.Yes[Global.LANG];
			msgDialog.mStrRight = Str.No[Global.LANG];
			msgDialog.setOnDismissListener(dialog -> {
				if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
					doStopCountDown(true);
				}
			});
			msgDialog.show();
			return false;
		});

		if(isContinue)
		{
			long breakDownTime = Utils.getLongSetting(mContext, Global.PREF_BREAK_DOWN_START_TIME);
			long breakDownDuration = System.currentTimeMillis() - breakDownTime;
			long remainDuration = 60 * Utils.parseIntFromString(descTypeInfo.strValue1) * 1000 - breakDownDuration;

			nCurrentCount = (int)(remainDuration / 1000);
			doCountDown(0);
		}
		else
		{
			if(nCurrentCount == 0)
			{
				nCurrentCount = 60 * Utils.parseIntFromString(descTypeInfo.strValue1);
				lTime = System.currentTimeMillis();
				Utils.setLongSetting(mContext, Global.PREF_BREAK_DOWN_START_TIME, lTime);
			}
			doCountDown(0);
		}

	}

	public void doCountDown(int nSubCnt)
	{
		this.nCurrentCount -= nSubCnt;
		bStopCounting = false;
		if(mThread == null)
		{
			mThread = new Thread(){
				@Override
				public void run() {
					while(nCurrentCount > 0)
					{
						if(bStopCounting)
						{
							if(nCurrentCount == 1)
							{
								new Handler(Looper.getMainLooper()).post(() -> doFinish());
								mThread = null;
								return;
							}
							else if(bFromUser)
							{
								new Handler(Looper.getMainLooper()).post(() -> doFinishManually());
								mThread = null;
								return;
							}
							else
							{
								mThread = null;
								return;
							}
						}
						String strText = "";
						int nMin = nCurrentCount / 60;
						int nSec = nCurrentCount % 60;
						if(nMin == 1)
						{
							strText += nMin + " minute ";
						}
						else
						{
							strText += nMin + " minutes ";
						}

						if(nSec == 1)
						{
							strText += nSec + " second left";
						}
						else
						{
							strText += nSec + " seconds left";
						}
						String finalStrText = strText;
						new Handler(Looper.getMainLooper()).post(() -> ((TextView)findViewById(R.id.txtCountDownMessage)).setText(finalStrText));

						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						nCurrentCount--;
					}
					new Handler(Looper.getMainLooper()).post(() -> doFinish());
					mThread = null;
					return;
				}
			};
			mThread.start();
		}
	}

	public void doStopCountDown(boolean bFromUser)
	{
		this.bFromUser = bFromUser;
		this.bStopCounting = true;
	}

	private void doFinishProcess(Location location)
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
		if(nCurrentCount >=0)
		{
			setGeneralUploadData("BEND", 0, 0, -1, String.valueOf(descTypeInfo.nNumericKey));
		}
		else
		{
			long trxnNo = System.currentTimeMillis() + nCurrentCount * 1000;
			setGeneralUploadData("BEND", 0, 0, trxnNo, String.valueOf(descTypeInfo.nNumericKey));
		}

		Utils.registerUploadFile(mContext, makeZipFileInfo);
		makeZipFileInfo = null;

		Utils.setLongSetting(mContext, Global.PREF_BREAK_DOWN_START_TIME, 0);

		dismiss();
		if(callback != null)
		{
			callback.OnOK();
		}
	}

	private void doFinish()
	{
		Utils.startMainJob(mContext);

		if(ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
				ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
		{
			doFinishProcess(null);
			return;
		}

		LocationServices.getFusedLocationProviderClient(mContext).getLastLocation().addOnCompleteListener(task -> {
			if (task.isSuccessful() && task.getResult() != null) {
				Location location = task.getResult();
				doFinishProcess(location);
			} else {
				doFinishProcess(null);
			}
		});
	}

	private void doFinishManuallyProcess(Location location)
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

		setGeneralUploadData("BDED", 0, 0, -1, String.valueOf(descTypeInfo.nNumericKey));
		Utils.registerUploadFile(mContext, makeZipFileInfo);
		makeZipFileInfo = null;

		Utils.setLongSetting(mContext, Global.PREF_BREAK_DOWN_START_TIME, 0);

		dismiss();
		if(callback != null)
		{
			callback.OnOK();
		}
	}

	private void doFinishManually()
	{
		Utils.startMainJob(mContext);

		if(ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
				ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
		{
			doFinishManuallyProcess(null);
			return;
		}

		LocationServices.getFusedLocationProviderClient(mContext).getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
			@Override
			public void onComplete(@NonNull Task<Location> task) {
				if (task.isSuccessful() && task.getResult() != null) {
					Location location = task.getResult();
					doFinishManuallyProcess(location);
				} else {
					doFinishManuallyProcess(null);
				}
			}
		});
	}

	public long setGeneralUploadData(String strDocType, int nChainNo, int nCustNo, long nTrxnNo, String strReference) {
		double nLat = Utils.getDoubleSetting(mContext, Global.PREF_MY_LATITUDE);
		double nLng = Utils.getDoubleSetting(mContext, Global.PREF_MY_LONGITUDE);

		long m_lTrxnNo;
		if( nTrxnNo != -1 )
			m_lTrxnNo = nTrxnNo;
		else
			m_lTrxnNo = System.currentTimeMillis();

		Date mCurDate = new Date(m_lTrxnNo);

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
		transactionInfo.strTime = new SimpleDateFormat("kk:mm").format(mCurDate);
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

}