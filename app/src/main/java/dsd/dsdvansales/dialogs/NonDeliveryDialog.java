package dsd.dsdvansales.dialogs;

import java.text.SimpleDateFormat;
import java.util.*;
import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;

import dsd.dsdvansales.R;
import dsd.dsdvansales.db.DbAdapter;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.model.DescTypeInfo;
import dsd.dsdvansales.model.MakeZipFileInfo;
import dsd.dsdvansales.model.TransactionInfo;
import dsd.dsdvansales.model.UOrderStatusDInfo;
import dsd.dsdvansales.model.UOrderStatusHInfo;
import dsd.dsdvansales.model.UServicesInfo;
import dsd.dsdvansales.model.UTransactionsInfo;
import dsd.dsdvansales.model.UVisitInfo;
import dsd.dsdvansales.views.AlertSingleChoiceAdapter;

public class NonDeliveryDialog extends Dialog {

	private Context mContext = null;
	private TextView mTxtReason = null;

	private int mReasonCodeIdx = 0;
	private List<DescTypeInfo> mDescTypeList = new ArrayList<>();
	private List<DescTypeInfo> mVisitTypeList = new ArrayList<>();

	public int mType = Global.REASON_TYPE_NON_DELIVERY;
	public String mStrMessage = "";
	public int mReturnCode = 0;
	public String mStrResult = "";

	public NonDeliveryDialog(Context context) {
		super(context, android.R.style.Theme_Translucent_NoTitleBar);
		this.mContext = context;
	}

