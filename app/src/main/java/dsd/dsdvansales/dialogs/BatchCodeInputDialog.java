package dsd.dsdvansales.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import dsd.dsdvansales.R;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;

public class BatchCodeInputDialog extends Dialog {

	public interface Callback
	{
		void onOK(String[] strBatchCodes);
	}

	private Context mContext = null;
	private Callback callback;
	private int nBatchCodeCnt = 0;
	private ListView mListView;
	private ListAdapter mListAdapter;
	private String[] strBatchCodes = null;

	private Map<String, Integer> mapBatchCodeList = null;
	public BatchCodeInputDialog(Context context, Callback callback, int nCount, String strBatchCode, Map<String, Integer> batchCodeList) {
		super(context, android.R.style.Theme_Translucent_NoTitleBar);
		this.mContext = context;
		this.callback = callback;
		this.nBatchCodeCnt = nCount;
		this.strBatchCodes = new String[nCount];
		this.mapBatchCodeList = batchCodeList;
		if(!TextUtils.isEmpty(strBatchCode))
		{
			String[] strBatchCodes = strBatchCode.split(",");
			for(int i = 0; i < strBatchCodes.length; i++)
			{
				this.strBatchCodes[i] = strBatchCodes[i].trim();
			}
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

		setContentView(R.layout.dialog_batch_code_input);

		initView();
	}

	private void initView() {
		if(mapBatchCodeList == null)
		{
			((TextView) findViewById(R.id.txtTitle)).setText(Str.LotNumber[Global.LANG].toUpperCase());
			((Button) findViewById(R.id.btnConfirm)).setText(Str.Confirm[Global.LANG]);
			((TextView) findViewById(R.id.txtMessage)).setText(Str.InputLotNumber[Global.LANG]);
		}
		else
		{
			((TextView) findViewById(R.id.txtTitle)).setText(Str.SerialNumber[Global.LANG].toUpperCase());
			((Button) findViewById(R.id.btnConfirm)).setText(Str.Confirm[Global.LANG]);
			((TextView) findViewById(R.id.txtMessage)).setText(Str.InputSerialNumber[Global.LANG]);
		}


		mListView = findViewById(R.id.list);
		mListAdapter = new ListAdapter();
		mListView.setAdapter(mListAdapter);

		findViewById(R.id.btnConfirm).setOnClickListener(view -> {

			if(mapBatchCodeList != null && mapBatchCodeList.size() > 0)
			{
				for(String strItem : strBatchCodes)
				{
					if(TextUtils.isEmpty(strItem) || TextUtils.isEmpty(strItem.trim()))
					{
						Toast.makeText(mContext, Str.input_all_serial_number[Global.LANG], Toast.LENGTH_LONG).show();
						return;
					}
					else if(mapBatchCodeList.get(strItem.trim()) == null)
					{
						String strAlert = String.format(Locale.ENGLISH, Str.batchcode_not_exist[Global.LANG], strItem.trim());
						Toast.makeText(mContext, strAlert, Toast.LENGTH_LONG).show();
						return;
					}
				}
			}

			Map<String, Boolean> batchCodeMap = new HashMap<>();
			for(String strItem : strBatchCodes)
			{
				if(TextUtils.isEmpty(strItem) || TextUtils.isEmpty(strItem.trim()))
				{
					Toast.makeText(mContext, Str.input_all_lot_number[Global.LANG], Toast.LENGTH_LONG).show();
					return;
				}
				else
				{
					batchCodeMap.put(strItem.trim(), true);
				}
			}
			callback.onOK(strBatchCodes);
			dismiss();
		});
	}

	public class ListAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			return nBatchCodeCnt;
		}

		@Override
		public String getItem(int i) {
			return strBatchCodes[i];
		}

		@Override
		public long getItemId(int i) {
			return i;
		}

		@Override
		public View getView(int i, View view, ViewGroup viewGroup) {
			view = getLayoutInflater().inflate(R.layout.item_batch_code, viewGroup, false);

			String batch_code = getItem(i);
			((EditText)view.findViewById(R.id.editBatchCode)).setText(batch_code);
			((EditText)view.findViewById(R.id.editBatchCode)).addTextChangedListener(new TextWatcher() {
				@Override
				public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

				}

				@Override
				public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

				}

				@Override
				public void afterTextChanged(Editable editable) {
					strBatchCodes[i] = editable.toString();
				}
			});
			if(i == 0 && mapBatchCodeList == null)
			{
				view.findViewById(R.id.btnCopy).setVisibility(View.VISIBLE);
				view.findViewById(R.id.btnCopy).setOnClickListener(view1 -> {
					for(int i12 = 1; i12 < nBatchCodeCnt; i12++)
					{
						strBatchCodes[i12] = strBatchCodes[0];
					}
					notifyDataSetChanged();
				});
			}
			else
			{
				view.findViewById(R.id.btnCopy).setVisibility(View.GONE);
			}
			return view;
		}
	}
}

