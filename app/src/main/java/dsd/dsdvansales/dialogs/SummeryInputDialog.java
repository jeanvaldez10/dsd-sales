package dsd.dsdvansales.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Locale;

import dsd.dsdvansales.R;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;

public class SummeryInputDialog extends Dialog {

	private Context mContext = null;
	private EditText mEditInput = null;
	private EditText mEditOrderTemp = null;

	public int mReturnCode = 0;
	public String mStrInputed = "";

	public boolean m_bClicked = false;

	public String ve = "+";

	public SummeryInputDialog(Context context) {
		super(context, android.R.style.Theme_Translucent_NoTitleBar);
		this.mContext = context;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
		lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
		lpWindow.dimAmount = 0.9f;
		getWindow().setAttributes(lpWindow);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.dialog_summery_input);
		
		findViewById(R.id.txtTitle).setVisibility(View.VISIBLE);
		((TextView) findViewById(R.id.txtTitle)).setText(Str.OrderNotes[Global.LANG]);
		
		findViewById(R.id.btnLeft).setVisibility(View.VISIBLE);
		((Button) findViewById(R.id.btnLeft)).setText(Str.Return[Global.LANG]);
		
		findViewById(R.id.btnRight).setVisibility(View.VISIBLE);
		((Button) findViewById(R.id.btnRight)).setText(Str.Confirm[Global.LANG]);

		String strTempFmt = Utils.getStringSetting(mContext, Global.PREF_TEMPFMT);
		if(strTempFmt.startsWith("-"))
		{
			ve = "-";
		}
		else {
			ve = "+";
		}
		String unit = "C°";
		if(strTempFmt.endsWith("F"))
		{
			unit = "F°";
		}
		((TextView)findViewById(R.id.order_temp)).setText(Str.DeliveryTemperature[Global.LANG] + " " + unit);

		findViewById(R.id.plus).setOnClickListener(view -> {
			ve = "+";
			String strTemp = mEditOrderTemp.getText().toString();
			if(!strTemp.startsWith("+"))
			{
				mEditOrderTemp.setText(ve + strTemp.substring(1));
			}
		});
		findViewById(R.id.minus).setOnClickListener(view -> {
			ve = "-";
			String strTemp = mEditOrderTemp.getText().toString();
			if(!strTemp.startsWith("-"))
			{
				mEditOrderTemp.setText(ve + strTemp.substring(1));
			}
		});
		int nStart = 0;
		mEditOrderTemp = findViewById(R.id.editOrderTemp);
		try
		{
			String[] strText = mStrInputed.split("  ");
			if( strText.length >= 2 )
			{
				String strOrderTemp = mStrInputed.split("  ")[0];
				nStart = strOrderTemp.length();
				if(strOrderTemp.startsWith("-") || strOrderTemp.startsWith("+"))
				{
					mEditOrderTemp.setText(strOrderTemp);
					if(strOrderTemp.startsWith("-"))
					{
						ve = "-";
					}
					else {
						ve = "+";
					}
				}
				else {
					mEditOrderTemp.setText(ve + strOrderTemp);
				}
			}
			else
			{
				mEditOrderTemp.setText(ve + "0.0");
			}
		}catch(Exception e)
		{
			nStart = 0;
			e.printStackTrace();
		}
		
		mEditOrderTemp.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				if (mEditOrderTemp.getTag() == null) {
					setFormattedAmount(s.toString());
				}
			}
		});
		mEditOrderTemp.post(new Runnable() {
			public void run() {
				mEditOrderTemp.setSelection(mEditOrderTemp.getText().toString().length());
				mEditOrderTemp.postDelayed(this, 100);
			}
		});
		
		mEditInput = findViewById(R.id.editInput);
		try
		{
			mEditInput.setText(mStrInputed.substring(nStart));
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		mEditInput.setHint(Str.Notes[Global.LANG]);
		mEditInput.setOnEditorActionListener((v, actionId, event) -> {
			if (actionId == EditorInfo.IME_ACTION_DONE) {
				doEnter();
				return true;
			}
			return false;
		});

		
		findViewById(R.id.txtCharacters).setVisibility(View.VISIBLE);
		int nAvailableCount = 46;
		((TextView) findViewById(R.id.txtCharacters)).setText(String.format(Locale.ENGLISH, Str.D_Characters[Global.LANG], nAvailableCount - mEditInput.getText().toString().length()));
		mEditInput.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				int nAvailableCount = 46;
				((TextView) findViewById(R.id.txtCharacters)).setText(String.format(Locale.ENGLISH, Str.D_Characters[Global.LANG], 46 - mEditInput.getText().toString().length()));
			}
		});
		

		findViewById(R.id.btnLeft).setOnClickListener(v -> {
			if( m_bClicked ) return;
			m_bClicked = true;
			mReturnCode = Global.DLG_RETURN_LEFT;
			dismiss();
			m_bClicked = false;

		});

		findViewById(R.id.btnRight).setOnClickListener(v -> {
			if( m_bClicked ) return;
			m_bClicked = true;
			doEnter();
			m_bClicked = false;
		});
	}

	private void setFormattedAmount(String s)
	{
		String strArray[] = s.substring(1).split("\\.");
		String strVal = "";
		for (int i=0; i<strArray.length; i++) {
			strVal += strArray[i];
		}
		float value =  (Utils.parseFloatFromString(strVal) / 10f);

		mEditOrderTemp.setTag("From App");
		String strCash = String.format(Locale.ENGLISH, "%s%.1f", ve, value);
		mEditOrderTemp.setText(strCash);
		mEditOrderTemp.setSelection(mEditOrderTemp.getText().toString().length());
		mEditOrderTemp.setTag(null);
	}
	
	private void doEnter() {
		mStrInputed = mEditOrderTemp.getText().toString() + "  " + mEditInput.getText().toString();
		if (mEditOrderTemp.getText().toString().length() == 0 || mEditInput.getText().toString().length() == 0) {
			Utils.printDebug(mContext, Str.Please_input_the_note[Global.LANG]);
		} else if (mStrInputed.length() > 52) {
			Utils.printDebug(mContext, Str.Please_input_the_note_below_50_characters[Global.LANG]);
		} else {
			mReturnCode = Global.DLG_RETURN_RIGHT;
			dismiss();
		}
	}
}

