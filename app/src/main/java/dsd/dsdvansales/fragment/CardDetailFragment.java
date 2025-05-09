package dsd.dsdvansales.fragment;

import android.content.*;
import android.os.*;
import android.text.*;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.util.List;
import java.util.Locale;

import dsd.dsdvansales.R;
import dsd.dsdvansales.db.DbAdapter;
import dsd.dsdvansales.dialogs.MessageDialog;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.model.ARHeaderInfo;
import dsd.dsdvansales.model.UCollectionsDInfo;


public class CardDetailFragment extends BaseFragment implements View.OnClickListener {

	private String mStrInvoiceNo = "";
	private String mStrARNotes = "";
	private float mAmount = 0;
	private EditText mEditAmount = null;

	private boolean mAutoCollectPayment = false;
	private UCollectionsDInfo mCollectionInfo = null;

	private DeliverySummaryFragment mParentFragment;

	public void setParentFragment(DeliverySummaryFragment fragment)
	{
		mParentFragment = fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootView = inflater.inflate(R.layout.fragment_card_detail, container, false);
		commonInit();
		return mRootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		Global.gFragIdx = Global.FRAG_CARD_DETAIL;
	}

	@Override
	public void initView() {
		mRootView.findViewById(R.id.btnBack).setOnClickListener(this);
		mRootView.findViewById(R.id.btnCompleted).setOnClickListener(this);

		mEditAmount = (EditText) mRootView.findViewById(R.id.editAmount);
		

		
		mEditAmount.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				if (mEditAmount.getTag() == null) {
					setFormattedAmount(s.toString());
				}
			}
		});
		mEditAmount.post(new Runnable() {
			public void run() {
				mEditAmount.setSelection(mEditAmount.getText().toString().length());
				mEditAmount.postDelayed(this, 100);
			}
		});

		((TextView) mRootView.findViewById(R.id.txtCustomerName)).setText(Global.gCurCustomerInfo.strName);
		loadData();
		refreshLanguage();
	}

	
	@Override
	public void loadData() {

		Bundle argments = getArguments();
		if( argments != null )
		{
			mAutoCollectPayment = argments.getBoolean(Global.EXTRA_IS_AUTO_COLLECT, false);
		}
		if( mAutoCollectPayment )
		{
			DbAdapter dbAdapter = new DbAdapter(mContext);
			dbAdapter.open();
			List<UCollectionsDInfo> uCollectionsDInfos = dbAdapter.getUCollectionsDList(Global.gCurCustomerInfo.nCustNo, Global.gCurCustomerInfo.nChainNo);
			for(int i = uCollectionsDInfos.size() - 1; i >= 0; i--)
			{
				if(uCollectionsDInfos.get(i).nPayType == Global.COLLECTION_CARD)
				{
					mCollectionInfo = uCollectionsDInfos.get(i);
					break;
				}
			}
			dbAdapter.close();
		}
		if(  mAutoCollectPayment && mCollectionInfo != null)
		{
			mEditAmount.setEnabled(false);
			mAmount = mCollectionInfo.nTrxnAmount;
		}
		else
		{
			mEditAmount.setEnabled(true);
			mAmount = Global.gUARPaymentCardInfo.nTrxnAmount;
		}

		setAmount();
		refreshPage();
	}

	@Override
	public void refreshPage() {
		float nCollectionRequired = 0;

		float nTotalCollected;
		if( mAutoCollectPayment && mCollectionInfo != null )
		{
			/*DbAdapter dbAdapter = new DbAdapter(mContext);
			dbAdapter.open();
			List<ARHeaderInfo> arHeaderInfos = dbAdapter.getARHeaderInfo(Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo);
			for(ARHeaderInfo arHeaderInfo : arHeaderInfos)
			{
				if( arHeaderInfo.nProcessStatus != 2 )
				{
					nCollectionRequired += arHeaderInfo.nTrxnAmount * 1.0 / Global.MULTIPLE_VALUE;
				}
			}
			dbAdapter.close();*/
			nCollectionRequired = mCollectionInfo.nTrxnAmount;
			nTotalCollected = mCollectionInfo.nTrxnAmount;
		}
		else
		{
			DbAdapter dbAdapter = new DbAdapter(mContext);
			dbAdapter.open();
			List<ARHeaderInfo> arHeaderInfos = dbAdapter.getARHeaderInfo(Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo);
			for(ARHeaderInfo arHeaderInfo : arHeaderInfos)
			{
				if( arHeaderInfo.nProcessStatus < 2 )
				{
					nCollectionRequired += arHeaderInfo.nTrxnAmount * 1.0 / Global.MULTIPLE_VALUE;
				}
			}
			dbAdapter.close();
			nTotalCollected = Global.gUARTotalInfo.nTotalCash + Global.gUARPaymentChequeInfo.nTrxnAmount + Global.gUARPaymentCardInfo.nTrxnAmount + Global.gUARPaymentEFTInfo.nTrxnAmount;
		}

		float nTotalRemaining = nCollectionRequired - nTotalCollected;
		((TextView) mRootView.findViewById(R.id.txtCollectionRequired)).setText(Utils.getFormattedPrice(mContext, nCollectionRequired));
		((TextView) mRootView.findViewById(R.id.txtTotalCollected)).setText(Utils.getFormattedPrice(mContext, nTotalCollected));
		((TextView) mRootView.findViewById(R.id.txtTotalRemaining)).setText(Utils.getFormattedPrice(mContext, nTotalRemaining));
		if(mAutoCollectPayment && mCollectionInfo != null )
		{
			((EditText) mRootView.findViewById(R.id.editReferenceNumber)).setText("");
			((EditText) mRootView.findViewById(R.id.editARNotes)).setText("");
		}else
		{
			((EditText) mRootView.findViewById(R.id.editReferenceNumber)).setText(Global.gUARPaymentCardInfo.strInvoiceNo);
			((EditText) mRootView.findViewById(R.id.editARNotes)).setText(Global.gUARPaymentCardInfo.strARNotes);
		}

		((TextView) mRootView.findViewById(R.id.lblCurrencySymbol)).setText(Global.gCurrencySymbol);
	}

	@Override
	public void refreshLanguage() {
		((TextView) mRootView.findViewById(R.id.lblTitle)).setText(Str.CardPaymentDetails[Global.LANG].toUpperCase());
		((TextView) mRootView.findViewById(R.id.lblCollectionRequired)).setText(Str.CollectionRequired[Global.LANG]);
		((TextView) mRootView.findViewById(R.id.lblTotalCollected)).setText(Str.TotalCollected[Global.LANG]);
		((TextView) mRootView.findViewById(R.id.lblTotalRemaining)).setText(Str.TotalRemaining[Global.LANG]);
		((TextView) mRootView.findViewById(R.id.lblReferenceNumber)).setText(Str.ReferenceNumber[Global.LANG]);
		((TextView) mRootView.findViewById(R.id.lblAmount)).setText(Str.Amount[Global.LANG]);
		((TextView) mRootView.findViewById(R.id.lblARNotes)).setText(Str.ARNotes[Global.LANG]);
		((Button) mRootView.findViewById(R.id.btnCompleted)).setText(Str.Completed[Global.LANG]);
	}

	@Override
	public void receiveBroadcast(String strAction, Intent intent) {
	}

	private void setFormattedAmount(String strVal) {
		String strArray[] = strVal.split("\\.");
		strVal = "";
		for (int i=0; i<strArray.length; i++) {
			strVal += strArray[i];
		}
		int nValue = Utils.getIntSetting(mContext, Global.PREF_DECIMAL);
		mAmount = Utils.parseFloatFromString(strVal) / (float)(Math.pow(10,nValue));
		setAmount();
	}

	private void setAmount() {
		mEditAmount.setTag("From App");
		mEditAmount.setText(String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(mContext, Global.PREF_DECIMAL) + "f", mAmount));
		mEditAmount.setSelection(mEditAmount.getText().toString().length());
		mEditAmount.setTag(null);
	}

	private void doComplete() {
		mAmount = Utils.parseFloatFromString(((EditText) mRootView.findViewById(R.id.editAmount)).getText().toString());
		if (mAmount == 0) {
			Utils.printDebug(mContext, Str.PleaseInputAmount[Global.LANG]);
			return;
		}
		mStrInvoiceNo = ((EditText) mRootView.findViewById(R.id.editReferenceNumber)).getText().toString();
		mStrARNotes = ((EditText) mRootView.findViewById(R.id.editARNotes)).getText().toString();
		if (mStrInvoiceNo.length() == 0 /*|| mStrARNotes.length() == 0*/) {
			try {
				final MessageDialog msgDialog = new MessageDialog(mContext);
				msgDialog.setCancelable(false);
				msgDialog.mStrMessage = Str.NotAllValuesEntered_msg[Global.LANG];
				msgDialog.mStrLeft = Str.Return[Global.LANG];
				msgDialog.mStrRight = Str.Proceed[Global.LANG];
				msgDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
							mIsCancelProcessed = true;
							goBack();
							if(mParentFragment != null)
							{
								mParentFragment.processCardDetailConfirm();
							}
						}
					}
				});
				msgDialog.show();
			}catch (Exception e)
			{
				e.printStackTrace();
			}
		} else {
			mIsCancelProcessed = true;
			goBack();
			if(mParentFragment != null)
			{
				mParentFragment.processCardDetailConfirm();
			}
		}
	}

	private void goBack() {
		if(mAutoCollectPayment && mCollectionInfo != null)
		{

		}
		else
		{
			Global.gUARPaymentCardInfo.strInvoiceNo = mStrInvoiceNo;
			Global.gUARPaymentCardInfo.strARNotes = mStrARNotes;
			Global.gUARPaymentCardInfo.nTrxnAmount = mAmount;
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
			case R.id.btnCompleted:
				doComplete();
				break;
		}
		m_bClicked = false;
	}

	public boolean mIsCancelProcessed = false;
	public void calledCancelProcess()
	{
		mIsCancelProcessed = true;
		if( mParentFragment != null)
		{
			mParentFragment.processCardDetailCancel();
		}
		mContext.onBackPressed();
	}

	public boolean isCalledCancelProcessing()
	{
		return mIsCancelProcessed;
	}
}