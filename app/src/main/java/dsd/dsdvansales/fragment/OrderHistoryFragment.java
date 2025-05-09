package dsd.dsdvansales.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import dsd.dsdvansales.R;
import dsd.dsdvansales.db.DbAdapter;
import dsd.dsdvansales.dialogs.MessageDialog;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.model.OrderHistoryInfo;
import dsd.dsdvansales.model.ProdDetlInfo;
import dsd.dsdvansales.model.UInvenDInfo;
import dsd.dsdvansales.model.UInvenHInfo;


public class OrderHistoryFragment extends BaseFragment implements View.OnClickListener{

    private int mType = Global.INVENTORY_FRESH;
    private ListView mItemListView;
    private LinearLayout mHeaderLayout;
    ListItemAdapter mListItemAdapter;
    HorizontalScrollView mContentLayout;
    TextView mNoDataFoundMsg;
    private List<String> mSADateList = new ArrayList<>();
    private List<String> mBBDateList = new ArrayList<>();
    private Map<String, String> mProdSeqMap = new HashMap<>();
    private Map<String, String> mProdParentMap = new HashMap<>();
    private List<String> mSAItemList = new ArrayList<>();
    private List<String> mBBItemList = new ArrayList<>();
    private Map<String, OrderHistoryInfo> mDataMap = new HashMap<>();
    private Map<String, ProdDetlInfo> mProdDetlMap = new HashMap<>();
    private Map<String, List<OrderHistoryInfo>> mDateDataMap = new HashMap<>();
    List<OrderHistoryInfo> mOrderHistoryInfoList = null;
    UInvenHInfo mUInvenHinfo;
    private String mStrOrderNo = "";

    private boolean bShowCase = true;

    public OrderHistoryFragment() {
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
        mRootView = inflater.inflate(R.layout.fragment_order_history, container, false);
        commonInit();
        return mRootView;
    }

    // TODO: Rename method, update argument and hook method into UI event

    @Override
    public void initView() {
        bShowCase = Utils.getStringSetting(mContext, Global.PREF_INVENTORYUOM).equals("U") == false;
        Bundle bundle = getArguments();
        if(bundle != null)
        {
            mStrOrderNo = bundle.getString("OrderNo");
        }
        mRootView.findViewById(R.id.btnBack).setOnClickListener(this);
        mHeaderLayout = (LinearLayout)mRootView.findViewById(R.id.layoutDate);
        mItemListView = (ListView)mRootView.findViewById(R.id.listItem);
        mContentLayout = (HorizontalScrollView)mRootView.findViewById(R.id.layoutContent);
        mNoDataFoundMsg = (TextView)mRootView.findViewById(R.id.no_date_msg);

        loadData();
    }

    private void setAndSendBroadcast(String strDate)
    {
        List<OrderHistoryInfo> dateMapInfoList = mDateDataMap.get(strDate);
        Global.gOrderHistoryResultInfoList = dateMapInfoList;
        Intent intent = new Intent(Global.ACTION_ORDER_HISTORY);
        mContext.sendBroadcast(intent);
    }

