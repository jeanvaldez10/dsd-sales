package dsd.dsdvansales.fragment;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import dsd.dsdvansales.R;
import dsd.dsdvansales.activities.BarcodeScannerActivity;
import dsd.dsdvansales.db.DbAdapter;
import dsd.dsdvansales.dialogs.BatchCodeInputDialog;
import dsd.dsdvansales.dialogs.InputBatchNumAllDialog;
import dsd.dsdvansales.dialogs.InputBatchNumDialog;
import dsd.dsdvansales.dialogs.MessageDialog;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.model.DescTypeInfo;
import dsd.dsdvansales.model.LoadBlotInfo;
import dsd.dsdvansales.model.PresoldOrDInfo;
import dsd.dsdvansales.model.ProdDetlInfo;
import dsd.dsdvansales.model.ProdInfo;
import dsd.dsdvansales.model.ProdLocnInfo;
import dsd.dsdvansales.model.ProdStruInfo;
import dsd.dsdvansales.model.UInvenDInfo;
import dsd.dsdvansales.model.UInvenHInfo;
import dsd.dsdvansales.views.AlertSingleChoiceAdapter;

public class LoadTruckCustomerOrderDetailFragment extends BaseFragment implements View.OnClickListener {

	private TextView mTxtNoData = null;
	private ListView mListView = null;
	private ListAdapter mListAdapter = null;
	private List<UInvenDInfo> mUInvenDList = new ArrayList<>();
	private List<ProdDetlInfo> mProdDetlList = new ArrayList<>();
	private Map<String, ProdDetlInfo> mProdDetlItemMap = new HashMap<>();
	private Map<String, ProdDetlInfo> mProdDetlUPCMap = new HashMap<>();
	private Map<String, ProdInfo> mProdInfoMap = new HashMap<>();

	private ProdDetlInfo mProdDetlSelInfo = null;
	private String m_nCurrentDetailFile = "";
	private String mStrCurrentCustomerName = "";
	private String mStrCurrentCustomerStartTime = "";

	private String strCartonDescription = "";
	private String mEntryInputMethod = "";

	private UInvenDInfo mCurSelUInvenDInfo = null;

	private String sortFlag = "";


