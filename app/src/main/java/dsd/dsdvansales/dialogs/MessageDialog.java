package dsd.dsdvansales.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import dsd.dsdvansales.R;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;

public class MessageDialog extends Dialog {

	public int mReturnCode = 0;
	public String mStrMessage = "";
	public String mStrTitle = "";
	public String mStrLeft = "";
	public String mStrMiddle = "";
	public String mStrRight = "";
	public boolean isCenterMessage = false;
	public boolean mIsFailed = false;
	public String mStrCustomerName = "";

	public MessageDialog(Context context) {
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

		setContentView(R.layout.dialog_message);

		if (mStrTitle.length() == 0) {
			findViewById(R.id.txtTitle).setVisibility(View.GONE);
		} else {
			findViewById(R.id.txtTitle).setVisibility(View.VISIBLE);
			((TextView) findViewById(R.id.txtTitle)).setText(mStrTitle);
		}
		if (mIsFailed == false) {
			findViewById(R.id.txtFailed).setVisibility(View.GONE);
		} else {
			findViewById(R.id.txtFailed).setVisibility(View.VISIBLE);
			((TextView) findViewById(R.id.txtFailed)).setText(Str.FAILED[Global.LANG]);
		}
		if (mStrCustomerName.length() > 0) {
			((TextView) findViewById(R.id.txtCustomerName)).setText(mStrCustomerName);
			findViewById(R.id.txtCustomerName).setVisibility(View.VISIBLE);
		} else {
			findViewById(R.id.txtCustomerName).setVisibility(View.GONE);
		}

		((TextView) findViewById(R.id.txtMessage)).setText(mStrMessage);

		if(isCenterMessage)
		{
			((TextView) findViewById(R.id.txtMessage)).setGravity(Gravity.CENTER);
		}

		if (mStrMiddle.length() == 0) {
			findViewById(R.id.btnMiddle).setVisibility(View.GONE);
		} else {
			findViewById(R.id.btnMiddle).setVisibility(View.VISIBLE);
			((Button) findViewById(R.id.btnMiddle)).setText(mStrMiddle);
		}
		if (mStrLeft.length() == 0) {
			findViewById(R.id.btnLeft).setVisibility(View.GONE);
		} else {
			findViewById(R.id.btnLeft).setVisibility(View.VISIBLE);
			((Button) findViewById(R.id.btnLeft)).setText(mStrLeft);
		}
		if (mStrRight.length() == 0) {
			findViewById(R.id.btnRight).setVisibility(View.GONE);
		} else {
			findViewById(R.id.btnRight).setVisibility(View.VISIBLE);
			((Button) findViewById(R.id.btnRight)).setText(mStrRight);
		}

		findViewById(R.id.btnMiddle).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mReturnCode = Global.DLG_RETURN_MIDDLE;
				dismiss();
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
}

