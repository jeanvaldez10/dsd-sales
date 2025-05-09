package dsd.dsdvansales.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
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

public class InputBatchNumDialog extends Dialog {

	private Context mContext = null;
	private EditText mEditInput = null;

	public int mReturnCode = 0;
	public String mStrTitle = "";
	public String mStrHint = "";
	public String mStrLeft = "";
	public String mStrRight = "";
	public String mStrInputed = "";
	public boolean mIsCharacter = false;

	public boolean m_bClicked = false;
	public InputBatchNumDialog(Context context) {
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

		setContentView(R.layout.dialog_input_batch);

		if (mStrTitle.length() > 0) {
			findViewById(R.id.txtTitle).setVisibility(View.VISIBLE);
			((TextView) findViewById(R.id.txtTitle)).setText(mStrTitle);
		} else {
			findViewById(R.id.txtTitle).setVisibility(View.GONE);
		}
		if (mStrLeft.length() > 0) {
			findViewById(R.id.btnLeft).setVisibility(View.VISIBLE);
			((Button) findViewById(R.id.btnLeft)).setText(mStrLeft);
		} else {
			findViewById(R.id.btnLeft).setVisibility(View.GONE);
		}
		if (mStrRight.length() > 0) {
			findViewById(R.id.btnRight).setVisibility(View.VISIBLE);
			((Button) findViewById(R.id.btnRight)).setText(mStrRight);
		} else {
			findViewById(R.id.btnRight).setVisibility(View.GONE);
		}

		mEditInput = ((EditText) findViewById(R.id.editInput));
		mEditInput.setText(mStrInputed);
		mEditInput.setHint(mStrHint);
		mEditInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					doEnter();
					return true;
				}
				return false;
			}
		});

		if (mIsCharacter == true) {
			findViewById(R.id.txtCharacters).setVisibility(View.VISIBLE);
			((TextView) findViewById(R.id.txtCharacters)).setText(String.format(Locale.ENGLISH, Str.D_Characters[Global.LANG], mEditInput.getText().toString().length()));
			mEditInput.addTextChangedListener(new TextWatcher() {
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				}

				@Override
				public void afterTextChanged(Editable s) {
					((TextView) findViewById(R.id.txtCharacters)).setText(String.format(Locale.ENGLISH, Str.D_Characters[Global.LANG], mEditInput.getText().toString().length()));
				}
			});
		} else {
			findViewById(R.id.txtCharacters).setVisibility(View.GONE);
		}

		findViewById(R.id.btnLeft).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if( m_bClicked ) return;
				m_bClicked = true;
				doEnter();
				m_bClicked = false;
			}
		});

		findViewById(R.id.btnRight).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if( m_bClicked ) return;
				m_bClicked = true;
				mReturnCode = Global.DLG_RETURN_RIGHT;
				dismiss();
				m_bClicked = false;
			}
		});
	}

	private void doEnter() {
		mStrInputed = mEditInput.getText().toString();
		if (mIsCharacter == false) {
			mReturnCode = Global.DLG_RETURN_LEFT;
			dismiss();
		} else {
			if (mStrInputed.length() == 0) {
				Utils.printDebug(mContext, Str.Please_input_the_note[Global.LANG]);
			} else if (mStrInputed.length() > 20) {
				Utils.printDebug(mContext, Str.Please_input_the_note_below_20_characters[Global.LANG]);
			} else {
				mReturnCode = Global.DLG_RETURN_LEFT;
				dismiss();
			}
		}
	}
}

