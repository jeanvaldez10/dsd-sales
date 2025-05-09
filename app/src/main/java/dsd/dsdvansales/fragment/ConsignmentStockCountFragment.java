package dsd.dsdvansales.fragment;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

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
import dsd.dsdvansales.dialogs.MessageDialog;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.model.CustConBalInfo;
import dsd.dsdvansales.model.PricingInfo;
import dsd.dsdvansales.model.PrintTaxRateInfo;
import dsd.dsdvansales.model.ProdDetlInfo;
import dsd.dsdvansales.model.ProdInfo;
import dsd.dsdvansales.model.ProdLocnInfo;
import dsd.dsdvansales.model.ProdStruInfo;
import dsd.dsdvansales.model.TaxRatesInfo;
import dsd.dsdvansales.model.UOrderDInfo;
import dsd.dsdvansales.model.UOrderHInfo;
import dsd.dsdvansales.model.UTaxInfo;
import dsd.dsdvansales.views.AlertSingleChoiceAdapter;

public class ConsignmentStockCountFragment extends BaseFragment implements View.OnClickListener {

	private TextView mTxtNoData = null;
	private ListView mListView = null;
	private ListAdapter mListAdapter = null;
	private List<UOrderDInfo> mUOrderDList = new ArrayList<>();
	private boolean bLoaded = false;

	private boolean filter = false;

	private List<ProdDetlInfo> mProdDetlList = new ArrayList<>();
	private Map<String, ProdDetlInfo> mProdDetlItemMap = new HashMap<>();
	private Map<String, ProdDetlInfo> mProdDetlUPCMap = new HashMap<>();
	private Map<String, ProdInfo> mProdInfoMap = new HashMap<>();
	private Map<String, PricingInfo> mPriceInfoMap = new HashMap<>();

