package dsd.dsdvansales.activities;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.text.InputType;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import com.bugsee.library.Bugsee;
import com.bugsee.library.attachment.CustomAttachment;
import com.bugsee.library.data.IssueSeverity;
import com.bugsee.library.data.IssueType;
import com.jaredrummler.android.device.DeviceName;
import com.quickblox.chat.QBRestChatService;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.core.request.QBRequestGetBuilder;

import dsd.dsdvansales.R;
import dsd.dsdvansales.bluetooth.BluetoothService;
import dsd.dsdvansales.chat.ChatHelper;
import dsd.dsdvansales.chat.DialogsManager;
import dsd.dsdvansales.db.DbAdapter;
import dsd.dsdvansales.db.UploadDbAdapter;
import dsd.dsdvansales.dialogs.AddMFADialog;
import dsd.dsdvansales.dialogs.DriverBreakCountDownDialog;
import dsd.dsdvansales.dialogs.DriverBreakDialog;
import dsd.dsdvansales.dialogs.InputDialog;
import dsd.dsdvansales.dialogs.InventoryApprovalDialog;
import dsd.dsdvansales.dialogs.MessageDialog;
import dsd.dsdvansales.dialogs.PrintPickListDialog;
import dsd.dsdvansales.dialogs.UpdateDriverDialog;
import dsd.dsdvansales.dialogs.UpdatePrinterTypeDialog;
import dsd.dsdvansales.dialogs.WindcaveStationIDDialog;
import dsd.dsdvansales.fragment.AdjustConsignmentInventoryFragment;
import dsd.dsdvansales.fragment.AdjustConsignmentInventorySummaryFragment;
import dsd.dsdvansales.fragment.AdjustInventoryFragment;
import dsd.dsdvansales.fragment.AdjustSummaryFragment;
import dsd.dsdvansales.fragment.BaseFragment;
import dsd.dsdvansales.fragment.CardDetailFragment;
import dsd.dsdvansales.fragment.ChequeDetailFragment;
import dsd.dsdvansales.fragment.CustomerDiaryFragment;
import dsd.dsdvansales.fragment.DeliverySummaryFragment;
import dsd.dsdvansales.fragment.EndingInventoryFragment;
import dsd.dsdvansales.fragment.EndingInventorySummaryFragment;
import dsd.dsdvansales.fragment.InstoreFragment;
import dsd.dsdvansales.fragment.LoadRequestFragment;
import dsd.dsdvansales.fragment.LoadTruckBulkFragment;
import dsd.dsdvansales.fragment.LoadTruckSignFragment;
import dsd.dsdvansales.fragment.LoginFragment;
import dsd.dsdvansales.fragment.MainConfigurationFragment;
import dsd.dsdvansales.fragment.MakeDeliveryFragment;
import dsd.dsdvansales.fragment.PaymentCollectionFragment;
import dsd.dsdvansales.fragment.PreTripTasksFragment;
import dsd.dsdvansales.fragment.PrintDocumentsFragment;
import dsd.dsdvansales.fragment.SalesTrackerFragment;
import dsd.dsdvansales.fragment.SelectCustomerFragment;
import dsd.dsdvansales.fragment.SpoilEntryFragment;
import dsd.dsdvansales.fragment.StockTakeCountEntryFragment;
import dsd.dsdvansales.fragment.TripSettlementFragment;
import dsd.dsdvansales.fragment.UpdateConsignmentInventoryFragment;
import dsd.dsdvansales.fragment.VehicleInventoryFragment;
import dsd.dsdvansales.global.BixolonPrinter;
import dsd.dsdvansales.global.BluetoothProcessor;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.HoneywellLinePrinter;
import dsd.dsdvansales.global.HoneywellPrinter;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.UploadViaHTTP;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.model.AdjustInventoryBluetoothData;
import dsd.dsdvansales.model.BluetoothTransferData;
import dsd.dsdvansales.model.DescTypeInfo;
import dsd.dsdvansales.model.InventoryData;
import dsd.dsdvansales.model.MakeZipFileInfo;
import dsd.dsdvansales.model.ProdDetlInfo;
import dsd.dsdvansales.model.UStockHInfo;
import dsd.dsdvansales.model.UTransactionsInfo;
import dsd.dsdvansales.model.UploadFileInfo;
import dsd.dsdvansales.model.UploadInfo;
import dsd.dsdvansales.services.GattDeviceAPI;
import dsd.dsdvansales.services.GattServerService;
import dsd.dsdvansales.services.MainJobService;
import dsd.dsdvansales.views.AlertSingleChoiceAdapter;

import com.mintwireless.mintmpos.integrator.utils.MTGAppSwitch;
import com.mintwireless.mintmpos.integrator.utils.MTGAppSwitchResponse;
import com.mintwireless.mintmpos.integrator.utils.MTGAppSwitchRequest;
import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.printer.PrinterStatus;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;
import com.zebra.sdk.printer.ZebraPrinterLanguageUnknownException;
import com.zebra.sdk.printer.discovery.BluetoothDiscoverer;
import com.zebra.sdk.printer.discovery.DiscoveredPrinter;
import com.zebra.sdk.printer.discovery.DiscoveredPrinterBluetooth;
import com.zebra.sdk.printer.discovery.DiscoveryHandler;

public class MainActivity extends FragmentActivity implements View.OnClickListener, Animation.AnimationListener, DialogsManager.ManagingDialogsCallbacks {

	private static final int REQUEST_ENABLE_BT = 1001;
	private static final int END_JOURNEY = 0;
	private static final int LOGOUT = 1;
	private static final int EXIT_APP = 2;

	public static final int	RESULT_TAKE_PHOTO		= 1010;

	private Context mContext = null;

	private Intent customIntent = null;

	private LinearLayout mLayoutMenu1 = null;
	private LinearLayout mLayoutMenu2 = null;
	private boolean mIsAnimation = false;
	private boolean mShowMenu1 = false;
	private boolean mShowMenu2 = false;

	private float mTouchY1_start, mTouchY1_end;
	private float mTouchY2_start, mTouchY2_end;
	private final int MIN_DISTANCE = 50;
	private boolean mIsRunning = false;

	private boolean isVisitChequeDetails = false;

	private ProgressDialog mProgressDlg = null;
	private String error = "";
	DriverBreakCountDownDialog countDownDialog = null;

	private long lStopTime = 0;

	private GattServiceConn gattServiceConn;

	private BluetoothHeadset bluetoothHeadset = null;
	private BluetoothProfile.ServiceListener profileListener = new BluetoothProfile.ServiceListener() {
		public void onServiceConnected(int profile, BluetoothProfile proxy) {
			if (profile == BluetoothProfile.HEADSET) {
				doRegisterBluetoothTransfer();
				bluetoothHeadset = (BluetoothHeadset)proxy;
			}
		}
		public void onServiceDisconnected(int profile) {
			if (profile == BluetoothProfile.HEADSET) {
				doUnregisterBluetoothTransfer();
				bluetoothHeadset = null;
			}
		}
	};

