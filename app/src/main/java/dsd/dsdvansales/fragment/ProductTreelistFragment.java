package dsd.dsdvansales.fragment;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import dsd.dsdvansales.R;
import dsd.dsdvansales.db.DbAdapter;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.model.ProdStruInfo;
import dsd.dsdvansales.views.treeview.AbstractTreeViewAdapter;
import dsd.dsdvansales.views.treeview.InMemoryTreeStateManager;
import dsd.dsdvansales.views.treeview.TreeBuilder;
import dsd.dsdvansales.views.treeview.TreeNodeInfo;
import dsd.dsdvansales.views.treeview.TreeStateManager;
import dsd.dsdvansales.views.treeview.TreeViewList;

public class ProductTreelistFragment extends BaseFragment implements View.OnClickListener {

	private static final int	PRODUCT_TREE_LEVEL	= 3;
	private List<ProdStruInfo> mProdStruList = new ArrayList<>();
	private List<Integer> mNodeList = new ArrayList<>();

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
		mRootView.findViewById(R.id.btnBack).setOnClickListener(this);
		refreshLanguage();
	}

	@Override
	public void loadData() {
		mProdStruList.clear();
		mNodeList.clear();
		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		List<ProdStruInfo> prodStruList = dbAdapter.getProdStruList();
		dbAdapter.close();
		for (ProdStruInfo info : prodStruList) {
			if (info.strParentID.equals("0") || info.strParentID.equals("")) {
				mProdStruList.add(info);
				mNodeList.add(0);
			} else if (info.strParentID.equals("1")) {
				mProdStruList.add(info);
				mNodeList.add(1);
				if (info.nGroupNo == 0) {
					ProdStruInfo nullInfo = new ProdStruInfo();
					nullInfo.strParentID = "2";
					nullInfo.strDescription = Str.NoProductFound[Global.LANG].toUpperCase();
					mProdStruList.add(nullInfo);
					mNodeList.add(2);
				} else {
					for (ProdStruInfo subInfo : prodStruList) {
						if (subInfo.strParentID.equals(info.strEntryID)) {
							subInfo.strDescription = subInfo.strReference + " " + subInfo.strShortDesc;
							mProdStruList.add(subInfo);
							mNodeList.add(2);
						}
					}
				}
			}
		}
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
				treeStateManager.expandDirectChildren((long) i);
			}
		}
		treeStateManager.expandDirectChildren((long) 0);
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

		public SimpleStandardAdapter(final TreeStateManager<Long> treeStateManager, final int numberOfLevels) {
			super(mContext, treeStateManager, numberOfLevels);
		}

		private String getDescription(final long id) {
			return mProdStruList.get((int) id).strDescription;
		}

		private String getReference(final long id) {
			return mProdStruList.get((int) id).strReference;
		}

		private String getParentID(final long id) {
			return mProdStruList.get((int) id).strParentID;
		}

		@Override
		public View getNewChildView(final TreeNodeInfo<Long> treeNodeInfo) {
			final LinearLayout viewLayout = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.row_product_treelist, null);
			return updateView(viewLayout, treeNodeInfo);
		}

		@Override
		public LinearLayout updateView(final View view, final TreeNodeInfo<Long> treeNodeInfo) {
			LinearLayout viewLayout = (LinearLayout) view;
			TextView descriptionView = (TextView) viewLayout.findViewById(R.id.txtProductName);
			TextView levelView = (TextView) viewLayout.findViewById(R.id.txtLevel);

			SpannableString spanString = new SpannableString(getDescription(treeNodeInfo.getId()));
			String parentID = getParentID(treeNodeInfo.getId());
			if (parentID.equals("0") || parentID.equals("")) {
				spanString.setSpan(new StyleSpan(Typeface.BOLD), 0, spanString.length(), 0);
			}
			descriptionView.setText(spanString);
			levelView.setText(Integer.toString(treeNodeInfo.getLevel()));
			return viewLayout;
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
				String strReference = getReference(longId);
				if (strReference.length() > 0) {
					selectProduct(strReference);
				}
			}
		}

		@Override
		public long getItemId(final int position) {
			return getTreeId(position);
		}
	}
}