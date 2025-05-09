package dsd.dsdvansales.fragment;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import dsd.dsdvansales.R;
import dsd.dsdvansales.activities.BarcodeScannerActivity;
import dsd.dsdvansales.activities.SignatureActivity;
import dsd.dsdvansales.db.DbAdapter;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.PrintEngine;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.model.SurvQoptInfo;
import dsd.dsdvansales.model.SurvQuesInfo;
import dsd.dsdvansales.model.SurveyInfo;
import dsd.dsdvansales.model.TransactionInfo;
import dsd.dsdvansales.model.UFileTransactionInfo;
import dsd.dsdvansales.model.USurveyDetailInfo;
import dsd.dsdvansales.model.USurveysInfo;
import dsd.dsdvansales.views.AlertSingleChoiceAdapter;

public class PreTripCheckFragment extends BaseFragment implements View.OnClickListener {

	private TextView mTxtNoData = null;
	private ListView mListView = null;
	private ListAdapter mListAdapter = null;

	private SurveyInfo mSurveyInfo = new SurveyInfo();
	private List<SurvQuesInfo> mQuestionList = new ArrayList<>();
	private List<String> mAnswerList = new ArrayList<>();
	private boolean mCompleted = false;
	private String mStrPhotoPath = "";
	private boolean mIsFromEndTrip = false;

	private Map<Integer, Boolean> mapInvisibleQues = new HashMap<>();

	private Map<Integer, String> mapPhotoMap = new HashMap<>();

	private int nFocusNo = -1;

