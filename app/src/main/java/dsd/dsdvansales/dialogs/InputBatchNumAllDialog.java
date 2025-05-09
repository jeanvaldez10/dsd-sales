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

import java.util.ArrayList;
import java.util.List;

import dsd.dsdvansales.R;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.model.UInvenDInfo;

public class InputBatchNumAllDialog extends Dialog {


	public Context mContext;
	public int mReturnCode = 0;
	public String mStrTitle = "";
	public String mStrDescription = "";
	public String mStrLeft = "";
	public String mStrRight = "";
	public String mStrInputed = "";
	public List<UInvenDInfo> mUInvenDList = new ArrayList<>();

	public boolean m_bClicked = false;
	public ListView mListView = null;
	public ListAdapter mListAdapter;

	public InputBatchNumAllDialog(Context context) {
		super(context, android.R.style.Theme_Translucent_NoTitleBar);
		this.mContext = context;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
		lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
		lpWindow.dimAmount = 0.9f;
		getWindow().setAttributes(lpWindow);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.dialog_input_batch_all);

		if (mStrTitle.length() > 0) {
			findViewById(R.id.txtTitle).setVisibility(View.VISIBLE);
			((TextView) findViewById(R.id.txtTitle)).setText(mStrTitle);
		} else {
			findViewById(R.id.txtTitle).setVisibility(View.GONE);
		}
		if (mStrLeft.length() > 0) {
			findViewById(R.id.btnLeft).setVisibility(View.VISIBLE);
			((Button) findViewById(R.id.btnLeft)).setText(mStrLeft);
		} else {
			findViewById(R.id.btnLeft).setVisibility(View.GONE);
		}
		if (mStrRight.length() > 0) {
			findViewById(R.id.btnRight).setVisibility(View.VISIBLE);
			((Button) findViewById(R.id.btnRight)).setText(mStrRight);
		} else {
			findViewById(R.id.btnRight).setVisibility(View.GONE);
		}

		if(!TextUtils.isEmpty(mStrDescription))
		{
			findViewById(R.id.txtDescription).setVisibility(View.VISIBLE);
		}
		else
		{
			findViewById(R.id.txtDescription).setVisibility(View.GONE);
		}


		findViewById(R.id.btnLeft).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if( m_bClicked ) return;
				m_bClicked = true;
				mReturnCode = Global.DLG_RETURN_RIGHT;
				dismiss();
				m_bClicked = false;
			}
		});

		findViewById(R.id.btnRight).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if( m_bClicked ) return;
				m_bClicked = true;
				mReturnCode = Global.DLG_RETURN_RIGHT;
				for(UInvenDInfo info : mUInvenDList)
				{
					info.strBatchCode = "";
				}
				dismiss();
				m_bClicked = false;
			}
		});

		mListView = findViewById(R.id.list);
		mListAdapter = new ListAdapter();
		mListView.setAdapter(mListAdapter);
	}
	public class ListAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			return mUInvenDList.size();
		}

		@Override
		public UInvenDInfo getItem(int i) {
			return mUInvenDList.get(i);
		}

		@Override
		public long getItemId(int i) {
			return i;
		}

		@Override
		public View getView(int i, View view, ViewGroup viewGroup) {
			view = getLayoutInflater().inflate(R.layout.row_batch_item, viewGroup, false);
			final UInvenDInfo info = getItem(i);
			((TextView)view.findViewById(R.id.txtItemNo)).setText(info.strItemNo);
			((TextView)view.findViewById(R.id.txtItemDescription)).setText(info.strShortDesc);
			((EditText)view.findViewById(R.id.editInput)).setText(info.strBatchCode);
			((EditText)view.findViewById(R.id.editInput)).addTextChangedListener(new TextWatcher() {
				@Override
				public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

				}

				@Override
				public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

				}

				@Override
				public void afterTextChanged(Editable editable) {
					info.strBatchCode = editable.toString();
				}
			});
			return view;
		}
	}
}

