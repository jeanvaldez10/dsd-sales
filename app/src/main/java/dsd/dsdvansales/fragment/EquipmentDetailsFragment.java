package dsd.dsdvansales.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.stripe.android.model.ElementsSession;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import dsd.dsdvansales.R;
import dsd.dsdvansales.db.DbAdapter;
import dsd.dsdvansales.dialogs.EquipmentNoteDialog;
import dsd.dsdvansales.dialogs.MessageDialog;
import dsd.dsdvansales.dialogs.UpdateEquipmentDialog;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.model.DescTypeInfo;
import dsd.dsdvansales.model.EquipmentWithAssInfo;
import dsd.dsdvansales.model.UFileTransactionInfo;
import dsd.dsdvansales.views.AlertSingleChoiceAdapter;
import dsd.dsdvansales.views.TextViewDrawableSize;

public class EquipmentDetailsFragment extends BaseFragment implements View.OnClickListener {

    private EquipmentWithAssInfo mEquipmentInfo;
    private TextView mTxtVerifyFlag;

    private int nSelIdx = 0;
    private int nOrgIdx = 0;

    private Map<String, DescTypeInfo> mEquipmentDescAlphaMap = new HashMap<>();
    private Map<Integer, DescTypeInfo> mEquipmentDescNumericMap = new HashMap<>();

    private Map<String, DescTypeInfo> mMakeDescAlphaMap = new HashMap<>();
    private Map<Integer, DescTypeInfo> mMakeDescNumericMap = new HashMap<>();

    private Map<String, DescTypeInfo> mModelDescAlphaMap = new HashMap<>();
    private Map<Integer, DescTypeInfo> mModelDescNumericMap = new HashMap<>();

    private Map<String, DescTypeInfo> mTypeDescAlphaMap = new HashMap<>();
    private Map<Integer, DescTypeInfo> mTypeDescNumericMap = new HashMap<>();

    public EquipmentDetailsFragment() {
        // Required empty public constructor
    }

    public static EquipmentDetailsFragment newInstance(EquipmentWithAssInfo info) {
        EquipmentDetailsFragment fragment = new EquipmentDetailsFragment();
        fragment.setEquipmentInfo(info);
        return fragment;
    }

    public void setEquipmentInfo(EquipmentWithAssInfo info)
    {
        mEquipmentInfo = info;
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
        mRootView = inflater.inflate(R.layout.fragment_equipment_details, container, false);

        commonInit();
        return mRootView;
    }

