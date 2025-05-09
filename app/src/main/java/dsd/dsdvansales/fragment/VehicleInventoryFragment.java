package dsd.dsdvansales.fragment;

import android.app.*;
import android.bluetooth.*;
import android.content.*;
import android.content.DialogInterface.*;
import android.os.*;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.zebra.sdk.comm.*;
import com.zebra.sdk.printer.*;
import com.zebra.sdk.printer.discovery.*;

import java.io.*;
import java.text.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import dsd.dsdvansales.R;
import dsd.dsdvansales.db.DbAdapter;
import dsd.dsdvansales.dialogs.AdjustInventoryDialog;
import dsd.dsdvansales.dialogs.InputDialog;
import dsd.dsdvansales.dialogs.MessageDialog;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.HoneywellLinePrinter;
import dsd.dsdvansales.global.HoneywellPrinter;
import dsd.dsdvansales.global.OTPUtils;
import dsd.dsdvansales.global.PrintEngine;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.model.CustomerOrderInfo;
import dsd.dsdvansales.model.OrderLineInfo;
import dsd.dsdvansales.model.PresoldOrDInfo;
import dsd.dsdvansales.model.ProdDetlInfo;
import dsd.dsdvansales.model.ProdInfo;
import dsd.dsdvansales.model.UInvenDInfo;
import dsd.dsdvansales.model.UInvenHInfo;
import dsd.dsdvansales.model.UTransactionsInfo;
import dsd.dsdvansales.views.AlertSingleChoiceAdapter;

public class VehicleInventoryFragment extends BaseFragment implements View.OnClickListener {

	private Font	fontBlack7Bold;
	private Font	fontBlack10Bold;
	private Font	fontBlack7Normal;

	private TextView mTxtNoData = null;
	private ListView mListView = null;
	private ListAdapter mListAdapter = null;
	private List<UInvenDInfo> mUInvenDList = new ArrayList<>();
	private int mType = Global.INVENTORY_FRESH;

	private long mPdfTrxnNo = 0;
	private String mStrPdfFileName = "";
	private Connection printerConnection = null;
	private String mStrMacAddress = "";
	private boolean mIsCreatedPDF = false;

	private int m_nInventoryValidationType = 0;
	private boolean bShowCase = true;
	private List<CustomerOrderInfo> latestOrderInfo = new ArrayList<>();

	private List<ProdDetlInfo> mProdDetlList = new ArrayList<>();

	private Map<String, ProdDetlInfo> mProdUPCDetMap = new HashMap<>();
	private Map<String, ProdDetlInfo> mProdDetlItemMap = new HashMap<>();
	private Map<String, ProdInfo> mProdInfoMap = new HashMap<>();

