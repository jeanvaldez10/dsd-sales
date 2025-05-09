package dsd.dsdvansales.fragment;

import java.text.SimpleDateFormat;
import java.util.*;

import android.app.*;
import android.content.*;
import android.os.Bundle;
import android.text.*;
import android.view.*;
import android.view.View.*;
import android.view.inputmethod.*;
import android.widget.*;

import dsd.dsdvansales.R;
import dsd.dsdvansales.db.DbAdapter;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.model.CustomerInfo;
import dsd.dsdvansales.model.DescTypeInfo;
import dsd.dsdvansales.model.UOrderStatusDInfo;
import dsd.dsdvansales.model.UOrderStatusHInfo;
import dsd.dsdvansales.model.URouteSchBody;
import dsd.dsdvansales.model.URouteSchHeader;
import dsd.dsdvansales.views.AlertSingleChoiceAdapter;


public class SearchCustomerFragment extends BaseFragment implements OnClickListener {

	private EditText mEditKeyword = null;
	private EditText mEditChainNo = null;
	private EditText mEditCustNo = null;

	private TextView mTxtCriteria = null;

	private TextView mTxtNoData = null;
	private ListView mListView = null;
	private ListAdapter mListAdapter = null;

	private List<CustomerInfo> mCustomerList = new ArrayList<>();
	private List<CustomerInfo> mSearchedList = new ArrayList<>();

	private String strSelectedSearchDistrict = "";
	private int mSearchByIdx = 0;
	private int mSalesDistrictIdx = -1;

	private String mStrKeyword = "";
	private String mSearchByArray[] = {"Customer Name", "Chain & Number", "Customer Postcode", "Customer Suburb", "Planned Delivery Day", "Sales District"};

	private static final int	SEARCH_BY_NAME				= 0;
	private static final int	SEARCH_BY_CUSTOMER_NUMBER	= 1;
	private static final int	SEARCH_BY_POSTCODE			= 2;
	private static final int	SEARCH_BY_SUBURB			= 3;
	private static final int	SEARCH_BY_PLANNED_DAY		= 4;
	private static final int	SEARCH_BY_SALES_DISTRICT 	= 5;

