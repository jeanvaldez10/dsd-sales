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
import dsd.dsdvansales.dialogs.BatchCodeInputDialog;
import dsd.dsdvansales.dialogs.MessageDialog;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.model.DescTypeInfo;
import dsd.dsdvansales.model.LoadBlotInfo;
import dsd.dsdvansales.model.ProdDetlInfo;
import dsd.dsdvansales.model.ProdInfo;
import dsd.dsdvansales.model.ProdStruInfo;
import dsd.dsdvansales.model.UInvenDInfo;
import dsd.dsdvansales.views.AlertSingleChoiceAdapter;

public class StockTakeCountEntryFragment extends BaseFragment implements View.OnClickListener {

	private TextView mTxtNoData = null;
	private TextView mTxtSlideNoData = null;
	private ListView mListView = null;
	private ListView mSlideListView = null;
	private ListAdapter mListAdapter = null;
	private SlideListAdapter mSlideListAdapter = null;
	private List<UInvenDInfo> mUInvenDList = new ArrayList<>();
	private UInvenDInfo mCurSelInvenDInfo = null;
	private List<ProdDetlInfo> mProdDetlList = new ArrayList<>();
	private Map<String, ProdDetlInfo> mProdUPCDetMap = new HashMap<>();
	private Map<String, ProdDetlInfo> mProdDetlItemMap = new HashMap<>();
	private Map<String, ProdInfo> mProdInfoMap = new HashMap<>();

	private int mProdDetlIdx = -1;

	private Map<String, Integer> mCaseMap = new HashMap<>();

	private List<DescTypeInfo> filterByList = new ArrayList<>();

	List<UInvenDInfo> mAllInvenDInfos = null;
	private boolean bLoaded = false;
	private boolean bShowCase = true;

	private String mEntryInputMethod = "";

	private String sortFlag = "";

	private boolean filter = false;

	public boolean bModified = false;

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
		mRootView = inflater.inflate(R.layout.fragment_adjust_inventory, container, false);
		commonInit();

		if(TextUtils.isEmpty(sortFlag))
		{
			sortFlag = Utils.getStringSetting(mContext, Global.PREF_UI_DETL_SORT);
			if(!Utils.isValidSortValue(sortFlag))
			{
				sortFlag = "code";
			}
			new Handler(Looper.getMainLooper()).post(() -> refreshPage());
		}

