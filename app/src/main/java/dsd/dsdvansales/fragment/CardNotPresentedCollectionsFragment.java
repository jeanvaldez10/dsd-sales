package dsd.dsdvansales.fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import dsd.dsdvansales.R;
import dsd.dsdvansales.activities.MintIFrameActivity;
import dsd.dsdvansales.db.DbAdapter;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.model.ARHeaderInfo;
import dsd.dsdvansales.model.StatusInfo;
import dsd.dsdvansales.model.TransactionInfo;
import dsd.dsdvansales.model.UARPaymentInfo;
import dsd.dsdvansales.model.UCollectionsDInfo;

public class CardNotPresentedCollectionsFragment extends BaseFragment implements View.OnClickListener {

	List<DetailCollectionItem> mCollectionItemList = new ArrayList<>();
	private int mSelectedIndex = -1;

	private ListView mListView;
	ListAdapter mListAdapter;

	float mTotalRemaining = 0f;

	//DeliverySummaryFragment previousFragment;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootView = inflater.inflate(R.layout.fragment_card_not_presented_collections, container, false);
		commonInit();
		return mRootView;
	}

//	public void setPreviousFragment(DeliverySummaryFragment fragment)
//	{
//		previousFragment = fragment;
//	}

	@Override
	public void onResume() {
		super.onResume();
		Global.gFragIdx = Global.FRAG_CARD_NOT_PRESENTED_COLLECTIONS;
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
		mRootView.findViewById(R.id.btnPay).setOnClickListener(this);
		((Button)mRootView.findViewById(R.id.btnPay)).setText(Str.Pay[Global.LANG]);

		((TextView) mRootView.findViewById(R.id.txtCustomerName)).setText(Global.gCurCustomerInfo.strName);

		mListView = mRootView.findViewById(R.id.list);
		mListAdapter = new ListAdapter();
		mListView.setAdapter(mListAdapter);

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

		//Getting Data From the DB.
		//1 Get Data from ARHEADER.

		mCollectionItemList.clear();

		float dTotalRemaining = 0;
		for(ARHeaderInfo info : arHeaderInfos)
		{
			if( info.nProcessStatus == 1 )
			{
				if (info.strUser1.equals(Global.AR_HEADER_PAID)) {
					continue;
				}
				DetailCollectionItem item = new DetailCollectionItem();
				item.arHeaderInfo = info;
				mCollectionItemList.add(item);
				dTotalRemaining += info.nTrxnAmount * 1.0 / Global.MULTIPLE_VALUE;
			}
		}

		float fCash = Global.gUARTotalInfo.nTotalCash;
		float fCheque = Global.gUARPaymentChequeInfo.nTrxnAmount;
		float fCard = Global.gUARPaymentCardInfo.nTrxnAmount;
		float fEFT = Global.gUARPaymentEFTInfo.nTrxnAmount;

		float fTotalCollected = fCash + fCheque + fCard + fEFT;
		mTotalRemaining = dTotalRemaining - fTotalCollected;

		if (!mCollectionItemList.isEmpty()) {
			mSelectedIndex = 0;
		}
		else {
			mSelectedIndex = -1;
		}

		if (mCollectionItemList.isEmpty() || mTotalRemaining == 0) {
			Toast.makeText(mContext, Str.NoCardNotPresentedCollectionItem[Global.LANG], Toast.LENGTH_SHORT).show();
			new Handler(Looper.getMainLooper()).postDelayed(() -> {
				goBack();
			}, 200);
			return;
		}

		refreshPage();
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
		mListAdapter.notifyDataSetChanged();
	}

	@Override
	public void refreshLanguage() {
		((TextView) mRootView.findViewById(R.id.lblTitle)).setText(Str.CardNotPresentedCollections[Global.LANG].toUpperCase());
	}

	@Override
	public void receiveBroadcast(String strAction, Intent intent) {
	}

	public void doPayCollectionItem() {
		if (mSelectedIndex < 0 || mSelectedIndex >= mCollectionItemList.size()) {
			return;
		}

		DetailCollectionItem item = mCollectionItemList.get(mSelectedIndex);

		String strInvoiceNumber = item.arHeaderInfo.strInvNo;
		String strArType = "";
		String strCurrency = "";

		if (item.arHeaderInfo.strARTrxnType == null || item.arHeaderInfo.strARTrxnType.length() == 0) {
			if (item.arHeaderInfo.nTrxnAmount > 0)
				strArType = "INV";
			else
				strArType = "CRN";
		} else {
			strArType = item.arHeaderInfo.strARTrxnType;
		}

		float fAmount = getRoundedValue(item.arHeaderInfo.nTrxnAmount * 1.0f / Global.MULTIPLE_VALUE);
		Intent intent = new Intent(mContext, MintIFrameActivity.class);
		intent.putExtra("reference", strInvoiceNumber);
		intent.putExtra("amount", fAmount);
		startActivityForResult(intent, Global.MINT_IFRAME);
	}

	private void goBack() {
//		if( previousFragment != null )
//		{
//			previousFragment.setResetCollectionValue();
//		}
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
			case R.id.btnPay:
				doPayCollectionItem();
				break;
		}
		m_bClicked = false;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == Global.MINT_IFRAME && resultCode == Activity.RESULT_OK)
		{
			if(data == null) return;
			String strUrl = data.getStringExtra("url");
			String strReference = data.getStringExtra("reference");
			float fRequestedAmount = data.getFloatExtra("amount", 0);
			Uri uri = Uri.parse(strUrl);
			if(uri.getQueryParameter("successful") != null && uri.getQueryParameter("successful").equals("true"))
			{
				// we need to mark the collection item so that we won't pay it repeatedly
				DbAdapter dbAdapter = new DbAdapter(mContext);
				dbAdapter.open();
				for(DetailCollectionItem item : mCollectionItemList)
				{
					if (item.arHeaderInfo.strInvNo.equals(strReference)) {
						ARHeaderInfo arHeaderInfo = item.arHeaderInfo;
						long nTrxnNo = System.currentTimeMillis();
						String strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(new Date());
						Global.gUARTotalInfo.nTrxnNo = 0;
						Global.gUARTotalInfo.nChainNo = Global.gCurCustomerInfo.nChainNo;
						Global.gUARTotalInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
						Global.gUARTotalInfo.nInvoiceTrxnNo = nTrxnNo;
						Global.gUARTotalInfo.nInvoiceAmount = arHeaderInfo.nTrxnAmount * 1.0f / Global.MULTIPLE_VALUE;

						float dCurrentArAmount = arHeaderInfo.nTrxnAmount * 1.0f / Global.MULTIPLE_VALUE;

						UARPaymentInfo uarPaymentInfo = new UARPaymentInfo();
						uarPaymentInfo.nTrxnNo = nTrxnNo;
						uarPaymentInfo.nChainNo = Global.gCurCustomerInfo.nChainNo;
						uarPaymentInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
						uarPaymentInfo.strTrxnDate = strTrxnDate;
						uarPaymentInfo.nPayType = Global.COLLECTION_CARD;
						uarPaymentInfo.nTrxnAmount = dCurrentArAmount;

						Global.gUARTotalInfo.nTrxnNo = nTrxnNo;
						Global.gUARTotalInfo.nChainNo = Global.gCurCustomerInfo.nChainNo;
						Global.gUARTotalInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;

						Global.gUARTotalInfo.strTrxnDate = arHeaderInfo.strInvDate;
						Global.gUARTotalInfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(new Date(nTrxnNo));

						dbAdapter.insertUARTotalInfo(Global.gUARTotalInfo);
						uarPaymentInfo.strDocNo = arHeaderInfo.strInvNo;
						uarPaymentInfo.strInvoiceNo = Global.gUARTotalInfo.strDocNo;
						dbAdapter.insertUARPaymentInfo(uarPaymentInfo);

						int nSeqNo = Utils.getIntSetting(mContext, String.format(Locale.ENGLISH, Global.PREF_COLLECTION_SEQUENCE_NO, Global.gTripNumber));
						nSeqNo++;
						Utils.setIntSetting(mContext, String.format(Locale.ENGLISH, Global.PREF_COLLECTION_SEQUENCE_NO, Global.gTripNumber), nSeqNo);
						long nPayTrxnNo = setGeneralUploadData("PAY", Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo, nTrxnNo);

						TransactionInfo transactionInfo = new TransactionInfo();
						transactionInfo.strType = "PAY";
						transactionInfo.strDocNo = Global.gUARTotalInfo.strDocNo;
						transactionInfo.value = uarPaymentInfo.nTrxnAmount;
						transactionInfo.strCustomerName = Global.gCurCustomerInfo.strName;
						transactionInfo.strTime = new SimpleDateFormat("kk:mm").format(new Date(nPayTrxnNo));

						dbAdapter.insertTransactionHistoryInfo(transactionInfo);

						UCollectionsDInfo uCollectionsDInfo = new UCollectionsDInfo();
						uCollectionsDInfo.nTrxnNo = nTrxnNo;
						uCollectionsDInfo.nSeqNo = nSeqNo;
						uCollectionsDInfo.nPayType = Global.COLLECTION_CARD;
						uCollectionsDInfo.strTrxnDate = strTrxnDate;

						arHeaderInfo.strUser1 = Global.AR_HEADER_PAID;
						dbAdapter.updateARHeaderInfo(arHeaderInfo);
						/*arHeaderInfo.nProcessStatus = 3;
						dbAdapter.updateARHeaderInfo(arHeaderInfo);*/
						uCollectionsDInfo.nTrxnAmount = dCurrentArAmount;
						uCollectionsDInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
						uCollectionsDInfo.nChainNo = Global.gCurCustomerInfo.nChainNo;
						uCollectionsDInfo.strCustomerName = Global.gCurCustomerInfo.strName;
						uCollectionsDInfo.strInvoiceNo = Global.gUARTotalInfo.strDocNo;
						float nTrxnAmount = Utils.getFloatSetting(mContext, Global.COLLECTIONS_TODAY);
						dbAdapter.insertUCollectionsDInfo(uCollectionsDInfo);
						nTrxnAmount += uCollectionsDInfo.nTrxnAmount;
						Utils.setFloatSetting(mContext, Global.COLLECTIONS_TODAY, nTrxnAmount);
						Utils.registerUploadFileAsTemp(mContext, makeZipFileInfo);

						break;
					}
				}
				dbAdapter.close();

				Global.gUARPaymentCardInfo.nTrxnAmount += fRequestedAmount;

				loadData();
			}
			else
			{
				if(!mContext.isFinishing() && !mContext.isDestroyed())
				{
					try {
						Toast.makeText(mContext, Str.PaymentNotCompleted[Global.LANG], Toast.LENGTH_LONG).show();
					}catch (Exception e1)
					{
						e1.printStackTrace();
					}
				}
			}
		}
	}

	public class ListAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mCollectionItemList.size();
		}

		@Override
		public DetailCollectionItem getItem(int i) {
			return mCollectionItemList.get(i);
		}

		@Override
		public long getItemId(int i) {
			return i;
		}

		@Override
		public View getView(int i, View view, ViewGroup viewGroup) {
			view = getLayoutInflater().inflate(R.layout.row_detail_collection, viewGroup, false);

			DetailCollectionItem item = getItem(i);

			CheckBox checkBox = ((CheckBox) view.findViewById(R.id.chkSelect));

			if (mSelectedIndex == i) {
				checkBox.setChecked(true);
			} else {
				checkBox.setChecked(false);
			}

			if (Global.gCurHeaderInfo != null && (Global.gCurCustomerInfo.strPayType.equals("3") || Global.gCurCustomerInfo.strPayType.equals("4") || Global.gCurCustomerInfo.strPayType.equals("5") || Global.gCurCustomerInfo.strPayType.equals("C"))) {
				if (item.arHeaderInfo == Global.gCurHeaderInfo) {
					checkBox.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							mSelectedIndex = i;
							refreshPage();
						}
					});
				} else {
					checkBox.setClickable(false);
					checkBox.setEnabled(false);
				}

			} else {
				boolean bPayType = false;
				if (Global.gCurCustomerInfo.strPayType.equals("$") ||
						Global.gCurCustomerInfo.strPayType.equals("1") ||
						Global.gCurCustomerInfo.strPayType.equals("2") ||
						Global.gCurCustomerInfo.strPayType.equals("3")) {
					if (item.arHeaderInfo.strARTrxnType.equals("CRN") || item.arHeaderInfo.strARTrxnType.equals("INV")) {
						bPayType = true;
					}
				} else if (Global.gCurCustomerInfo.strPayType.equals("B")) {
					if (!item.arHeaderInfo.strARTrxnType.equals("CRN") && !item.arHeaderInfo.strARTrxnType.equals("INV")) {
						bPayType = true;
					}
				}

				if (bPayType) {
					checkBox.setClickable(false);
				}

				checkBox.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						mSelectedIndex = i;
						refreshPage();
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
			strInvoiceNumber = item.arHeaderInfo.strInvNo;
			if (item.arHeaderInfo.strARTrxnType == null || item.arHeaderInfo.strARTrxnType.length() == 0) {
				if (item.arHeaderInfo.nTrxnAmount > 0)
					strArType = "INV";
				else
					strArType = "CRN";
			} else {
				strArType = item.arHeaderInfo.strARTrxnType;
			}

			if (item.arHeaderInfo.nTrxnAmount < 0) {
				bNeg = false;
				strCurrency = "-" + Global.gCurrencySymbol;
			} else {
				bNeg = true;
				strCurrency = Global.gCurrencySymbol;
			}

			strAmount = getRoundedFormattedPriceWithoutCurrency(item.arHeaderInfo.nTrxnAmount * 1.0f / Global.MULTIPLE_VALUE);

			((TextView) view.findViewById(R.id.txtInvoiceDate)).setText(strInvoiceDate);
			((TextView) view.findViewById(R.id.txtItemType)).setText(strArType);
			((TextView) view.findViewById(R.id.txtInvNumber)).setText(strInvoiceNumber);
			((TextView) view.findViewById(R.id.lblCurrencySymbol)).setText(strCurrency);
			((TextView) view.findViewById(R.id.editPrice)).setText(strAmount);
			if (bNeg) {
				((TextView) view.findViewById(R.id.txtInvoiceDate)).setTextColor(mContext.getResources().getColor(R.color.black_color));
				((TextView) view.findViewById(R.id.txtItemType)).setTextColor(mContext.getResources().getColor(R.color.black_color));
				((TextView) view.findViewById(R.id.txtInvNumber)).setTextColor(mContext.getResources().getColor(R.color.black_color));
				((TextView) view.findViewById(R.id.lblCurrencySymbol)).setTextColor(mContext.getResources().getColor(R.color.black_color));
				((TextView) view.findViewById(R.id.editPrice)).setTextColor(mContext.getResources().getColor(R.color.black_color));
			} else {
				((TextView) view.findViewById(R.id.txtInvoiceDate)).setTextColor(mContext.getResources().getColor(R.color.red_color));
				((TextView) view.findViewById(R.id.txtItemType)).setTextColor(mContext.getResources().getColor(R.color.red_color));
				((TextView) view.findViewById(R.id.txtInvNumber)).setTextColor(mContext.getResources().getColor(R.color.red_color));
				((TextView) view.findViewById(R.id.lblCurrencySymbol)).setTextColor(mContext.getResources().getColor(R.color.red_color));
				((TextView) view.findViewById(R.id.editPrice)).setTextColor(mContext.getResources().getColor(R.color.red_color));
			}
			return view;
		}
	}

	public class DetailCollectionItem
	{
		public boolean status = false;
		public ARHeaderInfo arHeaderInfo;
	}
}