	private boolean bAdd = false;
	private Map<String, CustomerInfo> mWhiteCustomerMap = new HashMap<>();
	private List<DescTypeInfo> mSalesDistrictTypeList = new ArrayList<>();
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootView = inflater.inflate(R.layout.fragment_search_customer, container, false);
		commonInit();
		return mRootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		Global.gFragIdx = Global.FRAG_SEARCH_CUSTOMER;
	}

	@Override
	public void initView() {
		mRootView.findViewById(R.id.btnSearch).setOnClickListener(this);
		mRootView.findViewById(R.id.btnBack).setOnClickListener(this);
		mRootView.findViewById(R.id.txtCriteria).setOnClickListener(this);

		mEditKeyword = ((EditText) mRootView.findViewById(R.id.editKeyword));
		mEditChainNo = ((EditText) mRootView.findViewById(R.id.editChainNumber));
		mEditCustNo = ((EditText) mRootView.findViewById(R.id.editCustNumber));
		mTxtNoData = (TextView) mRootView.findViewById(R.id.txtNoData);

		mListView = (ListView) mRootView.findViewById(R.id.listView);
		mListAdapter = new ListAdapter();
		mListAdapter.setListData(mSearchedList);
		mListView.setAdapter(mListAdapter);

		mListView.setVisibility(View.GONE);
		mTxtNoData.setVisibility(View.VISIBLE);

		mEditKeyword.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				doSearch();
			}
		});

		mEditKeyword.setOnEditorActionListener(new EditText.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
					showKeyboard(mEditKeyword, false);
					return true;
				}
				return false;
			}
		});

		mEditCustNo.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				doSearch();
			}
		});

		mEditCustNo.setOnEditorActionListener(new EditText.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
					showKeyboard(mEditKeyword, false);
					return true;
				}
				return false;
			}
		});

		mEditChainNo.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				doSearch();
			}
		});

		mEditChainNo.setOnEditorActionListener(new EditText.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
					showKeyboard(mEditKeyword, false);
					return true;
				}
				return false;
			}
		});

		mRootView.findViewById(R.id.txtSalesDistrict).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				showSalesDistrictType();
			}
		});

		mTxtCriteria = (TextView) mRootView.findViewById(R.id.txtCriteria);
		mTxtCriteria.setText(mSearchByArray[mSearchByIdx]);

		loadData();
		refreshLanguage();
	}

	private void showSalesDistrictType()
	{
		final int origIdx = mSalesDistrictIdx;
		final String origText = ((TextView)(mRootView.findViewById(R.id.txtSalesDistrict))).getText().toString();
		String[] searchByArray = new String[mSalesDistrictTypeList.size()];
		for(int i = 0; i < mSalesDistrictTypeList.size(); i++){
			searchByArray[i] = mSalesDistrictTypeList.get(i).strDescription;
		}

		try {
			new AlertDialog.Builder(mContext)
					.setTitle(Str.SalesDistrict[Global.LANG])
					.setSingleChoiceItems(new AlertSingleChoiceAdapter(mContext, searchByArray), origIdx, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int nSelected) {
							mSalesDistrictIdx = nSelected;
							if(mSalesDistrictIdx != -1)
							{
								strSelectedSearchDistrict = String.valueOf(mSalesDistrictTypeList.get(mSalesDistrictIdx).nNumericKey);
							}
							else
							{
								strSelectedSearchDistrict = "";
							}
							((TextView)mRootView.findViewById(R.id.txtSalesDistrict)).setText(mSalesDistrictTypeList.get(nSelected).strDescription);
						}
					})
					.setPositiveButton(Str.Ok[Global.LANG], new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int nSelected) {
							doSearch();
						}
					})
					.setNegativeButton(Str.Cancel[Global.LANG], new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int nSelected) {
							mSalesDistrictIdx = origIdx;
							if(mSalesDistrictIdx != -1)
							{
								strSelectedSearchDistrict = String.valueOf(mSalesDistrictTypeList.get(mSalesDistrictIdx).nNumericKey);
							}
							else
							{
								strSelectedSearchDistrict = "";
							}
							((TextView)mRootView.findViewById(R.id.txtSalesDistrict)).setText(origText);
						}
					})
					.create()
					.show();
		}catch (Exception e)
		{
			e.printStackTrace();
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

	@Override
	public void loadData() {
		mCustomerList.clear();
		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		mCustomerList = dbAdapter.getUnPlanedCustomerList(Utils.getIntSetting(mContext, Global.DAY_NO));
		List<CustomerInfo> whiteCustomerList = filterSameCustomer(dbAdapter.getWhiteCustomerInfo(false, Utils.getIntSetting(mContext, Global.DAY_NO)));
		List<CustomerInfo> planedCustomerList = dbAdapter.getCustomerList(true, false, Utils.getIntSetting(mContext, Global.DAY_NO));
		List<DescTypeInfo> salesDistrictDescTypeList = dbAdapter.getDescTypeList(Global.SALES_DISTRICT);
        dbAdapter.close();

        mWhiteCustomerMap.clear();

        for(CustomerInfo customerInfo : whiteCustomerList)
        {
            mWhiteCustomerMap.put(customerInfo.nChainNo + "___" + customerInfo.nCustNo, customerInfo);
        }

        for(CustomerInfo customerInfo : planedCustomerList)
		{
			mWhiteCustomerMap.put(customerInfo.nChainNo + "___" + customerInfo.nCustNo, customerInfo);
		}

		//Filter Same Customer
		Map<String, CustomerInfo> customerInfoMap = new HashMap<>();
		List<CustomerInfo> newCustomerList = new ArrayList<>();
        for(CustomerInfo customerInfo : mCustomerList)
		{
			String key = customerInfo.nChainNo + "___" + customerInfo.nCustNo;
			if(mWhiteCustomerMap.get(key) != null)
			{
				continue;
			}
			if(customerInfoMap.get(key) == null)
			{
				customerInfoMap.put(key, customerInfo);
				newCustomerList.add(customerInfo);
			}
		}
		mCustomerList = newCustomerList;

        mSalesDistrictTypeList.clear();
        for(DescTypeInfo descTypeInfo : salesDistrictDescTypeList)
		{
			if(TextUtils.isEmpty(descTypeInfo.strValue1) || Utils.getStringSetting(mContext, Global.PREF_DEF_LOCN_NO).equals(descTypeInfo.strValue1))
			{
				mSalesDistrictTypeList.add(descTypeInfo);
			}
		}

		Collections.sort(mSalesDistrictTypeList, new Comparator<DescTypeInfo>() {
			@Override
			public int compare(DescTypeInfo descTypeInfo, DescTypeInfo t1) {
				return descTypeInfo.strDescription.compareTo(t1.strDescription);
			}
		});

		if (Global._DEBUG_ == true) {
			mSearchedList.clear();
			mSearchedList.addAll(mCustomerList);
		}
	}

	@Override
	public void refreshPage() {
		if (mSearchedList.size() > 0) {
			mListView.setVisibility(View.VISIBLE);
			mTxtNoData.setVisibility(View.GONE);
			mListAdapter.setListData(mSearchedList);
			mListAdapter.notifyDataSetChanged();
		} else {
			mListAdapter.setListData(new ArrayList<CustomerInfo>());
			mListAdapter.notifyDataSetChanged();
			mListView.setVisibility(View.GONE);
			mTxtNoData.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void refreshLanguage() {
		((EditText) mRootView.findViewById(R.id.editKeyword)).setHint(Str.Keyword[Global.LANG]);
		((TextView) mRootView.findViewById(R.id.txtSearchCustomer)).setText(Str.SEARCH_CUSTOMER[Global.LANG]);
		((TextView) mRootView.findViewById(R.id.txtSearchBy)).setText(Str.Search_by[Global.LANG]);
		mTxtNoData.setText(Str.No_data_has_been_found[Global.LANG]);

		mSearchByArray[0] = Str.Customer_Name[Global.LANG];
		mSearchByArray[1] = Str.Customer_Number_ChainNo[Global.LANG];
		mSearchByArray[2] = Str.Customer_Postcode[Global.LANG];
		mSearchByArray[3] = Str.Customer_Suburb[Global.LANG];
		mSearchByArray[4] = Str.Planned_Delivery_Day[Global.LANG];
		mSearchByArray[5] = Str.SalesDistrict[Global.LANG];
		mTxtCriteria.setText(mSearchByArray[mSearchByIdx]);

		refreshPage();
	}

	private void doSearch() {
		mSearchedList.clear();
		mStrKeyword = mEditKeyword.getText().toString().toLowerCase();
		if(mSearchByIdx == SEARCH_BY_NAME ||
				mSearchByIdx == SEARCH_BY_POSTCODE ||
				mSearchByIdx == SEARCH_BY_SUBURB ||
				mSearchByIdx == SEARCH_BY_PLANNED_DAY)
		{
			if (mStrKeyword.length() == 0) {
				Utils.printDebug(mContext, Str.Please_input_the_keyword[Global.LANG]);
				refreshPage();
				return;
			}
		}

		int nCustNo = Utils.parseIntFromString(mEditCustNo.getText().toString());
		int nChainNo = Utils.parseIntFromString(mEditChainNo.getText().toString());

		if(mSearchByIdx == SEARCH_BY_CUSTOMER_NUMBER)
		{
			if(nCustNo == 0)
			{
				Utils.printDebug(mContext, Str.Please_input_the_cust_no[Global.LANG]);
				refreshPage();
				return;
			}
		}


		for (CustomerInfo info : mCustomerList) {
		    if(mWhiteCustomerMap.get(info.nChainNo + "___" + info.nCustNo) != null) continue;
			if (mSearchByIdx == SEARCH_BY_NAME) {
				if (info.strName.toLowerCase().contains(mStrKeyword) == true) {
					mSearchedList.add(info);
				}
			} else if (mSearchByIdx == SEARCH_BY_POSTCODE) {
				if (info.strZip.toLowerCase().contains(mStrKeyword) == true) {
					mSearchedList.add(info);
				}
			} else if (mSearchByIdx == SEARCH_BY_SUBURB) {
				if (info.strCity.toLowerCase().contains(mStrKeyword) == true) {
					mSearchedList.add(info);
				}
			} else if(mSearchByIdx == SEARCH_BY_PLANNED_DAY){
				if(info.bIsRouteSch && info.nDayNo != 0)
				{
					String strWeekDate = Utils.getWeekDateName(info.nDayNo);
					if( strWeekDate.toLowerCase().contains(mStrKeyword.toLowerCase()) )
					{
						mSearchedList.add(info);
					}
				}
			} else if(mSearchByIdx == SEARCH_BY_SALES_DISTRICT){
				if(TextUtils.isEmpty(info.strSalesDistrict)) continue;
				if(TextUtils.isEmpty(strSelectedSearchDistrict) || info.strSalesDistrict.equals(strSelectedSearchDistrict))
				{
					mSearchedList.add(info);
				}

			} else if(mSearchByIdx == SEARCH_BY_CUSTOMER_NUMBER) {
				if(nChainNo == info.nChainNo && nCustNo == info.nCustNo)
				{
					mSearchedList.add(info);
				}
			}
		}
		refreshPage();
	}

	private Map<Integer, CustomerInfo> addMap = new HashMap<>();
	private void doAdd(CustomerInfo info) {
		if(!info.bIsRouteSch)
		{
			info.bIsRouteSch = true;
			info.bFromAddCustomer = true;
			addMap.put(info.nCustNo, info);
			info.nSeqNo = 0;
			info.strStartTime1 = "0000";
			info.strArrivalTime = info.strStartTime1;
			info.nDayNo = Utils.getIntSetting(mContext, Global.DAY_NO);
			if(Global.gCustomerList != null )
			{
				info.nOrderNo = 0;
				for(CustomerInfo otherInfo: Global.gCustomerList)
				{
					otherInfo.nOrderNo++;
				}
			}
			else
			{
				info.nOrderNo = 0;
			}
			Global.gCustomerList.add(0, info);

			if(info.strDeliveryWindowFrom.length() > 0 && !info.strDeliveryWindowFrom.equals("0000"))
			{
				info.strStartTime1 = info.strDeliveryWindowFrom;
				info.strArrivalTime = info.strStartTime1;
			}
			if(info.strDeliveryWindowTo.length() > 0 && !info.strDeliveryWindowTo.equals("0000"))
			{
				info.strEndTime1 = info.strDeliveryWindowTo;
			}

			DbAdapter dbAdapter = new DbAdapter(mContext);
			dbAdapter.open();
			int nCustomerCount = dbAdapter.getCustomerListCount(true);
			info.nOrgSeqNo = nCustomerCount;
			dbAdapter.updateCustomerInfo(info);
			dbAdapter.close();

			int nIdx = -1;
			for (int i=0; i<mCustomerList.size(); i++) {
				if (mCustomerList.get(i).nCustNo == info.nCustNo) {
					nIdx = i;
					break;
				}
			}
			if (nIdx != -1) {
				mCustomerList.remove(nIdx);
			}
			mSearchedList.remove(info);
		}
		else
		{
			Global.gCustomerList.add(0, info);
            DbAdapter dbAdapter = new DbAdapter(mContext);
            dbAdapter.open();
            info.nSeqNo = 0;
			if(Global.gCustomerList != null )
			{
				info.nOrderNo = 0;
				for(CustomerInfo otherInfo: Global.gCustomerList)
				{
					otherInfo.nOrderNo++;
				}
			}
			else
			{
				info.nOrderNo = 0;
			}
			info.nPeriodNo = 0;
			info.strDeliveryDate = "";
			dbAdapter.resetSequNoForCustomer(info);
            dbAdapter.registerWhiteCustomer(info.nChainNo, info.nCustNo, Utils.getIntSetting(mContext, Global.DAY_NO));
			addMap.put(info.nCustNo, info);
			dbAdapter.updateCustomerInfo(info);
            dbAdapter.close();

			int nIdx = -1;
			for (int i=0; i<mCustomerList.size(); i++) {
				if (mCustomerList.get(i).nCustNo == info.nCustNo) {
					nIdx = i;
					break;
				}
			}
			if (nIdx != -1) {
				mCustomerList.remove(nIdx);
			}
			mSearchedList.remove(info);
		}

		refreshPage();
		bAdd = true;
	}

	private void showCriteriaType() {
		final int origIdx = mSearchByIdx;
		final String origText = mTxtCriteria.getText().toString();

		try {
			new AlertDialog.Builder(mContext)
					.setTitle(Str.Search_by[Global.LANG])
					.setSingleChoiceItems(new AlertSingleChoiceAdapter(mContext, mSearchByArray), origIdx, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int nSelected) {
							mSearchByIdx = nSelected;
							mTxtCriteria.setText(mSearchByArray[nSelected]);
						}
					})
					.setPositiveButton(Str.Ok[Global.LANG], new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int nSelected) {
							doRefreshKeywordSection();
							doSearch();
						}
					})
					.setNegativeButton(Str.Cancel[Global.LANG], new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int nSelected) {
							mSearchByIdx = origIdx;
							mTxtCriteria.setText(origText);
						}
					})
					.create()
					.show();
		}catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	private void doRefreshKeywordSection()
	{
		if(mSearchByIdx == SEARCH_BY_SALES_DISTRICT)
		{
			mRootView.findViewById(R.id.txtSalesDistrict).setVisibility(View.VISIBLE);
			mRootView.findViewById(R.id.editKeyword).setVisibility(View.INVISIBLE);
			mRootView.findViewById(R.id.layoutCustomerNo).setVisibility(View.INVISIBLE);
			mSalesDistrictIdx = -1;
			strSelectedSearchDistrict = "";
			((TextView)mRootView.findViewById(R.id.txtSalesDistrict)).setText(Str.SelectSalesDistrict[Global.LANG]);
			mEditKeyword.setText("");
			mEditChainNo.setText("0");
			mEditCustNo.setText("");
		}
		else if(mSearchByIdx == SEARCH_BY_CUSTOMER_NUMBER)
		{
			mRootView.findViewById(R.id.txtSalesDistrict).setVisibility(View.INVISIBLE);
			mRootView.findViewById(R.id.editKeyword).setVisibility(View.INVISIBLE);
			mRootView.findViewById(R.id.layoutCustomerNo).setVisibility(View.VISIBLE);
			mSalesDistrictIdx = -1;
			strSelectedSearchDistrict = "";
			((TextView)mRootView.findViewById(R.id.txtSalesDistrict)).setText(Str.SelectSalesDistrict[Global.LANG]);
			mEditKeyword.setText("");
			mEditChainNo.setText("0");
			mEditCustNo.setText("");
		}
		else
		{
			mRootView.findViewById(R.id.txtSalesDistrict).setVisibility(View.INVISIBLE);
			mRootView.findViewById(R.id.editKeyword).setVisibility(View.VISIBLE);
			mRootView.findViewById(R.id.layoutCustomerNo).setVisibility(View.INVISIBLE);
			mSalesDistrictIdx = -1;
			strSelectedSearchDistrict = "";
			((TextView)mRootView.findViewById(R.id.txtSalesDistrict)).setText(Str.SelectSalesDistrict[Global.LANG]);
			mEditKeyword.setText("");
			mEditChainNo.setText("0");
			mEditCustNo.setText("");
		}
	}

	@Override
	public void receiveBroadcast(String strAction, Intent intent) {
	}

	public boolean m_bClicked = false;
	@Override
	public void onClick(View v) {
		if( m_bClicked ) return;
		m_bClicked = true;
		switch (v.getId()) {
			case R.id.btnSearch:
				doSearch();
				break;
			case R.id.btnBack:
				if(bAdd)
				{
					saveData();
				}
				mContext.onBackPressed();
				break;
			case R.id.txtCriteria:
				showCriteriaType();
				break;
		}
		m_bClicked = false;
	}

	private void saveData()
	{
		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
        mCustomerList = filterSameCustomer(dbAdapter.getWhiteCustomerInfo(false, Utils.getIntSetting(mContext, Global.DAY_NO)));
        mCustomerList.addAll(dbAdapter.getCustomerList(true, false, Utils.getIntSetting(mContext, Global.DAY_NO)));

		if( mCustomerList.size() > 0 )
		{
			URouteSchHeader header = new URouteSchHeader();
			header.nTrxnNo = System.currentTimeMillis();
			SimpleDateFormat sdf= new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT);
			header.strTrxnDate = sdf.format(new Date(header.nTrxnNo));
			sdf = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT);
			header.strTrxnTime = sdf.format(new Date(header.nTrxnNo));
			dbAdapter.insertURouteSchHeader(header);

			UOrderStatusHInfo uOrderStatusInfo = new UOrderStatusHInfo();
			uOrderStatusInfo.nTrxnNo = header.nTrxnNo;
			uOrderStatusInfo.strDocNo = "";
			uOrderStatusInfo.strDocType = "IVUP";
			uOrderStatusInfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(header.nTrxnNo);
			uOrderStatusInfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(Utils.getRoutectlTripDate(mContext, new Date(header.nTrxnNo)));

			for (int i=0; i<mCustomerList.size(); i++) {
				CustomerInfo info = mCustomerList.get(i);
				info.nOrderNo = i;
				URouteSchBody body = new URouteSchBody();
				body.nTrxnNo = header.nTrxnNo;
				body.nChainNo = info.nChainNo;
				body.nCustNo = info.nCustNo;
				body.nOrgSeqNo = info.nOrgSeqNo;
				body.nChangeSeqNo = i;
				SimpleDateFormat sdf_date = new SimpleDateFormat("yyyyMMdd");
				body.strETADate = sdf_date.format(new Date());
				body.strETATime = "";
				dbAdapter.insertURouteSchBody(body);

				if(addMap.get(info.nCustNo) != null)
				{
					UOrderStatusDInfo dInfo = new UOrderStatusDInfo();
					dInfo.nChainNo = info.nChainNo;
					dInfo.nCustNo = info.nCustNo;
					dInfo.nDayNo = info.nDayNo;
					dInfo.nStatus = 6;
					dInfo.strReference = "";
					dInfo.strDeliveryDate = uOrderStatusInfo.strTrxnDate;
					dInfo.nTrxnNo = header.nTrxnNo;
					info.presoldOrHInfo = dbAdapter.getPresoldOrHInfo(info.nChainNo, info.nCustNo, info.nPeriodNo, info.nSeqNo);
					info.latestOrderInfo= dbAdapter.getCustomerLatestOrderList(info.nCustNo, info.nChainNo);
					if(info.presoldOrHInfo != null && info.presoldOrHInfo.size() > 0)
					{
						dInfo.strOrderNo = info.presoldOrHInfo.get(0).strOrderNo;
						dInfo.nDeliverySequence = info.presoldOrHInfo.get(0).nDeliverySequence;
						dInfo.strOrderBarcode = info.presoldOrHInfo.get(0).strOrderBarCode;
						dInfo.strPORef = info.presoldOrHInfo.get(0).strPORef;
					}
					else if(info.latestOrderInfo != null && info.latestOrderInfo.size() > 0)
					{
						dInfo.strOrderNo = String.valueOf(info.latestOrderInfo.get(0).orderNumber);
						dInfo.strPORef = info.latestOrderInfo.get(0).purchaseOrderNumber;
					}
					uOrderStatusInfo.uOrderStatusDInfoList.add(dInfo);
				}
			}
			dbAdapter.insertUOrderStatusInfo(uOrderStatusInfo);
			AddTrxnNo(0, 0, header.nTrxnNo);
			//Global.gUploadCustNoList.add(makeZipFileInfo);
			Utils.registerUploadFile(mContext, makeZipFileInfo);
		}
		dbAdapter.close();
	}

	private class ListAdapter extends BaseAdapter {

		private List<CustomerInfo> customerList;

		private class ViewHolder {
			public TextView txtCustomerName;
			public TextView txtCity;
			public TextView txtZip;
			public TextView txtCustomerNo;
			public Button btnAdd;
		}

		public int getCount() {
			return customerList.size();
		}

		public CustomerInfo getItem(int position) {
			if(position < customerList.size())
			{
				return customerList.get(position);
			}
			else
			{
				return null;
			}
		}

		public long getItemId(int position) {
			return position;
		}
		
		public void setListData(List<CustomerInfo> customerList) {
			this.customerList = customerList;
		}

		public View getView(final int position, View convertView, ViewGroup parent) {
			View view = convertView;
			final ViewHolder holder;
			final CustomerInfo info = this.getItem(position);
			if(info == null) return null;

			if (convertView == null) {
				view = mContext.getLayoutInflater().inflate(R.layout.row_search_customer, parent, false);
				holder = new ViewHolder();
				holder.txtCustomerName = (TextView) view.findViewById(R.id.txtCustomerName);
				holder.txtCity = (TextView) view.findViewById(R.id.txtCity);
				holder.txtZip = (TextView) view.findViewById(R.id.txtZip);
				holder.txtCustomerNo = (TextView) view.findViewById(R.id.txtCustomerNo);
				holder.btnAdd = (Button) view.findViewById(R.id.btnAdd);
				view.setTag(holder);
			} else {
				holder = (ViewHolder) view.getTag();
			}

			holder.btnAdd.setText(Str.Add[Global.LANG]);

			if (mSearchByIdx == SEARCH_BY_NAME) {
				holder.txtCustomerName.setText(Utils.setKeyword(info.strName, mStrKeyword));
			} else {
				holder.txtCustomerName.setText(info.strName);
			}

			if (mSearchByIdx == SEARCH_BY_SUBURB) {
				holder.txtCity.setText(TextUtils.concat(Str.City_[Global.LANG], Utils.setKeyword(info.strCity, mStrKeyword)));
			} else {
				holder.txtCity.setText(Str.City_[Global.LANG] + info.strCity);
			}

			if (mSearchByIdx == SEARCH_BY_POSTCODE) {
				holder.txtZip.setText(TextUtils.concat(Str.Zip_[Global.LANG], Utils.setKeyword(info.strZip, mStrKeyword)));
			} else {
				holder.txtZip.setText(Str.Zip_[Global.LANG] + info.strZip);
			}

			if(info.nChainNo != 0)
			{
				holder.txtCustomerNo.setText(Str.Customer_[Global.LANG] + info.nChainNo + "/" + info.nCustNo);
			}
			else
			{
				holder.txtCustomerNo.setText(Str.Customer_[Global.LANG] + info.nCustNo);
			}

			holder.btnAdd.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					doAdd(info);
				}
			});


			return view;
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		//Save Current Order to DB.
		for(CustomerInfo info : Global.gCustomerList )
		{
			dbAdapter.updateCustomerInfo(info);
		}
		dbAdapter.close();
	}
}