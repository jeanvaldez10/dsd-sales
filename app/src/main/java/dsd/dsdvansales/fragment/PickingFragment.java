package dsd.dsdvansales.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import dsd.dsdvansales.R;
import dsd.dsdvansales.activities.BarcodeScannerActivity;
import dsd.dsdvansales.db.DbAdapter;
import dsd.dsdvansales.dialogs.MessageDialog;
import dsd.dsdvansales.dialogs.PickingNotCompletedDialog;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.model.ProdDetlInfo;
import dsd.dsdvansales.model.UOrderDInfo;
import dsd.dsdvansales.views.AlertSingleChoiceAdapter;

public class PickingFragment extends BaseFragment implements View.OnClickListener {

    private TextView mTxtNoData = null;
    private ListView mListView = null;
    private ListAdapter mListAdapter = null;
    private Map<String, ProdDetlInfo> mProdUPCDetMap = new HashMap<>();
    private List<ProdDetlInfo> mProdDetlList = new ArrayList<>();
    private int mProdDetlIdx = -1;

    private String sortFlag = "";
    private boolean filter = false;
    private List<UOrderDInfo> mUOrderDList = new ArrayList<>();

    public PickingFragment() {
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
        mRootView =  inflater.inflate(R.layout.fragment_picking, container, false);
        commonInit();
        return mRootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        Global.gFragIdx = Global.FRAG_PICKING;
    }

    private String getItemCode(String strCode)
    {
        String strPrefix = Utils.getStringSetting(mContext, Global.PREF_BARCODE_PREFIX);
        String strSuffix = Utils.getStringSetting(mContext, Global.PREF_BARCODE_SUFFIX);
        if(!TextUtils.isEmpty(strPrefix))
        {
            String[] prefixArray = strPrefix.split(",");
            for(int i = 0; i < prefixArray.length; i++)
            {
                if(strCode.startsWith(prefixArray[i].trim()))
                {
                    strCode = strCode.substring(prefixArray[i].trim().length());
                    break;
                }
            }
        }
        if(!TextUtils.isEmpty(strSuffix))
        {
            String[] suffixArray = strSuffix.split(",");
            for(int i = 0; i < suffixArray.length; i++)
            {
                if(strCode.endsWith(suffixArray[i].trim()))
                {
                    strCode = strCode.substring(0, strCode.length() - suffixArray[i].trim().length());
                }
            }
        }
        return strCode;
    }

