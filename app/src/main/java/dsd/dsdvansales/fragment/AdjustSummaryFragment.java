package dsd.dsdvansales.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.*;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.*;
import android.text.TextUtils;
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
import com.itextpdf.text.Image;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.zebra.sdk.comm.*;
import com.zebra.sdk.printer.*;
import com.zebra.sdk.printer.discovery.*;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CountDownLatch;

import dsd.dsdvansales.R;
import dsd.dsdvansales.activities.SignatureActivity;
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
import dsd.dsdvansales.model.BatchLot;
import dsd.dsdvansales.model.CustConBalInfo;
import dsd.dsdvansales.model.DescTypeInfo;
import dsd.dsdvansales.model.LoadBlotInfo;
import dsd.dsdvansales.model.MakeZipFileInfo;
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

public class AdjustSummaryFragment extends BaseFragment implements View.OnClickListener {

	private Font fontBlack7Bold;
	private Font fontBlack10Bold;
	private Font fontBlack7Normal;

	private int mTemperatureIdx = 0;
	private List<Float> mTemperatureList = new ArrayList<>();

	private String mStrPdfFileName = "";
	private long mPdfTrxnNo = 0;
	private int mTruckCnt = 0;
	private int mContainerCnt = 0;
	private Date mTrxnDate = new Date();

	private Connection printerConnection = null;
	private String mStrMacAddress = "";
	private String mStrFriendlyName = "";
	private boolean mIsCreatedPDF = false;

	public int mTransferType = 0;
	private String mTransferRoute = "";
	private boolean mTransferLater = false;

	private int mInventoryType = Global.INVENTORY_FRESH;

	private boolean bShowCase = true;

	private String mCartonDescription = "";

	private DescTypeInfo descTypeInfo = null;

	private boolean bLastProcess = false;

	private UStockHInfo uStockHInfo = null;

	PrintEngine mHoneywellPrintEngine = null;
	String mHoneywellTemplatePath = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if(getArguments() != null )
		{
			Bundle args = getArguments();
			mTransferType = args.getInt(Global.EXTRA_TRANSFER_TYPE);
			mInventoryType = args.getInt(Global.EXTRA_INVENTORY_TYPE);
			mTransferRoute = args.getString(Global.EXTRA_TRANSFER_ROUTE);
			mTransferLater = args.getBoolean(Global.EXTRA_TRANSFER_LATER);
		}

