package dsd.dsdvansales.dialogs;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import dsd.dsdvansales.R;
import dsd.dsdvansales.activities.MainActivity;
import dsd.dsdvansales.db.DbAdapter;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.model.CustomerInfo;
import dsd.dsdvansales.model.CustomerNoteInfo;
import dsd.dsdvansales.model.DescTypeInfo;
import dsd.dsdvansales.model.MakeZipFileInfo;
import dsd.dsdvansales.model.UCustomerNoteInfo;
import dsd.dsdvansales.model.UTransactionsInfo;
import dsd.dsdvansales.model.UVisitInfo;

import static java.util.Calendar.DATE;

public class PostVisitDetailsDialog extends Dialog implements View.OnClickListener {

	public interface Callback{
		void onDone(long nTrxnNo);
		void onCancel();
	}

	EditText mEditNextVisitDate, mEditVisitFrequency, mEditNotes;
	MainActivity mContext;
	Callback callback;
	public PostVisitDetailsDialog(MainActivity context, Callback callback) {
		super(context, android.R.style.Theme_Translucent_NoTitleBar);
		this.mContext = context;
		this.callback = callback;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
		lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
		lpWindow.dimAmount = 0.9f;
		getWindow().setAttributes(lpWindow);
		setCancelable(false);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.dialog_post_visit_details);
		findViewById(R.id.btnDone).setOnClickListener(this);
		findViewById(R.id.btnReturn).setOnClickListener(this);
		findViewById(R.id.btnPickDate).setOnClickListener(this);

		if(Utils.getIntSetting(mContext, Global.PREF_POST_VISIT) == 1)
		{
			findViewById(R.id.btnReturn).setVisibility(View.GONE);
		}

		mEditNextVisitDate = (EditText)findViewById(R.id.editNextVisitDate);
		mEditVisitFrequency = (EditText)findViewById(R.id.editVisitFrequency);
		mEditNotes = (EditText)findViewById(R.id.editNotes);