	private void setSortFlag(String flag)
	{
		if(TextUtils.isEmpty(flag))
		{
			sortFlag = "code";
		}
		else if(flag.equalsIgnoreCase("ProdGroup"))
		{
			sortFlag = "group";
		}
		else if(flag.equalsIgnoreCase("MarketGroup"))
		{
			sortFlag = "market_group";
		}
		else if(flag.equalsIgnoreCase("PromoGroup"))
		{
			sortFlag = "promo_group";
		}
		else if(flag.equalsIgnoreCase("ItemType"))
		{
			sortFlag = "item_type";
		}
		else if(flag.equalsIgnoreCase("ItemUPC"))
		{
			sortFlag = "upc";
		}
		else if(flag.equalsIgnoreCase("Description"))
		{
			sortFlag = "desc";
		}
		else if(flag.equalsIgnoreCase("ShortDesc"))
		{
			sortFlag = "short_desc";
		}
		else if(flag.equalsIgnoreCase("Brand"))
		{
			sortFlag = "brand";
		}
		else
		{
			sortFlag = "code";
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootView = inflater.inflate(R.layout.fragment_load_truck_customer_order_detail, container, false);
		mStrCurrentCustomerStartTime = getArguments().getString(Global.KEY_CUSTOMER_TIME);
		m_nCurrentDetailFile = this.getArguments().getString(Global.KEY_DETAILFILE);
		mStrCurrentCustomerName = getArguments().getString(Global.KEY_CUSTOMER_NAME);
		commonInit();
		if(TextUtils.isEmpty(sortFlag))
		{
			sortFlag = Utils.getStringSetting(mContext, Global.PREF_UI_DETL_SORT);
			if(!Utils.isValidSortValue(sortFlag))
			{
				sortFlag = "code";
			}
			new Handler(Looper.getMainLooper()).post(new Runnable() {
				@Override
				public void run() {
					refreshPage();
				}
			});
		}
		return mRootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		Global.gFragIdx = Global.FRAG_LOAD_TRUCK_CUSTOMER_ORDER_DETAIL;
	}

	@Override
	public void onPause() {
		super.onPause();
		if (Global.gUInvenHinfo != null) {
//			Global.gUInvenHinfo.truckList.clear();
			for (UInvenDInfo info : mUInvenDList) {
				boolean bNew = true;
				UInvenDInfo oInfo = null;
				for( UInvenDInfo oldInfo : Global.gUInvenHinfo.truckList )
				{
					if( oldInfo.strDetailFile.equals(info.strDetailFile) && oldInfo.strItemNo.equals(info.strItemNo) && oldInfo.strStartTime.equals(this.mStrCurrentCustomerStartTime) )
					{
						bNew = false;
						oInfo = oldInfo;
						break;
					}
				}
				if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
				{
					info.dLoadAdj = info.dStock - info.dLoaded;
				}
				else
				{
					info.nLoadAdj = info.nStock - info.nLoaded;
				}
				if( bNew )
					Global.gUInvenHinfo.truckList.add(info);
				else
				{
					Global.gUInvenHinfo.truckList.remove(oInfo);
					Global.gUInvenHinfo.truckList.add(info);
				}
			}
		}
	}

	@Override
	public void initView() {
		mRootView.findViewById(R.id.btnAdd).setOnClickListener(this);
		mRootView.findViewById(R.id.btnBarcodeScanner).setOnClickListener(this);
		mRootView.findViewById(R.id.btnProductSearch).setOnClickListener(this);
		mRootView.findViewById(R.id.btnBack).setOnClickListener(this);
		mRootView.findViewById(R.id.btnConfirm).setOnClickListener(this);
		mRootView.findViewById(R.id.btnSort).setOnClickListener(this);
		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		List<ProdStruInfo> prodStruList = dbAdapter.getProdStruList();
		List<DescTypeInfo> descTypeInfoList = dbAdapter.getDescTypeList(Global.UNITOFMEASURE_DESC_TYPE_ID);
		dbAdapter.close();

		strCartonDescription = "";
		for(DescTypeInfo descTypeInfo : descTypeInfoList)
		{
			if( descTypeInfo.strAlphaKey.toUpperCase().equals("Z") )
			{
				if(TextUtils.isEmpty(strCartonDescription))
					strCartonDescription = descTypeInfo.strDescription;
				else
					strCartonDescription += "\n" + descTypeInfo.strDescription;
			}
		}

		if( prodStruList == null || prodStruList.size() == 0 )
		{
			mRootView.findViewById(R.id.btnProductTreeList).setEnabled(false);
			mRootView.findViewById(R.id.btnProductTreeList).setOnClickListener(null);
			mRootView.findViewById(R.id.btnProductTreeList).setBackground(mContext.getResources().getDrawable(R.drawable.btn_grey_large));
		}
		else
		{

			mRootView.findViewById(R.id.btnProductTreeList).setOnClickListener(this);
			mRootView.findViewById(R.id.btnProductTreeList).setEnabled(true);
			mRootView.findViewById(R.id.btnProductTreeList).setBackground(mContext.getResources().getDrawable(R.drawable.btn_white_medium));
		}

		mTxtNoData = (TextView) mRootView.findViewById(R.id.txtNoData);
		mListView = (ListView) mRootView.findViewById(R.id.listView);
		mListAdapter = new ListAdapter();
		mListView.setAdapter(mListAdapter);
		mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				if (firstVisibleItem + visibleItemCount >= totalItemCount) {
					mRootView.findViewById(R.id.txtMore).setVisibility(View.GONE);
				} else {
					mRootView.findViewById(R.id.txtMore).setVisibility(View.VISIBLE);
				}
			}

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
			}
		});

		((EditText) mRootView.findViewById(R.id.editItemNo)).addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				String strItemCode = getItemCode(s.toString());
				String strItemNo = strItemCode;
				ProdDetlInfo prodDetlInfo = mProdDetlUPCMap.get(strItemCode);
				if(prodDetlInfo != null)
				{
					strItemNo = prodDetlInfo.strItemNo;
				}

				if(strItemNo.length() == s.toString().length())
				{
					doSearch(false);
				}
				else
				{
					doProcessSelectProduct(strItemNo, 0,  false, false);
				}
			}
		});

		((EditText) mRootView.findViewById(R.id.editQty)).addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				if(mRootView.findViewById(R.id.editQty).getTag() == null)
				{
					setQtyFormat();
				}
			}
		});

		((EditText) mRootView.findViewById(R.id.editQty)).setOnEditorActionListener(new EditText.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					doAdd();
					return true;
				}
				return false;
			}
		});
		mEntryInputMethod = Utils.getStringSetting(mContext, Global.PREF_QTY_ENTRY_METHOD);
		if(mEntryInputMethod.equals("A"))
		{
			((EditText)mRootView.findViewById(R.id.editQty)).setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
		}
		else
		{
			((EditText)mRootView.findViewById(R.id.editQty)).setInputType(InputType.TYPE_CLASS_NUMBER);
		}
		loadData();
		refreshLanguage();
	}

	private String getItemCode(String strCode)
	{
		String strPrefix = Utils.getStringSetting(mContext, Global.PREF_BARCODE_PREFIX);
		String strSuffix = Utils.getStringSetting(mContext, Global.PREF_BARCODE_SUFFIX);
		if(!TextUtils.isEmpty(strPrefix))
		{
			String[] prefixArray = strPrefix.split(",");
			for(int i = 0; i < prefixArray.length; i++)
			{
				if(strCode.startsWith(prefixArray[i].trim()))
				{
					strCode = strCode.substring(prefixArray[i].trim().length());
					break;
				}
			}
		}
		if(!TextUtils.isEmpty(strSuffix))
		{
			String[] suffixArray = strSuffix.split(",");
			for(int i = 0; i < suffixArray.length; i++)
			{
				if(strCode.endsWith(suffixArray[i].trim()))
				{
					strCode = strCode.substring(0, strCode.length() - suffixArray[i].trim().length());
				}
			}
		}
		return strCode;
	}


	@Override
	public void loadData() {

		mUInvenDList.clear();

		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		mProdDetlList.clear();
		mProdDetlList = dbAdapter.getProdDetlList(mProdDetlItemMap, mProdDetlUPCMap);

		List<PresoldOrDInfo> loadDetlList = dbAdapter.getPresoldOrDList(m_nCurrentDetailFile);
		dbAdapter.close();

		List<UInvenDInfo> uInvenDList = new ArrayList<>();
		for (PresoldOrDInfo loadDetlInfo : loadDetlList) {

			UInvenDInfo uInvenDInfo = new UInvenDInfo();
			uInvenDInfo.strItemNo = loadDetlInfo.strItemNo;
			uInvenDInfo.strPickLocationCode = loadDetlInfo.strPickLocationCode;
			if(Utils.getProdLoconSCWType(mContext, loadDetlInfo.strItemNo) == Global.WEIGHT_ITEM)
			{
				uInvenDInfo.dLoaded = loadDetlInfo.dOrderQty;
				uInvenDInfo.dStock = loadDetlInfo.dOrderQty;
			}
			else
			{
				uInvenDInfo.nLoaded = loadDetlInfo.nOrderQty;
				uInvenDInfo.nStock = loadDetlInfo.nOrderQty;
			}

			ProdDetlInfo info = mProdDetlItemMap.get(loadDetlInfo.strItemNo);
			if( info != null )
			{
				uInvenDInfo.strShortDesc = info.strShortDesc;
				uInvenDInfo.strUOM = info.prodLocnInfo.strCaseUOM.length() > 0 ? info.prodLocnInfo.strCaseUOM : info.prodLocnInfo.strUnitUOM;
				uInvenDInfo.strFullCase = info.prodLocnInfo.strFullCase;
				uInvenDInfo.nWeight = info.nWeight;
				uInvenDInfo.strDetailFile = loadDetlInfo.strDetailFile;
				uInvenDInfo.strStartTime = mStrCurrentCustomerStartTime;
				uInvenDInfo.nSCWType = info.prodLocnInfo.nSCWType;
				uInvenDInfo.nOrder = info.prodLocnInfo.nOrderSeq;

				if(info.strLotControl.equalsIgnoreCase("Y") && Utils.getIntSetting(mContext, Global.PREF_LOAD_CUSTOMER) != 1)
				{
					uInvenDInfo.bLotControl = true;
				}

			}

			uInvenDList.add(uInvenDInfo);
			if( Utils.getIntSetting(mContext, Global.PREF_LOAD_CUSTOMER) == 2 && Global.gPartialLocationFilterList.size() > 0)
			{
				if( uInvenDInfo.strPickLocationCode.equals(Global.gPartialLocationFilterList.get(0)) )
					uInvenDInfo.bShow = true;
				else
					uInvenDInfo.bShow = false;
			}else
			{
				uInvenDInfo.bShow = true;
			}

		}

		for(UInvenDInfo uInvenDInfo : uInvenDList)
		{
			boolean bExist = false;
			UInvenDInfo bAddItem = uInvenDInfo;
			if( Global.gUInvenHinfo == null )
			{
				Date curDate = new Date();
				Global.gUInvenHinfo = new UInvenHInfo();
				Global.gUInvenHinfo.nTrxnNo = System.currentTimeMillis();
				Global.gUInvenHinfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(mContext, curDate));
				Global.gUInvenHinfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(curDate);
			}
			for(UInvenDInfo uHistoryInfo : Global.gUInvenHinfo.truckList)
			{
				if(uInvenDInfo.strDetailFile.equals(uHistoryInfo.strDetailFile) && uInvenDInfo.strItemNo.equals(uHistoryInfo.strItemNo)
						&& uInvenDInfo.strStartTime.equals(uHistoryInfo.strStartTime))
				{
					bExist = true;
					bAddItem = uHistoryInfo;
					uHistoryInfo.bShow = uInvenDInfo.bShow;
					break;
				}
			}

			if(!bExist)
			{
				Global.gUInvenHinfo.truckList.add(bAddItem);
			}

			if(bAddItem.bShow)
			{
				mUInvenDList.add(bAddItem);
			}
		}

		refreshPage();
	}

	private Map<String, Map<String, String>> sortMap = new HashMap<>();
	private Map<String, String> getSortItemMap(String strItemNo)
	{
		Map<String, String> map = sortMap.get(strItemNo);
		if(map == null)
		{
			map = Utils.getPropertyMap(mProdDetlItemMap.get(strItemNo));
			sortMap.put(strItemNo, map);
		}
		return map;
	}

	private void SortData(List<UInvenDInfo> uInvenDInfoList)
	{
		Collections.sort(uInvenDInfoList, (t1, t2) -> {
			Map<String, String> map1 = getSortItemMap(t1.strItemNo);
			Map<String, String> map2 = getSortItemMap(t2.strItemNo);
			String value1 = map1.get(sortFlag);
			String value2 = map2.get(sortFlag);
			if(value1 == null && value2 == null)
			{
				return 0;
			}
			else if(value1 != null && value2 == null)
			{
				return 1;
			}
			else if(value1 == null && value2 != null)
			{
				return -1;
			}
			else
			{
				return value1.compareTo(value2);
			}
		});
	}

	@Override
	public void refreshPage() {
		List<UInvenDInfo> list = new ArrayList<>();
		for (UInvenDInfo info : mUInvenDList) {
			if (info.bShow == true) {
				list.add(info);
			}
		}
		SortData(list);
		//Collections.sort(list, UInvenDInfo.itemComparator);
		//Collections.sort(list, UInvenDInfo.orderComparator);
		if (list.size() > 0) {
			mListView.setVisibility(View.VISIBLE);
			mTxtNoData.setVisibility(View.GONE);
			mListAdapter.setListData(list);
			mListAdapter.notifyDataSetChanged();
		} else {
			mListView.setVisibility(View.GONE);
			mTxtNoData.setVisibility(View.VISIBLE);
		}

		if(!TextUtils.isEmpty(strCartonDescription))
		{
			mRootView.findViewById(R.id.lblCartons).setVisibility(View.VISIBLE);
			mRootView.findViewById(R.id.txtCartons).setVisibility(View.VISIBLE);
			int nCartons = 0;
			DbAdapter dbAdapter = new DbAdapter(mContext);
			dbAdapter.open();
			for(UInvenDInfo uInvenDInfo : mUInvenDList)
			{
				if( uInvenDInfo.nSCWType != 2)
				{
					if(Utils.getProdLoconSCWType(mContext, uInvenDInfo.strItemNo) != Global.WEIGHT_ITEM)
					{
						nCartons += uInvenDInfo.nStock / getCaseValue(uInvenDInfo.strItemNo, dbAdapter);
					}

				}
			}
			dbAdapter.close();
			((TextView)mRootView.findViewById(R.id.txtCartons)).setText(String.valueOf(nCartons));
		}
		else
		{
			mRootView.findViewById(R.id.lblCartons).setVisibility(View.GONE);
			mRootView.findViewById(R.id.txtCartons).setVisibility(View.INVISIBLE);
		}
	}

	private Map<String, Integer> mCaseMap = new HashMap<>();
	private int getCaseValue(String strItemNo, DbAdapter dbAdapter)
	{
		if( mCaseMap.get(strItemNo) != null )
			return mCaseMap.get(strItemNo);
		ProdLocnInfo prodLocnInfo = dbAdapter.getProdLocnInfo(strItemNo);

		int nCase = 1;
		if( prodLocnInfo != null )
		{
			nCase = prodLocnInfo.nCaseFactor;
		}
		if( nCase < 1 ) nCase = 1;
		mCaseMap.put(strItemNo, nCase);
		return nCase;
	}

	@Override
	public void refreshLanguage() {
		((TextView) mRootView.findViewById(R.id.lblTitle)).setText(Str.LoadCustomer[Global.LANG].toUpperCase());
		((TextView) mRootView.findViewById(R.id.txtTrip)).setText(mStrCurrentCustomerName);
		((TextView) mRootView.findViewById(R.id.txtQty)).setText(Str.Qty[Global.LANG]);
		((TextView) mRootView.findViewById(R.id.txtDescription)).setText("");
		((Button) mRootView.findViewById(R.id.btnAdd)).setText(Str.Add[Global.LANG]);
		((Button) mRootView.findViewById(R.id.btnConfirm)).setText(Str.Confirm[Global.LANG]);
		if( !TextUtils.isEmpty(strCartonDescription))
		{
			((TextView)mRootView.findViewById(R.id.lblCartons)).setText(String.format(Locale.ENGLISH, Str.TotalCartonCount[Global.LANG], strCartonDescription));
		}
	}

	private void doProcessSelectProduct(final String strItemNo, int nQty, boolean bUpdateItemNo, boolean isSeachByItemNo)
	{
		if (strItemNo.length() == 0) {
			Utils.printDebug(mContext, Str.NoProductFound[Global.LANG]);
		} else {
			if(bUpdateItemNo || !Utils.getStringSetting(mContext, Global.PREF_AUTO_SCAN_ADD).equals("1"))
			{
				((EditText) mRootView.findViewById(R.id.editItemNo)).setText(strItemNo);
				if(Utils.getProdLoconSCWType(mContext, strItemNo) == Global.WEIGHT_ITEM)
				{
					((EditText) mRootView.findViewById(R.id.editQty)).setText(String.valueOf(nQty));
				}
				else
				{
					mRootView.findViewById(R.id.editQty).setTag("From App");
					((EditText) mRootView.findViewById(R.id.editQty)).setText(String.valueOf(nQty));
					mRootView.findViewById(R.id.editQty).setTag(null);
				}
			}


			this.showKeyboard((EditText) mRootView.findViewById(R.id.editQty), true);
		}

		doSearch(isSeachByItemNo);
		if(Utils.getStringSetting(mContext, Global.PREF_AUTO_SCAN_ADD).equals("1") && !bUpdateItemNo)
		{
			new Handler(Looper.getMainLooper()).post(new Runnable() {
				@Override
				public void run() {
					String strAmount = ((EditText) mRootView.findViewById(R.id.editQty)).getText().toString().trim();
					if(Utils.getProdLoconSCWType(mContext, strItemNo) == Global.WEIGHT_ITEM)
					{
						float amount = 0;
						if(!TextUtils.isEmpty(strAmount))
						{
							amount = Utils.getFloatFromString(strAmount);
						}
						amount += 1.0f;
						mRootView.findViewById(R.id.editQty).setTag("From App");
						((EditText) mRootView.findViewById(R.id.editQty)).setText(String.format(Locale.ENGLISH, "%.2f", amount));
						mRootView.findViewById(R.id.editQty).setTag(null);
					}
					else
					{
						int amount = 0;
						if(!TextUtils.isEmpty(strAmount))
						{
							amount = Utils.getIntFromString(strAmount);
						}
						amount++;
						((EditText) mRootView.findViewById(R.id.editQty)).setText(String.format(Locale.ENGLISH, "%d", amount));
					}
					doAdd();
				}
			});
		}
	}

	@Override
	public void receiveBroadcast(String strAction, Intent intent) {
		if (strAction.equals(Global.ACTION_SELECT_PRODUCT) == true) {
			int nType = intent.getIntExtra(Global.EXTRA_TYPE, 0);
			String strItemNo = "";
			int nQty = 0;
			double dQty = 0;
			if (nType == Global.SELECT_PRODUCT_ITEMNO) {
				nQty = 0;
				dQty = 0;
				strItemNo = intent.getStringExtra(Global.EXTRA_ITEM_NO);
				doProcessSelectProduct(strItemNo, nQty, true, true);
			} else if (nType == Global.SELECT_PRODUCT_ITEMUPC) {
				String strFrom = intent.getStringExtra(Global.EXTRA_BARCODESCAN_FROM);
				if (!strFrom.equals(getClass().getSimpleName())) {
					return;
				}
				nQty = 1;
				dQty = 1;
				String strItemUPC = intent.getStringExtra(Global.EXTRA_ITEM_UPC);
				ProdDetlInfo info = mProdDetlUPCMap.get(strItemUPC);
				if( info != null )
				{
					strItemNo = info.strItemNo;
				}
				((EditText) mRootView.findViewById(R.id.editItemNo)).setText(strItemUPC);
				doProcessSelectProduct(strItemNo, nQty, false, false);
			}

		} else if (strAction.equals(Global.ACTION_FILTER_LOCATION) == true) {
			List<String> addedList = new ArrayList<>();
			if (Global.gPrevLocationFilterList.size() > 0) {
				for (String strNew : Global.gLocationFilterList) {
					boolean bExist = false;
					for (String strOrig : Global.gPrevLocationFilterList) {
						if (strOrig.equals(strNew) == true) {
							bExist = true;
							break;
						}
					}
					if (bExist == false) {
						addedList.add(strNew);
					}
				}
			} else {
				addedList.addAll(Global.gLocationFilterList);
			}

			int nNewOrder = getMaxOrder() + 1;
			for (int i=0; i<mUInvenDList.size(); i++) {
				if (mUInvenDList.get(i).strPickLocationCode.length() == 0) {
					continue;
				}
				boolean bExist = false;
				int nOrder = mUInvenDList.get(i).nOrder;
				for (String strFilter : Global.gLocationFilterList) {
					if (mUInvenDList.get(i).strPickLocationCode.equals(strFilter) == true) {
						bExist = true;
						for (String strFilter1 : addedList) {
							if (mUInvenDList.get(i).strPickLocationCode.equals(strFilter1) == true) {
								nOrder = nNewOrder;
								break;
							}
						}
						break;
					}
				}
				if (bExist == true) {
					mUInvenDList.get(i).bShow = true;
					mUInvenDList.get(i).nOrder = nOrder;
				} else {
					mUInvenDList.get(i).bShow = false;
					mUInvenDList.get(i).nOrder = -1;
				}
			}
			refreshPage();
		}
	}

	private int getMaxOrder() {
		int nOrder = 0;
		for (UInvenDInfo info : mUInvenDList) {
			if (info.nOrder > nOrder) {
				nOrder = info.nOrder;
			}
		}
		return nOrder;
	}

	private void doSearch(boolean isByItemNo) {
		String strItemNo = getItemCode(((EditText) mRootView.findViewById(R.id.editItemNo)).getText().toString());
		String strDescription = Str.Invalid_Product[Global.LANG];
		String strQty = "";
		mProdDetlSelInfo = null;
		if (strItemNo.length() > 0) {
			if (isByItemNo) {
				for (int i = 0; i < mProdDetlList.size(); i++) {
					ProdDetlInfo prodDetlInfo = mProdDetlList.get(i);
					if (prodDetlInfo.strItemNo.equals(strItemNo)) {
						mProdDetlSelInfo = prodDetlInfo;
						strItemNo = prodDetlInfo.strItemNo;
						strDescription = prodDetlInfo.strDescription;
						break;
					}
				}
			}
			if (mProdDetlSelInfo == null) {
				for (int i = 0; i < mProdDetlList.size(); i++) {
					ProdDetlInfo prodDetlInfo = mProdDetlList.get(i);
					if (Utils.getDisplayItemCode(mContext, prodDetlInfo.strItemNo).equals(strItemNo)) {
						mProdDetlSelInfo = prodDetlInfo;
						strItemNo = prodDetlInfo.strItemNo;
						strDescription = prodDetlInfo.strDescription;
						break;
					}
				}
			}
			if (mProdDetlSelInfo == null) {
				for (int i = 0; i < mProdDetlList.size(); i++) {
					ProdDetlInfo prodDetlInfo = mProdDetlList.get(i);
					if (prodDetlInfo.strItemUPC.equals(strItemNo)) {
						mProdDetlSelInfo = prodDetlInfo;
						strItemNo = prodDetlInfo.strItemNo;
						strDescription = prodDetlInfo.strDescription;
						break;
					}
				}
			}
			if (mProdDetlSelInfo == null) {
				for (int i = 0; i < mProdDetlList.size(); i++) {
					ProdDetlInfo prodDetlInfo = mProdDetlList.get(i);
					if (prodDetlInfo.strItemUPC20.equals(strItemNo)) {
						mProdDetlSelInfo = prodDetlInfo;
						strItemNo = prodDetlInfo.strItemNo;
						strDescription = prodDetlInfo.strDescription;
						break;
					}
				}
			}

			for (UInvenDInfo info : mUInvenDList) {
				if (info.strItemNo.equals(strItemNo) == true) {
					if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
					{
						strQty = String.format(Locale.ENGLISH, "%.2f", info.dStock);
					}
					else
					{
						strQty = String.valueOf(info.nStock);
					}
					break;
				}
			}
		}
		else
		{
			strDescription = "";
		}
		((TextView) mRootView.findViewById(R.id.txtDescription)).setText(strDescription);
		if(Utils.getProdLoconSCWType(mContext, strItemNo) == Global.WEIGHT_ITEM)
		{
			mRootView.findViewById(R.id.editQty).setTag("From App");
			if(mEntryInputMethod.equals("A"))
			{
				((EditText) mRootView.findViewById(R.id.editQty)).setText("");
			}
			else
			{
				((EditText) mRootView.findViewById(R.id.editQty)).setText(strQty);
			}
			mRootView.findViewById(R.id.editQty).setTag(null);
		}
		else
		{
			if(mEntryInputMethod.equals("A"))
			{
				((EditText) mRootView.findViewById(R.id.editQty)).setText("");
			}
			else
			{
				((EditText) mRootView.findViewById(R.id.editQty)).setText(strQty);
			}
		}
	}

	private void doAdd() {
		mCurSelUInvenDInfo = null;
		String strQty = ((EditText) mRootView.findViewById(R.id.editQty)).getText().toString();
		if (mProdDetlSelInfo == null) {
			if(!TextUtils.isEmpty(((EditText) mRootView.findViewById(R.id.editItemNo)).getText().toString()))
			{
				Utils.printDebug(mContext, Str.Please_select_valid_product[Global.LANG]);
			}
			((EditText) mRootView.findViewById(R.id.editQty)).setText("");
			focusAndHideKeyboard((EditText) mRootView.findViewById(R.id.editItemNo));
			return;
		}
		if (strQty.length() == 0) {
			Utils.printDebug(mContext, Str.Please_input_the_Quantity[Global.LANG]);
			showKeyboard((EditText) mRootView.findViewById(R.id.editQty), true);
			return;
		}

		int nIdx = -1;
		for (int i = 0; i < mUInvenDList.size(); i++) {
			if (mUInvenDList.get(i).strItemNo.equals(mProdDetlSelInfo.strItemNo) == true) {
				nIdx = i;
				break;
			}
		}

		if(Utils.getProdLoconSCWType(mContext, mProdDetlSelInfo.strItemNo) == Global.WEIGHT_ITEM)
		{
			double dValue = Utils.parseDoubleFromString(strQty);
			if(nIdx == -1)
			{
				if(mEntryInputMethod.equals("A"))
				{
					if(dValue < 0)
					{
						((EditText) mRootView.findViewById(R.id.editQty)).setText("");
						Utils.printDebug(mContext, Str.Quantity_Removed_From_the_transaction[Global.LANG]);
						showKeyboard((EditText) mRootView.findViewById(R.id.editQty), true);
						return;
					}
				}
				UInvenDInfo info = new UInvenDInfo();
				info.strItemNo = mProdDetlSelInfo.strItemNo;
				info.dStock = dValue;
				info.strShortDesc = mProdDetlSelInfo.strShortDesc;
				info.strUOM = mProdDetlSelInfo.prodLocnInfo.strCaseUOM.length() > 0 ? mProdDetlSelInfo.prodLocnInfo.strCaseUOM : mProdDetlSelInfo.prodLocnInfo.strUnitUOM;
				info.strFullCase = mProdDetlSelInfo.prodLocnInfo.strFullCase;
				info.nWeight = mProdDetlSelInfo.nWeight;

				if(mProdDetlSelInfo.strLotControl.equalsIgnoreCase("Y") && Utils.getIntSetting(mContext, Global.PREF_LOAD_CUSTOMER) != 1)
				{
					info.bLotControl = true;
				}

				info.bShow = true;
				info.nOrder = getMaxOrder() + 1;
				mUInvenDList.add(info);
				mCurSelUInvenDInfo = info;
			}
			else
			{
				if(mEntryInputMethod.equals("A"))
				{
					if(dValue + mUInvenDList.get(nIdx).dStock == 0 && mUInvenDList.get(nIdx).nLoaded == 0){
						mUInvenDList.remove(nIdx);
					}
					else if(dValue + mUInvenDList.get(nIdx).dStock < 0){
						((EditText) mRootView.findViewById(R.id.editQty)).setText("");
						Utils.printDebug(mContext, Str.Quantity_Removed_From_the_transaction[Global.LANG]);
						showKeyboard((EditText) mRootView.findViewById(R.id.editQty), true);
						return;
					}
					else
					{
						mUInvenDList.get(nIdx).dStock += Utils.parseDoubleFromString(strQty);
						mCurSelUInvenDInfo = mUInvenDList.get(nIdx);
						mCurSelUInvenDInfo.bShow = true;
						if (mUInvenDList.get(nIdx).dLoaded < mUInvenDList.get(nIdx).dStock && mUInvenDList.get(nIdx).dLoaded > 0 ) {
							Utils.printDebug(mContext, Str.ActualLoadQuantityExceedsPlan[Global.LANG]);
						}
					}
				}
				else
				{
					if(strQty.equals("0.00") && mUInvenDList.get(nIdx).nLoaded == 0){
						mUInvenDList.remove(nIdx);
					}
					else
					{
						mCurSelUInvenDInfo = mUInvenDList.get(nIdx);
						mCurSelUInvenDInfo.bShow = true;
						mUInvenDList.get(nIdx).dStock = Utils.parseDoubleFromString(strQty);
						if (mUInvenDList.get(nIdx).dLoaded < mUInvenDList.get(nIdx).dStock && mUInvenDList.get(nIdx).dLoaded > 0 ) {
							Utils.printDebug(mContext, Str.ActualLoadQuantityExceedsPlan[Global.LANG]);
						}
					}
				}

			}
		}
		else
		{
			int nQuantity = Utils.parseIntFromString(strQty);
			if (nIdx == -1) {
				if (nQuantity == 0) {
					Utils.printDebug(mContext, Str.Please_input_the_Quantity[Global.LANG]);
					showKeyboard((EditText) mRootView.findViewById(R.id.editQty), true);
					return;
				} else {
					if(mEntryInputMethod.equals("A"))
					{
						if(nQuantity < 0)
						{
							((EditText) mRootView.findViewById(R.id.editQty)).setText("");
							Utils.printDebug(mContext, Str.Quantity_Removed_From_the_transaction[Global.LANG]);
							showKeyboard((EditText) mRootView.findViewById(R.id.editQty), true);
							return;
						}
					}
					UInvenDInfo info = new UInvenDInfo();
					info.strItemNo = mProdDetlSelInfo.strItemNo;
					info.nStock = nQuantity;
					info.strShortDesc = mProdDetlSelInfo.strShortDesc;
					info.strUOM = mProdDetlSelInfo.prodLocnInfo.strCaseUOM.length() > 0 ? mProdDetlSelInfo.prodLocnInfo.strCaseUOM : mProdDetlSelInfo.prodLocnInfo.strUnitUOM;
					info.strFullCase = mProdDetlSelInfo.prodLocnInfo.strFullCase;
					info.nWeight = mProdDetlSelInfo.nWeight;

					if(mProdDetlSelInfo.strLotControl.equalsIgnoreCase("Y") && Utils.getIntSetting(mContext, Global.PREF_LOAD_CUSTOMER) != 1)
					{
						info.bLotControl = true;
					}

					info.bShow = true;
					info.nOrder = getMaxOrder() + 1;
					mUInvenDList.add(info);
					mCurSelUInvenDInfo = info;
				}
			} else {
				if(mEntryInputMethod.equals("A"))
				{
					if(mUInvenDList.get(nIdx).nStock + nQuantity == 0 && mUInvenDList.get(nIdx).nLoaded == 0){
						mUInvenDList.remove(nIdx);
					}
					else if(mUInvenDList.get(nIdx).nStock + nQuantity < 0)
					{
						((EditText) mRootView.findViewById(R.id.editQty)).setText("");
						Utils.printDebug(mContext, Str.Quantity_Removed_From_the_transaction[Global.LANG]);
						showKeyboard((EditText) mRootView.findViewById(R.id.editQty), true);
						return;
					}
					else
					{
						mCurSelUInvenDInfo = mUInvenDList.get(nIdx);
						mCurSelUInvenDInfo.bShow = true;
						mUInvenDList.get(nIdx).nStock += nQuantity;
						if (mUInvenDList.get(nIdx).nLoaded < mUInvenDList.get(nIdx).nStock && mUInvenDList.get(nIdx).nLoaded > 0 ) {
							Utils.printDebug(mContext, Str.ActualLoadQuantityExceedsPlan[Global.LANG]);
						}
					}
				}
				else
				{
					if (nQuantity == 0 && mUInvenDList.get(nIdx).nLoaded == 0) {
						mUInvenDList.remove(nIdx);
					} else {
						mCurSelUInvenDInfo = mUInvenDList.get(nIdx);
						mCurSelUInvenDInfo.bShow = true;
						mUInvenDList.get(nIdx).nStock = nQuantity;
						if (mUInvenDList.get(nIdx).nLoaded < nQuantity && mUInvenDList.get(nIdx).nLoaded > 0 ) {
							Utils.printDebug(mContext, Str.ActualLoadQuantityExceedsPlan[Global.LANG]);
						}
					}
				}

			}
		}



		((EditText) mRootView.findViewById(R.id.editItemNo)).setText("");
		((EditText) mRootView.findViewById(R.id.editQty)).setText("");
		focusAndHideKeyboard((EditText) mRootView.findViewById(R.id.editItemNo));

		refreshPage();
		doCheckLoadBatch();
	}

	private void doCheckLoadBatch() {
		if(mCurSelUInvenDInfo == null) return;

		if(mCurSelUInvenDInfo.bLotControl && (mCurSelUInvenDInfo.loadBlotInfoList == null || mCurSelUInvenDInfo.loadBlotInfoList.size() < mCurSelUInvenDInfo.nStock) )
		{
			try {
				String strBatchCodes = "";
				for(LoadBlotInfo info : mCurSelUInvenDInfo.loadBlotInfoList)
				{
					if(TextUtils.isEmpty(strBatchCodes))
					{
						strBatchCodes = info.strLotNumber;
					}
					else
					{
						strBatchCodes += "," + info.strLotNumber;
					}
				}
				BatchCodeInputDialog inputDialog = new BatchCodeInputDialog(mContext, strBatchCodes1 -> {
					for(int i = 0; i < strBatchCodes1.length; i++)
					{
						if(i == mCurSelUInvenDInfo.loadBlotInfoList.size())
						{
							LoadBlotInfo loadBlotInfo = new LoadBlotInfo();
							loadBlotInfo.strBatchDate = new SimpleDateFormat("yyyyMMdd").format(new Date());
							loadBlotInfo.strLotNumber = strBatchCodes1[i];
							loadBlotInfo.nLotQty = 1;
							mCurSelUInvenDInfo.loadBlotInfoList.add(loadBlotInfo);
						}
						else
						{
							mCurSelUInvenDInfo.loadBlotInfoList.get(i).strLotNumber = strBatchCodes1[i];
						}
					}
				}, mCurSelUInvenDInfo.nStock, strBatchCodes, null);
				inputDialog.show();
			}catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		else if(Utils.getStringSetting(mContext, Global.PREF_LOAD_BATCH).equals("1"))
		{
			try {
				final InputBatchNumDialog inputDlg = new InputBatchNumDialog(mContext);
				inputDlg.mStrTitle = Str.BatchCode[Global.LANG];
				inputDlg.mStrHint = Str.InputBatchCode[Global.LANG];
				inputDlg.mStrLeft = Str.Enter[Global.LANG];
				inputDlg.mStrRight = Str.Return[Global.LANG];
				inputDlg.mStrInputed = mCurSelUInvenDInfo.strBatchCode;
				inputDlg.setCancelable(false);
				inputDlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						if (inputDlg.mReturnCode == Global.DLG_RETURN_LEFT) {
							mCurSelUInvenDInfo.strBatchCode = inputDlg.mStrInputed;
						}
					}
				});
				inputDlg.show();
			}catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	private void doProcessConfirm()
	{
		boolean bZero = false;
		for (UInvenDInfo info : mUInvenDList) {
			if(info.nStock == 0 && info.bShow == true && info.nSCWType == 1)
			{
				bZero = true;
				break;
			}
		}
		if( bZero )
		{
			try {
				final MessageDialog msgDialog = new MessageDialog(mContext);
				msgDialog.setCancelable(false);
				msgDialog.mStrTitle = Str.Warning[Global.LANG].toUpperCase();
				msgDialog.mStrMessage = Str.ZeroWarning[Global.LANG];
				msgDialog.mStrLeft = Str.No[Global.LANG];
				msgDialog.mStrRight = Str.Yes[Global.LANG];
				msgDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
							Global.gCurCustomerInfo.nCompleteStatus = 2;
							gotoPrev();
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
			Global.gCurCustomerInfo.nCompleteStatus = 1;
			gotoPrev();
		}
	}

	private void doConfirm() {
/*		if (mPickLocationCodeCnt > 0 && mPickLocationCodeCnt > Global.gLocationFilterList.size()) {
			final MessageDialog msgDialog = new MessageDialog(mContext);
			msgDialog.setCancelable(false);
			msgDialog.mStrTitle = Str.LoadNotComplete[Global.LANG].toUpperCase();
			msgDialog.mStrMessage = Str.LoadNotComplete_msg[Global.LANG];
			msgDialog.mStrLeft = Str.Return[Global.LANG];
			msgDialog.mStrRight = Str.Proceed[Global.LANG];
			msgDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
					if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
						Utils.setBooleanSetting(mContext, Global.PREF_LOAD_INVENTORY_COMPLETED, false);
						gotoNextFragment();
					}
				}
			});
			msgDialog.show();
		} else {
			Utils.setBooleanSetting(mContext, Global.PREF_LOAD_INVENTORY_COMPLETED, true);
			gotoNextFragment();
		}*/
		if(Utils.getStringSetting(mContext, Global.PREF_LOAD_BATCH).equals("1"))
		{
			List<UInvenDInfo> uUnknownBatchList = new ArrayList<>();
			for(UInvenDInfo uInvenDInfo : mUInvenDList)
			{
				if(TextUtils.isEmpty(uInvenDInfo.strBatchCode))
				{
					uUnknownBatchList.add(uInvenDInfo);
				}
			}

			if(uUnknownBatchList.size() > 0)
			{
				try {
					final InputBatchNumAllDialog inputDlg = new InputBatchNumAllDialog(mContext);
					inputDlg.mUInvenDList = uUnknownBatchList;
					inputDlg.mStrTitle = Str.BatchCode[Global.LANG];
					inputDlg.mStrLeft = Str.Enter[Global.LANG];
					inputDlg.mStrRight = Str.Return[Global.LANG];
					inputDlg.setCancelable(false);
					inputDlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
						@Override
						public void onDismiss(DialogInterface dialog) {
							doProcessConfirm();
						}
					});
					inputDlg.show();
				}catch (Exception e)
				{
					e.printStackTrace();
				}
				return;
			}
		}
		doProcessConfirm();
	}

	private void gotoPrev()
	{
		for (UInvenDInfo info : mUInvenDList) {
			if(info.bShow)
			{
				info.bCompleted = true;
				info.nLoaded = info.nStock;
			}
		}


		//Update Data
		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		mProdDetlItemMap.clear();
		mProdDetlUPCMap.clear();
		mProdInfoMap.clear();
		dbAdapter.getProdDetlList(mProdDetlItemMap, mProdDetlUPCMap);
//		if(Utils.getStringSetting(mContext, Global.PREF_ITEM_CODE_DISPLAY_SOURCE).equals("PRODINFO"))
//		{
//			mProdInfoMap = dbAdapter.getProdInfoMap("ItemCode");
//		}
		List<PresoldOrDInfo> loadDetlList = dbAdapter.getPresoldOrDList(m_nCurrentDetailFile);
		List<PresoldOrDInfo> loadFreeDetList = dbAdapter.getFreePresoldOrDList(m_nCurrentDetailFile);
		Map<String, Integer> freeItemCountMap = new HashMap<>();
		Map<String, Double> freeItemCountMapD = new HashMap<>();
		for(PresoldOrDInfo freeInfo : loadFreeDetList)
		{
			if(Utils.getProdLoconSCWType(mContext, freeInfo.strItemNo) == Global.WEIGHT_ITEM)
			{
				Double value = freeItemCountMapD.get(freeInfo.strItemNo);
				if( value != null )
				{
					value += freeInfo.dOrderQty;
					freeItemCountMapD.put(freeInfo.strItemNo, value);
				}
				else
				{
					freeItemCountMapD.put(freeInfo.strItemNo, freeInfo.dOrderQty);
				}
			}
			else
			{
				Integer value = freeItemCountMap.get(freeInfo.strItemNo);
				if( value != null )
				{
					value += freeInfo.nOrderQty;
					freeItemCountMap.put(freeInfo.strItemNo, value);
				}
				else
				{
					freeItemCountMap.put(freeInfo.strItemNo, freeInfo.nOrderQty);
				}
			}

		}

		for(PresoldOrDInfo info : loadDetlList )
		{
			boolean bModify = false;
			for(UInvenDInfo uInfo : mUInvenDList )
			{
				if( uInfo.strItemNo.equals(info.strItemNo) )
				{
					if(bModify == false)
					{
						bModify = true;
						if(Utils.getProdLoconSCWType(mContext, uInfo.strItemNo) == Global.WEIGHT_ITEM)
						{
							info.dOrderQty = uInfo.dStock;
						}
						else
						{
							info.nOrderQty = uInfo.nStock;
						}
					}
					else
					{
						if(Utils.getProdLoconSCWType(mContext, uInfo.strItemNo) == Global.WEIGHT_ITEM)
						{
							info.dOrderQty += uInfo.dStock;
						}
						else
						{
							info.nOrderQty += uInfo.nStock;
						}
					}
				}
			}

			if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
			{
				Double value = freeItemCountMapD.get(info.strItemNo);
				if( value != null )
				{
					info.dOrderQty -= value;
				}

				if( info.dOrderQty == 0 )
				{
					dbAdapter.deletePresoldOrDInfo(info);
				}
				else if(bModify)
				{
					dbAdapter.updatePresoldOrDInfo(info);
				}
			}
			else
			{
				Integer value = freeItemCountMap.get(info.strItemNo);
				if( value != null )
				{
					info.nOrderQty -= value;
				}

				if( info.nOrderQty == 0 )
				{
					dbAdapter.deletePresoldOrDInfo(info);
				}
				else if(bModify)
				{
					dbAdapter.updatePresoldOrDInfo(info);
				}
			}

		}
		dbAdapter.close();
		mContext.onBackPressed();
	}

	private void gotoNextFragment() {
		if (Utils.getIntSetting(mContext, Global.PREF_LOAD_CONTAINERS) == 1) {
			mContext.replaceFragment(new LoadContainersFragment());
		} else {
			mContext.replaceFragment(new LoadTruckSignFragment());
		}
	}

	private void showInvenDetail(UInvenDInfo info) {
		((EditText) mRootView.findViewById(R.id.editItemNo)).setText(Utils.getDisplayItemCode(mContext, info.strItemNo));
		String strQty = String.valueOf(info.nStock);

		if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
		{
			mRootView.findViewById(R.id.editQty).setTag("From App");
			if(mEntryInputMethod.equals("A"))
			{
				((EditText) mRootView.findViewById(R.id.editQty)).setText("");
			}
			else
			{
				((EditText) mRootView.findViewById(R.id.editQty)).setText(strQty);
			}
			mRootView.findViewById(R.id.editQty).setTag(null);
			mRootView.findViewById(R.id.editQty).requestFocus();
			((EditText) mRootView.findViewById(R.id.editQty)).setSelection(0);
		}
		else
		{
			if(mEntryInputMethod.equals("A"))
			{
				((EditText) mRootView.findViewById(R.id.editQty)).setText("");
				mRootView.findViewById(R.id.editQty).requestFocus();
				((EditText) mRootView.findViewById(R.id.editQty)).setSelection(0);
			}
			else
			{
				((EditText) mRootView.findViewById(R.id.editQty)).setText(strQty);
				mRootView.findViewById(R.id.editQty).requestFocus();
				((EditText) mRootView.findViewById(R.id.editQty)).setSelection(strQty.length());
			}
		}
	}

	private void doSort()
	{
		int nOrigIdx = 0;
		for(int i = 0; i < Global.sortArray.length; i++)
		{
			if(Utils.getSortValue(Global.sortArray[i]).equals(sortFlag))
			{
				nOrigIdx = i;
				break;
			}
		}
		final int nFinalOrigIdx = nOrigIdx;
		try {
			new AlertDialog.Builder(mContext)
					.setTitle(Str.SelectSortOption[Global.LANG])
					.setSingleChoiceItems(new AlertSingleChoiceAdapter(mContext, Global.sortArray), nOrigIdx, (dialog, nSelected) -> setSortFlag(Global.sortArray[nSelected]))
					.setPositiveButton(Str.Ok[Global.LANG], (dialog, nSelected) -> {
						refreshPage();
					})
					.setNegativeButton(Str.Cancel[Global.LANG], (dialog, nSelected) -> setSortFlag(Global.sortArray[nFinalOrigIdx]))
					.create()
					.show();
		}catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btnBack:
				mContext.onBackPressed();
				break;
			case R.id.btnAdd:
				doAdd();
				break;
			case R.id.btnSort:
				doSort();
				break;
			case R.id.btnProductTreeList:
				mContext.replaceFragment(new NewProductTreelistFragment().setFrom(this));
				break;
			case R.id.btnBarcodeScanner:
				Intent intent = new Intent(mContext, BarcodeScannerActivity.class);
				intent.putExtra(Global.EXTRA_BARCODESCAN_FROM, getClass().getSimpleName());
				startActivity(intent);
				mContext.overridePendingTransition(R.anim.start1, R.anim.start2);
				break;
			case R.id.btnProductSearch:
				SearchProductFragment fragment = new SearchProductFragment();
				Bundle args = new Bundle();
				args.putBoolean(Global.EXTRA_FILTER, Utils.getIntSetting(mContext, Global.PREF_INVENCKECK) != 0);
				fragment.setArguments(args);
				mContext.replaceFragment(fragment);
				break;
			case R.id.btnConfirm:
				doConfirm();
				break;
		}
	}

	private class ListAdapter extends BaseAdapter {

		private List<UInvenDInfo> list = new ArrayList<>();

		private class ViewHolder {
			public TextView txtItemNo, txtShortDesc, txtPickLocationCode, txtLoaded, txtStock;
		}

		public int getCount() {
			return list.size();
		}

		public void setListData(List<UInvenDInfo> list) {
			this.list.clear();
			this.list.addAll(list);
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
			if(info == null) return null;

			if (convertView == null) {
				view = mContext.getLayoutInflater().inflate(R.layout.row_truck_customer_order_detail, parent, false);
				holder = new ViewHolder();
				holder.txtItemNo = (TextView) view.findViewById(R.id.txtItemNo);
				holder.txtShortDesc = (TextView) view.findViewById(R.id.txtShortDesc);
				holder.txtPickLocationCode = (TextView) view.findViewById(R.id.txtPickLocationCode);
				holder.txtLoaded = (TextView) view.findViewById(R.id.txtLoaded);
				holder.txtStock = (TextView) view.findViewById(R.id.txtStock);
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
			holder.txtPickLocationCode.setText(info.strPickLocationCode);
			if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
			{
				holder.txtLoaded.setText(String.format(Locale.ENGLISH, "%.2f", info.dLoaded));
				holder.txtStock.setText(String.format(Locale.ENGLISH, "%.2f", info.nStock));
			}
			else
			{
				holder.txtLoaded.setText(String.valueOf(info.nLoaded));
				holder.txtStock.setText(String.valueOf(info.nStock));
			}


			view.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showInvenDetail(info);
				}
			});

			return view;
		}
	}

	private void setQtyFormat()
	{
		if( mProdDetlSelInfo != null )
		{

			if(Utils.getProdLoconSCWType(mContext, mProdDetlSelInfo.strItemNo) == Global.WEIGHT_ITEM)
			{
				EditText editQty = (EditText)mRootView.findViewById(R.id.editQty);
				String strVal = editQty.getText().toString();
				if(editQty.getTag() == null)
				{
					String strArray[] = strVal.split("\\.");
					strVal = "";
					for (int i=0; i<strArray.length; i++) {
						strVal += strArray[i];
					}
					double dValue = Utils.parseFloatFromString(strVal) / 100;
					editQty.setTag("From App");
					editQty.setText(String.format(Locale.ENGLISH, "%.2f", dValue));
					editQty.setSelection(editQty.getText().toString().length());
					editQty.setTag(null);
				}
			}
		}
	}
}