package dsd.dsdvansales.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

import java.net.URLEncoder;

import dsd.dsdvansales.R;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;

public class AddMFADialog extends Dialog {

	private Context mContext;

	public AddMFADialog(Context context) {
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

		setContentView(R.layout.dialog_add_mfa);

		findViewById(R.id.btnReturn).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});

		((TextView)findViewById(R.id.txtTitle)).setText(Str.AddMultiFactorAuthenticator[Global.LANG]);
		((TextView)findViewById(R.id.txtMessage)).setText(Str.ScanQRCodeForMFA[Global.LANG]);

		initQRCode();
	}

	public String buildOTPAuthUri(String secret, String account, String issuer) {
		String result = "";
		try {
			result = String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s&algorithm=SHA1&digits=6&period=30",
					URLEncoder.encode(issuer, "UTF-8"),
					URLEncoder.encode(account, "UTF-8"),
					secret,
					URLEncoder.encode(issuer, "UTF-8"));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return result;
	}

	private void initQRCode() {
		String strMFASecret = Utils.getStringSetting(mContext, Global.PREF_MFA_SECRET);
		String strUserDesc = Utils.getStringSetting(mContext, Global.PREF_USER_DESCRIPTION);
		String data = buildOTPAuthUri(strMFASecret, strUserDesc, "DSDVanSales");
		int width = 200;
		int height = 200;
		try {
			BitMatrix bitMatrix = new MultiFormatWriter().encode(
					data, BarcodeFormat.QR_CODE, width, height);
			Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
				}
			}
			((ImageView)findViewById(R.id.qr_image_view)).setImageBitmap(bitmap);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

