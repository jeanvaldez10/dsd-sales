package dsd.dsdvansales.fragment;

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
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import dsd.dsdvansales.R;
import dsd.dsdvansales.activities.BarcodeScannerActivity;
import dsd.dsdvansales.db.DbAdapter;
import dsd.dsdvansales.dialogs.InventoryMessageDialog;
import dsd.dsdvansales.dialogs.MessageDialog;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.model.CustConBalInfo;
import dsd.dsdvansales.model.CustomerOrderInfo;
import dsd.dsdvansales.model.OrderLineInfo;
import dsd.dsdvansales.model.PresoldOrDInfo;
import dsd.dsdvansales.model.PricingInfo;
import dsd.dsdvansales.model.ProdDetlInfo;
import dsd.dsdvansales.model.ProdInfo;
import dsd.dsdvansales.model.ProdStruInfo;
import dsd.dsdvansales.model.UInvenDInfo;
import dsd.dsdvansales.model.UStockDInfo;

public class AdjustConsignmentInventoryFragment extends BaseFragment implements View.OnClickListener {

	public int nTransferType = 0;

	private TextView mTxtNoData = null;
	private ListView mListView = null;
	private ListAdapter mListAdapter = null;
	private List<UStockDInfo> mUStockDList = new ArrayList<>();
	private boolean bLoaded = false;

	private boolean filter = false;

	private Map<String, ProdDetlInfo> mProdDetlItemMap = new HashMap<>();
	private Map<String, ProdDetlInfo> mProdDetlUPCMap = new HashMap<>();
	private Map<String, PricingInfo> mPriceInfoMap = new HashMap<>();
	private List<CustomerOrderInfo> latestOrderInfo = new ArrayList<>();

	private Map<String, ProdInfo> mProdInfoMap = new HashMap<>();
	public boolean mExist = false;

	private List<ProdDetlInfo> mProdDetlList = new ArrayList<>();
	private int mProdDetlIdx = -1;

	ProdDetlInfo mSelProductInfo = null;
	Map<String, UInvenDInfo> uInvenDInfoMap = new HashMap<>();

