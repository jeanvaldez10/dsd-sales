package dsd.dsdvansales.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.GridView;

import com.squareup.picasso.Picasso;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import dsd.dsdvansales.R;
import dsd.dsdvansales.db.DbAdapter;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.model.AuthDetInfo;
import dsd.dsdvansales.model.AuthHeaderInfo;
import dsd.dsdvansales.model.PresoldOrDInfo;
import dsd.dsdvansales.model.ProdDetlInfo;
import dsd.dsdvansales.model.UInvenDInfo;
import dsd.dsdvansales.model.UInvenHInfo;
import dsd.dsdvansales.views.AlertSingleChoiceAdapter;
import dsd.dsdvansales.views.ClickEffectImageView;

public class SearchProductFragment extends BaseFragment implements View.OnClickListener {

	private static final int	SEARCH_BY_PRODUCT_NAME	= 0;
	private static final int	SEARCH_BY_ITEM_TYPE		= 1;
	private static final int	SEARCH_BY_PRODUCT_GROUP	= 2;
	private static final int	SEARCH_BY_PRODUCT_LINE	= 3;
	private static final int	SEARCH_BY_BRAND			= 4;
	private static final int	SEARCH_BY_SUB_BRAND		= 5;

	private EditText mEditKeyword = null;
	private TextView mTxtCriteria = null;

	private TextView mTxtNoData = null;
	private ListView mListView = null;
	private ListAdapter mListAdapter = null;
	GridView mGridView = null;
	GridAdapter mGridAdapter = null;

	public ClickEffectImageView mImageGridView;
	public ClickEffectImageView mImageListView;

	private List<ProdDetlInfo> mProdDetlList = new ArrayList<>();
	private List<ProdDetlInfo> mSearchedList = new ArrayList<>();

	private int mSearchByIdx = 0;
	private String mStrKeyword = "";
	private String mSearchByArray[] = {"Product Name", "Item Type", "Product Group", "Product Line", "Brand", "Sub Brand"};
	private UInvenHInfo mUInvenHinfo;

	private  boolean enableFilterAuthorizedItem = false;

	private AuthHeaderInfo mAuthHeaderInfo;
	private List<AuthDetInfo> mAuthDetlInfoList;
	private Map<String, AuthDetInfo> mAuthItemMap;

	private boolean bFilter = false;
	private boolean bShowCase = true;
	private boolean bListView = true;

	private Map<String, UInvenDInfo> inventoryMap = new HashMap<>();

	public void setEnableFilterAuthorizedItem(boolean bValue)
	{
		enableFilterAuthorizedItem = bValue;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootView = inflater.inflate(R.layout.fragment_search_product, container, false);
		commonInit();
		return mRootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		Global.gFragIdx = Global.FRAG_SEARCH_PRODUCT;
	}

