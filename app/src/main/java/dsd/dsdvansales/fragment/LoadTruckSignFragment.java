package dsd.dsdvansales.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.*;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.pdf.PdfRenderer;
import android.os.*;
import android.text.Html;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bxl.config.editor.BXLConfigLoader;
import com.honeywell.mobility.print.LinePrinter;
import com.itextpdf.awt.AsianFontMapper;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.languages.ArabicLigaturizer;
import com.zebra.sdk.comm.*;
import com.zebra.sdk.graphics.internal.ZebraImageAndroid;
import com.zebra.sdk.printer.*;
import com.zebra.sdk.printer.discovery.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
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
import dsd.dsdvansales.global.ZPLConverter;
import dsd.dsdvansales.model.BatchLot;
import dsd.dsdvansales.model.CustomerInfo;
import dsd.dsdvansales.model.DescTypeInfo;
import dsd.dsdvansales.model.LoadBlotInfo;
import dsd.dsdvansales.model.LoadDetlInfo;
import dsd.dsdvansales.model.LoadHeadInfo;
import dsd.dsdvansales.model.PresoldOrDInfo;
import dsd.dsdvansales.model.PresoldOrHInfo;
import dsd.dsdvansales.model.PrintItem;
import dsd.dsdvansales.model.ProdDetlInfo;
import dsd.dsdvansales.model.StatusInfo;
import dsd.dsdvansales.model.TransactionInfo;
import dsd.dsdvansales.model.UInvenBlotDInfo;
import dsd.dsdvansales.model.UInvenBlotInfo;
import dsd.dsdvansales.model.UInvenDInfo;
import dsd.dsdvansales.model.UInvenHInfo;
import dsd.dsdvansales.model.UOrderStatusDInfo;
import dsd.dsdvansales.model.UOrderStatusHInfo;
import dsd.dsdvansales.model.UStockDInfo;
import dsd.dsdvansales.model.UStockHInfo;
import dsd.dsdvansales.model.UTransactionsInfo;
import dsd.dsdvansales.views.AlertSingleChoiceAdapter;

public class LoadTruckSignFragment extends BaseFragment implements View.OnClickListener {

	private Font fontBlack7Bold;
	private Font fontBlack10Bold;
	private Font fontBlack7Normal;

	private int mTemperatureIdx = -1;
	private List<Float> mTemperatureList = new ArrayList<>();

	private String mStrPdfFileName = Global.strLoadPdfFileName;
	private long mPdfTrxnNo = 0;
	private int mTruckCnt = 0;
	private int mContainerCnt = 0;
	private Date mTrxnDate = new Date();

	private Connection printerConnection = null;
	private String mStrMacAddress = "";
	private String mStrFriendlyName = "";
	private boolean mIsCreatedPDF = false;

	private String mCartonDescription = "";

	private boolean bShowCase = true;
	private boolean bProcessLastProcess = false;
//	Bitmap mPdfImage = null;

