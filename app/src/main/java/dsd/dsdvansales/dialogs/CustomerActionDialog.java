package dsd.dsdvansales.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.*;
import android.view.*;
import android.widget.*;

import java.util.List;

import dsd.dsdvansales.R;
import dsd.dsdvansales.activities.MainActivity;
import dsd.dsdvansales.db.DbAdapter;
import dsd.dsdvansales.fragment.SelectCustomerFragment;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.model.CustomerInfo;
import dsd.dsdvansales.model.CustomerNoteInfo;

public class CustomerActionDialog extends Dialog {

	private Context mContext = null;
	public String mStrCustomerName = "";
	public int mReturnCode = 0;
	public SelectCustomerFragment fragment;
	private CustomerInfo mSelCustomerInfo;
	public void setFragment(SelectCustomerFragment fragment)
	{
		this.fragment = fragment;
	}

	public CustomerActionDialog(Context context) {
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

		setContentView(R.layout.dialog_customer_action);

		mSelCustomerInfo = Global.gCurCustomerInfo;
		((TextView) findViewById(R.id.txtCustomerName)).setText(mStrCustomerName);

		Global.LANG = Utils.getIntSetting(mContext, Global.PREF_CUR_LANG);
		((TextView) findViewById(R.id.txtDeliverToStore)).setText(Str.DeliverToStore[Global.LANG]);
		((TextView) findViewById(R.id.txtViewStoreDetails)).setText(Str.ViewStoreDetails[Global.LANG]);
		((TextView) findViewById(R.id.txtUnableToDeliver)).setText(Str.UnableToDeliver[Global.LANG]);
		((TextView) findViewById(R.id.txtNavigateMeToStore)).setText(Str.NavigateMeToStore[Global.LANG]);
		((Button) findViewById(R.id.btnReturn)).setText(Str.Return[Global.LANG]);

		findViewById(R.id.btnReturn).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v)
			{
				mReturnCode = 0;
				Global.gCurCustomerInfo = null;
				dismiss();
			}
		});
		findViewById(R.id.layoutDeliverToStore).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mReturnCode = Global.ACTION_DELIVER_TO_STORE;
				if(fragment != null )
				{
					fragment.checkIsAtStore();
				}
				dismiss();
			}
		});
		findViewById(R.id.layoutViewStoreDetails).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mReturnCode = Global.ACTION_VIEW_STORE_DETAILS;
				dismiss();
			}
		});
		findViewById(R.id.layoutUnableToDeliver).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mReturnCode = Global.ACTION_UNABLE_TO_DELIVER;
				dismiss();
			}
		});
		findViewById(R.id.layoutNavigateMeToStore).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mReturnCode = Global.ACTION_NAVIGATE_ME_TO_STORE;
				dismiss();
			}
		});
		findViewById(R.id.layoutDeliveryInstructions).setOnClickListener(v -> {
			DbAdapter dbAdapter = new DbAdapter(mContext);
			dbAdapter.open();
			List<CustomerNoteInfo> noteInfoList = dbAdapter.getCustomerNoteInfoList(mSelCustomerInfo.nCustNo, mSelCustomerInfo.nChainNo);
			dbAdapter.close();
			CustomerNoteInfo selInfo = null;
			for(CustomerNoteInfo noteInfo : noteInfoList)
			{
				if(noteInfo.strNoteType.equals("92"))
				{
					selInfo = noteInfo;
					break;
				}
			}

			if(Global.gCurCustomerInfo == null)
			{
				Global.gCurCustomerInfo = mSelCustomerInfo;
			}
			CustomerNoteDialog customerNoteDialog = new CustomerNoteDialog((MainActivity) mContext, selInfo, () -> {

			});
			customerNoteDialog.show();
		});
	}
}

