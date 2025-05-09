package dsd.dsdvansales.fragment;

import android.app.*;
import android.bluetooth.BluetoothAdapter;
import android.content.*;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.*;
import android.os.*;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.google.android.datatransport.runtime.dagger.multibindings.ElementsIntoSet;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.printer.PrinterStatus;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;
import com.zebra.sdk.printer.ZebraPrinterLanguageUnknownException;
import com.zebra.sdk.printer.discovery.BluetoothDiscoverer;
import com.zebra.sdk.printer.discovery.DiscoveredPrinter;
import com.zebra.sdk.printer.discovery.DiscoveredPrinterBluetooth;
import com.zebra.sdk.printer.discovery.DiscoveryHandler;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.net.ftp.FTPClient;

import dsd.dsdvansales.R;
import dsd.dsdvansales.activities.LoginActivity;
import dsd.dsdvansales.api.ApiClient;
import dsd.dsdvansales.chat.ChatHelper;
import dsd.dsdvansales.chat.QbDialogHolder;
import dsd.dsdvansales.db.DbAdapter;
import dsd.dsdvansales.db.PrepareUploadDbAdapter;
import dsd.dsdvansales.db.UploadDbAdapter;
import dsd.dsdvansales.dialogs.MessageDialog;
import dsd.dsdvansales.global.BixolonPrinter;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.HoneywellLinePrinter;
import dsd.dsdvansales.global.HoneywellPrinter;
import dsd.dsdvansales.global.PrintEngine;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.UploadViaHTTP;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.model.CustomerInfo;
import dsd.dsdvansales.model.DescTypeInfo;
import dsd.dsdvansales.model.DocTextInfo;
import dsd.dsdvansales.model.LoginResult;
import dsd.dsdvansales.model.MakeZipFileInfo;
import dsd.dsdvansales.model.PresoldOrHInfo;
import dsd.dsdvansales.model.ProdDetlInfo;
import dsd.dsdvansales.model.Product;
import dsd.dsdvansales.model.ProductList;
import dsd.dsdvansales.model.SurveyInfo;
import dsd.dsdvansales.model.TransactionInfo;
import dsd.dsdvansales.model.TripUser;
import dsd.dsdvansales.model.UCollectionsDInfo;
import dsd.dsdvansales.model.UInvenDInfo;
import dsd.dsdvansales.model.UInvenHInfo;
import dsd.dsdvansales.model.UServicesInfo;
import dsd.dsdvansales.model.UTransactionsInfo;
import dsd.dsdvansales.model.UploadFileInfo;
import dsd.dsdvansales.model.UploadInfo;
import dsd.dsdvansales.views.AlertSingleChoiceAdapter;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import retrofit2.Call;

public class TripSettlementFragment extends BaseFragment implements View.OnClickListener {

	private String mStrTransactionPdfFileName = "";
	private Connection printerConnection = null;
	private String mStrMacAddress = "";
	private String mStrFriendlyName = "";
	private class CheckMenuInfo {
		public int nIdx = 0;
		public String strName = "";
		public boolean bEnabled = false;
		public boolean bChecked = false;
	}

	private class IncompleteInfo {
		public CustomerInfo customerInfo = new CustomerInfo();
		public String strReason = "";
	}

	private static final int	UNDELIVERED_REASON		= 0;
	private static final int	OFF_LOAD_INVENTORY		= 1;

	private static final int	LOAD_REQUEST		= 2;
	private static final int	BALANCE_COLLECTIONS		= 3;
	private static final int	SALES_PERFORMANCE		= 4;
	private static final int	POST_TRIP_INSPECTION	= 5;
	private static final int	TRANSACTION_HISTORY_PRINT = 6;


	private String mStrPdfFileName = "";
	private long mPdfTrxnNo = 0;

	private TextView mTxtNoData = null;
	private ListView mListView = null;
	private ListAdapter mListAdapter = null;
	private List<CheckMenuInfo> mCheckList = new ArrayList<>();

	private String error = "";

	private Font fontDarkGray7Normal;
	private Font fontBlack7Bold;
	private Font fontBlack10Bold;
	private List<IncompleteInfo> mIncompletedList = new ArrayList<>();

	private boolean mLoaded = false;
	List<DescTypeInfo> reasonList = new ArrayList<>();
	int nCnt = 0;
	List<UCollectionsDInfo> uCollectionDList = new ArrayList<>();
	SurveyInfo surveyInfo;

	private PrintEngine mHoneywellPrintEngine = null;
	private String mHoneywellTemplatePath = "";
	private void loadFont()
	{
		fontDarkGray7Normal = Utils.getFont(Utils.getPrintFontFilePath(), Utils.getPrintEncoding(), Utils.getPrintEmbeded(), 7, Font.NORMAL, BaseColor.DARK_GRAY);
		fontBlack7Bold = Utils.getFont(Utils.getPrintFontFilePath(), Utils.getPrintEncoding(), Utils.getPrintEmbeded(), 7, Font.BOLD, BaseColor.BLACK);
		fontBlack10Bold = Utils.getFont(Utils.getPrintFontFilePath(), Utils.getPrintEncoding(), Utils.getPrintEmbeded(), 10, Font.BOLD, BaseColor.BLACK);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootView = inflater.inflate(R.layout.fragment_trip_settlement, container, false);
		loadFont();
		commonInit();
		return mRootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		Global.gFragIdx = Global.FRAG_TRIP_SETTLEMENT;
	}

	@Override
	public void initView() {
		mRootView.findViewById(R.id.btnCompleted).setOnClickListener(this);

		mTxtNoData = (TextView) mRootView.findViewById(R.id.txtNoData);
		mListView = (ListView) mRootView.findViewById(R.id.listView);
		mListAdapter = new ListAdapter();
		mListView.setAdapter(mListAdapter);

		((TextView) mRootView.findViewById(R.id.txtWelcome)).setText("Hi " + Utils.getStringSetting(mContext, Global.PREF_USER_DESCRIPTION));
		refreshLanguage();

		Global.g_bVisitTripSettlement = true;
	}

	@Override
	public void refreshDriverName() {
		((TextView) mRootView.findViewById(R.id.txtWelcome)).setText("Hi " + Utils.getStringSetting(mContext, Global.PREF_USER_DESCRIPTION));
	}

