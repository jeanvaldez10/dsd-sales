package dsd.dsdvansales.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dsd.dsdvansales.R;
import dsd.dsdvansales.activities.BarcodeScannerActivity;
import dsd.dsdvansales.api.ConnectApiClient;
import dsd.dsdvansales.db.DbAdapter;
import dsd.dsdvansales.dialogs.MessageDialog;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.model.DescTypeInfo;
import dsd.dsdvansales.model.EquipAssInfo;
import dsd.dsdvansales.model.EquipmentInfo;
import dsd.dsdvansales.model.EquipmentMaster;
import dsd.dsdvansales.model.EquipmentWithAssInfo;
import dsd.dsdvansales.model.LoginInfo;
import dsd.dsdvansales.model.TransactionInfo;
import dsd.dsdvansales.model.UARPaymentInfo;
import dsd.dsdvansales.model.UARTotalInfo;
import dsd.dsdvansales.model.UAssetInfo;
import dsd.dsdvansales.model.UCollectionsDInfo;
import dsd.dsdvansales.model.UFileTransactionInfo;
import dsd.dsdvansales.model.UTransactionsInfo;
import dsd.dsdvansales.views.AlertSingleChoiceAdapter;
import retrofit2.Response;


public class EquipmentListFragment extends BaseFragment implements View.OnClickListener{

    ListView mListView;
    TextView mTitleView;
    TextView mCustomerNameView;
    TextView mNoDataView;
    EditText mEditBarcode;
    ListAdapter mListAdapter;
    List<EquipmentWithAssInfo> mEquipmentList = new ArrayList<>();
    Button mCompletedBtn;

    String mCurBarCode = "";
    private boolean isLoaded = false;

    public String[] strArray = new String[2];

    private Map<String, DescTypeInfo> mMakeDescTypeAlphaMap = new HashMap<>();
    private Map<Integer, DescTypeInfo> mMakeDescTypeNumericMap = new HashMap<>();

    private Map<String, DescTypeInfo> mEquipmentDescTypeAlphaMap = new HashMap<>();
    private Map<Integer, DescTypeInfo> mEquipmentDescTypeNumericMap = new HashMap<>();

    private int nAddType = 0;
//    private int mRepairDescCnt = 0;

    private boolean mRepairRequest = false;
    private String mEquipRMKey = "S";    //S = Serial Number, A = AltEquipment, E = Equipment Number
    public EquipmentListFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static EquipmentListFragment newInstance() {
        EquipmentListFragment fragment = new EquipmentListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mRootView = inflater.inflate(R.layout.fragment_equipment_list, container, false);

        commonInit();
        return mRootView;
    }

    private String getEquipmentKey(EquipmentWithAssInfo info)
    {
        String strData = "";
        if( mEquipRMKey.equals("S") )
        {
            strData = info.strSerialNo;
        }
        else if( mEquipRMKey.equals("A") )
        {
            strData = info.strAltEquipment;
        }
        else if( mEquipRMKey.equals("E") )
        {
            strData = info.strEquipmentNo;
        }
        return strData;
    }

    private String getEquipmentKey(EquipmentInfo info)
    {
        String strData = "";
        if( mEquipRMKey.equals("S") )
        {
            strData = info.strSerialNo;
        }
        else if( mEquipRMKey.equals("A") )
        {
            strData = info.strAltEquipment;
        }
        else if( mEquipRMKey.equals("E") )
        {
            strData = info.strEquipmentNo;
        }
        return strData;
    }

