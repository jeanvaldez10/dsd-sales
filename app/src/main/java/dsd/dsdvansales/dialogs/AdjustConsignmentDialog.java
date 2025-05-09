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

public class AdjustConsignmentDialog extends Dialog {

	private Context mContext = null;

	public int mReturnCode = 0;
	public int mReasonType = 0;
	public AdjustConsignmentDialog(Context context) {
		super(context, android.R.style.Theme_Translucent_NoTitleBar);
		this.mContext = context;
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

		setContentView(R.layout.dialog_adjust_consignment);

		((TextView) findViewById(R.id.txtTitle)).setText(Str.AdjustConsignment[Global.LANG].toUpperCase());
		((TextView) findViewById(R.id.txtAddStock)).setText(Str.AddStockToShelf[Global.LANG]);
		((TextView) findViewById(R.id.txtRemoveStock)).setText(Str.RemoveStockFromShelf[Global.LANG]);
		((TextView) findViewById(R.id.txtRemoveDamagedStock)).setText(Str.RemoveDamagedStockFromShelf[Global.LANG]);
		((Button) findViewById(R.id.btnReturn)).setText(Str.Return[Global.LANG]);
		((Button) findViewById(R.id.btnSelect)).setText(Str.Select[Global.LANG]);

		findViewById(R.id.txtAddStock).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if( m_bClicked ) return;
				m_bClicked = true;
				setReasonType(0);
				m_bClicked = false;
			}
		});
		findViewById(R.id.txtRemoveStock).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if( m_bClicked ) return;
				m_bClicked = true;
				setReasonType(1);
				m_bClicked = false;
			}
		});
		findViewById(R.id.txtRemoveDamagedStock).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if( m_bClicked ) return;
				m_bClicked = true;
				setReasonType(2);
				m_bClicked = false;
			}
		});
		findViewById(R.id.btnReturn).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if( m_bClicked ) return;
				m_bClicked = true;
				mReturnCode = 0;
				dismiss();
				m_bClicked = false;
			}
		});
		findViewById(R.id.btnSelect).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if( m_bClicked ) return;
				m_bClicked = true;
				mReturnCode = Global.DLG_RETURN_RIGHT;
				dismiss();
				m_bClicked = false;
			}
		});
	}

	private void setReasonType(int nType) {
		if (nType != mReasonType) {
			mReasonType = nType;
			if (mReasonType == 0) {
				((TextView) findViewById(R.id.txtAddStock)).setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_check_on, 0);
				((TextView) findViewById(R.id.txtRemoveStock)).setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_check_off, 0);
				((TextView) findViewById(R.id.txtRemoveDamagedStock)).setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_check_off, 0);
			} else if(mReasonType == 1){
				((TextView) findViewById(R.id.txtAddStock)).setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_check_off, 0);
				((TextView) findViewById(R.id.txtRemoveStock)).setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_check_on, 0);
				((TextView) findViewById(R.id.txtRemoveDamagedStock)).setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_check_off, 0);
			} else {
				((TextView) findViewById(R.id.txtAddStock)).setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_check_off, 0);
				((TextView) findViewById(R.id.txtRemoveStock)).setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_check_off, 0);
				((TextView) findViewById(R.id.txtRemoveDamagedStock)).setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_check_on, 0);
			}
		}
	}
}