package dsd.dsdvansales.fragment;

import java.text.*;
import java.util.*;

import android.app.AlertDialog;
import android.content.*;
import android.os.*;
import android.text.*;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import dsd.dsdvansales.R;
import dsd.dsdvansales.db.DbAdapter;
import dsd.dsdvansales.dialogs.MessageDialog;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.model.ContactDetail;
import dsd.dsdvansales.model.ContactInfo;
import dsd.dsdvansales.model.CustContactInfo;
import dsd.dsdvansales.model.CustomerInfo;
import dsd.dsdvansales.model.DescTypeInfo;
import dsd.dsdvansales.model.TransactionInfo;
import dsd.dsdvansales.model.UBillingDetailsInfo;
import dsd.dsdvansales.model.UCustomerFMInfo;
import dsd.dsdvansales.views.AlertSingleChoiceAdapter;


public class NewCustomerFragment extends BaseFragment implements View.OnClickListener {

	private CustomerInfo mCustomerInfo = null;
	private boolean mIsEdit = false;
	private List<DescTypeInfo> mCustomerTypeList = new ArrayList<>();

	private UBillingDetailsInfo mBillingDetailsInfo = null;
	private List<ContactDetail> mContactDetailList = new ArrayList<>();
	private List<ContactInfo> mCustInfoList = new ArrayList<>();

	private CustomerInfo mTemplateCustomerInfo = null;
	private int nTemplateCustNo = 0;
	private int nTemplateChainNo = 0;
	private boolean bLoadData = false;

	TextWatcher mValueChangedListener = new TextWatcher() {
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {

		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {

		}

		@Override
		public void afterTextChanged(Editable s) {
			refreshPage();
		}
	};

	TimePickerDialog tpd;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if(mRootView == null)
		{
			mRootView = inflater.inflate(R.layout.fragment_new_customer, container, false);
			commonInit();
		}
		return mRootView;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		Global.gFragIdx = Global.FRAG_NEW_CUSTOMER;

		if(Global.gUBillingDetailsInfo != null)
		{
			mBillingDetailsInfo = Global.gUBillingDetailsInfo;
			Global.gUBillingDetailsInfo = null;
		}
		refreshPage();
	}

