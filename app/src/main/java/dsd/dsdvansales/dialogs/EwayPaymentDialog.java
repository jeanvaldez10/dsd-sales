package dsd.dsdvansales.dialogs;


import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

import dsd.dsdvansales.activities.MainActivity;
import dsd.dsdvansales.db.DbAdapter;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.model.DocTextInfo;
import dsd.dsdvansales.model.EWayRefundResponse;
import dsd.dsdvansales.model.EWayTransactionResponse;
import dsd.dsdvansales.model.TransactionResponse;
import dsd.dsdvansales.services.Tls12SocketFactory;
import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.POST;
import retrofit.http.Path;

import android.app.*;
import android.content.Context;
import android.os.*;
import android.text.*;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.*;
import android.widget.*;

import com.eway.payment.sdk.android.*;
import com.eway.payment.sdk.android.beans.*;
import com.eway.payment.sdk.android.entities.*;
import com.squareup.okhttp.ConnectionSpec;
import com.squareup.okhttp.Credentials;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.Interceptor.Chain;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.TlsVersion;
import com.squareup.picasso.Picasso;

import javax.net.ssl.SSLContext;

import dsd.dsdvansales.R;
import dsd.dsdvansales.global.Utils;

@SuppressWarnings("deprecation")
public class EwayPaymentDialog extends Dialog {

	private Context mContext;
	private EditText mEditAmount = null;

	public int mReturnCode = 0;
	public String mTransactionID = "";
	public float mAmount = 0;

	public EwayPaymentDialog(Context context) {
		super(context, android.R.style.Theme_Translucent_NoTitleBar);
		this.mContext = context;
	}
	public boolean m_bClicked = false;

