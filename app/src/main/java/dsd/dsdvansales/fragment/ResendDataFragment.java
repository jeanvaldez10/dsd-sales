package dsd.dsdvansales.fragment;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import dsd.dsdvansales.R;
import dsd.dsdvansales.db.BackupDbAdapter;
import dsd.dsdvansales.dialogs.MessageDialog;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.UploadViaHTTP;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.model.UploadFileInfo;

public class ResendDataFragment extends BaseFragment implements View.OnClickListener {

	private TextView mTxtNoData = null;
	private ListView mListView = null;
	private ListAdapter mListAdapter = null;
	private List<String> mResendDateList = new ArrayList<>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootView = inflater.inflate(R.layout.fragment_resend, container, false);
		commonInit();
		return mRootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		Global.gFragIdx = Global.FRAG_RESEND_DATA;
	}

	@Override
	public void initView() {
		mTxtNoData = (TextView)mRootView.findViewById(R.id.txtNoData);
		mListView = (ListView)mRootView.findViewById(R.id.listView);
		mListAdapter = new ListAdapter();
		mListView.setAdapter(mListAdapter);
		mRootView.findViewById(R.id.btnBack).setOnClickListener(this);
		loadData();
		refreshLanguage();
	}

	@Override
	public void loadData() {
		showProgressDialog(Str.Loading[Global.LANG]);
		new Thread(){
			@Override
			public void run() {
				Utils.deleteBackupExpiredData(mContext);
				BackupDbAdapter dbAdapter = new BackupDbAdapter(mContext);
				dbAdapter.open();
				mResendDateList = dbAdapter.getDateList(Global.gRouteNumber);
				dbAdapter.close();
				new Handler(Looper.getMainLooper()).post(new Runnable() {
					@Override
					public void run() {
						hideProgressDialog();
						refreshPage();
					}
				});
			}
		}.start();
	}

	@Override
	public void refreshPage() {

		if (mResendDateList.size() > 0) {
			mListView.setVisibility(View.VISIBLE);
			mTxtNoData.setVisibility(View.GONE);
			mListAdapter.setListData(mResendDateList);
			mListAdapter.notifyDataSetChanged();
		} else {
			mListView.setVisibility(View.GONE);
			mTxtNoData.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void refreshLanguage() {
		((TextView) mRootView.findViewById(R.id.lblTitle)).setText(Str.VehicleInventory[Global.LANG].toUpperCase());
		((TextView) mRootView.findViewById(R.id.txtTrip)).setText(Str.Trip[Global.LANG] + " # " + Global.gTripNumber);
		mTxtNoData.setText(Str.No_data_has_been_found[Global.LANG]);
	}

	@Override
	public void receiveBroadcast(String strAction, Intent intent) {
	}

	private void goBack() {
		mContext.onBackPressed();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btnBack:
				mContext.onBackPressed();
				break;
		}
	}

	private ProgressDialog mProgressDlg = null;


	private class ListAdapter extends BaseAdapter {

		private List<String> list = new ArrayList<>();

		private class ViewHolder {
			public TextView txtShortDesc;
		}

		public int getCount() {
			return list.size();
		}

		public void setListData(List<String> list) {
			this.list.clear();
			this.list.addAll(list);
			Collections.sort(this.list, new Comparator<String>() {
				@Override
				public int compare(String t1, String t2) {
					return t1.compareTo(t2);
				}
			});
		}

		public String getItem(int position) {
			if(position < list.size())
			{
				return list.get(position);
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
			final String strDate = getItem(position);
			if(strDate == null) return null;
			if (convertView == null) {
				view = mContext.getLayoutInflater().inflate(R.layout.row_resend, parent, false);
				holder = new ViewHolder();
				holder.txtShortDesc = (TextView) view.findViewById(R.id.txtShortDesc);
				view.setTag(holder);
			} else {
				holder = (ViewHolder) view.getTag();
			}
			Date date = new Date();
			try{
				date = new SimpleDateFormat("yyyy-MM-dd").parse(strDate);
			}catch (Exception e)
			{
				e.printStackTrace();
			}
			holder.txtShortDesc.setText("Resend " + new SimpleDateFormat("dd MMMM ").format(date) + "transactions");

			view.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					doUploadTransaction(strDate);
				}
			});

			view.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View view) {
					doDeleteTransaction(strDate);
					return true;
				}
			});

			return view;
		}
	}

	private void doUploadTransaction(final String strDate)
	{
		try {
			final MessageDialog msgDialog = new MessageDialog(mContext);
			msgDialog.setCancelable(false);
			msgDialog.mStrMessage = Str.AreYouSureToUploadThisTransaction[Global.LANG];
			msgDialog.mStrLeft = Str.No[Global.LANG];
			msgDialog.mStrRight = Str.Yes[Global.LANG];
			msgDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
					if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
						processUploadTransaction(strDate);
					} else if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {

					}
				}
			});
			msgDialog.show();
		}catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void showProgressDialog(String strMsg)
	{
		try {
			if(mProgressDlg == null)
			{
				mProgressDlg = new ProgressDialog(mContext);
				mProgressDlg.setCancelable(false);
				mProgressDlg.show();
			}
			mProgressDlg.setMessage(strMsg);
		}catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void hideProgressDialog()
	{
		try {
			if(mProgressDlg != null)
			{
				mProgressDlg.dismiss();
				mProgressDlg = null;
			}
		}catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	private void processUploadTransaction(final String strDate)
	{
		showProgressDialog(Str.UploadingData[Global.LANG]);
		new Thread()
		{
			@Override
			public void run() {
				BackupDbAdapter backupDbAdapter = new BackupDbAdapter(mContext);
				backupDbAdapter.open();
				List<UploadFileInfo> uploadFileList = backupDbAdapter.getUploadFileList(strDate, Global.gRouteNumber);
				backupDbAdapter.close();
				final List<UploadFileInfo> notUploadedFileList = new ArrayList<>();
				final List<UploadFileInfo> uploadedFileList = new ArrayList<>();

				final String strFTPServerIP = Utils.getStringSetting(mContext, Global.PREF_FTP_SERVER_IP);
				final String strFTPCompanyName = Utils.getStringSetting(mContext, Global.PREF_FTP_COMPANY_NAME);
				final String strFTPUserName = Utils.getStringSetting(mContext, Global.PREF_FTP_USERNAME);

				for(UploadFileInfo fileInfo : uploadFileList)
				{
					if(new UploadViaHTTP().uploadFileToFTPServerUsingHttp(strFTPCompanyName, strFTPServerIP, strFTPUserName, fileInfo.localPath, fileInfo.serverPath))
					{
						uploadedFileList.add(fileInfo);
					}
					else if(!Utils.getStringSetting(mContext, Global.PREF_DisableFTP).equals("1") && Utils.uploadFileToFTPServer(mContext, fileInfo.localPath, fileInfo.serverPath))
					{
						uploadedFileList.add(fileInfo);
					}
					else
					{
						notUploadedFileList.add(fileInfo);
					}
				}

				if(notUploadedFileList.size() == 0)
				{
					new Handler(Looper.getMainLooper()).post(new Runnable() {
						@Override
						public void run() {
							hideProgressDialog();
							if(!mContext.isFinishing() && !mContext.isDestroyed())
							{
								try {
									Toast.makeText(mContext, String.format(Locale.ENGLISH, Str.FileUploaded[Global.LANG], uploadedFileList.size()), Toast.LENGTH_LONG).show();
								}catch (Exception e1)
								{
									e1.printStackTrace();
								}
							}

						}
					});
				}
				else
				{
					new Handler(Looper.getMainLooper()).post(new Runnable() {
						@Override
						public void run() {
							hideProgressDialog();
							if(!mContext.isFinishing() && !mContext.isDestroyed())
							{
								try {
									Toast.makeText(mContext, String.format(Locale.ENGLISH, Str.FileNotUploaded[Global.LANG], notUploadedFileList.size()), Toast.LENGTH_LONG).show();
								}catch (Exception e1)
								{
									e1.printStackTrace();
								}
							}

						}
					});
				}
			}
		}.start();
	}

	private void doDeleteTransaction(final String strDate)
	{
		try {
			final MessageDialog msgDialog = new MessageDialog(mContext);
			msgDialog.setCancelable(false);
			msgDialog.mStrMessage = Str.AreYouSureDeleteThisTransaction[Global.LANG];
			msgDialog.mStrLeft = Str.No[Global.LANG];
			msgDialog.mStrRight = Str.Yes[Global.LANG];
			msgDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
					if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
						processDeleteTransaction(strDate);
					} else if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {

					}
				}
			});
			msgDialog.show();
		}catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void processDeleteTransaction(final String strDate)
	{
		showProgressDialog(Str.DeletingData[Global.LANG]);
		new Thread(){
			@Override
			public void run() {
				Utils.deleteDirectory(Global.UPLOAD_DIR + Global.gRouteNumber + "/" + strDate);
				BackupDbAdapter backupDbAdapter = new BackupDbAdapter(mContext);
				backupDbAdapter.open();
				backupDbAdapter.deleteData(Global.gRouteNumber, strDate);
				backupDbAdapter.close();
				new Handler(Looper.getMainLooper()).post(new Runnable() {
					@Override
					public void run() {
						hideProgressDialog();
						if(!mContext.isFinishing() && !mContext.isDestroyed())
						{
							try {
								Toast.makeText(mContext, Str.DeletedSuccessfully[Global.LANG], Toast.LENGTH_LONG).show();
							}catch (Exception e1)
							{
								e1.printStackTrace();
							}
						}

					}
				});
			}
		}.start();
	}
}