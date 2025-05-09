package dsd.dsdvansales.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
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
import dsd.dsdvansales.dialogs.MessageDialog;
import dsd.dsdvansales.global.BixolonPrinter;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.HoneywellLinePrinter;
import dsd.dsdvansales.global.HoneywellPrinter;
import dsd.dsdvansales.global.PrintEngine;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.model.BatchLot;
import dsd.dsdvansales.model.DescTypeInfo;
import dsd.dsdvansales.model.LoadBlotInfo;
import dsd.dsdvansales.model.PrintItem;
import dsd.dsdvansales.model.ProdDetlInfo;
import dsd.dsdvansales.model.ProdLocnInfo;
import dsd.dsdvansales.model.TransactionInfo;
import dsd.dsdvansales.model.UInvenBlotDInfo;
import dsd.dsdvansales.model.UInvenBlotInfo;
import dsd.dsdvansales.model.UInvenDInfo;
import dsd.dsdvansales.model.UInvenHInfo;
import dsd.dsdvansales.model.UOrderDInfo;
import dsd.dsdvansales.model.UStockDInfo;
import dsd.dsdvansales.model.UStockHInfo;
import dsd.dsdvansales.model.UTransactionsInfo;
import dsd.dsdvansales.views.AlertSingleChoiceAdapter;

public class EndingInventorySummaryFragment extends BaseFragment implements View.OnClickListener {

	private Font fontDarkGray7Normal;
	private Font fontBlack7Bold;
	private Font fontBlack10Bold;


	private int mTemperatureIdx = 0;
	private List<Float> mTemperatureList = new ArrayList<>();

	private String mStrPdfFileName = "";
	private String mStrOffLoadPdfFileName = "";
	private long mPdfTrxnNo = 0;
	private Date mTrxnDate = new Date();

	private UInvenHInfo mUInvenHInfo = null;

	private boolean bShowCase = true;

	private String mCartonDescription = "";

	private boolean mIsCreatedPDF = false;

	private String mStrMacAddress = "";
	private String mStrFriendlyName = "";

	private Connection printerConnection = null;
	private long m_lUnloadTrxnNo = -1;
	private boolean bOffLoadPDfCreated = false;

