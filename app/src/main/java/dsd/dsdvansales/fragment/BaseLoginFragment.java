package dsd.dsdvansales.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.Date;

import dsd.dsdvansales.activities.LoginActivity;
import dsd.dsdvansales.db.DbAdapter;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.model.MakeZipFileInfo;
import dsd.dsdvansales.model.UTransactionsInfo;

public abstract class BaseLoginFragment extends Fragment {

    public LoginActivity mContext = null;
    public View mRootView = null;
    public abstract void initView();
    public abstract void loadData();
    public abstract void refreshPage();
    public abstract void refreshLanguage();
    public abstract void receiveBroadcast(String strAction, Intent intent);
    public long m_lTrxnNo;
    public Date mCurDate;

    public MakeZipFileInfo makeZipFileInfo = null;

    private EventReceiver mEventReceiver = null;

    private class EventReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String strAction = intent.getAction();
            if (strAction.equals(Global.ACTION_REFRESH_LANGUAGE) == true) {
                refreshLanguage();
            } else {
                receiveBroadcast(strAction, intent);
            }
        }
    }

    private void unregisterReceiver() {
        try {
            if (mEventReceiver != null) {
                mContext.unregisterReceiver(mEventReceiver);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void commonInit() {
        mContext = (LoginActivity) getActivity();

        unregisterReceiver();
        mEventReceiver = new EventReceiver();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mContext.registerReceiver(mEventReceiver, new IntentFilter(Global.ACTION_REFRESH_LANGUAGE), Context.RECEIVER_EXPORTED);
        } else {
            mContext.registerReceiver(mEventReceiver, new IntentFilter(Global.ACTION_REFRESH_LANGUAGE));
        }

        Global .LANG = Utils.getIntSetting(mContext, Global.PREF_CUR_LANG);
        Global.gTripNumber = Utils.getStringSetting(mContext, Global.PREF_TRIP_NUMBER);
        Global.gRouteNumber = Utils.getStringSetting(mContext, Global.PREF_ROUTE_NUMBER);
        Global.gCurrencySymbol = Utils.getStringSetting(mContext, Global.PREF_CURRENCY_SYMBOL);
        Global.MULTIPLE_VALUE = 100000 / (int)(Math.pow(10, Utils.getIntSetting(mContext, Global.PREF_SCALING)));
        initView();
    }

    public void showKeyboard(EditText editText, boolean bShow) {
        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (bShow == true) {
            editText.requestFocus();
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
            editText.setSelection(editText.getText().toString().length());
        } else {
            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        }
    }

    public void focusAndHideKeyboard(EditText editText) {
        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        editText.requestFocus();
        editText.setSelection(editText.getText().toString().length());
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    public void showProgressDialog(String strText)
    {
        mContext.showProgressDialog(strText);
    }

    public void hideProgressDialog()
    {
        mContext.hideProgressDialog();
    }

    public long setGeneralUploadData(String strDocType, int nChainNo, int nCustNo, long nTrxnNo) {
//        double nLat = Utils.getDoubleSetting(mContext, Global.PREF_MY_LATITUDE);
//        double nLng = Utils.getDoubleSetting(mContext, Global.PREF_MY_LONGITUDE);

        if( nTrxnNo != -1 )
            m_lTrxnNo = nTrxnNo;
        else
            m_lTrxnNo = System.currentTimeMillis();
        mCurDate = new Date(m_lTrxnNo);
        UTransactionsInfo uTransactionInfo = new UTransactionsInfo();
        uTransactionInfo.nTrxnNo = m_lTrxnNo;
        uTransactionInfo.strDocType = strDocType;
        uTransactionInfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(mContext, mCurDate));
        uTransactionInfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(mCurDate);
        uTransactionInfo.nChainNo = nChainNo;
        uTransactionInfo.nCustNo = nCustNo;

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
		uGPSInfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(mCurDate);*/

        uTransactionInfo.strTripId = Utils.getStringSetting(mContext, Global.PREF_TRIP_ID);
        uTransactionInfo.strDriverId = "";
        uTransactionInfo.strDriverName = Utils.getStringSetting(mContext, Global.PREF_USER_DESCRIPTION);
        uTransactionInfo.strDriverNumber = Utils.getStringSetting(mContext, Global.PREF_DRIVER_NUMBER);

        DbAdapter dbAdapter = new DbAdapter(mContext);
        dbAdapter.open();
        dbAdapter.insertUTransactionsInfo(uTransactionInfo);
//		dbAdapter.insertUGPSInfo(uGPSInfo);
        dbAdapter.close();
        AddTrxnNo(nCustNo, nChainNo, m_lTrxnNo);
        return m_lTrxnNo;
    }

    public long setGeneralUploadData(String strDocType, int nChainNo, int nCustNo, long nTrxnNo, String strReference) {
//        double nLat = Utils.getDoubleSetting(mContext, Global.PREF_MY_LATITUDE);
//        double nLng = Utils.getDoubleSetting(mContext, Global.PREF_MY_LONGITUDE);

        if( nTrxnNo != -1 )
            m_lTrxnNo = nTrxnNo;
        else
            m_lTrxnNo = System.currentTimeMillis();
        mCurDate = new Date(m_lTrxnNo);
        UTransactionsInfo uTransactionInfo = new UTransactionsInfo();
        uTransactionInfo.nTrxnNo = m_lTrxnNo;
        uTransactionInfo.strDocType = strDocType;
        uTransactionInfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(mContext, mCurDate));
        uTransactionInfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(mCurDate);
        uTransactionInfo.nChainNo = nChainNo;
        uTransactionInfo.strReference = strReference;
        uTransactionInfo.nCustNo = nCustNo;

/*		UGPSInfo uGPSInfo = new UGPSInfo();
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
		uGPSInfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(mCurDate);*/

        uTransactionInfo.strTripId = Utils.getStringSetting(mContext, Global.PREF_TRIP_ID);
        uTransactionInfo.strDriverId = "";
        uTransactionInfo.strDriverName = Utils.getStringSetting(mContext, Global.PREF_USER_DESCRIPTION);
        uTransactionInfo.strDriverNumber = Utils.getStringSetting(mContext, Global.PREF_DRIVER_NUMBER);

        DbAdapter dbAdapter = new DbAdapter(mContext);
        dbAdapter.open();
        dbAdapter.insertUTransactionsInfo(uTransactionInfo);
//		dbAdapter.insertUGPSInfo(uGPSInfo);
        dbAdapter.close();

        AddTrxnNo(nCustNo, nChainNo, m_lTrxnNo);

        return m_lTrxnNo;
    }

    public void AddTrxnNo(int nCustNo, int nChainNo, long nTrxnNo)
    {
        if( makeZipFileInfo == null )
        {
            makeZipFileInfo = new MakeZipFileInfo();
            makeZipFileInfo.nCustNo = nCustNo;
            makeZipFileInfo.nChainNo = nChainNo;
        }
        boolean bAdd = true;
        for(Long trxnNo : makeZipFileInfo.trxnList)
        {
            if(trxnNo == nTrxnNo)
            {
                bAdd = false;
                break;
            }
        }
        if( bAdd )
        {
            makeZipFileInfo.trxnList.add(nTrxnNo);
        }
    }

    @Override
    public void onDestroy() {
        unregisterReceiver();
        super.onDestroy();
    }
}
