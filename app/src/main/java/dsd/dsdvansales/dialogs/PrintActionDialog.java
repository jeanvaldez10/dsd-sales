package dsd.dsdvansales.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.*;
import android.os.*;
import android.view.*;
import android.widget.*;

import com.itextpdf.text.PageSize;

import dsd.dsdvansales.R;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.model.UOrderHInfo;

public class PrintActionDialog extends Dialog {

	public UOrderHInfo mInfo = new UOrderHInfo();
	public int mReturnCode = 0;

	Context mContext;
	public PrintActionDialog(Context context) {
		super(context, android.R.style.Theme_Translucent_NoTitleBar);
		mContext = context;
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

		setContentView(R.layout.dialog_print_action);

		((TextView) findViewById(R.id.txtTitle)).setText(Str.SelectAction[Global.LANG].toUpperCase());
		((Button) findViewById(R.id.btnReturn)).setText(Str.Return[Global.LANG]);

		if (mInfo != null && mInfo.nPrintedFlag == 0) {
			findViewById(R.id.layoutPrint).setEnabled(true);
			findViewById(R.id.layoutPrint).setBackgroundResource(R.drawable.table_normal);
			((TextView) findViewById(R.id.txtPrint)).setTextColor(Color.BLACK);
			if((Utils.getStringSetting(mContext, Global.PREF_ALLOW_VOID).equals("1") || Utils.getStringSetting(mContext, Global.PREF_ALLOW_VOID).equals("2"))
					&& mInfo.nVoidTrxnNo == 0 && mInfo.nOrgTrxnNo == 0)
			{
				findViewById(R.id.layoutVoid).setEnabled(true);
				findViewById(R.id.layoutVoid).setBackgroundResource(R.drawable.table_normal);
				((TextView) findViewById(R.id.txtVoid)).setTextColor(Color.BLACK);
			}
			else
			{
				findViewById(R.id.layoutVoid).setEnabled(false);
				findViewById(R.id.layoutVoid).setBackgroundResource(R.color.white_color);
				((TextView) findViewById(R.id.txtVoid)).setTextColor(Color.GRAY);
			}

			findViewById(R.id.layoutRePrint).setEnabled(false);
			findViewById(R.id.layoutRePrint).setBackgroundResource(R.color.white_color);
			((TextView) findViewById(R.id.txtRePrint)).setTextColor(Color.GRAY);
		} else {
			findViewById(R.id.layoutPrint).setEnabled(false);
			findViewById(R.id.layoutPrint).setBackgroundResource(R.color.white_color);
			((TextView) findViewById(R.id.txtPrint)).setTextColor(Color.GRAY);

			findViewById(R.id.layoutVoid).setEnabled(false);
			findViewById(R.id.layoutVoid).setBackgroundResource(R.color.white_color);
			((TextView) findViewById(R.id.txtVoid)).setTextColor(Color.GRAY);

			findViewById(R.id.layoutRePrint).setEnabled(true);
			findViewById(R.id.layoutRePrint).setBackgroundResource(R.drawable.table_normal);
			((TextView) findViewById(R.id.txtRePrint)).setTextColor(Color.BLACK);
		}

		findViewById(R.id.btnReturn).setOnClickListener(v -> {
			mReturnCode = 0;
			dismiss();
		});
		findViewById(R.id.layoutPrint).setOnClickListener(v -> {
			mReturnCode = Global.ACTION_PRINT;
			dismiss();
		});
		findViewById(R.id.layoutRePrint).setOnClickListener(v -> {
			mReturnCode = Global.ACTION_REPRINT;
			dismiss();
		});
		findViewById(R.id.layoutVoid).setOnClickListener(v -> {
			mReturnCode = Global.ACTION_VOID;
			dismiss();
		});
	}
}

