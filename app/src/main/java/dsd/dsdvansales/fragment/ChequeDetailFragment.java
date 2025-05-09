package dsd.dsdvansales.fragment;

import java.text.*;
import java.util.*;
import java.util.concurrent.CountDownLatch;

import android.app.*;
import android.bluetooth.BluetoothAdapter;
import android.content.*;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.*;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

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

import dsd.dsdvansales.R;
import dsd.dsdvansales.db.DbAdapter;
import dsd.dsdvansales.dialogs.MessageDialog;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.HoneywellLinePrinter;
import dsd.dsdvansales.global.HoneywellPrinter;
import dsd.dsdvansales.global.PrintEngine;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.model.ARHeaderInfo;
import dsd.dsdvansales.model.DescTypeInfo;
import dsd.dsdvansales.model.PrintItem;
import dsd.dsdvansales.model.TransactionInfo;
import dsd.dsdvansales.model.UARPaymentInfo;
import dsd.dsdvansales.model.UCollectionsDInfo;
import dsd.dsdvansales.model.UTransactionsInfo;
import dsd.dsdvansales.views.AlertSingleChoiceAdapter;

@SuppressWarnings("deprecation")
public class ChequeDetailFragment extends BaseFragment implements View.OnClickListener {

	private long mDate = 0;
	private String mStrBankNo = "";
	private String mStrAccount = "";
	private String mStrCheckNo = "";
	private String mStrARNotes = "";
	private float mAmount = 0;
	private EditText mEditAmount = null;
	private AutoCompleteTextView mAutoBankCode = null;

	private boolean mIsRoundPrice = false;
	public boolean m_bAutoCollected = false;

	public UCollectionsDInfo mCollectionInfo = null;

	private Connection printerConnection = null;
	private String mStrMacAddress = "";
	private String mStrPdfFileName = "";
	public boolean bClickedDone = false;

	public List<String> mBankCodeArray = new ArrayList<String>();
	ListAdapter mBankCodeAdapter;

