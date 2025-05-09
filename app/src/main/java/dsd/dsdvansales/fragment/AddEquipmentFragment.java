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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import dsd.dsdvansales.R;
import dsd.dsdvansales.activities.BarcodeScannerActivity;
import dsd.dsdvansales.api.ConnectApiClient;
import dsd.dsdvansales.db.DbAdapter;
import dsd.dsdvansales.dialogs.EquipmentNoteDialog;
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
import dsd.dsdvansales.views.AlertSingleChoiceAdapter;
import retrofit2.Response;

public class AddEquipmentFragment extends BaseFragment implements View.OnClickListener {

    private int mAddType = 0;  //0 --- Add New Existing,, 1 --- New Placement
    EquipmentWithAssInfo mEquipmentInfo;
    private TextView mTitleView, mCustomerNameView, mSerialNumberView, mDescriptionView, mMakeView, mModelView, mEquipmentTypeView; //Label Views
    private TextView mTxtDescription, mTxtEquipmentType, mTxtModel, mTxtMake; //Drop down text view
    Button mBtnConfirm;

    List<DescTypeInfo> mDescriptionList = new ArrayList<>();
    List<DescTypeInfo> mMakeList = new ArrayList<>();
    List<DescTypeInfo> mModelList = new ArrayList<>();
    List<DescTypeInfo> mTypeList = new ArrayList<>();

    String[] mDescriptions = null;
    String[] mMakes = null;
    String[] mModels = null;
    String[] mTypes = null;

    int mDescriptionIdx = -1;
    int mMakeIdx = -1;
    int mModelIdx = -1;
    int mTypeIdx = -1;

    boolean isLoaded = false;
    public AddEquipmentFragment() {
        // Required empty public constructor
    }

