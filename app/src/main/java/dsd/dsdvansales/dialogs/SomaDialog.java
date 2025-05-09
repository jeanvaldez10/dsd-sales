package dsd.dsdvansales.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import dsd.dsdvansales.R;
import dsd.dsdvansales.db.DbAdapter;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.model.CustomerInfo;
import dsd.dsdvansales.model.DescTypeInfo;
import dsd.dsdvansales.model.ProdDetlInfo;
import dsd.dsdvansales.model.UOrderDInfo;

public class SomaDialog extends Dialog {

	private Activity mContext;
	private CustomerInfo mCustomerInfo = null;
	public ProdDetlInfo mSelProduct = null;
	private ListView mProductListView = null;
	private ProductListAdapter mProductListAdpater = null;
	private class Product{
		public ProdDetlInfo info = null;
		public String strDescription = "";
		public float percent = 0;
		public String strFrom = "";
	}

	public interface Callback{
		void onSelect(ProdDetlInfo info);
	}

	public Callback callback = null;
	public List<Product> mProductList = new ArrayList<>();

	private List<UOrderDInfo> mOrderList = null;

	public SomaDialog(Activity context, Callback callback, List<UOrderDInfo> orderList) {
		super(context, android.R.style.Theme_Translucent_NoTitleBar);
		this.mContext = context;
		this.callback = callback;
		this.mOrderList = orderList;
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

		setContentView(R.layout.dialog_soma);

		mCustomerInfo = Global.gCurCustomerInfo;

//		((TextView) findViewById(R.id.txtCustomerName)).setText(String.format(Locale.ENGLISH, Str.UpSellOpportunites[Global.LANG], mCustomerInfo.strName));
		((TextView) findViewById(R.id.txtSomaDescription)).setText(Html.fromHtml(String.format(Locale.ENGLISH, Str.SomaDesc[Global.LANG], mCustomerInfo.strName)));

		((Button) findViewById(R.id.btnReturn)).setText(Str.ReturnToSales[Global.LANG]);
		findViewById(R.id.btnReturn).setOnClickListener(v -> {
			dismiss();
		});

		mProductListView = findViewById(R.id.listProduct);
		mProductListAdpater = new ProductListAdapter();
		mProductListView.setAdapter(mProductListAdpater);

		loadData();
	}

