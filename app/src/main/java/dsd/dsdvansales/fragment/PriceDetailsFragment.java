package dsd.dsdvansales.fragment;

import android.content.*;
import android.os.*;
import android.text.*;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.util.*;

import dsd.dsdvansales.R;
import dsd.dsdvansales.db.DbAdapter;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.model.ProdDetlInfo;
import dsd.dsdvansales.model.ProdInfo;
import dsd.dsdvansales.model.UOrderDInfo;

public class PriceDetailsFragment extends BaseFragment implements View.OnClickListener {

	private LinearLayout mLayoutPrices = null;
	private List<UOrderDInfo> mOrderList = new ArrayList<>();

	private Map<String, ProdDetlInfo> mProdDetlItemMap = new HashMap<>();
	private Map<String, ProdInfo> mProdInfoMap = new HashMap<>();

	private int mType = Global.INVENTORY_FRESH;

	private DeliverySummaryFragment previousFragment;
	private boolean bConsignment = false;

	public void setPreviousFragment(DeliverySummaryFragment fragment)
	{
		previousFragment = fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle bundle = getArguments();
		if(bundle != null)
		{
			bConsignment = bundle.getBoolean(Global.EXTRA_CONSIGNMENT);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootView = inflater.inflate(R.layout.fragment_price_details, container, false);
		commonInit();
		return mRootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		Global.gFragIdx = Global.FRAG_PRICE_DETAILS;
	}

	@Override
	public void initView() {
		mRootView.findViewById(R.id.txtDeliver).setOnClickListener(this);
		mRootView.findViewById(R.id.txtFree).setOnClickListener(this);
		mRootView.findViewById(R.id.txtReturns).setOnClickListener(this);
		mRootView.findViewById(R.id.txtContainers).setOnClickListener(this);
		mRootView.findViewById(R.id.btnBack).setOnClickListener(this);
		mRootView.findViewById(R.id.btnAdjust).setOnClickListener(this);

		mLayoutPrices = (LinearLayout) mRootView.findViewById(R.id.layoutPrices);

		((TextView) mRootView.findViewById(R.id.txtCustomerName)).setText(Global.gCurCustomerInfo.strName);
		mRootView.findViewById(R.id.btnAdjust).setEnabled(Global.gCurCustomerInfo.nCustPriceMaintFlag > 0);
		loadData();
		refreshLanguage();
		//setType(Global.INVENTORY_FRESH);
	}

	@Override
	public void loadData() {
		mOrderList.clear();
		int nSalesCnt = 0;
		int nDumpsCnt = 0;
		int nContainerCnt = 0;
		int nFreeCnt = 0;
		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
//		dbAdapter.getProdDetlList(mProdDetlItemMap, null);
//		if(Utils.getStringSetting(mContext, Global.PREF_ITEM_CODE_DISPLAY_SOURCE).equals("PRODINFO"))
//		{
//			mProdInfoMap = dbAdapter.getProdInfoMap("ItemCode");
//		}
		dbAdapter.close();
		for (UOrderDInfo info : Global.gUOrderHInfo.deliveryList) {
			if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
			{
				if (info.dSales > 0) {
					mOrderList.add(copyUOrderDInfo(info));
					if(info.nTrxnType == Global.TRXN_FREE || info.nTrxnType == Global.TRXN_SAMPLE)
					{
						nFreeCnt++;
					}
					else
					{
						nSalesCnt++;
					}

				}
			}
			else{
				if (info.nSales > 0) {
					mOrderList.add(copyUOrderDInfo(info));
					if(info.nTrxnType == Global.TRXN_FREE || info.nTrxnType == Global.TRXN_SAMPLE)
					{
						nFreeCnt++;
					}
					else
					{
						nSalesCnt++;
					}
				}
			}
		}
		for (UOrderDInfo info : Global.gUOrderHInfo.pickupList) {
			if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
			{
				if (info.dDumps > 0) {
					mOrderList.add(copyUOrderDInfo(info));
					nDumpsCnt++;
				}
			}
			else
			{
				if (info.nDumps > 0) {
					mOrderList.add(copyUOrderDInfo(info));
					nDumpsCnt++;
				}
			}
		}
		for (UOrderDInfo containerInfo : Global.gUOrderHInfo.containerList) {
			if ((containerInfo.nTrxnType == Global.TRXN_CONTAINER_SALE && containerInfo.nSales != 0) ||
					(containerInfo.nTrxnType == Global.TRXN_CONTAINER_DUMP && containerInfo.nDumps != 0)) {
				containerInfo.nSCWType = Global.INVENTORY_CONTAINERS;
				mOrderList.add(copyUOrderDInfo(containerInfo));
				nContainerCnt++;
			}
		}

		LinearLayout layout = (LinearLayout)mRootView.findViewById(R.id.layoutButton);
		View salesView = layout.findViewById(R.id.txtDeliver);
		View freeView = layout.findViewById(R.id.txtFree);
		View dumpsView = layout.findViewById(R.id.txtReturns);
		View containerView = layout.findViewById(R.id.txtContainers);
		layout.removeAllViews();
		if(nSalesCnt > 0)
		{
			layout.addView(salesView);
			salesView.setVisibility(View.VISIBLE);
		}
		if(nFreeCnt > 0)
		{
			layout.addView(freeView);
			freeView.setVisibility(View.VISIBLE);
		}

		if(nDumpsCnt > 0)
		{
			layout.addView(dumpsView);
			dumpsView.setVisibility(View.VISIBLE);
		}

		if(nContainerCnt > 0)
		{
			layout.addView(containerView);
			containerView.setVisibility(View.VISIBLE);
		}

		if(nSalesCnt == 0)
		{
			layout.addView(salesView);
			salesView.setVisibility(View.INVISIBLE);
		}
		if(nFreeCnt == 0)
		{
			layout.addView(freeView);
			freeView.setVisibility(View.INVISIBLE);
		}
		if(nDumpsCnt == 0)
		{
			layout.addView(dumpsView);
			dumpsView.setVisibility(View.INVISIBLE);
		}
		if(nContainerCnt == 0)
		{
			layout.addView(containerView);
			containerView.setVisibility(View.INVISIBLE);
		}

		if(nSalesCnt == 0)
        {
        	if(nFreeCnt == 0) {

				if (nDumpsCnt == 0) {
					if (nContainerCnt != 0) {
						containerView.performClick();
					}
				} else {
					dumpsView.performClick();
				}
			}
        }
        else
        {
            salesView.performClick();
        }
	}

	private UOrderDInfo copyUOrderDInfo(UOrderDInfo from) {
		UOrderDInfo to = new UOrderDInfo();
		to.nSCWType = from.nSCWType;
		to.nTrxnType = from.nTrxnType;
		to.strItemNo = from.strItemNo;
		to.strProdGroup = from.strProdGroup;
		to.strShortDesc = from.strShortDesc;
		to.nSales = from.nSales;
		to.nDumps = from.nDumps;
		to.nPrice = from.nPrice;
		to.nDiscountVal = from.nDiscountVal;

		to.dSales = from.dSales;
		to.dDumps = from.dDumps;

		return to;
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

	@Override
	public void refreshPage() {
		List<UOrderDInfo> list = new ArrayList<>();
		for (UOrderDInfo info : mOrderList) {
			if (mType == Global.INVENTORY_FRESH) {
				if ((info.nSCWType == Global.INVENTORY_TRUCK || info.nSCWType == Global.WEIGHT_ITEM) &&
						(info.nTrxnType == Global.TRXN_DELIVER)) {
					if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
					{
						info.dQuantity = info.dSales;
					}
					else
					{
						info.nQuantity = info.nSales;
					}

					list.add(info);
				}
			} else if (mType == Global.INVENTORY_FREE) {
				if ((info.nSCWType == Global.INVENTORY_TRUCK || info.nSCWType == Global.WEIGHT_ITEM) &&
						(info.nTrxnType == Global.TRXN_FREE || info.nTrxnType == Global.TRXN_SAMPLE)) {
					if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
					{
						info.dQuantity = info.dSales;
					}
					else
					{
						info.nQuantity = info.nSales;
					}

					list.add(info);
				}
			} else if (mType == Global.INVENTORY_RETURNS) {
				if ((info.nSCWType == Global.INVENTORY_TRUCK || info.nSCWType == Global.WEIGHT_ITEM) &&
						info.nTrxnType == Global.TRXN_BUYBACK) {
					if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
					{
						info.dQuantity = info.dDumps;
					}
					else
					{
						info.nQuantity = info.nDumps;
					}
					list.add(info);
				}
				else if ((info.nSCWType == Global.INVENTORY_TRUCK || info.nSCWType == Global.WEIGHT_ITEM) &&
						info.nTrxnType == Global.TRXN_PICKUP ) {
					if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
					{
						info.dQuantity = info.dDumps;
					}
					else
					{
						info.nQuantity = info.nDumps;
					}
					list.add(info);
				}
			} else if (mType == Global.INVENTORY_CONTAINERS) {
				if (info.nSCWType == Global.INVENTORY_CONTAINERS) {
					int nIdx = -1;
					for (int i=0; i<list.size(); i++) {
						if (list.get(i).strItemNo.equals(info.strItemNo) == true) {
							nIdx = i;
							break;
						}
					}
					if (nIdx == -1) {
						UOrderDInfo cInfo = copyUOrderDInfo(info);
						cInfo.nQuantity = (info.nSales - info.nDumps);
						list.add(cInfo);
					} else {
						list.get(nIdx).nQuantity += (info.nSales - info.nDumps);
					}
				}
			}
		}
		mLayoutPrices.removeAllViews();
		for (UOrderDInfo info : list) {
			View childView = mContext.getLayoutInflater().inflate(R.layout.row_price_detail, null);
			mLayoutPrices.addView(childView);

			final EditText editPrice = (EditText) childView.findViewById(R.id.editPrice);

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
//			((TextView) childView.findViewById(R.id.txtItemNo)).setText(strItemNo);

			((TextView) childView.findViewById(R.id.txtItemNo)).setText(Utils.getDisplayItemCode(mContext, info.strItemNo));

			((TextView) childView.findViewById(R.id.txtShortDesc)).setText(info.strShortDesc);
			if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
			{
				((TextView) childView.findViewById(R.id.txtQty)).setText(String.format(Locale.ENGLISH, "%.2f", info.dQuantity));
			}
			else
			{
				if(Global.gCurCustomerInfo.strSalEntryMode.equals("B"))
				{
					if (Utils.isContainerItem(mContext, info.strItemNo)) {
						((TextView) childView.findViewById(R.id.txtQty)).setText(String.format(Locale.ENGLISH, "%d", info.nQuantity));
					}
					else {
						int nCase = getCaseValue(info.strItemNo);
						int nCaseValue = info.nQuantity / nCase;
						int nUnitValue = info.nQuantity % nCase;
						((TextView) childView.findViewById(R.id.txtQty)).setText(String.format(Locale.ENGLISH, "%d/%d", nCaseValue, nUnitValue));
					}
				}
				else if( Global.gCurCustomerInfo.strSalEntryMode.equals("U") || Global.gCurCustomerInfo.strSalEntryMode.equals(""))
				{
					((TextView) childView.findViewById(R.id.txtQty)).setText(String.valueOf(info.nQuantity));
				}
				else
				{
					((TextView) childView.findViewById(R.id.txtQty)).setText(String.valueOf(info.nQuantity));
				}
			}

			((TextView) childView.findViewById(R.id.lblCurrencySymbol)).setText(Global.gCurrencySymbol);

			if( mType == Global.INVENTORY_RETURNS )
			{
				childView.findViewById(R.id.txtItemType).setVisibility(View.VISIBLE);
				if ( info.nTrxnType == Global.TRXN_BUYBACK )
				{
					((TextView)childView.findViewById(R.id.txtItemType)).setText("BBK");
				}
				else if ( info.nTrxnType == Global.TRXN_PICKUP )
				{
					((TextView)childView.findViewById(R.id.txtItemType)).setText("DMP");
				}
			}
			else
			{
				childView.findViewById(R.id.txtItemType).setVisibility(View.GONE);
			}

			if( mType == Global.INVENTORY_FREE )
			{
				childView.findViewById(R.id.txtFreeItem).setVisibility(View.VISIBLE);
			}
			else
			{
				childView.findViewById(R.id.txtFreeItem).setVisibility(View.GONE);
			}

			setPriceAmount(editPrice, info.nPrice, "");

			if (Global.gCurCustomerInfo.nCustPriceMaintFlag > 0) {
				String strItemNo = info.strItemNo;
				editPrice.setEnabled(true);
				editPrice.post(new Runnable() {
					public void run() {
						editPrice.setSelection(editPrice.getText().toString().length());
						editPrice.postDelayed(this, 100);
					}
				});
				String finalStrItemNo = strItemNo;
				editPrice.addTextChangedListener(new TextWatcher() {
					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count) {
					}
					@Override
					public void beforeTextChanged(CharSequence s, int start, int count, int after) {
					}
					@Override
					public void afterTextChanged(Editable s) {
						if (editPrice.getTag() == null) {
							setFormattedAmount(editPrice, s.toString(), finalStrItemNo);
						}
					}
				});
			} else {
				editPrice.setEnabled(false);
			}
		}
	}

	private void setFormattedAmount(EditText editPrice, String strVal, String strItemNo) {
		String strArray[] = strVal.split("\\.");
		strVal = "";
		for (int i=0; i<strArray.length; i++) {
			strVal += strArray[i];
		}
		int nValue = Utils.getIntSetting(mContext, Global.PREF_DECIMAL);
		float nPrice = Utils.parseFloatFromString(strVal) / (float)(Math.pow(10,nValue));
		setPriceAmount(editPrice, nPrice, strItemNo);
	}

	private void setPriceAmount(EditText editPrice, float nPrice, String strItemNo) {
		editPrice.setTag("From App");

		editPrice.setText(Utils.getRoundedString(mContext, nPrice));
		editPrice.setSelection(editPrice.getText().toString().length());
		editPrice.setTag(null);
		if (strItemNo.length() > 0) {
			for (int i=0; i<mOrderList.size(); i++) {
				if (mOrderList.get(i).strItemNo.equals(strItemNo) == true) {
					if( (mOrderList.get(i).nTrxnType == Global.TRXN_PICKUP && mType == Global.INVENTORY_RETURNS) ||
							(mOrderList.get(i).nTrxnType == Global.TRXN_BUYBACK && mType == Global.INVENTORY_RETURNS) ||
							(mOrderList.get(i).nTrxnType == Global.TRXN_DELIVER && mType == Global.INVENTORY_FRESH) ||
							(mOrderList.get(i).nSCWType == Global.INVENTORY_CONTAINERS && mType == Global.INVENTORY_CONTAINERS)
							)
					{
						mOrderList.get(i).nPrice = nPrice;
					}
				}
			}
		}
	}

	@Override
	public void refreshLanguage() {
		((TextView) mRootView.findViewById(R.id.lblTitle)).setText(Str.PriceDetails[Global.LANG].toUpperCase());
		((TextView) mRootView.findViewById(R.id.txtDeliver)).setText(Str.Deliver[Global.LANG]);
		((TextView) mRootView.findViewById(R.id.txtFree)).setText(Str.Free[Global.LANG]);
		((TextView) mRootView.findViewById(R.id.txtReturns)).setText(Str.Returns[Global.LANG]);
		((TextView) mRootView.findViewById(R.id.txtContainers)).setText(Str.Containers[Global.LANG]);
		((Button) mRootView.findViewById(R.id.btnAdjust)).setText(Str.Adjust[Global.LANG]);
	}

	@Override
	public void receiveBroadcast(String strAction, Intent intent) {
	}

	private void setType(int nType) {
		mType = nType;
		int nNormalColor = mContext.getResources().getColor(R.color.vehicle_inventory_normal_color);
		int nSelectedColor = mContext.getResources().getColor(R.color.vehicle_inventory_selected_color);
		mRootView.findViewById(R.id.txtDeliver).setBackgroundColor(nNormalColor);
		mRootView.findViewById(R.id.txtFree).setBackgroundColor(nNormalColor);
		mRootView.findViewById(R.id.txtReturns).setBackgroundColor(nNormalColor);
		mRootView.findViewById(R.id.txtContainers).setBackgroundColor(nNormalColor);
		if (mType == Global.INVENTORY_FRESH) {
			mRootView.findViewById(R.id.txtDeliver).setBackgroundColor(nSelectedColor);
		} else if (mType == Global.INVENTORY_RETURNS) {
			mRootView.findViewById(R.id.txtReturns).setBackgroundColor(nSelectedColor);
		} else if (mType == Global.INVENTORY_CONTAINERS) {
			mRootView.findViewById(R.id.txtContainers).setBackgroundColor(nSelectedColor);
		} else if (mType == Global.INVENTORY_FREE) {
			mRootView.findViewById(R.id.txtFree).setBackgroundColor(nSelectedColor);
		}
		refreshPage();
	}

	private void doAdjust() {
		for (int i=0; i<Global.gUOrderHInfo.deliveryList.size(); i++) {
			float nPrice = Global.gUOrderHInfo.deliveryList.get(i).nPrice;
			float nDiscountVal = Global.gUOrderHInfo.deliveryList.get(i).nDiscountVal;
			boolean bModified = false;
			for (UOrderDInfo info : mOrderList) {
				if (Global.gUOrderHInfo.deliveryList.get(i).strItemNo.equals(info.strItemNo) == true &&
						info.nTrxnType ==  Global.gUOrderHInfo.deliveryList.get(i).nTrxnType) {
					nDiscountVal -= info.nPrice - nPrice;
					if( Math.abs(nPrice - info.nPrice) > 0.000001 )
					{
						bModified = true;
					}
					nPrice = info.nPrice;
					break;
				}
			}
			if( bModified )
			{
				Global.gUOrderHInfo.deliveryList.get(i).nPriceOverride = 1;
			}
			Global.gUOrderHInfo.deliveryList.get(i).nCustomerRetailPrice = Global.gUOrderHInfo.deliveryList.get(i).nPrice;
			Global.gUOrderHInfo.deliveryList.get(i).nPrice = nPrice;
			Global.gUOrderHInfo.deliveryList.get(i).nDiscountVal = nDiscountVal;
		}
		for (int i=0; i<Global.gUOrderHInfo.pickupList.size(); i++) {
			float nPrice = Global.gUOrderHInfo.pickupList.get(i).nPrice;
			float nDiscountVal = Global.gUOrderHInfo.pickupList.get(i).nDiscountVal;
			boolean bModified = false;
			for (UOrderDInfo info : mOrderList) {
				if (Global.gUOrderHInfo.pickupList.get(i).strItemNo.equals(info.strItemNo) == true &&
						info.nTrxnType ==  Global.gUOrderHInfo.pickupList.get(i).nTrxnType) {
					nDiscountVal -= info.nPrice - nPrice;
					if( Math.abs(nPrice - info.nPrice) > 0.000001 )
					{
						bModified = true;
					}
					nPrice = info.nPrice;
					break;
				}
			}
			if( bModified )
			{
				Global.gUOrderHInfo.pickupList.get(i).nPriceOverride = 1;
			}
			Global.gUOrderHInfo.pickupList.get(i).nCustomerRetailPrice = Global.gUOrderHInfo.pickupList.get(i).nPrice;
			Global.gUOrderHInfo.pickupList.get(i).nPrice = nPrice;
			Global.gUOrderHInfo.pickupList.get(i).nDiscountVal = nDiscountVal;
		}
		if( previousFragment != null )
		{
			previousFragment.setResetCollectionValue();
		}
		mContext.onBackPressed();
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
			case R.id.btnAdjust:
				doAdjust();
				break;
			case R.id.txtDeliver:
				setType(Global.INVENTORY_FRESH);
				break;
			case R.id.txtFree:
				setType(Global.INVENTORY_FREE);
				break;
			case R.id.txtReturns:
				setType(Global.INVENTORY_RETURNS);
				break;
			case R.id.txtContainers:
				setType(Global.INVENTORY_CONTAINERS);
				break;
		}
		m_bClicked = false;
	}
}