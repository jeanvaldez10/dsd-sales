package dsd.dsdvansales.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jaiselrahman.filepicker.activity.FilePickerActivity;
import com.jaiselrahman.filepicker.config.Configurations;
import com.jaiselrahman.filepicker.model.MediaFile;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import dsd.dsdvansales.R;
import dsd.dsdvansales.db.DbAdapter;
import dsd.dsdvansales.dialogs.MessageDialog;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.model.CustomerNoteInfo;
import dsd.dsdvansales.model.DescTypeInfo;
import dsd.dsdvansales.model.TransactionInfo;
import dsd.dsdvansales.model.UAttachmentInfo;
import dsd.dsdvansales.model.UCustomerNoteInfo;
import dsd.dsdvansales.views.AlertSingleChoiceAdapter;

public class NewCustomerNoteFragment extends BaseFragment implements View.OnClickListener {

    static final int OPEN_MEDIA_PICKER = 1;  // Request code
    private List<DescTypeInfo> mTypeInfoList = new ArrayList<>();
    private int mSelTypeInfoIdx = -1;
    private List<String> mAttachFiles = new ArrayList<>();
    private EditText mEditMessage;
    public NewCustomerNoteFragment() {
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
        mRootView = inflater.inflate(R.layout.fragment_new_customer_note, container, false);
        commonInit();
        return mRootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        Global.gFragIdx = Global.FRAG_NEW_CUSTOMER_DIARY;
    }

    @Override
    public void initView() {
        mRootView.findViewById(R.id.btnDone).setOnClickListener(this);
        mRootView.findViewById(R.id.btnBack).setOnClickListener(this);
        mRootView.findViewById(R.id.txtNoteType).setOnClickListener(this);
        mRootView.findViewById(R.id.txtAttachment).setOnClickListener(this);
        mEditMessage = (EditText)mRootView.findViewById(R.id.editMessage);

        loadData();
        refreshLanguage();
    }

    @Override
    public void loadData() {
        DbAdapter dbAdapter = new DbAdapter(mContext);
        dbAdapter.open();
        List<DescTypeInfo> nodeTypeInfoList = dbAdapter.getDescTypeList("NoteType");
        //mTypeInfoList = dbAdapter.getDescTypeList("NoteType");

        //Remove the NoteType = 92 & 99
        mTypeInfoList.clear();
        for(DescTypeInfo info : nodeTypeInfoList)
        {
            if(info.nNumericKey != 92 && info.nNumericKey != 99)
            {
                mTypeInfoList.add(info);
            }
        }
        dbAdapter.close();
    }

    @Override
    public void refreshPage() {

    }

    @Override
    public void refreshLanguage() {
        ((TextView) mRootView.findViewById(R.id.lblTitle)).setText(Str.CustomerDiary[Global.LANG].toUpperCase());
        ((TextView) mRootView.findViewById(R.id.txtCustomerName)).setText(Global.gCurCustomerInfo.strName);
        ((Button) mRootView.findViewById(R.id.btnDone)).setText(Str.Done[Global.LANG]);
        loadData();
    }

    @Override
    public void receiveBroadcast(String strAction, Intent intent) {

    }

    private void doDone()
    {
        if(mSelTypeInfoIdx == -1)
        {
            if(!mContext.isFinishing() && !mContext.isDestroyed())
            {
                try {
                    Toast.makeText(mContext, Str.SelectNoteType[Global.LANG], Toast.LENGTH_LONG).show();
                }catch (Exception e1)
                {
                    e1.printStackTrace();
                }
            }
            return;
        }

        //Set Upload Data to the files
        long nTrxnNo = setGeneralUploadData("NOTE", Global.gCurCustomerInfo.nCustNo, Global.gCurCustomerInfo.nChainNo, -1);

        Date date = new Date(m_lTrxnNo);
        String strMessage = mEditMessage.getText().toString();

        CustomerNoteInfo info = new CustomerNoteInfo();
        info.nCustNo = Global.gCurCustomerInfo.nCustNo;
        info.nChainNo = Global.gCurCustomerInfo.nChainNo;
        info.nAttachment = mAttachFiles.size();
        info.strNoteTime = new SimpleDateFormat("kkmmss").format(date);

        info.strCreatedTimeAndNoteDate = new SimpleDateFormat("yyyyMMddkkmmssSSS").format(date);
        info.strNote = strMessage;
        info.strNoteId = "-1";
        info.strNoteDate = new SimpleDateFormat("yyyyMMdd").format(date);
        info.strNoteType = String.valueOf(mTypeInfoList.get(mSelTypeInfoIdx).nNumericKey);
        if(info.strNoteType.equals("99"))
        {
            info.strCreatedBy = Utils.getStringSetting(mContext, Global.PREF_USER_NAME);
        }
        else
        {
            info.strCreatedBy = Utils.getStringSetting(mContext, Global.PREF_USER_DESCRIPTION);
        }

        TransactionInfo transactionInfo = new TransactionInfo();
        transactionInfo.strType = "NOTE";
        transactionInfo.strCustomerName = Global.gCurCustomerInfo.strName;
        transactionInfo.strTime = new SimpleDateFormat("kk:mm").format(new Date(nTrxnNo));
        DbAdapter dbAdapter = new DbAdapter(mContext);
        dbAdapter.open();
        dbAdapter.insertTransactionHistoryInfo(transactionInfo);
        dbAdapter.close();

        UCustomerNoteInfo uInfo = new UCustomerNoteInfo();
        uInfo.nTrxnNo = m_lTrxnNo;
        uInfo.strDocNo = "0";
        uInfo.strDocType = "NOTE";
        uInfo.strTrxnDate = info.strNoteDate;
        uInfo.strTrxnTime = new SimpleDateFormat("kkmmss").format(date);
        uInfo.nChainNo = info.nChainNo;
        uInfo.nCustNo = info.nCustNo;
        uInfo.strSalesDate = info.strNoteDate;
        uInfo.strTransactionType = "A";
        //uInfo.strUser = info.strCreatedBy;
        uInfo.strUser = Utils.getStringSetting(mContext, Global.PREF_USER_NAME);
        uInfo.strNoteType = info.strNoteType;
        uInfo.strMessage = info.strNote;
        uInfo.nAttachment = info.nAttachment;
        info.strNoteId = String.valueOf(m_lTrxnNo);

        dbAdapter = new DbAdapter(mContext);
        dbAdapter.open();
        dbAdapter.insertCustomerNoteInfo(info);
        dbAdapter.insertUCustomerNoteInfo(uInfo);
        for(int i = 0; i < uInfo.nAttachment; i++)
        {
            UAttachmentInfo uAttachment = new UAttachmentInfo();
            uAttachment.nTrxnNo = m_lTrxnNo;
            uAttachment.strFilePath = mAttachFiles.get(i);
            uAttachment.strFileName = new File(mAttachFiles.get(i)).getName();
            dbAdapter.insertUAttachmentInfo(uAttachment);
        }
        dbAdapter.close();
        //Global.gUploadCustNoList.add(makeZipFileInfo);
        Utils.registerUploadFile(mContext, makeZipFileInfo);
        mContext.onBackPressed();
    }

