package dsd.dsdvansales.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;


import com.google.zxing.Result;
import dsd.dsdvansales.R;
import dsd.dsdvansales.global.Global;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

/* Import ZBar Class files */

public class BarcodeScannerActivity extends Activity implements ZXingScannerView.ResultHandler {

	private ZXingScannerView mScannerView;
	private String mFrom;

	@Override
	public void onCreate(Bundle state) {
		super.onCreate(state);
		mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
		setContentView(mScannerView);                // Set the scanner view as the content view

		Intent intent = getIntent();
		String value = intent.getStringExtra(Global.EXTRA_BARCODESCAN_FROM);
		mFrom = value == null ? "" : value;

//		new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
//			@Override
//			public void run() {
//				Intent intent = new Intent(Global.ACTION_SELECT_PRODUCT);
//				intent.putExtra(Global.EXTRA_TYPE, Global.SELECT_PRODUCT_ITEMUPC);
//				intent.putExtra(Global.EXTRA_ITEM_UPC, "3111145");
//				intent.putExtra(Global.EXTRA_BARCODESCAN_FROM, mFrom);
//				sendBroadcast(intent);
//				activityFinish();
//			}
//		}, 1000);
	}

	@Override
	public void onResume() {
		super.onResume();
		mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
		mScannerView.startCamera();          // Start camera on resume
	}

	@Override
	public void onPause() {
		super.onPause();
		mScannerView.stopCamera();           // Stop camera on pause
	}

	@Override
	public void handleResult(Result rawResult) {

		MakeVibrate();

		Intent intent = new Intent(Global.ACTION_SELECT_PRODUCT);
		intent.putExtra(Global.EXTRA_TYPE, Global.SELECT_PRODUCT_ITEMUPC);
		intent.putExtra(Global.EXTRA_ITEM_UPC, rawResult.getText());
		intent.putExtra(Global.EXTRA_BARCODESCAN_FROM, mFrom);
		sendBroadcast(intent);
		activityFinish();
	}
	private void MakeVibrate() {
		Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		vibrator.vibrate(500);
	}

	private void activityFinish() {
		finish();
		overridePendingTransition(R.anim.finish1, R.anim.finish2);
	}

	@Override
	public void onBackPressed() {
		activityFinish();
	}
}