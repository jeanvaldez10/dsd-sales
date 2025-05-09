package dsd.dsdvansales.fragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.*;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.*;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.fragment.app.Fragment;

import com.mintwireless.mintmpos.integrator.utils.MTGAppSwitchResponse;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.Customer;
import com.stripe.model.PaymentSource;
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
import dsd.dsdvansales.activities.StripePaymentActivity;
import dsd.dsdvansales.activities.WindCaveActivity;
import dsd.dsdvansales.db.DbAdapter;
import dsd.dsdvansales.dialogs.EwayPaymentDialog;
import dsd.dsdvansales.dialogs.MessageDialog;
import dsd.dsdvansales.dialogs.MintCardProcDialog;
import dsd.dsdvansales.dialogs.MintPaymentDialog;
import dsd.dsdvansales.dialogs.StripeSavedCardSelectDialog;
import dsd.dsdvansales.dialogs.WindcaveStationIDDialog;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.HoneywellLinePrinter;
import dsd.dsdvansales.global.HoneywellPrinter;
import dsd.dsdvansales.global.PrintEngine;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.model.ARHeaderInfo;
import dsd.dsdvansales.model.CustomerAddInfo;
import dsd.dsdvansales.model.CustomerInfo;
import dsd.dsdvansales.model.PrintItem;
import dsd.dsdvansales.model.StatusInfo;
import dsd.dsdvansales.model.StripeSavedCard;
import dsd.dsdvansales.model.TransactionInfo;
import dsd.dsdvansales.model.UARPaymentInfo;
import dsd.dsdvansales.model.UCollectionsDInfo;
import dsd.dsdvansales.model.UTransactionsInfo;
import dsd.dsdvansales.model.UFileTransactionInfo;
import dsd.dsdvansales.views.AlertSingleChoiceAdapter;

public class PaymentCollectionFragment extends BaseFragment implements View.OnClickListener, MintCardProcDialog.Callback {

	private float mCollectionRequired = 0;
	private float mCash = 0;
	private float mCheque = 0;
	private float mCard = 0;
	private float mEFT = 0;
	private float mTotalCollected = 0;
	private float mTotalRemaining = 0;
	private EditText mEditCash = null;
	
	private boolean mIsRoundPrice = false;
	private boolean bLoadShowFirst = true;
	private ImageView mBtnDetail = null;
	boolean bCard = false;
	boolean bIsFromDeliverySummary = false;

	float mAmount = 0f;
	long mTrxnNo = -1;
	private String mStrPdfFileName = "";

	private List<String> mStrPhotoPathList = new ArrayList<>();

	private Connection printerConnection = null;
	private String mStrMacAddress = "";

	private int mIntPayMethod = 0;
	private boolean bLastProcess = false;

	public static int WIND_CAVE_API_CALL = 10020;
	public static int STRIPE_PAYMENT = 10021;
	private int windcavePayMethod = 0;

	private PrintEngine mHoneywellPrintEngine = null;
	private String mHoneywellTemplatePath = "";

	private boolean mShouldCashDisabled = false;
	private boolean mShouldChequeDisabled = false;
	private boolean mShouldCardDisabled = false;
	private boolean mShouldEFTDisabled = false;

	private DeliverySummaryFragment mDeliverySummaryFragment;
	private boolean bPartialPayment = false;
	private float fPartialPaidAmount = 0f;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootView = inflater.inflate(R.layout.fragment_payment_collection, container, false);
		commonInit();
		return mRootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		Global.gFragIdx = Global.FRAG_PAYMENT_COLLECTION;
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

	Handler mHandler = new Handler(){
		public void handleMessage(Message msg){
			if( msg.what == 0 )
			{
				mRootView.findViewById(R.id.layoutCard).performClick();
			}
			else if( msg.what == 1)
			{
				goBack();
			}
		}
	};

	public void setDeliverySummaryFragment(DeliverySummaryFragment deliverySummaryFragment) {
		this.mDeliverySummaryFragment = deliverySummaryFragment;
	}
	
	@Override
	public void initView() {
		mRootView.findViewById(R.id.layoutCheque).setOnClickListener(this);
		mRootView.findViewById(R.id.layoutEFT).setOnClickListener(this);
		if(Utils.getIntSetting(mContext, Global.PREF_EWAY_CARD_PROC) != 0)
		{
			mRootView.findViewById(R.id.layoutCard).setOnClickListener(this);
		}
		mRootView.findViewById(R.id.btnBack).setOnClickListener(this);
		mRootView.findViewById(R.id.btnCompleted).setOnClickListener(this);

		mEditCash = (EditText) mRootView.findViewById(R.id.editCash);
		mEditCash.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				if (mEditCash.getTag() == null) {
					setFormattedAmount(s.toString());
					refreshPaymentsLayout();
				}
			}
		});
		mEditCash.post(new Runnable() {
			public void run() {
				mEditCash.setSelection(mEditCash.getText().toString().length());
				mEditCash.postDelayed(this, 100);
			}
		});

		((TextView) mRootView.findViewById(R.id.txtCustomerName)).setText(Global.gCurCustomerInfo.strName);

		updatePaymentOptions();
		
