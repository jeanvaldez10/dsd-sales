package dsd.dsdvansales.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.InputType;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.users.model.QBUser;
import com.stripe.android.PaymentConfiguration;

import org.apache.commons.net.ftp.FTPClient;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import dsd.dsdvansales.BuildConfig;
import dsd.dsdvansales.R;
import dsd.dsdvansales.activities.MainActivity;
import dsd.dsdvansales.activities.SplashActivity;
import dsd.dsdvansales.api.ApiClient;
import dsd.dsdvansales.api.ConnectApiClient;
import dsd.dsdvansales.api.TourApiClient;
import dsd.dsdvansales.api.UploadApiClient;
import dsd.dsdvansales.chat.ChatHelper;
import dsd.dsdvansales.db.DbAdapter;
import dsd.dsdvansales.db.DownloadDbAdapter;
import dsd.dsdvansales.dialogs.InputDialog;
import dsd.dsdvansales.dialogs.MessageDialog;
import dsd.dsdvansales.dialogs.UpdateDriverDialog;
import dsd.dsdvansales.global.DownloadEngine;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.MyIniFile;
import dsd.dsdvansales.global.OTPUtils;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.model.BugSeeResult;
import dsd.dsdvansales.model.CustomerInfo;
import dsd.dsdvansales.model.CustomerNoteInfo;
import dsd.dsdvansales.model.CustomerNoteResponse;
import dsd.dsdvansales.model.DescTypeInfo;
import dsd.dsdvansales.model.EquipmentInfo;
import dsd.dsdvansales.model.FileInfo;
import dsd.dsdvansales.model.GPSPath;
import dsd.dsdvansales.model.InstallerVersionInfo;
import dsd.dsdvansales.model.LoginInfo;
import dsd.dsdvansales.model.LoginResult;
import dsd.dsdvansales.model.Product;
import dsd.dsdvansales.model.ProductList;
import dsd.dsdvansales.model.RouteVersionDetailInfo;
import dsd.dsdvansales.model.RouteVersionInfo;
import dsd.dsdvansales.model.TransactionInfo;
import dsd.dsdvansales.model.TripResponse;
import dsd.dsdvansales.model.TripUser;
import dsd.dsdvansales.parsers.ParseXMLFile;
import dsd.dsdvansales.services.DownloadJobService;
import dsd.dsdvansales.views.AlertSingleChoiceAdapter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginFragment extends BaseLoginFragment implements View.OnClickListener {

	private static final int SHOW_CONFIRM_ALERT	= 1;
	private static final int DOWNLOAD_TRIPDATA	= 2;
	private static final int BYPASS_TRIPDATA	= 3;

	private static final int MSG_TYPE_NORMAL			= 1;
	private static final int MSG_TYPE_NO_DATABASE		= 2;
	private static final int MSG_TYPE_INVALID_SIGNON	= 3;
	private static final int MSG_TYPE_NO_CONNECTION		= 4;
	private static final int MSG_TYPE_LOAD_DATA			= 5;
	private static final int MSG_TYPE_REFRESH_DATA		= 6;
	private static final int MSG_TYPE_REFRESH_FAIL		= 7;

	public int INSTALL_UPGRADE = 1002;

	private int mDownloadType = SHOW_CONFIRM_ALERT;
	private String mStrFTPServerIP = "";
	private String mStrFTPCompanyName = "";
	private String mStrFTPUserName = "";
	private String mStrFTPPassword = "";

	private boolean bDownloaded = false;

	//private long mStartOfDayTrxnNo = -1;
	Spinner mSpinUserName;
	List<TripUser> mTripUserList = null;
	TripUser mSelTripUser = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//		try {
//			PackageInfo packageInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
//			String versionName = packageInfo.versionName; // e.g., "1.0.0"
//			Utils.generateLiterals(versionName);
//		}
//		catch (Exception ex) {}
//		String secretKey = "ABCDEFGHIJKLMNLOPQR";
//		String otp = OTPUtils.generateTOTP(secretKey);
//		Utils.printDebug(mContext, "Generated OTP: " + otp);

		mRootView = inflater.inflate(R.layout.fragment_login, container, false);
		commonInit();
		Global.gLoggedIn = false;
		File imgFile = new File(Global.DEFAULT_COMPANY_SPLASH_FILEPATH);
		Bitmap splashBitmap = null;
		if(imgFile.exists()){
			splashBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
		}
		if( splashBitmap != null )
		{
			((ImageView)mRootView.findViewById(R.id.logo)).setImageBitmap(splashBitmap);
			mRootView.findViewById(R.id.background).setBackgroundColor(mContext.getResources().getColor(R.color.transparent_color));
		}
		if(Utils.isMarkedUpgradeAsk())
		{
			new Handler(Looper.getMainLooper()).post(new Runnable() {
				@Override
				public void run() {
					if (mStrFTPServerIP.length() == 0 || mStrFTPCompanyName.length() == 0 || mStrFTPUserName.length() == 0 || mStrFTPPassword.length() == 0) {
						gotoConfiguration(true);
					} else {
						String strTripNumber = ((EditText) mRootView.findViewById(R.id.editTripNumber)).getText().toString();
						Global.gTripNumber = strTripNumber;
						int nType;
						if(Utils.isMarkedFromEndTrip())
						{
							nType = 0;
							Utils.setBooleanSetting(mContext, Global.PREF_END_VISIT_PLAN, true);
                            Utils.leaveLog(mContext, "Login_Upgrade", null, "END_VISIT_PLAN was set true");
						}
						else
						{
							nType = 1;
						}
						Utils.unmarkUpgradeAsk();
						doDownloadTask(nType);
					}
				}
			});

		}
		return mRootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		Global.gFragIdx = Global.FRAG_LOGIN;
	}
	
	@Override
	public void initView() {
		mRootView.findViewById(R.id.btnExit).setOnClickListener(this);
		mRootView.findViewById(R.id.btnLogin).setOnClickListener(this);

		((EditText) mRootView.findViewById(R.id.editTripNumber)).setOnEditorActionListener(new EditText.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					doLogin();
					return true;
				}
				return false;
			}
		});

		mSpinUserName = (Spinner)mRootView.findViewById(R.id.spinUserName);

		// Test code for Google Routes(New) API
