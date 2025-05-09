package dsd.dsdvansales.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import dsd.dsdvansales.R;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;

public class InputReceiverNameDialog extends Dialog {

	public String receivername = "";
	private Context mContext = null;
	public String mStrCustomerName = "";
	public int mReturnCode = 0;

	public InputReceiverNameDialog(Context context) {
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

		setContentView(R.layout.dialog_input_receivername);

		((TextView)findViewById(R.id.txtTitle)).setText(Str.receiver_name[Global.LANG]);
		((TextView)findViewById(R.id.txtCaption)).setText(Str.input_receiver_name[Global.LANG]);
		((Button)findViewById(R.id.btnOkay)).setText(Str.okay[Global.LANG]);

		findViewById(R.id.btnOkay).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				receivername = ((EditText)findViewById(R.id.editReceiverName)).getText().toString();
				dismiss();
			}
		});
	}
}