    @Override
    public void initView() {
        mEquipRMKey = Utils.getStringSetting(mContext, Global.PREF_EQUIPRMKEY);
        if(TextUtils.isEmpty(mEquipRMKey))
        {
            mEquipRMKey = "S";
        }

        String strEquipRepReq = Utils.getStringSetting(mContext, Global.PREF_EQUIPREPREQ);
        mRepairRequest = strEquipRepReq != null && (strEquipRepReq.trim().equalsIgnoreCase("Y") || strEquipRepReq.trim().equalsIgnoreCase("1"));

        mListView = mRootView.findViewById(R.id.equipmentList);
        mListAdapter = new ListAdapter();
        mListView.setAdapter(mListAdapter);
        mTitleView = mRootView.findViewById(R.id.lblTitle);
        mCustomerNameView = mRootView.findViewById(R.id.lblCustomerName);
        mEditBarcode = mRootView.findViewById(R.id.editBarCodeScan);
        mEditBarcode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
//                if(!mCurBarCode.equals(editable.toString()))
//                {
//                    mCurBarCode = editable.toString();
//                    doSearchEquipment(editable.toString());
//                }
            }
        });

        mEditBarcode.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    doSearchEquipment(v.getText().toString());
                    return true;
                }
                return false;
            }
        });

        mNoDataView = mRootView.findViewById(R.id.txtNoData);
        mCompletedBtn = mRootView.findViewById(R.id.btnCompleted);
        mRootView.findViewById(R.id.btnBack).setOnClickListener(this);
        mRootView.findViewById(R.id.btnAddEquipment).setOnClickListener(this);
        mRootView.findViewById(R.id.btnCompleted).setOnClickListener(this);
        mRootView.findViewById(R.id.btnScanBarCode).setOnClickListener(this);
        mRootView.findViewById(R.id.btnSearch).setOnClickListener(this);
        if(!isLoaded)
        {
            loadData();
        }
        else {
            refreshPage();
        }
        refreshLanguage();
    }

    private void doSearchEquipment(String strBarcode)
    {
        if(TextUtils.isEmpty(strBarcode.trim()))
        {
            return;
        }

        showProgressDialog(Str.Loading[Global.LANG]);
        new Thread()
        {
            @Override
            public void run() {
                for(EquipmentWithAssInfo info : mEquipmentList)
                {
                    if( getEquipmentKey(info).equals(strBarcode) )
                    {
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            hideProgressDialog();
                            gotoEquipmentDetail(info);
                        }, 500);
                        return;
                    }
                }

                //Need to check this equipment is exist in local db.
                DbAdapter dbAdapter = new DbAdapter(mContext);
                dbAdapter.open();
                List<EquipmentInfo> equipmentInfoList = dbAdapter.getAllEquipment();
                dbAdapter.close();

                for(EquipmentInfo info : equipmentInfoList)
                {
                    if( getEquipmentKey(info).equals(strBarcode) )
                    {
                        //Allocate this equipment to current user
                        EquipAssInfo assInfo = new EquipAssInfo();
                        assInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
                        assInfo.nChainNo = Global.gCurCustomerInfo.nChainNo;
                        assInfo.strEquipmentNo = info.strEquipmentNo;
                        dbAdapter.open();
                        dbAdapter.insertEquipAss(assInfo);
                        dbAdapter.close();

                        EquipmentWithAssInfo equipmentWithAssInfo = new EquipmentWithAssInfo();
                        equipmentWithAssInfo.nCustNo = assInfo.nCustNo;
                        equipmentWithAssInfo.nChainNo = assInfo.nChainNo;
                        equipmentWithAssInfo.strEquipmentNo = info.strEquipmentNo;
                        equipmentWithAssInfo.strSerialNo = info.strSerialNo;
                        equipmentWithAssInfo.strMake = info.strMake;
                        equipmentWithAssInfo.strModel = info.strModel;
                        equipmentWithAssInfo.strDescription = info.strDescription;
                        equipmentWithAssInfo.strAssetType = info.strAssetType;
                        equipmentWithAssInfo.strStatusCode = info.strStatusCode;
                        equipmentWithAssInfo.strEquipmentType = info.strEquipmentType;
                        equipmentWithAssInfo.strEquipmentActType = "A";
                        equipmentWithAssInfo.strApplication = info.strApplication;
                        equipmentWithAssInfo.strNewPlacementDocPath = info.strNewPlacementDocPath;
                        equipmentWithAssInfo.strAltEquipment = info.strAltEquipment;

                        mEquipmentList.add(equipmentWithAssInfo);

                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            hideProgressDialog();
                            gotoEquipmentDetail(equipmentWithAssInfo);
                        }, 500);
                        return;
                    }
                }

                //Get the equipment Info from the api.
                String strApiCompany = Utils.getStringSetting(mContext, Global.PREF_APICOMPANY);
                String strBaseUrl = Utils.getBaseURL(strApiCompany);
                Map<String, String> params = new HashMap<>();
                params.put("username", Utils.getStringSetting(mContext, Global.PREF_APIUSER));
                params.put("password", Utils.getStringSetting(mContext, Global.PREF_APIPASSWORD));

                EquipmentInfo equipmentInfo = null;
                try {
                    Response<LoginInfo> loginRes = ConnectApiClient.getMainApiClient(strBaseUrl, null).login(params).execute();
                    if(loginRes.isSuccessful() && loginRes.body() != null)
                    {
                        LoginInfo body = loginRes.body();
                        Map<String, String> headers = new HashMap<>();
                        headers.put("X-Auth-Token", body.token);

                        Response<List<EquipmentMaster>> equipmentMasterRes = ConnectApiClient.getMainApiClient(strBaseUrl, headers).getEquipmentMaster(mEquipRMKey, strBarcode).execute();
                        if(equipmentMasterRes.isSuccessful() && equipmentMasterRes.body() != null)
                        {
                            List<EquipmentMaster> equipmentMasterList = equipmentMasterRes.body();
                            if(equipmentMasterList.size() > 0)
                            {
                                EquipmentMaster equipmentMaster = equipmentMasterList.get(0);
                                equipmentInfo = new EquipmentInfo();
                                equipmentInfo.strAltEquipment = equipmentMaster.EquipmentNo;
                                equipmentInfo.strSerialNo = equipmentMaster.SerialNo;
                                equipmentInfo.strEquipmentNo = equipmentMaster.EquipmentNo;
                                equipmentInfo.strDescription = equipmentMaster.Description;
                                equipmentInfo.strMake = equipmentMaster.Make;
                                equipmentInfo.strModel = equipmentMaster.Model;
                                equipmentInfo.strEquipmentType = equipmentMaster.AssetType;
                                equipmentInfo.strEquipmentActType = "N";
                                equipmentInfo.strLastRepair = equipmentMaster.LastRepairDate;
                                equipmentInfo.strAssetType = equipmentMaster.AssetType;

                                dbAdapter.insertEquipment(equipmentInfo);
                            }
                        }
                    }
                }catch (Exception e)
                {
                    e.printStackTrace();
                }

                if(equipmentInfo != null)
                {
                    //Allocate this equipment to current user
                    EquipAssInfo assInfo = new EquipAssInfo();
                    assInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
                    assInfo.nChainNo = Global.gCurCustomerInfo.nChainNo;
                    assInfo.strEquipmentNo = equipmentInfo.strEquipmentNo;
                    dbAdapter.open();
                    dbAdapter.insertEquipAss(assInfo);
                    dbAdapter.close();

                    EquipmentWithAssInfo equipmentWithAssInfo = new EquipmentWithAssInfo();
                    equipmentWithAssInfo.nCustNo = assInfo.nCustNo;
                    equipmentWithAssInfo.nChainNo = assInfo.nChainNo;
                    equipmentWithAssInfo.strEquipmentNo = equipmentInfo.strEquipmentNo;
                    equipmentWithAssInfo.strSerialNo = equipmentInfo.strSerialNo;
                    equipmentWithAssInfo.strMake = equipmentInfo.strMake;
                    equipmentWithAssInfo.strModel = equipmentInfo.strModel;
                    equipmentWithAssInfo.strDescription = equipmentInfo.strDescription;
                    equipmentWithAssInfo.strAssetType = equipmentInfo.strAssetType;
                    equipmentWithAssInfo.strStatusCode = equipmentInfo.strStatusCode;
                    equipmentWithAssInfo.strEquipmentType = equipmentInfo.strEquipmentType;
                    equipmentWithAssInfo.strEquipmentActType = "N";
                    equipmentWithAssInfo.strApplication = equipmentInfo.strApplication;
                    equipmentWithAssInfo.strNewPlacementDocPath = equipmentInfo.strNewPlacementDocPath;
                    equipmentWithAssInfo.strAltEquipment = equipmentInfo.strAltEquipment;

                    mEquipmentList.add(equipmentWithAssInfo);

                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        hideProgressDialog();
                        gotoEquipmentDetail(equipmentWithAssInfo);
                    }, 500);
                    return;
                }
                else {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        hideProgressDialog();
                        doEquipmentNotExist();
                    });
                }
            }
        }.start();
    }

    private void gotoEquipmentDetail(EquipmentWithAssInfo info)
    {
        EquipmentDetailsFragment fragment = EquipmentDetailsFragment.newInstance(info);
        mContext.replaceFragment(fragment);
    }
    private void doEquipmentNotExist()
    {
        try {
            final MessageDialog msgDialog = new MessageDialog(mContext);
            msgDialog.setCancelable(false);
            msgDialog.mStrMessage = Str.EquipmentNoFound[Global.LANG];
            msgDialog.mStrLeft = Str.No[Global.LANG];
            msgDialog.mStrRight = Str.Yes[Global.LANG];
            msgDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
                        gotoAddEquipment();
                    } else if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {

                    }
                }
            });
            msgDialog.show();
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void doAddExistingEquipment()
    {
        Global.gCurEquipmentWithAssInfoList = mEquipmentList;
        AddEquipmentFragment fragment = AddEquipmentFragment.newInstance(0);
        mContext.replaceFragment(fragment);
    }

    private void doAddNewEquipment()
    {
        Global.gCurEquipmentWithAssInfoList = mEquipmentList;
        AddEquipmentFragment fragment = AddEquipmentFragment.newInstance(1);
        mContext.replaceFragment(fragment);
    }

    private void gotoAddEquipment()
    {
        try {
            new AlertDialog.Builder(mContext)
                    .setTitle(Str.Select_Reason_code[Global.LANG])
                    .setSingleChoiceItems(new AlertSingleChoiceAdapter(mContext, strArray), 0, (dialog, nSelected) -> nAddType = nSelected)
                    .setPositiveButton(Str.Ok[Global.LANG], (dialog, nSelected) -> {
                        if(nAddType == 0)
                        {
                            doAddExistingEquipment();
                        }
                        else
                        {
                            doAddNewEquipment();
                        }

                    })
                    .setNegativeButton(Str.Cancel[Global.LANG], (dialog, nSelected) -> {

                    })
                    .create()
                    .show();
        }catch (Exception e)
        {
            e.printStackTrace();
        }

        nAddType = 0;
    }
    @Override
    public void loadData() {

        showProgressDialog(Str.Load[Global.LANG]);
        new Thread(){
            @Override
            public void run() {

                //Load Equipment Data with CustNo & ChainNo = CurrentCustomer Cust & ChainNo
                DbAdapter dbAdapter = new DbAdapter(mContext);
                dbAdapter.open();
                mEquipmentList = dbAdapter.getEquipmentWithCustomer(Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo);
                List<DescTypeInfo> makeDescTypeList = dbAdapter.getDescTypeList(Global.EQUIP_MAKE_ID);
                List<DescTypeInfo> equipmentDescTypeList = dbAdapter.getDescTypeList(Global.EQUIP_DESCRIPTION_ID);
                dbAdapter.close();

                for(DescTypeInfo info : makeDescTypeList)
                {
                    if(!TextUtils.isEmpty(info.strAlphaKey))
                    {
                        mMakeDescTypeAlphaMap.put(info.strAlphaKey, info);
                    }
                    if(info.nNumericKey != 0)
                    {
                        mMakeDescTypeNumericMap.put(info.nNumericKey, info);
                    }
                }

                for(DescTypeInfo info : equipmentDescTypeList)
                {
                    if(!TextUtils.isEmpty(info.strAlphaKey))
                    {
                        mEquipmentDescTypeAlphaMap.put(info.strAlphaKey, info);
                    }
                    if(info.nNumericKey != 0)
                    {
                        mEquipmentDescTypeNumericMap.put(info.nNumericKey, info);
                    }
                }

                isLoaded = true;
                new Handler(Looper.getMainLooper()).post(() -> {
                    hideProgressDialog();
                    refreshLanguage();
                });
            }
        }.start();

    }

    @Override
    public void refreshPage() {
        if( mEquipmentList.size() == 0 )
        {
            mListView.setVisibility(View.GONE);
            mNoDataView.setVisibility(View.VISIBLE);
        }
        else
        {
            mListView.setVisibility(View.VISIBLE);
            mNoDataView.setVisibility(View.GONE);
            mListAdapter.notifyDataSetChanged();
        }
        mCustomerNameView.setText(Global.gCurCustomerInfo.strName);

    }

    @Override
    public void refreshLanguage() {
        mTitleView.setText(Str.EquipmentList[Global.LANG]);
        mNoDataView.setText(Str.No_data_has_been_found[Global.LANG]);
        mCompletedBtn.setText(Str.Completed[Global.LANG]);
        strArray[0] = Str.Add_Existing[Global.LANG] + "?";
        strArray[1] = Str.New_Placement[Global.LANG] + "?";
    }

    @Override
    public void receiveBroadcast(String strAction, Intent intent) {
        if(Global.gFragIdx != Global.FRAG_EQUIPMENT_LIST) return;

        if (strAction.equals(Global.ACTION_TAKE_PHOTO) == true) {
            String strPhotoPath = intent.getStringExtra(Global.EXTRA_PHOTO_PATH);
            if(!TextUtils.isEmpty(strPhotoPath))
            {
                String strNewPhotoPath = Global.UPLOAD_DIR + System.currentTimeMillis() + ".jpg";
                Utils.fileCopy(strPhotoPath, strNewPhotoPath);
                Utils.fileDelete(strPhotoPath);
                if(Global.gCurEquipmentWithAssInfo != null)
                {
                    Global.gCurEquipmentWithAssInfo.photoList.add(strNewPhotoPath);
                }
            }
            Global.gCurEquipmentWithAssInfo = null;
        }
        else if (strAction.equals(Global.ACTION_SELECT_PRODUCT) == true){
            int nType = intent.getIntExtra(Global.EXTRA_TYPE, 0);
            if (nType == Global.SELECT_PRODUCT_ITEMUPC) {
                String strFrom = intent.getStringExtra(Global.EXTRA_BARCODESCAN_FROM);
                if (!strFrom.equals(getClass().getSimpleName())) {
                    return;
                }
                String strItemUPC = intent.getStringExtra(Global.EXTRA_ITEM_UPC);
                mEditBarcode.setText(strItemUPC);
                doSearchEquipment(strItemUPC);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        Global.gFragIdx = Global.FRAG_EQUIPMENT_LIST;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id)
        {
            case R.id.btnBack:
                goBack();
                break;
            case R.id.btnAddEquipment:
                gotoAddEquipment();
                break;
            case R.id.btnSearch:
                String strBarcode = ((EditText)mRootView.findViewById(R.id.editBarCodeScan)).getText().toString();
                doSearchEquipment(strBarcode);
                break;
            case R.id.btnCompleted:
                doCompleted();
                break;
            case R.id.btnScanBarCode:
                doScanBarCode();
                break;
        }
    }

    private void doScanBarCode()
    {
        Intent intent = new Intent(mContext, BarcodeScannerActivity.class);
        intent.putExtra(Global.EXTRA_BARCODESCAN_FROM, getClass().getSimpleName());
        startActivity(intent);
        mContext.overridePendingTransition(R.anim.start1, R.anim.start2);
    }

    private void goBack()
    {
        mContext.onBackPressed();
    }



    private void doCompleted()
    {
        //Check Current Verify status Before Completed
        for(EquipmentWithAssInfo info : mEquipmentList)
        {
            if( info.strVerifyFlag.equals("Y") && TextUtils.isEmpty(info.strVerified) )
            {
                try {
                    final MessageDialog msgDialog = new MessageDialog(mContext);
                    msgDialog.setCancelable(false);
                    msgDialog.mStrMessage = Str.You_must_verify_the_in_store_equipment[Global.LANG];
                    msgDialog.mStrRight = Str.Ok[Global.LANG];
                    msgDialog.show();
                }catch (Exception e)
                {
                    e.printStackTrace();
                }

                return;
            }
        }

        long nTrxnNo = setGeneralUploadData("ASVT", Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo, -1);

        TransactionInfo transactionInfo = new TransactionInfo();
        transactionInfo.strType = "ASVT";
        transactionInfo.strTime = new SimpleDateFormat("kk:mm").format(new Date(nTrxnNo));
        transactionInfo.strCustomerName = Global.gCurCustomerInfo.strName;
        DbAdapter dbAdapter = new DbAdapter(mContext);
        dbAdapter.open();
        dbAdapter.insertTransactionHistoryInfo(transactionInfo);
        dbAdapter.close();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String strRequestDate = sdf.format(Utils.getRoutectlTripDate(mContext, new Date(nTrxnNo)));
        dbAdapter = new DbAdapter(mContext);
        dbAdapter.open();

        long nArTrxnNo = System.currentTimeMillis();
        while(nArTrxnNo == nTrxnNo)
        {
            nArTrxnNo = System.currentTimeMillis();
        }
        UARTotalInfo uARTotalInfo = new UARTotalInfo();
        uARTotalInfo.nTrxnNo = nArTrxnNo;
        uARTotalInfo.nChainNo = Global.gCurCustomerInfo.nChainNo;
        uARTotalInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
        uARTotalInfo.strTrxnDate = strRequestDate;
        uARTotalInfo.strDocType = "DEP";
        uARTotalInfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(new Date(nTrxnNo));
        boolean bCollectionExist = false;
        for(EquipmentWithAssInfo info : mEquipmentList)
        {
            if( !TextUtils.isEmpty(info.strEquipmentActType) )
            {
                //Make UEquipAsset information
                UAssetInfo uAssetInfo = new UAssetInfo();
                uAssetInfo.nTrxnNo = nTrxnNo;
                uAssetInfo.nChainNo = info.nChainNo;
                uAssetInfo.nCustNo = info.nCustNo;
                uAssetInfo.strSerialNo = info.strSerialNo;
                uAssetInfo.strEquipmentType = info.strEquipmentType;
                uAssetInfo.strEquipmentActType = info.strEquipmentActType;
                uAssetInfo.strMake = info.strMake;
                uAssetInfo.strModel = info.strModel;
                uAssetInfo.strApplication = info.strApplication;
                uAssetInfo.strStatusCode = info.strStatusCode;
                uAssetInfo.strAssetType = info.strAssetType;
                uAssetInfo.strRequestDate = strRequestDate;
                uAssetInfo.strRepairReason = info.strRepairReason;
                uAssetInfo.strRepairNotes = info.strRepairNotes;
                uAssetInfo.strResponse = info.strResponse;
                uAssetInfo.strAltEquipment = info.strAltEquipment;
                uAssetInfo.strEquipmentNo = info.strEquipmentNo;

                if( info.strEquipmentActType.equals("R") && !TextUtils.isEmpty(info.strRepairImage) )
                {
                    UFileTransactionInfo uFileTransactionInfo = new UFileTransactionInfo();
                    String strRef = setUCameraData("CAM", info.strRepairImage, "", uFileTransactionInfo, 1, true);
                    {
                        setUImageFileTransactionInfo(Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo,  uFileTransactionInfo.nFileTrxnNo, "",
                                "EQUIPREPAIR", "EQUIPREPAIR",
                                uFileTransactionInfo.strFileCreateDate, uFileTransactionInfo.strFileCreateTime, strRef);
                    }
                }
                if( info.strEquipmentActType.equals("N") && !TextUtils.isEmpty(info.strNewPlacementDocPath))
                {
                    String []strPaths = info.strNewPlacementDocPath.split("/");
                    String strPdfFileName = strPaths[strPaths.length - 1];
                    String strDocNo = strPdfFileName.split("\\.")[0];
                    uAssetInfo.strDocNo = strDocNo;
                    uARTotalInfo.strDocNo = strDocNo;
                    uARTotalInfo.nInvoiceTrxnNo = nTrxnNo;
                    UTransactionsInfo uTransactionsInfo = setUTransactionsInfo(Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo, nTrxnNo, "FARC", strPdfFileName);
                    setUFileTransactionInfo(uTransactionsInfo.nTrxnNo, Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo,
                            nTrxnNo, strDocNo, "EQUIPNEWPLACEMENT", "EQUIPMENT NEW PLACEMENT", uTransactionsInfo.strTrxnDate, uTransactionsInfo.strTrxnTime, strPdfFileName);
                }

                if(TextUtils.isEmpty(info.strEquipmentDoc))
                {
                    String []strPaths = info.strEquipmentDoc.split("/");
                    String strPdfFileName = strPaths[strPaths.length - 1];
                    String strDocNo = strPdfFileName.split("\\.")[0];
                    uAssetInfo.strDocNo = strDocNo;
                    uARTotalInfo.strDocNo = strDocNo;
                    uARTotalInfo.nInvoiceTrxnNo = nTrxnNo;
                    UTransactionsInfo uTransactionsInfo = setUTransactionsInfo(Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo, nTrxnNo, "FARC", strPdfFileName);
                    setUFileTransactionInfo(uTransactionsInfo.nTrxnNo, Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo,
                            nTrxnNo, strDocNo, "EQUIPDOC", "EQUIPMENT DOCUMENT", uTransactionsInfo.strTrxnDate, uTransactionsInfo.strTrxnTime, strPdfFileName);
                }
                if(info.photoList.size() > 0)
                {
                    for(String strPhotoPath : info.photoList)
                    {
                        UFileTransactionInfo uFileTransactionInfo = new UFileTransactionInfo();
                        String strRef = setUCameraData("CAM", strPhotoPath, "", uFileTransactionInfo, 1, true);
                        {
                            setUImageFileTransactionInfo(Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo,  uFileTransactionInfo.nFileTrxnNo, "",
                                    "EQUIP", "EQUIPMENT", uFileTransactionInfo.strFileCreateDate, uFileTransactionInfo.strFileCreateTime, strRef);
                        }
                    }
                }
                dbAdapter.insertUAssetsInfo(uAssetInfo);
                if(info.strEquipmentActType.equals("N"))
                {
                    bCollectionExist = true;
                    UCollectionsDInfo uCollectionsDInfo = dbAdapter.getUCollectionDInfo(info.nCustNo, info.nChainNo, info.strEquipmentNo);
                    if( uCollectionsDInfo != null )
                    {
                        uCollectionsDInfo.nTrxnNo = nArTrxnNo;
                        uCollectionsDInfo.strTrxnDate = strRequestDate;
                        dbAdapter.updateUCollectionsDInfo(uCollectionsDInfo);

                        //Save AR to Database
                        if(uCollectionsDInfo.nPayType == Global.COLLECTION_CARD)
                        {
                            UARPaymentInfo paymentInfo = new UARPaymentInfo();
                            paymentInfo.nTrxnNo = nArTrxnNo;
                            paymentInfo.nChainNo = Global.gCurCustomerInfo.nChainNo;
                            paymentInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
                            paymentInfo.strTrxnDate = strRequestDate;
                            paymentInfo.nPayType = Global.COLLECTION_CARD;
                            paymentInfo.nTrxnAmount = uCollectionsDInfo.nTrxnAmount;
                            dbAdapter.insertUARPaymentInfo(paymentInfo);
                        }
                        else if( uCollectionsDInfo.nPayType == Global.COLLECTION_CASH)
                        {
                            uARTotalInfo.nTotalCash += uCollectionsDInfo.nTrxnAmount;
                            UARPaymentInfo paymentInfo = new UARPaymentInfo();
                            paymentInfo.nTrxnNo = nArTrxnNo;
                            paymentInfo.nChainNo = Global.gCurCustomerInfo.nChainNo;
                            paymentInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
                            paymentInfo.strTrxnDate = strRequestDate;
                            paymentInfo.nTrxnAmount = uCollectionsDInfo.nTrxnAmount;
                            paymentInfo.nPayType = Global.COLLECTION_CASH;
                            dbAdapter.insertUARPaymentInfo(paymentInfo);
                        }
                        else if( uCollectionsDInfo.nPayType == Global.COLLECTION_CHEQUE )
                        {
                            uARTotalInfo.nTotalCheque += uCollectionsDInfo.nTrxnAmount;
                            UARPaymentInfo paymentInfo = new UARPaymentInfo();
                            paymentInfo.nTrxnNo = nArTrxnNo;
                            paymentInfo.nChainNo = Global.gCurCustomerInfo.nChainNo;
                            paymentInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
                            paymentInfo.strTrxnDate = strRequestDate;
                            paymentInfo.nPayType = Global.COLLECTION_CHEQUE;
                            dbAdapter.insertUARPaymentInfo(paymentInfo);
                        }
                    }
                }
            }
        }
        if( bCollectionExist )
        {
            dbAdapter.insertUARTotalInfo(uARTotalInfo);
        }

        AddTrxnNo(uARTotalInfo.nCustNo, uARTotalInfo.nChainNo, uARTotalInfo.nTrxnNo);
        dbAdapter.insertOrUpdateEquipCompleteStatus(Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo, true);
        dbAdapter.close();
        setGeneralUploadData("DEP", Global.gCurCustomerInfo.nChainNo, Global.gCurCustomerInfo.nCustNo, nTrxnNo);

        transactionInfo = new TransactionInfo();
        transactionInfo.strType = "DEP";
        transactionInfo.strTime = new SimpleDateFormat("kk:mm").format(new Date(nTrxnNo));
        transactionInfo.strCustomerName = Global.gCurCustomerInfo.strName;
        dbAdapter = new DbAdapter(mContext);
        dbAdapter.open();
        dbAdapter.insertTransactionHistoryInfo(transactionInfo);
        dbAdapter.close();

        //Global.gUploadCustNoList.add(makeZipFileInfo);
        //Utils.registerUploadFile(mContext, makeZipFileInfo);
        Utils.registerUploadFileAsTemp(mContext, makeZipFileInfo);
        goBack();
    }

    private void doRepair(EquipmentWithAssInfo info)
    {
        EquipmentRepairFragment fragment = EquipmentRepairFragment.newInstance(info);
        mContext.replaceFragment(fragment);
    }

    private void doTakePhoto(EquipmentWithAssInfo info)
    {
        Global.gCurEquipmentWithAssInfo = info;
        selectPhoto();
    }

    public String ConvertFormat(String strDate)
    {
        if(TextUtils.isEmpty(strDate)) return "";
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
            SimpleDateFormat newFormat = new SimpleDateFormat(Utils.getDateFormat(mContext, "dd/MM/yy"));
            return newFormat.format(format.parse(strDate));
        }catch(Exception e)
        {
            e.printStackTrace();
        }
        return "";
    }

    private class ViewHolder
    {
        public ImageView mImageView;
        public TextView mEquipmentKeyTxt;
        public TextView mMakeDescTxt;
        public TextView mEquipDescTxt;
        public TextView mInstalledDateTxt;

        public ImageView mRepairBtn;
        public ImageView mCameraBtn;
        public ImageView mCheckImg;
    }


    private class ListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mEquipmentList.size();
        }

        @Override
        public Object getItem(int i) {
            if(i < mEquipmentList.size())
            {
                return mEquipmentList.get(i);
            }
            else
            {
                return null;
            }
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder holder;
            if( view == null )
            {
                view = mContext.getLayoutInflater().inflate(R.layout.row_equipment, viewGroup, false);
                holder = new ViewHolder();
                holder.mImageView = view.findViewById(R.id.imgItem);
                holder.mEquipmentKeyTxt = view.findViewById(R.id.txtEquipmentKey);
                holder.mEquipDescTxt = view.findViewById(R.id.txtEquipDesc);
                holder.mMakeDescTxt = view.findViewById(R.id.txtMakeDesc);
                holder.mInstalledDateTxt = view.findViewById(R.id.txtInstalledDate);
                holder.mRepairBtn = view.findViewById(R.id.imgRepair);
                holder.mCameraBtn = view.findViewById(R.id.imgCamera);
                holder.mCheckImg = view.findViewById(R.id.imgCheck);

                view.setTag(holder);
            }
            else
            {
                holder = (ViewHolder)view.getTag();
            }

            final EquipmentWithAssInfo info = mEquipmentList.get(i);
            File file = new File(Global.ROOT_EQUIPMENT_CATALOG_LOCATION + "/" + info.strModel + ".jpg");
            Picasso.get().load(file).placeholder(mContext.getResources().getDrawable(R.drawable.no_image)).into(holder.mImageView);

            holder.mEquipmentKeyTxt.setText(getEquipmentKey(info));
            String strMake = mMakeDescTypeAlphaMap.get(info.strMake) != null ? mMakeDescTypeAlphaMap.get(info.strMake).strDescription : (mMakeDescTypeNumericMap.get(Utils.parseIntFromString(info.strMake)) != null ? mMakeDescTypeNumericMap.get(Utils.parseIntFromString(info.strMake)).strDescription : info.strMake);
            holder.mMakeDescTxt.setText(strMake);

            String strDescription = mEquipmentDescTypeAlphaMap.get(info.strDescription) != null ? mEquipmentDescTypeAlphaMap.get(info.strDescription).strDescription : (mEquipmentDescTypeNumericMap.get(Utils.parseIntFromString(info.strDescription)) != null ? mEquipmentDescTypeNumericMap.get(Utils.parseIntFromString(info.strDescription)).strDescription : info.strDescription);
            holder.mEquipDescTxt.setText(strDescription);
            holder.mInstalledDateTxt.setText(ConvertFormat(info.strInstalled));

            if( mRepairRequest )
            {
                holder.mRepairBtn.setVisibility(View.VISIBLE);
                holder.mRepairBtn.setOnClickListener(view1 -> {
                    doRepair(info);
                });
            }
            else
            {
                holder.mRepairBtn.setVisibility(View.GONE);
            }

            if (info.strVerified != null && !info.strVerified.isEmpty()) {
                holder.mCheckImg.setVisibility(View.VISIBLE);
            }
            else {
                holder.mCheckImg.setVisibility(View.GONE);
            }

            holder.mCameraBtn.setOnClickListener(view1 -> {
                doTakePhoto(info);
            });

            view.setOnClickListener(view12 -> gotoEquipmentDetail(info));
            return view;
        }
    }
}
