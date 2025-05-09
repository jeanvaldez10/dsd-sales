package dsd.dsdvansales.fragment;

import android.content.Intent;
import android.os.Bundle;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import dsd.dsdvansales.R;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.model.CustomerInfo;
import dsd.dsdvansales.model.UBillingDetailsInfo;

public class AddBillingDetailsFragment extends BaseFragment implements View.OnClickListener {

    private boolean mIsEdit = false;
    private UBillingDetailsInfo mBillingInfo = null;

    private CustomerInfo mTemplateCustomer = null;

    private boolean mBillingNameMandatory = false;
    private boolean mCompanyTaxIdMandatory = false;
    private boolean mAddress1Mandatory = false;
    private boolean mAddress2Mandatory = false;
    private boolean mCityMandatory = false;
    private boolean mStateMandatory = false;
    private boolean mPostcodeMandatory = false;
    private boolean mLegalNameMandatory = false;
    private boolean mCreditLimitProposedMandatory = false;

    private boolean bCompleteMandatory = false;

    private TextWatcher watcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            refreshPage();
        }
    };

    public AddBillingDetailsFragment() {
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
        mRootView = inflater.inflate(R.layout.fragment_add_billing_details, container, false);
        mIsEdit = getArguments().getBoolean(Global.EXTRA_IS_EDIT_CUSTOMER, false);
        mBillingInfo = Global.gUBillingDetailsInfo;
        mTemplateCustomer = Global.gCustomerTemplate;

        commonInit();
        return mRootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        Global.gFragIdx = Global.FRAG_ADD_BILLING_FRAGMENT;
    }

    @Override
    public void initView() {

        mRootView.findViewById(R.id.btnAdd).setOnClickListener(this);
        mRootView.findViewById(R.id.btnBack).setOnClickListener(this);

        ((EditText)mRootView.findViewById(R.id.editBillingName)).addTextChangedListener(watcher);
        ((EditText)mRootView.findViewById(R.id.editCompanyTaxID)).addTextChangedListener(watcher);
        ((EditText)mRootView.findViewById(R.id.editAddress1)).addTextChangedListener(watcher);
        ((EditText)mRootView.findViewById(R.id.editAddress2)).addTextChangedListener(watcher);
        ((EditText)mRootView.findViewById(R.id.editCity)).addTextChangedListener(watcher);
        ((EditText)mRootView.findViewById(R.id.editState)).addTextChangedListener(watcher);
        ((EditText)mRootView.findViewById(R.id.editPostCode)).addTextChangedListener(watcher);
        ((EditText)mRootView.findViewById(R.id.editLegalName)).addTextChangedListener(watcher);
        ((EditText)mRootView.findViewById(R.id.editCreditLimit)).addTextChangedListener(watcher);

        loadData();
        refreshLanguage();
    }

    @Override
    public void loadData() {
        ((EditText)mRootView.findViewById(R.id.editBillingName)).setText(mBillingInfo.strBillingName);
        ((EditText)mRootView.findViewById(R.id.editCompanyTaxID)).setText(mBillingInfo.strCompanyTaxID);
        ((EditText)mRootView.findViewById(R.id.editAddress1)).setText(mBillingInfo.strAddress1);
        ((EditText)mRootView.findViewById(R.id.editAddress2)).setText(mBillingInfo.strAddress2);
        ((EditText)mRootView.findViewById(R.id.editCity)).setText(mBillingInfo.strCity);
        ((EditText)mRootView.findViewById(R.id.editState)).setText(mBillingInfo.strState);
        ((EditText)mRootView.findViewById(R.id.editPostCode)).setText(mBillingInfo.strPostCode);
        ((EditText)mRootView.findViewById(R.id.editLegalName)).setText(mBillingInfo.strLegalName);
        ((EditText)mRootView.findViewById(R.id.editCreditLimit)).setText(mBillingInfo.strCreditLimit);

        if(!mIsEdit && mTemplateCustomer != null)
        {
            ((EditText)mRootView.findViewById(R.id.editCompanyTaxID)).setHint(mTemplateCustomer.strCompanyTaxID);
            mCompanyTaxIdMandatory = !TextUtils.isEmpty(mTemplateCustomer.strCompanyTaxID);

            ((EditText)mRootView.findViewById(R.id.editBillingName)).setHint(mTemplateCustomer.strBillName);
            mBillingNameMandatory = !TextUtils.isEmpty(mTemplateCustomer.strBillName);

            ((EditText)mRootView.findViewById(R.id.editAddress1)).setHint(mTemplateCustomer.strBillAddr1);
            mAddress1Mandatory = !TextUtils.isEmpty(mTemplateCustomer.strBillAddr1);

            ((EditText)mRootView.findViewById(R.id.editAddress2)).setHint(mTemplateCustomer.strBillAddr2);
            mAddress2Mandatory = !TextUtils.isEmpty(mTemplateCustomer.strBillAddr2);

            ((EditText)mRootView.findViewById(R.id.editCity)).setHint(mTemplateCustomer.strBillCity);
            mCityMandatory = !TextUtils.isEmpty(mTemplateCustomer.strBillCity);

            ((EditText)mRootView.findViewById(R.id.editState)).setHint(mTemplateCustomer.strState);
            mStateMandatory = !TextUtils.isEmpty(mTemplateCustomer.strState);

            ((EditText)mRootView.findViewById(R.id.editPostCode)).setHint(mTemplateCustomer.strZip);
            mPostcodeMandatory = !TextUtils.isEmpty(mTemplateCustomer.strZip);

            ((EditText)mRootView.findViewById(R.id.editLegalName)).setHint(mTemplateCustomer.strLegalName);
            mLegalNameMandatory = !TextUtils.isEmpty(mTemplateCustomer.strLegalName);

            ((EditText)mRootView.findViewById(R.id.editCreditLimit)).setHint(mTemplateCustomer.strCreditLimit);
            mCreditLimitProposedMandatory = !TextUtils.isEmpty(mTemplateCustomer.strCreditLimit);
        }
        refreshPage();
    }

    @Override
    public void refreshPage() {
        bCompleteMandatory = true;
        if(mCompanyTaxIdMandatory && TextUtils.isEmpty(((EditText)mRootView.findViewById(R.id.editCompanyTaxID)).getText().toString()))
        {
            bCompleteMandatory = false;
        }
        if(mBillingNameMandatory && TextUtils.isEmpty(((EditText)mRootView.findViewById(R.id.editBillingName)).getText().toString()))
        {
            bCompleteMandatory = false;
        }
        if(mAddress1Mandatory && TextUtils.isEmpty(((EditText)mRootView.findViewById(R.id.editAddress1)).getText().toString()))
        {
            bCompleteMandatory = false;
        }
        if(mAddress2Mandatory && TextUtils.isEmpty(((EditText)mRootView.findViewById(R.id.editAddress2)).getText().toString()))
        {
            bCompleteMandatory = false;
        }
        if(mCityMandatory && TextUtils.isEmpty(((EditText)mRootView.findViewById(R.id.editCity)).getText().toString()))
        {
            bCompleteMandatory = false;
        }
        if(mStateMandatory && TextUtils.isEmpty(((EditText)mRootView.findViewById(R.id.editState)).getText().toString()))
        {
            bCompleteMandatory = false;
        }
        if(mPostcodeMandatory && TextUtils.isEmpty(((EditText)mRootView.findViewById(R.id.editPostCode)).getText().toString()))
        {
            bCompleteMandatory = false;
        }
        if(mLegalNameMandatory && TextUtils.isEmpty(((EditText)mRootView.findViewById(R.id.editLegalName)).getText().toString()))
        {
            bCompleteMandatory = false;
        }
        if(mCreditLimitProposedMandatory && TextUtils.isEmpty(((EditText)mRootView.findViewById(R.id.editCreditLimit)).getText().toString()))
        {
            bCompleteMandatory = false;
        }

        if(bCompleteMandatory)
        {
            mRootView.findViewById(R.id.btnAdd).setEnabled(true);
            ((Button)mRootView.findViewById(R.id.btnAdd)).setTextColor(getResources().getColor(R.color.white));
            mRootView.findViewById(R.id.btnAdd).setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_orange_large));
        }
        else
        {
            mRootView.findViewById(R.id.btnAdd).setEnabled(false);
            ((Button)mRootView.findViewById(R.id.btnAdd)).setTextColor(getResources().getColor(R.color.grey_color));
            mRootView.findViewById(R.id.btnAdd).setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_button_grey_large));
        }

    }

    @Override
    public void refreshLanguage() {
        ((TextView) mRootView.findViewById(R.id.txtTrip)).setText(Str.Trip[Global.LANG] + " # " + Global.gTripNumber);
        ((TextView) mRootView.findViewById(R.id.lblTitle)).setText(mIsEdit == false ? Str.NewCustomer[Global.LANG].toUpperCase() : Str.EditCustomer[Global.LANG].toUpperCase());
        ((TextView) mRootView.findViewById(R.id.lblBillingDetails)).setText(Str.BillingDetails[Global.LANG].toUpperCase());

        if(mCompanyTaxIdMandatory)
        {
            ((TextView) mRootView.findViewById(R.id.lblCompanyTaxID)).setText(Str.CompanyTaxID[Global.LANG] + "  *");
        }
        else
        {
            ((TextView) mRootView.findViewById(R.id.lblCompanyTaxID)).setText(Str.CompanyTaxID[Global.LANG]);
        }
        if(mBillingNameMandatory)
        {
            ((TextView) mRootView.findViewById(R.id.lblBillingName)).setText(Str.BillingName[Global.LANG] + "  *");
        }
        else
        {
            ((TextView) mRootView.findViewById(R.id.lblBillingName)).setText(Str.BillingName[Global.LANG]);
        }
        if(mAddress1Mandatory)
        {
            ((TextView) mRootView.findViewById(R.id.lblAddress1)).setText(Str.AddressLine[Global.LANG] + "1  *");
        }
        else
        {
            ((TextView) mRootView.findViewById(R.id.lblAddress1)).setText(Str.AddressLine[Global.LANG] + "1");
        }

        if(mAddress2Mandatory)
        {
            ((TextView) mRootView.findViewById(R.id.lblAddress2)).setText(Str.AddressLine[Global.LANG] + "2  *");
        }
        else
        {
            ((TextView) mRootView.findViewById(R.id.lblAddress2)).setText(Str.AddressLine[Global.LANG] + "2");
        }

        if(mCityMandatory)
        {
            ((TextView) mRootView.findViewById(R.id.lblCity)).setText(Str.City[Global.LANG] + "  *");
        }
        else
        {
            ((TextView) mRootView.findViewById(R.id.lblCity)).setText(Str.City[Global.LANG]);
        }

        if(mStateMandatory)
        {
            ((TextView) mRootView.findViewById(R.id.lblState)).setText(Str.State[Global.LANG] + "  *");
        }
        else
        {
            ((TextView) mRootView.findViewById(R.id.lblState)).setText(Str.State[Global.LANG]);
        }

        if(mPostcodeMandatory)
        {
            ((TextView) mRootView.findViewById(R.id.lblPostCode)).setText(Str.Postcode[Global.LANG] + "  *");
        }
        else
        {
            ((TextView) mRootView.findViewById(R.id.lblPostCode)).setText(Str.Postcode[Global.LANG]);
        }

        if(mLegalNameMandatory)
        {
            ((TextView) mRootView.findViewById(R.id.lblLegalName)).setText(Str.LegalName[Global.LANG] + "  *");
        }
        else
        {
            ((TextView) mRootView.findViewById(R.id.lblLegalName)).setText(Str.LegalName[Global.LANG]);
        }

        if(mCreditLimitProposedMandatory)
        {
            ((TextView) mRootView.findViewById(R.id.lblCreditLimit)).setText(Str.CreditLimit[Global.LANG] + "  *");
        }
        else
        {
            ((TextView) mRootView.findViewById(R.id.lblCreditLimit)).setText(Str.CreditLimit[Global.LANG]);
        }
    }

    @Override
    public void receiveBroadcast(String strAction, Intent intent) {

    }

    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.btnAdd:
                Global.gUBillingDetailsInfo.strBillingName = ((EditText)mRootView.findViewById(R.id.editBillingName)).getText().toString();
                Global.gUBillingDetailsInfo.strCompanyTaxID = ((EditText)mRootView.findViewById(R.id.editCompanyTaxID)).getText().toString();
                Global.gUBillingDetailsInfo.strAddress1 = ((EditText)mRootView.findViewById(R.id.editAddress1)).getText().toString();
                Global.gUBillingDetailsInfo.strAddress2 = ((EditText)mRootView.findViewById(R.id.editAddress2)).getText().toString();
                Global.gUBillingDetailsInfo.strCity = ((EditText)mRootView.findViewById(R.id.editCity)).getText().toString();
                Global.gUBillingDetailsInfo.strState = ((EditText)mRootView.findViewById(R.id.editState)).getText().toString();
                Global.gUBillingDetailsInfo.strPostCode = ((EditText)mRootView.findViewById(R.id.editPostCode)).getText().toString();
                Global.gUBillingDetailsInfo.strLegalName = ((EditText)mRootView.findViewById(R.id.editLegalName)).getText().toString();
                Global.gUBillingDetailsInfo.strCreditLimit = ((EditText)mRootView.findViewById(R.id.editCreditLimit)).getText().toString();
                mContext.onBackPressed();
                break;
            case R.id.btnBack:
                Global.gUBillingDetailsInfo = null;
                mContext.onBackPressed();
                break;
        }
    }
}