    @Override
    public void loadData() {
        showProgressDialog(Str.Loading[Global.LANG]);
        new Thread()
        {
            @Override
            public void run() {
                DbAdapter dbAdapter = new DbAdapter(mContext);
                dbAdapter.open();
                mOrderHistoryInfoList = dbAdapter.getOrderHistoryList(Global.gCurCustomerInfo.nCustNo);
                dbAdapter.getProdDetlListWithInactiveItems(mProdDetlMap, null);
                dbAdapter.getProdStruObjectEntryIDMap(mProdSeqMap);
                dbAdapter.getProdStruParentIdEntryMap(mProdParentMap);
                mUInvenHinfo = dbAdapter.getUInvenHInfo(Utils.getLongSetting(mContext, Global.PREF_UINVEN_TRXN_NO));
                if (mUInvenHinfo == null) {
                    mUInvenHinfo = new UInvenHInfo();
                }
                mUInvenHinfo.uInvenDList = dbAdapter.getUInvenDList(Utils.getLongSetting(mContext, Global.PREF_UINVEN_TRXN_NO));
                dbAdapter.close();

                mSADateList.clear();
                mSAItemList.clear();
                mDataMap.clear();

                Map<String, Boolean> mSADateMap = new HashMap<>();
                Map<String, Boolean> mBBDateMap = new HashMap<>();
                Map<String, Boolean> mSAItemMap = new HashMap<>();
                Map<String, Boolean> mBBItemMap = new HashMap<>();

                for(OrderHistoryInfo info : mOrderHistoryInfoList)
                {
                    Boolean bValue = mSADateMap.get(info.strOrderDate);
                    if( bValue == null || bValue != true )
                    {
                        if(info.nSAQty > 0 )
                        {
                            mSADateMap.put(info.strOrderDate, true);
                            mSADateList.add(info.strOrderDate);
                        }
                    }

                    bValue = mBBDateMap.get(info.strOrderDate);
                    if( bValue == null || bValue != true )
                    {
                        if(info.nBBQty > 0 )
                        {
                            mBBDateMap.put(info.strOrderDate, true);
                            mBBDateList.add(info.strOrderDate);
                        }
                    }

                    bValue = mSAItemMap.get(info.strItemNo);
                    if( bValue == null || bValue != true )
                    {
                        if(info.nSAQty > 0 )
                        {
                            mSAItemMap.put(info.strItemNo, true);
                            mSAItemList.add(info.strItemNo);
                        }
                    }

                    bValue = mBBItemMap.get(info.strItemNo);
                    if( bValue == null || bValue != true )
                    {
                        if(info.nBBQty > 0 )
                        {
                            mBBItemMap.put(info.strItemNo, true);
                            mBBItemList.add(info.strItemNo);
                        }
                    }

                    mDataMap.put(info.strOrderDate + "+" + info.strItemNo, info);

                    List<OrderHistoryInfo> dateMapInfoList = mDateDataMap.get(info.strOrderDate);
                    if( dateMapInfoList == null )
                    {
                        dateMapInfoList = new ArrayList<>();
                        mDateDataMap.put(info.strOrderDate, dateMapInfoList);
                    }
                    dateMapInfoList.add(info);
                }

                Collections.sort(mSAItemList, (lhs, rhs) -> {
                    String lEntryValue = mProdSeqMap.get(lhs);
                    String rEntryValue = mProdSeqMap.get(rhs);
                    if( lEntryValue == null && rEntryValue == null ) return 0;
                    else if( lEntryValue == null && rEntryValue != null ) return 1;
                    else if( lEntryValue != null && rEntryValue == null ) return -1;
                    else return CompareProdSeq(lEntryValue, rEntryValue);
                });

                Collections.sort(mBBItemList, (lhs, rhs) -> {
                    String lEntryValue = mProdSeqMap.get(lhs);
                    String rEntryValue = mProdSeqMap.get(rhs);
                    if( lEntryValue == null && rEntryValue == null ) return 0;
                    else if( lEntryValue == null && rEntryValue != null ) return 1;
                    else if( lEntryValue != null && rEntryValue == null ) return -1;
                    else return CompareProdSeq(lEntryValue, rEntryValue);
                });


                Collections.sort(mSADateList, (s, t1) -> t1.compareTo(s));
                Collections.sort(mBBDateList, (s, t1) -> t1.compareTo(s));

                new Handler(Looper.getMainLooper()).post(() -> {
                    hideProgressDialog();
                    boolean bVisible;
                    if(mSADateList.size() == 0)
                    {
                        mRootView.findViewById(R.id.txtDeliver).setVisibility(View.GONE);
                        mRootView.findViewById(R.id.blank).setVisibility(View.VISIBLE);
                        bVisible = true;
                    }
                    else
                    {
                        mRootView.findViewById(R.id.txtDeliver).setVisibility(View.VISIBLE);
                        mRootView.findViewById(R.id.txtDeliver).setOnClickListener(OrderHistoryFragment.this);
                        mRootView.findViewById(R.id.blank).setVisibility(View.GONE);
                        bVisible = false;
                    }
                    if(mBBDateList.size() == 0)
                    {
                        mRootView.findViewById(R.id.txtReturns).setVisibility(View.GONE);
                        if(!bVisible)
                            mRootView.findViewById(R.id.blank).setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        mRootView.findViewById(R.id.txtReturns).setVisibility(View.VISIBLE);
                        mRootView.findViewById(R.id.txtReturns).setOnClickListener(OrderHistoryFragment.this);
                        if(!bVisible)
                            mRootView.findViewById(R.id.blank).setVisibility(View.GONE);
                    }
                    if( mOrderHistoryInfoList == null || mOrderHistoryInfoList.size() == 0 )
                    {
                        mContentLayout.setVisibility(View.GONE);
                        mNoDataFoundMsg.setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        mContentLayout.setVisibility(View.VISIBLE);
                        mNoDataFoundMsg.setVisibility(View.GONE);
                    }

                    mListItemAdapter = new ListItemAdapter();
                    mItemListView.setAdapter(mListItemAdapter);

                    refreshLanguage();
                    setType(mType);
                });
            }
        }.start();
    }

