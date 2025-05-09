package dsd.dsdvansales.fragment;

import android.app.*;
import android.bluetooth.*;
import android.content.*;
import android.net.Uri;
import android.os.*;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.google.android.datatransport.runtime.dagger.multibindings.ElementsIntoSet;
import com.zebra.sdk.comm.*;
import com.zebra.sdk.printer.*;
import com.zebra.sdk.printer.discovery.*;

import java.io.File;
import java.util.*;
import java.util.concurrent.CountDownLatch;

import dsd.dsdvansales.R;
import dsd.dsdvansales.db.DbAdapter;
import dsd.dsdvansales.dialogs.MessageDialog;
import dsd.dsdvansales.dialogs.ReprintActionDialog;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.HoneywellLinePrinter;
import dsd.dsdvansales.global.HoneywellPrinter;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.model.CustomerInfo;
import dsd.dsdvansales.model.DescTypeInfo;
import dsd.dsdvansales.model.PrintItem;
import dsd.dsdvansales.model.UOrderHInfo;
import dsd.dsdvansales.views.AlertSingleChoiceAdapter;

public class PrintDocumentsFragment extends BaseFragment implements View.OnClickListener {

	private class PrintDocumentInfo {
		public long nTrxnNo = 0;
		public String strCustomerName = "";
		public String strDocNo = "";
		public String strDocumentType = "";
		public String strFilePath = "";

		public float nInvoiceTotal = 0;
		public float nCentsRound = 0;
		public boolean bCentsRound = false;

		public UOrderHInfo uOrderHInfo = null;
		public PrintItem printItem = null;

		public long nVoidTrxnNo = 0;
		public String strVoidDocNo = "";
		public String strVoidFilePath = "";
		public float nVoidInvoiceTotal = 0;
		public float nVoidCentsRound = 0;
		public boolean bVoidCentsRound = false;

		public boolean bInvoice = false;
	}


	private TextView mTxtNoData = null;
	private ListView mListView = null;
	private ListAdapter mListAdapter = null;
	private List<PrintDocumentInfo> mDocumentList = new ArrayList<>();

	private Connection printerConnection = null;
	private String mStrMacAddress = "";
	private String mSelectedFilePath = "";
	private UOrderHInfo mSelectedInfo = null;
	private String mVoidType = "Void";
	private int showMethod = 0; //0 -- show all, 1 -- show only customer, 2 -- show with non-customer
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootView = inflater.inflate(R.layout.fragment_print_documents, container, false);
		
