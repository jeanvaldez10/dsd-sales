package dsd.dsdvansales.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.thread.GPSPollThread;
import dsd.dsdvansales.thread.MainThread;

public class MainService extends Service {

//	MainThread mMainThread = null;
//	GPSPollThread mGPSPollThread = null;

	@Override
	public IBinder onBind(Intent mIntent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();

//		mMainThread = new MainThread(this);
//		mMainThread.start();

		/*mGPSPollThread = new GPSPollThread(this);
		mGPSPollThread.start();*/
		//Set Alarm to wakeup system
//		Utils.setAlarm(getApplicationContext());
		Utils.startMainJob(getApplicationContext());
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Utils.endMainJob();
/*		if (mMainThread != null) {
			mMainThread.isRunning = false;
		}
		if (mGPSPollThread != null) {
			mGPSPollThread.isRunning = false;
		}*/
	}
}