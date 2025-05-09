package dsd.dsdvansales.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import dsd.dsdvansales.R;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.views.AlertSingleChoiceAdapter;

public class MintCardProcDialog extends Dialog {

	private Context mContext;
	private TextView mTxtMethod = null;

	private final String[] methodArray = {Str.CardPresent[Global.LANG], Str.NotPresentTransaction[Global.LANG]};

	private int mMethodIdx = 0;

	public interface Callback
	{
		public void onMintNotPresent();
		public void onMintCardPresent(boolean eWay);
	}

	private Callback callback;
	public boolean bEway = false;

	public MintCardProcDialog(Context context, Callback callback) {
		super(context, android.R.style.Theme_Translucent_NoTitleBar);
		this.mContext = context;
		this.callback = callback;
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

		setContentView(R.layout.dialog_mint_card_proc);


		mTxtMethod = (TextView)findViewById(R.id.txtMethod);
		mTxtMethod.setText(methodArray[mMethodIdx]);
		((Button) findViewById(R.id.btnReturn)).setText(Str.Return[Global.LANG]);
		((Button) findViewById(R.id.btnSelect)).setText(Str.Select[Global.LANG]);

		((TextView)findViewById(R.id.txtMessage)).setText(Str.WhatDoYouWantToPerform[Global.LANG]);
		mTxtMethod.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showMethodList();
			}
		});

		findViewById(R.id.btnReturn).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});

		findViewById(R.id.btnSelect).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(callback != null)
				{
					if(mMethodIdx == 0)
					{
						callback.onMintCardPresent(bEway);
					}
					else
					{
						callback.onMintNotPresent();
					}
				}
				dismiss();
			}
		});
	}

	private void showMethodList() {
		final int origIdx = mMethodIdx;
		final String origText = mTxtMethod.getText().toString();

		try
		{
			new AlertDialog.Builder(mContext)
					.setTitle(Str.SelectCardPaymentMethod[Global.LANG])
					.setSingleChoiceItems(new AlertSingleChoiceAdapter(mContext, methodArray), origIdx, new OnClickListener() {
						public void onClick(DialogInterface dialog, int nSelected) {
							mMethodIdx = nSelected;
							mTxtMethod.setText(methodArray[nSelected]);
						}
					})
					.setPositiveButton(Str.Ok[Global.LANG], new OnClickListener() {
						public void onClick(DialogInterface dialog, int nSelected) {
						}
					})
					.setNegativeButton(Str.Cancel[Global.LANG], new OnClickListener() {
						public void onClick(DialogInterface dialog, int nSelected) {
							mMethodIdx = origIdx;
							mTxtMethod.setText(origText);
						}
					})
					.create()
					.show();
		}catch (Exception e)
		{
			e.printStackTrace();
		}

	}
}