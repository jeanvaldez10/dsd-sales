package dsd.dsdvansales.fragment;

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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import dsd.dsdvansales.R;
import dsd.dsdvansales.db.DbAdapter;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.model.LoadDetlInfo;
import dsd.dsdvansales.model.ProdDetlInfo;
import dsd.dsdvansales.model.ProdInfo;
import dsd.dsdvansales.model.UInvenDInfo;
import dsd.dsdvansales.model.UInvenHInfo;

public class LoadContainersFragment extends BaseFragment implements View.OnClickListener {

	private TextView mTxtNoData = null;
	private ListView mListView = null;
	private ListAdapter mListAdapter = null;
	private List<UInvenDInfo> mUInvenDList = new ArrayList<>();
	private List<ProdDetlInfo> mProdDetlList = new ArrayList<>();
	private Map<String, ProdDetlInfo> mProdUPCDetMap = new HashMap<>();
	private Map<String, ProdDetlInfo> mProdDetlItemMap = new HashMap<>();
	private Map<String, ProdInfo> mProdInfoMap = new HashMap<>();

	private int mProdDetlIdx = -1;
	private String mEntryInputMethod = "";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootView = inflater.inflate(R.layout.fragment_load_containers, container, false);
		commonInit();
		return mRootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		Global.gFragIdx = Global.FRAG_LOAD_CONTAINERS;
	}

	@Override
	public void onPause() {
		super.onPause();
		if (Global.gUInvenHinfo != null) {
			Global.gUInvenHinfo.containerList.clear();
			for (UInvenDInfo info : mUInvenDList) {
				info.nLoadAdj = info.nStock - info.nLoaded;
				Global.gUInvenHinfo.containerList.add(info);
			}
		}
	}

	@Override
	public void initView() {
		mRootView.findViewById(R.id.btnAdd).setOnClickListener(this);
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
					doSearch();
				}
				else
				{
					doProcessSelectProduct(strItemNo, false);
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

	@Override
	public void loadData() {
		mUInvenDList.clear();
		mProdDetlList.clear();
		List<UInvenDInfo> uInvenDList = new ArrayList<>();
		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		mProdDetlList = dbAdapter.getProdDetlContainerList(mProdDetlItemMap, mProdUPCDetMap);
//		if(Utils.getStringSetting(mContext, Global.PREF_ITEM_CODE_DISPLAY_SOURCE).equals("PRODINFO"))
//		{
//			mProdInfoMap = dbAdapter.getProdInfoMap("ItemCode");
//		}

		List<ProdDetlInfo> prodDetlList = dbAdapter.getProdDetlList();
		List<LoadDetlInfo> loadDetlList = dbAdapter.getLoadDetlContainerList();
		dbAdapter.close();

		for (LoadDetlInfo loadDetlInfo : loadDetlList) {
			UInvenDInfo uInvenDInfo = new UInvenDInfo();
			uInvenDInfo.strItemNo = loadDetlInfo.strItemNo;
			uInvenDInfo.strPickLocationCode = loadDetlInfo.strPickLocationCode;
			uInvenDInfo.nLoaded = loadDetlInfo.nQty;
			for (ProdDetlInfo info : prodDetlList) {
				if (info.strItemNo.equals(loadDetlInfo.strItemNo) == true) {
					uInvenDInfo.strShortDesc = info.strShortDesc;
					break;
				}
			}
			uInvenDList.add(uInvenDInfo);
		}
		if (Global.gUInvenHinfo == null) {
			Date curDate = new Date();
			Global.gUInvenHinfo = new UInvenHInfo();
			Global.gUInvenHinfo.nTrxnNo = System.currentTimeMillis();
			Global.gUInvenHinfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(mContext, curDate));
			Global.gUInvenHinfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(curDate);
		}
		if (Global.gUInvenHinfo.containerList == null) {
			Global.gUInvenHinfo.containerList = new ArrayList<>();
			mUInvenDList.addAll(uInvenDList);
		} else {
			mUInvenDList.addAll(Global.gUInvenHinfo.containerList);
		}
		refreshPage();
	}

	@Override
	public void refreshPage() {
		if (mUInvenDList.size() > 0) {
			mListView.setVisibility(View.VISIBLE);
			mTxtNoData.setVisibility(View.GONE);
			mListAdapter.notifyDataSetChanged();
		} else {
			mListView.setVisibility(View.GONE);
			mTxtNoData.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void refreshLanguage() {
		((TextView) mRootView.findViewById(R.id.lblTitle)).setText(Str.LoadContainers[Global.LANG].toUpperCase());
		((TextView) mRootView.findViewById(R.id.txtTrip)).setText(Str.Trip[Global.LANG] + " # " + Global.gTripNumber);
		((TextView) mRootView.findViewById(R.id.txtQty)).setText(Str.Qty[Global.LANG]);
		((TextView) mRootView.findViewById(R.id.txtDescription)).setText("");
		((Button) mRootView.findViewById(R.id.btnAdd)).setText(Str.Add[Global.LANG]);
		((Button) mRootView.findViewById(R.id.btnDone)).setText(Str.Done[Global.LANG]);
		mTxtNoData.setText(Str.No_data_has_been_found[Global.LANG]);
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

	private void doProcessSelectProduct(final String strItemNo, boolean bUpdateItemNo)
	{
		if (strItemNo.length() == 0) {
			Utils.printDebug(mContext, Str.NoProductFound[Global.LANG]);
		} else {
			if(bUpdateItemNo || !Utils.getStringSetting(mContext, Global.PREF_AUTO_SCAN_ADD).equals("1"))
			{
				((EditText) mRootView.findViewById(R.id.editItemNo)).setText(strItemNo);
				if(Utils.getProdLoconSCWType(mContext, strItemNo) == Global.WEIGHT_ITEM)
				{
					mRootView.findViewById(R.id.editQty).setTag("From App");
					((EditText) mRootView.findViewById(R.id.editQty)).setText(String.format(Locale.ENGLISH, "@.2f", 0f));
					mRootView.findViewById(R.id.editQty).setTag(null);
					this.showKeyboard((EditText) mRootView.findViewById(R.id.editQty), true);
				}
				else
				{
					((EditText) mRootView.findViewById(R.id.editQty)).setText("0");
					this.showKeyboard((EditText) mRootView.findViewById(R.id.editQty), true);
				}
			}
		}
		doSearch();
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
	public void receiveBroadcast(String strAction, Intent intent) {
		if (strAction.equals(Global.ACTION_SELECT_PRODUCT) == true) {
			int nType = intent.getIntExtra(Global.EXTRA_TYPE, 0);
			String strItemNo = "";
			if (nType == Global.SELECT_PRODUCT_ITEMNO) {
				strItemNo = intent.getStringExtra(Global.EXTRA_ITEM_NO);
				doProcessSelectProduct(strItemNo, true);
			} else if (nType == Global.SELECT_PRODUCT_ITEMUPC) {
				String strItemUPC = intent.getStringExtra(Global.EXTRA_ITEM_UPC);
				for (ProdDetlInfo info : mProdDetlList) {
					if (info.strItemUPC.equals(strItemUPC) == true || info.strItemUPC20.equals(strItemUPC)) {
						strItemNo = info.strItemNo;
						break;
					}
				}
				((EditText) mRootView.findViewById(R.id.editItemNo)).setText(strItemUPC);
				doProcessSelectProduct(strItemNo, false);
			}

		}
	}

	private void doSearch() {
		String strItemNo = getItemCode(((EditText) mRootView.findViewById(R.id.editItemNo)).getText().toString());
		String strDescription = Str.Invalid_Product[Global.LANG];
		String strQty = "";
		mProdDetlIdx = -1;
		if (strItemNo.length() > 0) {
			for (int i = 0; i < mProdDetlList.size(); i++) {
				ProdDetlInfo prodDetlInfo = mProdDetlList.get(i);
				if (Utils.getDisplayItemCode(mContext, prodDetlInfo.strItemNo).equals(strItemNo)) {
					mProdDetlIdx = i;
					strItemNo = prodDetlInfo.strItemNo;
					strDescription = prodDetlInfo.strDescription;
					break;
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
					strQty = String.valueOf(info.nStock);
					break;
				}
			}
		}
		else
		{
			strDescription = "";
		}
		((TextView) mRootView.findViewById(R.id.txtDescription)).setText(strDescription);
		if(mEntryInputMethod.equals("A"))
		{
			((EditText) mRootView.findViewById(R.id.editQty)).setText("");
		}
		else
		{
			((EditText) mRootView.findViewById(R.id.editQty)).setText(strQty);
		}
	}

	private void doAdd() {
		String strQty = ((EditText) mRootView.findViewById(R.id.editQty)).getText().toString();
		if (mProdDetlIdx == -1) {
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
		ProdDetlInfo productInfo = mProdDetlList.get(mProdDetlIdx);
		for (int i = 0; i < mUInvenDList.size(); i++) {
			if (mUInvenDList.get(i).strItemNo.equals(productInfo.strItemNo) == true) {
				nIdx = i;
				break;
			}
		}

		int nQuantity = Utils.parseIntFromString(strQty);
		if (nIdx == -1) {
			if (nQuantity == 0) {
				Utils.printDebug(mContext, Str.Please_input_the_Quantity[Global.LANG]);
				showKeyboard((EditText) mRootView.findViewById(R.id.editQty), true);
				return;
			} else {
				if(nQuantity < 0)
				{
					((EditText) mRootView.findViewById(R.id.editQty)).setText("");
					Utils.printDebug(mContext, Str.Quantity_Removed_From_the_transaction[Global.LANG]);
					showKeyboard((EditText) mRootView.findViewById(R.id.editQty), true);
					return;
				}
				UInvenDInfo info = new UInvenDInfo();
				info.strItemNo = productInfo.strItemNo;
				info.nStock = nQuantity;
				info.strShortDesc = productInfo.strShortDesc;
				mUInvenDList.add(0, info);
			}
		} else {
			if(mEntryInputMethod.equals("A"))
			{
				if( mUInvenDList.get(nIdx).nStock + nQuantity == 0){
					if(mUInvenDList.get(nIdx).nLoaded == 0)
					{
						mUInvenDList.remove(nIdx);
					}
				}
				else if(mUInvenDList.get(nIdx).nStock + nQuantity < 0){
					((EditText) mRootView.findViewById(R.id.editQty)).setText("");
					Utils.printDebug(mContext, Str.Quantity_Removed_From_the_transaction[Global.LANG]);
					showKeyboard((EditText) mRootView.findViewById(R.id.editQty), true);
					return;
				}
				else
				{
					mUInvenDList.get(nIdx).nStock += nQuantity;
					if (mUInvenDList.get(nIdx).nLoaded < mUInvenDList.get(nIdx).nStock) {
						Utils.printDebug(mContext, Str.ActualLoadQuantityExceedsPlan[Global.LANG]);
					}
				}
			}
			else
			{
				if (nQuantity == 0) {
					if (mUInvenDList.get(nIdx).nLoaded == 0) {
						mUInvenDList.remove(nIdx);
					}
				} else {
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

		refreshPage();
	}

	private void showInvenDetail(UInvenDInfo info) {
		((EditText) mRootView.findViewById(R.id.editItemNo)).setText(Utils.getDisplayItemCode(mContext, info.strItemNo));
		String strQty = String.valueOf(info.nStock);
		if(mEntryInputMethod.equals("A"))
		{
			((EditText) mRootView.findViewById(R.id.editQty)).setText("");
		}
		else
		{
			((EditText) mRootView.findViewById(R.id.editQty)).setText(strQty);
		}
		mRootView.findViewById(R.id.editQty).requestFocus();
		((EditText) mRootView.findViewById(R.id.editQty)).setSelection(strQty.length());
	}

	private void doDone() {
		mContext.replaceFragment(new LoadTruckSignFragment());
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
			case R.id.btnAdd:
				doAdd();
				break;
			case R.id.btnDone:
				doDone();
				break;
		}
		m_bClicked = false;
	}

	private class ListAdapter extends BaseAdapter {

		private class ViewHolder {
			public TextView txtItemNo, txtShortDesc, txtLoaded, txtStock;
		}

		public int getCount() {
			return mUInvenDList.size();
		}

		public UInvenDInfo getItem(int position) {
			if(position < mUInvenDList.size())
			{
				return mUInvenDList.get(position);
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
				view = mContext.getLayoutInflater().inflate(R.layout.row_load_containers, parent, false);
				holder = new ViewHolder();
				holder.txtItemNo = (TextView) view.findViewById(R.id.txtItemNo);
				holder.txtShortDesc = (TextView) view.findViewById(R.id.txtShortDesc);
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
			holder.txtLoaded.setText(String.valueOf(info.nLoaded));
			holder.txtStock.setText(String.valueOf(info.nStock));

			view.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showInvenDetail(info);
				}
			});

			return view;
		}
	}
}