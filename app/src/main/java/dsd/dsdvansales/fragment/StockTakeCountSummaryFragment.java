package dsd.dsdvansales.fragment;

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.squareup.picasso.Picasso;
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
import dsd.dsdvansales.dialogs.MessageDialog;
import dsd.dsdvansales.dialogs.PresoldDialog;
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
import dsd.dsdvansales.model.StatusInfo;
import dsd.dsdvansales.model.TransactionInfo;
import dsd.dsdvansales.model.UInvenBlotDInfo;
import dsd.dsdvansales.model.UInvenBlotInfo;
import dsd.dsdvansales.model.UInvenDInfo;
import dsd.dsdvansales.model.UStockDInfo;
import dsd.dsdvansales.model.UStockHInfo;
import dsd.dsdvansales.model.UTransactionsInfo;
import dsd.dsdvansales.views.AlertSingleChoiceAdapter;

public class StockTakeCountSummaryFragment extends BaseFragment implements View.OnClickListener {

	private Font fontBlack7Bold;
	private Font fontBlack10Bold;
	private Font fontBlack7Normal;
	private String mStrPdfFileName = "";
	private long mPdfTrxnNo = 0;
	private int mTruckCnt = 0;
	private int mContainerCnt = 0;
	private Date mTrxnDate = new Date();

	private Connection printerConnection = null;
	private String mStrMacAddress = "";
	private boolean mIsCreatedPDF = false;

