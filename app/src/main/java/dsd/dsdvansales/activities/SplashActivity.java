package dsd.dsdvansales.activities;

import static android.provider.Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS;

import android.Manifest;
import android.app.Activity;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.LinearLayout;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.splashscreen.SplashScreen;

import java.io.File;
import java.util.Locale;

import dsd.dsdvansales.BuildConfig;
import dsd.dsdvansales.R;
import dsd.dsdvansales.dialogs.MessageDialog;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.listeners.GPSLocationListener;
import dsd.dsdvansales.listeners.NetworkLocationListener;
import dsd.dsdvansales.services.LocationJobService;
import dsd.dsdvansales.services.MainJobService;
import dsd.dsdvansales.services.MainService;

public class SplashActivity extends Activity {
	private static int APP_STORAGE_ACCESS_REQUEST_CODE_UPDATE = 11001;

	private static final int MAX_SPLASH_ANIMATION_CNT = 4;

	private LinearLayout mLayoutSplash = null;
	private int nIdx = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// Handle the splash screen transition.
		SplashScreen splashScreen = SplashScreen.installSplashScreen(this);

		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.activity_splash);
		InitView();
		doCheckBatteryOptimization();
		new Thread()
		{
			@Override
			public void run() {
				Utils.getIPAddress(SplashActivity.this);
			}
		}.start();
	}

	private void doCheckBatteryOptimization()
	{
		/*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			// Get power manager instance
			PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);

			// Check if app isn't already whitelisted from battery optimizations
			if (!powerManager.isIgnoringBatteryOptimizations(getPackageName())) {
				// Get app name as string
				String appName = getPackageManager().getApplicationLabel(getApplicationInfo()).toString();

				final MessageDialog msgDialog = new MessageDialog(this);
				msgDialog.setCancelable(false);
				msgDialog.mStrTitle = "Disable battery optimizations";
				msgDialog.mStrMessage = "If you process the GPS location to our server, please click OK and select \"All apps\" -> " + appName + " -> Don't optimize.";
				msgDialog.mStrLeft = Str.Cancel[Global.LANG];
				msgDialog.mStrRight = Str.Ok[Global.LANG];
				msgDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
							startActivityForResult(new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS), 10051);
						}
						else
						{
							startAnimation();
						}
					}
				});
				msgDialog.show();
				return;
			}
		}*/
//		if(!isNotificationServiceEnabled())
//		{
//			startActivityForResult(new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS), 10001);
//		}
//		else
//		{
//			startAnimation();
//		}
		startAnimation();
	}



	private void InitView() {
		mLayoutSplash = (LinearLayout) findViewById(R.id.layoutSplash);
	}

	private void startLocationListeners() {
		try {
			LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

			GPSLocationListener gpsLocationListener = new GPSLocationListener(this);
			if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
				// TODO: Consider calling
				//    ActivityCompat#requestPermissions
				// here to request the missing permissions, and then overriding
				//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
				//                                          int[] grantResults)
				// to handle the case where the user grants the permission. See the documentation
				// for ActivityCompat#requestPermissions for more details.
				return;
			}
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, Global.MIN_TIME_FOR_LOCATION_UPDATES, Global.MIN_DISTANCE_CHANGE_FOR_UPDATES, gpsLocationListener);

			NetworkLocationListener networkLocationListener = new NetworkLocationListener(this);
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, Global.MIN_TIME_FOR_LOCATION_UPDATES, Global.MIN_DISTANCE_CHANGE_FOR_UPDATES, networkLocationListener);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void startMainService() {
		if (Utils.isServiceExisted(this, Global.SERVICE_CLASS_NAME) != null) {
			Utils.printDebug(null, "MainService already exist.");
		} else {
			Utils.printDebug(null, "MainService created.");
			startService(new Intent(this, MainService.class));
		}
	}

	private void startAnimation() {
		float fromAlpha = 0;
		float toAlpha = 1;
		long duration = 3000;

		mLayoutSplash.setVisibility(View.VISIBLE);
		switch (nIdx) {
			case 0:
				fromAlpha = 0;
				toAlpha = 0;
				duration = 1000;
				break;
			case 1:
				fromAlpha = 0;
				toAlpha = 1;
				duration = 4000;
				break;
			case 2:
				fromAlpha = 1;
				toAlpha = 1;
				duration = 1000;
				break;
			case 3:
				fromAlpha = 1;
				toAlpha = 0;
				duration = 2000;
				break;
		}

		AlphaAnimation alpha = new AlphaAnimation((float) fromAlpha, (float) toAlpha);
		alpha.setDuration(duration);

		alpha.setFillAfter(true);
		alpha.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				if (nIdx < MAX_SPLASH_ANIMATION_CNT) {
					startAnimation();
				} else {
					finishAndStartView();
				}
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}
		});
		mLayoutSplash.startAnimation(alpha);

		nIdx++;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		/*if(requestCode == 10051)
		{
			doCheckBatteryOptimization();
		}*/
		if(requestCode == 10001)
		{
			doCheckBatteryOptimization();
		}
		else if(requestCode == APP_STORAGE_ACCESS_REQUEST_CODE_UPDATE) {
			finishAndStartView();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void finishAndStartView() {
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED ||
				ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
				ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
				//ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
				//ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED ||
				ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED)  {
			// Should we show an explanation?
			ActivityCompat.requestPermissions(this,
					new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION,
							Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.CALL_PHONE, Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN},
					1001);
		}
		else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
				ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
				ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED))
		{
			ActivityCompat.requestPermissions(this,
					new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_ADVERTISE},
					1001);
		}
		else if(Build.VERSION.SDK_INT < Build.VERSION_CODES.R && (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED))
		{
			ActivityCompat.requestPermissions(this,
					new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
					1001);
		}
		else {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
				if (!Environment.isExternalStorageManager() && Utils.getBooleanSetting(SplashActivity.this, Global.PREF_PASS_CHECK_EXTERNAL_STORAGE) == false) {
					final MessageDialog dlg = new MessageDialog(SplashActivity.this);
					dlg.mStrMessage = String.format(Locale.ENGLISH, Str.AppDescription[Global.LANG]);
					dlg.mStrTitle = "";
					dlg.mStrLeft = Str.No[Global.LANG];
					dlg.mStrRight = Str.Yes[Global.LANG];
					dlg.setCancelable(false);
					dlg.setOnDismissListener(dialog -> {
						if (dlg.mReturnCode == Global.DLG_RETURN_RIGHT) {
							try {
								Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
										Uri.parse("package:" + BuildConfig.APPLICATION_ID));
								startActivityForResult(intent, APP_STORAGE_ACCESS_REQUEST_CODE_UPDATE);
							} catch (Exception e) {
								Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
								startActivityForResult(intent, APP_STORAGE_ACCESS_REQUEST_CODE_UPDATE);
							}
						}
						else {
							Utils.setBooleanSetting(SplashActivity.this, Global.PREF_PASS_CHECK_EXTERNAL_STORAGE, true);
							finishAndStartView();
						}
					});
					dlg.show();
					return;
				}
			}

			finish();

			Utils.setupExternalEnvironmentVariables();
			boolean bPassExternalStorage = Utils.getBooleanSetting(SplashActivity.this, Global.PREF_PASS_CHECK_EXTERNAL_STORAGE);

			File file = new File(Global.ROOT_LOCATION);
			if( !file.exists() )
			{
				//Remove all Share Preference values
				Utils.removeAllPreferneces(this);
				Utils.setBooleanSetting(this, Global.PREF_PASS_CHECK_EXTERNAL_STORAGE, bPassExternalStorage);
				file.mkdirs();
			}

