package dsd.dsdvansales.fragment;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.*;
import android.graphics.Color;
import android.os.*;
import android.text.*;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

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

import java.io.*;
import java.text.*;
import java.util.*;
import java.util.concurrent.CountDownLatch;

import dsd.dsdvansales.R;
import dsd.dsdvansales.db.DbAdapter;
import dsd.dsdvansales.dialogs.MessageDialog;
import dsd.dsdvansales.dialogs.PresoldDialog;
import dsd.dsdvansales.global.BixolonPrinter;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.HoneywellLinePrinter;
import dsd.dsdvansales.global.HoneywellPrinter;
import dsd.dsdvansales.global.PrintEngine;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.model.ARHeaderInfo;
import dsd.dsdvansales.model.CustomerInfo;
import dsd.dsdvansales.model.PrintItem;
import dsd.dsdvansales.model.TransactionInfo;
import dsd.dsdvansales.model.UCollectionsDInfo;
import dsd.dsdvansales.model.UTransactionsInfo;
import dsd.dsdvansales.views.AlertSingleChoiceAdapter;

public class CollectionsBalancingFragment extends BaseFragment implements View.OnClickListener {

	private Font fontDarkGray7Normal;
	private Font fontBlack7Bold;
	private Font fontBlack10Bold;

	private String mStrPdfFileName = "";
	private long mPdfTrxnNo = 0;

	private List<UCollectionsDInfo> mUCollectionsDList = new ArrayList<>();
	private float mCashTotal = 0;
	private float mChequeTotal = 0;
	private float mCardTotal = 0;
	private float mTotal = 0;

	private String mStrTemplatePdfFileName = "";

	private Connection printerConnection = null;
	private String mStrMacAddress = "";
	private String mStrFriendlyName = "";

	private int mPrintType = 0; // 0 --- Print Banking Summary Information. 1--- Print Template File.


	ListView listView;
	ContentListAdapter listAdapter;
	List<UCollectionsDInfo> mCashList = new ArrayList<>();
	List<UCollectionsDInfo> mChequeList = new ArrayList<>();
	List<UCollectionsDInfo> mCardList = new ArrayList<>();

	boolean mDetailedMode = false;

	PrintEngine mHoneywellPrintEngine = null;
	String mHoneywellTemplatePath = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootView = inflater.inflate(R.layout.fragment_collections_balancing, container, false);
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
		Global.gFragIdx = Global.FRAG_COLLECTIONS_BALANCING;
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
		mRootView.findViewById(R.id.btnConfirm).setOnClickListener(this);
		mRootView.findViewById(R.id.btnBack).setOnClickListener(this);
		mRootView.findViewById(R.id.btnSummary).setOnClickListener(this);
		mRootView.findViewById(R.id.btnPrint).setOnClickListener(this);

		listView = (ListView)mRootView.findViewById(R.id.contentList);
		listAdapter = new ContentListAdapter();
		listView.setAdapter(listAdapter);

		String strTemplateFileName = Utils.getFinalPrintTemplateFileName(mContext, Global.PRINT_COLLECTION_REPORT_FILENAME);
		if(Utils.isFileExist(Global.ROOT_LOCATION + "/" + strTemplateFileName))
		{
			mRootView.findViewById(R.id.btnPrint).setVisibility(View.VISIBLE);
		}
		else
		{
			mRootView.findViewById(R.id.btnPrint).setVisibility(View.INVISIBLE);
		}

