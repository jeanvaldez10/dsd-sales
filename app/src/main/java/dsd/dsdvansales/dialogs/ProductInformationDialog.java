package dsd.dsdvansales.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.wang.avi.AVLoadingIndicatorView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dsd.dsdvansales.R;
import dsd.dsdvansales.db.DbAdapter;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.model.DescTypeInfo;
import dsd.dsdvansales.model.ProdDetlInfo;
import dsd.dsdvansales.model.ProdInfo;
import dsd.dsdvansales.model.UOrderDInfo;

public class ProductInformationDialog extends Dialog {

	UOrderDInfo uOrderDInfo;
	Context mContext;
	ListView listDetailsView = null;
	ListAdapter listDetailAdapter = null;
	List<ShowItem> showItemList = new ArrayList<>();

	public class ShowItem{
		public String strTitle;
		public String strDescription;
	}

	public ProductInformationDialog(Context context, UOrderDInfo uOrderDInfo) {
		super(context, android.R.style.Theme_Translucent_NoTitleBar);
		this.uOrderDInfo = uOrderDInfo;
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

		setContentView(R.layout.dialog_product_information);

		listDetailAdapter = new ListAdapter();
		listDetailsView = findViewById(R.id.listDetails);
		listDetailsView.setAdapter(listDetailAdapter);

		File file = new File(Global.PRODUCT_DOWNLOAD_DIR + uOrderDInfo.strItemNo + ".jpg");
		if(file.exists())
		{
			Picasso.get().load(file).into((ImageView) findViewById(R.id.image));
		}
		else
		{
			//Get the image url data....
			DbAdapter dbAdapter = new DbAdapter(mContext);
			dbAdapter.open();
			ProdDetlInfo prodDetlInfo = dbAdapter.getProdDetl(uOrderDInfo.strItemNo);
			dbAdapter.close();
			String strImageUrl = "";
			if(prodDetlInfo != null && !TextUtils.isEmpty(prodDetlInfo.strImageUrl))
			{
				strImageUrl = prodDetlInfo.strImageUrl;
			}

			if(!TextUtils.isEmpty(strImageUrl))
			{
				if(Utils.isConnectingToInternet(mContext))
				{
					Picasso.get().load(strImageUrl).into((ImageView) findViewById(R.id.image));
				}
				else
				{
					//No Image
					((ImageView) findViewById(R.id.image)).setImageDrawable(mContext.getResources().getDrawable(R.drawable.icon_no_product_photo));
				}
			}
			else
			{
				//No Image
				((ImageView) findViewById(R.id.image)).setImageDrawable(mContext.getResources().getDrawable(R.drawable.icon_no_product_photo));
			}
		}

		((TextView)findViewById(R.id.txtItemNo)).setText(uOrderDInfo.strItemNo);
		((TextView)findViewById(R.id.txtDescription)).setText(uOrderDInfo.strDescription);

		((AVLoadingIndicatorView)findViewById(R.id.avi)).show();
		new Thread()
		{
			@Override
			public void run() {

				Map<String, DescTypeInfo> descTypeInfoMap = new HashMap<>();
				DbAdapter dbAdapter = new DbAdapter(mContext);
				dbAdapter.open();
				List<ProdInfo> prodInfoList = dbAdapter.getProdInfo(uOrderDInfo.strItemNo);
				showItemList.clear();
				for(ProdInfo prodInfo : prodInfoList)
				{
					ShowItem item = new ShowItem();

					DescTypeInfo descTypeInfo = descTypeInfoMap.get(prodInfo.strInfoType);
					if(descTypeInfo == null)
					{
						descTypeInfo = dbAdapter.getDescTypeInfo("ProductInfoType", prodInfo.strInfoType);
						descTypeInfoMap.put(prodInfo.strInfoType, descTypeInfo);
					}

					if(descTypeInfo != null)
					{
						item.strTitle = descTypeInfo.strDescription;
					}
					else
					{
						item.strTitle = "";
					}
					item.strDescription = prodInfo.strInfoDetail;
					showItemList.add(item);
				}
				dbAdapter.close();

				new Handler(Looper.getMainLooper()).post(() -> {
					listDetailAdapter.notifyDataSetChanged();
					((AVLoadingIndicatorView)findViewById(R.id.avi)).hide();

				});
			}
		}.start();

		findViewById(R.id.btnRight).setOnClickListener(v -> dismiss());
	}

	public class ListAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			return showItemList.size();
		}

		@Override
		public ShowItem getItem(int position) {
			return showItemList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View view, ViewGroup parent) {
			view = getLayoutInflater().inflate(R.layout.item_product_info, parent, false);
			ShowItem item = getItem(position);
			if(TextUtils.isEmpty(item.strTitle))
			{
				view.findViewById(R.id.txtTitle).setVisibility(View.GONE);
			}
			else
			{
				view.findViewById(R.id.txtTitle).setVisibility(View.VISIBLE);
				((TextView)view.findViewById(R.id.txtTitle)).setText(item.strTitle);
			}

			((TextView)view.findViewById(R.id.txtDescription)).setText(item.strDescription);
			view.setOnClickListener(null);
			return view;
		}
	}
}

