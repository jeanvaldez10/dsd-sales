package dsd.dsdvansales.dialogs;


import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.stripe.Stripe;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.model.ConfirmPaymentIntentParams;
import com.stripe.android.model.PaymentMethodCreateParams;
import com.stripe.android.payments.paymentlauncher.PaymentLauncher;
import com.stripe.android.payments.paymentlauncher.PaymentResult;
import com.stripe.android.view.CardInputWidget;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import dsd.dsdvansales.R;
import dsd.dsdvansales.activities.MainActivity;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;

public class NewStripePaymentDialog extends Dialog {

	private MainActivity mContext = null;
	private EditText mEditAmount = null;

	public int mReturnCode = 0;
	public String mTransactionID = "";
	public String mCustomerID = "";
	public float mAmount = 0;
	public String mStrReference = "";

	private String paymentIntentClientSecret;
	private PaymentLauncher paymentLauncher;

	public NewStripePaymentDialog(MainActivity context) {
		super(context, android.R.style.Theme_Translucent_NoTitleBar);
		this.mContext = context;
	}
	public boolean m_bClicked = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
		lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
		lpWindow.dimAmount = 0.9f;
		getWindow().setAttributes(lpWindow);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.dialog_new_stripe_payment);

		((TextView) findViewById(R.id.txtTitle)).setText(Str.StripeCardPayment[Global.LANG].toUpperCase());
		((TextView) findViewById(R.id.lblAmount)).setText(Str.Amount[Global.LANG]);

		((Button) findViewById(R.id.btnLeft)).setText(Str.Return[Global.LANG]);
		((Button) findViewById(R.id.btnMiddle)).setText(Str.Manual[Global.LANG]);
		((Button) findViewById(R.id.btnRight)).setText(Str.Pay[Global.LANG]);


		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, 1);
		Date date = cal.getTime();

		mEditAmount = (EditText) findViewById(R.id.editAmount);
		if( (Global.gCurCustomerInfo.strPayType.trim().equals("1") || Global.gCurCustomerInfo.strPayType.trim().equals("2")) &&
				Utils.getStringSetting(mContext, Global.PREF_ROUTE_PAYMENTADJUST).trim().equals("1") )
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

		findViewById(R.id.btnLeft).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if( m_bClicked ) return;
				m_bClicked = true;
				mReturnCode = Global.DLG_RETURN_LEFT;
				dismiss();
				m_bClicked = false;
			}
		});

		findViewById(R.id.btnMiddle).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if( m_bClicked ) return;
				m_bClicked = true;
				mReturnCode = Global.DLG_RETURN_MIDDLE;
				dismiss();
				m_bClicked = false;
			}
		});

		findViewById(R.id.btnRight).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if( m_bClicked ) return;
				m_bClicked = true;
				doPayment();
				m_bClicked = false;
			}
		});

		final PaymentConfiguration paymentConfiguration
				= PaymentConfiguration.getInstance(mContext.getApplicationContext());
		paymentLauncher = PaymentLauncher.Companion.create(mContext, paymentConfiguration.getPublishableKey(), paymentConfiguration.getStripeAccountId(), this::onPaymentResult);
	}

	private void onPaymentResult(PaymentResult paymentResult) {
		String message = "";
		if (paymentResult instanceof PaymentResult.Completed) {
			message = "Completed!";
		} else if (paymentResult instanceof PaymentResult.Canceled) {
			message = "Canceled!";
		} else if (paymentResult instanceof PaymentResult.Failed) {
			// This string comes from the PaymentIntent's error message.
			// See here: https://stripe.com/docs/api/payment_intents/object#payment_intent_object-last_payment_error-message
			message = "Failed: "
					+ ((PaymentResult.Failed) paymentResult).getThrowable().getMessage();
		}

		//displayAlert("PaymentResult: ", message, true);
	}

	private void setFormattedAmount(String strVal) {
		String strArray[] = strVal.split("\\.");
		strVal = "";
		for (int i=0; i<strArray.length; i++) {
			strVal += strArray[i];
		}
		int nValue = Utils.getIntSetting(mContext, Global.PREF_DECIMAL);
		mAmount = Utils.parseFloatFromString(strVal) / (float)(Math.pow(10,nValue));
		setAmount();
	}

	private void setAmount() {
		mEditAmount.setTag("From App");
		mEditAmount.setText(String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(mContext, Global.PREF_DECIMAL) + "f", mAmount));
		mEditAmount.setSelection(mEditAmount.getText().toString().length());
		mEditAmount.setTag(null);
	}
	private void doPayment() {

		if (mAmount <= 0) {
			Utils.printDebug(mContext, Str.PleaseInputAmount[Global.LANG]);
			return;
		}
		final String strPubKey = Utils.getStringSetting(mContext, Global.PREF_STRIPE_PUB_KEY);
		final String strSecKey = Utils.getStringSetting(mContext, Global.PREF_STRIPE_SEC_KEY);

		if(TextUtils.isEmpty(strPubKey))
		{
			Utils.printDebug(mContext, Str.StripePubKeyNotCorrect[Global.LANG]);
			return;
		}

		if(TextUtils.isEmpty(strSecKey))
		{
			Utils.printDebug(mContext, Str.StripeSecKeyNotCorrect[Global.LANG]);
			return;
		}

		showProgressDialog(Str.Processing[Global.LANG]);
		new Thread()
		{
			@Override
			public void run() {
				Stripe.apiKey = "sk_test_BZ7A45nxaKK9cmAK4gWwrTmU";

				{
					PaymentIntentCreateParams params =
							PaymentIntentCreateParams.builder()
									.setAmount(1099L)
									.setCurrency("usd")
									.addPaymentMethodType("card")
									.build();

					PaymentIntent intent = null;
					try {
						intent = PaymentIntent.create(params);
					} catch (StripeException e) {
						new Handler(Looper.getMainLooper()).post(new Runnable() {
							@Override
							public void run() {
								hideProgressDialog();
								Toast.makeText(mContext, "Cannot process payment", Toast.LENGTH_LONG).show();
							}
						});
					}
					paymentIntentClientSecret = intent.getClientSecret();
				}

				new Handler(Looper.getMainLooper()).post(new Runnable() {
					@Override
					public void run() {
						hideProgressDialog();

						final CardInputWidget cardInputWidget = findViewById(R.id.cardInputWidget);
						final PaymentMethodCreateParams params = cardInputWidget.getPaymentMethodCreateParams();
						if (params != null) {
							final ConfirmPaymentIntentParams confirmParams =
									ConfirmPaymentIntentParams.createWithPaymentMethodCreateParams(
											params,
											paymentIntentClientSecret
									);
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
				progressDialog = new ProgressDialog(mContext);
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

