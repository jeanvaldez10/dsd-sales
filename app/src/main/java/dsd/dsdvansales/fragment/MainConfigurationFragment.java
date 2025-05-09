package dsd.dsdvansales.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import dsd.dsdvansales.R;
import dsd.dsdvansales.api.UploadApiClient;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;

public class MainConfigurationFragment extends BaseFragment implements View.OnClickListener {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootView = inflater.inflate(R.layout.fragment_configuration, container, false);
		commonInit();
		return mRootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		Global.gFragIdx = Global.FRAG_CONFIGURATION;
	}

	@Override
	public void initView() {
		mRootView.findViewById(R.id.btnBack).setOnClickListener(this);
		mRootView.findViewById(R.id.btnSave).setOnClickListener(this);

		((EditText) mRootView.findViewById(R.id.editFTPPassword)).setOnEditorActionListener(new EditText.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					doSave();
					return true;
				}
				return false;
			}
		});

		loadData();
		refreshLanguage();
	}

	@Override
	public void loadData() {
		String strFTPServerIP = Utils.getStringSetting(mContext, Global.PREF_FTP_SERVER_IP);
		String strFTPCompanyName = Utils.getStringSetting(mContext, Global.PREF_FTP_COMPANY_NAME);
		String strFTPUserName = Utils.getStringSetting(mContext, Global.PREF_FTP_USERNAME);
		String strFTPPassword = Utils.getStringSetting(mContext, Global.PREF_FTP_PASSWORD);
		((EditText) mRootView.findViewById(R.id.editFTPServerIP)).setText(strFTPServerIP);
		((EditText) mRootView.findViewById(R.id.editFTPCompanyName)).setText(strFTPCompanyName);
		((EditText) mRootView.findViewById(R.id.editFTPUserName)).setText(strFTPUserName);
		((EditText) mRootView.findViewById(R.id.editFTPPassword)).setText(strFTPPassword);
		if (Global._DEBUG_ == true) {
			if (strFTPServerIP.length() == 0) {
				((EditText) mRootView.findViewById(R.id.editFTPServerIP)).setText("54.153.250.85");
			}
			if (strFTPCompanyName.length() == 0) {
				((EditText) mRootView.findViewById(R.id.editFTPCompanyName)).setText("DSDPlus_Demo");
			}
			if (strFTPUserName.length() == 0) {
				((EditText) mRootView.findViewById(R.id.editFTPUserName)).setText("ftptst");
			}
			if (strFTPPassword.length() == 0) {
				((EditText) mRootView.findViewById(R.id.editFTPPassword)).setText("jpzyg5HP37atWuRi4mbq");
			}
		}
	}

	@Override
	public void refreshPage() {
	}

	@Override
	public void refreshLanguage() {
		((TextView) mRootView.findViewById(R.id.lblTitle)).setText(Str.Configuration[Global.LANG].toUpperCase());
		((EditText) mRootView.findViewById(R.id.editFTPServerIP)).setHint(Str.IP_Address_of_Server[Global.LANG]);
		((EditText) mRootView.findViewById(R.id.editFTPCompanyName)).setHint(Str.Company_Name[Global.LANG]);
		((EditText) mRootView.findViewById(R.id.editFTPUserName)).setHint(Str.FTP_User[Global.LANG]);
		((EditText) mRootView.findViewById(R.id.editFTPPassword)).setHint(Str.FTP_Password[Global.LANG]);
		((Button) mRootView.findViewById(R.id.btnSave)).setText(Str.Save[Global.LANG]);
	}

	@Override
	public void receiveBroadcast(String strAction, Intent intent) {
	}

	private void doSave() {
		String strFTPServerIP = ((EditText) mRootView.findViewById(R.id.editFTPServerIP)).getText().toString();
		if (strFTPServerIP.length() == 0) {
			Utils.printDebug(mContext, Str.Please_input_the_IP_Address_of_server[Global.LANG]);
			showKeyboard((EditText) mRootView.findViewById(R.id.editFTPServerIP), true);
			return;
		}
		String strFTPCompanyName = ((EditText) mRootView.findViewById(R.id.editFTPCompanyName)).getText().toString();
		if (strFTPCompanyName.length() == 0) {
			Utils.printDebug(mContext, Str.Please_input_the_Company_Name[Global.LANG]);
			showKeyboard((EditText) mRootView.findViewById(R.id.editFTPCompanyName), true);
			return;
		}
		String strFTPUserName = ((EditText) mRootView.findViewById(R.id.editFTPUserName)).getText().toString();
		if (strFTPUserName.length() == 0) {
			Utils.printDebug(mContext, Str.Please_input_the_FTP_User[Global.LANG]);
			showKeyboard((EditText) mRootView.findViewById(R.id.editFTPUserName), true);
			return;
		}
		String strFTPPassword = ((EditText) mRootView.findViewById(R.id.editFTPPassword)).getText().toString();
		if (strFTPPassword.length() == 0) {
			Utils.printDebug(mContext, Str.Please_input_the_FTP_Password[Global.LANG]);
			showKeyboard((EditText) mRootView.findViewById(R.id.editFTPPassword), true);
			return;
		}
		showKeyboard((EditText) mRootView.findViewById(R.id.editFTPPassword), false);

		Utils.setStringSetting(mContext, Global.PREF_FTP_SERVER_IP, strFTPServerIP);
		UploadApiClient.API_ROOT = "http://" + strFTPServerIP;
		Utils.setStringSetting(mContext, Global.PREF_FTP_COMPANY_NAME, strFTPCompanyName);
		Utils.setStringSetting(mContext, Global.PREF_FTP_USERNAME, strFTPUserName);
		Utils.setStringSetting(mContext, Global.PREF_FTP_PASSWORD, strFTPPassword);

		Utils.printDebug(mContext, Str.Saved_successfully[Global.LANG]);
		mContext.onBackPressed();
	}

	boolean m_bClicked = false;
	@Override
	public void onClick(View v) {
		if( m_bClicked ) return;
		m_bClicked = true;
		switch (v.getId()) {
			case R.id.btnBack:
				mContext.onBackPressed();
				break;
			case R.id.btnSave:
				doSave();
				break;
		}
		m_bClicked = false;
	}
}