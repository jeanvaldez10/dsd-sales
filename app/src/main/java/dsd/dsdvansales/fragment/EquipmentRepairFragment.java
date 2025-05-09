package dsd.dsdvansales.fragment;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import dsd.dsdvansales.R;
import dsd.dsdvansales.db.DbAdapter;
import dsd.dsdvansales.dialogs.MessageDialog;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.model.DescTypeInfo;
import dsd.dsdvansales.model.DocTextInfo;
import dsd.dsdvansales.model.EquipmentWithAssInfo;
import dsd.dsdvansales.views.AlertSingleChoiceAdapter;

public class EquipmentRepairFragment extends BaseFragment implements View.OnClickListener{

    private TextView mTitleView, mCustomerNameView, mFaultView, mNotesView, mRepairStatusView, mRepairTypeView, mRequestedPickUpDateView;
    private TextView mTxtFault;
    private TextView mTxtRepairStatus;
    private TextView mTxtRepairType;
    private TextView mTxtRequestedPickUpDate;
    private EditText mEditNotes;
    private Button mBtnConfirm;

    private List<DescTypeInfo> mDescTypeList = new ArrayList<>();
    private List<DescTypeInfo> mStatusDescTypeList = new ArrayList<>();
    private List<DescTypeInfo> mTypeDescTypeList = new ArrayList<>();
    private List<DocTextInfo> mDocTextList = new ArrayList<>();
    private String[] mDescTypeStrings = null;
    private String[] mStatusDescTypeStrings = null;
    private String[] mTypeDescTypeStrings = null;

    private String mStrPhotoPath = "";
    private int mReasonIdx = 0;

    private int mStatusReasonIdx = 0;
    private int mTypeReasonIdx = 0;

    private String mStrReasonCode = "";
    private String mStrTypeReasonCode = "";
    private String mStrStatusReasonCode = "";

    private String strRepairRequestedPickUpDate;

    EquipmentWithAssInfo mEquipmentInfo;

    public EquipmentRepairFragment() {
        // Required empty public constructor
    }

    public static EquipmentRepairFragment newInstance(EquipmentWithAssInfo info) {
        EquipmentRepairFragment fragment = new EquipmentRepairFragment();
        fragment.setEquipmentInfo(info);
        return fragment;
    }

