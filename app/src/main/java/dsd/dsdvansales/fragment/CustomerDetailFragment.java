package dsd.dsdvansales.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
//import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import dsd.dsdvansales.R;
import dsd.dsdvansales.db.DbAdapter;
import dsd.dsdvansales.dialogs.ContactDialog;
import dsd.dsdvansales.dialogs.MessageDialog;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.model.CustContactInfo;
import dsd.dsdvansales.model.CustomerInfo;
import dsd.dsdvansales.model.DescTypeInfo;
import dsd.dsdvansales.model.StatusInfo;
import dsd.dsdvansales.model.TransactionInfo;
import dsd.dsdvansales.model.UFileTransactionInfo;
import dsd.dsdvansales.model.UGPSInfo;

public class CustomerDetailFragment extends BaseFragment implements View.OnClickListener {

	private CustomerInfo mCustomerInfo = null;
	private List<CustContactInfo> mContactList = new ArrayList<>();
	private String mStrCustomerPhotoPath = "";
	private String mStrPhotoPath = "";

	private String mStrPaymentType = "";
	private String mStrPriceGroup = "";
	private String mStrVisitFrequency = "";
	private String mStrStoreNumber = "";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootView = inflater.inflate(R.layout.fragment_customer_detail, container, false);
		commonInit();
		return mRootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		Global.gFragIdx = Global.FRAG_CUSTOMER_DETAIL;
	}

	
	@Override
	public void onPause() {
		super.onPause();
		doComplete();
	}

	@Override
	public void initView() {
		mRootView.findViewById(R.id.btnBack).setOnClickListener(this);
		mRootView.findViewById(R.id.btnEditCustomer).setOnClickListener(this);
		mRootView.findViewById(R.id.btnMap).setOnClickListener(this);
		mRootView.findViewById(R.id.btnCall).setOnClickListener(this);
		mRootView.findViewById(R.id.btnPhoto).setOnClickListener(this);
		mRootView.findViewById(R.id.btnThumbnail).setOnClickListener(this);
		mRootView.findViewById(R.id.btnGPSPlot).setOnClickListener(this);

		mCustomerInfo = (CustomerInfo) getArguments().getSerializable(Global.EXTRA_CUSTOMER_INFO);
		loadData();
		refreshLanguage();
	}

	@Override
	public void loadData() {
		mContactList.clear();
		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		mContactList = dbAdapter.getCustContactList(mCustomerInfo.nChainNo, mCustomerInfo.nCustNo);
		List<DescTypeInfo> paymentTypeDescTypeList = dbAdapter.getDescTypeListWithAlphaKey(Global.TERMS, mCustomerInfo.strTerms);
		List<DescTypeInfo> priceGroupDescTypeList = dbAdapter.getDescTypeListWithNumericKey(Global.PRICE_GROUP, mCustomerInfo.nPriceGrp);
		List<DescTypeInfo> visitFrequencyTypeList = dbAdapter.getDescTypeListWithNumericKey(Global.VISIT_FREQUENCY, mCustomerInfo.nDeliveryFreq);
		dbAdapter.close();
		mStrPaymentType = "";
		for(DescTypeInfo descTypeInfo : paymentTypeDescTypeList)
		{
			if(mStrPaymentType.length() > 0)
			{
				mStrPaymentType += "\n";
			}
			mStrPaymentType += descTypeInfo.strDescription.trim();
		}

		mStrPriceGroup = "";
		for(DescTypeInfo descTypeInfo : priceGroupDescTypeList)
		{
			if(mStrPriceGroup.length() > 0)
			{
				mStrPriceGroup += "\n";
			}
			mStrPriceGroup += descTypeInfo.strDescription.trim();
		}

		mStrVisitFrequency = "";
		for(DescTypeInfo descTypeInfo : visitFrequencyTypeList)
		{
			if(mStrVisitFrequency.length() > 0)
			{
				mStrVisitFrequency += "\n";
			}
			mStrVisitFrequency += descTypeInfo.strDescription.trim();
		}

		mStrStoreNumber = mCustomerInfo.strStoreNo;
		refreshPage();
	}

	@Override
	public void refreshPage() {
		mStrCustomerPhotoPath = String.format(Locale.ENGLISH, "%s%05d_%09d.jpg", Global.CUSTOMER_PHOTO_DIR, mCustomerInfo.nChainNo, mCustomerInfo.nCustNo);
		if (Utils.isFileExist(mStrCustomerPhotoPath) == true) {
			((Button) mRootView.findViewById(R.id.btnThumbnail)).setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_thumbnail, 0, 0, 0);
			mRootView.findViewById(R.id.btnThumbnail).setEnabled(true);
			//mRootView.findViewById(R.id.btnPhoto).setVisibility(View.INVISIBLE);
		} else {
			((Button) mRootView.findViewById(R.id.btnThumbnail)).setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_no_photo, 0, 0, 0);
			mRootView.findViewById(R.id.btnThumbnail).setEnabled(false);
			//mRootView.findViewById(R.id.btnPhoto).setVisibility(View.VISIBLE);
		}

		((TextView) mRootView.findViewById(R.id.txtCustomerName)).setText(mCustomerInfo.strName);
		((TextView) mRootView.findViewById(R.id.txtAddress1)).setText(mCustomerInfo.strAddress1);
		((TextView) mRootView.findViewById(R.id.txtAddress2)).setText(mCustomerInfo.strAddress2);
		((TextView) mRootView.findViewById(R.id.txtCity)).setText(mCustomerInfo.strCity);
		((TextView) mRootView.findViewById(R.id.txtPhone)).setText(mCustomerInfo.strPhone);
		if(mCustomerInfo.nChainNo != 0)
		{
			((TextView) mRootView.findViewById(R.id.txtCustomerNo)).setText(mCustomerInfo.nChainNo + "/" + mCustomerInfo.nCustNo);
		}
		else
		{
			((TextView) mRootView.findViewById(R.id.txtCustomerNo)).setText(String.valueOf(mCustomerInfo.nCustNo));
		}
		String strTime1 = "";
		if (mCustomerInfo.strStartTime1.length() > 0 && mCustomerInfo.strEndTime1.length() > 0) {
			int nStartTime = Utils.parseIntFromString(mCustomerInfo.strStartTime1);
			int nEndTime = Utils.parseIntFromString(mCustomerInfo.strEndTime1);
			if( nStartTime != 0 || nEndTime != 0)
			{
				String strStartTime = "";
				String strEndTime = "";
				SimpleDateFormat sdf = new SimpleDateFormat("kkmm");
				SimpleDateFormat sdf_target = new SimpleDateFormat("h:mm a");
				try
				{
					Date start_date = sdf.parse(mCustomerInfo.strStartTime1);
					strStartTime = sdf_target.format(start_date);
				}catch (Exception e)
				{
					e.printStackTrace();
				}
				try
				{
					Date end_date = sdf.parse(mCustomerInfo.strEndTime1);
					strEndTime = sdf_target.format(end_date);
				}catch (Exception e)
				{
					e.printStackTrace();
				}

				strTime1 = String.format(Locale.ENGLISH, "%s to %s", strStartTime, strEndTime);
			}

		}

		if(!TextUtils.isEmpty(strTime1))
		{
			((TextView) mRootView.findViewById(R.id.txtStartEnd1)).setText(strTime1);
		}

		String strTime2 = "";
		if (mCustomerInfo.strStartTime2.length() > 0 && mCustomerInfo.strEndTime2.length() > 0) {
			int nStartTime = Utils.parseIntFromString(mCustomerInfo.strStartTime2);
			int nEndTime = Utils.parseIntFromString(mCustomerInfo.strEndTime2);
			if( nStartTime != 0 || nEndTime != 0)
			{
				String strStartTime = "";
				String strEndTime = "";
				SimpleDateFormat sdf = new SimpleDateFormat("kkmm");
				SimpleDateFormat sdf_target = new SimpleDateFormat("h:mm a");
				try
				{
					Date start_date = sdf.parse(mCustomerInfo.strStartTime2);
					strStartTime = sdf_target.format(start_date);
				}catch (Exception e)
				{
					e.printStackTrace();
				}
				try
				{
					Date end_date = sdf.parse(mCustomerInfo.strEndTime2);
					strEndTime = sdf_target.format(end_date);
				}catch (Exception e)
				{
					e.printStackTrace();
				}

				strTime2 = String.format(Locale.ENGLISH, "%s to %s", strStartTime, strEndTime);
			}

		}
		if(!TextUtils.isEmpty(strTime2))
		{
			if(TextUtils.isEmpty(strTime1))
			{
				((TextView) mRootView.findViewById(R.id.txtStartEnd1)).setText(strTime2);
			}
			else
			{
				((TextView) mRootView.findViewById(R.id.txtStartEnd2)).setText(strTime2);
			}
		}

		if(TextUtils.isEmpty(strTime1) && TextUtils.isEmpty(strTime2))
		{
			mRootView.findViewById(R.id.layoutSales).setVisibility(View.GONE);
		}
		else
		{
			mRootView.findViewById(R.id.layoutSales).setVisibility(View.VISIBLE);
		}

		if( (mCustomerInfo.driverLatitude != 0 && mCustomerInfo.driverLongitude != 0) ||
				(mCustomerInfo.latitude != 0 && mCustomerInfo.longitude != 0))
		{
			mRootView.findViewById(R.id.btnMap).setEnabled(true);
		}
		else
		{
			String strAddress = mCustomerInfo.strAddress1;
			if(!TextUtils.isEmpty(strAddress))
			{
				strAddress += ", " + mCustomerInfo.strAddress2;
			}
			final String finalStrAddress = strAddress;
			new Thread()
			{
				@Override
				public void run() {
					final LatLng latLng = Utils.getLocationFromAddress(mContext, finalStrAddress);
					new Handler(Looper.getMainLooper()).post(new Runnable() {
						@Override
						public void run() {
							if(latLng != null)
							{
								mRootView.findViewById(R.id.btnMap).setEnabled(true);
							}
							else
							{
								mRootView.findViewById(R.id.btnMap).setEnabled(false);
							}
						}
					});

				}
			}.start();

			mRootView.findViewById(R.id.btnMap).setEnabled(false);
		}
		mRootView.findViewById(R.id.btnGPSPlot).setVisibility(View.VISIBLE);

		mRootView.findViewById(R.id.txtCustomerName).setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				showContactDialog(mCustomerInfo.strPhone, mCustomerInfo.strEmail);
				return false;
			}
		});

		LinearLayout contactLayout = (LinearLayout) mRootView.findViewById(R.id.contactList);
		contactLayout.removeAllViews();
		for(final CustContactInfo info : mContactList)
		{
			View view = mContext.getLayoutInflater().inflate(R.layout.row_customer_detail, contactLayout, false);
			((TextView) view.findViewById(R.id.txtContactTypeDesc)).setText(info.strContactTypeDesc);
			((TextView) view.findViewById(R.id.txtContactName)).setText(info.strContactName);
			((TextView) view.findViewById(R.id.txtContactPhoneNumber)).setText(info.strContactPhoneNumber);
			((TextView) view.findViewById(R.id.txtContactEmailAddress)).setText(info.strContactEmailAddress);
			if (info.strContactInfo.equals("") == true) {
				view.findViewById(R.id.btnContactInfo).setVisibility(View.INVISIBLE);
			} else {
				view.findViewById(R.id.btnContactInfo).setVisibility(View.VISIBLE);
				view.findViewById(R.id.btnContactInfo).setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						showMessageDialog(Str.Notes[Global.LANG].toUpperCase(), info.strContactInfo);
					}
				});
			}

			view.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					showContactDialog(info.strContactPhoneNumber, info.strContactEmailAddress);
					return false;
				}
			});
			contactLayout.addView(view);
		}

		if(TextUtils.isEmpty(mStrPaymentType))
		{
			mRootView.findViewById(R.id.layoutPaymentType).setVisibility(View.GONE);
		}
		else
		{
			mRootView.findViewById(R.id.layoutPaymentType).setVisibility(View.VISIBLE);
			((TextView)mRootView.findViewById(R.id.txtPaymentType)).setText(mStrPaymentType);
		}

		if(TextUtils.isEmpty(mStrPriceGroup))
		{
			mRootView.findViewById(R.id.layoutPriceGroup).setVisibility(View.GONE);
		}
		else
		{
			mRootView.findViewById(R.id.layoutPriceGroup).setVisibility(View.VISIBLE);
			((TextView)mRootView.findViewById(R.id.txtPriceGroup)).setText(mStrPriceGroup);
		}

		if(TextUtils.isEmpty(mStrVisitFrequency))
		{
			mRootView.findViewById(R.id.layoutVisitFrequency).setVisibility(View.GONE);
		}
		else
		{
			mRootView.findViewById(R.id.layoutVisitFrequency).setVisibility(View.VISIBLE);
			((TextView)mRootView.findViewById(R.id.txtVisitFrequency)).setText(mStrVisitFrequency);
		}

		if(TextUtils.isEmpty(mStrStoreNumber) || mStrStoreNumber.equals("0"))
		{
			mRootView.findViewById(R.id.layoutStoreNumber).setVisibility(View.GONE);
		}
		else
		{
			mRootView.findViewById(R.id.layoutStoreNumber).setVisibility(View.VISIBLE);
			((TextView)mRootView.findViewById(R.id.txtStoreNumber)).setText(mStrStoreNumber);
		}

		if(TextUtils.isEmpty(mStrPaymentType) && TextUtils.isEmpty(mStrPriceGroup) && TextUtils.isEmpty(mStrVisitFrequency) && (TextUtils.isEmpty(mStrStoreNumber) || mStrStoreNumber.equals("0")))
		{
			mRootView.findViewById(R.id.split_terms).setVisibility(View.GONE);
		}
		else
		{
			mRootView.findViewById(R.id.split_terms).setVisibility(View.VISIBLE);
		}

		if (Utils.getStringSetting(mContext, Global.PREF_HIDE_CUST_MAINTENANCE).equals("1")) {
			mRootView.findViewById(R.id.btnEditCustomer).setVisibility(View.INVISIBLE);
		}

		if (Utils.getStringSetting(mContext, Global.PREF_DISABLE_GPS_PIN).equals("1")) {
			if (mCustomerInfo.driverLatitude != 0 && mCustomerInfo.driverLongitude != 0) {
				mRootView.findViewById(R.id.btnGPSPlot).setVisibility(View.INVISIBLE);
			}
		}
	}

	@Override
	public void refreshLanguage() {
		((TextView) mRootView.findViewById(R.id.lblTitle)).setText(Str.Details[Global.LANG].toUpperCase());
		((TextView) mRootView.findViewById(R.id.lblDelivery)).setText(Str.SalesOnDetail[Global.LANG]);
		((TextView) mRootView.findViewById(R.id.lblCustomerNo)).setText(Str.Customer[Global.LANG]);
		((TextView) mRootView.findViewById(R.id.lblPaymentType)).setText(Str.PaymentType[Global.LANG]);
		((TextView) mRootView.findViewById(R.id.lblPriceGroup)).setText(Str.PriceGroup[Global.LANG]);
		((TextView) mRootView.findViewById(R.id.lblVisitFrequency)).setText(Str.Visit[Global.LANG]);
		((TextView) mRootView.findViewById(R.id.lblStoreNumber)).setText(Str.StoreNumber[Global.LANG]);
	}

	@Override
	public void receiveBroadcast(String strAction, Intent intent) {
		if (strAction.equals(Global.ACTION_TAKE_PHOTO) == true) {
			mStrPhotoPath = intent.getStringExtra(Global.EXTRA_PHOTO_PATH);
			Utils.createDirectory(Global.CUSTOMER_PHOTO_DIR);
			Utils.fileCopy(mStrPhotoPath, mStrCustomerPhotoPath);
			refreshPage();
		} else if (strAction.equals(Global.ACTION_REFRESH_CUSTOMER_DETAIL) == true) {
			mCustomerInfo = (CustomerInfo) intent.getSerializableExtra(Global.EXTRA_CUSTOMER_INFO);
			loadData();
		}
	}

	private void callPhone(String strPhoneNumber) {
		if (strPhoneNumber.equals("") == true) {
			Utils.printDebug(mContext, Str.Invalid_phone_number[Global.LANG]);
		} else {
			Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + strPhoneNumber));
			startActivity(intent);
		}
	}

	private void sendSMS(String strPhoneNumber) {
		if (strPhoneNumber.equals("") == true) {
			Utils.printDebug(mContext, Str.Invalid_phone_number[Global.LANG]);
		} else {
			Uri uri = Uri.parse("smsto:" + strPhoneNumber);
			startActivity(new Intent(Intent.ACTION_SENDTO, uri));
		}
	}

	private void sendEmail(String strEmailAddress) {
		if (strEmailAddress.equals("") == true) {
			Utils.printDebug(mContext, Str.Invalid_email_address[Global.LANG]);
		} else {
			final Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
			emailIntent.setType("text/plain");
			emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{strEmailAddress});
			emailIntent.putExtra(Intent.EXTRA_SUBJECT, Str.Subject[Global.LANG]);
			emailIntent.putExtra(Intent.EXTRA_TEXT, Str.Body[Global.LANG]);
			startActivity(Intent.createChooser(emailIntent, Str.Send_mail[Global.LANG]));
		}
	}

	private void showContactDialog(final String strPhone, final String strEmail) {
		try {
			final ContactDialog dlg = new ContactDialog(mContext);
			dlg.mStrCustomerName = mCustomerInfo.strName;
			dlg.setCancelable(false);
			dlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
					if (dlg.mReturnCode == Global.CONTACT_RETURN_TELEPHONE) {
						callPhone(strPhone);
					} else if (dlg.mReturnCode == Global.CONTACT_RETURN_SMS) {
						sendSMS(strPhone);
					} else if (dlg.mReturnCode == Global.CONTACT_RETURN_EMAIL) {
						sendEmail(strEmail);
					}
				}
			});
			dlg.show();
		}catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void showMessageDialog(String strTitle, String strMessage) {
		try {
			final MessageDialog msgDialog = new MessageDialog(mContext);
			msgDialog.mStrTitle = strTitle;
			msgDialog.mStrMiddle = Str.Ok[Global.LANG];
			msgDialog.mStrMessage = strMessage;
			msgDialog.setCancelable(false);
			msgDialog.show();
		}catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void doComplete() {
		if (mStrPhotoPath.length() > 0) {
			Global.gCurCustomerInfo = mCustomerInfo;
			UFileTransactionInfo uFileTransactionInfo = new UFileTransactionInfo();
			setUCameraData("MAIT", mStrPhotoPath, String.format(Locale.ENGLISH, "%05d_%09d.jpg", mCustomerInfo.nChainNo, mCustomerInfo.nCustNo), uFileTransactionInfo, 1, true);
			/*setUImageFileTransactionInfo(0, 0, uFileTransactionInfo.nFileTrxnNo, "", "CUSTOMER_INFO", "CUSTOMER_INFO",
					uFileTransactionInfo.strFileCreateDate, uFileTransactionInfo.strFileCreateTime, String.format(Locale.ENGLISH, "%05d_%09d.jpg", mCustomerInfo.nChainNo, mCustomerInfo.nCustNo));*/
			//Global.gUploadCustNoList.add(makeZipFileInfo);
			Utils.registerUploadFile(mContext, makeZipFileInfo);
			mStrPhotoPath = "";
		}
	}

	boolean m_bClicked = false;
	@Override
	public void onClick(View v) {
		if( m_bClicked ) return;
		m_bClicked = true;
		switch (v.getId()) {
			case R.id.btnBack:
//				doComplete();
				mContext.onBackPressed();
				break;
			case R.id.btnEditCustomer: {
				Fragment fragment = new NewCustomerFragment();
				Bundle bundle = new Bundle();
				bundle.putBoolean(Global.EXTRA_IS_EDIT_CUSTOMER, true);
				bundle.putSerializable(Global.EXTRA_CUSTOMER_INFO, mCustomerInfo);
				fragment.setArguments(bundle);
				mContext.replaceFragment(fragment);
			}
				break;
			case R.id.btnMap:
				if (mCustomerInfo != null) {
					Fragment fragment = new CustomerMapviewFragment();
					Bundle bundle = new Bundle();
					bundle.putBoolean(Global.EXTRA_IS_ALL_CUSTOMER, false);
					bundle.putSerializable(Global.EXTRA_CUSTOMER_INFO, mCustomerInfo);
					fragment.setArguments(bundle);
					mContext.replaceFragment(fragment);
				}
				break;
			case R.id.btnCall:
				callPhone(mCustomerInfo.strPhone);
				break;
			case R.id.btnPhoto:
				//if (Utils.isFileExist(mStrCustomerPhotoPath) == false) {
					selectPhoto();
				//}
				break;
			case R.id.btnThumbnail:
				if (Utils.isFileExist(mStrCustomerPhotoPath) == true) {
					Fragment fragment = new CustomerPhotoviewFragment();
					Bundle bundle = new Bundle();
					bundle.putString(Global.EXTRA_PHOTO_PATH, mStrCustomerPhotoPath);
					fragment.setArguments(bundle);
					mContext.replaceFragment(fragment);
				}
				break;
			case R.id.btnGPSPlot:
				if(ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
						ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
				{
					Toast.makeText(mContext, Str.LocationPermissionDenied[Global.LANG], Toast.LENGTH_LONG).show();
					return;
				}

				showProgressDialog("Getting current location...");

				LocationServices.getFusedLocationProviderClient(mContext).getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
					@Override
					public void onComplete(@NonNull Task<Location> task) {
						if (task.isSuccessful() && task.getResult() != null) {
							hideProgressDialog();
							Location location = task.getResult();
							doGPSPlot(location);
						} else {
							hideProgressDialog();
							Toast.makeText(mContext, Str.FailedToGetCurrentLocation[Global.LANG], Toast.LENGTH_LONG).show();
						}
					}
				});
				break;
		}
		m_bClicked = false;
	}

	private void doGPSPlot(Location location)
	{
		double nMyLat = location.getLatitude();
		double nMyLng = location.getLongitude();
		if (nMyLat == 0 || nMyLng == 0) {
			showMessageDialog(Str.Alert[Global.LANG].toUpperCase(), Str.Cant_get_location_info_please_try_again_later[Global.LANG]);
		}
		else {
			mCustomerInfo.driverLatitude = nMyLat;
			mCustomerInfo.driverLongitude = nMyLng;
			DbAdapter dbAdapter = new DbAdapter(mContext);
			dbAdapter.open();
			dbAdapter.updateCustomerInfo(mCustomerInfo);
			dbAdapter.close();

			initMakeZipInfo();
			long nTrxnNo = setGeneralUploadData("MAIT", mCustomerInfo.nChainNo, mCustomerInfo.nCustNo, -1);

			TransactionInfo transactionInfo = new TransactionInfo();
			transactionInfo.strType = "MAIT";
			transactionInfo.strTime = new SimpleDateFormat("kk:mm").format(new Date(nTrxnNo));
			transactionInfo.strCustomerName = mCustomerInfo.strName;
			dbAdapter.open();
			dbAdapter.insertTransactionHistoryInfo(transactionInfo);
			dbAdapter.close();

			UGPSInfo uGPSInfo = new UGPSInfo();
			uGPSInfo.nTrxnNo = m_lTrxnNo;
			uGPSInfo.nChainNo = mCustomerInfo.nChainNo;
			uGPSInfo.nCustNo = mCustomerInfo.nCustNo;
			uGPSInfo.nStartTrxnNo = m_lTrxnNo;
			uGPSInfo.strGPSData = String.format(Locale.ENGLISH, "%f, %f", nMyLat, nMyLng);
			uGPSInfo.strDocType = "MAIT";
			uGPSInfo.strDocNo = "0";

			uGPSInfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(mContext, mCurDate));
			uGPSInfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(mCurDate);
			dbAdapter.open();
			dbAdapter.insertUGPSInfo(uGPSInfo);
			dbAdapter.close();

			//Global.gUploadCustNoList.add(makeZipFileInfo);
			Utils.registerUploadFile(mContext, makeZipFileInfo);
			showMessageDialog(Str.Alert[Global.LANG].toUpperCase(), Str.GPS_info_has_been_gathered_successfully[Global.LANG]);
			mRootView.findViewById(R.id.btnMap).setEnabled(true);
		}
	}
}