	private static String apiKey = "F9802CaTan7PEJXzII++VGKeriPWTCxG1DnEnv8rgbDtx+CMZfl1A2itKry4xSi+vhlVR4";
	private static String password = "zE1tlOcZ";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
		lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
		lpWindow.dimAmount = 0.9f;
		getWindow().setAttributes(lpWindow);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.dialog_eway_payment);

		((TextView) findViewById(R.id.txtTitle)).setText(Str.EWayCardPayment[Global.LANG].toUpperCase());
		((TextView) findViewById(R.id.lblCardName)).setText(Str.NameOnCard[Global.LANG]);
		((TextView) findViewById(R.id.lblCardNo)).setText(Str.CardNumber[Global.LANG]);
		((TextView) findViewById(R.id.lblExpire)).setText(Str.ExpiryDate[Global.LANG]);
		((TextView) findViewById(R.id.lblAmount)).setText(Str.Amount[Global.LANG]);
		((Button) findViewById(R.id.btnLeft)).setText(Str.Return[Global.LANG]);
		((Button) findViewById(R.id.btnMiddle)).setText(Str.Manual[Global.LANG]);
		((Button) findViewById(R.id.btnRight)).setText(Str.Pay[Global.LANG]);

		if(Utils.getIntSetting(mContext, Global.PREF_EWAY_CARD_PROC) == 1)
		{
			RapidAPI.PublicAPIKey = Utils.getStringSetting(mContext, Global.PREF_EWAY_API);
			RapidAPI.RapidEndpoint = Utils.getStringSetting(mContext, Global.PREF_EWAY_SYSTEM);
			apiKey = Utils.getStringSetting(mContext, Global.PREF_EWAY_API_KEY);
			password = Utils.getStringSetting(mContext, Global.PREF_EWAY_PASSWORD);
		}
		else
		{
			RapidAPI.PublicAPIKey = "epk-1C040E3E-E7E3-4A91-BF85-93D96D27AD5C";
			RapidAPI.RapidEndpoint = "https://api.sandbox.ewaypayments.com/";
			apiKey = "F9802CaTan7PEJXzII++VGKeriPWTCxG1DnEnv8rgbDtx+CMZfl1A2itKry4xSi+vhlVR4";
			password = "zE1tlOcZ";
		}

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

		if(Utils.isFileExist(Global.DEFAULT_COMPANY_LOGO_FILEPATH))
		{
			Picasso.get().load(new File(Global.DEFAULT_COMPANY_LOGO_FILEPATH)).into((ImageView)findViewById(R.id.imgLogo));
		}
		else
		{
			findViewById(R.id.imgLogo).setVisibility(View.GONE);
		}

		String strClientName = "";
		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		List<DocTextInfo> docTextInfoList = dbAdapter.getDocTextListWithTypeLine(6, 1);
		dbAdapter.close();
		for(DocTextInfo docTextInfo : docTextInfoList)
		{
			if(TextUtils.isEmpty(strClientName))
			{
				strClientName = docTextInfo.strDocText.trim();
			}
			else
			{
				strClientName += "\n" + docTextInfo.strDocText.trim();
			}
		}
		if(TextUtils.isEmpty(strClientName))
		{
			findViewById(R.id.txtClientName).setVisibility(View.GONE);
		}
		else
		{
			((TextView)findViewById(R.id.txtClientName)).setText(strClientName);
		}
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

	private static void enableTls12OnPreLollipop(OkHttpClient client)
	{
		try
		{
			if (Build.VERSION.SDK_INT >= 16 && Build.VERSION.SDK_INT < 20)
			{
				SSLContext sc = SSLContext.getInstance("TLSv1.2");
				sc.init(null, null, null);
				client.setSslSocketFactory(new Tls12SocketFactory(sc.getSocketFactory()));
				ConnectionSpec cs = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
						.tlsVersions(TlsVersion.TLS_1_2)
						.build();
				List<ConnectionSpec> specs = new ArrayList<>();
				specs.add(cs);
				specs.add(ConnectionSpec.COMPATIBLE_TLS);
				specs.add(ConnectionSpec.CLEARTEXT);
				client.setConnectionSpecs(specs);
			}
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	private static OkHttpClient getRapidEwayClient() throws RapidConfigurationException {
		OkHttpClient client = new OkHttpClient();
		enableTls12OnPreLollipop(client);
		client.interceptors().add(new Interceptor(){

			public Response intercept(Interceptor.Chain chain) throws IOException {
				String credential = Credentials.basic(RapidAPI.PublicAPIKey, "");
				Request originalRequest = chain.request();
				Request.Builder requestBuilder = originalRequest.newBuilder().header("Authorization", credential).header("User-Agent", ";eWAY SDK Android 1.2").method(originalRequest.method(), originalRequest.body());
				Request request = requestBuilder.build();
				return chain.proceed(request);
			}
		});
		client.setConnectTimeout(3, TimeUnit.MINUTES);
		client.setReadTimeout(3, TimeUnit.MINUTES);
		return client;
	}

	private static OkHttpClient getEwayClient() throws RapidConfigurationException {
		OkHttpClient client = new OkHttpClient();
		enableTls12OnPreLollipop(client);
		client.interceptors().add(new Interceptor(){

			public Response intercept(Interceptor.Chain chain) throws IOException {
				String credential = Credentials.basic(apiKey, password);
				Request originalRequest = chain.request();
				Request.Builder requestBuilder = originalRequest.newBuilder().header("Authorization", credential).header("User-Agent", ";eWAY SDK Android 1.2").method(originalRequest.method(), originalRequest.body());
				Request request = requestBuilder.build();
				return chain.proceed(request);
			}
		});
		client.setConnectTimeout(3, TimeUnit.MINUTES);
		client.setReadTimeout(3, TimeUnit.MINUTES);
		return client;
	}


	protected static ApiInterface getApiClient() throws RapidConfigurationException {
		String baseUrl = RapidAPI.changeUrl(RapidAPI.RapidEndpoint);
		Retrofit retrofit = (new Retrofit.Builder()).baseUrl(baseUrl).addConverterFactory(GsonConverterFactory.create()).client(getEwayClient()).build();
		ApiInterface adapter = retrofit.create(ApiInterface.class);
		return adapter;
	}

	private static void errorCheck() throws RapidConfigurationException {
		if(RapidAPI.PublicAPIKey != null && !RapidAPI.PublicAPIKey.isEmpty()) {
			if(RapidAPI.RapidEndpoint != null && !RapidAPI.RapidEndpoint.isEmpty()) {
				String urlPattern = "^http(s{0,1})://[a-zA-Z0-9_/\\-\\.]+\\.([A-Za-z/]{2,5})[a-zA-Z0-9_/\\&\\?\\=\\-\\.\\~\\%]*";
				if(!RapidAPI.RapidEndpoint.matches(urlPattern)) {
					throw new RapidConfigurationException(new String[]{"S9992"});
				}
			} else {
				throw new RapidConfigurationException(new String[]{"S9990"});
			}
		} else {
			throw new RapidConfigurationException(new String[]{"S9991"});
		}
	}

	private static void transferAddressData(Address source, SubmitPaymentRequest.Addressed target) {
		if(source != null) {
			target.setStreet1(source.getStreet1());
			target.setStreet2(source.getStreet2());
			target.setCity(source.getCity());
			target.setState(source.getState());
			target.setPostalCode(source.getPostalCode());
			target.setCountry(source.getCountry());
		}

	}

	private static SubmitPaymentRequest.CardDetails transformCardDetails(com.eway.payment.sdk.android.beans.CardDetails source) {
		if(source != null) {
			SubmitPaymentRequest.CardDetails target = new SubmitPaymentRequest.CardDetails();
			target.setName(source.getName());
			target.setNumber(source.getNumber());
			target.setCVN(source.getCVN());
			target.setExpiryMonth(source.getExpiryMonth());
			target.setExpiryYear(source.getExpiryYear());
			target.setStartMonth(source.getStartMonth());
			target.setStartYear(source.getStartYear());
			return target;
		} else {
			return null;
		}
	}


	static SubmitPaymentRequest.Customer transformCustomer(com.eway.payment.sdk.android.beans.Customer source) {
		if(source != null) {
			SubmitPaymentRequest.Customer target = new SubmitPaymentRequest.Customer();
			target.setTitle(source.getTitle());
			target.setFirstName(source.getFirstName());
			target.setLastName(source.getLastName());
			target.setCompanyName(source.getCompanyName());
			target.setJobDescription(source.getJobDescription());
			transferAddressData(source.getAddress(), target);
			target.setTokenCustomerID(source.getTokenCustomerID());
			target.setEmail(source.getEmail());
			target.setPhone(source.getPhone());
			target.setMobile(source.getMobile());
			target.setComments(source.getComments());
			target.setFax(source.getFax());
			target.setUrl(source.getUrl());
			target.setCardDetails(transformCardDetails(source.getCardDetails()));
			return target;
		} else {
			return null;
		}
	}

	static SubmitPaymentRequest.ShippingAddress transformShippingAddress(ShippingDetails source) {
		if(source != null) {
			SubmitPaymentRequest.ShippingAddress target = new SubmitPaymentRequest.ShippingAddress();
			target.setFirstName(source.getFirstName());
			target.setLastName(source.getLastName());
			transferAddressData(source.getShippingAddress(), target);
			target.setPhone(source.getPhone());
			return target;
		} else {
			return null;
		}
	}

	static String nullSafeGetShippingMethod(Transaction transaction) {
		return transaction != null && transaction.getShippingDetails() != null && transaction.getShippingDetails().getShippingMethod() != null?transaction.getShippingDetails().getShippingMethod().toString():null;
	}

	static List<SubmitPaymentRequest.Options> transformOptions(ArrayList<String> source) {
		if(source == null) {
			return null;
		} else {
			ArrayList target = new ArrayList();
			Iterator var2 = source.iterator();

			while(var2.hasNext()) {
				String value = (String)var2.next();
				target.add(new SubmitPaymentRequest.Options(value));
			}

			return target;
		}
	}

	public static String getUniquePsuedoID() {
		String m_szDevIDShort = "35" + Build.BOARD.length() % 10 + Build.BRAND.length() % 10 + Build.CPU_ABI.length() % 10 + Build.DEVICE.length() % 10 + Build.MANUFACTURER.length() % 10 + Build.MODEL.length() % 10 + Build.PRODUCT.length() % 10;
		String serial = null;

		try {
			serial = Build.class.getField("SERIAL").get((Object)null).toString();
			return (new UUID((long)m_szDevIDShort.hashCode(), (long)serial.hashCode())).toString();
		} catch (Exception var3) {
			serial = "serial";
			return (new UUID((long)m_szDevIDShort.hashCode(), (long)serial.hashCode())).toString();
		}
	}

	static String nullSafeGetTransactionType(Transaction transaction) {
		return transaction.getTransactionType() != null?transaction.getTransactionType().toString():null;
	}

	public static SubmitPaymentRequest buildSubmitRequest(Transaction transaction) {
		SubmitPaymentRequest request = new SubmitPaymentRequest();
		request.setMethod("ProcessPayment");
		request.setCustomer(transformCustomer(transaction.getCustomer()));
		request.setShippingAddress(transformShippingAddress(transaction.getShippingDetails()));
		request.setShippingMethod(nullSafeGetShippingMethod(transaction));
		request.setItems(transaction.getLineItems());
		request.setOptions(transformOptions(transaction.getOptions()));
		request.setPayment(transaction.getPayment());
		request.setDeviceID(getUniquePsuedoID());
		request.setPartnerID(transaction.getPartnerID());
		request.setTransactionType(nullSafeGetTransactionType(transaction));
		return request;
	}

	public static String changeUrl(String url) throws RapidConfigurationException {
		errorCheck();
		String baseUrl = url;
		if("Production".equalsIgnoreCase(url)) {
			baseUrl = "https://api.ewaypayments.com";
		} else if("Sandbox".equalsIgnoreCase(url)) {
			baseUrl = "https://api.sandbox.ewaypayments.com";
		}

		return baseUrl;
	}

	protected static RapidAPI.rapidAndroidApi callPost() throws RapidConfigurationException {
		String baseUrl = changeUrl(RapidAPI.RapidEndpoint);
		Retrofit retrofit = (new Retrofit.Builder()).baseUrl(baseUrl).addConverterFactory(GsonConverterFactory.create()).client(getRapidEwayClient()).build();
		RapidAPI.rapidAndroidApi adapter = (RapidAPI.rapidAndroidApi)retrofit.create(RapidAPI.rapidAndroidApi.class);
		return adapter;
	}


	private static void errorList(int responseCode) throws RapidConfigurationException {
		if(responseCode == 401) {
			throw new RapidConfigurationException(new String[]{"S9993"});
		} else if(responseCode == 443) {
			throw new RapidConfigurationException(new String[]{"S9991"});
		} else {
			throw new RapidConfigurationException(new String[]{"S9992"});
		}
	}

	public static void asycSubmitPayment(Transaction transaction, final RapidAPI.RapidRecordingTransactionListener callback) {
		try {
			errorCheck();
			if(transaction == null) {
				throw new RapidConfigurationException(new String[]{"S9995"});
			}

			SubmitPaymentRequest ex1 = buildSubmitRequest(transaction);
			callPost().submitPayment(ex1).enqueue(new Callback() {
				@Override
				public void onResponse(retrofit.Response response, Retrofit retrofit) {
					if(response.isSuccess()) {
						if(((SubmitPayResponse)response.body()).getErrors() == null) {
							callback.onResponseReceivedSuccess((SubmitPayResponse)response.body());
						} else {
							callback.onResponseReceivedError(((SubmitPayResponse)response.body()).getErrors());
						}
					} else {
						try {
							errorList(response.code());
						} catch (RapidConfigurationException var6) {
							RapidConfigurationException e = var6;

							try {
								callback.onResponseReceivedException(new SubmitPayResponse(e.getErrorCodes(), (String)null, (String)null));
							} catch (RapidConfigurationException var5) {
								var5.printStackTrace();
							}
						}
					}
				}

				public void onFailure(Throwable t) {
					callback.onResponseReceivedFailure(t.getMessage());
				}
			});
		} catch (RapidConfigurationException var5) {
			RapidConfigurationException ex = var5;

			try {
				callback.onResponseReceivedException(new SubmitPayResponse(ex.getErrorCodes(), (String)null, (String)null));
			} catch (RapidConfigurationException var4) {
				var4.printStackTrace();
			}
		}

	}

	private static String nullSafeGetLocale(String Language) {
		return Language != null?Language:Locale.getDefault().toString();
	}

	private static ArrayList<String> parseErrorCodeList(String ErrorCodes) {
		return ErrorCodes != null? newArrayList(ErrorCodes.trim().replaceAll("\\s+", "").split(",")): newArrayList(new String[0]);
	}

	public static <T> ArrayList<T> newArrayList(T... tValues) {
		ArrayList result = new ArrayList();
		Object[] var2 = tValues;
		int var3 = tValues.length;

		for(int var4 = 0; var4 < var3; ++var4) {
			Object value = var2[var4];
			result.add(value);
		}

		return result;
	}

	public static void asycUserMessage(String Language, String ErrorCodes, final RapidAPI.RapidUserMessageListener callback) throws RapidConfigurationException {
		callPost().codeLookUp(new CodeLookupRequest(nullSafeGetLocale(Language), parseErrorCodeList(ErrorCodes))).enqueue(new Callback() {

			@Override
			public void onResponse(retrofit.Response response, Retrofit retrofit) {
				if(response.isSuccess()) {
					if(((CodeLookupResponse)response.body()).getErrors() == null) {
						ArrayList e = new ArrayList();
						Iterator e1x = ((CodeLookupResponse)response.body()).getCodeDetails().iterator();

						while(e1x.hasNext()) {
							CodeDetail codeDetail = (CodeDetail)e1x.next();
							e.add(codeDetail.getDisplayMessage());
						}

						callback.onResponseReceivedSuccess(new UserMessageResponse(e, (String)null));
					} else {
						callback.onResponseReceivedError(((CodeLookupResponse)response.body()).getErrors());
					}
				} else {
					try {
						errorList(response.code());
					} catch (RapidConfigurationException var7) {
						RapidConfigurationException e1 = var7;

						try {
							callback.onResponseReceivedException(new UserMessageResponse((ArrayList)null, e1.getErrorCodes()));
						} catch (RapidConfigurationException var6) {
							var6.printStackTrace();
						}
					}
				}
			}

			public void onFailure(Throwable t) {
				callback.onResponseReceivedFailure(t.getMessage());
			}
		});
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
		}
		String strCardNo = strCardNo1 + strCardNo2 + strCardNo3 + strCardNo4;

		//new eWayPaymentTask().execute(new String[]{strCardName, strCardNo, strCVN, String.format(Locale.ENGLISH, "%02d", nExpireMonth), String.format(Locale.ENGLISH, "%04d", 2000 + nExpireYear)});
		showProgressDialog(Str.Loading[Global.LANG]);
		String strResult = Str.Unknown_Error[Global.LANG];

		Transaction transaction = new Transaction();
		Payment payment = new Payment();
		CardDetails cardDetails = new CardDetails();
		Customer customer = new Customer();
		int nTotalAmount = Math.round(mAmount * 100);
		payment.setTotalAmount(nTotalAmount);

		/*if (Utils.getIntSetting(mContext, Global.PREF_EWAY_CARD_PROC) == 1) {
			payment.setCurrencyCode("AUD");
		} else if (Utils.getIntSetting(mContext, Global.PREF_EWAY_CARD_PROC) == 2) {
			payment.setCurrencyCode("NZD");
		}*/
		payment.setCurrencyCode("AUD");

		cardDetails.setName(strCardName);
		cardDetails.setNumber(strCardNo);
		cardDetails.setCVN(strCVN);
		cardDetails.setExpiryMonth(String.format(Locale.ENGLISH, "%02d", nExpireMonth));
		cardDetails.setExpiryYear(String.format(Locale.ENGLISH, "%04d", 2000 + nExpireYear));
		customer.setCardDetails(cardDetails);

		transaction.setTransactionType(TransactionType.Purchase);
		transaction.setPayment(payment);
		transaction.setCustomer(customer);
		if( Global._DEBUG_ )
		{
			try
			{
				Toast.makeText(mContext, "Expiry Month :" + transaction.getCustomer().getCardDetails().getExpiryMonth() + "\n" + "Expiry Year :" + transaction.getCustomer().getCardDetails().getExpiryYear(), Toast.LENGTH_LONG).show();
			}catch (Exception e)
			{
				e.printStackTrace();
			}
		}


		SubmitPaymentRequest request = buildSubmitRequest(transaction);
		try {
			getApiClient().submitPayment(request).enqueue(new Callback<TransactionResponse>() {
				@Override
				public void onResponse(retrofit.Response<TransactionResponse> response, Retrofit retrofit) {
					try
					{
						String strResponseCode = response.body().getResponseCode();
						if(strResponseCode.equals("00") || strResponseCode.equals("08")
								|| strResponseCode.equals("10") || strResponseCode.equals("11") ||
								strResponseCode.equals("16"))
						{
							hideProgressDialog();
							Utils.printDebug(mContext, Str.SuccessToPay[Global.LANG]);
							mReturnCode = Global.DLG_RETURN_RIGHT;
							mTransactionID = response.body().getTransactionID();
							dismiss();
						}
						else
						{
							String strFullResponseCode = response.body().getResponseMessage();
							try
							{
								asycUserMessage(Locale.getDefault().getLanguage(), strFullResponseCode, new RapidAPI.RapidUserMessageListener(){

									@Override
									public void onResponseReceivedSuccess(UserMessageResponse userMessageResponse) {
										hideProgressDialog();
										String result = errorHandler(userMessageResponse.getErrorMessages());
										try
										{
											Toast.makeText(mContext, result, Toast.LENGTH_SHORT).show();
										}catch (Exception e)
										{
											e.printStackTrace();
										}
									}

									@Override
									public void onResponseReceivedError(String s) {
										hideProgressDialog();
										try
										{
											Toast.makeText(mContext, s, Toast.LENGTH_SHORT).show();
										}catch (Exception e)
										{
											e.printStackTrace();
										}
									}

									@Override
									public void onResponseReceivedFailure(String s) {
										hideProgressDialog();
										try
										{
											Toast.makeText(mContext, s, Toast.LENGTH_SHORT).show();
										}catch (Exception e)
										{
											e.printStackTrace();
										}
									}

									@Override
									public void onResponseReceivedException(UserMessageResponse userMessageResponse) {
										hideProgressDialog();
										try
										{
											Toast.makeText(mContext, userMessageResponse.getErrors(), Toast.LENGTH_SHORT).show();
										}catch (Exception e)
										{
											e.printStackTrace();
										}
									}
								});
							}catch (Exception e)
							{
								hideProgressDialog();
								e.printStackTrace();
								try
								{
									if( e.getMessage() != null )
									{
										Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
									}
									else
									{
										Toast.makeText(mContext, Str.network_connection_error[Global.LANG], Toast.LENGTH_SHORT).show();
									}
								}catch (Exception e1)
								{
									e1.printStackTrace();
								}

							}
						}
					}catch(Exception e)
					{
						hideProgressDialog();
						e.printStackTrace();
						try {
							if (e.getMessage() != null) {
								Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
							} else {
								Toast.makeText(mContext, Str.network_connection_error[Global.LANG], Toast.LENGTH_SHORT).show();

							}
						}catch (Exception e1)
						{
							e1.printStackTrace();
						}
					}
				}

				@Override
				public void onFailure(Throwable t) {
					try
					{
						Toast.makeText(mContext, Str.eway_setting_error[Global.LANG], Toast.LENGTH_SHORT).show();
					}catch (Exception e)
					{
						e.printStackTrace();
					}
					hideProgressDialog();
				}
			});
		} catch (RapidConfigurationException e) {
			e.printStackTrace();
			try
			{
				Toast.makeText(mContext, Str.eway_setting_error[Global.LANG], Toast.LENGTH_SHORT).show();
			}catch (Exception e1)
			{
				e1.printStackTrace();
			}
			hideProgressDialog();
		}

	}
	private String errorHandler(List<String> response) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < response.size(); i++) {
			if( i != 0 )
				result.append("\n");
			result.append(response.get(i));
			i++;
		}
		return result.toString();
	}

	public interface ApiInterface {
		@GET("/Transaction/{AccessCode}")
		Call<EWayTransactionResponse> getResponse(@Path("AccessCode") String accessCode);

		@GET("/Transaction/{TransactionID}/Refund")
		Call<EWayRefundResponse> refund(@Path("TransactionID") String transactionID);

		@Headers({"Content-Type: application/json"})
		@POST("/transaction")
		Call<TransactionResponse> submitPayment(@Body SubmitPaymentRequest var1);
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