    private void setEquipmentInfo(EquipmentWithAssInfo info)
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
        mRootView = inflater.inflate(R.layout.fragment_equipment_repair, container, false);
        commonInit();
        return mRootView;
    }


    @Override
    public void initView() {
        mTitleView = (TextView)mRootView.findViewById(R.id.lblTitle);
        mCustomerNameView = (TextView)mRootView.findViewById(R.id.lblCustomerName);
        mFaultView = (TextView)mRootView.findViewById(R.id.lblFault);
        mNotesView = (TextView)mRootView.findViewById(R.id.lblNotes);
        mRepairStatusView = (TextView)mRootView.findViewById(R.id.lblRepairStatus);
        mRepairTypeView = (TextView)mRootView.findViewById(R.id.lblRepairType);
        mRequestedPickUpDateView = (TextView)mRootView.findViewById(R.id.lblRequestedPickupDate);

        mEditNotes = (EditText)mRootView.findViewById(R.id.editNotes);
        mBtnConfirm = (Button)mRootView.findViewById(R.id.btnConfirm);
        mTxtFault = (TextView)mRootView.findViewById(R.id.txtFault);
        mTxtRepairStatus = (TextView)mRootView.findViewById(R.id.txtRepairStatus);
        mTxtRepairType = (TextView)mRootView.findViewById(R.id.txtRepairType);
        mTxtRequestedPickUpDate = (TextView)mRootView.findViewById(R.id.txtRequestedPickedUpDate);

        mTxtFault.setOnClickListener(this);
        mTxtRepairStatus.setOnClickListener(this);
        mTxtRepairType.setOnClickListener(this);
        mTxtRequestedPickUpDate.setOnClickListener(this);

        mBtnConfirm.setOnClickListener(this);
        mRootView.findViewById(R.id.btnBack).setOnClickListener(this);
        mRootView.findViewById(R.id.btnPhoto).setOnClickListener(this);
        mRootView.findViewById(R.id.btnContactCustomer).setOnClickListener(this);

        loadData();
        refreshLanguage();
    }

    @Override
    public void loadData() {
        //Load Repair Reason Fields From the DescType.xml file.
        DbAdapter dbAdapter = new DbAdapter(mContext);
        dbAdapter.open();
        mDescTypeList = dbAdapter.getDescTypeList(Global.EQUIP_REPAIR_REASON_DESC_ID);
        mTypeDescTypeList = dbAdapter.getDescTypeList(Global.EQUIP_REPAIR_TYPE_DESC_ID);
        mStatusDescTypeList = dbAdapter.getDescTypeList(Global.EQUIP_REPAIR_STATUS_DESC_ID);
        mDocTextList = dbAdapter.getDocTextList(Global.CONTACT_DOC_TEXT_TYPE);
        dbAdapter.close();
        refreshPage();
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

    @Override
    public void refreshPage() {
        mEditNotes.setText(mEquipmentInfo.strRepairNotes);
        mCustomerNameView.setText(Global.gCurCustomerInfo.strName);

        mDescTypeStrings = new String[mDescTypeList.size()];
        for(int i = 0; i < mDescTypeList.size(); i++)
        {
            mDescTypeStrings[i] = mDescTypeList.get(i).strDescription;
            if( mDescTypeList.get(i).strAlphaKey.equals(mEquipmentInfo.strRepairReason) )
            {
                mTxtFault.setText(mDescTypeStrings[i]);
                mReasonIdx = i;
                mStrReasonCode = mDescTypeList.get(i).strAlphaKey;
                break;
            }
        }
        if( mReasonIdx == 0 )
        {
            mStrReasonCode = "";
        }

        mStatusDescTypeStrings = new String[mStatusDescTypeList.size()];
        for(int i = 0; i < mStatusDescTypeList.size(); i++)
        {
            mStatusDescTypeStrings[i] = mStatusDescTypeList.get(i).strDescription;
            if( mStatusDescTypeList.get(i).strAlphaKey.equals(mEquipmentInfo.strRepairStatus) )
            {
                mTxtRepairStatus.setText(mStatusDescTypeStrings[i]);
                mStatusReasonIdx = i;
                mStrStatusReasonCode = mStatusDescTypeList.get(i).strAlphaKey;
                break;
            }
        }
        if( mStatusReasonIdx == 0 )
        {
            mStrStatusReasonCode = "";
        }

        mTypeDescTypeStrings = new String[mTypeDescTypeList.size()];
        for(int i = 0; i < mTypeDescTypeList.size(); i++)
        {
            mTypeDescTypeStrings[i] = mTypeDescTypeList.get(i).strDescription;
            if( mTypeDescTypeList.get(i).strAlphaKey.equals(mEquipmentInfo.strRepairType) )
            {
                mTxtRepairType.setText(mTypeDescTypeStrings[i]);
                mTypeReasonIdx = i;
                mStrTypeReasonCode = mTypeDescTypeList.get(i).strAlphaKey;
                break;
            }
        }
        if( mTypeReasonIdx == 0 )
        {
            mStrTypeReasonCode = "";
        }
        strRepairRequestedPickUpDate = mEquipmentInfo.strRepairRequestedPickUpDate;
        mTxtRequestedPickUpDate.setText(ConvertFormat(strRepairRequestedPickUpDate));

        mStrPhotoPath = mEquipmentInfo.strRepairImage;
    }

    @Override
    public void refreshLanguage() {
        mTitleView.setText(Str.EquipmentRepair[Global.LANG]);
        mFaultView.setText(Str.Fault[Global.LANG]);
        mNotesView.setText(Str.Notes[Global.LANG]);
        mRepairStatusView.setText(Str.RepairStatus[Global.LANG]);
        mRepairTypeView.setText(Str.RepairType[Global.LANG]);
        mRequestedPickUpDateView.setText(Str.RequestedPickUpDate[Global.LANG]);
        mBtnConfirm.setText(Str.Confirm[Global.LANG]);
    }

    @Override
    public void receiveBroadcast(String strAction, Intent intent) {
        if (strAction.equals(Global.ACTION_TAKE_PHOTO) == true) {
            mStrPhotoPath = intent.getStringExtra(Global.EXTRA_PHOTO_PATH);
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.btnBack:
                mContext.onBackPressed();
                break;
            case R.id.btnPhoto:
                selectPhoto();
                break;
            case R.id.btnContactCustomer:
                try {
                    final MessageDialog msgDialog = new MessageDialog(mContext);
                    msgDialog.setCancelable(false);
                    msgDialog.mStrTitle = Str.Repair_Contact_Details[Global.LANG];
                    String strContactMsg = "";
                    for(DocTextInfo docTextInfo : mDocTextList)
                    {
                        strContactMsg += docTextInfo.strDocText + "\n";
                    }
                    msgDialog.mStrMessage = strContactMsg;
                    msgDialog.mStrLeft = Str.Return[Global.LANG];
                    msgDialog.show();
                }catch (Exception e)
                {
                    e.printStackTrace();
                }
                break;
            case R.id.btnConfirm:
                doConfirm();
                break;
            case R.id.txtFault:
                selectFault();
                break;
            case R.id.txtRepairStatus:
                selectRepairStatus();
                break;
            case R.id.txtRepairType:
                selectRepairType();
                break;
            case R.id.txtRequestedPickedUpDate:
                selectRequestPickupDate();
                break;
        }
    }

    private DatePickerDialog.OnDateSetListener dateListener = (view, year, monthOfYear, dayOfMonth) -> {
        Date myDate = new Date(year-1900, monthOfYear, dayOfMonth, 0, 0, 0);
        SimpleDateFormat newFormat = new SimpleDateFormat("yyyyMMdd");
        strRepairRequestedPickUpDate = newFormat.format(myDate);
        mTxtRequestedPickUpDate.setText(ConvertFormat(strRepairRequestedPickUpDate));
    };

    private void selectRequestPickupDate()
    {
        Date date = new Date();
        if(!TextUtils.isEmpty(strRepairRequestedPickUpDate) && strRepairRequestedPickUpDate.length() == 8)
        {
            try {
                date = new SimpleDateFormat("yyyyMMdd").parse(strRepairRequestedPickUpDate);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            new DatePickerDialog(mContext, dateListener, 1900 + date.getYear(), date.getMonth(), date.getDate()).show();
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void selectFault()
    {
        if(mDescTypeStrings.length == 0)
        {
            Toast.makeText(mContext, Str.NoFaultReason[Global.LANG], Toast.LENGTH_LONG).show();
            return;
        }

        final int origIdx = mReasonIdx;
        final String strReasonCode = mStrReasonCode;

        try {
            new AlertDialog.Builder(mContext)
                    .setTitle(Str.SelectRepairReason[Global.LANG])
                    .setSingleChoiceItems(new AlertSingleChoiceAdapter(mContext, mDescTypeStrings), mReasonIdx, (dialog, nSelected) -> {
                        mReasonIdx = nSelected;
                        mStrReasonCode = mDescTypeList.get(mReasonIdx).strAlphaKey;
                        mTxtFault.setText(mDescTypeList.get(mReasonIdx).strDescription);
                    })
                    .setPositiveButton(Str.Ok[Global.LANG], (dialog, nSelected) -> {

                    })
                    .setNegativeButton(Str.Cancel[Global.LANG], (dialog, nSelected) -> {
                        mReasonIdx = origIdx;
                        mStrReasonCode = strReasonCode;
                        mTxtFault.setText(mDescTypeList.get(mReasonIdx).strDescription);
                    })
                    .create()
                    .show();
        }catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    private void selectRepairStatus()
    {
        if(mStatusDescTypeStrings.length == 0)
        {
            Toast.makeText(mContext, Str.NoRepairStatusReason[Global.LANG], Toast.LENGTH_LONG).show();
            return;
        }
        final int origIdx = mStatusReasonIdx;
        final String strReasonCode = mStrStatusReasonCode;

        try {
            new AlertDialog.Builder(mContext)
                    .setTitle(Str.SelectRepairStatus[Global.LANG])
                    .setSingleChoiceItems(new AlertSingleChoiceAdapter(mContext, mStatusDescTypeStrings), mStatusReasonIdx, (dialog, nSelected) -> {
                        mStatusReasonIdx = nSelected;
                        mStrStatusReasonCode = mStatusDescTypeList.get(mStatusReasonIdx).strAlphaKey;
                        mTxtRepairStatus.setText(mStatusDescTypeList.get(mStatusReasonIdx).strDescription);
                    })
                    .setPositiveButton(Str.Ok[Global.LANG], (dialog, nSelected) -> {

                    })
                    .setNegativeButton(Str.Cancel[Global.LANG], (dialog, nSelected) -> {
                        mStatusReasonIdx = origIdx;
                        mStrStatusReasonCode = strReasonCode;
                        mTxtRepairStatus.setText(mStatusDescTypeList.get(mStatusReasonIdx).strDescription);
                    })
                    .create()
                    .show();
        }catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    private void selectRepairType()
    {
        if(mTypeDescTypeStrings.length == 0)
        {
            Toast.makeText(mContext, Str.NoRepairTypeReason[Global.LANG], Toast.LENGTH_LONG).show();
            return;
        }
        final int origIdx = mTypeReasonIdx;
        final String strReasonCode = mStrTypeReasonCode;

        try {
            new AlertDialog.Builder(mContext)
                    .setTitle(Str.SelectRepairType[Global.LANG])
                    .setSingleChoiceItems(new AlertSingleChoiceAdapter(mContext, mTypeDescTypeStrings), mTypeReasonIdx, (dialog, nSelected) -> {
                        mTypeReasonIdx = nSelected;
                        mStrTypeReasonCode = mTypeDescTypeList.get(mTypeReasonIdx).strAlphaKey;
                        mTxtRepairType.setText(mTypeDescTypeList.get(mTypeReasonIdx).strDescription);
                    })
                    .setPositiveButton(Str.Ok[Global.LANG], (dialog, nSelected) -> {

                    })
                    .setNegativeButton(Str.Cancel[Global.LANG], (dialog, nSelected) -> {
                        mTypeReasonIdx = origIdx;
                        mStrTypeReasonCode = strReasonCode;
                        mTxtRepairType.setText(mTypeDescTypeList.get(mTypeReasonIdx).strDescription);
                    })
                    .create()
                    .show();
        }catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    private String getCurrentTime()
    {
        SimpleDateFormat newFormat = new SimpleDateFormat("yyyyMMdd");
        return newFormat.format(new Date());
    }

    private void doConfirm()
    {
        DbAdapter dbAdapter = new DbAdapter(mContext);
        dbAdapter.open();
//        if( mEquipmentInfo.strEquipmentActType.length() > 0)
//        {
//            //Backup Current Equipment AssInfo;
//            mEquipmentInfo.strOldEquipmentType = mEquipmentInfo.strEquipmentActType;
//            mEquipmentInfo.strStatus = "I";
//            dbAdapter.insertEquipAss(mEquipmentInfo);
//        }
        mEquipmentInfo.strStatus = "V";
        mEquipmentInfo.strEquipmentActType = "R";
        mEquipmentInfo.strRepairReason = mStrReasonCode;
        mEquipmentInfo.strRepairStatus = mStrStatusReasonCode;
        mEquipmentInfo.strRepairType = mStrTypeReasonCode;
        mEquipmentInfo.strRepairRequestedPickUpDate = strRepairRequestedPickUpDate;
        mEquipmentInfo.strRepairImage = mStrPhotoPath;
        mEquipmentInfo.strRepairedDate = getCurrentTime();
        mEquipmentInfo.strRepairNotes = mEditNotes.getText().toString();
        dbAdapter.updateEquipAss(mEquipmentInfo);
        dbAdapter.updateEquipmentType(mEquipmentInfo.nIdx, mEquipmentInfo.strEquipmentActType);
        dbAdapter.close();
        mContext.onBackPressed();
    }

}