//		Location location1 = new Location("gps");
//		location1.setLatitude(-33.801623);
//		location1.setLongitude(151.173144);
//		Location location2 = new Location("gps");
//		location2.setLatitude(-33.824943);
//		location2.setLongitude(151.186190);
//		Utils.getRoutePath(location1, location2);

		loadConf();
		loadData();
		refreshPage();
		refreshLanguage();
	}

	@Override
	public void loadData() {
		mStrFTPServerIP = Utils.getStringSetting(mContext, Global.PREF_FTP_SERVER_IP);
		mStrFTPCompanyName = Utils.getStringSetting(mContext, Global.PREF_FTP_COMPANY_NAME);
		mStrFTPUserName = Utils.getStringSetting(mContext, Global.PREF_FTP_USERNAME);
		mStrFTPPassword = Utils.getStringSetting(mContext, Global.PREF_FTP_PASSWORD);

		String strTripNumber = Utils.getStringSetting(mContext, Global.PREF_TRIP_NUMBER);
		String strGlobalTripNumber = Global.gTripNumber;

		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();

		if(Utils.getStringSetting(mContext, Global.PREF_TRIP_USER).equals("3"))
		{
			mTripUserList = new ArrayList<>();
			List<TripUser> tripUserList = dbAdapter.getTripUserList();
			//Remove the Exclude Trips from the list.
			String[] excludeTrips = Utils.getStringSetting(mContext, Global.PREF_EXCLUDES_TRIPS).split(",");
			Map<String, Boolean> excludeTripMap = new HashMap<>();
			for(String trip : excludeTrips)
			{
				excludeTripMap.put(trip.trim(), true);
			}
			for(TripUser tripUser : tripUserList)
			{
				if(excludeTripMap.get(tripUser.strTrip) == null || !excludeTripMap.get(tripUser.strTrip))
				{
					mTripUserList.add(tripUser);
				}
			}
			Collections.sort(mTripUserList, (tripUser, t1) -> {
				return tripUser.strDescription.compareTo(t1.strDescription);
			});
		}
		else if (Utils.getStringSetting(mContext, Global.PREF_TRIP_USER).equals("6")) {
			mTripUserList = dbAdapter.getTripUserList();
			TripUser theTripUser = null;
			for (TripUser tripUser: mTripUserList) {
				if (tripUser.strTrip.equals(strTripNumber)) {
					theTripUser = tripUser;
					break;
				}
			}

			mTripUserList.clear();
			if (theTripUser != null) {
				mTripUserList.add(theTripUser);
			}
		}
		else if(!Utils.getStringSetting(mContext, Global.PREF_TRIP_USER).equals("4") && !Utils.getStringSetting(mContext, Global.PREF_TRIP_USER).equals("5"))
		{
			mTripUserList = dbAdapter.getTripUserList();

			Collections.sort(mTripUserList, (tripUser, t1) -> {
				int nTrip1 = Utils.parseIntFromString(tripUser.strTrip);
				int nTrip2 = Utils.parseIntFromString(t1.strTrip);
				return nTrip1 - nTrip2;
			});
		}

		dbAdapter.close();
		if( mTripUserList == null  || mTripUserList.size() == 0 )
		{
			mRootView.findViewById(R.id.layout_spinner).setVisibility(View.GONE);
			mRootView.findViewById(R.id.editUsername).setVisibility(View.VISIBLE);
			((EditText)mRootView.findViewById(R.id.editPinNumber)).setImeOptions(EditorInfo.IME_ACTION_NEXT);
			mRootView.findViewById(R.id.editTripNumber).setEnabled(true);
		}
		else
		{
			if (Utils.getStringSetting(mContext, Global.PREF_TRIP_USER).equals("6")) {
				EditText myEditText = mRootView.findViewById(R.id.editUsername);
				myEditText.setVisibility(View.VISIBLE);
				mRootView.findViewById(R.id.layout_spinner).setVisibility(View.GONE);
				myEditText.setFocusable(false);
				myEditText.setClickable(false);
				myEditText.setCursorVisible(false);
				myEditText.setLongClickable(false);

				myEditText.setText(mTripUserList.get(0).toString());
			}
			else {
				mRootView.findViewById(R.id.layout_spinner).setVisibility(View.VISIBLE);
				mRootView.findViewById(R.id.editUsername).setVisibility(View.GONE);
			}

			ArrayAdapter<TripUser> dataAdapter = new ArrayAdapter<>(mContext, R.layout.spiner_textview, mTripUserList);
			dataAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

			mSpinUserName.setAdapter(dataAdapter);
			mRootView.findViewById(R.id.editTripNumber).setEnabled(false);
			((EditText)mRootView.findViewById(R.id.editPinNumber)).setImeOptions(EditorInfo.IME_ACTION_DONE);
			mSpinUserName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){

				@Override
				public void onItemSelected(AdapterView<?> arg0, View arg1,
										   int arg2, long arg3) {
					try{
						mSelTripUser = mTripUserList.get(arg2);
						if( mSelTripUser != null )
						{
							((EditText)mRootView.findViewById(R.id.editTripNumber)).setText(mSelTripUser.strTrip);

							if(mSelTripUser.strUserName.equals("Supervisor") && mSelTripUser.strDescription.equals("Supervisor"))
							{
								((EditText)mRootView.findViewById(R.id.editTripNumber)).setTextColor(mContext.getResources().getColor(R.color.gray_color));
							}
							else
							{
								((EditText)mRootView.findViewById(R.id.editTripNumber)).setTextColor(mContext.getResources().getColor(R.color.black_color));
							}
						}
					}catch(Exception e)
					{
						e.printStackTrace();
					}

				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
					// TODO Auto-generated method stub
					if( mTripUserList != null && mTripUserList.size() > 0 )
					{
						mSpinUserName.setSelection(0);
					}
				}

			});

			String strUserName = Utils.getStringSetting(mContext, Global.PREF_USER_LOGIN_DESCRIPTION);
			if(Utils.getBooleanSetting(mContext, Global.PREF_LOGIN_AS_SUPER_VISOR))
			{
				strUserName = "Supervisor";
			}
			if( strUserName != null && strUserName.length() > 0 )
			{
				boolean bAdd = false;
				for(int i = 0; i < mTripUserList.size(); i++){
					//if( strUserName.equals(mTripUserList.get(i).strDescription) )
					if(mTripUserList.get(i).strTrip.equals(Global.gTripNumber))
					{
						mSpinUserName.setSelection(i);
						mSelTripUser = mTripUserList.get(i);
						bAdd = true;
						break;
					}
				}
				if( !bAdd )
				{
					mSpinUserName.setSelection(0);
					mSelTripUser = mTripUserList.get(0);
				}
			}
			else
			{
				mSpinUserName.setSelection(0);
				mSelTripUser = mTripUserList.get(0);
			}
			if (Utils.getStringSetting(mContext, Global.PREF_TRIP_USER).equals("6")) {
				((EditText)mRootView.findViewById(R.id.editTripNumber)).setText(mSelTripUser.strTrip);
			}
		}

		Utils.loadLang(mContext);
	}

	@Override
	public void refreshPage() {
		if (Global._DEBUG_ == true) {
			((EditText) mRootView.findViewById(R.id.editUsername)).setText(Utils.getStringSetting(mContext, Global.PREF_LOGIN_USER_NAME));
			((EditText) mRootView.findViewById(R.id.editPinNumber)).setText(Utils.getStringSetting(mContext, Global.PREF_LOGIN_PIN_NUMBER));
			((EditText) mRootView.findViewById(R.id.editTripNumber)).setText(Utils.getStringSetting(mContext, Global.PREF_TRIP_NUMBER));
		}
	}

	@Override
	public void refreshLanguage() {
		((TextView) mRootView.findViewById(R.id.lblLogin)).setText(Str.LOG_IN[Global.LANG]);
		((Button) mRootView.findViewById(R.id.btnExit)).setText(Str.Exit[Global.LANG]);
		((Button) mRootView.findViewById(R.id.btnLogin)).setText(Str.Log_In[Global.LANG]);
		((EditText) mRootView.findViewById(R.id.editUsername)).setHint(Str.Username[Global.LANG]);
		((EditText) mRootView.findViewById(R.id.editPinNumber)).setHint(Str.PinNumber[Global.LANG]);
		((EditText) mRootView.findViewById(R.id.editTripNumber)).setHint(Str.TripNumber[Global.LANG]);
	}

	@Override
	public void receiveBroadcast(String strAction, Intent intent) {

	}

	private void loadConf() {
		if( Utils.getIntSetting(mContext, Global.PREF_CONFIG_STATUS) == Global.CONF_NOT_LOADED )
		{
			Utils.fileDelete(Global.ROOT_DATABASE_LOCATION);
		}

		if (Utils.getIntSetting(mContext, Global.PREF_CONFIG_STATUS) == Global.CONF_NOT_LOADED ||
				Utils.getStringSetting(mContext, Global.PREF_LOGIN_USER_NAME).equals("admin"))
		{
			Utils.setIntSetting(mContext, Global.PREF_CONFIG_STATUS, Global.CONF_NOT_EXIST);
			Utils.fileDelete(Global.ROOT_DATABASE_LOCATION);

			if(Build.VERSION.SDK_INT < Build.VERSION_CODES.R || Environment.isExternalStorageManager())
			{
				if (Utils.isFileExist(Global.CONF_PATH) == true) {
					MyIniFile iniFile = new MyIniFile(Global.CONF_PATH);
					String strUserName = iniFile.getString(Global.SECTION_ACCOUNT, Global.KEY_USER_NAME, "");
					String strPINNumber = iniFile.getString(Global.SECTION_ACCOUNT, Global.KEY_PIN_NUMBER, "");
					//String strTripNumber = iniFile.getString(Global.SECTION_ACCOUNT, Global.KEY_TRIP_NUMBER, "");
					String strTripNumber = Utils.readTripInfo();
					if(TextUtils.isEmpty(strTripNumber))
					{
						strTripNumber = iniFile.getString(Global.SECTION_ACCOUNT, Global.KEY_TRIP_NUMBER, "");
					}
					String strServerIP = iniFile.getString(Global.SECTION_FTP, Global.KEY_SERVER_IP, "");
					String strCompanyName = iniFile.getString(Global.SECTION_FTP, Global.KEY_COMPANY_NAME, "");
					String strFTPName = iniFile.getString(Global.SECTION_FTP, Global.KEY_USER_NAME, "");
					String strPassword = iniFile.getString(Global.SECTION_FTP, Global.KEY_PASSWORD, "");
					String strCompanyId = iniFile.getString(Global.SECTION_INSTALL, Global.KEY_COMPANY_ID, "");
					String strCompanyPin = iniFile.getString(Global.SECTION_INSTALL, Global.KEY_COMPANY_PIN, "");

					((EditText) mRootView.findViewById(R.id.editUsername)).setText(strUserName);
					((EditText) mRootView.findViewById(R.id.editPinNumber)).setText(strPINNumber);
					((EditText) mRootView.findViewById(R.id.editTripNumber)).setText(strTripNumber);
					Utils.setStringSetting(mContext, Global.PREF_FTP_SERVER_IP, strServerIP);
					UploadApiClient.API_ROOT = "http://" + strServerIP;
					Utils.setStringSetting(mContext, Global.PREF_FTP_COMPANY_NAME, strCompanyName);
					Utils.setStringSetting(mContext, Global.PREF_FTP_USERNAME, strFTPName);
					Utils.setStringSetting(mContext, Global.PREF_FTP_PASSWORD, strPassword);
					Utils.setStringSetting(mContext, Global.PREF_COMPANY_ID, strCompanyId);
					Utils.setStringSetting(mContext, Global.PREF_COMPNAY_PIN, strCompanyPin);

					Utils.setIntSetting(mContext, Global.PREF_CONFIG_STATUS, Global.CONF_GOT_LOADED);
				}
			}
			else {
				final MessageDialog dlg = new MessageDialog(mContext);
				dlg.mStrMessage = String.format(Locale.ENGLISH, Str.CannotUseDownloadFolder[Global.LANG]);
				dlg.mStrTitle = "";
				dlg.mStrRight = Str.Yes[Global.LANG];
				dlg.setCancelable(false);
				dlg.setOnDismissListener(dialog -> {
					if (dlg.mReturnCode == Global.DLG_RETURN_RIGHT) {
						gotoConfiguration(false);
					}
				});
				dlg.show();
			}
		}
		else
		{
			if(TextUtils.isEmpty(Utils.getStringSetting(mContext, Global.PREF_LOGIN_USER_NAME)))
			{
				if (Utils.isFileExist(Global.CONF_PATH) == true){
					MyIniFile iniFile = new MyIniFile(Global.CONF_PATH);
					String strUserName = iniFile.getString(Global.SECTION_ACCOUNT, Global.KEY_USER_NAME, "");
					String strPINNumber = iniFile.getString(Global.SECTION_ACCOUNT, Global.KEY_PIN_NUMBER, "");
					String strTripNumber = Utils.readTripInfo();
					if(TextUtils.isEmpty(strTripNumber))
					{
						strTripNumber = iniFile.getString(Global.SECTION_ACCOUNT, Global.KEY_TRIP_NUMBER, "");
					}
					((EditText) mRootView.findViewById(R.id.editUsername)).setText(strUserName);
					((EditText) mRootView.findViewById(R.id.editPinNumber)).setText(strPINNumber);
					((EditText) mRootView.findViewById(R.id.editTripNumber)).setText(strTripNumber);
				}
			}
		}
	}

	private void showMessageDialog(final int nMessageType, String strMessage) {
		try {
			final MessageDialog msgDialog = new MessageDialog(mContext);
			msgDialog.setCancelable(false);
			if (nMessageType == MSG_TYPE_NORMAL) {
				msgDialog.mStrTitle = Str.Alert[Global.LANG].toUpperCase();
				msgDialog.mStrMessage = strMessage;
				msgDialog.mStrMiddle = Str.Ok[Global.LANG];
			} else if (nMessageType == MSG_TYPE_NO_DATABASE) {
				msgDialog.mStrTitle = Str.NO_DATA[Global.LANG].toUpperCase();
				msgDialog.mStrMessage = Str.This_is_the_first_time_DSD_Delivery_has_been_used__[Global.LANG];
				msgDialog.mStrLeft = Str.Exit[Global.LANG];
				msgDialog.mStrRight = Str.Load_New[Global.LANG];
			} else if (nMessageType == MSG_TYPE_INVALID_SIGNON) {
				msgDialog.mStrTitle = Str.INVALID_SIGN_ON[Global.LANG];
				msgDialog.mStrMessage = Str.User_name_or_Password_is_invalid[Global.LANG];
				msgDialog.mStrMiddle = Str.Return[Global.LANG];
			} else if (nMessageType == MSG_TYPE_NO_CONNECTION) {
				msgDialog.mStrTitle = Str.NO_DATA_CONNECTION[Global.LANG];
				msgDialog.mStrMessage = Str.Unable_to_get_a_valid_connection__retry[Global.LANG];
				msgDialog.mStrLeft = Str.No[Global.LANG];
				msgDialog.mStrRight = Str.Yes[Global.LANG];
			} else if (nMessageType == MSG_TYPE_LOAD_DATA) {
				msgDialog.mStrTitle = Str.NO_DATA[Global.LANG];
				msgDialog.mStrMessage = String.format(Locale.ENGLISH, Str.Trip_S_IsNotLoaded__LoadItNow[Global.LANG], Global.gTripNumber);
				msgDialog.mStrLeft = Str.No[Global.LANG];
				msgDialog.mStrRight = Str.Yes[Global.LANG];
			} else if (nMessageType == MSG_TYPE_REFRESH_DATA) {
				msgDialog.mStrTitle = Str.Refresh_Data[Global.LANG];
				msgDialog.mStrMessage = Str.RefreshData_Confirm[Global.LANG];
				msgDialog.mStrLeft = Str.No[Global.LANG];
				msgDialog.mStrRight = Str.Yes[Global.LANG];
			} else if (nMessageType == MSG_TYPE_REFRESH_FAIL) {
				msgDialog.mStrTitle = Str.Refresh_Data[Global.LANG];
				msgDialog.mStrMessage = Str.Refresh_Failed_Continue[Global.LANG];
				msgDialog.mStrLeft = Str.No[Global.LANG];
				msgDialog.mStrRight = Str.Yes[Global.LANG];
				msgDialog.mIsFailed = true;
			}
			msgDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
					if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
						if (nMessageType == MSG_TYPE_NO_DATABASE || nMessageType == MSG_TYPE_REFRESH_FAIL) {
							mContext.finish();
						} else if (nMessageType == MSG_TYPE_REFRESH_DATA) {
							gotoPreTripTasks();
						}
					} else if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
						if (nMessageType == MSG_TYPE_NO_DATABASE || nMessageType == MSG_TYPE_NO_CONNECTION || nMessageType == MSG_TYPE_LOAD_DATA) {
							if (Utils.isConnectingToInternet(mContext) == false) {
								showMessageDialog(MSG_TYPE_NO_CONNECTION, "");
							} else {
								//loadData();
								if (mStrFTPServerIP.length() == 0 || mStrFTPCompanyName.length() == 0 || mStrFTPUserName.length() == 0 || mStrFTPPassword.length() == 0) {
									gotoConfiguration(true);
								} else {
									int nType = 0;
									if (nMessageType == MSG_TYPE_LOAD_DATA) {
										nType = 1;
									}
									doDownloadTask(nType);
								}
							}
						} else if (nMessageType == MSG_TYPE_REFRESH_DATA) {
							if(TextUtils.isEmpty(Utils.getStringSetting(mContext, Global.PREF_REFRESH_PIN_NUMBER)))
							{
								LoadTripData();
							}
							else
							{
								try {
									final InputDialog inputDlg = new InputDialog(mContext);
									inputDlg.mStrTitle = Str.PIN_CODE[Global.LANG];
									inputDlg.mStrHint = Str.PIN_code_for_communication[Global.LANG];
									inputDlg.mStrLeft = Str.Enter[Global.LANG];
									inputDlg.mStrRight = Str.Return[Global.LANG];
									inputDlg.mInputType = InputType.TYPE_CLASS_NUMBER;
									inputDlg.setCancelable(false);
									inputDlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
										@Override
										public void onDismiss(DialogInterface dialog) {
											if (inputDlg.mReturnCode == Global.DLG_RETURN_LEFT) {
												String strPINcode = Utils.getStringSetting(mContext, Global.PREF_REFRESH_PIN_NUMBER);
												if (strPINcode.equals(inputDlg.mStrInputed) == true) {
													LoadTripData();
												} else {
													try {
														final MessageDialog msgDialog = new MessageDialog(mContext);
														msgDialog.mStrTitle = Str.Alert[Global.LANG].toUpperCase();
														msgDialog.mStrMessage = Str.Invalid_code[Global.LANG];
														msgDialog.mStrMiddle = Str.Ok[Global.LANG];
														msgDialog.setCancelable(true);
														msgDialog.show();
													}catch (Exception e)
													{
														e.printStackTrace();
													}
												}
											}
										}
									});
									inputDlg.show();
								}catch (Exception e)
								{
									e.printStackTrace();
								}

							}


						} else if (nMessageType == MSG_TYPE_REFRESH_FAIL) {
							gotoPreTripTasks();
						}
					}
				}
			});
			msgDialog.show();
		}catch (Exception e)
		{
			e.printStackTrace();
		}


	}

	private void LoadTripData() {
		if (Utils.isConnectingToInternet(mContext) == true) {
			doDownloadTask(1);
		} else {
			showMessageDialog(MSG_TYPE_REFRESH_FAIL, "");
		}
	}

	public void continueChecking(final int nMessageType)
	{
		int nLoginCount = Utils.getLoginCount();
		if(nLoginCount > 0)
		{
			performLogin(nMessageType);
		}
		else
		{
			new Thread(){
				@Override
				public void run() {
					new Handler(Looper.getMainLooper()).post(new Runnable() {
						@Override
						public void run() {
							try
							{
								mContext.showProgressDialog(Str.checking_new_version[Global.LANG]);
							}catch (Exception e)
							{
								e.printStackTrace();
							}
						}
					});
					checkUsingNewHTTPServer(nMessageType);
				}
			}.start();
		}
	}

	public boolean downloadInstallerApp()
	{
		try {
			String downloadUrl = "https://installer.dsdassist.com/dsd/index.php/Product/downloadInstaller";
			String strTargetPath = Global.DOWNLOAD_INSTALLER_APK_PATH;
			URL url = new URL(downloadUrl);
			HttpURLConnection c = (HttpURLConnection) url.openConnection();

			c.setRequestMethod("GET");
			c.setDoOutput(true);

			c.connect();

			File file = new File(strTargetPath);
			if(file.exists())
			{
				file.delete();
			}

			FileOutputStream f = new FileOutputStream(file);
			InputStream in = new BufferedInputStream(url.openStream(), 4096);
			byte[] buffer = new byte[1024 * 1000];
			int len;
			while ((len = in.read(buffer)) > 0) {
				f.write(buffer, 0, len);
			}
			try
			{
				f.flush();
				f.close();
				in.close();
				c.disconnect();
			}catch (Exception e)
			{
				e.printStackTrace();
				return false;
			}
		}catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
		return true;
	}

	Uri getFileUri(Context context, File file) {
		return FileProvider.getUriForFile(context,
				"dsd.dsdvansales.fileprovider"
				, file);
	}

	public void doInstallInstallerApp()
	{
		try
		{
			File file = new File(Global.DOWNLOAD_INSTALLER_APK_PATH);
			Intent intent;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
				Uri downloaded_apk = getFileUri(mContext, file);
				intent = new Intent(Intent.ACTION_VIEW).setDataAndType(downloaded_apk,
						"application/vnd.android.package-archive");
				intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

			} else {
				intent = new Intent(Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.fromFile(file),
						"application/vnd.android.package-archive");
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			}
			intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
			startActivityForResult(intent, INSTALL_UPGRADE);
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	int mMessageType = 0;
	public void doDownloadInstallerApk(final int nMessageType)
	{
		mContext.showProgressDialog(Str.Downloading[Global.LANG]);
		new Thread()
		{
			@Override
			public void run() {
				boolean bSuccess = downloadInstallerApp();
				if(bSuccess)
				{
					new Handler(Looper.getMainLooper()).post(() -> {
						mContext.hideProgressDialog();
						mMessageType = nMessageType;
						doInstallInstallerApp();
					});
				}
				else {
					new Handler(Looper.getMainLooper()).post(() -> {
						mContext.hideProgressDialog();

						final MessageDialog msgDialog = new MessageDialog(mContext);
						msgDialog.setCancelable(false);
						msgDialog.mStrTitle = Str.Warning[Global.LANG];
						msgDialog.mStrMessage = Str.FailedToDownloadApkFile[Global.LANG];
						msgDialog.mStrLeft = Str.No[Global.LANG];
						msgDialog.mStrRight = Str.Yes[Global.LANG];
						msgDialog.setOnDismissListener(dialog -> {
							if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
								doDownloadInstallerApk(nMessageType);
							} else {
								continueChecking(nMessageType);
							}
						});
						msgDialog.show();

					});

				}
			}
		}.start();
	}

	public void checkDownload(final int nMessageType)
	{
		final PackageInfo info = Utils.isInstalledInstallerApp(mContext);
		if(info == null)
		{
			performLogin(nMessageType);
			return;
		}

		//Check Installer version is latest one or not.
		mContext.showProgressDialog(Str.Checking[Global.LANG]);
		TourApiClient.getApiClient().getLatestInstallerVersion(new HashMap<>()).enqueue(new Callback<InstallerVersionInfo>() {
			@Override
			public void onResponse(Call<InstallerVersionInfo> call, Response<InstallerVersionInfo> response) {
				if(response.isSuccessful() && response.body() != null && !response.body().version.equals(info.versionName)) {
					mContext.hideProgressDialog();

					final MessageDialog msgDialog = new MessageDialog(mContext);
					msgDialog.setCancelable(false);
					msgDialog.mStrTitle = Str.DSDNewInstallerVersionExists[Global.LANG];
					msgDialog.mStrMessage = Str.A_new_version_of_your_DSD_installer_application[Global.LANG];
					msgDialog.mStrLeft = Str.No[Global.LANG];
					msgDialog.mStrRight = Str.Yes[Global.LANG];
					msgDialog.setOnDismissListener(dialog -> {
						if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
							doDownloadInstallerApk(nMessageType);

						} else {
							continueChecking(nMessageType);
						}
					});
					msgDialog.show();

				} else {
					mContext.hideProgressDialog();
					continueChecking(nMessageType);
				}
			}

			@Override
			public void onFailure(Call<InstallerVersionInfo> call, Throwable t) {
				mContext.hideProgressDialog();
				continueChecking(nMessageType);
			}
		});
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

		if( requestCode == INSTALL_UPGRADE )
		{
			continueChecking(mMessageType);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void checkUsingNewHTTPServer(final int nMessageType)
	{
		// Login
		Map<String, String> params = new HashMap<>();
		params.put("COMPANY_ID", Utils.getStringSetting(mContext, Global.PREF_COMPANY_ID));
		params.put("PIN", Utils.getStringSetting(mContext, Global.PREF_COMPNAY_PIN));
		ApiClient.API_ROOT = Global.API_NEW_SERVER_ROOT;

		ApiClient.getApiClient().Login(params).enqueue(new retrofit2.Callback<LoginResult>() {
			@Override
			public void onResponse(Call<LoginResult> call, retrofit2.Response<LoginResult> response) {
				if( response != null )
				{
					if(response.body().result.equals("1"))
					{
						Global.API_TOKEN = response.body().token;
						checkUsingOldFTPServer(nMessageType);
					}
					else
					{
						ApiClient.API_ROOT = Global.API_OLD_SERVER_ROOT;
						checkUsingOldFTPServer(nMessageType);
					}
				}
				else
				{
					ApiClient.API_ROOT = Global.API_OLD_SERVER_ROOT;
					checkUsingOldFTPServer(nMessageType);
				}
			}

			@Override
			public void onFailure(Call<LoginResult> call, Throwable t) {
				new Handler(Looper.getMainLooper()).post(new Runnable() {
					@Override
					public void run() {
						mContext.hideProgressDialog();
						performLogin(nMessageType);
					}
				});
			}
		});
	}

	private void checkUsingOldFTPServer(final int nMessageType)
	{
		Map<String, String> params = new HashMap<>();
		params.put("COMPANY_ID", Utils.getStringSetting(mContext, Global.PREF_COMPANY_ID));
		params.put("PIN", Utils.getStringSetting(mContext, Global.PREF_COMPNAY_PIN));
		params.put("token", Global.API_TOKEN);
		ApiClient.getApiClient().GetProductList(params).enqueue(new retrofit2.Callback<ProductList>() {
			@Override
			public void onResponse(Call<ProductList> call, retrofit2.Response<ProductList> response) {
				if( response != null )
				{
					ProductList productList = response.body();
					Product currentProduct = null;
					for(Product product : productList.data)
					{
						if( product.SPEC.equals(mContext.getPackageName()) )
						{
							currentProduct = product;
							break;
						}
					}
					if( currentProduct == null )
					{
						if(ApiClient.API_ROOT.equals(Global.API_NEW_SERVER_ROOT))
						{
							ApiClient.API_ROOT = Global.API_OLD_SERVER_ROOT;
							checkUsingOldFTPServer(nMessageType);
						}
						else {
							new Handler(Looper.getMainLooper()).post(new Runnable() {
								@Override
								public void run() {
									mContext.hideProgressDialog();
									performLogin(nMessageType);
								}
							});
						}
					}
					else
					{
						try {
							final PackageInfo packageInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
							if(!currentProduct.VERSION_NUMBER.equals(packageInfo.versionName))
							{
								final Product finalCurrentProduct = currentProduct;
								new Handler(Looper.getMainLooper()).post(() -> {
									mContext.hideProgressDialog();
									try {
										final MessageDialog msgDialog = new MessageDialog(mContext);
										msgDialog.setCancelable(false);
										msgDialog.mStrTitle = Str.DSD_Version_Upgrade[Global.LANG];
										msgDialog.mStrMessage = Str.A_new_version_of_your_DSD_application[Global.LANG];
										msgDialog.mStrLeft = Str.No[Global.LANG];
										msgDialog.mStrRight = Str.Yes[Global.LANG];
										msgDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
											@Override
											public void onDismiss(DialogInterface dialog) {
												if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
													Utils.reportUpgradeStatus(Utils.getStringSetting(mContext, Global.PREF_FTP_COMPANY_NAME), Utils.getStringSetting(mContext, Global.PREF_LOGIN_USER_NAME), Global.gTripNumber, finalCurrentProduct.VERSION_NUMBER, packageInfo.versionName, true);
													mContext.upgradeNewVersion("Login");
												}
												else
												{
													Utils.reportUpgradeStatus(Utils.getStringSetting(mContext, Global.PREF_FTP_COMPANY_NAME), Utils.getStringSetting(mContext, Global.PREF_LOGIN_USER_NAME), Global.gTripNumber, finalCurrentProduct.VERSION_NUMBER, packageInfo.versionName, false);
													performLogin(nMessageType);
												}
											}
										});
										msgDialog.show();
									}catch (Exception e)
									{
										e.printStackTrace();
									}


								});
							}
							else
							{
								new Handler(Looper.getMainLooper()).post(new Runnable() {
									@Override
									public void run() {
										mContext.hideProgressDialog();
										performLogin(nMessageType);
									}
								});
							}
						} catch (PackageManager.NameNotFoundException e) {
							e.printStackTrace();
							new Handler(Looper.getMainLooper()).post(new Runnable() {
								@Override
								public void run() {
									mContext.hideProgressDialog();
									performLogin(nMessageType);
								}
							});
						}
					}
				}
				else
				{
					new Handler(Looper.getMainLooper()).post(new Runnable() {
						@Override
						public void run() {
							mContext.hideProgressDialog();
							performLogin(nMessageType);
						}
					});
				}
			}

			@Override
			public void onFailure(Call<ProductList> call, Throwable t) {
				new Handler(Looper.getMainLooper()).post(new Runnable() {
					@Override
					public void run() {
						mContext.hideProgressDialog();
						performLogin(nMessageType);
					}
				});
			}
		});
	}

	private void performLogin(int nMessageType)
	{
		//Because once user has clicked Login Button, we need to increase the login count.
		Utils.setLoginCount(Utils.getLoginCount() + 1);

		String strUserName = "";
		if( mTripUserList != null && mTripUserList.size() > 0 )
		{
			try
			{
				int nPosition = this.mSpinUserName.getSelectedItemPosition();
				TripUser user = mTripUserList.get(nPosition);
				strUserName = user.strDescription;
			}catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			strUserName = ((EditText) mRootView.findViewById(R.id.editUsername)).getText().toString();
		}

		String strPinNumber = ((EditText)mRootView.findViewById(R.id.editPinNumber)).getText().toString();
		String strTripNumber = ((EditText)mRootView.findViewById(R.id.editTripNumber)).getText().toString();

		if(Utils.getStringSetting(mContext, Global.PREF_TRIP_USER).equals("4") || Utils.getStringSetting(mContext, Global.PREF_TRIP_USER).equals("5"))
		{
			//Check the User validation
			String strApiCompany = Utils.getStringSetting(mContext, Global.PREF_APICOMPANY);
			String strBaseUrl = Utils.getBaseURL(strApiCompany);
			Map<String, String> params = new HashMap<>();
			params.put("username", strUserName.trim());
			params.put("password", strPinNumber);
			showProgressDialog(Str.Loading[Global.LANG]);
			String finalStrUserName = strUserName;
			ConnectApiClient.getMainApiClient(strBaseUrl, null).login(params).enqueue(new Callback<LoginInfo>() {
				@Override
				public void onResponse(Call<LoginInfo> call, Response<LoginInfo> response) {

					if(response.isSuccessful() && response.body() != null) {
						final LoginInfo body = response.body();
						Map<String, String> headers = new HashMap<>();
						headers.put("X-Auth-Token", body.token);
						String strToday = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
						ConnectApiClient.getMainApiClient(strBaseUrl, headers).getTripForToday(strToday).enqueue(new Callback<List<TripResponse>>() {
							@Override
							public void onResponse(Call<List<TripResponse>> call, Response<List<TripResponse>> response) {
								if(response.isSuccessful() && response.body() != null) {
									hideProgressDialog();
									List<TripResponse> tripResponseList = response.body();
									boolean bTripExist = false;
									TripResponse selRes = null;
									for(TripResponse res : tripResponseList)
									{
										if(String.valueOf(res.tripNumber).equals(strTripNumber.trim()))
										{
											selRes = res;
											bTripExist = true;
											break;
										}
									}

									if(bTripExist)
									{
										if(!selRes.driver.equals(finalStrUserName))
										{
											if(Utils.getStringSetting(mContext, Global.PREF_TRIP_USER).equals("4"))
											{
												try {
													final MessageDialog msgDialog = new MessageDialog(mContext);
													msgDialog.setCancelable(false);
													msgDialog.mStrMessage = Str.NotSignedForToday[Global.LANG];
													msgDialog.mStrLeft = Str.Yes[Global.LANG];
													msgDialog.mStrRight = Str.No[Global.LANG];
													msgDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
														@Override
														public void onDismiss(DialogInterface dialogInterface) {
															if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
																doDownloadTask(1);
															}
														}
													});
													msgDialog.show();
												}catch (Exception e)
												{
													e.printStackTrace();
												}
											}
											else if(Utils.getStringSetting(mContext, Global.PREF_TRIP_USER).equals("5"))
											{
												try {
													final MessageDialog msgDialog = new MessageDialog(mContext);
													msgDialog.setCancelable(false);
													msgDialog.mStrMessage = Str.NotSignedForTodaySuporvisor[Global.LANG];
													msgDialog.mStrMiddle = Str.Ok[Global.LANG];
													msgDialog.show();
												}catch (Exception e)
												{
													e.printStackTrace();
												}
											}
										} else {
											doNormalProcess(nMessageType, finalStrUserName, strPinNumber, strTripNumber);
										}
									}
									else {
										try {
											final MessageDialog msgDialog = new MessageDialog(mContext);
											msgDialog.setCancelable(false);
											msgDialog.mStrMessage = Str.NoTripDataForToday[Global.LANG];
											msgDialog.mStrMiddle = Str.Ok[Global.LANG];
											msgDialog.show();
										}catch (Exception e)
										{
											e.printStackTrace();
										}
									}
								}
								else {
									hideProgressDialog();
									try {
										final MessageDialog msgDialog = new MessageDialog(mContext);
										msgDialog.setCancelable(false);
										msgDialog.mStrMessage = Str.CannotConnectServer[Global.LANG];
										msgDialog.mStrMiddle = Str.Ok[Global.LANG];
										msgDialog.show();
									}catch (Exception e)
									{
										e.printStackTrace();
									}
								}
							}

							@Override
							public void onFailure(Call<List<TripResponse>> call, Throwable t) {
								hideProgressDialog();
								try {
									final MessageDialog msgDialog = new MessageDialog(mContext);
									msgDialog.setCancelable(false);
									msgDialog.mStrMessage = Str.NetworkConnectionError[Global.LANG];
									msgDialog.mStrMiddle = Str.Ok[Global.LANG];
									msgDialog.show();
								}catch (Exception e)
								{
									e.printStackTrace();
								}
							}
						});
					}
					else {
						hideProgressDialog();
						try {
							final MessageDialog msgDialog = new MessageDialog(mContext);
							msgDialog.setCancelable(false);
							msgDialog.mStrMessage = Str.User_name_or_Password_is_invalid[Global.LANG];
							msgDialog.mStrMiddle = Str.Ok[Global.LANG];
							msgDialog.show();
						}catch (Exception e)
						{
							e.printStackTrace();
						}
					}
				}

				@Override
				public void onFailure(Call<LoginInfo> call, Throwable t) {
					hideProgressDialog();
					try {
						final MessageDialog msgDialog = new MessageDialog(mContext);
						msgDialog.setCancelable(false);
						msgDialog.mStrMessage = Str.NetworkConnectionError[Global.LANG];
						msgDialog.mStrMiddle = Str.Ok[Global.LANG];
						msgDialog.show();
					}catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			});
		}
		else {
			doNormalProcess(nMessageType, strUserName, strPinNumber, strTripNumber);
		}

	}

	private void doNormalProcess(int nMessageType, String strUserName, String strPinNumber, String strTripNumber)
	{
		if (nMessageType == 0) {
			Utils.setStringSetting(mContext, Global.PREF_LOGIN_USER_NAME, strUserName);
			Utils.setStringSetting(mContext, Global.PREF_LOGIN_PIN_NUMBER, strPinNumber);
			Utils.setStringSetting(mContext, Global.PREF_TRIP_NUMBER, strTripNumber);
			if (mDownloadType == SHOW_CONFIRM_ALERT) {
				showMessageDialog(MSG_TYPE_REFRESH_DATA, "");
			} else if (mDownloadType == DOWNLOAD_TRIPDATA) {
				doDownloadTask(1);
			} else if (mDownloadType == BYPASS_TRIPDATA) {
				gotoPreTripTasks();
			}
		} else {
			showMessageDialog(nMessageType, "");
		}
	}

	private void doLogin() {

		String strUserName = "";
		if( mTripUserList != null && mTripUserList.size() > 0 )
		{
			try
			{
				int nPosition = mSpinUserName.getSelectedItemPosition();
				TripUser user = mTripUserList.get(nPosition);
				strUserName = user.strDescription;
			}catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			strUserName = ((EditText) mRootView.findViewById(R.id.editUsername)).getText().toString();
			if (strUserName.length() == 0) {
				Utils.printDebug(mContext, Str.Please_input_user_name[Global.LANG]);
				showKeyboard((EditText) mRootView.findViewById(R.id.editUsername), true);
				return;
			}
		}

		String strPinNumber = ((EditText) mRootView.findViewById(R.id.editPinNumber)).getText().toString();
		if (strPinNumber.length() == 0) {
			Utils.printDebug(mContext, Str.PleaseInputPinNumber[Global.LANG]);
			showKeyboard((EditText) mRootView.findViewById(R.id.editPinNumber), true);
			return;
		}
		String strTripNumber = ((EditText) mRootView.findViewById(R.id.editTripNumber)).getText().toString();
		if (strTripNumber.length() == 0) {
			Utils.printDebug(mContext, Str.PleaseInputTripNumber[Global.LANG]);
			showKeyboard((EditText) mRootView.findViewById(R.id.editTripNumber), true);
			return;
		}
		showKeyboard((EditText) mRootView.findViewById(R.id.editTripNumber), false);
		Global.gTripNumber = strTripNumber;

		if( mTripUserList != null && mTripUserList.size() > 0 )
		{
			try
			{
				int nPosition = this.mSpinUserName.getSelectedItemPosition();
				TripUser user = mTripUserList.get(nPosition);
				strUserName = user.strDescription;
			}catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			strUserName = ((EditText) mRootView.findViewById(R.id.editUsername)).getText().toString();
		}

		String strLoginUserName = Utils.getStringSetting(mContext, Global.PREF_LOGIN_USER_NAME);
		String strLoginPinNumber = Utils.getStringSetting(mContext, Global.PREF_LOGIN_PIN_NUMBER);
		String strLoginTripNumber = Utils.getStringSetting(mContext, Global.PREF_TRIP_NUMBER);

		if(strUserName.equals("Supervisor"))
		{
			strLoginPinNumber = Utils.getStringSetting(mContext, Global.PREF_SUPERVISOR_USER_PASSWORD);
		}

		final int nMessageType;

		if( mTripUserList != null && mTripUserList.size() > 0 )
		{
			int nPosition = this.mSpinUserName.getSelectedItemPosition();
			TripUser user = mTripUserList.get(nPosition);
			if( strPinNumber.equals(user.strSecurity) )
			{
				if (strLoginTripNumber.equals(strTripNumber) == true) {
					nMessageType = 0;
					if(Utils.getBooleanSetting(mContext, Global.PREF_LOGIN_AS_SUPER_VISOR) && !strUserName.equals("Supervisor"))
					{
						mDownloadType = DOWNLOAD_TRIPDATA;
						Utils.setBooleanSetting(mContext, Global.PREF_END_VISIT_PLAN, false);
                        Utils.leaveLog(mContext, "Login_Normal", null, "END_VISIT_PLAN was set false");
					}
					else
					{
						if (Utils.getBooleanSetting(mContext, Global.PREF_END_VISIT_PLAN) == true) {
							mDownloadType = DOWNLOAD_TRIPDATA;
							Utils.setBooleanSetting(mContext, Global.PREF_END_VISIT_PLAN, false);
                            Utils.leaveLog(mContext, "Login_Normal", null, "END_VISIT_PLAN was set false");
						}
					}
				} else {
					nMessageType = MSG_TYPE_LOAD_DATA;
				}
			}
			else
			{
				nMessageType = MSG_TYPE_INVALID_SIGNON;
			}
		}
		else{
			if (strLoginUserName.length() > 0 && strLoginPinNumber.length() > 0) {
				if (strLoginUserName.equals(strUserName) == true && strLoginPinNumber.equals(strPinNumber) == true) {
					if (strLoginTripNumber.equals(strTripNumber) == true) {
						nMessageType = 0;
						if (Utils.getBooleanSetting(mContext, Global.PREF_END_VISIT_PLAN) == true) {
							mDownloadType = DOWNLOAD_TRIPDATA;
							Utils.setBooleanSetting(mContext, Global.PREF_END_VISIT_PLAN, false);
                            Utils.leaveLog(mContext, "Login_NewUser", null, "END_VISIT_PLAN was set false");
						}
					} else {
						nMessageType = MSG_TYPE_LOAD_DATA;
					}
				} else if(Utils.getStringSetting(mContext, Global.PREF_TRIP_USER).equals("4") || Utils.getStringSetting(mContext, Global.PREF_TRIP_USER).equals("5")) {
					nMessageType = 0;
				} else {
					nMessageType = MSG_TYPE_INVALID_SIGNON;
				}
			} else {
				if (strUserName.equals("admin") == true && strPinNumber.equals("9999") == true) {
					nMessageType = MSG_TYPE_NO_DATABASE;
					mDownloadType = BYPASS_TRIPDATA;
				} else {
					nMessageType = MSG_TYPE_INVALID_SIGNON;
				}
			}
		}

		if(strUserName.equals("Supervisor") && nMessageType != MSG_TYPE_INVALID_SIGNON)
        {
			Utils.setStringSetting(mContext, Global.PREF_LOGIN_USER_NAME, strUserName);
			Utils.setBooleanSetting(mContext, Global.PREF_LOGIN_AS_SUPER_VISOR, true);
            gotoPreTripTasks();
        }
		else
        {
			Utils.setBooleanSetting(mContext, Global.PREF_LOGIN_AS_SUPER_VISOR, false);
            if(nMessageType == MSG_TYPE_INVALID_SIGNON)
            {
                showMessageDialog(nMessageType, "");
            }
            else
            {
				Utils.setStringSetting(mContext, Global.PREF_LOGIN_USER_NAME, strUserName);
				int nEndTripCnt = Utils.getLoginStatusCount(mContext, strTripNumber, 2);
				if(nEndTripCnt > 0)
				{
					if (Utils.getStringSetting(mContext, Global.PREF_DISABLE_AFTER_EOT).equals("1")) {
						try {
							final MessageDialog msgDialog = new MessageDialog(mContext);
							msgDialog.setCancelable(false);
							msgDialog.mStrMessage = Str.LoginEndTripCannotMsg[Global.LANG];
							msgDialog.mStrMiddle = Str.Return[Global.LANG];
							msgDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
								@Override
								public void onDismiss(DialogInterface dialog) {
								}
							});
							msgDialog.show();
						}catch (Exception e)
						{
							e.printStackTrace();
						}
					}
					else {
						try {
							final MessageDialog msgDialog = new MessageDialog(mContext);
							msgDialog.setCancelable(false);
							msgDialog.mStrMessage = Str.LoginEndTripWarningMsg[Global.LANG];
							msgDialog.mStrLeft = Str.Exit[Global.LANG];
							msgDialog.mStrRight = Str.Signon[Global.LANG];
							msgDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
								@Override
								public void onDismiss(DialogInterface dialog) {
									if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
										checkDownload(nMessageType);
									}
								}
							});
							msgDialog.show();
						}catch (Exception e)
						{
							e.printStackTrace();
						}
					}
				}
				else
				{
					checkDownload(nMessageType);
				}

            }
        }
	}

	private String mRouteApiToken = "";
	private void gotoPreTripTasks() {
		if(!TextUtils.isEmpty(Utils.getStringSetting(mContext, Global.PREF_COMPANY_NAME)))
		{
			new Thread()
			{
				@Override
				public void run() {
					Global.gCurQBUser = new QBUser();
					Global.gCurQBUser.setPassword("dsdproduct_" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_PIN_NUMBER));
					String strLoginUserName = Utils.getStringSetting(mContext, Global.PREF_COMPANY_NAME) + "_" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_USER_NAME);
					Global.gCurQBUser.setLogin(strLoginUserName.replace(" ", "+"));
					StringifyArrayList tags = new StringifyArrayList();
					tags.add(Utils.getStringSetting(mContext, Global.PREF_COMPANY_NAME));

					new Handler(Looper.getMainLooper()).post(new Runnable() {
						@Override
						public void run() {
							ChatHelper.getInstance().login(Global.gCurQBUser, new QBEntityCallback<Void>() {
								@Override
								public void onSuccess(Void aVoid, Bundle bundle) {
									if(!mContext.isFinishing() && !mContext.isDestroyed())
									{
										try {
											Toast.makeText(mContext, Str.SuccessfullyLoginToChat[Global.LANG], Toast.LENGTH_LONG).show();
										}catch (Exception e1)
										{
											e1.printStackTrace();
										}
									}

									Utils.registerChatService(mContext);
								}

								@Override
								public void onError(final QBResponseException e) {
									if(!mContext.isFinishing() && !mContext.isDestroyed())
									{
										try {
											Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
										}catch (Exception e1)
										{
											e1.printStackTrace();
										}
									}
								}
							});
						}
					});
				}
			}.start();
		}

		Global.gNeedToDownload = true;
		Global.gLoggedIn = true;

		if(Utils.getBooleanSetting(mContext, Global.PREF_PASSED_DOWNLOAD))
		{
			long nTrxnNo = setGeneralUploadData("PTTS", 0, 0, -1);

			TransactionInfo transactionInfo = new TransactionInfo();
			transactionInfo.strType = "PTTS";
			transactionInfo.strTime = new SimpleDateFormat("kk:mm").format(new Date(nTrxnNo));
			DbAdapter dbAdapter = new DbAdapter(mContext);
			dbAdapter.open();
			dbAdapter.insertTransactionHistoryInfo(transactionInfo);
			dbAdapter.close();

		}
		else
		{
			String strUserName = Utils.getStringSetting(mContext, Global.PREF_LOGIN_USER_NAME);

			//long nTrxnNo = setGeneralUploadData("LOG", 0, 0, -1, "Midday - " + strUserName);
			long nTrxnNo;
			if(!strUserName.equals("Supervisor"))
			{
				nTrxnNo = setGeneralUploadData("LOG", 0, 0, -1, "Midday DRVR");
			}
			else
			{
				nTrxnNo = setGeneralUploadData("LOG", 0, 0, -1, "Midday SUPVSR");
			}

			TransactionInfo transactionInfo = new TransactionInfo();
			transactionInfo.strType = "LOG";
			transactionInfo.strTime = new SimpleDateFormat("kk:mm").format(new Date(nTrxnNo));
			DbAdapter dbAdapter = new DbAdapter(mContext);
			dbAdapter.open();
			dbAdapter.insertTransactionHistoryInfo(transactionInfo);
			dbAdapter.close();
		}

		if(Utils.getStringSetting(mContext, Global.PREF_API_ROUTE_SCH).equals("1"))
		{
			final String strBaseUrl = Utils.getBaseURL(Utils.getStringSetting(mContext, Global.PREF_APICOMPANY));
			if(!TextUtils.isEmpty(strBaseUrl))
			{
				showProgressDialog(Str.LoadingVersionCodes[Global.LANG]);

				Map<String, String> params = new HashMap<>();
				params.put("username", Utils.getStringSetting(mContext, Global.PREF_APIUSER));
				params.put("password", Utils.getStringSetting(mContext, Global.PREF_APIPASSWORD));
				ConnectApiClient.getMainApiClient(strBaseUrl, null).login(params).enqueue(new Callback<LoginInfo>() {

					@Override
					public void onResponse(Call<LoginInfo> call, Response<LoginInfo> response) {
						if(response.isSuccessful() && response.body() != null) {
							final LoginInfo body = response.body();
							Map<String, String> headers = new HashMap<>();
							mRouteApiToken = body.token;
							headers.put("X-Auth-Token", body.token);
							//Get the Route Version Names
							Map<String, String> params = new HashMap<>();
							params.put("date", new SimpleDateFormat("dd-MM-yyy").format(new Date()));
							ConnectApiClient.getMainApiClient(strBaseUrl, headers).getRouteVersionNames(params).enqueue(new Callback<List<RouteVersionInfo>>() {
								@Override
								public void onResponse(Call<List<RouteVersionInfo>> call, Response<List<RouteVersionInfo>> response) {
									if(response.isSuccessful() && response.body() != null)
									{
										hideProgressDialog();
										showVersionCodeSelection(response.body());
									}
									else
									{
										hideProgressDialog();
										doProcessGotoPreTripTask();
									}
								}

								@Override
								public void onFailure(Call<List<RouteVersionInfo>> call, Throwable t) {
									hideProgressDialog();
									doProcessGotoPreTripTask();
								}
							});
						} else {
							hideProgressDialog();
							doProcessGotoPreTripTask();
						}

					}

					@Override
					public void onFailure(Call<LoginInfo> call, Throwable t) {
						hideProgressDialog();
						doProcessGotoPreTripTask();
					}
				});
			}
			else
			{
				doProcessGotoPreTripTask();
			}
		}
		else
		{
			doProcessGotoPreTripTask();
		}
	}

	private int nVersionCodeIdx = -1;
	private void showVersionCodeSelection(final List<RouteVersionInfo> versionInfoList)
	{
		final String strArray[] = new String[versionInfoList.size()];
		for (int i = 0; i < strArray.length; i++) {
			strArray[i] = versionInfoList.get(i).description;
		}
		try {
			new AlertDialog.Builder(mContext)
					.setTitle(Str.SelectRun[Global.LANG])
					.setSingleChoiceItems(new AlertSingleChoiceAdapter(mContext, strArray), nVersionCodeIdx, new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog, int nSelected)
						{
							nVersionCodeIdx = nSelected;
						}
					})
					.setPositiveButton(Str.Ok[Global.LANG], new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog, int nSelected)
						{
							if(nVersionCodeIdx < 0)
							{
								doProcessGotoPreTripTask();
								return;
							}
							RouteVersionInfo versionInfo = versionInfoList.get(nVersionCodeIdx);
							doLoadRouteVersionDetails(versionInfo);
						}
					})
					.setNegativeButton(Str.Cancel[Global.LANG], new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog, int nSelected)
						{
							doProcessGotoPreTripTask();
						}
					})
					.create()
					.show();
		}catch (Exception e)
		{
			e.printStackTrace();
		}

	}


	private int mRefreshCount = 0;
	private void doLoadRouteVersionDetails(final RouteVersionInfo versionInfo)
	{
		final String strBaseUrl = Utils.getBaseURL(Utils.getStringSetting(mContext, Global.PREF_APICOMPANY));
		if(!TextUtils.isEmpty(strBaseUrl))
		{
			showProgressDialog(Str.Loading[Global.LANG]);

			Map<String, String> headers = new HashMap<>();
			headers.put("X-Auth-Token", mRouteApiToken);
			//Get the Route Details
			Map<String, String> params = new HashMap<>();
			params.put("routeVersion", versionInfo.code);
			params.put("routeNumber", "1");
			params.put("day", "ALL");
			ConnectApiClient.getMainApiClient(strBaseUrl, headers).getRouteVersionDetails(params).enqueue(new Callback<List<RouteVersionDetailInfo>>() {
				@Override
				public void onResponse(Call<List<RouteVersionDetailInfo>> call, Response<List<RouteVersionDetailInfo>> response) {
					if(response.isSuccessful() && response.body() != null)
					{
						doBuildRouteSCH(response.body());
					}
					else
					{
						//Refresh token again and try once more.
						if(mRefreshCount == 0)
						{
							mRefreshCount++;
							Map<String, String> params = new HashMap<>();
							params.put("username", Utils.getStringSetting(mContext, Global.PREF_APIUSER));
							params.put("password", Utils.getStringSetting(mContext, Global.PREF_APIPASSWORD));
							ConnectApiClient.getMainApiClient(strBaseUrl, null).login(params).enqueue(new Callback<LoginInfo>() {

								@Override
								public void onResponse(Call<LoginInfo> call, Response<LoginInfo> response) {
									if(response.isSuccessful() && response.body() != null)
									{
										mRouteApiToken = response.body().token;
										doLoadRouteVersionDetails(versionInfo);
									}
									else
									{
										hideProgressDialog();
										doProcessGotoPreTripTask();
									}
								}

								@Override
								public void onFailure(Call<LoginInfo> call, Throwable t) {
									hideProgressDialog();
									doProcessGotoPreTripTask();
								}
							});
						}
					}
				}

				@Override
				public void onFailure(Call<List<RouteVersionDetailInfo>> call, Throwable t) {
					hideProgressDialog();
					doProcessGotoPreTripTask();
				}
			});
		}
		else
		{
			hideProgressDialog();
			doProcessGotoPreTripTask();
		}

	}

	List<CustomerNoteResponse> tempCustomerNoteList = new ArrayList<>();
	boolean bApiCalledResult = false;
	private List<CustomerNoteInfo> getCustomerNoteInfo(int nCustNo, int nChainNo)
	{
		final String strBaseUrl = Utils.getBaseURL(Utils.getStringSetting(mContext, Global.PREF_APICOMPANY));

		List<CustomerNoteInfo> customerNoteInfoList = new ArrayList<>();
		bApiCalledResult = false;
		if(!TextUtils.isEmpty(strBaseUrl))
		{
			Map<String, String> headers = new HashMap<>();
			headers.put("X-Auth-Token", mRouteApiToken);

			Map<String, String> params = new HashMap<>();
			params.put("chainNumber", String.valueOf(nChainNo));
			params.put("customerNumber", String.valueOf(nCustNo));
			final CountDownLatch latch = new CountDownLatch(1);
			ConnectApiClient.getMainApiClient(strBaseUrl, headers).getCustomerNotes(params).enqueue(new Callback<List<CustomerNoteResponse>>() {
				@Override
				public void onResponse(Call<List<CustomerNoteResponse>> call, Response<List<CustomerNoteResponse>> response) {
					if(response.isSuccessful() && response.body() != null)
					{
						tempCustomerNoteList.clear();
						tempCustomerNoteList.addAll(response.body());
						bApiCalledResult = true;
						latch.countDown();
					}
					else
					{
						bApiCalledResult = false;
						latch.countDown();
					}
				}

				@Override
				public void onFailure(Call<List<CustomerNoteResponse>> call, Throwable t) {
					bApiCalledResult = false;
					latch.countDown();
				}
			});
			try {
				latch.await();
			}catch (Exception e)
			{
				e.printStackTrace();
			}
		}


		if(bApiCalledResult == false)
		{
			if(mRefreshCount == 0 && !TextUtils.isEmpty(strBaseUrl)) {
				//Refresh token & once again.
				final CountDownLatch refreshLatch = new CountDownLatch(1);
				Map<String, String> params = new HashMap<>();
				params.put("username", Utils.getStringSetting(mContext, Global.PREF_APIUSER));
				params.put("password", Utils.getStringSetting(mContext, Global.PREF_APIPASSWORD));
				mRefreshCount++;
				ConnectApiClient.getMainApiClient(strBaseUrl, null).login(params).enqueue(new Callback<LoginInfo>() {

					@Override
					public void onResponse(Call<LoginInfo> call, Response<LoginInfo> response) {
						if(response.isSuccessful() && response.body() != null)
						{
							mRouteApiToken = response.body().token;
							bApiCalledResult = true;
							refreshLatch.countDown();
						}
						else
						{
							bApiCalledResult = false;
							refreshLatch.countDown();
						}
					}

					@Override
					public void onFailure(Call<LoginInfo> call, Throwable t) {
						bApiCalledResult = false;
						refreshLatch.countDown();
					}
				});
				try {
					refreshLatch.await();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if(bApiCalledResult)
				{
					return getCustomerNoteInfo(nCustNo, nChainNo);
				}
				else
				{
					return customerNoteInfoList;
				}
			}
			else
			{
				return customerNoteInfoList;
			}
		}
		else
		{
			for(CustomerNoteResponse response : tempCustomerNoteList)
			{
				CustomerNoteInfo info = new CustomerNoteInfo();
				info.nAttachment = response.attachmentCount;
				info.nChainNo = response.chainNumber;
				info.nCustNo = response.customerNumber;
				info.strCreatedBy = response.createdByName;
				info.strNote = response.note;
				info.strNoteDate = response.noteDate;
				info.strNoteId = response.noteId;
				info.strNoteTime = response.createdTime;
				info.strNoteType = String.valueOf(response.noteType);
				customerNoteInfoList.add(info);
			}
		}

		return customerNoteInfoList;
	}

	private void doBuildRouteSCH(final List<RouteVersionDetailInfo> versionDetailInfoList)
	{
		//Filter the version DetailsInfoList first.
		new Thread()
		{
			@Override
			public void run() {
				DbAdapter dbAdapter = new DbAdapter(mContext);
				dbAdapter.open();

				//Insert DayNo Table
				boolean bExistDayNo = false;
				List<Integer> dayNoList = dbAdapter.getDayNo();
				for(Integer dayNo : dayNoList)
				{
					if(dayNo == Utils.getTodayWeekNo())
					{
						bExistDayNo = true;
						break;
					}
				}
				Map<String, RouteVersionDetailInfo> routeMap = new HashMap<>();
				for(RouteVersionDetailInfo routeInfo : versionDetailInfoList)
				{
					if(routeMap.get(routeInfo.chainNo + "/" + routeInfo.customerNo) == null)
					{
						routeMap.put(routeInfo.chainNo + "/" + routeInfo.customerNo, routeInfo);
						//Check this customer is in RouteSCH or not.
						CustomerInfo customerInfo = dbAdapter.getCustomerInfo(routeInfo.customerNo, routeInfo.chainNo);
						if(customerInfo == null) continue;
						if(customerInfo.bIsRouteSch) continue;

						customerInfo.bIsRouteSch = true;
						customerInfo.nDayNo = Utils.getTodayWeekNo();
						customerInfo.nSeqNo = routeInfo.sequenceNumber;
						dbAdapter.updateCustomerInfo(customerInfo);
						if(!bExistDayNo)
						{
							dbAdapter.insertDayNo(Utils.getTodayWeekNo());
							bExistDayNo = true;
						}
						//Make the Customer note from the api.
						mRefreshCount = 0;
						List<CustomerNoteInfo> customerNoteInfoList = getCustomerNoteInfo(routeInfo.customerNo, routeInfo.chainNo);
						for(CustomerNoteInfo customerNoteInfo : customerNoteInfoList)
						{
							dbAdapter.insertCustomerNoteInfo(customerNoteInfo);
						}
					}
				}

				dbAdapter.close();

				new Handler(Looper.getMainLooper()).post(new Runnable() {
					@Override
					public void run() {
						hideProgressDialog();
						doProcessGotoPreTripTask();
					}
				});
			}
		}.start();
	}

	public void processGotoPreTripTask()
	{

		if(DownloadEngine.getEngine() == null)
		{
			DownloadEngine.initEngine();
			if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
			{
				JobScheduler jobScheduler =
						(JobScheduler) mContext.getSystemService(Context.JOB_SCHEDULER_SERVICE);
				assert jobScheduler != null;
				jobScheduler.schedule(new JobInfo.Builder(DownloadJobService.MAIN_SERVICE_JOB_ID,
						new ComponentName(mContext, DownloadJobService.class))
						.setOverrideDeadline(500)
						.setPersisted(true)
						.setRequiresCharging(false)
						.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
						.setRequiresDeviceIdle(false)
						.build());
			}
			else
			{
				DownloadEngine.getEngine().loadAllDataToCache();
				DownloadEngine.getEngine().run();
			}

		}

		Global.gRouteNumber = Utils.getStringSetting(mContext, Global.PREF_ROUTE_NUMBER);

		Utils.registerUploadFile(mContext, makeZipFileInfo);
		Utils.setBooleanSetting(mContext, Global.PREF_PASSED_DOWNLOAD, false);
		Utils.setBooleanSetting(mContext, Global.PREF_END_VISIT_PLAN, false);
        Utils.leaveLog(mContext, "Login_BeforePretrip", null, "END_VISIT_PLAN was set false");
		Utils.setEndTripPlan(false, mContext);
		Global.gSignedOn = true;
		Utils.registerLoginStatus(mContext, 1);

//		Utils.runDownloadThread(mStrFTPServerIP, mStrFTPUserName, mStrFTPPassword);
		if(Utils.getLongSetting(mContext, Global.PREF_LOGIN_START_TIME) == 0)
		{
			Utils.setLongSetting(mContext, Global.PREF_LOGIN_START_TIME, System.currentTimeMillis());
		}

		Utils.setLongSetting(mContext, Global.PREF_LAST_VISIT_BREAK, System.currentTimeMillis());

		if(!TextUtils.isEmpty(Utils.getStringSetting(mContext, Global.PREF_STRIPE_PUB_KEY).trim()))
		{
			PaymentConfiguration.init(mContext.getApplicationContext(), Utils.getStringSetting(mContext, Global.PREF_STRIPE_PUB_KEY).trim());
		}

		//mContext.replaceFragment(new PreTripTasksFragment());
		Intent intent = new Intent(mContext, MainActivity.class);
		mContext.startActivity(intent);
		mContext.finish();
	}

	private void doProcessGotoPreTripTask()
	{
		// Update Driver Number
		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		String strUserName = Utils.getStringSetting(mContext, Global.PREF_USER_NAME);
		List<DescTypeInfo> descTypeInfoList = dbAdapter.getDescTypeListWithDescription(Global.DRIVER_DESC_TYPE_ID, strUserName);
		if (descTypeInfoList.size() > 0) {
			int nNewDriverNumber = descTypeInfoList.get(0).nNumericKey;
			Utils.setStringSetting(mContext, Global.PREF_DRIVER_NUMBER, String.valueOf(nNewDriverNumber));
		}
		dbAdapter.close();

		//We have to complete the Change Driver function.
		if(Utils.getStringSetting(mContext, Global.PREF_TRIP_USER).equals("3") && !Utils.getBooleanSetting(mContext, Global.PREF_ASKED_CHANGE_DRIVER_NAME))
		{
			try {
				UpdateDriverDialog dialog = new UpdateDriverDialog(mContext, true);
				dialog.show();
			}catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			processGotoPreTripTask();
		}
	}

	private void gotoConfiguration(boolean bShowMessage) {
		if (bShowMessage == true) {
			Utils.printDebug(mContext, Str.Please_configure_the_FTP_server_detail[Global.LANG]);
		}
		mContext.replaceFragment(new ConfigurationFragment());
	}

	public boolean m_bClicked = false;
	@Override
	public void onClick(View v) {
		if( m_bClicked ) return;
		m_bClicked = true;
		switch (v.getId()) {
			case R.id.btnExit:
				mContext.onBackPressed();
				break;
			case R.id.btnLogin:
				doLogin();
				break;
		}
		m_bClicked = false;
	}

	private class DownloadTask extends AsyncTask<Void, String, String> {

		public int nType = 0;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			String strUserName = Utils.getStringSetting(mContext, Global.PREF_LOGIN_USER_NAME);
			long nTrxnNo = setGeneralUploadData("LOG", 0, 0, -1, "Start Day DRVR");
			/*if(TextUtils.isEmpty(strUserName) || strUserName.equals("admin"))
			{
				mStartOfDayTrxnNo = nTrxnNo;
			}*/
			TransactionInfo transactionInfo = new TransactionInfo();
			transactionInfo.strType = "LOG";
			transactionInfo.strTime = new SimpleDateFormat("kk:mm").format(new Date(nTrxnNo));
			DbAdapter dbAdapter = new DbAdapter(mContext);
			dbAdapter.open();
			dbAdapter.insertTransactionHistoryInfo(transactionInfo);
			dbAdapter.close();
			try {
				showProgressDialog(Str.Loading[Global.LANG]);
			}catch (Exception e)
			{
				e.printStackTrace();
			}

		}

		@Override
		protected String doInBackground(Void... params) {

			String strParentDir = String.format(Locale.ENGLISH, Global.FTP_DOWNLOAD_DIR, mStrFTPCompanyName);
			String strFileName = String.format(Locale.ENGLISH, "%s.zip", Global.gTripNumber);
			FTPClient ftpClient = null;

			if (Utils.checkFileStatusUsingHttp(mContext, strParentDir, strFileName) == null) {
				if(Utils.getStringSetting(mContext, Global.PREF_DisableFTP).equals("1"))
				{
					return Str.CantFindMatchedTripData[Global.LANG];
				}
				else
				{
					if(ftpClient == null)
					{
						ftpClient = Utils.connnectingwithFTP(mStrFTPServerIP, Global.DEFAULT_FTP_PORT, mStrFTPUserName, mStrFTPPassword);
					}

					if(ftpClient == null)
					{
						return Str.Cant_connect_to_FTP_server[Global.LANG];
					}

					if (Utils.findFileOnFTPServer(ftpClient, strParentDir, strFileName) == 0) {
						return Str.CantFindMatchedTripData[Global.LANG];
					}
				}
			}

			Utils.deleteDirectory(Global.DOWNLOAD_DIR);
			//Utils.deleteDirectory(Global.UPLOAD_DIR);
			Utils.fileDelete(Global.DEFAULT_SIGNATURE_FILEPATH);

			if (Utils.createDirectory(Global.DOWNLOAD_DIR) == false) {
				return Str.Cant_create_the_directory_on_SD_card[Global.LANG];
			}

			String strLocalPath = Global.DOWNLOAD_DIR + strFileName;

			if(!Utils.getStringSetting(mContext, Global.PREF_DisableFTP).equals("1"))
			{
				if(ftpClient == null)
				{
					ftpClient = Utils.connnectingwithFTP(mStrFTPServerIP, Global.DEFAULT_FTP_PORT, mStrFTPUserName, mStrFTPPassword);
				}
			}

			if(Utils.downloadFileFromServer(mContext, strParentDir, strFileName, strLocalPath, ftpClient) == null){
				return Str.Cant_download_the_territory_data[Global.LANG];
			}

			if (Utils.unzipData() == false) {
				return Str.Cant_extract_the_data[Global.LANG];
			}

			if (Utils.getIntSetting(mContext, Global.PREF_SUPERVISOR) == 1 && mSelTripUser != null && mSelTripUser.strUserName.equals("Supervisor") && mSelTripUser.strDescription.equals("Supervisor")) {
				// Parse Data
				// Register this login has been using super visor
				Utils.setBooleanSetting(mContext, Global.PREF_LOGIN_AS_SUPER_VISOR, true);
			} else {
				int nRes = ParseXMLFile.fillDataToDatabase(mContext, true);
				if (nRes == 1) {
					return Str.Cant_fill_the_data_to_database[Global.LANG];
				} else if (nRes == 2) {
					return Str.CorruptDataFound[Global.LANG];
				}
				Utils.setBooleanSetting(mContext, Global.PREF_LOGIN_AS_SUPER_VISOR, false);
			}

			//Download Font File from the server
			strParentDir = String.format(Locale.ENGLISH, Global.FTP_REPORTS_DIR, mStrFTPCompanyName);
			strFileName = Utils.getStringSetting(mContext, Global.PREF_PRINT_FONT_FILENAME);
			String strTargetFile = Global.ROOT_PRINT_TEMPLATE_LOCCATION + strFileName;
			Utils.downloadFileFromServer(mContext, strParentDir, strFileName, strTargetFile, ftpClient);

			// Download Company Logo
			strParentDir = String.format(Locale.ENGLISH, Global.FTP_REPORTS_DIR, mStrFTPCompanyName);
			strFileName = Global.COMPANY_LOGO_FILENAME;
			strTargetFile = Global.LOGO_DIR + strFileName; // Global.DEFAULT_COMPANY_LOGO_FILEPATH;
			Utils.downloadFileFromServer(mContext, strParentDir, strFileName, strTargetFile, ftpClient);

			// Download Company Logo
			strParentDir = String.format(Locale.ENGLISH, Global.FTP_REPORTS_DIR, mStrFTPCompanyName);
			strFileName = "CmpylogoDocket.bmp";
			strTargetFile = Global.LOGO_DIR + strFileName;
			Utils.downloadFileFromServer(mContext, strParentDir, strFileName, strTargetFile, ftpClient);

			// Download Company Splash
			strParentDir = String.format(Locale.ENGLISH, Global.FTP_REPORTS_DIR, mStrFTPCompanyName);
			strFileName = Global.COMPANY_SPLASH_FILENAME;
			strTargetFile = Global.DEFAULT_COMPANY_SPLASH_FILEPATH;
			Utils.downloadFileFromServer(mContext, strParentDir, strFileName, strTargetFile, ftpClient);

			// Download Print Template
			String[] templateFileNames = new String[]{
					Global.PRINT_TEMPLATE_INVOICE_FMT_FILENAME, Global.PRINT_TEMPLATE_ORDERACKFMT_FILENAME,
					Global.PRINT_TEMPLATE_DELIVERY_FMT_FILENAME, Global.PRINT_LOAD_SHEET_TEMPLATE_FILENAME,
					Global.PRINT_TRUCK_INVENTORY_TEMPLATE_FILENAME, Global.PRINT_LOAD_REQUEST_TEMPLATE_FILENAME,
					Global.PRINT_LOAD_ADJUSTMENT_TEMPLATE_FILENAME, Global.PRINT_STOCKTAKE_COUNT_TEMPLATE_FILENAME,
					Global.PRINT_PICK_SLIP_TEMPLATE_FILENAME, Global.PRINT_TERMS_TEMPLATE_FILENAME,
					Global.PRINT_SALES_PLAN_TEMPLATE_FILENAME, Global.PRINT_OFFLOAD_TEMPLATE_FILENAME,
					Global.PRINT_BANKING_TEMPLATE_FILENAME, Global.PRINT_RECEIPT_FILENAME,
					Global.PRINT_COLLECTION_REPORT_FILENAME, Global.PRINT_TRANSACTION_HISTORY_REPORT_FILENAME,
					Global.PRINT_CONSIGNMENT_FILENAME, Global.PRINT_CONSIGNMENT_INVENTORY_FILENAME,
					Global.PRINT_CONSIGNMENT_DOCKET_FILENAME, Global.PRINT_A4_INVOICE_FILENAME,
					Global.PRINT_A4_DOCKET_FILENAME, Global.PRINT_A4_NEW_EQUIPMENT_DOC_FMT_FILENAME,
					Global.PRINT_A4_SURVEY_DOC_FMT_FILENAME, Global.PRINT_TRUCK_INVENTORY_TEMPLATE_FILENAME,
			};

			// Download Print Template
			String strDescriptionTypeID = Global.PRINTER_TYPE_DESC_TYPE_ID;
			DbAdapter dbAdapter = new DbAdapter(mContext);
			dbAdapter.open();
			List<DescTypeInfo> _printerDescTypeList = dbAdapter.getDescTypeList(strDescriptionTypeID);
			dbAdapter.close();

			// we need to download the templates for current printer size
			int defaultPrintType = Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE);

			Collections.sort(_printerDescTypeList, new Comparator<DescTypeInfo>() {
				@Override
				public int compare(DescTypeInfo descTypeInfo, DescTypeInfo t1) {
					int nFirstValue1 = 0;
					int nFirstValue2 = 0;
					try {
						nFirstValue1 = Integer.parseInt(descTypeInfo.strValue1);
					}
					catch (Exception ex) {}
					try {
						nFirstValue2 = Integer.parseInt(descTypeInfo.strValue2);
					}
					catch (Exception ex) {}
					if (nFirstValue1 == nFirstValue2) {
						return descTypeInfo.nNumericKey - t1.nNumericKey;
					}
					else {
						if (nFirstValue1 == defaultPrintType) {
							return -1;
						}
						else if (nFirstValue2 == defaultPrintType) {
							return 1;
						}
						else {
							return descTypeInfo.nNumericKey - t1.nNumericKey;
						}
					}
				}
			});

			// do not duplicate printer size suffix
			List<DescTypeInfo> printerDescTypeList = new ArrayList<DescTypeInfo>();
			for (int i = 0; i < _printerDescTypeList.size(); i++) {
				DescTypeInfo descTypeInfo = _printerDescTypeList.get(i);
				boolean bDuplicated = false;
				for (int k = 0; k < printerDescTypeList.size(); k++) {
					DescTypeInfo existingDescTypeInfo = printerDescTypeList.get(k);
					if (existingDescTypeInfo.strValue2.equals(descTypeInfo.strValue2)) {
						bDuplicated = true;
						break;
					}
				}
				if (!bDuplicated) {
					printerDescTypeList.add(descTypeInfo);
				}
			}

			strParentDir = String.format(Locale.ENGLISH, Global.FTP_REPORTS_DIR, mStrFTPCompanyName);

			if (printerDescTypeList.size() == 0) {
				for (int k = 0; k < templateFileNames.length; k++) {
					String strTemplateFileName = templateFileNames[k];
					if (strTemplateFileName.length() == 0) {
						continue;
					}
					String _strTargetFile = Global.ROOT_PRINT_TEMPLATE_LOCCATION + strTemplateFileName;
					Utils.downloadFileFromServer(mContext, strParentDir, strTemplateFileName, _strTargetFile, ftpClient);
				}
			}
			else {
				DescTypeInfo printerDescType = printerDescTypeList.get(0);
				String strPrinterSize = printerDescType.strValue2;
				String strPrinterType = printerDescType.strValue1;

				if (strPrinterType.equals(Global.HONEYWELL_PRINTER_TYPE_VALUE1)) {
					Utils.setStringSetting(mContext, Global.PREF_HONEYWELLLINE_PRINTER_TEMPLATE_SUFFIX, strPrinterSize);
				}

				for (int k = 0; k < templateFileNames.length; k++) {
					String strTemplateFileName = templateFileNames[k];
					String finalTemplateFileName = Utils.getFinalPrintTemplateFileName(mContext, strTemplateFileName, strPrinterSize);
					if (finalTemplateFileName.length() == 0) {
						continue;
					}
					String _strTargetFile = Global.ROOT_PRINT_TEMPLATE_LOCCATION + finalTemplateFileName;
					Utils.downloadFileFromServer(mContext, strParentDir, finalTemplateFileName, _strTargetFile, ftpClient);

					if (strPrinterType.equals(Global.HONEYWELL_PRINTER_TYPE_VALUE1)) {
						finalTemplateFileName = Utils.getFinalPrintTemplateFileNameForHoneywell(strTemplateFileName, strPrinterSize);
						_strTargetFile = Global.ROOT_PRINT_TEMPLATE_LOCCATION + finalTemplateFileName;
						Utils.downloadFileFromServer(mContext, strParentDir, finalTemplateFileName, _strTargetFile, ftpClient);
					}
				}

				if (printerDescTypeList.size() > 1) {
					String finalStrParentDir = strParentDir;
					new Thread() {
						@Override
						public void run() {
							FTPClient ftpClient = null;
							if(!Utils.getStringSetting(mContext, Global.PREF_DisableFTP).equals("1"))
							{
								if(ftpClient == null)
								{
									ftpClient = Utils.connnectingwithFTP(mStrFTPServerIP, Global.DEFAULT_FTP_PORT, mStrFTPUserName, mStrFTPPassword);
								}
							}

							for (int i = 1; i < printerDescTypeList.size(); i++) {
								DescTypeInfo printerDescType = printerDescTypeList.get(i);
								String strPrinterSize = printerDescType.strValue2;
								String strPrinterType = printerDescType.strValue1;

								if (strPrinterType.equals(Global.HONEYWELL_PRINTER_TYPE_VALUE1)) {
									Utils.setStringSetting(mContext, Global.PREF_HONEYWELLLINE_PRINTER_TEMPLATE_SUFFIX, strPrinterSize);
								}

								for (int k = 0; k < templateFileNames.length; k++) {
									String strTemplateFileName = templateFileNames[k];
									String finalTemplateFileName = Utils.getFinalPrintTemplateFileName(mContext, strTemplateFileName, strPrinterSize);
									if (finalTemplateFileName.length() == 0) {
										continue;
									}
									String _strTargetFile = Global.ROOT_PRINT_TEMPLATE_LOCCATION + finalTemplateFileName;
									Utils.downloadFileFromServer(mContext, finalStrParentDir, finalTemplateFileName, _strTargetFile, ftpClient);

									if (strPrinterType.equals(Global.HONEYWELL_PRINTER_TYPE_VALUE1)) {
										finalTemplateFileName = Utils.getFinalPrintTemplateFileNameForHoneywell(strTemplateFileName, strPrinterSize);
										_strTargetFile = Global.ROOT_PRINT_TEMPLATE_LOCCATION + finalTemplateFileName;
										Utils.downloadFileFromServer(mContext, finalStrParentDir, finalTemplateFileName, _strTargetFile, ftpClient);
									}
								}
							}

							if (ftpClient != null) {
								try {
									ftpClient.disconnect();
								} catch (IOException e) {
									e.printStackTrace();
								}
							}

							mContext.sendBroadcast(new Intent(Global.ACTION_ADDITIONAL_DOWNLOAD_FINISHED));

							// need to broadcast that we've finished the downloading
						}
					}.start();
				}

//				for (int i = 0; i < printerDescTypeList.size(); i++) {
//					DescTypeInfo printerDescType = printerDescTypeList.get(i);
//					if (printerDescType.strValue2.length() == 0) {
//						continue;
//					}
//					String strPrinterSize = printerDescType.strValue2;
//					String strPrinterType = printerDescType.strValue1;
//
//					if (strPrinterType.equals(Global.HONEYWELL_PRINTER_TYPE_VALUE1)) {
//						Utils.setStringSetting(mContext, Global.PREF_HONEYWELLLINE_PRINTER_TEMPLATE_SUFFIX, strPrinterSize);
//					}
//
//					if (nPrinterIndex == 0) {
//						for (int k = 0; k < templateFileNames.length; k++) {
//							String strTemplateFileName = templateFileNames[k];
//							String finalTemplateFileName = Utils.getFinalPrintTemplateFileName(mContext, strTemplateFileName, strPrinterSize);
//							if (finalTemplateFileName.length() == 0) {
//								continue;
//							}
//							String _strTargetFile = Global.ROOT_PRINT_TEMPLATE_LOCCATION + finalTemplateFileName;
//							Utils.downloadFileFromServer(mContext, strParentDir, finalTemplateFileName, _strTargetFile, ftpClient);
//
//							if (strPrinterType.equals(Global.HONEYWELL_PRINTER_TYPE_VALUE1)) {
//								finalTemplateFileName = Utils.getFinalPrintTemplateFileNameForHoneywell(strTemplateFileName, strPrinterSize);
//								_strTargetFile = Global.ROOT_PRINT_TEMPLATE_LOCCATION + finalTemplateFileName;
//								Utils.downloadFileFromServer(mContext, strParentDir, finalTemplateFileName, _strTargetFile, ftpClient);
//							}
//						}
//					}
//					else {
//						String finalStrParentDir = strParentDir;
//						new Thread()
//						{
//							@Override
//							public void run() {
//
//								FTPClient ftpClient = null;
//								if(!Utils.getStringSetting(mContext, Global.PREF_DisableFTP).equals("1"))
//								{
//									if(ftpClient == null)
//									{
//										ftpClient = Utils.connnectingwithFTP(mStrFTPServerIP, Global.DEFAULT_FTP_PORT, mStrFTPUserName, mStrFTPPassword);
//									}
//								}
//
//								for (int k = 0; k < templateFileNames.length; k++) {
//									String strTemplateFileName = templateFileNames[k];
//									String finalTemplateFileName = Utils.getFinalPrintTemplateFileName(mContext, strTemplateFileName, strPrinterSize);
//									if (finalTemplateFileName.length() == 0) {
//										continue;
//									}
//									String _strTargetFile = Global.ROOT_PRINT_TEMPLATE_LOCCATION + finalTemplateFileName;
//									Utils.downloadFileFromServer(mContext, finalStrParentDir, finalTemplateFileName, _strTargetFile, ftpClient);
//
//									if (strPrinterType.equals(Global.HONEYWELL_PRINTER_TYPE_VALUE1)) {
//										finalTemplateFileName = Utils.getFinalPrintTemplateFileNameForHoneywell(strTemplateFileName, strPrinterSize);
//										_strTargetFile = Global.ROOT_PRINT_TEMPLATE_LOCCATION + finalTemplateFileName;
//										Utils.downloadFileFromServer(mContext, finalStrParentDir, finalTemplateFileName, _strTargetFile, ftpClient);
//									}
//								}
//
//
//
//								if (ftpClient != null) {
//									try {
//										ftpClient.disconnect();
//									} catch (IOException e) {
//										e.printStackTrace();
//									}
//								}
//							}
//						}.start();
//					}
//					nPrinterIndex++;
//				}
			}

			long nTrxnNo = setGeneralUploadData("DWNL", 0, 0, -1, "Regular");
			TransactionInfo transactionInfo = new TransactionInfo();
			transactionInfo.strType = "DWNL";
			transactionInfo.strTime = new SimpleDateFormat("kk:mm").format(new Date(nTrxnNo));
			dbAdapter = new DbAdapter(mContext);
			dbAdapter.open();
			dbAdapter.insertTransactionHistoryInfo(transactionInfo);
			dbAdapter.close();

			Utils.createDirectory(Global.LOGO_DIR);

			//Download Documents icon files
			new Thread()
			{
				@Override
				public void run() {
					String[] iconArray = {"Dropbox.png", "Box.png", "OneDrive.png", "GoogleDrive.png"};
					for(String iconName : iconArray)
					{
						String strFTPCompanyName = Utils.getStringSetting(mContext, Global.PREF_FTP_COMPANY_NAME);
						String strParentDir = String.format(Locale.ENGLISH, Global.FTP_REPORTS_DIR, strFTPCompanyName);
						String strFileName = iconName;
						Utils.downloadFileFromServer(mContext, strParentDir, strFileName, Global.ROOT_LOCATION + strFileName, null);
					}
				}
			}.start();

			Utils.deleteBackupExpiredData(mContext);

			if (ftpClient != null) {
				try {
					ftpClient.disconnect();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return "";
			//Utils.runDownloadThread(mStrFTPServerIP, mStrFTPUserName, mStrFTPPassword);
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);

			try
			{
				hideProgressDialog();

				if (result.length() == 0) {
//					Utils.printDebug(mContext, Str.LoadedTripData[Global.LANG]);
                    Utils.setBooleanSetting(mContext, Global.PREF_PASSED_DOWNLOAD, true);
					if (nType == 1) {
						gotoPreTripTasks();
					} else {
						((EditText) mRootView.findViewById(R.id.editUsername)).setText("");
						((EditText) mRootView.findViewById(R.id.editPinNumber)).setText("");
						((EditText) mRootView.findViewById(R.id.editTripNumber)).setText("");

						File imgFile = new  File(Global.DEFAULT_COMPANY_SPLASH_FILEPATH);
						Bitmap splashBitmap = null;
						if(imgFile.exists()){
							splashBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
						}
						if( splashBitmap != null )
						{
							((ImageView)mRootView.findViewById(R.id.logo)).setImageBitmap(splashBitmap);
							mRootView.findViewById(R.id.background).setBackgroundColor(mContext.getResources().getColor(R.color.transparent_color));
						}
						loadData();
					}
				} else {
					showMessageDialog(MSG_TYPE_NORMAL, result);
				}
			}catch(Exception e)
			{
				e.printStackTrace();
			}
			
		}
	}

	private void doDownloadTask(final int nType)
	{
		if(Utils.isConnectingToInternet(mContext))
		{
			showProgressDialog(Str.Loading[Global.LANG]);
			ApiClient.API_ROOT = Global.API_NEW_SERVER_ROOT;
			Map<String, String> params = new HashMap<>();
			params.put("package_name", "dsdvansales");
			params.put("version", String.valueOf(BuildConfig.VERSION_CODE));
			ApiClient.getApiClient().getBugSeeValidation(params).enqueue(new Callback<BugSeeResult>() {
				@Override
				public void onResponse(Call<BugSeeResult> call, Response<BugSeeResult> response) {
					if(response.isSuccessful() && response.body().result.equals("1"))
					{
						Utils.setStringSetting(mContext, Global.PREF_BUGSEE_VALIDATION, response.body().bugsee_validation);
					}
					else
					{
						Utils.setStringSetting(mContext, Global.PREF_BUGSEE_VALIDATION, "");
					}

					DownloadTask task = new DownloadTask();
					task.nType = nType;
					task.execute();
				}

				@Override
				public void onFailure(Call<BugSeeResult> call, Throwable t) {
					Utils.setStringSetting(mContext, Global.PREF_BUGSEE_VALIDATION, "");

					DownloadTask task = new DownloadTask();
					task.nType = nType;
					task.execute();
				}
			});
		}
	}
}