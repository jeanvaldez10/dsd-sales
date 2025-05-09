package dsd.dsdvansales.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.*;
import android.content.DialogInterface.*;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.*;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.AdapterView.*;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
//import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.nhaarman.listviewanimations.itemmanipulation.*;
import com.nhaarman.listviewanimations.itemmanipulation.dragdrop.OnItemMovedListener;
import com.nhaarman.listviewanimations.util.*;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.ResponseBody;

import org.apache.commons.net.ftp.FTPClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.text.*;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.CountDownLatch;

import dsd.dsdvansales.R;
import dsd.dsdvansales.activities.MainActivity;
import dsd.dsdvansales.api.ConnectApiClient;
import dsd.dsdvansales.api.RoutificApiClient;
import dsd.dsdvansales.db.DbAdapter;
import dsd.dsdvansales.dialogs.CustomerActionDialog;
import dsd.dsdvansales.dialogs.CustomerInfoDialog;
import dsd.dsdvansales.dialogs.CustomerNoteDialog;
import dsd.dsdvansales.dialogs.MessageDialog;
import dsd.dsdvansales.dialogs.NonDeliveryDialog;
import dsd.dsdvansales.dialogs.OptimizationDialog;
import dsd.dsdvansales.dialogs.InputDialog;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Hotspot;
import dsd.dsdvansales.global.OTPUtils;
import dsd.dsdvansales.global.RoutificLogs;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.model.ARHeaderInfo;
import dsd.dsdvansales.model.CustomerInfo;
import dsd.dsdvansales.model.CustomerNoteInfo;
import dsd.dsdvansales.model.DescTypeInfo;
import dsd.dsdvansales.model.DocTextInfo;
import dsd.dsdvansales.model.LoadDetl;
import dsd.dsdvansales.model.LoadDetlInfo;
import dsd.dsdvansales.model.LoadHead;
import dsd.dsdvansales.model.LoadHeadInfo;
import dsd.dsdvansales.model.LoadTripResponse;
import dsd.dsdvansales.model.LocationInfo;
import dsd.dsdvansales.model.LoginInfo;
import dsd.dsdvansales.model.MakeZipFileInfo;
import dsd.dsdvansales.model.PresoldOrHInfo;
import dsd.dsdvansales.model.RoutificJob;
import dsd.dsdvansales.model.RoutificLocation;
import dsd.dsdvansales.model.RoutificResponse;
import dsd.dsdvansales.model.StatusInfo;
import dsd.dsdvansales.model.TransactionInfo;
import dsd.dsdvansales.model.UARPaymentInfo;
import dsd.dsdvansales.model.UARTotalInfo;
import dsd.dsdvansales.model.UOrderStatusDInfo;
import dsd.dsdvansales.model.UOrderStatusHInfo;
import dsd.dsdvansales.model.URouteSchBody;
import dsd.dsdvansales.model.URouteSchHeader;
import dsd.dsdvansales.model.UVisitInfo;
import dsd.dsdvansales.model.UploadInfo;
import dsd.dsdvansales.parsers.ParseXMLFile;
import dsd.dsdvansales.views.AlertSingleChoiceAdapter;
import dsd.dsdvansales.views.AlertSingleCustomerChoiceAdapter;
import dsd.dsdvansales.views.FullDrawerLayout;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import retrofit2.Call;

public class SelectCustomerFragment extends BaseFragment implements View.OnClickListener {

	private TextView mTxtDetailNoData = null;
	private DynamicListView mDetailListView = null;
	private DetailListAdapter mDetailListAdapter = null;

	private TextView mTxtNoData = null;
	private DynamicListView mListView = null;
	private ListAdapter mListAdapter = null;
	private List<CustomerInfo> mCustomerList = new ArrayList<>();
	private CheckBox mChkShowAll = null;

	private boolean bRes = false;
	retrofit.Response<RoutificJob> res = null;
	private String mCartonDescription = "";

	private boolean m_bBackProcessed = false;

	private String strETATime = "", strETADate = "";

	private boolean bModified = false;
	CustomerInfo mSelCustomerInfo = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootView = inflater.inflate(R.layout.fragment_select_customer, container, false);
		commonInit();
		return mRootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		Global.gFragIdx = Global.FRAG_SELECT_CUSTOMER;
		Global.gCurCustomerInfo = null;
	}

	@Override
	public void initView() {

		String strDefaultTodayScreen = Utils.getStringSetting(mContext, Global.PREF_DEFAULT_TODAY_SCREEN);
		if (strDefaultTodayScreen.equals("1")) {
			LinearLayout drawerFirstLayout = mRootView.findViewById(R.id.drawerFirstLayout);
			LinearLayout drawerSecondLayout = mRootView.findViewById(R.id.drawerSecondLayout);
			View listView = mRootView.findViewById(R.id.listView);
			View detailListView = mRootView.findViewById(R.id.detail_listview);
			drawerFirstLayout.removeView(listView);
			drawerSecondLayout.removeView(detailListView);
			drawerFirstLayout.addView(detailListView, 0);
			drawerSecondLayout.addView(listView, 0);
		}

		mRootView.findViewById(R.id.btnSearch).setOnClickListener(this);
		mRootView.findViewById(R.id.btnEndTrip).setOnClickListener(this);
//		if( Utils.getIntSetting(mContext, Global.PREF_LOADMULTIALL) == 1)
//		{
			mRootView.findViewById(R.id.btnBack).setVisibility(View.VISIBLE);
			mRootView.findViewById(R.id.btnBack).setOnClickListener(this);
//		}
//		else
//		{
//			mRootView.findViewById(R.id.btnBack).setVisibility(View.INVISIBLE);
//		}

		if (Utils.getIntSetting(mContext, Global.PREF_CUST_ADD_NEW) == 1) {
			mRootView.findViewById(R.id.btnAddCustomer).setVisibility(View.VISIBLE);
			mRootView.findViewById(R.id.btnAddCustomer).setOnClickListener(this);
		} else {
			mRootView.findViewById(R.id.btnAddCustomer).setVisibility(View.GONE);
		}

		mTxtNoData = (TextView) mRootView.findViewById(R.id.txtNoData);
		mListView = (DynamicListView) mRootView.findViewById(R.id.listView);
		mListAdapter = new ListAdapter();
		mListView.setAdapter(mListAdapter);
		mListView.enableDragAndDrop();
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				doClickItem(position);


			}
		});
		mListView.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				mListView.startDragging(position);
				return true;
			}
		});

		mListView.setOnItemMovedListener(new OnItemMovedListener() {
			@Override
			public void onItemMoved(int originalPosition, int newPosition) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
				strETATime = "";
				strETADate = sdf.format(new Date());
				bModified = true;
			}
		});

		mTxtDetailNoData = (TextView) mRootView.findViewById(R.id.txtDetailNoData);
		mDetailListView = (DynamicListView) mRootView.findViewById(R.id.detail_listview);
		mDetailListAdapter = new DetailListAdapter();
		mDetailListView.setAdapter(mDetailListAdapter);

		mChkShowAll = (CheckBox) mRootView.findViewById(R.id.chkShowAll);
		mChkShowAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
				refreshPage();
			}
		});

		//if (Global.gCustomerList.size() == 0) {
			loadData();
		//}
		refreshPage();
		refreshLanguage();