	PrintEngine mHoneywellPrintEngine = null;
	String mHoneywellTemplatePath = null;
	String getRoundedFormattedPrice(float value)
	{
		if( mIsRoundPrice && (Global.gCurCustomerInfo.strPayType.trim().equals("1") || Global.gCurCustomerInfo.strPayType.trim().equals("2")))
		{
			float fNeg = 1;
			if(value < 0)
			{
				fNeg = -1;
			}
			int nTotal = Math.round(Utils.getRoundedValue(value * fNeg) * Global.MULTIPLE_VALUE);
			int nOffSet = nTotal % 5000;
			int nInvoiceTotal = ((nTotal / 5000) + ((Math.abs(nOffSet) >= 2500) ? 1 : 0)) * 5000;
			nOffSet = nTotal % 1000;
			nTotal = ((nTotal / 1000) + ((Math.abs(nOffSet) >= 500) ? 1 : 0)) * 1000;
			nOffSet = nInvoiceTotal - nTotal;
			
			float dValue = nInvoiceTotal / (Global.MULTIPLE_VALUE * 1f) * fNeg;
			String sValue = String.format(Locale.ENGLISH, "%s" + "%." + Utils.getIntSetting(mContext, Global.PREF_DECIMAL) + "f", Global.gCurrencySymbol, dValue);
			return sValue;
		}
		else
		{
			return String.format(Locale.ENGLISH, "%s" + "%." + Utils.getIntSetting(mContext, Global.PREF_DECIMAL) + "f", Global.gCurrencySymbol, value);
		}
		
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootView = inflater.inflate(R.layout.fragment_cheque_detail, container, false);
		commonInit();
		return mRootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		Global.gFragIdx = Global.FRAG_CHEQUE_DETAIL;
		bPaused = false;
		if(bNeedBack)
		{
			new Handler(Looper.getMainLooper()).post(new Runnable() {
				@Override
				public void run() {
					if (m_bAutoCollected && mCollectionInfo != null)
					{

					}
					else
					{
						Global.gUARPaymentChequeInfo.strBankNo = mStrBankNo;
						Global.gUARPaymentChequeInfo.strAccount = mStrAccount;
						Global.gUARPaymentChequeInfo.strCheckNo = mStrCheckNo;
						Global.gUARPaymentChequeInfo.strCheckDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(new Date(mDate));
						Global.gUARPaymentChequeInfo.strARNotes = mStrARNotes;
						Global.gUARPaymentChequeInfo.nTrxnAmount = mAmount;
					}
					bClickedDone = true;
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
		mRootView.findViewById(R.id.btnPickDate).setOnClickListener(this);
		mRootView.findViewById(R.id.btnBack).setOnClickListener(this);
		mRootView.findViewById(R.id.btnCompleted).setOnClickListener(this);

		mAutoBankCode = mRootView.findViewById(R.id.autoBankBranch);
		mAutoBankCode.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View view, boolean b) {
				if (b == true) {
					mAutoBankCode.showDropDown();
				}
			}
		});

		mAutoBankCode.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (!mAutoBankCode.isPopupShowing()) {
					mAutoBankCode.showDropDown();
				}
			}
		});

		if( Utils.getStringSetting(mContext, Global.PREF_ROUTE_CENTSROUND).trim().equals("1") &&
				(Global.gCurCustomerInfo.strPayType.trim().equals("1") || Global.gCurCustomerInfo.strPayType.trim().equals("2")))
		{
			mIsRoundPrice = true;
		}
		else if(Utils.getStringSetting(mContext, Global.PREF_ROUTE_CENTSROUND).trim().equals("2"))
		{
			mIsRoundPrice = true;
		}
		else
		{
			mIsRoundPrice = false;
		}
		
		mEditAmount = (EditText) mRootView.findViewById(R.id.editChequeAmount);
		mEditAmount.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				if (mEditAmount.getTag() == null) {
					setFormattedAmount(s.toString());
				}
			}
		});
		mEditAmount.post(new Runnable() {
			public void run() {
				mEditAmount.setSelection(mEditAmount.getText().toString().length());
				mEditAmount.postDelayed(this, 100);
			}
		});

		((TextView) mRootView.findViewById(R.id.txtCustomerName)).setText(Global.gCurCustomerInfo.strName);
		loadData();

		mBankCodeAdapter = new ListAdapter(mContext, mBankCodeArray);
		mAutoBankCode.setAdapter(mBankCodeAdapter);

		refreshLanguage();
	}

	@Override
	public void loadData() {

		// Bank Code
		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		List<DescTypeInfo> bankCodeDescTypeInfos = dbAdapter.getDescTypeList("BANKCODE");
		dbAdapter.close();
		Collections.sort(bankCodeDescTypeInfos, new Comparator<DescTypeInfo>() {
			@Override
			public int compare(DescTypeInfo descTypeInfo, DescTypeInfo t1) {
				return descTypeInfo.nNumericKey - t1.nNumericKey;
			}
		});
		for (DescTypeInfo descTypeInfo: bankCodeDescTypeInfos) {
			mBankCodeArray.add(descTypeInfo.strDescription);
		}

		Bundle args = getArguments();
		if( args != null )
		{
			m_bAutoCollected = args.getBoolean(Global.EXTRA_IS_AUTO_COLLECT, false);
		}

		if( m_bAutoCollected )
		{
			doProcessLastChequeDetailInfo();
			dbAdapter = new DbAdapter(mContext);
			dbAdapter.open();
			List<UCollectionsDInfo> uCollectionsDInfos = dbAdapter.getUCollectionsDList(Global.gCurCustomerInfo.nCustNo, Global.gCurCustomerInfo.nChainNo);
			for(int i = uCollectionsDInfos.size() - 1; i >= 0; i--)
			{
				if(uCollectionsDInfos.get(i).nPayType == Global.COLLECTION_CHEQUE)
				{
					mCollectionInfo = uCollectionsDInfos.get(i);
					break;
				}
			}
			dbAdapter.close();
		}
		if (Global.gUARPaymentChequeInfo.strCheckDate.length() > 0) {
			mDate = Utils.getDateFromString(Global.gUARPaymentChequeInfo.strCheckDate).getTime();
		} else {
			mDate = new Date().getTime();
		}
		showTimes();
		if( m_bAutoCollected && mCollectionInfo != null)
		{
			mAmount = mCollectionInfo.nTrxnAmount;
		}
		else
		{
			mAmount = Global.gUARPaymentChequeInfo.nTrxnAmount;
		}
		setAmount();
		refreshPage();
	}

	float getRoundedValue(float value)
	{
		boolean bCentsRound;
		if( Utils.getStringSetting(mContext, Global.PREF_ROUTE_CENTSROUND).trim().equals("1") &&
				(Global.gCurCustomerInfo.strPayType.trim().equals("1") || Global.gCurCustomerInfo.strPayType.trim().equals("2")))
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
		if( bCentsRound )
		{
			float fNeg = 1;
			if(value < 0)
			{
				fNeg = -1;
			}
			int nTotal = Math.round(Utils.getRoundedValue(value * fNeg) * Global.MULTIPLE_VALUE);
			int nOffSet = nTotal % 5000;
			int nInvoiceTotal = ((nTotal / 5000) + ((Math.abs(nOffSet) >= 2500) ? 1 : 0)) * 5000;
			nOffSet = nTotal % 1000;
			nTotal = ((nTotal / 1000) + ((Math.abs(nOffSet) >= 500) ? 1 : 0)) * 1000;
			nOffSet = nInvoiceTotal - nTotal;

			float dValue = nInvoiceTotal / (Global.MULTIPLE_VALUE * 1f);
			return fNeg * dValue;
		}
		else
		{
			return Utils.getRoundedValue(value);
		}
	}

	@Override
	public void refreshPage() {
		float nCollectionRequired = 0;

		float nTotalCollected;
		if( m_bAutoCollected && mCollectionInfo != null )
		{
			DbAdapter dbAdapter = new DbAdapter(mContext);
			dbAdapter.open();
			List<ARHeaderInfo> arHeaderInfos = dbAdapter.getARHeaderInfo(Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo);
			for(ARHeaderInfo arHeaderInfo : arHeaderInfos)
			{
				if( arHeaderInfo.nProcessStatus != 2 )
				{
					nCollectionRequired += getRoundedValue(arHeaderInfo.nTrxnAmount * 1.0f / Global.MULTIPLE_VALUE);
				}
			}
			dbAdapter.close();
			nTotalCollected = getRoundedValue(mCollectionInfo.nTrxnAmount);
			nCollectionRequired += nTotalCollected;
		}
		else
		{
			DbAdapter dbAdapter = new DbAdapter(mContext);
			dbAdapter.open();
			List<ARHeaderInfo> arHeaderInfos = dbAdapter.getARHeaderInfo(Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo);
			for(ARHeaderInfo arHeaderInfo : arHeaderInfos)
			{
				if( arHeaderInfo.nProcessStatus <= 2 )
				{
					nCollectionRequired += getRoundedValue(arHeaderInfo.nTrxnAmount * 1.0f / Global.MULTIPLE_VALUE);
				}
			}
			dbAdapter.close();
			nTotalCollected = getRoundedValue(Global.gUARTotalInfo.nTotalCash + Global.gUARPaymentChequeInfo.nTrxnAmount + Global.gUARPaymentCardInfo.nTrxnAmount + Global.gUARPaymentEFTInfo.nTrxnAmount);
		}
		float nTotalRemaining = nCollectionRequired - nTotalCollected;
		((TextView) mRootView.findViewById(R.id.txtCollectionRequired)).setText(getRoundedFormattedPrice(nCollectionRequired));
		((TextView) mRootView.findViewById(R.id.txtTotalCollected)).setText(getRoundedFormattedPrice(nTotalCollected));
		((TextView) mRootView.findViewById(R.id.txtTotalRemaining)).setText(getRoundedFormattedPrice(nTotalRemaining));
		if( m_bAutoCollected && mCollectionInfo != null )
		{
			//((EditText) mRootView.findViewById(R.id.editBankBranch)).setText(mCollectionInfo.strBankNo);
			mAutoBankCode.setText(mCollectionInfo.strBankNo);
			((EditText) mRootView.findViewById(R.id.editAccount)).setText(mCollectionInfo.strAccount);
			((EditText) mRootView.findViewById(R.id.editChequeNumber)).setText(mCollectionInfo.strCheckNo);
			((EditText) mRootView.findViewById(R.id.editARNotes)).setText("");
		}
		else
		{
			//((EditText) mRootView.findViewById(R.id.editBankBranch)).setText(Global.gUARPaymentChequeInfo.strBankNo);
			mAutoBankCode.setText(Global.gUARPaymentChequeInfo.strBankNo);
			((EditText) mRootView.findViewById(R.id.editAccount)).setText(Global.gUARPaymentChequeInfo.strAccount);
			((EditText) mRootView.findViewById(R.id.editChequeNumber)).setText(Global.gUARPaymentChequeInfo.strCheckNo);
			((EditText) mRootView.findViewById(R.id.editARNotes)).setText(Global.gUARPaymentChequeInfo.strARNotes);
		}

		((TextView) mRootView.findViewById(R.id.lblCurrencySymbol)).setText(Global.gCurrencySymbol);
	}

	@Override
	public void refreshLanguage() {

		if(  m_bAutoCollected && mCollectionInfo != null  )
		{
			mEditAmount.setEnabled(false);
			mRootView.findViewById(R.id.btnBack).setVisibility(View.INVISIBLE);
		}
		else
		{
			mEditAmount.setEnabled(true);
			mRootView.findViewById(R.id.btnBack).setVisibility(View.VISIBLE);
		}
		mRootView.findViewById(R.id.btnPickDate).setVisibility(View.VISIBLE);

		((TextView) mRootView.findViewById(R.id.lblTitle)).setText(Str.ChequeDetails[Global.LANG].toUpperCase());
		((TextView) mRootView.findViewById(R.id.lblCollectionRequired)).setText(Str.CollectionRequired[Global.LANG]);
		((TextView) mRootView.findViewById(R.id.lblTotalCollected)).setText(Str.TotalCollected[Global.LANG]);
		((TextView) mRootView.findViewById(R.id.lblTotalRemaining)).setText(Str.TotalRemaining[Global.LANG]);
		((TextView) mRootView.findViewById(R.id.lblBankBranch)).setText(Str.BankBranch[Global.LANG]);
		((TextView) mRootView.findViewById(R.id.lblAccount)).setText(Str.Account[Global.LANG]);
		((TextView) mRootView.findViewById(R.id.lblChequeNumber)).setText(Str.ChequeNumber[Global.LANG]);
		((TextView) mRootView.findViewById(R.id.lblChequeDate)).setText(Str.ChequeDate[Global.LANG]);
		((TextView) mRootView.findViewById(R.id.lblChequeAmount)).setText(Str.ChequeAmount[Global.LANG]);
		((TextView) mRootView.findViewById(R.id.lblARNotes)).setText(Str.ARNotes[Global.LANG]);
		((Button) mRootView.findViewById(R.id.btnCompleted)).setText(Str.Done[Global.LANG]);
	}

	@Override
	public void receiveBroadcast(String strAction, Intent intent) {
	}

	private void setFormattedAmount(String strVal) {
		String strArray[] = strVal.split("\\.");
		strVal = "";
		for (int i=0; i<strArray.length; i++) {
			strVal += strArray[i];
		}
		int nValue = Utils.getIntSetting(mContext, Global.PREF_DECIMAL);
		mAmount = Utils.parseFloatFromString(strVal) / (float)(Math.pow(10,nValue));
		setAmount1();
	}

	private void setAmount1() {
		mEditAmount.setTag("From App");
		String strAmount = String.format(Locale.ENGLISH, "%s" + "%." + Utils.getIntSetting(mContext, Global.PREF_DECIMAL) + "f", Global.gCurrencySymbol, mAmount);;
		mEditAmount.setText(strAmount.substring(Global.gCurrencySymbol.length()));
		mEditAmount.setSelection(mEditAmount.getText().toString().length());
		mEditAmount.setTag(null);
	}
	
	private void setAmount() {
		mEditAmount.setTag("From App");
		String strAmount = getRoundedFormattedPrice(mAmount);
		mEditAmount.setText(strAmount.substring(Global.gCurrencySymbol.length()));
		mEditAmount.setSelection(mEditAmount.getText().toString().length());
		mEditAmount.setTag(null);
	}

	private void showTimes() {
		Date date = new Date(mDate);
		((TextView) mRootView.findViewById(R.id.txtChequeDate)).setText(new SimpleDateFormat("EEE " + Utils.getDateFormat(mContext, "dd/MM/yyyy")).format(date));
	}

	private void doComplete() {
		//mStrBankNo = ((EditText) mRootView.findViewById(R.id.editBankBranch)).getText().toString();
		mStrBankNo = mAutoBankCode.getText().toString();
		mStrAccount = ((EditText) mRootView.findViewById(R.id.editAccount)).getText().toString();
		mStrCheckNo = ((EditText) mRootView.findViewById(R.id.editChequeNumber)).getText().toString();
		mStrARNotes = ((EditText) mRootView.findViewById(R.id.editARNotes)).getText().toString();

		if (m_bAutoCollected == false &&
				(mStrBankNo.length() == 0
						|| mStrAccount.length() == 0
						|| mStrCheckNo.length() == 0
						/*|| mStrARNotes.length() == 0*/)) {
			try {
				final MessageDialog msgDialog = new MessageDialog(mContext);
				msgDialog.setCancelable(false);
				msgDialog.mStrMessage = Str.NotAllValuesEntered_msg[Global.LANG];
				msgDialog.mStrLeft = Str.Return[Global.LANG];
				msgDialog.mStrRight = Str.Proceed[Global.LANG];
				msgDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
							goBack();
						}
					}
				});
				msgDialog.show();
			}catch (Exception e)
			{
				e.printStackTrace();
			}
		} else {
			if(m_bAutoCollected)
			{
				createPdf();
			}
			else
			{
				goBack();
			}
		}
	}

	private void doProcessLastChequeDetailInfo()
	{
		if( Global.g_bCurPaymentMethod && Global.gARHeaderInfoList.size() > 0)
		{
			int nSeqNo = Utils.getIntSetting(mContext, String.format(Locale.ENGLISH, Global.PREF_COLLECTION_SEQUENCE_NO, Global.gTripNumber));
			DbAdapter dbAdapter = new DbAdapter(mContext);
			dbAdapter.open();

			long nTrxnNo = System.currentTimeMillis();
			//AddTrxnNo(Global.gCurCustomerInfo.nCustNo, Global.gCurCustomerInfo.nChainNo, nTrxnNo);

			String strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(mContext, new Date(nTrxnNo)));
			UCollectionsDInfo uCollectionsDInfo = new UCollectionsDInfo();
			uCollectionsDInfo.nTrxnNo = nTrxnNo;
			uCollectionsDInfo.nSeqNo = nSeqNo;

			uCollectionsDInfo.nPayType = Global.COLLECTION_CHEQUE;

			uCollectionsDInfo.strTrxnDate = strTrxnDate;
			uCollectionsDInfo.nTrxnAmount = 0;
			for(ARHeaderInfo arHeaderInfo : Global.gARHeaderInfoList)
			{
				if( arHeaderInfo.nProcessStatus < 2 )
				{
					long nNewTrxnNo = System.currentTimeMillis();
					if(nNewTrxnNo == nTrxnNo)
					{
						nTrxnNo ++;
					}
					else
					{
						nTrxnNo = nNewTrxnNo;
					}
					//AddTrxnNo(Global.gCurCustomerInfo.nCustNo, Global.gCurCustomerInfo.nChainNo, nTrxnNo);

					Global.gUARTotalInfo.nTrxnNo = nTrxnNo;
					Global.gUARTotalInfo.nChainNo = Global.gCurCustomerInfo.nChainNo;
					Global.gUARTotalInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
					Global.gUARTotalInfo.nInvoiceTrxnNo = Global.gUOrderHInfo.nTrxnNo;
					Global.gUARTotalInfo.nInvoiceAmount = arHeaderInfo.nTrxnAmount * 1.0f / Global.MULTIPLE_VALUE;

					if (Global.gCurCustomerInfo.nInvoiceFmt == 1) {
						Global.gUARTotalInfo.bIsVisitedDelivery = true;
						Global.gUARTotalInfo.bIsVisitedPickup = true;
						Global.gUARTotalInfo.nDeliveryTrxnNo = nTrxnNo;
					} else {
						if (Global.g_bIsPickup == false) {
							Global.gUARTotalInfo.bIsVisitedDelivery = true;
							Global.gUARTotalInfo.nDeliveryTrxnNo = nTrxnNo;
						} else {
							Global.gUARTotalInfo.bIsVisitedPickup = true;
							Global.gUARTotalInfo.nPickupTrxnNo = nTrxnNo;
						}
					}

					float dCurrentArAmount = arHeaderInfo.nTrxnAmount * 1.0f / Global.MULTIPLE_VALUE;

					UARPaymentInfo uarPaymentInfo = new UARPaymentInfo();
					uarPaymentInfo.nTrxnNo = nTrxnNo;
					uarPaymentInfo.nChainNo = Global.gCurCustomerInfo.nChainNo;
					uarPaymentInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
					if (arHeaderInfo.strDescription.isEmpty()) {
						uarPaymentInfo.strDescription = Global.g_strDocNo;
					}
					else {
						uarPaymentInfo.strDescription = arHeaderInfo.strDescription;
					}
					uarPaymentInfo.strTrxnDate = strTrxnDate;
					uarPaymentInfo.strInvoiceDate = arHeaderInfo.strInvDate;
					uarPaymentInfo.nPayType = Global.COLLECTION_CHEQUE;
					uarPaymentInfo.nTrxnAmount = dCurrentArAmount;


					Global.gUARTotalInfo.nTrxnNo = nTrxnNo;
					Global.gUARTotalInfo.nChainNo = Global.gCurCustomerInfo.nChainNo;
					Global.gUARTotalInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
					Global.gUARTotalInfo.strTrxnDate = strTrxnDate;
					Global.gUARTotalInfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(new Date(nTrxnNo));
					if(Global.gUARTotalInfo.strDocNo.equals("0"))
					{
						Global.gUARTotalInfo.strDocNo = Global.g_strDocNo;
					}

					dbAdapter.insertUARTotalInfo(Global.gUARTotalInfo);
					if(Global.gUOrderHInfo != null)
					{
						uarPaymentInfo.strDocNo = Global.gUOrderHInfo.strDocNo;
					}
					else
					{
						uarPaymentInfo.strDocNo = "0";
					}
					uarPaymentInfo.strDocNo = arHeaderInfo.strInvNo;
					uarPaymentInfo.strInvoiceNo = Global.gUARTotalInfo.strDocNo;
					dbAdapter.insertUARPaymentInfo(uarPaymentInfo);
					Global.gCurCustomerInfo.bCompleted = true;
					dbAdapter.updateCustomerInfo(Global.gCurCustomerInfo);
					nSeqNo++;
					Utils.setIntSetting(mContext, String.format(Locale.ENGLISH, Global.PREF_COLLECTION_SEQUENCE_NO, Global.gTripNumber), nSeqNo);

					long nPayTrxnNo = setGeneralUploadData("PAY", Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo, nTrxnNo, "", Global.g_strDocNo);

					TransactionInfo transactionInfo = new TransactionInfo();
					transactionInfo.strType = "PAY";
					transactionInfo.strTime = new SimpleDateFormat("kk:mm").format(new Date(nPayTrxnNo));
					transactionInfo.strCustomerName = Global.gCurCustomerInfo.strName;
					transactionInfo.strDocNo = Global.g_strDocNo;
					transactionInfo.strVoid = Global.g_bReverseMode ? "Yes" : "No";
					transactionInfo.value = Global.gUOrderHInfo.nSaleAmount + Global.gUOrderHInfo.nTaxAmount + Global.gUOrderHInfo.nHandlingFee + Global.gUOrderHInfo.nHandlingFeeTaxAmount;
					dbAdapter.insertTransactionHistoryInfo(transactionInfo);

					arHeaderInfo.nProcessStatus = 3;
					//dbAdapter.updateARHeaderInfo(arHeaderInfo);
					dbAdapter.deleteARHeaderInfo(arHeaderInfo);
					uCollectionsDInfo.nTrxnAmount += dCurrentArAmount;
				}
			}

			uCollectionsDInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
			uCollectionsDInfo.nChainNo = Global.gCurCustomerInfo.nChainNo;
			uCollectionsDInfo.strInvoiceNo = Global.g_strDocNo;
			uCollectionsDInfo.strCustomerName = Global.gCurCustomerInfo.strName;
			float nTrxnAmount = Utils.getFloatSetting(mContext, Global.COLLECTIONS_TODAY);
			nTrxnAmount += uCollectionsDInfo.nTrxnAmount;
			dbAdapter.insertUCollectionsDInfo(uCollectionsDInfo);
			Utils.setFloatSetting(mContext, Global.COLLECTIONS_TODAY, nTrxnAmount);
			dbAdapter.close();
		}
		else
		{
			long nTrxnNo = System.currentTimeMillis();
			//AddTrxnNo(Global.gCurCustomerInfo.nCustNo, Global.gCurCustomerInfo.nChainNo, nTrxnNo);

			String strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(mContext, new Date(nTrxnNo)));
			Global.gUARTotalInfo.nTrxnNo = nTrxnNo;
			Global.gUARTotalInfo.nChainNo = Global.gCurCustomerInfo.nChainNo;
			Global.gUARTotalInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
			Global.gUARTotalInfo.nInvoiceTrxnNo = Global.gUOrderHInfo.nTrxnNo;
			Global.gUARTotalInfo.nInvoiceAmount = Global.dCurrentArAmount;

			if (Global.gCurCustomerInfo.nInvoiceFmt == 1) {
				Global.gUARTotalInfo.bIsVisitedDelivery = true;
				Global.gUARTotalInfo.bIsVisitedPickup = true;
				Global.gUARTotalInfo.nDeliveryTrxnNo = nTrxnNo;
			} else {
				if (Global.g_bIsPickup == false) {
					Global.gUARTotalInfo.bIsVisitedDelivery = true;
					Global.gUARTotalInfo.nDeliveryTrxnNo = nTrxnNo;
				} else {
					Global.gUARTotalInfo.bIsVisitedPickup = true;
					Global.gUARTotalInfo.nPickupTrxnNo = nTrxnNo;
				}
			}

			UARPaymentInfo uarPaymentInfo = new UARPaymentInfo();
			uarPaymentInfo.nTrxnNo = nTrxnNo;
			uarPaymentInfo.nChainNo = Global.gCurCustomerInfo.nChainNo;
			uarPaymentInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
			uarPaymentInfo.strTrxnDate = strTrxnDate;
			uarPaymentInfo.nPayType = Global.COLLECTION_CHEQUE;
			uarPaymentInfo.nTrxnAmount = Global.dCurrentArAmount;

			Global.gUARTotalInfo.nTrxnNo = nTrxnNo;
			Global.gUARTotalInfo.nChainNo = Global.gCurCustomerInfo.nChainNo;
			Global.gUARTotalInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
			Global.gUARTotalInfo.strTrxnDate = strTrxnDate;
			Global.gUARTotalInfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(new Date(nTrxnNo));
