package dsd.dsdvansales.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import dsd.dsdvansales.R;
import dsd.dsdvansales.db.DbAdapter;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.model.CustomerInfo;
import dsd.dsdvansales.model.DescTypeInfo;
import dsd.dsdvansales.model.UInvenDInfo;
import dsd.dsdvansales.model.UServicesInfo;

public class SalesPerformanceFragment extends BaseFragment implements View.OnClickListener {


    int mSalesCustomerCnt = 0, mNonSalesCustomerCnt = 0;
    int mEndingStockCnt = 0;
    int mSalesCnt = 0;
    int mReturnsCnt = 0;
    int mSamplesCnt = 0;
    private String mCartonDescription = "";
    public SalesPerformanceFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mRootView =  inflater.inflate(R.layout.fragment_sales_performance, container, false);
        commonInit();
        return mRootView;
    }

    @Override
    public void initView() {
        mRootView.findViewById(R.id.btnDone).setOnClickListener(this);
        loadData();
    }

    private List<CustomerInfo> filterSameCustomer(List<CustomerInfo> customerInfoList)
    {
        Map<String, CustomerInfo> customerInfoMap = new HashMap<>();
        List<CustomerInfo> newList = new ArrayList<>();
        for(CustomerInfo customerInfo : customerInfoList)
        {
            String key = customerInfo.nChainNo + "___" + customerInfo.nCustNo;
            CustomerInfo info = customerInfoMap.get(key);
            if( info == null || info.bCompleted == false)
            {
                customerInfoMap.put(key, customerInfo);
                newList.add(customerInfo);
            }
        }
        return newList;
    }

    @Override
    public void loadData() {

        mSalesCustomerCnt = 0;
        mNonSalesCustomerCnt = 0;
        mEndingStockCnt = 0;
        mSalesCnt = 0;
        mReturnsCnt = 0;
        mSamplesCnt = 0;

        DbAdapter dbAdapter = new DbAdapter(mContext);
        dbAdapter.open();
        List<CustomerInfo> customerList = filterSameCustomer( dbAdapter.getWhiteCustomerInfo(false, Utils.getIntSetting(mContext, Global.DAY_NO)));
        customerList.addAll(dbAdapter.getCustomerList(true, false, Utils.getIntSetting(mContext, Global.DAY_NO)));
        List<UInvenDInfo> uInvenDInfoList = dbAdapter.getUInvenDList(Utils.getLongSetting(mContext, Global.PREF_UINVEN_TRXN_NO));

        mCartonDescription = "";
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

        for(CustomerInfo customerInfo : customerList)
        {
            UServicesInfo uServicesInfo = dbAdapter.getUServiceInfo(customerInfo.nCustNo, customerInfo.nChainNo);
            if(customerInfo.bCompleted && (uServicesInfo == null || uServicesInfo.strReason.equals("0")))
            {
                mSalesCustomerCnt++;
            }
            else if( uServicesInfo != null )
            {
                mNonSalesCustomerCnt++;
            }
        }
        dbAdapter.close();

        for(UInvenDInfo uInvenDInfo : uInvenDInfoList)
        {
            if(uInvenDInfo.nStock != 0 )
            {
                mEndingStockCnt += uInvenDInfo.nStock;
            }
        }

        mSalesCnt = Utils.getIntSetting(mContext, Global.TOTAL_SALES_COUNT);
        mSamplesCnt = Utils.getIntSetting(mContext, Global.TOTAL_SAMPLE_COUNT);
        mReturnsCnt = Utils.getIntSetting(mContext, Global.TOTAL_RETURNS_COUNT);


        refreshPage();
        refreshLanguage();
    }

    @Override
    public void refreshPage() {
        ((TextView) mRootView.findViewById(R.id.txtVisitsWithSales)).setText(String.valueOf(mSalesCustomerCnt));
        ((TextView) mRootView.findViewById(R.id.txtVisitsWithoutSales)).setText(String.valueOf(mNonSalesCustomerCnt));
        if(mNonSalesCustomerCnt + mSalesCustomerCnt != 0)
        {
            ((TextView) mRootView.findViewById(R.id.txtStrikeRate)).setText(String.format(Locale.ENGLISH, "%d%%", mSalesCustomerCnt * 100 / ( mNonSalesCustomerCnt + mSalesCustomerCnt)));
        }
        else
        {
            ((TextView) mRootView.findViewById(R.id.txtStrikeRate)).setText("0%");
        }

        if(!TextUtils.isEmpty(mCartonDescription))
        {
            if(mSalesCnt != 1)
            {
                ((TextView) mRootView.findViewById(R.id.txtTotalCustomerDeliveries)).setText(String.format(Locale.ENGLISH, "%d %ss", mSalesCnt, mCartonDescription));
            }
            else
            {
                ((TextView) mRootView.findViewById(R.id.txtTotalCustomerDeliveries)).setText(String.format(Locale.ENGLISH, "%d %s", mSalesCnt, mCartonDescription));
            }
        }
        else
        {
            ((TextView) mRootView.findViewById(R.id.txtTotalCustomerDeliveries)).setText(String.format(Locale.ENGLISH, "%d", mSalesCnt));
        }

        if(!TextUtils.isEmpty(mCartonDescription))
        {
            if(mSamplesCnt != 1)
            {
                ((TextView) mRootView.findViewById(R.id.txtTotalFreeGoods)).setText(String.format(Locale.ENGLISH, "%d %ss", mSamplesCnt, mCartonDescription));
            }
            else
            {
                ((TextView) mRootView.findViewById(R.id.txtTotalFreeGoods)).setText(String.format(Locale.ENGLISH, "%d %s", mSamplesCnt, mCartonDescription));
            }
        }
        else
        {
            ((TextView) mRootView.findViewById(R.id.txtTotalFreeGoods)).setText(String.format(Locale.ENGLISH, "%d", mSamplesCnt));
        }

        if(!TextUtils.isEmpty(mCartonDescription))
        {
            if(mReturnsCnt != 1)
            {
                ((TextView) mRootView.findViewById(R.id.txtTotalCustomerReturns)).setText(String.format(Locale.ENGLISH, "%d %ss", mReturnsCnt, mCartonDescription));
            }
            else
            {
                ((TextView) mRootView.findViewById(R.id.txtTotalCustomerReturns)).setText(String.format(Locale.ENGLISH, "%d %s", mReturnsCnt, mCartonDescription));
            }
        }
        else
        {
            ((TextView) mRootView.findViewById(R.id.txtTotalCustomerReturns)).setText(String.format(Locale.ENGLISH, "%d", mReturnsCnt));
        }

        int nNetDeliveries = mSalesCnt + mSamplesCnt - mReturnsCnt;
        if(!TextUtils.isEmpty(mCartonDescription))
        {
            if(nNetDeliveries != 1)
            {
                ((TextView) mRootView.findViewById(R.id.txtNetDeliveries)).setText(String.format(Locale.ENGLISH, "%d %ss", nNetDeliveries, mCartonDescription));
            }
            else
            {
                ((TextView) mRootView.findViewById(R.id.txtNetDeliveries)).setText(String.format(Locale.ENGLISH, "%d %s", nNetDeliveries, mCartonDescription));
            }
        }
        else
        {
            ((TextView) mRootView.findViewById(R.id.txtNetDeliveries)).setText(String.format(Locale.ENGLISH, "%d", nNetDeliveries));
        }

        if(!TextUtils.isEmpty(mCartonDescription))
        {
            if(mEndingStockCnt != 1)
            {
                ((TextView) mRootView.findViewById(R.id.txtEndingStock)).setText(String.format(Locale.ENGLISH, "%d %ss", mEndingStockCnt, mCartonDescription));
            }
            else
            {
                ((TextView) mRootView.findViewById(R.id.txtEndingStock)).setText(String.format(Locale.ENGLISH, "%d %s", mEndingStockCnt, mCartonDescription));
            }
        }
        else
        {
            ((TextView) mRootView.findViewById(R.id.txtEndingStock)).setText(String.format(Locale.ENGLISH, "%d", mEndingStockCnt));
        }

        int nDeliveries = mSalesCnt + mSamplesCnt;

        if(!TextUtils.isEmpty(mCartonDescription))
        {
            if(nDeliveries != 1)
            {
                ((TextView) mRootView.findViewById(R.id.txtCustomerDeliveries)).setText(String.format(Locale.ENGLISH, "%d %ss", nDeliveries, mCartonDescription));
            }
            else
            {
                ((TextView) mRootView.findViewById(R.id.txtCustomerDeliveries)).setText(String.format(Locale.ENGLISH, "%d %s", nDeliveries, mCartonDescription));
            }
        }
        else
        {
            ((TextView) mRootView.findViewById(R.id.txtCustomerDeliveries)).setText(String.format(Locale.ENGLISH, "%d", nDeliveries));
        }

        if(nDeliveries + mEndingStockCnt != 0)
        {
            ((TextView) mRootView.findViewById(R.id.txtUtilisation)).setText(String.format(Locale.ENGLISH, "%d%%", nDeliveries * 100 / (nDeliveries + mEndingStockCnt)));
        }
        else
        {
            ((TextView) mRootView.findViewById(R.id.txtUtilisation)).setText("0%");
        }

        float value = Utils.getFloatSetting(mContext, Global.SALE_TODAY);
        ((TextView)mRootView.findViewById(R.id.txtNetSalesValue)).setText(String.format(Locale.ENGLISH, Global.gCurrencySymbol + "%." + Utils.getIntSetting(mContext, Global.PREF_DECIMAL) + "f", Utils.getRoundedValue(value)));
    }

    @Override
    public void refreshLanguage() {
        ((TextView) mRootView.findViewById(R.id.lblTitle)).setText(Str.SalesPerformance[Global.LANG].toUpperCase());
        ((TextView) mRootView.findViewById(R.id.lblCustomerVisits)).setText(Str.CustomerVisits[Global.LANG]);
        ((TextView) mRootView.findViewById(R.id.lblVisitsWithSales)).setText(Str.VisitsWithSales[Global.LANG]);
        ((TextView) mRootView.findViewById(R.id.lblVisitsWithoutSales)).setText(Str.VisitsWithoutSales[Global.LANG]);
        ((TextView) mRootView.findViewById(R.id.lblStrikeRate)).setText(Str.StrikeRate[Global.LANG]);
        ((TextView) mRootView.findViewById(R.id.lblSales)).setText(Str.Sales[Global.LANG]);
        ((TextView) mRootView.findViewById(R.id.lblTotalCustomerDeliveries)).setText(Str.TotalCustomerDeliveries[Global.LANG]);
        ((TextView) mRootView.findViewById(R.id.lblTotalFreeGoods)).setText(Str.TotalFreeGoods[Global.LANG]);
        ((TextView) mRootView.findViewById(R.id.lblTotalCustomerReturns)).setText(Str.TotalCustomerReturns[Global.LANG]);
        ((TextView) mRootView.findViewById(R.id.lblNetDeliveries)).setText(Str.NetDeliveries[Global.LANG]);
        ((TextView) mRootView.findViewById(R.id.lblInventoryUtilisation)).setText(Str.InventoryUtilisation[Global.LANG]);
        ((TextView) mRootView.findViewById(R.id.lblEndingStock)).setText(Str.EndingStock[Global.LANG]);
        ((TextView) mRootView.findViewById(R.id.lblCustomerDeliveries)).setText(Str.CustomerDeliveries[Global.LANG]);
        ((TextView) mRootView.findViewById(R.id.lblUtilisation)).setText(Str.Utilisation[Global.LANG]);
    }

    @Override
    public void receiveBroadcast(String strAction, Intent intent) {

    }

    @Override
    public void onResume() {
        super.onResume();
        Global.gFragIdx = Global.FRAG_SALES_PERFORMANCE;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id)
        {
            case R.id.btnDone:
                Utils.setBooleanSetting(mContext, String.format(Locale.ENGLISH, Global.PREF_END_TRIP_CHECKED, 4), true);
                mContext.onBackPressed();
                break;
        }
    }
}

