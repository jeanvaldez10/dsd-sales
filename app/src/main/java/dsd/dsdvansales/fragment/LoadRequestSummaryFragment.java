package dsd.dsdvansales.fragment;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
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

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import dsd.dsdvansales.R;
import dsd.dsdvansales.activities.SignatureActivity;
import dsd.dsdvansales.db.DbAdapter;
import dsd.dsdvansales.dialogs.InputDialog;
import dsd.dsdvansales.dialogs.MessageDialog;
import dsd.dsdvansales.global.BixolonPrinter;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.HoneywellLinePrinter;
import dsd.dsdvansales.global.HoneywellPrinter;
import dsd.dsdvansales.global.OTPUtils;
import dsd.dsdvansales.global.PrintEngine;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.model.DescTypeInfo;
import dsd.dsdvansales.model.PrintItem;
import dsd.dsdvansales.model.ProdDetlInfo;
import dsd.dsdvansales.model.TransactionInfo;
import dsd.dsdvansales.model.UInvenDInfo;
import dsd.dsdvansales.model.UStockDInfo;
import dsd.dsdvansales.model.UStockHInfo;
import dsd.dsdvansales.model.UTransactionsInfo;
import dsd.dsdvansales.views.AlertSingleChoiceAdapter;

public class LoadRequestSummaryFragment extends BaseFragment implements View.OnClickListener {

	private Font fontBlack7Bold;
	private Font fontBlack10Bold;
	private Font fontBlack7Normal;

	private ListView mListView = null;
	private ListAdapter mListAdapter = null;

	private String mStrPdfFileName = "";
	private long mPdfTrxnNo = 0;
	private Date mTrxnDate = new Date();

	private Connection printerConnection = null;
	private String mStrMacAddress = "";
	private String mStrFriendlyName = "";
	private boolean mIsCreatedPDF = false;
	private String mStrSignatureFilePath = "";
	private long mDate = System.currentTimeMillis();
	private boolean bShowCase = true;

	private String mCartonDescription = "";
	private boolean bLastProcess = false;

	private boolean isCompleteProcessPossible = true;

	private PrintEngine mHoneywellPrintEngine = null;
	private String mHoneywellTemplatePath = "";

	private boolean bPassedApprovalKey = false;

