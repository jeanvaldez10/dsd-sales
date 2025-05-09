package dsd.dsdvansales.fragment;

import android.content.*;
import android.os.Bundle;
import android.support.v4.app.*;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.*;
import android.widget.*;

import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import dsd.dsdvansales.R;
import dsd.dsdvansales.db.DbAdapter;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.model.SurveyInfo;

public class PosCheckFragment extends BaseFragment implements OnClickListener {

	private TextView mTxtNoData = null;
	private ListView mListView = null;
	private ListAdapter mListAdapter = null;
	private List<SurveyInfo> mSurveyList = new ArrayList<>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootView = inflater.inflate(R.layout.fragment_pos_check, container, false);
		commonInit();
		return mRootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		Global.gFragIdx = Global.FRAG_POS_CHECK;
	}

	@Override
	public void initView() {
		mRootView.findViewById(R.id.btnBack).setOnClickListener(this);
		mRootView.findViewById(R.id.btnCompleted).setOnClickListener(this);

		mTxtNoData = (TextView) mRootView.findViewById(R.id.txtNoData);
		mListView = (ListView) mRootView.findViewById(R.id.listView);
		mListAdapter = new ListAdapter();
		mListView.setAdapter(mListAdapter);

		((TextView) mRootView.findViewById(R.id.lblTitle)).setText(getArguments().getString(Global.EXTRA_SURVEY_TITLE).toUpperCase());
		((TextView) mRootView.findViewById(R.id.txtCustomerName)).setText(Global.gCurCustomerInfo.strName);
		loadData();
		refreshLanguage();
	}

	@Override
	public void loadData() {
		int nSurveyType = getArguments().getInt(Global.EXTRA_SURVEY_TYPE);
		mSurveyList.clear();
		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		mSurveyList.clear();
		String curDate = new SimpleDateFormat("yyyyMMdd").format(new Date());
		List<SurveyInfo> surveyList = dbAdapter.getSurveyListWithDistAndHead(nSurveyType, Global.gCurCustomerInfo.nCustNo, Global.gCurCustomerInfo.nChainNo);
		for(SurveyInfo surveyInfo : surveyList)
		{
			if((curDate.compareTo(surveyInfo.strCreateDate) >= 0 || TextUtils.isEmpty(surveyInfo.strCreateDate))
					&& (curDate.compareTo(surveyInfo.strCompletionDate) <= 0 || TextUtils.isEmpty(surveyInfo.strCompletionDate)))
			{
				surveyInfo.bCompleted = dbAdapter.isCompletedSurvey(surveyInfo.strSurveyID, Global.gCurCustomerInfo.nCustNo, Global.gCurCustomerInfo.nChainNo);
				mSurveyList.add(surveyInfo);
			}
		}

		dbAdapter.close();

		if(mSurveyList.size() == 1)
		{
			gotoPosCheckDetail(mSurveyList.get(0));
		}
		refreshPage();
	}

	@Override
	public void refreshPage() {
		if (mSurveyList.size() > 0) {
			mListView.setVisibility(View.VISIBLE);
			mTxtNoData.setVisibility(View.GONE);
			mListAdapter.setListData(mSurveyList);
			mListAdapter.notifyDataSetChanged();
		} else {
			mListView.setVisibility(View.GONE);
			mTxtNoData.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void refreshLanguage() {
		((TextView) mRootView.findViewById(R.id.txtVisitStarted)).setText(Str.Visit_Started_at[Global.LANG] + Global.gVisitStartedTime);
		((Button) mRootView.findViewById(R.id.btnCompleted)).setText(Str.Completed[Global.LANG]);
		mTxtNoData.setText(Str.No_data_has_been_found[Global.LANG]);
	}

	@Override
	public void receiveBroadcast(String strAction, Intent intent) {
	}

	private void gotoPosCheckDetail(SurveyInfo info) {
		Bundle bundle = new Bundle();
		bundle.putInt(Global.EXTRA_SURVEY_IDX, info.nIdx);
		bundle.putString(Global.EXTRA_SURVEY_ID, info.strSurveyID);
		bundle.putString(Global.EXTRA_SURVEY_TITLE, info.strSurveyTitle);
		bundle.putString(Global.EXTRA_SURVEY_RESPONSE, info.strSurveyResponse);
		bundle.putBoolean(Global.EXTRA_FROM_SURVEY_ID, info.bFromSurveyID);
		Fragment fragment = new PosCheckDetailFragment();
		fragment.setArguments(bundle);
		mContext.replaceFragment(fragment);
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
			case R.id.btnCompleted:
				mContext.onBackPressed();
				break;
		}
		m_bClicked = false;
	}

	private class ListAdapter extends BaseAdapter {

		private List<SurveyInfo> list = new ArrayList<>();

		private class ViewHolder {
			public TextView txtSubject;
			public Button btnCheck;
			public ImageView imgCheck;
		}

		public int getCount() {
			return list.size();
		}

		public void setListData(List<SurveyInfo> list) {
			this.list.clear();
			this.list.addAll(list);
		}

		public SurveyInfo getItem(int position) {
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
			final SurveyInfo info = getItem(position);
			if(info == null) return null;

			if (convertView == null) {
				view = mContext.getLayoutInflater().inflate(R.layout.row_pos_check, parent, false);
				holder = new ViewHolder();
				holder.txtSubject = (TextView) view.findViewById(R.id.txtSubject);
				holder.btnCheck = (Button) view.findViewById(R.id.btnCheck);
				holder.imgCheck = (ImageView) view.findViewById(R.id.imgCheck);
				view.setTag(holder);
			} else {
				holder = (ViewHolder) view.getTag();
			}

			if(info.strMandatory.equals("Y"))
			{
				holder.txtSubject.setTextColor(mContext.getResources().getColor(R.color.green_color));
			}
			else {
				holder.txtSubject.setTextColor(mContext.getResources().getColor(R.color.black_color));
			}

			holder.txtSubject.setText(info.strSurveyTitle);
			holder.btnCheck.setText(Str.Check[Global.LANG]);

			if (info.bCompleted) {
				holder.btnCheck.setVisibility(View.GONE);
				holder.imgCheck.setVisibility(View.VISIBLE);
			} else {
				holder.btnCheck.setVisibility(View.VISIBLE);
				holder.imgCheck.setVisibility(View.GONE);
			}

			holder.btnCheck.setOnClickListener(v -> gotoPosCheckDetail(info));

			return view;
		}
	}
}