	private List<CustomerInfo> filterSameCustomer(List<CustomerInfo> customerInfoList)
	{
		Map<String, CustomerInfo> customerInfoMap = new HashMap<>();
		List<CustomerInfo> newList = new ArrayList<>();
		for(CustomerInfo customerInfo : customerInfoList)
		{
			String key = customerInfo.nChainNo + "___" + customerInfo.nCustNo;
			CustomerInfo info = customerInfoMap.get(key);
			if( info == null || info.bCompleted == false)
			{
				customerInfoMap.put(key, customerInfo);
				newList.add(customerInfo);
			}
		}
		return newList;
	}

	@Override
	public void loadData() {

		if( !mLoaded )
		{
			DbAdapter dbAdapter = new DbAdapter(mContext);
			dbAdapter.open();
			reasonList = dbAdapter.getDescTypeList("NOSERVRSN");
			List<CustomerInfo> customerList = filterSameCustomer(dbAdapter.getWhiteCustomerInfo(false, Utils.getIntSetting(mContext, Global.DAY_NO)));
			customerList.addAll(dbAdapter.getCustomerList(true, false, Utils.getIntSetting(mContext, Global.DAY_NO)));
			uCollectionDList = dbAdapter.getUCollectionsDList();
			surveyInfo = dbAdapter.getSurveyInfo(98, true);

			nCnt = 0;
			for (CustomerInfo customerInfo : customerList) {
				if (customerInfo.bCompleted == false) {
					nCnt++;
				}
			}

			if(nCnt == 0 && reasonList.size() > 0)
			{
				for(CustomerInfo customerInfo : customerList)
				{
					if(customerInfo.bCompleted == true)
					{
						UServicesInfo uServicesInfo = dbAdapter.getUServiceInfo(customerInfo.nCustNo, customerInfo.nChainNo);

						if(uServicesInfo != null)
						{
							IncompleteInfo incompleteInfo = new IncompleteInfo();
							incompleteInfo.customerInfo = customerInfo;
							incompleteInfo.strReason = "";
							for(int i = 0; i < reasonList.size(); i++)
							{
								if(reasonList.get(i).strAlphaKey.equals(uServicesInfo.strReason))
								{
									incompleteInfo.strReason = reasonList.get(i).strDescription;
									break;
								}
							}
							if(!TextUtils.isEmpty(incompleteInfo.strReason))
							{
								mIncompletedList.add(incompleteInfo);
							}
						}
					}
				}
			}

			dbAdapter.close();
			mLoaded = true;
		}

		
		mCheckList.clear();
		CheckMenuInfo info;

		info = new CheckMenuInfo();
		info.nIdx = UNDELIVERED_REASON;
		info.strName = Str.UndeliveredReasons[Global.LANG];

		info.bEnabled = reasonList.size() > 0 && nCnt > 0;
		info.bChecked = Utils.getBooleanSetting(mContext, String.format(Locale.ENGLISH, Global.PREF_END_TRIP_CHECKED, UNDELIVERED_REASON));
		mCheckList.add(info);


		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		List<DescTypeInfo> mSpoilsReasonDescList = dbAdapter.getDescTypeList("SPOILSRSN");
		UInvenHInfo	mUInvenHInfo = dbAdapter.getUInvenHInfo(Utils.getLongSetting(mContext, Global.PREF_UINVEN_TRXN_NO));
		if (mUInvenHInfo == null) {
			mUInvenHInfo = new UInvenHInfo();
			mUInvenHInfo.nTrxnNo = Utils.getLongSetting(mContext, Global.PREF_UINVEN_TRXN_NO);
		}
		mUInvenHInfo.uInvenDList = dbAdapter.getUInvenDList(mUInvenHInfo.nTrxnNo);
		if( mUInvenHInfo.uInvenDList == null ) mUInvenHInfo.uInvenDList = new ArrayList<>();
		dbAdapter.close();

		boolean bExistReturns = false;
		boolean bExistSpoils = mSpoilsReasonDescList.size() > 0;
		boolean bExistContainer = Utils.getIntSetting(mContext, Global.PREF_LOAD_CONTAINERS) != 0;
		boolean bExistFresh = !Utils.getStringSetting(mContext, Global.PREF_DISABLE_OFFLOADS).equals("1");
		for(UInvenDInfo uInvenDInfo : mUInvenHInfo.uInvenDList)
		{
			if(uInvenDInfo.nDumps > 0 || uInvenDInfo.dDumps > 0)
			{
				bExistReturns = true;
				break;
			}
		}


		info = new CheckMenuInfo();
		info.nIdx = OFF_LOAD_INVENTORY;
		info.strName = Str.EndingInventory[Global.LANG];
		info.bEnabled = bExistReturns || bExistSpoils || bExistContainer || bExistFresh;
		info.bChecked = Utils.getBooleanSetting(mContext, String.format(Locale.ENGLISH, Global.PREF_END_TRIP_CHECKED, OFF_LOAD_INVENTORY));
		mCheckList.add(info);

		info = new CheckMenuInfo();
		info.nIdx = LOAD_REQUEST;
		info.strName = Str.LoadRequest[Global.LANG];
		info.bEnabled = true;

		String strLoadRequestEOT = Utils.getStringSetting(mContext, Global.PREF_LOAD_REQUEST_EOT);
		if (strLoadRequestEOT.equals("1") || strLoadRequestEOT.equals("2") || strLoadRequestEOT.equals("3")) {
			info.bChecked = Utils.getBooleanSetting(mContext, String.format(Locale.ENGLISH, Global.PREF_END_TRIP_CHECKED, LOAD_REQUEST));
		}
		else {
			info.bChecked = false;
		}
		mCheckList.add(info);

		info = new CheckMenuInfo();
		info.nIdx = BALANCE_COLLECTIONS;
		info.strName = Str.BalanceCollections[Global.LANG];
		info.bEnabled = uCollectionDList.size() > 0;
		info.bChecked = Utils.getBooleanSetting(mContext, String.format(Locale.ENGLISH, Global.PREF_END_TRIP_CHECKED, BALANCE_COLLECTIONS));
		mCheckList.add(info);

		info = new CheckMenuInfo();
		info.nIdx = SALES_PERFORMANCE;
		info.strName = Str.SalesPerformance[Global.LANG];
		info.bEnabled = Utils.getIntSetting(mContext, Global.PREF_SHOWSALESPERF) == 1 && Utils.isAllCustomerHasReasonCode(mContext);
		info.bChecked = Utils.getBooleanSetting(mContext, String.format(Locale.ENGLISH, Global.PREF_END_TRIP_CHECKED, SALES_PERFORMANCE));
		mCheckList.add(info);

		info = new CheckMenuInfo();
		info.nIdx = POST_TRIP_INSPECTION;
		info.strName = Str.PostTripInspection[Global.LANG];
		info.bEnabled = surveyInfo.nSurveyType == 98;
		info.bChecked = Utils.getBooleanSetting(mContext, String.format(Locale.ENGLISH, Global.PREF_END_TRIP_CHECKED, POST_TRIP_INSPECTION));
		mCheckList.add(info);

		String strTemplateFileName = Utils.getFinalPrintTemplateFileName(mContext, Global.PRINT_TRANSACTION_HISTORY_REPORT_FILENAME);
		if(Utils.isFileExist(Global.ROOT_PRINT_TEMPLATE_LOCCATION + strTemplateFileName))
		{
			info = new CheckMenuInfo();
			info.nIdx = TRANSACTION_HISTORY_PRINT;
			info.strName = Str.Print_Route_Activity[Global.LANG];
			info.bEnabled = true;
			info.bChecked = false;
			mCheckList.add(info);
		}



		boolean bEnabled = true;
		for (CheckMenuInfo info1 : mCheckList) {
			if (info1.bEnabled == true && info1.bChecked == false && info1.nIdx != TRANSACTION_HISTORY_PRINT && info1.nIdx != LOAD_REQUEST) {
				bEnabled = false;
				break;
			}
		}
		mRootView.findViewById(R.id.btnCompleted).setEnabled(bEnabled);
		refreshPage();

		if(mIncompletedList.size() > 0 && Utils.getBooleanSetting(mContext, Global.PREF_CREATED_UNSERVICED_REASON_REPORT) == false)
		{
			new Thread()
			{
				@Override
				public void run() {
					createPDF();
					super.run();
				}
			}.start();
		}
	}