    private int CompareProdSeq(String lEntryVal, String rEntryVal)
    {
        List<String> lEntryParentList = new ArrayList<>();
        List<String> rEntryParentList = new ArrayList<>();
        lEntryParentList.add(lEntryVal);
        rEntryParentList.add(rEntryVal);
        getParentEntry(lEntryVal, lEntryParentList);
        getParentEntry(rEntryVal, rEntryParentList);

        while(lEntryParentList.size() > 0 && rEntryParentList.size() > 0)
        {
            String lValue = lEntryParentList.get(lEntryParentList.size() - 1);
            String rValue = rEntryParentList.get(rEntryParentList.size() - 1);
            if( lValue.equals(rValue) )
            {
                lEntryParentList.remove(lEntryParentList.size() - 1);
                rEntryParentList.remove(rEntryParentList.size() - 1);
            }
            else
            {
                return lValue.compareTo(rValue);
            }
        }
        return 0;
    }

    private void getParentEntry(String strEntryID, List<String> parentList)
    {
        String strParentIDObj = mProdParentMap.get(strEntryID);
        if( strParentIDObj == null ) strParentIDObj = "0";
        parentList.add(strParentIDObj);
        if( strParentIDObj.equals("0") )
        {
            return;
        }
        else
        {
            getParentEntry(strParentIDObj, parentList);
        }
    }