	private ArrayList<UInvenDInfo> mAdditionalContainers = new ArrayList<>();
	private Map<String, ProdDetlInfo> mProdDetlItemMap = new HashMap<>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootView = inflater.inflate(R.layout.fragment_load_request_summary, container, false);
		commonInit();
		loadFont();
		return mRootView;
	}

	private void loadFont()
	{
		fontBlack7Bold		= Utils.getFont(Utils.getPrintFontFilePath(), Utils.getPrintEncoding(), Utils.getPrintEmbeded(), 7, Font.BOLD, BaseColor.BLACK);
		fontBlack10Bold		= Utils.getFont(Utils.getPrintFontFilePath(), Utils.getPrintEncoding(), Utils.getPrintEmbeded(), 10, Font.BOLD, BaseColor.BLACK);
		fontBlack7Normal	= Utils.getFont(Utils.getPrintFontFilePath(), Utils.getPrintEncoding(), Utils.getPrintEmbeded(), 7, Font.NORMAL, BaseColor.BLACK);
	}

	@Override
	public void onResume() {
		super.onResume();
		Global.gFragIdx = Global.FRAG_LOAD_REQUEST_SUMMARY;
		bPaused = false;

		if(bNeedBack)
		{
			new Handler(Looper.getMainLooper()).post(new Runnable() {
				@Override
				public void run() {
					Global.gLoadReqeustInfo.clear();
					mContext.onBackPressed();
					mContext.onBackPressed();
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

		Bundle bundle = getArguments();
		boolean bFromVehicleInventory = false;
		if( bundle != null )
		{
			try
			{
				bFromVehicleInventory = bundle.getBoolean(Global.EXTRA_FROM, false);
			}catch(Exception e)
			{
				e.printStackTrace();
			}
		}

		if(Utils.getStringSetting(mContext, Global.PREF_LOAD_REQUEST_EOT).equals("1") || Utils.getStringSetting(mContext, Global.PREF_LOAD_REQUEST_EOT).equals("2"))
		{
			isCompleteProcessPossible = !bFromVehicleInventory || (!Utils.getStringSetting(mContext, Global.PREF_LOAD_REQUEST_EOT).equals("1") && !Utils.getStringSetting(mContext, Global.PREF_LOAD_REQUEST_EOT).equals("2"));
		}

		bShowCase = Utils.getStringSetting(mContext, Global.PREF_INVENTORYUOM).equals("U") == false;

		mRootView.findViewById(R.id.btnPickDate).setOnClickListener(this);
		mRootView.findViewById(R.id.btnSignature).setOnClickListener(this);
		mRootView.findViewById(R.id.btnCompleted).setOnClickListener(this);
		mRootView.findViewById(R.id.btnBack).setOnClickListener(this);
		mRootView.findViewById(R.id.btnKey).setOnClickListener(this);

		mListView = (ListView) mRootView.findViewById(R.id.containerListView);
		mListAdapter = new ListAdapter();
		mListView.setAdapter(mListAdapter);

		Global.gLoadStartedTime = new SimpleDateFormat("HH:mm:ss").format(new Date());

		if(!isCompleteProcessPossible)
		{
			mRootView.findViewById(R.id.layoutForEndingInventory).setVisibility(View.GONE);
		}
		else {
			mRootView.findViewById(R.id.layoutForEndingInventory).setVisibility(View.VISIBLE);
		}

		loadData();
		refreshLanguage();
		showTimes();
	}

	private void showTimes() {
		Date date = new Date(mDate);
		((TextView) mRootView.findViewById(R.id.txtDate)).setText(new SimpleDateFormat("EEE " + Utils.getDateFormat(mContext, "dd/MM/yyyy")).format(date));
	}


	@Override
	public void loadData() {
		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		dbAdapter.getProdDetlList(mProdDetlItemMap, null);
		List<DescTypeInfo> descTypeInfoList = dbAdapter.getDescTypeList(Global.UNITOFMEASURE_DESC_TYPE_ID);
		for(DescTypeInfo descTypeInfo : descTypeInfoList)
		{
			if( descTypeInfo.strAlphaKey.toUpperCase().equals("Z") )
			{
				if(TextUtils.isEmpty(mCartonDescription))
					mCartonDescription = descTypeInfo.strDescription;
				else
					mCartonDescription += "\n" + descTypeInfo.strDescription;
			}
		}
		dbAdapter.close();

		// Load container details for each load items
		dbAdapter.open();
		List<DescTypeInfo> packSizeDescTypeInfoList = dbAdapter.getDescTypeList(Global.PACK_SIZE_DESC_TYPE_ID);
		dbAdapter.close();

		if (Utils.getIntSetting(mContext, Global.PREF_LOAD_CONTAINERS) == 2 && packSizeDescTypeInfoList.size() > 0) {
			mAdditionalContainers.clear();
			for (UInvenDInfo info : Global.gLoadReqeustInfo) {
				ProdDetlInfo prodDetlInfo = mProdDetlItemMap.get(info.strItemNo);
				if (prodDetlInfo == null) {
					continue;
				}
				String strPackSize = prodDetlInfo.strPackSize;
				if (strPackSize == null || strPackSize.isEmpty()) {
					continue;
				}

				// check DESCTYPE for container for this item
				DescTypeInfo packSizeDescTypeInfo = null;
				for (DescTypeInfo descTypeInfo: packSizeDescTypeInfoList) {
					if (descTypeInfo.strAlphaKey.equals(strPackSize)) {
						packSizeDescTypeInfo = descTypeInfo;
						break;
					}
				}
				if (packSizeDescTypeInfo == null) {
					continue;
				}

				String strContainerItemNo = packSizeDescTypeInfo.strValue1;
				if (strContainerItemNo == null || strContainerItemNo.isEmpty()) {
					continue;
				}

				ProdDetlInfo containerProdDetlInfo = mProdDetlItemMap.get(strContainerItemNo);
				if (containerProdDetlInfo == null) {
					continue;
				}

				int nQuantity = info.nStock;
				int nCases = (int)Math.ceil((double)nQuantity / prodDetlInfo.prodLocnInfo.nCaseFactor);

				if (nCases <= 0) {
					continue;
				}

				// insert or update the additional containers
				int containerIdx = -1;
				for (int i = 0; i < mAdditionalContainers.size(); i++) {
					if (mAdditionalContainers.get(i).strItemNo.equals(strContainerItemNo)) {
						containerIdx = i;
						mAdditionalContainers.get(i).nStock += nCases;
						break;
					}
				}
				if (containerIdx == -1) {
					UInvenDInfo uInvenDInfo = new UInvenDInfo();
					uInvenDInfo.bShow = true;
					uInvenDInfo.nType = Global.INVENTORY_CONTAINERS;
					uInvenDInfo.strShortDesc = containerProdDetlInfo.strShortDesc;
					uInvenDInfo.strDescription = containerProdDetlInfo.strDescription;
					uInvenDInfo.strUOM = containerProdDetlInfo.prodLocnInfo.strCaseUOM.length() > 0 ? containerProdDetlInfo.prodLocnInfo.strCaseUOM : containerProdDetlInfo.prodLocnInfo.strUnitUOM;
					uInvenDInfo.strItemNo = strContainerItemNo;
					uInvenDInfo.nStock = nCases;
					uInvenDInfo.nSCWType = Global.INVENTORY_CONTAINERS;
					mAdditionalContainers.add(uInvenDInfo);
				}
			}
		}

		refreshPage();
	}

	@Override
	public void refreshPage() {
		String strLoadRequestEOT = Utils.getStringSetting(mContext, Global.PREF_LOAD_REQUEST_EOT);
		if (strLoadRequestEOT.equals("2")) {
			String strMFASecret = Utils.getStringSetting(mContext, Global.PREF_MFA_SECRET);
			String strApprovalKey = Utils.getStringSetting(mContext, Global.PREF_LOADREQUEST_APPROVAL_KEY);
			if (strMFASecret.length() > 0 || strApprovalKey.length() > 0) {
				if (bPassedApprovalKey) {
					mRootView.findViewById(R.id.btnKey).setVisibility(View.INVISIBLE);
				}
				else {
					mRootView.findViewById(R.id.btnKey).setVisibility(View.VISIBLE);
				}
				mRootView.findViewById(R.id.btnCompleted).setEnabled(bPassedApprovalKey);
			}
			else {
				mRootView.findViewById(R.id.btnKey).setVisibility(View.INVISIBLE);
			}
		}
		else {
			mRootView.findViewById(R.id.btnKey).setVisibility(View.INVISIBLE);
		}
		// update the containers
		mListAdapter.setListData(mAdditionalContainers);
		mListAdapter.notifyDataSetChanged();
	}

	private void processKeyButtonPressed() {
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
						String strMFASecret = Utils.getStringSetting(mContext, Global.PREF_MFA_SECRET);
						String strPasscode = "";
						if (strMFASecret.length() > 0) {
							strPasscode = OTPUtils.generateTOTP(strMFASecret);
						}
						else {
							strPasscode = Utils.getStringSetting(mContext, Global.PREF_LOADREQUEST_APPROVAL_KEY);
						}

						if (strPasscode == null || strPasscode.isEmpty()) {
							strPasscode = Utils.getStringSetting(mContext, Global.PREF_LOADREQUEST_APPROVAL_KEY);
						}
						//String otpCode = OTPUtils.generateTOTP(strMFASecret);
						//String strApprovalKey = Utils.getStringSetting(mContext, Global.PREF_LOADREQUEST_APPROVAL_KEY);
						if (inputDlg.mStrInputed.equals(strPasscode)) {
							bPassedApprovalKey = true;
							refreshPage();
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

	private Map<String, Integer> mCaseMap = new HashMap<>();
	private int getCaseValue(String strItemNo)
	{
		if( mCaseMap.size() == 0 )
		{
			DbAdapter adapter = new DbAdapter(mContext);
			adapter.open();
			adapter.getCaseValue(mCaseMap);
			adapter.close();
		}
		int nCase = 1;
		if( mCaseMap.get(strItemNo) != null )
			nCase = mCaseMap.get(strItemNo);

		if( nCase < 1 ) nCase = 1;
		return nCase;
	}
	private Map<String, Integer> mOrderSequenceMap = new HashMap<>();
	private int getSequenceValue(String strItemNo)
	{
		if( mOrderSequenceMap.size() == 0 )
		{
			DbAdapter adapter = new DbAdapter(mContext);
			adapter.open();
			adapter.getOrderSeqValue(mOrderSequenceMap);
			adapter.close();
		}
		int nCase = 0;
		if( mOrderSequenceMap.get(strItemNo) != null )
			nCase = mOrderSequenceMap.get(strItemNo);
		return nCase;
	}

	@Override
	public void refreshLanguage() {
		((TextView) mRootView.findViewById(R.id.lblTitle)).setText(Str.LoadRequest[Global.LANG].toUpperCase());
		((TextView) mRootView.findViewById(R.id.txtLoadStarted)).setText(Str.LoadStartedAt[Global.LANG] + " " + Global.gLoadStartedTime);
		((TextView) mRootView.findViewById(R.id.lblYouHaveLoaded)).setText(Str.YouHaveRequested[Global.LANG]);
		((Button) mRootView.findViewById(R.id.btnSignature)).setText(Str.Signature[Global.LANG]);
		((Button) mRootView.findViewById(R.id.btnCompleted)).setText(Str.Completed[Global.LANG]);

		int nCartons = 0;
		int nUnits = 0;
		int nContainers = 0;
		double dValue = 0;
		double dOrderPrice = 0;
		if (Global.gLoadReqeustInfo.size() != 0) {
			for (UInvenDInfo info : Global.gLoadReqeustInfo) {
				float driverPrice = Utils.getProductDriverPrice(mContext, info.strItemNo);
				if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
				{
					dValue += info.dStock;
					dOrderPrice += driverPrice*info.dStock;
				}
				else
				{
					if(bShowCase)
					{
						int nCaseValue = getCaseValue(info.strItemNo);
						int nCase = info.nStock / nCaseValue;
						int nQty = info.nStock % nCaseValue;
						nCartons += nCase;
						nUnits += nQty;
					}
					else
					{
						int nQty = info.nStock;
						nUnits += nQty;
					}
					dOrderPrice += driverPrice*info.nStock;
				}

			}
		}
		String strLoaded = "";

		if(nCartons > 0)
		{
			strLoaded += String.format(Locale.ENGLISH, "%d %s", nCartons, Str.Baskets[Global.LANG]);
		}
		if(nUnits > 0)
		{
			if(strLoaded.length() > 0) strLoaded += "\n";
			String strCarton = "";
			if( TextUtils.isEmpty(mCartonDescription) )
			{
				strCarton = Str.units[Global.LANG];
			}
			else
			{
				strCarton = mCartonDescription;
			}
			if(nUnits > 1)
			{
				strLoaded += String.format(Locale.ENGLISH, "%d %ss", nUnits, strCarton);
			}
			else
			{
				strLoaded += String.format(Locale.ENGLISH, "%d %s", nUnits, strCarton);
			}
		}
		if(nContainers > 0)
		{
			if(strLoaded.length() > 0) strLoaded += "\n";
			if(nContainers > 1)
			{
				strLoaded += String.format(Locale.ENGLISH, "%d %ss", nContainers, Str.containers[Global.LANG]);
			}
			else
			{
				strLoaded += String.format(Locale.ENGLISH, "%d %s", nContainers, Str.containers[Global.LANG]);
			}
		}
		if(dValue > 0)
		{
			if(strLoaded.length() > 0) strLoaded += "\n";
			strLoaded += String.format(Locale.ENGLISH, "%.2f Kg", dValue);
		}
		((TextView) mRootView.findViewById(R.id.txtLoaded)).setText(strLoaded);

		String strOrderValue = "";
		strOrderValue = String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(mContext, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(dOrderPrice));

		((TextView) mRootView.findViewById(R.id.txtOrderValue)).setText(strOrderValue);
	}

	@Override
	public void receiveBroadcast(String strAction, Intent intent) {
		if (strAction.equals(Global.ACTION_CAPTURE_SIGNATURE) == true) {
			if (Global.gFragIdx == Global.FRAG_LOAD_REQUEST_SUMMARY) {
				mStrSignatureFilePath = intent.getStringExtra(Global.EXTRA_SIGNATURE_PATH);
				Utils.printDebug(mContext, Str.Saved_successfully[Global.LANG]);
			}
		}
	}

	private PdfPTable createHeader() {
		PdfPTable table = new PdfPTable(1);
		PdfPCell cell;
		Phrase phrase;
		try {
			table.setWidths(new int[]{1});
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
			cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
			cell.setBorder(Rectangle.NO_BORDER);
			table.addCell(cell);

			phrase = new Phrase("Load Request", fontBlack10Bold);
			cell = new PdfPCell(phrase);
			cell.setPaddingTop(5);
			cell.setColspan(2);
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell.setBorder(Rectangle.NO_BORDER);
			table.addCell(cell);

			phrase = new Phrase(String.format(Locale.ENGLISH, "Date	   %s", new SimpleDateFormat(Utils.getDateFormat(mContext, "dd/MM/yyyy") + " hh:mm aa").format(mTrxnDate)), fontBlack7Normal);
			cell = new PdfPCell(phrase);
			cell.setPaddingTop(5);
			cell.setBorder(Rectangle.NO_BORDER);
			table.addCell(cell);

			phrase = new Phrase(String.format(Locale.ENGLISH, "Trip#	  %s", Global.gTripNumber), fontBlack7Normal);
			table.addCell(phrase);

			phrase = new Phrase(String.format(Locale.ENGLISH, "Location # %s", Utils.getStringSetting(mContext, Global.PREF_DEF_LOCN_NO)), fontBlack7Normal);
			table.addCell(phrase);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return table;
	}

	private PdfPTable createBody() {
		PdfPTable table = new PdfPTable(3);
		PdfPCell cell;
		Phrase phrase;
		try {
			table.setWidths(new int[]{3, 4, 2});
			table.getDefaultCell().setBorder(Rectangle.NO_BORDER);

			phrase = new Phrase("Item", fontBlack7Normal);
			cell = new PdfPCell(phrase);
			cell.setColspan(2);
			cell.setBorder(Rectangle.BOTTOM);
			table.addCell(cell);
			phrase = new Phrase("Qty", fontBlack7Normal);
			cell = new PdfPCell(phrase);
			cell.setBorder(Rectangle.BOTTOM);
			table.addCell(cell);

			int nTotal = 0;
			double dTotal = 0;
			for (UInvenDInfo info : Global.gLoadReqeustInfo) {
				if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
				{
					if ((info.nSCWType == Global.INVENTORY_TRUCK || info.nSCWType == Global.WEIGHT_ITEM) && info.dStock > 0) {
						phrase = new Phrase(Utils.getDisplayItemCode(mContext, info.strItemNo), fontBlack7Normal);
						cell = new PdfPCell(phrase);
						cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
						cell.setBorder(Rectangle.NO_BORDER);
						table.addCell(cell);
						phrase = new Phrase(info.strShortDesc, fontBlack7Normal);
						cell = new PdfPCell(phrase);
						cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
						cell.setBorder(Rectangle.NO_BORDER);
						table.addCell(cell);
						phrase = new Phrase(String.format(Locale.ENGLISH, "%.2f", info.dStock), fontBlack7Normal);
						cell = new PdfPCell(phrase);
						cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
						cell.setBorder(Rectangle.NO_BORDER);
						table.addCell(cell);
						dTotal += info.dStock;
					}
				}
				else
				{
					if ((info.nSCWType == Global.INVENTORY_TRUCK || info.nSCWType == Global.WEIGHT_ITEM) && info.nStock > 0) {
						phrase = new Phrase(Utils.getDisplayItemCode(mContext, info.strItemNo), fontBlack7Normal);
						cell = new PdfPCell(phrase);
						cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
						cell.setBorder(Rectangle.NO_BORDER);
						table.addCell(cell);
						phrase = new Phrase(info.strShortDesc, fontBlack7Normal);
						cell = new PdfPCell(phrase);
						cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
						cell.setBorder(Rectangle.NO_BORDER);
						table.addCell(cell);
						phrase = new Phrase(String.format(Locale.ENGLISH, "%d", info.nStock), fontBlack7Normal);
						cell = new PdfPCell(phrase);
						cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
						cell.setBorder(Rectangle.NO_BORDER);
						table.addCell(cell);
						nTotal += info.nStock;
					}
				}

			}

			phrase = new Phrase("Total", fontBlack7Bold);
			cell = new PdfPCell(phrase);
			cell.setColspan(2);
			cell.setBorder(Rectangle.TOP);
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			table.addCell(cell);
			if(dTotal > 0 && nTotal > 0)
			{
				phrase = new Phrase(String.format(Locale.ENGLISH, "%d\n%.2f Kg", nTotal, dTotal), fontBlack7Normal);
			}
			else if( dTotal > 0 )
			{
				phrase = new Phrase(String.format(Locale.ENGLISH, "%.2f Kg", dTotal), fontBlack7Normal);
			}
			else
			{
				phrase = new Phrase(String.format(Locale.ENGLISH, "%d", nTotal), fontBlack7Normal);
			}
			cell = new PdfPCell(phrase);
			cell.setBorder(Rectangle.TOP);
			table.addCell(cell);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return table;
	}

	private PdfPTable createFooter() {
		PdfPTable table = new PdfPTable(1);
		PdfPCell cell;
		Phrase phrase;
		try {
			table.setWidths(new int[]{1});
			table.getDefaultCell().setBorder(Rectangle.NO_BORDER);

			if (mStrSignatureFilePath.length() > 0 && Utils.isFileExist(mStrSignatureFilePath) == true) {
				Image image = Image.getInstance(mStrSignatureFilePath);
				image.scaleToFit(120, 120);
				cell = new PdfPCell(image);
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			} else {
				phrase = new Phrase("No Signature File", fontBlack10Bold);
				cell = new PdfPCell(phrase);
			}
			cell.setPaddingTop(5);
			cell.setPaddingBottom(5);
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell.setVerticalAlignment(Element.ALIGN_CENTER);
			cell.setBorder(Rectangle.NO_BORDER);
			table.addCell(cell);

			cell = new PdfPCell();
			cell.setBorder(Rectangle.TOP);
			table.addCell(cell);

			phrase = new Phrase("Picked Up By:", fontBlack7Normal);
			cell = new PdfPCell(phrase);
			cell.setPaddingTop(5);
			cell.setBorder(Rectangle.NO_BORDER);
			table.addCell(cell);
			phrase = new Phrase(Utils.getStringSetting(mContext, Global.PREF_USER_DESCRIPTION), fontBlack7Normal);
			table.addCell(phrase);

			phrase = new Phrase("Load Document#", fontBlack7Normal);
			cell = new PdfPCell(phrase);
			cell.setPaddingTop(5);
			cell.setBorder(Rectangle.NO_BORDER);
			table.addCell(cell);
			phrase = new Phrase("" + mPdfTrxnNo, fontBlack7Normal);
			table.addCell(phrase);

			phrase = new Phrase("Printed: " + new SimpleDateFormat(Utils.getDateFormat(mContext, "dd/MM/yyyy") + " hh:mm aa").format(new Date()), fontBlack7Normal);
			cell = new PdfPCell(phrase);
			cell.setPaddingTop(5);
			cell.setBorder(Rectangle.NO_BORDER);
			table.addCell(cell);

			phrase = new Phrase("-------- END OF REPORT --------", fontBlack7Normal);
			cell = new PdfPCell(phrase);
			cell.setColspan(2);
			cell.setPaddingTop(5);
			cell.setBorder(Rectangle.NO_BORDER);
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			table.addCell(cell);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return table;
	}

	private void createPDF() {
		try {
			Utils.createDirectory(Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/");
			mPdfTrxnNo = System.currentTimeMillis();
			mStrPdfFileName = getPDFFileName();
			int nHeight = 250 + 17 * Global.gLoadReqeustInfo.size();

			if (mStrSignatureFilePath.length() > 0 && Utils.isFileExist(mStrSignatureFilePath) == true) {
				nHeight += 120;
			}
			Document document = new Document(new Rectangle(0, 0, 140, nHeight), 0, 0, 0, 0);
			PdfWriter.getInstance(document, new FileOutputStream(Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + mStrPdfFileName));
			document.open();
			PdfPTable table = createHeader();
			document.add(table);
			table = createBody();
			table.setSpacingBefore(5);
			document.add(table);
			table = createFooter();
			table.setSpacingBefore(5);
			document.add(table);
			document.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void doDone(){
		new Thread(){
			@Override
			public void run() {
				new Handler(Looper.getMainLooper()).post(() -> {
					try
					{
						showProgressDialog(Str.CreatingPdf[Global.LANG]);
					}catch (Exception e)
					{
						e.printStackTrace();
					}

				});

				String strTemplateFileName = Utils.getFinalPrintTemplateFileName(mContext, Global.PRINT_LOAD_REQUEST_TEMPLATE_FILENAME);
				if(Utils.isFileExist(Global.ROOT_PRINT_TEMPLATE_LOCCATION + "/" + strTemplateFileName))
				{
					PrintEngine printEngine = new PrintEngine();
					mHoneywellPrintEngine = printEngine;
					mPdfTrxnNo = System.currentTimeMillis();
					Utils.createDirectory(Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/");
					mStrPdfFileName = getPDFFileName();
					Date date = new Date(mDate);

					DbAdapter dbAdapter = new DbAdapter(mContext);
					dbAdapter.open();
					Utils.gDetlMap.clear();

					if(Global.gLoadReqeustInfo != null)
					{
						// if there are additional containers, add them, too
						if (mAdditionalContainers.size() > 0) {
							Global.gLoadReqeustInfo.addAll(mAdditionalContainers);
						}

						for(UInvenDInfo info : Global.gLoadReqeustInfo)
						{
							ProdDetlInfo prodDetlInfo = dbAdapter.getProdDetl(info.strItemNo);
							if(prodDetlInfo != null)
							{
								Utils.gDetlMap.put(prodDetlInfo.strItemNo, Utils.getPropertyMap(prodDetlInfo));
							}
						}
					}
					dbAdapter.close();

					printEngine.PreparePrint(mContext, Global.gLoadReqeustInfo, null, "Load Request", mTrxnDate, mPdfTrxnNo,
							mStrSignatureFilePath, mStrPdfFileName.substring(0, mStrPdfFileName.length() - 4), date, "", null);

					String strHoneywellFileName = Utils.getFinalPrintTemplateFileNameForHoneywell(mContext, Global.PRINT_LOAD_REQUEST_TEMPLATE_FILENAME);
					mHoneywellTemplatePath = Global.ROOT_PRINT_TEMPLATE_LOCCATION  + strHoneywellFileName;
					printEngine.createDocument(mContext, false, Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + mStrPdfFileName, Global.LOAD_REQUEST_PRINT);
				}
				else
				{
					createPDF();
				}

				initMakeZipInfo();
				int nCustNo = Utils.parseIntFromString(Utils.getStringSetting(mContext, Global.PREF_ROUTE_NUMBER));
				String strDocNo = mStrPdfFileName.substring(0, mStrPdfFileName.length() - 4);
				long nTrxnNo = setGeneralUploadData("LDRQ" ,Global.DRIVER_CUSTOMER_CHAINNO, nCustNo, -1, "", strDocNo);

				TransactionInfo transactionInfo = new TransactionInfo();
				transactionInfo.strType = "LDRQ";
				transactionInfo.strTime = new SimpleDateFormat("kk:mm").format(new Date(nTrxnNo));
				transactionInfo.strDocNo = strDocNo;
				DbAdapter dbAdapter = new DbAdapter(mContext);
				dbAdapter.open();

				//Register Print document list.
				PrintItem printItem = new PrintItem();
				printItem.nTrxnNo = nTrxnNo;
				printItem.strDocNo = strDocNo;
				printItem.strDocType = "LDRQ";
				printItem.strFilePath = Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + mStrPdfFileName;
				printItem.nCustNo = 0;
				printItem.nChainNo = 0;
				printItem.strCustomerName = "";
				dbAdapter.insertPrint(printItem);

				dbAdapter.insertTransactionHistoryInfo(transactionInfo);
				dbAdapter.close();

				UTransactionsInfo uTransactionsInfo = setUTransactionsInfo(Global.DRIVER_CUSTOMER_CHAINNO, nCustNo, nTrxnNo, "FARC", mStrPdfFileName);
				setUFileTransactionInfo(uTransactionsInfo.nTrxnNo, Global.DRIVER_CUSTOMER_CHAINNO, nCustNo, mPdfTrxnNo, strDocNo, "LOAD", "LOAD REQUEST", uTransactionsInfo.strTrxnDate, uTransactionsInfo.strTrxnTime, mStrPdfFileName);

				if (Global.gLoadReqeustInfo.size() > 0) {
					UStockHInfo stockHInfo = new UStockHInfo();
					Date date = new Date(mDate);
					//stockHInfo.strRequiredDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(mContext, date));
					stockHInfo.strRequiredDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(date);
					stockHInfo.strDocNo = mStrPdfFileName.substring(0, mStrPdfFileName.length() - 4);
					stockHInfo.strReference = stockHInfo.strDocNo + ".pdf";

					stockHInfo.nTrxnNo = nTrxnNo;
					stockHInfo.strStockType = "4";

					stockHInfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(mContext, new Date(nTrxnNo)));
					stockHInfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(new Date(nTrxnNo));
					stockHInfo.strDocType = "LDRQ";

					dbAdapter = new DbAdapter(mContext);
					dbAdapter.open();
					dbAdapter.insertUStockHInfoList(stockHInfo);
					for (UInvenDInfo info : Global.gLoadReqeustInfo) {
						UStockDInfo stockDInfo = new UStockDInfo();
						stockDInfo.nTrxnNo = nTrxnNo;
						stockDInfo.strItemNo = info.strItemNo;
						stockDInfo.nReasonCode = 0;
						stockDInfo.strDetailType = " ";
						if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
						{
							stockDInfo.dActualQty = info.dStock;
							stockDInfo.dBeginQty = info.dBeginQty;
							stockDInfo.dSuggestQty = info.dLoaded;
						}
						else
						{
							stockDInfo.nActualQty = info.nStock;
							stockDInfo.nBeginQty = info.nBeginQty;
							stockDInfo.nSuggestQty = info.nLoaded;
						}

						dbAdapter.insertUStockDInfoList(stockDInfo);
					}

					dbAdapter.close();
				}
				doLastProcess();
				new Handler(Looper.getMainLooper()).post(new Runnable() {
					@Override
					public void run() {
						hideProgressDialog();

						if(Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 1)
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
									} else if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
										goBack();
										m_bCompletedClicked = true;
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
										goBack();
										m_bCompletedClicked = false;
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
										goBack();
										m_bCompletedClicked = false;
									}
								});
								msgDialog.show();
							}catch (Exception e)
							{
								e.printStackTrace();
							}
						}
						else if(Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 4)
						{
							try {
								final MessageDialog msgDialog = new MessageDialog(mContext);
								msgDialog.setCancelable(false);
								msgDialog.mStrMessage = Str.Print_msg[Global.LANG];
								msgDialog.mStrLeft = Str.No[Global.LANG];
								msgDialog.mStrRight = Str.Yes[Global.LANG];
								msgDialog.setOnDismissListener(dialog -> {
									if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
										outputBixolonPrinter();
									} else if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
										goBack();
										m_bCompletedClicked = false;
									}
								});
								msgDialog.show();
							}catch (Exception e)
							{
								e.printStackTrace();
							}
						}
						else{
							goBack();
							m_bCompletedClicked = true;
						}
					}
				});
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
						goBack();
						m_bCompletedClicked = false;
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
						goBack();
						m_bCompletedClicked = false;
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

	private void outputBixolonPrinter() {
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
						outputBixolonPrinter();
					} else if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
						goBack();
						m_bCompletedClicked = false;
					}
				});
				msgDialog.show();
			}catch (Exception e)
			{
				e.printStackTrace();
			}
			return;
		}
		mStrMacAddress = Utils.getStringSetting(mContext, Global.PREF_BIXOLON_PRINTER_MAC_ADDRESS);
		mStrFriendlyName = Utils.getStringSetting(mContext, Global.PREF_BIXOLON_PRINTER_FRIENDLY_NAME);

		if (mStrMacAddress.length() == 0 || mStrFriendlyName.length() == 0) {
			discoveryBixolonPrinters();
		} else {
			doBixolonPrintPdfTask();
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
					goBack();
					m_bCompletedClicked = false;
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
							goBack();
							m_bCompletedClicked = false;
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
					goBack();
					m_bCompletedClicked = false;
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
							goBack();
							m_bCompletedClicked = false;
						}
					});
					msgDialog.show();
				}catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}, mStrMacAddress, mStrFriendlyName, Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + mStrPdfFileName);
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
						goBack();
						m_bCompletedClicked = false;
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
								goBack();
								m_bCompletedClicked = false;
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
			goBack();
			m_bCompletedClicked = false;
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

	private void doComplete() {
		if(!isCompleteProcessPossible)
		{
			try {
				final MessageDialog msgDialog = new MessageDialog(mContext);
				msgDialog.setCancelable(false);
				msgDialog.mStrMessage = Str.LoadReuqestDoneByTripSettlement[Global.LANG];
				msgDialog.mStrMiddle = Str.Ok[Global.LANG];
				msgDialog.setOnDismissListener(dialog -> {
					goBack();
					m_bCompletedClicked = true;
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
				msgDialog.mStrTitle = Str.OrderConfirmation[Global.LANG];
				msgDialog.mStrMessage = String.format(Locale.ENGLISH, Str.PleaseConfirmYouAre[Global.LANG], new SimpleDateFormat(Utils.getDateFormat(mContext, "dd/MM/yyyy")).format(new Date(mDate)));
				msgDialog.mStrRight = Str.Yes[Global.LANG];
				msgDialog.mStrLeft = Str.No[Global.LANG];
				msgDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT){
							m_bCompletedClicked = false;
							return;
						}
						else if( msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT){
							doDone();
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

	private void outputPrinter() {
		mIsCreatedPDF = true;
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
							goBack();
							m_bCompletedClicked = true;
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
				msgDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
							discoveryPrinters();
						} else if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
							m_bCompletedClicked = false;
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
			mStrFriendlyName = strArray[0];
		}

		try {
			new AlertDialog.Builder(mContext)
					.setTitle(Str.SelectPrinter[Global.LANG])
					.setSingleChoiceItems(new AlertSingleChoiceAdapter(mContext, strDevices), 0, (dialogInterface, i) -> {
						String strArray1[] = strDevices[i].split("\n");
						if (strArray1.length > 0) {
							mStrMacAddress = strArray1[0];
						}
						if (strArray1.length > 1) {
							mStrFriendlyName = strArray1[0];
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

	private void doLastProcess()
	{
		if(bLastProcess == true) return;
		//Global.gUploadCustNoList.add(makeZipFileInfo);
		Utils.registerUploadFile(mContext, makeZipFileInfo);
		bLastProcess = true;
	}

	private void goBack() {
		if(Utils.getStringSetting(mContext, Global.PREF_LOAD_REQUEST_EOT).equals("1") || Utils.getStringSetting(mContext, Global.PREF_LOAD_REQUEST_EOT).equals("2"))
		{
			//Save Current LoadRequest Data to the preference storage.
			Gson gson = new Gson();
			String strLoadRequestData = gson.toJson(Global.gLoadReqeustInfo);
			Utils.setStringSetting(mContext, Global.PREF_LOAD_REQUEST_DATA, strLoadRequestData);
		}

		if(bPaused)
		{
			bNeedBack = true;
		}
		else
		{
			if(Utils.getStringSetting(mContext, Global.PREF_LOAD_REQUEST_EOT).equals("1") || Utils.getStringSetting(mContext, Global.PREF_LOAD_REQUEST_EOT).equals("2") || Utils.getStringSetting(mContext, Global.PREF_LOAD_REQUEST_EOT).equals("3"))
			{
				Utils.setBooleanSetting(mContext, String.format(Locale.ENGLISH, Global.PREF_END_TRIP_CHECKED, 2), true);
			}
			((LoadRequestFragment)Global.gFragmentForCheckingModification).bModified = false;
			Global.gLoadReqeustInfo.clear();
			mContext.onBackPressed();
			mContext.onBackPressed();
//			mContext.onBackPressed();
		}

	}

	boolean bNeedBack = false;
	boolean bPaused = true;

	private DatePickerDialog.OnDateSetListener dateListener = new DatePickerDialog.OnDateSetListener() {
		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			Date myDate = new Date(year-1900, monthOfYear, dayOfMonth, 0, 0, 0);
			mDate = myDate.getTime();
			showTimes();
		}
	};

	public boolean m_bClicked = false;
	public boolean m_bCompletedClicked = false;
	@Override
	public void onClick(View v) {
		if( m_bCompletedClicked ) return;
		if( m_bClicked ) return;
		m_bClicked = true;
		switch (v.getId()) {
			case R.id.btnPickDate:
				Date startDate = new Date(mDate);
				try {
					new DatePickerDialog(mContext, dateListener, 1900 + startDate.getYear(), startDate.getMonth(), startDate.getDate()).show();
				}catch (Exception e)
				{
					e.printStackTrace();
				}

				break;
			case R.id.btnSignature:
				Intent intent = new Intent(mContext, SignatureActivity.class);
				intent.putExtra(Global.EXTRA_SIGNATURE_PATH, Global.DEFAULT_SIGNATURE_FILEPATH);
				startActivity(intent);
				mContext.overridePendingTransition(R.anim.zoom_in, 0);
				break;
			case R.id.btnKey:
				processKeyButtonPressed();
				break;
			case R.id.btnCompleted:
				if( m_bCompletedClicked ) return;
				m_bCompletedClicked = true;
				if (mIsCreatedPDF == false) {
					doComplete();
				} else {
					doLastProcess();
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
								} else if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
									goBack();
									m_bCompletedClicked = true;
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
									goBack();
									m_bCompletedClicked = false;
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
									goBack();
									m_bCompletedClicked = false;
								}
							});
							msgDialog.show();
						}catch (Exception e)
						{
							e.printStackTrace();
						}
					}
					else if(Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 4)
					{
						try {
							final MessageDialog msgDialog = new MessageDialog(mContext);
							msgDialog.setCancelable(false);
							msgDialog.mStrMessage = Str.Print_msg[Global.LANG];
							msgDialog.mStrLeft = Str.No[Global.LANG];
							msgDialog.mStrRight = Str.Yes[Global.LANG];
							msgDialog.setOnDismissListener(dialog -> {
								if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
									outputBixolonPrinter();
								} else if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
									goBack();
									m_bCompletedClicked = false;
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
						m_bCompletedClicked = true;
					}
				}
				break;
			case R.id.btnBack:
				mContext.onBackPressed();
				break;
		}
		m_bClicked = false;
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
					goBack();
					m_bCompletedClicked = true;
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
									goBack();
									m_bCompletedClicked = true;
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

	private class ListAdapter extends BaseAdapter {

		private List<UInvenDInfo> list = new ArrayList<>();

		private class ViewHolder {
			public TextView txtShortDesc, txtQty;
		}

		public int getCount() {
			return list.size();
		}

		public void setListData(List<UInvenDInfo> list) {
			this.list.clear();
			this.list.addAll(list);
		}

		public UInvenDInfo getItem(int position) {
			if(position < list.size())
			{
				return list.get(position);
			}
			else {
				return null;
			}
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(final int position, View convertView, ViewGroup parent) {
			View view = convertView;
			final ListAdapter.ViewHolder holder;
			final UInvenDInfo info = getItem(position);
			if(info == null) return null;
			if (convertView == null) {
				view = mContext.getLayoutInflater().inflate(R.layout.row_load_request_container, parent, false);
				holder = new ListAdapter.ViewHolder();
				holder.txtShortDesc = (TextView) view.findViewById(R.id.txtItemNo);
				holder.txtQty = (TextView) view.findViewById(R.id.txtQty);
				view.setTag(holder);
			} else {
				holder = (ListAdapter.ViewHolder) view.getTag();
			}

			holder.txtShortDesc.setText(info.strShortDesc);
			holder.txtQty.setText(String.format(Locale.ENGLISH, "%d", info.nStock));

			return view;
		}
	}
}