		loadData();
		refreshLanguage();
	}

	@Override
	public void loadData() {
		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		mUCollectionsDList = dbAdapter.getUCollectionsDList();
		dbAdapter.close();

		mCashList.clear();
		mChequeList.clear();
		mCardList.clear();

		for (UCollectionsDInfo info : mUCollectionsDList) {
			if (info.nPayType == Global.COLLECTION_CASH) {
				mCashTotal += info.nTrxnAmount;
				mCashList.add(info);
			} else if (info.nPayType == Global.COLLECTION_CHEQUE) {
				mChequeTotal += info.nTrxnAmount;
				mChequeList.add(info);
			} else if (info.nPayType == Global.COLLECTION_CARD) {
				mCardTotal += info.nTrxnAmount;
				mCardList.add(info);
			}
		}
		mTotal = mCashTotal + mChequeTotal + mCardTotal;
	}

	@Override
	public void refreshPage() {
	}

	@Override
	public void refreshLanguage() {
		((TextView) mRootView.findViewById(R.id.lblTitle)).setText(Str.CollectionsBalancing[Global.LANG].toUpperCase());
		((TextView) mRootView.findViewById(R.id.lblDescription)).setText(String.format(Locale.ENGLISH, Str.CollectionsBalancing_desc[Global.LANG], Utils.getFormattedPrice(mContext, mTotal)));
		((Button) mRootView.findViewById(R.id.btnConfirm)).setText(Str.Done[Global.LANG]);

		if(mDetailedMode)
		{
			((Button) mRootView.findViewById(R.id.btnSummary)).setText(Str.Summary[Global.LANG]);
		}
		else
		{
			((Button) mRootView.findViewById(R.id.btnSummary)).setText(Str.Detail[Global.LANG]);
		}
		listAdapter.notifyDataSetChanged();
	}

	@Override
	public void receiveBroadcast(String strAction, Intent intent) {
	}

	private void setAmount(String strVal, EditText editText, int nType) {

		String strArray[] = strVal.split("\\.");
		strVal = "";
		for (int i=0; i<strArray.length; i++) {
			strVal += strArray[i];
		}
		float fValue = Utils.parseFloatFromString(strVal) / 100;
		if(nType == 0)
		{
			mCashTotal = fValue;
		}
		else
		{
			mChequeTotal = fValue;
		}
		editText.setTag("From App");
		editText.setText(String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(mContext, Global.PREF_DECIMAL) + "f", fValue));
		editText.setSelection(editText.getText().toString().length());
		editText.setTag(null);
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
		PdfPTable table = new PdfPTable(6);
		PdfPCell cell;
		Phrase phrase;
		try {
			table.setWidths(new int[]{1, 1, 1, 1, 3, 1});
			table.getDefaultCell().setBorder(Rectangle.NO_BORDER);

			phrase = new Phrase("Collection Confirmation Report", fontBlack10Bold);
			cell = new PdfPCell(phrase);
			cell.setColspan(6);
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell.setBorder(Rectangle.NO_BORDER);
			cell.setPadding(5);
			table.addCell(cell);

			// Cheque Collection
			phrase = new Phrase("Cheque Collection", fontBlack10Bold);
			cell = new PdfPCell(phrase);
			cell.setColspan(6);
			cell.setBorder(Rectangle.NO_BORDER);
			cell.setPadding(5);
			table.addCell(cell);

			phrase = new Phrase("BankNo", fontBlack7Bold);
			table.addCell(phrase);
			phrase = new Phrase("Account", fontBlack7Bold);
			table.addCell(phrase);
			phrase = new Phrase("Cheque No", fontBlack7Bold);
			table.addCell(phrase);
			phrase = new Phrase("Date", fontBlack7Bold);
			table.addCell(phrase);
			phrase = new Phrase("Customer", fontBlack7Bold);
			table.addCell(phrase);
			phrase = new Phrase("Amount", fontBlack10Bold);
			cell = new PdfPCell(phrase);
			cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
			cell.setBorder(Rectangle.NO_BORDER);
			table.addCell(cell);

			float nTotal1 = 0;
			for (UCollectionsDInfo info : mUCollectionsDList) {
				if (info.nPayType != Global.COLLECTION_CHEQUE) {
					continue;
				}
				phrase = new Phrase(info.strBankNo, fontDarkGray7Normal);
				table.addCell(phrase);
				phrase = new Phrase(info.strAccount, fontDarkGray7Normal);
				table.addCell(phrase);
				phrase = new Phrase(info.strCheckNo, fontDarkGray7Normal);
				table.addCell(phrase);
				phrase = new Phrase(new SimpleDateFormat(Utils.getDateFormat(mContext, Global.DATE_DD_MM_YY_FORMAT)).format(Utils.getDateFromString(info.strTrxnDate)), fontDarkGray7Normal);
				table.addCell(phrase);
				phrase = new Phrase(info.strCustomerName, fontDarkGray7Normal);
				table.addCell(phrase);
				phrase = new Phrase(Utils.getFormattedPrice(mContext, info.nTrxnAmount), fontDarkGray7Normal);
				cell = new PdfPCell(phrase);
				cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				cell.setBorder(Rectangle.NO_BORDER);
				table.addCell(cell);
				//
				nTotal1 += Utils.parseFloatFromString(String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(mContext, Global.PREF_DECIMAL) + "f", info.nTrxnAmount));
			}
			phrase = new Phrase("Total", fontDarkGray7Normal);
			cell = new PdfPCell(phrase);
			cell.setColspan(5);
			cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
			cell.setBorder(Rectangle.NO_BORDER);
			cell.setPaddingTop(5);
			cell.setPaddingRight(30);
			table.addCell(cell);
			phrase = new Phrase(Utils.getFormattedPrice(mContext, nTotal1), fontDarkGray7Normal);
			cell = new PdfPCell(phrase);
			cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
			cell.setBorder(Rectangle.NO_BORDER);
			cell.setPaddingTop(5);
			table.addCell(cell);

			// Cash Collection
			phrase = new Phrase("Cash Collection", fontBlack10Bold);
			cell = new PdfPCell(phrase);
			cell.setColspan(6);
			cell.setBorder(Rectangle.NO_BORDER);
			cell.setPadding(5);
			table.addCell(cell);

			float nTotal2 = 0;
			for (UCollectionsDInfo info : mUCollectionsDList) {
				if (info.nPayType != Global.COLLECTION_CASH) {
					continue;
				}
				phrase = new Phrase(info.strBankNo, fontDarkGray7Normal);
				table.addCell(phrase);
				phrase = new Phrase(info.strAccount, fontDarkGray7Normal);
				table.addCell(phrase);
				phrase = new Phrase(info.strCheckNo, fontDarkGray7Normal);
				table.addCell(phrase);
				phrase = new Phrase(new SimpleDateFormat(Utils.getDateFormat(mContext, Global.DATE_DD_MM_YY_FORMAT)).format(Utils.getDateFromString(info.strTrxnDate)), fontDarkGray7Normal);
				table.addCell(phrase);
				phrase = new Phrase(info.strCustomerName, fontDarkGray7Normal);
				table.addCell(phrase);
				phrase = new Phrase(Utils.getFormattedPrice(mContext, info.nTrxnAmount), fontDarkGray7Normal);
				cell = new PdfPCell(phrase);
				cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				cell.setBorder(Rectangle.NO_BORDER);
				table.addCell(cell);
				nTotal2 += Utils.parseFloatFromString(String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(mContext, Global.PREF_DECIMAL) + "f", info.nTrxnAmount));
			}
			phrase = new Phrase("Total", fontDarkGray7Normal);
			cell = new PdfPCell(phrase);
			cell.setColspan(5);
			cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
			cell.setBorder(Rectangle.NO_BORDER);
			cell.setPaddingTop(5);
			cell.setPaddingRight(30);
			table.addCell(cell);
			phrase = new Phrase(Utils.getFormattedPrice(mContext, nTotal2), fontDarkGray7Normal);
			cell = new PdfPCell(phrase);
			cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
			cell.setBorder(Rectangle.NO_BORDER);
			cell.setPaddingTop(5);
			table.addCell(cell);

			// Card Collection
			phrase = new Phrase("Card Collection", fontBlack10Bold);
			cell = new PdfPCell(phrase);
			cell.setColspan(6);
			cell.setBorder(Rectangle.NO_BORDER);
			cell.setPadding(5);
			table.addCell(cell);

			float nTotal3 = 0;
			for (UCollectionsDInfo info : mUCollectionsDList) {
				if (info.nPayType != Global.COLLECTION_CARD) {
					continue;
				}
				phrase = new Phrase(info.strBankNo, fontDarkGray7Normal);
				table.addCell(phrase);
				phrase = new Phrase(info.strAccount, fontDarkGray7Normal);
				table.addCell(phrase);
				phrase = new Phrase(info.strCheckNo, fontDarkGray7Normal);
				table.addCell(phrase);
				phrase = new Phrase(new SimpleDateFormat(Utils.getDateFormat(mContext, Global.DATE_DD_MM_YY_FORMAT)).format(Utils.getDateFromString(info.strTrxnDate)), fontDarkGray7Normal);
				table.addCell(phrase);
				phrase = new Phrase(info.strCustomerName, fontDarkGray7Normal);
				table.addCell(phrase);
				phrase = new Phrase(Utils.getFormattedPrice(mContext, info.nTrxnAmount), fontDarkGray7Normal);
				cell = new PdfPCell(phrase);
				cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				cell.setBorder(Rectangle.NO_BORDER);
				table.addCell(cell);
				nTotal3 += Utils.parseFloatFromString(String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(mContext, Global.PREF_DECIMAL) + "f", info.nTrxnAmount));
			}
			phrase = new Phrase("Total", fontDarkGray7Normal);
			cell = new PdfPCell(phrase);
			cell.setColspan(5);
			cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
			cell.setBorder(Rectangle.NO_BORDER);
			cell.setPaddingTop(5);
			cell.setPaddingRight(30);
			table.addCell(cell);
			phrase = new Phrase(Utils.getFormattedPrice(mContext, nTotal3), fontDarkGray7Normal);
			cell = new PdfPCell(phrase);
			cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
			cell.setBorder(Rectangle.NO_BORDER);
			cell.setPaddingTop(5);
			table.addCell(cell);

			phrase = new Phrase("Collection Total", fontBlack7Bold);
			cell = new PdfPCell(phrase);
			cell.setColspan(5);
			cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
			cell.setBorder(Rectangle.NO_BORDER);
			cell.setPaddingTop(10);
			cell.setPaddingRight(30);
			table.addCell(cell);
			phrase = new Phrase(Utils.getFormattedPrice(mContext, nTotal1 + nTotal2 + nTotal3), fontBlack7Bold);
			cell = new PdfPCell(phrase);
			cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
			cell.setBorder(Rectangle.NO_BORDER);
			cell.setPaddingTop(10);
			table.addCell(cell);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return table;
	}

	private PdfPTable createFooter() {
		PdfPTable table = new PdfPTable(2);
		PdfPCell cell;
		Phrase phrase;
		try {
			table.setWidths(new int[]{1, 1});
			table.getDefaultCell().setBorder(Rectangle.NO_BORDER);

			phrase = new Phrase("Collection Confirmation", fontBlack10Bold);
			cell = new PdfPCell(phrase);
			cell.setColspan(2);
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell.setBorder(Rectangle.NO_BORDER);
			cell.setPadding(5);
			table.addCell(cell);

			phrase = new Phrase("Cheque", fontDarkGray7Normal);
			cell = new PdfPCell(phrase);
			cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
			cell.setBorder(Rectangle.NO_BORDER);
			cell.setPaddingRight(30);
			table.addCell(cell);
			phrase = new Phrase(Utils.getFormattedPrice(mContext, mChequeTotal), fontDarkGray7Normal);
			table.addCell(phrase);
			phrase = new Phrase("Cash", fontDarkGray7Normal);
			cell = new PdfPCell(phrase);
			cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
			cell.setBorder(Rectangle.NO_BORDER);
			cell.setPaddingRight(30);
			table.addCell(cell);
			phrase = new Phrase(Utils.getFormattedPrice(mContext, mCashTotal), fontDarkGray7Normal);
			table.addCell(phrase);
			phrase = new Phrase("Card", fontDarkGray7Normal);
			cell = new PdfPCell(phrase);
			cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
			cell.setBorder(Rectangle.NO_BORDER);
			cell.setPaddingRight(30);
			table.addCell(cell);
			phrase = new Phrase(Utils.getFormattedPrice(mContext, mCardTotal), fontDarkGray7Normal);
			table.addCell(phrase);

			phrase = new Phrase("Confirmation Total", fontBlack7Bold);
			cell = new PdfPCell(phrase);
			cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
			cell.setBorder(Rectangle.NO_BORDER);
			cell.setPaddingRight(30);
			table.addCell(cell);
			phrase = new Phrase(Utils.getFormattedPrice(mContext, mChequeTotal + mCashTotal + mCardTotal), fontBlack7Bold);
			table.addCell(phrase);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return table;
	}


	private void createTemplatePDF(){
		showProgressDialog(Str.CreatingPdf[Global.LANG]);
		new Thread()
		{
			@Override
			public void run() {
				PrintEngine printEngine = new PrintEngine();
				mHoneywellPrintEngine = printEngine;

				DbAdapter dbAdapter = new DbAdapter(mContext);
				dbAdapter.open();

				// get total required collection amount
				// get completed customers
				List<CustomerInfo> completedList = dbAdapter.getCompletedCustomerList();
				float dTotalRemaining = 0;
				for (CustomerInfo info: completedList) {
					List<ARHeaderInfo> arHeaderInfos = dbAdapter.getARHeaderInfo(info.nChainNo, info.nCustNo);

					if( arHeaderInfos.size() > 0)
					{
						for(ARHeaderInfo arHeaderInfo : arHeaderInfos)
						{
							if( arHeaderInfo.nProcessStatus < 2)
								dTotalRemaining += arHeaderInfo.nTrxnAmount * 1.0 / Global.MULTIPLE_VALUE;
									/*
									else if(arHeaderInfo.nProcessStatus == 2)
									{
										if (bLoadShowFirst) {
											arHeaderInfo.nProcessStatus = 1;
											dbAdapter.updateARHeaderInfo(arHeaderInfo);
											dTotalRemaining += arHeaderInfo.nTrxnAmount * 1.0 / Global.MULTIPLE_VALUE;
										}
										else {
											if(Global.gCurCustomerInfo.strPayType.equals("$") ||
													Global.gCurCustomerInfo.strPayType.equals("1") ||
													Global.gCurCustomerInfo.strPayType.equals("2") ||
													Global.gCurCustomerInfo.strPayType.equals("3"))
											{
												arHeaderInfo.nProcessStatus = 1;
												dbAdapter.updateARHeaderInfo(arHeaderInfo);
												dTotalRemaining += arHeaderInfo.nTrxnAmount * 1.0 / Global.MULTIPLE_VALUE;
											}
										}
									}*/
						}
					}
				}
				dbAdapter.close();

				printEngine.PrepareCollectionPrint(mContext, mUCollectionsDList, dTotalRemaining, new Date());
				mStrTemplatePdfFileName = getPDFFileName();
				Utils.gDetlMap.clear();

				String strHoneywellFileName = Utils.getFinalPrintTemplateFileNameForHoneywell(mContext, Global.PRINT_COLLECTION_REPORT_FILENAME);
				mHoneywellTemplatePath = Global.ROOT_PRINT_TEMPLATE_LOCCATION + strHoneywellFileName;
				printEngine.createDocument(mContext, false, Global.PDF_LOCAL_DIR + mStrTemplatePdfFileName, Global.COLLECTION_PRINT);

				mPrintType = 1;
				new Handler(Looper.getMainLooper()).post(new Runnable() {
					@Override
					public void run() {
						hideProgressDialog();
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

					}
				});
			}
		}.start();


	}
	private void createPDF() {
		try {
			Document document = new Document();
			PdfWriter.getInstance(document, new FileOutputStream(Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + mStrPdfFileName));
			document.open();
			PdfPTable table = createHeader();
			document.add(table);
			table = createBody();
			table.setSpacingBefore(20);
			document.add(table);
			table = createFooter();
			table.setSpacingBefore(10);
			document.add(table);
			document.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void doComplete(boolean bBack) {
		String strDocNo = mStrPdfFileName.substring(0, mStrPdfFileName.length() - 4);
		long nTrxnNo = setGeneralUploadData("DENM", 0, 0, -1, "", strDocNo);

		TransactionInfo transactionInfo = new TransactionInfo();
		transactionInfo.strType = "DENM";
		transactionInfo.strTime = new SimpleDateFormat("kk:mm").format(new Date(nTrxnNo));
		transactionInfo.strDocNo = strDocNo;
		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		dbAdapter.insertTransactionHistoryInfo(transactionInfo);

		PrintItem printItem = new PrintItem();
		printItem.nTrxnNo = nTrxnNo;
		printItem.strDocNo = strDocNo;
		printItem.strDocType = "DENM";
		printItem.strFilePath = Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + mStrPdfFileName;
		printItem.nCustNo = 0;
		printItem.nChainNo = 0;
		printItem.strCustomerName = "";
		dbAdapter.insertPrint(printItem);

		dbAdapter.close();

		UTransactionsInfo uTransactionsInfo = setUTransactionsLastInfo(nTrxnNo, "FARC", mStrPdfFileName);
		setUFileTransactionInfo(uTransactionsInfo.nTrxnNo, 0, 0, mPdfTrxnNo, strDocNo, "COLLECTION", "Collection Confirmation Report", uTransactionsInfo.strTrxnDate, uTransactionsInfo.strTrxnTime, mStrPdfFileName);

		String strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(new Date());
		int nSeqNo = Utils.getIntSetting(mContext, String.format(Locale.ENGLISH, Global.PREF_COLLECTION_SEQUENCE_NO, Global.gTripNumber)) + 1;

		UCollectionsDInfo uCollectionsDCashInfo = new UCollectionsDInfo();
		uCollectionsDCashInfo.nTrxnNo = nTrxnNo;
		uCollectionsDCashInfo.nSeqNo = nSeqNo;
		uCollectionsDCashInfo.nPayType = Global.COLLECTION_EOD_CASH;
		uCollectionsDCashInfo.strTrxnDate = strTrxnDate;
		uCollectionsDCashInfo.nTrxnAmount = mCashTotal;

		nSeqNo++;
		UCollectionsDInfo uCollectionsDChequeInfo = new UCollectionsDInfo();
		uCollectionsDChequeInfo.nTrxnNo = nTrxnNo;
		uCollectionsDChequeInfo.nSeqNo = nSeqNo;
		uCollectionsDChequeInfo.nPayType = Global.COLLECTION_EOD_CHEQUE;
		uCollectionsDChequeInfo.strTrxnDate = strTrxnDate;
		uCollectionsDChequeInfo.nTrxnAmount = mCashTotal;

		Utils.setIntSetting(mContext, String.format(Locale.ENGLISH, Global.PREF_COLLECTION_SEQUENCE_NO, Global.gTripNumber), nSeqNo);

		dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		float nTrxnAmount = Utils.getFloatSetting(mContext, Global.COLLECTIONS_TODAY);
		dbAdapter.insertUCollectionsDInfo(uCollectionsDChequeInfo);
		nTrxnAmount += uCollectionsDChequeInfo.nTrxnAmount;
		dbAdapter.insertUCollectionsDInfo(uCollectionsDCashInfo);
		nTrxnAmount += uCollectionsDCashInfo.nTrxnAmount;
		Utils.setFloatSetting(mContext, Global.COLLECTIONS_TODAY, nTrxnAmount);
		dbAdapter.close();

		Utils.setBooleanSetting(mContext, String.format(Locale.ENGLISH, Global.PREF_END_TRIP_CHECKED, 3), true);
		//Global.gUploadCustNoList.add(makeZipFileInfo);
		Utils.registerUploadFile(mContext, makeZipFileInfo);
		if(bBack)
		{
			new Handler(Looper.getMainLooper()).post(new Runnable() {
				@Override
				public void run() {
					goBack();
				}
			});
		}
	}

	public boolean m_bClicked = false;
	@Override
	public void onClick(View v) {
		if( m_bClicked ) return;
		m_bClicked = true;
		switch (v.getId()) {
			case R.id.btnBack:
				goBack();
				break;
			case R.id.btnConfirm:
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

						Utils.createDirectory(Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/");
						mPdfTrxnNo = System.currentTimeMillis();
						mStrPdfFileName = getPDFFileName();

						String strTemplateFileName = Utils.getFinalPrintTemplateFileName(mContext, Global.PRINT_BANKING_TEMPLATE_FILENAME);
						if(Utils.isFileExist(Global.ROOT_PRINT_TEMPLATE_LOCCATION + strTemplateFileName))
						{
							PrintEngine printEngine = new PrintEngine();
							mHoneywellPrintEngine = printEngine;

							DbAdapter dbAdapter = new DbAdapter(mContext);
							dbAdapter.open();

							List<CustomerInfo> completedList = dbAdapter.getCompletedCustomerList();
							float dTotalRemaining = 0;
							for (CustomerInfo info: completedList) {
								List<ARHeaderInfo> arHeaderInfos = dbAdapter.getARHeaderInfo(info.nChainNo, info.nCustNo);

								for(ARHeaderInfo arHeaderInfo : arHeaderInfos)
								{
									if( arHeaderInfo.nProcessStatus <= 2) {
										if (arHeaderInfo.nTrxnAmount >= 0) {
											dTotalRemaining += arHeaderInfo.nTrxnAmount * 1.0 / Global.MULTIPLE_VALUE;
										}
									}

									/*
									else if(arHeaderInfo.nProcessStatus == 2)
									{
										if (bLoadShowFirst) {
											arHeaderInfo.nProcessStatus = 1;
											dbAdapter.updateARHeaderInfo(arHeaderInfo);
											dTotalRemaining += arHeaderInfo.nTrxnAmount * 1.0 / Global.MULTIPLE_VALUE;
										}
										else {
											if(Global.gCurCustomerInfo.strPayType.equals("$") ||
													Global.gCurCustomerInfo.strPayType.equals("1") ||
													Global.gCurCustomerInfo.strPayType.equals("2") ||
													Global.gCurCustomerInfo.strPayType.equals("3"))
											{
												arHeaderInfo.nProcessStatus = 1;
												dbAdapter.updateARHeaderInfo(arHeaderInfo);
												dTotalRemaining += arHeaderInfo.nTrxnAmount * 1.0 / Global.MULTIPLE_VALUE;
											}
										}
									}*/
								}
							}
							dbAdapter.close();

							printEngine.PrepareBankingPrint(mContext, mUCollectionsDList, dTotalRemaining, mPdfTrxnNo);
							String strHoneywellFileName = Utils.getFinalPrintTemplateFileNameForHoneywell(mContext, Global.PRINT_BANKING_TEMPLATE_FILENAME);
							mHoneywellTemplatePath = Global.ROOT_PRINT_TEMPLATE_LOCCATION + strHoneywellFileName;
							printEngine.createDocument(mContext, false, Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + mStrPdfFileName, Global.BANKING_PRINT);
							if(mTotal != 0)
							{
								if(Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 1)
								{
									new Handler(Looper.getMainLooper()).post(() -> {
										doComplete(false);
										hideProgressDialog();
										try {
											final MessageDialog msgDialog = new MessageDialog(mContext);
											msgDialog.setCancelable(false);
											msgDialog.mStrMessage = Str.Print_msg[Global.LANG];
											msgDialog.mStrLeft = Str.No[Global.LANG];
											msgDialog.mStrRight = Str.Yes[Global.LANG];
											msgDialog.setOnDismissListener(dialog -> {
												if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
													mPrintType = 0;
													outputPrinter();
												} else if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
													goBack();
												}
											});
											msgDialog.show();
										}catch (Exception e)
										{
											e.printStackTrace();
										}

									});
								}
								else if(Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 2)
								{
									new Handler(Looper.getMainLooper()).post(() -> {
										doComplete(false);
										hideProgressDialog();
										try {
											final MessageDialog msgDialog = new MessageDialog(mContext);
											msgDialog.setCancelable(false);
											msgDialog.mStrMessage = Str.Print_msg[Global.LANG];
											msgDialog.mStrLeft = Str.No[Global.LANG];
											msgDialog.mStrRight = Str.Yes[Global.LANG];
											msgDialog.setOnDismissListener(dialog -> {
												if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
													mPrintType = 0;
													outputHoneywellPrinter();
												} else if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
													goBack();
												}
											});
											msgDialog.show();
										}catch (Exception e)
										{
											e.printStackTrace();
										}

									});
								}
								else if(Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 3)
								{
									new Handler(Looper.getMainLooper()).post(() -> {
										doComplete(false);
										hideProgressDialog();
										try {
											final MessageDialog msgDialog = new MessageDialog(mContext);
											msgDialog.setCancelable(false);
											msgDialog.mStrMessage = Str.Print_msg[Global.LANG];
											msgDialog.mStrLeft = Str.No[Global.LANG];
											msgDialog.mStrRight = Str.Yes[Global.LANG];
											msgDialog.setOnDismissListener(dialog -> {
												if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
													mPrintType = 0;
													outputHoneywellLinePrinter();
												} else if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
													goBack();
												}
											});
											msgDialog.show();
										}catch (Exception e)
										{
											e.printStackTrace();
										}

									});
								}
								else if(Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 4)
								{
									new Handler(Looper.getMainLooper()).post(() -> {
										doComplete(false);
										hideProgressDialog();
										try {
											final MessageDialog msgDialog = new MessageDialog(mContext);
											msgDialog.setCancelable(false);
											msgDialog.mStrMessage = Str.Print_msg[Global.LANG];
											msgDialog.mStrLeft = Str.No[Global.LANG];
											msgDialog.mStrRight = Str.Yes[Global.LANG];
											msgDialog.setOnDismissListener(dialog -> {
												if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
													mPrintType = 0;
													outputBixolonPrinter();
												} else if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
													goBack();
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
									doComplete(true);
									new Handler(Looper.getMainLooper()).post(new Runnable() {
										@Override
										public void run() {
											hideProgressDialog();
										}
									});
								}
							}
							else
							{
								doComplete(true);
								new Handler(Looper.getMainLooper()).post(new Runnable() {
									@Override
									public void run() {
										hideProgressDialog();
									}
								});
							}
						}
						else {
							createPDF();
							doComplete(true);
							new Handler(Looper.getMainLooper()).post(new Runnable() {
								@Override
								public void run() {
									hideProgressDialog();
								}
							});
						}

					}
				}.start();

				break;
			case R.id.btnSummary:
				mDetailedMode = !mDetailedMode;
				refreshLanguage();
				break;
			case R.id.btnPrint:
				createTemplatePDF();
				/*
				if(Utils.isFileExist(Global.ROOT_LOCATION + "/" + Global.PRINT_RECEIPT_FILENAME))
				{
					createTemplatePDF();
				}*/
				break;
		}
		m_bClicked = false;
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
					} else if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
						goBack();
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

	private void outputBixolonPrinter() {
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
					} else if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
						goBack();
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

	private void doHoneywellPrintPdfTask()
	{
		showProgressDialog(Str.Printing[Global.LANG]);

		String strPdfPath = "";
		if(mPrintType == 1)
		{
			strPdfPath = Global.PDF_LOCAL_DIR + mStrTemplatePdfFileName;
		}
		else
		{
			strPdfPath = Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + mStrPdfFileName;
		}
		HoneywellPrinter.doPrint(mContext, new HoneywellPrinter.PrintCallback() {
			@Override
			public void printFinished() {
				hideProgressDialog();
				try {
					Utils.setStringSetting(mContext, Global.PREF_HONEYWELL_PRINTER_MAC_ADDRESS, mStrMacAddress);
					Utils.printDebug(mContext, Str.SuccessToPrint[Global.LANG]);
					if(mPrintType == 0)
					{
						new Handler(Looper.getMainLooper()).post(() -> goBack());
					}
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
						}
					});
					msgDialog.show();
				}catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}, mStrMacAddress, strPdfPath);
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
						if (mPrintType == 0) {
							new Handler(Looper.getMainLooper()).post(() -> goBack());
						}
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
			if(mPrintType == 0)
			{
				new Handler(Looper.getMainLooper()).post(() -> goBack());
			}
		}
	}

	private void doBixolonPrintPdfTask()
	{
		showProgressDialog(Str.Printing[Global.LANG]);

		String strPdfPath = "";
		if(mPrintType == 1)
		{
			strPdfPath = Global.PDF_LOCAL_DIR + mStrTemplatePdfFileName;
		}
		else
		{
			strPdfPath = Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + mStrPdfFileName;
		}
		BixolonPrinter.doPrint(mContext, new BixolonPrinter.PrintCallback() {
			@Override
			public void printFinished() {
				hideProgressDialog();
				try {
					Utils.setStringSetting(mContext, Global.PREF_BIXOLON_PRINTER_MAC_ADDRESS, mStrMacAddress);
					Utils.setStringSetting(mContext, Global.PREF_BIXOLON_PRINTER_FRIENDLY_NAME, mStrFriendlyName);
					Utils.printDebug(mContext, Str.SuccessToPrint[Global.LANG]);
					if(mPrintType == 0)
					{
						new Handler(Looper.getMainLooper()).post(() -> goBack());
					}
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
							goBack();
						}
					});
					msgDialog.show();
				}catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}, mStrMacAddress, mStrFriendlyName, strPdfPath);
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
					new Handler(Looper.getMainLooper()).post(() -> {
						try {
							final MessageDialog msgDialog = new MessageDialog(mContext);
							msgDialog.setCancelable(false);
							msgDialog.mStrMessage = Str.FailedToPrint[Global.LANG] + "\n" + Str.DiscoveryPrinterAgain[Global.LANG];
							msgDialog.mStrLeft = Str.No[Global.LANG];
							msgDialog.mStrRight = Str.Yes[Global.LANG];
							msgDialog.setOnDismissListener(dialog -> {
								if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
									discoveryHoneywellPrinters();
								}
							});
							msgDialog.show();
						}catch (Exception e)
						{
							e.printStackTrace();
						}
					});

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
					new Handler(Looper.getMainLooper()).post(() -> {
						try {
							final MessageDialog msgDialog = new MessageDialog(mContext);
							msgDialog.setCancelable(false);
							msgDialog.mStrMessage = Str.FailedToPrint[Global.LANG] + "\n" + Str.DiscoveryPrinterAgain[Global.LANG];
							msgDialog.mStrLeft = Str.No[Global.LANG];
							msgDialog.mStrRight = Str.Yes[Global.LANG];
							msgDialog.setOnDismissListener(dialog -> {
								if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
									discoveryHoneywellLinePrinters();
								}
							});
							msgDialog.show();
						}catch (Exception e)
						{
							e.printStackTrace();
						}
					});

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
					new Handler(Looper.getMainLooper()).post(() -> {
						try {
							final MessageDialog msgDialog = new MessageDialog(mContext);
							msgDialog.setCancelable(false);
							msgDialog.mStrMessage = Str.FailedToPrint[Global.LANG] + "\n" + Str.DiscoveryPrinterAgain[Global.LANG];
							msgDialog.mStrLeft = Str.No[Global.LANG];
							msgDialog.mStrRight = Str.Yes[Global.LANG];
							msgDialog.setOnDismissListener(dialog -> {
								if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
									discoveryBixolonPrinters();
								}
							});
							msgDialog.show();
						}catch (Exception e)
						{
							e.printStackTrace();
						}
					});

				}

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				Looper.myLooper().quit();
			}
		}).start();
	}

	public class ContentListAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			if(mDetailedMode)
			{
				return mCashList.size() + mCardList.size() + mChequeList.size() + 3;
			}
			else
			{
				return 3;
			}
		}

		@Override
		public Object getItem(int i) {
			if(mDetailedMode)
			{
				if(i == 0 ) return null;
				if(i == mCashList.size() + 1) return null;
				if(i == mCashList.size() + mChequeList.size() + 2) return null;

				else if(i > 0 && i < mCashList.size() + 1) return mCashList.get(i - 1);
				else if( i > mCashList.size() + 1 && i < mCashList.size() + mChequeList.size() + 2) return mChequeList.get(i - mCashList.size() - 2);
				else return mCardList.get(i - mCashList.size() - mChequeList.size() - 3);
			}
			else
			{
				return null;
			}
		}

		@Override
		public long getItemId(int i) {
			return i;
		}

		@Override
		public View getView(int i, View view, ViewGroup viewGroup) {
			int nType;
			if(mDetailedMode)
			{
				if( i == 0 )
				{
					view = mContext.getLayoutInflater().inflate(R.layout.row_collection_balancing_header_1, viewGroup, false);
					nType = 1;
				}
				else if(i == mCashList.size() + 1)
				{
					view = mContext.getLayoutInflater().inflate(R.layout.row_collection_balancing_header_1, viewGroup, false);
					nType = 2;
				}
				else if(i == mCashList.size() + mChequeList.size() + 2)
				{
					view = mContext.getLayoutInflater().inflate(R.layout.row_collection_balancing_header_2, viewGroup, false);
					nType = 3;
				}
				else
				{
					view = mContext.getLayoutInflater().inflate(R.layout.row_collection_balancing_body, viewGroup, false);
					nType = 0;
				}
			}
			else
			{
				if( i == 0)
				{
					view = mContext.getLayoutInflater().inflate(R.layout.row_collection_balancing_header_1, viewGroup, false);
					nType = 1;
				}
				else if( i == 1)
				{
					view = mContext.getLayoutInflater().inflate(R.layout.row_collection_balancing_header_1, viewGroup, false);
					nType = 2;
				}
				else
				{
					view = mContext.getLayoutInflater().inflate(R.layout.row_collection_balancing_header_2, viewGroup, false);
					nType = 3;
				}
			}

			if(nType == 1)
			{
				final EditText editTotal = (EditText) view.findViewById(R.id.editTotal);
				editTotal.addTextChangedListener(new TextWatcher() {
					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count) {
					}

					@Override
					public void beforeTextChanged(CharSequence s, int start, int count, int after) {
					}

					@Override
					public void afterTextChanged(Editable s) {
						if (editTotal.getTag() == null) {
							setAmount(s.toString(), editTotal, 0);
						}
					}
				});
				editTotal.post(new Runnable() {
					public void run() {
						editTotal.setSelection(editTotal.getText().toString().length());
						editTotal.postDelayed(this, 100);
					}
				});

				((TextView) view.findViewById(R.id.lblLabel)).setText(Str.Cash[Global.LANG]);
				((TextView) view.findViewById(R.id.txtCount)).setText(String.valueOf(mCashList.size()));
				((TextView) view.findViewById(R.id.lblSymbol)).setText(Global.gCurrencySymbol);

				editTotal.setTag("From App");
				editTotal.setText(String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(mContext, Global.PREF_DECIMAL) + "f", mCashTotal));
				editTotal.setSelection(editTotal.getText().toString().length());
				editTotal.setTag(null);

				String strTenderFullAmount = Utils.getStringSetting(mContext, Global.PREF_TENDER_FULL_AMOUNT);
				if (strTenderFullAmount.equals("1")) {
					editTotal.setEnabled(false);
				}
			}
			else if(nType == 2)
			{
				final EditText editTotal = (EditText) view.findViewById(R.id.editTotal);
				editTotal.addTextChangedListener(new TextWatcher() {
					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count) {
					}

					@Override
					public void beforeTextChanged(CharSequence s, int start, int count, int after) {
					}

					@Override
					public void afterTextChanged(Editable s) {
						if (editTotal.getTag() == null) {
							setAmount(s.toString(), editTotal, 1);
						}
					}
				});
				editTotal.post(new Runnable() {
					public void run() {
						editTotal.setSelection(editTotal.getText().toString().length());
						editTotal.postDelayed(this, 100);
					}
				});

				((TextView) view.findViewById(R.id.lblLabel)).setText(Str.Cheque[Global.LANG]);
				((TextView) view.findViewById(R.id.txtCount)).setText(String.valueOf(mChequeList.size()));
				((TextView) view.findViewById(R.id.lblSymbol)).setText(Global.gCurrencySymbol);

				editTotal.setTag("From App");
				editTotal.setText(String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(mContext, Global.PREF_DECIMAL) + "f", mChequeTotal));
				editTotal.setSelection(editTotal.getText().toString().length());
				editTotal.setTag(null);

				String strTenderFullAmount = Utils.getStringSetting(mContext, Global.PREF_TENDER_FULL_AMOUNT);
				if (strTenderFullAmount.equals("1")) {
					editTotal.setEnabled(false);
				}
			}
			else if(nType == 3)
			{
				((TextView) view.findViewById(R.id.lblCard)).setText(Str.Card[Global.LANG]);
				((TextView) view.findViewById(R.id.txtCardCnt)).setText(String.valueOf(mCardList.size()));
				((TextView) view.findViewById(R.id.txtCardTotal)).setText(Utils.getFormattedPrice(mContext, mCardTotal));
			}
			else
			{
				UCollectionsDInfo info = (UCollectionsDInfo)getItem(i);
				if(info != null)
				{
					((TextView)view.findViewById(R.id.txtName)).setText(info.strCustomerName);
					((TextView)view.findViewById(R.id.txtInvoiceNo)).setText(info.strInvoiceNo);
					((TextView)view.findViewById(R.id.txtAmount)).setText(Utils.getFormattedPrice(mContext, info.nTrxnAmount));
				}
			}

			view.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {

				}
			});
			return view;
		}
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

