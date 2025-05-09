package dsd.dsdvansales.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import dsd.dsdvansales.R;
import dsd.dsdvansales.db.DbAdapter;
import dsd.dsdvansales.dialogs.MessageDialog;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.model.EquipmentInfo;

public class SelectModelFragment extends BaseFragment implements View.OnClickListener{

    private static int ADD_EXISTING = 0;
    private static int NEW_REPLACEMENT = 1;

    public int mAction = ADD_EXISTING;

    private TextView mTitleView;
    private TextView mCustomerNameView;
    private TextView mNoteView;
    private ListView mModelListView;
    private Button mReturnBtn;
    private Button mConfirmBtn;
    private EquipmentInfo mCurSelInfo = null;

    private List<EquipmentInfo> mListEquipment = new ArrayList<>();
    private ListAdapter mListAdapter;

    // TODO: Rename and change types and number of parameters
    public static SelectModelFragment newInstance(int nAction) {
        SelectModelFragment fragment = new SelectModelFragment();
        Bundle args = new Bundle();
        args.putInt("Action", nAction);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mAction = getArguments().getInt("Action");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mRootView = inflater.inflate(R.layout.fragment_select_model, container, false);
        commonInit();
        return mRootView;
    }


    @Override
    public void initView() {
        mTitleView = (TextView)mRootView.findViewById(R.id.lblTitle);
        mNoteView = (TextView)mRootView.findViewById(R.id.lblNote);
        mCustomerNameView = (TextView)mRootView.findViewById(R.id.lblCustomerName);
        mModelListView = (ListView)mRootView.findViewById(R.id.modelList);
        mListAdapter = new ListAdapter();
        mModelListView.setAdapter(mListAdapter);
        mModelListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mCurSelInfo = mListEquipment.get(i);
                refreshPage();
            }
        });
        mReturnBtn = (Button)mRootView.findViewById(R.id.btnReturn);
        mConfirmBtn = (Button)mRootView.findViewById(R.id.btnConfirm);
        mReturnBtn.setOnClickListener(this);
        mConfirmBtn.setOnClickListener(this);
        loadData();
        refreshLanguage();
    }

    @Override
    public void loadData() {
        mListEquipment.clear();
        DbAdapter dbAdapter = new DbAdapter(mContext);
        dbAdapter.open();
        List<EquipmentInfo> equipmentList = dbAdapter.getEquipmentModel();
        dbAdapter.close();
        mListEquipment.addAll(equipmentList);
        refreshPage();
    }

    @Override
    public void refreshPage() {
        mCustomerNameView.setText(Global.gCurCustomerInfo.strName);
        mListAdapter.notifyDataSetChanged();
    }

    @Override
    public void refreshLanguage() {
        mTitleView.setText(Str.Select_Model[Global.LANG]);
        mReturnBtn.setText(Str.Return[Global.LANG]);
        if(mAction == ADD_EXISTING)
        {
            mNoteView.setText(Str.Select_Model_to_Add[Global.LANG]);
            mConfirmBtn.setText(Str.Details[Global.LANG]);
        }
        else
        {
            mNoteView.setText(Str.Select_Preferred_Model[Global.LANG]);
            mConfirmBtn.setText(Str.Terms[Global.LANG]);
        }

    }

    @Override
    public void receiveBroadcast(String strAction, Intent intent) {

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id)
        {
            case R.id.btnReturn:
                mContext.onBackPressed();
                break;
            case R.id.btnConfirm:
                if(mAction == ADD_EXISTING)
                {
                    if( mCurSelInfo == null )
                    {
                        try {
                            String strMessage = Str.Please_Select_Model[Global.LANG] + "\n";
                            final MessageDialog msgDialog = new MessageDialog(mContext);
                            msgDialog.setCancelable(false);
                            msgDialog.mStrMessage = strMessage;
                            msgDialog.mStrRight = Str.Ok[Global.LANG];
                            msgDialog.show();
                        }catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                    else
                    {
                        Global.gCurEquipmentInfo = mCurSelInfo;
                        AddEquipmentFragment fragment = AddEquipmentFragment.newInstance(1);
                        mContext.replaceFragment(fragment);
                    }

                }
                else if( mAction == NEW_REPLACEMENT )
                {
                    if( mCurSelInfo == null )
                    {
                        try {
                            String strMessage = Str.Please_Select_Model[Global.LANG] + "\n";
                            final MessageDialog msgDialog = new MessageDialog(mContext);
                            msgDialog.setCancelable(false);
                            msgDialog.mStrMessage = strMessage;
                            msgDialog.mStrRight = Str.Ok[Global.LANG];
                            msgDialog.show();
                        }catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                    else
                    {
                        Global.gCurEquipmentInfo = mCurSelInfo;
                        TermsOfPlacementFragment fragment = TermsOfPlacementFragment.newInstance();
                        mContext.replaceFragment(fragment);
                    }
                }

                break;
        }
    }

    private class ViewHolder
    {
        public ImageView mImageView;
        public TextView mContentView;
        public RadioButton mSelectBtn;
    }

    private class ListAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return mListEquipment.size();
        }

        @Override
        public Object getItem(int i) {
            if(i < mListEquipment.size())
            {
                return mListEquipment.get(i);
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
                view = mContext.getLayoutInflater().inflate(R.layout.row_model, viewGroup, false);
                holder = new ViewHolder();
                holder.mImageView = (ImageView)view.findViewById(R.id.imgItem);
                holder.mContentView = (TextView)view.findViewById(R.id.txtContent);
                holder.mSelectBtn = (RadioButton)view.findViewById(R.id.radioSelect);
                view.setTag(holder);
            }
            else
            {
                holder = (ViewHolder)view.getTag();
            }

            final EquipmentInfo info = mListEquipment.get(i);
            File file = new File(Global.ROOT_EQUIPMENT_CATALOG_LOCATION + "/" + info.strModel + ".jpg");
            Picasso.get().load(file).placeholder(mContext.getResources().getDrawable(R.drawable.no_image)).into(holder.mImageView);

            String strContent = info.strDescription + "\n" + info.strMake;
            holder.mContentView.setText(strContent);
            holder.mSelectBtn.setChecked(mCurSelInfo == info);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mCurSelInfo = info;
                    refreshPage();
                }
            });
            holder.mSelectBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mCurSelInfo = info;
                    refreshPage();
                }
            });
            return view;
        }
    }

}
