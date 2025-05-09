package dsd.dsdvansales.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import dsd.dsdvansales.R;
import dsd.dsdvansales.activities.MainActivity;
import dsd.dsdvansales.db.DbAdapter;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.model.DescTypeInfo;
import dsd.dsdvansales.model.LocationInfo;
import dsd.dsdvansales.views.AlertSingleChoiceAdapter;

public class OrderDeliveryOptionDialog extends Dialog {


	public interface Callback{
		void onSelect(int nKind);
	}

	private MainActivity mContext = null;
	private TextView mTxtDeliveryOption;
	private int mSelIdx = 0;
	Callback callback;
	int nDefType = 0;

	public OrderDeliveryOptionDialog(MainActivity context, Callback callback, int nDefType) {
		super(context, android.R.style.Theme_Translucent_NoTitleBar);
		this.mContext = context;
		this.callback = callback;
		this.nDefType = nDefType;
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

		setContentView(R.layout.dialog_order_delivery_option);

		((TextView)findViewById(R.id.txtTitle)).setText(Str.OrderDeliveryOption[Global.LANG]);

		((Button)findViewById(R.id.btnReturn)).setText(Str.Return[Global.LANG]);
		((Button)findViewById(R.id.btnOkay)).setText(Str.Ok[Global.LANG]);

		mTxtDeliveryOption = (TextView)findViewById(R.id.txtDeliveryOption);

		mTxtDeliveryOption.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				showDeliveryOption();
			}
		});
		mSelIdx = nDefType - 1;
		final String strArray[] = {Str.DeliveryFromVehicle[Global.LANG], /*Str.DeliveryFromWarehouse[Global.LANG],*/ Str.DeliveryOnSpecificDate[Global.LANG]};
		mTxtDeliveryOption.setText(strArray[mSelIdx]);

		findViewById(R.id.btnReturn).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				dismiss();
			}
		});
		findViewById(R.id.btnOkay).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				callback.onSelect(mSelIdx);
				dismiss();
			}
		});
	}

	private void showDeliveryOption()
	{
		final String strArray[] = {Str.DeliveryFromVehicle[Global.LANG], /*Str.DeliveryFromWarehouse[Global.LANG],*/
				Str.DeliveryOnSpecificDate[Global.LANG]};

		final int origIdx = mSelIdx;
		final String origText = mTxtDeliveryOption.getText().toString();

		try
		{
			new AlertDialog.Builder(mContext)
					.setTitle(Str.SelectEndLocation[Global.LANG])
					.setSingleChoiceItems(new AlertSingleChoiceAdapter(mContext, strArray), origIdx, (dialog, nSelected) -> {
						mSelIdx = nSelected;
						mTxtDeliveryOption.setText(strArray[nSelected]);
					})
					.setPositiveButton(Str.Ok[Global.LANG], (dialog, nSelected) -> {
					})
					.setNegativeButton(Str.Cancel[Global.LANG], (dialog, nSelected) -> {
						mSelIdx = origIdx;
						mTxtDeliveryOption.setText(origText);
					})
					.create()
					.show();
		}catch (Exception e)
		{
			e.printStackTrace();
		}

	}

}