		mRootView = inflater.inflate(R.layout.fragment_adjust_summary, container, false);
		commonInit();
		loadFont();
		return mRootView;
	}

	private void loadFont()
	{
		fontBlack7Bold		= Utils.getFont(Utils.getPrintFontFilePath(), Utils.getPrintEncoding(), Utils.getPrintEmbeded(), 7, Font.BOLD, BaseColor.BLACK);
		fontBlack10Bold		= Utils.getFont(Utils.getPrintFontFilePath(), Utils.getPrintEncoding(), Utils.getPrintEmbeded(), 10, Font.BOLD, BaseColor.BLACK);
		fontBlack7Normal		= Utils.getFont(Utils.getPrintFontFilePath(), Utils.getPrintEncoding(), Utils.getPrintEmbeded(), 7, Font.NORMAL, BaseColor.BLACK);
	}

	@Override
	public void onPause() {
		super.onPause();
		bPaused = true;
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
					mContext.sendBroadcast(new Intent(Global.ACTION_REFRESH_STOCK_VALUE));
					mContext.onBackPressed();
					mContext.onBackPressed();
					mContext.onBackPressed();
				}
			});

		}
	}

	@Override
	public void initView() {
		bShowCase = Utils.getStringSetting(mContext, Global.PREF_INVENTORYUOM).equals("U") == false;
		mRootView.findViewById(R.id.txtTemperature).setOnClickListener(this);
		mRootView.findViewById(R.id.btnSignature).setOnClickListener(this);
		mRootView.findViewById(R.id.btnCompleted).setOnClickListener(this);
		if(mTransferType == 2 || mTransferType == 3)
		{
			mRootView.findViewById(R.id.btnBack).setVisibility(View.GONE);
		}
		else
		{
			mRootView.findViewById(R.id.btnBack).setVisibility(View.VISIBLE);
			mRootView.findViewById(R.id.btnBack).setOnClickListener(this);
		}


		Global.gLoadStartedTime = new SimpleDateFormat("HH:mm:ss").format(new Date());
		loadData();
		refreshLanguage();

	}

	@Override
	public void loadData() {
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
		if(mTransferType == 0 || mTransferType == 3)
		{
			List<DescTypeInfo> descTypeInfos = dbAdapter.getDescTypeListWithNumericKey("TRANSINRSN", Utils.parseIntFromString(Global.gAdjustinfo.strReference));
			if(descTypeInfos.size() > 0)
			{
				descTypeInfo = descTypeInfos.get(0);
			}
		}
		else
		{
			List<DescTypeInfo> descTypeInfos = dbAdapter.getDescTypeListWithNumericKey("TRANSOTRSN", Utils.parseIntFromString(Global.gAdjustinfo.strReference));
			if(descTypeInfos.size() > 0)
			{
				descTypeInfo = descTypeInfos.get(0);
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
		if(mTransferType == 2 || mTransferType == 3)
		{
			((TextView) mRootView.findViewById(R.id.lblTitle)).setText(Str.TransferSummary[Global.LANG].toUpperCase());
		}
		else
		{
			((TextView) mRootView.findViewById(R.id.lblTitle)).setText(Str.AdjustmentSummary[Global.LANG].toUpperCase());
		}

		((TextView) mRootView.findViewById(R.id.txtLoadStarted)).setText(Str.LoadStartedAt[Global.LANG] + " " + Global.gLoadStartedTime);
		if(mTransferType == 0)
		{
			((TextView) mRootView.findViewById(R.id.lblYouHaveLoaded)).setText(Str.YouHaveLoaded[Global.LANG]);
		}
		else if(mTransferType == 1)
		{
			((TextView) mRootView.findViewById(R.id.lblYouHaveLoaded)).setText(Str.YouHaveUnLoaded[Global.LANG]);
		}
		else if(mTransferType == 2)
		{
			((TextView) mRootView.findViewById(R.id.lblYouHaveLoaded)).setText(Str.YouHaveTransferred[Global.LANG]);
		}
		else if(mTransferType == 3)
		{
			((TextView) mRootView.findViewById(R.id.lblYouHaveLoaded)).setText(Str.YouHaveReceived[Global.LANG]);
		}
		((TextView) mRootView.findViewById(R.id.lblLoadTemperature)).setText(Str.LoadTemperature[Global.LANG]);
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
					if (info.nSCWType == Global.INVENTORY_TRUCK) {
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

					} else if (info.nSCWType == Global.INVENTORY_CONTAINERS) {
						nContainers += info.nStock;
					}
				}
			}
		}

		String strLoaded = "";

		if(nCartons > 0)
		{
			if(nCartons > 1)
			{
				strLoaded += String.format(Locale.ENGLISH, "%d %ss", nCartons, Str.cartons[Global.LANG]);
			}
			else
			{
				strLoaded += String.format(Locale.ENGLISH, "%d %s", nCartons, Str.cartons[Global.LANG]);
			}

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
			if (Global.gFragIdx == Global.FRAG_ADJUST_SUMMARY && Global.gAdjustinfo != null) {
				Global.gAdjustinfo.strSignatureFilePath = intent.getStringExtra(Global.EXTRA_SIGNATURE_PATH);
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
							if( mTemperatureIdx < 0 )
							{
								return;
							}
							float fTemp = mTemperatureList.get(mTemperatureIdx);
							if( fTemp >= Utils.getFloatSetting(mContext, Global.PREF_LOAD_TEMPMIN) && fTemp <= Utils.getFloatSetting(mContext, Global.PREF_LOAD_TEMPMAX))
							{
								refreshPage();
							}
							else
							{
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

			phrase = new Phrase("LOAD SHEET", fontBlack10Bold);
			cell = new PdfPCell(phrase);
			cell.setPaddingTop(5);
			cell.setColspan(2);
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell.setBorder(Rectangle.NO_BORDER);
			table.addCell(cell);

			phrase = new Phrase(String.format(Locale.ENGLISH, "Date	   %s", new SimpleDateFormat(Utils.getDateFormat(mContext, "dd/MM/yyyy")+ " hh:mm aa").format(mTrxnDate)), fontBlack7Normal);
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
			for (UInvenDInfo info : Global.gAdjustinfo.uInvenDList) {
				if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
				{
					double dValue = info.dAdjQty;

					if (/*info.bShow == true &&*/ info.nSCWType == Global.INVENTORY_TRUCK && dValue > 0) {
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
						phrase = new Phrase(String.format(Locale.ENGLISH, "%.2f", dValue), fontBlack7Normal);
						cell = new PdfPCell(phrase);
						cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
						cell.setBorder(Rectangle.NO_BORDER);
						table.addCell(cell);
						dTotal += dValue;
					}
				}
				else
				{
					int nValue = info.nAdjQty;

					if (/*info.bShow == true &&*/ info.nSCWType == Global.INVENTORY_TRUCK && nValue > 0) {
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
						phrase = new Phrase(String.format(Locale.ENGLISH, "%d", nValue), fontBlack7Normal);
						cell = new PdfPCell(phrase);
						cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
						cell.setBorder(Rectangle.NO_BORDER);
						table.addCell(cell);
						nTotal += nValue;
					}
				}

			}
			if (mContainerCnt > 0) {
				phrase = new Phrase("Containers", fontBlack7Normal);
				cell = new PdfPCell(phrase);
				cell.setPaddingTop(5);
				cell.setColspan(2);
				cell.setBorder(Rectangle.BOTTOM);
				table.addCell(cell);
				phrase = new Phrase("Qty", fontBlack7Normal);
				cell = new PdfPCell(phrase);
				cell.setPaddingTop(5);
				cell.setBorder(Rectangle.BOTTOM);
				table.addCell(cell);
				for (UInvenDInfo info : Global.gAdjustinfo.uInvenDList) {
					if (/*info.bShow == true &&*/ info.nSCWType == Global.INVENTORY_CONTAINERS && info.nStock > 0) {
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

			phrase = new Phrase("Printed: " + new SimpleDateFormat(Utils.getDateFormat(mContext, "dd/MM/yyyy") + "hh:mm aa").format(new Date()), fontBlack7Normal);
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
		if (Global.gAdjustinfo != null) {
			DbAdapter dbAdapter = new DbAdapter(mContext);
			dbAdapter.open();

			for (UInvenDInfo info : Global.gAdjustinfo.uInvenDList) {
				if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
				{
					if (info.nSCWType == Global.INVENTORY_TRUCK && info.dStock > 0) {
						mTruckCnt++;
					} else if (info.nSCWType == Global.INVENTORY_CONTAINERS && info.dStock > 0) {
						mContainerCnt++;
					}
					if( mTransferType == 0 || mTransferType == 3)
					{
						info.dTransIn += info.dAdjQty;
					}
					else
					{
						info.dTransOut += info.dAdjQty;
					}
					if( info.nIdx == -1 )
					{
						info.nTrxnNo = Utils.getLongSetting(mContext, Global.PREF_UINVEN_TRXN_NO);
						info.nSCWType = mInventoryType + 1;
						info.nIdx = (int)dbAdapter.insertUInvenDInfo(info);
					}
					else
					{
						dbAdapter.updateUInvenDInfo(info);
					}
				}
				else
				{
					if (info.nSCWType == Global.INVENTORY_TRUCK && info.nStock > 0) {
						mTruckCnt++;
					} else if (info.nSCWType == Global.INVENTORY_CONTAINERS && info.nStock > 0) {
						mContainerCnt++;
					}
					if( mTransferType == 0 || mTransferType == 3)
					{
						info.nTransIn += info.nAdjQty;
					}
					else
					{
						info.nTransOut += info.nAdjQty;
					}
					if( info.nIdx == -1 )
					{
						info.nTrxnNo = Utils.getLongSetting(mContext, Global.PREF_UINVEN_TRXN_NO);
						info.nSCWType = mInventoryType + 1;
						info.nIdx = (int)dbAdapter.insertUInvenDInfo(info);
					}
					else
					{
						dbAdapter.updateUInvenDInfo(info);
					}
				}

			}

			dbAdapter.close();
		}
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
				String strTemplateFileName = Utils.getFinalPrintTemplateFileName(mContext, Global.PRINT_LOAD_SHEET_TEMPLATE_FILENAME);
				if(Utils.isFileExist(Global.ROOT_PRINT_TEMPLATE_LOCCATION + "/" + strTemplateFileName))
				{
					PrintEngine printEngine = new PrintEngine();
					mHoneywellPrintEngine = printEngine;

					Utils.createDirectory(Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/");
					mPdfTrxnNo = System.currentTimeMillis();
					mStrPdfFileName = getPDFFileName();

					List<UInvenDInfo> truckList = new ArrayList<>();
					List<UInvenDInfo>  containerList = new ArrayList<>();
					for (UInvenDInfo info : Global.gAdjustinfo.uInvenDList) {
						if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
						{
							double dValue = info.dAdjQty;
							UInvenDInfo info1 = info.copyInfo();
							info1.dStock = dValue;
							if (info.bShow == true && info.nSCWType == Global.INVENTORY_TRUCK && info1.dStock > 0) {
								truckList.add(info1);
							}
						}
						else
						{
							int nValue = info.nAdjQty;
							UInvenDInfo info1 = info.copyInfo();
							info1.nStock = nValue;
							if (info.bShow == true && info.nSCWType == Global.INVENTORY_TRUCK && info1.nStock > 0) {
								truckList.add(info1);
							}
						}

					}
					if (mContainerCnt > 0) {
						for (UInvenDInfo info : Global.gAdjustinfo.uInvenDList) {
							if (info.bShow == true && info.nSCWType == Global.INVENTORY_CONTAINERS && info.nStock > 0) {
								containerList.add(info);
							}
						}
					}
					String strTitle = "";
					List<DescTypeInfo> reasonInList = null;
					if( mTransferType == 0 || mTransferType == 3)
					{
						reasonInList = loadDescTypeList("TRANSINRSN");
						strTitle = "ADJUST INVENTORY – IN";
					}
					else
					{
						reasonInList = loadDescTypeList("TRANSOTRSN");
						strTitle = "ADJUST INVENTORY – OUT";
					}
					String strType = "";
					if(!TextUtils.isEmpty(Global.gAdjustinfo.strReference))
					{
						for(DescTypeInfo info : reasonInList )
						{
							if( info.nNumericKey == Utils.parseIntFromString(Global.gAdjustinfo.strReference) )
							{
								strType = info.strDescription;
								break;
							}
						}
					}

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
					if(containerList != null)
					{
						for(UInvenDInfo info : containerList)
						{
							ProdDetlInfo prodDetlInfo = dbAdapter.getProdDetl(info.strItemNo);
							if(prodDetlInfo != null)
							{
								Utils.gDetlMap.put(prodDetlInfo.strItemNo, Utils.getPropertyMap(prodDetlInfo));
							}
						}
					}
					dbAdapter.close();

					printEngine.PreparePrint(mContext, truckList, containerList, strType, mTrxnDate,
							Global.gAdjustinfo.strSignatureFilePath, mStrPdfFileName.substring(0, mStrPdfFileName.length() - 4), strTitle, mTransferRoute);

					String strHoneywellFileName = Utils.getFinalPrintTemplateFileNameForHoneywell(mContext, Global.PRINT_LOAD_ADJUSTMENT_TEMPLATE_FILENAME);
					mHoneywellTemplatePath = Global.ROOT_PRINT_TEMPLATE_LOCCATION + strHoneywellFileName;
					printEngine.createDocument(mContext, false, Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + mStrPdfFileName, Global.LOAD_ADJUSTMENT_PRINT);
				}
				else
				{
					createPDF();
				}

				initMakeZipInfo();
				String strDocNo = mStrPdfFileName.substring(0, mStrPdfFileName.length() - 4);
				long nTrxnNo = setGeneralUploadData((mTransferType == 0 || mTransferType == 3) ? "TRNI" : "TRNO", 0, 0, mPdfTrxnNo, "", strDocNo);

				TransactionInfo transactionInfo = new TransactionInfo();
				transactionInfo.strType = (mTransferType == 0 || mTransferType == 3) ? "TRNI" : "TRNO";
				transactionInfo.strTime = new SimpleDateFormat("kk:mm").format(new Date(nTrxnNo));
				transactionInfo.strCustomerName = "";
				transactionInfo.strDocNo = strDocNo;
				DbAdapter dbAdapter = new DbAdapter(mContext);
				dbAdapter.open();
				dbAdapter.insertTransactionHistoryInfo(transactionInfo);
				dbAdapter.close();

				UTransactionsInfo uTransactionsInfo = setUTransactionsInfo(0, 0, m_lTrxnNo, "FARC", mStrPdfFileName);
				setUFileTransactionInfo(uTransactionsInfo.nTrxnNo, 0, 0, mPdfTrxnNo, strDocNo,
						"STOCKADJUST", "STOCK ADJUST", uTransactionsInfo.strTrxnDate,
						uTransactionsInfo.strTrxnTime, mStrPdfFileName);
				AddTrxnNo(0, 0, mPdfTrxnNo);
				//Get ReasonCode from the Reference . Don't forget this section. Very important.
				int nReasonCode = Utils.parseIntFromString(Global.gAdjustinfo.strReference);
				if (Global.gAdjustinfo != null) {
					Global.gAdjustinfo.nTrxnNo = mPdfTrxnNo;
					Global.gAdjustinfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(new Date(Global.gAdjustinfo.nTrxnNo));
					Global.gAdjustinfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(new Date(Global.gAdjustinfo.nTrxnNo));
					if(mTemperatureIdx >= 0 && mTemperatureIdx < mTemperatureList.size())
					{
						String strTempFmt = Utils.getStringSetting(mContext, Global.PREF_TEMPFMT);
						if(TextUtils.isEmpty(strTempFmt) || strTempFmt.endsWith("C"))
						{
							Global.gAdjustinfo.strReference = String.format(Locale.ENGLISH, "%.1f", mTemperatureList.get(mTemperatureIdx));
						}
						else
						{
							Global.gAdjustinfo.strReference = String.format(Locale.ENGLISH, "%d", (int)mTemperatureList.get(mTemperatureIdx).floatValue());
						}

					}
					else
					{
						Global.gAdjustinfo.strReference = "";
					}
					Global.gAdjustinfo.strDocType = "IVUP";
					uStockHInfo = new UStockHInfo();
					uStockHInfo.strDocNo = mStrPdfFileName.substring(0, mStrPdfFileName.length() - 4);
					uStockHInfo.strReference = uStockHInfo.strDocNo + ".pdf";

					uStockHInfo.nTrxnNo = Global.gAdjustinfo.nTrxnNo;
					/*if(descTypeInfo == null || TextUtils.isEmpty(descTypeInfo.strValue2))
					{
						stockHInfo.strStockType = "2";
					}
					else
					{
						stockHInfo.strStockType = mTransferType == 0 ? "3" : "4";
					}*/

					//Changed from Mr.Andrew --- 7/20 --- VS170
					if(mTransferType == 0)
					{
						uStockHInfo.strStockType = "2";
					}
					else if(mTransferType == 1)
					{
						uStockHInfo.strStockType = "3";
					}
					else if(mTransferType == 2)
					{
						if(mTransferLater)
						{
							uStockHInfo.strStockType = "31";
						}
						else
						{
							uStockHInfo.strStockType = "3";
						}
					}
					else if(mTransferType == 3)
					{
						uStockHInfo.strStockType = "2";
					}

					if(mTransferType == 2 || mTransferType == 3)
					{
						uStockHInfo.strTransferRoute = mTransferRoute;
					}
					if(descTypeInfo != null)
					{
						uStockHInfo.strTransferRoute = descTypeInfo.strValue2;
						uStockHInfo.strToWarehouse = descTypeInfo.strValue1;
					}
					uStockHInfo.strTrxnDate = Global.gAdjustinfo.strTrxnDate;
					uStockHInfo.strTrxnTime = Global.gAdjustinfo.strTrxnTime;
					uStockHInfo.strDocType = (mTransferType == 0 || mTransferType == 3) ? "TRNI" : "TRNO";

					dbAdapter = new DbAdapter(mContext);
					dbAdapter.open();

					PrintItem printItem = new PrintItem();
					printItem.nTrxnNo = uStockHInfo.nTrxnNo;
					printItem.strDocNo = uStockHInfo.strDocNo;
					printItem.strDocType = uStockHInfo.strDocType;
					printItem.strFilePath = Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + mStrPdfFileName;
					printItem.nCustNo = 0;
					printItem.nChainNo = 0;
					printItem.strCustomerName = "";

					dbAdapter.insertPrint(printItem);

					dbAdapter.insertUStockHInfoList(uStockHInfo);
					dbAdapter.insertUInvenHInfo(Global.gAdjustinfo);

					UInvenBlotInfo uInvenBlotInfo = Utils.getUInvenBlot(Global.gAdjustinfo, mStrPdfFileName.substring(0, mStrPdfFileName.length() - 4), Global.gTripNumber);
					List<UInvenBlotDInfo> uInvenBlotDInfoList = new ArrayList<>();

					for (UInvenDInfo info : Global.gAdjustinfo.uInvenDList) {
						if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
						{
							if (info.nSCWType == Global.INVENTORY_TRUCK && info.dStock > 0) {
								mTruckCnt++;
							} else if (info.nSCWType == Global.INVENTORY_CONTAINERS && info.dStock > 0) {
								mContainerCnt++;
							}

							info.nTrxnNo = mPdfTrxnNo;
							info.nIdx = (int)dbAdapter.insertUInvenDInfo(info);
							uInvenBlotDInfoList.addAll(Utils.getUInvenBlotList(info));

							if( info.dAdjQty > 0)
							{
								UStockDInfo stockDInfo = new UStockDInfo();
								stockDInfo.nTrxnNo = mPdfTrxnNo;
								stockDInfo.strItemNo = info.strItemNo;
								stockDInfo.nReasonCode = nReasonCode;
								stockDInfo.dActualQty = info.dAdjQty;
								if( mTransferType == 0 || mTransferType == 3)
								{
									stockDInfo.dBeginQty = info.dStock - info.dAdjQty;
								}
								else
								{
									stockDInfo.dBeginQty = info.dStock + info.dAdjQty;
								}
								stockDInfo.dSuggestQty = 0;

								dbAdapter.insertUStockDInfoList(stockDInfo);

								if(Utils.getIntSetting(mContext, Global.PREF_LOAD_CUSTOMER) != 1)
								{
									if(info.bLotControl && info.loadBlotInfoList != null && info.loadBlotInfoList.size() > 0)
									{
										for(LoadBlotInfo loadBlotInfo : info.loadBlotInfoList) {
											BatchLot batchLot = new BatchLot();
											batchLot.nTrxnNo = stockDInfo.nTrxnNo;
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
							}
						}
						else
						{
							if (info.nSCWType == Global.INVENTORY_TRUCK && info.nStock > 0) {
								mTruckCnt++;
							} else if (info.nSCWType == Global.INVENTORY_CONTAINERS && info.nStock > 0) {
								mContainerCnt++;
							}

							info.nTrxnNo = mPdfTrxnNo;
							info.nIdx = (int)dbAdapter.insertUInvenDInfo(info);

							if( info.nAdjQty > 0)
							{
								UStockDInfo stockDInfo = new UStockDInfo();
								stockDInfo.nTrxnNo = mPdfTrxnNo;
								stockDInfo.strItemNo = info.strItemNo;
								stockDInfo.nReasonCode = nReasonCode;
								stockDInfo.nActualQty = info.nAdjQty;
								if( mTransferType == 0 || mTransferType == 3)
								{
									stockDInfo.nBeginQty = info.nStock - info.nAdjQty;
								}
								else
								{
									stockDInfo.nBeginQty = info.nStock + info.nAdjQty;
								}
								stockDInfo.nSuggestQty = 0;

								dbAdapter.insertUStockDInfoList(stockDInfo);

								if(Utils.getIntSetting(mContext, Global.PREF_LOAD_CUSTOMER) != 1)
								{
									if(info.bLotControl && info.loadBlotInfoList != null && info.loadBlotInfoList.size() > 0)
									{
										for(LoadBlotInfo loadBlotInfo : info.loadBlotInfoList) {
											BatchLot batchLot = new BatchLot();
											batchLot.nTrxnNo = stockDInfo.nTrxnNo;
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
							}
						}
					}

					if(uInvenBlotDInfoList.size() > 0)
					{
						dbAdapter.insertULoadBlot(uInvenBlotInfo);
						dbAdapter.insertULoadBlotList(uInvenBlotDInfoList);
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
							m_bCompletedClicked = false;
						}
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

		try
		{
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

	private void doLastProcess()
	{
		if(bLastProcess) return;
		//Global.gUploadCustNoList.add(makeZipFileInfo);
		Utils.registerUploadFile(mContext, makeZipFileInfo);
		bLastProcess = true;
	}

	boolean bNeedBack = false;
	boolean bPaused = true;

	public boolean bPossibleBack = false;

	private void goBack() {
		bPossibleBack = true;
		if(bPaused)
		{
			bNeedBack = true;
		}
		else
		{
			mContext.sendBroadcast(new Intent(Global.ACTION_REFRESH_STOCK_VALUE));
			if(mTransferType == 3)
			{
				mContext.onBackPressed();
			}
			else if(mTransferType == 2)
			{
				mContext.onBackPressed();
				mContext.onBackPressed();
			}
			else
			{
				mContext.onBackPressed();
				mContext.onBackPressed();
				mContext.onBackPressed();
			}
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
				if( m_bCompletedClicked ) return;
				m_bCompletedClicked = true;
				if (mIsCreatedPDF == false) {
					doComplete();
				} else {
					doLastProcess();
					if( Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 1)
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
								doHoneywellLinePrintPdfTask();
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
			}, mStrMacAddress, mHoneywellTemplatePath, mHoneywellPrintEngine);
		}
		else {
			goBack();
			m_bCompletedClicked = false;
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
					Utils.setStringSetting(mContext, Global.PREF_BIXOLON_PRINTER_FRIENDLY_NAME, mStrMacAddress);
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
							doBixolonPrintPdfTask();
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