    private void setType(int nType)
    {
        mAddType = nType;
    }
    public static AddEquipmentFragment newInstance(int nType) {
        AddEquipmentFragment fragment = new AddEquipmentFragment();
        fragment.setType(nType);
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
        mRootView = inflater.inflate(R.layout.fragment_add_equipment, container, false);
        commonInit();
        return mRootView;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id)
        {
            case R.id.btnBack:
                mContext.onBackPressed();
                break;
            case R.id.btnConfirm:
                doConfirm();
                break;
            case R.id.btnSearch:
                doSearch();
                break;
            case R.id.btnNotes:
                doNote();
                break;
            case R.id.btnCamera:
                doCamera();
                break;
            case R.id.btnScanBarCode:
                doScanBarcode();
                break;
            case R.id.txtDescription:
                selectDescription();
                break;
            case R.id.txtMake:
                selectMake();
                break;
            case R.id.txtModel:
                selectModel();
                break;
            case R.id.txtEquipmentType:
                selectEquipmentType();
                break;
        }
    }

    private void doNote()
    {
        EquipmentNoteDialog dialog = new EquipmentNoteDialog(mContext, mEquipmentInfo);
        dialog.setCancelable(false);
        dialog.show();
    }

    private void doCamera()
    {
        selectPhoto();
    }
    private void doScanBarcode()
    {
        Intent intent = new Intent(mContext, BarcodeScannerActivity.class);
        intent.putExtra(Global.EXTRA_BARCODESCAN_FROM, getClass().getSimpleName());
        startActivity(intent);
        mContext.overridePendingTransition(R.anim.start1, R.anim.start2);
    }

    @Override
    public void onResume() {
        super.onResume();
        Global.gFragIdx = Global.FRAG_ADD_EQUIPMENT_LIST;
    }

    @Override
    public void initView() {
        mTitleView = (TextView)mRootView.findViewById(R.id.lblTitle);
        mCustomerNameView = (TextView)mRootView.findViewById(R.id.lblCustomerName);
        mSerialNumberView = (TextView)mRootView.findViewById(R.id.lblSerialNo);
        mDescriptionView = (TextView)mRootView.findViewById(R.id.lblDescription);
        mMakeView = (TextView)mRootView.findViewById(R.id.lblMake);
        mModelView = (TextView)mRootView.findViewById(R.id.lblModel);
        mEquipmentTypeView = (TextView)mRootView.findViewById(R.id.lblEquipmentType);

        mTxtMake = (TextView) mRootView.findViewById(R.id.txtMake);
        mTxtModel = (TextView)mRootView.findViewById(R.id.txtModel);
        mTxtDescription = (TextView)mRootView.findViewById(R.id.txtDescription);
        mTxtEquipmentType = (TextView)mRootView.findViewById(R.id.txtEquipmentType);

        mBtnConfirm = (Button)mRootView.findViewById(R.id.btnConfirm);
        mBtnConfirm.setOnClickListener(this);

        mRootView.findViewById(R.id.btnBack).setOnClickListener(this);
        mRootView.findViewById(R.id.btnScanBarCode).setOnClickListener(this);
        mRootView.findViewById(R.id.btnSearch).setOnClickListener(this);

        mTxtEquipmentType.setOnClickListener(this);
        mTxtMake.setOnClickListener(this);
        mTxtModel.setOnClickListener(this);
        mTxtDescription.setOnClickListener(this);

        String strTemplateFileName = Utils.getFinalPrintTemplateFileName(mContext, Global.PRINT_A4_NEW_EQUIPMENT_DOC_FMT_FILENAME);
        if(Utils.isFileExist(Global.ROOT_PRINT_TEMPLATE_LOCCATION + strTemplateFileName))
        {
            mRootView.findViewById(R.id.btnNotes).setVisibility(View.VISIBLE);
        }
        else {
            mRootView.findViewById(R.id.btnNotes).setVisibility(View.INVISIBLE);
        }

        mRootView.findViewById(R.id.btnNotes).setOnClickListener(this);
        mRootView.findViewById(R.id.btnCamera).setOnClickListener(this);

        ((EditText) mRootView.findViewById(R.id.editBarCodeScan)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String strEquipNo = editable.toString();
//                if(mAddType == 1)
//                {
                    mEquipmentInfo.strEquipmentNo = strEquipNo;

                    String strEquipRMKey = Utils.getStringSetting(mContext, Global.PREF_EQUIPRMKEY);
                    if(TextUtils.isEmpty(strEquipRMKey))
                    {
                        strEquipRMKey = "S";
                    }

                    if(strEquipRMKey.equals("S"))
                    {
                        mEquipmentInfo.strSerialNo = mEquipmentInfo.strEquipmentNo;
                    }
                    else if(strEquipRMKey.equals("A"))
                    {
                        mEquipmentInfo.strAltEquipment = mEquipmentInfo.strEquipmentNo;
                    }
//                }
                /*else
                {
                    //Need to find the equipment info from the database
                    doSearchEquipment(strEquipNo);
                }*/
            }
        });

        ((EditText) mRootView.findViewById(R.id.editBarCodeScan)).setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                if(mAddType != 1)
                {
                    doSearchEquipment(v.getText().toString());
                }
                return true;
            }
            return false;
        });

        if(!isLoaded)
        {
            loadData();
        }
        else {
            refreshPage();
        }

        refreshLanguage();
    }

    private void doSearch()
    {
        String strBarcode = ((EditText) mRootView.findViewById(R.id.editBarCodeScan)).getText().toString();
        doSearchEquipment(strBarcode);
    }

    private void doSearchEquipment(String strEquipNo)
    {
        if(TextUtils.isEmpty(strEquipNo.trim()))
        {
            return;
        }

        showProgressDialog(Str.Loading[Global.LANG]);
        new Thread()
        {
            @Override
            public void run() {
                String strEquipRMKey = Utils.getStringSetting(mContext, Global.PREF_EQUIPRMKEY);
                if(TextUtils.isEmpty(strEquipRMKey))
                {
                    strEquipRMKey = "S";
                }

                DbAdapter dbAdapter = new DbAdapter(mContext);
                dbAdapter.open();
                EquipmentInfo equipmentInfo = null;
                if(strEquipRMKey.equals("S"))
                {
                    equipmentInfo = dbAdapter.getEquipmentWithSerialNo(strEquipNo);
                }
                else if(strEquipRMKey.equals("A"))
                {
                    equipmentInfo = dbAdapter.getEquipmentWithAltEquipment(strEquipNo);
                }
                else {
                    equipmentInfo = dbAdapter.getEquipmentWithEquipNo(strEquipNo);
                }


                if(equipmentInfo == null)
                {
                    //Get the equipment Info from the api.
                    String strApiCompany = Utils.getStringSetting(mContext, Global.PREF_APICOMPANY);
                    String strBaseUrl = Utils.getBaseURL(strApiCompany);
                    Map<String, String> params = new HashMap<>();
                    params.put("username", Utils.getStringSetting(mContext, Global.PREF_APIUSER));
                    params.put("password", Utils.getStringSetting(mContext, Global.PREF_APIPASSWORD));

                    try {
                        Response<LoginInfo> loginRes = ConnectApiClient.getMainApiClient(strBaseUrl, null).login(params).execute();
                        if(loginRes.isSuccessful() && loginRes.body() != null)
                        {
                            LoginInfo body = loginRes.body();
                            Map<String, String> headers = new HashMap<>();
                            headers.put("X-Auth-Token", body.token);

                            Response<List<EquipmentMaster>> equipmentMasterRes = ConnectApiClient.getMainApiClient(strBaseUrl, headers).getEquipmentMaster(strEquipRMKey, strEquipNo).execute();
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
                }

                dbAdapter.close();
                EquipmentInfo finalEquipmentInfo = equipmentInfo;
                new Handler(Looper.getMainLooper()).post(() -> {
                    hideProgressDialog();
                    if(finalEquipmentInfo == null)
                    {
                        try {
                            final MessageDialog msgDialog = new MessageDialog(mContext);
                            msgDialog.setCancelable(false);
                            msgDialog.mStrMessage = Str.EquipmentNoFound[Global.LANG];
                            msgDialog.mStrMiddle = Str.Ok[Global.LANG];
                            msgDialog.show();
                        }catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                    else {
                        mEquipmentInfo.strAltEquipment = finalEquipmentInfo.strAltEquipment;
                        mEquipmentInfo.strSerialNo = finalEquipmentInfo.strSerialNo;
                        mEquipmentInfo.strEquipmentNo = finalEquipmentInfo.strEquipmentNo;
                        mEquipmentInfo.strDescription = finalEquipmentInfo.strDescription;
                        mEquipmentInfo.strMake = finalEquipmentInfo.strMake;
                        mEquipmentInfo.strModel = finalEquipmentInfo.strModel;
                        mEquipmentInfo.strEquipmentActType = "N";
                        mEquipmentInfo.strEquipmentType = finalEquipmentInfo.strEquipmentType;
                        mEquipmentInfo.strAssetType = finalEquipmentInfo.strAssetType;
                        refreshPage();
                    }
                });

            }
        }.start();
    }

    @Override
    public void loadData() {
        mEquipmentInfo = new EquipmentWithAssInfo();
        mEquipmentInfo.nChainNo = Global.gCurCustomerInfo.nChainNo;
        mEquipmentInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
        if(mAddType == 1)
        {
            mEquipmentInfo.strEquipmentNo = makeEquipNumber();
            String strEquipRMKey = Utils.getStringSetting(mContext, Global.PREF_EQUIPRMKEY);
            if(TextUtils.isEmpty(strEquipRMKey))
            {
                strEquipRMKey = "S";
            }

            if(strEquipRMKey.equals("S"))
            {
                mEquipmentInfo.strSerialNo = mEquipmentInfo.strEquipmentNo;
            }
            else if(strEquipRMKey.equals("A"))
            {
                mEquipmentInfo.strAltEquipment = mEquipmentInfo.strEquipmentNo;
            }
            ((EditText) mRootView.findViewById(R.id.editBarCodeScan)).setText(mEquipmentInfo.strEquipmentNo);
            mEquipmentInfo.strResponse = "P";
        }
        else {
            mEquipmentInfo.strResponse = "X";
        }

        DbAdapter dbAdapter = new DbAdapter(mContext);
        dbAdapter.open();
        mDescriptionList = dbAdapter.getDescTypeList(Global.EQUIP_DESCRIPTION_ID);
        mMakeList = dbAdapter.getDescTypeList(Global.EQUIP_MAKE_ID);
        mModelList = dbAdapter.getDescTypeList(Global.EQUIP_MODEL_ID);
        mTypeList = dbAdapter.getDescTypeList(Global.EQUIP_EQUIP_TYPE_ID);
        dbAdapter.close();

        if(mDescriptionList.size() > 0)
        {
            mDescriptions = new String[mDescriptionList.size()];
            for(int i = 0; i < mDescriptionList.size(); i++)
            {
                mDescriptions[i] = mDescriptionList.get(i).strDescription;
            }
        }
        else
        {
            mDescriptions = null;
        }

        if(mMakeList.size() > 0)
        {
            mMakes = new String[mMakeList.size()];
            for(int i = 0; i < mMakeList.size(); i++)
            {
                mMakes[i] = mMakeList.get(i).strDescription;
            }
        }
        else
        {
            mMakes = null;
        }

        if(mModelList.size() > 0)
        {
            mModels = new String[mModelList.size()];
            for(int i = 0; i < mModelList.size(); i++)
            {
                mModels[i] = mModelList.get(i).strDescription;
            }
        }
        else
        {
            mModels = null;
        }

        if(mTypeList.size() > 0)
        {
            mTypes = new String[mTypeList.size()];
            for(int i = 0; i < mTypeList.size(); i++)
            {
                mTypes[i] = mTypeList.get(i).strDescription;
            }
        }
        else
        {
            mTypes = null;
        }

        isLoaded = true;
        refreshPage();
    }

    @Override
    public void refreshPage() {
        mCustomerNameView.setText(Global.gCurCustomerInfo.strName);

        if(mDescriptions != null)
        {
            mDescriptionIdx = -1;
            for(int i = 0 ; i < mDescriptionList.size(); i++)
            {
                if( mDescriptionList.get(i).strAlphaKey.equals(mEquipmentInfo.strDescription) ||
                        (mDescriptionList.get(i).nNumericKey != 0 && mDescriptionList.get(i).nNumericKey == Utils.parseIntFromString(mEquipmentInfo.strDescription)))
                {
                    mDescriptionIdx = i;
                    break;
                }
            }
        }

        if(mDescriptionIdx != -1)
        {
            mTxtDescription.setText(mDescriptionList.get(mDescriptionIdx).strDescription);
        }


        if(mMakes != null)
        {
            mMakeIdx = -1;
            for(int i = 0 ; i < mMakeList.size(); i++)
            {
                if( mMakeList.get(i).strAlphaKey.equals(mEquipmentInfo.strMake) ||
                        (mMakeList.get(i).nNumericKey != 0 && mMakeList.get(i).nNumericKey == Utils.parseIntFromString(mEquipmentInfo.strMake)))
                {
                    mMakeIdx = i;
                    break;
                }
            }
        }
        if(mMakeIdx != -1)
        {
            mTxtMake.setText(mMakeList.get(mMakeIdx).strDescription);

        }

        if(mModels != null)
        {
            mModelIdx = -1;
            for(int i = 0 ; i < mModelList.size(); i++)
            {
                if( mModelList.get(i).strAlphaKey.equals(mEquipmentInfo.strModel) ||
                        (mModelList.get(i).nNumericKey != 0 && mModelList.get(i).nNumericKey == Utils.parseIntFromString(mEquipmentInfo.strModel)))
                {
                    mModelIdx = i;
                    break;
                }
            }
        }
        if(mModelIdx != -1)
        {
            mTxtModel.setText(mModelList.get(mModelIdx).strDescription);
        }

        if(mTypes != null)
        {
            mTypeIdx = -1;
            for(int i = 0 ; i < mTypeList.size(); i++)
            {
                if( mTypeList.get(i).strAlphaKey.equals(mEquipmentInfo.strEquipmentType) ||
                        (mTypeList.get(i).nNumericKey != 0 && mTypeList.get(i).nNumericKey == Utils.parseIntFromString(mEquipmentInfo.strEquipmentType)))
                {
                    mTypeIdx = i;
                    break;
                }
            }
        }
        if(mTypeIdx != -1)
        {
            mTxtEquipmentType.setText(mTypeList.get(mTypeIdx).strDescription);
        }
    }

    @Override
    public void refreshLanguage() {
        if(mAddType == 0)
        {
            mTitleView.setText(Str.AddEquipmentDetails[Global.LANG]);
        }
        else {
            mTitleView.setText(Str.RequestEquipment[Global.LANG]);
        }

        mSerialNumberView.setText(Str.SerialNumber[Global.LANG]);
        mDescriptionView.setText(Str.Description[Global.LANG]);
        mMakeView.setText(Str.Make[Global.LANG]);
        mModelView.setText(Str.Model[Global.LANG]);
        mEquipmentTypeView.setText(Str.EquipmentType[Global.LANG]);
        mBtnConfirm.setText(Str.Confirm[Global.LANG]);
    }

    @Override
    public void receiveBroadcast(String strAction, Intent intent) {
        if(Global.gFragIdx != Global.FRAG_ADD_EQUIPMENT_LIST) return;
        if (strAction.equals(Global.ACTION_TAKE_PHOTO) == true) {
            String strPhotoPath = intent.getStringExtra(Global.EXTRA_PHOTO_PATH);
            if(!TextUtils.isEmpty(strPhotoPath))
            {
                String strNewPhotoPath = Global.UPLOAD_DIR + System.currentTimeMillis() + ".jpg";
                Utils.fileCopy(strPhotoPath, strNewPhotoPath);
                Utils.fileDelete(strPhotoPath);
                mEquipmentInfo.photoList.add(strNewPhotoPath);
            }
        }
        else if (strAction.equals(Global.ACTION_SELECT_PRODUCT) == true){
            int nType = intent.getIntExtra(Global.EXTRA_TYPE, 0);
            if (nType == Global.SELECT_PRODUCT_ITEMUPC) {
                String strFrom = intent.getStringExtra(Global.EXTRA_BARCODESCAN_FROM);
                if (!strFrom.equals(getClass().getSimpleName())) {
                    return;
                }
                String strItemUPC = intent.getStringExtra(Global.EXTRA_ITEM_UPC);
                ((EditText) mRootView.findViewById(R.id.editBarCodeScan)).setText(strItemUPC);
                doSearchEquipment(strItemUPC);
            }
        }
    }

    private String makeEquipNumber()
    {
        String strEquipNumber = "9";
        if(Global.gRouteNumber.length() == 0) strEquipNumber += "000";
        else if(Global.gRouteNumber.length() == 1) strEquipNumber += "00" + Global.gRouteNumber;
        else if(Global.gRouteNumber.length() == 2) strEquipNumber += "0" + Global.gRouteNumber;
        else
        {
            strEquipNumber += Global.gRouteNumber.substring(Global.gRouteNumber.length() - 3, Global.gRouteNumber.length());
        }

        String strY = new SimpleDateFormat("yyyy").format(new Date());
        Calendar cal = Calendar.getInstance();
        int nDayOfYear = cal.get(Calendar.DAY_OF_YEAR);
        String strJJJ = String.format(Locale.ENGLISH, "%d", nDayOfYear);
        int nPdfSequenceNo = Utils.getIntSetting(mContext, String.format(Locale.ENGLISH, Global.PREF_PDF_SEQUENCE_NO, Global.gTripNumber)) + 1;
        Utils.setIntSetting(mContext, String.format(Locale.ENGLISH, Global.PREF_PDF_SEQUENCE_NO, Global.gTripNumber), nPdfSequenceNo + 1);
        String strSSS = String.format(Locale.ENGLISH, "%d", nPdfSequenceNo);

        strEquipNumber += strY.substring(3, 4);
        if(strJJJ.length() == 0 ) strEquipNumber += "000";
        else if(strJJJ.length() == 1) strEquipNumber += "00" + strJJJ;
        else if(strJJJ.length() == 2) strEquipNumber += "0" + strJJJ;
        else strEquipNumber += strJJJ.substring(strJJJ.length() - 3, strJJJ.length());

        if(strSSS.length() == 0) strEquipNumber += "00";
        else if(strSSS.length() == 1) strEquipNumber += "0" + strSSS;
        else strEquipNumber += strSSS.substring(strSSS.length() - 2, strSSS.length());

        return strEquipNumber;
    }
    private void doConfirm()
    {
        DbAdapter dbAdapter = new DbAdapter(mContext);
        dbAdapter.open();

//        if(mAddType == 1)
//        {
            EquipmentInfo equipmentInfo = new EquipmentInfo();
            equipmentInfo.strAltEquipment = mEquipmentInfo.strAltEquipment;
            equipmentInfo.strSerialNo = mEquipmentInfo.strSerialNo;
            equipmentInfo.strEquipmentNo = mEquipmentInfo.strEquipmentNo;
            equipmentInfo.strDescription = mEquipmentInfo.strDescription;
            equipmentInfo.strMake = mEquipmentInfo.strMake;
            equipmentInfo.strModel = mEquipmentInfo.strModel;
            equipmentInfo.strEquipmentType = mEquipmentInfo.strEquipmentType;
            mEquipmentInfo.strEquipmentActType = "A";
            equipmentInfo.strEquipmentActType = mEquipmentInfo.strEquipmentActType;
            equipmentInfo.strAssetType = mEquipmentInfo.strAssetType;
            dbAdapter.insertEquipment(equipmentInfo);
//        }

        EquipAssInfo equipAssInfo = new EquipAssInfo();
        equipAssInfo.nChainNo = Global.gCurCustomerInfo.nChainNo;
        equipAssInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
        equipAssInfo.strEquipmentNo = mEquipmentInfo.strEquipmentNo;
        mEquipmentInfo.nIdx = (int)dbAdapter.insertEquipAss(equipAssInfo);
        dbAdapter.close();

        Global.gCurEquipmentWithAssInfoList.add(mEquipmentInfo);

        mContext.onBackPressed();
    }

    private void selectDescription()
    {
        if( mDescriptions == null )
        {
            if(!mContext.isFinishing() && !mContext.isDestroyed())
            {
                try {
                    Toast.makeText(mContext, Str.There_is_no_description[Global.LANG], Toast.LENGTH_LONG).show();
                }catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
        else
        {
            try {
                final int origIdx = mDescriptionIdx;
                final String strReasonCode = mEquipmentInfo.strDescription;
                String strOrgText = mTxtDescription.getText().toString();
                new AlertDialog.Builder(mContext)
                        .setTitle(Str.SelectDescription[Global.LANG])
                        .setSingleChoiceItems(new AlertSingleChoiceAdapter(mContext, mDescriptions), origIdx, (dialog, nSelected) -> {
                            mDescriptionIdx = nSelected;
                            mEquipmentInfo.strDescription = (!TextUtils.isEmpty(mDescriptionList.get(mDescriptionIdx).strAlphaKey) ? mDescriptionList.get(mDescriptionIdx).strAlphaKey : (mDescriptionList.get(mDescriptionIdx).nNumericKey != 0 ? String.valueOf(mDescriptionList.get(mDescriptionIdx).nNumericKey) : mDescriptionList.get(mDescriptionIdx).strDescription));
                            String strDescription = mDescriptionList.get(mDescriptionIdx).strDescription;
                            mTxtDescription.setText(strDescription);
                        })
                        .setPositiveButton(Str.Ok[Global.LANG], (dialog, nSelected) -> {

                        })
                        .setNegativeButton(Str.Cancel[Global.LANG], (dialog, nSelected) -> {
                            mDescriptionIdx = nSelected;
                            mEquipmentInfo.strDescription = strReasonCode;
                            mTxtDescription.setText(strOrgText);
                        })
                        .create()
                        .show();
            }catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    private void selectMake()
    {
        if( mMakes == null )
        {
            if(!mContext.isFinishing() && !mContext.isDestroyed())
            {
                try {
                    Toast.makeText(mContext, Str.There_is_no_makes[Global.LANG], Toast.LENGTH_LONG).show();
                }catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
        else
        {
            try {
                final int origIdx = mMakeIdx;
                final String strReasonCode = mEquipmentInfo.strMake;
                String strOrgText = mTxtMake.getText().toString();
                new AlertDialog.Builder(mContext)
                        .setTitle(Str.SelectMake[Global.LANG])
                        .setSingleChoiceItems(new AlertSingleChoiceAdapter(mContext, mMakes), origIdx, (dialog, nSelected) -> {
                            mMakeIdx = nSelected;
                            mEquipmentInfo.strMake = (!TextUtils.isEmpty(mMakeList.get(mMakeIdx).strAlphaKey) ? mMakeList.get(mMakeIdx).strAlphaKey : (mMakeList.get(mMakeIdx).nNumericKey != 0 ? String.valueOf(mMakeList.get(mMakeIdx).nNumericKey) : mMakeList.get(mMakeIdx).strDescription));
                            String strMake = mMakeList.get(mMakeIdx).strDescription;
                            mTxtMake.setText(strMake);
                        })
                        .setPositiveButton(Str.Ok[Global.LANG], (dialog, nSelected) -> {

                        })
                        .setNegativeButton(Str.Cancel[Global.LANG], (dialog, nSelected) -> {
                            mMakeIdx = nSelected;
                            mEquipmentInfo.strMake = strReasonCode;
                            mTxtMake.setText(strOrgText);
                        })
                        .create()
                        .show();
            }catch (Exception e)
            {
                e.printStackTrace();
            }

        }
    }

    private void selectModel()
    {
        if( mModels == null )
        {
            if(!mContext.isFinishing() && !mContext.isDestroyed())
            {
                try {
                    Toast.makeText(mContext, Str.There_is_no_models[Global.LANG], Toast.LENGTH_LONG).show();
                }catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
        else
        {
            try {
                final int origIdx = mModelIdx;
                final String strReasonCode = mEquipmentInfo.strModel;
                String strOrgText = mTxtModel.getText().toString();
                new AlertDialog.Builder(mContext)
                        .setTitle(Str.SelectModel[Global.LANG])
                        .setSingleChoiceItems(new AlertSingleChoiceAdapter(mContext, mModels), origIdx, (dialog, nSelected) -> {
                            mModelIdx = nSelected;
                            mEquipmentInfo.strModel = (!TextUtils.isEmpty(mModelList.get(mModelIdx).strAlphaKey) ? mModelList.get(mModelIdx).strAlphaKey : (mModelList.get(mModelIdx).nNumericKey != 0 ? String.valueOf(mModelList.get(mModelIdx).nNumericKey) : mModelList.get(mModelIdx).strDescription));
                            String strModel = mModelList.get(mModelIdx).strDescription;
                            mTxtModel.setText(strModel);
                        })
                        .setPositiveButton(Str.Ok[Global.LANG], (dialog, nSelected) -> {

                        })
                        .setNegativeButton(Str.Cancel[Global.LANG], (dialog, nSelected) -> {
                            mModelIdx = nSelected;
                            mEquipmentInfo.strModel = strReasonCode;
                            mTxtModel.setText(strOrgText);
                        })
                        .create()
                        .show();
            }catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    private void selectEquipmentType()
    {
        if( mTypes == null )
        {
            if(!mContext.isFinishing() && !mContext.isDestroyed())
            {
                try {
                    Toast.makeText(mContext, Str.There_is_no_equipment_types[Global.LANG], Toast.LENGTH_LONG).show();
                }catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
        else {
            try {
                final int origIdx = mTypeIdx;
                final String strReasonCode = mEquipmentInfo.strEquipmentType;
                String strOrgText = mTxtEquipmentType.getText().toString();
                new AlertDialog.Builder(mContext)
                        .setTitle(Str.SelectEquipmentType[Global.LANG])
                        .setSingleChoiceItems(new AlertSingleChoiceAdapter(mContext, mTypes), origIdx, (dialog, nSelected) -> {
                            mTypeIdx = nSelected;
                            mEquipmentInfo.strEquipmentType = (!TextUtils.isEmpty(mTypeList.get(mTypeIdx).strAlphaKey) ? mTypeList.get(mTypeIdx).strAlphaKey : (mTypeList.get(mTypeIdx).nNumericKey != 0 ? String.valueOf(mTypeList.get(mTypeIdx).nNumericKey) : mTypeList.get(mTypeIdx).strDescription));
                            String strEquipmentType = mTypeList.get(mTypeIdx).strDescription;
                            mTxtEquipmentType.setText(strEquipmentType);
                        })
                        .setPositiveButton(Str.Ok[Global.LANG], (dialog, nSelected) -> {

                        })
                        .setNegativeButton(Str.Cancel[Global.LANG], (dialog, nSelected) -> {
                            mTypeIdx = nSelected;
                            mEquipmentInfo.strEquipmentType = strReasonCode;
                            mTxtEquipmentType.setText(strOrgText);
                        })
                        .create()
                        .show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
