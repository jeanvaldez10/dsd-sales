package dsd.dsdvansales.fragment;

import android.app.AlertDialog;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import dsd.dsdvansales.R;
import dsd.dsdvansales.activities.BarcodeScannerActivity;
import dsd.dsdvansales.db.DbAdapter;
import dsd.dsdvansales.dialogs.BatchCodeInputDialog;
import dsd.dsdvansales.dialogs.InventoryMessageDialog;
import dsd.dsdvansales.dialogs.MessageDialog;
import dsd.dsdvansales.dialogs.ProductInformationDialog;
import dsd.dsdvansales.dialogs.SomaDialog;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.model.AuthDetInfo;
import dsd.dsdvansales.model.AuthHeaderInfo;
import dsd.dsdvansales.model.CustomerOrderInfo;
import dsd.dsdvansales.model.DescTypeInfo;
import dsd.dsdvansales.model.LoadBlotInfo;
import dsd.dsdvansales.model.OrderHistoryInfo;
import dsd.dsdvansales.model.OrderLineInfo;
import dsd.dsdvansales.model.PresoldOrDInfo;
import dsd.dsdvansales.model.PresoldOrHInfo;
import dsd.dsdvansales.model.ProdDetlInfo;
import dsd.dsdvansales.model.ProdInfo;
import dsd.dsdvansales.model.ProdStruInfo;
import dsd.dsdvansales.model.TaxRatesInfo;
import dsd.dsdvansales.model.UFileTransactionInfo;
import dsd.dsdvansales.model.UInvenDInfo;
import dsd.dsdvansales.model.UInvenHInfo;
import dsd.dsdvansales.model.UOrderDInfo;
import dsd.dsdvansales.model.UOrderHInfo;
import dsd.dsdvansales.model.UPromotionInfo;
import dsd.dsdvansales.views.AlertSingleChoiceAdapter;

public class MakeDeliveryFragment extends BaseFragment implements View.OnClickListener {

	private int mWindowWidth, mWindowHeight;
	private int start_x_cord, start_y_cord, start_left_margin, start_top_margin;
	private ImageView mSomaBtn = null;
	private TextView mTxtNoData = null;
	private TextView mTxtSlideNoData = null;
	private ListView mListView = null;
	private ListView mSlideListView = null;
	private ListAdapter mListAdapter = null;
	private SlideListAdapter mSlideAdapter = null;

	private List<UOrderDInfo> mUOrderDList = new ArrayList<>();

	private List<DescTypeInfo> mDeliveryReasonCodeList = new ArrayList<>();
	private List<DescTypeInfo> mFreeReasonCodeList = new ArrayList<>();
	private List<DescTypeInfo> mSampleReasonCodeList = new ArrayList<>();

	public boolean mIsPickup = false;
	private String mStrPhotoPath = "";
	private boolean m_bModified = false;
	private int m_nInventoryValidationType = 0;
	private UInvenHInfo mUInvenHinfo;

	private List<ProdDetlInfo> mProdDetlList = new ArrayList<>();
	private Map<String, ProdDetlInfo> mProdDetlItemMap = new HashMap<>();
	private Map<String, ProdDetlInfo> mProdDetlUPCMap = new HashMap<>();
	private Map<String, ProdInfo> mProdInfoMap = new HashMap<>();

	private int mSelIdx = -1;
	private boolean mIsPresoldOrder = false;
	private String mStrOrderNo = "";

	private String strCartonDescription = "";

	private boolean bFreeItem = false;
	private String strModifiable = "Y";

	private AuthHeaderInfo mAuthHeaderInfo;
	private List<AuthDetInfo> mAuthDetlInfoList;
	private Map<String, AuthDetInfo> mAuthItemMap;
	List<CustomerOrderInfo> latestOrderInfo = new ArrayList<>();
	private boolean bShowCase = true;

	private boolean bReverseMode = false;
	private boolean bFirstVisit = true;
	private String mEntryInputMethod = "";

	private boolean filter = false;

	public boolean isModified()
	{
		return m_bModified;
	}

	private boolean futureOrder = false;

	private String sortFlag = "";

	private boolean bProcessingBarcodeScan = false;

	public void setModified(boolean bFlag)
	{
		m_bModified = bFlag;
	}
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
		m_bCompleted = false;
		mRootView = inflater.inflate(R.layout.fragment_make_delivery, container, false);

		if(Global.gUOrderHInfo != null && Global.gUOrderHInfo.futureOrderMethod == 0 && Global.gUOrderHInfo.strDeliveryDate.equals(new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(new Date())))
		{
			futureOrder = false;
		}
		else
		{
			futureOrder = true;
		}
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

		mWindowWidth = mContext.getWindowManager().getDefaultDisplay().getWidth();
		mWindowHeight = mContext.getWindowManager().getDefaultDisplay().getHeight();

		if(Utils.getStringSetting(mContext,Global.PREF_SHOW_SOMA).equals("1"))
		{
			new Handler(Looper.getMainLooper()).post(new Runnable() {
				@Override
				public void run() {
					int nSomaLeftMargin = Utils.getIntSetting(mContext, Global.PREF_SOMA_LEFT_MARGIN);
					int nSomaTopMargin = Utils.getIntSetting(mContext, Global.PREF_SOMA_TOP_MARGIN);

					if(nSomaLeftMargin == 0 && nSomaTopMargin == 0)
					{
						nSomaLeftMargin = mWindowWidth - mSomaBtn.getWidth();
						nSomaTopMargin = (int)Utils.convertDpToPixel(30, mContext);
					}

					RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mSomaBtn.getLayoutParams();
					layoutParams.topMargin = nSomaTopMargin;
					layoutParams.leftMargin = nSomaLeftMargin;
					mSomaBtn.setLayoutParams(layoutParams);
				}
			});
		}

