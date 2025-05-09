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
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.model.CustomerInfo;

public class KeyInformationDialog extends Dialog implements View.OnClickListener {

	private Context mContext = null;

	ListView mKeyListView;
	TextView mTitleView;
	TextView mDescriptionView;
	Button mReturnBtn;

	List<String> mDataList = new ArrayList<>();
	ArrayAdapter<String> mDataAdapter = null;
	List<CustomerInfo> mCustomerList = new ArrayList<>();
	public KeyInformationDialog(Context context) {
		super(context, android.R.style.Theme_Translucent_NoTitleBar);
		this.mContext = context;
	}

	public void setCustomerList(List<CustomerInfo> customerList)
	{
		mCustomerList = customerList;
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

		setContentView(R.layout.dialog_key_information);

		initView();
		loadData();
		refreshPage();
		refreshLanguage();
	}

	public void initView()
	{
		mKeyListView = (ListView)findViewById(R.id.keyList);
		mTitleView = (TextView)findViewById(R.id.txtTitle);
		mDescriptionView = (TextView)findViewById(R.id.txtDescription);
		mReturnBtn = (Button)findViewById(R.id.btnReturn);
		mReturnBtn.setOnClickListener(this);
		mDataAdapter = new ArrayAdapter<>(mContext, R.layout.item_array_list, mDataList);
		mKeyListView.setAdapter(mDataAdapter);
	}

	public void loadData()
	{
		mDataList.clear();
		for(CustomerInfo customerInfo: mCustomerList)
		{
			if( !TextUtils.isEmpty(customerInfo.strDexVersion))
			{
				mDataList.add(customerInfo.strName + "\n    "  + customerInfo.strDexVersion);
			}
		}
	}

	public void refreshPage()
	{
		mDataAdapter.notifyDataSetChanged();
	}

	public void refreshLanguage() {
		mTitleView.setText(Str.KeyInformation[Global.LANG]);
		mDescriptionView.setText(Str.KeyInformationDescription[Global.LANG]);
		mReturnBtn.setText(Str.Return[Global.LANG]);
	}

	@Override
	public void onClick(View view) {
		int id = view.getId();
		switch (id)
		{
			case R.id.btnReturn:
				dismiss();
				break;
		}

	}
}