package dsd.dsdvansales.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import dsd.dsdvansales.R;
import dsd.dsdvansales.db.DbAdapter;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.model.ARHeaderInfo;
import dsd.dsdvansales.model.StatusInfo;

public class DetailCollectionFragment extends BaseFragment implements View.OnClickListener {

	private float mCollectionRequired = 0;
	private boolean mIsRoundPrice = false;
	private LinearLayout mLayoutItem = null;
	private CheckBox mChkAllBox = null;
	private boolean mCheckAllStatus = true;
	private boolean bIsFromDeliverySummary = false;

	List<DetailCollectionItem> mCollectionItemList = new ArrayList<>();

	DeliverySummaryFragment previousFragment;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootView = inflater.inflate(R.layout.fragment_detail_collection_fragment, container, false);
		commonInit();
		return mRootView;
	}

	public void setPreviousFragment(DeliverySummaryFragment fragment)
	{
		previousFragment = fragment;
	}

	@Override
	public void onResume() {
		super.onResume();
		Global.gFragIdx = Global.FRAG_DETAIL_COLLECTION;
	}


	float getRoundedValue(float value)
	{
		boolean bCentsRount;
		if( Utils.getStringSetting(mContext, Global.PREF_ROUTE_CENTSROUND).trim().equals("1") &&
				(Global.gCurCustomerInfo.strPayType.trim().equals("1") || Global.gCurCustomerInfo.strPayType.trim().equals("2")))
		{
			bCentsRount = true;
		}
		else if(Utils.getStringSetting(mContext, Global.PREF_ROUTE_CENTSROUND).trim().equals("2"))
		{
			bCentsRount = true;
		}
		else
		{
			bCentsRount = false;
		}
		if( bCentsRount )
		{
			float fNeg = 1;
			if(value < 0)
			{
				fNeg = -1;
			}
			int nTotal = Math.round(Utils.getRoundedValue(value * fNeg) * Global.MULTIPLE_VALUE);
			int nOffSet = nTotal % 5000;
			int nInvoiceTotal = ((nTotal / 5000) + ((Math.abs(nOffSet) >= 2500) ? 1 : 0)) * 5000;
			nOffSet = nTotal % 1000;
			nTotal = ((nTotal / 1000) + ((Math.abs(nOffSet) >= 500) ? 1 : 0)) * 1000;
			nOffSet = nInvoiceTotal - nTotal;

			float dValue = nInvoiceTotal / (Global.MULTIPLE_VALUE * 1f);
			return fNeg * dValue;
		}
		else
		{
			return Utils.getRoundedValue(value);
		}
	}

	@Override
	public void initView() {
		mRootView.findViewById(R.id.btnBack).setOnClickListener(this);
		mRootView.findViewById(R.id.btnCompleted).setOnClickListener(this);
		((Button)mRootView.findViewById(R.id.btnCompleted)).setText(Str.Done[Global.LANG]);
		mChkAllBox = (CheckBox)mRootView.findViewById(R.id.chkAll);
		/*if(Global.gCurCustomerInfo.strPayType.equals("3"))
		{
			mChkAllBox.setClickable(false);
		}
		else*/
		{
			mChkAllBox.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mCheckAllStatus = mChkAllBox.isChecked();
					if( mCheckAllStatus )
					{
						for(DetailCollectionItem item : mCollectionItemList)
						{
							if(Global.gCurHeaderInfo != null && (Global.gCurCustomerInfo.strPayType.equals("3") || Global.gCurCustomerInfo.strPayType.equals("4") || Global.gCurCustomerInfo.strPayType.equals("5") | Global.gCurCustomerInfo.strPayType.equals("C")))
							{
								if(Global.gCurHeaderInfo != null && Global.gCurHeaderInfo == item.arHeaderInfo)
								{
									item.status = true;
									item.checkBox.setChecked(item.status);
								}
							}
							else
							{
								boolean bPayType = false;
								if(Global.gCurCustomerInfo.strPayType.equals("$") ||
										Global.gCurCustomerInfo.strPayType.equals("1") ||
										Global.gCurCustomerInfo.strPayType.equals("2"))
								{
									if(item.arHeaderInfo.strARTrxnType.equals("CRN") || item.arHeaderInfo.strARTrxnType.equals("INV"))
									{
										bPayType = true;
									}
								}
								else if(Global.gCurCustomerInfo.strPayType.equals("B"))
								{
									if(!item.arHeaderInfo.strARTrxnType.equals("CRN") && !item.arHeaderInfo.strARTrxnType.equals("INV"))
									{
										bPayType = true;
									}
								}
								if(!bPayType)
								{
									item.status = true;
									item.checkBox.setChecked(item.status);
								}
							}

						}
					}
					else
					{
						for(DetailCollectionItem item: mCollectionItemList)
						{
							if(Global.gCurHeaderInfo != null && (Global.gCurCustomerInfo.strPayType.equals("3") || Global.gCurCustomerInfo.strPayType.equals("4") || Global.gCurCustomerInfo.strPayType.equals("5") | Global.gCurCustomerInfo.strPayType.equals("C")))
							{
								if(Global.gCurHeaderInfo != null && Global.gCurHeaderInfo == item.arHeaderInfo)
								{
									item.status = false;
									item.checkBox.setChecked(item.status);
								}
							}
							else
							{
								boolean bPayType = false;
								if(Global.gCurCustomerInfo.strPayType.equals("$") ||
										Global.gCurCustomerInfo.strPayType.equals("1") ||
										Global.gCurCustomerInfo.strPayType.equals("2"))
								{
									if(item.arHeaderInfo.strARTrxnType.equals("CRN") || item.arHeaderInfo.strARTrxnType.equals("INV"))
									{
										bPayType = true;
									}
								}
								else if(Global.gCurCustomerInfo.strPayType.equals("B"))
								{
									if(!item.arHeaderInfo.strARTrxnType.equals("CRN") && !item.arHeaderInfo.strARTrxnType.equals("INV"))
									{
										bPayType = true;
									}
								}
								if(!bPayType)
								{
									item.status = false;
									item.checkBox.setChecked(item.status);
								}
							}
						}
					}
					float dValue = 0.0f;
					for(DetailCollectionItem detailCollectionItem: mCollectionItemList)
					{
						if(detailCollectionItem.status)
							dValue += detailCollectionItem.arHeaderInfo.nTrxnAmount;
					}
					mCollectionRequired = getRoundedValue(dValue / Global.MULTIPLE_VALUE);
					((TextView) mRootView.findViewById(R.id.txtCollectionRequired)).setText(getRoundedFormattedPrice(mCollectionRequired));
				}
			});
		}

		boolean bCentsRount;
		if( Utils.getStringSetting(mContext, Global.PREF_ROUTE_CENTSROUND).trim().equals("1") &&
				(Global.gCurCustomerInfo.strPayType.trim().equals("1") || Global.gCurCustomerInfo.strPayType.trim().equals("2")))
		{
			bCentsRount = true;
		}
		else if(Utils.getStringSetting(mContext, Global.PREF_ROUTE_CENTSROUND).trim().equals("2"))
		{
			bCentsRount = true;
		}
		else
		{
			bCentsRount = false;
		}
		if( bCentsRount )
		{
			mIsRoundPrice = true;
		}
		else
		{
			mIsRoundPrice = false;
		}
		((TextView) mRootView.findViewById(R.id.txtCustomerName)).setText(Global.gCurCustomerInfo.strName);
		mLayoutItem = (LinearLayout)mRootView.findViewById(R.id.layoutItem);

		bIsFromDeliverySummary = getArguments().getBoolean(Global.EXTRA_IS_FROM_DELIVERY_SUMMARY);

		loadData();
		refreshLanguage();


	}

	@Override
	public void loadData() {

		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		List<ARHeaderInfo> arHeaderInfos;
		arHeaderInfos = dbAdapter.getARHeaderInfo(Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo);

		dbAdapter.close();
		float dTotalRemaining = 0;
		if( arHeaderInfos.size() > 0 && bIsFromDeliverySummary == false)
		{
			for(ARHeaderInfo arHeaderInfo : arHeaderInfos)
			{
				if( arHeaderInfo.nProcessStatus < 2 )
					dTotalRemaining += getRoundedValue(arHeaderInfo.nTrxnAmount * 1.0f / Global.MULTIPLE_VALUE);
			}
		}

		if( Global.gCurHeaderInfo != null && Global.gCurHeaderInfo.nProcessStatus < 2)
		{
			mCollectionRequired = dTotalRemaining + getRoundedValue(Global.gCurHeaderInfo.nTrxnAmount * 1.0f / Global.MULTIPLE_VALUE);
		}

		//Getting Data From the DB.
		//1 Get Data from ARHEADER.

		float dValue = 0.0f;
		if (bIsFromDeliverySummary == false) {
			for(ARHeaderInfo info : arHeaderInfos)
			{
				if( info.nProcessStatus == 3 ) //Processed
				{
					continue;
				}
				else if( info.nProcessStatus == 2 )
				{
					DetailCollectionItem item = new DetailCollectionItem();
					item.arHeaderInfo = info;
					item.status = false;
					mCollectionItemList.add(item);
				}
				else
				{
					DetailCollectionItem item = new DetailCollectionItem();
					item.arHeaderInfo = info;
					item.status = true;
					mCollectionItemList.add(item);
					dValue += item.arHeaderInfo.nTrxnAmount;
				}
			}
		}

		if( Global.gCurHeaderInfo != null )
		{
			if( Global.gCurHeaderInfo.nProcessStatus == 2 )
			{
				DetailCollectionItem item = new DetailCollectionItem();
				item.arHeaderInfo = Global.gCurHeaderInfo;
				item.status = false;
				mCollectionItemList.add(item);
			}
			else if (Global.gCurHeaderInfo.nProcessStatus != 3)
			{
				DetailCollectionItem item = new DetailCollectionItem();
				item.arHeaderInfo = Global.gCurHeaderInfo;
				item.status = true;
				dValue += item.arHeaderInfo.nTrxnAmount;
				mCollectionItemList.add(item);
			}
		}

		mCollectionRequired = dValue / Global.MULTIPLE_VALUE;
		((TextView) mRootView.findViewById(R.id.txtCollectionRequired)).setText(getRoundedFormattedPrice(mCollectionRequired));
		refreshPage();
	}
	
	String getRoundedFormattedPrice(float value)
	{
		boolean bCentsRount;
		if( Utils.getStringSetting(mContext, Global.PREF_ROUTE_CENTSROUND).trim().equals("1") &&
				(Global.gCurCustomerInfo.strPayType.trim().equals("1") || Global.gCurCustomerInfo.strPayType.trim().equals("2")))
		{
			bCentsRount = true;
		}
		else if(Utils.getStringSetting(mContext, Global.PREF_ROUTE_CENTSROUND).trim().equals("2"))
		{
			bCentsRount = true;
		}
		else
		{
			bCentsRount = false;
		}

		if(bCentsRount)
		{
			float fNeg = 1;
			if(value < 0)
			{
				fNeg = -1;
			}
			int nTotal = Math.round(Utils.getRoundedValue(value * fNeg) * Global.MULTIPLE_VALUE);
			int nOffSet = nTotal % 5000;
			int nInvoiceTotal = ((nTotal / 5000) + ((Math.abs(nOffSet) >= 2500) ? 1 : 0)) * 5000;
			nOffSet = nTotal % 1000;
			nInvoiceTotal *= fNeg;
			nOffSet *= fNeg;

			float dValue = nInvoiceTotal / (Global.MULTIPLE_VALUE * 1f);
			String sValue;
			if( dValue >= 0 )
				sValue = String.format(Locale.ENGLISH, "%s" + "%." + Utils.getIntSetting(mContext, Global.PREF_DECIMAL) + "f", Global.gCurrencySymbol, dValue);
			else
				sValue = String.format(Locale.ENGLISH, "-%s" + "%." + Utils.getIntSetting(mContext, Global.PREF_DECIMAL) + "f", Global.gCurrencySymbol, Math.abs(dValue));
			return sValue;
		}
		else
		{
			if(value >= 0)
				return String.format(Locale.ENGLISH, "%s" + "%." + Utils.getIntSetting(mContext, Global.PREF_DECIMAL) + "f", Global.gCurrencySymbol, value);
			else
				return String.format(Locale.ENGLISH, "-%s" + "%." + Utils.getIntSetting(mContext, Global.PREF_DECIMAL) + "f", Global.gCurrencySymbol, Math.abs(value));
		}
		
	}

	String getRoundedFormattedPriceWithoutCurrency(float value)
	{
		boolean bCentsRount;
		if( Utils.getStringSetting(mContext, Global.PREF_ROUTE_CENTSROUND).trim().equals("1") &&
				(Global.gCurCustomerInfo.strPayType.trim().equals("1") || Global.gCurCustomerInfo.strPayType.trim().equals("2")))
		{
			bCentsRount = true;
		}
		else if(Utils.getStringSetting(mContext, Global.PREF_ROUTE_CENTSROUND).trim().equals("2"))
		{
			bCentsRount = true;
		}
		else
		{
			bCentsRount = false;
		}
		if(bCentsRount)
		{
			float fNeg = 1;
			if(value < 0)
			{
				fNeg = -1;
			}
			int nTotal = Math.round(Utils.getRoundedValue(value * fNeg) * Global.MULTIPLE_VALUE);
			int nOffSet = nTotal % 5000;
			int nInvoiceTotal = ((nTotal / 5000) + ((Math.abs(nOffSet) >= 2500) ? 1 : 0)) * 5000;
			nOffSet = nTotal % 1000;
			nInvoiceTotal *= fNeg;
			nOffSet *= fNeg;

			float dValue = nInvoiceTotal / (Global.MULTIPLE_VALUE * 1f);
			String sValue;
			if( dValue >= 0 )
				sValue = String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(mContext, Global.PREF_DECIMAL) + "f", dValue);
			else
				sValue = String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(mContext, Global.PREF_DECIMAL) + "f", Math.abs(dValue));
			return sValue;
		}
		else
		{
			if(value >= 0)
				return String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(mContext, Global.PREF_DECIMAL) + "f", value);
			else
				return String.format(Locale.ENGLISH, "-" + "%." + Utils.getIntSetting(mContext, Global.PREF_DECIMAL) + "f", Math.abs(value));
		}

	}


	
	@Override
	public void refreshPage() {
		((TextView) mRootView.findViewById(R.id.txtCollectionRequired)).setText(getRoundedFormattedPrice(mCollectionRequired));

		mLayoutItem.removeAllViews();
		mCheckAllStatus = true;
		for(final DetailCollectionItem item : this.mCollectionItemList)
		{
			View childView = mContext.getLayoutInflater().inflate(R.layout.row_detail_collection, null);
			mLayoutItem.addView(childView);
			item.checkBox = (CheckBox)childView.findViewById(R.id.chkSelect);
			item.checkBox.setChecked(item.status);
			item.checkBox.setTag(item);

			if(Global.gCurHeaderInfo != null && (Global.gCurCustomerInfo.strPayType.equals("3") || Global.gCurCustomerInfo.strPayType.equals("4") || Global.gCurCustomerInfo.strPayType.equals("5") || Global.gCurCustomerInfo.strPayType.equals("C")))
			{
				if(item.arHeaderInfo == Global.gCurHeaderInfo)
				{
					if( item.status == false )
						mCheckAllStatus = false;
					else
						mCheckAllStatus = true;

					item.checkBox.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							DetailCollectionItem item = (DetailCollectionItem)v.getTag();
							item.status = item.checkBox.isChecked();
							if( !item.status )
							{
								mCheckAllStatus = false;
								mChkAllBox.setChecked(false);
							}
							else
							{
								mCheckAllStatus = true;
								mChkAllBox.setChecked(true);
							}

							float dValue = 0.0f;
							for(DetailCollectionItem detailCollectionItem: mCollectionItemList)
							{
								if(detailCollectionItem.status)
									dValue += detailCollectionItem.arHeaderInfo.nTrxnAmount;
							}
							mCollectionRequired = getRoundedValue(dValue / Global.MULTIPLE_VALUE);
							((TextView) mRootView.findViewById(R.id.txtCollectionRequired)).setText(getRoundedFormattedPrice(mCollectionRequired));
						}
					});
				}
				else
				{
					item.checkBox.setClickable(false);
					item.checkBox.setEnabled(false);
				}

			}
			else
			{
				boolean bPayType = false;
				if(Global.gCurCustomerInfo.strPayType.equals("$") ||
						Global.gCurCustomerInfo.strPayType.equals("1") ||
						Global.gCurCustomerInfo.strPayType.equals("2"))
				{
					if(item.arHeaderInfo.strARTrxnType.equals("CRN") || item.arHeaderInfo.strARTrxnType.equals("INV"))
					{
						bPayType = true;
					}
				}
				else if(Global.gCurCustomerInfo.strPayType.equals("B"))
				{
					if(!item.arHeaderInfo.strARTrxnType.equals("CRN") && !item.arHeaderInfo.strARTrxnType.equals("INV"))
					{
						bPayType = true;
					}
				}

				if(bPayType)
				{
					item.checkBox.setClickable(false);
					item.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
						@Override
						public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
							item.checkBox.setChecked(true);
						}
					});
				}

				if( !bPayType && item.status == false )
					mCheckAllStatus = false;

				item.checkBox.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						DetailCollectionItem item = (DetailCollectionItem)v.getTag();
						item.status = item.checkBox.isChecked();
						if( !item.status )
						{
							mCheckAllStatus = false;
							mChkAllBox.setChecked(false);
						}
						else
						{
							mCheckAllStatus = true;
							for(DetailCollectionItem detailCollectionItem : mCollectionItemList)
							{
								if( detailCollectionItem.status == false )
								{
									mCheckAllStatus = false;
									break;
								}
							}
							if( mCheckAllStatus )
							{
								mChkAllBox.setChecked(true);
							}
							else
							{
								mChkAllBox.setChecked(false);
							}
						}

						float dValue = 0.0f;
						for(DetailCollectionItem detailCollectionItem: mCollectionItemList)
						{
							if(detailCollectionItem.status)
								dValue += detailCollectionItem.arHeaderInfo.nTrxnAmount;
						}
						mCollectionRequired = getRoundedValue(dValue / Global.MULTIPLE_VALUE);
						((TextView) mRootView.findViewById(R.id.txtCollectionRequired)).setText(getRoundedFormattedPrice(mCollectionRequired));
					}
				});
			}

			String strInvoiceDate = "", strInvoiceNumber = "", strArType = "", strInvoice = "", strCurrency = "", strAmount = "0.00";
			boolean bNeg = false;

			SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
			Date date = null;
			try {
				date = format.parse(item.arHeaderInfo.strInvDate);
				format = new SimpleDateFormat(Utils.getDateFormat(mContext, "dd/MM/yy"));
				strInvoiceDate = format.format(date);
			} catch (ParseException e) {
				e.printStackTrace();
			}

			if (item.arHeaderInfo.strDescription != null && !item.arHeaderInfo.strDescription.isEmpty()) {
				strInvoiceNumber = item.arHeaderInfo.strDescription;
			}
			else {
				strInvoiceNumber = item.arHeaderInfo.strInvNo;
			}

			if( item.arHeaderInfo.strARTrxnType == null || item.arHeaderInfo.strARTrxnType.length() == 0 )
			{
				if( item.arHeaderInfo.nTrxnAmount > 0 )
					strArType = "INV";
				else
					strArType = "CRN";
			}
			else
			{
				strArType = item.arHeaderInfo.strARTrxnType;
			}

			if( item.arHeaderInfo.nTrxnAmount < 0 )
			{
				bNeg = false;
				strCurrency = "-"  + Global.gCurrencySymbol;
			}
			else
			{
				bNeg = true;
				strCurrency = Global.gCurrencySymbol;
			}

			strAmount = getRoundedFormattedPriceWithoutCurrency(item.arHeaderInfo.nTrxnAmount * 1.0f / Global.MULTIPLE_VALUE);


			((TextView)childView.findViewById(R.id.txtInvoiceDate)).setText(strInvoiceDate);
			((TextView)childView.findViewById(R.id.txtItemType)).setText(strArType);
			((TextView)childView.findViewById(R.id.txtInvNumber)).setText(strInvoiceNumber);
			((TextView)childView.findViewById(R.id.lblCurrencySymbol)).setText(strCurrency);
			((TextView)childView.findViewById(R.id.editPrice)).setText(strAmount);
			if( bNeg )
			{
				((TextView)childView.findViewById(R.id.txtInvoiceDate)).setTextColor(mContext.getResources().getColor(R.color.black_color));
				((TextView)childView.findViewById(R.id.txtItemType)).setTextColor(mContext.getResources().getColor(R.color.black_color));
				((TextView)childView.findViewById(R.id.txtInvNumber)).setTextColor(mContext.getResources().getColor(R.color.black_color));
				((TextView)childView.findViewById(R.id.lblCurrencySymbol)).setTextColor(mContext.getResources().getColor(R.color.black_color));
				((TextView)childView.findViewById(R.id.editPrice)).setTextColor(mContext.getResources().getColor(R.color.black_color));
			}
			else
			{
				((TextView)childView.findViewById(R.id.txtInvoiceDate)).setTextColor(mContext.getResources().getColor(R.color.red_color));
				((TextView)childView.findViewById(R.id.txtItemType)).setTextColor(mContext.getResources().getColor(R.color.red_color));
				((TextView)childView.findViewById(R.id.txtInvNumber)).setTextColor(mContext.getResources().getColor(R.color.red_color));
				((TextView)childView.findViewById(R.id.lblCurrencySymbol)).setTextColor(mContext.getResources().getColor(R.color.red_color));
				((TextView)childView.findViewById(R.id.editPrice)).setTextColor(mContext.getResources().getColor(R.color.red_color));
			}
		}

		mChkAllBox.setChecked(mCheckAllStatus);
	}

	@Override
	public void refreshLanguage() {
		((TextView) mRootView.findViewById(R.id.lblTitle)).setText(Str.DetailCollection[Global.LANG].toUpperCase());
		((TextView) mRootView.findViewById(R.id.lblCollectionRequired)).setText(Str.CollectionRequired[Global.LANG]);
	}

	@Override
	public void receiveBroadcast(String strAction, Intent intent) {
	}


	private void doComplete() {
		// check if user selected only one invoice if payType = 3
		if (Global.gCurCustomerInfo != null && Global.gCurCustomerInfo.strPayType.equals("3")) {
			int nSelectedInvoices = 0;
			for(DetailCollectionItem item : mCollectionItemList)
			{
				if (item.arHeaderInfo.strARTrxnType.equals("INV")) {
					if (item.status) {
						nSelectedInvoices += 1;
					}
				}
			}
			if (nSelectedInvoices != 1) {
				Utils.printDebug(mContext, "You should be able to select only one invoice.");
				return;
			}
		}

		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		for(DetailCollectionItem item : mCollectionItemList)
		{
			if( item.arHeaderInfo.nIdx >= 0 )
			{
				if( item.status )
				{
					item.arHeaderInfo.nProcessStatus = 1;
				}
				else
				{
					item.arHeaderInfo.nProcessStatus = 2;
				}
				dbAdapter.updateARHeaderInfo(item.arHeaderInfo);
			}
			else
			{
				if( item.status )
				{
					item.arHeaderInfo.nProcessStatus = 1;
				}
				else
				{
					item.arHeaderInfo.nProcessStatus = 2;
				}
			}

		}
		dbAdapter.close();
		goBack();
	}

	private void goBack() {
		try
		{
			DbAdapter dbAdapter = new DbAdapter(mContext);
			dbAdapter.open();
			StatusInfo info = dbAdapter.getStatusInfo(Global.gCurCustomerInfo.nCustNo);
			dbAdapter.close();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		if( previousFragment != null )
		{
			previousFragment.setResetCollectionValue();
		}
		mContext.onBackPressed();
	}

	boolean m_bClicked = false;
	@Override
	public void onClick(View v) {
		if( m_bClicked ) return;
		m_bClicked = true;
		switch (v.getId()) {
			case R.id.btnBack:
				mContext.onBackPressed();
				break;
			case R.id.btnCompleted:
				doComplete();
				break;
		}
		m_bClicked = false;
	}

	public class DetailCollectionItem
	{
		public boolean status = false;
		public ARHeaderInfo arHeaderInfo;
		public CheckBox checkBox;
	}
}