package dsd.dsdvansales.fragment;

import android.content.*;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.AdapterView.*;

import androidx.fragment.app.Fragment;

import com.nhaarman.listviewanimations.itemmanipulation.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import dsd.dsdvansales.R;
import dsd.dsdvansales.db.DbAdapter;
import dsd.dsdvansales.dialogs.MessageDialog;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.model.CustomerInfo;
import dsd.dsdvansales.model.DescTypeInfo;
import dsd.dsdvansales.model.PresoldOrDInfo;
import dsd.dsdvansales.model.PresoldOrHInfo;
import dsd.dsdvansales.model.UInvenDInfo;
import dsd.dsdvansales.model.UInvenHInfo;

public class LoadTruckByCustomerFragment extends BaseFragment implements View.OnClickListener {

	private DynamicListView mListView = null;
	private ListAdapter mListAdapter = null;
	private List<CustomerInfo> mCustomerList = new ArrayList<>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootView = inflater.inflate(R.layout.fragment_loadtruck_by_customer, container, false);
		commonInit();
		return mRootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		Global.gFragIdx = Global.FRAG_LOADTRUCKBYCUSTOMER;
		Global.gCurCustomerInfo = null;
	}

	@Override
	public void initView() {
		if( Utils.getIntSetting(mContext, Global.PREF_LOAD_CUSTOMER) == 2)
			mRootView.findViewById(R.id.btnFilterBy).setOnClickListener(this);
		mRootView.findViewById(R.id.btnBack).setOnClickListener(this);
		mRootView.findViewById(R.id.btnDone).setOnClickListener(this);

		mListView = (DynamicListView) mRootView.findViewById(R.id.listView);
		mListAdapter = new ListAdapter();
		mListView.setAdapter(mListAdapter);
//		mListView.enableDragAndDrop();
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				try
				{
					CustomerInfo info = mListAdapter.getItem(position); 
				
					if( info.nCompleteStatus != 1 )
						gotoCustomerDetail(info);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		});
		mListView.setOnItemLongClickListener(new OnItemLongClickListener(){

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				try
				{
					CustomerInfo info = mListAdapter.getItem(position);

					//if( info.nCompleteStatus == 1 )
					gotoCustomerDetail(info);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				return true;
			}

		});