		return mRootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		mStockMapD.clear();
		mStockMap.clear();
		Global.gFragIdx = Global.FRAG_MAKE_DELIVERY;
	}

	@Override
	public void onPause() {
		super.onPause();
		setUOrderDList(false);
	}

	private void setUOrderDList(boolean bSetType) {
		if (Global.gUOrderHInfo == null) {
			return;
		}
		if (mIsPickup == false) {
			Global.gUOrderHInfo.deliveryList.clear();
			for (UOrderDInfo info : mUOrderDList) {
				if( info.nTrxnType == 0 )
				{
					info.nTrxnType = Global.TRXN_DELIVER;
				}

				if(bSetType)
				{
					if( mCurDescTypeinfo != null && info.nTrxnType == Global.TRXN_DELIVER)
					{
						info.nReasonCode = mCurDescTypeinfo.nNumericKey;
					}
					/*else
					{
						info.nReasonCode = 0;
					}*/
				}

				if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
				{
					info.dQuantity = info.dSales;
				}
				else
				{
					info.nQuantity = info.nSales;
				}

				Global.gUOrderHInfo.deliveryList.add(info);
			}
		} else {
			Global.gUOrderHInfo.pickupList.clear();
			for (UOrderDInfo info : mUOrderDList) {
				if(info.nTrxnType == 0)
				{
					if( mCurDescTypeinfo != null )
					{
						if( mCurDescTypeinfo.strDescriptionTypeID.equals("BUYBACKRSN") )
						{
							info.nTrxnType = Global.TRXN_BUYBACK;
						}
						else if ( mCurDescTypeinfo.strDescriptionTypeID.equals("RETURNRSN") )
						{
							info.nTrxnType = Global.TRXN_PICKUP;
						}
					}
					else
					{
						info.nTrxnType = Global.TRXN_PICKUP;
					}
				}
				if (bSetType)
				{
					if( mCurDescTypeinfo != null )
					{
						if( mCurDescTypeinfo.strDescriptionTypeID.equals("BUYBACKRSN") )
						{
							info.nTrxnType = Global.TRXN_BUYBACK;
						}
						else if ( mCurDescTypeinfo.strDescriptionTypeID.equals("RETURNRSN") )
						{
							info.nTrxnType = Global.TRXN_PICKUP;
						}
						info.nReasonCode = mCurDescTypeinfo.nNumericKey;
					}
					else
					{
						info.nTrxnType = Global.TRXN_PICKUP;
						info.nReasonCode = 0;
					}
				}
				if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
				{
					info.dQuantity = info.dDumps;
				}
				else
				{
					info.nQuantity = info.nDumps;
				}
				Global.gUOrderHInfo.pickupList.add(info);
			}
		}
	}

	@Override
	public void initView() {
		bShowCase = Utils.getStringSetting(mContext, Global.PREF_INVENTORYUOM).equals("U") == false;
		mRootView.findViewById(R.id.btnAdd).setOnClickListener(this);

		mRootView.findViewById(R.id.btnBarcodeScanner).setOnClickListener(this);
		mRootView.findViewById(R.id.btnProductSearch).setOnClickListener(this);
		mRootView.findViewById(R.id.btnBack).setOnClickListener(this);
		mRootView.findViewById(R.id.btnPhoto).setOnClickListener(this);
		mRootView.findViewById(R.id.btnCompleted).setOnClickListener(this);
		mRootView.findViewById(R.id.btnSort).setOnClickListener(this);

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
		mListView.setLongClickable(true);
		mSlideListView.setLongClickable(true);
//		mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//			@Override
//			public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
//				doShowAvailable(position);
//				return true;
//			}
//		});
//		mSlideListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//			@Override
//			public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
//				doShowAvailable(position);
//				return true;
//			}
//		});

		((EditText) mRootView.findViewById(R.id.editItemNo)).addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				if(bProcessingBarcodeScan) return;
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
					doProcessSelectProductForScan(strItemNo, 1, true, false);
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

		if(bShowCase)
		{
			if(Global.gCurCustomerInfo.strSalEntryMode.equals("B") || Global.gCurCustomerInfo.strSalEntryMode.equals("C"))
			{
				mRootView.findViewById(R.id.editCase).setVisibility(View.VISIBLE);
				mRootView.findViewById(R.id.lblSep).setVisibility(View.VISIBLE);
			}else if( Global.gCurCustomerInfo.strSalEntryMode.equals("") || Global.gCurCustomerInfo.strSalEntryMode.equals("U") )
			{
				mRootView.findViewById(R.id.editCase).setVisibility(View.GONE);
				mRootView.findViewById(R.id.lblSep).setVisibility(View.GONE);
			}
		}
		else
		{
			mRootView.findViewById(R.id.editCase).setVisibility(View.GONE);
			mRootView.findViewById(R.id.lblSep).setVisibility(View.GONE);
		}

		Bundle bundle = getArguments();
		if( bundle != null )
		{
			try
			{
				mIsPickup = bundle.getBoolean(Global.EXTRA_IS_PICKUP, false);
				bReverseMode = bundle.getBoolean(Global.EXTRA_IS_REVERSE, false);
			}catch(Exception e)
			{
				e.printStackTrace();
			}
			
		}
		((TextView) mRootView.findViewById(R.id.txtCustomerName)).setText(Global.gCurCustomerInfo.strName);

		if(!mIsPickup)
		{
			mRootView.findViewById(R.id.btnAdd).setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View view) {
					if (Utils.getStringSetting(mContext, Global.PREF_DISABLE_FREE).equals("1")) {
						return true;
					}

					bFreeItem = !bFreeItem;
					if(!mIsPickup)
					{
						if(bFreeItem)
						{
							mRootView.findViewById(R.id.btnAdd).setBackground(mContext.getResources().getDrawable(R.drawable.btn_orange_large));
							((TextView)mRootView.findViewById(R.id.btnAdd)).setText(Str.Free[Global.LANG]);
						}
						else
						{
							mRootView.findViewById(R.id.btnAdd).setBackground(mContext.getResources().getDrawable(R.drawable.btn_orange_large));
							((TextView)mRootView.findViewById(R.id.btnAdd)).setText(Str.Sell[Global.LANG]);
						}
					}
					else
					{
						mRootView.findViewById(R.id.btnAdd).setBackground(mContext.getResources().getDrawable(R.drawable.btn_orange_large));
						((TextView)mRootView.findViewById(R.id.btnAdd)).setText(Str.Return[Global.LANG]);
					}

					((EditText) mRootView.findViewById(R.id.editItemNo)).setText("");
					((EditText) mRootView.findViewById(R.id.editCase)).setText("");
					((EditText) mRootView.findViewById(R.id.editQty)).setText("");
					((TextView)mRootView.findViewById(R.id.txtPriceInv)).setText("");
					showKeyboard((EditText) mRootView.findViewById(R.id.editItemNo), true);
					refreshPage();

					mSelIdx = -1;
					showReasonCode();
					showPriceInven();
					return true;
				}
			});
		}
		if(!mIsPickup)
		{
			if(bFreeItem)
			{
				mRootView.findViewById(R.id.btnAdd).setBackground(mContext.getResources().getDrawable(R.drawable.btn_orange_large));
				((TextView)mRootView.findViewById(R.id.btnAdd)).setText(Str.Free[Global.LANG]);
			}
			else
			{
				mRootView.findViewById(R.id.btnAdd).setBackground(mContext.getResources().getDrawable(R.drawable.btn_orange_large));
				((TextView)mRootView.findViewById(R.id.btnAdd)).setText(Str.Sell[Global.LANG]);
			}
		}
		else
		{
			mRootView.findViewById(R.id.btnAdd).setBackground(mContext.getResources().getDrawable(R.drawable.btn_orange_large));
			((TextView)mRootView.findViewById(R.id.btnAdd)).setText(Str.Return[Global.LANG]);
		}


		mRootView.findViewById(R.id.txtReason).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if( mSelIdx != -1 && mListAdapter != null)
				{
					final UOrderDInfo info = mUOrderDList.get(mSelIdx);
					List<DescTypeInfo> descTypeInfoList = null;
					if( info.nTrxnType == Global.TRXN_DELIVER )
					{
						descTypeInfoList = mDeliveryReasonCodeList;
					}
					else if( info.nTrxnType == Global.TRXN_FREE )
					{
						descTypeInfoList = mFreeReasonCodeList;
					}
					else if( info.nTrxnType == Global.TRXN_SAMPLE)
					{
						descTypeInfoList = mSampleReasonCodeList;
					}
					if( descTypeInfoList.size() == 0 ) return;

					final String[] strDescription = new String[descTypeInfoList.size()];
					final String curItem = ((TextView)mRootView.findViewById(R.id.txtReason)).getText().toString();

					mSelReasonIdx = -1;
					for(int i = 0 ; i < descTypeInfoList.size(); i++)
					{
						strDescription[i] = descTypeInfoList.get(i).strDescription;
						if( strDescription[i].equals(curItem) )
						{
							mSelReasonIdx = i;
						}
					}

					final List<DescTypeInfo> finalDescTypeInfoList = descTypeInfoList;
					try {
						new AlertDialog.Builder(mContext)
								.setTitle(Str.Select_Reason_code[Global.LANG])
								.setSingleChoiceItems(new AlertSingleChoiceAdapter(mContext, strDescription), mSelReasonIdx, new DialogInterface.OnClickListener()
								{
									public void onClick(DialogInterface dialog, int nSelected)
									{
										info.nReasonCode = finalDescTypeInfoList.get(nSelected).nNumericKey;
										((TextView) mRootView.findViewById(R.id.txtReason)).setText(strDescription[nSelected]);
									}
								})
								.setPositiveButton(Str.Ok[Global.LANG], new DialogInterface.OnClickListener()
								{
									public void onClick(DialogInterface dialog, int nSelected)
									{

									}
								})
								.setNegativeButton(Str.Cancel[Global.LANG], new DialogInterface.OnClickListener()
								{
									public void onClick(DialogInterface dialog, int nSelected)
									{
										info.nReasonCode = finalDescTypeInfoList.get(mSelReasonIdx).nNumericKey;
										((TextView) mRootView.findViewById(R.id.txtReason)).setText(curItem);
									}
								})
								.create()
								.show();
					}catch (Exception e)
					{
						e.printStackTrace();
					}


				}
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

		mRootView.findViewById(R.id.btnFilter).setOnClickListener(this);
		if(Utils.getIntSetting(mContext, Global.PREF_DETAIL_ENTRY) != 2)
		{
			mRootView.findViewById(R.id.layoutTotal).setVisibility(View.GONE);
		}
		else
		{
			mRootView.findViewById(R.id.layoutTotal).setVisibility(View.VISIBLE);
		}

		mSomaBtn = mRootView.findViewById(R.id.btnSoma);
		if(!Utils.getStringSetting(mContext,Global.PREF_SHOW_SOMA).equals("1"))
		{
			mSomaBtn.setVisibility(View.GONE);
		}
		else {
			mSomaBtn.setVisibility(View.VISIBLE);
		}

		mSomaBtn.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mSomaBtn.getLayoutParams();
				switch (motionEvent.getAction()){
					case MotionEvent.ACTION_DOWN:
					{
						start_x_cord = (int) motionEvent.getRawX();
						start_y_cord = (int) motionEvent.getRawY();
						start_left_margin = layoutParams.leftMargin;
						start_top_margin = layoutParams.topMargin;
					}
						break;
					case MotionEvent.ACTION_MOVE:
					{
						int x_cord = (int) motionEvent.getRawX();
						int y_cord = (int) motionEvent.getRawY();

						int left_margin = start_left_margin + (x_cord - start_x_cord);
						int top_margin = start_top_margin + (y_cord - start_y_cord);

						if (left_margin > mWindowWidth - mSomaBtn.getWidth() / 2) {
							left_margin = mWindowWidth - mSomaBtn.getWidth() / 2;
						}
						if (top_margin > mWindowHeight - mSomaBtn.getHeight() / 2) {
							top_margin = mWindowHeight - mSomaBtn.getHeight() / 2;
						}

						layoutParams.leftMargin = left_margin;
						layoutParams.topMargin = top_margin;

						if(layoutParams.leftMargin < 0) layoutParams.leftMargin = 0;
						if(layoutParams.topMargin < 0) layoutParams.topMargin = 0;
						if(Math.abs(x_cord - start_x_cord) > 10 || Math.abs(y_cord - start_y_cord) > 10)
						{
							mSomaBtn.setLayoutParams(layoutParams);
						}

					}
						break;
					case MotionEvent.ACTION_UP:
					{
						start_left_margin = layoutParams.leftMargin;
						start_top_margin = layoutParams.topMargin;

						Utils.setIntSetting(mContext, Global.PREF_SOMA_LEFT_MARGIN, start_left_margin);
						Utils.setIntSetting(mContext, Global.PREF_SOMA_TOP_MARGIN, start_top_margin);
					}
						break;
				}
				return false;
			}
		});

		mSomaBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				doSomaClicked();
			}
		});

		//mListAdapter.notifyDataSetChanged();
		loadData();
		refreshLanguage();
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

	private void doAddProductFromSoma(ProdDetlInfo prodDetlInfo)
	{
		mSelProductInfo = mProdDetlItemMap.get(prodDetlInfo.strItemNo);
		for(UOrderDInfo uOrderDInfo: mUOrderDList)
		{
			if(uOrderDInfo.strItemNo.equals(prodDetlInfo.strItemNo))
			{
				Toast.makeText(mContext, "Item is already added", Toast.LENGTH_LONG).show();
				return;
			}
		}

		UOrderDInfo info = new UOrderDInfo();
		info.strItemNo = mSelProductInfo.strItemNo;
		info.strProdGroup = mSelProductInfo.strProdGrp;
		info.nCustNo = Global.gCurCustomerInfo.nCustNo;
		if(!mIsPickup)
		{
			if(bFreeItem)
			{
				info.nTrxnType = Global.TRXN_SAMPLE;
			}
			else
			{
				info.nTrxnType = Global.TRXN_DELIVER;
			}
		}
		else
		{
			info.nTrxnType = Global.TRXN_PICKUP;
		}
		if (mIsPickup == false) {
			info.dSales = 0;
		} else {
			info.dDumps = 0;
		}
		info.strShortDesc = mSelProductInfo.strShortDesc;
		info.strDescription = mSelProductInfo.strDescription;
		if(info.nTrxnType != Global.TRXN_SAMPLE)
		{
			info.nPrice = mSelProductInfo.nPrice;
			info.nGrossPrice = mSelProductInfo.nGrossPrice;
			info.nOrgPrice = mSelProductInfo.nOrgPrice;
			info.nOrgGrossPrice = mSelProductInfo.nOrgGrossPrice;
			info.nDiscountVal = mSelProductInfo.nDiscountVal;
		}

		info.nPromoType = mSelProductInfo.nPromoType;
		info.nPromoDiscount = mSelProductInfo.nPromoDiscount;
		if(info.nTrxnType != Global.TRXN_SAMPLE)
		{
			info.nBasePrice = Utils.getDividedValue(mSelProductInfo.prodLocnInfo.nBasePrice);
		}
		info.nLocnNo = mSelProductInfo.prodLocnInfo.nLocnNo;
		info.strFullCase = mSelProductInfo.prodLocnInfo.strFullCase;
		info.nSCWType = mSelProductInfo.prodLocnInfo.nSCWType;
		if(mSelProductInfo != null && mSelProductInfo.prodLocnInfo != null)
		{
			info.nCaseFactor = mSelProductInfo.prodLocnInfo.nCaseFactor;
		}

		if(info.uPromotionInfoList.size() == 0 && mSelProductInfo.uPromotionInfoList.size() > 0)
		{
			for(UPromotionInfo uPromotionInfo : mSelProductInfo.uPromotionInfoList)
			{
				UPromotionInfo uItem = uPromotionInfo.duplicate();
				uItem.nTrxnType = (mIsPickup) ? Global.TRXN_PICKUP : Global.TRXN_DELIVER;
				uItem.nCustNo = Global.gCurCustomerInfo.nCustNo;
				uItem.nChainNo = Global.gCurCustomerInfo.nChainNo;
				uItem.strItemNo = info.strItemNo;
				info.uPromotionInfoList.add(uItem);
			}
		}

		mUOrderDList.add(0, info);
		refreshPage();
	}
	private void doSomaClicked()
	{
		final SomaDialog dialog = new SomaDialog(mContext, info -> {
			doAddProductFromSoma(info);
		}, mUOrderDList);

		dialog.setOnDismissListener(dialogInterface -> {
			if(dialog.mSelProduct != null && dialog.callback == null)
			{
				doAddProductFromSoma(dialog.mSelProduct);
			}
		});
		dialog.setCancelable(false);
		dialog.show();
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

	private void showInvenDetailAsPopup(UOrderDInfo info)
	{
		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		ProdDetlInfo prodDetlInfo = dbAdapter.getProdDetl(info.strItemNo);
		List<ProdInfo> prodInfoList = dbAdapter.getProdInfo(info.strItemNo);
		dbAdapter.close();
		if(prodInfoList.size() == 0 && !Utils.isFileExist(Global.PRODUCT_DOWNLOAD_DIR + info.strItemNo + ".jpg"))
		{
			if(Utils.isConnectingToInternet(mContext) || TextUtils.isEmpty(prodDetlInfo.strImageUrl))
			{
				Toast.makeText(mContext, Str.NoAdditionalInfo[Global.LANG], Toast.LENGTH_SHORT).show();
				return;
			}
		}

		ProductInformationDialog dialog = new ProductInformationDialog(mContext, info);
		dialog.setCancelable(false);
		dialog.show();
	}

	private void doShowAvailable(int position)
	{
		UOrderDInfo uOrderDInfo = mListAdapter.getItem(position);
		if(m_nInventoryValidationType == 3 || m_nInventoryValidationType == 4 )
		{
			if(Utils.getProdLoconSCWType(mContext, uOrderDInfo.strItemNo) == Global.WEIGHT_ITEM)
			{
				double dStock = getStockValueD(uOrderDInfo.strItemNo);
				double dPresoldStock = getPresoldValueD(uOrderDInfo.strItemNo);
				dStock -= dPresoldStock;
				String strCaption = String.format(Locale.ENGLISH,"Available for upsell %.2f", dStock);
				if(!mContext.isFinishing() && !mContext.isDestroyed())
				{
					try {
						Toast.makeText(mContext, strCaption, Toast.LENGTH_SHORT).show();
					}catch (Exception e1)
					{
						e1.printStackTrace();
					}
				}
			}
			else
			{
				int nStock = getStockValue(uOrderDInfo.strItemNo);
				int nPresoldStock = getPresoldValue(uOrderDInfo.strItemNo);
				nStock -= nPresoldStock;
				int nCase = getCaseValue(uOrderDInfo.strItemNo);
				String strCaption = String.format(Locale.ENGLISH, "Available for upsell %d / %d", nStock / nCase, nStock % nCase);
				if(!mContext.isFinishing() && !mContext.isDestroyed())
				{
					try {
						Toast.makeText(mContext, strCaption, Toast.LENGTH_SHORT).show();
					}catch (Exception e1)
					{
						e1.printStackTrace();
					}
				}
			}

		}
	}

	int mSelReasonIdx = -1;
	@Override
	public void loadData() {

		showProgressDialog(Str.Loading[Global.LANG]);
		new Thread(){
			@Override
			public void run() {

				DbAdapter dbAdapter = new DbAdapter(mContext);
				dbAdapter.open();
				List<ProdStruInfo> prodStruList = dbAdapter.getProdStruList();
				List<OrderHistoryInfo> orderHistoryInfoList = dbAdapter.getOrderHistoryList(Global.gCurCustomerInfo.nCustNo);
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

				new Handler(Looper.getMainLooper()).post(new Runnable() {
					@Override
					public void run() {
						if( prodStruList == null || prodStruList.size() == 0 )
						{
							mRootView.findViewById(R.id.btnProductTreeList).setEnabled(false);
							mRootView.findViewById(R.id.btnProductTreeList).setOnClickListener(null);
							mRootView.findViewById(R.id.btnProductTreeList).setBackground(mContext.getResources().getDrawable(R.drawable.btn_grey_large));
						}
						else
						{
							mRootView.findViewById(R.id.btnProductTreeList).setOnClickListener(MakeDeliveryFragment.this);
							mRootView.findViewById(R.id.btnProductTreeList).setEnabled(true);
							mRootView.findViewById(R.id.btnProductTreeList).setBackground(mContext.getResources().getDrawable(R.drawable.btn_white_medium));
						}
					}
				});


				mUOrderDList.clear();
				if(bReverseMode)
				{
					if(mIsPickup)
					{
						mUOrderDList.addAll(Global.gUOrderReverseHInfo.pickupList);
					}
					else
					{
						mUOrderDList.addAll(Global.gUOrderReverseHInfo.deliveryList);
					}
					new Handler(Looper.getMainLooper()).post(() -> {
						hideProgressDialog();
						if(bFirstVisit)
						{
							bFirstVisit = false;
							mRootView.findViewById(R.id.btnCompleted).performClick();
						}
					});
					return;
				}
				mProdDetlItemMap.clear();
				mProdDetlUPCMap.clear();

				dbAdapter = new DbAdapter(mContext);
				dbAdapter.open();
				latestOrderInfo.clear();
				latestOrderInfo.addAll(dbAdapter.getAllCustomerLatestOrderList());
				mAuthHeaderInfo = dbAdapter.getAuthHeaderInfo(Utils.parseIntFromString(Global.gCurCustomerInfo.strAuthGrp));
				if( mAuthHeaderInfo != null )
				{
					mAuthDetlInfoList = dbAdapter.getAuthDetailInfo(mAuthHeaderInfo.nAuthGrp);
					mAuthItemMap = new HashMap<>();
					for(AuthDetInfo authDetInfo : mAuthDetlInfoList)
					{
						mAuthItemMap.put(authDetInfo.strItemNo, authDetInfo);
					}
				}
				mDeliveryReasonCodeList = dbAdapter.getDescTypeList("NODLVRYRSN");
				mFreeReasonCodeList = dbAdapter.getDescTypeList("SAMPSALRSN");
				mSampleReasonCodeList = dbAdapter.getDescTypeList("FREESALRSN");
				mProdDetlList = dbAdapter.getProductList(mProdDetlItemMap, mProdDetlUPCMap);
//				if(Utils.getStringSetting(mContext, Global.PREF_ITEM_CODE_DISPLAY_SOURCE).equals("PRODINFO"))
//				{
//					mProdInfoMap = dbAdapter.getProdInfoMap("ItemCode");
//				}
				mUInvenHinfo = dbAdapter.getUInvenHInfo(Utils.getLongSetting(mContext, Global.PREF_UINVEN_TRXN_NO));
				if (mUInvenHinfo == null) {
					mUInvenHinfo = new UInvenHInfo();
				}
				mUInvenHinfo.uInvenDList = dbAdapter.getUInvenDList(Utils.getLongSetting(mContext, Global.PREF_UINVEN_TRXN_NO));
				//PresoldOrHInfo presoldOrHInfo = dbAdapter.getPresoldOrHInfo(Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo);
				PresoldOrHInfo presoldOrHInfo = null;
				CustomerOrderInfo customerOrderInfo = null;
				if( Global.gCurCustomerInfo.presoldOrHInfo != null && Global.gCurCustomerInfo.presoldOrHInfo.size() > 0 )
				{
					presoldOrHInfo = Global.gCurCustomerInfo.presoldOrHInfo.get(0);
					strModifiable = Global.gCurCustomerInfo.presoldOrHInfo.get(0).strModifiable;
				}

				if(Global.gCurCustomerInfo.latestOrderInfo != null && Global.gCurCustomerInfo.latestOrderInfo.size() > 0)
				{
					customerOrderInfo = Global.gCurCustomerInfo.latestOrderInfo.get(0);
				}

				if (mIsPickup == false) {
					if (Global.gUOrderHInfo == null || Global.gUOrderHInfo.deliveryList == null || Global.gUOrderHInfo.deliveryList.size() == 0) {
						if( Global.gUOrderHInfo == null )
						{
							Global.gUOrderHInfo = new UOrderHInfo();
						}
						if( Global.gUOrderHInfo.deliveryList == null )
						{
							Global.gUOrderHInfo.deliveryList = new ArrayList<>();
						}
						Global.gUOrderHInfo.strNettSplit = Global.gCurCustomerInfo.nInvoiceFmt == 1 ? "N" : "S";
						if(presoldOrHInfo == null && customerOrderInfo == null)
						{
							List<UOrderDInfo> orderHInfoList = getDataFromOrderHistory(dbAdapter);
							if(orderHInfoList != null && orderHInfoList.size() > 0)
							{
								Global.gUOrderHInfo.deliveryList.addAll(orderHInfoList);
								mUOrderDList.addAll(orderHInfoList);
							}
						}
						else if(customerOrderInfo != null) {
							Global.gUOrderHInfo.strSourceType = "P";
							List<UOrderDInfo> uOrderDList = new ArrayList<>();
							if (Global.gUOrderHInfo.deliveryList.size() == 0) {
								Global.gUOrderHInfo.strOrderNumber = String.valueOf(customerOrderInfo.orderNumber);
								Global.gUOrderHInfo.strUser1 = "";
								Global.gUOrderHInfo.strUser2 = "";

								for (OrderLineInfo lineInfo : customerOrderInfo.orderLines) {
//						if( presoldOrDInfo.nOrderQty == 0 && !presoldOrHInfo.strOrderNo.equals("0")) continue;
									UOrderDInfo info = new UOrderDInfo();

									info.strItemNo = lineInfo.itemNumber;
									info.isFromPresoldOrder = true;
									if (Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM) {
										info.dPresoldValue = lineInfo.quantity;
										info.dPlanQty = lineInfo.quantity;
										info.dSales = info.dPlanQty;
									} else {
										info.nPresoldValue = lineInfo.quantity;
										info.nPlanQty = lineInfo.quantity;
										info.nSales = info.nPlanQty;
									}

									info.nReasonCode = 0;
									info.nCustNo = Global.gCurCustomerInfo.nCustNo;
									info.strPickLocationCode = "";

									info.nTrxnType = lineInfo.transactionType;

									ProdDetlInfo productInfo = mProdDetlItemMap.get(info.strItemNo);
									if (productInfo != null && productInfo.prodLocnInfo != null) {
										info.nCaseFactor = productInfo.prodLocnInfo.nCaseFactor;
										info.strProdGroup = productInfo.strProdGrp;
									}
									if (productInfo != null) {
										if (productInfo.prodLocnInfo != null) {
											info.nLocnNo = productInfo.prodLocnInfo.nLocnNo;
										}

										if (info.nTrxnType == Global.TRXN_DELIVER) {
											info.strShortDesc = productInfo.strShortDesc;
											info.strDescription = productInfo.strDescription;
											info.nPrice = lineInfo.price;
											if (info.nPrice == 0.0f) {
												info.nPrice = productInfo.nPrice;
												info.nOrgPrice = productInfo.nOrgPrice;
												info.nGrossPrice = productInfo.nGrossPrice;
												info.nOrgGrossPrice = productInfo.nOrgGrossPrice;
												info.nDiscountVal = productInfo.nDiscountVal;
												info.nPromoType = productInfo.nPromoType;
												info.nPromoDiscount = productInfo.nPromoDiscount;
											} else {
												info.nDiscountVal = 0;
												info.nPromoType = 0;
												info.nPromoDiscount = 0;

												info.nOrgPrice = info.nPrice;
												info.nGrossPrice = Utils.get5RoundedValue(mContext, info.nPrice);
												info.nOrgGrossPrice = info.nPrice;
											}


											info.nBasePrice = lineInfo.price;
											if (info.nBasePrice == 0.0f) {
												info.nBasePrice = Utils.getDividedValue(productInfo.prodLocnInfo.nBasePrice);
											}

											for (UPromotionInfo uPromotionInfo : productInfo.uPromotionInfoList) {
												UPromotionInfo uItem = uPromotionInfo.duplicate();
												uItem.nTrxnType = (mIsPickup == false) ? Global.TRXN_DELIVER : Global.TRXN_PICKUP;
												uItem.nCustNo = Global.gCurCustomerInfo.nCustNo;
												uItem.nChainNo = Global.gCurCustomerInfo.nChainNo;
												uItem.strItemNo = info.strItemNo;
												info.uPromotionInfoList.add(uItem);
											}
										} else if (info.nTrxnType == Global.TRXN_FREE || info.nTrxnType == Global.TRXN_SAMPLE) {
											info.strShortDesc = productInfo.strShortDesc;
											info.strDescription = productInfo.strDescription;
										}

										info.strFullCase = productInfo.prodLocnInfo.strFullCase;
										info.nSCWType = productInfo.prodLocnInfo.nSCWType;
									}

									uOrderDList.add(info);
								}
							}
							mUOrderDList.addAll(uOrderDList);
						}
						else if(presoldOrHInfo != null)
						{
							Global.gUOrderHInfo.strSourceType = "P";
							List<UOrderDInfo> uOrderDList = new ArrayList<>();
							if( Global.gUOrderHInfo.deliveryList.size() == 0 )
							{
								List<PresoldOrDInfo> presoldOrDInfoList = new ArrayList<>();
								if( Global.gCurCustomerInfo.presoldOrHInfo != null && Global.gCurCustomerInfo.presoldOrHInfo.size() > 0 )
								{
									presoldOrDInfoList = dbAdapter.getPresoldOrDListForFreeItem(Global.gCurCustomerInfo.presoldOrHInfo.get(0).strDetailFile);
								}
								if(Global.gCurCustomerInfo.presoldOrHInfo != null && Global.gCurCustomerInfo.presoldOrHInfo.size() > 0)
								{
									Global.gUOrderHInfo.strOrderNumber = Global.gCurCustomerInfo.presoldOrHInfo.get(0).strOrderNo;
									Global.gUOrderHInfo.strUser1 = Global.gCurCustomerInfo.presoldOrHInfo.get(0).strUser1;
									Global.gUOrderHInfo.strUser2 = Global.gCurCustomerInfo.presoldOrHInfo.get(0).strUser2;
								}
								else
								{
									Global.gUOrderHInfo.strOrderNumber = "0";
								}
								for (PresoldOrDInfo presoldOrDInfo : presoldOrDInfoList) {
//						if( presoldOrDInfo.nOrderQty == 0 && !presoldOrHInfo.strOrderNo.equals("0")) continue;
									UOrderDInfo info = new UOrderDInfo();

									info.strItemNo = presoldOrDInfo.strItemNo;
									info.isFromPresoldOrder = true;
									if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
									{
										info.dPresoldValue = presoldOrDInfo.dOrderQty;
										info.dPlanQty = presoldOrDInfo.dOrderQty;
										info.dSales = info.dPlanQty;
									}
									else
									{
										info.nPresoldValue = presoldOrDInfo.nOrderQty;
										info.nPlanQty = presoldOrDInfo.nOrderQty;
										info.nSales = info.nPlanQty;
									}

									info.nReasonCode = presoldOrDInfo.nReasonCode;
									info.nCustNo = Global.gCurCustomerInfo.nCustNo;
									info.strPickLocationCode = presoldOrDInfo.strPickLocationCode;

									info.nTrxnType = Utils.parseIntFromString(presoldOrDInfo.strTrxnType);
									if(info.nTrxnType == 0)
									{
										info.nTrxnType = Global.TRXN_DELIVER;
									}
									ProdDetlInfo productInfo = mProdDetlItemMap.get(presoldOrDInfo.strItemNo);
									if(productInfo != null && productInfo.prodLocnInfo != null)
									{
										info.nCaseFactor = productInfo.prodLocnInfo.nCaseFactor;
										info.strProdGroup = productInfo.strProdGrp;
									}
									if( productInfo != null )
									{
										if( productInfo.prodLocnInfo != null )
										{
											info.nLocnNo = productInfo.prodLocnInfo.nLocnNo;
										}

										if( info.nTrxnType == Global.TRXN_DELIVER )
										{
											info.strShortDesc = productInfo.strShortDesc;
											info.strDescription = productInfo.strDescription;
											info.nPrice = Utils.getDividedValue(presoldOrDInfo.nPrice);
											if( info.nPrice == 0.0f)
											{
												info.nPrice = productInfo.nPrice;
												info.nOrgPrice = productInfo.nOrgPrice;
												info.nGrossPrice = productInfo.nGrossPrice;
												info.nOrgGrossPrice = productInfo.nOrgGrossPrice;
												info.nDiscountVal = productInfo.nDiscountVal;
												info.nPromoType = productInfo.nPromoType;
												info.nPromoDiscount = productInfo.nPromoDiscount;
											}
											else
											{
												info.nDiscountVal = 0;
												info.nPromoType = 0;
												info.nPromoDiscount = 0;

												info.nOrgPrice = info.nPrice;
												info.nGrossPrice = Utils.get5RoundedValue(mContext, info.nPrice);
												info.nOrgGrossPrice = info.nPrice;
											}


											info.nBasePrice = Utils.getDividedValue(presoldOrDInfo.nPrice);
											if( info.nBasePrice == 0.0f)
											{
												info.nBasePrice = Utils.getDividedValue(productInfo.prodLocnInfo.nBasePrice);
											}

											for(UPromotionInfo uPromotionInfo : productInfo.uPromotionInfoList)
											{
												UPromotionInfo uItem = uPromotionInfo.duplicate();
												uItem.nTrxnType = (mIsPickup == false) ? Global.TRXN_DELIVER : Global.TRXN_PICKUP;
												uItem.nCustNo = Global.gCurCustomerInfo.nCustNo;
												uItem.nChainNo = Global.gCurCustomerInfo.nChainNo;
												uItem.strItemNo = info.strItemNo;
												info.uPromotionInfoList.add(uItem);
											}
										}
										else if (info.nTrxnType == Global.TRXN_FREE || info.nTrxnType == Global.TRXN_SAMPLE)
										{
											info.strShortDesc = productInfo.strShortDesc;
											info.strDescription = productInfo.strDescription;
										}

										info.strFullCase = productInfo.prodLocnInfo.strFullCase;
										info.nSCWType = productInfo.prodLocnInfo.nSCWType;
									}

									uOrderDList.add(info);
								}
							}

							if(Global.gUOrderHInfo.handCountList != null && Global.gUOrderHInfo.handCountList.size() > 0 )
							{
								if(presoldOrHInfo.strType.equals("B"))
								{
									for (UOrderDInfo info : uOrderDList) {
										for(UOrderDInfo handCountInfo : Global.gUOrderHInfo.handCountList)
										{
											if( handCountInfo.strItemNo.equals(info.strItemNo))
											{
												//info.nPlanQty = info.nPlanQty - handCountInfo.nSales;
												if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
												{
													info.dSales = info.dPlanQty - handCountInfo.dSales;
												}
												else
												{
													info.nSales = info.nPlanQty - handCountInfo.nSales;
												}

												break;
											}
										}
										if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
										{
											if( info.dSales < 0 )
											{
												info.dSales = 0;
											}
										}
										else
										{
											if( info.nSales < 0 )
											{
												info.nSales = 0;
											}
										}

										mUOrderDList.add(info);
									}
								}
								else
								{
									for (UOrderDInfo  info : uOrderDList) {
										if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
										{
											info.dSales = info.dPlanQty;
										}
										else
										{
											info.nSales = info.nPlanQty;
										}
										if(!mIsPickup)
										{
											if(bFreeItem)
											{
												info.nTrxnType = Global.TRXN_SAMPLE;
											}
											else
											{
												info.nTrxnType = Global.TRXN_DELIVER;
											}
										}
										else
										{
											info.nTrxnType = Global.TRXN_PICKUP;
										}

										mUOrderDList.add(info);
									}
								}

								//Modified by Mr. Malcolm 2016-9.12
					/*else if(presoldOrHInfo.strType.equals("C"))
					{
						for (UOrderDInfo info : uOrderDList) {
							for(UOrderDInfo handCountInfo : Global.gUOrderHInfo.handCountList)
							{
								if( handCountInfo.strItemNo.equals(info.strItemNo) && handCountInfo.nSales != 0)
								{
									info.nPlanQty = handCountInfo.nSales;
									break;
								}
							}
							mUOrderDList.add(info);
						}
					}*/

							}
							else
							{
								if(presoldOrHInfo != null && presoldOrHInfo.strType != null && presoldOrHInfo.strType.equals("P"))
								{
									for (UOrderDInfo  info : uOrderDList) {
										if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
										{
											info.dSales = info.dPlanQty;
										}
										else
										{
											info.nSales = info.nPlanQty;
										}

										mUOrderDList.add(info);
									}
								}
								else
								{
									mUOrderDList.addAll(uOrderDList);
								}
							}

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
						}
					} else {
						mUOrderDList.addAll(Global.gUOrderHInfo.deliveryList);
					}

				} else {
					if( Global.gUOrderHInfo == null )
					{
						Global.gUOrderHInfo = new UOrderHInfo();
					}
					if( Global.gUOrderHInfo.pickupList == null )
					{
						Global.gUOrderHInfo.pickupList = new ArrayList<>();
					}
					mUOrderDList.addAll(Global.gUOrderHInfo.pickupList);

					if(mUOrderDList.size() == 0 && !Utils.getStringSetting(mContext, Global.PREF_RETURNS).equals("2"))
					{
						List<UOrderDInfo> orderHInfoList = getDataFromOrderHistoryForReturns(dbAdapter);
						if(orderHInfoList != null && orderHInfoList.size() > 0)
						{
							Global.gUOrderHInfo.pickupList.addAll(orderHInfoList);
							mUOrderDList.addAll(orderHInfoList);
						}
					}

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

			/*Collections.sort(mUOrderDList, new Comparator<UOrderDInfo>() {
				@Override
				public int compare(UOrderDInfo lhs, UOrderDInfo rhs) {
					return lhs.strItemNo.compareTo(rhs.strItemNo);
				}
			});*/
				}
				dbAdapter.close();
				mIsPresoldOrder = presoldOrHInfo != null;
				if(mIsPresoldOrder)
				{
					mStrOrderNo = presoldOrHInfo.strOrderNo;
				}
				m_nInventoryValidationType = Utils.getIntSetting(mContext, Global.PREF_INVENCKECK);

				//Remove UnAuthorized item
				removeUnAuthorizedItem();

				//Check if there is items with transaction type = 4
				boolean bExist = false;
				boolean bFreeExist = false;
				for(UOrderDInfo uOrderDInfo : mUOrderDList)
				{
					if(uOrderDInfo.nTrxnType == Global.TRXN_DELIVER || uOrderDInfo.nTrxnType == 0)
					{
						bExist = true;
						break;
					}
					if(uOrderDInfo.nTrxnType == Global.TRXN_FREE || uOrderDInfo.nTrxnType == Global.TRXN_SAMPLE)
					{
						bFreeExist = true;
					}
				}
				if(!bExist && bFreeExist)
				{
					bFreeItem = true;
				}
				else
				{
					bFirstVisit = false;
				}

				new Handler(Looper.getMainLooper()).post(new Runnable() {
					@Override
					public void run() {
						if(!mIsPickup)
						{
							if(bFreeItem)
							{
								mRootView.findViewById(R.id.btnAdd).setBackground(mContext.getResources().getDrawable(R.drawable.btn_orange_large));
								((TextView)mRootView.findViewById(R.id.btnAdd)).setText(Str.Free[Global.LANG]);
							}
							else
							{
								mRootView.findViewById(R.id.btnAdd).setBackground(mContext.getResources().getDrawable(R.drawable.btn_orange_large));
								((TextView)mRootView.findViewById(R.id.btnAdd)).setText(Str.Sell[Global.LANG]);
							}
						}
						else
						{
							mRootView.findViewById(R.id.btnAdd).setBackground(mContext.getResources().getDrawable(R.drawable.btn_orange_large));
							((TextView)mRootView.findViewById(R.id.btnAdd)).setText(Str.Return[Global.LANG]);
						}
						hideProgressDialog();

						if( mIsPickup )
						{
							mRootView.findViewById(R.id.btnOrderHistory).setVisibility(View.GONE);
						}
						else
						{
							if( orderHistoryInfoList != null && orderHistoryInfoList.size() > 0 )
							{
								mRootView.findViewById(R.id.btnOrderHistory).setVisibility(View.VISIBLE);
								mRootView.findViewById(R.id.btnOrderHistory).setOnClickListener(MakeDeliveryFragment.this);

							}
							else
							{
								mRootView.findViewById(R.id.btnOrderHistory).setVisibility(View.GONE);
							}
						}
						refreshPage();
					}
				});
			}
		}.start();
	}

	private List<UOrderDInfo> getDataFromOrderHistoryForReturns(DbAdapter dbAdapter)
	{
		List<UOrderDInfo> dataList = new ArrayList<>();
		List<OrderHistoryInfo> orderHistoryInfoList = dbAdapter.getOrderHistoryList(Global.gCurCustomerInfo.nCustNo);
		Map<String, OrderHistoryInfo> orderMap = new HashMap<>();
		for(OrderHistoryInfo info : orderHistoryInfoList)
		{
			if(orderMap.get(info.strItemNo) != null) continue;
			if(info.nSAQty == 0 ) continue;

			orderMap.put(info.strItemNo, info);
			ProdDetlInfo prodDetlInfo = mProdDetlItemMap.get(info.strItemNo);
			UOrderDInfo uOrderDInfo = new UOrderDInfo();
			uOrderDInfo.strItemNo = info.strItemNo;
			uOrderDInfo.nTrxnType = Global.TRXN_PICKUP;
			uOrderDInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
			uOrderDInfo.nSales = 0;
			if( prodDetlInfo != null )
			{
				uOrderDInfo.strProdGroup = prodDetlInfo.strProdGrp;
				uOrderDInfo.strShortDesc = prodDetlInfo.strShortDesc;
				uOrderDInfo.strDescription = prodDetlInfo.strDescription;
				uOrderDInfo.nPrice = prodDetlInfo.nPrice;
				uOrderDInfo.nGrossPrice = prodDetlInfo.nGrossPrice;
				uOrderDInfo.nOrgPrice = prodDetlInfo.nOrgPrice;
				uOrderDInfo.nOrgGrossPrice = prodDetlInfo.nOrgGrossPrice;
				uOrderDInfo.nDiscountVal = prodDetlInfo.nDiscountVal;
				uOrderDInfo.nPromoType = prodDetlInfo.nPromoType;
				uOrderDInfo.nPromoDiscount = prodDetlInfo.nPromoDiscount;
				uOrderDInfo.nBasePrice = Utils.getDividedValue(prodDetlInfo.prodLocnInfo.nBasePrice);
				uOrderDInfo.nLocnNo = prodDetlInfo.prodLocnInfo.nLocnNo;
				uOrderDInfo.strFullCase = prodDetlInfo.prodLocnInfo.strFullCase;
				uOrderDInfo.nSCWType = prodDetlInfo.prodLocnInfo.nSCWType;
				if(prodDetlInfo != null && prodDetlInfo.prodLocnInfo != null)
				{
					uOrderDInfo.nCaseFactor = prodDetlInfo.prodLocnInfo.nCaseFactor;
				}
				for(UPromotionInfo uPromotionInfo : prodDetlInfo.uPromotionInfoList)
				{
					UPromotionInfo uItem = uPromotionInfo.duplicate();
					uItem.nTrxnType = (mIsPickup) ? Global.TRXN_PICKUP : Global.TRXN_DELIVER;
					uItem.nCustNo = Global.gCurCustomerInfo.nCustNo;
					uItem.nChainNo = Global.gCurCustomerInfo.nChainNo;
					uItem.strItemNo = info.strItemNo;
					uOrderDInfo.uPromotionInfoList.add(uItem);
				}
				dataList.add(uOrderDInfo);
			}
		}
		return dataList;
	}

	private List<UOrderDInfo> getDataFromOrderHistory(DbAdapter dbAdapter)
	{
		List<OrderHistoryInfo> orderHistoryInfoList = dbAdapter.getOrderHistoryList(Global.gCurCustomerInfo.nCustNo);
		List<AuthDetInfo> templateAuthDetlInfoList = new ArrayList<>();
		List<AuthDetInfo> featureAuthDetlInfoList = new ArrayList<>();
		List<UOrderDInfo> dataList = new ArrayList<>();
		if(Global.gCurCustomerInfo.nFeatGrp != 0)
		{
			featureAuthDetlInfoList.addAll(dbAdapter.getAuthDetailInfo(Global.gCurCustomerInfo.nFeatGrp));
			Map<String, AuthDetInfo> orderMap = new HashMap<>();
			for(AuthDetInfo info : featureAuthDetlInfoList)
			{
				if(orderMap.get(info.strItemNo) != null) continue;

				orderMap.put(info.strItemNo, info);
				ProdDetlInfo prodDetlInfo = mProdDetlItemMap.get(info.strItemNo);
				UOrderDInfo uOrderDInfo = new UOrderDInfo();
				uOrderDInfo.strItemNo = info.strItemNo;
				uOrderDInfo.nTrxnType = Global.TRXN_DELIVER;
				uOrderDInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
				uOrderDInfo.nSales = 0;
				if( prodDetlInfo != null)
				{
					uOrderDInfo.strProdGroup = prodDetlInfo.strProdGrp;
					uOrderDInfo.strShortDesc = prodDetlInfo.strShortDesc;
					uOrderDInfo.strDescription = prodDetlInfo.strDescription;
					uOrderDInfo.nPrice = prodDetlInfo.nPrice;
					uOrderDInfo.nGrossPrice = prodDetlInfo.nGrossPrice;
					uOrderDInfo.nOrgPrice = prodDetlInfo.nOrgPrice;
					uOrderDInfo.nOrgGrossPrice = prodDetlInfo.nOrgGrossPrice;
					uOrderDInfo.nDiscountVal = prodDetlInfo.nDiscountVal;
					uOrderDInfo.nPromoType = prodDetlInfo.nPromoType;
					uOrderDInfo.nPromoDiscount = prodDetlInfo.nPromoDiscount;
					uOrderDInfo.nBasePrice = Utils.getDividedValue(prodDetlInfo.prodLocnInfo.nBasePrice);
					uOrderDInfo.nLocnNo = prodDetlInfo.prodLocnInfo.nLocnNo;
					uOrderDInfo.strFullCase = prodDetlInfo.prodLocnInfo.strFullCase;
					uOrderDInfo.nSCWType = prodDetlInfo.prodLocnInfo.nSCWType;

					if(prodDetlInfo != null && prodDetlInfo.prodLocnInfo != null)
					{
						uOrderDInfo.nCaseFactor = prodDetlInfo.prodLocnInfo.nCaseFactor;
					}
					for( UPromotionInfo uPromotionInfo : prodDetlInfo.uPromotionInfoList )
					{
						UPromotionInfo uItem = uPromotionInfo.duplicate();
						uItem.nTrxnType = (mIsPickup) ? Global.TRXN_PICKUP : Global.TRXN_DELIVER;
						uItem.nCustNo = Global.gCurCustomerInfo.nCustNo;
						uItem.nChainNo = Global.gCurCustomerInfo.nChainNo;
						uItem.strItemNo = info.strItemNo;
						uOrderDInfo.uPromotionInfoList.add(uItem);
					}
					dataList.add(uOrderDInfo);
				}
			}
		}

		if(Global.gCurCustomerInfo.nTempGrp != 0)
		{
			templateAuthDetlInfoList.addAll(dbAdapter.getAuthDetailInfo(Global.gCurCustomerInfo.nTempGrp));
			Map<String, AuthDetInfo> orderMap = new HashMap<>();
			for(AuthDetInfo info : templateAuthDetlInfoList)
			{
				if(orderMap.get(info.strItemNo) != null) continue;

				orderMap.put(info.strItemNo, info);
				ProdDetlInfo prodDetlInfo = mProdDetlItemMap.get(info.strItemNo);
				UOrderDInfo uOrderDInfo = new UOrderDInfo();
				uOrderDInfo.nTrxnType = Global.TRXN_DELIVER;
				uOrderDInfo.strItemNo = info.strItemNo;
				uOrderDInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
				uOrderDInfo.nSales = 0;
				if( prodDetlInfo != null)
				{
					uOrderDInfo.strProdGroup = prodDetlInfo.strProdGrp;
					uOrderDInfo.strShortDesc = prodDetlInfo.strShortDesc;
					uOrderDInfo.strDescription = prodDetlInfo.strDescription;
					uOrderDInfo.nPrice = prodDetlInfo.nPrice;
					uOrderDInfo.nGrossPrice = prodDetlInfo.nGrossPrice;
					uOrderDInfo.nOrgPrice = prodDetlInfo.nOrgPrice;
					uOrderDInfo.nOrgGrossPrice = prodDetlInfo.nOrgGrossPrice;
					uOrderDInfo.nDiscountVal = prodDetlInfo.nDiscountVal;
					uOrderDInfo.nPromoType = prodDetlInfo.nPromoType;
					uOrderDInfo.nPromoDiscount = prodDetlInfo.nPromoDiscount;
					uOrderDInfo.nBasePrice = Utils.getDividedValue(prodDetlInfo.prodLocnInfo.nBasePrice);
					uOrderDInfo.nLocnNo = prodDetlInfo.prodLocnInfo.nLocnNo;
					uOrderDInfo.strFullCase = prodDetlInfo.prodLocnInfo.strFullCase;
					uOrderDInfo.nSCWType = prodDetlInfo.prodLocnInfo.nSCWType;
					if(prodDetlInfo != null && prodDetlInfo.prodLocnInfo != null)
					{
						uOrderDInfo.nCaseFactor = prodDetlInfo.prodLocnInfo.nCaseFactor;
					}
					for(UPromotionInfo uPromotionInfo : prodDetlInfo.uPromotionInfoList)
					{
						UPromotionInfo uItem = uPromotionInfo.duplicate();
						uItem.nTrxnType = (mIsPickup) ? Global.TRXN_PICKUP : Global.TRXN_DELIVER;
						uItem.nCustNo = Global.gCurCustomerInfo.nCustNo;
						uItem.nChainNo = Global.gCurCustomerInfo.nChainNo;
						uItem.strItemNo = info.strItemNo;
						uOrderDInfo.uPromotionInfoList.add(uItem);
					}
					dataList.add(uOrderDInfo);
				}
			}
			if(dataList.size() > 0)
			{
				return dataList;
			}
			else
			{
				return null;
			}
		}

		if(orderHistoryInfoList == null || orderHistoryInfoList.size() == 0) {
			if (dataList.size() > 0) {
				return dataList;
			} else {
				return null;
			}
		}

		Map<String, OrderHistoryInfo> orderMap = new HashMap<>();
		for(OrderHistoryInfo info : orderHistoryInfoList)
		{
			if(orderMap.get(info.strItemNo) != null) continue;
			if( mIsPickup && info.nBBQty == 0 ) continue;
			if( !mIsPickup && info.nSAQty == 0 ) continue;

			orderMap.put(info.strItemNo, info);
			ProdDetlInfo prodDetlInfo = mProdDetlItemMap.get(info.strItemNo);
			UOrderDInfo uOrderDInfo = new UOrderDInfo();
			uOrderDInfo.nTrxnType = Global.TRXN_DELIVER;
			uOrderDInfo.strItemNo = info.strItemNo;
			uOrderDInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
			uOrderDInfo.nSales = 0;
			if( prodDetlInfo != null )
			{
				uOrderDInfo.strProdGroup = prodDetlInfo.strProdGrp;
				uOrderDInfo.strShortDesc = prodDetlInfo.strShortDesc;
				uOrderDInfo.strDescription = prodDetlInfo.strDescription;
				uOrderDInfo.nPrice = prodDetlInfo.nPrice;
				uOrderDInfo.nGrossPrice = prodDetlInfo.nGrossPrice;
				uOrderDInfo.nOrgPrice = prodDetlInfo.nOrgPrice;
				uOrderDInfo.nOrgGrossPrice = prodDetlInfo.nOrgGrossPrice;
				uOrderDInfo.nDiscountVal = prodDetlInfo.nDiscountVal;
				uOrderDInfo.nPromoType = prodDetlInfo.nPromoType;
				uOrderDInfo.nPromoDiscount = prodDetlInfo.nPromoDiscount;
				uOrderDInfo.nBasePrice = Utils.getDividedValue(prodDetlInfo.prodLocnInfo.nBasePrice);
				uOrderDInfo.nLocnNo = prodDetlInfo.prodLocnInfo.nLocnNo;
				uOrderDInfo.strFullCase = prodDetlInfo.prodLocnInfo.strFullCase;
				uOrderDInfo.nSCWType = prodDetlInfo.prodLocnInfo.nSCWType;
				if(prodDetlInfo != null && prodDetlInfo.prodLocnInfo != null)
				{
					uOrderDInfo.nCaseFactor = prodDetlInfo.prodLocnInfo.nCaseFactor;
				}
				for(UPromotionInfo uPromotionInfo : prodDetlInfo.uPromotionInfoList)
				{
					UPromotionInfo uItem = uPromotionInfo.duplicate();
					uItem.nTrxnType = (mIsPickup) ? Global.TRXN_PICKUP : Global.TRXN_DELIVER;
					uItem.nCustNo = Global.gCurCustomerInfo.nCustNo;
					uItem.nChainNo = Global.gCurCustomerInfo.nChainNo;
					uItem.strItemNo = info.strItemNo;
					uOrderDInfo.uPromotionInfoList.add(uItem);
				}
				dataList.add(uOrderDInfo);
			}
		}
		return dataList;
	}

	private void removeUnAuthorizedItem()
	{
		if( mAuthHeaderInfo != null)
		{
			List<UOrderDInfo> delList = new ArrayList<>();
			for(UOrderDInfo info : mUOrderDList)
			{
				if(!isAuthorizedItem(info.strItemNo))
				{
					delList.add(info);
				}
			}
			for(UOrderDInfo info : delList)
			{
				mUOrderDList.remove(info);
			}
		}
	}

	private boolean isAuthorizedItem(String strItem)
	{
		if(mAuthHeaderInfo == null)
		{
			return true;
		}

		if(mAuthHeaderInfo.strAuthType.equals("A"))
		{
			AuthDetInfo authDetInfo = mAuthItemMap.get(strItem);
			if( authDetInfo != null )
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		else if(mAuthHeaderInfo.strAuthType.equals("U"))
		{
			AuthDetInfo authDetInfo = mAuthItemMap.get(strItem);
			if( authDetInfo != null )
			{
				return false;
			}
			else
			{
				return true;
			}
		}
		else
		{
			return true;
		}
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

	private void SortData()
	{
		synchronized (mUOrderDList) {
			Collections.sort(mUOrderDList, (t1, t2) -> {
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
	}

	@Override
	public void refreshPage() {
		if(bReverseMode) return;
		showProgressDialog(Str.Processing[Global.LANG]);
		new Thread()
		{
			@Override
			public void run() {
				final List<UOrderDInfo> uOrderDList = new ArrayList<>();
				if (mUOrderDList.size() > 0) {
					if(!mIsPickup)
					{
						SortData();
						for(UOrderDInfo uOrderDInfo: mUOrderDList)
						{
							if(Utils.getProdLoconSCWType(mContext, uOrderDInfo.strItemNo) == Global.WEIGHT_ITEM)
							{
								if(filter && uOrderDInfo.dSales == 0) continue;
							}
							else
							{
								if(filter && uOrderDInfo.nSales == 0) continue;
							}

							if(bFreeItem)
							{
								if(uOrderDInfo.nTrxnType == Global.TRXN_SAMPLE)
								{
									uOrderDList.add(uOrderDInfo);
								}
							}
							else
							{
								if(uOrderDInfo.nTrxnType == 0)
								{
									uOrderDInfo.nTrxnType = Global.TRXN_DELIVER;
								}
								uOrderDList.add(uOrderDInfo);
							}
						}
					}
					else
					{
						SortData();
						for(UOrderDInfo uOrderDInfo: mUOrderDList)
						{
							if(Utils.getProdLoconSCWType(mContext, uOrderDInfo.strItemNo) == Global.WEIGHT_ITEM)
							{
								if(filter && uOrderDInfo.dDumps == 0) continue;
							}
							else
							{
								if(filter && uOrderDInfo.nDumps == 0) continue;
							}

							uOrderDList.add(uOrderDInfo);
						}
					}

				}

				int nCartons = 0;
				if(!TextUtils.isEmpty(strCartonDescription))
				{
					for(UOrderDInfo uOrderDInfo : mUOrderDList)
					{
						if(Utils.getProdLoconSCWType(mContext, uOrderDInfo.strItemNo) != Global.WEIGHT_ITEM)
						{
							if(mIsPickup)
							{
								//nCartons += uOrderDInfo.nDumps / getCaseValue(uOrderDInfo.strItemNo);  --Changed from Mr.Malcolm's request 2023.03.30
								nCartons += uOrderDInfo.nDumps;
							}
							else
							{
								//nCartons += uOrderDInfo.nSales / getCaseValue(uOrderDInfo.strItemNo); -- Changed from Mr. Malcolm's request 2023.03.30
								nCartons += uOrderDInfo.nSales;
							}
						}
					}
				}
				else
				{
					new Handler(Looper.getMainLooper()).post(() -> {
						mRootView.findViewById(R.id.lblCartons).setVisibility(View.GONE);
						mRootView.findViewById(R.id.txtCartons).setVisibility(View.INVISIBLE);
						calculateTotal();
					});
				}
				final int finalNCartons = nCartons;
				new Handler(Looper.getMainLooper()).post(() -> {
					if(!TextUtils.isEmpty(strCartonDescription))
					{
						mRootView.findViewById(R.id.lblCartons).setVisibility(View.VISIBLE);
						mRootView.findViewById(R.id.txtCartons).setVisibility(View.VISIBLE);
						((TextView)mRootView.findViewById(R.id.txtCartons)).setText(String.valueOf(finalNCartons));
					}
					else
					{
						mRootView.findViewById(R.id.lblCartons).setVisibility(View.GONE);
						mRootView.findViewById(R.id.txtCartons).setVisibility(View.INVISIBLE);
					}

					if(uOrderDList.size() > 0)
					{
						mListView.setVisibility(View.VISIBLE);
						mSlideListView.setVisibility(View.VISIBLE);
						mTxtNoData.setVisibility(View.GONE);
						mTxtSlideNoData.setVisibility(View.GONE);

						mListAdapter.setListData(uOrderDList);
						mListAdapter.notifyDataSetChanged();

						mSlideAdapter.setListData(uOrderDList);
						mSlideAdapter.notifyDataSetChanged();
					}
					else
					{
						mListView.setVisibility(View.GONE);
						mSlideListView.setVisibility(View.GONE);
						mTxtNoData.setVisibility(View.VISIBLE);
						mTxtSlideNoData.setVisibility(View.VISIBLE);

						mListAdapter.setListData(uOrderDList);
						mListAdapter.notifyDataSetChanged();

						mSlideAdapter.setListData(uOrderDList);
						mSlideAdapter.notifyDataSetChanged();
					}
					calculateTotal();
					hideProgressDialog();
				});
			}
		}.start();

	}

	public void calculateTotal()
	{
		float dTotal = 0.0f;
		float dTaxAmount = 0.0f;
		List<UOrderDInfo> deliveryList = new ArrayList<>();
		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		if( Global.gUOrderHInfo != null )
		{
			if( !mIsPickup )
			{
				if(Global.gUOrderHInfo.pickupList != null)
				{
					deliveryList.addAll(Global.gUOrderHInfo.pickupList);
				}
			}
			else
			{
				if(Global.gUOrderHInfo.deliveryList != null)
				{
					deliveryList.addAll(Global.gUOrderHInfo.deliveryList);
				}
			}

			if( deliveryList != null && deliveryList.size() > 0 )
			{
				for (UOrderDInfo info : deliveryList) {

					//Calculation Tax Amount
					List<TaxRatesInfo> taxRatesInfoList = dbAdapter.getTaxRatesInfoList(Global.gCurCustomerInfo.strTaxCode, info.strItemNo);
					ProdDetlInfo prodDetlInfo = dbAdapter.getProdDetl(info.strItemNo);
					info.nTrxnNo = Global.gUOrderHInfo.nTrxnNo;
					info.nTaxAmount = 0;
					info.nPrintTaxAmount = 0;
					boolean bAppliedGST = false;
					float fGSTOnCDS = 0.0f;
					if(taxRatesInfoList != null)
					{
						for(TaxRatesInfo taxRatesInfo : taxRatesInfoList)
						{
							float nTaxAmount = 0;
							float nPrintTaxAmount = 0;

							float nAmount;
							if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
							{
								if(mIsPickup)
								{
									nAmount = (float)info.dSales;
								}
								else
								{
									nAmount = (float)info.dDumps;
								}
							}
							else
							{
								if(mIsPickup)
								{
									nAmount = info.nSales;
								}
								else
								{
									nAmount = info.nDumps;
								}
							}

							if(taxRatesInfo.nTaxMethod == 1) //Applying Cents Amount
							{
								nTaxAmount = Utils.getDividedValue(taxRatesInfo.nTaxRate) * nAmount * prodDetlInfo.nConsumerUnit;
								nPrintTaxAmount = Utils.getDividedValue(taxRatesInfo.nTaxRate) * nAmount * prodDetlInfo.nConsumerUnit;
							}
							else if( taxRatesInfo.nTaxMethod == 2 )
							{
								nTaxAmount = (nAmount * (info.nBasePrice - info.nDiscountVal) + info.nTaxAmount) / 100 * Utils.getDividedValue(taxRatesInfo.nTaxRate) ;
								nPrintTaxAmount = (nAmount * (info.nBasePrice) + info.nPrintTaxAmount) / 100 * Utils.getDividedValue(taxRatesInfo.nTaxRate) ;
							}

							if(taxRatesInfo.strTaxType.equals("S"))
							{
								List<TaxRatesInfo> gstOnCDSRatesInfoList = dbAdapter.getTaxRatesInfoList(Global.gCurCustomerInfo.strTaxCode, taxRatesInfo.strTaxRateNumber);
								for(TaxRatesInfo gstOnCDSRatesInfo : gstOnCDSRatesInfoList)
								{
									if(gstOnCDSRatesInfo.nTaxMethod == 2 && gstOnCDSRatesInfo.strTaxRateCode.equals("GST") )
									{
										fGSTOnCDS += Utils.getDividedValue(taxRatesInfo.nTaxRate) / 100 * Utils.getDividedValue(gstOnCDSRatesInfo.nTaxRate) * nAmount * prodDetlInfo.nConsumerUnit;
									}
								}
							}
							info.nTaxAmount += Utils.get5RoundedValue(mContext, nTaxAmount);
							info.nPrintTaxAmount += Utils.get5RoundedValue(mContext, nPrintTaxAmount);
							if(taxRatesInfo.strTaxRateCode.equals("GST"))
							{
								bAppliedGST = true;
							}
						}
					}

					if(!bAppliedGST)
					{
						info.nTaxAmount += Utils.get5RoundedValue(mContext, fGSTOnCDS);
						info.nPrintTaxAmount += Utils.get5RoundedValue(mContext, fGSTOnCDS);
					}

					if(!mIsPickup){
						dTotal -= (info.nDumps * 1.0f )* info.nBasePrice;
						dTaxAmount -= info.nPrintTaxAmount;
					}
					else
					{
						dTotal += (info.nSales * 1.0f) * info.nBasePrice;
						dTaxAmount += info.nPrintTaxAmount;
					}
				}
			}
		}

		if( mUOrderDList == null || mUOrderDList.size() == 0 )
		{
			dbAdapter.close();
			dTotal += dTaxAmount;
			dTotal = Utils.get2DecimalValue(dTotal);
			if( dTotal >= 0.0f )
			{
				((TextView)mRootView.findViewById(R.id.txtOrderTotalValue)).setTextColor(mContext.getResources().getColor(R.color.black_color));
			}
			else
			{
				((TextView)mRootView.findViewById(R.id.txtOrderTotalValue)).setTextColor(mContext.getResources().getColor(R.color.red_color));
			}
			((TextView)mRootView.findViewById(R.id.txtOrderTotalValue)).setText(String.format(Locale.ENGLISH, "$%.2f", dTotal));

			return;
		}

		deliveryList.clear();
		if(mUOrderDList != null)
		{
			deliveryList.addAll(mUOrderDList);
		}
		for (UOrderDInfo info : deliveryList) {
			//Calculation Tax Amount
			List<TaxRatesInfo> taxRatesInfoList = dbAdapter.getTaxRatesInfoList(Global.gCurCustomerInfo.strTaxCode, info.strItemNo);
			ProdDetlInfo prodDetlInfo = dbAdapter.getProdDetl(info.strItemNo);
			info.nTrxnNo = Global.gUOrderHInfo.nTrxnNo;
			info.nTaxAmount = 0;
			info.nPrintTaxAmount = 0;
			boolean bAppliedGST = false;
			float fGSTOnCDS = 0.0f;
			for(TaxRatesInfo taxRatesInfo : taxRatesInfoList)
			{
				float nTaxAmount = 0;
				float nPrintTaxAmount = 0;
				float nAmount;
				if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
				{
					if(!mIsPickup)
					{
						nAmount = (float)info.dSales;
					}
					else
					{
						nAmount = (float)info.dDumps;
					}
				}
				else {
					if(!mIsPickup)
					{
						nAmount = info.nSales;
					}
					else
					{
						nAmount = info.nDumps;
					}
				}

				if(taxRatesInfo.nTaxMethod == 1) //Applying Cents Amount
				{
					if( mIsPickup )
					{
						nTaxAmount = Utils.getDividedValue(taxRatesInfo.nTaxRate) * info.nDumps * prodDetlInfo.nConsumerUnit;
						nPrintTaxAmount = Utils.getDividedValue(taxRatesInfo.nTaxRate) * info.nDumps * prodDetlInfo.nConsumerUnit;
					}
					else
					{
						nTaxAmount = Utils.getDividedValue(taxRatesInfo.nTaxRate) * info.nSales * prodDetlInfo.nConsumerUnit;
						nPrintTaxAmount = Utils.getDividedValue(taxRatesInfo.nTaxRate) * info.nSales * prodDetlInfo.nConsumerUnit;
					}

				}
				else if( taxRatesInfo.nTaxMethod == 2 )
				{
					nTaxAmount = (nAmount * (info.nBasePrice - info.nDiscountVal) + info.nTaxAmount) / 100 * Utils.getDividedValue(taxRatesInfo.nTaxRate);
					nPrintTaxAmount = (nAmount * (info.nBasePrice) + info.nTaxAmount) / 100 * Utils.getDividedValue(taxRatesInfo.nTaxRate);
				}

				if(taxRatesInfo.strTaxType.equals("S"))
				{
					List<TaxRatesInfo> gstOnCDSRatesInfoList = dbAdapter.getTaxRatesInfoList(Global.gCurCustomerInfo.strTaxCode, taxRatesInfo.strTaxRateNumber);
					for(TaxRatesInfo gstOnCDSRatesInfo : gstOnCDSRatesInfoList)
					{
						if(gstOnCDSRatesInfo.nTaxMethod == 2 && gstOnCDSRatesInfo.strTaxRateCode.equals("GST") )
						{
							fGSTOnCDS += Utils.getDividedValue(taxRatesInfo.nTaxRate) / 100 * Utils.getDividedValue(gstOnCDSRatesInfo.nTaxRate) * nAmount * prodDetlInfo.nConsumerUnit;;
						}
					}
				}

				info.nTaxAmount += Utils.get5RoundedValue(mContext, nTaxAmount);
				info.nPrintTaxAmount += Utils.get5RoundedValue(mContext, nPrintTaxAmount);

				if(taxRatesInfo.strTaxRateCode.equals("GST"))
				{
					bAppliedGST = true;
				}
			}
			if(!bAppliedGST)
			{
				info.nTaxAmount += Utils.get5RoundedValue(mContext, fGSTOnCDS);
				info.nPrintTaxAmount += Utils.get5RoundedValue(mContext, fGSTOnCDS);
			}

			if(mIsPickup){
				dTotal -= ((info.nDumps *1.0f) * info.nBasePrice);
				dTaxAmount -= info.nPrintTaxAmount;
			}
			else
			{
				dTotal += (info.nSales * 1.0f) * info.nBasePrice;
				dTaxAmount += info.nPrintTaxAmount;
			}
		}
		dbAdapter.close();
		dTotal += dTaxAmount;
		dTotal = Utils.get2DecimalValue(dTotal);
		if( dTotal >= 0 )
		{
			((TextView)mRootView.findViewById(R.id.txtOrderTotalValue)).setTextColor(mContext.getResources().getColor(R.color.black_color));
		}
		else
		{
			((TextView)mRootView.findViewById(R.id.txtOrderTotalValue)).setTextColor(mContext.getResources().getColor(R.color.red_color));
		}
		((TextView)mRootView.findViewById(R.id.txtOrderTotalValue)).setText(String.format(Locale.ENGLISH, "$%.2f", dTotal));
	}

	@Override
	public void refreshLanguage() {
		((TextView) mRootView.findViewById(R.id.lblTitle)).setText(mIsPickup == false ? Str.Sale_Entry[Global.LANG].toUpperCase() : Str.ReturnEntry[Global.LANG].toUpperCase());
		((TextView) mRootView.findViewById(R.id.txtQty)).setText(Str.Qty[Global.LANG]);
		((TextView) mRootView.findViewById(R.id.txtDescription)).setText("");
		((Button) mRootView.findViewById(R.id.btnAdd)).setText(mIsPickup == false ? Str.SaleBtn[Global.LANG] : Str.ReturnBtn[Global.LANG]);
		((Button) mRootView.findViewById(R.id.btnCompleted)).setText(Str.Completed[Global.LANG]);
		((TextView) mRootView.findViewById(R.id.txtOrderTotal)).setText(Str.OrderTotalIncGST[Global.LANG]);
		mTxtNoData.setText(Str.No_data_has_been_found[Global.LANG]);
		mTxtSlideNoData.setText(Str.No_data_has_been_found[Global.LANG]);
		if( !TextUtils.isEmpty(strCartonDescription))
		{
			//((TextView)mRootView.findViewById(R.id.lblCartons)).setText(String.format(Locale.ENGLISH, Str.TotalCartonCount[Global.LANG], strCartonDescription));
			((TextView)mRootView.findViewById(R.id.lblCartons)).setText(Str.Count[Global.LANG]);
		}
	}

	private void doProcessSelectProductForScan(String strItemNo, int nQty, boolean bUnit, boolean bUpdateItemNo)
	{
		bProcessingBarcodeScan = true;
		((EditText) mRootView.findViewById(R.id.editItemNo)).setText(strItemNo);
		doSearch(false);

		if(Utils.getStringSetting(mContext, Global.PREF_AUTO_SCAN_ADD).equals("1"))
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
			if(mSelProductInfo != null && mSelProductInfo.strItemNo != null && (Utils.getProdLoconSCWType(mContext, mSelProductInfo.strItemNo) == Global.WEIGHT_ITEM))
			{
				showKeyboard(((EditText) mRootView.findViewById(R.id.editQty)), true);
			}
			else
			{
				if(bShowCase)
				{
					if( Global.gCurCustomerInfo.strSalEntryMode.equals("B") || Global.gCurCustomerInfo.strSalEntryMode.equals("C"))
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
			bProcessingBarcodeScan = false;
		}
	}

	private void doProcessSelectProduct(String strItemNo, int nQty, boolean bUnit, boolean bUpdateItemNo, boolean isSearchByItemNo)
	{
		if (strItemNo.length() == 0) {
			Utils.printDebug(mContext, Str.NoProductFound[Global.LANG]);
		} else {

			ProdDetlInfo selInfo = mProdDetlItemMap.get(strItemNo);

			if( !mIsPickup )
			{
				if( selInfo != null && selInfo.strSalesAllowed.toUpperCase().equals("Y") )
				{
					//if(bUpdateItemNo)
					{
						((EditText) mRootView.findViewById(R.id.editItemNo)).setText(strItemNo);
					}
					if(Utils.getProdLoconSCWType(mContext, strItemNo) == Global.WEIGHT_ITEM)
					{
						mRootView.findViewById(R.id.editCase).setVisibility(View.GONE);
						mRootView.findViewById(R.id.lblSep).setVisibility(View.GONE);
						mRootView.findViewById(R.id.editCase).setTag("From App");
						((EditText) mRootView.findViewById(R.id.editQty)).setText(String.format(Locale.ENGLISH, "%.2f", nQty * 1f));
						mRootView.findViewById(R.id.editCase).setTag(null);
					}
					else
					{
						if(bShowCase)
						{
							if( Global.gCurCustomerInfo.strSalEntryMode.equals("B") || Global.gCurCustomerInfo.strSalEntryMode.equals("C"))
							{
								int nCase = getCaseValue(strItemNo);
								int nCaseValue = nQty / nCase;
								int nQtyValue = nQty % nCase;
								mRootView.findViewById(R.id.editCase).setVisibility(View.VISIBLE);
								mRootView.findViewById(R.id.lblSep).setVisibility(View.VISIBLE);
								if(nQty != 0)
								{
									((EditText) mRootView.findViewById(R.id.editCase)).setText(String.valueOf(nCaseValue));
									((EditText) mRootView.findViewById(R.id.editQty)).setText(String.valueOf(nQtyValue));
								}
								else
								{
									((EditText) mRootView.findViewById(R.id.editCase)).setText("");
									((EditText) mRootView.findViewById(R.id.editQty)).setText("");
								}

								if(bUnit)
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
								if(nQty != 0)
								{
									((EditText) mRootView.findViewById(R.id.editQty)).setText(String.valueOf(nQty));
								}
								else
								{
									((EditText) mRootView.findViewById(R.id.editQty)).setText("");
								}

								this.showKeyboard((EditText) mRootView.findViewById(R.id.editQty), true);
							}
						}
						else
						{
							if(nQty != 0)
							{
								((EditText) mRootView.findViewById(R.id.editQty)).setText(String.valueOf(nQty));
							}
							else
							{
								((EditText) mRootView.findViewById(R.id.editQty)).setText("");
							}

							this.showKeyboard((EditText) mRootView.findViewById(R.id.editQty), true);
						}
					}
				}
				else
				{
					try {
						String strMessage = Str.ThisItemIsNotAvailableForSales[Global.LANG] + "\n";
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
			else
			{
				if( selInfo != null && selInfo.strDumpsAllowed.toUpperCase().equals("Y") )
				{
					//if(bUpdateItemNo)
					{
						((EditText) mRootView.findViewById(R.id.editItemNo)).setText(strItemNo);
					}
					if(Utils.getProdLoconSCWType(mContext, strItemNo) == Global.WEIGHT_ITEM)
					{
						mRootView.findViewById(R.id.editCase).setVisibility(View.GONE);
						mRootView.findViewById(R.id.lblSep).setVisibility(View.GONE);
						mRootView.findViewById(R.id.editCase).setTag("From App");
						((EditText) mRootView.findViewById(R.id.editQty)).setText(String.format(Locale.ENGLISH, "%.2f", nQty * 1f));
						mRootView.findViewById(R.id.editCase).setTag(null);
					}
					else
					{
						if(bShowCase)
						{
							if( Global.gCurCustomerInfo.strSalEntryMode.equals("B") ||Global.gCurCustomerInfo.strSalEntryMode.equals("C"))
							{
								int nCase = getCaseValue(strItemNo);
								int nCaseValue = nQty / nCase;
								int nQtyValue = nQty % nCase;
								mRootView.findViewById(R.id.editCase).setVisibility(View.VISIBLE);
								mRootView.findViewById(R.id.lblSep).setVisibility(View.VISIBLE);
								if(nQty != 0)
								{
									((EditText) mRootView.findViewById(R.id.editCase)).setText(String.valueOf(nCaseValue));
									((EditText) mRootView.findViewById(R.id.editQty)).setText(String.valueOf(nQtyValue));
								}
								else
								{
									((EditText) mRootView.findViewById(R.id.editCase)).setText("");
									((EditText) mRootView.findViewById(R.id.editQty)).setText("");
								}

								if( bUnit)
								{
									this.showKeyboard((EditText) mRootView.findViewById(R.id.editQty), true);
								}
								else
								{
									this.showKeyboard((EditText) mRootView.findViewById(R.id.editCase), true);
								}
							}
							else if(Global.gCurCustomerInfo.strSalEntryMode.equals("") || Global.gCurCustomerInfo.strSalEntryMode.equals("U"))
							{
								if(nQty != 0)
								{
									((EditText) mRootView.findViewById(R.id.editQty)).setText(String.valueOf(nQty));
								}
								else
								{
									((EditText) mRootView.findViewById(R.id.editQty)).setText("");
								}
								this.showKeyboard((EditText) mRootView.findViewById(R.id.editQty), true);
							}
						}
						else
						{
							if(nQty != 0)
							{
								((EditText) mRootView.findViewById(R.id.editQty)).setText(String.valueOf(nQty));
							}
							else
							{
								((EditText) mRootView.findViewById(R.id.editQty)).setText("");
							}
							this.showKeyboard((EditText) mRootView.findViewById(R.id.editQty), true);
						}
					}
				}
				else
				{
					try {
						String strMessage = Str.ThisItemIsNotAvailableForReturns[Global.LANG] + "\n";
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
		}
		m_bModified = true;
		doSearch(isSearchByItemNo);
		if(mSelProductInfo != null && mSelProductInfo.strItemNo != null && (Utils.getProdLoconSCWType(mContext, mSelProductInfo.strItemNo) == Global.WEIGHT_ITEM))
		{
			showKeyboard(((EditText) mRootView.findViewById(R.id.editQty)), true);
		}
		else
		{
			if(bShowCase)
			{
				if( Global.gCurCustomerInfo.strSalEntryMode.equals("B") || Global.gCurCustomerInfo.strSalEntryMode.equals("C"))
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

		Fragment fragment = mContext.getCurrentFragment();
		if(fragment != this) return;
		if (strAction.equals(Global.ACTION_SELECT_PRODUCT) == true) {
			int nType = intent.getIntExtra(Global.EXTRA_TYPE, 0);

			String strItemNo = "";
			int nQty = 0;
			if (nType == Global.SELECT_PRODUCT_ITEMNO) {
				nQty = 0;
				strItemNo = intent.getStringExtra(Global.EXTRA_ITEM_NO);
				String finalStrItemNo = strItemNo;
				int finalNQty = nQty;
				boolean finalBUnit = bUnit;

				//that is for android 8.1 device.
				new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
					@Override
					public void run() {
						doProcessSelectProduct(finalStrItemNo, finalNQty, finalBUnit, true, true);
					}
				}, 100);
			}
			else if (nType == Global.SELECT_PRODUCT_ITEMUPC) {
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
			}
		}
		else if (strAction.equals(Global.ACTION_TAKE_PHOTO) == true) {
			mStrPhotoPath = intent.getStringExtra(Global.EXTRA_PHOTO_PATH);
		}
		else if( strAction.equals(Global.ACTION_ORDER_HISTORY) == true && mIsPickup == false){
			new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
				@Override
				public void run() {
					List<UOrderDInfo> orderHistoryDInfoList = new ArrayList<>();
					for(OrderHistoryInfo info : Global.gOrderHistoryResultInfoList)
					{
						ProdDetlInfo prodDetlInfo = mProdDetlItemMap.get(info.strItemNo);
						if( info.nSAQty > 0 && prodDetlInfo != null)
						{
							UOrderDInfo uOrderDInfo = new UOrderDInfo();
							uOrderDInfo.nTrxnType = Global.TRXN_DELIVER;
							if(mAuthHeaderInfo != null && !isAuthorizedItem(info.strItemNo))
							{
								continue;
							}
							uOrderDInfo.strItemNo = info.strItemNo;
							uOrderDInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
							if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
							{
								uOrderDInfo.dSales = info.nSAQty / 100f;
								uOrderDInfo.dPlanQty = uOrderDInfo.dSales;
							}
							else
							{
								uOrderDInfo.nSales = info.nSAQty / 100;
								uOrderDInfo.nPlanQty = uOrderDInfo.nSales;
							}

							uOrderDInfo.strProdGroup = prodDetlInfo.strProdGrp;
							uOrderDInfo.strShortDesc = prodDetlInfo.strShortDesc;
							uOrderDInfo.strDescription = prodDetlInfo.strDescription;
							uOrderDInfo.nPrice = prodDetlInfo.nPrice;
							uOrderDInfo.nGrossPrice = prodDetlInfo.nGrossPrice;
							uOrderDInfo.nOrgPrice = prodDetlInfo.nOrgPrice;
							uOrderDInfo.nOrgGrossPrice = prodDetlInfo.nOrgGrossPrice;
							uOrderDInfo.nDiscountVal = prodDetlInfo.nDiscountVal;
							uOrderDInfo.nPromoType = prodDetlInfo.nPromoType;
							uOrderDInfo.nPromoDiscount = prodDetlInfo.nPromoDiscount;
							uOrderDInfo.nBasePrice = Utils.getDividedValue(prodDetlInfo.prodLocnInfo.nBasePrice);
							uOrderDInfo.nLocnNo = prodDetlInfo.prodLocnInfo.nLocnNo;
							uOrderDInfo.strFullCase = prodDetlInfo.prodLocnInfo.strFullCase;
							uOrderDInfo.nSCWType = prodDetlInfo.prodLocnInfo.nSCWType;
							if(prodDetlInfo != null && prodDetlInfo.prodLocnInfo != null)
							{
								uOrderDInfo.nCaseFactor = prodDetlInfo.prodLocnInfo.nCaseFactor;
							}
							for(UPromotionInfo uPromotionInfo : prodDetlInfo.uPromotionInfoList)
							{
								UPromotionInfo uItem = uPromotionInfo.duplicate();
								uItem.nTrxnType = (mIsPickup) ? Global.TRXN_PICKUP : Global.TRXN_DELIVER;
								uItem.nCustNo = Global.gCurCustomerInfo.nCustNo;
								uItem.nChainNo = Global.gCurCustomerInfo.nChainNo;
								uItem.strItemNo = info.strItemNo;
								uOrderDInfo.uPromotionInfoList.add(uItem);
							}
							m_bModified = true;
							orderHistoryDInfoList.add(uOrderDInfo);
						}
					}
					if( orderHistoryDInfoList.size() > 0 )
					{
						if(mStrOrderNo.equals("0") || mStrOrderNo.equals(""))
						{
							//Merge OrderHistory and Current UOrderList
							//Mapping Current UOrderDList
							Map<String, UOrderDInfo> map = new HashMap<>();
							for(UOrderDInfo uOrderDInfo : mUOrderDList)
							{
								map.put(uOrderDInfo.strItemNo, uOrderDInfo);
							}
							for(UOrderDInfo historyInfo : orderHistoryDInfoList)
							{
								UOrderDInfo uOrderDInfo = map.get(historyInfo.strItemNo);
								if( uOrderDInfo != null )
								{
									if(Utils.getProdLoconSCWType(mContext, uOrderDInfo.strItemNo) == Global.WEIGHT_ITEM)
									{
										uOrderDInfo.dPlanQty = historyInfo.nPlanQty * 1.0f;
										uOrderDInfo.dSales = historyInfo.dSales;
									}
									else
									{
										uOrderDInfo.nPlanQty = historyInfo.nPlanQty;
										uOrderDInfo.nSales = historyInfo.nSales;
									}
								}
								else
								{
									mUOrderDList.add(historyInfo);
								}
							}
							refreshPage();
						}
						else
						{
							mUOrderDList.clear();
							mUOrderDList.addAll(orderHistoryDInfoList);
							refreshPage();
						}
					}
				}
			}, 100);

		}
		else if (strAction.equals(Global.ACTION_REFRESH_STOCK_VALUE) == true) {
			mStockMap.clear();
			mStockMapD.clear();
		}
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

	private void doSearch(boolean isByItemNo) {
		String strItemNo = getItemCode(((EditText) mRootView.findViewById(R.id.editItemNo)).getText().toString());
		String strDescription = Str.Invalid_Product[Global.LANG];
		((TextView)mRootView.findViewById(R.id.txtPriceInv)).setText("");
		int nQty = 0;
		double dQty = 0;
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

			mSelIdx = -1;
			if(bFreeItem)
			{
				for (int i = 0; i < mUOrderDList.size(); i++) {
					UOrderDInfo info = mUOrderDList.get(i);
					if (info.strItemNo.equals(strItemNo) == true && ( info.nTrxnType == Global.TRXN_FREE || info.nTrxnType == Global.TRXN_SAMPLE)) {
						nQty = mIsPickup == false ? info.nSales : info.nDumps;
						dQty = mIsPickup == false ? info.dSales : info.dDumps;
						mSelIdx = i;
						break;
					}
				}
			}
			else
			{
				for (int i = 0; i < mUOrderDList.size(); i++) {
					UOrderDInfo info = mUOrderDList.get(i);
					if (info.strItemNo.equals(strItemNo) == true && info.nTrxnType == Global.TRXN_DELIVER) {
						nQty = mIsPickup == false ? info.nSales : info.nDumps;
						dQty = mIsPickup == false ? info.dSales : info.dDumps;
						mSelIdx = i;
						break;
					}
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
			mRootView.findViewById(R.id.editQty).setTag("From App");
			if(mEntryInputMethod.equals("A"))
			{
				((EditText) mRootView.findViewById(R.id.editQty)).setText("0.00");
			}
			else
			{
				((EditText) mRootView.findViewById(R.id.editQty)).setText(String.format(Locale.ENGLISH, "%.2f", dQty));
			}
			mRootView.findViewById(R.id.editQty).setTag(null);
		}
		else
		{
			if(bShowCase)
			{
				if( Global.gCurCustomerInfo.strSalEntryMode.equals("B") || Global.gCurCustomerInfo.strSalEntryMode.equals("C"))
				{

					int nCase = getCaseValue(strItemNo);
					int nCaseValue = nQty / nCase;
					int nQtyValue = nQty % nCase;
					mRootView.findViewById(R.id.editCase).setVisibility(View.VISIBLE);
					mRootView.findViewById(R.id.lblSep).setVisibility(View.VISIBLE);
					if(nQty != 0)
					{
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
					else
					{
						((EditText) mRootView.findViewById(R.id.editCase)).setText("");
						((EditText) mRootView.findViewById(R.id.editQty)).setText("");
					}
				}
				else if (Global.gCurCustomerInfo.strSalEntryMode.equals("") || Global.gCurCustomerInfo.strSalEntryMode.equals("U"))
				{
					if(nQty != 0)
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
					else
					{
						((EditText) mRootView.findViewById(R.id.editQty)).setText("");
					}
				}
			}
			else
			{
				if(nQty != 0)
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
				else
				{
					((EditText) mRootView.findViewById(R.id.editQty)).setText("");
				}
			}
		}

		showReasonCode();
		showPriceInven();
	}

	private void showReasonCode()
	{
		if( mSelIdx == -1 )
		{
			mRootView.findViewById(R.id.txtReason).setVisibility(View.GONE);
		}
		else
		{
			if(mSelIdx >= mUOrderDList.size())
			{
				mRootView.findViewById(R.id.txtReason).setVisibility(View.GONE);
				return;
			}

			final UOrderDInfo uOrderDInfo = mUOrderDList.get(mSelIdx);
			if( uOrderDInfo.nReasonCode == 0 )
			{
				mRootView.findViewById(R.id.txtReason).setVisibility(View.GONE);
			}
			else
			{
				new Thread()
				{
					@Override
					public void run() {
						boolean bExistDesc = false;
						List<DescTypeInfo> descTypeInfoList = null;
						if( uOrderDInfo.nTrxnType == Global.TRXN_DELIVER && mDeliveryReasonCodeList.size() > 0 )
						{
							bExistDesc = true;
							descTypeInfoList = mDeliveryReasonCodeList;
						}
						else if( uOrderDInfo.nTrxnType == Global.TRXN_FREE && mFreeReasonCodeList.size() > 0 )
						{
							bExistDesc = true;
							descTypeInfoList = mFreeReasonCodeList;
						}
						else if( uOrderDInfo.nTrxnType == Global.TRXN_SAMPLE && mSampleReasonCodeList.size() > 0)
						{
							bExistDesc = true;
							descTypeInfoList = mSampleReasonCodeList;
						}

						if( bExistDesc )
						{
							String strDescription = "";
							for(int i = 0; i < descTypeInfoList.size(); i++)
							{
								if( descTypeInfoList.get(i).nNumericKey == uOrderDInfo.nReasonCode)
								{
									strDescription = descTypeInfoList.get(i).strDescription;
									break;
								}
							}
							final String finalStrDescription = strDescription;
							new Handler(Looper.getMainLooper()).post(new Runnable() {
								@Override
								public void run() {
									if(!TextUtils.isEmpty(finalStrDescription))
									{
										mRootView.findViewById(R.id.txtReason).setVisibility(View.VISIBLE);
										((TextView)mRootView.findViewById(R.id.txtReason)).setText(finalStrDescription);
									}
									else
									{
										mRootView.findViewById(R.id.txtReason).setVisibility(View.GONE);
									}
								}
							});


						}
						else
						{
							new Handler(Looper.getMainLooper()).post(new Runnable() {
								@Override
								public void run() {
									mRootView.findViewById(R.id.txtReason).setVisibility(View.GONE);
								}
							});

						}
					}
				}.start();

			}
		}
	}

	ProdDetlInfo mSelProductInfo = null;
	boolean mIsSimFreeMode = false;
	String mStrSimQty = "0";
	String mStrSimCase = "0";
	ProdDetlInfo mSimSelProductInfo = null;

	private void doAdd() {
		doAdd(false);
	}

	private void doAdd(boolean isSimFreeMode) {
		new Thread()
		{
			@Override
			public void run() {
				if(bReverseMode) return;

				String strQty = ((EditText) mRootView.findViewById(R.id.editQty)).getText().toString();
				String _strCase = ((EditText) mRootView.findViewById(R.id.editCase)).getText().toString();
				boolean _bFreeItem = bFreeItem;
				ProdDetlInfo _selProductInfo = mSelProductInfo;

				if (isSimFreeMode) {
					strQty = mStrSimQty;
					_strCase = mStrSimCase;
					_bFreeItem = true;
					_selProductInfo = mSimSelProductInfo;
				}
				final String strCase = _strCase;
				final boolean isFreeItem = _bFreeItem;
				final ProdDetlInfo selProductInfo = _selProductInfo;

				if (selProductInfo == null) {
					new Handler(Looper.getMainLooper()).post(new Runnable() {
						@Override
						public void run() {
							if(!TextUtils.isEmpty(((EditText) mRootView.findViewById(R.id.editItemNo)).getText().toString()))
							{
								Utils.printDebug(mContext, Str.Please_select_valid_product[Global.LANG]);
							}
							((EditText) mRootView.findViewById(R.id.editQty)).setText("");
							((EditText) mRootView.findViewById(R.id.editCase)).setText("");
							focusAndHideKeyboard((EditText) mRootView.findViewById(R.id.editItemNo));
							bProcessingBarcodeScan = false;
						}
					});
					return;
				}

				if(!futureOrder)
				{
					if( !mIsPickup )
					{
						if(!selProductInfo.strSalesAllowed.toUpperCase().equals("Y"))
						{
							new Handler(Looper.getMainLooper()).post(new Runnable() {
								@Override
								public void run() {
									try {
										String strMessage = Str.ThisItemIsNotAvailableForSales[Global.LANG] + "\n";
										final MessageDialog msgDialog = new MessageDialog(mContext);
										msgDialog.setCancelable(false);
										msgDialog.mStrMessage = strMessage;
										msgDialog.mStrMiddle = Str.Return[Global.LANG];
										msgDialog.setOnDismissListener(new OnDismissListener() {
											@Override
											public void onDismiss(DialogInterface dialog) {
												if(Utils.getStringSetting(mContext, Global.PREF_AUTO_SCAN_ADD).equals("1"))
												{
													((EditText) mRootView.findViewById(R.id.editItemNo)).setText("");
													((EditText) mRootView.findViewById(R.id.editQty)).setText("");
													((EditText) mRootView.findViewById(R.id.editCase)).setText("");
												}
												focusAndHideKeyboard((EditText) mRootView.findViewById(R.id.editItemNo));
											}
										});
										msgDialog.show();
									}catch (Exception e)
									{
										e.printStackTrace();
									}

								}
							});
							bProcessingBarcodeScan = false;
							return;
						}
					}
					else
					{
						if(!selProductInfo.strDumpsAllowed.toUpperCase().equals("Y"))
						{
							new Handler(Looper.getMainLooper()).post(new Runnable() {
								@Override
								public void run() {
									try {
										String strMessage = Str.ThisItemIsNotAvailableForReturns[Global.LANG] + "\n";
										final MessageDialog msgDialog = new MessageDialog(mContext);
										msgDialog.setCancelable(false);
										msgDialog.mStrMessage = strMessage;
										msgDialog.mStrMiddle = Str.Return[Global.LANG];
										msgDialog.setOnDismissListener(new OnDismissListener() {
											@Override
											public void onDismiss(DialogInterface dialog) {
												if(Utils.getStringSetting(mContext, Global.PREF_AUTO_SCAN_ADD).equals("1"))
												{
													((EditText) mRootView.findViewById(R.id.editItemNo)).setText("");
													((EditText) mRootView.findViewById(R.id.editQty)).setText("");
													((EditText) mRootView.findViewById(R.id.editCase)).setText("");
												}
												focusAndHideKeyboard((EditText) mRootView.findViewById(R.id.editItemNo));
											}
										});
										msgDialog.show();
									}catch (Exception e)
									{
										e.printStackTrace();
									}

								}
							});
							bProcessingBarcodeScan = false;
							return;
						}
					}
				}

				if(!isAuthorizedItem(selProductInfo.strItemNo))
				{
					new Handler(Looper.getMainLooper()).post(new Runnable() {
						@Override
						public void run() {
							if(Utils.getStringSetting(mContext, Global.PREF_AUTO_SCAN_ADD).equals("1"))
							{
								((EditText) mRootView.findViewById(R.id.editItemNo)).setText("");
								((EditText) mRootView.findViewById(R.id.editQty)).setText("");
								((EditText) mRootView.findViewById(R.id.editCase)).setText("");
							}
							Utils.printDebug(mContext, Str.ItemNoAuthorized[Global.LANG]);
						}
					});
					bProcessingBarcodeScan = false;
					return;
				}

				int nIdx = -1;


				final int nCase = getCaseValue(selProductInfo.strItemNo);

                List<UOrderDInfo> uOrderDInfoList = mUOrderDList;
                for (int i = 0; i < uOrderDInfoList.size(); i++) {
                    if (uOrderDInfoList.get(i).strItemNo.equals(selProductInfo.strItemNo) == true) {
                        if(isFreeItem && ( uOrderDInfoList.get(i).nTrxnType == Global.TRXN_FREE ||
                                uOrderDInfoList.get(i).nTrxnType == Global.TRXN_SAMPLE))
                        {
                            nIdx = i;
                            break;
                        }
                        else if(!isFreeItem && ( uOrderDInfoList.get(i).nTrxnType != Global.TRXN_FREE && uOrderDInfoList.get(i).nTrxnType != Global.TRXN_SAMPLE))
                        {
                            nIdx = i;
                            break;
                        }
                    }
                }

				if(Utils.getProdLoconSCWType(mContext, selProductInfo.strItemNo) == Global.WEIGHT_ITEM)
				{
					if(nIdx != -1 && strQty.equals("0.00"))
					{
						new Handler(Looper.getMainLooper()).post(new Runnable() {
							@Override
							public void run() {
								Utils.printDebug(mContext, Str.Please_input_the_Quantity[Global.LANG]);
								focusAndHideKeyboard((EditText) mRootView.findViewById(R.id.editItemNo));
							}
						});
						bProcessingBarcodeScan = false;
						return;
					}
				}
				else
				{
					if(bShowCase)
					{
						if( Global.gCurCustomerInfo.strSalEntryMode.equals("B") || Global.gCurCustomerInfo.strSalEntryMode.equals("C"))
						{
							if (strQty.length() == 0) {
								if( strCase.length() == 0 )
								{
									new Handler(Looper.getMainLooper()).post(new Runnable() {
										@Override
										public void run() {
											Utils.printDebug(mContext, Str.Please_input_the_Quantity[Global.LANG]);
											showKeyboard((EditText) mRootView.findViewById(R.id.editQty), true);
										}
									});
									bProcessingBarcodeScan = false;
									return;
								}
								else
								{
									new Handler(Looper.getMainLooper()).post(new Runnable() {
										@Override
										public void run() {
											((EditText) mRootView.findViewById(R.id.editQty)).setText("");
										}
									});

								}
							}
							new Handler(Looper.getMainLooper()).post(new Runnable() {
								@Override
								public void run() {
									mRootView.findViewById(R.id.editCase).setVisibility(View.VISIBLE);
									mRootView.findViewById(R.id.lblSep).setVisibility(View.VISIBLE);
									if( strCase.length() == 0 ){
										((EditText) mRootView.findViewById(R.id.editCase)).setText("");
									}
								}
							});

						}
						else if( Global.gCurCustomerInfo.strSalEntryMode.equals("") || Global.gCurCustomerInfo.strSalEntryMode.equals("U"))
						{
							if (strQty.length() == 0)
							{
								new Handler(Looper.getMainLooper()).post(new Runnable() {
									@Override
									public void run() {
										Utils.printDebug(mContext, Str.Please_input_the_Quantity[Global.LANG]);
										showKeyboard((EditText) mRootView.findViewById(R.id.editQty), true);
									}
								});
								bProcessingBarcodeScan = false;
								return;
							}
						}
					}
					else
					{
						if (strQty.length() == 0)
						{
							new Handler(Looper.getMainLooper()).post(new Runnable() {
								@Override
								public void run() {
									Utils.printDebug(mContext, Str.Please_input_the_Quantity[Global.LANG]);
									showKeyboard((EditText) mRootView.findViewById(R.id.editQty), true);
								}
							});
							bProcessingBarcodeScan = false;
							return;
						}
					}
				}

				if(Utils.getProdLoconSCWType(mContext, selProductInfo.strItemNo) == Global.WEIGHT_ITEM)
				{
					double dQuantity = Utils.parseDoubleFromString(strQty);

					if (nIdx == -1)
					{
						if (mIsPickup == false)
						{
							if(!futureOrder)
							{
								if(TextUtils.isEmpty(strModifiable) || strModifiable.equals("N"))
								{
									new Handler(Looper.getMainLooper()).post(new Runnable() {
										@Override
										public void run() {
											try {
												if(Utils.getStringSetting(mContext, Global.PREF_AUTO_SCAN_ADD).equals("1"))
												{
													((EditText) mRootView.findViewById(R.id.editItemNo)).setText("");
													((EditText) mRootView.findViewById(R.id.editQty)).setText("");
													((EditText) mRootView.findViewById(R.id.editCase)).setText("");
												}
												MessageDialog messageDialog = new MessageDialog(mContext);
												messageDialog.mStrMessage = Str.NoChangesAllowed[Global.LANG];
												messageDialog.mStrMiddle = Str.Ok[Global.LANG];
												messageDialog.show();
											}catch (Exception e)
											{
												e.printStackTrace();

											}

										}
									});
									bProcessingBarcodeScan = false;
									return;
								}
								else if(strModifiable.equals("D") || strModifiable.equals("X"))
								{
									new Handler(Looper.getMainLooper()).post(new Runnable() {
										@Override
										public void run() {
											try {
												if(Utils.getStringSetting(mContext, Global.PREF_AUTO_SCAN_ADD).equals("1"))
												{
													((EditText) mRootView.findViewById(R.id.editItemNo)).setText("");
													((EditText) mRootView.findViewById(R.id.editQty)).setText("");
													((EditText) mRootView.findViewById(R.id.editCase)).setText("");
												}
												MessageDialog messageDialog = new MessageDialog(mContext);
												messageDialog.mStrMessage = Str.YouCanOnlyDecreaseQuantity[Global.LANG];
												messageDialog.mStrMiddle = Str.Ok[Global.LANG];
												messageDialog.show();
											}catch (Exception e)
											{
												e.printStackTrace();
											}
										}
									});
									bProcessingBarcodeScan = false;
									return;
								}

								final double dStock = getStockValueD(selProductInfo.strItemNo);
								if( dQuantity > dStock /*&& nStock > 0*/ && dQuantity != 0)
								{
									if( m_nInventoryValidationType == 1 )
									{
										final double finalDQuantity2 = dQuantity;
										new Handler(Looper.getMainLooper()).post(new Runnable() {
											@Override
											public void run() {
												try {
													final InventoryMessageDialog msgDialog = new InventoryMessageDialog(mContext);
													msgDialog.setCancelable(false);
													String strCaption = "";
													strCaption = String.format(Locale.ENGLISH, "%.2f.", dStock);

													msgDialog.mStrTitle = Str.Warning[Global.LANG];
													msgDialog.mStrCustomerName = selProductInfo.strItemNo + "\n" + selProductInfo.strDescription;
													msgDialog.mStrMessage = String.format(Locale.ENGLISH, Str.The_quantity_you_have_entered_exceeds_the_available_stock_of[Global.LANG], strCaption);
													msgDialog.mStrSubMessage = Str.Ignore_the_oversell[Global.LANG];
													msgDialog.mStrLeft = Str.No[Global.LANG];
													msgDialog.mStrRight = Str.Yes[Global.LANG];
													final double finalNQuantity1 = finalDQuantity2;
													msgDialog.setOnDismissListener(new OnDismissListener() {
														@Override
														public void onDismiss(DialogInterface dialog) {
															if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
																if(Utils.getStringSetting(mContext, Global.PREF_AUTO_SCAN_ADD).equals("1"))
																{
																	((EditText) mRootView.findViewById(R.id.editItemNo)).setText("");
																	((EditText) mRootView.findViewById(R.id.editQty)).setText("");
																	((EditText) mRootView.findViewById(R.id.editCase)).setText("");
																}
																showKeyboard((EditText) mRootView.findViewById(R.id.editCase), true);
															} else if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
																UOrderDInfo info = new UOrderDInfo();
																info.strItemNo = selProductInfo.strItemNo;
																info.strProdGroup = selProductInfo.strProdGrp;
																info.nCustNo = Global.gCurCustomerInfo.nCustNo;
																if(!mIsPickup)
																{
																	if(isFreeItem)
																	{
																		info.nTrxnType = Global.TRXN_SAMPLE;
																	}
																	else
																	{
																		info.nTrxnType = Global.TRXN_DELIVER;
																	}
																}
																else
																{
																	info.nTrxnType = Global.TRXN_PICKUP;
																}
																if (mIsPickup == false) {
																	info.dSales = finalNQuantity1;
																} else {
																	info.dDumps = finalNQuantity1;
																}
																info.strShortDesc = selProductInfo.strShortDesc;
																info.strDescription = selProductInfo.strDescription;
																if(info.nTrxnType != Global.TRXN_SAMPLE)
																{
																	info.nPrice = selProductInfo.nPrice;
																	info.nGrossPrice = selProductInfo.nGrossPrice;
																	info.nOrgPrice = selProductInfo.nOrgPrice;
																	info.nOrgGrossPrice = selProductInfo.nOrgGrossPrice;
																	info.nDiscountVal = selProductInfo.nDiscountVal;
																}
																info.nPromoType = selProductInfo.nPromoType;
																info.nPromoDiscount = selProductInfo.nPromoDiscount;
																if(info.nTrxnType != Global.TRXN_SAMPLE)
																{
																	info.nBasePrice = Utils.getDividedValue(selProductInfo.prodLocnInfo.nBasePrice);
																}
																info.nLocnNo = selProductInfo.prodLocnInfo.nLocnNo;
																info.strFullCase = selProductInfo.prodLocnInfo.strFullCase;
																info.nSCWType = selProductInfo.prodLocnInfo.nSCWType;
																if(selProductInfo != null && selProductInfo.prodLocnInfo != null)
																{
																	info.nCaseFactor = selProductInfo.prodLocnInfo.nCaseFactor;
																}

																if(info.uPromotionInfoList.size() == 0 && selProductInfo.uPromotionInfoList.size() > 0)
																{
																	for(UPromotionInfo uPromotionInfo : selProductInfo.uPromotionInfoList)
																	{
																		UPromotionInfo uItem = uPromotionInfo.duplicate();
																		uItem.nTrxnType = (mIsPickup) ? Global.TRXN_PICKUP : Global.TRXN_DELIVER;
																		uItem.nCustNo = Global.gCurCustomerInfo.nCustNo;
																		uItem.nChainNo = Global.gCurCustomerInfo.nChainNo;
																		uItem.strItemNo = info.strItemNo;
																		info.uPromotionInfoList.add(uItem);
																	}
																}

																mUOrderDList.add(0, info);
																m_bModified = true;

																if(bShowCase)
																{
																	if( Global.gCurCustomerInfo.strSalEntryMode.equals("B") || Global.gCurCustomerInfo.strSalEntryMode.equals("C"))
																	{
																		mRootView.findViewById(R.id.editCase).setVisibility(View.VISIBLE);
																		mRootView.findViewById(R.id.lblSep).setVisibility(View.VISIBLE);
																	}
																}
																else
																{
																	mRootView.findViewById(R.id.editCase).setVisibility(View.GONE);
																	mRootView.findViewById(R.id.lblSep).setVisibility(View.GONE);
																}
																((EditText) mRootView.findViewById(R.id.editItemNo)).setText("");
																((EditText) mRootView.findViewById(R.id.editCase)).setText("");
																((EditText) mRootView.findViewById(R.id.editQty)).setText("");
																focusAndHideKeyboard((EditText) mRootView.findViewById(R.id.editItemNo));
																refreshPage();
																mSelIdx = -1;
																showReasonCode();
																showPriceInven();
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
										bProcessingBarcodeScan = false;
										return;
									}
									else if( m_nInventoryValidationType == 2 )
									{
										new Handler(Looper.getMainLooper()).post(new Runnable() {
											@Override
											public void run() {
												try {
													final InventoryMessageDialog msgDialog = new InventoryMessageDialog(mContext);
													msgDialog.setCancelable(false);
													String strCaption = String.format(Locale.ENGLISH, "%.2f", dStock);

													msgDialog.mStrTitle = Str.Warning[Global.LANG];
													msgDialog.mStrCustomerName = selProductInfo.strItemNo + "\n" + selProductInfo.strDescription;
													msgDialog.mStrMessage = String.format(Locale.ENGLISH, Str.The_quantity_you_have_entered_exceeds_the_available_stock_of[Global.LANG], strCaption);
													msgDialog.mStrSubMessage = Str.Reenter_the_quantity_to_continue[Global.LANG];
													msgDialog.mStrMiddle = Str.Return[Global.LANG];
													msgDialog.setOnDismissListener(new OnDismissListener() {
														@Override
														public void onDismiss(DialogInterface dialog) {
															if (msgDialog.mReturnCode == Global.DLG_RETURN_MIDDLE) {
																if(Utils.getStringSetting(mContext, Global.PREF_AUTO_SCAN_ADD).equals("1"))
																{
																	((EditText) mRootView.findViewById(R.id.editItemNo)).setText("");
																	((EditText) mRootView.findViewById(R.id.editQty)).setText("");
																	((EditText) mRootView.findViewById(R.id.editCase)).setText("");
																}
																showKeyboard((EditText) mRootView.findViewById(R.id.editCase), true);
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
										bProcessingBarcodeScan = false;
										return;
									}
								}

								final double dPresold = getPresoldValueD(selProductInfo.strItemNo);
								double dPresoldDValue = (nIdx >= 0) ? mUOrderDList.get(nIdx).dPresoldValue : 0;
								double dPeddle = dStock - dPresold;
								double dAvailable = 0;
								if(dStock > dPresoldDValue) {
									dAvailable = dPresoldDValue;
								}
								else {
									dAvailable = dStock;
								}
								if(dPeddle > 0)
								{
									dAvailable += dPeddle;
								}
								if( dQuantity > dAvailable  && dQuantity != 0)
								{
									if( m_nInventoryValidationType == 3 )
									{
										final double finalDQuantity = dQuantity;
										new Handler(Looper.getMainLooper()).post(new Runnable() {
											@Override
											public void run() {
												try {
													final InventoryMessageDialog msgDialog = new InventoryMessageDialog(mContext);
													msgDialog.setCancelable(false);
													String strStockCaption = String.format(Locale.ENGLISH, "%.2f", dStock);
													String strPresoldCaption = String.format(Locale.ENGLISH, "%.2f", dPresold);
													String strAvailable = String.format(Locale.ENGLISH, "%.2f", (dStock - dPresold));

													msgDialog.mStrTitle = Str.Warning[Global.LANG];
													msgDialog.mStrCustomerName = selProductInfo.strItemNo + "\n" + selProductInfo.strDescription;
													msgDialog.mStrMessage = String.format(Locale.ENGLISH, Str.The_quantity_entered_exceeds_the_reserved_inventory_levels[Global.LANG], strPresoldCaption, strAvailable, strStockCaption);
													msgDialog.mStrSubMessage = Str.Ignore_the_oversell[Global.LANG];
													msgDialog.mStrLeft = Str.No[Global.LANG];
													msgDialog.mStrRight = Str.Yes[Global.LANG];
													final double finalNQuantity1 = finalDQuantity;
													msgDialog.setOnDismissListener(new OnDismissListener() {
														@Override
														public void onDismiss(DialogInterface dialog) {
															if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
																if(Utils.getStringSetting(mContext, Global.PREF_AUTO_SCAN_ADD).equals("1"))
																{
																	((EditText) mRootView.findViewById(R.id.editItemNo)).setText("");
																	((EditText) mRootView.findViewById(R.id.editQty)).setText("");
																	((EditText) mRootView.findViewById(R.id.editCase)).setText("");
																}
																showKeyboard((EditText) mRootView.findViewById(R.id.editCase), true);
															} else if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
																UOrderDInfo info = new UOrderDInfo();
																info.strItemNo = selProductInfo.strItemNo;
																info.strProdGroup = selProductInfo.strProdGrp;
																info.nCustNo = Global.gCurCustomerInfo.nCustNo;
																if(!mIsPickup)
																{
																	if(isFreeItem)
																	{
																		info.nTrxnType = Global.TRXN_SAMPLE;
																	}
																	else
																	{
																		info.nTrxnType = Global.TRXN_DELIVER;
																	}
																}
																else
																{
																	info.nTrxnType = Global.TRXN_PICKUP;
																}
																if (mIsPickup == false) {
																	info.dSales = finalNQuantity1;
																} else {
																	info.dDumps = finalNQuantity1;
																}
																info.strShortDesc = selProductInfo.strShortDesc;
																info.strDescription = selProductInfo.strDescription;
																if(info.nTrxnType != Global.TRXN_SAMPLE)
																{
																	info.nPrice = selProductInfo.nPrice;
																	info.nGrossPrice = selProductInfo.nGrossPrice;
																	info.nOrgPrice = selProductInfo.nOrgPrice;
																	info.nOrgGrossPrice = selProductInfo.nOrgGrossPrice;
																	info.nDiscountVal = selProductInfo.nDiscountVal;
																}

																info.nPromoType = selProductInfo.nPromoType;
																info.nPromoDiscount = selProductInfo.nPromoDiscount;
																if(info.nTrxnType != Global.TRXN_SAMPLE)
																{
																	info.nBasePrice = Utils.getDividedValue(selProductInfo.prodLocnInfo.nBasePrice);
																}
																info.nLocnNo = selProductInfo.prodLocnInfo.nLocnNo;
																info.strFullCase = selProductInfo.prodLocnInfo.strFullCase;
																info.nSCWType = selProductInfo.prodLocnInfo.nSCWType;
																if(selProductInfo != null && selProductInfo.prodLocnInfo != null)
																{
																	info.nCaseFactor = selProductInfo.prodLocnInfo.nCaseFactor;
																}

																if(info.uPromotionInfoList.size() == 0 && selProductInfo.uPromotionInfoList.size() > 0)
																{
																	for(UPromotionInfo uPromotionInfo : selProductInfo.uPromotionInfoList)
																	{
																		UPromotionInfo uItem = uPromotionInfo.duplicate();
																		uItem.nTrxnType = (mIsPickup) ? Global.TRXN_PICKUP : Global.TRXN_DELIVER;
																		uItem.nCustNo = Global.gCurCustomerInfo.nCustNo;
																		uItem.nChainNo = Global.gCurCustomerInfo.nChainNo;
																		uItem.strItemNo = info.strItemNo;
																		info.uPromotionInfoList.add(uItem);
																	}
																}

																mUOrderDList.add(0, info);
																m_bModified = true;
																if(bShowCase)
																{
																	if( Global.gCurCustomerInfo.strSalEntryMode.equals("B") || Global.gCurCustomerInfo.strSalEntryMode.equals("C"))
																	{
																		mRootView.findViewById(R.id.editCase).setVisibility(View.VISIBLE);
																		mRootView.findViewById(R.id.lblSep).setVisibility(View.VISIBLE);
																	}
																}
																else {
																	mRootView.findViewById(R.id.editCase).setVisibility(View.GONE);
																	mRootView.findViewById(R.id.lblSep).setVisibility(View.GONE);
																}
																((EditText) mRootView.findViewById(R.id.editItemNo)).setText("");
																((EditText) mRootView.findViewById(R.id.editCase)).setText("");
																((EditText) mRootView.findViewById(R.id.editQty)).setText("");
																focusAndHideKeyboard((EditText) mRootView.findViewById(R.id.editItemNo));
																refreshPage();
																mSelIdx = -1;
																showReasonCode();
																showPriceInven();
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
										bProcessingBarcodeScan = false;
										return;
									}
									else if( m_nInventoryValidationType == 4 )
									{
										new Handler(Looper.getMainLooper()).post(new Runnable() {
											@Override
											public void run() {
												try {
													final InventoryMessageDialog msgDialog = new InventoryMessageDialog(mContext);
													msgDialog.setCancelable(false);
													String strStockCaption = String.format(Locale.ENGLISH, "%.2f", dStock);
													String strPresoldCaption = String.format(Locale.ENGLISH, "%.2f", dPresold);
													String strAvailable = String.format(Locale.ENGLISH, "%.2f", (dStock - dPresold));

													msgDialog.mStrCustomerName = selProductInfo.strItemNo + "\n" + selProductInfo.strDescription;
													msgDialog.mStrTitle = Str.Warning[Global.LANG];
													msgDialog.mStrMessage = String.format(Locale.ENGLISH, Str.The_quantity_entered_exceeds_the_reserved_inventory_levels[Global.LANG], strPresoldCaption, strAvailable, strStockCaption);
													msgDialog.mStrSubMessage = Str.Reenter_the_quantity_to_continue[Global.LANG];
													msgDialog.mStrMiddle = Str.Return[Global.LANG];
													msgDialog.setOnDismissListener(new OnDismissListener() {
														@Override
														public void onDismiss(DialogInterface dialog) {
															if (msgDialog.mReturnCode == Global.DLG_RETURN_MIDDLE) {
																if(Utils.getStringSetting(mContext, Global.PREF_AUTO_SCAN_ADD).equals("1"))
																{
																	((EditText) mRootView.findViewById(R.id.editItemNo)).setText("");
																	((EditText) mRootView.findViewById(R.id.editQty)).setText("");
																	((EditText) mRootView.findViewById(R.id.editCase)).setText("");
																}
																showKeyboard((EditText) mRootView.findViewById(R.id.editCase), true);
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
										bProcessingBarcodeScan = false;
										return;
									}
								}
							}
						}

						if(!futureOrder)
						{
							//Check Max Order Size
							if(selProductInfo != null && selProductInfo.nMaxQty > 0 && dQuantity > selProductInfo.nMaxQty && dQuantity != 0)
							{
								new Handler(Looper.getMainLooper()).post(new Runnable() {
									@Override
									public void run() {
										try {
											String strMessage = String.format(Locale.ENGLISH, Str.The_quantity_you_have_entered_exceeds_the_maximum_allowed_1[Global.LANG], selProductInfo.nMaxQty, strCartonDescription) + "\n";
											final MessageDialog msgDialog = new MessageDialog(mContext);
											msgDialog.setCancelable(false);
											msgDialog.mStrMessage = strMessage;
											msgDialog.mStrMiddle = Str.Return[Global.LANG];
											msgDialog.mStrTitle = Str.Error[Global.LANG];
											msgDialog.setOnDismissListener(new OnDismissListener() {
												@Override
												public void onDismiss(DialogInterface dialog) {
													if(Utils.getStringSetting(mContext, Global.PREF_AUTO_SCAN_ADD).equals("1"))
													{
														((EditText) mRootView.findViewById(R.id.editItemNo)).setText("");
														((EditText) mRootView.findViewById(R.id.editQty)).setText("");
														((EditText) mRootView.findViewById(R.id.editCase)).setText("");
													}
													focusAndHideKeyboard((EditText) mRootView.findViewById(R.id.editItemNo));
												}
											});
											msgDialog.show();
										}catch (Exception e)
										{
											e.printStackTrace();
										}

									}
								});
								bProcessingBarcodeScan = false;
								return;
							}

						}

						if(mEntryInputMethod.equals("A"))
						{
							if(dQuantity < 0)
							{
								new Handler(Looper.getMainLooper()).post(new Runnable() {
									@Override
									public void run() {
										((EditText) mRootView.findViewById(R.id.editQty)).setText("");
										Utils.printDebug(mContext, Str.Quantity_Removed_From_the_transaction[Global.LANG]);
										showKeyboard((EditText) mRootView.findViewById(R.id.editQty), true);
									}
								});
								bProcessingBarcodeScan = false;
								return;
							}
						}

						UOrderDInfo info = new UOrderDInfo();
						info.strItemNo = selProductInfo.strItemNo;
						info.strProdGroup = selProductInfo.strProdGrp;
						info.nCustNo = Global.gCurCustomerInfo.nCustNo;
						if(!mIsPickup)
						{
							if(isFreeItem)
							{
								info.nTrxnType = Global.TRXN_SAMPLE;
							}
							else
							{
								info.nTrxnType = Global.TRXN_DELIVER;
							}
						}
						else
						{
							info.nTrxnType = Global.TRXN_PICKUP;
						}
						if (mIsPickup == false) {
							info.dSales = dQuantity;
						} else {
							info.dDumps = dQuantity;
						}
						info.strShortDesc = selProductInfo.strShortDesc;
						info.strDescription = selProductInfo.strDescription;
						if(info.nTrxnType != Global.TRXN_SAMPLE)
						{
							info.nPrice = selProductInfo.nPrice;
							info.nGrossPrice = selProductInfo.nGrossPrice;
							info.nOrgPrice = selProductInfo.nOrgPrice;
							info.nOrgGrossPrice = selProductInfo.nOrgGrossPrice;
							info.nDiscountVal = selProductInfo.nDiscountVal;
						}

						info.nPromoType = selProductInfo.nPromoType;
						info.nPromoDiscount = selProductInfo.nPromoDiscount;
						if(info.nTrxnType != Global.TRXN_SAMPLE)
						{
							info.nBasePrice = Utils.getDividedValue(selProductInfo.prodLocnInfo.nBasePrice);
						}
						info.nLocnNo = selProductInfo.prodLocnInfo.nLocnNo;
						info.strFullCase = selProductInfo.prodLocnInfo.strFullCase;
						info.nSCWType = selProductInfo.prodLocnInfo.nSCWType;
						if(selProductInfo != null && selProductInfo.prodLocnInfo != null)
						{
							info.nCaseFactor = selProductInfo.prodLocnInfo.nCaseFactor;
						}
						for(UPromotionInfo uPromotionInfo : selProductInfo.uPromotionInfoList)
						{
							UPromotionInfo uItem = uPromotionInfo.duplicate();
							uItem.nTrxnType = (mIsPickup) ? Global.TRXN_PICKUP : Global.TRXN_DELIVER;
							uItem.nCustNo = Global.gCurCustomerInfo.nCustNo;
							uItem.nChainNo = Global.gCurCustomerInfo.nChainNo;
							uItem.strItemNo = info.strItemNo;
							info.uPromotionInfoList.add(uItem);
						}

						mUOrderDList.add(0, info);
					}
					else {

						if(mUOrderDList.get(nIdx).dPlanQty != 0 && isFreeItem)
						{
							String strAvailableType = "Y";
							DbAdapter dbAdapter = new DbAdapter(mContext);
							dbAdapter.open();
							List<DescTypeInfo> typeInfoList = dbAdapter.getDescTypeListWithNumericKey("FREERSN",  mUOrderDList.get(nIdx).nReasonCode);
							dbAdapter.close();
							if(typeInfoList != null && typeInfoList.size() > 0)
							{
								strAvailableType = typeInfoList.get(0).strValue1;
							}

							/*if(strAvailableType.equals("N") && mUOrderDList.get(nIdx).dSales != dQuantity)
							{
								final MessageDialog msgDialog = new MessageDialog(mContext);
								msgDialog.setCancelable(false);
								msgDialog.mStrMessage = Str.YouCanNotChangeThisItem[Global.LANG];
								msgDialog.mStrMiddle = Str.Ok[Global.LANG];
								msgDialog.show();
								return;
							}
							else*/
							if(!futureOrder)
							{
								if(strAvailableType.equals("D"))
								{
									if(mUOrderDList.get(nIdx).dSales < dQuantity)
									{
										new Handler(Looper.getMainLooper()).post(new Runnable() {
											@Override
											public void run() {
												try {
													if(Utils.getStringSetting(mContext, Global.PREF_AUTO_SCAN_ADD).equals("1"))
													{
														((EditText) mRootView.findViewById(R.id.editItemNo)).setText("");
														((EditText) mRootView.findViewById(R.id.editQty)).setText("");
														((EditText) mRootView.findViewById(R.id.editCase)).setText("");
													}
													final MessageDialog msgDialog = new MessageDialog(mContext);
													msgDialog.setCancelable(false);
													msgDialog.mStrMessage = Str.YouCanOnlyDecreaseThisItem[Global.LANG];
													msgDialog.mStrMiddle = Str.Ok[Global.LANG];
													msgDialog.show();
												}catch (Exception e)
												{
													e.printStackTrace();
												}

											}
										});
										bProcessingBarcodeScan = false;
										return;
									}
								}
								else if(!strAvailableType.equals("Y"))
								{
									if(mUOrderDList.get(nIdx).dSales != dQuantity)
									{
										new Handler(Looper.getMainLooper()).post(new Runnable() {
											@Override
											public void run() {
												try {
													if(Utils.getStringSetting(mContext, Global.PREF_AUTO_SCAN_ADD).equals("1"))
													{
														((EditText) mRootView.findViewById(R.id.editItemNo)).setText("");
														((EditText) mRootView.findViewById(R.id.editQty)).setText("");
														((EditText) mRootView.findViewById(R.id.editCase)).setText("");
													}
													final MessageDialog msgDialog = new MessageDialog(mContext);
													msgDialog.setCancelable(false);
													msgDialog.mStrMessage = Str.YouCanNotChangeThisItem[Global.LANG];
													msgDialog.mStrMiddle = Str.Ok[Global.LANG];
													msgDialog.show();
												}catch (Exception e)
												{
													e.printStackTrace();
												}

											}
										});
										bProcessingBarcodeScan = false;
										return;
									}
								}
							}
						}
						if(!futureOrder)
						{
							//Check Max Order Size
							if(selProductInfo != null && selProductInfo.nMaxQty > 0 && dQuantity > selProductInfo.nMaxQty)
							{
								new Handler(Looper.getMainLooper()).post(new Runnable() {
									@Override
									public void run() {
										try {
											String strMessage = String.format(Locale.ENGLISH, Str.The_quantity_you_have_entered_exceeds_the_maximum_allowed_1[Global.LANG], selProductInfo.nMaxQty, strCartonDescription) + "\n";
											final MessageDialog msgDialog = new MessageDialog(mContext);
											msgDialog.setCancelable(false);
											msgDialog.mStrMessage = strMessage;
											msgDialog.mStrMiddle = Str.Return[Global.LANG];
											msgDialog.mStrTitle = Str.Error[Global.LANG];
											msgDialog.setOnDismissListener(new OnDismissListener() {
												@Override
												public void onDismiss(DialogInterface dialog) {
													if(Utils.getStringSetting(mContext, Global.PREF_AUTO_SCAN_ADD).equals("1"))
													{
														((EditText) mRootView.findViewById(R.id.editItemNo)).setText("");
														((EditText) mRootView.findViewById(R.id.editQty)).setText("");
														((EditText) mRootView.findViewById(R.id.editCase)).setText("");
													}
													focusAndHideKeyboard((EditText) mRootView.findViewById(R.id.editItemNo));
												}
											});
											msgDialog.show();
										}catch (Exception e)
										{
											e.printStackTrace();
										}

									}
								});
								bProcessingBarcodeScan = false;
								return;
							}
						}


						if(mEntryInputMethod.equals("A"))
						{
							if(mIsPickup)
							{
								dQuantity = dQuantity + mUOrderDList.get(nIdx).dDumps;
							}
							else
							{
								dQuantity = dQuantity + mUOrderDList.get(nIdx).dSales;
							}

							if(dQuantity < 0)
							{
								new Handler(Looper.getMainLooper()).post(new Runnable() {
									@Override
									public void run() {
										if(Utils.getStringSetting(mContext, Global.PREF_AUTO_SCAN_ADD).equals("1"))
										{
											((EditText) mRootView.findViewById(R.id.editItemNo)).setText("");
											((EditText) mRootView.findViewById(R.id.editQty)).setText("");
											((EditText) mRootView.findViewById(R.id.editCase)).setText("");
										}
										((EditText) mRootView.findViewById(R.id.editQty)).setText("");
										Utils.printDebug(mContext, Str.Quantity_Removed_From_the_transaction[Global.LANG]);
										showKeyboard((EditText) mRootView.findViewById(R.id.editQty), true);
									}
								});
								bProcessingBarcodeScan = false;
								return;
							}
						}

						if (dQuantity == 0 && mUOrderDList.get(nIdx).dPlanQty == 0) {
							mUOrderDList.remove(nIdx);

						} else {
							if (mIsPickup == false) {
								if(!futureOrder)
								{
									if(TextUtils.isEmpty(strModifiable) || strModifiable.equals("N"))
									{
										if(dQuantity != mUOrderDList.get(nIdx).dPlanQty)
										{
											new Handler().post(new Runnable() {
												@Override
												public void run() {
													try {
														if(Utils.getStringSetting(mContext, Global.PREF_AUTO_SCAN_ADD).equals("1"))
														{
															((EditText) mRootView.findViewById(R.id.editItemNo)).setText("");
															((EditText) mRootView.findViewById(R.id.editQty)).setText("");
															((EditText) mRootView.findViewById(R.id.editCase)).setText("");
														}
														MessageDialog messageDialog = new MessageDialog(mContext);
														messageDialog.mStrMessage = Str.NoChangesAllowed[Global.LANG];
														messageDialog.mStrMiddle = Str.Ok[Global.LANG];
														messageDialog.show();
													}catch (Exception e)
													{
														e.printStackTrace();
													}

												}
											});
											bProcessingBarcodeScan = false;
											return;
										}
									}
									else if(strModifiable.equals("D") || strModifiable.equals("X"))
									{
										if(dQuantity > mUOrderDList.get(nIdx).dPlanQty)
										{
											new Handler(Looper.getMainLooper()).post(new Runnable() {
												@Override
												public void run() {
													try {
														if(Utils.getStringSetting(mContext, Global.PREF_AUTO_SCAN_ADD).equals("1"))
														{
															((EditText) mRootView.findViewById(R.id.editItemNo)).setText("");
															((EditText) mRootView.findViewById(R.id.editQty)).setText("");
															((EditText) mRootView.findViewById(R.id.editCase)).setText("");
														}
														MessageDialog messageDialog = new MessageDialog(mContext);
														messageDialog.mStrMessage = Str.YouCanOnlyDecreaseQuantity[Global.LANG];
														messageDialog.mStrMiddle = Str.Ok[Global.LANG];
														messageDialog.show();
													}catch (Exception e)
													{

													}

												}
											});
											bProcessingBarcodeScan = false;
											return;
										}
									}

									final double dStock = getStockValueD(mUOrderDList.get(nIdx).strItemNo);
									if( dQuantity != 0 && dQuantity > dStock /*&& nStock > 0*/ && mUOrderDList.get(nIdx).nTrxnType != Global.TRXN_FREE && mUOrderDList.get(nIdx).nTrxnType != Global.TRXN_SAMPLE)
									{
										if( m_nInventoryValidationType == 1 )
										{
											final double finalDQuantity3 = dQuantity;
											final int finalNIdx1 = nIdx;
											new Handler(Looper.getMainLooper()).post(new Runnable() {
												@Override
												public void run() {
													try {
														final InventoryMessageDialog msgDialog = new InventoryMessageDialog(mContext);
													msgDialog.setCancelable(false);
													String strCaption = String.format(Locale.ENGLISH, "%.2f.", dStock);

													msgDialog.mStrTitle = Str.Warning[Global.LANG];
													msgDialog.mStrCustomerName = selProductInfo.strItemNo + "\n" + selProductInfo.strDescription;
													msgDialog.mStrMessage = String.format(Locale.ENGLISH, Str.The_quantity_you_have_entered_exceeds_the_available_stock_of[Global.LANG], strCaption);
													msgDialog.mStrSubMessage = Str.Ignore_the_oversell[Global.LANG];
													msgDialog.mStrLeft = Str.No[Global.LANG];
													msgDialog.mStrRight = Str.Yes[Global.LANG];
													final double finalNQuantity = finalDQuantity3;
													final int finalNIdx = finalNIdx1;
													msgDialog.setOnDismissListener(new OnDismissListener() {
														@Override
														public void onDismiss(DialogInterface dialog) {
															if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
																if(Utils.getStringSetting(mContext, Global.PREF_AUTO_SCAN_ADD).equals("1"))
																{
																	((EditText) mRootView.findViewById(R.id.editItemNo)).setText("");
																	((EditText) mRootView.findViewById(R.id.editQty)).setText("");
																	((EditText) mRootView.findViewById(R.id.editCase)).setText("");
																}
																showKeyboard((EditText) mRootView.findViewById(R.id.editCase), true);
															} else if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
																mUOrderDList.get(finalNIdx).dSales = finalNQuantity;

																if(bShowCase)
																{
																	if( Global.gCurCustomerInfo.strSalEntryMode.equals("B") || Global.gCurCustomerInfo.strSalEntryMode.equals("C"))
																	{
																		mRootView.findViewById(R.id.editCase).setVisibility(View.VISIBLE);
																		mRootView.findViewById(R.id.lblSep).setVisibility(View.VISIBLE);
																	}
																}
																else
																{
																	mRootView.findViewById(R.id.editCase).setVisibility(View.GONE);
																	mRootView.findViewById(R.id.lblSep).setVisibility(View.GONE);
																}
																((EditText) mRootView.findViewById(R.id.editItemNo)).setText("");
																((EditText) mRootView.findViewById(R.id.editCase)).setText("");
																((EditText) mRootView.findViewById(R.id.editQty)).setText("");
																focusAndHideKeyboard((EditText) mRootView.findViewById(R.id.editItemNo));
																m_bModified = true;
																refreshPage();
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
											bProcessingBarcodeScan = false;
											return;
										}
										else if( m_nInventoryValidationType == 2 )
										{
											new Handler(Looper.getMainLooper()).post(new Runnable() {
												@Override
												public void run() {
													try {
														final InventoryMessageDialog msgDialog = new InventoryMessageDialog(mContext);
														msgDialog.setCancelable(false);
														String strCaption = String.format(Locale.ENGLISH, "%.2f.",dStock);

														msgDialog.mStrTitle = Str.Warning[Global.LANG];
														msgDialog.mStrCustomerName = selProductInfo.strItemNo + "\n" + selProductInfo.strDescription;
														msgDialog.mStrMessage = String.format(Locale.ENGLISH, Str.The_quantity_you_have_entered_exceeds_the_available_stock_of[Global.LANG], strCaption);
														msgDialog.mStrSubMessage = Str.Reenter_the_quantity_to_continue[Global.LANG];
														msgDialog.mStrMiddle = Str.Return[Global.LANG];
														msgDialog.setOnDismissListener(new OnDismissListener() {
															@Override
															public void onDismiss(DialogInterface dialog) {
																if (msgDialog.mReturnCode == Global.DLG_RETURN_MIDDLE) {
																	if(Utils.getStringSetting(mContext, Global.PREF_AUTO_SCAN_ADD).equals("1"))
																	{
																		((EditText) mRootView.findViewById(R.id.editItemNo)).setText("");
																		((EditText) mRootView.findViewById(R.id.editQty)).setText("");
																		((EditText) mRootView.findViewById(R.id.editCase)).setText("");
																	}
																	showKeyboard((EditText) mRootView.findViewById(R.id.editCase), true);
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
											bProcessingBarcodeScan = false;
											return;
										}
									}

									final double dPresold = getPresoldValueD(selProductInfo.strItemNo);
									double dPresoldDValue = (nIdx >= 0) ? mUOrderDList.get(nIdx).dPresoldValue : 0;
									double dPeddle = dStock - dPresold;
									double dAvailable = 0;
									if(dStock > dPresoldDValue) {
										dAvailable = dPresoldDValue;
									}
									else {
										dAvailable = dStock;
									}
									if(dPeddle > 0)
									{
										dAvailable += dPeddle;
									}
									if( dQuantity > dAvailable  && dQuantity != 0){
										if (m_nInventoryValidationType == 3) {
											final double finalDQuantity1 = dQuantity;
											final int finalNIdx1 = nIdx;
											new Handler(Looper.getMainLooper()).post(new Runnable() {
												@Override
												public void run() {
													try {
														final InventoryMessageDialog msgDialog = new InventoryMessageDialog(mContext);
														msgDialog.setCancelable(false);
														String strStockCaption = String.format(Locale.ENGLISH, "%.2f", dStock);
														String strPresoldCaption = String.format(Locale.ENGLISH, "%.2f", dPresold);
														String strAvailable = String.format(Locale.ENGLISH, "%.2f", (dStock - dPresold));

														msgDialog.mStrTitle = Str.Warning[Global.LANG];
														msgDialog.mStrCustomerName = selProductInfo.strItemNo + "\n" + selProductInfo.strDescription;
														msgDialog.mStrMessage = String.format(Locale.ENGLISH, Str.The_quantity_entered_exceeds_the_reserved_inventory_levels[Global.LANG], strPresoldCaption, strAvailable, strStockCaption);
														msgDialog.mStrSubMessage = Str.Ignore_the_oversell[Global.LANG];
														msgDialog.mStrLeft = Str.No[Global.LANG];
														msgDialog.mStrRight = Str.Yes[Global.LANG];
														final double finalNQuantity = finalDQuantity1;
														final int finalNIdx = finalNIdx1;
														msgDialog.setOnDismissListener(new OnDismissListener() {
															@Override
															public void onDismiss(DialogInterface dialog) {
																if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
																	if(Utils.getStringSetting(mContext, Global.PREF_AUTO_SCAN_ADD).equals("1"))
																	{
																		((EditText) mRootView.findViewById(R.id.editItemNo)).setText("");
																		((EditText) mRootView.findViewById(R.id.editQty)).setText("");
																		((EditText) mRootView.findViewById(R.id.editCase)).setText("");
																	}
																	showKeyboard((EditText) mRootView.findViewById(R.id.editCase), true);
																} else if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
																	mUOrderDList.get(finalNIdx).dSales = finalNQuantity;
																	if(bShowCase)
																	{
																		if( Global.gCurCustomerInfo.strSalEntryMode.equals("B") || Global.gCurCustomerInfo.strSalEntryMode.equals("C"))
																		{
																			mRootView.findViewById(R.id.editCase).setVisibility(View.VISIBLE);
																			mRootView.findViewById(R.id.lblSep).setVisibility(View.VISIBLE);
																		}
																	}
																	else
																	{
																		mRootView.findViewById(R.id.editCase).setVisibility(View.GONE);
																		mRootView.findViewById(R.id.lblSep).setVisibility(View.GONE);
																	}
																	((EditText) mRootView.findViewById(R.id.editItemNo)).setText("");
																	((EditText) mRootView.findViewById(R.id.editCase)).setText("");
																	((EditText) mRootView.findViewById(R.id.editQty)).setText("");
																	focusAndHideKeyboard((EditText) mRootView.findViewById(R.id.editItemNo));
																	m_bModified = true;
																	refreshPage();
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
											bProcessingBarcodeScan = false;
											return;
										} else if (m_nInventoryValidationType == 4) {
											new Handler(Looper.getMainLooper()).post(new Runnable() {
												@Override
												public void run() {
													try {
														final InventoryMessageDialog msgDialog = new InventoryMessageDialog(mContext);
													msgDialog.setCancelable(false);
													String strStockCaption = String.format(Locale.ENGLISH, "%.2f", dStock);
													String strPresoldCaption = String.format(Locale.ENGLISH, "%.2f", dPresold);
													String strAvailable = String.format(Locale.ENGLISH, "%.2f", (dStock - dPresold));

													msgDialog.mStrTitle = Str.Warning[Global.LANG];
													msgDialog.mStrCustomerName = selProductInfo.strItemNo + "\n" + selProductInfo.strDescription;
													msgDialog.mStrMessage = String.format(Locale.ENGLISH, Str.The_quantity_entered_exceeds_the_reserved_inventory_levels[Global.LANG], strPresoldCaption, strAvailable, strStockCaption);
													msgDialog.mStrSubMessage = Str.Reenter_the_quantity_to_continue[Global.LANG];
													msgDialog.mStrMiddle = Str.Return[Global.LANG];
													msgDialog.setOnDismissListener(new OnDismissListener() {
														@Override
														public void onDismiss(DialogInterface dialog) {
															if (msgDialog.mReturnCode == Global.DLG_RETURN_MIDDLE) {
																if(Utils.getStringSetting(mContext, Global.PREF_AUTO_SCAN_ADD).equals("1"))
																{
																	((EditText) mRootView.findViewById(R.id.editItemNo)).setText("");
																	((EditText) mRootView.findViewById(R.id.editQty)).setText("");
																	((EditText) mRootView.findViewById(R.id.editCase)).setText("");
																}
																showKeyboard((EditText) mRootView.findViewById(R.id.editCase), true);
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
											bProcessingBarcodeScan = false;
											return;
										}
									}
								}
								mUOrderDList.get(nIdx).dSales = dQuantity;
							} else {
								mUOrderDList.get(nIdx).dDumps = dQuantity;
							}
						}
					}
				}
				else
				{
					int nQuantity = 0;
					if(bShowCase)
					{
						if( Global.gCurCustomerInfo.strSalEntryMode.equals("B") || Global.gCurCustomerInfo.strSalEntryMode.equals("C"))
						{
							nQuantity = Utils.parseIntFromString(strQty) + Utils.parseIntFromString(strCase) * nCase;
						}
						else if( Global.gCurCustomerInfo.strSalEntryMode.equals("") || Global.gCurCustomerInfo.strSalEntryMode.equals("U") )
						{
							nQuantity = Utils.parseIntFromString(strQty);
						}
					}
					else {
						nQuantity = Utils.parseIntFromString(strQty);
					}

					final int nInputQty = nQuantity;

					if (nIdx == -1) {
						if (nQuantity == 0) {
							new Handler(Looper.getMainLooper()).post(new Runnable() {
								@Override
								public void run() {
									Utils.printDebug(mContext, Str.Please_input_the_Quantity[Global.LANG]);
									showKeyboard((EditText) mRootView.findViewById(R.id.editQty), true);
								}
							});
							bProcessingBarcodeScan = false;
							return;
						} else {
							if (mIsPickup == false)
							{
								if(!futureOrder)
								{
									if(TextUtils.isEmpty(strModifiable) || strModifiable.equals("N"))
									{
										new Handler(Looper.getMainLooper()).post(new Runnable() {
											@Override
											public void run() {
												try {
													if(Utils.getStringSetting(mContext, Global.PREF_AUTO_SCAN_ADD).equals("1"))
													{
														((EditText) mRootView.findViewById(R.id.editItemNo)).setText("");
														((EditText) mRootView.findViewById(R.id.editQty)).setText("");
														((EditText) mRootView.findViewById(R.id.editCase)).setText("");
													}
													MessageDialog messageDialog = new MessageDialog(mContext);
													messageDialog.mStrMessage = Str.NoChangesAllowed[Global.LANG];
													messageDialog.mStrMiddle = Str.Ok[Global.LANG];
													messageDialog.show();
												}catch (Exception e)
												{
													e.printStackTrace();
												}

											}
										});
										bProcessingBarcodeScan = false;
										return;
									}
									else if(strModifiable.equals("D") || strModifiable.equals("X"))
									{
										new Handler(Looper.getMainLooper()).post(new Runnable() {
											@Override
											public void run() {
												try {
													if(Utils.getStringSetting(mContext, Global.PREF_AUTO_SCAN_ADD).equals("1"))
													{
														((EditText) mRootView.findViewById(R.id.editItemNo)).setText("");
														((EditText) mRootView.findViewById(R.id.editQty)).setText("");
														((EditText) mRootView.findViewById(R.id.editCase)).setText("");
													}
													MessageDialog messageDialog = new MessageDialog(mContext);
													messageDialog.mStrMessage = Str.YouCanOnlyDecreaseQuantity[Global.LANG];
													messageDialog.mStrMiddle = Str.Ok[Global.LANG];
													messageDialog.show();
												}catch (Exception e)
												{
													e.printStackTrace();
												}
											}
										});
										bProcessingBarcodeScan = false;
										return;
									}

									final int nStock = getStockValue(selProductInfo.strItemNo);

									// check if there is insufficient balance because of free promotion
									FreeItemDetails freeItemDetails = getFreeItemDetailsByPromotion(selProductInfo, nQuantity);
									if (freeItemDetails != null && freeItemDetails.nFreeItemCount != 0) {
										String strFreeItemNo = freeItemDetails.strFreeItemNo;

										final int nFreeItemStock = getStockValue(freeItemDetails.strFreeItemNo);
										if (strFreeItemNo.equals(selProductInfo.strItemNo)) {
											if (nQuantity + freeItemDetails.nFreeItemCount > nStock) {
												if (m_nInventoryValidationType == 2 || m_nInventoryValidationType == 4)
												{
													int finalNQuantity = nQuantity;
													new Handler(Looper.getMainLooper()).post(new Runnable() {
														@Override
														public void run() {
															try {
																final InventoryMessageDialog msgDialog = new InventoryMessageDialog(mContext);
																msgDialog.setCancelable(false);

																msgDialog.mStrTitle = Str.Warning[Global.LANG];
																msgDialog.mStrCustomerName = selProductInfo.strItemNo + "\n" + selProductInfo.strDescription;
																msgDialog.mStrMessage = String.format(Locale.ENGLISH, "You do not have enough stock to sell %d and provide %d Promotional Items. Suggest you change to sell less", finalNQuantity, freeItemDetails.nFreeItemCount);
																msgDialog.mStrMiddle = Str.Ok[Global.LANG];
																msgDialog.setOnDismissListener(new OnDismissListener() {
																	@Override
																	public void onDismiss(DialogInterface dialog) {
																		if (msgDialog.mReturnCode == Global.DLG_RETURN_MIDDLE) {
																			if(Utils.getStringSetting(mContext, Global.PREF_AUTO_SCAN_ADD).equals("1"))
																			{
																				((EditText) mRootView.findViewById(R.id.editItemNo)).setText("");
																				((EditText) mRootView.findViewById(R.id.editQty)).setText("");
																				((EditText) mRootView.findViewById(R.id.editCase)).setText("");
																			}
																			showKeyboard((EditText) mRootView.findViewById(R.id.editCase), true);
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
													bProcessingBarcodeScan = false;
													return;
												}
											}
										}
										else {
											if ( nQuantity > nStock || freeItemDetails.nFreeItemCount > nFreeItemStock) {
												if (m_nInventoryValidationType == 2 || m_nInventoryValidationType == 4)
												{
													int finalNQuantity = nQuantity;
													new Handler(Looper.getMainLooper()).post(new Runnable() {
														@Override
														public void run() {
															try {
																final InventoryMessageDialog msgDialog = new InventoryMessageDialog(mContext);
																msgDialog.setCancelable(false);

																msgDialog.mStrTitle = Str.Warning[Global.LANG];
																msgDialog.mStrCustomerName = selProductInfo.strItemNo + "\n" + selProductInfo.strDescription;
																msgDialog.mStrMessage = String.format(Locale.ENGLISH, "You do not have enough stock to sell %d and provide %d Promotional Items. Suggest you change to sell less", finalNQuantity, freeItemDetails.nFreeItemCount);
																msgDialog.mStrMiddle = Str.Ok[Global.LANG];
																msgDialog.setOnDismissListener(new OnDismissListener() {
																	@Override
																	public void onDismiss(DialogInterface dialog) {
																		if (msgDialog.mReturnCode == Global.DLG_RETURN_MIDDLE) {
																			if(Utils.getStringSetting(mContext, Global.PREF_AUTO_SCAN_ADD).equals("1"))
																			{
																				((EditText) mRootView.findViewById(R.id.editItemNo)).setText("");
																				((EditText) mRootView.findViewById(R.id.editQty)).setText("");
																				((EditText) mRootView.findViewById(R.id.editCase)).setText("");
																			}
																			showKeyboard((EditText) mRootView.findViewById(R.id.editCase), true);
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
													bProcessingBarcodeScan = false;
													return;
												}
											}
										}
									}

									if( nQuantity > nStock /*&& nStock > 0*/ && nQuantity != 0)
									{
										if( m_nInventoryValidationType == 1 )
										{
											final int finalNQuantity2 = nQuantity;
											final int finalNIdx3 = nIdx;
											new Handler(Looper.getMainLooper()).post(new Runnable() {
												@Override
												public void run() {
													try {
														final InventoryMessageDialog msgDialog = new InventoryMessageDialog(mContext);
														msgDialog.setCancelable(false);
														String strCaption = "";
														if(bShowCase)
														{
															if( Global.gCurCustomerInfo.strSalEntryMode.equals("B") || Global.gCurCustomerInfo.strSalEntryMode.equals("C"))

															{
																if (Utils.isContainerItem(mContext, selProductInfo.strItemNo)) {
																	strCaption = String.format(Locale.ENGLISH, "%d.", nStock);
																}
																else {
																	strCaption = String.format(Locale.ENGLISH, "%d / %d.", nStock / nCase, nStock % nCase);
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
														msgDialog.mStrCustomerName = selProductInfo.strItemNo + "\n" + selProductInfo.strDescription;
														msgDialog.mStrMessage = String.format(Locale.ENGLISH, Str.The_quantity_you_have_entered_exceeds_the_available_stock_of[Global.LANG], strCaption);
														msgDialog.mStrSubMessage = Str.Ignore_the_oversell[Global.LANG];
														msgDialog.mStrLeft = Str.No[Global.LANG];
														msgDialog.mStrRight = Str.Yes[Global.LANG];
														final int finalNQuantity = finalNQuantity2;
														final int finalNIdx = finalNIdx3;
														final int finalNQuantity1 = finalNQuantity2;
														msgDialog.setOnDismissListener(new OnDismissListener() {
															@Override
															public void onDismiss(DialogInterface dialog) {
																if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
																	if(Utils.getStringSetting(mContext, Global.PREF_AUTO_SCAN_ADD).equals("1"))
																	{
																		((EditText) mRootView.findViewById(R.id.editItemNo)).setText("");
																		((EditText) mRootView.findViewById(R.id.editQty)).setText("");
																		((EditText) mRootView.findViewById(R.id.editCase)).setText("");
																	}
																	showKeyboard((EditText) mRootView.findViewById(R.id.editCase), true);
																} else if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
																	UOrderDInfo info = new UOrderDInfo();
																	info.strItemNo = selProductInfo.strItemNo;
																	info.strProdGroup = selProductInfo.strProdGrp;
																	info.nCustNo = Global.gCurCustomerInfo.nCustNo;
																	if(!mIsPickup)
																	{
																		if(isFreeItem)
																		{
																			info.nTrxnType = Global.TRXN_SAMPLE;
																		}
																		else
																		{
																			info.nTrxnType = Global.TRXN_DELIVER;
																		}
																	}
																	else
																	{
																		info.nTrxnType = Global.TRXN_PICKUP;
																	}
																	if (mIsPickup == false) {
																		info.nSales = finalNQuantity1;
																	} else {
																		info.nDumps = finalNQuantity1;
																	}
																	info.strShortDesc = selProductInfo.strShortDesc;
																	info.strDescription = selProductInfo.strDescription;
																	if(info.nTrxnType != Global.TRXN_SAMPLE)
																	{
																		info.nPrice = selProductInfo.nPrice;
																		info.nGrossPrice = selProductInfo.nGrossPrice;
																		info.nOrgPrice = selProductInfo.nOrgPrice;
																		info.nOrgGrossPrice = selProductInfo.nOrgGrossPrice;
																		info.nDiscountVal = selProductInfo.nDiscountVal;
																	}

																	info.nPromoType = selProductInfo.nPromoType;
																	info.nPromoDiscount = selProductInfo.nPromoDiscount;
																	if(info.nTrxnType != Global.TRXN_SAMPLE)
																	{
																		info.nBasePrice = Utils.getDividedValue(selProductInfo.prodLocnInfo.nBasePrice);
																	}
																	info.nLocnNo = selProductInfo.prodLocnInfo.nLocnNo;
																	info.strFullCase = selProductInfo.prodLocnInfo.strFullCase;
																	info.nSCWType = selProductInfo.prodLocnInfo.nSCWType;
																	if( selProductInfo != null && selProductInfo.prodLocnInfo != null)
																	{
																		info.nCaseFactor = selProductInfo.prodLocnInfo.nCaseFactor;
																	}

																	if(info.uPromotionInfoList.size() == 0 && selProductInfo.uPromotionInfoList.size() > 0)
																	{
																		for(UPromotionInfo uPromotionInfo : selProductInfo.uPromotionInfoList)
																		{
																			UPromotionInfo uItem = uPromotionInfo.duplicate();
																			uItem.nTrxnType = (mIsPickup) ? Global.TRXN_PICKUP : Global.TRXN_DELIVER;
																			uItem.nCustNo = Global.gCurCustomerInfo.nCustNo;
																			uItem.nChainNo = Global.gCurCustomerInfo.nChainNo;
																			uItem.strItemNo = info.strItemNo;
																			info.uPromotionInfoList.add(uItem);
																		}
																	}

																	mUOrderDList.add(0, info);
																	m_bModified = true;
																	if(bShowCase)
																	{
																		if( Global.gCurCustomerInfo.strSalEntryMode.equals("B") || Global.gCurCustomerInfo.strSalEntryMode.equals("C"))
																		{
																			mRootView.findViewById(R.id.editCase).setVisibility(View.VISIBLE);
																			mRootView.findViewById(R.id.lblSep).setVisibility(View.VISIBLE);
																		}
																	}
																	else
																	{
																		mRootView.findViewById(R.id.editCase).setVisibility(View.GONE);
																		mRootView.findViewById(R.id.lblSep).setVisibility(View.GONE);
																	}

																	((EditText) mRootView.findViewById(R.id.editItemNo)).setText("");
																	((EditText) mRootView.findViewById(R.id.editCase)).setText("");
																	((EditText) mRootView.findViewById(R.id.editQty)).setText("");
																	focusAndHideKeyboard((EditText) mRootView.findViewById(R.id.editItemNo));
																	refreshPage();
																	mSelIdx = -1;
																	showReasonCode();
																	showPriceInven();

																	doCheckBatchLot(info);

																	doProcessMultiPromotion(info, selProductInfo, nInputQty, isSimFreeMode);
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
											bProcessingBarcodeScan = false;
											return;
										}
										else if( m_nInventoryValidationType == 2 )
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
															if( Global.gCurCustomerInfo.strSalEntryMode.equals("B") || Global.gCurCustomerInfo.strSalEntryMode.equals("C"))
															{
																if (Utils.isContainerItem(mContext, selProductInfo.strItemNo)) {
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
														msgDialog.mStrCustomerName = selProductInfo.strItemNo + "\n" + selProductInfo.strDescription;
														msgDialog.mStrMessage = String.format(Locale.ENGLISH, Str.The_quantity_you_have_entered_exceeds_the_available_stock_of[Global.LANG], strCaption);
														msgDialog.mStrSubMessage = Str.Reenter_the_quantity_to_continue[Global.LANG];
														msgDialog.mStrMiddle = Str.Return[Global.LANG];
														msgDialog.setOnDismissListener(new OnDismissListener() {
															@Override
															public void onDismiss(DialogInterface dialog) {
																if (msgDialog.mReturnCode == Global.DLG_RETURN_MIDDLE) {
																	if(Utils.getStringSetting(mContext, Global.PREF_AUTO_SCAN_ADD).equals("1"))
																	{
																		((EditText) mRootView.findViewById(R.id.editItemNo)).setText("");
																		((EditText) mRootView.findViewById(R.id.editQty)).setText("");
																		((EditText) mRootView.findViewById(R.id.editCase)).setText("");
																	}
																	showKeyboard((EditText) mRootView.findViewById(R.id.editCase), true);
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
											bProcessingBarcodeScan = false;
											return;
										}
									}

									final int nPresold = getPresoldValue(selProductInfo.strItemNo);
									int nOrderPresoldValue = (nIdx >= 0) ? mUOrderDList.get(nIdx).nPresoldValue : 0;
									int nPeddle = nStock - nPresold;
									int nAvailable = 0;
									if(nStock > nOrderPresoldValue) {
										nAvailable = nOrderPresoldValue;
									}
									else {
										nAvailable = nStock;
									}
									if(nPeddle > 0)
									{
										nAvailable += nPeddle;
									}
									if( nQuantity > nAvailable  && nQuantity != 0)
									{
										if( m_nInventoryValidationType == 3 )
										{
											final int nQuantity4 = nQuantity;
											new Handler(Looper.getMainLooper()).post(new Runnable() {
												@Override
												public void run() {
													try {
														final InventoryMessageDialog msgDialog = new InventoryMessageDialog(mContext);
														msgDialog.setCancelable(false);
														String strStockCaption = "";
														String strPresoldCaption = "";
														String strAvailable = "";
														if(bShowCase)
														{
															if( Global.gCurCustomerInfo.strSalEntryMode.equals("B") || Global.gCurCustomerInfo.strSalEntryMode.equals("C"))
															{
																if (Utils.isContainerItem(mContext, selProductInfo.strItemNo)) {
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
														msgDialog.mStrCustomerName = selProductInfo.strItemNo + "\n" + selProductInfo.strDescription;
														msgDialog.mStrMessage = String.format(Locale.ENGLISH, Str.The_quantity_entered_exceeds_the_reserved_inventory_levels[Global.LANG], strPresoldCaption, strAvailable, strStockCaption);
														msgDialog.mStrSubMessage = Str.Ignore_the_oversell[Global.LANG];
														msgDialog.mStrLeft = Str.No[Global.LANG];
														msgDialog.mStrRight = Str.Yes[Global.LANG];
														final int finalNQuantity1 = nQuantity4;
														msgDialog.setOnDismissListener(new OnDismissListener() {
															@Override
															public void onDismiss(DialogInterface dialog) {
																if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
																	if(Utils.getStringSetting(mContext, Global.PREF_AUTO_SCAN_ADD).equals("1"))
																	{
																		((EditText) mRootView.findViewById(R.id.editItemNo)).setText("");
																		((EditText) mRootView.findViewById(R.id.editQty)).setText("");
																		((EditText) mRootView.findViewById(R.id.editCase)).setText("");
																	}
																	showKeyboard((EditText) mRootView.findViewById(R.id.editCase), true);
																} else if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
																	UOrderDInfo info = new UOrderDInfo();
																	info.strItemNo = selProductInfo.strItemNo;
																	info.strProdGroup = selProductInfo.strProdGrp;
																	info.nCustNo = Global.gCurCustomerInfo.nCustNo;
																	if(!mIsPickup)
																	{
																		if(isFreeItem)
																		{
																			info.nTrxnType = Global.TRXN_SAMPLE;
																		}
																		else
																		{
																			info.nTrxnType = Global.TRXN_DELIVER;
																		}
																	}
																	else
																	{
																		info.nTrxnType = Global.TRXN_PICKUP;
																	}
																	if (mIsPickup == false) {
																		info.nSales = finalNQuantity1;
																	} else {
																		info.nDumps = finalNQuantity1;
																	}
																	info.strShortDesc = selProductInfo.strShortDesc;
																	info.strDescription = selProductInfo.strDescription;
																	if(info.nTrxnType != Global.TRXN_SAMPLE)
																	{
																		info.nPrice = selProductInfo.nPrice;
																		info.nGrossPrice = selProductInfo.nGrossPrice;
																		info.nOrgPrice = selProductInfo.nOrgPrice;
																		info.nOrgGrossPrice = selProductInfo.nOrgGrossPrice;
																		info.nDiscountVal = selProductInfo.nDiscountVal;
																	}

																	info.nPromoType = selProductInfo.nPromoType;
																	info.nPromoDiscount = selProductInfo.nPromoDiscount;
																	if(info.nTrxnType != Global.TRXN_SAMPLE)
																	{
																		info.nBasePrice = Utils.getDividedValue(selProductInfo.prodLocnInfo.nBasePrice);
																	}
																	info.nLocnNo = selProductInfo.prodLocnInfo.nLocnNo;
																	info.strFullCase = selProductInfo.prodLocnInfo.strFullCase;
																	info.nSCWType = selProductInfo.prodLocnInfo.nSCWType;
																	if(selProductInfo != null && selProductInfo.prodLocnInfo != null)
																	{
																		info.nCaseFactor = selProductInfo.prodLocnInfo.nCaseFactor;
																	}

																	if(info.uPromotionInfoList.size() == 0 && selProductInfo.uPromotionInfoList.size() > 0)
																	{
																		for(UPromotionInfo uPromotionInfo : selProductInfo.uPromotionInfoList)
																		{
																			UPromotionInfo uItem = uPromotionInfo.duplicate();
																			uItem.nTrxnType = (mIsPickup) ? Global.TRXN_PICKUP : Global.TRXN_DELIVER;
																			uItem.nCustNo = Global.gCurCustomerInfo.nCustNo;
																			uItem.nChainNo = Global.gCurCustomerInfo.nChainNo;
																			uItem.strItemNo = info.strItemNo;
																			info.uPromotionInfoList.add(uItem);
																		}
																	}


																	mUOrderDList.add(0, info);
																	m_bModified = true;
																	if(bShowCase)
																	{
																		if( Global.gCurCustomerInfo.strSalEntryMode.equals("B") || Global.gCurCustomerInfo.strSalEntryMode.equals("C"))
																		{
																			mRootView.findViewById(R.id.editCase).setVisibility(View.VISIBLE);
																			mRootView.findViewById(R.id.lblSep).setVisibility(View.VISIBLE);
																		}
																	}
																	else
																	{
																		mRootView.findViewById(R.id.editCase).setVisibility(View.GONE);
																		mRootView.findViewById(R.id.lblSep).setVisibility(View.GONE);
																	}
																	((EditText) mRootView.findViewById(R.id.editItemNo)).setText("");
																	((EditText) mRootView.findViewById(R.id.editCase)).setText("");
																	((EditText) mRootView.findViewById(R.id.editQty)).setText("");
																	focusAndHideKeyboard((EditText) mRootView.findViewById(R.id.editItemNo));
																	refreshPage();
																	mSelIdx = -1;
																	showReasonCode();
																	showPriceInven();

																	doCheckBatchLot(info);

																	doProcessMultiPromotion(info, selProductInfo, nInputQty, isSimFreeMode);
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
											bProcessingBarcodeScan = false;
											return;
										}
										else if( m_nInventoryValidationType == 4 )
										{
											new Handler(Looper.getMainLooper()).post(new Runnable() {
												@Override
												public void run() {
													try {
														final InventoryMessageDialog msgDialog = new InventoryMessageDialog(mContext);
														msgDialog.setCancelable(false);
														String strStockCaption = "";
														String strPresoldCaption = "";

														String strAvailable = "";
														if(bShowCase)
														{
															if( Global.gCurCustomerInfo.strSalEntryMode.equals("B") || Global.gCurCustomerInfo.strSalEntryMode.equals("C"))
															{
																if (Utils.isContainerItem(mContext, selProductInfo.strItemNo)) {
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
														msgDialog.mStrCustomerName = selProductInfo.strItemNo + "\n" + selProductInfo.strDescription;
														msgDialog.mStrMessage = String.format(Locale.ENGLISH, Str.The_quantity_entered_exceeds_the_reserved_inventory_levels[Global.LANG], strPresoldCaption, strAvailable, strStockCaption);
														msgDialog.mStrSubMessage = Str.Reenter_the_quantity_to_continue[Global.LANG];
														msgDialog.mStrMiddle = Str.Return[Global.LANG];
														msgDialog.setOnDismissListener(new OnDismissListener() {
															@Override
															public void onDismiss(DialogInterface dialog) {
																if (msgDialog.mReturnCode == Global.DLG_RETURN_MIDDLE) {
																	if(Utils.getStringSetting(mContext, Global.PREF_AUTO_SCAN_ADD).equals("1"))
																	{
																		((EditText) mRootView.findViewById(R.id.editItemNo)).setText("");
																		((EditText) mRootView.findViewById(R.id.editQty)).setText("");
																		((EditText) mRootView.findViewById(R.id.editCase)).setText("");
																	}
																	showKeyboard((EditText) mRootView.findViewById(R.id.editCase), true);
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
											bProcessingBarcodeScan = false;
											return;
										}
									}
								}
							}

							// Check Max quantity allowed for return by ShelfLife
							if (mIsPickup == true) {
								if (Utils.getStringSetting(mContext, Global.PREF_CHECK_RETURN_QTY).equals("1")) {
									String strItemNo = selProductInfo.strItemNo;

									// get sales made today
									int nTodaySales = 0;

									List<UInvenDInfo> invenDInfos = new ArrayList<>();
									DbAdapter dbAdapter = new DbAdapter(mContext);
									dbAdapter.open();
									invenDInfos = dbAdapter.getUInvenDList(strItemNo);
									dbAdapter.close();
									for (UInvenDInfo invenDInfo: invenDInfos) {
										nTodaySales += invenDInfo.nSales;
									}

									int nSAQtySum = 0;
									int nDUQtySum = 0;
									int nBBQtySum = 0;

									int nShelfLife = selProductInfo.nShelfLife;
									List<OrderHistoryInfo> orderHistoryInfos = new ArrayList<>();
									dbAdapter = new DbAdapter(mContext);
									dbAdapter.open();
									orderHistoryInfos = dbAdapter.getOrderHistoryList(Global.gCurCustomerInfo.nCustNo, strItemNo);
									dbAdapter.close();

									Date tripDate = Utils.getRoutectlTripDate(mContext, new Date());
									Calendar calendar = Calendar.getInstance();
									calendar.setTime(tripDate);
									calendar.add(Calendar.DAY_OF_MONTH, -1*nShelfLife);
									Date limitDate = calendar.getTime();
									String limitDateString = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(limitDate);

									for (OrderHistoryInfo info: orderHistoryInfos) {
										if (nShelfLife > 0) {
											if (info.strOrderDate.compareTo(limitDateString) < 0) {
												continue;
											}
										}

										nSAQtySum += info.nSAQty / 100;
										nDUQtySum += info.nDUQty / 100;
										nBBQtySum += info.nBBQty / 100;
									}

									int nMaxQty = nSAQtySum+nTodaySales-nDUQtySum-nBBQtySum;
									if(nQuantity > nMaxQty)
									{
										new Handler(Looper.getMainLooper()).post(new Runnable() {
											@Override
											public void run() {
												try {

													String strMessage = String.format(Locale.ENGLISH, Str.MaxReturnQtyIs[Global.LANG], nMaxQty);
													final MessageDialog msgDialog = new MessageDialog(mContext);
													msgDialog.setCancelable(false);
													msgDialog.mStrMessage = strMessage;
													msgDialog.mStrMiddle = Str.Return[Global.LANG];
													msgDialog.setOnDismissListener(new OnDismissListener() {
														@Override
														public void onDismiss(DialogInterface dialog) {
															if(Utils.getStringSetting(mContext, Global.PREF_AUTO_SCAN_ADD).equals("1"))
															{
																((EditText) mRootView.findViewById(R.id.editItemNo)).setText("");
																((EditText) mRootView.findViewById(R.id.editQty)).setText("");
																((EditText) mRootView.findViewById(R.id.editCase)).setText("");
															}
															focusAndHideKeyboard((EditText) mRootView.findViewById(R.id.editItemNo));
														}
													});
													msgDialog.show();
												}catch (Exception e)
												{
													e.printStackTrace();
												}

											}
										});
										bProcessingBarcodeScan = false;
										return;
									}
								}
							}

							if(!futureOrder)
							{
								//Check Max Order Size
								if(selProductInfo != null && selProductInfo.nMaxQty > 0 && nQuantity > selProductInfo.nMaxQty && nQuantity != 0)
								{
									new Handler(Looper.getMainLooper()).post(new Runnable() {
										@Override
										public void run() {
											String strMessage = String.format(Locale.ENGLISH, Str.The_quantity_you_have_entered_exceeds_the_maximum_allowed_2[Global.LANG], (int)mSelProductInfo.nMaxQty, strCartonDescription) + "\n";
											try {
												final MessageDialog msgDialog = new MessageDialog(mContext);
												msgDialog.setCancelable(false);
												msgDialog.mStrMessage = strMessage;
												msgDialog.mStrMiddle = Str.Return[Global.LANG];
												msgDialog.mStrTitle = Str.Error[Global.LANG];
												msgDialog.setOnDismissListener(new OnDismissListener() {
													@Override
													public void onDismiss(DialogInterface dialog) {
														if(Utils.getStringSetting(mContext, Global.PREF_AUTO_SCAN_ADD).equals("1"))
														{
															((EditText) mRootView.findViewById(R.id.editItemNo)).setText("");
															((EditText) mRootView.findViewById(R.id.editQty)).setText("");
															((EditText) mRootView.findViewById(R.id.editCase)).setText("");
														}
														focusAndHideKeyboard((EditText) mRootView.findViewById(R.id.editItemNo));
													}
												});
												msgDialog.show();
											}catch (Exception e)
											{
												e.printStackTrace();
											}

										}
									});
									bProcessingBarcodeScan = false;
									return;
								}
							}


							if(mEntryInputMethod.equals("A"))
							{
								if(nQuantity < 0)
								{
									new Handler(Looper.getMainLooper()).post(new Runnable() {
										@Override
										public void run() {
											((EditText) mRootView.findViewById(R.id.editQty)).setText("");
											((EditText) mRootView.findViewById(R.id.editCase)).setText("");
											Utils.printDebug(mContext, Str.Quantity_Removed_From_the_transaction[Global.LANG]);
											showKeyboard((EditText) mRootView.findViewById(R.id.editQty), true);
										}
									});
									bProcessingBarcodeScan = false;
									return;
								}
							}

							UOrderDInfo info = new UOrderDInfo();
							info.strItemNo = selProductInfo.strItemNo;
							info.strProdGroup = selProductInfo.strProdGrp;
							info.nCustNo = Global.gCurCustomerInfo.nCustNo;
							if(!mIsPickup)
							{
								if(isFreeItem)
								{
									info.nTrxnType = Global.TRXN_SAMPLE;
								}
								else
								{
									info.nTrxnType = Global.TRXN_DELIVER;
								}
							}
							else
							{
								info.nTrxnType = Global.TRXN_PICKUP;
							}
							if (mIsPickup == false) {
								info.nSales = nQuantity;
							} else {
								info.nDumps = nQuantity;
							}
							info.strShortDesc = selProductInfo.strShortDesc;
							info.strDescription = selProductInfo.strDescription;
							if(info.nTrxnType != Global.TRXN_SAMPLE)
							{
								info.nPrice = selProductInfo.nPrice;
								info.nGrossPrice = selProductInfo.nGrossPrice;
								info.nOrgPrice = selProductInfo.nOrgPrice;
								info.nOrgGrossPrice = selProductInfo.nOrgGrossPrice;
								info.nDiscountVal = selProductInfo.nDiscountVal;
							}

							info.nPromoType = selProductInfo.nPromoType;
							info.nPromoDiscount = selProductInfo.nPromoDiscount;
							if(info.nTrxnType != Global.TRXN_SAMPLE)
							{
								info.nBasePrice = Utils.getDividedValue(selProductInfo.prodLocnInfo.nBasePrice);
							}
							info.nLocnNo = selProductInfo.prodLocnInfo.nLocnNo;
							info.strFullCase = selProductInfo.prodLocnInfo.strFullCase;
							info.nSCWType = selProductInfo.prodLocnInfo.nSCWType;
							if( selProductInfo != null && selProductInfo.prodLocnInfo != null)
							{
								info.nCaseFactor = selProductInfo.prodLocnInfo.nCaseFactor;
							}
							for(UPromotionInfo uPromotionInfo : selProductInfo.uPromotionInfoList)
							{
								UPromotionInfo uItem = uPromotionInfo.duplicate();
								uItem.nTrxnType = (mIsPickup) ? Global.TRXN_PICKUP : Global.TRXN_DELIVER;
								uItem.nCustNo = Global.gCurCustomerInfo.nCustNo;
								uItem.nChainNo = Global.gCurCustomerInfo.nChainNo;
								uItem.strItemNo = info.strItemNo;
								info.uPromotionInfoList.add(uItem);
							}

							mUOrderDList.add(0, info);

							doCheckBatchLot(info);

							doProcessMultiPromotion(info, selProductInfo, nQuantity, isSimFreeMode);
						}
					} else {

						if(mUOrderDList.get(nIdx).nPlanQty != 0 && isFreeItem)
						{
							String strAvailableType = "Y";
							DbAdapter dbAdapter = new DbAdapter(mContext);
							dbAdapter.open();
							List<DescTypeInfo> typeInfoList = dbAdapter.getDescTypeListWithNumericKey("FREERSN",  mUOrderDList.get(nIdx).nReasonCode);
							dbAdapter.close();
							if(typeInfoList != null && typeInfoList.size() > 0)
							{
								strAvailableType = typeInfoList.get(0).strValue1;
							}

					/*if(strAvailableType.equals("N") && mUOrderDList.get(nIdx).nSales != nQuantity)
					{
						final MessageDialog msgDialog = new MessageDialog(mContext);
						msgDialog.setCancelable(false);
						msgDialog.mStrMessage = Str.YouCanNotChangeThisItem[Global.LANG];
						msgDialog.mStrMiddle = Str.Ok[Global.LANG];
						msgDialog.show();
						return;
					}
					else */
							if(!futureOrder)
							{
								if(strAvailableType.equals("D") )
								{
									if(mUOrderDList.get(nIdx).nSales < nQuantity)
									{
										new Handler(Looper.getMainLooper()).post(new Runnable() {
											@Override
											public void run() {
												try {
													if(Utils.getStringSetting(mContext, Global.PREF_AUTO_SCAN_ADD).equals("1"))
													{
														((EditText) mRootView.findViewById(R.id.editItemNo)).setText("");
														((EditText) mRootView.findViewById(R.id.editQty)).setText("");
														((EditText) mRootView.findViewById(R.id.editCase)).setText("");
													}
													final MessageDialog msgDialog = new MessageDialog(mContext);
													msgDialog.setCancelable(false);
													msgDialog.mStrMessage = Str.YouCanOnlyDecreaseThisItem[Global.LANG];
													msgDialog.mStrMiddle = Str.Ok[Global.LANG];
													msgDialog.show();
												}catch (Exception e)
												{
													e.printStackTrace();
												}

											}
										});
										bProcessingBarcodeScan = false;
										return;
									}

								}
								else if(!strAvailableType.equals("Y"))
								{
									if( mUOrderDList.get(nIdx).nSales != nQuantity)
									{
										new Handler(Looper.getMainLooper()).post(new Runnable() {
											@Override
											public void run() {
												try {
													if(Utils.getStringSetting(mContext, Global.PREF_AUTO_SCAN_ADD).equals("1"))
													{
														((EditText) mRootView.findViewById(R.id.editItemNo)).setText("");
														((EditText) mRootView.findViewById(R.id.editQty)).setText("");
														((EditText) mRootView.findViewById(R.id.editCase)).setText("");
													}
													final MessageDialog msgDialog = new MessageDialog(mContext);
													msgDialog.setCancelable(false);
													msgDialog.mStrMessage = Str.YouCanNotChangeThisItem[Global.LANG];
													msgDialog.mStrMiddle = Str.Ok[Global.LANG];
													msgDialog.show();
												}catch (Exception e)
												{
													e.printStackTrace();
												}

											}
										});
										bProcessingBarcodeScan = false;
										return;
									}
								}
							}
						}

						if(!futureOrder)
						{
							if(selProductInfo != null && selProductInfo.nMaxQty > 0 && nQuantity > selProductInfo.nMaxQty && nQuantity != 0)
							{
								new Handler(Looper.getMainLooper()).post(new Runnable() {
									@Override
									public void run() {
										String strMessage = String.format(Locale.ENGLISH, Str.The_quantity_you_have_entered_exceeds_the_maximum_allowed_2[Global.LANG], (int)mSelProductInfo.nMaxQty, strCartonDescription) + "\n";
										try {
											final MessageDialog msgDialog = new MessageDialog(mContext);
											msgDialog.setCancelable(false);
											msgDialog.mStrTitle = Str.Error[Global.LANG];
											msgDialog.mStrMessage = strMessage;
											msgDialog.mStrMiddle = Str.Return[Global.LANG];
											msgDialog.setOnDismissListener(new OnDismissListener() {
												@Override
												public void onDismiss(DialogInterface dialog) {
													if(Utils.getStringSetting(mContext, Global.PREF_AUTO_SCAN_ADD).equals("1"))
													{
														((EditText) mRootView.findViewById(R.id.editItemNo)).setText("");
														((EditText) mRootView.findViewById(R.id.editQty)).setText("");
														((EditText) mRootView.findViewById(R.id.editCase)).setText("");
													}
													focusAndHideKeyboard((EditText) mRootView.findViewById(R.id.editItemNo));
												}
											});
											msgDialog.show();
										}catch (Exception e)
										{
											e.printStackTrace();
										}

									}
								});
								bProcessingBarcodeScan = false;
								return;
							}
						}

						if(mEntryInputMethod.equals("A"))
						{
							if(mIsPickup)
							{
								nQuantity = nQuantity + mUOrderDList.get(nIdx).nDumps;
							}
							else
							{
								nQuantity = nQuantity + mUOrderDList.get(nIdx).nSales;
							}
							if(nQuantity < 0)
							{
								new Handler(Looper.getMainLooper()).post(new Runnable() {
									@Override
									public void run() {
										((EditText) mRootView.findViewById(R.id.editQty)).setText("");
										((EditText) mRootView.findViewById(R.id.editCase)).setText("");
										Utils.printDebug(mContext, Str.Quantity_Removed_From_the_transaction[Global.LANG]);
										showKeyboard((EditText) mRootView.findViewById(R.id.editQty), true);
									}
								});
								bProcessingBarcodeScan = false;
								return;
							}
						}

						if (nQuantity == 0 && mUOrderDList.get(nIdx).nPlanQty == 0) {
							UOrderDInfo orderDInfo = mUOrderDList.get(nIdx);
							mUOrderDList.remove(orderDInfo);
							doProcessMultiPromotion(orderDInfo, selProductInfo, nInputQty, isSimFreeMode);
						} else {
							// Check Max quantity allowed for return by ShelfLife
							if (mIsPickup == true) {
								if (Utils.getStringSetting(mContext, Global.PREF_CHECK_RETURN_QTY).equals("1")) {
									String strItemNo = selProductInfo.strItemNo;

									// get sales made today
									int nTodaySales = 0;

									List<UInvenDInfo> invenDInfos = new ArrayList<>();
									DbAdapter dbAdapter = new DbAdapter(mContext);
									dbAdapter.open();
									invenDInfos = dbAdapter.getUInvenDList(strItemNo);
									dbAdapter.close();
									for (UInvenDInfo invenDInfo: invenDInfos) {
										nTodaySales += invenDInfo.nSales;
									}

									int nSAQtySum = 0;
									int nDUQtySum = 0;
									int nBBQtySum = 0;

									int nShelfLife = selProductInfo.nShelfLife;
									List<OrderHistoryInfo> orderHistoryInfos = new ArrayList<>();
									dbAdapter = new DbAdapter(mContext);
									dbAdapter.open();
									orderHistoryInfos = dbAdapter.getOrderHistoryList(Global.gCurCustomerInfo.nCustNo, strItemNo);
									dbAdapter.close();

									Date tripDate = Utils.getRoutectlTripDate(mContext, new Date());
									Calendar calendar = Calendar.getInstance();
									calendar.setTime(tripDate);
									calendar.add(Calendar.DAY_OF_MONTH, -1*nShelfLife);
									Date limitDate = calendar.getTime();
									String limitDateString = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(limitDate);

									for (OrderHistoryInfo info: orderHistoryInfos) {
										if (nShelfLife > 0) {
											if (info.strOrderDate.compareTo(limitDateString) < 0) {
												continue;
											}
										}

										nSAQtySum += info.nSAQty / 100;
										nDUQtySum += info.nDUQty / 100;
										nBBQtySum += info.nBBQty / 100;
									}

									int nMaxQty = nSAQtySum+nTodaySales-nDUQtySum-nBBQtySum;
									if(nQuantity > nMaxQty)
									{
										new Handler(Looper.getMainLooper()).post(new Runnable() {
											@Override
											public void run() {
												try {

													String strMessage = String.format(Locale.ENGLISH, Str.MaxReturnQtyIs[Global.LANG], nMaxQty);
													final MessageDialog msgDialog = new MessageDialog(mContext);
													msgDialog.setCancelable(false);
													msgDialog.mStrMessage = strMessage;
													msgDialog.mStrMiddle = Str.Return[Global.LANG];
													msgDialog.setOnDismissListener(new OnDismissListener() {
														@Override
														public void onDismiss(DialogInterface dialog) {
															if(Utils.getStringSetting(mContext, Global.PREF_AUTO_SCAN_ADD).equals("1"))
															{
																((EditText) mRootView.findViewById(R.id.editItemNo)).setText("");
																((EditText) mRootView.findViewById(R.id.editQty)).setText("");
																((EditText) mRootView.findViewById(R.id.editCase)).setText("");
															}
															focusAndHideKeyboard((EditText) mRootView.findViewById(R.id.editItemNo));
														}
													});
													msgDialog.show();
												}catch (Exception e)
												{
													e.printStackTrace();
												}

											}
										});
										bProcessingBarcodeScan = false;
										return;
									}
								}
							}

							if (mIsPickup == false) {
								if(!futureOrder)
								{
									if(TextUtils.isEmpty(strModifiable) || strModifiable.equals("N"))
									{
										if(nQuantity != mUOrderDList.get(nIdx).nPlanQty)
										{
											new Handler(Looper.getMainLooper()).post(new Runnable() {
												@Override
												public void run() {
													try {
														if(Utils.getStringSetting(mContext, Global.PREF_AUTO_SCAN_ADD).equals("1"))
														{
															((EditText) mRootView.findViewById(R.id.editItemNo)).setText("");
															((EditText) mRootView.findViewById(R.id.editQty)).setText("");
															((EditText) mRootView.findViewById(R.id.editCase)).setText("");
														}
														MessageDialog messageDialog = new MessageDialog(mContext);
														messageDialog.mStrMessage = Str.NoChangesAllowed[Global.LANG];
														messageDialog.mStrMiddle = Str.Ok[Global.LANG];
														messageDialog.show();
													}catch (Exception e)
													{
														e.printStackTrace();
													}

												}
											});
											bProcessingBarcodeScan = false;
											return;
										}
									}
									else if(strModifiable.equals("D") || strModifiable.equals("X"))
									{
										if(nQuantity > mUOrderDList.get(nIdx).nPlanQty)
										{
											new Handler(Looper.getMainLooper()).post(new Runnable() {
												@Override
												public void run() {
													try {
														if(Utils.getStringSetting(mContext, Global.PREF_AUTO_SCAN_ADD).equals("1"))
														{
															((EditText) mRootView.findViewById(R.id.editItemNo)).setText("");
															((EditText) mRootView.findViewById(R.id.editQty)).setText("");
															((EditText) mRootView.findViewById(R.id.editCase)).setText("");
														}
														MessageDialog messageDialog = new MessageDialog(mContext);
														messageDialog.mStrMessage = Str.YouCanOnlyDecreaseQuantity[Global.LANG];
														messageDialog.mStrMiddle = Str.Ok[Global.LANG];
														messageDialog.show();
													}catch (Exception e)
													{
														e.printStackTrace();
													}

												}
											});
											bProcessingBarcodeScan = false;
											return;
										}
									}

									final int nStock = getStockValue(mUOrderDList.get(nIdx).strItemNo);

									FreeItemDetails freeItemDetails = getFreeItemDetailsByPromotion(selProductInfo, nQuantity);
									if (freeItemDetails != null && freeItemDetails.nFreeItemCount != 0) {
										String strFreeItemNo = freeItemDetails.strFreeItemNo;

										final int nFreeItemStock = getStockValue(freeItemDetails.strFreeItemNo);
										if (strFreeItemNo.equals(selProductInfo.strItemNo)) {
											if (nQuantity + freeItemDetails.nFreeItemCount > nStock && mUOrderDList.get(nIdx).nTrxnType != Global.TRXN_FREE && mUOrderDList.get(nIdx).nTrxnType != Global.TRXN_SAMPLE) {
												if (m_nInventoryValidationType == 2 || m_nInventoryValidationType == 4)
												{
													int finalNQuantity = nQuantity;
													new Handler(Looper.getMainLooper()).post(new Runnable() {
														@Override
														public void run() {
															try {
																final InventoryMessageDialog msgDialog = new InventoryMessageDialog(mContext);
																msgDialog.setCancelable(false);
																msgDialog.mStrTitle = Str.Warning[Global.LANG];
																msgDialog.mStrCustomerName = selProductInfo.strItemNo + "\n" + selProductInfo.strDescription;
																msgDialog.mStrMessage = String.format(Locale.ENGLISH, "You do not have enough stock to sell %d and provide %d Promotional Items. Suggest you change to sell less", finalNQuantity, freeItemDetails.nFreeItemCount);
																msgDialog.mStrMiddle = Str.Return[Global.LANG];
																msgDialog.setOnDismissListener(new OnDismissListener() {
																	@Override
																	public void onDismiss(DialogInterface dialog) {
																		if (msgDialog.mReturnCode == Global.DLG_RETURN_MIDDLE) {
																			if(Utils.getStringSetting(mContext, Global.PREF_AUTO_SCAN_ADD).equals("1"))
																			{
																				((EditText) mRootView.findViewById(R.id.editItemNo)).setText("");
																				((EditText) mRootView.findViewById(R.id.editQty)).setText("");
																				((EditText) mRootView.findViewById(R.id.editCase)).setText("");
																			}
																			showKeyboard((EditText) mRootView.findViewById(R.id.editCase), true);
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
													bProcessingBarcodeScan = false;
													return;
												}
											}
										}
										else {
											if ( (nQuantity > nStock || freeItemDetails.nFreeItemCount > nFreeItemStock) && mUOrderDList.get(nIdx).nTrxnType != Global.TRXN_FREE && mUOrderDList.get(nIdx).nTrxnType != Global.TRXN_SAMPLE) {
												if (m_nInventoryValidationType == 2 || m_nInventoryValidationType == 4)
												{
													int finalNQuantity = nQuantity;
													new Handler(Looper.getMainLooper()).post(new Runnable() {
														@Override
														public void run() {
															try {
																final InventoryMessageDialog msgDialog = new InventoryMessageDialog(mContext);
																msgDialog.setCancelable(false);

																msgDialog.mStrTitle = Str.Warning[Global.LANG];
																msgDialog.mStrCustomerName = selProductInfo.strItemNo + "\n" + selProductInfo.strDescription;
																msgDialog.mStrMessage = String.format(Locale.ENGLISH, "You do not have enough stock to sell %d and provide %d Promotional Items. Suggest you change to sell less", finalNQuantity, freeItemDetails.nFreeItemCount);
																msgDialog.mStrMiddle = Str.Ok[Global.LANG];
																msgDialog.setOnDismissListener(new OnDismissListener() {
																	@Override
																	public void onDismiss(DialogInterface dialog) {
																		if (msgDialog.mReturnCode == Global.DLG_RETURN_MIDDLE) {
																			if(Utils.getStringSetting(mContext, Global.PREF_AUTO_SCAN_ADD).equals("1"))
																			{
																				((EditText) mRootView.findViewById(R.id.editItemNo)).setText("");
																				((EditText) mRootView.findViewById(R.id.editQty)).setText("");
																				((EditText) mRootView.findViewById(R.id.editCase)).setText("");
																			}
																			showKeyboard((EditText) mRootView.findViewById(R.id.editCase), true);
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
													bProcessingBarcodeScan = false;
													return;
												}
											}
										}
									}

									if( nQuantity != 0 && nQuantity > nStock /*&& nStock > 0*/ && mUOrderDList.get(nIdx).nTrxnType != Global.TRXN_FREE && mUOrderDList.get(nIdx).nTrxnType != Global.TRXN_SAMPLE)
									{
										if( m_nInventoryValidationType == 1 )
										{
											final int nQuantity6 = nQuantity;
											final int nIdx6 = nIdx;
											new Handler(Looper.getMainLooper()).post(new Runnable() {
												@Override
												public void run() {
													try {
														final InventoryMessageDialog msgDialog = new InventoryMessageDialog(mContext);
														msgDialog.setCancelable(false);
														String strCaption = "";
														if(bShowCase)
														{
															if( Global.gCurCustomerInfo.strSalEntryMode.equals("B") || Global.gCurCustomerInfo.strSalEntryMode.equals("C"))
															{
																if (Utils.isContainerItem(mContext, selProductInfo.strItemNo)) {
																	strCaption = String.format(Locale.ENGLISH, "%d.", nStock);
																}
																else {
																	strCaption = String.format(Locale.ENGLISH, "%d / %d.", nStock / nCase, nStock % nCase);
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
														msgDialog.mStrCustomerName = selProductInfo.strItemNo + "\n" + selProductInfo.strDescription;
														msgDialog.mStrMessage = String.format(Locale.ENGLISH, Str.The_quantity_you_have_entered_exceeds_the_available_stock_of[Global.LANG], strCaption);
														msgDialog.mStrSubMessage = Str.Ignore_the_oversell[Global.LANG];
														msgDialog.mStrLeft = Str.No[Global.LANG];
														msgDialog.mStrRight = Str.Yes[Global.LANG];
														final int finalNQuantity = nQuantity6;
														final int finalNIdx = nIdx6;
														msgDialog.setOnDismissListener(new OnDismissListener() {
															@Override
															public void onDismiss(DialogInterface dialog) {
																if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
																	if(Utils.getStringSetting(mContext, Global.PREF_AUTO_SCAN_ADD).equals("1"))
																	{
																		((EditText) mRootView.findViewById(R.id.editItemNo)).setText("");
																		((EditText) mRootView.findViewById(R.id.editQty)).setText("");
																		((EditText) mRootView.findViewById(R.id.editCase)).setText("");
																	}
																	showKeyboard((EditText) mRootView.findViewById(R.id.editCase), true);
																} else if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
																	mUOrderDList.get(finalNIdx).nSales = finalNQuantity;

																	if(bShowCase)
																	{
																		if( Global.gCurCustomerInfo.strSalEntryMode.equals("B") || Global.gCurCustomerInfo.strSalEntryMode.equals("C"))
																		{
																			mRootView.findViewById(R.id.editCase).setVisibility(View.VISIBLE);
																			mRootView.findViewById(R.id.lblSep).setVisibility(View.VISIBLE);
																		}
																	}
																	else
																	{
																		mRootView.findViewById(R.id.editCase).setVisibility(View.GONE);
																		mRootView.findViewById(R.id.lblSep).setVisibility(View.GONE);
																	}

																	((EditText) mRootView.findViewById(R.id.editItemNo)).setText("");
																	((EditText) mRootView.findViewById(R.id.editCase)).setText("");
																	((EditText) mRootView.findViewById(R.id.editQty)).setText("");
																	focusAndHideKeyboard((EditText) mRootView.findViewById(R.id.editItemNo));
																	m_bModified = true;
																	refreshPage();
																	doCheckBatchLot(mUOrderDList.get(finalNIdx));

																	doProcessMultiPromotion(mUOrderDList.get(finalNIdx), selProductInfo, nInputQty, isSimFreeMode);
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
											bProcessingBarcodeScan = false;
											return;
										}
										else if( m_nInventoryValidationType == 2 )
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
															if( Global.gCurCustomerInfo.strSalEntryMode.equals("B") || Global.gCurCustomerInfo.strSalEntryMode.equals("C"))
															{
																if (Utils.isContainerItem(mContext, selProductInfo.strItemNo)) {
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
														msgDialog.mStrCustomerName = selProductInfo.strItemNo + "\n" + selProductInfo.strDescription;
														msgDialog.mStrMessage = String.format(Locale.ENGLISH, Str.The_quantity_you_have_entered_exceeds_the_available_stock_of[Global.LANG], strCaption);
														msgDialog.mStrSubMessage = Str.Reenter_the_quantity_to_continue[Global.LANG];
														msgDialog.mStrMiddle = Str.Return[Global.LANG];
														msgDialog.setOnDismissListener(new OnDismissListener() {
															@Override
															public void onDismiss(DialogInterface dialog) {
																if (msgDialog.mReturnCode == Global.DLG_RETURN_MIDDLE) {
																	if(Utils.getStringSetting(mContext, Global.PREF_AUTO_SCAN_ADD).equals("1"))
																	{
																		((EditText) mRootView.findViewById(R.id.editItemNo)).setText("");
																		((EditText) mRootView.findViewById(R.id.editQty)).setText("");
																		((EditText) mRootView.findViewById(R.id.editCase)).setText("");
																	}
																	showKeyboard((EditText) mRootView.findViewById(R.id.editCase), true);
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
											bProcessingBarcodeScan = false;
											return;
										}
									}
									final int nPresold = getPresoldValue(selProductInfo.strItemNo);
									int nOrderPresoldValue = (nIdx >= 0) ? mUOrderDList.get(nIdx).nPresoldValue : 0;
									int nPeddle = nStock - nPresold;
									int nAvailable = 0;
									if(nStock > nOrderPresoldValue) {
										nAvailable = nOrderPresoldValue;
									}
									else {
										nAvailable = nStock;
									}
									if(nPeddle > 0)
									{
										nAvailable += nPeddle;
									}
									if( nQuantity > nAvailable  && nQuantity != 0)
									{
										if (m_nInventoryValidationType == 3) {
											final int nQuantity7 = nQuantity;
											final int nIdx7 = nIdx;
											new Handler(Looper.getMainLooper()).post(new Runnable() {
												@Override
												public void run() {
													try {
														final InventoryMessageDialog msgDialog = new InventoryMessageDialog(mContext);
														msgDialog.setCancelable(false);
														String strStockCaption = "";
														String strPresoldCaption = "";
														String strAvailable = "";
														if(bShowCase)
														{
															if (Global.gCurCustomerInfo.strSalEntryMode.equals("B") ||Global.gCurCustomerInfo.strSalEntryMode.equals("C")) {
																if (Utils.isContainerItem(mContext, selProductInfo.strItemNo)) {
																	strStockCaption = String.format(Locale.ENGLISH, "%d", nStock);
																	strPresoldCaption = String.format(Locale.ENGLISH, "%d", nPresold);
																	strAvailable = String.format(Locale.ENGLISH, "%d", nStock - nPresold);
																}
																else {
																	strStockCaption = String.format(Locale.ENGLISH, "%d / %d", nStock / nCase, nStock % nCase);
																	strPresoldCaption = String.format(Locale.ENGLISH, "%d / %d", nPresold / nCase, nPresold % nCase);
																	strAvailable = String.format(Locale.ENGLISH, "%d / %d", (nStock - nPresold) / nCase, (nStock - nPresold) % nCase);
																}
															} else if (Global.gCurCustomerInfo.strSalEntryMode.equals("U") || Global.gCurCustomerInfo.strSalEntryMode.equals("")) {
																strStockCaption = String.format(Locale.ENGLISH, "%d", nStock);
																strPresoldCaption = String.format(Locale.ENGLISH, "%d", nPresold);
																strAvailable = String.format(Locale.ENGLISH, "%d", nStock - nPresold);
															}
														}
														else
														{
															strStockCaption = String.format(Locale.ENGLISH, "%d", nStock);
															strPresoldCaption = String.format(Locale.ENGLISH, "%d", nPresold);
															strAvailable = String.format(Locale.ENGLISH, "%d", nStock - nPresold);
														}

														msgDialog.mStrTitle = Str.Warning[Global.LANG];
														msgDialog.mStrCustomerName = selProductInfo.strItemNo + "\n" + selProductInfo.strDescription;
														msgDialog.mStrMessage = String.format(Locale.ENGLISH, Str.The_quantity_entered_exceeds_the_reserved_inventory_levels[Global.LANG], strPresoldCaption, strAvailable, strStockCaption);
														msgDialog.mStrSubMessage = Str.Ignore_the_oversell[Global.LANG];
														msgDialog.mStrLeft = Str.No[Global.LANG];
														msgDialog.mStrRight = Str.Yes[Global.LANG];
														final int finalNQuantity = nQuantity7;
														final int finalNIdx = nIdx7;
														msgDialog.setOnDismissListener(new OnDismissListener() {
															@Override
															public void onDismiss(DialogInterface dialog) {
																if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
																	if(Utils.getStringSetting(mContext, Global.PREF_AUTO_SCAN_ADD).equals("1"))
																	{
																		((EditText) mRootView.findViewById(R.id.editItemNo)).setText("");
																		((EditText) mRootView.findViewById(R.id.editQty)).setText("");
																		((EditText) mRootView.findViewById(R.id.editCase)).setText("");
																	}
																	showKeyboard((EditText) mRootView.findViewById(R.id.editCase), true);
																} else if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
																	mUOrderDList.get(finalNIdx).nSales = finalNQuantity;
																	if(bShowCase)
																	{
																		if( Global.gCurCustomerInfo.strSalEntryMode.equals("B") || Global.gCurCustomerInfo.strSalEntryMode.equals("C"))
																		{
																			mRootView.findViewById(R.id.editCase).setVisibility(View.VISIBLE);
																			mRootView.findViewById(R.id.lblSep).setVisibility(View.VISIBLE);
																		}
																	}
																	else
																	{
																		mRootView.findViewById(R.id.editCase).setVisibility(View.GONE);
																		mRootView.findViewById(R.id.lblSep).setVisibility(View.GONE);
																	}

																	((EditText) mRootView.findViewById(R.id.editItemNo)).setText("");
																	((EditText) mRootView.findViewById(R.id.editCase)).setText("");
																	((EditText) mRootView.findViewById(R.id.editQty)).setText("");
																	focusAndHideKeyboard((EditText) mRootView.findViewById(R.id.editItemNo));
																	m_bModified = true;
																	refreshPage();
																	doCheckBatchLot(mUOrderDList.get(finalNIdx));

																	doProcessMultiPromotion(mUOrderDList.get(finalNIdx), selProductInfo, nInputQty, isSimFreeMode);
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
											bProcessingBarcodeScan = false;
											return;
										} else if (m_nInventoryValidationType == 4) {
											new Handler(Looper.getMainLooper()).post(new Runnable() {
												@Override
												public void run() {
													try {
														final InventoryMessageDialog msgDialog = new InventoryMessageDialog(mContext);
														msgDialog.setCancelable(false);
														String strStockCaption = "";
														String strPresoldCaption = "";
														String strAvailable = "";
														if(bShowCase)
														{
															if (Global.gCurCustomerInfo.strSalEntryMode.equals("B") || Global.gCurCustomerInfo.strSalEntryMode.equals("C")) {
																if (Utils.isContainerItem(mContext, selProductInfo.strItemNo)) {
																	strStockCaption = String.format(Locale.ENGLISH, "%d", nStock);
																	strPresoldCaption = String.format(Locale.ENGLISH, "%d", nPresold);
																	strAvailable = String.format(Locale.ENGLISH, "%d", nStock - nPresold);
																}
																else {
																	strStockCaption = String.format(Locale.ENGLISH, "%d / %d", nStock / nCase, nStock % nCase);
																	strPresoldCaption = String.format(Locale.ENGLISH, "%d / %d", nPresold / nCase, nPresold % nCase);
																	strAvailable = String.format(Locale.ENGLISH, "%d / %d", (nStock - nPresold) / nCase, (nStock - nPresold) % nCase);
																}
															} else if (Global.gCurCustomerInfo.strSalEntryMode.equals("U") || Global.gCurCustomerInfo.strSalEntryMode.equals("")) {
																strStockCaption = String.format(Locale.ENGLISH, "%d", nStock);
																strPresoldCaption = String.format(Locale.ENGLISH, "%d", nPresold);
																strAvailable = String.format(Locale.ENGLISH, "%d", nStock - nPresold);
															}
														}
														else
														{
															strStockCaption = String.format(Locale.ENGLISH, "%d", nStock);
															strPresoldCaption = String.format(Locale.ENGLISH, "%d", nPresold);
															strAvailable = String.format(Locale.ENGLISH, "%d", nStock - nPresold);
														}
														msgDialog.mStrTitle = Str.Warning[Global.LANG];
														msgDialog.mStrCustomerName = selProductInfo.strItemNo + "\n" + selProductInfo.strDescription;
														msgDialog.mStrMessage = String.format(Locale.ENGLISH, Str.The_quantity_entered_exceeds_the_reserved_inventory_levels[Global.LANG], strPresoldCaption, strAvailable, strStockCaption);
														msgDialog.mStrSubMessage = Str.Reenter_the_quantity_to_continue[Global.LANG];
														msgDialog.mStrMiddle = Str.Return[Global.LANG];
														msgDialog.setOnDismissListener(new OnDismissListener() {
															@Override
															public void onDismiss(DialogInterface dialog) {
																if (msgDialog.mReturnCode == Global.DLG_RETURN_MIDDLE) {
																	if(Utils.getStringSetting(mContext, Global.PREF_AUTO_SCAN_ADD).equals("1"))
																	{
																		((EditText) mRootView.findViewById(R.id.editItemNo)).setText("");
																		((EditText) mRootView.findViewById(R.id.editQty)).setText("");
																		((EditText) mRootView.findViewById(R.id.editCase)).setText("");
																	}
																	showKeyboard((EditText) mRootView.findViewById(R.id.editCase), true);
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
											bProcessingBarcodeScan = false;
											return;
										}
									}
								}

								mUOrderDList.get(nIdx).nSales = nQuantity;
								doCheckBatchLot(mUOrderDList.get(nIdx));

								doProcessMultiPromotion(mUOrderDList.get(nIdx), selProductInfo, nInputQty, isSimFreeMode);
							} else {
								mUOrderDList.get(nIdx).nDumps = nQuantity;
								doCheckBatchLot(mUOrderDList.get(nIdx));

								doProcessMultiPromotion(mUOrderDList.get(nIdx), selProductInfo, nInputQty, isSimFreeMode);
							}
						}
					}
				}

				m_bModified = true;
				new Handler(Looper.getMainLooper()).post(new Runnable() {
					@Override
					public void run() {
						if(bShowCase)
						{
							if( Global.gCurCustomerInfo.strSalEntryMode.equals("B") || Global.gCurCustomerInfo.strSalEntryMode.equals("C"))
							{
								mRootView.findViewById(R.id.editCase).setVisibility(View.VISIBLE);
								mRootView.findViewById(R.id.lblSep).setVisibility(View.VISIBLE);
							}
						}
						else
						{
							mRootView.findViewById(R.id.editCase).setVisibility(View.GONE);
							mRootView.findViewById(R.id.lblSep).setVisibility(View.GONE);
						}
						((EditText) mRootView.findViewById(R.id.editItemNo)).setText("");
						((EditText) mRootView.findViewById(R.id.editCase)).setText("");
						((EditText) mRootView.findViewById(R.id.editQty)).setText("");
						focusAndHideKeyboard((EditText) mRootView.findViewById(R.id.editItemNo));
						refreshPage();
						bProcessingBarcodeScan = false;
						mSelIdx = -1;
						showReasonCode();
						showPriceInven();
					}
				});

			}
		}.start();

	}

	private boolean doCheckBatchLot(UOrderDInfo info)
	{
		ProdDetlInfo prodDetlInfo = mProdDetlItemMap.get(info.strItemNo);
		if(prodDetlInfo == null) return false;

		if(prodDetlInfo.strLotControl.equalsIgnoreCase("Y") && Utils.getIntSetting(mContext, Global.PREF_LOAD_CUSTOMER) != 1)
		{
			if(mIsPickup)
			{
				new Handler(Looper.getMainLooper()).post(new Runnable() {
					@Override
					public void run() {
						try {
							String strBatchCodes = "";
							for(LoadBlotInfo info : info.uLoadBlotInfo)
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
									if(i == info.uLoadBlotInfo.size())
									{
										LoadBlotInfo loadBlotInfo = new LoadBlotInfo();
										loadBlotInfo.strBatchDate = new SimpleDateFormat("yyyyMMdd").format(Utils.getRoutectlTripDate(mContext, new Date()));
										loadBlotInfo.strLotNumber = strBatchCodes1[i];
										loadBlotInfo.nLotQty = 1;
										info.uLoadBlotInfo.add(loadBlotInfo);
									}
									else
									{
										info.uLoadBlotInfo.get(i).strLotNumber = strBatchCodes1[i];
									}
								}
							}, info.nDumps, strBatchCodes, new HashMap<>());
							inputDialog.show();
						}catch (Exception e)
						{
							e.printStackTrace();
						}
					}
				});
				return true;
			}
			else
			{
				//Get the UInvenD For this Item.
				UInvenDInfo uSelInvenDInfo = null;
				for(UInvenDInfo uInvenDInfo : mUInvenHinfo.uInvenDList)
				{
					if(uInvenDInfo.bLotControl && uInvenDInfo.strItemNo.equals(info.strItemNo))
					{
						uSelInvenDInfo = uInvenDInfo;
						break;
					}
				}
				if(uSelInvenDInfo == null || uSelInvenDInfo.loadBlotInfoList == null || uSelInvenDInfo.loadBlotInfoList.size() < info.nSales)
				{
					return false;
				}
				else
				{
					List<LoadBlotInfo> uLoadBlot = new ArrayList<>();
					uLoadBlot.addAll(uSelInvenDInfo.loadBlotInfoList);
					if(Utils.getStringSetting(mContext, Global.PREF_LOT_ASSIGNMENT).equals("FIFO"))
					{
						Collections.sort(uLoadBlot, (o1, o2) -> o1.strLotNumber.compareTo(o2.strLotNumber));
						info.uLoadBlotInfo.clear();
						info.uLoadBlotInfo.addAll(uLoadBlot.subList(0, info.nSales));
					}
					else if(Utils.getStringSetting(mContext, Global.PREF_LOT_ASSIGNMENT).equals("LIFO"))
					{
						Collections.sort(uLoadBlot, (o1, o2) -> o2.strLotNumber.compareTo(o1.strLotNumber));
						info.uLoadBlotInfo.clear();
						info.uLoadBlotInfo.addAll(uLoadBlot.subList(0, info.nSales));
					}
					else if(Utils.getStringSetting(mContext, Global.PREF_LOT_ASSIGNMENT).equals("SERIAL"))
					{
						new Handler(Looper.getMainLooper()).post(() -> {
							try {
								String strBatchCodes = "";
								for(LoadBlotInfo info1 : info.uLoadBlotInfo)
								{
									if(TextUtils.isEmpty(strBatchCodes))
									{
										strBatchCodes = info1.strLotNumber;
									}
									else
									{
										strBatchCodes += "," + info1.strLotNumber;
									}
								}
								Map<String, Integer> batchCodeList = new HashMap<>();
								for(LoadBlotInfo loadBlotInfo : uLoadBlot)
								{
									batchCodeList.put(loadBlotInfo.strLotNumber, 1);
								}

								BatchCodeInputDialog inputDialog = new BatchCodeInputDialog(mContext, strBatchCodes1 -> {
									for(int i = 0; i < strBatchCodes1.length; i++)
									{
										if(i == info.uLoadBlotInfo.size())
										{
											LoadBlotInfo loadBlotInfo = new LoadBlotInfo();
											loadBlotInfo.strBatchDate = new SimpleDateFormat("yyyyMMdd").format(new Date());
											loadBlotInfo.strLotNumber = strBatchCodes1[i];
											loadBlotInfo.nLotQty = 1;
											info.uLoadBlotInfo.add(loadBlotInfo);
										}
										else
										{
											info.uLoadBlotInfo.get(i).strLotNumber = strBatchCodes1[i];
										}
									}
								}, info.nSales, strBatchCodes, batchCodeList);
								inputDialog.show();
							}catch (Exception e)
							{
								e.printStackTrace();
							}
						});
					}
					else
					{
						info.uLoadBlotInfo.clear();
						info.uLoadBlotInfo.addAll(uLoadBlot.subList(0, info.nSales));
					}

					return true;
				}
			}
		}
		else
		{
			return false;
		}
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

	Map<String, Integer> mPeddleMap = new HashMap<>();
	private int getPeddle(String strItemNo){
		if( mPeddleMap.get(strItemNo) != null )
			return mPeddleMap.get(strItemNo);
		if( mUInvenHinfo != null && mUInvenHinfo.uInvenDList != null )
		{
			int nPeddle = 0;
			for(UInvenDInfo info : mUInvenHinfo.uInvenDList)
			{
				if( info.strItemNo.equals(strItemNo) )
				{
					nPeddle = info.nResPeddle;
					break;
				}
			}
			mPeddleMap.put(strItemNo, nPeddle);
			return nPeddle;
		}
		else
			return 0;
	}

	Map<String, Double> mPeddleMapD = new HashMap<>();
	private double getPeddleD(String strItemNo){
		if( mPeddleMapD.get(strItemNo) != null )
			return mPeddleMapD.get(strItemNo);
		if( mUInvenHinfo != null && mUInvenHinfo.uInvenDList != null )
		{
			double dPeddle = 0;
			for(UInvenDInfo info : mUInvenHinfo.uInvenDList)
			{
				if( info.strItemNo.equals(strItemNo) )
				{
					dPeddle = info.dResPeddle;
					break;
				}
			}
			mPeddleMapD.put(strItemNo, dPeddle);
			return dPeddle;
		}
		else
			return 0;
	}

	Map<String,Integer> mPresoldMap = new HashMap<>();
	private int getPresold(String strItemNo){
		if( mPresoldMap.get(strItemNo) != null )
			return mPresoldMap.get(strItemNo);
		if( mUInvenHinfo != null && mUInvenHinfo.uInvenDList != null )
		{
			int nPresold = 0;
			for(UInvenDInfo info : mUInvenHinfo.uInvenDList)
			{
				if( info.strItemNo.equals(strItemNo) )
				{
					nPresold = info.nResPresold;
					break;
				}
			}
			mPresoldMap.put(strItemNo, nPresold);
			return nPresold;
		}
		else
			return 0;
	}

	Map<String,Double> mPresoldMapD = new HashMap<>();
	private double getPresoldD(String strItemNo){
		if( mPresoldMapD.get(strItemNo) != null )
			return mPresoldMapD.get(strItemNo);
		if( mUInvenHinfo != null && mUInvenHinfo.uInvenDList != null )
		{
			double dPresold = 0;
			for(UInvenDInfo info : mUInvenHinfo.uInvenDList)
			{
				if( info.strItemNo.equals(strItemNo) )
				{
					dPresold = info.dResPresold;
					break;
				}
			}
			mPresoldMapD.put(strItemNo, dPresold);
			return dPresold;
		}
		else
			return 0;
	}

	DescTypeInfo mCurDescTypeinfo = null;
	private void doComplete() {
		if(bReverseMode)
		{
			if(!mIsPickup)
			{
				MakeDeliveryFragment fragment = new MakeDeliveryFragment();
				Bundle bundle = new Bundle();
				bundle.putBoolean(Global.EXTRA_IS_PICKUP, true);
				bundle.putBoolean(Global.EXTRA_IS_REVERSE, true);
				fragment.setArguments(bundle);
				mContext.replaceFragment(fragment);
			}
			else
			{
				String strDisableNegInvAmt = Utils.getStringSetting(mContext, Global.PREF_DISABLE_NEG_INV_AMT);
				float result = DeliverySummaryFragment.getTotalInvoiceValue(mContext);
				if (strDisableNegInvAmt.equals("1") && Global.gCurCustomerInfo.nInvoiceFmt == 1 && result < 0) {
					try {
						final MessageDialog msgDialog = new MessageDialog(mContext);
						msgDialog.setCancelable(false);
						msgDialog.mStrMessage = Str.SalesMustGreaterThanReturns[Global.LANG];
						msgDialog.mStrMiddle = Str.Back[Global.LANG];
						msgDialog.setOnDismissListener(dialog1 -> {
							if (msgDialog.mReturnCode == Global.DLG_RETURN_MIDDLE) {
								m_bCompleted = false;
							}
						});
						msgDialog.show();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				else {
					DeliverySummaryFragment fragment = new DeliverySummaryFragment();
					Bundle bundle = new Bundle();
					bundle.putBoolean(Global.EXTRA_IS_REVERSE, true);
					fragment.setArguments(bundle);
					mContext.replaceFragment(fragment);
				}
			}
			return;
		}

		if(mIsPickup == false && !futureOrder)
        {
			UOrderDInfo selInfo = null;
			double dStock = 0;
			double dQuantity = 0;
			double dPeddle = 0;
			double dPresold = 0;

			int nStock = 0;
			int nQuantity = 0;
			int nPeddle = 0;
			int nPresold = 0;

			for(UOrderDInfo info : mUOrderDList)
			{
				if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
				{
					dStock = getStockValueD(info.strItemNo);
					dQuantity = info.dSales;
					if(dQuantity > dStock && dQuantity != 0)
					{
						if( m_nInventoryValidationType == 1 || m_nInventoryValidationType == 2)
						{
							selInfo = info;
							break;
						}
					}

					dPresold = getPresoldValueD(info.strItemNo);
					double dOrderPresoldValue = info.dPresoldValue;
					dPeddle = dStock - dPresold;
					double dAvailable = 0;
					if(dStock > dOrderPresoldValue) {
						dAvailable = dOrderPresoldValue;
					}
					else {
						dAvailable = dStock;
					}
					if(dPeddle > 0)
					{
						dAvailable += dPeddle;
					}
					if( dQuantity > dAvailable  && dQuantity != 0)
					{
						if( m_nInventoryValidationType == 3 || m_nInventoryValidationType == 4)
						{
							selInfo = info;
							break;
						}
					}
				}
				else
				{
					nStock = getStockValue(info.strItemNo);
					nQuantity = info.nSales;
					if(nQuantity > nStock && nQuantity != 0)
					{
						if( m_nInventoryValidationType == 1 || m_nInventoryValidationType == 2)
						{
							selInfo = info;
							break;
						}
					}

					nPresold = getPresoldValue(info.strItemNo);
					int nOrderPresoldValue = info.nPresoldValue;
					nPeddle = nStock - nPresold;
					int nAvailable = 0;
					if(nStock > nOrderPresoldValue) {
						nAvailable = nOrderPresoldValue;
					}
					else {
						nAvailable = nStock;
					}
					if(nPeddle > 0)
					{
						nAvailable += nPeddle;
					}
					if( nQuantity > nAvailable  && nQuantity != 0)
					{
						if( m_nInventoryValidationType == 3 || m_nInventoryValidationType == 4)
						{
							selInfo = info;
							break;
						}
					}
				}
			}
			if(selInfo != null)
			{
				if(Utils.getProdLoconSCWType(mContext, selInfo.strItemNo) == Global.WEIGHT_ITEM)
				{
					if( m_nInventoryValidationType == 1)
					{
						try {
							final InventoryMessageDialog msgDialog = new InventoryMessageDialog(mContext);
							msgDialog.setCancelable(false);
							String strCaption = "";
							strCaption = String.format(Locale.ENGLISH, "%.2f.", dStock);

							msgDialog.mStrTitle = Str.Warning[Global.LANG];
							msgDialog.mStrCustomerName = selInfo.strItemNo + "\n" + selInfo.strDescription;
							msgDialog.mStrMessage = String.format(Locale.ENGLISH, Str.The_quantity_you_have_entered_exceeds_the_available_stock_of_1[Global.LANG], selInfo.strDescription, strCaption);
							msgDialog.mStrSubMessage = Str.Ignore_the_oversell[Global.LANG];
							msgDialog.mStrLeft = Str.No[Global.LANG];
							msgDialog.mStrRight = Str.Yes[Global.LANG];
							final double finalNQuantity1 = dQuantity;
							msgDialog.setOnDismissListener(new OnDismissListener() {
								@Override
								public void onDismiss(DialogInterface dialog) {
									if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
										showKeyboard((EditText) mRootView.findViewById(R.id.editCase), true);
									} else if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
										doProcessComplete();
									}
								}
							});
							msgDialog.show();
						}catch (Exception e)
						{
							e.printStackTrace();
						}

						m_bCompleted = false;
						return;
					}
					else if( m_nInventoryValidationType == 2 )
					{
						try {
							final InventoryMessageDialog msgDialog = new InventoryMessageDialog(mContext);
							msgDialog.setCancelable(false);
							String strCaption = String.format(Locale.ENGLISH, "%.2f", dStock);

							msgDialog.mStrTitle = Str.Warning[Global.LANG];
							msgDialog.mStrCustomerName = selInfo.strItemNo + "\n" + selInfo.strDescription;
							msgDialog.mStrMessage = String.format(Locale.ENGLISH, Str.The_quantity_you_have_entered_exceeds_the_available_stock_of_1[Global.LANG], selInfo.strDescription, strCaption);
							msgDialog.mStrSubMessage = Str.Reenter_the_quantity_to_continue[Global.LANG];
							msgDialog.mStrMiddle = Str.Return[Global.LANG];
							msgDialog.setOnDismissListener(new OnDismissListener() {
								@Override
								public void onDismiss(DialogInterface dialog) {
									if (msgDialog.mReturnCode == Global.DLG_RETURN_MIDDLE) {
										showKeyboard((EditText) mRootView.findViewById(R.id.editCase), true);
									}
								}
							});
							msgDialog.show();
						}catch (Exception e)
						{
							e.printStackTrace();
						}

						m_bCompleted = false;
						return;
					}
					else if( m_nInventoryValidationType == 3 )
					{
						try {
							final InventoryMessageDialog msgDialog = new InventoryMessageDialog(mContext);
							msgDialog.setCancelable(false);
							String strStockCaption = String.format(Locale.ENGLISH, "%.2f", dStock);
							String strPresoldCaption = String.format(Locale.ENGLISH, "%.2f", dPresold);
							String strAvailable = String.format(Locale.ENGLISH, "%.2f", (dStock - dPresold));

							msgDialog.mStrCustomerName = selInfo.strItemNo + "\n" + selInfo.strDescription;
							msgDialog.mStrTitle = Str.Warning[Global.LANG];
							msgDialog.mStrMessage = String.format(Locale.ENGLISH, Str.The_quantity_entered_exceeds_the_reserved_inventory_levels[Global.LANG], strPresoldCaption, strAvailable, strStockCaption);
							msgDialog.mStrSubMessage = Str.Ignore_the_oversell[Global.LANG];
							msgDialog.mStrLeft = Str.No[Global.LANG];
							msgDialog.mStrRight = Str.Yes[Global.LANG];
							msgDialog.setOnDismissListener(new OnDismissListener() {
								@Override
								public void onDismiss(DialogInterface dialog) {
									if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
										showKeyboard((EditText) mRootView.findViewById(R.id.editCase), true);
									} else if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
										doProcessComplete();
									}
								}
							});
							msgDialog.show();
						}catch (Exception e)
						{
							e.printStackTrace();
						}

						m_bCompleted = false;
						return;
					}
					else if( m_nInventoryValidationType == 4 )
					{
						try {
							final InventoryMessageDialog msgDialog = new InventoryMessageDialog(mContext);
							msgDialog.setCancelable(false);
							String strStockCaption = String.format(Locale.ENGLISH, "%.2f", dStock);
							String strPresoldCaption = String.format(Locale.ENGLISH, "%.2f", dPresold);
							String strAvailable = String.format(Locale.ENGLISH, "%.2f", (dStock - dPresold));

							msgDialog.mStrTitle = Str.Warning[Global.LANG];
							msgDialog.mStrCustomerName = selInfo.strItemNo + "\n" + selInfo.strDescription;
							msgDialog.mStrMessage = String.format(Locale.ENGLISH, Str.The_quantity_entered_exceeds_the_reserved_inventory_levels[Global.LANG], strPresoldCaption, strAvailable, strStockCaption);
							msgDialog.mStrSubMessage = Str.Reenter_the_quantity_to_continue[Global.LANG];
							msgDialog.mStrMiddle = Str.Return[Global.LANG];
							msgDialog.setOnDismissListener(new OnDismissListener() {
								@Override
								public void onDismiss(DialogInterface dialog) {
									if (msgDialog.mReturnCode == Global.DLG_RETURN_MIDDLE) {
										showKeyboard((EditText) mRootView.findViewById(R.id.editCase), true);
									}
								}
							});
							msgDialog.show();
						}catch (Exception e)
						{
							e.printStackTrace();
						}

						m_bCompleted = false;
						return;
					}
				}
				else
				{
					if( m_nInventoryValidationType == 1)
					{
						try {
							final InventoryMessageDialog msgDialog = new InventoryMessageDialog(mContext);
							msgDialog.setCancelable(false);
							String strCaption = "";
							int nCase = getCaseValue(selInfo.strItemNo);
							if(bShowCase)
							{
								if( Global.gCurCustomerInfo.strSalEntryMode.equals("B") || Global.gCurCustomerInfo.strSalEntryMode.equals("C"))
								{
									if (Utils.isContainerItem(mContext, selInfo.strItemNo)) {
										strCaption = String.format(Locale.ENGLISH, "%d.", nStock);
									}
									else {
										strCaption = String.format(Locale.ENGLISH, "%d / %d.", nStock / nCase, nStock % nCase);
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
							msgDialog.mStrCustomerName = selInfo.strItemNo + "\n" + selInfo.strDescription;
							msgDialog.mStrMessage = String.format(Locale.ENGLISH, Str.The_quantity_you_have_entered_exceeds_the_available_stock_of_1[Global.LANG], selInfo.strDescription, strCaption);
							msgDialog.mStrSubMessage = Str.Ignore_the_oversell[Global.LANG];
							msgDialog.mStrLeft = Str.No[Global.LANG];
							msgDialog.mStrRight = Str.Yes[Global.LANG];
							msgDialog.setOnDismissListener(new OnDismissListener() {
								@Override
								public void onDismiss(DialogInterface dialog) {
									if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
										showKeyboard((EditText) mRootView.findViewById(R.id.editCase), true);
									} else if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
										doProcessComplete();
									}
								}
							});
							msgDialog.show();
						}catch (Exception e)
						{
							e.printStackTrace();
						}

						m_bCompleted = false;
						return;
					}
					else if( m_nInventoryValidationType == 2 )
					{
						try {
							final InventoryMessageDialog msgDialog = new InventoryMessageDialog(mContext);
							msgDialog.setCancelable(false);
							String strCaption = "";
							int nCase = getCaseValue(selInfo.strItemNo);
							if(bShowCase)
							{
								if( Global.gCurCustomerInfo.strSalEntryMode.equals("B") || Global.gCurCustomerInfo.strSalEntryMode.equals("C"))
								{
									if (Utils.isContainerItem(mContext, selInfo.strItemNo)) {
										strCaption = String.format(Locale.ENGLISH, "%d.", nStock);
									}
									else {
										strCaption = String.format(Locale.ENGLISH, "%d / %d.", nStock / nCase, nStock % nCase);
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
							msgDialog.mStrCustomerName = selInfo.strItemNo + "\n" + selInfo.strDescription;
							msgDialog.mStrMessage = String.format(Locale.ENGLISH, Str.The_quantity_you_have_entered_exceeds_the_available_stock_of_1[Global.LANG], selInfo.strDescription, strCaption);
							msgDialog.mStrSubMessage = Str.Reenter_the_quantity_to_continue[Global.LANG];
							msgDialog.mStrMiddle = Str.Return[Global.LANG];
							msgDialog.setOnDismissListener(new OnDismissListener() {
								@Override
								public void onDismiss(DialogInterface dialog) {
									if (msgDialog.mReturnCode == Global.DLG_RETURN_MIDDLE) {
										showKeyboard((EditText) mRootView.findViewById(R.id.editCase), true);
									}
								}
							});
							msgDialog.show();
						}catch (Exception e)
						{
							e.printStackTrace();
						}

						m_bCompleted = false;
						return;
					}
					else if( m_nInventoryValidationType == 3 )
					{
						try {
							final InventoryMessageDialog msgDialog = new InventoryMessageDialog(mContext);
							msgDialog.setCancelable(false);
							String strStockCaption = "";
							String strPresoldCaption = "";
							String strAvailable = "";
							int nCase = getCaseValue(selInfo.strItemNo);
							if(bShowCase)
							{
								if( Global.gCurCustomerInfo.strSalEntryMode.equals("B") || Global.gCurCustomerInfo.strSalEntryMode.equals("C"))
								{
									if (Utils.isContainerItem(mContext, selInfo.strItemNo)) {
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
							msgDialog.mStrCustomerName = selInfo.strItemNo + "\n" + selInfo.strDescription;
							msgDialog.mStrMessage = String.format(Locale.ENGLISH, Str.The_quantity_entered_exceeds_the_reserved_inventory_levels[Global.LANG], strPresoldCaption, strAvailable, strStockCaption);
							msgDialog.mStrSubMessage = Str.Ignore_the_oversell[Global.LANG];
							msgDialog.mStrLeft = Str.No[Global.LANG];
							msgDialog.mStrRight = Str.Yes[Global.LANG];
							msgDialog.setOnDismissListener(new OnDismissListener() {
								@Override
								public void onDismiss(DialogInterface dialog) {
									if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
										showKeyboard((EditText) mRootView.findViewById(R.id.editCase), true);
									} else if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
										doProcessComplete();
									}
								}
							});
							msgDialog.show();
						}catch (Exception e)
						{
							e.printStackTrace();
						}

						m_bCompleted = false;
						return;
					}
					else if( m_nInventoryValidationType == 4 )
					{
						try {
							final InventoryMessageDialog msgDialog = new InventoryMessageDialog(mContext);
							msgDialog.setCancelable(false);
							String strStockCaption = "";
							String strPresoldCaption = "";
							String strAvailable = "";
							int nCase = getCaseValue(selInfo.strItemNo);
							if(bShowCase)
							{
								if( Global.gCurCustomerInfo.strSalEntryMode.equals("B") || Global.gCurCustomerInfo.strSalEntryMode.equals("C"))
								{
									if (Utils.isContainerItem(mContext, selInfo.strItemNo)) {
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
							msgDialog.mStrCustomerName = selInfo.strItemNo + "\n" + selInfo.strDescription;
							msgDialog.mStrMessage = String.format(Locale.ENGLISH, Str.The_quantity_entered_exceeds_the_reserved_inventory_levels[Global.LANG], strPresoldCaption, strAvailable, strStockCaption);
							msgDialog.mStrSubMessage = Str.Reenter_the_quantity_to_continue[Global.LANG];
							msgDialog.mStrMiddle = Str.Return[Global.LANG];
							msgDialog.setOnDismissListener(new OnDismissListener() {
								@Override
								public void onDismiss(DialogInterface dialog) {
									if (msgDialog.mReturnCode == Global.DLG_RETURN_MIDDLE) {
										showKeyboard((EditText) mRootView.findViewById(R.id.editCase), true);
									}
								}
							});
							msgDialog.show();
						}catch (Exception e)
						{
							e.printStackTrace();
						}

						m_bCompleted = false;
						return;
					}
				}

			}
        }
        doProcessComplete();
	}

	private void doProcessComplete()
    {
        if (mStrPhotoPath.length() > 0) {
            UFileTransactionInfo uFileTransactionInfo = new UFileTransactionInfo();
            setUCameraData("CAM", mStrPhotoPath, "", uFileTransactionInfo, 1, true);
        }

        if(!mIsPickup)
        {
            //Check Free Items
            DbAdapter dbAdapter = new DbAdapter(mContext);
            dbAdapter.open();
            List<DescTypeInfo> descTypeInfoList = dbAdapter.getDescTypeList("FREERSN");

            List<DescTypeInfo> delDescTypeInfoList = new ArrayList<>();
            for(DescTypeInfo info : descTypeInfoList)
            {
                if(TextUtils.isEmpty(info.strValue2))
                {
                    info.strValue2 = "Y";
                }
                if(!info.strValue2.equals("Y"))
                {
                    delDescTypeInfoList.add(info);
                }
            }

            for(DescTypeInfo info : delDescTypeInfoList){
                descTypeInfoList.remove(info);
            }

            int nCnt = 0;
            for (UOrderDInfo info : mUOrderDList) {
                if(info.nTrxnType == Global.TRXN_SAMPLE && info.nSales > 0 && info.nReasonCode == 0 /*&& info.nPlanQty - info.nSales != 0*/)
                {
                    String strReasonType = "Y";
                    if(info.nPlanQty != 0)
                    {
                        List<DescTypeInfo> descTypeInfos = dbAdapter.getDescTypeListWithNumericKey("FREERSN", info.nReasonCode);

                        if(descTypeInfos != null && descTypeInfos.size() > 0)
                        {
                            strReasonType = descTypeInfos.get(0).strValue1;
                        }
                    }

                    if(!strReasonType.equals("N"))
                    {
                        if(descTypeInfoList != null && descTypeInfoList.size() == 1)
                        {
                            info.nReasonCode = descTypeInfoList.get(0).nNumericKey;

                            if(Utils.getStringSetting(mContext, Global.PREF_FRGOODPRM).equals("1") && descTypeInfoList.get(0).strValue3.equals("Y"))
							{
								//Apply the FREE GOOD PLAN & ASS
								info.uPromotionInfoList.clear();
								List<ProdDetlInfo> prodInfoList = dbAdapter.getProductListWithFreeGoods(info.strItemNo);
								if(prodInfoList != null && prodInfoList.size() > 0)
								{
									info.nPrice = prodInfoList.get(0).nPrice;
									info.nGrossPrice = prodInfoList.get(0).nGrossPrice;
									info.nOrgPrice = prodInfoList.get(0).nOrgPrice;
									info.nOrgGrossPrice = prodInfoList.get(0).nOrgGrossPrice;
									info.nDiscountVal = prodInfoList.get(0).nDiscountVal;
									for(UPromotionInfo uPromotionInfo : prodInfoList.get(0).uPromotionInfoList)
									{
										UPromotionInfo uItem = uPromotionInfo.duplicate();
										uItem.nTrxnType = (mIsPickup) ? Global.TRXN_PICKUP : Global.TRXN_DELIVER;
										uItem.nCustNo = Global.gCurCustomerInfo.nCustNo;
										uItem.nChainNo = Global.gCurCustomerInfo.nChainNo;
										uItem.strItemNo = info.strItemNo;
										info.uPromotionInfoList.add(uItem);
									}
								}
							}
                        }
                        else
						{
							nCnt++;
						}
                    }
                }
            }

            dbAdapter.close();

            if(descTypeInfoList.size() > 1 && nCnt > 0)
            {
				String strDisableNegInvAmt = Utils.getStringSetting(mContext, Global.PREF_DISABLE_NEG_INV_AMT);
				float result = 0;
				if (Global.gDeliveryPickupCnt >= 1) {
					result = DeliverySummaryFragment.getTotalInvoiceValue(mContext, mUOrderDList, Global.gUOrderHInfo.pickupList);
				}
				if (Global.gDeliveryPickupCnt >= 1 && strDisableNegInvAmt.equals("1") && Global.gCurCustomerInfo.nInvoiceFmt == 1 && result<0) {
					try {
						final MessageDialog msgDialog = new MessageDialog(mContext);
						msgDialog.setCancelable(false);
						msgDialog.mStrMessage = Str.SalesMustGreaterThanReturns[Global.LANG];
						msgDialog.mStrMiddle = Str.Back[Global.LANG];
						msgDialog.setOnDismissListener(dialog1 -> {
							if (msgDialog.mReturnCode == Global.DLG_RETURN_MIDDLE) {
								m_bCompleted = false;
							}
						});
						msgDialog.show();
						return;
					} catch (Exception e) {
						e.printStackTrace();
					}
					return;
				}
				else {
					FreeReasonsFragment fragment = new FreeReasonsFragment();
					fragment.setOrderList(mUOrderDList);
					mContext.replaceFragment(fragment);
					return;
				}
            }
        }

        int nCnt = 0;
        for (UOrderDInfo info : mUOrderDList) {
            if( info.nTrxnType == Global.TRXN_SAMPLE || info.nTrxnType == Global.TRXN_FREE )
            {
                nCnt++;
                continue;
            }
            if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
            {
                double dQuantity = mIsPickup == false ? info.dSales : info.dDumps;
                double nprs = info.dSales - info.dPlanQty;
                if( mIsPickup && dQuantity == 0)
                {
                    nCnt++;
                }
                else if( !mIsPickup && nprs == 0)
                {
                    nCnt++;
                }
            }
            else
            {
                int nQuantity = mIsPickup == false ? info.nSales : info.nDumps;
                int nprs = info.nSales - info.nPlanQty;
                if( mIsPickup && nQuantity == 0)
                {
                    nCnt++;
                }
                else if( !mIsPickup && nprs == 0)
                {
                    nCnt++;
                }
            }
        }
        List<DescTypeInfo> descTypeInfoList = null;
        if (nCnt < mUOrderDList.size() && (mIsPickup || (!mIsPickup && Global.gCurCustomerInfo.presoldOrHInfo != null && Global.gCurCustomerInfo.presoldOrHInfo.size() > 0))) {
            DbAdapter dbAdapter = new DbAdapter(mContext);
            dbAdapter.open();
            if( mIsPickup )
            {
                descTypeInfoList = new ArrayList<>();
                List<DescTypeInfo> returnTypeList = dbAdapter.getDescTypeList("RETURNRSN");
                for(DescTypeInfo info : returnTypeList)
                {
                    descTypeInfoList.add(info);
                }
                List<DescTypeInfo> buyBackTypeList = dbAdapter.getDescTypeList("BUYBACKRSN");
                for(DescTypeInfo info : buyBackTypeList)
                {
                    descTypeInfoList.add(info);
                }
            }
            else
            {
                descTypeInfoList = dbAdapter.getDescTypeList("NODLVRYRSN");
            }
            dbAdapter.close();
            if( descTypeInfoList != null && descTypeInfoList.size() > 1 )
            {
				String strDisableNegInvAmt = Utils.getStringSetting(mContext, Global.PREF_DISABLE_NEG_INV_AMT);
				float result = 0;
				if (Global.gDeliveryPickupCnt >= 1) {
					if (mIsPickup) {
						result = DeliverySummaryFragment.getTotalInvoiceValue(mContext, Global.gUOrderHInfo.deliveryList, mUOrderDList);
					}
					else {
						result = DeliverySummaryFragment.getTotalInvoiceValue(mContext, mUOrderDList, Global.gUOrderHInfo.pickupList);
					}
				}

				if (Global.gDeliveryPickupCnt >= 1 && strDisableNegInvAmt.equals("1") && Global.gCurCustomerInfo.nInvoiceFmt == 1 && result < 0) {
					try {
						final MessageDialog msgDialog = new MessageDialog(mContext);
						msgDialog.setCancelable(false);
						msgDialog.mStrMessage = Str.SalesMustGreaterThanReturns[Global.LANG];
						msgDialog.mStrMiddle = Str.Back[Global.LANG];
						msgDialog.setOnDismissListener(dialog1 -> {
							if (msgDialog.mReturnCode == Global.DLG_RETURN_MIDDLE) {
								m_bCompleted = false;
							}
						});
						msgDialog.show();
						return;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				else {
					OrderAdjustmentsFragment fragment = new OrderAdjustmentsFragment();
					fragment.setOrderList(mUOrderDList, mIsPickup);
					mContext.replaceFragment(fragment);
					return;
				}
            }
        }

        if (descTypeInfoList != null && descTypeInfoList.size() == 1)
        {
            mCurDescTypeinfo = descTypeInfoList.get(0);
        }
        if (Global.gCurCustomerInfo.nInvoiceFmt == 1 && Global.gDeliveryPickupCnt == 0 && !mIsPickup) {
            String strMessage = Str.ThisIsNettInvoiceCustomer[Global.LANG] + "\n";
            if (mIsPickup == false) {
                strMessage += Str.DoYouHaveReturns[Global.LANG];
            } else {
                strMessage += Str.DoYouHaveDeliveries[Global.LANG];
            }
            try {
				final MessageDialog msgDialog = new MessageDialog(mContext);
				msgDialog.setCancelable(false);
				msgDialog.mStrMessage = strMessage;
				msgDialog.mStrLeft = Str.No[Global.LANG];
				msgDialog.mStrRight = Str.Yes[Global.LANG];
				msgDialog.setOnDismissListener(new OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
							String strDisableNegInvAmt = Utils.getStringSetting(mContext, Global.PREF_DISABLE_NEG_INV_AMT);
							float result = DeliverySummaryFragment.getTotalInvoiceValue(mContext);
							if (strDisableNegInvAmt.equals("1") && Global.gCurCustomerInfo.nInvoiceFmt == 1 && result < 0) {
								try {
									final MessageDialog msgDialog = new MessageDialog(mContext);
									msgDialog.setCancelable(false);
									msgDialog.mStrMessage = Str.SalesMustGreaterThanReturns[Global.LANG];
									msgDialog.mStrMiddle = Str.Back[Global.LANG];
									msgDialog.setOnDismissListener(dialog1 -> {
										if (msgDialog.mReturnCode == Global.DLG_RETURN_MIDDLE) {
											m_bCompleted = false;
										}
									});
									msgDialog.show();
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
							else {
								setUOrderDList(true);
								Global.gCurHeaderInfo = null;
								if(Utils.getStringSetting(mContext, Global.PREF_PRINT_PICK_LIST).equals("2"))
								{
									Fragment fragment = new PickingFragment();
									mContext.replaceFragment(fragment);
								}
								else {
									mContext.replaceFragment(new DeliverySummaryFragment());
								}
							}
						} else if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
							setUOrderDList(true);
							Global.gDeliveryPickupCnt++;
							BaseFragment fragment = new MakeDeliveryFragment();
							Bundle bundle = new Bundle();
							bundle.putBoolean(Global.EXTRA_IS_PICKUP, true);
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

        }else if (Global.gCurCustomerInfo.nInvoiceFmt == 1 && Global.gDeliveryPickupCnt == 0 && mIsPickup) {
            String strMessage = Str.ThisIsNettInvoiceCustomer[Global.LANG] + "\n";
            if (mIsPickup == false) {
                strMessage += Str.DoYouHaveReturns[Global.LANG];
            } else {
                strMessage += Str.DoYouHaveDeliveries[Global.LANG];
            }
            try {
				final MessageDialog msgDialog = new MessageDialog(mContext);
				msgDialog.setCancelable(false);
				msgDialog.mStrMessage = strMessage;
				msgDialog.mStrLeft = Str.No[Global.LANG];
				msgDialog.mStrRight = Str.Yes[Global.LANG];
				msgDialog.setOnDismissListener(new OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
							String strDisableNegInvAmt = Utils.getStringSetting(mContext, Global.PREF_DISABLE_NEG_INV_AMT);
							float result = DeliverySummaryFragment.getTotalInvoiceValue(mContext);
							if (strDisableNegInvAmt.equals("1") && Global.gCurCustomerInfo.nInvoiceFmt == 1 && result < 0) {
								try {
									final MessageDialog msgDialog = new MessageDialog(mContext);
									msgDialog.setCancelable(false);
									msgDialog.mStrMessage = Str.SalesMustGreaterThanReturns[Global.LANG];
									msgDialog.mStrMiddle = Str.Back[Global.LANG];
									msgDialog.setOnDismissListener(dialog1 -> {
										if (msgDialog.mReturnCode == Global.DLG_RETURN_MIDDLE) {
											m_bCompleted = false;
										}
									});
									msgDialog.show();
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
							else {
								setUOrderDList(true);
								Global.gCurHeaderInfo = null;
								if(Utils.getStringSetting(mContext, Global.PREF_PRINT_PICK_LIST).equals("2"))
								{
									Fragment fragment = new PickingFragment();
									mContext.replaceFragment(fragment);
								}
								else
								{
									mContext.replaceFragment(new DeliverySummaryFragment());
								}
							}
						} else if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
							setUOrderDList(true);
							Global.gDeliveryPickupCnt++;
							if((Global.gCurCustomerInfo.presoldOrHInfo != null && Global.gCurCustomerInfo.presoldOrHInfo.size() > 0 && (
									Global.gCurCustomerInfo.presoldOrHInfo.get(0).strType.equals("B") ||
											Global.gCurCustomerInfo.presoldOrHInfo.get(0).strType.equals("C"))
							))
							{
								BaseFragment fragment = new OnHandCountFragment();
								Bundle bundle = new Bundle();
								bundle.putBoolean(Global.EXTRA_IS_PICKUP, false);
								fragment.setArguments(bundle);
								mContext.replaceFragment(fragment);
							}
							else
							{
								BaseFragment fragment = new MakeDeliveryFragment();
								Bundle bundle = new Bundle();
								bundle.putBoolean(Global.EXTRA_IS_PICKUP, false);
								fragment.setArguments(bundle);
								mContext.replaceFragment(fragment);
							}

						}
					}
				});
				msgDialog.show();
			}catch (Exception e)
			{
				e.printStackTrace();
			}

        }
        else {
            setUOrderDList(true);
			Global.gCurHeaderInfo = null;
			if(Utils.getStringSetting(mContext, Global.PREF_PRINT_PICK_LIST).equals("2"))
			{
				Fragment fragment = new PickingFragment();
				mContext.replaceFragment(fragment);
			}
			else {
				String strDisableNegInvAmt = Utils.getStringSetting(mContext, Global.PREF_DISABLE_NEG_INV_AMT);
				float result = DeliverySummaryFragment.getTotalInvoiceValue(mContext);
				if (strDisableNegInvAmt.equals("1") && Global.gCurCustomerInfo.nInvoiceFmt == 1 && result < 0) {
					try {
						final MessageDialog msgDialog = new MessageDialog(mContext);
						msgDialog.setCancelable(false);
						msgDialog.mStrMessage = Str.SalesMustGreaterThanReturns[Global.LANG];
						msgDialog.mStrMiddle = Str.Back[Global.LANG];
						msgDialog.setOnDismissListener(dialog1 -> {
							if (msgDialog.mReturnCode == Global.DLG_RETURN_MIDDLE) {
								m_bCompleted = false;
							}
						});
						msgDialog.show();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				else {
					Bundle bundle = new Bundle();
					bundle.putBoolean(Global.EXTRA_IS_PICKUP, mIsPickup);
					Fragment fragment = new DeliverySummaryFragment();
					fragment.setArguments(bundle);
					mContext.replaceFragment(fragment);
				}
			}

        }
    }

	private void showInvenDetail(UOrderDInfo info, int position) {

		if(!bFreeItem && (info.nTrxnType == Global.TRXN_FREE || info.nTrxnType == Global.TRXN_SAMPLE))
		{
			if (Utils.getStringSetting(mContext, Global.PREF_DISABLE_FREE).equals("1")) {
				return;
			}

			bFreeItem = !bFreeItem;
			if(!mIsPickup)
			{
				if(bFreeItem)
				{
					mRootView.findViewById(R.id.btnAdd).setBackground(mContext.getResources().getDrawable(R.drawable.btn_orange_large));
					((TextView)mRootView.findViewById(R.id.btnAdd)).setText(Str.Free[Global.LANG]);
				}
				else
				{
					mRootView.findViewById(R.id.btnAdd).setBackground(mContext.getResources().getDrawable(R.drawable.btn_orange_large));
					((TextView)mRootView.findViewById(R.id.btnAdd)).setText(Str.Sell[Global.LANG]);
				}
			}
			else
			{
				mRootView.findViewById(R.id.btnAdd).setBackground(mContext.getResources().getDrawable(R.drawable.btn_orange_large));
				((TextView)mRootView.findViewById(R.id.btnAdd)).setText(Str.Return[Global.LANG]);
			}

			((EditText) mRootView.findViewById(R.id.editItemNo)).setText("");
			((EditText) mRootView.findViewById(R.id.editCase)).setText("");
			((EditText) mRootView.findViewById(R.id.editQty)).setText("");
			((TextView)mRootView.findViewById(R.id.txtPriceInv)).setText("");
			showKeyboard((EditText) mRootView.findViewById(R.id.editItemNo), true);
			refreshPage();

			mSelIdx = -1;
			showReasonCode();
			showPriceInven();
			showInvenDetail(info, position);
			return;
		}
		((EditText) mRootView.findViewById(R.id.editItemNo)).setText(Utils.getDisplayItemCode(mContext, info.strItemNo));
		//((EditText) mRootView.findViewById(R.id.editItemNo)).setText(info.strItemNo);
		if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
		{
			mRootView.findViewById(R.id.editCase).setVisibility(View.GONE);
			mRootView.findViewById(R.id.lblSep).setVisibility(View.GONE);
			EditText editQty = (EditText) mRootView.findViewById(R.id.editQty);
			editQty.setTag("From App");
			if(mEntryInputMethod.equals("A"))
			{
				editQty.setText("0.00");
			}
			else
			{
				editQty.setText(String.format(Locale.ENGLISH, "%.2f", mIsPickup ? info.dDumps : info.dSales));
			}
			editQty.setTag(null);
			editQty.requestFocus();
			editQty.setSelection(editQty.length(), editQty.length());
		}
		else
		{
			if(bShowCase)
			{
				if( Global.gCurCustomerInfo.strSalEntryMode.equals("B") || Global.gCurCustomerInfo.strSalEntryMode.equals("C"))
				{
					int nCase = getCaseValue(info.strItemNo);
					int nQty =  mIsPickup == false ? info.nSales : info.nDumps;
					int nCaseValue = nQty / nCase;
					int nQtyValue = nQty % nCase;
					mRootView.findViewById(R.id.editCase).setVisibility(View.VISIBLE);
					mRootView.findViewById(R.id.lblSep).setVisibility(View.VISIBLE);
					EditText editQty = (EditText) mRootView.findViewById(R.id.editQty);
					EditText editCase = (EditText) mRootView.findViewById(R.id.editCase);
					if(nQty != 0)
					{
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
					else
					{
						editQty.setText("");
						editCase.setText("");
						editCase.requestFocus();
						editCase.setSelection(0, 0);
					}

				}
				else if(Global.gCurCustomerInfo.strSalEntryMode.equals("") || Global.gCurCustomerInfo.strSalEntryMode.equals("U"))
				{
					EditText editQty = (EditText) mRootView.findViewById(R.id.editQty);
					String strQty = String.valueOf(mIsPickup == false ? info.nSales : info.nDumps);
					if(!strQty.equals("0"))
					{
						if(mEntryInputMethod.equals("A"))
						{
							editQty.setText(strQty);
						}
						else
						{
							editQty.setText("");
						}
						editQty.requestFocus();
						editQty.setSelection(editQty.length());
					}
					else
					{
						editQty.setText("");
						editQty.requestFocus();
						editQty.setSelection(0);
					}

				}
			}
			else
			{
				EditText editQty = (EditText) mRootView.findViewById(R.id.editQty);
				String strQty = String.valueOf(mIsPickup == false ? info.nSales : info.nDumps);
				if(!strQty.equals("0"))
				{
					if(mEntryInputMethod.equals("A"))
					{
						editQty.setText("");
					}
					else
					{
						editQty.setText(strQty);
						editQty.setSelection(editQty.length());
					}
					editQty.requestFocus();

				}
				else
				{
					editQty.setText("");
					editQty.requestFocus();
					editQty.setSelection(0);
				}
			}
		}

		/*if( info.nTrxnType == Global.TRXN_FREE || info.nTrxnType == Global.TRXN_SAMPLE)
		{
			mSelIdx = position;
		}
		else
		{
			mSelIdx = position;
		}*/

        mSelIdx = -1;
        if(bFreeItem)
        {
            for (int i = 0; i < mUOrderDList.size(); i++) {
                UOrderDInfo uOrderDInfo = mUOrderDList.get(i);
                if (info.strItemNo.equals(info.strItemNo) == true && ( uOrderDInfo.nTrxnType == Global.TRXN_FREE || uOrderDInfo.nTrxnType == Global.TRXN_SAMPLE)) {
                    mSelIdx = i;
                    break;
                }
            }
        }
        else
        {
            for (int i = 0; i < mUOrderDList.size(); i++) {
                UOrderDInfo uOrderDInfo = mUOrderDList.get(i);
                if (info.strItemNo.equals(info.strItemNo) == true && uOrderDInfo.nTrxnType == Global.TRXN_DELIVER) {
                    mSelIdx = i;
                    break;
                }
            }
        }

		showReasonCode();
		showPriceInven();
	}

	public void showPriceInven()
	{
		/*UOrderDInfo info = mListAdapter.getItem(mSelIdx);
		String strShow = "";
		if(info != null)
		{
			if(Global.gCurCustomerInfo.nShowPrice == 1)
			{
				strShow += String.format(Locale.ENGLISH, "$%.2f", Utils.getRoundedValue(info.nPrice));
			}
			if(!mIsPickup)
			{
				if(Utils.getIntSetting(mContext, Global.PREF_SHOW_INV) == 1)
				{
					if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
					{
						double dStock = getStockValueD(info.strItemNo);
						double dPresoldStock = getPresoldValueD(info.strItemNo);
						dStock -= dPresoldStock;
						if(!TextUtils.isEmpty(strShow))
						{
							strShow += "\n" + "On Truck " + String.format(Locale.ENGLISH, "%.2f", dStock);
						}
						else
						{
							strShow += "On Truck " + String.format(Locale.ENGLISH, "%.2f", dStock);
						}

					}
					else
					{
						int nStock = getStockValue(info.strItemNo);
						int nPresoldStock = getPresoldValue(info.strItemNo);
						nStock -= nPresoldStock;
						int nCase = getCaseValue(info.strItemNo);
						if(Global.gCurCustomerInfo.strSalEntryMode.equals("B"))
						{
							if(!TextUtils.isEmpty(strShow))
							{
								strShow += "\n" + "On Truck " + String.format(Locale.ENGLISH, "%d / %d", nStock / nCase, nStock % nCase);
							}
							else
							{
								strShow += "On Truck " + String.format(Locale.ENGLISH, "%d / %d", nStock / nCase, nStock % nCase);
							}
						}
						else
						{
							if(!TextUtils.isEmpty(strShow))
							{
								strShow += "\n" + "On Truck " + String.format(Locale.ENGLISH, "%d", nStock);
							}
							else
							{
								strShow += "On Truck " + String.format(Locale.ENGLISH, "%d", nStock);
							}
						}

					}
				}
			}
		}

		((TextView)mRootView.findViewById(R.id.txtPriceInv)).setText(strShow);*/
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
					.setPositiveButton(Str.Ok[Global.LANG], (dialog, nSelected) -> refreshPage())
					.setNegativeButton(Str.Cancel[Global.LANG], (dialog, nSelected) -> setSortFlag(Global.sortArray[nFinalOrigIdx]))
					.create()
					.show();
		}catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public boolean m_bClicked = false;
	public boolean m_bCompleted = false;
	@Override
	public void onClick(View v) {
		if( m_bClicked ) return;
		m_bClicked = true;
		switch (v.getId()) {
			case R.id.btnBack:
				Global.gDeliveryPickupCnt--;
				if( Global.gDeliveryPickupCnt < 0 )
				{
					Global.gDeliveryPickupCnt = 0;
				}
				mContext.onBackPressed();
				break;
			case R.id.btnPhoto:
				selectPhoto();
				break;
			case R.id.btnSort:
				doSort();
				break;
			case R.id.btnOrderHistory:
				if(!bReverseMode)
				{
					/*if(mStrOrderNo.equals("0") || mStrOrderNo.equals(""))
					{
						OrderHistoryFragment fragment = new OrderHistoryFragment();
						mContext.replaceFragment(fragment);
					}
					else
					{
						final MessageDialog msgDialog = new MessageDialog(mContext);
						msgDialog.setCancelable(false);
						msgDialog.mStrMessage = Str.You_can_not_use_history[Global.LANG];
						msgDialog.mStrMiddle = Str.Ok[Global.LANG];
						msgDialog.show();
					}*/
					OrderHistoryFragment fragment = new OrderHistoryFragment();
					Bundle params = new Bundle();
					params.putString("OrderNo", mStrOrderNo);
					fragment.setArguments(params);
					mContext.replaceFragment(fragment);
				}
				break;
			case R.id.btnAdd:
				if(!bReverseMode)
				{
					doAdd();
				}
				break;
			case R.id.btnProductTreeList:
				if(!bReverseMode)
				{
					DbAdapter dbAdapter = new DbAdapter(mContext);
					dbAdapter.open();
					List<ProdStruInfo> prodStruList = dbAdapter.getProdStruList();
					dbAdapter.close();
					if( prodStruList != null && prodStruList.size() > 0 )
					{
						NewProductTreelistFragment fragment = new NewProductTreelistFragment();
						fragment.setFrom(this);
						fragment.setEnableFilterAuthorizedItem(true);
						mContext.replaceFragment(fragment);
					}
				}
				break;
			case R.id.btnBarcodeScanner:
				if(!bReverseMode)
				{
					Intent intent = new Intent(mContext, BarcodeScannerActivity.class);
					intent.putExtra(Global.EXTRA_BARCODESCAN_FROM, getClass().getSimpleName());
					startActivity(intent);
					mContext.overridePendingTransition(R.anim.start1, R.anim.start2);
				}

				break;
			case R.id.btnProductSearch:
				{
					if(!bReverseMode)
					{
						SearchProductFragment fragment = new SearchProductFragment();
						fragment.setEnableFilterAuthorizedItem(true);
						boolean bFilter = false;
						if(Global.gUOrderHInfo != null && Global.gUOrderHInfo.futureOrderMethod == 0)
						{
							bFilter = Utils.getIntSetting(mContext, Global.PREF_INVENCKECK) != 0;
						}
						else
						{
							bFilter = false;
						}
						Bundle args = new Bundle();
						args.putBoolean(Global.EXTRA_FILTER, bFilter);
						fragment.setArguments(args);

						mContext.replaceFragment(fragment);
					}
				}
				break;
			case R.id.btnCompleted:
				if( m_bCompleted ) return;
				m_bCompleted = true;
				doComplete();
				break;
			case R.id.btnFilter:
				doFilter();
				break;
		}
		m_bClicked = false;
	}

	private void doFilter()
	{
		filter = !filter;
		mSelIdx = -1;
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

	public void ProcessExit() {
		String strMessage = Str.Are_you_sure_wish_to_exit_make_delivery[Global.LANG] + "\n";
		try {
			final MessageDialog msgDialog = new MessageDialog(mContext);
			msgDialog.setCancelable(false);
			if( mIsPickup )
				msgDialog.mStrTitle = Str.exit_return[Global.LANG];
			else
				msgDialog.mStrTitle = Str.exit_sale[Global.LANG];
			msgDialog.mStrMessage = strMessage;
			msgDialog.mStrLeft = Str.No[Global.LANG];
			msgDialog.mStrRight = Str.Yes[Global.LANG];
			msgDialog.setOnDismissListener(new OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
					if( msgDialog.mReturnCode == Global.DLG_RETURN_LEFT )
					{

					}
					else if( msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT )
					{
						if(mIsPickup)
						{
							Global.gUOrderHInfo.pickupList.clear();
						}
						else
						{
							Global.gUOrderHInfo.deliveryList.clear();
						}
						m_bModified = false;
						mContext.onBackPressed();
					}
				}
			});
			msgDialog.show();
		}catch (Exception e)
		{
			e.printStackTrace();
		}


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
			public TextView txtItemNo, txtShortDesc, txtPickLocationCode, txtLoaded, txtStock, txtFreeItem, txtAvailable, txtPrice;
		}

		public List<UOrderDInfo> getListData()
		{
			return list;
		}

		public int getCount() {
			return list.size();
		}

		public void setListData(List<UOrderDInfo> list) {
			this.list.clear();
			this.list.addAll(list);
			/*Collections.sort(this.list, new Comparator<UOrderDInfo>() {
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
			});*/
		}

		public UOrderDInfo getItem(int position) {
			if(position < 0) return null;
			if(position >= list.size()) return null;
			return list.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(final int position, View convertView, ViewGroup parent) {
			View view = convertView;
			final ViewHolder holder;
			final UOrderDInfo info = getItem(position);

			if(Utils.getIntSetting(mContext, Global.PREF_DETAIL_ENTRY) == 2) {
				if (convertView == null) {
					view = mContext.getLayoutInflater().inflate(R.layout.row_make_delivery_detail_entry_2, parent, false);
					holder = new ViewHolder();
					holder.txtPrice = (TextView) view.findViewById(R.id.txtPrice);
					holder.txtShortDesc = (TextView) view.findViewById(R.id.txtShortDesc);
					holder.txtLoaded = (TextView) view.findViewById(R.id.txtLoaded);
					holder.txtStock = (TextView) view.findViewById(R.id.txtStock);
					view.setTag(holder);
				} else {
					holder = (ViewHolder) view.getTag();
				}

				String strCurrencySymbol = Utils.getStringSetting(mContext, Global.PREF_CURRENCY_SYMBOL);
				if( strCurrencySymbol == null || strCurrencySymbol.length() == 0 )
				{
					strCurrencySymbol = "$";
				}
				if( Global.gCurCustomerInfo.nShowPrice == 1 && (info.nTrxnType != Global.TRXN_SAMPLE && info.nTrxnType != Global.TRXN_FREE))
				{
					holder.txtPrice.setVisibility(View.VISIBLE);
					holder.txtPrice.setText(String.format(Locale.ENGLISH, "%s%.2f", strCurrencySymbol, info.nPrice));
				}
				else
				{
					holder.txtPrice.setVisibility(View.INVISIBLE);
				}
				holder.txtShortDesc.setText(info.strDescription);
				holder.txtLoaded.setText(String.valueOf(info.nPlanQty));
				holder.txtStock.setText(String.valueOf(mIsPickup == false ? info.nSales : info.nDumps));

				view.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						showInvenDetail(info, position);
					}
				});

				view.setOnLongClickListener(v -> {
					showInvenDetailAsPopup(info);
					return false;
				});

			}
			else
			{
				if (convertView == null) {
					if(Utils.getIntSetting(mContext, Global.PREF_DETAIL_ENTRY) == 1 && !mIsPickup)
					{
						view = mContext.getLayoutInflater().inflate(R.layout.row_make_delivery_detail_entry_1, parent, false);
					}
					else
					{
						view = mContext.getLayoutInflater().inflate(R.layout.row_make_delivery, parent, false);
					}

					holder = new ViewHolder();
					holder.txtItemNo = (TextView) view.findViewById(R.id.txtItemNo);
					holder.txtShortDesc = (TextView) view.findViewById(R.id.txtShortDesc);
					holder.txtPickLocationCode = (TextView) view.findViewById(R.id.txtPickLocationCode);
					holder.txtLoaded = (TextView) view.findViewById(R.id.txtLoaded);
					holder.txtFreeItem = (TextView) view.findViewById(R.id.txtFreeItem);
					holder.txtAvailable = (TextView) view.findViewById(R.id.txtAvailableTruck);
					holder.txtPrice = (TextView)view.findViewById(R.id.txtPrice);
					view.setTag(holder);
				} else {
					holder = (ViewHolder) view.getTag();
				}
				if(info.nTrxnType == Global.TRXN_FREE || info.nTrxnType == Global.TRXN_SAMPLE)
				{
					holder.txtFreeItem.setVisibility(View.VISIBLE);

					holder.txtItemNo.setTextColor(mContext.getResources().getColor(R.color.orange_text_color));
					holder.txtShortDesc.setTextColor(mContext.getResources().getColor(R.color.orange_text_color));
					holder.txtPickLocationCode.setTextColor(mContext.getResources().getColor(R.color.orange_text_color));
					holder.txtFreeItem.setTextColor(mContext.getResources().getColor(R.color.orange_text_color));
					holder.txtLoaded.setTextColor(mContext.getResources().getColor(R.color.orange_text_color));
					holder.txtAvailable.setTextColor(mContext.getResources().getColor(R.color.orange_text_color));
					holder.txtPrice.setTextColor(mContext.getResources().getColor(R.color.orange_text_color));

				}
				else
				{
					holder.txtFreeItem.setVisibility(View.INVISIBLE);

					if(Utils.getIntSetting(mContext, Global.PREF_DETAIL_ENTRY) == 1 && !mIsPickup)
					{
						if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
						{
							double dStock = getStockValueD(info.strItemNo);
							double dPresoldStock = getPresoldValueD(info.strItemNo);
							dStock -= dPresoldStock;
							if(dStock > 0)
							{
								holder.txtItemNo.setTextColor(mContext.getResources().getColor(R.color.black_color));
								holder.txtShortDesc.setTextColor(mContext.getResources().getColor(R.color.black_color));
								holder.txtPickLocationCode.setTextColor(mContext.getResources().getColor(R.color.black_color));
								holder.txtFreeItem.setTextColor(mContext.getResources().getColor(R.color.black_color));
								holder.txtLoaded.setTextColor(mContext.getResources().getColor(R.color.black_color));
								holder.txtAvailable.setTextColor(mContext.getResources().getColor(R.color.black_color));
								holder.txtPrice.setTextColor(mContext.getResources().getColor(R.color.black_color));
							}
							else
							{
								holder.txtItemNo.setTextColor(mContext.getResources().getColor(R.color.red_color));
								holder.txtShortDesc.setTextColor(mContext.getResources().getColor(R.color.red_color));
								holder.txtPickLocationCode.setTextColor(mContext.getResources().getColor(R.color.red_color));
								holder.txtFreeItem.setTextColor(mContext.getResources().getColor(R.color.red_color));
								holder.txtLoaded.setTextColor(mContext.getResources().getColor(R.color.red_color));
								holder.txtAvailable.setTextColor(mContext.getResources().getColor(R.color.red_color));
								holder.txtPrice.setTextColor(mContext.getResources().getColor(R.color.red_color));
							}
						}
						else
						{
							int nStock = getStockValue(info.strItemNo);
							int nPresoldStock = getPresoldValue(info.strItemNo);
							nStock -= nPresoldStock;
							if(nStock > 0)
							{
								holder.txtItemNo.setTextColor(mContext.getResources().getColor(R.color.black_color));
								holder.txtShortDesc.setTextColor(mContext.getResources().getColor(R.color.black_color));
								holder.txtPickLocationCode.setTextColor(mContext.getResources().getColor(R.color.black_color));
								holder.txtFreeItem.setTextColor(mContext.getResources().getColor(R.color.black_color));
								holder.txtLoaded.setTextColor(mContext.getResources().getColor(R.color.black_color));
								holder.txtAvailable.setTextColor(mContext.getResources().getColor(R.color.black_color));
								holder.txtPrice.setTextColor(mContext.getResources().getColor(R.color.black_color));
							}
							else
							{
								holder.txtItemNo.setTextColor(mContext.getResources().getColor(R.color.red_color));
								holder.txtShortDesc.setTextColor(mContext.getResources().getColor(R.color.red_color));
								holder.txtPickLocationCode.setTextColor(mContext.getResources().getColor(R.color.red_color));
								holder.txtFreeItem.setTextColor(mContext.getResources().getColor(R.color.red_color));
								holder.txtLoaded.setTextColor(mContext.getResources().getColor(R.color.red_color));
								holder.txtAvailable.setTextColor(mContext.getResources().getColor(R.color.red_color));
								holder.txtPrice.setTextColor(mContext.getResources().getColor(R.color.red_color));
							}
						}
					}
					else
					{
						holder.txtItemNo.setTextColor(mContext.getResources().getColor(R.color.black_color));
						holder.txtShortDesc.setTextColor(mContext.getResources().getColor(R.color.black_color));
						holder.txtPickLocationCode.setTextColor(mContext.getResources().getColor(R.color.black_color));
						holder.txtFreeItem.setTextColor(mContext.getResources().getColor(R.color.black_color));
						holder.txtLoaded.setTextColor(mContext.getResources().getColor(R.color.black_color));
					}

				}

//				String strItemNo = "";
//				if(Utils.getStringSetting(mContext, Global.PREF_ITEM_CODE_DISPLAY_SOURCE).equals("PRODINFO"))
//				{
//					ProdInfo prodInfo = mProdInfoMap.get(info.strItemNo);
//					strItemNo = prodInfo != null ? prodInfo.strInfoType : "";
//				}
//				else if(Utils.getStringSetting(mContext, Global.PREF_ITEM_CODE_DISPLAY_SOURCE).equals("UPC"))
//				{
//					ProdDetlInfo prodDetlInfo = mProdDetlItemMap.get(info.strItemNo);
//					strItemNo = prodDetlInfo != null ? prodDetlInfo.strItemUPC : "";
//				}
//				else if(Utils.getStringSetting(mContext, Global.PREF_ITEM_CODE_DISPLAY_SOURCE).equals("UPC20"))
//				{
//					ProdDetlInfo prodDetlInfo = mProdDetlItemMap.get(info.strItemNo);
//					strItemNo = prodDetlInfo != null ? prodDetlInfo.strItemUPC20 : "";
//				}
//				else {
//					strItemNo = info.strItemNo;
//				}
//				holder.txtItemNo.setText(strItemNo);

				holder.txtItemNo.setText(Utils.getDisplayItemCode(mContext, info.strItemNo));

				holder.txtShortDesc.setText(info.strShortDesc);
				holder.txtPickLocationCode.setText(info.strPickLocationCode);
				if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
				{
					holder.txtLoaded.setText(String.format(Locale.ENGLISH, "%.2f", mIsPickup == false ? info.dSales : info.dDumps));
				}
				else
				{
					if(bShowCase)
					{
						if( Global.gCurCustomerInfo.strSalEntryMode.equals("B") ||Global.gCurCustomerInfo.strSalEntryMode.equals("C"))
						{
							int nCase = getCaseValue(info.strItemNo);
							int nQty = mIsPickup == false ? info.nSales : info.nDumps;
							if (Utils.isContainerItem(mContext, info.strItemNo)) {
								holder.txtLoaded.setText(String.format(Locale.ENGLISH, "%d", nQty));
							}
							else {
								holder.txtLoaded.setText(String.format(Locale.ENGLISH, "%d/%d", nQty / nCase, nQty % nCase));
							}
						}
						else if( Global.gCurCustomerInfo.strSalEntryMode.equals("") || Global.gCurCustomerInfo.strSalEntryMode.equals("U"))
						{
							int nQty = mIsPickup == false ? info.nSales : info.nDumps;
							holder.txtLoaded.setText(String.format(Locale.ENGLISH, "%d", nQty));
						}
					}
					else
					{
						int nQty = mIsPickup == false ? info.nSales : info.nDumps;
						holder.txtLoaded.setText(String.format(Locale.ENGLISH, "%d", nQty));
					}
				}

				if(Utils.getIntSetting(mContext, Global.PREF_DETAIL_ENTRY) == 1)
				{
					view.findViewById(R.id.layoutDetailEntry).setVisibility(View.VISIBLE);
					if(!mIsPickup)
					{
						String strShow;
						if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
						{
							double dStock = getStockValueD(info.strItemNo);
							double dPresoldStock = getPresoldValueD(info.strItemNo);
							dStock -= dPresoldStock;
							strShow = "         " + Str.AvailableForVanSales[Global.LANG] + " : " + String.format(Locale.ENGLISH, "%.2f", dStock);

						}
						else
						{
							int nStock = getStockValue(info.strItemNo);
							int nPresoldStock = getPresoldValue(info.strItemNo);
							nStock -= nPresoldStock;
							int nCase = getCaseValue(info.strItemNo);
							if(Global.gCurCustomerInfo.strSalEntryMode.equals("B") || Global.gCurCustomerInfo.strSalEntryMode.equals("C"))
							{
								if (Utils.isContainerItem(mContext, info.strItemNo)) {
									strShow = "         " + Str.AvailableForVanSales[Global.LANG]  + " : " + String.format(Locale.ENGLISH, "%d", nStock);
								}
								else {
									strShow = "         " + Str.AvailableForVanSales[Global.LANG]  + " : " + String.format(Locale.ENGLISH, "%d / %d", nStock / nCase, nStock % nCase);
								}
							}
							else
							{
								strShow = "         " + Str.AvailableForVanSales[Global.LANG] + " : " + String.format(Locale.ENGLISH, "%d", nStock);
							}
						}
						holder.txtAvailable.setText(strShow);
					}

					if(info.nTrxnType != Global.TRXN_SAMPLE && info.nTrxnType != Global.TRXN_FREE)
					{
						String strShow = String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(mContext, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(info.nPrice));
						holder.txtPrice.setVisibility(View.VISIBLE);
						holder.txtPrice.setText(strShow);
					}
					else
					{
						holder.txtPrice.setVisibility(View.INVISIBLE);
					}

				}
				else
				{
					view.findViewById(R.id.layoutDetailEntry).setVisibility(View.GONE);
				}

				view.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						showInvenDetail(info, position);
					}
				});
				view.setOnLongClickListener(v -> {
					showInvenDetailAsPopup(info);
					return false;
				});
			}

			return view;
		}
	}

	private class SlideListAdapter extends BaseAdapter {

		private List<UOrderDInfo> list = new ArrayList<>();

		private class ViewHolder {
			public TextView txtItemNo, txtShortDesc, txtPickLocationCode, txtLoaded, txtStock, txtFreeItem;
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
				view = mContext.getLayoutInflater().inflate(R.layout.slide_row_make_delivery, parent, false);
				holder = new ViewHolder();
				holder.txtShortDesc = (TextView) view.findViewById(R.id.txtShortDesc);
				holder.txtPickLocationCode = (TextView) view.findViewById(R.id.txtPickLocationCode);
				holder.txtLoaded = (TextView) view.findViewById(R.id.txtLoaded);
				holder.txtStock = (TextView) view.findViewById(R.id.txtStock);
				holder.txtFreeItem = (TextView) view.findViewById(R.id.txtFreeItem);
				view.setTag(holder);
			} else {
				holder = (ViewHolder) view.getTag();
			}

			if(info.nTrxnType == Global.TRXN_FREE || info.nTrxnType == Global.TRXN_SAMPLE)
			{
				holder.txtFreeItem.setVisibility(View.VISIBLE);

				holder.txtShortDesc.setTextColor(mContext.getResources().getColor(R.color.orange_text_color));
				holder.txtPickLocationCode.setTextColor(mContext.getResources().getColor(R.color.orange_text_color));
				holder.txtLoaded.setTextColor(mContext.getResources().getColor(R.color.orange_text_color));
				holder.txtStock.setTextColor(mContext.getResources().getColor(R.color.orange_text_color));
				holder.txtFreeItem.setTextColor(mContext.getResources().getColor(R.color.orange_text_color));
			}
			else
			{
				holder.txtFreeItem.setVisibility(View.INVISIBLE);

				if(Utils.getIntSetting(mContext, Global.PREF_SHOW_INV) == 1 && !mIsPickup)
				{
					if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
					{
						double dStock = getStockValueD(info.strItemNo);
						double dPresoldStock = getPresoldValueD(info.strItemNo);
						dStock -= dPresoldStock;
						if(dStock > 0)
						{
							holder.txtShortDesc.setTextColor(mContext.getResources().getColor(R.color.black_color));
							holder.txtPickLocationCode.setTextColor(mContext.getResources().getColor(R.color.black_color));
							holder.txtLoaded.setTextColor(mContext.getResources().getColor(R.color.black_color));
							holder.txtStock.setTextColor(mContext.getResources().getColor(R.color.black_color));
							holder.txtFreeItem.setTextColor(mContext.getResources().getColor(R.color.black_color));
						}
						else
						{
							holder.txtShortDesc.setTextColor(mContext.getResources().getColor(R.color.red_color));
							holder.txtPickLocationCode.setTextColor(mContext.getResources().getColor(R.color.red_color));
							holder.txtLoaded.setTextColor(mContext.getResources().getColor(R.color.red_color));
							holder.txtStock.setTextColor(mContext.getResources().getColor(R.color.red_color));
							holder.txtFreeItem.setTextColor(mContext.getResources().getColor(R.color.red_color));
						}
					}
					else
					{
						int nStock = getStockValue(info.strItemNo);
						int nPresoldStock = getPresoldValue(info.strItemNo);
						nStock -= nPresoldStock;
						if(nStock > 0)
						{
							holder.txtShortDesc.setTextColor(mContext.getResources().getColor(R.color.black_color));
							holder.txtPickLocationCode.setTextColor(mContext.getResources().getColor(R.color.black_color));
							holder.txtLoaded.setTextColor(mContext.getResources().getColor(R.color.black_color));
							holder.txtStock.setTextColor(mContext.getResources().getColor(R.color.black_color));
							holder.txtFreeItem.setTextColor(mContext.getResources().getColor(R.color.black_color));
						}
						else
						{
							holder.txtShortDesc.setTextColor(mContext.getResources().getColor(R.color.red_color));
							holder.txtPickLocationCode.setTextColor(mContext.getResources().getColor(R.color.red_color));
							holder.txtLoaded.setTextColor(mContext.getResources().getColor(R.color.red_color));
							holder.txtStock.setTextColor(mContext.getResources().getColor(R.color.red_color));
							holder.txtFreeItem.setTextColor(mContext.getResources().getColor(R.color.red_color));
						}
					}
				}
				else
				{
					holder.txtShortDesc.setTextColor(mContext.getResources().getColor(R.color.black_color));
					holder.txtPickLocationCode.setTextColor(mContext.getResources().getColor(R.color.black_color));
					holder.txtLoaded.setTextColor(mContext.getResources().getColor(R.color.black_color));
					holder.txtStock.setTextColor(mContext.getResources().getColor(R.color.black_color));
					holder.txtFreeItem.setTextColor(mContext.getResources().getColor(R.color.black_color));
				}
			}

			holder.txtShortDesc.setText(info.strDescription);
			holder.txtPickLocationCode.setText(info.strPickLocationCode);
			if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
			{
				double dQty = mIsPickup == false ? info.dSales : info.dDumps;
				holder.txtStock.setText(String.format(Locale.ENGLISH, "%.2f", dQty));
				holder.txtLoaded.setText(String.format(Locale.ENGLISH, "%.2f", info.dPlanQty));
			}
			else
			{
				if(bShowCase)
				{
					if( Global.gCurCustomerInfo.strSalEntryMode.equals("B") || Global.gCurCustomerInfo.strSalEntryMode.equals("C"))
					{
						if (Utils.isContainerItem(mContext, info.strItemNo)) {
							holder.txtLoaded.setText(String.format(Locale.ENGLISH, "%d", info.nPlanQty));
							int nQty = mIsPickup == false ? info.nSales : info.nDumps;
							holder.txtStock.setText(String.format(Locale.ENGLISH, "%d", nQty));
							holder.txtLoaded.setText(String.format(Locale.ENGLISH, "%d", info.nPlanQty));
						}
						else {
							int nCase = getCaseValue(info.strItemNo);
							holder.txtLoaded.setText(String.format(Locale.ENGLISH, "%d/%d", info.nPlanQty / nCase, info.nPlanQty % nCase));
							int nQty = mIsPickup == false ? info.nSales : info.nDumps;
							holder.txtStock.setText(String.format(Locale.ENGLISH, "%d/%d", nQty / nCase, nQty % nCase));
							holder.txtLoaded.setText(String.format(Locale.ENGLISH, "%d/%d", info.nPlanQty / nCase, info.nPlanQty % nCase));
						}
					}
					else if( Global.gCurCustomerInfo.strSalEntryMode.equals("") || Global.gCurCustomerInfo.strSalEntryMode.equals("U"))
					{
						holder.txtLoaded.setText(String.format(Locale.ENGLISH, "%d", info.nPlanQty));
						int nQty = mIsPickup == false ? info.nSales : info.nDumps;
						holder.txtStock.setText(String.valueOf(nQty));
						holder.txtLoaded.setText(String.valueOf(info.nPlanQty));
					}
				}
				else {
					holder.txtLoaded.setText(String.format(Locale.ENGLISH, "%d", info.nPlanQty));
					int nQty = mIsPickup == false ? info.nSales : info.nDumps;
					holder.txtStock.setText(String.valueOf(nQty));
					holder.txtLoaded.setText(String.valueOf(info.nPlanQty));
				}
			}

			view.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showInvenDetail(info, position);
				}
			});
			view.setOnLongClickListener(v -> {
				showInvenDetailAsPopup(info);
				return false;
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

		int nTrxnType = 0;
		if(!mIsPickup)
		{
			if(bFreeItem)
			{
				nTrxnType = Global.TRXN_SAMPLE;
			}
			else
			{
				nTrxnType = Global.TRXN_DELIVER;
			}
		}

		if(nTrxnType != Global.TRXN_SAMPLE)
		{
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
		else
		{
			return 0;
		}
	}

	class MultiPromotionDetails {
		String strFreeItemNo = "";
		int nFreeItemCount = 0;

		public MultiPromotionDetails(String freeItemNo, int freeItemCount) {
			this.strFreeItemNo = freeItemNo;
			this.nFreeItemCount = freeItemCount;
		}
	}

	private int getMultiPromotionCount(UOrderDInfo orderInfo, ProdDetlInfo prodInfo, int nQuantity, boolean isSimFreeMode, MultiPromotionDetails promDetails) {
		if (isSimFreeMode) {
			return 0;
		}

		if (mIsPickup || bFreeItem) {
			return 0;
		}

		if (prodInfo.uPromotionInfoList == null) {
			return 0;
		}

		if (prodInfo.uPromotionInfoList.size() == 0) {
			return 0;
		}

		UPromotionInfo promInfo = null;
		for (UPromotionInfo _promInfo: prodInfo.uPromotionInfoList) {
			if (_promInfo.nPromoType == Global.PROMO_TYPE_BUY_FREE_MULTI) {
				promInfo = _promInfo;
				break;
			}
		}
		if (promInfo == null) {
			return 0;
		}

		int nPromoDiscount = Math.round(Utils.getDividedValue(promInfo.nDiscAmt));
		if (nPromoDiscount <= 0) {
			return 0;
		}

		String strFreeItemNo = promInfo.strFreeItemNo;
		if (strFreeItemNo == null) {
			return 0;
		}

		int nFreeItemCount = 0;
		if (nQuantity == 0 && orderInfo.nPlanQty == 0) {
			nFreeItemCount = 0;
		}
		else {
			nFreeItemCount = nQuantity / nPromoDiscount;
		}

		mSimSelProductInfo = mProdDetlItemMap.get(strFreeItemNo);
		if (mSimSelProductInfo == null) {
			return 0;
		}

		promDetails.nFreeItemCount = nFreeItemCount;
		promDetails.strFreeItemNo = strFreeItemNo;

		return nFreeItemCount;
	}

	private void doProcessMultiPromotion(UOrderDInfo orderInfo, ProdDetlInfo prodInfo, int nQuantity, boolean isSimFreeMode) {
		// check if there is multipromotion
		// and process manual free item if there is proper promotion
		if (isSimFreeMode) {
			return;
		}

		if (mIsPickup || bFreeItem) {
			return;
		}

		if (prodInfo.uPromotionInfoList == null) {
			return;
		}

		if (prodInfo.uPromotionInfoList.size() == 0) {
			return;
		}

		UPromotionInfo promInfo = null;
		for (UPromotionInfo _promInfo: prodInfo.uPromotionInfoList) {
			if (_promInfo.nPromoType == Global.PROMO_TYPE_BUY_FREE_MULTI) {
				promInfo = _promInfo;
				break;
			}
		}
		if (promInfo == null) {
			return;
		}

		int nPromoDiscount = Math.round(Utils.getDividedValue(promInfo.nDiscAmt));
		if (nPromoDiscount <= 0) {
			return;
		}

		String strFreeItemNo = promInfo.strFreeItemNo;
		if (strFreeItemNo == null) {
			return;
		}

		int nFreeItemCount = 0;
		if (nQuantity == 0 && orderInfo.nPlanQty == 0) {
			nFreeItemCount = 0;
		}
		else {
			nFreeItemCount = nQuantity / nPromoDiscount;
		}

		mSimSelProductInfo = mProdDetlItemMap.get(strFreeItemNo);
		if (mSimSelProductInfo == null) {
			return;
		}

		mIsSimFreeMode = true;
		mStrSimCase = "";
		mStrSimQty = String.valueOf(nFreeItemCount);

		doAdd(true);
	}

	static class FreeItemDetails {
		String strFreeItemNo = "";
		int nFreeItemCount = 0;
	}

	private FreeItemDetails getFreeItemDetailsByPromotion(ProdDetlInfo prodInfo, int nQuantity) {

		if (mIsPickup || bFreeItem) {
			return null;
		}

		if (prodInfo.uPromotionInfoList == null) {
			return null;
		}

		if (prodInfo.uPromotionInfoList.size() == 0) {
			return null;
		}

		UPromotionInfo promInfo = null;
		for (UPromotionInfo _promInfo: prodInfo.uPromotionInfoList) {
			if (_promInfo.nPromoType == Global.PROMO_TYPE_BUY_FREE_MULTI) {
				promInfo = _promInfo;
				break;
			}
		}
		if (promInfo == null) {
			return null;
		}

		int nPromoDiscount = Math.round(Utils.getDividedValue(promInfo.nDiscAmt));
		if (nPromoDiscount <= 0) {
			return null;
		}

		String strFreeItemNo = promInfo.strFreeItemNo;
		if (strFreeItemNo == null) {
			return null;
		}

		FreeItemDetails freeItemDetails = new FreeItemDetails();
		freeItemDetails.strFreeItemNo = strFreeItemNo;

		freeItemDetails.nFreeItemCount = nQuantity / nPromoDiscount;

		if (mProdDetlItemMap.get(strFreeItemNo) != null)
		{
			return freeItemDetails;
		}
		return null;
	}
}

