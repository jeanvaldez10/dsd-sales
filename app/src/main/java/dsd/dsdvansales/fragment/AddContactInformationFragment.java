package dsd.dsdvansales.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import dsd.dsdvansales.R;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.model.ContactDetail;
import dsd.dsdvansales.model.ContactInfo;

public class AddContactInformationFragment extends BaseFragment implements View.OnClickListener {

    private boolean mIsEdit = false;
    private boolean mIsEditCustInfo = false;

    private List<View> mContactInfoListView = new ArrayList<>();
    private ContactDetail mContactDetail = null;
    private List<ContactInfo> mCustInfoList = new ArrayList<>();
    public AddContactInformationFragment() {
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
        mRootView = inflater.inflate(R.layout.fragment_add_contact_information, container, false);
        mIsEdit = getArguments().getBoolean(Global.EXTRA_IS_EDIT_CUSTOMER, false);
        mIsEditCustInfo = getArguments().getBoolean(Global.EXTRA_IS_EDIT_CUST_INFO, false);
        if(mIsEditCustInfo)
        {
            mCustInfoList = Global.gCustAdditionalInfoList;
        }
        else
        {
            mContactDetail = Global.gContactDetail;
        }

        commonInit();
        return mRootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        Global.gFragIdx = Global.FRAG_ADD_CONTACT_INFO;
    }

    @Override
    public void initView() {
        mRootView.findViewById(R.id.btnAdd).setOnClickListener(this);
        mRootView.findViewById(R.id.btnBack).setOnClickListener(this);
        loadData();
        refreshLanguage();
    }

    @Override
    public void loadData() {

        LinearLayout linearLayout = mRootView.findViewById(R.id.layoutContactInfoList);
        List<ContactInfo> contactInfoList = null;
        if(mIsEditCustInfo)
        {
            contactInfoList = mCustInfoList;
        }
        else
        {
            contactInfoList = mContactDetail.contactInfoList;
        }
        for(int i = 0; i < contactInfoList.size(); i++)
        {
            View view = mContext.getLayoutInflater().inflate(R.layout.row_contact_info, null);
            ((TextView)view.findViewById(R.id.lblContactInfoDesc)).setText(contactInfoList.get(i).strContactInfoDescription);
            ((EditText)view.findViewById(R.id.editContactInfo)).setText(contactInfoList.get(i).strContactInfoValue);
            mContactInfoListView.add(view);
            linearLayout.addView(view);
        }
        refreshPage();
    }

    @Override
    public void refreshPage() {

    }

    @Override
    public void refreshLanguage() {
        ((TextView) mRootView.findViewById(R.id.txtTrip)).setText(Str.Trip[Global.LANG] + " # " + Global.gTripNumber);
        ((TextView) mRootView.findViewById(R.id.lblTitle)).setText(mIsEdit == false ? Str.NewCustomer[Global.LANG].toUpperCase() : Str.EditCustomer[Global.LANG].toUpperCase());

        if(mIsEditCustInfo)
        {
            ((TextView) mRootView.findViewById(R.id.lblContactDetail)).setText(Str.CustInformation[Global.LANG].toUpperCase());
        }
        else
        {
            ((TextView) mRootView.findViewById(R.id.lblContactDetail)).setText(Str.ContactInformation[Global.LANG].toUpperCase());
        }


        ((Button)mRootView.findViewById(R.id.btnAdd)).setText(Str.Add[Global.LANG]);
    }

    private void doAdd()
    {
        List<ContactInfo> contactInfoList = null;
        if(mIsEditCustInfo)
        {
            contactInfoList = mCustInfoList;
        }
        else
        {
            contactInfoList = mContactDetail.contactInfoList;
        }
        for(int i = 0; i < contactInfoList.size(); i++)
        {
            View view = mContactInfoListView.get(i);
            contactInfoList.get(i).strContactInfoValue = ((EditText)view.findViewById(R.id.editContactInfo)).getText().toString();
        }
        Global.gCustAdditionalInfoList = null;
        mContext.onBackPressed();
    }

    @Override
    public void receiveBroadcast(String strAction, Intent intent) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.btnAdd:
                doAdd();
                break;
            case R.id.btnBack:
                Global.gCustAdditionalInfoList = null;
                mContext.onBackPressed();
                break;
        }
    }
}