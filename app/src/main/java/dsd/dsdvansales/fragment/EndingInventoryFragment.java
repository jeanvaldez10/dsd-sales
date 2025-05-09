package dsd.dsdvansales.fragment;

import android.app.AlertDialog;
import android.content.*;
import android.os.*;
import android.text.*;
import android.view.*;
import android.view.inputmethod.*;
import android.widget.*;

import java.util.*;

import dsd.dsdvansales.R;
import dsd.dsdvansales.activities.BarcodeScannerActivity;
import dsd.dsdvansales.db.DbAdapter;
import dsd.dsdvansales.dialogs.MessageDialog;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.model.DescTypeInfo;
import dsd.dsdvansales.model.ProdDetlInfo;
import dsd.dsdvansales.model.ProdInfo;
import dsd.dsdvansales.model.ProdStruInfo;
import dsd.dsdvansales.model.UInvenDInfo;
import dsd.dsdvansales.model.UInvenHInfo;
import dsd.dsdvansales.views.AlertSingleChoiceAdapter;

public class EndingInventoryFragment extends BaseFragment implements View.OnClickListener {

	private TextView mTxtNoData = null;
	private ListView mListView = null;
	private ListAdapter mListAdapter = null;

	private UInvenHInfo mUInvenHInfo = null;
	private List<UInvenDInfo> mUInvenDList = new ArrayList<>();
	private List<ProdDetlInfo> mProdDetlList = new ArrayList<>();
	private Map<String, ProdDetlInfo> mProdUPCDetMap = new HashMap<>();
	private Map<String, ProdDetlInfo> mProdDetlItemMap = new HashMap<>();
	private Map<String, ProdInfo> mProdInfoMap = new HashMap<>();

	private int mUInvenDIdx = -1;
	private int mType = Global.INVENTORY_FRESH;
	private boolean bInit = true;

	private boolean bShowCase = true;

