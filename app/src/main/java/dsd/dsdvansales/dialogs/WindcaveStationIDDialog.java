package dsd.dsdvansales.dialogs;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import dsd.dsdvansales.R;
import dsd.dsdvansales.activities.BarcodeScannerActivity;
import dsd.dsdvansales.activities.MainActivity;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.model.MakeZipFileInfo;

public class WindcaveStationIDDialog extends Dialog {

	public interface Callback{
		void onUpdate();
		void onCancel();
	}

	MainActivity mContext;
	public MakeZipFileInfo makeZipFileInfo = null;

	Callback callback;
	public WindcaveStationIDDialog(MainActivity context, Callback callback) {
		super(context, android.R.style.Theme_Translucent_NoTitleBar);
		this.mContext = context;
		this.callback = callback;
	}

	BroadcastReceiver receiver = null;

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

		setContentView(R.layout.dialog_windcave_id);


		((TextView)findViewById(R.id.lblNote)).setText(Str.WindcaveCardPresentStationID[Global.LANG]);
		((TextView)findViewById(R.id.txtTitle)).setText(Str.StationID[Global.LANG]);


		((Button)findViewById(R.id.btnReturn)).setText(Str.Return[Global.LANG]);
		((Button)findViewById(R.id.btnUpdate)).setText(Str.Update[Global.LANG]);

		findViewById(R.id.btnReturn).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				dismiss();
				if(callback != null)
				{
					callback.onCancel();
				}
			}
		});

		findViewById(R.id.btnUpdate).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				String strStationId = ((EditText)findViewById(R.id.editStationID)).getText().toString();
				if(TextUtils.isEmpty(strStationId))
				{
					try
					{
						Toast.makeText(mContext, Str.WindcaveCardPresentStationID[Global.LANG], Toast.LENGTH_LONG).show();
					}catch (Exception e)
					{
						e.printStackTrace();
					}
				}
				else
				{
					dismiss();
					Utils.setStringSetting(mContext, Global.PREF_WINDCAVE_HITSTATION_ID, strStationId);
					if(callback != null)
					{
						callback.onUpdate();
					}
				}
			}
		});

		findViewById(R.id.imgBarcode).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if(receiver == null)
				{
					receiver = new BroadcastReceiver() {
						@Override
						public void onReceive(Context context, Intent intent) {
							String strFrom = intent.getStringExtra(Global.EXTRA_BARCODESCAN_FROM);
							if (!strFrom.equals(WindcaveStationIDDialog.this.getClass().getSimpleName())) {
								return;
							}
							String barcode = intent.getStringExtra(Global.EXTRA_ITEM_UPC);
							((EditText)findViewById(R.id.editStationID)).setText(barcode);
						}
					};
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
						mContext.registerReceiver(receiver, new IntentFilter(Global.ACTION_SELECT_PRODUCT), Context.RECEIVER_EXPORTED);
					} else {
						mContext.registerReceiver(receiver, new IntentFilter(Global.ACTION_SELECT_PRODUCT));
					}
				}
				Intent intent = new Intent(mContext, BarcodeScannerActivity.class);
				intent.putExtra(Global.EXTRA_BARCODESCAN_FROM, WindcaveStationIDDialog.this.getClass().getSimpleName());
				mContext.startActivity(intent);
			}
		});

		((EditText) findViewById(R.id.editStationID)).setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if( actionId == EditorInfo.IME_ACTION_DONE)
				{
					findViewById(R.id.btnUpdate).performClick();
				}
				return false;
			}
		});

		((EditText)findViewById(R.id.editStationID)).setText(Utils.getStringSetting(mContext, Global.PREF_WINDCAVE_HITSTATION_ID));
	}

	@Override
	protected void onStop() {
		super.onStop();
		if(receiver != null)
		{
			mContext.unregisterReceiver(receiver);
		}
	}
}