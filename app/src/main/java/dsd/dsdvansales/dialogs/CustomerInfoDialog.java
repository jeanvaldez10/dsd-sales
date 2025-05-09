package dsd.dsdvansales.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import dsd.dsdvansales.R;
import dsd.dsdvansales.fragment.SelectCustomerFragment;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.model.CustomerInfo;

public class CustomerInfoDialog extends Dialog implements View.OnClickListener {

	private Context mContext = null;

	TextView mTitleView;
	Button mReturnBtn, mProceedBtn;
	TextView mDexVersionView;
	TextView mDescriptionView;
	SelectCustomerFragment parentFragment;
	private boolean bShowReturn = true;
	private String mStrDexVersion = "", mstrDescription = "";

	public CustomerInfoDialog(Context context) {
		super(context, android.R.style.Theme_Translucent_NoTitleBar);
		this.mContext = context;
	}

	public void setData(String strDexVersion, String strDescription, SelectCustomerFragment parentFragment)
	{
		mStrDexVersion = strDexVersion;
		mstrDescription = strDescription;
		this.parentFragment = parentFragment;
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

		setContentView(R.layout.dialog_customer_information);

		initView();
		loadData();
		refreshPage();
		refreshLanguage();

		if(!bShowReturn)
		{
			findViewById(R.id.btnReturn).setVisibility(View.GONE);
		}
	}

	public void initView()
	{
		mTitleView = (TextView)findViewById(R.id.txtTitle);
		mDexVersionView = (TextView)findViewById(R.id.txtDexVersion);
		mDescriptionView = (TextView)findViewById(R.id.txtDescription);
		mReturnBtn = (Button)findViewById(R.id.btnReturn);
		mReturnBtn.setOnClickListener(this);
		mProceedBtn = (Button)findViewById(R.id.btnProceed);
		mProceedBtn.setOnClickListener(this);

	}

	public void loadData()
	{

	}

	public void refreshPage()
	{
		if(!TextUtils.isEmpty(mStrDexVersion))
		{
			mDexVersionView.setVisibility(View.VISIBLE);
			mDexVersionView.setText(mStrDexVersion);
		}
		else
		{
			mDexVersionView.setVisibility(View.GONE);
		}

		if(!TextUtils.isEmpty(mstrDescription))
		{
			mDescriptionView.setVisibility(View.VISIBLE);
			mDescriptionView.setText(mstrDescription);
		}
		else
		{
			mDescriptionView.setVisibility(View.GONE);
		}
	}

	public void refreshLanguage() {
		mTitleView.setText(Str.CustomerInfo[Global.LANG]);
		mReturnBtn.setText(Str.Return[Global.LANG]);
		mProceedBtn.setText(Str.Proceed[Global.LANG]);
	}

	@Override
	public void onClick(View view) {
		int id = view.getId();
		switch (id)
		{
			case R.id.btnReturn:
				Global.gCurCustomerInfo = null;
				dismiss();
				break;
			case R.id.btnProceed:
				if( parentFragment != null && Global.gCurCustomerInfo != null)
				{
					parentFragment.showCustomerActionDialog(Global.gCurCustomerInfo.strName);
				}
				dismiss();
				break;
		}
	}

	public void showReturnBtn(boolean bShowReturn)
	{
		this.bShowReturn = bShowReturn;
	}


}