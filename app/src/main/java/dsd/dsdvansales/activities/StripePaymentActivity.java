package dsd.dsdvansales.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.paymentexpress.paymentmodule.Transaction;
import com.paymentexpress.paymentmodule.TransactionDetails;
import com.paymentexpress.paymentmodule.TransactionInterface;
import com.paymentexpress.paymentmodule.TransactionResult;
import com.paymentexpress.paymentmodule.TransactionResultDetails;
import com.paymentexpress.paymentmodule.TransactionResultHandler;
import com.paymentexpress.paymentmodule.TransactionResultPageSettings;
import com.paymentexpress.paymentmodule.TransactionType;
import com.stripe.Stripe;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.model.ConfirmPaymentIntentParams;
import com.stripe.android.model.PaymentMethodCreateParams;
import com.stripe.android.payments.paymentlauncher.PaymentLauncher;
import com.stripe.android.payments.paymentlauncher.PaymentResult;
import com.stripe.android.view.CardInputWidget;
import com.stripe.android.view.CardMultilineWidget;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import com.windcave.hitmodule.Transaction.HITTransaction;
import com.windcave.hitmodule.Transaction.HITTransactionDetails;
import com.windcave.hitmodule.Transaction.HITTransactionResult;
import com.windcave.hitmodule.Transaction.HITTransactionResultDetails;
import com.windcave.hitmodule.Transaction.HITTransactionResultHandler;
import com.windcave.hitmodule.Transaction.HITTransactionType;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import dsd.dsdvansales.R;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;
import kotlinx.coroutines.android.HandlerDispatcher;

public class StripePaymentActivity extends AppCompatActivity {

    private EditText mEditAmount = null;

    public int mReturnCode = 0;
    public String mTransactionID = "";
    public String mCustomerID = "";
    public float mAmount = 0;
    public String mStrReference = "";

    private String paymentIntentClientSecret;
    private PaymentLauncher paymentLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

            WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
            lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            lpWindow.dimAmount = 0.1f;
            getWindow().setAttributes(lpWindow);

            if (Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        }catch (Exception e)
        {
            e.printStackTrace();
        }

        setContentView(R.layout.activity_stripe_payment);