	PrintEngine mHoneywellPrintEngine = null;
	String mHoneywellTemplatePath = null;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootView = inflater.inflate(R.layout.fragment_load_truck_sign, container, false);
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
	public void onResume() {
		super.onResume();
		Global.gFragIdx = Global.FRAG_LOAD_TRUCK_SIGN;
		bPaused = false;
		if(bNeedBack)
		{
			new Handler(Looper.getMainLooper()).post(new Runnable() {
				@Override
				public void run() {
					mContext.replaceFragment(new PreTripTasksFragment());
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
		dbAdapter.close();

		if (Global.gUInvenHinfo == null) {
			Global.gUInvenHinfo = new UInvenHInfo();
		}

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
		((TextView) mRootView.findViewById(R.id.lblTitle)).setText(Str.LoadTruck[Global.LANG].toUpperCase());
		((TextView) mRootView.findViewById(R.id.txtLoadStarted)).setText(Str.LoadStartedAt[Global.LANG] + " " + Global.gLoadStartedTime);
		((TextView) mRootView.findViewById(R.id.lblYouHaveLoaded)).setText(Str.YouHaveLoaded[Global.LANG]);
		((TextView) mRootView.findViewById(R.id.lblLoadTemperature)).setText(Str.LoadTemperature[Global.LANG]);
		((Button) mRootView.findViewById(R.id.btnSignature)).setText(Str.Signature[Global.LANG]);
		((Button) mRootView.findViewById(R.id.btnCompleted)).setText(Str.Completed[Global.LANG]);

		//		int nCartons = 0;
		int nUnits = 0;
		int nContainers = 0;
		double dWeight = 0;
		if (Global.gUInvenHinfo != null) {
			for (UInvenDInfo info : Global.gUInvenHinfo.truckList) {
				if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
				{
					dWeight += info.dStock;
				}
				else
				{
					if( info.nSCWType == 2 )
					{
						boolean bExist = false;
						for (UInvenDInfo containerInfo : Global.gUInvenHinfo.containerList){
							if(containerInfo.strItemNo.equals(info.strItemNo))
							{
								bExist = true;
								break;
							}
						}
						if(!bExist)
						{
							nContainers += info.nStock;
						}
					}
					else
					{
/*					Remove by DEL 42 at 11th of May, 2018
					if (info.strFullCase.equalsIgnoreCase("Y") == true) {
						nCartons += info.nStock;
					} else {
						nUnits += info.nStock;
					}*/
						nUnits += info.nStock;
					}
				}

			}
			if (Global.gUInvenHinfo.containerList != null) {
				for (UInvenDInfo info : Global.gUInvenHinfo.containerList) {
					nContainers += info.nStock;
				}
			}
		}

		String strLoaded = "";

		if (Utils.getIntSetting(mContext, Global.PREF_LOAD_CONTAINERS) == 2) {
			nContainers = 0;
			for (UInvenDInfo info : Global.gUInvenHinfo.truckList) {
				if (Utils.isContainerItem(mContext, info.strItemNo)) {
					nContainers += info.nStock;
				}
			}
			strLoaded = String.format(Locale.ENGLISH, "<b>%d %s</b>", nContainers, Str.containers[Global.LANG]);
			((TextView) mRootView.findViewById(R.id.txtLoaded)).setText(Html.fromHtml(strLoaded));
		}
		else {
			if(nUnits != 0)
			{
				String strCarton = "";
				if( TextUtils.isEmpty(mCartonDescription) )
				{
					strCarton = "unit";
				}
				else
				{
					strCarton = mCartonDescription;
				}
				if(nUnits > 1 )
				{
					strLoaded = String.format(Locale.ENGLISH, "<b>%d %ss</b>", nUnits, strCarton);
				}
				else
				{
					strLoaded = String.format(Locale.ENGLISH, "<b>%d %s</b>", nUnits, strCarton);
				}
			}
			if(nContainers != 0)
			{
				String strCaption = "";
				if(nContainers > 1)
				{
					strCaption = String.format(Locale.ENGLISH, "<b>%d %ss</b>", nContainers, Str.containers[Global.LANG]);
				}
				else
				{
					strCaption = String.format(Locale.ENGLISH, "<b>%d %s</b>", nContainers, Str.containers[Global.LANG]);
				}
				if(TextUtils.isEmpty(strLoaded))
				{
					strLoaded = strCaption;
				}
				else
				{
					strLoaded += "<br>" + strCaption;
				}
			}
			if(dWeight != 0)
			{
				if(TextUtils.isEmpty(strLoaded))
				{
					strLoaded = String.format(Locale.ENGLISH, "<b>%.2f Kg</b>", dWeight);
				}
				else
				{
					strLoaded += "<br>" + String.format(Locale.ENGLISH, "<b>%.2f Kg</b>", dWeight);
				}
			}
			((TextView) mRootView.findViewById(R.id.txtLoaded)).setText(Html.fromHtml(strLoaded));
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
							if(mTemperatureIdx < 0)
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

			String strDateTime = Utils.getRoutectlTripDateTime(mContext, "dd/MM/yyyy", " hh:mm aa", mTrxnDate);
			phrase = new Phrase(String.format(Locale.ENGLISH, "Date	   %s", strDateTime), fontBlack7Normal);
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
			boolean bLoadByCustomer = Utils.getIntSetting(mContext, Global.PREF_LOAD_CUSTOMER) == 1 || Utils.getIntSetting(mContext, Global.PREF_LOAD_CUSTOMER) == 2;
			for (UInvenDInfo info : Global.gUInvenHinfo.truckList) {
				if(info.bShow == true && info.nStock > 0 && !bLoadByCustomer)
				{
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
						phrase = new Phrase(String.format(Locale.ENGLISH, "%.2f", info.dStock), fontBlack7Normal);
						dTotal += info.dStock;
					}
					else
					{
						phrase = new Phrase(String.format(Locale.ENGLISH, "%d", info.nStock), fontBlack7Normal);
						nTotal += info.nStock;
					}
					cell = new PdfPCell(phrase);
					cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
					cell.setBorder(Rectangle.NO_BORDER);
					table.addCell(cell);
				}
				else if( info.bCompleted == true && info.nStock > 0 && bLoadByCustomer){
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
						phrase = new Phrase(String.format(Locale.ENGLISH, "%.2f", info.dStock), fontBlack7Normal);
						dTotal += info.dStock;
					}
					else
					{
						phrase = new Phrase(String.format(Locale.ENGLISH, "%d", info.nStock), fontBlack7Normal);
						nTotal += info.nStock;
					}
					cell = new PdfPCell(phrase);
					cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
					cell.setBorder(Rectangle.NO_BORDER);
					table.addCell(cell);
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
				for (UInvenDInfo info : Global.gUInvenHinfo.containerList) {
					if (info.nStock > 0) {
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
			else if( dTotal > 0)
			{
				phrase = new Phrase(String.format(Locale.ENGLISH, "%.2f Kg", dTotal), fontBlack7Normal);
			}
			else if( nTotal > 0)
			{
				phrase = new Phrase(String.format(Locale.ENGLISH, "%d", nTotal), fontBlack7Normal);
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

			if (Global.gUInvenHinfo != null && Global.gUInvenHinfo.strSignatureFilePath.length() > 0 && Utils.isFileExist(Global.gUInvenHinfo.strSignatureFilePath) == true) {
				Image image = Image.getInstance(Global.gUInvenHinfo.strSignatureFilePath);
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

			String strDateTime = Utils.getRoutectlTripDateTime(mContext, "dd/MM/yyyy", " hh:mm aa", new Date());
			phrase = new Phrase("Printed: " + strDateTime, fontBlack7Normal);
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
			if(TextUtils.isEmpty(mStrPdfFileName))
			{
				mStrPdfFileName = getPDFFileName();
			}
			int nHeight = 250 + 17 * mTruckCnt;
			if (mContainerCnt > 0) {
				nHeight += 17 * (mContainerCnt + 1);
			}
			if (Global.gUInvenHinfo.strSignatureFilePath.length() > 0 && Utils.isFileExist(Global.gUInvenHinfo.strSignatureFilePath) == true) {
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

			Utils.fileCopy(Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + mStrPdfFileName, Global.PDF_LOCAL_DIR + mStrPdfFileName);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void doMakeUInventory(long trxnNo)
	{
		mTruckCnt = 0;
		mContainerCnt = 0;
		mTrxnDate = new Date(trxnNo);
		if (Global.gUInvenHinfo != null) {
			Global.gUInvenHinfo.nTrxnNo = trxnNo;
			Global.gUInvenHinfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(mContext, mTrxnDate));
			Global.gUInvenHinfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(mTrxnDate);
			if(mTemperatureIdx >= 0)
			{
				String strTempFmt = Utils.getStringSetting(mContext, Global.PREF_TEMPFMT);
				if(TextUtils.isEmpty(strTempFmt) || strTempFmt.endsWith("C"))
				{
					Global.gUInvenHinfo.strReference = String.format(Locale.ENGLISH, "%.1f", mTemperatureList.get(mTemperatureIdx));
				}
				else
				{
					Global.gUInvenHinfo.strReference = String.format(Locale.ENGLISH, "%d", (int)mTemperatureList.get(mTemperatureIdx).floatValue());
				}
			}
			else
			{
				Global.gUInvenHinfo.strReference = "";
			}
			if(Global.bTransferLoad)
			{
				Global.gUInvenHinfo.strDocType = "IVUP";
			}
			else {
				Global.gUInvenHinfo.strDocType = "LOAD";
			}

			Global.gUInvenHinfo.uInvenDList.clear();
			for (UInvenDInfo info : Global.gUInvenHinfo.truckList) {
				if (Utils.getIntSetting(mContext, Global.PREF_LOAD_CUSTOMER) == 1 || Utils.getIntSetting(mContext, Global.PREF_LOAD_CUSTOMER) == 2)
				{
					if (info.bCompleted == true) {
						info.nSCWType = Global.INVENTORY_TRUCK;
						Global.gUInvenHinfo.uInvenDList.add(info);
						if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
						{
							if (info.dStock > 0) {
								mTruckCnt++;
							}
						}
						else {
							if (info.nStock > 0) {
								mTruckCnt++;
							}
						}
					}
				}
				else
				{
					if (info.bShow == true) {
						info.nSCWType = Global.INVENTORY_TRUCK;
						Global.gUInvenHinfo.uInvenDList.add(info);
						if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
						{
							if (info.dStock > 0) {
								mTruckCnt++;
							}
						}
						else{
							if (info.nStock > 0) {
								mTruckCnt++;
							}
						}
					}
				}
			}

			if (Global.gUInvenHinfo.containerList != null) {
				for (UInvenDInfo info : Global.gUInvenHinfo.containerList) {
					info.nSCWType = Global.INVENTORY_CONTAINERS;
					info.nLoadAdj = info.nStock - info.nLoaded;
					Global.gUInvenHinfo.uInvenDList.add(info);
					if (info.nStock > 0) {
						mContainerCnt++;
					}
				}
			}
			DbAdapter dbAdapter = new DbAdapter(mContext);
			dbAdapter.open();
			dbAdapter.insertUInvenHInfo(Global.gUInvenHinfo);
			UInvenBlotInfo uInvenBlotInfo = Utils.getUInvenBlot(Global.gUInvenHinfo, mStrPdfFileName.substring(0, mStrPdfFileName.length() - 4), Global.gTripNumber);
			List<UInvenBlotDInfo> uInvenBlotDInfoList = new ArrayList<>();

			long paramNo = trxnNo;
			trxnNo = Utils.getLongSetting(mContext, Global.PREF_UINVEN_TRXN_NO);
			if( trxnNo == 0 )
			{
				trxnNo = System.currentTimeMillis();
				Utils.setLongSetting(mContext, Global.PREF_UINVEN_TRXN_NO, trxnNo);
			}

			List<UInvenDInfo> uInvenDInfos = dbAdapter.getUInvenDList(trxnNo);

			for (UInvenDInfo info : Global.gUInvenHinfo.uInvenDList) {
				info.nTrxnNo = Global.gUInvenHinfo.nTrxnNo;
				if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
				{
					if(Global.bTransferLoad)
					{
						info.dLoadAdj = info.dStock - info.dLoaded;
						info.dTransIn = info.dStock;
						info.dLoaded = 0;
						for(UInvenDInfo info1 : uInvenDInfos)
						{
							if( info1.strItemNo.equals(info.strItemNo))
							{
								info.dBeginQty += info1.dBeginQty;
								info.dStock += info1.dStock;
								info.dAdjQty = info.dLoadAdj;
								info.dLoadAdj += info1.dLoadAdj;
								info.dTransIn += info1.dTransIn;
								info.dSales += info1.dSales;
								info.dBuybacks += info1.dBuybacks;
								info.dDumps += info1.dDumps;
								break;
							}
						}
					}
					else {
						info.dLoadAdj = info.dStock - info.dLoaded;
						for(UInvenDInfo info1 : uInvenDInfos)
						{
							if( info1.strItemNo.equals(info.strItemNo))
							{
								info.dBeginQty += info1.dBeginQty;
								info.dStock += info1.dStock;
								info.dAdjQty = info.dLoadAdj;
								info.dLoadAdj += info1.dLoadAdj;
								info.dLoaded += info1.dLoaded;
								info.dSales += info1.dSales;
								info.dBuybacks += info1.dBuybacks;
								info.dDumps += info1.dDumps;
								break;
							}
						}
					}

					info.dBeginPresold = dbAdapter.getBeginPresoldCalcD(info.strItemNo);
					info.dResPresold = info.dBeginPresold;
					info.dBeginPeddle = info.dStock - info.dBeginPresold;
					info.dResPeddle = info.dBeginPeddle;
				}
				else
				{
					if(Global.bTransferLoad)
					{
						info.nLoadAdj = info.nStock - info.nLoaded;
						info.nTransIn = info.nStock;
						info.nLoaded = 0;
						for(UInvenDInfo info1 : uInvenDInfos)
						{
							if( info1.strItemNo.equals(info.strItemNo))
							{
								info.nBeginQty += info1.nBeginQty;
								info.nStock += info1.nStock;
								info.nAdjQty = info.nLoadAdj;
								info.nLoadAdj += info1.nLoadAdj;
								info.nTransIn += info1.nTransIn;
								info.nSales += info1.nSales;
								info.nBuybacks += info1.nBuybacks;
								info.nDumps += info1.nDumps;
								break;
							}
						}
					}
					else {
						info.nLoadAdj = info.nStock - info.nLoaded;
						for(UInvenDInfo info1 : uInvenDInfos)
						{
							if( info1.strItemNo.equals(info.strItemNo))
							{
								info.nBeginQty += info1.nBeginQty;
								info.nStock += info1.nStock;
								info.nAdjQty = info.nLoadAdj;
								info.nLoadAdj += info1.nLoadAdj;
								info.nLoaded += info1.nLoaded;
								info.nSales += info1.nSales;
								info.nBuybacks += info1.nBuybacks;
								info.nDumps += info1.nDumps;
								break;
							}
						}
					}

					info.nBeginPresold = dbAdapter.getBeginPresoldCalc(info.strItemNo);
					info.nResPresold = info.nBeginPresold;
					info.nBeginPeddle = info.nStock - info.nBeginPresold;
					info.nResPeddle = info.nBeginPeddle;
				}


				info.nIdx = (int)dbAdapter.insertUInvenDInfo(info);
				uInvenBlotDInfoList.addAll(Utils.getUInvenBlotList(info));
			}


			for(UInvenDInfo info : uInvenDInfos){
				boolean bAdd = true;
				for(UInvenDInfo info1 : Global.gUInvenHinfo.uInvenDList){
					if(info.strItemNo.equals(info1.strItemNo))
					{
						bAdd = false;
						break;
					}
				}
				if( bAdd )
				{
					info.nTrxnNo = Global.gUInvenHinfo.nTrxnNo;
					if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
					{
						info.dBeginPresold = dbAdapter.getBeginPresoldCalcD(info.strItemNo);
						info.dResPresold = info.dBeginPresold;
						info.dBeginPeddle = info.dStock - info.dBeginPresold;
						info.dResPeddle = info.dBeginPeddle;
					}
					else
					{
						info.nBeginPresold = dbAdapter.getBeginPresoldCalc(info.strItemNo);
						info.nResPresold = info.nBeginPresold;
						info.nBeginPeddle = info.nStock - info.nBeginPresold;
						info.nResPeddle = info.nBeginPeddle;
					}

					dbAdapter.insertUInvenDInfo(info);

					uInvenBlotDInfoList.addAll(Utils.getUInvenBlotList(info));

					info.nTrxnNo = trxnNo;
				}
			}

			if(uInvenBlotDInfoList.size() > 0)
			{
				dbAdapter.insertULoadBlot(uInvenBlotInfo);
				dbAdapter.insertULoadBlotList(uInvenBlotDInfoList);
			}

			for (UInvenDInfo info : Global.gUInvenHinfo.uInvenDList) {
				boolean bAdd = true;
				UInvenDInfo selInfo = null;
				for(UInvenDInfo info1 : uInvenDInfos )
				{
					if(info1.strItemNo.equals(info.strItemNo))
					{
						bAdd = false;
						selInfo = info1;
						break;
					}
				}
				if( bAdd )
				{
					UInvenDInfo info2 = info.copyInfo();
					info2.nTrxnNo = trxnNo;
					info2.bShow = true;
					if(Utils.getProdLoconSCWType(mContext, info2.strItemNo) == Global.WEIGHT_ITEM)
					{
						info2.dBeginPresold = dbAdapter.getBeginPresoldCalcD(info.strItemNo);
						info2.dResPresold = info2.dBeginPresold;
						info2.dBeginPeddle = info2.dStock - info.dBeginPresold;
						info2.dResPeddle = info2.dBeginPeddle;
					}
					else
					{
						info2.nBeginPresold = dbAdapter.getBeginPresoldCalc(info2.strItemNo);
						info2.nResPresold = info2.nBeginPresold;
						info2.nBeginPeddle = info2.nStock - info.nBeginPresold;
						info2.nResPeddle = info2.nBeginPeddle;
					}

					dbAdapter.insertUInvenDInfo(info2);
				}
				else
				{
					selInfo.bShow = true;
					selInfo.nLoaded = info.nLoaded;
					selInfo.nLoadAdj = info.nLoadAdj;
					selInfo.nStock = info.nStock;
					selInfo.nSales = info.nSales;
					selInfo.nDumps = info.nDumps;
					selInfo.nBuybacks = info.nBuybacks;
					selInfo.dLoaded = info.dLoaded;
					selInfo.dLoadAdj = info.dLoadAdj;
					selInfo.dStock = info.dStock;
					selInfo.dSales = info.dSales;
					selInfo.dDumps = info.dDumps;
					selInfo.dBuybacks = info.dBuybacks;


					if(Utils.getProdLoconSCWType(mContext, selInfo.strItemNo) == Global.WEIGHT_ITEM)
					{
						selInfo.dBeginPresold = dbAdapter.getBeginPresoldCalcD(selInfo.strItemNo);
						selInfo.dResPresold = selInfo.dBeginPresold;
						selInfo.dBeginPeddle = selInfo.dStock - selInfo.dBeginPresold;
						selInfo.dResPeddle = selInfo.dBeginPeddle;
					}
					else
					{
						selInfo.nBeginPresold = dbAdapter.getBeginPresoldCalc(selInfo.strItemNo);
						selInfo.nResPresold = selInfo.nBeginPresold;
						selInfo.nBeginPeddle = selInfo.nStock - selInfo.nBeginPresold;
						selInfo.nResPeddle = selInfo.nBeginPeddle;
					}

					dbAdapter.updateUInvenDInfo(selInfo);
				}
			}

			boolean bFirst = true;
			trxnNo = paramNo;
			if( Global.gUInvenHinfo != null /*&& Global.gUInvenHinfo.truckList != null && Global.gUInvenHinfo.truckList.size() > 0 */)
			{
				UStockHInfo stockHInfo = new UStockHInfo();
				int nDocNo = Utils.getIntSetting(mContext, String.format(Locale.ENGLISH, Global.PREF_PDF_SEQUENCE_NO, Global.gTripNumber));

				String strRRR;
				if (Global.gRouteNumber.length() > 3) {
					strRRR = Global.gRouteNumber.substring(Global.gRouteNumber.length() - 3);
				} else {
					strRRR = Global.gRouteNumber;
				}
				String strY = new SimpleDateFormat("yyyy").format(new Date()).substring(3);
				Calendar cal = Calendar.getInstance();
				int nDayOfYear = cal.get(Calendar.DAY_OF_YEAR);
				String strJJJ = String.format(Locale.ENGLISH, "%03d", nDayOfYear);
				String strSSS = String.format(Locale.ENGLISH, "%03d", nDocNo);
				stockHInfo.strDocNo = strRRR + strY + strJJJ + strSSS;

				stockHInfo.nTrxnNo = Global.gUInvenHinfo.nTrxnNo;
				if( Global.gUInvenHinfo != null )
				{
					stockHInfo.strTrxnDate = Global.gUInvenHinfo.strTrxnDate;
					stockHInfo.strTrxnTime = Global.gUInvenHinfo.strTrxnTime;
				}

				Map<String, Double> rBeginQtyMap = new HashMap<>();
				List<LoadHeadInfo> loadHeadInfoList = dbAdapter.getSelectedLoadHeadInfo();
				for(LoadHeadInfo loadHeadInfo : loadHeadInfoList)
				{
					//stockHInfo.nTrxnNo++;
					stockHInfo.strReference = loadHeadInfo.strType;
					if(loadHeadInfo.strType.equals("R"))
					{
						stockHInfo.nTrxnNo = setGeneralUploadData("LOAD", 0, 0, -1, loadHeadInfo.strDetailFile, "");
						TransactionInfo transactionInfo = new TransactionInfo();
						transactionInfo.strType = "LOAD";
						transactionInfo.strTime = new SimpleDateFormat("kk:mm").format(new Date(stockHInfo.nTrxnNo));
						dbAdapter.insertTransactionHistoryInfo(transactionInfo);

						stockHInfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(mContext, new Date(stockHInfo.nTrxnNo)));
						stockHInfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(stockHInfo.nTrxnNo);
						List<LoadDetlInfo> loadDetlInfoList = dbAdapter.getRLoadDetl(loadHeadInfo.strDetailFile);

						List<LoadBlotInfo> loadBlotInfoList = dbAdapter.getLoadBlot(loadHeadInfo.strDetailFile);
						Map<String, List<LoadBlotInfo>> loadBlotMap = new HashMap<>();
						for(LoadBlotInfo info : loadBlotInfoList)
						{
							List<LoadBlotInfo> infoList = loadBlotMap.get(info.strItemNo);
							if(infoList == null)
							{
								infoList = new ArrayList<>();
								loadBlotMap.put(info.strItemNo, infoList);
							}
							infoList.add(info);
						}

						for(LoadDetlInfo loadDetlInfo : loadDetlInfoList)
						{
							UStockDInfo stockDInfo = new UStockDInfo();
							stockDInfo.nTrxnNo = stockHInfo.nTrxnNo;
							stockDInfo.strItemNo = loadDetlInfo.strItemNo;
							stockDInfo.nReasonCode = 0;

							if(Utils.getProdLoconSCWType(mContext, loadDetlInfo.strItemNo) == Global.WEIGHT_ITEM)
							{
								double nValue = 0;
								if(rBeginQtyMap.get(loadDetlInfo.strItemNo) != null)
								{
									try {
										nValue += rBeginQtyMap.get(loadDetlInfo.strItemNo);
									}catch (Exception e)
									{
										e.printStackTrace();
									}
								}
								stockDInfo.dActualQty = loadDetlInfo.dQty;
								stockDInfo.dBeginQty = nValue;
								stockDInfo.dSuggestQty = loadDetlInfo.dQty;
								nValue += loadDetlInfo.dQty;
								rBeginQtyMap.put(loadDetlInfo.strItemNo, nValue);
							}
							else
							{
								double nValue = 0;
								if(rBeginQtyMap.get(loadDetlInfo.strItemNo) != null)
								{
									try {
										nValue += rBeginQtyMap.get(loadDetlInfo.strItemNo);
									}catch (Exception e)
									{
										e.printStackTrace();
									}
								}
								stockDInfo.nActualQty = loadDetlInfo.nQty;
								stockDInfo.nBeginQty = (int)nValue;
								stockDInfo.nSuggestQty = loadDetlInfo.nQty;
								nValue += loadDetlInfo.dQty;
								rBeginQtyMap.put(loadDetlInfo.strItemNo, nValue);
							}

							if(Utils.getIntSetting(mContext, Global.PREF_LOAD_CUSTOMER) != 1)
							{
								List<LoadBlotInfo> infoList = loadBlotMap.get(loadDetlInfo.strItemNo);
								if(infoList != null && infoList.size() > 0)
								{
									ProdDetlInfo prodDetlInfo = dbAdapter.getProdDetl(loadDetlInfo.strItemNo);
									if(prodDetlInfo.strLotControl.equalsIgnoreCase("Y"))
									{
										for(LoadBlotInfo loadBlotInfo : infoList)
										{
											BatchLot batchLot = new BatchLot();
											batchLot.nTrxnNo = stockHInfo.nTrxnNo;
											batchLot.strItemNo = prodDetlInfo.strItemNo;
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
							dbAdapter.insertUStockDInfoList(stockDInfo);
						}
					}
					else if( loadHeadInfo.strType.equals("L") || loadHeadInfo.strType.equals("T") )
					{
						stockHInfo.nTrxnNo = trxnNo;
						if(loadHeadInfo.strType.equals("T"))
						{
							stockHInfo.strDocType = "TRNI";
							stockHInfo.strTransferRoute = loadHeadInfo.strUser1;
							stockHInfo.strStockType = "2";
						}

						stockHInfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(mContext, new Date(trxnNo)));
						stockHInfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(trxnNo);
						for (UInvenDInfo info : Global.gUInvenHinfo.truckList) {
							//if(info.nReasonCode != -1)  //Removed by Wang for VS118.
							{
								UStockDInfo stockDInfo = new UStockDInfo();
								stockDInfo.nTrxnNo = stockHInfo.nTrxnNo;
								stockDInfo.strItemNo = info.strItemNo;
								stockDInfo.nReasonCode = (info.nReasonCode != -1) ? info.nReasonCode : 0;
								if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
								{
									//stockDInfo.dActualQty = info.dStock - info.dBeginQty + info.dSales;
									stockDInfo.dActualQty = info.dLoadAdj + info.dLoaded;
									stockDInfo.dBeginQty = info.dBeginQty;
									stockDInfo.dSuggestQty = info.dLoaded;
								}
								else
								{
									//stockDInfo.nActualQty = info.nStock - info.nBeginQty + info.nSales;
									stockDInfo.nActualQty = info.nLoadAdj + info.nLoaded;
									stockDInfo.nBeginQty = info.nBeginQty;
									stockDInfo.nSuggestQty = info.nLoaded;
								}

								if(Utils.getIntSetting(mContext, Global.PREF_LOAD_CUSTOMER) != 1 && info.loadBlotInfoList != null && info.loadBlotInfoList.size() > 0)
								{
									for(LoadBlotInfo loadBlotInfo : info.loadBlotInfoList)
									{
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

								dbAdapter.insertUStockDInfoList(stockDInfo);
							}
						}
					}
					stockHInfo.strDetailKeyExpanded = loadHeadInfo.strDetailFile;
					dbAdapter.insertUStockHInfoList(stockHInfo);
					if(stockHInfo.nTrxnNo != trxnNo)
					{
						AddTrxnNo(0, 0, stockHInfo.nTrxnNo);
					}
				}
			}
			if( Global.gUInvenHinfo != null && Global.gUInvenHinfo.containerList != null && Global.gUInvenHinfo.containerList.size() > 0 )
			{
				for (UInvenDInfo info : Global.gUInvenHinfo.containerList)
				{
					info.nReasonCode = 0;
					{
						if( bFirst )
						{
							UStockHInfo stockHInfo = new UStockHInfo();
							int nDocNo = Utils.getIntSetting(mContext, String.format(Locale.ENGLISH, Global.PREF_PDF_SEQUENCE_NO, Global.gTripNumber));

							String strRRR;
							if (Global.gRouteNumber.length() > 3) {
								strRRR = Global.gRouteNumber.substring(Global.gRouteNumber.length() - 3);
							} else {
								strRRR = Global.gRouteNumber;
							}
							String strY = new SimpleDateFormat("yyyy").format(new Date()).substring(3);
							Calendar cal = Calendar.getInstance();
							int nDayOfYear = cal.get(Calendar.DAY_OF_YEAR);
							String strJJJ = String.format(Locale.ENGLISH, "%03d", nDayOfYear);
							String strSSS = String.format(Locale.ENGLISH, "%03d", nDocNo);
							stockHInfo.strDocNo = strRRR + strY + strJJJ + strSSS;
							stockHInfo.strReference = stockHInfo.strDocNo + ".pdf";
							stockHInfo.nTrxnNo = Global.gUInvenHinfo.nTrxnNo;
							if( Global.gUInvenHinfo != null )
							{
								stockHInfo.strTrxnDate = Global.gUInvenHinfo.strTrxnDate;
								stockHInfo.strTrxnTime = Global.gUInvenHinfo.strTrxnTime;
							}
							List<LoadHeadInfo> loadHeadInfoList = dbAdapter.getSelectedLoadHeadInfo();
							//for(LoadHeadInfo loadHeadInfo : loadHeadInfoList)
							if( loadHeadInfoList.size() > 0 )
							{
								stockHInfo.strDetailKeyExpanded = loadHeadInfoList.get(0).strDetailFile;
								dbAdapter.insertUStockHInfoList(stockHInfo);
							}
						}
						bFirst = false;
						if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
						{
							if( info.dAdjQty != 0 )
							{
								UStockDInfo stockDInfo = new UStockDInfo();
								stockDInfo.nTrxnNo = Global.gUInvenHinfo.nTrxnNo;
								stockDInfo.strItemNo = info.strItemNo;
								stockDInfo.nReasonCode = 0;
								stockDInfo.dActualQty = info.dLoadAdj;
								stockDInfo.dBeginQty = info.dLoaded;
								stockDInfo.dSuggestQty = 0;
								dbAdapter.insertUStockDInfoList(stockDInfo);
							}
						}
						else
						{
							if( info.nAdjQty != 0 )
							{
								UStockDInfo stockDInfo = new UStockDInfo();
								stockDInfo.nTrxnNo = Global.gUInvenHinfo.nTrxnNo;
								stockDInfo.strItemNo = info.strItemNo;
								stockDInfo.nReasonCode = 0;
								stockDInfo.nActualQty = info.nLoadAdj;
								stockDInfo.nBeginQty = info.nLoaded;
								stockDInfo.nSuggestQty = 0;
								dbAdapter.insertUStockDInfoList(stockDInfo);
							}
						}

					}
				}
			}

			dbAdapter.close();
		}
	}
	private void doComplete() {

		new Thread(){
			@Override
			public void run() {
				new Handler(Looper.getMainLooper()).post(new Runnable() {
					@Override
					public void run() {
						try
						{
							showProgressDialog(Str.CreatingPdf[Global.LANG]);
						}catch (Exception e)
						{
							e.printStackTrace();
						}

					}
				});

				String strTransactionType = "L";
				String strRoute = "";
				String strDescription = "";
				DbAdapter dbAdapter = new DbAdapter(mContext);
				dbAdapter.open();
				List<LoadHeadInfo> headInfoList = dbAdapter.getSelectedLoadHeadInfo();
				for(LoadHeadInfo info : headInfoList)
				{
					if( info.strType.equals("L") || info.strType.equals("T") )
					{
						strDescription = info.strDescription;
						strTransactionType = info.strType;
						strRoute = info.strUser1;
						break;
					}
				}
				dbAdapter.close();

				boolean bTemplateExist = false;
				if(strTransactionType.equals("T"))
				{
					String strTemplateFileName = Utils.getFinalPrintTemplateFileName(mContext, Global.PRINT_LOAD_ADJUSTMENT_TEMPLATE_FILENAME);
					bTemplateExist = Utils.isFileExist(Global.ROOT_PRINT_TEMPLATE_LOCCATION + "/" + strTemplateFileName);

					String strHoneywellFileName = Utils.getFinalPrintTemplateFileNameForHoneywell(mContext, Global.PRINT_LOAD_SHEET_TEMPLATE_FILENAME);
					mHoneywellTemplatePath = Global.ROOT_PRINT_TEMPLATE_LOCCATION + "/" + strHoneywellFileName;
				}
				else {
					String strTemplateFileName = Utils.getFinalPrintTemplateFileName(mContext, Global.PRINT_LOAD_SHEET_TEMPLATE_FILENAME);
					bTemplateExist = Utils.isFileExist(Global.ROOT_PRINT_TEMPLATE_LOCCATION + "/" + strTemplateFileName);

					String strHoneywellFileName = Utils.getFinalPrintTemplateFileNameForHoneywell(mContext, Global.PRINT_LOAD_SHEET_TEMPLATE_FILENAME);
					mHoneywellTemplatePath = Global.ROOT_PRINT_TEMPLATE_LOCCATION + "/" + strHoneywellFileName;
				}

				if(bTemplateExist)
				{
					PrintEngine printEngine = new PrintEngine();
					PrintEngine printLocalEngine = new PrintEngine();
					mHoneywellPrintEngine = printLocalEngine;
					mPdfTrxnNo = System.currentTimeMillis();
					Utils.createDirectory(Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/");
					if(TextUtils.isEmpty(mStrPdfFileName))
					{
						mStrPdfFileName = getPDFFileName();
					}

					if( Global.gUInvenHinfo != null )
					{
						dbAdapter = new DbAdapter(mContext);
						dbAdapter.open();
						Utils.gDetlMap.clear();
						if(Global.gUInvenHinfo.truckList != null)
						{
							for(UInvenDInfo info : Global.gUInvenHinfo.truckList)
							{
								ProdDetlInfo prodDetlInfo = dbAdapter.getProdDetl(info.strItemNo);
								if(prodDetlInfo != null)
								{
									Utils.gDetlMap.put(prodDetlInfo.strItemNo, Utils.getPropertyMap(prodDetlInfo));
								}
							}
						}

						if(Global.gUInvenHinfo.containerList != null)
						{
							for(UInvenDInfo info : Global.gUInvenHinfo.containerList)
							{
								ProdDetlInfo prodDetlInfo = dbAdapter.getProdDetl(info.strItemNo);
								if(prodDetlInfo != null)
								{
									Utils.gDetlMap.put(prodDetlInfo.strItemNo, Utils.getPropertyMap(prodDetlInfo));
								}
							}
						}

						dbAdapter.close();

						if(strTransactionType.equals("T"))
						{
							printEngine.PreparePrint(mContext, Global.gUInvenHinfo.truckList, Global.gUInvenHinfo.containerList, strDescription, mTrxnDate,
									Global.gUInvenHinfo.strSignatureFilePath, mStrPdfFileName.substring(0, mStrPdfFileName.length() - 4), "ADJUST INVENTORY – IN", strRoute);
							printLocalEngine.PreparePrint(mContext, Global.gUInvenHinfo.truckList, Global.gUInvenHinfo.containerList, strDescription, mTrxnDate,
									Global.gUInvenHinfo.strSignatureFilePath, mStrPdfFileName.substring(0, mStrPdfFileName.length() - 4), "ADJUST INVENTORY – IN", strRoute);
						}
						else {
							List<UInvenDInfo> printList = new ArrayList<>();

							printList.addAll(Global.gUInvenHinfo.truckList);

							if (Utils.getStringSetting(mContext, Global.PREF_LOAD_SHEET_INCLUDE_RLOAD).equals("1")) {
								List<UInvenDInfo> rLoadList = dbAdapter.getUInvenDList(Utils.getLongSetting(mContext, Global.PREF_UINVEN_TRXN_NO));
								for (UInvenDInfo rLoadInfo: rLoadList) {
									boolean bAlreadyAdded = false;
									for (UInvenDInfo truckInfo: Global.gUInvenHinfo.truckList) {
										if (rLoadInfo.strItemNo.equals(truckInfo.strItemNo)) {
											truckInfo.dLoadTruck = rLoadInfo.dStock;
											truckInfo.nLoadTruck = rLoadInfo.nStock;
											//dbAdapter.updateUInvenDInfo(truckInfo);
											bAlreadyAdded = true;
											break;
										}
									}
									if (!bAlreadyAdded) {
										rLoadInfo.dLoadTruck = rLoadInfo.dStock;
										rLoadInfo.nLoadTruck = rLoadInfo.nStock;
										rLoadInfo.dStock = 0;
										rLoadInfo.nStock = 0;
										printList.add(rLoadInfo);
									}
								}
							}

							printEngine.PreparePrint(mContext, printList, Global.gUInvenHinfo.containerList, "Load Sheet",
									mTrxnDate, mPdfTrxnNo, Global.gUInvenHinfo.strSignatureFilePath, mStrPdfFileName.substring(0, mStrPdfFileName.length() - 4), null,
									strDescription, Global.gLoadAdjustmentPhotoList);
							printLocalEngine.PreparePrint(mContext, printList, Global.gUInvenHinfo.containerList, "Load Sheet",
									mTrxnDate, mPdfTrxnNo, Global.gUInvenHinfo.strSignatureFilePath, mStrPdfFileName.substring(0, mStrPdfFileName.length() - 4), null,
									strDescription, null);
						}

					}
					else
					{
						if(strTransactionType.equals("T"))
						{
							printEngine.PreparePrint(mContext, Global.gUInvenHinfo.truckList, Global.gUInvenHinfo.containerList, strDescription, mTrxnDate,
									Global.gUInvenHinfo.strSignatureFilePath, mStrPdfFileName.substring(0, mStrPdfFileName.length() - 4), "ADJUST INVENTORY – IN", strRoute);
							printLocalEngine.PreparePrint(mContext, Global.gUInvenHinfo.truckList, Global.gUInvenHinfo.containerList, strDescription, mTrxnDate,
									Global.gUInvenHinfo.strSignatureFilePath, mStrPdfFileName.substring(0, mStrPdfFileName.length() - 4), "ADJUST INVENTORY – IN", strRoute);
						}
						else {
							Utils.gDetlMap.clear();
							printEngine.PreparePrint(mContext, new ArrayList<UInvenDInfo>(), new ArrayList<UInvenDInfo>(), "Load Sheet",
									mTrxnDate, mPdfTrxnNo, "", mStrPdfFileName.substring(0, mStrPdfFileName.length() - 4), null,
									strDescription, Global.gLoadAdjustmentPhotoList);
							printLocalEngine.PreparePrint(mContext, new ArrayList<UInvenDInfo>(), new ArrayList<UInvenDInfo>(), "Load Sheet",
									mTrxnDate, mPdfTrxnNo, "", mStrPdfFileName.substring(0, mStrPdfFileName.length() - 4), null,
									strDescription, null);
						}

					}
					Utils.createDirectory(Global.PDF_LOCAL_DIR);
					if(strTransactionType.equals("T"))
					{
						printLocalEngine.createDocument(mContext, false, Global.PDF_LOCAL_DIR + mStrPdfFileName, Global.LOAD_ADJUSTMENT_PRINT);
						printEngine.createDocument(mContext, false, Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + mStrPdfFileName, Global.LOAD_ADJUSTMENT_PRINT);
					}
					else {
						printLocalEngine.createDocument(mContext, false, Global.PDF_LOCAL_DIR + mStrPdfFileName, Global.LOAD_SHEET_PRINT);
						printEngine.createDocument(mContext, false, Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + mStrPdfFileName, Global.LOAD_SHEET_PRINT);
					}

				}
				else
				{
					createPDF();
				}

				long transactionNo = 0;
				initMakeZipInfo();
				dbAdapter = new DbAdapter(mContext);
				dbAdapter.open();
				headInfoList = dbAdapter.getSelectedLoadHeadInfo();
				dbAdapter.close();
				String strDocNo = mStrPdfFileName.substring(0, mStrPdfFileName.length() - 4);
				if( headInfoList != null && headInfoList.size() > 0)
				{
					//Get L Load
					LoadHeadInfo selHeadInfo = null;
					for(LoadHeadInfo loadHeadInfo : headInfoList)
					{
						if(loadHeadInfo.strType.equals("L") || loadHeadInfo.strType.equals("T"))
						{
							selHeadInfo = loadHeadInfo;
							break;
						}
					}
					if(selHeadInfo != null)
					{
						transactionNo = setGeneralUploadData("LOAD", 0, 0, -1, selHeadInfo.strDetailFile, strDocNo);
					}
					else
					{
						transactionNo = setGeneralUploadData("LOAD", 0, 0, -1, "", strDocNo);
					}

					TransactionInfo transactionInfo = new TransactionInfo();
					transactionInfo.strType = "LOAD";
					transactionInfo.strTime = new SimpleDateFormat("kk:mm").format(new Date(transactionNo));
					transactionInfo.strDocNo = strDocNo;
					dbAdapter = new DbAdapter(mContext);
					dbAdapter.open();

					//Register Print document list.
					PrintItem printItem = new PrintItem();
					printItem.nTrxnNo = transactionNo;
					printItem.strDocNo = strDocNo;
					printItem.strDocType = "LOAD";
					printItem.strFilePath =  Global.PDF_LOCAL_DIR + mStrPdfFileName;
					printItem.nCustNo = 0;
					printItem.nChainNo = 0;
					printItem.strCustomerName = "";
					dbAdapter.insertPrint(printItem);

					dbAdapter.insertTransactionHistoryInfo(transactionInfo);
					dbAdapter.close();

				}
				else
				{

					transactionNo = setGeneralUploadData("LOAD", 0, 0, -1, "", strDocNo);

					TransactionInfo transactionInfo = new TransactionInfo();
					transactionInfo.strType = "LOAD";
					transactionInfo.strTime = new SimpleDateFormat("kk:mm").format(new Date(transactionNo));
					transactionInfo.strDocNo = strDocNo;
					dbAdapter = new DbAdapter(mContext);
					dbAdapter.open();

					//Register Print document list.
					PrintItem printItem = new PrintItem();
					printItem.nTrxnNo = transactionNo;
					printItem.strDocNo = strDocNo;
					printItem.strDocType = "LOAD";
					printItem.strFilePath = Global.PDF_LOCAL_DIR + mStrPdfFileName;
					printItem.nCustNo = 0;
					printItem.nChainNo = 0;
					printItem.strCustomerName = "";
					dbAdapter.insertPrint(printItem);

					dbAdapter.insertTransactionHistoryInfo(transactionInfo);
					dbAdapter.close();
				}
				for(int i = 1; i < headInfoList.size(); i++)
				{
					setUTransactionsInfo(0, 0, transactionNo, "LOAD", headInfoList.get(i).strDetailFile);
				}
				UTransactionsInfo uTransactionsInfo = setUTransactionsInfo(0, 0, transactionNo, "FARC", mStrPdfFileName);
				setUFileTransactionInfo(uTransactionsInfo.nTrxnNo, 0, 0, mPdfTrxnNo, strDocNo, "LOADSHEET", "LOAD SHEET", uTransactionsInfo.strTrxnDate, uTransactionsInfo.strTrxnTime, mStrPdfFileName);
				doMakeUInventory(transactionNo);
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
//							String strPrintText = HoneywellLinePrinter.doTestPrintDotMatric(mHoneywellTemplatePath, mHoneywellPrintEngine);
//							try
//							{
//								//Convert PDF to Base 64 image.
//								File file = new File(Global.PDF_LOCAL_DIR + mStrPdfFileName);
//								ParcelFileDescriptor fileDesc = ParcelFileDescriptor.open(file, 268435456);
//								PdfRenderer renderer = new PdfRenderer(fileDesc);
//								int numPgs = renderer.getPageCount();
//
//								for(int i = 0; i < numPgs; ++i) {
//									PdfRenderer.Page page = renderer.openPage(i);
//									int width = page.getWidth();
//									int height = page.getHeight();
//									float scale = 1.0f;
//									float scaledWidth = (float)width * scale;
//									float scaledHeight = (float)height * scale;
//									Bitmap pdfImage = Bitmap.createBitmap((int)scaledWidth, (int)scaledHeight, Bitmap.Config.ARGB_8888);
//									pdfImage.eraseColor(Color.WHITE);
//									page.render(pdfImage, (Rect)null, (Matrix)null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT);
////                            this.writeImage(pdfImage, printHeadWidth);
//									FileOutputStream fos = new FileOutputStream(new File(Global.PDF_DIR + mStrPdfFileName + "_i.jpg"));
//									ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//									pdfImage.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
//									pdfImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
//									byte[] byteArray = byteArrayOutputStream .toByteArray();
//									String base64LogoPng = android.util.Base64.encodeToString(byteArray, Base64.DEFAULT);
//									page.close();
//									fos.close();
//								}
//							}catch (Exception e)
//							{
//								e.printStackTrace();
//							}
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

		/*if(TextUtils.isEmpty(mStrMacAddress))
		{
			mStrMacAddress = "84:25:3F:28:5E:1F";
		}*/

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

		/*if(TextUtils.isEmpty(mStrMacAddress))
		{
			mStrMacAddress = "84:25:3F:28:5E:1F";
		}*/

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
					DbAdapter dbAdapter = new DbAdapter(mContext);
					dbAdapter.open();
					List<UStockHInfo> uStockHInfos = dbAdapter.getUStockHInfoList();
					for(UStockHInfo info : uStockHInfos)
					{
						info.printedFlag = 1;
						dbAdapter.updateUStockHInfo(info);
					}
					dbAdapter.close();
					m_bCompletedClicked = false;
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
		}, mStrMacAddress, Global.PDF_LOCAL_DIR + mStrPdfFileName);
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
						DbAdapter dbAdapter = new DbAdapter(mContext);
						dbAdapter.open();
						List<UStockHInfo> uStockHInfos = dbAdapter.getUStockHInfoList();
						for(UStockHInfo info : uStockHInfos)
						{
							info.printedFlag = 1;
							dbAdapter.updateUStockHInfo(info);
						}
						dbAdapter.close();
						m_bCompletedClicked = false;
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
			m_bCompletedClicked = false;
			goBack();
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
					DbAdapter dbAdapter = new DbAdapter(mContext);
					dbAdapter.open();
					List<UStockHInfo> uStockHInfos = dbAdapter.getUStockHInfoList();
					for(UStockHInfo info : uStockHInfos)
					{
						info.printedFlag = 1;
						dbAdapter.updateUStockHInfo(info);
					}
					dbAdapter.close();
					m_bCompletedClicked = false;
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
		}, mStrMacAddress, mStrFriendlyName, Global.PDF_LOCAL_DIR + mStrPdfFileName);
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
		}).start();

		//After 30 seconds, it will be called for discovery finished
		//new Handler(Looper.getMainLooper()).postDelayed(() -> HoneywellPrinter.cancelDiscoveryPrinters(), 30 * 1000);
	}

	private void discoveryHoneywellLinePrinters()
	{
//		try
//		{
//			AssetManager assetManager = mContext.getAssets();
//			InputStream input = assetManager.open("printer_profiles.JSON");
//			int initialBufferSize = 8000;
//			ByteArrayOutputStream output = new ByteArrayOutputStream(initialBufferSize);
//
//			byte[] buf = new byte[1024];
//			int len;
//			while ((len = input.read(buf)) > 0)
//			{
//				output.write(buf, 0, len);
//			}
//			input.close();
//
//			output.flush();
//			output.close();
//			String jsonCmdAttribStr = output.toString();
//			Log.e("JSON prop", jsonCmdAttribStr);
//		}catch (Exception e)
//		{
//			e.printStackTrace();
//		}
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
		}).start();

		//After 30 seconds, it will be called for discovery finished
		//new Handler(Looper.getMainLooper()).postDelayed(() -> HoneywellPrinter.cancelDiscoveryPrinters(), 30 * 1000);
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
		}).start();

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
							else if(Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 4)
							{
								discoveryBixolonPrinters();
							}
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
						else if (Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 4)
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
		if(bProcessLastProcess) return;

		if( Utils.getLongSetting(mContext, Global.PREF_UINVEN_TRXN_NO) == 0 )
		{
			if(Global.gUInvenHinfo == null)
			{
				Date curDate = new Date();
				Global.gUInvenHinfo = new UInvenHInfo();
				Global.gUInvenHinfo.nTrxnNo = System.currentTimeMillis();
				Global.gUInvenHinfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(mContext, curDate));
				Global.gUInvenHinfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(curDate);
			}

			Utils.setLongSetting(mContext, Global.PREF_UINVEN_TRXN_NO, Global.gUInvenHinfo.nTrxnNo);
		}
		Utils.setBooleanSetting(mContext, Global.PREF_PASSED_LOAD_SECTION, true);

		Global.gUInvenHinfo = null;
		if(Global.gLocationFilterList != null )
			Global.gLocationFilterList.clear();

		if (Utils.getIntSetting(mContext, Global.PREF_LOAD_CUSTOMER) == 1 ||
				Utils.getIntSetting(mContext, Global.PREF_LOAD_CUSTOMER) == 2
				|| Utils.getIntSetting(mContext, Global.PREF_LOAD_CUSTOMER) == 0)
		{
			// Delete customer with orderQty = 0
			DbAdapter dbAdapter = new DbAdapter(mContext);
			dbAdapter.open();
			//List<CustomerInfo> customerInfoList = dbAdapter.getDeferredCustomerList(true);
			int nSelDayNo = Utils.getIntSetting(mContext, Global.DAY_NO);
			List<CustomerInfo> customerInfoList = dbAdapter.getCustomerList(true, true, nSelDayNo);
			customerInfoList.addAll(filterSameCustomer(dbAdapter.getWhiteCustomerInfo(true, nSelDayNo)));

			UOrderStatusHInfo uOrderStatusInfo = null;
			if(customerInfoList.size() > 0 )
			{
				uOrderStatusInfo = new UOrderStatusHInfo();
				uOrderStatusInfo.nTrxnNo = m_lTrxnNo;
				uOrderStatusInfo.strDocNo = mStrPdfFileName.substring(0, mStrPdfFileName.length() - 4);
				uOrderStatusInfo.strDocType = "OSTA";
				uOrderStatusInfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(mContext, new Date(m_lTrxnNo)));
				uOrderStatusInfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(m_lTrxnNo);
			}
			//int nSelDayNo = Utils.getIntSetting(mContext, Global.DAY_NO);
			int nLoadCust = Utils.getIntSetting(mContext, Global.PREF_LOAD_CUSTOMER);

			for(CustomerInfo customerInfo : customerInfoList)
			{
				if(Utils.getIntSetting(mContext, Global.PREF_LOAD_CUSTOMER) == 1)
				{
					customerInfo.nDeferredStatus = 1;
				}
				else if( customerInfo.nCompleteStatus == 0 )
				{
					if(Utils.getIntSetting(mContext, Global.PREF_LOADDEFER) == 1)
					{
						customerInfo.nDeferredStatus = 3;
					}
					else
					{
						customerInfo.nDeferredStatus = 2;
					}
				}
				else if( customerInfo.nCompleteStatus == 2 )
				{
					customerInfo.nDeferredStatus = 1;
				}
				else if(customerInfo.nCompleteStatus == 1)
				{
					customerInfo.nDeferredStatus = 1;
				}
				else if( customerInfo.nCompleteStatus == 3)
				{
					customerInfo.nDeferredStatus = 2;
				}

				dbAdapter.updateCustomerInfo(customerInfo);
				if(customerInfo.presoldOrHInfo == null || customerInfo.presoldOrHInfo.size() == 0)
				{
					customerInfo.presoldOrHInfo = dbAdapter.getPresoldOrHInfo(customerInfo.nChainNo, customerInfo.nCustNo, customerInfo.nPeriodNo);
				}

				if( m_lTrxnNo != -1 && (nLoadCust == 1 || nLoadCust == 0))
				{
					if(customerInfo.presoldOrHInfo == null || customerInfo.presoldOrHInfo.size() == 0)
					{
						customerInfo.presoldOrHInfo = dbAdapter.getPresoldOrHInfo(customerInfo.nChainNo, customerInfo.nCustNo, customerInfo.nPeriodNo);
					}
					if( customerInfo.presoldOrHInfo != null && customerInfo.presoldOrHInfo.size() > 0 )
					{
						for(PresoldOrHInfo presoldOrHInfo : customerInfo.presoldOrHInfo)
						{
							/*if(nLoadCust == 0 && nSelDayNo != presoldOrHInfo.nDayNo)
								continue;*/

							UOrderStatusDInfo dInfo = new UOrderStatusDInfo();
							dInfo.nChainNo = customerInfo.nChainNo;
							dInfo.nCustNo = customerInfo.nCustNo;
							dInfo.nDayNo = presoldOrHInfo.nDayNo;
							if(Utils.getIntSetting(mContext, Global.PREF_LOAD_CUSTOMER) == 0 && customerInfo.nDeferredStatus == 3)
							{
								dInfo.nStatus = 1;
							}
							else
							{
								dInfo.nStatus = customerInfo.nDeferredStatus;
							}
							//dInfo.strDeliveryDate = customerInfo.strDeliveryDate;
							dInfo.strDeliveryDate = uOrderStatusInfo.strTrxnDate;
							dInfo.strOrderNo = presoldOrHInfo.strOrderNo;
							dInfo.nDeliverySequence = presoldOrHInfo.nDeliverySequence;
							dInfo.strOrderBarcode = presoldOrHInfo.strOrderBarCode;
							dInfo.strPORef = presoldOrHInfo.strPORef;
							dInfo.nTrxnNo = m_lTrxnNo;
							uOrderStatusInfo.uOrderStatusDInfoList.add(dInfo);
						}
					}
					else
					{	// VS154 from Andrew
						UOrderStatusDInfo dInfo = new UOrderStatusDInfo();
						dInfo.nChainNo = customerInfo.nChainNo;
						dInfo.nCustNo = customerInfo.nCustNo;
						dInfo.nDayNo = customerInfo.nDayNo;
						if(Utils.getIntSetting(mContext, Global.PREF_LOAD_CUSTOMER) == 0 && customerInfo.nDeferredStatus == 3)
						{
							dInfo.nStatus = 1;
						}
						else
						{
							dInfo.nStatus = customerInfo.nDeferredStatus;
						}
						//dInfo.strDeliveryDate = customerInfo.strDeliveryDate;
						dInfo.strDeliveryDate = uOrderStatusInfo.strTrxnDate;
						dInfo.strOrderNo = "";
						dInfo.nDeliverySequence = customerInfo.nSeqNo;
						dInfo.strOrderBarcode = "";
						dInfo.strPORef = "";
						dInfo.nTrxnNo = m_lTrxnNo;
						uOrderStatusInfo.uOrderStatusDInfoList.add(dInfo);
					}
				}

				if(customerInfo.nCompleteStatus != 2) continue;
				if(customerInfo.presoldOrHInfo != null && customerInfo.presoldOrHInfo.size() > 0 )
				{
					List<PresoldOrDInfo> loadDetlList = dbAdapter.getPresoldOrDList(customerInfo.presoldOrHInfo.get(0).strDetailFile);
					for(PresoldOrDInfo info : loadDetlList )
					{
						if( info.nOrderQty == 0 )
						{
							dbAdapter.deletePresoldOrDInfo(info);
						}
					}
				}
			}
			if( uOrderStatusInfo != null)
			{
				dbAdapter.insertUOrderStatusInfo(uOrderStatusInfo);
			}
			dbAdapter.close();
		}

		//Global.gUploadCustNoList.add(makeZipFileInfo);
		Utils.registerUploadFile(mContext, makeZipFileInfo);
		{
			DbAdapter dbAdapter = new DbAdapter(mContext);
			dbAdapter.open();
			dbAdapter.updateLLoadStatusAsLoadCompleted();
			dbAdapter.updateRLoadStatusAsLoadCompleted();
			dbAdapter.close();
		}
		Global.bVisitAutoSelectCustomer = true;
		bProcessLastProcess = true;
	}

	private List<CustomerInfo> filterSameCustomer(List<CustomerInfo> customerInfoList)
	{
		Map<String, CustomerInfo> customerInfoMap = new HashMap<>();
		List<CustomerInfo> newList = new ArrayList<>();
		for(CustomerInfo customerInfo : customerInfoList)
		{
			String key = customerInfo.nChainNo + "___" + customerInfo.nCustNo;
			if(customerInfoMap.get(key) == null)
			{
				customerInfoMap.put(key, customerInfo);
				newList.add(customerInfo);
			}
		}
		return newList;
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
			mContext.replaceFragment(new PreTripTasksFragment());
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
									doLastProcess();
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
									doLastProcess();
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
//						String strPrintText = HoneywellLinePrinter.doTestPrintDotMatric(mHoneywellTemplatePath, mHoneywellPrintEngine);
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
									doLastProcess();
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
									doLastProcess();
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
						doLastProcess();
						goBack();
						m_bCompletedClicked = false;
					}

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
							printer.sendFileContents(Global.PDF_LOCAL_DIR + mStrPdfFileName);
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
					DbAdapter dbAdapter = new DbAdapter(mContext);
					dbAdapter.open();
					List<UStockHInfo> uStockHInfos = dbAdapter.getUStockHInfoList();
					for(UStockHInfo info : uStockHInfos)
					{
						info.printedFlag = 1;
						dbAdapter.updateUStockHInfo(info);
					}
					dbAdapter.close();
					m_bCompletedClicked = false;
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