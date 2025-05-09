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
import java.util.Locale;
import java.util.Map;

import dsd.dsdvansales.R;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.model.CustomerInfo;
import dsd.dsdvansales.model.CustomerOrderInfo;

public class NewOrdersFoundDialog extends Dialog {

	private Context mContext;
	private List<CustomerOrderInfo> customerOrderInfoList = new ArrayList<>();
	private Map<CustomerOrderInfo, Boolean> selMap = new HashMap<>();
	private Map<String, CustomerInfo> customerInfoMap;

	private ListView mListView;
	ListAdapter mListAdapter;
	private Callback callback;
	public interface Callback{
		void onSelect(List<CustomerOrderInfo> customerOrderInfoList, List<CustomerOrderInfo> unSelOrderInfoList);
	}

	public NewOrdersFoundDialog(Context context, List<CustomerOrderInfo> customerOrderInfoList, Map<String, CustomerInfo> customerInfoMap, Callback callback) {
		super(context, android.R.style.Theme_Translucent_NoTitleBar);
		this.mContext = context;
		this.customerOrderInfoList.clear();
		this.customerOrderInfoList.addAll(customerOrderInfoList);
		//Sort the Customer Order Info List
		Collections.sort(this.customerOrderInfoList, (t1, t2) -> {
			if(t1.customerNumber != t2.customerNumber)
			{
				return t1.customerNumber - t2.customerNumber;
			}
			else if(t1.chainNumber != t2.chainNumber)
			{
				return t1.chainNumber - t2.chainNumber;
			}
			return 0;
		});
		this.callback = callback;
		this.customerInfoMap = customerInfoMap;
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

		setContentView(R.layout.dialog_new_orders_found);

		((Button) findViewById(R.id.btnSelect)).setText(Str.Select[Global.LANG]);


		findViewById(R.id.btnSelect).setOnClickListener(v -> {
			List<CustomerOrderInfo> selectCustomerList = new ArrayList<>();
			List<CustomerOrderInfo> unSelectCustomerList = new ArrayList<>();
			for(CustomerOrderInfo customerOrderInfo : customerOrderInfoList)
			{
				if(selMap.get(customerOrderInfo) != null && selMap.get(customerOrderInfo))
				{
					selectCustomerList.add(customerOrderInfo);
				}
				else
				{
					unSelectCustomerList.add(customerOrderInfo);
				}
			}

			if(callback != null)
			{
				callback.onSelect(selectCustomerList, unSelectCustomerList);
			}
			dismiss();
		});

		((TextView)findViewById(R.id.txtTitle)).setText(Str.NewOrdersFound[Global.LANG]);
		if(customerOrderInfoList.size() == 1)
		{
			((TextView)findViewById(R.id.txtMessage)).setText(String.format(Locale.ENGLISH, Str.NewOrderFoundMessage[Global.LANG], customerOrderInfoList.size(), "order"));
		}
		else
		{
			((TextView)findViewById(R.id.txtMessage)).setText(String.format(Locale.ENGLISH, Str.NewOrderFoundMessage[Global.LANG], customerOrderInfoList.size(), "orders"));
		}

		((Button)findViewById(R.id.btnSelect)).setText(Str.Select[Global.LANG]);

		mListView = findViewById(R.id.list);
		mListAdapter = new ListAdapter();
		mListView.setAdapter(mListAdapter);
	}

	public class ListAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return customerOrderInfoList.size();
		}

		@Override
		public CustomerOrderInfo getItem(int i) {
			return customerOrderInfoList.get(i);
		}

		@Override
		public long getItemId(int i) {
			return i;
		}

		@Override
		public View getView(int i, View view, ViewGroup viewGroup) {
			view = getLayoutInflater().inflate(R.layout.row_order, viewGroup, false);

			CustomerOrderInfo orderInfo = getItem(i);
			if(i == 0 || (orderInfo.customerNumber != getItem(i - 1).customerNumber) || (orderInfo.chainNumber != getItem(i - 1).chainNumber))
			{
				view.findViewById(R.id.txtCustomerName).setVisibility(View.VISIBLE);
				CustomerInfo customerInfo = customerInfoMap.get(orderInfo.customerNumber + "&&" + orderInfo.chainNumber);
				((TextView)view.findViewById(R.id.txtCustomerName)).setText(customerInfo.strName);
			}
			else {
				view.findViewById(R.id.txtCustomerName).setVisibility(View.GONE);
			}


			if(selMap.get(orderInfo) != null && selMap.get(orderInfo))
			{
				((CheckBox)view.findViewById(R.id.checkbox)).setChecked(true);
			}
			else
			{
				((CheckBox)view.findViewById(R.id.checkbox)).setChecked(false);
			}

			((CheckBox)view.findViewById(R.id.checkbox)).setOnCheckedChangeListener((compoundButton, b) -> selMap.put(orderInfo, b));

			view.findViewById(R.id.txtCustomerName).setOnClickListener(view1 -> {
				PresoldDialog presoldDialog = new PresoldDialog(mContext, null);
				presoldDialog.mCustomerOrderInfo = customerOrderInfoList.get(i);
				presoldDialog.show();
			});

			view.findViewById(R.id.layoutOrderNumber).setOnClickListener(view1 -> {
				PresoldDialog presoldDialog = new PresoldDialog(mContext, null);
				presoldDialog.mCustomerOrderInfo = customerOrderInfoList.get(i);
				presoldDialog.show();
			});

			((TextView)view.findViewById(R.id.txtOrderNumber)).setText(String.valueOf(orderInfo.orderNumber));
			((TextView)view.findViewById(R.id.txtPurchaseOrderNumber)).setText(orderInfo.purchaseOrderNumber);
			((TextView)view.findViewById(R.id.txtRequestDeliveryDate)).setText(orderInfo.requestedDeliveryDate);
			((TextView)view.findViewById(R.id.txtDeliveryInstruction)).setText(orderInfo.deliveryInstructions);
			((TextView)view.findViewById(R.id.txtQuantityOrdered)).setText(String.valueOf((int)orderInfo.totalQuantityOrdered));

			return view;
		}
	}

}