		loadData();
		refreshPage();
	}

	private void refreshPage()
	{
		((TextView)findViewById(R.id.txtTitle)).setText(Str.PostVisitDetails[Global.LANG]);
		((TextView)findViewById(R.id.lblNextVisitDate)).setText(Str.NextVisitDate[Global.LANG]);
		((TextView)findViewById(R.id.lblVisitFrequency)).setText(Str.VisitFrequency[Global.LANG]);
		((TextView)findViewById(R.id.lblWeeks)).setText(Str.Weeks[Global.LANG]);
		((Button)findViewById(R.id.btnDone)).setText(Str.Done[Global.LANG]);
		if(Utils.getIntSetting(mContext, Global.PREF_POST_VISIT) == 2)
		{
			findViewById(R.id.txtNextVisitDateMandantory).setVisibility(View.VISIBLE);
			findViewById(R.id.txtVisitFrequencyMandantory).setVisibility(View.VISIBLE);
			((EditText)findViewById(R.id.editNotes)).setHint(Html.fromHtml(Str.VisitNotes[Global.LANG] + " <font color='red'>*</font>"));
			findViewById(R.id.btnReturn).setVisibility(View.GONE);
		}
		else
		{
			findViewById(R.id.txtNextVisitDateMandantory).setVisibility(View.GONE);
			findViewById(R.id.txtVisitFrequencyMandantory).setVisibility(View.GONE);
			((EditText)findViewById(R.id.editNotes)).setHint(Html.fromHtml(Str.VisitNotes[Global.LANG]));
			findViewById(R.id.btnReturn).setVisibility(View.VISIBLE);
		}
	}

	private void loadData()
	{
		mEditVisitFrequency.setText(String.valueOf(Global.gCurCustomerInfo.nDeliveryFreq));
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(DATE, 7 * Global.gCurCustomerInfo.nDeliveryFreq);
		SimpleDateFormat sdf = new SimpleDateFormat(Utils.getDateFormat(mContext, Global.DATE_DD_MM_YYYY_FORMAT));
		mEditNextVisitDate.setText(sdf.format(calendar.getTime()));
		mEditVisitFrequency.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

			}

			@Override
			public void afterTextChanged(Editable editable) {
				int nDeliveryFreq = Utils.parseIntFromString(editable.toString());
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(new Date());
				calendar.add(DATE, 7 * nDeliveryFreq);
				SimpleDateFormat sdf = new SimpleDateFormat(Utils.getDateFormat(mContext, Global.DATE_DD_MM_YYYY_FORMAT));
				mEditNextVisitDate.setText(sdf.format(calendar.getTime()));
			}
		});
	}

	@Override
	public void onClick(View view) {
		switch (view.getId())
		{
			case R.id.btnDone:
				doDone();
				break;
			case R.id.btnReturn:
				doReturn();
				break;
			case R.id.btnPickDate:
				doPickDate();
				break;
		}
	}

	private void doPickDate()
	{
		String strDate = mEditNextVisitDate.getText().toString();

		Date startDate = new Date();
		try
		{
			startDate = new SimpleDateFormat(Utils.getDateFormat(mContext, Global.DATE_DD_MM_YYYY_FORMAT)).parse(strDate);
		}catch (Exception e)
		{
			e.printStackTrace();
		}

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(startDate);
		com.wdullaer.materialdatetimepicker.date.DatePickerDialog dpd = com.wdullaer.materialdatetimepicker.date.DatePickerDialog.newInstance(
				new com.wdullaer.materialdatetimepicker.date.DatePickerDialog.OnDateSetListener() {
					@Override
					public void onDateSet(com.wdullaer.materialdatetimepicker.date.DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
						Date myDate = new Date(year-1900, monthOfYear, dayOfMonth, 0, 0, 0);
						String strDate = new SimpleDateFormat(Utils.getDateFormat(mContext, Global.DATE_DD_MM_YYYY_FORMAT)).format(myDate);
						mEditNextVisitDate.setText(strDate);
					}
				},
				calendar.get(Calendar.YEAR), // Initial year selection
				calendar.get(Calendar.MONTH), // Initial month selection
				calendar.get(Calendar.DAY_OF_MONTH) // Inital day selection
		);
		dpd.setAccentColor(mContext.getResources().getColor(R.color.top_banner_color));
		dpd.show(mContext.getFragmentManager(), "Datepickerdialog");
	}

	private void doDone()
	{
		String strVisitDate = mEditNextVisitDate.getText().toString();
		String strVisitFrequency = mEditVisitFrequency.getText().toString();
		String strNotes = mEditNotes.getText().toString();
		if(Utils.getIntSetting(mContext, Global.PREF_POST_VISIT) == 2)
		{
			//Check Validation every field has been inputted or not.
			try
			{
				if(TextUtils.isEmpty(strVisitFrequency))
				{
					Toast.makeText(mContext, Str.PleaseInputVisitFrequency[Global.LANG], Toast.LENGTH_LONG).show();
					return;
				}
				if(TextUtils.isEmpty(strNotes))
				{
					Toast.makeText(mContext, Str.Please_input_the_note[Global.LANG], Toast.LENGTH_LONG).show();
					return;
				}
			}catch (Exception e)
			{
				e.printStackTrace();
			}

		}

		SimpleDateFormat sdf = new SimpleDateFormat(Utils.getDateFormat(mContext, Global.DATE_DD_MM_YYYY_FORMAT));

		//Check Next Visit Date Format
		Date nextVisitDate = null;
		try
		{
			nextVisitDate = sdf.parse(strVisitDate);
		}catch (Exception e)
		{
			e.printStackTrace();
			try
			{
				Toast.makeText(mContext, Str.Please_input_correct_date_format[Global.LANG], Toast.LENGTH_LONG).show();
			}catch (Exception e1)
			{
				e1.printStackTrace();
			}
			return;
		}


		String strNoteDate = "";
		String strNextVisitDate = "";
		try {
			Date date = new Date();
			strNoteDate = new SimpleDateFormat("yyyyMMdd").format(date);
			strNextVisitDate =  new SimpleDateFormat("yyyyMMdd").format(sdf.parse(strVisitDate));
		} catch (Exception e) {
			e.printStackTrace();
		}

		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();

		long m_lTrxnNo = System.currentTimeMillis();
        Date mCurDate = new Date(m_lTrxnNo);

		//Make the Customer Note Info Record.
        if(!TextUtils.isEmpty(strNotes))
        {
            CustomerNoteInfo customerNoteInfo = new CustomerNoteInfo();
            customerNoteInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
            customerNoteInfo.nChainNo = Global.gCurCustomerInfo.nChainNo;
            customerNoteInfo.strCreatedBy = Utils.getStringSetting(mContext, Global.PREF_USER_DESCRIPTION);
            customerNoteInfo.strNote = strNotes;
            customerNoteInfo.strNoteId = String.valueOf(m_lTrxnNo);
            customerNoteInfo.strNoteType = "99";
            customerNoteInfo.strNoteDate = strNoteDate;
            customerNoteInfo.strNoteTime = new SimpleDateFormat("kkmm").format(new Date());
            dbAdapter.insertCustomerNoteInfo(customerNoteInfo);

            UCustomerNoteInfo uInfo = new UCustomerNoteInfo();
            uInfo.nTrxnNo = m_lTrxnNo;
            uInfo.strDocNo = "0";
            uInfo.strDocType = "NOTE";
            uInfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(mContext, mCurDate));
            uInfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(mCurDate);;
            uInfo.nChainNo = Global.gCurCustomerInfo.nChainNo;
            uInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
            uInfo.strSalesDate = uInfo.strTrxnDate;
            uInfo.strTransactionType = "A";
            //uInfo.strUser = customerNoteInfo.strCreatedBy;
			uInfo.strUser = Utils.getStringSetting(mContext, Global.PREF_USER_NAME);
            uInfo.strNoteType = "99";
            uInfo.strMessage = strNotes;
            uInfo.nAttachment = 0;
            dbAdapter.insertUCustomerNoteInfo(uInfo);
        }

		List<DescTypeInfo> visitTypeList = dbAdapter.getDescTypeList("VisitType");

        UVisitInfo visitInfo = new UVisitInfo();
        visitInfo.nTrxnNo = m_lTrxnNo;
        visitInfo.strDocType = "VIS";
        visitInfo.nChainNo = Global.gCurCustomerInfo.nChainNo;
        visitInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
        visitInfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(mContext, mCurDate));
        visitInfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(mCurDate);
        if(Global.gUOrderHInfo != null)
        {
            visitInfo.nPrintedFlag = Global.gUOrderHInfo.nPrintedFlag;
        }
        visitInfo.strSaleDate = visitInfo.strTrxnDate;
        visitInfo.strVisitStart = Global.strVisitStart;
        visitInfo.strVisitEnd = new SimpleDateFormat("kkmmss").format(new Date());
        visitInfo.strNextVisit = strNextVisitDate;
        visitInfo.strVisitMessage = strNotes;
        visitInfo.nOrderCreated = Global.bOrderCreated ? 1 : 0;
        visitInfo.nSurveyCompleted = Global.nSurveyCompleted;
        visitInfo.nAssetChecked = Global.nAssetChecked;
        visitInfo.nAssetRequested = Global.nAssetRequested;
        visitInfo.nVisitFrequency = Utils.parseIntFromString(strVisitFrequency);
