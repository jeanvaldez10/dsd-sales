package dsd.dsdvansales.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import dsd.dsdvansales.R;
import dsd.dsdvansales.activities.SignatureActivity;
import dsd.dsdvansales.db.DbAdapter;
import dsd.dsdvansales.dialogs.MessageDialog;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.PrintEngine;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.model.EquipAssInfo;
import dsd.dsdvansales.model.EquipmentInfo;
import dsd.dsdvansales.views.ScrollViewExt;
import dsd.dsdvansales.views.ScrollViewListener;

public class TermsOfPlacementFragment extends BaseFragment implements View.OnClickListener{


    private TextView mTxtTermsView;
    private EditText mEditAgreedByView;
    private TextView mTitleView, mAgreedByView, mCustomerNameView, mSignatureView;
    private Button mReturnBtn, mConfirmBtn;
    private ImageView mSignatureBtn;
    private ProgressDialog mProgressDialog = null;
    String mStrSignatureFilePath = "";
    private ScrollViewExt mTermsScrollView = null;
    public TermsOfPlacementFragment() {
        // Required empty public constructor
    }

    public static TermsOfPlacementFragment newInstance() {
        TermsOfPlacementFragment fragment = new TermsOfPlacementFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
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
        mRootView = inflater.inflate(R.layout.fragment_terms_of_placement, container, false);
        commonInit();
        return mRootView;
    }