	@Override
	public void refreshPage() {
		if (mCheckList.size() > 0) {
			mListView.setVisibility(View.VISIBLE);
			mTxtNoData.setVisibility(View.GONE);
			mListAdapter.notifyDataSetChanged();
		} else {
			mListView.setVisibility(View.GONE);
			mTxtNoData.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void refreshLanguage() {
		((TextView) mRootView.findViewById(R.id.lblTitle)).setText(Str.TripSettlement[Global.LANG].toUpperCase());
		((TextView) mRootView.findViewById(R.id.txtTrip)).setText(Str.Trip[Global.LANG] + " #" + Global.gTripNumber);
		((Button) mRootView.findViewById(R.id.btnCompleted)).setText(Str.Completed[Global.LANG]);
		mTxtNoData.setText(Str.No_data_has_been_found[Global.LANG]);
		loadData();
	}

	@Override
	public void receiveBroadcast(String strAction, Intent intent) {
	}

	private void gotoCheck(int nIdx) {
		switch (nIdx) {
			case UNDELIVERED_REASON:
				mContext.replaceFragment(new IncompleteDeliveriesFragment());
				break;
			case OFF_LOAD_INVENTORY:
				Global.gCurEndingInventory = null;
				Global.gCurEndingInventoryList = null;
				mContext.replaceFragment(new EndingInventoryFragment());
				break;
			case LOAD_REQUEST: {
					LoadRequestFragment fragment = new LoadRequestFragment();
					Bundle args = new Bundle();
					args.putBoolean(Global.EXTRA_FROM, false);
					fragment.setArguments(args);
					mContext.replaceFragment(fragment);
				}
				break;
			case BALANCE_COLLECTIONS:
				mContext.replaceFragment(new CollectionsBalancingFragment());
				break;
			case SALES_PERFORMANCE:
				mContext.replaceFragment(new SalesPerformanceFragment());
				break;
			case POST_TRIP_INSPECTION:
				Bundle bundle = new Bundle();
				bundle.putBoolean(Global.EXTRA_IS_FROM_END_TRIP, true);
				PreTripCheckFragment fragment = new PreTripCheckFragment();
				fragment.setArguments(bundle);
				mContext.replaceFragment(fragment);
				break;
			case TRANSACTION_HISTORY_PRINT:
				doPrintTransactionHistory();
				break;
		}
	}

	private void doPrintTransactionHistory()
	{

		String strTemplateFileName = Utils.getFinalPrintTemplateFileName(mContext, Global.PRINT_TRANSACTION_HISTORY_REPORT_FILENAME);
		if(Utils.isFileExist(Global.ROOT_PRINT_TEMPLATE_LOCCATION + "/" + strTemplateFileName))
		{
			new Thread()
			{
				@Override
				public void run() {
					new Handler(Looper.getMainLooper()).post(new Runnable() {
						@Override
						public void run() {
							showProgressDialog(Str.CreatingPdf[Global.LANG]);
						}
					});

					PrintEngine printEngine = new PrintEngine();
					mHoneywellPrintEngine = printEngine;

					Utils.createDirectory(Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/");
					mStrTransactionPdfFileName = getPDFFileName();

					DbAdapter dbAdapter = new DbAdapter(mContext);
					dbAdapter.open();
					List<TransactionInfo> transactionInfoList = dbAdapter.getAllTransactionHistory();
					dbAdapter.close();

					printEngine.PrepareTransactionHistoryPrint(mContext, transactionInfoList);
					String strHoneywellFileName = Utils.getFinalPrintTemplateFileNameForHoneywell(mContext, Global.PRINT_TRANSACTION_HISTORY_REPORT_FILENAME);
					mHoneywellTemplatePath = Global.ROOT_PRINT_TEMPLATE_LOCCATION + strHoneywellFileName;
					printEngine.createDocument(mContext, false, Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + mStrTransactionPdfFileName, Global.TRANSACTION_HISTORY_PRINT);

					new Handler(Looper.getMainLooper()).post(new Runnable() {
						@Override
						public void run() {
							hideProgressDialog();
							outputPrinter();
						}
					});

				}
			}.start();

		}
		else
		{
			if(!mContext.isFinishing() && !mContext.isDestroyed())
			{
				try {
					Toast.makeText(mContext, Str.NoExistTemplateFile[Global.LANG], Toast.LENGTH_LONG).show();
				}catch (Exception e1)
				{
					e1.printStackTrace();
				}
			}

		}
	}

	private void showMessageDialog(String strMessage) {
		try {
			final MessageDialog msgDialog = new MessageDialog(mContext);
			msgDialog.setCancelable(false);
			msgDialog.mStrTitle = Str.UPLOADING_FAILED[Global.LANG];
			msgDialog.mStrMessage = strMessage;
			msgDialog.mStrLeft = Str.No[Global.LANG];
			msgDialog.mStrRight = Str.Yes[Global.LANG];
			msgDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
					if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
						doComplete();
					}
				}
			});
			msgDialog.show();
		}catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	private void showErrorDialog(String strMessage) {
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
						uploadData();
					}
				}
			});
			msgDialog.show();
		}catch (Exception e)
		{
			e.printStackTrace();
		}
	}


	private void doComplete() {
		if (Utils.isConnectingToInternet(mContext) == false) {
			showErrorDialog("Unable to access network.  Please enable radios and try again");
		} else {
//			new UploadTask().execute();
			uploadData();
		}
	}

	public boolean m_bClicked = false;
	@Override
	public void onClick(View v) {
		if( m_bClicked ) return;
		m_bClicked = true;
		switch (v.getId()) {
			case R.id.btnCompleted:
				if(Utils.getStringSetting(mContext, Global.PREF_LOAD_REQUEST_EOT).equals("2") && !Utils.getBooleanSetting(mContext, String.format(Locale.ENGLISH, Global.PREF_END_TRIP_CHECKED, LOAD_REQUEST)))
				{
					try {
//						final MessageDialog msgDialog = new MessageDialog(mContext);
//						msgDialog.setCancelable(false);
//						msgDialog.mStrMessage = Str.NeedToCompleteLoadRequest[Global.LANG];
//						msgDialog.mStrMiddle = Str.Ok[Global.LANG];
//						msgDialog.show();
						final MessageDialog msgDialog = new MessageDialog(mContext);
						msgDialog.setCancelable(false);
						msgDialog.mStrMessage = Str.CompleteLoadRequestWarning[Global.LANG];
						msgDialog.mStrLeft = Str.Return[Global.LANG];
						msgDialog.mStrRight = Str.Proceed[Global.LANG];
						msgDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
							@Override
							public void onDismiss(DialogInterface dialog) {
								if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT){
								}
								else{
									doComplete();
								}
							}
						});
						msgDialog.show();
					}catch (Exception e)
					{
						e.printStackTrace();
					}
				}
				else {
					doComplete();
				}
				break;
		}
		m_bClicked = false;
	}


	private void doLastProcess()
	{
		Utils.setBooleanSetting(mContext, Global.FLAG_MAKE_END_TRIP_DATA, false);
		Utils.leaveLog(mContext, "TripSettle_doLastProcess", null, "Started");

		//Clear all product locon information saved for product tree view.
		Global.prodLocnInfoMap.clear();

		//Remove Old latest order list.
		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		dbAdapter.deleteLatestOrderList();
		if(Utils.getIntSetting(mContext, Global.PREF_SUPERVISOR) == 1)
		{
			//Check SuperVisor User is in DB, and add one

			List<TripUser> tripUserList = dbAdapter.getTripUserList();

			boolean bExist = false;
			for(TripUser tripUser : tripUserList)
			{
				if(tripUser.strDescription.equals("Supervisor") && tripUser.strUserName.equals("Supervisor"))
				{
					bExist = true;
					break;
				}
			}
			if(!bExist)
			{
				//Add SuperVisor User
				TripUser info = new TripUser();
				info.strUserName = "Supervisor";
				info.strRouteNumber = Global.gRouteNumber;
				info.strSecurity = Utils.getStringSetting(mContext, Global.PREF_SUPERVISOR_USER_PASSWORD);
				info.strDescription = "Supervisor";
				info.strTrip = Global.gTripNumber;

				dbAdapter.insertTripUser(info);
			}

		}
		dbAdapter.close();

		Global.gEnteredEndTrip = false;
		Utils.leaveLog(mContext, "TripSettle_doLastProcess", null, "gEnteredEndTrip = false");
		//Utils.deleteDirectory(Global.UPLOAD_DIR);
		//Utils.deleteDirectory(Global.PDF_DIR);
		Utils.deleteDirectory(Global.PDF_LOCAL_DIR);
		Utils.deleteDirectory(Global.QRCODE_LOCAL_DIR);
		Utils.fileDelete(Global.ROOT_ROUTECTL_PATH);

		Utils.leaveLog(mContext, "TripSettle_doLastProcess", null, "removed pdf_local, qrcode_local, routectl");
		//Utils.deleteDirectory(Global.PHOTO_DIR);
		Utils.resetLoginCount();
		Utils.registerLoginStatus(mContext, 2);
		new Handler(Looper.getMainLooper()).post(new Runnable() {
			@Override
			public void run() {
				Utils.printDebug(mContext, Str.UploadedSuccessfully[Global.LANG]);
				Utils.setBooleanSetting(mContext, Global.PREF_END_VISIT_PLAN, true);
				Utils.leaveLog(mContext, "TripSettle_EndVisit", null, "END_VISIT_PLAN was set true");
				Utils.setEndTripPlan(true, mContext);
				Utils.unregisterQbChatListeners();

				if(Utils.isInstalledInstallerApp(mContext) == null)
				{
					hideProgressDialog();

					//mContext.replaceFragment(new LoginFragment());
					mContext.startActivity(new Intent(mContext, LoginActivity.class));
					mContext.finish();
					Global.g_bVisitTripSettlement = false;
					Global.gSignedOn = false;
				}
				else
				{
					new Thread(){
						@Override
						public void run() {
							new Handler(Looper.getMainLooper()).post(new Runnable() {
								@Override
								public void run() {
									showProgressDialog(Str.checking_new_version[Global.LANG]);
								}
							});
							checkUsingNewHTTPServer();

						}
					}.start();
				}
			}
		});
	}

	private void checkUsingNewHTTPServer()
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
						checkUsingOldFTPServer();
					}
					else
					{
						ApiClient.API_ROOT = Global.API_OLD_SERVER_ROOT;
						checkUsingOldFTPServer();
					}

				}
				else
				{
					ApiClient.API_ROOT = Global.API_OLD_SERVER_ROOT;
					checkUsingOldFTPServer();
				}
			}

			@Override
			public void onFailure(Call<LoginResult> call, Throwable t) {
				new Handler(Looper.getMainLooper()).post(new Runnable() {
					@Override
					public void run() {
						hideProgressDialog();
						//mContext.replaceFragment(new LoginFragment());
						mContext.startActivity(new Intent(mContext, LoginActivity.class));
						mContext.finish();
						Global.g_bVisitTripSettlement = false;
						Global.gSignedOn = false;
					}
				});
			}
		});
    }

	private void checkUsingOldFTPServer()
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
							checkUsingOldFTPServer();
						}
						else {
							new Handler(Looper.getMainLooper()).post(new Runnable() {
								@Override
								public void run() {
									hideProgressDialog();
									//mContext.replaceFragment(new LoginFragment());
									mContext.startActivity(new Intent(mContext, LoginActivity.class));
									mContext.finish();
									Global.g_bVisitTripSettlement = false;
									Global.gSignedOn = false;
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
								new Handler(Looper.getMainLooper()).post(new Runnable() {
									@Override
									public void run() {
										hideProgressDialog();
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
														mContext.upgradeNewVersion("EndTrip");
													}
													else {
														Utils.reportUpgradeStatus(Utils.getStringSetting(mContext, Global.PREF_FTP_COMPANY_NAME), Utils.getStringSetting(mContext, Global.PREF_LOGIN_USER_NAME), Global.gTripNumber, finalCurrentProduct.VERSION_NUMBER, packageInfo.versionName, false);
														//mContext.replaceFragment(new LoginFragment());
														mContext.startActivity(new Intent(mContext, LoginActivity.class));
														mContext.finish();
														Global.g_bVisitTripSettlement = false;
														Global.gSignedOn = false;
													}
												}
											});
											msgDialog.show();
										}catch (Exception e)
										{
											e.printStackTrace();

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
										//mContext.replaceFragment(new LoginFragment());
										mContext.startActivity(new Intent(mContext, LoginActivity.class));
										mContext.finish();
										Global.g_bVisitTripSettlement = false;
										Global.gSignedOn = false;
									}
								});
							}
						} catch (PackageManager.NameNotFoundException e) {
							e.printStackTrace();
							new Handler(Looper.getMainLooper()).post(new Runnable() {
								@Override
								public void run() {
									hideProgressDialog();
									//mContext.replaceFragment(new LoginFragment());
									mContext.startActivity(new Intent(mContext, LoginActivity.class));
									mContext.finish();
									Global.g_bVisitTripSettlement = false;
									Global.gSignedOn = false;
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
							hideProgressDialog();
							//mContext.replaceFragment(new LoginFragment());
							mContext.startActivity(new Intent(mContext, LoginActivity.class));
							mContext.finish();
							Global.g_bVisitTripSettlement = false;
							Global.gSignedOn = false;
						}
					});
				}
			}

			@Override
			public void onFailure(Call<ProductList> call, Throwable t) {
				new Handler(Looper.getMainLooper()).post(new Runnable() {
					@Override
					public void run() {
						hideProgressDialog();
						//mContext.replaceFragment(new LoginFragment());
						mContext.startActivity(new Intent(mContext, LoginActivity.class));
						mContext.finish();
						Global.g_bVisitTripSettlement = false;
						Global.gSignedOn = false;
					}
				});
			}
		});
    }

	private void uploadData()
	{
		//
		Global.gEndTripProcessing = true;
		showProgressDialog(Str.UploadingData[Global.LANG]);
		new Thread()
		{
			@Override
			public void run() {

				//Process all prepare database data
				Utils.leaveLog(mContext, "TripSettle_uploadData", null, "Waiting for gUploadProcess");
				while(Global.gUploadProcess)
				{
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				Utils.leaveLog(mContext, "TripSettle_uploadData", null, "Collecting PrepareUploadDB data");
				PrepareUploadDbAdapter prepareUploadDbAdapter = new PrepareUploadDbAdapter(mContext);
				try {
					prepareUploadDbAdapter.open();
					Global.gUploadCustNoList.addAll(prepareUploadDbAdapter.getMakeZipFileInfoList());
					prepareUploadDbAdapter.close();
				}catch (Exception e)
				{
					Utils.addEndTripExceptionStackToFileIfNeeded(mContext, Global.UPLOAD_ERROR_FILENAME, e, "getMakeZipFileInfoList");
					e.printStackTrace();
				}


				Utils.leaveLog(mContext, "TripSettle_uploadData", null, "Making zip files for uploading");
				//make zip files for uploading data.
				List<UploadInfo> uploadInfoList = new ArrayList<>();
				uploadInfoList.addAll(Global.gUploadCustNoList);
				for(UploadInfo info : uploadInfoList)
				{
					try {
						Utils.uploadData(mContext, info.makeZipFileInfo.nCustNo, info, false);
					}catch (Exception e) {
						e.printStackTrace();
						Utils.unregisterUploadFile(mContext, info);
					}
				}
				uploadInfoList.clear();
				Global.gUploadCustNoList.clear();

				Utils.leaveLog(mContext, "TripSettle_uploadData", null, "Doing Utils.uploadData for 0");
				Global.gIsEndTrip = true;
				boolean bReturn;
				if(!Utils.getBooleanSetting(mContext, Global.FLAG_MAKE_END_TRIP_DATA))
				{
					bReturn = Utils.uploadData(mContext, 0, null, false);
					if(bReturn)
					{
						Utils.leaveLog(mContext, "TripSettle_uploadData", null, "Success in Utils.uploadData for 0");
						Utils.setBooleanSetting(mContext, Global.FLAG_MAKE_END_TRIP_DATA, true);
					}
				}
				else
				{
					bReturn = true;
				}

				if(!bReturn)
				{
					new Handler(Looper.getMainLooper()).post(new Runnable() {
						@Override
						public void run() {
							hideProgressDialog();
							showMessageDialog(Str.CreatingUploadDataError[Global.LANG]);
						}
					});
					return;
				}
				Global.gbStopBackgroundUpload =  true;
				Utils.leaveLog(mContext, "TripSettle_uploadData", null, "Waiting gbBackgroundUploading");
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

				Utils.leaveLog(mContext, "TripSettle_uploadData", null, "Uploading EUPL");
				{
					MakeZipFileInfo oldMakeZipFileInfo = makeZipFileInfo;
					makeZipFileInfo = null;
					long nTrxnNo = setGeneralUploadData("EUPL", 0, 0, -1);

					TransactionInfo transactionInfo = new TransactionInfo();
					transactionInfo.strType = "STKE";
					transactionInfo.strTime = new SimpleDateFormat("kk:mm").format(new Date(nTrxnNo));
					DbAdapter dbAdapter = new DbAdapter(mContext);
					dbAdapter.open();
					dbAdapter.insertTransactionHistoryInfo(transactionInfo);
					dbAdapter.close();

					UploadInfo uploadInfo = new UploadInfo();
					uploadInfo.makeZipFileInfo = makeZipFileInfo;
					Utils.uploadData(mContext, 0, uploadInfo, false);
					makeZipFileInfo = oldMakeZipFileInfo;
				}

				//Upload Data to Server
				Utils.leaveLog(mContext, "TripSettle_uploadData", null, "Started doUploadRemainingFiles");
				if( doUploadRemainingFiles() )
				{
					Utils.leaveLog(mContext, "TripSettle_uploadData", null, "Success in doUploadRemainingFiles");
					doLastProcess();
					Global.gEndTripProcessing = false;
					Global.gbStopBackgroundUpload = false;
				}
				else
				{
					Utils.leaveLog(mContext, "TripSettle_uploadData", null, "Failed in doUploadRemainingFiles");
					Global.gEndTripProcessing = false;
					Global.gbStopBackgroundUpload = false;
					new Handler(Looper.getMainLooper()).post(new Runnable() {
						@Override
						public void run() {
							hideProgressDialog();
							showErrorDialog(error);
						}
					});
				}
			}
		}.start();
	}

	private boolean doUploadRemainingFiles() {
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
				Utils.leaveLog(mContext, "TripSettle_doUpload", uploadFile, "Error File, File Not Exist");
			} else {
				long fileSize = Utils.getFileSize(uploadFile.localPath);
				if (fileSize == 0) {
					uploadDbAdapter.open();
					uploadDbAdapter.deleteUploadFile(uploadFile.idx);
					uploadDbAdapter.close();
					Utils.leaveLog(mContext, "TripSettle_doUpload", uploadFile, "File Size is 0");
				} else if (new UploadViaHTTP().uploadFileToFTPServerUsingHttp(strCompanyName, strFTPServerIP, strFTPUserName, uploadFile.localPath, uploadFile.serverPath)) {
					uploadDbAdapter.open();
					uploadDbAdapter.deleteUploadFile(uploadFile.idx);
					uploadDbAdapter.close();
					Utils.leaveLog(mContext, "TripSettle_doUpload", uploadFile, "Uploaded Success using Http");
				} else if (!Utils.getStringSetting(mContext, Global.PREF_DisableFTP).equals("1") && Utils.uploadFileToFTPServer(mContext, uploadFile.localPath, uploadFile.serverPath)) {
					uploadDbAdapter.open();
					uploadDbAdapter.deleteUploadFile(uploadFile.idx);
					uploadDbAdapter.close();
					Utils.leaveLog(mContext, "TripSettle_doUpload", uploadFile, "Uploaded Success using FTP");
				} else if (!Utils.isConnectingToInternet(mContext)) {
					Utils.leaveLog(mContext, "TripSettle_doUpload", uploadFile, "Unable to access network");
					error = "Unable to access network.  Please enable radios and try again\n\nFailed Server Folder Path : " + uploadFile.serverPath + "\n\n";
					Global.gbStopBackgroundUpload = false;
					return false;
				} else if (!Utils.checkDirectoryExists(mContext, uploadFile.serverPath)) {
					Utils.leaveLog(mContext, "TripSettle_doUpload", uploadFile, "Unable to access correct server folder");
					error = "Unable to access correct server folder.  Please contact your administrator\n\nServer Folder Path : " + uploadFile.serverPath + "\n\n";
					Global.gbStopBackgroundUpload = false;
					return false;
				} else {
					Utils.leaveLog(mContext, "TripSettle_doUpload", uploadFile, "Cannot upload file because of network or permission");
					error = "Cannot upload file because of network weak or permission of directory in ftp server\n\nServer Folder Path : " + uploadFile.serverPath + "\n\n";
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
					Utils.leaveLog("TripSettle_doUpload", fileInfo.get(i), "Error File, File Not Exist");
				} else {
					long fileSize = Utils.getFileSize(fileInfo.get(i).localPath);
					if (fileSize == 0) {
						uploadDbAdapter.open();
						uploadDbAdapter.deleteUploadFile(fileInfo.get(i).idx);
						uploadDbAdapter.close();
						Utils.leaveLog("TripSettle_doUpload", fileInfo.get(i), "File Size is 0");
					} else if (new UploadViaHTTP().uploadFileToFTPServerUsingHttp(strCompanyName, strFTPServerIP, strFTPUserName, fileInfo.get(i).localPath, fileInfo.get(i).serverPath)) {
						uploadDbAdapter.open();
						uploadDbAdapter.deleteUploadFile(fileInfo.get(i).idx);
						uploadDbAdapter.close();
						Utils.leaveLog("TripSettle_doUpload", fileInfo.get(i), "Uploaded Success using Http");
					} else if (!Utils.getStringSetting(mContext, Global.PREF_DisableFTP).equals("1") && Utils.uploadFileToFTPServer(mContext, fileInfo.get(i).localPath, fileInfo.get(i).serverPath)) {
						uploadDbAdapter.open();
						uploadDbAdapter.deleteUploadFile(fileInfo.get(i).idx);
						uploadDbAdapter.close();
						Utils.leaveLog("TripSettle_doUpload", fileInfo.get(i), "Uploaded Success using FTP");
					} else if (!Utils.isConnectingToInternet(mContext)) {
						Utils.leaveLog("TripSettle_doUpload", fileInfo.get(i), "Unable to access network");
						error = "Unable to access network.  Please enable radios and try again\n\nFailed Server Folder Path : " + fileInfo.get(i).serverPath + "\n\n";
						Global.gbStopBackgroundUpload = false;
						return false;
					} else if (!Utils.checkDirectoryExists(mContext, fileInfo.get(i).serverPath)) {
						Utils.leaveLog("TripSettle_doUpload", fileInfo.get(i), "Unable to access correct server folder");
						error = "Unable to access correct server folder.  Please contact your administrator\n\nServer Folder Path : " + fileInfo.get(i).serverPath + "\n\n";
						Global.gbStopBackgroundUpload = false;
						return false;
					} else {
						Utils.leaveLog("TripSettle_doUpload", fileInfo.get(i), "Cannot upload file because of network or permission");
						error = "Cannot upload file because of network weak or permission of directory in ftp server\n\nServer Folder Path : " + fileInfo.get(i).serverPath + "\n\n";
						Global.gbStopBackgroundUpload = false;
						return false;
					}
				}
			}
		}*/
		Global.gbStopBackgroundUpload = false;
		return true;
	}

	private class ListAdapter extends BaseAdapter {

		private class ViewHolder {
			public TextView txtName;
			public ImageView imgIcon;
		}

		public int getCount() {
			return mCheckList.size();
		}

		public CheckMenuInfo getItem(int position) {
			if(position < mCheckList.size())
			{
				return mCheckList.get(position);
			}
			else
			{
				return null;
			}
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(final int position, View convertView, ViewGroup parent) {
			View view = convertView;
			final ViewHolder holder;
			final CheckMenuInfo info = getItem(position);
			if(info == null) return null;

			if (convertView == null) {
				view = mContext.getLayoutInflater().inflate(R.layout.row_instore, parent, false);
				holder = new ViewHolder();
				holder.txtName = (TextView) view.findViewById(R.id.txtName);
				holder.imgIcon = (ImageView) view.findViewById(R.id.imgIcon);
				view.findViewById(R.id.txtPrice).setVisibility(View.GONE);
				view.findViewById(R.id.btnPresold).setVisibility(View.GONE);
				view.setTag(holder);
			} else {
				holder = (ViewHolder) view.getTag();
			}

			holder.imgIcon.setBackgroundResource(R.drawable.icon_check);
			holder.imgIcon.setVisibility(View.GONE);
			if (info.bEnabled == true) {
				if (info.bChecked == true) {
					view.setBackgroundResource(R.color.white_color);
					holder.imgIcon.setVisibility(View.VISIBLE);
				} else {
					view.setBackgroundResource(R.drawable.table_normal);
				}
				holder.txtName.setTextColor(Color.BLACK);
			} else {
				view.setBackgroundResource(R.color.white_color);
				holder.txtName.setTextColor(Color.GRAY);
			}

			holder.txtName.setText(info.strName);

			view.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					String strLoadRequestEOT = Utils.getStringSetting(mContext, Global.PREF_LOAD_REQUEST_EOT);
					if (info.bEnabled == true) {
						if (info.bChecked == false || (info.nIdx == LOAD_REQUEST && strLoadRequestEOT.trim().equals("2"))) {
							gotoCheck(info.nIdx);
						}
					}
				}
			});

			return view;
		}
	}

	private void createPDF() {
		try {
			Utils.createDirectory(Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/");
			mPdfTrxnNo = System.currentTimeMillis();
			mStrPdfFileName = getPDFFileName();
			Document document = new Document();
			PdfWriter.getInstance(document, new FileOutputStream(Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + mStrPdfFileName));
			document.open();
			PdfPTable table = createHeader();
			document.add(table);
			table = createBody();
			table.setSpacingBefore(20);
			document.add(table);
			document.close();

			String strDocNo = mStrPdfFileName.substring(0, mStrPdfFileName.length() - 4);
			long nTrxnNo = setGeneralUploadData("SERV", 0, 0, -1, "", strDocNo);

			/*TransactionInfo transactionInfo = new TransactionInfo();
			transactionInfo.strType = "STKE";
			transactionInfo.strDocNo = strDocNo;
			transactionInfo.strTime = new SimpleDateFormat("kk:mm").format(new Date(nTrxnNo));
			DbAdapter dbAdapter = new DbAdapter(mContext);
			dbAdapter.open();
			dbAdapter.insertTransactionHistoryInfo(transactionInfo);
			dbAdapter.close();*/

			nTrxnNo = System.currentTimeMillis();
			UTransactionsInfo uTransactionsInfo = setUTransactionsLastInfo(nTrxnNo, "FARC", mStrPdfFileName);
			setUFileTransactionInfo(uTransactionsInfo.nTrxnNo, 0, 0, mPdfTrxnNo, strDocNo, "NONSERVICED", "Undelivered Customers Report", uTransactionsInfo.strTrxnDate, uTransactionsInfo.strTrxnTime, mStrPdfFileName);
			Utils.setBooleanSetting(mContext, Global.PREF_CREATED_UNSERVICED_REASON_REPORT, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private PdfPTable createHeader() {
		PdfPTable table = new PdfPTable(3);
		PdfPCell cell;
		Phrase phrase;
		Date curDate = new Date();
		try {
			table.setWidths(new int[]{1, 1, 1});
			table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
			if (Utils.isFileExist(Global.DEFAULT_COMPANY_LOGO_FILEPATH) == true) {
				Image image = Image.getInstance(Global.DEFAULT_COMPANY_LOGO_FILEPATH);
				image.scaleToFit(120, 60);
				cell = new PdfPCell(image);
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			} else {
				phrase = new Phrase("No Logo File", fontBlack10Bold);
				cell = new PdfPCell(phrase);
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell.setVerticalAlignment(Element.ALIGN_CENTER);
			}
			cell.setRowspan(7);
			cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
			cell.setBorder(Rectangle.NO_BORDER);
			table.addCell(cell);

			String strDocument = "";
			if (mStrPdfFileName.length() > 4) {
				strDocument = mStrPdfFileName.substring(0, mStrPdfFileName.length() - 4);
			}
			phrase = new Phrase("Trip Number: " + Global.gTripNumber, fontDarkGray7Normal);
			table.addCell(phrase);
			phrase = new Phrase("Document #: " + strDocument, fontDarkGray7Normal);
			cell = new PdfPCell(phrase);
			cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
			cell.setRowspan(7);
			cell.setBorder(Rectangle.NO_BORDER);
			table.addCell(cell);
			phrase = new Phrase("Date: " + new SimpleDateFormat(Utils.getDateFormat(mContext, Global.DATE_DD_MM_YY_FORMAT)).format(curDate), fontDarkGray7Normal);
			table.addCell(phrase);
			phrase = new Phrase("Time: " + new SimpleDateFormat(Global.TIME_HH_MM_FORMAT).format(curDate), fontDarkGray7Normal);
			table.addCell(phrase);
			phrase = new Phrase("User Name: " + Utils.getStringSetting(mContext, Global.PREF_USER_DESCRIPTION), fontDarkGray7Normal);
			table.addCell(phrase);
			phrase = new Phrase("Route #: " + Utils.getStringSetting(mContext, Global.PREF_ROUTE_NUMBER), fontDarkGray7Normal);
			table.addCell(phrase);
			phrase = new Phrase("Vehicle #: " + Utils.getStringSetting(mContext, Global.PREF_VEHICLE_NUMBER), fontDarkGray7Normal);
			table.addCell(phrase);
			phrase = new Phrase("Transaction #: " + mPdfTrxnNo, fontDarkGray7Normal);
			table.addCell(phrase);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return table;
	}

	private PdfPTable createBody() {
		PdfPTable table = new PdfPTable(4);
		PdfPCell cell;
		Phrase phrase;
		try {
			table.setWidths(new int[]{2, 3, 4, 2});
			table.getDefaultCell().setBorder(Rectangle.NO_BORDER);

			phrase = new Phrase("Undelivered Customers Report", fontBlack10Bold);
			cell = new PdfPCell(phrase);
			cell.setColspan(4);
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell.setBorder(Rectangle.NO_BORDER);
			cell.setPadding(5);
			table.addCell(cell);

			phrase = new Phrase("Customer Number", fontBlack7Bold);
			table.addCell(phrase);
			phrase = new Phrase("Customer Name", fontBlack7Bold);
			table.addCell(phrase);
			phrase = new Phrase("Ship To Address", fontBlack7Bold);
			table.addCell(phrase);
			phrase = new Phrase("Undelivered Reason", fontBlack7Bold);
			table.addCell(phrase);

			List<IncompleteInfo> printList = new ArrayList<>();
			printList.addAll(mIncompletedList);
			for (IncompleteInfo info : printList) {
				phrase = new Phrase(String.valueOf(info.customerInfo.nCustNo), fontDarkGray7Normal);
				table.addCell(phrase);
				phrase = new Phrase(info.customerInfo.strName, fontDarkGray7Normal);
				table.addCell(phrase);
				String strAddress = info.customerInfo.strAddress1;
				if (info.customerInfo.strAddress2.length() > 0) {
					if (strAddress.length() > 0) {
						strAddress += ", ";
					}
					strAddress += info.customerInfo.strAddress2;
				}
				if (info.customerInfo.strCity.length() > 0) {
					if (strAddress.length() > 0) {
						strAddress += ", ";
					}
					strAddress += info.customerInfo.strCity;
				}
				phrase = new Phrase(strAddress, fontDarkGray7Normal);
				table.addCell(phrase);
				phrase = new Phrase(info.strReason, fontDarkGray7Normal);
				table.addCell(phrase);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return table;
	}

	private void outputPrinter() {
		if (BluetoothAdapter.getDefaultAdapter().isEnabled() == false) {
			try {
				final MessageDialog msgDialog = new MessageDialog(mContext);
				msgDialog.setCancelable(false);
				msgDialog.mStrMessage = Str.TurnOnBluetooth_alert[Global.LANG];
				msgDialog.mStrLeft = Str.Cancel[Global.LANG];
				msgDialog.mStrRight = Str.Retry[Global.LANG];
				msgDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
							outputPrinter();
						} else if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {

						}
					}
				});
				msgDialog.show();
			}catch (Exception e)
			{
				e.printStackTrace();
			}

			return;
		}
		if(Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 1)
		{
			mStrMacAddress = Utils.getStringSetting(mContext, Global.PREF_PRINTER_MAC_ADDRESS);
		}
		else if(Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 2)
		{
			mStrMacAddress = Utils.getStringSetting(mContext, Global.PREF_HONEYWELL_PRINTER_MAC_ADDRESS);
		}
		else if(Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 3)
		{
			mStrMacAddress = Utils.getStringSetting(mContext, Global.PREF_HONEYWELL_LINE_PRINTER_MAC_ADDRESS);
		}
		else if(Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 4)
		{
			mStrMacAddress = Utils.getStringSetting(mContext, Global.PREF_BIXOLON_PRINTER_MAC_ADDRESS);
			mStrFriendlyName = Utils.getStringSetting(mContext, Global.PREF_BIXOLON_PRINTER_FRIENDLY_NAME);
		}

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
		}, mStrMacAddress, Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + mStrTransactionPdfFileName);
	}

	private void doHoneywellLinePrintPdfTask()
	{
		if(Utils.isFileExist(mHoneywellTemplatePath))
		{
			showProgressDialog(Str.Printing[Global.LANG]);
			HoneywellLinePrinter.doPrintDotMatrix(mContext, new HoneywellLinePrinter.PrintCallback() {
				@Override
				public void printFinished() {
					hideProgressDialog();
					try {
						Utils.setStringSetting(mContext, Global.PREF_HONEYWELL_PRINTER_MAC_ADDRESS, mStrMacAddress);
						Utils.printDebug(mContext, Str.SuccessToPrint[Global.LANG]);
					} catch (Exception e) {
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
								doHoneywellLinePrintPdfTask();
							} else if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
							}
						});
						msgDialog.show();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}, mStrMacAddress, mHoneywellTemplatePath, mHoneywellPrintEngine);
		}
		else {

		}

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
							doBixolonPrintPdfTask();
						} else if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
						}
					});
					msgDialog.show();
				}catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}, mStrMacAddress, mStrFriendlyName, Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + mStrTransactionPdfFileName);
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
				final MessageDialog msgDialog = new MessageDialog(mContext);
				msgDialog.setCancelable(false);
				msgDialog.mStrMessage = Str.TurnOnZebraPrinter[Global.LANG];
				msgDialog.mStrLeft = Str.Cancel[Global.LANG];
				msgDialog.mStrRight = Str.Retry[Global.LANG];
				msgDialog.setOnDismissListener(dialog -> {
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
			if(!mContext.isFinishing() && !mContext.isDestroyed())
			{
				try {
					Toast.makeText(mContext, Str.NoDeviceDiscover[Global.LANG], Toast.LENGTH_LONG).show();
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
		/*AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setTitle(Str.SelectPrinter[Global.LANG]);
		builder.setItems(strDevices, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int position) {
				String strArray[] = strDevices[position].split("\n");
				if (strArray.length > 0) {
					mStrMacAddress = strArray[0];
					new PrintPdfTask().execute();
				}
			}
		});
		AlertDialog alert = builder.create();
		alert.setCancelable(false);
		alert.show();*/

		String strArray[] = strDevices[0].split("\n");
		if (strArray.length > 0) {
			mStrMacAddress = strArray[0];
		}
		if (strArray.length > 1) {
			mStrFriendlyName = strArray[1];
		}

		try {
			new AlertDialog.Builder(mContext)
					.setTitle(Str.SelectPrinter[Global.LANG])
					.setSingleChoiceItems(new AlertSingleChoiceAdapter(mContext, strDevices), 0, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialogInterface, int i) {
							String strArray[] = strDevices[i].split("\n");
							if (strArray.length > 0) {
								mStrMacAddress = strArray[0];
							}
							if (strArray.length > 1) {
								mStrFriendlyName = strArray[1];
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

	private class PrintPdfTask extends AsyncTask<Void, String, String> {

		int nMessageType = 0;

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
							printer.sendFileContents(Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + mStrTransactionPdfFileName);
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
			try
			{
				hideProgressDialog();
				if (result.length() == 0) {
					Utils.setStringSetting(mContext, Global.PREF_PRINTER_MAC_ADDRESS, mStrMacAddress);
					Utils.printDebug(mContext, Str.SuccessToPrint[Global.LANG]);
				} else {
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
					msgDialog.setOnDismissListener(dialog -> {
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
					});
					msgDialog.show();
				}
			}catch(Exception e)
			{
				e.printStackTrace();
			}

		}
	}
}