    @Override
    public void refreshPage() {
        mHeaderLayout.removeAllViews();
        List<String> mDateList = null;
        if( mType == Global.INVENTORY_FRESH)
        {
            mDateList = mSADateList;
        }
        else
        {
            mDateList = mBBDateList;
        }
        for(int i = 0; i < mDateList.size(); i++)
        {
            TextView itemView = (TextView)mContext.getLayoutInflater().inflate(R.layout.item_order_history_selectable, mHeaderLayout, false);
            mHeaderLayout.addView(itemView);
            SimpleDateFormat sdf = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT);
            try {
                Date date = sdf.parse(mDateList.get(i));
                itemView.setText(String.format(Locale.ENGLISH, "%d/%d", date.getDate(), date.getMonth() + 1));
            } catch (ParseException e) {
                e.printStackTrace();
                itemView.setText("");
            }
            itemView.setTag(mDateList.get(i));
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(mStrOrderNo.equals("") || mStrOrderNo.equals("0"))
                    {
                        final String strDate = (String)view.getTag();
                        String strShowDate = "";
                        SimpleDateFormat sdf = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT);
                        try {
                            Date date = sdf.parse(strDate);
                            strShowDate = String.format(Locale.ENGLISH, "%d/%d", date.getDate(), date.getMonth() + 1);
                        } catch (ParseException e) {
                            e.printStackTrace();
                            strShowDate = "";
                        }
                        try {
                            final MessageDialog msgDialog = new MessageDialog(mContext);
                            msgDialog.setCancelable(false);
                            msgDialog.mStrMessage = String.format(Locale.ENGLISH, Str.Do_you_wish_to_use_the_history_from[Global.LANG], strShowDate);
                            msgDialog.mStrLeft = Str.No[Global.LANG];
                            msgDialog.mStrRight = Str.Yes[Global.LANG];
                            msgDialog.show();
                            msgDialog.setOnDismissListener(new DialogInterface.OnDismissListener(){
                                @Override
                                public void onDismiss(DialogInterface arg0) {
                                    if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
                                        mContext.onBackPressed();
                                        setAndSendBroadcast(strDate);
                                    }
                                }
                            });
                        }catch (Exception e)
                        {
                            e.printStackTrace();
                        }

                    }
                    else
                    {
                        try {
                            final MessageDialog msgDialog = new MessageDialog(mContext);
                            msgDialog.setCancelable(false);
                            msgDialog.mStrMessage = Str.You_can_not_use_history[Global.LANG];
                            msgDialog.mStrMiddle = Str.Ok[Global.LANG];
                            msgDialog.show();
                        }catch (Exception e)
                        {
                            e.printStackTrace();
                        }

                    }

                }
            });
        }

        if(mDateList.size() == 0)
        {
            mContentLayout.setVisibility(View.GONE);
            mNoDataFoundMsg.setVisibility(View.VISIBLE);
        }
        else
        {
            mContentLayout.setVisibility(View.VISIBLE);
            mNoDataFoundMsg.setVisibility(View.GONE);
            mListItemAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void refreshLanguage() {
        ((TextView) mRootView.findViewById(R.id.lblTitle)).setText(Str.OrderHistory[Global.LANG].toUpperCase());
        ((TextView) mRootView.findViewById(R.id.txtDeliver)).setText(Str.Orders[Global.LANG]);
        ((TextView) mRootView.findViewById(R.id.txtReturns)).setText(Str.Returns[Global.LANG]);
        ((TextView) mRootView.findViewById(R.id.txtCustomerName)).setText(Global.gCurCustomerInfo.strName);
        mNoDataFoundMsg.setText(Str.No_data_has_been_found[Global.LANG]);
    }

    @Override
    public void receiveBroadcast(String strAction, Intent intent) {

    }

    private void setType(int nType) {
        mType = nType;
        int nNormalColor = mContext.getResources().getColor(R.color.vehicle_inventory_normal_color);
        int nSelectedColor = mContext.getResources().getColor(R.color.vehicle_inventory_selected_color);
        mRootView.findViewById(R.id.txtDeliver).setBackgroundColor(nNormalColor);
        mRootView.findViewById(R.id.txtReturns).setBackgroundColor(nNormalColor);

        if (mType == Global.INVENTORY_FRESH) {
            mRootView.findViewById(R.id.txtDeliver).setBackgroundColor(nSelectedColor);
        } else if (mType == Global.INVENTORY_RETURNS) {
            mRootView.findViewById(R.id.txtReturns).setBackgroundColor(nSelectedColor);
        }
        refreshPage();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnBack:
                mContext.onBackPressed();
                break;
            case R.id.txtDeliver:
                setType(Global.INVENTORY_FRESH);
                break;
            case R.id.txtReturns:
                setType(Global.INVENTORY_RETURNS);
                break;
        }
    }

    private class ViewHolder {
        public List<TextView> valueTxtView = new ArrayList<>();
    }

    public class ListItemAdapter extends BaseAdapter
    {

        @Override
        public int getCount() {
            if(mType == Global.INVENTORY_FRESH)
            {
                return mSAItemList.size();
            }
            else
            {
                return mBBItemList.size();
            }
        }

        @Override
        public Object getItem(int i) {
            if(mType == Global.INVENTORY_FRESH)
            {
                if(i < mSADateList.size())
                {
                    return mSAItemList.get(i);
                }
                else
                {
                    return null;
                }
            }
            else
            {
                if(i < mBBDateList.size())
                {
                    return mBBItemList.get(i);
                }
                else
                {
                    return null;
                }
            }
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public boolean isEnabled(int position) {
            return false;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            convertView = mContext.getLayoutInflater().inflate(R.layout.item_order_history_list, parent, false);
            LinearLayout itemLayout = (LinearLayout)convertView.findViewById(R.id.layout_value);
            holder = new ViewHolder();
            List<String> mDateList = null;
            if( mType == Global.INVENTORY_FRESH)
            {
                mDateList = mSADateList;
            }
            else
            {
                mDateList = mBBDateList;
            }
            for(int i = 0; i < mDateList.size(); i++)
            {
                TextView itemView = (TextView)mContext.getLayoutInflater().inflate(R.layout.item_order_history, itemLayout, false);
                holder.valueTxtView.add(itemView);
                itemLayout.addView(itemView);
            }
            convertView.setTag(holder);

            String strDescription = "";
            ProdDetlInfo detlInfo;
            boolean isInactive = false;
            try
            {
                if( mType == Global.INVENTORY_FRESH)
                {
                    detlInfo = mProdDetlMap.get(mSAItemList.get(position));
                    strDescription = detlInfo.strShortDesc;
                }
                else
                {
                    detlInfo = mProdDetlMap.get(mBBItemList.get(position));
                    strDescription = detlInfo.strShortDesc;
                }
                isInactive = detlInfo.isInactive();
            }catch(Exception e)
            {
                e.printStackTrace();
            }

            ((TextView)convertView.findViewById(R.id.txtName)).setText(strDescription);

            if (isInactive) {
                ((TextView)convertView.findViewById(R.id.txtName)).setTextColor(Color.GRAY);
            }
            else {
                ((TextView)convertView.findViewById(R.id.txtName)).setTextColor(Color.BLACK);
            }

            OrderHistoryInfo info = null;
            for(int i = 0; i < mDateList.size(); i++)
            {
                String key;
                if (isInactive) {
                    holder.valueTxtView.get(i).setTextColor(Color.GRAY);
                }
                else {
                    holder.valueTxtView.get(i).setTextColor(Color.BLACK);
                }

                if( mType == Global.INVENTORY_FRESH)
                {
                    key = mDateList.get(i) + "+" + mSAItemList.get(position);
                }
                else
                {
                    key = mDateList.get(i) + "+" + mBBItemList.get(position);
                }
                info = mDataMap.get(key);
                if( info == null )
                {
                    holder.valueTxtView.get(i).setText("0");
                }
                else
                {
                    if(info.nSCWType == Global.WEIGHT_ITEM)
                    {
                        DecimalFormat df = new DecimalFormat("###.##");
                        if(mType == Global.INVENTORY_FRESH)
                        {
                            holder.valueTxtView.get(i).setText(df.format(info.nSAQty / 100.0f));
                        }
                        else
                        {
                            holder.valueTxtView.get(i).setText(df.format(-1 * info.nBBQty / 100.0f));
                        }
                    }
                    else
                    {
                        if(mType == Global.INVENTORY_FRESH)
                        {
                            holder.valueTxtView.get(i).setText(String.valueOf(info.nSAQty / 100));
                        }
                        else
                        {
                            holder.valueTxtView.get(i).setText(String.valueOf(-1 * info.nBBQty / 100));
                        }
                    }

                }
            }
            convertView.setOnLongClickListener(view -> {
                String strItemNo = "";
                if( mType == Global.INVENTORY_FRESH)
                {
                    strItemNo = mProdDetlMap.get(mSAItemList.get(position)).strItemNo;
                }
                else
                {
                    strItemNo = mProdDetlMap.get(mBBItemList.get(position)).strItemNo;
                }
                int nInvenCheck = Utils.getIntSetting(mContext, Global.PREF_INVENCKECK);
                if( nInvenCheck == 1 || nInvenCheck == 2 )
                {
                    int nStock = getStockValue(strItemNo);
                    if(bShowCase)
                    {
                        String strCaption = "";
                        if (Utils.isContainerItem(mContext, strItemNo)) {
                            strCaption = String.format(Locale.ENGLISH, "Available %d", nStock);
                        }
                        else {
                            int nCase = getCaseValue(strItemNo);
                            strCaption = String.format(Locale.ENGLISH, "Available %d / %d", nStock / nCase, nStock % nCase);
                        }
                        if(!mContext.isFinishing() && !mContext.isDestroyed())
                        {
                            try {
                                Toast.makeText(mContext, strCaption, Toast.LENGTH_SHORT).show();
                            }catch (Exception e1)
                            {
                                e1.printStackTrace();
                            }
                        }

                    }
                    else
                    {
                        String strCaption = String.format(Locale.ENGLISH, "Available %d", nStock);
                        if(!mContext.isFinishing() && !mContext.isDestroyed())
                        {
                            try {
                                Toast.makeText(mContext, strCaption, Toast.LENGTH_SHORT).show();
                            }catch (Exception e1)
                            {
                                e1.printStackTrace();
                            }
                        }

                    }

                }
                return true;
            });
            return convertView;
        }
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

    Map<String, Integer> mStockMap = new HashMap<>();
    private int getStockValue(String strItemNo) {
        if( mStockMap.get(strItemNo) != null )
            return mStockMap.get(strItemNo);
        if( mUInvenHinfo != null && mUInvenHinfo.uInvenDList != null )
        {
            int nStock = 0;
            for(UInvenDInfo info : mUInvenHinfo.uInvenDList)
            {
                if( info.strItemNo.equals(strItemNo) )
                    nStock += info.nStock;
            }
            mStockMap.put(strItemNo, nStock);
            return nStock;
        }
        else
            return 0;

    }
}
