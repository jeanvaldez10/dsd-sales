package dsd.dsdvansales.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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
import dsd.dsdvansales.model.ProdLocnInfo;
import dsd.dsdvansales.model.UOrderDInfo;
import dsd.dsdvansales.views.AlertSingleChoiceAdapter;

public class OrderAdjustmentsFragment extends BaseFragment implements View.OnClickListener {

	private class OrderAdjustments {
		public UOrderDInfo uOrderDInfo;
		public int nReasonIdx = -1;
		public int nCartons = 0;
		public int nUnits = 0;
		public double dValue = 0f;
	}

	private TextView mTxtNoData = null;
	private ListView mListView = null;
	private ListAdapter mListAdapter = null;
	private List<OrderAdjustments> mOrderAdjustmentList = new ArrayList<>();
	private List<DescTypeInfo> mDescTypeList = new ArrayList<>();

	private List<UOrderDInfo> mUOrderDList;

	private boolean mIsPickup = false;
	private int m_nReturnReasonCodeCnt = 0;
	private boolean bShowCase = true;

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

	public void setOrderList(List<UOrderDInfo> uOrderDList, boolean isPickup)
	{
		mUOrderDList = uOrderDList;
		mIsPickup = isPickup;
	}

	@Override
	public void onResume() {
		super.onResume();
		Global.gFragIdx = Global.FRAG_ORDER_ADJUSTMENT;
	}

