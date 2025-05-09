package dsd.dsdvansales.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import dsd.dsdvansales.R;
import dsd.dsdvansales.fragment.LoadTruckBulkFragment;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.model.StripeSavedCard;
import dsd.dsdvansales.model.UInvenDInfo;

public class StripeSavedCardSelectDialog extends Dialog {

	public int mReturnCode = 0;
	public String mStrMessage = "";
	public String mStrTitle = "";
	public String mStrLeft = "";
	public String mStrMiddle = "";
	public String mStrRight = "";
	public boolean mIsFailed = false;
	public String mStrCustomerName = "";
	private List<StripeSavedCard> savedCardList = new ArrayList<>();
	private ListView mSavedCardView;
	ListAdapter mSavedCardAdapter = null;
	public int mSelIdx = 0;

	public StripeSavedCardSelectDialog(Context context, List<StripeSavedCard> savedCardList) {
		super(context, android.R.style.Theme_Translucent_NoTitleBar);
		this.savedCardList.addAll(savedCardList);
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

		setContentView(R.layout.dialog_stripe_saved_card_select);

		if (mStrTitle.length() == 0) {
			findViewById(R.id.txtTitle).setVisibility(View.GONE);
		} else {
			findViewById(R.id.txtTitle).setVisibility(View.VISIBLE);
			((TextView) findViewById(R.id.txtTitle)).setText(mStrTitle);
		}
		if (mIsFailed == false) {
			findViewById(R.id.txtFailed).setVisibility(View.GONE);
		} else {
			findViewById(R.id.txtFailed).setVisibility(View.VISIBLE);
			((TextView) findViewById(R.id.txtFailed)).setText(Str.FAILED[Global.LANG]);
		}
		if (mStrCustomerName.length() > 0) {
			((TextView) findViewById(R.id.txtCustomerName)).setText(mStrCustomerName);
			findViewById(R.id.txtCustomerName).setVisibility(View.VISIBLE);
		} else {
			findViewById(R.id.txtCustomerName).setVisibility(View.GONE);
		}
		((TextView) findViewById(R.id.txtMessage)).setText(mStrMessage);

		if (mStrMiddle.length() == 0) {
			findViewById(R.id.btnMiddle).setVisibility(View.GONE);
		} else {
			findViewById(R.id.btnMiddle).setVisibility(View.VISIBLE);
			((Button) findViewById(R.id.btnMiddle)).setText(mStrMiddle);
		}
		if (mStrLeft.length() == 0) {
			findViewById(R.id.btnLeft).setVisibility(View.GONE);
		} else {
			findViewById(R.id.btnLeft).setVisibility(View.VISIBLE);
			((Button) findViewById(R.id.btnLeft)).setText(mStrLeft);
		}
		if (mStrRight.length() == 0) {
			findViewById(R.id.btnRight).setVisibility(View.GONE);
		} else {
			findViewById(R.id.btnRight).setVisibility(View.VISIBLE);
			((Button) findViewById(R.id.btnRight)).setText(mStrRight);
		}

		findViewById(R.id.btnMiddle).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mReturnCode = Global.DLG_RETURN_MIDDLE;
				dismiss();
			}
		});

		findViewById(R.id.btnLeft).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mReturnCode = Global.DLG_RETURN_LEFT;
				dismiss();
			}
		});

		findViewById(R.id.btnRight).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mReturnCode = Global.DLG_RETURN_RIGHT;
				dismiss();
			}
		});

		mSavedCardView = (ListView)findViewById(R.id.listSavedCard);
		mSavedCardAdapter = new ListAdapter();
		mSavedCardView.setAdapter(mSavedCardAdapter);
	}

	private class ListAdapter extends BaseAdapter {

		public int getCount() {
			return savedCardList.size();
		}

		public Object getItem(int position) {
			return savedCardList.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(final int position, View convertView, ViewGroup parent) {
			View view = convertView;
			final StripeSavedCard info = (StripeSavedCard)getItem(position);

			if (convertView == null) {
				view = getLayoutInflater().inflate(R.layout.row_saved_card, parent, false);

			}
			if(position == mSelIdx)
			{
				((RadioButton)view.findViewById(R.id.radioCheck)).setChecked(true);
			}
			else
			{
				((RadioButton)view.findViewById(R.id.radioCheck)).setChecked(false);
			}
			((RadioButton)view.findViewById(R.id.radioCheck)).setText(String.format(Locale.ENGLISH, Str.StripeSavedCardTitle[Global.LANG], info.strCardKind, info.strCardEnding));
			view.findViewById(R.id.radioCheck).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					mSelIdx = position;
					notifyDataSetChanged();
				}
			});

			return view;
		}
	}
}