	private void showProgressBar()
	{
		findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);
		mProductListView.setVisibility(View.INVISIBLE);
		findViewById(R.id.txtNoProduct).setVisibility(View.GONE);
	}

	private void hideProgressBar()
	{
		findViewById(R.id.progress_bar).setVisibility(View.GONE);

		if(mProductList.size() > 0)
		{
			mProductListView.setVisibility(View.VISIBLE);
			findViewById(R.id.txtNoProduct).setVisibility(View.GONE);
		}
		else {
			findViewById(R.id.txtNoProduct).setVisibility(View.VISIBLE);
			mProductListView.setVisibility(View.GONE);
		}
	}
	private void loadData()
	{
		showProgressBar();
		new Thread(){
			@Override
			public void run() {
				mProductList.clear();
				DbAdapter dbAdapter = new DbAdapter(mContext);
				dbAdapter.open();
				String strUnit = "";
				DescTypeInfo descTypeInfo = dbAdapter.getDescTypeInfo("CustomerType", mCustomerInfo.strType);
				if(descTypeInfo != null)
				{
					strUnit = descTypeInfo.strDescription;
				}
				else {
					descTypeInfo = dbAdapter.getDescTypeInfo("CustomerType", "");
					if(descTypeInfo != null)
					{
						strUnit = descTypeInfo.strDescription;
					}
				}

				List<String> orderHistoryInfoList = dbAdapter.getAllOrderHistoryItem();
				Map<String, String> purchasedHistoryMap = dbAdapter.getAllOrderHistoryItemAsMap(mCustomerInfo.nCustNo);
				Map<String, ProdDetlInfo> itemMap = new HashMap<>();
				Map<String, Product> productMap = new HashMap<>();
				dbAdapter.getProdDetlList(itemMap, null);

				int nTotalCustCount = dbAdapter.getAllCustomerCountInOrderHistory(mCustomerInfo.strType);
				Map<String, UOrderDInfo> uOrderedMap = new HashMap<>();
				if(mOrderList != null)
				{
					for(UOrderDInfo orderDInfo : mOrderList)
					{
						uOrderedMap.put(orderDInfo.strItemNo, orderDInfo);
					}
				}
				for(String strItemNo : orderHistoryInfoList)
				{
					if(productMap.get(strItemNo) != null) continue;
					if(purchasedHistoryMap.get(strItemNo) != null) continue;
					if(uOrderedMap.get(strItemNo) != null) continue;

					int nItemCustCount = dbAdapter.getCustomerCountInOrderHistory(mCustomerInfo.strType, strItemNo);
					float percent = (nItemCustCount * 1.0f / nTotalCustCount) * 100.0f;
					if(percent > 15)
					{
						Product product = new Product();
						product.info = itemMap.get(strItemNo);
						product.strDescription = String.format(Locale.ENGLISH, "%d%% of %s customers have bought this recently", (int)percent, strUnit);
						product.strFrom = "From Order History";
						product.percent = percent;
						productMap.put(strItemNo, product);
						mProductList.add(product);
					}
				}
				//Get the OrderHistory information

				// Get the Feature Group & Template Group Product
				List<Integer> authGrp = new ArrayList<>();
				if(mCustomerInfo.nTempGrp != 0)
				{
					authGrp.add(mCustomerInfo.nTempGrp);
				}
				if(mCustomerInfo.nTemplateGrp != 0)
				{
					authGrp.add(mCustomerInfo.nTemplateGrp);
				}
				if(mCustomerInfo.nFeatGrp != 0)
				{
					authGrp.add(mCustomerInfo.nFeatGrp);
				}
				if(authGrp.size() > 0)
				{
					List<ProdDetlInfo> authList = dbAdapter.getAuthDetailProduct(authGrp);
					for(ProdDetlInfo info : authList)
					{
						if(productMap.get(info.strItemNo) != null) continue;
						if(purchasedHistoryMap.get(info.strItemNo) != null) continue;

						Product product = new Product();
						product.info = info;
						product.strDescription = String.format(Locale.ENGLISH, "%d%% of %s customers have bought this recently", 0, strUnit);
						product.strFrom = "From Featured and Template Group";
						productMap.put(info.strItemNo, product);
						mProductList.add(product);
					}
				}

				Collections.sort(mProductList, (p1, p2) -> {
					if(p2.percent - p1.percent == 0) return 0;
					else if(p2.percent - p1.percent > 0) return 1;
					else return -1;
				});

				dbAdapter.close();
				new Handler(Looper.getMainLooper()).post(() -> {
					hideProgressBar();
					mProductListAdpater.notifyDataSetChanged();
				});
				new Handler(Looper.getMainLooper()).post(() -> hideProgressBar());

			}
		}.start();
	}

	private void doAddProduct(Product info)
	{
		mSelProduct = info.info;
		if(callback != null)
		{
			mProductList.remove(info);
			mProductListAdpater.notifyDataSetChanged();
			callback.onSelect(info.info);
		}
		else {
			dismiss();
		}
	}

	private class ProductListAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			return mProductList.size();
		}

		@Override
		public Product getItem(int i) {
			return mProductList.get(i);
		}

		@Override
		public long getItemId(int i) {
			return i;
		}

		@Override
		public View getView(int i, View view, ViewGroup parent) {
			if(view == null)
			{
				view = mContext.getLayoutInflater().inflate(R.layout.row_soma_product_item, parent, false);
			}

			Product product = getItem(i);
			((TextView)view.findViewById(R.id.txtItemNo)).setText(product.info.strItemNo);
			((TextView)view.findViewById(R.id.txtDescription)).setText(product.info.strDescription);
			if(!TextUtils.isEmpty(product.strDescription))
			{
				((TextView)view.findViewById(R.id.txtCalculation)).setText(product.strDescription);
			}
			else {
				((TextView)view.findViewById(R.id.txtCalculation)).setText(product.strFrom);
			}
			view.setOnClickListener(view1 -> doAddProduct(product));
			return view;
		}
	}
}