package dsd.dsdvansales.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import dsd.dsdvansales.R;
import dsd.dsdvansales.activities.MainActivity;
import dsd.dsdvansales.activities.PhotoDetailActivity;
import dsd.dsdvansales.api.AttachmentApiClient;
import dsd.dsdvansales.db.DbAdapter;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.model.AttachmentInfo;
import dsd.dsdvansales.model.CustomerNoteInfo;
import dsd.dsdvansales.model.LoginInfo;
import dsd.dsdvansales.model.MakeZipFileInfo;
import dsd.dsdvansales.model.UAttachmentInfo;
import dsd.dsdvansales.model.UCustomerNoteInfo;
import dsd.dsdvansales.model.UTransactionsInfo;
import retrofit2.Call;
import retrofit2.Response;

public class CustomerNoteDialog extends Dialog implements View.OnClickListener {

	public interface Callback{
		void doContinue();
	}

	private MainActivity mContext = null;
	Callback callback;
	CustomerNoteInfo customerNoteInfo;

	List<UAttachmentInfo> attachmentInfoList = new ArrayList<>();
	GridAdapter gridAdapter = null;
	GridView mGridView = null;

	private boolean bModified = false;
	private boolean bVisitMode;
	private String strNotes = "";

	private boolean bNewData = false;

	public CustomerNoteDialog(MainActivity context, CustomerNoteInfo customerNoteInfo, Callback callback) {
		super(context, android.R.style.Theme_Translucent_NoTitleBar);
		this.mContext = context;
		this.callback = callback;
		if(customerNoteInfo != null)
		{
			this.bNewData = false;
			this.customerNoteInfo = customerNoteInfo;
			this.bVisitMode = true;
			this.bModified = false;
		}
		else
		{
			this.bNewData = true;
			Date date = new Date();
			this.customerNoteInfo = new CustomerNoteInfo();
			this.customerNoteInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
			this.customerNoteInfo.nChainNo = Global.gCurCustomerInfo.nChainNo;

			this.customerNoteInfo.strNoteDate = new SimpleDateFormat("yyyyMMdd").format(date);
			this.customerNoteInfo.strNoteTime = new SimpleDateFormat("kkmm").format(date);
			this.customerNoteInfo.strCreatedTimeAndNoteDate = new SimpleDateFormat("yyyyMMddkkmmssSSS").format(date);
			this.customerNoteInfo.strNote = "";
			this.customerNoteInfo.strNoteId = "-1";

			this.customerNoteInfo.strCreatedBy = Utils.getStringSetting(mContext, Global.PREF_USER_DESCRIPTION);
			this.customerNoteInfo.strNoteType = "92";
			this.bVisitMode = false;
			this.bModified = true;
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
		lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
		lpWindow.dimAmount = 0.9f;
		getWindow().setAttributes(lpWindow);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.dialog_customer_note);

		initView();
	}