//		if( (Global.gCurCustomerInfo.strPayType.trim().equals("1")|| Global.gCurCustomerInfo.strPayType.trim().equals("2")) &&
//				Utils.getStringSetting(mContext, Global.PREF_ROUTE_PAYMENTADJUST).trim().equals("1") )
//		{
//			mRootView.findViewById(R.id.btnBack).setVisibility(View.INVISIBLE);
//			mShouldCashDisabled = true;
//			enableCashLayout(false);
//			//mEditCash.setEnabled(false);
//		}
//		else
//		{
//			mRootView.findViewById(R.id.btnBack).setVisibility(View.VISIBLE);
//
//			//mRootView.findViewById(R.id.layoutCheque).setEnabled(true);
//			if(Utils.getIntSetting(mContext, Global.PREF_EWAY_CARD_PROC) != 0)
//			{
//				mShouldCardDisabled = false;
//				enableCardLayout(true);
//				// mRootView.findViewById(R.id.layoutCard).setEnabled(true);
//			}
//		}

		mBtnDetail = (ImageView)mRootView.findViewById(R.id.btnDetail);
		mBtnDetail.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				DetailCollectionFragment fragment = new DetailCollectionFragment();
				Bundle args = new Bundle();
				args.putBoolean(Global.EXTRA_IS_FROM_DELIVERY_SUMMARY, bIsFromDeliverySummary);
				fragment.setArguments(args);
				mContext.replaceFragment(fragment);
			}
		});

		bIsFromDeliverySummary = getArguments().getBoolean(Global.EXTRA_IS_FROM_DELIVERY_SUMMARY);
		
		loadData();
		refreshLanguage();
		
		bCard = getArguments().getBoolean(Global.EXTRA_CARD);
		if(bCard)
		{
			mHandler.sendEmptyMessage(1);
		}

		bLoadShowFirst = false;

		if(Utils.getIntSetting(mContext, Global.PREF_EWAY_CARD_PROC) == 0)
		{
			mRootView.findViewById(R.id.layoutCard).setVisibility(View.INVISIBLE);
		}
		else
		{
			mRootView.findViewById(R.id.layoutCard).setVisibility(View.VISIBLE);
		}

		// show/hide Photo button
		mRootView.findViewById(R.id.btnPhoto).setVisibility(View.GONE);
		if (Utils.getStringSetting(mContext, Global.PREF_ROUTE_PAYMENTADJUST).equals("2")) {
			if (Global.gCurCustomerInfo.strTerms.equals("COD")) {
				mRootView.findViewById(R.id.btnPhoto).setVisibility(View.VISIBLE);
			}
		}
		mRootView.findViewById(R.id.btnPhoto).setOnClickListener(this);
	}

	public void updatePaymentOptions() {

		String strPayType = Global.gCurCustomerInfo.strPayType;
		if (strPayType.equals("0")) {
			mShouldCashDisabled = false;
			mShouldChequeDisabled = false;
			mShouldCardDisabled = false;

			mShouldEFTDisabled = true;

			enableEFTLayout(false);
		}
		else if (strPayType.equals("1")) {
			mShouldCashDisabled = false;

			mShouldChequeDisabled = true;
			mShouldCardDisabled = true;
			mShouldEFTDisabled = true;

			enableChequeLayout(false);
			enableCardLayout(false);
			enableEFTLayout(false);
		}
		else if (strPayType.equals("2") || strPayType.equals("3") || strPayType.equals("4") || strPayType.equals("5")) {
			mShouldCashDisabled = false;
			mShouldChequeDisabled = false;
			mShouldCardDisabled = false;
			mShouldEFTDisabled = false;
		}
		else if (strPayType.equals("6") || strPayType.equals("B") || strPayType.equals("C") || strPayType.isEmpty()) {
			mShouldCashDisabled = true;
			mShouldChequeDisabled = true;
			mShouldCardDisabled = true;
			mShouldEFTDisabled = true;

			enableCashLayout(false);
			enableChequeLayout(false);
			enableCardLayout(false);
			enableEFTLayout(false);
		}
		else if (strPayType.equals("7")) {
			mShouldCardDisabled = false;

			mShouldChequeDisabled = true;
			mShouldCashDisabled = true;
			mShouldEFTDisabled = true;

			enableChequeLayout(false);
			enableCashLayout(false);
			enableEFTLayout(false);
		}
		else if (strPayType.equals("8")) {
			mShouldChequeDisabled = false;
			mShouldCardDisabled = false;
			mShouldEFTDisabled = false;

			mShouldCashDisabled = true;

			enableCashLayout(false);
		}
		else if (strPayType.equals("9")) {
			mShouldEFTDisabled = false;

			mShouldChequeDisabled = true;
			mShouldCardDisabled = true;
			mShouldCashDisabled = true;

			enableChequeLayout(false);
			enableCardLayout(false);
			enableCashLayout(false);
		}
		else if (strPayType.equals("$")) {
			mShouldCashDisabled = false;
			mShouldEFTDisabled = false;
			mShouldChequeDisabled = false;

			mShouldCardDisabled = true;

			enableCardLayout(false);
		}

	}

	@Override
	public void loadData() {
		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		List<ARHeaderInfo> arHeaderInfos = dbAdapter.getARHeaderInfo(Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo);

		float dTotalRemaining = 0;
		if (arHeaderInfos.size() > 0 && bIsFromDeliverySummary == false) {
			for (ARHeaderInfo arHeaderInfo : arHeaderInfos) {
				if (arHeaderInfo.nProcessStatus < 2)
					dTotalRemaining += arHeaderInfo.nTrxnAmount * 1.0 / Global.MULTIPLE_VALUE;
				else if (arHeaderInfo.nProcessStatus == 2) {
					if (bLoadShowFirst) {
						arHeaderInfo.nProcessStatus = 1;
						dbAdapter.updateARHeaderInfo(arHeaderInfo);
						dTotalRemaining += arHeaderInfo.nTrxnAmount * 1.0 / Global.MULTIPLE_VALUE;
					} else {
						if (Global.gCurCustomerInfo.strPayType.equals("$") ||
								Global.gCurCustomerInfo.strPayType.equals("1") ||
								Global.gCurCustomerInfo.strPayType.equals("2")) {
							arHeaderInfo.nProcessStatus = 1;
							dbAdapter.updateARHeaderInfo(arHeaderInfo);
							dTotalRemaining += arHeaderInfo.nTrxnAmount * 1.0 / Global.MULTIPLE_VALUE;
						}
					}
					/*
					if(!Global.gCurCustomerInfo.strPayType.equals("$") &&
							!Global.gCurCustomerInfo.strPayType.equals("1") &&
							!Global.gCurCustomerInfo.strPayType.equals("2"))
					{
						arHeaderInfo.nProcessStatus = 1;
						dbAdapter.updateARHeaderInfo(arHeaderInfo);
						dTotalRemaining += arHeaderInfo.nTrxnAmount * 1.0 / Global.MULTIPLE_VALUE;
					}*/
				}
			}
		}
		dbAdapter.close();

		if (bIsFromDeliverySummary) {
			if (Global.gCurHeaderInfo != null && Global.gCurHeaderInfo.nProcessStatus < 2) {
				mCollectionRequired = dTotalRemaining + getRoundedPrice(Global.gCurHeaderInfo.nTrxnAmount * 1.0f / Global.MULTIPLE_VALUE);
			} else {
				mCollectionRequired = dTotalRemaining;
			}
		}
		else {
			mCollectionRequired = dTotalRemaining;
		}

		mCash = Global.gUARTotalInfo.nTotalCash;
		mCheque = Global.gUARPaymentChequeInfo.nTrxnAmount;
		mCard = Global.gUARPaymentCardInfo.nTrxnAmount;
		mEFT = Global.gUARPaymentEFTInfo.nTrxnAmount;

		mTotalCollected = mCash + mCheque + mCard + mEFT;
		//mCollectionRequired += mTotalCollected;

		mTotalRemaining = mCollectionRequired - mTotalCollected;
		boolean bCentsRount;
		if (Utils.getStringSetting(mContext, Global.PREF_ROUTE_CENTSROUND).trim().equals("1") &&
				(Global.gCurCustomerInfo.strPayType.trim().equals("1") || Global.gCurCustomerInfo.strPayType.trim().equals("2"))) {
			bCentsRount = true;
		} else if (Utils.getStringSetting(mContext, Global.PREF_ROUTE_CENTSROUND).trim().equals("2")) {
			bCentsRount = true;
		} else {
			bCentsRount = false;
		}
		if (bCentsRount) {
			mIsRoundPrice = true;
		} else {
			mIsRoundPrice = false;
		}
		setCashAmount();
		refreshPage();
	}
	
	String getRoundedFormattedPrice(float value)
	{
		boolean bCentsRount;
		if( Utils.getStringSetting(mContext, Global.PREF_ROUTE_CENTSROUND).trim().equals("1") &&
				(Global.gCurCustomerInfo.strPayType.trim().equals("1") || Global.gCurCustomerInfo.strPayType.trim().equals("2")))
		{
			bCentsRount = true;
		}
		else if(Utils.getStringSetting(mContext, Global.PREF_ROUTE_CENTSROUND).trim().equals("2"))
		{
			bCentsRount = true;
		}
		else
		{
			bCentsRount = false;
		}
		if(bCentsRount)
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
			nTotal = ((nTotal / 1000) + ((nOffSet >= 500) ? 1 : 0)) * 1000;  
			nOffSet = nInvoiceTotal - nTotal;

			nInvoiceTotal *= fNeg;
			nOffSet *= fNeg;

			float dValue = nInvoiceTotal / (Global.MULTIPLE_VALUE * 1f);
			String sValue = String.format(Locale.ENGLISH, "%s" + "%." + Utils.getIntSetting(mContext, Global.PREF_DECIMAL) + "f", Global.gCurrencySymbol, dValue);
			return sValue;
		}
		else
		{
			return String.format(Locale.ENGLISH, "%s" + "%." + Utils.getIntSetting(mContext, Global.PREF_DECIMAL) + "f", Global.gCurrencySymbol, value);
		}
		
	}
	
	float getRoundedPrice(float value)
	{
		boolean bCentsRount;
		if( Utils.getStringSetting(mContext, Global.PREF_ROUTE_CENTSROUND).trim().equals("1") &&
				(Global.gCurCustomerInfo.strPayType.trim().equals("1") || Global.gCurCustomerInfo.strPayType.trim().equals("2")))
		{
			bCentsRount = true;
		}
		else if(Utils.getStringSetting(mContext, Global.PREF_ROUTE_CENTSROUND).trim().equals("2"))
		{
			bCentsRount = true;
		}
		else
		{
			bCentsRount = false;
		}
		if(bCentsRount)
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
			nTotal = ((nTotal / 1000) + ((nOffSet >= 500) ? 1 : 0)) * 1000;  
			nOffSet = nInvoiceTotal - nTotal;

			nInvoiceTotal *= fNeg;
			nOffSet *= fNeg;
			float dValue = nInvoiceTotal / (Global.MULTIPLE_VALUE * 1f);
			return dValue;
		}
		else
		{
			return value;
		}
	}
	
	@Override
	public void refreshPage() {
		((TextView) mRootView.findViewById(R.id.txtCollectionRequired)).setText(getRoundedFormattedPrice(mCollectionRequired));
		((TextView) mRootView.findViewById(R.id.txtCheque)).setText(getRoundedFormattedPrice(mCheque));
		((TextView) mRootView.findViewById(R.id.txtCard)).setText(getRoundedFormattedPrice(mCard));
		((TextView) mRootView.findViewById(R.id.txtEFT)).setText(getRoundedFormattedPrice(mEFT));
		((TextView) mRootView.findViewById(R.id.txtTotalCollected)).setText(getRoundedFormattedPrice(mTotalCollected));
		((TextView) mRootView.findViewById(R.id.txtTotalRemaining)).setText(getRoundedFormattedPrice(mTotalRemaining));
		((TextView) mRootView.findViewById(R.id.lblCurrencySymbol)).setText(Global.gCurrencySymbol);

		refreshPaymentsLayout();
	}

	@Override
	public void refreshLanguage() {
		((TextView) mRootView.findViewById(R.id.lblTitle)).setText(Str.PaymentCollection[Global.LANG].toUpperCase());
		((TextView) mRootView.findViewById(R.id.lblCollectionRequired)).setText(Str.CollectionRequired[Global.LANG]);
		((TextView) mRootView.findViewById(R.id.lblCash)).setText(Str.Cash[Global.LANG]);
		((TextView) mRootView.findViewById(R.id.lblCheque)).setText(Str.Cheque[Global.LANG]);
		((TextView) mRootView.findViewById(R.id.lblCard)).setText(Str.Card[Global.LANG]);
		((TextView) mRootView.findViewById(R.id.lblEFT)).setText(Str.EFT[Global.LANG]);
		((TextView) mRootView.findViewById(R.id.lblTotalCollected)).setText(Str.TotalCollected[Global.LANG]);
		((TextView) mRootView.findViewById(R.id.lblTotalRemaining)).setText(Str.TotalRemaining[Global.LANG]);
		((Button) mRootView.findViewById(R.id.btnCompleted)).setText(Str.Done[Global.LANG]);
	}

	private void setFormattedAmount(String strVal) {
		String strArray[] = strVal.split("\\.");
		strVal = "";
		for (int i=0; i<strArray.length; i++) {
			strVal += strArray[i];
		}
		//mCash = Utils.parseFloatFromString(strVal) / 100f;
		int nValue = Utils.getIntSetting(mContext, Global.PREF_DECIMAL);
		mCash = Utils.parseFloatFromString(strVal) / (float)(Math.pow(10,nValue));
		mTotalCollected = mCash + mCheque + mCard + mEFT;
		mTotalRemaining = mCollectionRequired - mTotalCollected;
		((TextView) mRootView.findViewById(R.id.txtTotalCollected)).setText(getRoundedFormattedPrice(mTotalCollected));
		((TextView) mRootView.findViewById(R.id.txtTotalRemaining)).setText(getRoundedFormattedPrice(mTotalRemaining));

		setCashAmount1();
	}

	private void setCashAmount1() {
		mEditCash.setTag("From App");
		String strCash = String.format(Locale.ENGLISH, "%s" + "%." + Utils.getIntSetting(mContext, Global.PREF_DECIMAL) + "f", Global.gCurrencySymbol, mCash);
		mEditCash.setText(strCash.substring(Global.gCurrencySymbol.length()));
		mEditCash.setSelection(mEditCash.getText().toString().length());
		mEditCash.setTag(null);
	}
	
	private void setCashAmount() {
		mEditCash.setTag("From App");
		String strCash = getRoundedFormattedPrice(mCash);
		mEditCash.setText(strCash.substring(Global.gCurrencySymbol.length()));
		mEditCash.setSelection(mEditCash.getText().toString().length());
		mEditCash.setTag(null);
	}

	private void doComplete() {
		// check if COD customer has one or more photos
		boolean bShouldHavePhoto = false;
		if (Utils.getStringSetting(mContext, Global.PREF_ROUTE_PAYMENTADJUST).equals("2")) {
			if (Global.gCurCustomerInfo.strTerms.equals("COD")) {
				if (mCash > 0 || mCheque > 0 || mEFT > 0) {
					bShouldHavePhoto = true;
				}
			}
		}

		if (bShouldHavePhoto && mStrPhotoPathList.isEmpty()) {
			try {
				if(mContext.isFinishing() || mContext.isDestroyed()) return;
				MessageDialog msgDialog = new MessageDialog(mContext);
				msgDialog.setCancelable(false);
				msgDialog.mStrTitle = Global.gCurCustomerInfo.strName;
				msgDialog.mStrMessage = Str.You_must_capture_an_image_before_being_able_to_proceed[Global.LANG];
				msgDialog.mStrMiddle = Str.Ok[Global.LANG];
				msgDialog.show();
			}catch (Exception e)
			{
				e.printStackTrace();
			}
			return;
		}

		// Do not allow overpayment for PayType = 3
		if (Global.gCurCustomerInfo.strPayType.equals("3")) {
			mCash = Utils.parseFloatFromString(mEditCash.getText().toString());
			mTotalCollected = mCash + mCheque + mCard + mEFT;
			mTotalRemaining = mCollectionRequired - mTotalCollected;
			if (mTotalRemaining <= -0.01) {
				try {
					if(mContext.isFinishing() || mContext.isDestroyed()) return;
					MessageDialog msgDialog = new MessageDialog(mContext);
					msgDialog.setCancelable(false);
					msgDialog.mStrMessage = Str.NoOverCollectionAllowed[Global.LANG];
					msgDialog.mStrMiddle = Str.Return[Global.LANG];
					msgDialog.setOnDismissListener(dialog -> {
					});
					msgDialog.show();
				}catch (Exception e)
				{
					e.printStackTrace();
				}
				return;
			}
		}

		// Check credit limit
		if (bIsFromDeliverySummary) {
			if (Global.gCurCustomerInfo.strPayType.equals("3")) {
				DbAdapter dbAdapter = new DbAdapter(mContext);
				dbAdapter.open();
				float creditLimit = 0;
				int routeNumber = Utils.getIntFromString(Global.gRouteNumber);
				try {
					routeNumber = Integer.parseInt(Global.gRouteNumber);
				} catch (Exception ex) {

				}
				CustomerInfo custInfo = dbAdapter.getCustomerInfo(routeNumber, 99999);
				if (custInfo == null) {
					creditLimit = 0;
				}
				else {
					int nCreditLimit = Utils.getIntFromString(custInfo.strCreditLimit);
					if (nCreditLimit == 0) {
						creditLimit = 0;
					}
					else {
						creditLimit = (float)nCreditLimit;
					}
				}
				dbAdapter.close();

				boolean bWithinCreditLimit = false;
				if (creditLimit == 0) {
					bWithinCreditLimit = true;
				}
				else {
					bWithinCreditLimit = mCollectionRequired <= creditLimit;
				}

				if (bWithinCreditLimit) {
					continueCompleteProcess();
				}
				else {
					try {
						if(mContext.isFinishing() || mContext.isDestroyed()) return;
						MessageDialog msgDialog = new MessageDialog(mContext);
						msgDialog.setCancelable(false);
						msgDialog.mStrMessage = String.format(Locale.ENGLISH, Str.NoEnoughCreditToAllow[Global.LANG]);
						msgDialog.mStrMiddle = Str.Return[Global.LANG];
						msgDialog.setOnDismissListener(dialog -> {
						});
						msgDialog.show();
					}catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}
		}
		else {
			continueCompleteProcess();
		}
	}

	private void continueCompleteProcess() {

		if (mDeliverySummaryFragment != null) {
			mDeliverySummaryFragment.setCodPaymentOptionSelected(true);
		}

		mCash = Utils.parseFloatFromString(mEditCash.getText().toString());
		mTotalCollected = mCash + mCheque + mCard + mEFT;
		mTotalRemaining = mCollectionRequired - mTotalCollected;
		if (Math.abs(getRoundedPrice(mTotalRemaining)) < 0.01 || mTotalRemaining < -0.01) {
			//goBack();
			String strTemplateFileName = Utils.getFinalPrintTemplateFileName(mContext, Global.PRINT_RECEIPT_FILENAME);
			if(Utils.isFileExist(Global.ROOT_LOCATION + "/" + strTemplateFileName))
			{
				createPdf();
			}
			else
			{
				doLastProcess();
				goBack();
			}
		} else {
			String strMessage;
			if (mTotalRemaining > 0) {
				strMessage = Str.FullAmountNotCollected_small_msg[Global.LANG];
			} else {
				strMessage = Str.FullAmountNotCollected_big_msg[Global.LANG];
			}
			try {
				final MessageDialog msgDialog = new MessageDialog(mContext);
				msgDialog.setCancelable(false);
				msgDialog.mStrTitle = Str.FullAmountNotCollected[Global.LANG];
				msgDialog.mStrMessage = strMessage;
				msgDialog.mStrLeft = Str.Return[Global.LANG];
				msgDialog.mStrRight = Str.Proceed[Global.LANG];
				msgDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
							//goBack();
							String strTemplateFileName = Utils.getFinalPrintTemplateFileName(mContext, Global.PRINT_RECEIPT_FILENAME);
							if(Utils.isFileExist(Global.ROOT_LOCATION + "/" + strTemplateFileName))
							{
								createPdf();
							}
							else
							{
								doLastProcess();
								goBack();
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
	}

	private void doLastProcess()
	{
		if(bLastProcess) return;
		bLastProcess = true;
		if( mTotalCollected >= 0.01 && !bCard)
		{
			Global.gUARTotalInfo.nTotalCash = mCash;
			Global.gUARTotalInfo.nTotalCheque = mCheque + mCard + mEFT;
			DbAdapter dbAdapter = new DbAdapter(mContext);
			dbAdapter.open();
			StatusInfo info = dbAdapter.getStatusInfo(Global.gCurCustomerInfo.nCustNo);
			dbAdapter.close();
			try
			{
				if (mStrPhotoPathList.size() > 0)
				{
					int nIdx = 1;
					for(String strPhotoPath : mStrPhotoPathList)
					{
						UFileTransactionInfo uFileTransactionInfo = new UFileTransactionInfo();
						String strRef = setUCameraData("CAM", strPhotoPath, "", uFileTransactionInfo, nIdx++, false);
						{
							setUImageFileTransactionInfo(Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo,  uFileTransactionInfo.nFileTrxnNo, "", "PAYMENTCOLLECTION", "PAYMENTCOLLECTION",
									uFileTransactionInfo.strFileCreateDate, uFileTransactionInfo.strFileCreateTime, strRef);
						}
					}

				}

				boolean bFullPayment =  false;
				if (Math.abs(getRoundedPrice(mTotalRemaining)) < 0.01 || mTotalRemaining < -0.01) {
					bFullPayment = true;
				}
				this.bPartialPayment = !bFullPayment;
				if( bFullPayment )
				{
					dbAdapter.open();
					List<ARHeaderInfo> infos = dbAdapter.getARHeaderInfo(Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo);
					long nInitialTrxnNo;
					if(mTrxnNo == -1)
						nInitialTrxnNo = System.currentTimeMillis();
					else
						nInitialTrxnNo = mTrxnNo;

					Global.gUARTotalInfo.nInvoiceTrxnNo = nInitialTrxnNo;
					Global.gUARTotalInfo.nInvoiceAmount = mTotalCollected;
					Global.gUARTotalInfo.nTrxnNo = nInitialTrxnNo;
					Global.gUARTotalInfo.nChainNo = Global.gCurCustomerInfo.nChainNo;
					Global.gUARTotalInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
					//Global.gUARTotalInfo.strTrxnDate = strTrxnDate;
					Global.gUARTotalInfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(new Date());
					Global.gUARTotalInfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(new Date());

					boolean bFirstPayment = true;
					for(ARHeaderInfo arHeaderInfo : infos)
					{
						if( arHeaderInfo.nProcessStatus < 2 )
						{
							long nTrxnNo;
							if(mTrxnNo == -1) {
								nTrxnNo = nInitialTrxnNo;
//								if (bFirstPayment) {
//									nTrxnNo = nInitialTrxnNo;
//									bFirstPayment = false;
//								}
//								else {
//									nTrxnNo = System.currentTimeMillis();
//								}
							}
							else
								nTrxnNo = mTrxnNo;
							String strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(new Date());

							//Global.gUARTotalInfo.strDocNo = "";
/*							if (Global.gCurCustomerInfo.nInvoiceFmt > 0) {
								Global.gUARTotalInfo.bIsVisitedDelivery = true;
								Global.gUARTotalInfo.bIsVisitedPickup = true;
								Global.gUARTotalInfo.nDeliveryTrxnNo = 0;
							}*/

							float dCurrentArAmount = arHeaderInfo.nTrxnAmount * 1.0f / Global.MULTIPLE_VALUE;

							UARPaymentInfo uarPaymentInfo = new UARPaymentInfo();
							uarPaymentInfo.nTrxnNo = nTrxnNo;
							uarPaymentInfo.nChainNo = Global.gCurCustomerInfo.nChainNo;
							uarPaymentInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
							if (arHeaderInfo.strDescription.isEmpty()) {
								uarPaymentInfo.strDescription = Global.gUARTotalInfo.strDocNo;
							}
							else {
								uarPaymentInfo.strDescription = arHeaderInfo.strDescription;
							}
							uarPaymentInfo.strTrxnDate = strTrxnDate;
							uarPaymentInfo.strInvoiceDate = arHeaderInfo.strInvDate;
							if (mCard == 0 && mCash != 0 && mCheque == 0 && mEFT == 0) {
								uarPaymentInfo.nPayType = Global.COLLECTION_CASH;
								uarPaymentInfo.nTrxnAmount = dCurrentArAmount;
							}
							else if (mCard == 0 && mCash == 0 && mCheque != 0 && mEFT == 0) {
								uarPaymentInfo.nPayType = Global.COLLECTION_CHEQUE;

								uarPaymentInfo.strBankNo = Global.gUARPaymentChequeInfo.strBankNo;
								uarPaymentInfo.strAccount = Global.gUARPaymentChequeInfo.strAccount;
								uarPaymentInfo.strCheckNo = Global.gUARPaymentChequeInfo.strCheckNo;
								uarPaymentInfo.strCheckDate = Global.gUARPaymentChequeInfo.strCheckDate;
								uarPaymentInfo.strARNotes = Global.gUARPaymentChequeInfo.strARNotes;
								//uarPaymentInfo.nTrxnAmount = Global.gUARPaymentChequeInfo.nTrxnAmount;
								uarPaymentInfo.nTrxnAmount = dCurrentArAmount;
							}
							else if (mCard != 0 && mCash == 0 && mCheque == 0 && mEFT == 0) {
								uarPaymentInfo.nPayType = Global.COLLECTION_CARD;
								uarPaymentInfo.nTrxnAmount = dCurrentArAmount;
							}
							else if (mCard == 0 && mCash == 0 && mCheque == 0 && mEFT != 0) {
								uarPaymentInfo.nPayType = Global.COLLECTION_EFT;
								uarPaymentInfo.strCheckDate = Global.gUARPaymentEFTInfo.strCheckDate;
								uarPaymentInfo.strARNotes = Global.gUARPaymentEFTInfo.strARNotes;
								//uarPaymentInfo.nTrxnAmount = Global.gUARPaymentChequeInfo.nTrxnAmount;
								uarPaymentInfo.nTrxnAmount = dCurrentArAmount;
							} else {
								uarPaymentInfo.nPayType = 0;
								uarPaymentInfo.nTrxnAmount = dCurrentArAmount;
							}

							uarPaymentInfo.strDocNo = arHeaderInfo.strInvNo;
							uarPaymentInfo.strInvoiceNo = Global.gUARTotalInfo.strDocNo;
							uarPaymentInfo.nIdx = (int)dbAdapter.insertUARPaymentInfo(uarPaymentInfo);

							if (Global.gCurCustomerInfo.strPayType.equals("3") && mDeliverySummaryFragment != null) {
								mDeliverySummaryFragment.addCollectionPaymentInfo(uarPaymentInfo);
							}

//							Global.gUARTotalInfo.strDocNo = arHeaderInfo.strInvNo;
							//setUTransactionsInfo(Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo, nTrxnNo, "PDF", Global.gUARTotalInfo.strDocNo);


//							if(Global.gUOrderHInfo != null)
//							{
//								uarPaymentInfo.strDocNo = Global.gUOrderHInfo.strDocNo;
//							}
//							else
//							{
//								uarPaymentInfo.strDocNo = "0";
//							}

//							Global.gCurCustomerInfo.bCompleted = true;
//							dbAdapter.updateCustomerInfo(Global.gCurCustomerInfo);

							int nSeqNo = Utils.getIntSetting(mContext, String.format(Locale.ENGLISH, Global.PREF_COLLECTION_SEQUENCE_NO, Global.gTripNumber));
							nSeqNo++;
							Utils.setIntSetting(mContext, String.format(Locale.ENGLISH, Global.PREF_COLLECTION_SEQUENCE_NO, Global.gTripNumber), nSeqNo);
							long nPayTrxnNo = setGeneralUploadData("PAY", Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo, nTrxnNo);

							TransactionInfo transactionInfo = new TransactionInfo();
							transactionInfo.strType = "PAY";
							transactionInfo.strDocNo = Global.gUARTotalInfo.strDocNo;
							transactionInfo.value = uarPaymentInfo.nTrxnAmount;
							transactionInfo.strCustomerName = Global.gCurCustomerInfo.strName;
							transactionInfo.strTime = new SimpleDateFormat("kk:mm").format(new Date(nPayTrxnNo));

							dbAdapter.insertTransactionHistoryInfo(transactionInfo);

							UCollectionsDInfo uCollectionsDInfo = new UCollectionsDInfo();
							uCollectionsDInfo.nTrxnNo = nTrxnNo;
							uCollectionsDInfo.nSeqNo = nSeqNo;
							if (mCard == 0 && mCash != 0 && mCheque == 0 && mEFT == 0) {
								uCollectionsDInfo.nPayType = Global.COLLECTION_CASH;
							}
							else if (mCard == 0 && mCash == 0 && mCheque != 0 && mEFT == 0) {
								uCollectionsDInfo.nPayType = Global.COLLECTION_CHEQUE;
							}
							else if (mCard != 0 && mCash == 0 && mCheque == 0 && mEFT == 0) {
								uCollectionsDInfo.nPayType = Global.COLLECTION_CARD;
							} else if (mCard == 0 && mCash == 0 && mCheque == 0 && mEFT != 0) {
								uCollectionsDInfo.nPayType = Global.COLLECTION_EFT;
							} else {
								uCollectionsDInfo.nPayType = 0;
							}
							uCollectionsDInfo.strTrxnDate = strTrxnDate;

							arHeaderInfo.nProcessStatus = 3;
							dbAdapter.updateARHeaderInfo(arHeaderInfo);

							if (Global.gCurCustomerInfo.strPayType.equals("3") && mDeliverySummaryFragment != null) {
								mDeliverySummaryFragment.addCollectionHeaderInfo(arHeaderInfo);
							}

							uCollectionsDInfo.nTrxnAmount = dCurrentArAmount;
							uCollectionsDInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
							uCollectionsDInfo.nChainNo = Global.gCurCustomerInfo.nChainNo;
							uCollectionsDInfo.strCustomerName = Global.gCurCustomerInfo.strName;
							uCollectionsDInfo.strInvoiceNo = Global.gUARTotalInfo.strDocNo;
							float nTrxnAmount = Utils.getFloatSetting(mContext, Global.COLLECTIONS_TODAY);
							dbAdapter.insertUCollectionsDInfo(uCollectionsDInfo);
							nTrxnAmount += uCollectionsDInfo.nTrxnAmount;
							Utils.setFloatSetting(mContext, Global.COLLECTIONS_TODAY, nTrxnAmount);
							//Global.gUploadCustNoList.add(makeZipFileInfo);
							//Utils.registerUploadFile(mContext, makeZipFileInfo);
							Utils.registerUploadFileAsTemp(mContext, makeZipFileInfo);
						}
					}
					dbAdapter.insertUARTotalInfo(Global.gUARTotalInfo);

					dbAdapter.deleteProcessedARHeader(Global.gCurCustomerInfo.nCustNo, Global.gCurCustomerInfo.nChainNo);
					infos = dbAdapter.getARHeaderInfo(Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo);
					for(ARHeaderInfo arHeaderInfo : infos)
					{
						if(arHeaderInfo.nProcessStatus < 2)
						{
							if(Global.gCurCustomerInfo.strPayType.equals("3") || Global.gCurCustomerInfo.strPayType.equals("4") || Global.gCurCustomerInfo.strPayType.equals("5") || Global.gCurCustomerInfo.strPayType.equals("C"))
							{
								arHeaderInfo.nProcessStatus = 2;
							}
							else
							{
								{
									arHeaderInfo.nProcessStatus = 0;
								}
							}
						}

						dbAdapter.updateARHeaderInfo(arHeaderInfo);
					}
					dbAdapter.close();
				}
				else
				{
					// get the selected invoice
					String strSelectedInvNo = "";
					if (Global.gCurHeaderInfo == null) {
						dbAdapter.open();
						List<ARHeaderInfo> infos = dbAdapter.getARHeaderInfo(Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo);
						for (ARHeaderInfo headerInfo: infos) {
							if( headerInfo.nProcessStatus < 2 ) {
								if (headerInfo.strARTrxnType.equals("INV") && !headerInfo.strInvNo.isEmpty()) {
									strSelectedInvNo = headerInfo.strInvNo;
									break;
								}
							}
						}
						dbAdapter.close();
					}

					String strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(mContext, new Date()));
					long nTrxnNo;
					if(mTrxnNo == -1)
						nTrxnNo = System.currentTimeMillis();
					else
						nTrxnNo = mTrxnNo;
					Global.gUARPaymentCardInfo.nTrxnNo = nTrxnNo;
					Global.gUARPaymentCardInfo.nChainNo = Global.gCurCustomerInfo.nChainNo;
					Global.gUARPaymentCardInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
					Global.gUARPaymentCardInfo.strTrxnDate = strTrxnDate;
					Global.gUARPaymentCardInfo.nPayType = Global.COLLECTION_CARD;

					Global.gUARPaymentChequeInfo.nTrxnNo = nTrxnNo;
					Global.gUARPaymentChequeInfo.nChainNo = Global.gCurCustomerInfo.nChainNo;
					Global.gUARPaymentChequeInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
					Global.gUARPaymentChequeInfo.strTrxnDate = strTrxnDate;
					Global.gUARPaymentChequeInfo.nPayType = Global.COLLECTION_CHEQUE;

					Global.gUARPaymentEFTInfo.nTrxnNo = nTrxnNo;
					Global.gUARPaymentEFTInfo.nChainNo = Global.gCurCustomerInfo.nChainNo;
					Global.gUARPaymentEFTInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
					Global.gUARPaymentEFTInfo.strTrxnDate = strTrxnDate;
					Global.gUARPaymentEFTInfo.nPayType = Global.COLLECTION_EFT;

					Global.gUARTotalInfo.nTrxnNo = nTrxnNo;
					Global.gUARTotalInfo.nChainNo = Global.gCurCustomerInfo.nChainNo;
					Global.gUARTotalInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
					Global.gUARTotalInfo.strTrxnDate = strTrxnDate;
					Global.gUARTotalInfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(new Date(nTrxnNo));

					Global.gUARTotalInfo.nInvoiceAmount = Global.gUARPaymentChequeInfo.nTrxnAmount + Global.gUARTotalInfo.nTotalCash + Global.gUARPaymentCardInfo.nTrxnAmount + Global.gUARPaymentEFTInfo.nTrxnAmount;

					ARHeaderInfo arHeaderInfo = new ARHeaderInfo();
					arHeaderInfo.nProcessStatus = 0;
					arHeaderInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
					arHeaderInfo.nChainNo = Global.gCurCustomerInfo.nChainNo;
					arHeaderInfo.nTrxnAmount = Math.round(Global.gUARTotalInfo.nInvoiceAmount * (-1) * Global.MULTIPLE_VALUE);
					if (Global.gCurCustomerInfo.strPayType.equals("3")) {
						arHeaderInfo.strInvNo =  Global.gUARTotalInfo.strDocNo;
					}
					else {
						arHeaderInfo.strInvNo = "0";
					}
					arHeaderInfo.strInvDate = Global.gUARTotalInfo.strTrxnDate;
					arHeaderInfo.strARTrxnType = "PAY";
					arHeaderInfo.nIdx = (int)dbAdapter.insertARHeaderInfo(arHeaderInfo);

					if (Global.gCurCustomerInfo.strPayType.equals("3") && mDeliverySummaryFragment != null) {
						mDeliverySummaryFragment.addCollectionHeaderInfo(arHeaderInfo);
					}

					int nSeqNo = Utils.getIntSetting(mContext, String.format(Locale.ENGLISH, Global.PREF_COLLECTION_SEQUENCE_NO, Global.gTripNumber));
					// Cheque
					UCollectionsDInfo uCollectionsDChequeInfo = null;
					if (Global.gUARPaymentChequeInfo.nTrxnAmount > 0) {
						nSeqNo++;
						uCollectionsDChequeInfo = new UCollectionsDInfo();
						uCollectionsDChequeInfo.nTrxnNo = nTrxnNo;
						uCollectionsDChequeInfo.nSeqNo = nSeqNo;
						uCollectionsDChequeInfo.nPayType = Global.COLLECTION_CHEQUE;
						uCollectionsDChequeInfo.strTrxnDate = strTrxnDate;
						uCollectionsDChequeInfo.nTrxnAmount = Global.gUARPaymentChequeInfo.nTrxnAmount;
						uCollectionsDChequeInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
						uCollectionsDChequeInfo.nChainNo = Global.gCurCustomerInfo.nChainNo;
						uCollectionsDChequeInfo.strCustomerName = Global.gCurCustomerInfo.strName;
						uCollectionsDChequeInfo.strBankNo = Global.gUARPaymentChequeInfo.strBankNo;
						uCollectionsDChequeInfo.strAccount = Global.gUARPaymentChequeInfo.strAccount;
						uCollectionsDChequeInfo.strCheckNo = Global.gUARPaymentChequeInfo.strCheckNo;
						uCollectionsDChequeInfo.strCheckDate = Global.gUARPaymentChequeInfo.strCheckDate;
					}

					// Cash
					UCollectionsDInfo uCollectionsDCashInfo = null;
					if (Global.gUARTotalInfo.nTotalCash > 0) {
						nSeqNo++;
						uCollectionsDCashInfo = new UCollectionsDInfo();
						uCollectionsDCashInfo.nTrxnNo = nTrxnNo;
						uCollectionsDCashInfo.nSeqNo = nSeqNo;
						uCollectionsDCashInfo.nPayType = Global.COLLECTION_CASH;
						uCollectionsDCashInfo.strTrxnDate = strTrxnDate;
						uCollectionsDCashInfo.nTrxnAmount = Global.gUARTotalInfo.nTotalCash;
						uCollectionsDCashInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
						uCollectionsDCashInfo.nChainNo = Global.gCurCustomerInfo.nChainNo;
						uCollectionsDCashInfo.strCustomerName = Global.gCurCustomerInfo.strName;
					}

					// EFT
					UCollectionsDInfo uCollectionsDEFTInfo = null;
					if (Global.gUARPaymentEFTInfo.nTrxnAmount > 0) {
						nSeqNo++;
						uCollectionsDEFTInfo = new UCollectionsDInfo();
						uCollectionsDEFTInfo.nTrxnNo = nTrxnNo;
						uCollectionsDEFTInfo.nSeqNo = nSeqNo;
						uCollectionsDEFTInfo.nPayType = Global.COLLECTION_EFT;
						uCollectionsDEFTInfo.strTrxnDate = strTrxnDate;
						uCollectionsDEFTInfo.nTrxnAmount = Global.gUARPaymentEFTInfo.nTrxnAmount;
						uCollectionsDEFTInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
						uCollectionsDEFTInfo.nChainNo = Global.gCurCustomerInfo.nChainNo;
						uCollectionsDEFTInfo.strCustomerName = Global.gCurCustomerInfo.strName;
						uCollectionsDEFTInfo.strCheckDate = Global.gUARPaymentEFTInfo.strCheckDate;
					}

					// Card
					UCollectionsDInfo uCollectionsDCardInfo = null;
					if (Global.gUARPaymentCardInfo.nTrxnAmount > 0) {
						nSeqNo++;
						uCollectionsDCardInfo = new UCollectionsDInfo();
						uCollectionsDCardInfo.nTrxnNo = nTrxnNo;
						uCollectionsDCardInfo.nSeqNo = nSeqNo;
						uCollectionsDCardInfo.nPayType = Global.COLLECTION_CARD;
						uCollectionsDCardInfo.strTrxnDate = strTrxnDate;
						uCollectionsDCardInfo.nTrxnAmount = Global.gUARPaymentCardInfo.nTrxnAmount;
						uCollectionsDCardInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
						uCollectionsDCardInfo.nChainNo = Global.gCurCustomerInfo.nChainNo;
						uCollectionsDCardInfo.strCustomerName = Global.gCurCustomerInfo.strName;
					}

					// Total Cash
					UARPaymentInfo uARPaymentCashInfo = null;
					this.fPartialPaidAmount = Global.gUARTotalInfo.nTotalCash;
					if (Global.gUARTotalInfo.nTotalCash > 0) {
						uARPaymentCashInfo = new UARPaymentInfo();
						uARPaymentCashInfo.nTrxnNo = nTrxnNo;
						uARPaymentCashInfo.nChainNo = Global.gCurCustomerInfo.nChainNo;
						uARPaymentCashInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
						uARPaymentCashInfo.strTrxnDate = strTrxnDate;
						uARPaymentCashInfo.nTrxnAmount = Global.gUARTotalInfo.nTotalCash;
						uARPaymentCashInfo.nPayType = Global.COLLECTION_CASH;
					}

					Utils.setIntSetting(mContext, String.format(Locale.ENGLISH, Global.PREF_COLLECTION_SEQUENCE_NO, Global.gTripNumber), nSeqNo);
					long nPayTrxnNo = setGeneralUploadData("PAY", Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo, nTrxnNo);

					TransactionInfo transactionInfo = new TransactionInfo();
					transactionInfo.strType = "PAY";
					transactionInfo.strDocNo = Global.gUARTotalInfo.strDocNo;
					transactionInfo.value = Global.gUARTotalInfo.nTotalCash;
					transactionInfo.strCustomerName = Global.gCurCustomerInfo.strName;
					transactionInfo.strTime = new SimpleDateFormat("kk:mm").format(new Date(nPayTrxnNo));
					dbAdapter = new DbAdapter(mContext);
					dbAdapter.open();
					dbAdapter.insertTransactionHistoryInfo(transactionInfo);
					dbAdapter.close();

					dbAdapter.open();
					dbAdapter.insertUARTotalInfo(Global.gUARTotalInfo);
					if (Global.gUARPaymentCardInfo.nTrxnAmount > 0) {
						if (!strSelectedInvNo.isEmpty()) {
							Global.gUARPaymentCardInfo.strDocNo = strSelectedInvNo;
						}
						else if (Global.gCurCustomerInfo.strPayType.equals("3")) {
							Global.gUARPaymentCardInfo.strDocNo = Global.gUARTotalInfo.strDocNo;
						}
						else {
							Global.gUARPaymentCardInfo.strDocNo = "0";
						}
						Global.gUARPaymentCardInfo.strInvoiceNo = Global.gUARTotalInfo.strDocNo;
						Global.gUARPaymentCardInfo.strInvoiceDate = arHeaderInfo.strInvDate;
						Global.gUARPaymentCardInfo.nIdx = (int)dbAdapter.insertUARPaymentInfo(Global.gUARPaymentCardInfo);
						if (Global.gCurCustomerInfo.strPayType.equals("3") && mDeliverySummaryFragment != null) {
							mDeliverySummaryFragment.addCollectionPaymentInfo(Global.gUARPaymentCardInfo);
						}
					}
					if (Global.gUARPaymentChequeInfo.nTrxnAmount > 0) {
						if (!strSelectedInvNo.isEmpty()) {
							Global.gUARPaymentChequeInfo.strDocNo = strSelectedInvNo;
						}
						else if (Global.gCurCustomerInfo.strPayType.equals("3")) {
							Global.gUARPaymentChequeInfo.strDocNo = Global.gUARTotalInfo.strDocNo;
						}
						else {
							Global.gUARPaymentChequeInfo.strDocNo = "0";
						}
						Global.gUARPaymentChequeInfo.strInvoiceNo = Global.gUARTotalInfo.strDocNo;
						Global.gUARPaymentChequeInfo.strInvoiceDate = arHeaderInfo.strInvDate;
						Global.gUARPaymentChequeInfo.nIdx = (int)dbAdapter.insertUARPaymentInfo(Global.gUARPaymentChequeInfo);
						if (Global.gCurCustomerInfo.strPayType.equals("3") && mDeliverySummaryFragment != null) {
							mDeliverySummaryFragment.addCollectionPaymentInfo(Global.gUARPaymentChequeInfo);
						}
					}
					if (Global.gUARPaymentEFTInfo.nTrxnAmount > 0) {
						if (!strSelectedInvNo.isEmpty()) {
							Global.gUARPaymentEFTInfo.strDocNo = strSelectedInvNo;
						}
						else if (Global.gCurCustomerInfo.strPayType.equals("3")) {
							Global.gUARPaymentEFTInfo.strDocNo = Global.gUARTotalInfo.strDocNo;
						}
						else {
							Global.gUARPaymentEFTInfo.strDocNo = "0";
						}
						Global.gUARPaymentEFTInfo.strInvoiceNo = Global.gUARTotalInfo.strDocNo;
						Global.gUARPaymentEFTInfo.strInvoiceDate = arHeaderInfo.strInvDate;
						Global.gUARPaymentEFTInfo.nIdx = (int)dbAdapter.insertUARPaymentInfo(Global.gUARPaymentEFTInfo);
						if (Global.gCurCustomerInfo.strPayType.equals("3") && mDeliverySummaryFragment != null) {
							mDeliverySummaryFragment.addCollectionPaymentInfo(Global.gUARPaymentEFTInfo);
						}
					}
					if (uARPaymentCashInfo != null) {
						if (!strSelectedInvNo.isEmpty()) {
							uARPaymentCashInfo.strDocNo = strSelectedInvNo;
						}
						else if (Global.gCurCustomerInfo.strPayType.equals("3")) {
							uARPaymentCashInfo.strDocNo = Global.gUARTotalInfo.strDocNo;
						}
						else {
							uARPaymentCashInfo.strDocNo = "0";
						}
						uARPaymentCashInfo.strInvoiceNo = Global.gUARTotalInfo.strDocNo;
						uARPaymentCashInfo.strInvoiceDate = arHeaderInfo.strInvDate;
						uARPaymentCashInfo.nIdx = (int)dbAdapter.insertUARPaymentInfo(uARPaymentCashInfo);
						if (Global.gCurCustomerInfo.strPayType.equals("3") && mDeliverySummaryFragment != null) {
							mDeliverySummaryFragment.addCollectionPaymentInfo(uARPaymentCashInfo);
						}
					}
//					dbAdapter.updateCustomerInfo(Global.gCurCustomerInfo);
					float nTrxnAmount = Utils.getFloatSetting(mContext, Global.COLLECTIONS_TODAY);
					if (uCollectionsDChequeInfo != null) {
						nTrxnAmount += uCollectionsDChequeInfo.nTrxnAmount;
						uCollectionsDChequeInfo.strInvoiceNo = Global.gUARTotalInfo.strDocNo;
						dbAdapter.insertUCollectionsDInfo(uCollectionsDChequeInfo);
					}
					if (uCollectionsDCashInfo != null) {
						nTrxnAmount += uCollectionsDCashInfo.nTrxnAmount;
						uCollectionsDCashInfo.strInvoiceNo = Global.gUARTotalInfo.strDocNo;
						dbAdapter.insertUCollectionsDInfo(uCollectionsDCashInfo);
					}
					if (uCollectionsDEFTInfo != null) {
						nTrxnAmount += uCollectionsDEFTInfo.nTrxnAmount;
						uCollectionsDEFTInfo.strInvoiceNo = Global.gUARTotalInfo.strDocNo;
						dbAdapter.insertUCollectionsDInfo(uCollectionsDEFTInfo);
					}
					if (uCollectionsDCardInfo != null) {
						nTrxnAmount += uCollectionsDCardInfo.nTrxnAmount;
						uCollectionsDCardInfo.strInvoiceNo = Global.gUARTotalInfo.strDocNo;
						dbAdapter.insertUCollectionsDInfo(uCollectionsDCardInfo);
					}
					Utils.setFloatSetting(mContext, Global.COLLECTIONS_TODAY, nTrxnAmount);

					dbAdapter.close();
					//Global.gUploadCustNoList.add(makeZipFileInfo);
					Utils.registerUploadFileAsTemp(mContext, makeZipFileInfo);
				}

				Global.gUARPaymentChequeInfo.nTrxnAmount = 0;
				Global.gUARPaymentCardInfo.nTrxnAmount = 0;
				Global.gUARPaymentEFTInfo.nTrxnAmount = 0;
				Global.gUARTotalInfo.nTotalCash = 0;

			}catch(Exception e)
			{
				e.printStackTrace();
			}
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
			mContext.onBackPressed();
		}

		if (mDeliverySummaryFragment != null) {
			if (mDeliverySummaryFragment.getCodPaymentOptionSelected()) {
				mDeliverySummaryFragment.setPartialPayment(this.bPartialPayment);
				mDeliverySummaryFragment.setPartialPaidAmount(this.fPartialPaidAmount);
				mDeliverySummaryFragment.doContinueCodCustomer();
			}
		}
	}

	public boolean m_bClicked = false;
	@Override
	public void onClick(View v) {
		if( m_bClicked ) return;
		m_bClicked = true;
//		if(!v.isEnabled()) return;
		switch (v.getId()) {
			case R.id.layoutCheque:
				{
					Bundle bundle = new Bundle();
					Global.gUARTotalInfo.nTotalCash = mCash;
					Global.gUARPaymentChequeInfo.nTrxnAmount = mCheque;
					Global.gUARPaymentCardInfo.nTrxnAmount = mCard;
					Global.gUARPaymentEFTInfo.nTrxnAmount = mEFT;
					bundle.putBoolean(Global.EXTRA_IS_AUTO_COLLECT, false);
					Fragment fragment = new ChequeDetailFragment();
					fragment.setArguments(bundle);
					mContext.replaceFragment(fragment);
				}
				break;
			case R.id.layoutEFT:
				{
					Bundle bundle = new Bundle();
					Global.gUARTotalInfo.nTotalCash = mCash;
					Global.gUARPaymentChequeInfo.nTrxnAmount = mCheque;
					Global.gUARPaymentCardInfo.nTrxnAmount = mCard;
					Global.gUARPaymentEFTInfo.nTrxnAmount = mEFT;
					bundle.putBoolean(Global.EXTRA_IS_AUTO_COLLECT, false);
					Fragment fragment = new EFTDetailFragment();
					fragment.setArguments(bundle);
					mContext.replaceFragment(fragment);
				}
				break;
			case R.id.layoutCard:
				if (Utils.getIntSetting(mContext, Global.PREF_EWAY_CARD_PROC) == 0) {
					CardDetailFragment fragment = new CardDetailFragment();
					mContext.replaceFragment(fragment);
				} else if(Utils.getIntSetting(mContext, Global.PREF_EWAY_CARD_PROC) == 1){
					onEwayPayment();
				} else if(Utils.getIntSetting(mContext, Global.PREF_EWAY_CARD_PROC) == 2) {
					onMintCardPresent(true);
				} else if(Utils.getIntSetting(mContext, Global.PREF_EWAY_CARD_PROC) == 3) {
					String strMintApiKey = Utils.getStringSetting(mContext, Global.PREF_MINTAPI);
					String strMintSecret = Utils.getStringSetting(mContext, Global.PREF_MINTSECRET);
					if(!TextUtils.isEmpty(strMintApiKey) && !TextUtils.isEmpty(strMintSecret))
					{
						final String strArray[] = {Str.CardPresent[Global.LANG], Str.EwayTransaction[Global.LANG]};
						final int origIdx = mIntPayMethod;
						try {
							new AlertDialog.Builder(mContext)
									.setTitle(Str.SelectCardPaymentMethod[Global.LANG])
									.setSingleChoiceItems(new AlertSingleChoiceAdapter(mContext, strArray), mIntPayMethod, new DialogInterface.OnClickListener()
									{
										public void onClick(DialogInterface dialog, int nSelected)
										{
											mIntPayMethod = nSelected;
										}
									})
									.setPositiveButton(Str.Ok[Global.LANG], new DialogInterface.OnClickListener()
									{
										public void onClick(DialogInterface dialog, int nSelected)
										{
											if(mIntPayMethod == 0)
											{
												onMintCardPresent(true);
											}
											else
											{
												onEwayPayment();
											}
										}
									})
									.setNegativeButton(Str.Cancel[Global.LANG], new DialogInterface.OnClickListener()
									{
										public void onClick(DialogInterface dialog, int nSelected)
										{
											mIntPayMethod = origIdx;
										}
									})
									.create()
									.show();
						}catch (Exception e)
						{
							e.printStackTrace();
						}


					/*MintCardProcDialog dialog = new MintCardProcDialog(mContext, DeliverySummaryFragment.this);
					dialog.show();*/
					}
					else
					{
						onEwayPayment();
					}
				} else if(Utils.getIntSetting(mContext, Global.PREF_EWAY_CARD_PROC) == 4) {
					onMintNotPresent();
				} else if(Utils.getIntSetting(mContext, Global.PREF_EWAY_CARD_PROC) == 5) {
					String strMintApiKey = Utils.getStringSetting(mContext, Global.PREF_MINTAPI);
					String strMintSecret = Utils.getStringSetting(mContext, Global.PREF_MINTSECRET);
					if(!TextUtils.isEmpty(strMintApiKey) && !TextUtils.isEmpty(strMintSecret))
					{
						final String strArray[] = {Str.CardPresent[Global.LANG], Str.NotPresentTransaction[Global.LANG]};
						final int origIdx = mIntPayMethod;
						try {
							new AlertDialog.Builder(mContext)
									.setTitle(Str.SelectCardPaymentMethod[Global.LANG])
									.setSingleChoiceItems(new AlertSingleChoiceAdapter(mContext, strArray), mIntPayMethod, new DialogInterface.OnClickListener()
									{
										public void onClick(DialogInterface dialog, int nSelected)
										{
											mIntPayMethod = nSelected;
										}
									})
									.setPositiveButton(Str.Ok[Global.LANG], new DialogInterface.OnClickListener()
									{
										public void onClick(DialogInterface dialog, int nSelected)
										{
											if(mIntPayMethod == 0)
											{
												onMintCardPresent(false);
											}
											else
											{
												onMintNotPresent();
											}
										}
									})
									.setNegativeButton(Str.Cancel[Global.LANG], new DialogInterface.OnClickListener()
									{
										public void onClick(DialogInterface dialog, int nSelected)
										{
											mIntPayMethod = origIdx;
										}
									})
									.create()
									.show();
						}catch (Exception e)
						{
							e.printStackTrace();
						}


					/*MintCardProcDialog dialog = new MintCardProcDialog(mContext, DeliverySummaryFragment.this);
					dialog.show();*/
					}
					else
					{
						onMintNotPresent();
					}
				} else if(Utils.getIntSetting(mContext, Global.PREF_EWAY_CARD_PROC) == 6) {
					onMintIFrame();
				} else if(Utils.getIntSetting(mContext, Global.PREF_EWAY_CARD_PROC) == 7) {
					String strMintApiKey = Utils.getStringSetting(mContext, Global.PREF_MINTAPI);
					String strMintSecret = Utils.getStringSetting(mContext, Global.PREF_MINTSECRET);
					if(!TextUtils.isEmpty(strMintApiKey) && !TextUtils.isEmpty(strMintSecret))
					{
						final String strArray[] = {Str.CardPresent[Global.LANG], Str.NotPresentTransaction[Global.LANG]};
						final int origIdx = mIntPayMethod;
						try {
							new AlertDialog.Builder(mContext)
									.setTitle(Str.SelectCardPaymentMethod[Global.LANG])
									.setSingleChoiceItems(new AlertSingleChoiceAdapter(mContext, strArray), mIntPayMethod, new DialogInterface.OnClickListener()
									{
										public void onClick(DialogInterface dialog, int nSelected)
										{
											mIntPayMethod = nSelected;
										}
									})
									.setPositiveButton(Str.Ok[Global.LANG], new DialogInterface.OnClickListener()
									{
										public void onClick(DialogInterface dialog, int nSelected)
										{
											if(mIntPayMethod == 0)
											{
												onMintCardPresent(false);
											}
											else
											{
												onMintIFrame();
											}
										}
									})
									.setNegativeButton(Str.Cancel[Global.LANG], new DialogInterface.OnClickListener()
									{
										public void onClick(DialogInterface dialog, int nSelected)
										{
											mIntPayMethod = origIdx;
										}
									})
									.create()
									.show();
						}catch (Exception e)
						{
							e.printStackTrace();
						}


					/*MintCardProcDialog dialog = new MintCardProcDialog(mContext, DeliverySummaryFragment.this);
					dialog.show();*/
					}
					else
					{
						onMintIFrame();
					}
				} else if(Utils.getIntSetting(mContext, Global.PREF_EWAY_CARD_PROC) == 8) {
					onStripePayment();
				} else if(Utils.getIntSetting(mContext, Global.PREF_EWAY_CARD_PROC) == 9)
				{
					onWindCaveNotPresent();
				}
				else if(Utils.getIntSetting(mContext, Global.PREF_EWAY_CARD_PROC) == 10)
				{
					String strWindCaveApi = Utils.getStringSetting(mContext, Global.PREF_WINDCAVE_API);
					String strWindCavePassword = Utils.getStringSetting(mContext, Global.PREF_WINDCAVE_PASSWORD);
					boolean bCardNotPresent = !TextUtils.isEmpty(Utils.getStringSetting(mContext, Global.PREF_WINDCAVE_API)) && !TextUtils.isEmpty(Utils.getStringSetting(mContext, Global.PREF_WINDCAVE_PASSWORD));
					boolean bCardPresent = !TextUtils.isEmpty(Utils.getStringSetting(mContext, Global.PREF_WINDCAVE_HITUSERNAME)) && !TextUtils.isEmpty(Utils.getStringSetting(mContext, Global.PREF_WINDCAVE_HITKEY));
					if(bCardNotPresent && bCardPresent)
					{
						final String strArray[] = {Str.WINDCAVE_CARD_PRESENT[Global.LANG], Str.WINDCAVE_CARD_NOT_PRESENT[Global.LANG]};
						final int origIdx = windcavePayMethod;
						try {
							new AlertDialog.Builder(mContext)
									.setTitle(Str.SelectCardPaymentMethod[Global.LANG])
									.setSingleChoiceItems(new AlertSingleChoiceAdapter(mContext, strArray), windcavePayMethod, new DialogInterface.OnClickListener()
									{
										public void onClick(DialogInterface dialog, int nSelected)
										{
											windcavePayMethod = nSelected;
										}
									})
									.setPositiveButton(Str.Ok[Global.LANG], new DialogInterface.OnClickListener()
									{
										public void onClick(DialogInterface dialog, int nSelected)
										{
											if(windcavePayMethod == 0)
											{
												onWindCavePresent();
											}
											else
											{
												onWindCaveNotPresent();
											}
										}
									})
									.setNegativeButton(Str.Cancel[Global.LANG], new DialogInterface.OnClickListener()
									{
										public void onClick(DialogInterface dialog, int nSelected)
										{
											windcavePayMethod = origIdx;
										}
									})
									.create()
									.show();
						}catch (Exception e)
						{
							e.printStackTrace();
						}

					}
					else if(bCardPresent)
					{
						onWindCavePresent();
					}
					else if(bCardNotPresent)
					{
						onWindCaveNotPresent();
					}
					else
					{
						onEwayPayment();
					}
				}
				break;
			case R.id.btnPhoto:
				selectPhoto();
				break;
			case R.id.btnBack:
				mContext.onBackPressed();
				break;
			case R.id.btnCompleted:
				doComplete();
				break;
		}
		m_bClicked = false;
	}

	public void onWindCavePresent(){
		doWindCaveCardPresentBySDK();
	}

	public void onWindCaveNotPresent() {
		doWindCaveBySDK();
	}

	private void doWindCaveBySDK()
	{
		if(TextUtils.isEmpty(mStrPdfFileName))
		{
			if(Utils.getStringSetting(mContext, Global.PREF_USEORDNOASINV).equals("N") )
			{
				mStrPdfFileName = getPDFFileName();
			}
			else
			{
				if(TextUtils.isEmpty(Utils.getStringSetting(mContext, Global.PREF_INVOICENUM)))
				{
					if(Global.gCurCustomerInfo != null && Global.gCurCustomerInfo.presoldOrHInfo.size() > 0 && Global.gCurCustomerInfo.presoldOrHInfo.get(0).strOrderNo.length() > 3 )
					{
						mStrPdfFileName = Global.gCurCustomerInfo.presoldOrHInfo.get(0).strOrderNo + ".pdf";
					}
					else
					{
						mStrPdfFileName = getPDFFileName();
					}
				}
				else
				{
					if(Global.gCurCustomerInfo != null && Global.gCurCustomerInfo.presoldOrHInfo.size() > 0 )
					{
						mStrPdfFileName = Utils.getOrderNo(String.valueOf(Global.gCurCustomerInfo.presoldOrHInfo.get(0).nQty), Utils.getStringSetting(mContext, Global.PREF_INVOICENUM)) + ".pdf";
					}
					else
					{
						mStrPdfFileName = getPDFFileName();
					}
				}
			}
		}

		Global.gCurRealPaymentAmount = mTotalRemaining;
		final String strReference = Global.gCurCustomerInfo.nChainNo + "/" + Global.gCurCustomerInfo.nCustNo + "--(" + mStrPdfFileName.substring(0, mStrPdfFileName.length() - 4) + ")";
		Intent intent = new Intent(mContext, WindCaveActivity.class);
		intent.putExtra("reference", strReference);
		intent.putExtra("card_present", false);
		startActivityForResult(intent, WIND_CAVE_API_CALL);
	}

	private void doWindCaveCardPresentBySDK()
	{
		if(TextUtils.isEmpty(Utils.getStringSetting(mContext, Global.PREF_WINDCAVE_HITSTATION_ID)))
		{
			try {
				WindcaveStationIDDialog dialog = new WindcaveStationIDDialog(mContext, new WindcaveStationIDDialog.Callback() {
					@Override
					public void onUpdate() {
						doWindCaveCardPresentBySDK();
					}

					@Override
					public void onCancel() {

					}
				});
				dialog.setCancelable(false);
				dialog.show();
			}catch (Exception e)
			{
				e.printStackTrace();
			}

		}
		else
		{
			if(TextUtils.isEmpty(mStrPdfFileName))
			{
				if(Utils.getStringSetting(mContext, Global.PREF_USEORDNOASINV).equals("N") )
				{
					mStrPdfFileName = getPDFFileName();
				}
				else
				{
					if(TextUtils.isEmpty(Utils.getStringSetting(mContext, Global.PREF_INVOICENUM)))
					{
						if(Global.gCurCustomerInfo != null && Global.gCurCustomerInfo.presoldOrHInfo.size() > 0 && Global.gCurCustomerInfo.presoldOrHInfo.get(0).strOrderNo.length() > 3 )
						{
							mStrPdfFileName = Global.gCurCustomerInfo.presoldOrHInfo.get(0).strOrderNo + ".pdf";
						}
						else
						{
							mStrPdfFileName = getPDFFileName();
						}
					}
					else
					{
						if(Global.gCurCustomerInfo != null && Global.gCurCustomerInfo.presoldOrHInfo.size() > 0 )
						{
							mStrPdfFileName = Utils.getOrderNo(String.valueOf(Global.gCurCustomerInfo.presoldOrHInfo.get(0).nQty), Utils.getStringSetting(mContext, Global.PREF_INVOICENUM)) + ".pdf";
						}
						else
						{
							mStrPdfFileName = getPDFFileName();
						}
					}
				}
			}
			Global.gCurRealPaymentAmount = mTotalRemaining;
			final String strReference = Global.gCurCustomerInfo.nChainNo + "/" + Global.gCurCustomerInfo.nCustNo + "--(" + mStrPdfFileName.substring(0, mStrPdfFileName.length() - 4) + ")";
			Intent intent = new Intent(mContext, WindCaveActivity.class);
			intent.putExtra("reference", strReference);
			intent.putExtra("card_present", true);
			startActivityForResult(intent, WIND_CAVE_API_CALL);
		}


	}

	public void onMintIFrame()
	{
		// show the card not presented collections fragment
		CardNotPresentedCollectionsFragment fragment = new CardNotPresentedCollectionsFragment();
		mContext.replaceFragment(fragment);
	}

	@Override
	public void receiveBroadcast(String strAction, Intent intent) {
		if (strAction.equals(Global.ACTION_TAKE_PHOTO) == true) {
			String strPhotoPath = intent.getStringExtra(Global.EXTRA_PHOTO_PATH);
			if(!TextUtils.isEmpty(strPhotoPath))
			{
				String strNewPhotoPath = Global.UPLOAD_DIR + System.currentTimeMillis() + ".jpg";
				Utils.fileCopy(strPhotoPath, strNewPhotoPath);
				Utils.fileDelete(strPhotoPath);
				mStrPhotoPathList.add(strNewPhotoPath);
			}
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == Global.MINT_IFRAME && resultCode == Activity.RESULT_OK)
		{
			if(data == null) return;
			String strUrl = data.getStringExtra("url");
			Uri uri = Uri.parse(strUrl);
			if(uri.getQueryParameter("successful") != null && uri.getQueryParameter("successful").equals("true"))
			{
				float amount = Utils.parseFloatFromString(uri.getQueryParameter("amount")) / 100.0f;
				if(amount > mTotalRemaining )
				{
					amount = mTotalRemaining;
				}
				Global.gUARPaymentCardInfo.nTrxnAmount = amount;
				loadData();
			}
			else
			{
				if(!mContext.isFinishing() && !mContext.isDestroyed())
				{
					try {
						Toast.makeText(mContext, Str.PaymentNotCompleted[Global.LANG], Toast.LENGTH_LONG).show();
					}catch (Exception e1)
					{
						e1.printStackTrace();
					}
				}

			}
		}
		else if(requestCode == WIND_CAVE_API_CALL)
		{
			if(resultCode == Activity.RESULT_OK)
			{
				if(data == null) return;
				String strReference = data.getStringExtra("reference");
				Global.gUARPaymentCardInfo.nTrxnAmount = Global.gCurRealPaymentAmount;

				/*DbAdapter dbAdapter = new DbAdapter(mContext);
				dbAdapter.open();
				List<ARHeaderInfo> arHeaderInfos = dbAdapter.getARHeaderInfo(Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo);
				if( arHeaderInfos.size() > 0)
				{
					for(ARHeaderInfo arHeaderInfo : arHeaderInfos)
					{
						arHeaderInfo.nProcessStatus = 2;
						dbAdapter.updateARHeaderInfo(arHeaderInfo);
					}
				}
				dbAdapter.close();*/
				mCard += Global.gUARPaymentCardInfo.nTrxnAmount;
				doComplete();
			}
			else
			{
				if(data == null) return;
				String errorMsg = data.getStringExtra("error_msg");
				if(!mContext.isFinishing() && !mContext.isDestroyed())
				{
					try {
						Toast.makeText(mContext, errorMsg, Toast.LENGTH_LONG).show();
					}catch (Exception e1)
					{
						e1.printStackTrace();
					}
				}

			}
		}
		else if(requestCode == STRIPE_PAYMENT && resultCode == Activity.RESULT_OK)
		{
			if(data == null ) return;
			String strReturnType = data.getStringExtra("return_type");
			if(strReturnType.equals("middle"))
			{
				Global.gUARTotalInfo.nTotalCash = mCash;
				Global.gUARPaymentChequeInfo.nTrxnAmount = mCheque;
				Global.gUARPaymentCardInfo.nTrxnAmount = mCard;
				Global.gUARPaymentEFTInfo.nTrxnAmount = mEFT;
				CardDetailFragment fragment = new CardDetailFragment();
				Bundle param = new Bundle();
				param.putBoolean(Global.EXTRA_IS_AUTO_COLLECT, true);
				fragment.setArguments(param);
				mContext.replaceFragment(fragment);
			}
			else {
				float nTrxnAmount = data.getFloatExtra("trxnAmount", 0);
				Global.gUARPaymentCardInfo.nTrxnAmount = nTrxnAmount;
				loadData();
			}
		}
		else
		{
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	public void onEwayPayment()
	{
		if(bCard)
		{
			DbAdapter dbAdapter = new DbAdapter(mContext);
			dbAdapter.open();
			UCollectionsDInfo uCollectionsDInfo = null;
			List<UCollectionsDInfo> uCollectionsDInfos = dbAdapter.getUCollectionsDList(Global.gCurCustomerInfo.nCustNo, Global.gCurCustomerInfo.nChainNo);
			for(int i = uCollectionsDInfos.size() - 1; i >= 0; i--)
			{
				if(uCollectionsDInfos.get(i).nPayType == Global.COLLECTION_CARD)
				{
					uCollectionsDInfo = uCollectionsDInfos.get(i);
					break;
				}
			}
			dbAdapter.close();

			try {
				final EwayPaymentDialog dlg = new EwayPaymentDialog(mContext);
				if( uCollectionsDInfo != null )
					dlg.mAmount = uCollectionsDInfo.nTrxnAmount;
				else
					dlg.mAmount = 0;
				dlg.setCancelable(false);
				dlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						if (dlg.mReturnCode == Global.DLG_RETURN_RIGHT) {
//								Global.gUARPaymentCardInfo.nTrxnAmount += dlg.mAmount;
							Global.gUARPaymentCardInfo.nTrxnAmount = dlg.mAmount;
							loadData();
						} else if (dlg.mReturnCode == Global.DLG_RETURN_MIDDLE) {
							Global.gUARTotalInfo.nTotalCash = mCash;
							Global.gUARPaymentChequeInfo.nTrxnAmount = mCheque;
							Global.gUARPaymentCardInfo.nTrxnAmount = mCard;
							Global.gUARPaymentEFTInfo.nTrxnAmount = mEFT;
							CardDetailFragment fragment = new CardDetailFragment();
							Bundle param = new Bundle();
							param.putBoolean(Global.EXTRA_IS_AUTO_COLLECT, true);
							fragment.setArguments(param);
							mContext.replaceFragment(fragment);
						}
					}
				});
				dlg.show();
			}catch (Exception e)
			{
				e.printStackTrace();
			}

		}
		else
		{
			try {
				final EwayPaymentDialog dlg = new EwayPaymentDialog(mContext);
				dlg.mAmount = Global.gUARPaymentCardInfo.nTrxnAmount;
				dlg.setCancelable(false);
				dlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						if (dlg.mReturnCode == Global.DLG_RETURN_RIGHT) {
//								Global.gUARPaymentCardInfo.nTrxnAmount += dlg.mAmount;
							Global.gUARPaymentCardInfo.nTrxnAmount = dlg.mAmount;
							loadData();
						} else if (dlg.mReturnCode == Global.DLG_RETURN_MIDDLE) {
							Global.gUARTotalInfo.nTotalCash = mCash;
							Global.gUARPaymentChequeInfo.nTrxnAmount = mCheque;
							Global.gUARPaymentCardInfo.nTrxnAmount = mCard;
							Global.gUARPaymentEFTInfo.nTrxnAmount = mEFT;
							mContext.replaceFragment(new CardDetailFragment());
						}
					}
				});
				dlg.show();
			}catch (Exception e)
			{
				e.printStackTrace();
			}

		}
	}

	private void showStripePaymentDialog(final float amount)
	{
		if(bCard)
		{
			try {

				Intent intent = new Intent(mContext, StripePaymentActivity.class);
				intent.putExtra("amount", amount);
				startActivityForResult(intent, STRIPE_PAYMENT);


				/*final NewStripePaymentDialog dlg = new NewStripePaymentDialog(mContext);
				dlg.mAmount = amount;
				dlg.setCancelable(false);
				dlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						if (dlg.mReturnCode == Global.DLG_RETURN_RIGHT) {
							Global.gUARPaymentCardInfo.nTrxnAmount = dlg.mAmount;

							if(!TextUtils.isEmpty(dlg.mCustomerID))
							{
								initMakeZipInfo();
								CustomerAddInfo customerAddInfo = new CustomerAddInfo();
								customerAddInfo.strInfoType = "50";
								customerAddInfo.strInfo = dlg.mCustomerID;
								customerAddInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
								customerAddInfo.nChainNo = Global.gCurCustomerInfo.nChainNo;

								UCustomerFMInfo uCustomerFMInfo = Global.gDefaultUCustomerFMInfo.duplicate();
								uCustomerFMInfo.nChainNo = Global.gCurCustomerInfo.nChainNo;
								uCustomerFMInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
								uCustomerFMInfo.strEditType = "EDT";
								uCustomerFMInfo.strStripeInfo = dlg.mCustomerID;
								setGeneralUploadData("CSFM", Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo, -1);
								uCustomerFMInfo.nTrxnNo = m_lTrxnNo;

								uCustomerFMInfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(mCurDate);
								uCustomerFMInfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(mCurDate);
								uCustomerFMInfo.strStartDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(mCurDate);
								uCustomerFMInfo.strStartTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(mCurDate);
								uCustomerFMInfo.strEndDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(mCurDate);
								uCustomerFMInfo.strEndTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(mCurDate);

								DbAdapter dbAdapter = new DbAdapter(mContext);
								dbAdapter.open();
								dbAdapter.insertCustAddInfo(customerAddInfo);
								dbAdapter.insertUCustomerFMInfo(uCustomerFMInfo);
								dbAdapter.close();

								//Utils.registerUploadFile(mContext, makeZipFileInfo);
								Utils.registerUploadFileAsTemp(mContext, makeZipFileInfo);
								makeZipFileInfo = null;
							}
							loadData();
						} else if (dlg.mReturnCode == Global.DLG_RETURN_MIDDLE) {
							Global.gUARTotalInfo.nTotalCash = mCash;
							Global.gUARPaymentChequeInfo.nTrxnAmount = mCheque;
							Global.gUARPaymentCardInfo.nTrxnAmount = mCard;
							CardDetailFragment fragment = new CardDetailFragment();
							Bundle param = new Bundle();
							param.putBoolean(Global.EXTRA_IS_AUTO_COLLECT, true);
							fragment.setArguments(param);
							mContext.replaceFragment(fragment);
						}
					}
				});
				dlg.show();*/

			}catch (Exception e)
			{
				e.printStackTrace();
			}

		}
		else
		{
			try {
				Intent intent = new Intent(mContext, StripePaymentActivity.class);
				intent.putExtra("amount", amount);
				startActivityForResult(intent, STRIPE_PAYMENT);
				/*final NewStripePaymentDialog dlg = new NewStripePaymentDialog(mContext);
				dlg.mAmount = amount;
				dlg.setCancelable(false);
				dlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						if (dlg.mReturnCode == Global.DLG_RETURN_RIGHT) {
							Global.gUARPaymentCardInfo.nTrxnAmount = dlg.mAmount;
							if(!TextUtils.isEmpty(dlg.mCustomerID))
							{
								initMakeZipInfo();
								CustomerAddInfo customerAddInfo = new CustomerAddInfo();
								customerAddInfo.strInfoType = "50";
								customerAddInfo.strInfo = dlg.mCustomerID;
								customerAddInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
								customerAddInfo.nChainNo = Global.gCurCustomerInfo.nChainNo;

								UCustomerFMInfo uCustomerFMInfo = Global.gDefaultUCustomerFMInfo.duplicate();
								uCustomerFMInfo.nChainNo = Global.gCurCustomerInfo.nChainNo;
								uCustomerFMInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
								uCustomerFMInfo.strEditType = "EDT";
								uCustomerFMInfo.strStripeInfo = dlg.mCustomerID;
								setGeneralUploadData("CSFM", Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo, -1);
								uCustomerFMInfo.nTrxnNo = m_lTrxnNo;

								uCustomerFMInfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(mCurDate);
								uCustomerFMInfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(mCurDate);
								uCustomerFMInfo.strStartDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(mCurDate);
								uCustomerFMInfo.strStartTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(mCurDate);
								uCustomerFMInfo.strEndDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(mCurDate);
								uCustomerFMInfo.strEndTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(mCurDate);

								DbAdapter dbAdapter = new DbAdapter(mContext);
								dbAdapter.open();
								dbAdapter.insertCustAddInfo(customerAddInfo);
								dbAdapter.insertUCustomerFMInfo(uCustomerFMInfo);
								dbAdapter.close();

								//Utils.registerUploadFile(mContext, makeZipFileInfo);
								Utils.registerUploadFile(mContext, makeZipFileInfo);
								makeZipFileInfo = null;
							}
							loadData();
						} else if (dlg.mReturnCode == Global.DLG_RETURN_MIDDLE) {
							Global.gUARTotalInfo.nTotalCash = mCash;
							Global.gUARPaymentChequeInfo.nTrxnAmount = mCheque;
							Global.gUARPaymentCardInfo.nTrxnAmount = mCard;
							mContext.replaceFragment(new CardDetailFragment());
						}
					}
				});
				dlg.show();*/
			}catch (Exception e)
			{
				e.printStackTrace();
			}

		}
	}

	public void onStripePayment()
	{
		float amount = 0f;
		if(bCard)
		{
			DbAdapter dbAdapter = new DbAdapter(mContext);
			dbAdapter.open();
			UCollectionsDInfo uCollectionsDInfo = null;
			List<UCollectionsDInfo> uCollectionsDInfos = dbAdapter.getUCollectionsDList(Global.gCurCustomerInfo.nCustNo, Global.gCurCustomerInfo.nChainNo);
			for(int i = uCollectionsDInfos.size() - 1; i >= 0; i--)
			{
				if(uCollectionsDInfos.get(i).nPayType == Global.COLLECTION_CARD)
				{
					uCollectionsDInfo = uCollectionsDInfos.get(i);
					break;
				}
			}
			dbAdapter.close();

			if( uCollectionsDInfo != null )
				amount = uCollectionsDInfo.nTrxnAmount;
			else
				amount = 0;
		}
		else
		{
			amount = Global.gUARPaymentCardInfo.nTrxnAmount;
		}
		if(amount == 0)
		{
			showStripePaymentDialog(amount);
		}
		else
		{
			DbAdapter dbAdapter = new DbAdapter(mContext);
			dbAdapter.open();
			final List<CustomerAddInfo> customerAddInfoList = dbAdapter.getCustAddInfoList(Global.gCurCustomerInfo.nCustNo, Global.gCurCustomerInfo.nChainNo, "50");
			dbAdapter.close();

			final String strPubKey = Utils.getStringSetting(mContext, Global.PREF_STRIPE_PUB_KEY);
			final String strSecKey = Utils.getStringSetting(mContext, Global.PREF_STRIPE_SEC_KEY);
			Stripe.apiKey = strSecKey;

			if(customerAddInfoList.size() > 0 && !TextUtils.isEmpty(strPubKey) && !TextUtils.isEmpty(strSecKey))
			{
				showProgressDialog(Str.Loading[Global.LANG]);
				final float finalAmount = amount;
				final float finalAmount1 = amount;
				final float finalAmount2 = amount;
				new Thread()
				{
					@Override
					public void run() {
						final List<StripeSavedCard> savedCardList = new ArrayList<>();
						//Get Card Details
						for(CustomerAddInfo addInfo : customerAddInfoList)
						{
							try {
								Customer customer = Customer.retrieve(addInfo.strInfo);
								List<PaymentSource> sourceList = customer.getSources().getData();
								if(sourceList.size() == 1 && sourceList.get(0) instanceof com.stripe.model.Card)
								{
									com.stripe.model.Card card = (com.stripe.model.Card)sourceList.get(0);
									StripeSavedCard savedCard = new StripeSavedCard();
									savedCard.addInfo = addInfo;
									savedCard.strCardEnding = card.getLast4();
									savedCard.strCardKind = card.getBrand();
									savedCard.strCardHolder = card.getName();

									savedCardList.add(savedCard);
								}

							} catch (StripeException e) {
								e.printStackTrace();
							}
						}

						if(savedCardList.size() == 1)
						{
							new Handler(Looper.getMainLooper()).post(new Runnable() {
								@Override
								public void run() {
									hideProgressDialog();
									try {
										final MessageDialog msgDialog = new MessageDialog(mContext);
										msgDialog.setCancelable(false);
										msgDialog.mStrMessage = String.format(Locale.ENGLISH, Str.UseSavedCard[Global.LANG],
												savedCardList.get(0).strCardKind, savedCardList.get(0).strCardEnding,
												savedCardList.get(0).strCardHolder, Global.gCurCustomerInfo.strName);
										msgDialog.mStrLeft = Str.No[Global.LANG];
										msgDialog.mStrRight = Str.Yes[Global.LANG];
										msgDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
											@Override
											public void onDismiss(DialogInterface dialog) {
												if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
													doProcessSavedCardStripeProcessing(savedCardList.get(0).addInfo, finalAmount1);
												} else if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
													showStripePaymentDialog(finalAmount);
												}
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
						else if(savedCardList.size() == 0)
						{
							new Handler(Looper.getMainLooper()).post(new Runnable() {
								@Override
								public void run() {
									hideProgressDialog();
									showStripePaymentDialog(finalAmount);
								}
							});
						}
						else
						{
							new Handler(Looper.getMainLooper()).post(new Runnable() {
								@Override
								public void run() {
									hideProgressDialog();
									try {
										final StripeSavedCardSelectDialog msgDialog = new StripeSavedCardSelectDialog(mContext, savedCardList);
										msgDialog.setCancelable(false);
										msgDialog.mStrMessage = Str.UseSavedFollowingCard[Global.LANG];
										msgDialog.mStrLeft = Str.No[Global.LANG];
										msgDialog.mStrRight = Str.Yes[Global.LANG];
										msgDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
											@Override
											public void onDismiss(DialogInterface dialog) {
												if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
													doProcessSavedCardStripeProcessing(savedCardList.get(msgDialog.mSelIdx).addInfo, finalAmount2);
												} else if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
													showStripePaymentDialog(finalAmount);
												}
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

					}
				}.start();
			}
			else {
				showStripePaymentDialog(amount);
			}
		}
	}

	private void doProcessSavedCardStripeProcessing(final CustomerAddInfo addInfo, final float amount)
	{
		if(addInfo == null)
		{
			showStripePaymentDialog(amount);
		}
		else
		{
			showProgressDialog(Str.Loading[Global.LANG]);
			new Thread(){
				@Override
				public void run() {

					Global.gCurRealPaymentAmount = mTotalRemaining;
					String strCurrency = Utils.getStringSetting(mContext, Global.PREF_CURRENCY);
					if(TextUtils.isEmpty(strCurrency))
					{
						strCurrency = "AUD";
					}

					Map<String, Object> chargeParams = new HashMap<>();
					chargeParams.put("amount", (int)(Global.gCurRealPaymentAmount * 100));
					chargeParams.put("currency", strCurrency);
					chargeParams.put("customer", addInfo.strInfo);
					chargeParams.put("description", "Inv No : " + "-");
					try {
						final Charge charge = Charge.create(chargeParams);
						if(charge.getStatus().equals("succeeded"))
						{
							new Handler(Looper.getMainLooper()).post(new Runnable() {
								@Override
								public void run() {
									hideProgressDialog();
									Utils.printDebug(mContext, Str.SuccessToPay[Global.LANG]);
									//Success Process
									Global.gUARPaymentCardInfo.nTrxnAmount = amount;
									loadData();
								}
							});
						}
						else
						{
							new Handler(Looper.getMainLooper()).post(new Runnable() {
								@Override
								public void run() {
									hideProgressDialog();
									Utils.printDebug(mContext, Str.PaymentError[Global.LANG]);
									// Failed Process
								}
							});
						}
					} catch (StripeException e) {
						new Handler(Looper.getMainLooper()).post(new Runnable() {
							@Override
							public void run() {
								hideProgressDialog();
								Utils.printDebug(mContext, Str.PaymentDeclined[Global.LANG]);
								// Failed Process
							}
						});
					}
				}
			}.start();
		}
	}

	@Override
	public void onMintNotPresent() {
		if(bCard)
		{
			DbAdapter dbAdapter = new DbAdapter(mContext);
			dbAdapter.open();
			UCollectionsDInfo uCollectionsDInfo = null;
			List<UCollectionsDInfo> uCollectionsDInfos = dbAdapter.getUCollectionsDList(Global.gCurCustomerInfo.nCustNo, Global.gCurCustomerInfo.nChainNo);
			for(int i = uCollectionsDInfos.size() - 1; i >= 0; i--)
			{
				if(uCollectionsDInfos.get(i).nPayType == Global.COLLECTION_CARD)
				{
					uCollectionsDInfo = uCollectionsDInfos.get(i);
					break;
				}
			}
			dbAdapter.close();

			try {
				final MintPaymentDialog dlg = new MintPaymentDialog(mContext);
				if( uCollectionsDInfo != null )
					dlg.mAmount = uCollectionsDInfo.nTrxnAmount;
				else
					dlg.mAmount = 0;
				dlg.setCancelable(false);
				dlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						if (dlg.mReturnCode == Global.DLG_RETURN_RIGHT) {
//								Global.gUARPaymentCardInfo.nTrxnAmount += dlg.mAmount;
							Global.gUARPaymentCardInfo.nTrxnAmount = dlg.mAmount;
							loadData();
						} else if (dlg.mReturnCode == Global.DLG_RETURN_MIDDLE) {
							Global.gUARTotalInfo.nTotalCash = mCash;
							Global.gUARPaymentChequeInfo.nTrxnAmount = mCheque;
							Global.gUARPaymentCardInfo.nTrxnAmount = mCard;
							Global.gUARPaymentEFTInfo.nTrxnAmount = mEFT;
							CardDetailFragment fragment = new CardDetailFragment();
							Bundle param = new Bundle();
							param.putBoolean(Global.EXTRA_IS_AUTO_COLLECT, true);
							fragment.setArguments(param);
							mContext.replaceFragment(fragment);
						}
					}
				});
				dlg.show();
			}catch (Exception e)
			{
				e.printStackTrace();
			}

		}
		else
		{
			try {
				final MintPaymentDialog dlg = new MintPaymentDialog(mContext);
				dlg.mAmount = Global.gUARPaymentCardInfo.nTrxnAmount;
				dlg.setCancelable(false);
				dlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						if (dlg.mReturnCode == Global.DLG_RETURN_RIGHT) {
//								Global.gUARPaymentCardInfo.nTrxnAmount += dlg.mAmount;
							Global.gUARPaymentCardInfo.nTrxnAmount = dlg.mAmount;
							loadData();
						} else if (dlg.mReturnCode == Global.DLG_RETURN_MIDDLE) {
							Global.gUARTotalInfo.nTotalCash = mCash;
							Global.gUARPaymentChequeInfo.nTrxnAmount = mCheque;
							Global.gUARPaymentCardInfo.nTrxnAmount = mCard;
							Global.gUARPaymentEFTInfo.nTrxnAmount = mEFT;
							mContext.replaceFragment(new CardDetailFragment());
						}
					}
				});
				dlg.show();
			}catch (Exception e)
			{
				e.printStackTrace();
			}

		}
	}

	@Override
	public void onMintCardPresent(final boolean eWay) {
		String mintApiKey = Utils.getStringSetting(mContext, Global.PREF_MINTAPI);
		String mintSecretKey = Utils.getStringSetting(mContext, Global.PREF_MINTSECRET);
		if(!TextUtils.isEmpty(mintApiKey) && !TextUtils.isEmpty(mintSecretKey))
		{
			if(bCard) {
				DbAdapter dbAdapter = new DbAdapter(mContext);
				dbAdapter.open();
				UCollectionsDInfo uCollectionsDInfo = null;
				List<UCollectionsDInfo> uCollectionsDInfos = dbAdapter.getUCollectionsDList(Global.gCurCustomerInfo.nCustNo, Global.gCurCustomerInfo.nChainNo);
				for (int i = uCollectionsDInfos.size() - 1; i >= 0; i--) {
					if (uCollectionsDInfos.get(i).nPayType == Global.COLLECTION_CARD) {
						uCollectionsDInfo = uCollectionsDInfos.get(i);
						break;
					}
				}
				dbAdapter.close();

				mAmount = 0;
				if( uCollectionsDInfo != null )
					mAmount = uCollectionsDInfo.nTrxnAmount;

				if(mAmount != 0)
				{
					mContext.initMintegrate();
					mTrxnNo = System.currentTimeMillis();
					mContext.doMintPayment(String.valueOf(Global.gCurCustomerInfo.nCustNo), Math.round(mAmount * 100), mTrxnNo);
				}
				else
				{
					if(eWay)
					{
						onEwayPayment();
					}
					else
					{
						onMintNotPresent();
					}
				}
			}
			else
			{
				mAmount = mTotalRemaining;
				if(mAmount != 0)
				{
					mContext.initMintegrate();
					mTrxnNo = System.currentTimeMillis();
					mContext.doMintPayment(String.valueOf(Global.gCurCustomerInfo.nCustNo), Math.round(mAmount * 100), mTrxnNo);
				}
				else
				{
					if(eWay)
					{
						onEwayPayment();
					}
					else
					{
						onMintNotPresent();
					}
				}
			}

		}
		else
		{
			if(eWay)
			{
				onEwayPayment();
			}
			else
			{
				onMintNotPresent();
			}

		}
	}

	public void doProcessMintCard(MTGAppSwitchResponse response)
	{
		if(response != null)
		{
			String error = response.getErrorMessage();
			if(!TextUtils.isEmpty(error))
			{
//				MintCardProcDialog dialog = new MintCardProcDialog(mContext, DeliverySummaryFragment.this);
//				dialog.show();
				if(!mContext.isFinishing() && !mContext.isDestroyed())
				{
					try {
						Toast.makeText(mContext, error, Toast.LENGTH_LONG).show();
					}catch (Exception e1)
					{
						e1.printStackTrace();
					}
				}


			}
			else
			{
				Global.gUARPaymentCardInfo.nTrxnAmount = mAmount;
				loadData();
			}
		}
		else
		{
//			MintCardProcDialog dialog = new MintCardProcDialog(mContext, DeliverySummaryFragment.this);
//			dialog.show();
			if(!mContext.isFinishing() && !mContext.isDestroyed())
			{
				try {
					Toast.makeText(mContext, Str.Unknown_Error[Global.LANG], Toast.LENGTH_LONG).show();
				}catch (Exception e1)
				{
					e1.printStackTrace();
				}
			}

		}
	}

	private void createPdf()
	{
		showProgressDialog(Str.CreatingPdf[Global.LANG]);
		new Thread()
		{
			@Override
			public void run() {
				mStrPdfFileName = getPDFFileName();
				Global.gUARTotalInfo.strDocNo = mStrPdfFileName.substring(0, mStrPdfFileName.length() - 4);
				Global.gUARTotalInfo.nTotalCash = mCash;

				//Register Print document list.
				PrintItem printItem = new PrintItem();
				printItem.nTrxnNo = Global.gUARTotalInfo.nTrxnNo;
				printItem.strDocNo = Global.gUARTotalInfo.strDocNo;
				printItem.strDocType = Global.gUARTotalInfo.strDocType;
				printItem.strFilePath = Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + mStrPdfFileName;
				printItem.nCustNo = Global.gCurCustomerInfo.nCustNo;
				printItem.nChainNo = Global.gCurCustomerInfo.nChainNo;
				printItem.strCustomerName = Global.gCurCustomerInfo.strName;
				printItem.nInvoiceAmount = Global.gUARTotalInfo.nTotalCash + Global.gUARPaymentChequeInfo.nTrxnAmount + Global.gUARPaymentCardInfo.nTrxnAmount + Global.gUARPaymentEFTInfo.nTrxnAmount;
				DbAdapter dbAdapter = new DbAdapter(mContext);
				dbAdapter.open();
				dbAdapter.insertPrint(printItem);
				dbAdapter.close();

				PrintEngine printEngine = new PrintEngine();
				mHoneywellPrintEngine = printEngine;

				printEngine.PrepareCashPrint(mContext, mStrPhotoPathList);
				String strHoneywellFileName = Utils.getFinalPrintTemplateFileNameForHoneywell(mContext, Global.PRINT_RECEIPT_FILENAME);
				mHoneywellTemplatePath = Global.ROOT_PRINT_TEMPLATE_LOCCATION + strHoneywellFileName;
				printEngine.createDocument(mContext, false, Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + mStrPdfFileName, Global.CASH_PRINT);
				String strDocNo = mStrPdfFileName.substring(0, mStrPdfFileName.length() - 4);
				long transactionNo = System.currentTimeMillis();
				m_lTrxnNo = transactionNo;
				//m_lTrxnNo = setGeneralUploadData("CASH", Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo, -1, "", strDocNo);
				UTransactionsInfo uTransactionsInfo = setUTransactionsInfo(Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo, transactionNo, "FARC", mStrPdfFileName);
				setUFileTransactionInfo(uTransactionsInfo.nTrxnNo, Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo,
						m_lTrxnNo, strDocNo, "RECEIPT", "PAYMENT RECEIPT",
						uTransactionsInfo.strTrxnDate, uTransactionsInfo.strTrxnTime, mStrPdfFileName);
				//Global.makeZipFileInfoList.add(makeZipFileInfo);
				//Utils.registerUploadFile(mContext, makeZipFileInfo);
				Utils.registerUploadFileAsTemp(mContext, makeZipFileInfo);
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						hideProgressDialog();
						doLastProcess();
						if (bIsFromDeliverySummary == false) {
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
						else {
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
			discoveryHoneywellPrinters();
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
					new Handler(Looper.getMainLooper()).post(() -> {
						try {
							final MessageDialog msgDialog = new MessageDialog(mContext);
							msgDialog.setCancelable(false);
							msgDialog.mStrMessage = Str.FailedToPrint[Global.LANG] + "\n" + Str.DiscoveryPrinterAgain[Global.LANG];
							msgDialog.mStrLeft = Str.No[Global.LANG];
							msgDialog.mStrRight = Str.Yes[Global.LANG];
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
										discoveryHoneywellPrinters();
									}
								} else if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
									goBack();
								}
							});
							msgDialog.show();
						}catch (Exception e)
						{
							e.printStackTrace();
						}});

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
							discoveryHoneywellPrinters();
						}
					})
					.setCancelable(false)
					.show();
		}catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void enableCashLayout(boolean bEnable) {
		mRootView.findViewById(R.id.layoutCash).setEnabled(bEnable);
		if (bEnable) {
			((TextView)mRootView.findViewById(R.id.lblCash)).setTextColor(Color.BLACK);
			((TextView)mRootView.findViewById(R.id.lblCurrencySymbol)).setTextColor(Color.BLACK);
			((EditText)mRootView.findViewById(R.id.editCash)).setEnabled(true);
		}
		else {
			((TextView)mRootView.findViewById(R.id.lblCash)).setTextColor(Color.GRAY);
			((TextView)mRootView.findViewById(R.id.lblCurrencySymbol)).setTextColor(Color.GRAY);
			((EditText)mRootView.findViewById(R.id.editCash)).setEnabled(false);
		}
	}

	private void enableChequeLayout(boolean bEnable) {
		mRootView.findViewById(R.id.layoutCheque).setEnabled(bEnable);
		if (bEnable) {
			((TextView)mRootView.findViewById(R.id.lblCheque)).setTextColor(Color.BLACK);
			((TextView)mRootView.findViewById(R.id.txtCheque)).setTextColor(Color.BLACK);
		}
		else {
			((TextView)mRootView.findViewById(R.id.lblCheque)).setTextColor(Color.GRAY);
			((TextView)mRootView.findViewById(R.id.txtCheque)).setTextColor(Color.GRAY);
		}
	}

	private void enableEFTLayout(boolean bEnable) {
		mRootView.findViewById(R.id.layoutEFT).setEnabled(bEnable);
		if (bEnable) {
			((TextView)mRootView.findViewById(R.id.lblEFT)).setTextColor(Color.BLACK);
			((TextView)mRootView.findViewById(R.id.txtEFT)).setTextColor(Color.BLACK);
		}
		else {
			((TextView)mRootView.findViewById(R.id.lblEFT)).setTextColor(Color.GRAY);
			((TextView)mRootView.findViewById(R.id.txtEFT)).setTextColor(Color.GRAY);
		}
	}

	private void enableCardLayout(boolean bEnable) {
		mRootView.findViewById(R.id.layoutCard).setEnabled(bEnable);
		if (bEnable) {
			((TextView)mRootView.findViewById(R.id.lblCard)).setTextColor(Color.BLACK);
			((TextView)mRootView.findViewById(R.id.txtCard)).setTextColor(Color.BLACK);
		}
		else {
			((TextView)mRootView.findViewById(R.id.lblCard)).setTextColor(Color.GRAY);
			((TextView)mRootView.findViewById(R.id.txtCard)).setTextColor(Color.GRAY);
		}
	}

	private void refreshPaymentsLayout() {
		if (mCash > 0.0) {
			// should disable cheque and card
			enableChequeLayout(false);
			enableCardLayout(false);
			enableEFTLayout(false);

            enableCashLayout(!mShouldCashDisabled);
		}
		else if (mCheque > 0.0) {
			enableCashLayout(false);
			enableCardLayout(false);
			enableEFTLayout(false);

            enableChequeLayout(!mShouldChequeDisabled);
		} else if (mCard > 0.0) {
			enableCashLayout(false);
			enableChequeLayout(false);
			enableEFTLayout(false);

            enableCardLayout(!mShouldCardDisabled);
		} else if (mEFT > 0.0) {
			enableCashLayout(false);
			enableCardLayout(false);
			enableChequeLayout(false);

			enableEFTLayout(!mShouldEFTDisabled);
		}
		else {
			enableCashLayout(!mShouldCashDisabled);
			enableChequeLayout(!mShouldChequeDisabled);
			enableCardLayout(!mShouldCardDisabled);
			enableEFTLayout(!mShouldEFTDisabled);
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