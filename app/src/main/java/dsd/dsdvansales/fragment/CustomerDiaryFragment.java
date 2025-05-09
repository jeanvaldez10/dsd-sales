package dsd.dsdvansales.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.UiAutomation;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.stripe.model.Customer;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dsd.dsdvansales.R;
import dsd.dsdvansales.api.AttachmentApiClient;
import dsd.dsdvansales.db.DbAdapter;
import dsd.dsdvansales.dialogs.MessageDialog;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.model.AttachmentInfo;
import dsd.dsdvansales.model.CustomerNoteInfo;
import dsd.dsdvansales.model.DescTypeInfo;
import dsd.dsdvansales.model.LoginInfo;
import dsd.dsdvansales.model.TransactionInfo;
import dsd.dsdvansales.model.UAttachmentInfo;
import dsd.dsdvansales.model.UCustomerNoteInfo;
import dsd.dsdvansales.views.AlertSingleChoiceAdapter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomerDiaryFragment extends BaseFragment implements View.OnClickListener {


    private TextView mTxtNoData = null;
    private ListView mListView = null;
    private ListAdapter mListAdapter = null;

    private List<CustomerNoteInfo> mCustomerNoteList = new ArrayList<>();
    private Map<String, DescTypeInfo> mDescTypeInfo = new HashMap<>();

    private int nSelIdx;

    public CustomerDiaryFragment() {
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
        mRootView = inflater.inflate(R.layout.fragment_customer_diary, container, false);
        commonInit();
        return mRootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        Global.gFragIdx = Global.FRAG_CUSTOMER_DIARY;
    }


    @Override
    public void initView() {
        mTxtNoData = (TextView) mRootView.findViewById(R.id.txtNoData);
        mListView = (ListView) mRootView.findViewById(R.id.listView);
        mListAdapter = new ListAdapter();
        mListView.setAdapter(mListAdapter);

        mRootView.findViewById(R.id.btnAdd).setOnClickListener(this);
        mRootView.findViewById(R.id.btnBack).setOnClickListener(this);
        loadData();
        refreshLanguage();
    }

    public Date getNoteDateTime(CustomerNoteInfo info)
    {
        Date date = new Date();
        if(!TextUtils.isEmpty(info.strCreatedTimeAndNoteDate))
        {
            try
            {
                date = new SimpleDateFormat("yyyyMMddkkmmssSSS").parse(info.strCreatedTimeAndNoteDate);
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
        else
        {
            try
            {

            }catch (Exception e) {
                e.printStackTrace();
            }

            try
            {
                if(info.strNoteId.equals("-1"))
                {
                    date = new SimpleDateFormat("yyyyMMdd kkmmss").parse(info.strNoteDate + " " + info.strNoteTime);
                }
                else
                {
                    date = new SimpleDateFormat("kkmm").parse(info.strNoteDate + " " + info.strNoteTime);
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
        return date;
    }

    @Override
    public void loadData() {
        //Get DescType Info
        DbAdapter dbAdapter = new DbAdapter(mContext);
        dbAdapter.open();
        List<DescTypeInfo> typeInfoList = dbAdapter.getDescTypeList("NoteType");
        List<CustomerNoteInfo> customerNoteInfoList = dbAdapter.getCustomerNoteInfoList(Global.gCurCustomerInfo.nCustNo, Global.gCurCustomerInfo.nChainNo);
        dbAdapter.close();

        if(typeInfoList != null && typeInfoList.size() > 0)
        {
            mDescTypeInfo.clear();
            for(DescTypeInfo info : typeInfoList)
            {
                mDescTypeInfo.put(String.valueOf(info.nNumericKey), info);
            }
        }
        mCustomerNoteList.clear();
        if(customerNoteInfoList != null && customerNoteInfoList.size() > 0)
        {
            //Sort the Customer Note Info List
            if(customerNoteInfoList != null && customerNoteInfoList.size() > 0)
            {
                Collections.sort(customerNoteInfoList, (o1, o2) -> {
                    if(o1.strNoteType.equals("91") && !o2.strNoteType.equals("91"))
                    {
                        return -1;
                    }
                    else if(o2.strNoteType.equals("91") && !o1.strNoteType.equals("91"))
                    {
                        return 1;
                    }
                    else
                    {
                        return getNoteDateTime(o2).compareTo(getNoteDateTime(o1));
                    }
                });
            }
            mCustomerNoteList.addAll(customerNoteInfoList);
        }
        refreshPage();
    }

    @Override
    public void refreshPage() {
        if (mCustomerNoteList.size() > 0) {
            mListView.setVisibility(View.VISIBLE);
            mTxtNoData.setVisibility(View.GONE);
            mListAdapter.notifyDataSetChanged();
        } else {
            mListView.setVisibility(View.GONE);
            mTxtNoData.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void refreshLanguage() {
        ((TextView) mRootView.findViewById(R.id.lblTitle)).setText(Str.CustomerDiary[Global.LANG].toUpperCase());
        ((TextView) mRootView.findViewById(R.id.txtCustomerName)).setText(Global.gCurCustomerInfo.strName);
        //((Button) mRootView.findViewById(R.id.btnAdd)).setText(Str.Add[Global.LANG]);
        mTxtNoData.setText(Str.No_data_has_been_found[Global.LANG]);
        loadData();
    }

    @Override
    public void receiveBroadcast(String strAction, Intent intent) {

    }

    private void showAttachmentList(final List<UAttachmentInfo> uAttachmentInfoList)
    {
        if(uAttachmentInfoList == null || uAttachmentInfoList.size() == 0)
        {
            return;
        }
        if(uAttachmentInfoList.size() == 1)
        {
            nSelIdx = 0;
            showAttachment(uAttachmentInfoList.get(nSelIdx));
            return;
        }

        final String[] strArray = new String[uAttachmentInfoList.size()];
        for(int i = 0; i < strArray.length; i++)
        {
            strArray[i] = uAttachmentInfoList.get(i).strFileName;
        }
        nSelIdx = -1;
        try {
            new AlertDialog.Builder(mContext)
                    .setTitle(Str.Select_Attachment[Global.LANG])
                    .setSingleChoiceItems(new AlertSingleChoiceAdapter(mContext, strArray), nSelIdx, (dialog, nSelected) -> nSelIdx = nSelected)
                    .setPositiveButton(Str.Ok[Global.LANG], (dialog, nSelected) -> {
                        if(nSelIdx != -1)
                        {
                            showAttachment(uAttachmentInfoList.get(nSelIdx));
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

    }

    private void showAttachment(UAttachmentInfo info)
    {
        if(info.strFileName.toLowerCase().endsWith(".pdf"))
        {
            Intent pdfOpenintent = new Intent(Intent.ACTION_VIEW);
            pdfOpenintent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pdfOpenintent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            pdfOpenintent.setDataAndType(Uri.parse(info.strFilePath), "application/pdf");
            try {
                startActivity(pdfOpenintent);
            }
            catch (Exception e) {
                try {
                    final MessageDialog msgDialog = new MessageDialog(mContext);
                    msgDialog.setCancelable(false);
                    msgDialog.mStrMessage = Str.No_Viewer_Available[Global.LANG];
                    msgDialog.mStrMiddle = Str.Ok[Global.LANG];
                    msgDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {

                        }
                    });
                    msgDialog.show();
                }catch (Exception e1)
                {
                    e1.printStackTrace();
                }
            }
        }
        else
        {
            try
            {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                if(info.strFilePath.startsWith("/"))
                {
                    if(Build.VERSION.SDK_INT >= 24)
                    {
                        Uri photoURI = FileProvider.getUriForFile(mContext,
                                "dsd.dsdvansales.fileprovider",
                                new File(info.strFilePath));
                        intent.setDataAndType(photoURI, "image/*");
                    }
                    else
                    {
                        intent.setDataAndType(Uri.fromFile(new File(info.strFilePath)), "image/*");
                    }
                }
                else
                {
                    intent.setDataAndType(Uri.parse(info.strFilePath), "image/*");
                }
                startActivity(intent);
            }catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    private void doShowAttachment(final CustomerNoteInfo info)
    {
        DbAdapter dbAdapter = new DbAdapter(mContext);
        dbAdapter.open();
        List<UAttachmentInfo> uAttachmentInfoList = new ArrayList<>();
        if(info.strNoteId.equals("-1"))
        {
            List<UCustomerNoteInfo> uInfoList = dbAdapter.getUCustomerInfoList(info.nCustNo, info.nChainNo, info.strNoteType, info.strNoteDate, info.strNoteTime);
            if(uInfoList.size() > 0)
            {
                uAttachmentInfoList.addAll(dbAdapter.getUAttachmentInfo(uInfoList.get(0).nTrxnNo));
            }
        }
        dbAdapter.close();

        if(uAttachmentInfoList.size() == 0 && !info.strNoteId.equals("-1") &&
                !TextUtils.isEmpty(Utils.getStringSetting(mContext, Global.PREF_APICOMPANY)) && !TextUtils.isEmpty(Utils.getStringSetting(mContext, Global.PREF_APIUSER))
                && !TextUtils.isEmpty(Utils.getStringSetting(mContext, Global.PREF_APIPASSWORD)) && Utils.isConnectingToInternet(mContext))
        {
            //Get From Api
            showProgressDialog("Loading Attachment From the server...");
            final String strPinNumber = Utils.getStringSetting(mContext, Global.PREF_ATTACHMENT_LOGIN_PIN_NUMBER);
            final String strBaseUrl = Utils.getBaseURL(strPinNumber);
            Map<String, String> params = new HashMap<>();
            params.put("username", Utils.getStringSetting(mContext, Global.PREF_ATTACHMENT_LOGIN_USERNAME));
            params.put("password", Utils.getStringSetting(mContext, Global.PREF_ATTACHMENT_LOGIN_PASSWORD));
            AttachmentApiClient.getMainApiClient(strBaseUrl, null).login(params).enqueue(new Callback<LoginInfo>() {
                @Override
                public void onResponse(Call<LoginInfo> call, Response<LoginInfo> response) {
                    if(response.body() != null)
                    {
                        String token = response.body().token;
                        Map<String, String> headers = new HashMap<>();
                        headers.put("X-Auth-Token", token);
                        AttachmentApiClient.getMainApiClient(strBaseUrl, headers).getAttachment(info.strNoteId).enqueue(new Callback<List<AttachmentInfo>>() {
                            @Override
                            public void onResponse(Call<List<AttachmentInfo>> call, Response<List<AttachmentInfo>> response) {
                                List<AttachmentInfo> attachmentInfoList = response.body();
                                if(attachmentInfoList != null && attachmentInfoList.size() > 0)
                                {
                                    hideProgressDialog();
                                    List<UAttachmentInfo> uAttachmentInfoList = new ArrayList<>();
                                    for(AttachmentInfo attachmentInfo : attachmentInfoList)
                                    {
                                        uAttachmentInfoList.add(attachmentInfo.convertToUAttachment(strBaseUrl, info.strNoteId));
                                    }
                                    showAttachmentList(uAttachmentInfoList);
                                }
                                else
                                {
                                    hideProgressDialog();
                                    if(!mContext.isFinishing() && !mContext.isDestroyed())
                                    {
                                        try {
                                            Toast.makeText(mContext, Str.NoAttachment[Global.LANG], Toast.LENGTH_LONG).show();
                                        }catch (Exception e)
                                        {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Call<List<AttachmentInfo>> call, Throwable t) {
                                hideProgressDialog();
                                if(!mContext.isFinishing() && !mContext.isDestroyed())
                                {
                                    try {
                                        Toast.makeText(mContext, Str.NetworkConnectionError[Global.LANG], Toast.LENGTH_LONG).show();
                                    }catch (Exception e)
                                    {
                                        e.printStackTrace();
                                    }
                                }

                            }
                        });
                    }
                    else
                    {
                        hideProgressDialog();
                        if(!mContext.isFinishing() && !mContext.isDestroyed())
                        {
                            try {
                                Toast.makeText(mContext, Str.LoginFailedGettingAttachment[Global.LANG], Toast.LENGTH_LONG).show();
                            }catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }

                    }
                }

                @Override
                public void onFailure(Call<LoginInfo> call, Throwable t) {
                    hideProgressDialog();
                    if(!mContext.isFinishing() && !mContext.isDestroyed())
                    {
                        try {
                            Toast.makeText(mContext, Str.NetworkConnectionError[Global.LANG], Toast.LENGTH_LONG).show();
                        }catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }

                }
            });
        }
        else
        {
            showAttachmentList(uAttachmentInfoList);
        }
    }

    private void doAddCustomerNote()
    {
        mContext.replaceFragment(new NewCustomerNoteFragment());
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id)
        {
            case R.id.btnAdd:
                doAddCustomerNote();
                break;
            case R.id.btnBack:
                mContext.onBackPressed();
                break;
        }
    }

    private void doDeleteCustomerNote(CustomerNoteInfo info)
    {
        if(info.strNoteType.equals("91"))
        {
            mCustomerNoteList.remove(info);

            //Set Upload Data to the files
            long nTrxnNo = setGeneralUploadData("NOTE", Global.gCurCustomerInfo.nCustNo, Global.gCurCustomerInfo.nChainNo, -1);

            TransactionInfo transactionInfo = new TransactionInfo();
            transactionInfo.strType = "NOTE";
            transactionInfo.strCustomerName = Global.gCurCustomerInfo.strName;
            transactionInfo.strTime = new SimpleDateFormat("kk:mm").format(new Date(nTrxnNo));
            DbAdapter dbAdapter = new DbAdapter(mContext);
            dbAdapter.open();
            dbAdapter.insertTransactionHistoryInfo(transactionInfo);
            dbAdapter.close();

            Date date = new Date();
            UCustomerNoteInfo uInfo = new UCustomerNoteInfo();
            uInfo.nTrxnNo = m_lTrxnNo;
            uInfo.strDocNo = "0";
            uInfo.strDocType = "NOTE";
            uInfo.strTrxnDate = info.strNoteDate;
            uInfo.strTrxnTime = new SimpleDateFormat("kkmmss").format(date);
            uInfo.nChainNo = info.nChainNo;
            uInfo.nCustNo = info.nCustNo;
            uInfo.strSalesDate = info.strNoteDate;
            uInfo.strTransactionType = "D";
            uInfo.strUser = Utils.getStringSetting(mContext, Global.PREF_USER_NAME);
            uInfo.strNoteType = info.strNoteType;
            uInfo.strMessage = info.strNote;
            if(!TextUtils.isEmpty(info.strNoteId) && !info.strNoteId.equals("-1"))
            {
                uInfo.strReference = info.strNoteId;
            }

            dbAdapter = new DbAdapter(mContext);
            dbAdapter.open();
            dbAdapter.deleteCustomerNoteInfo(info);
            dbAdapter.insertUCustomerNoteInfo(uInfo);
            dbAdapter.close();
            Utils.registerUploadFile(mContext, makeZipFileInfo);

            mListAdapter.notifyDataSetChanged();
        }
    }

    private class ListAdapter extends BaseAdapter {

        private class ViewHolder {
            public TextView txtCreatedDate, txtTime, txtPromotionMessage, txtMessage;
            public ImageView imgAttachment, imgDelete;
        }

        public int getCount() {
            return mCustomerNoteList.size();
        }

        public CustomerNoteInfo getItem(int position) {
            if(position < mCustomerNoteList.size())
            {
                return mCustomerNoteList.get(position);
            }
            else
            {
                return null;
            }
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = convertView;
            final ListAdapter.ViewHolder holder;
            final CustomerNoteInfo info = getItem(position);
            if(info == null) return null;
            if (convertView == null) {
                view = mContext.getLayoutInflater().inflate(R.layout.row_customer_note, parent, false);
                holder = new ListAdapter.ViewHolder();
                holder.txtCreatedDate = (TextView) view.findViewById(R.id.txtCreatedDate);
                holder.txtTime = (TextView) view.findViewById(R.id.txtTime);
                holder.txtPromotionMessage = (TextView) view.findViewById(R.id.txtPromotionMessage);
                holder.txtMessage = (TextView) view.findViewById(R.id.txtMessage);
                holder.imgAttachment = (ImageView) view.findViewById(R.id.imgAttachment);
                holder.imgDelete = (ImageView) view.findViewById(R.id.imgDelete);
                view.setTag(holder);
            } else {
                holder = (ListAdapter.ViewHolder) view.getTag();
            }
            Date date;
            String strDate = "";
            String strTime = "";
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE dd MMMM");

            if(!TextUtils.isEmpty(info.strCreatedTimeAndNoteDate))
            {
                try
                {
                    date = new SimpleDateFormat("yyyyMMddkkmmssSSS").parse(info.strCreatedTimeAndNoteDate);
                    strDate = sdf.format(date);
                    strTime = new SimpleDateFormat("kk:mm").format(date);
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else
            {
                try
                {
                    date = new SimpleDateFormat("yyyyMMdd").parse(info.strNoteDate);
                    strDate = sdf.format(date);
                }catch (Exception e) {
                    e.printStackTrace();
                }

                try
                {
                    if(info.strNoteId.equals("-1"))
                    {
                        date = new SimpleDateFormat("kkmmss").parse(info.strNoteTime);
                    }
                    else
                    {
                        date = new SimpleDateFormat("kkmm").parse(info.strNoteTime);
                    }
                    strTime = new SimpleDateFormat("kk:mm").format(date);
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }


            holder.txtCreatedDate.setText(strDate);
            holder.txtTime.setText(strTime + " by " + info.strCreatedBy);
            DescTypeInfo descTypeInfo = mDescTypeInfo.get(info.strNoteType);
            if(descTypeInfo != null)
            {
                holder.txtPromotionMessage.setText(descTypeInfo.strDescription);
            }
            else
            {
                holder.txtPromotionMessage.setText("");
            }
            holder.txtMessage.setText(info.strNote);

            if(info.nAttachment > 0)
            {
                holder.imgAttachment.setVisibility(View.VISIBLE);
                holder.imgAttachment.setTag(info);
                holder.imgAttachment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        CustomerNoteInfo info = (CustomerNoteInfo)view.getTag();
                        doShowAttachment(info);
                    }
                });
            }
            else
            {
                holder.imgAttachment.setVisibility(View.INVISIBLE);
            }

            if(info.strNoteType.equals("91"))
            {
                holder.imgDelete.setVisibility(View.VISIBLE);
                holder.imgDelete.setTag(info);
                holder.imgDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CustomerNoteInfo info = (CustomerNoteInfo)v.getTag();
                        doDeleteCustomerNote(info);
                    }
                });
            }
            else
            {
                holder.imgDelete.setVisibility(View.GONE);
            }
            return view;
        }
    }
}
