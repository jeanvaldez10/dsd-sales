package dsd.dsdvansales.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

import dsd.dsdvansales.R;
import dsd.dsdvansales.chat.ChatHelper;
import dsd.dsdvansales.db.DbAdapter;
import dsd.dsdvansales.dialogs.InputDialog;
import dsd.dsdvansales.dialogs.MessageDialog;
import dsd.dsdvansales.dialogs.WindcaveStationIDDialog;
import dsd.dsdvansales.fragment.BaseFragment;
import dsd.dsdvansales.fragment.ConfigurationFragment;
import dsd.dsdvansales.fragment.CustomerDiaryFragment;
import dsd.dsdvansales.fragment.EndingInventoryFragment;
import dsd.dsdvansales.fragment.EndingInventorySummaryFragment;
import dsd.dsdvansales.fragment.LoginFragment;
import dsd.dsdvansales.fragment.PreTripTasksFragment;
import dsd.dsdvansales.fragment.PrintDocumentsFragment;
import dsd.dsdvansales.fragment.SalesTrackerFragment;
import dsd.dsdvansales.fragment.SelectCustomerFragment;
import dsd.dsdvansales.fragment.SpoilEntryFragment;
import dsd.dsdvansales.fragment.TripSettlementFragment;
import dsd.dsdvansales.fragment.VehicleInventoryFragment;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.views.AlertSingleChoiceAdapter;

public class LoginActivity extends FragmentActivity implements View.OnClickListener, Animation.AnimationListener {

    private ProgressDialog mProgressDlg = null;
    private LinearLayout mLayoutMenu1 = null;
    private LinearLayout mLayoutMenu2 = null;
    private float mTouchY1_start, mTouchY1_end;
    private float mTouchY2_start, mTouchY2_end;
    private final int MIN_DISTANCE = 50;

    private boolean mIsAnimation = false;
    private boolean mShowMenu1 = false;
    private boolean mShowMenu2 = false;

    public void showProgressDialog(String strMessage)
    {
        try {
            if(mProgressDlg == null)
            {
                mProgressDlg = new ProgressDialog(this);
                mProgressDlg.setCancelable(false);
                mProgressDlg.show();
            }
            mProgressDlg.setMessage(strMessage);
        }catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    public void hideProgressDialog(){
        try {
            if(mProgressDlg != null)
            {
                mProgressDlg.dismiss();
                mProgressDlg = null;
            }
        }catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);
        initView();

        replaceFragment(new LoginFragment());
    }

