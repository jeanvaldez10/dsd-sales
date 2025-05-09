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
import android.widget.Toast;

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
import dsd.dsdvansales.activities.MainActivity;
import dsd.dsdvansales.api.ConnectApiClient;
import dsd.dsdvansales.db.DbAdapter;
import dsd.dsdvansales.dialogs.BatchCodeInputDialog;
import dsd.dsdvansales.dialogs.InputBatchNumAllDialog;
import dsd.dsdvansales.dialogs.InputBatchNumDialog;
import dsd.dsdvansales.dialogs.LoadDataCheckPopupDialog;
import dsd.dsdvansales.dialogs.MessageDialog;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.model.DescTypeInfo;
import dsd.dsdvansales.model.LoadBlotInfo;
import dsd.dsdvansales.model.LoadDetl;
import dsd.dsdvansales.model.LoadDetlInfo;
import dsd.dsdvansales.model.LoadHead;
import dsd.dsdvansales.model.LoadHeadInfo;
import dsd.dsdvansales.model.LoadTripResponse;
import dsd.dsdvansales.model.LoginInfo;
import dsd.dsdvansales.model.PresoldOrDInfo;
import dsd.dsdvansales.model.PresoldOrHInfo;
import dsd.dsdvansales.model.ProdDetlInfo;
import dsd.dsdvansales.model.ProdInfo;
import dsd.dsdvansales.model.ProdStruInfo;
import dsd.dsdvansales.model.UInvenDInfo;
import dsd.dsdvansales.model.UInvenHInfo;
import dsd.dsdvansales.views.AlertSingleChoiceAdapter;
import retrofit2.Call;

public class LoadTruckBulkFragment extends BaseFragment implements View.OnClickListener, LoadDataCheckPopupDialog.Callback {

	private TextView mTxtNoData = null;
	private TextView mTxtSlidieNoData = null;
	private ListView mListView = null;
	private ListView mSlideListView = null;
	private ListAdapter mListAdapter = null;
	private SlideListAdapter mSlideListAdapter = null;
	public	 List<UInvenDInfo> mUInvenDList = new ArrayList<>();
	private List<ProdDetlInfo> mProdDetlList = new ArrayList<>();
	private Map<String, ProdDetlInfo> mProdUPCDetMap = new HashMap<>();
	private Map<String, ProdDetlInfo> mProdDetlItemMap = new HashMap<>();
	private Map<String, ProdInfo> mProdInfoMap = new HashMap<>();
	private boolean filter = false;

	private UInvenDInfo mCurSelInvenDInfo = null;

	private int mProdDetlIdx = -1;
	private int mPickLocationCodeCnt = 0;
	private String strCartonDescription = "";

	private Map<String, Integer> mCaseMap = new HashMap<>();

	private boolean bShowCase = true;

