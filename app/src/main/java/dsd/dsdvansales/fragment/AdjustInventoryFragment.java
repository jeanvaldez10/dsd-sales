package dsd.dsdvansales.fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

import androidx.core.app.ActivityCompat;
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
import dsd.dsdvansales.bluetooth.BluetoothService;
import dsd.dsdvansales.db.DbAdapter;
import dsd.dsdvansales.dialogs.BatchCodeInputDialog;
import dsd.dsdvansales.dialogs.DiscoveryBluetoothDeviceDialog;
import dsd.dsdvansales.dialogs.InventoryMessageDialog;
import dsd.dsdvansales.dialogs.InventoryNotApprovalDialog;
import dsd.dsdvansales.dialogs.MessageDialog;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.model.AdjustInventoryBluetoothData;
import dsd.dsdvansales.model.BluetoothTransferData;
import dsd.dsdvansales.model.CustomerOrderInfo;
import dsd.dsdvansales.model.DescTypeInfo;
import dsd.dsdvansales.model.InventoryData;
import dsd.dsdvansales.model.LoadBlotInfo;
import dsd.dsdvansales.model.OrderLineInfo;
import dsd.dsdvansales.model.PresoldOrDInfo;
import dsd.dsdvansales.model.ProdDetlInfo;
import dsd.dsdvansales.model.ProdInfo;
import dsd.dsdvansales.model.ProdStruInfo;
import dsd.dsdvansales.model.UInvenDInfo;
import dsd.dsdvansales.views.AlertSingleChoiceAdapter;

import static android.app.Activity.RESULT_OK;

public class AdjustInventoryFragment extends BaseFragment implements View.OnClickListener {

	private static int REQUEST_ENABLE_BT = 1011;
	private TextView mTxtNoData = null;
	private TextView mTxtSlideNoData = null;
	private ListView mListView = null;
	private ListView mSlideListView = null;
	private ListAdapter mListAdapter = null;
	private SlideListAdapter mSlideListAdapter = null;
	private List<UInvenDInfo> mUInvenDList = new ArrayList<>();
	private UInvenDInfo mCurSelInvenDInfo = null;
	private List<ProdDetlInfo> mProdDetlList = new ArrayList<>();
	private int mProdDetlIdx = -1;
	private int mTransferType = 0;
	private int mInventoryType = Global.INVENTORY_FRESH;

	private Map<String, Integer> mCaseMap = new HashMap<>();
	private Map<String, Integer> mOrderSequenceMap = new HashMap<>();
	private Map<String, ProdDetlInfo> mProdUPCDetMap = new HashMap<>();
	private Map<String, ProdDetlInfo> mProdDetlItemMap = new HashMap<>();
	private Map<String, ProdInfo> mProdInfoMap = new HashMap<>();

	private List<CustomerOrderInfo> latestOrderInfo = new ArrayList<>();
	List<UInvenDInfo> mAllInvenDInfos = null;
	private boolean bLoaded = false;
	private boolean bShowCase = true;
	private String strEntryMethod = "";
	private String mTransferRoute = "";
	private boolean bleProcess = true;

	private String sortFlag = "";
	private boolean filter = false;
	private boolean bLaterTransfer = false;

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
		Global.gFragIdx = Global.FRAG_ADJUST_INVENTORY;

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
		mTxtSlideNoData = (TextView)mRootView.findViewById(R.id.txtSlideNoData);

		mListView = (ListView) mRootView.findViewById(R.id.listView);
		mSlideListView = (ListView)mRootView.findViewById(R.id.slidelistView);
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

		Bundle args = getArguments();
		mTransferType = args.getInt(Global.EXTRA_TRANSFER_TYPE);
		mInventoryType = args.getInt(Global.EXTRA_INVENTORY_TYPE);
		bLaterTransfer = args.getBoolean(Global.EXTRA_TRANSFER_LATER);