    @Override
    public void initView() {

        mTitleView = (TextView)mRootView.findViewById(R.id.lblTitle);
        mAgreedByView = (TextView)mRootView.findViewById(R.id.lblAgreedBy);
        mSignatureView = (TextView)mRootView.findViewById(R.id.lblSignature);
        mTxtTermsView = (TextView)mRootView.findViewById(R.id.txtTerms);
        mCustomerNameView = (TextView)mRootView.findViewById(R.id.lblCustomerName);
        mEditAgreedByView = (EditText) mRootView.findViewById(R.id.editName);
        mReturnBtn = (Button)mRootView.findViewById(R.id.btnReturn);
        mConfirmBtn = (Button)mRootView.findViewById(R.id.btnConfirm);
        mSignatureBtn = (ImageView) mRootView.findViewById(R.id.btnSignature);
        mTermsScrollView = (ScrollViewExt) mRootView.findViewById(R.id.scroll_terms);
        mTermsScrollView.setScrollViewListener(new ScrollViewListener() {
            @Override
            public void onScrollChanged(ScrollViewExt scrollView, int x, int y, int oldx, int oldy) {
                View view = scrollView.getChildAt(scrollView.getChildCount() - 1);
                int diff = (view.getBottom() - (scrollView.getHeight() + scrollView.getScrollY()));
                // if diff is zero, then the bottom has been reached
                if (diff < 5) {
                    mEditAgreedByView.requestFocus();
                    mSignatureBtn.setVisibility(View.VISIBLE);
                    mEditAgreedByView.setVisibility(View.VISIBLE);
                    mRootView.findViewById(R.id.editSignatureDisable).setVisibility(View.GONE);
                    mRootView.findViewById(R.id.editNameDisable).setVisibility(View.GONE);
                }
                else
                {
                    mSignatureBtn.setVisibility(View.GONE);
                    mEditAgreedByView.setVisibility(View.GONE);
                    mRootView.findViewById(R.id.editSignatureDisable).setVisibility(View.VISIBLE);
                    mRootView.findViewById(R.id.editNameDisable).setVisibility(View.VISIBLE);
                }
            }
        });
        Drawable editDrawable = mRootView.findViewById(R.id.editSignature).getBackground();
        mSignatureBtn.setImageDrawable(editDrawable);
        mRootView.findViewById(R.id.editSignature).setVisibility(View.GONE);
        mReturnBtn.setOnClickListener(this);
        mConfirmBtn.setOnClickListener(this);
        mSignatureBtn.setOnClickListener(this);
        mEditAgreedByView.requestFocus();

        ViewTreeObserver viewTreeObserver = mTxtTermsView.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mTxtTermsView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    View view = mTermsScrollView.getChildAt(mTermsScrollView.getChildCount() - 1);
                    int diff = (view.getBottom() - (mTermsScrollView.getHeight() + mTermsScrollView.getScrollY()));
                    // if diff is zero, then the bottom has been reached
                    if (diff < 5) {
                        mEditAgreedByView.requestFocus();
                        mSignatureBtn.setVisibility(View.VISIBLE);
                        mEditAgreedByView.setVisibility(View.VISIBLE);
                        mRootView.findViewById(R.id.editSignatureDisable).setVisibility(View.GONE);
                        mRootView.findViewById(R.id.editNameDisable).setVisibility(View.GONE);
                    }
                    else
                    {
                        mSignatureBtn.setVisibility(View.GONE);
                        mEditAgreedByView.setVisibility(View.GONE);
                        mRootView.findViewById(R.id.editSignatureDisable).setVisibility(View.VISIBLE);
                        mRootView.findViewById(R.id.editNameDisable).setVisibility(View.VISIBLE);
                    }
                }
            });
        }

        loadData();
        refreshLanguage();
    }

    @Override
    public void loadData() {
        if( TextUtils.isEmpty(Global.TERMS_TEXT))
        {
            Global.TERMS_TEXT = Utils.getTermsText();
        }
        refreshPage();

    }

    @Override
    public void refreshPage() {
        String strBodyText = Global.TERMS_TEXT;
        mTxtTermsView.setText(strBodyText);
        mCustomerNameView.setText(Global.gCurCustomerInfo.strName);
    }

    @Override
    public void refreshLanguage() {
        mTitleView.setText(Str.TermsOfPlacement[Global.LANG]);
        mAgreedByView.setText(Str.AgreedBy[Global.LANG]);
        mReturnBtn.setText(Str.Return[Global.LANG]);
        mConfirmBtn.setText(Str.CollectDeposit[Global.LANG]);
        mSignatureView.setText(Str.Signature[Global.LANG]);
    }

    @Override
    public void receiveBroadcast(String strAction, Intent intent) {
        if (strAction.equals(Global.ACTION_CAPTURE_SIGNATURE) == true) {
            mStrSignatureFilePath = intent.getStringExtra(Global.EXTRA_SIGNATURE_PATH);
            Utils.printDebug(mContext, Str.Saved_successfully[Global.LANG]);
        }
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
                if( mEditAgreedByView.getVisibility() == View.GONE )
                {
                    try {
                        final MessageDialog msgDialog = new MessageDialog(mContext);
                        msgDialog.setCancelable(false);
                        msgDialog.mStrMessage = Str.Please_read_our_temrm[Global.LANG];
                        msgDialog.mStrRight = Str.Ok[Global.LANG];
                        msgDialog.show();
                    }catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    return;
                }
                if(TextUtils.isEmpty(mEditAgreedByView.getText()))
                {
                    try {
                        final MessageDialog msgDialog = new MessageDialog(mContext);
                        msgDialog.setCancelable(false);
                        msgDialog.mStrMessage = Str.PleaseInputAgreedField[Global.LANG];
                        msgDialog.mStrRight = Str.Ok[Global.LANG];
                        msgDialog.show();
                    }catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    return;
                }
                if(TextUtils.isEmpty(mStrSignatureFilePath))
                {
                    try {
                        final MessageDialog msgDialog = new MessageDialog(mContext);
                        msgDialog.setCancelable(false);
                        msgDialog.mStrMessage = Str.Please_Sign_your_signature[Global.LANG];
                        msgDialog.mStrRight = Str.Ok[Global.LANG];
                        msgDialog.show();
                    }catch (Exception e)
                    {
                        e.printStackTrace();
                    }

                    return;
                }
                doMakePdf();
                break;
            case R.id.btnSignature:
                Intent intent = new Intent(mContext, SignatureActivity.class);
                intent.putExtra(Global.EXTRA_SIGNATURE_PATH, Global.DEFAULT_SIGNATURE_FILEPATH);
                startActivity(intent);
                mContext.overridePendingTransition(R.anim.zoom_in, 0);
                break;
        }
    }

    private void doMakePdf()
    {
        loadingStart(Str.CreatingPdf[Global.LANG]);
        new Thread()
        {
            @Override
            public void run() {
                PrintEngine printEngine = new PrintEngine();

                String strFileName = getPDFFileName();
                String strPath = Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + strFileName;

                File file = new File(mStrSignatureFilePath);
                printEngine.PrepareTermsPrint(mContext, file, mEditAgreedByView.getText().toString());
                printEngine.createDocument(mContext, false, strPath, Global.TERMS_PRINT);

                Global.gCurEquipmentInfo.strNewPlacementDocPath = strPath;
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        loadingFinished();
                        processConfirm();

                    }
                });
            }
        }.start();
    }

    private String makeEquipNumber()
    {
        String strEquipNumber = "9";
        if(Global.gRouteNumber.length() == 0) strEquipNumber += "000";
        else if(Global.gRouteNumber.length() == 1) strEquipNumber += "00" + Global.gRouteNumber;
        else if(Global.gRouteNumber.length() == 2) strEquipNumber += "0" + Global.gRouteNumber;
        else
        {
            strEquipNumber += Global.gRouteNumber.substring(Global.gRouteNumber.length() - 3, Global.gRouteNumber.length());
        }

        String strY = new SimpleDateFormat("yyyy").format(new Date());
        Calendar cal = Calendar.getInstance();
        int nDayOfYear = cal.get(Calendar.DAY_OF_YEAR);
        String strJJJ = String.format(Locale.ENGLISH, "%d", nDayOfYear);
        int nPdfSequenceNo = Utils.getIntSetting(mContext, String.format(Locale.ENGLISH, Global.PREF_PDF_SEQUENCE_NO, Global.gTripNumber)) + 1;
        Utils.setIntSetting(mContext, String.format(Locale.ENGLISH, Global.PREF_PDF_SEQUENCE_NO, Global.gTripNumber), nPdfSequenceNo + 1);
        String strSSS = String.format(Locale.ENGLISH, "%d", nPdfSequenceNo);

        strEquipNumber += strY.substring(3, 4);
        if(strJJJ.length() == 0 ) strEquipNumber += "000";
        else if(strJJJ.length() == 1) strEquipNumber += "00" + strJJJ;
        else if(strJJJ.length() == 2) strEquipNumber += "0" + strJJJ;
        else strEquipNumber += strJJJ.substring(strJJJ.length() - 3, strJJJ.length());

        if(strSSS.length() == 0) strEquipNumber += "00";
        else if(strSSS.length() == 1) strEquipNumber += "0" + strSSS;
        else strEquipNumber += strSSS.substring(strSSS.length() - 2, strSSS.length());

        return strEquipNumber;
    }

    private void processConfirm()
    {
        EquipmentInfo mEquipmentInfo = Global.gCurEquipmentInfo;
        String strOldEquipNo = mEquipmentInfo.strEquipmentNo;
        String strEquipNo = makeEquipNumber();
        DbAdapter dbAdapter = new DbAdapter(mContext);
        dbAdapter.open();
        mEquipmentInfo.strEquipmentNo = strEquipNo;
        mEquipmentInfo.strEquipmentActType = "N";
        dbAdapter.insertEquipment(mEquipmentInfo);
        EquipAssInfo equipAssInfo = new EquipAssInfo();
        equipAssInfo.nChainNo = Global.gCurCustomerInfo.nChainNo;
        equipAssInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
        equipAssInfo.strEquipmentNo = strEquipNo;
        equipAssInfo.strOldEquipmentNo = strOldEquipNo;
        dbAdapter.insertEquipAss(equipAssInfo);
        dbAdapter.close();
        mContext.onBackPressed();
        mContext.onBackPressed();

        // Comment for ignoring Collect Payment Processing.
        /*CollectDepositFragment fragment = CollectDepositFragment.newInstance();
        mContext.replaceFragment(fragment);*/
    }

    private void loadingStart(String strText)
    {
        try {
            if( mProgressDialog == null )
            {
                mProgressDialog = new ProgressDialog(mContext);
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
            }
            mProgressDialog.setMessage(strText);
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void loadingFinished()
    {
        try {
            if( mProgressDialog != null )
            {
                mProgressDialog.dismiss();
                mProgressDialog = null;
            }
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }


}