	private PrintEngine mHoneywellPrintEngine = null;
	private String mHoneywellTemplatePath = "";
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootView = inflater.inflate(R.layout.fragment_ending_inventory_summary, container, false);
		commonInit();
		loadFont();
		return mRootView;
	}

	private void loadFont()
	{
		fontDarkGray7Normal = Utils.getFont(Utils.getPrintFontFilePath(), Utils.getPrintEncoding(), Utils.getPrintEmbeded(), 7, Font.NORMAL, BaseColor.DARK_GRAY);
		fontBlack7Bold = Utils.getFont(Utils.getPrintFontFilePath(), Utils.getPrintEncoding(), Utils.getPrintEmbeded(), 7, Font.BOLD, BaseColor.BLACK);
		fontBlack10Bold = Utils.getFont(Utils.getPrintFontFilePath(), Utils.getPrintEncoding(), Utils.getPrintEmbeded(), 10, Font.BOLD, BaseColor.BLACK);
	}

	@Override
	public void onResume() {
		super.onResume();
		Global.gFragIdx = Global.FRAG_ENDING_INVENTORY_SUMMARY;
		bPaused = false;
		if(bNeedBack)
		{
			new Handler(Looper.getMainLooper()).post(new Runnable() {
				@Override
				public void run() {
					Fragment fragment = mContext.getSupportFragmentManager().findFragmentById(R.id.frameContainer);
					while(!(fragment instanceof TripSettlementFragment))
					{
						mContext.onBackPressed();
						fragment = mContext.getSupportFragmentManager().findFragmentById(R.id.frameContainer);
					}

					m_bCompletedClicked = false;
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
		bShowCase = Utils.getStringSetting(mContext, Global.PREF_INVENTORYUOM).equals("U") == false;
		mRootView.findViewById(R.id.txtTemperature).setOnClickListener(this);
		mRootView.findViewById(R.id.btnSignature).setOnClickListener(this);
		mRootView.findViewById(R.id.btnCompleted).setOnClickListener(this);
		mRootView.findViewById(R.id.btnBack).setOnClickListener(this);

		loadData();
		refreshLanguage();
	}

	@Override
	public void loadData() {
		mUInvenHInfo = Global.gCurEndingInventory;
		int nIdx = 0;
		String strTempFmt = Utils.getStringSetting(mContext, Global.PREF_TEMPFMT);
		if(TextUtils.isEmpty(strTempFmt) || strTempFmt.endsWith("C"))
		{
			float nVal;
			float min = Utils.getFloatSetting(mContext, Global.PREF_LOAD_TEMPMIN);
			float max = Utils.getFloatSetting(mContext, Global.PREF_LOAD_TEMPMAX);
			do {
				nVal = min + nIdx * 0.5f;
				mTemperatureList.add(nVal);
				nIdx++;
			} while (nVal < max);
		}
		else
		{
			float nVal;
			int min = (int)Utils.getFloatSetting(mContext, Global.PREF_LOAD_TEMPMIN).floatValue();
			int max = (int)Utils.getFloatSetting(mContext, Global.PREF_LOAD_TEMPMAX).floatValue();
			do {
				nVal = min + nIdx;
				mTemperatureList.add(nVal);
				nIdx++;
			} while (nVal < max);
		}

		mTemperatureIdx = -1;

		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
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

		refreshPage();
	}

	@Override
	public void refreshPage() {
		if(Utils.getIntSetting(mContext, Global.PREF_LOAD_TEMPREQ) == 1)
		{
			if(mTemperatureIdx != -1)
			{
				mRootView.findViewById(R.id.btnCompleted).setEnabled(true);
			}
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
		try
		{
			((TextView) mRootView.findViewById(R.id.lblTitle)).setText(Str.EndingInventory[Global.LANG].toUpperCase());
			((TextView) mRootView.findViewById(R.id.txtTrip)).setText(Str.Trip[Global.LANG] + " # " + Global.gTripNumber);
			((TextView) mRootView.findViewById(R.id.lblYouHaveLoaded)).setText(Str.YouAreUnloadingTotalOf[Global.LANG]);
			((TextView) mRootView.findViewById(R.id.lblLoadTemperature)).setText(Str.OffLoadTemporature[Global.LANG]);
			((Button) mRootView.findViewById(R.id.btnSignature)).setText(Str.Signature[Global.LANG]);
			((Button) mRootView.findViewById(R.id.btnCompleted)).setText(Str.Completed[Global.LANG]);

			int nCartons = 0;
			int nUnits = 0;
			int nContainers = 0;
			double dValue = 0;
			int nFreshCartons = 0;
			int nDumpCartons = 0;
			int nSpoilsCartons = 0;
			int nFreshUnits = 0;
			int nDumpUnits = 0;
			int nSpoilsUnits = 0;
			double dFreshValue = 0;
			double dDumpValue = 0;
			double dSpoilsValue = 0;
			if( mUInvenHInfo.uInvenDList != null && mUInvenHInfo.uInvenDList.size() > 0 )
			{
				for (UInvenDInfo info : mUInvenHInfo.uInvenDList) {
					if (Utils.isContainerItem(mContext, info.strItemNo)) {
						nContainers += info.nUnload;
					}
					else {
						if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
						{
							dValue += info.dUnload + info.dDumpsUnload + info.dSpoils;
							dFreshValue += info.dUnload;
							dDumpValue += info.dDumpsUnload;
							dSpoilsValue += info.dSpoils;
						}
						else
						{
							if(bShowCase)
							{
								int nCase = getCaseValue(info.strItemNo);
								int nValue = info.nUnload + info.nDumpsUnload + info.nSpoils;
								int nCaseValue = nValue / nCase;
								int nQtyValue = nValue % nCase;
								nCartons += nCaseValue;
								nUnits += nQtyValue;

								nFreshCartons += info.nUnload / nCase;
								nDumpCartons += info.nDumpsUnload / nCase;
								nSpoilsCartons += info.nSpoils / nCase;
								nFreshUnits += info.nUnload % nCase;
								nDumpUnits += info.nDumpsUnload % nCase;
								nSpoilsUnits += info.nSpoils % nCase;
							}
							else
							{
								int nValue = info.nUnload + info.nDumpsUnload + info.nSpoils;
								nUnits += nValue;

								nFreshUnits += info.nUnload;
								nDumpUnits += info.nDumpsUnload;
								nSpoilsUnits += info.nSpoils;
							}
						}
					}
				}
			}

			String strLoaded = "";

			if (nFreshUnits > 0) {
				if (bShowCase) {
					strLoaded += String.format(Locale.ENGLISH, "%d/%d %s", nFreshCartons, nFreshUnits, Str.Fresh[Global.LANG]);
				}
				else {
					strLoaded += String.format(Locale.ENGLISH, "%d %s", nFreshUnits, Str.Fresh[Global.LANG]);
				}
			}
			if (nDumpUnits > 0) {
				if(strLoaded.length() > 0) strLoaded += "\n";
				if (bShowCase) {
					strLoaded += String.format(Locale.ENGLISH, "%d/%d %s", nDumpCartons, nDumpUnits, Str.Dumps[Global.LANG]);
				}
				else {
					strLoaded += String.format(Locale.ENGLISH, "%d %s", nDumpUnits, Str.Dumps[Global.LANG]);
				}
			}
			if (nSpoilsUnits > 0) {
				if(strLoaded.length() > 0) strLoaded += "\n";
				if (bShowCase) {
					strLoaded += String.format(Locale.ENGLISH, "%d/%d %s", nSpoilsCartons, nSpoilsUnits, Str.Spoils[Global.LANG]);
				}
				else {
					strLoaded += String.format(Locale.ENGLISH, "%d %s", nSpoilsUnits, Str.Spoils[Global.LANG]);
				}
			}
			if (nContainers > 0)
			{
				if(strLoaded.length() > 0) strLoaded += "\n";
				strLoaded += String.format(Locale.ENGLISH, "%d %s", nContainers, Str.containers[Global.LANG]);
			}
			if(dValue > 0)
			{
				if(strLoaded.length() > 0) strLoaded += "\n";
				strLoaded += String.format(Locale.ENGLISH, "%.2f Kg", dValue);
			}
			if(TextUtils.isEmpty(strLoaded))
			{
				String strCarton;
				if( TextUtils.isEmpty(mCartonDescription) )
				{
					strCarton = Str.units[Global.LANG];
				}
				else
				{
					strCarton = mCartonDescription;
				}
				strLoaded += String.format(Locale.ENGLISH, "%d %s", nUnits, strCarton);
			}
			((TextView) mRootView.findViewById(R.id.txtLoaded)).setText(strLoaded);

		}catch(Exception e)
		{
			e.printStackTrace();
		}

		if(Utils.getIntSetting(mContext, Global.PREF_LOAD_TEMPREQ) == 1)
		{
			if(mTemperatureIdx == -1)
			{
				((TextView) mRootView.findViewById(R.id.txtTemperature)).setText(Str.SelectTemp[Global.LANG]);
				mRootView.findViewById(R.id.btnCompleted).setEnabled(false);
			}
		}

	}

	@Override
	public void receiveBroadcast(String strAction, Intent intent) {
		if (strAction.equals(Global.ACTION_CAPTURE_SIGNATURE) == true) {
			if (Global.gFragIdx == Global.FRAG_LOAD_TRUCK_SIGN && Global.gUInvenHinfo != null) {
				Global.gUInvenHinfo.strSignatureFilePath = intent.getStringExtra(Global.EXTRA_SIGNATURE_PATH);
				Utils.printDebug(mContext, Str.Saved_successfully[Global.LANG]);
			}
		}
	}

	private void showTemperatureList(int nOrgIdx, String strOrgText) {
		final String strArray[] = new String[mTemperatureList.size()];
		String strTempFmt = Utils.getStringSetting(mContext, Global.PREF_TEMPFMT);
		for (int i = 0; i < strArray.length; i++) {
			if(TextUtils.isEmpty(strTempFmt) || strTempFmt.endsWith("C"))
			{
				strArray[i] = mTemperatureList.get(i) + " °C";
			}
			else
			{
				strArray[i] = (int)mTemperatureList.get(i).floatValue() + " °F";
			}
		}
		final int origIdx;
		final String origText;
		if( nOrgIdx == -2 )
		{
			origIdx = mTemperatureIdx;
			origText = ((TextView) mRootView.findViewById(R.id.txtTemperature)).getText().toString();
		}
		else
		{
			origIdx = nOrgIdx;
			origText = strOrgText;
		}

		try {
			new AlertDialog.Builder(mContext)
					.setTitle(Str.SelectTemperature[Global.LANG])
					.setSingleChoiceItems(new AlertSingleChoiceAdapter(mContext, strArray), mTemperatureIdx, (dialog, nSelected) -> {
						mTemperatureIdx = nSelected;
						((TextView) mRootView.findViewById(R.id.txtTemperature)).setText(strArray[nSelected]);
					})
					.setPositiveButton(Str.Ok[Global.LANG], new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog, int nSelected)
						{
							if(mTemperatureIdx < 0) return;
							float fTemp = mTemperatureList.get(mTemperatureIdx);
							if( fTemp >= Utils.getFloatSetting(mContext, Global.PREF_LOAD_TEMPMIN) && fTemp <= Utils.getFloatSetting(mContext, Global.PREF_LOAD_TEMPMAX))
							{
								refreshPage();
							}
							else
							{
								//mTemperatureIdx = origIdx;
								//((TextView) mRootView.findViewById(R.id.txtTemperature)).setText(origText);
								try {
									final MessageDialog msgDialog = new MessageDialog(mContext);
									msgDialog.setCancelable(false);
									msgDialog.mStrMessage = Str.TemperatureSelectedIsOutsideRange[Global.LANG];
									msgDialog.mStrMiddle = Str.Ok[Global.LANG];
									msgDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
										@Override
										public void onDismiss(DialogInterface dialog) {
											if (msgDialog.mReturnCode == Global.DLG_RETURN_MIDDLE) {
												showTemperatureList(origIdx, origText);
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
					})
					.setNegativeButton(Str.Cancel[Global.LANG], new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog, int nSelected)
						{
							mTemperatureIdx = origIdx;
							((TextView) mRootView.findViewById(R.id.txtTemperature)).setText(origText);
						}
					})
					.create()
					.show();
		}catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	public boolean m_bClicked = false;
	public boolean m_bCompletedClicked = false;
	@Override
	public void onClick(View v) {
		if( m_bClicked ) return;
		m_bClicked = true;
		switch (v.getId()) {
			case R.id.txtTemperature:
				showTemperatureList(-2, null);
				break;
			case R.id.btnSignature:
				Intent intent = new Intent(mContext, SignatureActivity.class);
				intent.putExtra(Global.EXTRA_SIGNATURE_PATH, Global.DEFAULT_SIGNATURE_FILEPATH);
				startActivity(intent);
				mContext.overridePendingTransition(R.anim.zoom_in, 0);
				break;
			case R.id.btnCompleted:
				mTrxnDate = new Date();
				Utils.createDirectory(Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/");
				mPdfTrxnNo = System.currentTimeMillis();
				mStrPdfFileName = getPDFFileName();

				//Create OffLoad Report Pdf
				boolean bExistOffLoadIven = false;
				for(UInvenDInfo info : mUInvenHInfo.uInvenDList)
				{
					if(info.nDumpsUnload == 0 && info.nUnload == 0 && info.nSpoils == 0
							&& info.dDumpsUnload == 0 && info.dUnload == 0 && info.dSpoils == 0)
					{
						continue;
					}
					else
					{
						bExistOffLoadIven = true;
						break;
					}
				}

				if(bExistOffLoadIven)
				{
					mStrOffLoadPdfFileName = getPDFFileName();
					String strDocNo = mStrOffLoadPdfFileName.substring(0, mStrOffLoadPdfFileName.length() - 4);
					m_lUnloadTrxnNo = setGeneralUploadData("UNLD", 0, 0, -1, "", strDocNo);

					TransactionInfo transactionInfo = new TransactionInfo();
					transactionInfo.strType = "UNLD";
					transactionInfo.strTime = new SimpleDateFormat("kk:mm").format(new Date(m_lUnloadTrxnNo));
					transactionInfo.strDocNo = strDocNo;
					DbAdapter dbAdapter = new DbAdapter(mContext);
					dbAdapter.open();
					dbAdapter.insertTransactionHistoryInfo(transactionInfo);

					//Register Print document list.
					PrintItem printItem = new PrintItem();
					printItem.nTrxnNo = m_lUnloadTrxnNo;
					printItem.strDocNo = strDocNo;
					printItem.strDocType = "UNLD";
					printItem.strFilePath = Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + mStrOffLoadPdfFileName;
					printItem.nCustNo = 0;
					printItem.nChainNo = 0;
					printItem.strCustomerName = "";
					dbAdapter.insertPrint(printItem);

					dbAdapter.close();
					String strTemplateFileName = Utils.getFinalPrintTemplateFileName(mContext, Global.PRINT_OFFLOAD_TEMPLATE_FILENAME);
					if(Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) != 0
							&& Utils.isFileExist(Global.ROOT_LOCATION + "/" + strTemplateFileName))
					{
						new Thread()
						{
							@Override
							public void run() {
								new Handler(Looper.getMainLooper()).post(new Runnable() {
									@Override
									public void run() {
										try {
											showProgressDialog(Str.CreatingPdf[Global.LANG]);
										}catch (Exception e)
										{
											e.printStackTrace();
										}

									}
								});
								doCreateOffloadPdf();
								new Handler(Looper.getMainLooper()).post(new Runnable() {
									@Override
									public void run() {
										hideProgressDialog();
										try {
											final MessageDialog msgDialog = new MessageDialog(mContext);
											msgDialog.setCancelable(false);
											msgDialog.mStrMessage = Str.Print_msg[Global.LANG];
											msgDialog.mStrLeft = Str.No[Global.LANG];
											msgDialog.mStrRight = Str.Yes[Global.LANG];
											msgDialog.setOnDismissListener(dialog -> {
												if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
													doProcessCreateOffLoadPdf();
												} else if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
													doProcessCompleted();
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
					}
					else
					{
						doProcessCompleted();
					}
				}
				else
				{
					doProcessCompleted();
				}

				break;
			case R.id.btnBack:
			{
				mContext.onBackPressed();
			}
				break;
		}
		m_bClicked = false;
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
						doProcessCompleted();
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
						doProcessCompleted();
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
						outputHoneywellLinePrinter();
					} else if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
						doProcessCompleted();
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
		mStrFriendlyName = Utils.getStringSetting(mContext, Global.PREF_BIXOLON_PRINTER_MAC_ADDRESS);

		if (mStrMacAddress.length() == 0 || mStrFriendlyName.length() == 0) {
			discoveryBixolonPrinters();
		} else {
			doBixolonPrintPdfTask();
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

	private void doProcessCreateOffLoadPdf()
	{
		new Thread(){
			@Override
			public void run() {
				new Handler(Looper.getMainLooper()).post(new Runnable() {
					@Override
					public void run() {
						if(Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 1)
						{
							outputPrinter();
						}
						else if(Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 2)
						{
							outputHoneywellPrinter();
						}
						else if(Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 3)
						{
							outputHoneywellLinePrinter();
						}
						else if(Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 4)
						{
							outputBixolonPrinter();
						}
//						if (BluetoothAdapter.getDefaultAdapter().isEnabled() == false) {
//							try {
//								final MessageDialog msgDialog = new MessageDialog(mContext);
//								msgDialog.setCancelable(false);
//								msgDialog.mStrMessage = Str.TurnOnBluetooth_alert[Global.LANG];
//								msgDialog.mStrLeft = Str.Cancel[Global.LANG];
//								msgDialog.mStrRight = Str.Retry[Global.LANG];
//								msgDialog.setOnDismissListener(dialog -> {
//									if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
//										outputPrinter();
//									} else if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
//										doProcessCompleted();
//									}
//								});
//								msgDialog.show();
//							}catch (Exception e)
//							{
//								e.printStackTrace();
//							}
//
//							return;
//						}
//						mStrMacAddress = Utils.getStringSetting(mContext, Global.PREF_PRINTER_MAC_ADDRESS);
//						if (mStrMacAddress.length() == 0) {
//							discoveryPrinters();
//						} else {
//							new PrintPdfTask().execute();
//						}
					}
				});


			}
		}.start();

	}

	private void doCreateOffloadPdf()
	{
		if(!bOffLoadPDfCreated)
		{
			String strDocNo = mStrOffLoadPdfFileName.substring(0, mStrOffLoadPdfFileName.length() - 4);
			PrintEngine printEngine = new PrintEngine();
			mHoneywellPrintEngine = printEngine;
			String strTemplateFileName = Utils.getFinalPrintTemplateFileName(mContext, Global.PRINT_OFFLOAD_TEMPLATE_FILENAME);
			if(Utils.isFileExist(Global.ROOT_PRINT_TEMPLATE_LOCCATION + "/" + strTemplateFileName))
			{
				DbAdapter dbAdapter = new DbAdapter(mContext);
				dbAdapter.open();
				Utils.gDetlMap.clear();
				if(mUInvenHInfo != null && mUInvenHInfo.uInvenDList != null)
				{
					for(UInvenDInfo info : mUInvenHInfo.uInvenDList)
					{
						ProdDetlInfo prodDetlInfo = dbAdapter.getProdDetl(info.strItemNo);
						if(prodDetlInfo != null)
						{
							Utils.gDetlMap.put(prodDetlInfo.strItemNo, Utils.getPropertyMap(prodDetlInfo));
						}
					}
				}
				dbAdapter.close();

				printEngine.PrepareOffLoadPrint(mContext, mUInvenHInfo);
				String strHoneywellFileName = Utils.getFinalPrintTemplateFileNameForHoneywell(mContext, Global.PRINT_OFFLOAD_TEMPLATE_FILENAME);
				mHoneywellTemplatePath = Global.ROOT_PRINT_TEMPLATE_LOCCATION + "/" + strHoneywellFileName;
				printEngine.createDocument(mContext, false, Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + mStrOffLoadPdfFileName, Global.OFFLOAD_PRINT);

				UTransactionsInfo uTransactionsInfo = setUTransactionsLastInfo(m_lUnloadTrxnNo + 1, "FARC", mStrOffLoadPdfFileName);
				setUFileTransactionInfo(uTransactionsInfo.nTrxnNo, 0, 0, m_lUnloadTrxnNo, strDocNo, "UNLD", "INVENTORY OFFLOAD", uTransactionsInfo.strTrxnDate, uTransactionsInfo.strTrxnTime, mStrOffLoadPdfFileName);
			}
			bOffLoadPDfCreated = true;
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
							doProcessCompleted();
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
					doProcessCompleted();
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
							doProcessCompleted();
						}
					});
					msgDialog.show();
				}catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}, mStrMacAddress, Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + mStrOffLoadPdfFileName);
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
						doProcessCompleted();
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
								doProcessCompleted();
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
			doProcessCompleted();
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
					doProcessCompleted();
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
							doProcessCompleted();
						}
					});
					msgDialog.show();
				}catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}, mStrMacAddress, mStrFriendlyName, Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + mStrOffLoadPdfFileName);
	}

	private void showDeviceList(List<String> deviceList) {
		if(deviceList.size() == 0)
		{
			if(!mContext.isFinishing() && !mContext.isDestroyed())
			{
				try {
					Toast.makeText(mContext, Str.NoDeviceDiscover[Global.LANG], Toast.LENGTH_LONG).show();
				}catch (Exception e)
				{
					e.printStackTrace();
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
					.setSingleChoiceItems(new AlertSingleChoiceAdapter(mContext, strDevices), 0, (dialogInterface, i) -> {
						String strArray1[] = strDevices[i].split("\n");
						if (strArray1.length > 0) {
							mStrMacAddress = strArray1[0];
						}
						if (strArray1.length > 1) {
							mStrFriendlyName = strArray1[1];
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
							doProcessCompleted();
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

	private void doProcessCompleted()
	{
		new Thread(){
			@Override
			public void run() {
				new Handler(Looper.getMainLooper()).post(new Runnable() {
					@Override
					public void run() {
						try {
							showProgressDialog(Str.CreatingPdf[Global.LANG]);
						}catch (Exception e)
						{
							e.printStackTrace();
						}

					}
				});
				createPDF();
				doComplete();

				new Handler(Looper.getMainLooper()).post(new Runnable() {
					@Override
					public void run() {
						hideProgressDialog();
					}
				});

			}
		}.start();
	}

	private PdfPTable createHeader() {
		PdfPTable table = new PdfPTable(3);
		PdfPCell cell;
		Phrase phrase;
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
			phrase = new Phrase("Date: " + new SimpleDateFormat(Utils.getDateFormat(mContext, Global.DATE_DD_MM_YY_FORMAT)).format(mTrxnDate), fontDarkGray7Normal);
			table.addCell(phrase);
			phrase = new Phrase("Time: " + new SimpleDateFormat(Global.TIME_HH_MM_FORMAT).format(mTrxnDate), fontDarkGray7Normal);
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

	public String getCaseUnitFormat(int nValue, int nCase)
	{
		if(bShowCase)
		{
			int nCaseValue = nValue / nCase;
			int nQty = nValue % nCase;
			return nCaseValue + "/" + nQty;
		}
		else
		{
			return String.valueOf(nValue);
		}

	}

	private PdfPTable createBody() {
		PdfPTable table = new PdfPTable(13);
		PdfPCell cell;
		Phrase phrase;
		try {
			table.setWidths(new int[]{5, 12, 3, 4, 4, 4, 4, 5, 4, 4, 4, 4, 6});
			table.getDefaultCell().setBorder(Rectangle.NO_BORDER);

			phrase = new Phrase("End of Trip Inventory Report", fontBlack10Bold);
			cell = new PdfPCell(phrase);
			cell.setColspan(13);
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell.setBorder(Rectangle.NO_BORDER);
			cell.setPadding(5);
			table.addCell(cell);

			phrase = new Phrase("Item#", fontBlack7Bold);
			table.addCell(phrase);
			phrase = new Phrase("Description", fontBlack7Bold);
			table.addCell(phrase);
			phrase = new Phrase("UoM", fontBlack7Bold);
			table.addCell(phrase);
			phrase = new Phrase("Open", fontBlack7Bold);
			table.addCell(phrase);
			phrase = new Phrase("Load", fontBlack7Bold);
			table.addCell(phrase);
			phrase = new Phrase("Adjust", fontBlack7Bold);
			table.addCell(phrase);
			phrase = new Phrase("Sales", fontBlack7Bold);
			table.addCell(phrase);
			phrase = new Phrase("Buyback", fontBlack7Bold);
			table.addCell(phrase);
			phrase = new Phrase("Dumps", fontBlack7Bold);
			table.addCell(phrase);
			phrase = new Phrase("Off Fresh", fontBlack7Bold);
			table.addCell(phrase);
			phrase = new Phrase("Off Dump", fontBlack7Bold);
			table.addCell(phrase);
			phrase = new Phrase("End Adj", fontBlack7Bold);
			table.addCell(phrase);
			phrase = new Phrase("End", fontBlack7Bold);
			table.addCell(phrase);

			int nTotals1[] = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
			int nTotalQty[] = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

			double dTotals1[] = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
			double dTotalQty[] = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

            int nEndTotalCount = 0;

			Collections.sort(mUInvenHInfo.uInvenDList, new Comparator<UInvenDInfo>() {
				@Override
				public int compare(UInvenDInfo lhs, UInvenDInfo rhs) {
					return lhs.strItemNo.compareTo(rhs.strItemNo);
				}
			});
			for (UInvenDInfo info : mUInvenHInfo.uInvenDList) {
				if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
				{
					if (info.nSCWType == Global.INVENTORY_CONTAINERS) {
						continue;
					}
					if(info.dBeginQty == 0 && info.dLoaded == 0 && info.dSpoils == 0 && info.dStock == 0 &&
							info.dSales == 0 && info.dBuybacks == 0 && info.dDumps == 0 && info.dUnload == 0 && info.dDumpsUnload == 0 )
					{
						continue;
					}
					phrase = new Phrase(Utils.getDisplayItemCode(mContext, info.strItemNo), fontDarkGray7Normal);
					table.addCell(phrase);

					phrase = new Phrase(info.strShortDesc, fontDarkGray7Normal);
					table.addCell(phrase);

					phrase = new Phrase(info.strUOM, fontDarkGray7Normal);
					table.addCell(phrase);

					phrase = new Phrase(String.format(Locale.ENGLISH, "%.2f", info.dBeginQty), fontDarkGray7Normal);
					dTotalQty[0] += info.dBeginQty;
					table.addCell(phrase);

					phrase = new Phrase(String.format(Locale.ENGLISH, "%.2f", info.dLoaded + info.dLoadAdj), fontDarkGray7Normal);
					dTotalQty[1] += info.dLoaded + info.dLoadAdj;
					table.addCell(phrase);

					phrase = new Phrase(String.format(Locale.ENGLISH, "%.2f", info.dTransIn - info.dTransOut), fontDarkGray7Normal);
					dTotalQty[2] += info.dTransIn - info.dTransOut;
					table.addCell(phrase);

					phrase = new Phrase(String.format(Locale.ENGLISH, "%.2f", info.dSales), fontDarkGray7Normal);
					dTotalQty[3] += info.dSales;

					table.addCell(phrase);

					phrase = new Phrase(String.format(Locale.ENGLISH, "%.2f", info.dBuybacks), fontDarkGray7Normal);
					dTotalQty[4] += info.dBuybacks;
					table.addCell(phrase);

					phrase = new Phrase(String.format(Locale.ENGLISH, "%.2f", info.dDumps), fontDarkGray7Normal);
					dTotalQty[5] += info.dDumps;
					table.addCell(phrase);

					phrase = new Phrase(String.format(Locale.ENGLISH, "%.2f", info.dUnload + info.dSpoils), fontDarkGray7Normal);
					dTotalQty[6] += info.dUnload + info.dSpoils;
					table.addCell(phrase);

					phrase = new Phrase(String.format(Locale.ENGLISH, "%.2f", info.dDumpsUnload), fontDarkGray7Normal);
					dTotalQty[7] += info.dDumpsUnload;
					table.addCell(phrase);

					double dStock = info.dStock + info.dSales - info.dBuybacks - info.dBeginQty - info.dLoaded - info.dLoadAdj - info.dUnload - info.dSpoils;
					phrase = new Phrase(String.format(Locale.ENGLISH, "%.2f", dStock), fontDarkGray7Normal);
					//dTotalQty[8] += (info.dStock + info.dSales - info.dBeginQty - info.dLoaded - info.dLoadAdj);
					table.addCell(phrase);

					//info.dEndAdj = info.dBeginQty + info.dLoaded + info.dLoadAdj + info.dTransIn - info.dTransOut - info.dSales + info.dBuybacks /*- info.nDumps*/ - info.dUnload - info.dSpoils/*+ info.nDumpsUnload*/;
					info.dEndAdj = dStock;
					dStock = info.dStock - info.dUnload - info.dSpoils/*+ info.nDumpsUnload*/;
					phrase = new Phrase(String.format(Locale.ENGLISH, "%.2f", dStock), fontDarkGray7Normal);
					dTotalQty[9] += info.dEndAdj;
					table.addCell(phrase);
				}
				else
				{
					int nCaseValue = getCaseValue(info.strItemNo);
					if (info.nSCWType == Global.INVENTORY_CONTAINERS) {
						continue;
					}
					if(info.nBeginQty == 0 && info.nLoaded == 0 && info.nSpoils == 0 && info.nStock == 0 && info.nSales == 0 && info.nBuybacks == 0 && info.nDumps == 0 && info.nUnload == 0 && info.nDumpsUnload == 0 )
					{
						continue;
					}
					phrase = new Phrase(Utils.getDisplayItemCode(mContext, info.strItemNo), fontDarkGray7Normal);
					table.addCell(phrase);

					phrase = new Phrase(info.strShortDesc, fontDarkGray7Normal);
					table.addCell(phrase);

					phrase = new Phrase(info.strUOM, fontDarkGray7Normal);
					table.addCell(phrase);

					phrase = new Phrase(getCaseUnitFormat(info.nBeginQty, nCaseValue), fontDarkGray7Normal);
					if(bShowCase)
					{
						nTotals1[0] += (info.nBeginQty / nCaseValue);
						nTotalQty[0] += (info.nBeginQty % nCaseValue);
					}
					else
					{
						nTotalQty[0] += info.nBeginQty;
					}

					table.addCell(phrase);

					phrase = new Phrase(getCaseUnitFormat(info.nLoaded + info.nLoadAdj, nCaseValue), fontDarkGray7Normal);
					if(bShowCase)
					{
						nTotals1[1] += ((info.nLoaded + info.nLoadAdj) / nCaseValue);
						nTotalQty[1] += ((info.nLoaded + info.nLoadAdj) % nCaseValue);
					}
					else
					{
						nTotalQty[1] += (info.nLoaded + info.nLoadAdj);
					}

					table.addCell(phrase);

					phrase = new Phrase(getCaseUnitFormat(info.nTransIn - info.nTransOut, nCaseValue), fontDarkGray7Normal);
					if(bShowCase)
					{
						nTotals1[2] += ((info.nTransIn - info.nTransOut) / nCaseValue);
						nTotalQty[2] += ((info.nTransIn - info.nTransOut) % nCaseValue);
					}
					else
					{
						nTotalQty[2] += ((info.nTransIn - info.nTransOut));
					}

					table.addCell(phrase);

					phrase = new Phrase(getCaseUnitFormat(info.nSales, nCaseValue), fontDarkGray7Normal);
					if(bShowCase)
					{
						nTotals1[3] += (info.nSales / nCaseValue);
						nTotalQty[3] += (info.nSales % nCaseValue);
					}
					else
					{
						nTotalQty[3] += info.nSales;
					}


					table.addCell(phrase);

					phrase = new Phrase(getCaseUnitFormat(info.nBuybacks, nCaseValue), fontDarkGray7Normal);
					if(bShowCase)
					{
						nTotals1[4] += (info.nBuybacks / nCaseValue);
						nTotalQty[4] += (info.nBuybacks % nCaseValue);
					}
					else
					{
						nTotalQty[4] += info.nBuybacks;
					}

					table.addCell(phrase);

					phrase = new Phrase(getCaseUnitFormat(info.nDumps, nCaseValue), fontDarkGray7Normal);
					if(bShowCase)
					{
						nTotals1[5] += (info.nDumps / nCaseValue);
						nTotalQty[5] += (info.nDumps % nCaseValue);
					}
					else
					{
						nTotalQty[5] += info.nDumps;
					}

					table.addCell(phrase);

					phrase = new Phrase(getCaseUnitFormat(info.nUnload + info.nSpoils, nCaseValue), fontDarkGray7Normal);
					if(bShowCase)
					{
						nTotals1[6] += ((info.nUnload + info.nSpoils) / nCaseValue);
						nTotalQty[6] += ((info.nUnload + info.nSpoils) % nCaseValue);
					}
					else
					{
						nTotalQty[6] += info.nUnload + info.nSpoils;
					}

					table.addCell(phrase);

					phrase = new Phrase(getCaseUnitFormat(info.nDumpsUnload, nCaseValue), fontDarkGray7Normal);
					if(bShowCase)
					{
						nTotals1[7] += ((info.nDumpsUnload) / nCaseValue);
						nTotalQty[7] += ((info.nDumpsUnload) % nCaseValue);
					}
					else
					{
						nTotalQty[7] += info.nDumpsUnload;
					}

					table.addCell(phrase);

					int nStock = info.nStock + info.nSales - info.nBuybacks - info.nBeginQty - info.nLoaded - info.nLoadAdj - info.nUnload - info.nSpoils;
					phrase = new Phrase(getCaseUnitFormat(nStock, nCaseValue), fontDarkGray7Normal);
					if(bShowCase)
					{
						nTotals1[8] += ((nStock) / nCaseValue);
						nTotalQty[8] += ((nStock) % nCaseValue);
					}
					else
					{
						nTotalQty[8] += nStock;
					}

					table.addCell(phrase);

					//info.nEndAdj = info.nBeginQty + info.nLoaded + info.nLoadAdj + info.nTransIn - info.nTransOut - info.nSales + info.nBuybacks /*- info.nDumps*/ - info.nUnload - info.nSpoils/*+ info.nDumpsUnload*/;
					info.nEndAdj = nStock;
					nStock = info.nStock /*- info.nDumps*/ - info.nUnload - info.nSpoils/*+ info.nDumpsUnload*/;
					if(bShowCase)
					{
						nTotals1[9] += (nStock / nCaseValue);
						nTotalQty[9] += (nStock % nCaseValue);
                        nEndTotalCount += nStock;
                        phrase = new Phrase(getCaseUnitFormat(nStock, nCaseValue)+"["+ nStock +"]", fontDarkGray7Normal);
					}
					else
					{
						nTotalQty[9] += nStock;
                        phrase = new Phrase(getCaseUnitFormat(nStock, nCaseValue), fontDarkGray7Normal);
					}
					table.addCell(phrase);
				}
			}

			cell = new PdfPCell();
			cell.setColspan(3);
			cell.setBorder(Rectangle.NO_BORDER);
			table.addCell(cell);

			for (int i = 0; i < 10; i++) {
				phrase = new Phrase("-----", fontDarkGray7Normal);
				table.addCell(phrase);
			}

			phrase = new Phrase("Totals: ", fontBlack7Bold);
			cell = new PdfPCell(phrase);
			cell.setColspan(3);
			cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
			cell.setBorder(Rectangle.NO_BORDER);
			cell.setPaddingRight(30);
			table.addCell(cell);

			for (int i = 0; i < 10; i++) {
				if((nTotals1[i]!= 0 || nTotalQty[i] != 0) && dTotalQty[i] != 0)
				{
					if(bShowCase)
					{
						phrase = new Phrase(nTotals1[i] + "/" + nTotalQty[i] + "\n" + String.format(Locale.ENGLISH, "%.2f", dTotalQty[i]), fontBlack7Bold);
					}
					else
					{
						phrase = new Phrase(nTotalQty[i] + "\n" + String.format(Locale.ENGLISH, "%.2f", dTotalQty[i]), fontBlack7Bold);
					}

				}
				else if( dTotalQty[i] != 0)
				{
					phrase = new Phrase(String.format(Locale.ENGLISH, "%.2f", dTotalQty[i]), fontBlack7Bold);
				}
				else
				{
					if(bShowCase)
					{
                        if (i==9) {
                            phrase = new Phrase(nTotals1[i] + "/" + nTotalQty[i]+"["+nEndTotalCount+"]" , fontBlack7Bold);
                        }
                        else {
                            phrase = new Phrase(nTotals1[i] + "/" + nTotalQty[i] , fontBlack7Bold);
                        }
					}
					else
					{
						phrase = new Phrase(String.valueOf(nTotalQty[i]) , fontBlack7Bold);
					}
				}
				table.addCell(phrase);
			}

			int nCnt = 0;
			for (UInvenDInfo info : mUInvenHInfo.uInvenDList) {
				if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
				{
					if(info.dBeginQty == 0 && info.dLoaded == 0 && info.dStock == 0 && info.dSales == 0 && info.dBuybacks == 0 && info.dDumps == 0 && info.dUnload == 0 && info.dSpoils == 0 && info.dDumpsUnload == 0 )
						continue;
				}
				else
				{
					if(info.nBeginQty == 0 && info.nLoaded == 0 && info.nStock == 0 && info.nSales == 0 && info.nBuybacks == 0 && info.nDumps == 0 && info.nUnload == 0 && info.nDumpsUnload == 0 && info.nSpoils == 0 )
						continue;
				}
				if (info.nSCWType == Global.INVENTORY_CONTAINERS) {
					nCnt++;
				}
			}
			if (nCnt > 0) {
				phrase = new Phrase("Containers", fontBlack7Bold);
				cell = new PdfPCell(phrase);
				cell.setColspan(13);
				cell.setBorder(Rectangle.NO_BORDER);
				cell.setPadding(5);
				table.addCell(cell);

				int nTotals2[] = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
				for (UInvenDInfo info : mUInvenHInfo.uInvenDList) {
					if (info.nSCWType == Global.INVENTORY_TRUCK || info.nSCWType == Global.WEIGHT_ITEM) {
						continue;
					}
					if(info.nBeginQty == 0 && info.nLoaded == 0 && info.nStock == 0 && info.nSales == 0 && info.nBuybacks == 0 && info.nDumps == 0 && info.nUnload == 0 && info.nDumpsUnload == 0 && info.nSpoils == 0)
						continue;
					phrase = new Phrase(Utils.getDisplayItemCode(mContext, info.strItemNo), fontDarkGray7Normal);
					table.addCell(phrase);

					phrase = new Phrase(info.strShortDesc, fontDarkGray7Normal);
					table.addCell(phrase);

					phrase = new Phrase(info.strUOM, fontDarkGray7Normal);
					table.addCell(phrase);

					phrase = new Phrase(String.valueOf(info.nBeginQty), fontDarkGray7Normal);
					nTotals2[0] += info.nBeginQty;
					table.addCell(phrase);

					phrase = new Phrase(String.valueOf(info.nLoaded - info.nLoadAdj), fontDarkGray7Normal);
					nTotals2[1] += info.nLoaded - info.nLoadAdj;
					table.addCell(phrase);

					phrase = new Phrase(String.valueOf(info.nTransIn - info.nTransOut), fontDarkGray7Normal);
					nTotals2[2] += (info.nTransIn - info.nTransOut);
					table.addCell(phrase);

					phrase = new Phrase(String.valueOf(info.nSales), fontDarkGray7Normal);
					nTotals2[3] += info.nSales;
					table.addCell(phrase);

					phrase = new Phrase(String.valueOf(info.nBuybacks), fontDarkGray7Normal);
					nTotals2[4] += info.nBuybacks;
					table.addCell(phrase);

					phrase = new Phrase(String.valueOf(info.nDumps), fontDarkGray7Normal);
					nTotals2[5] += info.nDumps;
					table.addCell(phrase);

					phrase = new Phrase(String.valueOf(info.nUnload + info.nSpoils), fontDarkGray7Normal);
					nTotals1[6] += info.nUnload + info.nSpoils;
					table.addCell(phrase);

					phrase = new Phrase(String.valueOf(info.nDumpsUnload), fontDarkGray7Normal);
					nTotals1[7] += info.nDumpsUnload;
					table.addCell(phrase);

					int nStock = info.nStock + info.nSales - info.nBuybacks - info.nBeginQty - info.nLoaded - info.nLoadAdj - info.nUnload - info.nSpoils;
					phrase = new Phrase(String.valueOf(nStock), fontDarkGray7Normal);
					nTotals1[8] += nStock;
					table.addCell(phrase);

					//info.nEndAdj = info.nBeginQty + info.nLoaded - info.nLoadAdj + info.nTransIn - info.nTransOut - info.nSales + info.nBuybacks /*+ info.nDumps*/ - info.nUnload - info.nSpoils/*+ info.nDumpsUnload*/;
					info.nEndAdj = nStock;
					nStock = info.nStock /*- info.nDumps*/ - info.nUnload - info.nSpoils/*+ info.nDumpsUnload*/;
					phrase = new Phrase(String.valueOf(nStock), fontDarkGray7Normal);
					nTotals2[9] += nStock;
					table.addCell(phrase);
				}

				cell = new PdfPCell();
				cell.setColspan(3);
				cell.setBorder(Rectangle.NO_BORDER);
				table.addCell(cell);

				for (int i = 0; i < 10; i++) {
					phrase = new Phrase("-----", fontDarkGray7Normal);
					table.addCell(phrase);
				}

				phrase = new Phrase("Totals: ", fontBlack7Bold);
				cell = new PdfPCell(phrase);
				cell.setColspan(3);
				cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				cell.setBorder(Rectangle.NO_BORDER);
				cell.setPaddingRight(30);
				table.addCell(cell);

				for (int i = 0; i < 10; i++) {
					phrase = new Phrase(String.valueOf(nTotals2[i]), fontBlack7Bold);
					table.addCell(phrase);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return table;
	}

	private void createPDF() {
		try {
			Document document = new Document(PageSize.A4, 0, 0, 10, 10);
			PdfWriter.getInstance(document, new FileOutputStream(Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + mStrPdfFileName));
			document.open();
			PdfPTable table = createHeader();
			document.add(table);
			table = createBody();
			table.setSpacingBefore(20);
			document.add(table);
			document.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void doComplete() {
		String strDocNo = mStrPdfFileName.substring(0, mStrPdfFileName.length() - 4);
		long nTrxnNo = setGeneralUploadData("EINV", 0, 0, -1, "", strDocNo);

		TransactionInfo transactionInfo = new TransactionInfo();
		transactionInfo.strType = "EINV";
		transactionInfo.strTime = new SimpleDateFormat("kk:mm").format(new Date(nTrxnNo));
		transactionInfo.strDocNo = strDocNo;
		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();

		//Register Print document list.
		PrintItem printItem = new PrintItem();
		printItem.nTrxnNo = nTrxnNo;
		printItem.strDocNo = strDocNo;
		printItem.strDocType = "EINV";
		printItem.strFilePath = Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + mStrPdfFileName;
		printItem.nCustNo = 0;
		printItem.nChainNo = 0;
		printItem.strCustomerName = "";
		dbAdapter.insertPrint(printItem);

		dbAdapter.insertTransactionHistoryInfo(transactionInfo);
		dbAdapter.close();

		UTransactionsInfo uTransactionsInfo = setUTransactionsLastInfo(nTrxnNo + 1, "FARC", mStrPdfFileName);
		setUFileTransactionInfo(uTransactionsInfo.nTrxnNo, 0, 0, mPdfTrxnNo, strDocNo, "ENDINVENTORY", "End of Trip Inventory Report", uTransactionsInfo.strTrxnDate, uTransactionsInfo.strTrxnTime, mStrPdfFileName);

/*		if (mStrSignatureFilePath.length() > 0) {
			String strReference = String.format(Locale.ENGLISH, "%s_Signature_%d.jpg", new SimpleDateFormat(Global.DATE_YYYYMMDDHHMMSS_FORMAT).format(new Date()), 0);
			setUCameraData("SIG", mStrSignatureFilePath, strReference);
		}*/
		if (mUInvenHInfo != null) {
			long nOldTrxnNo = mUInvenHInfo.nTrxnNo;
			mUInvenHInfo.nTrxnNo = nTrxnNo;
			mUInvenHInfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(mContext, mTrxnDate));
			mUInvenHInfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(mTrxnDate);
			mUInvenHInfo.strDocType = "EINV";
			if(mTemperatureIdx >= 0 && mTemperatureIdx < mTemperatureList.size())
			{
				String strTempFmt = Utils.getStringSetting(mContext, Global.PREF_TEMPFMT);
				if(TextUtils.isEmpty(strTempFmt) || strTempFmt.endsWith("C"))
				{
					mUInvenHInfo.strReference = String.format(Locale.ENGLISH, "%.1f", mTemperatureList.get(mTemperatureIdx));
				}
				else
				{
					mUInvenHInfo.strReference = String.format(Locale.ENGLISH, "%d", (int)mTemperatureList.get(mTemperatureIdx).floatValue());
				}
			}
			else
			{
				mUInvenHInfo.strReference = "";
			}

			dbAdapter = new DbAdapter(mContext);
			dbAdapter.open();
			dbAdapter.insertUInvenHInfo(mUInvenHInfo);

			UInvenBlotInfo uInvenBlotInfo = Utils.getUInvenBlot(mUInvenHInfo, mStrPdfFileName.substring(0, mStrPdfFileName.length() - 4), Global.gTripNumber);
			List<UInvenBlotDInfo> uInvenBlotDInfoList = new ArrayList<>();

			int nStock1Cnt = 0;
			int nStockCnt = 0;
			for (UInvenDInfo info : mUInvenHInfo.uInvenDList)
			{
				if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
				{
					info.dStock -= info.dSpoils;
					info.dStock -= info.dUnload;
					info.dDumps -= info.dDumpsUnload;
					if( info.nTrxnNo == 0 && info.dUnload == 0 && info.dDumpsUnload == 0 && info.dSpoils == 0 )
						continue;

					if( info.nTrxnNo == 0 )
					{
						info.nTrxnNo = nOldTrxnNo;
						dbAdapter.insertUInvenDInfo(info);
					}
					else
					{
						dbAdapter.updateUInvenDInfo(info);
					}
					info.nTrxnNo = nTrxnNo;
					info.nIdx = (int)dbAdapter.insertUInvenDInfo(info);
					uInvenBlotDInfoList.addAll(Utils.getUInvenBlotList(info));
					if(info.dBeginQty == 0 && info.dLoaded == 0 && info.dStock == 0 && info.dSales == 0 &&
							info.dBuybacks == 0 && info.dDumps == 0 && info.dUnload == 0 && info.dDumpsUnload == 0 &&
							info.dSpoils == 0)
						continue;

					if( info.dDumpsUnload > 0) {
						UStockDInfo dInfo = new UStockDInfo();
						dInfo.nTrxnNo = m_lUnloadTrxnNo;
						dInfo.strDetailType = "D";
						dInfo.dActualQty = info.dDumpsUnload;
						dInfo.dBeginQty = info.dDumps + info.dDumpsUnload;
						dInfo.strItemNo = info.strItemNo;
						dbAdapter.insertUStockDInfoList(dInfo);

						if(Utils.getIntSetting(mContext, Global.PREF_LOAD_CUSTOMER) != 1)
						{
							if(info.bLotControl && info.loadBlotInfoList != null && info.loadBlotInfoList.size() > 0)
							{
								for(LoadBlotInfo loadBlotInfo : info.loadBlotInfoList) {
									BatchLot batchLot = new BatchLot();
									batchLot.nTrxnNo = dInfo.nTrxnNo;
									batchLot.strItemNo = info.strItemNo;
									batchLot.nLocnNo = dInfo.nLocnNo;
									batchLot.nReasonCode = dInfo.nReasonCode;
									batchLot.nSequence = dInfo.nSequence;
									batchLot.strLotNumber = loadBlotInfo.strLotNumber;
									batchLot.strBatchDate = loadBlotInfo.strBatchDate;
									batchLot.nActualQty = loadBlotInfo.nLotQty;
									batchLot.nSuggestQty = loadBlotInfo.nLotQty;
									dbAdapter.insertBatchLot(batchLot);
								}
							}
						}

						nStockCnt++;
					}
					if(info.dUnload > 0 ) {
						UStockDInfo dInfo = new UStockDInfo();
						dInfo.nTrxnNo = m_lUnloadTrxnNo;
						dInfo.strDetailType = "F";
						dInfo.dActualQty = info.dUnload;
						dInfo.dBeginQty = info.dStock + info.dUnload;
						dInfo.strItemNo = info.strItemNo;
						dbAdapter.insertUStockDInfoList(dInfo);

						if(Utils.getIntSetting(mContext, Global.PREF_LOAD_CUSTOMER) != 1)
						{
							if(info.bLotControl && info.loadBlotInfoList != null && info.loadBlotInfoList.size() > 0)
							{
								for(LoadBlotInfo loadBlotInfo : info.loadBlotInfoList) {
									BatchLot batchLot = new BatchLot();
									batchLot.nTrxnNo = dInfo.nTrxnNo;
									batchLot.strItemNo = info.strItemNo;
									batchLot.nLocnNo = dInfo.nLocnNo;
									batchLot.nReasonCode = dInfo.nReasonCode;
									batchLot.nSequence = dInfo.nSequence;
									batchLot.strLotNumber = loadBlotInfo.strLotNumber;
									batchLot.strBatchDate = loadBlotInfo.strBatchDate;
									batchLot.nActualQty = loadBlotInfo.nLotQty;
									batchLot.nSuggestQty = loadBlotInfo.nLotQty;
									dbAdapter.insertBatchLot(batchLot);
								}
							}
						}
						nStockCnt++;
					}
					if(info.dSpoils > 0 ) {
						UStockDInfo dInfo = new UStockDInfo();
						dInfo.nTrxnNo = m_lUnloadTrxnNo;
						dInfo.strDetailType = "S";
						dInfo.dActualQty = info.dSpoils;
						dInfo.dBeginQty = 0;
						dInfo.nReasonCode = info.nReasonCode;
						dInfo.strItemNo = info.strItemNo;
						dbAdapter.insertUStockDInfoList(dInfo);

						if(Utils.getIntSetting(mContext, Global.PREF_LOAD_CUSTOMER) != 1)
						{
							if(info.bLotControl && info.loadBlotInfoList != null && info.loadBlotInfoList.size() > 0)
							{
								for(LoadBlotInfo loadBlotInfo : info.loadBlotInfoList) {
									BatchLot batchLot = new BatchLot();
									batchLot.nTrxnNo = dInfo.nTrxnNo;
									batchLot.strItemNo = info.strItemNo;
									batchLot.nLocnNo = dInfo.nLocnNo;
									batchLot.nReasonCode = dInfo.nReasonCode;
									batchLot.nSequence = dInfo.nSequence;
									batchLot.strLotNumber = loadBlotInfo.strLotNumber;
									batchLot.strBatchDate = loadBlotInfo.strBatchDate;
									batchLot.nActualQty = loadBlotInfo.nLotQty;
									batchLot.nSuggestQty = loadBlotInfo.nLotQty;
									dbAdapter.insertBatchLot(batchLot);
								}
							}
						}
						nStockCnt++;
					}

					if( info.dStock > 0 || info.dUnload > 0 )
					{
						UStockDInfo dInfo1 = new UStockDInfo();
						dInfo1.nTrxnNo = nTrxnNo;
						dInfo1.strItemNo = info.strItemNo;
						dInfo1.strDetailType = "";
						dInfo1.dActualQty = info.dStock;
						dInfo1.dBeginQty = info.dUnload;
						dbAdapter.insertUStockDInfoList(dInfo1);

						if(Utils.getIntSetting(mContext, Global.PREF_LOAD_CUSTOMER) != 1)
						{
							if(info.bLotControl && info.loadBlotInfoList != null && info.loadBlotInfoList.size() > 0)
							{
								for(LoadBlotInfo loadBlotInfo : info.loadBlotInfoList) {
									BatchLot batchLot = new BatchLot();
									batchLot.nTrxnNo = dInfo1.nTrxnNo;
									batchLot.strItemNo = info.strItemNo;
									batchLot.nLocnNo = dInfo1.nLocnNo;
									batchLot.nReasonCode = dInfo1.nReasonCode;
									batchLot.nSequence = dInfo1.nSequence;
									batchLot.strLotNumber = loadBlotInfo.strLotNumber;
									batchLot.strBatchDate = loadBlotInfo.strBatchDate;
									batchLot.nActualQty = loadBlotInfo.nLotQty;
									batchLot.nSuggestQty = loadBlotInfo.nLotQty;
									dbAdapter.insertBatchLot(batchLot);
								}
							}
						}
						nStock1Cnt++;
					}
				}
				else
				{
					info.nStock -= info.nSpoils;
					info.nStock -= info.nUnload;
					info.nDumps -= info.nDumpsUnload;
					if( info.nTrxnNo == 0 && info.nUnload == 0 && info.nDumpsUnload == 0 && info.nSpoils == 0 )
						continue;

					if( info.nTrxnNo == 0 )
					{
						info.nTrxnNo = nOldTrxnNo;
						dbAdapter.insertUInvenDInfo(info);
					}
					else
					{
						dbAdapter.updateUInvenDInfo(info);
					}
					info.nTrxnNo = nTrxnNo;
					info.nIdx = (int)dbAdapter.insertUInvenDInfo(info);
					uInvenBlotDInfoList.addAll(Utils.getUInvenBlotList(info));
					if(info.nBeginQty == 0 && info.nLoaded == 0 && info.nStock == 0 && info.nSales == 0 &&
							info.nBuybacks == 0 && info.nDumps == 0 && info.nUnload == 0 && info.nDumpsUnload == 0 &&
							info.nSpoils == 0)
						continue;

					if( info.nDumpsUnload > 0) {
						UStockDInfo dInfo = new UStockDInfo();
						dInfo.nTrxnNo = m_lUnloadTrxnNo;
						dInfo.strDetailType = "D";
						dInfo.nActualQty = info.nDumpsUnload;
						dInfo.nBeginQty = info.nDumps + info.nDumpsUnload;
						dInfo.strItemNo = info.strItemNo;
						dbAdapter.insertUStockDInfoList(dInfo);

						if(Utils.getIntSetting(mContext, Global.PREF_LOAD_CUSTOMER) != 1)
						{
							if(info.bLotControl && info.loadBlotInfoList != null && info.loadBlotInfoList.size() > 0)
							{
								for(LoadBlotInfo loadBlotInfo : info.loadBlotInfoList) {
									BatchLot batchLot = new BatchLot();
									batchLot.nTrxnNo = dInfo.nTrxnNo;
									batchLot.strItemNo = info.strItemNo;
									batchLot.nLocnNo = dInfo.nLocnNo;
									batchLot.nReasonCode = dInfo.nReasonCode;
									batchLot.nSequence = dInfo.nSequence;
									batchLot.strLotNumber = loadBlotInfo.strLotNumber;
									batchLot.strBatchDate = loadBlotInfo.strBatchDate;
									batchLot.nActualQty = loadBlotInfo.nLotQty;
									batchLot.nSuggestQty = loadBlotInfo.nLotQty;
									dbAdapter.insertBatchLot(batchLot);
								}
							}
						}
						nStockCnt++;
					}
					if(info.nUnload > 0 ) {
						UStockDInfo dInfo = new UStockDInfo();
						dInfo.nTrxnNo = m_lUnloadTrxnNo;
						dInfo.strDetailType = "F";
						dInfo.nActualQty = info.nUnload;
						dInfo.nBeginQty = info.nStock + info.nUnload;
						dInfo.strItemNo = info.strItemNo;
						dbAdapter.insertUStockDInfoList(dInfo);

						if(Utils.getIntSetting(mContext, Global.PREF_LOAD_CUSTOMER) != 1)
						{
							if(info.bLotControl && info.loadBlotInfoList != null && info.loadBlotInfoList.size() > 0)
							{
								for(LoadBlotInfo loadBlotInfo : info.loadBlotInfoList) {
									BatchLot batchLot = new BatchLot();
									batchLot.nTrxnNo = dInfo.nTrxnNo;
									batchLot.strItemNo = info.strItemNo;
									batchLot.nLocnNo = dInfo.nLocnNo;
									batchLot.nReasonCode = dInfo.nReasonCode;
									batchLot.nSequence = dInfo.nSequence;
									batchLot.strLotNumber = loadBlotInfo.strLotNumber;
									batchLot.strBatchDate = loadBlotInfo.strBatchDate;
									batchLot.nActualQty = loadBlotInfo.nLotQty;
									batchLot.nSuggestQty = loadBlotInfo.nLotQty;
									dbAdapter.insertBatchLot(batchLot);
								}
							}
						}
						nStockCnt++;
					}
					if(info.nSpoils > 0 ) {
						UStockDInfo dInfo = new UStockDInfo();
						dInfo.nTrxnNo = m_lUnloadTrxnNo;
						dInfo.strDetailType = "S";
						dInfo.nActualQty = info.nSpoils;
						dInfo.nBeginQty = 0;
						dInfo.nReasonCode = info.nReasonCode;
						dInfo.strItemNo = info.strItemNo;
						dbAdapter.insertUStockDInfoList(dInfo);

						if(Utils.getIntSetting(mContext, Global.PREF_LOAD_CUSTOMER) != 1)
						{
							if(info.bLotControl && info.loadBlotInfoList != null && info.loadBlotInfoList.size() > 0)
							{
								for(LoadBlotInfo loadBlotInfo : info.loadBlotInfoList) {
									BatchLot batchLot = new BatchLot();
									batchLot.nTrxnNo = dInfo.nTrxnNo;
									batchLot.strItemNo = info.strItemNo;
									batchLot.nLocnNo = dInfo.nLocnNo;
									batchLot.nReasonCode = dInfo.nReasonCode;
									batchLot.nSequence = dInfo.nSequence;
									batchLot.strLotNumber = loadBlotInfo.strLotNumber;
									batchLot.strBatchDate = loadBlotInfo.strBatchDate;
									batchLot.nActualQty = loadBlotInfo.nLotQty;
									batchLot.nSuggestQty = loadBlotInfo.nLotQty;
									dbAdapter.insertBatchLot(batchLot);
								}
							}
						}
						nStockCnt++;
					}

					if( info.nStock > 0 || info.nUnload > 0 )
					{
						UStockDInfo dInfo1 = new UStockDInfo();
						dInfo1.nTrxnNo = nTrxnNo;
						dInfo1.strItemNo = info.strItemNo;
						dInfo1.strDetailType = "";
						dInfo1.nActualQty = info.nStock;
						dInfo1.nBeginQty = info.nUnload;
						dbAdapter.insertUStockDInfoList(dInfo1);

						if(Utils.getIntSetting(mContext, Global.PREF_LOAD_CUSTOMER) != 1)
						{
							if(info.bLotControl && info.loadBlotInfoList != null && info.loadBlotInfoList.size() > 0)
							{
								for(LoadBlotInfo loadBlotInfo : info.loadBlotInfoList) {
									BatchLot batchLot = new BatchLot();
									batchLot.nTrxnNo = dInfo1.nTrxnNo;
									batchLot.strItemNo = info.strItemNo;
									batchLot.nLocnNo = dInfo1.nLocnNo;
									batchLot.nReasonCode = dInfo1.nReasonCode;
									batchLot.nSequence = dInfo1.nSequence;
									batchLot.strLotNumber = loadBlotInfo.strLotNumber;
									batchLot.strBatchDate = loadBlotInfo.strBatchDate;
									batchLot.nActualQty = loadBlotInfo.nLotQty;
									batchLot.nSuggestQty = loadBlotInfo.nLotQty;
									dbAdapter.insertBatchLot(batchLot);
								}
							}
						}
						nStock1Cnt++;
					}
				}

			}
			if(nStockCnt > 0)
			{
				UStockHInfo uStockHInfo = new UStockHInfo();
				uStockHInfo.nTrxnNo = m_lUnloadTrxnNo;
				uStockHInfo.strDocType = "UNLD";
				try
				{
					uStockHInfo.strDocNo = mStrOffLoadPdfFileName.substring(0, mStrOffLoadPdfFileName.length() - 4);
					uStockHInfo.strReference = uStockHInfo.strDocNo + ".pdf";
				}catch(Exception e)
				{
					e.printStackTrace();
				}
				uStockHInfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(mContext, new Date(m_lUnloadTrxnNo)));
				uStockHInfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(m_lUnloadTrxnNo);
				uStockHInfo.strStockType = "6";
				uStockHInfo.strTransferRoute = Utils.getStringSetting(mContext, Global.PREF_ROUTE_NUMBER);
				dbAdapter.insertUStockHInfoList(uStockHInfo);
			}
			if( nStock1Cnt > 0 )
			{
				UStockHInfo uStockHInfo1 = new UStockHInfo();
				uStockHInfo1.strDocType = "EINV";
				uStockHInfo1.nTrxnNo = nTrxnNo;
				try
				{
					uStockHInfo1.strDocNo = mStrPdfFileName.substring(0, mStrPdfFileName.length() - 4);
					uStockHInfo1.strReference = uStockHInfo1.strDocNo + ".pdf";
				}catch(Exception e)
				{
					e.printStackTrace();
				}
				uStockHInfo1.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(mContext, new Date(nTrxnNo)));
				uStockHInfo1.strTrxnTime =  new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(nTrxnNo);
				uStockHInfo1.strStockType = "9";
				uStockHInfo1.strTransferRoute = Utils.getStringSetting(mContext, Global.PREF_ROUTE_NUMBER);
				dbAdapter.insertUStockHInfoList(uStockHInfo1);
			}

			if(uInvenBlotDInfoList.size() > 0)
			{
				dbAdapter.insertULoadBlot(uInvenBlotInfo);
				dbAdapter.insertULoadBlotList(uInvenBlotDInfoList);
			}
			dbAdapter.close();
			AddTrxnNo(0, 0, m_lUnloadTrxnNo);
		}
		Utils.setBooleanSetting(mContext, String.format(Locale.ENGLISH, Global.PREF_END_TRIP_CHECKED, 1), true);
		//Global.gUploadCustNoList.add(0);
		if(bPaused)
		{
			bNeedBack = true;
		}
		else
		{
			new Handler(Looper.getMainLooper()).post(new Runnable() {
				@Override
				public void run() {
					Fragment fragment = mContext.getSupportFragmentManager().findFragmentById(R.id.frameContainer);
					while(!(fragment instanceof TripSettlementFragment))
					{
						mContext.onBackPressed();
						fragment = mContext.getSupportFragmentManager().findFragmentById(R.id.frameContainer);
					}

					m_bCompletedClicked = false;
				}
			});
		}
	}

	boolean bPaused = true;
	boolean bNeedBack = false;

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
							printer.sendFileContents(Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + mStrOffLoadPdfFileName);
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
				if (result.length() == 0)
				{
					Utils.setStringSetting(mContext, Global.PREF_PRINTER_MAC_ADDRESS, mStrMacAddress);
					Utils.printDebug(mContext, Str.SuccessToPrint[Global.LANG]);
					doProcessCompleted();
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
								doProcessCompleted();
							}
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