	private TextView mTxtRunningQty, mTxtCartons, mTxtWeight;
	ProdDetlInfo mSelProductInfo = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootView = inflater.inflate(R.layout.fragment_consignment_stock_count, container, false);
		commonInit();
		return mRootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		Global.gFragIdx = Global.FRAG_CONSIGNMENT_COUNT;
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void initView() {

		mRootView.findViewById(R.id.btnCount).setOnClickListener(this);
		mRootView.findViewById(R.id.btnFilter).setOnClickListener(this);
		mRootView.findViewById(R.id.btnSort).setOnClickListener(this);

		mTxtRunningQty = (TextView)mRootView.findViewById(R.id.txtRunningTotal);
		mTxtCartons = (TextView)mRootView.findViewById(R.id.txtCartons);
		mTxtWeight = (TextView)mRootView.findViewById(R.id.txtWeight);

		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		List<ProdStruInfo> prodStruList = dbAdapter.getProdStruList();
		dbAdapter.close();
		if( prodStruList == null || prodStruList.size() == 0 )
		{
			mRootView.findViewById(R.id.btnProductTreeList).setEnabled(false);
			mRootView.findViewById(R.id.btnProductTreeList).setOnClickListener(null);
			mRootView.findViewById(R.id.btnProductTreeList).setBackground(mContext.getResources().getDrawable(R.drawable.btn_grey_large));
		}else
		{
			mRootView.findViewById(R.id.btnProductTreeList).setOnClickListener(this);
			mRootView.findViewById(R.id.btnProductTreeList).setEnabled(true);
			mRootView.findViewById(R.id.btnProductTreeList).setBackground(mContext.getResources().getDrawable(R.drawable.btn_white_medium));
		}

		mRootView.findViewById(R.id.btnBarcodeScanner).setOnClickListener(this);
		mRootView.findViewById(R.id.btnProductSearch).setOnClickListener(this);
		mRootView.findViewById(R.id.btnBack).setOnClickListener(this);
		mRootView.findViewById(R.id.btnCompleted).setOnClickListener(this);

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
					doSearch(0, false);
				}
				else
				{
					if(Utils.getStringSetting(mContext, Global.PREF_AUTO_SCAN_ADD).equals("1"))
					{
						doProcessSelectProduct(strItemNo, 4, false);
						doCount();
					}
					else
					{
						doProcessSelectProduct(strItemNo, 3, false);
					}
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

		((EditText) mRootView.findViewById(R.id.editItemNo)).setOnEditorActionListener(new EditText.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_NEXT) {
					mRootView.findViewById(R.id.editQty).requestFocus();
					EditText editQty = (EditText)mRootView.findViewById(R.id.editQty);
					String strQty = editQty.getText().toString();
					editQty.setSelection(strQty.length(), strQty.length());
					return true;
				}
				return false;
			}
		});


		((EditText) mRootView.findViewById(R.id.editQty)).setOnEditorActionListener(new EditText.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					doCount();
					return true;
				}
				return false;
			}
		});

		loadData();
		refreshLanguage();
	}

	private void doFilter()
	{
		filter = !filter;
		if(filter)
		{
			((ImageView)mRootView.findViewById(R.id.btnFilter)).setImageDrawable(mContext.getResources().getDrawable(R.drawable.icon_filter_fill));
		}
		else
		{
			((ImageView)mRootView.findViewById(R.id.btnFilter)).setImageDrawable(mContext.getResources().getDrawable(R.drawable.icon_filter));
		}

		/*((EditText) mRootView.findViewById(R.id.editItemNo)).setText("");
		((EditText) mRootView.findViewById(R.id.editQty)).setText("");

		focusAndHideKeyboard((EditText) mRootView.findViewById(R.id.editItemNo));*/
		refreshPage();
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
		if(!bLoaded)
		{
			mProdDetlItemMap.clear();
			mProdDetlUPCMap.clear();
			DbAdapter dbAdapter = new DbAdapter(mContext);
			dbAdapter.open();
			mProdDetlList.clear();
			mProdDetlList = dbAdapter.getProductList(mProdDetlItemMap, mProdDetlUPCMap);
			mPriceInfoMap = dbAdapter.getPricingInfo(Global.gCurCustomerInfo.nCustNo);
//			if(Utils.getStringSetting(mContext, Global.PREF_ITEM_CODE_DISPLAY_SOURCE).equals("PRODINFO"))
//			{
//				mProdInfoMap = dbAdapter.getProdInfoMap("ItemCode");
//			}
			List<CustConBalInfo> custConBalInfoList = dbAdapter.getCustConBalInfo(Global.gCurCustomerInfo.nCustNo, Global.gCurCustomerInfo.nChainNo);
			dbAdapter.close();

			mUOrderDList.clear();
			if(Global.gUOrderHInfo != null)
			{
				mUOrderDList.addAll(Global.gUOrderHInfo.deliveryList);
			}
			else
			{
				for(CustConBalInfo info : custConBalInfoList)
				{
					UOrderDInfo uOrderDInfo = new UOrderDInfo();
					uOrderDInfo.nSales = 0;
					uOrderDInfo.nPlanQty = info.nQty;
					uOrderDInfo.dPlanQty = info.fWeight;
					uOrderDInfo.dSales = 0;
					uOrderDInfo.strItemNo = info.strItemNo;
					uOrderDInfo.nTrxnType = Global.TRXN_DELIVER;
					ProdDetlInfo productInfo = mProdDetlItemMap.get(info.strItemNo);
					if(productInfo != null)
					{
						uOrderDInfo.strProdGroup = productInfo.strProdGrp;
						uOrderDInfo.strShortDesc = productInfo.strShortDesc;
						uOrderDInfo.strDescription = productInfo.strDescription;
						if(productInfo.prodLocnInfo != null)
						{
							uOrderDInfo.nCaseFactor = productInfo.prodLocnInfo.nCaseFactor;
							uOrderDInfo.nBasePrice = Utils.getDividedValue(productInfo.prodLocnInfo.nBasePrice);
							uOrderDInfo.nOrgPrice = productInfo.nOrgPrice;
							uOrderDInfo.nGrossPrice = productInfo.nGrossPrice;
							uOrderDInfo.nOrgGrossPrice = productInfo.nOrgGrossPrice;

							uOrderDInfo.nLocnNo = productInfo.prodLocnInfo.nLocnNo;
							PricingInfo pricingInfo = mPriceInfoMap.get(uOrderDInfo.strItemNo);
							if(pricingInfo != null)
							{
								uOrderDInfo.nPrice = Utils.getDividedValue(pricingInfo.nPrice);
							}
							else
							{
								uOrderDInfo.nPrice = productInfo.nPrice;
							}
							uOrderDInfo.nDiscountVal = uOrderDInfo.nBasePrice - uOrderDInfo.nPrice;
						}
					}
					mUOrderDList.add(uOrderDInfo);
				}
			}
		}
		bLoaded = true;
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

	public void SortData(List<UOrderDInfo> uOrderDInfoList)
	{
		Collections.sort(uOrderDInfoList, (t1, t2) -> {
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
		List<UOrderDInfo> uOrderDInfoList = new ArrayList<>();
		if(filter)
		{
			for(UOrderDInfo info : mUOrderDList)
			{
				if(info.dSales != 0 || info.nSales != 0)
				{
					uOrderDInfoList.add(info);
				}
			}
		}
		else
		{
			uOrderDInfoList.addAll(mUOrderDList);
		}

		SortData(uOrderDInfoList);

		if (uOrderDInfoList.size() > 0) {
			mListView.setVisibility(View.VISIBLE);
			mTxtNoData.setVisibility(View.GONE);
			mListAdapter.setListData(uOrderDInfoList);
			mListAdapter.notifyDataSetChanged();
		} else {
			mListView.setVisibility(View.GONE);
			mTxtNoData.setVisibility(View.VISIBLE);
		}
		refreshCurrentStatus();
	}

	private void refreshCurrentStatus()
	{
		boolean bExistWeightItem = false;
		boolean bExitNormalItem = false;
		float fTotalWeight = 0;
		int nTotalQty = 0;
		float fSoldWeight = 0;
		int nSoldQty = 0;

		for(UOrderDInfo uOrderDInfo : mUOrderDList)
		{
			if(Utils.getProdLoconSCWType(mContext, uOrderDInfo.strItemNo) == Global.WEIGHT_ITEM)
			{
				fTotalWeight += Utils.parseFloatFromString(String.format(Locale.ENGLISH, "%.2f", uOrderDInfo.dSales));
				fSoldWeight += Utils.parseFloatFromString(String.format(Locale.ENGLISH, "%.2f", uOrderDInfo.dPlanQty - uOrderDInfo.dSales));
				bExistWeightItem = true;
			}
			else
			{
				nTotalQty += uOrderDInfo.nSales;
				nSoldQty += uOrderDInfo.nPlanQty - uOrderDInfo.nSales;
				bExitNormalItem = true;
			}
		}

		if(bExistWeightItem && bExitNormalItem)
		{
			mRootView.findViewById(R.id.layoutCarton).setVisibility(View.VISIBLE);
			mRootView.findViewById(R.id.layoutWeight).setVisibility(View.VISIBLE);
			mTxtWeight.setText(String.format(Locale.ENGLISH, "%.2f", fTotalWeight));
			mTxtCartons.setText(String.valueOf(nTotalQty));
			mTxtRunningQty.setVisibility(View.VISIBLE);
			mTxtRunningQty.setText(Str.TotalSellThroughQuantity[Global.LANG] + " " + String.valueOf(nSoldQty) + "\n" +
					Str.TotalSellThroughWeight[Global.LANG] + " " + String.format(Locale.ENGLISH, "%.2f", fSoldWeight));
		}
		else if(bExistWeightItem)
		{
			mRootView.findViewById(R.id.layoutCarton).setVisibility(View.GONE);
			mRootView.findViewById(R.id.layoutWeight).setVisibility(View.VISIBLE);
			mTxtWeight.setText(String.format(Locale.ENGLISH, "%.2f", fTotalWeight));
			mTxtRunningQty.setVisibility(View.VISIBLE);
			mTxtRunningQty.setText(Str.TotalSellThroughWeight[Global.LANG] + " " + String.format(Locale.ENGLISH, "%.2f", fSoldWeight));
		}
		else if(bExitNormalItem)
		{
			mRootView.findViewById(R.id.layoutCarton).setVisibility(View.VISIBLE);
			mRootView.findViewById(R.id.layoutWeight).setVisibility(View.GONE);
			mTxtCartons.setText(String.valueOf(nTotalQty));
			mTxtRunningQty.setVisibility(View.VISIBLE);
			mTxtRunningQty.setText(Str.TotalSellThroughQuantity[Global.LANG] + " " + String.valueOf(nSoldQty));
		}
		else
		{
			mRootView.findViewById(R.id.layoutCarton).setVisibility(View.GONE);
			mRootView.findViewById(R.id.layoutWeight).setVisibility(View.GONE);
			mTxtRunningQty.setVisibility(View.GONE);
		}
	}

	@Override
	public void refreshLanguage() {
		((TextView) mRootView.findViewById(R.id.lblTitle)).setText(Str.SalesCount[Global.LANG].toUpperCase());
		((TextView) mRootView.findViewById(R.id.txtCustomerName)).setText(Global.gCurCustomerInfo.strName);
		((TextView) mRootView.findViewById(R.id.txtDescription)).setText("");
		((Button) mRootView.findViewById(R.id.btnCompleted)).setText(Str.Completed[Global.LANG]);
		((Button) mRootView.findViewById(R.id.btnCount)).setText(Str.Count[Global.LANG]);
		((TextView) mRootView.findViewById(R.id.lblCartons)).setText(Str.TotalCount[Global.LANG]);
		((TextView) mRootView.findViewById(R.id.lblWeight)).setText(Str.TotalWeight[Global.LANG]);

		mTxtNoData.setText(Str.No_data_has_been_found[Global.LANG]);
		refreshCurrentStatus();
	}

	private void doProcessSelectProduct(String strItemNo, int nType, boolean isSearchByItemNo)
	{
		((EditText) mRootView.findViewById(R.id.editItemNo)).setText(strItemNo);
		doSearch(nType, isSearchByItemNo);
		showKeyboard(((EditText) mRootView.findViewById(R.id.editQty)), true);
	}

	@Override
	public void receiveBroadcast(String strAction, Intent intent) {
		boolean bUnit = false;
		if (strAction.equals(Global.ACTION_SELECT_PRODUCT) == true) {
			int nType = intent.getIntExtra(Global.EXTRA_TYPE, 0);

			String strItemNo = "";
			if (nType == Global.SELECT_PRODUCT_ITEMNO) {
				strItemNo = intent.getStringExtra(Global.EXTRA_ITEM_NO);
				doProcessSelectProduct(strItemNo, 1, true);

			}
			else if (nType == Global.SELECT_PRODUCT_ITEMUPC) {
				String strFrom = intent.getStringExtra(Global.EXTRA_BARCODESCAN_FROM);
				if (!strFrom.equals(getClass().getSimpleName())) {
					return;
				}
				String strItemUPC = intent.getStringExtra(Global.EXTRA_ITEM_UPC);
				ProdDetlInfo info = mProdDetlUPCMap.get(strItemUPC);
				if(info != null)
				{
					strItemNo = info.strItemNo;
					if(Utils.getStringSetting(mContext, Global.PREF_AUTO_SCAN_ADD).equals("1"))
					{
						doProcessSelectProduct(strItemNo, 4, false);
						doCount();
					}
					else
					{
						doProcessSelectProduct(strItemNo, 2, false);
					}

				}
			}
		}
	}

	private void doSearch(int nType, boolean isByItemNo) {
		String strItemNo = getItemCode(((EditText) mRootView.findViewById(R.id.editItemNo)).getText().toString());
		String strDescription = Str.Invalid_Product[Global.LANG];

		mSelProductInfo = null;

		if (strItemNo.length() > 0) {
			if (isByItemNo) {
				for (int i = 0; i < mProdDetlList.size(); i++) {
					ProdDetlInfo prodDetlInfo = mProdDetlList.get(i);
					if (prodDetlInfo.strItemNo.equals(strItemNo)) {
						mSelProductInfo = prodDetlInfo;
						strItemNo = prodDetlInfo.strItemNo;
						strDescription = prodDetlInfo.strDescription;
						break;
					}
				}
			}
			if (mSelProductInfo == null) {
				for (int i = 0; i < mProdDetlList.size(); i++) {
					ProdDetlInfo prodDetlInfo = mProdDetlList.get(i);
					if (Utils.getDisplayItemCode(mContext, prodDetlInfo.strItemNo).equals(strItemNo)) {
						mSelProductInfo = prodDetlInfo;
						strItemNo = prodDetlInfo.strItemNo;
						strDescription = prodDetlInfo.strDescription;
						break;
					}
				}
			}
			if (mSelProductInfo == null) {
				for (int i = 0; i < mProdDetlList.size(); i++) {
					ProdDetlInfo prodDetlInfo = mProdDetlList.get(i);
					if (prodDetlInfo.strItemUPC.equals(strItemNo)) {
						mSelProductInfo = prodDetlInfo;
						strItemNo = prodDetlInfo.strItemNo;
						strDescription = prodDetlInfo.strDescription;
						break;
					}
				}
			}
			if (mSelProductInfo == null) {
				for (int i = 0; i < mProdDetlList.size(); i++) {
					ProdDetlInfo prodDetlInfo = mProdDetlList.get(i);
					if (prodDetlInfo.strItemUPC20.equals(strItemNo)) {
						mSelProductInfo = prodDetlInfo;
						strItemNo = prodDetlInfo.strItemNo;
						strDescription = prodDetlInfo.strDescription;
						break;
					}
				}
			}

			String strQty  = "";
			for (UOrderDInfo info : mUOrderDList) {
				if (info.strItemNo.equals(strItemNo) == true) {
					if(Utils.getProdLoconSCWType(mContext, strItemNo) == Global.WEIGHT_ITEM)
					{
						if(nType == 4)
						{
							strQty = String.format(Locale.ENGLISH, "%.2f", info.dSales + 1.0f);
						}
						else
						{
							strQty = String.format(Locale.ENGLISH, "%.2f", info.dSales);
						}
					}
					else
					{
						if(nType == 4)
						{
							strQty = String.valueOf(info.nSales + 1);
						}
						else
						{
							strQty = String.valueOf(info.nSales);
						}
					}
					break;
				}
			}
			((TextView) mRootView.findViewById(R.id.txtDescription)).setText(strDescription);
			if(Utils.getProdLoconSCWType(mContext, strItemNo) == Global.WEIGHT_ITEM)
			{
				if(!TextUtils.isEmpty(strQty))
				{
					(mRootView.findViewById(R.id.editQty)).setTag("From App");
					((EditText) mRootView.findViewById(R.id.editQty)).setText(strQty);
					(mRootView.findViewById(R.id.editQty)).setTag(null);
				}
				else
				{
					(mRootView.findViewById(R.id.editQty)).setTag("From App");
					if(nType == 0 || nType == 1 || nType == 3)
					{
						((EditText) mRootView.findViewById(R.id.editQty)).setText("0.00");
					}
					else if(nType == 2 || nType == 4)
					{
						((EditText) mRootView.findViewById(R.id.editQty)).setText("1.00");
					}
					(mRootView.findViewById(R.id.editQty)).setTag(null);
				}
			}
			else
			{
				int nQty = 0;
				try
				{
					nQty = Integer.parseInt(strQty);
				}catch(Exception e)
				{
					e.printStackTrace();
				}
				if(!TextUtils.isEmpty(strQty))
				{
					((EditText) mRootView.findViewById(R.id.editQty)).setText(String.valueOf(nQty));
				}
				else
				{
					if(nType == 0 || nType == 1 || nType == 3)
					{
						((EditText) mRootView.findViewById(R.id.editQty)).setText(String.valueOf(0));
					}
					else
					{
						((EditText) mRootView.findViewById(R.id.editQty)).setText(String.valueOf(1));
					}
				}
			}
		}
		else
		{
			((TextView) mRootView.findViewById(R.id.txtDescription)).setText("");
			((EditText) mRootView.findViewById(R.id.editQty)).setText("0");
		}
	}

	private void doCount() {
		String strQty = ((EditText) mRootView.findViewById(R.id.editQty)).getText().toString();
		if (mSelProductInfo == null) {
			if(!TextUtils.isEmpty(((EditText) mRootView.findViewById(R.id.editItemNo)).getText().toString()))
			{
				Utils.printDebug(mContext, Str.Please_select_valid_product[Global.LANG]);
			}
			((EditText) mRootView.findViewById(R.id.editQty)).setText("");
			focusAndHideKeyboard((EditText) mRootView.findViewById(R.id.editItemNo));
			return;
		}
		if( Utils.getProdLoconSCWType(mContext, mSelProductInfo.strItemNo) != Global.WEIGHT_ITEM)
		{
			if (strQty.length() == 0) {
				strQty = "0";
				((EditText) mRootView.findViewById(R.id.editQty)).setText(strQty);
			}
		}

		int nIdx = -1;
		for (int i = 0; i < mUOrderDList.size(); i++) {
			if (mUOrderDList.get(i).strItemNo.equals(mSelProductInfo.strItemNo) == true) {
				nIdx = i;
				break;
			}
		}

		if(nIdx == -1)
		{
			try {
				final MessageDialog msgDialog = new MessageDialog(mContext);
				msgDialog.setCancelable(false);
				msgDialog.mStrMessage = Str.There_is_no_stock_for_this_item[Global.LANG];
				msgDialog.mStrRight = Str.Ok[Global.LANG];
				msgDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
					}
				});
				msgDialog.show();
			}catch (Exception e)
			{
				e.printStackTrace();
			}
			return;
		}


		if(Utils.getProdLoconSCWType(mContext, mSelProductInfo.strItemNo) == Global.WEIGHT_ITEM)
		{
			final double dQuantity = Utils.parseDoubleFromString(strQty);
			if(nIdx == -1)
			{
				UOrderDInfo info = new UOrderDInfo();
				info.strItemNo = mSelProductInfo.strItemNo;
				info.dSales = dQuantity;
				info.strShortDesc = mSelProductInfo.strShortDesc;
				info.strDescription = mSelProductInfo.strDescription;
				info.strProdGroup = mSelProductInfo.strProdGrp;
				info.nTrxnType = Global.TRXN_DELIVER;
				if(mSelProductInfo.prodLocnInfo != null)
				{
					info.nBasePrice = Utils.getDividedValue(mSelProductInfo.prodLocnInfo.nBasePrice);
					info.nOrgPrice = mSelProductInfo.nOrgPrice;
					info.nGrossPrice = mSelProductInfo.nGrossPrice;
					info.nOrgGrossPrice = mSelProductInfo.nOrgGrossPrice;
					info.nCaseFactor = mSelProductInfo.prodLocnInfo.nCaseFactor;
					info.nLocnNo = mSelProductInfo.prodLocnInfo.nLocnNo;
					PricingInfo pricingInfo = mPriceInfoMap.get(info.strItemNo);
					if(pricingInfo != null)
					{
						info.nPrice = Utils.getDividedValue(pricingInfo.nPrice);
					}
					else
					{
						info.nPrice = info.nBasePrice;
					}
					info.nDiscountVal = info.nBasePrice - info.nPrice;
				}

				mUOrderDList.add(info);
			}
			else
			{
				if(mUOrderDList.get(nIdx).dPlanQty < dQuantity)
				{
					try {
						final MessageDialog msgDialog = new MessageDialog(mContext);
						msgDialog.setCancelable(false);
						msgDialog.mStrMessage = Str.There_is_less_stock_for_this_item[Global.LANG];
						msgDialog.mStrLeft = Str.Return[Global.LANG];
						msgDialog.mStrRight = Str.Proceed[Global.LANG];
						final int finalNIdx = nIdx;
						msgDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
							@Override
							public void onDismiss(DialogInterface dialog) {
								if(msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT)
								{
									mUOrderDList.get(finalNIdx).dSales = dQuantity;
									((EditText) mRootView.findViewById(R.id.editItemNo)).setText("");
									((EditText) mRootView.findViewById(R.id.editQty)).setText("");

									focusAndHideKeyboard((EditText) mRootView.findViewById(R.id.editItemNo));

									refreshPage();
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
				mUOrderDList.get(nIdx).dSales = dQuantity;
			}
		}
		else
		{
			final int nQuantity ;
			nQuantity = Utils.parseIntFromString(strQty);
			if (nIdx == -1) {
				if (nQuantity == 0) {
					Utils.printDebug(mContext, Str.Please_input_the_Quantity[Global.LANG]);
					showKeyboard((EditText) mRootView.findViewById(R.id.editQty), true);
					return;
				} else {
					UOrderDInfo info = new UOrderDInfo();
					info.strItemNo = mSelProductInfo.strItemNo;
					info.strProdGroup = mSelProductInfo.strProdGrp;
					info.nSales = nQuantity;
					info.strShortDesc = mSelProductInfo.strShortDesc;
					info.strDescription = mSelProductInfo.strDescription;
					info.nTrxnType = Global.TRXN_DELIVER;

					if(mSelProductInfo.prodLocnInfo != null)
					{
						info.nBasePrice = Utils.getDividedValue(mSelProductInfo.prodLocnInfo.nBasePrice);
						info.nCaseFactor = mSelProductInfo.prodLocnInfo.nCaseFactor;
						info.nOrgPrice = mSelProductInfo.nOrgPrice;
						info.nGrossPrice = mSelProductInfo.nGrossPrice;
						info.nOrgGrossPrice = mSelProductInfo.nOrgGrossPrice;
						info.nLocnNo = mSelProductInfo.prodLocnInfo.nLocnNo;

						PricingInfo pricingInfo = mPriceInfoMap.get(info.strItemNo);
						if(pricingInfo != null)
						{
							info.nPrice = Utils.getDividedValue(pricingInfo.nPrice);
						}
						else
						{
							info.nPrice = info.nBasePrice;
						}
						info.nDiscountVal = info.nBasePrice - info.nPrice;
					}
					mUOrderDList.add(info);
				}
			} else {
				if(mUOrderDList.get(nIdx).nPlanQty < nQuantity)
				{
					try {
						final MessageDialog msgDialog = new MessageDialog(mContext);
						msgDialog.setCancelable(false);
						msgDialog.mStrMessage = Str.There_is_less_stock_for_this_item[Global.LANG];
						msgDialog.mStrLeft = Str.Return[Global.LANG];
						msgDialog.mStrRight = Str.Proceed[Global.LANG];
						final int finalNIdx = nIdx;
						msgDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
							@Override
							public void onDismiss(DialogInterface dialog) {
								if(msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT)
								{
									mUOrderDList.get(finalNIdx).nSales = nQuantity;
									((EditText) mRootView.findViewById(R.id.editItemNo)).setText("");
									((EditText) mRootView.findViewById(R.id.editQty)).setText("");

									focusAndHideKeyboard((EditText) mRootView.findViewById(R.id.editItemNo));

									refreshPage();
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
				mUOrderDList.get(nIdx).nSales = nQuantity;
			}
		}

		((EditText) mRootView.findViewById(R.id.editItemNo)).setText("");
		((EditText) mRootView.findViewById(R.id.editQty)).setText("");

		focusAndHideKeyboard((EditText) mRootView.findViewById(R.id.editItemNo));

		refreshPage();
	}

	private void doCompleted() {
		Global.gUOrderHInfo = new UOrderHInfo();
		Global.gUOrderHInfo.strDeliveryDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(new Date());
		Global.gUOrderHInfo.deliveryList = mUOrderDList;

		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		for (int i = 0; i < Global.gUOrderHInfo.deliveryList.size(); i++) {
			UOrderDInfo info = Global.gUOrderHInfo.deliveryList.get(i);
			ProdLocnInfo prodLocnInfo = dbAdapter.getProdLocnInfo(info.strItemNo);
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
			float dRealQty = 0;
			if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
			{
				dRealQty = (float)(info.dPlanQty - info.dSales);
			}
			else
			{
				dRealQty = info.nPlanQty - info.nSales;
			}
			for(TaxRatesInfo taxRatesInfo : taxRatesInfoList)
			{
				float nTaxAmount = 0;
				float nPrintTaxAmount = 0;
				float nLinePrinceTaxAmount = 0;
				if(taxRatesInfo.nTaxMethod == 1) //Applying Cents Amount
				{
					nTaxAmount = Utils.getDividedValue(taxRatesInfo.nTaxRate) * dRealQty * prodDetlInfo.nConsumerUnit ;
					nLinePrinceTaxAmount = Utils.getDividedValue(taxRatesInfo.nTaxRate) * prodDetlInfo.nConsumerUnit ;
				}
				else if( taxRatesInfo.nTaxMethod == 2 )
				{
					nTaxAmount = (Utils.getRoundedValue(dRealQty * info.nPrice) + info.nTaxAmount) / 100 * Utils.getDividedValue(taxRatesInfo.nTaxRate);
					nLinePrinceTaxAmount = (Utils.getRoundedValue(info.nBasePrice) + info.nPrintLineTaxAmount) / 100 * Utils.getDividedValue(taxRatesInfo.nTaxRate);
				}

				if(taxRatesInfo.strTaxType.equals("S"))
				{
					ProdLocnInfo locnInfo = dbAdapter.getProdLocnInfo(taxRatesInfo.strTaxRateNumber);
					List<TaxRatesInfo> gstOnCDSRatesInfoList = dbAdapter.getTaxRatesInfoList(Global.gCurCustomerInfo.strTaxCode, taxRatesInfo.strTaxRateNumber);
					for(TaxRatesInfo gstOnCDSRatesInfo : gstOnCDSRatesInfoList)
					{
						if(gstOnCDSRatesInfo.nTaxMethod == 2 && gstOnCDSRatesInfo.strTaxRateCode.equals("GST") )
						{
							float nGSTOnCDSTaxAmount = Utils.getDividedValue(taxRatesInfo.nTaxRate) / 100 * Utils.getDividedValue(gstOnCDSRatesInfo.nTaxRate) * dRealQty * prodDetlInfo.nConsumerUnit;
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
				info.nPrintLineTaxAmount += Utils.get5RoundedValue(mContext, nLinePrinceTaxAmount);

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
							cdsAmount.nBottles = (int)(dRealQty * prodDetlInfo.nConsumerUnit);
							cdsAmount.strTaxCode = taxRatesInfo.strTaxRateCode;
							cdsAmount.price = Utils.getDividedValue(taxRatesInfo.nTaxRate);
							cdsAmount.strRateDescription = taxRatesInfo.strRateDescription;

							Global.gUOrderHInfo.cdsMap.put(taxRatesInfo.strTaxRateCode, cdsAmount);
						}
						else
						{
							cdsAmount.nBottles += (int)(dRealQty * prodDetlInfo.nConsumerUnit);
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
		dbAdapter.close();
		//mContext.replaceFragment(new ConsignmentStockCountSummaryFragment());
		if(Utils.getStringSetting(mContext, Global.PREF_PRINT_PICK_LIST).equals("2"))
		{
			Fragment fragment = new PickingFragment();
			mContext.replaceFragment(fragment);
		}
		else {
			Fragment fragment = new DeliverySummaryFragment();
			Bundle bundle = new Bundle();
			bundle.putBoolean(Global.EXTRA_CONSIGNMENT, true);
			fragment.setArguments(bundle);
			mContext.replaceFragment(fragment);
		}
	}

	private void showInvenDetail(UOrderDInfo info) {
		((EditText) mRootView.findViewById(R.id.editItemNo)).setText(Utils.getDisplayItemCode(mContext, info.strItemNo));
		if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
		{
			double dValue = info.dSales;
			String strQty = String.format(Locale.ENGLISH, "%.2f", dValue);

			mRootView.findViewById(R.id.editQty).setTag("From App");
			((EditText) mRootView.findViewById(R.id.editQty)).setText(strQty);
			mRootView.findViewById(R.id.editQty).setTag(null);
			mRootView.findViewById(R.id.editQty).requestFocus();
			((EditText) mRootView.findViewById(R.id.editQty)).setSelection(strQty.length());
		}
		else
		{
			int nValue = info.nSales;

			String strQty = String.format(Locale.ENGLISH, "%d", nValue);
			((EditText) mRootView.findViewById(R.id.editQty)).setText(strQty);

			mRootView.findViewById(R.id.editQty).requestFocus();
			((EditText) mRootView.findViewById(R.id.editQty)).setSelection(strQty.length());
		}

	}

	boolean m_bClicked = false;
	@Override
	public void onClick(View v) {
		if( m_bClicked )
			return;
		m_bClicked = true;
		switch (v.getId()) {
			case R.id.btnBack:
				mContext.onBackPressed();
				break;
			case R.id.btnCount:
				doCount();
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
			case R.id.btnCompleted:
				doCompleted();
				break;
			case R.id.btnFilter:
				doFilter();
				break;
			case R.id.btnSort:
				doSort();
				break;
		}
		m_bClicked = false;
	}

	private String sortFlag = "code";

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

	public void doSort()
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
					.setPositiveButton(Str.Ok[Global.LANG], (dialog, nSelected) -> refreshPage())
					.setNegativeButton(Str.Cancel[Global.LANG], (dialog, nSelected) -> setSortFlag(Global.sortArray[nFinalOrigIdx]))
					.create()
					.show();
		}catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private class ListAdapter extends BaseAdapter {

		private List<UOrderDInfo> list = new ArrayList<>();

		private class ViewHolder {
			public TextView txtItemNo, txtShortDesc, txtQty, txtLoaded;
		}

		public int getCount() {
			return list.size();
		}

		public void setListData(List<UOrderDInfo> list) {
			this.list.clear();
			this.list.addAll(list);
		}

		public UOrderDInfo getItem(int position) {
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
			final UOrderDInfo info = getItem(position);
			if(info == null) return null;
			if (convertView == null) {
				view = mContext.getLayoutInflater().inflate(R.layout.row_consignment_stock_count, parent, false);
				holder = new ViewHolder();
				holder.txtItemNo = (TextView) view.findViewById(R.id.txtItemNo);
				holder.txtShortDesc = (TextView) view.findViewById(R.id.txtShortDesc);
				holder.txtQty = (TextView) view.findViewById(R.id.txtQty);
				holder.txtLoaded = (TextView) view.findViewById(R.id.txtLoaded);
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
				holder.txtQty.setText(String.format(Locale.ENGLISH, "%.2f", info.dSales));
				holder.txtLoaded.setText(String.format(Locale.ENGLISH, "%.2f", info.dPlanQty));
			}
			else
			{
				holder.txtQty.setText(String.format(Locale.ENGLISH, "%d", info.nSales));
				holder.txtLoaded.setText(String.format(Locale.ENGLISH, "%d", info.nPlanQty));
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
		if( mSelProductInfo != null )
		{
			if(Utils.getProdLoconSCWType(mContext, mSelProductInfo.strItemNo) == Global.WEIGHT_ITEM)
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

	@Override
	public float getProductPrice(String strItemNo) {

		for(UOrderDInfo uOrderDInfo : mUOrderDList)
		{
			if(uOrderDInfo.strItemNo.equals(strItemNo))
			{
				return uOrderDInfo.nPrice;
			}
		}

		ProdDetlInfo prodDetlInfo = mProdDetlItemMap.get(strItemNo);
		if(prodDetlInfo != null)
		{
			return prodDetlInfo.nPrice;
		}
		else
		{
			return 0;
		}
	}

}