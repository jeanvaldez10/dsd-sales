package dsd.dsdvansales.fragment;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import dsd.dsdvansales.R;
import dsd.dsdvansales.activities.SignatureActivity;
import dsd.dsdvansales.db.DbAdapter;
import dsd.dsdvansales.dialogs.AdjustConsignmentDialog;
import dsd.dsdvansales.dialogs.InputReceiverNameDialog;
import dsd.dsdvansales.dialogs.MessageDialog;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.HoneywellLinePrinter;
import dsd.dsdvansales.global.HoneywellPrinter;
import dsd.dsdvansales.global.PrintEngine;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.model.CustConBalInfo;
import dsd.dsdvansales.model.MakeZipFileInfo;
import dsd.dsdvansales.model.PricingInfo;
import dsd.dsdvansales.model.PrintItem;
import dsd.dsdvansales.model.ProdDetlInfo;
import dsd.dsdvansales.model.ProdInfo;
import dsd.dsdvansales.model.UInvenDInfo;
import dsd.dsdvansales.model.UInvenHInfo;
import dsd.dsdvansales.model.UStockDInfo;
import dsd.dsdvansales.model.UStockHInfo;
import dsd.dsdvansales.model.UTransactionsInfo;
import dsd.dsdvansales.views.AlertSingleChoiceAdapter;

public class UpdateConsignmentInventoryFragment extends BaseFragment implements View.OnClickListener {

	private TextView mTxtNoData = null;
	private ListView mListView = null;
	private ListAdapter mListAdapter = null;
	private List<CustConBalInfo> mCustConBalList = new ArrayList<>();

	private long mPdfTrxnNo = 0;
	private String mStrPdfFileName = "";
	private Connection printerConnection = null;
	private String mStrMacAddress = "";
	private boolean mIsCreatedPDF = false;
	private Map<String, ProdDetlInfo> mProdDetlItemMap = new HashMap<>();
	private Map<String, ProdDetlInfo> mProdDetlUPCMap = new HashMap<>();
	private Map<String, ProdInfo> mProdInfoMap = new HashMap<>();
	private Map<String, PricingInfo> mPriceInfoMap = new HashMap<>();
	private String mStrSignatureFilePath = "";
	private String mStrReceiverName = "";
	public boolean mExist = false;