	public boolean m_bClicked = false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
		lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
		lpWindow.dimAmount = 0.9f;
		getWindow().setAttributes(lpWindow);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.dialog_non_delivery);

		mDescTypeList = loadDescTypeList();
		if (mDescTypeList.size() == 0) {
			Utils.printDebug(mContext, "No reason code list.");
			new Handler(Looper.getMainLooper()).post(new Runnable() {
				@Override
				public void run() {
					dismiss();
				}
			});

			return;
		}

		mStrResult = mDescTypeList.get(mReasonCodeIdx).strAlphaKey;
		mTxtReason = ((TextView) findViewById(R.id.txtReason));
		mTxtReason.setText(mDescTypeList.get(mReasonCodeIdx).strDescription);

		if (mType == Global.REASON_TYPE_NON_DELIVERY) {
			((TextView) findViewById(R.id.txtTitle)).setText(Str.NonSalesReason[Global.LANG].toUpperCase());
		} else if (mType == Global.REASON_TYPE_NON_SERVICE) {
			((TextView) findViewById(R.id.txtTitle)).setText(Str.NonServiceReason[Global.LANG].toUpperCase());
		} else if (mType == Global.REASON_TYPE_SHORT_DELIVERY) {
			((TextView) findViewById(R.id.txtTitle)).setText(Str.Order_Adjustments[Global.LANG].toUpperCase());
		} else if (mType == Global.REASON_TYPE_SHORT_PICKUP) {
			((TextView) findViewById(R.id.txtTitle)).setText(Str.ShortReturnReason[Global.LANG].toUpperCase());
		}
		if (mStrMessage.length() > 0) {
			((TextView) findViewById(R.id.txtMessage)).setText(mStrMessage);
			findViewById(R.id.txtMessage).setVisibility(View.VISIBLE);
		} else {
			findViewById(R.id.txtMessage).setVisibility(View.GONE);
		}

		((Button) findViewById(R.id.btnReturn)).setText(Str.Return[Global.LANG]);
		((Button) findViewById(R.id.btnSelect)).setText(Str.Select[Global.LANG]);

		mTxtReason.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if( m_bClicked ) return;
				m_bClicked = true;
				showReasonCodeList();
				m_bClicked = false;
			}
		});
		findViewById(R.id.btnReturn).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if( m_bClicked ) return;
				Global.gCurCustomerInfo = null;
				m_bClicked = true;
				mReturnCode = 0;
				dismiss();
				m_bClicked = false;
			}
		});
		findViewById(R.id.btnSelect).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if( m_bClicked ) return;
				m_bClicked = true;
				mStrResult = mDescTypeList.get(mReasonCodeIdx).strAlphaKey;
				mReturnCode = Global.DLG_RETURN_RIGHT;

				String strDescriptionEntryKey = mDescTypeList.get(mReasonCodeIdx).strDescriptionEntryKey;
				long nTrxnNo;
				if(Global.gCurCustomerInfo != null)
				{
					nTrxnNo = setGeneralUploadData("SERV", Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo, -1, mStrResult);
				}
				else
				{
					nTrxnNo = setGeneralUploadData("SERV", 0, 0, -1, mStrResult);
				}

				Date curDate = new Date(nTrxnNo);
				DbAdapter dbAdapter = new DbAdapter(mContext);
				dbAdapter.open();

				TransactionInfo transactionInfo = new TransactionInfo();
				transactionInfo.strType = "SERV";
				transactionInfo.strTime = new SimpleDateFormat("kk:mm").format(new Date(nTrxnNo));
				if(Global.gCurCustomerInfo != null)
				{
					transactionInfo.strCustomerName = Global.gCurCustomerInfo.strName;
				}
				else
				{
					transactionInfo.strCustomerName = "";
				}
				dbAdapter.insertTransactionHistoryInfo(transactionInfo);

				UServicesInfo uServicesInfo = new UServicesInfo();
				uServicesInfo.nTrxnNo = nTrxnNo;
				if(Global.gCurCustomerInfo != null)
				{
					uServicesInfo.nChainNo = Global.gCurCustomerInfo.nChainNo;
					uServicesInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
					uServicesInfo.nSeqNo = Global.gCurCustomerInfo.nSeqNo;
				}
				else
				{
					uServicesInfo.nChainNo = 0;
					uServicesInfo.nCustNo = 0;
					uServicesInfo.nSeqNo = 0;
				}

				uServicesInfo.nPeriod = 0;

				uServicesInfo.nDone = 0;
				uServicesInfo.strReason = mStrResult;
				uServicesInfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(mContext, curDate));
				uServicesInfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(curDate);
				dbAdapter.insertUServicesInfo(uServicesInfo);

				UOrderStatusHInfo uOrderStatusInfo = new UOrderStatusHInfo();
				uOrderStatusInfo.nTrxnNo = nTrxnNo;
				uOrderStatusInfo.strDocNo = "";
				uOrderStatusInfo.strDocType = "OSTA";
				uOrderStatusInfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(mContext, new Date(nTrxnNo)));
				uOrderStatusInfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(nTrxnNo);
				UOrderStatusDInfo dInfo = new UOrderStatusDInfo();
				dInfo.strDeliveryDate = uOrderStatusInfo.strTrxnDate;

				if(Global.gCurCustomerInfo != null)
				{
					dInfo.nChainNo = Global.gCurCustomerInfo.nChainNo;
					dInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
					dInfo.nDayNo = Global.gCurCustomerInfo.nDayNo;
				}
				else
				{
					dInfo.nChainNo = 0;
					dInfo.nCustNo = 0;
					dInfo.nDayNo = 0;
				}

				dInfo.nStatus = 4;
				dInfo.strReference = mDescTypeList.get(mReasonCodeIdx).strDescription;

				dInfo.nTrxnNo = nTrxnNo;

				if(Global.gCurCustomerInfo != null)
				{
					//dInfo.strDeliveryDate = Global.gCurCustomerInfo.strDeliveryDate;
					if(Global.gCurCustomerInfo.presoldOrHInfo == null || Global.gCurCustomerInfo.presoldOrHInfo.size() == 0)
					{
						Global.gCurCustomerInfo.presoldOrHInfo = dbAdapter.getPresoldOrHInfo(Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo, Global.gCurCustomerInfo.nPeriodNo, Global.gCurCustomerInfo.nSeqNo);
					}

					if(Global.gCurCustomerInfo.presoldOrHInfo != null && Global.gCurCustomerInfo.presoldOrHInfo.size() > 0)
					{
						dInfo.strOrderNo = Global.gCurCustomerInfo.presoldOrHInfo.get(0).strOrderNo;
						dInfo.nDeliverySequence = Global.gCurCustomerInfo.presoldOrHInfo.get(0).nDeliverySequence;
						dInfo.strOrderBarcode = Global.gCurCustomerInfo.presoldOrHInfo.get(0).strOrderBarCode;
						dInfo.strPORef = Global.gCurCustomerInfo.presoldOrHInfo.get(0).strPORef;
					}
				}
				else
				{
					//dInfo.strDeliveryDate = "";
				}

				uOrderStatusInfo.uOrderStatusDInfoList.add(dInfo);
				dbAdapter.insertUOrderStatusInfo(uOrderStatusInfo);

				MakeZipFileInfo info = new MakeZipFileInfo();
				info.nChainNo = 0;
				info.nCustNo = 0;
				info.trxnList.add(nTrxnNo);

				//Upload the Visit Information
				if(Global.gCurCustomerInfo != null)
				{
					nTrxnNo = setGeneralUploadData("VIS", Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo, -1, "No Sales Reasons");
					UVisitInfo visitInfo = new UVisitInfo();
					visitInfo.nTrxnNo = nTrxnNo;
					visitInfo.strDocType = "VIS";
					visitInfo.nChainNo = Global.gCurCustomerInfo.nChainNo;
					visitInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
					visitInfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(mContext, curDate));
					visitInfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(curDate);
					visitInfo.nPrintedFlag = 0;
					visitInfo.strVisitReason = mStrResult;
					visitInfo.strVisitReasonID = strDescriptionEntryKey;
					visitInfo.strSaleDate = visitInfo.strTrxnDate;
					visitInfo.strVisitStart = Global.strVisitStart;
					visitInfo.strVisitEnd = new SimpleDateFormat("kkmmss").format(new Date());
					visitInfo.strNextVisit = "";
					visitInfo.strVisitMessage = "";
					visitInfo.nOrderCreated = Global.bOrderCreated ? 1 : 0;
					visitInfo.nSurveyCompleted = Global.nSurveyCompleted;
					visitInfo.nAssetChecked = Global.nAssetChecked;
					visitInfo.nAssetRequested = Global.nAssetRequested;
					visitInfo.nVisitFrequency = 0;
