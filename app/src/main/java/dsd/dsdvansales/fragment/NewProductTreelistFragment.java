package dsd.dsdvansales.fragment;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collection;
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
import dsd.dsdvansales.model.CustomerOrderInfo;
import dsd.dsdvansales.model.OrderLineInfo;
import dsd.dsdvansales.model.PresoldOrDInfo;
import dsd.dsdvansales.model.ProdDetlInfo;
import dsd.dsdvansales.model.ProdStruInfo;
import dsd.dsdvansales.model.UInvenDInfo;
import dsd.dsdvansales.model.UInvenHInfo;
import dsd.dsdvansales.views.treeview.AbstractTreeViewAdapter;
import dsd.dsdvansales.views.treeview.InMemoryTreeStateManager;
import dsd.dsdvansales.views.treeview.TreeBuilder;
import dsd.dsdvansales.views.treeview.TreeNodeInfo;
import dsd.dsdvansales.views.treeview.TreeStateManager;
import dsd.dsdvansales.views.treeview.TreeViewList;

public class NewProductTreelistFragment extends BaseFragment implements View.OnClickListener {

	private static final int	PRODUCT_TREE_LEVEL	= 10;
	private List<List<ProdStruInfo>> mProdStruList = new ArrayList<>();
	private List<Integer> mNodeList = new ArrayList<>();
	private Map<String, TreeItem> mProdMap = new HashMap<String, TreeItem>();
	private UInvenHInfo mUInvenHinfo;
	private Map<String, ProdDetlInfo> mProdDetlMap = new HashMap<>();

	private  boolean enableFilterAuthorizedItem = false;
	private List<CustomerOrderInfo> latestOrderInfo = new ArrayList<>();
	private AuthHeaderInfo mAuthHeaderInfo;
	private List<AuthDetInfo> mAuthDetlInfoList;
	private Map<String, AuthDetInfo> mAuthItemMap;
	private BaseFragment from;
	private boolean bImageTree = false;
	private boolean bShowCase = true;

	private Map<String, UInvenDInfo> inventoryMap = new HashMap<>();

