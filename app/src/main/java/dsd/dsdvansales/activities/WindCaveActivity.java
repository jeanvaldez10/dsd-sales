package dsd.dsdvansales.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.paymentexpress.paymentmodule.Transaction;
import com.paymentexpress.paymentmodule.TransactionDetails;
import com.paymentexpress.paymentmodule.TransactionInterface;
import com.paymentexpress.paymentmodule.TransactionResult;
import com.paymentexpress.paymentmodule.TransactionResultDetails;
import com.paymentexpress.paymentmodule.TransactionResultHandler;
import com.paymentexpress.paymentmodule.TransactionResultPageSettings;
import com.paymentexpress.paymentmodule.TransactionType;
import com.windcave.hitmodule.Transaction.HITTransaction;
import com.windcave.hitmodule.Transaction.HITTransactionDetails;
import com.windcave.hitmodule.Transaction.HITTransactionResult;
import com.windcave.hitmodule.Transaction.HITTransactionResultDetails;
import com.windcave.hitmodule.Transaction.HITTransactionResultHandler;
import com.windcave.hitmodule.Transaction.HITTransactionType;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Locale;
import java.util.Map;

import dsd.dsdvansales.R;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Utils;

public class WindCaveActivity extends AppCompatActivity {

    public String strReference = "";
    public boolean bCardPresent = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

            if (android.os.Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        }catch (Exception e)
        {
            e.printStackTrace();
        }