//					visitInfo.strVisitTypeId = (mVisitTypeList != null && mVisitTypeList.size() > 0 ? mVisitTypeList.get(0).strDescriptionEntryKey : "0");
					dbAdapter.insertUVisitInfo(visitInfo);
				}
				else
				{
					nTrxnNo = setGeneralUploadData("VIS", 0, 0, -1, "No Sales Reasons");
					UVisitInfo visitInfo = new UVisitInfo();
					visitInfo.nTrxnNo = nTrxnNo;
					visitInfo.strDocType = "VIS";
					visitInfo.nChainNo = 0;
					visitInfo.nCustNo = 0;
					visitInfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(mContext, curDate));
					visitInfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(curDate);
					visitInfo.nPrintedFlag = 0;
					visitInfo.strVisitReason = mStrResult;
					visitInfo.strVisitReasonID = strDescriptionEntryKey;
					visitInfo.strSaleDate = visitInfo.strTrxnDate;
					visitInfo.strVisitStart = Global.strVisitStart;
					visitInfo.strVisitEnd = new SimpleDateFormat("kkmmss").format(new Date());
					visitInfo.strNextVisit = "";
					visitInfo.strVisitMessage = "";
					visitInfo.nOrderCreated = Global.bOrderCreated ? 1 : 0;
					visitInfo.nSurveyCompleted = Global.nSurveyCompleted;
					visitInfo.nAssetChecked = Global.nAssetChecked;
					visitInfo.nAssetRequested = Global.nAssetRequested;
					visitInfo.nVisitFrequency = 0;