//		if(Utils.getStringSetting(mContext, Global.PREF_ENABLE_BREAKS).equals("1"))
//		{
//
//		}
	}

	float getRoundedValue(float value)
	{
		if(Global.gCurCustomerInfo == null)
		{
			Global.gCurCustomerInfo = mSelCustomerInfo;
		}

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

	public void doClickItem(int position)
	{
		if(bModified)
		{
			saveData();
		}

		Global.gCurCustomerInfo = mCustomerList.get(position);
		mSelCustomerInfo = Global.gCurCustomerInfo;

		if(Global.gCurCustomerInfo.nCreditHold > 0)
		{
			String strDesc = Str.CUSTOMER_IS_ON_CREDIT_HOLD[Global.LANG];
			try {
				CustomerInfoDialog dialog = new CustomerInfoDialog(mContext);
				dialog.setData(Global.gCurCustomerInfo.strDexVersion, strDesc, SelectCustomerFragment.this);
				dialog.showReturnBtn(false);
				dialog.show();
			}catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			String strDesc = "";
			if(Global.gCurCustomerInfo.strPayType.equals("B"))
			{
				DbAdapter dbAdapter = new DbAdapter(mContext);
				dbAdapter.open();
				List<ARHeaderInfo> arHeaderInfoList = dbAdapter.getARHeaderInfo(Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo);
				dbAdapter.close();
				float dTotalRemaining = 0;
				if( arHeaderInfoList.size() > 0)
				{
					for(ARHeaderInfo arHeaderInfo : arHeaderInfoList)
					{
						if(arHeaderInfo.nProcessStatus <= 2 )
							dTotalRemaining += getRoundedValue(arHeaderInfo.nTrxnAmount * 1.0f / Global.MULTIPLE_VALUE);
					}
					if(dTotalRemaining > 0f)
					{
						strDesc = String.format(Locale.ENGLISH, Str.BillToBillCustomerInfo[Global.LANG], String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(mContext, Global.PREF_DECIMAL) + "f",dTotalRemaining));
						strDesc += "\n\n";
					}
				}
			}

			String strMsgSetting = Utils.getStringSetting(mContext, Global.PREF_CUSTINFO_MSG);
			List<String> strTextTypeList = new ArrayList<>();
			strTextTypeList.add("3");
			strTextTypeList.add("7");
			String[] strMsgs = strMsgSetting.split(",");
			for(int i = 0; i<strMsgs.length; i++)
			{
				strTextTypeList.add(strMsgs[i].trim());
			}
			List<List<DocTextInfo>> docTextList = new ArrayList<>();
			DbAdapter dbAdapter = new DbAdapter(mContext);
			dbAdapter.open();
			for(String strMsgSettingNo : strTextTypeList)
			{
				int nType = Utils.parseIntFromString(strMsgSettingNo);
				List<DocTextInfo> docText = dbAdapter.getDocTextList(Global.gCurCustomerInfo.nCustNo, Global.gCurCustomerInfo.nChainNo, nType);
				docTextList.add(docText);
			}
			dbAdapter.close();
			for(List<DocTextInfo> docTextInfo : docTextList)
			{
				for(DocTextInfo info : docTextInfo)
				{
					if(!info.strSubType.equals("P"))
					{
						strDesc += info.strDocText + "\n";
					}
				}
			}

			if(TextUtils.isEmpty(strDesc) && TextUtils.isEmpty(Global.gCurCustomerInfo.strDexVersion))
			{
				showCustomerActionDialog(Global.gCurCustomerInfo.strName);
			}
			else
			{
				try {
					CustomerInfoDialog dialog = new CustomerInfoDialog(mContext);
					dialog.setData(Global.gCurCustomerInfo.strDexVersion, strDesc, SelectCustomerFragment.this);
					dialog.show();
				}catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}

	}

	@Override
	public void onPause() {
		super.onPause();
		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		//Save Current Order to DB.
		int i = 0;
		for(CustomerInfo info : mCustomerList )
		{
			info.nOrderNo = i;
			i++;
			dbAdapter.updateCustomerInfo(info);
		}
		dbAdapter.close();

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
		Global.gCustomerList = filterSameCustomer(dbAdapter.getWhiteCustomerInfo(false, Utils.getIntSetting(mContext,  Global.DAY_NO)));

		Global.gCustomerList.addAll(dbAdapter.getCustomerList(true, false, Utils.getIntSetting(mContext, Global.DAY_NO)));

		for (int i=0; i<Global.gCustomerList.size(); i++) {
			Global.gCustomerList.get(i).nMessageCnt = dbAdapter.getMessageList(Global.gCustomerList.get(i).nCustNo).size();
			Global.gCustomerList.get(i).presoldOrHInfo = dbAdapter.getPresoldOrHInfo(Global.gCustomerList.get(i).nChainNo, Global.gCustomerList.get(i).nCustNo, Global.gCustomerList.get(i).nPeriodNo);
			Global.gCustomerList.get(i).latestOrderInfo = dbAdapter.getCustomerLatestOrderList(Global.gCustomerList.get(i).nCustNo, Global.gCustomerList.get(i).nChainNo);

			List<PresoldOrHInfo> infos = new ArrayList<>();
			for(PresoldOrHInfo info : Global.gCustomerList.get(i).presoldOrHInfo)
			{
				if( info.nVisited == 0)
				{
					infos.add(info);
				}
			}

			Global.gCustomerList.get(i).presoldOrHInfo = infos;

			String[] strCustInfos = Utils.getStringSetting(mContext, Global.PREF_CUSTINFO_MSG).split(",");
			Global.gCustomerList.get(i).strCustMsg= "";

			List<DocTextInfo> allDocTextInfoList = new ArrayList<>();
			for(int j = 0; j < strCustInfos.length; j++)
			{
				int nTextType = Utils.parseIntFromString(strCustInfos[j].trim());
				List<DocTextInfo> docTextInfoList = dbAdapter.getDocTextList(Global.gCustomerList.get(i).nCustNo, nTextType);
				allDocTextInfoList.addAll(docTextInfoList);
			}

			Collections.sort(allDocTextInfoList, new Comparator<DocTextInfo>() {
				@Override
				public int compare(DocTextInfo docTextInfo, DocTextInfo t1) {
					if(docTextInfo.nTextType == t1.nTextType)
					{
						return docTextInfo.nLine - t1.nLine;
					}
					else
					{
						return docTextInfo.nTextType - t1.nTextType;
					}
				}
			});

			for(DocTextInfo docTextInfo : allDocTextInfoList)
			{
				if(TextUtils.isEmpty(Global.gCustomerList.get(i).strCustMsg))
				{
					Global.gCustomerList.get(i).strCustMsg = docTextInfo.strDocText;
				}
				else
				{
					Global.gCustomerList.get(i).strCustMsg += "\n" + docTextInfo.strDocText;
				}
			}
		}
		if (Global.gCustomerList.size() > 0) {
			if (Global.gCustomerList.get(0).nOrderNo == -1) {
				Collections.sort(Global.gCustomerList, CustomerInfo.seqNoComparator);
				Collections.sort(Global.gCustomerList, CustomerInfo.periodNoComparator);
				Collections.sort(Global.gCustomerList, CustomerInfo.deliveryDateComparator);
				for (int i=0; i<Global.gCustomerList.size(); i++) {
					Global.gCustomerList.get(i).nOrderNo = i;
					dbAdapter.updateCustomerInfo(Global.gCustomerList.get(i));
				}
			} else {
				Collections.sort(Global.gCustomerList, CustomerInfo.orderComparator);
			}
		}
		mListAdapter.notifyDataSetChanged();
		dbAdapter.close();

		if(!Utils.getBooleanSetting(mContext, Global.PREF_VISIT_SELECT_CUSTOMER))
		{
			long nTrxnNo = setGeneralUploadData("PTSE", 0, 0, -1);

			TransactionInfo transactionInfo = new TransactionInfo();
			transactionInfo.strType = "PTSE";
			transactionInfo.strTime = new SimpleDateFormat("kk:mm").format(new Date(nTrxnNo));
			dbAdapter = new DbAdapter(mContext);
			dbAdapter.open();
			dbAdapter.insertTransactionHistoryInfo(transactionInfo);
			dbAdapter.close();

			Utils.setBooleanSetting(mContext, Global.PREF_VISIT_SELECT_CUSTOMER, true);
			//Global.gUploadCustNoList.add(makeZipFileInfo);
			Utils.registerUploadFile(mContext, makeZipFileInfo);
			makeZipFileInfo = null;
		}
	}

	@Override
	public void refreshPage() {
		String apiToken = Utils.getStringSetting(mContext, Global.PREF_ROUTIFICAPI);
		if( TextUtils.isEmpty(apiToken) )
		{
			mRootView.findViewById(R.id.btnRoutific).setVisibility(View.INVISIBLE);
			mRootView.findViewById(R.id.btnRoutific).setOnClickListener(null);
		}
		else
		{
			mRootView.findViewById(R.id.btnRoutific).setVisibility(View.VISIBLE);
			mRootView.findViewById(R.id.btnRoutific).setOnClickListener(this);
		}

		mCustomerList.clear();
		int nUnCompletedCustomer = 0;
		for (CustomerInfo info : Global.gCustomerList) {
			if (mChkShowAll.isChecked() == false && info.bCompleted == true) {
				continue;
			}
			if(!info.bCompleted) nUnCompletedCustomer++;
			mCustomerList.add(info);
		}
		if (mCustomerList.size() > 0) {
			mListView.setVisibility(View.VISIBLE);
			mDetailListView.setVisibility(View.VISIBLE);
			mTxtNoData.setVisibility(View.GONE);
			mTxtDetailNoData.setVisibility(View.GONE);
			mListAdapter.notifyDataSetChanged();
			mDetailListAdapter.notifyDataSetChanged();
		} else {
			mListAdapter.notifyDataSetChanged();
			mDetailListAdapter.notifyDataSetChanged();
			mListView.setVisibility(View.GONE);
			mDetailListView.setVisibility(View.GONE);
			mTxtNoData.setVisibility(View.VISIBLE);
			mTxtDetailNoData.setVisibility(View.VISIBLE);
		}

		if(nUnCompletedCustomer > 0 && Utils.getStringSetting(mContext, Global.PREF_ENDTRIP_HIDE).equals("1"))
		{
			mRootView.findViewById(R.id.btnEndTrip).setVisibility(View.GONE);
		}
		else
		{
			mRootView.findViewById(R.id.btnEndTrip).setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void refreshLanguage() {
		((TextView) mRootView.findViewById(R.id.lblTitle)).setText(Str.SELECT_CUSTOMER[Global.LANG].toUpperCase());
		//((TextView) mRootView.findViewById(R.id.lblPlanForTrip)).setText(Str.PlanForTrip[Global.LANG].toUpperCase());
		((TextView) mRootView.findViewById(R.id.lblPlanForTrip)).setText(Utils.getWeekDateName(Utils.getIntSetting(mContext, Global.DAY_NO))+ "'s Trip");
		((Button) mRootView.findViewById(R.id.btnEndTrip)).setText(Str.EndTrip[Global.LANG]);
		((Button) mRootView.findViewById(R.id.btnSearch)).setText(Str.Search_here[Global.LANG]);
		mTxtNoData.setText(Str.No_data_has_been_found[Global.LANG]);
		mTxtDetailNoData.setText(Str.No_data_has_been_found[Global.LANG]);
		mChkShowAll.setText(Str.Select_All[Global.LANG]);
	}

	@Override
	public void receiveBroadcast(String strAction, Intent intent) {
		if (strAction.equals(Global.ACTION_REFRESH_CUSTOMER_LIST) == true) {
			loadData();
			refreshPage();
		}
	}

	public void showCustomerActionDialog(String strCustomerName) {

		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		List<CustomerNoteInfo> noteInfoList = dbAdapter.getCustomerNoteInfoList(Global.gCurCustomerInfo.nCustNo, Global.gCurCustomerInfo.nChainNo);
		dbAdapter.close();
		CustomerNoteInfo selInfo = null;
		for(CustomerNoteInfo noteInfo : noteInfoList)
		{
			if(noteInfo.strNoteType.equals("92"))
			{
				selInfo = noteInfo;
				break;
			}
		}

		if(selInfo != null)
		{
			//Show the Customer Note Dialog
			CustomerNoteDialog customerNoteDialog = new CustomerNoteDialog(mContext, selInfo, () -> {
				doShowCustomerActionDialog(strCustomerName);
			});
			customerNoteDialog.show();
		}
		else
		{
			doShowCustomerActionDialog(strCustomerName);
		}

	}

	private void doShowCustomerActionDialog(String strCustomerName)
	{
		if(Global.gCurCustomerInfo == null)
		{
			Global.gCurCustomerInfo = mSelCustomerInfo;
		}
		try {
			final CustomerActionDialog dlg = new CustomerActionDialog(mContext);
			dlg.setFragment(this);
			dlg.mStrCustomerName = strCustomerName;
			dlg.setCancelable(false);
			dlg.setOnDismissListener(new OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
					if(Global.gCurCustomerInfo == null)
					{
						Global.gCurCustomerInfo = mSelCustomerInfo;
					}

					if (dlg.mReturnCode == Global.ACTION_DELIVER_TO_STORE) {
						//checkIsAtStore();
					} else if (dlg.mReturnCode == Global.ACTION_VIEW_STORE_DETAILS) {
						Fragment fragment = new CustomerDetailFragment();
						Bundle bundle = new Bundle();
						bundle.putSerializable(Global.EXTRA_CUSTOMER_INFO, Global.gCurCustomerInfo);
						fragment.setArguments(bundle);
						mContext.replaceFragment(fragment);
					} else if (dlg.mReturnCode == Global.ACTION_UNABLE_TO_DELIVER) {
						showNonDeliveryDialog();
					} else if (dlg.mReturnCode == Global.ACTION_NAVIGATE_ME_TO_STORE) {
						navigateMeToStore();
					}
				}
			});
			dlg.show();
		}catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void checkIsAtStore(Location location)
	{
		double nMyLat = location.getLatitude();
		double nMyLng = location.getLongitude();
		if (nMyLat == 0 || nMyLng == 0) {
			gotoInStore();
		}
		else {
			final CustomerInfo info = Global.gCurCustomerInfo;
//			if(info.latitude == 0 || info.longitude == 0)
//			{
//				gotoInStore();
//			}
			if (info == null) {
				gotoInStore();
			}

			if(info.driverLatitude != 0 && info.driverLongitude != 0)
			{
				float fNotAtStoreRadius = Global.NOT_AT_STORE_RADIUS;
				String strNotAtStoreRadius = Utils.getStringSetting(mContext, Global.PREF_NOT_AT_STORE_RADIUS);
				if (!strNotAtStoreRadius.isEmpty()) {
					float fPrefNotAtStoreRadius = 0f;
					try {
						fPrefNotAtStoreRadius = Float.parseFloat(strNotAtStoreRadius) / 1000;
					}
					catch (Exception ex) {
					}

					if (fPrefNotAtStoreRadius != 0f) {
						fNotAtStoreRadius = fPrefNotAtStoreRadius;
					}
				}
				if(Utils.getDistance(nMyLat, nMyLng, info.driverLatitude, info.driverLongitude) > fNotAtStoreRadius)
				{
					String strNotAtStore = Utils.getStringSetting(mContext, Global.PREF_NOT_AT_STORE);
					try {
						final MessageDialog dlg = new MessageDialog(mContext);
						dlg.mStrTitle = Str.NotAtTheStore[Global.LANG].toUpperCase();
						dlg.mStrMessage = Str.NotAtTheStore_msg[Global.LANG];
						if (strNotAtStore.equals("1")) {
							dlg.mStrMiddle = Str.Return[Global.LANG];
						}
						else {
							dlg.mStrLeft = Str.Return[Global.LANG];
							dlg.mStrRight = Str.Confirm[Global.LANG];
						}
						dlg.mStrCustomerName = info.strName;
						dlg.setCancelable(false);
						dlg.setOnDismissListener(dialog -> {
							if(Global.gCurCustomerInfo == null)
							{
								Global.gCurCustomerInfo = mSelCustomerInfo;
							}
							if (strNotAtStore.equals("1")) {
								if (dlg.mReturnCode == Global.DLG_RETURN_MIDDLE) {
									Global.gCurCustomerInfo = null;
								}
							}
							else {
								if (dlg.mReturnCode == Global.DLG_RETURN_RIGHT) {
									gotoInStore();
								}
								else if(dlg.mReturnCode == Global.DLG_RETURN_LEFT)
								{
									Global.gCurCustomerInfo = null;
								}
							}
						});
						dlg.show();
					}catch (Exception e)
					{
						e.printStackTrace();
					}
				}
				else {
					gotoInStore();
				}
			}
			else if (info.latitude != 0 && info.longitude != 0 ) {
				float fNotAtStoreRadius = Global.NOT_AT_STORE_RADIUS;
				String strNotAtStoreRadius = Utils.getStringSetting(mContext, Global.PREF_NOT_AT_STORE_RADIUS);
				if (!strNotAtStoreRadius.isEmpty()) {
					float fPrefNotAtStoreRadius = 0f;
					try {
						fPrefNotAtStoreRadius = Float.parseFloat(strNotAtStoreRadius) / 1000; // convert into km
					}
					catch (Exception ex) {
					}

					if (fPrefNotAtStoreRadius != 0f) {
						fNotAtStoreRadius = fPrefNotAtStoreRadius;
					}
				}
				if(Utils.getDistance(nMyLat, nMyLng, info.latitude, info.longitude) > fNotAtStoreRadius)
				{
					String strNotAtStore = Utils.getStringSetting(mContext, Global.PREF_NOT_AT_STORE);
					try {
						final MessageDialog dlg = new MessageDialog(mContext);
						dlg.mStrTitle = Str.NotAtTheStore[Global.LANG].toUpperCase();
						dlg.mStrMessage = Str.NotAtTheStore_msg[Global.LANG];
						if (strNotAtStore.equals("1")) {
							dlg.mStrMiddle = Str.Return[Global.LANG];
						}
						else {
							dlg.mStrLeft = Str.Return[Global.LANG];
							dlg.mStrRight = Str.Confirm[Global.LANG];
						}
						dlg.mStrCustomerName = info.strName;
						dlg.setCancelable(false);
						dlg.setOnDismissListener(dialog -> {
							if(Global.gCurCustomerInfo == null)
							{
								Global.gCurCustomerInfo = mSelCustomerInfo;
							}
							if (strNotAtStore.equals("1")) {
								if (dlg.mReturnCode == Global.DLG_RETURN_MIDDLE) {
									Global.gCurCustomerInfo = null;
								}
							}
							else {
								if (dlg.mReturnCode == Global.DLG_RETURN_RIGHT) {
									gotoInStore();
								}
								else if(dlg.mReturnCode == Global.DLG_RETURN_LEFT)
								{
									Global.gCurCustomerInfo = null;
								}
							}
						});
						dlg.show();
					}catch (Exception e)
					{
						e.printStackTrace();
					}
				}
				else {
					gotoInStore();
				}
			} else {
				gotoInStore();
			}
		}
	}

	public void checkIsAtStore() {
		if(Global.gCurCustomerInfo == null)
		{
			Global.gCurCustomerInfo = mSelCustomerInfo;
		}

		if(ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
				ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
		{
			Toast.makeText(mContext, Str.LocationPermissionDenied[Global.LANG], Toast.LENGTH_LONG).show();
			return;
		}

		showProgressDialog("Getting current location...");

		LocationServices.getFusedLocationProviderClient(mContext).getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
			@Override
			public void onComplete(@NonNull Task<Location> task) {
				if (task.isSuccessful() && task.getResult() != null) {
					hideProgressDialog();
					Location location = task.getResult();
					checkIsAtStore(location);
				} else {
					hideProgressDialog();
					gotoInStore();
					Toast.makeText(mContext, Str.FailedToGetCurrentLocation[Global.LANG], Toast.LENGTH_LONG).show();
				}
			}
		});
	}

	private void gotoInStore() {

		if(Global.gCurCustomerInfo == null)
		{
			Global.gCurCustomerInfo = mSelCustomerInfo;
		}
		Global.gUARTotalInfo = new UARTotalInfo();
		Global.gUARPaymentCardInfo = new UARPaymentInfo();
		Global.gUARPaymentChequeInfo = new UARPaymentInfo();
		Global.gUARPaymentEFTInfo = new UARPaymentInfo();

		Global.gUARTotalInfoList.clear();
		Global.gUARPaymentCardInfoList.clear();
		Global.gUARPaymentChequeInfoList.clear();
		Global.gUARPaymentEFTInfoList.clear();

		Global.gProcessedPayment = false;

		Global.gVisitStartedTime = new SimpleDateFormat("HH:mm:ss").format(new Date());
		Global.gCurCustomerDeliveryList.clear();

		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		List<ARHeaderInfo> arHeaderInfos = dbAdapter.getARHeaderInfo(Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo);
		Global.gCurCustomerTemplateOrderInfoList = dbAdapter.getCustomerTemplateOrderInfoList(Global.gCurCustomerInfo.nTemplateGrp, Global.gCurCustomerInfo.nCustNo);
		dbAdapter.close();
		float dTotalRemaining = 0;
		if( arHeaderInfos.size() > 0)
		{
			for(ARHeaderInfo arHeaderInfo : arHeaderInfos)
			{
				if(arHeaderInfo.nProcessStatus < 2 )
					dTotalRemaining += arHeaderInfo.nTrxnAmount * 1.0 / Global.MULTIPLE_VALUE;
			}
		}
		Global.gCurPreviousBalance = dTotalRemaining;

		Global.strVisitStart = new SimpleDateFormat("kkmmss").format(new Date());
		Global.nSurveyCompleted = 0;
		Global.nAssetChecked = 0;
		Global.nAssetRequested = 0;
		Global.bOrderCreated = false;

		if(Utils.getStringSetting(mContext, Global.PREF_CON_EMPTY).equals("1"))
		{
			Global.gStockDateTime = 0;
			Global.gAdjustDateTime = 0;
		}

		//Check and enable hotspot
		boolean bCardPresent = !TextUtils.isEmpty(Utils.getStringSetting(mContext, Global.PREF_WINDCAVE_HITUSERNAME)) && !TextUtils.isEmpty(Utils.getStringSetting(mContext, Global.PREF_WINDCAVE_HITKEY));
		if(bCardPresent && Utils.getIntSetting(mContext, Global.PREF_EWAY_CARD_PROC) == 10 && Utils.getBooleanSetting(mContext, Global.PREF_HOTSPOT))
		{
			if(Global.hotspot == null)
			{
				Global.hotspot = new Hotspot(mContext);
			}

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				if (Settings.System.canWrite(mContext)) {
					Global.hotspot.start("DSD Android", "12345678");
					mContext.replaceFragment(new InstoreFragment());
				}
				else {
					Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
					intent.setData(Uri.parse("package:" + mContext.getPackageName()));
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivityForResult(intent, Global.WRITE_SETTINGS_PERMISSION);
				}
			}
			else
			{
				Global.hotspot.start("DSD Android", "12345678");
				mContext.replaceFragment(new InstoreFragment());
			}

		}
		else
		{
			mContext.replaceFragment(new InstoreFragment());
		}

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == Global.WRITE_SETTINGS_PERMISSION)
		{
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
					if (Settings.System.canWrite(mContext)) {
					Global.hotspot.start("DSD Android", "12345678");
					mContext.replaceFragment(new InstoreFragment());
				}
				else
				{
					Global.hotspot = null;
					mContext.replaceFragment(new InstoreFragment());
				}
			}
		}
	}

	private void showNonDeliveryDialog() {
		if(Global.gCurCustomerInfo == null)
		{
			Global.gCurCustomerInfo = mSelCustomerInfo;
		}
		try {
			final NonDeliveryDialog dlg = new NonDeliveryDialog(mContext);
			dlg.mType = Global.REASON_TYPE_NON_DELIVERY;
			dlg.setCancelable(false);
			dlg.setOnDismissListener(new OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
					if(Global.gCurCustomerInfo == null)
					{
						Global.gCurCustomerInfo = mSelCustomerInfo;
					}
					if (dlg.mReturnCode == Global.DLG_RETURN_RIGHT) {
						Global.gCurCustomerInfo.bCompleted = true;
						DbAdapter dbAdapter = new DbAdapter(mContext);
						dbAdapter.open();
						dbAdapter.updateCustomerInfo(Global.gCurCustomerInfo);
						dbAdapter.close();
						Global.gCurCustomerInfo = null;
						refreshPage();
					}
				}
			});
			dlg.show();
		}catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	private void showNonServiceDialog() {
		if(Global.gCurCustomerInfo == null)
		{
			Global.gCurCustomerInfo = mSelCustomerInfo;
		}
		try {
			final NonDeliveryDialog dlg = new NonDeliveryDialog(mContext);
			dlg.mType = Global.REASON_TYPE_NON_SERVICE;
			dlg.setCancelable(false);
			dlg.setOnDismissListener(new OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
					if(Global.gCurCustomerInfo == null)
					{
						Global.gCurCustomerInfo = mSelCustomerInfo;
					}
					if (dlg.mReturnCode == Global.DLG_RETURN_RIGHT) {
						Global.gCurCustomerInfo.bCompleted = true;
						DbAdapter dbAdapter = new DbAdapter(mContext);
						dbAdapter.open();
						dbAdapter.updateCustomerInfo(Global.gCurCustomerInfo);
						// UOrderStatus
						UOrderStatusHInfo uOrderStatusInfo = new UOrderStatusHInfo();
						uOrderStatusInfo.nTrxnNo = System.currentTimeMillis();
						uOrderStatusInfo.strDocNo = "";
						uOrderStatusInfo.strDocType = "IVUP";
						uOrderStatusInfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(mContext, new Date(uOrderStatusInfo.nTrxnNo)));
						uOrderStatusInfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(uOrderStatusInfo.nTrxnNo);
						UOrderStatusDInfo dInfo = new UOrderStatusDInfo();
						dInfo.nChainNo = Global.gCurCustomerInfo.nChainNo;
						dInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
						dInfo.nDayNo = Global.gCurCustomerInfo.nDayNo;
						dInfo.nStatus = 5;
						dInfo.strReference = "";
						//dInfo.strDeliveryDate = Global.gCurCustomerInfo.strDeliveryDate;
						dInfo.strDeliveryDate = uOrderStatusInfo.strTrxnDate;
						dInfo.nTrxnNo = uOrderStatusInfo.nTrxnNo;

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

						uOrderStatusInfo.uOrderStatusDInfoList.add(dInfo);
						dbAdapter.insertUOrderStatusInfo(uOrderStatusInfo);
						dbAdapter.close();
						MakeZipFileInfo oldMakeZipFile = makeZipFileInfo;
						makeZipFileInfo = null;
						//setUTransactionsInfo(Global.gCurCustomerInfo.nCustNo, Global.gCurCustomerInfo.nChainNo, uOrderStatusInfo.nTrxnNo, "IVUP", "");
						AddTrxnNo(Global.gCurCustomerInfo.nCustNo, Global.gCurCustomerInfo.nChainNo, uOrderStatusInfo.nTrxnNo);
						//Global.gUploadCustNoList.add(makeZipFileInfo);
						Utils.registerUploadFile(mContext, makeZipFileInfo);
						makeZipFileInfo = oldMakeZipFile;
						refreshPage();
					}
				}
			});
			dlg.show();
		}catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	private void navigateMeToStore() {

		//Check the Location is set on or not.
		if(ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
				ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
		{
			Toast.makeText(mContext, Str.LocationPermissionDenied[Global.LANG], Toast.LENGTH_LONG).show();
			return;
		}

		showProgressDialog("Getting current location...");

		LocationServices.getFusedLocationProviderClient(mContext).getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
			@Override
			public void onComplete(@NonNull Task<Location> task) {
				if (task.isSuccessful() && task.getResult() != null) {
					hideProgressDialog();
					Location location = task.getResult();
					doNavigateMetoStore(location);
				} else {
					hideProgressDialog();
					Toast.makeText(mContext, Str.FailedToGetCurrentLocation[Global.LANG], Toast.LENGTH_LONG).show();
				}
			}
		});
	}

	private void doNavigateMetoStore(Location curLocation)
	{
		if (Global.gCurCustomerInfo == null) {
			return;
		}

		double nSrcLat = curLocation.getLatitude();
		double nSrcLng = curLocation.getLongitude();
		double nDstLat;
		double nDstLng;

		if(Global.gCurCustomerInfo.driverLongitude == 0 || Global.gCurCustomerInfo.driverLatitude == 0)
		{
			nDstLat = Global.gCurCustomerInfo.latitude;
			nDstLng = Global.gCurCustomerInfo.longitude;
		}
		else
		{
			nDstLat = Global.gCurCustomerInfo.driverLatitude;
			nDstLng = Global.gCurCustomerInfo.driverLongitude;
		}

		if (nSrcLat == 0 || nSrcLng == 0) {
			Utils.printDebug(mContext, Str.Cant_get_the_location_info_of_this_device[Global.LANG]);
			return;
		}

		if (nDstLat == 0 || nDstLng == 0) {
			/*String strAddress = Global.gCurCustomerInfo.strAddress1;
			if(!TextUtils.isEmpty(Global.gCurCustomerInfo.strAddress2))
			{
				if(!TextUtils.isEmpty(strAddress))
				{
					strAddress += ", " + Global.gCurCustomerInfo.strAddress2;
				}
				else
				{
					strAddress = Global.gCurCustomerInfo.strAddress2;
				}
			}*/
			//String strDstAddress = Utils.getFullAddress(mContext, strAddress);
			String strDstAddress = Global.gCurCustomerInfo.strAddress1 + ", " + Global.gCurCustomerInfo.strCity + " " + Global.gCurCustomerInfo.strShipToState + " " + Global.gCurCustomerInfo.strShipToZip + ", Australia";
			if(strDstAddress != null)
			{
				String strSrcLat = String.format(Locale.ENGLISH, "%f", nSrcLat).replace(",", ".");
				String strSrcLng = String.format(Locale.ENGLISH, "%f", nSrcLng).replace(",", ".");
				String uri = "http://maps.google.com/maps?saddr=" + strSrcLat + "," + strSrcLng+ "&daddr=" + strDstAddress;
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
				startActivity(intent);
				return;
			}
			else
			{
				Utils.printDebug(mContext, Str.CantGetLocationForThisCustomer[Global.LANG]);
				return;
			}

		}
		else
		{
			String strSrcLat = String.format(Locale.ENGLISH, "%f", nSrcLat).replace(",", ".");
			String strSrcLng = String.format(Locale.ENGLISH, "%f", nSrcLng).replace(",", ".");
			String strDstLng = String.format(Locale.ENGLISH, "%f", nDstLng).replace(",", ".");
			String strDstLat = String.format(Locale.ENGLISH, "%f", nDstLat).replace(",", ".");
			String uri = "http://maps.google.com/maps?saddr=" + strSrcLat + "," + strSrcLng+ "&daddr=" + strDstLat + "," + strDstLng;

			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
			startActivity(intent);
		}
	}

	private void doEndTrip() {
		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		
		for (int i=0; i<mCustomerList.size(); i++) {
			CustomerInfo info = mCustomerList.get(i);
			info.nOrderNo = i;
			dbAdapter.updateCustomerInfo(info);
		}
		dbAdapter.close();
		Global.gEnteredEndTrip = true;

		MakeZipFileInfo oldMakeZipFileInfo = makeZipFileInfo;
		makeZipFileInfo = null;
		long nTrxnNo = setGeneralUploadData("EODS", 0, 0, -1);

		TransactionInfo transactionInfo = new TransactionInfo();
		transactionInfo.strType = "EODS";
		transactionInfo.strTime = new SimpleDateFormat("kk:mm").format(new Date(nTrxnNo));
		dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		dbAdapter.insertTransactionHistoryInfo(transactionInfo);

		nTrxnNo = setGeneralUploadData("VIS", 0, 0, -1, "EODS");
		//Upload the Visit Information
		UVisitInfo visitInfo = new UVisitInfo();
		visitInfo.nTrxnNo = nTrxnNo;
		visitInfo.strDocType = "VIS";
		visitInfo.nChainNo = 0;
		visitInfo.nCustNo = 0;
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

//		List<DescTypeInfo> descTypeInfoList = dbAdapter.getDescTypeList("VisitType");
//		visitInfo.strVisitTypeId = (descTypeInfoList != null && descTypeInfoList.size() > 0 ? descTypeInfoList.get(0).strDescriptionEntryKey : "0");

		dbAdapter.insertUVisitInfo(visitInfo);

		dbAdapter.close();

		UploadInfo uploadInfo = new UploadInfo();
		uploadInfo.makeZipFileInfo = makeZipFileInfo;
		Utils.uploadData(mContext, 0, uploadInfo, false);
		makeZipFileInfo = oldMakeZipFileInfo;

		mContext.replaceFragment(new TripSettlementFragment());
	}

	public void showEndTripDialog() {
		try {
			final MessageDialog msgDialog = new MessageDialog(mContext);
			msgDialog.setCancelable(false);
			msgDialog.mStrTitle = Str.EndTrip[Global.LANG].toUpperCase();
			msgDialog.mStrMessage = Str.EndTrip_msg[Global.LANG];
			msgDialog.mStrLeft = Str.Return[Global.LANG];
			msgDialog.mStrRight = Str.EndTrip[Global.LANG];
			msgDialog.setOnDismissListener(new OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
					if(Global.gCurCustomerInfo == null)
					{
						Global.gCurCustomerInfo = mSelCustomerInfo;
					}
					if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
	//					Global.gEnteredEndTrip = true;
						if (Utils.getStringSetting(mContext, Global.PREF_EOT_PIN).equals("1")) {
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
										String strSecurity8 = Utils.getStringSetting(mContext, Global.PREF_LOADREQUEST_APPROVAL_KEY);
										String strMFASecret = Utils.getStringSetting(mContext, Global.PREF_MFA_SECRET);
										String strPasscode = "";

										if (strMFASecret.length() > 0) {
											strPasscode = OTPUtils.generateTOTP(strMFASecret);
										}
										else {
											strPasscode = strSecurity8;
										}
										if (strPasscode == null || strPasscode.isEmpty()) {
											strPasscode = strSecurity8;
										}

										if (strPasscode.equals(inputDlg.mStrInputed)) {
											doEndTrip();
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
						}
						else {
							doEndTrip();
						}
					}
					else
					{
						Global.gCurCustomerInfo = null;
					}
				}
			});
			msgDialog.show();
		}catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	public boolean m_bClicked = false;

	public boolean getBackProcessed()
	{
		return m_bBackProcessed;
	}

	@Override
	public void onClick(View v) {
		if( m_bClicked ) return;
		m_bClicked = true;
		switch (v.getId()) {
			case R.id.btnSearch:
				Global.gCustomerList = mCustomerList;
				mContext.replaceFragment(new SearchCustomerFragment());
				break;
			case R.id.btnBack:
				if( Utils.getIntSetting(mContext, Global.PREF_LOADMULTIALL) > 0 )
				{
					if( m_bBackProcessed )
					{
						Global.bVisitAutoSelectCustomer = false;
						mContext.onBack();
					}
					else
					{
						SpecialLoadProcessWithApi();
					}
				}
				else
				{
					Global.bVisitAutoSelectCustomer = false;
					mContext.onBack();
				}
				break;
			case R.id.btnAddCustomer:
				Global.gCustomerList = mCustomerList;
				doAddCustomer();
				break;
			case R.id.btnEndTrip:
				showEndTripDialog();
				break;
			case R.id.btnRoutific:
				try {
					OptimizationDialog optimizationDialog = new OptimizationDialog(mContext, new OptimizationDialog.Callback() {
						@Override
						public void onSelect(LocationInfo startLocation, LocationInfo endLocation) {
							doRoutificClicked(startLocation, endLocation);
						}
					});
					optimizationDialog.show();
				}catch (Exception e)
				{
					e.printStackTrace();
				}
				break;
		}
		m_bClicked = false;
	}

	private void gotoNewCustomer(CustomerInfo info)
	{
		Fragment fragment = new NewCustomerFragment();
		Bundle bundle = new Bundle();
		bundle.putBoolean(Global.EXTRA_IS_EDIT_CUSTOMER, false);
		bundle.putSerializable(Global.EXTRA_CUSTOMER_INFO, info);
		fragment.setArguments(bundle);
		mContext.replaceFragment(fragment);
	}

	private int mSelCustomer = -1;
	private void doAddCustomer(){
		//Get the Customer Template
		if(!TextUtils.isEmpty(Utils.getStringSetting(mContext, Global.PREF_TEMPLATE_CHAIN)))
		{
			int nTemplateChainNo = 0;
			try {
				nTemplateChainNo = Integer.parseInt(Utils.getStringSetting(mContext, Global.PREF_TEMPLATE_CHAIN).trim());
				DbAdapter dbAdapter = new DbAdapter(mContext);
				dbAdapter.open();
				List<CustomerInfo> tempCustInfoList = dbAdapter.getCustomerInfoListWithChainNumber(nTemplateChainNo);
				dbAdapter.close();

				if(tempCustInfoList.size() == 0)
				{
					gotoNewCustomer(null);
				}
				else
				{
					try {
						new AlertDialog.Builder(mContext)
								.setTitle(Str.Select_Customer_Template[Global.LANG])
								.setSingleChoiceItems(new AlertSingleCustomerChoiceAdapter(mContext, tempCustInfoList), -1, (dialog, nSelected) -> mSelCustomer = nSelected)
								.setPositiveButton(Str.Ok[Global.LANG], (dialog, nSelected) -> {
									if(mSelCustomer != -1)
									{
										gotoNewCustomer(tempCustInfoList.get(mSelCustomer));
									}
									else
									{
										Toast.makeText(mContext, Str.PleaseSelectTemplate[Global.LANG], Toast.LENGTH_LONG).show();
									}
								})
								.setNegativeButton(Str.Cancel[Global.LANG], (dialog1, nSelected1) -> {})
								.create()
								.show();
					}catch (Exception e)
					{
						e.printStackTrace();
					}
				}

			}catch (Exception e)
			{
				gotoNewCustomer(null);
			}
		}
		else
		{
			gotoNewCustomer(null);
		}
	}

	private void doRoutificClicked(final LocationInfo startLocation, final LocationInfo endLocation)
	{
		if(!Utils.isConnectingToInternet(mContext))
		{
			Toast.makeText(mContext, Str.network_connection_error[Global.LANG], Toast.LENGTH_LONG).show();
			return;
		}

		//Check the Location is set on or not.
		if(ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
				ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
		{
			Toast.makeText(mContext, Str.LocationPermissionDenied[Global.LANG], Toast.LENGTH_LONG).show();
			return;
		}

		showProgressDialog("Getting current location...");

		LocationServices.getFusedLocationProviderClient(mContext).getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
			@Override
			public void onComplete(@NonNull Task<Location> task) {
				if (task.isSuccessful() && task.getResult() != null) {
					hideProgressDialog();
					Location location = task.getResult();
					doProcessRoutfic(startLocation, endLocation, location);
				} else {
					hideProgressDialog();
					Toast.makeText(mContext, Str.FailedToGetCurrentLocation[Global.LANG], Toast.LENGTH_LONG).show();
				}
			}
		});

	}

	private void doProcessRoutfic(final LocationInfo startLocation, final LocationInfo endLocation, Location curLocation)
	{
		Utils.setDoubleSetting(mContext, Global.PREF_MY_LATITUDE, curLocation.getLatitude());
		Utils.setDoubleSetting(mContext, Global.PREF_MY_LONGITUDE, curLocation.getLongitude());

		showProgressDialog(Str.Optimizing[Global.LANG]);
		new Thread()
		{
			@Override
			public void run() {
				Map<String, String> headers = new HashMap<>();
				headers.put("Authorization", "bearer " + Utils.getStringSetting(mContext, Global.PREF_ROUTIFICAPI));
				final JSONObject request = new JSONObject();
				JSONObject fleet = new JSONObject();
				//Add Fleet
				try
				{
					JSONObject vehicle = new JSONObject();

					SimpleDateFormat sdf1 = new SimpleDateFormat("kk:mm");
					Calendar c = Calendar.getInstance();
					c.setTime(new Date());
					c.add(Calendar.MINUTE, 2);
					//c.add(Calendar.HOUR, 5);
					Date dt = c.getTime();

					vehicle.put("shift_start", sdf1.format(dt));

					JSONObject end_location = new JSONObject();
					end_location.put("id", endLocation.locationName);
					end_location.put("lat",  endLocation.latitude);
					end_location.put("lng", endLocation.longitude);
					vehicle.put("end_location", end_location);

					JSONObject start_location = new JSONObject();
					if(startLocation.locationName.equals(Str.CurrentLocation[Global.LANG]))
					{
						start_location.put("id", Global.gTripNumber);
					}
					else
					{
						start_location.put("id", startLocation.locationName);
					}
					start_location.put("lat", startLocation.latitude);
					start_location.put("lng", startLocation.longitude);

					vehicle.put("start_location", start_location);

					vehicle.put("strict_start", true);
					fleet.put("vehicle_1", vehicle);
					request.put("fleet", fleet);

					//Commented by asking from Andrew... at 11th of July, 2022
//					JSONObject options = new JSONObject();
//					options.put("traffic", "predicted");
//					request.put("options", options);

					dt = new Date();

					/*c = Calendar.getInstance();
					c.setTime(dt);
					c.add(Calendar.DATE, 1);
					dt = c.getTime();*/

					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
					String today = sdf.format(dt);
					request.put("date", today);


				}catch (Exception e)
				{
					e.printStackTrace();
				}
				JSONObject visits = new JSONObject();
				//Set Visit Parameters
				int orderNo = 0;
				for(CustomerInfo customer : mCustomerList)
				{
					boolean bAddress = false;
					String strAddress = "";
					if( customer.latitude == 0 && customer.longitude == 0
							&& customer.driverLatitude == 0 && customer.driverLongitude == 0 )
					{
						/*strAddress = customer.strAddress1;
						if(!TextUtils.isEmpty(customer.strAddress2))
						{
							if(!TextUtils.isEmpty(strAddress))
							{
								strAddress += ", " + customer.strAddress2;
							}
							else
							{
								strAddress = customer.strAddress2;
							}
						}
						strAddress = Utils.getFullAddress(mContext, strAddress);*/
						strAddress = customer.strAddress1 + ", " + customer.strCity + " " + customer.strShipToState + " " + customer.strShipToZip + ", Australia";
						if(strAddress != null)
						{
							bAddress = true;
						}
						else
						{
							continue;
						}
					}

					orderNo++;
					JSONObject order = new JSONObject();
					JSONObject location = new JSONObject();
					try
					{
						location.put("name", customer.nChainNo + "/" + customer.nCustNo);
						if(bAddress)
						{
							location.put("address", strAddress);
						}
						else
						{
							if(customer.driverLatitude == 0 || customer.driverLongitude == 0)
							{
								location.put("lat", customer.latitude);
								location.put("lng", customer.longitude);
							}
							else
							{
								location.put("lat", customer.driverLatitude);
								location.put("lng", customer.driverLongitude);
							}
						}
						order.put("location", location);
					}catch (Exception e)
					{
						e.printStackTrace();
					}
					try
					{
						JSONArray time_windows = new JSONArray();
						if((!customer.strStartTime1.equals("0000") && !TextUtils.isEmpty(customer.strStartTime1)) && (!customer.strEndTime1.equals("0000") && !TextUtils.isEmpty(customer.strEndTime1)))
						{
							JSONObject time = new JSONObject();
							if(!customer.strStartTime1.contains(":"))
							{
								time.put("start", customer.strStartTime1.substring(0, 2) + ":" + customer.strStartTime1.substring(2, 4));
							}
							else
							{
								time.put("start", customer.strStartTime1);
							}
							if(!customer.strEndTime1.contains(":"))
							{
								time.put("end", customer.strEndTime1.substring(0, 2) + ":" + customer.strEndTime1.substring(2, 4));
							}
							else
							{
								time.put("end", customer.strEndTime1);
							}
							time_windows.put(time);
						}
						if((!customer.strStartTime2.equals("0000") && !TextUtils.isEmpty(customer.strStartTime2)) && (!customer.strEndTime2.equals("0000") && !TextUtils.isEmpty(customer.strEndTime2)))
						{
							JSONObject time = new JSONObject();
							if(!customer.strStartTime2.contains(":"))
							{
								time.put("start", customer.strStartTime2.substring(0, 2) + ":" + customer.strStartTime2.substring(2, 4));
							}
							else
							{
								time.put("start", customer.strStartTime2);
							}
							if(!customer.strEndTime2.contains(":"))
							{
								time.put("end", customer.strEndTime2.substring(0, 2) + ":" + customer.strEndTime2.substring(2, 4));
							}
							else
							{
								time.put("end", customer.strEndTime2);
							}
							time_windows.put(time);
						}
						if( time_windows.length() != 0)
						{
							order.put("time_windows", time_windows);
						}
					}catch (Exception e)
					{
						e.printStackTrace();
					}

					try
					{
						order.put("duration", Utils.getIntSetting(mContext, Global.PREF_VISITDURATION));
					}catch (Exception e)
					{
						e.printStackTrace();
					}

					try {
						visits.put("order_" + orderNo, order);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				try
				{
					request.put("visits", visits);
				}catch (Exception e)
				{
					e.printStackTrace();
				}
				res = null;

				try {
					final CountDownLatch latch = new CountDownLatch(1);
					//Utils.writeAsJson(request.toString(), true);
					RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), request.toString().getBytes());
					if(Utils.getStringSetting(mContext, Global.PREF_ROUTEFIC_LOG).equals("1"))
					{
						RoutificLogs.getInstance().WriteLogs("\napi url : https://api.routific.com/v1/vrp-long\n" + "method : " + "POST\n" + "Authorization : " + "bearer " + Utils.getStringSetting(mContext, Global.PREF_ROUTIFICAPI) +
								"\nContent-Type : application/json\n\n" + "Body : \n" + request.toString());
					}
					RoutificApiClient.getApiClient().vrp(  "bearer " + Utils.getStringSetting(mContext, Global.PREF_ROUTIFICAPI), requestBody).enqueue(new retrofit.Callback<RoutificJob>() {
						@Override
						public void onResponse(retrofit.Response<RoutificJob> response, Retrofit retrofit) {
							if(Utils.getStringSetting(mContext, Global.PREF_ROUTEFIC_LOG).equals("1"))
							{
								Gson gson = new Gson();
								RoutificLogs.getInstance().WriteLogs("\napi response:\n" + gson.toJson(response.body()));
							}

							res = response;
							latch.countDown();
						}

						@Override
						public void onFailure(Throwable t) {
							latch.countDown();
						}
					});
					latch.await();
				} catch (Exception e) {
					e.printStackTrace();
				}
				if(res == null )
				{
					new Handler(Looper.getMainLooper()).post(() -> {
						hideProgressDialog();
						if(!mContext.isFinishing() && !mContext.isDestroyed())
						{
							try {
								Toast.makeText(mContext, Str.FailedToCallRoutificApi[Global.LANG], Toast.LENGTH_LONG).show();
							}catch (Exception e1)
							{
								e1.printStackTrace();
							}
						}

					});
					return;
				}

				if(!res.isSuccess() || res.body() == null)
				{
					new Handler(Looper.getMainLooper()).post(() -> {
						hideProgressDialog();
						if( res.isSuccess() )
						{
							if(!mContext.isFinishing() && !mContext.isDestroyed())
							{
								try {
									Toast.makeText(mContext, Str.FailedToCallRoutificApi[Global.LANG], Toast.LENGTH_LONG).show();
								}catch (Exception e1)
								{
									e1.printStackTrace();
								}
							}

						}
						else
						{
							try {
								InputStream is = res.errorBody().byteStream();
								byte[] buffer = new byte[is.available()];
								is.read(buffer);
								String jsonResult = new String(buffer);
								JSONObject jsonObject = new JSONObject(jsonResult);
								String message = jsonObject.getString("error");
								if(!mContext.isFinishing() && !mContext.isDestroyed())
								{
									try {
										Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
									}catch (Exception e1)
									{
										e1.printStackTrace();
									}
								}

							} catch (Exception e) {
								e.printStackTrace();
								if(!mContext.isFinishing() && !mContext.isDestroyed())
								{
									try {
										Toast.makeText(mContext, Str.FailedToCallRoutificApi[Global.LANG], Toast.LENGTH_LONG).show();
									}catch (Exception e1)
									{
										e1.printStackTrace();
									}
								}

							}
						}

					});
					return;
				}
				try
				{
					bRes = false;
					RoutificJob job = res.body();
					while(bRes == false)
					{
						final CountDownLatch latch = new CountDownLatch(1);
						if(Utils.getStringSetting(mContext, Global.PREF_ROUTEFIC_LOG).equals("1"))
						{
							RoutificLogs.getInstance().WriteLogs("\napi url : https://api.routific.com/jobs/" + job.job_id + "\nmethod : GET");
						}
						RoutificApiClient.getApiClient().getJobStatus(job.job_id).enqueue(new retrofit.Callback<RoutificResponse>() {
							@Override
							public void onResponse(final retrofit.Response<RoutificResponse> response, Retrofit retrofit) {
								if(Utils.getStringSetting(mContext, Global.PREF_ROUTEFIC_LOG).equals("1"))
								{
									Gson gson = new Gson();
									RoutificLogs.getInstance().WriteLogs("\napi response : \n" + gson.toJson(response.body()));
								}
								if( response.isSuccess() && response.body() != null)
								{
									if(!response.body().status.equals("finished") )
									{
										bRes = false;
									}
									else
									{
										bRes = true;
										SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'kk:mm:ss.SSS'Z'");
										try {
											Date date = sdf.parse(response.body().createdAt);
											SimpleDateFormat sdf_date = new SimpleDateFormat("yyyyMMdd");
											SimpleDateFormat sdf_time = new SimpleDateFormat("kkmmss");
											strETADate = sdf_date.format(date);
											strETATime = sdf_time.format(date);
										} catch (ParseException e) {
											e.printStackTrace();
										}

										new Handler(Looper.getMainLooper()).post(new Runnable() {
											@Override
											public void run() {
												hideProgressDialog();
												RoutificResponse routificResponse = response.body();
												if( routificResponse.output == null )
												{
													if(!mContext.isFinishing() && !mContext.isDestroyed())
													{
														try {
															Toast.makeText(mContext, Str.FailedToCallRoutificApi[Global.LANG], Toast.LENGTH_LONG).show();
														}catch (Exception e1)
														{
															e1.printStackTrace();
														}
													}

												}
												else
												{
													if( !TextUtils.isEmpty(routificResponse.output.status) && routificResponse.output.status.equals("success"))
													{
														if( routificResponse.output.solution != null )
														{
															if( routificResponse.output.solution.vehicle_1 != null && routificResponse.output.solution.vehicle_1.size() > 0 )
															{
																optimizeSequence(routificResponse.output.solution.vehicle_1);
															}
															else
															{
																if(!mContext.isFinishing() && !mContext.isDestroyed())
																{
																	try {
																		Toast.makeText(mContext, Str.FailedToCallRoutificApi[Global.LANG], Toast.LENGTH_LONG).show();
																	}catch (Exception e1)
																	{
																		e1.printStackTrace();
																	}
																}

															}
														}
														else
														{
															if(!mContext.isFinishing() && !mContext.isDestroyed())
															{
																try {
																	Toast.makeText(mContext, Str.FailedToCallRoutificApi[Global.LANG], Toast.LENGTH_LONG).show();
																}catch (Exception e1)
																{
																	e1.printStackTrace();
																}
															}

														}
													}
													else
													{
														if(!mContext.isFinishing() && !mContext.isDestroyed())
														{
															try {
																Toast.makeText(mContext, Str.FailedToCallRoutificApi[Global.LANG], Toast.LENGTH_LONG).show();
															}catch (Exception e1)
															{
																e1.printStackTrace();
															}
														}

													}

												}
											}
										});
									}
								}
								else
								{
									bRes = true;
									new Handler(Looper.getMainLooper()).post(new Runnable() {
										@Override
										public void run() {
											hideProgressDialog();
											if( response.isSuccess() )
											{
												if(!mContext.isFinishing() && !mContext.isDestroyed())
												{
													try {
														Toast.makeText(mContext, Str.FailedToCallRoutificApi[Global.LANG], Toast.LENGTH_LONG).show();
													}catch (Exception e1)
													{
														e1.printStackTrace();
													}
												}

											}
											else
											{
												try {
													InputStream is = response.errorBody().byteStream();
													byte[] buffer = new byte[is.available()];
													is.read(buffer);
													String jsonResult = new String(buffer);
													JSONObject jsonObject = new JSONObject(jsonResult);
													String message = jsonObject.getString("error");
													if(!mContext.isFinishing() && !mContext.isDestroyed())
													{
														try {
															Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
														}catch (Exception e1)
														{
															e1.printStackTrace();
														}
													}

												} catch (Exception e) {
													e.printStackTrace();
													if(!mContext.isFinishing() && !mContext.isDestroyed())
													{
														try {
															Toast.makeText(mContext, Str.FailedToCallRoutificApi[Global.LANG], Toast.LENGTH_LONG).show();
														}catch (Exception e1)
														{
															e1.printStackTrace();
														}
													}

												}
											}

										}
									});
								}
								latch.countDown();
							}

							@Override
							public void onFailure(Throwable t) {
								new Handler(Looper.getMainLooper()).post(new Runnable() {
									@Override
									public void run() {
										hideProgressDialog();
										if(!mContext.isFinishing() && !mContext.isDestroyed())
										{
											try {
												Toast.makeText(mContext, Str.FailedToCallRoutificApi[Global.LANG], Toast.LENGTH_LONG).show();
											}catch (Exception e1)
											{
												e1.printStackTrace();
											}
										}

									}
								});
								bRes = true;
								latch.countDown();
							}
						});
						latch.await();
					}

					/*RoutificApiClient.getApiClient().getJobStatusAsText(job.job_id).enqueue(new Callback<ResponseBody>() {
						@Override
						public void onResponse(Response<ResponseBody> response, Retrofit retrofit) {
							try {
								Utils.writeAsJson(response.body().string(), false);
							} catch (IOException e) {
								e.printStackTrace();
							}
						}

						@Override
						public void onFailure(Throwable t) {

						}
					});*/
				}catch (Exception e)
				{
					e.printStackTrace();
				}

			}
		}.start();
	}

	private void optimizeSequence(List<RoutificLocation> locationList)
	{

		final Map<String, RoutificLocation> routificLocationMap = new HashMap<>();

		for(RoutificLocation location : locationList)
		{
			routificLocationMap.put(location.location_name, location);
		}
		for(CustomerInfo customerInfo : mCustomerList)
		{
			String key = customerInfo.nChainNo + "/" + customerInfo.nCustNo;
			RoutificLocation location = routificLocationMap.get(key);
			if( location != null )
			{
				customerInfo.strArrivalTime = location.arrival_time;
			}
			else if( customerInfo.latitude == 0 && customerInfo.longitude == 0
				&& customerInfo.driverLongitude == 0 && customerInfo.driverLatitude == 0)
			{
				customerInfo.strArrivalTime = "NO GPS";
			}
			else
			{
				customerInfo.strArrivalTime = "UNSCH";
			}
		}

		Collections.sort(mCustomerList, new Comparator<CustomerInfo>() {
			@Override
			public int compare(CustomerInfo t1, CustomerInfo t2) {
				int type1, type2 ;
				if(t1.strArrivalTime.equals("NO GPS") || t1.strArrivalTime.equals("UNSCH"))
				{
					type1 = 1;
				}
				else
				{
					type1 = 2;
				}

				if(t2.strArrivalTime.equals("NO GPS") || t2.strArrivalTime.equals("UNSCH"))
				{
					type2 = 1;
				}
				else
				{
					type2 = 2;
				}

				if(type1 == type2)
				{
					return t1.strArrivalTime.compareTo(t2.strArrivalTime);
				}
				else
				{
					return type1 - type2;
				}
			}
		});
		mDetailListAdapter.notifyDataSetChanged();
		mListAdapter.notifyDataSetChanged();
		long nTrxnNo = setGeneralUploadData("ROPT", 0, 0, -1);

		TransactionInfo transactionInfo = new TransactionInfo();
		transactionInfo.strType = "ROPT";
		transactionInfo.strTime = new SimpleDateFormat("kk:mm").format(new Date(nTrxnNo));
		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		dbAdapter.insertTransactionHistoryInfo(transactionInfo);
		dbAdapter.close();

		saveData();
		if(!mContext.isFinishing() && !mContext.isDestroyed())
		{
			try {
				Toast.makeText(mContext, Str.SuccessfullyOptimized[Global.LANG], Toast.LENGTH_LONG).show();
			}catch (Exception e1)
			{
				e1.printStackTrace();
			}
		}

	}

	private void saveData()
	{
		bModified = false;
		if( mCustomerList.size() > 0 )
		{
			DbAdapter dbAdapter = new DbAdapter(mContext);
			dbAdapter.open();

			URouteSchHeader header = new URouteSchHeader();
			header.nTrxnNo = System.currentTimeMillis();
			SimpleDateFormat sdf= new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT);
			header.strTrxnDate = sdf.format(Utils.getRoutectlTripDate(mContext, new Date(header.nTrxnNo)));
			sdf = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT);
			header.strTrxnTime = sdf.format(new Date(header.nTrxnNo));
			dbAdapter.insertURouteSchHeader(header);
			for (int i=0; i<mCustomerList.size(); i++) {
				CustomerInfo info = mCustomerList.get(i);
				info.nOrderNo = i;
				dbAdapter.updateCustomerInfo(info);
				URouteSchBody body = new URouteSchBody();
				body.nTrxnNo = header.nTrxnNo;
				body.nChainNo = info.nChainNo;
				body.nCustNo = info.nCustNo;
				body.nOrgSeqNo = info.nOrgSeqNo;
				body.nChangeSeqNo = i;
				body.strETADate = strETADate;
				if(strETATime != null && !strETATime.equals("") && !strETATime.equals("NO GPS"))
				{
					sdf = new SimpleDateFormat("kk:mm");
					try {
						Date date = sdf.parse(info.strArrivalTime);
						SimpleDateFormat sdf_time = new SimpleDateFormat("kkmmss");
						body.strETATime = sdf_time.format(date);
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}
				else
				{
					body.strETATime = "";
				}
				dbAdapter.insertURouteSchBody(body);
			}
			dbAdapter.close();
			AddTrxnNo(0, 0, header.nTrxnNo);
			//Global.gUploadCustNoList.add(makeZipFileInfo);
			Utils.registerUploadFile(mContext, makeZipFileInfo);

		}
	}

	public void SpecialLoadProcessWithApi()
	{
		final String strBaseUrl = Utils.getBaseURL(Utils.getStringSetting(mContext, Global.PREF_APICOMPANY));

		if(!TextUtils.isEmpty(strBaseUrl))
		{
			showProgressDialog(Str.checking_for_new_loads[Global.LANG]);
			//Call Api to investigate

			Map<String, String> params = new HashMap<>();
			params.put("username", Utils.getStringSetting(mContext, Global.PREF_APIUSER));
			params.put("password", Utils.getStringSetting(mContext, Global.PREF_APIPASSWORD));
			try {
				ConnectApiClient.getMainApiClient(strBaseUrl, null).login(params).enqueue(new retrofit2.Callback<LoginInfo>() {

					@Override
					public void onResponse(Call<LoginInfo> call, retrofit2.Response<LoginInfo> response) {
						if(response.isSuccessful() && response.body() != null)
						{
							final LoginInfo body = response.body();
							Map<String, String> headers = new HashMap<>();
							headers.put("X-Auth-Token", body.token);

							ConnectApiClient.getMainApiClient(strBaseUrl, headers).getLoad(Utils.getStringSetting(mContext, Global.PREF_TRIP_ID)).enqueue(new retrofit2.Callback<LoadTripResponse>() {
								@Override
								public void onResponse(Call<LoadTripResponse> call, retrofit2.Response<LoadTripResponse> response) {
									if(response.isSuccessful() && response.body() != null)
									{
										//Load Data
										new Thread()
										{
											@Override
											public void run() {
												List<LoadHead> loadHeadList = response.body().LoadHead;
												boolean bFoundNewLoad = false;
												if(loadHeadList.size() > 0)
												{
													DbAdapter dbAdapter = new DbAdapter(mContext);
													dbAdapter.open();

													for(LoadHead loadHead : loadHeadList)
													{
														LoadHeadInfo loadHeadInfo = dbAdapter.getLoadHeadInfoWithDetailFile(loadHead.DetailFile);
														if(loadHeadInfo == null)
														{
															loadHeadInfo = new LoadHeadInfo();
															loadHeadInfo.strLoadDate = loadHead.LoadDate;
															loadHeadInfo.nDayNo = loadHead.DayNo;
															loadHeadInfo.strType = loadHead.Type;
															loadHeadInfo.nPeriodNo = loadHead.PeriodNo;
															loadHeadInfo.strDescription = loadHead.Description;
															loadHeadInfo.strDetailFile = loadHead.DetailFile;
															loadHeadInfo.strRouteNumber = Global.gRouteNumber;
															loadHeadInfo.strTripNumber = Global.gTripNumber;
															dbAdapter.insertLoadHeadInfo(loadHeadInfo);

															for(LoadDetl loadDetl : loadHead.LoadDetl)
															{
																LoadDetlInfo info = new LoadDetlInfo();
																info.strDetailFile = loadDetl.DetailFile;
																info.strItemNo = loadDetl.ItemNo;

																String strNodeValue = String.valueOf(loadDetl.Qty);
																if(strNodeValue.length() > Utils.getIntSetting(mContext, Global.PREF_SCALING))
																{
																	info.nQty = Utils.getDividedValue(strNodeValue.substring(0, strNodeValue.length() - Utils.getIntSetting(mContext, Global.PREF_SCALING)));
																}
																else
																{
																	info.nQty = 0;
																}

																strNodeValue = String.valueOf(loadDetl.Weight);
																if(strNodeValue.length() > Utils.getIntSetting(mContext, Global.PREF_SCALING))
																{
																	info.dQty = Utils.getRoundedValue(Utils.getDividedDoubleValue(strNodeValue.substring(0, strNodeValue.length() - Utils.getIntSetting(mContext, Global.PREF_SCALING))));
																}
																else
																{
																	info.dQty = 0;
																}
																bFoundNewLoad = true;
																dbAdapter.insertLoadDetlInfo(info);
															}
														}
													}

													dbAdapter.close();
												}

												boolean finalBFoundNewLoad = bFoundNewLoad;
												new Handler(Looper.getMainLooper()).post(new Runnable() {
													@Override
													public void run() {
														hideProgressDialog();
														m_bBackProcessed = true;
														mRootView.findViewById(R.id.btnBack).performClick();
														Utils.setBooleanSetting(mContext, Global.PREF_FIRST_CHECK_LOAD, true);

														if(finalBFoundNewLoad)
														{
															Toast.makeText(mContext, Str.new_load_found[Global.LANG], Toast.LENGTH_LONG).show();
														}
													}
												});
											}
										}.start();
									}
									else
									{
										hideProgressDialog();
										m_bBackProcessed = true;
										mRootView.findViewById(R.id.btnBack).performClick();
										Utils.setBooleanSetting(mContext, Global.PREF_FIRST_CHECK_LOAD, true);
									}
								}

								@Override
								public void onFailure(Call<LoadTripResponse> call, Throwable t) {
									hideProgressDialog();
									m_bBackProcessed = true;
									mRootView.findViewById(R.id.btnBack).performClick();
									Utils.setBooleanSetting(mContext, Global.PREF_FIRST_CHECK_LOAD, true);
								}
							});
						}
						else
						{
							hideProgressDialog();
							m_bBackProcessed = true;
							mRootView.findViewById(R.id.btnBack).performClick();
							Utils.setBooleanSetting(mContext, Global.PREF_FIRST_CHECK_LOAD, true);
						}
					}

					@Override
					public void onFailure(Call<LoginInfo> call, Throwable t) {
						hideProgressDialog();
						m_bBackProcessed = true;
						mRootView.findViewById(R.id.btnBack).performClick();
						Utils.setBooleanSetting(mContext, Global.PREF_FIRST_CHECK_LOAD, true);
					}
				});
			}catch (Exception e)
			{
				e.printStackTrace();
				hideProgressDialog();
				m_bBackProcessed = true;
				mRootView.findViewById(R.id.btnBack).performClick();
				Utils.setBooleanSetting(mContext, Global.PREF_FIRST_CHECK_LOAD, true);
			}
		}
		else
		{
			m_bBackProcessed = true;
			mRootView.findViewById(R.id.btnBack).performClick();
			Utils.setBooleanSetting(mContext, Global.PREF_FIRST_CHECK_LOAD, true);
		}
	}

	public void SpecialLoadProcess()
	{
		showProgressDialog(Str.checking_for_new_loads[Global.LANG]);
		new Thread(){
			@Override
			public void run()
			{
				try
				{
					String strFTPServerIP = Utils.getStringSetting(mContext, Global.PREF_FTP_SERVER_IP);
					String strFTPCompanyName = Utils.getStringSetting(mContext, Global.PREF_FTP_COMPANY_NAME);
					String strFTPUserName = Utils.getStringSetting(mContext, Global.PREF_FTP_USERNAME);
					String strFTPPassword = Utils.getStringSetting(mContext, Global.PREF_FTP_PASSWORD);
					FTPClient ftpClient = null;
					ftpClient = Utils.connnectingwithFTP(strFTPServerIP, Global.DEFAULT_FTP_PORT, strFTPUserName, strFTPPassword);
					String strParentDir = String.format(Locale.ENGLISH, Global.FTP_SPECIAL_DIR, strFTPCompanyName);
					String strFileName = String.format(Locale.ENGLISH, "%s.zip", Global.gTripNumber);
					if (Utils.findFileOnFTPServer(ftpClient, strParentDir, strFileName) == 0) {
						new Handler(Looper.getMainLooper()).post(new Runnable() {
							@Override
							public void run() {
								Utils.printDebug(null, "Can't find the matched trip data.");
							}
						});
					}
					Utils.deleteDirectory(Global.DOWNLOAD_DIR);
					if (Utils.createDirectory(Global.DOWNLOAD_DIR) == false) {
						new Handler(Looper.getMainLooper()).post(new Runnable() {
							@Override
							public void run() {
								Utils.printDebug(null, "Can't create the download directory on SD Card.");
							}
						});
					}
					String strServerPath = String.format(Locale.ENGLISH, Global.FTP_SPECIAL_PATH, strFTPCompanyName, Global.gTripNumber);
					String strLocalPath = Global.DOWNLOAD_DIR + strFileName;
					if (Utils.downloadFileFromFTPServer(ftpClient, strServerPath, strLocalPath) == false) {
						new Handler(Looper.getMainLooper()).post(new Runnable() {
							@Override
							public void run() {
								Utils.printDebug(null, "Can't download the territory data.");
							}
						});
					}
					if (Utils.unzipData() == false) {
						new Handler(Looper.getMainLooper()).post(new Runnable() {
							@Override
							public void run() {
								Utils.printDebug(null, "Can't extract the data.");
							}
						});
					}
					if (ParseXMLFile.fillDataToDatabase(mContext, false) == 1) {
						new Handler(Looper.getMainLooper()).post(new Runnable() {
							@Override
							public void run() {
								Utils.printDebug(null, "Can't fill the data to database.");
							}
						});
					}
					else
					{
						MakeZipFileInfo oldMakeZipFileInfo = makeZipFileInfo;
						makeZipFileInfo = null;
						long nTrxnNo = setGeneralUploadData("DWNL", 0, 0, -1, "Special");

						TransactionInfo transactionInfo = new TransactionInfo();
						transactionInfo.strType = "DWNL";
						transactionInfo.strTime = new SimpleDateFormat("kk:mm").format(new Date(nTrxnNo));
						DbAdapter dbAdapter = new DbAdapter(mContext);
						dbAdapter.open();
						dbAdapter.insertTransactionHistoryInfo(transactionInfo);
						dbAdapter.close();

						//Global.gUploadCustNoList.add(makeZipFileInfo);
						Utils.registerUploadFile(mContext, makeZipFileInfo);
						makeZipFileInfo = oldMakeZipFileInfo;
					}
				}catch(Exception e)
				{
					e.printStackTrace();
					Utils.printDebug(null, e.getMessage());
				}

				new Handler(Looper.getMainLooper()).post(new Runnable() {
					@Override
					public void run() {
						hideProgressDialog();
						m_bBackProcessed = true;
						mRootView.findViewById(R.id.btnBack).performClick();
					}
				});
			}
		}.start();
	}

	private class ListAdapter extends BaseAdapter implements Swappable {

		private class ViewHolder {
			public TextView txtStartTime1, txtCustomerName;
			public ImageView imgMessage;
		}

		public int getCount() {
			return mCustomerList.size();
		}

		public CustomerInfo getItem(int position) {
			if(position < mCustomerList.size())
			{
				return mCustomerList.get(position);
			}
			else
			{
				return null;
			}
		}

		public long getItemId(int position) {
//			return -1;
			if( mCustomerList != null && mCustomerList.size() > 0 && position < mCustomerList.size() && position >= 0 )
			{
				return mCustomerList.get(position).nOrderNo;
			}
			else
			{
				return 0;
			}
		}

		public View getView(final int position, View convertView, ViewGroup parent) {
			View view = convertView;
			final ViewHolder holder;
			final CustomerInfo info = getItem(position);
			if(info == null) return null;
			if (convertView == null) {
				view = mContext.getLayoutInflater().inflate(R.layout.row_delivery_plan, parent, false);
				holder = new ViewHolder();
				holder.txtStartTime1 = (TextView) view.findViewById(R.id.txtStartTime1);
				holder.txtCustomerName = (TextView) view.findViewById(R.id.txtCustomerName);
				holder.imgMessage = (ImageView) view.findViewById(R.id.imgMessage);
				view.setTag(holder);
			} else {
				holder = (ViewHolder) view.getTag();
			}

			holder.txtStartTime1.setText(info.strArrivalTime);
			holder.txtCustomerName.setText(info.strName);
			if (info.bCompleted == true) {
				holder.txtCustomerName.setTextColor(mContext.getResources().getColor(R.color.green_color));
			} else {
				int nColor = 0;
				if(info.latestOrderInfo != null && info.latestOrderInfo.size() > 0)
				{
					nColor = 3;
				}
				else
				{
					nColor = Utils.getCustomerColorWithPresoldH(info.presoldOrHInfo);
				}

				if(info.nCreditHold != 0)
				{
					holder.txtCustomerName.setTextColor(mContext.getResources().getColor(R.color.red_color));
				}
				else if(nColor == 3)
				{
					holder.txtCustomerName.setTextColor(mContext.getResources().getColor(R.color.blue_color));
				}
				else if(nColor == 2)
				{
					holder.txtCustomerName.setTextColor(mContext.getResources().getColor(R.color.purple_text_color));
				}
				else if(nColor == 1)
				{
					holder.txtCustomerName.setTextColor(mContext.getResources().getColor(R.color.orange_text_color));
				}
				else if(info.strDeliveryType.equals("5"))
				{
					holder.txtCustomerName.setTextColor(mContext.getResources().getColor(R.color.orange_text_color));
				}
				else
				{
					holder.txtCustomerName.setTextColor(mContext.getResources().getColor(R.color.black_color));
				}
			}
			if (info.nMessageCnt > 0) {
				holder.imgMessage.setVisibility(View.VISIBLE);
			} else {
				holder.imgMessage.setVisibility(View.GONE);
			}

			holder.imgMessage.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Bundle bundle = new Bundle();
					bundle.putInt(Global.EXTRA_CUSTOMER_NO, info.nCustNo);
					bundle.putString(Global.EXTRA_CUSTOMER_NAME, info.strName);
					Fragment fragment = new MessageBoardFragment();
					fragment.setArguments(bundle);
					mContext.replaceFragment(fragment);
				}
			});

			return view;
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		@Override
		public void swapItems(int positionOne, int positionTwo) {
			Collections.swap(mCustomerList, positionOne, positionTwo);
			mDetailListAdapter.notifyDataSetChanged();
			strETATime = "";
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			strETADate = sdf.format(new Date());
		}
	}

	private class DetailListAdapter extends BaseAdapter implements Swappable{

		@Override
		public int getCount() {
			return mCustomerList.size();
		}

		@Override
		public Object getItem(int position) {
			if(position < mCustomerList.size())
			{
				return mCustomerList.get(position);
			}
			else
			{
				return null;
			}
		}

		@Override
		public long getItemId(int position) {
			return mCustomerList.get(position).nOrderNo;
		}

		@Override
		public View getView(final int position, View view, ViewGroup viewGroup) {
			if(view == null)
			{
				view = mContext.getLayoutInflater().inflate(R.layout.row_customer_detail_view, viewGroup, false);
			}
			view.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {

				}
			});

			String strDefaultTodayScreen = Utils.getStringSetting(mContext, Global.PREF_DEFAULT_TODAY_SCREEN);
			if (strDefaultTodayScreen.equals("1")) {
				view.findViewById(R.id.customer_detail).setVisibility(View.GONE);
			}

			CustomerInfo customerInfo = mCustomerList.get(position);
			if(customerInfo == null) return view;
			((TextView)view.findViewById(R.id.customer_name)).setText(customerInfo.strName);
			CustomerInfo info = (CustomerInfo)getItem(position);
			if (info.bCompleted == true) {
				((TextView)view.findViewById(R.id.customer_name)).setTextColor(mContext.getResources().getColor(R.color.green_color));
			} else {
				int nColor = 0;
				if(info.latestOrderInfo != null && info.latestOrderInfo.size() > 0)
				{
					nColor = 3;
				}
				else
				{
					nColor = Utils.getCustomerColorWithPresoldH(info.presoldOrHInfo);
				}
				if(info.nCreditHold > 0)
				{
					((TextView)view.findViewById(R.id.customer_name)).setTextColor(mContext.getResources().getColor(R.color.red_color));
				}
				else if(nColor == 3)
				{
					((TextView)view.findViewById(R.id.customer_name)).setTextColor(mContext.getResources().getColor(R.color.blue_color));
				}
				else if(nColor == 2)
				{
					((TextView)view.findViewById(R.id.customer_name)).setTextColor(mContext.getResources().getColor(R.color.purple_text_color));
				}
				else if(nColor == 1)
				{
					((TextView)view.findViewById(R.id.customer_name)).setTextColor(mContext.getResources().getColor(R.color.orange_text_color));
				}
				else if(info.strDeliveryType.equals("5"))
				{
					((TextView) view.findViewById(R.id.customer_name)).setTextColor(mContext.getResources().getColor(R.color.orange_text_color));
				}
				else
				{
					((TextView)view.findViewById(R.id.customer_name)).setTextColor(mContext.getResources().getColor(R.color.black_color));
				}
			}

			String strContent = "";
			if((!TextUtils.isEmpty(customerInfo.strStartTime1) && !TextUtils.isEmpty(customerInfo.strEndTime1) && !customerInfo.strStartTime1.equals("0000") && !customerInfo.strEndTime1.equals("0000"))
					|| (!TextUtils.isEmpty(customerInfo.strStartTime2) && !TextUtils.isEmpty(customerInfo.strEndTime2) && !customerInfo.strStartTime2.equals("0000") && !customerInfo.strEndTime2.equals("0000")))
			{
				strContent += "Delivery ";
				boolean bTime1Exist = false;
				if(!customerInfo.strStartTime1.equals("0000") && !customerInfo.strEndTime1.equals("0000"))
				{
					SimpleDateFormat sdf1 = new SimpleDateFormat("kk:mm");
					SimpleDateFormat sdf2 = new SimpleDateFormat("kkmm");
					SimpleDateFormat sdf_target = new SimpleDateFormat("h:mm a");

					String strStartTime = "";
					if(customerInfo.strStartTime1.contains(":"))
					{
						try
						{
							Date date = sdf1.parse(customerInfo.strStartTime1);
							strStartTime = sdf_target.format(date);
						}catch (Exception e)
						{
							e.printStackTrace();
						}
					}
					else
					{
						try
						{
							Date date = sdf2.parse(customerInfo.strStartTime1);
							strStartTime = sdf_target.format(date);
						}catch (Exception e)
						{
							e.printStackTrace();
						}
					}

					String strEndTime = "";
					if(customerInfo.strEndTime1.contains(":"))
					{
						try
						{
							Date date = sdf1.parse(customerInfo.strEndTime1);
							strEndTime = sdf_target.format(date);
						}catch (Exception e)
						{
							e.printStackTrace();
						}
					}
					else
					{
						try
						{
							Date date = sdf2.parse(customerInfo.strEndTime1);
							strEndTime = sdf_target.format(date);
						}catch (Exception e)
						{
							e.printStackTrace();
						}
					}
					if(!TextUtils.isEmpty(strStartTime) && !TextUtils.isEmpty(strEndTime))
					{
						strContent += strStartTime + " to " + strEndTime;
						bTime1Exist = true;
					}
				}
				if(!customerInfo.strStartTime2.equals("0000") || !customerInfo.strEndTime2.equals("0000"))
				{
					SimpleDateFormat sdf1 = new SimpleDateFormat("kk:mm");
					SimpleDateFormat sdf2 = new SimpleDateFormat("kkmm");
					SimpleDateFormat sdf_target = new SimpleDateFormat("h:mm a");

					String strStartTime = "";
					if(customerInfo.strStartTime2.contains(":"))
					{
						try
						{
							Date date = sdf1.parse(customerInfo.strStartTime2);
							strStartTime = sdf_target.format(date);
						}catch (Exception e)
						{
							e.printStackTrace();
						}
					}
					else
					{
						if(!TextUtils.isEmpty(customerInfo.strStartTime2))
						{
							try
							{
								Date date = sdf2.parse(customerInfo.strStartTime2);
								strStartTime = sdf_target.format(date);
							}catch (Exception e)
							{
								e.printStackTrace();
							}
						}
					}

					String strEndTime = "";
					if(customerInfo.strEndTime2.contains(":"))
					{
						try
						{
							Date date = sdf1.parse(customerInfo.strEndTime2);
							strEndTime = sdf_target.format(date);
						}catch (Exception e)
						{
							e.printStackTrace();
						}
					}
					else
					{
						if(!TextUtils.isEmpty(customerInfo.strEndTime2))
						{
							try
							{
								Date date = sdf2.parse(customerInfo.strEndTime2);
								strEndTime = sdf_target.format(date);
							}catch (Exception e)
							{
								e.printStackTrace();
							}
						}
					}
					if(!TextUtils.isEmpty(strStartTime) && !TextUtils.isEmpty(strEndTime))
					{
						if(bTime1Exist)
						{
							strContent += " or " +strStartTime + " to " + strEndTime;
						}
					}
				}
			}
			if(customerInfo.presoldOrHInfo != null && customerInfo.presoldOrHInfo.size() > 0)
			{
				for(int i = 0; i < customerInfo.presoldOrHInfo.size(); i++)
				{
					String strUser1 = customerInfo.presoldOrHInfo.get(i).strUser1;

					if(!TextUtils.isEmpty(strContent))
					{
						strContent += "\n";
					}
					strContent += "Order " + customerInfo.presoldOrHInfo.get(i).strOrderNo;
					if(strUser1.trim().length() > 0 && !strUser1.trim().equals("0"))
					{
						strContent += " for " + customerInfo.presoldOrHInfo.get(i).strUser1;
						if(!TextUtils.isEmpty(mCartonDescription))
						{
							int nValue = Utils.parseIntFromString(customerInfo.presoldOrHInfo.get(i).strUser1);
							if( nValue > 1 )
							{
								strContent += " " + mCartonDescription + "s";
							}
							else
							{
								strContent += " " + mCartonDescription;
							}
						}
					}
					if(customerInfo.presoldOrHInfo.get(i).strInstrs.trim().length() > 0)
					{
						strContent += "\n" + customerInfo.presoldOrHInfo.get(i).strInstrs;
					}
				}
			}

			if(!TextUtils.isEmpty(customerInfo.strCustMsg))
			{
				strContent += "\n" + customerInfo.strCustMsg;
			}
			((TextView)view.findViewById(R.id.customer_detail)).setText(strContent);

			view.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					doClickItem(position);
				}
			});

			view.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View view) {
					//mDetailListView.startDragging(position);
					Global.gCurCustomerInfo = mCustomerList.get(position);
					showNonServiceDialog();
					return true;
				}
			});
			return view;
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		@Override
		public void swapItems(int positionOne, int positionTwo) {
			Collections.swap(mCustomerList, positionOne, positionTwo);
			mListAdapter.notifyDataSetChanged();
		}
	}
}