    private void doAttachment()
    {
        /*DialogProperties properties = new DialogProperties();
        properties.selection_mode = DialogConfigs.MULTI_MODE;
        properties.selection_type = DialogConfigs.FILE_SELECT;
        properties.root = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath());
        properties.error_dir = new File(DialogConfigs.DEFAULT_DIR);
        properties.offset = new File(DialogConfigs.DEFAULT_DIR);
        properties.extensions = new String[]{"jpg", "jpeg", "png", "bmp", "pdf"};

        FilePickerDialog dialog = new FilePickerDialog(mContext, properties, R.style.DialogTheme );
        dialog.setTitle("Select Files");
        dialog.setDialogSelectionListener(new DialogSelectionListener() {
            @Override
            public void onSelectedFilePaths(String[] files) {
                mAttachFiles = Arrays.asList(files);
                String strContent = "";
                for(String path : mAttachFiles)
                {
                    if(TextUtils.isEmpty(strContent))
                    {
                        strContent = new File(path).getName();
                    }
                    else
                    {
                        strContent +=", " + new File(path).getName();
                    }

                }
                ((TextView)mRootView.findViewById(R.id.txtAttachment)).setText(strContent);
            }
        });
        dialog.show();*/

        Intent intent = new Intent(mContext, FilePickerActivity.class);
        intent.putExtra(FilePickerActivity.CONFIGS, new Configurations.Builder()
                .setCheckPermission(true)
                .setShowImages(true)
                .enableImageCapture(true)
                .setMaxSelection(10)
                .setSkipZeroSizeFiles(true)
                .build());
        startActivityForResult(intent, OPEN_MEDIA_PICKER);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == OPEN_MEDIA_PICKER && resultCode == Activity.RESULT_OK)
        {
            ArrayList<MediaFile> files = data.getParcelableArrayListExtra(FilePickerActivity.MEDIA_FILES);
            for(MediaFile file : files)
            {
                mAttachFiles.add(file.getPath());
            }
            String strContent = "";
            for(String path : mAttachFiles)
            {
                if(TextUtils.isEmpty(strContent))
                {
                    strContent = new File(path).getName();
                }
                else
                {
                    strContent +=", " + new File(path).getName();
                }

            }
            ((TextView)mRootView.findViewById(R.id.txtAttachment)).setText(strContent);
        }
    }

    private void doNoteType()
    {
        if(mTypeInfoList.size() == 0)
        {
            if(!mContext.isFinishing() && !mContext.isDestroyed())
            {
                try {
                    Toast.makeText(mContext, Str.NoNoteType[Global.LANG], Toast.LENGTH_LONG).show();
                }catch (Exception e1)
                {
                    e1.printStackTrace();
                }
            }
            return;
        }

        final String[] strArray = new String[mTypeInfoList.size()];
        for(int i = 0; i < strArray.length; i++)
        {
            strArray[i] = mTypeInfoList.get(i).strDescription;
        }
        final int origIdx = mSelTypeInfoIdx;
        final String origText = ((TextView) mRootView.findViewById(R.id.txtNoteType)).getText().toString();
        try {
            new AlertDialog.Builder(mContext)
                    .setTitle(Str.Select_Note_Type[Global.LANG])
                    .setSingleChoiceItems(new AlertSingleChoiceAdapter(mContext, strArray), mSelTypeInfoIdx, new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int nSelected)
                        {
                            mSelTypeInfoIdx = nSelected;
                            ((TextView) mRootView.findViewById(R.id.txtNoteType)).setText(strArray[nSelected]);
                        }
                    })
                    .setPositiveButton(Str.Ok[Global.LANG], new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int nSelected)
                        {

                        }
                    })
                    .setNegativeButton(Str.Cancel[Global.LANG], new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int nSelected)
                        {
                            mSelTypeInfoIdx = origIdx;
                            ((TextView) mRootView.findViewById(R.id.txtNoteType)).setText(origText);
                        }
                    })
                    .create()
                    .show();
        }catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id)
        {
            case R.id.btnDone:
                doDone();
                break;
            case R.id.btnBack:
                mContext.onBackPressed();
                break;
            case R.id.txtAttachment:
                doAttachment();
                break;
            case R.id.txtNoteType:
                doNoteType();
                break;
        }
    }
}
