package dsd.dsdvansales.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dsd.dsdvansales.R;
import dsd.dsdvansales.activities.LoginActivity;
import dsd.dsdvansales.activities.MainActivity;
import dsd.dsdvansales.db.DbAdapter;
import dsd.dsdvansales.fragment.BaseFragment;
import dsd.dsdvansales.fragment.PreTripTasksFragment;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.model.LoadBlotInfo;
import dsd.dsdvansales.model.LoadDetlInfo;
import dsd.dsdvansales.model.LoadHeadInfo;
import dsd.dsdvansales.model.ProdDetlInfo;
import dsd.dsdvansales.model.UInvenDInfo;

public class LoadDataCheckPopupDialog extends Dialog {

	private String[] weekValues = {"", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
	private Context mContext = null;
	private ListView mLoadDataList;

	List<LoadHeadInfo> datas;
	public boolean m_bClicked = false;
	private BaseFragment nextScreen;

	public boolean bBackPreTrip = false;
	public boolean bShouldJustReturn = false;

	public interface Callback
	{
		public void onLoadDataSelected();
	}

	public Callback callback;

	public LoadDataCheckPopupDialog(Context context) {
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

		setContentView(R.layout.dialog_load_data_check);

		((TextView) findViewById(R.id.txtTitle)).setText(Str.Load_Date_Check[Global.LANG].toUpperCase());
		((TextView) findViewById(R.id.txtCaption)).setText(Str.Please_Select_The_Loads_Being_Loaded[Global.LANG]);
		((TextView) findViewById(R.id.txtDetail)).setText(Str.Detail[Global.LANG]);
		((TextView) findViewById(R.id.txtLoad)).setText(Str.Load[Global.LANG]);
		((TextView) findViewById(R.id.txtDelivery)).setText(Str.Sale[Global.LANG]);

		((Button) findViewById(R.id.btnReturn)).setText(Str.Return[Global.LANG]);
		((Button) findViewById(R.id.btnSelect)).setText(Str.Proceed[Global.LANG]);

		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		datas = dbAdapter.getLLoadUnProcessedHeadInfo();
		dbAdapter.close();

		mLoadDataList = (ListView)findViewById(R.id.list);
		LoadDataAdapter adapter = new LoadDataAdapter();
		mLoadDataList.setAdapter(adapter);
		findViewById(R.id.btnReturn).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if( m_bClicked ) return;
				m_bClicked = true;
				if( !bBackPreTrip )
				{
					//((MainActivity)mContext).replaceFragment(new LoginFragment());
					mContext.startActivity(new Intent(mContext, LoginActivity.class));
					((MainActivity)mContext).finish();

					Global.gSignedOn = false;
				}
				dismiss();
				m_bClicked = false;

			}
		});

		findViewById(R.id.btnSelect).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if( m_bClicked ) return;
				m_bClicked = true;
				DbAdapter dbAdapter = new DbAdapter(mContext);
				dbAdapter.open();
				boolean bApply = false;
				boolean bRLoad = false;
				for(LoadHeadInfo info : datas)
				{
					if( info.bLoadTripDataFlag) bApply = true;
					if( info.bLoadTripDataFlag && info.strType.equals("R"))
					{
						bRLoad = true;
					}
					dbAdapter.updateLoadHeadInfo(info);
				}
				dbAdapter.close();
				if( bApply )
				{
					if( bRLoad )
					{
						TaskLoadRLoad();
					}
					else
					{
						//TaskLoadRLoad();
						if (!bShouldJustReturn) {
							if( nextScreen != null )
							{
								((MainActivity)mContext).replaceFragment(nextScreen);
							}
							else
							{
								((MainActivity)mContext).replaceFragment(new PreTripTasksFragment());
							}
						}
					}
					if (callback != null) {
						callback.onLoadDataSelected();
					}
					dismiss();

					if( bRLoad )
					{
						try
						{
							MessageDialog msgDialog = new MessageDialog(mContext);
							msgDialog.setCancelable(false);
							msgDialog.mStrTitle = Str.Alert[Global.LANG].toUpperCase();
							msgDialog.mStrMessage = Str.Rolled_Inventory_Updated[Global.LANG];
							msgDialog.mStrMiddle = Str.Ok[Global.LANG];
							msgDialog.show();
							((MainActivity)mContext).replaceFragment(new PreTripTasksFragment());
						}catch (Exception e)
						{
							e.printStackTrace();
						}

					}
				}
				else
				{
					try
					{
						MessageDialog msgDialog = new MessageDialog(mContext);
						msgDialog.setCancelable(false);
						msgDialog.mStrTitle = Str.Alert[Global.LANG].toUpperCase();
						msgDialog.mStrMessage = Str.You_have_to_select_at_least_one_load_date[Global.LANG];
						msgDialog.mStrMiddle = Str.Ok[Global.LANG];
						msgDialog.show();
					}catch (Exception e)
					{
						e.printStackTrace();
					}

				}
				m_bClicked = false;

			}
		});
	}

	private void TaskLoadRLoad() {
		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		List<LoadDetlInfo> loadDetlInfoList = dbAdapter.getRLoadDetlList();
		List<UInvenDInfo> updateUInvenList = new ArrayList<>();
		long m_lTrxnNo = Utils.getLongSetting(mContext, Global.PREF_UINVEN_TRXN_NO);
		if( m_lTrxnNo == 0 )
		{
			m_lTrxnNo = System.currentTimeMillis();
			Utils.setLongSetting(mContext, Global.PREF_UINVEN_TRXN_NO, m_lTrxnNo);
		}
		if(loadDetlInfoList.size() > 0 )
		{
			List<String> strCodeList = new ArrayList<>();
			List<ProdDetlInfo> mProdDetlList = dbAdapter.getProdDetlList();
			List<UInvenDInfo> uLoadedDList = dbAdapter.getUInvenDList(Utils.getLongSetting(mContext, Global.PREF_UINVEN_TRXN_NO));

			Map<String, Map<String, List<LoadBlotInfo>>> blotMap = new HashMap<>();

			for (LoadDetlInfo loadDetlInfo : loadDetlInfoList) {
				UInvenDInfo selInfo = null;
				for(UInvenDInfo uLoadedInvenDInfo : uLoadedDList)
				{
					if(uLoadedInvenDInfo.strItemNo.equals(loadDetlInfo.strItemNo) )
					{
						selInfo = uLoadedInvenDInfo;
						break;
					}
				}
				UInvenDInfo uInvenDInfo;
				if( selInfo != null )
				{
					uInvenDInfo = selInfo;
					uInvenDInfo.nBeginQty += loadDetlInfo.nQty;
					uInvenDInfo.nStock += loadDetlInfo.nQty;

					if(uInvenDInfo.bLotControl)
					{
						Map<String, List<LoadBlotInfo>> blotDetailMap = blotMap.get(loadDetlInfo.strDetailFile);
						if(blotDetailMap == null)
						{
							blotDetailMap = new HashMap<>();
							blotMap.put(loadDetlInfo.strDetailFile, blotDetailMap);

							List<LoadBlotInfo> loadBlotInfoList = dbAdapter.getLoadBlot(loadDetlInfo.strDetailFile);

							for(LoadBlotInfo loadBlotInfo : loadBlotInfoList)
							{
								List<LoadBlotInfo> infoList = blotDetailMap.get(loadBlotInfo.strItemNo);
								if(infoList == null)
								{
									infoList = new ArrayList<>();
									blotDetailMap.put(loadBlotInfo.strItemNo, infoList);
								}
								infoList.add(loadBlotInfo);
							}
						}

						if(blotDetailMap.get(uInvenDInfo.strItemNo) != null)
						{
							uInvenDInfo.loadBlotInfoList.addAll(blotDetailMap.get(uInvenDInfo.strItemNo));
						}
					}

					dbAdapter.updateUInvenDInfo(uInvenDInfo);
				}
				else
				{
					uInvenDInfo = new UInvenDInfo();
					uInvenDInfo.strItemNo = loadDetlInfo.strItemNo;
					uInvenDInfo.strPickLocationCode = loadDetlInfo.strPickLocationCode;
					uInvenDInfo.nBeginQty = loadDetlInfo.nQty;
					uInvenDInfo.nLoaded = 0;
					uInvenDInfo.nStock = loadDetlInfo.nQty;
					uInvenDInfo.nSequence = uInvenDInfo.nIdx;
					uInvenDInfo.nSCWType = Global.INVENTORY_TRUCK;
					uInvenDInfo.bShow = true;
					for (ProdDetlInfo info : mProdDetlList) {
						if (info.strItemNo.equals(loadDetlInfo.strItemNo) == true) {
							uInvenDInfo.strShortDesc = info.strShortDesc;
							uInvenDInfo.strDescription = info.strDescription;
							uInvenDInfo.strUOM = info.prodLocnInfo.strCaseUOM.length() > 0 ? info.prodLocnInfo.strCaseUOM : info.prodLocnInfo.strUnitUOM;
							uInvenDInfo.strFullCase = info.prodLocnInfo.strFullCase;
							uInvenDInfo.nWeight = info.nWeight;
							if(Utils.getIntSetting(mContext, Global.PREF_LOAD_CUSTOMER) != 1 && info.strLotControl.equalsIgnoreCase("Y"))
							{
								uInvenDInfo.bLotControl = true;
							}
							break;
						}
					}
					if(uInvenDInfo.bLotControl)
					{
						Map<String, List<LoadBlotInfo>> blotDetailMap = blotMap.get(loadDetlInfo.strDetailFile);
						if(blotDetailMap == null)
						{
							blotDetailMap = new HashMap<>();
							blotMap.put(loadDetlInfo.strDetailFile, blotDetailMap);

							List<LoadBlotInfo> loadBlotInfoList = dbAdapter.getLoadBlot(loadDetlInfo.strDetailFile);

							for(LoadBlotInfo loadBlotInfo : loadBlotInfoList)
							{
								List<LoadBlotInfo> infoList = blotDetailMap.get(loadBlotInfo.strItemNo);
								if(infoList == null)
								{
									infoList = new ArrayList<>();
									blotDetailMap.put(loadBlotInfo.strItemNo, infoList);
								}
								infoList.add(loadBlotInfo);
							}
						}

						uInvenDInfo.loadBlotInfoList = blotDetailMap.get(uInvenDInfo.strItemNo);
					}
					uInvenDInfo.nTrxnNo = m_lTrxnNo;
					dbAdapter.insertUInvenDInfo(uInvenDInfo);
					uLoadedDList.add(uInvenDInfo);
				}

				//uInvenDInfo.bShow = uInvenDInfo.strPickLocationCode.trim().length() > 0;
				//Modified by Andrew 2016-9.11

				if (uInvenDInfo.strPickLocationCode.length() > 0) {
					boolean bExist = false;
					for (String strCode : strCodeList) {
						if (strCode.equals(uInvenDInfo.strPickLocationCode) == true) {
							bExist = true;
							break;
						}
					}
					if (bExist == false) {
						strCodeList.add(uInvenDInfo.strPickLocationCode);
					}
				}
			}
			//Set LoadCompleted Flag = 1
			//dbAdapter.updateRLoadStatusAsLoadCompleted();
		}
		dbAdapter.close();
	}

	public void setNextScreen(BaseFragment nextScreen) {
		this.nextScreen = nextScreen;
	}

	class LoadDataAdapter extends BaseAdapter
	{
		public LoadDataAdapter()
		{

		}

		private LayoutInflater inflater;
		@Override
		public int getCount() {
			return datas.size();
		}

		@Override
		public Object getItem(int position) {
			return datas.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			if (inflater == null)
				inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			if (convertView == null)
				convertView = inflater.inflate(R.layout.row_load_date, null);

			LoadHeadInfo data = datas.get(position);

			convertView.findViewById(R.id.chkDetail).setTag(data);
			convertView.findViewById(R.id.chkDetail).setOnClickListener(view -> {
				LoadHeadInfo data1 = (LoadHeadInfo)view.getTag();
				CheckBox checkBox = (CheckBox)view;
				data1.bLoadTripDataFlag = checkBox.isChecked();
				if( checkBox.isChecked() )
				{
					for(LoadHeadInfo info : datas)
					{
						if( info != data1)
						{
							info.bLoadTripDataFlag = false;
						}
					}
				}
				notifyDataSetChanged();
			});

			((CheckBox)convertView.findViewById(R.id.chkDetail)).setChecked(data.bLoadTripDataFlag);
			((TextView)convertView.findViewById(R.id.txtDetail)).setText(data.strDescription);
			((TextView)convertView.findViewById(R.id.txtLoad)).setText(ConvertFormat(data.strLoadDate));
			((TextView)convertView.findViewById(R.id.txtDay)).setText(data.nDayNo <= 7 ? weekValues[data.nDayNo] : "");
			((TextView)convertView.findViewById(R.id.txtBay)).setText(data.strBay);
			((TextView)convertView.findViewById(R.id.txtUser1)).setText(data.strUser1);
			((TextView)convertView.findViewById(R.id.txtUser2)).setText(data.strUser2);
			return convertView;
		}
	}

	public String ConvertFormat(String strDate)
	{
		try {
			SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
			SimpleDateFormat newFormat = new SimpleDateFormat(Utils.getDateFormat(mContext, "dd/MM/yy"));
			return newFormat.format(format.parse(strDate));
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		return "";
	}


}