package dsd.dsdvansales.activities;

import android.Manifest;
import android.app.Activity;
import android.content.*;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.*;
import android.os.*;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;


import androidx.core.app.ActivityCompat;

import dsd.dsdvansales.R;
import dsd.dsdvansales.db.DbAdapter;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.model.CmpyContactInfo;


public class AboutActivity extends Activity implements OnClickListener {
	private CmpyContactInfo mCompanyInfo = new CmpyContactInfo();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.about_view);

		InitView();
		refreshLanguage();
		LoadCmpyContactInfo();
	}

	private void InitView() {
		findViewById(R.id.txtWeb).setOnClickListener(this);
		findViewById(R.id.txtEmail).setOnClickListener(this);
		findViewById(R.id.txtPhone1).setOnClickListener(this);
		findViewById(R.id.txtPhone2).setOnClickListener(this);
		findViewById(R.id.btnExit).setOnClickListener(this);
		findViewById(R.id.txtAppVersionName).setOnClickListener(this);
		String strAppVersion = "";
		PackageManager manager = getPackageManager();
		PackageInfo info;
		try {
			info = manager.getPackageInfo(getPackageName(), 0);
			strAppVersion = info.versionName;
		} catch (Exception e) {
			strAppVersion = "" + getText(R.string.version_name);
			e.printStackTrace();
		}
		((TextView) findViewById(R.id.txtAppVersionName)).setText(strAppVersion);
	}

	private void LoadCmpyContactInfo() {
		DbAdapter dbAdapter = new DbAdapter(getApplicationContext());
		dbAdapter.open();
		mCompanyInfo = dbAdapter.getCmpyContactInfo();
		dbAdapter.close();

		((TextView) findViewById(R.id.txtWeb)).setText(mCompanyInfo.strCompanyWeb);
		((TextView) findViewById(R.id.txtEmail)).setText(mCompanyInfo.strCompanyEmail);
		((TextView) findViewById(R.id.txtPhone1)).setText(mCompanyInfo.strCompanyPhone1);
		((TextView) findViewById(R.id.txtPhone2)).setText(mCompanyInfo.strCompanyPhone2);
		((TextView) findViewById(R.id.txtInfo)).setText(mCompanyInfo.strCompanyInfo);
	}

	private void CallPhone(final String strPhoneNumber) {
		Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + strPhoneNumber));
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
			// TODO: Consider calling
			//    ActivityCompat#requestPermissions
			// here to request the missing permissions, and then overriding
			//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
			//                                          int[] grantResults)
			// to handle the case where the user grants the permission. See the documentation
			// for ActivityCompat#requestPermissions for more details.
			return;
		}
		startActivity(intent);
	}

	private void SendEmail(String strEmailAddress)
	{
		try {
			Uri uri = Uri.parse("mailto:" + strEmailAddress);
			Intent emailIntent = new Intent(Intent.ACTION_SENDTO, uri);
			emailIntent.putExtra(Intent.EXTRA_SUBJECT, "");
			emailIntent.putExtra(Intent.EXTRA_TEXT, "");
			startActivity(emailIntent);
		}
		catch (ActivityNotFoundException e) {
			Toast.makeText(this, Str.EmailAppNotFound[Global.LANG], Toast.LENGTH_LONG).show();
		}
	}

	private void OpenWebsite(String strWebsite)
	{
		if (strWebsite.startsWith("http") == false && strWebsite.startsWith("https") == false)
		{
			strWebsite = "http://" + strWebsite;
		}
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(strWebsite));
		startActivity(browserIntent);
	}

	public void refreshLanguage()
	{
		Global.LANG = Utils.getIntSetting(this, Global.PREF_CUR_LANG);
		((Button) findViewById(R.id.btnExit)).setText(Str.Exit[Global.LANG]);
	}

	@Override
	public void onBackPressed()
	{
		activityFinish();
	}

	public void activityFinish()
	{
		finish();
		overridePendingTransition(R.anim.finish1, R.anim.finish2);
	}

	public boolean m_bClicked = false;
	public void onClick(View v)
	{
		if( m_bClicked ) return;
		m_bClicked = true;
		switch (v.getId())
		{
		case R.id.txtWeb:
			OpenWebsite(mCompanyInfo.strCompanyWeb);
			break;

		case R.id.txtEmail:
			SendEmail(mCompanyInfo.strCompanyEmail);
			break;

		case R.id.txtPhone1:
			CallPhone(mCompanyInfo.strCompanyPhone1);
			break;

		case R.id.txtPhone2:
			CallPhone(mCompanyInfo.strCompanyPhone2);
			break;

		case R.id.btnExit:
			activityFinish();
			break;

		case R.id.txtAppVersionName:
			//doFireCrash();
			break;
		}
		m_bClicked = false;
	}

	public void doFireCrash()
	{
		throw new RuntimeException("Test Crash");
	}
}