//		visitInfo.strVisitTypeId = (visitTypeList != null && visitTypeList.size() > 0 ? visitTypeList.get(0).strDescriptionEntryKey : "0");

        dbAdapter.insertUVisitInfo(visitInfo);

		//Make the Upload Data and start uploading.
		UTransactionsInfo uTransactionInfo = new UTransactionsInfo();
		uTransactionInfo.nTrxnNo = m_lTrxnNo;
		uTransactionInfo.strDocType = "VIS";
		uTransactionInfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(mContext, mCurDate));
		uTransactionInfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(mCurDate);
		uTransactionInfo.nChainNo = Global.gCurCustomerInfo.nChainNo;
		uTransactionInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;

		uTransactionInfo.strTripId = Utils.getStringSetting(mContext, Global.PREF_TRIP_ID);
		uTransactionInfo.strDriverId = "";
		uTransactionInfo.strDriverName = Utils.getStringSetting(mContext, Global.PREF_USER_DESCRIPTION);
		uTransactionInfo.strDriverNumber = Utils.getStringSetting(mContext, Global.PREF_DRIVER_NUMBER);

		dbAdapter.insertUTransactionsInfo(uTransactionInfo);
		dbAdapter.close();

		dismiss();
		if(callback != null)
		{
			callback.onDone(m_lTrxnNo);
		}
	}

	private void doReturn()
	{
		dismiss();
		if(callback != null)
		{
			callback.onCancel();
		}
	}

	@Override
	public void onBackPressed() {
		if(Utils.getIntSetting(mContext, Global.PREF_POST_VISIT) == 1)
		{
			doReturn();
		}
	}
}

