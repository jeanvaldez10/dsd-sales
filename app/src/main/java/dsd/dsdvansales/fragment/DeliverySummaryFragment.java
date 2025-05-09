package dsd.dsdvansales.fragment;

import java.io.*;
import java.text.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import android.annotation.SuppressLint;
import android.app.*;
import android.bluetooth.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.*;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.eway.payment.sdk.android.RapidAPI;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
//import com.google.android.material.snackbar.Snackbar;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.mintwireless.mintmpos.integrator.utils.MTGAppSwitch;
import com.mintwireless.mintmpos.integrator.utils.MTGAppSwitchRequest;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.Customer;
import com.stripe.model.PaymentSource;

import dsd.dsdvansales.R;
import dsd.dsdvansales.activities.MintIFrameActivity;
import dsd.dsdvansales.activities.SignatureActivity;
import dsd.dsdvansales.activities.StripePaymentActivity;
import dsd.dsdvansales.activities.WindCaveActivity;
import dsd.dsdvansales.db.DbAdapter;
import dsd.dsdvansales.dialogs.ContainerTrackingDialog;
import dsd.dsdvansales.dialogs.EwayPaymentDialog;
import dsd.dsdvansales.dialogs.LongButtonsMessageDialog;
import dsd.dsdvansales.dialogs.MessageDialog;
import dsd.dsdvansales.dialogs.MintCardProcDialog;
import dsd.dsdvansales.dialogs.MintPaymentDialog;
import dsd.dsdvansales.dialogs.NewStripePaymentDialog;
import dsd.dsdvansales.dialogs.PrintPickListDialog;
import dsd.dsdvansales.dialogs.StripePaymentDialog;
import dsd.dsdvansales.dialogs.StripeSavedCardSelectDialog;
import dsd.dsdvansales.dialogs.SummeryInputDialog;
import dsd.dsdvansales.dialogs.WindCavePaymentDialog;
import dsd.dsdvansales.dialogs.WindcaveStationIDDialog;
import dsd.dsdvansales.global.BixolonPrinter;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.HoneywellLinePrinter;
import dsd.dsdvansales.global.HoneywellPrinter;
import dsd.dsdvansales.global.PrintEngine;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.model.ARHeaderInfo;
import dsd.dsdvansales.model.CustConBalInfo;
import dsd.dsdvansales.model.CustomerAddInfo;
import dsd.dsdvansales.model.CustomerInfo;
import dsd.dsdvansales.model.CustomerOrderInfo;
import dsd.dsdvansales.model.DescTypeInfo;
import dsd.dsdvansales.model.DocTextInfo;
import dsd.dsdvansales.model.EWayRefundResponse;
import dsd.dsdvansales.model.LoadBlotInfo;
import dsd.dsdvansales.model.PaymentCollectInfo;
import dsd.dsdvansales.model.PresoldOrDInfo;
import dsd.dsdvansales.model.PresoldOrHInfo;
import dsd.dsdvansales.model.PriceGrpInfo;
import dsd.dsdvansales.model.PrintTaxRateInfo;
import dsd.dsdvansales.model.ProdDetlInfo;
import dsd.dsdvansales.model.ProdLocnInfo;
import dsd.dsdvansales.model.StripeSavedCard;
import dsd.dsdvansales.model.TaxRatesInfo;
import dsd.dsdvansales.model.TransactionInfo;
import dsd.dsdvansales.model.UARPaymentInfo;
import dsd.dsdvansales.model.UARTotalInfo;
import dsd.dsdvansales.model.UCollectionsDInfo;
import dsd.dsdvansales.model.UCustomerFMInfo;
import dsd.dsdvansales.model.UFileTransactionInfo;
import dsd.dsdvansales.model.UGPSInfo;
import dsd.dsdvansales.model.UInvenDInfo;
import dsd.dsdvansales.model.UInvenHInfo;
import dsd.dsdvansales.model.UOrderDInfo;
import dsd.dsdvansales.model.UOrderHInfo;
import dsd.dsdvansales.model.UOrderStatusDInfo;
import dsd.dsdvansales.model.UOrderStatusHInfo;
import dsd.dsdvansales.model.UPromotionInfo;
import dsd.dsdvansales.model.UStockDInfo;
import dsd.dsdvansales.model.UStockHInfo;
import dsd.dsdvansales.model.UTaxInfo;
import dsd.dsdvansales.model.UTransactionsInfo;
import dsd.dsdvansales.views.AlertSingleChoiceAdapter;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

import com.mintwireless.mintmpos.integrator.utils.MTGAppSwitchResponse;
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

import org.apache.commons.net.ftp.FTPClient;

public class DeliverySummaryFragment extends BaseFragment implements View.OnClickListener, MintCardProcDialog.Callback {

	private Font fontBlack7Normal;
	private Font fontBlack7Bold;
	private Font fontBlack10Bold;
	private Font fontBlack9Bold;
	private Font fontBlack9Normal;
	private Font fontBlackUnderLine9Bold;
	private Font fontBlackUnderLine10Bold;

	private long mDate = 0;
	private boolean mIsPickup = false;
//	private String mStrPhotoPath = "";
	private List<String> mStrPhotoPathList = new ArrayList<>();
	private String mStrPdfFileName = "";
	private String mStrPdfFileName_ = null;
	private String mStrPrintPDFFileName = "";

	private long mPdfTrxnNo = 0;
	private int mDeliveredCnt = 0;
	private int mReturnsCnt = 0;
	private int mContainersCnt = 0;
	private int mBuybackCnt = 0;
	private Date mTrxnDate = new Date();

//	private List<UOrderDInfo> mContainerList = new ArrayList<>();
	private List<UOrderDInfo> mPrintList = new ArrayList<>();
	private List<DocTextInfo> mDocTextList = new ArrayList<>();
	private UInvenHInfo mUInvenHinfo = null;
	private Connection printerConnection = null;
	private String mStrMacAddress = "";
	private String mStrFriendlyName = "";
	private boolean mIsCreatedPDF = false;

	private int mPayMethod = 0;
	private CustomerInfo mInvoiceToInfo = null;
	private CustomerInfo mDeliverToInfo = null;

	private boolean mIsSignature = false;
	private boolean mIsPoRefNumberRequired = false;
	private String mStrPoRefMask = "";

	private String mStrReceiptText = "";

	private boolean m_bFutureOrder;
	private boolean m_bDetailopen = true;

	private EditText mEditOrderTemp = null;
	private String mCartonDescription = "";

	private boolean bShowedPrintPickList = false;

	private boolean bShowCase = true;

	private String strPaymentReference = "";

	private int mIntPayMethod = 0;
	private int windcavePayMethod = 0;

	private long mTrxnNo = -1;

	private boolean bReverseMode = false;
	private String strCardType = "";
	private String transaction_id = "";

	private boolean futureOrder = false;
	private PaymentCollectInfo mPaymentCollectInfo;

	private boolean bLoaded = false;
	private boolean mPassedGPSPlot = false;
	private boolean mGPSNewPickedUp = false;

	private boolean bConsignment = false;

	private Map<String, PriceGrpInfo> priceGrpInfoMap = new HashMap<>();
	private Map<String, PriceGrpInfo> altPriceGrpInfoMap = new HashMap<>();

	public static int WIND_CAVE_API_CALL = 10020;
	public static int STRIPE_PAYMENT = 10021;

	private boolean mCalculating = false;

	private boolean bPrintZeroQtyPresoldItem = false;

	private String ve = "+";

	PrintEngine mHoneywellPrintEngine = null;
	String mHoneywellTemplatePath = null;

	private boolean bCodPaymentOptionSelected = false;

	private boolean bShouldDisableCashCheque = false;

	private boolean bPartialPayment = false;
	private float fPartialPaidAmount = 0f;

	private ArrayList<ARHeaderInfo> collectionHeaderInfoList = null;
	private ArrayList<UARPaymentInfo> collectionPaymentInfoList = new ArrayList<>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootView = inflater.inflate(R.layout.fragment_delivery_summary, container, false);

		if(Global.gUOrderHInfo != null && Global.gUOrderHInfo.futureOrderMethod == 0 && Global.gUOrderHInfo.strDeliveryDate.equals(new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(new Date())))
		{
			futureOrder = false;
		}
		else
		{
			futureOrder = true;
		}

		Bundle bundle = getArguments();
		if( bundle != null )
		{
			try
			{
				bReverseMode = bundle.getBoolean(Global.EXTRA_IS_REVERSE, false);
			}catch(Exception e)
			{
				e.printStackTrace();
			}

			try
			{
				bConsignment = bundle.getBoolean(Global.EXTRA_CONSIGNMENT, false);
			}catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		if(!bLoaded)
		{
			if(Global.gCurCustomerInfo.strPayType.equals("3") || Global.gCurCustomerInfo.strPayType.equals("4") || Global.gCurCustomerInfo.strPayType.equals("5") || Global.gCurCustomerInfo.strPayType.equals("C"))
			{
				doProcessCollectionStatus();
			}

			if(Global.gCurCustomerInfo.strPayType.equals("$") ||
					Global.gCurCustomerInfo.strPayType.equals("1") ||
					Global.gCurCustomerInfo.strPayType.equals("2") ||
					Global.gCurCustomerInfo.strPayType.equals("3"))
			{
				doRemoveUnCheckORDCollection();
			}
		}

		commonInit();

		loadFont();
		bLoaded = true;

        if(!hasQuantity() && !bConsignment)
        {
        	try {
        		if(!mContext.isFinishing() && !mContext.isDestroyed())
				{
					final MessageDialog msgDialog = new MessageDialog(mContext);
					msgDialog.setCancelable(false);
					msgDialog.mStrTitle = Str.no_items_on_order[Global.LANG].toUpperCase();
					msgDialog.mStrMessage = Str.no_items_on_order_desc[Global.LANG];
					msgDialog.mStrMiddle = Str.Return[Global.LANG];
					msgDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
						@Override
						public void onDismiss(DialogInterface dialog) {
							if (msgDialog.mReturnCode == Global.DLG_RETURN_MIDDLE){
								Global.gDeliveryPickupCnt = 0;
								mContext.onBackPressed();
							}
						}
					});
					msgDialog.show();
				}
			}catch (Exception e)
			{
				e.printStackTrace();
			}
        }
        else
        {
            if(!bReverseMode)
            {
            	if(!futureOrder && !bConsignment)
				{
					if(bShowedPrintPickList == false && Utils.getStringSetting(mContext, Global.PREF_PRINT_PICK_LIST).equals("1"))
					{
						try {
							if(!mContext.isFinishing() && !mContext.isDestroyed())
							{
								PrintPickListDialog dialog = new PrintPickListDialog(mContext);
								dialog.show();
							}
						}catch (Exception e)
						{
							e.printStackTrace();
						}
						bShowedPrintPickList = true;
					}
				}

            }
            else
            {
                mRootView.findViewById(R.id.chkCard).setEnabled(false);
                mRootView.findViewById(R.id.chkCash).setEnabled(false);
                mRootView.findViewById(R.id.chkCheque).setEnabled(false);

                DbAdapter dbAdapter = new DbAdapter(mContext);
                dbAdapter.open();
                mPaymentCollectInfo = dbAdapter.getPaymentCollectInfo(Global.gUOrderReverseHInfo.strDocNo);
                dbAdapter.close();

                mPayMethod = Utils.parseIntFromString(mPaymentCollectInfo.strPayMethod);
                if(mPayMethod == 1)
                {
                    ((CheckBox)mRootView.findViewById(R.id.chkCash)).setChecked(true);
                    ((CheckBox)mRootView.findViewById(R.id.chkCheque)).setChecked(false);
                    ((CheckBox)mRootView.findViewById(R.id.chkCard)).setChecked(false);
                }
                else if(mPayMethod == 2)
                {
                    ((CheckBox)mRootView.findViewById(R.id.chkCash)).setChecked(false);
                    ((CheckBox)mRootView.findViewById(R.id.chkCheque)).setChecked(true);
                    ((CheckBox)mRootView.findViewById(R.id.chkCard)).setChecked(false);
                }
                else if(mPayMethod == 3)
                {
                    ((CheckBox)mRootView.findViewById(R.id.chkCash)).setChecked(false);
                    ((CheckBox)mRootView.findViewById(R.id.chkCheque)).setChecked(false);
                    ((CheckBox)mRootView.findViewById(R.id.chkCard)).setChecked(true);
                }
                else
                {
                    ((CheckBox)mRootView.findViewById(R.id.chkCash)).setChecked(false);
                    ((CheckBox)mRootView.findViewById(R.id.chkCheque)).setChecked(false);
                    ((CheckBox)mRootView.findViewById(R.id.chkCard)).setChecked(false);
                }
            }
        }

		return mRootView;
	}

	public void setCodPaymentOptionSelected(boolean bCodPaymentOptionSelected) {
		this.bCodPaymentOptionSelected = bCodPaymentOptionSelected;
	}

	public boolean getCodPaymentOptionSelected() {
		return this.bCodPaymentOptionSelected;
	}

	public void setPartialPayment(boolean bPartialPayment) {
		this.bPartialPayment = bPartialPayment;
	}

	public void setPartialPaidAmount(float fPartialPaidAmount) {
		this.fPartialPaidAmount = fPartialPaidAmount;
	}

	public void addCollectionHeaderInfo(ARHeaderInfo headerInfo) {
		if (collectionHeaderInfoList != null) {
			collectionHeaderInfoList.add(headerInfo);
		}
	}

	public void addCollectionPaymentInfo(UARPaymentInfo paymentInfo) {
		if (collectionPaymentInfoList != null) {
			collectionPaymentInfoList.add(paymentInfo);
		}
	}

	private boolean hasQuantity()
	{
		for(UOrderDInfo uOrderDInfo : Global.gUOrderHInfo.deliveryList)
		{
			if(uOrderDInfo.nSales != 0 || uOrderDInfo.dSales != 0) return true;
		}

		for(UOrderDInfo uOrderDInfo : Global.gUOrderHInfo.pickupList)
		{
			if(uOrderDInfo.nDumps != 0 || uOrderDInfo.dDumps !=0) return true;
		}

		for(UOrderDInfo uOrderDInfo : Global.gUOrderHInfo.containerList)
		{
			if(uOrderDInfo.nSales !=0) return true;
		}

		return false;
	}

	private void doProcessCollectionStatus()
	{
		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		List<ARHeaderInfo> arHeaderInfos = dbAdapter.getARHeaderInfo(Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo);
		for(ARHeaderInfo info : arHeaderInfos)
		{
			if( info.nProcessStatus == 3 ) //Processed
			{
				continue;
			}
			info.nProcessStatus = 2;
			dbAdapter.updateARHeaderInfo(info);
		}
		dbAdapter.close();
	}

	private void doRemoveUnCheckORDCollection()
	{
		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		List<ARHeaderInfo> arHeaderInfos = dbAdapter.getARHeaderInfo(Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo);
		for(ARHeaderInfo info : arHeaderInfos)
		{
			if( info.nProcessStatus == 3 ) //Processed
			{
				continue;
			}
			if(info.strARTrxnType.equals("ORD"))
			{
				info.nProcessStatus = 2;
			}
			dbAdapter.updateARHeaderInfo(info);
		}
		dbAdapter.close();
	}

	private void loadFont()
	{
		fontBlack7Normal		= Utils.getFont(Utils.getPrintFontFilePath(), Utils.getPrintEncoding(), Utils.getPrintEmbeded(), 7, Font.NORMAL, BaseColor.BLACK);
		fontBlack7Bold		= Utils.getFont(Utils.getPrintFontFilePath(), Utils.getPrintEncoding(), Utils.getPrintEmbeded(), 7, Font.BOLD, BaseColor.BLACK);
		fontBlack10Bold		= Utils.getFont(Utils.getPrintFontFilePath(), Utils.getPrintEncoding(), Utils.getPrintEmbeded(), 10, Font.BOLD, BaseColor.BLACK);
		fontBlack9Bold		= Utils.getFont(Utils.getPrintFontFilePath(), Utils.getPrintEncoding(), Utils.getPrintEmbeded(), 9, Font.BOLD, BaseColor.BLACK);
		fontBlack9Normal		= Utils.getFont(Utils.getPrintFontFilePath(), Utils.getPrintEncoding(), Utils.getPrintEmbeded(), 9, Font.NORMAL, BaseColor.BLACK);
		fontBlackUnderLine9Bold = Utils.getFont(Utils.getPrintFontFilePath(), Utils.getPrintEncoding(), Utils.getPrintEmbeded(), 9, Font.BOLD | Font.UNDERLINE, BaseColor.BLACK);
		fontBlackUnderLine10Bold = Utils.getFont(Utils.getPrintFontFilePath(), Utils.getPrintEncoding(), Utils.getPrintEmbeded(), 9, Font.BOLD | Font.UNDERLINE, BaseColor.BLACK);
	}

	float collectionValue = 0.0f;
	public boolean bEditInit = false;

	public boolean bPaused = true;
	public boolean bNeedBack = false;

	@Override
	public void onResume() {
		super.onResume();

		bPaused = false;

		Global.gFragIdx = Global.FRAG_DELIVERY_SUMMARY;
		String strData = Utils.getRoundedString(mContext, collectionValue);
		//if( m_bDetailopen )
		if(Global.gCurCustomerInfo.nShowPrice == 2)
		{
			collectionValue = 0f;
		}
		((EditText) mRootView.findViewById(R.id.editTotalCollection)).setText(strData);
		mAmount = collectionValue;
		setAmount();
		doRefreshPaymentMethod(strData);

		if (bShouldDisableCashCheque) {
			disableCashCheque();
		}
		//Check Mandantory of Orders Notes
		//m_bDetailopen = false;

		if( m_bProcessedMandantory == false )
		{
			if(Utils.getIntSetting(mContext, Global.PREF_SALENOTEMAN) == 1)
			{
				mRootView.findViewById(R.id.btnNote).performClick();
			}
		}
		bEditInit = true;
		m_bProcessedMandantory = true;

		if(bNeedBack)
		{
			new Handler(Looper.getMainLooper()).post(new Runnable() {
				@Override
				public void run() {
					Fragment fragment = mContext.getSupportFragmentManager().findFragmentById(R.id.frameContainer);
					int nCnt = mContext.getSupportFragmentManager().getBackStackEntryCount();
					boolean bBack = true;

					while(!(fragment instanceof InstoreFragment))
					{
						if( fragment instanceof  MakeDeliveryFragment)
						{
							((MakeDeliveryFragment)fragment).setModified(false);
						}
						mContext.onBackPressed();
						fragment = mContext.getSupportFragmentManager().findFragmentById(R.id.frameContainer);

						if(mContext.getSupportFragmentManager().getBackStackEntryCount() == nCnt)
						{
							bBack = false;
							break;
						}
					}

					if(bBack == false)
					{
						fragment = mContext.getSupportFragmentManager().findFragmentByTag(InstoreFragment.class.getName());
						if(fragment != null)
						{
							mContext.replaceFragment(fragment);
						}
					}
				}
			});

		}
	}

	public boolean m_bProcessedMandantory = false;

	@Override
	public void onPause() {
		super.onPause();
		if(!Utils.getStringSetting(mContext, Global.PREF_UNLESHED).equals("1"))
		{
			Global.gUOrderHInfo.strPOReference = ((EditText) mRootView.findViewById(R.id.editReceiptNumber)).getText().toString();
		}
		Global.gUOrderHInfo.strOrderName = ((EditText) mRootView.findViewById(R.id.editReceiverName)).getText().toString();
		Global.gUOrderHInfo.strDeliveryDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(mContext, new Date(mDate)));

		bPaused = true;
	}

	@Override
	public void initView() {
		bShowCase = Utils.getStringSetting(mContext, Global.PREF_INVENTORYUOM).equals("U") == false;
		bPrintZeroQtyPresoldItem = Utils.getStringSetting(mContext, Global.PREF_PRINT_ZERO_QTY_PRESOLD_ITEM).equals("1");
		if(Utils.getStringSetting(mContext, Global.PREF_SUMMARY_DETAIL).equals("1"))
		{
			mRootView.findViewById(R.id.layoutDetailSummary).setVisibility(View.VISIBLE);
		}
		else
		{
			mRootView.findViewById(R.id.layoutDetailSummary).setVisibility(View.GONE);
		}

		if(Utils.getIntSetting(mContext, Global.PREF_LOAD_CONTAINERS) == 0)
		{
			mRootView.findViewById(R.id.btnContainerTracking).setVisibility(View.INVISIBLE);
		}
		else
		{
			mRootView.findViewById(R.id.btnContainerTracking).setVisibility(View.VISIBLE);
			mRootView.findViewById(R.id.btnContainerTracking).setOnClickListener(this);
		}

		String strTempReqSetting = Utils.getStringSetting(mContext, Global.PREF_TEMPREQ);
		if(TextUtils.isEmpty(strTempReqSetting) || strTempReqSetting.equals("0"))
		{
			mRootView.findViewById(R.id.layoutDeliverTemp).setVisibility(View.GONE);
		}
		else
		{
			mRootView.findViewById(R.id.layoutDeliverTemp).setVisibility(View.VISIBLE);
			if(strTempReqSetting.equals("2"))
			{
				((TextView)mRootView.findViewById(R.id.lblDeliveryTemperature)).setTextColor(getResources().getColor(R.color.green_color));
				((TextView)mRootView.findViewById(R.id.lblDeliveryTemperature)).setText(Str.DeliveryTemperature[Global.LANG] + " *");
			}
			else
			{
				((TextView)mRootView.findViewById(R.id.lblDeliveryTemperature)).setTextColor(getResources().getColor(R.color.black_color));
				((TextView)mRootView.findViewById(R.id.lblDeliveryTemperature)).setText(Str.DeliveryTemperature[Global.LANG]);
			}
		}

		mRootView.findViewById(R.id.btnSignature).setOnClickListener(this);
		mRootView.findViewById(R.id.btnBack).setOnClickListener(this);
		mRootView.findViewById(R.id.btnNote).setOnClickListener(this);
		mRootView.findViewById(R.id.btnPhoto).setOnClickListener(this);
		mRootView.findViewById(R.id.btnConfirm).setOnClickListener(this);

		if(Global.gCurCustomerInfo.nShowPrice > 0)
		{
			mRootView.findViewById(R.id.layoutTotalValue).setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					if(!bConsignment)
					{
						PriceDetailsFragment fragment = new PriceDetailsFragment();
						fragment.setPreviousFragment(DeliverySummaryFragment.this);
						Bundle bundle = new Bundle();
						bundle.putBoolean(Global.EXTRA_CONSIGNMENT, bConsignment);
						fragment.setArguments(bundle);
						bEditInit = false;
						mContext.replaceFragment(fragment);
					}
					return true;
				}
			});
		}
		else
		{
			((TextView)mRootView.findViewById(R.id.lblTotalValue)).setTextColor(mContext.getResources().getColor(R.color.gray_color));
			((TextView)mRootView.findViewById(R.id.lblCurrencySymbol)).setTextColor(mContext.getResources().getColor(R.color.gray_color));
			((TextView)mRootView.findViewById(R.id.txtTotalValue)).setTextColor(mContext.getResources().getColor(R.color.gray_color));
			((TextView) mRootView.findViewById(R.id.txtOutstandingValue)).setTextColor(mContext.getResources().getColor(R.color.gray_color));
			((TextView) mRootView.findViewById(R.id.txtTotalAllValue)).setTextColor(mContext.getResources().getColor(R.color.gray_color));
		}

		if (Utils.getIntSetting(mContext, Global.PREF_LOAD_CONTAINERS) == 1) {
			mRootView.findViewById(R.id.btnContainerTracking).setEnabled(true);
		} else {
			mRootView.findViewById(R.id.btnContainerTracking).setEnabled(false);
		}

		((CheckBox) mRootView.findViewById(R.id.chkCard)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked == true) {
					((CheckBox) mRootView.findViewById(R.id.chkCheque)).setChecked(false);
					((CheckBox) mRootView.findViewById(R.id.chkCash)).setChecked(false);
					mPayMethod = 3;
				}
				else
				{
					mPayMethod = 0;
				}
			}
		});

		((CheckBox) mRootView.findViewById(R.id.chkCash)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked == true) {
					((CheckBox) mRootView.findViewById(R.id.chkCheque)).setChecked(false);
					((CheckBox) mRootView.findViewById(R.id.chkCard)).setChecked(false);
					mPayMethod = 1;
				}
				else
				{
					mPayMethod = 0;
				}
			}
		});
		((CheckBox) mRootView.findViewById(R.id.chkCheque)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked == true) {
					((CheckBox) mRootView.findViewById(R.id.chkCash)).setChecked(false);
					((CheckBox) mRootView.findViewById(R.id.chkCard)).setChecked(false);
					mPayMethod = 2;
				}
				else
				{
					mPayMethod = 0;
				}
			}
		});

		if( Utils.getStringSetting(mContext, Global.PREF_HIDE_SUMMARY_PAYMENT).equals("1")
				/* || (!Global.gCurCustomerInfo.strPayType.trim().equals("1") && !Global.gCurCustomerInfo.strPayType.trim().equals("2")
						&& Utils.getStringSetting(mContext, Global.PREF_ROUTE_PAYMENTADJUST).trim().equals("1"))*/)
		{
			mRootView.findViewById(R.id.chkCard).setEnabled(false);
			((CheckBox)mRootView.findViewById(R.id.chkCard)).setTextColor(Color.GRAY);
			mRootView.findViewById(R.id.chkCash).setEnabled(false);
			((CheckBox)mRootView.findViewById(R.id.chkCash)).setTextColor(Color.GRAY);
			mRootView.findViewById(R.id.chkCheque).setEnabled(false);
			((CheckBox)mRootView.findViewById(R.id.chkCheque)).setTextColor(Color.GRAY);
			((TextView)mRootView.findViewById(R.id.lblTotalCollection)).setTextColor(mContext.getResources().getColor(R.color.gray_color));
			((EditText)mRootView.findViewById(R.id.editTotalCollection)).setEnabled(false);

			if(Global.gCurCustomerInfo.nShowPrice == 2)
			{
				((TextView)mRootView.findViewById(R.id.lblFullPaymentBy)).setTextColor(Color.GRAY);
				((TextView)mRootView.findViewById(R.id.lblTerms)).setTextColor(Color.GRAY);
				((TextView)mRootView.findViewById(R.id.lblCurrencySymbol)).setTextColor(Color.GRAY);
			}
			else
			{
				((TextView)mRootView.findViewById(R.id.lblFullPaymentBy)).setTextColor(Color.BLACK);
				((TextView)mRootView.findViewById(R.id.lblTerms)).setTextColor(Color.BLACK);
				((TextView)mRootView.findViewById(R.id.lblCurrencySymbol)).setTextColor(Color.BLACK);
			}
			
			if(Utils.getStringSetting(mContext, Global.PREF_HIDE_SUMMARY_PAYMENT).equals("1"))
			{
				mRootView.findViewById(R.id.layoutTotalCollection).setVisibility(View.GONE);
			}
			else
			{
				mRootView.findViewById(R.id.layoutTotalCollection).setVisibility(View.VISIBLE);
			}
		}
		else
		{
			if(Global.gCurCustomerInfo.nShowPrice == 2)
			{
				mRootView.findViewById(R.id.chkCard).setEnabled(false);
				((CheckBox)mRootView.findViewById(R.id.chkCard)).setTextColor(Color.GRAY);
				mRootView.findViewById(R.id.chkCash).setEnabled(false);
				((CheckBox)mRootView.findViewById(R.id.chkCash)).setTextColor(Color.GRAY);
				mRootView.findViewById(R.id.chkCheque).setEnabled(false);
				((CheckBox)mRootView.findViewById(R.id.chkCheque)).setTextColor(Color.GRAY);
				((TextView)mRootView.findViewById(R.id.lblTotalCollection)).setTextColor(mContext.getResources().getColor(R.color.gray_color));
				((EditText)mRootView.findViewById(R.id.editTotalCollection)).setEnabled(false);

				((TextView)mRootView.findViewById(R.id.lblFullPaymentBy)).setTextColor(Color.GRAY);
				((TextView)mRootView.findViewById(R.id.lblTerms)).setTextColor(Color.GRAY);
				((TextView)mRootView.findViewById(R.id.lblCurrencySymbol)).setTextColor(Color.GRAY);
			}
			else
			{
			    if(Utils.getIntSetting(mContext, Global.PREF_EWAY_CARD_PROC) != 0)
                {
                    mRootView.findViewById(R.id.chkCard).setEnabled(true);
                    ((CheckBox)mRootView.findViewById(R.id.chkCard)).setTextColor(Color.BLACK);
                }
                else
                {
                    mRootView.findViewById(R.id.chkCard).setEnabled(false);
                    ((CheckBox)mRootView.findViewById(R.id.chkCard)).setTextColor(Color.GRAY);
                }

				mRootView.findViewById(R.id.chkCash).setEnabled(true);
				((CheckBox)mRootView.findViewById(R.id.chkCash)).setTextColor(Color.BLACK);
				mRootView.findViewById(R.id.chkCheque).setEnabled(true);
				((CheckBox)mRootView.findViewById(R.id.chkCheque)).setTextColor(Color.BLACK);
				((TextView)mRootView.findViewById(R.id.lblTotalCollection)).setTextColor(mContext.getResources().getColor(R.color.black_color));
				((EditText)mRootView.findViewById(R.id.editTotalCollection)).setEnabled(true);

				((EditText)mRootView.findViewById(R.id.editTotalCollection)).addTextChangedListener(new TextWatcher() {
					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count) {
					}

					@Override
					public void beforeTextChanged(CharSequence s, int start, int count, int after) {
					}

					@Override
					public void afterTextChanged(Editable s) {
						if (((EditText)mRootView.findViewById(R.id.editTotalCollection)).getTag() == null) {
							setFormattedAmount(s.toString());
						}
						String strText = ((EditText)mRootView.findViewById(R.id.editTotalCollection)).getText().toString();
						doRefreshPaymentMethod(strText);
					}
				});
				((TextView)mRootView.findViewById(R.id.lblFullPaymentBy)).setTextColor(Color.BLACK);
				((TextView)mRootView.findViewById(R.id.lblTerms)).setTextColor(Color.BLACK);
				((TextView)mRootView.findViewById(R.id.lblCurrencySymbol)).setTextColor(Color.BLACK);
			}
		}

		if (Global.gCurCustomerInfo.nInvoiceFmt == 0) {
			if( getArguments() != null ) { mIsPickup = getArguments().getBoolean(Global.EXTRA_IS_PICKUP); }
		}
		
		if( Global.gCurCustomerInfo.strSignature.trim().equals("Y") || Global.gCurCustomerInfo.strSignature.trim().equals("y") )
		{
			mIsSignature = true;
		}
		else
		{
			mIsSignature = false;
		}

		if(Global.gCurCustomerInfo.strPONumberRequired.trim().equals("1"))
		{
			mIsPoRefNumberRequired = true;
		}
		else
		{
			mIsPoRefNumberRequired = false;
		}

		mStrPoRefMask = Global.gCurCustomerInfo.strPONumberMask;
		if(!TextUtils.isEmpty(mStrPoRefMask))
		{
			mIsPoRefNumberRequired = true;
		}

		((TextView) mRootView.findViewById(R.id.txtCustomerName)).setText(Global.gCurCustomerInfo.strName);
		if(Global.gCurCustomerInfo.nShowPrice != 2)
		{
			mRootView.findViewById(R.id.btnDetail).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					DetailCollectionFragment fragment = new DetailCollectionFragment();
					fragment.setPreviousFragment(DeliverySummaryFragment.this);
					Bundle args = new Bundle();
					args.putBoolean(Global.EXTRA_CONSIGNMENT, bConsignment);
					args.putBoolean(Global.EXTRA_IS_FROM_DELIVERY_SUMMARY, false);
					fragment.setArguments(args);
					bEditInit = false;
					mContext.replaceFragment(fragment);
				}
			});
		}
		else
		{
			mRootView.findViewById(R.id.btnDetail).setOnClickListener(null);
		}

		((EditText) mRootView.findViewById(R.id.editTotalCollection)).removeTextChangedListener(mTotalCollectionChangeListener);
		((EditText) mRootView.findViewById(R.id.editTotalCollection)).addTextChangedListener(mTotalCollectionChangeListener);

		if(!bLoaded)
		{
			if(Global.gCurCustomerInfo.fHandlingFee != 0)
			{
				calculateTotal();
				if(Global.gUOrderHInfo.nSaleAmount < Global.gCurCustomerInfo.fMiniumOrderValue)
				{
					try {
						if(mContext.isFinishing() || mContext.isDestroyed()) return;
						final MessageDialog msgDialog = new MessageDialog(mContext);
						msgDialog.setCancelable(false);
						msgDialog.mStrTitle = Str.DeliveryCharge[Global.LANG];

						if(Global.gUOrderHInfo.nHandlingFee == 0)
						{
							//Apply the Handling fee first.
							loadData();
							doApplyHandlingFee();
							refreshLanguage();

							msgDialog.mStrRight = Str.Proceed[Global.LANG];
							msgDialog.mStrLeft = Str.Remove[Global.LANG];
							msgDialog.mStrMessage = String.format(Locale.ENGLISH, Str.ThisInvoiceIsThanTheMinumumOf[Global.LANG], Global.gCurrencySymbol, Global.gCurCustomerInfo.fMiniumOrderValue, Global.gCurrencySymbol, Global.gCurCustomerInfo.fHandlingFee);
							msgDialog.setOnDismissListener(dialog -> {
								if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT){
									Global.gUOrderHInfo.nHandlingFee = 0;
									Global.gUOrderHInfo.nHandlingFeeTaxAmount = 0;
									Global.gUOrderHInfo.strHandlingTaxCode = "";
									loadData();
									refreshLanguage();
								}
							});
						}
						else
						{
							msgDialog.mStrRight = Str.No[Global.LANG];
							msgDialog.mStrLeft = Str.Yes[Global.LANG];
							msgDialog.mStrMessage = String.format(Locale.ENGLISH, Str.ADeliveryFeeHasAlreadyBeenAppliedToThisInvoice[Global.LANG]);
							msgDialog.setOnDismissListener(dialog -> {
								if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT){
									loadData();
									doApplyHandlingFee();
									refreshLanguage();
								}
								else{
									Global.gUOrderHInfo.nHandlingFee = 0;
									Global.gUOrderHInfo.nHandlingFeeTaxAmount = 0;
									Global.gUOrderHInfo.strHandlingTaxCode = "";
									loadData();
									refreshLanguage();
								}
							});
						}

						msgDialog.show();
					}catch (Exception e)
					{
						e.printStackTrace();
					}

					return;
				}
				else
				{
					if(Global.gUOrderHInfo.nHandlingFee != 0)
					{
						try {
							if(!mContext.isFinishing() && !mContext.isDestroyed())
							{
								final MessageDialog msgDialog = new MessageDialog(mContext);
								msgDialog.setCancelable(false);
								msgDialog.mStrTitle = Str.DeliveryCharge[Global.LANG];
								msgDialog.mStrRight = Str.Continue[Global.LANG];
								msgDialog.mStrMessage = String.format(Locale.ENGLISH, String.format(Locale.ENGLISH, Str.ThisOrderHasApplied[Global.LANG], Global.gCurrencySymbol, Global.gCurCustomerInfo.fMiniumOrderValue));
								msgDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
									@Override
									public void onDismiss(DialogInterface dialog) {
										if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT){
											Global.gUOrderHInfo.nHandlingFee = 0;
											Global.gUOrderHInfo.nHandlingFeeTaxAmount = 0;
											Global.gUOrderHInfo.strHandlingTaxCode = "";
											loadData();
											refreshLanguage();
										}
									}
								});
								msgDialog.show();
							}

						}catch (Exception e)
						{
							e.printStackTrace();
						}
						return;
					}
					else
					{
						Global.gUOrderHInfo.nHandlingFee = 0;
						Global.gUOrderHInfo.nHandlingFeeTaxAmount = 0;
						Global.gUOrderHInfo.strHandlingTaxCode = "";
					}
				}
			}
		}

		if(Utils.getStringSetting(mContext, Global.PREF_UNLESHED).equals("1"))
		{
			mRootView.findViewById(R.id.editReceiptNumber).setEnabled(false);
			mRootView.findViewById(R.id.editReceiptNumber).setAlpha(0.5f);
		}
		else
		{
			mRootView.findViewById(R.id.editReceiptNumber).setEnabled(true);
		}

		String strTempFmt = Utils.getStringSetting(mContext, Global.PREF_TEMPFMT);
		if(strTempFmt.startsWith("-"))
		{
			ve = "-";
		}
		else {
			ve = "+";
		}

		mRootView.findViewById(R.id.plus).setOnClickListener(view -> {
			ve = "+";
			String strTemp = mEditOrderTemp.getText().toString();
			if(!strTemp.startsWith("+"))
			{
				mEditOrderTemp.setText(ve + strTemp.substring(1));
			}
		});
		mRootView.findViewById(R.id.minus).setOnClickListener(view -> {
			ve = "-";
			String strTemp = mEditOrderTemp.getText().toString();
			if(!strTemp.startsWith("-"))
			{
				mEditOrderTemp.setText(ve + strTemp.substring(1));
			}
		});
		mEditOrderTemp = mRootView.findViewById(R.id.editDeliverTemp);
		mEditOrderTemp.setText(ve + "0.0");
		mEditOrderTemp.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				if (mEditOrderTemp.getTag() == null) {
					setOrderFormattedAmount(s.toString());
				}
			}
		});
		mEditOrderTemp.post(new Runnable() {
			public void run() {
				mEditOrderTemp.setSelection(mEditOrderTemp.getText().toString().length());
				mEditOrderTemp.postDelayed(this, 100);
			}
		});

		loadData();
		refreshLanguage();
	}

	private void setOrderFormattedAmount(String s)
	{
		String strArray[] = s.substring(1).split("\\.");
		String strVal = "";
		for (int i=0; i<strArray.length; i++) {
			strVal += strArray[i];
		}
		float value =  (Utils.parseFloatFromString(strVal) / 10f);

		mEditOrderTemp.setTag("From App");
		String strCash = String.format(Locale.ENGLISH, "%s%.1f", ve, value);
		mEditOrderTemp.setText(strCash);
		mEditOrderTemp.setSelection(mEditOrderTemp.getText().toString().length());
		mEditOrderTemp.setTag(null);
	}

	private void doRefreshPaymentMethod(String strText)
	{
		/*if( Utils.getStringSetting(mContext, Global.PREF_HIDE_SUMMARY_PAYMENT).equals("1") || Global.gCurCustomerInfo.nShowPrice == 2 ||
				(!Global.gCurCustomerInfo.strPayType.trim().equals("1") && !Global.gCurCustomerInfo.strPayType.trim().equals("2")
						&& Utils.getStringSetting(mContext, Global.PREF_ROUTE_PAYMENTADJUST).trim().equals("1")))
		{
			return;
		}*/

		if( Utils.getStringSetting(mContext, Global.PREF_HIDE_SUMMARY_PAYMENT).equals("1") || Global.gCurCustomerInfo.nShowPrice == 2 ||
				/*Utils.getStringSetting(mContext, Global.PREF_ROUTE_PAYMENTADJUST).trim().equals("1") || */
				Global.gCurCustomerInfo.strPayType.trim().equals("1") || Global.gCurCustomerInfo.strPayType.trim().equals("2") || Global.gCurCustomerInfo.strPayType.trim().equals("$"))
		{
			return;
		}
		if(Utils.parseFloatFromString(strText) == 0.0f)
		{
			mRootView.findViewById(R.id.chkCard).setEnabled(false);
			mRootView.findViewById(R.id.chkCheque).setEnabled(false);
			mRootView.findViewById(R.id.chkCash).setEnabled(false);
			((CheckBox)mRootView.findViewById(R.id.chkCard)).setTextColor(mContext.getResources().getColor(R.color.gray_color));
			((CheckBox)mRootView.findViewById(R.id.chkCash)).setTextColor(mContext.getResources().getColor(R.color.gray_color));
			((CheckBox)mRootView.findViewById(R.id.chkCheque)).setTextColor(mContext.getResources().getColor(R.color.gray_color));
		}
		else
		{
			boolean bEnable = !bReverseMode /*&& Utils.getIntSetting(mContext, Global.PREF_EWAY_CARD_PROC) != 0*/;
			//bEnable = bEnable && (Global.gCurCustomerInfo.strPayType.trim().equals("1") || Global.gCurCustomerInfo.strPayType.trim().equals("2") || Utils.getStringSetting(mContext, Global.PREF_ROUTE_PAYMENTADJUST).trim().equals("1"));
			bEnable = bEnable && (Global.gCurCustomerInfo.nShowPrice != 2);
			bEnable = bEnable && (!Global.gCurCustomerInfo.strPayType.equals("0") && Global.gCurCustomerInfo.strPayType.length() != 0 );
			String strValueText = ((TextView) mRootView.findViewById(R.id.txtTotalAllValue)).getText().toString();
			float fValue = Utils.getFloatFromString(strValueText.substring(Global.gCurrencySymbol.length()));
			bEnable = bEnable && (fValue != 0.0f);

			if(bEnable)
			{
				if(Utils.getIntSetting(mContext, Global.PREF_EWAY_CARD_PROC) != 0)
				{
					mRootView.findViewById(R.id.chkCard).setEnabled(true);
					((CheckBox)mRootView.findViewById(R.id.chkCard)).setTextColor(mContext.getResources().getColor(R.color.black_color));
				}
				else
				{
					mRootView.findViewById(R.id.chkCard).setEnabled(false);
					((CheckBox)mRootView.findViewById(R.id.chkCard)).setTextColor(mContext.getResources().getColor(R.color.gray_color));
				}
				mRootView.findViewById(R.id.chkCheque).setEnabled(true);
				mRootView.findViewById(R.id.chkCash).setEnabled(true);

				((CheckBox)mRootView.findViewById(R.id.chkCash)).setTextColor(mContext.getResources().getColor(R.color.black_color));
				((CheckBox)mRootView.findViewById(R.id.chkCheque)).setTextColor(mContext.getResources().getColor(R.color.black_color));
			}
		}
	}

	private void doApplyHandlingFee()
	{
		Global.gUOrderHInfo.strHandlingTaxCode = Utils.getStringSetting(mContext, Global.PREF_HANDLING_FEE_TAX_CODE);
		Global.gUOrderHInfo.nHandlingFee = Global.gCurCustomerInfo.fHandlingFee;

		if(Global.gUOrderHInfo.strHandlingTaxCode.length() > 0)
		{
			DbAdapter dbAdapter = new DbAdapter(mContext);
			dbAdapter.open();
			TaxRatesInfo taxRatesInfo = dbAdapter.getTaxRatesInfo(Global.gUOrderHInfo.strHandlingTaxCode);
			dbAdapter.close();

			if( taxRatesInfo != null){
				if(taxRatesInfo.nTaxMethod == 1) //Applying Cents Amount
				{
					Global.gUOrderHInfo.nHandlingFeeTaxAmount = Utils.getDividedValue(taxRatesInfo.nTaxRate) * Global.gUOrderHInfo.nHandlingFee;
				}
				else if( taxRatesInfo.nTaxMethod == 2 )
				{
					Global.gUOrderHInfo.nHandlingFeeTaxAmount = Global.gUOrderHInfo.nHandlingFee / 100 * Utils.getDividedValue(taxRatesInfo.nTaxRate);
				}
			}
		}
        if(Global.gCurHeaderInfo != null)
        {
            Global.gCurHeaderInfo.nTrxnAmount = Math.round(getRoundedValue(Global.gUOrderHInfo.nSaleAmount + Global.gUOrderHInfo.nTaxAmount + Global.gUOrderHInfo.nHandlingFee + Global.gUOrderHInfo.nHandlingFeeTaxAmount) * Global.MULTIPLE_VALUE);
        }
		/*float fSales = Utils.getFloatSetting(mContext, Global.SALE_TODAY) + Global.gUOrderHInfo.nHandlingFee;
		Utils.setFloatSetting(mContext, Global.SALE_TODAY, fSales);*/
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

	public void setResetCollectionValue()
	{
		//Recalculate the tax amount for each item
		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		if( Global.gUOrderHInfo != null && Global.gUOrderHInfo.deliveryList != null && Global.gUOrderHInfo.deliveryList.size() > 0 ) {
			for (int i = 0; i < Global.gUOrderHInfo.deliveryList.size(); i++) {
				UOrderDInfo info = Global.gUOrderHInfo.deliveryList.get(i);
				ProdLocnInfo prodLocnInfo = dbAdapter.getProdLocnInfo(info.strItemNo);
				ProdDetlInfo prodDetlInfo = dbAdapter.getProdDetl(info.strItemNo);
				info.prodLocnInfo = prodLocnInfo;
				if (prodLocnInfo != null && prodLocnInfo.strTaxFlag.equals("Y")) {
					info.strItemTaxCode = prodLocnInfo.strItemTaxCode;
				}

				info.uTaxInfoList.clear();
				//Calculation Tax Amount
				List<TaxRatesInfo> taxRatesInfoList = dbAdapter.getTaxRatesInfoList(Global.gCurCustomerInfo.strTaxCode, info.strItemNo);
				info.nTrxnNo = Global.gUOrderHInfo.nTrxnNo;
				info.nTaxAmount = 0;
				info.nPrintTaxAmount = 0;
				info.nPrintLineTaxAmount = 0;
				info.nPrintCDSAmount = 0;
				boolean bAppliedGST = false;
				float fGSTOnCDS = 0.0f;
				List<UTaxInfo> gstOnCDSTaxRateInfoList = new ArrayList<>();
				for(TaxRatesInfo taxRatesInfo : taxRatesInfoList)
				{
					float nTaxAmount = 0;
					float nPrintTaxAmount = 0;
					float nPrintLineTaxAmount = 0;
					if(taxRatesInfo.nTaxMethod == 1) //Applying Cents Amount
					{
						if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
						{
							float dSales;
							if(bConsignment)
							{
								dSales = (float)(info.dPlanQty - info.dSales);
							}
							else
							{
								dSales = (float)info.dSales;
							}
							nTaxAmount = Utils.getDividedValue(taxRatesInfo.nTaxRate) * dSales * prodDetlInfo.nConsumerUnit ;
							nPrintTaxAmount = Utils.getDividedValue(taxRatesInfo.nTaxRate) * dSales * prodDetlInfo.nConsumerUnit ;
							nPrintLineTaxAmount = Utils.getDividedValue(taxRatesInfo.nTaxRate) * prodDetlInfo.nConsumerUnit ;
						}
						else
						{
							int nSales;
							if(bConsignment)
							{
								nSales = info.nPlanQty - info.nSales;
							}
							else
							{
								nSales = info.nSales;
							}
							nTaxAmount = Utils.getDividedValue(taxRatesInfo.nTaxRate) * nSales * prodDetlInfo.nConsumerUnit ;
							nPrintTaxAmount = Utils.getDividedValue(taxRatesInfo.nTaxRate) * nSales * prodDetlInfo.nConsumerUnit ;
							nPrintLineTaxAmount = Utils.getDividedValue(taxRatesInfo.nTaxRate) * prodDetlInfo.nConsumerUnit ;
						}

					}
					else if( taxRatesInfo.nTaxMethod == 2 )
					{
						if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
						{
							float dSales;
							if(bConsignment)
							{
								dSales = (float)(info.dPlanQty - info.dSales);
							}
							else
							{
								dSales = (float)info.dSales;
							}
							if(!bConsignment)
							{
								nTaxAmount = (Utils.getRoundedValue(dSales *(info.nBasePrice - info.nDiscountVal)) + info.nTaxAmount) / 100 * Utils.getDividedValue(taxRatesInfo.nTaxRate);
							}
							else
							{
								nTaxAmount = (Utils.getRoundedValue(dSales *info.nPrice) + info.nTaxAmount) / 100 * Utils.getDividedValue(taxRatesInfo.nTaxRate);
							}
							nPrintTaxAmount = (Utils.getRoundedValue(dSales * info.nBasePrice) + info.nTaxAmount) / 100 * Utils.getDividedValue(taxRatesInfo.nTaxRate);
							nPrintLineTaxAmount = (Utils.getRoundedValue(info.nBasePrice) + info.nPrintLineTaxAmount) / 100 * Utils.getDividedValue(taxRatesInfo.nTaxRate);
						}
						else
						{
							int nSales;
							if(bConsignment)
							{
								nSales = info.nPlanQty - info.nSales;
							}
							else
							{
								nSales = info.nSales;
							}
							if(!bConsignment)
							{
								nTaxAmount = (Utils.getRoundedValue(nSales *(info.nBasePrice - info.nDiscountVal)) + info.nTaxAmount) / 100 * Utils.getDividedValue(taxRatesInfo.nTaxRate);
							}
							else
							{
								nTaxAmount = (Utils.getRoundedValue(nSales *info.nPrice) + info.nTaxAmount) / 100 * Utils.getDividedValue(taxRatesInfo.nTaxRate);
							}

							nPrintTaxAmount = (Utils.getRoundedValue(nSales * info.nBasePrice) + info.nTaxAmount) / 100 * Utils.getDividedValue(taxRatesInfo.nTaxRate);
							nPrintLineTaxAmount = (Utils.getRoundedValue(info.nBasePrice) + info.nPrintLineTaxAmount) / 100 * Utils.getDividedValue(taxRatesInfo.nTaxRate);
						}

					}

					if(taxRatesInfo.strTaxType.equals("S"))
					{
						ProdLocnInfo locnInfo = dbAdapter.getProdLocnInfo(taxRatesInfo.strTaxRateNumber);
						List<TaxRatesInfo> gstOnCDSRatesInfoList = dbAdapter.getTaxRatesInfoList(Global.gCurCustomerInfo.strTaxCode, taxRatesInfo.strTaxRateNumber);
						for(TaxRatesInfo gstOnCDSRatesInfo : gstOnCDSRatesInfoList)
						{
							if(gstOnCDSRatesInfo.nTaxMethod == 2 && gstOnCDSRatesInfo.strTaxRateCode.equals("GST") )
							{
								float nGSTOnCDSTaxAmount;
								if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
								{
									nGSTOnCDSTaxAmount = Utils.getDividedValue(taxRatesInfo.nTaxRate) / 100 * Utils.getDividedValue(gstOnCDSRatesInfo.nTaxRate) * ((float)info.dSales) * prodDetlInfo.nConsumerUnit;
								}
								else
								{
									nGSTOnCDSTaxAmount = Utils.getDividedValue(taxRatesInfo.nTaxRate) / 100 * Utils.getDividedValue(gstOnCDSRatesInfo.nTaxRate) * info.nSales * prodDetlInfo.nConsumerUnit;
								}

								fGSTOnCDS += nGSTOnCDSTaxAmount;

								UTaxInfo uTaxInfo = new UTaxInfo();
								uTaxInfo.nTrxnNo = info.nTrxnNo;
								uTaxInfo.nTrxnType = info.nTrxnType;
								uTaxInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
								uTaxInfo.nLocnNo = locnInfo.nLocnNo;
								uTaxInfo.strItemNo = taxRatesInfo.strTaxRateNumber;
								uTaxInfo.strTaxRateCode = gstOnCDSRatesInfo.strTaxRateCode;
								uTaxInfo.nTaxAmount = Utils.get2DecimalValue(nGSTOnCDSTaxAmount);
								uTaxInfo.strCumulativeFlag = gstOnCDSRatesInfo.strCumulativeFlag;
								uTaxInfo.nTaxRate = gstOnCDSRatesInfo.nTaxRate;
								uTaxInfo.strTaxRateNumber = gstOnCDSRatesInfo.strTaxRateNumber;
								uTaxInfo.nReasonCode = info.nReasonCode;
								gstOnCDSTaxRateInfoList.add(uTaxInfo);
							}
						}
					}

					info.nTaxAmount += Utils.get5RoundedValue(mContext, nTaxAmount);
					info.nPrintTaxAmount += Utils.get5RoundedValue(mContext, nPrintTaxAmount);
					info.nPrintLineTaxAmount += Utils.get5RoundedValue(mContext, nPrintLineTaxAmount);

					if(taxRatesInfo.strTaxRateCode.equals("GST"))
					{
						bAppliedGST = true;
					}

					if( taxRatesInfo.strTaxType.toUpperCase().equals("S") )
					{
						info.nPrintCDSAmount += Utils.get2DecimalValue(nPrintTaxAmount);
						info.nCDSRate = taxRatesInfo.nTaxRate;
						PrintTaxRateInfo cdsAmount = Global.gUOrderHInfo.cdsMap.get(taxRatesInfo.strTaxRateCode);
						if(taxRatesInfo.strTaxRateCode.length() > 0 && !taxRatesInfo.strTaxRateCode.equals("0"))
						{
							int nSales;
							if(bConsignment)
							{
								nSales = info.nPlanQty - info.nSales;
							}
							else
							{
								nSales = info.nSales;
							}

							if( cdsAmount == null )
							{
								cdsAmount = new PrintTaxRateInfo();
								cdsAmount.nBottles = nSales * prodDetlInfo.nConsumerUnit;
								cdsAmount.strTaxCode = taxRatesInfo.strTaxRateCode;
								cdsAmount.price = Utils.getDividedValue(taxRatesInfo.nTaxRate);
								cdsAmount.strRateDescription = taxRatesInfo.strRateDescription;

								Global.gUOrderHInfo.cdsMap.put(taxRatesInfo.strTaxRateCode, cdsAmount);
							}
							else
							{
								cdsAmount.nBottles += nSales * prodDetlInfo.nConsumerUnit;
							}
						}
					}

					UTaxInfo uTaxInfo = new UTaxInfo();
					uTaxInfo.nTrxnNo = info.nTrxnNo;
					uTaxInfo.nTrxnType = info.nTrxnType;
					uTaxInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
					uTaxInfo.nLocnNo = info.nLocnNo;
					uTaxInfo.strItemNo = info.strItemNo;
					uTaxInfo.strTaxRateCode = taxRatesInfo.strTaxRateCode;
					uTaxInfo.nTaxAmount = Utils.get5RoundedValue(mContext, nTaxAmount);
					uTaxInfo.strCumulativeFlag = taxRatesInfo.strCumulativeFlag;
					uTaxInfo.nTaxRate = taxRatesInfo.nTaxRate;
					uTaxInfo.strTaxRateNumber = taxRatesInfo.strTaxRateNumber;
					uTaxInfo.nReasonCode = info.nReasonCode;
					info.uTaxInfoList.add(uTaxInfo);
				}
				if(!bAppliedGST)
				{
					info.nTaxAmount += Utils.get5RoundedValue(mContext, fGSTOnCDS);
					info.nPrintTaxAmount += Utils.get5RoundedValue(mContext, fGSTOnCDS);
					info.uTaxInfoList.addAll(gstOnCDSTaxRateInfoList);
				}

				Global.gUOrderHInfo.deliveryList.set(i, info);
			}
		}
		if( Global.gUOrderHInfo != null && Global.gUOrderHInfo.pickupList != null && Global.gUOrderHInfo.pickupList.size() > 0 ) {
			for (int i = 0; i < Global.gUOrderHInfo.pickupList.size(); i++) {
				UOrderDInfo info = Global.gUOrderHInfo.pickupList.get(i);
				ProdLocnInfo prodLocnInfo = dbAdapter.getProdLocnInfo(info.strItemNo);
				info.prodLocnInfo = prodLocnInfo;
				ProdDetlInfo prodDetlInfo = dbAdapter.getProdDetl(info.strItemNo);
				if (prodLocnInfo != null && prodLocnInfo.strTaxFlag.equals("Y")) {
					info.strItemTaxCode = prodLocnInfo.strItemTaxCode;
				}

				info.uTaxInfoList.clear();

				//Calculation Tax Amount
				List<TaxRatesInfo> taxRatesInfoList = dbAdapter.getTaxRatesInfoList(Global.gCurCustomerInfo.strTaxCode, info.strItemNo);
				info.nTrxnNo = Global.gUOrderHInfo.nTrxnNo;
				info.nTaxAmount = 0;
				info.nPrintTaxAmount = 0;
				info.nPrintLineTaxAmount = 0;
				info.nPrintCDSAmount = 0;
				boolean bAppliedGST = false;
				float fGSTOnCDS = 0.0f;
				List<UTaxInfo> gstOnCDSTaxRateInfoList = new ArrayList<>();
				for(TaxRatesInfo taxRatesInfo : taxRatesInfoList)
				{
					float nTaxAmount = 0;
					float nPrintTaxAmount = 0;
					float nPrintLineTaxAmount = 0;
					if(taxRatesInfo.nTaxMethod == 1) //Applying Cents Amount
					{
						if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
						{
							nTaxAmount = Utils.getDividedValue(taxRatesInfo.nTaxRate) * ((float)info.dDumps) * prodDetlInfo.nConsumerUnit;
							nPrintTaxAmount = Utils.getDividedValue(taxRatesInfo.nTaxRate) * ((float)info.dDumps) * prodDetlInfo.nConsumerUnit;
							nPrintLineTaxAmount = Utils.getDividedValue(taxRatesInfo.nTaxRate) * prodDetlInfo.nConsumerUnit;
						}
						else
						{
							nTaxAmount = Utils.getDividedValue(taxRatesInfo.nTaxRate) * info.nDumps * prodDetlInfo.nConsumerUnit;
							nPrintTaxAmount = Utils.getDividedValue(taxRatesInfo.nTaxRate) * info.nDumps * prodDetlInfo.nConsumerUnit;
							nPrintLineTaxAmount = Utils.getDividedValue(taxRatesInfo.nTaxRate) * prodDetlInfo.nConsumerUnit;
						}
						
					}
					else if( taxRatesInfo.nTaxMethod == 2 )
					{
						if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
						{
							nTaxAmount = (Utils.getRoundedValue(((float)info.dDumps) * (info.nBasePrice - info.nDiscountVal)) + info.nTaxAmount) / 100 * Utils.getDividedValue(taxRatesInfo.nTaxRate);
							nPrintTaxAmount = (Utils.getRoundedValue(((float)info.dDumps) * info.nBasePrice) + info.nTaxAmount) / 100 * Utils.getDividedValue(taxRatesInfo.nTaxRate);
							nPrintLineTaxAmount = (Utils.getRoundedValue(info.nBasePrice) + info.nPrintLineTaxAmount) / 100 * Utils.getDividedValue(taxRatesInfo.nTaxRate);
						}
						else
						{
							nTaxAmount = (Utils.getRoundedValue(info.nDumps * (info.nBasePrice - info.nDiscountVal)) + info.nTaxAmount) / 100 * Utils.getDividedValue(taxRatesInfo.nTaxRate);
							nPrintTaxAmount = (Utils.getRoundedValue(info.nDumps * info.nBasePrice) + info.nTaxAmount) / 100 * Utils.getDividedValue(taxRatesInfo.nTaxRate);
							nPrintLineTaxAmount = (Utils.getRoundedValue(info.nBasePrice) + info.nPrintLineTaxAmount) / 100 * Utils.getDividedValue(taxRatesInfo.nTaxRate);
						}

					}

					if(taxRatesInfo.strTaxType.equals("S"))
					{
						ProdLocnInfo locnInfo = dbAdapter.getProdLocnInfo(taxRatesInfo.strTaxRateNumber);
						List<TaxRatesInfo> gstOnCDSRatesInfoList = dbAdapter.getTaxRatesInfoList(Global.gCurCustomerInfo.strTaxCode, taxRatesInfo.strTaxRateNumber);
						for(TaxRatesInfo gstOnCDSRatesInfo : gstOnCDSRatesInfoList)
						{
							if(gstOnCDSRatesInfo.nTaxMethod == 2 && gstOnCDSRatesInfo.strTaxRateCode.equals("GST") )
							{
								float nGSTOnCDSTaxAmount;
								if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
								{
									nGSTOnCDSTaxAmount = Utils.getDividedValue(taxRatesInfo.nTaxRate) / 100 * Utils.getDividedValue(gstOnCDSRatesInfo.nTaxRate) * ((float)info.dDumps) * prodDetlInfo.nConsumerUnit;
								}
								else
								{
									nGSTOnCDSTaxAmount = Utils.getDividedValue(taxRatesInfo.nTaxRate) / 100 * Utils.getDividedValue(gstOnCDSRatesInfo.nTaxRate) * info.nDumps * prodDetlInfo.nConsumerUnit;
								}

								fGSTOnCDS += nGSTOnCDSTaxAmount;

								UTaxInfo uTaxInfo = new UTaxInfo();
								uTaxInfo.nTrxnNo = info.nTrxnNo;
								uTaxInfo.nTrxnType = info.nTrxnType;
								uTaxInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
								uTaxInfo.nLocnNo = locnInfo.nLocnNo;
								uTaxInfo.strItemNo = taxRatesInfo.strTaxRateNumber;
								uTaxInfo.strTaxRateCode = gstOnCDSRatesInfo.strTaxRateCode;
								uTaxInfo.nTaxAmount = Utils.getRoundedValue(nGSTOnCDSTaxAmount);
								uTaxInfo.strCumulativeFlag = gstOnCDSRatesInfo.strCumulativeFlag;
								uTaxInfo.nTaxRate = gstOnCDSRatesInfo.nTaxRate;
								uTaxInfo.strTaxRateNumber = gstOnCDSRatesInfo.strTaxRateNumber;
								uTaxInfo.nReasonCode = info.nReasonCode;
								gstOnCDSTaxRateInfoList.add(uTaxInfo);
							}
						}
					}

					info.nTaxAmount += Utils.get5RoundedValue(mContext, nTaxAmount);
					info.nPrintTaxAmount += Utils.get5RoundedValue(mContext, nPrintTaxAmount);
					info.nPrintLineTaxAmount += Utils.get5RoundedValue(mContext, nPrintLineTaxAmount);

					if(taxRatesInfo.strTaxRateCode.equals("GST"))
					{
						bAppliedGST = true;
					}

					if( taxRatesInfo.strTaxType.toUpperCase().equals("S") )
					{
						info.nPrintCDSAmount += Utils.get2DecimalValue(nPrintTaxAmount);
						info.nCDSRate = taxRatesInfo.nTaxRate;
						PrintTaxRateInfo cdsAmount = Global.gUOrderHInfo.cdsMap.get(taxRatesInfo.strTaxRateCode);
						if(taxRatesInfo.strTaxRateCode.length() > 0 && !taxRatesInfo.strTaxRateCode.equals("0"))
						{
							if( cdsAmount == null )
							{
								cdsAmount = new PrintTaxRateInfo();
								cdsAmount.nBottles = -1 * info.nDumps * prodDetlInfo.nConsumerUnit;
								cdsAmount.strTaxCode = taxRatesInfo.strTaxRateCode;
								cdsAmount.price = Utils.getDividedValue(taxRatesInfo.nTaxRate);
								cdsAmount.strRateDescription = taxRatesInfo.strRateDescription;

								Global.gUOrderHInfo.cdsMap.put(taxRatesInfo.strTaxRateCode, cdsAmount);
							}
							else
							{
								cdsAmount.nBottles -= info.nDumps * prodDetlInfo.nConsumerUnit;
							}
						}

					}

					UTaxInfo uTaxInfo = new UTaxInfo();
					uTaxInfo.nTrxnNo = info.nTrxnNo;
					uTaxInfo.nTrxnType = info.nTrxnType;
					uTaxInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
					uTaxInfo.nLocnNo = info.nLocnNo;
					uTaxInfo.strItemNo = info.strItemNo;
					uTaxInfo.strTaxRateCode = taxRatesInfo.strTaxRateCode;
					uTaxInfo.nTaxAmount = Utils.getRoundedValue(nTaxAmount);
					uTaxInfo.strCumulativeFlag = taxRatesInfo.strCumulativeFlag;
					uTaxInfo.nTaxRate = taxRatesInfo.nTaxRate;
					uTaxInfo.strTaxRateNumber = taxRatesInfo.strTaxRateNumber;
					uTaxInfo.nReasonCode = info.nReasonCode;
					info.uTaxInfoList.add(uTaxInfo);
				}
				if(!bAppliedGST)
				{
					info.nTaxAmount += Utils.get5RoundedValue(mContext, fGSTOnCDS);
					info.nPrintTaxAmount += Utils.get5RoundedValue(mContext, fGSTOnCDS);
					info.uTaxInfoList.addAll(gstOnCDSTaxRateInfoList);
				}
				Global.gUOrderHInfo.pickupList.set(i, info);
			}
		}
		List<ARHeaderInfo> infos = dbAdapter.getARHeaderInfo(Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo);
		dbAdapter.close();

		calculateTotal();
		float mCollectionValue = 0.0f;

		for(ARHeaderInfo arHeaderInfo : infos)
		{
			if( arHeaderInfo.nProcessStatus < 2 )
			{
				mCollectionValue += arHeaderInfo.nTrxnAmount * 1.0 / Global.MULTIPLE_VALUE;
				//arHeaderInfo.nProcessStatus = 2;
				//dbAdapter.updateARHeaderInfo(arHeaderInfo);
			}
		}


		if( Global.gCurHeaderInfo != null )
		{
			if (Global.gCurHeaderInfo.nProcessStatus != 3 && Global.gCurHeaderInfo.nProcessStatus != 2 )
			{
				Global.gCurHeaderInfo.nTrxnAmount = Math.round(getRoundedValue(Global.gUOrderHInfo.nSaleAmount + Global.gUOrderHInfo.nTaxAmount + Global.gUOrderHInfo.nHandlingFee + Global.gUOrderHInfo.nHandlingFeeTaxAmount) * Global.MULTIPLE_VALUE);
				mCollectionValue += Global.gCurHeaderInfo.nTrxnAmount * 1.0 / Global.MULTIPLE_VALUE;
			}
			else
			{
				Global.gCurHeaderInfo.nTrxnAmount = Math.round(getRoundedValue(Global.gUOrderHInfo.nSaleAmount + Global.gUOrderHInfo.nTaxAmount + Global.gUOrderHInfo.nHandlingFee + Global.gUOrderHInfo.nHandlingFeeTaxAmount) * Global.MULTIPLE_VALUE);
			}
		}
		collectionValue = getRoundedValue(mCollectionValue);

		String strData = Utils.getRoundedString(mContext, collectionValue);
		if(Global.gCurCustomerInfo.nShowPrice == 2)
		{
			collectionValue = 0f;
		}
		((EditText) mRootView.findViewById(R.id.editTotalCollection)).setText(strData);
		mAmount = collectionValue;
		setAmount();
	}

	TextWatcher mTotalCollectionChangeListener = new TextWatcher() {
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {

		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {

		}

		@Override
		public void afterTextChanged(Editable s) {
			if( bEditInit )
			{
				String strValue = ((EditText) mRootView.findViewById(R.id.editTotalCollection)).getText().toString();
				collectionValue = Utils.getFloatFromString(strValue);
			}
		}
	};

	private void setAmount() {
		((EditText)mRootView.findViewById(R.id.editTotalCollection)).setTag("From App");
		String strAmount = getRoundedFormattedPrice(mAmount);
		((EditText)mRootView.findViewById(R.id.editTotalCollection)).setText(strAmount.substring(Global.gCurrencySymbol.length()));
		((EditText)mRootView.findViewById(R.id.editTotalCollection)).setSelection(((EditText)mRootView.findViewById(R.id.editTotalCollection)).getText().toString().length());
		((EditText)mRootView.findViewById(R.id.editTotalCollection)).setTag(null);
	}

	float getRoundedValue(float value)
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
		if( bCentsRount )
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

	String getRoundedFormattedPrice(float value)
	{
		return String.format(Locale.ENGLISH, "%s" + "%." + Utils.getIntSetting(mContext, Global.PREF_DECIMAL) + "f", Global.gCurrencySymbol, value);
	}

	float mAmount = 0.0f;
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
		((EditText)mRootView.findViewById(R.id.editTotalCollection)).setTag("From App");
		String strAmount = String.format(Locale.ENGLISH, "%s" + "%." + Utils.getIntSetting(mContext, Global.PREF_DECIMAL) + "f", Global.gCurrencySymbol, mAmount);
		((EditText)mRootView.findViewById(R.id.editTotalCollection)).setText(strAmount.substring(Global.gCurrencySymbol.length()));
		((EditText)mRootView.findViewById(R.id.editTotalCollection)).setSelection(((EditText)mRootView.findViewById(R.id.editTotalCollection)).getText().toString().length());
		((EditText)mRootView.findViewById(R.id.editTotalCollection)).setTag(null);
	}

	@Override
	public void loadData() {
		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();

		//Load Price Group information
		priceGrpInfoMap = dbAdapter.getPriceGrpInfo(Global.gCurCustomerInfo.nPriceGrp);
		altPriceGrpInfoMap = dbAdapter.getPriceGrpInfo(Global.gCurCustomerInfo.nAltPriceGrp);

		mCartonDescription = "";
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

		if( mDocTextList == null )
		{
			mDocTextList = new ArrayList<>();
		}
		for(int i = 6; i <= 7; i++)
		{
			List<DocTextInfo> docTextList = dbAdapter.getDocTextList(Global.gCurCustomerInfo.nCustNo, Global.gCurCustomerInfo.nChainNo, i);
			if( docTextList == null || docTextList.size() == 0 )
			{
				docTextList = dbAdapter.getDocTextList(Global.gCurCustomerInfo.nCustNo, i);
			}
			if( docTextList == null || docTextList.size() == 0 )
			{
				docTextList = dbAdapter.getDocTextList(i);
			}
			if( docTextList != null && docTextList.size() > 0)
			{
				mDocTextList.addAll(docTextList);
			}
		}
		if(!bConsignment)
		{

			PresoldOrHInfo presoldOrHInfo = null;
			CustomerOrderInfo customerOrderInfo = null;
			if( Global.gCurCustomerInfo.presoldOrHInfo != null && Global.gCurCustomerInfo.presoldOrHInfo.size() > 0)
			{
				presoldOrHInfo = Global.gCurCustomerInfo.presoldOrHInfo.get(0);
			}

			if( Global.gCurCustomerInfo.presoldOrHInfo != null && Global.gCurCustomerInfo.latestOrderInfo.size() > 0)
			{
				customerOrderInfo = Global.gCurCustomerInfo.latestOrderInfo.get(0);
			}

			mUInvenHinfo = dbAdapter.getUInvenHInfo(Utils.getLongSetting(mContext, Global.PREF_UINVEN_TRXN_NO));
			if (mUInvenHinfo == null) {
				mUInvenHinfo = new UInvenHInfo();
				mUInvenHinfo.nTrxnNo = Utils.getLongSetting(mContext, Global.PREF_UINVEN_TRXN_NO);
			/*if(mUInvenHinfo.nTrxnNo == 0)
			{
				mUInvenHinfo.nTrxnNo = System.currentTimeMillis();
			}*/
			}
			mUInvenHinfo.uInvenDList = dbAdapter.getUInvenDList(mUInvenHinfo.nTrxnNo);

			if( Global.gUOrderHInfo != null && Global.gUOrderHInfo.deliveryList != null && Global.gUOrderHInfo.deliveryList.size() > 0 ) {
				for (UOrderDInfo uOrderDInfo : Global.gUOrderHInfo.deliveryList) {
					checkAndAddUInvenDInfo(uOrderDInfo);
				}
			}
			if( Global.gUOrderHInfo != null && Global.gUOrderHInfo.pickupList != null && Global.gUOrderHInfo.pickupList.size() > 0 ) {
				for (UOrderDInfo uOrderDInfo : Global.gUOrderHInfo.pickupList) {
					checkAndAddUInvenDInfo(uOrderDInfo);
				}
			}

			mPdfTrxnNo = System.currentTimeMillis();
			if(Global.gUOrderHInfo == null)
			{
				Global.gUOrderHInfo = new UOrderHInfo();
			}
			Global.gUOrderHInfo.nTrxnNo = mPdfTrxnNo;

			Global.gUOrderHInfo.cdsMap.clear();
			if( Global.gUOrderHInfo != null && Global.gUOrderHInfo.deliveryList != null && Global.gUOrderHInfo.deliveryList.size() > 0 ) {
				for (int i = 0; i < Global.gUOrderHInfo.deliveryList.size(); i++) {
					UOrderDInfo info = Global.gUOrderHInfo.deliveryList.get(i);
					ProdLocnInfo prodLocnInfo = dbAdapter.getProdLocnInfo(info.strItemNo);
					ProdDetlInfo prodDetlInfo = dbAdapter.getProdDetl(info.strItemNo);
					info.prodLocnInfo = prodLocnInfo;
					if (prodLocnInfo != null && prodLocnInfo.strTaxFlag.equals("Y")) {
						info.strItemTaxCode = prodLocnInfo.strItemTaxCode;
					}

					info.uTaxInfoList.clear();
					//Calculation Tax Amount
					List<TaxRatesInfo> taxRatesInfoList = dbAdapter.getTaxRatesInfoList(Global.gCurCustomerInfo.strTaxCode, info.strItemNo);
					info.nTrxnNo = Global.gUOrderHInfo.nTrxnNo;
					info.nTaxAmount = 0;
					info.nPrintTaxAmount = 0;
					info.nPrintLineTaxAmount = 0;
					info.nPrintCDSAmount = 0;
					boolean bAppliedGST = false;
					float fGSTOnCDS = 0.0f;
					List<UTaxInfo> gstOnCDSTaxRateInfoList = new ArrayList<>();
					for(TaxRatesInfo taxRatesInfo : taxRatesInfoList)
					{
						float nTaxAmount = 0;
						float nPrintTaxAmount = 0;
						float nPrintLineTaxAmount = 0;
						if(taxRatesInfo.nTaxMethod == 1) //Applying Cents Amount
						{
							if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
							{
								nTaxAmount = Utils.getDividedValue(taxRatesInfo.nTaxRate) * ((float)info.dSales) * prodDetlInfo.nConsumerUnit ;
								nPrintTaxAmount = Utils.getDividedValue(taxRatesInfo.nTaxRate) * ((float)info.dSales) * prodDetlInfo.nConsumerUnit ;
								nPrintLineTaxAmount = Utils.getDividedValue(taxRatesInfo.nTaxRate) * prodDetlInfo.nConsumerUnit ;
							}
							else
							{
								nTaxAmount = Utils.getDividedValue(taxRatesInfo.nTaxRate) * info.nSales * prodDetlInfo.nConsumerUnit ;
								nPrintTaxAmount = Utils.getDividedValue(taxRatesInfo.nTaxRate) * info.nSales * prodDetlInfo.nConsumerUnit ;
								nPrintLineTaxAmount = Utils.getDividedValue(taxRatesInfo.nTaxRate) * prodDetlInfo.nConsumerUnit ;
							}

						}
						else if( taxRatesInfo.nTaxMethod == 2 )
						{
							if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
							{
								nTaxAmount = (Utils.getRoundedValue(((float)info.dSales) *(info.nBasePrice - info.nDiscountVal)) + info.nTaxAmount) / 100 * Utils.getDividedValue(taxRatesInfo.nTaxRate);
								nPrintTaxAmount = (Utils.getRoundedValue(((float)info.dSales) * info.nBasePrice) + info.nTaxAmount) / 100 * Utils.getDividedValue(taxRatesInfo.nTaxRate);
								nPrintLineTaxAmount = (Utils.getRoundedValue(info.nBasePrice) + info.nPrintLineTaxAmount) / 100 * Utils.getDividedValue(taxRatesInfo.nTaxRate);
							}
							else
							{
								nTaxAmount = (Utils.getRoundedValue(info.nSales *(info.nBasePrice - info.nDiscountVal)) + info.nTaxAmount) / 100 * Utils.getDividedValue(taxRatesInfo.nTaxRate);
								nPrintTaxAmount = (Utils.getRoundedValue(info.nSales * info.nBasePrice) + info.nTaxAmount) / 100 * Utils.getDividedValue(taxRatesInfo.nTaxRate);
								nPrintLineTaxAmount = (Utils.getRoundedValue(info.nBasePrice) + info.nPrintLineTaxAmount) / 100 * Utils.getDividedValue(taxRatesInfo.nTaxRate);
							}

						}

						if(taxRatesInfo.strTaxType.equals("S"))
						{
							ProdLocnInfo locnInfo = dbAdapter.getProdLocnInfo(taxRatesInfo.strTaxRateNumber);
							List<TaxRatesInfo> gstOnCDSRatesInfoList = dbAdapter.getTaxRatesInfoList(Global.gCurCustomerInfo.strTaxCode, taxRatesInfo.strTaxRateNumber);
							for(TaxRatesInfo gstOnCDSRatesInfo : gstOnCDSRatesInfoList)
							{
								if(gstOnCDSRatesInfo.nTaxMethod == 2 && gstOnCDSRatesInfo.strTaxRateCode.equals("GST") )
								{
									float nGSTOnCDSTaxAmount;
									if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
									{
										nGSTOnCDSTaxAmount = Utils.getDividedValue(taxRatesInfo.nTaxRate) / 100 * Utils.getDividedValue(gstOnCDSRatesInfo.nTaxRate) * ((float)info.dSales) * prodDetlInfo.nConsumerUnit;
									}
									else
									{
										nGSTOnCDSTaxAmount = Utils.getDividedValue(taxRatesInfo.nTaxRate) / 100 * Utils.getDividedValue(gstOnCDSRatesInfo.nTaxRate) * info.nSales * prodDetlInfo.nConsumerUnit;
									}
									fGSTOnCDS += nGSTOnCDSTaxAmount;

									UTaxInfo uTaxInfo = new UTaxInfo();
									uTaxInfo.nTrxnNo = info.nTrxnNo;
									uTaxInfo.nTrxnType = info.nTrxnType;
									uTaxInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
									uTaxInfo.nLocnNo = locnInfo.nLocnNo;
									uTaxInfo.strItemNo = taxRatesInfo.strTaxRateNumber;
									uTaxInfo.strTaxRateCode = gstOnCDSRatesInfo.strTaxRateCode;
									uTaxInfo.nTaxAmount = Utils.get2DecimalValue(nGSTOnCDSTaxAmount);
									uTaxInfo.strCumulativeFlag = gstOnCDSRatesInfo.strCumulativeFlag;
									uTaxInfo.nTaxRate = gstOnCDSRatesInfo.nTaxRate;
									uTaxInfo.strTaxRateNumber = gstOnCDSRatesInfo.strTaxRateNumber;
									uTaxInfo.nReasonCode = info.nReasonCode;
									gstOnCDSTaxRateInfoList.add(uTaxInfo);
								}
							}
						}

						info.nTaxAmount += Utils.get5RoundedValue(mContext, nTaxAmount);
						info.nPrintTaxAmount += Utils.get5RoundedValue(mContext, nPrintTaxAmount);
						info.nPrintLineTaxAmount += Utils.get5RoundedValue(mContext, nPrintLineTaxAmount);

						if(taxRatesInfo.strTaxRateCode.equals("GST"))
						{
							bAppliedGST = true;
						}

						if( taxRatesInfo.strTaxType.toUpperCase().equals("S") )
						{
							info.nPrintCDSAmount += Utils.get2DecimalValue(nPrintTaxAmount);
							info.nCDSRate = taxRatesInfo.nTaxRate;
							PrintTaxRateInfo cdsAmount = Global.gUOrderHInfo.cdsMap.get(taxRatesInfo.strTaxRateCode);
							if(taxRatesInfo.strTaxRateCode.length() > 0 && !taxRatesInfo.strTaxRateCode.equals("0"))
							{
								if( cdsAmount == null )
								{
									cdsAmount = new PrintTaxRateInfo();

									cdsAmount.nBottles = info.nSales * prodDetlInfo.nConsumerUnit;
									cdsAmount.strTaxCode = taxRatesInfo.strTaxRateCode;
									cdsAmount.price = Utils.getDividedValue(taxRatesInfo.nTaxRate);
									cdsAmount.strRateDescription = taxRatesInfo.strRateDescription;

									Global.gUOrderHInfo.cdsMap.put(taxRatesInfo.strTaxRateCode, cdsAmount);
								}
								else
								{
									cdsAmount.nBottles += info.nSales * prodDetlInfo.nConsumerUnit;
								}
							}
						}

						UTaxInfo uTaxInfo = new UTaxInfo();
						uTaxInfo.nTrxnNo = info.nTrxnNo;
						uTaxInfo.nTrxnType = info.nTrxnType;
						uTaxInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
						uTaxInfo.nLocnNo = info.nLocnNo;
						uTaxInfo.strItemNo = info.strItemNo;
						uTaxInfo.strTaxRateCode = taxRatesInfo.strTaxRateCode;
						uTaxInfo.nTaxAmount = Utils.get5RoundedValue(mContext, nTaxAmount);
						uTaxInfo.strCumulativeFlag = taxRatesInfo.strCumulativeFlag;
						uTaxInfo.nTaxRate = taxRatesInfo.nTaxRate;
						uTaxInfo.strTaxRateNumber = taxRatesInfo.strTaxRateNumber;
						uTaxInfo.nReasonCode = info.nReasonCode;
						info.uTaxInfoList.add(uTaxInfo);
					}
					if(!bAppliedGST)
					{
						info.nTaxAmount += Utils.get5RoundedValue(mContext, fGSTOnCDS);
						info.nPrintTaxAmount += Utils.get5RoundedValue(mContext, fGSTOnCDS);
						info.uTaxInfoList.addAll(gstOnCDSTaxRateInfoList);
					}

					Global.gUOrderHInfo.deliveryList.set(i, info);
				}
			}
			if( Global.gUOrderHInfo != null && Global.gUOrderHInfo.pickupList != null && Global.gUOrderHInfo.pickupList.size() > 0 ) {
				for (int i = 0; i < Global.gUOrderHInfo.pickupList.size(); i++) {
					UOrderDInfo info = Global.gUOrderHInfo.pickupList.get(i);
					ProdLocnInfo prodLocnInfo = dbAdapter.getProdLocnInfo(info.strItemNo);
					info.prodLocnInfo = prodLocnInfo;
					ProdDetlInfo prodDetlInfo = dbAdapter.getProdDetl(info.strItemNo);
					if (prodLocnInfo != null && prodLocnInfo.strTaxFlag.equals("Y")) {
						info.strItemTaxCode = prodLocnInfo.strItemTaxCode;
					}

					info.uTaxInfoList.clear();

					//Calculation Tax Amount
					List<TaxRatesInfo> taxRatesInfoList = dbAdapter.getTaxRatesInfoList(Global.gCurCustomerInfo.strTaxCode, info.strItemNo);
					info.nTrxnNo = Global.gUOrderHInfo.nTrxnNo;
					info.nTaxAmount = 0;
					info.nPrintTaxAmount = 0;
					info.nPrintLineTaxAmount = 0;
					info.nPrintCDSAmount = 0;
					boolean bAppliedGST = false;
					float fGSTOnCDS = 0.0f;
					List<UTaxInfo> gstOnCDSTaxRateInfoList = new ArrayList<>();
					for(TaxRatesInfo taxRatesInfo : taxRatesInfoList)
					{
						float nTaxAmount = 0;
						float nPrintTaxAmount = 0;
						float nPrintLineTaxAmount = 0;
						if(taxRatesInfo.nTaxMethod == 1) //Applying Cents Amount
						{
							if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
							{
								nTaxAmount = Utils.getDividedValue(taxRatesInfo.nTaxRate) * ((float)info.dDumps) * prodDetlInfo.nConsumerUnit;
								nPrintTaxAmount = Utils.getDividedValue(taxRatesInfo.nTaxRate) * ((float)info.dDumps) * prodDetlInfo.nConsumerUnit;
								nPrintLineTaxAmount = Utils.getDividedValue(taxRatesInfo.nTaxRate) * prodDetlInfo.nConsumerUnit;
							}
							else
							{
								nTaxAmount = Utils.getDividedValue(taxRatesInfo.nTaxRate) * info.nDumps * prodDetlInfo.nConsumerUnit;
								nPrintTaxAmount = Utils.getDividedValue(taxRatesInfo.nTaxRate) * info.nDumps * prodDetlInfo.nConsumerUnit;
								nPrintLineTaxAmount = Utils.getDividedValue(taxRatesInfo.nTaxRate) * prodDetlInfo.nConsumerUnit;
							}

						}
						else if( taxRatesInfo.nTaxMethod == 2 )
						{
							if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
							{
								nTaxAmount = (Utils.getRoundedValue(((float)info.dDumps) * (info.nBasePrice - info.nDiscountVal)) + info.nTaxAmount) / 100 * Utils.getDividedValue(taxRatesInfo.nTaxRate);
								nPrintTaxAmount = (Utils.getRoundedValue(((float)info.dDumps) * info.nBasePrice) + info.nTaxAmount) / 100 * Utils.getDividedValue(taxRatesInfo.nTaxRate);
								nPrintLineTaxAmount = (Utils.getRoundedValue(info.nBasePrice) + info.nPrintLineTaxAmount) / 100 * Utils.getDividedValue(taxRatesInfo.nTaxRate);
							}
							else
							{
								nTaxAmount = (Utils.getRoundedValue(info.nDumps * (info.nBasePrice - info.nDiscountVal)) + info.nTaxAmount) / 100 * Utils.getDividedValue(taxRatesInfo.nTaxRate);
								nPrintTaxAmount = (Utils.getRoundedValue(info.nDumps * info.nBasePrice) + info.nTaxAmount) / 100 * Utils.getDividedValue(taxRatesInfo.nTaxRate);
								nPrintLineTaxAmount = (Utils.getRoundedValue(info.nBasePrice) + info.nPrintLineTaxAmount) / 100 * Utils.getDividedValue(taxRatesInfo.nTaxRate);
							}

						}

						if(taxRatesInfo.strTaxType.equals("S"))
						{
							ProdLocnInfo locnInfo = dbAdapter.getProdLocnInfo(taxRatesInfo.strTaxRateNumber);
							List<TaxRatesInfo> gstOnCDSRatesInfoList = dbAdapter.getTaxRatesInfoList(Global.gCurCustomerInfo.strTaxCode, taxRatesInfo.strTaxRateNumber);
							for(TaxRatesInfo gstOnCDSRatesInfo : gstOnCDSRatesInfoList)
							{
								float nGSTOnCDSTaxAmount;
								if(gstOnCDSRatesInfo.nTaxMethod == 2 && gstOnCDSRatesInfo.strTaxRateCode.equals("GST") )
								{
									if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
									{
										nGSTOnCDSTaxAmount = Utils.getDividedValue(taxRatesInfo.nTaxRate) / 100 * Utils.getDividedValue(gstOnCDSRatesInfo.nTaxRate) * ((float)info.dDumps) * prodDetlInfo.nConsumerUnit;
									}
									else
									{
										nGSTOnCDSTaxAmount = Utils.getDividedValue(taxRatesInfo.nTaxRate) / 100 * Utils.getDividedValue(gstOnCDSRatesInfo.nTaxRate) * info.nDumps * prodDetlInfo.nConsumerUnit;
									}

									fGSTOnCDS += nGSTOnCDSTaxAmount;

									UTaxInfo uTaxInfo = new UTaxInfo();
									uTaxInfo.nTrxnNo = info.nTrxnNo;
									uTaxInfo.nTrxnType = info.nTrxnType;
									uTaxInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
									uTaxInfo.nLocnNo = locnInfo.nLocnNo;
									uTaxInfo.strItemNo = taxRatesInfo.strTaxRateNumber;
									uTaxInfo.strTaxRateCode = gstOnCDSRatesInfo.strTaxRateCode;
									uTaxInfo.nTaxAmount = Utils.getRoundedValue(nGSTOnCDSTaxAmount);
									uTaxInfo.strCumulativeFlag = gstOnCDSRatesInfo.strCumulativeFlag;
									uTaxInfo.nTaxRate = gstOnCDSRatesInfo.nTaxRate;
									uTaxInfo.strTaxRateNumber = gstOnCDSRatesInfo.strTaxRateNumber;
									uTaxInfo.nReasonCode = info.nReasonCode;
									gstOnCDSTaxRateInfoList.add(uTaxInfo);
								}
							}
						}

						info.nTaxAmount += Utils.get5RoundedValue(mContext, nTaxAmount);
						info.nPrintTaxAmount += Utils.get5RoundedValue(mContext, nPrintTaxAmount);
						info.nPrintLineTaxAmount += Utils.get5RoundedValue(mContext, nPrintLineTaxAmount);

						if(taxRatesInfo.strTaxRateCode.equals("GST"))
						{
							bAppliedGST = true;
						}

						if( taxRatesInfo.strTaxType.toUpperCase().equals("S") )
						{
							info.nPrintCDSAmount += Utils.get2DecimalValue(nPrintTaxAmount);
							info.nCDSRate = taxRatesInfo.nTaxRate;
							PrintTaxRateInfo cdsAmount = Global.gUOrderHInfo.cdsMap.get(taxRatesInfo.strTaxRateCode);
							if(taxRatesInfo.strTaxRateCode.length() > 0 && !taxRatesInfo.strTaxRateCode.equals("0"))
							{
								if( cdsAmount == null )
								{
									cdsAmount = new PrintTaxRateInfo();
									cdsAmount.nBottles = -1 * info.nDumps * prodDetlInfo.nConsumerUnit;
									cdsAmount.strTaxCode = taxRatesInfo.strTaxRateCode;
									cdsAmount.price = Utils.getDividedValue(taxRatesInfo.nTaxRate);
									cdsAmount.strRateDescription = taxRatesInfo.strRateDescription;

									Global.gUOrderHInfo.cdsMap.put(taxRatesInfo.strTaxRateCode, cdsAmount);
								}
								else
								{
									cdsAmount.nBottles -= info.nDumps * prodDetlInfo.nConsumerUnit;
								}
							}

						}

						UTaxInfo uTaxInfo = new UTaxInfo();
						uTaxInfo.nTrxnNo = info.nTrxnNo;
						uTaxInfo.nTrxnType = info.nTrxnType;
						uTaxInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
						uTaxInfo.nLocnNo = info.nLocnNo;
						uTaxInfo.strItemNo = info.strItemNo;
						uTaxInfo.strTaxRateCode = taxRatesInfo.strTaxRateCode;
						uTaxInfo.nTaxAmount = Utils.getRoundedValue(nTaxAmount);
						uTaxInfo.strCumulativeFlag = taxRatesInfo.strCumulativeFlag;
						uTaxInfo.nTaxRate = taxRatesInfo.nTaxRate;
						uTaxInfo.strTaxRateNumber = taxRatesInfo.strTaxRateNumber;
						uTaxInfo.nReasonCode = info.nReasonCode;
						info.uTaxInfoList.add(uTaxInfo);
					}
					if(!bAppliedGST)
					{
						info.nTaxAmount += Utils.get5RoundedValue(mContext, fGSTOnCDS);
						info.nPrintTaxAmount += Utils.get5RoundedValue(mContext, fGSTOnCDS);
						info.uTaxInfoList.addAll(gstOnCDSTaxRateInfoList);
					}
					Global.gUOrderHInfo.pickupList.set(i, info);
				}
			}
			if(!Utils.getStringSetting(mContext, Global.PREF_UNLESHED).equals("1"))
			{
				if (Global.gUOrderHInfo.strPOReference.length() == 0 && presoldOrHInfo != null && presoldOrHInfo.nVisited == 0 ) {
					if (mIsPickup == false) {
						Global.gUOrderHInfo.strPOReference = presoldOrHInfo.strPORef;
					} else {
						Global.gUOrderHInfo.strPOReference = presoldOrHInfo.strCredRef;
					}
				}
				else if(Global.gUOrderHInfo.strPOReference.length() == 0 && customerOrderInfo != null)
				{
					Global.gUOrderHInfo.strPOReference = customerOrderInfo.purchaseOrderNumber;
				}
			}
		}
		else
		{
			if(Global.gUOrderHInfo.nTrxnNo == 0)
			{
				mPdfTrxnNo = System.currentTimeMillis();
				Global.gUOrderHInfo.nTrxnNo = mPdfTrxnNo;
			}
			else
			{
				mPdfTrxnNo = Global.gUOrderHInfo.nTrxnNo;
			}
		}
		dbAdapter.close();

		mInvoiceToInfo = new CustomerInfo();
		mInvoiceToInfo.strName = Global.gCurCustomerInfo.strBillName.length() > 0 ? Global.gCurCustomerInfo.strBillName : Global.gCurCustomerInfo.strName;
		mInvoiceToInfo.strAddress1 = Global.gCurCustomerInfo.strBillAddr1.length() > 0 ? Global.gCurCustomerInfo.strBillAddr1 : Global.gCurCustomerInfo.strAddress1;
		mInvoiceToInfo.strAddress2 = Global.gCurCustomerInfo.strBillAddr2.length() > 0 ? Global.gCurCustomerInfo.strBillAddr2 : Global.gCurCustomerInfo.strAddress2;
		mInvoiceToInfo.strCity = Global.gCurCustomerInfo.strBillCity.length() > 0 ? Global.gCurCustomerInfo.strBillCity : Global.gCurCustomerInfo.strCity;
		mInvoiceToInfo.strState = Global.gCurCustomerInfo.strState.length() > 0 ? Global.gCurCustomerInfo.strState : Global.gCurCustomerInfo.strShipToState;
		mInvoiceToInfo.strZip = Global.gCurCustomerInfo.strZip.length() > 0 ? Global.gCurCustomerInfo.strZip : Global.gCurCustomerInfo.strShipToZip;
		CustomerInfo deliveryToInfo = new CustomerInfo();
		deliveryToInfo.strName = Global.gCurCustomerInfo.strName;
		deliveryToInfo.strAddress1 = Global.gCurCustomerInfo.strAddress1;
		deliveryToInfo.strAddress2 = Global.gCurCustomerInfo.strAddress2;
		deliveryToInfo.strCity = Global.gCurCustomerInfo.strCity;
		deliveryToInfo.strState = Global.gCurCustomerInfo.strShipToState;
		deliveryToInfo.strZip = Global.gCurCustomerInfo.strShipToZip;
		mDeliverToInfo = deliveryToInfo;

		if( Global.gCurCustomerInfo.presoldOrHInfo != null && Global.gCurCustomerInfo.presoldOrHInfo.size() == 1 && Global.gCurCustomerInfo.presoldOrHInfo.get(0).nVisited == 0)
		{
			((EditText) mRootView.findViewById(R.id.editReceiptNumber)).setText(Global.gCurCustomerInfo.presoldOrHInfo.get(0).strPORef);
		}
		else if(Global.gCurCustomerInfo.latestOrderInfo != null && Global.gCurCustomerInfo.latestOrderInfo.size() == 1)
		{
			((EditText) mRootView.findViewById(R.id.editReceiptNumber)).setText(Global.gCurCustomerInfo.latestOrderInfo.get(0).purchaseOrderNumber);
		}

		if (Global.gUOrderHInfo.strDeliveryDate.length() > 0) {
			mDate = Utils.getDateFromString(Global.gUOrderHInfo.strDeliveryDate).getTime();
		} else {
			int nDeliveryDay = Utils.getIntSetting(mContext, Global.PREF_DELFUTDTE);
			mDate = new Date().getTime() + nDeliveryDay * 24 * 60 * 60 * 1000;
		}

//		if(Global.gCurHeaderInfo == null)

		//Check the INVOICEFMT.xml file and DELIVERYFMT.xml file
		checkPrintFormat();

	}

	private void checkPrintFormat()
	{
		new Thread()
		{
			@Override
			public void run() {
				String strDelieveryFmtFileName = Utils.getFinalPrintTemplateFileName(mContext, Global.PRINT_TEMPLATE_DELIVERY_FMT_FILENAME);
				String strInvoiceFmtFileName = Utils.getFinalPrintTemplateFileName(mContext, Global.PRINT_TEMPLATE_INVOICE_FMT_FILENAME);
				if(!Utils.isFileExist(Global.ROOT_PRINT_TEMPLATE_LOCCATION + strDelieveryFmtFileName) ||
						!Utils.isFileExist(Global.ROOT_PRINT_TEMPLATE_LOCCATION + strInvoiceFmtFileName) ||
						!Utils.isXmlFileCorrect(Global.ROOT_PRINT_TEMPLATE_LOCCATION + strDelieveryFmtFileName) ||
						!Utils.isXmlFileCorrect(Global.ROOT_PRINT_TEMPLATE_LOCCATION + strInvoiceFmtFileName))
				{
					new Handler(Looper.getMainLooper()).post(() -> {
						try
						{
							final MessageDialog msgDialog = new MessageDialog(mContext);
							msgDialog.setCancelable(false);
							msgDialog.mStrMessage = Str.PrintFormatDamagedOrMissing[Global.LANG];
							msgDialog.mStrLeft = Str.Return[Global.LANG];
							msgDialog.mStrRight = Str.DownloadingNow[Global.LANG];
							msgDialog.setOnDismissListener(dialog -> {
								if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
									if(!Utils.isConnectingToInternet(mContext))
									{
										Toast.makeText(mContext, Str.NoInternetConnection[Global.LANG], Toast.LENGTH_LONG ).show();
										checkPrintFormat();
									}
									else
									{
										showProgressDialog(Str.Loading[Global.LANG]);
										new Thread()
										{
											@Override
											public void run() {
												String mStrFTPCompanyName = Utils.getStringSetting(mContext, Global.PREF_FTP_COMPANY_NAME);
												String mStrFTPServerIP = Utils.getStringSetting(mContext, Global.PREF_FTP_SERVER_IP);
												String mStrFTPUserName = Utils.getStringSetting(mContext, Global.PREF_FTP_USERNAME);
												String mStrFTPPassword = Utils.getStringSetting(mContext, Global.PREF_FTP_PASSWORD);

												FTPClient ftpClient = null;
												if(!Utils.getStringSetting(mContext, Global.PREF_DisableFTP).equals("1"))
												{
													ftpClient = Utils.connnectingwithFTP(mStrFTPServerIP, Global.DEFAULT_FTP_PORT, mStrFTPUserName, mStrFTPPassword);
												}

												String strDeliveryFmtFileName = Utils.getFinalPrintTemplateFileName(mContext, Global.PRINT_TEMPLATE_DELIVERY_FMT_FILENAME);
												if(!Utils.isFileExist(Global.ROOT_PRINT_TEMPLATE_LOCCATION + strDeliveryFmtFileName) ||
														!Utils.isXmlFileCorrect(Global.ROOT_PRINT_TEMPLATE_LOCCATION + strDeliveryFmtFileName))
												{
													String strParentDir = String.format(Locale.ENGLISH, Global.FTP_REPORTS_DIR, mStrFTPCompanyName);
													String strFileName = Global.PRINT_TEMPLATE_DELIVERY_FMT_FILENAME;
													String strTargetFile = Global.ROOT_PRINT_TEMPLATE_LOCCATION + strFileName;
													Utils.downloadFileFromServer(mContext, strParentDir, strFileName, strTargetFile, ftpClient);
												}

												if(!Utils.isFileExist(Global.ROOT_PRINT_TEMPLATE_LOCCATION + Global.PRINT_TEMPLATE_INVOICE_FMT_FILENAME) ||
														!Utils.isXmlFileCorrect(Global.ROOT_PRINT_TEMPLATE_LOCCATION + Global.PRINT_TEMPLATE_INVOICE_FMT_FILENAME))
												{
													String strParentDir = String.format(Locale.ENGLISH, Global.FTP_REPORTS_DIR, mStrFTPCompanyName);
													String strFileName = Global.PRINT_TEMPLATE_INVOICE_FMT_FILENAME;
													String strTargetFile = Global.ROOT_PRINT_TEMPLATE_LOCCATION + strFileName;
													Utils.downloadFileFromServer(mContext, strParentDir, strFileName, strTargetFile, ftpClient);
												}

												if(ftpClient != null)
												{
													try {
														ftpClient.disconnect();
													}catch (Exception e)
													{
														e.printStackTrace();
													}
												}

												new Handler(Looper.getMainLooper()).post(() -> hideProgressDialog());
											}
										}.start();
									}


								}
							});
							msgDialog.show();
						}catch (Exception e)
						{
							e.printStackTrace();
						}
					});

				}
			}
		}.start();
	}

	private void checkAndAddUInvenDInfo(UOrderDInfo uOrderDInfo) {
		boolean bExist = false;
		for (UInvenDInfo info : mUInvenHinfo.uInvenDList) {
			if (info.strItemNo.equals(uOrderDInfo.strItemNo) == true) {
				bExist = true;
				break;
			}
		}
		if (bExist == false) {
			UInvenDInfo uInvenDInfo = new UInvenDInfo();
			uInvenDInfo.strItemNo = uOrderDInfo.strItemNo;
			uInvenDInfo.nSCWType = uOrderDInfo.nSCWType;
			mUInvenHinfo.uInvenDList.add(uInvenDInfo);
		}
	}

	private void calculateTotal() {
		if(!bConsignment)
		{
			if(mTrxnDate == null)
			{
				mTrxnDate = new Date();
			}
			float nSalesValue = 0;
			if( Global.gUOrderHInfo == null )
			{
				Global.gUOrderHInfo = new UOrderHInfo();
			}
			Global.gUOrderHInfo.nTotalDumps = 0;
			Global.gUOrderHInfo.nTotalBuybacks = 0;
			Global.gUOrderHInfo.nTotalAmount = 0;

			Global.gUOrderHInfo.nTaxAmount = 0;
			Global.gUOrderHInfo.nTotalFree = 0;

			Global.gUOrderHInfo.nPromotionAmount = 0;
			Global.gUOrderHInfo.nDiscBuybacks = 0;
			Global.gUOrderHInfo.nDiscDumps = 0;
			Global.gUOrderHInfo.nDiscSales = 0;
			Global.gUOrderHInfo.nNetBuybacks = 0;
			Global.gUOrderHInfo.nNetDumps = 0;
			Global.gUOrderHInfo.nNetSales = 0;
			Global.gUOrderHInfo.nPrintCDSAmount = 0;

			mDeliveredCnt = mReturnsCnt = mBuybackCnt = mContainersCnt = 0;
			mPrintList.clear();
			DbAdapter dbAdapter = new DbAdapter(mContext);
			dbAdapter.open();
			for (UOrderDInfo info : Global.gUOrderHInfo.deliveryList) {
				if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
				{
					if (info.dSales != 0 || (bPrintZeroQtyPresoldItem && info.isFromPresoldOrder)) {
						Global.gUOrderHInfo.dTotalAmount += info.dSales;
						nSalesValue += Utils.getRoundedValue(info.dSales * info.nPrice);
						if(Utils.getStringSetting(mContext, Global.PREF_INVCALC).equals("TTx"))
						{
							Global.gUOrderHInfo.nDiscSales += Utils.getRoundedValueForTTx(info.dSales * Utils.getRoundedValueForTTx(info.nBasePrice - info.nPrice));
						}
						else
						{
							Global.gUOrderHInfo.nDiscSales += info.dSales * (info.nBasePrice - info.nGrossPrice);
						}

						Global.gUOrderHInfo.nNetSales += info.dSales * (info.nPrice);
						Global.gUOrderHInfo.nPromotionAmount += info.dSales * (info.nGrossPrice - info.nPrice);
						boolean bExistFreeItem = false;
						for(UPromotionInfo uPromotionInfo : info.uPromotionInfoList)
						{
							uPromotionInfo.nTrxnType = info.nTrxnType;
							uPromotionInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
							uPromotionInfo.nChainNo = Global.gCurCustomerInfo.nChainNo;
							uPromotionInfo.strItemNo = info.strItemNo;

							//Global.gUOrderHInfo.nPromotionAmount += info.dSales * Utils.getDividedValue(uPromotionInfo.nAmount);
							if( uPromotionInfo.nPromoType == Global.PROMO_TYPE_BUY_FREE_MULTI )
							{
								bExistFreeItem = true;
							}
						}

						ProdLocnInfo prodLocnInfo = dbAdapter.getProdLocnInfo(info.strItemNo);
						if (prodLocnInfo != null && prodLocnInfo.strTaxFlag.equals("Y")) {
							info.strItemTaxCode = prodLocnInfo.strItemTaxCode;
						}
						//TaxRatesInfo taxRatesInfo = dbAdapter.getTaxRatesInfo(Global.gCurCustomerInfo.strTaxCode, info.strItemNo);

						if (bExistFreeItem) {
							double dFreeQty = (Utils.myDivider((float)info.dSales, info.nPromoDiscount));
							Global.gUOrderHInfo.dTotalFree += dFreeQty;
						}

					/*
					info.nTaxAmount = Utils.get5RoundedValue(mContext, Utils.getRoundedValue((float)(info.dSales * (info.nBasePrice - info.nDiscountVal))) / 100 * Utils.getDividedValue(taxRatesInfo.nTaxRate));

					info.uTaxInfo = new UTaxInfo();
					info.uTaxInfo.nTrxnNo = info.nTrxnNo;
					info.uTaxInfo.nTrxnType = info.nTrxnType;
					info.uTaxInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
					info.uTaxInfo.nLocnNo = info.nLocnNo;
					info.uTaxInfo.strItemNo = info.strItemNo;
					info.uTaxInfo.strTaxRateCode = taxRatesInfo.strTaxRateCode;
					info.uTaxInfo.nTaxAmount = info.nTaxAmount;
					info.uTaxInfo.strCumulativeFlag = taxRatesInfo.strCumulativeFlag;
					info.uTaxInfo.nTaxRate = taxRatesInfo.nTaxRate;
					info.uTaxInfo.nReasonCode = info.nReasonCode;*/

						Global.gUOrderHInfo.nTaxAmount += info.nTaxAmount;
						Global.gUOrderHInfo.nPrintCDSAmount += info.nPrintCDSAmount;
						mPrintList.add(info);
						if (info.nSCWType == Global.INVENTORY_TRUCK || info.nSCWType == Global.WEIGHT_ITEM) {
							mDeliveredCnt++;
						} else {
							mContainersCnt++;
						}
					}
				}
				else
				{
					if (info.nSales != 0 || (bPrintZeroQtyPresoldItem && info.isFromPresoldOrder)) {
						Global.gUOrderHInfo.nTotalAmount += info.nSales;
						nSalesValue += Utils.getRoundedValue(info.nSales * info.nPrice);
						if(Utils.getStringSetting(mContext, Global.PREF_INVCALC).equals("TTx"))
						{
							Global.gUOrderHInfo.nDiscSales += Utils.getRoundedValueForTTx(info.nSales * Utils.getRoundedValueForTTx(info.nBasePrice - info.nPrice));
						}
						else
						{
							Global.gUOrderHInfo.nDiscSales += info.nSales * (info.nBasePrice - info.nGrossPrice);
						}

						Global.gUOrderHInfo.nNetSales += info.nSales * (info.nPrice);
						Global.gUOrderHInfo.nPromotionAmount += info.nSales * (info.nGrossPrice - info.nPrice);
						boolean bExistFreeItem = false;
						for(UPromotionInfo uPromotionInfo : info.uPromotionInfoList)
						{
							uPromotionInfo.nTrxnType = info.nTrxnType;
							uPromotionInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
							uPromotionInfo.nChainNo = Global.gCurCustomerInfo.nChainNo;
							uPromotionInfo.strItemNo = info.strItemNo;
							//Global.gUOrderHInfo.nPromotionAmount += info.nSales * Utils.getDividedValue(uPromotionInfo.nAmount);
							if( uPromotionInfo.nPromoType == Global.PROMO_TYPE_BUY_FREE_MULTI )
							{
								bExistFreeItem = true;
							}
						}

						ProdLocnInfo prodLocnInfo = dbAdapter.getProdLocnInfo(info.strItemNo);
						if (prodLocnInfo != null && prodLocnInfo.strTaxFlag.equals("Y")) {
							info.strItemTaxCode = prodLocnInfo.strItemTaxCode;
						}
						//TaxRatesInfo taxRatesInfo = dbAdapter.getTaxRatesInfo(Global.gCurCustomerInfo.strTaxCode, info.strItemNo);

						if (bExistFreeItem) {
							int nFreeQty = Math.round(Utils.myDivider(info.nSales, info.nPromoDiscount));
							Global.gUOrderHInfo.nTotalFree += nFreeQty;
						}
						//info.nTaxAmount = Utils.get5RoundedValue(mContext, Utils.getRoundedValue((float)(info.nSales * (info.nBasePrice - info.nDiscountVal))) / 100 * Utils.getDividedValue(taxRatesInfo.nTaxRate));

					/*info.uTaxInfo = new UTaxInfo();
					info.uTaxInfo.nTrxnNo = info.nTrxnNo;
					info.uTaxInfo.nTrxnType = info.nTrxnType;
					info.uTaxInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
					info.uTaxInfo.nLocnNo = info.nLocnNo;
					info.uTaxInfo.strItemNo = info.strItemNo;
					info.uTaxInfo.strTaxRateCode = taxRatesInfo.strTaxRateCode;
					info.uTaxInfo.nTaxAmount = info.nTaxAmount;
					info.uTaxInfo.strCumulativeFlag = taxRatesInfo.strCumulativeFlag;
					info.uTaxInfo.nTaxRate = taxRatesInfo.nTaxRate;
					info.uTaxInfo.nReasonCode = info.nReasonCode;*/

						Global.gUOrderHInfo.nTaxAmount += info.nTaxAmount;
						Global.gUOrderHInfo.nPrintCDSAmount += info.nPrintCDSAmount;
						mPrintList.add(info);
						if (info.nSCWType == Global.INVENTORY_TRUCK || info.nSCWType == Global.WEIGHT_ITEM) {
							mDeliveredCnt++;
						} else {
							mContainersCnt++;
						}
					}
				}
			}
			for (UOrderDInfo info : Global.gUOrderHInfo.pickupList) {
				if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
				{
					if (info.dDumps != 0 || (bPrintZeroQtyPresoldItem && info.isFromPresoldOrder)) {
						if(info.nTrxnType == 1)
						{
							Global.gUOrderHInfo.dTotalDumps += info.dDumps;
						}
						else
						{
							Global.gUOrderHInfo.dTotalBuybacks += info.dDumps;
						}
						Global.gUOrderHInfo.dTotalAmount -= info.dDumps;
						nSalesValue -= Utils.getRoundedValue(info.dDumps * info.nPrice);
						Global.gUOrderHInfo.nPromotionAmount -= info.dDumps * (info.nGrossPrice - info.nPrice);
						boolean bExist = false;
						for(UPromotionInfo uPromotionInfo : info.uPromotionInfoList)
						{
							uPromotionInfo.nTrxnType = info.nTrxnType;
							uPromotionInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
							uPromotionInfo.nChainNo = Global.gCurCustomerInfo.nChainNo;
							uPromotionInfo.strItemNo = info.strItemNo;
							//Global.gUOrderHInfo.nPromotionAmount -= info.dDumps * Utils.getDividedValue(uPromotionInfo.nAmount);
							if( uPromotionInfo.nPromoType == Global.PROMO_TYPE_BUY_FREE_MULTI )
							{
								bExist = true;
							}
						}

						ProdLocnInfo prodLocnInfo = dbAdapter.getProdLocnInfo(info.strItemNo);
						if (prodLocnInfo != null && prodLocnInfo.strTaxFlag.equals("Y")) {
							info.strItemTaxCode = prodLocnInfo.strItemTaxCode;
						}
						//TaxRatesInfo taxRatesInfo = dbAdapter.getTaxRatesInfo(Global.gCurCustomerInfo.strTaxCode, info.strItemNo);
						if (bExist) {
							double dFreeQty = (Utils.myDivider((float)info.dDumps, info.nPromoDiscount));
							Global.gUOrderHInfo.dTotalFree += dFreeQty;
						}
					/*info.nTaxAmount = Utils.get5RoundedValue(mContext, Utils.getRoundedValue((float)(info.dDumps * (info.nBasePrice - info.nDiscountVal))) / 100 * Utils.getDividedValue(taxRatesInfo.nTaxRate));

					info.uTaxInfo = new UTaxInfo();
					info.uTaxInfo.nTrxnNo = info.nTrxnNo;
					info.uTaxInfo.nTrxnType = info.nTrxnType;
					info.uTaxInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
					info.uTaxInfo.nLocnNo = info.nLocnNo;
					info.uTaxInfo.strItemNo = info.strItemNo;
					info.uTaxInfo.strTaxRateCode = taxRatesInfo.strTaxRateCode;
					info.uTaxInfo.nTaxAmount = info.nTaxAmount;
					info.uTaxInfo.strCumulativeFlag = taxRatesInfo.strCumulativeFlag;
					info.uTaxInfo.nTaxRate = taxRatesInfo.nTaxRate;
					info.uTaxInfo.nReasonCode = info.nReasonCode;*/
						//info.nTaxAmount = info.nDumps * info.nPrice / 10;
						//Global.gUOrderHInfo.nDiscSales -= info.dDumps * info.nDiscountVal;
						if(info.nTrxnType == 1)
						{
							if(Utils.getStringSetting(mContext, Global.PREF_INVCALC).equals("TTx"))
							{
								Global.gUOrderHInfo.nDiscDumps += Utils.getRoundedValueForTTx(info.dDumps * Utils.getRoundedValueForTTx(info.nBasePrice - info.nPrice));
							}
							else
							{
								Global.gUOrderHInfo.nDiscDumps += info.dDumps * (info.nBasePrice - info.nGrossPrice);
							}

							Global.gUOrderHInfo.nNetDumps += info.dDumps * (info.nPrice);
						}
						else
						{
							if(Utils.getStringSetting(mContext, Global.PREF_INVCALC).equals("TTx"))
							{
								Global.gUOrderHInfo.nDiscBuybacks += Utils.getRoundedValueForTTx(info.dDumps * Utils.getRoundedValueForTTx(info.nBasePrice - info.nPrice));
							}
							else
							{
								Global.gUOrderHInfo.nDiscBuybacks += info.dDumps * (info.nBasePrice - info.nGrossPrice);
							}

							Global.gUOrderHInfo.nNetBuybacks += info.dDumps * (info.nPrice);
						}
						Global.gUOrderHInfo.nTaxAmount -= info.nTaxAmount;
						Global.gUOrderHInfo.nPrintCDSAmount -= info.nPrintCDSAmount;

						mPrintList.add(info);
						if ((info.nSCWType == Global.INVENTORY_TRUCK || info.nSCWType == Global.WEIGHT_ITEM) && info.nTrxnType == Global.TRXN_PICKUP) {
							mReturnsCnt++;
						} else if ((info.nSCWType == Global.INVENTORY_TRUCK || info.nSCWType == Global.WEIGHT_ITEM) && info.nTrxnType == Global.TRXN_BUYBACK) {
							mBuybackCnt++;
						} else if(info.nSCWType != Global.INVENTORY_TRUCK && info.nSCWType != Global.WEIGHT_ITEM){
							mContainersCnt++;
						}
					}
				}
				else
				{
					if (info.nDumps != 0 || (bPrintZeroQtyPresoldItem && info.isFromPresoldOrder)) {
						if(info.nTrxnType == 1)
						{
							Global.gUOrderHInfo.nTotalDumps += info.nDumps;
						}
						else
						{
							Global.gUOrderHInfo.nTotalBuybacks += info.nDumps;
						}
						Global.gUOrderHInfo.nTotalAmount -= info.nDumps;
						nSalesValue -= Utils.getRoundedValue(info.nDumps * info.nPrice);
						Global.gUOrderHInfo.nPromotionAmount -= info.nDumps * (info.nGrossPrice - info.nPrice);
						boolean bExist = false;
						for(UPromotionInfo uPromotionInfo : info.uPromotionInfoList)
						{
							uPromotionInfo.nTrxnType = info.nTrxnType;
							uPromotionInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
							uPromotionInfo.nChainNo = Global.gCurCustomerInfo.nChainNo;
							uPromotionInfo.strItemNo = info.strItemNo;
							//Global.gUOrderHInfo.nPromotionAmount -= info.nDumps * Utils.getDividedValue(uPromotionInfo.nAmount);
							if( uPromotionInfo.nPromoType == Global.PROMO_TYPE_BUY_FREE_MULTI )
							{
								bExist = true;
							}
						}

						ProdLocnInfo prodLocnInfo = dbAdapter.getProdLocnInfo(info.strItemNo);
						if (prodLocnInfo != null && prodLocnInfo.strTaxFlag.equals("Y")) {
							info.strItemTaxCode = prodLocnInfo.strItemTaxCode;
						}
						//TaxRatesInfo taxRatesInfo = dbAdapter.getTaxRatesInfo(Global.gCurCustomerInfo.strTaxCode, info.strItemNo);
						if (bExist) {
							int nFreeQty = Math.round(Utils.myDivider(info.nDumps, info.nPromoDiscount));
							Global.gUOrderHInfo.nTotalFree += nFreeQty;
						}
						//info.nTaxAmount = Utils.get5RoundedValue(mContext, Utils.getRoundedValue((float)(info.nDumps * (info.nBasePrice - info.nDiscountVal))) / 100 * Utils.getDividedValue(taxRatesInfo.nTaxRate));

					/*info.uTaxInfo = new UTaxInfo();
					info.uTaxInfo.nTrxnNo = info.nTrxnNo;
					info.uTaxInfo.nTrxnType = info.nTrxnType;
					info.uTaxInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
					info.uTaxInfo.nLocnNo = info.nLocnNo;
					info.uTaxInfo.strItemNo = info.strItemNo;
					info.uTaxInfo.strTaxRateCode = taxRatesInfo.strTaxRateCode;
					info.uTaxInfo.nTaxAmount = info.nTaxAmount;
					info.uTaxInfo.strCumulativeFlag = taxRatesInfo.strCumulativeFlag;
					info.uTaxInfo.nTaxRate = taxRatesInfo.nTaxRate;
					info.uTaxInfo.nReasonCode = info.nReasonCode;*/
						//info.nTaxAmount = info.nDumps * info.nPrice / 10;
						//Global.gUOrderHInfo.nDiscSales -= info.nDumps * info.nDiscountVal;
						if(info.nTrxnType == 1)
						{
							if(Utils.getStringSetting(mContext, Global.PREF_INVCALC).equals("TTx")) {
								Global.gUOrderHInfo.nDiscDumps += Utils.getRoundedValueForTTx(info.nDumps * Utils.getRoundedValueForTTx(info.nBasePrice - info.nPrice));
							} else {
								Global.gUOrderHInfo.nDiscDumps += info.nDumps * (info.nBasePrice - info.nGrossPrice);
							}

							Global.gUOrderHInfo.nNetDumps += info.nDumps * (info.nPrice);
						}
						else
						{
							if(Utils.getStringSetting(mContext, Global.PREF_INVCALC).equals("TTx")) {
								Global.gUOrderHInfo.nDiscBuybacks += Utils.getRoundedValueForTTx(info.nDumps * Utils.getRoundedValueForTTx(info.nBasePrice - info.nPrice));
							}
							else
							{
								Global.gUOrderHInfo.nDiscBuybacks += info.nDumps * (info.nBasePrice - info.nGrossPrice);
							}
							Global.gUOrderHInfo.nNetBuybacks += info.nDumps * (info.nPrice);
						}
						Global.gUOrderHInfo.nTaxAmount -= info.nTaxAmount;
						Global.gUOrderHInfo.nPrintCDSAmount -= info.nPrintCDSAmount;
						mPrintList.add(info);
						if ((info.nSCWType == Global.INVENTORY_TRUCK || info.nSCWType == Global.WEIGHT_ITEM) && info.nTrxnType == Global.TRXN_PICKUP) {
							mReturnsCnt++;
						} else if ((info.nSCWType == Global.INVENTORY_TRUCK || info.nSCWType == Global.WEIGHT_ITEM) && info.nTrxnType == Global.TRXN_BUYBACK) {
							mBuybackCnt++;
						} else if(info.nSCWType != Global.INVENTORY_TRUCK && info.nSCWType != Global.WEIGHT_ITEM){
							mContainersCnt++;
						}
					}
				}
			}
			dbAdapter.close();
			for (UOrderDInfo containerInfo : Global.gUOrderHInfo.containerList) {
				if ((containerInfo.nTrxnType == Global.TRXN_CONTAINER_SALE && containerInfo.nSales != 0)
						|| (containerInfo.nTrxnType == Global.TRXN_CONTAINER_DUMP && containerInfo.nDumps != 0)) {
					UOrderDInfo existInfo = null;
					for(UOrderDInfo info : mPrintList )
					{
						if( info.strItemNo.equals(containerInfo.strItemNo) && info.nSCWType == Global.INVENTORY_CONTAINERS)
						{
							existInfo = info;
							break;
						}
					}
					if( existInfo != null )
					{
						existInfo.nQuantity += (containerInfo.nSales - containerInfo.nDumps);
					}
					else
					{
						mContainersCnt++;
						containerInfo.nSCWType = Global.INVENTORY_CONTAINERS;
						containerInfo.nQuantity = containerInfo.nSales - containerInfo.nDumps;
						mPrintList.add(containerInfo);
					}
				}
			}

			Global.gUOrderHInfo.nSaleAmount = Utils.getRoundedValue(nSalesValue);
		}

		if(Utils.getStringSetting(mContext, Global.PREF_SUMMARY_DETAIL).equals("1"))
		{
			if(mPrintList.size() > 0)
			{
				//Refresh Summary values.
				mRootView.findViewById(R.id.layoutDetailSummary).setVisibility(View.VISIBLE);
				if(mCalculating) return;
				showProgressDialog(Str.Loading[Global.LANG]);
				mCalculating = true;
				new Thread(){
					@Override
					public void run() {
						final PrintEngine printEngine = new PrintEngine();
						List<UOrderDInfo> uOrderDInfoList = new ArrayList<>();
						uOrderDInfoList.addAll(mPrintList);
						EditText deliverTemp = mRootView.findViewById(R.id.editDeliverTemp);
						String strTemp = deliverTemp.getText().toString();
						printEngine.PreparePrint(mContext, Global.gCurCustomerInfo, uOrderDInfoList, mPayMethod, bReverseMode, m_bFutureOrder, priceGrpInfoMap, altPriceGrpInfoMap, null, strTemp, bPartialPayment, fPartialPaidAmount);

						new Handler(Looper.getMainLooper()).post(new Runnable() {
							@Override
							public void run() {
								hideProgressDialog();
								LinearLayout layoutSummaryInfo = mRootView.findViewById(R.id.layoutDetailInfo);

								layoutSummaryInfo.removeAllViews();
								String strTotalSaleCase = (String)printEngine.mObjectMap.get("total_sale_case");
								if(!TextUtils.isEmpty(strTotalSaleCase) && !strTotalSaleCase.equals("0"))
								{
									View view = mContext.getLayoutInflater().inflate(R.layout.row_summary_detail, layoutSummaryInfo, false);
									((TextView)view.findViewById(R.id.lblCaption)).setText(Str.TotalSales[Global.LANG]);
									((TextView)view.findViewById(R.id.txtCase)).setText(strTotalSaleCase);
									((TextView)view.findViewById(R.id.txtUnitPrice)).setText((String)printEngine.mObjectMap.get("total_sale_unit_price"));
									((TextView)view.findViewById(R.id.txtDiscAmt)).setText((String)printEngine.mObjectMap.get("total_sale_disc_amount"));
									((TextView)view.findViewById(R.id.txtNetAmt)).setText((String)printEngine.mObjectMap.get("total_sale_net_amount"));
									layoutSummaryInfo.addView(view);
								}

								String strTotalFreeGoods = (String)printEngine.mObjectMap.get("total_free_goods");
								if(!TextUtils.isEmpty(strTotalFreeGoods) && !strTotalFreeGoods.equals("0"))
								{
									View view = mContext.getLayoutInflater().inflate(R.layout.row_summary_detail, layoutSummaryInfo, false);
									((TextView)view.findViewById(R.id.lblCaption)).setText(Str.TotalFreeGoods[Global.LANG]);
									((TextView)view.findViewById(R.id.txtCase)).setText(strTotalFreeGoods);
									((TextView)view.findViewById(R.id.txtUnitPrice)).setText((String)printEngine.mObjectMap.get("total_free_unit_price"));
									((TextView)view.findViewById(R.id.txtDiscAmt)).setText((String)printEngine.mObjectMap.get("total_free_disc_amount"));
									((TextView)view.findViewById(R.id.txtNetAmt)).setText((String)printEngine.mObjectMap.get("total_free_net_amount"));
									layoutSummaryInfo.addView(view);
								}

								String strTotalCredits = (String)printEngine.mObjectMap.get("total_credits");
								if(!TextUtils.isEmpty(strTotalCredits) && !strTotalCredits.equals("0"))
								{
									View view = mContext.getLayoutInflater().inflate(R.layout.row_summary_detail, layoutSummaryInfo, false);
									((TextView)view.findViewById(R.id.lblCaption)).setText(Str.TotalCredits[Global.LANG]);
									((TextView)view.findViewById(R.id.txtCase)).setText(strTotalCredits);
									((TextView)view.findViewById(R.id.txtUnitPrice)).setText((String)printEngine.mObjectMap.get("total_credits_unit_price"));
									((TextView)view.findViewById(R.id.txtDiscAmt)).setText((String)printEngine.mObjectMap.get("total_credits_disc_amount"));
									((TextView)view.findViewById(R.id.txtNetAmt)).setText((String)printEngine.mObjectMap.get("total_credits_net_amount"));
									layoutSummaryInfo.addView(view);
								}

								String strTotalBuybacks = (String)printEngine.mObjectMap.get("total_buybacks");
								if(!TextUtils.isEmpty(strTotalBuybacks) && !strTotalBuybacks.equals("0"))
								{
									View view = mContext.getLayoutInflater().inflate(R.layout.row_summary_detail, layoutSummaryInfo, false);
									((TextView)view.findViewById(R.id.lblCaption)).setText(Str.TotalBuybacks[Global.LANG]);
									((TextView)view.findViewById(R.id.txtCase)).setText(strTotalCredits);
									((TextView)view.findViewById(R.id.txtUnitPrice)).setText((String)printEngine.mObjectMap.get("total_buybacks_unit_price"));
									((TextView)view.findViewById(R.id.txtDiscAmt)).setText((String)printEngine.mObjectMap.get("total_buybacks_disc_amount"));
									((TextView)view.findViewById(R.id.txtNetAmt)).setText((String)printEngine.mObjectMap.get("total_buybacks_net_amount"));
									layoutSummaryInfo.addView(view);
								}

								((TextView)mRootView.findViewById(R.id.txtSubTotal)).setText(Global.gCurrencySymbol + (String)printEngine.mObjectMap.get("SubTotal"));
								((TextView)mRootView.findViewById(R.id.txtGST)).setText(Global.gCurrencySymbol + ((List<String>)printEngine.mObjectMap.get("GST")).get(0));
								((TextView)mRootView.findViewById(R.id.txtInvoiceTotal)).setText(Global.gCurrencySymbol + (String)printEngine.mObjectMap.get("InvoiceGSTTotal"));

								mCalculating = false;
							}
						});
					}
				}.start();

			}
			else
			{
				mRootView.findViewById(R.id.layoutDetailSummary).setVisibility(View.GONE);
			}
		}
	}

	@Override
	public void refreshPage() {
		if(!bConsignment)
		{
			calculateTotal();

			//		int nCartons = 0;
			int nUnits = 0;
			int nContainers = 0;
			double dValue = 0;
			for (UOrderDInfo info : Global.gUOrderHInfo.deliveryList) {
				if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
				{
					dValue += info.dSales;
				}
				else{
					if (info.nSCWType == Global.INVENTORY_TRUCK || info.nSCWType == Global.WEIGHT_ITEM) {
						nUnits += info.nSales;
					} else {
						nContainers += info.nSales;
					}
				}
			}
			for (UOrderDInfo info : Global.gUOrderHInfo.pickupList) {
				if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
				{
					dValue -= info.dDumps;
				}
				else{
					if (info.nSCWType == Global.INVENTORY_TRUCK || info.nSCWType == Global.WEIGHT_ITEM) {
						nUnits -= info.nDumps;
					} else {
						nContainers -= info.nDumps;
					}
				}
			}
			for (UOrderDInfo info : Global.gUOrderHInfo.containerList) {
				if (info.nTrxnType == Global.TRXN_CONTAINER_SALE) {
					nContainers += info.nSales;
				} else {
					nContainers -= info.nDumps;
				}
			}
			if(dValue > 0)
			{
				((TextView) mRootView.findViewById(R.id.txtCartons)).setText(String.format(Locale.ENGLISH, "%.2f Kg", dValue));
			}
			else
			{
				((TextView) mRootView.findViewById(R.id.txtCartons)).setText("");
			}
			String strCartonDescription = "";
			if(TextUtils.isEmpty(mCartonDescription))
			{
				strCartonDescription = "unit";
			}
			else
			{
				strCartonDescription = mCartonDescription;
			}

			if(nUnits == 0)
			{
				((TextView) mRootView.findViewById(R.id.txtUnits)).setText("");
			}
			else if(nUnits == 1 || nUnits == -1)
			{
				((TextView) mRootView.findViewById(R.id.txtUnits)).setText(String.format(Locale.ENGLISH, "%d %s", nUnits, strCartonDescription));
			}
			else
			{
				((TextView) mRootView.findViewById(R.id.txtUnits)).setText(String.format(Locale.ENGLISH, "%d %ss", nUnits, strCartonDescription));
			}

			if(nContainers == 0)
			{
				((TextView) mRootView.findViewById(R.id.txtContainers)).setText("");
			}
			else if( nContainers == 1 || nContainers == -1)
			{
				((TextView) mRootView.findViewById(R.id.txtContainers)).setText(String.format(Locale.ENGLISH, "%d container", nContainers));
			}
			else
			{
				((TextView) mRootView.findViewById(R.id.txtContainers)).setText(String.format(Locale.ENGLISH, "%d containers", nContainers));
			}


			if (nUnits < 0) {
				((TextView) mRootView.findViewById(R.id.txtUnits)).setTextColor(Color.RED);
			}
			if (nContainers < 0) {
				((TextView) mRootView.findViewById(R.id.txtContainers)).setTextColor(Color.RED);
			}


			DbAdapter dbAdapter = new DbAdapter(mContext);
			dbAdapter.open();
			float dOutstandingValue = 0.0f;
			List<ARHeaderInfo> infos = dbAdapter.getARHeaderInfo(Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo);
			dbAdapter.close();
			for(ARHeaderInfo arHeaderInfo : infos)
			{
				if( arHeaderInfo.nProcessStatus < 2 )
				{
					dOutstandingValue += arHeaderInfo.nTrxnAmount * 1.0 / Global.MULTIPLE_VALUE;
				}
			}
			if(Global.gCurCustomerInfo.nShowPrice != 2 )
			{
				((TextView) mRootView.findViewById(R.id.txtOutstandingValue)).setText(String.format(Locale.ENGLISH, "%s" + "%." + Utils.getIntSetting(mContext, Global.PREF_DECIMAL) + "f", Global.gCurrencySymbol, getRoundedValue(Utils.getRoundedValue(dOutstandingValue))));
				((TextView) mRootView.findViewById(R.id.txtTotalValue)).setText(String.format(Locale.ENGLISH, "%s" + "%." + Utils.getIntSetting(mContext, Global.PREF_DECIMAL) + "f", Global.gCurrencySymbol, getRoundedValue(Utils.getRoundedValue(Global.gUOrderHInfo.nSaleAmount + Global.gUOrderHInfo.nTaxAmount + Global.gUOrderHInfo.nHandlingFee + Global.gUOrderHInfo.nHandlingFeeTaxAmount))));
				Global.gCurTotalValue = getRoundedValue(dOutstandingValue + Global.gUOrderHInfo.nSaleAmount + Global.gUOrderHInfo.nTaxAmount + Global.gUOrderHInfo.nHandlingFee + Global.gUOrderHInfo.nHandlingFeeTaxAmount);
				Global.gCurTotalDueInvoice = getRoundedValue(Global.gUOrderHInfo.nSaleAmount + Global.gUOrderHInfo.nTaxAmount + Global.gUOrderHInfo.nHandlingFee + Global.gUOrderHInfo.nHandlingFeeTaxAmount);
				((TextView) mRootView.findViewById(R.id.txtTotalAllValue)).setText(String.format(Locale.ENGLISH, "%s" + "%." + Utils.getIntSetting(mContext, Global.PREF_DECIMAL) + "f", Global.gCurrencySymbol, getRoundedValue(Utils.getRoundedValue(dOutstandingValue) + getRoundedValue(Utils.getRoundedValue(Global.gUOrderHInfo.nSaleAmount + Global.gUOrderHInfo.nTaxAmount + Global.gUOrderHInfo.nHandlingFee + Global.gUOrderHInfo.nHandlingFeeTaxAmount)))));
			}
			else
			{
				((TextView) mRootView.findViewById(R.id.txtOutstandingValue)).setText(String.format(Locale.ENGLISH, "%s" + "%." + Utils.getIntSetting(mContext, Global.PREF_DECIMAL) + "f", Global.gCurrencySymbol, 0f));
				((TextView) mRootView.findViewById(R.id.txtTotalValue)).setText(String.format(Locale.ENGLISH, "%s" + "%." + Utils.getIntSetting(mContext, Global.PREF_DECIMAL) + "f", Global.gCurrencySymbol, 0f));
				((TextView) mRootView.findViewById(R.id.txtTotalAllValue)).setText(String.format(Locale.ENGLISH, "%s" + "%." + Utils.getIntSetting(mContext, Global.PREF_DECIMAL) + "f", Global.gCurrencySymbol, 0f));
			}


			if( Global.gCurCustomerInfo.strPayType.equals("0") || Global.gCurCustomerInfo.strPayType.length() == 0 )
			{
				((TextView)mRootView.findViewById(R.id.lblTotalValue)).setTextColor(mContext.getResources().getColor(R.color.gray_color));
				((TextView)mRootView.findViewById(R.id.lblCurrencySymbol)).setTextColor(mContext.getResources().getColor(R.color.gray_color));
				((TextView)mRootView.findViewById(R.id.txtTotalValue)).setTextColor(mContext.getResources().getColor(R.color.gray_color));
				((TextView) mRootView.findViewById(R.id.txtOutstandingValue)).setTextColor(mContext.getResources().getColor(R.color.gray_color));
				((TextView) mRootView.findViewById(R.id.txtTotalAllValue)).setTextColor(mContext.getResources().getColor(R.color.gray_color));

				((TextView) mRootView.findViewById(R.id.lblTotalCollection)).setTextColor(mContext.getResources().getColor(R.color.gray_color));
				((TextView) mRootView.findViewById(R.id.lblCurrencySymbol)).setTextColor(mContext.getResources().getColor(R.color.gray_color));
				((EditText) mRootView.findViewById(R.id.editTotalCollection)).setTextColor(mContext.getResources().getColor(R.color.gray_color));
				((TextView) mRootView.findViewById(R.id.lblFullPaymentBy)).setTextColor(mContext.getResources().getColor(R.color.gray_color));
				((TextView) mRootView.findViewById(R.id.lblTerms)).setTextColor(mContext.getResources().getColor(R.color.gray_color));
				mRootView.findViewById(R.id.chkCard).setEnabled(false);
				mRootView.findViewById(R.id.chkCash).setEnabled(false);
				mRootView.findViewById(R.id.chkCheque).setEnabled(false);
				((CheckBox)mRootView.findViewById(R.id.chkCard)).setTextColor(mContext.getResources().getColor(R.color.gray_color));
				((CheckBox)mRootView.findViewById(R.id.chkCash)).setTextColor(mContext.getResources().getColor(R.color.gray_color));
				((CheckBox)mRootView.findViewById(R.id.chkCheque)).setTextColor(mContext.getResources().getColor(R.color.gray_color));
				mRootView.findViewById(R.id.editTotalCollection).setEnabled(false);

				mRootView.findViewById(R.id.btnDetail).setVisibility(View.INVISIBLE);

			}
			else
			{
				String strText = ((TextView) mRootView.findViewById(R.id.txtTotalAllValue)).getText().toString();
				float fValue = Utils.getFloatFromString(strText.substring(Global.gCurrencySymbol.length()));
				boolean bEnable = true;
				if( Global.gCurCustomerInfo.nShowPrice == 2 || Utils.getStringSetting(mContext, Global.PREF_HIDE_SUMMARY_PAYMENT).equals("1")
					/*|| Global.gCurCustomerInfo.strPayType.trim().equals("1") || Global.gCurCustomerInfo.strPayType.trim().equals("2") || Global.gCurCustomerInfo.strPayType.trim().equals("$")*/
						/*|| (!Global.gCurCustomerInfo.strPayType.trim().equals("1") && !Global.gCurCustomerInfo.strPayType.trim().equals("2")
								&& Utils.getStringSetting(mContext, Global.PREF_ROUTE_PAYMENTADJUST).trim().equals("1"))*/)
				{
					bEnable = false;
				}

				if( fValue != 0.0f && bEnable)
				{
					((TextView)mRootView.findViewById(R.id.lblTotalValue)).setTextColor(Color.BLACK);
					((TextView)mRootView.findViewById(R.id.lblCurrencySymbol)).setTextColor(Color.BLACK);
					if( Global.gCurCustomerInfo.nShowPrice > 0)
					{
						if ((Global.gUOrderHInfo.nSaleAmount + Global.gUOrderHInfo.nTaxAmount + Global.gUOrderHInfo.nHandlingFee + Global.gUOrderHInfo.nHandlingFeeTaxAmount) < 0) {
							((TextView) mRootView.findViewById(R.id.txtTotalValue)).setTextColor(Color.RED);
						}
						else
						{
							((TextView) mRootView.findViewById(R.id.txtTotalValue)).setTextColor(Color.BLACK);
						}

						if(dOutstandingValue < 0 && Global.gCurCustomerInfo.nShowPrice != 2)
						{
							((TextView) mRootView.findViewById(R.id.txtOutstandingValue)).setTextColor(Color.RED);
						}
						else
						{
							((TextView) mRootView.findViewById(R.id.txtOutstandingValue)).setTextColor(Color.BLACK);
						}

						if(dOutstandingValue + Global.gUOrderHInfo.nSaleAmount + Global.gUOrderHInfo.nTaxAmount + Global.gUOrderHInfo.nHandlingFee + Global.gUOrderHInfo.nHandlingFeeTaxAmount < 0)
						{
							((TextView) mRootView.findViewById(R.id.txtTotalAllValue)).setTextColor(Color.RED);
						}
						else
						{
							((TextView) mRootView.findViewById(R.id.txtTotalAllValue)).setTextColor(Color.BLACK);
						}
					}
					((TextView) mRootView.findViewById(R.id.lblTotalCollection)).setTextColor(mContext.getResources().getColor(R.color.black_color));
					((TextView) mRootView.findViewById(R.id.lblCurrencySymbol)).setTextColor(mContext.getResources().getColor(R.color.black_color));
					((EditText) mRootView.findViewById(R.id.editTotalCollection)).setTextColor(mContext.getResources().getColor(R.color.black_color));
					((TextView) mRootView.findViewById(R.id.lblFullPaymentBy)).setTextColor(mContext.getResources().getColor(R.color.black_color));
					((TextView) mRootView.findViewById(R.id.lblTerms)).setTextColor(mContext.getResources().getColor(R.color.black_color));
					if(Utils.getIntSetting(mContext, Global.PREF_EWAY_CARD_PROC) == 0)
					{
						mRootView.findViewById(R.id.chkCard).setEnabled(false);
						((CheckBox)mRootView.findViewById(R.id.chkCard)).setTextColor(Color.GRAY);
					}
					else
					{
						mRootView.findViewById(R.id.chkCard).setEnabled(true);
						((CheckBox)mRootView.findViewById(R.id.chkCard)).setTextColor(mContext.getResources().getColor(R.color.black_color));
					}

					mRootView.findViewById(R.id.chkCash).setEnabled(true);
					mRootView.findViewById(R.id.chkCheque).setEnabled(true);
					if(Global.gCurCustomerInfo.strPayType.equals("B"))
					{
						mRootView.findViewById(R.id.editTotalCollection).setEnabled(false);
					}
					else
					{
						mRootView.findViewById(R.id.editTotalCollection).setEnabled(true);
					}

					((CheckBox)mRootView.findViewById(R.id.chkCash)).setTextColor(mContext.getResources().getColor(R.color.black_color));
					((CheckBox)mRootView.findViewById(R.id.chkCheque)).setTextColor(mContext.getResources().getColor(R.color.black_color));

					mRootView.findViewById(R.id.btnDetail).setVisibility(View.VISIBLE);
				}
				else
				{
					((TextView)mRootView.findViewById(R.id.lblTotalValue)).setTextColor(mContext.getResources().getColor(R.color.gray_color));
					((TextView)mRootView.findViewById(R.id.lblCurrencySymbol)).setTextColor(mContext.getResources().getColor(R.color.gray_color));
					((TextView)mRootView.findViewById(R.id.txtTotalValue)).setTextColor(mContext.getResources().getColor(R.color.gray_color));
					((TextView) mRootView.findViewById(R.id.txtOutstandingValue)).setTextColor(mContext.getResources().getColor(R.color.gray_color));
					((TextView) mRootView.findViewById(R.id.txtTotalAllValue)).setTextColor(mContext.getResources().getColor(R.color.gray_color));

					((TextView) mRootView.findViewById(R.id.lblTotalCollection)).setTextColor(mContext.getResources().getColor(R.color.gray_color));
					((TextView) mRootView.findViewById(R.id.lblCurrencySymbol)).setTextColor(mContext.getResources().getColor(R.color.gray_color));
					((EditText) mRootView.findViewById(R.id.editTotalCollection)).setTextColor(mContext.getResources().getColor(R.color.gray_color));
					((TextView) mRootView.findViewById(R.id.lblFullPaymentBy)).setTextColor(mContext.getResources().getColor(R.color.gray_color));
					((TextView) mRootView.findViewById(R.id.lblTerms)).setTextColor(mContext.getResources().getColor(R.color.gray_color));
					mRootView.findViewById(R.id.chkCard).setEnabled(false);
					mRootView.findViewById(R.id.chkCash).setEnabled(false);
					mRootView.findViewById(R.id.chkCheque).setEnabled(false);
					mRootView.findViewById(R.id.editTotalCollection).setEnabled(false);
					((CheckBox)mRootView.findViewById(R.id.chkCard)).setTextColor(mContext.getResources().getColor(R.color.gray_color));
					((CheckBox)mRootView.findViewById(R.id.chkCash)).setTextColor(mContext.getResources().getColor(R.color.gray_color));
					((CheckBox)mRootView.findViewById(R.id.chkCheque)).setTextColor(mContext.getResources().getColor(R.color.gray_color));
				}
			}

			//showTimes();

			if(Global.gCurCustomerInfo != null && Global.gCurCustomerInfo.presoldOrHInfo != null && Global.gCurCustomerInfo.presoldOrHInfo.size() == 1 &&
					Global.gCurCustomerInfo.presoldOrHInfo.get(0).nVisited == 0 )
			{
				if(!Utils.getStringSetting(mContext, Global.PREF_UNLESHED).equals("1"))
				{
					((EditText) mRootView.findViewById(R.id.editReceiptNumber)).setText(Global.gUOrderHInfo.strPOReference);
				}
				((EditText) mRootView.findViewById(R.id.editReceiverName)).setText(Global.gUOrderHInfo.strOrderName);
			}
		}
		else
		{
			Global.gUOrderHInfo.nSaleAmount = 0;
			Global.gUOrderHInfo.nTaxAmount = 0;
			for(UOrderDInfo uOrderDInfo : Global.gUOrderHInfo.deliveryList)
			{
				if(Utils.getProdLoconSCWType(mContext, uOrderDInfo.strItemNo) == Global.WEIGHT_ITEM)
				{
					Global.gUOrderHInfo.nSaleAmount += Utils.getRoundedValue(uOrderDInfo.nPrice * (uOrderDInfo.dPlanQty - uOrderDInfo.dSales));
					Global.gUOrderHInfo.nTaxAmount += Utils.get5RoundedValue(mContext, uOrderDInfo.nTaxAmount);
				}
				else
				{
					Global.gUOrderHInfo.nSaleAmount += Utils.getRoundedValue(uOrderDInfo.nPrice * (uOrderDInfo.nPlanQty - uOrderDInfo.nSales));
					Global.gUOrderHInfo.nTaxAmount += Utils.get5RoundedValue(mContext, uOrderDInfo.nTaxAmount);
				}
			}
			float dValue = 0;
			int nUnits = 0;
			for (UOrderDInfo info : Global.gUOrderHInfo.deliveryList) {
				if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
				{
					dValue += Utils.parseFloatFromString(String.format(Locale.ENGLISH, "%.2f", info.dPlanQty - info.dSales));
				}
				else{
					nUnits += info.nPlanQty - info.nSales;
				}
			}

			if(dValue > 0)
			{
				((TextView) mRootView.findViewById(R.id.txtCartons)).setText(String.format(Locale.ENGLISH, "%.2f Kg", dValue));
			}
			else
			{
				((TextView) mRootView.findViewById(R.id.txtCartons)).setText("");
			}

			String strCartonDescription = "";
			if(TextUtils.isEmpty(mCartonDescription))
			{
				strCartonDescription = "unit";
			}
			else
			{
				strCartonDescription = mCartonDescription;
			}

			if(nUnits == 0)
			{
				((TextView) mRootView.findViewById(R.id.txtUnits)).setText("");
			}
			else if(nUnits == 1 || nUnits == -1)
			{
				((TextView) mRootView.findViewById(R.id.txtUnits)).setText(String.format(Locale.ENGLISH, "%d %s", nUnits, strCartonDescription));
			}
			else
			{
				((TextView) mRootView.findViewById(R.id.txtUnits)).setText(String.format(Locale.ENGLISH, "%d %ss", nUnits, strCartonDescription));
			}

			DbAdapter dbAdapter = new DbAdapter(mContext);
			dbAdapter.open();
			float dOutstandingValue = 0.0f;
			List<ARHeaderInfo> infos = dbAdapter.getARHeaderInfo(Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo);
			dbAdapter.close();

			for(ARHeaderInfo arHeaderInfo : infos)
			{
			if( arHeaderInfo.nProcessStatus < 2 )
				{
					dOutstandingValue += arHeaderInfo.nTrxnAmount * 1.0 / Global.MULTIPLE_VALUE;
				}
			}
			if(Global.gCurCustomerInfo.nShowPrice != 2 )
			{
				((TextView) mRootView.findViewById(R.id.txtOutstandingValue)).setText(String.format(Locale.ENGLISH, "%s" + "%." + Utils.getIntSetting(mContext, Global.PREF_DECIMAL) + "f", Global.gCurrencySymbol, getRoundedValue(Utils.getRoundedValue(dOutstandingValue))));
				((TextView) mRootView.findViewById(R.id.txtTotalValue)).setText(String.format(Locale.ENGLISH, "%s" + "%." + Utils.getIntSetting(mContext, Global.PREF_DECIMAL) + "f", Global.gCurrencySymbol, getRoundedValue(Utils.getRoundedValue(Global.gUOrderHInfo.nSaleAmount + Global.gUOrderHInfo.nTaxAmount + Global.gUOrderHInfo.nHandlingFee + Global.gUOrderHInfo.nHandlingFeeTaxAmount))));
				Global.gCurTotalValue = getRoundedValue(dOutstandingValue + Global.gUOrderHInfo.nSaleAmount + Global.gUOrderHInfo.nTaxAmount + Global.gUOrderHInfo.nHandlingFee + Global.gUOrderHInfo.nHandlingFeeTaxAmount);
				Global.gCurTotalDueInvoice = getRoundedValue(Global.gUOrderHInfo.nSaleAmount + Global.gUOrderHInfo.nTaxAmount + Global.gUOrderHInfo.nHandlingFee + Global.gUOrderHInfo.nHandlingFeeTaxAmount);
				((TextView) mRootView.findViewById(R.id.txtTotalAllValue)).setText(String.format(Locale.ENGLISH, "%s" + "%." + Utils.getIntSetting(mContext, Global.PREF_DECIMAL) + "f", Global.gCurrencySymbol, getRoundedValue(Utils.getRoundedValue(dOutstandingValue) + getRoundedValue(Utils.getRoundedValue(Global.gUOrderHInfo.nSaleAmount + Global.gUOrderHInfo.nTaxAmount + Global.gUOrderHInfo.nHandlingFee + Global.gUOrderHInfo.nHandlingFeeTaxAmount)))));
			}
			else
			{
				((TextView) mRootView.findViewById(R.id.txtOutstandingValue)).setText(String.format(Locale.ENGLISH, "%s" + "%." + Utils.getIntSetting(mContext, Global.PREF_DECIMAL) + "f", Global.gCurrencySymbol, 0f));
				((TextView) mRootView.findViewById(R.id.txtTotalValue)).setText(String.format(Locale.ENGLISH, "%s" + "%." + Utils.getIntSetting(mContext, Global.PREF_DECIMAL) + "f", Global.gCurrencySymbol, 0f));
				((TextView) mRootView.findViewById(R.id.txtTotalAllValue)).setText(String.format(Locale.ENGLISH, "%s" + "%." + Utils.getIntSetting(mContext, Global.PREF_DECIMAL) + "f", Global.gCurrencySymbol, 0f));
			}


			if( Global.gCurCustomerInfo.strPayType.equals("0") || Global.gCurCustomerInfo.strPayType.length() == 0 )
			{
				((TextView)mRootView.findViewById(R.id.lblTotalValue)).setTextColor(mContext.getResources().getColor(R.color.gray_color));
				((TextView)mRootView.findViewById(R.id.lblCurrencySymbol)).setTextColor(mContext.getResources().getColor(R.color.gray_color));
				((TextView)mRootView.findViewById(R.id.txtTotalValue)).setTextColor(mContext.getResources().getColor(R.color.gray_color));
				((TextView) mRootView.findViewById(R.id.txtOutstandingValue)).setTextColor(mContext.getResources().getColor(R.color.gray_color));
				((TextView) mRootView.findViewById(R.id.txtTotalAllValue)).setTextColor(mContext.getResources().getColor(R.color.gray_color));

				((TextView) mRootView.findViewById(R.id.lblTotalCollection)).setTextColor(mContext.getResources().getColor(R.color.gray_color));
				((TextView) mRootView.findViewById(R.id.lblCurrencySymbol)).setTextColor(mContext.getResources().getColor(R.color.gray_color));
				((EditText) mRootView.findViewById(R.id.editTotalCollection)).setTextColor(mContext.getResources().getColor(R.color.gray_color));
				((TextView) mRootView.findViewById(R.id.lblFullPaymentBy)).setTextColor(mContext.getResources().getColor(R.color.gray_color));
				((TextView) mRootView.findViewById(R.id.lblTerms)).setTextColor(mContext.getResources().getColor(R.color.gray_color));
				mRootView.findViewById(R.id.chkCard).setEnabled(false);
				mRootView.findViewById(R.id.chkCash).setEnabled(false);
				mRootView.findViewById(R.id.chkCheque).setEnabled(false);
				((CheckBox)mRootView.findViewById(R.id.chkCard)).setTextColor(mContext.getResources().getColor(R.color.gray_color));
				((CheckBox)mRootView.findViewById(R.id.chkCash)).setTextColor(mContext.getResources().getColor(R.color.gray_color));
				((CheckBox)mRootView.findViewById(R.id.chkCheque)).setTextColor(mContext.getResources().getColor(R.color.gray_color));
				mRootView.findViewById(R.id.editTotalCollection).setEnabled(false);

				mRootView.findViewById(R.id.btnDetail).setVisibility(View.INVISIBLE);

			}
			else
			{
				String strText = ((TextView) mRootView.findViewById(R.id.txtTotalAllValue)).getText().toString();
				float fValue = Utils.getFloatFromString(strText.substring(Global.gCurrencySymbol.length()));
				if( fValue != 0.0f )
				{
					((TextView)mRootView.findViewById(R.id.lblTotalValue)).setTextColor(Color.BLACK);
					((TextView)mRootView.findViewById(R.id.lblCurrencySymbol)).setTextColor(Color.BLACK);
					if( Global.gCurCustomerInfo.nShowPrice > 0)
					{
						if ((Global.gUOrderHInfo.nSaleAmount + Global.gUOrderHInfo.nTaxAmount + Global.gUOrderHInfo.nHandlingFee + Global.gUOrderHInfo.nHandlingFeeTaxAmount) < 0) {
							((TextView) mRootView.findViewById(R.id.txtTotalValue)).setTextColor(Color.RED);
						}
						else
						{
							((TextView) mRootView.findViewById(R.id.txtTotalValue)).setTextColor(Color.BLACK);
						}

						if(dOutstandingValue < 0 && Global.gCurCustomerInfo.nShowPrice != 2)
						{
							((TextView) mRootView.findViewById(R.id.txtOutstandingValue)).setTextColor(Color.RED);
						}
						else
						{
							((TextView) mRootView.findViewById(R.id.txtOutstandingValue)).setTextColor(Color.BLACK);
						}

						if(dOutstandingValue + Global.gUOrderHInfo.nSaleAmount + Global.gUOrderHInfo.nTaxAmount + Global.gUOrderHInfo.nHandlingFee + Global.gUOrderHInfo.nHandlingFeeTaxAmount < 0)
						{
							((TextView) mRootView.findViewById(R.id.txtTotalAllValue)).setTextColor(Color.RED);
						}
						else
						{
							((TextView) mRootView.findViewById(R.id.txtTotalAllValue)).setTextColor(Color.BLACK);
						}
					}
					((TextView) mRootView.findViewById(R.id.lblTotalCollection)).setTextColor(mContext.getResources().getColor(R.color.black_color));
					((TextView) mRootView.findViewById(R.id.lblCurrencySymbol)).setTextColor(mContext.getResources().getColor(R.color.black_color));
					((EditText) mRootView.findViewById(R.id.editTotalCollection)).setTextColor(mContext.getResources().getColor(R.color.black_color));
					((TextView) mRootView.findViewById(R.id.lblFullPaymentBy)).setTextColor(mContext.getResources().getColor(R.color.black_color));
					((TextView) mRootView.findViewById(R.id.lblTerms)).setTextColor(mContext.getResources().getColor(R.color.black_color));
					if(Utils.getIntSetting(mContext, Global.PREF_EWAY_CARD_PROC) == 0)
					{
						mRootView.findViewById(R.id.chkCard).setEnabled(false);
						((CheckBox)mRootView.findViewById(R.id.chkCard)).setTextColor(Color.GRAY);
					}
					else
					{
						mRootView.findViewById(R.id.chkCard).setEnabled(true);
						((CheckBox)mRootView.findViewById(R.id.chkCard)).setTextColor(mContext.getResources().getColor(R.color.black_color));
					}

					mRootView.findViewById(R.id.chkCash).setEnabled(true);
					mRootView.findViewById(R.id.chkCheque).setEnabled(true);
					if(Global.gCurCustomerInfo.strPayType.equals("B"))
					{
						mRootView.findViewById(R.id.editTotalCollection).setEnabled(false);
					}
					else
					{
						mRootView.findViewById(R.id.editTotalCollection).setEnabled(true);
					}

					((CheckBox)mRootView.findViewById(R.id.chkCash)).setTextColor(mContext.getResources().getColor(R.color.black_color));
					((CheckBox)mRootView.findViewById(R.id.chkCheque)).setTextColor(mContext.getResources().getColor(R.color.black_color));

					mRootView.findViewById(R.id.btnDetail).setVisibility(View.VISIBLE);
				}
				else
				{
					((TextView)mRootView.findViewById(R.id.lblTotalValue)).setTextColor(mContext.getResources().getColor(R.color.gray_color));
					((TextView)mRootView.findViewById(R.id.lblCurrencySymbol)).setTextColor(mContext.getResources().getColor(R.color.gray_color));
					((TextView)mRootView.findViewById(R.id.txtTotalValue)).setTextColor(mContext.getResources().getColor(R.color.gray_color));
					((TextView) mRootView.findViewById(R.id.txtOutstandingValue)).setTextColor(mContext.getResources().getColor(R.color.gray_color));
					((TextView) mRootView.findViewById(R.id.txtTotalAllValue)).setTextColor(mContext.getResources().getColor(R.color.gray_color));

					((TextView) mRootView.findViewById(R.id.lblTotalCollection)).setTextColor(mContext.getResources().getColor(R.color.gray_color));
					((TextView) mRootView.findViewById(R.id.lblCurrencySymbol)).setTextColor(mContext.getResources().getColor(R.color.gray_color));
					((EditText) mRootView.findViewById(R.id.editTotalCollection)).setTextColor(mContext.getResources().getColor(R.color.gray_color));
					((TextView) mRootView.findViewById(R.id.lblFullPaymentBy)).setTextColor(mContext.getResources().getColor(R.color.gray_color));
					((TextView) mRootView.findViewById(R.id.lblTerms)).setTextColor(mContext.getResources().getColor(R.color.gray_color));
					mRootView.findViewById(R.id.chkCard).setEnabled(false);
					mRootView.findViewById(R.id.chkCash).setEnabled(false);
					mRootView.findViewById(R.id.chkCheque).setEnabled(false);
					mRootView.findViewById(R.id.editTotalCollection).setEnabled(false);
					((CheckBox)mRootView.findViewById(R.id.chkCard)).setTextColor(mContext.getResources().getColor(R.color.gray_color));
					((CheckBox)mRootView.findViewById(R.id.chkCash)).setTextColor(mContext.getResources().getColor(R.color.gray_color));
					((CheckBox)mRootView.findViewById(R.id.chkCheque)).setTextColor(mContext.getResources().getColor(R.color.gray_color));

					mRootView.findViewById(R.id.btnDetail).setVisibility(View.INVISIBLE);
				}
			}

			//showTimes();

			if(Global.gCurCustomerInfo != null && Global.gCurCustomerInfo.presoldOrHInfo != null && Global.gCurCustomerInfo.presoldOrHInfo.size() == 1 &&
					Global.gCurCustomerInfo.presoldOrHInfo.get(0).nVisited == 0 )
			{
				if(!Utils.getStringSetting(mContext, Global.PREF_UNLESHED).equals("1"))
				{
					((EditText) mRootView.findViewById(R.id.editReceiptNumber)).setText(Global.gUOrderHInfo.strPOReference);
				}
				((EditText) mRootView.findViewById(R.id.editReceiverName)).setText(Global.gUOrderHInfo.strOrderName);
			}
		}

		if (Global.gCurCustomerInfo.strPayType.trim().equals("1")) {
			mRootView.findViewById(R.id.chkCheque).setEnabled(false);
			((CheckBox)mRootView.findViewById(R.id.chkCheque)).setTextColor(Color.GRAY);
		}

		if(Global.gCurHeaderInfo == null )
		{
			Global.gCurHeaderInfo = new ARHeaderInfo();
			Global.gCurHeaderInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
			Global.gCurHeaderInfo.nChainNo = Global.gCurCustomerInfo.nChainNo;

			if(!Global.gCurCustomerInfo.strPayType.equals("$") &&
					!Global.gCurCustomerInfo.strPayType.equals("1") &&
					!Global.gCurCustomerInfo.strPayType.equals("2") &&
                    !Global.gCurCustomerInfo.strPayType.equals("3"))
			{
				Global.gCurHeaderInfo.nProcessStatus = 2;
			}
			else
			{
				Global.gCurHeaderInfo.nProcessStatus = 1;
			}

			Global.gCurHeaderInfo.nTrxnAmount = Math.round(getRoundedValue(Global.gUOrderHInfo.nSaleAmount + Global.gUOrderHInfo.nTaxAmount + Global.gUOrderHInfo.nHandlingFee + Global.gUOrderHInfo.nHandlingFeeTaxAmount) * Global.MULTIPLE_VALUE);
			if( Global.gCurHeaderInfo.nTrxnAmount < 0 )
				Global.gCurHeaderInfo.strARTrxnType = "CRN";
			else
			{
				if(Global.gUOrderHInfo.futureOrderMethod == 0)
				{
					Global.gCurHeaderInfo.strARTrxnType = "INV";
				}
				else
				{
					Global.gCurHeaderInfo.strARTrxnType = "ORD";
				}
			}

			Global.gCurHeaderInfo.strInvNo = "0";
			Global.gCurHeaderInfo.nIdx = -1;
			SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
			Global.gCurHeaderInfo.strInvDate = format.format(new Date(System.currentTimeMillis()));

		}

		if(Global.gCurCustomerInfo.strPayType.equals("$") ||
				Global.gCurCustomerInfo.strPayType.equals("1") ||
				Global.gCurCustomerInfo.strPayType.equals("2") ||
                Global.gCurCustomerInfo.strPayType.equals("3") ||
                Global.gCurCustomerInfo.strPayType.equals("B") ||
				Global.gCurCustomerInfo.strPayType.equals("4") ||
				Global.gCurCustomerInfo.strPayType.equals("5") ||
				Global.gCurCustomerInfo.strPayType.equals("C"))
		{
			//mRootView.findViewById(R.id.btnDetail).setEnabled(false);
			//mRootView.findViewById(R.id.btnDetail).setOnClickListener(null);
			((EditText)mRootView.findViewById(R.id.editTotalCollection)).setEnabled(false);
		}

		// check disallow account payment
		String strDisallowAccountPayment = Utils.getStringSetting(mContext, Global.PREF_DISALLOW_ACCOUNT_PAY);
		if (strDisallowAccountPayment.equals("1")) {
			String strPayType = Global.gCurCustomerInfo.strPayType;
			if (!strPayType.equals("1") && !strPayType.equals("2") && !strPayType.equals("C")) {
				mRootView.findViewById(R.id.btnDetail).setVisibility(View.INVISIBLE);
				mRootView.findViewById(R.id.editTotalCollection).setEnabled(false);
			}
		}

		if (bShouldDisableCashCheque) {
			disableCashCheque();
		}
	}

	@Override
	public void refreshLanguage() {

		String strTempFmt = Utils.getStringSetting(mContext, Global.PREF_TEMPFMT);
		String unit = "C°";
		if(strTempFmt.endsWith("F"))
		{
			unit = "F°";
		}
		((TextView)mRootView.findViewById(R.id.lblDeliveryTemperature)).setText(Str.DeliveryTemperature[Global.LANG]);
		((TextView)mRootView.findViewById(R.id.lblTempUnit)).setText(unit);

		((TextView)mRootView.findViewById(R.id.lblCurrencySymbol)).setText(Global.gCurrencySymbol);
		((TextView) mRootView.findViewById(R.id.lblTitle)).setText(Str.Summary[Global.LANG].toUpperCase());
		((TextView) mRootView.findViewById(R.id.lblTotalQty)).setText(Str.TotalQuantity[Global.LANG]);
		((TextView) mRootView.findViewById(R.id.lblTotalValue)).setText(Str.TotalValue[Global.LANG]);
		((TextView) mRootView.findViewById(R.id.lblTotalCollection)).setText(Str.TotalCollection[Global.LANG]);
		String strDueDate = Utils.getCalculatedInvoiceDueDate(mContext, Global.gCurCustomerInfo, 1);
		if(!TextUtils.isEmpty(strDueDate))
		{
			((TextView) mRootView.findViewById(R.id.lblFullPaymentBy)).setText(Str.Due[Global.LANG]);
			((TextView) mRootView.findViewById(R.id.lblTerms)).setText(strDueDate);
		}
		else
		{
			((TextView) mRootView.findViewById(R.id.lblFullPaymentBy)).setText(Str.FullPaymentBy[Global.LANG]);
			((TextView) mRootView.findViewById(R.id.lblTerms)).setText(Global.gCurCustomerInfo.strTerms);
		}

		//((TextView) mRootView.findViewById(R.id.lblDesiredDelDate)).setText(Str.Desired_Del_Date[Global.LANG]);
		if(mIsPoRefNumberRequired)
		{
			((TextView) mRootView.findViewById(R.id.lblReceiptNumber)).setText(Str.ReceiptNumber[Global.LANG] + " *");
			((TextView) mRootView.findViewById(R.id.lblReceiptNumber)).setTextColor(mContext.getResources().getColor(R.color.green_color));
			if(!TextUtils.isEmpty(mStrPoRefMask))
			{
				mStrReceiptText = mStrPoRefMask;
				((EditText)mRootView.findViewById(R.id.editReceiptNumber)).setText(mStrPoRefMask);

/*				final int nonEditableLength = mStrPoRefMask.length();
				InputFilter filter = (source, start, end, dest, dstart, dend) -> {
					if(!source.toString().startsWith(mStrPoRefMask))
						return dest;
					else
						return null;
//					if (dstart < nonEditableLength) {
//						return dest.subSequence(dstart, dend);
//					}
//					Log.d("Wang Log", source.toString());
//					Log.d("Wang Log", dest.toString());
//					return null;
				};*/

				//((EditText)mRootView.findViewById(R.id.editReceiptNumber)).setFilters(new InputFilter[] {filter});
				((EditText)mRootView.findViewById(R.id.editReceiptNumber)).addTextChangedListener(new TextWatcher() {
					@Override
					public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

					}

					@Override
					public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

					}

					@Override
					public void afterTextChanged(Editable editable) {
						String strText = editable.toString();
						if(strText.equals(mStrReceiptText))
						{
							return;
						}
						if(!strText.startsWith(mStrPoRefMask))
						{
							((EditText)mRootView.findViewById(R.id.editReceiptNumber)).setText(mStrReceiptText);
						}
						else {
							mStrReceiptText = strText;
						}
					}
				});
			}
		}
		else
		{
			((TextView) mRootView.findViewById(R.id.lblReceiptNumber)).setText(Str.ReceiptNumber[Global.LANG]);
			((TextView) mRootView.findViewById(R.id.lblReceiptNumber)).setTextColor(mContext.getResources().getColor(R.color.black_color));
		}


		((TextView) mRootView.findViewById(R.id.lblStoreNumber)).setText(Str.StoreNumber[Global.LANG]);

		((CheckBox)mRootView.findViewById(R.id.chkCard)).setText(Str.Card[Global.LANG]);
		((CheckBox)mRootView.findViewById(R.id.chkCash)).setText(Str.Cash[Global.LANG]);
		((CheckBox)mRootView.findViewById(R.id.chkCheque)).setText(Str.Cheque[Global.LANG]);
		if(TextUtils.isEmpty(Global.gCurCustomerInfo.strStoreNo) || Global.gCurCustomerInfo.strStoreNo.equals("0"))
		{
			((TextView) mRootView.findViewById(R.id.txtStoreNumber)).setText("");
		}
		else
		{
			((TextView) mRootView.findViewById(R.id.txtStoreNumber)).setText(Global.gCurCustomerInfo.strStoreNo);
		}

		if(mIsSignature)
		{
			((TextView) mRootView.findViewById(R.id.lblReceiverName)).setText(Str.ReceiverName[Global.LANG] + " *");
			((Button) mRootView.findViewById(R.id.btnSignature)).setText(Str.Signature[Global.LANG] + " *");
			((TextView) mRootView.findViewById(R.id.lblReceiverName)).setTextColor(mContext.getResources().getColor(R.color.green_color));
			((Button) mRootView.findViewById(R.id.btnSignature)).setTextColor(mContext.getResources().getColor(R.color.green_color));
		}
		else
		{
			((TextView) mRootView.findViewById(R.id.lblReceiverName)).setText(Str.ReceiverName[Global.LANG]);
			((Button) mRootView.findViewById(R.id.btnSignature)).setText(Str.Signature[Global.LANG]);
			((TextView) mRootView.findViewById(R.id.lblReceiverName)).setTextColor(mContext.getResources().getColor(R.color.black_color));
			((Button) mRootView.findViewById(R.id.btnSignature)).setTextColor(mContext.getResources().getColor(R.color.black_color));
		}
		
		((Button) mRootView.findViewById(R.id.btnConfirm)).setText(Str.Confirm[Global.LANG]);
		refreshPage();

		if( collectionValue == 0.0f && Global.gCurCustomerInfo.nShowPrice != 2)
		{
			setResetCollectionValue();
		}
		
		if(Utils.getStringSetting(mContext, Global.PREF_SUMMARY_DETAIL).equals("1"))
		{
			((TextView)mRootView.findViewById(R.id.lblCase)).setText(Str.Case[Global.LANG]);
			((TextView)mRootView.findViewById(R.id.lblUnitPrice)).setText(Str.UnitPrice[Global.LANG]);
			((TextView)mRootView.findViewById(R.id.lblDiscAmount)).setText(Str.DiscAmt[Global.LANG]);
			((TextView)mRootView.findViewById(R.id.lblNetAmount)).setText(Str.NetAmt[Global.LANG]);
			((TextView)mRootView.findViewById(R.id.lblSubTotal)).setText(Str.SubTotal[Global.LANG]);
			((TextView)mRootView.findViewById(R.id.lblGST)).setText(Str.GST[Global.LANG]);
			((TextView)mRootView.findViewById(R.id.lblInvoiceTotal)).setText(Str.InvoiceTotalincGST[Global.LANG]);
		}
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
		} else if (strAction.equals(Global.ACTION_CAPTURE_SIGNATURE) == true) {
			if (Global.gFragIdx == Global.FRAG_DELIVERY_SUMMARY && Global.gUOrderHInfo != null) {
				Global.gUOrderHInfo.strSignatureFilePath = intent.getStringExtra(Global.EXTRA_SIGNATURE_PATH);
				Utils.printDebug(mContext, Str.Saved_successfully[Global.LANG]);
			}
		}
	}

	/*private void showTimes() {
		Date date = new Date(mDate);
		((TextView) mRootView.findViewById(R.id.txtDate)).setText(new SimpleDateFormat("EEE dd/MM/yyyy").format(date));
	}*/

	private int getSalesQuantity(String strItemNo) {

		String strCurDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(new Date());
		final String strDelDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(mDate);

		Date curDate = Utils.getDateFromString(strCurDate);
		Date delDate = Utils.getDateFromString(strDelDate);

		if(Utils.getIntSetting(mContext, Global.PREF_DELFUTDTE) == 0 || curDate.compareTo(delDate) == 0)
		{
			int nSales = 0;
			for (UOrderDInfo info : Global.gUOrderHInfo.deliveryList) {
				if (info.strItemNo.equals(strItemNo) == true) {
					nSales += info.nSales;
				}
			}
			return nSales;
		}
		else
		{
			return 0;
		}
	}

	private List<LoadBlotInfo> getSalesLoadBlotArray(String strItemNo) {
		for (UOrderDInfo info : Global.gUOrderHInfo.deliveryList) {
			if (info.strItemNo.equals(strItemNo) == true) {
				return info.uLoadBlotInfo;
			}
		}
		return null;
	}

	private List<LoadBlotInfo> getReturnsLoadBlotArray(String strItemNo) {
		for (UOrderDInfo info : Global.gUOrderHInfo.pickupList) {
			if (info.strItemNo.equals(strItemNo) == true) {
				return info.uLoadBlotInfo;
			}
		}
		return null;
	}

	private double getSalesQuantityD(String strItemNo) {
		String strCurDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(new Date());
		final String strDelDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(mDate);

		Date curDate = Utils.getDateFromString(strCurDate);
		Date delDate = Utils.getDateFromString(strDelDate);

		if(Utils.getIntSetting(mContext, Global.PREF_DELFUTDTE) == 0 || curDate.compareTo(delDate) == 0)
		{
			double dSales = 0;
			for (UOrderDInfo info : Global.gUOrderHInfo.deliveryList) {
				if (info.strItemNo.equals(strItemNo) == true) {
					dSales += info.dSales;
				}
			}
			return dSales;
		}
		else
		{
			return 0;
		}

	}

	private int getBuyBacksValues(String strItemNo) {
		String strCurDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(new Date());
		final String strDelDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(mDate);

		Date curDate = Utils.getDateFromString(strCurDate);
		Date delDate = Utils.getDateFromString(strDelDate);

		if(Utils.getIntSetting(mContext, Global.PREF_DELFUTDTE) == 0 || curDate.compareTo(delDate) == 0)
		{
			int nDumps = 0;
			for (UOrderDInfo info : Global.gUOrderHInfo.pickupList) {
				if (info.strItemNo.equals(strItemNo) == true && info.nTrxnType == 2) {
					nDumps += info.nDumps;
				}
			}
			return nDumps;
		}
		else
		{
			return 0;
		}


	}

	private double getBuyBacksValuesD(String strItemNo) {
		String strCurDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(new Date());
		final String strDelDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(mDate);

		Date curDate = Utils.getDateFromString(strCurDate);
		Date delDate = Utils.getDateFromString(strDelDate);

		if(Utils.getIntSetting(mContext, Global.PREF_DELFUTDTE) == 0 || curDate.compareTo(delDate) == 0)
		{
			double dDumps = 0;
			for (UOrderDInfo info : Global.gUOrderHInfo.pickupList) {
				if (info.strItemNo.equals(strItemNo) == true && info.nTrxnType == 2) {
					dDumps += info.dDumps;
				}
			}
			return dDumps;
		}
		else
		{
			return 0;
		}

	}

	private boolean isPeddle(String strItemNo)
	{
		for (UOrderDInfo info : Global.gUOrderHInfo.deliveryList) {
			if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
			{
				if (info.strItemNo.equals(strItemNo) == true) {
					if( info.dPlanQty > 0 )
						return false;
					else
						return true;
				}
			}
			else
			{
				if (info.strItemNo.equals(strItemNo) == true) {
					if( info.nPlanQty > 0 )
						return false;
					else
						return true;
				}
			}
		}
		return true;
	}

	private int getDumpsQuantity(String strItemNo) {
		String strCurDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(new Date());
		final String strDelDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(mDate);

		Date curDate = Utils.getDateFromString(strCurDate);
		Date delDate = Utils.getDateFromString(strDelDate);

		if(Utils.getIntSetting(mContext, Global.PREF_DELFUTDTE) == 0 || curDate.compareTo(delDate) == 0)
		{
			int nDumps = 0;
			for (UOrderDInfo info : Global.gUOrderHInfo.pickupList) {
				if (info.strItemNo.equals(strItemNo) == true && info.nTrxnType == 1) {
					nDumps += info.nDumps;
				}
			}
			return nDumps;
		}
		else
		{
			return 0;
		}

	}

	private double getDumpsQuantityD(String strItemNo) {
		String strCurDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(new Date());
		final String strDelDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(mDate);

		Date curDate = Utils.getDateFromString(strCurDate);
		Date delDate = Utils.getDateFromString(strDelDate);

		if(Utils.getIntSetting(mContext, Global.PREF_DELFUTDTE) == 0 || curDate.compareTo(delDate) == 0)
		{
			double dDumps = 0;
			for (UOrderDInfo info : Global.gUOrderHInfo.pickupList) {
				if (info.strItemNo.equals(strItemNo) == true && info.nTrxnType == 1) {
					dDumps += info.dDumps;
				}
			}
			return dDumps;
		}
		else
		{
			return 0;
		}
	}

	private boolean bDelayPrintNormal = false;
	
	private void doDelayPrintTask() {
		
		PresoldOrHInfo presoldOrHInfo = null;
		if( Global.gCurCustomerInfo.presoldOrHInfo.size() == 1 )
		{
			presoldOrHInfo = Global.gCurCustomerInfo.presoldOrHInfo.get(0);
			if(TextUtils.isEmpty(Utils.getStringSetting(mContext, Global.PREF_INVOICENUM)))
			{
				presoldOrHInfo.strOrderNo = getOrderNo();
			}
			else
			{
				presoldOrHInfo.strOrderNo = Utils.getOrderNo(String.valueOf(presoldOrHInfo.nQty), Global.PREF_INVOICENUM);
			}
		}
		else
		{
			presoldOrHInfo = new PresoldOrHInfo();
			presoldOrHInfo.nChainNo = Global.gCurCustomerInfo.nChainNo;
			presoldOrHInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
			presoldOrHInfo.nCustIdx = Global.gCurCustomerInfo.nIdx;
			presoldOrHInfo.strCredRef = "";
			presoldOrHInfo.strType = "4";
			presoldOrHInfo.nDayNo = mTrxnDate.getDay();

			if(presoldOrHInfo.nDayNo == Time.SUNDAY)
			{
				presoldOrHInfo.nDayNo = 7;
			}
			if(presoldOrHInfo.nDayNo == 0) presoldOrHInfo.nDayNo = 7;

			int nPdfSequenceNo = Utils.getIntSetting(mContext, String.format(Locale.ENGLISH, Global.PREF_PDF_SEQUENCE_NO, Global.gTripNumber)) + 1;
			presoldOrHInfo.strOrderNo = getOrderNo();
		}
		
		String strDeliveryDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(new Date(mDate));
		presoldOrHInfo.strDetailFile = strDeliveryDate + String.valueOf(System.currentTimeMillis() / 1000) + presoldOrHInfo.strOrderNo;
		presoldOrHInfo.strInstrs = "";
		if(Utils.getStringSetting(mContext, Global.PREF_UNLESHED).equals("1"))
		{
			presoldOrHInfo.strPORef = "ex Vehicle";
		}
		else
		{
			presoldOrHInfo.strPORef = ((EditText) mRootView.findViewById(R.id.editReceiptNumber)).getText().toString();
		}
		presoldOrHInfo.nVisited = 0;
		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
	
		if( Global.gCurCustomerInfo.presoldOrHInfo.size() == 1 )
		{
			dbAdapter.updatePresoldOrHInfo(presoldOrHInfo);
		}
		else
		{
			dbAdapter.insertPresoldOrHInfo(presoldOrHInfo);
		}
			
		for (UOrderDInfo info : Global.gUOrderHInfo.deliveryList) {
			PresoldOrDInfo presoldOrDInfo = new PresoldOrDInfo();
			if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
			{
				presoldOrDInfo.dOrderQty = info.dQuantity;
			}
			else
			{
				presoldOrDInfo.nOrderQty = info.nQuantity;
			}
			if(presoldOrDInfo.nOrderQty == 0 && presoldOrDInfo.dOrderQty == 0)
			{
				continue;
			}
			presoldOrDInfo.strTrxnType = "4";

			presoldOrDInfo.strDetailFile = presoldOrHInfo.strDetailFile;
			presoldOrDInfo.strItemNo = info.strItemNo;
			presoldOrDInfo.strPickLocationCode = info.strPickLocationCode;
			presoldOrDInfo.nQtyOption  = 0;
			dbAdapter.insertPresoldOrDInfo(presoldOrDInfo);
		}	
		dbAdapter.close();

		Global.gCurHeaderInfo = null;
		androidx.fragment.app.Fragment fragment = mContext.getSupportFragmentManager().findFragmentById(R.id.frameContainer);

		while(!(fragment instanceof InstoreFragment))
		{
			if( fragment instanceof  MakeDeliveryFragment)
			{
				((MakeDeliveryFragment)fragment).setModified(false);
			}
			mContext.onBackPressed();
			fragment = mContext.getSupportFragmentManager().findFragmentById(R.id.frameContainer);
		}
		m_bConfirmClicked = false;

	}
	
	private void doConfirm() {

		//Check Delivery Temperature Valid
		String strTempSetting = Utils.getStringSetting(mContext, Global.PREF_TEMPREQ);
		if( !TextUtils.isEmpty(strTempSetting) && strTempSetting.equals("2"))
		{
			EditText deliverTemp = mRootView.findViewById(R.id.editDeliverTemp);
			if(TextUtils.isEmpty(deliverTemp.getText()))
			{
				final MessageDialog msgDialog = new MessageDialog(mContext);
				msgDialog.setCancelable(false);
				msgDialog.mStrMessage = Str.Delivery_Temp_Required[Global.LANG];
				msgDialog.mStrRight = Str.Ok[Global.LANG];
				msgDialog.show();
				m_bConfirmClicked = false;

				return;
			}
		}

		long lCurTime = System.currentTimeMillis();
		String strCurDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(lCurTime);
		final String strDelDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(mDate);

		Date curDate = Utils.getDateFromString(strCurDate);
		Date delDate = Utils.getDateFromString(strDelDate);

		if(!bReverseMode)
		{
			//First Check delay Print
			if(Global.gCurCustomerInfo.nDelayedPrint > 0 && !bDelayPrintNormal)
			{
				if( Global.gUOrderHInfo.pickupList == null || Global.gUOrderHInfo.pickupList.size() == 0 )
				{
					try {
						if(mContext.isFinishing() || mContext.isDestroyed()) return;
						final MessageDialog msgDialog = new MessageDialog(mContext);
						msgDialog.setCancelable(false);
						msgDialog.mStrTitle = Str.DelayPrintOption[Global.LANG];
						msgDialog.mStrMessage = Str.Do_you_wish_to_delay_printing_or_print_now[Global.LANG];
						msgDialog.mStrRight = Str.Invoice[Global.LANG];
						msgDialog.mStrLeft = Str.Delay[Global.LANG];
						msgDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
							@Override
							public void onDismiss(DialogInterface dialog) {
								if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT){
									doDelayPrintTask();
								}
								else{
									bDelayPrintNormal = true;
									doConfirm();

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
			}

			if(mTrxnDate == null)
			{
				mTrxnDate = new Date();
			}
			final String strReference = "";

			if(!bConsignment)
			{
				if( curDate.compareTo(delDate) < 0 && m_bFutureOrder == false)
				{
					try {
						if(mContext.isFinishing() || mContext.isDestroyed()) return;
						final MessageDialog msgDialog = new MessageDialog(mContext);
						msgDialog.setCancelable(false);
						msgDialog.mStrTitle = Str.OrderConfirmation[Global.LANG];
						if(Global.gUOrderHInfo.futureOrderMethod == 1)
						{
							msgDialog.mStrMessage = Str.Please_confirm_future_warehouse_order[Global.LANG];
						}
						else if(Global.gUOrderHInfo.futureOrderMethod == 2)
						{
							msgDialog.mStrMessage = String.format(Locale.ENGLISH, Str.Please_confirm_future_order[Global.LANG], new SimpleDateFormat(Utils.getDateFormat(mContext, "dd/MM/yyyy")).format(mDate));
						}
						msgDialog.mStrRight = Str.Yes[Global.LANG];
						msgDialog.mStrLeft = Str.No[Global.LANG];
						msgDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
							@Override
							public void onDismiss(DialogInterface dialog) {
								if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT){
									m_bFutureOrder = true;
									doConfirm();
								}
								else
								{
									m_bConfirmClicked = false;
								}
							}
						});
						msgDialog.show();
					}catch (Exception e)
					{
						e.printStackTrace();
					}
					m_bClicked = false;
					m_bConfirmClicked = false;
					return;
				}
			}

			if( mIsSignature && m_bFutureOrder == false)
			{
				if( ((TextView)mRootView.findViewById(R.id.editReceiverName)).getText().toString().trim().length() == 0 ||
						Global.gUOrderHInfo.strSignatureFilePath.trim().length() == 0)
				{
					try {
						if(mContext.isFinishing() || mContext.isDestroyed()) return;
						MessageDialog msgDialog = new MessageDialog(mContext);
						msgDialog.setCancelable(false);
						msgDialog.mStrTitle = Global.gCurCustomerInfo.strName;
						msgDialog.mStrMessage = Str.SignatureAndReceiverMandatory[Global.LANG];
						msgDialog.mStrRight = Str.Return[Global.LANG];
						msgDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
							@Override
							public void onDismiss(DialogInterface dialog) {
								m_bConfirmClicked = false;
							}
						});
						msgDialog.show();
					}catch (Exception e)
					{
						e.printStackTrace();
					}
					m_bClicked = false;
					m_bConfirmClicked = false;
					return;
				}
			}

			if(mIsPoRefNumberRequired && m_bFutureOrder == false)
			{
				if( ((TextView)mRootView.findViewById(R.id.editReceiptNumber)).getText().toString().trim().length() == 0 )
				{
					try {
						if(mContext.isFinishing() || mContext.isDestroyed()) return;
						MessageDialog msgDialog = new MessageDialog(mContext);
						msgDialog.setCancelable(false);
						msgDialog.mStrTitle = Global.gCurCustomerInfo.strName;
						msgDialog.mStrMessage = Str.PoRefMandatory[Global.LANG];
						msgDialog.mStrRight = Str.Return[Global.LANG];
						msgDialog.setOnDismissListener(dialog -> m_bConfirmClicked = false);
						msgDialog.show();
					}catch (Exception e)
					{
						e.printStackTrace();
					}
					m_bClicked = false;
					m_bConfirmClicked = false;
					return;
				}
				else if(mIsPoRefNumberRequired && ((TextView)mRootView.findViewById(R.id.editReceiptNumber)).getText().toString().trim().length() == mStrPoRefMask.length())
				{
					try {
						if(mContext.isFinishing() || mContext.isDestroyed()) return;
						MessageDialog msgDialog = new MessageDialog(mContext);
						msgDialog.setCancelable(false);
						msgDialog.mStrTitle = Global.gCurCustomerInfo.strName;
						msgDialog.mStrMessage = Str.PoNumberNotUpdated[Global.LANG];
						msgDialog.mStrLeft = Str.No[Global.LANG];
						msgDialog.mStrRight = Str.Yes[Global.LANG];
						msgDialog.isCenterMessage = true;
						msgDialog.setOnDismissListener(dialogInterface -> {
							if(msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT)
							{
								String strDelDate1 = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(mContext, new Date(mDate)));
								doContinueConfirmProcessing(strDelDate1, strReference);
							}
						});
						msgDialog.show();
					}catch (Exception e)
					{
						e.printStackTrace();
					}
					m_bClicked = false;
					m_bConfirmClicked = false;
					return;
				}
			}

			String strDelDate1 = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(mContext, new Date(mDate)));
			doContinueConfirmProcessing(strDelDate1, strReference);
		}
		else
		{
			Global.gCurVoidCustomerDeliveryList.add(Global.gReverseTransactionID);

			if(mPayMethod == 3 && (Global.gCurCustomerInfo.strPayType.equals("C") ||
					Global.gCurCustomerInfo.strPayType.equals("3") ||
					Global.gCurCustomerInfo.strPayType.equals("4") ||
					Global.gCurCustomerInfo.strPayType.equals("5")))
			{
				if(mPaymentCollectInfo.strKind.equals("eway"))
				{
					//Refund eWay
					String apiKey, password;
					if(Utils.getIntSetting(mContext, Global.PREF_EWAY_CARD_PROC) == 1)
					{
						RapidAPI.PublicAPIKey = Utils.getStringSetting(mContext, Global.PREF_EWAY_API);
						RapidAPI.RapidEndpoint = Utils.getStringSetting(mContext, Global.PREF_EWAY_SYSTEM);
						apiKey = Utils.getStringSetting(mContext, Global.PREF_EWAY_API_KEY);
						password = Utils.getStringSetting(mContext, Global.PREF_EWAY_PASSWORD);
					}
					else
					{
						RapidAPI.PublicAPIKey = "epk-1C040E3E-E7E3-4A91-BF85-93D96D27AD5C";
						RapidAPI.RapidEndpoint = "https://api.sandbox.ewaypayments.com/";
						apiKey = "F9802CaTan7PEJXzII++VGKeriPWTCxG1DnEnv8rgbDtx+CMZfl1A2itKry4xSi+vhlVR4";
						password = "zE1tlOcZ";
					}

					try {
						Utils.getEWayApiClient(apiKey, password).refund(mPaymentCollectInfo.strOther).enqueue(new Callback<EWayRefundResponse>() {
							@Override
							public void onResponse(Response<EWayRefundResponse> response, Retrofit retrofit) {
								String strDelDate1 = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(mContext, new Date(mDate)));
								processConfirm(strDelDate1, "", true);
							}

							@Override
							public void onFailure(Throwable t) {

							}
						});
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				else if(mPaymentCollectInfo.strKind.equals("mint_not_present"))
				{
					String mintMode = Utils.getStringSetting(mContext, Global.PREF_MINTMODE);
					String username = "", password = "";
					if(mintMode.equals("D"))
					{
						username = Utils.getStringSetting(mContext, Global.PREF_MINTCNPUSER);
						password = Utils.getStringSetting(mContext, Global.PREF_MINTCNPPWD);
					}
					else if(mintMode.equals("S") || mintMode.equals("P"))
					{
						username = Utils.getStringSetting(mContext, Global.PREF_COMPANY_NAME) + Global.gTripNumber;
						password = Global.gTripNumber;
					}
					MintPaymentDialog.RefundRequest refundRequest = new MintPaymentDialog.RefundRequest();
					String values[] = mPaymentCollectInfo.strOther.split("\\|\\|");
					refundRequest.transaction_id = values[0];
					refundRequest.reference = values[1];
					refundRequest.amount = Utils.parseIntFromString(values[2]);
					MintPaymentDialog.getApiClient(mContext, username, password).refunds(refundRequest).enqueue(new Callback<MintPaymentDialog.RefundResponse>() {
						@Override
						public void onResponse(Response<MintPaymentDialog.RefundResponse> response, Retrofit retrofit) {
							String strDelDate1 = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(mContext, new Date(mDate)));
							processConfirm(strDelDate1, "", true);
						}

						@Override
						public void onFailure(Throwable t) {

						}
					});
				}
				else if(mPaymentCollectInfo.strKind.equals("mint"))
				{
					MTGAppSwitch.setIsProduction(true);
					String apiKey = Utils.getStringSetting(mContext, Global.PREF_MINTAPI);
					String apiSecret = Utils.getStringSetting(mContext, Global.PREF_MINTSECRET);
					if(!TextUtils.isEmpty(apiKey) && !TextUtils.isEmpty(apiSecret))
					{
						//MTGAppSwitch.initialise(apiKey, apiSecret, "mintmpos", MTGAppSwitch.CountryCode.MTGCountryCodeAustralia);
						MTGAppSwitch.initialise(apiKey, apiSecret, "mintegrate", MTGAppSwitch.CountryCode.MTGCountryCodeAustralia);
					}

					MTGAppSwitchRequest mtgAppSwitchRequest = new MTGAppSwitchRequest();

					//Toast.makeText(mContext, "Voiding Transaction is for \n" + mPaymentCollectInfo.strOther, Toast.LENGTH_LONG).show();

					String[] params = mPaymentCollectInfo.strOther.split("\\|\\|");

					mtgAppSwitchRequest.setTransactionRequestId(params[0]);

					mtgAppSwitchRequest.setExternalReference(params[2]);
					mtgAppSwitchRequest.setNotes(String.valueOf(Global.gCurCustomerInfo.nCustNo));
					mtgAppSwitchRequest.setAmount(params[1]);
					mtgAppSwitchRequest.setMuid(Utils.getStringSetting(mContext, Global.PREF_MINTUSER));
					mtgAppSwitchRequest.setRememberLastLogin(true);

					try {
						MTGAppSwitch.submitRefundWithExternalReference(mtgAppSwitchRequest, mContext);
					} catch (Exception e) {
						e.printStackTrace();
						if(!mContext.isFinishing() && !mContext.isDestroyed())
						{
							try {
								Toast.makeText(mContext, "Voiding Transaction is for \n" + mPaymentCollectInfo.strOther + "\n" + e.getMessage(), Toast.LENGTH_LONG).show();
							}catch (Exception e1)
							{
								e.printStackTrace();
							}

						}
					}
				}
				else if(mPaymentCollectInfo.strKind.equals("other"))
				{
					String[] params = mPaymentCollectInfo.strKind.split("\\|\\|");
					Global.gUARPaymentCardInfo.strInvoiceNo = params[0];
					Global.gUARPaymentCardInfo.strARNotes = params[1];
					Global.gUARPaymentCardInfo.nTrxnAmount = Utils.getFloatFromString(params[2]);
					performCreatePdfTask();
				}
			}
			else
			{
				String strDelDate1 = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(mContext, new Date(mDate)));
				processConfirm(strDelDate1, "", true);
			}
		}

	}

	private void doContinueConfirmProcessing(String strDelDate, String strReference)
	{
		String strRealAmount = "";
		float fCollectedAmount = 0f;
		try
		{
			strRealAmount = ((TextView)mRootView.findViewById(R.id.editTotalCollection)).getText().toString();
			fCollectedAmount = Utils.parseFloatFromString(strRealAmount);
		}catch(Exception e)
		{
			e.printStackTrace();

		}

		if(!TextUtils.isEmpty(Global.gCurCustomerInfo.strPayType.trim()) && fCollectedAmount != 0f && !Utils.getStringSetting(mContext, Global.PREF_HIDE_SUMMARY_PAYMENT).equals("1"))
		{
			if((Global.gCurCustomerInfo.strPayType.trim().equals("1") || Global.gCurCustomerInfo.strPayType.trim().equals("2") || Global.gCurCustomerInfo.strPayType.trim().equals("$")) && m_bFutureOrder == false && !Global.gProcessedPayment)
			{
				if( mPayMethod != 1 && mPayMethod != 2 && mPayMethod != 3 && m_bFutureOrder == false &&
						((Global.gUOrderHInfo.nSaleAmount + Global.gUOrderHInfo.nTaxAmount + Global.gUOrderHInfo.nHandlingFee + Global.gUOrderHInfo.nHandlingFeeTaxAmount)) > 0)
				{
					try {
						if(mContext.isFinishing() || mContext.isDestroyed()) return;
						MessageDialog msgDialog = new MessageDialog(mContext);
						msgDialog.setCancelable(false);
						msgDialog.mStrMessage = Str.SelectPaymentMethod[Global.LANG];
						msgDialog.mStrMiddle = Str.Ok[Global.LANG];
						msgDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
							@Override
							public void onDismiss(DialogInterface dialog) {
								m_bConfirmClicked = false;
							}
						});
						msgDialog.show();
					}catch (Exception e)
					{
						e.printStackTrace();
					}

					return;
				}
			}
				/*else if(Global.gCurCustomerInfo.strPayType.trim().equals("3")  && m_bFutureOrder == false)
				{
					if( mPayMethod != 1 && mPayMethod != 2 && mPayMethod != 3 && m_bFutureOrder == false &&
							((Global.gUOrderHInfo.nSaleAmount + Global.gUOrderHInfo.nTaxAmount + Global.gUOrderHInfo.nHandlingFee + Global.gUOrderHInfo.nHandlingFeeTaxAmount)) > 0)
					{
						try {
							if(mContext.isFinishing() || mContext.isDestroyed()) return;
							MessageDialog msgDialog = new MessageDialog(mContext);
							msgDialog.setCancelable(false);
							msgDialog.mStrMessage = String.format(Locale.ENGLISH, Str.ConfirmCollectingPayment[Global.LANG], Global.gCurCustomerInfo.strName);
							msgDialog.mStrLeft = Str.No[Global.LANG];
							msgDialog.mStrRight = Str.Yes[Global.LANG];
							String finalStrRealAmount = strRealAmount;
							msgDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
								@Override
								public void onDismiss(DialogInterface dialog) {
									if(msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT)
									{
										continueProcess(strDelDate, strReference, finalStrRealAmount);
									}
									else
									{
										m_bConfirmClicked = false;
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
				}*/
		}

		continueProcess(strDelDate, strReference, strRealAmount);
	}

	public void continueProcess(String strDelDate, String strReference, String strRealAmount)
	{
		if(mPayMethod == 3)
		{
			Global.gCurRealPaymentAmount = 0;
			try
			{
				Global.gCurRealPaymentAmount = Float.parseFloat(strRealAmount);
			}catch(Exception e)
			{
				e.printStackTrace();
			}

			if (Utils.getIntSetting(mContext, Global.PREF_EWAY_CARD_PROC) == 0) {
				CardDetailFragment fragment = new CardDetailFragment();
				fragment.setParentFragment(this);
				mContext.replaceFragment(fragment);
			} else if(Utils.getIntSetting(mContext, Global.PREF_EWAY_CARD_PROC) == 1){
				onEwayPayment();
			} else if(Utils.getIntSetting(mContext, Global.PREF_EWAY_CARD_PROC) == 2) {
				onMintCardPresent(true);
				m_bConfirmClicked = false;
			} else if(Utils.getIntSetting(mContext, Global.PREF_EWAY_CARD_PROC) == 3) {
				String strMintApiKey = Utils.getStringSetting(mContext, Global.PREF_MINTAPI);
				String strMintSecret = Utils.getStringSetting(mContext, Global.PREF_MINTSECRET);
				if(!TextUtils.isEmpty(strMintApiKey) && !TextUtils.isEmpty(strMintSecret))
				{
					final String strArray[] = {Str.CardPresent[Global.LANG], Str.EwayTransaction[Global.LANG]};
					final int origIdx = mIntPayMethod;
					try {
						if(mContext.isFinishing() || mContext.isDestroyed()) return;
						new AlertDialog.Builder(mContext)
								.setTitle(Str.SelectCardPaymentMethod[Global.LANG])
								.setSingleChoiceItems(new AlertSingleChoiceAdapter(mContext, strArray), mIntPayMethod, (dialog, nSelected) -> mIntPayMethod = nSelected)
								.setPositiveButton(Str.Ok[Global.LANG], (dialog, nSelected) -> {
									if(mIntPayMethod == 0)
									{
										onMintCardPresent(true);
									}
									else
									{
										onEwayPayment();
									}
								})
								.setNegativeButton(Str.Cancel[Global.LANG], (dialog, nSelected) -> mIntPayMethod = origIdx)
								.create()
								.show();
					}catch (Exception e)
					{
						e.printStackTrace();
					}

				}
				else
				{
					onEwayPayment();
				}
				m_bConfirmClicked = false;
			} else if(Utils.getIntSetting(mContext, Global.PREF_EWAY_CARD_PROC) == 4) {
				onMintNotPresent();
				m_bConfirmClicked = false;
			} else if(Utils.getIntSetting(mContext, Global.PREF_EWAY_CARD_PROC) == 5) {
				String strMintApiKey = Utils.getStringSetting(mContext, Global.PREF_MINTAPI);
				String strMintSecret = Utils.getStringSetting(mContext, Global.PREF_MINTSECRET);
				if(!TextUtils.isEmpty(strMintApiKey) && !TextUtils.isEmpty(strMintSecret))
				{
					final String strArray[] = {Str.CardPresent[Global.LANG], Str.NotPresentTransaction[Global.LANG]};
					final int origIdx = mIntPayMethod;
					try {
						if(mContext.isFinishing() || mContext.isDestroyed()) return;
						new AlertDialog.Builder(mContext)
								.setTitle(Str.SelectCardPaymentMethod[Global.LANG])
								.setSingleChoiceItems(new AlertSingleChoiceAdapter(mContext, strArray), mIntPayMethod, (dialog, nSelected) -> mIntPayMethod = nSelected)
								.setPositiveButton(Str.Ok[Global.LANG], (dialog, nSelected) -> {
									if(mIntPayMethod == 0)
									{
										onMintCardPresent(false);
									}
									else
									{
										onMintNotPresent();
									}
								})
								.setNegativeButton(Str.Cancel[Global.LANG], (dialog, nSelected) -> mIntPayMethod = origIdx)
								.create()
								.show();
					}catch (Exception e)
					{
						e.printStackTrace();
					}

				}
				else
				{
					onMintNotPresent();
				}
				m_bConfirmClicked = false;
			}
			else if(Utils.getIntSetting(mContext, Global.PREF_EWAY_CARD_PROC) == 6) {
				onMintIFrame();
				//onMintNotPresent();
				m_bConfirmClicked = false;
			} else if(Utils.getIntSetting(mContext, Global.PREF_EWAY_CARD_PROC) == 7) {
				String strMintApiKey = Utils.getStringSetting(mContext, Global.PREF_MINTAPI);
				String strMintSecret = Utils.getStringSetting(mContext, Global.PREF_MINTSECRET);
				if(!TextUtils.isEmpty(strMintApiKey) && !TextUtils.isEmpty(strMintSecret))
				{
					final String strArray[] = {Str.CardPresent[Global.LANG], Str.MINT_HPP[Global.LANG]};
					final int origIdx = mIntPayMethod;
					try {
						if(mContext.isFinishing() || mContext.isDestroyed()) return;
						new AlertDialog.Builder(mContext)
								.setTitle(Str.SelectCardPaymentMethod[Global.LANG])
								.setSingleChoiceItems(new AlertSingleChoiceAdapter(mContext, strArray), mIntPayMethod, (dialog, nSelected) -> mIntPayMethod = nSelected)
								.setPositiveButton(Str.Ok[Global.LANG], (dialog, nSelected) -> {
									if(mIntPayMethod == 0)
									{
										onMintCardPresent(false);
									}
									else
									{
										onMintIFrame();
									}
								})
								.setNegativeButton(Str.Cancel[Global.LANG], (dialog, nSelected) -> mIntPayMethod = origIdx)
								.create()
								.show();
					}catch (Exception e)
					{
						e.printStackTrace();
					}

				}
				else
				{
					onMintIFrame();
				}
				m_bConfirmClicked = false;
			}
			else if(Utils.getIntSetting(mContext, Global.PREF_EWAY_CARD_PROC) == 8)
			{
				onStripePayment();
				m_bConfirmClicked = false;
			}
			else if(Utils.getIntSetting(mContext, Global.PREF_EWAY_CARD_PROC) == 9)
			{
				onWindCaveNotPresent();
				m_bConfirmClicked = false;
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
						if(mContext.isFinishing() || mContext.isDestroyed()) return;
						new AlertDialog.Builder(mContext)
								.setTitle(Str.SelectCardPaymentMethod[Global.LANG])
								.setSingleChoiceItems(new AlertSingleChoiceAdapter(mContext, strArray), windcavePayMethod, (dialog, nSelected) -> windcavePayMethod = nSelected)
								.setPositiveButton(Str.Ok[Global.LANG], (dialog, nSelected) -> {
									if(windcavePayMethod == 0)
									{
										onWindCavePresent();
									}
									else
									{
										onWindCaveNotPresent();
									}
								})
								.setNegativeButton(Str.Cancel[Global.LANG], (dialog, nSelected) -> windcavePayMethod = origIdx)
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
				m_bConfirmClicked = false;
			}
		}
		else
		{
			processConfirm(strDelDate, strReference, true);
		}
	}

	public void doSavePaymentMethodStatus()
    {
        PaymentCollectInfo info = new PaymentCollectInfo();
        info.strDocNo = mStrPdfFileName.substring(0, mStrPdfFileName.length() - 4);
        info.strPayMethod = String.valueOf(mPayMethod);
        if(mPayMethod == 3)
        {
            info.strKind = strCardType;
            info.strOther = transaction_id;
        }
        DbAdapter dbAdapter = new DbAdapter(mContext);
        dbAdapter.open();
        dbAdapter.insertCollectPaymentStatus(info);
        dbAdapter.close();
    }

	private void processConfirm(String strDelDate, String strReference, boolean bInit)
	{
		showProgressDialog(Str.Processing[Global.LANG]);

		new Thread()
		{
			@Override
			public void run() {
				if(bInit)
				{
					initMakeZipInfo();
				}
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
				if(bConsignment)
				{
//			initMakeZipInfo();
					String strDocNo = mStrPdfFileName.substring(0, mStrPdfFileName.length() - 4);
					long nTrxnNo = setGeneralUploadData("INV", Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo, mPdfTrxnNo, "", strDocNo, mTrxnDate);

					TransactionInfo transactionInfo = new TransactionInfo();
					transactionInfo.strType = "INV";
					transactionInfo.strTime = new SimpleDateFormat("kk:mm").format(new Date(nTrxnNo));
					transactionInfo.strCustomerName = "";
					transactionInfo.strDocNo = mStrPdfFileName.substring(0, mStrPdfFileName.length() - 4);
					DbAdapter dbAdapter = new DbAdapter(mContext);
					dbAdapter.open();
					dbAdapter.insertTransactionHistoryInfo(transactionInfo);
					dbAdapter.close();

					if(mPdfTrxnNo == 0)
					{
						mPdfTrxnNo = System.currentTimeMillis();
					}
			/*UTransactionsInfo uTransactionsInfo = setUTransactionsInfo(0, 0, m_lTrxnNo, "FARC", mStrPdfFileName);
			setUFileTransactionInfo(uTransactionsInfo.nTrxnNo, Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo, mPdfTrxnNo, transactionInfo.strDocNo,
					"CONSIGNMENT", "CONSIGNMENT INVOICE", uTransactionsInfo.strTrxnDate,
					uTransactionsInfo.strTrxnTime, mStrPdfFileName);*/
					//AddTrxnNo(0, 0, mPdfTrxnNo);
					if (Global.gUOrderHInfo != null) {
						Global.gUOrderHInfo.nTrxnNo = mPdfTrxnNo;
						Global.gUOrderHInfo.strDocType = "INV";
						Global.gUOrderHInfo.strDocNo = mStrPdfFileName.substring(0, mStrPdfFileName.length() - 4);
						Global.gUOrderHInfo.nChainNo = Global.gCurCustomerInfo.nChainNo;
						Global.gUOrderHInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
						if(Utils.getStringSetting(mContext, Global.PREF_UNLESHED).equals("1"))
						{
							Global.gUOrderHInfo.strPOReference = "ex Vehicle";
						}
						else
						{
							Global.gUOrderHInfo.strPOReference = ((EditText) mRootView.findViewById(R.id.editReceiptNumber)).getText().toString();
						}

						Global.gUOrderHInfo.strOrderName = ((EditText) mRootView.findViewById(R.id.editReceiverName)).getText().toString();
						Global.gUOrderHInfo.strReference = strReference;

						Global.gUOrderHInfo.nDayNo = mTrxnDate.getDay();
						if(Global.gUOrderHInfo.nDayNo == Time.SUNDAY)
						{
							Global.gUOrderHInfo.nDayNo = 7;
						}
						if(Global.gUOrderHInfo.nDayNo == 0) Global.gUOrderHInfo.nDayNo = 7;

						Global.gUOrderHInfo.strDSD = ((EditText)mRootView.findViewById(R.id.editDeliverTemp)).getText().toString();

						Global.gUOrderHInfo.strDeliveryDate = strDelDate;
						Global.gUOrderHInfo.strDriverNumber = Utils.getStringSetting(mContext, Global.PREF_DRIVER_NUMBER);
						Global.gUOrderHInfo.nNetSales = 0;
						Global.gUOrderHInfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(mContext, mTrxnDate));
						Global.gUOrderHInfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(mTrxnDate);

						UStockHInfo stockHInfo = new UStockHInfo();
						stockHInfo.strDocNo = mStrPdfFileName.substring(0, mStrPdfFileName.length() - 4);
						stockHInfo.strReference = stockHInfo.strDocNo + ".pdf";

						stockHInfo.nTrxnNo = Global.gUOrderHInfo.nTrxnNo;

						stockHInfo.strTrxnDate = Global.gUOrderHInfo.strTrxnDate;
						stockHInfo.strTrxnTime = Global.gUOrderHInfo.strTrxnTime;
						stockHInfo.strDocType = "INV";
						stockHInfo.nChainNo = Global.gCurCustomerInfo.nChainNo;
						stockHInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
						stockHInfo.strStockType = "1"; //---Consignment Type

						dbAdapter = new DbAdapter(mContext);
						dbAdapter.open();

						float nSalesValue = Utils.getFloatSetting(mContext, Global.SALE_TODAY);
						int nSampleCnt = Utils.getIntSetting(mContext, Global.TOTAL_SAMPLE_COUNT);
						int nSalesCnt = Utils.getIntSetting(mContext, Global.TOTAL_SALES_COUNT);
						int nReturnsCnt = Utils.getIntSetting(mContext, Global.TOTAL_RETURNS_COUNT);
						float nDumpsValue = Utils.getFloatSetting(mContext, Global.RETURNS_TODAY);

						for (UOrderDInfo info : Global.gUOrderHInfo.deliveryList) {

							info.nCustNo = Global.gCurCustomerInfo.nCustNo;

							if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
							{
								info.nTrxnNo = mPdfTrxnNo;
								{
									UStockDInfo stockDInfo = new UStockDInfo();
									stockDInfo.nTrxnNo = mPdfTrxnNo;
									stockDInfo.strItemNo = info.strItemNo;
									stockDInfo.dActualQty = info.dSales;
									stockDInfo.dBeginQty = info.dPlanQty;
									stockDInfo.dSuggestQty = 0;
									dbAdapter.insertUStockDInfoList(stockDInfo);

									CustConBalInfo custConBalInfo = new CustConBalInfo();
									custConBalInfo.fWeight = (float)info.dSales;
									custConBalInfo.strItemNo = info.strItemNo;
									custConBalInfo.nChainNo = Global.gCurCustomerInfo.nChainNo;
									custConBalInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;

									dbAdapter.updateCustConBalInfo(custConBalInfo);
								}
								double dSales = info.dSales;
								info.dSales = info.dPlanQty - info.dSales;
								info.dQuantity = info.dSales;
								Global.gUOrderHInfo.dTotalAmount += info.dSales;
								if(Utils.getStringSetting(mContext, Global.PREF_INVCALC).equals("TTx"))
								{
									Global.gUOrderHInfo.nDiscSales += Utils.getRoundedValueForTTx(info.dSales * Utils.getRoundedValueForTTx(info.nBasePrice - info.nPrice));
								}
								else
								{
									Global.gUOrderHInfo.nDiscSales += info.dSales * (info.nBasePrice - info.nGrossPrice);
								}

								Global.gUOrderHInfo.nNetSales += info.dSales * (info.nPrice);
								Global.gUOrderHInfo.nPromotionAmount += info.dSales * (info.nGrossPrice - info.nPrice);

								nSalesValue += info.dSales * info.nPrice;

								//Calculate the Tax Record
								ProdDetlInfo prodDetlInfo = dbAdapter.getProdDetl(info.strItemNo);
								List<TaxRatesInfo> taxRatesInfoList = dbAdapter.getTaxRatesInfoList(Global.gCurCustomerInfo.strTaxCode, info.strItemNo);
								info.nTrxnNo = Global.gUOrderHInfo.nTrxnNo;
								info.nTaxAmount = 0;
								info.nPrintTaxAmount = 0;
								info.nPrintLineTaxAmount = 0;
								info.nPrintCDSAmount = 0;
								boolean bAppliedGST = false;
								float fGSTOnCDS = 0.0f;
								List<UTaxInfo> gstOnCDSTaxRateInfoList = new ArrayList<>();
								for(TaxRatesInfo taxRatesInfo : taxRatesInfoList) {
									float nTaxAmount = 0;
									float nPrintTaxAmount = 0;
									float nPrintLineTaxAmount = 0;
									if (taxRatesInfo.nTaxMethod == 1) //Applying Cents Amount
									{
										nTaxAmount = (float)(Utils.getDividedValue(taxRatesInfo.nTaxRate) * info.dSales * prodDetlInfo.nConsumerUnit);
										nPrintTaxAmount = (float)(Utils.getDividedValue(taxRatesInfo.nTaxRate) * info.dSales * prodDetlInfo.nConsumerUnit);
										nPrintLineTaxAmount = Utils.getDividedValue(taxRatesInfo.nTaxRate) * prodDetlInfo.nConsumerUnit;
									} else if (taxRatesInfo.nTaxMethod == 2) {
										nTaxAmount = (Utils.getRoundedValue(info.dSales * info.nPrice) + info.nTaxAmount) / 100 * Utils.getDividedValue(taxRatesInfo.nTaxRate);
										nPrintTaxAmount = (Utils.getRoundedValue(info.dSales * info.nBasePrice) + info.nTaxAmount) / 100 * Utils.getDividedValue(taxRatesInfo.nTaxRate);
										nPrintLineTaxAmount = (Utils.getRoundedValue(info.nBasePrice) + info.nPrintLineTaxAmount) / 100 * Utils.getDividedValue(taxRatesInfo.nTaxRate);
									}

									if (taxRatesInfo.strTaxType.equals("S")) {
										ProdLocnInfo locnInfo = dbAdapter.getProdLocnInfo(taxRatesInfo.strTaxRateNumber);
										List<TaxRatesInfo> gstOnCDSRatesInfoList = dbAdapter.getTaxRatesInfoList(Global.gCurCustomerInfo.strTaxCode, taxRatesInfo.strTaxRateNumber);
										for (TaxRatesInfo gstOnCDSRatesInfo : gstOnCDSRatesInfoList) {
											if (gstOnCDSRatesInfo.nTaxMethod == 2 && gstOnCDSRatesInfo.strTaxRateCode.equals("GST")) {
												float nGSTOnCDSTaxAmount = Utils.getDividedValue(taxRatesInfo.nTaxRate) / 100 * Utils.getDividedValue(gstOnCDSRatesInfo.nTaxRate) * info.nSales * prodDetlInfo.nConsumerUnit;
												fGSTOnCDS += nGSTOnCDSTaxAmount;

												UTaxInfo uTaxInfo = new UTaxInfo();
												uTaxInfo.nTrxnNo = info.nTrxnNo;
												uTaxInfo.nTrxnType = info.nTrxnType;
												uTaxInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
												uTaxInfo.nLocnNo = locnInfo.nLocnNo;
												uTaxInfo.strItemNo = taxRatesInfo.strTaxRateNumber;
												uTaxInfo.strTaxRateCode = gstOnCDSRatesInfo.strTaxRateCode;
												uTaxInfo.nTaxAmount = Utils.get2DecimalValue(nGSTOnCDSTaxAmount);
												uTaxInfo.strCumulativeFlag = gstOnCDSRatesInfo.strCumulativeFlag;
												uTaxInfo.nTaxRate = gstOnCDSRatesInfo.nTaxRate;
												uTaxInfo.strTaxRateNumber = gstOnCDSRatesInfo.strTaxRateNumber;
												uTaxInfo.nReasonCode = info.nReasonCode;
												gstOnCDSTaxRateInfoList.add(uTaxInfo);
											}
										}
									}

									info.nTaxAmount += Utils.get5RoundedValue(mContext, nTaxAmount);
									info.nPrintTaxAmount += Utils.get5RoundedValue(mContext, nPrintTaxAmount);
									info.nPrintLineTaxAmount += Utils.get5RoundedValue(mContext, nPrintLineTaxAmount);

									if (taxRatesInfo.strTaxRateCode.equals("GST")) {
										bAppliedGST = true;
									}

									if (taxRatesInfo.strTaxType.toUpperCase().equals("S")) {
										info.nPrintCDSAmount += Utils.get2DecimalValue(nPrintTaxAmount);
										info.nCDSRate = taxRatesInfo.nTaxRate;
										PrintTaxRateInfo cdsAmount = Global.gUOrderHInfo.cdsMap.get(taxRatesInfo.strTaxRateCode);
										if (taxRatesInfo.strTaxRateCode.length() > 0 && !taxRatesInfo.strTaxRateCode.equals("0")) {
											if (cdsAmount == null) {
												cdsAmount = new PrintTaxRateInfo();
												cdsAmount.nBottles = (int)(info.dSales * prodDetlInfo.nConsumerUnit);
												cdsAmount.strTaxCode = taxRatesInfo.strTaxRateCode;
												cdsAmount.price = Utils.getDividedValue(taxRatesInfo.nTaxRate);
												cdsAmount.strRateDescription = taxRatesInfo.strRateDescription;

												Global.gUOrderHInfo.cdsMap.put(taxRatesInfo.strTaxRateCode, cdsAmount);
											} else {
												cdsAmount.nBottles += (int)(info.dSales * prodDetlInfo.nConsumerUnit);
											}
										}
									}

									UTaxInfo uTaxInfo = new UTaxInfo();
									uTaxInfo.nTrxnNo = info.nTrxnNo;
									uTaxInfo.nTrxnType = info.nTrxnType;
									uTaxInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
									uTaxInfo.nLocnNo = info.nLocnNo;
									uTaxInfo.strItemNo = info.strItemNo;
									uTaxInfo.strTaxRateCode = taxRatesInfo.strTaxRateCode;
									uTaxInfo.nTaxAmount = Utils.get5RoundedValue(mContext, nTaxAmount);
									uTaxInfo.strCumulativeFlag = taxRatesInfo.strCumulativeFlag;
									uTaxInfo.nTaxRate = taxRatesInfo.nTaxRate;
									uTaxInfo.strTaxRateNumber = taxRatesInfo.strTaxRateNumber;
									uTaxInfo.nReasonCode = info.nReasonCode;
									info.uTaxInfoList.add(uTaxInfo);
								}
								info.nIdx = (int)dbAdapter.insertOrUpdateUOrderDInfo(info);
								info.dSales = dSales;
							}
							else
							{
								info.nTrxnNo = mPdfTrxnNo;

								{
									UStockDInfo stockDInfo = new UStockDInfo();
									stockDInfo.nTrxnNo = mPdfTrxnNo;
									stockDInfo.strItemNo = info.strItemNo;
									stockDInfo.nActualQty = info.nSales;
									stockDInfo.nBeginQty = info.nPlanQty;
									stockDInfo.nSuggestQty = 0;
									dbAdapter.insertUStockDInfoList(stockDInfo);

									CustConBalInfo custConBalInfo = new CustConBalInfo();
									custConBalInfo.nQty = info.nSales;
									custConBalInfo.strItemNo = info.strItemNo;
									custConBalInfo.nChainNo = Global.gCurCustomerInfo.nChainNo;
									custConBalInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;

									dbAdapter.updateCustConBalInfo(custConBalInfo);
								}

								int nSales = info.nSales;
								info.nSales = info.nPlanQty - info.nSales;
								info.nQuantity = info.nSales;

								Global.gUOrderHInfo.nTotalAmount += info.nSales;
								if(Utils.getStringSetting(mContext, Global.PREF_INVCALC).equals("TTx"))
								{
									Global.gUOrderHInfo.nDiscSales += Utils.getRoundedValueForTTx(info.nSales * Utils.getRoundedValueForTTx(info.nBasePrice - info.nPrice));
								}
								else
								{
									Global.gUOrderHInfo.nDiscSales += info.nSales * (info.nBasePrice - info.nGrossPrice);
								}

								Global.gUOrderHInfo.nNetSales += info.nSales * (info.nPrice);
								Global.gUOrderHInfo.nPromotionAmount += info.nSales * (info.nGrossPrice - info.nPrice);

								nSalesCnt += info.nSales;
								nSalesValue += info.nSales * info.nPrice;

								//Calculate the Tax Record
								ProdDetlInfo prodDetlInfo = dbAdapter.getProdDetl(info.strItemNo);
								List<TaxRatesInfo> taxRatesInfoList = dbAdapter.getTaxRatesInfoList(Global.gCurCustomerInfo.strTaxCode, info.strItemNo);
								info.nTrxnNo = Global.gUOrderHInfo.nTrxnNo;
								info.nTaxAmount = 0;
								info.nPrintTaxAmount = 0;
								info.nPrintLineTaxAmount = 0;
								info.nPrintCDSAmount = 0;
								boolean bAppliedGST = false;
								float fGSTOnCDS = 0.0f;
								List<UTaxInfo> gstOnCDSTaxRateInfoList = new ArrayList<>();
								for(TaxRatesInfo taxRatesInfo : taxRatesInfoList) {
									float nTaxAmount = 0;
									float nPrintTaxAmount = 0;
									float nPrintLineTaxAmount = 0;
									if (taxRatesInfo.nTaxMethod == 1) //Applying Cents Amount
									{
										nTaxAmount = Utils.getDividedValue(taxRatesInfo.nTaxRate) * info.nSales * prodDetlInfo.nConsumerUnit;
										nPrintTaxAmount = Utils.getDividedValue(taxRatesInfo.nTaxRate) * info.nSales * prodDetlInfo.nConsumerUnit;
										nPrintLineTaxAmount = Utils.getDividedValue(taxRatesInfo.nTaxRate) * prodDetlInfo.nConsumerUnit;
									} else if (taxRatesInfo.nTaxMethod == 2) {
										nTaxAmount = (Utils.getRoundedValue(info.nSales * (info.nPrice)) + info.nTaxAmount) / 100 * Utils.getDividedValue(taxRatesInfo.nTaxRate);
										nPrintTaxAmount = (Utils.getRoundedValue(info.nSales * info.nBasePrice) + info.nTaxAmount) / 100 * Utils.getDividedValue(taxRatesInfo.nTaxRate);
										nPrintLineTaxAmount = (Utils.getRoundedValue(info.nBasePrice) + info.nPrintLineTaxAmount) / 100 * Utils.getDividedValue(taxRatesInfo.nTaxRate);
									}

									if (taxRatesInfo.strTaxType.equals("S")) {
										ProdLocnInfo locnInfo = dbAdapter.getProdLocnInfo(taxRatesInfo.strTaxRateNumber);
										List<TaxRatesInfo> gstOnCDSRatesInfoList = dbAdapter.getTaxRatesInfoList(Global.gCurCustomerInfo.strTaxCode, taxRatesInfo.strTaxRateNumber);
										for (TaxRatesInfo gstOnCDSRatesInfo : gstOnCDSRatesInfoList) {
											if (gstOnCDSRatesInfo.nTaxMethod == 2 && gstOnCDSRatesInfo.strTaxRateCode.equals("GST")) {
												float nGSTOnCDSTaxAmount = Utils.getDividedValue(taxRatesInfo.nTaxRate) / 100 * Utils.getDividedValue(gstOnCDSRatesInfo.nTaxRate) * info.nSales * prodDetlInfo.nConsumerUnit;
												fGSTOnCDS += nGSTOnCDSTaxAmount;

												UTaxInfo uTaxInfo = new UTaxInfo();
												uTaxInfo.nTrxnNo = info.nTrxnNo;
												uTaxInfo.nTrxnType = info.nTrxnType;
												uTaxInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
												uTaxInfo.nLocnNo = locnInfo.nLocnNo;
												uTaxInfo.strItemNo = taxRatesInfo.strTaxRateNumber;
												uTaxInfo.strTaxRateCode = gstOnCDSRatesInfo.strTaxRateCode;
												uTaxInfo.nTaxAmount = Utils.get2DecimalValue(nGSTOnCDSTaxAmount);
												uTaxInfo.strCumulativeFlag = gstOnCDSRatesInfo.strCumulativeFlag;
												uTaxInfo.nTaxRate = gstOnCDSRatesInfo.nTaxRate;
												uTaxInfo.strTaxRateNumber = gstOnCDSRatesInfo.strTaxRateNumber;
												uTaxInfo.nReasonCode = info.nReasonCode;
												gstOnCDSTaxRateInfoList.add(uTaxInfo);
											}
										}
									}

									info.nTaxAmount += Utils.get5RoundedValue(mContext, nTaxAmount);
									info.nPrintTaxAmount += Utils.get5RoundedValue(mContext, nPrintTaxAmount);
									info.nPrintLineTaxAmount += Utils.get5RoundedValue(mContext, nPrintLineTaxAmount);

									if (taxRatesInfo.strTaxRateCode.equals("GST")) {
										bAppliedGST = true;
									}

									if (taxRatesInfo.strTaxType.toUpperCase().equals("S")) {
										info.nPrintCDSAmount += Utils.get2DecimalValue(nPrintTaxAmount);
										info.nCDSRate = taxRatesInfo.nTaxRate;
										PrintTaxRateInfo cdsAmount = Global.gUOrderHInfo.cdsMap.get(taxRatesInfo.strTaxRateCode);
										if (taxRatesInfo.strTaxRateCode.length() > 0 && !taxRatesInfo.strTaxRateCode.equals("0")) {
											if (cdsAmount == null) {
												cdsAmount = new PrintTaxRateInfo();
												cdsAmount.nBottles = info.nSales * prodDetlInfo.nConsumerUnit;
												cdsAmount.strTaxCode = taxRatesInfo.strTaxRateCode;
												cdsAmount.price = Utils.getDividedValue(taxRatesInfo.nTaxRate);
												cdsAmount.strRateDescription = taxRatesInfo.strRateDescription;

												Global.gUOrderHInfo.cdsMap.put(taxRatesInfo.strTaxRateCode, cdsAmount);
											} else {
												cdsAmount.nBottles += info.nSales * prodDetlInfo.nConsumerUnit;
											}
										}
									}

									UTaxInfo uTaxInfo = new UTaxInfo();
									uTaxInfo.nTrxnNo = info.nTrxnNo;
									uTaxInfo.nTrxnType = info.nTrxnType;
									uTaxInfo.nCustNo = info.nCustNo;
									uTaxInfo.nLocnNo = info.nLocnNo;
									uTaxInfo.strItemNo = info.strItemNo;
									uTaxInfo.strTaxRateCode = taxRatesInfo.strTaxRateCode;
									uTaxInfo.nTaxAmount = Utils.get5RoundedValue(mContext, nTaxAmount);
									uTaxInfo.strCumulativeFlag = taxRatesInfo.strCumulativeFlag;
									uTaxInfo.nTaxRate = taxRatesInfo.nTaxRate;
									uTaxInfo.strTaxRateNumber = taxRatesInfo.strTaxRateNumber;
									uTaxInfo.nReasonCode = info.nReasonCode;
									info.uTaxInfoList.add(uTaxInfo);
								}

								info.nIdx = (int)dbAdapter.insertOrUpdateUOrderDInfo(info);
								info.nSales = nSales;
							}
						}

						Utils.setFloatSetting(mContext, Global.RETURNS_TODAY, nDumpsValue);
						Utils.setFloatSetting(mContext, Global.SALE_TODAY, nSalesValue);
						Utils.setIntSetting(mContext, Global.TOTAL_SAMPLE_COUNT, nSampleCnt);
						Utils.setIntSetting(mContext, Global.TOTAL_SALES_COUNT, nSalesCnt);
						Utils.setIntSetting(mContext, Global.TOTAL_RETURNS_COUNT, nReturnsCnt);

						dbAdapter.insertUConsignmentInfoList(stockHInfo);
						dbAdapter.insertOrUpdateUOrderHInfo(Global.gUOrderHInfo);
						dbAdapter.close();
					}
					//Utils.registerUploadFile(mContext, makeZipFileInfo);
				}
				else
				{
					if(!bReverseMode)
					{
						// Save Current Payment Method mPayMethod = 0, mPayMethod = 1 (Cash), mPayMethod = 2, mPayMethod = 3 with EWay, mInt Processing
						doSavePaymentMethodStatus();
					}

					if (mStrPhotoPathList.size() > 0)
					{
						int nIdx = 1;
						for(String strPhotoPath : mStrPhotoPathList)
						{
							UFileTransactionInfo uFileTransactionInfo = new UFileTransactionInfo();
							String strRef = setUCameraData("CAM", strPhotoPath, "", uFileTransactionInfo, nIdx++, false);
							{
								setUImageFileTransactionInfo(Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo,  uFileTransactionInfo.nFileTrxnNo, "", "SALESUMMARY", "GR" + mStrPdfFileName.substring(0, mStrPdfFileName.length() - 4),
										uFileTransactionInfo.strFileCreateDate, uFileTransactionInfo.strFileCreateTime, strRef);
							}
						}

					}

					String strDocNo = "";
					if(mStrPdfFileName.length() > 4)
					{
						strDocNo = mStrPdfFileName.substring(0, mStrPdfFileName.length() - 4);
					}
					long nTrxnNo = setGeneralUploadData("IVUP", Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo, -1, "", strDocNo, mTrxnDate);
					Date delDate = Utils.getDateFromString(Global.gUOrderHInfo.strDeliveryDate);
					Date cDate = new Date(System.currentTimeMillis());

					TransactionInfo transactionInfo = new TransactionInfo();
					if(Global.gUOrderHInfo.futureOrderMethod == 0)
					{
						nTrxnNo = setGeneralUploadData(cDate.compareTo(delDate) < 0 /*&& !bReverseMode*/ ? "ORD" : "INV", Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo, Global.gUOrderHInfo.nTrxnNo, "", strDocNo, mTrxnDate);
						if (cDate.compareTo(delDate) < 0 /*&& !bReverseMode*/)
							transactionInfo.strType = "ORD";
						else
							transactionInfo.strType = "INV";
					}
					else
					{
						nTrxnNo = setGeneralUploadData("ORD", Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo, Global.gUOrderHInfo.nTrxnNo, "", strDocNo, mTrxnDate);
						transactionInfo.strType = "ORD";
					}

					transactionInfo.strTime = new SimpleDateFormat("kk:mm").format(new Date(nTrxnNo));
					transactionInfo.strCustomerName = Global.gCurCustomerInfo.strName;
					transactionInfo.strDocNo = strDocNo;
					transactionInfo.strVoid = bReverseMode ? "Yes" : "No";
					transactionInfo.value = Global.gUOrderHInfo.nSaleAmount + Global.gUOrderHInfo.nTaxAmount + Global.gUOrderHInfo.nHandlingFee + Global.gUOrderHInfo.nHandlingFeeTaxAmount;

					DbAdapter dbAdapter = new DbAdapter(mContext);
					dbAdapter.open();
					dbAdapter.insertTransactionHistoryInfo(transactionInfo);
					dbAdapter.close();

					//AddTrxnNo(Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo, Global.gUOrderHInfo.nTrxnNo);
					Global.gUOrderHInfo.nChainNo = Global.gCurCustomerInfo.nChainNo;
					Global.gUOrderHInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
					Global.gUOrderHInfo.nPeriodNo = Global.gCurCustomerInfo.nPeriodNo;
					Global.gUOrderHInfo.strDriverNumber = Utils.getStringSetting(mContext, Global.PREF_DRIVER_NUMBER);
					Global.gUOrderHInfo.nDayNo = mCurDate.getDay();
					if(Global.gUOrderHInfo.nDayNo == Time.SUNDAY)
					{
						Global.gUOrderHInfo.nDayNo = 7;
					}
					if(Global.gUOrderHInfo.nDayNo == 0) Global.gUOrderHInfo.nDayNo = 7;

					Global.gUOrderHInfo.strDSD = ((EditText)mRootView.findViewById(R.id.editDeliverTemp)).getText().toString();

					Global.gUOrderHInfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(mContext, mTrxnDate));
					Global.gUOrderHInfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(mTrxnDate);
					Global.gUOrderHInfo.strDistributor = "";
					Global.gUOrderHInfo.strDocType = transactionInfo.strType;

					if(Utils.getStringSetting(mContext, Global.PREF_UNLESHED).equals("1"))
					{
						if(transactionInfo.strType.equals("ORD"))
						{
							Global.gUOrderHInfo.strPOReference = "Warehouse";
						}
						else if(transactionInfo.strType.equals("INV"))
						{
							Global.gUOrderHInfo.strPOReference = "ex Vehicle";
						}
					}
					else
					{
						Global.gUOrderHInfo.strPOReference = ((EditText) mRootView.findViewById(R.id.editReceiptNumber)).getText().toString();
					}
					Global.gUOrderHInfo.strOrderName = ((EditText) mRootView.findViewById(R.id.editReceiverName)).getText().toString();
					Global.gUOrderHInfo.strReference = strReference;
					Global.gUOrderHInfo.strDeliveryDate = strDelDate;

					if (mStrPdfFileName.length() > 4) {
						Global.gUOrderHInfo.strDocNo = strDocNo;
						String strPDFFormat = Utils.getStringSetting(mContext, Global.PREF_UPLOAD_INV_FMT);
						if( strPDFFormat.equals("1") )
						{
							mStrPdfFileName_ = Global.gUOrderHInfo.strDocNo + "_.pdf";
						}
					}

					dbAdapter = new DbAdapter(mContext);
					dbAdapter.open();

					float nSalesValue = Utils.getFloatSetting(mContext, Global.SALE_TODAY);
					int nSampleCnt = Utils.getIntSetting(mContext, Global.TOTAL_SAMPLE_COUNT);
					int nSalesCnt = Utils.getIntSetting(mContext, Global.TOTAL_SALES_COUNT);
					int nReturnsCnt = Utils.getIntSetting(mContext, Global.TOTAL_RETURNS_COUNT);
					float nDumpsValue = Utils.getFloatSetting(mContext, Global.RETURNS_TODAY);

					for (UOrderDInfo info : Global.gUOrderHInfo.deliveryList) {
						info.nTrxnNo = Global.gUOrderHInfo.nTrxnNo;
						dbAdapter.insertOrUpdateUOrderDInfo(info);
					}
					for (UOrderDInfo info : Global.gUOrderHInfo.pickupList) {
						info.nTrxnNo = Global.gUOrderHInfo.nTrxnNo;
						dbAdapter.insertOrUpdateUOrderDInfo(info);
					}
					if(!bReverseMode)
					{
						for(UOrderDInfo info : Global.gUOrderHInfo.deliveryList){
							if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
							{
								nSalesValue += info.dSales * info.nPrice;
							}
							else
							{
								if(info.nTrxnType == Global.TRXN_SAMPLE || info.nTrxnType == Global.TRXN_FREE)
								{
									nSampleCnt += info.nSales;
								}
								else
								{
									nSalesCnt += info.nSales;
								}
								nSalesValue += info.nSales * info.nPrice;
							}
						}

						for (UOrderDInfo info : Global.gUOrderHInfo.pickupList) {
							if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
							{
								nDumpsValue += info.dDumps * info.nPrice;
								nSalesValue -= info.dDumps * info.nPrice;
							}
							else
							{
								nDumpsValue += info.nDumps * info.nPrice;
								nSalesValue -= info.nDumps * info.nPrice;

								nReturnsCnt += info.nDumps;
							}
						}

						Utils.setFloatSetting(mContext, Global.RETURNS_TODAY, nDumpsValue);
						Utils.setFloatSetting(mContext, Global.SALE_TODAY, nSalesValue);
						Utils.setIntSetting(mContext, Global.TOTAL_SAMPLE_COUNT, nSampleCnt);
						Utils.setIntSetting(mContext, Global.TOTAL_SALES_COUNT, nSalesCnt);
						Utils.setIntSetting(mContext, Global.TOTAL_RETURNS_COUNT, nReturnsCnt);
					}
					else
					{
						for(UOrderDInfo info : Global.gUOrderReverseOriginalHInfo.deliveryList){
							if(info.dSales > 0 || info.nSales > 0)
							{
								if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
								{
									nSalesValue -= info.dSales * info.nPrice;
								}
								else
								{
									if(info.nTrxnType == Global.TRXN_SAMPLE || info.nTrxnType == Global.TRXN_FREE)
									{
										nSampleCnt -= info.nSales;
									}
									else
									{
										nSalesCnt -= info.nSales;
									}
									nSalesValue -= info.nSales * info.nPrice;
								}
							}
							else if(info.nDumps > 0 || info.dDumps > 0)
							{
								if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
								{
									nDumpsValue -= info.dDumps * info.nPrice;
									nSalesValue += info.dDumps * info.nPrice;
								}
								else
								{
									nDumpsValue -= info.nDumps * info.nPrice;
									nSalesValue += info.nDumps * info.nPrice;

									nReturnsCnt -= info.nDumps;
								}
							}
						}
						Utils.setFloatSetting(mContext, Global.RETURNS_TODAY, nDumpsValue);
						Utils.setFloatSetting(mContext, Global.SALE_TODAY, nSalesValue);
						Utils.setIntSetting(mContext, Global.TOTAL_SAMPLE_COUNT, nSampleCnt);
						Utils.setIntSetting(mContext, Global.TOTAL_SALES_COUNT, nSalesCnt);
						Utils.setIntSetting(mContext, Global.TOTAL_RETURNS_COUNT, nReturnsCnt);
					}


					for (UOrderDInfo info : Global.gUOrderHInfo.handCountList){
						info.nTrxnType = 3;
						info.nTrxnNo = Global.gUOrderHInfo.nTrxnNo;
						dbAdapter.insertOrUpdateUOrderDInfo(info);
					}
					for (UOrderDInfo containerInfo : Global.gUOrderHInfo.containerList) {
						if ((containerInfo.nTrxnType == Global.TRXN_CONTAINER_SALE && containerInfo.nSales != 0) || (containerInfo.nTrxnType == Global.TRXN_CONTAINER_DUMP && containerInfo.nDumps != 0)) {
							containerInfo.nTrxnNo = Global.gUOrderHInfo.nTrxnNo;
							dbAdapter.insertOrUpdateUOrderDInfo(containerInfo);
						}
					}

					long nUInvenTrxnNo = Utils.getLongSetting(mContext, Global.PREF_UINVEN_TRXN_NO);
					for (UInvenDInfo info : mUInvenHinfo.uInvenDList) {
						if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
						{
							double dSales = getSalesQuantityD(info.strItemNo);
							info.dSales += dSales;
							info.dStock -= dSales;
							info.dDumps += getDumpsQuantityD(info.strItemNo);
							double dBuybacks = getBuyBacksValuesD(info.strItemNo);
							info.dBuybacks += dBuybacks;
							info.dStock += dBuybacks;
							if(dSales > 0)
							{
								double dResPresold = 0;
								if(isPeddle(info.strItemNo))
								{
									info.dResPeddle -= dSales;
									if( info.dResPeddle < 0 )
									{
										dResPresold = info.dResPeddle * -1f;
										info.dResPeddle = 0;
									}
								}
								else
								{
									dResPresold = dSales;
								}
								info.dResPresold -= dResPresold;

								if( info.dResPresold < 0 )
									info.dResPresold = 0;
							}

							double dContainerQty = 0;
							for (UOrderDInfo containerInfo : Global.gUOrderHInfo.containerList) {
								if (containerInfo.strItemNo.equals(info.strItemNo) == true) {
									if (containerInfo.nTrxnType == Global.TRXN_CONTAINER_SALE) {
										if (containerInfo.dSales != 0) {
											dContainerQty = containerInfo.dSales * -1f;
										}
									} else {
										if (containerInfo.nDumps != 0) {
											dContainerQty = containerInfo.dDumps;
										}
									}
									break;
								}
							}
							info.dStock += dContainerQty;
							if (info.nTrxnNo == 0) {
								List<ProdDetlInfo> productList = dbAdapter.getProductList(info.strItemNo);
								if( productList != null && productList.size() > 0 )
								{
									info.strDescription = productList.get(0).strDescription;
									info.strShortDesc = productList.get(0).strShortDesc;
									info.strPickLocationCode = "";
								}
								info.nTrxnNo = nUInvenTrxnNo;
								if( (info.dSales != 0|| info.dDumps != 0 || info.dBuybacks != 0) )
								{
									info.bShow = true;
								}
								else if( info.dStock == 0)
								{
									info.bShow = false;
								}

								info.nIdx = (int)dbAdapter.insertUInvenDInfo(info);
							} else {
								if( (info.dSales != 0|| info.dDumps != 0 || info.dBuybacks != 0) )
								{
									info.bShow = true;
								}
								else if( info.dStock == 0)
								{
									info.bShow = false;
								}
								dbAdapter.updateUInvenDInfo(info);
							}
							info.nTrxnNo = m_lTrxnNo;
							info.nIdx = (int)dbAdapter.insertUInvenDInfo(info);
						}
						else
						{
							int nSales = getSalesQuantity(info.strItemNo);
							info.nSales += nSales;
							info.nStock -= nSales;
							int nDumps = getDumpsQuantity(info.strItemNo);
							info.nDumps += nDumps;
							int nBuybacks = getBuyBacksValues(info.strItemNo);
							info.nBuybacks += nBuybacks;
							info.nStock += nBuybacks;
							if(nSales > 0)
							{
								int nResPresold = 0;
								if(isPeddle(info.strItemNo))
								{
									info.nResPeddle -= nSales;
									if( info.nResPeddle < 0 )
									{
										nResPresold = info.nResPeddle * -1;
										info.nResPeddle = 0;
									}
								}
								else
								{
									nResPresold = nSales;
								}
								info.nResPresold -= nResPresold;

								if( info.nResPresold < 0 )
									info.nResPresold = 0;
							}

							if(info.bLotControl)
							{
								if(info.loadBlotInfoList != null && info.loadBlotInfoList.size() >= nSales && nSales > 0)
								{
									List<LoadBlotInfo> uLoadBlotInfoList = getSalesLoadBlotArray(info.strItemNo);
									if(uLoadBlotInfoList.size() > 0)
									{
										Map<String, Integer> uLoadMap = new HashMap<>();
										for(LoadBlotInfo loadBlotInfo : uLoadBlotInfoList)
										{
											if(uLoadMap.get(loadBlotInfo.strLotNumber) == null)
											{
												uLoadMap.put(loadBlotInfo.strLotNumber, 1);
											}
											else
											{
												uLoadMap.put(loadBlotInfo.strLotNumber, uLoadMap.get(loadBlotInfo.strLotNumber) + 1);
											}
										}

										List<LoadBlotInfo> delInfoList = new ArrayList<>();
										for(LoadBlotInfo loadBlotInfo : info.loadBlotInfoList)
										{
											Integer nSaleCnt = uLoadMap.get(loadBlotInfo.strLotNumber);
											if(nSaleCnt != null && nSaleCnt > 0)
											{
												delInfoList.add(loadBlotInfo);
												uLoadMap.put(loadBlotInfo.strLotNumber, nSaleCnt - 1);
											}
										}
										for(LoadBlotInfo loadBlotInfo : delInfoList)
										{
											info.loadBlotInfoList.remove(loadBlotInfo);
										}
									}

								}

								if(nDumps > 0)
								{
									List<LoadBlotInfo> uLoadBlotInfoList = getReturnsLoadBlotArray(info.strItemNo);
									if(uLoadBlotInfoList.size() > 0)
									{
										info.loadBlotInfoList.addAll(uLoadBlotInfoList);
									}
								}
							}

							int nContainerQty = 0;
							for (UOrderDInfo containerInfo : Global.gUOrderHInfo.containerList) {
								if (containerInfo.strItemNo.equals(info.strItemNo) == true) {
									if (containerInfo.nTrxnType == Global.TRXN_CONTAINER_SALE) {
										if (containerInfo.nSales != 0) {
											nContainerQty = containerInfo.nSales * -1;
										}
									} else {
										if (containerInfo.nDumps != 0) {
											nContainerQty = containerInfo.nDumps;
										}
									}
									break;
								}
							}
							info.nStock += nContainerQty;
							if (info.nTrxnNo == 0) {
								List<ProdDetlInfo> productList = dbAdapter.getProductList(info.strItemNo);
								if( productList != null && productList.size() > 0 )
								{
									info.strDescription = productList.get(0).strDescription;
									info.strShortDesc = productList.get(0).strShortDesc;
									info.strPickLocationCode = "";
								}
								info.nTrxnNo = nUInvenTrxnNo;
								if( (info.nSales != 0|| info.nDumps != 0 || info.nBuybacks != 0) )
								{
									info.bShow = true;
								}
								else if( info.nStock == 0)
								{
									info.bShow = false;
								}

								info.nIdx = (int)dbAdapter.insertUInvenDInfo(info);
							} else {
								if( (info.nSales != 0|| info.nDumps != 0 || info.nBuybacks != 0) )
								{
									info.bShow = true;
								}
								else if( info.nStock == 0)
								{
									info.bShow = false;
								}
								dbAdapter.updateUInvenDInfo(info);
							}
							info.nTrxnNo = m_lTrxnNo;
							info.nIdx = (int)dbAdapter.insertUInvenDInfo(info);
						}
					}
					mUInvenHinfo.nTrxnNo = m_lTrxnNo;
					mUInvenHinfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
					mUInvenHinfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(mContext, mTrxnDate));
					mUInvenHinfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(mTrxnDate);
					mUInvenHinfo.strDocType = "IVUP";
					dbAdapter.insertUInvenHInfo(mUInvenHinfo);
					Global.gUOrderHInfo.strDeliveryDate = strDelDate;
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
					Global.gUOrderHInfo.bCentsRound = bCentsRount;

					if(bReverseMode && Global.gUOrderReverseOriginalHInfo != null)
					{
						Global.gUOrderReverseOriginalHInfo.nVoidTrxnNo = Global.gUOrderHInfo.nTrxnNo;
						Global.gUOrderHInfo.nOrgTrxnNo = Global.gUOrderReverseOriginalHInfo.nTrxnNo;
						dbAdapter.insertOrUpdateUOrderHInfo(Global.gUOrderReverseOriginalHInfo);
					}

					dbAdapter.insertOrUpdateUOrderHInfo(Global.gUOrderHInfo);

					if( Global.gCurCustomerInfo.presoldOrHInfo != null && Global.gCurCustomerInfo.presoldOrHInfo.size() > 0 )
					{
						for(PresoldOrHInfo info : Global.gCurCustomerInfo.presoldOrHInfo)
						{
							info.nVisited = 1;
							info.nCustIdx = Global.gCurCustomerInfo.nIdx;
							dbAdapter.updatePresoldOrHInfo(info);
						}
					}

					if(Global.gCurCustomerInfo.latestOrderInfo != null && Global.gCurCustomerInfo.latestOrderInfo.size() > 0)
					{
						for(CustomerOrderInfo info : Global.gCurCustomerInfo.latestOrderInfo)
						{
							dbAdapter.updateCustomerLatestOrderStatus(info);
						}
					}

					UOrderStatusHInfo uOrderStatusInfo = new UOrderStatusHInfo();
					uOrderStatusInfo.nTrxnNo = mUInvenHinfo.nTrxnNo;
					uOrderStatusInfo.strDocNo = Global.gUOrderHInfo.strDocNo;;
					uOrderStatusInfo.strDocType = "IVUP";
					uOrderStatusInfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(mContext, mTrxnDate));
					uOrderStatusInfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(mTrxnDate);
					UOrderStatusDInfo dInfo = new UOrderStatusDInfo();
					dInfo.nChainNo = Global.gCurCustomerInfo.nChainNo;
					dInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
					dInfo.nDayNo = Global.gCurCustomerInfo.nDayNo;

					//Disabled from the VS189
		/*if(Global.gCurCustomerInfo.bFromAddCustomer || Utils.getIntSetting(mContext, Global.PREF_LOAD_CUSTOMER) == 0)
		{
			dInfo.nStatus = 5;
		}
		else
		{
			dInfo.nStatus = 1;
		}*/
					dInfo.nStatus = 5;
					dInfo.strReference = "";
					//dInfo.strDeliveryDate = Global.gCurCustomerInfo.strDeliveryDate;
					dInfo.strDeliveryDate = uOrderStatusInfo.strTrxnDate;
					dInfo.nTrxnNo = mUInvenHinfo.nTrxnNo;

					if(Global.gCurCustomerInfo.presoldOrHInfo != null && Global.gCurCustomerInfo.presoldOrHInfo.size() > 0)
					{
						dInfo.strOrderNo = Global.gCurCustomerInfo.presoldOrHInfo.get(0).strOrderNo;
						dInfo.nDeliverySequence = Global.gCurCustomerInfo.presoldOrHInfo.get(0).nDeliverySequence;
						dInfo.strOrderBarcode = Global.gCurCustomerInfo.presoldOrHInfo.get(0).strOrderBarCode;
						dInfo.strPORef = Global.gCurCustomerInfo.presoldOrHInfo.get(0).strPORef;
					}
					else if(Global.gCurCustomerInfo.latestOrderInfo != null && Global.gCurCustomerInfo.latestOrderInfo.size() > 0)
					{
						dInfo.strOrderNo = String.valueOf(Global.gCurCustomerInfo.latestOrderInfo.get(0).orderNumber);
						dInfo.strPORef = Global.gCurCustomerInfo.latestOrderInfo.get(0).purchaseOrderNumber;
					}
					else
					{
						dInfo.strOrderNo = "";
						dInfo.strOrderBarcode = "";
						dInfo.nDeliverySequence = Global.gCurCustomerInfo.nSeqNo;
					}
					uOrderStatusInfo.uOrderStatusDInfoList.add(dInfo);
					dbAdapter.insertUOrderStatusInfo(uOrderStatusInfo);
//		dbAdapter.updateOrInsertOrderStatusInfo(uOrderStatusInfo);

					//Save GPS Location to DB.
					double nLat; double nLng;
					if(mGPSNewPickedUp)
					{
						nLat = Global.gCurCustomerInfo.driverLatitude;
						nLng = Global.gCurCustomerInfo.driverLongitude;
					}
					else
					{
						nLat = Utils.getDoubleSetting(mContext, Global.PREF_MY_LATITUDE);
						nLng = Utils.getDoubleSetting(mContext, Global.PREF_MY_LONGITUDE);
					}

					UGPSInfo uGPSInfo = new UGPSInfo();
					nTrxnNo = mUInvenHinfo.nTrxnNo;
					uGPSInfo.nTrxnNo = nTrxnNo;
					uGPSInfo.nChainNo = Global.gCurCustomerInfo.nChainNo;
					uGPSInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
					uGPSInfo.nStartTrxnNo = nTrxnNo;
					if (nLat != Global.DEFAULT_FLOAT && nLng != Global.DEFAULT_FLOAT) {
						uGPSInfo.strGPSData = String.format(Locale.ENGLISH, "%f, %f", nLat, nLng);
					} else {
						uGPSInfo.strGPSData = "ERROR: NO GPS";
					}
					if(mGPSNewPickedUp)
					{
						uGPSInfo.strDocType = "MAIT";
					}
					else
					{
						uGPSInfo.strDocType = "GPS";
					}
					uGPSInfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(mContext, mTrxnDate));
					uGPSInfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(mTrxnDate);
					if(mStrPdfFileName.length() > 4)
					{
						uGPSInfo.strDocNo = mStrPdfFileName.substring(0, mStrPdfFileName.length() - 4);
					}
					dbAdapter.insertUGPSInfo(uGPSInfo);
//		AddTrxnNo(uGPSInfo.nCustNo, uGPSInfo.nChainNo, nTrxnNo);

					dbAdapter.close();

					String strRealAmount = ((EditText)mRootView.findViewById(R.id.editTotalCollection)).getText().toString();
					Global.gCurRealPaymentAmount = 0;
					try
					{
						Global.gCurRealPaymentAmount = Float.parseFloat(strRealAmount);
					}catch(Exception e)
					{
						e.printStackTrace();
					}

					Global.makeZipFileInfo = makeZipFileInfo;
				}

				//Global.makeZipFileInfoList.add(makeZipFileInfo);
				//Utils.registerUploadFile(mContext, makeZipFileInfo);
				new Handler(Looper.getMainLooper()).post(new Runnable() {
					@Override
					public void run() {
						hideProgressDialog();
						performCreatePdfTask();
					}
				});

			}
		}.start();

	}

	public void processCardDetailConfirm()
	{
		PaymentCollectInfo info = new PaymentCollectInfo();
		if(!TextUtils.isEmpty(mStrPdfFileName) && mStrPdfFileName.length() > 4)
		{
			info.strDocNo = mStrPdfFileName.substring(0, mStrPdfFileName.length() - 4);
		}
		info.strPayMethod = "3";
		info.strKind = "other";
		info.strOther = Global.gUARPaymentCardInfo.strInvoiceNo + "||" + Global.gUARPaymentCardInfo.strARNotes
		+ "||" + Global.gUARPaymentCardInfo.nTrxnAmount;

		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		dbAdapter.insertCollectPaymentStatus(info);
		dbAdapter.close();

		performCreatePdfTask();
	}

	private boolean isPdfCreated = false;
	public void performCreatePdfTask()
	{
		new Thread(){
			@SuppressLint("SuspiciousIndentation")
			@Override
			public void run() {
				if(!isPdfCreated)
				{
					new Handler(Looper.getMainLooper()).post(() -> {
						try {
							showProgressDialog(Str.CreatingPdf[Global.LANG]);
						}catch (Exception e)
						{
							e.printStackTrace();
						}

					});
					String shortDescForFile, longDescForFile;
					Utils.createDirectory(Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/");
					Utils.createDirectory(Global.PDF_LOCAL_DIR);
					if(bConsignment)
					{
						shortDescForFile = "CONSIGNMENT";
						String strTemplateFileName;
						int nTemplateType;
						if(Global.gCurCustomerInfo.nShowPrice == 2)
						{
							longDescForFile = "CONSIGNMENT DOCKET";
							strTemplateFileName = Global.PRINT_CONSIGNMENT_DOCKET_FILENAME;
							nTemplateType = Global.CONSIGNMENT_DOCKET_PRINT;
						}
						else
						{
							longDescForFile = "CONSIGNMENT INVOICE";
							strTemplateFileName = Global.PRINT_CONSIGNMENT_FILENAME;
							nTemplateType = Global.CONSIGNMENT_COUNT_PRINT;
						}

						if(Utils.isFileExist(Global.ROOT_PRINT_TEMPLATE_LOCCATION + "/" + strTemplateFileName))
						{
							PrintEngine printEngine = new PrintEngine();
							mHoneywellPrintEngine = printEngine;
							Utils.createDirectory(Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/");
							if(mPdfTrxnNo == 0)
							{
								mPdfTrxnNo = System.currentTimeMillis();
							}
							if(TextUtils.isEmpty(mStrPdfFileName))
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
							Global.gUOrderHInfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(mContext, mTrxnDate));
							Global.gUOrderHInfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(mTrxnDate);
							Global.gUOrderHInfo.strDocNo = mStrPdfFileName.substring(0, mStrPdfFileName.length() - 4);

							printEngine.PrepareConsignmentPrint(mContext, Global.gUOrderHInfo.deliveryList,  Global.gUOrderHInfo.strSignatureFilePath, mPayMethod, priceGrpInfoMap, altPriceGrpInfoMap);

							DbAdapter dbAdapter = new DbAdapter(mContext);
							dbAdapter.open();
							Utils.gDetlMap.clear();
							if(Global.gUOrderHInfo != null && Global.gUOrderHInfo.deliveryList != null)
							{
								for(UOrderDInfo info : Global.gUOrderHInfo.deliveryList)
								{
									ProdDetlInfo prodDetlInfo = dbAdapter.getProdDetl(info.strItemNo);
									if(prodDetlInfo != null)
									{
										Utils.gDetlMap.put(prodDetlInfo.strItemNo, Utils.getPropertyMap(prodDetlInfo));
									}
								}
							}
							dbAdapter.close();
							String strHoneywellFileName = "";
							if (nTemplateType == Global.CONSIGNMENT_DOCKET_PRINT) {
								strHoneywellFileName = Utils.getFinalPrintTemplateFileNameForHoneywell(mContext, Global.PRINT_CONSIGNMENT_DOCKET_FILENAME);
							}
							else {
								strHoneywellFileName = Utils.getFinalPrintTemplateFileNameForHoneywell(mContext, Global.PRINT_CONSIGNMENT_FILENAME);
							}
							mHoneywellTemplatePath = Global.ROOT_PRINT_TEMPLATE_LOCCATION + strHoneywellFileName;
							printEngine.createDocument(mContext, false, Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + mStrPdfFileName, nTemplateType);
							printEngine.createDocument(mContext,true, Global.PDF_LOCAL_DIR + mStrPdfFileName, nTemplateType);
						}
					}
					else
					{
						String strPDFFormat = Utils.getStringSetting(mContext, Global.PREF_UPLOAD_INV_FMT);
						String fmtFileName;
						int nPrintType;


						Date delDate = Utils.getDateFromString(Global.gUOrderHInfo.strDeliveryDate);
						Date cDate = new Date(System.currentTimeMillis());

						if (cDate.compareTo(delDate) < 0 /*&& !bReverseMode*/
						|| Global.gUOrderHInfo.futureOrderMethod != 0)
						{
							fmtFileName = Global.PRINT_TEMPLATE_ORDERACKFMT_FILENAME;
							nPrintType = Global.ORDER_PRINT;
							shortDescForFile = "ORDER";
							longDescForFile = "ORDER ACKNOWLEDGEMENT";
						}
						else
						{
							if( Global.gCurCustomerInfo.nShowPrice == 1 || Global.gCurCustomerInfo.nShowPrice == 0 )
							{
								fmtFileName = Global.PRINT_TEMPLATE_INVOICE_FMT_FILENAME;
								nPrintType = Global.SALE_PRINT;
								shortDescForFile = "INVOICE";
								longDescForFile = "TAX INVOICE";
							}
							else if( Global.gCurCustomerInfo.nShowPrice == 2 )
							{
								fmtFileName = Global.PRINT_TEMPLATE_DELIVERY_FMT_FILENAME;
								nPrintType = Global.SALE_DOCKET_PRINT;
								shortDescForFile = "DELIVERY";
								longDescForFile = "DELIVERY DOCKET";
							}
							else
							{
								fmtFileName = Global.PRINT_TEMPLATE_INVOICE_FMT_FILENAME;
								nPrintType = Global.SALE_PRINT;
								shortDescForFile = "INVOICE";
								longDescForFile = "TAX INVOICE";
							}
						}

						PrintEngine printEngine = new PrintEngine();
						mHoneywellPrintEngine = printEngine;

						PrintEngine printEngineForPrint = new PrintEngine();

						if(Utils.isFileExist(Global.ROOT_PRINT_TEMPLATE_LOCCATION + "/" + fmtFileName))
						{
							DbAdapter dbAdapter = new DbAdapter(mContext);
							dbAdapter.open();
							Utils.gDetlMap.clear();
							if(mPrintList != null)
							{
								for(UOrderDInfo info : mPrintList)
								{
									ProdDetlInfo prodDetlInfo = dbAdapter.getProdDetl(info.strItemNo);
									if(prodDetlInfo != null)
									{
										Utils.gDetlMap.put(prodDetlInfo.strItemNo, Utils.getPropertyMap(prodDetlInfo));
									}
								}
							}
							dbAdapter.close();
							EditText deliverTemp = mRootView.findViewById(R.id.editDeliverTemp);
							String strTemp = deliverTemp.getText().toString();

							printEngine.PreparePrint(mContext, Global.gCurCustomerInfo, mPrintList, mPayMethod, bReverseMode, m_bFutureOrder, priceGrpInfoMap, altPriceGrpInfoMap, mStrPhotoPathList, strTemp, bPartialPayment, fPartialPaidAmount);
							printEngineForPrint.PreparePrint(mContext, Global.gCurCustomerInfo, mPrintList, mPayMethod, bReverseMode, m_bFutureOrder, priceGrpInfoMap, altPriceGrpInfoMap, null, strTemp, bPartialPayment, fPartialPaidAmount);
						}

						mStrPrintPDFFileName = Global.PDF_LOCAL_DIR + "Print.pdf";
						if(Utils.isFileExist(mStrPrintPDFFileName))
						{
							Utils.fileDelete(mStrPrintPDFFileName);
						}
						if( strPDFFormat.equals("1") )
						{
							if(Global.gCurCustomerInfo.nShowPrice == 2 )
							{
								if(Utils.isFileExist(Global.ROOT_PRINT_TEMPLATE_LOCCATION + "/" + Global.PRINT_A4_DOCKET_FILENAME))
								{
									String strHoneywellFileName = Utils.getFinalPrintTemplateFileNameForHoneywell(mContext, Global.PRINT_TEMPLATE_DELIVERY_FMT_FILENAME);
									mHoneywellTemplatePath = Global.ROOT_PRINT_TEMPLATE_LOCCATION + strHoneywellFileName;
									printEngine.createDocument(mContext, false, Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + mStrPdfFileName, Global.A4_DOCKET_PRINT);
									printEngineForPrint.createDocument(mContext, false, mStrPrintPDFFileName, Global.A4_DOCKET_PRINT);
								}
								else
								{
									if(Utils.isFileExist(Global.ROOT_PRINT_TEMPLATE_LOCCATION + "/" + fmtFileName))
									{
										if(nPrintType == Global.ORDER_PRINT)
										{
											String strHoneywellFileName = Utils.getFinalPrintTemplateFileNameForHoneywell(mContext, Global.PRINT_TEMPLATE_ORDERACKFMT_FILENAME);
											mHoneywellTemplatePath = Global.ROOT_PRINT_TEMPLATE_LOCCATION + strHoneywellFileName;
										}
										else if(nPrintType == Global.SALE_PRINT)
										{
											String strHoneywellFileName = Utils.getFinalPrintTemplateFileNameForHoneywell(mContext, Global.PRINT_TEMPLATE_INVOICE_FMT_FILENAME);
											mHoneywellTemplatePath = Global.ROOT_PRINT_TEMPLATE_LOCCATION + strHoneywellFileName;
										}
										else if(nPrintType == Global.SALE_DOCKET_PRINT)
										{
											String strHoneywellFileName = Utils.getFinalPrintTemplateFileNameForHoneywell(mContext, Global.PRINT_TEMPLATE_DELIVERY_FMT_FILENAME);
											mHoneywellTemplatePath = Global.ROOT_PRINT_TEMPLATE_LOCCATION + strHoneywellFileName;
										}
										printEngine.createDocument(mContext, false, Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + mStrPdfFileName, nPrintType);
										printEngineForPrint.createDocument(mContext, false, mStrPrintPDFFileName, nPrintType);
									}
									else
									{
										createPDF(false, Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + mStrPdfFileName);
										Utils.fileCopy(Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + mStrPdfFileName, mStrPrintPDFFileName);
									}
								}
							}
							else
							{
								if(Utils.isFileExist(Global.ROOT_PRINT_TEMPLATE_LOCCATION + "/" + Global.PRINT_A4_INVOICE_FILENAME))
								{
									String strHoneywellFileName = Utils.getFinalPrintTemplateFileNameForHoneywell(mContext, Global.PRINT_TEMPLATE_INVOICE_FMT_FILENAME);
									mHoneywellTemplatePath = Global.ROOT_PRINT_TEMPLATE_LOCCATION + strHoneywellFileName;
									printEngine.createDocument(mContext, false, Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + mStrPdfFileName, Global.A4_INVOICE_PRINT);
									printEngineForPrint.createDocument(mContext, false, mStrPrintPDFFileName, Global.A4_INVOICE_PRINT);
								}
								else
								{
									if(Utils.isFileExist(Global.ROOT_PRINT_TEMPLATE_LOCCATION + "/" + fmtFileName))
									{
										if(nPrintType == Global.ORDER_PRINT)
										{
											String strHoneywellFileName = Utils.getFinalPrintTemplateFileNameForHoneywell(mContext, Global.PRINT_TEMPLATE_ORDERACKFMT_FILENAME);
											mHoneywellTemplatePath = Global.ROOT_PRINT_TEMPLATE_LOCCATION + strHoneywellFileName;
										}
										else if(nPrintType == Global.SALE_PRINT)
										{
											String strHoneywellFileName = Utils.getFinalPrintTemplateFileNameForHoneywell(mContext, Global.PRINT_TEMPLATE_INVOICE_FMT_FILENAME);
											mHoneywellTemplatePath = Global.ROOT_PRINT_TEMPLATE_LOCCATION + strHoneywellFileName;
										}
										else if(nPrintType == Global.SALE_DOCKET_PRINT)
										{
											String strHoneywellFileName = Utils.getFinalPrintTemplateFileNameForHoneywell(mContext, Global.PRINT_TEMPLATE_DELIVERY_FMT_FILENAME);
											mHoneywellTemplatePath = Global.ROOT_PRINT_TEMPLATE_LOCCATION + strHoneywellFileName;
										}
										printEngine.createDocument(mContext, true, Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + mStrPdfFileName, nPrintType);
										printEngineForPrint.createDocument(mContext, false, mStrPrintPDFFileName, nPrintType);
									}
									else
									{
										createPDF(false, Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + mStrPdfFileName);
										Utils.fileCopy(Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + mStrPdfFileName, mStrPrintPDFFileName);
									}
								}
							}

							if(Utils.isFileExist(Global.ROOT_PRINT_TEMPLATE_LOCCATION + "/" + fmtFileName))
							{
								printEngine.createDocument(mContext, true, Global.PDF_LOCAL_DIR + mStrPdfFileName, nPrintType);
							}else {
								createPDF(true, mStrPdfFileName);
							}


							mStrPdfFileName_= mStrPdfFileName.substring(0, mStrPdfFileName.length() - 4) + "_.pdf";
							if(Utils.isFileExist(Global.ROOT_PRINT_TEMPLATE_LOCCATION + "/" + fmtFileName))
							{
								printEngine.createDocument(mContext, false, Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + mStrPdfFileName_, nPrintType);
								printEngine.createDocument(mContext, true, Global.PDF_LOCAL_DIR + mStrPdfFileName_, nPrintType);
							}
							else
							{
								createPDF(true, mStrPdfFileName_);
								createPDF(false, mStrPdfFileName_);
							}
						}
						else
						{
							if(Utils.isFileExist(Global.ROOT_PRINT_TEMPLATE_LOCCATION + "/" + fmtFileName))
							{
								if(nPrintType == Global.ORDER_PRINT)
								{
									String strHoneywellFileName = Utils.getFinalPrintTemplateFileNameForHoneywell(mContext, Global.PRINT_TEMPLATE_ORDERACKFMT_FILENAME);
									mHoneywellTemplatePath = Global.ROOT_PRINT_TEMPLATE_LOCCATION + strHoneywellFileName;
								}
								else if(nPrintType == Global.SALE_PRINT)
								{
									String strHoneywellFileName = Utils.getFinalPrintTemplateFileNameForHoneywell(mContext, Global.PRINT_TEMPLATE_INVOICE_FMT_FILENAME);
									mHoneywellTemplatePath = Global.ROOT_PRINT_TEMPLATE_LOCCATION + strHoneywellFileName;
								}
								else if(nPrintType == Global.SALE_DOCKET_PRINT)
								{
									String strHoneywellFileName = Utils.getFinalPrintTemplateFileNameForHoneywell(mContext, Global.PRINT_TEMPLATE_DELIVERY_FMT_FILENAME);
									mHoneywellTemplatePath = Global.ROOT_PRINT_TEMPLATE_LOCCATION + strHoneywellFileName;
								}

								printEngine.createDocument(mContext, false, Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + mStrPdfFileName, nPrintType);
								printEngine.createDocument(mContext, true, Global.PDF_LOCAL_DIR + mStrPdfFileName, nPrintType);
								printEngineForPrint.createDocument(mContext, false, mStrPrintPDFFileName, nPrintType);
							}
							else
							{
								createPDF(true, mStrPdfFileName);
								createPDF(false, mStrPdfFileName);
								Utils.fileCopy(Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + mStrPdfFileName, mStrPrintPDFFileName);
							}
						}
					}

					//Remove Photo Files
					for (String strFilePath : mStrPhotoPathList) {
						Utils.fileDelete(strFilePath);
					}

					new Handler(Looper.getMainLooper()).post(() -> {
						try
						{
							hideProgressDialog();
						}catch(Exception e)
						{
							e.printStackTrace();
						}
					});

					long nTrxnNo = 0;
					nTrxnNo = System.currentTimeMillis();
					UTransactionsInfo uTransactionsInfo = setUTransactionsInfo(Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo, nTrxnNo, "FARC", mStrPdfFileName);
					if(TextUtils.isEmpty(mStrPdfFileName))
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
					if( mStrPdfFileName.length() > 4)
					{
						setUFileTransactionInfo(uTransactionsInfo.nTrxnNo, Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo, mPdfTrxnNo, mStrPdfFileName.substring(0, mStrPdfFileName.length() - 4), shortDescForFile, longDescForFile, uTransactionsInfo.strTrxnDate, uTransactionsInfo.strTrxnTime, mStrPdfFileName);
					}
					else
					{
						setUFileTransactionInfo(uTransactionsInfo.nTrxnNo, Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo, mPdfTrxnNo, mStrPdfFileName, shortDescForFile, longDescForFile, uTransactionsInfo.strTrxnDate, uTransactionsInfo.strTrxnTime, mStrPdfFileName);
					}

					//Utils.registerUploadFile(mContext, makeZipFileInfo);
					TaskPayment(mStrPdfFileName.substring(0,  mStrPdfFileName.length() - 4));

					if(bConsignment)
					{
						if(Utils.getStringSetting(mContext, Global.PREF_CON_EMPTY).equals("1"))
						{
							//Remove All Con Inventory from the shelf.
							doEmptyConsignmentShelf();
						}
						Utils.registerUploadFile(mContext, makeZipFileInfo);
						makeZipFileInfo = null;
					}
					else
					{
						Utils.registerUploadFileAsTemp(mContext, makeZipFileInfo);
					}
					isPdfCreated = true;
				}

				new Handler(Looper.getMainLooper()).post(() -> {
					try
					{
						if(Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 1)
						{
							if(mContext.isFinishing() || mContext.isDestroyed()) return;
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
									m_bConfirmClicked = false;
								}
							});
							msgDialog.show();
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
										/*
										For test
										Utils.printDebug(mContext, Str.SuccessToPrint[Global.LANG]);
										Global.gUOrderHInfo.nPrintedFlag = 1;
										DbAdapter dbAdapter = new DbAdapter(mContext);
										dbAdapter.open();
										dbAdapter.updateUOrderHInfo(Global.gUOrderHInfo);
										dbAdapter.close();
										m_bConfirmClicked = false;
										goBack();*/
										goBack();
										m_bConfirmClicked = false;
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
										m_bConfirmClicked = false;
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
										m_bConfirmClicked = false;
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
							m_bConfirmClicked = false;
						}
					}catch (Exception e)
					{
						e.printStackTrace();
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
						m_bConfirmClicked = false;
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
						m_bConfirmClicked = false;
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
						m_bConfirmClicked = false;
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
		String strPdfFilePath = "";
		if(bConsignment)
		{
			strPdfFilePath = Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + mStrPdfFileName;
		}
		else
		{
			if(!TextUtils.isEmpty(mStrPrintPDFFileName))
			{
				strPdfFilePath = mStrPrintPDFFileName;
			}
			else
			{
				String strPDFFormat = Utils.getStringSetting(mContext, Global.PREF_UPLOAD_INV_FMT);
				if( strPDFFormat.equals("1") )
				{
					strPdfFilePath = Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + mStrPdfFileName_;
				}
				else
				{
					strPdfFilePath = Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + mStrPdfFileName;
				}
			}

		}

		HoneywellPrinter.doPrint(mContext, new HoneywellPrinter.PrintCallback() {
			@Override
			public void printFinished() {
				hideProgressDialog();
				try {
					Utils.setStringSetting(mContext, Global.PREF_HONEYWELL_PRINTER_MAC_ADDRESS, mStrMacAddress);
					Utils.printDebug(mContext, Str.SuccessToPrint[Global.LANG]);
					Global.gUOrderHInfo.nPrintedFlag = 1;
					DbAdapter dbAdapter = new DbAdapter(mContext);
					dbAdapter.open();
					dbAdapter.updateUOrderHInfo(Global.gUOrderHInfo);
					dbAdapter.close();
					m_bConfirmClicked = false;
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
							m_bConfirmClicked = false;
						}
					});
					msgDialog.show();
				}catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}, mStrMacAddress, strPdfFilePath);
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
						Global.gUOrderHInfo.nPrintedFlag = 1;
						DbAdapter dbAdapter = new DbAdapter(mContext);
						dbAdapter.open();
						dbAdapter.updateUOrderHInfo(Global.gUOrderHInfo);
						dbAdapter.close();
						m_bConfirmClicked = false;
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
								m_bConfirmClicked = false;
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
			Global.gUOrderHInfo.nPrintedFlag = 1;
			DbAdapter dbAdapter = new DbAdapter(mContext);
			dbAdapter.open();
			dbAdapter.updateUOrderHInfo(Global.gUOrderHInfo);
			dbAdapter.close();
			m_bConfirmClicked = false;
			goBack();
		}

	}

	private void doBixolonPrintPdfTask()
	{
		showProgressDialog(Str.Printing[Global.LANG]);
		String strPdfFilePath = "";
		if(bConsignment)
		{
			strPdfFilePath = Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + mStrPdfFileName;
		}
		else
		{
			if(!TextUtils.isEmpty(mStrPrintPDFFileName))
			{
				strPdfFilePath = mStrPrintPDFFileName;
			}
			else
			{
				String strPDFFormat = Utils.getStringSetting(mContext, Global.PREF_UPLOAD_INV_FMT);
				if( strPDFFormat.equals("1") )
				{
					strPdfFilePath = Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + mStrPdfFileName_;
				}
				else
				{
					strPdfFilePath = Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + mStrPdfFileName;
				}
			}

		}

		BixolonPrinter.doPrint(mContext, new BixolonPrinter.PrintCallback() {
			@Override
			public void printFinished() {
				hideProgressDialog();
				try {
					Utils.setStringSetting(mContext, Global.PREF_BIXOLON_PRINTER_MAC_ADDRESS, mStrMacAddress);
					Utils.setStringSetting(mContext, Global.PREF_BIXOLON_PRINTER_FRIENDLY_NAME, mStrFriendlyName);
					Utils.printDebug(mContext, Str.SuccessToPrint[Global.LANG]);
					Global.gUOrderHInfo.nPrintedFlag = 1;
					DbAdapter dbAdapter = new DbAdapter(mContext);
					dbAdapter.open();
					dbAdapter.updateUOrderHInfo(Global.gUOrderHInfo);
					dbAdapter.close();
					m_bConfirmClicked = false;
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
							m_bConfirmClicked = false;
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

	public void processCardDetailCancel()
	{
		m_bConfirmClicked = false;
		((CheckBox) mRootView.findViewById(R.id.chkCheque)).setChecked(false);
		((CheckBox) mRootView.findViewById(R.id.chkCash)).setChecked(false);
		((CheckBox) mRootView.findViewById(R.id.chkCard)).setChecked(false);
		Global.gCurRealPaymentAmount = 0;
		Global.gUARPaymentCardInfo.nTrxnAmount = 0;
		mPayMethod = 0;
	}

	private void createA4PDF(boolean bDuplicate, String strPdfFileName){
		try {
			if (bDuplicate == false) {
				Utils.createDirectory(Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/");
			} else {
				Utils.createDirectory(Global.PDF_LOCAL_DIR);
			}
	
			List<UOrderDInfo> deliveryList = new ArrayList<>();
			List<UOrderDInfo> replaceList = new ArrayList<>();
			List<UOrderDInfo> containerList = new ArrayList<>();
			List<UOrderDInfo> buybackList = new ArrayList<>();
			for(UOrderDInfo info : mPrintList)
			{
				if((info.nSCWType == Global.INVENTORY_TRUCK || info.nSCWType == Global.WEIGHT_ITEM) &&
						(info.nTrxnType == Global.TRXN_DELIVER ||
								info.nTrxnType == Global.TRXN_FREE || info.nTrxnType == Global.TRXN_SAMPLE))
				{
					deliveryList.add(info);
					//Get FreeItem
					DbAdapter dbAdapter = new DbAdapter(mContext);
					dbAdapter.open();
					List<UOrderDInfo> uOrderDList = dbAdapter.getUOrderDList(info.nTrxnNo);
					dbAdapter.close();
					
					for(UOrderDInfo uOrderDInfo : uOrderDList)
					{
						boolean bExistFreeItem = false;
						for(UPromotionInfo uPromotionInfo : uOrderDInfo.uPromotionInfoList)
						{
							if( uPromotionInfo.nPromoType == Global.PROMO_TYPE_BUY_FREE_MULTI )
							{
								bExistFreeItem = true;
								break;
							}
						}
						if (bExistFreeItem && uOrderDInfo.strItemNo.equals(info.strItemNo)) {
							uOrderDInfo.nTrxnType = 12;
							uOrderDInfo.nQuantity = uOrderDInfo.nPromoDiscount == 0 ? 0 : Math.round(Utils.myDivider(uOrderDInfo.nQuantity, uOrderDInfo.nPromoDiscount));
							uOrderDInfo.nBasePrice = 0;
							uOrderDInfo.nRetailPrice = 0;
							uOrderDInfo.nTaxAmount = 0;
							uOrderDInfo.nPrice = 0;
							for(UTaxInfo uTaxInfo : uOrderDInfo.uTaxInfoList)
							{
								uTaxInfo.nTrxnNo = uOrderDInfo.nTrxnNo;
								uTaxInfo.nTrxnType = uOrderDInfo.nTrxnType;
							}
							deliveryList.add(uOrderDInfo);
						}
					}
				}
				else if ((info.nSCWType == Global.INVENTORY_TRUCK || info.nSCWType == Global.WEIGHT_ITEM) && info.nTrxnType == Global.TRXN_PICKUP)
					replaceList.add(info);
				else if ((info.nSCWType == Global.INVENTORY_TRUCK || info.nSCWType == Global.WEIGHT_ITEM) && info.nTrxnType == Global.TRXN_BUYBACK)
					buybackList.add(info);
				else if (info.nSCWType == Global.INVENTORY_CONTAINERS)
					containerList.add(info);
			}
			
			List<UOrderDInfo> cur_deliveryList = new ArrayList<>();
			List<UOrderDInfo> cur_replaceList = new ArrayList<>();
			List<UOrderDInfo> cur_buybackList = new ArrayList<>();
			List<UOrderDInfo> cur_containerList = new ArrayList<>();
			
			int nTotalRemainCnt = 0;
			
			if(deliveryList.size() > 0 )
				nTotalRemainCnt += deliveryList.size() + 2;
			if(replaceList.size() > 0 )
				nTotalRemainCnt += replaceList.size() + 2;
			if(buybackList.size() > 0 )
				nTotalRemainCnt += buybackList.size() + 2;
			if(containerList.size() > 0)
				nTotalRemainCnt += containerList.size() + 2;
			
			Document document = new Document(PageSize.A4, -50, -50, 10, 10);
			if (bDuplicate == false) {
//				PdfWriter.getInstance(document, new FileOutputStream(Global.PDF_DIR + mStrPdfFileName));
				PdfWriter.getInstance(document, new FileOutputStream(Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + strPdfFileName));
			} else {
//				PdfWriter.getInstance(document, new FileOutputStream(Global.PDF_LOCAL_DIR + mStrPdfFileName));
				PdfWriter.getInstance(document, new FileOutputStream(Global.PDF_LOCAL_DIR + strPdfFileName));
			}
			document.open();
			
			boolean bShowPaymentSection = false;
			if (mPayMethod == 0 || m_bFutureOrder)
				bShowPaymentSection = true;
			
			
			int nTotalHeight = bShowPaymentSection ? 280 : 400;
			int nShowCount = (int)(nTotalHeight / 15.5);
			if( nTotalRemainCnt <= nShowCount )
			{
				PdfPTable table = createA4Header(bDuplicate);
				document.add(table);							
				table = createA4Body(deliveryList, replaceList, buybackList, containerList, false, true);
				table.setSpacingBefore(5);
				document.add(table);

				table = createA4Footer();
				table.setSpacingBefore(5);
				document.add(table);
			}
			else
			{
				int nPageNo = 0;
				int nTotalHeightNew = 510;
				int nShowCountNew = (int)(nTotalHeightNew / 15.5);
				boolean bFooterPrint = false;
				while(nTotalRemainCnt > 0)
				{
					PdfPTable table = createA4Header(bDuplicate);
					document.add(table);			
					nPageNo++;
					if( nTotalRemainCnt < nShowCount )
					{
						table = createA4Body(deliveryList, replaceList, buybackList, containerList, false, true);
						bFooterPrint = true;
						table.setSpacingBefore(5);
						document.add(table);
						
						table = createA4Footer();
						table.setSpacingBefore(5);
						document.add(table);
						
						table = createA4PageNumber(nPageNo);
						table.setSpacingBefore(5);
						document.add(table);
						
						deliveryList.clear();
						replaceList.clear();
						buybackList.clear();
						containerList.clear();
						
					}
					else if (nTotalRemainCnt < nShowCountNew )
					{
						table = createA4Body(deliveryList, replaceList, buybackList, containerList, false, false);
						table.setSpacingBefore(5);
						document.add(table);
						table = new PdfPTable(1);
						table.setWidths(new int[]{1});
						table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
						Phrase phrase = new Phrase("continued on next page", fontBlack9Bold);
						PdfPCell cell = new PdfPCell(phrase);
						
						cell.setHorizontalAlignment(Element.ALIGN_CENTER);
						cell.setBorder(Rectangle.NO_BORDER);
						table.addCell(cell);
						document.add(table);
						
						table = new PdfPTable(1);
						table.setWidths(new int[]{1});
						table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
						phrase = new Phrase(" ", fontBlack9Bold);
						cell = new PdfPCell(phrase);
						
						cell.setHorizontalAlignment(Element.ALIGN_CENTER);
						cell.setBorder(Rectangle.NO_BORDER);
						table.addCell(cell);
						
						
						for(int i = 0; i < nShowCountNew - nTotalRemainCnt; i++)
						{
							document.add(table);
						}
						
						table = createA4PageNumber(nPageNo);
						table.setSpacingBefore(5);
						document.add(table);
						
						document.newPage();
						
						deliveryList.clear();
						replaceList.clear();
						buybackList.clear();
						containerList.clear();
					}
					else
					{
						cur_deliveryList.clear();
						cur_replaceList.clear();
						cur_buybackList.clear();
						cur_containerList.clear();
						int nCurCnt = 0;
						if(deliveryList.size() > 0 && nCurCnt < nShowCountNew)
						{
							nCurCnt += 2;
							for(UOrderDInfo info : deliveryList)
							{
								if(nCurCnt < nShowCountNew)
									cur_deliveryList.add(info);
								else
									break;
								nCurCnt++;
							}
							for(UOrderDInfo info : cur_deliveryList)
							{
								deliveryList.remove(info);
							}
						}
						if(replaceList.size() > 0 && nCurCnt < nShowCountNew)
						{
							nCurCnt += 2;
							for(UOrderDInfo info : replaceList)
							{
								if(nCurCnt < nShowCountNew)
									cur_replaceList.add(info);
								else
									break;
								nCurCnt++;
							}
							for(UOrderDInfo info : cur_replaceList)
							{
								replaceList.remove(info);
							}
						}
						if(buybackList.size() > 0 && nCurCnt < nShowCountNew)
						{
							nCurCnt += 2;
							for(UOrderDInfo info : buybackList)
							{
								if(nCurCnt < nShowCountNew)
									cur_buybackList.add(info);
								else
									break;
								nCurCnt++;
							}
							for(UOrderDInfo info : cur_buybackList)
							{
								buybackList.remove(info);
							}
						}
						if(containerList.size() > 0 && nCurCnt < nShowCountNew)
						{
							nCurCnt += 2;
							for(UOrderDInfo info : containerList)
							{
								if(nCurCnt < nShowCountNew)
									cur_containerList.add(info);
								else
									break;
								nCurCnt++;
							}
							for(UOrderDInfo info : containerList)
							{
								containerList.remove(info);
							}
						}
						
						table = createA4Body(cur_deliveryList, cur_replaceList, cur_buybackList, cur_containerList, true, false);
						table.setSpacingBefore(5);
						document.add(table);
						
						
						table = new PdfPTable(1);
						table.setWidths(new int[]{1});
						table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
						Phrase phrase = new Phrase("continued on next page", fontBlack9Bold);
						PdfPCell cell = new PdfPCell(phrase);
						
						cell.setHorizontalAlignment(Element.ALIGN_CENTER);
						cell.setBorder(Rectangle.NO_BORDER);
						table.addCell(cell);
						document.add(table);
						
						table = new PdfPTable(1);
						table.setWidths(new int[]{1});
						table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
						
						phrase = new Phrase(" ", fontBlack9Bold);
						cell = new PdfPCell(phrase);
						
						cell.setHorizontalAlignment(Element.ALIGN_CENTER);
						cell.setBorder(Rectangle.NO_BORDER);
						table.addCell(cell);
						
						nTotalRemainCnt = 0;
						if( cur_deliveryList.size() > 0 )
							nTotalRemainCnt += cur_deliveryList.size() + 2;
						if( cur_replaceList.size() > 0 )
							nTotalRemainCnt += cur_replaceList.size() + 2;
						if( cur_buybackList.size() > 0 )
							nTotalRemainCnt += cur_buybackList.size() + 2;
						if( cur_containerList.size() > 0 )
							nTotalRemainCnt += cur_containerList.size() + 2;
						
						
						for(int i = 0; i < nShowCountNew - nTotalRemainCnt; i++)
						{
							document.add(table);
						}
						
						table = createA4PageNumber(nPageNo);
						table.setSpacingBefore(5);
						document.add(table);
						document.newPage();
					}
					
					nTotalRemainCnt = 0;
					if(deliveryList.size() > 0 )
						nTotalRemainCnt += deliveryList.size() + 2;
					if(replaceList.size() > 0 )
						nTotalRemainCnt += replaceList.size() + 2;
					if(buybackList.size() > 0 )
						nTotalRemainCnt += buybackList.size() + 2;
					if(containerList.size() > 0)
						nTotalRemainCnt += containerList.size() + 2;
				}
				if(!bFooterPrint)
				{
					PdfPTable table = createA4Header(bDuplicate);
					document.add(table);			
					nPageNo++;
					table = createA4Body(deliveryList, replaceList, buybackList, containerList, false, true);
					bFooterPrint = true;
					table.setSpacingBefore(5);
					document.add(table);
					
					table = createA4Footer();
					table.setSpacingBefore(5);
					document.add(table);
					
					table = createA4PageNumber(nPageNo);
					table.setSpacingBefore(5);
					document.add(table);
				}
			}
			document.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void createA4DocketPDF(boolean bDuplicate, String strPdfFileName){
		try {
			if (bDuplicate == false) {
				Utils.createDirectory(Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/");
			} else {
				Utils.createDirectory(Global.PDF_LOCAL_DIR);
			}

			List<UOrderDInfo> deliveryList = new ArrayList<>();
			List<UOrderDInfo> replaceList = new ArrayList<>();
			List<UOrderDInfo> buybackList = new ArrayList<>();
			List<UOrderDInfo> containerDeliveredList = new ArrayList<>();
			List<UOrderDInfo> containerPickedList = new ArrayList<>();
			for(UOrderDInfo info : mPrintList)
			{
				if((info.nSCWType == Global.INVENTORY_TRUCK || info.nSCWType == Global.WEIGHT_ITEM) && (
						info.nTrxnType == Global.TRXN_DELIVER ||
								info.nTrxnType == Global.TRXN_FREE || info.nTrxnType == Global.TRXN_SAMPLE))
				{
					deliveryList.add(info);
					//Get FreeItem
					DbAdapter dbAdapter = new DbAdapter(mContext);
					dbAdapter.open();
					List<UOrderDInfo> uOrderDList = dbAdapter.getUOrderDList(info.nTrxnNo);
					dbAdapter.close();

					for(UOrderDInfo uOrderDInfo : uOrderDList)
					{
						if (uOrderDInfo.nPromoType == Global.PROMO_TYPE_BUY_FREE_MULTI && uOrderDInfo.strItemNo.equals(info.strItemNo)) {
							uOrderDInfo.nTrxnType = 12;
							uOrderDInfo.nQuantity = uOrderDInfo.nPromoDiscount == 0 ? 0 : Math.round(Utils.myDivider(uOrderDInfo.nQuantity, uOrderDInfo.nPromoDiscount));
							uOrderDInfo.nBasePrice = 0;
							uOrderDInfo.nRetailPrice = 0;
							uOrderDInfo.nTaxAmount = 0;
							uOrderDInfo.nPrice = 0;
							for(UTaxInfo uTaxInfo : uOrderDInfo.uTaxInfoList)
							{
								uTaxInfo.nTrxnNo = uOrderDInfo.nTrxnNo;
								uTaxInfo.nTrxnType = uOrderDInfo.nTrxnType;
							}
							deliveryList.add(uOrderDInfo);
						}
					}
				}
				else if ((info.nSCWType == Global.INVENTORY_TRUCK || info.nSCWType == Global.WEIGHT_ITEM) && info.nTrxnType == Global.TRXN_PICKUP)
					replaceList.add(info);
				else if ((info.nSCWType == Global.INVENTORY_TRUCK || info.nSCWType == Global.WEIGHT_ITEM) && info.nTrxnType == Global.TRXN_BUYBACK)
					buybackList.add(info);
				else if (info.nSCWType == Global.INVENTORY_CONTAINERS) {
					if( info.nTrxnType == Global.TRXN_CONTAINER_SALE)
					{
						containerDeliveredList.add(info);
					}
					else
					{
						containerPickedList.add(info);
					}
				}
			}

			List<UOrderDInfo> cur_deliveryList = new ArrayList<>();
			List<UOrderDInfo> cur_replaceList = new ArrayList<>();
			List<UOrderDInfo> cur_buybackList = new ArrayList<>();
			List<UOrderDInfo> cur_containerDeliveredList = new ArrayList<>();
			List<UOrderDInfo> cur_containerPickedupList = new ArrayList<>();

			int nTotalRemainCnt = 0;

			if(deliveryList.size() > 0 )
				nTotalRemainCnt += deliveryList.size() + 2;
			if(replaceList.size() > 0 )
				nTotalRemainCnt += replaceList.size() + 2;
			if(buybackList.size() > 0 )
				nTotalRemainCnt += buybackList.size() + 2;
			if(containerDeliveredList.size() > 0)
				nTotalRemainCnt += containerDeliveredList.size() + 2;
			if(containerPickedList.size() > 0)
				nTotalRemainCnt += containerPickedList.size() + 2;

			Document document = new Document(PageSize.A4, -50, -50, 20, 10);
			if (bDuplicate == false) {
//				PdfWriter.getInstance(document, new FileOutputStream(Global.PDF_DIR + mStrPdfFileName));
				PdfWriter.getInstance(document, new FileOutputStream(Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + strPdfFileName));
			} else {
//				PdfWriter.getInstance(document, new FileOutputStream(Global.PDF_LOCAL_DIR + mStrPdfFileName));
				PdfWriter.getInstance(document, new FileOutputStream(Global.PDF_LOCAL_DIR + strPdfFileName));
			}
			document.open();

			boolean bShowPaymentSection = false;
			if (mPayMethod == 0 || m_bFutureOrder)
				bShowPaymentSection = true;


			int nTotalHeight = 360;
			int nShowCount = (int)(nTotalHeight / 15.5);
			if( nTotalRemainCnt <= nShowCount )
			{
				PdfPTable table = createA4DocketHeader(bDuplicate);
				document.add(table);
				table = createA4DocketBody(deliveryList, replaceList, buybackList, containerDeliveredList, containerPickedList, false, true);
				table.setSpacingBefore(5);
				document.add(table);

				table = createA4DocketFooter();
				table.setSpacingBefore(5);
				document.add(table);
			}
			else
			{
				int nPageNo = 0;
				int nTotalHeightNew = 510;
				int nShowCountNew = (int)(nTotalHeightNew / 15.5);
				boolean bFooterPrint = false;
				while(nTotalRemainCnt > 0)
				{
					PdfPTable table = createA4DocketHeader(bDuplicate);
					document.add(table);
					nPageNo++;
					if( nTotalRemainCnt < nShowCount )
					{
						table = createA4DocketBody(deliveryList, replaceList, buybackList, containerDeliveredList, containerPickedList, false, true);
						bFooterPrint = true;
						table.setSpacingBefore(5);
						document.add(table);

						table = createA4DocketFooter();
						table.setSpacingBefore(5);
						document.add(table);

						table = createA4DocketPageNumber(nPageNo);
						table.setSpacingBefore(5);
						document.add(table);

						deliveryList.clear();
						replaceList.clear();
						buybackList.clear();
						containerDeliveredList.clear();
						containerPickedList.clear();

					}
					else if (nTotalRemainCnt < nShowCountNew )
					{
						table = createA4DocketBody(deliveryList, replaceList, buybackList, containerDeliveredList, containerPickedList, false, false);
						table.setSpacingBefore(5);
						document.add(table);
						table = new PdfPTable(1);
						table.setWidths(new int[]{1});
						table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
						Phrase phrase = new Phrase("continued on next page", fontBlack9Bold);
						PdfPCell cell = new PdfPCell(phrase);

						cell.setHorizontalAlignment(Element.ALIGN_CENTER);
						cell.setBorder(Rectangle.NO_BORDER);
						table.addCell(cell);
						document.add(table);

						table = new PdfPTable(1);
						table.setWidths(new int[]{1});
						table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
						phrase = new Phrase(" ", fontBlack9Bold);
						cell = new PdfPCell(phrase);

						cell.setHorizontalAlignment(Element.ALIGN_CENTER);
						cell.setBorder(Rectangle.NO_BORDER);
						table.addCell(cell);


						for(int i = 0; i < nShowCountNew - nTotalRemainCnt; i++)
						{
							document.add(table);
						}

						table = createA4DocketPageNumber(nPageNo);
						table.setSpacingBefore(5);
						document.add(table);

						document.newPage();

						deliveryList.clear();
						replaceList.clear();
						buybackList.clear();
						containerDeliveredList.clear();
						containerPickedList.clear();
					}
					else
					{
						cur_deliveryList.clear();
						cur_replaceList.clear();
						cur_buybackList.clear();
						cur_containerDeliveredList.clear();
						cur_containerPickedupList.clear();
						int nCurCnt = 0;
						if(deliveryList.size() > 0 && nCurCnt < nShowCountNew)
						{
							nCurCnt += 2;
							for(UOrderDInfo info : deliveryList)
							{
								if(nCurCnt < nShowCountNew)
									cur_deliveryList.add(info);
								else
									break;
								nCurCnt++;
							}
							for(UOrderDInfo info : cur_deliveryList)
							{
								deliveryList.remove(info);
							}
						}
						if(replaceList.size() > 0 && nCurCnt < nShowCountNew)
						{
							nCurCnt += 2;
							for(UOrderDInfo info : replaceList)
							{
								if(nCurCnt < nShowCountNew)
									cur_replaceList.add(info);
								else
									break;
								nCurCnt++;
							}
							for(UOrderDInfo info : cur_replaceList)
							{
								replaceList.remove(info);
							}
						}
						if(buybackList.size() > 0 && nCurCnt < nShowCountNew)
						{
							nCurCnt += 2;
							for(UOrderDInfo info : buybackList)
							{
								if(nCurCnt < nShowCountNew)
									cur_buybackList.add(info);
								else
									break;
								nCurCnt++;
							}
							for(UOrderDInfo info : cur_buybackList)
							{
								buybackList.remove(info);
							}
						}
						if(containerDeliveredList.size() > 0 && nCurCnt < nShowCountNew)
						{
							nCurCnt += 2;
							for(UOrderDInfo info : containerDeliveredList)
							{
								if(nCurCnt < nShowCountNew)
									cur_containerDeliveredList.add(info);
								else
									break;
								nCurCnt++;
							}
							for(UOrderDInfo info : containerDeliveredList)
							{
								containerDeliveredList.remove(info);
							}
						}
						if(cur_containerPickedupList.size() > 0 && nCurCnt < nShowCountNew)
						{
							nCurCnt += 2;
							for(UOrderDInfo info : cur_containerPickedupList)
							{
								if(nCurCnt < nShowCountNew)
									cur_containerPickedupList.add(info);
								else
									break;
								nCurCnt++;
							}
							for(UOrderDInfo info : cur_containerPickedupList)
							{
								cur_containerPickedupList.remove(info);
							}
						}

						table = createA4DocketBody(cur_deliveryList, cur_replaceList, cur_buybackList, cur_containerDeliveredList, cur_containerPickedupList, true, false);
						table.setSpacingBefore(5);
						document.add(table);


						table = new PdfPTable(1);
						table.setWidths(new int[]{1});
						table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
						Phrase phrase = new Phrase("continued on next page", fontBlack9Bold);
						PdfPCell cell = new PdfPCell(phrase);

						cell.setHorizontalAlignment(Element.ALIGN_CENTER);
						cell.setBorder(Rectangle.NO_BORDER);
						table.addCell(cell);
						document.add(table);

						table = new PdfPTable(1);
						table.setWidths(new int[]{1});
						table.getDefaultCell().setBorder(Rectangle.NO_BORDER);

						phrase = new Phrase(" ", fontBlack9Bold);
						cell = new PdfPCell(phrase);

						cell.setHorizontalAlignment(Element.ALIGN_CENTER);
						cell.setBorder(Rectangle.NO_BORDER);
						table.addCell(cell);

						nTotalRemainCnt = 0;
						if( cur_deliveryList.size() > 0 )
							nTotalRemainCnt += cur_deliveryList.size() + 2;
						if( cur_replaceList.size() > 0 )
							nTotalRemainCnt += cur_replaceList.size() + 2;
						if( cur_buybackList.size() > 0 )
							nTotalRemainCnt += cur_buybackList.size() + 2;
						if( cur_containerDeliveredList.size() > 0 )
							nTotalRemainCnt += cur_containerDeliveredList.size() + 2;
						if( cur_containerPickedupList.size() > 0 )
							nTotalRemainCnt += cur_containerPickedupList.size() + 2;


						for(int i = 0; i < nShowCountNew - nTotalRemainCnt; i++)
						{
							document.add(table);
						}

						table = createA4DocketPageNumber(nPageNo);
						table.setSpacingBefore(5);
						document.add(table);
						document.newPage();
					}

					nTotalRemainCnt = 0;
					if(deliveryList.size() > 0 )
						nTotalRemainCnt += deliveryList.size() + 2;
					if(replaceList.size() > 0 )
						nTotalRemainCnt += replaceList.size() + 2;
					if(buybackList.size() > 0 )
						nTotalRemainCnt += buybackList.size() + 2;
					if(containerDeliveredList.size() > 0)
						nTotalRemainCnt += containerDeliveredList.size() + 2;
					if(cur_containerPickedupList.size() > 0)
						nTotalRemainCnt += cur_containerPickedupList.size() + 2;
				}
				if(!bFooterPrint)
				{
					PdfPTable table = createA4DocketHeader(bDuplicate);
					document.add(table);
					nPageNo++;
					table = createA4DocketBody(deliveryList, replaceList, buybackList, containerDeliveredList, containerPickedList, false, true);
					bFooterPrint = true;
					table.setSpacingBefore(5);
					document.add(table);

					table = createA4DocketFooter();
					table.setSpacingBefore(5);
					document.add(table);

					table = createA4DocketPageNumber(nPageNo);
					table.setSpacingBefore(5);
					document.add(table);
				}
			}
			document.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private PdfPTable createA4DocketBodyFooter()
	{
		PdfPTable table = new PdfPTable(2);
		PdfPCell cell;
		Phrase phrase;
		try{
			table.setWidths(new int[]{5, 6});
			table.getDefaultCell().setBorder(Rectangle.NO_BORDER);

			PdfPTable table_sub = new PdfPTable(1);
			table_sub.setWidths(new int[]{1});

			if (Global.gUOrderHInfo.strSignatureFilePath.length() > 0 && Utils.isFileExist(Global.gUOrderHInfo.strSignatureFilePath) == true) {
				Image image = Image.getInstance(Global.gUOrderHInfo.strSignatureFilePath);
				image.scaleToFit(60, 60);
				cell = new PdfPCell(image);
				cell.setBorder(Rectangle.NO_BORDER);
				cell.setPaddingTop(5);
				cell.setPaddingBottom(5);
			} else {
				phrase = new Phrase("No Signature File", fontBlack10Bold);
				cell = new PdfPCell(phrase);
				cell.setPaddingTop(30);
				cell.setPaddingBottom(30);
				cell.setBorder(Rectangle.NO_BORDER);
			}

			cell.setHorizontalAlignment(Element.ALIGN_LEFT);
			cell.setVerticalAlignment(Element.ALIGN_CENTER);
			cell.setBorder(Rectangle.NO_BORDER);
			table_sub.addCell(cell);

			phrase = new Phrase("Received by: " + Global.gUOrderHInfo.strOrderName, fontBlack9Bold);
			cell = new PdfPCell(phrase);
			cell.setHorizontalAlignment(Element.ALIGN_LEFT);

			cell.setBorder(Rectangle.NO_BORDER);
			table_sub.addCell(cell);
			cell = new PdfPCell(table_sub);
			cell.setPaddingLeft(20);
			cell.setPaddingBottom(5);
			table.addCell(cell);

			table_sub = new PdfPTable(2);
			table_sub.setWidths(new int[]{1,1});
			table_sub.getDefaultCell().setBorder(Rectangle.NO_BORDER);

			int nUnit = 0;
			int nQty = 0;
			double dValue = 0;
			for(UOrderDInfo uOrderDInfo : Global.gUOrderHInfo.deliveryList)
			{
				if(Utils.getProdLoconSCWType(mContext, uOrderDInfo.strItemNo) == Global.WEIGHT_ITEM)
				{
					dValue += uOrderDInfo.dSales;
				}
				else
				{
					if(bShowCase)
					{
						if( Global.gCurCustomerInfo.strSalEntryMode.equals("B") )
						{
							int nCase = getCaseValue(uOrderDInfo.strItemNo);
							nUnit += uOrderDInfo.nSales / nCase;
							nQty += uOrderDInfo.nSales % nCase;
						}
						else
						{
							nQty += uOrderDInfo.nSales;
						}
					}
					else
					{
						nQty += uOrderDInfo.nSales;
					}

				}
			}

			for(UOrderDInfo uOrderDInfo : Global.gUOrderHInfo.pickupList)
			{
				if(Utils.getProdLoconSCWType(mContext, uOrderDInfo.strItemNo) == Global.WEIGHT_ITEM)
				{
					dValue -= uOrderDInfo.dDumps;
				}
				else
				{
					if(bShowCase)
					{
						if( Global.gCurCustomerInfo.strSalEntryMode.equals("B") )
						{
							int nCase = getCaseValue(uOrderDInfo.strItemNo);
							nUnit -= uOrderDInfo.nDumps / nCase;
							nQty -= uOrderDInfo.nDumps % nCase;
						}
						else
						{
							nQty -= uOrderDInfo.nDumps;
						}
					}
					else
					{
						nQty -= uOrderDInfo.nDumps;
					}
				}
			}

			phrase = new Phrase("Total Quantity", fontBlack9Normal);
			cell = new PdfPCell(phrase);
			cell.setBorder(Rectangle.BOTTOM);
			table_sub.addCell(cell);
			if(Global.gCurCustomerInfo.strSalEntryMode.equals("B"))
			{
				if(dValue != 0)
				{
					phrase = new Phrase(String.format(Locale.ENGLISH, "%d/%d\n%.2f", nUnit, nQty, dValue), fontBlack9Normal);
				}
				else
				{
					phrase = new Phrase(String.format(Locale.ENGLISH, "%d/%d", nUnit, nQty), fontBlack9Normal);
				}
			}
			else
			{
				if(dValue != 0)
				{
					phrase = new Phrase(String.format(Locale.ENGLISH, "%d\n%.2f", nQty, dValue), fontBlack9Normal);
				}
				else
				{
					phrase = new Phrase(String.format(Locale.ENGLISH, "%d", nQty), fontBlack9Normal);
				}
			}
			cell = new PdfPCell(phrase);
			cell.setBorder(Rectangle.BOTTOM);
			table_sub.addCell(cell);

			cell = new PdfPCell(table_sub);
			cell.setBorder(Rectangle.NO_BORDER);
			cell.setPaddingBottom(3);
			cell.setPaddingLeft(50);
			cell.setPaddingRight(50);
			table.addCell(cell);


		}catch(Exception e)
		{
			e.printStackTrace();
		}
		return table;
	}

	private PdfPTable createA4DocketBody(List<UOrderDInfo> deliverItem, List<UOrderDInfo> replaceItem,
										 List<UOrderDInfo> buybackItem, List<UOrderDInfo> containerDeliveryItems,
										 List<UOrderDInfo> containerReturnItems,boolean bContinous, boolean bShowFooter)
	{
		PdfPTable tableAll = new PdfPTable(1);
		PdfPTable table = new PdfPTable(1);
		PdfPCell cell;
		Phrase phrase;
		try{
			table.setWidths(new int[]{1});
			tableAll.setWidths(new int[]{1});
			table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
			float fItemHeight = 0f;
			if(deliverItem != null && deliverItem.size() > 0)
			{
				phrase = new Phrase("DELIVERED", fontBlack9Bold);
				cell = new PdfPCell(phrase);
				cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
				cell.setHorizontalAlignment(Element.ALIGN_LEFT);
				cell.setPadding(3);
				table.addCell(cell);
				fItemHeight += 15.5;

				cell = new PdfPCell(createA4DocketItem(deliverItem));
				table.addCell(cell);
				fItemHeight += 15.5 * (deliverItem.size() + 1);
			}

			if(replaceItem != null && replaceItem.size() > 0)
			{
				phrase = new Phrase("RETURNED", fontBlack9Bold);
				cell = new PdfPCell(phrase);
				cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
				cell.setHorizontalAlignment(Element.ALIGN_LEFT);
				cell.setPadding(3);
				table.addCell(cell);
				fItemHeight += 15.5;

				cell = new PdfPCell(createA4DocketItem(replaceItem));
				table.addCell(cell);
				fItemHeight += 15.5 * (replaceItem.size() + 1);
			}
			if(buybackItem != null && buybackItem.size() > 0)
			{
				phrase = new Phrase("BUYBACKS", fontBlack9Bold);
				cell = new PdfPCell(phrase);
				cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
				cell.setHorizontalAlignment(Element.ALIGN_LEFT);
				cell.setPadding(3);
				table.addCell(cell);
				fItemHeight += 15.5;

				cell = new PdfPCell(createA4DocketItem(buybackItem));
				table.addCell(cell);
				fItemHeight += 15.5 * (buybackItem.size() + 1);
			}



			if(containerDeliveryItems != null && containerDeliveryItems.size() > 0)
			{
				phrase = new Phrase("RETURNABLE CONTAINERS DELIVERED", fontBlack9Bold);
				cell = new PdfPCell(phrase);
				cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
				cell.setHorizontalAlignment(Element.ALIGN_LEFT);
				cell.setPadding(3);
				table.addCell(cell);
				fItemHeight += 15.5;

				cell = new PdfPCell(createA4DocketItem(containerDeliveryItems));
				table.addCell(cell);
				fItemHeight += 15.5 * (containerDeliveryItems.size() + 1);
			}
			if(containerReturnItems != null && containerReturnItems.size() > 0)
			{
				phrase = new Phrase("RETURNABLE CONTAINERS RETURNED", fontBlack9Bold);
				cell = new PdfPCell(phrase);
				cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
				cell.setHorizontalAlignment(Element.ALIGN_LEFT);
				cell.setPadding(3);
				table.addCell(cell);
				fItemHeight += 15.5;

				cell = new PdfPCell(createA4DocketItem(containerReturnItems));
				table.addCell(cell);
				fItemHeight += 15.5 * (containerReturnItems.size() + 1);
			}


			if(bShowFooter)
			{
				cell = new PdfPCell(createA4DocketBodyFooter());
				boolean bShowPaymentSection = false;
				if (mPayMethod == 0 || m_bFutureOrder)
					bShowPaymentSection = true;
//				boolean bShowPaymentSection = true;

				int nTotalHeight = 360;

				cell.setPaddingTop(nTotalHeight - fItemHeight);
				cell.setBorder(Rectangle.NO_BORDER);
				table.addCell(cell);
			}
			cell = new PdfPCell(table);
			tableAll.addCell(cell);

		}catch(Exception e)
		{
			e.printStackTrace();
		}
		return tableAll;
	}

	private PdfPTable createA4DocketItem(List<UOrderDInfo> item)
	{
		PdfPTable table = new PdfPTable(3);
		PdfPCell cell;
		Phrase phrase;
		try{
			table.setWidths(new int[]{3, 12, 3});

			phrase = new Phrase("Code", fontBlack9Bold);
			cell = new PdfPCell(phrase);
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell.setPadding(3);
			table.addCell(cell);

			phrase = new Phrase("Description", fontBlack9Bold);
			cell = new PdfPCell(phrase);
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell.setPadding(3);
			table.addCell(cell);

			phrase = new Phrase("Quantity", fontBlack9Bold);
			cell = new PdfPCell(phrase);
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell.setPadding(3);
			table.addCell(cell);


			for(UOrderDInfo info : item)
			{
				if(info.nTrxnType == Global.TRXN_FREE)
				{
					phrase = new Phrase(" ", fontBlack9Normal);
					cell = new PdfPCell(phrase);
					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					cell.setPadding(3);
					table.addCell(cell);

					phrase = new Phrase("Free Item", fontBlack9Normal);
					cell = new PdfPCell(phrase);
					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					cell.setPadding(3);
					table.addCell(cell);

					phrase = new Phrase(String.valueOf(info.nQuantity), fontBlack9Normal);
					cell = new PdfPCell(phrase);
					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					cell.setPadding(3);
					table.addCell(cell);
				}
				else if(info.nSCWType == Global.INVENTORY_CONTAINERS)
				{
					phrase = new Phrase(info.strItemNo.length() > 0 ? info.strItemNo : " ", fontBlack9Normal);
					cell = new PdfPCell(phrase);
					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					cell.setPadding(3);
					table.addCell(cell);

					phrase = new Phrase(info.strDescription.length() > 0 ? info.strDescription : " ", fontBlack9Normal);
					cell = new PdfPCell(phrase);
					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					cell.setPadding(3);
					table.addCell(cell);

					if( info.nTrxnType == Global.TRXN_CONTAINER_DUMP)
					{
						phrase = new Phrase(String.valueOf(-1 * info.nQuantity), fontBlack9Normal);
					}
					else
					{
						phrase = new Phrase(String.valueOf(info.nQuantity), fontBlack9Normal);
					}
					cell = new PdfPCell(phrase);
					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					cell.setPadding(3);
					table.addCell(cell);
				}
				else if(info.nTrxnType == Global.TRXN_PICKUP || info.nTrxnType == Global.TRXN_BUYBACK)
				{

					phrase = new Phrase(info.strItemNo.length() > 0 ? info.strItemNo : " ", fontBlack9Normal);
					cell = new PdfPCell(phrase);
					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					cell.setPadding(3);
					table.addCell(cell);

					phrase = new Phrase(info.strDescription.length() > 0 ? info.strDescription : " ", fontBlack9Normal);
					cell = new PdfPCell(phrase);
					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					cell.setPadding(3);
					table.addCell(cell);

					if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
					{
						phrase = new Phrase(String.format(Locale.ENGLISH, "%.2f", info.dQuantity), fontBlack9Normal);
					}
					else
					{
						if(bShowCase)
						{
							if( Global.gCurCustomerInfo.strSalEntryMode.equals("B") )
							{
								if (Utils.isContainerItem(mContext, info.strItemNo)) {
									phrase = new Phrase(String.format(Locale.ENGLISH, "%d", info.nDumps), fontBlack9Normal);
								}
								else {
									int nCase = getCaseValue(info.strItemNo);
									int nQty =  info.nDumps;
									int nCaseValue = nQty / nCase;
									int nQtyValue = nQty % nCase;
									phrase = new Phrase(String.format(Locale.ENGLISH, "%d/%d", nCaseValue, nQtyValue), fontBlack9Normal);
								}
							}
							else
							{
								phrase = new Phrase(String.valueOf(info.nQuantity), fontBlack9Normal);
							}
						}
						else
						{
							phrase = new Phrase(String.valueOf(info.nQuantity), fontBlack9Normal);
						}
					}

					cell = new PdfPCell(phrase);
					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					cell.setPadding(3);
					table.addCell(cell);
				}
				else
				{
					phrase = new Phrase(info.strItemNo.length() > 0 ? info.strItemNo : " ", fontBlack9Normal);
					cell = new PdfPCell(phrase);
					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					cell.setPadding(3);
					table.addCell(cell);

					phrase = new Phrase(info.strDescription.length() > 0 ? info.strDescription : " ", fontBlack9Normal);
					cell = new PdfPCell(phrase);
					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					cell.setPadding(3);
					table.addCell(cell);
					if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
					{
						phrase = new Phrase(String.format(Locale.ENGLISH, "%.2f", info.dQuantity), fontBlack9Normal);
					}
					else
					{
						if(bShowCase)
						{
							if( Global.gCurCustomerInfo.strSalEntryMode.equals("B") )
							{
								if (Utils.isContainerItem(mContext, info.strItemNo)) {
									phrase = new Phrase(String.format(Locale.ENGLISH, "%d", info.nSales), fontBlack9Normal);
								}
								else {
									int nCase = getCaseValue(info.strItemNo);
									int nQty =  info.nSales;
									int nCaseValue = nQty / nCase;
									int nQtyValue = nQty % nCase;
									phrase = new Phrase(String.format(Locale.ENGLISH, "%d/%d", nCaseValue, nQtyValue), fontBlack9Normal);
								}
							}
							else
							{
								phrase = new Phrase(String.valueOf(info.nQuantity), fontBlack9Normal);
							}
						}
						else
						{
							phrase = new Phrase(String.valueOf(info.nQuantity), fontBlack9Normal);
						}
					}
					cell = new PdfPCell(phrase);
					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					cell.setPadding(3);
					table.addCell(cell);
				}

			}
		}catch(Exception e)
		{
			e.printStackTrace();
		}

		return table;
	}

	private PdfPTable createA4DocketHeader(boolean bDuplicate){
		PdfPTable table = new PdfPTable(3);
		PdfPCell cell;
		Phrase phrase;
		try {
			table.setWidths(new int[]{1, 1, 1});
			table.getDefaultCell().setBorder(Rectangle.NO_BORDER);

			PdfPTable table_sub = new PdfPTable(1);
			table_sub.setWidths(new int[]{1});
			table_sub.getDefaultCell().setBorder(Rectangle.NO_BORDER);

			if (Utils.isFileExist(Global.DEFAULT_COMPANY_LOGO_FILEPATH) == true) {
				Image image = Image.getInstance(Global.DEFAULT_COMPANY_LOGO_FILEPATH);
				image.scaleToFit(80, 80);
				cell = new PdfPCell(image);
				cell.setHorizontalAlignment(Element.ALIGN_LEFT);
			} else {
				phrase = new Phrase("No Logo File", fontBlack10Bold);
				cell = new PdfPCell(phrase);
				cell.setHorizontalAlignment(Element.ALIGN_LEFT);
				cell.setVerticalAlignment(Element.ALIGN_CENTER);
			}
			cell.setPaddingTop(10);
			cell.setBorder(Rectangle.NO_BORDER);
			table_sub.addCell(cell);

			if (bDuplicate == true) {
				phrase = new Phrase("DUPLICATE", fontBlack10Bold);
				cell = new PdfPCell(phrase);
				cell.setPaddingTop(5);
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell.setBorder(Rectangle.NO_BORDER);
				table_sub.addCell(cell);
			}
			else
			{
				phrase = new Phrase(" ", fontBlack10Bold);
				cell = new PdfPCell(phrase);
				cell.setPaddingTop(5);
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell.setBorder(Rectangle.NO_BORDER);
				table_sub.addCell(cell);
			}
			int index = 0;
			for(index = 0; index < 8; index++)
			{
				DocTextInfo info = null;
				if( index < mDocTextList.size())
					info = mDocTextList.get(index);
				if (info != null && info.nTextType == 6) {
					if( info.strDocText.length() == 0 )
					{
						phrase = new Phrase("  ", fontBlack9Bold);
						cell = new PdfPCell(phrase);
						cell.setBorder(Rectangle.NO_BORDER);
						table_sub.addCell(cell);
					}
					else
					{
						phrase = new Phrase(info.strDocText.trim(), fontBlack9Bold);
						cell = new PdfPCell(phrase);
						cell.setBorder(Rectangle.NO_BORDER);
						table_sub.addCell(cell);
					}
				}
			}

			cell = new PdfPCell(table_sub);
			cell.setColspan(2);
			cell.setHorizontalAlignment(Element.ALIGN_LEFT);
			cell.setBorder(Rectangle.NO_BORDER);
			table.addCell(cell);

			table_sub = new PdfPTable(2);
			table_sub.setWidths(new int[]{1, 1});
			table_sub.getDefaultCell().setBorder(Rectangle.NO_BORDER);

			if(!m_bFutureOrder)
			{
				phrase = new Phrase("Delivery Docket", fontBlack10Bold);
			}
			else
			{
				phrase = new Phrase("Order Confirmation", fontBlack10Bold);
			}
			cell = new PdfPCell(phrase);
			cell.setHorizontalAlignment(Element.ALIGN_LEFT);
			cell.setBorder(Rectangle.NO_BORDER);
			cell.setPaddingTop(60);
			cell.setColspan(2);
			table_sub.addCell(cell);

			if(!m_bFutureOrder)
			{
				phrase = new Phrase("Docket Number", fontBlack9Bold);
			}
			else
			{
				phrase = new Phrase("Order Number", fontBlack9Bold);
			}
			cell = new PdfPCell(phrase);
			cell.setPaddingTop(8);
			cell.setBorder(Rectangle.NO_BORDER);
			cell.setHorizontalAlignment(Element.ALIGN_LEFT);
			table_sub.addCell(cell);


			if(Global.gUOrderHInfo.strDocNo.length() == 0 )
				phrase = new Phrase("  ", fontBlack9Normal);
			else
				phrase = new Phrase(Global.gUOrderHInfo.strDocNo, fontBlack9Normal);
			cell = new PdfPCell(phrase);
			cell.setPaddingTop(8);
			cell.setBorder(Rectangle.NO_BORDER);
			cell.setHorizontalAlignment(Element.ALIGN_LEFT);
			table_sub.addCell(cell);

			phrase = new Phrase(" ", fontBlack9Bold);
			cell = new PdfPCell(phrase);
			cell.setColspan(2);
			cell.setBorder(Rectangle.NO_BORDER);
			table_sub.addCell(cell);

			phrase = new Phrase("PO#", fontBlack9Bold);
			cell = new PdfPCell(phrase);
			cell.setBorder(Rectangle.NO_BORDER);
			table_sub.addCell(cell);

			if(Global.gUOrderHInfo.strPOReference.length() > 0 )
				phrase = new Phrase(Global.gUOrderHInfo.strPOReference, fontBlack9Normal);
			else
				phrase = new Phrase(" ", fontBlack9Normal);
			cell = new PdfPCell(phrase);
			cell.setBorder(Rectangle.NO_BORDER);
			table_sub.addCell(phrase);

			phrase = new Phrase("Delivery Driver", fontBlack9Bold);
			cell = new PdfPCell(phrase);
			cell.setBorder(Rectangle.NO_BORDER);
			cell.setPaddingTop(5);
			table_sub.addCell(cell);

			String strDescription = Utils.getStringSetting(mContext, Global.PREF_USER_DESCRIPTION);
			if(strDescription.length() > 0 )
				phrase = new Phrase(strDescription, fontBlack9Normal);
			else
				phrase = new Phrase(" ", fontBlack9Normal);
			cell = new PdfPCell(phrase);
			cell.setPaddingTop(5);
			cell.setBorder(Rectangle.NO_BORDER);
			table_sub.addCell(cell);

			phrase = new Phrase("Delivery Date", fontBlack9Bold);
			cell = new PdfPCell(phrase);
			cell.setBorder(Rectangle.NO_BORDER);
			table_sub.addCell(cell);

			String strDate = Global.gUOrderHInfo.strDeliveryDate.substring(6) + "/" + Global.gUOrderHInfo.strDeliveryDate.substring(4, 6) + "/" + Global.gUOrderHInfo.strDeliveryDate.substring(0, 4);
			phrase = new Phrase(strDate, fontBlack9Normal);
			cell = new PdfPCell(phrase);
			cell.setBorder(Rectangle.NO_BORDER);
			cell.setHorizontalAlignment(Element.ALIGN_LEFT);
			table_sub.addCell(cell);

			SimpleDateFormat df = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT);
			SimpleDateFormat tdf = new SimpleDateFormat(Global.TIME_HH_MM_FORMAT);
			String time = "";
			try{
				time = tdf.format((df.parse(Global.gUOrderHInfo.strTrxnTime)));
			}catch(Exception e)
			{
				e.printStackTrace();
			}

			phrase = new Phrase("Delivery Time", fontBlack9Bold);
			cell = new PdfPCell(phrase);
			cell.setBorder(Rectangle.NO_BORDER);
			table_sub.addCell(cell);

			phrase = new Phrase(time, fontBlack9Normal);
			cell = new PdfPCell(phrase);
			cell.setBorder(Rectangle.NO_BORDER);
			cell.setHorizontalAlignment(Element.ALIGN_LEFT);
			table_sub.addCell(cell);

			cell = new PdfPCell(table_sub);
			cell.setBorder(Rectangle.NO_BORDER);
			table.addCell(cell);



			phrase = new Phrase(" ", fontBlack10Bold);
			cell = new PdfPCell(phrase);
			cell.setPaddingTop(5);
			cell.setColspan(3);
			cell.setBorder(Rectangle.NO_BORDER);
			table.addCell(cell);


			table_sub = new PdfPTable(1);
			table_sub.setWidths(new int[]{1});
			table_sub.getDefaultCell().setBorder(Rectangle.NO_BORDER);

			phrase = new Phrase("Deliver To:", fontBlack9Bold);
			cell = new PdfPCell(phrase);
			cell.setPaddingTop(8);
			cell.setBorder(Rectangle.NO_BORDER);
			table_sub.addCell(cell);

			if( !TextUtils.isEmpty(Global.gCurCustomerInfo.strName) )
			{
				phrase = new Phrase(Global.gCurCustomerInfo.strName, fontBlack9Normal);
			}
			else
			{
				phrase = new Phrase(" ", fontBlack9Normal);
			}

			table_sub.addCell(phrase);
			String strAddress1 = Global.gCurCustomerInfo.strAddress1;
			if( !TextUtils.isEmpty(strAddress1) )
			{
				phrase = new Phrase(strAddress1, fontBlack9Normal);
			}
			else
			{
				phrase = new Phrase(" ", fontBlack9Normal);
			}

			table_sub.addCell(phrase);

			String strAddress2 = Global.gCurCustomerInfo.strAddress2;

			if(!TextUtils.isEmpty(strAddress2))
			{
				phrase = new Phrase(strAddress2, fontBlack9Normal);
				table_sub.addCell(phrase);
			}

			phrase = new Phrase(Global.gCurCustomerInfo.strCity + " " + Global.gCurCustomerInfo.strState + " " + Global.gCurCustomerInfo.strZip, fontBlack9Normal);
			table_sub.addCell(phrase);

			cell = new PdfPCell(table_sub);
			cell.setBorder(Rectangle.NO_BORDER);
			table.addCell(cell);
			phrase = new Phrase(" ", fontBlack10Bold);
			cell = new PdfPCell(phrase);
			cell.setPaddingTop(8);
			cell.setBorder(Rectangle.NO_BORDER);
			table.addCell(cell);

			table_sub = new PdfPTable(2);
			table_sub.setWidths(new int[]{1, 1});
			table_sub.getDefaultCell().setBorder(Rectangle.NO_BORDER);

			if( mInvoiceToInfo.strBillName == null || mInvoiceToInfo.strBillName.trim().length() == 0 )
				phrase = new Phrase(mInvoiceToInfo.strName, fontBlack9Bold);
			else
				phrase = new Phrase(mInvoiceToInfo.strBillName, fontBlack9Bold);
			cell = new PdfPCell(phrase);
			cell.setPaddingTop(8);
			cell.setBorder(Rectangle.NO_BORDER);
			cell.setColspan(2);
			table_sub.addCell(cell);

			phrase = new Phrase("Customer ID", fontBlack9Bold);
			cell = new PdfPCell(phrase);
			cell.setBorder(Rectangle.NO_BORDER);
			table_sub.addCell(cell);

			if( Global.gCurCustomerInfo.strAltCustNo.equals("") )
			{
				if( Global.gCurCustomerInfo.nChainNo == 0 )
				{
					phrase = new Phrase(String.valueOf(Global.gCurCustomerInfo.nCustNo), fontBlack9Bold);
				}
				else
				{
					phrase = new Phrase(Global.gCurCustomerInfo.nChainNo + " / " + Global.gCurCustomerInfo.nCustNo, fontBlack9Bold);
				}
			}
			else
			{
				phrase = new Phrase(Global.gCurCustomerInfo.strAltCustNo, fontBlack9Bold);
			}

			cell = new PdfPCell(phrase);
			cell.setBorder(Rectangle.NO_BORDER);
			table_sub.addCell(cell);

			phrase = new Phrase("(Use as payment reference)", fontBlack9Bold);
			cell = new PdfPCell(phrase);
			cell.setColspan(2);
			cell.setBorder(Rectangle.NO_BORDER);
			table_sub.addCell(cell);

			cell = new PdfPCell(table_sub);
			cell.setBorder(Rectangle.NO_BORDER);
			table.addCell(cell);

			phrase = new Phrase(" ", fontBlack10Bold);
			cell = new PdfPCell(phrase);
			cell.setColspan(3);
			cell.setPaddingTop(5);
			cell.setBorder(Rectangle.NO_BORDER);
			table.addCell(cell);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return table;
	}

	private PdfPTable createA4DocketPageNumber(int nPageNo)
	{
		PdfPTable table = new PdfPTable(1);
		PdfPCell cell;
		try{
			table.setWidths(new int[]{1});
			table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
			Phrase phrase = new Phrase("-" + nPageNo + "-", fontBlack9Normal);
			cell = new PdfPCell(phrase);
			cell.setBorder(Rectangle.NO_BORDER);
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			table.addCell(cell);
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		return table;
	}

	private PdfPTable createA4DocketFooter(){
		PdfPTable table = new PdfPTable(1);
		PdfPCell cell;
		Phrase phrase;
		try
		{
			table.setWidths(new int[]{1});
			table.getDefaultCell().setBorder(Rectangle.NO_BORDER);

			phrase = new Phrase("Thank you for your business", fontBlack10Bold);
			cell = new PdfPCell(phrase);
			cell.setBorder(Rectangle.NO_BORDER);
			cell.setPaddingTop(4);
			table.addCell(cell);

			phrase = new Phrase("Please retain this document for your records", fontBlack10Bold);
			cell = new PdfPCell(phrase);
			cell.setBorder(Rectangle.NO_BORDER);
			cell.setPaddingTop(4);
			table.addCell(cell);

		}catch(Exception e)
		{
			e.printStackTrace();
		}
		return table;
	}

	private void createPDF(boolean bDuplicate, String pdfFileName) {
		try {
			if (bDuplicate == false) {
				Utils.createDirectory(Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/");
			} else {
				Utils.createDirectory(Global.PDF_LOCAL_DIR);
			}

			int nHeight = 380 + 10 * (Global.gUOrderHInfo.strPOReference.length() / 30 + 1);
			if (Global.gCurCustomerInfo.nInvoiceFmt > 0) {
				nHeight += 22 * (mDeliveredCnt + mReturnsCnt) + 11 * (mContainersCnt + mDocTextList.size());
				if (mDeliveredCnt > 0) {
					nHeight += 15;
				}
				if (mReturnsCnt > 0) {
					nHeight += 15;
				}
				if (mContainersCnt > 0) {
					nHeight += 15;
				}
			} else {
				nHeight += 22 * (mDeliveredCnt + mReturnsCnt + mContainersCnt) + 11 * mDocTextList.size();
			}
			if (bDuplicate == true) {
				nHeight += 20;
			}
			if (mDeliverToInfo != null) {
				nHeight += 40;
			}
			if (mPayMethod > 0) {
				nHeight += 40 + 10 * (Global.gUOrderHInfo.strOrderName.length() / 15);
			}
			if (Global.gUOrderHInfo.strSignatureFilePath.length() > 0 && Utils.isFileExist(Global.gUOrderHInfo.strSignatureFilePath) == true) {
				nHeight += 120;
			}
			
			nHeight += 20;
			
			Integer nHeightObj = new Integer(0);
			PdfPTable table_header = createHeader(bDuplicate, nHeightObj);
			table_header.setLockedWidth(true);
			table_header.setTotalWidth(120f);
			
			float fHeight = table_header.getTotalHeight();
			
			PdfPTable table_body = createBody_InvoiceFmt(nHeightObj);
			table_body.setLockedWidth(true);
			table_body.setTotalWidth(120f);
			fHeight += table_body.getTotalHeight();
			
			PdfPTable table_body_footer = createBody_Footer(nHeightObj);
			table_body_footer.setLockedWidth(true);
			table_body_footer.setTotalWidth(120f);
			fHeight += table_body_footer.getTotalHeight();
			
			
			PdfPTable table_footer = createFooter(nHeightObj);
			table_footer.setLockedWidth(true);
			table_footer.setTotalWidth(120f);
			fHeight += table_footer.getTotalHeight();
			
			fHeight += 20;
			
			if( nHeight < fHeight ) nHeight = Math.round(fHeight);
			
			Document document = new Document(new Rectangle(0, 0, 140, nHeight), 10, 10, 0, 0);
			
			if (bDuplicate == false) {
				PdfWriter.getInstance(document, new FileOutputStream(Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + pdfFileName));
			} else {
				PdfWriter.getInstance(document, new FileOutputStream(Global.PDF_LOCAL_DIR + pdfFileName));
			}
			
			document.open();
			document.add(table_header);
/*			if (Global.gCurCustomerInfo.nInvoiceFmt > 0) {
				table = createBody_InvoiceFmt();
			} else {
				table = createBody();
			}*/
			table_body.setSpacingBefore(5);
			document.add(table_body);
			document.add(table_body_footer);
			table_footer.setSpacingBefore(5);
			document.add(table_footer);
			document.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private PdfPTable createA4PageNumber(int nPageNo)
	{
		PdfPTable table = new PdfPTable(1);
		PdfPCell cell;
		try{
			table.setWidths(new int[]{1});
			table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
			Phrase phrase = new Phrase("-" + nPageNo + "-", fontBlack9Normal);
			cell = new PdfPCell(phrase);
			cell.setBorder(Rectangle.NO_BORDER);
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			table.addCell(cell);
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		return table;		
	}
	
	private PdfPTable createA4Footer(){
		PdfPTable table = new PdfPTable(3);
		PdfPCell cell;
		Phrase phrase;
		
		boolean bShowPaymentSection = false;
		if (mPayMethod == 0 || m_bFutureOrder)
			bShowPaymentSection = true;
		
		try {
			table.setWidths(new int[]{3, 4, 2});
			table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
			
			if(bShowPaymentSection)
			{
				phrase = new Phrase(" ", fontBlackUnderLine10Bold);
				cell = new PdfPCell(phrase);
				cell.setBorder(Rectangle.NO_BORDER);
				cell.setPaddingTop(4);
				cell.setColspan(3);
				table.addCell(cell);
				
				PdfPTable table_sub = new PdfPTable(2);
				table_sub.setWidths(new int[]{1, 1});
				table_sub.getDefaultCell().setBorder(Rectangle.NO_BORDER);
				
				phrase = new Phrase("Payment Options", fontBlackUnderLine10Bold);
				cell = new PdfPCell(phrase);
				cell.setHorizontalAlignment(Element.ALIGN_LEFT);
				cell.setBorder(Rectangle.NO_BORDER);
				cell.setColspan(2);
				
				table_sub.addCell(cell);
				
				boolean bFirst = true;
				for(int i = 9; i <= 13; i++)
				{
					DocTextInfo curInfo = null;
					for(DocTextInfo info:mDocTextList)
					{
						if(info.nTextType == 6 && info.nLine == i)
						{
							curInfo = info;
							break;
						}
					}
					if(curInfo != null)
					{
						if( i == 9 || bFirst)
							phrase = new Phrase(curInfo.strDocText, fontBlack9Bold);
						else
							phrase = new Phrase(curInfo.strDocText, fontBlack9Normal);
						cell = new PdfPCell(phrase);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						cell.setBorder(Rectangle.NO_BORDER);
						cell.setColspan(2);
						table_sub.addCell(cell);
					}
					else
					{
						if( i == 9 || bFirst)
							phrase = new Phrase(" ", fontBlack9Bold);
						else
							phrase = new Phrase(" ", fontBlack9Normal);
						cell = new PdfPCell(phrase);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
						cell.setBorder(Rectangle.NO_BORDER);
						cell.setColspan(2);
						table_sub.addCell(cell);
					}
					if(curInfo != null && curInfo.strDocText.length() != 0 )
						bFirst =  false;
				}
				
				
				phrase = new Phrase();
				Chunk chunk = new Chunk("In order for us to allocate your payments, please use your ", fontBlack9Bold);
				phrase.add(chunk);
				String customerString = "";
				if( Global.gCurCustomerInfo.strAltCustNo.equals("") )
				{
					if( Global.gCurCustomerInfo.nChainNo == 0 )
					{
						customerString = String.valueOf(Global.gCurCustomerInfo.nCustNo);
					}
					else
					{
						customerString = Global.gCurCustomerInfo.nChainNo + " / " + Global.gCurCustomerInfo.nCustNo;
					}
				}
				else
				{
					customerString = Global.gCurCustomerInfo.strAltCustNo;
				}
				
				chunk = new Chunk("Customer ID " + customerString + " ", fontBlackUnderLine9Bold);
				phrase.add(chunk);
				chunk = new Chunk("as reference.", fontBlack9Bold);
				phrase.add(chunk);

				cell = new PdfPCell(phrase);
				cell.setColspan(2);
				cell.setHorizontalAlignment(Element.ALIGN_LEFT);
				cell.setBorder(Rectangle.NO_BORDER);
				table_sub.addCell(cell);
				
				cell = new PdfPCell(table_sub);
				cell.setBorder(Rectangle.RIGHT);
				table.addCell(cell);
				
				table_sub = new PdfPTable(1);
				table_sub.setWidths(new int[]{1});
				table_sub.getDefaultCell().setBorder(Rectangle.NO_BORDER);
				
				phrase = new Phrase("  ", fontBlack10Bold);
				cell = new PdfPCell(phrase);
				cell.setHorizontalAlignment(Element.ALIGN_LEFT);
				cell.setBorder(Rectangle.NO_BORDER);
				table_sub.addCell(cell);
				
				phrase = new Phrase("Credit Card Details", fontBlack9Bold);
				cell = new PdfPCell(phrase);
				cell.setHorizontalAlignment(Element.ALIGN_LEFT);
				cell.setBorder(Rectangle.NO_BORDER);
				cell.setPaddingLeft(15);
				table_sub.addCell(cell);
				
				
				PdfPTable underlineTable = new PdfPTable(3);
				underlineTable.setWidths(new int[]{1, 6, 1});
				underlineTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
				Phrase underlinePhrase = new Phrase("Name", fontBlack9Normal);
				PdfPCell underlineCell = new PdfPCell(underlinePhrase);
				underlineCell.setBorder(Rectangle.NO_BORDER);
				underlineTable.addCell(underlineCell);
				underlinePhrase = new Phrase(" ", fontBlack9Bold);
				underlineCell = new PdfPCell(underlinePhrase);
				underlineCell.setBorder(Rectangle.BOTTOM);
				underlineTable.addCell(underlineCell);
				underlinePhrase = new Phrase(" ", fontBlack9Bold);
				underlineCell = new PdfPCell(underlinePhrase);
				underlineCell.setBorder(Rectangle.NO_BORDER);
				underlineTable.addCell(underlineCell);
				
				cell = new PdfPCell(underlineTable);
				cell.setBorder(Rectangle.NO_BORDER);
				cell.setPaddingLeft(14);			
				table_sub.addCell(cell);
							
				phrase = new Phrase();
				phrase.add(new Chunk("Visa  ", fontBlack9Normal));
				
				Drawable d = mContext.getResources ().getDrawable (R.drawable.ic_check);
				Bitmap bitmap = ((BitmapDrawable)d).getBitmap();
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
				byte[] bitmapData = stream.toByteArray();
				Image image = Image.getInstance(bitmapData);
				image.scaleToFit(new Rectangle(0, 0, 10, 10));
				phrase.add(new Chunk(image, 0, 0, true));
				
				phrase.add(new Chunk("  Master Card  ", fontBlack9Normal));
				phrase.add(new Chunk(image, 0, 0, true));
				phrase.add(new Chunk("  American Express  ", fontBlack9Normal));
				phrase.add(new Chunk(image, 0, 0, true));
				cell = new PdfPCell(phrase);
				cell.setBorder(Rectangle.NO_BORDER);
				cell.setPaddingLeft(16);
				cell.setPaddingTop(3);
				table_sub.addCell(cell);
				
				underlineTable = new PdfPTable(8);
				underlineTable.setWidths(new int[]{4, 4, 1, 4, 1, 4, 1, 4});
				underlineTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
				
				underlinePhrase = new Phrase("Card # ", fontBlack9Normal);
				underlineCell = new PdfPCell(underlinePhrase);
				underlineCell.setBorder(Rectangle.NO_BORDER);
				underlineTable.addCell(underlineCell);
				
				underlinePhrase = new Phrase(" ", fontBlack9Normal);
				underlineCell = new PdfPCell(underlinePhrase);
				underlineCell.setBorder(Rectangle.BOTTOM);
				underlineTable.addCell(underlineCell);
				
				underlinePhrase = new Phrase(" ", fontBlack9Normal);
				underlineCell = new PdfPCell(underlinePhrase);
				underlineCell.setBorder(Rectangle.NO_BORDER);
				underlineTable.addCell(underlineCell);
				
				underlinePhrase = new Phrase(" ", fontBlack9Normal);
				underlineCell = new PdfPCell(underlinePhrase);
				underlineCell.setBorder(Rectangle.BOTTOM);
				underlineTable.addCell(underlineCell);
				
				underlinePhrase = new Phrase(" ", fontBlack9Normal);
				underlineCell = new PdfPCell(underlinePhrase);
				underlineCell.setBorder(Rectangle.NO_BORDER);
				underlineTable.addCell(underlineCell);

				underlinePhrase = new Phrase(" ", fontBlack9Normal);
				underlineCell = new PdfPCell(underlinePhrase);
				underlineCell.setBorder(Rectangle.BOTTOM);
				underlineTable.addCell(underlineCell);
				
				underlinePhrase = new Phrase(" ", fontBlack9Normal);
				underlineCell = new PdfPCell(underlinePhrase);
				underlineCell.setBorder(Rectangle.NO_BORDER);
				underlineTable.addCell(underlineCell);

				underlinePhrase = new Phrase(" ", fontBlack9Normal);
				underlineCell = new PdfPCell(underlinePhrase);
				underlineCell.setBorder(Rectangle.BOTTOM);
				underlineTable.addCell(underlineCell);
				
				cell = new PdfPCell(underlineTable);
				cell.setBorder(Rectangle.NO_BORDER);
				cell.setPaddingLeft(14);
				table_sub.addCell(cell);
				
				underlineTable = new PdfPTable(5);
				underlineTable.setWidths(new int[]{4, 4, 1, 4, 12});
				underlineTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
				
				underlinePhrase = new Phrase("Expiry ", fontBlack9Normal);
				underlineCell = new PdfPCell(underlinePhrase);
				underlineCell.setBorder(Rectangle.NO_BORDER);
				underlineTable.addCell(underlineCell);
				
				underlinePhrase = new Phrase(" ", fontBlack9Normal);
				underlineCell = new PdfPCell(underlinePhrase);
				underlineCell.setBorder(Rectangle.BOTTOM);
				underlineTable.addCell(underlineCell);
				
				underlinePhrase = new Phrase("/", fontBlack9Normal);
				underlineCell = new PdfPCell(underlinePhrase);
				underlineCell.setBorder(Rectangle.NO_BORDER);
				underlineTable.addCell(underlineCell);
				
				underlinePhrase = new Phrase(" ", fontBlack9Normal);
				underlineCell = new PdfPCell(underlinePhrase);
				underlineCell.setBorder(Rectangle.BOTTOM);
				underlineTable.addCell(underlineCell);
				
				underlinePhrase = new Phrase(" ", fontBlack9Normal);
				underlineCell = new PdfPCell(underlinePhrase);
				underlineCell.setBorder(Rectangle.NO_BORDER);
				underlineTable.addCell(underlineCell);
				
				cell = new PdfPCell(underlineTable);
				cell.setBorder(Rectangle.NO_BORDER);
				cell.setPaddingLeft(14);
				table_sub.addCell(cell);
						
				underlineTable = new PdfPTable(3);
				underlineTable.setWidths(new int[]{2, 4, 1});
				underlineTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
				underlinePhrase = new Phrase("Signature", fontBlack9Normal);
				underlineCell = new PdfPCell(underlinePhrase);
				underlineCell.setPaddingLeft(11);
				underlineCell.setBorder(Rectangle.NO_BORDER);
				underlineTable.addCell(underlineCell);
				underlinePhrase = new Phrase(" ", fontBlack9Bold);
				underlineCell = new PdfPCell(underlinePhrase);
				underlineCell.setBorder(Rectangle.BOTTOM);
				underlineTable.addCell(underlineCell);
				underlinePhrase = new Phrase(" ", fontBlack9Bold);
				underlineCell = new PdfPCell(underlinePhrase);
				underlineCell.setBorder(Rectangle.NO_BORDER);
				underlineTable.addCell(underlineCell);
				
				cell = new PdfPCell(underlineTable);
				cell.setBorder(Rectangle.NO_BORDER);
				cell.setPaddingLeft(14);
				cell.setPadding(5);
				table_sub.addCell(cell);
				
				cell = new PdfPCell(table_sub);
				cell.setBorder(Rectangle.RIGHT);
				cell.setPaddingRight(2);
				table.addCell(cell);
				
				table_sub = new PdfPTable(1);
				table_sub.setWidths(new int[]{1});
				table_sub.getDefaultCell().setBorder(Rectangle.NO_BORDER);
				
				phrase = new Phrase("  ", fontBlackUnderLine10Bold);
				cell = new PdfPCell(phrase);
				cell.setHorizontalAlignment(Element.ALIGN_LEFT);
				cell.setBorder(Rectangle.NO_BORDER);
				table_sub.addCell(cell);
				
				phrase = new Phrase("Cheque", fontBlack9Bold);
				cell = new PdfPCell(phrase);
				cell.setHorizontalAlignment(Element.ALIGN_LEFT);
				cell.setBorder(Rectangle.NO_BORDER);
				table_sub.addCell(cell);
				
				cell = new PdfPCell(image);
				cell.setHorizontalAlignment(Element.ALIGN_LEFT);
				cell.setBorder(Rectangle.NO_BORDER);
				cell.setPadding(5);
				table_sub.addCell(cell);
				
				phrase = new Phrase();
				phrase.add(new Chunk("Please make cheques payable to :", fontBlack9Normal));
				
				cell = new PdfPCell(phrase);
				cell.setHorizontalAlignment(Element.ALIGN_LEFT);
				cell.setBorder(Rectangle.NO_BORDER);
				cell.setPadding(2);
				table_sub.addCell(cell);
				
				
				phrase = new Phrase();
				DocTextInfo curInfo = null;
				
				for(DocTextInfo info : mDocTextList)
				{
					if(info.nTextType == 6 && info.nLine == 1)
					{
						curInfo = info;
						break; 
					}
				}
				if( curInfo != null )
					phrase.add(new Chunk(curInfo.strDocText.trim(), fontBlack9Bold));
				else
					phrase.add(new Chunk(" ", fontBlack9Bold));
				cell = new PdfPCell(phrase);
				cell.setHorizontalAlignment(Element.ALIGN_LEFT);
				cell.setBorder(Rectangle.NO_BORDER);
				cell.setPadding(2);
				table_sub.addCell(cell);
				
				cell = new PdfPCell(table_sub);
				cell.setBorder(Rectangle.NO_BORDER);
				cell.setPaddingLeft(15);
				table.addCell(cell);
			}
			
			if( mPayMethod == 0 || m_bFutureOrder )
			{
				if( Global.gUOrderHInfo.nSaleAmount + Global.gUOrderHInfo.nTaxAmount + Global.gUOrderHInfo.nHandlingFee + Global.gUOrderHInfo.nHandlingFeeTaxAmount> 0 )
				{
					phrase = new Phrase();
					for(DocTextInfo info : mDocTextList)
					{
						if( info.nLine > 1 && info.nTextType == 7 && (TextUtils.isEmpty(info.strSubType) || info.strSubType.equals("P")))
						{
							if( info.strDocText.length() == 0 )
								phrase.add(new Chunk(" ", fontBlack7Normal));
							else
								phrase.add(new Chunk(info.strDocText + ". ", fontBlack7Normal));
						}
					}
					cell = new PdfPCell(phrase);
					cell.setBorder(Rectangle.NO_BORDER);
					cell.setPaddingTop(8);
					cell.setColspan(3);
					table.addCell(cell);
				}
				
			}
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return table;
	}
	
	private PdfPTable createA4Header(boolean bDuplicate){
		PdfPTable table = new PdfPTable(3);
		PdfPCell cell;
		Phrase phrase;
		try {
			table.setWidths(new int[]{1, 1, 1});
			table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
			
			PdfPTable table_sub = new PdfPTable(1);
			table_sub.setWidths(new int[]{1});
			table_sub.getDefaultCell().setBorder(Rectangle.NO_BORDER);
			
			if (Utils.isFileExist(Global.DEFAULT_COMPANY_LOGO_FILEPATH) == true) {
				Image image = Image.getInstance(Global.DEFAULT_COMPANY_LOGO_FILEPATH);
				image.scaleToFit(80, 80);
				cell = new PdfPCell(image);
				cell.setHorizontalAlignment(Element.ALIGN_LEFT);
			} else {
				phrase = new Phrase("No Logo File", fontBlack10Bold);
				cell = new PdfPCell(phrase);
				cell.setHorizontalAlignment(Element.ALIGN_LEFT);
				cell.setVerticalAlignment(Element.ALIGN_CENTER);
			}
			cell.setPaddingTop(10);
			cell.setBorder(Rectangle.NO_BORDER);
			table_sub.addCell(cell);
			
			if (bDuplicate == true) {
				phrase = new Phrase("DUPLICATE", fontBlack10Bold);
				cell = new PdfPCell(phrase);
				cell.setPaddingTop(5);
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell.setBorder(Rectangle.NO_BORDER);
				table_sub.addCell(cell);
			}
			else
			{
				phrase = new Phrase(" ", fontBlack10Bold);
				cell = new PdfPCell(phrase);
				cell.setPaddingTop(5);
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell.setBorder(Rectangle.NO_BORDER);
				table_sub.addCell(cell);
			}
			int index = 0;
			for(index = 0; index < 8; index++)
			{
				DocTextInfo info = null;
				if( index < mDocTextList.size())
					info = mDocTextList.get(index);			
				if (info != null && info.nTextType == 6) {
					if( info.strDocText.length() == 0 )
					{
						phrase = new Phrase("  ", fontBlack9Bold);
						cell = new PdfPCell(phrase);
						cell.setBorder(Rectangle.NO_BORDER);
						table_sub.addCell(cell);
					}
					else
					{
						phrase = new Phrase(info.strDocText.trim(), fontBlack9Bold);
						cell = new PdfPCell(phrase);
						cell.setBorder(Rectangle.NO_BORDER);
						table_sub.addCell(cell);
					}
				}
			}
			
			cell = new PdfPCell(table_sub);
			cell.setColspan(2);
			cell.setHorizontalAlignment(Element.ALIGN_LEFT);
			cell.setBorder(Rectangle.NO_BORDER);
			table.addCell(cell);
						
			table_sub = new PdfPTable(2);
			table_sub.setWidths(new int[]{1, 1});
			table_sub.getDefaultCell().setBorder(Rectangle.NO_BORDER);
			
			if(!m_bFutureOrder)
			{
				phrase = new Phrase("Tax Invoice", fontBlack10Bold);
			}
			else
			{
				phrase = new Phrase("Order Confirmation", fontBlack10Bold);
			}
			cell = new PdfPCell(phrase);
			cell.setHorizontalAlignment(Element.ALIGN_LEFT);
			cell.setBorder(Rectangle.NO_BORDER);
			cell.setPaddingTop(60);
			cell.setColspan(2);
			table_sub.addCell(cell);
			
			if(!m_bFutureOrder)
			{
				phrase = new Phrase("Invoice Number", fontBlack9Bold);
			}
			else
			{
				phrase = new Phrase("Order Number", fontBlack9Bold);
			}
			cell = new PdfPCell(phrase);
			cell.setPaddingTop(8);
			cell.setBorder(Rectangle.NO_BORDER);
			cell.setHorizontalAlignment(Element.ALIGN_LEFT);
			table_sub.addCell(cell);
			

			if(Global.gUOrderHInfo.strDocNo.length() == 0 )
				phrase = new Phrase("  ", fontBlack9Normal);
			else
				phrase = new Phrase(Global.gUOrderHInfo.strDocNo, fontBlack9Normal);
			cell = new PdfPCell(phrase);
			cell.setPaddingTop(8);
			cell.setBorder(Rectangle.NO_BORDER);
			cell.setHorizontalAlignment(Element.ALIGN_LEFT);
			table_sub.addCell(cell);
			
			
			phrase = new Phrase("Terms", fontBlack9Bold);
			cell = new PdfPCell(phrase);
			cell.setBorder(Rectangle.NO_BORDER);
			cell.setHorizontalAlignment(Element.ALIGN_LEFT);
			table_sub.addCell(cell);
			
			if(Global.gCurCustomerInfo.strTerms.length() > 0 )
				phrase = new Phrase(Global.gCurCustomerInfo.strTerms, fontBlack9Normal);
			else
				phrase = new Phrase(" ", fontBlack9Normal);
			cell = new PdfPCell(phrase);
			cell.setBorder(Rectangle.NO_BORDER);
			cell.setHorizontalAlignment(Element.ALIGN_LEFT);
			table_sub.addCell(cell);
			
			phrase = new Phrase(" ", fontBlack9Bold);
			cell = new PdfPCell(phrase);
			cell.setColspan(2);
			cell.setBorder(Rectangle.NO_BORDER);
			table_sub.addCell(cell);
			
			phrase = new Phrase("PO#", fontBlack9Bold);
			cell = new PdfPCell(phrase);
			cell.setBorder(Rectangle.NO_BORDER);
			table_sub.addCell(cell);
			
			if(Global.gUOrderHInfo.strPOReference.length() > 0 )
				phrase = new Phrase(Global.gUOrderHInfo.strPOReference, fontBlack9Normal);
			else
				phrase = new Phrase(" ", fontBlack9Normal);
			cell = new PdfPCell(phrase);
			cell.setBorder(Rectangle.NO_BORDER);
			table_sub.addCell(phrase);
			
			phrase = new Phrase("Sales Rep", fontBlack9Bold);
			cell = new PdfPCell(phrase);
			cell.setBorder(Rectangle.NO_BORDER);
			cell.setPaddingTop(5);
			table_sub.addCell(cell);
			
			String strDescription = Utils.getStringSetting(mContext, Global.PREF_USER_DESCRIPTION);
			if(strDescription.length() > 0 )
				phrase = new Phrase(strDescription, fontBlack9Normal);
			else
				phrase = new Phrase(" ", fontBlack9Normal);
			cell = new PdfPCell(phrase);
			cell.setPaddingTop(5);
			cell.setBorder(Rectangle.NO_BORDER);
			table_sub.addCell(cell);
			
			phrase = new Phrase("Delivery Date", fontBlack9Bold);
			cell = new PdfPCell(phrase);
			cell.setBorder(Rectangle.NO_BORDER);
			table_sub.addCell(cell);
			
			String strDate = Global.gUOrderHInfo.strTrxnDate.substring(6) + "/" + Global.gUOrderHInfo.strTrxnDate.substring(4, 6) + "/" + Global.gUOrderHInfo.strTrxnDate.substring(0, 4);
			phrase = new Phrase(strDate, fontBlack9Normal);
			cell = new PdfPCell(phrase);
			cell.setBorder(Rectangle.NO_BORDER);
			cell.setHorizontalAlignment(Element.ALIGN_LEFT);
			table_sub.addCell(cell);
			
			phrase = new Phrase("Invoice Date", fontBlack9Bold);
			cell = new PdfPCell(phrase);
			cell.setBorder(Rectangle.NO_BORDER);
			table_sub.addCell(cell);
			
			strDate = Global.gUOrderHInfo.strDeliveryDate.substring(6) + "/" + Global.gUOrderHInfo.strDeliveryDate.substring(4, 6) + "/" + Global.gUOrderHInfo.strDeliveryDate.substring(0, 4);
			phrase = new Phrase(strDate, fontBlack9Normal);
			cell = new PdfPCell(phrase);
			cell.setBorder(Rectangle.NO_BORDER);
			cell.setHorizontalAlignment(Element.ALIGN_LEFT);
			table_sub.addCell(cell);
			
			cell = new PdfPCell(table_sub);
			cell.setBorder(Rectangle.NO_BORDER);
			table.addCell(cell);
			
			
	
			phrase = new Phrase(" ", fontBlack10Bold);
			cell = new PdfPCell(phrase);
			cell.setPaddingTop(5);
			cell.setColspan(3);
			cell.setBorder(Rectangle.NO_BORDER);
			table.addCell(cell);
						

			table_sub = new PdfPTable(1);
			table_sub.setWidths(new int[]{1});
			table_sub.getDefaultCell().setBorder(Rectangle.NO_BORDER);
			
			phrase = new Phrase("Invoice To:", fontBlack9Bold);
			cell = new PdfPCell(phrase);
			cell.setPaddingTop(8);
			cell.setBorder(Rectangle.NO_BORDER);
			table_sub.addCell(cell);
			
			if( mInvoiceToInfo.strBillName == null || mInvoiceToInfo.strBillName.trim().length() == 0 )
				phrase = new Phrase(mInvoiceToInfo.strName, fontBlack9Normal);
			else
				phrase = new Phrase(mInvoiceToInfo.strBillName, fontBlack9Normal);
			table_sub.addCell(phrase);
			
			if( mInvoiceToInfo.strBillAddr1 == null || mInvoiceToInfo.strBillAddr1.trim().length() == 0 )
				phrase = new Phrase(mInvoiceToInfo.strAddress1, fontBlack9Normal);
			else
				phrase = new Phrase(mInvoiceToInfo.strBillAddr1, fontBlack9Normal);
			table_sub.addCell(phrase);
			
			String strAddress2 = Global.gCurCustomerInfo.strBillAddr2;

			if(!TextUtils.isEmpty(strAddress2))
			{
				phrase = new Phrase(strAddress2, fontBlack9Normal);
				table_sub.addCell(phrase);
			}			

			phrase = new Phrase(mInvoiceToInfo.strCity + " " + mInvoiceToInfo.strState + " " + mInvoiceToInfo.strZip, fontBlack9Normal);
			table_sub.addCell(phrase);
			
			cell = new PdfPCell(table_sub);
			cell.setBorder(Rectangle.NO_BORDER);
			table.addCell(cell);
			
			if (mDeliverToInfo != null && !mDeliverToInfo.compare(mInvoiceToInfo)) {
				table_sub = new PdfPTable(1);
				table_sub.setWidths(new int[]{1});
				table_sub.getDefaultCell().setBorder(Rectangle.NO_BORDER);
				
				phrase = new Phrase("Deliver To:", fontBlack9Bold);
				cell = new PdfPCell(phrase);
				cell.setPaddingTop(8);
				cell.setBorder(Rectangle.NO_BORDER);
				table_sub.addCell(cell);
								
				phrase = new Phrase(mDeliverToInfo.strName, fontBlack9Normal);
				table_sub.addCell(phrase);
				
				phrase = new Phrase(Global.gCurCustomerInfo.strAddress1, fontBlack9Normal);
				table_sub.addCell(phrase);
				
				if(!TextUtils.isEmpty(Global.gCurCustomerInfo.strAddress2))
				{
					phrase = new Phrase(Global.gCurCustomerInfo.strAddress2, fontBlack9Normal);
					table_sub.addCell(phrase);
				}
				
				
				phrase = new Phrase(Global.gCurCustomerInfo.strCity + " " + Global.gCurCustomerInfo.strShipToState + " " + Global.gCurCustomerInfo.strShipToZip, fontBlack9Normal);
				table_sub.addCell(phrase);
				
				cell = new PdfPCell(table_sub);
				cell.setBorder(Rectangle.NO_BORDER);
				table.addCell(cell);
			}
			else
			{
				phrase = new Phrase(" ", fontBlack10Bold);
				cell = new PdfPCell(phrase);
				cell.setPaddingTop(8);
				cell.setBorder(Rectangle.NO_BORDER);
				table.addCell(cell);
			}

			table_sub = new PdfPTable(2);
			table_sub.setWidths(new int[]{1, 1});
			table_sub.getDefaultCell().setBorder(Rectangle.NO_BORDER);
			
			if( mInvoiceToInfo.strBillName == null || mInvoiceToInfo.strBillName.trim().length() == 0 )
				phrase = new Phrase(mInvoiceToInfo.strName, fontBlack9Bold);
			else
				phrase = new Phrase(mInvoiceToInfo.strBillName, fontBlack9Bold);
			cell = new PdfPCell(phrase);
			cell.setPaddingTop(8);
			cell.setBorder(Rectangle.NO_BORDER);
			cell.setColspan(2);
			table_sub.addCell(cell);
			
			phrase = new Phrase("Customer ID", fontBlack9Bold);
			cell = new PdfPCell(phrase);
			cell.setBorder(Rectangle.NO_BORDER);
			table_sub.addCell(cell);

			if( Global.gCurCustomerInfo.strAltCustNo.equals("") )
			{
				if( Global.gCurCustomerInfo.nChainNo == 0 )
				{
					phrase = new Phrase(String.valueOf(Global.gCurCustomerInfo.nCustNo), fontBlack9Bold);
				}
				else
				{
					phrase = new Phrase(Global.gCurCustomerInfo.nChainNo + " / " + Global.gCurCustomerInfo.nCustNo, fontBlack9Bold);
				}
			}
			else
			{
				phrase = new Phrase(Global.gCurCustomerInfo.strAltCustNo, fontBlack9Bold);
			}
			cell = new PdfPCell(phrase);
			cell.setBorder(Rectangle.NO_BORDER);
			table_sub.addCell(cell);
			
			phrase = new Phrase("(Use as payment reference)", fontBlack9Bold);
			cell = new PdfPCell(phrase);
			cell.setColspan(2);
			cell.setBorder(Rectangle.NO_BORDER);
			table_sub.addCell(cell);
			
			cell = new PdfPCell(table_sub);
			cell.setBorder(Rectangle.NO_BORDER);
			table.addCell(cell);
			
			phrase = new Phrase(" ", fontBlack10Bold);
			cell = new PdfPCell(phrase);
			cell.setColspan(3);
			cell.setPaddingTop(5);
			cell.setBorder(Rectangle.NO_BORDER);
			table.addCell(cell);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return table;
	}
	private PdfPTable createHeader(boolean bDuplicate, Integer nHeight) {
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
				nHeight += 60;
			} else {
				phrase = new Phrase("No Logo File", fontBlack10Bold);
				cell = new PdfPCell(phrase);
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell.setVerticalAlignment(Element.ALIGN_CENTER);
				nHeight += 60;
			}
			cell.setPaddingTop(10);
			cell.setBorder(Rectangle.NO_BORDER);
			table.addCell(cell);

			if (bDuplicate == true) {
				phrase = new Phrase("DUPLICATE", fontBlack10Bold);
				cell = new PdfPCell(phrase);
				cell.setPaddingTop(5);
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell.setBorder(Rectangle.NO_BORDER);
				table.addCell(cell);
				nHeight += 20;
			}
			
			if(!m_bFutureOrder)
			{
				phrase = new Phrase("TAX INVOICE", fontBlack10Bold);
			}
			else
			{
				phrase = new Phrase("ORDER CONFIRMATION", fontBlack10Bold);
			}
			cell = new PdfPCell(phrase);
			cell.setPaddingTop(5);
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell.setBorder(Rectangle.NO_BORDER);
			table.addCell(cell);
			nHeight += 20;

			if(!m_bFutureOrder)
			{
				phrase = new Phrase("INVOICE # " + Global.gUOrderHInfo.strDocNo, fontBlack7Normal);
			}
			else
			{
				phrase = new Phrase("ORDER # " + Global.gUOrderHInfo.strDocNo, fontBlack7Normal);
			}
			cell = new PdfPCell(phrase);
			cell.setPaddingTop(4);
			cell.setBorder(Rectangle.NO_BORDER);
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			table.addCell(cell);
			nHeight += 20;
			
			if(!m_bFutureOrder)
			{
				phrase = new Phrase("Date:   " + new SimpleDateFormat(Utils.getDateFormat(mContext, "dd/MM/yyyy")).format(mTrxnDate), fontBlack7Normal);
			}
			else
			{
				phrase = new Phrase("Date:   " + new SimpleDateFormat(Utils.getDateFormat(mContext, "dd/MM/yyyy")).format(mDate), fontBlack7Normal);
			}
			cell = new PdfPCell(phrase);
			cell.setBorder(Rectangle.NO_BORDER);
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell.setPaddingBottom(8);
			table.addCell(cell);
			nHeight += 20;
/*			phrase = new Phrase("Trip#   " + Global.gTripNumber, fontBlack7Normal);
			table.addCell(phrase);*/

			phrase = new Phrase("", fontBlack7Bold);
			cell = new PdfPCell(phrase);
			cell.setPaddingTop(5);
			cell.setBorder(Rectangle.NO_BORDER);
			table.addCell(cell);
			
			for (DocTextInfo info : mDocTextList) {
				if (info.nTextType == 6) {
					if( info.strDocText.length() == 0 )
					{
						phrase = new Phrase("  ", fontBlack7Bold);
						cell = new PdfPCell(phrase);
						cell.setBorder(Rectangle.NO_BORDER);
						table.addCell(cell);
					}
					else
					{
						phrase = new Phrase(info.strDocText, fontBlack7Bold);
						cell = new PdfPCell(phrase);
						cell.setBorder(Rectangle.NO_BORDER);
						table.addCell(cell);
					}
					nHeight += 18;
				}
			}

			phrase = new Phrase("Invoice to", fontBlack7Bold);
			cell = new PdfPCell(phrase);
			cell.setPaddingTop(8);
			cell.setBorder(Rectangle.NO_BORDER);
			nHeight += 18;
			table.addCell(cell);
			if( Global.gCurCustomerInfo.strAltCustNo.equals("") )
			{
				if( Global.gCurCustomerInfo.nChainNo == 0 )
				{
					phrase = new Phrase("Customer ID:   " + Global.gCurCustomerInfo.nCustNo, fontBlack7Normal);
				}
				else
				{
					phrase = new Phrase("Customer ID:   " + Global.gCurCustomerInfo.nChainNo + " / " + Global.gCurCustomerInfo.nCustNo, fontBlack7Normal);
				}
			}
			else
			{
				phrase = new Phrase("Customer ID:   " + Global.gCurCustomerInfo.strAltCustNo, fontBlack7Normal);
			}
			nHeight += 18;
			table.addCell(phrase);
			if( mInvoiceToInfo.strBillName == null || mInvoiceToInfo.strBillName.trim().length() == 0 )
				phrase = new Phrase(mInvoiceToInfo.strName, fontBlack7Normal);
			else
				phrase = new Phrase(mInvoiceToInfo.strBillName, fontBlack7Normal);
			nHeight += 15;
			table.addCell(phrase);
			
			if( mInvoiceToInfo.strBillAddr1 == null || mInvoiceToInfo.strBillAddr1.trim().length() == 0 )
				phrase = new Phrase(mInvoiceToInfo.strAddress1, fontBlack7Normal);
			else
				phrase = new Phrase(mInvoiceToInfo.strBillAddr1, fontBlack7Normal);
			nHeight += 15;
			table.addCell(phrase);
			
			if( mInvoiceToInfo.strBillAddr2 == null || mInvoiceToInfo.strBillAddr2.trim().length() == 0 )
				phrase = new Phrase(mInvoiceToInfo.strAddress2, fontBlack7Normal);
			else
				phrase = new Phrase(mInvoiceToInfo.strBillAddr2, fontBlack7Normal);
			nHeight += 15;
			table.addCell(phrase);
			
//			if( mInvoiceToInfo.strBillCity == null || mInvoiceToInfo.strBillCity.trim().length() == 0 )
				phrase = new Phrase(mInvoiceToInfo.strCity + " " + mInvoiceToInfo.strState + " " + mInvoiceToInfo.strZip, fontBlack7Normal);
//			else
//				phrase = new Phrase(mInvoiceToInfo.strBillCity + " " + mInvoiceToInfo.strState + " " + mInvoiceToInfo.strZip, fontBlack7Normal);
//			phrase = new Phrase(Global.gCurCustomerInfo.strBillCity + " " + Global.gCurCustomerInfo.strState + " " + Global.gCurCustomerInfo.strZip, fontBlack7Normal);
			table.addCell(phrase);
			nHeight += 15;
			
			if (mDeliverToInfo != null && !mDeliverToInfo.compare(mInvoiceToInfo)) {
				phrase = new Phrase("Deliver to", fontBlack7Bold);
				cell = new PdfPCell(phrase);
				cell.setPaddingTop(5);
				nHeight += 20;
				cell.setBorder(Rectangle.NO_BORDER);
				table.addCell(cell);
				if( Global.gCurCustomerInfo.strAltCustNo.equals("") )
				{
					if( Global.gCurCustomerInfo.nChainNo == 0 )
					{
						phrase = new Phrase("Customer ID:   "  + Global.gCurCustomerInfo.nCustNo, fontBlack7Normal);
					}
					else
					{
						phrase = new Phrase("Customer ID:   " + Global.gCurCustomerInfo.nChainNo + " / " + Global.gCurCustomerInfo.nCustNo, fontBlack7Normal);
					}
				}
				else
				{
					phrase = new Phrase("Customer ID:   "  + Global.gCurCustomerInfo.strAltCustNo, fontBlack7Normal);
				}
				nHeight += 20;
				table.addCell(phrase);
				
				phrase = new Phrase(mDeliverToInfo.strName, fontBlack7Normal);
				nHeight += 20;
				table.addCell(phrase);
				nHeight += 20;
				phrase = new Phrase(Global.gCurCustomerInfo.strAddress1, fontBlack7Normal);
				table.addCell(phrase);
				
				if(Global.gCurCustomerInfo.strAddress2.length() > 0)
				{
					phrase = new Phrase(Global.gCurCustomerInfo.strAddress2, fontBlack7Normal);
					table.addCell(phrase);
				}
				nHeight += 20;
				
				
				phrase = new Phrase(Global.gCurCustomerInfo.strCity + " " + Global.gCurCustomerInfo.strShipToState + " " + Global.gCurCustomerInfo.strShipToZip, fontBlack7Normal);
				nHeight += 20;
				table.addCell(phrase);
			}
			
			phrase = new Phrase("PO Reference:", fontBlack7Bold);
			cell = new PdfPCell(phrase);
			nHeight += 20;
			cell.setPaddingTop(8);
			cell.setBorder(Rectangle.NO_BORDER);
			table.addCell(cell);
			phrase = new Phrase(Global.gUOrderHInfo.strPOReference, fontBlack7Normal);
			nHeight += 20;
			table.addCell(phrase);
			
			for (DocTextInfo info : mDocTextList) {
				if (info.nTextType == 3) {
					phrase = new Phrase(info.strDocText, fontBlack7Bold);
					table.addCell(phrase);
					nHeight += 20;
				}
			}

			phrase = new Phrase("   ", fontBlack7Normal);
			table.addCell(phrase);
			phrase = new Phrase("", fontBlack7Normal);
			cell = new PdfPCell(phrase);
			cell.setBorder(Rectangle.BOTTOM);
			table.addCell(cell);
			nHeight += 40;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return table;
	}

	private String getFormattedPrice(float nPrice) {
		String strResult = String.format(Locale.ENGLISH, "%s" + Global.gCurrencySymbol + "%." + Utils.getIntSetting(mContext, Global.PREF_DECIMAL) + "f", mIsPickup == false ? "" : "-", Math.abs(nPrice));
		if (Global.gCurCustomerInfo.nInvoiceFmt > 0) {
			strResult = String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(mContext, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(nPrice));
		}
		return strResult;
	}

	private PdfPTable createBody() {
		PdfPTable table = new PdfPTable(4);
		PdfPCell cell;
		Phrase phrase;
		try {
			table.setWidths(new int[]{1, 2, 2, 3});
			table.getDefaultCell().setBorder(Rectangle.NO_BORDER);

			for (UOrderDInfo info : mPrintList) {
				int nQuantity = (info.nTrxnType == Global.TRXN_DELIVER || info.nTrxnType == Global.TRXN_FREE || info.nTrxnType == Global.TRXN_SAMPLE)  ? info.nSales : info.nDumps;
/*				
				phrase = new Phrase(info.strItemNo, fontBlack7Normal);
				cell = new PdfPCell(phrase);
				cell.setColspan(2);
				cell.setBorder(Rectangle.NO_BORDER);
				table.addCell(cell);*/
				
				phrase = new Phrase(info.strShortDesc + (info.strItemTaxCode.length() > 0 ? " *" : ""), fontBlack7Normal);
				cell = new PdfPCell(phrase);
				cell.setColspan(4);
				cell.setBorder(Rectangle.NO_BORDER);
				table.addCell(cell);

				phrase = new Phrase(String.format(Locale.ENGLISH, "%d @ " + Global.gCurrencySymbol + "%." + Utils.getIntSetting(mContext, Global.PREF_DECIMAL) + "f", nQuantity, info.nPrice), fontBlack7Normal);
				cell = new PdfPCell(phrase);
				cell.setColspan(3);
				cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				cell.setBorder(Rectangle.NO_BORDER);
				table.addCell(cell);
				phrase = new Phrase(String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(mContext, Global.PREF_DECIMAL) + "f", (nQuantity * info.nPrice)), fontBlack7Normal);
				cell = new PdfPCell(phrase);
				cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				cell.setBorder(Rectangle.NO_BORDER);
				table.addCell(cell);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return table;
	}
	
	private PdfPTable createA4Item(List<UOrderDInfo> item)
	{
		PdfPTable table = new PdfPTable(8);
		PdfPCell cell;
		Phrase phrase;
		try{
			table.setWidths(new int[]{3, 12, 3, 3, 3, 3, 3, 4});

			phrase = new Phrase("Code", fontBlack9Bold);
			cell = new PdfPCell(phrase);
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell.setPadding(3);
			table.addCell(cell);

			phrase = new Phrase("Description", fontBlack9Bold);
			cell = new PdfPCell(phrase);
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell.setPadding(3);
			table.addCell(cell);

			phrase = new Phrase("Quantity", fontBlack9Bold);
			cell = new PdfPCell(phrase);
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell.setPadding(3);
			table.addCell(cell);

			phrase = new Phrase("Unit Price", fontBlack9Bold);
			cell = new PdfPCell(phrase);
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell.setPadding(3);
			table.addCell(cell);

			phrase = new Phrase("CDS Levy", fontBlack9Bold);
			cell = new PdfPCell(phrase);
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell.setPadding(3);
			table.addCell(cell);

			phrase = new Phrase("Amount", fontBlack9Bold);
			cell = new PdfPCell(phrase);
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell.setPadding(3);
			table.addCell(cell);

			phrase = new Phrase("GST", fontBlack9Bold);
			cell = new PdfPCell(phrase);
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell.setPadding(3);
			table.addCell(cell);

			phrase = new Phrase("Total Amount", fontBlack9Bold);
			cell = new PdfPCell(phrase);
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell.setPadding(3);
			table.addCell(cell);

			for(UOrderDInfo info : item)
			{

				DbAdapter dbAdapter = new DbAdapter(mContext);
				dbAdapter.open();
				ProdDetlInfo prodDetlInfo = dbAdapter.getProdDetl(info.strItemNo);
				dbAdapter.close();

				int nConsumerUnit = 1;
				if(prodDetlInfo != null)
				{
					nConsumerUnit = prodDetlInfo.nConsumerUnit;
				}

				if(info.nTrxnType == Global.TRXN_FREE)
				{
					phrase = new Phrase(" ", fontBlack9Normal);
					cell = new PdfPCell(phrase);
					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					cell.setPadding(3);
					table.addCell(cell);

					phrase = new Phrase("Free Item", fontBlack9Normal);
					cell = new PdfPCell(phrase);
					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					cell.setPadding(3);
					table.addCell(cell);

					phrase = new Phrase(String.valueOf(info.nQuantity), fontBlack9Normal);
					cell = new PdfPCell(phrase);
					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					cell.setPadding(3);
					table.addCell(cell);

					phrase = new Phrase("$0.00", fontBlack9Normal);
					cell = new PdfPCell(phrase);
					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					cell.setPadding(3);
					table.addCell(cell);

					phrase = new Phrase("$0.00", fontBlack9Normal);
					cell = new PdfPCell(phrase);
					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					cell.setPadding(3);
					table.addCell(cell);

					phrase = new Phrase("$0.00", fontBlack9Normal);
					cell = new PdfPCell(phrase);
					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					cell.setPadding(3);
					table.addCell(cell);

					phrase = new Phrase("$0.00", fontBlack9Normal);
					cell = new PdfPCell(phrase);
					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					cell.setPadding(3);
					table.addCell(cell);

					phrase = new Phrase("$0.00", fontBlack9Normal);
					cell = new PdfPCell(phrase);
					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					cell.setPadding(3);
					table.addCell(cell);
				}
				else if(info.nSCWType == Global.INVENTORY_CONTAINERS)
				{
					phrase = new Phrase(info.strItemNo.length() > 0 ? info.strItemNo : " ", fontBlack9Normal);
					cell = new PdfPCell(phrase);
					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					cell.setPadding(3);
					table.addCell(cell);

					phrase = new Phrase(info.strDescription.length() > 0 ? info.strDescription : " ", fontBlack9Normal);
					cell = new PdfPCell(phrase);
					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					cell.setPadding(3);
					table.addCell(cell);

					if( info.nTrxnType == Global.TRXN_CONTAINER_DUMP)
					{
						phrase = new Phrase(String.valueOf(-1 * info.nQuantity), fontBlack9Normal);
					}
					else
					{
						phrase = new Phrase(String.valueOf(info.nQuantity), fontBlack9Normal);
					}
					cell = new PdfPCell(phrase);
					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					cell.setPadding(3);
					table.addCell(cell);

					phrase = new Phrase(" ", fontBlack9Normal);
					cell = new PdfPCell(phrase);
					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					cell.setPadding(3);
					table.addCell(cell);

					phrase = new Phrase(" ", fontBlack9Normal);
					cell = new PdfPCell(phrase);
					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					cell.setPadding(3);
					table.addCell(cell);

					phrase = new Phrase(" ", fontBlack9Normal);
					cell = new PdfPCell(phrase);
					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					cell.setPadding(3);
					table.addCell(cell);

					phrase = new Phrase(" ", fontBlack9Normal);
					cell = new PdfPCell(phrase);
					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					cell.setPadding(3);
					table.addCell(cell);

					phrase = new Phrase(" ", fontBlack9Normal);
					cell = new PdfPCell(phrase);
					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					cell.setPadding(3);
					table.addCell(cell);
				}
				else if(info.nTrxnType == Global.TRXN_PICKUP)
				{
					phrase = new Phrase(info.strItemNo.length() > 0 ? info.strItemNo : " ", fontBlack9Normal);
					cell = new PdfPCell(phrase);
					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					cell.setPadding(3);
					table.addCell(cell);

					phrase = new Phrase(info.strDescription.length() > 0 ? info.strDescription : " ", fontBlack9Normal);
					cell = new PdfPCell(phrase);
					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					cell.setPadding(3);
					table.addCell(cell);

					phrase = new Phrase(String.valueOf(info.nQuantity), fontBlack9Normal);
					cell = new PdfPCell(phrase);
					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					cell.setPadding(3);
					table.addCell(cell);
					float nPrice = info.nBasePrice;
					/*for(UPromotionInfo uPromotionInfo : info.uPromotionInfoList)
					{
						if( uPromotionInfo.nPromoAppMethod == 4 )
							nPrice += Utils.getDividedValue(uPromotionInfo.nAmount);
					}*/
					if( nPrice != 0 )
						phrase = new Phrase(String.format(Locale.ENGLISH, "-$%.2f", nPrice), fontBlack9Normal);
					else
						phrase = new Phrase(String.format(Locale.ENGLISH, "$%.2f", nPrice), fontBlack9Normal);
					cell = new PdfPCell(phrase);
					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					cell.setPadding(3);
					table.addCell(cell);

					if( info.nCDSRate != 0 )
					{
						phrase = new Phrase(String.format(Locale.ENGLISH, "-$%.2f", Utils.getDividedValue(info.nCDSRate)), fontBlack9Normal);
					}
					else
					{
						phrase = new Phrase(String.format(Locale.ENGLISH, "$%.2f", Utils.getDividedValue(info.nCDSRate)), fontBlack9Normal);
					}
					cell = new PdfPCell(phrase);
					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					cell.setPadding(3);
					table.addCell(cell);


					if( (nPrice + Utils.get2DecimalValue(Utils.getDividedValue(info.nCDSRate))) * info.nQuantity != 0 )
						phrase = new Phrase(String.format(Locale.ENGLISH, "-$%.2f", (nPrice + Utils.getDividedValue(info.nCDSRate) * nConsumerUnit) * info.nQuantity ), fontBlack9Normal);
					else
						phrase = new Phrase(String.format(Locale.ENGLISH, "$%.2f", (nPrice + Utils.getDividedValue(info.nCDSRate) * nConsumerUnit) * info.nQuantity * nConsumerUnit), fontBlack9Normal);
					cell = new PdfPCell(phrase);
					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					cell.setPadding(3);
					table.addCell(cell);

					float fGSTAmount = info.nPrintTaxAmount - info.nPrintCDSAmount;

					if( fGSTAmount != 0 )
						phrase = new Phrase(String.format(Locale.ENGLISH, "-$%.2f", fGSTAmount), fontBlack9Normal);
					else
						phrase = new Phrase(String.format(Locale.ENGLISH, "$%.2f", fGSTAmount), fontBlack9Normal);
					cell = new PdfPCell(phrase);
					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					cell.setPadding(3);
					table.addCell(cell);

					float nPrintAmount = info.nPrice * info.nQuantity + fGSTAmount + Utils.getDividedValue(info.nCDSRate) * info.nQuantity * nConsumerUnit;

					if(nPrintAmount != 0 )
						phrase = new Phrase(String.format(Locale.ENGLISH, "-$%.2f", nPrintAmount), fontBlack9Normal);
					else
						phrase = new Phrase(String.format(Locale.ENGLISH, "$%.2f", nPrintAmount), fontBlack9Normal);
					cell = new PdfPCell(phrase);
					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					cell.setPadding(3);
					table.addCell(cell);
				}
				else
				{
					phrase = new Phrase(info.strItemNo.length() > 0 ? info.strItemNo : " ", fontBlack9Normal);
					cell = new PdfPCell(phrase);
					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					cell.setPadding(3);
					table.addCell(cell);

					phrase = new Phrase(info.strDescription.length() > 0 ? info.strDescription : " ", fontBlack9Normal);
					cell = new PdfPCell(phrase);
					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					cell.setPadding(3);
					table.addCell(cell);

					phrase = new Phrase(String.valueOf(info.nQuantity), fontBlack9Normal);
					cell = new PdfPCell(phrase);
					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					cell.setPadding(3);
					table.addCell(cell);

					float nPrice = info.nBasePrice;
					/*for(UPromotionInfo uPromotionInfo : info.uPromotionInfoList)
					{
						if( uPromotionInfo.nPromoAppMethod == 4 )
							nPrice += Utils.getDividedValue(uPromotionInfo.nAmount);
					}*/

					phrase = new Phrase(String.format(Locale.ENGLISH, "$%.2f", nPrice), fontBlack9Normal);
					cell = new PdfPCell(phrase);
					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					cell.setPadding(3);
					table.addCell(cell);

					phrase = new Phrase(String.format(Locale.ENGLISH, "$%.2f", Utils.getDividedValue(info.nCDSRate)), fontBlack9Normal);
					cell = new PdfPCell(phrase);
					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					cell.setPadding(3);
					table.addCell(cell);

					phrase = new Phrase(String.format(Locale.ENGLISH, "$%.2f", (nPrice + Utils.getDividedValue(info.nCDSRate) * nConsumerUnit) * info.nQuantity), fontBlack9Normal);
					cell = new PdfPCell(phrase);
					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					cell.setPadding(3);
					table.addCell(cell);

					float fGst = info.nPrintTaxAmount - info.nPrintCDSAmount;

					phrase = new Phrase(String.format(Locale.ENGLISH, "$%.2f", fGst ), fontBlack9Normal);
					cell = new PdfPCell(phrase);
					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					cell.setPadding(3);
					table.addCell(cell);

					float fTotalAmount = nPrice * info.nQuantity + fGst + Utils.getDividedValue(info.nCDSRate) * info.nQuantity * nConsumerUnit;

					phrase = new Phrase(String.format(Locale.ENGLISH, "$%.2f", fTotalAmount), fontBlack9Normal);
					cell = new PdfPCell(phrase);
					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					cell.setPadding(3);
					table.addCell(cell);
				}
			}
		}catch(Exception e)
		{
			e.printStackTrace();
		}

		return table;
	}
	
	private PdfPTable createA4BodyFooter()
	{
		PdfPTable table = new PdfPTable(2);
		PdfPCell cell;
		Phrase phrase;
		try{
			table.setWidths(new int[]{4, 6});
			table.getDefaultCell().setBorder(Rectangle.NO_BORDER);

			PdfPTable table_sub = new PdfPTable(1);
			table_sub.setWidths(new int[]{1});

			if (Global.gUOrderHInfo.strSignatureFilePath.length() > 0 && Utils.isFileExist(Global.gUOrderHInfo.strSignatureFilePath) == true) {
				Image image = Image.getInstance(Global.gUOrderHInfo.strSignatureFilePath);
				image.scaleToFit(60, 60);
				cell = new PdfPCell(image);
				cell.setBorder(Rectangle.NO_BORDER);
				cell.setPaddingTop(5);
				cell.setPaddingBottom(5);
			} else {
				phrase = new Phrase("No Signature", fontBlack10Bold);
				cell = new PdfPCell(phrase);
				cell.setPaddingTop(30);
				cell.setPaddingBottom(30);
				cell.setBorder(Rectangle.NO_BORDER);
			}

			cell.setHorizontalAlignment(Element.ALIGN_LEFT);
			cell.setVerticalAlignment(Element.ALIGN_CENTER);
			cell.setBorder(Rectangle.NO_BORDER);
			table_sub.addCell(cell);

			phrase = new Phrase("Received by: " + Global.gUOrderHInfo.strOrderName, fontBlack9Bold);
			cell = new PdfPCell(phrase);
			cell.setHorizontalAlignment(Element.ALIGN_LEFT);

			cell.setBorder(Rectangle.NO_BORDER);
			table_sub.addCell(cell);
			cell = new PdfPCell(table_sub);
			cell.setPaddingLeft(20);
			cell.setPaddingBottom(5);
			table.addCell(cell);

			table_sub = new PdfPTable(2);
			table_sub.setWidths(new int[]{4,1});
			table_sub.getDefaultCell().setBorder(Rectangle.NO_BORDER);
			List<PrintTaxRateInfo> taxRateInfoList = new ArrayList<>();

			float cdsAmount = 0;
			for (Map.Entry<String, PrintTaxRateInfo> entry : Global.gUOrderHInfo.cdsMap.entrySet())
			{

				PrintTaxRateInfo taxRateInfo = entry.getValue();
				if( taxRateInfo.strTaxCode != null && taxRateInfo.strTaxCode.length() > 0 && !taxRateInfo.strTaxCode.equals("0") && taxRateInfo.nBottles != 0)
				{
					taxRateInfoList.add(taxRateInfo);
				}
			}

			Collections.sort(taxRateInfoList, new Comparator<PrintTaxRateInfo>() {
				@Override
				public int compare(PrintTaxRateInfo printTaxRateInfo, PrintTaxRateInfo t1) {
					return printTaxRateInfo.strTaxCode.compareTo(t1.strTaxCode);
				}
			});

			for(PrintTaxRateInfo taxRateInfo : taxRateInfoList)
			{
				phrase = new Phrase(String.format(Locale.ENGLISH, "%d bottles @ %s", taxRateInfo.nBottles, taxRateInfo.strRateDescription), fontBlack9Normal);
				cell = new PdfPCell(phrase);
				cell.setBorder(Rectangle.NO_BORDER);
				table_sub.addCell(cell);

				phrase = new Phrase(getFormattedPrice(Utils.get2DecimalValue(taxRateInfo.price) * taxRateInfo.nBottles), fontBlack9Normal);
				cell = new PdfPCell(phrase);
				cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				cell.setBorder(Rectangle.NO_BORDER);
				table_sub.addCell(cell);

				cdsAmount += Utils.get2DecimalValue(taxRateInfo.price) * taxRateInfo.nBottles;
			}



			phrase = new Phrase("Sub Total", fontBlack9Normal);
			cell = new PdfPCell(phrase);
			cell.setBorder(Rectangle.NO_BORDER);
			table_sub.addCell(cell);

			float nSalesAmount = Global.gUOrderHInfo.nSaleAmount + Global.gUOrderHInfo.nDiscSales;

			phrase = new Phrase(getFormattedPrice(nSalesAmount), fontBlack9Normal);
			cell = new PdfPCell(phrase);
			cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
			cell.setBorder(Rectangle.NO_BORDER);
			table_sub.addCell(cell);

			phrase = new Phrase("Shipping Cost(optional)", fontBlack9Normal);
			cell = new PdfPCell(phrase);
			cell.setBorder(Rectangle.NO_BORDER);
			table_sub.addCell(cell);

			phrase = new Phrase(getFormattedPrice(Global.gUOrderHInfo.nHandlingFee), fontBlack9Normal);
			cell = new PdfPCell(phrase);
			cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
			cell.setBorder(Rectangle.NO_BORDER);
			table_sub.addCell(cell);

			phrase = new Phrase("GST", fontBlack9Normal);
			cell = new PdfPCell(phrase);
			cell.setBorder(Rectangle.NO_BORDER);
			table_sub.addCell(cell);

			phrase = new Phrase(getFormattedPrice(Global.gUOrderHInfo.nTaxAmount - Global.gUOrderHInfo.nPrintCDSAmount + Global.gUOrderHInfo.nHandlingFeeTaxAmount), fontBlack9Normal);
			cell = new PdfPCell(phrase);
			cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
			cell.setBorder(Rectangle.NO_BORDER);
			table_sub.addCell(cell);

			int nPayType = Utils.parseIntFromString(Global.gCurCustomerInfo.strPayType);
			if( Utils.getStringSetting(mContext, Global.PREF_ROUTE_CENTSROUND).trim().equals("1") && (nPayType == 1 || nPayType == 2))
			{
				int value = (int)((nSalesAmount + Global.gUOrderHInfo.nHandlingFee + Global.gUOrderHInfo.nHandlingFeeTaxAmount + Global.gUOrderHInfo.nTaxAmount - Global.gUOrderHInfo.nPrintCDSAmount + cdsAmount) * Global.MULTIPLE_VALUE);
				int nTotal = Math.abs(value);
				int nOffSet = nTotal % 5000;
				int nInvoiceTotal = ((nTotal / 5000) + ((nOffSet >= 2500) ? 1 : 0)) * 5000;
				nOffSet = nTotal % 1000;
				nTotal = ((nTotal / 1000) + ((nOffSet >= 500) ? 1 : 0)) * 1000;
				nOffSet = nInvoiceTotal - nTotal;
				float dOffSet = nOffSet / (Global.MULTIPLE_VALUE * 1f);
				float dTotal = nTotal / (Global.MULTIPLE_VALUE * 1f);

				phrase = new Phrase("Rounding", fontBlack9Normal);
				cell = new PdfPCell(phrase);
				cell.setBorder(Rectangle.BOTTOM);
				table_sub.addCell(cell);

				String strOffSet= "";
				strOffSet = String.format(Locale.ENGLISH, "%.2f", Math.abs(dOffSet));
				if(value < 0)
				{
					dOffSet *= -1.0f;
				}

				if(dOffSet >= 0f)
					strOffSet = "$" + strOffSet.substring(0);
				else
					strOffSet = "-$" + strOffSet.substring(0);
				phrase = new Phrase(strOffSet, fontBlack9Normal);
				cell = new PdfPCell(phrase);
				cell.setBorder(Rectangle.BOTTOM);
				cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				table_sub.addCell(cell);

				phrase = new Phrase("Total", fontBlack9Bold);
				cell = new PdfPCell(phrase);
				cell.setBorder(Rectangle.BOTTOM);
				table_sub.addCell(cell);

				if( value < 0 )
					nInvoiceTotal *= -1;
				phrase = new Phrase(getFormattedPrice(nInvoiceTotal / (Global.MULTIPLE_VALUE * 1f)), fontBlack9Bold);
				cell = new PdfPCell(phrase);
				cell.setBorder(Rectangle.BOTTOM);
				cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				table_sub.addCell(cell);
			}
			else
			{
				phrase = new Phrase("Total", fontBlack9Bold);
				cell = new PdfPCell(phrase);
				cell.setBorder(Rectangle.BOTTOM);
				table_sub.addCell(cell);

				phrase = new Phrase(getFormattedPrice(nSalesAmount + Global.gUOrderHInfo.nHandlingFee + Global.gUOrderHInfo.nHandlingFeeTaxAmount + Global.gUOrderHInfo.nTaxAmount), fontBlack9Bold);
				cell = new PdfPCell(phrase);
				cell.setBorder(Rectangle.BOTTOM);
				cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				table_sub.addCell(cell);
			}

			if (mPayMethod == 0 || m_bFutureOrder)
			{
				phrase = new Phrase("Paid", fontBlack9Normal);
				cell = new PdfPCell(phrase);
				cell.setBorder(Rectangle.NO_BORDER);
				table_sub.addCell(cell);
				phrase = new Phrase(getFormattedPrice(0), fontBlack9Normal);
				cell = new PdfPCell(phrase);
				cell.setBorder(Rectangle.NO_BORDER);
				cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				table_sub.addCell(cell);

				{
					phrase = new Phrase("Balance Due", fontBlack9Bold);
					cell = new PdfPCell(phrase);
					cell.setPaddingTop(5);
					cell.setBorder(Rectangle.NO_BORDER);
					table_sub.addCell(cell);
					if( Utils.getStringSetting(mContext, Global.PREF_ROUTE_CENTSROUND).trim().equals("1") && (nPayType == 1 || nPayType == 2))
					{
						int nValue = (int)((nSalesAmount + Global.gUOrderHInfo.nHandlingFee + Global.gUOrderHInfo.nHandlingFeeTaxAmount +Global.gUOrderHInfo.nTaxAmount - Global.gUOrderHInfo.nPrintCDSAmount + cdsAmount) * Global.MULTIPLE_VALUE);;
						int nTotal = Math.abs(nValue);
						int nOffSet = nTotal % 5000;
						int nInvoiceTotal = ((nTotal / 5000) + ((nOffSet >= 2500) ? 1 : 0)) * 5000;
						nOffSet = nTotal % 1000;
						nTotal = ((nTotal / 1000) + ((nOffSet >= 500) ? 1 : 0)) * 1000;
						nOffSet = nInvoiceTotal - nTotal;
						if( nValue < 0 )
						{
							nInvoiceTotal *= -1;
						}

						phrase = new Phrase(getFormattedPrice(nInvoiceTotal / (Global.MULTIPLE_VALUE * 1f)), fontBlack9Bold);
						cell = new PdfPCell(phrase);
						cell.setPaddingTop(5);
						cell.setBorder(Rectangle.NO_BORDER);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						table_sub.addCell(cell);
					}
					else
					{
						phrase = new Phrase(getFormattedPrice(nSalesAmount + Global.gUOrderHInfo.nHandlingFee + Global.gUOrderHInfo.nHandlingFeeTaxAmount + Global.gUOrderHInfo.nTaxAmount), fontBlack9Bold);
						cell = new PdfPCell(phrase);
						cell.setPaddingTop(5);
						cell.setBorder(Rectangle.NO_BORDER);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						table_sub.addCell(cell);
					}
				}
			}
			else
			{
				{
					phrase = new Phrase("Paid", fontBlack9Bold);
					cell = new PdfPCell(phrase);
					cell.setPaddingTop(5);
					cell.setBorder(Rectangle.NO_BORDER);
					table_sub.addCell(cell);

					if( Utils.getStringSetting(mContext, Global.PREF_ROUTE_CENTSROUND).trim().equals("1") && (nPayType == 1 || nPayType == 2))
					{
						int nValue = (int)((nSalesAmount + Global.gUOrderHInfo.nHandlingFee + Global.gUOrderHInfo.nHandlingFeeTaxAmount + Global.gUOrderHInfo.nTaxAmount) * Global.MULTIPLE_VALUE);
						int nTotal = Math.abs(nValue);
						int nOffSet = nTotal % 5000;
						int nInvoiceTotal = ((nTotal / 5000) + ((nOffSet >= 2500) ? 1 : 0)) * 5000;
						nOffSet = nTotal % 1000;
						nTotal = ((nTotal / 1000) + ((nOffSet >= 500) ? 1 : 0)) * 1000;
						nOffSet = nInvoiceTotal - nTotal;
						float dOffSet = nOffSet / (Global.MULTIPLE_VALUE * 1f);
						float dTotal = nTotal / (Global.MULTIPLE_VALUE * 1f);

						if( nValue < 0 )
						{
							nInvoiceTotal *= -1;
						}
						phrase = new Phrase(getFormattedPrice(nInvoiceTotal / (Global.MULTIPLE_VALUE * 1f)), fontBlack9Bold);
						cell = new PdfPCell(phrase);
						cell.setPaddingTop(5);
						cell.setBorder(Rectangle.NO_BORDER);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						table_sub.addCell(cell);
					}
					else
					{
						phrase = new Phrase(getFormattedPrice(nSalesAmount + Global.gUOrderHInfo.nHandlingFee + Global.gUOrderHInfo.nHandlingFeeTaxAmount + Global.gUOrderHInfo.nTaxAmount), fontBlack9Bold);
						cell = new PdfPCell(phrase);
						cell.setPaddingTop(5);
						cell.setBorder(Rectangle.NO_BORDER);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						table_sub.addCell(cell);
					}
				}
				phrase = new Phrase("Balance Due", fontBlack9Normal);
				cell = new PdfPCell(phrase);
				cell.setBorder(Rectangle.NO_BORDER);
				table_sub.addCell(cell);
				phrase = new Phrase(getFormattedPrice(0), fontBlack9Normal);
				cell = new PdfPCell(phrase);
				cell.setBorder(Rectangle.NO_BORDER);
				cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				table_sub.addCell(cell);
			}

			cell = new PdfPCell(table_sub);
			cell.setBorder(Rectangle.NO_BORDER);
			cell.setPaddingBottom(3);
			cell.setPaddingLeft(50);
			cell.setPaddingRight(50);
			table.addCell(cell);


		}catch(Exception e)
		{
			e.printStackTrace();
		}
		return table;
	}
	
	private PdfPTable createA4Body(List<UOrderDInfo> deliverItem, List<UOrderDInfo> replaceItem, List<UOrderDInfo> buybackItem, List<UOrderDInfo> containerItem, boolean bContinous, boolean bShowFooter)
	{
		PdfPTable tableAll = new PdfPTable(1);
		PdfPTable table = new PdfPTable(1);
		PdfPCell cell;
		Phrase phrase;
		try{
			table.setWidths(new int[]{1});
			tableAll.setWidths(new int[]{1});
			table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
			float fItemHeight = 0f;
			if(deliverItem != null && deliverItem.size() > 0)
			{
				phrase = new Phrase("DELIVERED", fontBlack9Bold);
				cell = new PdfPCell(phrase);
				cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
				cell.setHorizontalAlignment(Element.ALIGN_LEFT);
				cell.setPadding(3);
				table.addCell(cell);
				fItemHeight += 15.5;
				
				cell = new PdfPCell(createA4Item(deliverItem));
				table.addCell(cell);
				fItemHeight += 15.5 * (deliverItem.size() + 1);
			}
			
			if(replaceItem != null && replaceItem.size() > 0)
			{
				phrase = new Phrase("DUMPS", fontBlack9Bold);
				cell = new PdfPCell(phrase);
				cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
				cell.setHorizontalAlignment(Element.ALIGN_LEFT);
				cell.setPadding(3);
				table.addCell(cell);
				fItemHeight += 15.5;
				
				cell = new PdfPCell(createA4Item(replaceItem));
				table.addCell(cell);
				fItemHeight += 15.5 * (replaceItem.size() + 1);
			}

			if(buybackItem != null && buybackItem.size() > 0)
			{
				phrase = new Phrase("BUYBACKS", fontBlack9Bold);
				cell = new PdfPCell(phrase);
				cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
				cell.setHorizontalAlignment(Element.ALIGN_LEFT);
				cell.setPadding(3);
				table.addCell(cell);
				fItemHeight += 15.5;

				cell = new PdfPCell(createA4Item(buybackItem));
				table.addCell(cell);
				fItemHeight += 15.5 * (buybackItem.size() + 1);
			}
			
			if(containerItem != null && containerItem.size() > 0)
			{
				phrase = new Phrase("CONTAINERS", fontBlack9Bold);
				cell = new PdfPCell(phrase);
				cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
				cell.setHorizontalAlignment(Element.ALIGN_LEFT);
				cell.setPadding(3);
				table.addCell(cell);
				fItemHeight += 15.5;
				
				cell = new PdfPCell(createA4Item(containerItem));
				table.addCell(cell);
				fItemHeight += 15.5 * (containerItem.size() + 1);
			}
			
			
			if(bShowFooter)
			{
				cell = new PdfPCell(createA4BodyFooter());
				boolean bShowPaymentSection = false;
				if (mPayMethod == 0 || m_bFutureOrder)
					bShowPaymentSection = true;
//				boolean bShowPaymentSection = true;
				
				int nTotalHeight = bShowPaymentSection ? 280 : 400;
				
				cell.setPaddingTop(nTotalHeight - fItemHeight);
				cell.setBorder(Rectangle.NO_BORDER);
				table.addCell(cell);
			}
			cell = new PdfPCell(table);
			tableAll.addCell(cell);
			
		}catch(Exception e)
		{
			e.printStackTrace();
		}		
		return tableAll;
	}
	
	private PdfPTable createBody_InvoiceFmt(Integer nHeight) {
		PdfPTable table = new PdfPTable(4);
		PdfPCell cell;
		Phrase phrase;
		try {
			table.setWidths(new int[]{1, 2, 2, 3});
			table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
			if (mDeliveredCnt > 0) {
				phrase = new Phrase("DELIVERED", fontBlack7Bold);
				nHeight += 20;
				cell = new PdfPCell(phrase);
				cell.setColspan(4);
				cell.setPaddingTop(5);
				cell.setBorder(Rectangle.NO_BORDER);
				table.addCell(cell);
				for (UOrderDInfo info : mPrintList) {
					if ( info.nSCWType != Global.INVENTORY_TRUCK && info.nSCWType != Global.WEIGHT_ITEM) {
						continue;
					}
					if ( info.nTrxnType != Global.TRXN_DELIVER && info.nTrxnType != Global.TRXN_FREE && info.nTrxnType != Global.TRXN_SAMPLE){
						continue;
					}

					if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
					{
						double dQuantity = (info.nTrxnType == Global.TRXN_DELIVER || info.nTrxnType == Global.TRXN_FREE || info.nTrxnType == Global.TRXN_SAMPLE)
								? info.dSales : info.dDumps;

						if(info.nTrxnType == Global.TRXN_DELIVER || info.nTrxnType == Global.TRXN_FREE || info.nTrxnType == Global.TRXN_SAMPLE)
						{
							phrase = new Phrase(info.strShortDesc + (info.strItemTaxCode.length() > 0 ? " *" : ""), fontBlack7Normal);
							cell = new PdfPCell(phrase);
							cell.setColspan(4);
							cell.setBorder(Rectangle.NO_BORDER);
							nHeight += 20;
							table.addCell(cell);
						}

						float nPrice = info.nPrice;
						for(UPromotionInfo uPromotionInfo : info.uPromotionInfoList)
						{
							if( uPromotionInfo.nPromoAppMethod == 4 )
								nPrice += Utils.getDividedValue(uPromotionInfo.nAmount);
						}

						phrase = new Phrase(String.format(Locale.ENGLISH, "%.2f @ " + Global.gCurrencySymbol + "%." + Utils.getIntSetting(mContext, Global.PREF_DECIMAL) + "f", dQuantity, nPrice), fontBlack7Normal);
						cell = new PdfPCell(phrase);
						cell.setColspan(3);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						cell.setBorder(Rectangle.NO_BORDER);
						table.addCell(cell);
						nHeight += 20;

						if( info.nTrxnType == Global.TRXN_FREE || info.nTrxnType == Global.TRXN_SAMPLE)
							phrase = new Phrase("", fontBlack7Normal);
						else
							phrase = new Phrase(String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(mContext, Global.PREF_DECIMAL) + "f", (dQuantity * nPrice)), fontBlack7Normal);
					}
					else
					{
						int nQuantity = (info.nTrxnType == Global.TRXN_DELIVER || info.nTrxnType == Global.TRXN_FREE || info.nTrxnType == Global.TRXN_SAMPLE)
								? info.nSales : info.nDumps;

						if(info.nTrxnType == Global.TRXN_DELIVER || info.nTrxnType == Global.TRXN_FREE || info.nTrxnType == Global.TRXN_SAMPLE)
						{
							phrase = new Phrase(info.strShortDesc + (info.strItemTaxCode.length() > 0 ? " *" : ""), fontBlack7Normal);
							cell = new PdfPCell(phrase);
							cell.setColspan(4);
							cell.setBorder(Rectangle.NO_BORDER);
							nHeight += 20;
							table.addCell(cell);
						}

						float nPrice = info.nPrice;
						for(UPromotionInfo uPromotionInfo : info.uPromotionInfoList)
						{
							if( uPromotionInfo.nPromoAppMethod == 4 )
								nPrice += Utils.getDividedValue(uPromotionInfo.nAmount);
						}

						phrase = new Phrase(String.format(Locale.ENGLISH, "%d @ " + Global.gCurrencySymbol + "%." + Utils.getIntSetting(mContext, Global.PREF_DECIMAL) + "f", nQuantity, nPrice), fontBlack7Normal);
						cell = new PdfPCell(phrase);
						cell.setColspan(3);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						cell.setBorder(Rectangle.NO_BORDER);
						table.addCell(cell);
						nHeight += 20;

						if( info.nTrxnType == Global.TRXN_FREE || info.nTrxnType == Global.TRXN_SAMPLE)
							phrase = new Phrase("", fontBlack7Normal);
						else
							phrase = new Phrase(String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(mContext, Global.PREF_DECIMAL) + "f", (nQuantity * nPrice)), fontBlack7Normal);
					}

					
					
					cell = new PdfPCell(phrase);
					cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
					cell.setBorder(Rectangle.NO_BORDER);
					table.addCell(cell);
					nHeight += 20;
					
					//Get FreeItem
					DbAdapter dbAdapter = new DbAdapter(mContext);
					dbAdapter.open();
					List<UOrderDInfo> uOrderDList = dbAdapter.getUOrderDList(info.nTrxnNo);
					dbAdapter.close();
					
					for(UOrderDInfo uOrderDInfo : uOrderDList)
					{
						boolean bExistFreeItem = false;
						for(UPromotionInfo uPromotionInfo : uOrderDInfo.uPromotionInfoList)
						{
							if( uPromotionInfo.nPromoType == Global.PROMO_TYPE_BUY_FREE_MULTI )
							{
								bExistFreeItem = true;
								break;
							}
						}
						if (bExistFreeItem && uOrderDInfo.strItemNo.equals(info.strItemNo)) {
							uOrderDInfo.nTrxnType = 12;
							uOrderDInfo.nQuantity = uOrderDInfo.nPromoDiscount == 0 ? 0 : Math.round(Utils.myDivider(uOrderDInfo.nQuantity, uOrderDInfo.nPromoDiscount));
							uOrderDInfo.nBasePrice = 0;
							uOrderDInfo.nRetailPrice = 0;
							uOrderDInfo.nTaxAmount = 0;
							uOrderDInfo.nPrice = 0;
							for(UTaxInfo uTaxInfo : uOrderDInfo.uTaxInfoList)
							{
								uTaxInfo.nTrxnNo = uOrderDInfo.nTrxnNo;
								uTaxInfo.nTrxnType = uOrderDInfo.nTrxnType;
							}
							phrase = new Phrase(String.format(Locale.ENGLISH, "%d @ " + Global.gCurrencySymbol + "%." + Utils.getIntSetting(mContext, Global.PREF_DECIMAL) + "f", uOrderDInfo.nQuantity, 0.0), fontBlack7Normal);
							cell = new PdfPCell(phrase);
							cell.setColspan(3);
							cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
							cell.setBorder(Rectangle.NO_BORDER);
							table.addCell(cell);
							nHeight += 20;

							phrase = new Phrase("", fontBlack7Normal);
							cell = new PdfPCell(phrase);
							cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
							cell.setBorder(Rectangle.NO_BORDER);
							table.addCell(cell);
							nHeight += 20;
						}
					}
				}
			}
			if (mReturnsCnt > 0) {
				phrase = new Phrase("REPLACED", fontBlack7Bold);
				cell = new PdfPCell(phrase);
				cell.setColspan(4);
				cell.setPaddingTop(5);
				cell.setBorder(Rectangle.NO_BORDER);
				table.addCell(cell);
				nHeight += 20;
				for (UOrderDInfo info : mPrintList) {
					if ((info.nSCWType != Global.INVENTORY_TRUCK && info.nSCWType != Global.WEIGHT_ITEM )|| info.nTrxnType != Global.TRXN_PICKUP) {
						continue;
					}
					if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
					{
						double dQuantity = (info.nTrxnType == Global.TRXN_DELIVER || info.nTrxnType == Global.TRXN_FREE || info.nTrxnType == Global.TRXN_SAMPLE) ? info.dSales : info.dDumps;

						phrase = new Phrase(info.strShortDesc + (info.strItemTaxCode.length() > 0 ? " *" : ""), fontBlack7Normal);
						cell = new PdfPCell(phrase);
						cell.setColspan(4);
						cell.setBorder(Rectangle.NO_BORDER);
						table.addCell(cell);
						nHeight += 20;

						float nPrice = info.nPrice;
						for(UPromotionInfo uPromotionInfo : info.uPromotionInfoList)
						{
							if( uPromotionInfo.nPromoAppMethod == 4 )
								nPrice += Utils.getDividedValue(uPromotionInfo.nAmount);
						}


						phrase = new Phrase(String.format(Locale.ENGLISH, "-%.2f @ " + Global.gCurrencySymbol + "%." + Utils.getIntSetting(mContext, Global.PREF_DECIMAL) + "f", dQuantity, (nPrice)), fontBlack7Normal);
						cell = new PdfPCell(phrase);
						cell.setColspan(3);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						cell.setBorder(Rectangle.NO_BORDER);
						table.addCell(cell);
						nHeight += 20;

						phrase = new Phrase(String.format(Locale.ENGLISH, "-" + Global.gCurrencySymbol + "%." + Utils.getIntSetting(mContext, Global.PREF_DECIMAL) + "f", (dQuantity * nPrice)), fontBlack7Normal);
						cell = new PdfPCell(phrase);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						cell.setBorder(Rectangle.NO_BORDER);
						table.addCell(cell);
						nHeight += 20;
					}
					else
					{
						int nQuantity = (info.nTrxnType == Global.TRXN_DELIVER || info.nTrxnType == Global.TRXN_FREE || info.nTrxnType == Global.TRXN_SAMPLE) ? info.nSales : info.nDumps;

						phrase = new Phrase(info.strShortDesc + (info.strItemTaxCode.length() > 0 ? " *" : ""), fontBlack7Normal);
						cell = new PdfPCell(phrase);
						cell.setColspan(4);
						cell.setBorder(Rectangle.NO_BORDER);
						table.addCell(cell);
						nHeight += 20;

						float nPrice = info.nPrice;
						for(UPromotionInfo uPromotionInfo : info.uPromotionInfoList)
						{
							if( uPromotionInfo.nPromoAppMethod == 4 )
								nPrice += Utils.getDividedValue(uPromotionInfo.nAmount);
						}


						phrase = new Phrase(String.format(Locale.ENGLISH, "-%d @ " + Global.gCurrencySymbol + "%." + Utils.getIntSetting(mContext, Global.PREF_DECIMAL) + "f", nQuantity, (nPrice)), fontBlack7Normal);
						cell = new PdfPCell(phrase);
						cell.setColspan(3);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						cell.setBorder(Rectangle.NO_BORDER);
						table.addCell(cell);
						nHeight += 20;

						phrase = new Phrase(String.format(Locale.ENGLISH, "-" + Global.gCurrencySymbol + "%." + Utils.getIntSetting(mContext, Global.PREF_DECIMAL) + "f", (nQuantity * nPrice)), fontBlack7Normal);
						cell = new PdfPCell(phrase);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						cell.setBorder(Rectangle.NO_BORDER);
						table.addCell(cell);
						nHeight += 20;
					}

				}
			}

			if (mBuybackCnt > 0) {
				phrase = new Phrase("BUYBACKS", fontBlack7Bold);
				cell = new PdfPCell(phrase);
				cell.setColspan(4);
				cell.setPaddingTop(5);
				cell.setBorder(Rectangle.NO_BORDER);
				table.addCell(cell);
				nHeight += 20;
				for (UOrderDInfo info : mPrintList) {
					if ((info.nSCWType != Global.INVENTORY_TRUCK && info.nSCWType != Global.WEIGHT_ITEM) || info.nTrxnType != Global.TRXN_BUYBACK) {
						continue;
					}
					if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
					{
						double dQuantity = (info.nTrxnType == Global.TRXN_DELIVER || info.nTrxnType == Global.TRXN_FREE || info.nTrxnType == Global.TRXN_SAMPLE) ? info.dSales : info.dDumps;

						float nPrice = info.nPrice;
						for(UPromotionInfo uPromotionInfo : info.uPromotionInfoList)
						{
							if( uPromotionInfo.nPromoAppMethod == 4 )
								nPrice += Utils.getDividedValue(uPromotionInfo.nAmount);
						}


						phrase = new Phrase(info.strShortDesc + (info.strItemTaxCode.length() > 0 ? " *" : ""), fontBlack7Normal);
						cell = new PdfPCell(phrase);
						cell.setColspan(4);
						cell.setBorder(Rectangle.NO_BORDER);
						table.addCell(cell);
						nHeight += 20;

						phrase = new Phrase(String.format(Locale.ENGLISH, "-%.2f @ " + Global.gCurrencySymbol + "%." + Utils.getIntSetting(mContext, Global.PREF_DECIMAL) + "f", dQuantity, (nPrice)), fontBlack7Normal);
						cell = new PdfPCell(phrase);
						cell.setColspan(3);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						cell.setBorder(Rectangle.NO_BORDER);
						table.addCell(cell);
						nHeight += 20;

						phrase = new Phrase(String.format(Locale.ENGLISH, "-" + Global.gCurrencySymbol + "%." + Utils.getIntSetting(mContext, Global.PREF_DECIMAL) + "f", (dQuantity * nPrice)), fontBlack7Normal);
						cell = new PdfPCell(phrase);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						cell.setBorder(Rectangle.NO_BORDER);
						table.addCell(cell);
						nHeight += 20;
					}
					else
					{
						int nQuantity = (info.nTrxnType == Global.TRXN_DELIVER || info.nTrxnType == Global.TRXN_FREE || info.nTrxnType == Global.TRXN_SAMPLE) ? info.nSales : info.nDumps;

						float nPrice = info.nPrice;
						for(UPromotionInfo uPromotionInfo : info.uPromotionInfoList)
						{
							if( uPromotionInfo.nPromoAppMethod == 4 )
								nPrice += Utils.getDividedValue(uPromotionInfo.nAmount);
						}


						phrase = new Phrase(info.strShortDesc + (info.strItemTaxCode.length() > 0 ? " *" : ""), fontBlack7Normal);
						cell = new PdfPCell(phrase);
						cell.setColspan(4);
						cell.setBorder(Rectangle.NO_BORDER);
						table.addCell(cell);
						nHeight += 20;

						phrase = new Phrase(String.format(Locale.ENGLISH, "-%d @ " + Global.gCurrencySymbol + "%." + Utils.getIntSetting(mContext, Global.PREF_DECIMAL) + "f", nQuantity, (nPrice)), fontBlack7Normal);
						cell = new PdfPCell(phrase);
						cell.setColspan(3);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						cell.setBorder(Rectangle.NO_BORDER);
						table.addCell(cell);
						nHeight += 20;

						phrase = new Phrase(String.format(Locale.ENGLISH, "-" + Global.gCurrencySymbol + "%." + Utils.getIntSetting(mContext, Global.PREF_DECIMAL) + "f", (nQuantity * nPrice)), fontBlack7Normal);
						cell = new PdfPCell(phrase);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						cell.setBorder(Rectangle.NO_BORDER);
						table.addCell(cell);
						nHeight += 20;
					}

				}
			}

			if (mContainersCnt > 0) {
				phrase = new Phrase("CONTAINERS", fontBlack7Bold);
				cell = new PdfPCell(phrase);
				cell.setColspan(4);
				cell.setPaddingTop(5);
				cell.setBorder(Rectangle.NO_BORDER);
				table.addCell(cell);
				nHeight += 20;
				for (UOrderDInfo info : mPrintList) {
					if (info.nSCWType != Global.INVENTORY_CONTAINERS) {
						continue;
					}
					int nQuantity = info.nQuantity;
					
					phrase = new Phrase(/*info.strItemNo + "        " +*/ info.strShortDesc, fontBlack7Normal);
					cell = new PdfPCell(phrase);
					cell.setColspan(4);
					cell.setBorder(Rectangle.NO_BORDER);
					table.addCell(cell);
					nHeight += 20;
					
					phrase = new Phrase(" ", fontBlack7Normal);
					cell = new PdfPCell(phrase);
					cell.setBorder(Rectangle.NO_BORDER);
					cell.setColspan(1);
					cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
					table.addCell(cell);
					nHeight += 20;
					
					phrase = new Phrase(String.valueOf(nQuantity), fontBlack7Normal);
					cell = new PdfPCell(phrase);
					cell.setBorder(Rectangle.NO_BORDER);
					cell.setColspan(1);
					cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
					table.addCell(cell);
					nHeight += 20;
					
					phrase = new Phrase(" ", fontBlack7Normal);
					cell = new PdfPCell(phrase);
					cell.setBorder(Rectangle.NO_BORDER);
					cell.setColspan(2);
					cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
					table.addCell(cell);
					nHeight += 20;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return table;
	}

	private PdfPTable createBody_Footer(Integer nHeight) {
		PdfPTable table = new PdfPTable(2);
		PdfPCell cell;
		Phrase phrase;
		try {
			table.setWidths(new int[]{2, 1});
			table.getDefaultCell().setBorder(Rectangle.NO_BORDER);

			phrase = new Phrase("  ", fontBlack7Normal);
			cell = new PdfPCell(phrase);
			cell.setColspan(2);
			cell.setBorder(Rectangle.BOTTOM);
			table.addCell(cell);
			nHeight += 20;

			phrase = new Phrase("Total", fontBlack7Normal);
			cell = new PdfPCell(phrase);
			cell.setPaddingTop(5);
			cell.setBorder(Rectangle.NO_BORDER);
			table.addCell(cell);

			float nSalesAmount = Global.gUOrderHInfo.nSaleAmount + Global.gUOrderHInfo.nDiscSales;

			phrase = new Phrase(getFormattedPrice(nSalesAmount/* + Global.gUOrderHInfo.nDiscSales*/), fontBlack7Normal);
			cell = new PdfPCell(phrase);
			cell.setPaddingTop(5);
			cell.setBorder(Rectangle.NO_BORDER);
			cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
			table.addCell(cell);
			nHeight += 20;

			phrase = new Phrase("Handling fee", fontBlack7Normal);
			cell = new PdfPCell(phrase);
			cell.setPaddingTop(5);
			cell.setBorder(Rectangle.NO_BORDER);
			table.addCell(cell);

			phrase = new Phrase(getFormattedPrice(Global.gUOrderHInfo.nHandlingFee/* + Global.gUOrderHInfo.nDiscSales*/), fontBlack7Normal);
			cell = new PdfPCell(phrase);
			cell.setPaddingTop(5);
			cell.setBorder(Rectangle.NO_BORDER);
			cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
			table.addCell(cell);
			nHeight += 20;
			List<PrintTaxRateInfo> taxRateInfoList = new ArrayList<>();

			for (Map.Entry<String, PrintTaxRateInfo> entry : Global.gUOrderHInfo.cdsMap.entrySet())
			{
				//System.out.println(entry.getKey() + "/" + entry.getValue());
				PrintTaxRateInfo taxRateInfo = entry.getValue();
				if( taxRateInfo.strTaxCode != null && taxRateInfo.strTaxCode.length() > 0 && !taxRateInfo.strTaxCode.equals("0") && taxRateInfo.nBottles != 0)
				{
					taxRateInfoList.add(taxRateInfo);
				}
			}

			Collections.sort(taxRateInfoList, new Comparator<PrintTaxRateInfo>() {
				@Override
				public int compare(PrintTaxRateInfo printTaxRateInfo, PrintTaxRateInfo t1) {
					return printTaxRateInfo.strTaxCode.compareTo(t1.strTaxCode);
				}
			});

			for(PrintTaxRateInfo taxRateInfo : taxRateInfoList)
			{
				phrase = new Phrase(String.format(Locale.ENGLISH, "%d × %s", taxRateInfo.nBottles, taxRateInfo.strRateDescription), fontBlack7Normal);
				cell = new PdfPCell(phrase);
				cell.setBorder(Rectangle.NO_BORDER);
				table.addCell(cell);

				phrase = new Phrase(getFormattedPrice(taxRateInfo.price * taxRateInfo.nBottles), fontBlack7Normal);
				cell = new PdfPCell(phrase);
				cell.setBorder(Rectangle.NO_BORDER);
				cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				table.addCell(cell);
				nHeight += 20;
			}
/*
			phrase = new Phrase("NSW CDS Levy", fontBlack7Normal);
			cell = new PdfPCell(phrase);
			cell.setBorder(Rectangle.NO_BORDER);
			table.addCell(cell);

			phrase = new Phrase(getFormattedPrice(Global.gUOrderHInfo.nPrintCDSAmount), fontBlack7Normal);
			cell = new PdfPCell(phrase);
			cell.setBorder(Rectangle.NO_BORDER);
			cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
			table.addCell(cell);
			nHeight += 20;
			*/

			phrase = new Phrase("GST*", fontBlack7Normal);
			cell = new PdfPCell(phrase);
			cell.setBorder(Rectangle.NO_BORDER);
			table.addCell(cell);
			phrase = new Phrase(getFormattedPrice(Global.gUOrderHInfo.nTaxAmount - Global.gUOrderHInfo.nPrintCDSAmount + Global.gUOrderHInfo.nHandlingFeeTaxAmount), fontBlack7Normal);
			cell = new PdfPCell(phrase);
			cell.setBorder(Rectangle.NO_BORDER);
			cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
			table.addCell(cell);
			nHeight += 20;

			phrase = new Phrase("Total", fontBlack7Bold);
			cell = new PdfPCell(phrase);
			cell.setBorder(Rectangle.NO_BORDER);
			table.addCell(cell);
			nHeight += 20;
			int nPayType = Utils.parseIntFromString(Global.gCurCustomerInfo.strPayType);
			nSalesAmount += Global.gUOrderHInfo.nHandlingFeeTaxAmount + Global.gUOrderHInfo.nHandlingFee;
			if( Utils.getStringSetting(mContext, Global.PREF_ROUTE_CENTSROUND).trim().equals("1") &&
					/*(Global.gCurCustomerInfo.strPayType.trim().equals("1") || Global.gCurCustomerInfo.strPayType.trim().equals("2"))*/
					(nPayType == 1 || nPayType == 2))
			{
				int nValue = (int)((nSalesAmount + Global.gUOrderHInfo.nTaxAmount) * Global.MULTIPLE_VALUE);
				int nTotal = Math.abs(nValue);
				int nOffSet = nTotal % 5000;
				int nInvoiceTotal = ((nTotal / 5000) + ((nOffSet >= 2500) ? 1 : 0)) * 5000;
				nOffSet = nTotal % 1000;
				nTotal = ((nTotal / 1000) + ((nOffSet >= 500) ? 1 : 0)) * 1000;
				nOffSet = nInvoiceTotal - nTotal;
				float dOffSet = nOffSet / (Global.MULTIPLE_VALUE * 1f);
				float dTotal = nTotal / (Global.MULTIPLE_VALUE * 1f);

				if( nValue < 0 )
				{
					nInvoiceTotal *= -1;
				}
				phrase = new Phrase(getFormattedPrice(nInvoiceTotal / (Global.MULTIPLE_VALUE * 1f)), fontBlack7Bold);
				cell = new PdfPCell(phrase);
				cell.setBorder(Rectangle.NO_BORDER);
				cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				table.addCell(cell);
				nHeight += 20;

				phrase = new Phrase("Round Amount", fontBlack7Normal);
				cell = new PdfPCell(phrase);
				cell.setBorder(Rectangle.NO_BORDER);
				table.addCell(cell);
				nHeight += 20;

				if( nValue < 0 )
				{
					dOffSet *= -1.0f;
				}
				String strOffSet= "";
				strOffSet = String.format(Locale.ENGLISH, "%.2f", Math.abs(dOffSet));
				if(dOffSet >= 0f)
					strOffSet = "" + strOffSet.substring(1);
				else
					strOffSet = "-" + strOffSet.substring(1);
				phrase = new Phrase(strOffSet, fontBlack7Normal);
				cell = new PdfPCell(phrase);
				cell.setBorder(Rectangle.NO_BORDER);
				cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				table.addCell(cell);
				nHeight += 20;
			}
			else
			{
				phrase = new Phrase(getFormattedPrice(nSalesAmount + Global.gUOrderHInfo.nTaxAmount), fontBlack7Bold);
				cell = new PdfPCell(phrase);
				cell.setBorder(Rectangle.NO_BORDER);
				cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				table.addCell(cell);
				nHeight += 20;
			}


			if( Global.gUOrderHInfo.nDiscSales > 0 && nSalesAmount == Global.gUOrderHInfo.nSaleAmount)
			{
				phrase = new Phrase("Discount Included", fontBlack7Normal);
				cell = new PdfPCell(phrase);
				cell.setBorder(Rectangle.NO_BORDER);
				table.addCell(cell);
				nHeight += 20;

				phrase = new Phrase(getFormattedPrice(Global.gUOrderHInfo.nDiscSales), fontBlack7Normal);
				cell = new PdfPCell(phrase);
				cell.setBorder(Rectangle.NO_BORDER);
				cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				table.addCell(cell);
				nHeight += 20;
			}

			if (mPayMethod > 0 && !m_bFutureOrder) {

				if( nSalesAmount + Global.gUOrderHInfo.nTaxAmount > 0 )
				{
					phrase = new Phrase("Payment", fontBlack7Normal);
					cell = new PdfPCell(phrase);
					cell.setPaddingTop(5);
					cell.setBorder(Rectangle.NO_BORDER);
					table.addCell(cell);
					nHeight += 20;

					if( Utils.getStringSetting(mContext, Global.PREF_ROUTE_CENTSROUND).trim().equals("1") && (nPayType == 1 || nPayType == 2))
					{
						int nValue = (int)((nSalesAmount + Global.gUOrderHInfo.nTaxAmount) * Global.MULTIPLE_VALUE);;
						int nTotal = Math.abs(nValue);
						int nOffSet = nTotal % 5000;
						int nInvoiceTotal = ((nTotal / 5000) + ((nOffSet >= 2500) ? 1 : 0)) * 5000;
						nOffSet = nTotal % 1000;
						nTotal = ((nTotal / 1000) + ((nOffSet >= 500) ? 1 : 0)) * 1000;
						nOffSet = nInvoiceTotal - nTotal;
						float dOffSet = nOffSet / (Global.MULTIPLE_VALUE * 1f);
						float dTotal = nTotal / (Global.MULTIPLE_VALUE * 1f);

						if( nValue < 0 )
						{
							nInvoiceTotal *= -1;
						}
						phrase = new Phrase(getFormattedPrice(nInvoiceTotal / (Global.MULTIPLE_VALUE * 1f)), fontBlack7Normal);
						cell = new PdfPCell(phrase);
						cell.setPaddingTop(5);
						cell.setBorder(Rectangle.NO_BORDER);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						table.addCell(cell);
						nHeight += 20;
					}
					else
					{
						phrase = new Phrase(getFormattedPrice(nSalesAmount + Global.gUOrderHInfo.nTaxAmount), fontBlack7Normal);
						cell = new PdfPCell(phrase);
						cell.setPaddingTop(5);
						cell.setBorder(Rectangle.NO_BORDER);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						table.addCell(cell);
						nHeight += 20;
					}

					phrase = new Phrase("Balance", fontBlack7Normal);
					cell = new PdfPCell(phrase);
					cell.setBorder(Rectangle.NO_BORDER);
					table.addCell(cell);
					phrase = new Phrase(getFormattedPrice(0), fontBlack7Normal);
					cell = new PdfPCell(phrase);
					cell.setBorder(Rectangle.NO_BORDER);
					cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
					table.addCell(cell);
					nHeight += 20;

				}


			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return table;
	}

	private PdfPTable createFooter(Integer nHeight) {
		PdfPTable table = new PdfPTable(1);
		PdfPCell cell;
		Phrase phrase;
		try {
			table.setWidths(new int[]{1});
			table.getDefaultCell().setBorder(Rectangle.NO_BORDER);

			cell = new PdfPCell();
			cell.setBorder(Rectangle.TOP);
			table.addCell(cell);

			phrase = new Phrase("Signed by: ", fontBlack7Normal);
			table.addCell(cell);
			nHeight += 20;

			if (Global.gUOrderHInfo.strSignatureFilePath.length() > 0 && Utils.isFileExist(Global.gUOrderHInfo.strSignatureFilePath) == true) {
				Image image = Image.getInstance(Global.gUOrderHInfo.strSignatureFilePath);
				image.scaleToFit(120, 120);
				cell = new PdfPCell(image);
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				nHeight += 120;
			} else {
				phrase = new Phrase("No Signature File", fontBlack10Bold);
				cell = new PdfPCell(phrase);
				nHeight += 20;
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

			phrase = new Phrase("Received By:", fontBlack7Normal);
			
			cell = new PdfPCell(phrase);
			cell.setBorder(Rectangle.NO_BORDER);
			table.addCell(cell);
			phrase = new Phrase("   " + Global.gUOrderHInfo.strOrderName, fontBlack7Normal);
			cell = new PdfPCell(phrase);
			cell.setBorder(Rectangle.NO_BORDER);
			table.addCell(cell);
			nHeight += 20;
			
			phrase = new Phrase("Delivery By:", fontBlack7Normal);
			cell = new PdfPCell(phrase);
			cell.setPaddingTop(5);
			cell.setBorder(Rectangle.NO_BORDER);
			table.addCell(cell);
			phrase = new Phrase("   " + Utils.getStringSetting(mContext, Global.PREF_USER_DESCRIPTION), fontBlack7Normal);
			cell.setPaddingBottom(5);
			table.addCell(phrase);
			nHeight += 20;

			for (DocTextInfo info : mDocTextList) {
				if (info.nTextType == 7 && (TextUtils.isEmpty(info.strSubType) || info.strSubType.equals("P"))) {
					
					if( info.strDocText.length() > 0 )
					{
						phrase = new Phrase(info.strDocText, fontBlack7Bold);
						cell = new PdfPCell(phrase);
						cell.setBorder(Rectangle.NO_BORDER);
						table.addCell(cell);
						nHeight += 20;
					}
					else
					{
						phrase = new Phrase("  ", fontBlack7Bold);
						cell = new PdfPCell(phrase);
						cell.setBorder(Rectangle.NO_BORDER);
						table.addCell(cell);
						nHeight += 20;
					}
					
				}
			}

			phrase = new Phrase("Printed: " + new SimpleDateFormat(Utils.getDateFormat(mContext, "dd/MM/yyyy") + " hh:mm aa").format(new Date()), fontBlack7Normal);
			cell = new PdfPCell(phrase);
			cell.setPaddingTop(5);
			cell.setBorder(Rectangle.NO_BORDER);
			table.addCell(cell);
			nHeight += 20;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return table;
	}

	private void outputPrinter() {
		mIsCreatedPDF = true;
		if (BluetoothAdapter.getDefaultAdapter().isEnabled() == false) {
			try {
				if(mContext.isFinishing() || mContext.isDestroyed()) return;
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
							m_bConfirmClicked = false;
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
						new Handler(Looper.getMainLooper()).post(new Runnable(){

							@Override
							public void run() {
								try
								{
									if(mContext.isFinishing() || mContext.isDestroyed()) return;
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
	

	public Handler mHander = new Handler(){
        public void handleMessage(Message msg){
        	try {
				if(mContext.isFinishing() || mContext.isDestroyed()) return;
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
							m_bConfirmClicked = false;
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
			if(mContext.isFinishing() || mContext.isDestroyed()) return;
			try {
				Toast.makeText(mContext, Str.NoDeviceDiscover[Global.LANG], Toast.LENGTH_LONG).show();
			}catch (Exception e1)
			{
				e1.printStackTrace();
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
				else
				{
					m_bConfirmClicked = false;
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
			if(mContext.isFinishing() || mContext.isDestroyed()) return;
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

	private void showContainerTracking() {
		try {
			if(mContext.isFinishing() || mContext.isDestroyed()) return;
			final ContainerTrackingDialog dlg = new ContainerTrackingDialog(mContext);
			//dlg.mContainerList.clear();
			//dlg.mContainerList.addAll(Global.gUOrderHInfo.containerList);
			dlg.setCancelable(false);
			dlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
					if (dlg.mReturnCode == Global.DLG_RETURN_RIGHT) {
//					Global.gUOrderHInfo.containerList.clear();
//					Global.gUOrderHInfo.containerList.addAll(dlg.mContainerList);
						refreshLanguage();
					}
				}
			});
			dlg.show();
		}catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void doEmptyConsignmentShelf()
	{
		long nTrxnNo = setGeneralUploadData("ADJO", Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo, -1, mTrxnDate);
		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		Global.gAdjustinfo = dbAdapter.getUInvenHInfo(Utils.getLongSetting(mContext, Global.PREF_UINVEN_TRXN_NO));
		if (Global.gAdjustinfo == null) {
			Global.gAdjustinfo = new UInvenHInfo();
			Global.gAdjustinfo.nTrxnNo = Utils.getLongSetting(mContext, Global.PREF_UINVEN_TRXN_NO);
		}

		List<UInvenDInfo> uInvenDInfoList = dbAdapter.getUInvenDList(Global.gAdjustinfo.nTrxnNo);
		List<CustConBalInfo> conBalInfoList = dbAdapter.getCustConBalInfo(Global.gCurCustomerInfo.nCustNo, Global.gCurCustomerInfo.nChainNo);
		Map<String, UInvenDInfo> uInvenDInfoMap = new HashMap<>();
		for(UInvenDInfo uInvenDInfo : uInvenDInfoList)
		{
			uInvenDInfoMap.put(uInvenDInfo.strItemNo, uInvenDInfo);
		}

		UStockHInfo uStockHInfo = new UStockHInfo();
		uStockHInfo.strStockType = "2";
		uStockHInfo.nTrxnNo = nTrxnNo;
		uStockHInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
		uStockHInfo.nChainNo = Global.gCurCustomerInfo.nChainNo;

		uStockHInfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(mContext, mTrxnDate));
		uStockHInfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(mTrxnDate);
		uStockHInfo.strDocNo = "0";

		uStockHInfo.strDocType = "ADJO";

		dbAdapter.insertUConsignmentInfoList(uStockHInfo);

		UInvenHInfo uInvenHInfo = new UInvenHInfo();
		uInvenHInfo.nTrxnNo = nTrxnNo;
		uInvenHInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
		uInvenHInfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(mContext, mTrxnDate));
		uInvenHInfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(mTrxnDate);
		dbAdapter.insertUInvenHInfo(uInvenHInfo);
		//Make the Consignment UInventory
		for(CustConBalInfo info : conBalInfoList)
		{
			UStockDInfo uStockDInfo = new UStockDInfo();
			uStockDInfo.strItemNo = info.strItemNo;
			if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
			{
				uStockDInfo.dActualQty = info.fWeight;
				uStockDInfo.dBeginQty = info.fWeight;
			}
			else
			{
				uStockDInfo.nActualQty = info.nQty;
				uStockDInfo.nBeginQty = info.nQty;
			}

			uStockDInfo.nTrxnNo = nTrxnNo;
			dbAdapter.insertUStockDInfoList(uStockDInfo);

			UInvenDInfo uInvenDInfo = uInvenDInfoMap.get(info.strItemNo);
			if(uInvenDInfo == null)
			{
				uInvenDInfo = new UInvenDInfo();
				uInvenDInfo.strItemNo = info.strItemNo;
				uInvenDInfo.nTrxnNo = Global.gAdjustinfo.nTrxnNo;
				uInvenDInfoMap.put(info.strItemNo, uInvenDInfo);
			}

			if(Utils.getProdLoconSCWType(mContext, uStockDInfo.strItemNo) == Global.WEIGHT_ITEM)
			{
				uInvenDInfo.dTransIn += info.fWeight;
				uInvenDInfo.dStock += info.fWeight;
			}
			else
			{
				uInvenDInfo.nTransIn += info.nQty;
				uInvenDInfo.nStock += info.nQty;
			}

			if(uInvenDInfo.nIdx == 0)
			{
				dbAdapter.insertUInvenDInfo(uInvenDInfo);
			}
			else
			{
				dbAdapter.updateUInvenDInfo(uInvenDInfo);
			}

			uInvenDInfo.nTrxnNo = nTrxnNo;
			dbAdapter.insertUInvenDInfo(uInvenDInfo);
			info.nQty = 0;
			info.fWeight = 0;
			dbAdapter.updateCustConBalInfo(info);
		}

		dbAdapter.close();

		Global.gStockDateTime = System.currentTimeMillis();
	}

	private void goBack() {

		if(!bConsignment)
		{
			if (Global.gCurCustomerInfo.nInvoiceFmt == 1) {
				if(!bReverseMode)
				{
					Global.gUARTotalInfo.bIsVisitedDelivery = true;
					Global.gUARTotalInfo.bIsVisitedPickup = true;
				}
				else {
					Global.gUARTotalInfo.bIsVisitedDelivery = false;
					Global.gUARTotalInfo.bIsVisitedPickup = false;
				}

				Global.gUARTotalInfo.nDeliveryTrxnNo = mPdfTrxnNo;
			} else {
				if (mIsPickup == false) {
					if(!bReverseMode)
					{
						Global.gUARTotalInfo.bIsVisitedDelivery = true;
					}
					else
					{
						Global.gUARTotalInfo.bIsVisitedDelivery = false;
					}
					Global.gUARTotalInfo.nDeliveryTrxnNo = mPdfTrxnNo;
				} else {
					if(!bReverseMode)
					{
						Global.gUARTotalInfo.bIsVisitedPickup = true;
					}
					else
					{
						Global.gUARTotalInfo.bIsVisitedPickup = false;
					}
					Global.gUARTotalInfo.nPickupTrxnNo = mPdfTrxnNo;
				}
			}
		}


//		if(!bReverseMode)
		{
			Global.gCurCustomerDeliveryList.add(mPdfTrxnNo);
		}

		if(!m_bFutureOrder)
		{
			/*if (mPayMethod == 1) {
				// Cash
				//Processed Data Remove;
				DbAdapter dbAdapter = new DbAdapter(mContext);
				dbAdapter.open();
				dbAdapter.deleteProcessedARHeader(Global.gCurCustomerInfo.nCustNo, Global.gCurCustomerInfo.nChainNo);
				dbAdapter.close();
				Bundle bundle = new Bundle();
				bundle.putBoolean(Global.EXTRA_CARD, false);
				fragment = new PaymentCollectionFragment();
				fragment.setArguments(bundle);
				mContext.replaceFragment(fragment);
			} else*/ if (mPayMethod == 2) {
			// Cheque
			if(!bReverseMode)
			{
				mContext.setMarkVisitAutoCollect(true);
			}

		} /*else if (mPayMethod == 3){
				DbAdapter dbAdapter = new DbAdapter(mContext);
				dbAdapter.open();
				dbAdapter.deleteProcessedARHeader(Global.gCurCustomerInfo.nCustNo, Global.gCurCustomerInfo.nChainNo);
				dbAdapter.close();
				Bundle bundle = new Bundle();
				bundle.putBoolean(Global.EXTRA_CARD, true);
				fragment = new PaymentCollectionFragment();
				fragment.setArguments(bundle);
				mContext.replaceFragment(fragment);
			}*/
		}

		Global.bOrderCreated = true;

		Global.gProcessedPayment = true;

		//int nCnt = mContext.getSupportFragmentManager().getBackStackEntryCount();
		if(bPaused)
		{
			bNeedBack = true;
		}
		else
		{
			if(!bReverseMode || (Global.startFragment instanceof InstoreFragment))
			{
				Fragment fragment = mContext.getSupportFragmentManager().findFragmentById(R.id.frameContainer);
				int nCnt = mContext.getSupportFragmentManager().getBackStackEntryCount();
				boolean bBack = true;
	
				while(!(fragment instanceof InstoreFragment))
				{
					if( fragment instanceof  MakeDeliveryFragment)
					{
						((MakeDeliveryFragment)fragment).setModified(false);
					}
					mContext.onBackPressed();
					fragment = mContext.getSupportFragmentManager().findFragmentById(R.id.frameContainer);
	
					if(mContext.getSupportFragmentManager().getBackStackEntryCount() == nCnt)
					{
						bBack = false;
						break;
					}
				}
	
				if(bBack == false)
				{
					fragment = mContext.getSupportFragmentManager().findFragmentByTag(InstoreFragment.class.getName());
					if(fragment != null)
					{
						mContext.replaceFragment(fragment);
					}
				}
			}
			else
			{
				Utils.doProcessInTemp(mContext);
				Fragment fragment = mContext.getSupportFragmentManager().findFragmentById(R.id.frameContainer);
				int nCnt = mContext.getSupportFragmentManager().getBackStackEntryCount();
				boolean bBack = true;

				while(!(fragment instanceof PrintDocumentsFragment))
				{
					if( fragment instanceof  MakeDeliveryFragment)
					{
						((MakeDeliveryFragment)fragment).setModified(false);
					}
					mContext.onBackPressed();
					fragment = mContext.getSupportFragmentManager().findFragmentById(R.id.frameContainer);

					if(mContext.getSupportFragmentManager().getBackStackEntryCount() == nCnt)
					{
						bBack = false;
						break;
					}
				}

				if(bBack == false)
				{
					fragment = mContext.getSupportFragmentManager().findFragmentByTag(InstoreFragment.class.getName());
					if(fragment != null)
					{
						mContext.replaceFragment(fragment);
					}
				}
			}
			
		}
	}

	private void doGetGPSPlot(Location location) {
		if (location == null) {
			if(mContext.isFinishing() || mContext.isDestroyed()) return;
			try {
				Toast.makeText(mContext, Str.Cant_get_location_info_please_try_again_later[Global.LANG], Toast.LENGTH_LONG).show();
			}catch (Exception e1)
			{
				e1.printStackTrace();
			}
		} else {
			double nMyLat = location.getLatitude();
			double nMyLng = location.getLongitude();

			Global.gCurCustomerInfo.driverLatitude = nMyLat;
			Global.gCurCustomerInfo.driverLongitude = nMyLng;
			Global.gCurCustomerInfo.strArrivalTime = "NOSCH";
			mGPSNewPickedUp = true;
			DbAdapter dbAdapter = new DbAdapter(mContext);
			dbAdapter.open();
			dbAdapter.updateCustomerInfo(Global.gCurCustomerInfo);
			dbAdapter.close();

			if(mContext.isFinishing() || mContext.isDestroyed()) return;
			try {
				Toast.makeText(mContext, Str.GPS_info_has_been_gathered_successfully[Global.LANG], Toast.LENGTH_LONG).show();
			}catch (Exception e1)
			{
				e1.printStackTrace();
			}
		}
		prepareConfirm();
	}

	private void doGetGPSPlot(){

		if(ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
				ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
		{
			doGetGPSPlot(null);
		}

		showProgressDialog("Getting current location...");

		LocationServices.getFusedLocationProviderClient(mContext).getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
			@Override
			public void onComplete(@NonNull Task<Location> task) {
				if (task.isSuccessful() && task.getResult() != null) {
					hideProgressDialog();
					Location location = task.getResult();
					doGetGPSPlot(location);
				} else {
					hideProgressDialog();
					doGetGPSPlot(null);
				}
			}
		});
	}

	public boolean m_bClicked = false;
	public boolean m_bConfirmClicked = false;
	@Override
	public void onClick(View v) {
		if( m_bClicked ) return;
		m_bClicked = true;
		switch (v.getId()) {
			case R.id.btnContainerTracking:
				showContainerTracking();
				break;
			/*case R.id.btnPickDate:
				if(Utils.getIntSetting(mContext, Global.PREF_DELFUTDTE) != 0 )
				{
					Date startDate = new Date(mDate);
					new DatePickerDialog(mContext, dateListener, 1900 + startDate.getYear(), startDate.getMonth(), startDate.getDate()).show();
				}
				break;*/
			case R.id.btnSignature:
				Intent intent = new Intent(mContext, SignatureActivity.class);
				intent.putExtra(Global.EXTRA_SIGNATURE_PATH, Global.gUOrderHInfo.strSignatureFilePath);
				startActivity(intent);
				mContext.overridePendingTransition(R.anim.zoom_in, 0);
				break;
			case R.id.btnBack:
				Global.gDeliveryPickupCnt = 0;
				mContext.onBackPressed();
				break;
			case R.id.btnNote:
				try {
					if(mContext.isFinishing() || mContext.isDestroyed()) break;
					final SummeryInputDialog inputDlg = new SummeryInputDialog(mContext);
//				if( Global.gCurCustomerInfo != null && Global.gCurCustomerInfo.presoldOrHInfo != null && Global.gCurCustomerInfo.presoldOrHInfo.nVisited == 0 )
					{
						inputDlg.mStrInputed = Global.gUOrderHInfo.strSpecialInstructions;
					}
					inputDlg.setCancelable(false);
					inputDlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
						@Override
						public void onDismiss(DialogInterface dialog) {
							if (inputDlg.mReturnCode == Global.DLG_RETURN_RIGHT) {
								Global.gUOrderHInfo.strSpecialInstructions = inputDlg.mStrInputed;
							}
						}
					});
					inputDlg.show();
				}catch (Exception e)
				{
					e.printStackTrace();
				}
				break;
			case R.id.btnPhoto:
				selectPhoto();
				break;
			case R.id.btnConfirm:
				{
					prepareConfirm();
				}
				break;
		}
		m_bClicked = false;
	}

	private void prepareConfirm()
	{
		if(Global.gCurCustomerInfo.latitude == 0 && Global.gCurCustomerInfo.longitude == 0
				&& Global.gCurCustomerInfo.driverLatitude == 0 && Global.gCurCustomerInfo.driverLongitude == 0
				&& Utils.getIntSetting(mContext, Global.PREF_GET_GPS) == 1 && !mPassedGPSPlot)
		{
			try {
				if(mContext.isFinishing() || mContext.isDestroyed()) return;
				final MessageDialog msgDialog = new MessageDialog(mContext);
				msgDialog.setCancelable(false);
				msgDialog.mStrMessage = String.format(Locale.ENGLISH, Str.NoGPSRecord[Global.LANG], Global.gCurCustomerInfo.strName);
				msgDialog.mStrLeft = Str.No[Global.LANG];
				msgDialog.mStrRight = Str.Yes[Global.LANG];
				msgDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						mPassedGPSPlot = true;
						if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
							doGetGPSPlot();
						}
						else
						{
							prepareConfirm();
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

		if( m_bConfirmClicked ) return;
		m_bConfirmClicked = true;

		if(bReverseMode)
		{
			doConfirm();
		}
		else
		{
			String strTotalCollection = ((EditText)mRootView.findViewById(R.id.editTotalCollection)).getText().toString();
			float fTotalCollection = Utils.getFloatFromString(strTotalCollection);

			float creditLimit = Utils.getFloatFromString(Global.gCurCustomerInfo.strCreditLimit);
			if (creditLimit > 0 && Global.gCurCustomerInfo.strPayType.equals("3") && mPayMethod == 0) {
				DbAdapter dbAdapter = new DbAdapter(mContext);
				dbAdapter.open();
				List<ARHeaderInfo> arHeaderInfos = dbAdapter.getARHeaderInfo(Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo);
				String todayString = new SimpleDateFormat("yyyyMMdd").format(new Date());
				float totalAmount = 0.0f;
				for (ARHeaderInfo arHeader: arHeaderInfos) {
					if (arHeader.strInvDate.equals(todayString)) {
						float trxnValue = getRoundedValue(arHeader.nTrxnAmount * 1.0f / Global.MULTIPLE_VALUE);
						totalAmount += trxnValue;
					}
				}
				dbAdapter.close();

				if (totalAmount + fTotalCollection > creditLimit) {
					try {
						if(mContext.isFinishing() || mContext.isDestroyed()) return;
						MessageDialog msgDialog = new MessageDialog(mContext);
						msgDialog.setCancelable(false);
						msgDialog.mStrMessage = Str.ExceedingCreditLimit[Global.LANG];
						msgDialog.mStrMiddle = Str.Return[Global.LANG];
						msgDialog.setOnDismissListener(dialog -> {
						});
						msgDialog.show();
					}catch (Exception e)
					{
						e.printStackTrace();
					}
					m_bConfirmClicked = false;
					m_bClicked = false;
					return;
				}
			}

			if (!Utils.getStringSetting(mContext, Global.PREF_HIDE_SUMMARY_PAYMENT).equals("1")) {
				if ((Global.gCurCustomerInfo.strPayType.trim().equals("1")||
						Global.gCurCustomerInfo.strPayType.trim().equals("2") ||
						Global.gCurCustomerInfo.strPayType.trim().equals("$"))
						&& mPayMethod == 0 && Global.gCurCustomerInfo.nShowPrice != 2) {
					long lCurTime = System.currentTimeMillis();
					String strCurDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(lCurTime);
					String strDelDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(mDate);

					Date curDate = Utils.getDateFromString(strCurDate);
					Date delDate = Utils.getDateFromString(strDelDate);

					if(curDate.compareTo(delDate) >= 0 && fTotalCollection > 0 && !Global.gProcessedPayment)
					{
						try {
							if(mContext.isFinishing() || mContext.isDestroyed()) return;
							MessageDialog msgDialog = new MessageDialog(mContext);
							msgDialog.setCancelable(false);
							msgDialog.mStrMessage = Str.CODCustomer_Payment_msg[Global.LANG];
							msgDialog.mStrMiddle = Str.Ok[Global.LANG];
							msgDialog.show();
						}catch (Exception e)
						{
							e.printStackTrace();
						}
						m_bConfirmClicked = false;
						m_bClicked = false;
						return;
					}
				}
				else if(((fTotalCollection) > 0 && Global.gCurCustomerInfo.strPayType.trim().equals("3")) && mPayMethod == 0 && Global.gCurCustomerInfo.nShowPrice != 2)
				{
					if (bCodPaymentOptionSelected == false) {
						try {
							if(mContext.isFinishing() || mContext.isDestroyed()) return;
							LongButtonsMessageDialog msgDialog = new LongButtonsMessageDialog(mContext);
							msgDialog.setCancelable(false);
							msgDialog.mStrMessage = String.format(Locale.ENGLISH, Str.ConfirmCollectingPayment[Global.LANG], Global.gCurCustomerInfo.strName);
							msgDialog.mStrBottom = Str.Return[Global.LANG];
							msgDialog.mStrLeft = Str.PartPayment[Global.LANG];
							msgDialog.mStrRight = Str.NoPayment[Global.LANG];
							msgDialog.setOnDismissListener(dialog -> {
								if(msgDialog.mReturnCode == Global.DLG_RETURN_BOTTOM) {
									// Return
								}
								else if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
									// Part Payment
									//bCodPaymentOptionSelected = true;
									handlePartPaymentForCodCustomer();
								}
								else if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT)
								{
									// No Payment
									bCodPaymentOptionSelected = true;
									handleNoPaymentForCodCustomer();
								}
							});
							msgDialog.show();
						}catch (Exception e)
						{
							e.printStackTrace();
						}
						m_bConfirmClicked = false;
						m_bClicked = false;
						return;
					}
				}
			}
			doContinueCodCustomer();
		}
	}

	private void handlePartPaymentForCodCustomer() {

		bShouldDisableCashCheque = true;
		disableCashCheque();

		Bundle bundle = new Bundle();
		bundle.putBoolean(Global.EXTRA_CARD, false);
		bundle.putBoolean(Global.EXTRA_IS_FROM_DELIVERY_SUMMARY, true);

		Global.gUARTotalInfo = new UARTotalInfo();
		Global.gUARPaymentChequeInfo = new UARPaymentInfo();
		Global.gUARPaymentCardInfo = new UARPaymentInfo();
		Global.gUARPaymentEFTInfo = new UARPaymentInfo();

		PaymentCollectionFragment fragment = new PaymentCollectionFragment();
		fragment.setDeliverySummaryFragment(DeliverySummaryFragment.this);
		fragment.setArguments(bundle);
		mContext.replaceFragment(fragment);
	}

	private void disableCashCheque() {
		mRootView.findViewById(R.id.chkCash).setEnabled(false);
		mRootView.findViewById(R.id.chkCheque).setEnabled(false);
		((CheckBox)mRootView.findViewById(R.id.chkCash)).setTextColor(mContext.getResources().getColor(R.color.gray_color));
		((CheckBox)mRootView.findViewById(R.id.chkCheque)).setTextColor(mContext.getResources().getColor(R.color.gray_color));
	}

	public void setShouldDisableCashCheque(boolean bShouldDisableCashCheque) {
		this.bShouldDisableCashCheque = bShouldDisableCashCheque;
	}

	private void handleNoPaymentForCodCustomer() {

		// Disable Cash and Cheque checkboxes
		bShouldDisableCashCheque = true;
		disableCashCheque();

		// sum the open AR amounts in ARINFO
		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();

		String strValueText = ((TextView) mRootView.findViewById(R.id.txtTotalAllValue)).getText().toString();
		float fTotalInvoiceAmount = Utils.getFloatFromString(strValueText.substring(Global.gCurrencySymbol.length()));

		// get credit limit
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
			bWithinCreditLimit = fTotalInvoiceAmount <= creditLimit;
		}

		if (bWithinCreditLimit) {
			doContinueCodCustomer();
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

	public void doContinueCodCustomer()
	{
		if(Global.gCurCustomerInfo.nImageCapture == 1 && mStrPhotoPathList.size() == 0)
		{
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
			m_bConfirmClicked = false;
			return;
		}
		if (mIsCreatedPDF == false) {
			doConfirm();
		} else {
			if( Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 1 )
			{
				try {
					if(mContext.isFinishing() || mContext.isDestroyed()) return;
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
							m_bConfirmClicked = false;
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
							m_bConfirmClicked = false;
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
							m_bConfirmClicked = false;
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
							m_bConfirmClicked = false;
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
				m_bConfirmClicked = false;
			}

		}
	}

	/*private DatePickerDialog.OnDateSetListener dateListener = new DatePickerDialog.OnDateSetListener() {
		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			Date myDate = new Date(year-1900, monthOfYear, dayOfMonth, 0, 0, 0);
			mDate = myDate.getTime();
			showTimes();
		}
	};*/

	private void TaskPayment(String strDocNo)
	{
		String strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(mContext, new Date()));
		int nSeqNo = Utils.getIntSetting(mContext, String.format(Locale.ENGLISH, Global.PREF_COLLECTION_SEQUENCE_NO, Global.gTripNumber));
		//Make ARTotal and AR Payment
		//if(bConsignment) return;

		// Process collection arHeaders and payments
		DbAdapter dbAdapter = new DbAdapter(mContext);
		for (UARPaymentInfo paymentInfo: collectionPaymentInfoList) {
			paymentInfo.strDocNo = strDocNo;
			dbAdapter.updateUARPaymentInfo(paymentInfo);
		}
		dbAdapter.close();

		if(Global.gUARTotalInfoList.size() > 0 || collectionPaymentInfoList.size() > 0)
		{
			Global.gUARTotalInfo = new UARTotalInfo();
			Global.gUARPaymentCardInfo = new UARPaymentInfo();
			Global.gUARPaymentChequeInfo = new UARPaymentInfo();
			Global.gUARPaymentEFTInfo = new UARPaymentInfo();
		}

		try {

			dbAdapter = new DbAdapter(mContext);
			dbAdapter.open();
			Global.gCurHeaderInfo.strInvNo = strDocNo;

			dbAdapter.insertARHeaderInfo(Global.gCurHeaderInfo);
			Global.gCurHeaderInfo = null;
			dbAdapter.close();

			if(mPayMethod > 0 && !m_bFutureOrder)
			{
				dbAdapter.open();
				Global.gUARTotalInfo.nInvoiceAmount = 0;

				String strRealAmount = ((EditText)mRootView.findViewById(R.id.editTotalCollection)).getText().toString();
				float dCurrentArAmount = Float.parseFloat(strRealAmount);
				boolean bInvoiceSel = false;
				List<ARHeaderInfo> infos = dbAdapter.getARHeaderInfo(Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo);
				for (ARHeaderInfo arHeaderInfo : infos) {
					if( arHeaderInfo.nProcessStatus < 2)
					{
						Global.gUARTotalInfo.nInvoiceAmount += arHeaderInfo.nTrxnAmount * 1.0 / Global.MULTIPLE_VALUE;
						bInvoiceSel = true;
					}
				}

				if(bInvoiceSel == false && dCurrentArAmount > 0)
				{
					for (ARHeaderInfo arHeaderInfo : infos) {
						if( arHeaderInfo.nProcessStatus == 2)
						{
							Global.gUARTotalInfo.nInvoiceAmount += arHeaderInfo.nTrxnAmount * 1.0 / Global.MULTIPLE_VALUE;
							arHeaderInfo.nProcessStatus = 1;
							dbAdapter.updateARHeaderInfo(arHeaderInfo);
						}
					}
				}
				dbAdapter.close();

				boolean bFullPayment = false;


				int nInvoiceAmount = Math.round(Global.gUARTotalInfo.nInvoiceAmount * Global.MULTIPLE_VALUE);
				int nCurrentArAmount = Math.round(dCurrentArAmount * Global.MULTIPLE_VALUE);

				if( Math.abs(nInvoiceAmount - nCurrentArAmount) < 1000)
				{
					bFullPayment = true;
				}

				if(mPayMethod != 2 || bReverseMode)
				{
					if( bFullPayment && infos.size() > 0)
					{
						dbAdapter.open();
						long nTrxnNo = mTrxnNo;
						if( nTrxnNo == -1 )
						{
							nTrxnNo = System.currentTimeMillis();
						}
						UCollectionsDInfo uCollectionsDInfo = new UCollectionsDInfo();
						uCollectionsDInfo.nTrxnNo = nTrxnNo;
						uCollectionsDInfo.nSeqNo = nSeqNo;
						if (mPayMethod == 1) {
							uCollectionsDInfo.nPayType = Global.COLLECTION_CASH;
						} else if (mPayMethod == 2) {
							uCollectionsDInfo.nPayType = Global.COLLECTION_CHEQUE;
						} else if (mPayMethod == 3) {
							uCollectionsDInfo.nPayType = Global.COLLECTION_CARD;
						}
						uCollectionsDInfo.strTrxnDate = strTrxnDate;
						uCollectionsDInfo.nTrxnAmount = 0;
						for(ARHeaderInfo arHeaderInfo : infos)
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
								Global.gUARTotalInfo.nTrxnNo = mPdfTrxnNo;
								Global.gUARTotalInfo.nChainNo = Global.gCurCustomerInfo.nChainNo;
								Global.gUARTotalInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
								Global.gUARTotalInfo.nInvoiceTrxnNo = Global.gUOrderHInfo.nTrxnNo;
								Global.gUARTotalInfo.nInvoiceAmount = arHeaderInfo.nTrxnAmount * 1.0f / Global.MULTIPLE_VALUE;
								if(!bConsignment)
								{
									if (Global.gCurCustomerInfo.nInvoiceFmt == 1) {
										if(!bReverseMode)
										{
											Global.gUARTotalInfo.bIsVisitedDelivery = true;
											Global.gUARTotalInfo.bIsVisitedPickup = true;
										}
										else
										{
											Global.gUARTotalInfo.bIsVisitedDelivery = false;
											Global.gUARTotalInfo.bIsVisitedPickup = false;
										}

										Global.gUARTotalInfo.nDeliveryTrxnNo = mPdfTrxnNo;
									} else {
										if (mIsPickup == false) {
											if(!bReverseMode)
											{
												Global.gUARTotalInfo.bIsVisitedDelivery = true;
											}
											else
											{
												Global.gUARTotalInfo.bIsVisitedDelivery = false;
											}
											Global.gUARTotalInfo.nDeliveryTrxnNo = mPdfTrxnNo;
										} else {
											if(!bReverseMode)
											{
												Global.gUARTotalInfo.bIsVisitedPickup = true;
											}
											else
											{
												Global.gUARTotalInfo.bIsVisitedPickup = false;
											}

											Global.gUARTotalInfo.nPickupTrxnNo = mPdfTrxnNo;
										}
									}
								}
								else
								{
									Global.gUARTotalInfo.bIsVisitedDelivery = false;
									Global.gUARTotalInfo.nDeliveryTrxnNo = mPdfTrxnNo;
								}

								dCurrentArAmount = arHeaderInfo.nTrxnAmount * 1.0f / Global.MULTIPLE_VALUE;

								//Comment By Wang
							/*if (Global.gUOrderHInfo.bCentsRound) {
								float fNeg = 1;
								if(dCurrentArAmount < 0)
								{
									fNeg = -1;
								}
								int nTotal = (int) (Utils.getRoundedValue(dCurrentArAmount * fNeg) * Global.MULTIPLE_VALUE);
								int nOffSet = nTotal % 5000;
								int nInvoiceTotal = ((nTotal / 5000) + ((Math.abs(nOffSet) >= 2500) ? 1 : 0)) * 5000;
								nInvoiceTotal *= fNeg;
								dCurrentArAmount = nInvoiceTotal * 1.0f / Global.MULTIPLE_VALUE;
							}*/

								UARPaymentInfo uarPaymentInfo = new UARPaymentInfo();
								uarPaymentInfo.nTrxnNo = nTrxnNo;
								uarPaymentInfo.nChainNo = Global.gCurCustomerInfo.nChainNo;
								uarPaymentInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
								if (arHeaderInfo.strDescription.isEmpty()) {
									uarPaymentInfo.strDescription = mStrPdfFileName.substring(0, mStrPdfFileName.length() - 4);
								}
								else {
									uarPaymentInfo.strDescription = arHeaderInfo.strDescription;
								}
								uarPaymentInfo.strTrxnDate = strTrxnDate;
								uarPaymentInfo.strInvoiceDate = arHeaderInfo.strInvDate;
								if (mPayMethod != 0 && m_bFutureOrder == false) {
									if (mPayMethod == 1) {
										uarPaymentInfo.nPayType = Global.COLLECTION_CASH;
									} else if (mPayMethod == 2) {
										uarPaymentInfo.nPayType = Global.COLLECTION_CHEQUE;
									} else if (mPayMethod == 3) {
										uarPaymentInfo.nPayType = Global.COLLECTION_CARD;
									}
								} else {
									uarPaymentInfo.nPayType = 0;
								}
								uarPaymentInfo.nTrxnAmount = dCurrentArAmount;


								Global.gUARTotalInfo.nTrxnNo = nTrxnNo;
								Global.gUARTotalInfo.nChainNo = Global.gCurCustomerInfo.nChainNo;
								Global.gUARTotalInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
								Global.gUARTotalInfo.strTrxnDate = strTrxnDate;
								Global.gUARTotalInfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(mTrxnDate);
								Global.gUARTotalInfo.strDocNo = mStrPdfFileName.substring(0, mStrPdfFileName.length() - 4);

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

								long nPayTrxnNo = setGeneralUploadData("PAY", Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo, nTrxnNo, "", mStrPdfFileName.substring(0, mStrPdfFileName.length() - 4), mTrxnDate);

								TransactionInfo transactionInfo = new TransactionInfo();
								transactionInfo.strType = "PAY";
								transactionInfo.strTime = new SimpleDateFormat("kk:mm").format(new Date(nPayTrxnNo));
								transactionInfo.strCustomerName = Global.gCurCustomerInfo.strName;
								transactionInfo.strDocNo = mStrPdfFileName.substring(0, mStrPdfFileName.length() - 4);
								transactionInfo.strVoid = bReverseMode ? "Yes" : "No";
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
						uCollectionsDInfo.strInvoiceNo = strDocNo;
						uCollectionsDInfo.strCustomerName = Global.gCurCustomerInfo.strName;
						float nTrxnAmount = Utils.getFloatSetting(mContext, Global.COLLECTIONS_TODAY);
						nTrxnAmount += uCollectionsDInfo.nTrxnAmount;
						dbAdapter.insertUCollectionsDInfo(uCollectionsDInfo);
						Utils.setFloatSetting(mContext, Global.COLLECTIONS_TODAY, nTrxnAmount);
						dbAdapter.close();
					}
					else
					{
						long nTrxnNo = mTrxnNo;
						if( nTrxnNo == -1 )
						{
							nTrxnNo = System.currentTimeMillis();
						}
						Global.gUARTotalInfo.nTrxnNo = mPdfTrxnNo;
						Global.gUARTotalInfo.nChainNo = Global.gCurCustomerInfo.nChainNo;
						Global.gUARTotalInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
						Global.gUARTotalInfo.nInvoiceTrxnNo = Global.gUOrderHInfo.nTrxnNo;
						Global.gUARTotalInfo.nInvoiceAmount = dCurrentArAmount;

						if(!bConsignment)
						{
							if (Global.gCurCustomerInfo.nInvoiceFmt == 1) {
								Global.gUARTotalInfo.bIsVisitedDelivery = true;
								Global.gUARTotalInfo.bIsVisitedPickup = true;
								Global.gUARTotalInfo.nDeliveryTrxnNo = mPdfTrxnNo;
							} else {
								if (mIsPickup == false) {
									Global.gUARTotalInfo.bIsVisitedDelivery = true;
									Global.gUARTotalInfo.nDeliveryTrxnNo = mPdfTrxnNo;
								} else {
									Global.gUARTotalInfo.bIsVisitedPickup = true;
									Global.gUARTotalInfo.nPickupTrxnNo = mPdfTrxnNo;
								}
							}
						}
						else
						{
							Global.gUARTotalInfo.bIsVisitedDelivery = false;
							Global.gUARTotalInfo.nDeliveryTrxnNo = mPdfTrxnNo;
						}


						//Comment By Wang.
					/*if (Global.gUOrderHInfo.bCentsRound) {
						float fNeg = 1;
						if(dCurrentArAmount<0)
						{
							fNeg = -1;
						}
						int nTotal = (int) (Utils.getRoundedValue(dCurrentArAmount * fNeg) * Global.MULTIPLE_VALUE);
						int nOffSet = nTotal % 5000;
						int nInvoiceTotal = ((nTotal / 5000) + ((Math.abs(nOffSet) >= 2500) ? 1 : 0)) * 5000;
						nInvoiceTotal *= fNeg;
						dCurrentArAmount = nInvoiceTotal * 1.0f / Global.MULTIPLE_VALUE;
					}*/

						UARPaymentInfo uarPaymentInfo = new UARPaymentInfo();
						uarPaymentInfo.nTrxnNo = nTrxnNo;
						uarPaymentInfo.nChainNo = Global.gCurCustomerInfo.nChainNo;
						uarPaymentInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
						uarPaymentInfo.strTrxnDate = strTrxnDate;
						if (mPayMethod != 0 && m_bFutureOrder == false) {
							if (mPayMethod == 1) {
								uarPaymentInfo.nPayType = Global.COLLECTION_CASH;
							} else if (mPayMethod == 2) {
								uarPaymentInfo.nPayType = Global.COLLECTION_CHEQUE;
							} else if (mPayMethod == 3) {
								uarPaymentInfo.nPayType = Global.COLLECTION_CARD;
							}
						} else {
							uarPaymentInfo.nPayType = 0;
						}
						uarPaymentInfo.nTrxnAmount = dCurrentArAmount;

						Global.gUARTotalInfo.nTrxnNo = nTrxnNo;
						Global.gUARTotalInfo.nChainNo = Global.gCurCustomerInfo.nChainNo;
						Global.gUARTotalInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
						Global.gUARTotalInfo.strTrxnDate = strTrxnDate;
						Global.gUARTotalInfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(mTrxnDate);
						Global.gUARTotalInfo.strReference = strPaymentReference;
						dbAdapter.open();
						dbAdapter.insertUARTotalInfo(Global.gUARTotalInfo);

						ARHeaderInfo arHeaderInfo = new ARHeaderInfo();
						arHeaderInfo.nProcessStatus = 1;
						arHeaderInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
						arHeaderInfo.nChainNo = Global.gCurCustomerInfo.nChainNo;
						arHeaderInfo.nTrxnAmount = nCurrentArAmount * (-1);
						arHeaderInfo.strInvNo = "0";
						arHeaderInfo.strARTrxnType = "PAY";
						arHeaderInfo.strInvDate = Global.gUARTotalInfo.strTrxnDate;
						dbAdapter.insertARHeaderInfo(arHeaderInfo);

						uarPaymentInfo.strInvoiceDate = arHeaderInfo.strInvDate;
						if(Global.gUOrderHInfo != null)
						{
							uarPaymentInfo.strDocNo = Global.gUOrderHInfo.strDocNo;
						}
						else
						{
							uarPaymentInfo.strDocNo = "0";
						}
						uarPaymentInfo.strInvoiceNo = Global.gUARTotalInfo.strDocNo;
						dbAdapter.insertUARPaymentInfo(uarPaymentInfo);
						Global.gCurCustomerInfo.bCompleted = true;
						dbAdapter.updateCustomerInfo(Global.gCurCustomerInfo);

						nSeqNo++;
						Utils.setIntSetting(mContext, String.format(Locale.ENGLISH, Global.PREF_COLLECTION_SEQUENCE_NO, Global.gTripNumber), nSeqNo);
						long nPayTrxnNo = setGeneralUploadData("PAY", Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo, nTrxnNo, "", mStrPdfFileName.substring(0, mStrPdfFileName.length() - 4), mTrxnDate);

						TransactionInfo transactionInfo = new TransactionInfo();
						transactionInfo.strType = "PAY";
						transactionInfo.strTime = new SimpleDateFormat("kk:mm").format(new Date(nPayTrxnNo));
						transactionInfo.strCustomerName = Global.gCurCustomerInfo.strName;
						transactionInfo.strDocNo = mStrPdfFileName.substring(0, mStrPdfFileName.length() - 4);
						transactionInfo.strVoid = bReverseMode ? "Yes" : "No";
						transactionInfo.value = Global.gUOrderHInfo.nSaleAmount + Global.gUOrderHInfo.nTaxAmount + Global.gUOrderHInfo.nHandlingFee + Global.gUOrderHInfo.nHandlingFeeTaxAmount;
						dbAdapter.insertTransactionHistoryInfo(transactionInfo);

						UCollectionsDInfo uCollectionsDInfo = new UCollectionsDInfo();
						uCollectionsDInfo.nTrxnNo = nTrxnNo;
						uCollectionsDInfo.nSeqNo = nSeqNo;
						if (mPayMethod == 1) {
							uCollectionsDInfo.nPayType = Global.COLLECTION_CASH;
						} else if (mPayMethod == 2) {
							uCollectionsDInfo.nPayType = Global.COLLECTION_CHEQUE;
						} else if (mPayMethod == 3) {
							uCollectionsDInfo.nPayType = Global.COLLECTION_CARD;
						}
						uCollectionsDInfo.strTrxnDate = strTrxnDate;
						uCollectionsDInfo.nTrxnAmount = dCurrentArAmount;
						uCollectionsDInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
						uCollectionsDInfo.nChainNo = Global.gCurCustomerInfo.nChainNo;
						uCollectionsDInfo.strCustomerName = Global.gCurCustomerInfo.strName;
						uCollectionsDInfo.strInvoiceNo = strDocNo;
						float nTrxnAmount = Utils.getFloatSetting(mContext, Global.COLLECTIONS_TODAY);
						dbAdapter.insertUCollectionsDInfo(uCollectionsDInfo);
						nTrxnAmount += uCollectionsDInfo.nTrxnAmount;
						Utils.setFloatSetting(mContext, Global.COLLECTIONS_TODAY, nTrxnAmount);
						dbAdapter.close();
					}
				}
				else
				{
					Global.g_bCurPaymentMethod = bFullPayment;
					Global.gARHeaderInfoList = infos;
					Global.g_bIsPickup = mIsPickup;
					Global.g_bReverseMode = bReverseMode;
					Global.g_strDocNo = strDocNo;
					Global.dCurrentArAmount = dCurrentArAmount;
					Global.gPaymentReference = strPaymentReference;
				}


				dbAdapter.open();
				Global.gCurCustomerInfo.bCompleted = true;
				dbAdapter.updateCustomerInfo(Global.gCurCustomerInfo);
				dbAdapter.close();

				Global.gUARTotalInfoList.add(Global.gUARTotalInfo);
				Global.gUARPaymentChequeInfoList.add(Global.gUARPaymentChequeInfo);
				Global.gUARPaymentCardInfoList.add(Global.gUARPaymentCardInfo);
				Global.gUARPaymentEFTInfoList.add(Global.gUARPaymentCardInfo);
			}
		}
	catch(Exception e)
		{
			e.printStackTrace();
		}
	}


	public void onEwayPayment()
	{
		final String strReference = "";
		final String strDelDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(mDate);
		try {
			if(mContext.isFinishing() || mContext.isDestroyed()) return;
			final EwayPaymentDialog dlg = new EwayPaymentDialog(mContext);
//				dlg.mAmount = mTotalRemaining;
			Global.gUARPaymentCardInfo.nTrxnAmount = Global.gCurRealPaymentAmount;
			dlg.mAmount = Global.gCurRealPaymentAmount;
			dlg.setCancelable(false);
			dlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
					if (dlg.mReturnCode == Global.DLG_RETURN_RIGHT) {
						Global.gUARPaymentCardInfo.nTrxnAmount = dlg.mAmount;
						strCardType = "eway";
						transaction_id = dlg.mTransactionID;
						processConfirm(strDelDate, strReference, true);
					} else if (dlg.mReturnCode == Global.DLG_RETURN_MIDDLE) {
						CardDetailFragment fragment = new CardDetailFragment();
						fragment.setParentFragment(DeliverySummaryFragment.this);
						mContext.replaceFragment(fragment);
					} else {
						m_bConfirmClicked = false;
						((CheckBox) mRootView.findViewById(R.id.chkCheque)).setChecked(false);
						((CheckBox) mRootView.findViewById(R.id.chkCash)).setChecked(false);
						((CheckBox) mRootView.findViewById(R.id.chkCard)).setChecked(false);
						Global.gCurRealPaymentAmount = 0;
						Global.gUARPaymentCardInfo.nTrxnAmount = 0;
						mPayMethod = 0;
					}
				}
			});
			dlg.show();
		}catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	public void onStripePayment()
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
		final String strReference = mStrPdfFileName.substring(0, mStrPdfFileName.length() - 4);
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
									if(mContext.isFinishing() || mContext.isDestroyed()) return;
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
												doProcessSavedCardStripeProcessing(savedCardList.get(0).addInfo);
											} else if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
												showStripePaymentDialog();
											}
										}
									});
									msgDialog.show();
								}catch (Exception e){

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
								showStripePaymentDialog();
							}
						});
					}
					else
					{
						new Handler(Looper.getMainLooper()).post(new Runnable() {
							@Override
							public void run() {
								hideProgressDialog();
								try
								{
									if(mContext.isFinishing() || mContext.isDestroyed()) return;
									final StripeSavedCardSelectDialog msgDialog = new StripeSavedCardSelectDialog(mContext, savedCardList);
									msgDialog.setCancelable(false);
									msgDialog.mStrMessage = Str.UseSavedFollowingCard[Global.LANG];
									msgDialog.mStrLeft = Str.No[Global.LANG];
									msgDialog.mStrRight = Str.Yes[Global.LANG];
									msgDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
										@Override
										public void onDismiss(DialogInterface dialog) {
											if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
												doProcessSavedCardStripeProcessing(savedCardList.get(msgDialog.mSelIdx).addInfo);
											} else if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
												showStripePaymentDialog();
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
			showStripePaymentDialog();
		}
	}

	private void doProcessSavedCardStripeProcessing(final CustomerAddInfo addInfo)
	{
		if(addInfo == null)
		{
			showStripePaymentDialog();
		}
		else
		{
			showProgressDialog(Str.Loading[Global.LANG]);
			new Thread(){
				@Override
				public void run() {
					String strCurrency = Utils.getStringSetting(mContext, Global.PREF_CURRENCY);
					if(TextUtils.isEmpty(strCurrency))
					{
						strCurrency = "AUD";
					}

					Map<String, Object> chargeParams = new HashMap<>();
					chargeParams.put("amount", (int)(Global.gCurRealPaymentAmount * 100));
					chargeParams.put("currency", strCurrency);
					chargeParams.put("customer", addInfo.strInfo);
					final String strReference = mStrPdfFileName.substring(0, mStrPdfFileName.length() - 4);
					chargeParams.put("description", "Inv No : " + strReference);
					try {
						final Charge charge = Charge.create(chargeParams);
						if(charge.getStatus().equals("succeeded"))
						{
							new Handler(Looper.getMainLooper()).post(new Runnable() {
								@Override
								public void run() {
									hideProgressDialog();
									Utils.printDebug(mContext, Str.SuccessToPay[Global.LANG]);
									Global.gUARPaymentCardInfo.nTrxnAmount = Global.gCurRealPaymentAmount;
									strCardType = "stripe";
									transaction_id = charge.getId();
									final String strReference = mStrPdfFileName.substring(0, mStrPdfFileName.length() - 4);
									final String strDelDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(mContext, new Date(mDate)));
									processConfirm(strDelDate, strReference, true);
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
									m_bConfirmClicked = false;
									((CheckBox) mRootView.findViewById(R.id.chkCheque)).setChecked(false);
									((CheckBox) mRootView.findViewById(R.id.chkCash)).setChecked(false);
									((CheckBox) mRootView.findViewById(R.id.chkCard)).setChecked(false);
									Global.gCurRealPaymentAmount = 0;
									Global.gUARPaymentCardInfo.nTrxnAmount = 0;
									mPayMethod = 0;
								}
							});
						}
					} catch (StripeException e) {
						new Handler(Looper.getMainLooper()).post(new Runnable() {
							@Override
							public void run() {
								hideProgressDialog();
								Utils.printDebug(mContext, Str.PaymentDeclined[Global.LANG]);
								m_bConfirmClicked = false;
								((CheckBox) mRootView.findViewById(R.id.chkCheque)).setChecked(false);
								((CheckBox) mRootView.findViewById(R.id.chkCash)).setChecked(false);
								((CheckBox) mRootView.findViewById(R.id.chkCard)).setChecked(false);
								Global.gCurRealPaymentAmount = 0;
								Global.gUARPaymentCardInfo.nTrxnAmount = 0;
								mPayMethod = 0;
							}
						});
					}
				}
			}.start();
		}
	}

	private void showStripePaymentDialog()
	{
		final String strReference = mStrPdfFileName.substring(0, mStrPdfFileName.length() - 4);
		final String strDelDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(mDate);
		try {
			if(mContext.isFinishing() || mContext.isDestroyed()) return;

			Intent intent = new Intent(mContext, StripePaymentActivity.class);
			intent.putExtra("reference", strReference);
			intent.putExtra("amount", Global.gCurRealPaymentAmount);
			startActivityForResult(intent, STRIPE_PAYMENT);
			/*final NewStripePaymentDialog dlg = new NewStripePaymentDialog(mContext);
			Global.gUARPaymentCardInfo.nTrxnAmount = Global.gCurRealPaymentAmount;
			dlg.mAmount = Global.gCurRealPaymentAmount;
			dlg.mStrReference = strReference;
			dlg.setCancelable(false);
			dlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
					if (dlg.mReturnCode == Global.DLG_RETURN_RIGHT) {
						Global.gUARPaymentCardInfo.nTrxnAmount = dlg.mAmount;
						strCardType = "stripe";
						transaction_id = dlg.mTransactionID;

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
							setGeneralUploadData("CSFM", Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo, -1, mTrxnDate);
							uCustomerFMInfo.nTrxnNo = m_lTrxnNo;

							uCustomerFMInfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(mTrxnDate);
							uCustomerFMInfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(mTrxnDate);
							uCustomerFMInfo.strStartDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(mTrxnDate);
							uCustomerFMInfo.strStartTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(mTrxnDate);
							uCustomerFMInfo.strEndDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(mTrxnDate);
							uCustomerFMInfo.strEndTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(mTrxnDate);

							DbAdapter dbAdapter = new DbAdapter(mContext);
							dbAdapter.open();
							dbAdapter.insertCustAddInfo(customerAddInfo);
							dbAdapter.insertUCustomerFMInfo(uCustomerFMInfo);
							dbAdapter.close();

							//Utils.registerUploadFile(mContext, makeZipFileInfo);
							processConfirm(strDelDate, strReference, false);
						}
						else
						{
							processConfirm(strDelDate, strReference, true);
						}


					} else if (dlg.mReturnCode == Global.DLG_RETURN_MIDDLE) {
						CardDetailFragment fragment = new CardDetailFragment();
						fragment.setParentFragment(DeliverySummaryFragment.this);
						mContext.replaceFragment(fragment);
					} else {
						m_bConfirmClicked = false;
						((CheckBox) mRootView.findViewById(R.id.chkCheque)).setChecked(false);
						((CheckBox) mRootView.findViewById(R.id.chkCash)).setChecked(false);
						((CheckBox) mRootView.findViewById(R.id.chkCard)).setChecked(false);
						Global.gCurRealPaymentAmount = 0;
						Global.gUARPaymentCardInfo.nTrxnAmount = 0;
						mPayMethod = 0;
					}
				}
			});
			dlg.show();*/
		}catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	@Override
	public void onMintNotPresent() {
		final String strReference = "";
		final String strDelDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(mContext, new Date(mDate)));
		try {
			if(mContext.isFinishing() || mContext.isDestroyed()) return;
			final MintPaymentDialog dlg = new MintPaymentDialog(mContext);
//				dlg.mAmount = mTotalRemaining;
		Global.gUARPaymentCardInfo.nTrxnAmount = Global.gCurRealPaymentAmount;
		dlg.mAmount = Global.gCurRealPaymentAmount;
		dlg.setCancelable(false);
		dlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				if (dlg.mReturnCode == Global.DLG_RETURN_RIGHT) {
					Global.gUARPaymentCardInfo.nTrxnAmount = dlg.mAmount;
					strCardType = "mint_not_present";
					transaction_id = dlg.mTransactionID;
					processConfirm(strDelDate, strReference, true);
				} else if (dlg.mReturnCode == Global.DLG_RETURN_MIDDLE) {
					CardDetailFragment fragment = new CardDetailFragment();
					fragment.setParentFragment(DeliverySummaryFragment.this);
					mContext.replaceFragment(fragment);
				} else {
					m_bConfirmClicked = false;
					((CheckBox) mRootView.findViewById(R.id.chkCheque)).setChecked(false);
					((CheckBox) mRootView.findViewById(R.id.chkCash)).setChecked(false);
					((CheckBox) mRootView.findViewById(R.id.chkCard)).setChecked(false);
					Global.gCurRealPaymentAmount = 0;
					Global.gUARPaymentCardInfo.nTrxnAmount = 0;
					mPayMethod = 0;
				}
			}
		});
		dlg.show();
		}catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	private void doWindCaveByRESTAPI()
	{
		final String strReference = "";
		final String strDelDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(mDate);
		try {
			if(mContext.isFinishing() || mContext.isDestroyed()) return;
			final WindCavePaymentDialog dlg = new WindCavePaymentDialog(mContext);
			Global.gUARPaymentCardInfo.nTrxnAmount = Global.gCurRealPaymentAmount;
			dlg.mAmount = Global.gCurRealPaymentAmount;
			dlg.setCancelable(false);
			dlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
					if (dlg.mReturnCode == Global.DLG_RETURN_RIGHT) {
						Global.gUARPaymentCardInfo.nTrxnAmount = dlg.mAmount;
						strCardType = "windcave_not_present";
						transaction_id = dlg.mTransactionID;
						processConfirm(strDelDate, strReference, true);
					} else if (dlg.mReturnCode == Global.DLG_RETURN_MIDDLE) {
						CardDetailFragment fragment = new CardDetailFragment();
						fragment.setParentFragment(DeliverySummaryFragment.this);
						mContext.replaceFragment(fragment);
					} else {
						m_bConfirmClicked = false;
						((CheckBox) mRootView.findViewById(R.id.chkCheque)).setChecked(false);
						((CheckBox) mRootView.findViewById(R.id.chkCash)).setChecked(false);
						((CheckBox) mRootView.findViewById(R.id.chkCard)).setChecked(false);
						Global.gCurRealPaymentAmount = 0;
						Global.gUARPaymentCardInfo.nTrxnAmount = 0;
						mPayMethod = 0;
					}
				}
			});
			dlg.show();
		}catch (Exception e)
		{
			e.printStackTrace();
		}

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
				if(mContext.isFinishing() || mContext.isDestroyed()) return;
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

			final String strReference = Global.gCurCustomerInfo.nChainNo + "/" + Global.gCurCustomerInfo.nCustNo + "--(" + mStrPdfFileName.substring(0, mStrPdfFileName.length() - 4) + ")";
			Intent intent = new Intent(mContext, WindCaveActivity.class);
			intent.putExtra("reference", strReference);
			intent.putExtra("card_present", true);
			startActivityForResult(intent, WIND_CAVE_API_CALL);
		}


	}


	public void onWindCavePresent(){
		doWindCaveCardPresentBySDK();
	}

	public void onWindCaveNotPresent() {
		doWindCaveBySDK();
	}

	public void onMintIFrame()
	{
		//Launch the WebView Activity
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

		String strReference = mStrPdfFileName.substring(0, mStrPdfFileName.length() - 4);

		Intent intent = new Intent(mContext, MintIFrameActivity.class);
		intent.putExtra("reference", Global.gCurCustomerInfo.nChainNo + "-" + Global.gCurCustomerInfo.nCustNo + "-" + strReference);
		intent.putExtra("amount", Global.gCurRealPaymentAmount);
		startActivityForResult(intent, Global.MINT_IFRAME);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == Global.MINT_IFRAME && resultCode == Activity.RESULT_OK)
		{
			String strUrl = data.getStringExtra("url");
			Uri uri = Uri.parse(strUrl);
			if(uri.getQueryParameter("successful") != null && uri.getQueryParameter("successful").equals("true"))
			{
				String strDelDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(mContext, new Date(mDate)));
				final String strReference = "";
				strCardType = "mint_iframe";
				transaction_id = uri.getQueryParameter("id");
				if(!mContext.isFinishing() && !mContext.isDestroyed())
				{
					try {
						Toast.makeText(mContext, Str.PaymentSuccessful[Global.LANG], Toast.LENGTH_LONG).show();
					}catch (Exception e1)
					{
						e1.printStackTrace();
					}

				}
				processConfirm(strDelDate, strReference, true);
			}
			else
			{
				if(!mContext.isFinishing() && !mContext.isDestroyed())
				{
					try {
						Toast.makeText(mContext, uri.getQueryParameter("message"), Toast.LENGTH_LONG).show();
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
				transaction_id = data.getStringExtra("trxn_id");
				String strReference = data.getStringExtra("reference");
				Global.gUARPaymentCardInfo.nTrxnAmount = Global.gCurRealPaymentAmount;
				strCardType = "windcave_not_present";
				String strDelDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(mContext, new Date(mDate)));
				processConfirm(strDelDate, transaction_id, true);
			}
			else
			{
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
		else if(requestCode == STRIPE_PAYMENT)
		{
			m_bConfirmClicked = false;
			if(resultCode == Activity.RESULT_OK)
			{
				if(data == null ) return;
				String strReturnType = data.getStringExtra("return_type");
				if(strReturnType.equals("middle"))
				{
					CardDetailFragment fragment = new CardDetailFragment();
					fragment.setParentFragment(DeliverySummaryFragment.this);
					mContext.replaceFragment(fragment);
				}
				else {
					float nTrxnAmount = data.getFloatExtra("trxnAmount", 0);
					String strTrxnId = data.getStringExtra("trxnID");
					Global.gUARPaymentCardInfo.nTrxnAmount = nTrxnAmount;
					strCardType = "stripe";
					transaction_id = strTrxnId;
					final String strReference = mStrPdfFileName.substring(0, mStrPdfFileName.length() - 4);
					final String strDelDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(mContext, new Date(mDate)));
					processConfirm(strDelDate, strReference, true);
				}
			}
			else {
				((CheckBox) mRootView.findViewById(R.id.chkCheque)).setChecked(false);
				((CheckBox) mRootView.findViewById(R.id.chkCash)).setChecked(false);
				((CheckBox) mRootView.findViewById(R.id.chkCard)).setChecked(false);
				Global.gCurRealPaymentAmount = 0;
				Global.gUARPaymentCardInfo.nTrxnAmount = 0;
				mPayMethod = 0;
			}
		}
		else
		{
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	public void onMintCardPresent(boolean bEway) {
		String mintApiKey = Utils.getStringSetting(mContext, Global.PREF_MINTAPI);
		String mintSecretKey = Utils.getStringSetting(mContext, Global.PREF_MINTSECRET);
		if(!TextUtils.isEmpty(mintApiKey) && !TextUtils.isEmpty(mintSecretKey))
		{
			mContext.initMintegrate();
			mTrxnNo = System.currentTimeMillis();
			mContext.doMintPayment(String.valueOf(Global.gCurCustomerInfo.nCustNo), Math.round(Global.gCurRealPaymentAmount * 100), mTrxnNo);
		}
		else
		{
			if(bEway)
			{
				onEwayPayment();
			}
			else
			{
				onMintNotPresent();
			}

		}
	}

	public static float getTotalInvoiceValue(Context context) {
		return getTotalInvoiceValue(context, Global.gUOrderHInfo.deliveryList, Global.gUOrderHInfo.pickupList);
	}

	public static float getTotalInvoiceValue(Context context, List<UOrderDInfo> deliveryList, List<UOrderDInfo> pickupList) {

		float nSalesValue = 0;

		UOrderHInfo uOrderHInfo = new UOrderHInfo();

		uOrderHInfo.nTotalDumps = 0;
		uOrderHInfo.nTotalBuybacks = 0;
		uOrderHInfo.nTotalAmount = 0;

		uOrderHInfo.nTaxAmount = 0;
		uOrderHInfo.nTotalFree = 0;

		uOrderHInfo.nPromotionAmount = 0;
		uOrderHInfo.nDiscBuybacks = 0;
		uOrderHInfo.nDiscDumps = 0;
		uOrderHInfo.nDiscSales = 0;
		uOrderHInfo.nNetBuybacks = 0;
		uOrderHInfo.nNetDumps = 0;
		uOrderHInfo.nNetSales = 0;
		uOrderHInfo.nPrintCDSAmount = 0;

		int nDeliveredCnt = 0;
		int nReturnsCnt = 0;
		int nBuybackCnt = 0;
		int nContainersCnt = 0;

		boolean bPrintZeroQtyPresoldItem = true;

		DbAdapter dbAdapter = new DbAdapter(context);
		dbAdapter.open();
		for (UOrderDInfo info : deliveryList) {
			if(Utils.getProdLoconSCWType(context, info.strItemNo) == Global.WEIGHT_ITEM)
			{
				if (info.dSales != 0 || (bPrintZeroQtyPresoldItem && info.isFromPresoldOrder)) {
					uOrderHInfo.dTotalAmount += info.dSales;
					nSalesValue += Utils.getRoundedValue(info.dSales * info.nPrice);
					if(Utils.getStringSetting(context, Global.PREF_INVCALC).equals("TTx"))
					{
						uOrderHInfo.nDiscSales += Utils.getRoundedValueForTTx(info.dSales * Utils.getRoundedValueForTTx(info.nBasePrice - info.nPrice));
					}
					else
					{
						uOrderHInfo.nDiscSales += info.dSales * (info.nBasePrice - info.nGrossPrice);
					}

					uOrderHInfo.nNetSales += info.dSales * (info.nPrice);
					uOrderHInfo.nPromotionAmount += info.dSales * (info.nGrossPrice - info.nPrice);
					boolean bExistFreeItem = false;
					for(UPromotionInfo uPromotionInfo : info.uPromotionInfoList)
					{
						uPromotionInfo.nTrxnType = info.nTrxnType;
						uPromotionInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
						uPromotionInfo.nChainNo = Global.gCurCustomerInfo.nChainNo;
						uPromotionInfo.strItemNo = info.strItemNo;
						//Global.gUOrderHInfo.nPromotionAmount += info.dSales * Utils.getDividedValue(uPromotionInfo.nAmount);
						if( uPromotionInfo.nPromoType == Global.PROMO_TYPE_BUY_FREE_MULTI )
						{
							bExistFreeItem = true;
						}
					}

					ProdLocnInfo prodLocnInfo = dbAdapter.getProdLocnInfo(info.strItemNo);
					if (prodLocnInfo != null && prodLocnInfo.strTaxFlag.equals("Y")) {
						info.strItemTaxCode = prodLocnInfo.strItemTaxCode;
					}
					//TaxRatesInfo taxRatesInfo = dbAdapter.getTaxRatesInfo(Global.gCurCustomerInfo.strTaxCode, info.strItemNo);

					if (bExistFreeItem) {
						double dFreeQty = (Utils.myDivider((float)info.dSales, info.nPromoDiscount));
						uOrderHInfo.dTotalFree += dFreeQty;
					}

					uOrderHInfo.nTaxAmount += info.nTaxAmount;
					uOrderHInfo.nPrintCDSAmount += info.nPrintCDSAmount;

					if (info.nSCWType == Global.INVENTORY_TRUCK || info.nSCWType == Global.WEIGHT_ITEM) {
						nDeliveredCnt++;
					} else {
						nContainersCnt++;
					}
				}
			}
			else
			{
				if (info.nSales != 0 || (bPrintZeroQtyPresoldItem && info.isFromPresoldOrder)) {
					uOrderHInfo.nTotalAmount += info.nSales;
					nSalesValue += Utils.getRoundedValue(info.nSales * info.nPrice);
					if(Utils.getStringSetting(context, Global.PREF_INVCALC).equals("TTx"))
					{
						uOrderHInfo.nDiscSales += Utils.getRoundedValueForTTx(info.nSales * Utils.getRoundedValueForTTx(info.nBasePrice - info.nPrice));
					}
					else
					{
						uOrderHInfo.nDiscSales += info.nSales * (info.nBasePrice - info.nGrossPrice);
					}

					uOrderHInfo.nNetSales += info.nSales * (info.nPrice);
					uOrderHInfo.nPromotionAmount += info.nSales * (info.nGrossPrice - info.nPrice);
					boolean bExistFreeItem = false;
					for(UPromotionInfo uPromotionInfo : info.uPromotionInfoList)
					{
						uPromotionInfo.nTrxnType = info.nTrxnType;
						uPromotionInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
						uPromotionInfo.nChainNo = Global.gCurCustomerInfo.nChainNo;
						uPromotionInfo.strItemNo = info.strItemNo;
						//Global.gUOrderHInfo.nPromotionAmount += info.nSales * Utils.getDividedValue(uPromotionInfo.nAmount);
						if( uPromotionInfo.nPromoType == Global.PROMO_TYPE_BUY_FREE_MULTI )
						{
							bExistFreeItem = true;
						}
					}

					ProdLocnInfo prodLocnInfo = dbAdapter.getProdLocnInfo(info.strItemNo);
					if (prodLocnInfo != null && prodLocnInfo.strTaxFlag.equals("Y")) {
						info.strItemTaxCode = prodLocnInfo.strItemTaxCode;
					}
					//TaxRatesInfo taxRatesInfo = dbAdapter.getTaxRatesInfo(Global.gCurCustomerInfo.strTaxCode, info.strItemNo);

					if (bExistFreeItem) {
						int nFreeQty = Math.round(Utils.myDivider(info.nSales, info.nPromoDiscount));
						uOrderHInfo.nTotalFree += nFreeQty;
					}

					uOrderHInfo.nTaxAmount += info.nTaxAmount;
					uOrderHInfo.nPrintCDSAmount += info.nPrintCDSAmount;
					if (info.nSCWType == Global.INVENTORY_TRUCK || info.nSCWType == Global.WEIGHT_ITEM) {
						nDeliveredCnt++;
					} else {
						nContainersCnt++;
					}
				}
			}
		}
		for (UOrderDInfo info : pickupList) {
			if(Utils.getProdLoconSCWType(context, info.strItemNo) == Global.WEIGHT_ITEM)
			{
				if (info.dDumps != 0 || (bPrintZeroQtyPresoldItem && info.isFromPresoldOrder)) {
					if(info.nTrxnType == 1)
					{
						uOrderHInfo.dTotalDumps += info.dDumps;
					}
					else
					{
						uOrderHInfo.dTotalBuybacks += info.dDumps;
					}
					uOrderHInfo.dTotalAmount -= info.dDumps;
					nSalesValue -= Utils.getRoundedValue(info.dDumps * info.nPrice);
					uOrderHInfo.nPromotionAmount -= info.dDumps * (info.nGrossPrice - info.nPrice);
					boolean bExist = false;
					for(UPromotionInfo uPromotionInfo : info.uPromotionInfoList)
					{
						uPromotionInfo.nTrxnType = info.nTrxnType;
						uPromotionInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
						uPromotionInfo.nChainNo = Global.gCurCustomerInfo.nChainNo;
						uPromotionInfo.strItemNo = info.strItemNo;
						//Global.gUOrderHInfo.nPromotionAmount -= info.dDumps * Utils.getDividedValue(uPromotionInfo.nAmount);
						if( uPromotionInfo.nPromoType == Global.PROMO_TYPE_BUY_FREE_MULTI )
						{
							bExist = true;
						}
					}

					ProdLocnInfo prodLocnInfo = dbAdapter.getProdLocnInfo(info.strItemNo);
					if (prodLocnInfo != null && prodLocnInfo.strTaxFlag.equals("Y")) {
						info.strItemTaxCode = prodLocnInfo.strItemTaxCode;
					}
					//TaxRatesInfo taxRatesInfo = dbAdapter.getTaxRatesInfo(Global.gCurCustomerInfo.strTaxCode, info.strItemNo);
					if (bExist) {
						double dFreeQty = (Utils.myDivider((float)info.dDumps, info.nPromoDiscount));
						uOrderHInfo.dTotalFree += dFreeQty;
					}

					if(info.nTrxnType == 1)
					{
						if(Utils.getStringSetting(context, Global.PREF_INVCALC).equals("TTx"))
						{
							uOrderHInfo.nDiscDumps += Utils.getRoundedValueForTTx(info.dDumps * Utils.getRoundedValueForTTx(info.nBasePrice - info.nPrice));
						}
						else
						{
							uOrderHInfo.nDiscDumps += info.dDumps * (info.nBasePrice - info.nGrossPrice);
						}

						uOrderHInfo.nNetDumps += info.dDumps * (info.nPrice);
					}
					else
					{
						if(Utils.getStringSetting(context, Global.PREF_INVCALC).equals("TTx"))
						{
							uOrderHInfo.nDiscBuybacks += Utils.getRoundedValueForTTx(info.dDumps * Utils.getRoundedValueForTTx(info.nBasePrice - info.nPrice));
						}
						else
						{
							uOrderHInfo.nDiscBuybacks += info.dDumps * (info.nBasePrice - info.nGrossPrice);
						}

						uOrderHInfo.nNetBuybacks += info.dDumps * (info.nPrice);
					}
					uOrderHInfo.nTaxAmount -= info.nTaxAmount;
					uOrderHInfo.nPrintCDSAmount -= info.nPrintCDSAmount;

					if ((info.nSCWType == Global.INVENTORY_TRUCK || info.nSCWType == Global.WEIGHT_ITEM) && info.nTrxnType == Global.TRXN_PICKUP) {
						nReturnsCnt++;
					} else if ((info.nSCWType == Global.INVENTORY_TRUCK || info.nSCWType == Global.WEIGHT_ITEM) && info.nTrxnType == Global.TRXN_BUYBACK) {
						nBuybackCnt++;
					} else if(info.nSCWType != Global.INVENTORY_TRUCK && info.nSCWType != Global.WEIGHT_ITEM){
						nContainersCnt++;
					}
				}
			}
			else
			{
				if (info.nDumps != 0 || (bPrintZeroQtyPresoldItem && info.isFromPresoldOrder)) {
					if(info.nTrxnType == 1)
					{
						uOrderHInfo.nTotalDumps += info.nDumps;
					}
					else
					{
						uOrderHInfo.nTotalBuybacks += info.nDumps;
					}
					uOrderHInfo.nTotalAmount -= info.nDumps;
					nSalesValue -= Utils.getRoundedValue(info.nDumps * info.nPrice);
					uOrderHInfo.nPromotionAmount -= info.nDumps * (info.nGrossPrice - info.nPrice);
					boolean bExist = false;
					for(UPromotionInfo uPromotionInfo : info.uPromotionInfoList)
					{
						uPromotionInfo.nTrxnType = info.nTrxnType;
						uPromotionInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
						uPromotionInfo.nChainNo = Global.gCurCustomerInfo.nChainNo;
						uPromotionInfo.strItemNo = info.strItemNo;
						//Global.gUOrderHInfo.nPromotionAmount -= info.nDumps * Utils.getDividedValue(uPromotionInfo.nAmount);
						if( uPromotionInfo.nPromoType == Global.PROMO_TYPE_BUY_FREE_MULTI )
						{
							bExist = true;
						}
					}

					ProdLocnInfo prodLocnInfo = dbAdapter.getProdLocnInfo(info.strItemNo);
					if (prodLocnInfo != null && prodLocnInfo.strTaxFlag.equals("Y")) {
						info.strItemTaxCode = prodLocnInfo.strItemTaxCode;
					}
					if (bExist) {
						int nFreeQty = Math.round(Utils.myDivider(info.nDumps, info.nPromoDiscount));
						uOrderHInfo.nTotalFree += nFreeQty;
					}

					if(info.nTrxnType == 1)
					{
						if(Utils.getStringSetting(context, Global.PREF_INVCALC).equals("TTx")) {
							uOrderHInfo.nDiscDumps += Utils.getRoundedValueForTTx(info.nDumps * Utils.getRoundedValueForTTx(info.nBasePrice - info.nPrice));
						} else {
							uOrderHInfo.nDiscDumps += info.nDumps * (info.nBasePrice - info.nGrossPrice);
						}

						uOrderHInfo.nNetDumps += info.nDumps * (info.nPrice);
					}
					else
					{
						if(Utils.getStringSetting(context, Global.PREF_INVCALC).equals("TTx")) {
							uOrderHInfo.nDiscBuybacks += Utils.getRoundedValueForTTx(info.nDumps * Utils.getRoundedValueForTTx(info.nBasePrice - info.nPrice));
						}
						else
						{
							uOrderHInfo.nDiscBuybacks += info.nDumps * (info.nBasePrice - info.nGrossPrice);
						}
						uOrderHInfo.nNetBuybacks += info.nDumps * (info.nPrice);
					}
					uOrderHInfo.nTaxAmount -= info.nTaxAmount;
					uOrderHInfo.nPrintCDSAmount -= info.nPrintCDSAmount;

					if ((info.nSCWType == Global.INVENTORY_TRUCK || info.nSCWType == Global.WEIGHT_ITEM) && info.nTrxnType == Global.TRXN_PICKUP) {
						nReturnsCnt++;
					} else if ((info.nSCWType == Global.INVENTORY_TRUCK || info.nSCWType == Global.WEIGHT_ITEM) && info.nTrxnType == Global.TRXN_BUYBACK) {
						nBuybackCnt++;
					} else if(info.nSCWType != Global.INVENTORY_TRUCK && info.nSCWType != Global.WEIGHT_ITEM){
						nContainersCnt++;
					}
				}
			}
		}
		dbAdapter.close();

		uOrderHInfo.nSaleAmount = Utils.getRoundedValue(nSalesValue);
		float result = uOrderHInfo.nSaleAmount + uOrderHInfo.nTaxAmount;
		return result;
	}

	private class PrintPdfTask extends AsyncTask<Void, String, String> {

		int nMessageType = 0;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			try
			{
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
							if(bConsignment)
							{
								printer.sendFileContents(Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + mStrPdfFileName);
							}
							else
							{
								if(!TextUtils.isEmpty(mStrPrintPDFFileName))
								{
									printer.sendFileContents(mStrPrintPDFFileName);
								}
								else
								{
									String strPDFFormat = Utils.getStringSetting(mContext, Global.PREF_UPLOAD_INV_FMT);
									if( strPDFFormat.equals("1") )
									{
										printer.sendFileContents(Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + mStrPdfFileName_);
									}
									else
									{
										printer.sendFileContents(Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + mStrPdfFileName);
									}
								}

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
			try
			{
				hideProgressDialog();
				if (result.length() == 0) {
					Utils.setStringSetting(mContext, Global.PREF_PRINTER_MAC_ADDRESS, mStrMacAddress);
					Utils.printDebug(mContext, Str.SuccessToPrint[Global.LANG]);
					Global.gUOrderHInfo.nPrintedFlag = 1;
					DbAdapter dbAdapter = new DbAdapter(mContext);
					dbAdapter.open();
					dbAdapter.updateUOrderHInfo(Global.gUOrderHInfo);
					dbAdapter.close();
					m_bConfirmClicked = false;
					goBack();
				} else {
					if(mContext.isFinishing() || mContext.isDestroyed()) return;
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
								m_bConfirmClicked = false;
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

	public void doProcessMintCard(MTGAppSwitchResponse response)
	{
		if(!bReverseMode)
		{
			if(response != null)
			{
				String error = response.getErrorMessage();
				if(!TextUtils.isEmpty(error))
				{
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
					String strDelDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(mContext, new Date(mDate)));
					Global.gUARPaymentCardInfo.nTrxnAmount = Global.gCurRealPaymentAmount;
					String strReference = response.getTransactionRequestId();
					strPaymentReference = strReference;
					strCardType = "mint";
					String strAmount = String.valueOf(Math.round(Global.gCurRealPaymentAmount * 100));
					String strTrxnNo = String.valueOf(mTrxnNo);
					transaction_id = strPaymentReference + "||" + strAmount + "||" + strTrxnNo;
					processConfirm(strDelDate, strReference, true);
				}
			}
			else
			{
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
		else
		{
			if(response != null)
			{
				String error = response.getErrorMessage();
				if(!TextUtils.isEmpty(error))
				{
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
					String strDelDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(mContext, new Date(mDate)));
					processConfirm(strDelDate, "", true);
				}
			}
			else
			{
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


	}
}