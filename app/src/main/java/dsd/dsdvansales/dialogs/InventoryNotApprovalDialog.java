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
import android.widget.TextView;

import java.util.Locale;
import java.util.Map;

import dsd.dsdvansales.R;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.model.AdjustInventoryBluetoothData;
import dsd.dsdvansales.model.InventoryData;
import dsd.dsdvansales.model.ProdDetlInfo;

public class InventoryNotApprovalDialog extends Dialog {

	private AdjustInventoryBluetoothData inventoryData;
	private Map<String, ProdDetlInfo> prodDetlInfoMap;
	private Callback callback;
	public interface Callback{
		void doReturn();
		void doTryOtherDevice();
	}

	Context mContext;
	ListView dataListView;
	ListAdapter dataListAdapter;
	public InventoryNotApprovalDialog(Context context, AdjustInventoryBluetoothData inventoryData, Map<String, ProdDetlInfo> prodDetlInfoMap, Callback callback) {
		super(context, android.R.style.Theme_Translucent_NoTitleBar);
		this.callback = callback;
		this.inventoryData = inventoryData;
		this.prodDetlInfoMap = prodDetlInfoMap;
		mContext = context;
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

		setContentView(R.layout.dialog_inventory_not_approval);

		((TextView)findViewById(R.id.txtTitle)).setText(Str.Warning[Global.LANG]);
		((TextView)findViewById(R.id.txtMessage)).setText(String.format(Locale.ENGLISH, Str.NotAvailableItemForDevice[Global.LANG], inventoryData.strUserName, inventoryData.strUserName));
		((Button)findViewById(R.id.btnReturn)).setText(Str.Return[Global.LANG]);
		((Button)findViewById(R.id.btnTryOtherDevice)).setText(Str.TryOtherDevice[Global.LANG]);
		findViewById(R.id.btnReturn).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if(callback != null)
				{
					callback.doReturn();
				}
				dismiss();
			}
		});
		findViewById(R.id.btnTryOtherDevice).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if(callback != null)
				{
					callback.doTryOtherDevice();
				}
				dismiss();
			}
		});

		dataListView = (ListView) findViewById(R.id.listItem);
		dataListAdapter = new ListAdapter();
		dataListView.setAdapter(dataListAdapter);
	}

	public class ListAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return inventoryData.uInvenDInfoList.size();
		}

		@Override
		public InventoryData getItem(int i) {
			return inventoryData.uInvenDInfoList.get(i);
		}

		@Override
		public long getItemId(int i) {
			return i;
		}

		@Override
		public View getView(int i, View view, ViewGroup viewGroup) {
			view = getLayoutInflater().inflate(R.layout.row_inventory_data, viewGroup, false);
			InventoryData data = getItem(i);
			((TextView)view.findViewById(R.id.txtItemNo)).setText(data.strItem);
			((TextView)view.findViewById(R.id.txtDescription)).setText(prodDetlInfoMap.get(data.strItem).strDescription);
			if(Utils.getProdLoconSCWType(mContext, data.strItem) == Global.WEIGHT_ITEM)
			{
				((TextView)view.findViewById(R.id.txtQty)).setText(String.format(Locale.ENGLISH, "%.2f", data.weight));
			}
			else
			{
				((TextView)view.findViewById(R.id.txtQty)).setText(String.format(Locale.ENGLISH, "%d", data.nQty));
			}
			return view;
		}
	}
}

