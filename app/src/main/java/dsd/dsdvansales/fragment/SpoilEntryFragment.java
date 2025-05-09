package dsd.dsdvansales.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dsd.dsdvansales.R;
import dsd.dsdvansales.db.DbAdapter;
import dsd.dsdvansales.dialogs.MessageDialog;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.model.DescTypeInfo;
import dsd.dsdvansales.model.ProdLocnInfo;
import dsd.dsdvansales.model.UInvenDInfo;
import dsd.dsdvansales.views.AlertSingleChoiceAdapter;

public class SpoilEntryFragment extends BaseFragment implements View.OnClickListener {

	private class SpoilReason {
		public UInvenDInfo uInvenDInfo;
		public int nReasonIdx = 0;
		public int nCartons = 0;
		public int nUnits = 0;
		public double dUnits = 0;
	}

	private TextView mTxtNoData = null;
	private ListView mListView = null;
	private ListAdapter mListAdapter = null;
	private List<SpoilReason> mIncompleteList = new ArrayList<>();
	private List<DescTypeInfo> mDescTypeList = new ArrayList<>();


	boolean bShowCase = true;
	String mCartonDescription = "";

	boolean bApplyAll = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootView = inflater.inflate(R.layout.fragment_load_adjustments, container, false);
		commonInit();
		return mRootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		Global.gFragIdx = Global.FRAG_LOAD_ADJUSTMENT;
	}

	@Override
	public void initView() {
		bShowCase = Utils.getStringSetting(mContext, Global.PREF_INVENTORYUOM).equals("U") == false;

		mRootView.findViewById(R.id.btnReturn).setOnClickListener(this);
		mRootView.findViewById(R.id.btnProceed).setOnClickListener(this);

		mTxtNoData = (TextView) mRootView.findViewById(R.id.txtNoData);
		mListView = (ListView) mRootView.findViewById(R.id.listView);
		mListAdapter = new ListAdapter();
		mListView.setAdapter(mListAdapter);

		loadData();
	}
	
	

	@Override
	public void loadData() {
		mIncompleteList.clear();
		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		mDescTypeList = dbAdapter.getDescTypeList("SPOILSRSN");
		List<UInvenDInfo> uInvenDInfo = Global.gCurEndingInventory.uInvenDList;
		for (UInvenDInfo info : uInvenDInfo) {
			if (info.nSpoils > 0) {
				SpoilReason spoilReason = new SpoilReason();
				spoilReason.nReasonIdx = info.nReasonCode;
				spoilReason.uInvenDInfo = info;
				if(bShowCase)
				{
					ProdLocnInfo prodLocnInfo = dbAdapter.getProdLocnInfo(info.strItemNo);
					int nCase = 1;
					if( prodLocnInfo != null )
					{
						nCase = prodLocnInfo.nCaseFactor;
					}
					if( nCase < 1 ) nCase = 1;

					int nPrs = info.nSpoils;
					spoilReason.nCartons = nPrs / nCase;
					spoilReason.nUnits = nPrs % nCase;
				}
				else
				{
					spoilReason.nUnits = info.nSpoils;
				}
				mIncompleteList.add(spoilReason);
			}
			else if(info.dSpoils > 0){
				SpoilReason spoilReason = new SpoilReason();
				spoilReason.nReasonIdx = info.nReasonCode;
				spoilReason.uInvenDInfo = info;
				spoilReason.dUnits = info.dSpoils;
				mIncompleteList.add(spoilReason);
			}

		}
		List<DescTypeInfo> descTypeInfoList = dbAdapter.getDescTypeList(Global.UNITOFMEASURE_DESC_TYPE_ID);
		for(DescTypeInfo descTypeInfo : descTypeInfoList)
		{
			if( descTypeInfo.strAlphaKey.toUpperCase().equals("Z") )
			{
				if(TextUtils.isEmpty(mCartonDescription))
					mCartonDescription = descTypeInfo.strDescription;
				else
					mCartonDescription += "\n" + descTypeInfo.strDescription;
			}
		}
		dbAdapter.close();

		refreshLanguage();
	}

	@Override
	public void refreshPage() {
		if (mIncompleteList.size() > 0) {
			mListView.setVisibility(View.VISIBLE);
			mTxtNoData.setVisibility(View.GONE);
			mListAdapter.notifyDataSetChanged();
		} else {
			mListView.setVisibility(View.GONE);
			mTxtNoData.setVisibility(View.VISIBLE);
		}
		boolean bEnabled = true;
		for (SpoilReason info : mIncompleteList) {
			if (info.nReasonIdx <= 0) {
				bEnabled = false;
				break;
			}
		}
		mRootView.findViewById(R.id.btnProceed).setEnabled(bEnabled);
	}

	@Override
	public void refreshLanguage() {
		((TextView) mRootView.findViewById(R.id.lblTitle)).setText(Str.SpoilsReasons[Global.LANG].toUpperCase());
		((TextView) mRootView.findViewById(R.id.lblDescription)).setText(Str.SpoilsReasonsCaption[Global.LANG]);
		((Button) mRootView.findViewById(R.id.btnReturn)).setText(Str.Return[Global.LANG]);
		((Button) mRootView.findViewById(R.id.btnProceed)).setText(Str.Proceed[Global.LANG]);
		mTxtNoData.setText(Str.No_data_has_been_found[Global.LANG]);
		refreshPage();
	}

	@Override
	public void receiveBroadcast(String strAction, Intent intent) {
	}

	private void showReasonCodeList(final int nPosition) {
		final String strArray[] = new String[mDescTypeList.size()];
		for (int i=0; i<mDescTypeList.size(); i++) {
			strArray[i] = mDescTypeList.get(i).strDescription;
		}
		final int nOrigIdx = mIncompleteList.get(nPosition).nReasonIdx;
		int nIdx = 0;
		if( nOrigIdx <= 0 ) nIdx = -1;
		else
		{
			for(int i= 0; i < mDescTypeList.size(); i++)
			{
				if( nOrigIdx == mDescTypeList.get(i).nNumericKey )
				{
					nIdx = i;
					break;
				}
			}
		}

		try {
			new AlertDialog.Builder(mContext)
			.setTitle(Str.Select_Reason_code[Global.LANG])
			.setSingleChoiceItems(new AlertSingleChoiceAdapter(mContext, strArray), nIdx, (dialog, nSelected) -> mIncompleteList.get(nPosition).nReasonIdx = mDescTypeList.get(nSelected).nNumericKey)
			.setPositiveButton(Str.Ok[Global.LANG], (dialog, nSelected) -> {
				if(!bApplyAll)
				{
					if(mIncompleteList.size() > 1)
					{
						try {
							final MessageDialog msgDialog = new MessageDialog(mContext);
							msgDialog.setCancelable(false);
							msgDialog.mStrMessage = Str.ReasonCodeApplyAll[Global.LANG];
							msgDialog.mStrLeft = Str.No[Global.LANG];
							msgDialog.mStrRight = Str.Yes[Global.LANG];
							msgDialog.setOnDismissListener(dialog1 -> {
								if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
									bApplyAll = true;
									refreshPage();
								} else if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
									bApplyAll = true;
									for(SpoilReason reason : mIncompleteList)
									{
										reason.nReasonIdx = mIncompleteList.get(nPosition).nReasonIdx;
									}
									refreshPage();
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
				bApplyAll = true;
				refreshPage();
			})
			.setNegativeButton(Str.Cancel[Global.LANG], new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int nSelected) {
					mIncompleteList.get(nPosition).nReasonIdx = nOrigIdx;
				}
			})
			.create()
			.show();
		}catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void doComplete() {
		for (SpoilReason info : mIncompleteList) {
			info.uInvenDInfo.nReasonCode = info.nReasonIdx;
		}
		mContext.replaceFragment(new EndingInventorySummaryFragment());
	}

	public boolean m_bClicked = false;
	@Override
	public void onClick(View v) {
		if( m_bClicked ) return;
		m_bClicked = true;
		switch (v.getId()) {
			case R.id.btnReturn:
				mContext.onBackPressed();
				break;
			case R.id.btnProceed:
				doComplete();
				break;
		}
		m_bClicked = false;
	}

	private class ListAdapter extends BaseAdapter {

		private class ViewHolder {
			public TextView txtName, txtCustNo, txtReason, txtCartons, txtUnits;
		}

		public int getCount() {
			return mIncompleteList.size();
		}

		public SpoilReason getItem(int position) {
			if(position < mIncompleteList.size())
			{
				return mIncompleteList.get(position);
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
			final SpoilReason info = getItem(position);
			if(info == null) return view;
			if (convertView == null) {
				view = mContext.getLayoutInflater().inflate(R.layout.row_incomplete_deliveries, parent, false);
				holder = new ViewHolder();
				holder.txtName = (TextView) view.findViewById(R.id.txtName);
				holder.txtCustNo = (TextView) view.findViewById(R.id.txtCustNo);
				holder.txtReason = (TextView) view.findViewById(R.id.txtReason);
				holder.txtCartons = (TextView) view.findViewById(R.id.txtCartons);
				holder.txtUnits = (TextView) view.findViewById(R.id.txtUnits);
				view.setTag(holder);
			} else {
				holder = (ViewHolder) view.getTag();
			}
			holder.txtName.setText(info.uInvenDInfo.strShortDesc);
			holder.txtCustNo.setText(String.valueOf(info.uInvenDInfo.strItemNo));
			if (info.nReasonIdx <= 0) {
				holder.txtReason.setText(Str.Spoils_Reason[Global.LANG]);
			} else {
				for(int i = 0; i < mDescTypeList.size(); i++)
				{
					if( mDescTypeList.get(i).nNumericKey == info.nReasonIdx )
					{
						holder.txtReason.setText(mDescTypeList.get(i).strDescription);
						break;
					}
				}
			}
			if(bShowCase && info.uInvenDInfo.nSpoils > 0)
			{
				holder.txtCartons.setVisibility(View.VISIBLE);
				if(info.nCartons > 1)
				{
					holder.txtCartons.setText(String.format(Locale.ENGLISH, "%d %ss", info.nCartons, Str.cartons[Global.LANG]));
				}
				else
				{
					holder.txtCartons.setText(String.format(Locale.ENGLISH, "%d %s", info.nCartons, Str.cartons[Global.LANG]));
				}
				if(info.nUnits > 1)
				{
					if(!TextUtils.isEmpty(mCartonDescription))
					{
						holder.txtUnits.setText(String.format(Locale.ENGLISH, "%d %ss", info.nUnits, mCartonDescription));
					}
					else
					{
						holder.txtUnits.setText(String.format(Locale.ENGLISH, "%d %ss", info.nUnits, Str.units[Global.LANG]));
					}
				}
				else
				{
					if(!TextUtils.isEmpty(mCartonDescription))
					{
						holder.txtUnits.setText(String.format(Locale.ENGLISH, "%d %s", info.nUnits, mCartonDescription));
					}
					else
					{
						holder.txtUnits.setText(String.format(Locale.ENGLISH, "%d %s", info.nUnits, Str.units[Global.LANG]));
					}
				}
			}
			else if(info.uInvenDInfo.nSpoils > 0)
			{
				holder.txtCartons.setVisibility(View.GONE);
				if(info.nUnits > 1)
				{
					if(!TextUtils.isEmpty(mCartonDescription))
					{
						holder.txtUnits.setText(String.format(Locale.ENGLISH, "%d %ss", info.nUnits, mCartonDescription));
					}
					else
					{
						holder.txtUnits.setText(String.format(Locale.ENGLISH, "%d %ss", info.nUnits, Str.units[Global.LANG]));
					}
				}
				else
				{
					if(!TextUtils.isEmpty(mCartonDescription))
					{
						holder.txtUnits.setText(String.format(Locale.ENGLISH, "%d %s", info.nUnits, mCartonDescription));
					}
					else
					{
						holder.txtUnits.setText(String.format(Locale.ENGLISH, "%d %s", info.nUnits, Str.units[Global.LANG]));
					}
				}
			}
			else if(info.uInvenDInfo.dSpoils > 0)
			{
				holder.txtCartons.setVisibility(View.GONE);
				holder.txtUnits.setText(String.format(Locale.ENGLISH, "%.2f", info.dUnits));
			}
			//holder.txtCartons.setText(String.format(Locale.ENGLISH, "%d %s", info.nCartons, Str.cartons[Global.LANG]));
			//holder.txtUnits.setText(String.format(Locale.ENGLISH, "%d %s", info.nUnits, Str.units[Global.LANG]));
			holder.txtCartons.setTextColor(mContext.getResources().getColor(R.color.black_color));
			holder.txtUnits.setTextColor(mContext.getResources().getColor(R.color.black_color));
			holder.txtReason.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showReasonCodeList(position);
				}
			});

			return view;
		}
	}
}