	private boolean bShowCase = true;
	private boolean bLastProcess = false;
	private PrintEngine mHoneywellPrintEngine = null;
	private String mHoneywellTemplatePath = "";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		mRootView = inflater.inflate(R.layout.fragment_stock_take_summary, container, false);
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
		Global.gFragIdx = Global.FRAG_ADJUST_SUMMARY;
		bPaused = false;
		if(bNeedBack)
		{
			new Handler(Looper.getMainLooper()).post(new Runnable() {
				@Override
				public void run() {
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
		bShowCase = Utils.getStringSetting(mContext, Global.PREF_INVENTORYUOM).equals("U") == false;
		mRootView.findViewById(R.id.btnSignature).setOnClickListener(this);
		mRootView.findViewById(R.id.btnCompleted).setOnClickListener(this);
		mRootView.findViewById(R.id.btnBack).setOnClickListener(this);

		Global.gLoadStartedTime = new SimpleDateFormat("HH:mm:ss").format(new Date());
		loadData();
		refreshLanguage();

	}

	@Override
	public void loadData() {

		refreshPage();
	}

	@Override
	public void refreshPage() {
		((TextView)mRootView.findViewById(R.id.txtDate)).setText(new SimpleDateFormat(Utils.getDateFormat(mContext, "dd/MM/yyyy")).format(new Date(System.currentTimeMillis())));
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
		((TextView) mRootView.findViewById(R.id.lblTitle)).setText(Str.StockTakeSummary[Global.LANG].toUpperCase());
		((TextView) mRootView.findViewById(R.id.txtLoadStarted)).setText(Str.CountStartedAt[Global.LANG] + " " + Global.gLoadStartedTime);
		((TextView) mRootView.findViewById(R.id.lblYouHaveLoaded)).setText(Str.YouHaveCounted[Global.LANG]);
		((TextView) mRootView.findViewById(R.id.lblStocktakeDate)).setText(Str.StocktakeDate[Global.LANG]);
		((Button) mRootView.findViewById(R.id.btnSignature)).setText(Str.Signature[Global.LANG]);
		((Button) mRootView.findViewById(R.id.btnCompleted)).setText(Str.Completed[Global.LANG]);

		int nCartons = 0;
		int nUnits = 0;
		int nContainers = 0;
		double dValue = 0;
		if (Global.gAdjustinfo != null) {
			for (UInvenDInfo info : Global.gAdjustinfo.uInvenDList) {
				if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
				{
					dValue += info.dAdjQty;
				}
				else
				{
					if(bShowCase)
					{
						int nCaseValue = getCaseValue(info.strItemNo);
						int nValue = info.nAdjQty;
						int nCase = nValue / nCaseValue;
						int nQty = nValue % nCaseValue;
						nCartons += nCase;
						nUnits += nQty;
					}
					else
					{
						int nValue = info.nAdjQty;
						nUnits += nValue;
					}
				}
			}
		}
		String strLoaded = "";

		if(nCartons > 0)
		{
			strLoaded += String.format(Locale.ENGLISH, "%d %s", nCartons, Str.cartons[Global.LANG]);
		}
		if(nUnits > 0)
		{
			if(strLoaded.length() > 0) strLoaded += "\n";
			strLoaded += String.format(Locale.ENGLISH, "%d %s", nUnits, Str.units[Global.LANG]);
		}
		if(nContainers > 0)
		{
			if(strLoaded.length() > 0) strLoaded += "\n";
			strLoaded += String.format(Locale.ENGLISH, "%d %s", nContainers, Str.containers[Global.LANG]);
		}
		if(dValue > 0)
		{
			if(strLoaded.length() > 0) strLoaded += "\n";
			strLoaded += String.format(Locale.ENGLISH, "%.2f Kg", dValue);
		}

		((TextView) mRootView.findViewById(R.id.txtLoaded)).setText(strLoaded);
	}

	@Override
	public void receiveBroadcast(String strAction, Intent intent) {
		if (strAction.equals(Global.ACTION_CAPTURE_SIGNATURE) == true) {
			if (Global.gFragIdx == Global.FRAG_ADJUST_SUMMARY && Global.gAdjustinfo != null) {
				Global.gAdjustinfo.strSignatureFilePath = intent.getStringExtra(Global.EXTRA_SIGNATURE_PATH);
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

			phrase = new Phrase("STOCKTAKE COUNT", fontBlack10Bold);
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

			phrase = new Phrase(String.format(Locale.ENGLISH, "Stocktake For %s", new SimpleDateFormat(Utils.getDateFormat(mContext, "dd/MM/yyyy")).format(new Date(System.currentTimeMillis()))), fontBlack7Normal);
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
			int dTotal = 0;
			for (UInvenDInfo info : Global.gAdjustinfo.uInvenDList) {


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
				if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
				{
					double dValue = info.dAdjQty;
					phrase = new Phrase(String.format(Locale.ENGLISH, "%.2f", dValue), fontBlack7Normal);
					cell = new PdfPCell(phrase);
					cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
					cell.setBorder(Rectangle.NO_BORDER);
					table.addCell(cell);
					dTotal += dValue;
				}
				else
				{
					int nValue = info.nAdjQty;
					phrase = new Phrase(String.format(Locale.ENGLISH, "%d", nValue), fontBlack7Normal);
					cell = new PdfPCell(phrase);
					cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
					cell.setBorder(Rectangle.NO_BORDER);
					table.addCell(cell);
					nTotal += nValue;
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
			else if(dTotal > 0)
			{
				phrase = new Phrase(String.format(Locale.ENGLISH, "%.2f Kg", nTotal, dTotal), fontBlack7Normal);
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

			if (Global.gAdjustinfo != null && Global.gAdjustinfo.strSignatureFilePath.length() > 0 && Utils.isFileExist(Global.gAdjustinfo.strSignatureFilePath) == true) {
				Image image = Image.getInstance(Global.gAdjustinfo.strSignatureFilePath);
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
			int nHeight = 250 + 17 * mTruckCnt;
			if (mContainerCnt > 0) {
				nHeight += 17 * (mContainerCnt + 1);
			}
			if (Global.gAdjustinfo.strSignatureFilePath.length() > 0 && Utils.isFileExist(Global.gAdjustinfo.strSignatureFilePath) == true) {
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

	private void doComplete() {
		mTruckCnt = 0;
		mContainerCnt = 0;
		mTrxnDate = new Date();
		new Thread(){
			@Override
			public void run() {
				new Handler(Looper.getMainLooper()).post(new Runnable() {
					@Override
					public void run() {
						showProgressDialog(Str.CreatingPdf[Global.LANG]);
					}
				});
				String strTemplateFileName = Utils.getFinalPrintTemplateFileName(mContext, Global.PRINT_STOCKTAKE_COUNT_TEMPLATE_FILENAME);
				if(Utils.isFileExist(Global.ROOT_PRINT_TEMPLATE_LOCCATION + "/" + strTemplateFileName))
				{
					PrintEngine printEngine = new PrintEngine();
					mHoneywellPrintEngine = printEngine;

					Utils.createDirectory(Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/");
					mPdfTrxnNo = System.currentTimeMillis();
					mStrPdfFileName = getPDFFileName();

					List<UInvenDInfo> truckList = new ArrayList<>();
					for (UInvenDInfo info : Global.gAdjustinfo.uInvenDList) {

						UInvenDInfo info1 = info.copyInfo();
						if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
						{
							double dValue = info.dAdjQty;
							info1.dStock = dValue;
						}
						else
						{
							int nValue = info.nAdjQty;
							info1.nStock = nValue;
						}
						truckList.add(info1);
					}

					Date date = new Date(System.currentTimeMillis());

					DbAdapter dbAdapter = new DbAdapter(mContext);
					dbAdapter.open();
					Utils.gDetlMap.clear();
					if(truckList != null)
					{
						for(UInvenDInfo info : truckList)
						{
							ProdDetlInfo prodDetlInfo = dbAdapter.getProdDetl(info.strItemNo);
							if(prodDetlInfo != null)
							{
								Utils.gDetlMap.put(prodDetlInfo.strItemNo, Utils.getPropertyMap(prodDetlInfo));
							}
						}
					}
					dbAdapter.close();

					printEngine.PreparePrint(mContext, truckList, null, "Stocktake Count", mTrxnDate, mPdfTrxnNo,
							Global.gAdjustinfo.strSignatureFilePath, mStrPdfFileName.substring(0, mStrPdfFileName.length() - 4), date, "", null);

					String strHoneywellFileName = Utils.getFinalPrintTemplateFileNameForHoneywell(mContext, Global.PRINT_STOCKTAKE_COUNT_TEMPLATE_FILENAME);
					mHoneywellTemplatePath = Global.ROOT_PRINT_TEMPLATE_LOCCATION + strHoneywellFileName;
					printEngine.createDocument(mContext, false, Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + mStrPdfFileName, Global.LOAD_STOCKTAKE_PRINT);
				}
				else
				{
					createPDF();
				}

				initMakeZipInfo();
				String strDocNo = mStrPdfFileName.substring(0, mStrPdfFileName.length() - 4);
				long nTrxnNo = setGeneralUploadData("STKE", 0, 0, -1, "", strDocNo);

				TransactionInfo transactionInfo = new TransactionInfo();
				transactionInfo.strType = "STKE";
				transactionInfo.strTime = new SimpleDateFormat("kk:mm").format(new Date(nTrxnNo));
				DbAdapter dbAdapter = new DbAdapter(mContext);
				dbAdapter.open();
				dbAdapter.insertTransactionHistoryInfo(transactionInfo);

				//Register Print document list.
				PrintItem printItem = new PrintItem();
				printItem.nTrxnNo = nTrxnNo;
				printItem.strDocNo = strDocNo;
				printItem.strDocType = "STKE";
				printItem.strFilePath = Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + mStrPdfFileName;
				printItem.nCustNo = 0;
				printItem.nChainNo = 0;
				printItem.strCustomerName = "";
				dbAdapter.insertPrint(printItem);

				dbAdapter.close();

				UTransactionsInfo uTransactionsInfo = setUTransactionsInfo(0, 0, m_lTrxnNo, "FARC", mStrPdfFileName);
				setUFileTransactionInfo(uTransactionsInfo.nTrxnNo, 0, 0, mPdfTrxnNo, strDocNo, "STOCKTAKE", "Stocktake Count", uTransactionsInfo.strTrxnDate, uTransactionsInfo.strTrxnTime, mStrPdfFileName);
				//Get ReasonCode from the Reference . Don't forget this section. Very important.
				makeZipFileInfo.trxnList.add(mPdfTrxnNo);
				if (Global.gAdjustinfo != null) {
					Global.gAdjustinfo.nTrxnNo = mPdfTrxnNo;
					Global.gAdjustinfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(mTrxnDate);
					Global.gAdjustinfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(mTrxnDate);
					Global.gAdjustinfo.strDocType = "STKE";

					UStockHInfo stockHInfo = new UStockHInfo();
					stockHInfo.strDocNo = mStrPdfFileName.substring(0, mStrPdfFileName.length() - 4);
					stockHInfo.strReference = stockHInfo.strDocNo + ".pdf";
					stockHInfo.nTrxnNo = Global.gAdjustinfo.nTrxnNo;
					stockHInfo.strStockType = "14";

					stockHInfo.strTrxnDate = Global.gAdjustinfo.strTrxnDate;
					stockHInfo.strTrxnTime = Global.gAdjustinfo.strTrxnTime;
					stockHInfo.strDocType ="STKE";

					dbAdapter = new DbAdapter(mContext);
					dbAdapter.open();
					dbAdapter.insertUStockHInfoList(stockHInfo);
					dbAdapter.insertUInvenHInfo(Global.gAdjustinfo);

					UInvenBlotInfo uInvenBlotInfo = Utils.getUInvenBlot(Global.gAdjustinfo, mStrPdfFileName.substring(0, mStrPdfFileName.length() - 4), Global.gTripNumber);
					List<UInvenBlotDInfo> uInvenBlotDInfoList = new ArrayList<>();

					for (UInvenDInfo info : Global.gAdjustinfo.uInvenDList) {
						if (info.nSCWType == Global.INVENTORY_TRUCK || info.nSCWType == Global.WEIGHT_ITEM) {
							mTruckCnt++;
						} else if (info.nSCWType == Global.INVENTORY_CONTAINERS) {
							mContainerCnt++;
						}

						UStockDInfo stockDInfo = new UStockDInfo();
						stockDInfo.nTrxnNo = mPdfTrxnNo;
						stockDInfo.strItemNo = info.strItemNo;
						if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
						{
							stockDInfo.dActualQty = info.dAdjQty;
							stockDInfo.dBeginQty = info.dStock;
							stockDInfo.dSuggestQty = 0;
						}
						else
						{
							stockDInfo.nActualQty = info.nAdjQty;
							stockDInfo.nBeginQty = info.nStock;
							stockDInfo.nSuggestQty = 0;
						}

						if(Utils.getIntSetting(mContext, Global.PREF_LOAD_CUSTOMER) != 1)
						{
							if(info.bLotControl && info.loadBlotInfoList != null && info.loadBlotInfoList.size() > 0)
							{
								for(LoadBlotInfo loadBlotInfo : info.loadBlotInfoList) {
									BatchLot batchLot = new BatchLot();
									batchLot.nTrxnNo = stockHInfo.nTrxnNo;
									batchLot.strItemNo = info.strItemNo;
									batchLot.nLocnNo = stockDInfo.nLocnNo;
									batchLot.nReasonCode = stockDInfo.nReasonCode;
									batchLot.nSequence = stockDInfo.nSequence;
									batchLot.strLotNumber = loadBlotInfo.strLotNumber;
									batchLot.strBatchDate = loadBlotInfo.strBatchDate;
									batchLot.nActualQty = loadBlotInfo.nLotQty;
									batchLot.nSuggestQty = loadBlotInfo.nLotQty;
									dbAdapter.insertBatchLot(batchLot);
								}
							}
						}

						dbAdapter.insertUStockDInfoList(stockDInfo);
						if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
						{
							info.dStock = info.dAdjQty;
						}
						else
						{
							info.nStock = info.nAdjQty;
						}
						if( info.nIdx == -1 )
						{
							info.nTrxnNo = Utils.getLongSetting(mContext, Global.PREF_UINVEN_TRXN_NO);
							info.nSCWType = Global.INVENTORY_FRESH + 1;
							info.nIdx = (int)dbAdapter.insertUInvenDInfo(info);
						}
						else
						{
							dbAdapter.updateUInvenDInfo(info);
						}
						info.nTrxnNo = mPdfTrxnNo;
						info.nIdx = (int)dbAdapter.insertUInvenDInfo(info);

						uInvenBlotDInfoList.addAll(Utils.getUInvenBlotList(info));
					}

					if(uInvenBlotDInfoList.size() > 0)
					{
						dbAdapter.insertULoadBlot(uInvenBlotInfo);
						dbAdapter.insertULoadBlotList(uInvenBlotDInfoList);
					}
					dbAdapter.close();
				}
				//Create UInven and UStock file

				doLastProcess();
				new Handler(Looper.getMainLooper()).post(() -> {
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
									m_bCompletedClicked = false;
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
					else
					{
						goBack();
						m_bCompletedClicked = false;
					}
				});
			}
		}.start();
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
							m_bCompletedClicked = false;
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
						} else if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
							m_bCompletedClicked = false;
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
    };

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

	private void doLastProcess()
	{
		if(bLastProcess) return;
		bLastProcess = true;
		//Global.gUploadCustNoList.add(makeZipFileInfo);
		Utils.registerUploadFile(mContext, makeZipFileInfo);
	}

	boolean bPaused = true;
	boolean bNeedBack = false;
	private void goBack() {
		if(bPaused)
		{
			bNeedBack = true;
		}
		else
		{
			((StockTakeCountEntryFragment)Global.gFragmentForCheckingModification).bModified = false;
			mContext.onBackPressed();
			mContext.onBackPressed();
//			mContext.onBackPressed();
		}
	}

	public boolean m_bClicked = false;
	public boolean m_bCompletedClicked = false;
	@Override
	public void onClick(View v) {
		if( m_bClicked ) return;
		m_bClicked = true;
		switch (v.getId()) {
			case R.id.btnSignature:
				Intent intent = new Intent(mContext, SignatureActivity.class);
				intent.putExtra(Global.EXTRA_SIGNATURE_PATH, Global.DEFAULT_SIGNATURE_FILEPATH);
				startActivity(intent);
				mContext.overridePendingTransition(R.anim.zoom_in, 0);
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
									m_bCompletedClicked = false;
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
					else
					{
						goBack();
						m_bCompletedClicked = false;
					}

				}
				break;
			case R.id.btnBack:
				mContext.onBackPressed();
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

	private List<DescTypeInfo> loadDescTypeList(String strDescriptionTypeID) {
		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		List<DescTypeInfo> list = dbAdapter.getDescTypeList(strDescriptionTypeID);
		dbAdapter.close();
		return list;
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
					m_bCompletedClicked = false;
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
			}catch(Exception e)
			{
				e.printStackTrace();
			}
			
		}
	}
}