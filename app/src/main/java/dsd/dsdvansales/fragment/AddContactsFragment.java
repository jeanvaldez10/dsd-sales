package dsd.dsdvansales.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import dsd.dsdvansales.R;
import dsd.dsdvansales.db.DbAdapter;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.model.ContactDetail;
import dsd.dsdvansales.model.DescTypeInfo;
import dsd.dsdvansales.views.AlertSingleChoiceAdapter;

public class AddContactsFragment extends BaseFragment implements View.OnClickListener {
    private ContactDetail mContactDetail = null;
    public AddContactsFragment() {
        // Required empty public constructor
    }

    private boolean mIsEdit = false;
    private boolean mIsEditContact = false;

    private List<DescTypeInfo> descTypeInfoList = new ArrayList<>();

    private int mSelContactType = -1;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if(mRootView != null)
        {
            return mRootView;
        }

        mRootView = inflater.inflate(R.layout.fragment_add_contacts, container, false);
        mIsEdit = getArguments().getBoolean(Global.EXTRA_IS_EDIT_CUSTOMER, false);
        mIsEditContact = getArguments().getBoolean(Global.EXTRA_IS_EDIT_CONTACT, false);
        mContactDetail = Global.gContactDetail;
        commonInit();
        return mRootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        Global.gFragIdx = Global.FRAG_ADD_CONTACT;
    }

    @Override
    public void initView() {
        mRootView.findViewById(R.id.btnBack).setOnClickListener(this);
        mRootView.findViewById(R.id.btnAdd).setOnClickListener(this);
        mRootView.findViewById(R.id.btnInfo).setOnClickListener(this);
        mRootView.findViewById(R.id.txtContactType).setOnClickListener(this);
        loadData();
        refreshLanguage();
    }

    @Override
    public void loadData() {

        DbAdapter dbAdapter = new DbAdapter(mContext);
        dbAdapter.open();
        descTypeInfoList = dbAdapter.getDescTypeList("ContactTypes");
        dbAdapter.close();

        if(!TextUtils.isEmpty(mContactDetail.strContactType))
        {
            ((TextView)mRootView.findViewById(R.id.txtContactType)).setText(mContactDetail.strContactType);
            for(int i = 0; i < descTypeInfoList.size(); i++)
            {
                DescTypeInfo info = descTypeInfoList.get(i);
                if(info.strDescription.equals(mContactDetail.strContactType))
                {
                    mSelContactType = i;
                    break;
                }
            }
        }
        else
        {
            ((TextView)mRootView.findViewById(R.id.txtContactType)).setText(Str.SelectContactType[Global.LANG]);
        }

        ((EditText)mRootView.findViewById(R.id.editContactName)).setText(mContactDetail.strContactName);
        ((EditText)mRootView.findViewById(R.id.editContactPhone1)).setText(mContactDetail.strContactPhone1);
        ((EditText)mRootView.findViewById(R.id.editContactPhone2)).setText(mContactDetail.strContactPhone2);
        ((EditText)mRootView.findViewById(R.id.editContactPhone3)).setText(mContactDetail.strContactPhone3);
        ((EditText)mRootView.findViewById(R.id.editContactEmail1)).setText(mContactDetail.strContactEmail1);
        ((EditText)mRootView.findViewById(R.id.editContactEmail2)).setText(mContactDetail.strContactEmail2);
        ((EditText)mRootView.findViewById(R.id.editContactEmail3)).setText(mContactDetail.strContactEmail3);
        refreshPage();
    }

    @Override
    public void refreshPage() {

        if(mContactDetail.contactInfoList.size() > 0)
        {
            mRootView.findViewById(R.id.btnInfo).setVisibility(View.VISIBLE);
        }
        else
        {
            mRootView.findViewById(R.id.btnInfo).setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void refreshLanguage() {
        ((TextView) mRootView.findViewById(R.id.txtTrip)).setText(Str.Trip[Global.LANG] + " # " + Global.gTripNumber);
        ((TextView) mRootView.findViewById(R.id.lblTitle)).setText(mIsEdit == false ? Str.NewCustomer[Global.LANG].toUpperCase() : Str.EditCustomer[Global.LANG].toUpperCase());
        ((TextView) mRootView.findViewById(R.id.lblContactDetail)).setText(Str.ContactDetails[Global.LANG].toUpperCase());

        if(mIsEditContact)
        {
            ((Button)mRootView.findViewById(R.id.btnAdd)).setText(Str.Save[Global.LANG]);
        }
        else
        {
            ((Button)mRootView.findViewById(R.id.btnAdd)).setText(Str.Add[Global.LANG]);
        }

        ((TextView) mRootView.findViewById(R.id.lblContactType)).setText(Str.ContactType[Global.LANG]);
        ((TextView) mRootView.findViewById(R.id.lblContactName)).setText(Str.ContactName[Global.LANG]);
        ((TextView) mRootView.findViewById(R.id.lblContactPhone)).setText(Str.ContactPhone[Global.LANG]);
        ((TextView) mRootView.findViewById(R.id.lblContactEmail)).setText(Str.ContactEmail[Global.LANG]);

        if(mSelContactType == -1)
        {
            ((TextView)mRootView.findViewById(R.id.txtContactType)).setText(Str.SelectContactType[Global.LANG]);
        }
    }

    @Override
    public void receiveBroadcast(String strAction, Intent intent) {

    }

    private void doAdd()
    {
        if(mSelContactType != -1)
        {
            mContactDetail.strContactType = descTypeInfoList.get(mSelContactType).strDescription;
            mContactDetail.strContactTypeID = descTypeInfoList.get(mSelContactType).strDescriptionEntryKey;
            mContactDetail.nContactTypeNumericKey = descTypeInfoList.get(mSelContactType).nNumericKey;
        }

        mContactDetail.strContactName = ((EditText)mRootView.findViewById(R.id.editContactName)).getText().toString();
        mContactDetail.strContactPhone1 = ((EditText)mRootView.findViewById(R.id.editContactPhone1)).getText().toString();
        mContactDetail.strContactPhone2 = ((EditText)mRootView.findViewById(R.id.editContactPhone2)).getText().toString();
        mContactDetail.strContactPhone3 = ((EditText)mRootView.findViewById(R.id.editContactPhone3)).getText().toString();
        mContactDetail.strContactEmail1 = ((EditText)mRootView.findViewById(R.id.editContactEmail1)).getText().toString();
        mContactDetail.strContactEmail2 = ((EditText)mRootView.findViewById(R.id.editContactEmail2)).getText().toString();
        mContactDetail.strContactEmail3 = ((EditText)mRootView.findViewById(R.id.editContactEmail3)).getText().toString();

        if(!mIsEditContact)
        {
            Global.gContactDetailList.add(mContactDetail);
        }

        Global.gContactDetail = null;
        mContext.onBackPressed();
    }

    private void doAddInfo()
    {
        AddContactInformationFragment fragment = new AddContactInformationFragment();
        Bundle args = new Bundle();
        args.putBoolean(Global.EXTRA_IS_EDIT_CUSTOMER, mIsEdit);
        args.putBoolean(Global.EXTRA_IS_EDIT_CUST_INFO, false);

        fragment.setArguments(args);
        mContext.replaceFragment(fragment);
    }

    private int selected = -1;
    private void doChangeContactType()
    {
        String[] strArray = new String[descTypeInfoList.size()];
        for(int i = 0; i < descTypeInfoList.size(); i++)
        {
            strArray[i] = descTypeInfoList.get(i).strDescription;
        }

        selected = mSelContactType;

        try {
            new AlertDialog.Builder(mContext)
                    .setTitle(Str.SelectContactType[Global.LANG])
                    .setSingleChoiceItems(new AlertSingleChoiceAdapter(mContext, strArray), selected, (dialog, nSelected) -> {
                        selected = nSelected;
                    })
                    .setPositiveButton(Str.Ok[Global.LANG], (dialog, nSelected) -> {
                        ((TextView) mRootView.findViewById(R.id.txtContactType)).setText(strArray[selected]);
                        mSelContactType = selected;
                    })
                    .setNegativeButton(Str.Cancel[Global.LANG], (dialog, nSelected) -> {
                        //((TextView) mRootView.findViewById(R.id.txtContactType)).setText(strArray[mSelContactType]);
                    })
                    .create()
                    .show();
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.btnAdd:
                doAdd();
                break;
            case R.id.btnBack:
                Global.gContactDetail = null;
                Global.bContactDetailCancelled = true;
                mContext.onBackPressed();
                break;
            case R.id.btnInfo:
                doAddInfo();
                break;
            case R.id.txtContactType:
                doChangeContactType();
                break;
        }
    }
}