	@Override
	public void initView() {
		bShowCase = Utils.getStringSetting(mContext, Global.PREF_INVENTORYUOM).equals("U") == false;
		((TextView)mRootView.findViewById(R.id.lblTitle)).setText(Str.order_adjustments[Global.LANG]);
		if(mIsPickup)
		{
			((TextView)mRootView.findViewById(R.id.lblDescription)).setText(Str.ReturnReasonsDescription[Global.LANG]);
		}
		else
		{
			((TextView)mRootView.findViewById(R.id.lblDescription)).setText(Str.OrderAdjustmentsDescription[Global.LANG]);
		}

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
		mOrderAdjustmentList.clear();
		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		if( mIsPickup )
		{
			mDescTypeList = new ArrayList<>();
			List<DescTypeInfo> returnTypeList = dbAdapter.getDescTypeList("RETURNRSN");
			for(DescTypeInfo info : returnTypeList)
			{
				mDescTypeList.add(info);
			}
			m_nReturnReasonCodeCnt = mDescTypeList.size();
			List<DescTypeInfo> buyBackTypeList = dbAdapter.getDescTypeList("BUYBACKRSN");
			for(DescTypeInfo info : buyBackTypeList)
			{
				mDescTypeList.add(info);
			}
		}
		else
		{
			mDescTypeList = dbAdapter.getDescTypeList("NODLVRYRSN");
		}
		List<UOrderDInfo> uOrderDInfoList = mUOrderDList;
		for (UOrderDInfo info : uOrderDInfoList) {
			//Order Adjustment screen – (does not show the 9001 item which is listed as an issue already)
			/*if( info.nPlanQty == 0 && !mIsPickup)
			{
				continue;
			}*/
			if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
			{
				if( info.nTrxnType == Global.TRXN_FREE || info.nTrxnType == Global.TRXN_SAMPLE) continue;
				double dQuantity = mIsPickup ? info.dDumps : info.dSales;
				if (dQuantity == 0 && mIsPickup) {
					continue;
				}

				double dPrs = info.dSales - info.dPlanQty;
				if( dPrs == 0 && !mIsPickup)
				{
					continue;
				}
			}
			else
			{
				if( info.nTrxnType == Global.TRXN_FREE || info.nTrxnType == Global.TRXN_SAMPLE) continue;
				int nQuantity = mIsPickup ? info.nDumps : info.nSales;
				if (nQuantity == 0 && mIsPickup) {
					continue;
				}

				int nPrs = info.nSales - info.nPlanQty;
				if( nPrs == 0 && !mIsPickup)
				{
					continue;
				}
			}

			OrderAdjustments loadAdjustmentReason = new OrderAdjustments();
			loadAdjustmentReason.uOrderDInfo = info;
			if(mIsPickup)
			{
				loadAdjustmentReason.nReasonIdx = -1;
				for(int i = 0; i < mDescTypeList.size(); i++)
				{
					if(loadAdjustmentReason.uOrderDInfo.nTrxnType == 1 && mDescTypeList.get(i).strDescriptionTypeID.equals("RETURNRSN") &&
							loadAdjustmentReason.uOrderDInfo.nReasonCode == mDescTypeList.get(i).nNumericKey )
					{
						loadAdjustmentReason.nReasonIdx = i;
						break;
					}
					else if(loadAdjustmentReason.uOrderDInfo.nTrxnType == 2 && mDescTypeList.get(i).strDescriptionTypeID.equals("BUYBACKRSN") &&
							loadAdjustmentReason.uOrderDInfo.nReasonCode == mDescTypeList.get(i).nNumericKey )
					{
						loadAdjustmentReason.nReasonIdx = i;
						break;
					}
				}
			}
			else
			{
				loadAdjustmentReason.nReasonIdx = -1;
				for(int i = 0; i < mDescTypeList.size(); i++)
				{
					if(loadAdjustmentReason.uOrderDInfo.nReasonCode == mDescTypeList.get(i).nNumericKey )
					{
						loadAdjustmentReason.nReasonIdx = i;
						break;
					}
				}
			}


			ProdLocnInfo prodLocnInfo = dbAdapter.getProdLocnInfo(info.strItemNo);
			if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
			{
				double dQuantity = mIsPickup ? info.dDumps : info.dSales;
				double dPrs = info.dSales - info.dPlanQty;
				if( mIsPickup )
				{
					dPrs = dQuantity;
				}
				loadAdjustmentReason.dValue = dPrs;
			}
			else
			{
				if(bShowCase)
				{
					int nQuantity = mIsPickup ? info.nDumps : info.nSales;
					int nPrs = info.nSales - info.nPlanQty;
					int nCase = 1;
					if( prodLocnInfo != null )
					{
						nCase = prodLocnInfo.nCaseFactor;
					}
					if( nCase < 1 ) nCase = 1;

					if( mIsPickup )
					{
						nPrs = nQuantity;
					}
					loadAdjustmentReason.nCartons = nPrs / nCase;
					loadAdjustmentReason.nUnits = nPrs % nCase;
				}
				else
				{
					int nQuantity = mIsPickup ? info.nDumps : info.nSales;
					int nPrs = info.nSales - info.nPlanQty;

					if( mIsPickup )
					{
						nPrs = nQuantity;
					}
					loadAdjustmentReason.nUnits = nPrs;
				}

			}


			mOrderAdjustmentList.add(loadAdjustmentReason);
		}
		dbAdapter.close();

		refreshLanguage();
	}

	@Override
	public void refreshPage() {
		if (mOrderAdjustmentList.size() > 0) {
			mListView.setVisibility(View.VISIBLE);
			mTxtNoData.setVisibility(View.GONE);
			mListAdapter.notifyDataSetChanged();
		} else {
			mListView.setVisibility(View.GONE);
			mTxtNoData.setVisibility(View.VISIBLE);
		}
		boolean bEnabled = true;
		for (OrderAdjustments info : mOrderAdjustmentList) {
			if (info.nReasonIdx == -1) {
				bEnabled = false;
				break;
			}
		}
		mRootView.findViewById(R.id.btnProceed).setEnabled(bEnabled);
	}