    @Override
    public void initView() {
        mRootView.findViewById(R.id.btnBarcodeScanner).setOnClickListener(this);
        mRootView.findViewById(R.id.btnBack).setOnClickListener(this);
        mRootView.findViewById(R.id.btnCompleted).setOnClickListener(this);
        mRootView.findViewById(R.id.btnFilter).setOnClickListener(this);
        mRootView.findViewById(R.id.btnSort).setOnClickListener(this);
        mRootView.findViewById(R.id.btnAdd).setOnClickListener(this);

        mTxtNoData = (TextView) mRootView.findViewById(R.id.txtNoData);

        mListView = (ListView) mRootView.findViewById(R.id.listView);
        mListAdapter = new ListAdapter();
        mListView.setAdapter(mListAdapter);
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem + visibleItemCount >= totalItemCount) {
                    mRootView.findViewById(R.id.txtMore).setVisibility(View.GONE);
                } else {
                    mRootView.findViewById(R.id.txtMore).setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }
        });

        ((EditText) mRootView.findViewById(R.id.editItemNo)).addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String strItemCode = getItemCode(s.toString());
                String strItemNo = strItemCode;
                ProdDetlInfo prodDetlInfo = mProdUPCDetMap.get(strItemNo);
                if(prodDetlInfo != null)
                {
                    strItemNo = prodDetlInfo.strItemNo;
                    if(!strItemNo.equals(s.toString()))
                    {
                        for(UOrderDInfo uOrderDInfo : mUOrderDList)
                        {
                            if(uOrderDInfo.strItemNo.equals(strItemNo))
                            {
                                int nPicked = 0;
                                if(Global.pickedStatusMap.get(uOrderDInfo.strItemNo) != null)
                                {
                                    nPicked = Global.pickedStatusMap.get(uOrderDInfo.strItemNo);
                                }
                                nPicked++;
                                if(nPicked > uOrderDInfo.nSales)
                                {
                                    try {
                                        final MessageDialog msgDialog = new MessageDialog(mContext);
                                        msgDialog.setCancelable(false);
                                        msgDialog.mStrMessage = String.format(Locale.ENGLISH, Str.PickedOverFlowAlreadyPicked[Global.LANG], uOrderDInfo.strShortDesc, uOrderDInfo.nSales);
                                        msgDialog.mStrLeft = Str.No[Global.LANG];
                                        msgDialog.mStrRight = Str.Yes[Global.LANG];
                                        int finalNPicked = nPicked;
                                        msgDialog.setOnDismissListener(dialog1 -> {
                                            if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {

                                            } else if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
                                                Global.pickedStatusMap.put(uOrderDInfo.strItemNo, finalNPicked);
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
                                else {
                                    Global.pickedStatusMap.put(uOrderDInfo.strItemNo, nPicked);
                                    refreshPage();
                                }
                                return;
                            }
                        }
                    }

                    doSearch();
                }
                else {
                    doSearch();
                }
            }
        });

        ((EditText) mRootView.findViewById(R.id.editItemNo)).setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                showKeyboard(((EditText) mRootView.findViewById(R.id.editQty)), true);
                return true;
            }
            return false;
        });

        ((EditText) mRootView.findViewById(R.id.editQty)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(mRootView.findViewById(R.id.editQty).getTag() == null)
                {
                    setQtyFormat();
                }
            }
        });

        ((EditText) mRootView.findViewById(R.id.editQty)).setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                doAdd();
                return true;
            }
            return false;
        });

        ((EditText)mRootView.findViewById(R.id.editQty)).setInputType(InputType.TYPE_CLASS_NUMBER);
        loadData();
        refreshLanguage();
    }

    private void setQtyFormat()
    {
        if( mProdDetlIdx != -1 )
        {
            ProdDetlInfo productInfo = mProdDetlList.get(mProdDetlIdx);
            if(Utils.getProdLoconSCWType(mContext, productInfo.strItemNo) == Global.WEIGHT_ITEM)
            {
                EditText editQty = (EditText)mRootView.findViewById(R.id.editQty);
                String strVal = editQty.getText().toString();
                if(editQty.getTag() == null)
                {
                    String strArray[] = strVal.split("\\.");
                    strVal = "";
                    for (int i=0; i<strArray.length; i++) {
                        strVal += strArray[i];
                    }
                    double dValue = Utils.parseFloatFromString(strVal) / 100;
                    editQty.setTag("From App");
                    editQty.setText(String.format(Locale.ENGLISH, "%.2f", dValue));
                    editQty.setSelection(editQty.getText().toString().length());
                    editQty.setTag(null);
                }
            }
        }
    }

    private void doAdd()
    {
        String strQty = ((EditText) mRootView.findViewById(R.id.editQty)).getText().toString();
        if (mProdDetlIdx == -1) {
            if(!TextUtils.isEmpty(((EditText) mRootView.findViewById(R.id.editItemNo)).getText().toString()))
            {
                Utils.printDebug(mContext, Str.Please_select_valid_product[Global.LANG]);
            }
            ((EditText) mRootView.findViewById(R.id.editQty)).setText("");
            focusAndHideKeyboard((EditText) mRootView.findViewById(R.id.editItemNo));
            return;
        }

        int nIdx = -1;
        final ProdDetlInfo productInfo = mProdDetlList.get(mProdDetlIdx);
        for (int i = 0; i < mUOrderDList.size(); i++) {
            if (mUOrderDList.get(i).strItemNo.equals(productInfo.strItemNo) == true) {
                nIdx = i;
                break;
            }
        }

        if (strQty.length() == 0) {
            strQty = "0";
            ((EditText) mRootView.findViewById(R.id.editQty)).setText(strQty);
        }

        int nQuantity = 0;
        nQuantity = Utils.parseIntFromString(strQty);

        if (nIdx == -1) {
            if(!TextUtils.isEmpty(((EditText) mRootView.findViewById(R.id.editItemNo)).getText().toString()))
            {
                Utils.printDebug(mContext, Str.Please_select_valid_product[Global.LANG]);
            }
            ((EditText) mRootView.findViewById(R.id.editQty)).setText("");
            focusAndHideKeyboard((EditText) mRootView.findViewById(R.id.editItemNo));
            return;
        }

        if(nQuantity > mUOrderDList.get(nIdx).nSales)
        {
            try {
                final MessageDialog msgDialog = new MessageDialog(mContext);
                msgDialog.setCancelable(false);
                msgDialog.mStrMessage = String.format(Locale.ENGLISH, Str.PickedOverFlow[Global.LANG], mUOrderDList.get(nIdx).strShortDesc, mUOrderDList.get(nIdx).nSales);
                msgDialog.mStrLeft = Str.No[Global.LANG];
                msgDialog.mStrRight = Str.Yes[Global.LANG];
                int finalNIdx = nIdx;
                int finalNQuantity = nQuantity;
                msgDialog.setOnDismissListener(dialog1 -> {
                    if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {

                    } else if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
                        Global.pickedStatusMap.put(mUOrderDList.get(finalNIdx).strItemNo, finalNQuantity);
                        ((EditText) mRootView.findViewById(R.id.editItemNo)).setText("");
                        ((EditText) mRootView.findViewById(R.id.editQty)).setText("");

                        focusAndHideKeyboard((EditText) mRootView.findViewById(R.id.editItemNo));
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

        Global.pickedStatusMap.put(mUOrderDList.get(nIdx).strItemNo, nQuantity);
        ((EditText) mRootView.findViewById(R.id.editItemNo)).setText("");
        ((EditText) mRootView.findViewById(R.id.editQty)).setText("");

        focusAndHideKeyboard((EditText) mRootView.findViewById(R.id.editItemNo));
        refreshPage();
    }

    private void doSearch()
    {
        String strItemNo = getItemCode(((EditText) mRootView.findViewById(R.id.editItemNo)).getText().toString());
        String strDescription = Str.Invalid_Product[Global.LANG];

        mProdDetlIdx = -1;
        if (strItemNo.length() > 0) {
            for (int i = 0; i < mProdDetlList.size(); i++) {
                ProdDetlInfo prodDetlInfo = mProdDetlList.get(i);
                if (Utils.getDisplayItemCode(mContext, prodDetlInfo.strItemNo).equals(strItemNo)) {
                    mProdDetlIdx = i;
                    strDescription = prodDetlInfo.strDescription;
                    break;
                }
            }
            if (mProdDetlIdx == -1) {
                for (int i = 0; i < mProdDetlList.size(); i++) {
                    ProdDetlInfo prodDetlInfo = mProdDetlList.get(i);
                    if (prodDetlInfo.strItemUPC.equals(strItemNo)) {
                        mProdDetlIdx = i;
                        strDescription = prodDetlInfo.strDescription;
                        break;
                    }
                }
            }
            if (mProdDetlIdx == -1) {
                for (int i = 0; i < mProdDetlList.size(); i++) {
                    ProdDetlInfo prodDetlInfo = mProdDetlList.get(i);
                    if (prodDetlInfo.strItemUPC20.equals(strItemNo)) {
                        mProdDetlIdx = i;
                        strDescription = prodDetlInfo.strDescription;
                        break;
                    }
                }
            }

            if(mProdDetlIdx != -1)
            {
                //Search from the list.
                boolean bExist = false;
                for(int i = 0; i < mUOrderDList.size(); i++)
                {
                    if(mUOrderDList.get(i).strItemNo.equals(mProdDetlList.get(mProdDetlIdx).strItemNo))
                    {
                        bExist = true;
                        break;
                    }
                }
                if(!bExist)
                {
                    mProdDetlIdx = -1;
                    strDescription = Str.Invalid_Product[Global.LANG];
                }
            }


            ((TextView) mRootView.findViewById(R.id.txtDescription)).setText(strDescription);
            ((EditText) mRootView.findViewById(R.id.editQty)).setText("0");

        }
        else
        {
            ((TextView) mRootView.findViewById(R.id.txtDescription)).setText("");
            ((EditText) mRootView.findViewById(R.id.editQty)).setText("0");
        }
    }

    @Override
    public void loadData() {

        mContext.showProgressDialog(Str.Loading[Global.LANG]);
        new Thread()
        {
            @Override
            public void run() {
                mProdDetlList.clear();
                mProdUPCDetMap.clear();
                DbAdapter dbAdapter = new DbAdapter(mContext);
                dbAdapter.open();
                mProdDetlList = dbAdapter.getProdDetlList(mProdUPCDetMap);
                dbAdapter.close();

                new Handler(Looper.getMainLooper()).post(() -> {
                    mContext.hideProgressDialog();
                    mUOrderDList.clear();
                    mUOrderDList.addAll(Global.gUOrderHInfo.deliveryList);

                    refreshPage();
                });
            }
        }.start();
    }

    private void doFilter()
    {
        filter = !filter;
        if(filter)
        {
            ((ImageView)mRootView.findViewById(R.id.btnFilter)).setImageDrawable(mContext.getResources().getDrawable(R.drawable.icon_filter_fill));
        }
        else
        {
            ((ImageView)mRootView.findViewById(R.id.btnFilter)).setImageDrawable(mContext.getResources().getDrawable(R.drawable.icon_filter));
        }
        refreshPage();
    }

    private Map<String, Map<String, String>> sortMap = new HashMap<>();
    private Map<String, String> getSortItemMap(String strItemNo)
    {
        Map<String, String> map = sortMap.get(strItemNo);
        if(map == null)
        {
            map = Utils.getPropertyMap(mProdUPCDetMap.get(strItemNo));
            sortMap.put(strItemNo, map);
        }
        return map;
    }

    private void setSortFlag(String flag)
    {
        if(TextUtils.isEmpty(flag))
        {
            sortFlag = "code";
        }
        else if(flag.equalsIgnoreCase("ProdGroup"))
        {
            sortFlag = "group";
        }
        else if(flag.equalsIgnoreCase("MarketGroup"))
        {
            sortFlag = "market_group";
        }
        else if(flag.equalsIgnoreCase("PromoGroup"))
        {
            sortFlag = "promo_group";
        }
        else if(flag.equalsIgnoreCase("ItemType"))
        {
            sortFlag = "item_type";
        }
        else if(flag.equalsIgnoreCase("ItemUPC"))
        {
            sortFlag = "upc";
        }
        else if(flag.equalsIgnoreCase("Description"))
        {
            sortFlag = "desc";
        }
        else if(flag.equalsIgnoreCase("ShortDesc"))
        {
            sortFlag = "short_desc";
        }
        else if(flag.equalsIgnoreCase("Brand"))
        {
            sortFlag = "brand";
        }
        else
        {
            sortFlag = "code";
        }
    }

    private void doSort()
    {
        int nOrigIdx = 0;
        for(int i = 0; i < Global.sortArray.length; i++)
        {
            if(Utils.getSortValue(Global.sortArray[i]).equals(sortFlag))
            {
                nOrigIdx = i;
                break;
            }
        }
        final int nFinalOrigIdx = nOrigIdx;
        try {
            new AlertDialog.Builder(mContext)
                    .setTitle(Str.SelectSortOption[Global.LANG])
                    .setSingleChoiceItems(new AlertSingleChoiceAdapter(mContext, Global.sortArray), nOrigIdx, (dialog, nSelected) -> setSortFlag(Global.sortArray[nSelected]))
                    .setPositiveButton(Str.Ok[Global.LANG], (dialog, nSelected) -> {
                        refreshPage();
                    })
                    .setNegativeButton(Str.Cancel[Global.LANG], (dialog, nSelected) -> setSortFlag(Global.sortArray[nFinalOrigIdx]))
                    .create()
                    .show();
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void SortData(List<UOrderDInfo> uInvenDInfoList)
    {
        Collections.sort(uInvenDInfoList, (t1, t2) -> {
            Map<String, String> map1 = getSortItemMap(t1.strItemNo);
            Map<String, String> map2 = getSortItemMap(t2.strItemNo);
            String value1 = map1.get(sortFlag);
            String value2 = map2.get(sortFlag);
            if(value1 == null && value2 == null)
            {
                return 0;
            }
            else if(value1 != null && value2 == null)
            {
                return 1;
            }
            else if(value1 == null && value2 != null)
            {
                return -1;
            }
            else
            {
                return value1.compareTo(value2);
            }
        });
    }

    @Override
    public void refreshPage() {
        List<UOrderDInfo> list = new ArrayList<>();
        for (UOrderDInfo info : mUOrderDList) {
            int nPicked = 0;
            if(Global.pickedStatusMap.get(info.strItemNo) != null)
            {
                nPicked = Global.pickedStatusMap.get(info.strItemNo);
            }
            if(filter && nPicked == 0) continue;
            list.add(info);
        }

        SortData(list);

        mRootView.findViewById(R.id.btnSort).setVisibility(View.VISIBLE);

        int nPicked = 0;
        int nSales = 0;
        for(UOrderDInfo info : mUOrderDList)
        {
            nSales += info.nSales;
            int picked = 0;
            if(Global.pickedStatusMap.get(info.strItemNo) != null)
            {
                picked = Global.pickedStatusMap.get(info.strItemNo);
            }
            nPicked += picked;
        }

        ((TextView)mRootView.findViewById(R.id.txtPicked)).setText(String.valueOf(nPicked));
        ((TextView)mRootView.findViewById(R.id.txtSales)).setText(String.valueOf(nSales));

        if (list.size() > 0) {
            mListView.setVisibility(View.VISIBLE);
            mTxtNoData.setVisibility(View.GONE);
            mListAdapter.setData(list);
            mListAdapter.notifyDataSetChanged();
        } else {
            mListView.setVisibility(View.GONE);
            mTxtNoData.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void refreshLanguage() {
        ((TextView) mRootView.findViewById(R.id.lblTitle)).setText(Str.Picking[Global.LANG].toUpperCase());
        ((TextView) mRootView.findViewById(R.id.txtTrip)).setText(Str.Trip[Global.LANG] + " # " + Global.gTripNumber);
        ((TextView) mRootView.findViewById(R.id.txtDescription)).setText("");
        ((Button) mRootView.findViewById(R.id.btnCompleted)).setText(Str.Completed[Global.LANG]);
        ((Button) mRootView.findViewById(R.id.btnAdd)).setText(Str.Pick[Global.LANG]);
        ((TextView) mRootView.findViewById(R.id.lblSales)).setText(Str.Sales[Global.LANG]);
        ((TextView) mRootView.findViewById(R.id.lblPicked)).setText(Str.Picked[Global.LANG]);
    }

    @Override
    public void receiveBroadcast(String strAction, Intent intent) {
        if (strAction.equals(Global.ACTION_SELECT_PRODUCT) == true) {
            int nType = intent.getIntExtra(Global.EXTRA_TYPE, 0);
            if (nType == Global.SELECT_PRODUCT_ITEMUPC) {
                String strFrom = intent.getStringExtra(Global.EXTRA_BARCODESCAN_FROM);
                if (!strFrom.equals(getClass().getSimpleName())) {
                    return;
                }
                String strItemUPC = intent.getStringExtra(Global.EXTRA_ITEM_UPC);
                ProdDetlInfo prodDetlInfo = mProdUPCDetMap.get(strItemUPC);
                if(prodDetlInfo != null)
                {
                    for(UOrderDInfo info : mUOrderDList)
                    {
                        if(info.strItemNo.equals(prodDetlInfo.strItemNo))
                        {
                            int nPicked = 0;
                            if(Global.pickedStatusMap.get(info.strItemNo) != null)
                            {
                                nPicked = Global.pickedStatusMap.get(info.strItemNo);
                            }
                            nPicked++;
                            if(nPicked > info.nSales)
                            {
                                try {
                                    final MessageDialog msgDialog = new MessageDialog(mContext);
                                    msgDialog.setCancelable(false);
                                    msgDialog.mStrMessage = String.format(Locale.ENGLISH, Str.PickedOverFlowAlreadyPicked[Global.LANG], info.strShortDesc, info.nSales);
                                    msgDialog.mStrLeft = Str.No[Global.LANG];
                                    msgDialog.mStrRight = Str.Yes[Global.LANG];
                                    int finalNPicked = nPicked;
                                    msgDialog.setOnDismissListener(dialog1 -> {
                                        if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {

                                        } else if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
                                            Global.pickedStatusMap.put(info.strItemNo, finalNPicked);
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
                            else {
                                Global.pickedStatusMap.put(info.strItemNo, nPicked);
                                refreshPage();
                            }
                            return;
                        }
                    }
                }

                if(prodDetlInfo != null)
                {
                    try {
                        final MessageDialog msgDialog = new MessageDialog(mContext);
                        msgDialog.setCancelable(false);
                        msgDialog.mStrMessage = String.format(Locale.ENGLISH, Str.NotInOrder[Global.LANG], prodDetlInfo.strShortDesc);
                        msgDialog.mStrMiddle = Str.Ok[Global.LANG];
                        msgDialog.show();
                    }catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                else {
                    try {
                        final MessageDialog msgDialog = new MessageDialog(mContext);
                        msgDialog.setCancelable(false);
                        msgDialog.mStrMessage = Str.NoItemFound[Global.LANG];
                        msgDialog.mStrMiddle = Str.Ok[Global.LANG];
                        msgDialog.show();
                    }catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }

            }
        }
    }

    public void doComplete()
    {
        boolean bNeedShowWarning = false;
        for(UOrderDInfo uOrderDInfo : mUOrderDList)
        {
            int nPicked = 0;
            if(Global.pickedStatusMap.get(uOrderDInfo.strItemNo) != null)
            {
                nPicked = Global.pickedStatusMap.get(uOrderDInfo.strItemNo);
            }

            if(nPicked < uOrderDInfo.nSales)
            {
                bNeedShowWarning = true;
                break;
            }
        }
        if(bNeedShowWarning)
        {
            PickingNotCompletedDialog dialog = new PickingNotCompletedDialog(mContext, mUOrderDList, new PickingNotCompletedDialog.Callback() {
                @Override
                public void OnOkay() {
                    doProcessCompleted();
                }

                @Override
                public void OnCancel() {

                }
            });
            dialog.show();
        }
        else {
            doProcessCompleted();
        }
    }

    private void doProcessCompleted()
    {
        for(UOrderDInfo uOrderDInfo : mUOrderDList)
        {
            int nPicked = 0;
            if(Global.pickedStatusMap.get(uOrderDInfo.strItemNo) != null)
            {
                nPicked = Global.pickedStatusMap.get(uOrderDInfo.strItemNo);
            }
            uOrderDInfo.nSales = nPicked;
        }

        mContext.replaceFragment(new DeliverySummaryFragment());
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.btnBarcodeScanner:
                Intent intent = new Intent(mContext, BarcodeScannerActivity.class);
                intent.putExtra(Global.EXTRA_BARCODESCAN_FROM, getClass().getSimpleName());
                startActivity(intent);
                mContext.overridePendingTransition(R.anim.start1, R.anim.start2);
                break;
            case R.id.btnBack:
                mContext.onBackPressed();
                break;
            case R.id.btnCompleted:
                doComplete();
                break;
            case R.id.btnFilter:
                doFilter();
                break;
            case R.id.btnSort:
                doSort();
                break;
            case R.id.btnAdd:
                doAdd();
                break;
        }
    }

    private void showInvenDetail(UOrderDInfo uOrderDInfo)
    {
        ((EditText) mRootView.findViewById(R.id.editItemNo)).setText(Utils.getDisplayItemCode(mContext, uOrderDInfo.strItemNo));

        int picked = 0;
        if(Global.pickedStatusMap.get(uOrderDInfo.strItemNo) != null)
        {
            picked = Global.pickedStatusMap.get(uOrderDInfo.strItemNo);
        }
        ((EditText) mRootView.findViewById(R.id.editQty)).setText(String.valueOf(picked));
        mRootView.findViewById(R.id.editQty).requestFocus();
        ((EditText) mRootView.findViewById(R.id.editQty)).setSelection(String.valueOf(picked).length());
    }

    class ListAdapter extends BaseAdapter{

        private List<UOrderDInfo> uOrderDInfoList = new ArrayList<>();

        public void setData(List<UOrderDInfo> uOrderDInfoList)
        {
            this.uOrderDInfoList.clear();
            this.uOrderDInfoList.addAll(uOrderDInfoList);
        }
        @Override
        public int getCount() {
            return this.uOrderDInfoList.size();
        }

        @Override
        public UOrderDInfo getItem(int i) {
            return uOrderDInfoList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup parent) {
            if(view == null)
            {
                view = mContext.getLayoutInflater().inflate(R.layout.row_make_delivery_detail_entry_2, parent, false);
            }

            view.findViewById(R.id.txtPrice).setVisibility(View.GONE);
            UOrderDInfo uOrderDInfo = getItem(i);
            int nPicked = 0;
            if(Global.pickedStatusMap.get(uOrderDInfo.strItemNo) != null)
            {
                nPicked = Global.pickedStatusMap.get(uOrderDInfo.strItemNo);
            }

            ((TextView)view.findViewById(R.id.txtLoaded)).setText(String.valueOf(uOrderDInfo.nSales));
            ((TextView)view.findViewById(R.id.txtStock)).setText(String.valueOf(nPicked));
            ((TextView)view.findViewById(R.id.txtShortDesc)).setText(uOrderDInfo.strShortDesc);
            view.setOnClickListener(v -> showInvenDetail(uOrderDInfo));
            return view;
        }
    }
}