	public void showProgressDialog(String strMessage)
	{
		try {
			if(mProgressDlg == null)
		{
			mProgressDlg = new ProgressDialog(this);
			mProgressDlg.setCancelable(false);
			mProgressDlg.show();
		}
		mProgressDlg.setMessage(strMessage);
		}catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	public void hideProgressDialog(){
		try {
			if(mProgressDlg != null)
		{
			mProgressDlg.dismiss();
			mProgressDlg = null;
		}
		}catch (Exception e)
		{
			e.printStackTrace();
		}

	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_main);

		//Bugsee settings
		Bugsee.setReportAttachmentsProvider(report -> {
			ArrayList<CustomAttachment> resultList = new ArrayList<>();

			// If it is a Blocker or crash.
			if (report.getType() == IssueType.Crash || report.getSeverity() == IssueSeverity.Blocker) {
				// myFilePath is a path to file, which should be attached to the report.
				if(Utils.isFileExist(Global.ROOT_ROUTECTL_PATH))
				{
					CustomAttachment attachment = CustomAttachment.fromDataFilePath(Global.ROOT_ROUTECTL_PATH);
					attachment.setFileName("ROUTECTL.xml");
					attachment.setName("Route Control Information");
					resultList.add(attachment);
				}
			}
			return resultList;
		});

		String strCurDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		String strBugseeValidation = Utils.getStringSetting(MainActivity.this, Global.PREF_BUGSEE_VALIDATION);
		if(Utils.getStringSetting(this, Global.PREF_CRASH_REPORT).equals("1") || (!TextUtils.isEmpty(strBugseeValidation) && strBugseeValidation.compareTo(strCurDate) > 0))
		{
			if(!Global.bugSeeRecording)
			{
				HashMap<String, Object> options = new HashMap<>();
				options.put(Bugsee.Option.MaxRecordingTime, 60);
				options.put(Bugsee.Option.ShakeToTrigger, false);
				options.put(Bugsee.Option.ScreenshotEnabled, false);
				if(!Global.bugSeeLaunch)
				{
					Bugsee.launch(this, "615a0763-2c86-48dd-9620-36333fe5e714", options);
					Global.bugSeeLaunch = true;
				}
				else
				{
					Bugsee.relaunch(options);
				}
				Global.bugSeeRecording = true;
			}
		}
		else {
			if(Global.bugSeeRecording)
			{
				Bugsee.stop();
				Global.bugSeeRecording = false;
			}
		}

		Utils.BuildHTMMap();

		mContext = this;
		initView();
		DeviceName.with(this).request(new DeviceName.Callback() {

			@Override public void onFinished(DeviceName.DeviceInfo info, Exception error) {
				String manufacturer = info.manufacturer;  // "Samsung"
				String name = info.marketName;            // "Galaxy S7 Edge"
				String model = info.model;                // "SAMSUNG-SM-G935A"
				String codename = info.codename;          // "hero2lte"
				String deviceName = info.getName();       // "Galaxy S7 Edge"
				Utils.setStringSetting(MainActivity.this, Global.DEVICE_MODEL, model);
				// FYI: We are on the UI thread.
			}
		});

/*		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.
		client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();*/

		mIsRunning = true;
		checkMessageStatus();

		if(savedInstanceState == null /*&& getIntent().getData() == null*/)
		{
			showNewChatOn(false);
			replaceFragment(new PreTripTasksFragment());
		}

//		try
//		{
//			BluetoothAdapter.getDefaultAdapter().getProfileProxy(this, profileListener, BluetoothProfile.HEADSET);
//		}catch (Exception e)
//		{
//			e.printStackTrace();
//		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			mContext.registerReceiver(new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					try {
						DescTypeInfo info = (DescTypeInfo) intent.getSerializableExtra(Global.EXTRA_DESCTYPE_INFO);
						DbAdapter dbAdapter = new DbAdapter(mContext);
						dbAdapter.open();
						info.strValue6 = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss").format(new Date());
						dbAdapter.updateDescTypeInfo(info);
						dbAdapter.close();

						final MessageDialog dlg = new MessageDialog(mContext);
						dlg.mStrMessage = Str.ReminderText[Global.LANG];
						dlg.mStrMiddle = Str.Ok[Global.LANG];
						dlg.setCancelable(false);
						dlg.show();
					}catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}, new IntentFilter(Global.ACTION_BREAK_TIME_FIRE), RECEIVER_EXPORTED);
		} else {
			mContext.registerReceiver(new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					try {
						DescTypeInfo info = (DescTypeInfo) intent.getSerializableExtra(Global.EXTRA_DESCTYPE_INFO);
						DbAdapter dbAdapter = new DbAdapter(mContext);
						dbAdapter.open();
						info.strValue6 = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss").format(new Date());
						dbAdapter.updateDescTypeInfo(info);
						dbAdapter.close();

						final MessageDialog dlg = new MessageDialog(mContext);
						dlg.mStrMessage = Str.ReminderText[Global.LANG];
						dlg.mStrMiddle = Str.Ok[Global.LANG];
						dlg.setCancelable(false);
						dlg.show();
					}catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}, new IntentFilter(Global.ACTION_BREAK_TIME_FIRE));
		}

		updatePrinterTypeDetails();

		//Utils.printDebug(this, "Starting GattServerService");
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			startForegroundService(new Intent(this, GattServerService.class));
		}
		else {
			startService(new Intent(this, GattServerService.class));
		}
	}

	private BluetoothService.BluetoothServiceCallback bluetoothServiceCallback = new BluetoothService.BluetoothServiceCallback() {
		@Override
		public void onConnected(BluetoothSocket socket, BluetoothDevice device) {

		}

		@Override
		public void onConnectionLost() {
			//BluetoothService.getInstance().start();
		}

		@Override
		public void onConnectionFailed() {
			//BluetoothService.getInstance().start();
		}

		@Override
		public void onReceive(String strText) {
			BluetoothTransferData data = BluetoothTransferData.fromJSON(strText);
			if(data != null)
			{
				switch (data.nType)
				{
					case 10 :
						doAdjustInventoryBluetoothProcess(AdjustInventoryBluetoothData.fromJSON(data.strJSONData));
						break;
				}
			}
		}

		@Override
		public void onSend(String strText) {

		}
	};

	public void doAdjustInventoryBluetoothProcess(final AdjustInventoryBluetoothData inventoryData)
	{
		//if User has not logged in yet, then we will send message "This user has not logged in yet".
		if(!Global.gLoggedIn)
		{
			BluetoothTransferData data = new BluetoothTransferData();
			data.nType = 2;
			BluetoothService.getInstance().write(data.toJSON());
			return;
		}

		Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frameContainer);
		if(fragment instanceof AdjustInventoryFragment || fragment instanceof AdjustSummaryFragment)
		{
			BluetoothTransferData data = new BluetoothTransferData();
			data.nType = 3;
			BluetoothService.getInstance().write(data.toJSON());
			return;
		}

		if(!(fragment instanceof VehicleInventoryFragment))
		{
			BluetoothTransferData data = new BluetoothTransferData();
			data.nType = 5;
			BluetoothService.getInstance().write(data.toJSON());
			return;
		}

		//Check all inventory is available or not.
		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		List<InventoryData> unavailableDataList = new ArrayList<>();
		Map<String, ProdDetlInfo> prodDetlInfoMap = new HashMap<>();
		for(InventoryData itemData : inventoryData.uInvenDInfoList)
		{
			List<ProdDetlInfo> prodDetlInfoList = dbAdapter.getProductList(itemData.strItem);
			if(prodDetlInfoList == null || prodDetlInfoList.size() == 0)
			{
				unavailableDataList.add(itemData);
			}
			else
			{
				prodDetlInfoMap.put(itemData.strItem, prodDetlInfoList.get(0));
			}
		}
		dbAdapter.close();

		if(unavailableDataList.size() > 0)
		{
			BluetoothTransferData data = new BluetoothTransferData();
			data.nType = 4;
			AdjustInventoryBluetoothData unavailableData = new AdjustInventoryBluetoothData();
			unavailableData.uInvenDInfoList = unavailableDataList;
			unavailableData.strUserName = Utils.getStringSetting(mContext, Global.PREF_USER_NAME);
			data.strJSONData = unavailableData.toJSON();
			BluetoothService.getInstance().write(data.toJSON());
			return;
		}

		if(inventoryData != null)
		{
			try {
				InventoryApprovalDialog dialog = new InventoryApprovalDialog(mContext, inventoryData, prodDetlInfoMap, new InventoryApprovalDialog.Callback() {
					@Override
					public void doNo() {
						//Send Message to Cancel
						BluetoothTransferData data = new BluetoothTransferData();
						data.nType = 1;
						data.strRouteNumber = Utils.getStringSetting(mContext, Global.PREF_ROUTE_NUMBER);
						BluetoothService.getInstance().write(data.toJSON());
					}

					@Override
					public void doYes() {
						BluetoothTransferData data = new BluetoothTransferData();
						data.nType = 0;
						data.strRouteNumber = Utils.getStringSetting(mContext, Global.PREF_ROUTE_NUMBER);
						BluetoothService.getInstance().write(data.toJSON());
						doAcceptAdjustInventoryData(inventoryData);
					}
				});
				dialog.show();
			}catch (Exception e)
			{
				e.printStackTrace();
			}

		}
	}

	public void doAcceptAdjustInventoryData(final AdjustInventoryBluetoothData data)
	{
		showProgressDialog(Str.Processing[Global.LANG]);
		new Thread()
		{
			@Override
			public void run() {
				/*
				//Make And Update UInventory, Make UStock, after that, Make PDF File.
				BluetoothProcessor processor = new BluetoothProcessor(MainActivity.this);
				processor.doProcess(data);


				new Handler(getMainLooper()).post(new Runnable() {
					@Override
					public void run() {
						Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frameContainer);
						if(fragment instanceof VehicleInventoryFragment)
						{
							((VehicleInventoryFragment)fragment).loadData();
							((VehicleInventoryFragment)fragment).refreshPage();
						}
						hideProgressDialog();
					}
				});

				 */

				//Make AdjustInventory, and go to the Adjust Summary screen.
				BluetoothProcessor processor = new BluetoothProcessor(MainActivity.this);
				processor.doMakeAdjustInventory(data);

				new Handler(getMainLooper()).post(() -> {
					Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frameContainer);
					if(fragment instanceof VehicleInventoryFragment)
					{
						Bundle bundle = new Bundle();
						bundle.putInt(Global.EXTRA_TRANSFER_TYPE, 3);
						bundle.putInt(Global.EXTRA_INVENTORY_TYPE, Global.INVENTORY_FRESH);
						bundle.putBoolean(Global.EXTRA_TRANSFER_LATER, false);
						bundle.putString(Global.EXTRA_TRANSFER_ROUTE, data.strRouteNumber);
						fragment = new AdjustSummaryFragment();
						fragment.setArguments(bundle);
						replaceFragment(fragment);
					}
					hideProgressDialog();
				});
			}
		}.start();
	}

	public void doRegisterBluetoothTransfer()
	{
		BluetoothService.getInstance().start();
		BluetoothService.getInstance().setCallback(bluetoothServiceCallback);
	}

	public void doUnregisterBluetoothTransfer()
	{
		BluetoothService.getInstance().stop();
	}

	public void upgradeNewVersion(String strMark)
	{
		//Write Trip No to the .DSDInstaller Folder
		Utils.writeTripInfo(Global.gTripNumber);
		Utils.markUpgradeAsk(strMark);

		//Kill all services
		Utils.stopMainService(this);
		Utils.setEndTripPlan(true, this);
		finishAffinity();
		Intent intent = new Intent();
		intent.setClassName("dsd.assist", "dsd.assist.activities.MainActivity");
		intent.putExtra("PackageName", getPackageName());
		intent.putExtra("ExternalLaunch", true);
		intent.putExtra("CompanyId", Utils.getStringSetting(this, Global.PREF_COMPANY_ID));
		intent.putExtra("CompanyPin", Utils.getStringSetting(this, Global.PREF_COMPNAY_PIN));

		//intent.putExtra("TripNo", Global.gTripNumber);
		startActivity(intent);
		finish();

		//Remove all trip data
		Utils.clearSettings(mContext);
	}


	private void initView() {
		findViewById(R.id.imgMenu1).setOnClickListener(this);
		findViewById(R.id.imgMenu2).setOnClickListener(this);
		findViewById(R.id.layoutMenu1).setOnClickListener(this);
		findViewById(R.id.layoutMenu2).setOnClickListener(this);
		findViewById(R.id.mark_logo).setOnClickListener(this);
		findViewById(R.id.txtEndJourney).setOnClickListener(this);
		findViewById(R.id.txtLogout).setOnClickListener(this);
		findViewById(R.id.txtChangeDriver).setOnClickListener(this);
		findViewById(R.id.txtChangePrinterType).setOnClickListener(this);
		findViewById(R.id.txtEndTrip).setOnClickListener(this);
		findViewById(R.id.txtMultiLang).setOnClickListener(this);
		findViewById(R.id.txtVehicleInventory).setOnClickListener(this);
		findViewById(R.id.txtHelp).setOnClickListener(this);
		findViewById(R.id.txtAbout).setOnClickListener(this);
		findViewById(R.id.txtMakeDeviceDiscoverable).setOnClickListener(this);
		findViewById(R.id.txtCommunication).setOnClickListener(this);
		findViewById(R.id.txtPrintDocuments).setOnClickListener(this);
		findViewById(R.id.txtSalesTracker).setOnClickListener(this);
		findViewById(R.id.txtDiary).setOnClickListener(this);
		findViewById(R.id.txtWindcaveReader).setOnClickListener(this);
		findViewById(R.id.txtDocuments).setOnClickListener(this);
		findViewById(R.id.imgDocuments).setOnClickListener(this);
		findViewById(R.id.txtSetInvoiceEnding).setOnClickListener(this);
		findViewById(R.id.txtDriverBreak).setOnClickListener(this);
		//findViewById(R.id.txtAddMFA).setOnClickListener(this);

		mLayoutMenu1 = ((LinearLayout) findViewById(R.id.layoutMenu1));
		mLayoutMenu2 = ((LinearLayout) findViewById(R.id.layoutMenu2));

		findViewById(R.id.layoutMenu1).setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						mTouchY1_start = event.getY();
						break;
					case MotionEvent.ACTION_UP:
						mTouchY1_end = event.getY();
						float deltaY = mTouchY1_start - mTouchY1_end;
						if (deltaY > MIN_DISTANCE) {
							startMenu1Animation();
						}
						break;
				}
				return false;
			}
		});

		findViewById(R.id.layoutMenu2).setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						mTouchY2_start = event.getY();
						break;
					case MotionEvent.ACTION_UP:
						mTouchY2_end = event.getY();
						float deltaY = mTouchY2_start - mTouchY2_end;
						if (deltaY > MIN_DISTANCE) {
							startMenu2Animation();
						}
						break;
				}
				return false;
			}
		});

		showNewChatOn(false);

