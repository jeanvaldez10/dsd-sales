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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Collections;
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
import dsd.dsdvansales.model.CustomerInfo;
import dsd.dsdvansales.model.CustomerOrderInfo;
import dsd.dsdvansales.model.DescTypeInfo;
import dsd.dsdvansales.model.OrderLineInfo;
import dsd.dsdvansales.model.PresoldOrDInfo;
import dsd.dsdvansales.model.PresoldOrHInfo;
import dsd.dsdvansales.model.ProdDetlInfo;
import dsd.dsdvansales.model.ProdStruInfo;
import dsd.dsdvansales.model.UInvenDInfo;
import dsd.dsdvansales.model.UInvenHInfo;
import dsd.dsdvansales.views.AlertSingleChoiceAdapter;

public class LoadRequestFragment extends BaseFragment implements View.OnClickListener {

	private TextView mTxtNoData = null;
	private ListView mListView = null;
	private ListAdapter mListAdapter = null;
	private List<UInvenDInfo> mUInvenDList = new ArrayList<>();
	private List<ProdDetlInfo> mProdDetlList = new ArrayList<>();
	private int mProdDetlIdx = -1;
	private CustomerInfo mDriverCustomer = null;
	private Map<String, Integer> mCaseMap = new HashMap<>();
	private Map<String, ProdDetlInfo> mProdUPCDetMap = new HashMap<>();
	private Map<String, ProdDetlInfo> mProdDetlItemMap = new HashMap<>();
	private boolean m_bLoaded = false;
	private String strCartonDescription = "";
	private boolean bShowCase = true;
	private String mEntryInputMethod = "";
	int nSalesSortFlag = -1;
	int nOnTruckSortFlag = -1;
	int nSuggestSortFlag = -1;

	private boolean filter = false;

	private List<CustomerOrderInfo> latestOrderInfo = new ArrayList<>();
	private Map<String, PresoldOrDInfo> presoldOrDInfoMap = new HashMap<>();

	private UInvenHInfo mUInvenHinfo;

//	private boolean m_bLoadReqFromPresoldH = false;

	private String sortFlag = "";

	private boolean bProcessingBarCodeScan = false;

	public boolean bModified = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

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
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootView = inflater.inflate(R.layout.fragment_load_request, container, false);
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
		Global.gFragIdx = Global.FRAG_LOAD_REQUEST;
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
				if(bProcessingBarCodeScan) return;
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