	private String sortFlag = "";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	private void setSortFlag(String flag)
	{
		if(TextUtils.isEmpty(flag))
		{
			sortFlag = "desc";
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
		else if(flag.equalsIgnoreCase("Item code"))
		{
			sortFlag = "code";
		}
		else
		{
			sortFlag = "desc";
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootView = inflater.inflate(R.layout.fragment_load_truck_bulk, container, false);
		commonInit();
		if(TextUtils.isEmpty(sortFlag))
		{
			sortFlag = Utils.getStringSetting(mContext, Global.PREF_LOAD_SRT_DEF);
			if(Utils.getSortValue(sortFlag) == null) {
				sortFlag = "desc";
			} else {
				sortFlag = Utils.getSortValue(sortFlag);
			}
			new Handler(Looper.getMainLooper()).post(() -> refreshPage());
		}
		return mRootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		Global.gFragIdx = Global.FRAG_LOAD_TRUCK_BULK;
	}
	
	@Override
	public void onPause() {
		super.onPause();
		if (Global.gUInvenHinfo != null) {
			Global.gUInvenHinfo.truckList.clear();
			for (UInvenDInfo info : mUInvenDList) {
				info.nLoadAdj = info.nStock - info.nLoaded;
				Global.gUInvenHinfo.truckList.add(info);
			}
		}
	}

	@Override
	public void initView() {
		mRootView.findViewById(R.id.btnFilterBy).setOnClickListener(this);
		mRootView.findViewById(R.id.btnAdd).setOnClickListener(this);
		bShowCase = Utils.getStringSetting(mContext, Global.PREF_INVENTORYUOM).equals("U") == false;

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

		mRootView.findViewById(R.id.btnSort).setOnClickListener(this);
		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		List<ProdStruInfo> prodStruList = dbAdapter.getProdStruList();
		List<DescTypeInfo> descTypeInfoList = dbAdapter.getDescTypeList(Global.UNITOFMEASURE_DESC_TYPE_ID);
		dbAdapter.close();

		strCartonDescription = "";
		for(DescTypeInfo descTypeInfo : descTypeInfoList)
		{
			if( descTypeInfo.strAlphaKey.equalsIgnoreCase("Z") )
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
		}else
		{

			mRootView.findViewById(R.id.btnProductTreeList).setOnClickListener(this);
			mRootView.findViewById(R.id.btnProductTreeList).setEnabled(true);
			mRootView.findViewById(R.id.btnProductTreeList).setBackground(mContext.getResources().getDrawable(R.drawable.btn_white_medium));
		}

		mRootView.findViewById(R.id.btnBarcodeScanner).setOnClickListener(this);
		mRootView.findViewById(R.id.btnProductSearch).setOnClickListener(this);
		mRootView.findViewById(R.id.btnRefresh).setOnClickListener(this);
		mRootView.findViewById(R.id.btnBack).setOnClickListener(this);
		mRootView.findViewById(R.id.btnDone).setOnClickListener(this);

		mTxtNoData = (TextView) mRootView.findViewById(R.id.txtNoData);
		mTxtSlidieNoData = (TextView) mRootView.findViewById(R.id.txtSlideNoData);

		mListView = (ListView) mRootView.findViewById(R.id.listView);
		mSlideListView = (ListView) mRootView.findViewById(R.id.slidelistView);

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
			public void onScrollStateChanged(AbsListView view, int scrollState) {
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				if (firstVisibleItem + visibleItemCount >= totalItemCount) {
					mRootView.findViewById(R.id.txtSlideMore).setVisibility(View.GONE);
				} else {
					mRootView.findViewById(R.id.txtSlideMore).setVisibility(View.VISIBLE);
				}
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
					doProcessSelectProduct(strItemNo, 0, true, false, false);
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
					if(mProdDetlIdx != -1)
					{
						ProdDetlInfo detlInfo = mProdDetlList.get(mProdDetlIdx);
						if(Utils.getProdLoconSCWType(mContext, detlInfo.strItemNo) == Global.WEIGHT_ITEM)
						{
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

		if(Utils.getStringSetting(mContext, Global.PREF_QTY_ENTRY_METHOD).equals("A"))
		{
			((EditText)mRootView.findViewById(R.id.editCase)).setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
			((EditText)mRootView.findViewById(R.id.editQty)).setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
		}
		else
		{
			((EditText)mRootView.findViewById(R.id.editCase)).setInputType(InputType.TYPE_CLASS_NUMBER);
			((EditText)mRootView.findViewById(R.id.editQty)).setInputType(InputType.TYPE_CLASS_NUMBER);
		}

		if (Utils.getStringSetting(mContext, Global.PREF_DISABLE_LOAD_ADJUST).equals("1")) {
			mRootView.findViewById(R.id.btnProductSearch).setVisibility(View.GONE);
			mRootView.findViewById(R.id.btnRefresh).setVisibility(View.VISIBLE);
		}
		else {
			mRootView.findViewById(R.id.btnProductSearch).setVisibility(View.VISIBLE);
			mRootView.findViewById(R.id.btnRefresh).setVisibility(View.GONE);
		}

		mRootView.findViewById(R.id.btnFilter).setOnClickListener(this);

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
		mProdDetlList.clear();
		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		mProdDetlList = dbAdapter.getProdDetlList(mProdUPCDetMap);
//		dbAdapter.getProdDetlList(mProdDetlItemMap, null);
//		if(Utils.getStringSetting(mContext, Global.PREF_ITEM_CODE_DISPLAY_SOURCE).equals("PRODINFO"))
//		{
//			mProdInfoMap = dbAdapter.getProdInfoMap("ItemCode");
//		}
		List<LoadDetlInfo> loadDetlList = dbAdapter.getLoadDetlList();
		List<LoadHeadInfo> loadHeadInfoList = dbAdapter.getLLoadHeadInfo(true);
		dbAdapter.close();

		Global.bTransferLoad = false;
		if(loadHeadInfoList.size() > 0)
		{
			if(loadHeadInfoList.get(0).strType.equals("T"))
			{
				Global.bTransferLoad = true;
			}
		}
		List<PresoldOrHInfo> presoldOrHInfoList = new ArrayList<>();
		Map<String, List<PresoldOrDInfo>> presoldMap = new HashMap<>();

		if(loadDetlList.size() == 0 && Utils.getIntSetting(mContext, Global.PREF_LOAD_CUSTOMER) == 0 && Utils.getStringSetting(mContext, Global.PREF_USE_ORDERS_AS_LOAD).equals("1"))
		{
			presoldOrHInfoList = dbAdapter.getAllPresoldOrHInfoList();
			for(PresoldOrHInfo presoldOrHInfo : presoldOrHInfoList)
			{
				if(presoldOrHInfo.strType.equals("P") || presoldOrHInfo.strType.equals("D"))
				{
					List<PresoldOrDInfo> presoldOrDInfoList = dbAdapter.getPresoldOrDList(presoldOrHInfo.strDetailFile);
					presoldMap.put(presoldOrHInfo.strDetailFile, presoldOrDInfoList);
				}
			}
		}

		List<UInvenDInfo> uInvenDList = new ArrayList<>();
		List<String> strCodeList = new ArrayList<>();

		Map<String, Map<String, List<LoadBlotInfo>>> blotMap = new HashMap<>();
		if(loadDetlList.size() > 0)
		{
			dbAdapter.open();
			for (LoadDetlInfo loadDetlInfo : loadDetlList) {
				UInvenDInfo uInvenDInfo = new UInvenDInfo();
				uInvenDInfo.strItemNo = loadDetlInfo.strItemNo;

				for (ProdDetlInfo info : mProdDetlList) {
					if (info.strItemNo.equals(loadDetlInfo.strItemNo) == true) {
						uInvenDInfo.strShortDesc = info.strShortDesc;
						uInvenDInfo.strDescription = info.strDescription;
						uInvenDInfo.strUOM = info.prodLocnInfo.strCaseUOM.length() > 0 ? info.prodLocnInfo.strCaseUOM : info.prodLocnInfo.strUnitUOM;
						uInvenDInfo.strFullCase = info.prodLocnInfo.strFullCase;
						uInvenDInfo.nWeight = info.nWeight;
						uInvenDInfo.nProdLoconSCWType = info.prodLocnInfo.nSCWType;

						if(info.strLotControl.equalsIgnoreCase("Y") && Utils.getIntSetting(mContext, Global.PREF_LOAD_CUSTOMER) != 1)
						{
							uInvenDInfo.bLotControl = true;
						}
						break;
					}
				}

				if(uInvenDInfo.bLotControl)
				{
					//Load the LoadBlot information from the db.
					Map<String, List<LoadBlotInfo>> blotDetailMap = blotMap.get(loadDetlInfo.strDetailFile);
					if(blotDetailMap == null)
					{
						blotDetailMap = new HashMap<>();
						blotMap.put(loadDetlInfo.strDetailFile, blotDetailMap);

						List<LoadBlotInfo> loadBlotInfoList = dbAdapter.getLoadBlot(loadDetlInfo.strDetailFile);

						for(LoadBlotInfo loadBlotInfo : loadBlotInfoList)
						{
							List<LoadBlotInfo> infoList = blotDetailMap.get(loadBlotInfo.strItemNo);
							if(infoList == null)
							{
								infoList = new ArrayList<>();
								blotDetailMap.put(loadBlotInfo.strItemNo, infoList);
							}
							infoList.add(loadBlotInfo);
						}
					}

					uInvenDInfo.loadBlotInfoList = blotDetailMap.get(uInvenDInfo.strItemNo);
				}

				uInvenDInfo.strPickLocationCode = loadDetlInfo.strPickLocationCode;
				if(uInvenDInfo.nProdLoconSCWType == Global.WEIGHT_ITEM)
				{
					uInvenDInfo.dBeginQty = 0;
					uInvenDInfo.dLoaded = loadDetlInfo.dQty;
				}
				else
				{
					uInvenDInfo.nBeginQty = 0;
					uInvenDInfo.nLoaded = loadDetlInfo.nQty;
				}


				if(Utils.getBooleanSetting(mContext, Global.PREF_LOAD_QTY_ENTRY))
				{
					if(uInvenDInfo.nProdLoconSCWType == Global.WEIGHT_ITEM)
					{
						uInvenDInfo.dStock = uInvenDInfo.dLoaded;
						uInvenDInfo.dBeginQty = 0;
					}
					else
					{
						uInvenDInfo.nStock = uInvenDInfo.nLoaded;
						uInvenDInfo.nBeginQty = 0;
					}
				}
				else
				{
					if(uInvenDInfo.nProdLoconSCWType == Global.WEIGHT_ITEM)
					{
						uInvenDInfo.dStock = 0;
						uInvenDInfo.dBeginQty = 0;
					}
					else
					{
						uInvenDInfo.nStock = 0;
						uInvenDInfo.nBeginQty = 0;
					}
					uInvenDInfo.nSequence = uInvenDInfo.nIdx;

				}
				uInvenDList.add(uInvenDInfo);
				if (uInvenDInfo.strPickLocationCode.length() > 0) {
					boolean bExist = false;
					for (String strCode : strCodeList) {
						if (strCode.equals(uInvenDInfo.strPickLocationCode) == true) {
							bExist = true;
							break;
						}
					}
					if (bExist == false) {
						strCodeList.add(uInvenDInfo.strPickLocationCode);
					}
				}
			}
			dbAdapter.close();
		}
		else if(presoldOrHInfoList.size() > 0 && Utils.getIntSetting(mContext, Global.PREF_LOAD_CUSTOMER) == 0 && loadHeadInfoList.size() == 0
			&& Utils.getStringSetting(mContext, Global.PREF_USE_ORDERS_AS_LOAD).equals("1"))
		{
			Map<String, UInvenDInfo> map = new HashMap<>();
			for(PresoldOrHInfo presoldOrHInfo : presoldOrHInfoList)
			{
				if(presoldOrHInfo.strType.equals("P") || presoldOrHInfo.strType.equals("D"))
				{
					List<PresoldOrDInfo> presoldOrDInfoList = presoldMap.get(presoldOrHInfo.strDetailFile);
					for(PresoldOrDInfo presoldOrDInfo : presoldOrDInfoList)
					{
						if(!presoldOrDInfo.strTrxnType.equals("4") && !presoldOrDInfo.strTrxnType.equals("11"))
							continue;
						UInvenDInfo uInvenDInfo = map.get(presoldOrDInfo.strItemNo);
						if( uInvenDInfo == null )
						{
							uInvenDInfo = new UInvenDInfo();
							uInvenDInfo.strItemNo = presoldOrDInfo.strItemNo;
							uInvenDInfo.strPickLocationCode = presoldOrDInfo.strPickLocationCode;

							for (ProdDetlInfo info : mProdDetlList) {
								if (info.strItemNo.equals(presoldOrDInfo.strItemNo) == true) {
									uInvenDInfo.strShortDesc = info.strShortDesc;
									uInvenDInfo.strDescription = info.strDescription;
									uInvenDInfo.strUOM = info.prodLocnInfo.strCaseUOM.length() > 0 ? info.prodLocnInfo.strCaseUOM : info.prodLocnInfo.strUnitUOM;
									uInvenDInfo.strFullCase = info.prodLocnInfo.strFullCase;
									uInvenDInfo.nWeight = info.nWeight;
									uInvenDInfo.nProdLoconSCWType = info.prodLocnInfo.nSCWType;

									if(info.strLotControl.equalsIgnoreCase("Y") && Utils.getIntSetting(mContext, Global.PREF_LOAD_CUSTOMER) != 1)
									{
										uInvenDInfo.bLotControl = true;
									}
									break;
								}
							}

							if (uInvenDInfo.strPickLocationCode.length() > 0) {
								boolean bExist = false;
								for (String strCode : strCodeList) {
									if (strCode.equals(uInvenDInfo.strPickLocationCode) == true) {
										bExist = true;
										break;
									}
								}
								if (bExist == false) {
									strCodeList.add(uInvenDInfo.strPickLocationCode);
								}
							}
							uInvenDList.add(uInvenDInfo);
							map.put(presoldOrDInfo.strItemNo, uInvenDInfo);
						}
						if(uInvenDInfo.nProdLoconSCWType == Global.WEIGHT_ITEM)
						{
							uInvenDInfo.dLoaded += presoldOrDInfo.dOrderQty;
							uInvenDInfo.dStock = uInvenDInfo.dLoaded;
						}
						else
						{
							uInvenDInfo.nLoaded += presoldOrDInfo.nOrderQty;
							uInvenDInfo.nStock = uInvenDInfo.nLoaded;
						}
					}
				}
			}
//			Collections.sort(uInvenDList, new Comparator<UInvenDInfo>() {
//				@Override
//				public int compare(UInvenDInfo lhs, UInvenDInfo rhs) {
//					return lhs.strItemNo.compareTo(rhs.strItemNo);
//				}
//			});
		}

		mPickLocationCodeCnt = strCodeList.size();
		if (Global.gUInvenHinfo == null || Global.gUInvenHinfo.truckList == null || Global.gUInvenHinfo.truckList.size() == 0) {
			if( Global.gUInvenHinfo == null )
			{
				Global.gUInvenHinfo = new UInvenHInfo();
			}
			for (UInvenDInfo info : uInvenDList) {
				info.bShow = mPickLocationCodeCnt == 0;
				mUInvenDList.add(info);
			}
		} else {
			mUInvenDList.addAll(Global.gUInvenHinfo.truckList);
		}

		refreshPage();
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
			int nSCWType1 = Utils.getSCWType(mContext, t1.strItemNo);
			int nSCWType2 = Utils.getSCWType(mContext, t2.strItemNo);
			if (nSCWType1 != nSCWType2) {
				return nSCWType1-nSCWType2;
			}
			else {
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
			}
		});
	}

	@Override
	public void refreshPage() {
		List<UInvenDInfo> list = new ArrayList<>();
		for (UInvenDInfo info : mUInvenDList) {
			if (info.bShow == true) {
				if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
				{
					if(filter && (info.dStock - info.dLoaded) == 0) continue;
				}
				else
				{
					if(filter && (info.nStock - info.nLoaded == 0)) continue;
				}
				list.add(info);
			}
		}

		SortData(list);
		//Collections.sort(list, UInvenDInfo.codeComparator);
		//Collections.sort(list, UInvenDInfo.orderComparator);
		if (list.size() > 0) {
			mListView.setVisibility(View.VISIBLE);
			mSlideListView.setVisibility(View.VISIBLE);
			mTxtNoData.setVisibility(View.GONE);
			mTxtSlidieNoData.setVisibility(View.GONE);
			mListAdapter.setListData(list);
			mListAdapter.notifyDataSetChanged();
			mSlideListAdapter.setListData(list);
			mSlideListAdapter.notifyDataSetChanged();
		} else {
			mListView.setVisibility(View.GONE);
			mSlideListView.setVisibility(View.GONE);
			mTxtNoData.setVisibility(View.VISIBLE);
			mTxtSlidieNoData.setVisibility(View.VISIBLE);
		}

		if(!TextUtils.isEmpty(strCartonDescription) && bShowCase)
		{
			mRootView.findViewById(R.id.lblCartons).setVisibility(View.VISIBLE);
			mRootView.findViewById(R.id.txtCartons).setVisibility(View.VISIBLE);
			int nCartons = 0;
			if (Utils.getIntSetting(mContext, Global.PREF_LOAD_CONTAINERS) == 2) {
				for(UInvenDInfo uInvenDInfo : mUInvenDList)
				{
					if (Utils.isContainerItem(mContext, uInvenDInfo.strItemNo)) {
						nCartons += uInvenDInfo.nStock;
					}
				}
			}
			else {
				for(UInvenDInfo uInvenDInfo : mUInvenDList)
				{
					if(uInvenDInfo.nSCWType != 2)
					{
						nCartons += uInvenDInfo.nStock / getCaseValue(uInvenDInfo.strItemNo);
					}
				}
			}
			((TextView)mRootView.findViewById(R.id.txtCartons)).setText(String.valueOf(nCartons));
		}
		else
		{
			mRootView.findViewById(R.id.lblCartons).setVisibility(View.GONE);
			mRootView.findViewById(R.id.txtCartons).setVisibility(View.INVISIBLE);
		}

		if(Global.bTransferLoad)
		{
			mRootView.findViewById(R.id.btnAdd).setVisibility(View.INVISIBLE);
		}
		else {
			mRootView.findViewById(R.id.btnAdd).setVisibility(View.VISIBLE);
		}

		String strDisableLoadAdjust = Utils.getStringSetting(mContext, Global.PREF_DISABLE_LOAD_ADJUST);
		if (Utils.getIntSetting(mContext, Global.PREF_LOAD_CUSTOMER) == 0 && strDisableLoadAdjust.equals("1")) {
			mRootView.findViewById(R.id.editItemNo).setEnabled(false);
			mRootView.findViewById(R.id.editCase).setEnabled(false);
			mRootView.findViewById(R.id.editQty).setEnabled(false);
			mRootView.findViewById(R.id.btnAdd).setEnabled(false);
			mRootView.findViewById(R.id.btnBarcodeScanner).setEnabled(false);
			mRootView.findViewById(R.id.btnProductSearch).setEnabled(false);
		}
	}

	@Override
	public void refreshLanguage() {
		((TextView) mRootView.findViewById(R.id.lblTitle)).setText(Str.LoadTruck[Global.LANG].toUpperCase());
		((TextView) mRootView.findViewById(R.id.txtTrip)).setText(Str.Trip[Global.LANG] + " # " + Global.gTripNumber);
		((TextView) mRootView.findViewById(R.id.txtQty)).setText(Str.Qty[Global.LANG]);
		((TextView) mRootView.findViewById(R.id.txtDescription)).setText("");
		((Button) mRootView.findViewById(R.id.btnFilterBy)).setText(Str.FilterBy[Global.LANG]);
		((Button) mRootView.findViewById(R.id.btnAdd)).setText(Str.Add[Global.LANG]);
		((Button) mRootView.findViewById(R.id.btnDone)).setText(Str.Done[Global.LANG]);
		if (mPickLocationCodeCnt == 0) {
			mTxtNoData.setText(Str.No_data_has_been_found[Global.LANG]);
			mTxtSlidieNoData.setText(Str.No_data_has_been_found[Global.LANG]);
		} else {
			mTxtNoData.setText(Str.UseFilterToLoadData[Global.LANG]);
			mTxtSlidieNoData.setText(Str.UseFilterToLoadData[Global.LANG]);
		}

		if( !TextUtils.isEmpty(strCartonDescription))
		{
			((TextView)mRootView.findViewById(R.id.lblCartons)).setText(String.format(Locale.ENGLISH, Str.TotalCartonCount[Global.LANG], strCartonDescription));
		}
	}

	private void doProcessSelectProduct(final String strItemNo, int nQty, boolean bUnit, boolean bUpdateItemNo, boolean isSearchByItemNo)
	{
		if (strItemNo.length() == 0) {
			Utils.printDebug(mContext, Str.NoProductFound[Global.LANG]);
		} else {
			if(bUpdateItemNo || !Utils.getStringSetting(mContext, Global.PREF_AUTO_SCAN_ADD).equals("1"))
			{
				((EditText) mRootView.findViewById(R.id.editItemNo)).setText(strItemNo);
				if(Utils.getProdLoconSCWType(mContext, strItemNo) == Global.WEIGHT_ITEM)
				{
					((EditText)mRootView.findViewById(R.id.editQty)).setTag("From App");
					((EditText)mRootView.findViewById(R.id.editQty)).setText(String.format(Locale.ENGLISH, "%.2f", 0f));
					((EditText)mRootView.findViewById(R.id.editQty)).setTag(null);
					this.showKeyboard((EditText) mRootView.findViewById(R.id.editQty), true);
				}
				else
				{

					if(bUnit)
					{
						((EditText) mRootView.findViewById(R.id.editQty)).setText(String.valueOf(nQty));
						this.showKeyboard((EditText) mRootView.findViewById(R.id.editQty), true);
					}
					else
					{
						if(bShowCase)
						{
							int nCase = getCaseValue(strItemNo);
							int nCaseValue = nQty / nCase;
							int nQtyValue = nQty / nCase;
							((EditText) mRootView.findViewById(R.id.editCase)).setText(String.valueOf(nCaseValue));
							((EditText) mRootView.findViewById(R.id.editQty)).setText(String.valueOf(nQtyValue));
							this.showKeyboard((EditText) mRootView.findViewById(R.id.editCase), true);
						}
						else
						{
							((EditText) mRootView.findViewById(R.id.editQty)).setText(String.valueOf(nQty));
							this.showKeyboard((EditText) mRootView.findViewById(R.id.editQty), true);
						}
					}
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
			if(bUnit)
			{
				showKeyboard(((EditText) mRootView.findViewById(R.id.editQty)), true);
			}
			else
			{
				if(bShowCase)
				{
					showKeyboard((EditText) mRootView.findViewById(R.id.editCase), true);
				}
				else
				{
					showKeyboard(((EditText) mRootView.findViewById(R.id.editQty)), true);
				}
			}
		}

	}

	@Override
	public void receiveBroadcast(String strAction, Intent intent) {
		if (strAction.equals(Global.ACTION_SELECT_PRODUCT) == true) {
			boolean bUnit = false;
			int nType = intent.getIntExtra(Global.EXTRA_TYPE, 0);
			String strItemNo = "";
			int nQty = 0;
			float dQty = 0;
			if (nType == Global.SELECT_PRODUCT_ITEMNO) {
				nQty = 0;
				strItemNo = intent.getStringExtra(Global.EXTRA_ITEM_NO);
				doProcessSelectProduct(strItemNo, nQty, bUnit, true, true);
			} else if (nType == Global.SELECT_PRODUCT_ITEMUPC) {
				String strFrom = intent.getStringExtra(Global.EXTRA_BARCODESCAN_FROM);
				if (!strFrom.equals(getClass().getSimpleName())) {
					return;
				}
				nQty = 1;
				bUnit = true;
				String strItemUPC = intent.getStringExtra(Global.EXTRA_ITEM_UPC);
				for (ProdDetlInfo info : mProdDetlList) {
					if (info.strItemUPC.equals(strItemUPC) == true || info.strItemUPC20.equals(strItemUPC) == true) {
						strItemNo = info.strItemNo;
						break;
					}
				}
				((EditText) mRootView.findViewById(R.id.editItemNo)).setText(strItemUPC);
				doProcessSelectProduct(strItemNo, nQty, bUnit, false, false);
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

			doSearch(false);
			showKeyboard(((EditText) mRootView.findViewById(R.id.editQty)), true);
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
			((TextView) mRootView.findViewById(R.id.txtDescription)).setText(strDescription);
			if(Utils.getProdLoconSCWType(mContext, strItemNo) != Global.WEIGHT_ITEM)
			{
				if(bShowCase)
				{
					int nCase = getCaseValue(strItemNo);
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
					((EditText) mRootView.findViewById(R.id.editCase)).setVisibility(View.VISIBLE);
					(mRootView.findViewById(R.id.txtCaseSplit)).setVisibility(View.VISIBLE);
					if(!Utils.getStringSetting(mContext, Global.PREF_QTY_ENTRY_METHOD).equals("A"))
					{
						((EditText) mRootView.findViewById(R.id.editCase)).setText(String.valueOf(nCaseValue));
						((EditText) mRootView.findViewById(R.id.editQty)).setText(String.valueOf(nQty));
					}
					else
					{
						((EditText) mRootView.findViewById(R.id.editCase)).setText("");
						((EditText) mRootView.findViewById(R.id.editQty)).setText("");
					}

				}
				else
				{

					int nQty = 0;
					if(!TextUtils.isEmpty(strQty))
					{
						try
						{
							nQty = Integer.parseInt(strQty);
						}catch(Exception e)
						{
							e.printStackTrace();
						}
					}
					((EditText) mRootView.findViewById(R.id.editCase)).setVisibility(View.GONE);
					(mRootView.findViewById(R.id.txtCaseSplit)).setVisibility(View.GONE);
					if(!Utils.getStringSetting(mContext, Global.PREF_QTY_ENTRY_METHOD).equals("A"))
					{
						((EditText) mRootView.findViewById(R.id.editQty)).setText(String.valueOf(nQty));
					}
					else
					{
						((EditText) mRootView.findViewById(R.id.editQty)).setText("");
					}
				}
			}
			else
			{
				((EditText) mRootView.findViewById(R.id.editCase)).setVisibility(View.GONE);
				(mRootView.findViewById(R.id.txtCaseSplit)).setVisibility(View.GONE);
				if(!Utils.getStringSetting(mContext, Global.PREF_QTY_ENTRY_METHOD).equals("A"))
				{
					((EditText) mRootView.findViewById(R.id.editCase)).setText(String.valueOf(0));
					((EditText)mRootView.findViewById(R.id.editQty)).setTag("From App");
					((EditText) mRootView.findViewById(R.id.editQty)).setText(strQty);
					((EditText)mRootView.findViewById(R.id.editQty)).setTag(null);
				}
				else
				{
					((EditText) mRootView.findViewById(R.id.editCase)).setText(String.valueOf(0));
					((EditText)mRootView.findViewById(R.id.editQty)).setTag("From App");
					((EditText) mRootView.findViewById(R.id.editQty)).setText("0.00");
					((EditText)mRootView.findViewById(R.id.editQty)).setTag(null);
				}
			}
		}
		else
		{
			if(bShowCase)
			{
				((EditText) mRootView.findViewById(R.id.editCase)).setVisibility(View.VISIBLE);
				(mRootView.findViewById(R.id.txtCaseSplit)).setVisibility(View.VISIBLE);
				((TextView) mRootView.findViewById(R.id.txtDescription)).setText("");
				if(!Utils.getStringSetting(mContext, Global.PREF_QTY_ENTRY_METHOD).equals("A"))
				{
					((EditText) mRootView.findViewById(R.id.editQty)).setText("0");
					((EditText) mRootView.findViewById(R.id.editCase)).setText("0");
				}
				else
				{
					((EditText) mRootView.findViewById(R.id.editQty)).setText("");
					((EditText) mRootView.findViewById(R.id.editCase)).setText("");
				}

			}
			else
			{
				((EditText) mRootView.findViewById(R.id.editCase)).setVisibility(View.GONE);
				(mRootView.findViewById(R.id.txtCaseSplit)).setVisibility(View.GONE);
				((TextView) mRootView.findViewById(R.id.txtDescription)).setText("");
				if(!Utils.getStringSetting(mContext, Global.PREF_QTY_ENTRY_METHOD).equals("A"))
				{
					((EditText) mRootView.findViewById(R.id.editQty)).setText("0");
				}
				else
				{
					((EditText) mRootView.findViewById(R.id.editQty)).setText("");
				}
			}
		}
		//showKeyboard(((EditText) mRootView.findViewById(R.id.editCase)), true);
	}

	private void doAdd() {
		mCurSelInvenDInfo = null;
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

		ProdDetlInfo productInfo = mProdDetlList.get(mProdDetlIdx);
		if(productInfo.prodLocnInfo.nSCWType != Global.WEIGHT_ITEM)
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

		for (int i = 0; i < mUInvenDList.size(); i++) {
			if (mUInvenDList.get(i).strItemNo.equals(productInfo.strItemNo) == true) {
				nIdx = i;
				break;
			}
		}

		if(Utils.getProdLoconSCWType(mContext, productInfo.strItemNo) != Global.WEIGHT_ITEM)
		{
			if(bShowCase)
			{
				int nCase = getCaseValue(productInfo.strItemNo);
				int nQuantity = Utils.parseIntFromString(strQty) + Utils.parseIntFromString(strCase) * nCase;
				if (nIdx == -1) {
					if (nQuantity == 0) {
						Utils.printDebug(mContext, Str.Please_input_the_Quantity[Global.LANG]);
						showKeyboard((EditText) mRootView.findViewById(R.id.editQty), true);
						return;
					} else {
						if(Utils.getStringSetting(mContext, Global.PREF_QTY_ENTRY_METHOD).equals("A"))
						{
							if(nQuantity < 0)
							{
								((EditText) mRootView.findViewById(R.id.editQty)).setText("");
								((EditText) mRootView.findViewById(R.id.editCase)).setText("");
								Utils.printDebug(mContext, Str.Quantity_Removed_From_the_transaction[Global.LANG]);
								showKeyboard((EditText) mRootView.findViewById(R.id.editQty), true);
								return;
							}

							UInvenDInfo info = new UInvenDInfo();
							info.strItemNo = productInfo.strItemNo;
							info.nStock += nQuantity;
							info.strShortDesc = productInfo.strShortDesc;
							info.strDescription = productInfo.strDescription;
							info.strUOM = productInfo.prodLocnInfo.strCaseUOM.length() > 0 ? productInfo.prodLocnInfo.strCaseUOM : productInfo.prodLocnInfo.strUnitUOM;
							info.strFullCase = productInfo.prodLocnInfo.strFullCase;
							info.nWeight = productInfo.nWeight;

							if(productInfo.strLotControl.equalsIgnoreCase("Y") && Utils.getIntSetting(mContext, Global.PREF_LOAD_CUSTOMER) != 1)
							{
								info.bLotControl = true;
							}

							info.bShow = true;
							info.nOrder = getMaxOrder() + 1;
							mUInvenDList.add(info);
							mCurSelInvenDInfo = info;
						}
						else
						{
							UInvenDInfo info = new UInvenDInfo();
							info.strItemNo = productInfo.strItemNo;
							info.nStock = nQuantity;
							info.strShortDesc = productInfo.strShortDesc;
							info.strDescription = productInfo.strDescription;
							info.strUOM = productInfo.prodLocnInfo.strCaseUOM.length() > 0 ? productInfo.prodLocnInfo.strCaseUOM : productInfo.prodLocnInfo.strUnitUOM;
							info.strFullCase = productInfo.prodLocnInfo.strFullCase;
							info.nWeight = productInfo.nWeight;

							if(productInfo.strLotControl.equalsIgnoreCase("Y") && Utils.getIntSetting(mContext, Global.PREF_LOAD_CUSTOMER) != 1)
							{
								info.bLotControl = true;
							}

							info.bShow = true;
							info.nOrder = getMaxOrder() + 1;
							mUInvenDList.add(info);
							mCurSelInvenDInfo = info;
						}

					}
				} else {
					if(Utils.getStringSetting(mContext, Global.PREF_QTY_ENTRY_METHOD).equals("A"))
					{
						//mUInvenDList.get(nIdx).nStock += nQuantity;
						if(mUInvenDList.get(nIdx).nStock + nQuantity < 0)
						{
							((EditText) mRootView.findViewById(R.id.editQty)).setText("");
							((EditText) mRootView.findViewById(R.id.editCase)).setText("");
							Utils.printDebug(mContext, Str.Quantity_Removed_From_the_transaction[Global.LANG]);
							showKeyboard((EditText) mRootView.findViewById(R.id.editQty), true);
							refreshPage();
							return;
						}
						mUInvenDList.get(nIdx).nStock += nQuantity;
						if (mUInvenDList.get(nIdx).nStock == 0) {
							if( mUInvenDList.get(nIdx).nLoaded == 0)
							{
								mUInvenDList.remove(nIdx);
							}
						} else {
							mCurSelInvenDInfo = mUInvenDList.get(nIdx);
							mCurSelInvenDInfo.bShow = true;
							if (mUInvenDList.get(nIdx).nLoaded < mUInvenDList.get(nIdx).nStock) {
								Utils.printDebug(mContext, Str.ActualLoadQuantityExceedsPlan[Global.LANG]);
							}
						}
					}
					else
					{
						if (nQuantity == 0 && mUInvenDList.get(nIdx).nLoaded == 0) {
							mUInvenDList.remove(nIdx);
						} else {
							mCurSelInvenDInfo = mUInvenDList.get(nIdx);
							mCurSelInvenDInfo.bShow = true;
							mUInvenDList.get(nIdx).nStock = nQuantity;
							if (mUInvenDList.get(nIdx).nLoaded < nQuantity) {
								Utils.printDebug(mContext, Str.ActualLoadQuantityExceedsPlan[Global.LANG]);
							}
						}
					}
				}

				((EditText) mRootView.findViewById(R.id.editItemNo)).setText("");
				((EditText) mRootView.findViewById(R.id.editQty)).setText("");
				((EditText) mRootView.findViewById(R.id.editCase)).setText("");
				focusAndHideKeyboard((EditText) mRootView.findViewById(R.id.editItemNo));
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
						if(Utils.getStringSetting(mContext, Global.PREF_QTY_ENTRY_METHOD).equals("A"))
						{
							if(nQuantity < 0)
							{
								((EditText) mRootView.findViewById(R.id.editQty)).setText("");
								Utils.printDebug(mContext, Str.Quantity_Removed_From_the_transaction[Global.LANG]);
								showKeyboard((EditText) mRootView.findViewById(R.id.editQty), true);
								return;
							}
							UInvenDInfo info = new UInvenDInfo();
							info.strItemNo = productInfo.strItemNo;
							info.nStock += nQuantity;
							info.strShortDesc = productInfo.strShortDesc;
							info.strDescription = productInfo.strDescription;
							info.strUOM = productInfo.prodLocnInfo.strCaseUOM.length() > 0 ? productInfo.prodLocnInfo.strCaseUOM : productInfo.prodLocnInfo.strUnitUOM;
							info.strFullCase = productInfo.prodLocnInfo.strFullCase;
							info.nWeight = productInfo.nWeight;

							if(productInfo.strLotControl.equalsIgnoreCase("Y") && Utils.getIntSetting(mContext, Global.PREF_LOAD_CUSTOMER) != 1)
							{
								info.bLotControl = true;
							}

							info.bShow = true;
							info.nOrder = getMaxOrder() + 1;
							mUInvenDList.add(info);
							mCurSelInvenDInfo = info;
						}
						else
						{
							UInvenDInfo info = new UInvenDInfo();
							info.strItemNo = productInfo.strItemNo;
							info.nStock = nQuantity;
							info.strShortDesc = productInfo.strShortDesc;
							info.strDescription = productInfo.strDescription;
							info.strUOM = productInfo.prodLocnInfo.strCaseUOM.length() > 0 ? productInfo.prodLocnInfo.strCaseUOM : productInfo.prodLocnInfo.strUnitUOM;
							info.strFullCase = productInfo.prodLocnInfo.strFullCase;
							info.nWeight = productInfo.nWeight;

							if(productInfo.strLotControl.equalsIgnoreCase("Y") && Utils.getIntSetting(mContext, Global.PREF_LOAD_CUSTOMER) != 1)
							{
								info.bLotControl = true;
							}

							info.bShow = true;
							info.nOrder = getMaxOrder() + 1;
							mUInvenDList.add(info);
							mCurSelInvenDInfo = info;
						}
					}
				} else {
					if(Utils.getStringSetting(mContext, Global.PREF_QTY_ENTRY_METHOD).equals("A"))
					{
						if(mUInvenDList.get(nIdx).nStock + nQuantity < 0)
						{
							((EditText) mRootView.findViewById(R.id.editQty)).setText("");
							Utils.printDebug(mContext, Str.Quantity_Removed_From_the_transaction[Global.LANG]);
							((EditText) mRootView.findViewById(R.id.editQty)).setText("");
							showKeyboard((EditText) mRootView.findViewById(R.id.editQty), true);
							refreshPage();
							return;
						}

						mUInvenDList.get(nIdx).nStock += nQuantity;
						if (mUInvenDList.get(nIdx).nStock == 0) {
							if(mUInvenDList.get(nIdx).nLoaded == 0)
							{
								mUInvenDList.remove(nIdx);
							}
						} else {
							mCurSelInvenDInfo = mUInvenDList.get(nIdx);
							mCurSelInvenDInfo.bShow = true;
							if (mUInvenDList.get(nIdx).nLoaded < mUInvenDList.get(nIdx).nStock) {
								Utils.printDebug(mContext, Str.ActualLoadQuantityExceedsPlan[Global.LANG]);
							}
						}
					}
					else
					{
						if (nQuantity == 0 && mUInvenDList.get(nIdx).nLoaded == 0) {
							mUInvenDList.remove(nIdx);
						} else {
							mCurSelInvenDInfo = mUInvenDList.get(nIdx);
							mCurSelInvenDInfo.bShow = true;
							mUInvenDList.get(nIdx).nStock = nQuantity;
							if (mUInvenDList.get(nIdx).nLoaded < nQuantity) {
								Utils.printDebug(mContext, Str.ActualLoadQuantityExceedsPlan[Global.LANG]);
							}
						}
					}
				}

				((EditText) mRootView.findViewById(R.id.editItemNo)).setText("");
				((EditText) mRootView.findViewById(R.id.editQty)).setText("");
				focusAndHideKeyboard((EditText) mRootView.findViewById(R.id.editItemNo));

			}
		}
		else
		{
			double dQty = Utils.parseDoubleFromString(strQty);
			if(nIdx == -1){
				if (dQty == 0) {
					Utils.printDebug(mContext, Str.Please_input_the_Quantity[Global.LANG]);
					showKeyboard((EditText) mRootView.findViewById(R.id.editQty), true);
					return;
				} else {
					if(Utils.getStringSetting(mContext, Global.PREF_QTY_ENTRY_METHOD).equals("A"))
					{
						if(dQty < 0)
						{
							((EditText) mRootView.findViewById(R.id.editQty)).setText("");
							Utils.printDebug(mContext, Str.Quantity_Removed_From_the_transaction[Global.LANG]);
							showKeyboard((EditText) mRootView.findViewById(R.id.editQty), true);
							return;
						}
						UInvenDInfo info = new UInvenDInfo();
						info.strItemNo = productInfo.strItemNo;
						info.dStock += dQty;
						info.strShortDesc = productInfo.strShortDesc;
						info.strDescription = productInfo.strDescription;
						info.strUOM = productInfo.prodLocnInfo.strCaseUOM.length() > 0 ? productInfo.prodLocnInfo.strCaseUOM : productInfo.prodLocnInfo.strUnitUOM;
						info.strFullCase = productInfo.prodLocnInfo.strFullCase;
						info.nWeight = productInfo.nWeight;

						if(productInfo.strLotControl.equalsIgnoreCase("Y") && Utils.getIntSetting(mContext, Global.PREF_LOAD_CUSTOMER) != 1)
						{
							info.bLotControl = true;
						}

						info.bShow = true;
						info.nOrder = getMaxOrder() + 1;
						mUInvenDList.add(info);
						mCurSelInvenDInfo = info;
					}
					else
					{
						UInvenDInfo info = new UInvenDInfo();
						info.strItemNo = productInfo.strItemNo;
						info.dStock = dQty;
						info.strShortDesc = productInfo.strShortDesc;
						info.strDescription = productInfo.strDescription;
						info.strUOM = productInfo.prodLocnInfo.strCaseUOM.length() > 0 ? productInfo.prodLocnInfo.strCaseUOM : productInfo.prodLocnInfo.strUnitUOM;
						info.strFullCase = productInfo.prodLocnInfo.strFullCase;
						info.nWeight = productInfo.nWeight;

						if(productInfo.strLotControl.equalsIgnoreCase("Y") && Utils.getIntSetting(mContext, Global.PREF_LOAD_CUSTOMER) != 1)
						{
							info.bLotControl = true;
						}

						info.bShow = true;
						info.nOrder = getMaxOrder() + 1;
						mUInvenDList.add(info);
						mCurSelInvenDInfo = info;
					}
				}
			}
			else
			{
				if(Utils.getStringSetting(mContext, Global.PREF_QTY_ENTRY_METHOD).equals("A"))
				{

					if(mUInvenDList.get(nIdx).dStock + dQty < 0)
					{
						((EditText) mRootView.findViewById(R.id.editQty)).setText("");
						Utils.printDebug(mContext, Str.Quantity_Removed_From_the_transaction[Global.LANG]);
						showKeyboard((EditText) mRootView.findViewById(R.id.editQty), true);
						refreshPage();
						return;
					}

					mUInvenDList.get(nIdx).dStock += dQty;

					if(mUInvenDList.get(nIdx).dStock == 0 && mUInvenDList.get(nIdx).dLoaded == 0){
						mUInvenDList.remove(nIdx);
					}
					else if (mUInvenDList.get(nIdx).dLoaded < mUInvenDList.get(nIdx).dStock) {
						mCurSelInvenDInfo = mUInvenDList.get(nIdx);
						mCurSelInvenDInfo.bShow = true;
						Utils.printDebug(mContext, Str.ActualLoadQuantityExceedsPlan[Global.LANG]);
					}
					else
					{
						mCurSelInvenDInfo = mUInvenDList.get(nIdx);
						mCurSelInvenDInfo.bShow = true;
					}
				}
				else
				{
					if (dQty == 0 && mUInvenDList.get(nIdx).dLoaded == 0) {
						mUInvenDList.remove(nIdx);
					} else {
						mCurSelInvenDInfo = mUInvenDList.get(nIdx);
						mCurSelInvenDInfo.bShow = true;
						mUInvenDList.get(nIdx).dStock = dQty;
						if (mUInvenDList.get(nIdx).dLoaded < dQty) {
							Utils.printDebug(mContext, Str.ActualLoadQuantityExceedsPlan[Global.LANG]);
						}
					}
				}

			}
			if(bShowCase)
			{
				(mRootView.findViewById(R.id.editCase)).setVisibility(View.VISIBLE);
				(mRootView.findViewById(R.id.txtCaseSplit)).setVisibility(View.VISIBLE);
				((EditText) mRootView.findViewById(R.id.editItemNo)).setText("");
				((EditText) mRootView.findViewById(R.id.editQty)).setText("");
				((EditText) mRootView.findViewById(R.id.editCase)).setText("");
			}
			else
			{
				(mRootView.findViewById(R.id.editCase)).setVisibility(View.GONE);
				(mRootView.findViewById(R.id.txtCaseSplit)).setVisibility(View.GONE);
				((EditText) mRootView.findViewById(R.id.editItemNo)).setText("");
				((EditText) mRootView.findViewById(R.id.editQty)).setText("");
				((EditText) mRootView.findViewById(R.id.editCase)).setText("");
			}
		}

		focusAndHideKeyboard((EditText) mRootView.findViewById(R.id.editItemNo));
		refreshPage();

		doCheckLoadBatch();
	}

	private void doCheckLoadBatch() {
		if(mCurSelInvenDInfo == null) return;

		if(mCurSelInvenDInfo.bLotControl && (mCurSelInvenDInfo.loadBlotInfoList == null || mCurSelInvenDInfo.loadBlotInfoList.size() < mCurSelInvenDInfo.nStock) )
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
							loadBlotInfo.strBatchDate = new SimpleDateFormat("yyyyMMdd").format(Utils.getRoutectlTripDate(mContext, new Date()));
							loadBlotInfo.strLotNumber = strBatchCodes1[i];
							loadBlotInfo.nLotQty = 1;
							mCurSelInvenDInfo.loadBlotInfoList.add(loadBlotInfo);
						}
						else
						{
							mCurSelInvenDInfo.loadBlotInfoList.get(i).strLotNumber = strBatchCodes1[i];
						}
					}
				}, mCurSelInvenDInfo.nStock, strBatchCodes, null);
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
				inputDlg.mStrInputed = mCurSelInvenDInfo.strBatchCode;
				inputDlg.setCancelable(false);
				inputDlg.setOnDismissListener(dialog -> {
					if (inputDlg.mReturnCode == Global.DLG_RETURN_LEFT) {
						mCurSelInvenDInfo.strBatchCode = inputDlg.mStrInputed;
					}
				});
				inputDlg.show();
			}catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	private void doProcessDone()
	{
		if (mPickLocationCodeCnt > 0 && mPickLocationCodeCnt > Global.gLocationFilterList.size()) {
			try {
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
			}catch (Exception e)
			{
				e.printStackTrace();
			}

		} else {
			Utils.setBooleanSetting(mContext, Global.PREF_LOAD_INVENTORY_COMPLETED, true);
			gotoNextFragment();
		}
	}

