package dsd.dsdvansales.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import dsd.dsdvansales.R;
import dsd.dsdvansales.db.DbAdapter;
import dsd.dsdvansales.dialogs.MessageDialog;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.model.ContactDetail;
import dsd.dsdvansales.model.ContactInfo;
import dsd.dsdvansales.model.DescTypeInfo;

public class ContactsDetailListFragment extends BaseFragment implements View.OnClickListener {

    List<ContactDetail> mContactDetailList = new ArrayList<>();

    private boolean mIsEdit = false;

    private ListView mContactListView = null;
    private ListAdapter mContactListAdapter = null;
    public ContactsDetailListFragment() {
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
        mRootView = inflater.inflate(R.layout.fragment_contact_detail_list, container, false);
        mIsEdit = getArguments().getBoolean(Global.EXTRA_IS_EDIT_CUSTOMER, false);
        mContactDetailList = Global.gContactDetailList;

        commonInit();
        return mRootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        Global.gFragIdx = Global.FRAG_CONTACT_DETAIL_LIST;
    }

    @Override
    public void initView() {

        mRootView.findViewById(R.id.btnAdd).setOnClickListener(this);
        mRootView.findViewById(R.id.btnBack).setOnClickListener(this);
        mContactListView = mRootView.findViewById(R.id.contact_list);
        mContactListAdapter = new ListAdapter();
        mContactListView.setAdapter(mContactListAdapter);

        loadData();
        refreshLanguage();
    }

    @Override
    public void loadData() {

        if(mContactDetailList.size() == 0)
        {
            if (!Global.bContactDetailCancelled) {
                new Handler(Looper.getMainLooper()).post(() -> mRootView.findViewById(R.id.btnAdd).performClick());
            }
            if (Global.bContactDetailCancelled) {
                Global.bContactDetailCancelled = false;
                new Handler(Looper.getMainLooper()).post(() -> mRootView.findViewById(R.id.btnBack).performClick());
            }
        }
        refreshPage();
    }

    @Override
    public void refreshPage() {
        mContactListAdapter.notifyDataSetChanged();
    }

    @Override
    public void refreshLanguage() {
        ((TextView) mRootView.findViewById(R.id.txtTrip)).setText(Str.Trip[Global.LANG] + " # " + Global.gTripNumber);
        ((TextView) mRootView.findViewById(R.id.lblTitle)).setText(mIsEdit == false ? Str.NewCustomer[Global.LANG].toUpperCase() : Str.EditCustomer[Global.LANG].toUpperCase());
        ((TextView) mRootView.findViewById(R.id.lblContactDetail)).setText(Str.ContactDetails[Global.LANG].toUpperCase());
    }

    @Override
    public void receiveBroadcast(String strAction, Intent intent) {

    }

    private ContactDetail newContact()
    {
        ContactDetail contactDetail = new ContactDetail();
        //Get the contact info fields from db.
        DbAdapter dbAdapter = new DbAdapter(mContext);
        dbAdapter.open();
        List<DescTypeInfo> descTypeInfList = dbAdapter.getDescTypeList("CONTINFO");
        dbAdapter.close();

        if(descTypeInfList != null && descTypeInfList.size() > 0)
        {
            for(DescTypeInfo typeInfo : descTypeInfList)
            {
                ContactInfo contactInfo = new ContactInfo();
                contactInfo.strContactInfoAlphaKey = typeInfo.strAlphaKey;
                contactInfo.strContactInfoEntryKey = typeInfo.strDescriptionEntryKey;
                contactInfo.strContactInfoDescription = typeInfo.strDescription;
                contactInfo.nContactInfoNumericKey = typeInfo.nNumericKey;
                contactDetail.contactInfoList.add(contactInfo);
            }
        }
        return contactDetail;
    }

    private void doAddContact()
    {
        Global.gContactDetail = newContact();

        AddContactsFragment fragment = new AddContactsFragment();
        Bundle args = new Bundle();
        args.putBoolean(Global.EXTRA_IS_EDIT_CUSTOMER, mIsEdit);
        args.putBoolean(Global.EXTRA_IS_EDIT_CONTACT, false);
        fragment.setArguments(args);
        mContext.replaceFragment(fragment);
    }

    private void doEditContact(ContactDetail detail)
    {
        Global.gContactDetail = detail;

        AddContactsFragment fragment = new AddContactsFragment();
        Bundle args = new Bundle();
        args.putBoolean(Global.EXTRA_IS_EDIT_CUSTOMER, mIsEdit);
        args.putBoolean(Global.EXTRA_IS_EDIT_CONTACT, true);
        fragment.setArguments(args);
        mContext.replaceFragment(fragment);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.btnAdd:
                doAddContact();
                break;
            case R.id.btnBack:
                Global.gContactDetailList = null;
                mContext.onBackPressed();
                break;
        }
    }

    public class ListAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return mContactDetailList.size();
        }

        @Override
        public ContactDetail getItem(int position) {
            return mContactDetailList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            view = mContext.getLayoutInflater().inflate(R.layout.row_contact_detail, parent, false);

            ContactDetail contactDetail = getItem(position);
            ((TextView)view.findViewById(R.id.txtContactType)).setText(contactDetail.strContactType);
            ((TextView)view.findViewById(R.id.txtContactName)).setText(contactDetail.strContactName);
            ((TextView)view.findViewById(R.id.txtContactPhone)).setText(contactDetail.strContactPhone1);
            ((TextView)view.findViewById(R.id.txtContactEmail)).setText(contactDetail.strContactEmail1);

            view.setOnClickListener(v -> doEditContact(contactDetail));
            view.setOnLongClickListener(v -> {
                try {
                    final MessageDialog msgDialog = new MessageDialog(mContext);
                    msgDialog.setCancelable(false);
                    msgDialog.mStrMessage = Str.WishToDeleteThisContact[Global.LANG];
                    msgDialog.mStrLeft = Str.Yes[Global.LANG];
                    msgDialog.mStrRight = Str.No[Global.LANG];
                    msgDialog.setOnDismissListener(dialog -> {
                        if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
                            mContactDetailList.remove(contactDetail);
                            refreshPage();
                        }
                    });
                    msgDialog.show();
                }catch (Exception e)
                {
                    e.printStackTrace();
                }
                return false;
            });

            return view;
        }
    }
}