	@Override
	public void refreshLanguage() {
		if( !mIsPickup )
			((TextView) mRootView.findViewById(R.id.lblTitle)).setText(Str.OrderAdjustments[Global.LANG].toUpperCase());
		else
			((TextView) mRootView.findViewById(R.id.lblTitle)).setText(Str.ReturnReasons[Global.LANG].toUpperCase());
		if(mIsPickup)
		{
			((TextView)mRootView.findViewById(R.id.lblDescription)).setText(Str.ReturnReasonsDescription[Global.LANG]);
		}
		else
		{
			((TextView)mRootView.findViewById(R.id.lblDescription)).setText(Str.OrderAdjustmentsDescription[Global.LANG]);
		}

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

		final int nOrigIdx = mOrderAdjustmentList.get(nPosition).nReasonIdx;

		try {
			new AlertDialog.Builder(mContext)
					.setTitle(Str.Select_Reason_code[Global.LANG])
					.setSingleChoiceItems(new AlertSingleChoiceAdapter(mContext, strArray), nOrigIdx, (dialog, nSelected) -> mOrderAdjustmentList.get(nPosition).nReasonIdx = nSelected)
					.setPositiveButton(Str.Ok[Global.LANG], (dialog, nSelected) -> {
						if(!bApplyAll)
						{
							if(mOrderAdjustmentList.size() > 1)
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
											for(OrderAdjustments reason : mOrderAdjustmentList)
											{
												reason.nReasonIdx = mOrderAdjustmentList.get(nPosition).nReasonIdx;
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
					.setNegativeButton(Str.Cancel[Global.LANG], (dialog, nSelected) -> mOrderAdjustmentList.get(nPosition).nReasonIdx = nOrigIdx)
					.create()
					.show();
		}catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void setUOrderDList() {
		if (mIsPickup == false) {
			Global.gUOrderHInfo.deliveryList.clear();
			for (UOrderDInfo info : mUOrderDList) {
				info.nQuantity = info.nSales;
				Global.gUOrderHInfo.deliveryList.add(info);
			}
			for(OrderAdjustments orderAdjustments : mOrderAdjustmentList)
			{
				orderAdjustments.uOrderDInfo.nReasonCode = mDescTypeList.get(orderAdjustments.nReasonIdx).nNumericKey;
			}
		} else {
			Global.gUOrderHInfo.pickupList.clear();
			for (UOrderDInfo info : mUOrderDList) {
				info.nQuantity = info.nDumps;
				Global.gUOrderHInfo.pickupList.add(info);
			}
			for(OrderAdjustments orderAdjustments : mOrderAdjustmentList)
			{
				orderAdjustments.uOrderDInfo.nReasonCode = mDescTypeList.get(orderAdjustments.nReasonIdx).nNumericKey;
			}
		}
	}

	private void doComplete() {
		for (OrderAdjustments info : mOrderAdjustmentList) {
			if( mIsPickup )
			{
				if( mDescTypeList.get(info.nReasonIdx).strDescriptionTypeID.equals("RETURNRSN") )
				{
					info.uOrderDInfo.nTrxnType = Global.TRXN_PICKUP;
				}
				else if ( mDescTypeList.get(info.nReasonIdx).strDescriptionTypeID.equals("BUYBACKRSN") )
				{
					info.uOrderDInfo.nTrxnType = Global.TRXN_BUYBACK;
				}
			}
			info.uOrderDInfo.nReasonCode = mDescTypeList.get(info.nReasonIdx).nNumericKey;
		}
		Global.gUOrderHInfo.strPresoldChgd = "Y";
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
				msgDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
							String strDisableNegInvAmt = Utils.getStringSetting(mContext, Global.PREF_DISABLE_NEG_INV_AMT);
							float result = DeliverySummaryFragment.getTotalInvoiceValue(mContext);
							if (strDisableNegInvAmt.equals("1") && Global.gCurCustomerInfo.nInvoiceFmt == 1 && result<0) {
								try {
									final MessageDialog msgDialog = new MessageDialog(mContext);
									msgDialog.setCancelable(false);
									msgDialog.mStrMessage = Str.SalesMustGreaterThanReturns[Global.LANG];
									msgDialog.mStrMiddle = Str.Back[Global.LANG];
									msgDialog.setOnDismissListener(dialog1 -> {
										if (msgDialog.mReturnCode == Global.DLG_RETURN_MIDDLE) {

										}
									});
									msgDialog.show();
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
							else {
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
							setUOrderDList();
							Global.gDeliveryPickupCnt++;

							MakeDeliveryFragment fragment = new MakeDeliveryFragment();
							Bundle bundle = new Bundle();
							bundle.putBoolean(Global.EXTRA_IS_PICKUP, !mIsPickup);
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
				msgDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
							String strDisableNegInvAmt = Utils.getStringSetting(mContext, Global.PREF_DISABLE_NEG_INV_AMT);
							float result = DeliverySummaryFragment.getTotalInvoiceValue(mContext);
							if (strDisableNegInvAmt.equals("1") && Global.gCurCustomerInfo.nInvoiceFmt == 1 && result<0) {
								try {
									final MessageDialog msgDialog = new MessageDialog(mContext);
									msgDialog.setCancelable(false);
									msgDialog.mStrMessage = Str.SalesMustGreaterThanReturns[Global.LANG];
									msgDialog.mStrMiddle = Str.Back[Global.LANG];
									msgDialog.setOnDismissListener(dialog1 -> {
										if (msgDialog.mReturnCode == Global.DLG_RETURN_MIDDLE) {

										}
									});
									msgDialog.show();
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
							else {
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
							setUOrderDList();
							Global.gDeliveryPickupCnt++;
							if((Global.gCurCustomerInfo.presoldOrHInfo != null && Global.gCurCustomerInfo.presoldOrHInfo.size() > 0 && (
									Global.gCurCustomerInfo.presoldOrHInfo.get(0).strType.equals("B") ||
											Global.gCurCustomerInfo.presoldOrHInfo.get(0).strType.equals("C"))
							))
							{
								BaseFragment fragment = new OnHandCountFragment();
								Bundle bundle = new Bundle();
								bundle.putBoolean(Global.EXTRA_IS_PICKUP, !mIsPickup);
								fragment.setArguments(bundle);
								mContext.replaceFragment(fragment);
							}
							else
							{
								MakeDeliveryFragment fragment = new MakeDeliveryFragment();
								Bundle bundle = new Bundle();
								bundle.putBoolean(Global.EXTRA_IS_PICKUP, !mIsPickup);
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
			Global.gCurHeaderInfo = null;
			if(Utils.getStringSetting(mContext, Global.PREF_PRINT_PICK_LIST).equals("2"))
			{
				Fragment fragment = new PickingFragment();
				mContext.replaceFragment(fragment);
			}
			else {
				Bundle bundle = new Bundle();
				bundle.putBoolean(Global.EXTRA_IS_PICKUP, mIsPickup);
				DeliverySummaryFragment fragment = new DeliverySummaryFragment();
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
			return mOrderAdjustmentList.size();
		}

		public OrderAdjustments getItem(int position) {
			if( position < mOrderAdjustmentList.size() )
			{
				return mOrderAdjustmentList.get(position);
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
			final OrderAdjustments info = getItem(position);
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
				if( mIsPickup )
				{
					holder.txtReason.setText(Str.DropDownReturnReasons[Global.LANG]);
				}
				else
				{
					holder.txtReason.setText(Str.Order_Adjustments[Global.LANG]);
				}
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
			if(Utils.getProdLoconSCWType(mContext, info.uOrderDInfo.strItemNo) == Global.WEIGHT_ITEM)
			{
				holder.txtCartons.setVisibility(View.GONE);
				if(info.dValue < 0)
				{
					holder.txtUnits.setTextColor(mContext.getResources().getColor(R.color.red_color));
				}
				else
				{
					holder.txtUnits.setTextColor(mContext.getResources().getColor(R.color.black_color));
				}
				holder.txtUnits.setText(String.format(Locale.ENGLISH, "%.2f", info.dValue));
			}
			else
			{
				if(bShowCase)
				{
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
				}
				else
				{
					holder.txtCartons.setVisibility(View.GONE);
					if( info.nUnits < 0)
					{
						holder.txtUnits.setTextColor(mContext.getResources().getColor(R.color.red_color));
					}
					else
					{
						holder.txtUnits.setTextColor(mContext.getResources().getColor(R.color.black_color));
					}
					holder.txtUnits.setText(String.format(Locale.ENGLISH, "%d %s", info.nUnits, Str.units[Global.LANG]));
				}

			}

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