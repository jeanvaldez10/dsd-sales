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
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.Credentials;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import dsd.dsdvansales.R;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;
import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;

@SuppressWarnings("deprecation")
public class WindCavePaymentDialog extends Dialog {

	private Context mContext = null;
	private EditText mEditAmount = null;
	public float mAmount = 0;

	public String username = "";
	public String password = "";
	public int mReturnCode = 0;
	public String mTransactionID = "";
	public class CardInfo{
		public String cardHolderName;
		public String cardNumber;
		public String dateExpiryMonth;
		public String dateExpiryYear;
		public String cvc2;
	}

	public class PurchaseInfo{
		public String type;
		public String amount;
		public String currency;
		public String merchantReference;
		public CardInfo card;
	}

	public class PaymentResponse{
		public String id;
	}

	public class QueryResponse{
		public String id;
		public String username;
		public boolean authorised;
		public String reCo;
		public String responseText;
		public String authCode;
		public String type;
		public String amount;
	}

	public WindCavePaymentDialog(Context context) {
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

		setContentView(R.layout.dialog_windcave_payment);

		((TextView) findViewById(R.id.txtTitle)).setText(Str.CardPayment[Global.LANG].toUpperCase());
		((TextView) findViewById(R.id.lblCardName)).setText(Str.NameOnCard[Global.LANG]);
		((TextView) findViewById(R.id.lblCardNo)).setText(Str.CardNumber[Global.LANG]);
		((TextView) findViewById(R.id.lblExpire)).setText(Str.ExpiryDate[Global.LANG]);
		((TextView) findViewById(R.id.lblAmount)).setText(Str.Amount[Global.LANG]);

		((Button) findViewById(R.id.btnLeft)).setText(Str.Return[Global.LANG]);
		((Button) findViewById(R.id.btnMiddle)).setText(Str.Manual[Global.LANG]);
		((Button) findViewById(R.id.btnRight)).setText(Str.Pay[Global.LANG]);

		username = Utils.getStringSetting(mContext, Global.PREF_WINDCAVE_API);
		password = Utils.getStringSetting(mContext, Global.PREF_WINDCAVE_PASSWORD);

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, 1);
		Date date = cal.getTime();
		((EditText) findViewById(R.id.editExpiryMonth)).setText(String.format(Locale.ENGLISH, "%02d", date.getMonth() + 1));
		((EditText) findViewById(R.id.editExpiryYear)).setText(String.format(Locale.ENGLISH, "%02d", date.getYear() - 100));

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