    @Override
    public void initView() {

        ((TextView)mRootView.findViewById(R.id.txtVerify)).setOnClickListener(this);
        ((TextView)mRootView.findViewById(R.id.btnBack)).setOnClickListener(this);
        ((TextView)mRootView.findViewById(R.id.btnCompleted)).setOnClickListener(this);
        mRootView.findViewById(R.id.btnCamera).setOnClickListener(this);

        mTxtVerifyFlag = mRootView.findViewById(R.id.txtVerify);

        loadData();
        refreshLanguage();
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
                List<DescTypeInfo> descTypeInfoList = dbAdapter.getDescTypeList(Global.EQUIP_DESCRIPTION_ID);
                List<DescTypeInfo> makeTypeInfoList = dbAdapter.getDescTypeList(Global.EQUIP_MAKE_ID);
                List<DescTypeInfo> modelTypeInfoList = dbAdapter.getDescTypeList(Global.EQUIP_MODEL_ID);
                List<DescTypeInfo> typeTypeInfoList = dbAdapter.getDescTypeList(Global.EQUIP_EQUIP_TYPE_ID);
                dbAdapter.close();

                for(DescTypeInfo info : descTypeInfoList)
                {
                    if(!TextUtils.isEmpty(info.strAlphaKey))
                    {
                        mEquipmentDescAlphaMap.put(info.strAlphaKey, info);
                    }
                    if(info.nNumericKey != 0)
                    {
                        mEquipmentDescNumericMap.put(info.nNumericKey, info);
                    }
                }

                for(DescTypeInfo info : makeTypeInfoList)
                {
                    if(!TextUtils.isEmpty(info.strAlphaKey))
                    {
                        mMakeDescAlphaMap.put(info.strAlphaKey, info);
                    }
                    if(info.nNumericKey != 0)
                    {
                        mMakeDescNumericMap.put(info.nNumericKey, info);
                    }
                }

                for(DescTypeInfo info : modelTypeInfoList)
                {
                    if(!TextUtils.isEmpty(info.strAlphaKey))
                    {
                        mModelDescAlphaMap.put(info.strAlphaKey, info);
                    }
                    if(info.nNumericKey != 0)
                    {
                        mModelDescNumericMap.put(info.nNumericKey, info);
                    }
                }

                for(DescTypeInfo info : typeTypeInfoList)
                {
                    if(!TextUtils.isEmpty(info.strAlphaKey))
                    {
                        mTypeDescAlphaMap.put(info.strAlphaKey, info);
                    }
                    if(info.nNumericKey != 0)
                    {
                        mTypeDescNumericMap.put(info.nNumericKey, info);
                    }
                }

                new Handler(Looper.getMainLooper()).post(() -> {
                    hideProgressDialog();
                    refreshPage();
                });
            }
        }.start();
    }

    @Override
    public void refreshPage() {
        ((TextView)mRootView.findViewById(R.id.lblCustomerName)).setText(Global.gCurCustomerInfo.strName);

        if(mEquipmentInfo != null)
        {
            String strDescription = mEquipmentDescAlphaMap.get(mEquipmentInfo.strDescription) != null ?
                    Objects.requireNonNull(mEquipmentDescAlphaMap.get(mEquipmentInfo.strDescription)).strDescription :
                    (mEquipmentDescNumericMap.get(Utils.parseIntFromString(mEquipmentInfo.strDescription)) != null ?
                            Objects.requireNonNull(mEquipmentDescNumericMap.get(Utils.parseIntFromString(mEquipmentInfo.strDescription))).strDescription : mEquipmentInfo.strDescription);

            ((EditText)mRootView.findViewById(R.id.editDescription)).setText(strDescription);

            String strMake = mMakeDescAlphaMap.get(mEquipmentInfo.strMake) != null ?
                    Objects.requireNonNull(mMakeDescAlphaMap.get(mEquipmentInfo.strMake)).strDescription :
                    (mMakeDescNumericMap.get(Utils.parseIntFromString(mEquipmentInfo.strMake)) != null ?
                            Objects.requireNonNull(mMakeDescNumericMap.get(Utils.parseIntFromString(mEquipmentInfo.strMake))).strDescription : mEquipmentInfo.strMake);
            ((EditText)mRootView.findViewById(R.id.editMake)).setText(strMake);

            String strModel = mModelDescAlphaMap.get(mEquipmentInfo.strModel) != null ?
                    Objects.requireNonNull(mModelDescAlphaMap.get(mEquipmentInfo.strModel)).strDescription :
                    (mModelDescNumericMap.get(Utils.parseIntFromString(mEquipmentInfo.strModel)) != null ?
                            Objects.requireNonNull(mModelDescNumericMap.get(Utils.parseIntFromString(mEquipmentInfo.strModel))).strDescription : mEquipmentInfo.strModel);
            ((EditText)mRootView.findViewById(R.id.editModel)).setText(strModel);

            String strEquipmentType = mTypeDescAlphaMap.get(mEquipmentInfo.strEquipmentType) != null ?
                    Objects.requireNonNull(mTypeDescAlphaMap.get(mEquipmentInfo.strEquipmentType)).strDescription :
                    (mTypeDescNumericMap.get(Utils.parseIntFromString(mEquipmentInfo.strEquipmentType)) != null ?
                            Objects.requireNonNull(mTypeDescNumericMap.get(Utils.parseIntFromString(mEquipmentInfo.strEquipmentType))).strDescription : mEquipmentInfo.strEquipmentType);
            ((EditText)mRootView.findViewById(R.id.editEquipmentType)).setText(strEquipmentType);

            if(!TextUtils.isEmpty(mEquipmentInfo.strRepairedDate))
            {
                ((EditText)mRootView.findViewById(R.id.editLastRepaired)).setText(Utils.convertTimeFormat(mEquipmentInfo.strRepairedDate, "yyyyMMdd", Utils.getDateFormat(mContext, Global.DATE_DD_MM_YYYY_FORMAT)));
            }
            else {
                ((EditText)mRootView.findViewById(R.id.editLastRepaired)).setText("");
            }

            if(!TextUtils.isEmpty(mEquipmentInfo.strVerifiedDate))
            {
                ((EditText)mRootView.findViewById(R.id.editLastVerified)).setText(Utils.convertTimeFormat(mEquipmentInfo.strVerifiedDate, "yyyyMMdd", Utils.getDateFormat(mContext, Global.DATE_DD_MM_YYYY_FORMAT)));
            }
            else {
                ((EditText)mRootView.findViewById(R.id.editLastVerified)).setText("");
            }

            //if(mEquipmentInfo.strVerifyFlag.toUpperCase().equals("Y"))
            //{
                mTxtVerifyFlag.setEnabled(true);

                if(mEquipmentInfo.strVerified.equals(""))
                {
                    mTxtVerifyFlag.setText("");
                }
                else if( mEquipmentInfo.strVerified.equals("V") )
                {
                    mTxtVerifyFlag.setText("Verified");
                }
                else if( mEquipmentInfo.strVerified.equals("U") )
                {
                    mTxtVerifyFlag.setText("Updated");
                }
                else if( mEquipmentInfo.strVerified.equals("N") )
                {
                    mTxtVerifyFlag.setText("Not Found");
                }
                else
                {
                    mTxtVerifyFlag.setText("");
                }
            //}
        }

    }

    @Override
    public void refreshLanguage() {
        ((TextView)mRootView.findViewById(R.id.lblTitle)).setText(Str.EquipmentDetails[Global.LANG].toUpperCase());
        ((TextView)mRootView.findViewById(R.id.lblEquipmentType)).setText(Str.EquipmentType[Global.LANG]);
        ((TextView)mRootView.findViewById(R.id.lblMake)).setText(Str.Make[Global.LANG]);
        ((TextView)mRootView.findViewById(R.id.lblModel)).setText(Str.Model[Global.LANG]);
        ((TextView)mRootView.findViewById(R.id.lblLastRepaired)).setText(Str.LastRepaired[Global.LANG]);
        ((TextView)mRootView.findViewById(R.id.lblLastVerified)).setText(Str.LastVerified[Global.LANG]);
        ((TextView)mRootView.findViewById(R.id.lblVerifyEquipment)).setText(Str.VerifyEquipment[Global.LANG]);

        ((TextView)mRootView.findViewById(R.id.btnCompleted)).setText(Str.Completed[Global.LANG]);
    }

    @Override
    public void receiveBroadcast(String strAction, Intent intent) {
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
    }

    private String getCurrentTime()
    {
        SimpleDateFormat newFormat = new SimpleDateFormat("yyyyMMdd");
        return newFormat.format(new Date());
    }
    private void doVerify() {
        final String strArray[] = {"Verified", "Update", "Not Found"};
        String strValue = mTxtVerifyFlag.getText().toString();
        if( strValue.equals("") )
        {
            nOrgIdx = nSelIdx = -1;
        }
        else if( strValue.equals("Verified"))
        {
            nOrgIdx = nSelIdx = 0;
        }
        else if( strValue.equals("Update") )
        {
            nOrgIdx = nSelIdx = 1;
        }
        else if( strValue.equals("Not Found") )
        {
            nOrgIdx = nSelIdx = 2;
        }
        else
        {
            nOrgIdx = nSelIdx = -1;
        }
        try {
            new AlertDialog.Builder(mContext)
                    .setTitle(Str.SelectVerificationStatus[Global.LANG])
                    .setSingleChoiceItems(new AlertSingleChoiceAdapter(mContext, strArray), nSelIdx, (dialog, nSelected) -> {
                        nSelIdx = nSelected;
                        if( nSelIdx >=0 )
                        {
                            mTxtVerifyFlag.setText(strArray[nSelIdx]);
                        }
                    })
                    .setPositiveButton(Str.Ok[Global.LANG], (dialog, nSelected) -> {
                        if( nSelIdx == 0 )
                        {
                            mEquipmentInfo.strVerified = "V";
                            mEquipmentInfo.strVerifiedDate = getCurrentTime();
                        }
                        else if( nSelIdx == 1 )
                        {
                            mEquipmentInfo.strVerified = "U";
                            mEquipmentInfo.strVerifiedDate = getCurrentTime();
                            try {
                                final UpdateEquipmentDialog inputDlg = new UpdateEquipmentDialog(mContext);
                                inputDlg.mStrTitle = Str.Update_Equipment[Global.LANG];
                                inputDlg.mStrHint = Str.Unique_Asset_ID[Global.LANG];
                                inputDlg.mStrLeft = Str.Return[Global.LANG];
                                inputDlg.mStrRight = Str.Update[Global.LANG];
                                inputDlg.mStrInputed = mEquipmentInfo.strAltEquipment;
                                inputDlg.setCancelable(false);
                                inputDlg.setOnDismissListener(dialog1 -> {
                                    if (inputDlg.mReturnCode == Global.DLG_RETURN_RIGHT) {
                                        mEquipmentInfo.strAltEquipment = inputDlg.mStrInputed;
                                        DbAdapter dbAdapter = new DbAdapter(mContext);
                                        dbAdapter.open();
                                        dbAdapter.updateAltEquipment(mEquipmentInfo.nIdx, mEquipmentInfo.strAltEquipment);
                                        dbAdapter.close();
                                    }
                                });
                                inputDlg.show();
                            }catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
                        else if( nSelIdx == 2 )
                        {
                            mEquipmentInfo.strVerified = "N";
                            mEquipmentInfo.strVerifiedDate = getCurrentTime();
                        }
                        else
                        {
                            mEquipmentInfo.strVerified = "";
                        }
                        DbAdapter dbAdapter = new DbAdapter(mContext);
                        dbAdapter.open();
//                        if( mEquipmentInfo.strEquipmentActType.length() > 0 && !mEquipmentInfo.strEquipmentActType.equals("V"))
//                        {
//                            //Backup Current Equipment AssInfo;
//                            mEquipmentInfo.strOldEquipmentType = mEquipmentInfo.strEquipmentActType;
//                            mEquipmentInfo.strStatus = "I";
//                            dbAdapter.insertEquipAss(mEquipmentInfo);
//                        }
                        mEquipmentInfo.strStatus = "V";
                        mEquipmentInfo.strEquipmentActType = mEquipmentInfo.strVerified.length() > 0 ? "V" : "";
                        dbAdapter.updateEquipAss(mEquipmentInfo);
                        dbAdapter.updateEquipmentType(mEquipmentInfo.nIdx, mEquipmentInfo.strEquipmentActType);
                        dbAdapter.close();
                    })
                    .setNegativeButton(Str.Cancel[Global.LANG], (dialog, nSelected) -> {
                        if( nOrgIdx == -1 )
                        {
                            mTxtVerifyFlag.setText("");
                        }
                        else
                        {
                            mTxtVerifyFlag.setText(strArray[nOrgIdx]);
                        }
                    })
                    .create()
                    .show();
        }catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    private void doCompleted(){
        //Check verify status
        if( mEquipmentInfo.strVerifyFlag.equals("Y") && TextUtils.isEmpty(mEquipmentInfo.strVerified) )
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
        goBack();
    }

    private void goBack()
    {
        mContext.onBackPressed();
    }
    private void doTakePhoto()
    {
        //Add the photo for this equipment
        selectPhoto();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.txtVerify:
                doVerify();
                break;
            case R.id.btnBack:
                goBack();
                break;
            case R.id.btnCompleted:
                doCompleted();
                break;
            case R.id.btnCamera:
                doTakePhoto();
                break;
        }

    }
}