//			startLocationListeners();
			if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
			{
				JobScheduler jobScheduler =
						(JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
				assert jobScheduler != null;
				jobScheduler.schedule(new JobInfo.Builder(LocationJobService.LOCATION_SERVICE_JOB_ID,
						new ComponentName(this, LocationJobService.class))
						.setOverrideDeadline(500)
						.setRequiresCharging(false)
						.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
						.setPersisted(true)
						.setRequiresDeviceIdle(false)
						.build());

				if(Utils.mMainThread == null || !Utils.mMainThread.isAlive() || Utils.mMainThread.isInterrupted())
				{
					if(Utils.mMainThread != null) Utils.mMainThread.isRunning = false;
					jobScheduler.schedule(new JobInfo.Builder(MainJobService.MAIN_SERVICE_JOB_ID,
							new ComponentName(this, MainJobService.class))
							.setOverrideDeadline(500)
							.setRequiresCharging(false)
							.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
							.setPersisted(true)
							.setRequiresDeviceIdle(false)
							.build());
				}
			}
			else
			{
				Utils.startLocationUpdates(getApplicationContext());
				startMainService();
			}

			startActivity(new Intent(this, LoginActivity.class));
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode,
										   String permissions[], int[] grantResults) {
		switch (requestCode) {
			case 1001: {
				// If request is cancelled, the result arrays are empty.
				if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED ||
						ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
						ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
						//ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
						//ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED ||
						ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

					finish();
				} else {
					if(Build.VERSION.SDK_INT < Build.VERSION_CODES.R)
					{
						if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
						{
							finish();
							return;
						}
					}

					if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
					{
						if(ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
								ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
								ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED)
						{
							finish();
							return;
						}
					}
					finishAndStartView();
				}
				return;
			}
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_UP) {
			finishAndStartView();
		}
		return false;
	}


	private boolean isNotificationServiceEnabled(){
		String pkgName = getPackageName();
		final String flat = Settings.Secure.getString(getContentResolver(),
				"enabled_notification_listeners");
		if (!TextUtils.isEmpty(flat)) {
			final String[] names = flat.split(":");
			for (String name: names) {
				final ComponentName cn = ComponentName.unflattenFromString(name);
				if (cn != null) {
					if (TextUtils.equals(pkgName, cn.getPackageName())) {
						return true;
					}
				}
			}
		}
		return false;
	}
}