		return mRootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		Global.gFragIdx = Global.FRAG_STOCKTAKE_COUNT_ENTRY;
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void initView() {
		bShowCase = Utils.getStringSetting(mContext, Global.PREF_INVENTORYUOM).equals("U") == false;
		mRootView.findViewById(R.id.btnAdd).setOnClickListener(this);
		mRootView.findViewById(R.id.btnSort).setOnClickListener(this);
		mRootView.findViewById(R.id.btnFilter).setOnClickListener(this);
		if(!TextUtils.isEmpty(Utils.getStringSetting(mContext, Global.PREF_STKTKEGRP)))
		{
			mRootView.findViewById(R.id.btnFilterBy).setVisibility(View.VISIBLE);
			mRootView.findViewById(R.id.btnFilterBy).setOnClickListener(this);
		}

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
		mRootView.findViewById(R.id.btnDone).setOnClickListener(this);

		mTxtNoData = (TextView) mRootView.findViewById(R.id.txtNoData);
		mTxtSlideNoData = (TextView)mRootView.findViewById(R.id.txtSlideNoData);

		if(Utils.getIntSetting(mContext, Global.PREF_LOADSTKTAKE) == 2)
		{
			mListView = (ListView) mRootView.findViewById(R.id.slidelistView);
			mSlideListView = (ListView)mRootView.findViewById(R.id.listView);
		}
		else
		{
			mListView = (ListView) mRootView.findViewById(R.id.listView);
			mSlideListView = (ListView)mRootView.findViewById(R.id.slidelistView);
		}

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
		mSlideListAdapter = new SlideListAdapter();
		mSlideListView.setAdapter(mSlideListAdapter);
		mSlideListView.setOnScrollListener(new AbsListView.OnScrollListener() {
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

		if(bShowCase)
		{
			mRootView.findViewById(R.id.editCase).setVisibility(View.VISIBLE);
			mRootView.findViewById(R.id.txtCaseSplit).setVisibility(View.VISIBLE);
		}
		else
		{
			mRootView.findViewById(R.id.editCase).setVisibility(View.GONE);
			mRootView.findViewById(R.id.txtCaseSplit).setVisibility(View.GONE);
		}

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
				ProdDetlInfo prodDetlInfo = mProdUPCDetMap.get(strItemCode);
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
					doProcessSelectProduct(strItemNo, true, false, false);
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
					if(mProdDetlIdx != -1) {
						ProdDetlInfo detlInfo = mProdDetlList.get(mProdDetlIdx);
						if (Utils.getProdLoconSCWType(mContext, detlInfo.strItemNo) == Global.WEIGHT_ITEM) {
							showKeyboard(((EditText) mRootView.findViewById(R.id.editQty)), true);
							return true;
						}
					}
					if(bShowCase)
					{
						showKeyboard(((EditText) mRootView.findViewById(R.id.editCase)), true);
					}
					else
					{
						showKeyboard(((EditText) mRootView.findViewById(R.id.editQty)), true);
					}
					return true;
				}
				return false;
			}
		});

		if(bShowCase)
		{
			((EditText) mRootView.findViewById(R.id.editCase)).setOnEditorActionListener(new EditText.OnEditorActionListener() {
				@Override
				public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
					if (actionId == EditorInfo.IME_ACTION_NEXT) {
						showKeyboard(((EditText) mRootView.findViewById(R.id.editQty)), true);
						return true;
					}
					return false;
				}
			});
		}

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
			((EditText)mRootView.findViewById(R.id.editCase)).setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
			((EditText)mRootView.findViewById(R.id.editQty)).setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
		}
		else
		{
			((EditText)mRootView.findViewById(R.id.editCase)).setInputType(InputType.TYPE_CLASS_NUMBER);
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
		if(!bLoaded)
		{
			mContext.showProgressDialog(Str.Loading[Global.LANG]);
			new Thread(){
				@Override
				public void run() {
					mProdDetlList.clear();
					DbAdapter dbAdapter = new DbAdapter(mContext);
					dbAdapter.open();
					mProdDetlList = dbAdapter.getProdDetlList(mProdUPCDetMap);
					List<UInvenDInfo> uInvenDInfos = dbAdapter.getUInvenDList(Utils.getLongSetting(mContext, Global.PREF_UINVEN_TRXN_NO));
					dbAdapter.close();

					mUInvenDList.clear();

					mAllInvenDInfos = uInvenDInfos;
					String strStocktakeGrp = Utils.getStringSetting(mContext, Global.PREF_STKTKEGRP);
					if(Utils.getIntSetting(mContext, Global.PREF_LOADSTKTAKE) == 2)
					{
						String strStockRolGrp = Utils.getStringSetting(mContext, Global.PREF_STKROLGRP);
						String[] strKeys = strStockRolGrp.split(",");
						if(strKeys.length > 0)
						{
							dbAdapter.open();
							List<DescTypeInfo> descTypeInfoList = dbAdapter.getDescTypeList(strStocktakeGrp);
							for(DescTypeInfo info : descTypeInfoList)
							{
								for(String key : strKeys)
								{
									if(info.strDescription.equalsIgnoreCase(key))
									{
										filterByList.add(info);
										break;
									}
								}
							}
							dbAdapter.close();
						}
					}

					if(Utils.getIntSetting(mContext, Global.PREF_LOADSTKTAKE) == 2 && !TextUtils.isEmpty(strStocktakeGrp) && filterByList.size() > 0)
					{
						List<UInvenDInfo> uInvenDInfoList = filterByProc(uInvenDInfos);
						Map<String, UInvenDInfo> filterMap = new HashMap<>();
						for (UInvenDInfo uInvenDInfo : uInvenDInfoList) {
							filterMap.put(uInvenDInfo.strItemNo, uInvenDInfo);
						}

						for (UInvenDInfo uInvenDInfo : uInvenDInfos) {
							if( uInvenDInfo.bShow )
							{
								if(Utils.getProdLoconSCWType(mContext, uInvenDInfo.strItemNo) == Global.WEIGHT_ITEM)
								{
									if(filterMap.get(uInvenDInfo.strItemNo) != null)
									{
										uInvenDInfo.dAdjQty = 0;
									}
									else {
										uInvenDInfo.dAdjQty = uInvenDInfo.dStock;
									}
									mUInvenDList.add(uInvenDInfo);
								}
								else
								{
									if(filterMap.get(uInvenDInfo.strItemNo) != null)
									{
										uInvenDInfo.nAdjQty = 0;
									}
									else {
										uInvenDInfo.nAdjQty = uInvenDInfo.nStock;
									}
									mUInvenDList.add(uInvenDInfo);
								}
							}
						}
					}
					else {
						for (UInvenDInfo uInvenDInfo : uInvenDInfos) {
							if( uInvenDInfo.bShow )
							{
								if(Utils.getProdLoconSCWType(mContext, uInvenDInfo.strItemNo) == Global.WEIGHT_ITEM)
								{
									uInvenDInfo.dAdjQty = 0;
									mUInvenDList.add(uInvenDInfo);
								}
								else
								{
									uInvenDInfo.nAdjQty = 0;
									mUInvenDList.add(uInvenDInfo);
								}

							}
						}
					}


					Collections.sort(mUInvenDList, (lhs, rhs) -> lhs.strItemNo.compareTo(rhs.strItemNo));

					new Handler(Looper.getMainLooper()).post(() -> {
						mContext.hideProgressDialog();
						refreshPage();
					});
				}
			}.start();
		}
		bLoaded = true;
		refreshPage();
	}

	@Override
	public void refreshPage() {

		List<UInvenDInfo> uInvenDInfoList = new ArrayList<>();
		if(filter) {
			for(UInvenDInfo uInvenDInfo : mUInvenDList) {
				if(Utils.getProdLoconSCWType(mContext, uInvenDInfo.strItemNo) == Global.WEIGHT_ITEM) {
					if(uInvenDInfo.dAdjQty != 0) {
						uInvenDInfoList.add(uInvenDInfo);
					}
				} else {
					if(uInvenDInfo.nAdjQty != 0){
						uInvenDInfoList.add(uInvenDInfo);
					}
				}
			}
		}
		else {
			uInvenDInfoList.addAll(mUInvenDList);
		}

		//Processing filter By....
		String strStocktakeGrp = Utils.getStringSetting(mContext, Global.PREF_STKTKEGRP);
		if(!TextUtils.isEmpty(strStocktakeGrp) && filterByList.size() > 0)
		{
			uInvenDInfoList = filterByProc(uInvenDInfoList);
		}

		if (mUInvenDList.size() > 0) {
			mListView.setVisibility(View.VISIBLE);
			mSlideListView.setVisibility(View.VISIBLE);
			mTxtNoData.setVisibility(View.GONE);
			mTxtSlideNoData.setVisibility(View.GONE);
			mListAdapter.setListData(uInvenDInfoList);
			mSlideListAdapter.setListData(uInvenDInfoList);
			mListAdapter.notifyDataSetChanged();
			mSlideListAdapter.notifyDataSetChanged();
		} else {
			mListView.setVisibility(View.GONE);
			mSlideListView.setVisibility(View.GONE);
			mTxtNoData.setVisibility(View.VISIBLE);
			mTxtSlideNoData.setVisibility(View.VISIBLE);
		}
	}

	private List<UInvenDInfo> filterByProc(List<UInvenDInfo> uInvenDInfoList)
	{
		String strStocktakeGrp = Utils.getStringSetting(mContext, Global.PREF_STKTKEGRP);
		if(!TextUtils.isEmpty(strStocktakeGrp) && filterByList.size() > 0) {
			List<UInvenDInfo> newUInvenDInfoList = new ArrayList<>();
			for(UInvenDInfo uInvenDInfo : uInvenDInfoList)
			{
				boolean bExist = false;
				String strGrpValue = "";
				ProdDetlInfo prodDetlInfo = mProdUPCDetMap.get(uInvenDInfo.strItemNo);
				if(prodDetlInfo != null)
				{
					if(strStocktakeGrp.equals("Brand"))
					{
						strGrpValue = prodDetlInfo.strBrand;
					}
					else if(strStocktakeGrp.equals("ItemType"))
					{
						strGrpValue = prodDetlInfo.strItemType;
					}
					else if(strStocktakeGrp.equals("MarketGroup"))
					{
						strGrpValue = prodDetlInfo.strMarketGrp;
					}
					else if(strStocktakeGrp.equals("ProductGroup"))
					{
						strGrpValue = prodDetlInfo.strProdGrp;
					}
					else if(strStocktakeGrp.equals("ProductLine"))
					{
						strGrpValue = prodDetlInfo.strProdLine;
					}
					else if(strStocktakeGrp.equals("PromoGrp"))
					{
						strGrpValue = prodDetlInfo.strPromoGrp;
					}
					else if(strStocktakeGrp.equals("SubBrand"))
					{
						strGrpValue = prodDetlInfo.strSubBrand;
					}

					if(!TextUtils.isEmpty(strGrpValue))
					{
						for(DescTypeInfo descTypeInfo : filterByList)
						{
							if(strGrpValue.equals(descTypeInfo.strAlphaKey))
							{
								bExist = true;
								break;
							}
						}
					}

					if(bExist)
					{
						newUInvenDInfoList.add(uInvenDInfo);
					}
				}
			}

			return newUInvenDInfoList;
		}
		else {
			return uInvenDInfoList;
		}


	}

	@Override
	public void refreshLanguage() {
		((TextView) mRootView.findViewById(R.id.lblTitle)).setText(Str.StockTakeCount[Global.LANG].toUpperCase());
		((TextView) mRootView.findViewById(R.id.txtTrip)).setText(Str.Trip[Global.LANG] + " # " + Global.gTripNumber);
		((TextView) mRootView.findViewById(R.id.txtDescription)).setText("");
		((Button) mRootView.findViewById(R.id.btnDone)).setText(Str.Done[Global.LANG]);
		((Button) mRootView.findViewById(R.id.btnAdd)).setText(Str.Add[Global.LANG]);
		mTxtNoData.setText(Str.No_data_has_been_found[Global.LANG]);
		mTxtSlideNoData.setText(Str.No_data_has_been_found[Global.LANG]);
	}

	private void doProcessSelectProduct(final String strItemNo, boolean bUnit, boolean bUpdateItemNo, boolean isSearchByItemNo)
	{
		if (strItemNo.length() == 0) {
			Utils.printDebug(mContext, Str.NoProductFound[Global.LANG]);
		} else {
			if(bUpdateItemNo || !Utils.getStringSetting(mContext, Global.PREF_AUTO_SCAN_ADD).equals("1"))
			{
				if(bShowCase)
				{
					mRootView.findViewById(R.id.txtCaseSplit).setVisibility(View.VISIBLE);
					(mRootView.findViewById(R.id.editCase)).setVisibility(View.VISIBLE);
					//if(bUpdateItemNo)
					{
						((EditText) mRootView.findViewById(R.id.editItemNo)).setText(strItemNo);
					}
					((EditText) mRootView.findViewById(R.id.editCase)).setText("0");
					((EditText) mRootView.findViewById(R.id.editQty)).setText("0");
					this.showKeyboard((EditText) mRootView.findViewById(R.id.editCase), true);
				}
				else
				{
					mRootView.findViewById(R.id.txtCaseSplit).setVisibility(View.GONE);
					(mRootView.findViewById(R.id.editCase)).setVisibility(View.GONE);
					//if(bUpdateItemNo)
					{
						((EditText) mRootView.findViewById(R.id.editItemNo)).setText(strItemNo);
					}
					((EditText) mRootView.findViewById(R.id.editQty)).setText("0");
					this.showKeyboard((EditText) mRootView.findViewById(R.id.editQty), true);
				}
			}

		}
		doSearch(isSearchByItemNo);
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
		else
		{
			if(bShowCase)
			{
				if( bUnit )
				{
					showKeyboard(((EditText) mRootView.findViewById(R.id.editQty)), true);
				}
				else
				{
					showKeyboard(((EditText) mRootView.findViewById(R.id.editCase)), true);
				}
			}
			else
			{
				showKeyboard(((EditText) mRootView.findViewById(R.id.editQty)), true);
			}
		}

	}

	@Override
	public void receiveBroadcast(String strAction, Intent intent) {
		boolean bUnit = false;
		if (strAction.equals(Global.ACTION_SELECT_PRODUCT) == true) {
			int nType = intent.getIntExtra(Global.EXTRA_TYPE, 0);
			String strItemNo = "";
			if (nType == Global.SELECT_PRODUCT_ITEMNO) {
				strItemNo = intent.getStringExtra(Global.EXTRA_ITEM_NO);
				doProcessSelectProduct(strItemNo, bUnit, false, true);
			} else if (nType == Global.SELECT_PRODUCT_ITEMUPC) {
				String strFrom = intent.getStringExtra(Global.EXTRA_BARCODESCAN_FROM);
				if (!strFrom.equals(getClass().getSimpleName())) {
					return;
				}
				bUnit = true;
				String strItemUPC = intent.getStringExtra(Global.EXTRA_ITEM_UPC);
				for (ProdDetlInfo info : mProdDetlList) {
					if (info.strItemUPC.equals(strItemUPC) == true || info.strItemUPC20.equals(strItemUPC) == true) {
						strItemNo = info.strItemNo;
						break;
					}
				}
				((EditText) mRootView.findViewById(R.id.editItemNo)).setText(strItemUPC);
				doProcessSelectProduct(strItemNo, bUnit, false, false);
			}

		} else if(strAction.equals(Global.ACTION_FILTER_LOCATION) == true) {
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
		mProdDetlIdx = -1;
		if (strItemNo.length() > 0) {
			if (isByItemNo) {
				for (int i = 0; i < mProdDetlList.size(); i++) {
					ProdDetlInfo prodDetlInfo = mProdDetlList.get(i);
					if (prodDetlInfo.strItemNo.equals(strItemNo)) {
						mProdDetlIdx = i;
						strItemNo = prodDetlInfo.strItemNo;
						strDescription = prodDetlInfo.strDescription;
						break;
					}
				}
			}
			if (mProdDetlIdx == -1) {
				for (int i = 0; i < mProdDetlList.size(); i++) {
					ProdDetlInfo prodDetlInfo = mProdDetlList.get(i);
					if (Utils.getDisplayItemCode(mContext, prodDetlInfo.strItemNo).equals(strItemNo)) {
						mProdDetlIdx = i;
						strItemNo = prodDetlInfo.strItemNo;
						strDescription = prodDetlInfo.strDescription;
						break;
					}
				}
			}
			if (mProdDetlIdx == -1) {
				for (int i = 0; i < mProdDetlList.size(); i++) {
					ProdDetlInfo prodDetlInfo = mProdDetlList.get(i);
					if (prodDetlInfo.strItemUPC.equals(strItemNo)) {
						mProdDetlIdx = i;
						strItemNo = prodDetlInfo.strItemNo;
						strDescription = prodDetlInfo.strDescription;
						break;
					}
				}
			}
			if (mProdDetlIdx == -1) {
				for (int i = 0; i < mProdDetlList.size(); i++) {
					ProdDetlInfo prodDetlInfo = mProdDetlList.get(i);
					if (prodDetlInfo.strItemUPC20.equals(strItemNo)) {
						mProdDetlIdx = i;
						strItemNo = prodDetlInfo.strItemNo;
						strDescription = prodDetlInfo.strDescription;
						break;
					}
				}
			}

			String strQty  = "";
			for (UInvenDInfo info : mAllInvenDInfos) {
				if (info.strItemNo.equals(strItemNo) == true) {
					if(Utils.getProdLoconSCWType(mContext, strItemNo) == Global.WEIGHT_ITEM)
					{
						strQty = String.format(Locale.ENGLISH, "%.2f", info.dAdjQty);
					}
					else
					{
						strQty = String.valueOf(info.nAdjQty);
					}

					break;
				}
			}
			((TextView) mRootView.findViewById(R.id.txtDescription)).setText(strDescription);
			if(Utils.getProdLoconSCWType(mContext, strItemNo) == Global.WEIGHT_ITEM)
			{
				(mRootView.findViewById(R.id.editCase)).setVisibility(View.GONE);
				(mRootView.findViewById(R.id.txtCaseSplit)).setVisibility(View.GONE);
				(mRootView.findViewById(R.id.editQty)).setTag("From App");
				if(mEntryInputMethod.equals("A"))
				{
					((EditText) mRootView.findViewById(R.id.editQty)).setText("");
				}
				else
				{
					((EditText) mRootView.findViewById(R.id.editQty)).setText(strQty);
				}
				(mRootView.findViewById(R.id.editQty)).setTag(null);
			}
			else
			{
				if(bShowCase)
				{
					int nCase = getCaseValue(strItemNo);
					if( nCase == 0 ) nCase = 1;
					int nQty = 0;
					try
					{
						nQty = Integer.parseInt(strQty);
					}catch(Exception e)
					{
						e.printStackTrace();
					}
					int nCaseValue = nQty / nCase;
					nQty = nQty % nCase;
					(mRootView.findViewById(R.id.editCase)).setVisibility(View.VISIBLE);
					(mRootView.findViewById(R.id.txtCaseSplit)).setVisibility(View.VISIBLE);
					if(mEntryInputMethod.equals("A"))
					{
						((EditText) mRootView.findViewById(R.id.editCase)).setText("");
						((EditText) mRootView.findViewById(R.id.editQty)).setText("");
					}
					else
					{
						((EditText) mRootView.findViewById(R.id.editCase)).setText(String.valueOf(nCaseValue));
						((EditText) mRootView.findViewById(R.id.editQty)).setText(String.valueOf(nQty));
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

					(mRootView.findViewById(R.id.editCase)).setVisibility(View.GONE);
					(mRootView.findViewById(R.id.txtCaseSplit)).setVisibility(View.GONE);
					if(mEntryInputMethod.equals("A"))
					{
						((EditText) mRootView.findViewById(R.id.editQty)).setText("");
					}
					else
					{
						((EditText) mRootView.findViewById(R.id.editQty)).setText(String.valueOf(nQty));
					}
				}

			}

		}
		else
		{
			if(bShowCase)
			{
				(mRootView.findViewById(R.id.editCase)).setVisibility(View.VISIBLE);
				(mRootView.findViewById(R.id.txtCaseSplit)).setVisibility(View.VISIBLE);
				((TextView) mRootView.findViewById(R.id.txtDescription)).setText("");
				if(mEntryInputMethod.equals("A"))
				{
					((EditText) mRootView.findViewById(R.id.editQty)).setText("");
					((EditText) mRootView.findViewById(R.id.editCase)).setText("");
				}
				else
				{
					((EditText) mRootView.findViewById(R.id.editQty)).setText("0");
					((EditText) mRootView.findViewById(R.id.editCase)).setText("0");
				}

			}
			else
			{
				(mRootView.findViewById(R.id.editCase)).setVisibility(View.GONE);
				(mRootView.findViewById(R.id.txtCaseSplit)).setVisibility(View.GONE);
				((TextView) mRootView.findViewById(R.id.txtDescription)).setText("");
				if(mEntryInputMethod.equals("A"))
				{
					((EditText) mRootView.findViewById(R.id.editQty)).setText("");
				}
				else
				{
					((EditText) mRootView.findViewById(R.id.editQty)).setText("0");
				}
			}
		}
	}

	private void doAdd() {
		String strQty = ((EditText) mRootView.findViewById(R.id.editQty)).getText().toString();
		String strCase = ((EditText) mRootView.findViewById(R.id.editCase)).getText().toString();
		if (mProdDetlIdx == -1) {
			if(!TextUtils.isEmpty(((EditText) mRootView.findViewById(R.id.editItemNo)).getText().toString()))
			{
				Utils.printDebug(mContext, Str.Please_select_valid_product[Global.LANG]);
			}
			((EditText) mRootView.findViewById(R.id.editQty)).setText("");
			((EditText) mRootView.findViewById(R.id.editCase)).setText("");
			focusAndHideKeyboard((EditText) mRootView.findViewById(R.id.editItemNo));
			return;
		}
		if( Utils.getProdLoconSCWType(mContext, mProdDetlList.get(mProdDetlIdx).strItemNo) != Global.WEIGHT_ITEM)
		{
			if(bShowCase)
			{
				if (strQty.length() == 0) {
					if( strCase.length() == 0 )
					{
						Utils.printDebug(mContext, Str.Please_input_the_Quantity[Global.LANG]);
						showKeyboard((EditText) mRootView.findViewById(R.id.editQty), true);
						return;
					}
					else
					{
						strQty = "0";
						((EditText) mRootView.findViewById(R.id.editQty)).setText(strQty);
					}
				}
				if( strCase.length() == 0 ){
					strCase = "0";
					((EditText) mRootView.findViewById(R.id.editCase)).setText(strCase);
				}
			}
			else
			{
				if (strQty.length() == 0) {
					strQty = "0";
					((EditText) mRootView.findViewById(R.id.editQty)).setText(strQty);
				}
			}
		}

		int nIdx = -1;
		ProdDetlInfo productInfo = mProdDetlList.get(mProdDetlIdx);
		for (int i = 0; i < mUInvenDList.size(); i++) {
			if (mUInvenDList.get(i).strItemNo.equals(productInfo.strItemNo) == true) {
				nIdx = i;
				break;
			}
		}

		if( nIdx == -1 )
		{
			for(int i = 0; i < mAllInvenDInfos.size(); i++){
				if( mAllInvenDInfos.get(i).strItemNo.equalsIgnoreCase(productInfo.strItemNo) == true){
					mAllInvenDInfos.get(i).bShow = true;
					mUInvenDList.add(mAllInvenDInfos.get(i));
					nIdx = mUInvenDList.size() - 1;
					break;
				}
			}
		}

		if(Utils.getProdLoconSCWType(mContext, productInfo.strItemNo) == Global.WEIGHT_ITEM)
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
				info.strItemNo = productInfo.strItemNo;
				info.dAdjQty = dValue;
				info.dEndAdj = (info.dAdjQty - info.dStock);
				info.strShortDesc = productInfo.strShortDesc;
				info.strDescription = productInfo.strDescription;
				info.strUOM = productInfo.prodLocnInfo.strCaseUOM.length() > 0 ? productInfo.prodLocnInfo.strCaseUOM : productInfo.prodLocnInfo.strUnitUOM;
				info.nWeight = productInfo.nWeight;
				info.bShow = true;
				info.nOrder = getMaxOrder() + 1;
				mUInvenDList.add(info);
				mCurSelInvenDInfo = info;
			}
			else
			{
				if(mEntryInputMethod.equals("A"))
				{
					if(mUInvenDList.get(nIdx).dAdjQty + dValue < 0)
					{
						((EditText) mRootView.findViewById(R.id.editQty)).setText("");
						Utils.printDebug(mContext, Str.Quantity_Removed_From_the_transaction[Global.LANG]);
						showKeyboard((EditText) mRootView.findViewById(R.id.editQty), true);
						return;
					}
					mUInvenDList.get(nIdx).dAdjQty += dValue;
					mUInvenDList.get(nIdx).dEndAdj = (mUInvenDList.get(nIdx).dAdjQty - mUInvenDList.get(nIdx).dStock);
				}
				else
				{
					mUInvenDList.get(nIdx).dAdjQty = dValue;
					mUInvenDList.get(nIdx).dEndAdj = (mUInvenDList.get(nIdx).dAdjQty - mUInvenDList.get(nIdx).dStock);
				}
				mCurSelInvenDInfo = mUInvenDList.get(nIdx);
			}
		}
		else
		{
			int nQuantity = 0;
			if(bShowCase)
			{
				int nCase = getCaseValue(productInfo.strItemNo);
				if( nCase == 0 ) nCase = 1;

				nQuantity = Utils.parseIntFromString(strQty) + Utils.parseIntFromString(strCase) * nCase;
			}
			else
			{
				nQuantity = Utils.parseIntFromString(strQty);
			}

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
							((EditText) mRootView.findViewById(R.id.editCase)).setText("");
							Utils.printDebug(mContext, Str.Quantity_Removed_From_the_transaction[Global.LANG]);
							showKeyboard((EditText) mRootView.findViewById(R.id.editQty), true);
							return;
						}
					}
					UInvenDInfo info = new UInvenDInfo();
					info.strItemNo = productInfo.strItemNo;
					info.nAdjQty = nQuantity;
					info.nEndAdj = (nQuantity - info.nStock);
					info.strShortDesc = productInfo.strShortDesc;
					info.strDescription = productInfo.strDescription;
					info.strUOM = productInfo.prodLocnInfo.strCaseUOM.length() > 0 ? productInfo.prodLocnInfo.strCaseUOM : productInfo.prodLocnInfo.strUnitUOM;
					info.nWeight = productInfo.nWeight;
					info.bShow = true;
					info.nOrder = getMaxOrder() + 1;
					mUInvenDList.add(info);
					mCurSelInvenDInfo = info;
				}
			} else {
				if(mEntryInputMethod.equals("A"))
				{
					if(mUInvenDList.get(nIdx).nAdjQty + nQuantity < 0)
					{
						((EditText) mRootView.findViewById(R.id.editQty)).setText("");
						((EditText) mRootView.findViewById(R.id.editCase)).setText("");
						Utils.printDebug(mContext, Str.Quantity_Removed_From_the_transaction[Global.LANG]);
						showKeyboard((EditText) mRootView.findViewById(R.id.editQty), true);
						return;
					}
					mUInvenDList.get(nIdx).nAdjQty += nQuantity;
					mUInvenDList.get(nIdx).nEndAdj = (mUInvenDList.get(nIdx).nAdjQty - mUInvenDList.get(nIdx).nStock);
				}
				else
				{
					mUInvenDList.get(nIdx).nAdjQty = nQuantity;
					mUInvenDList.get(nIdx).nEndAdj = (mUInvenDList.get(nIdx).nAdjQty - mUInvenDList.get(nIdx).nStock);
				}
				mCurSelInvenDInfo = mUInvenDList.get(nIdx);
			}
		}

		if(bShowCase)
		{
			mRootView.findViewById(R.id.editCase).setVisibility(View.VISIBLE);
			mRootView.findViewById(R.id.txtCaseSplit).setVisibility(View.VISIBLE);
			((EditText) mRootView.findViewById(R.id.editItemNo)).setText("");
			((EditText) mRootView.findViewById(R.id.editQty)).setText("");
			((EditText) mRootView.findViewById(R.id.editCase)).setText("");
		}
		else
		{
			mRootView.findViewById(R.id.editCase).setVisibility(View.GONE);
			mRootView.findViewById(R.id.txtCaseSplit).setVisibility(View.GONE);
			((EditText) mRootView.findViewById(R.id.editItemNo)).setText("");
			((EditText) mRootView.findViewById(R.id.editQty)).setText("");
		}

		bModified = true;

		focusAndHideKeyboard((EditText) mRootView.findViewById(R.id.editItemNo));
		refreshPage();
		doCheckLoadBatch();
	}

	private void doCheckLoadBatch() {
		if(mCurSelInvenDInfo == null) return;

		if(mCurSelInvenDInfo.bLotControl && (mCurSelInvenDInfo.loadBlotInfoList == null || mCurSelInvenDInfo.loadBlotInfoList.size() < mCurSelInvenDInfo.nAdjQty) )
		{
			try {
				String strBatchCodes = "";
				for(LoadBlotInfo info : mCurSelInvenDInfo.loadBlotInfoList)
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
						if(i == mCurSelInvenDInfo.loadBlotInfoList.size())
						{
							LoadBlotInfo loadBlotInfo = new LoadBlotInfo();
							loadBlotInfo.strBatchDate = new SimpleDateFormat("yyyyMMdd").format(new Date());
							loadBlotInfo.strLotNumber = strBatchCodes1[i];
							loadBlotInfo.nLotQty = 1;
							mCurSelInvenDInfo.loadBlotInfoList.add(loadBlotInfo);
						}
						else
						{
							mCurSelInvenDInfo.loadBlotInfoList.get(i).strLotNumber = strBatchCodes1[i];
						}
					}
				}, mCurSelInvenDInfo.nAdjQty, strBatchCodes, null);
				inputDialog.show();

			}catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	private void doDone() {
		boolean bValidation = true;
		List<UInvenDInfo> showList = mListAdapter.getListData();

		for(UInvenDInfo invenDInfo : showList)
		{
			if(Utils.getProdLoconSCWType(mContext, invenDInfo.strItemNo) == Global.WEIGHT_ITEM)
			{
				if( invenDInfo.dAdjQty == 0 )
				{
					bValidation = false;
					break;
				}
			}
			else
			{
				if( invenDInfo.nAdjQty == 0 )
				{
					bValidation = false;
					break;
				}
			}
		}
		if( bValidation )
		{
			Global.gFragmentForCheckingModification = this;
			Global.gAdjustinfo.uInvenDList.clear();
			Global.gAdjustinfo.uInvenDList.addAll(mUInvenDList);
			Bundle bundle = new Bundle();
			Fragment fragment = new StockTakeCountSummaryFragment();
			fragment.setArguments(bundle);
			mContext.replaceFragment(fragment);
		}
		else
		{
			try {
				final MessageDialog msgDialog = new MessageDialog(mContext);
				msgDialog.setCancelable(false);
				msgDialog.mStrTitle = Str.CountConfirmation[Global.LANG];
				msgDialog.mStrMessage = Str.The_stock_count_shows_there_are_items_with_zero_quantities[Global.LANG];
				msgDialog.mStrRight = Str.Yes[Global.LANG];
				msgDialog.mStrLeft = Str.No[Global.LANG];
				msgDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT){

						}
						else{
							Global.gFragmentForCheckingModification = StockTakeCountEntryFragment.this;
							Global.gAdjustinfo.uInvenDList.clear();
							Global.gAdjustinfo.uInvenDList.addAll(mUInvenDList);
							Bundle bundle = new Bundle();
							Fragment fragment = new StockTakeCountSummaryFragment();
							fragment.setArguments(bundle);
							mContext.replaceFragment(fragment);
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

	private void showInvenDetail(UInvenDInfo info) {
		((EditText) mRootView.findViewById(R.id.editItemNo)).setText(Utils.getDisplayItemCode(mContext, info.strItemNo));

		if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
		{
			double dValue = info.dAdjQty;
			String strQty = String.format(Locale.ENGLISH, "%.2f", dValue);

			mRootView.findViewById(R.id.editCase).setVisibility(View.GONE);
			mRootView.findViewById(R.id.txtCaseSplit).setVisibility(View.GONE);
			mRootView.findViewById(R.id.editQty).setTag("From App");
			if(mEntryInputMethod.equals("A"))
			{
				((EditText) mRootView.findViewById(R.id.editQty)).setText("");
				mRootView.findViewById(R.id.editQty).setTag(null);
				mRootView.findViewById(R.id.editQty).requestFocus();
				((EditText) mRootView.findViewById(R.id.editQty)).setSelection(0);
			}
			else
			{
				((EditText) mRootView.findViewById(R.id.editQty)).setText(strQty);
				mRootView.findViewById(R.id.editQty).setTag(null);
				mRootView.findViewById(R.id.editQty).requestFocus();
				((EditText) mRootView.findViewById(R.id.editQty)).setSelection(strQty.length());
			}

		}
		else
		{
			if(bShowCase)
			{
				int nCase = getCaseValue(info.strItemNo);
				int nValue = info.nAdjQty;
				String strQty = String.format(Locale.ENGLISH, "%d", nValue % nCase);
				String strCase = String.format(Locale.ENGLISH, "%d", nValue / nCase);
				mRootView.findViewById(R.id.editCase).setVisibility(View.VISIBLE);
				mRootView.findViewById(R.id.txtCaseSplit).setVisibility(View.VISIBLE);
				if(mEntryInputMethod.equals("A"))
				{
					((EditText) mRootView.findViewById(R.id.editQty)).setText("");
					((EditText) mRootView.findViewById(R.id.editCase)).setText("");
					mRootView.findViewById(R.id.editCase).requestFocus();
					((EditText) mRootView.findViewById(R.id.editCase)).setSelection(0);
				}
				else
				{
					((EditText) mRootView.findViewById(R.id.editQty)).setText(strQty);
					((EditText) mRootView.findViewById(R.id.editCase)).setText(strCase);
					mRootView.findViewById(R.id.editCase).requestFocus();
					((EditText) mRootView.findViewById(R.id.editCase)).setSelection(strCase.length());
				}
			}
			else
			{

				int nValue = info.nAdjQty;
				String strQty = String.format(Locale.ENGLISH, "%d", nValue);
				mRootView.findViewById(R.id.editCase).setVisibility(View.GONE);
				mRootView.findViewById(R.id.txtCaseSplit).setVisibility(View.GONE);
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
		refreshPage();
	}

	public void doCofirmBack()
	{
		if(bModified)
		{
			try {
				final MessageDialog msgDialog = new MessageDialog(mContext);
				msgDialog.setCancelable(false);
				msgDialog.mStrMessage = Str.AreYouSureExitStockTake[Global.LANG];
				msgDialog.mStrRight = Str.Yes[Global.LANG];
				msgDialog.mStrLeft = Str.No[Global.LANG];
				msgDialog.setOnDismissListener(dialog -> {
					if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT){

					}
					else{
						bModified = false;
						mContext.onBackPressed();
					}
				});
				msgDialog.show();
			}catch (Exception e)
			{
				e.printStackTrace();
			}
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
			case R.id.btnAdd:
				doAdd();
				break;
			case R.id.btnFilterBy:
				{
					FilterByFragment fragment = new FilterByFragment();
					Global.gCurFilterBy = filterByList;
					mContext.replaceFragment(fragment);
				}
				break;

			case R.id.btnSort:
				doSort();
				break;
			case R.id.btnFilter:
				doFilter();
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
			case R.id.btnDone:
				doDone();
				break;
		}
		m_bClicked = false;
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

	private class ListAdapter extends BaseAdapter {

		private List<UInvenDInfo> list = new ArrayList<>();

		private class ViewHolder {
			public TextView txtItemNo, txtShortDesc, txtPickLocationCode, txtLoaded;
		}

		public int getCount() {
			return list.size();
		}

		public List<UInvenDInfo> getListData()
		{
			return list;
		}

		public void setListData(List<UInvenDInfo> list) {
			this.list.clear();
			this.list.addAll(list);
			SortData(this.list);
//			Collections.sort(this.list, new Comparator<UInvenDInfo>() {
//				@Override
//				public int compare(UInvenDInfo t1, UInvenDInfo t2) {
//					int nSeq1 = getSequenceValue(t1.strItemNo);
//					int nSeq2 = getSequenceValue(t2.strItemNo);
//					if( nSeq1 == 0 && nSeq2 != 0 )
//						return -1;
//					else if( nSeq1 != 0 && nSeq2 == 0)
//						return 1;
//					else if( nSeq1 == 0 && nSeq2 == 0 )
//					{
//						return t1.strItemNo.compareTo(t2.strItemNo);
//					}
//					else
//						return nSeq1 - nSeq2;
//				}
//			});
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
				view = mContext.getLayoutInflater().inflate(R.layout.row_load_truck_bulk, parent, false);
				holder = new ViewHolder();
				holder.txtItemNo = (TextView) view.findViewById(R.id.txtItemNo);
				holder.txtShortDesc = (TextView) view.findViewById(R.id.txtShortDesc);
				holder.txtPickLocationCode = (TextView) view.findViewById(R.id.txtPickLocationCode);
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
			holder.txtPickLocationCode.setText(info.strPickLocationCode);
			if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
			{
				holder.txtLoaded.setText(String.format(Locale.ENGLISH, "%.2f", info.dAdjQty));
			}
			else
			{
				if(bShowCase)
				{
					if (Utils.isContainerItem(mContext, info.strItemNo)) {
						holder.txtLoaded.setText(String.format(Locale.ENGLISH, "%d", info.nAdjQty));
					}
					else {
						int nCase = getCaseValue(info.strItemNo);
						holder.txtLoaded.setText(String.format(Locale.ENGLISH, "%d/%d", info.nAdjQty / nCase, info.nAdjQty % nCase));
					}
				}
				else
				{
					holder.txtLoaded.setText(String.format(Locale.ENGLISH, "%d", info.nAdjQty));
				}

			}


			view.setOnClickListener(v -> showInvenDetail(info));

			return view;
		}
	}

	private Map<String, Map<String, String>> sortMap = new HashMap<>();
	private Map<String, String> getSortItemMap(String strItemNo)
	{
		Map<String, String> map = sortMap.get(strItemNo);
		if(map == null)
		{
			map = Utils.getPropertyMap(mProdUPCDetMap.get(strItemNo));
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

	private class SlideListAdapter extends BaseAdapter {

		private List<UInvenDInfo> list = new ArrayList<>();

		private class ViewHolder {
			public TextView txtShortDesc, txtPickLocationCode, txtLoaded, txtStock;
		}

		public int getCount() {
			return list.size();
		}

		public void setListData(List<UInvenDInfo> list) {
			this.list.clear();
			this.list.addAll(list);
			SortData(this.list);
//			Collections.sort(this.list, new Comparator<UInvenDInfo>() {
//				@Override
//				public int compare(UInvenDInfo t1, UInvenDInfo t2) {
//					int nSeq1 = getSequenceValue(t1.strItemNo);
//					int nSeq2 = getSequenceValue(t2.strItemNo);
//					if( nSeq1 == 0 && nSeq2 != 0 )
//						return -1;
//					else if( nSeq1 != 0 && nSeq2 == 0)
//						return 1;
//					else if( nSeq1 == 0 && nSeq2 == 0 )
//					{
//						return t1.strItemNo.compareTo(t2.strItemNo);
//					}
//					else
//						return nSeq1 - nSeq2;
//				}
//			});
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
				view = mContext.getLayoutInflater().inflate(R.layout.slide_row_load_truck_bulk, parent, false);
				holder = new ViewHolder();
				holder.txtShortDesc = (TextView) view.findViewById(R.id.txtShortDesc);
				holder.txtPickLocationCode = (TextView) view.findViewById(R.id.txtPickLocationCode);
				holder.txtLoaded = (TextView) view.findViewById(R.id.txtLoaded);
				holder.txtStock = (TextView) view.findViewById(R.id.txtStock);
				view.setTag(holder);
			} else {
				holder = (ViewHolder) view.getTag();
			}

			holder.txtShortDesc.setText(info.strDescription);
			holder.txtPickLocationCode.setText(info.strPickLocationCode);

			if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
			{
				holder.txtLoaded.setText(String.format(Locale.ENGLISH, "%.2f",  info.dStock));
				holder.txtStock.setText(String.format(Locale.ENGLISH, "%.2f", info.dAdjQty));
			}
			else
			{
				if(bShowCase)
				{
					if (Utils.isContainerItem(mContext, info.strItemNo)) {
						holder.txtLoaded.setText(String.format(Locale.ENGLISH, "%d",  info.nStock));
						holder.txtStock.setText(String.format(Locale.ENGLISH, "%d", info.nAdjQty));
					}
					else {
						int nCase = getCaseValue(info.strItemNo);
						holder.txtLoaded.setText(String.format(Locale.ENGLISH, "%d/%d",  info.nStock / nCase, info.nStock % nCase));
						holder.txtStock.setText(String.format(Locale.ENGLISH, "%d/%d", info.nAdjQty / nCase, info.nAdjQty % nCase));
					}
				}
				else
				{
					holder.txtLoaded.setText(String.format(Locale.ENGLISH, "%d",  info.nStock));
					holder.txtStock.setText(String.format(Locale.ENGLISH, "%d", info.nAdjQty));
				}
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

	private void setQtyFormat()
	{
		if( mProdDetlIdx != -1 )
		{
			ProdDetlInfo productInfo = mProdDetlList.get(mProdDetlIdx);
			if(Utils.getProdLoconSCWType(mContext, productInfo.strItemNo) == Global.WEIGHT_ITEM)
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