    private void initView() {
        findViewById(R.id.imgMenu1).setOnClickListener(this);
        findViewById(R.id.imgMenu2).setOnClickListener(this);
        findViewById(R.id.layoutMenu1).setOnClickListener(this);
        findViewById(R.id.layoutMenu2).setOnClickListener(this);
        findViewById(R.id.mark_logo).setOnClickListener(this);
        findViewById(R.id.txtEndJourney).setOnClickListener(this);
        findViewById(R.id.txtLogout).setOnClickListener(this);
        findViewById(R.id.txtChangeDriver).setOnClickListener(this);
        findViewById(R.id.txtChangePrinterType).setOnClickListener(this);
        findViewById(R.id.txtMultiLang).setOnClickListener(this);
        findViewById(R.id.txtVehicleInventory).setOnClickListener(this);
        findViewById(R.id.txtHelp).setOnClickListener(this);
        findViewById(R.id.txtAbout).setOnClickListener(this);
        findViewById(R.id.txtMakeDeviceDiscoverable).setOnClickListener(this);
        findViewById(R.id.txtCommunication).setOnClickListener(this);
        findViewById(R.id.txtPrintDocuments).setOnClickListener(this);
        findViewById(R.id.txtSalesTracker).setOnClickListener(this);
        findViewById(R.id.txtDiary).setOnClickListener(this);
        findViewById(R.id.txtWindcaveReader).setOnClickListener(this);
        findViewById(R.id.txtDocuments).setOnClickListener(this);
        findViewById(R.id.imgDocuments).setOnClickListener(this);
        findViewById(R.id.txtSetInvoiceEnding).setOnClickListener(this);
        findViewById(R.id.txtDriverBreak).setOnClickListener(this);

        mLayoutMenu1 = ((LinearLayout) findViewById(R.id.layoutMenu1));
        mLayoutMenu2 = ((LinearLayout) findViewById(R.id.layoutMenu2));

        findViewById(R.id.layoutMenu1).setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mTouchY1_start = event.getY();
                    break;
                case MotionEvent.ACTION_UP:
                    mTouchY1_end = event.getY();
                    float deltaY = mTouchY1_start - mTouchY1_end;
                    if (deltaY > MIN_DISTANCE) {
                        startMenu1Animation();
                    }
                    break;
            }
            return false;
        });

        findViewById(R.id.layoutMenu2).setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mTouchY2_start = event.getY();
                    break;
                case MotionEvent.ACTION_UP:
                    mTouchY2_end = event.getY();
                    float deltaY = mTouchY2_start - mTouchY2_end;
                    if (deltaY > MIN_DISTANCE) {
                        startMenu2Animation();
                    }
                    break;
            }
            return false;
        });

    }

    private void startMenu1Animation() {

        try {
            if(BluetoothAdapter.getDefaultAdapter() == null)
            {
                findViewById(R.id.txtMakeDeviceDiscoverable).setEnabled(false);
            }
            else if(BluetoothAdapter.getDefaultAdapter().isEnabled() && BluetoothAdapter.getDefaultAdapter().getScanMode() == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE)
            {
                findViewById(R.id.txtMakeDeviceDiscoverable).setEnabled(false);
            }
            else
            {
                findViewById(R.id.txtMakeDeviceDiscoverable).setEnabled(true);
            }
        }catch (Exception e)
        {
            e.printStackTrace();
            findViewById(R.id.txtMakeDeviceDiscoverable).setEnabled(false);
        }

        if (mIsAnimation == true) {
            return;
        }
        if (mShowMenu2 == true) {
            mShowMenu2 = false;
            mLayoutMenu2.setVisibility(View.GONE);
        }
        mIsAnimation = true;
        Animation anim;
        if (mShowMenu1 == false) {
            refreshMenuLanguage();
            mLayoutMenu1.setVisibility(View.VISIBLE);
            anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.top_to_down);
        } else {
            anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.bottom_to_up);
        }

        anim.setAnimationListener(this);
        mLayoutMenu1.startAnimation(anim);
        mShowMenu1 = !mShowMenu1;
    }

    private void startMenu2Animation() {
        if(Utils.getBooleanSetting(this, Global.PREF_LOGIN_AS_SUPER_VISOR))
        {
            findViewById(R.id.txtPrintDocuments).setVisibility(View.GONE);
            findViewById(R.id.txtVehicleInventory).setVisibility(View.GONE);
            findViewById(R.id.txtPrintDocuments).setVisibility(View.GONE);
            findViewById(R.id.txtSalesTracker).setVisibility(View.GONE);
            findViewById(R.id.txtDiary).setVisibility(View.GONE);
            findViewById(R.id.txtChangeDriver).setVisibility(View.GONE);
            findViewById(R.id.txtChangePrinterType).setVisibility(View.GONE);
            findViewById(R.id.txtWindcaveReader).setVisibility(View.GONE);
            findViewById(R.id.txtDriverBreak).setVisibility(View.GONE);
        }
        else
        {
            findViewById(R.id.txtPrintDocuments).setVisibility(View.VISIBLE);
            findViewById(R.id.txtVehicleInventory).setVisibility(View.VISIBLE);
            findViewById(R.id.txtPrintDocuments).setVisibility(View.VISIBLE);
            findViewById(R.id.txtSalesTracker).setVisibility(View.VISIBLE);
            findViewById(R.id.txtDiary).setVisibility(View.VISIBLE);
            findViewById(R.id.txtChangeDriver).setVisibility(View.VISIBLE);
            findViewById(R.id.txtChangePrinterType).setVisibility(View.VISIBLE);
            findViewById(R.id.txtWindcaveReader).setVisibility(View.GONE);

            findViewById(R.id.txtDriverBreak).setVisibility(View.GONE);

            findViewById(R.id.txtPrintDocuments).setEnabled(false);
            if(Global.gCurCustomerInfo != null && Global.gCurCustomerInfo.nCustNo != 0 && Global.gEnteredEndTrip == false) {
                findViewById(R.id.txtDiary).setEnabled(true);
            } else {
                findViewById(R.id.txtDiary).setEnabled(false);
            }
            findViewById(R.id.txtSalesTracker).setEnabled(false);
            findViewById(R.id.txtChangeDriver).setEnabled(false);
            findViewById(R.id.txtChangePrinterType).setEnabled(false);
        }

        findViewById(R.id.txtDocuments).setVisibility(View.GONE);
        findViewById(R.id.imgDocuments).setVisibility(View.GONE);

        if(Utils.getBooleanSetting(this, Global.PREF_DEBUG))
        {
            findViewById(R.id.txtSetInvoiceEnding).setVisibility(View.VISIBLE);
        }
        else
        {
            findViewById(R.id.txtSetInvoiceEnding).setVisibility(View.GONE);
        }

        if (mIsAnimation == true) {
            return;
        }
        if (mShowMenu1 == true) {
            mShowMenu1 = false;
            mLayoutMenu1.setVisibility(View.GONE);
        }
        mIsAnimation = true;
        Animation anim;
        if (mShowMenu2 == false) {
            refreshMenuLanguage();
            mLayoutMenu2.setVisibility(View.VISIBLE);
            anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.top_to_down);
        } else {
            anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.bottom_to_up);
        }
        anim.setAnimationListener(this);
        mLayoutMenu2.startAnimation(anim);
        mShowMenu2 = !mShowMenu2;
    }

    @SuppressLint("ResourceType")
    private void refreshMenuLanguage() {
        if(!Utils.getBooleanSetting(this, Global.PREF_PASSED_LOAD_SECTION) && Utils.getLongSetting(this, Global.PREF_UINVEN_TRXN_NO) == 0)	// This has been applied from Wang because of Andrew Requested at 12th of Feb, 2020.
        {
            ((TextView) findViewById(R.id.txtVehicleInventory)).setTextColor(getResources().getColor(R.color.gray_color));
        } else {
            if(!Utils.getBooleanSetting(this, String.format(Locale.ENGLISH, Global.PREF_END_TRIP_CHECKED, 1)))
            {
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frameContainer);
                if( !(fragment instanceof EndingInventoryFragment) && !(fragment instanceof EndingInventorySummaryFragment)
                        && !(fragment instanceof SpoilEntryFragment))
                {
                    ((TextView) findViewById(R.id.txtVehicleInventory)).setTextColor(getResources().getColorStateList(R.drawable.clickable_textview_white));
                }
                else
                {
                    ((TextView) findViewById(R.id.txtVehicleInventory)).setTextColor(getResources().getColor(R.color.gray_color));
                }
            }
            else
            {
                ((TextView) findViewById(R.id.txtVehicleInventory)).setTextColor(getResources().getColor(R.color.gray_color));
            }
        }
        if (Global.gEnteredEndTrip == true) {
            ((TextView) findViewById(R.id.txtPrintDocuments)).setTextColor(getResources().getColor(R.color.gray_color));
        } else {
            ((TextView) findViewById(R.id.txtPrintDocuments)).setTextColor(getResources().getColorStateList(R.drawable.clickable_textview_white));
        }

        if(Global.gCurCustomerInfo != null && Global.gCurCustomerInfo.nCustNo != 0 && Global.gEnteredEndTrip == false) {
            ((TextView) findViewById(R.id.txtDiary)).setTextColor(getResources().getColorStateList(R.drawable.clickable_textview_white));
        }
        else {
            ((TextView) findViewById(R.id.txtDiary)).setTextColor(getResources().getColor(R.color.gray_color));
        }

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frameContainer);
        if(!TextUtils.isEmpty(Utils.getStringSetting(this, Global.PREF_DOCUMENTS)) && !(fragment instanceof LoginFragment))
        {
            findViewById(R.id.txtDocuments).setVisibility(View.VISIBLE);
            findViewById(R.id.imgDocuments).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.txtDocuments)).setText(Utils.getAppNameFromPackage(Utils.getStringSetting(this, Global.PREF_DOCUMENTS)));
            Drawable drawable = Utils.getAppDrawableIcon(this, Utils.getStringSetting(this, Global.PREF_DOCUMENTS));
            ((ImageView)findViewById(R.id.imgDocuments)).setImageDrawable(drawable);
        }
        else
        {
            findViewById(R.id.txtDocuments).setVisibility(View.GONE);
            findViewById(R.id.imgDocuments).setVisibility(View.GONE);
        }

        ((TextView) findViewById(R.id.txtEndJourney)).setText(Str.End_Journey[Global.LANG]);
        ((TextView) findViewById(R.id.txtLogout)).setText(Str.Logout[Global.LANG]);
        ((TextView) findViewById(R.id.txtChangeDriver)).setText(Str.ChangeDriver[Global.LANG]);
        ((TextView) findViewById(R.id.txtChangePrinterType)).setText(Str.ChangePrinterType[Global.LANG]);
        ((TextView) findViewById(R.id.txtCommunication)).setText(Str.Communication[Global.LANG]);
        ((TextView) findViewById(R.id.txtVehicleInventory)).setText(Str.VehicleInventory[Global.LANG]);
        ((TextView) findViewById(R.id.txtAbout)).setText(Str.About[Global.LANG]);
        ((TextView) findViewById(R.id.txtMakeDeviceDiscoverable)).setText(Str.MakeDeviceDiscoverable[Global.LANG]);
        ((TextView) findViewById(R.id.txtHelp)).setText(Str.Help[Global.LANG]);
        ((TextView) findViewById(R.id.txtPrintDocuments)).setText(Str.PrintDocuments[Global.LANG]);
        ((TextView) findViewById(R.id.txtSalesTracker)).setText(Str.SalesTracker[Global.LANG]);
        ((TextView) findViewById(R.id.txtDiary)).setText(Str.CustomerDiary[Global.LANG]);
        ((TextView) findViewById(R.id.txtDriverBreak)).setText(Str.DriverBreak[Global.LANG]);


        switch (Global.LANG) {
            case Global.LANG_ENGLISH:
                ((TextView) findViewById(R.id.txtMultiLang)).setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_english, 0, 0, 0);
                break;
            case Global.LANG_INDONESIAN:
                ((TextView) findViewById(R.id.txtMultiLang)).setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_indonesian, 0, 0, 0);
                break;
            case Global.LANG_THAI:
                ((TextView) findViewById(R.id.txtMultiLang)).setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_thai, 0, 0, 0);
                break;
            case Global.LANG_SPANISH:
                ((TextView) findViewById(R.id.txtMultiLang)).setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_spanish, 0, 0, 0);
                break;
            case Global.LANG_AMERICAN:
                ((TextView) findViewById(R.id.txtMultiLang)).setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_american, 0, 0, 0);
                break;
            case Global.LANG_ARABIC:
                ((TextView) findViewById(R.id.txtMultiLang)).setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_egypt, 0, 0, 0);
                break;
        }
        ((TextView) findViewById(R.id.txtMultiLang)).setText(Str.LANGUAGES[Global.LANG]);
    }

    public void replaceFragment(Fragment fragment) {
        try
        {
            if (fragment != null) {
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.frameContainer, fragment);
                ft.addToBackStack(fragment.getClass().getName());
                ft.commit();
            }
        }catch(Exception e)
        {
            e.printStackTrace();
        }

    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            startMenu2Animation();
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    public boolean m_bClicked = false;
    @Override
    public void onClick(View v) {
        if( m_bClicked ) return;
        m_bClicked = true;
//		switch (v.getId()) {
        int nId = v.getId();
        if (nId == R.id.imgMenu1) {
            startMenu1Animation();
        } else if (nId == R.id.imgMenu2) {
            startMenu2Animation();
        }else if (nId == R.id.mark_logo) {
            m_bClicked = false;
            return;
        } else if (nId == R.id.txtHelp) {
            startMenu1Animation();
        } else if (nId == R.id.txtAbout) {
            startMenu1Animation();
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.start1, R.anim.start2);
        } else if (nId == R.id.txtMakeDeviceDiscoverable) {
            startMenu1Animation();
        } else if (nId == R.id.txtCommunication) {
            if (Global.gFragIdx == Global.FRAG_CONFIGURATION) {
                startMenu1Animation();
            } else {
                mLayoutMenu1.setVisibility(View.GONE);
                mShowMenu1 = false;
                if (Utils.getIntSetting(this, Global.PREF_CONFIG_STATUS) == Global.CONF_GOT_LOADED) {
                    try {
                        final InputDialog inputDlg = new InputDialog(this);
                        inputDlg.mStrTitle = Str.PIN_CODE[Global.LANG];
                        inputDlg.mStrHint = Str.PIN_code_for_communication[Global.LANG];
                        inputDlg.mStrLeft = Str.Enter[Global.LANG];
                        inputDlg.mStrRight = Str.Return[Global.LANG];
                        inputDlg.mInputType = InputType.TYPE_CLASS_NUMBER;
                        inputDlg.setCancelable(false);
                        inputDlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                if (inputDlg.mReturnCode == Global.DLG_RETURN_LEFT) {
                                    String strPINcode = Utils.getStringSetting(LoginActivity.this, Global.PREF_COMMUNICATION_PIN_CODE);
                                    if (/*inputDlg.mStrInputed.length() > 0 && */strPINcode.equals(inputDlg.mStrInputed) == true) {
                                        replaceFragment(new ConfigurationFragment());
                                    } else {
                                        try {
                                            final MessageDialog msgDialog = new MessageDialog(LoginActivity.this);
                                            msgDialog.mStrTitle = Str.Alert[Global.LANG].toUpperCase();
                                            msgDialog.mStrMessage = Str.Invalid_code[Global.LANG];
                                            msgDialog.mStrMiddle = Str.Ok[Global.LANG];
                                            msgDialog.setCancelable(true);
                                            msgDialog.show();
                                        }catch (Exception e)
                                        {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        });
                        inputDlg.show();
                    }catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                } else {
                    replaceFragment(new ConfigurationFragment());
                }
            }
        } else if (nId == R.id.txtEndJourney) {
            startMenu2Animation();
        } else if (nId == R.id.txtLogout) {
            startMenu2Animation();
        } else if (nId == R.id.txtChangeDriver) {
            startMenu2Animation();
        } else if (nId == R.id.txtChangePrinterType) {
            startMenu2Animation();
        } else if (nId == R.id.txtMultiLang) {
            showLanguages();
            startMenu1Animation();
        } else if (nId == R.id.txtVehicleInventory) {
        } else if (nId == R.id.txtPrintDocuments) {
            startMenu2Animation();
        } else if( nId == R.id.txtSalesTracker ) {
            startMenu2Animation();
        } else if( nId == R.id.txtDiary) {
            startMenu2Animation();
        } else if( nId == R.id.txtWindcaveReader) {
            startMenu2Animation();

        } else if(nId == R.id.txtSetInvoiceEnding) {
            startMenu2Animation();

        } else if(nId == R.id.txtDocuments || nId == R.id.imgDocuments){
            startMenu2Animation();
        } else if(nId == R.id.txtDriverBreak) {
            startMenu2Animation();
        }
        m_bClicked = false;
    }

    private void showLanguages() {
        int nChecked = Global.LANG;
        final int origIdx = Global.LANG;
        try {
            new AlertDialog.Builder(this)
                    .setTitle(Str.Please_select_the_language[Global.LANG])
                    .setSingleChoiceItems(new AlertSingleChoiceAdapter(LoginActivity.this, Str.LANGUAGES), nChecked, (dialog, nSelected) -> Global.LANG = nSelected)
                    .setPositiveButton(Str.Ok[Global.LANG], (dialog, nSelected) -> {
                        Utils.setIntSetting(LoginActivity.this, Global.PREF_CUR_LANG, Global.LANG);
                        sendBroadcast(new Intent(Global.ACTION_REFRESH_LANGUAGE));
                    })
                    .setNegativeButton(Str.Cancel[Global.LANG], (dialog, nSelected) -> Global.LANG = origIdx)
                    .create()
                    .show();
        }catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    @Override
    public void onAnimationEnd(Animation animation) {
        if (mShowMenu1 == false) {
            mLayoutMenu1.setVisibility(View.GONE);
        }
        if (mShowMenu2 == false) {
            mLayoutMenu2.setVisibility(View.GONE);
        }
        mIsAnimation = false;
    }

    @Override
    public void onAnimationRepeat(Animation animation) {
    }

    @Override
    public void onAnimationStart(Animation animation) {
    }

    public void upgradeNewVersion(String strMark)
    {
        //Write Trip No to the .DSDInstaller Folder
        Utils.writeTripInfo(Global.gTripNumber);
        Utils.markUpgradeAsk(strMark);

        //Kill all services
        Utils.stopMainService(this);
        Utils.setEndTripPlan(true, this);
        finishAffinity();
        Intent intent = new Intent();
        intent.setClassName("dsd.assist", "dsd.assist.activities.MainActivity");
        intent.putExtra("PackageName", getPackageName());
        intent.putExtra("ExternalLaunch", true);
        intent.putExtra("CompanyId", Utils.getStringSetting(this, Global.PREF_COMPANY_ID));
        intent.putExtra("CompanyPin", Utils.getStringSetting(this, Global.PREF_COMPNAY_PIN));

        //intent.putExtra("TripNo", Global.gTripNumber);
        startActivity(intent);
        finish();

        //Remove all trip data
        Utils.clearSettings(this);
    }

    @Override
    public void onBackPressed() {
        if (mShowMenu1 == true) {
            startMenu1Animation();
            return;
        }
        if (mShowMenu2 == true) {
            startMenu2Animation();
            return;
        }

        int count = getSupportFragmentManager().getBackStackEntryCount();
        if (count > 1) {
            super.onBackPressed();
        } else
        {
            showConfirmDialog();
        }
    }

    public void showConfirmDialog()
    {
        final MessageDialog dlg = new MessageDialog(this);
        dlg.mStrMessage = Str.Are_you_sure_wish_to_exit_the_application[Global.LANG];
        dlg.mStrTitle = "";
        dlg.mStrLeft = Str.No[Global.LANG];
        dlg.mStrRight = Str.Yes[Global.LANG];
        dlg.setCancelable(false);
        dlg.setOnDismissListener(new DialogInterface.OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
                if (dlg.mReturnCode == Global.DLG_RETURN_RIGHT){
                    Global.gSignedOn = false;
                    finish();
                }
            }
        });
        dlg.show();
    }

    public void refreshDriverName()
    {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frameContainer);
        if(fragment instanceof LoginFragment)
        {
            ((LoginFragment)fragment).processGotoPreTripTask();
        }
    }
}