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
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

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
import dsd.dsdvansales.global.BixolonPrinter;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.HoneywellLinePrinter;
import dsd.dsdvansales.global.HoneywellPrinter;
import dsd.dsdvansales.global.PrintEngine;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.model.CustConBalInfo;
import dsd.dsdvansales.model.DescTypeInfo;
import dsd.dsdvansales.model.PricingInfo;
import dsd.dsdvansales.model.PrintItem;
import dsd.dsdvansales.model.ProdDetlInfo;
import dsd.dsdvansales.model.TaxRatesInfo;
import dsd.dsdvansales.model.TransactionInfo;
import dsd.dsdvansales.model.UInvenDInfo;
import dsd.dsdvansales.model.UInvenHInfo;
import dsd.dsdvansales.model.UOrderDInfo;
import dsd.dsdvansales.model.UStockDInfo;
import dsd.dsdvansales.model.UStockHInfo;
import dsd.dsdvansales.model.UTransactionsInfo;
import dsd.dsdvansales.views.AlertSingleChoiceAdapter;

public class AdjustConsignmentInventorySummaryFragment extends BaseFragment implements View.OnClickListener {


	private String mStrPdfFileName = "";
	private String mStrSignatureFilePath = "";
	private long mPdfTrxnNo = 0;

	private Connection printerConnection = null;
	private String mStrMacAddress = "";
	private String mStrFriendlyName = "";
	private boolean mIsCreatedPDF = false;

	private String mCartonDescription = "";

	private boolean bLastProcess = false;

	private int nTransferType = 0;

	private String mStrReceiverName = "";

	PrintEngine mHoneywellPrintEngine = null;
	String mHoneywellTemplatePath = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootView = inflater.inflate(R.layout.fragment_adjust_consignment_inventory_summary, container, false);
		commonInit();
		return mRootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		Global.gFragIdx = Global.FRAG_CONSIGNMENT_INVENTORY_SUMMARY;
		bPaused = false;