	private void doDone() {

		if(Utils.getStringSetting(mContext, Global.PREF_LOAD_BATCH).equals("1"))
		{
			List<UInvenDInfo> uUnknownBatchList = new ArrayList<>();
			for(UInvenDInfo uInvenDInfo : mUInvenDList)
			{
				if(TextUtils.isEmpty(uInvenDInfo.strBatchCode) && uInvenDInfo.bShow == true)
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
							doProcessDone();
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
		doProcessDone();


	}

	private void gotoNextFragment() {
		boolean bLoadAdjustment = false;
		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		List<DescTypeInfo> descTypeInfos = dbAdapter.getDescTypeList("LOADAJUST");
		dbAdapter.close();
		if( descTypeInfos != null && descTypeInfos.size() > 0 )
		{
			for(UInvenDInfo info : mUInvenDList)
			{
				if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
				{
					if( info.dLoaded != info.dStock )
					{
						bLoadAdjustment = true;
						break;
					}
				}
				else
				{
					if( info.nLoaded != info.nStock )
					{
						bLoadAdjustment = true;
						break;
					}
				}

			}
		}

		if( bLoadAdjustment )
		{
			mContext.replaceFragment(new LoadAdjustmentsFragment());
		}
		else
		{
			if (Utils.getIntSetting(mContext, Global.PREF_LOAD_CONTAINERS) == 1) {
				mContext.replaceFragment(new LoadContainersFragment());
			} else {
				mContext.replaceFragment(new LoadTruckSignFragment());
			}
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


	private void showInvenDetail(UInvenDInfo info) {
		((EditText) mRootView.findViewById(R.id.editItemNo)).setText(Utils.getDisplayItemCode(mContext, info.strItemNo));
		if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
		{
			String strQty = String.format(Locale.ENGLISH, "%.2f", info.dStock);
			((EditText) mRootView.findViewById(R.id.editQty)).setTag("From App");
			if(!Utils.getStringSetting(mContext, Global.PREF_QTY_ENTRY_METHOD).equals("A"))
			{
				((EditText) mRootView.findViewById(R.id.editQty)).setText(strQty);
			}
			else
			{
				((EditText) mRootView.findViewById(R.id.editQty)).setText("0.00");
			}
			((EditText) mRootView.findViewById(R.id.editQty)).setTag(null);
			(mRootView.findViewById(R.id.editCase)).setVisibility(View.GONE);
			(mRootView.findViewById(R.id.txtCaseSplit)).setVisibility(View.GONE);
			mRootView.findViewById(R.id.editQty).requestFocus();
		}
		else
		{
			if(bShowCase)
			{
				if(!Utils.getStringSetting(mContext, Global.PREF_QTY_ENTRY_METHOD).equals("A"))
				{
					int nCase = getCaseValue(info.strItemNo);

					String strQty = String.valueOf(info.nStock % nCase);
					String strCase = String.valueOf(info.nStock / nCase);
					((EditText) mRootView.findViewById(R.id.editQty)).setText(strQty);
					((EditText) mRootView.findViewById(R.id.editCase)).setText(strCase);
					mRootView.findViewById(R.id.editCase).requestFocus();
					((EditText) mRootView.findViewById(R.id.editCase)).setSelection(strCase.length());
				}
				else
				{
					((EditText) mRootView.findViewById(R.id.editQty)).setText("");
					((EditText) mRootView.findViewById(R.id.editCase)).setText("");
					mRootView.findViewById(R.id.editCase).requestFocus();
					((EditText) mRootView.findViewById(R.id.editCase)).setSelection(0);
				}
			}
			else
			{
				if(!Utils.getStringSetting(mContext, Global.PREF_QTY_ENTRY_METHOD).equals("A"))
				{
					String strQty = String.valueOf(info.nStock);
					((EditText) mRootView.findViewById(R.id.editQty)).setText(strQty);

					mRootView.findViewById(R.id.editQty).requestFocus();
					((EditText) mRootView.findViewById(R.id.editQty)).setSelection(strQty.length());
				}
				else
				{
					String strQty = String.valueOf(info.nStock);
					((EditText) mRootView.findViewById(R.id.editQty)).setText("");

					mRootView.findViewById(R.id.editQty).requestFocus();
					((EditText) mRootView.findViewById(R.id.editQty)).setSelection(0);
				}

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

	public boolean m_bClicked = false;
	@Override
	public void onClick(View v) {
		if( m_bClicked ) return;
		m_bClicked = true;
		switch (v.getId()) {
			case R.id.btnBack:
				{
					DbAdapter dbAdapter = new DbAdapter(mContext);
					dbAdapter.open();
					dbAdapter.updateSignedLoadHeader();
					dbAdapter.close();
					mUInvenDList.clear();
					Global.gUInvenHinfo.truckList.clear();
				}
				mContext.onBackPressed();
				break;
			case R.id.btnSort:
				doSort();
				break;
			case R.id.btnFilter:
				doFilter();
				break;
			case R.id.btnFilterBy:
				{
					Global.gPrevLocationFilterList.clear();
					Global.gPrevLocationFilterList.addAll(Global.gLocationFilterList);
					FilterLocationFragment fragment = new FilterLocationFragment();
					Bundle bundle = new Bundle();
					bundle.putSerializable(Global.KEY_PICKLOCN, null);
					fragment.setArguments(bundle);
					mContext.replaceFragment(fragment);
				}

				break;
			case R.id.btnAdd:
				doAdd();
				break;
			case R.id.btnProductTreeList:
				DbAdapter dbAdapter = new DbAdapter(mContext);
				dbAdapter.open();
				List<ProdStruInfo> prodStruList = dbAdapter.getProdStruList();
				dbAdapter.close();
				if( prodStruList != null && prodStruList.size() > 0 )
				{
					mContext.replaceFragment(new NewProductTreelistFragment().setFrom(this));
				}
				break;
			case R.id.btnBarcodeScanner:
				Intent intent = new Intent(mContext, BarcodeScannerActivity.class);
				intent.putExtra(Global.EXTRA_BARCODESCAN_FROM, getClass().getSimpleName());
				startActivity(intent);
				mContext.overridePendingTransition(R.anim.start1, R.anim.start2);
				break;
			case R.id.btnProductSearch:
				{
					SearchProductFragment fragment = new SearchProductFragment();
					Bundle args = new Bundle();
					args.putBoolean(Global.EXTRA_FILTER, Utils.getIntSetting(mContext, Global.PREF_INVENCKECK) != 0);
					fragment.setArguments(args);
					mContext.replaceFragment(fragment);
				}
				break;
			case R.id.btnRefresh:
				SpecialLoadProcessWithApi();
				break;
			case R.id.btnDone:
				doDone();
				break;
		}
		m_bClicked = false;
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


	private class ListAdapter extends BaseAdapter {

		private List<UInvenDInfo> list = new ArrayList<>();

		private class ViewHolder {
			public TextView txtItemNo, txtShortDesc, txtPickLocationCode, txtLoaded;
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
				holder.txtLoaded.setText(String.format(Locale.ENGLISH, "%.2f", info.dStock));
			}
			else
			{
				int nCase = getCaseValue(info.strItemNo);
				if(bShowCase)
				{
					if (Utils.isContainerItem(mContext, info.strItemNo)) {
						holder.txtLoaded.setText(String.format(Locale.ENGLISH, "%d", info.nStock));
					}
					else {
						holder.txtLoaded.setText(String.format(Locale.ENGLISH, "%d/%d", info.nStock / nCase, info.nStock % nCase));
					}
				}
				else
				{
					holder.txtLoaded.setText(String.format(Locale.ENGLISH, "%d", info.nStock));
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
				holder.txtLoaded.setText(String.format(Locale.ENGLISH, "%.2f", info.dLoaded));
				holder.txtStock.setText(String.format(Locale.ENGLISH, "%.2f", info.dStock));
			}
			else
			{
				int nCase = getCaseValue(info.strItemNo);
				if(bShowCase)
				{
					if (Utils.isContainerItem(mContext, info.strItemNo)) {
						holder.txtLoaded.setText(String.format(Locale.ENGLISH, "%d", info.nLoaded));
						holder.txtStock.setText(String.format(Locale.ENGLISH, "%d", info.nStock));
					}
					else {
						holder.txtLoaded.setText(String.format(Locale.ENGLISH, "%d/%d", info.nLoaded / nCase, info.nLoaded % nCase));
						holder.txtStock.setText(String.format(Locale.ENGLISH, "%d/%d", info.nStock / nCase, info.nStock % nCase));
					}
				}
				else
				{
					holder.txtLoaded.setText(String.format(Locale.ENGLISH, "%d", info.nLoaded ));
					holder.txtStock.setText(String.format(Locale.ENGLISH, "%d", info.nStock));
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
															loadHeadInfo.strDescription = loadHead.Description;
															loadHeadInfo.nPeriodNo = loadHead.PeriodNo;
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

												DbAdapter adapter = new DbAdapter(mContext);
												adapter.open();
												Date curDate = new Date(System.currentTimeMillis());
												SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
												String strDate = format.format(Utils.getRoutectlTripDate(mContext, curDate));
												List<LoadHeadInfo> datas = adapter.getRLoadHeadInfo(strDate);

												if( datas.size() > 0 )
												{
													for(LoadHeadInfo info : datas)
													{
														info.bLoadTripDataFlag = true;
														adapter.updateLoadHeadInfo(info);
													}
												}

												datas = adapter.getLoadUnProcessedHeadInfo();
												adapter.close();

												if(mContext == null)
												{
													mContext = (MainActivity)getActivity();
												}

												String strUserOrdersAsLoad = Utils.getStringSetting(mContext, Global.PREF_USE_ORDERS_AS_LOAD);

												try {
													if(datas != null && datas.size() > 0 && (strUserOrdersAsLoad.equals("0") || strUserOrdersAsLoad.isEmpty()))
													{
														if (datas.size() >= 2) {
															new Handler(Looper.getMainLooper()).post(new Runnable() {
																@Override
																public void run() {
																	Utils.setBooleanSetting(mContext, Global.PREF_FIRST_CHECK_LOAD, true);
																	hideProgressDialog();

																	Toast.makeText(mContext, Str.new_load_found[Global.LANG], Toast.LENGTH_LONG).show();

																	// show popup dialog
																	LoadDataCheckPopupDialog dialog = new LoadDataCheckPopupDialog(mContext);
																	dialog.callback = LoadTruckBulkFragment.this;
																	dialog.bBackPreTrip = true;
																	dialog.bShouldJustReturn = true;
																	dialog.setCancelable(false);
																	dialog.show();
																}
															});
														}
														else {
															// just load the single load
															DbAdapter adapter1 = new DbAdapter(mContext);
															adapter1.open();
															datas = adapter1.getLoadUnProcessedHeadInfo();
															LoadHeadInfo theLoadHeadInfo = datas.get(0);
															theLoadHeadInfo.bLoadTripDataFlag = true;
															adapter.updateLoadHeadInfo(theLoadHeadInfo);
															adapter1.close();

															new Handler(Looper.getMainLooper()).post(new Runnable() {
																@Override
																public void run() {
																	Utils.setBooleanSetting(mContext, Global.PREF_FIRST_CHECK_LOAD, true);
																	hideProgressDialog();

																	Toast.makeText(mContext, Str.new_load_found[Global.LANG], Toast.LENGTH_LONG).show();
																	processLoad();
																}
															});
														}
													}
													else {
														new Handler(Looper.getMainLooper()).post(new Runnable() {
															@Override
															public void run() {
																Utils.setBooleanSetting(mContext, Global.PREF_FIRST_CHECK_LOAD, true);
																hideProgressDialog();
																processLoad();
															}
														});
													}
												}catch (Exception e)
												{
													e.printStackTrace();
												}

											}
										}.start();
									}
									else
									{
										Utils.setBooleanSetting(mContext, Global.PREF_FIRST_CHECK_LOAD, true);
										hideProgressDialog();
										processLoad();
									}
								}

								@Override
								public void onFailure(Call<LoadTripResponse> call, Throwable t) {
									Utils.setBooleanSetting(mContext, Global.PREF_FIRST_CHECK_LOAD, true);
									hideProgressDialog();
									processLoad();
								}
							});
						}
						else
						{
							Utils.setBooleanSetting(mContext, Global.PREF_FIRST_CHECK_LOAD, true);
							hideProgressDialog();
							processLoad();
						}
					}

					@Override
					public void onFailure(Call<LoginInfo> call, Throwable t) {
						Utils.setBooleanSetting(mContext, Global.PREF_FIRST_CHECK_LOAD, true);
						hideProgressDialog();
						processLoad();
					}
				});
			}catch (Exception e)
			{
				e.printStackTrace();
				Utils.setBooleanSetting(mContext, Global.PREF_FIRST_CHECK_LOAD, true);
				hideProgressDialog();
				processLoad();
			}
		}
		else
		{
			processLoad();
		}
	}

	@Override
	public void onLoadDataSelected() {
		processLoad();
	}

	public void processLoad() {
		loadData();

//		showProgressDialog(Str.Loading[Global.LANG]);
//		new Thread()
//		{
//			@Override
//			public void run() {
//				DbAdapter dbAdapter = new DbAdapter(mContext);
//				dbAdapter.open();
//				List<LoadHeadInfo> loadHeadInfoList = dbAdapter.getLoadProcessedHeadInfo();
//				if( Utils.getBooleanSetting(mContext, Global.PREF_PASSED_LOAD_SECTION) == false &&
//						(loadHeadInfoList == null || loadHeadInfoList.size() == 0 ) &&
//						Utils.getIntSetting(mContext, Global.PREF_LOADMULTIALL) == 0)
//				{
//					boolean bLoaded = dbAdapter.getSelectedLoadHeadInfo().size() > 0;
//					List<LoadHeadInfo> unProcessedHeaderInfoList = dbAdapter.getLoadUnProcessedHeadInfo();
//					if( bLoaded || unProcessedHeaderInfoList.size() == 0 )
//					{
//						dbAdapter.close();
//						new Handler(Looper.getMainLooper()).post(new Runnable() {
//							@Override
//							public void run() {
//								hideProgressDialog();
//								commonInit();
//							}
//						});
//						return;
//					}
//					else
//					{
//						Date curDate = new Date(System.currentTimeMillis());
//						SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
//						String strDate = format.format(curDate);
//						List<LoadHeadInfo> datas = dbAdapter.getLoadHeadInfo(strDate);
//
//						if( datas.size() > 0 )
//						{
//							for(LoadHeadInfo info : datas)
//							{
//								info.bLoadTripDataFlag = true;
//								dbAdapter.updateLoadHeadInfo(info);
//							}
//							dbAdapter.close();
//							new Handler(Looper.getMainLooper()).post(new Runnable() {
//								@Override
//								public void run() {
//									hideProgressDialog();
//									loadData();
//								}
//							});
//							return;
//						}
//						dbAdapter.close();
//						if(mContext == null)
//						{
//							mContext = (MainActivity)getActivity();
//						}
//						new Handler(Looper.getMainLooper()).post(new Runnable() {
//							@Override
//							public void run() {
//								hideProgressDialog();
//								try {
//									LoadDataCheckPopupDialog dialog = new LoadDataCheckPopupDialog(mContext);
//									dialog.bBackPreTrip = true;
//									dialog.bShouldJustReturn = true;
//									//dialog.setNextScreen(PreTripTasksFragment.this);
//									dialog.setCancelable(false);
//									dialog.show();
//								}catch (Exception e)
//								{
//									e.printStackTrace();
//								}
//							}
//						});
//
//					}
//				}
//				else
//				{
//					//Load "R" only
//					List<LoadHeadInfo> datas = dbAdapter.getRLoadHeadInfo();
//					if( datas.size() > 0 )
//					{
//						Date date = new Date();
//						long trxnNo = date.getTime();
//						List<LoadDetlInfo> allLoadDetlInfoList = new ArrayList<>();
//
//						Map<String, Double> rBeginQtyMap = new HashMap<>();
//						for(LoadHeadInfo info : datas)
//						{
//							info.bLoadTripDataFlag = true;
//							info.bLoadCompleted = true;
//							dbAdapter.updateLoadHeadInfo(info);
//
//							UStockHInfo stockHInfo = new UStockHInfo();
//							stockHInfo.nTrxnNo = trxnNo++;
//							int nDocNo = Utils.getIntSetting(mContext, String.format(Locale.ENGLISH, Global.PREF_PDF_SEQUENCE_NO, Global.gTripNumber));
//
//							String strRRR;
//							if (Global.gRouteNumber.length() > 3) {
//								strRRR = Global.gRouteNumber.substring(Global.gRouteNumber.length() - 3);
//							} else {
//								strRRR = Global.gRouteNumber;
//							}
//							String strY = new SimpleDateFormat("yyyy").format(new Date()).substring(3);
//							Calendar cal = Calendar.getInstance();
//							int nDayOfYear = cal.get(Calendar.DAY_OF_YEAR);
//							String strJJJ = String.format(Locale.ENGLISH, "%03d", nDayOfYear);
//							String strSSS = String.format(Locale.ENGLISH, "%03d", nDocNo);
//							stockHInfo.strDocNo = strRRR + strY + strJJJ + strSSS;
//
//							stockHInfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(date);
//							stockHInfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(date);
//
//							stockHInfo.strReference = info.strType;
//
//							if(info.strType.equals("R"))
//							{
//								stockHInfo.nTrxnNo = setGeneralUploadData("LOAD", 0, 0, stockHInfo.nTrxnNo, "Rollover", "");
//								TransactionInfo transactionInfo = new TransactionInfo();
//								transactionInfo.strType = "LOAD";
//								transactionInfo.strTime = new SimpleDateFormat("kk:mm").format(new Date(stockHInfo.nTrxnNo));
//								dbAdapter.insertTransactionHistoryInfo(transactionInfo);
//
//								List<LoadBlotInfo> loadBlotInfoList = dbAdapter.getLoadBlot(info.strDetailFile);
//								Map<String, List<LoadBlotInfo>> loadBlotMap = new HashMap<>();
//								for(LoadBlotInfo loadBlotInfo : loadBlotInfoList)
//								{
//									List<LoadBlotInfo> infoList = loadBlotMap.get(loadBlotInfo.strItemNo);
//									if(infoList == null)
//									{
//										infoList = new ArrayList<>();
//										loadBlotMap.put(loadBlotInfo.strItemNo, infoList);
//									}
//									infoList.add(loadBlotInfo);
//								}
//
//								List<LoadDetlInfo> loadDetlInfoList = dbAdapter.getRLoadDetl(info.strDetailFile);
//								allLoadDetlInfoList.addAll(loadDetlInfoList);
//								for(LoadDetlInfo loadDetlInfo : loadDetlInfoList)
//								{
//									UStockDInfo stockDInfo = new UStockDInfo();
//									stockDInfo.nTrxnNo = stockHInfo.nTrxnNo;
//									stockDInfo.strItemNo = loadDetlInfo.strItemNo;
//									stockDInfo.nReasonCode = 0;
//									if(Utils.getProdLoconSCWType(mContext, loadDetlInfo.strItemNo) == Global.WEIGHT_ITEM)
//									{
//										double nValue = 0;
//										if(rBeginQtyMap.get(loadDetlInfo.strItemNo) != null)
//										{
//											try {
//												nValue += rBeginQtyMap.get(loadDetlInfo.strItemNo);
//											}catch (Exception e)
//											{
//												e.printStackTrace();
//											}
//										}
//										stockDInfo.dActualQty = loadDetlInfo.dQty;
//										stockDInfo.dBeginQty = nValue;
//										stockDInfo.dSuggestQty = loadDetlInfo.dQty;
//										nValue += loadDetlInfo.dQty;
//										rBeginQtyMap.put(loadDetlInfo.strItemNo, nValue);
//									}
//									else
//									{
//										double nValue = 0;
//										if(rBeginQtyMap.get(loadDetlInfo.strItemNo) != null)
//										{
//											try {
//												nValue += rBeginQtyMap.get(loadDetlInfo.strItemNo);
//											}catch (Exception e)
//											{
//												e.printStackTrace();
//											}
//										}
//										stockDInfo.nActualQty = loadDetlInfo.nQty;
//										stockDInfo.nBeginQty = (int)nValue;
//										stockDInfo.nSuggestQty = loadDetlInfo.nQty;
//										nValue += loadDetlInfo.dQty;
//										rBeginQtyMap.put(loadDetlInfo.strItemNo, nValue);
//									}
//
//									if(Utils.getIntSetting(mContext, Global.PREF_LOAD_CUSTOMER) != 1)
//									{
//										List<LoadBlotInfo> infoList = loadBlotMap.get(loadDetlInfo.strItemNo);
//										if(infoList != null && infoList.size() > 0)
//										{
//											ProdDetlInfo prodDetlInfo = dbAdapter.getProdDetl(loadDetlInfo.strItemNo);
//											if(prodDetlInfo.strLotControl.equalsIgnoreCase("Y"))
//											{
//												for(LoadBlotInfo loadBlotInfo : infoList)
//												{
//													BatchLot batchLot = new BatchLot();
//													batchLot.nTrxnNo = stockHInfo.nTrxnNo;
//													batchLot.strItemNo = prodDetlInfo.strItemNo;
//													batchLot.nLocnNo = stockDInfo.nLocnNo;
//													batchLot.nReasonCode = stockDInfo.nReasonCode;
//													batchLot.nSequence = stockDInfo.nSequence;
//													batchLot.strLotNumber = loadBlotInfo.strLotNumber;
//													batchLot.strBatchDate = loadBlotInfo.strBatchDate;
//													batchLot.nActualQty = loadBlotInfo.nLotQty;
//													batchLot.nSuggestQty = loadBlotInfo.nLotQty;
//													dbAdapter.insertBatchLot(batchLot);
//												}
//											}
//										}
//									}
//
//									dbAdapter.insertUStockDInfoList(stockDInfo);
//								}
//								stockHInfo.strDetailKeyExpanded = info.strDetailFile;
//								dbAdapter.insertUStockHInfoList(stockHInfo);
//							}
//						}
//
//				/*Global.gAdjustinfo = dbAdapter.getUInvenHInfo(Utils.getLongSetting(mContext, Global.PREF_UINVEN_TRXN_NO));
//				if (Global.gAdjustinfo == null) {
//					Global.gAdjustinfo = new UInvenHInfo();
//					Global.gAdjustinfo.nTrxnNo = Utils.getLongSetting(mContext, Global.PREF_UINVEN_TRXN_NO);
//				}
//
//				List<UInvenDInfo> uInvenDList = dbAdapter.getUInvenDList(Global.gAdjustinfo.nTrxnNo);*/
//						if(Utils.getBooleanSetting(mContext, Global.PREF_PASSED_LOAD_SECTION) == false && allLoadDetlInfoList.size() > 0)
//						{
//							long nTrxnNo = System.currentTimeMillis();
//							Global.gRTrxnDate = Global.gAdjustinfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(nTrxnNo);
//							Global.gRTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(nTrxnNo);
//							//Make UInventory with type = 'R'
//							if(Utils.getLongSetting(mContext, Global.PREF_UINVEN_TRXN_NO) == 0)
//							{
//								m_lTrxnNo = trxnNo;
//								Global.gUInvenHinfo = new UInvenHInfo();
//								Global.gUInvenHinfo.nTrxnNo = m_lTrxnNo;
//								Global.gUInvenHinfo.strDocType = "LOAD";
//								Global.gUInvenHinfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(new Date(m_lTrxnNo));
//								Global.gUInvenHinfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(new Date(m_lTrxnNo));
//								dbAdapter.insertUInvenHInfo(Global.gUInvenHinfo);
//
//								Utils.setLongSetting(mContext, Global.PREF_UINVEN_TRXN_NO, m_lTrxnNo);
//								setGeneralUploadData("LOAD", 0, 0, m_lTrxnNo + 1, "Rollover", "");
//								//setGeneralUploadData("LOAD", 0, 0, m_lTrxnNo + 1);
//								Global.gUInvenHinfo.nTrxnNo = m_lTrxnNo;
//								dbAdapter.insertUInvenHInfo(Global.gUInvenHinfo);
//
//								List<UInvenDInfo> uInvenDList = new ArrayList<>();
//								List<String> strCodeList = new ArrayList<>();
//								List<ProdDetlInfo> mProdDetlList = dbAdapter.getProdDetlList();
//								Map<String, Map<String, List<LoadBlotInfo>>> blotMap = new HashMap<>();
//								for (LoadDetlInfo loadDetlInfo : allLoadDetlInfoList) {
//									UInvenDInfo uInvenDInfo = new UInvenDInfo();
//									uInvenDInfo.strItemNo = loadDetlInfo.strItemNo;
//									uInvenDInfo.strPickLocationCode = loadDetlInfo.strPickLocationCode;
//									if(Utils.getProdLoconSCWType(mContext, uInvenDInfo.strItemNo) == Global.WEIGHT_ITEM)
//									{
//										uInvenDInfo.dBeginQty = loadDetlInfo.dQty;
//										uInvenDInfo.dLoaded = 0;
//										uInvenDInfo.dStock = loadDetlInfo.dQty;
//									}
//									else
//									{
//										uInvenDInfo.nBeginQty = loadDetlInfo.nQty;
//										uInvenDInfo.nLoaded = 0;
//										uInvenDInfo.nStock = loadDetlInfo.nQty;
//									}
//
//									uInvenDInfo.nSequence = uInvenDInfo.nIdx;
//									uInvenDInfo.nSCWType = Global.INVENTORY_TRUCK;
//									//uInvenDInfo.bShow = uInvenDInfo.strPickLocationCode.trim().length() > 0;
//									//Modified by Andrew 2016-9.11
//									uInvenDInfo.bShow = true;
//									for (ProdDetlInfo info : mProdDetlList) {
//										if (info.strItemNo.equals(loadDetlInfo.strItemNo) == true) {
//											uInvenDInfo.strShortDesc = info.strShortDesc;
//											uInvenDInfo.strDescription = info.strDescription;
//											uInvenDInfo.strUOM = info.prodLocnInfo.strCaseUOM.length() > 0 ? info.prodLocnInfo.strCaseUOM : info.prodLocnInfo.strUnitUOM;
//											uInvenDInfo.strFullCase = info.prodLocnInfo.strFullCase;
//											uInvenDInfo.nWeight = info.nWeight;
//
//											if(info.strLotControl.equalsIgnoreCase("Y") && Utils.getIntSetting(mContext, Global.PREF_LOAD_CUSTOMER) != 1)
//											{
//												uInvenDInfo.bLotControl = true;
//											}
//											break;
//										}
//									}
//
//									if(uInvenDInfo.bLotControl)
//									{
//										//Load the LoadBlot information from the db.
//										Map<String, List<LoadBlotInfo>> blotDetailMap = blotMap.get(loadDetlInfo.strDetailFile);
//										if(blotDetailMap == null)
//										{
//											blotDetailMap = new HashMap<>();
//											blotMap.put(loadDetlInfo.strDetailFile, blotDetailMap);
//
//											List<LoadBlotInfo> loadBlotInfoList = dbAdapter.getLoadBlot(loadDetlInfo.strDetailFile);
//
//											for(LoadBlotInfo loadBlotInfo : loadBlotInfoList)
//											{
//												List<LoadBlotInfo> infoList = blotDetailMap.get(loadBlotInfo.strItemNo);
//												if(infoList == null)
//												{
//													infoList = new ArrayList<>();
//													blotDetailMap.put(loadBlotInfo.strItemNo, infoList);
//												}
//												infoList.add(loadBlotInfo);
//											}
//										}
//
//										uInvenDInfo.loadBlotInfoList = blotDetailMap.get(uInvenDInfo.strItemNo);
//									}
//
//									uInvenDList.add(uInvenDInfo);
//									if (uInvenDInfo.strPickLocationCode.length() > 0) {
//										boolean bExist = false;
//										for (String strCode : strCodeList) {
//											if (strCode.equals(uInvenDInfo.strPickLocationCode) == true) {
//												bExist = true;
//												break;
//											}
//										}
//										if (bExist == false) {
//											strCodeList.add(uInvenDInfo.strPickLocationCode);
//										}
//									}
//
//								}
//
//								//Make 'R' Load
//								UInvenBlotInfo uInvenBlotInfo = Utils.getUInvenBlot(Global.gUInvenHinfo, "", Global.gTripNumber);
//								List<UInvenBlotDInfo> uInvenBlotDInfoList = new ArrayList<>();
//								for(UInvenDInfo info : uInvenDList)
//								{
//									info.nTrxnNo = m_lTrxnNo - 1;
//									dbAdapter.insertUInvenDInfo(info);
//
//									info.nTrxnNo = m_lTrxnNo;
//									dbAdapter.insertUInvenDInfo(info);
//									uInvenBlotDInfoList.addAll(Utils.getUInvenBlotList(info));
//								}
//
//								if(uInvenBlotDInfoList.size() > 0)
//								{
//									dbAdapter.insertULoadBlot(uInvenBlotInfo);
//									dbAdapter.insertULoadBlotList(uInvenBlotDInfoList);
//								}
//								//Set LoadCompleted Flag = 1
//							}
//						}
//					}
//					dbAdapter.close();
//
//					if(makeZipFileInfo != null && makeZipFileInfo.trxnList.size() > 0)
//					{
//						//Global.gUploadCustNoList.add(makeZipFileInfo);
//						Utils.registerUploadFile(mContext, makeZipFileInfo);
//					}
//					new Handler(Looper.getMainLooper()).post(new Runnable() {
//						@Override
//						public void run() {
//							hideProgressDialog();
//							commonInit();
//						}
//					});
//				}
//			}
//		}.start();
//
//		new Thread()
//		{
//			@Override
//			public void run() {
//				Global.gUploadCustNoList.clear();
//				PrepareUploadDbAdapter dbAdapter = new PrepareUploadDbAdapter(mContext);
//				dbAdapter.open();
//				Global.gUploadCustNoList.addAll(dbAdapter.getMakeZipFileInfoList());
//				dbAdapter.close();
//			}
//		}.start();
	}
}