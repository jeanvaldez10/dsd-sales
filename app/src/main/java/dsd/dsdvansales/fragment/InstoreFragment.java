package dsd.dsdvansales.fragment;

import android.app.*;
import android.bluetooth.*;
import android.content.*;
import android.content.DialogInterface.*;
import android.graphics.*;
import android.os.*;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.zebra.sdk.comm.*;
import com.zebra.sdk.printer.*;
import com.zebra.sdk.printer.discovery.*;

import java.text.*;
import java.util.*;
import java.util.concurrent.CountDownLatch;

import dsd.dsdvansales.R;
import dsd.dsdvansales.api.ConnectApiClient;
import dsd.dsdvansales.db.DbAdapter;
import dsd.dsdvansales.dialogs.MessageDialog;
import dsd.dsdvansales.dialogs.NewOrdersFoundDialog;
import dsd.dsdvansales.dialogs.OrderDeliveryOptionDialog;
import dsd.dsdvansales.dialogs.PostVisitDetailsDialog;
import dsd.dsdvansales.dialogs.PresoldDialog;
import dsd.dsdvansales.dialogs.PrintActionDialog;
import dsd.dsdvansales.global.BixolonPrinter;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.HoneywellLinePrinter;
import dsd.dsdvansales.global.HoneywellPrinter;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.model.ARHeaderInfo;
import dsd.dsdvansales.model.CustConBalInfo;
import dsd.dsdvansales.model.CustomerInfo;
import dsd.dsdvansales.model.CustomerNoteInfo;
import dsd.dsdvansales.model.CustomerOrderInfo;
import dsd.dsdvansales.model.DescTypeInfo;
import dsd.dsdvansales.model.EquipmentWithAssInfo;
import dsd.dsdvansales.model.LoginInfo;
import dsd.dsdvansales.model.OrderLineInfo;
import dsd.dsdvansales.model.PresoldOrHInfo;
import dsd.dsdvansales.model.ProdDetlInfo;
import dsd.dsdvansales.model.SurveyInfo;
import dsd.dsdvansales.model.UARPaymentInfo;
import dsd.dsdvansales.model.UARTotalInfo;
import dsd.dsdvansales.model.UOrderDInfo;
import dsd.dsdvansales.model.UOrderHInfo;
import dsd.dsdvansales.model.UServicesInfo;
import dsd.dsdvansales.model.UVisitInfo;
import dsd.dsdvansales.views.AlertSingleChoiceAdapter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InstoreFragment extends BaseFragment implements View.OnClickListener {

	private class CheckMenuInfo {
		public int nIdx = 0;
		public String strName = "";
		public int nSurveyType = 0;
		public List<SurveyInfo> surveyInfoList = new ArrayList<>();
		public boolean bHasSurvey = false;	//When Equipment, this flag is used that Equipment verify flag is Y or no.
		public boolean bCompleted = false;
		public boolean bMandatory = false;
	}

	private boolean bPassedPostVisit = false;

	private int	MAKE_DELIVERY		= 0;
	private int	PICKUP_RETURN		= 1;
	private int	CONSIGNMENT_STOCK_COUNT		= 2;
	private int	UPDATE_CONSIGNMENT_INVENTORY		= 3;
	private int	COLLECT_PAYMENT		= 4;
	private int	EQUIPMENT_IDX = COLLECT_PAYMENT + 1;
	private int VIEW_PREVIOUS_INVOICES_IDX = EQUIPMENT_IDX + 1;

	private TextView mTxtNoData = null;
	private ListView mListView = null;
	private ListAdapter mListAdapter = null;
	private List<CheckMenuInfo> mCheckList = new ArrayList<>();

	private UOrderHInfo mDeliveryInfo = null;
	private UOrderHInfo mPickupInfo = null;
	private boolean mIsPickup = false;

	private Connection printerConnection = null;
	private String mStrMacAddress = "";
	private String mStrFriendlyName = "";
	private String mStrPdfFilePath = "";

	private boolean mIsRoundPrice = false;
	private boolean mbCompleted = false;

	private List<SurveyInfo> mSurveyInfoList = new ArrayList<>();
	//private UInvenHInfo mUInvenHinfo = null;

	private boolean bShowedCustomerDiary = false;

	public boolean isCompleted()
	{
		return mbCompleted;
	}

	String getRoundedFormattedPrice(float value)
	{
		if( mIsRoundPrice )
		{
			float fNeg = 1;
			if( value < 0 )
			{
				fNeg = -1;
			}
			int nTotal = Math.round(Utils.getRoundedValue(value * fNeg) * Global.MULTIPLE_VALUE);
			int nOffSet = nTotal % 5000;
			int nInvoiceTotal = ((nTotal / 5000) + ((Math.abs(nOffSet) >= 2500) ? 1 : 0)) * 5000;
			nOffSet = nTotal % 1000;
			nTotal = ((nTotal / 1000) + ((nOffSet >= 500) ? 1 : 0)) * 1000;  
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

		if(mContext != null && mContext.getMarkVisitAutoCollect())
		{
			Bundle bundle = new Bundle();
			bundle.putBoolean(Global.EXTRA_IS_AUTO_COLLECT, true);
			ChequeDetailFragment fragment = new ChequeDetailFragment();
			fragment.setArguments(bundle);
			mContext.replaceFragment(fragment);
			mContext.setMarkVisitAutoCollect(false);
		}
		else
		{
			mRootView = inflater.inflate(R.layout.fragment_instore, container, false);
			mbCompleted = false;
			commonInit();
		}

		return mRootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		Global.gFragIdx = Global.FRAG_INSTORE_ACTIVITIES;
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
		mRootView.findViewById(R.id.btnDetails).setOnClickListener(this);
		mRootView.findViewById(R.id.btnCompleteVisit).setOnClickListener(this);

		mTxtNoData = (TextView) mRootView.findViewById(R.id.txtNoData);
		mListView = (ListView) mRootView.findViewById(R.id.listView);
		mListAdapter = new ListAdapter();
		mListView.setAdapter(mListAdapter);

		((TextView) mRootView.findViewById(R.id.txtCustomerName)).setText(Global.gCurCustomerInfo.strName);
		refreshLanguage();
	}

	@Override
	public void loadData() {
		mCheckList.clear();

		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		Global.gCurCustomerInfo.presoldOrHInfo = dbAdapter.getPresoldOrHInfo(Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo, Global.gCurCustomerInfo.nPeriodNo);
		Global.gCurCustomerInfo.latestOrderInfo = dbAdapter.getCustomerLatestOrderList(Global.gCurCustomerInfo.nCustNo, Global.gCurCustomerInfo.nChainNo);
		dbAdapter.close();
		List<PresoldOrHInfo> infos = new ArrayList();
		for(PresoldOrHInfo presoldOrHInfo : Global.gCurCustomerInfo.presoldOrHInfo)
		{
			if( presoldOrHInfo.nVisited == 0 )
			{
				infos.add(presoldOrHInfo);
			}
		}
		Global.gCurCustomerInfo.presoldOrHInfo = infos;

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
			mIsRoundPrice = true;
		}
		else
		{
			mIsRoundPrice = false;
		}
		
		CheckMenuInfo info;

		info = new CheckMenuInfo();
		info.nIdx = mCheckList.size();
		MAKE_DELIVERY = info.nIdx;
		info.strName = Str.MakeSales[Global.LANG];
		mCheckList.add(info);

		info = new CheckMenuInfo();
		info.nIdx = mCheckList.size();
		PICKUP_RETURN = info.nIdx;
		info.strName = Str.PickupReturn[Global.LANG];
		mCheckList.add(info);

		if(Global.gCurCustomerInfo.nConSales != 0)
		{
			info = new CheckMenuInfo();
			info.nIdx = mCheckList.size();
			CONSIGNMENT_STOCK_COUNT = info.nIdx;
			info.strName = Str.Consignment_Stock_Count[Global.LANG];
			mCheckList.add(info);

			info = new CheckMenuInfo();
			info.nIdx = mCheckList.size();
			UPDATE_CONSIGNMENT_INVENTORY = info.nIdx;
			info.strName = Str.Update_Consignment_Inventory[Global.LANG];
			mCheckList.add(info);
		}
		else {
			CONSIGNMENT_STOCK_COUNT = -1;
			UPDATE_CONSIGNMENT_INVENTORY = -1;
		}

		info = new CheckMenuInfo();
		info.nIdx = mCheckList.size();
		COLLECT_PAYMENT = info.nIdx;
		info.strName = Str.CollectPayment[Global.LANG];
		mCheckList.add(info);

		//Add the View previous Invoices Menu
		if(!TextUtils.isEmpty(Utils.getStringSetting(mContext, Global.PREF_APICOMPANY)))
		{
			info = new CheckMenuInfo();
			info.nIdx = mCheckList.size();
			VIEW_PREVIOUS_INVOICES_IDX = info.nIdx;
			info.strName = Str.ViewPreviousInvoices[Global.LANG];
			mCheckList.add(info);
		}
		else {
			VIEW_PREVIOUS_INVOICES_IDX = -1;
		}

		dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		mDeliveryInfo = dbAdapter.getUOrderHInfo(Global.gUARTotalInfo.nDeliveryTrxnNo);
		mPickupInfo = dbAdapter.getUOrderHInfo(Global.gUARTotalInfo.nPickupTrxnNo);
		if(Global.gUOrderHInfo != null)
		{
			if(mDeliveryInfo != null )
			{
				mDeliveryInfo.nPrintedFlag = Global.gUOrderHInfo.nPrintedFlag;
				mDeliveryInfo.bPrinted = (Global.gUOrderHInfo.nPrintedFlag == 1) ? true : false;
			}
			else if( mPickupInfo != null )
			{
				mPickupInfo.nPrintedFlag = Global.gUOrderHInfo.nPrintedFlag;
				mPickupInfo.bPrinted = (Global.gUOrderHInfo.nPrintedFlag == 1) ? true : false;
			}
		}
		
		List<DescTypeInfo> surveyTypeList = dbAdapter.getDescTypeList("SURVEYTYPE");
		int nCnt = 0;
		mSurveyInfoList.clear();
		for (DescTypeInfo descTypeInfo : surveyTypeList) {
			if( descTypeInfo.nNumericKey == 99 ) continue;
			nCnt++;

			List<SurveyInfo> candSurveyList = new ArrayList<>();
			String curDate = new SimpleDateFormat("yyyyMMdd").format(new Date());

			boolean bMandatory = false;
			List<SurveyInfo> surveyList = dbAdapter.getSurveyListWithDistAndHead(descTypeInfo.nNumericKey, Global.gCurCustomerInfo.nCustNo, Global.gCurCustomerInfo.nChainNo);
			for(SurveyInfo surveyInfo : surveyList)
			{
				if((curDate.compareTo(surveyInfo.strCreateDate) >= 0 || TextUtils.isEmpty(surveyInfo.strCreateDate))
						&& (curDate.compareTo(surveyInfo.strCompletionDate) <= 0 || TextUtils.isEmpty(surveyInfo.strCompletionDate)))
				{
					if(surveyInfo.strMandatory.equals("Y"))
					{
						bMandatory = true;
					}
					candSurveyList.add(surveyInfo);
				}
			}

			if(candSurveyList.size() == 0) continue;

			mSurveyInfoList.addAll(candSurveyList);

			info = new CheckMenuInfo();

			info.nIdx = mCheckList.size();
			info.strName = descTypeInfo.strDescription;
			info.nSurveyType = descTypeInfo.nNumericKey;
			info.surveyInfoList.addAll(candSurveyList);
			info.bHasSurvey = candSurveyList.size() > 0;
			info.bCompleted = true;
			info.bMandatory = bMandatory;

			for (SurveyInfo surveyInfo : candSurveyList) {
				surveyInfo.bCompleted = dbAdapter.isCompletedSurvey(surveyInfo.strSurveyID, Global.gCurCustomerInfo.nCustNo, Global.gCurCustomerInfo.nChainNo);
				if(!surveyInfo.bCompleted)
				{
					info.bCompleted = false;
				}
			}
			if(info.bCompleted)
			{
				Global.nSurveyCompleted = 1;
			}
//			Global.nSurveyCompleted = info.bCompleted ? 1 : 0;
			mCheckList.add(info);
		}

		EQUIPMENT_IDX = -1;
		boolean bEquipExist = false;
		//Check Equipment Menu has exist or not.
		if(Utils.getIntSetting(mContext, Global.PREF_EQUIPCHECK) == 0)
		{
			bEquipExist = false;
		}
		else if( Utils.getIntSetting(mContext, Global.PREF_EQUIPCHECK) == 1 )
		{
			bEquipExist = dbAdapter.isExistEquip(Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo);
		}
		else if( Utils.getIntSetting(mContext, Global.PREF_EQUIPCHECK) == 2 )
		{
			bEquipExist = true;
		}

		//Add Equipment Menu
		if( bEquipExist )
		{
			info = new CheckMenuInfo();
			info.nIdx = mCheckList.size();
			EQUIPMENT_IDX = info.nIdx;
			info.strName = Str.EquipmentCheck[Global.LANG];
			info.bCompleted = dbAdapter.isCompletedEquipStatus(Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo);
			mCheckList.add(info);

			info.bHasSurvey = true;
			List<EquipmentWithAssInfo> equipmentList = dbAdapter.getEquipmentWithCustomer(Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo);
			for(EquipmentWithAssInfo equipment : equipmentList)
			{
				if( equipment.strVerifyFlag.equals("Y") )
				{
					info.bHasSurvey = false;
					break;
				}
			}

			Global.nAssetChecked = info.bCompleted ? 1 : 0;
		}
		Global.nAssetRequested = bEquipExist ? 1 : 0;

		dbAdapter.close();

		if( Global.gUARTotalInfo.bIsVisitedDelivery == false )
		{
			Global.gCurHeaderInfo = null;
		}

		dbAdapter.open();
		List<ARHeaderInfo> arHeaderInfos = dbAdapter.getARHeaderInfo(Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo);
		Global.gUARTotalInfo.nInvoiceAmount = 0;
		for(ARHeaderInfo arHeaderInfo : arHeaderInfos)
		{
			Global.gUARTotalInfo.nInvoiceAmount += arHeaderInfo.nTrxnAmount * 1.0 / Global.MULTIPLE_VALUE;
		}
		dbAdapter.close();

		refreshPage();

		if(!bShowedCustomerDiary)
		{
			bShowedCustomerDiary = true;
			new Handler(Looper.getMainLooper()).post(new Runnable() {
				@Override
				public void run() {
					DbAdapter dbAdapter = new DbAdapter(mContext);
					dbAdapter.open();
					List<CustomerNoteInfo> noteInfoList = dbAdapter.getCustomerNoteInfoList(Global.gCurCustomerInfo.nCustNo, Global.gCurCustomerInfo.nChainNo);
					dbAdapter.close();
					boolean bExistAlertNote = false;
					for(CustomerNoteInfo info : noteInfoList)
					{
						if(info.strNoteType.equals("91"))
						{
							bExistAlertNote = true;
							break;
						}
					}

					if(bExistAlertNote)
					{
						CustomerDiaryFragment fragment = new CustomerDiaryFragment();
						mContext.replaceFragment(fragment);
					}
				}
			});

		}
	}

	/*private void checkAndAddUInvenDInfo(UOrderDInfo uOrderDInfo) {
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
	}*/
	@Override
	public void refreshPage() {
		if (mCheckList.size() > 0) {
			mListView.setVisibility(View.VISIBLE);
			mTxtNoData.setVisibility(View.GONE);
			mListAdapter.notifyDataSetChanged();
		} else {
			mListView.setVisibility(View.GONE);
			mTxtNoData.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void refreshLanguage() {
		((TextView) mRootView.findViewById(R.id.lblTitle)).setText(Str.IN_STORE_ACTIVITIES[Global.LANG].toUpperCase());
		((TextView) mRootView.findViewById(R.id.txtVisitStarted)).setText(Str.Visit_Started_at[Global.LANG] + " " + Global.gVisitStartedTime);
		((Button) mRootView.findViewById(R.id.btnDetails)).setText(Str.Details[Global.LANG]);
		((Button) mRootView.findViewById(R.id.btnCompleteVisit)).setText(Str.Complete_Visit[Global.LANG]);
		mTxtNoData.setText(Str.No_data_has_been_found[Global.LANG]);
		loadData();
	}

	@Override
	public void receiveBroadcast(String strAction, Intent intent) {
	}

	private void processDeliveryPickup(boolean bIsPickup){
		if( Global.gCurCustomerInfo.presoldOrHInfo != null && Global.gCurCustomerInfo.presoldOrHInfo.size() > 0 )
		{
			if( bIsPickup == false && (Global.gCurCustomerInfo.presoldOrHInfo != null &&
					Global.gCurCustomerInfo.presoldOrHInfo.size() > 0 && (
					Global.gCurCustomerInfo.presoldOrHInfo.get(0).strType.equals("B") ||
							Global.gCurCustomerInfo.presoldOrHInfo.get(0).strType.equals("C"))
			))
			{
				BaseFragment fragment = new OnHandCountFragment();
				Bundle bundle = new Bundle();
				bundle.putBoolean(Global.EXTRA_IS_PICKUP, bIsPickup);
				fragment.setArguments(bundle);
				mContext.replaceFragment(fragment);
				return;
			}
		}

		Global.gDeliveryPickupCnt = 0;
		Global.startFragment = this;
		MakeDeliveryFragment fragment = new MakeDeliveryFragment();
		Bundle bundle = new Bundle();
		bundle.putBoolean(Global.EXTRA_IS_PICKUP, bIsPickup);
		fragment.setArguments(bundle);
		Global.pickedStatusMap.clear();
		mContext.replaceFragment(fragment);
	}

	private void gotoDeliveryPickup(final boolean bIsPickup) {

		Global.gDeliveryPickupCnt = 0;
		Global.gStrDocNo = (Global.gUOrderHInfo != null ) ? Global.gUOrderHInfo.strDocNo : "0";
		Global.gUOrderHInfo = new UOrderHInfo();

		if(Utils.getIntSetting(mContext, Global.PREF_DELFUTDTE) == 0)
		{
			Global.gUOrderHInfo.futureOrderMethod = 0;
			Global.gUOrderHInfo.strDeliveryDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(new Date());
			processDeliveryPickup(bIsPickup);
		}
		else
		{
			try {
				OrderDeliveryOptionDialog dialog = new OrderDeliveryOptionDialog(mContext, new OrderDeliveryOptionDialog.Callback() {
					@Override
					public void onSelect(int nKind) {
						Calendar calendar = Calendar.getInstance();
						switch (nKind)
						{
							case 0:
								Global.gUOrderHInfo.futureOrderMethod = 0;
								Global.gUOrderHInfo.strDeliveryDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(new Date());
								processDeliveryPickup(bIsPickup);
								break;
//							case 1:
//								Global.gUOrderHInfo.futureOrderMethod = 1;
//								calendar.setTime(new Date());
//								calendar.add(Calendar.DATE, 1);
//								Global.gUOrderHInfo.strDeliveryDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(calendar.getTime());
//								processDeliveryPickup(bIsPickup);
//								break;
							case 1:
								Global.gUOrderHInfo.futureOrderMethod = 2;
								calendar.setTime(new Date());
								calendar.add(Calendar.DATE, 2);
								DatePickerDialog dpd = DatePickerDialog.newInstance(
										new DatePickerDialog.OnDateSetListener() {
											@Override
											public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
												Global.gUOrderHInfo.strDeliveryDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(new Date(year - 1900, monthOfYear, dayOfMonth));
												processDeliveryPickup(bIsPickup);
											}
										},
										calendar.get(Calendar.YEAR), // Initial year selection
										calendar.get(Calendar.MONTH), // Initial month selection
										calendar.get(Calendar.DAY_OF_MONTH) // Inital day selection
								);
								dpd.show(mContext.getFragmentManager(), "Datepickerdialog");
								break;
						}
					}
				}, Utils.getIntSetting(mContext, Global.PREF_DELFUTDTE));
				dialog.show();
			}catch (Exception e)
			{
				e.printStackTrace();
			}

		}
	}

	private void gotoConsignmentStockCount(){
		Global.gUOrderHInfo = null;
		ConsignmentStockCountFragment fragment = new ConsignmentStockCountFragment();
		mContext.replaceFragment(fragment);
	}

	private void gotoUpdateConsignmentInventory(){
		Global.gCustConBalList.clear();
		Global.gCurInventoryList.clear();
		Global.gConsignmentAdjust.clear();

		UpdateConsignmentInventoryFragment fragment = new UpdateConsignmentInventoryFragment();
		mContext.replaceFragment(fragment);
	}

	private void gotoCheck(CheckMenuInfo info) {
		if (info.nIdx == MAKE_DELIVERY) {
			if(Global.gCurCustomerInfo.nCreditHold > 0) return;

			if (Global.gCurCustomerInfo.strPayType.equals("3")) {
				int nMaxOpenDaysUnauth = Utils.getIntFromString(Utils.getStringSetting(mContext, Global.PREF_MAX_OPEN_DAYS_UNAUTH));
				if (nMaxOpenDaysUnauth > 0) {
					DbAdapter dbAdapter = new DbAdapter(mContext);
					dbAdapter.open();

					List<ARHeaderInfo> arHeaderInfos = dbAdapter.getARHeaderInfo(Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo);
					boolean bFound = false;
					for (ARHeaderInfo arHeaderInfo: arHeaderInfos) {
						int user1 = Utils.getIntFromString(arHeaderInfo.strUser1);
						if (user1 > nMaxOpenDaysUnauth) {
							bFound = true;
							break;
						}
					}
					dbAdapter.close();

					if (bFound) {
						try {
							if(mContext.isFinishing() || mContext.isDestroyed()) return;
							MessageDialog msgDialog = new MessageDialog(mContext);
							msgDialog.setCancelable(false);
							msgDialog.mStrMessage = String.format(Str.TempCreditUnsatisfied[Global.LANG], nMaxOpenDaysUnauth);
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
			}

			boolean bAllowRepeatSales = (Utils.getStringSetting(mContext, Global.PREF_ALLOW_REPEAT_SALES).equals("1"));
			if ((!bAllowRepeatSales && Global.gUARTotalInfo.bIsVisitedDelivery == false)
			|| (bAllowRepeatSales)) {
				PresoldOrHInfo selPresoldHInfo = null;
				boolean bCanAdd = true;
				for(PresoldOrHInfo presoldHInfo : Global.gCurCustomerInfo.presoldOrHInfo)
				{
					if (presoldHInfo.strType.equals("P") == true || presoldHInfo.strType.equals("B") == true ||
							presoldHInfo.strType.equals("C") == true ||
							presoldHInfo.strType.equals("4") )
					{
						if( presoldHInfo.strOrderNo.equals("0") /*&& TextUtils.isEmpty(Utils.getStringSetting(mContext, Global.PREF_INVOICENUM))*/)
						{
							bCanAdd = false;
							selPresoldHInfo = presoldHInfo;
						}
					}
				}
				if( bCanAdd )
				{
					if( Global.gCurCustomerInfo.presoldOrHInfo != null && Global.gCurCustomerInfo.presoldOrHInfo.size() > 0 )
					{
						String strFirstItemType = Global.gCurCustomerInfo.presoldOrHInfo.get(0).strType;
						if( strFirstItemType.equals("B") || strFirstItemType.equals("C"))
						{
							try {
								final MessageDialog msgDialog = new MessageDialog(mContext);
								msgDialog.setCancelable(false);
								msgDialog.mStrMessage = Str.Do_you_wish_to_use_the_presold[Global.LANG];
								msgDialog.mStrLeft = Str.No[Global.LANG];
								msgDialog.mStrRight = Str.Yes[Global.LANG];
								msgDialog.setOnDismissListener(new OnDismissListener() {
									@Override
									public void onDismiss(DialogInterface dialog) {
										if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
											PresoldOrHInfo presoldOrHInfo = Global.gCurCustomerInfo.presoldOrHInfo.get(0);
											gotoDelivery(presoldOrHInfo);
										}
										else
										{
											Global.gCurCustomerInfo.presoldOrHInfo.clear();
											if(Global.gCurCustomerInfo.latestOrderInfo != null)
											{
												Global.gCurCustomerInfo.latestOrderInfo.clear();
											}
											gotoDeliveryPickup(false);
										}
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
							Global.gCurCustomerInfo.presoldOrHInfo.clear();
							if(Global.gCurCustomerInfo.latestOrderInfo != null)
							{
								Global.gCurCustomerInfo.latestOrderInfo.clear();
							}
							gotoDeliveryPickup(false);
						}
					}
					else
					{
						Global.gCurCustomerInfo.presoldOrHInfo.clear();
						if(Global.gCurCustomerInfo.latestOrderInfo != null)
						{
							Global.gCurCustomerInfo.latestOrderInfo.clear();
						}
						gotoDeliveryPickup(false);
					}
				}
				else
				{
					gotoDelivery(selPresoldHInfo);
				}
			}
		}
		else if (info.nIdx == PICKUP_RETURN) {
			if(Global.gCurCustomerInfo.strDumpsAllowed.equalsIgnoreCase("Y"))
			{
				if (Global.gUARTotalInfo.bIsVisitedPickup == false) {
					gotoDeliveryPickup(true);
				}
			}
		}
		else if(info.nIdx == CONSIGNMENT_STOCK_COUNT) {
			gotoConsignmentStockCount();
		}
		else if(info.nIdx == UPDATE_CONSIGNMENT_INVENTORY){
			gotoUpdateConsignmentInventory();
		}
		else if (info.nIdx == COLLECT_PAYMENT) {
			String strDisallowAccountPayment = Utils.getStringSetting(mContext, Global.PREF_DISALLOW_ACCOUNT_PAY);
			String strPayType = Global.gCurCustomerInfo.strPayType;

			if (strPayType.equals("6") || strPayType.equals("B") || strPayType.equals("C") || strPayType.isEmpty()) {

			}
			else {
				if (!strPayType.equals("3") && strDisallowAccountPayment.equals("1") && !strPayType.equals("1") && !strPayType.equals("2") && !strPayType.equals("C")) {

				}
				else {
					if(Global.gCurCustomerInfo.nShowPrice != 2)
					{
						DbAdapter dbAdapter = new DbAdapter(mContext);
						dbAdapter.open();
						List<ARHeaderInfo> arHeaderInfos = dbAdapter.getARHeaderInfo(Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo);
						dbAdapter.close();
						float dTotalRemaining = 0;
						if( arHeaderInfos.size() > 0)
						{
							for(ARHeaderInfo arHeaderInfo : arHeaderInfos)
							{
								if(arHeaderInfo.nProcessStatus <= 2 ) {
									if (!arHeaderInfo.strARTrxnType.equals("ORD")) {
										dTotalRemaining += arHeaderInfo.nTrxnAmount * 1.0 / Global.MULTIPLE_VALUE;
									}
								}
							}
						}

						String strShowValue = getRoundedFormattedPrice(dTotalRemaining);
						float fValue = Utils.getFloatFromString(strShowValue.substring(Global.gCurrencySymbol.length()));
						if( fValue == 0.0f )
						{

						}
						else
						{
							Bundle bundle = new Bundle();
							bundle.putBoolean(Global.EXTRA_CARD, false);
							bundle.putBoolean(Global.EXTRA_IS_FROM_DELIVERY_SUMMARY, false);

							Global.gUARTotalInfo = new UARTotalInfo();
							Global.gUARPaymentChequeInfo = new UARPaymentInfo();
							Global.gUARPaymentCardInfo = new UARPaymentInfo();
							Global.gUARPaymentEFTInfo = new UARPaymentInfo();

							PaymentCollectionFragment fragment = new PaymentCollectionFragment();
							fragment.setArguments(bundle);
							mContext.replaceFragment(fragment);
						}
					}
				}
			}
		}
		else if(info.nIdx == EQUIPMENT_IDX){
			if( Utils.getIntSetting(mContext, Global.PREF_EQUIPCHECK) == 2) {
				EquipmentListFragment fragment = new EquipmentListFragment();
				mContext.replaceFragment(fragment);
			}
			else {
				if(info.bCompleted == false)
				{
					EquipmentListFragment fragment = new EquipmentListFragment();
					mContext.replaceFragment(fragment);
				}
			}
		}
		else if(info.nIdx == VIEW_PREVIOUS_INVOICES_IDX){
			ViewPreviousInvoicesFragment fragment = new ViewPreviousInvoicesFragment();
			mContext.replaceFragment(fragment);
		}
		else {
			if (info.bHasSurvey == true && info.bCompleted == false) {
				if(info.surveyInfoList.size() == 1)
				{
					Bundle bundle = new Bundle();
					bundle.putInt(Global.EXTRA_SURVEY_IDX, info.nIdx);
					bundle.putString(Global.EXTRA_SURVEY_ID, info.surveyInfoList.get(0).strSurveyID);
					bundle.putString(Global.EXTRA_SURVEY_TITLE, info.surveyInfoList.get(0).strSurveyTitle);
					bundle.putString(Global.EXTRA_SURVEY_RESPONSE, info.surveyInfoList.get(0).strSurveyResponse);
					bundle.putBoolean(Global.EXTRA_FROM_SURVEY_ID, info.surveyInfoList.get(0).bFromSurveyID);
					Fragment fragment = new PosCheckDetailFragment();
					fragment.setArguments(bundle);
					mContext.replaceFragment(fragment);
				}
				else {
					Bundle bundle = new Bundle();
					bundle.putInt(Global.EXTRA_SURVEY_TYPE, info.nSurveyType);
					bundle.putString(Global.EXTRA_SURVEY_TITLE, info.strName);
					PosCheckFragment fragment = new PosCheckFragment();
					fragment.setArguments(bundle);
					mContext.replaceFragment(fragment);
				}

			}
		}
	}
	
	public boolean isVisit()
	{
		return (Global.gUARTotalInfo.bIsVisitedDelivery == true || Global.gUARTotalInfo.bIsVisitedPickup == true);
	}

	private boolean isContainedPresoldOrderWithNonOrder()
	{
		if( Global.gCurCustomerInfo.presoldOrHInfo == null ||
				Global.gCurCustomerInfo.presoldOrHInfo.size() == 0 )
		{
			return false;
		}

		for(PresoldOrHInfo presoldOrHInfo : Global.gCurCustomerInfo.presoldOrHInfo)
		{
			int orderNo = Utils.parseIntFromString(presoldOrHInfo.strOrderNo);
			if( orderNo != 0 && presoldOrHInfo.nVisited == 0 && presoldOrHInfo.strType.equals("P"))
			{
				return true;
			}
		}
		return false;
	}

	public void doComplete() {


		//Check Equipment Verify Status

		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();


		boolean bCheckOK;
		if(Utils.getIntSetting(mContext, Global.PREF_EQUIPCHECK) == 0)
		{
			bCheckOK = true;
		}
		else if(Utils.getIntSetting(mContext, Global.PREF_EQUIPCHECK) == 1 && !dbAdapter.isExistEquip(Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo))
		{
			bCheckOK = true;
		}
		else if( dbAdapter.isCompletedEquipStatus(Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo) )
		{
			bCheckOK = true;
		}
		else
		{
			bCheckOK = true;
			List<EquipmentWithAssInfo> equipmentList = dbAdapter.getEquipmentWithCustomer(Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo);
			for(EquipmentWithAssInfo equipment : equipmentList)
			{
				if( equipment.strVerifyFlag.equals("Y") )
				{
					bCheckOK = false;
					break;
				}
			}
		}
		if( !bCheckOK )
		{
			m_bClicked = false;
			dbAdapter.close();
			try {
				final MessageDialog msgDialog = new MessageDialog(mContext);
				msgDialog.setCancelable(false);
				msgDialog.mStrMessage = Str.You_must_verify_the_in_store_equipment[Global.LANG];
				msgDialog.mStrRight = Str.Ok[Global.LANG];
				msgDialog.show();
			}catch (Exception e)
			{
				e.printStackTrace();
			}

			return;
		}
		if(!isContainedPresoldOrderWithNonOrder())
		{
			long nTrxnNo = setGeneralUploadData("SERV", Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo, -1, "Complete Visit");

			/*TransactionInfo transactionInfo = new TransactionInfo();
			transactionInfo.strType = "SERV";
			transactionInfo.strTime = new SimpleDateFormat("kk:mm").format(new Date(nTrxnNo));
			transactionInfo.strCustomerName = Global.gCurCustomerInfo.strName;
			dbAdapter.insertTransactionHistoryInfo(transactionInfo);*/

			UServicesInfo uServicesInfo = new UServicesInfo();
			uServicesInfo.nTrxnNo = nTrxnNo;
			uServicesInfo.nChainNo = Global.gCurCustomerInfo.nChainNo;
			uServicesInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
			uServicesInfo.nPeriod = 0;
			uServicesInfo.nSeqNo = Global.gCurCustomerInfo.nSeqNo;
			uServicesInfo.nDone = 0;
			uServicesInfo.strReason = "0";
			uServicesInfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(mContext, mCurDate));
			uServicesInfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(mCurDate);
			dbAdapter.insertUServicesInfo(uServicesInfo);
//			Global.makeZipFileInfoList.add(makeZipFileInfo);
		}

		//Upload the Visit Information
		if(!bPassedPostVisit)
		{
			long nTrxnNo = setGeneralUploadData("VIS", Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo, -1, "Complete Visit");
			UVisitInfo visitInfo = new UVisitInfo();
			visitInfo.nTrxnNo = nTrxnNo;
			visitInfo.strDocType = "VIS";
			visitInfo.nChainNo = Global.gCurCustomerInfo.nChainNo;
			visitInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
			visitInfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(mContext, mCurDate));
			visitInfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(mCurDate);
			visitInfo.nPrintedFlag = 0;
			visitInfo.strSaleDate = visitInfo.strTrxnDate;
			visitInfo.strVisitStart = Global.strVisitStart;
			visitInfo.strVisitEnd = new SimpleDateFormat("kkmmss").format(new Date());
			visitInfo.strNextVisit = "";
			visitInfo.strVisitMessage = "";
			visitInfo.nOrderCreated = Global.bOrderCreated ? 1 : 0;
			visitInfo.nSurveyCompleted = Global.nSurveyCompleted;
			visitInfo.nAssetChecked = Global.nAssetChecked;
			visitInfo.nAssetRequested = Global.nAssetRequested;
			visitInfo.nVisitFrequency = 0;
			dbAdapter.insertUVisitInfo(visitInfo);
		}


		dbAdapter.close();

		/*if(Global.makeZipFileInfoList.size() > 0)
		{
			MakeZipFileInfo totalZipFileInfo = new MakeZipFileInfo();
			for(MakeZipFileInfo zipFileInfo : Global.makeZipFileInfoList)
			{
				totalZipFileInfo.nChainNo = Global.gCurCustomerInfo.nChainNo;
				totalZipFileInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
				totalZipFileInfo.trxnList.addAll(zipFileInfo.trxnList);
			}
			//Global.gUploadCustNoList.add(totalZipFileInfo);
            //Utils.registerUploadFile(mContext, makeZipFileInfo);

		}*/
		if(makeZipFileInfo != null)
		{
			Utils.registerUploadFileAsTemp(mContext, makeZipFileInfo);
		}
		Utils.doProcessInTemp(mContext);

		if( isContainedPresoldOrderWithNonOrder() )
		{
			m_bClicked = false;
			try {
				final MessageDialog msgDialog = new MessageDialog(mContext);
				msgDialog.setCancelable(false);
				msgDialog.mStrTitle = Str.NonDeliveryWarning[Global.LANG].toUpperCase();
				msgDialog.mStrMessage = Str.NonDeliveryWarningMsg[Global.LANG];
				msgDialog.mStrLeft = Str.Return[Global.LANG];
				msgDialog.mStrRight = Str.Continue[Global.LANG];
				msgDialog.setOnDismissListener(new OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
							DbAdapter dbAdapter = new DbAdapter(mContext);
							dbAdapter.open();
							if(Global.gCurCustomerInfo != null)
							{
								Global.gCurCustomerInfo.bCompleted = false;
								dbAdapter.updateCustomerCompleted(Global.gCurCustomerInfo);
								dbAdapter.deleteProcessedARHeader(Global.gCurCustomerInfo.nCustNo, Global.gCurCustomerInfo.nChainNo);
								List<ARHeaderInfo> arHeaderInfos = dbAdapter.getARHeaderInfo(Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo);
								for(ARHeaderInfo arHeaderInfo : arHeaderInfos)
								{
									arHeaderInfo.nProcessStatus = 0;
									dbAdapter.updateARHeaderInfo(arHeaderInfo);
								}
							}
							dbAdapter.close();

							mbCompleted = true;
							doGetLatestSalesOrder();
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
		else
		{
			dbAdapter = new DbAdapter(mContext);
			dbAdapter.open();
			Global.gCurCustomerInfo.bCompleted = true;

			dbAdapter.updateCustomerInfo(Global.gCurCustomerInfo);
			dbAdapter.deleteProcessedARHeader(Global.gCurCustomerInfo.nCustNo, Global.gCurCustomerInfo.nChainNo);
			List<ARHeaderInfo> arHeaderInfos = dbAdapter.getARHeaderInfo(Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo);
			for(ARHeaderInfo arHeaderInfo : arHeaderInfos)
			{
				arHeaderInfo.nProcessStatus = 0;
				dbAdapter.updateARHeaderInfo(arHeaderInfo);
			}

			dbAdapter.close();
			mbCompleted = true;
			doGetLatestSalesOrder();
		}

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

	private void doGetLatestSalesOrder()
	{
		if(Utils.getStringSetting(mContext, Global.PREF_GETORDERS).equals("1"))
		{
			if(!Utils.possibleCheckNewOrders(mContext))
			{
				Toast.makeText(mContext, Str.No_New_Order_Found[Global.LANG], Toast.LENGTH_LONG).show();
				goBack();
				return;
			}

			final String strBaseUrl = Utils.getBaseURL(Utils.getStringSetting(mContext, Global.PREF_APICOMPANY));
			if(!TextUtils.isEmpty(strBaseUrl) && Utils.isConnectingToInternet(mContext))
			{
				showProgressDialog(Str.CheckingForNewOrders[Global.LANG]);

				Map<String, String> params = new HashMap<>();
				params.put("username", Utils.getStringSetting(mContext, Global.PREF_APIUSER));
				params.put("password", Utils.getStringSetting(mContext, Global.PREF_APIPASSWORD));
				ConnectApiClient.getMainApiClient(strBaseUrl, null).login(params).enqueue(new Callback<LoginInfo>() {
					@Override
					public void onResponse(Call<LoginInfo> call, Response<LoginInfo> response) {

						if(response.isSuccessful() && response.body() != null)
						{
							final LoginInfo body = response.body();
							Map<String, String> headers = new HashMap<>();
							headers.put("X-Auth-Token", body.token);
							Map<String, String> params = new HashMap<>();
							params.put("deliveryDate", new SimpleDateFormat("dd-MM-yyyy").format(new Date()));
							params.put("tripNumber", Global.gTripNumber);
							Callback<List<CustomerOrderInfo>> callback = new Callback<List<CustomerOrderInfo>>() {
								@Override
								public void onResponse(Call<List<CustomerOrderInfo>> call, final Response<List<CustomerOrderInfo>> response) {

									if(response.isSuccessful() && response.body() != null && response.body().size() > 0)
									{
										new Thread()
										{
											@Override
											public void run() {
												List<CustomerOrderInfo> orderInfoList = response.body();
												DbAdapter dbAdapter = new DbAdapter(mContext);
												dbAdapter.open();
												Map<String, ProdDetlInfo> mProdDetlItemMap = new HashMap<>();
												Map<String, ProdDetlInfo> mProdDetlUPCMap = new HashMap<>();
												dbAdapter.getProductList(mProdDetlItemMap, mProdDetlUPCMap);
												if(Global.gCustomerList == null || Global.gCustomerList.size() == 0)
												{
													Global.gCustomerList = filterSameCustomer(dbAdapter.getWhiteCustomerInfo(false, Utils.getIntSetting(mContext,  Global.DAY_NO)));

													Global.gCustomerList.addAll(dbAdapter.getCustomerList(true, false, Utils.getIntSetting(mContext, Global.DAY_NO)));
												}
												final List<CustomerOrderInfo> customerOrderInfoList = new ArrayList<>();
												final Map<String, CustomerInfo> customerMap = new HashMap<>();
												if(orderInfoList != null && orderInfoList.size() > 0)
												{
													for(CustomerOrderInfo orderInfo : orderInfoList)
													{
														if(dbAdapter.getLatestOrder(orderInfo.orderNumber) != null) continue;
														if(dbAdapter.getPresoldOrHInfoWithOrderNo(orderInfo.orderNumber) != null) continue;
														if(orderInfo.orderLines == null || orderInfo.orderLines.size() == 0) continue;
														//Check This Customer is available for today, and if not, we will add this customer.
														CustomerInfo customerInfo = dbAdapter.getCustomerInfo(orderInfo.customerNumber, orderInfo.chainNumber);
														if(customerInfo != null && customerInfo.nCustNo == orderInfo.customerNumber && customerInfo.nChainNo == orderInfo.chainNumber)
														{
															customerMap.put(customerInfo.nCustNo + "&&" + customerInfo.nChainNo, customerInfo);
															customerOrderInfoList.add(orderInfo);
														}
													}
												}
												dbAdapter.close();

												new Handler(Looper.getMainLooper()).post(() -> {
													hideProgressDialog();
													if(customerOrderInfoList.size() > 0)
													{
														//Show the Popup dialog to select users.
														NewOrdersFoundDialog dialog = new NewOrdersFoundDialog(mContext, customerOrderInfoList, customerMap, (selOrderInfoList, unSelOrderInfoList) -> {
															showProgressDialog(Str.AddingNewOrders[Global.LANG]);
															new Thread()
															{
																@Override
																public void run() {
																	final List<CustomerInfo> newCustomerList = new ArrayList<>();
																	Map<String, CustomerInfo> customerInfoMap = new HashMap<>();
																	dbAdapter.open();
																	for(CustomerOrderInfo orderInfo : selOrderInfoList)
																	{
																		CustomerInfo customerInfo = customerMap.get(orderInfo.customerNumber + "&&" + orderInfo.chainNumber);
																		if(customerInfoMap.get(orderInfo.customerNumber + "/" + orderInfo.chainNumber) == null)
																		{
																			newCustomerList.add(customerInfo);
																			customerInfoMap.put(orderInfo.customerNumber + "/" + orderInfo.chainNumber, customerInfo);
																		}
																		for(OrderLineInfo orderLineInfo : orderInfo.orderLines)
																		{
																			ProdDetlInfo productInfo = mProdDetlItemMap.get(orderLineInfo.itemNumber);
																			if(productInfo != null)
																			{
																				orderLineInfo.strShortDesc = productInfo.strShortDesc;
																				orderLineInfo.strLongDesc = productInfo.strDescription;
																			}
																		}
																		dbAdapter.insertLastestOrder(orderInfo);
																		if(!customerInfo.bIsRouteSch)
																		{
																			customerInfo.bIsRouteSch = true;
																			customerInfo.bFromAddCustomer = true;
																			customerInfo.nSeqNo = 0;
																			customerInfo.strStartTime1 = "0000";
																			customerInfo.strArrivalTime = customerInfo.strStartTime1;
																			customerInfo.nDayNo = Utils.getIntSetting(mContext, Global.DAY_NO);
																			if(Global.gCustomerList != null )
																			{
																				customerInfo.nOrderNo = 0;
																				for(CustomerInfo otherInfo: Global.gCustomerList)
																				{
																					otherInfo.nOrderNo++;
																				}
																			}
																			else
																			{
																				customerInfo.nOrderNo = 0;
																			}
																			Global.gCustomerList.add(0, customerInfo);

																			if(customerInfo.strDeliveryWindowFrom.length() > 0 && !customerInfo.strDeliveryWindowFrom.equals("0000"))
																			{
																				customerInfo.strStartTime1 = customerInfo.strDeliveryWindowFrom;
																				customerInfo.strArrivalTime = customerInfo.strStartTime1;
																			}
																			if(customerInfo.strDeliveryWindowTo.length() > 0 && !customerInfo.strDeliveryWindowTo.equals("0000"))
																			{
																				customerInfo.strEndTime1 = customerInfo.strDeliveryWindowTo;
																			}

																			int nCustomerCount = dbAdapter.getCustomerListCount(true);
																			customerInfo.nOrgSeqNo = nCustomerCount;
																			dbAdapter.updateCustomerInfo(customerInfo);
																		}
																		else
																		{
																			boolean bExist = false;
																			for(CustomerInfo info : Global.gCustomerList)
																			{
																				if(info.nCustNo == customerInfo.nCustNo && info.nChainNo == customerInfo.nChainNo)
																				{
																					bExist = true;
																				}
																			}
																			if(!bExist)
																			{
																				Global.gCustomerList.add(0, customerInfo);
																				customerInfo.nSeqNo = 0;
																				if(Global.gCustomerList != null )
																				{
																					customerInfo.nOrderNo = 0;
																					for(CustomerInfo otherInfo: Global.gCustomerList)
																					{
																						otherInfo.nOrderNo++;
																					}
																				}
																				else
																				{
																					customerInfo.nOrderNo = 0;
																				}
																				customerInfo.nPeriodNo = 0;
																				customerInfo.strDeliveryDate = "";
																				dbAdapter.resetSequNoForCustomer(customerInfo);
																				dbAdapter.registerWhiteCustomer(customerInfo.nChainNo, customerInfo.nCustNo, Utils.getIntSetting(mContext, Global.DAY_NO));
																				dbAdapter.updateCustomerInfo(customerInfo);
																			}
																		}

																		customerInfo.bCompleted = false;
																		dbAdapter.updateCustomerInfo(customerInfo);
																	}

																	//Register Ignored Order
																	for(CustomerOrderInfo orderInfo : unSelOrderInfoList)
																	{
																		dbAdapter.insertIgnoredLastestOrder(orderInfo);
																	}
																	dbAdapter.close();

																	new Handler(Looper.getMainLooper()).post(new Runnable() {
																		@Override
																		public void run() {
																			hideProgressDialog();
																			goBack();
																		}
																	});
																}
															}.start();

														});
														dialog.show();
													}
													else
													{
														goBack();
													}
												});
											}
										}.start();

									}
									else
									{
										hideProgressDialog();
										if(!mContext.isFinishing() && !mContext.isDestroyed())
										{
											try {
												Toast.makeText(mContext, Str.CouldNotConnect[Global.LANG], Toast.LENGTH_LONG).show();
											}catch (Exception e1)
											{
												e1.printStackTrace();
											}
										}
										goBack();
									}
								}

								@Override
								public void onFailure(Call<List<CustomerOrderInfo>> call, Throwable t) {
									hideProgressDialog();
									if(!mContext.isFinishing() && !mContext.isDestroyed())
									{
										try {
											Toast.makeText(mContext, Str.CouldNotConnect[Global.LANG], Toast.LENGTH_LONG).show();
										}catch (Exception e1)
										{
											e1.printStackTrace();
										}
									}
									goBack();
								}
							};
							if(Utils.getBooleanSetting(mContext, Global.PREF_GET_SALES_DELAY))
							{
								ConnectApiClient.getMainApiClient(strBaseUrl, headers).delay(params).enqueue(callback);
							}
							else
							{
								ConnectApiClient.getMainApiClient(strBaseUrl, headers).getSalesOrder(params).enqueue(callback);
							}

						}
						else {
							hideProgressDialog();
							if(!mContext.isFinishing() && !mContext.isDestroyed())
							{
								try {
									Toast.makeText(mContext, Str.CouldNotConnect[Global.LANG], Toast.LENGTH_LONG).show();
								}catch (Exception e1)
								{
									e1.printStackTrace();
								}
							}
							goBack();
						}
					}

					@Override
					public void onFailure(Call<LoginInfo> call, Throwable t) {
						hideProgressDialog();
						if(!mContext.isFinishing() && !mContext.isDestroyed())
						{
							try {
								Toast.makeText(mContext, Str.CouldNotConnect[Global.LANG], Toast.LENGTH_LONG).show();
							}catch (Exception e1)
							{
								e1.printStackTrace();
							}
						}
						goBack();
					}
				});
			}
			else
			{
				goBack();
			}
		}
		else
		{
			goBack();
		}

	}

	boolean bPaused = true;
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

	private int getSalesQuantity(String strItemNo) {
		int nSales = 0;
		for (UOrderDInfo info : Global.gUOrderHInfo.deliveryList) {
			if (info.strItemNo.equals(strItemNo) == true) {
				nSales += info.nSales;
			}
		}
		return nSales;
	}

	private int getDumpsQuantity(String strItemNo) {
		int nDumps = 0;
		for (UOrderDInfo info : Global.gUOrderHInfo.pickupList) {
			if (info.strItemNo.equals(strItemNo) == true) {
				nDumps += info.nDumps;
			}
		}
		return nDumps;
	}

	private void showPresoldDialog(boolean bIsPickup, PresoldOrHInfo info) {
		try {
			PresoldDialog dlg = new PresoldDialog(mContext, this);
			dlg.mInfo = info;
			dlg.mIsPickup = bIsPickup;
			dlg.show();
		}catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	private void showPresoldDialog(boolean bIsPickup, CustomerOrderInfo info) {
		try {
			PresoldDialog dlg = new PresoldDialog(mContext, this);
			//dlg.mInfo = info;
			dlg.mCustomerOrderInfo = info;
			dlg.mIsPickup = bIsPickup;
			dlg.show();
		}catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void showPrintActionDialog(long nTrxnID)
	{
		final PrintActionDialog dlg = new PrintActionDialog(mContext);
		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		final UOrderHInfo deliveryInfo = dbAdapter.getUOrderHInfo(nTrxnID);
		dbAdapter.close();

		dlg.mInfo = deliveryInfo;
		dlg.setCancelable(false);
		dlg.setOnDismissListener(dialog -> {
			if (dlg.mReturnCode == Global.ACTION_PRINT) {
				mStrPdfFilePath = Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + dlg.mInfo.strDocNo + "_.pdf";
				if(!Utils.isFileExist(mStrPdfFilePath))
				{
					mStrPdfFilePath = Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + dlg.mInfo.strDocNo + ".pdf";
				}
				showPrintConfirmDialog();
			} else if (dlg.mReturnCode == Global.ACTION_REPRINT) {
				if(dlg.mInfo == null)
				{
					dlg.mInfo = Global.gUOrderHInfo;
				}
				mStrPdfFilePath = Global.PDF_LOCAL_DIR + dlg.mInfo.strDocNo + "_.pdf";
				if(!Utils.isFileExist(mStrPdfFilePath))
				{
					mStrPdfFilePath = Global.PDF_LOCAL_DIR + dlg.mInfo.strDocNo + ".pdf";
				}
				showPrintConfirmDialog();
			} else if (dlg.mReturnCode == Global.ACTION_VOID) {
				if(Utils.getStringSetting(mContext, Global.PREF_ALLOW_VOID).equals("1"))
				{
					try {
						final MessageDialog msgDialog = new MessageDialog(mContext);
						msgDialog.setCancelable(false);
						msgDialog.mStrTitle = Str.VoidWarning[Global.LANG].toUpperCase();
						msgDialog.mStrMessage = Str.Void_msg[Global.LANG];
						msgDialog.mStrLeft = Str.No[Global.LANG];
						msgDialog.mStrRight = Str.Yes[Global.LANG];
						msgDialog.setOnDismissListener(new OnDismissListener() {
							@Override
							public void onDismiss(DialogInterface dialog) {
								if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
									DbAdapter dbAdapter1 = new DbAdapter(mContext);
									dbAdapter1.open();
									deliveryInfo.nVoidFlag = 1;
									dbAdapter1.updateUOrderHInfo(deliveryInfo);
									dbAdapter1.close();
									mListAdapter.notifyDataSetChanged();
								}
							}
						});
						msgDialog.show();
					}catch (Exception e)
					{
						e.printStackTrace();
					}

				}
				else if(Utils.getStringSetting(mContext, Global.PREF_ALLOW_VOID).equals("2"))
				{
					Utils.doVoidInvoice(mContext, deliveryInfo);
				}

			}
		});
		try {
			dlg.show();
		}catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	private void showPrintActionDialog(boolean bIsPickup) {
		mIsPickup = bIsPickup;
		final PrintActionDialog dlg = new PrintActionDialog(mContext);
		dlg.mInfo = bIsPickup == false ? mDeliveryInfo : mPickupInfo;
		dlg.setCancelable(false);
		dlg.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				if (dlg.mReturnCode == Global.ACTION_PRINT) {
					mStrPdfFilePath = Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + dlg.mInfo.strDocNo + "_.pdf";
					if(!Utils.isFileExist(mStrPdfFilePath))
					{
						mStrPdfFilePath = Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + dlg.mInfo.strDocNo + ".pdf";
					}
					showPrintConfirmDialog();
				} else if (dlg.mReturnCode == Global.ACTION_REPRINT) {
					mStrPdfFilePath = Global.PDF_LOCAL_DIR + dlg.mInfo.strDocNo + "_.pdf";
					if(!Utils.isFileExist(mStrPdfFilePath))
					{
						mStrPdfFilePath = Global.PDF_LOCAL_DIR + dlg.mInfo.strDocNo + ".pdf";
					}
					showPrintConfirmDialog();
				} else if (dlg.mReturnCode == Global.ACTION_VOID) {
					if(Utils.getStringSetting(mContext, Global.PREF_ALLOW_VOID).equals("1"))
					{
						try {
							final MessageDialog msgDialog = new MessageDialog(mContext);
						msgDialog.setCancelable(false);
						msgDialog.mStrTitle = Str.VoidWarning[Global.LANG].toUpperCase();
						msgDialog.mStrMessage = Str.Void_msg[Global.LANG];
						msgDialog.mStrLeft = Str.No[Global.LANG];
						msgDialog.mStrRight = Str.Yes[Global.LANG];
						msgDialog.setOnDismissListener(new OnDismissListener() {
							@Override
							public void onDismiss(DialogInterface dialog) {
								if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
									DbAdapter dbAdapter = new DbAdapter(mContext);
									dbAdapter.open();
									if (mIsPickup == false) {
										mDeliveryInfo.nVoidFlag = 1;
										dbAdapter.updateUOrderHInfo(mDeliveryInfo);
									} else {
										mPickupInfo.nVoidFlag = 1;
										dbAdapter.updateUOrderHInfo(mPickupInfo);
									}
									dbAdapter.close();
									mListAdapter.notifyDataSetChanged();
								}
							}
						});
						msgDialog.show();
						}catch (Exception e)
						{
							e.printStackTrace();
						}

					}
					else if(Utils.getStringSetting(mContext, Global.PREF_ALLOW_VOID).equals("2"))
					{
						Utils.doVoidInvoice(mContext, dlg.mInfo);
					}

				}
			}
		});
		try {
			dlg.show();
		}catch (Exception e)
		{
			e.printStackTrace();
		}

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
					if (mIsPickup == false) {
						mDeliveryInfo.nPrintedFlag = 1;
						Global.gUOrderHInfo.nPrintedFlag = 1;
						dbAdapter.updateUOrderHInfo(mDeliveryInfo);
					} else {
						mPickupInfo.nPrintedFlag = 1;
						Global.gUOrderHInfo.nPrintedFlag = 1;
						dbAdapter.updateUOrderHInfo(mPickupInfo);
					}
					dbAdapter.close();
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
						}
					});
					msgDialog.show();
				}catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}, mStrMacAddress, mStrPdfFilePath);
	}

	private void doHoneywellLinePrintPdfTask()
	{
		showProgressDialog(Str.Printing[Global.LANG]);
		HoneywellLinePrinter.doPrint(mContext, new HoneywellLinePrinter.PrintCallback() {
			@Override
			public void printFinished() {
				hideProgressDialog();
				try {
					Utils.setStringSetting(mContext, Global.PREF_HONEYWELL_PRINTER_MAC_ADDRESS, mStrMacAddress);
					Utils.printDebug(mContext, Str.SuccessToPrint[Global.LANG]);
					DbAdapter dbAdapter = new DbAdapter(mContext);
					dbAdapter.open();
					if (mIsPickup == false) {
						mDeliveryInfo.nPrintedFlag = 1;
						Global.gUOrderHInfo.nPrintedFlag = 1;
						dbAdapter.updateUOrderHInfo(mDeliveryInfo);
					} else {
						mPickupInfo.nPrintedFlag = 1;
						Global.gUOrderHInfo.nPrintedFlag = 1;
						dbAdapter.updateUOrderHInfo(mPickupInfo);
					}
					dbAdapter.close();
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
						}
					});
					msgDialog.show();
				}catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}, mStrMacAddress, mStrPdfFilePath);
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
					if (mIsPickup == false) {
						mDeliveryInfo.nPrintedFlag = 1;
						Global.gUOrderHInfo.nPrintedFlag = 1;
						dbAdapter.updateUOrderHInfo(mDeliveryInfo);
					} else {
						mPickupInfo.nPrintedFlag = 1;
						Global.gUOrderHInfo.nPrintedFlag = 1;
						dbAdapter.updateUOrderHInfo(mPickupInfo);
					}
					dbAdapter.close();
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
						}
					});
					msgDialog.show();
				}catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}, mStrMacAddress, mStrFriendlyName, mStrPdfFilePath);
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

	private void showPrintConfirmDialog() {
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
			//outputPrinter();
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
				msgDialog.setOnDismissListener(new OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
							outputPrinter();
						} else if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
							DbAdapter dbAdapter = new DbAdapter(mContext);
							dbAdapter.open();
							if (mIsPickup == false) {
								if(mDeliveryInfo == null)
								{
									mDeliveryInfo = Global.gUOrderHInfo;
								}
								mDeliveryInfo.nPrintedFlag = 0;
								dbAdapter.updateUOrderHInfo(mDeliveryInfo);
							} else {
								mPickupInfo.nPrintedFlag = 0;
								dbAdapter.updateUOrderHInfo(mPickupInfo);
							}
							dbAdapter.close();
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
						else if(Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE) == 4)
						{
							discoveryBixolonPrinters();
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

	public boolean m_bClicked = false;
	@Override
	public void onClick(View v) {
		if( m_bClicked ) return;
		m_bClicked = true;
		switch (v.getId()) {
			case R.id.btnDetails:
				CustomerDetailFragment fragment = new CustomerDetailFragment();
				Bundle bundle = new Bundle();
				bundle.putSerializable(Global.EXTRA_CUSTOMER_INFO, Global.gCurCustomerInfo);
				fragment.setArguments(bundle);
				mContext.replaceFragment(fragment);
				break;
			case R.id.btnCompleteVisit:
				doCompleteVisit();


				break;
		}
		m_bClicked = false;
	}

	public void doCompleteVisit()
	{
		if(Utils.getStringSetting(mContext, Global.PREF_CON_EMPTY).equals("1"))
		{
			DbAdapter dbAdapter = new DbAdapter(mContext);
			dbAdapter.open();
			List<CustConBalInfo> custConBalInfoList = dbAdapter.getCustConBalInfo(Global.gCurCustomerInfo.nCustNo, Global.gCurCustomerInfo.nChainNo);
			dbAdapter.close();
			boolean bConbalUsage = false;
			for(CustConBalInfo info : custConBalInfoList)
			{
				if(info.nQty != 0)
				{
					bConbalUsage = true;
					break;
				}
			}

			if(Global.gStockDateTime == 0 && bConbalUsage == true)
			{
				try {
					final MessageDialog msgDialog = new MessageDialog(mContext);
					msgDialog.setCancelable(false);
					msgDialog.mStrMessage = Str.HaveNotCheckedConsignmentStock[Global.LANG];
					msgDialog.mStrLeft = Str.Return[Global.LANG];
					msgDialog.mStrRight = Str.Ok[Global.LANG];
					msgDialog.setOnDismissListener(dialog -> {
						if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
							Global.gStockDateTime = System.currentTimeMillis();
							Global.gAdjustDateTime = System.currentTimeMillis();
							doCompleteVisit();
						} else if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
						}
					});
					msgDialog.show();
				}catch (Exception e)
				{
					e.printStackTrace();
				}
				return;
			}
			else if(Global.gStockDateTime > Global.gAdjustDateTime)
			{
				try {
					final MessageDialog msgDialog = new MessageDialog(mContext);
					msgDialog.setCancelable(false);
					msgDialog.mStrMessage = Str.DoNotAppearHaveStock[Global.LANG];
					msgDialog.mStrLeft = Str.Return[Global.LANG];
					msgDialog.mStrRight = Str.Ok[Global.LANG];
					msgDialog.setOnDismissListener(dialog -> {
						if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
							Global.gStockDateTime = System.currentTimeMillis();
							Global.gAdjustDateTime = System.currentTimeMillis();
							doCompleteVisit();
						} else if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
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

		if(Global.gCurCustomerInfo.strPayType.equals("$") || Global.gCurCustomerInfo.strPayType.equals("1") || Global.gCurCustomerInfo.strPayType.equals("2"))
		{
			DbAdapter dbAdapter = new DbAdapter(mContext);
			dbAdapter.open();
			List<ARHeaderInfo> arHeaderInfos = dbAdapter.getARHeaderInfo(Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo);
			dbAdapter.close();
			float dTotalRemaining = 0;
			if( arHeaderInfos.size() > 0)
			{
				for(ARHeaderInfo arHeaderInfo : arHeaderInfos)
				{
					if(arHeaderInfo.nProcessStatus < 2) {
						if (!arHeaderInfo.strARTrxnType.equals("ORD")) {
							dTotalRemaining += getRoundedValue(arHeaderInfo.nTrxnAmount * 1.0 / Global.MULTIPLE_VALUE);
						}
					}
				}
			}
//			dTotalRemaining = getRoundedValue(dTotalRemaining);

			//if(dTotalRemaining > 0.001 && !Global.gProcessedPayment)
			if(dTotalRemaining > 0.001) //&& !Global.gProcessedPayment this section removed because Mr.Malcolm's request at 18th of July --- DSDVanSales CompleteVisit issue whe nthe sale is for a cash customer,,, word document number..
			{
				m_bClicked = false;
				try {
					final MessageDialog msgDialog = new MessageDialog(mContext);
					msgDialog.setCancelable(false);
					msgDialog.mStrMessage = Str.MustCompleteInvoicePayment[Global.LANG];
					msgDialog.mStrMiddle = Str.Ok[Global.LANG];
					msgDialog.show();
				}catch (Exception e)
				{
					e.printStackTrace();
				}

			}
			else
			{
				doContinueCompleteVisit();
			}
		}
		else
		{
			doContinueCompleteVisit();
		}
	}

	float getRoundedValue(double value)
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

	private void doContinueCompleteVisit()
	{
		boolean bSurveyMadantory = true;
		for(SurveyInfo surveyInfo : mSurveyInfoList)
		{
			if(surveyInfo.strMandatory != null && surveyInfo.strMandatory.equals("Y") &&
					surveyInfo.bCompleted == false)
			{
				bSurveyMadantory = false;
				break;
			}
		}
		if(!bSurveyMadantory)
		{
			m_bClicked = false;
			try {
				final MessageDialog msgDialog = new MessageDialog(mContext);
				msgDialog.setCancelable(false);
				msgDialog.mStrMessage = Str.MustCompleteMandantorySurvey[Global.LANG];
				msgDialog.mStrMiddle = Str.Ok[Global.LANG];
				msgDialog.show();
			}catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			doProcessCompleteVisit();
		}
	}

	private void doProcessCompleteVisit()
	{
		if(Utils.getIntSetting(mContext, Global.PREF_POST_VISIT) == 1 || Utils.getIntSetting(mContext, Global.PREF_POST_VISIT) == 2)
		{
			if(!bPassedPostVisit)
			{
				m_bClicked = false;
				try
				{
					PostVisitDetailsDialog dialog = new PostVisitDetailsDialog(mContext, new PostVisitDetailsDialog.Callback() {
					@Override
					public void onDone(long nTrxnNo) {
						bPassedPostVisit = true;
						AddTrxnNo(Global.gCurCustomerInfo.nCustNo, Global.gCurCustomerInfo.nChainNo, nTrxnNo);
						doComplete();
					}

					@Override
					public void onCancel() {
					}
				});
				dialog.show();
				}catch (Exception e)
				{
					e.printStackTrace();
				}

			}
			else
			{
				doComplete();
			}

		}
		else
		{
			doComplete();
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
							printer.sendFileContents(mStrPdfFilePath);
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
					DbAdapter dbAdapter = new DbAdapter(mContext);
					dbAdapter.open();
					if (mIsPickup == false) {
						mDeliveryInfo.nPrintedFlag = 1;
						Global.gUOrderHInfo.nPrintedFlag = 1;
						dbAdapter.updateUOrderHInfo(mDeliveryInfo);
					} else {
						mPickupInfo.nPrintedFlag = 1;
						Global.gUOrderHInfo.nPrintedFlag = 1;
						dbAdapter.updateUOrderHInfo(mPickupInfo);
					}
					dbAdapter.close();
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
	
	
	private class ListAdapter extends BaseAdapter {

		private class ViewHolder {
			public TextView txtName, txtPrice;
			public ImageView imgIcon;
			public GridLayout gridPresold;
			public List<Button> btnPresoldList = new ArrayList<>();
			Button btnPresold;
		}

		public int getCount() {
			return mCheckList.size();
		}

		public CheckMenuInfo getItem(int position) {
			if(position < mCheckList.size())
			{
				return mCheckList.get(position);
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
			final CheckMenuInfo info = getItem(position);
			if(info == null) return null;

			view = mContext.getLayoutInflater().inflate(R.layout.row_instore, parent, false);
			holder = new ViewHolder();
			holder.txtName = (TextView) view.findViewById(R.id.txtName);
			holder.txtPrice = (TextView) view.findViewById(R.id.txtPrice);
			holder.imgIcon = (ImageView) view.findViewById(R.id.imgIcon);
			holder.gridPresold = (GridLayout)view.findViewById(R.id.gridPresold);
			holder.btnPresold = (Button)view.findViewById(R.id.btnPresold);
			view.setTag(holder);

			holder.btnPresold.setVisibility(View.GONE);
			holder.imgIcon.setVisibility(View.GONE);
			holder.txtPrice.setVisibility(View.GONE);
			if (info.nIdx == MAKE_DELIVERY) {
				if(Global.gCurCustomerInfo.nCreditHold > 0)
				{
					view.setBackgroundResource(R.color.white_color);
					holder.txtName.setTextColor(Color.GRAY);
				}
				else
				{
					view.setBackgroundResource(R.drawable.table_normal);
					holder.txtName.setTextColor(Color.BLACK);
					boolean bCanAdd = true;
					for(PresoldOrHInfo presoldHInfo : Global.gCurCustomerInfo.presoldOrHInfo)
					{
						if (presoldHInfo.strType.equals("P") == true || presoldHInfo.strType.equals("B") == true || presoldHInfo.strType.equals("4")
						|| presoldHInfo.strType.equals("11") || presoldHInfo.strType.equals("12"))
						{
							if( presoldHInfo.strOrderNo.equals("0") && TextUtils.isEmpty(Utils.getStringSetting(mContext, Global.PREF_INVOICENUM)))
							{
								bCanAdd = false;
								break;
							}
						}
					}

					if(!bCanAdd)
					{
						if(Global.gCurCustomerInfo.latestOrderInfo != null && Global.gCurCustomerInfo.latestOrderInfo.size() > 0)
						{
							for(CustomerOrderInfo customerOrderInfo : Global.gCurCustomerInfo.latestOrderInfo)
							{
								for(OrderLineInfo lineInfo : customerOrderInfo.orderLines)
								{
									if(lineInfo.transactionType == 4 || lineInfo.transactionType == 11 || lineInfo.transactionType == 12)
									{
										bCanAdd = true;
										break;
									}
								}
								if(bCanAdd) break;
							}
						}
					}
					if( bCanAdd )
					{
						for(PresoldOrHInfo presoldHInfo : Global.gCurCustomerInfo.presoldOrHInfo)
						{
							if (presoldHInfo.strType.equals("P") == true || presoldHInfo.strType.equals("B") == true ||
									presoldHInfo.strType.equals("C") == true ||  presoldHInfo.strType.equals("4") ||
									presoldHInfo.strType.equals("11") || presoldHInfo.strType.equals("12")) {
								if( presoldHInfo.nVisited == 0 )
								{
									LinearLayout layout = (LinearLayout)mContext.getLayoutInflater().inflate(R.layout.row_instore_makedelivery, parent, false);
									Button btn = (Button)layout.findViewById(R.id.btnPresold);
									btn.setTag(presoldHInfo);
									btn.setOnClickListener(new View.OnClickListener() {

										@Override
										public void onClick(View arg0) {
											// TODO Auto-generated method stub
											PresoldOrHInfo info = (PresoldOrHInfo)arg0.getTag();
											showPresoldDialog(false, info);
										}
									});
									holder.gridPresold.addView(layout);
									((TextView)layout.findViewById(R.id.txtPresold)).setText(Utils.last4Character(presoldHInfo.strOrderNo));
									btn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_has_presold, 0, 0, 0);
								}
							}
						}

						if(Global.gCurCustomerInfo.latestOrderInfo != null && Global.gCurCustomerInfo.latestOrderInfo.size() > 0)
						{
							for(CustomerOrderInfo customerOrderInfo : Global.gCurCustomerInfo.latestOrderInfo)
							{
								boolean bAdd = false;
								for(OrderLineInfo lineInfo : customerOrderInfo.orderLines)
								{
									if(lineInfo.transactionType == 4 || lineInfo.transactionType == 11 || lineInfo.transactionType == 12)
									{
										bAdd = true;
										break;
									}
								}
								if(bAdd)
								{
									LinearLayout layout = (LinearLayout)mContext.getLayoutInflater().inflate(R.layout.row_instore_makedelivery, parent, false);
									Button btn = (Button)layout.findViewById(R.id.btnPresold);
									btn.setTag(customerOrderInfo);
									btn.setOnClickListener(new View.OnClickListener() {

										@Override
										public void onClick(View arg0) {
											// TODO Auto-generated method stub
											CustomerOrderInfo info = (CustomerOrderInfo)arg0.getTag();
											showPresoldDialog(false, info);
										}
									});
									holder.gridPresold.addView(layout);
									((TextView)layout.findViewById(R.id.txtPresold)).setText(Utils.last4Character(String.valueOf(customerOrderInfo.orderNumber)));
									btn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_has_presold, 0, 0, 0);
								}
							}
						}
					}

					DbAdapter dbAdapter = new DbAdapter(mContext);
					dbAdapter.open();
					for(Long nDeliveryTrxn : Global.gCurCustomerDeliveryList)
					{
						LinearLayout layout = (LinearLayout)mContext.getLayoutInflater().inflate(R.layout.row_instore_makedelivery, parent, false);
						Button btn = (Button)layout.findViewById(R.id.btnPresold);
						btn.setOnClickListener(arg0 -> {
							// TODO Auto-generated method stub
							Long nDeliveryTrxn1 = (Long)arg0.getTag();
							showPrintActionDialog(nDeliveryTrxn1);
						});
						btn.setTag(nDeliveryTrxn);
						holder.btnPresoldList.add(btn);
						holder.gridPresold.addView(layout);
						layout.findViewById(R.id.txtPresold).setVisibility(View.GONE);
						UOrderHInfo uOrderHInfo = dbAdapter.getUOrderHInfo(nDeliveryTrxn);
						if(uOrderHInfo != null && uOrderHInfo.nOrgTrxnNo != 0)
						{
							btn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_pdf_voided, 0, 0, 0);
						}
						else
						{
							btn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_pdf_created, 0, 0, 0);
						}
					}
					dbAdapter.close();
				}
			}
			else if (info.nIdx == PICKUP_RETURN) {
				if(Global.gCurCustomerInfo.strDumpsAllowed.equalsIgnoreCase("Y"))
				{
					if (Global.gUARTotalInfo.bIsVisitedPickup == false) {
						view.setBackgroundResource(R.drawable.table_normal);
						holder.txtName.setTextColor(Color.BLACK);
						if(Global.gCurCustomerInfo.nInvoiceFmt == 0 )
						{
							for(PresoldOrHInfo presoldHInfo : Global.gCurCustomerInfo.presoldOrHInfo)
							{
								if (presoldHInfo.strType.equals("R") == true) {
									LinearLayout layout = (LinearLayout)mContext.getLayoutInflater().inflate(R.layout.row_instore_makedelivery, parent, false);
									Button btn = (Button)layout.findViewById(R.id.btnPresold);
									btn.setTag(presoldHInfo);
									btn.setOnClickListener(new View.OnClickListener() {

										@Override
										public void onClick(View arg0) {
											// TODO Auto-generated method stub
											PresoldOrHInfo info = (PresoldOrHInfo)arg0.getTag();
											showPresoldDialog(true, info);
										}
									});
									holder.gridPresold.addView(layout);
									((TextView)layout.findViewById(R.id.txtPresold)).setText(Utils.last4Character(presoldHInfo.strOrderNo));
									btn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_has_presold, 0, 0, 0);
								}
							}
						}
					} else {
						view.setBackgroundResource(R.color.white_color);
						holder.txtName.setTextColor(Color.GRAY);
						if (Global.gCurCustomerInfo.nInvoiceFmt == 0) {
							if (mPickupInfo != null) {
								if (mPickupInfo.nVoidFlag == 0) {
									holder.btnPresold.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_pdf_created, 0, 0, 0);
									holder.btnPresold.setEnabled(true);
								} else {
									holder.btnPresold.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_pdf_voided, 0, 0, 0);
									holder.btnPresold.setEnabled(false);
								}
								holder.btnPresold.setVisibility(View.VISIBLE);
							}
						}
					}
				}
				else
				{
					view.setBackgroundResource(R.color.white_color);
					holder.txtName.setTextColor(Color.GRAY);
				}

			}
			else if (info.nIdx == COLLECT_PAYMENT) {
				if(Global.gCurCustomerInfo.nShowPrice != 2)
				{
					DbAdapter dbAdapter = new DbAdapter(mContext);
					dbAdapter.open();
					List<ARHeaderInfo> arHeaderInfos = dbAdapter.getARHeaderInfo(Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo);
					dbAdapter.close();
					float dTotalRemaining = 0;
					if( arHeaderInfos.size() > 0)
					{
						for(ARHeaderInfo arHeaderInfo : arHeaderInfos)
						{
							if(arHeaderInfo.nProcessStatus <= 2) {
								if (!arHeaderInfo.strARTrxnType.equals("ORD")) {
									dTotalRemaining += arHeaderInfo.nTrxnAmount * 1.0 / Global.MULTIPLE_VALUE;
								}
							}
						}
					}

					holder.txtPrice.setVisibility(View.VISIBLE);
					String strShowValue = getRoundedFormattedPrice(dTotalRemaining);
					float fValue = Utils.getFloatFromString(strShowValue.substring(Global.gCurrencySymbol.length()));
					if( fValue == 0.0f )
					{
						view.setBackgroundResource(R.color.white_color);
						holder.txtName.setTextColor(Color.GRAY);
						holder.txtPrice.setTextColor(Color.GRAY);
					}
					else
					{
						view.setBackgroundResource(R.drawable.table_normal);
						holder.txtName.setTextColor(Color.BLACK);
						holder.txtPrice.setTextColor(Color.BLACK);
					}

					holder.txtPrice.setText(getRoundedFormattedPrice(dTotalRemaining));
				}
				else
				{
					holder.txtPrice.setVisibility(View.VISIBLE);
					view.setBackgroundResource(R.color.white_color);
					holder.txtName.setTextColor(Color.GRAY);
					holder.txtPrice.setTextColor(Color.GRAY);
					holder.txtPrice.setText(String.format(Locale.ENGLISH, "%s0.00", Global.gCurrencySymbol));
				}

				String strDisallowAccountPayment = Utils.getStringSetting(mContext, Global.PREF_DISALLOW_ACCOUNT_PAY);
				String strPayType = Global.gCurCustomerInfo.strPayType;
				if (!strPayType.equals("3") && strDisallowAccountPayment.equals("1") && !strPayType.equals("1") && !strPayType.equals("2") && !strPayType.equals("C")) {
					view.setBackgroundResource(R.color.white_color);
					holder.txtName.setTextColor(Color.GRAY);
					holder.txtPrice.setTextColor(Color.GRAY);
				}

				if (strPayType.equals("6") || strPayType.equals("B") || strPayType.equals("C") || strPayType.isEmpty()) {
					view.setBackgroundResource(R.color.white_color);
					holder.txtName.setTextColor(Color.GRAY);
					holder.txtPrice.setTextColor(Color.GRAY);
				}
			}
			else if(info.nIdx == EQUIPMENT_IDX) {

				if( info.bHasSurvey )
				{
					holder.txtName.setTextColor(Color.BLACK);
				}
				else
				{
					holder.txtName.setTextColor(mContext.getResources().getColor(R.color.green_color));
				}
				if (info.bCompleted == true) {
					if (Utils.getIntFromString(Global.PREF_EQUIPCHECK) == 2) {
						view.setBackgroundResource(R.drawable.table_normal);
					}
					else {
						view.setBackgroundResource(R.color.white_color);
					}
					holder.imgIcon.setBackgroundResource(R.drawable.icon_check);
					holder.imgIcon.setVisibility(View.VISIBLE);
				} else {
					view.setBackgroundResource(R.drawable.table_normal);
				}
			}
			else if(info.nIdx == CONSIGNMENT_STOCK_COUNT || info.nIdx == UPDATE_CONSIGNMENT_INVENTORY){
				holder.txtName.setTextColor(Color.BLACK);
				view.setBackgroundResource(R.drawable.table_normal);
			}
			else if(info.nIdx == VIEW_PREVIOUS_INVOICES_IDX)
			{
				holder.txtName.setTextColor(Color.BLACK);
				view.setBackgroundResource(R.drawable.table_normal);
			}
			else {
				if (info.bHasSurvey == false) {
					view.setBackgroundResource(R.color.white_color);
					holder.txtName.setTextColor(Color.GRAY);
				} else {
					if(info.bMandatory)
					{
						holder.txtName.setTextColor(mContext.getResources().getColor(R.color.green_color));
					}
					else {
						holder.txtName.setTextColor(Color.BLACK);
					}

					if (info.bCompleted == true) {
						view.setBackgroundResource(R.color.white_color);
						holder.imgIcon.setBackgroundResource(R.drawable.icon_check);
						holder.imgIcon.setVisibility(View.VISIBLE);
					} else {
						view.setBackgroundResource(R.drawable.table_normal);
					}
				}
			}

			holder.txtName.setText(info.strName);

			holder.btnPresold.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (info.nIdx == MAKE_DELIVERY) {
						if (Global.gUARTotalInfo.bIsVisitedDelivery == false) {
//							showPresoldDialog(false);
						} else {
							if (mDeliveryInfo != null && mDeliveryInfo.nVoidFlag == 0) {
								showPrintActionDialog(false);
							}
						}
					} else if (info.nIdx == PICKUP_RETURN) {
						if(Global.gCurCustomerInfo.strDumpsAllowed.equalsIgnoreCase("Y")){
							if (Global.gUARTotalInfo.bIsVisitedPickup == false) {
//								showPresoldDialog(false);
							} else {
								if (mPickupInfo != null && mPickupInfo.nVoidFlag == 0) {
									showPrintActionDialog(true);
								}
							}
						}
					}
				}
			});
			view.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					gotoCheck(info);
				}
			});

			return view;
		}
	}

	public void gotoDelivery(PresoldOrHInfo mInfo) {
		if(Global.gCurCustomerInfo == null) return;
		if(Global.gCurCustomerInfo.latestOrderInfo == null)
		{
			Global.gCurCustomerInfo.latestOrderInfo = new ArrayList<>();
		}
		else
		{
			Global.gCurCustomerInfo.latestOrderInfo.clear();
		}
		Global.gCurCustomerInfo.presoldOrHInfo.clear();
		Global.gCurCustomerInfo.presoldOrHInfo.add(mInfo);
		gotoDeliveryPickup(false);
	}

	public void gotoDelivery(CustomerOrderInfo mInfo) {
		if(Global.gCurCustomerInfo == null) return;
		if(Global.gCurCustomerInfo.latestOrderInfo == null)
		{
			Global.gCurCustomerInfo.latestOrderInfo = new ArrayList<>();
		}
		else
		{
			Global.gCurCustomerInfo.latestOrderInfo.clear();
		}
		Global.gCurCustomerInfo.presoldOrHInfo.clear();
		Global.gCurCustomerInfo.latestOrderInfo.add(mInfo);
		gotoDeliveryPickup(false);
	}
}