		if(bNeedBack)
		{
			new Handler(Looper.getMainLooper()).post(new Runnable() {
				@Override
				public void run() {
					Fragment fragment = mContext.getSupportFragmentManager().findFragmentById(R.id.frameContainer);
					while(!(fragment instanceof InstoreFragment))
					{
						if( fragment instanceof  AdjustConsignmentInventoryFragment)
						{
							((AdjustConsignmentInventoryFragment)fragment).mExist = true;
						}
						mContext.onBackPressed();
						fragment = mContext.getSupportFragmentManager().findFragmentById(R.id.frameContainer);
					}
				}
			});
		}
	}

	@Override
	public void initView() {
		mRootView.findViewById(R.id.btnSignature).setOnClickListener(this);
		mRootView.findViewById(R.id.btnCompleted).setOnClickListener(this);
		mRootView.findViewById(R.id.btnBack).setOnClickListener(this);

		loadData();
		refreshLanguage();

	}

	@Override
	public void loadData() {

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

	}

	@Override
	public void refreshLanguage() {
		((TextView) mRootView.findViewById(R.id.lblTitle)).setText(Str.Consignment_Inventory_Summary[Global.LANG].toUpperCase());
		((Button) mRootView.findViewById(R.id.btnSignature)).setText(Str.Signature[Global.LANG]);
		((Button) mRootView.findViewById(R.id.btnCompleted)).setText(Str.Completed[Global.LANG]);
		((TextView) mRootView.findViewById(R.id.lblReceiverName)).setText(Str.ReceiverName[Global.LANG]);

		int nUnits = 0;
		double dValue = 0;
		if (Global.gStockDInfoList != null) {
			for (UStockDInfo info : Global.gStockDInfoList) {
				if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
				{
					if(nTransferType == 0)
					{
						dValue += (info.dActualQty - info.dBeginQty);
					}
					else
					{
						dValue += (info.dBeginQty - info.dActualQty);
					}
				}
				else
				{
					if(nTransferType == 0)
					{
						nUnits += (info.nActualQty - info.nBeginQty);
					}
					else
					{
						nUnits += (info.nBeginQty - info.nActualQty);
					}
				}
			}
		}

		String strLoaded = "";


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

		if(dValue > 0)
		{
			if(strLoaded.length() > 0) strLoaded += ", ";
			strLoaded += String.format(Locale.ENGLISH, "%.2f Kg", dValue);
		}

		if(nTransferType == 0)
		{
			strLoaded = String.format(Locale.ENGLISH, Str.added_shelf[Global.LANG], strLoaded);
		}
		else
		{
			strLoaded = String.format(Locale.ENGLISH, Str.remove_shelf[Global.LANG], strLoaded);
		}

		((TextView) mRootView.findViewById(R.id.lblYouHaveLoaded)).setText(strLoaded);

	}

	@Override
	public void receiveBroadcast(String strAction, Intent intent) {
		if (strAction.equals(Global.ACTION_CAPTURE_SIGNATURE) == true) {
			if (Global.gFragIdx == Global.FRAG_CONSIGNMENT_INVENTORY_SUMMARY) {
				mStrSignatureFilePath = intent.getStringExtra(Global.EXTRA_SIGNATURE_PATH);
				Utils.printDebug(mContext, Str.Saved_successfully[Global.LANG]);
			}
		}
	}

	private void doComplete() {
		mStrReceiverName = ((EditText)mRootView.findViewById(R.id.editReceiverName)).getText().toString();
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

				String strTemplateFileName = Utils.getFinalPrintTemplateFileName(mContext, Global.PRINT_CONSIGNMENT_INVENTORY_FILENAME);
				if(Utils.isFileExist(Global.ROOT_PRINT_TEMPLATE_LOCCATION + "/" + strTemplateFileName))
				{
					Utils.createDirectory(Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/");
					mPdfTrxnNo = System.currentTimeMillis();
					mStrPdfFileName = getPDFFileName();
				}

				if(nTransferType == 0)
				{
					setGeneralUploadData("ADJI", Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo, -1);
				}
				else if(nTransferType == 1)
				{
					setGeneralUploadData("ADJO", Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo, -1);
				}
				else
				{
					setGeneralUploadData("ADJS", Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo, -1);
				}

				UStockHInfo uStockHInfo = new UStockHInfo();
				uStockHInfo.strStockType = "2";
				uStockHInfo.nTrxnNo = m_lTrxnNo;
				uStockHInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
				uStockHInfo.nChainNo = Global.gCurCustomerInfo.nChainNo;

				uStockHInfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(mContext, mCurDate));
				uStockHInfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(mCurDate);
				if(!TextUtils.isEmpty(mStrPdfFileName))
				{
					uStockHInfo.strDocNo = mStrPdfFileName.substring(0, mStrPdfFileName.length() - 4);
				}
				if(nTransferType == 0)
				{
					uStockHInfo.strDocType = "ADJI";
				}
				else if(nTransferType == 1)
				{
					uStockHInfo.strDocType = "ADJO";
				}
				else
				{
					uStockHInfo.strDocType = "ADJS";
				}

				DbAdapter dbAdapter = new DbAdapter(mContext);
				dbAdapter.open();
				Map<String, ProdDetlInfo> mProdDetlItemMap = new HashMap<>();
				Map<String, ProdDetlInfo> mProdDetlUPCMap = new HashMap<>();
				dbAdapter.getProductList(mProdDetlItemMap, mProdDetlUPCMap);
				Map<String, CustConBalInfo> custConBalInfoMap = new HashMap<>();
				Map<String, PricingInfo> mPriceInfoMap = dbAdapter.getPricingInfo(Global.gCurCustomerInfo.nCustNo);
				for(CustConBalInfo custConBalInfo : Global.gCustConBalList)
				{
					custConBalInfoMap.put(custConBalInfo.strItemNo, custConBalInfo);

					ProdDetlInfo productInfo = mProdDetlItemMap.get(custConBalInfo.strItemNo);
					if(productInfo != null)
					{
						if(productInfo.prodLocnInfo != null)
						{
							if(custConBalInfo.nCustNo != 0)
							{
								PricingInfo pricingInfo = mPriceInfoMap.get(custConBalInfo.strItemNo);
								if(pricingInfo != null)
								{
									custConBalInfo.nPrice = Utils.getDividedValue(pricingInfo.nPrice);
								}
								else
								{
									custConBalInfo.nPrice = Utils.getDividedValue(productInfo.prodLocnInfo.nBasePrice);;
								}
							}
							else
							{
								custConBalInfo.nPrice = Utils.getDividedValue(productInfo.prodLocnInfo.nBasePrice);;
							}
						}
					}

					List<TaxRatesInfo> taxRatesInfoList = dbAdapter.getTaxRatesInfoList(Global.gCurCustomerInfo.strTaxCode, custConBalInfo.strItemNo);
					float nLinePrinceTaxAmount = 0;
					for(TaxRatesInfo taxRatesInfo : taxRatesInfoList) {
						if (taxRatesInfo.nTaxMethod == 1) //Applying Cents Amount
						{
							nLinePrinceTaxAmount += Utils.getDividedValue(taxRatesInfo.nTaxRate) * productInfo.nConsumerUnit;
						} else if (taxRatesInfo.nTaxMethod == 2) {
							nLinePrinceTaxAmount += (Utils.getRoundedValue(custConBalInfo.nPrice) + nLinePrinceTaxAmount) / 100 * Utils.getDividedValue(taxRatesInfo.nTaxRate);
						}
					}
					custConBalInfo.nPrice = Utils.getRoundedValue(custConBalInfo.nPrice + Utils.get5RoundedValue(mContext, nLinePrinceTaxAmount));
				}

				dbAdapter.insertUConsignmentInfoList(uStockHInfo);

				List<UInvenDInfo> uInvenDInfoList = dbAdapter.getUInvenDList(Utils.getLongSetting(mContext, Global.PREF_UINVEN_TRXN_NO));
				Map<String, UInvenDInfo> uInvenDInfoMap = new HashMap<>();
				for(UInvenDInfo uInvenDInfo : uInvenDInfoList)
				{
					uInvenDInfoMap.put(uInvenDInfo.strItemNo, uInvenDInfo);
				}

				UInvenHInfo uInvenHInfo = new UInvenHInfo();
				uInvenHInfo.strDocType = "IVUP";
				uInvenHInfo.nTrxnNo = uStockHInfo.nTrxnNo;
				uInvenHInfo.strTrxnDate = uStockHInfo.strTrxnDate;
				uInvenHInfo.strTrxnTime = uStockHInfo.strTrxnTime;
				uInvenHInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
				dbAdapter.insertUInvenHInfo(uInvenHInfo);

				for(UStockDInfo uStockDInfo : Global.gStockDInfoList)
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

					uStockDInfo.nTrxnNo = m_lTrxnNo;
					CustConBalInfo custConBalInfo = custConBalInfoMap.get(uStockDInfo.strItemNo);
					if(custConBalInfo == null)
					{
						custConBalInfo = new CustConBalInfo();
						custConBalInfo.strItemNo = uStockDInfo.strItemNo;
						custConBalInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
						custConBalInfo.nChainNo = Global.gCurCustomerInfo.nChainNo;

						ProdDetlInfo productInfo = mProdDetlItemMap.get(custConBalInfo.strItemNo);
						if(productInfo != null)
						{
							if(productInfo.prodLocnInfo != null)
							{
								if(custConBalInfo.nCustNo != 0)
								{
									PricingInfo pricingInfo = mPriceInfoMap.get(custConBalInfo.strItemNo);
									if(pricingInfo != null)
									{
										custConBalInfo.nPrice = Utils.getDividedValue(pricingInfo.nPrice);
									}
									else
									{
										custConBalInfo.nPrice = Utils.getDividedValue(productInfo.prodLocnInfo.nBasePrice);;
									}
								}
								else
								{
									custConBalInfo.nPrice = Utils.getDividedValue(productInfo.prodLocnInfo.nBasePrice);;
								}
							}
						}

						List<TaxRatesInfo> taxRatesInfoList = dbAdapter.getTaxRatesInfoList(Global.gCurCustomerInfo.strTaxCode, custConBalInfo.strItemNo);
						float nLinePrinceTaxAmount = 0;
						for(TaxRatesInfo taxRatesInfo : taxRatesInfoList) {
							if (taxRatesInfo.nTaxMethod == 1) //Applying Cents Amount
							{
								nLinePrinceTaxAmount += Utils.getDividedValue(taxRatesInfo.nTaxRate) * productInfo.nConsumerUnit;
							} else if (taxRatesInfo.nTaxMethod == 2) {
								nLinePrinceTaxAmount += (Utils.getRoundedValue(custConBalInfo.nPrice) + nLinePrinceTaxAmount) / 100 * Utils.getDividedValue(taxRatesInfo.nTaxRate);
							}
						}
						custConBalInfo.nPrice = Utils.getRoundedValue(custConBalInfo.nPrice + Utils.get5RoundedValue(mContext, nLinePrinceTaxAmount));

						if(Utils.getProdLoconSCWType(mContext, uStockDInfo.strItemNo) == Global.WEIGHT_ITEM)
						{
							custConBalInfo.fWeight = (float)uStockDInfo.dActualQty;
						}
						else
						{
							custConBalInfo.nQty = uStockDInfo.nActualQty;
						}
						custConBalInfoMap.put(uStockDInfo.strItemNo, custConBalInfo);
						Global.gCustConBalList.add(custConBalInfo);
						dbAdapter.insertCustConBalInfo(custConBalInfo);
					}
					else
					{
						if(Utils.getProdLoconSCWType(mContext, uStockDInfo.strItemNo) == Global.WEIGHT_ITEM)
						{
							custConBalInfo.fWeight = (float)uStockDInfo.dActualQty;
						}
						else
						{
							custConBalInfo.nQty = uStockDInfo.nActualQty;
						}
						dbAdapter.updateCustConBalInfo(custConBalInfo);
					}

					UInvenDInfo uInvenDInfo = uInvenDInfoMap.get(uStockDInfo.strItemNo);
					if(uInvenDInfo == null)
					{
						uInvenDInfo = new UInvenDInfo();
						uInvenDInfo.nTrxnNo = Utils.getLongSetting(mContext, Global.PREF_UINVEN_TRXN_NO);
						uInvenDInfo.strItemNo = uStockDInfo.strItemNo;
						Global.gCurInventoryList.add(uInvenDInfo);
						uInvenDInfoMap.put(uInvenDInfo.strItemNo, uInvenDInfo);
						uInvenDInfo.bShow = true;
						uInvenDInfo.nIdx = (int)dbAdapter.insertUInvenDInfo(uInvenDInfo);
					}

					if(uInvenDInfo != null)
					{
						UInvenDInfo newUInvenDInfo = uInvenDInfo.copyInfo();
						newUInvenDInfo.nTrxnNo = uInvenHInfo.nTrxnNo;
						if(nTransferType == 0)
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
						else if(nTransferType == 1)
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
						else if(nTransferType == 2)
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
						dbAdapter.insertUStockDInfoList(uStockDInfo);
						dbAdapter.insertUInvenDInfo(newUInvenDInfo);
					}
				}
				dbAdapter.close();

				strTemplateFileName = Utils.getFinalPrintTemplateFileName(mContext, Global.PRINT_CONSIGNMENT_INVENTORY_FILENAME);
				if(Utils.isFileExist(Global.ROOT_PRINT_TEMPLATE_LOCCATION + "/" + strTemplateFileName))
				{
					PrintEngine printEngine = new PrintEngine();

					dbAdapter = new DbAdapter(mContext);
					dbAdapter.open();
					Utils.gDetlMap.clear();
					if(Global.gCustConBalList != null)
					{
						for(CustConBalInfo info : Global.gCustConBalList)
						{
							ProdDetlInfo prodDetlInfo = dbAdapter.getProdDetl(info.strItemNo);
							if(prodDetlInfo != null)
							{
								Utils.gDetlMap.put(prodDetlInfo.strItemNo, Utils.getPropertyMap(prodDetlInfo));
							}
						}
					}
					dbAdapter.close();
					mHoneywellPrintEngine = printEngine;

					String strHoneywellFileName = Utils.getFinalPrintTemplateFileNameForHoneywell(mContext, Global.PRINT_CONSIGNMENT_INVENTORY_FILENAME);
					mHoneywellTemplatePath = Global.ROOT_PRINT_TEMPLATE_LOCCATION + strHoneywellFileName;

					printEngine.PrepareConsignmentInventoryPrint(mContext, Global.gCustConBalList, new Date(), mStrPdfFileName.substring(0, mStrPdfFileName.length() - 4), mStrSignatureFilePath, mStrReceiverName);
					printEngine.createDocument(mContext, false, Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + mStrPdfFileName, Global.CONSIGNMENT_INVENTORY_PRINT);

					long nTrxnNo = System.currentTimeMillis();

					//File Transaction
					UTransactionsInfo uTransactionsInfo = setUTransactionsInfo(Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo, nTrxnNo, "FARC", mStrPdfFileName);
					setUFileTransactionInfo(uTransactionsInfo.nTrxnNo, Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo, mPdfTrxnNo, mStrPdfFileName.substring(0, mStrPdfFileName.length() - 4), "CONSIGNMENTINVENTORY", "CONSIGNMENT INVENTORY", uTransactionsInfo.strTrxnDate, uTransactionsInfo.strTrxnTime, mStrPdfFileName);

					//Register print list
					PrintItem printItem = new PrintItem();
					printItem.nTrxnNo = uStockHInfo.nTrxnNo;
					printItem.strDocNo = uStockHInfo.strDocNo;
					printItem.strDocType = uStockHInfo.strDocType;
					printItem.strFilePath = Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + mStrPdfFileName;
					printItem.nCustNo = Global.gCurCustomerInfo.nCustNo;
					printItem.nChainNo = Global.gCurCustomerInfo.nChainNo;
					printItem.strCustomerName = Global.gCurCustomerInfo.strName;

					dbAdapter = new DbAdapter(mContext);
					dbAdapter.open();
					dbAdapter.insertPrint(printItem);
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

	private void outputPrinter() {
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
		Utils.registerUploadFile(mContext, makeZipFileInfo);
		Global.gAdjustDateTime = System.currentTimeMillis();
		bLastProcess = true;
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
			new Handler(Looper.getMainLooper()).post(new Runnable() {
				@Override
				public void run() {
					Fragment fragment = mContext.getSupportFragmentManager().findFragmentById(R.id.frameContainer);
					while(!(fragment instanceof InstoreFragment))
					{
						if( fragment instanceof  AdjustConsignmentInventoryFragment)
						{
							((AdjustConsignmentInventoryFragment)fragment).mExist = true;
						}
						mContext.onBackPressed();
						fragment = mContext.getSupportFragmentManager().findFragmentById(R.id.frameContainer);
					}
				}
			});

		}
	}

	@Override
	public void onPause() {
		super.onPause();
		bPaused = true;
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
					if( Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 1)
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
				}
			}catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}