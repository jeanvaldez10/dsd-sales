package dsd.dsdvansales.dialogs;

import java.util.*;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.*;
import android.view.*;
import android.widget.*;

import dsd.dsdvansales.R;
import dsd.dsdvansales.db.DbAdapter;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.model.ProdDetlInfo;
import dsd.dsdvansales.model.ProdLocnInfo;
import dsd.dsdvansales.model.TaxRatesInfo;
import dsd.dsdvansales.model.UOrderDInfo;
import dsd.dsdvansales.model.UTaxInfo;


public class ContainerTrackingDialog extends Dialog {

	public int mReturnCode = 0;
	public List<UOrderDInfo> mContainerList = new ArrayList<>();

	private Context mContext = null;
	private TextView mTxtNoData = null;
	private LinearLayout mLayoutContainers = null;

	private Map<String, UOrderDInfo> mSaleMap = new HashMap<>();
	private Map<String, UOrderDInfo> mDumpMap = new HashMap<>();

	public ContainerTrackingDialog(Context context) {
		super(context, android.R.style.Theme_Translucent_NoTitleBar);
		this.mContext = context;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
		lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
		lpWindow.dimAmount = 0.9f;
		getWindow().setAttributes(lpWindow);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.dialog_container_tracking);

		initView();
	}

	private void initView() {
		mLayoutContainers = (LinearLayout) findViewById(R.id.layoutContainers);
		mTxtNoData = (TextView) findViewById(R.id.txtNoData);

		findViewById(R.id.btnReturn).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mReturnCode = Global.DLG_RETURN_LEFT;
				dismiss();
			}
		});

		findViewById(R.id.btnConfirm).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mReturnCode = Global.DLG_RETURN_RIGHT;
				DbAdapter dbAdapter = new DbAdapter(mContext);
				dbAdapter.open();
				for(UOrderDInfo info : mContainerList)
				{
					UOrderDInfo dInfo = null;
					if(info.nTrxnType == Global.TRXN_CONTAINER_SALE)
					{
						dInfo = mSaleMap.get(info.strItemNo);
					}
					else
					{
						dInfo = mDumpMap.get(info.strItemNo);
					}
					if( dInfo == null )
					{
						if( Global.gUOrderHInfo != null )
						{
							if( info.nTrxnType == Global.TRXN_CONTAINER_SALE )
							{
								if( Global.gUOrderHInfo.deliveryList == null )
								{
									Global.gUOrderHInfo.deliveryList = new ArrayList<>();
								}
								Global.gUOrderHInfo.deliveryList.add(info);
								ProdLocnInfo prodLocnInfo = dbAdapter.getProdLocnInfo(info.strItemNo);
								ProdDetlInfo prodDetlInfo = dbAdapter.getProdDetl(info.strItemNo);
								if (prodLocnInfo != null && prodLocnInfo.strTaxFlag.equals("Y")) {
									info.strItemTaxCode = prodLocnInfo.strItemTaxCode;
								}


								if (info.nPromoType == Global.PROMO_TYPE_BUY_FREE_MULTI) {
									int nFreeQty = (int)(Utils.myDivider(info.nSales, info.nPromoDiscount));
									Global.gUOrderHInfo.nTotalFree += nFreeQty;
								}

								//Calculation Tax Amount
								List<TaxRatesInfo> taxRatesInfoList = dbAdapter.getTaxRatesInfoList(Global.gCurCustomerInfo.strTaxCode, info.strItemNo);
								info.nTrxnNo = Global.gUOrderHInfo.nTrxnNo;
								info.nTaxAmount = 0;
								info.nPrintTaxAmount = 0;
								boolean bAppliedGST = false;
								float fGSTOnCDS = 0.0f;
								List<UTaxInfo> gstOnCDSTaxRateInfoList = new ArrayList<>();
								for(TaxRatesInfo taxRatesInfo : taxRatesInfoList)
								{
									float nTaxAmount = 0;
									float nPrintTaxAmount = 0;
									if(taxRatesInfo.nTaxMethod == 1) //Applying Cents Amount
									{
										nTaxAmount = Utils.getDividedValue(taxRatesInfo.nTaxRate) * info.nSales * prodDetlInfo.nConsumerUnit;
										nPrintTaxAmount = Utils.getDividedValue(taxRatesInfo.nTaxRate) * info.nSales * prodDetlInfo.nConsumerUnit;
									}
									else if( taxRatesInfo.nTaxMethod == 2 )
									{
										nTaxAmount = (info.nSales * (info.nBasePrice - info.nDiscountVal) + info.nTaxAmount) / 100 * Utils.getDividedValue(taxRatesInfo.nTaxRate) ;
										nPrintTaxAmount = (info.nSales * (info.nBasePrice) + info.nTaxAmount) / 100 * Utils.getDividedValue(taxRatesInfo.nTaxRate);
									}

									if(taxRatesInfo.strTaxType.equals("S"))
									{
										ProdLocnInfo locnInfo = dbAdapter.getProdLocnInfo(taxRatesInfo.strTaxRateNumber);
										List<TaxRatesInfo> gstOnCDSRatesInfoList = dbAdapter.getTaxRatesInfoList(Global.gCurCustomerInfo.strTaxCode, taxRatesInfo.strTaxRateNumber);
										for(TaxRatesInfo gstOnCDSRatesInfo : gstOnCDSRatesInfoList)
										{
											if(gstOnCDSRatesInfo.nTaxMethod == 2 && gstOnCDSRatesInfo.strTaxRateCode.equals("GST") )
											{
												float nGSTOnCDSTaxAmount = Utils.getDividedValue(taxRatesInfo.nTaxRate) / 100 * Utils.getDividedValue(gstOnCDSRatesInfo.nTaxRate) * info.nSales * prodDetlInfo.nConsumerUnit;
												fGSTOnCDS += nGSTOnCDSTaxAmount;

												UTaxInfo uTaxInfo = new UTaxInfo();
												uTaxInfo.nTrxnNo = info.nTrxnNo;
												uTaxInfo.nTrxnType = info.nTrxnType;
												uTaxInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
												uTaxInfo.nLocnNo = locnInfo.nLocnNo;
												uTaxInfo.strItemNo = taxRatesInfo.strTaxRateNumber;
												uTaxInfo.strTaxRateCode = gstOnCDSRatesInfo.strTaxRateCode;
												uTaxInfo.nTaxAmount = nGSTOnCDSTaxAmount;
												uTaxInfo.strCumulativeFlag = gstOnCDSRatesInfo.strCumulativeFlag;
												uTaxInfo.nTaxRate = gstOnCDSRatesInfo.nTaxRate;
												uTaxInfo.strTaxRateNumber = gstOnCDSRatesInfo.strTaxRateNumber;
												uTaxInfo.nReasonCode = 0;
												gstOnCDSTaxRateInfoList.add(uTaxInfo);
											}
										}
									}

									info.nTaxAmount += nTaxAmount;
									info.nPrintTaxAmount += nPrintTaxAmount;

									if(taxRatesInfo.strTaxRateCode.equals("GST"))
									{
										bAppliedGST = true;
									}

									UTaxInfo uTaxInfo = new UTaxInfo();
									uTaxInfo.nTrxnNo = info.nTrxnNo;
									uTaxInfo.nTrxnType = info.nTrxnType;
									uTaxInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
									uTaxInfo.nLocnNo = info.nLocnNo;
									uTaxInfo.strItemNo = info.strItemNo;
									uTaxInfo.strTaxRateCode = taxRatesInfo.strTaxRateCode;
									uTaxInfo.nTaxAmount = nTaxAmount;
									uTaxInfo.strCumulativeFlag = taxRatesInfo.strCumulativeFlag;
									uTaxInfo.nTaxRate = taxRatesInfo.nTaxRate;
									uTaxInfo.strTaxRateNumber = taxRatesInfo.strTaxRateNumber;
									uTaxInfo.nReasonCode = 0;
									info.uTaxInfoList.add(uTaxInfo);
								}
								if(!bAppliedGST)
								{
									info.nTaxAmount += fGSTOnCDS;
									info.nPrintTaxAmount += fGSTOnCDS;
									info.uTaxInfoList.addAll(gstOnCDSTaxRateInfoList);
								}
							}
							else
							{
								if( Global.gUOrderHInfo.pickupList == null )
								{
									Global.gUOrderHInfo.pickupList = new ArrayList<>();
								}
								Global.gUOrderHInfo.pickupList.add(info);
								ProdLocnInfo prodLocnInfo = dbAdapter.getProdLocnInfo(info.strItemNo);
								ProdDetlInfo prodDetlInfo = dbAdapter.getProdDetl(info.strItemNo);
								if (prodLocnInfo != null && prodLocnInfo.strTaxFlag.equals("Y")) {
									info.strItemTaxCode = prodLocnInfo.strItemTaxCode;
								}

								if (info.nPromoType == Global.PROMO_TYPE_BUY_FREE_MULTI) {
									int nFreeQty = (int)(Utils.myDivider(info.nDumps, info.nPromoDiscount));
									Global.gUOrderHInfo.nTotalFree += nFreeQty;
								}

								//Calculation Tax Amount
								List<TaxRatesInfo> taxRatesInfoList = dbAdapter.getTaxRatesInfoList(Global.gCurCustomerInfo.strTaxCode, info.strItemNo);
								info.nTrxnNo = Global.gUOrderHInfo.nTrxnNo;
								info.nTaxAmount = 0;
								info.nPrintTaxAmount = 0;
								boolean bAppliedGST = false;
								float fGSTOnCDS = 0.0f;
								List<UTaxInfo> gstOnCDSTaxRateInfoList = new ArrayList<>();
								for(TaxRatesInfo taxRatesInfo : taxRatesInfoList)
								{
									float nTaxAmount = 0;
									float nPrintTaxAmount = 0;
									if(taxRatesInfo.nTaxMethod == 1) //Applying Cents Amount
									{
										nTaxAmount = Utils.getDividedValue(taxRatesInfo.nTaxRate) * info.nSales * prodDetlInfo.nConsumerUnit;
										nPrintTaxAmount = Utils.getDividedValue(taxRatesInfo.nTaxRate) * info.nSales * prodDetlInfo.nConsumerUnit;
									}
									else if( taxRatesInfo.nTaxMethod == 2 )
									{
										nTaxAmount = (info.nDumps * (info.nBasePrice - info.nDiscountVal) + info.nTaxAmount) / 100 * Utils.getDividedValue(taxRatesInfo.nTaxRate);
										nPrintTaxAmount = (info.nDumps * (info.nBasePrice) + info.nTaxAmount) / 100 * Utils.getDividedValue(taxRatesInfo.nTaxRate);
									}

									if(taxRatesInfo.strTaxType.equals("S"))
									{
										ProdLocnInfo locnInfo = dbAdapter.getProdLocnInfo(taxRatesInfo.strTaxRateNumber);
										List<TaxRatesInfo> gstOnCDSRatesInfoList = dbAdapter.getTaxRatesInfoList(Global.gCurCustomerInfo.strTaxCode, taxRatesInfo.strTaxRateNumber);
										for(TaxRatesInfo gstOnCDSRatesInfo : gstOnCDSRatesInfoList)
										{
											if(gstOnCDSRatesInfo.nTaxMethod == 2 && gstOnCDSRatesInfo.strTaxRateCode.equals("GST") )
											{
												float nGSTOnCDSTaxAmount = Utils.getDividedValue(taxRatesInfo.nTaxRate) / 100 * Utils.getDividedValue(gstOnCDSRatesInfo.nTaxRate) * info.nDumps * prodDetlInfo.nConsumerUnit;
												fGSTOnCDS += nGSTOnCDSTaxAmount;

												UTaxInfo uTaxInfo = new UTaxInfo();
												uTaxInfo.nTrxnNo = info.nTrxnNo;
												uTaxInfo.nTrxnType = info.nTrxnType;
												uTaxInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
												uTaxInfo.nLocnNo = locnInfo.nLocnNo;
												uTaxInfo.strItemNo = taxRatesInfo.strTaxRateNumber;
												uTaxInfo.strTaxRateCode = gstOnCDSRatesInfo.strTaxRateCode;
												uTaxInfo.nTaxAmount = nGSTOnCDSTaxAmount;
												uTaxInfo.strCumulativeFlag = gstOnCDSRatesInfo.strCumulativeFlag;
												uTaxInfo.nTaxRate = gstOnCDSRatesInfo.nTaxRate;
												uTaxInfo.strTaxRateNumber = gstOnCDSRatesInfo.strTaxRateNumber;
												uTaxInfo.nReasonCode = 0;
												gstOnCDSTaxRateInfoList.add(uTaxInfo);
											}
										}
									}

									info.nTaxAmount += nTaxAmount;
									info.nPrintTaxAmount += nPrintTaxAmount;

									if(taxRatesInfo.strTaxRateCode.equals("GST"))
									{
										bAppliedGST = true;
									}

									UTaxInfo uTaxInfo = new UTaxInfo();
									uTaxInfo.nTrxnNo = info.nTrxnNo;
									uTaxInfo.nTrxnType = info.nTrxnType;
									uTaxInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
									uTaxInfo.nLocnNo = info.nLocnNo;
									uTaxInfo.strItemNo = info.strItemNo;
									uTaxInfo.strTaxRateCode = taxRatesInfo.strTaxRateCode;
									uTaxInfo.nTaxAmount = nTaxAmount;
									uTaxInfo.strCumulativeFlag = taxRatesInfo.strCumulativeFlag;
									uTaxInfo.nTaxRate = taxRatesInfo.nTaxRate;
									uTaxInfo.strTaxRateNumber = taxRatesInfo.strTaxRateNumber;
									uTaxInfo.nReasonCode = 0;
									info.uTaxInfoList.add(uTaxInfo);
								}
								if(!bAppliedGST)
								{
									info.nTaxAmount += fGSTOnCDS;
									info.nPrintTaxAmount += fGSTOnCDS;
									if(!bAppliedGST)
									{
										info.nTaxAmount += fGSTOnCDS;
										info.nPrintTaxAmount += fGSTOnCDS;
										info.uTaxInfoList.addAll(gstOnCDSTaxRateInfoList);
									}
								}
							}
						}
					}
					else
					{
						if( info.nTrxnType == Global.TRXN_CONTAINER_SALE )
							dInfo.nSales = info.nSales;
						else
							dInfo.nDumps = info.nDumps;
					}
				}
				dbAdapter.close();
				dismiss();
			}
		});

		((TextView) findViewById(R.id.txtTitle)).setText(Str.ContainerTracking[Global.LANG].toUpperCase());
		((Button) findViewById(R.id.btnReturn)).setText(Str.Return[Global.LANG]);
		((Button) findViewById(R.id.btnConfirm)).setText(Str.Confirm[Global.LANG]);
		mTxtNoData.setText(Str.No_data_has_been_found[Global.LANG]);

		if (mContainerList.size() > 0) {
			refreshPage();
		} else {
			loadData();
		}
	}

	private void loadData() {
		if( Global.gUOrderHInfo != null && Global.gUOrderHInfo.deliveryList != null && Global.gUOrderHInfo.deliveryList.size() > 0 )
		{
			for(UOrderDInfo info : Global.gUOrderHInfo.deliveryList )
			{
				mSaleMap.put(info.strItemNo, info);
			}
		}

		if( Global.gUOrderHInfo != null && Global.gUOrderHInfo.pickupList != null && Global.gUOrderHInfo.pickupList.size() > 0 )
		{
			for(UOrderDInfo info : Global.gUOrderHInfo.pickupList )
			{
				mDumpMap.put(info.strItemNo, info);
			}
		}

		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		List<ProdDetlInfo> prodDetlList = dbAdapter.getProdDetlContainerList();
		dbAdapter.close();
		for (ProdDetlInfo prodDetlInfo : prodDetlList) {
			UOrderDInfo uOrderDInfo = mSaleMap.get(prodDetlInfo.strItemNo);
			if( uOrderDInfo == null )
			{
				uOrderDInfo = new UOrderDInfo();
				uOrderDInfo.strItemNo = prodDetlInfo.strItemNo;
				uOrderDInfo.strProdGroup = prodDetlInfo.strProdGrp;
				uOrderDInfo.strShortDesc = prodDetlInfo.strShortDesc;
			}
			else
			{
				uOrderDInfo = uOrderDInfo.duplicate();
			}
			uOrderDInfo.nTrxnType = Global.TRXN_CONTAINER_SALE;
			mContainerList.add(uOrderDInfo);
		}
		for (ProdDetlInfo prodDetlInfo : prodDetlList) {
			UOrderDInfo uOrderDInfo = mDumpMap.get(prodDetlInfo.strItemNo);
			if( uOrderDInfo == null )
			{
				uOrderDInfo = new UOrderDInfo();
				uOrderDInfo.strItemNo = prodDetlInfo.strItemNo;
				uOrderDInfo.strProdGroup = prodDetlInfo.strProdGrp;
				uOrderDInfo.strShortDesc = prodDetlInfo.strShortDesc;
			}
			else
			{
				uOrderDInfo = uOrderDInfo.duplicate();
			}
			uOrderDInfo.nTrxnType = Global.TRXN_CONTAINER_DUMP;
			mContainerList.add(uOrderDInfo);
		}
		refreshPage();
	}

	private void refreshPage()
	{
		if (mContainerList.size() == 0) {
			mLayoutContainers.setVisibility(View.GONE);
			mTxtNoData.setVisibility(View.VISIBLE);
			return;
		}
		mTxtNoData.setVisibility(View.GONE);
		mLayoutContainers.setVisibility(View.VISIBLE);
		mLayoutContainers.removeAllViews();
		for (int i=0; i<mContainerList.size(); i++) {
			final int nIdx = i;
			UOrderDInfo info = mContainerList.get(i);
			View childView = getLayoutInflater().inflate(R.layout.row_container_tracking, null);
			mLayoutContainers.addView(childView);

			TextView txtTitle = (TextView) childView.findViewById(R.id.txtTitle);
			TextView txtItemNo = (TextView) childView.findViewById(R.id.txtItemNo);
			TextView txtShortDesc = (TextView) childView.findViewById(R.id.txtShortDesc);
			EditText editQty = (EditText) childView.findViewById(R.id.editQty);

			if (nIdx == 0) {
				txtTitle.setText(Str.ContainersIntoStore[Global.LANG]);
				txtTitle.setVisibility(View.VISIBLE);
			} else if (nIdx * 2 == mContainerList.size()) {
				txtTitle.setText(Str.ContainersOutOfStore[Global.LANG]);
				txtTitle.setVisibility(View.VISIBLE);
			} else {
				txtTitle.setVisibility(View.GONE);
			}

			txtItemNo.setText(Utils.getDisplayItemCode(mContext, info.strItemNo));
			txtShortDesc.setText(info.strShortDesc);
			editQty.setText(String.valueOf(info.nTrxnType == Global.TRXN_CONTAINER_SALE ? info.nSales : info.nDumps));

			editQty.addTextChangedListener(new TextWatcher() {
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
				}
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				}
				@Override
				public void afterTextChanged(Editable s) {
					int nQuantity = Utils.parseIntFromString(s.toString());
					if (mContainerList.get(nIdx).nTrxnType == Global.TRXN_CONTAINER_SALE) {
						mContainerList.get(nIdx).nSales = nQuantity;
					} else {
						mContainerList.get(nIdx).nDumps = nQuantity;
					}
				}
			});
		}
	}
}