//					visitInfo.strVisitTypeId = (mVisitTypeList != null && mVisitTypeList.size() > 0 ? mVisitTypeList.get(0).strDescriptionEntryKey : "0");
					dbAdapter.insertUVisitInfo(visitInfo);
				}


				info.trxnList.add(nTrxnNo);
				dbAdapter.close();

				//Global.gUploadCustNoList.add(info);
				Utils.registerUploadFile(mContext, info);
				dismiss();
				m_bClicked = false;
			}
		});
	}

	public long setGeneralUploadData(String strDocType, int nChainNo, int nCustNo, long nTrxnNo, String strReason) {
//		double nLat = Utils.getDoubleSetting(mContext, Global.PREF_MY_LATITUDE);
//		double nLng = Utils.getDoubleSetting(mContext, Global.PREF_MY_LONGITUDE);
		long m_lTrxnNo = 0;

		if( nTrxnNo != -1 )
			m_lTrxnNo = nTrxnNo;
		else
			m_lTrxnNo = System.currentTimeMillis();

		Date mCurDate = new Date(m_lTrxnNo);
		UTransactionsInfo uTransactionInfo = new UTransactionsInfo();
		uTransactionInfo.nTrxnNo = m_lTrxnNo;
		uTransactionInfo.strDocType = strDocType;
		uTransactionInfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(mContext, mCurDate));
		uTransactionInfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(mCurDate);
		uTransactionInfo.nChainNo = nChainNo;
		uTransactionInfo.nCustNo = nCustNo;
		uTransactionInfo.strReference = strReason;

		/*UGPSInfo uGPSInfo = new UGPSInfo();
		uGPSInfo.nTrxnNo = m_lTrxnNo;
		uGPSInfo.nChainNo = nChainNo;
		uGPSInfo.nCustNo = nCustNo;
		uGPSInfo.nStartTrxnNo = uTransactionInfo.nTrxnNo;
		if (nLat != Global.DEFAULT_FLOAT && nLng != Global.DEFAULT_FLOAT) {
			uGPSInfo.strGPSData = String.format(Locale.ENGLISH, "%f, %f", nLat, nLng);
		} else {
			uGPSInfo.strGPSData = "ERROR: NO GPS";
		}
		if(strDocType.equals("MAIT"))
			uGPSInfo.strDocType = "MAIT";
		else
			uGPSInfo.strDocType = "GPS";
		uGPSInfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(mCurDate);
		uGPSInfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(mCurDate);
*/
		uTransactionInfo.strTripId = Utils.getStringSetting(mContext, Global.PREF_TRIP_ID);
		uTransactionInfo.strDriverId = "";
		uTransactionInfo.strDriverName = Utils.getStringSetting(mContext, Global.PREF_USER_DESCRIPTION);
		uTransactionInfo.strDriverNumber = Utils.getStringSetting(mContext, Global.PREF_DRIVER_NUMBER);

		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		dbAdapter.insertUTransactionsInfo(uTransactionInfo);
//		dbAdapter.insertUGPSInfo(uGPSInfo);
		dbAdapter.close();
		return m_lTrxnNo;
	}

	private List<DescTypeInfo> loadDescTypeList() {
		String strDescriptionTypeID = "";
		if (mType == Global.REASON_TYPE_NON_DELIVERY || mType == Global.REASON_TYPE_NON_SERVICE)  {
			strDescriptionTypeID = "NOSERVRSN";
		} else if (mType == Global.REASON_TYPE_SHORT_DELIVERY || mType == Global.REASON_TYPE_SHORT_PICKUP) {
			strDescriptionTypeID = "NODLVRYRSN";
		}
		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		List<DescTypeInfo> list = dbAdapter.getDescTypeList(strDescriptionTypeID);
		mVisitTypeList = dbAdapter.getDescTypeList("VisitType");
		dbAdapter.close();
		return list;
	}

	private void showReasonCodeList() {
		final String strArray[] = new String[mDescTypeList.size()];
		for (int i=0; i<mDescTypeList.size(); i++) {
			strArray[i] = mDescTypeList.get(i).strDescription;
		}
		final int origIdx = mReasonCodeIdx;
		final String origText = mTxtReason.getText().toString();
		try
		{
			new AlertDialog.Builder(mContext)
					.setTitle(Str.Select_Reason_code[Global.LANG])
					.setSingleChoiceItems(new AlertSingleChoiceAdapter(mContext, strArray), origIdx, (dialog, nSelected) -> {
						mReasonCodeIdx = nSelected;
						mTxtReason.setText(strArray[nSelected]);
					})
					.setPositiveButton(Str.Ok[Global.LANG], (dialog, nSelected) -> {
					})
					.setNegativeButton(Str.Cancel[Global.LANG], (dialog, nSelected) -> {
						mReasonCodeIdx = origIdx;
						mTxtReason.setText(origText);
					})
					.create()
					.show();
		}catch (Exception e)
		{
			e.printStackTrace();
		}

	}
}