//		replaceFragment(new LoginFragment());
	}

	public void showNewChatOn(boolean bValue)
	{
		if( bValue )
		{
			findViewById(R.id.mark_chat).setVisibility(View.VISIBLE);
		}
		else
		{
			findViewById(R.id.mark_chat).setVisibility(View.GONE);
		}
	}

	@SuppressLint("ResourceType")
	private void refreshMenuLanguage() {
		//if (Utils.getLongSetting(mContext, Global.PREF_UINVEN_TRXN_NO) == 0)
		if(!Utils.getBooleanSetting(mContext, Global.PREF_PASSED_LOAD_SECTION) && Utils.getLongSetting(mContext, Global.PREF_UINVEN_TRXN_NO) == 0)	// This has been applied from Wang because of Andrew Requested at 12th of Feb, 2020.
		{
			((TextView) findViewById(R.id.txtVehicleInventory)).setTextColor(getResources().getColor(R.color.gray_color));
		} else {
			if(!Utils.getBooleanSetting(mContext, String.format(Locale.ENGLISH, Global.PREF_END_TRIP_CHECKED, 1)))
			{
				Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frameContainer);
				if( !(fragment instanceof EndingInventoryFragment) && !(fragment instanceof EndingInventorySummaryFragment)
						&& !(fragment instanceof SpoilEntryFragment))
				{
					((TextView) findViewById(R.id.txtVehicleInventory)).setTextColor(getResources().getColorStateList(R.drawable.clickable_textview_white));
				}
				else
				{
					((TextView) findViewById(R.id.txtVehicleInventory)).setTextColor(getResources().getColor(R.color.gray_color));
				}
			}
			else
			{
				((TextView) findViewById(R.id.txtVehicleInventory)).setTextColor(getResources().getColor(R.color.gray_color));
			}
		}
		if (Global.gEnteredEndTrip == true) {
			((TextView) findViewById(R.id.txtPrintDocuments)).setTextColor(getResources().getColor(R.color.gray_color));
		} else {
			((TextView) findViewById(R.id.txtPrintDocuments)).setTextColor(getResources().getColorStateList(R.drawable.clickable_textview_white));
		}

		if(Global.gCurCustomerInfo != null && Global.gCurCustomerInfo.nCustNo != 0 && Global.gEnteredEndTrip == false) {
			((TextView) findViewById(R.id.txtDiary)).setTextColor(getResources().getColorStateList(R.drawable.clickable_textview_white));
		}
		else {
			((TextView) findViewById(R.id.txtDiary)).setTextColor(getResources().getColor(R.color.gray_color));
		}

		Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frameContainer);
		if(!TextUtils.isEmpty(Utils.getStringSetting(this, Global.PREF_DOCUMENTS)) && !(fragment instanceof LoginFragment))
		{
			findViewById(R.id.txtDocuments).setVisibility(View.VISIBLE);
			findViewById(R.id.imgDocuments).setVisibility(View.VISIBLE);
			((TextView) findViewById(R.id.txtDocuments)).setText(Utils.getAppNameFromPackage(Utils.getStringSetting(this, Global.PREF_DOCUMENTS)));
			Drawable drawable = Utils.getAppDrawableIcon(this, Utils.getStringSetting(this, Global.PREF_DOCUMENTS));
			((ImageView)findViewById(R.id.imgDocuments)).setImageDrawable(drawable);
		}
		else {
			findViewById(R.id.txtDocuments).setVisibility(View.GONE);
			findViewById(R.id.imgDocuments).setVisibility(View.GONE);
		}

		((TextView) findViewById(R.id.txtEndJourney)).setText(Str.End_Journey[Global.LANG]);
		((TextView) findViewById(R.id.txtLogout)).setText(Str.Logout[Global.LANG]);
		((TextView) findViewById(R.id.txtChangeDriver)).setText(Str.ChangeDriver[Global.LANG]);
		((TextView) findViewById(R.id.txtChangePrinterType)).setText(Str.ChangePrinterType[Global.LANG]);
		((TextView) findViewById(R.id.txtCommunication)).setText(Str.Communication[Global.LANG]);
		((TextView) findViewById(R.id.txtVehicleInventory)).setText(Str.VehicleInventory[Global.LANG]);
		((TextView) findViewById(R.id.txtAbout)).setText(Str.About[Global.LANG]);
		((TextView) findViewById(R.id.txtMakeDeviceDiscoverable)).setText(Str.MakeDeviceDiscoverable[Global.LANG]);
		((TextView) findViewById(R.id.txtHelp)).setText(Str.Help[Global.LANG]);
		((TextView) findViewById(R.id.txtPrintDocuments)).setText(Str.PrintDocuments[Global.LANG]);
		((TextView) findViewById(R.id.txtSalesTracker)).setText(Str.SalesTracker[Global.LANG]);
		((TextView) findViewById(R.id.txtDiary)).setText(Str.CustomerDiary[Global.LANG]);
		((TextView) findViewById(R.id.txtDriverBreak)).setText(Str.DriverBreak[Global.LANG]);
		((TextView) findViewById(R.id.txtEndTrip)).setText(Str.EndTrip[Global.LANG]);
		//((TextView) findViewById(R.id.txtAddMFA)).setText(Str.AddMFA[Global.LANG]);


		switch (Global.LANG) {
			case Global.LANG_ENGLISH:
				((TextView) findViewById(R.id.txtMultiLang)).setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_english, 0, 0, 0);
				break;
			case Global.LANG_INDONESIAN:
				((TextView) findViewById(R.id.txtMultiLang)).setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_indonesian, 0, 0, 0);
				break;
			case Global.LANG_THAI:
				((TextView) findViewById(R.id.txtMultiLang)).setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_thai, 0, 0, 0);
				break;
			case Global.LANG_SPANISH:
				((TextView) findViewById(R.id.txtMultiLang)).setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_spanish, 0, 0, 0);
				break;
			case Global.LANG_AMERICAN:
				((TextView) findViewById(R.id.txtMultiLang)).setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_american, 0, 0, 0);
				break;
			case Global.LANG_ARABIC:
				((TextView) findViewById(R.id.txtMultiLang)).setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_egypt, 0, 0, 0);
				break;
		}
		((TextView) findViewById(R.id.txtMultiLang)).setText(Str.LANGUAGES[Global.LANG]);
	}

	private void showLanguages() {
		int nChecked = Global.LANG;
		final int origIdx = Global.LANG;
		try {
			new AlertDialog.Builder(this)
				.setTitle(Str.Please_select_the_language[Global.LANG])
				.setSingleChoiceItems(new AlertSingleChoiceAdapter(MainActivity.this, Str.LANGUAGES), nChecked, (dialog, nSelected) -> Global.LANG = nSelected)
				.setPositiveButton(Str.Ok[Global.LANG], (dialog, nSelected) -> {
					Utils.setIntSetting(mContext, Global.PREF_CUR_LANG, Global.LANG);
					sendBroadcast(new Intent(Global.ACTION_REFRESH_LANGUAGE));
				})
				.setNegativeButton(Str.Cancel[Global.LANG], (dialog, nSelected) -> Global.LANG = origIdx)
				.create()
				.show();
		}catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	private void doRegisterLogoffTransaction(int nProcessType)
	{
		// Make the Logoff transaction
		UTransactionsInfo uTransactionInfo = new UTransactionsInfo();
		uTransactionInfo.nTrxnNo = System.currentTimeMillis();
		uTransactionInfo.strDocType = "LOG";
		uTransactionInfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(mContext, new Date()));
		uTransactionInfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(uTransactionInfo.nTrxnNo);
		String strUserName = Utils.getStringSetting(this, Global.PREF_LOGIN_USER_NAME);
		if(nProcessType == EXIT_APP)
			uTransactionInfo.strReference = "Exit App";
		else if(nProcessType == END_JOURNEY)
			uTransactionInfo.strReference = "End Journey";
		else if(nProcessType == LOGOUT)
			uTransactionInfo.strReference = "Log Out";
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

		MakeZipFileInfo makeZipFileInfo = new MakeZipFileInfo();
		makeZipFileInfo.nChainNo = 0;
		makeZipFileInfo.nCustNo = 0;
		makeZipFileInfo.trxnList.add(uTransactionInfo.nTrxnNo);

		UploadInfo uploadInfo = new UploadInfo();
		uploadInfo.makeZipFileInfo = makeZipFileInfo;
		Utils.uploadData(mContext, 0, uploadInfo, false);
	}
	private void showConfirmDialog(final int nType) {
		try {
			final MessageDialog dlg = new MessageDialog(this);
			if (nType == END_JOURNEY) {
				dlg.mStrMessage = Str.Do_you_want_to_exit_now[Global.LANG];
			} else if (nType == LOGOUT) {
				dlg.mStrMessage = Str.Do_you_want_to_logout_now[Global.LANG];
			} else if (nType == EXIT_APP) {
				dlg.mStrMessage = Str.Are_you_sure_wish_to_exit_the_application[Global.LANG];
			}
			dlg.mStrTitle = "";
			dlg.mStrLeft = Str.No[Global.LANG];
			dlg.mStrRight = Str.Yes[Global.LANG];
			dlg.setCancelable(false);
			dlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
					if (dlg.mReturnCode == Global.DLG_RETURN_RIGHT)
					{
						Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frameContainer);
						if(fragment instanceof LoginFragment)
						{
							Global.gSignedOn = false;
							finish();
						}
						else
						{
							boolean bLoginAsSuperUser = Utils.getBooleanSetting(MainActivity.this, Global.PREF_LOGIN_AS_SUPER_VISOR);
							if (nType == END_JOURNEY || nType == EXIT_APP)
							{
								if(bLoginAsSuperUser)
								{
									new Thread()
									{
										@Override
										public void run() {
											doLastProcessing(nType);
										}
									}.start();

								}
								else
								{
									doRegisterLogoffTransaction(nType);
									Utils.unregisterQbChatListeners();
									Global.gSignedOn = false;
									finish();
								}

							}
							else if (nType == LOGOUT)
							{
								if(bLoginAsSuperUser)
								{
									new Thread()
									{
										@Override
										public void run() {
											doLastProcessing(LOGOUT);
										}
									}.start();

								}
								else
								{
									doRegisterLogoffTransaction(nType);
									//replaceFragment(new LoginFragment());
									mContext.startActivity(new Intent(mContext, LoginActivity.class));
									finish();
									Global.gSignedOn = false;
								}
							}
						}
					}
				}
			});
			dlg.show();
		}catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void doLastProcessing(final int nProcessType)
	{
		if (Utils.isConnectingToInternet(mContext) == false) {
			new Handler(getMainLooper()).post(new Runnable() {
				@Override
				public void run() {
					showErrorDialog("Unable to access network.  Please enable network and try again", nProcessType);
				}
			});

			return;
		}

		new Handler(getMainLooper()).post(new Runnable() {
			@Override
			public void run() {
				showProgressDialog("Processing...");
			}
		});

		//Wait till all upload data has been made
		while(Global.gUploadCustNoList.size() > 0)
		{
			try
			{
				Thread.sleep(100);
			}catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		//Stop Uploading Data to the server.
		Global.gbStopBackgroundUpload =  true;
		while(Global.gbBackgroundUploading)
		{
			try
			{
				Thread.sleep(100);
			}catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		{
			// Make the Logoff transaction
			UTransactionsInfo uTransactionInfo = new UTransactionsInfo();
			uTransactionInfo.nTrxnNo = System.currentTimeMillis();
			uTransactionInfo.strDocType = "LOG";
			uTransactionInfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(mContext, new Date()));
			uTransactionInfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(uTransactionInfo.nTrxnNo);
			if(nProcessType == EXIT_APP)
				uTransactionInfo.strReference = "Exit App";
			else if(nProcessType == END_JOURNEY)
				uTransactionInfo.strReference = "End Journey";
			else if(nProcessType == LOGOUT)
				uTransactionInfo.strReference = "Log Out";
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

			MakeZipFileInfo makeZipFileInfo = new MakeZipFileInfo();
			makeZipFileInfo.nChainNo = 0;
			makeZipFileInfo.nCustNo = 0;
			makeZipFileInfo.trxnList.add(uTransactionInfo.nTrxnNo);

			UploadInfo uploadInfo = new UploadInfo();
			uploadInfo.makeZipFileInfo = makeZipFileInfo;
			Utils.uploadData(mContext, 0, uploadInfo, false);
		}

		//Upload Data to Server
		if( doUploadRemainingFiles() )
		{
			new Handler(getMainLooper()).post(new Runnable() {
				@Override
				public void run() {
					hideProgressDialog();
					switch (nProcessType)
					{
						case END_JOURNEY: case EXIT_APP:
							Utils.unregisterQbChatListeners();
							Global.gSignedOn = false;
							finish();
							break;
						case LOGOUT:
							//replaceFragment(new LoginFragment());
							mContext.startActivity(new Intent(mContext, LoginActivity.class));
							finish();
							Global.gSignedOn = false;
							break;
					}
				}
			});

		}
		else
		{
			new Handler(Looper.getMainLooper()).post(new Runnable() {
				@Override
				public void run() {
					hideProgressDialog();
					showErrorDialog(error, nProcessType);
				}
			});

		}
	}

	private void showErrorDialog(String strMessage, final int nProcessType) {
		try {
			final MessageDialog msgDialog = new MessageDialog(mContext);
		msgDialog.setCancelable(false);
		msgDialog.mStrTitle = Str.UPLOADING_FAILED[Global.LANG];
		msgDialog.mStrMessage = strMessage;
		msgDialog.mStrMiddle = Str.Retry[Global.LANG];
		msgDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				if (msgDialog.mReturnCode == Global.DLG_RETURN_MIDDLE) {
					new Thread(){
						@Override
						public void run() {
							doLastProcessing(nProcessType);
						}
					}.start();

				}
			}
		});
		msgDialog.show();
		}catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private boolean doUploadRemainingFiles()
	{
		String strFTPServerIP = Utils.getStringSetting(mContext, Global.PREF_FTP_SERVER_IP);
		String strFTPUserName = Utils.getStringSetting(mContext, Global.PREF_FTP_USERNAME);
		String strCompanyName = Utils.getStringSetting(mContext, Global.PREF_FTP_COMPANY_NAME);

		UploadDbAdapter uploadDbAdapter = new UploadDbAdapter(mContext);
		uploadDbAdapter.open();
		List<UploadFileInfo> fileInfo = uploadDbAdapter.getUploadFileList();
		uploadDbAdapter.close();
		while (fileInfo.size() > 0) {
			UploadFileInfo uploadFile = fileInfo.get(0);
			if (uploadFile.localPath.length() == 0 || uploadFile.serverPath.length() == 0 ||
					Utils.isFileExist(uploadFile.localPath) == false) {
				uploadDbAdapter.open();
				uploadDbAdapter.deleteUploadFile(uploadFile.idx);
				uploadDbAdapter.close();
				Utils.leaveLog(mContext, "MainActivity", uploadFile, "Error File, File Not Exist");
			} else {
				long fileSize = Utils.getFileSize(uploadFile.localPath);
				if (fileSize == 0) {
					uploadDbAdapter.open();
					uploadDbAdapter.deleteUploadFile(uploadFile.idx);
					uploadDbAdapter.close();
					Utils.leaveLog(mContext, "MainActivity", uploadFile, "File Size is 0");

				} else if (new UploadViaHTTP().uploadFileToFTPServerUsingHttp(strCompanyName, strFTPServerIP, strFTPUserName, uploadFile.localPath, uploadFile.serverPath)) {
					uploadDbAdapter.open();
					uploadDbAdapter.deleteUploadFile(uploadFile.idx);
					uploadDbAdapter.close();
					Utils.leaveLog(mContext, "MainActivity", uploadFile, "Uploaded Success using Http");
				} else if (!Utils.getStringSetting(mContext, Global.PREF_DisableFTP).equals("1") && Utils.uploadFileToFTPServer(mContext, uploadFile.localPath, uploadFile.serverPath)) {
					Utils.leaveLog(mContext, "MainActivity", uploadFile, "Uploaded Failed using Http");
					uploadDbAdapter.open();
					uploadDbAdapter.deleteUploadFile(uploadFile.idx);
					uploadDbAdapter.close();
					Utils.leaveLog(mContext, "MainActivity", uploadFile, "Uploaded Success using FTP & Http");
				} else if (!Utils.isConnectingToInternet(mContext)) {
					error = "Unable to access network.  Please enable radios and try again";
					Global.gbStopBackgroundUpload = false;
					return false;
				} else if (!Utils.checkDirectoryExists(mContext, uploadFile.serverPath)) {
					Utils.leaveLog(mContext, "MainActivity", uploadFile, "Unable to access correct server folder");
					error = "Unable to access correct server folder.  Please contact your administrator";
					Global.gbStopBackgroundUpload = false;
					return false;
				} else {
					Utils.leaveLog(mContext, "MainActivity", uploadFile, "Cannot upload file because of network or permission");
					error = "Cannot upload file because of network weak or permission of directory in ftp server";
					Global.gbStopBackgroundUpload = false;
					return false;
				}
			}
			uploadDbAdapter.open();
			fileInfo = uploadDbAdapter.getUploadFileList();
			uploadDbAdapter.close();
		}
		/*
		if (fileInfo.size() > 0) {
			for (int i = 0; i < fileInfo.size(); i++) {
				if (fileInfo.get(i).localPath.length() == 0 || fileInfo.get(i).serverPath.length() == 0 ||
						Utils.isFileExist(fileInfo.get(i).localPath) == false) {
					uploadDbAdapter.open();
					uploadDbAdapter.deleteUploadFile(fileInfo.get(i).idx);
					uploadDbAdapter.close();
					Utils.leaveLog("MainActivity", fileInfo.get(i), "Error File, File Not Exist");
				} else {
					long fileSize = Utils.getFileSize(fileInfo.get(i).localPath);
					if (fileSize == 0) {
						uploadDbAdapter.open();
						uploadDbAdapter.deleteUploadFile(fileInfo.get(i).idx);
						uploadDbAdapter.close();
						Utils.leaveLog("MainActivity", fileInfo.get(i), "File Size is 0");

					} else if (new UploadViaHTTP().uploadFileToFTPServerUsingHttp(strCompanyName, strFTPServerIP, strFTPUserName, fileInfo.get(i).localPath, fileInfo.get(i).serverPath)) {
						uploadDbAdapter.open();
						uploadDbAdapter.deleteUploadFile(fileInfo.get(i).idx);
						uploadDbAdapter.close();
						Utils.leaveLog("MainActivity", fileInfo.get(i), "Uploaded Success using Http");
					} else if (!Utils.getStringSetting(mContext, Global.PREF_DisableFTP).equals("1") && Utils.uploadFileToFTPServer(mContext, fileInfo.get(i).localPath, fileInfo.get(i).serverPath)) {
						Utils.leaveLog("MainActivity", fileInfo.get(i), "Uploaded Failed using Http");
						uploadDbAdapter.open();
						uploadDbAdapter.deleteUploadFile(fileInfo.get(i).idx);
						uploadDbAdapter.close();
						Utils.leaveLog("MainActivity", fileInfo.get(i), "Uploaded Success using FTP & Http");
					} else if (!Utils.isConnectingToInternet(mContext)) {
						error = "Unable to access network.  Please enable radios and try again";
						Global.gbStopBackgroundUpload = false;
						return false;
					} else if (!Utils.checkDirectoryExists(mContext, fileInfo.get(i).serverPath)) {
						Utils.leaveLog("MainActivity", fileInfo.get(i), "Unable to access correct server folder");
						error = "Unable to access correct server folder.  Please contact your administrator";
						Global.gbStopBackgroundUpload = false;
						return false;
					} else {
						Utils.leaveLog("MainActivity", fileInfo.get(i), "Cannot upload file because of network or permission");
						error = "Cannot upload file because of network weak or permission of directory in ftp server";
						Global.gbStopBackgroundUpload = false;
						return false;
					}
				}
			}
		}
		 */
		Global.gbStopBackgroundUpload = false;
		return true;
	}

	public void registerChatService()
	{
		//Utils.dialogsManager.addManagingDialogsCallbackListener(this);
	}

	private void startMenu1Animation() {

		try {
			if(BluetoothAdapter.getDefaultAdapter() == null)
			{
				findViewById(R.id.txtMakeDeviceDiscoverable).setEnabled(false);
			}
			else if(BluetoothAdapter.getDefaultAdapter().isEnabled() && BluetoothAdapter.getDefaultAdapter().getScanMode() == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE)
			{
				findViewById(R.id.txtMakeDeviceDiscoverable).setEnabled(false);
			}
			else
			{
				findViewById(R.id.txtMakeDeviceDiscoverable).setEnabled(true);
			}
		}catch (Exception e)
		{
			e.printStackTrace();
			findViewById(R.id.txtMakeDeviceDiscoverable).setEnabled(false);
		}

		if (mIsAnimation == true) {
			return;
		}
		if (mShowMenu2 == true) {
			mShowMenu2 = false;
			mLayoutMenu2.setVisibility(View.GONE);
		}
		mIsAnimation = true;
		Animation anim;
		if (mShowMenu1 == false) {
			refreshMenuLanguage();
			mLayoutMenu1.setVisibility(View.VISIBLE);
			anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.top_to_down);
		} else {
			anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.bottom_to_up);
		}

		anim.setAnimationListener(this);
		mLayoutMenu1.startAnimation(anim);
		mShowMenu1 = !mShowMenu1;
	}

	private void startMenu2Animation() {

		mCurrentFragment = getCurrentFragment();
		if(Utils.getBooleanSetting(mContext, Global.PREF_LOGIN_AS_SUPER_VISOR))
		{
			findViewById(R.id.txtPrintDocuments).setVisibility(View.GONE);
			findViewById(R.id.txtVehicleInventory).setVisibility(View.GONE);
			findViewById(R.id.txtPrintDocuments).setVisibility(View.GONE);
			findViewById(R.id.txtSalesTracker).setVisibility(View.GONE);
			findViewById(R.id.txtDiary).setVisibility(View.GONE);
			findViewById(R.id.txtChangeDriver).setVisibility(View.GONE);
			findViewById(R.id.txtChangePrinterType).setVisibility(View.GONE);
			findViewById(R.id.txtWindcaveReader).setVisibility(View.GONE);
			findViewById(R.id.txtDriverBreak).setVisibility(View.GONE);
			findViewById(R.id.txtAddMFA).setVisibility(View.GONE);
		}
		else
		{
			findViewById(R.id.txtPrintDocuments).setVisibility(View.VISIBLE);
			findViewById(R.id.txtVehicleInventory).setVisibility(View.VISIBLE);
			findViewById(R.id.txtPrintDocuments).setVisibility(View.VISIBLE);
			findViewById(R.id.txtSalesTracker).setVisibility(View.VISIBLE);
			findViewById(R.id.txtDiary).setVisibility(View.VISIBLE);
			findViewById(R.id.txtChangeDriver).setVisibility(View.VISIBLE);
			findViewById(R.id.txtChangePrinterType).setVisibility(View.VISIBLE);
			//findViewById(R.id.txtAddMFA).setVisibility(View.VISIBLE);

			boolean bCardPresent = !TextUtils.isEmpty(Utils.getStringSetting(mContext, Global.PREF_WINDCAVE_HITUSERNAME)) && !TextUtils.isEmpty(Utils.getStringSetting(mContext, Global.PREF_WINDCAVE_HITKEY));
			if(Utils.getIntSetting(this, Global.PREF_EWAY_CARD_PROC) == 10 && bCardPresent && !(mCurrentFragment instanceof LoginFragment))
			{
				findViewById(R.id.txtWindcaveReader).setVisibility(View.VISIBLE);
			}
			else
			{
				findViewById(R.id.txtWindcaveReader).setVisibility(View.GONE);
			}

			if(Utils.getStringSetting(this, Global.PREF_ENABLE_BREAKS).equals("1") && mCurrentFragment instanceof SelectCustomerFragment)
			{
				findViewById(R.id.txtDriverBreak).setVisibility(View.VISIBLE);
			}
			else
			{
				findViewById(R.id.txtDriverBreak).setVisibility(View.GONE);
			}

			if (Utils.getStringSetting(this, Global.PREF_DISABLE_LOGOUT).equals("1")) {
				findViewById(R.id.txtLogout).setVisibility(View.GONE);
			}
			else {
				findViewById(R.id.txtLogout).setVisibility(View.VISIBLE);
			}

			if (Global.gFragIdx == Global.FRAG_SELECT_CUSTOMER || Global.gFragIdx == Global.FRAG_PRE_TRIP_TASKS
					|| mCurrentFragment instanceof SelectCustomerFragment || mCurrentFragment instanceof PreTripTasksFragment) {
				findViewById(R.id.txtPrintDocuments).setEnabled(true);
			} else {
				findViewById(R.id.txtPrintDocuments).setEnabled(false);
			}
			if(Global.gCurCustomerInfo != null && Global.gCurCustomerInfo.nCustNo != 0 && Global.gEnteredEndTrip == false) {
				findViewById(R.id.txtDiary).setEnabled(true);
			} else {
				findViewById(R.id.txtDiary).setEnabled(false);
			}
			if(Global.gFragIdx == Global.FRAG_PRE_TRIP_TASKS || Global.gFragIdx == Global.FRAG_TRIP_SETTLEMENT
					|| mCurrentFragment instanceof PreTripTasksFragment || mCurrentFragment instanceof TripSettlementFragment
					|| Global.gEnteredEndTrip )
			{
				findViewById(R.id.txtSalesTracker).setEnabled(false);
			}
			else
			{
				findViewById(R.id.txtSalesTracker).setEnabled(true);
			}
			if(Global.gFragIdx != Global.FRAG_LOGIN)
			{
				DbAdapter dbAdapter = new DbAdapter(mContext);
				dbAdapter.open();
				int nDriverCnt = dbAdapter.getDescTypeList(Global.DRIVER_DESC_TYPE_ID).size();
				int nPrinterTypeCnt = dbAdapter.getDescTypeList(Global.PRINTER_TYPE_DESC_TYPE_ID).size();
				dbAdapter.close();
				if( nDriverCnt > 1 )
				{
					findViewById(R.id.txtChangeDriver).setEnabled(true);
				}
				else
				{
					findViewById(R.id.txtChangeDriver).setEnabled(false);
				}

				if( nPrinterTypeCnt > 1 )
				{
					findViewById(R.id.txtChangePrinterType).setEnabled(true);
				}
				else
				{
					findViewById(R.id.txtChangePrinterType).setEnabled(false);
				}

//				if (Utils.getStringSetting(mContext, Global.PREF_MFA_SECRET).isEmpty()) {
//					findViewById(R.id.txtAddMFA).setEnabled(false);
//				}
//				else {
//					findViewById(R.id.txtAddMFA).setEnabled(true);
//				}
			}
			else
			{
				findViewById(R.id.txtChangeDriver).setEnabled(false);
				findViewById(R.id.txtChangePrinterType).setEnabled(false);
				//findViewById(R.id.txtAddMFA).setEnabled(false);
			}
		}

		Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frameContainer);
		if(Utils.getStringSetting(mContext, Global.PREF_ENDTRIP_HIDE).equals("1"))
		{
			findViewById(R.id.txtEndTrip).setVisibility(View.VISIBLE);
			if(fragment instanceof SelectCustomerFragment)
			{
				findViewById(R.id.txtEndTrip).setEnabled(true);
			}
			else
			{
				findViewById(R.id.txtEndTrip).setEnabled(false);
			}
		}
		else
		{
			findViewById(R.id.txtEndTrip).setVisibility(View.GONE);
		}

		if(!TextUtils.isEmpty(Utils.getStringSetting(this, Global.PREF_DOCUMENTS)) && !(fragment instanceof LoginFragment))
		{
			findViewById(R.id.txtDocuments).setVisibility(View.VISIBLE);
			findViewById(R.id.imgDocuments).setVisibility(View.VISIBLE);
		}
		else
		{
			findViewById(R.id.txtDocuments).setVisibility(View.GONE);
			findViewById(R.id.imgDocuments).setVisibility(View.GONE);
		}

		if(Utils.getBooleanSetting(this, Global.PREF_DEBUG))
		{
			findViewById(R.id.txtSetInvoiceEnding).setVisibility(View.VISIBLE);
		}
		else
		{
			findViewById(R.id.txtSetInvoiceEnding).setVisibility(View.GONE);
		}

		if (mIsAnimation == true) {
			return;
		}
		if (mShowMenu1 == true) {
			mShowMenu1 = false;
			mLayoutMenu1.setVisibility(View.GONE);
		}
		mIsAnimation = true;
		Animation anim;
		if (mShowMenu2 == false) {
			refreshMenuLanguage();
			mLayoutMenu2.setVisibility(View.VISIBLE);
			anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.top_to_down);
		} else {
			anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.bottom_to_up);
		}
		anim.setAnimationListener(this);
		mLayoutMenu2.startAnimation(anim);
		mShowMenu2 = !mShowMenu2;
	}

	private Fragment mCurrentFragment = null;

	public void replaceFragment(Fragment fragment) {
		try
		{
			if (fragment != null) {
				FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
				ft.replace(R.id.frameContainer, fragment);
				ft.addToBackStack(fragment.getClass().getName());
				ft.commit();
				mCurrentFragment = fragment;
			}
		}catch(Exception e)
		{
			e.printStackTrace();
		}

	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			startMenu2Animation();
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}

	@Override
	public void onBackPressed() {
		if (mShowMenu1 == true) {
			startMenu1Animation();
			return;
		}
		if (mShowMenu2 == true) {
			startMenu2Animation();
			return;
		}

		if( Global.gFragIdx == Global.FRAG_SELECT_CUSTOMER){
			return;
		}

		if (Global.gFragIdx == Global.FRAG_TRIP_SETTLEMENT) {
			return;
		}
		if (Global.gFragIdx == Global.FRAG_LOGIN || Global.gFragIdx == Global.FRAG_PRE_TRIP_TASKS) {
			showConfirmDialog(EXIT_APP);
			return;
		}

		/*if(Global.gFragIdx == Global.FRAG_CHEQUE_DETAIL)
		{
			return;
		}*/

		Fragment curFragment = getSupportFragmentManager().findFragmentById(R.id.frameContainer);
		try{

			if(curFragment instanceof StockTakeCountEntryFragment)
			{
				if(((StockTakeCountEntryFragment)curFragment).bModified)
				{
					((StockTakeCountEntryFragment)curFragment).doCofirmBack();
					return;
				}
			}

			if(curFragment instanceof LoadRequestFragment)
			{
				if(((LoadRequestFragment)curFragment).bModified)
				{
					((LoadRequestFragment)curFragment).doCofirmBack();
					return;
				}
			}

			if (Global.gFragIdx == Global.FRAG_INSTORE_ACTIVITIES) {
				if (curFragment instanceof InstoreFragment) {
					if(Global.hotspot != null)
					{
						Global.hotspot.stop();
						Global.hotspot = null;
					}

					InstoreFragment fragment = (InstoreFragment) curFragment;
					if (!fragment.isCompleted() && fragment.isVisit()) {
						fragment.doCompleteVisit();
						return;
					}
				}
			}


			if( Global.gFragIdx == Global.FRAG_MAKE_DELIVERY /*&& mCurrentFragment instanceof MakeDeliveryFragment*/){
					if (curFragment instanceof MakeDeliveryFragment) {
						if( ((MakeDeliveryFragment)curFragment).isModified() )
						{
							((MakeDeliveryFragment)curFragment).ProcessExit();
							return;
						}
					}
			}

			if(curFragment instanceof ChequeDetailFragment)
			{
				ChequeDetailFragment chequeDetailFragment = (ChequeDetailFragment) curFragment;
				if(  chequeDetailFragment.m_bAutoCollected && chequeDetailFragment.mCollectionInfo != null  )
				{
					if(!chequeDetailFragment.bClickedDone)
					{
						return;
					}
				}
			}

			if(curFragment instanceof UpdateConsignmentInventoryFragment)
			{
				UpdateConsignmentInventoryFragment updateConsignmentInventoryFragment = (UpdateConsignmentInventoryFragment) curFragment;
				if(!updateConsignmentInventoryFragment.mExist)
				{
					updateConsignmentInventoryFragment.doProcessBack();
					return;
				}
			}

			if(curFragment instanceof AdjustConsignmentInventoryFragment)
			{
				AdjustConsignmentInventoryFragment adjustConsignmentInventoryFragment = (AdjustConsignmentInventoryFragment) curFragment;
				if(!adjustConsignmentInventoryFragment.mExist)
				{
					adjustConsignmentInventoryFragment.doProcessBack();
					return;
				}
			}

			if(curFragment instanceof AdjustSummaryFragment)
			{
				AdjustSummaryFragment adjustSummaryFragment = (AdjustSummaryFragment) curFragment;
				if((adjustSummaryFragment.mTransferType == 2 || adjustSummaryFragment.mTransferType == 3) && !adjustSummaryFragment.bPossibleBack)
				{
					return;
				}
			}

			if( Global.gFragIdx == Global.FRAG_LOAD_TRUCK_BULK )
			{
				DbAdapter dbAdapter = new DbAdapter(mContext);
				dbAdapter.open();
				dbAdapter.updateSignedLoadHeader();
				dbAdapter.close();
				Global.gUInvenHinfo.truckList.clear();
				try{
					if( curFragment instanceof LoadTruckBulkFragment )
					{
						((LoadTruckBulkFragment)curFragment).mUInvenDList.clear();
					}
				}catch(Exception e)
				{
					e.printStackTrace();
				}
			}
			else
			{
				if( curFragment instanceof LoadTruckBulkFragment )
				{
					DbAdapter dbAdapter = new DbAdapter(mContext);
					dbAdapter.open();
					dbAdapter.updateSignedLoadHeader();
					dbAdapter.close();
					((LoadTruckBulkFragment)curFragment).mUInvenDList.clear();
					Global.gUInvenHinfo.truckList.clear();
				}
				else if( curFragment instanceof SelectCustomerFragment)
				{
					if( !((SelectCustomerFragment)curFragment).getBackProcessed() && Utils.getIntSetting(mContext, Global.PREF_LOADMULTIALL) > 0 )
					{
						((SelectCustomerFragment)curFragment).SpecialLoadProcessWithApi();
						return;
					}
				}
				else if( curFragment instanceof CardDetailFragment)
				{
					if(!((CardDetailFragment)curFragment).isCalledCancelProcessing())
					{
						((CardDetailFragment)curFragment).calledCancelProcess();
						return;
					}
				}
			}


			int count = getSupportFragmentManager().getBackStackEntryCount();
			if (count > 0) {
				int nPrevFragIdx = Global.gFragIdx;
				super.onBackPressed();
				if (nPrevFragIdx == Global.FRAG_FILTER_LOCATION) {
					sendBroadcast(new Intent(Global.ACTION_FILTER_LOCATION));
				} else if (nPrevFragIdx == Global.FRAG_FILTER_BY) {
					sendBroadcast(new Intent(Global.ACTION_FILTER_BY));
				} else if (nPrevFragIdx == Global.FRAG_SEARCH_CUSTOMER) {
					sendBroadcast(new Intent(Global.ACTION_REFRESH_CUSTOMER_LIST));
				}
			} else {
				showConfirmDialog(EXIT_APP);
			}
		}catch(Exception e)
		{
			e.printStackTrace();
		}


	}

	public void onBack() {
		if (Global.gFragIdx == Global.FRAG_TRIP_SETTLEMENT) {
			return;
		}
		if (Global.gFragIdx == Global.FRAG_LOGIN || Global.gFragIdx == Global.FRAG_PRE_TRIP_TASKS) {
			showConfirmDialog(EXIT_APP);
			return;
		}
		Fragment curFragment = getSupportFragmentManager().findFragmentById(R.id.frameContainer);

		try
		{
			if (Global.gFragIdx == Global.FRAG_INSTORE_ACTIVITIES) {
				if (curFragment instanceof InstoreFragment) {
					if(Global.hotspot != null)
					{
						Global.hotspot.stop();
						Global.hotspot = null;
					}
					InstoreFragment fragment = (InstoreFragment) curFragment;
					if (!fragment.isCompleted() && fragment.isVisit()) {
						fragment.doComplete();
						return;
					}
				}
			}

			if( Global.gFragIdx == Global.FRAG_MAKE_DELIVERY /*&& mCurrentFragment instanceof MakeDeliveryFragment*/){
				if (curFragment instanceof MakeDeliveryFragment) {
					MakeDeliveryFragment fragment = (MakeDeliveryFragment) curFragment;
					if( fragment.isModified() )
					{
						fragment.ProcessExit();
						return;
					}
				}
			}

			if( Global.gFragIdx == Global.FRAG_LOAD_TRUCK_BULK )
			{
				DbAdapter dbAdapter = new DbAdapter(mContext);
				dbAdapter.open();
				dbAdapter.updateSignedLoadHeader();
				dbAdapter.close();
				Global.gUInvenHinfo.truckList.clear();
				try{
					if( curFragment instanceof LoadTruckBulkFragment )
					{
						((LoadTruckBulkFragment)curFragment).mUInvenDList.clear();
					}
				}catch(Exception e)
				{
					e.printStackTrace();
				}
			}
			else
			{
				if( curFragment instanceof LoadTruckBulkFragment )
				{
					DbAdapter dbAdapter = new DbAdapter(mContext);
					dbAdapter.open();
					dbAdapter.updateSignedLoadHeader();
					dbAdapter.close();
					((LoadTruckBulkFragment)curFragment).mUInvenDList.clear();
					Global.gUInvenHinfo.truckList.clear();
				}
			}

			int count = getSupportFragmentManager().getBackStackEntryCount();
			if (count > 0) {
				int nPrevFragIdx = Global.gFragIdx;
				super.onBackPressed();
				if (nPrevFragIdx == Global.FRAG_FILTER_LOCATION) {
					sendBroadcast(new Intent(Global.ACTION_FILTER_LOCATION));
				} else if (nPrevFragIdx == Global.FRAG_FILTER_BY) {
					sendBroadcast(new Intent(Global.ACTION_FILTER_BY));
				} else if (nPrevFragIdx == Global.FRAG_SEARCH_CUSTOMER) {
					sendBroadcast(new Intent(Global.ACTION_REFRESH_CUSTOMER_LIST));
				}
			} else {
				showConfirmDialog(EXIT_APP);
			}
		}catch(Exception e)
		{
			e.printStackTrace();
		}

	}

	public boolean m_bClicked = false;
	@Override
	public void onClick(View v) {
		if( m_bClicked ) return;
		m_bClicked = true;
//		switch (v.getId()) {
		int nId = v.getId();
		if (nId == R.id.imgMenu1) {
			startMenu1Animation();
		} else if (nId == R.id.imgMenu2) {
			startMenu2Animation();
		}else if (nId == R.id.mark_logo) {
			Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frameContainer);
			if( !(fragment instanceof LoginFragment) && TextUtils.isEmpty(Utils.getStringSetting(mContext, Global.PREF_COMPANY_NAME)))
			{
				m_bClicked = false;
				return;
			}
			else if( fragment instanceof LoginFragment ) {
				m_bClicked = false;
				return;
			}

			if(Global.gCurQBUser != null && Global.gCurQBUser.getId() != null)
			{
				if( ChatHelper.getInstance().isLogged() )
				{
					startActivity(new Intent(MainActivity.this, DialogActivity.class));
				}
				else
				{
					if(!MainActivity.this.isDestroyed() && !MainActivity.this.isFinishing())
					{
						Toast.makeText(MainActivity.this, Str.YouHaventRegisteredToourLoginSystem[Global.LANG], Toast.LENGTH_LONG).show();
					}
				}
			}
			else
			{
				if(!MainActivity.this.isDestroyed() && !MainActivity.this.isFinishing())
				{
					Toast.makeText(MainActivity.this, Str.YouHaventRegisteredToourLoginSystem[Global.LANG], Toast.LENGTH_LONG).show();
				}
			}
		} else if (nId == R.id.txtHelp) {
			startMenu1Animation();
		} else if (nId == R.id.txtAbout) {
			startMenu1Animation();
			Intent intent = new Intent(this, AboutActivity.class);
			startActivity(intent);
			overridePendingTransition(R.anim.start1, R.anim.start2);
		} else if (nId == R.id.txtMakeDeviceDiscoverable) {
			startMenu1Animation();
			doMakeDeviceDiscoverable();
		} else if (nId == R.id.txtCommunication) {
			if (Global.gFragIdx == Global.FRAG_CONFIGURATION) {
				startMenu1Animation();
			} else {
				mLayoutMenu1.setVisibility(View.GONE);
				mShowMenu1 = false;
				if (Utils.getIntSetting(this, Global.PREF_CONFIG_STATUS) == Global.CONF_GOT_LOADED) {
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
									String strPINcode = Utils.getStringSetting(mContext, Global.PREF_COMMUNICATION_PIN_CODE);
									if (/*inputDlg.mStrInputed.length() > 0 && */strPINcode.equals(inputDlg.mStrInputed) == true) {
										replaceFragment(new MainConfigurationFragment());
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
				} else {
					replaceFragment(new MainConfigurationFragment());
				}
			}
		} else if (nId == R.id.txtEndJourney) {
			startMenu2Animation();
			showConfirmDialog(END_JOURNEY);
		} else if (nId == R.id.txtLogout) {
			startMenu2Animation();
			if (Global.gFragIdx != Global.FRAG_LOGIN) {
				showConfirmDialog(LOGOUT);
			}
		} else if (nId == R.id.txtChangeDriver) {
			startMenu2Animation();
			doChangeDriver();
		} else if (nId == R.id.txtChangePrinterType) {
			startMenu2Animation();
			doChangePrinterType();
		} else if (nId == R.id.txtEndTrip){
			startMenu2Animation();
			doEndTrip();
		} else if (nId == R.id.txtMultiLang) {
			showLanguages();
			startMenu1Animation();
		} else if (nId == R.id.txtVehicleInventory) {
			//if (Utils.getLongSetting(mContext, Global.PREF_UINVEN_TRXN_NO) > 0)
			if(Utils.getBooleanSetting(mContext, Global.PREF_PASSED_LOAD_SECTION) || Utils.getLongSetting(mContext, Global.PREF_UINVEN_TRXN_NO) != 0)
			{
				if(!Utils.getBooleanSetting(mContext, String.format(Locale.ENGLISH, Global.PREF_END_TRIP_CHECKED, 1)))
				{
					Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frameContainer);
					if( !(fragment instanceof EndingInventoryFragment) && !(fragment instanceof EndingInventorySummaryFragment)
							&& !(fragment instanceof SpoilEntryFragment))
					{
						startMenu2Animation();
						if (Global.gFragIdx != Global.FRAG_LOGIN && Global.gFragIdx != Global.FRAG_VEHICLE_INVENTORY) {
							replaceFragment(new VehicleInventoryFragment());
						}
					}
				}
			}
		} else if (nId == R.id.txtPrintDocuments) {
			if (Global.gEnteredEndTrip == false) {
				startMenu2Animation();
				if (Global.gFragIdx != Global.FRAG_LOGIN && Global.gFragIdx != Global.FRAG_PRINT_DOCUMENTS) {
					replaceFragment(new PrintDocumentsFragment());
				}
			}
		} else if( nId == R.id.txtSalesTracker ) {
			startMenu2Animation();
			if (Global.gFragIdx != Global.FRAG_LOGIN && Global.gFragIdx != Global.FRAG_SALES_TRACKER) {
				replaceFragment(new SalesTrackerFragment());
			}
		} else if( nId == R.id.txtDiary) {
			if(Global.gCurCustomerInfo != null && Global.gCurCustomerInfo.nCustNo != 0 && Global.gEnteredEndTrip == false){
				startMenu2Animation();
				replaceFragment(new CustomerDiaryFragment());
			}
		} else if( nId == R.id.txtWindcaveReader) {
			startMenu2Animation();
			try {
				WindcaveStationIDDialog dialog = new WindcaveStationIDDialog(this, null);
				dialog.setCancelable(false);
				dialog.show();
			}catch (Exception e)
			{
				e.printStackTrace();
			}

		} else if(nId == R.id.txtSetInvoiceEnding) {
			startMenu2Animation();
			try {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("Input the Invoice Ending");

				final EditText input = new EditText(this);
				input.setInputType(InputType.TYPE_CLASS_NUMBER);
				String strEnding = String.valueOf(Utils.getIntSetting(mContext, String.format(Locale.ENGLISH, Global.PREF_PDF_SEQUENCE_NO, Global.gTripNumber)));
				input.setText(strEnding);
				input.setSelection(strEnding.length(), strEnding.length());
				builder.setView(input);
				builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Utils.setIntSetting(mContext, String.format(Locale.ENGLISH, Global.PREF_PDF_SEQUENCE_NO, Global.gTripNumber), Utils.parseIntFromString(input.getText().toString()));
					}
				});
				builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});

				builder.show();
			}catch (Exception e)
			{
				e.printStackTrace();
			}

		} else if(nId == R.id.txtDocuments || nId == R.id.imgDocuments){
			startMenu2Animation();
			doLaunchDocument();
		} else if(nId == R.id.txtDriverBreak) {
			startMenu2Animation();
			doDriverBreak();
		} else if(nId == R.id.txtAddMFA) {
			//startMenu2Animation();
			//doAddMFA();
		}

		m_bClicked = false;
	}

	private void doAddMFA() {
		AddMFADialog dialog = new AddMFADialog(mContext);
		dialog.show();
	}

	private void doFireBreakTime(DescTypeInfo info)
	{
		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		info.strValue5 = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss").format(new Date());
		dbAdapter.updateDescTypeInfo(info);
		dbAdapter.close();

		final int nMinutes = Utils.parseIntFromString(info.strValue1);
		if(nMinutes == 0) return;

		Utils.setStringSetting(mContext, Global.PREF_DRIVER_BREAK_DOWN_TYPE_INFO, info.toJSON());

		countDownDialog = new DriverBreakCountDownDialog(MainActivity.this, () -> {
			//Save Last visit time in here
			Utils.setLongSetting(MainActivity.this, Global.PREF_LAST_VISIT_BREAK, 0);
			countDownDialog = null;
		}, info, false);
		countDownDialog.setCancelable(true);
		countDownDialog.show();
	}
	private void doDriverBreak()
	{
		DriverBreakDialog dialog = new DriverBreakDialog(this, new DriverBreakDialog.Callback() {
			@Override
			public void onReturn() {

			}

			@Override
			public void onSelect(DescTypeInfo info) {
				if(info == null) return;
				doFireBreakTime(info);
			}
		});
		dialog.setCancelable(false);
		dialog.show();
	}

	public void BreakDownContinue()
	{
		String strJSON = Utils.getStringSetting(mContext, Global.PREF_DRIVER_BREAK_DOWN_TYPE_INFO);
		if(!TextUtils.isEmpty(strJSON))
		{
			DescTypeInfo info = null;
			try {
				info = DescTypeInfo.fromJSON(strJSON);

				countDownDialog = new DriverBreakCountDownDialog(MainActivity.this, () -> {
					//Save Last visit time in here
					Utils.setLongSetting(MainActivity.this, Global.PREF_LAST_VISIT_BREAK, 0);
					countDownDialog = null;
				}, info, true);
				countDownDialog.setCancelable(true);
				countDownDialog.show();
			}catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		if(countDownDialog != null)
		{
			lStopTime = System.currentTimeMillis();
			countDownDialog.doStopCountDown(false);
		}
	}


	private void doMakeDeviceDiscoverable()
	{
		if(BluetoothAdapter.getDefaultAdapter().isEnabled())
		{
			if(BluetoothAdapter.getDefaultAdapter().getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE)
			{
				Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
				intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 30);
				startActivity(intent);
			}
		}
		else
		{
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}

	}

	private boolean isPackageExisted(String targetPackage){
		List<ApplicationInfo> packages;
		PackageManager pm;

		pm = getPackageManager();
		packages = pm.getInstalledApplications(0);
		for (ApplicationInfo packageInfo : packages) {
			if(packageInfo.packageName.equals(targetPackage))
				return true;
		}
		return false;
	}

	private void doLaunchDocument()
	{
		final String strPackageName = Utils.getStringSetting(this, Global.PREF_DOCUMENTS);

		if(!TextUtils.isEmpty(strPackageName) && isPackageExisted(strPackageName))
		{
			PackageManager manager = getPackageManager();
			Intent i = manager.getLaunchIntentForPackage(strPackageName);
			i.addCategory(Intent.CATEGORY_LAUNCHER);
			startActivity(i);
		}
		else
		{
			try {
				//Show Message Dialog.
				final MessageDialog msgDialog = new MessageDialog(mContext);
				msgDialog.setCancelable(false);
				msgDialog.mStrTitle = Str.Warning[Global.LANG];
				msgDialog.mStrMessage = String.format(Locale.ENGLISH, Str.NotInstalledPackage[Global.LANG], Utils.getAppNameFromPackage(strPackageName));
				msgDialog.mStrLeft = Str.InstallNow[Global.LANG];
				msgDialog.mStrRight = Str.Return[Global.LANG];
				msgDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
							try {
								startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + strPackageName)));
							} catch (android.content.ActivityNotFoundException anfe) {
								startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + strPackageName)));
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
	}

	@Override
	public void onAnimationEnd(Animation animation) {
		if (mShowMenu1 == false) {
			mLayoutMenu1.setVisibility(View.GONE);
		}
		if (mShowMenu2 == false) {
			mLayoutMenu2.setVisibility(View.GONE);
		}
		mIsAnimation = false;
	}

	@Override
	public void onAnimationRepeat(Animation animation) {
	}

	@Override
	public void onAnimationStart(Animation animation) {
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mIsRunning = false;
		Global.gLoggedIn = false;
		if(Utils.dialogsManager != null )
		{
			Utils.dialogsManager.removeManagingDialogsCallbackListener(this);
			//Utils.unregisterQbChatListeners();
		}
//		doUnregisterBluetoothTransfer();
//		if(bluetoothHeadset != null)
//		{
//			BluetoothAdapter.getDefaultAdapter().closeProfileProxy(BluetoothProfile.HEADSET, bluetoothHeadset);
//		}

		stopService(new Intent(this, GattServerService.class));
	}

	public Intent getCustomIntent() {
		return customIntent;
	}

	public void setCustomIntent(Intent customeIntent) {
		this.customIntent = customeIntent;
	}


	@Override
	public void onStart() {
		super.onStart();

/*		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.
		client.connect();
		Action viewAction = Action.newAction(
				Action.TYPE_VIEW, // TODO: choose an action type.
				"Main Page", // TODO: Define a title for the content shown.
				// TODO: If you have web page content that matches this app activity's content,
				// make sure this auto-generated web page URL is correct.
				// Otherwise, set the URL to null.
				Uri.parse("http://host/path"),
				// TODO: Make sure this auto-generated app URL is correct.
				Uri.parse("android-app://dsd.dsdvansales.activities/http/host/path")
		);
		AppIndex.AppIndexApi.start(client, viewAction);*/

		GattServiceConn latestGattServiceConn = new GattServiceConn();
		if (bindService(new Intent(this, GattServerService.class), latestGattServiceConn, 0)) {
			gattServiceConn = latestGattServiceConn;
		}
	}

	@SuppressLint("RestrictedApi")
	@Override
	public void onStop() {
		super.onStop();

		if (gattServiceConn != null) {
			unbindService(gattServiceConn);
			gattServiceConn = null;
		}
/*		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.
		Action viewAction = Action.newAction(
				Action.TYPE_VIEW, // TODO: choose an action type.
				"Main Page", // TODO: Define a title for the content shown.
				// TODO: If you have web page content that matches this app activity's content,
				// make sure this auto-generated web page URL is correct.
				// Otherwise, set the URL to null.
				Uri.parse("http://host/path"),
				// TODO: Make sure this auto-generated app URL is correct.
				Uri.parse("android-app://dsd.dsdvansales.activities/http/host/path")
		);
		AppIndex.AppIndexApi.end(client, viewAction);
		client.disconnect();*/
	}

	@Override
	public void onDialogCreated(QBChatDialog chatDialog) {
			}

	@Override
	public void onDialogUpdated(String chatDialog) {

	}

	@Override
	public void onNewDialogLoaded(QBChatDialog chatDialog) {

	}


	List<QBChatDialog> dialogList = new ArrayList<>();
	public void checkMessageStatus()
	{
		if(mIsRunning)
		{
			Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frameContainer);
			if( fragment != null && !(fragment instanceof LoginFragment) && TextUtils.isEmpty(Utils.getStringSetting(mContext, Global.PREF_COMPANY_NAME)))
			{
				return;
			}

			if(fragment instanceof LoginFragment)
			{
				new Handler(getMainLooper()).postDelayed(new Runnable() {
					@Override
					public void run() {
						findViewById(R.id.mark_chat).setVisibility(View.GONE);
						checkMessageStatus();
					}
				}, 1000);
				return;
			}

			new Thread()
			{
				@Override
				public void run() {
					if(Global.gCurQBUser == null || ChatHelper.getInstance() == null || !ChatHelper.getInstance().isLogged())
					{
						dialogList.clear();
						new Handler(getMainLooper()).postDelayed(new Runnable() {
							@Override
							public void run() {
								findViewById(R.id.mark_chat).setVisibility(View.GONE);
								checkMessageStatus();
							}
						}, 1000);
						return;
					}

					QBRequestGetBuilder requestBuilder = new QBRequestGetBuilder();
					requestBuilder.setSkip(0);
					requestBuilder.setLimit(ChatHelper.DIALOG_ITEMS_PER_PAGE);
					try {
						List<QBChatDialog> dataList = QBRestChatService.getChatDialogs(QBDialogType.PRIVATE, requestBuilder).perform();
						dialogList.clear();

						if(dataList != null && dataList.size() > 0)
						{
							dialogList.addAll(dataList);
						}

						dataList = QBRestChatService.getChatDialogs(QBDialogType.GROUP, requestBuilder).perform();
						if(dataList != null && dataList.size() > 0)
						{
							dialogList.addAll(dataList);
						}

						dataList = QBRestChatService.getChatDialogs(QBDialogType.PUBLIC_GROUP, requestBuilder).perform();
						if(dataList != null && dataList.size() > 0)
						{
							dialogList.addAll(dataList);
						}

					} catch (Exception e) {
						e.printStackTrace();
					}

					boolean bExist = false;
					for(QBChatDialog dialog : dialogList)
					{
						if(getUnreadMsgCount(dialog) > 0)
						{
							bExist = true;
							new Handler(getMainLooper()).post(new Runnable() {
								@Override
								public void run() {
									findViewById(R.id.mark_chat).setVisibility(View.VISIBLE);
									checkMessageStatus();
								}
							});
							break;
						}
					}
					if(!bExist)
					{
						new Handler(getMainLooper()).post(new Runnable() {
							@Override
							public void run() {
								findViewById(R.id.mark_chat).setVisibility(View.GONE);
								checkMessageStatus();
							}
						});
					}
				}
			}.start();
		}
	}

	private int getUnreadMsgCount(QBChatDialog chatDialog){
		Integer unreadMessageCount = chatDialog.getUnreadMessageCount();
		if (unreadMessageCount == null) {
			return 0;
		} else {
			return unreadMessageCount;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
		{
			if(Utils.mMainThread == null || !Utils.mMainThread.isAlive() || Utils.mMainThread.isInterrupted())
			{
				if(Utils.mMainThread != null) Utils.mMainThread.isRunning = false;
				Utils.mMainThread = null;
				JobScheduler jobScheduler =
						(JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
				assert jobScheduler != null;
				jobScheduler.schedule(new JobInfo.Builder(MainJobService.MAIN_SERVICE_JOB_ID,
						new ComponentName(this, MainJobService.class))
						.setOverrideDeadline(500)
						.setPersisted(true)
						.setRequiresCharging(false)
						.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
						.setRequiresDeviceIdle(false)
						.build());
			}
		}

		if(countDownDialog != null)
		{
			int nPassedCnt = 0;
			if(lStopTime != 0)
			{
				nPassedCnt = (int)((System.currentTimeMillis() - lStopTime) / 1000);
			}
			countDownDialog.doCountDown(nPassedCnt);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == RESULT_TAKE_PHOTO)
		{
			if (resultCode == Activity.RESULT_OK) {

				String strPhotoPath = "";

				if(Build.VERSION.SDK_INT >= 24)
				{
					strPhotoPath = mCurrentPhotoPath;
				}
				else
				{
					File f = new File(Global.ROOT_LOCATION + "/temp.jpg");
					strPhotoPath = f.getAbsolutePath();
				}

				String strNewTempPath = Global.PHOTO_DIR + "tmp.jpg";

				if(!Utils.isFileExist(strNewTempPath))
				{
					Utils.fileDelete(strNewTempPath);
				}
				new File(strPhotoPath).renameTo(new File(strNewTempPath));

				if(Global.gCustomerNoteDialog != null)
				{
					Global.gCustomerNoteDialog.doTakePhoto(strNewTempPath);
				}
			} else if (resultCode != Activity.RESULT_CANCELED) {
				Utils.printDebug(mContext, Str.Error_occurs[Global.LANG] + resultCode, false);
			}
		}
//		else if(requestCode == Global.EDIT_PHOTO )
//		{
//			String strPhotoPath = "";
//
//			if(Build.VERSION.SDK_INT >= 24)
//			{
//				strPhotoPath = mCurrentPhotoPath;
//			}
//			else
//			{
//				File f = new File(Global.ROOT_LOCATION + "/temp.jpg");
//				strPhotoPath = f.getAbsolutePath();
//			}
//
//			if(!Utils.isFileExist(Global.PHOTO_DIR + "tmp.jpg"))
//			{
//				new File(strPhotoPath).renameTo(new File(Global.PHOTO_DIR + "tmp.jpg"));
//			}
//			else
//			{
//				Utils.fileDelete(strPhotoPath);
//			}
//
//			if(resultCode == Activity.RESULT_OK && Global.gCustomerNoteDialog != null)
//			{
//				Global.gCustomerNoteDialog.doTakePhoto(Global.PHOTO_DIR + "tmp.jpg");
//				Global.gCustomerNoteDialog = null;
//			}
//		}
		else
		{
			if (requestCode == REQUEST_ENABLE_BT) {
				if (resultCode == RESULT_OK) {
					doMakeDeviceDiscoverable();
				} else if (resultCode == RESULT_CANCELED) {

				}
			}
			else
			{
				super.onActivityResult(requestCode, resultCode, data);
			}
		}
	}

	public void initMintegrate()
	{
		MTGAppSwitch.setIsProduction(true);
		String apiKey = Utils.getStringSetting(this, Global.PREF_MINTAPI);
		String apiSecret = Utils.getStringSetting(this, Global.PREF_MINTSECRET);
		if(!TextUtils.isEmpty(apiKey) && !TextUtils.isEmpty(apiSecret))
		{
			MTGAppSwitch.initialise(apiKey, apiSecret, "mintegrate", MTGAppSwitch.CountryCode.MTGCountryCodeAustralia);
		}
	}

	@SuppressLint("RestrictedApi")
	public void doMintPayment(String reference, int nAmount, long nTrxnNo)
	{
		try
		{
			MTGAppSwitchRequest mtgAppSwitchRequest = new MTGAppSwitchRequest();

			mtgAppSwitchRequest.setExternalReference(String.valueOf(nTrxnNo));
			mtgAppSwitchRequest.setNotes(String.valueOf(Global.gCurCustomerInfo.nCustNo));
			mtgAppSwitchRequest.setAmount(String.valueOf(nAmount));
			if(TextUtils.isEmpty(Utils.getStringSetting(this, Global.PREF_MINTUSER)))
			{
				if(!MainActivity.this.isDestroyed() && !MainActivity.this.isFinishing())
				{
					Toast.makeText(this, Str.MintUserNameNotSet[Global.LANG], Toast.LENGTH_LONG).show();
				}
				Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frameContainer);
				if( fragment instanceof DeliverySummaryFragment )
				{
					((DeliverySummaryFragment)fragment).doProcessMintCard(null);
				}
				return;
			}
			mtgAppSwitchRequest.setMuid(Utils.getStringSetting(this, Global.PREF_MINTUSER));
			mtgAppSwitchRequest.setRememberLastLogin(true);

			MTGAppSwitch.submitPaymentWithExternalReference(mtgAppSwitchRequest, this);

		}catch (Exception e)
		{
			e.printStackTrace();
			Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frameContainer);
			if( fragment instanceof DeliverySummaryFragment )
			{
				((DeliverySummaryFragment)fragment).doProcessMintCard(null);
			}
			else if(fragment instanceof PaymentCollectionFragment)
			{
				((PaymentCollectionFragment)fragment).doProcessMintCard(null);
			}
		}

	}

	@Override
	protected void onRestart() {
		super.onRestart();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		try
		{
			MTGAppSwitchResponse response = MTGAppSwitch.responseWithUrl(intent.getData());
			if(response != null)
			{
				//
				/*String strJSON = "";
				try
				{
					File file = new File(Global.ROOT_LOCATION + "Log.json");
					Gson gson = new Gson();

					strJSON = gson.toJson(response);
					FileOutputStream fos = new FileOutputStream(file);
					fos.write(strJSON.getBytes());
					fos.close();
				}catch (Exception e)
				{
					e.printStackTrace();
				}
				final String finalStrJSON = strJSON;
				new Handler(getMainLooper()).post(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(MainActivity.this, finalStrJSON, Toast.LENGTH_LONG).show();
					}
				});*/
				//
				if(!response.isAborted())
				{
					if(response.getStatus().toUpperCase().equals("APPROVED"))
					{
						Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frameContainer);
						if( fragment instanceof DeliverySummaryFragment )
						{
							((DeliverySummaryFragment)fragment).doProcessMintCard(response);
						}
						else if(fragment instanceof PaymentCollectionFragment)
						{
							((PaymentCollectionFragment)fragment).doProcessMintCard(response);
						}
					}
				}
			}
		}catch (final Exception e)
		{
			e.printStackTrace();
			if(!TextUtils.isEmpty(e.getMessage()))
			{
				new Handler(getMainLooper()).post(new Runnable() {
					@Override
					public void run() {
						if(!MainActivity.this.isDestroyed() && !MainActivity.this.isFinishing())
						{
							Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
						}
					}
				});
			}
		}

	}

	private void doEndTrip()
	{
		if(Utils.getStringSetting(mContext, Global.PREF_ENDTRIP_HIDE).equals("1"))
		{
			Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frameContainer);
			if(fragment instanceof SelectCustomerFragment)
			{
				((SelectCustomerFragment)fragment).showEndTripDialog();
			}
		}
	}

	private void doChangeDriver()
	{
		try {
			UpdateDriverDialog dialog = new UpdateDriverDialog(this, false);
			dialog.show();
		}catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void doChangePrinterType()
	{
		try {
			UpdatePrinterTypeDialog dialog = new UpdatePrinterTypeDialog(this);
			dialog.show();
		}catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void refreshDriverName()
	{
		Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frameContainer);
		((BaseFragment)fragment).refreshDriverName();
	}

	public void setMarkVisitAutoCollect(boolean b)
	{
		isVisitChequeDetails = b;
	}

	public boolean getMarkVisitAutoCollect()
	{
		return isVisitChequeDetails;
	}

	public Fragment getCurrentFragment()
	{
		Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frameContainer);
		return fragment;
	}

	//Print the document
	boolean mIsCreatedPDF = false;
	String mStrMacAddress = "";
	String mStrFriendlyName = "";
	String mStrPDFPath = "";
	public void doPrint(String strPath)
	{
		mIsCreatedPDF = true;
		if (BluetoothAdapter.getDefaultAdapter().isEnabled() == false) {
			try {
				final MessageDialog msgDialog = new MessageDialog(this);
				msgDialog.setCancelable(false);
				msgDialog.mStrMessage = Str.TurnOnBluetooth_alert[Global.LANG];
				msgDialog.mStrLeft = Str.Cancel[Global.LANG];
				msgDialog.mStrRight = Str.Retry[Global.LANG];
				msgDialog.setOnDismissListener(dialog -> {
					if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
						doPrint(strPath);
					} else if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {

					}
				});
				msgDialog.show();
			}catch (Exception e)
			{
				e.printStackTrace();
			}
			return;
		}
		mStrPDFPath = strPath;
		mStrMacAddress = Utils.getStringSetting(this, Global.PREF_PRINTER_MAC_ADDRESS);
		if (mStrMacAddress.length() == 0) {
			if(Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 1)
			{
				discoveryPrinters();
			}
			else if(Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 2)
			{
				discoveryHoneywellPrinters();
			}
			else if(Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 3)
			{
				discoveryHoneywellLinePrinters();
			}
			else if(Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 4)
			{
				discoveryBixolonPrinters();
			}
		} else {
			if(Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 1)
			{
				new PrintPdfTask().execute();
			}
			else if(Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 2)
			{
				doHoneywellPrintPdfTask();
			}
			else if(Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 3)
			{
				doHoneywellLinePrintPdfTask();
			}
			else if(Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 4)
			{
				doBixolonPrintPdfTask();
			}
		}
	}

	private void discoveryHoneywellPrinters()
	{
		showProgressDialog(Str.DiscoveryingHoneywellPrinters[Global.LANG]);
		new Thread(() -> {
			Looper.prepare();
			try {
				final CountDownLatch latch = new CountDownLatch(1);
				final boolean[] result = {false};
				BluetoothDiscoverer.findPrinters(mContext, new DiscoveryHandler() {
					List<String> deviceList = new ArrayList<>();
					@Override
					public void foundPrinter(DiscoveredPrinter printer) {
						String strFriendlyName = "";
						if (printer instanceof DiscoveredPrinterBluetooth) {
							strFriendlyName = ((DiscoveredPrinterBluetooth) printer).friendlyName;
						}
						String strDevice = printer.address + "\n";
						if (strFriendlyName != null) {
							strDevice += strFriendlyName;
						} else {
							strDevice += "Unknown";
						}
						deviceList.add(strDevice);
					}

					@Override
					public void discoveryFinished() {
						hideProgressDialog();
						if (deviceList.size() > 0) {
							showDeviceList(deviceList);
							result[0] = true;
						}
						else
						{
							result[0] = false;
						}
						latch.countDown();
					}

					@Override
					public void discoveryError(String message) {
						Utils.printDebug(mContext, "Discovery error - " + message);
						result[0] = false;
						latch.countDown();
					}
				});

				latch.await();
				if(result[0] == false)
				{
					hideProgressDialog();
					mHander.sendEmptyMessage(0);

				}

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				Looper.myLooper().quit();
			}
		}).start();
	}

	private void discoveryHoneywellLinePrinters()
	{
		showProgressDialog(Str.DiscoveryingHoneywellPrinters[Global.LANG]);
		new Thread(() -> {
			Looper.prepare();
			try {
				final CountDownLatch latch = new CountDownLatch(1);
				final boolean[] result = {false};
				BluetoothDiscoverer.findPrinters(mContext, new DiscoveryHandler() {
					List<String> deviceList = new ArrayList<>();
					@Override
					public void foundPrinter(DiscoveredPrinter printer) {
						String strFriendlyName = "";
						if (printer instanceof DiscoveredPrinterBluetooth) {
							strFriendlyName = ((DiscoveredPrinterBluetooth) printer).friendlyName;
						}
						String strDevice = printer.address + "\n";
						if (strFriendlyName != null) {
							strDevice += strFriendlyName;
						} else {
							strDevice += "Unknown";
						}
						deviceList.add(strDevice);
					}

					@Override
					public void discoveryFinished() {
						hideProgressDialog();
						if (deviceList.size() > 0) {
							showDeviceList(deviceList);
							result[0] = true;
						}
						else
						{
							result[0] = false;
						}
						latch.countDown();
					}

					@Override
					public void discoveryError(String message) {
						Utils.printDebug(mContext, "Discovery error - " + message);
						result[0] = false;
						latch.countDown();
					}
				});

				latch.await();
				if(result[0] == false)
				{
					hideProgressDialog();
					mHander.sendEmptyMessage(0);

				}

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				Looper.myLooper().quit();
			}
		}).start();
	}

	private void discoveryBixolonPrinters()
	{
		showProgressDialog(Str.DiscoveryingBixolonPrinters[Global.LANG]);
		new Thread(() -> {
			Looper.prepare();
			try {
				final CountDownLatch latch = new CountDownLatch(1);
				final boolean[] result = {false};
				BluetoothDiscoverer.findPrinters(mContext, new DiscoveryHandler() {
					List<String> deviceList = new ArrayList<>();
					@Override
					public void foundPrinter(DiscoveredPrinter printer) {
						String strFriendlyName = "";
						if (printer instanceof DiscoveredPrinterBluetooth) {
							strFriendlyName = ((DiscoveredPrinterBluetooth) printer).friendlyName;
						}
						String strDevice = printer.address + "\n";
						if (strFriendlyName != null) {
							strDevice += strFriendlyName;
						} else {
							strDevice += "Unknown";
						}
						deviceList.add(strDevice);
					}

					@Override
					public void discoveryFinished() {
						hideProgressDialog();
						if (deviceList.size() > 0) {
							showDeviceList(deviceList);
							result[0] = true;
						}
						else
						{
							result[0] = false;
						}
						latch.countDown();
					}

					@Override
					public void discoveryError(String message) {
						Utils.printDebug(mContext, "Discovery error - " + message);
						result[0] = false;
						latch.countDown();
					}
				});

				latch.await();
				if(result[0] == false)
				{
					hideProgressDialog();
					mHander.sendEmptyMessage(0);

				}

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				Looper.myLooper().quit();
			}
		}).start();
	}

	private void discoveryPrinters() {
		showProgressDialog(Str.DiscoveryingPrinters[Global.LANG]);
		new Thread(new Runnable() {
			public void run() {
				Looper.prepare();
				try {
					final CountDownLatch latch = new CountDownLatch(1);
					final boolean[] result = {false};
					BluetoothDiscoverer.findPrinters(MainActivity.this, new DiscoveryHandler() {
						List<String> deviceList = new ArrayList<>();
						@Override
						public void foundPrinter(DiscoveredPrinter printer) {
							String strFriendlyName = "";
							if (printer instanceof DiscoveredPrinterBluetooth) {
								strFriendlyName = ((DiscoveredPrinterBluetooth) printer).friendlyName;
							}
							String strDevice = printer.address + "\n";
							if (strFriendlyName != null) {
								strDevice += strFriendlyName;
							} else {
								strDevice += "Unknown";
							}
							deviceList.add(strDevice);
						}

						@Override
						public void discoveryFinished() {
							hideProgressDialog();
							if (deviceList.size() > 0) {
								showDeviceList(deviceList);
								result[0] = true;
							}
							else
							{
								result[0] = false;
							}
							latch.countDown();
						}

						@Override
						public void discoveryError(String message) {
							Utils.printDebug(MainActivity.this, "Discovery error - " + message);
							result[0] = false;
							latch.countDown();
						}
					});

					//latch.await(Global.DISCOVER_BLUETOOTH_TIMEOUT, TimeUnit.SECONDS);
					latch.await();
					if(result[0] == false)
					{
						hideProgressDialog();
						mHander.sendEmptyMessage(0);

					}

				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					Looper.myLooper().quit();
				}
			}
		}).start();
	}

	public Handler mHander = new Handler(){
		public void handleMessage(Message msg){

			try {
				final MessageDialog msgDialog = new MessageDialog(MainActivity.this);
				msgDialog.setCancelable(false);
				msgDialog.mStrMessage = Str.TurnOnZebraPrinter[Global.LANG];
				msgDialog.mStrLeft = Str.Cancel[Global.LANG];
				msgDialog.mStrRight = Str.Retry[Global.LANG];
				msgDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
							if(Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 1)
							{
								discoveryPrinters();
							}
							else if(Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 2)
							{
								discoveryHoneywellPrinters();
							}
							else if(Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 3)
							{
								discoveryHoneywellLinePrinters();
							}
							else if(Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 4)
							{
								discoveryBixolonPrinters();
							}
						} else if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
						}
					}
				});
				msgDialog.show();
			}catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	};
	private void showDeviceList(List<String> deviceList) {
		if(deviceList.size() == 0)
		{
			if(!isFinishing() && !isDestroyed())
			{
				try {
					Toast.makeText(this, Str.NoDeviceDiscover[Global.LANG], Toast.LENGTH_LONG).show();
				}catch (Exception e1)
				{
					e1.printStackTrace();
				}
			}

			return;
		}
		if(deviceList.size() == 0)
		{
			if(!isFinishing() && !isDestroyed())
			{
				try {
					Toast.makeText(this, Str.NoDeviceDiscover[Global.LANG], Toast.LENGTH_LONG).show();
				}catch (Exception e1)
				{
					e1.printStackTrace();
				}
			}

			return;
		}
		final String strDevices[] = new String[deviceList.size()];
		for (int i=0; i<strDevices.length; i++) {
			strDevices[i] = deviceList.get(i);
		}

		String strArray[] = strDevices[0].split("\n");
		if (strArray.length > 0) {
			mStrMacAddress = strArray[0];
		}
		if (strArray.length > 1) {
			mStrFriendlyName = strArray[1];
		}

		try {
			new AlertDialog.Builder(this)
					.setTitle(Str.SelectPrinter[Global.LANG])
					.setSingleChoiceItems(new AlertSingleChoiceAdapter(MainActivity.this, strDevices), 0, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialogInterface, int i) {
							String strArray[] = strDevices[i].split("\n");
							if (strArray.length > 0) {
								mStrMacAddress = strArray[0];
							}
							if (strArray.length > 1) {
								mStrFriendlyName = strArray[0];
							}
						}
					})
					.setPositiveButton(Str.Choose[Global.LANG], (dialogInterface, which) -> {
						if(Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 1)
						{
							new PrintPdfTask().execute();
						}
						else if(Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 2)
						{
							doHoneywellPrintPdfTask();
						}
						else if(Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 3)
						{
							doHoneywellLinePrintPdfTask();
						}
						else if(Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 4)
						{
							doBixolonPrintPdfTask();
						}
					})
					.setNegativeButton(Str.Research[Global.LANG], (dialogInterface, i) -> {
						if(Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 1)
						{
							discoveryPrinters();
						}
						else if(Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 2)
						{
							discoveryHoneywellPrinters();
						}
						else if(Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 3)
						{
							discoveryHoneywellLinePrinters();
						}
						else if(Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 4)
						{
							discoveryBixolonPrinters();
						}
					})
					.setCancelable(false)
					.show();
		}catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	private void doHoneywellPrintPdfTask()
	{
		showProgressDialog(Str.Printing[Global.LANG]);
		HoneywellPrinter.doPrint(mContext, new HoneywellPrinter.PrintCallback() {
			@Override
			public void printFinished() {
				hideProgressDialog();
				try {
					Utils.setStringSetting(mContext, Global.PREF_HONEYWELL_PRINTER_MAC_ADDRESS, mStrMacAddress);
					Utils.printDebug(mContext, Str.SuccessToPrint[Global.LANG]);
				}catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			@Override
			public void printError(String error) {
				hideProgressDialog();
				try {
					final MessageDialog msgDialog = new MessageDialog(mContext);
					msgDialog.setCancelable(false);
					msgDialog.mStrTitle = "Printer Error";
					msgDialog.mStrMessage = error;
					msgDialog.mStrLeft = Str.No[Global.LANG];
					msgDialog.mStrRight = Str.Yes[Global.LANG];
					msgDialog.setOnDismissListener(dialog -> {
						if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
							doHoneywellPrintPdfTask();
						} else if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {

						}
					});
					msgDialog.show();
				}catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}, mStrMacAddress, mStrPDFPath);
	}

	private void doHoneywellLinePrintPdfTask()
	{
		showProgressDialog(Str.Printing[Global.LANG]);
		HoneywellLinePrinter.doPrint(mContext, new HoneywellLinePrinter.PrintCallback() {
			@Override
			public void printFinished() {
				hideProgressDialog();
				try {
					Utils.setStringSetting(mContext, Global.PREF_HONEYWELL_LINE_PRINTER_MAC_ADDRESS, mStrMacAddress);
					Utils.printDebug(mContext, Str.SuccessToPrint[Global.LANG]);
				}catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			@Override
			public void printError(String error) {
				hideProgressDialog();
				try {
					final MessageDialog msgDialog = new MessageDialog(mContext);
					msgDialog.setCancelable(false);
					msgDialog.mStrTitle = "Printer Error";
					msgDialog.mStrMessage = error;
					msgDialog.mStrLeft = Str.No[Global.LANG];
					msgDialog.mStrRight = Str.Yes[Global.LANG];
					msgDialog.setOnDismissListener(dialog -> {
						if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
							doHoneywellPrintPdfTask();
						} else if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {

						}
					});
					msgDialog.show();
				}catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}, mStrMacAddress, mStrPDFPath);
	}

	private void doBixolonPrintPdfTask()
	{
		showProgressDialog(Str.Printing[Global.LANG]);
		BixolonPrinter.doPrint(mContext, new BixolonPrinter.PrintCallback() {
			@Override
			public void printFinished() {
				hideProgressDialog();
				try {
					Utils.setStringSetting(mContext, Global.PREF_BIXOLON_PRINTER_MAC_ADDRESS, mStrMacAddress);
					Utils.setStringSetting(mContext, Global.PREF_BIXOLON_PRINTER_FRIENDLY_NAME, mStrFriendlyName);
					Utils.printDebug(mContext, Str.SuccessToPrint[Global.LANG]);
				}catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			@Override
			public void printError(String error) {
				hideProgressDialog();
				try {
					final MessageDialog msgDialog = new MessageDialog(mContext);
					msgDialog.setCancelable(false);
					msgDialog.mStrTitle = "Printer Error";
					msgDialog.mStrMessage = error;
					msgDialog.mStrLeft = Str.No[Global.LANG];
					msgDialog.mStrRight = Str.Yes[Global.LANG];
					msgDialog.setOnDismissListener(dialog -> {
						if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
							doHoneywellPrintPdfTask();
						} else if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {

						}
					});
					msgDialog.show();
				}catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}, mStrMacAddress, mStrFriendlyName, mStrPDFPath);
	}

	private class PrintPdfTask extends AsyncTask<Void, String, String> {

		int nMessageType = 0;
		BluetoothConnection printerConnection;
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			try {
				showProgressDialog(Str.Printing[Global.LANG]);
			}catch (Exception e)
			{
				e.printStackTrace();
			}

		}

		@Override
		protected String doInBackground(Void... params) {
			String strResult = "Unknown Error";
			printerConnection = new BluetoothConnection(mStrMacAddress);
			try {
				printerConnection.open();
				ZebraPrinter printer = null;
				if (printerConnection.isConnected()) {
					printer = ZebraPrinterFactory.getInstance(printerConnection);


					if (printer != null) {
						PrinterStatus printerStatus = printer.getCurrentStatus();
						if(printerStatus.isReadyToPrint)
						{
							printer.sendFileContents(mStrPDFPath);
							strResult = "";
						}
						else if(printerStatus.isPaused)
						{
							strResult = Str.PrinterPaused[Global.LANG];
							nMessageType = 1;
						}
						else if(printerStatus.isHeadOpen)
						{
							strResult = Str.PrinterHeadOpen[Global.LANG];
							nMessageType = 1;
						}
						else if(printerStatus.isPaperOut)
						{
							strResult = Str.PrinterPaperOut[Global.LANG];
							nMessageType = 1;
						}
						else if(printerStatus.isRibbonOut)
						{
							strResult = Str.PrinterRibbonOut[Global.LANG];
							nMessageType = 1;
						}
						else if(printerStatus.isHeadCold)
						{
							strResult = Str.PrinterHeadCold[Global.LANG];
							nMessageType = 1;
						}
						else if(printerStatus.isHeadTooHot)
						{
							strResult = Str.PrinterHeadTooHot[Global.LANG];
							nMessageType = 1;
						}
						else if(printerStatus.isReceiveBufferFull)
						{
							strResult = Str.PrinterReceiverBufferFull[Global.LANG];
							nMessageType = 1;
						}
						else
						{
							strResult = Str.PrinterError[Global.LANG];
							nMessageType = 2;
						}

						printerConnection.close();
					}
				}
			} catch (ConnectionException e) {
				Utils.addPrintExceptionStackToFileIfNeeded(mContext, Global.ZEBRA_PRINT_ERROR_FILENAME, e);
				e.printStackTrace();
				strResult = e.getMessage();
				nMessageType = 2;
			} catch (ZebraPrinterLanguageUnknownException e) {
				Utils.addPrintExceptionStackToFileIfNeeded(mContext, Global.ZEBRA_PRINT_ERROR_FILENAME, e);
				e.printStackTrace();
				strResult = e.getMessage();
				nMessageType = 2;
			} catch(Exception e){
				Utils.addPrintExceptionStackToFileIfNeeded(mContext, Global.ZEBRA_PRINT_ERROR_FILENAME, e);
				e.printStackTrace();
				strResult = e.getMessage();
				nMessageType = 2;
			}
			return strResult;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			hideProgressDialog();
			try
			{
				if (result.length() == 0) {
					Utils.setStringSetting(MainActivity.this, Global.PREF_PRINTER_MAC_ADDRESS, mStrMacAddress);
					Utils.printDebug(MainActivity.this, Str.SuccessToPrint[Global.LANG]);
				} else {
					try {
						final MessageDialog msgDialog = new MessageDialog(mContext);
						msgDialog.setCancelable(false);
						if(nMessageType == 1)
						{
							msgDialog.mStrMessage = result;
							msgDialog.mStrLeft = Str.Cancel[Global.LANG];
							msgDialog.mStrRight = Str.Retry[Global.LANG];
						}
						else
						{
							msgDialog.mStrMessage = Str.Cannot_connect_printer[Global.LANG];
							msgDialog.mStrLeft = Str.No[Global.LANG];
							msgDialog.mStrRight = Str.Yes[Global.LANG];
						}
						msgDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
							@Override
							public void onDismiss(DialogInterface dialog) {
								if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
									if(nMessageType == 2)
									{
										discoveryPrinters();
									}
									else
									{
										new PrintPdfTask().execute();
									}
								} else if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {

								}
							}
						});
						msgDialog.show();
					}catch (Exception e)
					{
						e.printStackTrace();
					}

				}
			}catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	private String mCurrentPhotoPath = "";
	public File createImageFile() throws IOException {
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

	private void updatePrinterTypeDetails() {
		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		List<DescTypeInfo> descTypeList = dbAdapter.getDescTypeList(Global.PRINTER_TYPE_DESC_TYPE_ID);
		dbAdapter.close();
		Collections.sort(descTypeList, new Comparator<DescTypeInfo>() {
			@Override
			public int compare(DescTypeInfo o1, DescTypeInfo o2) {
				return o1.nNumericKey - o2.nNumericKey;
			}
		});

		int nPrinterType = Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE);
		String strPrinterSize = Utils.getStringSetting(mContext, Global.PREF_PRINTER_SIZE);
		if (strPrinterSize.length() == 0) {
			for (int i = 0; i < descTypeList.size(); i++) {
				DescTypeInfo descTypeInfo = descTypeList.get(i);
				if (descTypeInfo.strValue1.equals(String.valueOf(nPrinterType))) {
					Utils.setStringSetting(mContext, Global.PREF_PRINTER_SIZE, descTypeInfo.strValue2);
					Utils.setStringSetting(mContext, Global.PREF_PRINTER_NAME, descTypeInfo.strDescription);
					break;
				}
			}
		}
	}

	private static class GattServiceConn implements ServiceConnection {
		GattDeviceAPI binding;

		@Override
		public void onServiceDisconnected(ComponentName name) {
			binding = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			if (service instanceof GattDeviceAPI) {
				binding = (GattDeviceAPI) service;
			}
		}
	}
}