	private void initView() {
		((TextView)findViewById(R.id.txtTitle)).setText(Str.Delivery_Instructions[Global.LANG].toUpperCase());
		((Button)findViewById(R.id.btnContinue)).setText(Str.Continue[Global.LANG]);
		((Button)findViewById(R.id.btnUpdate)).setText(Str.Update[Global.LANG]);
		((Button)findViewById(R.id.btnSaveChanges)).setText(Str.SaveChanges[Global.LANG]);

		findViewById(R.id.btnContinue).setOnClickListener(this);
		findViewById(R.id.btnUpdate).setOnClickListener(this);
		findViewById(R.id.btnSaveChanges).setOnClickListener(this);
		findViewById(R.id.btnNewPhoto).setOnClickListener(this);

		final int nAvailableCount = 1000;
		((EditText)findViewById(R.id.editDeliveryInstruction)).addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				strNotes = s.toString();
				((TextView)findViewById(R.id.txtDeliveryInstruction)).setText(strNotes);
				((TextView) findViewById(R.id.txtCharacters)).setText(String.format(Locale.ENGLISH, Str.D_Characters[Global.LANG], nAvailableCount - strNotes.length()));
			}
		});
		mGridView = findViewById(R.id.imgList);
		gridAdapter = new GridAdapter();
		mGridView.setAdapter(gridAdapter);
		loadData();
	}

	private void loadData() {

		((TextView)findViewById(R.id.txtDeliveryInstruction)).setText(customerNoteInfo.strNote);
		((EditText)findViewById(R.id.editDeliveryInstruction)).setText(customerNoteInfo.strNote);
		int nAvailableCount = 1000;
		((TextView) findViewById(R.id.txtCharacters)).setText(String.format(Locale.ENGLISH, Str.D_Characters[Global.LANG], nAvailableCount - customerNoteInfo.strNote.length()));
		findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);
		strNotes = customerNoteInfo.strNote;

		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		List<UCustomerNoteInfo> uCustomerNoteInfoList = dbAdapter.getUCustomerInfoList(Global.gCurCustomerInfo.nCustNo, Global.gCurCustomerInfo.nChainNo, customerNoteInfo.strNoteType, customerNoteInfo.strNoteDate, customerNoteInfo.strNoteTime);
		dbAdapter.close();
		if(uCustomerNoteInfoList.size() == 0)
		{
			if(!TextUtils.isEmpty(customerNoteInfo.strNoteId) && !customerNoteInfo.strNoteId.equals("-1") &&
					!TextUtils.isEmpty(Utils.getStringSetting(mContext, Global.PREF_APICOMPANY)) && !TextUtils.isEmpty(Utils.getStringSetting(mContext, Global.PREF_APIUSER))
					&& !TextUtils.isEmpty(Utils.getStringSetting(mContext, Global.PREF_APIPASSWORD)) && Utils.isConnectingToInternet(mContext))
			{
				final String strPinNumber = Utils.getStringSetting(mContext, Global.PREF_APICOMPANY);
				final String strBaseUrl = Utils.getBaseURL(strPinNumber);
				Map<String, String> params = new HashMap<>();
				params.put("username", Utils.getStringSetting(mContext, Global.PREF_APIUSER));
				params.put("password", Utils.getStringSetting(mContext, Global.PREF_APIPASSWORD));
				AttachmentApiClient.getMainApiClient(strBaseUrl, null).login(params).enqueue(new retrofit2.Callback<LoginInfo>() {
					@Override
					public void onResponse(Call<LoginInfo> call, Response<LoginInfo> response) {
						if(response.body() != null)
						{
							String token = response.body().token;
							Map<String, String> headers = new HashMap<>();
							headers.put("X-Auth-Token", token);
							AttachmentApiClient.getMainApiClient(strBaseUrl, headers).getAttachment(customerNoteInfo.strNoteId).enqueue(new retrofit2.Callback<List<AttachmentInfo>>() {
								@Override
								public void onResponse(Call<List<AttachmentInfo>> call, Response<List<AttachmentInfo>> response) {
									List<AttachmentInfo> attachmentInfoList = response.body();
									if(attachmentInfoList != null && attachmentInfoList.size() > 0)
									{
										List<UAttachmentInfo> uAttachmentInfoList = new ArrayList<>();
										for(AttachmentInfo attachmentInfo : attachmentInfoList)
										{
											UAttachmentInfo info = attachmentInfo.convertToUAttachment(strBaseUrl, customerNoteInfo.strNoteId);
											if(Utils.isFileExist(Global.PHOTO_DIR + info.strFileName))
											{
												info.strFilePath = Global.PHOTO_DIR + info.strFileName;
											}
											uAttachmentInfoList.add(info);
										}

										CustomerNoteDialog.this.attachmentInfoList.clear();
										CustomerNoteDialog.this.attachmentInfoList.addAll(uAttachmentInfoList);
										findViewById(R.id.progress_bar).setVisibility(View.GONE);
										refreshPage();
									}
									else
									{
										findViewById(R.id.progress_bar).setVisibility(View.GONE);
									}
								}

								@Override
								public void onFailure(Call<List<AttachmentInfo>> call, Throwable t) {
									findViewById(R.id.progress_bar).setVisibility(View.GONE);
									if(!((Activity)mContext).isFinishing() && !((Activity)mContext).isDestroyed())
									{
										try {
											Toast.makeText(mContext, Str.NetworkConnectionError[Global.LANG], Toast.LENGTH_LONG).show();
										}catch (Exception e)
										{
											e.printStackTrace();
										}
									}

								}
							});
						}
						else
						{
							findViewById(R.id.progress_bar).setVisibility(View.GONE);
							if(!((Activity)mContext).isFinishing() && !((Activity)mContext).isDestroyed())
							{
								try {
									Toast.makeText(mContext, Str.LoginFailedGettingAttachment[Global.LANG], Toast.LENGTH_LONG).show();
								}catch (Exception e)
								{
									e.printStackTrace();
								}
							}

						}
					}

					@Override
					public void onFailure(Call<LoginInfo> call, Throwable t) {
						findViewById(R.id.progress_bar).setVisibility(View.GONE);
						if(!((Activity)mContext).isFinishing() && !((Activity)mContext).isDestroyed())
						{
							try {
								Toast.makeText(mContext, Str.NetworkConnectionError[Global.LANG], Toast.LENGTH_LONG).show();
							}catch (Exception e)
							{
								e.printStackTrace();
							}
						}

					}
				});
			}
			else {
				attachmentInfoList.clear();
				findViewById(R.id.progress_bar).setVisibility(View.GONE);
				refreshPage();
			}

		}
		else
		{
			attachmentInfoList.clear();
			attachmentInfoList.addAll(uCustomerNoteInfoList.get(uCustomerNoteInfoList.size() - 1).uAttachmentInfoList);
			findViewById(R.id.progress_bar).setVisibility(View.GONE);
			refreshPage();
		}
	}

	private void refreshPage()
	{
		if(bVisitMode)
		{
			findViewById(R.id.btnUpdate).setVisibility(View.VISIBLE);
			findViewById(R.id.btnSaveChanges).setVisibility(View.GONE);
			findViewById(R.id.btnNewPhoto).setVisibility(View.GONE);
			findViewById(R.id.btnContinue).setVisibility(View.VISIBLE);

			findViewById(R.id.txtDeliveryInstruction).setVisibility(View.VISIBLE);
			findViewById(R.id.layoutEditMessage).setVisibility(View.GONE);
		}
		else
		{
			findViewById(R.id.btnUpdate).setVisibility(View.GONE);
			findViewById(R.id.btnSaveChanges).setVisibility(View.VISIBLE);
			findViewById(R.id.btnNewPhoto).setVisibility(View.VISIBLE);
			findViewById(R.id.btnContinue).setVisibility(View.GONE);

			findViewById(R.id.txtDeliveryInstruction).setVisibility(View.GONE);
			findViewById(R.id.layoutEditMessage).setVisibility(View.VISIBLE);
		}

		gridAdapter.notifyDataSetChanged();
	}

	private void doContinue()
	{
		if(bModified)
		{
			doProcessModify();
		}
		else
		{
			if(callback != null)
			{
				callback.doContinue();
			}
			dismiss();
		}
	}

	private void doUpdate()
	{
		bVisitMode = false;
		bModified = true;
		refreshPage();
	}

	private void doEditComplete()
	{
		bVisitMode = true;
		refreshPage();
	}

	private void doTakeNewPhoto()
	{
		if(Build.VERSION.SDK_INT >= 24)
		{
			File photoFile = null;
			try {
				photoFile = mContext.createImageFile();
			} catch (IOException ex) {
			}
			if(photoFile != null) {
				try {
					Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					Uri photoURI = FileProvider.getUriForFile(mContext,
							"dsd.dsdvansales.fileprovider",
							photoFile);
					takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
					Global.gCustomerNoteDialog = this;
					mContext.startActivityForResult(takePictureIntent, mContext.RESULT_TAKE_PHOTO);
				}
				catch (ActivityNotFoundException e) {
					Toast.makeText(mContext, Str.CameraAppNotFound[Global.LANG], Toast.LENGTH_LONG).show();
				}
			}
		}
		else
		{
			try {
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				File f = new File(Global.ROOT_LOCATION, "temp.jpg");
				intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
				Global.gCustomerNoteDialog = this;
				mContext.startActivityForResult(intent, mContext.RESULT_TAKE_PHOTO);
			}
			catch (ActivityNotFoundException e) {
				Toast.makeText(mContext, Str.CameraAppNotFound[Global.LANG], Toast.LENGTH_LONG).show();
			}
		}
	}

	public void doTakePhoto(String strPath)
	{
		if(!TextUtils.isEmpty(strPath) && strPath.equals(Global.PHOTO_DIR + "tmp.jpg"))
		{
			Global.gCustomerNoteDialog = null;
			UAttachmentInfo uAttachmentInfo = new UAttachmentInfo();
			uAttachmentInfo.strFileName = System.currentTimeMillis() + ".jpg";
			uAttachmentInfo.strFilePath = Global.PHOTO_DIR + uAttachmentInfo.strFileName;
			Utils.fileCopy(strPath, uAttachmentInfo.strFilePath);
			Utils.fileDelete(strPath);
			attachmentInfoList.add(uAttachmentInfo);
			gridAdapter.notifyDataSetChanged();
			return;
		}
	}

	private void doProcessModify()
	{
		ProgressDialog progressDialog = new ProgressDialog(mContext);
		progressDialog.setCancelable(false);
		progressDialog.show();
		new Thread()
		{
			@Override
			public void run() {
				DbAdapter dbAdapter = new DbAdapter(mContext);
				dbAdapter.open();

				long nTrxnNo = System.currentTimeMillis();
				Date date = new Date(nTrxnNo);
				//Update current progress....
				customerNoteInfo.strNoteDate = new SimpleDateFormat("yyyyMMdd").format(date);
				customerNoteInfo.strNoteTime = new SimpleDateFormat("kkmmss").format(date);
				customerNoteInfo.strNote = strNotes;
				customerNoteInfo.nAttachment = attachmentInfoList.size();
				if(bNewData)
				{
					dbAdapter.insertCustomerNoteInfo(customerNoteInfo);
				}
				else
				{
					dbAdapter.updateCustomerNoteInfo(customerNoteInfo);
				}


				UCustomerNoteInfo uInfo = new UCustomerNoteInfo();
				uInfo.nTrxnNo = nTrxnNo;
				uInfo.strDocNo = "0";
				uInfo.strDocType = "NOTE";
				uInfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(uInfo.nTrxnNo);;
				uInfo.strTrxnTime = new SimpleDateFormat("kkmmss").format(uInfo.nTrxnNo);;
				uInfo.nChainNo = customerNoteInfo.nChainNo;
				uInfo.nCustNo = customerNoteInfo.nCustNo;
				uInfo.strSalesDate = uInfo.strTrxnDate;
				if(bNewData) {
					uInfo.strTransactionType = "A";
				} else{
					uInfo.strTransactionType = "U";
				}
				uInfo.strUser = Utils.getStringSetting(mContext, Global.PREF_USER_NAME);
				uInfo.strNoteType = "92";
				if(customerNoteInfo.strNoteId != null && !TextUtils.isEmpty(customerNoteInfo.strNoteId) && !customerNoteInfo.strNoteId.equals("-1"))
				{
					uInfo.strReference = customerNoteInfo.strNoteId;
				}
				uInfo.strMessage = strNotes;
				uInfo.nAttachment = attachmentInfoList.size();
				dbAdapter.insertUCustomerNoteInfo(uInfo);

				for(UAttachmentInfo uAttachmentInfo : attachmentInfoList)
				{
					if(!Utils.isFileExist(Global.PHOTO_DIR + uAttachmentInfo.strFileName))
					{
						//Download the file from the server.
						CountDownLatch latch = new CountDownLatch(1);
						Glide.with(mContext)
								.asBitmap()
								.load(uAttachmentInfo.strFilePath)
								.into(new CustomTarget<Bitmap>() {
									@Override
									public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
										Utils.createDirectory(Global.PHOTO_DIR);
										Utils.saveBitmapToFile(resource, Global.PHOTO_DIR + uAttachmentInfo.strFileName);
										uAttachmentInfo.strFilePath = Global.PHOTO_DIR + uAttachmentInfo.strFileName;
										latch.countDown();
									}

									@Override
									public void onLoadCleared(@Nullable Drawable placeholder) {
										latch.countDown();
									}
								});
						try {
							latch.await();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					uAttachmentInfo.nTrxnNo = uInfo.nTrxnNo;
					dbAdapter.insertUAttachmentInfo(uAttachmentInfo);
				}

				//Make the Upload Data and start uploading.
				UTransactionsInfo uTransactionInfo = new UTransactionsInfo();
				uTransactionInfo.nTrxnNo = uInfo.nTrxnNo;
				uTransactionInfo.strDocType = "NOTE";
				uTransactionInfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(mContext, new Date(uInfo.nTrxnNo)));
				uTransactionInfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(uInfo.nTrxnNo);
				uTransactionInfo.nChainNo = customerNoteInfo.nChainNo;
				uTransactionInfo.nCustNo = customerNoteInfo.nCustNo;

				uTransactionInfo.strTripId = Utils.getStringSetting(mContext, Global.PREF_TRIP_ID);
				uTransactionInfo.strDriverId = "";
				uTransactionInfo.strDriverName = Utils.getStringSetting(mContext, Global.PREF_USER_DESCRIPTION);
				uTransactionInfo.strDriverNumber = Utils.getStringSetting(mContext, Global.PREF_DRIVER_NUMBER);

				dbAdapter.insertUTransactionsInfo(uTransactionInfo);
				dbAdapter.close();

				AddTrxnNo(customerNoteInfo.nCustNo, customerNoteInfo.nChainNo, uInfo.nTrxnNo);
				Utils.registerUploadFile(mContext, makeZipFileInfo);

				new Handler(Looper.getMainLooper()).post(new Runnable() {
					@Override
					public void run() {
						progressDialog.dismiss();
						bModified = false;
						doContinue();
					}
				});
			}
		}.start();
	}

	public MakeZipFileInfo makeZipFileInfo = null;

	public void AddTrxnNo(int nCustNo, int nChainNo, long nTrxnNo)
	{
		if( makeZipFileInfo == null )
		{
			makeZipFileInfo = new MakeZipFileInfo();
			makeZipFileInfo.nCustNo = nCustNo;
			makeZipFileInfo.nChainNo = nChainNo;
		}
		boolean bAdd = true;
		for(Long trxnNo : makeZipFileInfo.trxnList)
		{
			if(trxnNo == nTrxnNo)
			{
				bAdd = false;
				break;
			}
		}
		if( bAdd )
		{
			makeZipFileInfo.trxnList.add(nTrxnNo);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId())
		{
			case R.id.btnContinue:
				doContinue();
				break;
			case R.id.btnUpdate:
				doUpdate();
				break;
			case R.id.btnSaveChanges:
				doEditComplete();
				break;
			case R.id.btnNewPhoto:
				doTakeNewPhoto();
				break;
		}
	}

	public class GridAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			return attachmentInfoList.size();
		}

		@Override
		public Object getItem(int position) {
			return attachmentInfoList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			UAttachmentInfo info = (UAttachmentInfo)getItem(position);
			final View myImageView;
			if (convertView == null) {
				myImageView = (View)getLayoutInflater().inflate(R.layout.image_item, parent, false);
			} else {
				myImageView = (View)convertView;
			}

			if(Utils.isFileExist(Global.PHOTO_DIR + info.strFileName))
			{
				Glide.with(mContext)
					.load(Global.PHOTO_DIR + info.strFileName)
					.dontTransform()
					.placeholder(R.drawable.unknown)
					.error(R.drawable.ic_error)
					.into((ImageView)myImageView.findViewById(R.id.img));
			}
			else
			{
				Glide.with(mContext)
						.asBitmap()
						.load(info.strFilePath)
						.into(new CustomTarget<Bitmap>() {
							@Override
							public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
								Utils.createDirectory(Global.PHOTO_DIR);
								Utils.saveBitmapToFile(resource, Global.PHOTO_DIR + info.strFileName);
								info.strFilePath = Global.PHOTO_DIR + info.strFileName;
								notifyDataSetChanged();
							}

							@Override
							public void onLoadCleared(@Nullable Drawable placeholder) {

							}
						});
				((ImageView)myImageView.findViewById(R.id.img)).setImageDrawable(mContext.getResources().getDrawable(R.drawable.unknown));
			}

			if(bVisitMode)
			{
				myImageView.findViewById(R.id.btnDelete).setVisibility(View.GONE);
				myImageView.findViewById(R.id.btnDelete).setOnClickListener(null);
			}
			else
			{
				myImageView.findViewById(R.id.btnDelete).setVisibility(View.VISIBLE);
				myImageView.findViewById(R.id.btnDelete).setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						attachmentInfoList.remove(info);
						notifyDataSetChanged();
					}
				});
			}

			myImageView.findViewById(R.id.img).setOnClickListener(v -> {
				Global.gShowImageUrl = info.strFilePath;
				Intent intent = new Intent(mContext, PhotoDetailActivity.class);
				mContext.startActivity(intent);
			});
			return myImageView;
		}
	}

}