	@Override
	public void initView() {
		bShowCase = Utils.getStringSetting(mContext, Global.PREF_INVENTORYUOM).equals("U") == false;

		mStrKeyword = Utils.getStringSetting(mContext, Global.PREF_SEARCH_KEYWORD);
		mSearchByIdx = Utils.getIntSetting(mContext, Global.PREF_SEARCH_CRITIA_IDX);
		bFilter = Utils.getBooleanSetting(mContext, Global.PREF_SEARCH_FILTER);

		Bundle bundle = getArguments();
		if( bundle != null )
		{
			try
			{
				bFilter = bundle.getBoolean(Global.EXTRA_FILTER, false);
			}catch(Exception e)
			{
				e.printStackTrace();
			}

		}

		mRootView.findViewById(R.id.btnBack).setOnClickListener(this);
		mRootView.findViewById(R.id.btnSearch).setOnClickListener(this);
		mRootView.findViewById(R.id.txtCriteria).setOnClickListener(this);

		mTxtNoData = (TextView) mRootView.findViewById(R.id.txtNoData);
		mListView = (ListView) mRootView.findViewById(R.id.listView);
		mListAdapter = new ListAdapter();
		mListView.setAdapter(mListAdapter);

		mGridView = (GridView) mRootView.findViewById(R.id.gridView);
		mGridAdapter = new GridAdapter();
		mGridView.setAdapter(mGridAdapter);

		mImageListView = (ClickEffectImageView) mRootView.findViewById(R.id.btnListView);
		mImageGridView = (ClickEffectImageView) mRootView.findViewById(R.id.btnGridView);

		mEditKeyword = ((EditText) mRootView.findViewById(R.id.editKeyword));
		mTxtNoData = (TextView) mRootView.findViewById(R.id.txtNoData);
		mEditKeyword.setText(mStrKeyword);
		mEditKeyword.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				doSearch();
			}
		});

		mEditKeyword.setOnEditorActionListener((v, actionId, event) -> {
			if (actionId == EditorInfo.IME_ACTION_SEARCH) {
				showKeyboard(mEditKeyword, false);
				return true;
			}
			return false;
		});

		mTxtCriteria = (TextView) mRootView.findViewById(R.id.txtCriteria);
		mTxtCriteria.setText(mSearchByArray[mSearchByIdx]);
		mRootView.findViewById(R.id.btnFilter).setOnClickListener(view -> {
			bFilter = !bFilter;
			setFilterImage();
			doSearch();
		});
		setFilterImage();

		mImageListView.setOnClickListener(view -> {
			bListView = true;
			setViewModeImages();
			refreshPage();
		});

		mImageGridView.setOnClickListener(view -> {
			bListView = false;
			setViewModeImages();
			refreshPage();
		});

		// set default view mode from route ctl setting
		if (Utils.getStringSetting(mContext, Global.PREF_PROD_SEARCH_IMAGE_MODE).equals("1")) {
			bListView = false;
		}
		else {
			bListView = true;
		}

		setViewModeImages();

		loadData();
	}

	private void setViewModeImages() {
		if(bListView)
		{
			mImageListView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.icon_list_view_selected));
			mImageGridView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.icon_grid_view_normal));
		}
		else
		{
			mImageListView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.icon_list_view_normal));
			mImageGridView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.icon_grid_view_selected));
		}
	}

	private void setFilterImage()
	{
		if(bFilter)
		{
			((ImageView)mRootView.findViewById(R.id.btnFilter)).setImageDrawable(mContext.getResources().getDrawable(R.drawable.icon_filter_fill));
		}
		else
		{
			((ImageView)mRootView.findViewById(R.id.btnFilter)).setImageDrawable(mContext.getResources().getDrawable(R.drawable.icon_filter));
		}
	}

	@Override
	public void loadData() {
		showProgressDialog(Str.Loading[Global.LANG]);
		new Thread()
		{
			@Override
			public void run() {
				DbAdapter dbAdapter = new DbAdapter(mContext);
				dbAdapter.open();

				Global.gAdjustinfo = dbAdapter.getUInvenHInfo(Utils.getLongSetting(mContext, Global.PREF_UINVEN_TRXN_NO));
				if (Global.gAdjustinfo == null) {
					Global.gAdjustinfo = new UInvenHInfo();
					Global.gAdjustinfo.nTrxnNo = Utils.getLongSetting(mContext, Global.PREF_UINVEN_TRXN_NO);
				}

				inventoryMap.clear();
				List<UInvenDInfo> uInvenDList = dbAdapter.getUInvenDList(Global.gAdjustinfo.nTrxnNo);
				for (UInvenDInfo uInvenDInfo : uInvenDList) {
					if(uInvenDInfo.nStock > 0 || uInvenDInfo.dStock > 0)
					{
						inventoryMap.put(uInvenDInfo.strItemNo, uInvenDInfo);
					}
				}

				if(enableFilterAuthorizedItem && Global.gCurCustomerInfo != null && !Global.gCurCustomerInfo.strAuthGrp.equals(""))
				{
					mAuthHeaderInfo = dbAdapter.getAuthHeaderInfo(Utils.parseIntFromString(Global.gCurCustomerInfo.strAuthGrp));
					if(mAuthHeaderInfo != null)
					{
						mAuthDetlInfoList = dbAdapter.getAuthDetailInfo(mAuthHeaderInfo.nAuthGrp);
						mAuthItemMap = new HashMap<>();
						for(AuthDetInfo authDetInfo : mAuthDetlInfoList)
						{
							mAuthItemMap.put(authDetInfo.strItemNo, authDetInfo);
						}
					}
				}

				List<ProdDetlInfo> list;
				if (Global.gProdDetlList.size() == 0) {
					list = dbAdapter.getProdDetlList();
					for (ProdDetlInfo info : list) {
						info.strDescriptionArray[0] = dbAdapter.getDescTypeInfo("ITEMTYPE", info.strItemType).strDescription;
						if(TextUtils.isEmpty(info.strDescriptionArray[0])) info.strDescriptionArray[0] = dbAdapter.getDescTypeInfo("ItemType", info.strItemType).strDescription;

						info.strDescriptionArray[1] = dbAdapter.getDescTypeInfo("PRODGROUP", info.strProdGrp).strDescription;
						if(TextUtils.isEmpty(info.strDescriptionArray[1])) info.strDescriptionArray[1] = dbAdapter.getDescTypeInfo("ProductGroup", info.strProdGrp).strDescription;

						info.strDescriptionArray[2] = dbAdapter.getDescTypeInfo("PRODLINE", info.strProdLine).strDescription;
						if(TextUtils.isEmpty(info.strDescriptionArray[2])) info.strDescriptionArray[2] = dbAdapter.getDescTypeInfo("ProductLine", info.strProdLine).strDescription;

						info.strDescriptionArray[3] = dbAdapter.getDescTypeInfo("BRAND", info.strBrand).strDescription;
						if(TextUtils.isEmpty(info.strDescriptionArray[3])) info.strDescriptionArray[3] = dbAdapter.getDescTypeInfo("Brand", info.strBrand).strDescription;

						info.strDescriptionArray[4] = dbAdapter.getDescTypeInfo("SUBBRAND", info.strSubBrand).strDescription;
						if(TextUtils.isEmpty(info.strDescriptionArray[4])) info.strDescriptionArray[4] = dbAdapter.getDescTypeInfo("SubBrand", info.strSubBrand).strDescription;
					}
					Global.gProdDetlList.clear();
					Global.gProdDetlList.addAll(list);
				}
				mUInvenHinfo = dbAdapter.getUInvenHInfo(Utils.getLongSetting(mContext, Global.PREF_UINVEN_TRXN_NO));
				if (mUInvenHinfo == null) {
					mUInvenHinfo = new UInvenHInfo();
				}
				mUInvenHinfo.uInvenDList = dbAdapter.getUInvenDList(Utils.getLongSetting(mContext, Global.PREF_UINVEN_TRXN_NO));
				dbAdapter.close();
				mProdDetlList.clear();
				if(enableFilterAuthorizedItem && mAuthHeaderInfo != null)
				{
					for(ProdDetlInfo info : Global.gProdDetlList)
					{
						if(isAuthorizedItem(info.strItemNo))
						{
							mProdDetlList.add(info);
						}
					}
				}
				else
				{
					mProdDetlList.addAll(Global.gProdDetlList);
				}
				new Handler(Looper.getMainLooper()).post(() -> {
					hideProgressDialog();
					doSearch();
					refreshLanguage();
				});

			}
		}.start();
	}

	@Override
	public void refreshPage() {

		if (bListView) {
			mRootView.findViewById(R.id.listView).setVisibility(View.VISIBLE);
			mRootView.findViewById(R.id.gridView).setVisibility(View.GONE);
		}
		else {
			mRootView.findViewById(R.id.listView).setVisibility(View.GONE);
			mRootView.findViewById(R.id.gridView).setVisibility(View.VISIBLE);
		}

		if (mSearchedList.size() > 0) {
			mTxtNoData.setVisibility(View.GONE);
			mListAdapter.setList(mSearchedList);
			mListAdapter.notifyDataSetChanged();
			mGridAdapter.setList(mSearchedList);
			mGridAdapter.notifyDataSetChanged();
		} else {
			mListAdapter.setList(mSearchedList);
			mListAdapter.notifyDataSetChanged();
			mGridAdapter.setList(mSearchedList);
			mGridAdapter.notifyDataSetChanged();
			mListView.setVisibility(View.GONE);
			mGridView.setVisibility(View.GONE);
			mTxtNoData.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void refreshLanguage() {
		((TextView) mRootView.findViewById(R.id.lblTitle)).setText(Str.SearchProduct[Global.LANG].toUpperCase());
		((TextView) mRootView.findViewById(R.id.txtSearchBy)).setText(Str.Search_by[Global.LANG]);
		mEditKeyword.setHint(Str.Keyword[Global.LANG]);
		mTxtNoData.setText(Str.No_data_has_been_found[Global.LANG]);
		mSearchByArray[0] = Str.ProductName[Global.LANG];
		mSearchByArray[1] = Str.ItemType[Global.LANG];
		mSearchByArray[2] = Str.ProductGroup[Global.LANG];
		mSearchByArray[3] = Str.ProductLine[Global.LANG];
		mSearchByArray[4] = Str.Brand[Global.LANG];
		mSearchByArray[5] = Str.SubBrand[Global.LANG];
		mTxtCriteria.setText(mSearchByArray[mSearchByIdx]);
		refreshPage();
	}

	@Override
	public void receiveBroadcast(String strAction, Intent intent) {
	}

	public final static Comparator<ProdDetlInfo> prodComparator = new Comparator<ProdDetlInfo>() {
		private final Collator collator = Collator.getInstance();
		@Override
		public int compare(ProdDetlInfo object1, ProdDetlInfo object2) {
			return collator.compare(object1.strDescription, object2.strDescription);
		}
	};
	
	private void doSearch() {
		mStrKeyword = mEditKeyword.getText().toString().toLowerCase();
		mSearchedList.clear();
		for (ProdDetlInfo info : mProdDetlList) {
			if(bFilter && (inventoryMap.get(info.strItemNo) == null || (inventoryMap.get(info.strItemNo).nStock == 0 && inventoryMap.get(info.strItemNo).dStock == 0)))
			{
				continue;
			}
			if (mSearchByIdx == SEARCH_BY_PRODUCT_NAME) {
				if (info.strDescription.toLowerCase().contains(mStrKeyword) == true) {
					mSearchedList.add(info);
				}
			} else if (mSearchByIdx == SEARCH_BY_ITEM_TYPE) {
				if (info.strDescriptionArray[0].toLowerCase().contains(mStrKeyword) == true) {
					mSearchedList.add(info);
				}
			} else if (mSearchByIdx == SEARCH_BY_PRODUCT_GROUP) {
				if (info.strDescriptionArray[1].toLowerCase().contains(mStrKeyword) == true) {
					mSearchedList.add(info);
				}
			} else if (mSearchByIdx == SEARCH_BY_PRODUCT_LINE) {
				if (info.strDescriptionArray[2].toLowerCase().contains(mStrKeyword) == true) {
					mSearchedList.add(info);
				}
			} else if (mSearchByIdx == SEARCH_BY_BRAND) {
				if (info.strDescriptionArray[3].toLowerCase().contains(mStrKeyword) == true) {
					mSearchedList.add(info);
				}
			} else if (mSearchByIdx == SEARCH_BY_SUB_BRAND) {
				if (info.strDescriptionArray[4].toLowerCase().contains(mStrKeyword) == true) {
					mSearchedList.add(info);
				}
			}
		}
		Collections.sort(mSearchedList, prodComparator);
		refreshPage();

		Utils.setStringSetting(mContext, Global.PREF_SEARCH_KEYWORD, mStrKeyword);
		Utils.setIntSetting(mContext, Global.PREF_SEARCH_CRITIA_IDX, mSearchByIdx);
		Utils.setBooleanSetting(mContext, Global.PREF_SEARCH_FILTER, bFilter);
	}

	private void showCriteriaType() {
		final int origIdx = mSearchByIdx;
		final String origText = mTxtCriteria.getText().toString();
		try {
			new AlertDialog.Builder(mContext)
					.setTitle(Str.Search_by[Global.LANG])
					.setSingleChoiceItems(new AlertSingleChoiceAdapter(mContext, mSearchByArray), origIdx, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int nSelected) {
							mSearchByIdx = nSelected;
							mTxtCriteria.setText(mSearchByArray[nSelected]);
						}
					})
					.setPositiveButton(Str.Ok[Global.LANG], new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int nSelected) {
							doSearch();
						}
					})
					.setNegativeButton(Str.Cancel[Global.LANG], new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int nSelected) {
							mSearchByIdx = origIdx;
							mTxtCriteria.setText(origText);
						}
					})
					.create()
					.show();
		}catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	private void doAdd(String strItemNo) {
		mContext.onBackPressed();
		Intent intent = new Intent(Global.ACTION_SELECT_PRODUCT);
		intent.putExtra(Global.EXTRA_TYPE, Global.SELECT_PRODUCT_ITEMNO);
		intent.putExtra(Global.EXTRA_ITEM_NO, strItemNo);
		mContext.sendBroadcast(intent);
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
			case R.id.btnSearch:
				doSearch();
				break;
			case R.id.txtCriteria:
				showCriteriaType();
				break;
		}
		m_bClicked = false;
	}

	private class ListAdapter extends BaseAdapter {

		List<ProdDetlInfo> list = new ArrayList<>();
		private class ViewHolder {
			public TextView txtDescription, txtItemType, txtProductGroup, txtProductLine, txtBrand, txtSubBrand, txtStock;
			public Button btnAdd;
		}

		public int getCount() {
			return list.size();
		}

		public void setList(List<ProdDetlInfo> list)
		{
			if(list != null)
			{
				this.list.clear();;
				this.list.addAll(list);
			}
		}

		public ProdDetlInfo getItem(int position) {
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
			final ProdDetlInfo info = this.getItem(position);
			if(info == null) return null;

			if (convertView == null) {
				view = mContext.getLayoutInflater().inflate(R.layout.row_search_product, parent, false);
				holder = new ViewHolder();
				holder.txtDescription = (TextView) view.findViewById(R.id.txtDescription);
				holder.txtItemType = (TextView) view.findViewById(R.id.txtItemType);
				holder.txtProductGroup = (TextView) view.findViewById(R.id.txtProductGroup);
				holder.txtProductLine = (TextView) view.findViewById(R.id.txtProductLine);
				holder.txtBrand = (TextView) view.findViewById(R.id.txtBrand);
				holder.txtSubBrand = (TextView) view.findViewById(R.id.txtSubBrand);
				holder.txtStock = (TextView) view.findViewById(R.id.txtStock);
				holder.btnAdd = (Button) view.findViewById(R.id.btnAdd);
				view.setTag(holder);
			} else {
				holder = (ViewHolder) view.getTag();
			}

			holder.btnAdd.setText(Str.Add[Global.LANG]);
			if (mSearchByIdx == SEARCH_BY_PRODUCT_NAME) {
				holder.txtDescription.setText(Utils.setKeyword(info.strDescription, mStrKeyword));
			} else {
				holder.txtDescription.setText(info.strDescription);
			}
			if (mSearchByIdx == SEARCH_BY_ITEM_TYPE) {
				holder.txtItemType.setText(TextUtils.concat(Str.ItemType[Global.LANG], " - ", Utils.setKeyword(info.strDescriptionArray[0], mStrKeyword)));
			} else {
				holder.txtItemType.setText(Str.ItemType[Global.LANG] + " - " + info.strDescriptionArray[0]);
			}
			if (mSearchByIdx == SEARCH_BY_PRODUCT_GROUP) {
				holder.txtProductGroup.setText(TextUtils.concat(Str.ProductGroup[Global.LANG], " - ", Utils.setKeyword(info.strDescriptionArray[1], mStrKeyword)));
			} else {
				holder.txtProductGroup.setText(Str.ProductGroup[Global.LANG] + " - " + info.strDescriptionArray[1]);
			}
			if (mSearchByIdx == SEARCH_BY_PRODUCT_LINE) {
				holder.txtProductLine.setText(TextUtils.concat(Str.ProductLine[Global.LANG], " - ", Utils.setKeyword(info.strDescriptionArray[2], mStrKeyword)));
			} else {
				holder.txtProductLine.setText(Str.ProductLine[Global.LANG] + " - " + info.strDescriptionArray[2]);
			}
			if (mSearchByIdx == SEARCH_BY_BRAND) {
				holder.txtBrand.setText(TextUtils.concat(Str.Brand[Global.LANG], " - ", Utils.setKeyword(info.strDescriptionArray[3], mStrKeyword)));
			} else {
				holder.txtBrand.setText(Str.Brand[Global.LANG] + " - " + info.strDescriptionArray[3]);
			}
			if (mSearchByIdx == SEARCH_BY_SUB_BRAND) {
				holder.txtSubBrand.setText(TextUtils.concat(Str.SubBrand[Global.LANG], " - ", Utils.setKeyword(info.strDescriptionArray[4], mStrKeyword)));
			} else {
				holder.txtSubBrand.setText(Str.SubBrand[Global.LANG] + " - " + info.strDescriptionArray[4]);
			}

			UInvenDInfo uInvenDInfo = inventoryMap.get(info.strItemNo);
			if(uInvenDInfo != null)
			{
				if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
				{
					if(uInvenDInfo.dStock > 0)
					{
						holder.txtStock.setText(String.format(Locale.ENGLISH, "%.2f", uInvenDInfo.dStock));
					}
					else
					{
						holder.txtStock.setText("");
					}
				}
				else
				{
					if(bShowCase)
					{
						if (Utils.isContainerItem(mContext, info.strItemNo)) {
							holder.txtStock.setText(String.format(Locale.ENGLISH, "%d", uInvenDInfo.nStock));
						}
						else {
							int nCase = getCaseValue(info.strItemNo);
							int nCaseValue = uInvenDInfo.nStock / nCase;
							int nQty = uInvenDInfo.nStock % nCase;
							holder.txtStock.setText(String.format(Locale.ENGLISH, "%d/%d", nCaseValue, nQty));
						}
					}
					else
					{
						int nQty = uInvenDInfo.nStock;
						holder.txtStock.setText(String.format(Locale.ENGLISH, "%d",nQty));
					}
				}
			}
			else
			{
				holder.txtStock.setText("");
			}
			holder.btnAdd.setOnClickListener(v -> doAdd(info.strItemNo));
			view.setOnLongClickListener(view1 -> {
				doShowAvailable(info.strItemNo);
				return true;
			});
			return view;
		}
	}

	public class GridAdapter extends BaseAdapter{

		List<ProdDetlInfo> list = new ArrayList<>();

		private class ViewHolder {
			public TextView txtDescription, txtItemType, txtProductGroup, txtProductLine, txtBrand, txtSubBrand, txtStock;
			public ImageView imgProduct;
			public Button btnAdd;
		}

		@Override
		public int getCount() {
			return list.size();
		}

		public void setList(List<ProdDetlInfo> list)
		{
			if(list != null)
			{
				this.list.clear();;
				this.list.addAll(list);
			}
		}

		@Override
		public ProdDetlInfo getItem(int position) {
			if(position < list.size())
			{
				return list.get(position);
			}
			else
			{
				return null;
			}
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			final ViewHolder holder;
			final ProdDetlInfo info = this.getItem(position);
			if(info == null) return null;

			if (convertView == null) {
				view = mContext.getLayoutInflater().inflate(R.layout.panel_search_product, parent, false);
				holder = new ViewHolder();
				holder.imgProduct = (ImageView) view.findViewById(R.id.imgProduct);
				holder.txtDescription = (TextView) view.findViewById(R.id.txtDescription);
				holder.txtItemType = (TextView) view.findViewById(R.id.txtItemType);
				holder.txtProductGroup = (TextView) view.findViewById(R.id.txtProductGroup);
				holder.txtProductLine = (TextView) view.findViewById(R.id.txtProductLine);
				holder.txtBrand = (TextView) view.findViewById(R.id.txtBrand);
				holder.txtSubBrand = (TextView) view.findViewById(R.id.txtSubBrand);
				holder.txtStock = (TextView) view.findViewById(R.id.txtStock);
				holder.btnAdd = (Button) view.findViewById(R.id.btnAdd);
				view.setTag(holder);
			} else {
				holder = (ViewHolder) view.getTag();
			}

			// update product image view
			if (info.strImageUrl == null || info.strImageUrl.isEmpty()) {
				Picasso
						.get()
						.load(R.drawable.no_image)
						.placeholder(mContext.getResources().getDrawable(R.drawable.no_image))
						.into(holder.imgProduct);
			} else {
				Picasso
						.get()
						.load(info.strImageUrl)
						.placeholder(mContext.getResources().getDrawable(R.drawable.no_image))
						.into(holder.imgProduct);
			}

			holder.btnAdd.setText(Str.Add[Global.LANG]);
			if (mSearchByIdx == SEARCH_BY_PRODUCT_NAME) {
				holder.txtDescription.setText(Utils.setKeyword(info.strDescription, mStrKeyword));
			} else {
				holder.txtDescription.setText(info.strDescription);
			}
			if (mSearchByIdx == SEARCH_BY_ITEM_TYPE) {
				holder.txtItemType.setText(TextUtils.concat(Str.ItemType[Global.LANG], " - ", Utils.setKeyword(info.strDescriptionArray[0], mStrKeyword)));
			} else {
				holder.txtItemType.setText(Str.ItemType[Global.LANG] + " - " + info.strDescriptionArray[0]);
			}
			if (mSearchByIdx == SEARCH_BY_PRODUCT_GROUP) {
				holder.txtProductGroup.setText(TextUtils.concat(Str.ProductGroup[Global.LANG], " - ", Utils.setKeyword(info.strDescriptionArray[1], mStrKeyword)));
			} else {
				holder.txtProductGroup.setText(Str.ProductGroup[Global.LANG] + " - " + info.strDescriptionArray[1]);
			}
			if (mSearchByIdx == SEARCH_BY_PRODUCT_LINE) {
				holder.txtProductLine.setText(TextUtils.concat(Str.ProductLine[Global.LANG], " - ", Utils.setKeyword(info.strDescriptionArray[2], mStrKeyword)));
			} else {
				holder.txtProductLine.setText(Str.ProductLine[Global.LANG] + " - " + info.strDescriptionArray[2]);
			}
			if (mSearchByIdx == SEARCH_BY_BRAND) {
				holder.txtBrand.setText(TextUtils.concat(Str.Brand[Global.LANG], " - ", Utils.setKeyword(info.strDescriptionArray[3], mStrKeyword)));
			} else {
				holder.txtBrand.setText(Str.Brand[Global.LANG] + " - " + info.strDescriptionArray[3]);
			}
			if (mSearchByIdx == SEARCH_BY_SUB_BRAND) {
				holder.txtSubBrand.setText(TextUtils.concat(Str.SubBrand[Global.LANG], " - ", Utils.setKeyword(info.strDescriptionArray[4], mStrKeyword)));
			} else {
				holder.txtSubBrand.setText(Str.SubBrand[Global.LANG] + " - " + info.strDescriptionArray[4]);
			}

			UInvenDInfo uInvenDInfo = inventoryMap.get(info.strItemNo);
			if(uInvenDInfo != null)
			{
				if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
				{
					if(uInvenDInfo.dStock > 0)
					{
						holder.txtStock.setText(String.format(Locale.ENGLISH, "%.2f", uInvenDInfo.dStock));
					}
					else
					{
						holder.txtStock.setText("");
					}
				}
				else
				{
					if(bShowCase)
					{
						if (Utils.isContainerItem(mContext, info.strItemNo)) {
							holder.txtStock.setText(String.format(Locale.ENGLISH, "%d", uInvenDInfo.nStock));
						}
						else {
							int nCase = getCaseValue(info.strItemNo);
							int nCaseValue = uInvenDInfo.nStock / nCase;
							int nQty = uInvenDInfo.nStock % nCase;
							holder.txtStock.setText(String.format(Locale.ENGLISH, "%d/%d", nCaseValue, nQty));
						}
					}
					else
					{
						int nQty = uInvenDInfo.nStock;
						holder.txtStock.setText(String.format(Locale.ENGLISH, "%d",nQty));
					}
				}
			}
			else
			{
				holder.txtStock.setText("");
			}
			holder.btnAdd.setOnClickListener(v -> doAdd(info.strItemNo));
			view.setOnLongClickListener(view1 -> {
				doShowAvailable(info.strItemNo);
				return true;
			});
			return view;
		}
	}

	Map<String, Integer> mPresoldMap = new HashMap<>();
	private int getPresoldValue(String strItemNo)
	{
		if( mPresoldMap.get(strItemNo) != null )
			return mPresoldMap.get(strItemNo);
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
		mPresoldMap.put(strItemNo, nStock);
		return nStock;
	}

	private void doShowAvailable(String strItemNo)
	{
		int nInvenType = Utils.getIntSetting(mContext, Global.PREF_INVENCKECK);
		if(nInvenType == 3 || nInvenType == 4 )
		{
			int nStock = getStockValue(strItemNo);
			int nPresoldStock = getPresoldValue(strItemNo);
			nStock -= nPresoldStock;
			String strCaption = "";
			if (Utils.isContainerItem(mContext, strItemNo)) {
				strCaption = String.format(Locale.ENGLISH, "Available for upsell %d", nStock);
			}
			else {
				int nCase = getCaseValue(strItemNo);
				strCaption = String.format(Locale.ENGLISH, "Available for upsell %d / %d", nStock / nCase, nStock % nCase);
			}

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
}