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

import androidx.fragment.app.Fragment;

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
import dsd.dsdvansales.model.ProdDetlInfo;
import dsd.dsdvansales.model.ProdLocnInfo;
import dsd.dsdvansales.model.UOrderDInfo;
import dsd.dsdvansales.model.UPromotionInfo;
import dsd.dsdvansales.views.AlertSingleChoiceAdapter;

public class FreeReasonsFragment extends BaseFragment implements View.OnClickListener {

	private class FreeReasons {
		public UOrderDInfo uOrderDInfo;
		public int nReasonIdx = -1;
		public int nCartons = 0;
		public int nUnits = 0;
	}

	private TextView mTxtNoData = null;
	private ListView mListView = null;
	private ListAdapter mListAdapter = null;
	private List<FreeReasons> mFreeReasonsList = new ArrayList<>();
	private List<DescTypeInfo> mDescTypeList = new ArrayList<>();

	private List<UOrderDInfo> mUOrderDList;

	private boolean bApplyAll = false;

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

	public void setOrderList(List<UOrderDInfo> uOrderDList)
	{
		mUOrderDList = uOrderDList;
	}

	@Override
	public void onResume() {
		super.onResume();
		Global.gFragIdx = Global.FRAG_FREE_REASONS;
	}

	@Override
	public void initView() {
		((TextView)mRootView.findViewById(R.id.lblTitle)).setText(Str.FreeReasons[Global.LANG]);
		((TextView)mRootView.findViewById(R.id.lblDescription)).setText(Str.FreeReasonsDescription[Global.LANG]);

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
		mFreeReasonsList.clear();
		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();

		mDescTypeList = dbAdapter.getDescTypeList("FREERSN");

		List<DescTypeInfo> delDescTypeInfoList = new ArrayList<>();
		for(DescTypeInfo info : mDescTypeList)
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
			mDescTypeList.remove(info);
		}

		List<UOrderDInfo> uOrderDInfoList = mUOrderDList;
		for (UOrderDInfo info : uOrderDInfoList) {
			if(info.nTrxnType == Global.TRXN_SAMPLE)
			{
				if( info.nSales == 0)
				{
					continue;
				}
			}
			if(info.nTrxnType != Global.TRXN_SAMPLE)
			{
				continue;
			}

			if(info.nReasonCode != 0)
			{
				continue;
			}
			String strReasonType = "Y";
			if(info.nPlanQty != 0)
			{
				List<DescTypeInfo> descTypeInfos = dbAdapter.getDescTypeListWithNumericKey("FREERSN", info.nReasonCode);

				if(descTypeInfos != null && descTypeInfos.size() > 0)
				{
					strReasonType = descTypeInfos.get(0).strValue1;
				}
			}

			if(strReasonType.equals("N"))
			{
				continue;
			}

			FreeReasons freeReason = new FreeReasons();
			freeReason.uOrderDInfo = info;
			freeReason.nReasonIdx = -1;
			for(int i = 0; i < mDescTypeList.size(); i++)
			{
				if(freeReason.uOrderDInfo.nReasonCode == mDescTypeList.get(i).nNumericKey )
				{
					freeReason.nReasonIdx = i;
					break;
				}
			}


			ProdLocnInfo prodLocnInfo = dbAdapter.getProdLocnInfo(info.strItemNo);

			int nQuantity = info.nSales;
			int nPrs = info.nSales;
			int nCase = 1;
			if( prodLocnInfo != null )
			{
				nCase = prodLocnInfo.nCaseFactor;
			}
			if( nCase < 1 ) nCase = 1;

			freeReason.nCartons = nPrs / nCase;
			freeReason.nUnits = nPrs % nCase;


			mFreeReasonsList.add(freeReason);
		}
		dbAdapter.close();

