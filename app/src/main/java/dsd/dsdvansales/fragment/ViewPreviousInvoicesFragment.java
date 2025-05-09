package dsd.dsdvansales.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dsd.dsdvansales.R;
import dsd.dsdvansales.api.ConnectApiClient;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.model.ArchiveInfo;
import dsd.dsdvansales.model.LoginInfo;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ViewPreviousInvoicesFragment extends BaseFragment implements View.OnClickListener {

    private List<ArchiveInfo> mArchiveInfoList = new ArrayList<>();
    private ListView mArchiveListView = null;
    private ListAdapter mArchiveAdapter = null;

    public ViewPreviousInvoicesFragment() {
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
        mRootView = inflater.inflate(R.layout.fragment_view_previous_invoices, container, false);
        commonInit();
        return mRootView;
    }

    @Override
    public void initView() {
        mArchiveListView = mRootView.findViewById(R.id.list);
        mArchiveAdapter = new ListAdapter();
        mArchiveListView.setAdapter(mArchiveAdapter);
        mRootView.findViewById(R.id.btnBack).setOnClickListener(this);
        loadData();
        refreshLanguage();
    }

    @Override
    public void loadData() {
        //Call api to get the data.
        final String strBaseUrl = Utils.getBaseURL(Utils.getStringSetting(mContext, Global.PREF_APICOMPANY));
        if(!TextUtils.isEmpty(strBaseUrl) && Utils.isConnectingToInternet(mContext))
        {
            showProgressDialog(Str.Loading[Global.LANG]);
            Map<String, String> params = new HashMap<>();
            params.put("username", Utils.getStringSetting(mContext, Global.PREF_APIUSER));
            params.put("password", Utils.getStringSetting(mContext, Global.PREF_APIPASSWORD));
            ConnectApiClient.getMainApiClient(strBaseUrl, null).login(params).enqueue(new Callback<LoginInfo>() {
                @Override
                public void onResponse(Call<LoginInfo> call, Response<LoginInfo> response) {
                    if(response.isSuccessful() && response.body() != null)
                    {
                        final LoginInfo body = response.body();
                        Map<String, String> headers = new HashMap<>();
                        headers.put("X-Auth-Token", body.token);
                        String strEndDate = new SimpleDateFormat("yyyyMMdd").format(new Date());
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(new Date());
                        calendar.add(Calendar.DATE, -1 * Utils.getIntSetting(mContext, Global.PREF_DOC_ARCHIVE_HISTORY_DAYS));
                        String strStartDate = new SimpleDateFormat("yyyyMMdd").format(calendar.getTime());
                        ConnectApiClient.getMainApiClient(strBaseUrl, headers).getDocumentArchiveList(Global.gCurCustomerInfo.nCustNo, strStartDate, strEndDate).enqueue(new Callback<List<ArchiveInfo>>() {
                            @Override
                            public void onResponse(Call<List<ArchiveInfo>> call, Response<List<ArchiveInfo>> response) {
                                if(response.isSuccessful() && response.body() != null && response.body().size() > 0)
                                {
                                    mArchiveInfoList.clear();
                                    mArchiveInfoList.addAll(response.body());
                                    Collections.sort(mArchiveInfoList, new Comparator<ArchiveInfo>() {
                                        @Override
                                        public int compare(ArchiveInfo o1, ArchiveInfo o2) {
                                            return o2.date.compareTo(o1.date);
                                        }
                                    });
                                    hideProgressDialog();
                                    refreshPage();
                                }
                                else
                                {
                                    hideProgressDialog();
                                    if(!mContext.isFinishing() && !mContext.isDestroyed())
                                    {
                                        String strMesssage = Str.CouldNotConnect[Global.LANG];
                                        if (response.isSuccessful() && response.body() != null && response.body().size() == 0) {
                                            strMesssage = "No previous invoices";
                                        }

                                        try {
                                            Toast.makeText(mContext, strMesssage, Toast.LENGTH_LONG).show();
                                        }catch (Exception e1)
                                        {
                                            e1.printStackTrace();
                                        }
                                    }
                                    mContext.onBackPressed();
                                }
                            }

                            @Override
                            public void onFailure(Call<List<ArchiveInfo>> call, Throwable t) {
                                hideProgressDialog();
                                if(!mContext.isFinishing() && !mContext.isDestroyed())
                                {
                                    try {
                                        Toast.makeText(mContext, Str.CouldNotConnect[Global.LANG], Toast.LENGTH_LONG).show();
                                    }catch (Exception e1)
                                    {
                                        e1.printStackTrace();
                                    }
                                }
                                mContext.onBackPressed();
                            }
                        });
                    }
                    else {
                        hideProgressDialog();
                        if(!mContext.isFinishing() && !mContext.isDestroyed())
                        {
                            try {
                                Toast.makeText(mContext, Str.CouldNotConnect[Global.LANG], Toast.LENGTH_LONG).show();
                            }catch (Exception e1)
                            {
                                e1.printStackTrace();
                            }
                        }
                        mContext.onBackPressed();
                    }
                }

                @Override
                public void onFailure(Call<LoginInfo> call, Throwable t) {
                    hideProgressDialog();
                    if(!mContext.isFinishing() && !mContext.isDestroyed())
                    {
                        try {
                            Toast.makeText(mContext, Str.CouldNotConnect[Global.LANG], Toast.LENGTH_LONG).show();
                        }catch (Exception e1)
                        {
                            e1.printStackTrace();
                        }
                    }
                    mContext.onBackPressed();
                }
            });
        }
        else
        {
            mContext.onBackPressed();
        }
    }

    @Override
    public void refreshPage() {
        if(mArchiveInfoList.size() == 0)
        {
            mRootView.findViewById(R.id.txtNoInvoice).setVisibility(View.VISIBLE);
            mArchiveListView.setVisibility(View.GONE);
        }
        else
        {
            mRootView.findViewById(R.id.txtNoInvoice).setVisibility(View.GONE);
            mArchiveListView.setVisibility(View.VISIBLE);
            mArchiveAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void refreshLanguage() {
        ((TextView)(mRootView.findViewById(R.id.lblCustomerName))).setText(Global.gCurCustomerInfo.strName);
        ((TextView)(mRootView.findViewById(R.id.lblTitle))).setText(Str.PreviousDocuments[Global.LANG]);
        ((TextView)(mRootView.findViewById(R.id.lblInvoiceNo))).setText(Str.InvoiceNo[Global.LANG]);
        ((TextView)(mRootView.findViewById(R.id.lblInvoiceDate))).setText(Str.Date[Global.LANG]);
        ((TextView)(mRootView.findViewById(R.id.txtNoInvoice))).setText(Str.NoInvoiceMsg[Global.LANG]);
    }

    @Override
    public void receiveBroadcast(String strAction, Intent intent) {

    }


    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.btnBack:
                mContext.onBackPressed();
                break;
        }
    }

    public void doViewInvoice(ArchiveInfo info)
    {
        Utils.showPdf(mContext, info);
    }

    public void doPrintInvoice(ArchiveInfo info)
    {
        Utils.printPdf(mContext, info);
    }

    public void doEmailInvoice(ArchiveInfo info)
    {
        Utils.emailPdf(mContext, info);
    }

    public class ListAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return mArchiveInfoList.size();
        }

        @Override
        public ArchiveInfo getItem(int position) {
            return mArchiveInfoList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null)
            {
                convertView = getLayoutInflater().inflate(R.layout.previous_documents_item, parent, false);
            }
            ArchiveInfo info = getItem(position);
            ((TextView)convertView.findViewById(R.id.txtInvoiceNo)).setText(info.docNo);
            ((TextView)convertView.findViewById(R.id.txtInvoiceDate)).setText(Utils.convertTimeFormat(Utils.getDateFromDocumentArchiveData(info.date), "yyyy-MM-dd", "dd-MM-yyyy"));
            convertView.findViewById(R.id.btnView).setOnClickListener(v -> doViewInvoice(info));
            convertView.findViewById(R.id.btnPrint).setOnClickListener(v -> doPrintInvoice(info));
            convertView.findViewById(R.id.btnEmail).setOnClickListener(v -> doEmailInvoice(info));
            return convertView;
        }
    }
}