	private PrintEngine mHoneywellPrintEngine = null;
	private String mHoneywellTemplatePath = "";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootView = inflater.inflate(R.layout.fragment_vehicle_inventory, container, false);
		commonInit();
		loadFont();
		return mRootView;
	}

	private void loadFont()
	{
		fontBlack7Bold	= Utils.getFont(Utils.getPrintFontFilePath(), Utils.getPrintEncoding(), Utils.getPrintEmbeded(), 7, Font.BOLD, BaseColor.BLACK);
		fontBlack10Bold	= Utils.getFont(Utils.getPrintFontFilePath(), Utils.getPrintEncoding(), Utils.getPrintEmbeded(), 10, Font.BOLD, BaseColor.BLACK);
		fontBlack7Normal= Utils.getFont(Utils.getPrintFontFilePath(), Utils.getPrintEncoding(), Utils.getPrintEmbeded(), 7, Font.NORMAL, BaseColor.BLACK);
	}

	@Override
	public void onResume() {
		super.onResume();
		Global.gFragIdx = Global.FRAG_VEHICLE_INVENTORY;
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
		bShowCase = Utils.getStringSetting(mContext, Global.PREF_INVENTORYUOM).equals("U") == false;
		mRootView.findViewById(R.id.txtFresh).setOnClickListener(this);
		mRootView.findViewById(R.id.txtReturns).setOnClickListener(this);
		mRootView.findViewById(R.id.txtContainers).setOnClickListener(this);
		mRootView.findViewById(R.id.btnBack).setOnClickListener(this);
		mRootView.findViewById(R.id.btnNote).setOnClickListener(this);
		mRootView.findViewById(R.id.btnAdjust).setOnClickListener(this);
		mTxtNoData = (TextView) mRootView.findViewById(R.id.txtNoData);
		mListView = (ListView) mRootView.findViewById(R.id.listView);

		if( Utils.getIntSetting(mContext, Global.PREF_LOADSTKTAKE) != 0)
		{
			mRootView.findViewById(R.id.btnTake).setVisibility(View.VISIBLE);
			mRootView.findViewById(R.id.btnTake).setOnClickListener(this);
		}
		else
		{
			mRootView.findViewById(R.id.btnTake).setVisibility(View.INVISIBLE);
		}
		if( Utils.getIntSetting(mContext, Global.PREF_LOADREQUEST) == 1 && !Global.g_bVisitTripSettlement)
		{
			mRootView.findViewById(R.id.btnRequest).setVisibility(View.VISIBLE);
			mRootView.findViewById(R.id.btnRequest).setOnClickListener(this);
		}
		else
		{
			mRootView.findViewById(R.id.btnRequest).setVisibility(View.INVISIBLE);
		}

		if(Utils.getIntSetting(mContext, Global.PREF_LOAD_CONTAINERS) == 0)
		{
			mRootView.findViewById(R.id.txtContainers).setVisibility(View.INVISIBLE);
		}
		else
		{
			mRootView.findViewById(R.id.txtContainers).setVisibility(View.VISIBLE);
		}

		mListAdapter = new ListAdapter();
		mListView.setAdapter(mListAdapter);

		mRootView.findViewById(R.id.btnAdjust).setEnabled(Utils.getIntSetting(mContext, Global.PREF_ADJUST_ALLOW) == 1);

		loadData();

		refreshLanguage();
		setType(Global.INVENTORY_FRESH);
	}

	@Override
	public void loadData() {
		m_nInventoryValidationType = Utils.getIntSetting(mContext, Global.PREF_INVENCKECK);
		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		latestOrderInfo.clear();
		mProdDetlList = dbAdapter.getProdDetlList(mProdUPCDetMap);
		dbAdapter.getProdDetlList(mProdDetlItemMap, null);
//		if(Utils.getStringSetting(mContext, Global.PREF_ITEM_CODE_DISPLAY_SOURCE).equals("PRODINFO"))
//		{
//			mProdInfoMap = dbAdapter.getProdInfoMap("ItemCode");
//		}
		latestOrderInfo.addAll(dbAdapter.getAllCustomerLatestOrderList());
		Global.gAdjustinfo = dbAdapter.getUInvenHInfo(Utils.getLongSetting(mContext, Global.PREF_UINVEN_TRXN_NO));
		if (Global.gAdjustinfo == null) {
			Global.gAdjustinfo = new UInvenHInfo();
			Global.gAdjustinfo.nTrxnNo = Utils.getLongSetting(mContext, Global.PREF_UINVEN_TRXN_NO);
		}

		List<UInvenDInfo> uInvenDList = dbAdapter.getUInvenDList(Global.gAdjustinfo.nTrxnNo);
		mUInvenDList.clear();


		Map<String, Float> weightMap = new HashMap<>();
		List<String> uomList = new ArrayList<>();
		for (UInvenDInfo uInvenDInfo : uInvenDList) {
			if( uInvenDInfo.bShow )
			{
				ProdDetlInfo prodDetlInfo = mProdUPCDetMap.get(uInvenDInfo.strItemNo);
				if (prodDetlInfo == null) {
					continue;
				}
				String strUOM = prodDetlInfo.prodLocnInfo.strCaseUOM.length() > 0 ? prodDetlInfo.prodLocnInfo.strCaseUOM : prodDetlInfo.prodLocnInfo.strUnitUOM;
				if(TextUtils.isEmpty(strUOM)) strUOM = "KG";
				if(weightMap.get(strUOM) == null)
				{
					weightMap.put(strUOM, 0f);
					uomList.add(strUOM);
				}
				float fTotalWeight = weightMap.get(strUOM);
				if(Utils.getProdLoconSCWType(mContext, uInvenDInfo.strItemNo) != Global.WEIGHT_ITEM) {
					fTotalWeight += uInvenDInfo.nStock * prodDetlInfo.nWeight;
				}
				else {
					fTotalWeight += uInvenDInfo.dStock * prodDetlInfo.nWeight;
				}
				weightMap.put(strUOM, fTotalWeight);
				mUInvenDList.add(uInvenDInfo);
			}
		}

		String strTotalWeight = "";
		for(String uom : uomList)
		{
			float fTotalWeight = weightMap.get(uom);
			if(fTotalWeight == 0f) continue;

			fTotalWeight = Utils.parseFloatFromString(String.format(Locale.ENGLISH, "%.2f", fTotalWeight));
			if(TextUtils.isEmpty(strTotalWeight)) {
				strTotalWeight = NumberFormat.getInstance().format(fTotalWeight) + " " + uom;
			}
			else {
				strTotalWeight = strTotalWeight + ", " +NumberFormat.getInstance().format(fTotalWeight) + " " + uom;
			}
		}

		if(TextUtils.isEmpty(strTotalWeight))
		{
			strTotalWeight = "0 KG";
		}
		((TextView)mRootView.findViewById(R.id.txtTotalWeight)).setText(strTotalWeight);

		/*Collections.sort(mUInvenDList, new Comparator<UInvenDInfo>() {
			@Override
			public int compare(UInvenDInfo t1, UInvenDInfo t2) {
				int nSeq1 = getSequenceValue(t1.strItemNo);
				int nSeq2 = getSequenceValue(t2.strItemNo);
				if( nSeq1 == 0 && nSeq2 != 0 )
					return -1;
				else if( nSeq1 != 0 && nSeq2 == 0)
					return 1;
				else if( nSeq1 == 0 && nSeq2 == 0 )
				{
					return t1.strItemNo.compareTo(t2.strItemNo);
				}
				else
					return nSeq1 - nSeq2;
			}
		});*/
		dbAdapter.close();
	}

	@Override
	public void refreshPage() {
		int nLoadContainers = Utils.getIntSetting(mContext, Global.PREF_LOAD_CONTAINERS);
		List<UInvenDInfo> uInvenDList = new ArrayList<>();
		for (int i=0; i<mUInvenDList.size(); i++) {
			UInvenDInfo info = mUInvenDList.get(i);
			mUInvenDList.get(i).bShow = false;
			ProdDetlInfo prodDetlInfo = mProdDetlItemMap.get(info.strItemNo);
			if (mType == Global.INVENTORY_FRESH) {
				if (nLoadContainers == 2) {
					if ((prodDetlInfo != null && prodDetlInfo.prodLocnInfo.nSCWType == 1) || info.nSCWType == Global.WEIGHT_ITEM) {
						mUInvenDList.get(i).bShow = true;
						uInvenDList.add(info.copyInfo());
					}
				}
				else {
					if (info.nSCWType == Global.INVENTORY_TRUCK || info.nSCWType == Global.WEIGHT_ITEM) {
						mUInvenDList.get(i).bShow = true;
						uInvenDList.add(info.copyInfo());
					}
				}
			} else if (mType == Global.INVENTORY_RETURNS) {
				if (mUInvenDList.get(i).nDumps != 0) {
					mUInvenDList.get(i).bShow = true;
					uInvenDList.add(info.copyInfo());
				}
			} else if (mType == Global.INVENTORY_CONTAINERS) {
				if (nLoadContainers == 2) {
					if (prodDetlInfo != null && prodDetlInfo.prodLocnInfo.nSCWType == 2) {
						mUInvenDList.get(i).bShow = true;
						uInvenDList.add(info.copyInfo());
					}
				}
				else {
					if (info.nSCWType == Global.INVENTORY_CONTAINERS) {
						mUInvenDList.get(i).bShow = true;
						uInvenDList.add(info.copyInfo());
					}
				}
			}
		}

		if (uInvenDList.size() > 0) {
			mListView.setVisibility(View.VISIBLE);
			mTxtNoData.setVisibility(View.GONE);
			mListAdapter.setListData(uInvenDList);
			mListAdapter.notifyDataSetChanged();
		} else {
			mListView.setVisibility(View.GONE);
			mTxtNoData.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void refreshLanguage() {
		((TextView) mRootView.findViewById(R.id.lblTitle)).setText(Str.VehicleInventory[Global.LANG].toUpperCase());
		((TextView) mRootView.findViewById(R.id.txtTrip)).setText(Str.Trip[Global.LANG] + " # " + Global.gTripNumber);
		((TextView) mRootView.findViewById(R.id.txtFresh)).setText(Str.Fresh[Global.LANG]);
		((TextView) mRootView.findViewById(R.id.txtReturns)).setText(Str.Returns[Global.LANG]);
		((TextView) mRootView.findViewById(R.id.txtContainers)).setText(Str.Containers[Global.LANG]);
		((Button) mRootView.findViewById(R.id.btnAdjust)).setText(Str.Adjust[Global.LANG]);
		mTxtNoData.setText(Str.No_data_has_been_found[Global.LANG]);
	}

	@Override
	public void receiveBroadcast(String strAction, Intent intent) {
	}

	private void setType(int nType) {
		mType = nType;
		int nNormalColor = mContext.getResources().getColor(R.color.vehicle_inventory_normal_color);
		int nSelectedColor = mContext.getResources().getColor(R.color.vehicle_inventory_selected_color);
		mRootView.findViewById(R.id.txtFresh).setBackgroundColor(nNormalColor);
		mRootView.findViewById(R.id.txtReturns).setBackgroundColor(nNormalColor);
		mRootView.findViewById(R.id.txtContainers).setBackgroundColor(nNormalColor);
		if (mType == Global.INVENTORY_FRESH) {
			mRootView.findViewById(R.id.txtFresh).setBackgroundColor(nSelectedColor);
		} else if (mType == Global.INVENTORY_RETURNS) {
			mRootView.findViewById(R.id.txtReturns).setBackgroundColor(nSelectedColor);
		} else if (mType == Global.INVENTORY_CONTAINERS) {
			mRootView.findViewById(R.id.txtContainers).setBackgroundColor(nSelectedColor);
		}
		refreshPage();
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
						} else if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
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

			phrase = new Phrase("TRUCK INVENTORY", fontBlack10Bold);
			cell = new PdfPCell(phrase);
			cell.setPaddingTop(5);
			cell.setColspan(2);
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell.setBorder(Rectangle.NO_BORDER);
			table.addCell(cell);

			phrase = new Phrase(String.format(Locale.ENGLISH, "Date	   %s", new SimpleDateFormat(Utils.getDateFormat(mContext, "dd/MM/yyyy") + " hh:mm aa").format(new Date())), fontBlack7Normal);
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

			phrase = new Phrase("Saleable", fontBlack7Bold);
			cell = new PdfPCell(phrase);
			cell.setColspan(3);
			cell.setPaddingTop(10);
			cell.setBorder(Rectangle.NO_BORDER);
			table.addCell(cell);
			for (UInvenDInfo info : mUInvenDList) {
				if (info.nSCWType == Global.INVENTORY_TRUCK || info.nSCWType == Global.WEIGHT_ITEM) {
					if( info.nStock == 0 ) continue;
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
					cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
					cell.setBorder(Rectangle.NO_BORDER);
					table.addCell(cell);
				}
			}

			phrase = new Phrase("Returns", fontBlack7Bold);
			cell = new PdfPCell(phrase);
			cell.setColspan(3);
			cell.setPaddingTop(10);
			cell.setBorder(Rectangle.NO_BORDER);
			table.addCell(cell);
			for (UInvenDInfo info : mUInvenDList) {
				if( info.nDumps == 0 ) continue;
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
				phrase = new Phrase(String.format(Locale.ENGLISH, "%d", info.nDumps), fontBlack7Normal);
				cell = new PdfPCell(phrase);
				cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
				cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				cell.setBorder(Rectangle.NO_BORDER);
				table.addCell(cell);
			}

			phrase = new Phrase("Containers", fontBlack7Bold);
			cell = new PdfPCell(phrase);
			cell.setColspan(3);
			cell.setPaddingTop(10);
			cell.setBorder(Rectangle.NO_BORDER);
			table.addCell(cell);
			for (UInvenDInfo info : mUInvenDList) {
				if (info.nSCWType == Global.INVENTORY_CONTAINERS) {
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
					cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
					cell.setBorder(Rectangle.NO_BORDER);
					table.addCell(cell);
				}
			}
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
			int nHeight = 200 + 17 * mUInvenDList.size() * 2;
			Document document = new Document(new Rectangle(0, 0, 140, nHeight), 0, 0, 0, 0);
			PdfWriter.getInstance(document, new FileOutputStream(Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + mStrPdfFileName));
			document.open();
			PdfPTable table = createHeader();
			document.add(table);
			table = createBody();
			table.setSpacingBefore(5);
			document.add(table);
			document.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	boolean bPaused = true;
	boolean bNeedBack = false;
	private void goBack() {
		//Global.gUploadCustNoList.add(Global.gCurCustomerInfo.nCustNo);
		//Global.gUploadCustNoList.add(makeZipFileInfo);
		Utils.registerUploadFile(mContext, makeZipFileInfo);
		if(bPaused)
		{
			bNeedBack = true;
		}
		else
		{
			mContext.onBackPressed();
		}
	}

	public boolean m_bClicked = false;
	private int mSelectedTransferMethod = -1;
	@Override
	public void onClick(View v) {
		if( m_bClicked ) return;
		m_bClicked = true;
		switch (v.getId()) {
			case R.id.btnBack:
				mContext.onBackPressed();
				break;
			case R.id.btnNote:
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
							String strTemplateFileName = Utils.getFinalPrintTemplateFileName(mContext, Global.PRINT_TRUCK_INVENTORY_TEMPLATE_FILENAME);
							if(Utils.isFileExist(Global.ROOT_PRINT_TEMPLATE_LOCCATION + "/" + strTemplateFileName))
							{
								PrintEngine printEngine = new PrintEngine();
								mHoneywellPrintEngine = printEngine;
								Utils.createDirectory(Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/");
								mPdfTrxnNo = System.currentTimeMillis();
								mStrPdfFileName = getPDFFileName();
								DbAdapter dbAdapter = new DbAdapter(mContext);
								dbAdapter.open();
								List<UInvenDInfo> uInvenDInfoList = dbAdapter.getUInvenDList(Utils.getLongSetting(mContext, Global.PREF_UINVEN_TRXN_NO));
								dbAdapter.close();
								Collections.sort(uInvenDInfoList, new Comparator<UInvenDInfo>() {
									@Override
									public int compare(UInvenDInfo uInvenDInfo, UInvenDInfo t1) {
										return uInvenDInfo.strItemNo.compareTo(t1.strItemNo);
									}
								});

								dbAdapter = new DbAdapter(mContext);
								dbAdapter.open();
								Utils.gDetlMap.clear();
								if(uInvenDInfoList != null)
								{
									for(UInvenDInfo info : uInvenDInfoList)
									{
										ProdDetlInfo prodDetlInfo = dbAdapter.getProdDetl(info.strItemNo);
										if(prodDetlInfo != null)
										{
											Utils.gDetlMap.put(prodDetlInfo.strItemNo, Utils.getPropertyMap(prodDetlInfo));
										}
									}
								}
								dbAdapter.close();

								printEngine.PreparePrint(mContext, uInvenDInfoList, null, "Truck Inventory", new Date(), mPdfTrxnNo, null
										, mStrPdfFileName.substring(0, mStrPdfFileName.length() - 4), null, "", null);
								String strHoneywellFileName = Utils.getFinalPrintTemplateFileNameForHoneywell(mContext, Global.PRINT_TRUCK_INVENTORY_TEMPLATE_FILENAME);
								mHoneywellTemplatePath = Global.ROOT_PRINT_TEMPLATE_LOCCATION + "/" + strHoneywellFileName;
								printEngine.createDocument(mContext, false, Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + mStrPdfFileName, Global.TRUCK_INVENTORY_PRINT);
							}
							else
							{
								createPDF();
							}

							long nTrxnNo = System.currentTimeMillis();
							//UTransactionsInfo uTransactionsInfo = setUTransactionsInfo(Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo, nTrxnNo, "FARC", mStrPdfFileName);
							UTransactionsInfo uTransactionsInfo = setUTransactionsInfo(0, 0, nTrxnNo, "FARC", mStrPdfFileName);
							setUFileTransactionInfo(uTransactionsInfo.nTrxnNo, 0, 0, mPdfTrxnNo, mStrPdfFileName.substring(0, mStrPdfFileName.length() - 4), "TRUCKINVENTORY", "TRUCK INVENTORY", uTransactionsInfo.strTrxnDate, uTransactionsInfo.strTrxnTime, mStrPdfFileName);

							// Utils.registerUploadFile(mContext, makeZipFileInfo);

							//Register Print document list.
							/*PrintItem printItem = new PrintItem();
							printItem.nTrxnNo = nTrxnNo;
							printItem.strDocNo = mStrPdfFileName.substring(0, mStrPdfFileName.length() - 4);
							printItem.strDocType = "TRUCKINVEN";
							printItem.strFilePath = Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + mStrPdfFileName;
							printItem.nCustNo = 0;
							printItem.nChainNo = 0;
							printItem.strCustomerName = "";
							DbAdapter dbAdapter = new DbAdapter(mContext);
							dbAdapter.open();
							dbAdapter.insertPrint(printItem);
							dbAdapter.close();*/

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
				break;
			case R.id.btnAdjust:
				String strSecurity8Key = Utils.getStringSetting(mContext, Global.PREF_LOADREQUEST_APPROVAL_KEY);
				String strSecurity9Key = Utils.getStringSetting(mContext, Global.PREF_SECURITY9_KEY);
				if (!strSecurity8Key.isEmpty() || !strSecurity9Key.isEmpty()) {
					String strPasscode = "";
					if (!strSecurity8Key.isEmpty()) {
						String strMFASecret = Utils.getStringSetting(mContext, Global.PREF_MFA_SECRET);
						if (strMFASecret.length() > 0) {
							strPasscode = OTPUtils.generateTOTP(strMFASecret);
						}
						else {
							strPasscode = strSecurity8Key;
						}
						if (strPasscode == null || strPasscode.isEmpty()) {
							strPasscode = strSecurity8Key;
						}
					}
					else {
						strPasscode = strSecurity9Key;
					}

					try {
						final InputDialog inputDlg = new InputDialog(mContext);
						inputDlg.mStrTitle = Str.PIN_CODE[Global.LANG];
						inputDlg.mStrHint = Str.PIN_code_for_communication[Global.LANG];
						inputDlg.mStrLeft = Str.Enter[Global.LANG];
						inputDlg.mStrRight = Str.Return[Global.LANG];
						inputDlg.mInputType = InputType.TYPE_CLASS_NUMBER;
						inputDlg.setCancelable(false);
						String finalStrPasscode = strPasscode;
						inputDlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
							@Override
							public void onDismiss(DialogInterface dialog) {
								if (inputDlg.mReturnCode == Global.DLG_RETURN_LEFT) {
									if (finalStrPasscode.equals(inputDlg.mStrInputed)) {
										if (!strSecurity8Key.isEmpty()) {
											doAdjustProcess(true);
										}
										else {
											doAdjustProcess(false);
										}
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
				else {
					doAdjustProcess(false);
				}

				break;
			case R.id.btnRequest: {
					Global.gAdjustinfo.uInvenDList.clear();
					Global.gAdjustinfo.uInvenDList.addAll(mUInvenDList);
					LoadRequestFragment fragment = new LoadRequestFragment();
					Bundle args = new Bundle();
					args.putBoolean(Global.EXTRA_FROM, true);
					fragment.setArguments(args);
					mContext.replaceFragment(fragment);
				}
				break;
			case R.id.btnTake:
				String strSecurity9Key1 = Utils.getStringSetting(mContext, Global.PREF_SECURITY9_KEY);
				if (!strSecurity9Key1.isEmpty()) {
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
									if (strSecurity9Key1.equals(inputDlg.mStrInputed)) {
										doTakeProcess();
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
				else {
					doTakeProcess();
				}

				break;
			case R.id.txtFresh:
				setType(Global.INVENTORY_FRESH);
				break;
			case R.id.txtReturns:
				setType(Global.INVENTORY_RETURNS);
				break;
			case R.id.txtContainers:
				setType(Global.INVENTORY_CONTAINERS);
				break;
		}
		m_bClicked = false;
	}

	private void doTakeProcess() {
		Global.gAdjustinfo.uInvenDList.clear();
		Global.gAdjustinfo.uInvenDList.addAll(mUInvenDList);
		mContext.replaceFragment(new StockTakeCountEntryFragment());
	}

	private void doAdjustProcess(boolean fromSecurity8) {
		try {
			final AdjustInventoryDialog dlg = new AdjustInventoryDialog(mContext, fromSecurity8);
			dlg.setCancelable(false);
			dlg.setOnDismissListener(new OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
					if (dlg.mReturnCode == Global.DLG_RETURN_RIGHT) {
						if(Utils.getStringSetting(mContext, Global.PREF_DisableInterTruck).equals("3") && dlg.mReasonType == 2)
						{
							String[] strMethodArray = {Str.TransferNow[Global.LANG], Str.TransferLater[Global.LANG]};
							try {
								new AlertDialog.Builder(mContext)
										.setTitle(Str.SelectTransferMethod[Global.LANG])
										.setSingleChoiceItems(new AlertSingleChoiceAdapter(mContext, strMethodArray), -1, (dialog1, nSelected) -> {
											mSelectedTransferMethod = nSelected;
										})
										.setPositiveButton(Str.Ok[Global.LANG], (dialog12, nSelected) -> {
											Global.gAdjustinfo.strReference = dlg.mStrResult;
											Global.gAdjustinfo.uInvenDList.clear();
											Global.gAdjustinfo.uInvenDList.addAll(mUInvenDList);
											Bundle bundle = new Bundle();
											bundle.putInt(Global.EXTRA_TRANSFER_TYPE, dlg.mReasonType);
											bundle.putInt(Global.EXTRA_INVENTORY_TYPE, Global.INVENTORY_FRESH);
											if(mSelectedTransferMethod == 0)
											{
												bundle.putBoolean(Global.EXTRA_TRANSFER_LATER, false);
											}
											else {
												bundle.putBoolean(Global.EXTRA_TRANSFER_LATER, true);
											}
											AdjustInventoryFragment fragment = new AdjustInventoryFragment();
											fragment.setArguments(bundle);
											mContext.replaceFragment(fragment);
										})
										.setNegativeButton(Str.Cancel[Global.LANG], (dialog13, nSelected) -> {

										})
										.create()
										.show();
							}catch (Exception e)
							{
								e.printStackTrace();
							}
						}
						else
						{
							Global.gAdjustinfo.strReference = dlg.mStrResult;
							Global.gAdjustinfo.uInvenDList.clear();
							Global.gAdjustinfo.uInvenDList.addAll(mUInvenDList);
							Bundle bundle = new Bundle();
							bundle.putInt(Global.EXTRA_TRANSFER_TYPE, dlg.mReasonType);
							if(dlg.mReasonType == 2)
							{
								if(Utils.getStringSetting(mContext, Global.PREF_DisableInterTruck).equals("2"))
								{
									bundle.putBoolean(Global.EXTRA_TRANSFER_LATER, true);
								}
								else
								{
									bundle.putBoolean(Global.EXTRA_TRANSFER_LATER, false);
								}
								bundle.putInt(Global.EXTRA_INVENTORY_TYPE, Global.INVENTORY_FRESH);
							}
							else
							{
								bundle.putBoolean(Global.EXTRA_TRANSFER_LATER, false);
								bundle.putInt(Global.EXTRA_INVENTORY_TYPE, mType);
							}
							AdjustInventoryFragment fragment = new AdjustInventoryFragment();
							fragment.setArguments(bundle);
							mContext.replaceFragment(fragment);
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
		HoneywellPrinter.doPrint(mContext, new HoneywellPrinter.PrintCallback() {
			@Override
			public void printFinished() {
				hideProgressDialog();
				try {
					Utils.setStringSetting(mContext, Global.PREF_HONEYWELL_PRINTER_MAC_ADDRESS, mStrMacAddress);
					Utils.printDebug(mContext, Str.SuccessToPrint[Global.LANG]);
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
			goBack();
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
							} else if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
								goBack();
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

		private List<UInvenDInfo> list = new ArrayList<>();

		private class ViewHolder {
			public TextView txtItemNo, txtShortDesc, txtQty;
		}

		public int getCount() {
			return list.size();
		}

		public void setListData(List<UInvenDInfo> list) {
			this.list.clear();
			this.list.addAll(list);
			Collections.sort(this.list, new Comparator<UInvenDInfo>() {
				@Override
				public int compare(UInvenDInfo t1, UInvenDInfo t2) {
					int nSeq1 = getSequenceValue(t1.strItemNo);
					int nSeq2 = getSequenceValue(t2.strItemNo);
					if( nSeq1 == 0 && nSeq2 != 0 )
						return -1;
					else if( nSeq1 != 0 && nSeq2 == 0)
						return 1;
					else if( nSeq1 == 0 && nSeq2 == 0 )
					{
						return t1.strItemNo.compareTo(t2.strItemNo);
					}
					else
						return nSeq1 - nSeq2;
				}
			});
		}

		public UInvenDInfo getItem(int position) {
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
			final UInvenDInfo info = getItem(position);
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

			holder.txtShortDesc.setText(info.strShortDesc);
			if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
			{
				if (mType == Global.INVENTORY_RETURNS) {
					holder.txtQty.setText(String.format(Locale.ENGLISH, "%.2f", info.dDumps));
				} else {
					holder.txtQty.setText(String.format(Locale.ENGLISH, "%.2f", info.dStock));
				}
			}
			else
			{
				if(bShowCase)
				{
					if (mType == Global.INVENTORY_RETURNS) {
						if (Utils.isContainerItem(mContext, info.strItemNo)) {
							holder.txtQty.setText(String.format(Locale.ENGLISH, "%d", info.nDumps));
						}
						else {
							int nCase = getCaseValue(info.strItemNo);
							int nCaseValue = info.nDumps / nCase;
							int nQty = info.nDumps % nCase;
							holder.txtQty.setText(String.format(Locale.ENGLISH, "%d/%d", nCaseValue, nQty));
						}
					} else {
						if (Utils.isContainerItem(mContext, info.strItemNo)) {
							holder.txtQty.setText(String.format(Locale.ENGLISH, "%d", info.nStock));
						}
						else {
							int nCase = getCaseValue(info.strItemNo);
							int nCaseValue = info.nStock / nCase;
							int nQty = info.nStock % nCase;
							holder.txtQty.setText(String.format(Locale.ENGLISH, "%d/%d", nCaseValue, nQty));
						}
					}
				}
				else
				{
					if (mType == Global.INVENTORY_RETURNS) {
						int nQty = info.nDumps;
						holder.txtQty.setText(String.format(Locale.ENGLISH, "%d", nQty));
					} else {
						int nQty = info.nStock;
						holder.txtQty.setText(String.format(Locale.ENGLISH, "%d",nQty));
					}
				}
			}


			view.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
				}
			});
			view.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View view) {
					doShowAvailable(position);
					return true;
				}
			});

			return view;
		}
	}

	private void doShowAvailable(int position)
	{
		UInvenDInfo uOrderDInfo = mListAdapter.getItem(position);
		if(uOrderDInfo == null) return;

		if(m_nInventoryValidationType == 3 || m_nInventoryValidationType == 4 )
		{
			if(Utils.getProdLoconSCWType(mContext, uOrderDInfo.strItemNo) == Global.WEIGHT_ITEM)
			{
				double dStock = getStockValueD(uOrderDInfo.strItemNo);
				double dPresoldStock = getPresoldValueD(uOrderDInfo.strItemNo);
				dStock -= dPresoldStock;
				String strCaption = String.format(Locale.ENGLISH, "Available for upsell %.2f", dStock);
				if(!mContext.isFinishing() && !mContext.isDestroyed())
				{
					try {
						Toast.makeText(mContext, strCaption, Toast.LENGTH_SHORT).show();
					}catch (Exception e1)
					{
						e1.printStackTrace();
					}
				}

			}
			else
			{
				int nStock = getStockValue(uOrderDInfo.strItemNo);
				int nPresoldStock = getPresoldValue(uOrderDInfo.strItemNo);
				nStock -= nPresoldStock;
				String strCaption = "";
				if (Utils.isContainerItem(mContext, uOrderDInfo.strItemNo)) {
					strCaption = String.format(Locale.ENGLISH, "Available for upsell %d", nStock);
				}
				else {
					int nCase = getCaseValue(uOrderDInfo.strItemNo);
					strCaption = String.format(Locale.ENGLISH, "Available for upsell %d / %d", nStock / nCase, nStock % nCase);
				}
				if(!mContext.isFinishing() && !mContext.isDestroyed())
				{
					try {
						Toast.makeText(mContext, strCaption, Toast.LENGTH_SHORT).show();
					}catch (Exception e1)
					{
						e1.printStackTrace();
					}
				}

			}

		}
	}

	Map<String, Integer> mStockMap = new HashMap<>();
	private int getStockValue(String strItemNo) {
		if( mStockMap.get(strItemNo) != null )
			return mStockMap.get(strItemNo);
		int nStock = 0;
		for(UInvenDInfo info : mUInvenDList)
		{
			if( info.strItemNo.equals(strItemNo) )
				nStock += info.nStock;
		}
		mStockMap.put(strItemNo, nStock);
		return nStock;
	}

	Map<String, Double> mStockMapD = new HashMap<>();
	private double getStockValueD(String strItemNo) {
		if( mStockMapD.get(strItemNo) != null )
			return mStockMapD.get(strItemNo);
		double dStock = 0;
		for(UInvenDInfo info : mUInvenDList)
		{
			if( info.strItemNo.equals(strItemNo) )
				dStock += info.dStock;
		}
		mStockMapD.put(strItemNo, dStock);
		return dStock;
	}

	Map<String, Integer> mPresoldMap = new HashMap<>();
	private int getPresoldValue(String strItemNo)
	{
		if( mPresoldMap.get(strItemNo) != null )
			return mPresoldMap.get(strItemNo);
		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		List<PresoldOrDInfo> presoldOrDInfos = dbAdapter.getUnusedPresoldOrDInfo(strItemNo);
		dbAdapter.close();
		int nStock = 0;
		for(PresoldOrDInfo info : presoldOrDInfos)
		{
			if( info.strItemNo.equals(strItemNo) )
				nStock += info.nOrderQty;
		}

		for(CustomerOrderInfo orderInfo : latestOrderInfo)
		{
			for(OrderLineInfo lineInfo : orderInfo.orderLines)
			{
				if(lineInfo.itemNumber.equals(strItemNo))
				{
					nStock += lineInfo.quantity;
				}
			}
		}

		mPresoldMap.put(strItemNo, nStock);
		return nStock;
	}

	Map<String, Double> mPresoldMapD = new HashMap<>();
	private double getPresoldValueD(String strItemNo)
	{
		if( mPresoldMapD.get(strItemNo) != null )
			return mPresoldMapD.get(strItemNo);
		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		List<PresoldOrDInfo> presoldOrDInfos = dbAdapter.getUnusedPresoldOrDInfo(strItemNo);
		dbAdapter.close();
		double dStock = 0;
		for(PresoldOrDInfo info : presoldOrDInfos)
		{
			if( info.strItemNo.equals(strItemNo) )
				dStock += info.dOrderQty;
		}

		for(CustomerOrderInfo orderInfo : latestOrderInfo)
		{
			for(OrderLineInfo lineInfo : orderInfo.orderLines)
			{
				if(lineInfo.itemNumber.equals(strItemNo))
				{
					dStock += lineInfo.quantity;
				}
			}
		}

		mPresoldMapD.put(strItemNo, dStock);
		return dStock;
	}
}