/*		mListView.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				mListView.startDragging(position);
				return true;
			}
		});*/

		loadData();
	}

	@Override
	public void loadData() {
		if(Global.gLoadTruckCustomerList != null && Global.gLoadTruckCustomerList.size() > 0)
		{
			mCustomerList.clear();
			mCustomerList.addAll(Global.gLoadTruckCustomerList);

			if( Global.gUInvenHinfo == null )
			{
				Global.gUInvenHinfo = new UInvenHInfo();
			}
		}
		else
		{
			mCustomerList.clear();
			DbAdapter dbAdapter = new DbAdapter(mContext);
			dbAdapter.open();
			List<PresoldOrHInfo> presoldOrHInfos = dbAdapter.getPresoldOrHInfoList();
			for(PresoldOrHInfo presoldOrHInfo : presoldOrHInfos)
			{
				if(presoldOrHInfo.strType.equals("B")) continue;
				CustomerInfo info = dbAdapter.getCustomerInfo(presoldOrHInfo.nCustNo);
				if( info == null ) continue;
				info.presoldOrHInfo = new ArrayList<>();
				info.presoldOrHInfo.add(presoldOrHInfo);
				mCustomerList.add(info);
			}
			dbAdapter.close();

			if (mCustomerList.size() > 0) {
				Collections.sort(mCustomerList, new Comparator<CustomerInfo>() {
					@Override
					public int compare(CustomerInfo customerInfo, CustomerInfo t1) {
						PresoldOrHInfo tt1 = t1.presoldOrHInfo.get(0);
						PresoldOrHInfo tt2 = customerInfo.presoldOrHInfo.get(0);
						int nComp = tt1.strDeliveryDate.compareTo(tt2.strDeliveryDate);
						if( nComp == 0 )
						{
							nComp = tt1.nPeriodNo - tt2.nPeriodNo;
						}
						if( nComp == 0 )
						{
							nComp = tt1.nDeliverySequence - tt2.nDeliverySequence;
						}
						return nComp;
					}
				});
			}
			List<CustomerInfo> delList = new ArrayList<>();

			if(Utils.getIntSetting(mContext, Global.PREF_LOAD_CUSTOMER) == 2)
			{
				//Check Partial LoadTruck
				if( Global.gPartialLocationFilterList.size() > 0 )
				{
					delList.clear();
					CustomerInfo info = null;
					for(int i = 0; i < mCustomerList.size(); i++)
					{
						info = mCustomerList.get(i);
						boolean bDel = false;
						for(String strKey : Global.gPartialLocationFilterList)
						{
							if( hasKey(info, strKey) )
							{
								bDel = true;
								break;
							}
						}
						if( !bDel )
						{
							delList.add(info);
						}
					}

					for(CustomerInfo dInfo : delList)
					{
						mCustomerList.remove(dInfo);
					}
					mRootView.findViewById(R.id.txtNoData).setVisibility(View.GONE);
					mRootView.findViewById(R.id.listView).setVisibility(View.VISIBLE);


					dbAdapter.open();
					List<DescTypeInfo> mDescTypeList = new ArrayList<>();
					mDescTypeList = dbAdapter.getDescTypeList("PICKLOCN");
					dbAdapter.close();

					String pickFullName = Global.gPartialLocationFilterList.get(0);
					if(pickFullName != null)
					{
						for(DescTypeInfo dInfo : mDescTypeList)
						{
							if( dInfo.strAlphaKey.equals(pickFullName))
								pickFullName = dInfo.strDescription;
						}

					}
					((TextView)mRootView.findViewById(R.id.txtPickName)).setText(pickFullName);
				}
				else
				{
					mRootView.findViewById(R.id.txtNoData).setVisibility(View.VISIBLE);
					mRootView.findViewById(R.id.listView).setVisibility(View.GONE);
					((TextView)mRootView.findViewById(R.id.txtPickName)).setText("");
				}
			}
			else
			{
				mRootView.findViewById(R.id.txtNoData).setVisibility(View.GONE);
				mRootView.findViewById(R.id.listView).setVisibility(View.VISIBLE);
				((TextView)mRootView.findViewById(R.id.txtPickName)).setText("");
			}

			if( Global.gUInvenHinfo == null )
			{
				Date curDate = new Date();
				Global.gUInvenHinfo = new UInvenHInfo();
				Global.gUInvenHinfo.nTrxnNo = System.currentTimeMillis();
				Global.gUInvenHinfo.strTrxnDate = new SimpleDateFormat(Global.DATE_YYYYMMDD_FORMAT).format(Utils.getRoutectlTripDate(mContext, curDate));
				Global.gUInvenHinfo.strTrxnTime = new SimpleDateFormat(Global.TIME_HHMMSS_FORMAT).format(curDate);
			}
			if(Global.gLoadTruckCustomerList == null)
			{
				Global.gLoadTruckCustomerList = new ArrayList<>();
			}
			Global.gLoadTruckCustomerList.addAll(mCustomerList);
		}

		refreshPage();
		refreshLanguage();
	}

	public boolean hasKey(CustomerInfo info, String strKey)
	{
		if( info == null || strKey == null || strKey.length() == 0 ) return false;
		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		if(info.presoldOrHInfo.size() > 0 )
		{
			List<PresoldOrDInfo> preSoldInfoList = dbAdapter.getPresoldOrDList(info.presoldOrHInfo.get(0).strDetailFile);
			if( preSoldInfoList.size() > 0 )
			{
				for(PresoldOrDInfo pInfo : preSoldInfoList)
				{
					if( pInfo.strPickLocationCode.equals(strKey) )
					{
						dbAdapter.close();
						return true;
					}
				}
			}
		}

		dbAdapter.close();
		return false;
	}
	@Override
	public void refreshPage() {
		mListAdapter.notifyDataSetChanged();
	}

	@Override
	public void refreshLanguage() {
		((Button) mRootView.findViewById(R.id.btnFilterBy)).setText(Str.FilterBy[Global.LANG]);
		((TextView) mRootView.findViewById(R.id.lblTitle)).setText(Str.LoadTruck[Global.LANG].toUpperCase());
		((TextView) mRootView.findViewById(R.id.txtNoData)).setText(Str.LoadLocationContinue[Global.LANG]);
		((TextView) mRootView.findViewById(R.id.txtTrip)).setText(Str.Trip[Global.LANG] + " # " + Global.gTripNumber);
		((Button) mRootView.findViewById(R.id.btnDone)).setText(Str.Done[Global.LANG]);
	}

	@Override
	public void receiveBroadcast(String strAction, Intent intent) {
		
	}

	private void gotoCustomerDetail(CustomerInfo info) {
		Fragment fragment = new LoadTruckCustomerOrderDetailFragment();
		try{
			Bundle bundle = new Bundle();
			bundle.putString(Global.KEY_CUSTOMER_TIME, info.strStartTime1);
			if( info.presoldOrHInfo.size() > 0 )
			{
				bundle.putString(Global.KEY_DETAILFILE, info.presoldOrHInfo.get(0).strDetailFile);
			}
			bundle.putString(Global.KEY_CUSTOMER_NAME, info.strName);
			Global.gCurCustomerInfo = info;
			fragment.setArguments(bundle);
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		
		Global.gLocationFilterList = null;
		mContext.replaceFragment(fragment);
	}

	private void doDone() {
		
/*		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		for (int i=0; i<mCustomerList.size(); i++) {
			CustomerInfo info = mCustomerList.get(i);
			info.nOrderNo = i;
			dbAdapter.updateCustomerInfo(info);
		}
		dbAdapter.close();
		mContext.onBackPressed();*/
		for(CustomerInfo info : mCustomerList)
		{
			if( info.nCompleteStatus == 0 || info.nCompleteStatus == 2)
			{
				try {
					final MessageDialog msgDialog = new MessageDialog(mContext);
					msgDialog.setCancelable(false);
					msgDialog.mStrTitle = Str.Warning[Global.LANG].toUpperCase();
					msgDialog.mStrMessage = Str.CompleteWarning[Global.LANG];
					msgDialog.mStrLeft = Str.No[Global.LANG];
					msgDialog.mStrRight = Str.Yes[Global.LANG];
					msgDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
						@Override
						public void onDismiss(DialogInterface dialog) {
							if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
								gotoNext();
							}
						}
					});
					msgDialog.show();
				}catch (Exception e)
				{
					e.printStackTrace();
				}
				return;
			}
		}
		
		gotoNext();
	}
	
	private void gotoNext()
	{
		Utils.setBooleanSetting(mContext, Global.PREF_LOAD_INVENTORY_COMPLETED, true);
		boolean bLoadByCustomer = Utils.getIntSetting(mContext, Global.PREF_LOAD_CUSTOMER) == 1 || Utils.getIntSetting(mContext, Global.PREF_LOAD_CUSTOMER) == 2;  
		if(bLoadByCustomer)
		{
			//Sum Items
			if(Global.gUInvenHinfo != null && Global.gUInvenHinfo.truckList != null)
			{
				List<UInvenDInfo> newList = new ArrayList<>();
				newList.clear();
				for (UInvenDInfo info : Global.gUInvenHinfo.truckList)
				{
					if(info.bCompleted == false)	continue;
					boolean bExist = false;
					UInvenDInfo existItem = null;
					for(UInvenDInfo newInfo : newList)
					{
						if(newInfo != null && info != null && info.strItemNo.equals(newInfo.strItemNo)) 
						{
							bExist = true;
							existItem = newInfo;
							break;
						}
					}
					if( bExist && existItem != null)
					{
						existItem.nStock += info.nStock;
						existItem.nLoaded += info.nLoaded;
						existItem.loadBlotInfoList.addAll(info.loadBlotInfoList);
					}
					else
						newList.add(info);
				}
				Global.gUInvenHinfo.truckList = newList;
			}
/*			DbAdapter dbAdapter = new DbAdapter(mContext);
			dbAdapter.open();
			for (UInvenDInfo info : Global.gUInvenHinfo.truckList)
			{
				dbAdapter.updateUInvenDInfo(info);
			}
			dbAdapter.close();*/
		}
		
		if (Utils.getIntSetting(mContext, Global.PREF_LOAD_CONTAINERS) == 1) {
			mContext.replaceFragment(new LoadContainersFragment());
		} else {
			mContext.replaceFragment(new LoadTruckSignFragment());
		}
	}
	public boolean m_bClicked = false;
	@Override
	public void onClick(View v) {
		if( m_bClicked ) return;
		m_bClicked = true;
		switch (v.getId()) {
			case R.id.btnBack:
				mContext.onBackPressed();
				break;
			case R.id.btnDone:
				doDone();
				break;
			case R.id.btnFilterBy:
				Global.gPartialPrevLocationFilterList.clear();
				Global.gPartialPrevLocationFilterList.addAll(Global.gPartialLocationFilterList);
				PartialCustomerFilterLocationFragment fragment = new PartialCustomerFilterLocationFragment();
				Bundle bundle = new Bundle();
				bundle.putSerializable(Global.KEY_PICKLOCN, null);
				
				fragment.setArguments(bundle);
				mContext.replaceFragment(fragment);
				break;
		}
		m_bClicked = false;
	}

	private class ListAdapter extends BaseAdapter{

		private class ViewHolder {
			public TextView txtStartTime1, txtCustomerName;
			public TextView txtQty;
			public ImageView imgCheck;
		}

		public int getCount() {
			if( mCustomerList == null || mCustomerList.size() == 0 ) return 0;
			return mCustomerList.size();
		}

		public CustomerInfo getItem(int position) {
			if( mCustomerList == null || mCustomerList.size() == 0 ) return null;
			return mCustomerList.get(position);
		}

		public long getItemId(int position) { 
			if( mCustomerList == null || mCustomerList.size() == 0 ) return 0;
			return mCustomerList.get(position).nOrderNo;
		}

		public View getView(final int position, View convertView, ViewGroup parent) {
			View view = convertView;
			final ViewHolder holder;
			final CustomerInfo info = getItem(position);

			boolean bDiff = false;
			int nDefPeriodNo = Utils.getIntSetting(mContext, Global.PREF_LOADPERDDEF);
			if( info.presoldOrHInfo != null && info.presoldOrHInfo.size() > 0 )
			{
				for(PresoldOrHInfo presoldOrHInfo : info.presoldOrHInfo)
				{
					if( presoldOrHInfo.nPeriodNo != nDefPeriodNo)
					{
						bDiff = true;
						break;
					}
				}
			}

			if (convertView == null) {
				view = mContext.getLayoutInflater().inflate(R.layout.row_loadtruck_by_customer, parent, false);
				holder = new ViewHolder();
				holder.txtStartTime1 = (TextView) view.findViewById(R.id.txtStartTime1);
				holder.txtCustomerName = (TextView) view.findViewById(R.id.txtCustomerName);
				holder.txtQty = (TextView) view.findViewById(R.id.txtQty);
				holder.imgCheck = (ImageView) view.findViewById(R.id.imgCheck);
				view.setTag(holder);
			} else {
				holder = (ViewHolder) view.getTag();
			}

			holder.txtStartTime1.setText(info.strStartTime1);
			if(bDiff)
			{
				holder.txtStartTime1.setTextColor(mContext.getResources().getColor(R.color.orange_text_color));
			}
			else
			{
				holder.txtStartTime1.setTextColor(mContext.getResources().getColor(R.color.black_color));
			}

			holder.txtStartTime1.setText(info.strStartTime1);
			holder.txtCustomerName.setText(info.strName);
			if( info.nCompleteStatus == 1 )
			{
				holder.imgCheck.setVisibility(View.VISIBLE);
				holder.txtQty.setVisibility(View.GONE);
				holder.txtCustomerName.setTextColor(mContext.getResources().getColor(R.color.green_color));
			}
			else if (info.nCompleteStatus == 0)
			{
				if( info.presoldOrHInfo.size() > 0 )
				{
					holder.txtQty.setText(info.presoldOrHInfo.get(0).strOrderNo);
					holder.imgCheck.setVisibility(View.GONE);
					holder.txtQty.setVisibility(View.VISIBLE);
					holder.txtCustomerName.setTextColor(mContext.getResources().getColor(R.color.black_color));
					holder.txtQty.setTextColor(mContext.getResources().getColor(R.color.black_color));
					if(bDiff)
					{
						holder.txtCustomerName.setTextColor(mContext.getResources().getColor(R.color.orange_text_color));
						holder.txtQty.setTextColor(mContext.getResources().getColor(R.color.orange_text_color));
					}
					else
					{
						holder.txtCustomerName.setTextColor(mContext.getResources().getColor(R.color.black_color));
						holder.txtQty.setTextColor(mContext.getResources().getColor(R.color.black_color));
					}
				}

			}
			else if( info.nCompleteStatus == 2)
			{
				if( info.presoldOrHInfo.size() > 0 )
				{
					holder.txtQty.setText(info.presoldOrHInfo.get(0).strOrderNo);
					holder.imgCheck.setVisibility(View.GONE);
					holder.txtQty.setVisibility(View.VISIBLE);
					holder.txtCustomerName.setTextColor(mContext.getResources().getColor(R.color.red_color));
					holder.txtQty.setTextColor(mContext.getResources().getColor(R.color.red_color));
				}
			}
			return view;
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		/*@Override
		public void swapItems(int positionOne, int positionTwo) {
			Collections.swap(mCustomerList, positionOne, positionTwo);
		}*/
	}
}