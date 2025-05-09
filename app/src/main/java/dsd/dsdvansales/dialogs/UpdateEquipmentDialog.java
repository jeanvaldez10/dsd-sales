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

import dsd.dsdvansales.R;
import dsd.dsdvansales.global.Global;

public class UpdateEquipmentDialog extends Dialog {

	private EditText mEditInput = null;

	public int mReturnCode = 0;
	public String mStrTitle = "";
	public String mStrHint = "";
	public String mStrLeft = "";
	public String mStrRight = "";
	public String mStrInputed = "";

	public UpdateEquipmentDialog(Context context) {
		super(context, android.R.style.Theme_Translucent_NoTitleBar);
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

		setContentView(R.layout.dialog_update_equipment);

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

		mEditInput.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				mStrInputed = s.toString();
			}
		});

		findViewById(R.id.btnLeft).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mReturnCode = Global.DLG_RETURN_LEFT;
				dismiss();
			}
		});

		findViewById(R.id.btnRight).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mReturnCode = Global.DLG_RETURN_RIGHT;
				dismiss();
			}
		});
	}

	private void doEnter() {
		findViewById(R.id.btnRight).performClick();
	}
}

