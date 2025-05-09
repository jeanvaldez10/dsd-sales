package dsd.dsdvansales.fragment;

import android.content.*;
import android.content.DialogInterface.*;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
import dsd.dsdvansales.model.ProdDetlInfo;
import dsd.dsdvansales.model.ProdInfo;
import dsd.dsdvansales.model.ProdStruInfo;
import dsd.dsdvansales.model.UInvenHInfo;
import dsd.dsdvansales.model.UOrderDInfo;

public class OnHandCountFragment extends BaseFragment implements View.OnClickListener {

	private TextView mTxtNoData = null;
	private TextView mTxtSlideNoData = null;
	private ListView mListView = null;
	private ListView mSlideListView = null;
	private ListAdapter mListAdapter = null;
	private SlideListAdapter mSlideAdapter = null;

	private List<UOrderDInfo> mUOrderDList = new ArrayList<>();
	private List<ProdDetlInfo> mProdDetlList = new ArrayList<>();
	private Map<String, ProdDetlInfo> mProdUPCDetMap = new HashMap<>();
	private Map<String, ProdDetlInfo> mProdDetlItemMap = new HashMap<>();
	private Map<String, ProdInfo> mProdInfoMap = new HashMap<>();

	private int mProdDetlIdx = -1;
	private String mStrPhotoPath = "";
	private boolean m_bModified = false;
	private int m_nInventoryValidationType = 0;
	private UInvenHInfo mUInvenHinfo;

	private boolean mIsReturn = false;

	private boolean bShowCase = true;
	private String mEntryInputMethod = "";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if( getArguments() != null )
		{
			mIsReturn = getArguments().getBoolean(Global.EXTRA_IS_PICKUP, false);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootView = inflater.inflate(R.layout.fragment_on_hand_count, container, false);
		commonInit();
		return mRootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		Global.gFragIdx = Global.FRAG_ON_HANDCOUNT_FRAGMENT;
	}

	@Override
	public void onPause() {
		super.onPause();
		setUOrderDList();
	}

	private void setUOrderDList() {

		Global.gUOrderHInfo.handCountList.clear();
		for (UOrderDInfo info : mUOrderDList) {
			info.nTrxnType = Global.TRXN_DELIVER;
			if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
			{
				info.dQuantity = info.dSales;
			}
			else
			{
				info.nQuantity = info.nSales;
			}

			Global.gUOrderHInfo.handCountList.add(info);
		}
	}