	public BaseFragment setFrom(BaseFragment from)
	{
		this.from = from;
		return this;
	}
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
		mRootView = inflater.inflate(R.layout.fragment_product_treelist, container, false);
		commonInit();
		return mRootView;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		Global.gFragIdx = Global.FRAG_PRODUCT_TREELIST;
	}

	@Override
	public void initView() {
		bImageTree = Utils.getStringSetting(mContext, Global.PREF_TREE_AS_IMAGE).equals("1");
		bShowCase = Utils.getStringSetting(mContext, Global.PREF_INVENTORYUOM).equals("U") == false;
		mRootView.findViewById(R.id.btnBack).setOnClickListener(this);
		refreshLanguage();
	}
	
	public class TreeItem
	{
		public int nDepth = 0;
		public TreeItem parentNode = null;
		public List<TreeItem> childNode = null;
		public boolean bProductNode = false;
		public List<ProdStruInfo> data = new ArrayList<>();
		public boolean bValid = true;
	}

	void BuildNodeList(TreeItem item)
	{
		if( item == null )
		{
			return;
		}

		int nPos = mNodeList.size();
		if( item.childNode != null && item.childNode.size() > 0)
		{
			List<TreeItem> delChild = new ArrayList<>();

			for(TreeItem child : item.childNode)
			{
				BuildNodeList(child);
				if(!child.bValid)
				{
					delChild.add(child);
				}
			}
			for(TreeItem child : delChild)
			{
				item.childNode.remove(child);
			}

			if(item.childNode.size() > 0)
			{
				mNodeList.add(nPos, item.nDepth);
				mProdStruList.add(nPos, item.data);
			}
			else
			{
				item.bValid = false;
			}
		}
		else
		{
			mNodeList.add(nPos, item.nDepth);
			mProdStruList.add(nPos, item.data);
		}

	}

	private void arrangeTree(TreeItem item) {
		if( item == null )
		{
			return;
		}

		if( item.childNode != null && item.childNode.size() > 0)
		{
			List<TreeItem> delChild = new ArrayList<>();
			Collections.sort(item.childNode, new Comparator<TreeItem>() {
				@Override
				public int compare(TreeItem treeItem, TreeItem t1) {
					if(Utils.getStringSetting(mContext, Global.PREF_TREE_ORDER).equals("DESC"))
					{
						String strDesc = "";
						if (!treeItem.data.isEmpty()) {
							strDesc = treeItem.data.get(0).strDescription;
						}
						String strDesc1 = "";
						if (!t1.data.isEmpty()) {
							strDesc1 = t1.data.get(0).strDescription;
						}
						return strDesc.compareTo(strDesc1);
					}
					else
					{
						String strEntryID = "";
						if (!treeItem.data.isEmpty()) {
							strEntryID = treeItem.data.get(0).strEntryID;
						}
						String strEntryID1 = "";
						if (!t1.data.isEmpty()) {
							strEntryID1 = t1.data.get(0).strEntryID;
						}
						return strEntryID.compareTo(strEntryID1);
					}
				}
			});
			for(TreeItem child : item.childNode)
			{
				arrangeTree(child);
				if(!child.bValid)
				{
					delChild.add(child);
				}
			}
			for(TreeItem child : delChild)
			{
				item.childNode.remove(child);
			}

			if(item.childNode.size() > 0)
			{
			}
			else
			{
				item.bValid = false;
			}
		}
	}

	private void rebuildImageTree(TreeItem item) {
		if( item == null ) {
			return;
		}

		if( item.childNode != null && !item.childNode.isEmpty()) {
			// merge each 2 children to 1
			int childIndex = 0;
			int indexInPair = 0;
			TreeItem firstChildItem = null;
			while (childIndex < item.childNode.size()) {
				TreeItem childItem = item.childNode.get(childIndex);
				if (!childItem.bProductNode) {
					childIndex += 1;
					continue;
				}

				if (indexInPair == 0) {
					firstChildItem = childItem;
					childIndex += 1;
					indexInPair = (indexInPair+1) % 2;
				} else {
					firstChildItem.data.add(childItem.data.get(0));
					item.childNode.remove(childIndex);
					indexInPair = (indexInPair+1) % 2;
				}
			}

			// recursive call
			for (TreeItem childItem: item.childNode) {
				rebuildImageTree(childItem);
			}
		}
		else {
			return;
		}
	}

	@Override
	public void loadData() {
		mProdStruList.clear();
		mNodeList.clear();
		mProdMap.clear();
		mProdDetlMap.clear();
		
		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();

		dbAdapter.getProdDetlList(mProdDetlMap, null);

		inventoryMap.clear();
		List<UInvenDInfo> uInvenDList = dbAdapter.getUInvenDList(Global.gAdjustinfo.nTrxnNo);
		for (UInvenDInfo uInvenDInfo : uInvenDList) {
			if(uInvenDInfo.nStock > 0 || uInvenDInfo.dStock > 0)
			{
				inventoryMap.put(uInvenDInfo.strItemNo, uInvenDInfo);
			}
		}

		latestOrderInfo.clear();
		latestOrderInfo.addAll(dbAdapter.getAllCustomerLatestOrderList());
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

		List<ProdStruInfo> prodStruList = dbAdapter.getProdStruList();
		mUInvenHinfo = dbAdapter.getUInvenHInfo(Utils.getLongSetting(mContext, Global.PREF_UINVEN_TRXN_NO));
		if (mUInvenHinfo == null) {
			mUInvenHinfo = new UInvenHInfo();
		}
		mUInvenHinfo.uInvenDList = dbAdapter.getUInvenDList(Utils.getLongSetting(mContext, Global.PREF_UINVEN_TRXN_NO));
		dbAdapter.close();
		TreeItem rootItem = null;

		Map<String, ProdStruInfo> prodStruInfoMap = new HashMap<>();
		for(ProdStruInfo info : prodStruList)
		{
			prodStruInfoMap.put(info.strEntryID, info);
		}

		Map<String, ProdStruInfo> builtStruInfoMap = new HashMap<>();
		List<ProdStruInfo> prodStruInfos = new ArrayList<>();
		for(ProdStruInfo info : prodStruList)
		{
			if(builtStruInfoMap.get(info.strEntryID) != null) continue;
			if(builtStruInfoMap.get(info.strParentID) != null)
			{
				prodStruInfos.add(info);
				builtStruInfoMap.put(info.strEntryID, info);
			}
			else
			{
				//Add All Parents ID
				ProdStruInfo curInfo = info;
				int nIdx = prodStruInfos.size();
				while(builtStruInfoMap.get(curInfo.strParentID) == null)
				{
					ProdStruInfo parentInfo = prodStruInfoMap.get(curInfo.strParentID);
					if(parentInfo == null) break;
					prodStruInfos.add(nIdx, parentInfo);
					builtStruInfoMap.put(parentInfo.strEntryID, parentInfo);
					curInfo = parentInfo;
				}
				prodStruInfos.add(info);
				builtStruInfoMap.put(info.strEntryID, info);
			}
		}

		for (ProdStruInfo info : prodStruInfos)
		{
			TreeItem item = new TreeItem();
			item.data.add(info);
			if( info.strEntryID.equals("0") || info.strEntryID.equals(""))
			{
				item.nDepth = 0;
				item.parentNode = null;
				item.bProductNode = false;
				item.childNode = new ArrayList<>();
				rootItem = item;
			}
			else
			{
				TreeItem parentItem = mProdMap.get(info.strParentID);
				if( parentItem != null )
				{
					item.nDepth = parentItem.nDepth + 1;
					item.parentNode = parentItem;

					if(info.strObjectType.equals(""))
					{
						item.bProductNode = false;
						item.childNode = new ArrayList<>();
					}
					else
					{
						item.bProductNode = true;
					}
					if(enableFilterAuthorizedItem && item.bProductNode)
					{
						if(mAuthHeaderInfo != null)
						{
							if (!item.data.isEmpty()) {
								if(isAuthorizedItem(item.data.get(0).strReference))
								{
									item.parentNode.childNode.add(item);
								}
							}
							else {
								item.parentNode.childNode.add(item);
							}
						}
						else
						{
							item.parentNode.childNode.add(item);
						}
					}
					else
					{
						item.parentNode.childNode.add(item);
					}
				}
				else
				{
					item.nDepth = 0;
					item.parentNode = null;
					item.bProductNode = false;	
					item.childNode = new ArrayList<>();
					rootItem = item;
				}
			}
			if(enableFilterAuthorizedItem && item.bProductNode)
			{
				if(mAuthHeaderInfo != null)
				{
					if (!item.data.isEmpty()) {
						if(isAuthorizedItem(item.data.get(0).strReference))
						{
							mProdMap.put(info.strEntryID, item);
						}
					}
					else {
						mProdMap.put(info.strEntryID, item);
					}
				}
				else
				{
					mProdMap.put(info.strEntryID, item);
				}
			}
			else
			{
				mProdMap.put(info.strEntryID, item);
			}
		}
		if( rootItem == null )
		{
			rootItem = mProdMap.get("1");
		}

		// rebuild 2-pair tree items for product items
		arrangeTree(rootItem);

		if (bImageTree) {
			rebuildImageTree(rootItem);
		}

		if( rootItem != null )
		{
			BuildNodeList(rootItem);
		}

		
/*		for (ProdStruInfo info : prodStruList) {
			if (info.nParentID == 0) {
				mProdStruList.add(info);
				mNodeList.add(0);
			} else if (info.nParentID == 1) {
				mProdStruList.add(info);
				mNodeList.add(1);
				if (info.nGroupNo == 0) {
					ProdStruInfo nullInfo = new ProdStruInfo();
					nullInfo.nParentID = 2;
					nullInfo.strDescription = Str.NoProductFound[Global.LANG].toUpperCase();
					mProdStruList.add(nullInfo);
					mNodeList.add(2);
				} else {
					for (ProdStruInfo subInfo : prodStruList) {
						if (subInfo.nParentID == info.nEntryID) {
							subInfo.strDescription = subInfo.strReference + " " + subInfo.strShortDesc;
							mProdStruList.add(subInfo);
							mNodeList.add(2);
						}
					}
				}
			}
		}*/
		refreshPage();
	}

	@Override
	public void refreshPage() {
		TreeStateManager<Long> treeStateManager = new InMemoryTreeStateManager<>();
		final TreeBuilder<Long> treeBuilder = new TreeBuilder<>(treeStateManager);

		boolean bNeedFillExpandedList = false;
		if (Global.gProductTreeExpandedList == null) {
			Global.gProductTreeExpandedList = new ArrayList<>();
			bNeedFillExpandedList = true;
		}
		for (int i = 0; i < mNodeList.size(); i++) {
			treeBuilder.sequentiallyAddNextNode((long) i, mNodeList.get(i));
			if (bNeedFillExpandedList == true) {
				Global.gProductTreeExpandedList.add(false);
			}
		}
		TreeViewList treeViewList = (TreeViewList) mRootView.findViewById(R.id.treeViewList);
		SimpleStandardAdapter simpleAdapter = new SimpleStandardAdapter(treeStateManager, PRODUCT_TREE_LEVEL);
		treeViewList.setAdapter(simpleAdapter);
		treeViewList.setCollapsible(true);
		registerForContextMenu(treeViewList);

		treeStateManager.collapseChildren(null);
		for (int i = 0; i < Global.gProductTreeExpandedList.size(); i++) {
			if (Global.gProductTreeExpandedList.get(i) == true) {
				if(treeStateManager.isInTree((long) i))
				{
					treeStateManager.expandDirectChildren((long) i);
				}
			}
		}
		if(treeStateManager.isInTree((long) 0))
		{
			treeStateManager.expandDirectChildren((long) 0);
		}
	}

	@Override
	public void refreshLanguage() {
		((TextView) mRootView.findViewById(R.id.lblTitle)).setText(Str.ProductList[Global.LANG].toUpperCase());
		loadData();
	}

	@Override
	public void receiveBroadcast(String strAction, Intent intent) {
	}

	private void selectProduct(String strItemNo) {
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
		}
		m_bClicked = false;
	}

	private class SimpleStandardAdapter extends AbstractTreeViewAdapter<Long> {

		private class ViewHolder {
			public CardView cardView1;
			public TextView txtDescription1, txtStock1;
			public ImageView imgProduct1;
			public Button btnAdd1;
			public CardView cardView2;
			public TextView txtDescription2, txtStock2;
			public ImageView imgProduct2;
			public Button btnAdd2;
		}

		public SimpleStandardAdapter(final TreeStateManager<Long> treeStateManager, final int numberOfLevels) {
			super(mContext, treeStateManager, numberOfLevels);
		}

		private String getDescription(final long id, int pairIndex) {
			try
			{
				List<ProdStruInfo> infoList = mProdStruList.get((int)id);
				ProdStruInfo info = infoList.get(pairIndex);
				if(!info.strObjectType.equals(""))
				{
					if(Utils.getStringSetting(mContext, Global.PREF_TREE_DESC).equals("1"))
					{
						return info.strFullDescription;
					}
					else if(Utils.getStringSetting(mContext, Global.PREF_TREE_DESC).equals("2"))
					{
						float nPrice = 0;
						if(from != null)
						{
							nPrice = from.getProductPrice(info.strReference);
						}
						else
						{
							nPrice = getProductPrice(info.strReference);
						}
						return info.strShortDesc + " " + String.format(Locale.ENGLISH, "%s" + "%." + Utils.getIntSetting(mContext, Global.PREF_DECIMAL) + "f", Global.gCurrencySymbol, nPrice);
					}
					else
					{
						return Utils.getDisplayItemCode(mContext, info.strReference) + " " + info.strShortDesc;
					}
				}
				else
				{
					return info.strDescription;
				}

			}catch(Exception e)
			{
				e.printStackTrace();
				return "";
			}
		}

		private String getReference(final long id) {
			try
			{
				List<ProdStruInfo> infoList = mProdStruList.get((int)id);
				return infoList.get(0).strReference;
			}catch(Exception e)
			{
				e.printStackTrace();
				return "";
			}
		}

		private String getParentID(final long id) {
			try
			{
				List<ProdStruInfo> infoList = mProdStruList.get((int)id);
				return infoList.get(0).strParentID;
			}catch(Exception e)
			{
				e.printStackTrace();
				return "0";
			}
		}

		@Override
		public View getNewChildView(final TreeNodeInfo<Long> treeNodeInfo) {
			Log.i("NewProduct", "getNewChildView");
			long nodeId = treeNodeInfo.getId();
			final TreeNodeInfo<Long> info = getManager().getNodeInfo(nodeId);
			if (info.isWithChildren()) {
				final LinearLayout viewLayout = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.row_product_treelist, null);
				return updateView(viewLayout, treeNodeInfo);
			} else {
				if (bImageTree) {
					final LinearLayout viewLayout = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.row_product_imagetreelist, null);
					return updateView(viewLayout, treeNodeInfo);
				} else {
					final LinearLayout viewLayout = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.row_product_treelist, null);
					return updateView(viewLayout, treeNodeInfo);
				}
			}
		}

		@Override
		public LinearLayout updateView(final View view, final TreeNodeInfo<Long> treeNodeInfo) {
			Log.i("NewProduct", "updateView");

			LinearLayout viewLayout = (LinearLayout) view;
			long nodeId = treeNodeInfo.getId();
			final TreeNodeInfo<Long> info = getManager().getNodeInfo(nodeId);
			if (!info.isWithChildren() && bImageTree) {
				ViewHolder holder = null;
				try {
					holder = (ViewHolder)view.getTag();
				}
				catch (Exception ex) {}

				if (holder == null) {
					holder = new ViewHolder();
					holder.cardView1 = (CardView) viewLayout.findViewById(R.id.cardView1);
					holder.txtDescription1 = (TextView) viewLayout.findViewById(R.id.txtDescription1);
					holder.imgProduct1 = (ImageView) viewLayout.findViewById(R.id.imgProduct1);
					holder.btnAdd1 = (Button) viewLayout.findViewById(R.id.btnAdd1);
					holder.txtStock1 = (TextView) viewLayout.findViewById(R.id.txtStock1);
					holder.cardView2 = (CardView) viewLayout.findViewById(R.id.cardView2);
					holder.txtDescription2 = (TextView) viewLayout.findViewById(R.id.txtDescription2);
					holder.imgProduct2 = (ImageView) viewLayout.findViewById(R.id.imgProduct2);
					holder.btnAdd2 = (Button) viewLayout.findViewById(R.id.btnAdd2);
					holder.txtStock2 = (TextView) viewLayout.findViewById(R.id.txtStock2);
					view.setTag(holder);
				}
				else {
					int kk = 0;
				}

				// populate first view
				// description
				SpannableString spanString1 = null;
				try
				{
					spanString1 = new SpannableString(getDescription(treeNodeInfo.getId(), 0));
					holder.txtDescription1.setText(spanString1);
				}catch(Exception e)
				{
					e.printStackTrace();
				}

				// add button
				holder.btnAdd1.setText(Str.Add[Global.LANG]);

				// image
				List<ProdStruInfo> infoList = mProdStruList.get((int)nodeId);
				ProdStruInfo info1 = infoList.get(0);
				String strItemNo = info1.strReference;
				ProdDetlInfo prodDetlInfo = mProdDetlMap.get(strItemNo);
				String strImageUrl1 = null;
				if (prodDetlInfo != null) {
					strImageUrl1 = prodDetlInfo.strImageUrl;
				}

				if (strImageUrl1 == null || strImageUrl1.isEmpty()) {
					Picasso
							.get()
							.load(R.drawable.no_image)
							.placeholder(mContext.getResources().getDrawable(R.drawable.no_image))
							.into(holder.imgProduct1);
				} else {
					Picasso
							.get()
							.load(strImageUrl1)
							.placeholder(mContext.getResources().getDrawable(R.drawable.no_image))
							.into(holder.imgProduct1);
				}

				// stock
				holder.txtStock1.setText("");
				UInvenDInfo uInvenDInfo = inventoryMap.get(strItemNo);
				if (uInvenDInfo != null)
				{
					if(Utils.getProdLoconSCWType(mContext, strItemNo) == Global.WEIGHT_ITEM)
					{
						if(uInvenDInfo.dStock > 0)
						{
							holder.txtStock1.setText(String.format(Locale.ENGLISH, "%.2f", uInvenDInfo.dStock));
						}
						else
						{
							holder.txtStock1.setText("");
						}
					}
					else {
						if(bShowCase)
						{
							if (Utils.isContainerItem(mContext, strItemNo)) {
								holder.txtStock1.setText(String.format(Locale.ENGLISH, "%d", uInvenDInfo.nStock));
							}
							else {
								int nCase = getCaseValue(strItemNo);
								int nCaseValue = uInvenDInfo.nStock / nCase;
								int nQty = uInvenDInfo.nStock % nCase;
								holder.txtStock1.setText(String.format(Locale.ENGLISH, "%d/%d", nCaseValue, nQty));
							}
						}
						else
						{
							int nQty = uInvenDInfo.nStock;
							holder.txtStock1.setText(String.format(Locale.ENGLISH, "%d",nQty));
						}
					}
				}

				String finalStrItemNo = strItemNo;
				holder.btnAdd1.setOnClickListener(v -> selectProduct(finalStrItemNo));
				holder.cardView1.setOnLongClickListener(view1 -> {
					doShowAvailable(finalStrItemNo);
					return true;
				});

				// populate second view
				if (infoList.size() > 1) {
					holder.cardView2.setVisibility(View.VISIBLE);

					// description
					SpannableString spanString2 = null;
					try
					{
						spanString2 = new SpannableString(getDescription(treeNodeInfo.getId(), 1));
						holder.txtDescription2.setText(spanString2);
					}catch(Exception e)
					{
						e.printStackTrace();
					}

					// add button
					holder.btnAdd2.setText(Str.Add[Global.LANG]);

					// image
					ProdStruInfo info2 = infoList.get(1);
					strItemNo = info2.strReference;
					prodDetlInfo = mProdDetlMap.get(strItemNo);
					String strImageUrl2 = null;
					if (prodDetlInfo != null) {
						strImageUrl2 = prodDetlInfo.strImageUrl;
					}
					if (strImageUrl2 == null || strImageUrl2.isEmpty()) {
						Picasso
								.get()
								.load(R.drawable.no_image)
								.placeholder(mContext.getResources().getDrawable(R.drawable.no_image))
								.into(holder.imgProduct2);
					} else {
						Picasso
								.get()
								.load(strImageUrl2)
								.placeholder(mContext.getResources().getDrawable(R.drawable.no_image))
								.into(holder.imgProduct2);
					}

					// stock count
					uInvenDInfo = inventoryMap.get(strItemNo);
					holder.txtStock2.setText("");
					if(uInvenDInfo != null)
					{
						if(Utils.getProdLoconSCWType(mContext, strItemNo) == Global.WEIGHT_ITEM)
						{
							if(uInvenDInfo.dStock > 0)
							{
								holder.txtStock2.setText(String.format(Locale.ENGLISH, "%.2f", uInvenDInfo.dStock));
							}
							else
							{
								holder.txtStock2.setText("");
							}
						}
						else {
							if(bShowCase)
							{
								if (Utils.isContainerItem(mContext, strItemNo)) {
									holder.txtStock2.setText(String.format(Locale.ENGLISH, "%d", uInvenDInfo.nStock));
								}
								else {
									int nCase = getCaseValue(strItemNo);
									int nCaseValue = uInvenDInfo.nStock / nCase;
									int nQty = uInvenDInfo.nStock % nCase;
									holder.txtStock2.setText(String.format(Locale.ENGLISH, "%d/%d", nCaseValue, nQty));
								}
							}
							else
							{
								int nQty = uInvenDInfo.nStock;
								holder.txtStock2.setText(String.format(Locale.ENGLISH, "%d",nQty));
							}
						}
					}

					String finalStrItemNo1 = strItemNo;
					holder.btnAdd2.setOnClickListener(v -> selectProduct(finalStrItemNo1));
					holder.cardView2.setOnLongClickListener(view1 -> {
						doShowAvailable(finalStrItemNo1);
						return true;
					});
				}
				else {
					holder.cardView2.setVisibility(View.INVISIBLE);
				}
				return viewLayout;
			}
			else {
				TextView descriptionView = (TextView) viewLayout.findViewById(R.id.txtProductName);
				TextView levelView = (TextView) viewLayout.findViewById(R.id.txtLevel);

				SpannableString spanString = null;
				try
				{
					spanString = new SpannableString(getDescription(treeNodeInfo.getId(), 0));
					if (getParentID(treeNodeInfo.getId()).equals("0")) {
						spanString.setSpan(new StyleSpan(Typeface.BOLD), 0, spanString.length(), 0);
					}
					descriptionView.setText(spanString);
					levelView.setText(Integer.toString(treeNodeInfo.getLevel()));
				}catch(Exception e)
				{
					e.printStackTrace();
				}

				return viewLayout;
			}
		}

		@Override
		public void handleItemClick(final View view, final Object id) {
			final Long longId = (Long) id;
			final TreeNodeInfo<Long> info = getManager().getNodeInfo(longId);
			if (info.isWithChildren()) {
				int intId = longId.intValue();
				if (Global.gProductTreeExpandedList != null && intId >= 0 && intId < Global.gProductTreeExpandedList.size()) {
					Global.gProductTreeExpandedList.set(intId, !info.isExpanded());
				}
				super.handleItemClick(view, id);
			} else {
				if (bImageTree) {
					super.handleItemClick(view, id);
				}
				else {
					String strReference = getReference(longId);
					if (strReference.length() > 0) {
						selectProduct(strReference);
					}
				}
			}
		}

		@Override
		public void handleLongItemClick(View view, Object id) {
			final Long longId = (Long) id;
			final TreeNodeInfo<Long> info = getManager().getNodeInfo(longId);
			if (!info.isWithChildren()) {
				if (bImageTree) {
					super.handleLongItemClick(view, id);
				}
				else {
					String strItemNo = getReference(longId);
					doShowAvailable(strItemNo);
				}
			}
		}

		@Override
		public long getItemId(final int position) {
			try{
				return getTreeId(position);
			}catch(Exception e)
			{
				e.printStackTrace();
				return 0;
			}
			
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

		mPresoldMap.put(strItemNo, nStock);
		return nStock;
	}

	Map<String, Double> mPresoldMapD = new HashMap<>();
	private double getPresoldValueD(String strItemNo)
	{
		if( mPresoldMapD.get(strItemNo) != null )
			return mPresoldMapD.get(strItemNo);
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
		mPresoldMapD.put(strItemNo, dStock);
		return dStock;
	}

	private void doShowAvailable(String strItemNo)
	{
		int nInvenType = Utils.getIntSetting(mContext, Global.PREF_INVENCKECK);
		if(nInvenType == 3 || nInvenType == 4 )
		{
			if(Utils.getProdLoconSCWType(mContext, strItemNo) == Global.WEIGHT_ITEM)
			{
				double dStock = getStockValueD(strItemNo);
				double dPresoldStock = getPresoldValueD(strItemNo);
				dStock -= dPresoldStock;
				int nCase = getCaseValue(strItemNo);
				String strCaption = String.format(Locale.ENGLISH, "Available for upsell %.2f", dStock);
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
				int nStock = getStockValue(strItemNo);
				int nPresoldStock = getPresoldValue(strItemNo);
				nStock -= nPresoldStock;
				int nCase = getCaseValue(strItemNo);
				String strCaption = "";
				if (Utils.isContainerItem(mContext, strItemNo)) {
					strCaption = String.format(Locale.ENGLISH, "Available for upsell %d", nStock);
				}
				else {
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
				if( info.strItemNo.equals(strItemNo) )
					dStock += info.dStock;
			}
			mStockMapD.put(strItemNo, dStock);
			return dStock;
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