        Intent intent = getIntent();
        if(intent != null)
        {
            mAmount = intent.getFloatExtra("amount", 0);
        }
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null)
        {
            actionBar.hide();
        }

        ((TextView) findViewById(R.id.txtTitle)).setText(Str.StripeCardPayment[Global.LANG].toUpperCase());
        ((TextView) findViewById(R.id.lblAmount)).setText(Str.Amount[Global.LANG]);

        ((Button) findViewById(R.id.btnLeft)).setText(Str.Return[Global.LANG]);
        ((Button) findViewById(R.id.btnMiddle)).setText(Str.Manual[Global.LANG]);
        ((Button) findViewById(R.id.btnRight)).setText(Str.Pay[Global.LANG]);

        ((Button) findViewById(R.id.btnLeft)).setOnClickListener(view -> {
            setResult(RESULT_CANCELED);
            finish();
        });

        ((Button) findViewById(R.id.btnRight)).setOnClickListener(view -> doPayment());

        ((Button) findViewById(R.id.btnMiddle)).setOnClickListener(view -> {
            Intent intent1 = new Intent();
            intent1.putExtra("return_type", "middle");
            setIntent(intent1);
            setResult(RESULT_OK, intent1);
            finish();
        });

        mEditAmount = (EditText) findViewById(R.id.editAmount);
        if( (Global.gCurCustomerInfo.strPayType.trim().equals("1") || Global.gCurCustomerInfo.strPayType.trim().equals("2")) &&
                Utils.getStringSetting(this, Global.PREF_ROUTE_PAYMENTADJUST).trim().equals("1") )
        {
            mEditAmount.setEnabled(false);
        }
        else
        {
            mEditAmount.setEnabled(true);
        }

        mEditAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (mEditAmount.getTag() == null) {
                    setFormattedAmount(s.toString());
                }
            }
        });
        mEditAmount.post(new Runnable() {
            public void run() {
                mEditAmount.setSelection(mEditAmount.getText().toString().length());
                mEditAmount.postDelayed(this, 100);
            }
        });
        setAmount();

        final PaymentConfiguration paymentConfiguration
                = PaymentConfiguration.getInstance(getApplicationContext());
        paymentLauncher = PaymentLauncher.Companion.create(this, paymentConfiguration.getPublishableKey(), paymentConfiguration.getStripeAccountId(), this::onPaymentResult);

        loadData();
    }

    private void loadData()
    {

    }

    private void onPaymentResult(PaymentResult paymentResult) {
        if (paymentResult instanceof PaymentResult.Completed) {
            Intent intent = new Intent();
            intent.putExtra("return_type", "right");
            intent.putExtra("trxnAmount", mAmount);
            intent.putExtra("trxnID", mTransactionID);
            setResult(RESULT_OK, intent);
            finish();
//            showProgressDialog(Str.Processing[Global.LANG]);
//            new Thread()
//            {
//                @Override
//                public void run() {
//                    PaymentIntent paymentIntent = null;
//                    try {
//                        paymentIntent = PaymentIntent.retrieve(paymentIntentClientSecret);
//                    } catch (StripeException e) {
//                        new Handler(Looper.getMainLooper()).post(new Runnable() {
//                            @Override
//                            public void run() {
//                                hideProgressDialog();
//                                Toast.makeText(StripePaymentActivity.this, "Payment Retrive failed", Toast.LENGTH_LONG).show();
//                            }
//                        });
//
//                    }
//                    String transationID = paymentIntent.getId();
//
//                    new Handler(Looper.getMainLooper()).post(new Runnable() {
//                        @Override
//                        public void run() {
//
//                        }
//                    });
//                }
//            }.start();
        } else if (paymentResult instanceof PaymentResult.Canceled) {

        } else if (paymentResult instanceof PaymentResult.Failed) {
            // This string comes from the PaymentIntent's error message.
            // See here: https://stripe.com/docs/api/payment_intents/object#payment_intent_object-last_payment_error-message
            //message = "Failed: " + ((PaymentResult.Failed) paymentResult).getThrowable().getMessage();
        }

        //displayAlert("PaymentResult: ", message, true);
    }

    private void setFormattedAmount(String strVal) {
        String strArray[] = strVal.split("\\.");
        strVal = "";
        for (int i=0; i<strArray.length; i++) {
            strVal += strArray[i];
        }
        int nValue = Utils.getIntSetting(this, Global.PREF_DECIMAL);
        mAmount = Utils.parseFloatFromString(strVal) / (float)(Math.pow(10,nValue));
        setAmount();
    }

    private void setAmount() {
        mEditAmount.setTag("From App");
        mEditAmount.setText(String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(this, Global.PREF_DECIMAL) + "f", mAmount));
        mEditAmount.setSelection(mEditAmount.getText().toString().length());
        mEditAmount.setTag(null);
    }
    private void doPayment() {

        final String strPubKey = Utils.getStringSetting(this, Global.PREF_STRIPE_PUB_KEY);
        final String strSecKey = Utils.getStringSetting(this, Global.PREF_STRIPE_SEC_KEY);

        if(TextUtils.isEmpty(strPubKey))
        {
            Utils.printDebug(this, Str.StripePubKeyNotCorrect[Global.LANG]);
            return;
        }

        if(TextUtils.isEmpty(strSecKey))
        {
            Utils.printDebug(this, Str.StripeSecKeyNotCorrect[Global.LANG]);
            return;
        }

        if (mAmount <= 0) {
            Utils.printDebug(this, Str.PleaseInputAmount[Global.LANG]);
            return;
        }


        showProgressDialog(Str.Loading[Global.LANG]);
        new Thread(){
            @Override
            public void run() {
                Stripe.apiKey = strSecKey;

                //Create the Customer of Stripe
                com.stripe.model.Customer customer = null;

                Map<String, String> metaData = new HashMap<>();
                metaData.put("Chain No", String.valueOf(Global.gCurCustomerInfo.nChainNo));
                metaData.put("Customer No", String.valueOf(Global.gCurCustomerInfo.nCustNo));
                metaData.put("Customer Name", String.valueOf(Global.gCurCustomerInfo.strName));
                com.stripe.param.CustomerCreateParams custParams =
                        com.stripe.param.CustomerCreateParams.builder()
                                .setEmail(Global.gCurCustomerInfo.strEmail)
                                .setName(Global.gCurCustomerInfo.strName)
                                .setPhone(Global.gCurCustomerInfo.strPhone)
                                .setMetadata(metaData)
                                .build();

                try {
                    customer = com.stripe.model.Customer.create(custParams);
                } catch (StripeException e) {
                    e.printStackTrace();
                }

                String strCurrency = Utils.getStringSetting(StripePaymentActivity.this, Global.PREF_CURRENCY);
                if(TextUtils.isEmpty(strCurrency))
                {
                    strCurrency = "AUD";
                }

                PaymentIntentCreateParams params =
                        PaymentIntentCreateParams.builder()
                                .setAmount((long)(mAmount * 100))
                                .setCurrency(strCurrency)
                                .setCustomer(customer.getId())
                                .addPaymentMethodType("card")
                                .build();

                PaymentIntent intent = null;
                try {
                    intent = PaymentIntent.create(params);
                    mTransactionID = intent.getId();
                } catch (StripeException e) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            hideProgressDialog();
                            Toast.makeText(StripePaymentActivity.this, "Cannot process payment", Toast.LENGTH_LONG).show();
                        }
                    });
                }
                paymentIntentClientSecret = intent.getClientSecret();
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        hideProgressDialog();


                        final CardMultilineWidget cardInputWidget = findViewById(R.id.cardInputWidget);
                        final PaymentMethodCreateParams params = cardInputWidget.getPaymentMethodCreateParams();
                        if (params != null) {
                            final ConfirmPaymentIntentParams confirmParams = ConfirmPaymentIntentParams.createWithPaymentMethodCreateParams(params, paymentIntentClientSecret);
                            paymentLauncher.confirm(confirmParams);
                        }
                    }
                });
            }
        }.start();



    }

    private ProgressDialog progressDialog = null;
    public void showProgressDialog(String strText)
    {
        try
        {
            if(progressDialog == null)
            {
                progressDialog = new ProgressDialog(this);
                progressDialog.setCancelable(false);
                progressDialog.show();
            }
            progressDialog.setMessage(strText);
        }catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    public void hideProgressDialog()
    {
        try
        {
            if(progressDialog != null)
            {
                progressDialog.dismiss();
                progressDialog = null;
            }
        }catch (Exception e)
        {
            e.printStackTrace();
        }

    }
}