//			Global.gUARTotalInfo.strDocNo = "0";
			Global.gUARTotalInfo.strReference = Global.gPaymentReference;
			DbAdapter dbAdapter = new DbAdapter(mContext);
			dbAdapter.open();
			dbAdapter.insertUARTotalInfo(Global.gUARTotalInfo);
			if(Global.gUOrderHInfo != null)
			{
				uarPaymentInfo.strDocNo = Global.gUOrderHInfo.strDocNo;
			}
			else
			{
				uarPaymentInfo.strDocNo = "0";
			}
			dbAdapter.insertUARPaymentInfo(uarPaymentInfo);
			Global.gCurCustomerInfo.bCompleted = true;
			dbAdapter.updateCustomerInfo(Global.gCurCustomerInfo);

			int nSeqNo = Utils.getIntSetting(mContext, String.format(Locale.ENGLISH, Global.PREF_COLLECTION_SEQUENCE_NO, Global.gTripNumber));
			nSeqNo++;
			Utils.setIntSetting(mContext, String.format(Locale.ENGLISH, Global.PREF_COLLECTION_SEQUENCE_NO, Global.gTripNumber), nSeqNo);
			long nPayTrxnNo = setGeneralUploadData("PAY", Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo, nTrxnNo, "", Global.g_strDocNo);

			TransactionInfo transactionInfo = new TransactionInfo();
			transactionInfo.strType = "PAY";
			transactionInfo.strTime = new SimpleDateFormat("kk:mm").format(new Date(nPayTrxnNo));
			transactionInfo.strCustomerName = Global.gCurCustomerInfo.strName;
			transactionInfo.strDocNo = Global.g_strDocNo;
			transactionInfo.strVoid = Global.g_bReverseMode ? "Yes" : "No";
			transactionInfo.value = Global.gUOrderHInfo.nSaleAmount + Global.gUOrderHInfo.nTaxAmount + Global.gUOrderHInfo.nHandlingFee + Global.gUOrderHInfo.nHandlingFeeTaxAmount;
			dbAdapter.insertTransactionHistoryInfo(transactionInfo);

			UCollectionsDInfo uCollectionsDInfo = new UCollectionsDInfo();
			uCollectionsDInfo.nTrxnNo = nTrxnNo;
			uCollectionsDInfo.nSeqNo = nSeqNo;
			uCollectionsDInfo.nPayType = Global.COLLECTION_CHEQUE;
			uCollectionsDInfo.strTrxnDate = strTrxnDate;

			int nCurrentArAmount = Math.round(Global.dCurrentArAmount * Global.MULTIPLE_VALUE);
			ARHeaderInfo arHeaderInfo = new ARHeaderInfo();
			arHeaderInfo.nProcessStatus = 1;
			arHeaderInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
			arHeaderInfo.nChainNo = Global.gCurCustomerInfo.nChainNo;
			arHeaderInfo.nTrxnAmount = nCurrentArAmount * (-1);
			arHeaderInfo.strInvNo = "0";
			arHeaderInfo.strInvDate = Global.gUARTotalInfo.strTrxnDate;
			dbAdapter.insertARHeaderInfo(arHeaderInfo);

			uCollectionsDInfo.nTrxnAmount = Global.dCurrentArAmount;
			uCollectionsDInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
			uCollectionsDInfo.nChainNo = Global.gCurCustomerInfo.nChainNo;
			uCollectionsDInfo.strCustomerName = Global.gCurCustomerInfo.strName;
			uCollectionsDInfo.strInvoiceNo = Global.g_strDocNo;
			float nTrxnAmount = Utils.getFloatSetting(mContext, Global.COLLECTIONS_TODAY);
			dbAdapter.insertUCollectionsDInfo(uCollectionsDInfo);
			nTrxnAmount += uCollectionsDInfo.nTrxnAmount;
			Utils.setFloatSetting(mContext, Global.COLLECTIONS_TODAY, nTrxnAmount);
			dbAdapter.close();
		}
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
			if (m_bAutoCollected && mCollectionInfo != null)
			{

			}
			else
			{
				Global.gUARPaymentChequeInfo.strBankNo = mStrBankNo;
				Global.gUARPaymentChequeInfo.strAccount = mStrAccount;
				Global.gUARPaymentChequeInfo.strCheckNo = mStrCheckNo;
				Global.gUARPaymentChequeInfo.strCheckDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(new Date(mDate));
				Global.gUARPaymentChequeInfo.strARNotes = mStrARNotes;
				Global.gUARPaymentChequeInfo.nTrxnAmount = mAmount;
			}
			bClickedDone = true;
			mContext.onBackPressed();
		}

	}

	public boolean m_bClicked = false;
	@Override
	public void onClick(View v) {
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
			case R.id.btnBack:
				if(m_bAutoCollected && mCollectionInfo != null)
				{
					//Utils.registerUploadFile(mContext, makeZipFileInfo);
					Utils.registerUploadFileAsTemp(mContext, makeZipFileInfo);
					makeZipFileInfo = null;
				}
				mContext.onBackPressed();
				break;
			case R.id.btnCompleted:
				doComplete();
				break;
		}
		m_bClicked = false;
	}

	private DatePickerDialog.OnDateSetListener dateListener = new DatePickerDialog.OnDateSetListener() {
		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			Date myDate = new Date(year-1900, monthOfYear, dayOfMonth, 0, 0, 0);
			mDate = myDate.getTime();
			showTimes();
		}
	};

	private void createPdf()
	{
		showProgressDialog(Str.CreatingPdf[Global.LANG]);
		new Thread()
		{
			@Override
			public void run() {
				mStrPdfFileName = getPDFFileName();
				Global.gUARTotalInfo.strDocNo = mStrPdfFileName.substring(0, mStrPdfFileName.length() - 4);
				Global.gUARTotalInfo.nTotalCheque = mAmount;
				Global.gUARPaymentChequeInfo.strBankNo = mStrBankNo;
				Global.gUARPaymentChequeInfo.strAccount = mStrAccount;
				Global.gUARPaymentChequeInfo.strCheckNo = mStrCheckNo;
				Global.gUARPaymentChequeInfo.strCheckDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(new Date(mDate));
				Global.gUARPaymentChequeInfo.strARNotes = mStrARNotes;
				Global.gUARPaymentChequeInfo.nTrxnAmount = mAmount;

				PrintEngine printEngine = new PrintEngine();
				mHoneywellPrintEngine = printEngine;

				printEngine.PrepareCashPrint(mContext, null);
				Utils.gDetlMap.clear();

				String strHoneywellFileName = Utils.getFinalPrintTemplateFileNameForHoneywell(mContext, Global.PRINT_RECEIPT_FILENAME);
				mHoneywellTemplatePath = Global.ROOT_PRINT_TEMPLATE_LOCCATION + strHoneywellFileName;
				printEngine.createDocument(mContext, false, Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + mStrPdfFileName, Global.CASH_PRINT);

				{
					DbAdapter dbAdapter = new DbAdapter(mContext);
					dbAdapter.open();
					PrintItem printItem = new PrintItem();
					printItem.nTrxnNo = Global.gUARTotalInfo.nTrxnNo;
					printItem.strDocNo = Global.gUARTotalInfo.strDocNo;
					printItem.strDocType = Global.gUARTotalInfo.strDocType;
					printItem.strFilePath = Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + mStrPdfFileName;
					printItem.nCustNo = Global.gCurCustomerInfo.nCustNo;
					printItem.nChainNo = Global.gCurCustomerInfo.nChainNo;
					printItem.strCustomerName = Global.gCurCustomerInfo.strName;
					printItem.nInvoiceAmount = Global.gUARTotalInfo.nTotalCheque;

					dbAdapter.insertPrint(printItem);
					dbAdapter.close();
				}


				String strDocNo = mStrPdfFileName.substring(0, mStrPdfFileName.length() - 4);

				long transactionNo = System.currentTimeMillis();
				m_lTrxnNo = transactionNo;
				//m_lTrxnNo = setGeneralUploadData("CASH", Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo, -1, "", strDocNo);

				UTransactionsInfo uTransactionsInfo = setUTransactionsInfo(Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo, transactionNo, "FARC", mStrPdfFileName);
				setUFileTransactionInfo(uTransactionsInfo.nTrxnNo, Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo,
						m_lTrxnNo, strDocNo, "RECEIPT", "PAYMENT RECEIPT",
						uTransactionsInfo.strTrxnDate, uTransactionsInfo.strTrxnTime, mStrPdfFileName);

				if (m_bAutoCollected && mCollectionInfo != null)
				{
					DbAdapter dbAdapter = new DbAdapter(mContext);
					dbAdapter.open();
					dbAdapter.beginTransacion();
					List<UARPaymentInfo> infos = dbAdapter.getUARPaymentList(Global.gUARTotalInfo.nTrxnNo);
					for(UARPaymentInfo info : infos)
					{
						info.strBankNo = mStrBankNo;
						info.strAccount = mStrAccount;
						info.strCheckNo = mStrCheckNo;
						info.strCheckDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(new Date(mDate));
						info.strARNotes = mStrARNotes;
						dbAdapter.updateUARPaymentInfo(info);
					}
					mCollectionInfo.strBankNo = mStrBankNo;
					mCollectionInfo.strAccount = mStrAccount;
					mCollectionInfo.strCheckNo = mStrCheckNo;
					mCollectionInfo.strCheckDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(new Date(mDate));
					dbAdapter.updateUCollectionsDInfo(mCollectionInfo);
					dbAdapter.endTransaction();
					dbAdapter.close();
				}
				else
				{
					Global.gUARPaymentChequeInfo.strBankNo = mStrBankNo;
					Global.gUARPaymentChequeInfo.strAccount = mStrAccount;
					Global.gUARPaymentChequeInfo.strCheckNo = mStrCheckNo;
					Global.gUARPaymentChequeInfo.strCheckDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(new Date(mDate));
					Global.gUARPaymentChequeInfo.strARNotes = mStrARNotes;
					Global.gUARPaymentChequeInfo.nTrxnAmount = mAmount;
				}

				//Utils.registerUploadFile(mContext, makeZipFileInfo);
				Utils.registerUploadFileAsTemp(mContext, makeZipFileInfo);
				makeZipFileInfo = null;
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
				});
			}
		}.start();
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
						Utils.setStringSetting(mContext, Global.PREF_PRINTER_MAC_ADDRESS, mStrMacAddress);
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
									discoveryHoneywellPrinters();
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

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				Looper.myLooper().quit();
			}
		}).start();
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

	/*	public Handler mHander = new Handler(){
            public void handleMessage(Message msg){

                    final MessageDialog msgDialog = new MessageDialog(mContext);
                    msgDialog.setCancelable(false);
                    msgDialog.mStrMessage = Str.TurnOnZebraPrinter[Global.LANG];
                    msgDialog.mStrMiddle = Str.Ok[Global.LANG];
                    msgDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }
                    });
                    msgDialog.show();
            }
        };*/
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

		try{
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
			hideProgressDialog();
			try
			{
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

	public class ListAdapter extends BaseAdapter implements Filterable {

		private List<String> originalList;
		private List<String> filteredList;
		private ItemFilter mFilter = new ItemFilter();

		public ListAdapter(Context context, List<String> data) {
			this.originalList = data;
			this.filteredList = data;
		}


		@Override
		public int getCount() {
			return filteredList.size();
		}

		@Override
		public String getItem(int i) {
			return filteredList.get(i);
		}

		@Override
		public long getItemId(int i) {
			return i;
		}

		@Override
		public View getView(int i, View view, ViewGroup viewGroup) {
			view = getLayoutInflater().inflate(R.layout.row_bank_code, viewGroup, false);
			String strItem = getItem(i);
			((TextView)view.findViewById(R.id.txtBankCode)).setText(strItem);
			return view;
		}

		@Override
		public Filter getFilter() {
			return mFilter;
		}

		private class ItemFilter extends Filter {
			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				List<String> resultList = new ArrayList<>();
				if (constraint != null && constraint.length() > 0) {
					String filterString = constraint.toString().toLowerCase();

					for (String strItemo : originalList) {
						if (strItemo.toLowerCase().contains(filterString)) {
							resultList.add(strItemo);
						}
					}
				} else {
					resultList = new ArrayList<>(originalList);
				}

				FilterResults results = new FilterResults();
				results.values = resultList;
				results.count = resultList.size();
				return results;
			}

			@Override
			protected void publishResults(CharSequence constraint, FilterResults results) {
				filteredList = (List<String>) results.values;
				notifyDataSetChanged();
			}
		}
	}
}