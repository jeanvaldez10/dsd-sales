package dsd.dsdvansales.thread;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
//import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import dsd.dsdvansales.db.DbAdapter;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.model.MakeZipFileInfo;
import dsd.dsdvansales.model.UGPSInfo;
import dsd.dsdvansales.model.UTransactionsInfo;

public class GPSPollThread extends Thread implements Runnable {

	private static final long THREAD_SLEEP_TIME = 1 * 60 * 1000;
	private Context mContext = null;
	private int mCount = 0;

	public boolean isRunning = true;

	public GPSPollThread(Context mContext) {
		this.mContext = mContext;
		isRunning = true;
	}

	public void run() {
		while (isRunning == true) {
			try {
				int nGPSPoll = Utils.getIntSetting(mContext, Global.PREF_GPS_POLL);
				if (nGPSPoll > 0) {
					if (mCount == 0 && Global.gLoggedIn == true) {
						setUGPSData();
					}
					mCount++;
					if (mCount >= nGPSPoll) {
						mCount = 0;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					Thread.sleep(THREAD_SLEEP_TIME);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		Utils.printDebug(null, "Thread exit...");
	}

	private void setUGPSData() {
		if(Global.gSignedOn)
		{
			if(ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
					ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
			{
				return;
			}

			LocationServices.getFusedLocationProviderClient(mContext).getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
				@Override
				public void onComplete(@NonNull Task<Location> task) {
					if (task.isSuccessful() && task.getResult() != null) {
						Location location = task.getResult();

						double nLat = location.getLatitude();
						double nLng = location.getLongitude();
						Date curDate = new Date();
						UGPSInfo uGPSInfo = new UGPSInfo();
						uGPSInfo.nTrxnNo = System.currentTimeMillis();
						uGPSInfo.nChainNo = 0;
						uGPSInfo.nCustNo = 0;
						uGPSInfo.nStartTrxnNo = 0;
						if (nLat != 0 && nLng != 0 ){
							uGPSInfo.strGPSData = String.format(Locale.ENGLISH, "%f, %f", nLat, nLng);
						} else {
							uGPSInfo.strGPSData = "ERROR: NO GPS";
						}
						uGPSInfo.strDocType = "GPS";
						uGPSInfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(mContext, curDate));
						uGPSInfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(curDate);

						UTransactionsInfo uTransactionInfo = new UTransactionsInfo();
						uTransactionInfo.nTrxnNo = uGPSInfo.nTrxnNo;
						uTransactionInfo.strDocType = "GPS";
						uTransactionInfo.strDocNo = "0";
						uTransactionInfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(mContext, new Date(uGPSInfo.nTrxnNo)));
						uTransactionInfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(uGPSInfo.nTrxnNo);
						uTransactionInfo.nChainNo = 0;
						uTransactionInfo.nCustNo = 0;

						uTransactionInfo.strTripId = Utils.getStringSetting(mContext, Global.PREF_TRIP_ID);
						uTransactionInfo.strDriverId = "";
						uTransactionInfo.strDriverName = Utils.getStringSetting(mContext, Global.PREF_USER_DESCRIPTION);
						uTransactionInfo.strDriverNumber = Utils.getStringSetting(mContext, Global.PREF_DRIVER_NUMBER);

						DbAdapter dbAdapter = new DbAdapter(mContext);
						dbAdapter.open();
						dbAdapter.insertUTransactionsInfo(uTransactionInfo);
						dbAdapter.insertUGPSInfo(uGPSInfo);
						dbAdapter.close();

						MakeZipFileInfo makeZipFileInfo = new MakeZipFileInfo();
						makeZipFileInfo.nCustNo = 0;
						makeZipFileInfo.nChainNo = 0;
						makeZipFileInfo.trxnList.add(uGPSInfo.nTrxnNo);
						//Global.gUploadCustNoList.add(makeZipFileInfo);
						Utils.registerUploadFile(mContext, makeZipFileInfo);

					} else {
					}
				}
			});


		}
	}
}