	private int m_nInventoryValidationType = 0;
	private boolean bModify = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle bundle = getArguments();
		if(bundle != null)
		{
			nTransferType = bundle.getInt(Global.EXTRA_TRANSFER_TYPE);
		}

	}

	public void doProcessBack()
	{
		mRootView.findViewById(R.id.btnBack).performClick();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootView = inflater.inflate(R.layout.fragment_adjust_consignment_inventory, container, false);
		commonInit();
		return mRootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		Global.gFragIdx = Global.FRAG_ADJUST_CONSIGNMENT_INVENTORY;
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void initView() {

		m_nInventoryValidationType = Utils.getIntSetting(mContext, Global.PREF_INVENCKECK);
		mRootView.findViewById(R.id.btnAdd).setOnClickListener(this);

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
						doAdd();
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
					doAdd();
					return true;
				}
				return false;
			}
		});

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

	private Map<String, String> mShortDescMap = new HashMap<>();
	private String getShortDesc(String strItemNo)
	{
		if( mShortDescMap.get(strItemNo) != null )
			return mShortDescMap.get(strItemNo);
		DbAdapter adapter = new DbAdapter(mContext);
		adapter.open();
		List<ProdDetlInfo> prodDetInfos =  adapter.getProductList(strItemNo);
		adapter.close();

		String strDesc = "";
		if( prodDetInfos != null && prodDetInfos.size() > 0)
		{
			strDesc = prodDetInfos.get(0).strShortDesc;
		}
		else
		{
			strDesc = "";
		}
		mShortDescMap.put(strItemNo, strDesc);
		return strDesc;
	}

	@Override
	public void loadData() {
		if(!bLoaded)
		{
			mProdDetlItemMap.clear();
			mProdDetlUPCMap.clear();
			DbAdapter dbAdapter = new DbAdapter(mContext);
			dbAdapter.open();
			latestOrderInfo.clear();
			latestOrderInfo.addAll(dbAdapter.getAllCustomerLatestOrderList());
			mProdDetlList.clear();
			mProdDetlList = dbAdapter.getProductList(mProdDetlItemMap, mProdDetlUPCMap);
//			if(Utils.getStringSetting(mContext, Global.PREF_ITEM_CODE_DISPLAY_SOURCE).equals("PRODINFO"))
//			{
//				mProdInfoMap = dbAdapter.getProdInfoMap("ItemCode");
//			}
			mPriceInfoMap = dbAdapter.getPricingInfo(Global.gCurCustomerInfo.nCustNo);
			List<CustConBalInfo> custConBalInfoList = Global.gCustConBalList;
			dbAdapter.close();

			mUStockDList.clear();
			for(CustConBalInfo info : custConBalInfoList)
			{
				UStockDInfo uStockDInfo = new UStockDInfo();
				uStockDInfo.nBeginQty = info.nQty;
				uStockDInfo.dBeginQty = info.fWeight;
				uStockDInfo.nActualQty = uStockDInfo.nBeginQty;
				uStockDInfo.dActualQty = uStockDInfo.nBeginQty;

				uStockDInfo.strItemNo = info.strItemNo;
				mUStockDList.add(uStockDInfo);
			}

			uInvenDInfoMap.clear();
			for(UInvenDInfo uInvenDInfo : Global.gCurInventoryList)
			{
				uInvenDInfoMap.put(uInvenDInfo.strItemNo, uInvenDInfo);
			}
		}
		bLoaded = true;
		refreshPage();
	}

	@Override
	public void refreshPage() {
		if (mUStockDList.size() > 0) {
			mListView.setVisibility(View.VISIBLE);
			mTxtNoData.setVisibility(View.GONE);
			mListAdapter.setListData(mUStockDList);
			mListAdapter.notifyDataSetChanged();
		} else {
			mListView.setVisibility(View.GONE);
			mTxtNoData.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void refreshLanguage() {
		((TextView) mRootView.findViewById(R.id.lblTitle)).setText(Str.AdjustShelfStock[Global.LANG].toUpperCase());
		((TextView) mRootView.findViewById(R.id.txtCustomerName)).setText(Global.gCurCustomerInfo.strName);
		((TextView) mRootView.findViewById(R.id.txtDescription)).setText("");
		((Button) mRootView.findViewById(R.id.btnCompleted)).setText(Str.Completed[Global.LANG]);
		if(nTransferType == 0)
		{
			((Button) mRootView.findViewById(R.id.btnAdd)).setText(Str.Add[Global.LANG]);
			mRootView.findViewById(R.id.btnAdd).setBackgroundResource(R.drawable.btn_orange_large);
		}
		else
		{
			((Button) mRootView.findViewById(R.id.btnAdd)).setText(Str.Minus[Global.LANG]);
			mRootView.findViewById(R.id.btnAdd).setBackgroundResource(R.drawable.btn_red_large);
		}

		mTxtNoData.setText(Str.No_data_has_been_found[Global.LANG]);
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
		if (strAction.equals(Global.ACTION_SELECT_PRODUCT)) {
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
						doAdd();
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
			for (UStockDInfo info : mUStockDList) {
				if (info.strItemNo.equals(strItemNo) == true) {
					if(Utils.getProdLoconSCWType(mContext, strItemNo) == Global.WEIGHT_ITEM)
					{
						if(nTransferType == 0)
						{
							if(nType == 4)
							{
								strQty = String.format(Locale.ENGLISH, "%.2f", info.dActualQty - info.dBeginQty + 1.0f);
							}
							else {
								strQty = String.format(Locale.ENGLISH, "%.2f", info.dActualQty - info.dBeginQty);
							}

						}
						else
						{
							if(nType == 4)
							{
								strQty = String.format(Locale.ENGLISH, "%.2f", info.dBeginQty - info.dActualQty + 1.0f);
							}
							else
							{
								strQty = String.format(Locale.ENGLISH, "%.2f", info.dBeginQty - info.dActualQty);
							}
						}
					}
					else
					{
						if(nTransferType == 0)
						{
							if(nType == 4)
							{
								strQty = String.valueOf(info.nActualQty - info.nBeginQty + 1);
							}
							else
							{
								strQty = String.valueOf(info.nActualQty - info.nBeginQty);
							}
						}
						else
						{
							if(nType == 4)
							{
								strQty = String.valueOf(info.nBeginQty - info.nActualQty + 1);
							}
							else
							{
								strQty = String.valueOf(info.nBeginQty - info.nActualQty);
							}
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

	private void doAdd() {
		bModify = true;
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
		for (int i = 0; i < mUStockDList.size(); i++) {
			if (mUStockDList.get(i).strItemNo.equals(mSelProductInfo.strItemNo) == true) {
				nIdx = i;
				break;
			}
		}
		UInvenDInfo uInvenDInfo = uInvenDInfoMap.get(mSelProductInfo.strItemNo);
		if(Utils.getProdLoconSCWType(mContext, mSelProductInfo.strItemNo) == Global.WEIGHT_ITEM)
		{
			final double dQuantity = Utils.parseDoubleFromString(strQty);
			if(nIdx == -1)
			{
				//Process Error Processing
				if(nTransferType == 0)
				{
					if(m_nInventoryValidationType == 1 || m_nInventoryValidationType == 2)
					{
						if(uInvenDInfo == null || uInvenDInfo.dStock < dQuantity)
						{
							double dStock = 0;
							if(uInvenDInfo != null)
							{
								dStock = uInvenDInfo.dStock;
							}
							try {
								final InventoryMessageDialog msgDialog = new InventoryMessageDialog(mContext);
								msgDialog.setCancelable(false);
								String strCaption = "";
								strCaption = String.format(Locale.ENGLISH, "%.2f.", dStock);
								if(m_nInventoryValidationType == 1)
								{
									msgDialog.mStrTitle = Str.Warning[Global.LANG];
									msgDialog.mStrCustomerName = mSelProductInfo.strItemNo + "\n" + mSelProductInfo.strDescription;
									msgDialog.mStrMessage = String.format(Locale.ENGLISH, Str.The_quantity_you_have_entered_exceeds_the_available_stock_of[Global.LANG], strCaption);
									msgDialog.mStrSubMessage = Str.Ignore_the_oversell[Global.LANG];
									msgDialog.mStrLeft = Str.No[Global.LANG];
									msgDialog.mStrRight = Str.Yes[Global.LANG];
								}
								else
								{
									msgDialog.mStrTitle = Str.Warning[Global.LANG];
									msgDialog.mStrCustomerName = mSelProductInfo.strItemNo + "\n" + mSelProductInfo.strDescription;
									msgDialog.mStrMessage = String.format(Locale.ENGLISH, Str.The_quantity_you_have_entered_exceeds_the_available_stock_of[Global.LANG], strCaption);
									msgDialog.mStrSubMessage = Str.Reenter_the_quantity_to_continue[Global.LANG];
									msgDialog.mStrMiddle = Str.Return[Global.LANG];
								}
								msgDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
									@Override
									public void onDismiss(DialogInterface dialogInterface) {
										if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT || msgDialog.mReturnCode == Global.DLG_RETURN_MIDDLE)
										{

										}
										else
										{
											UStockDInfo info = new UStockDInfo();
											info.strItemNo = mSelProductInfo.strItemNo;
											info.dActualQty = info.dBeginQty + dQuantity;
											mUStockDList.add(info);

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
					}
					else if(m_nInventoryValidationType == 2 || m_nInventoryValidationType == 4)
					{
						final double dPresold = getPresoldValueD(mSelProductInfo.strItemNo);

						if(uInvenDInfo == null || uInvenDInfo.dStock - dPresold < dQuantity)
						{
							double dStock = 0;
							double dReserved = 0;
							if(uInvenDInfo != null)
							{
								dStock = uInvenDInfo.dStock;
							}
							try {
								final InventoryMessageDialog msgDialog = new InventoryMessageDialog(mContext);
								msgDialog.setCancelable(false);
								String strCaption = "", strCaption1, strCaption2;
								strCaption = String.format(Locale.ENGLISH, "%.2f", dStock);
								strCaption1 = String.format(Locale.ENGLISH, "%.2f", dPresold);
								strCaption2 = String.format(Locale.ENGLISH, "%.2f", dStock - dPresold);
								if(m_nInventoryValidationType == 3)
								{
									msgDialog.mStrTitle = Str.Warning[Global.LANG];
									msgDialog.mStrCustomerName = mSelProductInfo.strItemNo + "\n" + mSelProductInfo.strDescription;
									msgDialog.mStrMessage = String.format(Locale.ENGLISH, Str.The_quantity_entered_exceeds_the_reserved_inventory_levels[Global.LANG], strCaption1, strCaption2, strCaption);
									msgDialog.mStrSubMessage = Str.Ignore_the_oversell[Global.LANG];
									msgDialog.mStrLeft = Str.No[Global.LANG];
									msgDialog.mStrRight = Str.Yes[Global.LANG];
								}
								else
								{
									msgDialog.mStrCustomerName = mSelProductInfo.strItemNo + "\n" + mSelProductInfo.strDescription;
									msgDialog.mStrTitle = Str.Warning[Global.LANG];
									msgDialog.mStrMessage = String.format(Locale.ENGLISH, Str.The_quantity_entered_exceeds_the_reserved_inventory_levels[Global.LANG], strCaption1, strCaption2, strCaption);
									msgDialog.mStrSubMessage = Str.Reenter_the_quantity_to_continue[Global.LANG];
									msgDialog.mStrMiddle = Str.Return[Global.LANG];
								}
								msgDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
									@Override
									public void onDismiss(DialogInterface dialogInterface) {
										if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT || msgDialog.mReturnCode == Global.DLG_RETURN_MIDDLE)
										{

										}
										else
										{
											UStockDInfo info = new UStockDInfo();
											info.strItemNo = mSelProductInfo.strItemNo;
											info.dActualQty = info.dBeginQty + dQuantity;
											mUStockDList.add(info);

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
					}
				}
				else
				{
					if(dQuantity > 0)
					{
						try {
							final MessageDialog msgDialog = new MessageDialog(mContext);
							msgDialog.setCancelable(false);
							msgDialog.mStrMessage = Str.There_is_not_enough_stock_to_remove_from_consignment[Global.LANG];
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
				}
				UStockDInfo info = new UStockDInfo();
				info.strItemNo = mSelProductInfo.strItemNo;
				if(nTransferType == 0)
				{
					info.dActualQty = info.dBeginQty + dQuantity;
				}
				else
				{
					info.dActualQty = info.dBeginQty - dQuantity;
				}
				mUStockDList.add(info);
			}
			else
			{
				if (dQuantity == 0) {
					/*Utils.printDebug(mContext, Str.Please_input_the_Quantity[Global.LANG]);
					showKeyboard((EditText) mRootView.findViewById(R.id.editQty), true);
					return;*/
					mUStockDList.get(nIdx).dActualQty = mUStockDList.get(nIdx).dBeginQty - dQuantity;
					if(mUStockDList.get(nIdx).dBeginQty == 0)
					{
						mUStockDList.remove(nIdx);
					}

					((EditText) mRootView.findViewById(R.id.editItemNo)).setText("");
					((EditText) mRootView.findViewById(R.id.editQty)).setText("");

					focusAndHideKeyboard((EditText) mRootView.findViewById(R.id.editItemNo));

					refreshPage();
					return;
				}

				if(nTransferType == 0)
				{
					if(m_nInventoryValidationType == 1 || m_nInventoryValidationType == 2)
					{
						if(uInvenDInfo == null || uInvenDInfo.dStock < dQuantity)
						{
							double dStock = 0;
							if(uInvenDInfo != null)
							{
								dStock = uInvenDInfo.dStock;
							}
							try {
								final InventoryMessageDialog msgDialog = new InventoryMessageDialog(mContext);
								msgDialog.setCancelable(false);
								String strCaption = "";
								strCaption = String.format(Locale.ENGLISH, "%.2f.", dStock);
								if(m_nInventoryValidationType == 1)
								{
									msgDialog.mStrTitle = Str.Warning[Global.LANG];
									msgDialog.mStrCustomerName = mSelProductInfo.strItemNo + "\n" + mSelProductInfo.strDescription;
									msgDialog.mStrMessage = String.format(Locale.ENGLISH, Str.The_quantity_you_have_entered_exceeds_the_available_stock_of[Global.LANG], strCaption);
									msgDialog.mStrSubMessage = Str.Ignore_the_oversell[Global.LANG];
									msgDialog.mStrLeft = Str.No[Global.LANG];
									msgDialog.mStrRight = Str.Yes[Global.LANG];
								}
								else
								{
									msgDialog.mStrTitle = Str.Warning[Global.LANG];
									msgDialog.mStrCustomerName = mSelProductInfo.strItemNo + "\n" + mSelProductInfo.strDescription;
									msgDialog.mStrMessage = String.format(Locale.ENGLISH, Str.The_quantity_you_have_entered_exceeds_the_available_stock_of[Global.LANG], strCaption);
									msgDialog.mStrSubMessage = Str.Reenter_the_quantity_to_continue[Global.LANG];
									msgDialog.mStrMiddle = Str.Return[Global.LANG];
								}
								final int finalNIdx = nIdx;
								msgDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
									@Override
									public void onDismiss(DialogInterface dialogInterface) {
										if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT || msgDialog.mReturnCode == Global.DLG_RETURN_MIDDLE)
										{

										}
										else
										{
											mUStockDList.get(finalNIdx).dActualQty = mUStockDList.get(finalNIdx).dBeginQty - dQuantity;
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
					else if(m_nInventoryValidationType == 2 || m_nInventoryValidationType == 4)
					{
						final double dPresold = getPresoldValueD(mSelProductInfo.strItemNo);

						if(uInvenDInfo == null || uInvenDInfo.dStock - dPresold < dQuantity)
						{
							double dStock = 0;
							double dReserved = 0;
							if(uInvenDInfo != null)
							{
								dStock = uInvenDInfo.dStock;
							}
							try {
								final InventoryMessageDialog msgDialog = new InventoryMessageDialog(mContext);
								msgDialog.setCancelable(false);
								String strCaption = "", strCaption1, strCaption2;
								strCaption = String.format(Locale.ENGLISH, "%.2f", dStock);
								strCaption1 = String.format(Locale.ENGLISH, "%.2f", dPresold);
								strCaption2 = String.format(Locale.ENGLISH, "%.2f", dStock - dPresold);
								if(m_nInventoryValidationType == 3)
								{
									msgDialog.mStrTitle = Str.Warning[Global.LANG];
									msgDialog.mStrCustomerName = mSelProductInfo.strItemNo + "\n" + mSelProductInfo.strDescription;
									msgDialog.mStrMessage = String.format(Locale.ENGLISH, Str.The_quantity_entered_exceeds_the_reserved_inventory_levels[Global.LANG], strCaption1, strCaption2, strCaption);
									msgDialog.mStrSubMessage = Str.Ignore_the_oversell[Global.LANG];
									msgDialog.mStrLeft = Str.No[Global.LANG];
									msgDialog.mStrRight = Str.Yes[Global.LANG];
								}
								else
								{
									msgDialog.mStrCustomerName = mSelProductInfo.strItemNo + "\n" + mSelProductInfo.strDescription;
									msgDialog.mStrTitle = Str.Warning[Global.LANG];
									msgDialog.mStrMessage = String.format(Locale.ENGLISH, Str.The_quantity_entered_exceeds_the_reserved_inventory_levels[Global.LANG], strCaption1, strCaption2, strCaption);
									msgDialog.mStrSubMessage = Str.Reenter_the_quantity_to_continue[Global.LANG];
									msgDialog.mStrMiddle = Str.Return[Global.LANG];
								}
								final int finalNIdx = nIdx;
								msgDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
									@Override
									public void onDismiss(DialogInterface dialogInterface) {
										if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT || msgDialog.mReturnCode == Global.DLG_RETURN_MIDDLE)
										{

										}
										else
										{
											mUStockDList.get(finalNIdx).dActualQty = mUStockDList.get(finalNIdx).dBeginQty - dQuantity;
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
					mUStockDList.get(nIdx).dActualQty = mUStockDList.get(nIdx).dBeginQty + dQuantity;
				}
				else
				{
					if(dQuantity > mUStockDList.get(nIdx).dBeginQty)
					{
						try {
							final MessageDialog msgDialog = new MessageDialog(mContext);
							msgDialog.setCancelable(false);
							msgDialog.mStrMessage = Str.There_is_not_enough_stock_to_remove_from_consignment[Global.LANG];
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
					mUStockDList.get(nIdx).dActualQty = mUStockDList.get(nIdx).dBeginQty - dQuantity;
				}
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

					if(nTransferType == 0)
					{
						if(m_nInventoryValidationType == 1 || m_nInventoryValidationType == 2)
						{
							if(uInvenDInfo == null || uInvenDInfo.nStock < nQuantity)
							{
								int nStock = 0;
								if(uInvenDInfo != null)
								{
									nStock = uInvenDInfo.nStock;
								}
								try {
									final InventoryMessageDialog msgDialog = new InventoryMessageDialog(mContext);
									msgDialog.setCancelable(false);
									String strCaption = "";
									strCaption = String.format(Locale.ENGLISH, "%d.", nStock);
									if(m_nInventoryValidationType == 1)
									{
										msgDialog.mStrTitle = Str.Warning[Global.LANG];
										msgDialog.mStrCustomerName = mSelProductInfo.strItemNo + "\n" + mSelProductInfo.strDescription;
										msgDialog.mStrMessage = String.format(Locale.ENGLISH, Str.The_quantity_you_have_entered_exceeds_the_available_stock_of[Global.LANG], strCaption);
										msgDialog.mStrSubMessage = Str.Ignore_the_oversell[Global.LANG];
										msgDialog.mStrLeft = Str.No[Global.LANG];
										msgDialog.mStrRight = Str.Yes[Global.LANG];
									}
									else
									{
										msgDialog.mStrTitle = Str.Warning[Global.LANG];
										msgDialog.mStrCustomerName = mSelProductInfo.strItemNo + "\n" + mSelProductInfo.strDescription;
										msgDialog.mStrMessage = String.format(Locale.ENGLISH, Str.The_quantity_you_have_entered_exceeds_the_available_stock_of[Global.LANG], strCaption);
										msgDialog.mStrSubMessage = Str.Reenter_the_quantity_to_continue[Global.LANG];
										msgDialog.mStrMiddle = Str.Return[Global.LANG];
									}
									msgDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
										@Override
										public void onDismiss(DialogInterface dialogInterface) {
											if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT || msgDialog.mReturnCode == Global.DLG_RETURN_MIDDLE)
											{

											}
											else
											{
												UStockDInfo info = new UStockDInfo();
												info.strItemNo = mSelProductInfo.strItemNo;
												info.nActualQty = info.nBeginQty + nQuantity;
												mUStockDList.add(info);

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
						}
						else if(m_nInventoryValidationType == 2 || m_nInventoryValidationType == 4)
						{
							final int nPresold = getPresoldValue(mSelProductInfo.strItemNo);

							if(uInvenDInfo == null || uInvenDInfo.nStock - nPresold < nQuantity)
							{
								int nStock = 0;
								if(uInvenDInfo != null)
								{
									nStock = uInvenDInfo.nStock;
								}
								try {
									final InventoryMessageDialog msgDialog = new InventoryMessageDialog(mContext);
									msgDialog.setCancelable(false);
									String strCaption = "", strCaption1, strCaption2;
									strCaption = String.format(Locale.ENGLISH, "%d", nStock);
									strCaption1 = String.format(Locale.ENGLISH, "%d", nPresold);
									strCaption2 = String.format(Locale.ENGLISH, "%d", nStock - nPresold);
									if(m_nInventoryValidationType == 3)
									{
										msgDialog.mStrTitle = Str.Warning[Global.LANG];
										msgDialog.mStrCustomerName = mSelProductInfo.strItemNo + "\n" + mSelProductInfo.strDescription;
										msgDialog.mStrMessage = String.format(Locale.ENGLISH, Str.The_quantity_entered_exceeds_the_reserved_inventory_levels[Global.LANG], strCaption1, strCaption2, strCaption);
										msgDialog.mStrSubMessage = Str.Ignore_the_oversell[Global.LANG];
										msgDialog.mStrLeft = Str.No[Global.LANG];
										msgDialog.mStrRight = Str.Yes[Global.LANG];
									}
									else
									{
										msgDialog.mStrCustomerName = mSelProductInfo.strItemNo + "\n" + mSelProductInfo.strDescription;
										msgDialog.mStrTitle = Str.Warning[Global.LANG];
										msgDialog.mStrMessage = String.format(Locale.ENGLISH, Str.The_quantity_entered_exceeds_the_reserved_inventory_levels[Global.LANG], strCaption1, strCaption2, strCaption);
										msgDialog.mStrSubMessage = Str.Reenter_the_quantity_to_continue[Global.LANG];
										msgDialog.mStrMiddle = Str.Return[Global.LANG];
									}
									msgDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
										@Override
										public void onDismiss(DialogInterface dialogInterface) {
											if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT || msgDialog.mReturnCode == Global.DLG_RETURN_MIDDLE)
											{

											}
											else
											{
												UStockDInfo info = new UStockDInfo();
												info.strItemNo = mSelProductInfo.strItemNo;
												info.nActualQty = info.nBeginQty + nQuantity;
												mUStockDList.add(info);

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
						}
					}
					else
					{
						if(nQuantity > 0)
						{
							try {
								final MessageDialog msgDialog = new MessageDialog(mContext);
								msgDialog.setCancelable(false);
								msgDialog.mStrMessage = Str.There_is_not_enough_stock_to_remove_from_consignment[Global.LANG];
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
					}

					UStockDInfo info = new UStockDInfo();
					info.strItemNo = mSelProductInfo.strItemNo;
					if(nTransferType == 0)
					{
						info.nActualQty = info.nBeginQty + nQuantity;
					}
					else
					{
						info.nActualQty = info.nBeginQty - nQuantity;
					}
					mUStockDList.add(info);
				}
			}
			else {
				if (nQuantity == 0) {
					mUStockDList.get(nIdx).nActualQty = mUStockDList.get(nIdx).nBeginQty - nQuantity;
					if(mUStockDList.get(nIdx).nBeginQty== 0)
					{
						mUStockDList.remove(nIdx);
					}

					((EditText) mRootView.findViewById(R.id.editItemNo)).setText("");
					((EditText) mRootView.findViewById(R.id.editQty)).setText("");

					focusAndHideKeyboard((EditText) mRootView.findViewById(R.id.editItemNo));

					refreshPage();
					return;
				}
				if(nTransferType == 0)
				{
					if(m_nInventoryValidationType == 1 || m_nInventoryValidationType == 2)
					{
						if(uInvenDInfo == null || uInvenDInfo.nStock < nQuantity)
						{
							int nStock = 0;
							if(uInvenDInfo != null)
							{
								nStock = uInvenDInfo.nStock;
							}
							try {
								final InventoryMessageDialog msgDialog = new InventoryMessageDialog(mContext);
								msgDialog.setCancelable(false);
								String strCaption = "";
								strCaption = String.format(Locale.ENGLISH, "%d.", nStock);
								if(m_nInventoryValidationType == 1)
								{
									msgDialog.mStrTitle = Str.Warning[Global.LANG];
									msgDialog.mStrCustomerName = mSelProductInfo.strItemNo + "\n" + mSelProductInfo.strDescription;
									msgDialog.mStrMessage = String.format(Locale.ENGLISH, Str.The_quantity_you_have_entered_exceeds_the_available_stock_of[Global.LANG], strCaption);
									msgDialog.mStrSubMessage = Str.Ignore_the_oversell[Global.LANG];
									msgDialog.mStrLeft = Str.No[Global.LANG];
									msgDialog.mStrRight = Str.Yes[Global.LANG];
								}
								else
								{
									msgDialog.mStrTitle = Str.Warning[Global.LANG];
									msgDialog.mStrCustomerName = mSelProductInfo.strItemNo + "\n" + mSelProductInfo.strDescription;
									msgDialog.mStrMessage = String.format(Locale.ENGLISH, Str.The_quantity_you_have_entered_exceeds_the_available_stock_of[Global.LANG], strCaption);
									msgDialog.mStrSubMessage = Str.Reenter_the_quantity_to_continue[Global.LANG];
									msgDialog.mStrMiddle = Str.Return[Global.LANG];
								}
								final int finalNIdx = nIdx;
								msgDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
									@Override
									public void onDismiss(DialogInterface dialogInterface) {
										if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT || msgDialog.mReturnCode == Global.DLG_RETURN_MIDDLE)
										{

										}
										else
										{
											mUStockDList.get(finalNIdx).nActualQty = mUStockDList.get(finalNIdx).nBeginQty + nQuantity;

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
					}
					else if(m_nInventoryValidationType == 2 || m_nInventoryValidationType == 4)
					{
						final int nPresold = getPresoldValue(mSelProductInfo.strItemNo);

						if(uInvenDInfo == null || uInvenDInfo.nStock - nPresold < nQuantity)
						{
							int nStock = 0;
							if(uInvenDInfo != null)
							{
								nStock = uInvenDInfo.nStock;
							}
							try {
								final InventoryMessageDialog msgDialog = new InventoryMessageDialog(mContext);
								msgDialog.setCancelable(false);
								String strCaption = "", strCaption1, strCaption2;
								strCaption = String.format(Locale.ENGLISH, "%d", nStock);
								strCaption1 = String.format(Locale.ENGLISH, "%d", nPresold);
								strCaption2 = String.format(Locale.ENGLISH, "%d", nStock - nPresold);
								if(m_nInventoryValidationType == 3)
								{
									msgDialog.mStrTitle = Str.Warning[Global.LANG];
									msgDialog.mStrCustomerName = mSelProductInfo.strItemNo + "\n" + mSelProductInfo.strDescription;
									msgDialog.mStrMessage = String.format(Locale.ENGLISH, Str.The_quantity_entered_exceeds_the_reserved_inventory_levels[Global.LANG], strCaption1, strCaption2, strCaption);
									msgDialog.mStrSubMessage = Str.Ignore_the_oversell[Global.LANG];
									msgDialog.mStrLeft = Str.No[Global.LANG];
									msgDialog.mStrRight = Str.Yes[Global.LANG];
								}
								else
								{
									msgDialog.mStrCustomerName = mSelProductInfo.strItemNo + "\n" + mSelProductInfo.strDescription;
									msgDialog.mStrTitle = Str.Warning[Global.LANG];
									msgDialog.mStrMessage = String.format(Locale.ENGLISH, Str.The_quantity_entered_exceeds_the_reserved_inventory_levels[Global.LANG], strCaption1, strCaption2, strCaption);
									msgDialog.mStrSubMessage = Str.Reenter_the_quantity_to_continue[Global.LANG];
									msgDialog.mStrMiddle = Str.Return[Global.LANG];
								}
								final int finalNIdx = nIdx;
								msgDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
									@Override
									public void onDismiss(DialogInterface dialogInterface) {
										if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT || msgDialog.mReturnCode == Global.DLG_RETURN_MIDDLE)
										{

										}
										else
										{
											mUStockDList.get(finalNIdx).nActualQty = mUStockDList.get(finalNIdx).nBeginQty + nQuantity;

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
					}
					mUStockDList.get(nIdx).nActualQty = mUStockDList.get(nIdx).nBeginQty + nQuantity;
				}
				else
				{
					if(nQuantity > mUStockDList.get(nIdx).nBeginQty)
					{
						try {
							final MessageDialog msgDialog = new MessageDialog(mContext);
							msgDialog.setCancelable(false);
							msgDialog.mStrMessage = Str.There_is_not_enough_stock_to_remove_from_consignment[Global.LANG];
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
					mUStockDList.get(nIdx).nActualQty = mUStockDList.get(nIdx).nBeginQty - nQuantity;
				}
			}
		}

		((EditText) mRootView.findViewById(R.id.editItemNo)).setText("");
		((EditText) mRootView.findViewById(R.id.editQty)).setText("");

		focusAndHideKeyboard((EditText) mRootView.findViewById(R.id.editItemNo));

		refreshPage();
	}

	private void doCompleted() {
		Global.gStockDInfoList = mUStockDList;
		AdjustConsignmentInventorySummaryFragment summaryFragment = new AdjustConsignmentInventorySummaryFragment();
		Bundle args = new Bundle();
		args.putInt(Global.EXTRA_TRANSFER_TYPE, nTransferType);
		summaryFragment.setArguments(args);
		mContext.replaceFragment(summaryFragment);
	}

	private void showInvenDetail(UStockDInfo info) {
		((EditText) mRootView.findViewById(R.id.editItemNo)).setText(Utils.getDisplayItemCode(mContext, info.strItemNo));
		if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
		{
			double dValue ;
			if(nTransferType == 0)
			{
				dValue = info.dActualQty - info.dBeginQty;
			}
			else
			{
				dValue = info.dBeginQty - info.dActualQty;
			}

			String strQty = String.format(Locale.ENGLISH, "%.2f", dValue);

			mRootView.findViewById(R.id.editQty).setTag("From App");
			((EditText) mRootView.findViewById(R.id.editQty)).setText(strQty);
			mRootView.findViewById(R.id.editQty).setTag(null);
			mRootView.findViewById(R.id.editQty).requestFocus();
			((EditText) mRootView.findViewById(R.id.editQty)).setSelection(strQty.length());
		}
		else
		{
			int nValue ;
			if(nTransferType == 0)
			{
				nValue = info.nActualQty - info.nBeginQty;
			}
			else
			{
				nValue = info.nBeginQty - info.nActualQty;
			}

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
				if(bModify)
				{
					try {
						String strMessage = Str.Are_you_sure_wish_to_exit_make_delivery[Global.LANG] + "\n";
						final MessageDialog msgDialog = new MessageDialog(mContext);
						msgDialog.setCancelable(false);
						msgDialog.mStrTitle = Str.exit_consignment[Global.LANG];
						msgDialog.mStrMessage = strMessage;
						msgDialog.mStrLeft = Str.No[Global.LANG];
						msgDialog.mStrRight = Str.Yes[Global.LANG];
						msgDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
							@Override
							public void onDismiss(DialogInterface dialog) {
								if( msgDialog.mReturnCode == Global.DLG_RETURN_LEFT )
								{

								}
								else if( msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT )
								{
									goback();
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
					goback();
				}
				break;
			case R.id.btnAdd:
				doAdd();
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
		}
		m_bClicked = false;
	}

	private void goback()
	{
		mExist = true;
		mContext.onBackPressed();
	}

	private class ListAdapter extends BaseAdapter {

		private List<UStockDInfo> list = new ArrayList<>();

		private class ViewHolder {
			public TextView txtItemNo, txtShortDesc, txtQty, txtLoaded;
		}

		public int getCount() {
			return list.size();
		}

		public void setListData(List<UStockDInfo> list) {
			this.list.clear();
			this.list.addAll(list);
		}

		public UStockDInfo getItem(int position) {
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
			final UStockDInfo info = getItem(position);
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

			holder.txtShortDesc.setText(getShortDesc(info.strItemNo));
			if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
			{
				holder.txtQty.setText(String.format(Locale.ENGLISH, "%.2f", info.dActualQty));
				holder.txtLoaded.setText(String.format(Locale.ENGLISH, "%.2f", info.dBeginQty));
			}
			else
			{
				holder.txtQty.setText(String.format(Locale.ENGLISH, "%d", info.nActualQty));
				holder.txtLoaded.setText(String.format(Locale.ENGLISH, "%d", info.nBeginQty));
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