	private PrintEngine mHoneywellPrintEngine = null;
	private String mHoneywellTemplatePath = "";


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootView = inflater.inflate(R.layout.fragment_update_consignment_inventory, container, false);
		commonInit();
		return mRootView;
	}


	@Override
	public void onResume() {
		super.onResume();
		Global.gFragIdx = Global.FRAG_UPDATE_CONSIGNMENT_INVENTORY;
		bPaused = false;
		if(bNeedBack)
		{
			new Handler(Looper.getMainLooper()).post(new Runnable() {
				@Override
				public void run() {
					mContext.onBackPressed();
				}
			});
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		bPaused = true;
	}

	@Override
	public void initView() {
		mRootView.findViewById(R.id.btnBack).setOnClickListener(this);
//		mRootView.findViewById(R.id.btnNote).setOnClickListener(this);
		mRootView.findViewById(R.id.btnAdjust).setOnClickListener(this);
//		mRootView.findViewById(R.id.btnCompleted).setOnClickListener(this);

		mTxtNoData = (TextView) mRootView.findViewById(R.id.txtNoData);
		mListView = (ListView) mRootView.findViewById(R.id.listView);


		mListAdapter = new ListAdapter();
		mListView.setAdapter(mListAdapter);

		loadData();
		refreshLanguage();
	}

	@Override
	public void loadData() {
		showProgressDialog(Str.Loading[Global.LANG]);
		new Thread()
		{
			@Override
			public void run() {
				if(Global.gCustConBalList.size() == 0)
				{
					mProdDetlItemMap.clear();
					mProdDetlUPCMap.clear();
					DbAdapter dbAdapter = new DbAdapter(mContext);
					dbAdapter.open();
					dbAdapter.getProductList(mProdDetlItemMap, mProdDetlUPCMap);
//					if(Utils.getStringSetting(mContext, Global.PREF_ITEM_CODE_DISPLAY_SOURCE).equals("PRODINFO"))
//					{
//						mProdInfoMap = dbAdapter.getProdInfoMap("ItemCode");
//					}
					List<CustConBalInfo>  custConBalInfoList = dbAdapter.getCustConBalInfo(Global.gCurCustomerInfo.nCustNo, Global.gCurCustomerInfo.nChainNo);
					mCustConBalList.clear();
					mCustConBalList.addAll(custConBalInfoList);
					Collections.sort(mCustConBalList, new Comparator<CustConBalInfo>() {
						@Override
						public int compare(CustConBalInfo uInvenDInfo, CustConBalInfo t1) {
							return uInvenDInfo.strItemNo.compareTo(t1.strItemNo);
						}
					});

					for(CustConBalInfo info : mCustConBalList)
					{
						ProdDetlInfo productInfo = mProdDetlItemMap.get(info.strItemNo);
						if(productInfo != null)
						{
							if(productInfo.prodLocnInfo != null)
							{
								if(info.nCustNo != 0)
								{
									mPriceInfoMap = dbAdapter.getPricingInfo(info.nCustNo);
									PricingInfo pricingInfo = mPriceInfoMap.get(info.strItemNo);
									if(pricingInfo != null)
									{
										info.nPrice = Utils.getDividedValue(pricingInfo.nPrice);
									}
									else
									{
										info.nPrice = Utils.getDividedValue(productInfo.prodLocnInfo.nBasePrice);;
									}
								}
								else
								{
									info.nPrice = Utils.getDividedValue(productInfo.prodLocnInfo.nBasePrice);;
								}
							}
						}
					}
					Global.gCustConBalList.addAll(mCustConBalList);
					Global.gCurInventoryList.clear();
					Global.gAdjustinfo = dbAdapter.getUInvenHInfo(Utils.getLongSetting(mContext, Global.PREF_UINVEN_TRXN_NO));
					if (Global.gAdjustinfo == null) {
						Global.gAdjustinfo = new UInvenHInfo();
						Global.gAdjustinfo.nTrxnNo = Utils.getLongSetting(mContext, Global.PREF_UINVEN_TRXN_NO);
					}
					Global.gCurInventoryList.addAll(dbAdapter.getUInvenDList(Global.gAdjustinfo.nTrxnNo));
					dbAdapter.close();
				}
				else
				{
					mCustConBalList.clear();
					mCustConBalList.addAll(Global.gCustConBalList);
					Collections.sort(mCustConBalList, new Comparator<CustConBalInfo>() {
						@Override
						public int compare(CustConBalInfo uInvenDInfo, CustConBalInfo t1) {
							return uInvenDInfo.strItemNo.compareTo(t1.strItemNo);
						}
					});
				}

				new Handler(Looper.getMainLooper()).post(new Runnable() {
					@Override
					public void run() {
						hideProgressDialog();
						refreshPage();
					}
				});
			}
		}.start();
	}

	@Override
	public void refreshPage() {

		if (mCustConBalList.size() > 0) {
			mListView.setVisibility(View.VISIBLE);
			mTxtNoData.setVisibility(View.GONE);
			mListAdapter.setListData(mCustConBalList);
			mListAdapter.notifyDataSetChanged();
		} else {
			mListView.setVisibility(View.GONE);
			mTxtNoData.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void refreshLanguage() {
		((TextView) mRootView.findViewById(R.id.lblTitle)).setText(Str.Consignment_Inventory[Global.LANG].toUpperCase());
		((TextView) mRootView.findViewById(R.id.txtCustomerName)).setText(Global.gCurCustomerInfo.strName);
		((Button) mRootView.findViewById(R.id.btnAdjust)).setText(Str.Adjust[Global.LANG]);
		mTxtNoData.setText(Str.No_data_has_been_found[Global.LANG]);
	}

	private void showInputSignatureDialog()
	{
		try {
			final InputReceiverNameDialog dialog = new InputReceiverNameDialog(mContext);
			dialog.setOnDismissListener(new OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialogInterface) {
					mStrReceiverName = dialog.receivername;
					doSaveData();
				}
			});
			dialog.show();
		}catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	@Override
	public void receiveBroadcast(String strAction, Intent intent) {
		if (strAction.equals(Global.ACTION_CAPTURE_SIGNATURE) == true) {
			if (Global.gFragIdx == Global.FRAG_UPDATE_CONSIGNMENT_INVENTORY) {
				mStrSignatureFilePath = intent.getStringExtra(Global.EXTRA_SIGNATURE_PATH);
				showInputSignatureDialog();
			}
		}
		else if (strAction.equals(Global.ACTION_CAPTURE_SIGNATURE_EXIT) == true) {
			if (Global.gFragIdx == Global.FRAG_UPDATE_CONSIGNMENT_INVENTORY) {
				//Show the Input Signature screen
				showInputSignatureDialog();
			}
		}
	}

	private void outputPrinter() {
		mIsCreatedPDF = true;
		if (BluetoothAdapter.getDefaultAdapter().isEnabled() == false) {
			try {
				final MessageDialog msgDialog = new MessageDialog(mContext);
				msgDialog.setCancelable(false);
				msgDialog.mStrMessage = Str.TurnOnBluetooth_alert[Global.LANG];
				msgDialog.mStrLeft = Str.Cancel[Global.LANG];
				msgDialog.mStrRight = Str.Retry[Global.LANG];
				msgDialog.setOnDismissListener(new OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
							outputPrinter();
						}
						else
						{
							if(mExist)
							{
								goBack();
							}
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
		mStrMacAddress = Utils.getStringSetting(mContext, Global.PREF_PRINTER_MAC_ADDRESS);
		if (mStrMacAddress.length() == 0) {
			discoveryPrinters();
		} else {
			new PrintPdfTask().execute();
		}
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
				msgDialog.setOnDismissListener(new OnDismissListener() {
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
						} else if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
							if(mExist)
							{
								goBack();
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
		builder.setItems(strDevices, new OnClickListener() {
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
							if(mExist)
							{
								goBack();
							}
						}
					});
					msgDialog.show();
				}catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}, mStrMacAddress, Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + mStrPdfFileName);
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
						Utils.setStringSetting(mContext, Global.PREF_HONEYWELL_LINE_PRINTER_MAC_ADDRESS, mStrMacAddress);
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
								if (mExist) {
									goBack();
								}
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

	boolean bPaused = true;
	boolean bNeedBack = false;
	private void goBack() {
		mExist = true;
		if(bPaused)
		{
			bNeedBack = true;
		}
		else
		{
			mContext.onBackPressed();
		}
	}

	public void doProcessBack(){
		mRootView.findViewById(R.id.btnBack).performClick();
	}

	private void doPreparePDFData()
	{
		if(Utils.isFileExist(Global.ROOT_PRINT_TEMPLATE_LOCCATION + "/" + Global.PRINT_CONSIGNMENT_INVENTORY_FILENAME))
		{
			PrintEngine printEngine = new PrintEngine();
			mHoneywellPrintEngine = printEngine;

			Utils.createDirectory(Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/");
			mPdfTrxnNo = System.currentTimeMillis();
			mStrPdfFileName = getPDFFileName();

			DbAdapter dbAdapter = new DbAdapter(mContext);
			dbAdapter.open();
			Utils.gDetlMap.clear();
			if(mCustConBalList != null)
			{
				for(CustConBalInfo info : mCustConBalList)
				{
					ProdDetlInfo prodDetlInfo = dbAdapter.getProdDetl(info.strItemNo);
					if(prodDetlInfo != null)
					{
						Utils.gDetlMap.put(prodDetlInfo.strItemNo, Utils.getPropertyMap(prodDetlInfo));
					}
				}
			}
			dbAdapter.close();

			printEngine.PrepareConsignmentInventoryPrint(mContext, mCustConBalList, new Date(), mStrPdfFileName.substring(0, mStrPdfFileName.length() - 4), mStrSignatureFilePath, mStrReceiverName);
			String strHoneywellFileName = Utils.getFinalPrintTemplateFileNameForHoneywell(mContext, Global.PRINT_CONSIGNMENT_INVENTORY_FILENAME);
			mHoneywellTemplatePath = Global.ROOT_PRINT_TEMPLATE_LOCCATION + strHoneywellFileName;
			printEngine.createDocument(mContext, false, Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + mStrPdfFileName, Global.CONSIGNMENT_INVENTORY_PRINT);
		}

		long nTrxnNo = System.currentTimeMillis();
		//UTransactionsInfo uTransactionsInfo = setUTransactionsInfo(Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo, nTrxnNo, "FARC", mStrPdfFileName);
		UTransactionsInfo uTransactionsInfo = setUTransactionsInfo(0, 0, nTrxnNo, "FARC", mStrPdfFileName);
		setUFileTransactionInfo(uTransactionsInfo.nTrxnNo, 0, 0, mPdfTrxnNo, mStrPdfFileName.substring(0, mStrPdfFileName.length() - 4), "CONSIGNMENTINVENTORY", "CONSIGNMENT INVENTORY", uTransactionsInfo.strTrxnDate, uTransactionsInfo.strTrxnTime, mStrPdfFileName);
	}

	public boolean m_bClicked = false;
	@Override
	public void onClick(View v) {
		if( m_bClicked ) return;
		m_bClicked = true;
		switch (v.getId()) {
			case R.id.btnBack:
				/*if(Global.gConsignmentAdjust.size() > 0)
				{
					String strMessage = Str.Are_you_sure_wish_to_exit_make_delivery[Global.LANG] + "\n";
					final MessageDialog msgDialog = new MessageDialog(mContext);
					msgDialog.setCancelable(false);
					msgDialog.mStrTitle = Str.exit_consignment[Global.LANG];
					msgDialog.mStrMessage = strMessage;
					msgDialog.mStrLeft = Str.No[Global.LANG];
					msgDialog.mStrRight = Str.Yes[Global.LANG];
					msgDialog.setOnDismissListener(new OnDismissListener() {
						@Override
						public void onDismiss(DialogInterface dialog) {
							if( msgDialog.mReturnCode == Global.DLG_RETURN_LEFT )
							{

							}
							else if( msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT )
							{
								goBack();
							}
						}
					});
					msgDialog.show();
				}
				else
				{
					goBack();
				}*/
				goBack();
				break;

			/*case R.id.btnNote:
				doClickNote();
				break;*/

			case R.id.btnAdjust:
				try {
					final AdjustConsignmentDialog dlg = new AdjustConsignmentDialog(mContext);
					dlg.setCancelable(false);
					dlg.setOnDismissListener(new OnDismissListener() {
						@Override
						public void onDismiss(DialogInterface dialog) {
							if (dlg.mReturnCode == Global.DLG_RETURN_RIGHT) {
								int nType = dlg.mReasonType;
								Bundle bundle = new Bundle();
								bundle.putInt(Global.EXTRA_TRANSFER_TYPE, dlg.mReasonType);
								AdjustConsignmentInventoryFragment fragment = new AdjustConsignmentInventoryFragment();
								fragment.setArguments(bundle);
								mContext.replaceFragment(fragment);
							}
						}
					});
					dlg.show();
				}catch (Exception e)
				{
					e.printStackTrace();
				}

				break;

			/*case R.id.btnCompleted:
				doCompleted();
				break;*/
		}
		m_bClicked = false;
	}

	private void doClickNote()
	{
		if (mIsCreatedPDF == false) {
			new Thread(){
				@Override
				public void run() {
					new Handler(Looper.getMainLooper()).post(new Runnable() {
						@Override
						public void run() {
							showProgressDialog(Str.CreatingPdf[Global.LANG]);
						}
					});
					doPreparePDFData();

					new Handler(Looper.getMainLooper()).post(() -> {
						hideProgressDialog();
						if( Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 1 )
						{
							try {
								final MessageDialog msgDialog = new MessageDialog(mContext);
								msgDialog.setCancelable(false);
								msgDialog.mStrMessage = Str.Print_msg[Global.LANG];
								msgDialog.mStrLeft = Str.No[Global.LANG];
								msgDialog.mStrRight = Str.Yes[Global.LANG];
								msgDialog.setOnDismissListener(dialog -> {
									if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
										outputPrinter();
									}
									else
									{
										if(mExist)
										{
											goBack();
										}
									}
								});
								msgDialog.show();
							}catch (Exception e)
							{
								e.printStackTrace();
							}
						}
						else if(Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 2)
						{
							try {
								final MessageDialog msgDialog = new MessageDialog(mContext);
								msgDialog.setCancelable(false);
								msgDialog.mStrMessage = Str.Print_msg[Global.LANG];
								msgDialog.mStrLeft = Str.No[Global.LANG];
								msgDialog.mStrRight = Str.Yes[Global.LANG];
								msgDialog.setOnDismissListener(dialog -> {
									if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
										outputHoneywellPrinter();
									} else if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
										if(mExist)
										{
											goBack();
										}
									}
								});
								msgDialog.show();
							}catch (Exception e)
							{
								e.printStackTrace();
							}
						}
						else if(Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 3)
						{
							try {
								final MessageDialog msgDialog = new MessageDialog(mContext);
								msgDialog.setCancelable(false);
								msgDialog.mStrMessage = Str.Print_msg[Global.LANG];
								msgDialog.mStrLeft = Str.No[Global.LANG];
								msgDialog.mStrRight = Str.Yes[Global.LANG];
								msgDialog.setOnDismissListener(dialog -> {
									if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
										outputHoneywellLinePrinter();
									} else if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
										if(mExist)
										{
											goBack();
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
			}.start();
		} else {
			if( Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 1 )
			{
				try {
					final MessageDialog msgDialog = new MessageDialog(mContext);
					msgDialog.setCancelable(false);
					msgDialog.mStrMessage = Str.Print_msg[Global.LANG];
					msgDialog.mStrLeft = Str.No[Global.LANG];
					msgDialog.mStrRight = Str.Yes[Global.LANG];
					msgDialog.setOnDismissListener(dialog -> {
						if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
							outputPrinter();
						}
						else
						{
							if(mExist)
							{
								goBack();
							}
						}
					});
					msgDialog.show();
				}catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			else if(Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 2)
			{
				try {
					final MessageDialog msgDialog = new MessageDialog(mContext);
					msgDialog.setCancelable(false);
					msgDialog.mStrMessage = Str.Print_msg[Global.LANG];
					msgDialog.mStrLeft = Str.No[Global.LANG];
					msgDialog.mStrRight = Str.Yes[Global.LANG];
					msgDialog.setOnDismissListener(dialog -> {
						if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
							outputHoneywellPrinter();
						} else if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
							if(mExist)
							{
								goBack();
							}
						}
					});
					msgDialog.show();
				}catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			else if(Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 3)
			{
				try {
					final MessageDialog msgDialog = new MessageDialog(mContext);
					msgDialog.setCancelable(false);
					msgDialog.mStrMessage = Str.Print_msg[Global.LANG];
					msgDialog.mStrLeft = Str.No[Global.LANG];
					msgDialog.mStrRight = Str.Yes[Global.LANG];
					msgDialog.setOnDismissListener(dialog -> {
						if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
							outputHoneywellLinePrinter();
						} else if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
							if(mExist)
							{
								goBack();
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
	}
	private void doSaveData()
	{
		showProgressDialog(Str.processing[Global.LANG]);
		new Thread()
		{
			@Override
			public void run() {
				initMakeZipInfo();

				doPreparePDFData();

				if(Global.gConsignmentAdjust.size() > 0)
				{
					//Apply Consignment & UInvenList
					DbAdapter dbAdapter = new DbAdapter(mContext);
					dbAdapter.open();
					List<UInvenDInfo> uInvenDInfoList = dbAdapter.getUInvenDList(Global.gAdjustinfo.nTrxnNo);
					Map<String, UInvenDInfo> uInvenDInfoMap = new HashMap<>();
					for(UInvenDInfo uInvenDInfo : uInvenDInfoList)
					{
						uInvenDInfoMap.put(uInvenDInfo.strItemNo, uInvenDInfo);
					}

					//Apply Consignment
					for(CustConBalInfo info : mCustConBalList)
					{
						if(info.nIdx != 0)
						{
							dbAdapter.updateCustConBalInfo(info);
						}
						else
						{
							dbAdapter.insertCustConBalInfo(info);
						}
					}

					for(MakeZipFileInfo makeZipFileInfo : Global.gConsignmentAdjust)
					{
						if(makeZipFileInfo == null || makeZipFileInfo.trxnList.size() == 0) continue;
						List<UStockHInfo> uStockHInfoList = dbAdapter.getUConsignmentInfoList(makeZipFileInfo);
						for(UStockHInfo uStockHInfo : uStockHInfoList)
						{
							if(!uStockHInfo.strStockType.equals("2")) continue;

							List<UStockDInfo> uStockDInfoList = dbAdapter.getUStockDInfoList(uStockHInfo.nTrxnNo);
							if(uStockDInfoList.size() > 0)
							{
								UInvenHInfo uInvenHInfo = new UInvenHInfo();
								uInvenHInfo.strDocType = "IVUP";
								uInvenHInfo.nTrxnNo = uStockHInfo.nTrxnNo;
								uInvenHInfo.strTrxnDate = uStockHInfo.strTrxnDate;
								uInvenHInfo.strTrxnTime = uStockHInfo.strTrxnTime;
								uInvenHInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
								dbAdapter.insertUInvenHInfo(uInvenHInfo);
								if(!TextUtils.isEmpty(mStrPdfFileName))
								{
									uStockHInfo.strDocNo = mStrPdfFileName.substring(0, mStrPdfFileName.length() - 4);
									dbAdapter.updateUConsignmentInfo(uStockHInfo);

									//Register Print document list.
									PrintItem printItem = new PrintItem();
									printItem.nTrxnNo = uStockHInfo.nTrxnNo;
									printItem.strDocNo = uStockHInfo.strDocNo;
									printItem.strDocType = uStockHInfo.strDocType;
									printItem.strFilePath = Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + mStrPdfFileName;
									printItem.nCustNo = Global.gCurCustomerInfo.nCustNo;
									printItem.nChainNo = Global.gCurCustomerInfo.nChainNo;
									printItem.strCustomerName = Global.gCurCustomerInfo.strName;
									dbAdapter.insertPrint(printItem);
								}

								for(UStockDInfo uStockDInfo : uStockDInfoList)
								{
									boolean bModified = false;
									if(Utils.getProdLoconSCWType(mContext, uStockDInfo.strItemNo) == Global.WEIGHT_ITEM)
									{
										if((uStockDInfo.dActualQty - uStockDInfo.dBeginQty) != 0) bModified = true;
									}
									else
									{
										if((uStockDInfo.nActualQty - uStockDInfo.nBeginQty) != 0) bModified = true;
									}

									UInvenDInfo uInvenDInfo = uInvenDInfoMap.get(uStockDInfo.strItemNo);
									if(uInvenDInfo == null)
									{
										uInvenDInfo = new UInvenDInfo();
										uInvenDInfo.strItemNo = uStockDInfo.strItemNo;
										uInvenDInfoMap.put(uInvenDInfo.strItemNo, uInvenDInfo);
										uInvenDInfo.nTrxnNo = Global.gAdjustinfo.nTrxnNo;
										if(Utils.getProdLoconSCWType(mContext, uStockDInfo.strItemNo) == Global.WEIGHT_ITEM)
										{
											uInvenDInfo.nSCWType = Global.WEIGHT_ITEM;
										}
										else
										{
											uInvenDInfo.nSCWType = Global.INVENTORY_TRUCK;
										}
										List<ProdDetlInfo> prodDetlInfoList = dbAdapter.getProductList(uInvenDInfo.strItemNo);
										if(prodDetlInfoList != null && prodDetlInfoList.size() > 0)
										{
											uInvenDInfo.strDescription = prodDetlInfoList.get(0).strDescription;
											uInvenDInfo.strShortDesc = prodDetlInfoList.get(0).strShortDesc;
										}
										if(bModified)
										{
											uInvenDInfo.nIdx = (int)dbAdapter.insertUInvenDInfo(uInvenDInfo);
										}
									}

									uInvenDInfo.bShow = true;
									UInvenDInfo newUInvenDInfo = uInvenDInfo.copyInfo();
									newUInvenDInfo.nTrxnNo = uInvenHInfo.nTrxnNo;

									if(uStockHInfo.strDocType.equals("ADJI"))
									{
										if(Utils.getProdLoconSCWType(mContext, uStockDInfo.strItemNo) == Global.WEIGHT_ITEM)
										{
											newUInvenDInfo.dTransOut += (uStockDInfo.dActualQty - uStockDInfo.dBeginQty);
											newUInvenDInfo.dStock -= (uStockDInfo.dActualQty - uStockDInfo.dBeginQty);
											uInvenDInfo.dTransOut = newUInvenDInfo.dTransOut;
											uInvenDInfo.dStock = newUInvenDInfo.dStock;
											uStockDInfo.dActualQty = (uStockDInfo.dActualQty - uStockDInfo.dBeginQty);
										}
										else
										{
											newUInvenDInfo.nTransOut += (uStockDInfo.nActualQty - uStockDInfo.nBeginQty);
											newUInvenDInfo.nStock -= (uStockDInfo.nActualQty - uStockDInfo.nBeginQty);
											uInvenDInfo.nTransOut = newUInvenDInfo.nTransOut;
											uInvenDInfo.nStock = newUInvenDInfo.nStock;
											uStockDInfo.nActualQty = (uStockDInfo.nActualQty - uStockDInfo.nBeginQty);
										}
									}
									else if(uStockHInfo.strDocType.equals("ADJO"))
									{
										if(Utils.getProdLoconSCWType(mContext, uStockDInfo.strItemNo) == Global.WEIGHT_ITEM)
										{
											newUInvenDInfo.dTransIn += (uStockDInfo.dBeginQty - uStockDInfo.dActualQty);
											newUInvenDInfo.dStock += (uStockDInfo.dBeginQty - uStockDInfo.dActualQty);
											uInvenDInfo.dTransIn = newUInvenDInfo.dTransIn;
											uInvenDInfo.dStock = newUInvenDInfo.dStock;
											uStockDInfo.dActualQty = (uStockDInfo.dBeginQty - uStockDInfo.dActualQty);
										}
										else
										{
											newUInvenDInfo.nTransIn += (uStockDInfo.nBeginQty - uStockDInfo.nActualQty);
											newUInvenDInfo.nStock += (uStockDInfo.nBeginQty - uStockDInfo.nActualQty);
											uInvenDInfo.nTransIn = newUInvenDInfo.nTransIn;
											uInvenDInfo.nStock = newUInvenDInfo.nStock;
											uStockDInfo.nActualQty = (uStockDInfo.nBeginQty - uStockDInfo.nActualQty);
										}
									}
									else if(uStockHInfo.strDocType.equals("ADJS"))
									{
										if(Utils.getProdLoconSCWType(mContext, uStockDInfo.strItemNo) == Global.WEIGHT_ITEM)
										{
											newUInvenDInfo.dDumps += (uStockDInfo.dBeginQty - uStockDInfo.dActualQty);
											uInvenDInfo.dDumps = newUInvenDInfo.dDumps;
											uStockDInfo.dActualQty = (uStockDInfo.dBeginQty - uStockDInfo.dActualQty);
										}
										else
										{
											newUInvenDInfo.nDumps += (uStockDInfo.nBeginQty - uStockDInfo.nActualQty);
											uInvenDInfo.nDumps = newUInvenDInfo.nDumps;
											uStockDInfo.nActualQty = (uStockDInfo.nBeginQty - uStockDInfo.nActualQty);
										}
									}
									if(bModified)
									{
										dbAdapter.updateUInvenDInfo(uInvenDInfo);
									}
									dbAdapter.updateUStockDInfo(uStockDInfo);
									dbAdapter.insertUInvenDInfo(newUInvenDInfo);
								}

								AddTrxnNo(Global.gCurCustomerInfo.nCustNo, Global.gCurCustomerInfo.nChainNo, uStockHInfo.nTrxnNo);
							}

						}
					}
					dbAdapter.close();

					//Upload Data to the Server
					Utils.registerUploadFile(mContext, makeZipFileInfo);
					mExist = true;
					new Handler(Looper.getMainLooper()).post(() -> {
						hideProgressDialog();
						if( Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 1 )
						{
							try {
								final MessageDialog msgDialog = new MessageDialog(mContext);
								msgDialog.setCancelable(false);
								msgDialog.mStrMessage = Str.Print_msg[Global.LANG];
								msgDialog.mStrLeft = Str.No[Global.LANG];
								msgDialog.mStrRight = Str.Yes[Global.LANG];
								msgDialog.setOnDismissListener(dialog -> {
									if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
										outputPrinter();
									}
									else
									{
										if(mExist)
										{
											goBack();
										}
									}
								});
								msgDialog.show();
							}catch (Exception e)
							{
								e.printStackTrace();
							}
						}
						else if(Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 2)
						{
							try {
								final MessageDialog msgDialog = new MessageDialog(mContext);
								msgDialog.setCancelable(false);
								msgDialog.mStrMessage = Str.Print_msg[Global.LANG];
								msgDialog.mStrLeft = Str.No[Global.LANG];
								msgDialog.mStrRight = Str.Yes[Global.LANG];
								msgDialog.setOnDismissListener(dialog -> {
									if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
										outputHoneywellPrinter();
									} else if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
										if(mExist)
										{
											goBack();
										}
									}
								});
								msgDialog.show();
							}catch (Exception e)
							{
								e.printStackTrace();
							}
						}
						else if(Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 3)
						{
							try {
								final MessageDialog msgDialog = new MessageDialog(mContext);
								msgDialog.setCancelable(false);
								msgDialog.mStrMessage = Str.Print_msg[Global.LANG];
								msgDialog.mStrLeft = Str.No[Global.LANG];
								msgDialog.mStrRight = Str.Yes[Global.LANG];
								msgDialog.setOnDismissListener(dialog -> {
									if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
										outputHoneywellLinePrinter();
									} else if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
										if(mExist)
										{
											goBack();
										}
									}
								});
								msgDialog.show();
							}catch (Exception e)
							{
								e.printStackTrace();
							}
						}
						else
						{
							goBack();
						}

					});
				}
			}
		}.start();
	}

	private void outputHoneywellPrinter() {
		mIsCreatedPDF = true;
		if (BluetoothAdapter.getDefaultAdapter().isEnabled() == false) {
			try {
				final MessageDialog msgDialog = new MessageDialog(mContext);
				msgDialog.setCancelable(false);
				msgDialog.mStrMessage = Str.TurnOnBluetooth_alert[Global.LANG];
				msgDialog.mStrLeft = Str.Cancel[Global.LANG];
				msgDialog.mStrRight = Str.Retry[Global.LANG];
				msgDialog.setOnDismissListener(dialog -> {
					if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
						outputHoneywellPrinter();
					} else if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
						if(mExist)
						{
							goBack();
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
		mStrMacAddress = Utils.getStringSetting(mContext, Global.PREF_HONEYWELL_PRINTER_MAC_ADDRESS);

		if (mStrMacAddress.length() == 0) {
			discoveryHoneywellPrinters();
		} else {
			doHoneywellPrintPdfTask();
		}
	}

	private void outputHoneywellLinePrinter() {
		mIsCreatedPDF = true;
		if (BluetoothAdapter.getDefaultAdapter().isEnabled() == false) {
			try {
				final MessageDialog msgDialog = new MessageDialog(mContext);
				msgDialog.setCancelable(false);
				msgDialog.mStrMessage = Str.TurnOnBluetooth_alert[Global.LANG];
				msgDialog.mStrLeft = Str.Cancel[Global.LANG];
				msgDialog.mStrRight = Str.Retry[Global.LANG];
				msgDialog.setOnDismissListener(dialog -> {
					if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
						outputHoneywellLinePrinter();
					} else if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
						if(mExist)
						{
							goBack();
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
		mStrMacAddress = Utils.getStringSetting(mContext, Global.PREF_HONEYWELL_LINE_PRINTER_MAC_ADDRESS);

		if (mStrMacAddress.length() == 0) {
			discoveryHoneywellLinePrinters();
		} else {
			doHoneywellLinePrintPdfTask();
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

	private void doCompleted()
	{
		if(Global.gConsignmentAdjust.size() > 0 && Utils.isFileExist(Global.ROOT_PRINT_TEMPLATE_LOCCATION + "/" + Global.PRINT_CONSIGNMENT_INVENTORY_FILENAME) && mIsCreatedPDF == false)
		{
			//Logic..
			//Show the Signature screen
			//1. If user signed and okay -> Show the input receiver name screen.
			//2. If user not signed or cancel-> Show the input receiver name screen.
			//3. After user input the receiver name -> Create Pdf -> Save Data -> Show Print Dialog
			Intent intent = new Intent(mContext, SignatureActivity.class);
			intent.putExtra(Global.EXTRA_SIGNATURE_PATH, Global.DEFAULT_SIGNATURE_FILEPATH);
			startActivity(intent);
			mContext.overridePendingTransition(R.anim.zoom_in, 0);
		}
		else
		{
			doSaveData();
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
							printer.sendFileContents(Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + mStrPdfFileName);
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
						msgDialog.setOnDismissListener(new OnDismissListener() {
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
								}
								else
								{
									if(mExist)
									{
										goBack();
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
			}catch(Exception e)
			{
				e.printStackTrace();
			}
			
		}
	}


	private Map<String, String> mShortDescMap = new HashMap<>();
	private String getShortDesc(String strItemNo)
	{
		if( mShortDescMap.get(strItemNo) != null )
			return mShortDescMap.get(strItemNo);
		DbAdapter adapter = new DbAdapter(mContext);
		adapter.open();
		List<ProdDetlInfo> prodDetInfos =  adapter.getProductList(strItemNo);
		adapter.close();

		String strDesc = "";
		if( prodDetInfos != null && prodDetInfos.size() > 0)
		{
			strDesc = prodDetInfos.get(0).strShortDesc;
		}
		else
		{
			strDesc = "";
		}
		mShortDescMap.put(strItemNo, strDesc);
		return strDesc;
	}


	private class ListAdapter extends BaseAdapter {

		private List<CustConBalInfo> list = new ArrayList<>();

		private class ViewHolder {
			public TextView txtItemNo, txtShortDesc, txtQty;
		}

		public int getCount() {
			return list.size();
		}

		public void setListData(List<CustConBalInfo> list) {
			this.list.clear();
			this.list.addAll(list);
			Collections.sort(this.list, new Comparator<CustConBalInfo>() {
				@Override
				public int compare(CustConBalInfo t1, CustConBalInfo t2) {
					return t1.strItemNo.compareTo(t2.strItemNo);
				}
			});
		}

		public CustConBalInfo getItem(int position) {
			if(position < list.size())
			{
				return list.get(position);
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
			final CustConBalInfo info = getItem(position);
			if(info == null) return view;

			if (convertView == null) {
				view = mContext.getLayoutInflater().inflate(R.layout.row_vehicle_inventory, parent, false);
				holder = new ViewHolder();
				holder.txtItemNo = (TextView) view.findViewById(R.id.txtItemNo);
				holder.txtShortDesc = (TextView) view.findViewById(R.id.txtShortDesc);
				holder.txtQty = (TextView) view.findViewById(R.id.txtQty);
				view.setTag(holder);
			} else {
				holder = (ViewHolder) view.getTag();
			}

//			String strItemNo = "";
//			if(Utils.getStringSetting(mContext, Global.PREF_ITEM_CODE_DISPLAY_SOURCE).equals("PRODINFO"))
//			{
//				ProdInfo prodInfo = mProdInfoMap.get(info.strItemNo);
//				strItemNo = prodInfo != null ? prodInfo.strInfoType : "";
//			}
//			else if(Utils.getStringSetting(mContext, Global.PREF_ITEM_CODE_DISPLAY_SOURCE).equals("UPC"))
//			{
//				ProdDetlInfo prodDetlInfo = mProdDetlItemMap.get(info.strItemNo);
//				strItemNo = prodDetlInfo != null ? prodDetlInfo.strItemUPC : "";
//			}
//			else if(Utils.getStringSetting(mContext, Global.PREF_ITEM_CODE_DISPLAY_SOURCE).equals("UPC20"))
//			{
//				ProdDetlInfo prodDetlInfo = mProdDetlItemMap.get(info.strItemNo);
//				strItemNo = prodDetlInfo != null ? prodDetlInfo.strItemUPC20 : "";
//			}
//			else {
//				strItemNo = info.strItemNo;
//			}
//			holder.txtItemNo.setText(strItemNo);

			holder.txtItemNo.setText(Utils.getDisplayItemCode(mContext, info.strItemNo));

			holder.txtShortDesc.setText(getShortDesc(info.strItemNo));
			if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
			{
				holder.txtQty.setText(String.format(Locale.ENGLISH, "%.2f", info.fWeight));
			}
			else
			{
				holder.txtQty.setText(String.format(Locale.ENGLISH, "%d", info.nQty));
			}
			return view;
		}
	}
}