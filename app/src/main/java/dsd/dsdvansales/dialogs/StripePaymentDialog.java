package dsd.dsdvansales.dialogs;


import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.TextView;

import com.stripe.android.Stripe;
//import com.stripe.android.TokenCallback;
//import com.stripe.android.exception.APIConnectionException;
//import com.stripe.android.exception.APIException;
//import com.stripe.android.exception.AuthenticationException;
//import com.stripe.android.exception.CardException;
//import com.stripe.android.exception.InvalidRequestException;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.Customer;
import com.stripe.model.PaymentIntent;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import dsd.dsdvansales.R;
import dsd.dsdvansales.api.StripeApiClient;
import dsd.dsdvansales.db.DbAdapter;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.model.CustomerAddInfo;
import dsd.dsdvansales.model.StripeChargeResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StripePaymentDialog extends Dialog {

	private Context mContext = null;
	private EditText mEditAmount = null;

	public int mReturnCode = 0;
	public String mTransactionID = "";
	public String mCustomerID = "";
	public float mAmount = 0;
	public String mStrReference = "";


	public StripePaymentDialog(Context context) {
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

		setContentView(R.layout.dialog_stripe_payment);

		((TextView) findViewById(R.id.txtTitle)).setText(Str.StripeCardPayment[Global.LANG].toUpperCase());
		((TextView) findViewById(R.id.lblCardName)).setText(Str.NameOnCard[Global.LANG]);
		((TextView) findViewById(R.id.lblCardNo)).setText(Str.CardNumber[Global.LANG]);
		((TextView) findViewById(R.id.lblExpire)).setText(Str.ExpiryDate[Global.LANG]);
		((TextView) findViewById(R.id.lblAmount)).setText(Str.Amount[Global.LANG]);

		((Button) findViewById(R.id.btnLeft)).setText(Str.Return[Global.LANG]);
		((Button) findViewById(R.id.btnMiddle)).setText(Str.Manual[Global.LANG]);
		((Button) findViewById(R.id.btnRight)).setText(Str.Pay[Global.LANG]);


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

	private void doPayment() {
		final String strCardName = ((EditText) findViewById(R.id.editCardName)).getText().toString();
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
		final int nExpireMonth = Utils.parseIntFromString(((EditText) findViewById(R.id.editExpiryMonth)).getText().toString());
		if (nExpireMonth == 0) {
			Utils.printDebug(mContext, Str.PleaseInputExpiryMonthCorrectly[Global.LANG]);
			return;
		}
		final int nExpireYear = Utils.parseIntFromString(((EditText) findViewById(R.id.editExpiryYear)).getText().toString());
		if (nExpireYear < (new Date().getYear() - 100)) {
			Utils.printDebug(mContext, Str.PleaseInputExpiryYearCorrectly[Global.LANG]);
			return;
		}
		final String strCVN = ((EditText) findViewById(R.id.editCVN)).getText().toString();
		if (strCVN.length() == 0) {
			Utils.printDebug(mContext, Str.PleaseInputCVV[Global.LANG]);
			return;
		}
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

		final String strCardNo = strCardNo1 + "-" + strCardNo2 + "-" + strCardNo3 + "-" + strCardNo4;
		String strCurrency = Utils.getStringSetting(mContext, Global.PREF_CURRENCY);
		if(TextUtils.isEmpty(strCurrency))
		{
			strCurrency = "AUD";
		}


//
//		final Card card = new Card.Builder(strCardNo, nExpireMonth, Utils.parseIntFromString("20" + nExpireYear), strCVN).
//				currency(strCurrency).
//				name(strCardName).
//				build();
//
//
//		boolean validation = card.validateCard();
//		if(validation)
//		{
//			final String finalStrCurrency = strCurrency;
//			showProgressDialog(Str.Loading[Global.LANG]);
//			try {
//				final String finalStrCurrency1 = strCurrency;
//				new Stripe(mContext, strPubKey).createToken(card, new TokenCallback() {
//					@Override
//					public void onSuccess(Token token) {
//						final String strToken = token.getId();
//						new Thread(){
//                            @Override
//                            public void run() {
//                                //Charge Money
//                                com.stripe.Stripe.apiKey = strSecKey;
//                                Map<String, Object> params = new HashMap<>();
//                                params.put("amount", ((int)(mAmount * 100)));
//                                params.put("currency", finalStrCurrency);
//                                params.put("source", strToken);
//                                //params.put("description", "Charge For " + Utils.getStringSetting(mContext, Global.PREF_LOGIN_USER_NAME));
//								params.put("description", "Inv No : " + mStrReference);
//                                try {
//                                    final Charge charge = Charge.create(params);
//                                    if(charge.getStatus().equals("succeeded"))
//                                    {
//                                        new Handler(Looper.getMainLooper()).post(new Runnable() {
//                                            @Override
//                                            public void run() {
//                                                hideProgressDialog();
//
//                                                //Ask User to save this or not
//												try
//												{
//													final MessageDialog msgDialog = new MessageDialog(mContext);
//													msgDialog.setCancelable(false);
//													String strCardKind = card.getBrand();
//													if(!strCardKind.toLowerCase().endsWith("card"))
//													{
//														strCardKind += "Card";
//													}
//													msgDialog.mStrMessage = String.format(Locale.ENGLISH, Str.SaveThisCardForUse[Global.LANG], strCardKind, Global.gCurCustomerInfo.strName);
//													msgDialog.mStrLeft = Str.No[Global.LANG];
//													msgDialog.mStrRight = Str.Yes[Global.LANG];
//													msgDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
//														@Override
//														public void onDismiss(DialogInterface dialog) {
//															if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
//																showProgressDialog(Str.Loading[Global.LANG]);
//																DbAdapter dbAdapter = new DbAdapter(mContext);
//																dbAdapter.open();
//																final List<CustomerAddInfo> customerAddInfoList = dbAdapter.getCustAddInfoList(Global.gCurCustomerInfo.nCustNo, Global.gCurCustomerInfo.nChainNo, "50");
//																dbAdapter.close();
//
//																new Thread()
//																{
//																	@Override
//																	public void run() {
//																		Map<String, Object> customerParams = new HashMap<>();
//																		//Create Card Token again.
//																	/*Card card = new Card.Builder(strCardNo, nExpireMonth, Utils.parseIntFromString("20" + nExpireYear), strCVN).
//																			currency(finalStrCurrency1).
//																			name(strCardName).
//																			build();*/
//																		try {
//																			Token token = new Stripe(mContext, strPubKey).createTokenSynchronous(card);
//																			customerParams.put("source", token.getId());
//																			customerParams.put("name", strCardName);
//																			Map<String, String> metaData = new HashMap<>();
//																			metaData.put("Chain No", String.valueOf(Global.gCurCustomerInfo.nChainNo));
//																			metaData.put("Customer No", String.valueOf(Global.gCurCustomerInfo.nCustNo));
//																			metaData.put("Customer Name", String.valueOf(Global.gCurCustomerInfo.strName));
//																			customerParams.put("metadata", metaData);
//																			//customerParams.put("address", Global.gCurCustomerInfo.strAddress1);
//																			if(customerAddInfoList.size() == 1)
//																			{
//																				try {
//																					Customer customer = Customer.retrieve(customerAddInfoList.get(0).strInfo);
//																					customer.update(customerParams);
//																					new Handler(Looper.getMainLooper()).post(new Runnable() {
//																						@Override
//																						public void run() {
//																							hideProgressDialog();
//																							//No need to update CustomerFM flag
//																							mTransactionID = charge.getId();
//																							mReturnCode = Global.DLG_RETURN_RIGHT;
//																							dismiss();
//																							Utils.printDebug(mContext, Str.SaveCardSuccess[Global.LANG]);
//																						}
//																					});
//																				} catch (StripeException e) {
//																					e.printStackTrace();
//																					new Handler(Looper.getMainLooper()).post(new Runnable() {
//																						@Override
//																						public void run() {
//																							hideProgressDialog();
//																							mTransactionID = charge.getId();
//																							mReturnCode = Global.DLG_RETURN_RIGHT;
//																							dismiss();
//																							Utils.printDebug(mContext, Str.FailedSaveCard[Global.LANG]);
//																						}
//																					});
//
//																				}
//																			}
//																			else
//																			{
//																				try {
//																					final Customer customer = Customer.create(customerParams);
//																					new Handler(Looper.getMainLooper()).post(new Runnable() {
//																						@Override
//																						public void run() {
//																							//Save Card
//																							hideProgressDialog();
//
//																							mCustomerID = customer.getId();
//																							mTransactionID = charge.getId();
//																							mReturnCode = Global.DLG_RETURN_RIGHT;
//																							dismiss();
//																							Utils.printDebug(mContext, Str.SaveCardSuccess[Global.LANG]);
//																						}
//																					});
//																				} catch (StripeException e) {
//																					e.printStackTrace();
//																					new Handler(Looper.getMainLooper()).post(new Runnable() {
//																						@Override
//																						public void run() {
//																							hideProgressDialog();
//																							mTransactionID = charge.getId();
//																							mReturnCode = Global.DLG_RETURN_RIGHT;
//																							dismiss();
//																							Utils.printDebug(mContext, Str.FailedSaveCard[Global.LANG]);
//																						}
//																					});
//																				}
//																			}
//																		} catch (Exception e) {
//																			e.printStackTrace();
//																		}
//
//
//																	}
//																}.start();
//															} else if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
//																mTransactionID = charge.getId();
//																mReturnCode = Global.DLG_RETURN_RIGHT;
//																dismiss();
//
//															}
//														}
//													});
//													Utils.printDebug(mContext, Str.SuccessToPay[Global.LANG]);
//													msgDialog.show();
//												}catch (Exception e)
//												{
//													e.printStackTrace();
//												}
//                                            }
//                                        });
//                                    }
//                                    else
//                                    {
//                                        new Handler(Looper.getMainLooper()).post(new Runnable() {
//                                            @Override
//                                            public void run() {
//                                                hideProgressDialog();
//                                                Utils.printDebug(mContext, Str.PaymentError[Global.LANG]);
//                                            }
//                                        });
//                                    }
//                                } catch (final StripeException e) {
//                                	new Handler(Looper.getMainLooper()).post(new Runnable() {
//										@Override
//										public void run() {
//											hideProgressDialog();
//											Utils.printDebug(mContext, Str.PaymentDeclined[Global.LANG]);
//
//										}
//									});
//									e.printStackTrace();
//                                }
//                            }
//                        }.start();
//
//					}
//
//					@Override
//					public void onError(Exception e) {
//						hideProgressDialog();
//						Utils.printDebug(mContext, e.getLocalizedMessage());
//						return;
//					}
//				});
//			} catch (AuthenticationException e) {
//				hideProgressDialog();
//				Utils.printDebug(mContext, e.getLocalizedMessage());
//				return;
//			}
//		}
//		else if(!card.validateNumber())
//		{
//			Utils.printDebug(mContext, Str.PleaseInputCardNumberCorrectly[Global.LANG]);
//			return;
//		}
//		else if(!card.validateExpMonth())
//		{
//			Utils.printDebug(mContext, Str.PleaseInputExpiryMonthCorrectly[Global.LANG]);
//			return;
//		}
//		else if(!card.validateExpiryDate())
//		{
//			Utils.printDebug(mContext, Str.PleaseInputExpiryYearCorrectly[Global.LANG]);
//			return;
//		}
//		else if(!card.validateCVC())
//		{
//			Utils.printDebug(mContext, Str.PleaseInputCVVCorrectly[Global.LANG]);
//			return;
//		}
//		else
//		{
//			Utils.printDebug(mContext, Str.PleaseInputCardInformationCorrectly[Global.LANG]);
//			return;
//		}


		//new eWayPaymentTask().execute(new String[]{strCardName, strCardNo, strCVN, String.format(Locale.ENGLISH, "%02d", nExpireMonth), String.format(Locale.ENGLISH, "%04d", 2000 + nExpireYear)});

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