        setContentView(R.layout.activity_wind_cave);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null)
        {
            actionBar.hide();
        }

        Intent intent = getIntent();
        if(intent != null)
        {
            strReference = intent.getStringExtra("reference");
            bCardPresent = intent.getBooleanExtra("card_present", false);
        }
        new Handler(getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if(bCardPresent)
                {
                    doWindCaveCardPresentPayment();
                }
                else
                {
                    doWindCaveCardNotPresentPayment();
                }

            }
        });

    }

    private void doWindCaveCardPresentPayment()
    {
        String strAppVersion = "";
        PackageManager manager = getPackageManager();
        PackageInfo info;
        try {
            info = manager.getPackageInfo(getPackageName(), 0);
            strAppVersion = info.versionName;
        } catch (Exception e) {
            strAppVersion = "0.0.0";
            e.printStackTrace();
        }

        String strVendorId = Utils.getStringSetting(this, Global.PREF_USER_NAME);
        if(TextUtils.isEmpty(strVendorId))
        {
            strVendorId = Utils.getStringSetting(this, Global.PREF_USER_DESCRIPTION);
        }

        HITTransactionDetails transactionDetails = new HITTransactionDetails();
        transactionDetails.setAmount(new BigDecimal(String.format(Locale.ENGLISH, "%.2f", Global.gCurRealPaymentAmount)));
        transactionDetails.setCurrencyCode(Utils.getStringSetting(this, Global.PREF_WINDCAVE_CURR).toUpperCase());
        transactionDetails.setMerchantReference(strReference);
        transactionDetails.setTxnRef(getTrxnRef(strReference));
        transactionDetails.setStationId(Utils.getStringSetting(this, Global.PREF_WINDCAVE_HITSTATION_ID));

        transactionDetails.setUserName(Utils.getStringSetting(this, Global.PREF_WINDCAVE_HITUSERNAME));
        transactionDetails.setKey(Utils.getStringSetting(this, Global.PREF_WINDCAVE_HITKEY));

        transactionDetails.setDeviceId(strVendorId);
        transactionDetails.setPosName("DSD Assist");
        transactionDetails.setPosVersion(strAppVersion);
        transactionDetails.setVendorId("DSD Assist");

        HITTransactionResultHandler transactionResultHandler = new HITTransactionResultHandler() {
            @Override
            public void onHITTransactionFinished(HITTransactionResultDetails details) {
                HITTransactionResult result = details.getTransactionResult();
                Gson gson = new Gson();
                String jsonData = gson.toJson(result);
                Log.e("HitResponse", jsonData);

                if(result == HITTransactionResult.Succeeded)
                {
                    Intent intent = new Intent();
                    intent.putExtra("trxn_id", details.getDpsTxnRef());
                    intent.putExtra("reference", strReference);
                    setResult(RESULT_OK, intent);
                    finish();
                    overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                }
                else if(result == HITTransactionResult.Failed)
                {
                    Intent intent = new Intent();
                    Map<String, String> res = details.getResponse();
                    Log.e("HitResponse", res.toString());

                    String errorMsg = details.getResponseText();
                    intent.putExtra("error_msg", "Failed : " + errorMsg);
                    setResult(RESULT_CANCELED, intent);
                    finish();
                    overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                }
                else if(result == HITTransactionResult.InvalidTransactionType)
                {
                    Intent intent = new Intent();
                    intent.putExtra("error_msg", "Provided transaction type is not supported");
                    setResult(RESULT_CANCELED, intent);
                    finish();
                    overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                }
                else if(result == HITTransactionResult.UserKeyNotMatched)
                {
                    Intent intent = new Intent();
                    intent.putExtra("error_msg", "The provided pair of username and key were not found by Windcave");
                    setResult(RESULT_CANCELED, intent);
                    finish();
                    overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                }
                else if(result == HITTransactionResult.StationPrefixError)
                {
                    Intent intent = new Intent();
                    intent.putExtra("error_msg", "Station Prefix configured for Station, but not matched with input");
                    setResult(RESULT_CANCELED, intent);
                    finish();
                    overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                }
                else if(result == HITTransactionResult.ExistingRequestInProgress)
                {
                    Intent intent = new Intent();
                    intent.putExtra("error_msg", "There is another transaction in process");
                    setResult(RESULT_CANCELED, intent);
                    finish();
                    overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                }
                else if(result == HITTransactionResult.TransmitError)
                {
                    Intent intent = new Intent();
                    intent.putExtra("error_msg", "Transmission error");
                    setResult(RESULT_CANCELED, intent);
                    finish();
                    overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                }
                else if(result == HITTransactionResult.SessionInitError)
                {
                    Intent intent = new Intent();
                    intent.putExtra("error_msg", "Session initialization error. Please check internet connectivity to the pinpad, or try to do a manual logon");
                    setResult(RESULT_CANCELED, intent);
                    finish();
                    overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                }
                else if(result == HITTransactionResult.UserIDNotMatched)
                {
                    Intent intent = new Intent();
                    intent.putExtra("error_msg", "HIT User ID not matched");
                    setResult(RESULT_CANCELED, intent);
                    finish();
                    overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                }
                else if(result == HITTransactionResult.TerminalNotConnected)
                {
                    Intent intent = new Intent();
                    intent.putExtra("error_msg", "Pinpad is not connected");
                    setResult(RESULT_CANCELED, intent);
                    finish();
                    overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                }
                else if(result == HITTransactionResult.TerminalNotConnected)
                {
                    Intent intent = new Intent();
                    intent.putExtra("error_msg", "Module was unable to get response from the server in time");
                    setResult(RESULT_CANCELED, intent);
                    finish();
                    overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                }
            }
        };

        HITTransaction.setResultHandler(transactionResultHandler);

        try
        {
            HITTransaction.startTransaction(getSupportFragmentManager(), transactionDetails, HITTransactionType.Purchase, Utils.getBooleanSetting(this, Global.PREF_WINDCAVE_UAT), transactionResultHandler);
        }catch (Exception e)
        {
            Intent intent = new Intent();
            intent.putExtra("error_msg", e.getMessage());
            setResult(RESULT_CANCELED, intent);
            finish();
            overridePendingTransition(R.anim.fadein, R.anim.fadeout);
            e.printStackTrace();
        }


    }

    private String getTrxnRef(String strReference)
    {
        String values[] = strReference.split("--");
        String strRef = values[1].replace("(", "");
        strRef = strRef.replace(")","");
        return strRef;
    }

    private void doWindCaveCardNotPresentPayment()
    {
        final TransactionDetails transactionDetails = new TransactionDetails();
        transactionDetails.setAmount(new BigDecimal(String.format(Locale.ENGLISH, "%.2f", Global.gCurRealPaymentAmount)));
        transactionDetails.setCurrency(Currency.getInstance(Utils.getStringSetting(this, Global.PREF_WINDCAVE_CURR).toUpperCase()));
        transactionDetails.setUsername(Utils.getStringSetting(this, Global.PREF_WINDCAVE_API));
        transactionDetails.setPassword(Utils.getStringSetting(this, Global.PREF_WINDCAVE_PASSWORD));
        transactionDetails.setMerchantRef(strReference);

        TransactionResultHandler transactionResultHandler = new TransactionResultHandler() {
            @Override
            public void onResultReceived(TransactionResultDetails transactionResultDetails) {

                if(transactionResultDetails.getResult() == TransactionResult.Succeeded)
                {
                    Intent intent = new Intent();
                    intent.putExtra("trxn_id", transactionDetails.getTxnId());
                    intent.putExtra("reference", strReference);
                    setResult(RESULT_OK, intent);
                    finish();
                    overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                }
                else
                {
                    Intent intent = new Intent();
                    String errorMsg = transactionResultDetails.getErrorMessage();
                    if(TextUtils.isEmpty(errorMsg))
                    {
                        errorMsg = transactionResultDetails.getResponseText();
                    }
                    if(TextUtils.isEmpty(errorMsg))
                    {
                        errorMsg = transactionResultDetails.getHelpText();
                    }

                    intent.putExtra("error_msg", errorMsg);
                    setResult(RESULT_CANCELED, intent);
                    finish();
                    overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                }
            }
        };

        TransactionResultPageSettings transactionResultPageSettings = new TransactionResultPageSettings();

        transactionResultPageSettings.setHideTxnRef(true);
        transactionResultPageSettings.setHideAuthCode(true);
        transactionResultPageSettings.setHideTxnType(true);

        /*transactionResultPageSettings.setHideHelpText(true);
        transactionResultPageSettings.setHideMerchantRef(true);
        transactionResultPageSettings.setHideCardType(true);*/

        /*
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;

        Transaction.setPreferredWebViewHeight((int)(height * 0.85f));*/
        try
        {
            Transaction.doTransaction(getSupportFragmentManager(), TransactionInterface.Native, Utils.getBooleanSetting(this, Global.PREF_WINDCAVE_UAT), TransactionType.Purchase,
                    transactionDetails, transactionResultHandler, transactionResultPageSettings);
        }catch (Exception e)
        {
            Intent intent = new Intent();
            intent.putExtra("error_msg", e.getMessage());
            setResult(RESULT_CANCELED, intent);
            finish();
            overridePendingTransition(R.anim.fadein, R.anim.fadeout);
            e.printStackTrace();
        }
    }
}
