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
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dsd.dsdvansales.R;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.model.CustomerInfo;
import dsd.dsdvansales.model.CustomerOrderInfo;
import dsd.dsdvansales.model.UOrderDInfo;

public class PickingNotCompletedDialog extends Dialog {

	private Context mContext;
	private List<UOrderDInfo> mUOrderDList = new ArrayList<>();
	private ListView mListView;
	ListAdapter mListAdapter;
	private Callback callback;
	public interface Callback{
		void OnOkay();
		void OnCancel();
	}

	public PickingNotCompletedDialog(Context context, List<UOrderDInfo> orderDInfoList, Callback callback) {
		super(context, android.R.style.Theme_Translucent_NoTitleBar);
		this.mContext = context;
		for(UOrderDInfo info : orderDInfoList)
		{
			int nPicked = 0;
			if(Global.pickedStatusMap.get(info.strItemNo) != null)
			{
				nPicked = Global.pickedStatusMap.get(info.strItemNo);
			}
			if(info.nSales - nPicked > 0)
			{
				mUOrderDList.add(info);
			}
		}
		this.callback = callback;
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

		setContentView(R.layout.dialog_picking_not_completed);

		((Button) findViewById(R.id.btnYes)).setText(Str.Yes[Global.LANG]);
		((Button) findViewById(R.id.btnNo)).setText(Str.No[Global.LANG]);


		findViewById(R.id.btnYes).setOnClickListener(v -> {
			callback.OnOkay();
			dismiss();
		});

		findViewById(R.id.btnNo).setOnClickListener(v -> {
			callback.OnCancel();
			dismiss();
		});

		((TextView)findViewById(R.id.txtMessage)).setText(Str.NotPickedCompleted[Global.LANG]);
		((TextView)findViewById(R.id.txtConfirmMessage)).setText(Str.SureRemoveFromOrder[Global.LANG]);

		mListView = findViewById(R.id.list);
		mListAdapter = new ListAdapter();
		mListView.setAdapter(mListAdapter);
	}

	public class ListAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mUOrderDList.size();
		}

		@Override
		public UOrderDInfo getItem(int i) {
			return mUOrderDList.get(i);
		}

		@Override
		public long getItemId(int i) {
			return i;
		}

		@Override
		public View getView(int i, View view, ViewGroup viewGroup) {
			view = getLayoutInflater().inflate(R.layout.row_pick_not_complete, viewGroup, false);

			UOrderDInfo orderInfo = getItem(i);

			((TextView)view.findViewById(R.id.txtDescription)).setText(String.valueOf(orderInfo.strShortDesc));

			int nPicked = 0;
			if(Global.pickedStatusMap.get(orderInfo.strItemNo) != null)
			{
				nPicked = Global.pickedStatusMap.get(orderInfo.strItemNo);
			}

			((TextView)view.findViewById(R.id.txtLoad)).setText(String.valueOf(orderInfo.nSales - nPicked));

			return view;
		}
	}

}