		((EditText) mRootView.findViewById(R.id.editItemNo)).setOnEditorActionListener(new EditText.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_NEXT) {
					if(mProdDetlIdx != -1)
					{
						ProdDetlInfo info = mProdDetlList.get(mProdDetlIdx);
						if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
						{
							showKeyboard(((EditText) mRootView.findViewById(R.id.editQty)), true);
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

		((EditText) mRootView.findViewById(R.id.editQty)).addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

			}

			@Override
			public void afterTextChanged(Editable editable) {
				if(mRootView.findViewById(R.id.editQty).getTag() == null)
				{
					setQtyFormat();
				}
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
		mRootView.findViewById(R.id.lblSales).setOnClickListener(this);
		mRootView.findViewById(R.id.lblOnTruck).setOnClickListener(this);
		mRootView.findViewById(R.id.lblSuggest).setOnClickListener(this);

		Bundle bundle = getArguments();
		boolean bFromVehicleInventory = false;
		if( bundle != null )
		{
			try
			{
				bFromVehicleInventory = bundle.getBoolean(Global.EXTRA_FROM, false);
			}catch(Exception e)
			{
				e.printStackTrace();
			}
		}

		if (!bFromVehicleInventory) {
			String strLoadRequestEOT = Utils.getStringSetting(mContext, Global.PREF_LOAD_REQUEST_EOT);
			if (strLoadRequestEOT.equals("1")) {
				// Need to disable edit function
				mRootView.findViewById(R.id.btnAdd).setEnabled(false);
				mRootView.findViewById(R.id.editItemNo).setEnabled(false);
				mRootView.findViewById(R.id.editQty).setEnabled(false);
				mRootView.findViewById(R.id.editCase).setEnabled(false);
				mRootView.findViewById(R.id.txtCaseSplit).setEnabled(false);
				mRootView.findViewById(R.id.btnProductTreeList).setVisibility(View.INVISIBLE);
				mRootView.findViewById(R.id.btnBarcodeScanner).setVisibility(View.INVISIBLE);
				mRootView.findViewById(R.id.btnProductSearch).setVisibility(View.INVISIBLE);

			}
			else if (strLoadRequestEOT.equals("2")) {
				// Enable edit function
				mRootView.findViewById(R.id.btnAdd).setEnabled(true);
				mRootView.findViewById(R.id.editItemNo).setEnabled(true);
				mRootView.findViewById(R.id.editQty).setEnabled(true);
				mRootView.findViewById(R.id.editCase).setEnabled(true);
				mRootView.findViewById(R.id.txtCaseSplit).setEnabled(true);
				mRootView.findViewById(R.id.btnProductTreeList).setVisibility(View.VISIBLE);
				mRootView.findViewById(R.id.btnBarcodeScanner).setVisibility(View.VISIBLE);
				mRootView.findViewById(R.id.btnProductSearch).setVisibility(View.VISIBLE);
			}
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

	Map<String, Integer> mPresoldDataMap = new HashMap<>();
	private int getPresoldValue(String strItemNo, DbAdapter dbAdapter)
	{
		if( mPresoldDataMap.get(strItemNo) != null )
			return mPresoldDataMap.get(strItemNo);
		int nStock = 0;
		if(dbAdapter != null)
		{
			List<PresoldOrDInfo> presoldOrDInfos = dbAdapter.getUnusedPresoldOrDInfo(strItemNo);
			for(PresoldOrDInfo info : presoldOrDInfos)
			{
				if( info.strItemNo.equals(strItemNo) )
					nStock += info.nOrderQty;
			}
		}

		for(CustomerOrderInfo orderInfo : latestOrderInfo)
		{
			for(OrderLineInfo lineInfo : orderInfo.orderLines)
			{
				if(lineInfo.itemNumber.equals(strItemNo))
				{
					nStock += lineInfo.quantity;
				}
			}
		}

		mPresoldDataMap.put(strItemNo, nStock);
		return nStock;
	}

	Map<String, Double> mPresoldDataMapD = new HashMap<>();
	private double getPresoldValueD(String strItemNo, DbAdapter dbAdapter)
	{
		if( mPresoldDataMapD.get(strItemNo) != null )
			return mPresoldDataMapD.get(strItemNo);
		double dStock = 0;
		if(dbAdapter != null)
		{
			List<PresoldOrDInfo> presoldOrDInfos = dbAdapter.getUnusedPresoldOrDInfo(strItemNo);

			for(PresoldOrDInfo info : presoldOrDInfos)
			{
				if( info.strItemNo.equals(strItemNo) )
					dStock += info.dOrderQty;
			}
		}

		for(CustomerOrderInfo orderInfo : latestOrderInfo)
		{
			for(OrderLineInfo lineInfo : orderInfo.orderLines)
			{
				if(lineInfo.itemNumber.equals(strItemNo))
				{
					dStock += lineInfo.quantity;
				}
			}
		}

		mPresoldDataMapD.put(strItemNo, dStock);
		return dStock;
	}

	Map<String, Integer> mStockMap = new HashMap<>();
	private int getStockValue(String strItemNo) {
		if( mStockMap.get(strItemNo) != null )
			return mStockMap.get(strItemNo);
		if( mUInvenHinfo != null && mUInvenHinfo.uInvenDList != null )
		{
			int nStock = 0;
			for(UInvenDInfo info : mUInvenHinfo.uInvenDList)
			{
				if( info.strItemNo.equals(strItemNo) )
					nStock += info.nStock;
			}
			mStockMap.put(strItemNo, nStock);
			return nStock;
		}
		else
			return 0;

	}

	Map<String, Double> mStockMapD = new HashMap<>();
	private double getStockValueD(String strItemNo) {
		if( mStockMapD.get(strItemNo) != null )
			return mStockMapD.get(strItemNo);
		if( mUInvenHinfo != null && mUInvenHinfo.uInvenDList != null )
		{
			double dStock = 0;
			for(UInvenDInfo info : mUInvenHinfo.uInvenDList)
			{
				if( info.strItemNo.equals(strItemNo) ) {
					dStock += info.dStock;
				}
			}
			mStockMapD.put(strItemNo, dStock);
			return dStock;
		}
		else
			return 0;

	}

	@Override
	public void loadData() {
		if ( m_bLoaded == false )
		{
			showProgressDialog(Str.Loading[Global.LANG]);
			new Thread()
			{
				@Override
				public void run() {
					mProdDetlList.clear();
					DbAdapter dbAdapter = new DbAdapter(mContext);
					dbAdapter.open();
					List<DescTypeInfo> descTypeInfoList = dbAdapter.getDescTypeList(Global.UNITOFMEASURE_DESC_TYPE_ID);
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

					latestOrderInfo.clear();
					latestOrderInfo.addAll(dbAdapter.getAllCustomerLatestOrderList());
					mProdDetlList = dbAdapter.getProdDetlList(mProdUPCDetMap);
					dbAdapter.getProdDetlList(mProdDetlItemMap, null);
					// Get Driver Customer and Get PresoldOrH and PresoldOrD infos;
					if( mDriverCustomer == null )
					{
						String strCustNo = Utils.getStringSetting(mContext, Global.PREF_ROUTE_NUMBER);
						int nCustNo = Utils.parseIntFromString(strCustNo);
						mDriverCustomer = dbAdapter.getCustomerInfo(nCustNo, Global.DRIVER_CUSTOMER_CHAINNO);
						mDriverCustomer.presoldOrHInfo = dbAdapter.getPresoldOrHInfo(Global.DRIVER_CUSTOMER_CHAINNO, nCustNo);
					}

					if( Global.gLoadReqeustInfo == null || Global.gLoadReqeustInfo.size() == 0 )
					{
						if(Utils.getStringSetting(mContext, Global.PREF_LOAD_REQUEST_EOT).equals("1") || Utils.getStringSetting(mContext, Global.PREF_LOAD_REQUEST_EOT).equals("2"))
						{
							String strLoadRequest = Utils.getStringSetting(mContext, Global.PREF_LOAD_REQUEST_DATA);
							if(!TextUtils.isEmpty(strLoadRequest))
							{
								Gson gson = new Gson();
								List<UInvenDInfo> prevData = gson.fromJson(strLoadRequest, new TypeToken<List<UInvenDInfo>>(){}.getType());
								Global.gLoadReqeustInfo.addAll(prevData);

								mUInvenDList.clear();
								mUInvenDList.addAll(Global.gLoadReqeustInfo);
							}
						}

						if( Global.gLoadReqeustInfo == null || Global.gLoadReqeustInfo.size() == 0 )
						{
							if( Global.gLoadReqeustInfo == null )
							{
								Global.gLoadReqeustInfo = new ArrayList<>();
							}

							presoldOrDInfoMap.clear();
							List<PresoldOrDInfo> presoldOrDInfoList = new ArrayList<>();
							List<PresoldOrHInfo> presoldOrHInfoList = dbAdapter.getLoadRequestPresoldOrHInfoList(Global.gRouteNumber);
							for (PresoldOrHInfo presoldHInfo : presoldOrHInfoList)
							{
								List<PresoldOrDInfo> presoldOrDInfos = dbAdapter.getPresoldOrDList(presoldHInfo.strDetailFile);
								for(PresoldOrDInfo info : presoldOrDInfos)
								{
									PresoldOrDInfo curInfo = presoldOrDInfoMap.get(info.strItemNo);
									if(curInfo == null)
									{
										presoldOrDInfoMap.put(info.strItemNo, info);
										presoldOrDInfoList.add(info);
									}
									else
									{
										if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
										{
											curInfo.dOrderQty += curInfo.dOrderQty;
										}
										else
										{
											curInfo.nOrderQty += curInfo.nOrderQty;
										}
									}
								}
							}
							mUInvenDList.clear();
							Map<String, UInvenDInfo> mapInvenList = new HashMap<>();

							if(presoldOrDInfoList.size() > 0)
							{
								mUInvenHinfo = dbAdapter.getUInvenHInfo(Utils.getLongSetting(mContext, Global.PREF_UINVEN_TRXN_NO));
								if (mUInvenHinfo == null) {
									mUInvenHinfo = new UInvenHInfo();
								}
								mUInvenHinfo.uInvenDList = dbAdapter.getUInvenDList(Utils.getLongSetting(mContext, Global.PREF_UINVEN_TRXN_NO));
								Map<String, UInvenDInfo> uTruckMap = new HashMap<>();
								for(UInvenDInfo info : mUInvenHinfo.uInvenDList)
								{
									uTruckMap.put(info.strItemNo, info);
								}
								List<ProdDetlInfo> prodDetlList = dbAdapter.getProdDetlList();
								for (PresoldOrDInfo presoldOrDInfo : presoldOrDInfoList) {
									UInvenDInfo uInvenDInfo = new UInvenDInfo();

									uInvenDInfo.strItemNo = presoldOrDInfo.strItemNo;
									uInvenDInfo.bShow = true;
									uInvenDInfo.nType = Global.INVENTORY_FRESH;

									for (ProdDetlInfo info : prodDetlList) {
										if (info.strItemNo.equals(presoldOrDInfo.strItemNo) == true) {
											uInvenDInfo.strShortDesc = info.strShortDesc;
											uInvenDInfo.strDescription = info.strDescription;
											uInvenDInfo.strUOM = info.prodLocnInfo.strCaseUOM.length() > 0 ? info.prodLocnInfo.strCaseUOM : info.prodLocnInfo.strUnitUOM;
											break;
										}
									}

									//Calculate Par Level, Suggest, Load Quantity.
									if(Utils.getProdLoconSCWType(mContext, presoldOrDInfo.strItemNo) == Global.WEIGHT_ITEM)
									{
										UInvenDInfo truckInfo = uTruckMap.get(uInvenDInfo.strItemNo);
										if(truckInfo != null)
										{
											uInvenDInfo.dLoadTruck = truckInfo.dStock;
										}
										else
										{
											uInvenDInfo.dLoadTruck = 0;
										}
										uInvenDInfo.dStock = presoldOrDInfo.dOrderQty - uInvenDInfo.dLoadTruck;
										if(uInvenDInfo.dStock < 0)
										{
											uInvenDInfo.dStock = 0;
										}
									}
									else
									{
										UInvenDInfo truckInfo = uTruckMap.get(uInvenDInfo.strItemNo);
										if(truckInfo != null)
										{
											uInvenDInfo.nLoadTruck = truckInfo.nStock;
										}
										else
										{
											uInvenDInfo.nLoadTruck = 0;
										}
										uInvenDInfo.nStock = presoldOrDInfo.nOrderQty - uInvenDInfo.nLoadTruck;
										if(uInvenDInfo.nStock < 0)
										{
											uInvenDInfo.nStock = 0;
										}
									}
									mapInvenList.put(uInvenDInfo.strItemNo, uInvenDInfo);
									mUInvenDList.add(uInvenDInfo);
								}
//					m_bLoadReqFromPresoldH = true;
							}
							//else
							//Merge Items
							{
								List<UInvenDInfo> uInvenDList = dbAdapter.getUInvenDList(Utils.getLongSetting(mContext, Global.PREF_UINVEN_TRXN_NO));
								List<ProdDetlInfo> prodDetlList = dbAdapter.getProdDetlList();

								for (int i=0; i<uInvenDList.size(); i++) {
									for (ProdDetlInfo info : prodDetlList) {
										if (info.strItemNo.equals(uInvenDList.get(i).strItemNo) == true) {
											uInvenDList.get(i).strShortDesc = info.strShortDesc;
											uInvenDList.get(i).strDescription = info.strDescription;
											uInvenDList.get(i).strUOM = info.prodLocnInfo.strCaseUOM.length() > 0 ? info.prodLocnInfo.strCaseUOM : info.prodLocnInfo.strUnitUOM;
											break;
										}
									}
								}

								int nInvenQtyOffSetting = Utils.getIntSetting(mContext, Global.PREF_INVENQTYOFF);
								int nLoadContainers = Utils.getIntSetting(mContext, Global.PREF_LOAD_CONTAINERS);
								boolean bInvenQtyOff = nInvenQtyOffSetting == 1;
								for (UInvenDInfo info : uInvenDList) {
									if(mapInvenList.get(info.strItemNo) != null) continue;
									ProdDetlInfo prodDetlInfo = mProdDetlItemMap.get(info.strItemNo);
									info.bShow = false;
									if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
									{
										if (nLoadContainers == 2) {
											if (prodDetlInfo != null && prodDetlInfo.prodLocnInfo.nSCWType == 1) {
												info.bShow = true;
												info.nType = Global.INVENTORY_FRESH;
												mUInvenDList.add(info);
											}
											else if (prodDetlInfo != null && prodDetlInfo.prodLocnInfo.nSCWType == 2){
												/*
												info.bShow = false;
												info.nType = Global.INVENTORY_CONTAINERS;
												mUInvenDList.add(info);*/
											}
										}
										else {
											if (info.nSCWType == Global.INVENTORY_TRUCK || info.nSCWType == Global.WEIGHT_ITEM) {
												info.bShow = true;
												info.nType = Global.INVENTORY_FRESH;
												mUInvenDList.add(info);
											} else if (info.nSCWType == Global.INVENTORY_CONTAINERS) {
												info.bShow = true;
												info.nType = Global.INVENTORY_CONTAINERS;
												mUInvenDList.add(info);
											}
										}
									}
									else
									{
										if (nLoadContainers == 2) {
											if (prodDetlInfo != null && prodDetlInfo.prodLocnInfo.nSCWType == 1) {
												info.bShow = true;
												info.nType = Global.INVENTORY_FRESH;
												mUInvenDList.add(info);
											}
											else if (prodDetlInfo != null && prodDetlInfo.prodLocnInfo.nSCWType == 2){
												/*
												info.bShow = false;
												info.nType = Global.INVENTORY_CONTAINERS;
												mUInvenDList.add(info);*/
											}
										}
										else {
											if (info.nSCWType == Global.INVENTORY_TRUCK || info.nSCWType == Global.WEIGHT_ITEM) {
												info.bShow = true;
												info.nType = Global.INVENTORY_FRESH;
												mUInvenDList.add(info);
											} else if (info.nSCWType == Global.INVENTORY_CONTAINERS) {
												info.bShow = true;
												info.nType = Global.INVENTORY_CONTAINERS;
												mUInvenDList.add(info);
											}
										}
									}
									info.nLoadTruck = info.nStock;
									info.dLoadTruck = info.dStock;
									info.nStock = 0;
									info.dStock = 0;
									mapInvenList.put(info.strItemNo, info);
								}
							}
							SortData(mUInvenDList);
//				Collections.sort(mUInvenDList, new Comparator<UInvenDInfo>() {
//					@Override
//					public int compare(UInvenDInfo lhs, UInvenDInfo rhs) {
//						return lhs.strItemNo.compareTo(rhs.strItemNo);
//					}
//				});
						}

					}
					else
					{
						mUInvenDList.clear();
						mUInvenDList.addAll(Global.gLoadReqeustInfo);
					}

					//Recalculate the nStock Value Based on Case Factory... Stock has to be ordered in multiples of case factory ---VS299 (1)
					for(UInvenDInfo uInvenDInfo : mUInvenDList)
					{
						if(Utils.getProdLoconSCWType(mContext, uInvenDInfo.strItemNo) == Global.WEIGHT_ITEM)
							continue;

						int nCaseFactory = 1;
						ProdDetlInfo prodDetlInfo = mProdUPCDetMap.get(uInvenDInfo.strItemNo);
						if(prodDetlInfo != null && prodDetlInfo.prodLocnInfo != null)
						{
							try
							{
								nCaseFactory = mProdUPCDetMap.get(uInvenDInfo.strItemNo).prodLocnInfo.nCaseFactor;
							}catch (Exception e)
							{
								e.printStackTrace();
							}
						}

						if(nCaseFactory == 0) nCaseFactory = 1;

						if(uInvenDInfo.nStock % nCaseFactory != 0)
						{
							uInvenDInfo.nStock += nCaseFactory - (uInvenDInfo.nStock % nCaseFactory);
						}
					}
					dbAdapter.close();

					m_bLoaded = true;

					new Handler(Looper.getMainLooper()).post(new Runnable() {
						@Override
						public void run() {
							hideProgressDialog();
							refreshPage();
						}
					});
				}
			}.start();
		}
		else {
			refreshPage();
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

	@Override
	public void refreshPage() {
		List<UInvenDInfo> list = new ArrayList<>();
		for (UInvenDInfo info : mUInvenDList) {
			if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
			{
				if(filter && info.dStock == 0) continue;
			}
			else
			{
				if(filter && info.nStock == 0) continue;
			}
			list.add(info);
		}

		if(nSalesSortFlag != -1)
		{
			Collections.sort(list, (t1, t2) -> {
				PresoldOrDInfo p1 = presoldOrDInfoMap.get(t1.strItemNo);
				PresoldOrDInfo p2 = presoldOrDInfoMap.get(t2.strItemNo);
				double value1, value2;
				if(p1 != null)
				{
					if(Utils.getProdLoconSCWType(mContext, t1.strItemNo) == Global.WEIGHT_ITEM)
					{
						value1 = p1.dOrderQty;
					}
					else {
						value1 = p1.nOrderQty;
					}
				}
				else
				{
					value1 = 0;
				}

				if(p2 != null)
				{
					if(Utils.getProdLoconSCWType(mContext, t2.strItemNo) == Global.WEIGHT_ITEM)
					{
						value2 = p2.dOrderQty;
					}
					else {
						value2 = p2.nOrderQty;
					}
				}
				else
				{
					value2 = 0;
				}

				if(nSalesSortFlag == 0)
				{
					if(value1 - value2 > 0)
						return 1;
					else if(value1 - value2 < 0)
						return -1;
					else
						return 0;

				}
				else
				{
					if(value2 - value1 > 0)
						return 1;
					else if(value2 - value1 < 0)
						return -1;
					else
						return 0;
				}
			});
		}
		else if(nOnTruckSortFlag != -1)
		{
			Collections.sort(list, (uInvenDInfo, t1) -> {
				double value1;
				double value2;
				if(Utils.getProdLoconSCWType(mContext, uInvenDInfo.strItemNo) == Global.WEIGHT_ITEM)
				{
					value1 = uInvenDInfo.dLoadTruck;
				}
				else
				{
					value1 = uInvenDInfo.nLoadTruck;
				}

				if(Utils.getProdLoconSCWType(mContext, t1.strItemNo) == Global.WEIGHT_ITEM)
				{
					value2 = t1.dLoadTruck;
				}
				else
				{
					value2 = t1.nLoadTruck;
				}

				if(nOnTruckSortFlag == 0)
				{
					if(value1 - value2 > 0)
						return 1;
					else if(value1 - value2 < 0)
						return -1;
					else
						return 0;

				}
				else
				{
					if(value2 - value1 > 0)
						return 1;
					else if(value2 - value1 < 0)
						return -1;
					else
						return 0;
				}
			});
		}
		else if(nSuggestSortFlag != -1)
		{
			Collections.sort(list, (uInvenDInfo, t1) -> {
				double value1;
				double value2;
				if(Utils.getProdLoconSCWType(mContext, uInvenDInfo.strItemNo) == Global.WEIGHT_ITEM)
				{
					value1 = uInvenDInfo.dStock;
				}
				else
				{
					value1 = uInvenDInfo.nStock;
				}

				if(Utils.getProdLoconSCWType(mContext, t1.strItemNo) == Global.WEIGHT_ITEM)
				{
					value2 = t1.dStock;
				}
				else
				{
					value2 = t1.nStock;
				}

				if(nSuggestSortFlag == 0)
				{
					if(value1 - value2 > 0)
						return 1;
					else if(value1 - value2 < 0)
						return -1;
					else
						return 0;

				}
				else
				{
					if(value2 - value1 > 0)
						return 1;
					else if(value2 - value1 < 0)
						return -1;
					else
						return 0;
				}
			});
		}
		else
		{
			SortData(list);
		}

		if(nSalesSortFlag != -1 || nOnTruckSortFlag != -1 || nSuggestSortFlag != -1) {
			mRootView.findViewById(R.id.btnSort).setVisibility(View.INVISIBLE);
		}
		else {
			mRootView.findViewById(R.id.btnSort).setVisibility(View.VISIBLE);
		}

		if(!TextUtils.isEmpty(strCartonDescription))
		{
			int nCartons = 0;
			for(UInvenDInfo info : mUInvenDList)
			{
				if(Utils.getProdLoconSCWType(mContext, info.strItemNo) != Global.WEIGHT_ITEM)
				{
					//nCartons += info.nStock / getCaseValue(info.strItemNo); --- Modified from Mr.Malcolm's Request at VS299 (1)
					nCartons += info.nStock;
				}
			}

			mRootView.findViewById(R.id.lblCartons).setVisibility(View.VISIBLE);
			mRootView.findViewById(R.id.txtCartons).setVisibility(View.VISIBLE);
			((TextView)mRootView.findViewById(R.id.txtCartons)).setText(String.valueOf(nCartons));
		}
		else
		{
			mRootView.findViewById(R.id.lblCartons).setVisibility(View.GONE);
			mRootView.findViewById(R.id.txtCartons).setVisibility(View.INVISIBLE);
		}

		if (list.size() > 0) {
			mListView.setVisibility(View.VISIBLE);
			mTxtNoData.setVisibility(View.GONE);
			mListAdapter.setListData(list);
			mListAdapter.notifyDataSetChanged();
		} else {
			mListView.setVisibility(View.GONE);
			mTxtNoData.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void refreshLanguage() {
		((TextView) mRootView.findViewById(R.id.lblTitle)).setText(Str.LoadRequest[Global.LANG].toUpperCase());
		((TextView) mRootView.findViewById(R.id.txtTrip)).setText(Str.Trip[Global.LANG] + " # " + Global.gTripNumber);
		((TextView) mRootView.findViewById(R.id.txtDescription)).setText("");
		((Button) mRootView.findViewById(R.id.btnDone)).setText(Str.Done[Global.LANG]);
		((Button) mRootView.findViewById(R.id.btnAdd)).setText(Str.Add[Global.LANG]);

		if(nSalesSortFlag == 0)
		{
			((TextView) mRootView.findViewById(R.id.lblSales)).setText(Str.ParLevelUp[Global.LANG]);
		}
		else if(nSalesSortFlag == 1)
		{
			((TextView) mRootView.findViewById(R.id.lblSales)).setText(Str.ParLevelDown[Global.LANG]);
		}
		else
		{
			((TextView) mRootView.findViewById(R.id.lblSales)).setText(Str.ParLevel[Global.LANG]);
		}

		if(nOnTruckSortFlag == 0)
		{
			((TextView) mRootView.findViewById(R.id.lblOnTruck)).setText(Str.OnTruckUp[Global.LANG]);
		}
		else if(nOnTruckSortFlag == 1)
		{
			((TextView) mRootView.findViewById(R.id.lblOnTruck)).setText(Str.OnTruckDown[Global.LANG]);
		}
		else
		{
			((TextView) mRootView.findViewById(R.id.lblOnTruck)).setText(Str.OnTruck[Global.LANG]);
		}

		if(nSuggestSortFlag == 0)
		{
			((TextView) mRootView.findViewById(R.id.lblSuggest)).setText(Str.SuggestUp[Global.LANG]);
		}
		else if(nSuggestSortFlag == 1)
		{
			((TextView) mRootView.findViewById(R.id.lblSuggest)).setText(Str.SuggestDown[Global.LANG]);
		}
		else
		{
			((TextView) mRootView.findViewById(R.id.lblSuggest)).setText(Str.Suggest[Global.LANG]);
		}

		if( !TextUtils.isEmpty(strCartonDescription))
		{
			//((TextView)mRootView.findViewById(R.id.lblCartons)).setText(String.format(Locale.ENGLISH, Str.TotalCartonCount[Global.LANG], strCartonDescription));
			((TextView)mRootView.findViewById(R.id.lblCartons)).setText(Str.Count[Global.LANG]);
		}

		mTxtNoData.setText(Str.No_data_has_been_found[Global.LANG]);
	}

	private void doProcessSelectProduct(final String strItemNo, boolean bUnit, boolean bUpdateItemNo, boolean isSearchByItemNo)
	{
		bProcessingBarCodeScan = true;

		if (strItemNo.length() == 0) {
			Utils.printDebug(mContext, Str.NoProductFound[Global.LANG]);
		} else {
			if(!Utils.getStringSetting(mContext, Global.PREF_AUTO_SCAN_ADD).equals("1") || bUpdateItemNo)
			{
				if(bShowCase)
				{
					mRootView.findViewById(R.id.editCase).setVisibility(View.VISIBLE);
					mRootView.findViewById(R.id.txtCaseSplit).setVisibility(View.VISIBLE);
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
					mRootView.findViewById(R.id.editCase).setVisibility(View.GONE);
					mRootView.findViewById(R.id.txtCaseSplit).setVisibility(View.GONE);
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
					if(mProdDetlIdx != -1 && Utils.getProdLoconSCWType(mContext, mProdDetlList.get(mProdDetlIdx).strItemNo) == Global.WEIGHT_ITEM)
					{
						boolean bExist = false;
						for(UInvenDInfo dInfo: mUInvenDList)
						{
							if(dInfo.strItemNo.equals(mProdDetlList.get(mProdDetlIdx).strItemNo))
							{
								mRootView.findViewById(R.id.editQty).setTag("From App");
								if(mEntryInputMethod.equals("A"))
								{
									((EditText) mRootView.findViewById(R.id.editQty)).setText(String.format(Locale.ENGLISH, "%.2f", dInfo.dStock));
								}
								else
								{
									((EditText) mRootView.findViewById(R.id.editQty)).setText(String.format(Locale.ENGLISH, "%.2f", dInfo.dStock));
								}
								mRootView.findViewById(R.id.editQty).setTag(null);
								bExist = true;
							}
						}

						if(!bExist)
						{
							mRootView.findViewById(R.id.editQty).setTag("From App");
							((EditText) mRootView.findViewById(R.id.editQty)).setText(String.format(Locale.ENGLISH, "%.2f", 0f));
							mRootView.findViewById(R.id.editQty).setTag(null);
						}

						mRootView.findViewById(R.id.editCase).setVisibility(View.GONE);
						mRootView.findViewById(R.id.txtCaseSplit).setVisibility(View.GONE);
					}
					else if(mProdDetlIdx != -1)
					{
						boolean bExist = false;
						for(UInvenDInfo dInfo: mUInvenDList)
						{
							if(dInfo.strItemNo.equals(mProdDetlList.get(mProdDetlIdx).strItemNo))
							{
								((EditText) mRootView.findViewById(R.id.editQty)).setText(String.format(Locale.ENGLISH, "%d", dInfo.nStock));
								bExist = true;
							}
						}

						if(!bExist)
						{
							((EditText) mRootView.findViewById(R.id.editQty)).setText("0");
						}
					}

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
			bProcessingBarCodeScan = false;
		}
	}

	private void doProcessSelectProductByBarcodeScan(final String strItemNo)
	{
		bProcessingBarCodeScan = true;

		String strLoadRequestUOM = Utils.getStringSetting(mContext, Global.PREF_LOAD_REQUEST_UOM);
		String strAutoScanAdd = Utils.getStringSetting(mContext, Global.PREF_AUTO_SCAN_ADD);

		// store original strItemNo
		String strOldItemNo = ((EditText) mRootView.findViewById(R.id.editItemNo)).getText().toString().trim();
		String strOldQty = ((EditText) mRootView.findViewById(R.id.editQty)).getText().toString().trim();

		((EditText) mRootView.findViewById(R.id.editItemNo)).setText(strItemNo);

		if (strItemNo.length() == 0) {
			Utils.printDebug(mContext, Str.NoProductFound[Global.LANG]);
		} else {
			if(bShowCase)
			{
				mRootView.findViewById(R.id.editCase).setVisibility(View.VISIBLE);
				mRootView.findViewById(R.id.txtCaseSplit).setVisibility(View.VISIBLE);
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
				mRootView.findViewById(R.id.editCase).setVisibility(View.GONE);
				mRootView.findViewById(R.id.txtCaseSplit).setVisibility(View.GONE);
				//if(bUpdateItemNo)
				{
					((EditText) mRootView.findViewById(R.id.editItemNo)).setText(strItemNo);
				}
				((EditText) mRootView.findViewById(R.id.editQty)).setText("0");
				this.showKeyboard((EditText) mRootView.findViewById(R.id.editQty), true);
			}
		}

		doSearch(false);

		new Handler(Looper.getMainLooper()).post(new Runnable() {
			@Override
			public void run() {
				if (mProdDetlIdx != -1) {
					ProdDetlInfo prodDetlInfo = mProdDetlList.get(mProdDetlIdx);
					if (Utils.getProdLoconSCWType(mContext, prodDetlInfo.strItemNo) == Global.WEIGHT_ITEM) {
						boolean bExist = false;
						for (UInvenDInfo dInfo : mUInvenDList) {
							if (dInfo.strItemNo.equals(mProdDetlList.get(mProdDetlIdx).strItemNo)) {
								mRootView.findViewById(R.id.editQty).setTag("From App");
								if (mEntryInputMethod.equals("A")) {
									((EditText) mRootView.findViewById(R.id.editQty)).setText(String.format(Locale.ENGLISH, "%.2f", dInfo.dStock));
								} else {
									((EditText) mRootView.findViewById(R.id.editQty)).setText(String.format(Locale.ENGLISH, "%.2f", dInfo.dStock));
								}
								mRootView.findViewById(R.id.editQty).setTag(null);
								bExist = true;
							}
						}

						if (!bExist) {
							mRootView.findViewById(R.id.editQty).setTag("From App");
							((EditText) mRootView.findViewById(R.id.editQty)).setText(String.format(Locale.ENGLISH, "%.2f", 0f));
							mRootView.findViewById(R.id.editQty).setTag(null);
						}

						mRootView.findViewById(R.id.editCase).setVisibility(View.GONE);
						mRootView.findViewById(R.id.txtCaseSplit).setVisibility(View.GONE);
					} else {
						boolean bExist = false;
						for (UInvenDInfo dInfo : mUInvenDList) {
							if (dInfo.strItemNo.equals(mProdDetlList.get(mProdDetlIdx).strItemNo)) {
								((EditText) mRootView.findViewById(R.id.editQty)).setText(String.format(Locale.ENGLISH, "%d", dInfo.nStock));
								bExist = true;
							}
						}

						if (!bExist) {
							((EditText) mRootView.findViewById(R.id.editQty)).setText("0");
						}
					}

					// decide adding qty
					int nAddQty = 1;
					if (strLoadRequestUOM.equals("C")) {
						nAddQty = prodDetlInfo.prodLocnInfo.nCaseFactor;
					} else {
						if (strAutoScanAdd.equals("1")) {
							nAddQty = 1;
						} else {
							nAddQty = 0;
						}
					}

					String strAmount = ((EditText) mRootView.findViewById(R.id.editQty)).getText().toString().trim();

					if (strAutoScanAdd.equals("1")) {
						if (Utils.getProdLoconSCWType(mContext, strItemNo) == Global.WEIGHT_ITEM) {
							float amount = 0;
							if (!TextUtils.isEmpty(strAmount)) {
								amount = Utils.getFloatFromString(strAmount);
							}
							amount += nAddQty;
							mRootView.findViewById(R.id.editQty).setTag("From App");
							((EditText) mRootView.findViewById(R.id.editQty)).setText(String.format(Locale.ENGLISH, "%.2f", amount));
							mRootView.findViewById(R.id.editQty).setTag(null);
						} else {
							int amount = 0;
							if (!TextUtils.isEmpty(strAmount)) {
								amount = Utils.getIntFromString(strAmount);
							}
							amount += nAddQty;
							((EditText) mRootView.findViewById(R.id.editQty)).setText(String.format(Locale.ENGLISH, "%d", amount));
						}

						doAdd();
					} else {
						if (strItemNo.equals(strOldItemNo)) {
							// increase from old qty
							if (Utils.getProdLoconSCWType(mContext, strItemNo) == Global.WEIGHT_ITEM) {
								float amount = 0;
								if (!TextUtils.isEmpty(strAmount)) {
									amount = Utils.getFloatFromString(strOldQty);
								}
								amount += nAddQty;
								mRootView.findViewById(R.id.editQty).setTag("From App");
								((EditText) mRootView.findViewById(R.id.editQty)).setText(String.format(Locale.ENGLISH, "%.2f", amount));
								mRootView.findViewById(R.id.editQty).setTag(null);
							} else {
								int amount = 0;
								if (!TextUtils.isEmpty(strOldQty)) {
									amount = Utils.getIntFromString(strOldQty);
								}
								amount += nAddQty;
								((EditText) mRootView.findViewById(R.id.editQty)).setText(String.format(Locale.ENGLISH, "%d", amount));
							}
						}
						else {
							// increase from searched item's qty
							if (Utils.getProdLoconSCWType(mContext, strItemNo) == Global.WEIGHT_ITEM) {
								float amount = 0;
								if (!TextUtils.isEmpty(strAmount)) {
									amount = Utils.getFloatFromString(strAmount);
								}
								amount += nAddQty;
								mRootView.findViewById(R.id.editQty).setTag("From App");
								((EditText) mRootView.findViewById(R.id.editQty)).setText(String.format(Locale.ENGLISH, "%.2f", amount));
								mRootView.findViewById(R.id.editQty).setTag(null);
							} else {
								int amount = 0;
								if (!TextUtils.isEmpty(strAmount)) {
									amount = Utils.getIntFromString(strAmount);
								}
								amount += nAddQty;
								((EditText) mRootView.findViewById(R.id.editQty)).setText(String.format(Locale.ENGLISH, "%d", amount));
							}
						}
						if (bShowCase) {
							showKeyboard(((EditText) mRootView.findViewById(R.id.editCase)), true);
						} else {
							showKeyboard(((EditText) mRootView.findViewById(R.id.editQty)), true);
						}
					}

					bProcessingBarCodeScan = false;
				}
				else {
					if (bShowCase) {
						showKeyboard(((EditText) mRootView.findViewById(R.id.editCase)), true);
					} else {
						showKeyboard(((EditText) mRootView.findViewById(R.id.editQty)), true);
					}
					bProcessingBarCodeScan = false;
				}
			}
		});
	}

	@Override
	public void receiveBroadcast(String strAction, Intent intent) {
		boolean bUnit = false;
		if (strAction.equals(Global.ACTION_SELECT_PRODUCT) == true) {
			int nType = intent.getIntExtra(Global.EXTRA_TYPE, 0);
			String strItemNo = "";
			if (nType == Global.SELECT_PRODUCT_ITEMNO) {
				strItemNo = intent.getStringExtra(Global.EXTRA_ITEM_NO);
				doProcessSelectProduct(strItemNo, bUnit, true, true);
			} else if (nType == Global.SELECT_PRODUCT_ITEMUPC) {
				String strFrom = intent.getStringExtra(Global.EXTRA_BARCODESCAN_FROM);
				if (!strFrom.equals(getClass().getSimpleName())) {
					return;
				}
				String strItemUPC = intent.getStringExtra(Global.EXTRA_ITEM_UPC);
				doProcessSelectProductByBarcodeScan(strItemUPC);
			}

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
						strDescription = prodDetlInfo.strDescription;
						break;
					}
				}
			}

			if(mProdDetlIdx != -1 && Utils.getProdLoconSCWType(mContext, mProdDetlList.get(mProdDetlIdx).strItemNo) == Global.WEIGHT_ITEM)
			{
				boolean bExist = false;
				for(UInvenDInfo dInfo: mUInvenDList)
				{
					if(dInfo.strItemNo.equals(mProdDetlList.get(mProdDetlIdx).strItemNo))
					{
						mRootView.findViewById(R.id.editQty).setTag("From App");
						if(mEntryInputMethod.equals("A"))
						{
							((EditText) mRootView.findViewById(R.id.editQty)).setText(String.format(Locale.ENGLISH, "%.2f", dInfo.dStock));
						}
						else
						{
							((EditText) mRootView.findViewById(R.id.editQty)).setText(String.format(Locale.ENGLISH, "%.2f", dInfo.dStock));
						}
						mRootView.findViewById(R.id.editQty).setTag(null);
						bExist = true;
					}
				}
				((TextView) mRootView.findViewById(R.id.txtDescription)).setText(strDescription);
				if(!bExist)
				{
					mRootView.findViewById(R.id.editQty).setTag("From App");
					((EditText) mRootView.findViewById(R.id.editQty)).setText(String.format(Locale.ENGLISH, "%.2f", 0f));
					mRootView.findViewById(R.id.editQty).setTag(null);
				}

				mRootView.findViewById(R.id.editCase).setVisibility(View.GONE);
				mRootView.findViewById(R.id.txtCaseSplit).setVisibility(View.GONE);
			}
			else
			{
				if(bShowCase)
				{
					mRootView.findViewById(R.id.editCase).setVisibility(View.VISIBLE);
					mRootView.findViewById(R.id.txtCaseSplit).setVisibility(View.VISIBLE);
					((TextView) mRootView.findViewById(R.id.txtDescription)).setText(strDescription);
					if(mEntryInputMethod.equals("A"))
					{
						((EditText) mRootView.findViewById(R.id.editCase)).setText("");
						((EditText) mRootView.findViewById(R.id.editQty)).setText("");
					}
					else
					{
						((EditText) mRootView.findViewById(R.id.editCase)).setText("0");
						((EditText) mRootView.findViewById(R.id.editQty)).setText("0");
					}

				}
				else
				{
					mRootView.findViewById(R.id.editCase).setVisibility(View.GONE);
					mRootView.findViewById(R.id.txtCaseSplit).setVisibility(View.GONE);
					((TextView) mRootView.findViewById(R.id.txtDescription)).setText(strDescription);
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
		else
		{
			if(bShowCase)
			{
				mRootView.findViewById(R.id.editCase).setVisibility(View.VISIBLE);
				mRootView.findViewById(R.id.txtCaseSplit).setVisibility(View.VISIBLE);
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
				mRootView.findViewById(R.id.editCase).setVisibility(View.GONE);
				mRootView.findViewById(R.id.txtCaseSplit).setVisibility(View.GONE);
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
		nSuggestSortFlag = -1;
		((TextView) mRootView.findViewById(R.id.lblSuggest)).setText(Str.Suggest[Global.LANG]);

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
			bProcessingBarCodeScan = false;
			return;
		}

		int nIdx = -1;
		final ProdDetlInfo productInfo = mProdDetlList.get(mProdDetlIdx);
		for (int i = 0; i < mUInvenDList.size(); i++) {
			if (mUInvenDList.get(i).strItemNo.equals(productInfo.strItemNo) == true) {
				nIdx = i;
				break;
			}
		}

		bModified = true;
		if(Utils.getProdLoconSCWType(mContext, productInfo.strItemNo) != Global.WEIGHT_ITEM )
		{
			if(bShowCase)
			{
				if (strQty.length() == 0) {
					if( strCase.length() == 0 )
					{
						Utils.printDebug(mContext, Str.Please_input_the_Quantity[Global.LANG]);
						showKeyboard((EditText) mRootView.findViewById(R.id.editQty), true);
						bProcessingBarCodeScan = false;
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

		// Disable container item add when LOADCONT = 2
		int nLoadContainers = Utils.getIntSetting(mContext, Global.PREF_LOAD_CONTAINERS);
		if (nLoadContainers == 2) {
			if (productInfo.prodLocnInfo.nSCWType == 2) {
				Utils.printDebug(mContext, Str.CantAddContainer[Global.LANG]);
				((EditText) mRootView.findViewById(R.id.editQty)).setText("");
				((EditText) mRootView.findViewById(R.id.editCase)).setText("");
				focusAndHideKeyboard((EditText) mRootView.findViewById(R.id.editItemNo));
				bProcessingBarCodeScan = false;
				return;
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
						bProcessingBarCodeScan = false;
						return;
					}
				}
				UInvenDInfo info = new UInvenDInfo();
				info.strItemNo = productInfo.strItemNo;
				info.dStock = dValue;
				info.dLoaded = -1;
				info.strShortDesc = productInfo.strShortDesc;
				info.strUOM = productInfo.prodLocnInfo.strCaseUOM.length() > 0 ? productInfo.prodLocnInfo.strCaseUOM : productInfo.prodLocnInfo.strUnitUOM;
				info.nWeight = productInfo.nWeight;
				info.bShow = true;
				info.nOrder = getMaxOrder() + 1;
				mUInvenDList.add(info);
			}
			else
			{
				if(mEntryInputMethod.equals("A"))
				{
					if(dValue + mUInvenDList.get(nIdx).dStock < 0)
					{
						((EditText) mRootView.findViewById(R.id.editQty)).setText("");
						Utils.printDebug(mContext, Str.Quantity_Removed_From_the_transaction[Global.LANG]);
						showKeyboard((EditText) mRootView.findViewById(R.id.editQty), true);
						bProcessingBarCodeScan = false;
						return;
					}
					mUInvenDList.get(nIdx).dStock += dValue;
				}
				else
				{
					mUInvenDList.get(nIdx).dStock = dValue;
				}
			}
		}
		else
		{
			int nQuantity = 0;
			if(bShowCase)
			{
				int nCase = getCaseValue(productInfo.strItemNo);
				nQuantity = Utils.parseIntFromString(strQty) + Utils.parseIntFromString(strCase) * nCase;
			}
			else
			{
				nQuantity = Utils.parseIntFromString(strQty);
			}

			//Check inputted quantity === VS261
			if(Utils.getStringSetting(mContext, Global.PREF_LOAD_REQUEST_UOM).equals("C") && nQuantity != 0)
			{
				if(productInfo.prodLocnInfo != null)
				{
					if(productInfo.prodLocnInfo.nCaseFactor > 1 && nQuantity % productInfo.prodLocnInfo.nCaseFactor != 0)
					{
						try {
							final MessageDialog msgDialog = new MessageDialog(mContext);
							msgDialog.setCancelable(false);
							msgDialog.mStrMessage = String.format(Locale.ENGLISH, Str.LoadRequestUOM[Global.LANG], productInfo.strDescription, productInfo.prodLocnInfo.nCaseFactor, nQuantity, nQuantity + (productInfo.prodLocnInfo.nCaseFactor - nQuantity % productInfo.prodLocnInfo.nCaseFactor));
							msgDialog.mStrLeft = Str.Return[Global.LANG];
							msgDialog.mStrRight = Str.Ok[Global.LANG];
							final int finalNIdx = nIdx;
							final int finalNQuantity = nQuantity;
							msgDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
								@Override
								public void onDismiss(DialogInterface dialog) {
									if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
										int nQuantity = finalNQuantity + (productInfo.prodLocnInfo.nCaseFactor - finalNQuantity % productInfo.prodLocnInfo.nCaseFactor);
										if(finalNIdx == -1)
										{
											UInvenDInfo info = new UInvenDInfo();
											info.strItemNo = productInfo.strItemNo;
											info.nStock = nQuantity;
											info.nLoaded = -1;
											info.strShortDesc = productInfo.strShortDesc;
											info.strUOM = productInfo.prodLocnInfo.strCaseUOM.length() > 0 ? productInfo.prodLocnInfo.strCaseUOM : productInfo.prodLocnInfo.strUnitUOM;
											info.nWeight = productInfo.nWeight;
											info.bShow = true;
											info.nOrder = getMaxOrder() + 1;
											mUInvenDList.add(info);
										}
										else
										{
											if(mEntryInputMethod.equals("A"))
											{
												if( mUInvenDList.get(finalNIdx).nStock + nQuantity < 0)
												{
													((EditText) mRootView.findViewById(R.id.editQty)).setText("");
													((EditText) mRootView.findViewById(R.id.editCase)).setText("");
													Utils.printDebug(mContext, Str.Quantity_Removed_From_the_transaction[Global.LANG]);
													showKeyboard((EditText) mRootView.findViewById(R.id.editQty), true);
													return;
												}
												mUInvenDList.get(finalNIdx).nStock += nQuantity;
											}
											else
											{
												mUInvenDList.get(finalNIdx).nStock = nQuantity;
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

										focusAndHideKeyboard((EditText) mRootView.findViewById(R.id.editItemNo));
										refreshPage();
									}
									else if(msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT){
										((EditText) mRootView.findViewById(R.id.editQty)).setText("");
										((EditText) mRootView.findViewById(R.id.editCase)).setText("");
										showKeyboard((EditText) mRootView.findViewById(R.id.editQty), true);
									}
								}
							});
							msgDialog.show();
							bProcessingBarCodeScan = false;
							return;
						}catch (Exception e)
						{
							e.printStackTrace();
						}
					}
				}
			}
			if (nIdx == -1) {
				if (nQuantity == 0) {
					Utils.printDebug(mContext, Str.Please_input_the_Quantity[Global.LANG]);
					showKeyboard((EditText) mRootView.findViewById(R.id.editQty), true);
					bProcessingBarCodeScan = false;
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
							bProcessingBarCodeScan = false;
							return;
						}
					}
					UInvenDInfo info = new UInvenDInfo();
					info.strItemNo = productInfo.strItemNo;
					info.nStock = nQuantity;
					info.nLoaded = -1;
					info.strShortDesc = productInfo.strShortDesc;
					info.strUOM = productInfo.prodLocnInfo.strCaseUOM.length() > 0 ? productInfo.prodLocnInfo.strCaseUOM : productInfo.prodLocnInfo.strUnitUOM;
					info.nWeight = productInfo.nWeight;
					info.bShow = true;
					info.nOrder = getMaxOrder() + 1;
					mUInvenDList.add(info);
				}
			} else {
				if(mEntryInputMethod.equals("A"))
				{
					if( mUInvenDList.get(nIdx).nStock + nQuantity < 0)
					{
						((EditText) mRootView.findViewById(R.id.editQty)).setText("");
						((EditText) mRootView.findViewById(R.id.editCase)).setText("");
						Utils.printDebug(mContext, Str.Quantity_Removed_From_the_transaction[Global.LANG]);
						showKeyboard((EditText) mRootView.findViewById(R.id.editQty), true);
						bProcessingBarCodeScan = false;
						return;
					}
					mUInvenDList.get(nIdx).nStock += nQuantity;
				}
				else
				{
					mUInvenDList.get(nIdx).nStock = nQuantity;
				}

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

		focusAndHideKeyboard((EditText) mRootView.findViewById(R.id.editItemNo));
		refreshPage();
		bProcessingBarCodeScan = false;
	}

	public void doCofirmBack()
	{
		if(bModified)
		{
			try {
				final MessageDialog msgDialog = new MessageDialog(mContext);
				msgDialog.setCancelable(false);
				msgDialog.mStrMessage = Str.AreYouSureExitLoadRequest[Global.LANG];
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

	private void doDone() {

		Bundle bundle = getArguments();
		boolean bFromVehicleInventory = false;
		if( bundle != null )
		{
			try
			{
				bFromVehicleInventory = bundle.getBoolean(Global.EXTRA_FROM, false);
			}catch(Exception e)
			{
				e.printStackTrace();
			}
		}

		Global.gLoadReqeustInfo.clear();
		Global.gLoadReqeustInfo.addAll(mUInvenDList);
		bundle = new Bundle();
		Global.gFragmentForCheckingModification = this;
		Fragment fragment = new LoadRequestSummaryFragment();
		bundle.putBoolean(Global.EXTRA_FROM, bFromVehicleInventory);
		fragment.setArguments(bundle);
		mContext.replaceFragment(fragment);
	}

	private void showInvenDetail(UInvenDInfo info) {
		((EditText) mRootView.findViewById(R.id.editItemNo)).setText(Utils.getDisplayItemCode(mContext, info.strItemNo));
		if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
		{
			String strQty = String.format(Locale.ENGLISH, "%.2f", info.dStock);
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

			mRootView.findViewById(R.id.editCase).setVisibility(View.GONE);
			mRootView.findViewById(R.id.txtCaseSplit).setVisibility(View.GONE);
		}
		else
		{
			if(bShowCase)
			{
				int nCase = getCaseValue(info.strItemNo);
				int nValue = info.nStock;
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
				int nValue = info.nStock;
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

	private void doSort()
	{
		if(nSalesSortFlag != -1 || nOnTruckSortFlag != -1 || nSuggestSortFlag != -1) return;

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
				Global.gLoadReqeustInfo.clear();
				mContext.onBackPressed();
				break;
			case R.id.btnAdd:
				doAdd();
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
			case R.id.lblSales:
				//doSortBySales();
				break;
			case R.id.lblOnTruck:
				//doSortByOnTruck();
				break;
			case R.id.lblSuggest:
				//doSortBySuggest();
				break;
		}
		m_bClicked = false;
	}

	private void doSortBySales()
	{
		nOnTruckSortFlag = -1;
		nSuggestSortFlag = -1;

		if(nSalesSortFlag != 0)
		{
			nSalesSortFlag = 0;
		}
		else
		{
			nSalesSortFlag = 1;
		}
		refreshLanguage();
		refreshPage();
	}

	private void doSortByOnTruck()
	{
		nSalesSortFlag = -1;
		nSuggestSortFlag = -1;
		if(nOnTruckSortFlag != 0)
		{
			nOnTruckSortFlag = 0;
		}
		else
		{
			nOnTruckSortFlag = 1;
		}
		refreshLanguage();
		refreshPage();
	}

	private void doSortBySuggest()
	{
		nSalesSortFlag = -1;
		nOnTruckSortFlag = -1;
		if(nSuggestSortFlag != 0)
		{
			nSuggestSortFlag = 0;
		}
		else
		{
			nSuggestSortFlag = 1;
		}
		refreshLanguage();
		refreshPage();
	}

	private class ListAdapter extends BaseAdapter {

		private List<UInvenDInfo> list = new ArrayList<>();

		private class ViewHolder {
			public TextView txtShortDesc, txtPickLocationCode, txtSales, txtLoadTruck, txtStock;
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
			else {
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
				view = mContext.getLayoutInflater().inflate(R.layout.row_request, parent, false);
				holder = new ViewHolder();
				holder.txtStock = (TextView) view.findViewById(R.id.txtStock);
				holder.txtShortDesc = (TextView) view.findViewById(R.id.txtShortDesc);
				holder.txtPickLocationCode = (TextView) view.findViewById(R.id.txtPickLocationCode);
				holder.txtSales = (TextView) view.findViewById(R.id.txtSales);
				holder.txtLoadTruck = (TextView) view.findViewById(R.id.txtOnTruck);
				view.setTag(holder);
			} else {
				holder = (ViewHolder) view.getTag();
			}


			holder.txtShortDesc.setText(info.strShortDesc);
			holder.txtPickLocationCode.setText(info.strPickLocationCode);

			int nCase = getCaseValue(info.strItemNo);
			if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
			{
				PresoldOrDInfo presoldOrDInfo = presoldOrDInfoMap.get(info.strItemNo);

				if(presoldOrDInfo != null)
				{
					holder.txtSales.setText(String.format(Locale.ENGLISH, "%.2f", presoldOrDInfo.dOrderQty));
				}
				else
				{
					holder.txtSales.setText(String.format(Locale.ENGLISH, "%.2f", 0f));
				}
				holder.txtLoadTruck.setText(String.format(Locale.ENGLISH, "%.2f", info.dLoadTruck));
				holder.txtStock.setText(String.format(Locale.ENGLISH, "%.2f", info.dStock));
			}
			else
			{
				if(bShowCase)
				{
					PresoldOrDInfo presoldOrDInfo = presoldOrDInfoMap.get(info.strItemNo);
					if(presoldOrDInfo != null)
					{
						if (Utils.isContainerItem(mContext, info.strItemNo)) {
							holder.txtSales.setText(String.format(Locale.ENGLISH, "%d", presoldOrDInfo.nOrderQty));
						}
						else {
							holder.txtSales.setText(String.format(Locale.ENGLISH, "%d/%d", presoldOrDInfo.nOrderQty / nCase, presoldOrDInfo.nOrderQty % nCase));
						}
					}
					else
					{
						if (Utils.isContainerItem(mContext, info.strItemNo)) {
							holder.txtSales.setText(String.format(Locale.ENGLISH, "%d", 0));
						}
						else {
							holder.txtSales.setText(String.format(Locale.ENGLISH, "%d/%d", 0, 0));
						}
					}

					if (Utils.isContainerItem(mContext, info.strItemNo)) {
						holder.txtLoadTruck.setText(String.format(Locale.ENGLISH, "%d", info.nLoadTruck));
						holder.txtStock.setText(String.format(Locale.ENGLISH, "%d", info.nStock));
					}
					else {
						holder.txtLoadTruck.setText(String.format(Locale.ENGLISH, "%d/%d", info.nLoadTruck / nCase, info.nLoadTruck % nCase));
						holder.txtStock.setText(String.format(Locale.ENGLISH, "%d/%d", info.nStock / nCase, info.nStock % nCase));
					}
				}
				else
				{
					PresoldOrDInfo presoldOrDInfo = presoldOrDInfoMap.get(info.strItemNo);
					if(presoldOrDInfo != null)
					{
						holder.txtSales.setText(String.format(Locale.ENGLISH, "%d", presoldOrDInfo.nOrderQty));
					}
					else
					{
						holder.txtSales.setText(String.format(Locale.ENGLISH, "%d", 0));
					}
					holder.txtLoadTruck.setText(String.format(Locale.ENGLISH, "%d", info.nLoadTruck));
					holder.txtStock.setText(String.format(Locale.ENGLISH, "%d", info.nStock));
				}
			}

			view.setOnClickListener(v -> showInvenDetail(info));

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