	@Override
	public void initView() {
		bShowCase = Utils.getStringSetting(mContext, Global.PREF_INVENTORYUOM).equals("U") == false;
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
		mRootView.findViewById(R.id.btnPhoto).setOnClickListener(this);
		mRootView.findViewById(R.id.btnCompleted).setOnClickListener(this);

		mTxtNoData = (TextView) mRootView.findViewById(R.id.txtNoData);
		mTxtSlideNoData = (TextView) mRootView.findViewById(R.id.txtSlideNoData);
		mListView = (ListView) mRootView.findViewById(R.id.listView);
		mSlideListView = (ListView) mRootView.findViewById(R.id.slidelistView);
		mListAdapter = new ListAdapter();
		mSlideAdapter = new SlideListAdapter();

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

		mSlideListView.setAdapter(mSlideAdapter);
		mSlideListView.setOnScrollListener(new AbsListView.OnScrollListener() {
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				if (firstVisibleItem + visibleItemCount >= totalItemCount) {
					mRootView.findViewById(R.id.txtSlideMore).setVisibility(View.GONE);
				} else {
					mRootView.findViewById(R.id.txtSlideMore).setVisibility(View.VISIBLE);
				}
			}

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
			}
		});

		if(bShowCase)
		{
			mRootView.findViewById(R.id.editCase).setVisibility(View.VISIBLE);
			mRootView.findViewById(R.id.lblSep).setVisibility(View.VISIBLE);
		}
		else
		{
			mRootView.findViewById(R.id.editCase).setVisibility(View.GONE);
			mRootView.findViewById(R.id.lblSep).setVisibility(View.GONE);
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
					doProcessSelectProduct(strItemNo, 1, true, false, false);
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

		((EditText) mRootView.findViewById(R.id.editItemNo)).setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if( actionId == EditorInfo.IME_ACTION_NEXT)
				{
					if(bShowCase)
					{
						mRootView.findViewById(R.id.editQty).requestFocus();
						EditText editQty = (EditText)mRootView.findViewById(R.id.editCase);
						String strQty = editQty.getText().toString();
						editQty.setSelection(strQty.length(), strQty.length());
					}
					else
					{
						mRootView.findViewById(R.id.editQty).requestFocus();
						EditText editQty = (EditText)mRootView.findViewById(R.id.editQty);
						String strQty = editQty.getText().toString();
						editQty.setSelection(strQty.length(), strQty.length());
					}


				}
				return false;
			}
		});
		if(bShowCase)
		{
			((EditText) mRootView.findViewById(R.id.editCase)).setOnEditorActionListener(new TextView.OnEditorActionListener() {
				@Override
				public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
					if( actionId == EditorInfo.IME_ACTION_NEXT)
					{
						mRootView.findViewById(R.id.editQty).requestFocus();
						EditText editQty = (EditText)mRootView.findViewById(R.id.editQty);
						String strQty = editQty.getText().toString();
						editQty.setSelection(strQty.length(), strQty.length());

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

		if(Global.gCurCustomerInfo.strSalEntryMode.equals("B"))
		{
			mRootView.findViewById(R.id.editCase).setVisibility(View.VISIBLE);
			mRootView.findViewById(R.id.lblSep).setVisibility(View.VISIBLE);

		}else if( Global.gCurCustomerInfo.strSalEntryMode.equals("") || Global.gCurCustomerInfo.strSalEntryMode.equals("U") )
		{
			mRootView.findViewById(R.id.editCase).setVisibility(View.GONE);
			mRootView.findViewById(R.id.lblSep).setVisibility(View.GONE);
		}

		((TextView) mRootView.findViewById(R.id.txtCustomerName)).setText(Global.gCurCustomerInfo.strName);

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
		mUOrderDList.clear();

		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		mProdDetlList.clear();
		mProdDetlList = dbAdapter.getProdDetlList(mProdDetlItemMap, mProdUPCDetMap);
//		if(Utils.getStringSetting(mContext, Global.PREF_ITEM_CODE_DISPLAY_SOURCE).equals("PRODINFO"))
//		{
//			mProdInfoMap = dbAdapter.getProdInfoMap("ItemCode");
//		}
		mUInvenHinfo = dbAdapter.getUInvenHInfo(Utils.getLongSetting(mContext, Global.PREF_UINVEN_TRXN_NO));
		if (mUInvenHinfo == null) {
			mUInvenHinfo = new UInvenHInfo();
		}
		mUInvenHinfo.uInvenDList = dbAdapter.getUInvenDList(Utils.getLongSetting(mContext, Global.PREF_UINVEN_TRXN_NO));

		if (Global.gUOrderHInfo.handCountList.size() == 0) {
			List<UOrderDInfo> uOrderDList = dbAdapter.getUOrderDListForSuggestedOrder(Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo);
			mUOrderDList = uOrderDList;

			//Get Template Group from the DB
			if( Global.gCurCustomerInfo.nTemplateGrp > 0 && Global.gCurCustomerTemplateOrderInfoList != null && Global.gCurCustomerTemplateOrderInfoList.size() > 0)
			{
				for(UOrderDInfo info : Global.gCurCustomerTemplateOrderInfoList)
				{
					boolean bExist = false;
					for( UOrderDInfo aInfo : mUOrderDList)
					{
						if( info.strItemNo.equals(aInfo.strItemNo) )
						{
							bExist = true;
							break;
						}
					}
					if( !bExist )
					{
						mUOrderDList.add(info.duplicate());
					}
				}
			}
			Collections.sort(mUOrderDList, new Comparator<UOrderDInfo>() {
				@Override
				public int compare(UOrderDInfo lhs, UOrderDInfo rhs) {
					return lhs.strItemNo.compareTo(rhs.strItemNo);
				}
			});
		} else {
			mUOrderDList.addAll(Global.gUOrderHInfo.handCountList);
		}

		m_nInventoryValidationType = Utils.getIntSetting(mContext, Global.PREF_INVENCKECK);
		refreshPage();
	}

	@Override
	public void refreshPage() {
		if (mUOrderDList.size() > 0) {
			mListView.setVisibility(View.VISIBLE);
			mSlideListView.setVisibility(View.VISIBLE);
			mTxtNoData.setVisibility(View.GONE);
			mTxtSlideNoData.setVisibility(View.GONE);
			mListAdapter.setListData(mUOrderDList);
			mListAdapter.notifyDataSetChanged();
			mSlideAdapter.setListData(mUOrderDList);
			mSlideAdapter.notifyDataSetChanged();
		} else {
			mListView.setVisibility(View.GONE);
			mSlideListView.setVisibility(View.GONE);
			mTxtNoData.setVisibility(View.VISIBLE);
			mTxtSlideNoData.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void refreshLanguage() {
		((TextView) mRootView.findViewById(R.id.lblTitle)).setText(Str.COUNT_ON_HAND[Global.LANG].toUpperCase());
		((TextView) mRootView.findViewById(R.id.txtQty)).setText("");
		((TextView) mRootView.findViewById(R.id.txtDescription)).setText("");
		((Button) mRootView.findViewById(R.id.btnAdd)).setText(Str.Add[Global.LANG]);
		((Button) mRootView.findViewById(R.id.btnCompleted)).setText(Str.Create_Sale[Global.LANG]);
		mTxtNoData.setText(Str.No_data_has_been_found[Global.LANG]);
		mTxtSlideNoData.setText(Str.No_data_has_been_found[Global.LANG]);
	}

	private void doProcessSelectProduct(String strItemNo, int nQty, boolean bUnit, boolean bUpdateItemNo, boolean isSearchByItemNo)
	{
		if (strItemNo.length() == 0) {
			Utils.printDebug(mContext, Str.NoProductFound[Global.LANG]);
		} else {

			ProdDetlInfo selInfo = null;
			DbAdapter dbAdapter = new DbAdapter(mContext);
			dbAdapter.open();
			List<ProdDetlInfo> prodDetlInfoList = dbAdapter.getProductList(strItemNo);
			dbAdapter.close();
			if(prodDetlInfoList != null && prodDetlInfoList.size() > 0 )
			{
				selInfo = prodDetlInfoList.get(0);
			}

			if( selInfo != null && selInfo.strSalesAllowed.toUpperCase().equals("Y") )
			{
				if(bUpdateItemNo || !Utils.getStringSetting(mContext, Global.PREF_AUTO_SCAN_ADD).equals("1"))
				{
					((EditText) mRootView.findViewById(R.id.editItemNo)).setText(strItemNo);
					if(Utils.getProdLoconSCWType(mContext, selInfo.strItemNo) == Global.WEIGHT_ITEM)
					{
						(mRootView.findViewById(R.id.editCase)).setVisibility(View.GONE);
						(mRootView.findViewById(R.id.lblSep)).setVisibility(View.GONE);
						mRootView.findViewById(R.id.editQty).setTag("From App");
						((EditText) mRootView.findViewById(R.id.editQty)).setText(String.format(Locale.ENGLISH, "%.2f", 0f));
						mRootView.findViewById(R.id.editQty).setTag(null);
						this.showKeyboard((EditText) mRootView.findViewById(R.id.editQty), true);
					}
					else
					{
						if(bShowCase)
						{
							if( Global.gCurCustomerInfo.strSalEntryMode.equals("B"))
							{
								int nCase = getCaseValue(selInfo.strItemNo);
								int nCaseValue = nQty / nCase;
								int nQtyValue = nQty % nCase;
								(mRootView.findViewById(R.id.editCase)).setVisibility(View.VISIBLE);
								(mRootView.findViewById(R.id.lblSep)).setVisibility(View.VISIBLE);
								((EditText) mRootView.findViewById(R.id.editCase)).setText(String.valueOf(nCaseValue));
								((EditText) mRootView.findViewById(R.id.editQty)).setText(String.valueOf(nQtyValue));
								if( bUnit)
								{
									this.showKeyboard((EditText) mRootView.findViewById(R.id.editQty), true);
								}
								else
								{
									this.showKeyboard((EditText) mRootView.findViewById(R.id.editCase), true);
								}
							}
							else if (Global.gCurCustomerInfo.strSalEntryMode.equals("") || Global.gCurCustomerInfo.strSalEntryMode.equals("U"))
							{
								((EditText) mRootView.findViewById(R.id.editQty)).setText(String.valueOf(nQty));
								this.showKeyboard((EditText) mRootView.findViewById(R.id.editQty), true);
							}
						}
						else
						{
							((EditText) mRootView.findViewById(R.id.editQty)).setText(String.valueOf(nQty));
							this.showKeyboard((EditText) mRootView.findViewById(R.id.editQty), true);
						}
					}
				}
			}
			else
			{
				String strMessage = Str.ThisItemIsNotAvailableForSales[Global.LANG] + "\n";
				try {
					final MessageDialog msgDialog = new MessageDialog(mContext);
					msgDialog.setCancelable(false);
					msgDialog.mStrMessage = strMessage;
					msgDialog.mStrMiddle = Str.Return[Global.LANG];
					msgDialog.setOnDismissListener(new OnDismissListener() {
						@Override
						public void onDismiss(DialogInterface dialog) {
						}
					});
					msgDialog.show();
				}catch (Exception e)
				{
					e.printStackTrace();
				}
			}

		}
		m_bModified = true;
		doSearch(isSearchByItemNo);
		if(Utils.getStringSetting(mContext, Global.PREF_AUTO_SCAN_ADD).equals("1") && !bUpdateItemNo)
		{
			new Handler(Looper.getMainLooper()).post(new Runnable() {
				@Override
				public void run() {



					String strAmount = ((EditText) mRootView.findViewById(R.id.editQty)).getText().toString().trim();
					if(Utils.getProdLoconSCWType(mContext, mSelProductInfo.strItemNo) == Global.WEIGHT_ITEM)
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
				if( Global.gCurCustomerInfo.strSalEntryMode.equals("B"))
				{
					if( bUnit)
					{
						this.showKeyboard((EditText) mRootView.findViewById(R.id.editQty), true);
					}
					else
					{
						this.showKeyboard((EditText) mRootView.findViewById(R.id.editCase), true);
					}
				}
				else if (Global.gCurCustomerInfo.strSalEntryMode.equals("") || Global.gCurCustomerInfo.strSalEntryMode.equals("U"))
				{
					showKeyboard(((EditText) mRootView.findViewById(R.id.editQty)), true);
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
			int nQty = 0;
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
				DbAdapter dbAdapter = new DbAdapter(mContext);
				dbAdapter.open();
				String strData = dbAdapter.getProductListFromUPC(strItemUPC);
				dbAdapter.close();

				if( strData != null )
				{
					strItemNo = strData;
				}
				((EditText) mRootView.findViewById(R.id.editItemNo)).setText(strItemUPC);
				doProcessSelectProduct(strItemNo, nQty, bUnit, false, false);
			}


		} else if (strAction.equals(Global.ACTION_TAKE_PHOTO) == true) {
			mStrPhotoPath = intent.getStringExtra(Global.EXTRA_PHOTO_PATH);
		}
	}

	private void doSearch(boolean isByItemNo) {
		String strItemNo = getItemCode(((EditText) mRootView.findViewById(R.id.editItemNo)).getText().toString());
		String strDescription = Str.Invalid_Product[Global.LANG];
		int nQty = 0;
		double dQty = 0;
		mProdDetlIdx = -1;
		if (strItemNo.length() > 0) {
			mSelProductInfo = null;
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

			for (UOrderDInfo info : mUOrderDList) {
				if (info.strItemNo.equals(strItemNo) == true) {
					if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
					{
						dQty =  info.dSales;
					}
					else
					{
						nQty =  info.nSales;
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
			mRootView.findViewById(R.id.editCase).setVisibility(View.GONE);
			mRootView.findViewById(R.id.lblSep).setVisibility(View.GONE);
			if(mEntryInputMethod.equals("A"))
			{
				((EditText) mRootView.findViewById(R.id.editQty)).setText("0.00");
			}
			else
			{
				((EditText) mRootView.findViewById(R.id.editQty)).setText(String.format(Locale.ENGLISH, "%.2f", dQty));
			}
		}
		else
		{
			if(bShowCase)
			{
				if( Global.gCurCustomerInfo.strSalEntryMode.equals("B"))
				{
					int nCase = getCaseValue(strItemNo);
					int nCaseValue = nQty / nCase;
					int nQtyValue = nQty % nCase;
					mRootView.findViewById(R.id.editCase).setVisibility(View.VISIBLE);
					mRootView.findViewById(R.id.lblSep).setVisibility(View.VISIBLE);
					if(mEntryInputMethod.equals("A"))
					{
						((EditText) mRootView.findViewById(R.id.editCase)).setText("");
						((EditText) mRootView.findViewById(R.id.editQty)).setText("");
					}
					else
					{
						((EditText) mRootView.findViewById(R.id.editCase)).setText(String.valueOf(nCaseValue));
						((EditText) mRootView.findViewById(R.id.editQty)).setText(String.valueOf(nQtyValue));
					}
				}
				else if (Global.gCurCustomerInfo.strSalEntryMode.equals("") || Global.gCurCustomerInfo.strSalEntryMode.equals("U"))
				{
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
			else
			{
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

	ProdDetlInfo mSelProductInfo = null;
	private void doAdd() {


		String strQty = ((EditText) mRootView.findViewById(R.id.editQty)).getText().toString();
		String strCase = ((EditText) mRootView.findViewById(R.id.editCase)).getText().toString();

		if (mSelProductInfo == null) {
			if(!TextUtils.isEmpty(((EditText) mRootView.findViewById(R.id.editItemNo)).getText().toString()))
			{
				Utils.printDebug(mContext, Str.Please_select_valid_product[Global.LANG]);
			}
			((EditText) mRootView.findViewById(R.id.editQty)).setText("");
			((EditText) mRootView.findViewById(R.id.editCase)).setText("");
			focusAndHideKeyboard((EditText) mRootView.findViewById(R.id.editItemNo));
			return;
		}

		if(Utils.getProdLoconSCWType(mContext, mSelProductInfo.strItemNo) != Global.WEIGHT_ITEM)
		{
			if(bShowCase)
			{
				if( Global.gCurCustomerInfo.strSalEntryMode.equals("B"))
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
				else if( Global.gCurCustomerInfo.strSalEntryMode.equals("") || Global.gCurCustomerInfo.strSalEntryMode.equals("U"))
				{
					if (strQty.length() == 0)
					{
						Utils.printDebug(mContext, Str.Please_input_the_Quantity[Global.LANG]);
						showKeyboard((EditText) mRootView.findViewById(R.id.editQty), true);
						return;
					}
				}
			}
			else
			{
				if (strQty.length() == 0)
				{
					Utils.printDebug(mContext, Str.Please_input_the_Quantity[Global.LANG]);
					showKeyboard((EditText) mRootView.findViewById(R.id.editQty), true);
					return;
				}
			}
		}

		if( mSelProductInfo == null || !mSelProductInfo.strSalesAllowed.toUpperCase().equals("Y") )
		{
			String strMessage = Str.ThisItemIsNotAvailableForSales[Global.LANG] + "\n";
			try {
				final MessageDialog msgDialog = new MessageDialog(mContext);
				msgDialog.setCancelable(false);
				msgDialog.mStrMessage = strMessage;
				msgDialog.mStrMiddle = Str.Return[Global.LANG];
				msgDialog.setOnDismissListener(new OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						focusAndHideKeyboard((EditText) mRootView.findViewById(R.id.editItemNo));
					}
				});
				msgDialog.show();
			}catch (Exception e)
			{
				e.printStackTrace();
			}
			return;
		}


		int nIdx = -1;
		int nCase = getCaseValue(mSelProductInfo.strItemNo);

		for (int i = 0; i < mUOrderDList.size(); i++) {
			if (mUOrderDList.get(i).strItemNo.equals(mSelProductInfo.strItemNo) == true) {
				nIdx = i;
				break;
			}
		}

		if(Utils.getProdLoconSCWType(mContext, mSelProductInfo.strItemNo) == Global.WEIGHT_ITEM)
		{
			double dValue = Utils.parseDoubleFromString(strQty);
			if(nIdx == -1)
			{
				if(strQty.equals("0.00"))
				{
					Utils.printDebug(mContext, Str.Please_input_the_Quantity[Global.LANG]);
					showKeyboard((EditText) mRootView.findViewById(R.id.editQty), true);
					return;
				}
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
				UOrderDInfo info = new UOrderDInfo();
				info.strItemNo = mSelProductInfo.strItemNo;
				info.strProdGroup = mSelProductInfo.strProdGrp;
				info.nCustNo = Global.gCurCustomerInfo.nCustNo;

				info.dSales = dValue;
				info.strShortDesc = mSelProductInfo.strShortDesc;
				info.strDescription = mSelProductInfo.strDescription;
				info.nPrice = mSelProductInfo.nPrice;
				info.nGrossPrice = mSelProductInfo.nGrossPrice;
				info.nOrgPrice = mSelProductInfo.nOrgPrice;
				info.nOrgGrossPrice = mSelProductInfo.nOrgGrossPrice;
				info.nDiscountVal = mSelProductInfo.nDiscountVal;
				info.nPromoType = mSelProductInfo.nPromoType;
				info.nPromoDiscount = mSelProductInfo.nPromoDiscount;
				info.nBasePrice = Utils.getDividedValue(mSelProductInfo.prodLocnInfo.nBasePrice);
				info.nLocnNo = mSelProductInfo.prodLocnInfo.nLocnNo;
				info.strFullCase = mSelProductInfo.prodLocnInfo.strFullCase;
				info.nSCWType = mSelProductInfo.prodLocnInfo.nSCWType;
				mUOrderDList.add(0, info);
			}
			else
			{
				if(mEntryInputMethod.equals("A"))
				{
					if(mUOrderDList.get(nIdx).dSales + dValue == 0 && mUOrderDList.get(nIdx).dPlanQty == 0)
					{
						mUOrderDList.remove(nIdx);
					}
					else if(mUOrderDList.get(nIdx).dSales + dValue < 0)
					{
						((EditText) mRootView.findViewById(R.id.editQty)).setText("");
						Utils.printDebug(mContext, Str.Quantity_Removed_From_the_transaction[Global.LANG]);
						showKeyboard((EditText) mRootView.findViewById(R.id.editQty), true);
						return;
					}
					else
					{
						mUOrderDList.get(nIdx).dSales += dValue;
					}
				}
				else
				{
					if (strQty.equals("0.00") && mUOrderDList.get(nIdx).dPlanQty == 0) {
						mUOrderDList.remove(nIdx);
					} else {
						mUOrderDList.get(nIdx).dSales = Utils.parseDoubleFromString(strQty);
					}
				}
			}
		}
		else
		{
			int nQuantity = 0;
			if(bShowCase)
			{
				if( Global.gCurCustomerInfo.strSalEntryMode.equals("B"))
				{
					nQuantity = Utils.parseIntFromString(strQty) + Utils.parseIntFromString(strCase) * nCase;
				}
				else if( Global.gCurCustomerInfo.strSalEntryMode.equals("") || Global.gCurCustomerInfo.strSalEntryMode.equals("U") )
				{
					nQuantity = Utils.parseIntFromString(strQty);
				}
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
							Utils.printDebug(mContext, Str.Quantity_Removed_From_the_transaction[Global.LANG]);
							showKeyboard((EditText) mRootView.findViewById(R.id.editQty), true);
							return;
						}
					}

					UOrderDInfo info = new UOrderDInfo();
					info.strItemNo = mSelProductInfo.strItemNo;
					info.strProdGroup = mSelProductInfo.strProdGrp;
					info.nCustNo = Global.gCurCustomerInfo.nCustNo;

					info.nSales = nQuantity;
					info.strShortDesc = mSelProductInfo.strShortDesc;
					info.strDescription = mSelProductInfo.strDescription;
					info.nPrice = mSelProductInfo.nPrice;
					info.nGrossPrice = mSelProductInfo.nGrossPrice;
					info.nOrgPrice = mSelProductInfo.nOrgPrice;
					info.nOrgGrossPrice = mSelProductInfo.nOrgGrossPrice;
					info.nDiscountVal = mSelProductInfo.nDiscountVal;
					info.nPromoType = mSelProductInfo.nPromoType;
					info.nPromoDiscount = mSelProductInfo.nPromoDiscount;
					info.nBasePrice = Utils.getDividedValue(mSelProductInfo.prodLocnInfo.nBasePrice);
					info.nLocnNo = mSelProductInfo.prodLocnInfo.nLocnNo;
					info.strFullCase = mSelProductInfo.prodLocnInfo.strFullCase;
					info.nSCWType = mSelProductInfo.prodLocnInfo.nSCWType;
					mUOrderDList.add(0, info);
				}
			} else {
				if(mEntryInputMethod.equals("A"))
				{
					if(mUOrderDList.get(nIdx).nSales + nQuantity == 0 && mUOrderDList.get(nIdx).nPlanQty == 0)
					{
						mUOrderDList.remove(nIdx);
					}
					else if(mUOrderDList.get(nIdx).nSales + nQuantity < 0)
					{
						((EditText) mRootView.findViewById(R.id.editQty)).setText("");
						Utils.printDebug(mContext, Str.Quantity_Removed_From_the_transaction[Global.LANG]);
						showKeyboard((EditText) mRootView.findViewById(R.id.editQty), true);
						return;
					}
					else
					{
						mUOrderDList.get(nIdx).nSales += nQuantity;
					}
				}
				else
				{
					if (nQuantity == 0 && mUOrderDList.get(nIdx).nPlanQty == 0) {
						mUOrderDList.remove(nIdx);
					} else {

						mUOrderDList.get(nIdx).nSales = nQuantity;
					}
				}

			}
		}

		m_bModified = true;
		if(bShowCase)
		{
			((EditText) mRootView.findViewById(R.id.editItemNo)).setText("");
			((EditText) mRootView.findViewById(R.id.editCase)).setText("");
			((EditText) mRootView.findViewById(R.id.editQty)).setText("");
		}
		else
		{
			((EditText) mRootView.findViewById(R.id.editItemNo)).setText("");
			((EditText) mRootView.findViewById(R.id.editQty)).setText("");
		}

		focusAndHideKeyboard((EditText) mRootView.findViewById(R.id.editItemNo));
		refreshPage();
	}

	private void doComplete() {
		//Global.gDeliveryPickupCnt = 0;
		MakeDeliveryFragment fragment = new MakeDeliveryFragment();
		Global.gUOrderHInfo.deliveryList.clear();
		Global.gUOrderHInfo.pickupList.clear();
		Bundle bundle = new Bundle();
		bundle.putBoolean(Global.EXTRA_IS_PICKUP, mIsReturn);
		fragment.setArguments(bundle);
		mContext.replaceFragment(fragment);
	}

	private void showInvenDetail(UOrderDInfo info) {
		((EditText) mRootView.findViewById(R.id.editItemNo)).setText(Utils.getDisplayItemCode(mContext, info.strItemNo));
		if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
		{
			mRootView.findViewById(R.id.editCase).setVisibility(View.GONE);
			mRootView.findViewById(R.id.lblSep).setVisibility(View.GONE);

			EditText editQty = (EditText) mRootView.findViewById(R.id.editQty);
			String strQty = String.format(Locale.ENGLISH, "%.2f", info.dSales);
			editQty.setTag("From App");
			if(mEntryInputMethod.equals("A"))
			{
				editQty.setText("0.00");
			}
			else
			{
				editQty.setText(strQty);
			}
			editQty.setTag(null);
			editQty.requestFocus();
			editQty.setSelection(editQty.length());
		}
		else
		{
			if(bShowCase)
			{
				if( Global.gCurCustomerInfo.strSalEntryMode.equals("B"))
				{
					int nCase = getCaseValue(info.strItemNo);
					int nQty =  info.nSales ;
					int nCaseValue = nQty / nCase;
					int nQtyValue = nQty % nCase;

					mRootView.findViewById(R.id.editCase).setVisibility(View.VISIBLE);
					mRootView.findViewById(R.id.lblSep).setVisibility(View.VISIBLE);

					EditText editQty = (EditText) mRootView.findViewById(R.id.editQty);
					EditText editCase = (EditText) mRootView.findViewById(R.id.editCase);
					if(mEntryInputMethod.equals("A"))
					{
						editQty.setText("");
						editCase.setText("");
					}
					else
					{
						editQty.setText(String.valueOf(nQtyValue));
						editCase.setText(String.valueOf(nCaseValue));
					}
					editCase.requestFocus();
					editCase.setSelection(editCase.length(), editCase.length());
				}
				else if(Global.gCurCustomerInfo.strSalEntryMode.equals("") || Global.gCurCustomerInfo.strSalEntryMode.equals("U"))
				{
					EditText editQty = (EditText) mRootView.findViewById(R.id.editQty);
					String strQty = String.valueOf(info.nSales);
					if(mEntryInputMethod.equals("A"))
					{
						editQty.setText("");
					}
					else
					{
						editQty.setText(strQty);
					}
					editQty.requestFocus();
					editQty.setSelection(editQty.length());
				}
			}
			else{
				EditText editQty = (EditText) mRootView.findViewById(R.id.editQty);
				String strQty = String.valueOf(info.nSales);
				if(mEntryInputMethod.equals("A"))
				{
					editQty.setText("");
				}
				else {
					editQty.setText(strQty);
				}
				editQty.requestFocus();
				editQty.setSelection(editQty.length());
			}
		}
	}

	public boolean m_bClicked = false;
	@Override
	public void onClick(View v) {
		if( m_bClicked ) return;
		m_bClicked = true;
		switch (v.getId()) {
			case R.id.btnBack:
				mContext.onBackPressed();
				break;
			case R.id.btnPhoto:
				selectPhoto();
				break;
			case R.id.btnAdd:
				doAdd();
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
			break;
			case R.id.btnCompleted:
				doComplete();
				break;
		}
		m_bClicked = false;
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

	private class ListAdapter extends BaseAdapter {

		private List<UOrderDInfo> list = new ArrayList<>();

		private class ViewHolder {
			public TextView txtItemNo, txtShortDesc, txtPickLocationCode, txtLoaded, txtStock;
		}

		public int getCount() {
			return list.size();
		}

		public void setListData(List<UOrderDInfo> list) {
			this.list.clear();
			this.list.addAll(list);
			Collections.sort(this.list, new Comparator<UOrderDInfo>() {
				@Override
				public int compare(UOrderDInfo t1, UOrderDInfo t2) {
					int nSeq1 = getSequenceValue(t1.strItemNo);
					int nSeq2 = getSequenceValue(t2.strItemNo);
					if( nSeq1 == 0 && nSeq2 != 0 )
						return -1;
					else if( nSeq1 != 0 && nSeq2 == 0)
						return 1;
					else if( nSeq1 == 0 && nSeq2 == 0 )
					{
						return t1.strItemNo.compareTo(t2.strItemNo);
					}
					else
						return nSeq1 - nSeq2;
				}
			});
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
				holder.txtLoaded.setText(String.format(Locale.ENGLISH, "%.2f", info.dSales));
			}
			else
			{
				if(bShowCase)
				{
					if( Global.gCurCustomerInfo.strSalEntryMode.equals("B") )
					{
						if (Utils.isContainerItem(mContext, info.strItemNo)) {
							int nQty = info.nSales;
							holder.txtLoaded.setText(String.format(Locale.ENGLISH, "%d", nQty));
						}
						else {
							int nCase = getCaseValue(info.strItemNo);
							int nQty = info.nSales;
							holder.txtLoaded.setText(String.format(Locale.ENGLISH, "%d/%d", nQty / nCase, nQty % nCase));
						}
					}
					else if( Global.gCurCustomerInfo.strSalEntryMode.equals("") || Global.gCurCustomerInfo.strSalEntryMode.equals("U"))
					{
						int nQty =  info.nSales;
						holder.txtLoaded.setText(String.format(Locale.ENGLISH, "%d", nQty));
					}
				}
				else
				{
					int nQty =  info.nSales;
					holder.txtLoaded.setText(String.format(Locale.ENGLISH, "%d", nQty));
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

		private List<UOrderDInfo> list = new ArrayList<>();

		private class ViewHolder {
			public TextView txtItemNo, txtShortDesc, txtPickLocationCode, txtLoaded, txtStock;
		}

		public int getCount() {
			return list.size();
		}

		public void setListData(List<UOrderDInfo> list) {
			this.list.clear();
			this.list.addAll(list);
			Collections.sort(this.list, new Comparator<UOrderDInfo>() {
				@Override
				public int compare(UOrderDInfo t1, UOrderDInfo t2) {
					int nSeq1 = getSequenceValue(t1.strItemNo);
					int nSeq2 = getSequenceValue(t2.strItemNo);
					if( nSeq1 == 0 && nSeq2 != 0 )
						return -1;
					else if( nSeq1 != 0 && nSeq2 == 0)
						return 1;
					else if( nSeq1 == 0 && nSeq2 == 0 )
					{
						return t1.strItemNo.compareTo(t2.strItemNo);
					}
					else
						return nSeq1 - nSeq2;
				}
			});
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
				holder.txtStock.setText(String.format(Locale.ENGLISH, "%.2f", info.dSales));
				holder.txtLoaded.setText(String.format(Locale.ENGLISH, "%.2f", info.dPlanQty));
			}
			else
			{
				if(bShowCase)
				{
					if( Global.gCurCustomerInfo.strSalEntryMode.equals("B") )
					{
						if (Utils.isContainerItem(mContext, info.strItemNo)) {
							holder.txtLoaded.setText(String.format(Locale.ENGLISH, "%d", info.nPlanQty));
							int nQty = info.nSales ;
							holder.txtStock.setText(String.format(Locale.ENGLISH, "%d", nQty));
							holder.txtLoaded.setText(String.format(Locale.ENGLISH, "%d", info.nPlanQty));
						}
						else {
							int nCase = getCaseValue(info.strItemNo);
							holder.txtLoaded.setText(String.format(Locale.ENGLISH, "%d/%d", info.nPlanQty / nCase, info.nPlanQty % nCase));
							int nQty = info.nSales ;
							holder.txtStock.setText(String.format(Locale.ENGLISH, "%d/%d", nQty / nCase, nQty % nCase));
							holder.txtLoaded.setText(String.format(Locale.ENGLISH, "%d/%d", info.nPlanQty / nCase, info.nPlanQty % nCase));
						}
					}
					else if( Global.gCurCustomerInfo.strSalEntryMode.equals("") || Global.gCurCustomerInfo.strSalEntryMode.equals("U"))
					{
						holder.txtLoaded.setText(String.format(Locale.ENGLISH, "%d", info.nPlanQty));
						int nQty = info.nSales;
						holder.txtStock.setText(String.valueOf(nQty));
						holder.txtLoaded.setText(String.valueOf(info.nPlanQty));
					}
				}
				else
				{
					holder.txtLoaded.setText(String.format(Locale.ENGLISH, "%d", info.nPlanQty));
					int nQty = info.nSales;
					holder.txtStock.setText(String.valueOf(nQty));
					holder.txtLoaded.setText(String.valueOf(info.nPlanQty));
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