	private long pdfTrxnNo = -1;
	private String mStrPdfFileName = "";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootView = inflater.inflate(R.layout.fragment_pre_trip_check, container, false);
		commonInit();
		return mRootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		Global.gFragIdx = Global.FRAG_PRE_TRIP_CHECK;
	}

	@Override
	public void initView() {
		mRootView.findViewById(R.id.btnBack).setOnClickListener(this);
//		mRootView.findViewById(R.id.btnNote).setOnClickListener(this);
		mRootView.findViewById(R.id.btnPhoto).setOnClickListener(this);
		mRootView.findViewById(R.id.btnCompleted).setOnClickListener(this);

		mTxtNoData = (TextView) mRootView.findViewById(R.id.txtNoData);
		mListView = (ListView) mRootView.findViewById(R.id.listView);
		mListAdapter = new ListAdapter();
		mListView.setAdapter(mListAdapter);

		mIsFromEndTrip = getArguments().getBoolean(Global.EXTRA_IS_FROM_END_TRIP);
		loadData();
		refreshLanguage();
	}

	@Override
	public void loadData() {
		mQuestionList.clear();
		mAnswerList.clear();
		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		if( !mIsFromEndTrip )
		{
			mSurveyInfo = dbAdapter.getSurveyInfo(99);
		}
		else
		{
			mSurveyInfo = dbAdapter.getSurveyInfo(98, true);
		}
		List<SurvQuesInfo> list = dbAdapter.getSurvQuesList(mSurveyInfo.strSurveyID);
		Collections.sort(list, (t1, t2) -> t1.nLookupNo - t2.nLookupNo);
		for (SurvQuesInfo info : list) {
			info.responseList = dbAdapter.getSurvQoptListWithLookupId(info.strQuestionID);
			Collections.sort(info.responseList, (t1, t2) -> t1.nLookupNo - t2.nLookupNo);
			mQuestionList.add(info);
			if (info.strQuestionType.equals(Global.QUESTION_TYPE_CHOICE) == true || info.strQuestionType.equals(Global.QUESTION_TYPE_MULTI_CHOICE) == true) {
				mAnswerList.add(Str.Please_select_the_response[Global.LANG]);
			} else {
				mAnswerList.add("");
			}
		}
		dbAdapter.close();
		refreshPage();
	}

	@Override
	public void refreshPage() {
		if (mQuestionList.size() > 0) {
			mListView.setVisibility(View.VISIBLE);
			mTxtNoData.setVisibility(View.GONE);
			mListAdapter.notifyDataSetChanged();
		} else {
			mListView.setVisibility(View.GONE);
			mTxtNoData.setVisibility(View.VISIBLE);
		}
		refreshCompleted();
	}

	public void refreshCompleted()
	{
		mRootView.findViewById(R.id.btnCompleted).setEnabled(isAvailableCompleted());
	}

	private boolean isAvailableCompleted()
	{
		boolean bAvailable = true;
		for(int i = 0; i < mQuestionList.size(); i++)
		{
			if( mQuestionList.get(i).strMandatory.trim().equals("Y") )
			{
				if(mapInvisibleQues.get(mQuestionList.get(i).nLookupNo) != null && mapInvisibleQues.get(mQuestionList.get(i).nLookupNo))
				{
					continue;
				}

				if(mQuestionList.get(i).strQuestionType.equals(Global.QUESTION_TYPE_SIGNATURE))
				{
					if(mapPhotoMap.get(mQuestionList.get(i).nLookupNo) == null)
					{
						bAvailable = false;
						break;
					}
				}
				else if(mQuestionList.get(i).strQuestionType.equals(Global.QUESTION_TYPE_PHOTOGRAPH))
				{
					if(mapPhotoMap.get(mQuestionList.get(i).nLookupNo) == null)
					{
						bAvailable = false;
						break;
					}
				}
				else if(TextUtils.isEmpty(mAnswerList.get(i)) || mAnswerList.get(i).equals(Str.Please_select_the_response[Global.LANG]))
				{
					bAvailable = false;
					break;
				}
			}
		}
		return bAvailable;
	}

	@Override
	public void refreshLanguage() {
		((TextView) mRootView.findViewById(R.id.lblTitle)).setText(mIsFromEndTrip == false ? Str.PreTripCheck[Global.LANG].toUpperCase() : Str.PostTripCheck[Global.LANG].toUpperCase());
		((TextView) mRootView.findViewById(R.id.txtTrip)).setText(Str.Trip[Global.LANG] + " #" + Global.gTripNumber);
		((TextView) mRootView.findViewById(R.id.txtVisitStarted)).setText(Str.Visit_Started_at[Global.LANG] + new SimpleDateFormat("HH:mm:ss").format(new Date()));
		((Button) mRootView.findViewById(R.id.btnCompleted)).setText(Str.Completed[Global.LANG]);
		mTxtNoData.setText(Str.No_data_has_been_found[Global.LANG]);
		refreshPage();
	}

	@Override
	public void receiveBroadcast(String strAction, Intent intent) {
//		if (strAction.equals(Global.ACTION_TAKE_PHOTO) == true) {
//			mStrPhotoPath = intent.getStringExtra(Global.EXTRA_PHOTO_PATH);
//		}

		if (strAction.equals(Global.ACTION_TAKE_PHOTO) == true) {
			String strPhotoPath = intent.getStringExtra(Global.EXTRA_PHOTO_PATH);
			if(!TextUtils.isEmpty(strPhotoPath))
			{
				String strNewPhotoPath = Global.UPLOAD_DIR + System.currentTimeMillis() + ".jpg";
				Utils.fileCopy(strPhotoPath, strNewPhotoPath);
				Utils.fileDelete(strPhotoPath);

				if(nFocusNo == -1)
				{
					mStrPhotoPath = strNewPhotoPath;
				}
				else {
					//Register it to the map
					mapPhotoMap.put(nFocusNo,strNewPhotoPath);
					refreshPage();
				}
			}
		} else if (strAction.equals(Global.ACTION_CAPTURE_SIGNATURE) == true) {
			String strSignaturePath = intent.getStringExtra(Global.EXTRA_SIGNATURE_PATH);
			if(!TextUtils.isEmpty(strSignaturePath))
			{
				String strNewPhotoPath = Global.UPLOAD_DIR + System.currentTimeMillis() + ".jpg";
				Utils.fileCopy(strSignaturePath, strNewPhotoPath);
				Utils.fileDelete(strSignaturePath);

				//Register it to the map
				mapPhotoMap.put(nFocusNo,strNewPhotoPath);
				refreshPage();
			}
		} else if (strAction.equals(Global.ACTION_SELECT_PRODUCT) == true) {
			int nType = intent.getIntExtra(Global.EXTRA_TYPE, 0);
			if(nType == Global.SELECT_PRODUCT_ITEMUPC)
			{
				String strFrom = intent.getStringExtra(Global.EXTRA_BARCODESCAN_FROM);
				if (!strFrom.equals(getClass().getSimpleName())) {
					return;
				}
				String strItemUPC = intent.getStringExtra(Global.EXTRA_ITEM_UPC);
				for(int i = 0; i < mQuestionList.size(); i++)
				{
					if(mQuestionList.get(i).nLookupNo == nFocusNo)
					{
						mAnswerList.set(i, strItemUPC);
						refreshPage();
						break;
					}
				}
			}
		}
	}

	private int getBranchQuestion(List<SurvQoptInfo> responseList, String strAnswer) {
		int nBranchQuestion = 0;
		for (SurvQoptInfo info : responseList) {
			if (info.strQuestionVal.toUpperCase().equals(strAnswer.toUpperCase()) == true) {
				nBranchQuestion = info.nBranchQuestion;
				break;
			}
		}
		return nBranchQuestion;
	}
	private String getCheckedItemText(String[] strArray, boolean[] valueArray)
	{
		String strText = "";
		for(int i = 0; i < valueArray.length; i++)
		{
			if(valueArray[i])
			{
				if(TextUtils.isEmpty(strText))
				{
					strText = strArray[i];
				}
				else
				{
					strText += " || " + strArray[i];
				}
			}
		}
		return strText;
	}

	private boolean getBranchQuestion(List<SurvQoptInfo> responseList, String[] strAnswerArray, boolean[] checkedItems) {

		for(int i = 0; i < checkedItems.length; i++)
		{
			if(checkedItems[i])
			{
				if(getBranchQuestion(responseList, strAnswerArray[i]) == 999999)
					return true;
			}
		}
		return false;
	}

	private void showChoice(final int position) {

		SurvQuesInfo quesInfo = mQuestionList.get(position);
		if((!TextUtils.isEmpty(quesInfo.strMultiResp) && quesInfo.strMultiResp.equals("Y")) ||
				quesInfo.strQuestionType.equals(Global.QUESTION_TYPE_MULTI_CHOICE))
		{
			final String strOrigText = mAnswerList.get(position);
			String[] selItemArray = strOrigText.split("\\|\\|");
			final boolean bOrigCompleted = mCompleted;
			final List<SurvQoptInfo> responseList = mQuestionList.get(position).responseList;
			final String strArray[] = new String[responseList.size()];
			final boolean checkedItems[] = new boolean[responseList.size()];
			for (int i = 0; i < responseList.size(); i++) {
				strArray[i] = responseList.get(i).strQuestionVal;
				checkedItems[i] = false;
				for(int j = 0; j < selItemArray.length; j++)
				{
					if(strArray[i].trim().equals(selItemArray[j].trim()))
					{
						checkedItems[i] = true;
						break;
					}
				}
			}

			try {
				new AlertDialog.Builder(mContext)
						.setTitle(Str.Please_select_the_response[Global.LANG])
						.setMultiChoiceItems(strArray, checkedItems, (dialogInterface, i, b) -> {
							checkedItems[i] = b;
							mAnswerList.set(position, getCheckedItemText(strArray, checkedItems));
							mCompleted = getBranchQuestion(responseList, strArray, checkedItems);
						})
						.setPositiveButton(Str.Ok[Global.LANG], new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int nSelected) {
								mListAdapter.notifyDataSetChanged();
							}
						})
						.setNegativeButton(Str.Cancel[Global.LANG], new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int nSelected) {
								mAnswerList.set(position, strOrigText);
								mCompleted = bOrigCompleted;
							}
						})
						.create()
						.show();
			}catch (Exception e)
			{
				e.printStackTrace();
			}

		}
		else
		{
			int nOrigIdx = -1;
			final String strOrigText = mAnswerList.get(position);
			final boolean bOrigCompleted = mCompleted;
			final List<SurvQoptInfo> responseList = mQuestionList.get(position).responseList;
			final String strArray[] = new String[responseList.size()];
			for (int i = 0; i < responseList.size(); i++) {
				strArray[i] = responseList.get(i).strQuestionVal;
				if (strArray[i].equals(strOrigText) == true) {
					nOrigIdx = i;
				}
			}
			try {
				new AlertDialog.Builder(mContext)
						.setTitle(Str.Please_select_the_response[Global.LANG])
						.setSingleChoiceItems(new AlertSingleChoiceAdapter(mContext, strArray), nOrigIdx, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int nSelected) {
								mAnswerList.set(position, strArray[nSelected]);
								mCompleted = getBranchQuestion(responseList, strArray[nSelected]) == 999999 ? true : false;
							}
						})
						.setPositiveButton(Str.Ok[Global.LANG], new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int nSelected) {
								mListAdapter.notifyDataSetChanged();
							}
						})
						.setNegativeButton(Str.Cancel[Global.LANG], new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int nSelected) {
								mAnswerList.set(position, strOrigText);
								mCompleted = bOrigCompleted;
							}
						})
						.create()
						.show();
			}catch (Exception e)
			{
				e.printStackTrace();
			}

		}

	}

	private void doComplete() {
		if(mSurveyInfo.strSurveyResponse.equals("PDF"))
		{
			//Do Create PDF
			showProgressDialog(Str.CreatingPdf[Global.LANG]);
			new Thread()
			{
				@Override
				public void run() {
					super.run();
					String strDocNo = getOrderNo();
					if(TextUtils.isEmpty(mStrPdfFileName))
					{
						mStrPdfFileName = strDocNo + ".pdf";
					}

					pdfTrxnNo = System.currentTimeMillis();

					PrintEngine engine = new PrintEngine();
					engine.prepareSurvey(mContext, mQuestionList, mAnswerList, mapPhotoMap, mSurveyInfo.strSurveyTitle, pdfTrxnNo, strDocNo);

					engine.createDocument(mContext, false, Global.PDF_DIR + Global.gRouteNumber + "/" + Utils.getStringSetting(mContext, Global.PREF_LOGIN_DATE) + "/" + mStrPdfFileName, Global.PRINT_SURVEY_DOC);
					new Handler(Looper.getMainLooper()).post(new Runnable() {
						@Override
						public void run() {
							hideProgressDialog();
							doPrepareUploadData();
						}
					});
				}
			}.start();
		}
		else {
			doPrepareUploadData();
		}
	}

	private void doPrepareUploadData()
	{
		initMakeZipInfo();
		long nTrxnNo;
		if(mSurveyInfo != null)
		{
			nTrxnNo = setGeneralUploadData("SURV", 0, 0, -1, mSurveyInfo.strSurveyTitle);
		}
		else
		{
			nTrxnNo = setGeneralUploadData("SURV", 0, 0, -1);
		}

		TransactionInfo transactionInfo = new TransactionInfo();
		transactionInfo.strType = "SURV";
		transactionInfo.strTime = new SimpleDateFormat("kk:mm").format(new Date(nTrxnNo));
		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		dbAdapter.insertTransactionHistoryInfo(transactionInfo);
		dbAdapter.close();

		String strReference = "";
		if (mStrPhotoPath.length() > 0) {
			UFileTransactionInfo uFileTransactionInfo = new UFileTransactionInfo();
			strReference = setUCameraData("CAM", mStrPhotoPath, "", uFileTransactionInfo, 1, true);
			if( mSurveyInfo.nSurveyType == 99 )
			{
				setUImageFileTransactionInfo(0, 0, uFileTransactionInfo.nFileTrxnNo, "", "PREINSPECTION", mSurveyInfo.strSurveyTitle,
						uFileTransactionInfo.strFileCreateDate, uFileTransactionInfo.strFileCreateTime, strReference);
			}
			else if( mSurveyInfo.nSurveyType == 98 )
			{
				setUImageFileTransactionInfo(0, 0, uFileTransactionInfo.nFileTrxnNo, "", "POSTINSPECTION", mSurveyInfo.strSurveyTitle,
						uFileTransactionInfo.strFileCreateDate, uFileTransactionInfo.strFileCreateTime, strReference);
			}
		}
		USurveysInfo uSurveyInfo = new USurveysInfo();
		uSurveyInfo.nTrxnNo = nTrxnNo;
		uSurveyInfo.nChainNo = 0;
		uSurveyInfo.nCustNo = 0;
		uSurveyInfo.strSurveyID = mSurveyInfo.strSurveyID;
		uSurveyInfo.bFromSurveyID = mSurveyInfo.bFromSurveyID;
		uSurveyInfo.strCompleted = mCompleted == true ? "1" : "0";
		uSurveyInfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(mContext, new Date(nTrxnNo)));
		uSurveyInfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(nTrxnNo);