	private List<DescTypeInfo> mSpoilsReasonDescList = new ArrayList<>();
	private String mEntryInputMethod = "";
	private int nInventoryCheck = 0;

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
		mRootView = inflater.inflate(R.layout.fragment_ending_inventory, container, false);
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
		Global.gFragIdx = Global.FRAG_ENDING_INVENTORY;
	}

	@Override
	public void initView() {
		bShowCase = Utils.getStringSetting(mContext, Global.PREF_INVENTORYUOM).equals("U") == false;
		nInventoryCheck = Utils.getIntSetting(mContext, Global.PREF_INVENCKECK);
		mRootView.findViewById(R.id.btnSort).setOnClickListener(this);
		mRootView.findViewById(R.id.txtReturns).setOnClickListener(this);
		mRootView.findViewById(R.id.txtSpoils).setOnClickListener(this);
		mRootView.findViewById(R.id.txtContainers).setOnClickListener(this);

		if(Utils.getStringSetting(mContext, Global.PREF_DISABLE_OFFLOADS).equals("1"))
		{
			mRootView.findViewById(R.id.txtFresh).setVisibility(View.GONE);
		}
		else
		{
			mRootView.findViewById(R.id.txtFresh).setOnClickListener(this);
		}

		mRootView.findViewById(R.id.btnAdd).setOnClickListener(this);
		mRootView.findViewById(R.id.btnBack).setOnClickListener(this);
		mRootView.findViewById(R.id.btnDone).setOnClickListener(this);

		mTxtNoData = (TextView) mRootView.findViewById(R.id.txtNoData);
		mListView = (ListView) mRootView.findViewById(R.id.listView);
		mListAdapter = new ListAdapter();
		mListView.setAdapter(mListAdapter);

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

		/*if(Utils.getIntSetting(mContext, Global.PREF_LOAD_CONTAINERS) == 0)
		{
			mRootView.findViewById(R.id.txtContainers).setVisibility(View.INVISIBLE);
		}
		else
		{
			mRootView.findViewById(R.id.txtContainers).setVisibility(View.VISIBLE);
		}*/

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
					doProcessSelectProduct(strItemNo, false, false);
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
				if((mRootView.findViewById(R.id.editQty)).getTag() == null)
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
						showKeyboard((EditText)mRootView.findViewById(R.id.editQty), true);
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

		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		List<ProdStruInfo> prodStruList = dbAdapter.getProdStruList();
		dbAdapter.close();
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
		mRootView.findViewById(R.id.btnBarcodeScanner).setOnClickListener(this);
		mRootView.findViewById(R.id.btnProductSearch).setOnClickListener(this);

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
		if( bInit )
		{
			loadData();
		}

		refreshLanguage();
		setType(mType);
		bInit = false;
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

	private void doProcessSelectProduct(final String strItemNo, boolean bUpdateItemNo, boolean isSearchByItemNo)
	{
		if (strItemNo.length() == 0) {
			Utils.printDebug(mContext, Str.NoProductFound[Global.LANG]);
		} else {
			if(bUpdateItemNo || !Utils.getStringSetting(mContext, Global.PREF_AUTO_SCAN_ADD).equals("1"))
			{
				((EditText) mRootView.findViewById(R.id.editItemNo)).setText(strItemNo);
				if(Utils.getProdLoconSCWType(mContext, strItemNo) == Global.WEIGHT_ITEM)
				{
					mRootView.findViewById(R.id.editCase).setVisibility(View.GONE);
					mRootView.findViewById(R.id.txtCaseSplit).setVisibility(View.GONE);
					mRootView.findViewById(R.id.editQty).setTag("From App");
					((EditText) mRootView.findViewById(R.id.editQty)).setText(String.format(Locale.ENGLISH, "@.2f", 0f));
					mRootView.findViewById(R.id.editQty).setTag(null);
					this.showKeyboard((EditText) mRootView.findViewById(R.id.editQty), true);
				}
				else
				{
					if(bShowCase)
					{
						mRootView.findViewById(R.id.editCase).setVisibility(View.VISIBLE);
						mRootView.findViewById(R.id.txtCaseSplit).setVisibility(View.VISIBLE);

						((EditText) mRootView.findViewById(R.id.editCase)).setText("0");
						((EditText) mRootView.findViewById(R.id.editQty)).setText("0");
						this.showKeyboard((EditText) mRootView.findViewById(R.id.editCase), true);
					}
					else
					{
						mRootView.findViewById(R.id.editCase).setVisibility(View.GONE);
						mRootView.findViewById(R.id.txtCaseSplit).setVisibility(View.GONE);

						((EditText) mRootView.findViewById(R.id.editQty)).setText("0");
						this.showKeyboard((EditText) mRootView.findViewById(R.id.editQty), true);
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
			showKeyboard(((EditText) mRootView.findViewById(R.id.editQty)), true);
		}
	}

	@Override
	public void loadData() {
		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		List<ProdDetlInfo> prodDetlList = dbAdapter.getProdDetlList(mProdUPCDetMap);
		dbAdapter.getProdDetlList(mProdDetlItemMap, null);
//		if(Utils.getStringSetting(mContext, Global.PREF_ITEM_CODE_DISPLAY_SOURCE).equals("PRODINFO"))
//		{
//			mProdInfoMap = dbAdapter.getProdInfoMap("ItemCode");
//		}
		mSpoilsReasonDescList = dbAdapter.getDescTypeList("SPOILSRSN");
		mProdDetlList = prodDetlList;
		mUInvenHInfo = dbAdapter.getUInvenHInfo(Utils.getLongSetting(mContext, Global.PREF_UINVEN_TRXN_NO));
		if (mUInvenHInfo == null) {
			mUInvenHInfo = new UInvenHInfo();
			mUInvenHInfo.nTrxnNo = Utils.getLongSetting(mContext, Global.PREF_UINVEN_TRXN_NO);
		}
		List<UInvenDInfo> uInvenDList = dbAdapter.getUInvenDList(mUInvenHInfo.nTrxnNo);
		if( uInvenDList == null ) uInvenDList = new ArrayList<>();
		dbAdapter.close();

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
		mUInvenHInfo.uInvenDList.clear();

		boolean bInvenQtyOff = nInvenQtyOffSetting == 1;
		int nLoadContainers = Utils.getIntSetting(mContext, Global.PREF_LOAD_CONTAINERS);
		for (UInvenDInfo info : uInvenDList) {
			info.bShow = false;
			ProdDetlInfo prodDetlInfo = mProdDetlItemMap.get(info.strItemNo);
			if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
			{
				if (nLoadContainers == 2) {
					if ((prodDetlInfo != null && prodDetlInfo.prodLocnInfo.nSCWType == 1) || info.nSCWType == Global.WEIGHT_ITEM) {
						info.bShow = true;
						if( nInvenQtyOffSetting == 2 )
						{
							info.dUnload = 0;
						}
						else if( !bInvenQtyOff )
						{
							info.dUnload = info.dStock;
						}
						else
						{
							info.dUnload = 0;
						}
						info.nType = Global.INVENTORY_FRESH;
						mUInvenHInfo.uInvenDList.add(info);
					}
					else if (prodDetlInfo != null && prodDetlInfo.prodLocnInfo.nSCWType == 2) {
						info.bShow = true;
						if( nInvenQtyOffSetting == 2 )
						{
							info.dUnload = 0;
						}
						else if( !bInvenQtyOff )
						{
							info.dUnload = info.dStock;
						}
						else
						{
							info.dUnload = 0;
						}
						info.nType = Global.INVENTORY_CONTAINERS;
						mUInvenHInfo.uInvenDList.add(info);
					}
				}
				else {
					if (info.nSCWType == Global.INVENTORY_TRUCK || info.nSCWType == Global.WEIGHT_ITEM) {
						info.bShow = true;
						if( nInvenQtyOffSetting == 2 )
						{
							info.dUnload = 0;
						}
						else if( !bInvenQtyOff )
						{
							info.dUnload = info.dStock;
						}
						else
						{
							info.dUnload = 0;
						}
						info.nType = Global.INVENTORY_FRESH;
						mUInvenHInfo.uInvenDList.add(info);
					} else if (info.nSCWType == Global.INVENTORY_CONTAINERS) {
						info.bShow = true;
						if( nInvenQtyOffSetting == 2 )
						{
							info.dUnload = 0;
						}
						else if( !bInvenQtyOff )
						{
							info.dUnload = info.dStock;
						}
						else
						{
							info.dUnload = 0;
						}
						info.nType = Global.INVENTORY_CONTAINERS;
						mUInvenHInfo.uInvenDList.add(info);
					}
				}
			}
			else
			{
				if (nLoadContainers == 2) {
					if ((prodDetlInfo != null && prodDetlInfo.prodLocnInfo.nSCWType == 1) || info.nSCWType == Global.WEIGHT_ITEM) {
						info.bShow = true;
						if( nInvenQtyOffSetting == 2 )
						{
							info.nUnload = 0;
						}
						else if( !bInvenQtyOff )
						{
							info.nUnload = info.nStock;
						}
						else
						{
							info.nUnload = 0;
						}
						info.nType = Global.INVENTORY_FRESH;
						mUInvenHInfo.uInvenDList.add(info);
					}
					else if (prodDetlInfo != null && prodDetlInfo.prodLocnInfo.nSCWType == 2) {
						info.bShow = true;
						if( nInvenQtyOffSetting == 2 )
						{
							info.nUnload = 0;
						}
						else if( !bInvenQtyOff )
						{
							info.nUnload = info.nStock;
						}
						else
						{
							info.nUnload = 0;
						}
						info.nType = Global.INVENTORY_CONTAINERS;
						mUInvenHInfo.uInvenDList.add(info);
					}
				}
				else {
					if (info.nSCWType == Global.INVENTORY_TRUCK || info.nSCWType == Global.WEIGHT_ITEM) {
						info.bShow = true;
						if( nInvenQtyOffSetting == 2 )
						{
							info.nUnload = 0;
						}
						else if( !bInvenQtyOff )
						{
							info.nUnload = info.nStock;
						}
						else
						{
							info.nUnload = 0;
						}
						info.nType = Global.INVENTORY_FRESH;
						mUInvenHInfo.uInvenDList.add(info);
					} else if (info.nSCWType == Global.INVENTORY_CONTAINERS) {
						info.bShow = true;
						if( nInvenQtyOffSetting == 2 )
						{
							info.nUnload = 0;
						}
						else if( !bInvenQtyOff )
						{
							info.nUnload = info.nStock;
						}
						else
						{
							info.nUnload = 0;
						}
						info.nType = Global.INVENTORY_CONTAINERS;
						mUInvenHInfo.uInvenDList.add(info);
					}
				}
			}
		}
		for (UInvenDInfo info : uInvenDList) {
			if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
			{
				if (info.dDumps > 0) {
					UInvenDInfo newInfo = info;
					if( nInvenQtyOffSetting == 2 )
					{
						info.dDumpsUnload = 0;
					}
					else if( !bInvenQtyOff )
					{
						newInfo.dDumpsUnload = newInfo.dDumps;
					}
					else
					{
						newInfo.dDumpsUnload = 0;
					}
					newInfo.nType = Global.INVENTORY_RETURNS;
					//mUInvenHInfo.uInvenDList.add(newInfo);
				}
			}
			else
			{
				if (info.nDumps != 0) {
					UInvenDInfo newInfo = info;
					if( nInvenQtyOffSetting == 2 )
					{
						info.nDumpsUnload = 0;
					}
					else if( !bInvenQtyOff )
					{
						newInfo.nDumpsUnload = newInfo.nDumps;
					}
					else
					{
						newInfo.nDumpsUnload = 0;
					}
					newInfo.nType = Global.INVENTORY_RETURNS;
					//mUInvenHInfo.uInvenDList.add(newInfo);
				}
			}

		}

		if(Global.gCurEndingInventory != null)
		{
			mUInvenHInfo = Global.gCurEndingInventory;
		}

		refreshPage();

	}

	@Override
	public void refreshPage() {
		mUInvenDList.clear();
		int nContainerCnt = 0;
		int nReturnsCnt = 0;
		for (UInvenDInfo info : mUInvenHInfo.uInvenDList)
		{
			if( info.nType == Global.INVENTORY_CONTAINERS && (info.nUnload != 0 || info.nStock != 0) )
			{
				nContainerCnt++;
			}
			if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
			{
				if( info.nType == Global.INVENTORY_RETURNS && (info.dUnload != 0 || info.dDumps != 0))
				{
					nReturnsCnt++;
				}
			}
			else
			{
				if( info.nType == Global.INVENTORY_RETURNS && (info.nUnload != 0 || info.nDumps != 0))
				{
					nReturnsCnt++;
				}
			}

			if (info.nType == mType) {
				if( Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
				{
					if( info.nType == Global.INVENTORY_FRESH || info.nType == Global.INVENTORY_CONTAINERS)
					{
						if(Utils.getIntSetting(mContext, Global.PREF_INVENQTYOFF) == 2 && info.nType == Global.INVENTORY_FRESH)
						{
							if( info.dUnload > 0 )
							{
								mUInvenDList.add(info);
							}
						}
						else
						{
							if( info.dStock > 0 || info.dUnload > 0 )
							{
								mUInvenDList.add(info);
							}
						}
					}
					else if(info.nType == Global.INVENTORY_RETURNS )
					{
						if( info.dDumps > 0 )
						{
							mUInvenDList.add(info);
						}
					}
				}
				else
				{
					if( info.nType == Global.INVENTORY_FRESH || info.nType == Global.INVENTORY_CONTAINERS)
					{
						if(Utils.getIntSetting(mContext, Global.PREF_INVENQTYOFF) == 2 && info.nType == Global.INVENTORY_FRESH)
						{
							if( info.nUnload > 0 )
							{
								mUInvenDList.add(info);
							}
						}
						else
						{
							if( info.nStock > 0 || info.nUnload > 0 )
							{
								mUInvenDList.add(info);
							}
						}
					}
					else if(info.nType == Global.INVENTORY_RETURNS )
					{
						if( info.nDumps != 0 )
						{
							mUInvenDList.add(info);
						}
					}
				}
			}
			else if( info.nType > mType )
			{
				if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
				{
					if( mType == Global.INVENTORY_FRESH && info.nType == Global.INVENTORY_RETURNS)
					{
						if(Utils.getIntSetting(mContext, Global.PREF_INVENQTYOFF) == 2)
						{
							if( info.dUnload > 0 )
							{
								mUInvenDList.add(info);
							}
						}
						else
						{
							if( info.dStock > 0 )
							{
								mUInvenDList.add(info);
							}
						}
					}
				}
				else
				{
					if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
					{
						if( mType == Global.INVENTORY_FRESH && info.nType == Global.INVENTORY_RETURNS)
						{
							if(Utils.getIntSetting(mContext, Global.PREF_INVENQTYOFF) == 2)
							{
								if( info.dUnload > 0 )
								{
									mUInvenDList.add(info);
								}
							}
							else
							{
								if( info.dStock > 0 )
								{
									mUInvenDList.add(info);
								}
							}
						}
					}
					else
					{
						if( mType == Global.INVENTORY_FRESH && info.nType == Global.INVENTORY_RETURNS)
						{
							if(Utils.getIntSetting(mContext, Global.PREF_INVENQTYOFF) == 2)
							{
								if( info.nUnload > 0 )
								{
									mUInvenDList.add(info);
								}
							}
							else
							{
								if( info.nStock > 0 )
								{
									mUInvenDList.add(info);
								}
							}
						}
					}
				}
			}
			else
			{
				if( mType == Global.INVENTORY_RETURNS )
				{
					if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
					{
						if( info.dDumpsUnload > 0 )
						{
							mUInvenDList.add(info);
						}
					}
					else
					{
						if( info.nDumpsUnload > 0 )
						{
							mUInvenDList.add(info);
						}
					}

				}
				else if(mType == Global.INVENTORY_SPOILS)
				{
					if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
					{
						if( info.dSpoils > 0 )
						{
							mUInvenDList.add(info);
						}
					}
					else
					{
						if( info.nSpoils > 0 )
						{
							mUInvenDList.add(info);
						}
					}
				}
			}
		}
		if (mUInvenDList.size() > 0) {
			mListView.setVisibility(View.VISIBLE);
			mTxtNoData.setVisibility(View.GONE);
			mListAdapter.setListData(mUInvenDList);
			mListAdapter.notifyDataSetChanged();
		} else {
			mListView.setVisibility(View.GONE);
			mTxtNoData.setVisibility(View.VISIBLE);
		}

		if( nContainerCnt > 0 && mType != Global.INVENTORY_CONTAINERS)
		{
			mRootView.findViewById(R.id.btnDone).setEnabled(false);
		}
		else if( (nReturnsCnt > 0 || nContainerCnt > 0) && mType == Global.INVENTORY_FRESH )
		{
			mRootView.findViewById(R.id.btnDone).setEnabled(false);
		}
		else
		{
			mRootView.findViewById(R.id.btnDone).setEnabled(true);
		}
	}

	@Override
	public void refreshLanguage() {

		((TextView) mRootView.findViewById(R.id.lblTitle)).setText(Str.EndingInventory[Global.LANG].toUpperCase());
		((TextView) mRootView.findViewById(R.id.txtTrip)).setText(Str.Trip[Global.LANG] + " # " + Global.gTripNumber);

		if(!Utils.getStringSetting(mContext, Global.PREF_DISABLE_OFFLOADS).equals("1"))
		{
			((TextView) mRootView.findViewById(R.id.txtFresh)).setText(Str.Fresh[Global.LANG]);
		}

		((TextView) mRootView.findViewById(R.id.txtReturns)).setText(Str.Returns[Global.LANG]);
		((TextView) mRootView.findViewById(R.id.txtSpoils)).setText(Str.Spoils[Global.LANG]);
		((TextView) mRootView.findViewById(R.id.txtContainers)).setText(Str.Containers[Global.LANG]);

		boolean bExistReturns = false;
		boolean bExistSpoils = mSpoilsReasonDescList.size() > 0;
		boolean bExistContainer = Utils.getIntSetting(mContext, Global.PREF_LOAD_CONTAINERS) != 0;
		boolean bExistFresh = !Utils.getStringSetting(mContext, Global.PREF_DISABLE_OFFLOADS).equals("1");
		for(UInvenDInfo uInvenDInfo : mUInvenHInfo.uInvenDList)
		{
			if(uInvenDInfo.nDumps > 0 || uInvenDInfo.dDumps > 0)
			{
				bExistReturns = true;
				break;
			}
		}

		LinearLayout layoutButton = (LinearLayout) mRootView.findViewById(R.id.layoutButton);
		View freshView = mRootView.findViewById(R.id.txtFresh);

		View returnsView = mRootView.findViewById(R.id.txtReturns);
		View spoilsView = mRootView.findViewById(R.id.txtSpoils);
		View containersView = mRootView.findViewById(R.id.txtContainers);

		View views[] = new View[4];
		views[0] = freshView;
		views[1] = returnsView;
		views[2] = spoilsView;
		views[3] = containersView;

		boolean bExist[] = new boolean[4];
		bExist[0] = bExistFresh;
		bExist[1] = bExistReturns;
		bExist[2] = bExistSpoils;
		bExist[3] = bExistContainer;

		layoutButton.removeAllViews();
//		layoutButton.addView(freshView);
		View selView = null;
		for(int i = 0; i < 4; i++)
		{
			if(bExist[i])
			{
				views[i].setVisibility(View.VISIBLE);
				layoutButton.addView(views[i]);
				if(selView == null)
				{
					selView = views[i];
				}
			}
		}
		for(int i = 0; i < 4; i++)
		{
			if(!bExist[i])
			{
				views[i].setVisibility(View.INVISIBLE);
				layoutButton.addView(views[i]);
			}
		}

		if(!bExistFresh && selView != null)
		{
			View finalSelView = selView;
			new Handler(Looper.getMainLooper()).post(() -> finalSelView.performClick());
		}
		//((TextView) mRootView.findViewById(R.id.txtQty)).setText(Str.Qty[Global.LANG]);
		((TextView) mRootView.findViewById(R.id.txtDescription)).setText("");
		((Button) mRootView.findViewById(R.id.btnDone)).setText(Str.Done[Global.LANG]);
		mTxtNoData.setText(Str.No_data_has_been_found[Global.LANG]);
	}

	@Override
	public void receiveBroadcast(String strAction, Intent intent) {
		if (strAction.equals(Global.ACTION_SELECT_PRODUCT) == true) {
			int nType = intent.getIntExtra(Global.EXTRA_TYPE, 0);
			String strItemNo = "";
			if (nType == Global.SELECT_PRODUCT_ITEMNO) {
				strItemNo = intent.getStringExtra(Global.EXTRA_ITEM_NO);
				doProcessSelectProduct(strItemNo, true, true);
			} else if (nType == Global.SELECT_PRODUCT_ITEMUPC) {
				String strFrom = intent.getStringExtra(Global.EXTRA_BARCODESCAN_FROM);
				if (!strFrom.equals(getClass().getSimpleName())) {
					return;
				}
				String strItemUPC = intent.getStringExtra(Global.EXTRA_ITEM_UPC);
				for (ProdDetlInfo info : mProdDetlList) {
					if (info.strItemUPC.equals(strItemUPC) == true || info.strItemUPC20.equals(strItemUPC)) {
						strItemNo = info.strItemNo;
						break;
					}
				}
				((EditText) mRootView.findViewById(R.id.editItemNo)).setText(strItemUPC);
				doProcessSelectProduct(strItemNo, false, false);
			}


		}
	}

	private void setType(int nType) {
		mType = nType;

		mSelProductInfo = null;
		int nNormalColor = mContext.getResources().getColor(R.color.vehicle_inventory_normal_color);
		int nSelectedColor = mContext.getResources().getColor(R.color.vehicle_inventory_selected_color);

		mRootView.findViewById(R.id.txtFresh).setBackgroundColor(nNormalColor);
		mRootView.findViewById(R.id.txtReturns).setBackgroundColor(nNormalColor);
		mRootView.findViewById(R.id.txtSpoils).setBackgroundColor(nNormalColor);
		mRootView.findViewById(R.id.txtContainers).setBackgroundColor(nNormalColor);


		if (mType == Global.INVENTORY_FRESH) {
			mRootView.findViewById(R.id.txtFresh).setBackgroundColor(nSelectedColor);
		} else if (mType == Global.INVENTORY_RETURNS) {
			mRootView.findViewById(R.id.txtReturns).setBackgroundColor(nSelectedColor);
		} else if (mType == Global.INVENTORY_SPOILS) {
			mRootView.findViewById(R.id.txtSpoils).setBackgroundColor(nSelectedColor);
		} else if (mType == Global.INVENTORY_CONTAINERS) {
			mRootView.findViewById(R.id.txtContainers).setBackgroundColor(nSelectedColor);
		}

		((TextView) mRootView.findViewById(R.id.txtDescription)).setText("");
		((EditText) mRootView.findViewById(R.id.editItemNo)).setText("");
		if(bShowCase)
		{
			mRootView.findViewById(R.id.txtCaseSplit).setVisibility(View.VISIBLE);
			mRootView.findViewById(R.id.editCase).setVisibility(View.VISIBLE);
			((EditText) mRootView.findViewById(R.id.editCase)).setText("");
			((EditText) mRootView.findViewById(R.id.editQty)).setText("");
		}
		else
		{
			mRootView.findViewById(R.id.txtCaseSplit).setVisibility(View.GONE);
			mRootView.findViewById(R.id.editCase).setVisibility(View.GONE);
			((EditText) mRootView.findViewById(R.id.editQty)).setText("");
		}


		refreshPage();
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

	ProdDetlInfo mSelProductInfo = null;
	private void doSearch(boolean isByItemNo) {
		String strItemNo = getItemCode(((EditText) mRootView.findViewById(R.id.editItemNo)).getText().toString());
		String strDescription = Str.Invalid_Product[Global.LANG];
		String strQty = "";
		mUInvenDIdx = -1;
		mSelProductInfo = null;
		if (strItemNo.length() > 0) {
/*			DbAdapter dbAdapter = new DbAdapter(mContext);
			dbAdapter.open();
			String strData = dbAdapter.getProductListFromUPC(strItemNo);
			if( strData != null )
			{
				strItemNo = strData;
			}
			dbAdapter.close();*/
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

			for (int i=0; i<mUInvenDList.size(); i++) {
				UInvenDInfo info = mUInvenDList.get(i);
				if (info.strItemNo.equals(strItemNo) == true) {
					mUInvenDIdx = i;
					strItemNo = info.strItemNo;
					strDescription = info.strShortDesc;
					if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
					{
						strQty = String.format(Locale.ENGLISH, "%.2f", mType == Global.INVENTORY_RETURNS ? info.dDumpsUnload : info.dUnload);
					}
					else
					{
						strQty = String.valueOf(mType == Global.INVENTORY_RETURNS ? info.nDumpsUnload : info.nUnload);
					}

					break;
				}
			}
		}
		else
		{
			strDescription = "";
		}
		if(Utils.getProdLoconSCWType(mContext, strItemNo) == Global.WEIGHT_ITEM)
		{
			((TextView) mRootView.findViewById(R.id.txtDescription)).setText(strDescription);
			mRootView.findViewById(R.id.editCase).setVisibility(View.GONE);
			mRootView.findViewById(R.id.txtCaseSplit).setVisibility(View.GONE);
			mRootView.findViewById(R.id.editQty).setTag("From App");
			if(mEntryInputMethod.equals("A"))
			{
				((EditText)mRootView.findViewById(R.id.editQty)).setText("0.00");
			}
			else
			{
				if(TextUtils.isEmpty(strQty))
				{
					strQty = "0.00";
				}
				((EditText)mRootView.findViewById(R.id.editQty)).setText(strQty);
			}

			mRootView.findViewById(R.id.editQty).setTag(null);
		}
		else
		{
			if(bShowCase)
			{
				((TextView) mRootView.findViewById(R.id.txtDescription)).setText(strDescription);
				int nCase = getCaseValue(strItemNo);
				int nQty = Utils.parseIntFromString(strQty);
				int nCaseValue = nQty / nCase;
				nQty = nQty % nCase;
				mRootView.findViewById(R.id.editCase).setVisibility(View.VISIBLE);
				mRootView.findViewById(R.id.txtCaseSplit).setVisibility(View.VISIBLE);
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
				((TextView) mRootView.findViewById(R.id.txtDescription)).setText(strDescription);

				int nQty = Utils.parseIntFromString(strQty);
				mRootView.findViewById(R.id.editCase).setVisibility(View.GONE);
				mRootView.findViewById(R.id.txtCaseSplit).setVisibility(View.GONE);
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

	private void doAdd() {
		String strQty = ((EditText) mRootView.findViewById(R.id.editQty)).getText().toString();
		String strCase = ((EditText)mRootView.findViewById(R.id.editCase)).getText().toString();

		if (mUInvenDIdx == -1 && mSelProductInfo == null) {

			if(!TextUtils.isEmpty(((EditText) mRootView.findViewById(R.id.editItemNo)).getText().toString()))
			{
				Utils.printDebug(mContext, Str.Please_select_valid_product[Global.LANG]);
			}
			((EditText) mRootView.findViewById(R.id.editQty)).setText("");
			((EditText) mRootView.findViewById(R.id.editCase)).setText("");
			focusAndHideKeyboard((EditText) mRootView.findViewById(R.id.editItemNo));
			return;
		}
		else if( mUInvenDIdx == -1 && mSelProductInfo != null) {
			if(Utils.getProdLoconSCWType(mContext, mSelProductInfo.strItemNo) == Global.WEIGHT_ITEM)
			{
				mUInvenDIdx = mUInvenDList.size();
				UInvenDInfo info = null;
				for(UInvenDInfo candInfo : mUInvenHInfo.uInvenDList )
				{
					if( candInfo.strItemNo.equals(mSelProductInfo.strItemNo) )
					{
						info = candInfo;
						break;
					}
				}
				if( info == null )
				{
					info = new UInvenDInfo();
					info.strItemNo = mSelProductInfo.strItemNo;
					info.strShortDesc = mSelProductInfo.strShortDesc;
					info.strDescription = mSelProductInfo.strDescription;
					info.strUOM = mSelProductInfo.prodLocnInfo.strCaseUOM.length() > 0 ? mSelProductInfo.prodLocnInfo.strCaseUOM : mSelProductInfo.prodLocnInfo.strUnitUOM;
					info.nWeight = mSelProductInfo.nWeight;
					info.bShow = true;
					info.nOrder = getMaxOrder() + 1;
					if( mType == Global.INVENTORY_FRESH )
						info.nType = Global.INVENTORY_FRESH;
					else if( mType == Global.INVENTORY_CONTAINERS )
						info.nType = Global.INVENTORY_CONTAINERS;
					else if( mType == Global.INVENTORY_RETURNS )
						info.nType = Global.INVENTORY_FRESH;
					else if( mType == Global.INVENTORY_SPOILS )
						info.nType = Global.INVENTORY_FRESH;

					mUInvenHInfo.uInvenDList.add(info);
				}
				mUInvenDList.add(info);
			}
			else
			{
				int nQuantity = 0;
				if(bShowCase)
				{
					int nCase = getCaseValue(mSelProductInfo.strItemNo);
					nQuantity = Utils.parseIntFromString(strQty) + Utils.parseIntFromString(strCase) * nCase;
				}
				else
				{
					nQuantity = Utils.parseIntFromString(strQty);
				}

				if (nQuantity == 0) {
					Utils.printDebug(mContext, Str.Please_input_the_Quantity[Global.LANG]);
					showKeyboard((EditText) mRootView.findViewById(R.id.editQty), true);
					return;
				}
				else
				{
					mUInvenDIdx = mUInvenDList.size();
					UInvenDInfo info = null;
					for(UInvenDInfo candInfo : mUInvenHInfo.uInvenDList )
					{
						if( candInfo.strItemNo.equals(mSelProductInfo.strItemNo) )
						{
							info = candInfo;
							break;
						}
					}
					if( info == null )
					{
						info = new UInvenDInfo();
						info.strItemNo = mSelProductInfo.strItemNo;
						info.strShortDesc = mSelProductInfo.strShortDesc;
						info.strDescription = mSelProductInfo.strDescription;
						info.strUOM = mSelProductInfo.prodLocnInfo.strCaseUOM.length() > 0 ? mSelProductInfo.prodLocnInfo.strCaseUOM : mSelProductInfo.prodLocnInfo.strUnitUOM;
						info.nWeight = mSelProductInfo.nWeight;
						info.bShow = true;
						info.nOrder = getMaxOrder() + 1;
						if( mType == Global.INVENTORY_FRESH )
							info.nType = Global.INVENTORY_FRESH;
						else if( mType == Global.INVENTORY_CONTAINERS )
							info.nType = Global.INVENTORY_CONTAINERS;
						else if( mType == Global.INVENTORY_RETURNS )
							info.nType = Global.INVENTORY_FRESH;
						else if( mType == Global.INVENTORY_SPOILS )
							info.nType = Global.INVENTORY_FRESH;
						mUInvenHInfo.uInvenDList.add(info);
					}
					mUInvenDList.add(info);
				}
			}
		}

		if(Utils.getProdLoconSCWType(mContext,mUInvenDList.get(mUInvenDIdx).strItemNo) != Global.WEIGHT_ITEM)
		{
			if (strCase.length() == 0 && strQty.length() == 0 ) {
				Utils.printDebug(mContext, Str.Please_input_the_Quantity[Global.LANG]);
				showKeyboard((EditText) mRootView.findViewById(R.id.editCase), true);
				return;
			}

			if( strCase.length() == 0 ) strCase = "0";
			if( strQty.length() == 0 ) strQty = "0";
			final int nQuantity;
			if(bShowCase)
			{
				int nCase = getCaseValue(mUInvenDList.get(mUInvenDIdx).strItemNo);
				nQuantity = nCase * Utils.parseIntFromString(strCase) + Utils.parseIntFromString(strQty) ;
			}
			else
			{
				nQuantity = Utils.parseIntFromString(strQty) ;
			}

			if(mEntryInputMethod.equals("A"))
			{
				int nValue;
				int nLoadValue;
				if (mType == Global.INVENTORY_RETURNS) {
					nValue = mUInvenDList.get(mUInvenDIdx).nDumpsUnload;
					nLoadValue = mUInvenDList.get(mUInvenDIdx).nDumps;
				}
				else if(mType == Global.INVENTORY_SPOILS) {
					nValue = mUInvenDList.get(mUInvenDIdx).nSpoils;
					nLoadValue = mUInvenDList.get(mUInvenDIdx).nStock - mUInvenDList.get(mUInvenDIdx).nUnload;
				}
				else {
					nValue = mUInvenDList.get(mUInvenDIdx).nUnload;
					nLoadValue = mUInvenDList.get(mUInvenDIdx).nStock - mUInvenDList.get(mUInvenDIdx).nSpoils;
				}
				if(nValue + nQuantity < 0)
				{
					((EditText) mRootView.findViewById(R.id.editQty)).setText("");
					((EditText) mRootView.findViewById(R.id.editCase)).setText("");
					Utils.printDebug(mContext, Str.Quantity_Removed_From_the_transaction[Global.LANG]);
					showKeyboard((EditText) mRootView.findViewById(R.id.editQty), true);
					return;
				}

				if(nValue + nQuantity > nLoadValue)
				{
					if(nInventoryCheck == 0)
					{
						doProcessAdd(nQuantity);
					}
					else if(nInventoryCheck == 1 || nInventoryCheck == 3)
					{
						try {
							final MessageDialog msgDialog = new MessageDialog(mContext);
							msgDialog.setCancelable(false);
							msgDialog.mStrTitle = Str.Warning[Global.LANG];
							msgDialog.mStrMessage = Str.OffloadOverflowMessage[Global.LANG];
							msgDialog.mStrLeft = Str.Return[Global.LANG];
							msgDialog.mStrRight = Str.Proceed[Global.LANG];
							msgDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
								@Override
								public void onDismiss(DialogInterface dialog) {
									if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
										doProcessAdd(nQuantity);
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
					else if(nInventoryCheck == 2 || nInventoryCheck == 4)
					{
						try {
							final MessageDialog msgDialog = new MessageDialog(mContext);
							msgDialog.setCancelable(false);
							msgDialog.mStrTitle = Str.Alert[Global.LANG];
							msgDialog.mStrMessage = Str.OffloadOverflowMessage[Global.LANG];
							msgDialog.mStrMiddle = Str.Return[Global.LANG];
							msgDialog.show();
						}catch (Exception e)
						{
							e.printStackTrace();
						}

						return;
					}
					else
					{
						doProcessAdd(nQuantity);
					}
				}
				else
				{
					doProcessAdd(nQuantity);
				}
			}
			else
			{
				int nLoadValue = 0;
				if (mType == Global.INVENTORY_RETURNS) {
					nLoadValue = mUInvenDList.get(mUInvenDIdx).nDumps;
				}
				else if(mType == Global.INVENTORY_SPOILS) {
					nLoadValue = mUInvenDList.get(mUInvenDIdx).nStock - mUInvenDList.get(mUInvenDIdx).nUnload;
				}
				else {
					nLoadValue = mUInvenDList.get(mUInvenDIdx).nStock - mUInvenDList.get(mUInvenDIdx).nSpoils;
				}

				if(nQuantity > nLoadValue)
				{
					if(nInventoryCheck == 0)
					{
						doProcessAdd(nQuantity);
					}
					else if(nInventoryCheck == 1 || nInventoryCheck == 3)
					{
						try {
							final MessageDialog msgDialog = new MessageDialog(mContext);
							msgDialog.setCancelable(false);
							msgDialog.mStrTitle = Str.Warning[Global.LANG];
							msgDialog.mStrMessage = Str.OffloadOverflowMessage[Global.LANG];
							msgDialog.mStrLeft = Str.Return[Global.LANG];
							msgDialog.mStrRight = Str.Proceed[Global.LANG];
							msgDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
								@Override
								public void onDismiss(DialogInterface dialog) {
									if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
										doProcessAdd(nQuantity);
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
					else if(nInventoryCheck == 2 || nInventoryCheck == 4)
					{
						try {
							final MessageDialog msgDialog = new MessageDialog(mContext);
							msgDialog.setCancelable(false);
							msgDialog.mStrTitle = Str.Alert[Global.LANG];
							msgDialog.mStrMessage = Str.OffloadOverflowMessage[Global.LANG];
							msgDialog.mStrMiddle = Str.Ok[Global.LANG];
							msgDialog.show();
						}catch (Exception e)
						{
							e.printStackTrace();
						}
						return;
					}
					else
					{
						doProcessAdd(nQuantity);
					}
				}
				else
				{
					doProcessAdd(nQuantity);
				}
			}
		}
		else
		{
			final double dQty = Utils.parseDoubleFromString(strQty);
			if(mEntryInputMethod.equals("A"))
			{
				double dValue = 0;
				double dLoadValue = 0;
				if (mType == Global.INVENTORY_RETURNS) {
					dValue = mUInvenDList.get(mUInvenDIdx).dDumpsUnload;
					dLoadValue = mUInvenDList.get(mUInvenDIdx).dDumps;
				}
				else if(mType == Global.INVENTORY_SPOILS) {
					dValue = mUInvenDList.get(mUInvenDIdx).dSpoils;
					dLoadValue = mUInvenDList.get(mUInvenDIdx).dStock - mUInvenDList.get(mUInvenDIdx).dUnload;
				}
				else {
					dValue = mUInvenDList.get(mUInvenDIdx).dUnload;
					dLoadValue = mUInvenDList.get(mUInvenDIdx).dStock - mUInvenDList.get(mUInvenDIdx).dSpoils;
				}
				if(dValue + dQty < 0)
				{
					((EditText) mRootView.findViewById(R.id.editCase)).setText("");
					Utils.printDebug(mContext, Str.Quantity_Removed_From_the_transaction[Global.LANG]);
					showKeyboard((EditText) mRootView.findViewById(R.id.editQty), true);
					return;
				}
				if(dValue + dQty > dLoadValue)
				{
					if(nInventoryCheck == 0)
					{
						doProcessAdd(dQty);
					}
					else if(nInventoryCheck == 1 || nInventoryCheck == 3)
					{
						try {
							final MessageDialog msgDialog = new MessageDialog(mContext);
							msgDialog.setCancelable(false);
							msgDialog.mStrTitle = Str.Warning[Global.LANG];
							msgDialog.mStrMessage = Str.OffloadOverflowMessage[Global.LANG];
							msgDialog.mStrLeft = Str.Return[Global.LANG];
							msgDialog.mStrRight = Str.Proceed[Global.LANG];
							msgDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
								@Override
								public void onDismiss(DialogInterface dialog) {
									if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
										doProcessAdd(dQty);
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
					else if(nInventoryCheck == 2 || nInventoryCheck == 4)
					{
						try {
							final MessageDialog msgDialog = new MessageDialog(mContext);
							msgDialog.setCancelable(false);
							msgDialog.mStrTitle = Str.Alert[Global.LANG];
							msgDialog.mStrMessage = Str.OffloadOverflowMessage[Global.LANG];
							msgDialog.mStrMiddle = Str.Ok[Global.LANG];
							msgDialog.show();
						}catch (Exception e)
						{
							e.printStackTrace();
						}

						return;
					}
					else
					{
						doProcessAdd(dQty);
					}
				}
				else
				{
					doProcessAdd(dQty);
				}
			}
			else
			{
				double dLoadValue = 0;
				if (mType == Global.INVENTORY_RETURNS) {
					dLoadValue = mUInvenDList.get(mUInvenDIdx).dDumps;
				}
				else if(mType == Global.INVENTORY_SPOILS) {
					dLoadValue = mUInvenDList.get(mUInvenDIdx).dStock - mUInvenDList.get(mUInvenDIdx).dUnload;
				}
				else {
					dLoadValue = mUInvenDList.get(mUInvenDIdx).dStock - mUInvenDList.get(mUInvenDIdx).dSpoils;
				}

				if(dQty > dLoadValue)
				{
					if(nInventoryCheck == 0)
					{
						doProcessAdd(dQty);
					}
					else if(nInventoryCheck == 1 || nInventoryCheck == 3)
					{
						try {
							final MessageDialog msgDialog = new MessageDialog(mContext);
							msgDialog.setCancelable(false);
							msgDialog.mStrTitle = Str.Warning[Global.LANG];
							msgDialog.mStrMessage = Str.OffloadOverflowMessage[Global.LANG];
							msgDialog.mStrLeft = Str.Return[Global.LANG];
							msgDialog.mStrRight = Str.Proceed[Global.LANG];
							msgDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
								@Override
								public void onDismiss(DialogInterface dialog) {
									if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
										doProcessAdd(dQty);
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
					else if(nInventoryCheck == 2 || nInventoryCheck == 4)
					{
						try {
							final MessageDialog msgDialog = new MessageDialog(mContext);
							msgDialog.setCancelable(false);
							msgDialog.mStrTitle = Str.Alert[Global.LANG];
							msgDialog.mStrMessage = Str.OffloadOverflowMessage[Global.LANG];
							msgDialog.mStrMiddle = Str.Ok[Global.LANG];
							msgDialog.show();
						}catch (Exception e)
						{
							e.printStackTrace();
						}

						return;
					}
					else
					{
						doProcessAdd(dQty);
					}
				}
				else
				{
					doProcessAdd(dQty);
				}
			}
		}
	}

	private void doProcessAdd(int nQty)
	{
		int nValue = 0;
		if (mType == Global.INVENTORY_RETURNS) {
			nValue = mUInvenDList.get(mUInvenDIdx).nDumpsUnload;
		}
		else if(mType == Global.INVENTORY_SPOILS) {
			nValue = mUInvenDList.get(mUInvenDIdx).nSpoils;
		}
		else {
			nValue = mUInvenDList.get(mUInvenDIdx).nUnload;
		}

		if(mEntryInputMethod.equals("A"))
		{
			if (mType == Global.INVENTORY_RETURNS) {
				mUInvenDList.get(mUInvenDIdx).nDumpsUnload += nQty;
			}
			else if(mType == Global.INVENTORY_SPOILS) {
				mUInvenDList.get(mUInvenDIdx).nSpoils += nQty;
			}
			else {
				mUInvenDList.get(mUInvenDIdx).nUnload += nQty;
			}
			mUInvenDList.get(mUInvenDIdx).nEndAdj = nValue + nQty - mUInvenDList.get(mUInvenDIdx).nStock;
		}
		else
		{
			if (mType == Global.INVENTORY_RETURNS) {
				mUInvenDList.get(mUInvenDIdx).nDumpsUnload = nQty;
			}
			else if(mType == Global.INVENTORY_SPOILS) {
				mUInvenDList.get(mUInvenDIdx).nSpoils = nQty;
			}
			else {
				mUInvenDList.get(mUInvenDIdx).nUnload = nQty;
			}
			mUInvenDList.get(mUInvenDIdx).nEndAdj = nQty - mUInvenDList.get(mUInvenDIdx).nStock;
		}

		mSelProductInfo = null;
		if(bShowCase)
		{
			mRootView.findViewById(R.id.editCase).setVisibility(View.VISIBLE);
			mRootView.findViewById(R.id.txtCaseSplit).setVisibility(View.VISIBLE);
			((EditText) mRootView.findViewById(R.id.editItemNo)).setText("");
			((EditText) mRootView.findViewById(R.id.editCase)).setText("");
			((EditText) mRootView.findViewById(R.id.editQty)).setText("");
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

	private void doProcessAdd(double dQty)
	{
		double dValue = 0;
		if (mType == Global.INVENTORY_RETURNS) {
			dValue = mUInvenDList.get(mUInvenDIdx).dDumpsUnload;
		}
		else if(mType == Global.INVENTORY_SPOILS) {
			dValue = mUInvenDList.get(mUInvenDIdx).dSpoils;
		}
		else {
			dValue = mUInvenDList.get(mUInvenDIdx).dUnload;
		}

		if(mEntryInputMethod.equals("A"))
		{
			if (mType == Global.INVENTORY_RETURNS) {
				mUInvenDList.get(mUInvenDIdx).dDumpsUnload += dQty;
			}
			else if(mType == Global.INVENTORY_SPOILS) {
				mUInvenDList.get(mUInvenDIdx).dSpoils += dQty;
			}
			else {
				mUInvenDList.get(mUInvenDIdx).dUnload += dQty;
			}
			mUInvenDList.get(mUInvenDIdx).dEndAdj += dQty + dValue - mUInvenDList.get(mUInvenDIdx).dStock;
		}
		else
		{
			if (mType == Global.INVENTORY_RETURNS) {
				mUInvenDList.get(mUInvenDIdx).dDumpsUnload = dQty;
			}
			else if(mType == Global.INVENTORY_SPOILS) {
				mUInvenDList.get(mUInvenDIdx).dSpoils = dQty;
			}
			else {
				mUInvenDList.get(mUInvenDIdx).dUnload = dQty;
			}
			mUInvenDList.get(mUInvenDIdx).dEndAdj = dQty - mUInvenDList.get(mUInvenDIdx).dStock;
		}
		mSelProductInfo = null;
		if(bShowCase)
		{
			mRootView.findViewById(R.id.editCase).setVisibility(View.VISIBLE);
			mRootView.findViewById(R.id.txtCaseSplit).setVisibility(View.VISIBLE);
			((EditText) mRootView.findViewById(R.id.editItemNo)).setText("");
			((EditText) mRootView.findViewById(R.id.editCase)).setText("");
			((EditText) mRootView.findViewById(R.id.editQty)).setText("");
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

	private int getMaxOrder() {
		int nOrder = 0;
		for (UInvenDInfo info : mUInvenDList) {
			if (info.nOrder > nOrder) {
				nOrder = info.nOrder;
			}
		}
		return nOrder;
	}

	private void showInvenDetail(UInvenDInfo info) {
		((EditText) mRootView.findViewById(R.id.editItemNo)).setText(Utils.getDisplayItemCode(mContext, info.strItemNo));
		if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
		{
			double dUnLoaded = 0;
			if( mType == Global.INVENTORY_FRESH )
			{
				dUnLoaded = info.dUnload;
			}
			else if(mType == Global.INVENTORY_RETURNS)
			{
				dUnLoaded = info.dDumpsUnload;
			}
			else
			{
				dUnLoaded = info.nUnload;
			}
			String strQty = String.format(Locale.ENGLISH, "%.2f", dUnLoaded);
			mRootView.findViewById(R.id.txtCaseSplit).setVisibility(View.GONE);
			mRootView.findViewById(R.id.editCase).setVisibility(View.GONE);
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
			int nUnLoaded = 0;
			if( mType == Global.INVENTORY_FRESH )
			{
				nUnLoaded = info.nUnload;
			}
			else if(mType == Global.INVENTORY_RETURNS)
			{
				nUnLoaded = info.nDumpsUnload;
			}
			else if(mType == Global.INVENTORY_SPOILS)
			{
				nUnLoaded = info.nSpoils;
			}
			else if(mType == Global.INVENTORY_CONTAINERS)
			{
				nUnLoaded = info.nUnload;
			}
			if(bShowCase)
			{
				int nCase = getCaseValue(info.strItemNo);
				int nQty = nUnLoaded % nCase;
				int nCaseValue = nUnLoaded / nCase;
				String strQty = String.valueOf(info.nStock);

				mRootView.findViewById(R.id.txtCaseSplit).setVisibility(View.VISIBLE);
				mRootView.findViewById(R.id.editCase).setVisibility(View.VISIBLE);
				if(mEntryInputMethod.equals("A"))
				{
					((EditText) mRootView.findViewById(R.id.editCase)).setText("");
					((EditText) mRootView.findViewById(R.id.editQty)).setText("");
					mRootView.findViewById(R.id.editCase).requestFocus();
					((EditText) mRootView.findViewById(R.id.editCase)).setSelection(0);
				}
				else
				{
					((EditText) mRootView.findViewById(R.id.editCase)).setText(String.valueOf(nCaseValue));
					((EditText) mRootView.findViewById(R.id.editQty)).setText(String.valueOf(nQty));
					mRootView.findViewById(R.id.editCase).requestFocus();
					((EditText) mRootView.findViewById(R.id.editCase)).setSelection(String.valueOf(nCaseValue).length());
				}

			}
			else
			{
				int nQty = nUnLoaded;
				String strQty = String.valueOf(info.nStock);

				mRootView.findViewById(R.id.txtCaseSplit).setVisibility(View.GONE);
				mRootView.findViewById(R.id.editCase).setVisibility(View.GONE);
				if(mEntryInputMethod.equals("A"))
				{
					((EditText) mRootView.findViewById(R.id.editQty)).setText("");
					mRootView.findViewById(R.id.editQty).requestFocus();
					((EditText) mRootView.findViewById(R.id.editQty)).setSelection(0);
				}
				else
				{
					((EditText) mRootView.findViewById(R.id.editQty)).setText(String.valueOf(nQty));
					mRootView.findViewById(R.id.editQty).requestFocus();
					((EditText) mRootView.findViewById(R.id.editQty)).setSelection(String.valueOf(nQty).length());
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

	boolean m_bClicked = false;
	@Override
	public void onClick(View v) {
		if( m_bClicked ) return;
		m_bClicked = true;
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
			case R.id.btnDone:
				Global.gCurEndingInventory = mUInvenHInfo;
				Global.gCurEndingInventoryList = mUInvenDList;
				boolean bSpoilExist = false;
				for(UInvenDInfo info : mUInvenHInfo.uInvenDList)
				{
					if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
					{
						if( info.dSpoils > 0 )
						{
							bSpoilExist = true;
							break;
						}
					}
					else
					{
						if( info.nSpoils > 0 )
						{
							bSpoilExist = true;
							break;
						}
					}
				}
				if( bSpoilExist )
				{
					//For Spoil Object, set ReasonCode.
					int nReasonCode = 0;
					if( mSpoilsReasonDescList.size() == 0 )
					{
						nReasonCode = 1;
					}
					else if( mSpoilsReasonDescList.size() == 1)
					{
						nReasonCode = mSpoilsReasonDescList.get(0).nNumericKey;
					}
					else
					{
						mContext.replaceFragment(new SpoilEntryFragment());
						break;
					}
					for(UInvenDInfo info : mUInvenHInfo.uInvenDList)
					{
						if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
						{
							if(info.dSpoils > 0)
							{
								info.nReasonCode = nReasonCode;
							}
						}
						else{
							if(info.nSpoils > 0)
							{
								info.nReasonCode = nReasonCode;
							}
						}
					}
				}
				mContext.replaceFragment(new EndingInventorySummaryFragment());
				break;
			case R.id.txtFresh:
				setType(Global.INVENTORY_FRESH);
				break;
			case R.id.txtReturns:
				setType(Global.INVENTORY_RETURNS);
				break;
			case R.id.txtSpoils:
				setType(Global.INVENTORY_SPOILS);
				break;
			case R.id.txtContainers:
				setType(Global.INVENTORY_CONTAINERS);
				break;
			case R.id.btnProductTreeList:
			{
				DbAdapter dbAdapter = new DbAdapter(mContext);
				dbAdapter.open();
				List<ProdStruInfo> prodStruList = dbAdapter.getProdStruList();
				dbAdapter.close();
				if( prodStruList != null && prodStruList.size() > 0 )
				{
					NewProductTreelistFragment fragment = new NewProductTreelistFragment();
					fragment.setFrom(this);
					mContext.replaceFragment(fragment);
				}
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
		}
		m_bClicked = false;
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

	private class ListAdapter extends BaseAdapter {

		private List<UInvenDInfo> list = new ArrayList<>();

		private class ViewHolder {
			public TextView txtItemNo, txtShortDesc, txtQty, txtOffload;
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
				view = mContext.getLayoutInflater().inflate(R.layout.row_off_load_inventory, parent, false);
				holder = new ViewHolder();
				holder.txtItemNo = (TextView) view.findViewById(R.id.txtItemNo);
				holder.txtShortDesc = (TextView) view.findViewById(R.id.txtShortDesc);
				holder.txtQty = (TextView) view.findViewById(R.id.txtQty);
				holder.txtOffload = (TextView) view.findViewById(R.id.txtOffload);
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
				if (mType == Global.INVENTORY_RETURNS) {
					holder.txtQty.setText(String.format(Locale.ENGLISH, "%.2f", info.dDumps));
					holder.txtOffload.setText(String.format(Locale.ENGLISH, "%.2f", info.dDumpsUnload));
				}
				else if(mType == Global.INVENTORY_SPOILS) {
					holder.txtQty.setText(String.format(Locale.ENGLISH, "%.2f", 0f));
					holder.txtOffload.setText(String.format(Locale.ENGLISH, "%.2f", info.dSpoils));
				}
				else {
					holder.txtQty.setText(String.format(Locale.ENGLISH, "%.2f", info.dStock));
					holder.txtOffload.setText(String.format(Locale.ENGLISH, "%.2f", info.dUnload));
				}
			}
			else
			{
				if(bShowCase)
				{
					int nCase = getCaseValue(info.strItemNo);
					if (mType == Global.INVENTORY_RETURNS) {
						if (Utils.isContainerItem(mContext, info.strItemNo)) {
							holder.txtQty.setText(String.format(Locale.ENGLISH, "%d", info.nDumps));
							holder.txtOffload.setText(String.format(Locale.ENGLISH, "%d", info.nDumpsUnload));
						}
						else {
							holder.txtQty.setText(String.format(Locale.ENGLISH, "%d/%d", info.nDumps / nCase, info.nDumps % nCase));
							holder.txtOffload.setText(String.format(Locale.ENGLISH, "%d/%d", info.nDumpsUnload / nCase, info.nDumpsUnload % nCase));
						}
					}
					else if(mType == Global.INVENTORY_SPOILS) {
						if (Utils.isContainerItem(mContext, info.strItemNo)) {
							holder.txtQty.setText(String.format(Locale.ENGLISH, "%d", 0));
							holder.txtOffload.setText(String.format(Locale.ENGLISH, "%d", info.nSpoils));
						}
						else {
							holder.txtQty.setText(String.format(Locale.ENGLISH, "%d/%d", 0, 0));
							holder.txtOffload.setText(String.format(Locale.ENGLISH, "%d/%d", info.nSpoils / nCase, info.nSpoils % nCase));
						}
					}
					else {
						if (Utils.isContainerItem(mContext, info.strItemNo)) {
							holder.txtQty.setText(String.format(Locale.ENGLISH, "%d", info.nStock));
							holder.txtOffload.setText(String.format(Locale.ENGLISH, "%d", info.nUnload));
						}
						else {
							holder.txtQty.setText(String.format(Locale.ENGLISH, "%d/%d", info.nStock / nCase, info.nStock % nCase));
							holder.txtOffload.setText(String.format(Locale.ENGLISH, "%d/%d", info.nUnload / nCase, info.nUnload % nCase));
						}
					}
				}
				else
				{
					if (mType == Global.INVENTORY_RETURNS) {
						holder.txtQty.setText(String.format(Locale.ENGLISH, "%d", info.nDumps));
						holder.txtOffload.setText(String.format(Locale.ENGLISH, "%d", info.nDumpsUnload));
					}
					else if(mType == Global.INVENTORY_SPOILS) {
						holder.txtQty.setText(String.format(Locale.ENGLISH, "%d", 0));
						holder.txtOffload.setText(String.format(Locale.ENGLISH, "%d", info.nSpoils));
					}
					else {
						holder.txtQty.setText(String.format(Locale.ENGLISH, "%d", info.nStock));
						holder.txtOffload.setText(String.format(Locale.ENGLISH, "%d", info.nUnload));
					}
				}
			}

			if( Utils.getIntSetting(mContext, Global.PREF_INVENQTYOFF) == 2 && mType == Global.INVENTORY_FRESH)
			{
				holder.txtQty.setVisibility(View.GONE);
			}
			else
			{
				holder.txtQty.setVisibility(View.VISIBLE);
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
}