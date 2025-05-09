package dsd.dsdvansales.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.util.Locale;

import dsd.dsdvansales.R;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.model.PrintItem;
import dsd.dsdvansales.model.UOrderHInfo;

public class ReprintActionDialog extends Dialog {

	public UOrderHInfo mInfo = null;
	public PrintItem printItem = null;
	public int mReturnCode = 0;

	Context mContext;
	public ReprintActionDialog(Context context) {
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

		setContentView(R.layout.dialog_reprint_action);

		((TextView) findViewById(R.id.txtTitle)).setText(Str.SelectAction[Global.LANG].toUpperCase());
		((Button) findViewById(R.id.btnReturn)).setText(Str.Return[Global.LANG]);

		if(printItem == null)
		{
			((TextView) findViewById(R.id.txtView)).setText(Str.View[Global.LANG].toUpperCase());
			((TextView) findViewById(R.id.txtPrint)).setText(Str.Print[Global.LANG].toUpperCase());
		}
		else
		{
			((TextView) findViewById(R.id.txtView)).setText(String.format(Locale.ENGLISH, Str.View_DocType[Global.LANG], printItem.strDocType).toUpperCase());
			((TextView) findViewById(R.id.txtPrint)).setText(String.format(Locale.ENGLISH, Str.Print_DocType[Global.LANG], printItem.strDocType).toUpperCase());
		}


		((TextView) findViewById(R.id.txtViewVoid)).setText(Str.ViewVoid[Global.LANG].toUpperCase());


		if(mInfo != null)
		{
			if(Utils.getStringSetting(mContext, Global.PREF_ALLOW_VOID).equals("2"))
			{
				if(mInfo.nVoidTrxnNo == 0)
				{
					((TextView) findViewById(R.id.txtVoid)).setText(Str.Void[Global.LANG].toUpperCase());
					findViewById(R.id.layoutViewVoid).setVisibility(View.GONE);
				}
				else
				{
					((TextView) findViewById(R.id.txtVoid)).setText(Str.Void_Print[Global.LANG].toUpperCase());
					findViewById(R.id.layoutViewVoid).setVisibility(View.VISIBLE);
				}
			}
			else
			{
				findViewById(R.id.layoutVoid).setEnabled(false);
//				findViewById(R.id.layoutVoid).setBackgroundResource(R.color.white_color);
				((TextView) findViewById(R.id.txtVoid)).setTextColor(Color.GRAY);

				findViewById(R.id.layoutViewVoid).setVisibility(View.GONE);
			}
		}
		else
		{
			findViewById(R.id.layoutViewVoid).setVisibility(View.GONE);
			findViewById(R.id.layoutVoid).setVisibility(View.GONE);
		}

		String strDisableInvReprint = Utils.getStringSetting(mContext, Global.PREF_DISABLE_INV_REPRINT);
		if (strDisableInvReprint.equals("1")) {
			findViewById(R.id.txtPrint).setEnabled(false);
			((TextView) findViewById(R.id.txtPrint)).setTextColor(Color.GRAY);
		}

		findViewById(R.id.btnReturn).setOnClickListener(v -> {
			mReturnCode = 0;
			dismiss();
		});

		findViewById(R.id.layoutView).setOnClickListener(v -> {
			mReturnCode = Global.ACTION_VIEW;
			dismiss();
		});

		findViewById(R.id.layoutPrint).setOnClickListener(v -> {
			mReturnCode = Global.ACTION_PRINT;
			dismiss();
		});

		if(Utils.getStringSetting(mContext, Global.PREF_ALLOW_VOID).equals("2") && mInfo != null)
		{
			findViewById(R.id.layoutVoid).setOnClickListener(v -> {
				mReturnCode = Global.ACTION_VOID;
				dismiss();
			});

			findViewById(R.id.layoutViewVoid).setOnClickListener(v -> {
				mReturnCode = Global.ACTION_VIEW_VOID;
				dismiss();
			});
		}

	}
}

