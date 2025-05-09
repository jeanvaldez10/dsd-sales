package dsd.dsdvansales.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import dsd.dsdvansales.R;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;

public class ContactDialog extends Dialog {

	private Context mContext = null;
	public String mStrCustomerName = "";
	public int mReturnCode = 0;

	public ContactDialog(Context context) {
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

		setContentView(R.layout.dialog_contact);

		((TextView) findViewById(R.id.txtCustomerName)).setText(mStrCustomerName);

		Global.LANG = Utils.getIntSetting(mContext, Global.PREF_CUR_LANG);
		((Button) findViewById(R.id.btnTelephone)).setText(Str.Telephone[Global.LANG]);
		((Button) findViewById(R.id.btnSMSMessage)).setText(Str.SMS_Message[Global.LANG]);
		((Button) findViewById(R.id.btnEmail)).setText(Str.Email[Global.LANG]);
		((Button) findViewById(R.id.btnReturn)).setText(Str.Return[Global.LANG]);

		findViewById(R.id.btnTelephone).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mReturnCode = Global.CONTACT_RETURN_TELEPHONE;
				dismiss();
			}
		});

		findViewById(R.id.btnSMSMessage).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mReturnCode = Global.CONTACT_RETURN_SMS;
				dismiss();
			}
		});

		findViewById(R.id.btnEmail).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mReturnCode = Global.CONTACT_RETURN_EMAIL;
				dismiss();
			}
		});

		findViewById(R.id.btnReturn).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mReturnCode = 0;
				dismiss();
			}
		});
	}
}