//					latch.await(Global.DISCOVER_BLUETOOTH_TIMEOUT, TimeUnit.SECONDS);
					latch.await();
					if(result[0] == false)
					{
						hideProgressDialog();
//						mHander.sendEmptyMessage(0);
						new Handler(Looper.getMainLooper()).post(new Runnable(){

							@Override
							public void run() {
								try {
									final MessageDialog msgDialog = new MessageDialog(mContext);
									msgDialog.setCancelable(false);
									msgDialog.mStrMessage = Str.FailedToPrint[Global.LANG] + "\n" + Str.DiscoveryPrinterAgain[Global.LANG];
									msgDialog.mStrLeft = Str.No[Global.LANG];
									msgDialog.mStrRight = Str.Yes[Global.LANG];
									msgDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
										@Override
										public void onDismiss(DialogInterface dialog) {
											if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
												discoveryPrinters();
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

						});
					}

				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					Looper.myLooper().quit();
				}
			}
		}).start();
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
							if(mPrintType == 1)
							{
								printer.sendFileContents(Global.PDF_LOCAL_DIR + mStrTemplatePdfFileName);
							}
							else
							{
								printer.sendFileContents(Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + mStrPdfFileName);
							}
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
					Utils.setStringSetting(mContext, Global.PREF_PRINTER_MAC_ADDRESS, mStrMacAddress);
					Utils.printDebug(mContext, Str.SuccessToPrint[Global.LANG]);
					if(mPrintType == 0)
					{
						new Handler(Looper.getMainLooper()).post(new Runnable() {
							@Override
							public void run() {
								goBack();
							}
						});
					}

				} else {
					try
					{
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

	boolean bPaused = false;
	boolean bNeedBack = false;
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
}
