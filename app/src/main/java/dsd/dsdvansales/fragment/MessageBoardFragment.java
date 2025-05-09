package dsd.dsdvansales.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import dsd.dsdvansales.R;
import dsd.dsdvansales.db.DbAdapter;
import dsd.dsdvansales.dialogs.MessageDialog;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.model.MessageInfo;
import dsd.dsdvansales.model.TransactionInfo;
import dsd.dsdvansales.model.UMessagesInfo;

public class MessageBoardFragment extends BaseFragment implements View.OnClickListener {

	private TextView mTxtNoData = null;
	private ListView mListView = null;
	private ListAdapter mListAdapter = null;
	private List<MessageInfo> mMessageList = new ArrayList<>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootView = inflater.inflate(R.layout.fragment_message_board, container, false);
		commonInit();
		return mRootView;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		Global.gFragIdx = Global.FRAG_MESSAGE_BOARD;
	}

	@Override
	public void onPause() {
		super.onPause();
		doComplete();
	}

	@Override
	public void initView() {
		mRootView.findViewById(R.id.btnReturn).setOnClickListener(this);

		mTxtNoData = (TextView) mRootView.findViewById(R.id.txtNoData);
		mListView = (ListView) mRootView.findViewById(R.id.listView);
		mListAdapter = new ListAdapter();
		mListView.setAdapter(mListAdapter);

		String strCustomerName = getArguments().getString(Global.EXTRA_CUSTOMER_NAME);
		if (strCustomerName.length() > 0) {
			((TextView) mRootView.findViewById(R.id.txtWelcome)).setText(strCustomerName);
		} else {
			((TextView) mRootView.findViewById(R.id.txtWelcome)).setText("Hi " + Utils.getStringSetting(mContext, Global.PREF_USER_DESCRIPTION));
		}
		loadData();
	}

	@Override
	public void refreshDriverName() {
		String strCustomerName = getArguments().getString(Global.EXTRA_CUSTOMER_NAME);
		if (strCustomerName.length() > 0) {
			((TextView) mRootView.findViewById(R.id.txtWelcome)).setText(strCustomerName);
		} else {
			((TextView) mRootView.findViewById(R.id.txtWelcome)).setText("Hi " + Utils.getStringSetting(mContext, Global.PREF_USER_DESCRIPTION));
		}
	}

	@Override
	public void loadData() {
		int nCustNo = getArguments().getInt(Global.EXTRA_CUSTOMER_NO);
		mMessageList.clear();
		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		mMessageList = dbAdapter.getMessageList(nCustNo);
		dbAdapter.close();
		refreshPage();
		refreshLanguage();
	}

	@Override
	public void refreshPage() {
		if (mMessageList.size() > 0) {
			mListView.setVisibility(View.VISIBLE);
			mTxtNoData.setVisibility(View.GONE);
			mListAdapter.notifyDataSetChanged();
		} else {
			mListView.setVisibility(View.GONE);
			mTxtNoData.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void refreshLanguage() {
		((TextView) mRootView.findViewById(R.id.lblTitle)).setText(Str.MessageBoard[Global.LANG].toUpperCase());
		((Button) mRootView.findViewById(R.id.btnReturn)).setText(Str.Return[Global.LANG]);
		mTxtNoData.setText(Str.No_data_has_been_found[Global.LANG]);
	}

	@Override
	public void receiveBroadcast(String strAction, Intent intent) {
	}

	private void setReadMessage(int position) {
		mMessageList.get(position).bIsOldMessage = true;
		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		dbAdapter.updateMessageInfo(mMessageList.get(position));
		dbAdapter.close();
		refreshPage();
	}

	private void doDeleteMessage(int position) {
		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		dbAdapter.deleteMessageInfo(mMessageList.get(position).nIdx);
		dbAdapter.close();
		mMessageList.remove(position);
		refreshPage();
	}

	public boolean m_bCompleted = false;
	private void doComplete() {
		if( m_bCompleted ) return;
		m_bCompleted = true;
		Date curDate = new Date();

		long nTrxnNo = setGeneralUploadData("MESS", 0, 0, -1);
		TransactionInfo transactionInfo = new TransactionInfo();
		transactionInfo.strType = "MESS";
		transactionInfo.strTime = new SimpleDateFormat("kk:mm").format(new Date(nTrxnNo));
		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		dbAdapter.insertTransactionHistoryInfo(transactionInfo);
		dbAdapter.close();

		List<UMessagesInfo> uMessagesList = new ArrayList<>();
		for (MessageInfo info : mMessageList) {
			if (info.bIsOldMessage == true) {
				UMessagesInfo uMessageInfo = new UMessagesInfo();
				uMessageInfo.nTrxnNo = nTrxnNo;
				uMessageInfo.nChainNo = Global.gCurCustomerInfo.nChainNo;
				uMessageInfo.nCustNo = Global.gCurCustomerInfo.nCustNo;
				uMessageInfo.nMessageID = info.nMessageID;
				uMessageInfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(curDate);
				uMessageInfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(curDate);
				uMessagesList.add(uMessageInfo);
			}
		}
		dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		for (UMessagesInfo info : uMessagesList) {
			dbAdapter.insertUMessagesInfo(info);
		}
		dbAdapter.close();
		//Global.gUploadCustNoList.add(makeZipFileInfo);
        Utils.registerUploadFile(mContext, makeZipFileInfo);
		Global.gFragIdx = -1;
	}

	public boolean m_bClicked = false;
	@Override
	public void onClick(View v) {
		if( m_bClicked ) return;
		m_bClicked = true;
		switch (v.getId()) {
			case R.id.btnReturn:
				doComplete();
				mContext.onBackPressed();
				break;
		}
		m_bClicked = false;
	}

	private class ListAdapter extends BaseAdapter {

		private class ViewHolder {
			public TextView txtSubject, txtMessage;
			public Button btnDelete;
		}

		public int getCount() {
			return mMessageList.size();
		}

		public MessageInfo getItem(int position) {
			if(position < mMessageList.size())
			{
				return mMessageList.get(position);
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
			final MessageInfo info = getItem(position);

			if (convertView == null) {
				view = mContext.getLayoutInflater().inflate(R.layout.row_message_board, parent, false);
				holder = new ViewHolder();
				holder.txtSubject = (TextView) view.findViewById(R.id.txtSubject);
				holder.txtMessage = (TextView) view.findViewById(R.id.txtMessage);
				holder.btnDelete = (Button) view.findViewById(R.id.btnDelete);
				view.setTag(holder);
			} else {
				holder = (ViewHolder) view.getTag();
			}

			holder.txtSubject.setText(info.strSubject);
			holder.txtMessage.setText(info.strMessageText);

			if (info.bIsOldMessage == false) {
				holder.txtSubject.setTypeface(null, Typeface.BOLD);
				holder.txtMessage.setTypeface(null, Typeface.BOLD);
			} else {
				holder.txtSubject.setTypeface(null, Typeface.NORMAL);
				holder.txtMessage.setTypeface(null, Typeface.NORMAL);
			}

			holder.btnDelete.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					try {
						final MessageDialog dlg = new MessageDialog(mContext);
						dlg.mStrTitle = "";
						dlg.mStrMessage = Str.Do_you_want_to_delete_this_message[Global.LANG];
						dlg.mStrLeft = Str.No[Global.LANG];
						dlg.mStrRight = Str.Yes[Global.LANG];
						dlg.setCancelable(false);
						dlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
							@Override
							public void onDismiss(DialogInterface dialog) {
								if (dlg.mReturnCode == Global.DLG_RETURN_RIGHT) {
									doDeleteMessage(position);
								}
							}
						});
						dlg.show();
					}catch (Exception e)
					{
						e.printStackTrace();
					}

				}
			});

			view.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					setReadMessage(position);
				}
			});

			holder.txtMessage.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					try
					{
						String strHttpUrl = holder.txtMessage.getText().toString();
						Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(strHttpUrl));
						startActivity(browserIntent);
						setReadMessage(position);
					}catch(Exception e)
					{
						e.printStackTrace();
					}

				}
			});

			return view;
		}
	}
}