		if (mTransferType == 0) {
			mRootView.findViewById(R.id.btnAdd).setBackgroundResource(R.drawable.btn_orange_large);
		} else {
			mRootView.findViewById(R.id.btnAdd).setBackgroundResource(R.drawable.btn_red_large);
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
			mProdDetlList.clear();
			DbAdapter dbAdapter = new DbAdapter(mContext);
			dbAdapter.open();
			latestOrderInfo.clear();
			latestOrderInfo.addAll(dbAdapter.getAllCustomerLatestOrderList());
			mProdDetlList = dbAdapter.getProdDetlList(mProdUPCDetMap);
			dbAdapter.getProdDetlList(mProdDetlItemMap, null);
//			if(Utils.getStringSetting(mContext, Global.PREF_ITEM_CODE_DISPLAY_SOURCE).equals("PRODINFO"))
//			{
//				mProdInfoMap = dbAdapter.getProdInfoMap("ItemCode");
//			}
			List<UInvenDInfo> uInvenDInfos = dbAdapter.getUInvenDList(Utils.getLongSetting(mContext, Global.PREF_UINVEN_TRXN_NO));
			dbAdapter.close();

			mUInvenDList.clear();

			mAllInvenDInfos = uInvenDInfos;
			for (UInvenDInfo uInvenDInfo : uInvenDInfos) {
				if(Utils.getProdLoconSCWType(mContext, uInvenDInfo.strItemNo) == Global.WEIGHT_ITEM)
				{
					if( uInvenDInfo.bShow )
					{
						uInvenDInfo.dAdjQty = 0;
						mUInvenDList.add(uInvenDInfo);
					}
				}
				else
				{
					if( uInvenDInfo.bShow )
					{
						uInvenDInfo.nAdjQty = 0;
						mUInvenDList.add(uInvenDInfo);
					}
				}
			}

		}
		bLoaded = true;
		refreshPage();
	}

	@Override
	public void refreshPage() {
//		Collections.sort(list, UInvenDInfo.codeComparator);
//		Collections.sort(list, UInvenDInfo.orderComparator);
		List<UInvenDInfo> uOrderDInfoList = new ArrayList<>();
		if(filter)
		{
			for(UInvenDInfo info : mUInvenDList)
			{
				if(info.dAdjQty != 0 || info.nAdjQty != 0)
				{
					uOrderDInfoList.add(info);
				}
			}
		}
		else
		{
			uOrderDInfoList.addAll(mUInvenDList);
		}

		if (uOrderDInfoList.size() > 0) {
			mListView.setVisibility(View.VISIBLE);
			mSlideListView.setVisibility(View.VISIBLE);
			mTxtNoData.setVisibility(View.GONE);
			mTxtSlideNoData.setVisibility(View.GONE);
			mListAdapter.setListData(uOrderDInfoList);
			mSlideListAdapter.setListData(uOrderDInfoList);
			mListAdapter.notifyDataSetChanged();
			mSlideListAdapter.notifyDataSetChanged();
		} else {
			mListView.setVisibility(View.GONE);
			mSlideListView.setVisibility(View.GONE);
			mTxtNoData.setVisibility(View.VISIBLE);
			mTxtSlideNoData.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void refreshLanguage() {
		if(mTransferType == 2)
		{
			((TextView) mRootView.findViewById(R.id.lblTitle)).setText(Str.InterTruckTransfer[Global.LANG].toUpperCase());
		}
		else
		{
			((TextView) mRootView.findViewById(R.id.lblTitle)).setText(Str.AdjustInventory[Global.LANG].toUpperCase());
		}
		((TextView) mRootView.findViewById(R.id.txtTrip)).setText(Str.Trip[Global.LANG] + " # " + Global.gTripNumber);
		((TextView) mRootView.findViewById(R.id.txtDescription)).setText("");
		((Button) mRootView.findViewById(R.id.btnDone)).setText(Str.Done[Global.LANG]);
		if (mTransferType == 0) {
			((Button) mRootView.findViewById(R.id.btnAdd)).setText(Str.Add[Global.LANG]);
		} else if(mTransferType == 1){
			((Button) mRootView.findViewById(R.id.btnAdd)).setText(Str.Minus[Global.LANG]);
		} else {
			((Button) mRootView.findViewById(R.id.btnAdd)).setText(Str.Transfer[Global.LANG]);
		}
		mTxtNoData.setText(Str.No_data_has_been_found[Global.LANG]);
		mTxtSlideNoData.setText(Str.No_data_has_been_found[Global.LANG]);
	}

	private void doProcessSelectProduct(final String strItemNo, boolean bUnit, boolean bUpdateItemNo, boolean isSearchByItemNo)
	{
		if (strItemNo.length() == 0) {
			Utils.printDebug(mContext, Str.NoProductFound[Global.LANG]);
		} else {
			if(!Utils.getStringSetting(mContext, Global.PREF_AUTO_SCAN_ADD).equals("1") || bUpdateItemNo)
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
				doProcessSelectProduct(strItemNo, bUnit, true, true);
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
			if(mProdDetlIdx != -1)
			{
				ProdDetlInfo productInfo = mProdDetlList.get(mProdDetlIdx);
				for (int i = 0; i < mUInvenDList.size(); i++) {
					if (mUInvenDList.get(i).strItemNo.equals(productInfo.strItemNo) == true) {
						if(Utils.getProdLoconSCWType(mContext, strItemNo) == Global.WEIGHT_ITEM)
						{
							strQty = String.format(Locale.ENGLISH, "%.2f", mUInvenDList.get(i).dAdjQty);
						}
						else
						{
							strQty = String.valueOf(mUInvenDList.get(i).nAdjQty);
						}
						break;
					}
				}

				if(TextUtils.isEmpty(strQty))
				{
					for(int i = 0; i < mAllInvenDInfos.size(); i++){
						if( mAllInvenDInfos.get(i).strItemNo.equalsIgnoreCase(productInfo.strItemNo) == true){
							mAllInvenDInfos.get(i).bShow = true;
							mUInvenDList.add(mAllInvenDInfos.get(i));
							if(Utils.getProdLoconSCWType(mContext, strItemNo) == Global.WEIGHT_ITEM)
							{
								strQty = String.format(Locale.ENGLISH, "%.2f", mAllInvenDInfos.get(i).dAdjQty);
							}
							else
							{
								strQty = String.valueOf(mAllInvenDInfos.get(i).nAdjQty);
							}
							break;
						}
					}
				}
			}


			((TextView) mRootView.findViewById(R.id.txtDescription)).setText(strDescription);
			if(Utils.getProdLoconSCWType(mContext, strItemNo) == Global.WEIGHT_ITEM)
			{
				(mRootView.findViewById(R.id.editCase)).setVisibility(View.GONE);
				(mRootView.findViewById(R.id.txtCaseSplit)).setVisibility(View.GONE);
				(mRootView.findViewById(R.id.editQty)).setTag("From App");
				((EditText) mRootView.findViewById(R.id.editQty)).setText(strQty);
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

					((EditText) mRootView.findViewById(R.id.editCase)).setText(String.valueOf(nCaseValue));
					((EditText) mRootView.findViewById(R.id.editQty)).setText(String.valueOf(nQty));

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
					((EditText) mRootView.findViewById(R.id.editQty)).setText(String.valueOf(nQty));
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
				((EditText) mRootView.findViewById(R.id.editQty)).setText("0");
				((EditText) mRootView.findViewById(R.id.editCase)).setText("0");
			}
			else
			{
				(mRootView.findViewById(R.id.editCase)).setVisibility(View.GONE);
				(mRootView.findViewById(R.id.txtCaseSplit)).setVisibility(View.GONE);
				((TextView) mRootView.findViewById(R.id.txtDescription)).setText("");
				((EditText) mRootView.findViewById(R.id.editQty)).setText("0");
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

		//Check the Quantity
		if(mTransferType == 1)
		{
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

			if(nIdx == -1)
			{
				doProcessAdd(strQty, strCase);
				return;
			}
			UInvenDInfo uInvenDInfo = mUInvenDList.get(nIdx);

			int nInvenCheck = Utils.getIntSetting(mContext, Global.PREF_INVENCKECK);
			switch (nInvenCheck)
			{
				case 1: case 2:
				{
					if(Utils.getProdLoconSCWType(mContext, mProdDetlList.get(mProdDetlIdx).strItemNo) == Global.WEIGHT_ITEM)
					{
						float dQuantity = Utils.parseFloatFromString(strQty);
						if(uInvenDInfo.dStock + uInvenDInfo.dAdjQty - uInvenDInfo.dDumps - dQuantity < 0)
						{
							if(nInvenCheck == 1)
							{
								final InventoryMessageDialog msgDialog = new InventoryMessageDialog(mContext);
								msgDialog.setCancelable(false);
								msgDialog.mStrTitle = Str.Warning[Global.LANG];
								msgDialog.mStrCustomerName = uInvenDInfo.strItemNo + "\n" + uInvenDInfo.strDescription;
								msgDialog.mStrMessage = Str.This_adjustment_is_for_more_inventory[Global.LANG];
								msgDialog.mStrLeft = Str.Return[Global.LANG];
								msgDialog.mStrRight = Str.Proceed[Global.LANG];
								msgDialog.setOnDismissListener(dialog -> {
									if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {

									} else if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
										doProcessAdd(strQty, strCase);
									}
								});
								msgDialog.show();
							}
							else {
								final InventoryMessageDialog msgDialog = new InventoryMessageDialog(mContext);
								msgDialog.setCancelable(false);
								msgDialog.mStrTitle = Str.Warning[Global.LANG];
								msgDialog.mStrCustomerName = uInvenDInfo.strItemNo + "\n" + uInvenDInfo.strDescription;
								msgDialog.mStrMessage = Str.This_adjustment_is_for_more_inventory[Global.LANG];
								msgDialog.mStrLeft = Str.Return[Global.LANG];
								msgDialog.setOnDismissListener(dialog -> {
									if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {

									}
								});
								msgDialog.show();
							}
						}
						else
						{
							doProcessAdd(strQty, strCase);
						}
					}
					else
					{
						int nQuantity = Utils.parseIntFromString(strQty);
						if(uInvenDInfo.nStock + uInvenDInfo.nAdjQty - uInvenDInfo.nDumps - nQuantity < 0)
						{
							if(nInvenCheck == 1)
							{
								final InventoryMessageDialog msgDialog = new InventoryMessageDialog(mContext);
								msgDialog.setCancelable(false);
								msgDialog.mStrTitle = Str.Warning[Global.LANG];
								msgDialog.mStrCustomerName = uInvenDInfo.strItemNo + "\n" + uInvenDInfo.strDescription;
								msgDialog.mStrMessage = Str.This_adjustment_is_for_more_inventory[Global.LANG];
								msgDialog.mStrLeft = Str.Return[Global.LANG];
								msgDialog.mStrRight = Str.Proceed[Global.LANG];
								msgDialog.setOnDismissListener(dialog -> {
									if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {

									} else if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
										doProcessAdd(strQty, strCase);
									}
								});
								msgDialog.show();
							}
							else {
								final InventoryMessageDialog msgDialog = new InventoryMessageDialog(mContext);
								msgDialog.setCancelable(false);
								msgDialog.mStrTitle = Str.Warning[Global.LANG];
								msgDialog.mStrCustomerName = uInvenDInfo.strItemNo + "\n" + uInvenDInfo.strDescription;
								msgDialog.mStrMessage = Str.This_adjustment_is_for_more_inventory[Global.LANG];
								msgDialog.mStrLeft = Str.Return[Global.LANG];
								msgDialog.setOnDismissListener(dialog -> {
									if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {

									}
								});
								msgDialog.show();
							}
						}
						else
						{
							doProcessAdd(strQty, strCase);
						}
					}
				}
				break;
				case 3: case 4:
				{
					if(Utils.getProdLoconSCWType(mContext, mProdDetlList.get(mProdDetlIdx).strItemNo) == Global.WEIGHT_ITEM)
					{
						float dQuantity = Utils.parseFloatFromString(strQty);
						double dPresold = getPresoldValueD(uInvenDInfo.strItemNo);
						if(uInvenDInfo.dStock + uInvenDInfo.dAdjQty - uInvenDInfo.dDumps - dQuantity - dPresold < 0)
						{
							if(nInvenCheck == 3)
							{
								final InventoryMessageDialog msgDialog = new InventoryMessageDialog(mContext);
								msgDialog.setCancelable(false);
								msgDialog.mStrTitle = Str.Warning[Global.LANG];
								msgDialog.mStrCustomerName = uInvenDInfo.strItemNo + "\n" + uInvenDInfo.strDescription;
								msgDialog.mStrMessage = Str.This_adjustment_is_for_more_unreserved_inventory[Global.LANG];
								msgDialog.mStrLeft = Str.Return[Global.LANG];
								msgDialog.mStrRight = Str.Proceed[Global.LANG];
								msgDialog.setOnDismissListener(dialog -> {
									if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {

									} else if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
										doProcessAdd(strQty, strCase);
									}
								});
								msgDialog.show();
							}
							else {
								final InventoryMessageDialog msgDialog = new InventoryMessageDialog(mContext);
								msgDialog.setCancelable(false);
								msgDialog.mStrTitle = Str.Warning[Global.LANG];
								msgDialog.mStrCustomerName = uInvenDInfo.strItemNo + "\n" + uInvenDInfo.strDescription;
								msgDialog.mStrMessage = Str.This_adjustment_is_for_more_unreserved_inventory[Global.LANG];
								msgDialog.mStrLeft = Str.Return[Global.LANG];
								msgDialog.setOnDismissListener(dialog -> {
									if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {

									}
								});
								msgDialog.show();
							}
						}
						else
						{
							doProcessAdd(strQty, strCase);
						}
					}
					else
					{
						int nQuantity = Utils.parseIntFromString(strQty);
						int nPresold = getPresoldValue(uInvenDInfo.strItemNo);
						if(uInvenDInfo.nStock + uInvenDInfo.nAdjQty - uInvenDInfo.nDumps - nQuantity - nPresold < 0)
						{
							if(nInvenCheck == 3)
							{
								final InventoryMessageDialog msgDialog = new InventoryMessageDialog(mContext);
								msgDialog.setCancelable(false);
								msgDialog.mStrTitle = Str.Warning[Global.LANG];
								msgDialog.mStrCustomerName = uInvenDInfo.strItemNo + "\n" + uInvenDInfo.strDescription;
								msgDialog.mStrMessage = Str.This_adjustment_is_for_more_unreserved_inventory[Global.LANG];
								msgDialog.mStrLeft = Str.Return[Global.LANG];
								msgDialog.mStrRight = Str.Proceed[Global.LANG];
								msgDialog.setOnDismissListener(dialog -> {
									if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {

									} else if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
										doProcessAdd(strQty, strCase);
									}
								});
								msgDialog.show();
							}
							else {
								final InventoryMessageDialog msgDialog = new InventoryMessageDialog(mContext);
								msgDialog.setCancelable(false);
								msgDialog.mStrTitle = Str.Warning[Global.LANG];
								msgDialog.mStrCustomerName = uInvenDInfo.strItemNo + "\n" + uInvenDInfo.strDescription;
								msgDialog.mStrMessage = Str.This_adjustment_is_for_more_unreserved_inventory[Global.LANG];
								msgDialog.mStrLeft = Str.Return[Global.LANG];
								msgDialog.setOnDismissListener(dialog -> {
									if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {

									}
								});
								msgDialog.show();
							}
						}
						else
						{
							doProcessAdd(strQty, strCase);
						}
					}
				}
					break;
				default:
					doProcessAdd(strQty, strCase);
			}
		}
		else
		{
			doProcessAdd(strQty, strCase);
		}
	}

	private void doProcessAdd(String strQty, String strCase)
	{
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

				if( strCase.length() == 0 )
				{
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
			double dQuantity = Utils.parseDoubleFromString(strQty);
			if (mTransferType == 1 || mTransferType == 2) {
				dQuantity *= -1;
			}
			if(nIdx == -1)
			{
				UInvenDInfo info = new UInvenDInfo();
				info.strItemNo = productInfo.strItemNo;
				if (mInventoryType == Global.INVENTORY_RETURNS) {
					info.dDumps = dQuantity;
					info.dSpoils = dQuantity;
				} else {
					info.dStock = Utils.parseDoubleFromString(strQty);
					info.nSCWType = Global.INVENTORY_TRUCK;
					if (mTransferType == 0) {
						info.dAdjQty = dQuantity;
					} else {
						info.dAdjQty = dQuantity * -1;
					}
				}
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
				if (mInventoryType == Global.INVENTORY_RETURNS) {
					mUInvenDList.get(nIdx).dDumps += dQuantity;
					mUInvenDList.get(nIdx).dSpoils += dQuantity;
				} else {

					if (mTransferType == 0) {
						mUInvenDList.get(nIdx).dStock += (dQuantity - mUInvenDList.get(nIdx).dAdjQty);
						mUInvenDList.get(nIdx).dAdjQty = dQuantity;

					} else {
						mUInvenDList.get(nIdx).dStock -= (-1 * dQuantity - mUInvenDList.get(nIdx).dAdjQty);
						mUInvenDList.get(nIdx).dAdjQty = dQuantity * -1;
					}
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
				nQuantity = Utils.parseIntFromString(strQty) + Utils.parseIntFromString(strCase) * nCase;
			}
			else
			{
				nQuantity = Utils.parseIntFromString(strQty);
			}
			if (mTransferType == 1 || mTransferType == 2) {
				nQuantity *= -1;
			}
			if (nIdx == -1) {
				if (nQuantity == 0) {
					Utils.printDebug(mContext, Str.Please_input_the_Quantity[Global.LANG]);
					showKeyboard((EditText) mRootView.findViewById(R.id.editQty), true);
					return;
				} else {
					UInvenDInfo info = new UInvenDInfo();
					info.strItemNo = productInfo.strItemNo;
					if (mInventoryType == Global.INVENTORY_RETURNS) {
						info.nDumps = nQuantity;
						info.nSpoils = nQuantity;
					} else {
						info.nStock = nQuantity;
						info.nSCWType = Global.INVENTORY_TRUCK;
						if (mTransferType == 0) {
							info.nAdjQty = nQuantity;
						} else {
							info.nAdjQty = nQuantity * -1;
						}
					}
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
				if (mInventoryType == Global.INVENTORY_RETURNS) {
					mUInvenDList.get(nIdx).nDumps += nQuantity;
					mUInvenDList.get(nIdx).nSpoils += nQuantity;
				} else {

					if (mTransferType == 0) {
						mUInvenDList.get(nIdx).nStock += (nQuantity - mUInvenDList.get(nIdx).nAdjQty);
						mUInvenDList.get(nIdx).nAdjQty = nQuantity;

					} else {
						mUInvenDList.get(nIdx).nStock -= (-1 * nQuantity - mUInvenDList.get(nIdx).nAdjQty);
						mUInvenDList.get(nIdx).nAdjQty = nQuantity * -1;
					}
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
				}, mCurSelInvenDInfo.nStock, strBatchCodes, null);
				inputDialog.show();

			}catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	private void doProcessDone()
	{
		Global.gAdjustinfo.uInvenDList.clear();
		Global.gAdjustinfo.uInvenDList.addAll(mUInvenDList);
		Bundle bundle = new Bundle();
		bundle.putInt(Global.EXTRA_TRANSFER_TYPE, mTransferType);
		bundle.putInt(Global.EXTRA_INVENTORY_TYPE, mInventoryType);
		bundle.putString(Global.EXTRA_TRANSFER_ROUTE, mTransferRoute);
		bundle.putBoolean(Global.EXTRA_TRANSFER_LATER, bLaterTransfer);

		Fragment fragment = new AdjustSummaryFragment();
		fragment.setArguments(bundle);
		mContext.replaceFragment(fragment);
	}

	private void doDone() {

		boolean bValidation = false;
		for(UInvenDInfo invenDInfo : mUInvenDList)
		{
			if(Utils.getProdLoconSCWType(mContext, invenDInfo.strItemNo) == Global.WEIGHT_ITEM)
			{
				if( invenDInfo.dAdjQty > 0 )
				{
					bValidation = true;
					break;
				}
			}
			else
			{
				if( invenDInfo.nAdjQty > 0 )
				{
					bValidation = true;
					break;
				}
			}
		}
		if( bValidation )
		{
			if(mTransferType == 2)
			{
				//Check bluetooth is enabled or not.
				if (!bLaterTransfer && !BluetoothAdapter.getDefaultAdapter().isEnabled()) {
					Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
					startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
				}
				else
				{
					doCheckInventoryAvailable();
				}
			}
			else
			{
				doProcessDone();
			}
		}
		else
		{
			mContext.onBackPressed();
		}
	}

	Map<String, Integer> mPresoldDataMap = new HashMap<>();
	private int getPresoldValue(String strItemNo)
	{
		if( mPresoldDataMap.get(strItemNo) != null )
			return mPresoldDataMap.get(strItemNo);
		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		List<PresoldOrDInfo> presoldOrDInfos = dbAdapter.getUnusedPresoldOrDInfo(strItemNo);
		dbAdapter.close();
		int nStock = 0;
		for(PresoldOrDInfo info : presoldOrDInfos)
		{
			if( info.strItemNo.equals(strItemNo) )
				nStock += info.nOrderQty;
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
	private double getPresoldValueD(String strItemNo)
	{
		if( mPresoldDataMapD.get(strItemNo) != null )
			return mPresoldDataMapD.get(strItemNo);
		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		List<PresoldOrDInfo> presoldOrDInfos = dbAdapter.getUnusedPresoldOrDInfo(strItemNo);
		dbAdapter.close();
		double dStock = 0;
		for(PresoldOrDInfo info : presoldOrDInfos)
		{
			if( info.strItemNo.equals(strItemNo) )
				dStock += info.dOrderQty;
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

	private void doCheckInventoryAvailable()
	{
		int nInvenCheck = Utils.getIntSetting(mContext, Global.PREF_INVENCKECK);
		if(nInvenCheck == 0)
		{
			doDiscoveryBluetoothDevice();
		}
		else if(nInvenCheck == 1 || nInvenCheck == 2)
		{
			for(final UInvenDInfo info : mUInvenDList)
			{
				if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
				{
					if( info.dAdjQty > 0 && info.dStock < 0)
					{
						final double dStock = info.dAdjQty + info.dStock;
						if( nInvenCheck == 1 )
						{
							new Handler(Looper.getMainLooper()).post(new Runnable() {
								@Override
								public void run() {
									try {
										final InventoryMessageDialog msgDialog = new InventoryMessageDialog(mContext);
										msgDialog.setCancelable(false);
										String strCaption = String.format(Locale.ENGLISH, "%.2f.",dStock);
										msgDialog.mStrTitle = Str.Warning[Global.LANG];
										msgDialog.mStrCustomerName = info.strItemNo + "\n" + info.strDescription;
										msgDialog.mStrMessage = String.format(Locale.ENGLISH, Str.The_quantity_you_have_entered_exceeds_the_available_stock_of[Global.LANG], strCaption);
										msgDialog.mStrSubMessage = Str.Ignore_the_oversell[Global.LANG];
										msgDialog.mStrLeft = Str.No[Global.LANG];
										msgDialog.mStrRight = Str.Yes[Global.LANG];
										msgDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
											@Override
											public void onDismiss(DialogInterface dialog) {
												if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {

												} else if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
													doDiscoveryBluetoothDevice();
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
							return;
						}
						else if(nInvenCheck == 2)
						{
							new Handler(Looper.getMainLooper()).post(new Runnable() {
								@Override
								public void run() {
									try {
										final InventoryMessageDialog msgDialog = new InventoryMessageDialog(mContext);
										msgDialog.setCancelable(false);
										String strCaption = String.format(Locale.ENGLISH, "%.2f.",dStock);
										msgDialog.mStrTitle = Str.Warning[Global.LANG];
										msgDialog.mStrCustomerName = info.strItemNo + "\n" + info.strDescription;
										msgDialog.mStrMessage = String.format(Locale.ENGLISH, Str.The_quantity_you_have_entered_exceeds_the_available_stock_of[Global.LANG], strCaption);
										msgDialog.mStrSubMessage = Str.Reenter_the_quantity_to_continue[Global.LANG];
										msgDialog.mStrMiddle = Str.Return[Global.LANG];
										msgDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
											@Override
											public void onDismiss(DialogInterface dialog) {
												if (msgDialog.mReturnCode == Global.DLG_RETURN_MIDDLE) {
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
							return;
						}
					}
				}
				else
				{
					if( info.nAdjQty > 0 && info.nStock < 0)
					{
						final int nStock = info.nAdjQty + info.nStock;
						final int nCase = getCaseValue(info.strItemNo);
						if( nInvenCheck == 1 )
						{
							new Handler(Looper.getMainLooper()).post(new Runnable() {
								@Override
								public void run() {
									try {
										final InventoryMessageDialog msgDialog = new InventoryMessageDialog(mContext);
										msgDialog.setCancelable(false);
										String strCaption = "";
										if(bShowCase)
										{
											if( Global.gCurCustomerInfo.strSalEntryMode.equals("B"))
											{
												if (Utils.isContainerItem(mContext, info.strItemNo)) {
													strCaption = String.format(Locale.ENGLISH, "%d.",nStock);
												}
												else {
													strCaption = String.format(Locale.ENGLISH, "%d / %d.",nStock / nCase, nStock % nCase);
												}
											}
											else if( Global.gCurCustomerInfo.strSalEntryMode.equals("U") || Global.gCurCustomerInfo.strSalEntryMode.equals(""))
											{
												strCaption = String.format(Locale.ENGLISH, "%d.", nStock);
											}
										}
										else
										{
											strCaption = String.format(Locale.ENGLISH, "%d.", nStock);
										}
										msgDialog.mStrTitle = Str.Warning[Global.LANG];
										msgDialog.mStrCustomerName = info.strItemNo + "\n" + info.strDescription;
										msgDialog.mStrMessage = String.format(Locale.ENGLISH, Str.The_quantity_you_have_entered_exceeds_the_available_stock_of[Global.LANG], strCaption);
										msgDialog.mStrSubMessage = Str.Ignore_the_oversell[Global.LANG];
										msgDialog.mStrLeft = Str.No[Global.LANG];
										msgDialog.mStrRight = Str.Yes[Global.LANG];
										msgDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
											@Override
											public void onDismiss(DialogInterface dialog) {
												if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {

												} else if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
													doDiscoveryBluetoothDevice();
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
							return;
						}
						else if(nInvenCheck == 2)
						{
							new Handler(Looper.getMainLooper()).post(new Runnable() {
								@Override
								public void run() {
									try {
										final InventoryMessageDialog msgDialog = new InventoryMessageDialog(mContext);
										msgDialog.setCancelable(false);
										String strCaption = "";
										if(bShowCase)
										{
											if( Global.gCurCustomerInfo.strSalEntryMode.equals("B"))
											{
												if (Utils.isContainerItem(mContext, info.strItemNo)) {
													strCaption = String.format(Locale.ENGLISH, "%d.",nStock);
												}
												else {
													strCaption = String.format(Locale.ENGLISH, "%d / %d.",nStock / nCase, nStock % nCase);
												}
											}
											else if( Global.gCurCustomerInfo.strSalEntryMode.equals("U") || Global.gCurCustomerInfo.strSalEntryMode.equals(""))
											{
												strCaption = String.format(Locale.ENGLISH, "%d.", nStock);
											}
										}
										else
										{
											strCaption = String.format(Locale.ENGLISH, "%d.", nStock);
										}
										msgDialog.mStrTitle = Str.Warning[Global.LANG];
										msgDialog.mStrCustomerName = info.strItemNo + "\n" + info.strDescription;
										msgDialog.mStrMessage = String.format(Locale.ENGLISH, Str.The_quantity_you_have_entered_exceeds_the_available_stock_of[Global.LANG], strCaption);
										msgDialog.mStrSubMessage = Str.Reenter_the_quantity_to_continue[Global.LANG];
										msgDialog.mStrMiddle = Str.Return[Global.LANG];
										msgDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
											@Override
											public void onDismiss(DialogInterface dialog) {
												if (msgDialog.mReturnCode == Global.DLG_RETURN_MIDDLE) {
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
							return;
						}
					}
				}
			}
			doDiscoveryBluetoothDevice();
		}
		else if(nInvenCheck == 3 || nInvenCheck == 4)
		{
			for(final UInvenDInfo info : mUInvenDList)
			{
				if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
				{
					if(info.dAdjQty > 0)
					{
						final double dPresold = getPresoldValueD(info.strItemNo);
						if(info.dStock < dPresold)
						{
							final double dStock = info.dStock + info.dAdjQty;
							if(nInvenCheck == 3)
							{
								try {
									final InventoryMessageDialog msgDialog = new InventoryMessageDialog(mContext);
									msgDialog.setCancelable(false);
									String strStockCaption = String.format(Locale.ENGLISH, "%.2f", dStock);
									String strPresoldCaption = String.format(Locale.ENGLISH, "%.2f", dPresold);
									String strAvailable = String.format(Locale.ENGLISH, "%.2f", (dStock - dPresold));

									msgDialog.mStrCustomerName = info.strItemNo + "\n" + info.strDescription;
									msgDialog.mStrTitle = Str.Warning[Global.LANG];
									msgDialog.mStrMessage = String.format(Locale.ENGLISH, Str.The_quantity_entered_exceeds_the_reserved_inventory_levels[Global.LANG], strPresoldCaption, strAvailable, strStockCaption);
									msgDialog.mStrSubMessage = Str.Ignore_the_oversell[Global.LANG];
									msgDialog.mStrLeft = Str.No[Global.LANG];
									msgDialog.mStrRight = Str.Yes[Global.LANG];
									msgDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
										@Override
										public void onDismiss(DialogInterface dialog) {
											if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
											} else if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
												doDiscoveryBluetoothDevice();
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
								try {
									final InventoryMessageDialog msgDialog = new InventoryMessageDialog(mContext);
									msgDialog.setCancelable(false);
									String strStockCaption = String.format(Locale.ENGLISH, "%.2f", dStock);
									String strPresoldCaption = String.format(Locale.ENGLISH, "%.2f", dPresold);
									String strAvailable = String.format(Locale.ENGLISH, "%.2f", (dStock - dPresold));

									msgDialog.mStrTitle = Str.Warning[Global.LANG];
									msgDialog.mStrCustomerName = info.strItemNo + "\n" + info.strDescription;
									msgDialog.mStrMessage = String.format(Locale.ENGLISH, Str.The_quantity_entered_exceeds_the_reserved_inventory_levels[Global.LANG], strPresoldCaption, strAvailable, strStockCaption);
									msgDialog.mStrSubMessage = Str.Reenter_the_quantity_to_continue[Global.LANG];
									msgDialog.mStrMiddle = Str.Return[Global.LANG];
									msgDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
										@Override
										public void onDismiss(DialogInterface dialog) {
											if (msgDialog.mReturnCode == Global.DLG_RETURN_MIDDLE) {
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
					}
				}
				else
				{
					if(info.nAdjQty > 0)
					{
						final int nPresold = getPresoldValue(info.strItemNo);
						if(info.nStock < nPresold)
						{
							final int nStock = info.nStock + info.nAdjQty;
							if(nInvenCheck == 3)
							{
								try {
									final InventoryMessageDialog msgDialog = new InventoryMessageDialog(mContext);
									msgDialog.setCancelable(false);
									String strStockCaption = "";
									String strPresoldCaption = "";
									String strAvailable = "";
									int nCase = getCaseValue(info.strItemNo);
									if(bShowCase)
									{
										if( Global.gCurCustomerInfo.strSalEntryMode.equals("B"))
										{
											if (Utils.isContainerItem(mContext, info.strItemNo)) {
												strStockCaption = String.format(Locale.ENGLISH, "%d", nStock);
												strPresoldCaption = String.format(Locale.ENGLISH, "%d", nPresold);
												strAvailable = String.format(Locale.ENGLISH, "%d", nStock - nPresold);
											}
											else {
												strStockCaption = String.format(Locale.ENGLISH, "%d / %d", nStock / nCase, nStock % nCase);
												strPresoldCaption = String.format(Locale.ENGLISH, "%d / %d", nPresold / nCase, nPresold % nCase);
												strAvailable = String.format(Locale.ENGLISH, "%d / %d", (nStock - nPresold) / nCase, (nStock - nPresold) % nCase);
											}
										}
										else if( Global.gCurCustomerInfo.strSalEntryMode.equals("U") || Global.gCurCustomerInfo.strSalEntryMode.equals(""))
										{
											strStockCaption = String.format(Locale.ENGLISH, "%d", nStock);
											strPresoldCaption= String.format(Locale.ENGLISH, "%d", nPresold);
											strAvailable= String.format(Locale.ENGLISH, "%d", (nStock - nPresold));
										}
									}
									else
									{
										strStockCaption = String.format(Locale.ENGLISH, "%d", nStock);
										strPresoldCaption= String.format(Locale.ENGLISH, "%d", nPresold);
										strAvailable= String.format(Locale.ENGLISH, "%d", (nStock - nPresold));
									}

									msgDialog.mStrCustomerName = info.strItemNo + "\n" + info.strDescription;
									msgDialog.mStrTitle = Str.Warning[Global.LANG];
									msgDialog.mStrMessage = String.format(Locale.ENGLISH, Str.The_quantity_entered_exceeds_the_reserved_inventory_levels[Global.LANG], strPresoldCaption, strAvailable, strStockCaption);
									msgDialog.mStrSubMessage = Str.Ignore_the_oversell[Global.LANG];
									msgDialog.mStrLeft = Str.No[Global.LANG];
									msgDialog.mStrRight = Str.Yes[Global.LANG];
									msgDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
										@Override
										public void onDismiss(DialogInterface dialog) {
											if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
											} else if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
												doDiscoveryBluetoothDevice();
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
								try {
									final InventoryMessageDialog msgDialog = new InventoryMessageDialog(mContext);
									msgDialog.setCancelable(false);
									String strStockCaption = "";
									String strPresoldCaption = "";
									String strAvailable = "";
									int nCase = getCaseValue(info.strItemNo);
									if(bShowCase)
									{
										if( Global.gCurCustomerInfo.strSalEntryMode.equals("B"))
										{
											if (Utils.isContainerItem(mContext, info.strItemNo)) {
												strStockCaption = String.format(Locale.ENGLISH, "%d", nStock);
												strPresoldCaption = String.format(Locale.ENGLISH, "%d", nPresold);
												strAvailable = String.format(Locale.ENGLISH, "%d", nStock - nPresold);
											}
											else {
												strStockCaption = String.format(Locale.ENGLISH, "%d / %d", nStock / nCase, nStock % nCase);
												strPresoldCaption = String.format(Locale.ENGLISH, "%d / %d", nPresold / nCase, nPresold % nCase);
												strAvailable = String.format(Locale.ENGLISH, "%d / %d", (nStock - nPresold) / nCase, (nStock - nPresold) % nCase);
											}
										}
										else if( Global.gCurCustomerInfo.strSalEntryMode.equals("U") || Global.gCurCustomerInfo.strSalEntryMode.equals(""))
										{
											strStockCaption = String.format(Locale.ENGLISH, "%d", nStock);
											strPresoldCaption= String.format(Locale.ENGLISH, "%d", nPresold);
											strAvailable= String.format(Locale.ENGLISH, "%d", (nStock - nPresold));
										}
									}
									else
									{
										strStockCaption = String.format(Locale.ENGLISH, "%d", nStock);
										strPresoldCaption= String.format(Locale.ENGLISH, "%d", nPresold);
										strAvailable= String.format(Locale.ENGLISH, "%d", (nStock - nPresold));
									}

									msgDialog.mStrTitle = Str.Warning[Global.LANG];
									msgDialog.mStrCustomerName = info.strItemNo + "\n" + info.strDescription;
									msgDialog.mStrMessage = String.format(Locale.ENGLISH, Str.The_quantity_entered_exceeds_the_reserved_inventory_levels[Global.LANG], strPresoldCaption, strAvailable, strStockCaption);
									msgDialog.mStrSubMessage = Str.Reenter_the_quantity_to_continue[Global.LANG];
									msgDialog.mStrMiddle = Str.Return[Global.LANG];
									msgDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
										@Override
										public void onDismiss(DialogInterface dialog) {
											if (msgDialog.mReturnCode == Global.DLG_RETURN_MIDDLE) {
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
					}
				}
			}
			doDiscoveryBluetoothDevice();
		}
	}

	int mSelectedRouteNo = -1;
	private void doDiscoveryBluetoothDevice()
	{
		if(bLaterTransfer)
		{
			DbAdapter dbAdapter = new DbAdapter(mContext);
			dbAdapter.open();
			List<DescTypeInfo> descTypeInfoList = dbAdapter.getDescTypeList("RouteNumber");
			dbAdapter.close();
			String[] strDescArray = new String[descTypeInfoList.size()];
			for(int i = 0; i < descTypeInfoList.size(); i++)
			{
				strDescArray[i] = descTypeInfoList.get(i).strDescription;
			}

			try {
				new AlertDialog.Builder(mContext)
						.setTitle(Str.SelectRouteToCompleteTransfer[Global.LANG])
						.setSingleChoiceItems(new AlertSingleChoiceAdapter(mContext, strDescArray), -1, (dialog, nSelected) -> {
							mSelectedRouteNo = nSelected;
						})
						.setPositiveButton(Str.Ok[Global.LANG], new DialogInterface.OnClickListener()
						{
							public void onClick(DialogInterface dialog, int nSelected)
							{
								doProcessSelectRoute(descTypeInfoList.get(mSelectedRouteNo));
							}
						})
						.setNegativeButton(Str.Cancel[Global.LANG], new DialogInterface.OnClickListener()
						{
							public void onClick(DialogInterface dialog, int nSelected)
							{

							}
						})
						.create()
						.show();
			}catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			try {
				//Show Discovery Bluetooth Dialog
				DiscoveryBluetoothDeviceDialog dialog = new DiscoveryBluetoothDeviceDialog(mContext, new DiscoveryBluetoothDeviceDialog.DiscoveryCallback() {
					@Override
					public void doReturn() {

					}

					@Override
					public void doSelectDevice(BluetoothDevice device) {
						doProcessSelectDevice(device);
					}
				});
				dialog.show();
			}catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	public void doProcessSelectRoute(DescTypeInfo descTypeInfo)
	{
		mTransferRoute = String.valueOf(descTypeInfo.nNumericKey);
		doProcessDone();
	}

	public void doProcessSelectDevice(final BluetoothDevice device)
	{
		showProgressDialog(Str.Connecting[Global.LANG]);
		BluetoothService.getInstance().connect(device);
		BluetoothService.getInstance().setCallback(new BluetoothService.BluetoothServiceCallback() {
			@Override
			public void onConnected(BluetoothSocket socket, BluetoothDevice device) {
				List<InventoryData> dataList = new ArrayList<>();
				for(UInvenDInfo invenDInfo : mUInvenDList)
				{
					if(Utils.getProdLoconSCWType(mContext, invenDInfo.strItemNo) == Global.WEIGHT_ITEM)
					{
						if( invenDInfo.dAdjQty > 0 )
						{
							InventoryData data = new InventoryData();
							data.weight = (float)invenDInfo.dAdjQty;
							data.strItem = invenDInfo.strItemNo;
							dataList.add(data);
						}
					}
					else
					{
						if( invenDInfo.nAdjQty > 0 )
						{
							InventoryData data = new InventoryData();
							data.nQty = invenDInfo.nAdjQty;
							data.strItem = invenDInfo.strItemNo;
							dataList.add(data);
						}
					}
				}

				AdjustInventoryBluetoothData adjustInventoryBluetoothData = new AdjustInventoryBluetoothData();
				adjustInventoryBluetoothData.uInvenDInfoList = dataList;
				adjustInventoryBluetoothData.strUserName = Utils.getStringSetting(mContext, Global.PREF_USER_NAME);
				adjustInventoryBluetoothData.strRouteNumber = Utils.getStringSetting(mContext, Global.PREF_ROUTE_NUMBER);

				BluetoothTransferData transferData = new BluetoothTransferData();
				transferData.nType = 10;
				transferData.strJSONData = adjustInventoryBluetoothData.toJSON();
				BluetoothService.getInstance().write(transferData.toJSON());

				bleProcess = false;
			}

			@Override
			public void onConnectionLost() {
				//mContext.doRegisterBluetoothTransfer();
				if(bleProcess == false)
				{
					hideProgressDialog();
					try {
						final MessageDialog msgDialog = new MessageDialog(mContext);
						msgDialog.setCancelable(false);
						msgDialog.mStrTitle = Str.FAILED[Global.LANG];
						msgDialog.mStrMessage = Str.ConnectionLost[Global.LANG];
						msgDialog.mStrLeft = Str.No[Global.LANG];
						msgDialog.mStrRight = Str.Yes[Global.LANG];
						msgDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
							@Override
							public void onDismiss(DialogInterface dialog) {
								if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
									//Send Message to Cancel
									mContext.doRegisterBluetoothTransfer();
								}
								else if(msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
									BluetoothService.getInstance().stop();
									doDiscoveryBluetoothDevice();
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

			@Override
			public void onConnectionFailed() {
				hideProgressDialog();
				try {
					final MessageDialog msgDialog = new MessageDialog(mContext);
					msgDialog.setCancelable(false);
					msgDialog.mStrTitle = Str.FAILED[Global.LANG];
					msgDialog.mStrMessage = Str.ConnectionFailed[Global.LANG];
					msgDialog.mStrLeft = Str.No[Global.LANG];
					msgDialog.mStrRight = Str.Yes[Global.LANG];
					msgDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
						@Override
						public void onDismiss(DialogInterface dialog) {
							if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
								//Send Message to Cancel
								mContext.doRegisterBluetoothTransfer();
							}
							else if(msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
								BluetoothService.getInstance().stop();
								doDiscoveryBluetoothDevice();
							}
						}
					});
					msgDialog.show();
				}catch (Exception e)
				{
					e.printStackTrace();
				}

			}

			@Override
			public void onReceive(String strText) {
				hideProgressDialog();
				BluetoothTransferData data = BluetoothTransferData.fromJSON(strText);
				if(data != null)
				{
					switch (data.nType)
					{
						case 0 :
							bleProcess = true;
							mContext.doRegisterBluetoothTransfer();
							mTransferRoute = data.strRouteNumber;
							doProcessDone();
							break;
						case 1:
							{
								//Show Message dialog because user has rejected.
								bleProcess = true;
								try {
									final MessageDialog msgDialog = new MessageDialog(mContext);
									msgDialog.setCancelable(false);
									msgDialog.mStrTitle = Str.FAILED[Global.LANG];
									msgDialog.mStrMessage = Str.UserRejectedActivity[Global.LANG];
									msgDialog.mStrLeft = Str.No[Global.LANG];
									msgDialog.mStrRight = Str.Yes[Global.LANG];
									msgDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
										@Override
										public void onDismiss(DialogInterface dialog) {
											if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
												//Send Message to Cancel
												mContext.doRegisterBluetoothTransfer();
											}
											else if(msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
												BluetoothService.getInstance().stop();
												//after 1 secs try again.
												new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
													@Override
													public void run() {
														doProcessSelectDevice(device);
													}
												}, 1000);
											}
										}
									});
									msgDialog.show();
								}catch (Exception e)
								{
									e.printStackTrace();
								}

							}

							break;
						case 2:
							{
								//Show Message dialog because user has rejected.
								bleProcess = true;
								try {
									final MessageDialog msgDialog = new MessageDialog(mContext);
									msgDialog.setCancelable(false);
									msgDialog.mStrTitle = Str.FAILED[Global.LANG];
									msgDialog.mStrMessage = Str.UserHasNotLoggedInYet[Global.LANG];
									msgDialog.mStrLeft = Str.No[Global.LANG];
									msgDialog.mStrRight = Str.Yes[Global.LANG];
									msgDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
										@Override
										public void onDismiss(DialogInterface dialog) {
											if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
												//Send Message to Cancel
												mContext.doRegisterBluetoothTransfer();
											}
											else if(msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
												BluetoothService.getInstance().stop();
												//after 1 secs try again.
												new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
													@Override
													public void run() {
														doProcessSelectDevice(device);
													}
												}, 1000);
											}
										}
									});
									msgDialog.show();
								}catch (Exception e)
								{
									e.printStackTrace();
								}

							}
							break;
						case 3:
							{
								//Show Message dialog because user has rejected.
								bleProcess = true;
								try {
									final MessageDialog msgDialog = new MessageDialog(mContext);
									msgDialog.setCancelable(false);
									msgDialog.mStrTitle = Str.FAILED[Global.LANG];
									msgDialog.mStrMessage = Str.CompleteAdjustmentProcessing[Global.LANG];
									msgDialog.mStrLeft = Str.Return[Global.LANG];
									msgDialog.mStrRight = Str.Retry[Global.LANG];
									msgDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
										@Override
										public void onDismiss(DialogInterface dialog) {
											if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
												//Send Message to Cancel
												mContext.doRegisterBluetoothTransfer();
											}
											else if(msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
												BluetoothService.getInstance().stop();
												//after 1 secs try again.
												new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
													@Override
													public void run() {
														doProcessSelectDevice(device);
													}
												}, 1000);
											}
										}
									});
									msgDialog.show();
								}catch (Exception e)
								{
									e.printStackTrace();
								}

							}
							break;
						case 4:
							{
								bleProcess = true;
								try {
									AdjustInventoryBluetoothData adjustInventoryBluetoothData = AdjustInventoryBluetoothData.fromJSON(data.strJSONData);
									DbAdapter dbAdapter = new DbAdapter(mContext);
									dbAdapter.open();
									Map<String, ProdDetlInfo> prodDetlInfoMap = new HashMap<>();
									for(InventoryData itemData : adjustInventoryBluetoothData.uInvenDInfoList)
									{
										List<ProdDetlInfo> prodDetlInfoList = dbAdapter.getProductList(itemData.strItem);
										if(prodDetlInfoList != null && prodDetlInfoList.size() > 0)
										{
											prodDetlInfoMap.put(itemData.strItem, prodDetlInfoList.get(0));
										}
									}
									dbAdapter.close();

									InventoryNotApprovalDialog dialog = new InventoryNotApprovalDialog(mContext, adjustInventoryBluetoothData, prodDetlInfoMap, new InventoryNotApprovalDialog.Callback() {
										@Override
										public void doReturn() {
											mContext.doRegisterBluetoothTransfer();
										}

										@Override
										public void doTryOtherDevice() {
											BluetoothService.getInstance().stop();
											doDiscoveryBluetoothDevice();
										}
									});
									dialog.show();
								}catch (Exception e)
								{
									e.printStackTrace();
								}

							}
							break;
						case 5:
							{
								//Show Message dialog because user has rejected.
								bleProcess = true;
								try {
									final MessageDialog msgDialog = new MessageDialog(mContext);
									msgDialog.setCancelable(false);
									msgDialog.mStrTitle = Str.FAILED[Global.LANG];
									msgDialog.mStrMessage = Str.NotReadyAcceptTransfer[Global.LANG];
									msgDialog.mStrLeft = Str.Return[Global.LANG];
									msgDialog.mStrRight = Str.Retry[Global.LANG];
									msgDialog.setOnDismissListener(dialog -> {
										if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
											//Send Message to Cancel
											mContext.doRegisterBluetoothTransfer();
										}
										else if(msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
											BluetoothService.getInstance().stop();
											//after 1 secs try again.
											new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
												@Override
												public void run() {
													doProcessSelectDevice(device);
												}
											}, 1000);
										}
									});
									msgDialog.show();
								}catch (Exception e)
								{
									e.printStackTrace();
								}

							}
							break;
					}
				}
			}

			@Override
			public void onSend(String strText) {

			}
		});
	}
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK)
		{
			doCheckInventoryAvailable();
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
			((EditText) mRootView.findViewById(R.id.editQty)).setText(strQty);
			mRootView.findViewById(R.id.editQty).setTag(null);
			mRootView.findViewById(R.id.editQty).requestFocus();
			((EditText) mRootView.findViewById(R.id.editQty)).setSelection(strQty.length());
		}
		else
		{
			if(bShowCase)
			{
				int nCase = getCaseValue(info.strItemNo);
				int nValue = info.nAdjQty;

				String strQty = String.format(Locale.ENGLISH, "%d", nValue % nCase);
				String strCase = String.format(Locale.ENGLISH, "%d", nValue / nCase);
				((EditText) mRootView.findViewById(R.id.editQty)).setText(strQty);
				((EditText) mRootView.findViewById(R.id.editCase)).setText(strCase);

				mRootView.findViewById(R.id.editCase).requestFocus();
				((EditText) mRootView.findViewById(R.id.editCase)).setSelection(strCase.length());
			}
			else
			{
				int nValue = info.nAdjQty;

				String strQty = String.format(Locale.ENGLISH, "%d", nValue);
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
			public TextView txtItemNo, txtShortDesc, txtPickLocationCode, txtLoaded, txtStock;
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
				view = mContext.getLayoutInflater().inflate(R.layout.row_adjust_inventory, parent, false);
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
				holder.txtLoaded.setText(String.format(Locale.ENGLISH, "%.2f", info.dStock));

				double dAddValue = info.dAdjQty;
				holder.txtStock.setText(String.format(Locale.ENGLISH, "%.2f", dAddValue));
			}
			else
			{
				int nAddValue = info.nAdjQty;
				if(bShowCase)
				{
					if (Utils.isContainerItem(mContext, info.strItemNo)) {
						holder.txtLoaded.setText(String.format(Locale.ENGLISH, "%d", info.nStock));
						holder.txtStock.setText(String.format(Locale.ENGLISH, "%d", nAddValue));
					}
					else {
						int nCase = getCaseValue(info.strItemNo);
						holder.txtLoaded.setText(String.format(Locale.ENGLISH, "%d/%d", info.nStock / nCase, info.nStock % nCase));
						holder.txtStock.setText(String.format(Locale.ENGLISH, "%d/%d", nAddValue / nCase, nAddValue % nCase));
					}
				}
				else
				{
					holder.txtLoaded.setText(String.format(Locale.ENGLISH, "%d", info.nStock));
					holder.txtStock.setText(String.format(Locale.ENGLISH, "%d", nAddValue));
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

			holder.txtShortDesc.setText(info.strShortDesc);
			holder.txtPickLocationCode.setText(info.strPickLocationCode);

			int nCase = getCaseValue(info.strItemNo);
//			if(Global.gCurCustomerInfo.strSalEntryMode.equals("B"))
			if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
			{
				double dAddValue = 0;
				double dLoaded = 0;
				dAddValue = info.dAdjQty;
				if( mTransferType == 0 )
				{
					dLoaded = info.dStock - dAddValue;
				}else {
					dLoaded = info.dStock + dAddValue;
				}
				holder.txtLoaded.setText(String.format(Locale.ENGLISH, "%.2f", dLoaded));
				holder.txtStock.setText(String.format(Locale.ENGLISH, "%.2f", dAddValue));
			}
			else
			{
				int nAddValue = 0;
				int nLoaded = 0;
				nAddValue = info.nAdjQty;
				if( mTransferType == 0 )
				{
					nLoaded = info.nStock - nAddValue;
				}else {
					nLoaded = info.nStock + nAddValue;
				}
				if(bShowCase)
				{
					if (Utils.isContainerItem(mContext, info.strItemNo)) {
						holder.txtLoaded.setText(String.format(Locale.ENGLISH, "%d", nLoaded));
						holder.txtStock.setText(String.format(Locale.ENGLISH, "%d", nAddValue));
					}
					else {
						holder.txtLoaded.setText(String.format(Locale.ENGLISH, "%d/%d", nLoaded / nCase, nLoaded % nCase));
						holder.txtStock.setText(String.format(Locale.ENGLISH, "%d/%d", nAddValue / nCase, nAddValue % nCase));
					}
				}
				else
				{
					holder.txtLoaded.setText(String.format(Locale.ENGLISH, "%d", nLoaded));
					holder.txtStock.setText(String.format(Locale.ENGLISH, "%d", nAddValue));
				}

			}
/*			else if(Global.gCurCustomerInfo.strSalEntryMode.equals("") || Global.gCurCustomerInfo.strSalEntryMode.equals("U"))
			{
				holder.txtLoaded.setText(String.format(Locale.ENGLISH, "%d", info.nLoaded ));
				holder.txtStock.setText(String.format(Locale.ENGLISH, "%d", info.nStock));
			}*/

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
		int nCase = 0;
		if( mCaseMap.get(strItemNo) != null )
			nCase = mCaseMap.get(strItemNo);

		if( nCase < 1 ) nCase = 1;
		return nCase;
	}

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