//		uSurveyInfo.strReference = strReference;
		uSurveyInfo.detailList = new ArrayList<>();
		for (int i=0; i<mQuestionList.size(); i++) {
			USurveyDetailInfo uSurveyDetailInfo = new USurveyDetailInfo();
			uSurveyDetailInfo.nTrxnNo = uSurveyInfo.nTrxnNo;
			//uSurveyDetailInfo.strQuestionID = mQuestionList.get(i).strQuestionID;
			uSurveyDetailInfo.strQuestionID = String.valueOf(mQuestionList.get(i).nLookupNo);

			if(mQuestionList.get(i).strQuestionType.equals(Global.QUESTION_TYPE_PHOTOGRAPH) || mQuestionList.get(i).strQuestionType.equals(Global.QUESTION_TYPE_SIGNATURE))
			{
				String strValue = mapPhotoMap.get(mQuestionList.get(i).nLookupNo);
				uSurveyDetailInfo.strResultVal = Utils.getFileName(strValue);
			}
			else
			{
				uSurveyDetailInfo.strResultVal = mAnswerList.get(i);
			}
			uSurveyDetailInfo.nVisible = 0;
			uSurveyDetailInfo.nSeqNo = 0;
			uSurveyInfo.detailList.add(uSurveyDetailInfo);
		}
		dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		if( mSurveyInfo.nSurveyType != 98)
		{
			mSurveyInfo.bCompleted = true;
		}
		dbAdapter.updateSurveyInfo(mSurveyInfo);
		dbAdapter.insertUSurveysInfo(uSurveyInfo);
		for (USurveyDetailInfo info : uSurveyInfo.detailList) {
			dbAdapter.insertUSurveyDetailInfo(info);
		}
		dbAdapter.close();
		AddTrxnNo(0,0,uSurveyInfo.nTrxnNo);
		if (mIsFromEndTrip == false) {
			//Global.gUploadCustNoList.add(makeZipFileInfo);
			Utils.registerUploadFile(mContext, makeZipFileInfo);

		} else {
			Utils.setBooleanSetting(mContext, String.format(Locale.ENGLISH, Global.PREF_END_TRIP_CHECKED, 5), true);
		}

		mContext.onBackPressed();
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
			/*case R.id.btnNote:
				Utils.printDebug(mContext, "Note");
				break;*/
			case R.id.btnPhoto:
				nFocusNo = -1;
				selectPhoto();
				break;
			case R.id.btnCompleted:
				doComplete();
				break;
		}
		m_bClicked = false;
	}

	private void setFormattedAmount(EditText editText, String strVal) {
		String strArray[] = strVal.split("\\.");
		strVal = "";
		for (int i=0; i<strArray.length; i++) {
			strVal += strArray[i];
		}
		int nValue = Utils.getIntSetting(mContext, Global.PREF_DECIMAL);
		float mAmount = Utils.parseFloatFromString(strVal) / (float)(Math.pow(10,nValue));
		editText.setTag("From App");
		editText.setText(String.format(Locale.ENGLISH, "%." + Utils.getIntSetting(mContext, Global.PREF_DECIMAL) + "f", mAmount));
		editText.setSelection(editText.getText().toString().length());
		editText.setTag(null);
	}

	private class ListAdapter extends BaseAdapter {

		private class ViewHolder {
			public TextView txtQuestion, txtChoice;
			public RadioGroup radioGroup;
			public RadioButton radioYes, radioNo;
			public EditText editInput;

			public EditText editNumberInput;

			public Button	btnSignature;

			public RelativeLayout layoutPrice;
			public TextView lblCurrencySymbol;
			public EditText editPrice;

			public LinearLayout layoutBarcode;
			public ImageView btnBarcode;
			public TextView txtBarcode;

			public LinearLayout layoutImage;
			public ImageView btnCamera;
			public TextView txtImage;

			public LinearLayout layoutDate;
			public EditText editDate;
			public ImageView btnDate;

		}

		public int getCount() {
			return mQuestionList.size();
		}

		public SurvQuesInfo getItem(int position) {
			if(position < mQuestionList.size())
			{
				return mQuestionList.get(position);
			}
			else
			{
				return null;
			}
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(final int position, View convertView, ViewGroup parent) {
			View view = convertView;
			final ViewHolder holder;
			final SurvQuesInfo info = getItem(position);
			if(info == null) return null;

			view = mContext.getLayoutInflater().inflate(R.layout.row_pre_trip_check, parent, false);

			holder = new ViewHolder();
			holder.txtQuestion = view.findViewById(R.id.txtQuestion);
			holder.txtChoice = view.findViewById(R.id.txtChoice);
			holder.radioGroup = view.findViewById(R.id.radioGroup);
			holder.radioYes = view.findViewById(R.id.radioYes);
			holder.radioNo = view.findViewById(R.id.radioNo);
			holder.editInput = view.findViewById(R.id.editInput);
			holder.editNumberInput = view.findViewById(R.id.editNumberInput);
			holder.btnSignature = view.findViewById(R.id.btnSignature);
			holder.layoutPrice = view.findViewById(R.id.layoutPrice);
			holder.lblCurrencySymbol = view.findViewById(R.id.lblCurrencySymbol);
			holder.editPrice = view.findViewById(R.id.editPrice);
			holder.layoutBarcode = view.findViewById(R.id.layoutBarcode);
			holder.txtBarcode = view.findViewById(R.id.txtBarcode);
			holder.btnBarcode = view.findViewById(R.id.btnBarcode);
			holder.layoutImage = view.findViewById(R.id.layoutImage);
			holder.txtImage = view.findViewById(R.id.txtImage);
			holder.btnCamera = view.findViewById(R.id.btnCamera);
			holder.layoutDate = view.findViewById(R.id.layoutDate);
			holder.editDate = view.findViewById(R.id.editDate);
			holder.btnDate = view.findViewById(R.id.btnDate);

			view.setTag(holder);

			String strAnswer = "";
			if (position < mAnswerList.size()) {
				strAnswer = mAnswerList.get(position);
			}

			holder.txtQuestion.setText(info.strQuestionText);

			holder.radioGroup.setVisibility(View.GONE);
			holder.txtChoice.setVisibility(View.GONE);
			holder.editInput.setVisibility(View.GONE);
			holder.editNumberInput.setVisibility(View.GONE);
			holder.btnSignature.setVisibility(View.GONE);
			holder.layoutPrice.setVisibility(View.GONE);
			holder.layoutBarcode.setVisibility(View.GONE);
			holder.layoutImage.setVisibility(View.GONE);
			holder.layoutDate.setVisibility(View.GONE);

			if(mapInvisibleQues.get(info.nLookupNo) != null && mapInvisibleQues.get(info.nLookupNo))
			{
				holder.txtQuestion.setVisibility(View.GONE);
				view.findViewById(R.id.divider).setVisibility(View.GONE);
				return view;
			}
			else {
				holder.txtQuestion.setVisibility(View.VISIBLE);
				view.findViewById(R.id.divider).setVisibility(View.VISIBLE);
			}

			if (info.strQuestionType.equals(Global.QUESTION_TYPE_RADIO) == true) {
				holder.radioGroup.setVisibility(View.VISIBLE);
				holder.radioYes.setText(Str.Yes[Global.LANG]);
				holder.radioNo.setText(Str.No[Global.LANG]);
				if (mAnswerList.get(position).equals(Str.Yes[Global.LANG]) == true) {
					holder.radioYes.setChecked(true);
					holder.radioNo.setChecked(false);
				} else if (strAnswer.equals(Str.No[Global.LANG]) == true) {
					holder.radioYes.setChecked(false);
					holder.radioNo.setChecked(true);
				}
				holder.radioYes.setEnabled(!mCompleted);
				holder.radioNo.setEnabled(!mCompleted);
				holder.radioYes.setTextColor(mCompleted == true ? Color.GRAY : Color.BLACK);
				holder.radioNo.setTextColor(mCompleted == true ? Color.GRAY : Color.BLACK);
				holder.radioYes.setOnCheckedChangeListener((buttonView, isChecked) -> {
					if (isChecked == true) {
						mCompleted = getBranchQuestion(info.responseList, Str.Yes[Global.LANG]) == 999999 ? true : false;
						mAnswerList.set(position, Str.Yes[Global.LANG]);
						for(int i = info.nLookupNo + 1; i < info.nQuestionNumberNo; i++)
						{
							mapInvisibleQues.put(i, false);
						}
						for(int i = info.nLookupNo + 1; i < info.nQuestionNumberYes; i++)
						{
							mapInvisibleQues.put(i, true);
						}
						notifyDataSetChanged();
					}
					refreshCompleted();
				});
				holder.radioNo.setOnCheckedChangeListener((buttonView, isChecked) -> {
					if (isChecked == true) {
						mCompleted = getBranchQuestion(info.responseList, Str.No[Global.LANG]) == 999999 ? true : false;
						mAnswerList.set(position, Str.No[Global.LANG]);
						for(int i = info.nLookupNo + 1; i < info.nQuestionNumberYes; i++)
						{
							mapInvisibleQues.put(i, false);
						}
						for(int i = info.nLookupNo + 1; i < info.nQuestionNumberNo; i++)
						{
							mapInvisibleQues.put(i, true);
						}
						notifyDataSetChanged();
					}
					refreshCompleted();
				});
			}
			else if (info.strQuestionType.equals(Global.QUESTION_TYPE_CHOICE) == true || info.strQuestionType.equals(Global.QUESTION_TYPE_MULTI_CHOICE) == true) {
				holder.txtChoice.setVisibility(View.VISIBLE);
				if(TextUtils.isEmpty(strAnswer))
				{
					holder.txtChoice.setText(Str.Please_select_the_response[Global.LANG]);
				}
				else
				{
					holder.txtChoice.setText(strAnswer);
				}

				holder.txtChoice.setEnabled(!mCompleted);
				holder.txtChoice.setTextColor(mCompleted == true ? Color.GRAY : Color.BLACK);
				holder.txtChoice.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						showChoice(position);
						refreshPage();
					}
				});
			}
			else if ( info.strQuestionType.equals(Global.QUESTION_TYPE_ALPHANUMERIC) == true) {
				holder.editInput.setVisibility(View.VISIBLE);
				holder.editInput.setHint(Str.Please_input_the_response[Global.LANG]);
				holder.editInput.setText(strAnswer);
				holder.editInput.setEnabled(!mCompleted);
				holder.editInput.setTextColor(mCompleted == true ? Color.GRAY : Color.BLACK);
				holder.editInput.addTextChangedListener(new TextWatcher() {
					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count) {
					}

					@Override
					public void beforeTextChanged(CharSequence s, int start, int count, int after) {
					}

					@Override
					public void afterTextChanged(Editable s) {
						mAnswerList.set(position, s.toString());
						refreshCompleted();
					}
				});
			} else if (info.strQuestionType.equals(Global.QUESTION_TYPE_NUMERIC) == true) {
				holder.editNumberInput.setVisibility(View.VISIBLE);
				holder.editNumberInput.setHint(Str.Please_input_the_response[Global.LANG]);
				holder.editNumberInput.setText(strAnswer);
				holder.editNumberInput.setEnabled(!mCompleted);
				holder.editNumberInput.setTextColor(mCompleted == true ? Color.GRAY : Color.BLACK);
				holder.editNumberInput.addTextChangedListener(new TextWatcher() {
					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count) {
					}

					@Override
					public void beforeTextChanged(CharSequence s, int start, int count, int after) {
					}

					@Override
					public void afterTextChanged(Editable s) {
						mAnswerList.set(position, s.toString());
						refreshCompleted();
					}
				});
			} else if (info.strQuestionType.equals(Global.QUESTION_TYPE_PRICE) == true) {
				holder.layoutPrice.setVisibility(View.VISIBLE);
				holder.lblCurrencySymbol.setHint(Global.gCurrencySymbol);

				holder.editPrice.setText(strAnswer);
				holder.editPrice.setEnabled(!mCompleted);
				holder.editPrice.setTextColor(mCompleted == true ? Color.GRAY : Color.BLACK);
				holder.editPrice.addTextChangedListener(new TextWatcher() {
					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count) {
					}

					@Override
					public void beforeTextChanged(CharSequence s, int start, int count, int after) {
					}

					@Override
					public void afterTextChanged(Editable s) {
						if (holder.editPrice.getTag() == null) {
							setFormattedAmount(holder.editPrice, s.toString());
						}
						else {
							String strText = holder.editPrice.getText().toString();
							mAnswerList.set(position, strText);
							refreshCompleted();
						}
					}
				});
			} else if (info.strQuestionType.equals(Global.QUESTION_TYPE_SIGNATURE) == true) {
				holder.btnSignature.setVisibility(View.VISIBLE);
				holder.btnSignature.setOnClickListener(view13 -> {
					nFocusNo = info.nLookupNo;

					Intent intent = new Intent(mContext, SignatureActivity.class);
					startActivity(intent);
					mContext.overridePendingTransition(R.anim.zoom_in, 0);
				});
			} else if(info.strQuestionType.equals(Global.QUESTION_TYPE_PHOTOGRAPH) == true){
				holder.layoutImage.setVisibility(View.VISIBLE);
				String strPath = mapPhotoMap.get(info.nLookupNo);
				if(TextUtils.isEmpty(strPath))
				{
					holder.txtImage.setText(Str.ImageCaptured[Global.LANG]);
				}

				holder.btnCamera.setOnClickListener(view12 -> {
					nFocusNo = info.nLookupNo;
					selectPhoto();
				});
			} else if(info.strQuestionType.equals(Global.QUESTION_TYPE_BARCODE) == true){
				holder.layoutBarcode.setVisibility(View.VISIBLE);
				holder.txtBarcode.setText(mAnswerList.get(position));
				holder.btnBarcode.setOnClickListener(view1 -> {
					nFocusNo = info.nLookupNo;
					Intent intent = new Intent(mContext, BarcodeScannerActivity.class);
					intent.putExtra(Global.EXTRA_BARCODESCAN_FROM, PreTripCheckFragment.this.getClass().getSimpleName());
					startActivity(intent);
					mContext.overridePendingTransition(R.anim.start1, R.anim.start2);
				});
			} else if(info.strQuestionType.equals(Global.QUESTION_TYPE_DATE) == true){
				String strDate = Utils.convertTimeFormat(mAnswerList.get(position), "yyyyMMdd", Utils.getDateFormat(mContext, Global.DATE_DD_MM_YYYY_FORMAT));
				holder.layoutDate.setVisibility(View.VISIBLE);
				holder.editDate.setText(strDate);
				holder.btnDate.setOnClickListener(view1 -> {

					DatePickerDialog.OnDateSetListener dateListener = (dateView, year, monthOfYear, dayOfMonth) -> {
						String strDate1 = String.format(Locale.ENGLISH, "%04d%02d%02d", year, monthOfYear + 1, dayOfMonth);
						mAnswerList.set(position, strDate1);
						notifyDataSetChanged();
						refreshCompleted();
					};

					Date date = new Date();
					SimpleDateFormat sdf = new SimpleDateFormat(Utils.getDateFormat(mContext, "MM/dd/yyyy"));
					try {
						date = sdf.parse(mAnswerList.get(position));
					}catch (Exception e)
					{
						e.printStackTrace();
					}

					try {
						new DatePickerDialog(mContext, dateListener, 1900 + date.getYear(), date.getMonth(), date.getDate()).show();
					}catch (Exception e)
					{
						e.printStackTrace();
					}
				});
			}

			view.setOnClickListener(v -> {
			});

			return view;
		}
	}
}