		((EditText) findViewById(R.id.editCardNo1)).addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				int nLen = s.toString().length();
				if (nLen >= 4) {
					showKeyboard((EditText) findViewById(R.id.editCardNo2));
				}
			}
		});

		((EditText) findViewById(R.id.editCardNo2)).addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			@Override
			public void afterTextChanged(Editable s) {
				int nLen = s.toString().length();
				if (nLen >= 4) {
					showKeyboard((EditText) findViewById(R.id.editCardNo3));
				} else if (nLen <= 0) {
					showKeyboard((EditText) findViewById(R.id.editCardNo1));
				}
			}
		});

		((EditText) findViewById(R.id.editCardNo3)).addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			@Override
			public void afterTextChanged(Editable s) {
				int nLen = s.toString().length();
				if (nLen >= 4) {
					showKeyboard((EditText) findViewById(R.id.editCardNo4));
				} else if (nLen <= 0) {
					showKeyboard((EditText) findViewById(R.id.editCardNo2));
				}
			}
		});

		((EditText) findViewById(R.id.editCardNo4)).addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			@Override
			public void afterTextChanged(Editable s) {
				int nLen = s.toString().length();
				if (nLen <= 0) {
					showKeyboard((EditText) findViewById(R.id.editCardNo3));
				}
			}
		});
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

	private void showKeyboard(EditText editText) {
		InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
		editText.requestFocus();
		imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
		editText.setSelection(editText.getText().toString().length());
	}



	private static OkHttpClient getRapidEwayClient(final String username, final String password) {
		OkHttpClient client = new OkHttpClient();
		client.interceptors().add(new Interceptor(){

			public Response intercept(Chain chain) throws IOException {
				String credential = Credentials.basic(username, password);
				Request originalRequest = chain.request();
				Request.Builder requestBuilder = originalRequest.newBuilder().header("Authorization", credential).method(originalRequest.method(), originalRequest.body());
				Request request = requestBuilder.build();
				return chain.proceed(request);
			}
		});
		client.setConnectTimeout(3, TimeUnit.MINUTES);
		client.setReadTimeout(3, TimeUnit.MINUTES);
		return client;
	}



	public static ApiInterface getApiClient(Context context, String username, String password){
		String baseUrl;
		if(Utils.getBooleanSetting(context, Global.PREF_WINDCAVE_UAT))
		{
			baseUrl = "https://uat.windcave.com";
		}
		else
		{
			baseUrl = "https://sec.windcave.com";
		}

		Retrofit retrofit = (new Retrofit.Builder()).baseUrl(baseUrl).addConverterFactory(GsonConverterFactory.create()).client(getRapidEwayClient(username, password)).build();
		ApiInterface adapter = retrofit.create(ApiInterface.class);
		return adapter;
	}


	private void doPayment() {
		String strCardName = ((EditText) findViewById(R.id.editCardName)).getText().toString();
		if (strCardName.length() == 0) {
			Utils.printDebug(mContext, Str.PleaseInputCardName[Global.LANG]);
			return;
		}
		String strCardNo1 = ((EditText) findViewById(R.id.editCardNo1)).getText().toString();
		int nCardType = Utils.getCardLength(strCardNo1);
		if (strCardNo1.length() != 4) {
			Utils.printDebug(mContext, Str.PleaseInputCardNumberCorrectly[Global.LANG]);
			return;
		}
		String strCardNo2 = ((EditText) findViewById(R.id.editCardNo2)).getText().toString();
		if (strCardNo2.length() != 4) {
			Utils.printDebug(mContext, Str.PleaseInputCardNumberCorrectly[Global.LANG]);
			return;
		}
		String strCardNo3 = ((EditText) findViewById(R.id.editCardNo3)).getText().toString();
		if (strCardNo3.length() != 4) {
			Utils.printDebug(mContext, Str.PleaseInputCardNumberCorrectly[Global.LANG]);
			return;
		}
		String strCardNo4 = ((EditText) findViewById(R.id.editCardNo4)).getText().toString();
		if ((strCardNo4.length() != 4 && nCardType == Global.MAX_LENGTH_STANDARD) ||
				(strCardNo4.length() != 3 && nCardType == Global.MAX_LENGTH_AMERICAN_EXPRESS) ||
				(strCardNo4.length() != 2 && nCardType == Global.MAX_LENGTH_DINERS_CLUB)) {
			Utils.printDebug(mContext, Str.PleaseInputCardNumberCorrectly[Global.LANG]);
			return;
		}
		int nExpireMonth = Utils.parseIntFromString(((EditText) findViewById(R.id.editExpiryMonth)).getText().toString());
		if (nExpireMonth == 0) {
			Utils.printDebug(mContext, Str.PleaseInputExpiryMonthCorrectly[Global.LANG]);
			return;
		}
		int nExpireYear = Utils.parseIntFromString(((EditText) findViewById(R.id.editExpiryYear)).getText().toString());
		if (nExpireYear < (new Date().getYear() - 100)) {
			Utils.printDebug(mContext, Str.PleaseInputExpiryYearCorrectly[Global.LANG]);
			return;
		}
		String strCVN = ((EditText) findViewById(R.id.editCVN)).getText().toString();
		if (strCVN.length() == 0) {
			Utils.printDebug(mContext, Str.PleaseInputCVV[Global.LANG]);
			return;
		}
		if (mAmount <= 0) {
			Utils.printDebug(mContext, Str.PleaseInputAmount[Global.LANG]);
			return;
		}
		if(!Utils.isConnectingToInternet(mContext))
		{
			Utils.printDebug(mContext, Str.network_connection_error[Global.LANG]);
			return;
		}
		String strCardNo = strCardNo1 + strCardNo2 + strCardNo3 + strCardNo4;

		//new eWayPaymentTask().execute(new String[]{strCardName, strCardNo, strCVN, String.format(Locale.ENGLISH, "%02d", nExpireMonth), String.format(Locale.ENGLISH, "%04d", 2000 + nExpireYear)});
		final ProgressDialog progressDialog = ProgressDialog.show(mContext, "", Str.Loading[Global.LANG]);
		progressDialog.setCancelable(false);
		try
		{
			progressDialog.show();
		}catch (Exception e)
		{
			e.printStackTrace();
		}


		CardInfo cardInfo = new CardInfo();
		cardInfo.cardHolderName = strCardName;
		cardInfo.cardNumber = strCardNo;
		cardInfo.dateExpiryMonth = String.format(Locale.ENGLISH, "%02d", nExpireMonth);
		cardInfo.dateExpiryYear = String.format(Locale.ENGLISH, "%04d", nExpireYear + 2000);
		cardInfo.cvc2 = strCVN;
		PurchaseInfo purchaseInfo = new PurchaseInfo();
		purchaseInfo.amount = String.format(Locale.ENGLISH, "%.2f", mAmount);
		purchaseInfo.card = cardInfo;
		purchaseInfo.currency = Utils.getStringSetting(mContext, Global.PREF_WINDCAVE_CURR).toUpperCase();
		purchaseInfo.type = "purchase";
		purchaseInfo.merchantReference = strCardName + System.currentTimeMillis();

		getApiClient(mContext, username, password).purchase(purchaseInfo).enqueue(new Callback<PaymentResponse>() {
			@Override
			public void onResponse(retrofit.Response<PaymentResponse> response, Retrofit retrofit) {

				if( response.isSuccess() )
				{
					final PaymentResponse paymentResponse = response.body();
					new Thread()
					{
						@Override
						public void run() {
							retrofit.Response<QueryResponse> response;
							while(true)
							{
								try {
									response = getApiClient(mContext, username, password).query(paymentResponse.id).execute();
									if(response.isSuccess() && response.code() == 200)
									{
										break;
									}
								} catch (IOException e) {
									e.printStackTrace();
								}

								try {
									Thread.sleep(5 * 1000);
								} catch (InterruptedException e) {

								}
							}
							if(response != null && response.body().authorised)
							{
								final retrofit.Response<QueryResponse> finalResponse = response;
								new Handler(Looper.getMainLooper()).post(new Runnable() {
									@Override
									public void run() {
										progressDialog.dismiss();

										try
										{
											Toast.makeText(mContext, Str.SuccessToPay[Global.LANG], Toast.LENGTH_LONG).show();
										}catch (Exception e)
										{
											e.printStackTrace();
										}
										mReturnCode = Global.DLG_RETURN_RIGHT;
										mTransactionID = paymentResponse.id + "||" + finalResponse.body().authCode + "||" + finalResponse.body().amount;
										dismiss();
									}
								});
							}
							else
							{
								final retrofit.Response<QueryResponse> finalResponse = response;
								new Handler(Looper.getMainLooper()).post(new Runnable() {
									@Override
									public void run() {
										progressDialog.dismiss();

										try
										{
											Toast.makeText(mContext, finalResponse.body().responseText, Toast.LENGTH_LONG).show();
										}catch (Exception e)
										{
											e.printStackTrace();
										}

									}
								});
							}

						}
					}.start();

				}
				else
				{
					progressDialog.dismiss();
					String errorMsg = response.message();
					try
					{
						if(TextUtils.isEmpty(errorMsg))
						{
							Toast.makeText(mContext, errorMsg, Toast.LENGTH_SHORT).show();
						}
						else
						{
							Toast.makeText(mContext, Str.mint_setting_error[Global.LANG], Toast.LENGTH_SHORT).show();
						}
					}catch (Exception e)
					{
						e.printStackTrace();
					}

				}
			}

			@Override
			public void onFailure(Throwable t) {
				progressDialog.dismiss();
				try
				{
					Toast.makeText(mContext, Str.mint_setting_error[Global.LANG], Toast.LENGTH_SHORT).show();
				}catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
	}

	public interface ApiInterface {
		@POST("/api/v1/transactions")
		Call<PaymentResponse> purchase(@Body PurchaseInfo cardInfo);

		@GET("/api/v1/transactions/{id}")
		Call<QueryResponse> query(@Path("id") String id);
	}

	private ProgressDialog mProgressDialog = null;
	public void showProgressDialog(String strText)
	{
		try
		{
			if(mProgressDialog == null)
			{
				mProgressDialog = new ProgressDialog(mContext);
				mProgressDialog.setCancelable(false);
				mProgressDialog.setMessage(strText);
			}
			mProgressDialog.show();
		}catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	public void hideProgressDialog()
	{
		try
		{
			if(mProgressDialog != null)
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