		refreshLanguage();
	}

	@Override
	public void refreshPage() {
		if (mFreeReasonsList.size() > 0) {
			mListView.setVisibility(View.VISIBLE);
			mTxtNoData.setVisibility(View.GONE);
			mListAdapter.notifyDataSetChanged();
		} else {
			mListView.setVisibility(View.GONE);
			mTxtNoData.setVisibility(View.VISIBLE);
		}
		boolean bEnabled = true;
		for (FreeReasons info : mFreeReasonsList) {
			if (info.nReasonIdx == -1) {
				bEnabled = false;
				break;
			}
		}
		mRootView.findViewById(R.id.btnProceed).setEnabled(bEnabled);
	}

	@Override
	public void refreshLanguage() {
		((TextView) mRootView.findViewById(R.id.lblTitle)).setText(Str.FreeReasons[Global.LANG].toUpperCase());
		((TextView)mRootView.findViewById(R.id.lblDescription)).setText(Str.FreeReasonsDescription[Global.LANG]);

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

		final int nOrigIdx = mFreeReasonsList.get(nPosition).nReasonIdx;

		try {
			new AlertDialog.Builder(mContext)
					.setTitle(Str.Select_Reason_code[Global.LANG])
					.setSingleChoiceItems(new AlertSingleChoiceAdapter(mContext, strArray), nOrigIdx, (dialog, nSelected) -> mFreeReasonsList.get(nPosition).nReasonIdx = nSelected)
					.setPositiveButton(Str.Ok[Global.LANG], (dialog, nSelected) -> {
						if(!bApplyAll)
						{
							if(mFreeReasonsList.size() > 1)
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
											for(FreeReasons reason : mFreeReasonsList)
											{
												reason.nReasonIdx = mFreeReasonsList.get(nPosition).nReasonIdx;
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
					.setNegativeButton(Str.Cancel[Global.LANG], (dialog, nSelected) -> mFreeReasonsList.get(nPosition).nReasonIdx = nOrigIdx)
					.create()
					.show();
		}catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	private void setUOrderDList() {
		Global.gUOrderHInfo.deliveryList.clear();
		for (UOrderDInfo info : mUOrderDList) {
			if( info.nTrxnType == 0 )
			{
				info.nTrxnType = Global.TRXN_DELIVER;
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
		for(FreeReasons orderAdjustments : mFreeReasonsList)
		{
			orderAdjustments.uOrderDInfo.nReasonCode = mDescTypeList.get(orderAdjustments.nReasonIdx).nNumericKey;
		}
	}

	private void doComplete() {
		for (FreeReasons info : mFreeReasonsList) {
			info.uOrderDInfo.nReasonCode = mDescTypeList.get(info.nReasonIdx).nNumericKey;
			info.uOrderDInfo.uPromotionInfoList.clear();
			if(Utils.getStringSetting(mContext, Global.PREF_FRGOODPRM).equals("1") && mDescTypeList.get(info.nReasonIdx).strValue3.equals("Y"))
			{
				//Apply the FREE GOOD PLAN & ASS
				DbAdapter dbAdapter = new DbAdapter(mContext);
				dbAdapter.open();
				List<ProdDetlInfo> prodInfoList = dbAdapter.getProductListWithFreeGoods(info.uOrderDInfo.strItemNo);
				dbAdapter.close();
				if(prodInfoList != null && prodInfoList.size() > 0)
				{
					info.uOrderDInfo.nPrice = prodInfoList.get(0).nPrice;
					info.uOrderDInfo.nBasePrice = prodInfoList.get(0).nOrgGrossPrice;
					info.uOrderDInfo.nGrossPrice = prodInfoList.get(0).nGrossPrice;
					info.uOrderDInfo.nOrgPrice = prodInfoList.get(0).nOrgPrice;
					info.uOrderDInfo.nOrgGrossPrice = prodInfoList.get(0).nOrgGrossPrice;
					info.uOrderDInfo.nDiscountVal = prodInfoList.get(0).nDiscountVal;
					for(UPromotionInfo uPromotionInfo : prodInfoList.get(0).uPromotionInfoList)
					{
						UPromotionInfo uItem = uPromotionInfo.duplicate();
						uItem.nTrxnType =  Global.TRXN_DELIVER;
						uItem.nCustNo = Global.gCurCustomerInfo.nCustNo;
						uItem.nChainNo = Global.gCurCustomerInfo.nChainNo;
						uItem.strItemNo = info.uOrderDInfo.strItemNo;
						info.uOrderDInfo.uPromotionInfoList.add(uItem);
					}
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
				double nprs = info.dSales - info.dPlanQty;
				if( nprs == 0)
				{
					nCnt++;
				}
			}
			else
			{
				int nprs = info.nSales - info.nPlanQty;
				if( nprs == 0)
				{
					nCnt++;
				}
			}
		}
		List<DescTypeInfo> descTypeInfoList;
		if (nCnt < mUOrderDList.size() && (Global.gCurCustomerInfo.presoldOrHInfo != null && Global.gCurCustomerInfo.presoldOrHInfo.size() > 0)) {
			DbAdapter dbAdapter = new DbAdapter(mContext);
			dbAdapter.open();
			descTypeInfoList = dbAdapter.getDescTypeList("NODLVRYRSN");
			dbAdapter.close();
			if( descTypeInfoList != null && descTypeInfoList.size() > 1 )
			{
				OrderAdjustmentsFragment fragment = new OrderAdjustmentsFragment();
				fragment.setOrderList(mUOrderDList, false);
				mContext.replaceFragment(fragment);
				return;
			}
			else
			{
				if(descTypeInfoList != null && descTypeInfoList.size() == 1) {
					for(UOrderDInfo info : mUOrderDList)
					{
						if(info.nTrxnType == Global.TRXN_DELIVER)
						{
							info.nReasonCode = descTypeInfoList.get(0).nNumericKey;
						}
					}
				}
			}
		}

		if (Global.gCurCustomerInfo.nInvoiceFmt == 1 && Global.gDeliveryPickupCnt == 0) {
			String strMessage = Str.ThisIsNettInvoiceCustomer[Global.LANG] + "\n";

			strMessage += Str.DoYouHaveReturns[Global.LANG];

			try {
				final MessageDialog msgDialog = new MessageDialog(mContext);
				msgDialog.setCancelable(false);
				msgDialog.mStrMessage = strMessage;
				msgDialog.mStrLeft = Str.No[Global.LANG];
				msgDialog.mStrRight = Str.Yes[Global.LANG];
				msgDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
							setUOrderDList();
							Global.gCurHeaderInfo = null;
							if(Utils.getStringSetting(mContext, Global.PREF_PRINT_PICK_LIST).equals("2"))
							{
								Fragment fragment = new PickingFragment();
								mContext.replaceFragment(fragment);
							}
							else {
								mContext.replaceFragment(new DeliverySummaryFragment());
							}
						} else if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
							setUOrderDList();
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

		}
		else {
			setUOrderDList();
			Global.gCurHeaderInfo = null;
			if(Utils.getStringSetting(mContext, Global.PREF_PRINT_PICK_LIST).equals("2"))
			{
				Fragment fragment = new PickingFragment();
				mContext.replaceFragment(fragment);
			}
			else {
				Bundle bundle = new Bundle();
				bundle.putBoolean(Global.EXTRA_IS_PICKUP, false);
				Fragment fragment = new DeliverySummaryFragment();
				fragment.setArguments(bundle);
				mContext.replaceFragment(fragment);
			}
		}
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
			return mFreeReasonsList.size();
		}

		public FreeReasons getItem(int position) {
			if(position < mFreeReasonsList.size())
			{
				return mFreeReasonsList.get(position);
			}
			else {
				return null;
			}
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(final int position, View convertView, ViewGroup parent) {
			View view = convertView;
			final ViewHolder holder;
			final FreeReasons info = getItem(position);
			if(info == null) return null;
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

			holder.txtName.setText(info.uOrderDInfo.strShortDesc);
			holder.txtCustNo.setText(Utils.getDisplayItemCode(mContext, info.uOrderDInfo.strItemNo));
			if (info.nReasonIdx == -1) {

				holder.txtReason.setText(Str.FreeReason[Global.LANG]);
			} else {
				if(mDescTypeList != null && mDescTypeList.size() > 0 && info.nReasonIdx < mDescTypeList.size() && info.nReasonIdx >= 0)
				{
					holder.txtReason.setText(mDescTypeList.get(info.nReasonIdx).strDescription);
				}
				else
				{
					holder.txtReason.setText("");
				}
			}
			holder.txtCartons.setVisibility(View.VISIBLE);
			if( info.nCartons < 0 || info.nUnits < 0)
			{
				holder.txtCartons.setTextColor(mContext.getResources().getColor(R.color.red_color));
				holder.txtUnits.setTextColor(mContext.getResources().getColor(R.color.red_color));
			}
			else
			{
				holder.txtCartons.setTextColor(mContext.getResources().getColor(R.color.black_color));
				holder.txtUnits.setTextColor(mContext.getResources().getColor(R.color.black_color));
			}
			holder.txtCartons.setText(String.format(Locale.ENGLISH, "%d %s", info.nCartons, Str.cartons[Global.LANG]));
			holder.txtUnits.setText(String.format(Locale.ENGLISH, "%d %s", info.nUnits, Str.units[Global.LANG]));

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