		commonInit();
		return mRootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		Global.gFragIdx = Global.FRAG_PRINT_DOCUMENTS;
		bPaused = false;
		if(bNeedBack)
		{
			new Handler(Looper.getMainLooper()).post(() -> mContext.onBackPressed());

		}
	}

	@Override
	public void onPause() {
		super.onPause();
		bPaused = true;
	}

	@Override
	public void initView() {
		mRootView.findViewById(R.id.btnCompleted).setOnClickListener(this);

		mTxtNoData = mRootView.findViewById(R.id.txtNoData);
		mListView = mRootView.findViewById(R.id.listView);
		mListAdapter = new ListAdapter();
		mListView.setAdapter(mListAdapter);

		mRootView.findViewById(R.id.btnFilter).setOnClickListener(v -> {
			//Show Alert Dialog
			final int origIdx = showMethod;
			String strArray[] = {Str.All_Documents[Global.LANG], Str.Customer_Documents[Global.LANG], Str.Inventory_Documents[Global.LANG]};
			new AlertDialog.Builder(mContext)
					.setTitle(Str.Select_filter[Global.LANG])
					.setSingleChoiceItems(new AlertSingleChoiceAdapter(mContext, strArray), showMethod, (dialog, nSelected) -> {
						showMethod = nSelected;
					})
					.setPositiveButton(Str.Ok[Global.LANG], (dialog, nSelected) -> refreshPage())
					.setNegativeButton(Str.Cancel[Global.LANG], (dialog, nSelected) -> showMethod = origIdx)
					.create()
					.show();
		});

//		loadData();
		refreshLanguage();
	}

	@Override
	public void loadData() {
		mDocumentList.clear();
		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		List<UOrderHInfo> list = dbAdapter.getUOrderHList(-1);
		Collections.sort(list, custNoComparator);
		int nOldCustNo = 0;
		CustomerInfo customerInfo = null;
		Map<Long, PrintDocumentInfo> uPrintDocumentInfo = new HashMap<>();
		List<PrintDocumentInfo> printDocumentInfoList = new ArrayList<>();

		for (UOrderHInfo uOrderHInfo : list) {
			PrintDocumentInfo documentInfo = new PrintDocumentInfo();
			documentInfo.strDocumentType = "Invoice";
			documentInfo.bInvoice = true;
			documentInfo.strFilePath = Global.PDF_LOCAL_DIR + uOrderHInfo.strDocNo + ".pdf";
			documentInfo.nTrxnNo = uOrderHInfo.nTrxnNo;
			documentInfo.strDocNo = uOrderHInfo.strDocNo;
			documentInfo.nVoidTrxnNo = uOrderHInfo.nVoidTrxnNo;
			documentInfo.uOrderHInfo = uOrderHInfo;
			documentInfo.nInvoiceTotal = uOrderHInfo.nSaleAmount + uOrderHInfo.nTaxAmount + uOrderHInfo.nHandlingFee + uOrderHInfo.nHandlingFeeTaxAmount;

			if (nOldCustNo != uOrderHInfo.nCustNo) {
				customerInfo = dbAdapter.getCustomerInfo(uOrderHInfo.nCustNo);
				nOldCustNo = uOrderHInfo.nCustNo;
			}

			boolean bCentsRound;
			if( Utils.getStringSetting(mContext, Global.PREF_ROUTE_CENTSROUND).trim().equals("1") && (customerInfo != null) &&
					(customerInfo.strPayType.trim().equals("1") || customerInfo.strPayType.trim().equals("2")))
			{
				bCentsRound = true;
			}
			else if(Utils.getStringSetting(mContext, Global.PREF_ROUTE_CENTSROUND).trim().equals("2"))
			{
				bCentsRound = true;
			}
			else
			{
				bCentsRound = false;
			}
			documentInfo.bCentsRound = bCentsRound;
			if( bCentsRound )
			{
				float fNeg = 1;
				if(documentInfo.nInvoiceTotal < 0)
				{
					fNeg = -1;
				}
				int nTotal = Math.round(Utils.getRoundedValue(documentInfo.nInvoiceTotal * fNeg) * Global.MULTIPLE_VALUE);
				int nOffSet = nTotal % 5000;
				int nInvoiceTotal = ((nTotal / 5000) + ((Math.abs(nOffSet) >= 2500) ? 1 : 0)) * 5000;
				nOffSet = nInvoiceTotal - nTotal;
				nInvoiceTotal *= fNeg;
				nOffSet *= fNeg;
				documentInfo.nInvoiceTotal = nInvoiceTotal / (Global.MULTIPLE_VALUE * 1f);
				documentInfo.nCentsRound = nOffSet / (Global.MULTIPLE_VALUE * 1f);
			}

			if (customerInfo != null) {
				documentInfo.strCustomerName = customerInfo.strName;
			}
			else {
				documentInfo.strCustomerName = null;
			}

			uPrintDocumentInfo.put(uOrderHInfo.nTrxnNo, documentInfo);
			printDocumentInfoList.add(documentInfo);
		}


		if(Utils.getStringSetting(mContext, Global.PREF_ALLOW_VOID).equals("2"))
		{
			for(PrintDocumentInfo document : printDocumentInfoList)
			{
				if(document.uOrderHInfo.nOrgTrxnNo != 0) continue;
				PrintDocumentInfo voidDocumentInfo = uPrintDocumentInfo.get(document.uOrderHInfo.nVoidTrxnNo);
				if(voidDocumentInfo != null)
				{
					document.strVoidDocNo = voidDocumentInfo.strDocNo;
					document.nVoidTrxnNo = voidDocumentInfo.nTrxnNo;
					document.nVoidInvoiceTotal = voidDocumentInfo.nInvoiceTotal;
					document.bVoidCentsRound = voidDocumentInfo.bCentsRound;
					document.nVoidCentsRound = voidDocumentInfo.nVoidCentsRound;
					document.strVoidFilePath = voidDocumentInfo.strFilePath;
				}
				mDocumentList.add(document);
			}
		}
		else
		{
			mDocumentList.addAll(printDocumentInfoList);
		}

		//Get the Print list from the database.
		List<PrintItem> printItemList = dbAdapter.getPrintList();

		Map<String, String> docTypeMap = new HashMap<>();
		for(PrintItem item : printItemList)
		{
			PrintDocumentInfo documentInfo = new PrintDocumentInfo();
			documentInfo.bInvoice = false;
			documentInfo.strDocumentType = item.strDocType;
			documentInfo.nTrxnNo = item.nTrxnNo;
			documentInfo.printItem = item;
			documentInfo.strDocNo = item.strDocNo;
			documentInfo.strFilePath = item.strFilePath;
			documentInfo.strCustomerName = item.strCustomerName;

			String strDocType = docTypeMap.get(documentInfo.strDocumentType);
			if(strDocType == null)
			{
				List<DescTypeInfo> descTypeInfoList = dbAdapter.getDescTypeListWithAlphaKey("DocType", documentInfo.strDocumentType);
				if(descTypeInfoList != null && descTypeInfoList.size() > 0)
				{
					strDocType = descTypeInfoList.get(0).strDescription;
				}

				if(TextUtils.isEmpty(strDocType))
				{
					strDocType = documentInfo.strDocumentType;
				}
				docTypeMap.put(documentInfo.strDocumentType, strDocType);
			}

			documentInfo.strDocumentType = strDocType;
			mDocumentList.add(documentInfo);
		}

		List<DescTypeInfo> descTypeInfoList = dbAdapter.getDescTypeListWithAlphaKey("DocType", mVoidType);
		if(descTypeInfoList != null && descTypeInfoList.size() > 0)
		{
			mVoidType = descTypeInfoList.get(0).strDescription;
		}

		dbAdapter.close();
		Collections.sort(mDocumentList, (lhs, rhs) -> rhs.strDocNo.compareTo(lhs.strDocNo));
		refreshPage();
	}

	@Override
	public void refreshPage() {
		List<PrintDocumentInfo> documentInfoList = new ArrayList<>();
		if(showMethod == 0)
		{
			documentInfoList.addAll(mDocumentList);
		}
		else if(showMethod == 1)
		{
			for(PrintDocumentInfo documentInfo : mDocumentList)
			{
				if(!TextUtils.isEmpty(documentInfo.strCustomerName))
				{
					documentInfoList.add(documentInfo);
				}
			}
		}
		else
		{
			for(PrintDocumentInfo documentInfo : mDocumentList)
			{
				if(TextUtils.isEmpty(documentInfo.strCustomerName))
				{
					documentInfoList.add(documentInfo);
				}
			}
		}
		mListAdapter.setData(documentInfoList);

		if (documentInfoList.size() > 0) {
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
		((TextView) mRootView.findViewById(R.id.lblTitle)).setText(Str.PrintDocuments[Global.LANG].toUpperCase());
		((TextView) mRootView.findViewById(R.id.txtTrip)).setText(Str.Trip[Global.LANG] + " # " + Global.gTripNumber);
		((Button) mRootView.findViewById(R.id.btnCompleted)).setText(Str.Completed[Global.LANG]);
		mTxtNoData.setText(Str.No_data_has_been_found[Global.LANG]);
		loadData();
	}

	@Override
	public void receiveBroadcast(String strAction, Intent intent) {
	}

	private void outputPrinter() {
		if (BluetoothAdapter.getDefaultAdapter().isEnabled() == false) {
			try {
				final MessageDialog msgDialog = new MessageDialog(mContext);
				msgDialog.setCancelable(false);
				msgDialog.mStrMessage = Str.TurnOnBluetooth_alert[Global.LANG];
				msgDialog.mStrLeft = Str.Cancel[Global.LANG];
				msgDialog.mStrRight = Str.Retry[Global.LANG];
				msgDialog.setOnDismissListener(dialog -> {
					if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
						outputPrinter();
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
		mStrMacAddress = Utils.getStringSetting(mContext, Global.PREF_PRINTER_MAC_ADDRESS);
		if (mStrMacAddress.length() == 0) {
			discoveryPrinters();
		} else {
			new PrintPdfTask().execute();
		}
	}

	private void discoveryPrinters() {
		showProgressDialog(Str.DiscoveryingPrinters[Global.LANG]);
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

				 if( result[0] == false )
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

		String strArray[] = strDevices[0].split("\n");
		if (strArray.length > 0) {
			mStrMacAddress = strArray[0];
		}

		try {
			new AlertDialog.Builder(mContext)
					.setTitle(Str.SelectPrinter[Global.LANG])
					.setSingleChoiceItems(new AlertSingleChoiceAdapter(mContext, strDevices), 0, (dialogInterface, i) -> {
						String strArray1[] = strDevices[i].split("\n");
						if (strArray1.length > 0) {
							mStrMacAddress = strArray1[0];
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
					if(mSelectedInfo != null)
					{
						DbAdapter dbAdapter = new DbAdapter(mContext);
						dbAdapter.open();
						mSelectedInfo.nPrintedFlag = 1;
						mSelectedInfo.bUploaded = false;
						dbAdapter.updateUOrderHInfo(mSelectedInfo);
						dbAdapter.close();
					}
					goBack();
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
						}
					});
					msgDialog.show();
				}catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}, mStrMacAddress, mSelectedFilePath);
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
					if (mSelectedInfo != null) {
						DbAdapter dbAdapter = new DbAdapter(mContext);
						dbAdapter.open();
						mSelectedInfo.nPrintedFlag = 1;
						mSelectedInfo.bUploaded = false;
						dbAdapter.updateUOrderHInfo(mSelectedInfo);
						dbAdapter.close();
					}
					goBack();
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
						}
					});
					msgDialog.show();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, mStrMacAddress, mSelectedFilePath);
	}

	public boolean m_bClicked = false;
	@Override
	public void onClick(View v) {
		if( m_bClicked ) return;
		m_bClicked = true;
		switch (v.getId()) {
			case R.id.btnCompleted:
				goBack();
				break;
		}
		m_bClicked = false;
	}

	private class PrintPdfTask extends AsyncTask<Void, String, String> {
		int nMessageType = 0;
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			showProgressDialog(Str.Printing[Global.LANG]);
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
							String strPdfFilePath = mSelectedFilePath;
							printer.sendFileContents(strPdfFilePath);
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
					if(mSelectedInfo != null)
					{
						DbAdapter dbAdapter = new DbAdapter(mContext);
						dbAdapter.open();
						mSelectedInfo.nPrintedFlag = 1;
						mSelectedInfo.bUploaded = false;
						dbAdapter.updateUOrderHInfo(mSelectedInfo);
						dbAdapter.close();
					}
					goBack();
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

	boolean bNeedBack = false;
	boolean bPaused = true;

	private void goBack()
	{
		if(bPaused)
		{
			bNeedBack = true;
		}
		else
		{
			mContext.onBackPressed();
		}
	}

	private class ListAdapter extends BaseAdapter {

		private List<PrintDocumentInfo> documentInfoList = new ArrayList<>();
		private class ViewHolder {
			public TextView txtCustomerName, txtDocNo, txtInvoiceTotal, txtCentsRounding, txtDocType;
		}

		public void setData(List<PrintDocumentInfo> documentInfoList)
		{
			this.documentInfoList.clear();
			this.documentInfoList.addAll(documentInfoList);
		}

		public int getCount() {
			return documentInfoList.size();
		}

		public PrintDocumentInfo getItem(int position) {
			if(position < documentInfoList.size())
			{
				return documentInfoList.get(position);
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
			final PrintDocumentInfo info = getItem(position);
			if(info == null) return null;
			if (convertView == null) {
				view = mContext.getLayoutInflater().inflate(R.layout.row_print_document, parent, false);
				holder = new ViewHolder();
				holder.txtDocType = view.findViewById(R.id.txtType);
				holder.txtCustomerName = view.findViewById(R.id.txtCustomerName);
				holder.txtDocNo = view.findViewById(R.id.txtDocNo);
				holder.txtInvoiceTotal = view.findViewById(R.id.txtInvoiceTotal);
				holder.txtCentsRounding = view.findViewById(R.id.txtCentsRounding);
				view.setTag(holder);
			} else {
				holder = (ViewHolder) view.getTag();
			}

			holder.txtCustomerName.setText(info.strCustomerName);

			holder.txtDocType.setText(info.strDocumentType);
			holder.txtDocNo.setText(info.strDocNo);

			if(info.bInvoice)
			{
				String strText = Utils.getFormattedPrice(mContext, info.nInvoiceTotal);
				holder.txtInvoiceTotal.setText(strText);

				if(info.bCentsRound)
				{
					strText = Utils.getFormattedPrice(mContext, info.nCentsRound);
					holder.txtCentsRounding.setText(strText);
					holder.txtCentsRounding.setVisibility(View.GONE);
				}
				else
				{
					holder.txtCentsRounding.setVisibility(View.GONE);
				}

				if(info.nVoidTrxnNo != 0)
				{
					view.findViewById(R.id.layoutVoid).setVisibility(View.VISIBLE);
					((TextView)view.findViewById(R.id.txtVoidDocNo)).setText(info.strVoidDocNo);
					((TextView)view.findViewById(R.id.txtVoidType)).setText(mVoidType);
					strText = Utils.getFormattedPrice(mContext, info.nVoidInvoiceTotal);
					((TextView)view.findViewById(R.id.txtVoidInvoiceTotal)).setText(strText);
					if(info.bVoidCentsRound)
					{
						strText = Utils.getFormattedPrice(mContext, info.nVoidCentsRound);
						((TextView)view.findViewById(R.id.txtVoidCentsRounding)).setText(strText);
						view.findViewById(R.id.txtVoidCentsRounding).setVisibility(View.GONE);
					}
					else
					{
						view.findViewById(R.id.txtVoidCentsRounding).setVisibility(View.GONE);
					}
				}
				else
				{
					view.findViewById(R.id.layoutVoid).setVisibility(View.GONE);
				}
			}
			else
			{
				holder.txtCentsRounding.setVisibility(View.GONE);
				view.findViewById(R.id.layoutVoid).setVisibility(View.GONE);
				if(info.printItem.strDocType.equals("PAY"))
				{
					String strPrice =  String.format(Locale.ENGLISH, "%s" + "%." + Utils.getIntSetting(mContext, Global.PREF_DECIMAL) + "f", Global.gCurrencySymbol, info.printItem.nInvoiceAmount);
					holder.txtInvoiceTotal.setText(strPrice);
				}
				else
				{
					holder.txtInvoiceTotal.setText("");
				}
			}

			view.setOnClickListener(view1 -> {
				mSelectedInfo = info.uOrderHInfo;
				ReprintActionDialog dialog = new ReprintActionDialog(mContext);
				dialog.mInfo = mSelectedInfo;
				dialog.printItem = info.printItem;

				dialog.setOnDismissListener(dialogInterface -> {
					if(dialog.mReturnCode == Global.ACTION_PRINT)
					{
						mSelectedFilePath = info.strFilePath;
						if(!Utils.isFileExist(mSelectedFilePath))
						{
//							Toast.makeText(mContext, Str.FileNotExist[Global.LANG], Toast.LENGTH_LONG).show();
							try {
								final MessageDialog msgDialog = new MessageDialog(mContext);
								msgDialog.setCancelable(false);
								msgDialog.mStrMessage = mSelectedFilePath + " not Exists";
								msgDialog.mStrMiddle = Str.Ok[Global.LANG];
								msgDialog.show();
							}catch (Exception e1)
							{
								e1.printStackTrace();
							}
							return;
						}
						try
						{
							if( Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 1)
							{
								final MessageDialog msgDialog = new MessageDialog(mContext);
								msgDialog.setCancelable(false);
								msgDialog.mStrMessage = Str.Print_msg[Global.LANG];
								msgDialog.mStrLeft = Str.No[Global.LANG];
								msgDialog.mStrRight = Str.Yes[Global.LANG];
								msgDialog.setOnDismissListener(dialog1 -> {
									if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
										outputPrinter();
									}
								});
								msgDialog.show();
							}
							else if(Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 2)
							{
								try {
									final MessageDialog msgDialog = new MessageDialog(mContext);
									msgDialog.setCancelable(false);
									msgDialog.mStrMessage = Str.Print_msg[Global.LANG];
									msgDialog.mStrLeft = Str.No[Global.LANG];
									msgDialog.mStrRight = Str.Yes[Global.LANG];
									msgDialog.setOnDismissListener(dialog1 -> {
										if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
											outputHoneywellPrinter();
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
									msgDialog.setOnDismissListener(dialog1 -> {
										if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
											outputHoneywellLinePrinter();
										}
									});
									msgDialog.show();
								}catch (Exception e)
								{
									e.printStackTrace();
								}
							}
						}catch (Exception e)
						{
							e.printStackTrace();
						}
					}
					else if(dialog.mReturnCode == Global.ACTION_VIEW)
					{
						String strPdfFilePath = info.strFilePath;
						File file = new File(strPdfFilePath);
						if(!Utils.isFileExist(strPdfFilePath))
						{
							//Toast.makeText(mContext, Str.FileNotExist[Global.LANG], Toast.LENGTH_LONG).show();
							try {
								final MessageDialog msgDialog = new MessageDialog(mContext);
								msgDialog.setCancelable(false);
								msgDialog.mStrMessage = strPdfFilePath + " not Exists";
								msgDialog.mStrMiddle = Str.Ok[Global.LANG];
								msgDialog.show();
							}catch (Exception e1)
							{
								e1.printStackTrace();
							}
							return;
						}
						try {
							if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
								Uri photoURI = FileProvider.getUriForFile(mContext, "dsd.dsdvansales.fileprovider", file);

								Intent intent = new Intent(Intent.ACTION_VIEW);
								intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
								intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
								intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
								intent.setDataAndType(photoURI, "application/pdf");
								startActivity(intent);
							} else {
								Intent intent = new Intent(Intent.ACTION_VIEW);
								intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
								intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
								intent.setDataAndType(Uri.fromFile(file), "application/pdf");
								startActivity(intent);
							}
						} catch (Exception e) {
							try {
								final MessageDialog msgDialog = new MessageDialog(mContext);
								msgDialog.setCancelable(false);
								msgDialog.mStrMessage = Str.No_Viewer_Available[Global.LANG];
								msgDialog.mStrMiddle = Str.Ok[Global.LANG];
								msgDialog.setOnDismissListener(dialog12 -> {

								});
								msgDialog.show();
							}catch (Exception e1)
							{
								e1.printStackTrace();
							}
						}
					}
					else if(dialog.mReturnCode == Global.ACTION_VIEW_VOID)
					{
						if(Utils.getStringSetting(mContext, Global.PREF_ALLOW_VOID).equals("2"))
						{
							String strPdfFilePath = info.strVoidFilePath;
							File file = new File(strPdfFilePath);
							if(!Utils.isFileExist(strPdfFilePath))
							{
//								Toast.makeText(mContext, Str.FileNotExist[Global.LANG], Toast.LENGTH_LONG).show();
								try {
									final MessageDialog msgDialog = new MessageDialog(mContext);
									msgDialog.setCancelable(false);
									msgDialog.mStrMessage = strPdfFilePath + " not Exists";
									msgDialog.mStrMiddle = Str.Ok[Global.LANG];
									msgDialog.show();
								}catch (Exception e1)
								{
									e1.printStackTrace();
								}
								return;
							}
							try {
								if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
									Uri photoURI = FileProvider.getUriForFile(mContext, "dsd.dsdvansales.fileprovider", file);

									Intent intent = new Intent(Intent.ACTION_VIEW);
									intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
									intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
									intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
									intent.setDataAndType(photoURI, "application/pdf");
									startActivity(intent);
								} else {
									Intent intent = new Intent(Intent.ACTION_VIEW);
									intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
									intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
									intent.setDataAndType(Uri.fromFile(file), "application/pdf");
									startActivity(intent);
								}
							} catch (Exception e) {
								try {
									final MessageDialog msgDialog = new MessageDialog(mContext);
									msgDialog.setCancelable(false);
									msgDialog.mStrMessage = Str.No_Viewer_Available[Global.LANG];
									msgDialog.mStrMiddle = Str.Ok[Global.LANG];
									msgDialog.setOnDismissListener(dialog12 -> {

									});
									msgDialog.show();
								}catch (Exception e1)
								{
									e1.printStackTrace();
								}
							}
						}
					}
					else if(dialog.mReturnCode == Global.ACTION_VOID)
					{
						if(Utils.getStringSetting(mContext, Global.PREF_ALLOW_VOID).equals("2"))
						{
							if(mSelectedInfo != null && mSelectedInfo.nVoidTrxnNo == 0)
							{
								DbAdapter dbAdapter = new DbAdapter(mContext);
								dbAdapter.open();
								Global.gCurCustomerInfo = dbAdapter.getCustomerInfo(mSelectedInfo.nCustNo, mSelectedInfo.nChainNo);
								dbAdapter.close();
								Global.startFragment = PrintDocumentsFragment.this;
								Utils.doVoidInvoice(mContext, mSelectedInfo);
							}
							else
							{
								DbAdapter dbAdapter = new DbAdapter(mContext);
								dbAdapter.open();
								mSelectedInfo = dbAdapter.getUOrderHInfo(mSelectedInfo.nVoidTrxnNo);
								dbAdapter.close();

								mSelectedFilePath = info.strVoidFilePath;
								if(!Utils.isFileExist(mSelectedFilePath))
								{
//									Toast.makeText(mContext, Str.FileNotExist[Global.LANG], Toast.LENGTH_LONG).show();
									try {
										final MessageDialog msgDialog = new MessageDialog(mContext);
										msgDialog.setCancelable(false);
										msgDialog.mStrMessage = mSelectedFilePath + " not Exists";
										msgDialog.mStrMiddle = Str.Ok[Global.LANG];
										msgDialog.show();
									}catch (Exception e1)
									{
										e1.printStackTrace();
									}
									return;
								}
								if(mSelectedInfo == null) return;
								try
								{
									if(Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 1)
									{
										final MessageDialog msgDialog = new MessageDialog(mContext);
										msgDialog.setCancelable(false);
										msgDialog.mStrMessage = Str.Print_msg[Global.LANG];
										msgDialog.mStrLeft = Str.No[Global.LANG];
										msgDialog.mStrRight = Str.Yes[Global.LANG];
										msgDialog.setOnDismissListener(dialog12 -> {
											if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
												outputPrinter();
											}
										});
										msgDialog.show();
									}
									else if(Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 2)
									{
										try {
											final MessageDialog msgDialog = new MessageDialog(mContext);
											msgDialog.setCancelable(false);
											msgDialog.mStrMessage = Str.Print_msg[Global.LANG];
											msgDialog.mStrLeft = Str.No[Global.LANG];
											msgDialog.mStrRight = Str.Yes[Global.LANG];
											msgDialog.setOnDismissListener(dialog1 -> {
												if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
													outputHoneywellPrinter();
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
											msgDialog.setOnDismissListener(dialog1 -> {
												if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
													outputHoneywellLinePrinter();
												}
											});
											msgDialog.show();
										}catch (Exception e)
										{
											e.printStackTrace();
										}
									}

								}catch (Exception e)
								{
									e.printStackTrace();
								}
							}
						}
					}
				});
				dialog.show();

			});
			return view;
		}
	}

	private void outputHoneywellPrinter() {
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

	// Ascending
	public final static Comparator<UOrderHInfo> custNoComparator = (object1, object2) -> object1.nCustNo < object2.nCustNo ? -1 : object1.nCustNo > object2.nCustNo ? 1:0;
}