	@Override
	public void initView() {
		mRootView.findViewById(R.id.btnBack).setOnClickListener(this);
		mRootView.findViewById(R.id.btnAdd).setOnClickListener(this);
		mRootView.findViewById(R.id.btnBillingDetails).setOnClickListener(this);
		mRootView.findViewById(R.id.btnContacts).setOnClickListener(this);
		mRootView.findViewById(R.id.btnCustomerInfo).setOnClickListener(this);

		InputFilter[] filterArray = new InputFilter[2];
		filterArray[0] = new InputFilter.LengthFilter(3);
		filterArray[1] = new InputFilter.AllCaps();
		((EditText) mRootView.findViewById(R.id.editState)).setFilters(filterArray);

		mRootView.findViewById(R.id.txtDeliveryFrom).setOnClickListener(view -> {
			SimpleDateFormat sdf = new SimpleDateFormat("h:mm a");
			String strTime = ((TextView)mRootView.findViewById(R.id.txtDeliveryFrom)).getText().toString();
			Date date = new Date();
			try {
				date = sdf.parse(strTime);
			} catch (ParseException e) {
				e.printStackTrace();
			}

			if(tpd == null)
			{
				tpd = TimePickerDialog.newInstance((view1, hourOfDay, minute, second) -> {
					SimpleDateFormat sdf1 = new SimpleDateFormat("h:mm a");
					Date date1 = new Date(0, 0, 0, hourOfDay, minute);

					String strToTime = ((TextView)mRootView.findViewById(R.id.txtDeliveryTo)).getText().toString();
					Date toDate = date1;
					try
					{
						toDate = sdf1.parse(strToTime);
						toDate = new Date(0, 0, 0, toDate.getHours(), toDate.getMinutes());
					}catch (Exception e)
					{
						e.printStackTrace();
					}

					if(date1.compareTo(toDate) >= 0)
					{
						//Increase To Hour 1 more Automatically
						Calendar calendar = Calendar.getInstance();
						calendar.setTime(date1);
						calendar.add(Calendar.HOUR, 1);
						toDate = calendar.getTime();
						((TextView)mRootView.findViewById(R.id.txtDeliveryTo)).setText(sdf1.format(toDate));
					}

					String strTime1 = sdf1.format(date1);
					((TextView)mRootView.findViewById(R.id.txtDeliveryFrom)).setText(strTime1);
				}, date.getHours(), date.getMinutes(), 0,true);
			}
			else
			{
				tpd.initialize((view12, hourOfDay, minute, second) -> {
					SimpleDateFormat sdf12 = new SimpleDateFormat("h:mm a");
					Date date12 = new Date(0, 0, 0, hourOfDay, minute);

					String strToTime = ((TextView)mRootView.findViewById(R.id.txtDeliveryTo)).getText().toString();
					Date toDate = date12;
					try
					{
						toDate = sdf12.parse(strToTime);
						toDate = new Date(0, 0, 0, toDate.getHours(), toDate.getMinutes());
					}catch (Exception e)
					{
						e.printStackTrace();
					}

					if(date12.compareTo(toDate) >= 0)
					{
						//Increase To Hour 1 more Automatically
						Calendar calendar = Calendar.getInstance();
						calendar.setTime(date12);
						calendar.add(Calendar.HOUR, 1);
						toDate = calendar.getTime();
						((TextView)mRootView.findViewById(R.id.txtDeliveryTo)).setText(sdf12.format(toDate));
					}

					String strTime12 = sdf12.format(date12);
					((TextView)mRootView.findViewById(R.id.txtDeliveryFrom)).setText(strTime12);
				},  date.getHours(), date.getMinutes(), 0, true);
			}
			tpd.enableSeconds(false);
			tpd.setAccentColor(mContext.getResources().getColor(R.color.top_banner_color));
			tpd.show(mContext.getFragmentManager(), "Timepickerdialog");
		});

		mRootView.findViewById(R.id.txtDeliveryTo).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				SimpleDateFormat sdf = new SimpleDateFormat("h:mm a");
				String strTime = ((TextView)mRootView.findViewById(R.id.txtDeliveryTo)).getText().toString();
				Date date = new Date();
				try {
					date = sdf.parse(strTime);
				} catch (ParseException e) {
					e.printStackTrace();
				}

				if(tpd == null)
				{
					tpd = TimePickerDialog.newInstance(new TimePickerDialog.OnTimeSetListener() {
						@Override
						public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
							SimpleDateFormat sdf = new SimpleDateFormat("h:mm a");
							Date date = new Date(0, 0, 0, hourOfDay, minute);

							String strFromDate = ((TextView)mRootView.findViewById(R.id.txtDeliveryFrom)).getText().toString();
							if(!TextUtils.isEmpty(strFromDate))
							{
								try {
									Date fromDate = sdf.parse(strFromDate);
									fromDate = new Date(0, 0, 0, fromDate.getHours(), fromDate.getMinutes());
									if(date.compareTo(fromDate) < 0)
									{
										if(!mContext.isFinishing() && !mContext.isDestroyed())
										{
											try {
												Toast.makeText(mContext, Str.ToTimeLessThanFromTime[Global.LANG], Toast.LENGTH_LONG).show();
											}catch (Exception e1)
											{
												e1.printStackTrace();
											}
										}
										return;
									}
								} catch (ParseException e) {
									e.printStackTrace();
								}
							}

							String strTime = sdf.format(date);
							((TextView)mRootView.findViewById(R.id.txtDeliveryTo)).setText(strTime);
						}
					}, date.getHours(), date.getMinutes(), 0,true);
				}
				else
				{
					tpd.initialize((view13, hourOfDay, minute, second) -> {
						SimpleDateFormat sdf13 = new SimpleDateFormat("h:mm a");
						Date date13 = new Date(0, 0, 0, hourOfDay, minute);

						String strFromDate = ((TextView)mRootView.findViewById(R.id.txtDeliveryFrom)).getText().toString();
						if(!TextUtils.isEmpty(strFromDate))
						{
							try {
								Date fromDate = sdf13.parse(strFromDate);
								fromDate = new Date(0, 0, 0, fromDate.getHours(), fromDate.getMinutes());
								if(date13.compareTo(fromDate) < 0)
								{
									if(!mContext.isFinishing() && !mContext.isDestroyed())
									{
										try {
											Toast.makeText(mContext, Str.DeliveryWindowEndTimeWarning[Global.LANG], Toast.LENGTH_LONG).show();
										}catch (Exception e1)
										{
											e1.printStackTrace();
										}
									}
									return;
								}
							} catch (ParseException e) {
								e.printStackTrace();
							}
						}

						String strTime13 = sdf13.format(date13);
						((TextView)mRootView.findViewById(R.id.txtDeliveryTo)).setText(strTime13);
					},  date.getHours(), date.getMinutes(), 0, true);
				}
				tpd.enableSeconds(false);
				tpd.setAccentColor(mContext.getResources().getColor(R.color.top_banner_color));
				tpd.show(mContext.getFragmentManager(), "Timepickerdialog");
			}
		});
		mRootView.findViewById(R.id.txtCustomerType).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				showCustomerTypeList();
			}
		});

		((EditText)mRootView.findViewById(R.id.editCompanyTaxID)).addTextChangedListener(mValueChangedListener);
		((EditText)mRootView.findViewById(R.id.editCompanyName)).addTextChangedListener(mValueChangedListener);
		((EditText)mRootView.findViewById(R.id.editAddress1)).addTextChangedListener(mValueChangedListener);
		((EditText)mRootView.findViewById(R.id.editAddress2)).addTextChangedListener(mValueChangedListener);
		((EditText)mRootView.findViewById(R.id.editCity)).addTextChangedListener(mValueChangedListener);
		((EditText)mRootView.findViewById(R.id.editState)).addTextChangedListener(mValueChangedListener);
		((EditText)mRootView.findViewById(R.id.editPostCode)).addTextChangedListener(mValueChangedListener);
		((EditText)mRootView.findViewById(R.id.editPhone)).addTextChangedListener(mValueChangedListener);

		if(!bLoadData)
		{
			mIsEdit = getArguments().getBoolean(Global.EXTRA_IS_EDIT_CUSTOMER, false);
			mCustomerInfo = (CustomerInfo) getArguments().getSerializable(Global.EXTRA_CUSTOMER_INFO);
			loadData();
		}

		refreshLanguage();
	}

	private void showCustomerTypeList() {
		final String strArray[] = new String[mCustomerTypeList.size() + 1];
		strArray[0] = Str.not_selected[Global.LANG];
		int origIdx = 0;
		for (int i = 0; i < mCustomerTypeList.size(); i++) {
			if(Utils.getStringSetting(mContext, Global.PREF_CUSTTYPE).equals("2"))
			{
				if(mCustomerTypeList.get(i).strAlphaKey.trim().equals(mCustomerInfo.strUser1.trim()))
				{
					origIdx = i + 1;
				}
			}
			else
			{
				if(mCustomerTypeList.get(i).strAlphaKey.trim().equals(mCustomerInfo.strType.trim()))
				{
					origIdx = i + 1;
				}
			}

			strArray[i + 1] = mCustomerTypeList.get(i).strDescription;
		}

		final String origText = ((TextView) mRootView.findViewById(R.id.txtCustomerType)).getText().toString();
		final String origType;
		if(origIdx == 0)
		{
			origType = "";
		}
		else
		{
			origType = mCustomerTypeList.get(origIdx - 1).strAlphaKey;
		}
		try {
			new AlertDialog.Builder(mContext)
					.setTitle(Str.SelectCustomerType[Global.LANG])
					.setSingleChoiceItems(new AlertSingleChoiceAdapter(mContext, strArray), origIdx, new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog, int nSelected)
						{
							if(Utils.getStringSetting(mContext, Global.PREF_CUSTTYPE).equals("2"))
							{
								if(nSelected == 0)
								{
									mCustomerInfo.strUser1 = "";
								}
								else
								{
									mCustomerInfo.strUser1 = mCustomerTypeList.get(nSelected- 1).strAlphaKey;
								}
							}
							else
							{
								if(nSelected == 0)
								{
									mCustomerInfo.strType = "";
								}
								else
								{
									mCustomerInfo.strType = mCustomerTypeList.get(nSelected- 1).strAlphaKey;
								}
							}

							((TextView) mRootView.findViewById(R.id.txtCustomerType)).setText(strArray[nSelected]);
						}
					})
					.setPositiveButton(Str.Ok[Global.LANG], new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog, int nSelected)
						{

						}
					})
					.setNegativeButton(Str.Cancel[Global.LANG], new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog, int nSelected)
						{
							if(Utils.getStringSetting(mContext, Global.PREF_CUSTTYPE).equals("2"))
							{
								mCustomerInfo.strUser1 = origType;
							}
							else
							{
								mCustomerInfo.strType = origType;
							}

							((TextView) mRootView.findViewById(R.id.txtCustomerType)).setText(origText);
						}
					})
					.create()
					.show();
		}catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	private boolean mNameMandantory = false;
	private boolean mCompanyTaxIdMandantory = false;
	private boolean mAddress1Mandantory = false;
	private boolean mAddress2Mandantory = false;
	private boolean mCityMandantory = false;
	private boolean mShipToStateMandantory = false;
	private boolean mShipToZipMandantory = false;
	private boolean mPhoneMandantory = false;

	@Override
	public void loadData() {
		if (mCustomerInfo != null) {
			mTemplateCustomerInfo = mCustomerInfo;
			nTemplateCustNo = mCustomerInfo.nCustNo;
			nTemplateChainNo = mCustomerInfo.nChainNo;

			DbAdapter dbAdapter = new DbAdapter(mContext);
			dbAdapter.open();
			if(Utils.getStringSetting(mContext, Global.PREF_CUSTTYPE).equals("2"))
			{
				mCustomerTypeList = dbAdapter.getDescTypeList("CUDEF1");
			}
			else
			{
				mCustomerTypeList = dbAdapter.getDescTypeList("CustomerType");
			}
			dbAdapter.close();

			((TextView) mRootView.findViewById(R.id.lblDeliveryDetails)).setText(mCustomerInfo.strName);
			if(!mIsEdit)
			{
				((EditText) mRootView.findViewById(R.id.editCompanyName)).setHint(mCustomerInfo.strName);
				mNameMandantory = !TextUtils.isEmpty(mCustomerInfo.strName);
				((EditText) mRootView.findViewById(R.id.editCompanyTaxID)).setHint(mCustomerInfo.strCompanyTaxID);
				mCompanyTaxIdMandantory = !TextUtils.isEmpty(mCustomerInfo.strCompanyTaxID);
				((EditText) mRootView.findViewById(R.id.editAddress1)).setHint(mCustomerInfo.strAddress1);
				mAddress1Mandantory = !TextUtils.isEmpty(mCustomerInfo.strAddress1);
				((EditText) mRootView.findViewById(R.id.editAddress2)).setHint(mCustomerInfo.strAddress2);
				mAddress2Mandantory = !TextUtils.isEmpty(mCustomerInfo.strAddress2);
				((EditText) mRootView.findViewById(R.id.editCity)).setHint(mCustomerInfo.strCity);
				mCityMandantory = !TextUtils.isEmpty(mCustomerInfo.strCity);
				((EditText) mRootView.findViewById(R.id.editState)).setHint(mCustomerInfo.strShipToState);
				mShipToStateMandantory = !TextUtils.isEmpty(mCustomerInfo.strShipToState);
				((EditText) mRootView.findViewById(R.id.editPostCode)).setHint(mCustomerInfo.strShipToZip);
				mShipToZipMandantory = !TextUtils.isEmpty(mCustomerInfo.strShipToZip);
				((EditText) mRootView.findViewById(R.id.editPhone)).setHint(mCustomerInfo.strPhone);
				mPhoneMandantory = !TextUtils.isEmpty(mCustomerInfo.strPhone);
			}
			else
			{
				((EditText) mRootView.findViewById(R.id.editCompanyName)).setText(mCustomerInfo.strName);
				((EditText) mRootView.findViewById(R.id.editCompanyTaxID)).setText(mCustomerInfo.strCompanyTaxID);
				((EditText) mRootView.findViewById(R.id.editAddress1)).setText(mCustomerInfo.strAddress1);
				((EditText) mRootView.findViewById(R.id.editAddress2)).setText(mCustomerInfo.strAddress2);
				((EditText) mRootView.findViewById(R.id.editCity)).setText(mCustomerInfo.strCity);
				((EditText) mRootView.findViewById(R.id.editState)).setText(mCustomerInfo.strShipToState);
				((EditText) mRootView.findViewById(R.id.editPostCode)).setText(mCustomerInfo.strShipToZip);
				((EditText) mRootView.findViewById(R.id.editPhone)).setText(mCustomerInfo.strPhone);

				dbAdapter.open();
				mBillingDetailsInfo = dbAdapter.getUBillingDetailsInfo(mCustomerInfo.nCustNo, mCustomerInfo.nChainNo);
				mContactDetailList = dbAdapter.getContactDetailList(mCustomerInfo.nCustNo, mCustomerInfo.nChainNo);
				mCustInfoList = dbAdapter.getCustAdditionalInfoList(mCustomerInfo.nCustNo, mCustomerInfo.nChainNo);
				dbAdapter.close();
			}

			if(!mIsEdit)
			{
				SimpleDateFormat sdf = new SimpleDateFormat("h:mm a");
				SimpleDateFormat sdf_src = new SimpleDateFormat("kkmm");

				String strStartTime = "0:0 AM";
				try {
					Date date = sdf_src.parse(mCustomerInfo.strDeliveryWindowFrom);
					strStartTime = sdf.format(date);
				}catch (Exception e)
				{
					e.printStackTrace();
				}
				((TextView)mRootView.findViewById(R.id.txtDeliveryFrom)).setText(strStartTime);

				String strEndTime = "0:0 AM";
				try {
					Date date = sdf_src.parse(mCustomerInfo.strDeliveryWindowTo);
					strEndTime = sdf.format(date);
				}catch (Exception e)
				{
					e.printStackTrace();
				}
				((TextView)mRootView.findViewById(R.id.txtDeliveryTo)).setText(strEndTime);

//				String strPPP = String.format(Locale.ENGLISH, "%03d", Utils.getIntSetting(mContext, Global.PREF_TEMP_CUST_PREFIX));
//				String strY = new SimpleDateFormat("yyyy").format(new Date()).substring(3);
//
//				Calendar cal = Calendar.getInstance();
//				int nDayOfYear = cal.get(Calendar.DAY_OF_YEAR);
//				String strJJJ = String.format(Locale.ENGLISH, "%03d", nDayOfYear);
//
//				String strToday = Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE);
//				if (Utils.getStringSetting(mContext, Global.PREF_TODAY).equals(strToday) == false) {
//					Utils.setStringSetting(mContext, Global.PREF_TODAY, strToday);
//					Utils.setIntSetting(mContext, Global.PREF_CUST_SEQ_NO, 0);
//				}
//				int nCustSeqNo = Utils.getIntSetting(mContext, Global.PREF_CUST_SEQ_NO) + 1;
//				Utils.setIntSetting(mContext, Global.PREF_CUST_SEQ_NO, nCustSeqNo);
//				String strSS = String.format(Locale.ENGLISH, "%02d", nCustSeqNo);
//				String strCustNo = String.format(Locale.ENGLISH, "%s%s%s%s", strPPP, strY, strJJJ, strSS);

				mCustomerInfo.nChainNo = Utils.getIntSetting(mContext, Global.PREF_TEMP_CUST_CHAIN);
				mCustomerInfo.nCustNo = Utils.parseIntFromString(getNewCustomerNumber());
			}
			else
			{
				SimpleDateFormat sdf = new SimpleDateFormat("h:mm a");
				SimpleDateFormat sdf_src = new SimpleDateFormat("kkmm");

				String strStartTime = "0:0 AM";
				try {
					Date date = sdf_src.parse(mCustomerInfo.strStartTime1);
					strStartTime = sdf.format(date);
				}catch (Exception e)
				{
					e.printStackTrace();
				}
				((TextView)mRootView.findViewById(R.id.txtDeliveryFrom)).setText(strStartTime);

				String strEndTime = "0:0 AM";
				try {
					Date date = sdf_src.parse(mCustomerInfo.strEndTime1);
					strEndTime = sdf.format(date);
				}catch (Exception e)
				{
					e.printStackTrace();
				}
				((TextView)mRootView.findViewById(R.id.txtDeliveryTo)).setText(strEndTime);
			}
		}
		else {
//			String strPPP = String.format(Locale.ENGLISH, "%03d", Utils.getIntSetting(mContext, Global.PREF_TEMP_CUST_PREFIX));
//			String strY = new SimpleDateFormat("yyyy").format(new Date()).substring(3);
//
//			Calendar cal = Calendar.getInstance();
//			int nDayOfYear = cal.get(Calendar.DAY_OF_YEAR);
//			String strJJJ = String.format(Locale.ENGLISH, "%03d", nDayOfYear);
//
//			String strToday = Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE);
//			if (Utils.getStringSetting(mContext, Global.PREF_TODAY).equals(strToday) == false) {
//				Utils.setStringSetting(mContext, Global.PREF_TODAY, strToday);
//				Utils.setIntSetting(mContext, Global.PREF_CUST_SEQ_NO, 0);
//			}
//			int nCustSeqNo = Utils.getIntSetting(mContext, Global.PREF_CUST_SEQ_NO) + 1;
//			Utils.setIntSetting(mContext, Global.PREF_CUST_SEQ_NO, nCustSeqNo);
//			String strSS = String.format(Locale.ENGLISH, "%02d", nCustSeqNo);
//			String strCustNo = String.format(Locale.ENGLISH, "%s%s%s%s", strPPP, strY, strJJJ, strSS);


			int nDefCustNumber = Utils.getIntFromString(Utils.getStringSetting(mContext, Global.PREF_TEMPLATE_CUST));
			int nDefChainNumber = Utils.getIntFromString(Utils.getStringSetting(mContext, Global.PREF_TEMPLATE_CHAIN));
			
			DbAdapter dbAdapter = new DbAdapter(mContext);
			dbAdapter.open();
			mCustomerInfo = dbAdapter.getCustomerInfo(nDefCustNumber, nDefChainNumber);
			if(Utils.getStringSetting(mContext, Global.PREF_CUSTTYPE).equals("2"))
			{
				mCustomerTypeList = dbAdapter.getDescTypeList("CUDEF1");
			}
			else
			{
				mCustomerTypeList = dbAdapter.getDescTypeList("CustomerType");
			}
			int nCustomerCount = dbAdapter.getCustomerListCount(true);
			dbAdapter.close();
			
			if( mCustomerInfo == null )
				mCustomerInfo = Global.gDefaultCustomerInfo.duplicate();

			mCustomerInfo.nChainNo = Utils.getIntSetting(mContext, Global.PREF_TEMP_CUST_CHAIN);
			mCustomerInfo.nCustNo = Utils.parseIntFromString(getNewCustomerNumber());
			mCustomerInfo.bIsRouteSch = true;
			mCustomerInfo.nSeqNo = 0;
			mCustomerInfo.nOrgSeqNo = nCustomerCount;
			mCustomerInfo.strStartTime1 = "0000";

			CustContactInfo info =  Global.gDefaultContactInfo.duplicate();
			
			info = Global.gDefaultContactInfo.duplicate();
			info.nChainNo = mCustomerInfo.nChainNo;
			info.nCustNo = mCustomerInfo.nCustNo;
			info.nContactType = Utils.getIntSetting(mContext, Global.PREF_PRIME_CONTACT);
			
			((TextView) mRootView.findViewById(R.id.lblDeliveryDetails)).setText(mCustomerInfo.strName);
			((EditText) mRootView.findViewById(R.id.editCompanyName)).setText(mCustomerInfo.strName);
			((EditText) mRootView.findViewById(R.id.editCompanyTaxID)).setText(mCustomerInfo.strCompanyTaxID);
			((EditText) mRootView.findViewById(R.id.editAddress1)).setText(mCustomerInfo.strAddress1);
			((EditText) mRootView.findViewById(R.id.editAddress2)).setText(mCustomerInfo.strAddress2);
			((EditText) mRootView.findViewById(R.id.editCity)).setText(mCustomerInfo.strCity);
			((EditText) mRootView.findViewById(R.id.editState)).setText(mCustomerInfo.strShipToState);
			((EditText) mRootView.findViewById(R.id.editPostCode)).setText(mCustomerInfo.strShipToZip);
			((EditText) mRootView.findViewById(R.id.editPhone)).setText(mCustomerInfo.strPhone);

			SimpleDateFormat sdf = new SimpleDateFormat("h:mm a");
			SimpleDateFormat sdf_src = new SimpleDateFormat("kkmm");

			String strStartTime = "0:0 AM";
			try {
				Date date = sdf_src.parse(mCustomerInfo.strStartTime1);
				strStartTime = sdf.format(date);
			}catch (Exception e)
			{
				e.printStackTrace();
			}
			((TextView)mRootView.findViewById(R.id.txtDeliveryFrom)).setText(strStartTime);

			String strEndTime = "0:0 AM";
			try {
				Date date = sdf_src.parse(mCustomerInfo.strEndTime1);
				strEndTime = sdf.format(date);
			}catch (Exception e)
			{
				e.printStackTrace();
			}
			((TextView)mRootView.findViewById(R.id.txtDeliveryTo)).setText(strEndTime);
		}

		if(mCustomerTypeList.size() > 0)
		{
			//Remove if DescriptionEntryKey = ""
			List<DescTypeInfo> delTypeInfoList = new ArrayList<>();
			for(DescTypeInfo descTypeInfo : mCustomerTypeList)
			{
				if(descTypeInfo.strDescriptionEntryKey.equals(""))
				{
					delTypeInfoList.add(descTypeInfo);
				}
			}
			for(DescTypeInfo descTypeInfo : delTypeInfoList)
			{
				mCustomerTypeList.remove(descTypeInfo);
			}
		}

		if(!mIsEdit)
		{
			DbAdapter dbAdapter = new DbAdapter(mContext);
			dbAdapter.open();
			List<DescTypeInfo> descTypeInfoList = dbAdapter.getDescTypeList("CUSTINFO");
			dbAdapter.close();

			for(DescTypeInfo info : descTypeInfoList)
			{
				if(info.strOwnership.equals("5"))
				{
					ContactInfo custInfo = new ContactInfo();
					custInfo.nContactInfoNumericKey = info.nNumericKey;
					custInfo.strContactInfoAlphaKey = info.strAlphaKey;
					custInfo.strContactInfoEntryKey = info.strDescriptionEntryKey;
					custInfo.strContactInfoDescription = info.strDescription;
					mCustInfoList.add(custInfo);
				}
			}
		}

		bLoadData = true;
		refreshLanguage();
	}

	@Override
	public void refreshPage() {

		boolean bAddBtnEnabled = true;
		String strName = ((EditText) mRootView.findViewById(R.id.editCompanyName)).getText().toString();
		if (strName.length() == 0 && mNameMandantory) {
			bAddBtnEnabled = false;
		}
		String strCompanyTaxID = ((EditText) mRootView.findViewById(R.id.editCompanyTaxID)).getText().toString();
		if (strCompanyTaxID.length() == 0 && mCompanyTaxIdMandantory) {
			bAddBtnEnabled = false;
		}

		String strAddress1 = ((EditText) mRootView.findViewById(R.id.editAddress1)).getText().toString();
		if (strAddress1.length() == 0 && mAddress1Mandantory) {
			bAddBtnEnabled = false;
		}

		String strAddress2 = ((EditText) mRootView.findViewById(R.id.editAddress2)).getText().toString();
		if (strAddress2.length() == 0 && mAddress2Mandantory) {
			bAddBtnEnabled = false;
		}

		String strCity = ((EditText) mRootView.findViewById(R.id.editCity)).getText().toString();
		if (strCity.length() == 0 && mCityMandantory) {
			bAddBtnEnabled = false;
		}

		String strShipToState = ((EditText) mRootView.findViewById(R.id.editState)).getText().toString();
		if (strShipToState.length() == 0 && mShipToStateMandantory) {
			bAddBtnEnabled = false;
		}

		String strShipToZip = ((EditText) mRootView.findViewById(R.id.editPostCode)).getText().toString();
		if (strShipToZip.length() == 0 && mShipToZipMandantory) {
			bAddBtnEnabled = false;
		}

		String strPhone = ((EditText) mRootView.findViewById(R.id.editPhone)).getText().toString();
		if (strPhone.length() == 0 && mPhoneMandantory) {
			bAddBtnEnabled = false;
		}

		if(!isBillingDetailsFilled())
		{
			bAddBtnEnabled = false;
		}

		if(bAddBtnEnabled)
		{
			mRootView.findViewById(R.id.btnAdd).setEnabled(true);
			((Button)mRootView.findViewById(R.id.btnAdd)).setTextColor(getResources().getColor(R.color.white));
			mRootView.findViewById(R.id.btnAdd).setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_orange_large));
		}
		else
		{
			mRootView.findViewById(R.id.btnAdd).setEnabled(false);
			((Button)mRootView.findViewById(R.id.btnAdd)).setTextColor(getResources().getColor(R.color.grey_color));
			mRootView.findViewById(R.id.btnAdd).setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_button_grey_large));
		}
	}

	private boolean isBillingDetailsFilled()
	{
		if(mIsEdit) return true;

		boolean bCompanyTaxMandatory = !TextUtils.isEmpty(mCustomerInfo.strCompanyTaxID);
		boolean bBillingNameMandatory = !TextUtils.isEmpty(mCustomerInfo.strBillName);
		boolean bAddressLine1Mandatory = !TextUtils.isEmpty(mCustomerInfo.strBillAddr1);
		boolean bAddressLine2Mandatory = !TextUtils.isEmpty(mCustomerInfo.strBillAddr2);
		boolean bBillingCityMandatory = !TextUtils.isEmpty(mCustomerInfo.strBillCity);
		boolean bBillingStateMandatory = !TextUtils.isEmpty(mCustomerInfo.strState);
		boolean bBillingPostCodeMandatory = !TextUtils.isEmpty(mCustomerInfo.strZip);
		boolean bLegalNameMandatory = !TextUtils.isEmpty(mCustomerInfo.strLegalName);
		boolean bCreditLimitMandatory = !TextUtils.isEmpty(mCustomerInfo.strCreditLimit);

		if(bCompanyTaxMandatory && (mBillingDetailsInfo == null || TextUtils.isEmpty(mBillingDetailsInfo.strCompanyTaxID))) return false;
		if(bBillingNameMandatory && (mBillingDetailsInfo == null || TextUtils.isEmpty(mBillingDetailsInfo.strBillingName))) return false;
		if(bAddressLine1Mandatory && (mBillingDetailsInfo == null || TextUtils.isEmpty(mBillingDetailsInfo.strAddress1))) return false;
		if(bAddressLine2Mandatory && (mBillingDetailsInfo == null || TextUtils.isEmpty(mBillingDetailsInfo.strAddress2))) return false;
		if(bBillingCityMandatory && (mBillingDetailsInfo == null || TextUtils.isEmpty(mBillingDetailsInfo.strCity))) return false;
		if(bBillingStateMandatory && (mBillingDetailsInfo == null || TextUtils.isEmpty(mBillingDetailsInfo.strState))) return false;
		if(bBillingPostCodeMandatory && (mBillingDetailsInfo == null || TextUtils.isEmpty(mBillingDetailsInfo.strPostCode))) return false;
		if(bLegalNameMandatory && (mBillingDetailsInfo == null || TextUtils.isEmpty(mBillingDetailsInfo.strLegalName))) return false;
		if(bCreditLimitMandatory && (mBillingDetailsInfo == null || TextUtils.isEmpty(mBillingDetailsInfo.strCreditLimit))) return false;

		return true;
	}

	@Override
	public void refreshLanguage() {
		((TextView) mRootView.findViewById(R.id.lblTitle)).setText(mIsEdit == false ? Str.NewCustomer[Global.LANG].toUpperCase() : Str.EditCustomer[Global.LANG].toUpperCase());
		if (mIsEdit == false) {
			((TextView) mRootView.findViewById(R.id.lblDeliveryDetails)).setText(Str.SalesDetails[Global.LANG].toUpperCase());
		}
		if(mNameMandantory)
		{
			((TextView) mRootView.findViewById(R.id.lblCompanyName)).setText(Str.Company_Name[Global.LANG] + "  *");
		}
		else
		{
			((TextView) mRootView.findViewById(R.id.lblCompanyName)).setText(Str.Company_Name[Global.LANG]);
		}

		if(mCompanyTaxIdMandantory)
		{
			((TextView) mRootView.findViewById(R.id.lblCompanyTaxID)).setText(Str.CompanyTaxID[Global.LANG] + "  *");
		}
		else
		{
			((TextView) mRootView.findViewById(R.id.lblCompanyTaxID)).setText(Str.CompanyTaxID[Global.LANG]);
		}

		if(mAddress1Mandantory)
		{
			((TextView) mRootView.findViewById(R.id.lblAddress1)).setText(Str.AddressLine[Global.LANG] + " 1  *");
		}
		else
		{
			((TextView) mRootView.findViewById(R.id.lblAddress1)).setText(Str.AddressLine[Global.LANG] + " 1");
		}

		if(mAddress2Mandantory)
		{
			((TextView) mRootView.findViewById(R.id.lblAddress2)).setText(Str.AddressLine[Global.LANG] + " 2  *");
		}
		else
		{
			((TextView) mRootView.findViewById(R.id.lblAddress2)).setText(Str.AddressLine[Global.LANG] + " 2");
		}

		if(mCityMandantory)
		{
			((TextView) mRootView.findViewById(R.id.lblCity)).setText(Str.City[Global.LANG] + "  *");
		}
		else
		{
			((TextView) mRootView.findViewById(R.id.lblCity)).setText(Str.City[Global.LANG]);
		}

		if(mShipToStateMandantory)
		{
			((TextView) mRootView.findViewById(R.id.lblState)).setText(Str.State[Global.LANG] + "  *");
		}
		else
		{
			((TextView) mRootView.findViewById(R.id.lblState)).setText(Str.State[Global.LANG]);
		}

		if(mShipToZipMandantory)
		{
			((TextView) mRootView.findViewById(R.id.lblPostCode)).setText(Str.Postcode[Global.LANG] + "  *");
		}
		else
		{
			((TextView) mRootView.findViewById(R.id.lblPostCode)).setText(Str.Postcode[Global.LANG]);
		}

		if(mPhoneMandantory)
		{
			((TextView) mRootView.findViewById(R.id.lblPhone)).setText(Str.Phone[Global.LANG] + "  *");
		}
		else
		{
			((TextView) mRootView.findViewById(R.id.lblPhone)).setText(Str.Phone[Global.LANG]);
		}

		((TextView) mRootView.findViewById(R.id.txtTrip)).setText(Str.Trip[Global.LANG] + " # " + Global.gTripNumber);
		((Button) mRootView.findViewById(R.id.btnAdd)).setText(mIsEdit == false ? Str.Add[Global.LANG] : Str.Done[Global.LANG]);

		if(Utils.getStringSetting(mContext, Global.PREF_CUSTTYPE).equals("1") || Utils.getStringSetting(mContext, Global.PREF_CUSTTYPE).equals("2"))
		{
			((TextView) mRootView.findViewById(R.id.lblCustomerType)).setText(Str.CustomerType[Global.LANG] + " *");
		}
		else
		{
			((TextView) mRootView.findViewById(R.id.lblCustomerType)).setText(Str.CustomerType[Global.LANG]);
		}

		if(mCustomerInfo != null)
		{
			String strCustomerType = Str.not_selected[Global.LANG];
			for(DescTypeInfo descTypeInfo : mCustomerTypeList)
			{
				if(Utils.getStringSetting(mContext, Global.PREF_CUSTTYPE).equals("2"))
				{
					if(descTypeInfo.strAlphaKey.trim().equals(mCustomerInfo.strUser1))
					{
						strCustomerType = descTypeInfo.strDescription;
						break;
					}
				}
				else
				{
					if(descTypeInfo.strAlphaKey.trim().equals(mCustomerInfo.strType))
					{
						strCustomerType = descTypeInfo.strDescription;
						break;
					}
				}
			}
			((TextView) mRootView.findViewById(R.id.txtCustomerType)).setText(strCustomerType);
		}
		refreshPage();
	}

	public String getNewCustomerNumber()
	{
		String strNewCustomerNumber = "";
		String strPattern = Utils.getStringSetting(mContext, Global.PREF_TEMP_CUST_MASK);
		if(TextUtils.isEmpty(strPattern))
		{
			strPattern = "PPPYYYJJJSS";
		}

		String strPPP = String.format(Locale.ENGLISH, "%03d", Utils.getIntSetting(mContext, Global.PREF_TEMP_CUST_PREFIX));

		String strKK = Utils.getStringSetting(mContext, Global.PREF_KK_VALUE);

		Utils.getIntSetting(mContext, Global.PREF_CUST_SEQ_NO);
		int nSequenceNo =  Utils.getIntSetting(mContext, Global.PREF_CUST_SEQ_NO) + 1;
		Utils.setIntSetting(mContext, Global.PREF_CUST_SEQ_NO, nSequenceNo);

		String strRRR = Global.gRouteNumber;
		String strY = new SimpleDateFormat("yyyy").format(new Date());
		Calendar cal = Calendar.getInstance();
		int nDayOfYear = cal.get(Calendar.DAY_OF_YEAR);
		String strJJJ = String.format(Locale.ENGLISH, "%d", nDayOfYear);
		String strSSS = String.format(Locale.ENGLISH, "%d", nSequenceNo);

		strNewCustomerNumber = "";

		int nRIdx = 0;
		int nPIdx = 0;
		int nYIdx = 0;
		int nJIdx = 0;
		int nSIdx = 0;
		int nKIdx = 0;

		int nIdx;
		for(int i = strPattern.length() - 1; i >=0; i--)
		{
			char c = strPattern.charAt(i);
			if( c == 'R' )
			{
				nIdx = strRRR.length() - nRIdx - 1;
				if( nIdx >= 0 )
				{
					strNewCustomerNumber = strRRR.substring(nIdx, nIdx + 1) + strNewCustomerNumber;
				}
				else
				{
					strNewCustomerNumber = "0" + strNewCustomerNumber;
				}
				nRIdx++;
			}
			else if( c == 'P')
			{
				nIdx = strPPP.length() - nPIdx - 1;
				if( nIdx >= 0 )
				{
					strNewCustomerNumber = strPPP.substring(nIdx, nIdx + 1) + strNewCustomerNumber;
				}
				else
				{
					strNewCustomerNumber = "0" + strNewCustomerNumber;
				}
				nPIdx++;
			}
			else if( c == 'Y' )
			{
				nIdx = strY.length() - nYIdx - 1;
				if( nIdx >= 0 )
				{
					strNewCustomerNumber = strY.substring(nIdx, nIdx + 1) + strNewCustomerNumber ;
				}
				else
				{
					strNewCustomerNumber = "0" + strNewCustomerNumber;
				}
				nYIdx++;
			}
			else if( c == 'J' )
			{
				nIdx = strJJJ.length() - nJIdx - 1;
				if( nIdx >= 0 )
				{
					strNewCustomerNumber = strJJJ.substring(nIdx, nIdx + 1) + strNewCustomerNumber;
				}
				else
				{
					strNewCustomerNumber = "0" + strNewCustomerNumber;
				}
				nJIdx++;
			}
			else if( c == 'S' )
			{
				nIdx = strSSS.length() - nSIdx - 1;
				if( nIdx >= 0 )
				{
					strNewCustomerNumber = strSSS.substring(nIdx, nIdx + 1) + strNewCustomerNumber;
				}
				else
				{
					strNewCustomerNumber = "0" + strNewCustomerNumber;
				}
				nSIdx++;
			}
			else if( c == 'K')
			{
				nIdx = strKK.length() - nKIdx - 1;
				if( nIdx >= 0 )
				{
					strNewCustomerNumber = strKK.substring(nIdx, nIdx + 1) + strNewCustomerNumber;
				}
				else
				{
					strNewCustomerNumber = "0" + strNewCustomerNumber;
				}
				nKIdx++;
			}
		}

		return strNewCustomerNumber;
	}

	@Override
	public void receiveBroadcast(String strAction, Intent intent) {
	}

	private void doDone() {
		mCustomerInfo.strName = ((EditText) mRootView.findViewById(R.id.editCompanyName)).getText().toString();
		if (mCustomerInfo.strName.length() == 0 && mNameMandantory) {
			Utils.printDebug(mContext, Str.PleaseInputAllMandatoryFields[Global.LANG]);
			showKeyboard((EditText) mRootView.findViewById(R.id.editCompanyName), true);
			return;
		}
		mCustomerInfo.strCompanyTaxID = ((EditText) mRootView.findViewById(R.id.editCompanyTaxID)).getText().toString();
		if (mCustomerInfo.strCompanyTaxID.length() == 0 && mCompanyTaxIdMandantory) {
			Utils.printDebug(mContext, Str.PleaseInputAllMandatoryFields[Global.LANG]);
			showKeyboard((EditText) mRootView.findViewById(R.id.editCompanyTaxID), true);
			return;
		}

		mCustomerInfo.strAddress1 = ((EditText) mRootView.findViewById(R.id.editAddress1)).getText().toString();
		if (mCustomerInfo.strAddress1.length() == 0 && mAddress1Mandantory) {
			Utils.printDebug(mContext, Str.PleaseInputAllMandatoryFields[Global.LANG]);
			showKeyboard((EditText) mRootView.findViewById(R.id.editAddress1), true);
			return;
		}
		mCustomerInfo.strAddress2 = ((EditText) mRootView.findViewById(R.id.editAddress2)).getText().toString();
		if (mCustomerInfo.strAddress2.length() == 0 && mAddress2Mandantory) {
			Utils.printDebug(mContext, Str.PleaseInputAllMandatoryFields[Global.LANG]);
			showKeyboard((EditText) mRootView.findViewById(R.id.editAddress2), true);
			return;
		}

		mCustomerInfo.strCity = ((EditText) mRootView.findViewById(R.id.editCity)).getText().toString();
		if (mCustomerInfo.strCity.length() == 0 && mCityMandantory) {
			Utils.printDebug(mContext, Str.PleaseInputAllMandatoryFields[Global.LANG]);
			showKeyboard((EditText) mRootView.findViewById(R.id.editCity), true);
			return;
		}
		
		mCustomerInfo.strShipToState = ((EditText) mRootView.findViewById(R.id.editState)).getText().toString();
		if (mCustomerInfo.strShipToState.length() == 0 && mShipToStateMandantory) {
			Utils.printDebug(mContext, Str.PleaseInputAllMandatoryFields[Global.LANG]);
			showKeyboard((EditText) mRootView.findViewById(R.id.editState), true);
			return;
		}

		mCustomerInfo.strShipToZip = ((EditText) mRootView.findViewById(R.id.editPostCode)).getText().toString();
		if (mCustomerInfo.strShipToZip.length() == 0 && mShipToZipMandantory) {
			Utils.printDebug(mContext, Str.PleaseInputAllMandatoryFields[Global.LANG]);
			showKeyboard((EditText) mRootView.findViewById(R.id.editPostCode), true);
			return;
		}

		mCustomerInfo.strPhone = ((EditText) mRootView.findViewById(R.id.editPhone)).getText().toString();
		if (mCustomerInfo.strPhone.length() == 0 && mPhoneMandantory) {
			Utils.printDebug(mContext, Str.PleaseInputAllMandatoryFields[Global.LANG]);
			showKeyboard((EditText) mRootView.findViewById(R.id.editPhone), true);
			return;
		}
		initMakeZipInfo();


		if(Utils.getStringSetting(mContext, Global.PREF_CUSTTYPE).equals("1") || Utils.getStringSetting(mContext, Global.PREF_CUSTTYPE).equals("2"))
		{
			if(Utils.getStringSetting(mContext, Global.PREF_CUSTTYPE).equals("2"))
			{
				if(TextUtils.isEmpty(mCustomerInfo.strUser1))
				{
					Utils.printDebug(mContext, Str.PleaseSelectCustomerType[Global.LANG]);
					return;
				}
			}
			else
			{
				if(TextUtils.isEmpty(mCustomerInfo.strType))
				{
					Utils.printDebug(mContext, Str.PleaseSelectCustomerType[Global.LANG]);
					return;
				}
			}
		}

		showKeyboard((EditText) mRootView.findViewById(R.id.editCompanyName), false);

		UCustomerFMInfo uCustomerFMInfo = Global.gDefaultUCustomerFMInfo.duplicate();
		uCustomerFMInfo.nChainNo = mCustomerInfo.nChainNo;
		uCustomerFMInfo.nCustNo = mCustomerInfo.nCustNo;

		if(mTemplateCustomerInfo != null)
		{
			uCustomerFMInfo.nTemplateChainNo = nTemplateChainNo;
			uCustomerFMInfo.nTemplateCustNo = nTemplateCustNo;
		}
		else {
			uCustomerFMInfo.nTemplateChainNo = Utils.getIntFromString(Utils.getStringSetting(mContext, Global.PREF_TEMPLATE_CHAIN));
			uCustomerFMInfo.nTemplateCustNo = Utils.getIntFromString(Utils.getStringSetting(mContext, Global.PREF_TEMPLATE_CUST));
		}

		uCustomerFMInfo.strEditType = mIsEdit == false ? "ADD" : "EDT";
		uCustomerFMInfo.strName = mCustomerInfo.strName;
		uCustomerFMInfo.strCompanyTaxID = mCustomerInfo.strCompanyTaxID;
		uCustomerFMInfo.strAddress1 = mCustomerInfo.strAddress1;
		uCustomerFMInfo.strAddress2 = mCustomerInfo.strAddress2;
		uCustomerFMInfo.strCity = mCustomerInfo.strCity;
		uCustomerFMInfo.strShipToState = mCustomerInfo.strShipToState;
		uCustomerFMInfo.strShipToZip = mCustomerInfo.strShipToZip;
		uCustomerFMInfo.strPhone = mCustomerInfo.strPhone;

		String strTypeDescEntryKey = "";
		for(DescTypeInfo descTypeInfo : mCustomerTypeList)
		{
			if(Utils.getStringSetting(mContext, Global.PREF_CUSTTYPE).equals("2"))
			{
				if(descTypeInfo.strAlphaKey.trim().equals(mCustomerInfo.strUser1))
				{
					strTypeDescEntryKey = descTypeInfo.strDescriptionEntryKey;
					break;
				}
			}
			else
			{
				if(descTypeInfo.strAlphaKey.trim().equals(mCustomerInfo.strType))
				{
					strTypeDescEntryKey = descTypeInfo.strDescriptionEntryKey;
					break;
				}
			}
		}

		if(Utils.getStringSetting(mContext, Global.PREF_CUSTTYPE).equals("2"))
		{
			uCustomerFMInfo.strUser1 = strTypeDescEntryKey;
		}
		else
		{
			uCustomerFMInfo.strType = strTypeDescEntryKey;
		}

		long nTrxnNo = setGeneralUploadData("CSFM", mCustomerInfo.nChainNo, mCustomerInfo.nCustNo, -1);

		TransactionInfo transactionInfo = new TransactionInfo();
		transactionInfo.strType = "CSFM";
		transactionInfo.strTime = new SimpleDateFormat("kk:mm").format(new Date(nTrxnNo));
		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		dbAdapter.insertTransactionHistoryInfo(transactionInfo);
		dbAdapter.close();

		uCustomerFMInfo.nTrxnNo = m_lTrxnNo;
		
		uCustomerFMInfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(mCurDate);
		uCustomerFMInfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(mCurDate);
		uCustomerFMInfo.strStartDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(mCurDate);
		uCustomerFMInfo.strStartTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(mCurDate);
		uCustomerFMInfo.strEndDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(mCurDate);
		uCustomerFMInfo.strEndTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(mCurDate);

		SimpleDateFormat sdf = new SimpleDateFormat("h:mm a");
		SimpleDateFormat sdf_target = new SimpleDateFormat("kkmm");
		String strWindowFrom = ((TextView)mRootView.findViewById(R.id.txtDeliveryFrom)).getText().toString();
		String strWindowTo = ((TextView)mRootView.findViewById(R.id.txtDeliveryTo)).getText().toString();
		uCustomerFMInfo.strDeliveryWindowFrom = "0000";
		uCustomerFMInfo.strDeliveryWindowTo = "0000";

		try
		{
			Date date = sdf.parse(strWindowFrom);
			uCustomerFMInfo.strDeliveryWindowFrom = sdf_target.format(date);
		}catch (Exception e)
		{
			e.printStackTrace();
		}

		try
		{
			Date date = sdf.parse(strWindowTo);
			uCustomerFMInfo.strDeliveryWindowTo = sdf_target.format(date);
		}catch (Exception e)
		{
			e.printStackTrace();
		}
		mCustomerInfo.strStartTime1 = uCustomerFMInfo.strDeliveryWindowFrom;
		mCustomerInfo.strEndTime1 = uCustomerFMInfo.strDeliveryWindowTo;

		dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();

		if(mIsEdit == false)
		{
			mCustomerInfo.nDayNo = Utils.getIntSetting(mContext, Global.DAY_NO);

			//Do Process customer show in Select Customer Screen.
			mCustomerInfo.bIsRouteSch = true;
			mCustomerInfo.nSeqNo = 0;
			mCustomerInfo.bFromAddCustomer = true;

			if(Global.gCustomerList != null )
			{
				mCustomerInfo.nOrderNo = 0;
				for(CustomerInfo otherInfo: Global.gCustomerList)
				{
					otherInfo.nOrderNo++;
				}
			}
			else
			{
				mCustomerInfo.nOrderNo = 0;
			}
			Global.gCustomerList.add(0, mCustomerInfo);

			int nCustomerCount = dbAdapter.getCustomerListCount(true);
			mCustomerInfo.nOrgSeqNo = nCustomerCount;
		}

		if (mIsEdit == false) {
			dbAdapter.insertCustomerInfo(mCustomerInfo);
			if(mBillingDetailsInfo != null)
			{
				dbAdapter.insertUBillingDetailsInfo(mBillingDetailsInfo);
			}
			for(ContactDetail contactDetail : mContactDetailList)
			{
				contactDetail.nChainNo = mCustomerInfo.nChainNo;
				contactDetail.nCustNo = mCustomerInfo.nCustNo;
				dbAdapter.insertContactDetails(contactDetail);
			}
			dbAdapter.insertCustAdditionalInfo(mCustInfoList, mCustomerInfo.nCustNo, mCustomerInfo.nChainNo);

		} else {
			dbAdapter.updateCustomerInfo(mCustomerInfo);
			if(mBillingDetailsInfo != null)
			{
				dbAdapter.updateUBillingDetailsInfo(mBillingDetailsInfo);
			}
			dbAdapter.deleteContactDetails(mCustomerInfo.nCustNo, mCustomerInfo.nChainNo);
			for(ContactDetail contactDetail : mContactDetailList)
			{
				dbAdapter.insertContactDetails(contactDetail);
			}
			dbAdapter.updateCustAdditionalInfo(mCustInfoList, mCustomerInfo.nCustNo, mCustomerInfo.nChainNo);
		}

		dbAdapter.insertUCustomerFMInfo(uCustomerFMInfo);
		dbAdapter.close();

		//Global.gUploadCustNoList.add(makeZipFileInfo);
		Utils.registerUploadFile(mContext, makeZipFileInfo);
		mContext.onBackPressed();
		if (mIsEdit == false) {
			mContext.sendBroadcast(new Intent(Global.ACTION_REFRESH_CUSTOMER_LIST));
		} else {
			try
			{
				Intent intent = new Intent(Global.ACTION_REFRESH_CUSTOMER_DETAIL);
				intent.putExtra(Global.EXTRA_CUSTOMER_INFO, mCustomerInfo);
				mContext.sendBroadcast(intent);

			}catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		if( mIsEdit == false )
		{
			int nNewCustomers = Utils.getIntSetting(mContext, Global.NEW_CUSTOMERS_TODAY);
			Utils.setIntSetting(mContext, Global.NEW_CUSTOMERS_TODAY, nNewCustomers + 1);

			//Arrange CustomerList
			dbAdapter = new DbAdapter(mContext);
			dbAdapter.open();
			//Save Current Order to DB.
			for(CustomerInfo info : Global.gCustomerList )
			{
				info.nOrderNo++;
				dbAdapter.updateCustomerInfo(info);
			}
			dbAdapter.close();
		}
	}

	private void gotoBillingDetails(UBillingDetailsInfo info)
	{
		Global.gUBillingDetailsInfo = info;
		Global.gCustomerTemplate = mCustomerInfo;
		AddBillingDetailsFragment fragment = new AddBillingDetailsFragment();
		Bundle args = new Bundle();
		args.putBoolean(Global.EXTRA_IS_EDIT_CUSTOMER, mIsEdit);
		fragment.setArguments(args);
		mContext.replaceFragment(fragment);
	}

	private void doBillingDetails()
	{
		if(mBillingDetailsInfo == null)
		{
			UBillingDetailsInfo info = new UBillingDetailsInfo();
			info.nCustNo = mCustomerInfo.nCustNo;
			info.nChainNo = mCustomerInfo.nChainNo;

			try {
				final MessageDialog msgDialog = new MessageDialog(mContext);
				msgDialog.setCancelable(false);
				msgDialog.mStrMessage = Str.CopyDataFromSalesInfo[Global.LANG];
				msgDialog.mStrLeft = Str.Yes[Global.LANG];
				msgDialog.mStrRight = Str.No[Global.LANG];
				msgDialog.setOnDismissListener(dialog -> {
					if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
						info.strBillingName = ((EditText) mRootView.findViewById(R.id.editCompanyName)).getText().toString();
						info.strCompanyTaxID = ((EditText) mRootView.findViewById(R.id.editCompanyTaxID)).getText().toString();
						info.strAddress1 = ((EditText) mRootView.findViewById(R.id.editAddress1)).getText().toString();
						info.strAddress2 = ((EditText) mRootView.findViewById(R.id.editAddress2)).getText().toString();
						info.strCity = ((EditText) mRootView.findViewById(R.id.editCity)).getText().toString();
						info.strState = ((EditText) mRootView.findViewById(R.id.editState)).getText().toString();
						info.strPostCode = ((EditText) mRootView.findViewById(R.id.editPostCode)).getText().toString();
						gotoBillingDetails(info);
					}
					else {
						gotoBillingDetails(info);
					}
				});
				msgDialog.show();
			}catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			gotoBillingDetails(mBillingDetailsInfo.duplicate());
		}
	}

	private void doCustomerInfo()
	{
		if(mCustInfoList.size() > 0)
		{
			Global.gCustAdditionalInfoList = mCustInfoList;
			AddContactInformationFragment fragment = new AddContactInformationFragment();
			Bundle args = new Bundle();
			args.putBoolean(Global.EXTRA_IS_EDIT_CUSTOMER, mIsEdit);
			args.putBoolean(Global.EXTRA_IS_EDIT_CUST_INFO, true);

			fragment.setArguments(args);
			mContext.replaceFragment(fragment);
		}
	}

	private void doContacts()
	{
		Global.gContactDetailList = mContactDetailList;
		ContactsDetailListFragment fragment = new ContactsDetailListFragment();
		Bundle args = new Bundle();
		args.putBoolean(Global.EXTRA_IS_EDIT_CUSTOMER, mIsEdit);
		fragment.setArguments(args);
		mContext.replaceFragment(fragment);
	}

	public boolean m_bClicked = false;
	@Override
	public void onClick(View v) {
		if( m_bClicked ) return;
		m_bClicked = true;
		switch (v.getId()) {
			case R.id.btnBack:
				mContext.onBackPressed();
				break;
			case R.id.btnAdd:
				doDone();
				break;
			case R.id.btnBillingDetails:
				doBillingDetails();
				break;
			case R.id.btnCustomerInfo:
				doCustomerInfo();
				break;
			case R.id.btnContacts:
				doContacts();
				break;
		}
		m_bClicked = false;
	}
}