package dsd.dsdvansales.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.measite.minidns.record.A;
import dsd.dsdvansales.R;
import dsd.dsdvansales.db.DbAdapter;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.model.DescTypeInfo;

public class FilterByFragment extends BaseFragment implements View.OnClickListener {

	private EditText mEditKeyword = null;
	private TextView mTxtNoData = null;
	private ListView mListView = null;
	private ListAdapter mListAdapter = null;
	private List<DescTypeInfo> mDescTypeList = new ArrayList<>();
	private List<DescTypeInfo> mFilteredDescTypeList = new ArrayList<>();
	private String mStrKeyword = "";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootView = inflater.inflate(R.layout.fragment_filter_location, container, false);
		commonInit();
		return mRootView;
	}

	
	@Override
	public void onResume() {
		super.onResume();
		Global.gFragIdx = Global.FRAG_FILTER_BY;
	}

	@Override
	public void initView() {
		mRootView.findViewById(R.id.btnSearch).setOnClickListener(this);
		mRootView.findViewById(R.id.btnBack).setOnClickListener(this);
		mRootView.findViewById(R.id.btnClearAllFilters).setOnClickListener(this);

		mTxtNoData = (TextView) mRootView.findViewById(R.id.txtNoData);
		mListView = (ListView) mRootView.findViewById(R.id.listView);
		mListAdapter = new ListAdapter();
		mListView.setAdapter(mListAdapter);

		mEditKeyword = ((EditText) mRootView.findViewById(R.id.editKeyword));
		mTxtNoData = (TextView) mRootView.findViewById(R.id.txtNoData);

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

		loadData();
	}

	@Override
	public void loadData() {
		mFilteredDescTypeList = Global.gCurFilterBy;
		mDescTypeList.clear();
		String strDescTypeID = Utils.getStringSetting(mContext, Global.PREF_STKTKEGRP);
		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		mDescTypeList = dbAdapter.getDescTypeList(strDescTypeID);
		dbAdapter.close();

		doSearch();
		refreshLanguage();
	}

	@Override
	public void refreshPage() {
		if (mListAdapter.getCount() > 0) {
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
		((TextView) mRootView.findViewById(R.id.lblTitle)).setText(Str.FilterSelection[Global.LANG].toUpperCase());
		((Button) mRootView.findViewById(R.id.btnClearAllFilters)).setText(Str.ClearAllFilters[Global.LANG]);
		mEditKeyword.setHint(Str.Keyword[Global.LANG]);
		mTxtNoData.setText(Str.No_data_has_been_found[Global.LANG]);
		refreshPage();
	}

	@Override
	public void receiveBroadcast(String strAction, Intent intent) {
	}

	private void doSearch() {
		List<DescTypeInfo> infoList = new ArrayList<>();

		mStrKeyword = mEditKeyword.getText().toString().toLowerCase();
		for (DescTypeInfo info : mDescTypeList) {
			if (info.strDescription.toLowerCase().contains(mStrKeyword) == true) {
				infoList.add(info);
			}
		}
		mListAdapter.setList(infoList);
		refreshPage();
	}

	private void doCheck(DescTypeInfo info) {
		for (DescTypeInfo descTypeInfo : mFilteredDescTypeList) {
			if (descTypeInfo.strAlphaKey.equals(info.strAlphaKey) == true) {
				{
					mFilteredDescTypeList.remove(descTypeInfo);
					refreshPage();
					return;
				}
			}
		}
		mFilteredDescTypeList.add(info);
		refreshPage();
	}

	private boolean isChecked(DescTypeInfo info) {
		for (DescTypeInfo descTypeInfo : mFilteredDescTypeList) {
			if (descTypeInfo.strAlphaKey.equals(info.strAlphaKey) == true) {
				return true;
			}
		}
		return false;
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
			case R.id.btnClearAllFilters:
				mFilteredDescTypeList.clear();
				refreshPage();
				break;
		}
		m_bClicked = false;
	}

	private class ListAdapter extends BaseAdapter {
		private List<DescTypeInfo> descTypeInfoList = new ArrayList<>();

		public void setList(List<DescTypeInfo> infoList)
		{
			descTypeInfoList.clear();
			descTypeInfoList.addAll(infoList);
		}

		private class ViewHolder {
			public TextView txtDescription;
			public CheckBox chkCheck;
		}

		public int getCount() {
			return descTypeInfoList.size();
		}

		public DescTypeInfo getItem(int position) {
			if(position < descTypeInfoList.size())
			{
				return descTypeInfoList.get(position);
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
			final DescTypeInfo info = this.getItem(position);
			if(info == null) return null;
			if (convertView == null) {
				view = mContext.getLayoutInflater().inflate(R.layout.row_filter_location, parent, false);
				holder = new ViewHolder();
				holder.txtDescription = (TextView) view.findViewById(R.id.txtDescription);
				holder.chkCheck = (CheckBox) view.findViewById(R.id.chkCheck);
				view.setTag(holder);
			} else {
				holder = (ViewHolder) view.getTag();
			}

			holder.txtDescription.setText(Utils.setKeyword(info.strDescription, mStrKeyword));
			holder.chkCheck.setChecked(isChecked(info));

			holder.chkCheck.setOnClickListener(v -> doCheck(info));

			view.setOnClickListener(v -> doCheck(info));

			return view;
		}
	}
}