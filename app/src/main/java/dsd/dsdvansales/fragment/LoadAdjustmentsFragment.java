package dsd.dsdvansales.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.measite.minidns.record.A;
import dsd.dsdvansales.R;
import dsd.dsdvansales.db.DbAdapter;
import dsd.dsdvansales.dialogs.InputDialog;
import dsd.dsdvansales.dialogs.MessageDialog;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.model.DescTypeInfo;
import dsd.dsdvansales.model.ProdLocnInfo;
import dsd.dsdvansales.model.UFileTransactionInfo;
import dsd.dsdvansales.model.UInvenDInfo;
import dsd.dsdvansales.views.AlertSingleChoiceAdapter;

public class LoadAdjustmentsFragment extends BaseFragment implements View.OnClickListener {

	private class LoadAdjustmentReason {
		public UInvenDInfo uInvenDInfo;
		public int nReasonIdx = -1;
		public String strValue1 = "";
		public String strPhotoPath = "";
		public int nCartons = 0;
		public int nUnits = 0;
		public double dQty = 0;
	}

	private class AdjustmentItem{
		String strItem;
		String strDesc;
		String strQty;
	}

	private int nSelectedCameraFileIndex = 0;
	private TextView mTxtNoData = null;
	private ListView mListView = null;
	private ListAdapter mListAdapter = null;
	private List<LoadAdjustmentReason> mIncompleteList = new ArrayList<>();
	private List<DescTypeInfo> mDescTypeList = new ArrayList<>();
	private boolean bShowCase = true;
	private boolean bPassedApprovalKey = false;

	private boolean bApplyAll = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootView = inflater.inflate(R.layout.fragment_load_adjustments, container, false);
		commonInit();
		return mRootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		Global.gFragIdx = Global.FRAG_LOAD_ADJUSTMENT;
	}

	@Override
	public void initView() {
		bShowCase = Utils.getStringSetting(mContext, Global.PREF_INVENTORYUOM).equals("U") == false;
		mRootView.findViewById(R.id.btnReturn).setOnClickListener(this);
		mRootView.findViewById(R.id.btnProceed).setOnClickListener(this);
		mRootView.findViewById(R.id.btnKey).setOnClickListener(this);

		mRootView.findViewById(R.id.btnKey).setVisibility(View.INVISIBLE);

		mTxtNoData = (TextView) mRootView.findViewById(R.id.txtNoData);
		mListView = (ListView) mRootView.findViewById(R.id.listView);
		mListAdapter = new ListAdapter();
		mListView.setAdapter(mListAdapter);

		Global.strLoadPdfFileName = "";

		loadData();
	}
	
	

	@Override
	public void loadData() {
		mIncompleteList.clear();
		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		mDescTypeList = dbAdapter.getDescTypeList("LOADAJUST");
		List<UInvenDInfo> uInvenDInfo = Global.gUInvenHinfo.truckList;
		for (UInvenDInfo info : uInvenDInfo) {
			if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
			{
				if (info.dLoaded == info.dStock) {
					continue;
				}
			}
			else
			{
				if (info.nLoaded == info.nStock) {
					continue;
				}
			}

			LoadAdjustmentReason loadAdjustmentReason = new LoadAdjustmentReason();
			loadAdjustmentReason.nReasonIdx = -1;
			loadAdjustmentReason.uInvenDInfo = info;

			if( loadAdjustmentReason.uInvenDInfo.nReasonCode >= 1 )
			{
				loadAdjustmentReason.nReasonIdx = loadAdjustmentReason.uInvenDInfo.nReasonCode - 1;
			}

			if(Utils.getProdLoconSCWType(mContext, info.strItemNo) == Global.WEIGHT_ITEM)
			{
				loadAdjustmentReason.dQty = info.dStock - info.dLoaded;
			}
			else
			{
				if(bShowCase)
				{
					if (Utils.isContainerItem(mContext, info.strItemNo)) {
						loadAdjustmentReason.nUnits = info.nStock - info.nLoaded;
					}
					else {
						ProdLocnInfo prodLocnInfo = dbAdapter.getProdLocnInfo(info.strItemNo);
						int nCase = 1;
						if( prodLocnInfo != null )
						{
							nCase = prodLocnInfo.nCaseFactor;
						}
						if( nCase < 1 ) nCase = 1;

						int nPrs = info.nStock - info.nLoaded;
//			if( nPrs < 0 ) nPrs *= -1;
						loadAdjustmentReason.nCartons = nPrs / nCase;
						loadAdjustmentReason.nUnits = nPrs % nCase;
					}
				}
				else
				{
					loadAdjustmentReason.nUnits = info.nStock - info.nLoaded;
				}

			}

			mIncompleteList.add(loadAdjustmentReason);
		}
		dbAdapter.close();

		refreshLanguage();
	}

	@Override
	public void refreshPage() {
		if (mIncompleteList.size() > 0) {
			mListView.setVisibility(View.VISIBLE);
			mTxtNoData.setVisibility(View.GONE);
			mListAdapter.notifyDataSetChanged();
		} else {
			mListView.setVisibility(View.GONE);
			mTxtNoData.setVisibility(View.VISIBLE);
		}
		boolean bEnabled = true;
		for (LoadAdjustmentReason info : mIncompleteList) {
			if (info.nReasonIdx == -1) {
				bEnabled = false;
				break;
			}
		}
		String strApprovalKey = Utils.getStringSetting(mContext, Global.PREF_LOADADJUSTMENT_APPROVAL_KEY);
		if (strApprovalKey.length() > 0) {
			if (bPassedApprovalKey) {
				mRootView.findViewById(R.id.btnKey).setVisibility(View.INVISIBLE);
			}
			else {
				mRootView.findViewById(R.id.btnKey).setVisibility(View.VISIBLE);
			}
			mRootView.findViewById(R.id.btnProceed).setEnabled(bEnabled && bPassedApprovalKey);
		}
		else {
			mRootView.findViewById(R.id.btnProceed).setEnabled(bEnabled);
		}
	}

	@Override
	public void refreshLanguage() {
		((TextView) mRootView.findViewById(R.id.lblTitle)).setText(Str.LoadAdjustments[Global.LANG].toUpperCase());
		((TextView) mRootView.findViewById(R.id.lblDescription)).setText(Str.LoadAdjustmentsCaption[Global.LANG]);
		((Button) mRootView.findViewById(R.id.btnReturn)).setText(Str.Return[Global.LANG]);
		((Button) mRootView.findViewById(R.id.btnProceed)).setText(Str.Proceed[Global.LANG]);
		mTxtNoData.setText(Str.No_data_has_been_found[Global.LANG]);
		refreshPage();
	}

	@Override
	public void receiveBroadcast(String strAction, Intent intent) {
		if (strAction.equals(Global.ACTION_TAKE_PHOTO) == true) {
			String strPhotoPath = intent.getStringExtra(Global.EXTRA_PHOTO_PATH);
			if(!TextUtils.isEmpty(strPhotoPath))
			{
				String strNewPhotoPath = Global.UPLOAD_DIR + System.currentTimeMillis() + ".jpg";
				Utils.fileCopy(strPhotoPath, strNewPhotoPath);
				Utils.fileDelete(strPhotoPath);
				mIncompleteList.get(nSelectedCameraFileIndex).strPhotoPath = strNewPhotoPath;
			}
		}
	}

	private void showReasonCodeList(final int nPosition) {
		final String strArray[] = new String[mDescTypeList.size()];
		for (int i=0; i<mDescTypeList.size(); i++) {
			strArray[i] = mDescTypeList.get(i).strDescription;
		}
		final int nOrigIdx = mIncompleteList.get(nPosition).nReasonIdx;

		try {
			new AlertDialog.Builder(mContext)
			.setTitle(Str.Select_Reason_code[Global.LANG])
			.setSingleChoiceItems(new AlertSingleChoiceAdapter(mContext, strArray), nOrigIdx, (dialog, nSelected) -> {
				mIncompleteList.get(nPosition).nReasonIdx = nSelected;
				mIncompleteList.get(nPosition).strValue1 = mDescTypeList.get(nSelected).strValue1;
			})
			.setPositiveButton(Str.Ok[Global.LANG], (dialog, nSelected) -> {
				if(!bApplyAll)
				{
					if(mIncompleteList.size() > 1)
					{
						try {
							final MessageDialog msgDialog = new MessageDialog(mContext);
							msgDialog.setCancelable(false);
							msgDialog.mStrMessage = Str.ReasonCodeApplyAll[Global.LANG];
							msgDialog.mStrLeft = Str.No[Global.LANG];
							msgDialog.mStrRight = Str.Yes[Global.LANG];
							msgDialog.setOnDismissListener(dialog1 -> {
								if (msgDialog.mReturnCode == Global.DLG_RETURN_LEFT) {
									bApplyAll = true;
									refreshPage();
								} else if (msgDialog.mReturnCode == Global.DLG_RETURN_RIGHT) {
									bApplyAll = true;
									for(LoadAdjustmentReason reason : mIncompleteList)
									{
										reason.nReasonIdx = mIncompleteList.get(nPosition).nReasonIdx;
										reason.strValue1 = mIncompleteList.get(nPosition).strValue1;
									}
									refreshPage();
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
				refreshPage();
			})
			.setNegativeButton(Str.Cancel[Global.LANG], (dialog, nSelected) -> mIncompleteList.get(nPosition).nReasonIdx = nOrigIdx)
			.create()
			.show();
		}catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	private void processKeyButtonPressed() {
		try {
			final InputDialog inputDlg = new InputDialog(mContext);
			inputDlg.mStrTitle = Str.PIN_CODE[Global.LANG];
			inputDlg.mStrHint = Str.PIN_code_for_communication[Global.LANG];
			inputDlg.mStrLeft = Str.Enter[Global.LANG];
			inputDlg.mStrRight = Str.Return[Global.LANG];
			inputDlg.mInputType = InputType.TYPE_CLASS_NUMBER;
			inputDlg.setCancelable(false);
			inputDlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
					if (inputDlg.mReturnCode == Global.DLG_RETURN_LEFT) {
						String strApprovalKey = Utils.getStringSetting(mContext, Global.PREF_LOADADJUSTMENT_APPROVAL_KEY);
						if (strApprovalKey.equals(inputDlg.mStrInputed)) {
							bPassedApprovalKey = true;
							refreshPage();
						} else {
							try {
								final MessageDialog msgDialog = new MessageDialog(mContext);
								msgDialog.mStrTitle = Str.Alert[Global.LANG].toUpperCase();
								msgDialog.mStrMessage = Str.Invalid_code[Global.LANG];
								msgDialog.mStrMiddle = Str.Ok[Global.LANG];
								msgDialog.setCancelable(true);
								msgDialog.show();
							}catch (Exception e)
							{
								e.printStackTrace();
							}
						}
					}
				}
			});
			inputDlg.show();
		}catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void doComplete() {
		//Check Mandatory of Capture Image file
		for (LoadAdjustmentReason info : mIncompleteList) {
			if(info.strValue1.equals("Y") && TextUtils.isEmpty(info.strPhotoPath))
			{
				try {
					final MessageDialog msgDialog = new MessageDialog(mContext);
					msgDialog.setCancelable(false);
					msgDialog.mStrMessage = Str.NeedToCaptureAllImageWithCameraIcon[Global.LANG];
					msgDialog.mStrRight = Str.Ok[Global.LANG];
					msgDialog.show();
				}catch (Exception e)
				{
					e.printStackTrace();
				}
				return;
			}
		}

		Global.gLoadAdjustmentReasonList.clear();

		Map<String, List<AdjustmentItem>> itemMap = new HashMap<>();
		List<String> reasonKeyList = new ArrayList<>();

		int nIdx = 0;
		Global.gLoadAdjustmentPhotoList.clear();

		for (LoadAdjustmentReason info : mIncompleteList) {
			info.uInvenDInfo.nReasonCode = info.nReasonIdx + 1;
			if(info.nReasonIdx >= 0)
			{
				DescTypeInfo descTypeInfo = mDescTypeList.get(info.nReasonIdx);
				List<AdjustmentItem> map = itemMap.get(descTypeInfo.strDescription);
				if(map == null)
				{
					map = new ArrayList<>();
					itemMap.put(descTypeInfo.strDescription, map);
					reasonKeyList.add(descTypeInfo.strDescription);
				}
				AdjustmentItem item = new AdjustmentItem();
				item.strItem = info.uInvenDInfo.strItemNo;
				item.strDesc = info.uInvenDInfo.strShortDesc;
				if(Utils.getProdLoconSCWType(mContext, item.strItem) == Global.WEIGHT_ITEM)
				{
					item.strQty = String.format(Locale.ENGLISH, "%.2f", info.dQty);
				}
				else
				{
					if(bShowCase)
					{
						if (Utils.isContainerItem(mContext, info.uInvenDInfo.strItemNo)) {
							item.strQty = String.format(Locale.ENGLISH, "%d", info.nUnits);
						}
						else {
							item.strQty = String.format(Locale.ENGLISH, "%d/%d", info.nCartons, info.nUnits);
						}
					}
					else
					{
						item.strQty = String.format(Locale.ENGLISH, "%d", info.nUnits);
					}
				}
				map.add(item);
			}
			if(info.strValue1.equals("Y"))
			{
				Global.gLoadAdjustmentPhotoList.add(info.strPhotoPath);

				if(TextUtils.isEmpty(Global.strLoadPdfFileName))
				{
					Global.strLoadPdfFileName = getPDFFileName();
				}
				//Prepare the upload camera images.
				UFileTransactionInfo uFileTransactionInfo = new UFileTransactionInfo();
				String strRef = setUCameraData("CAM", info.strPhotoPath, "", uFileTransactionInfo, nIdx++, false);
				{
					setUImageFileTransactionInfo(0, 0,  uFileTransactionInfo.nFileTrxnNo, "", "LOADADJUST", "LA" + Global.strLoadPdfFileName.substring(0, Global.strLoadPdfFileName.length() - 4),
							uFileTransactionInfo.strFileCreateDate, uFileTransactionInfo.strFileCreateTime, strRef);
				}
			}
		}

		for(String key : reasonKeyList)
		{
			Map<String, String> item = new HashMap<>();
			item.put("title", key);
			Global.gLoadAdjustmentReasonList.add(item);

			List<AdjustmentItem> adjustmentItemList = itemMap.get(key);
			for(AdjustmentItem adjustmentItem : adjustmentItemList)
			{
				item = new HashMap<>();
				item.put("item", adjustmentItem.strItem);
				item.put("description", adjustmentItem.strDesc);
				item.put("qty", adjustmentItem.strQty);

				Global.gLoadAdjustmentReasonList.add(item);
			}
		}

		if(!TextUtils.isEmpty(Global.strLoadPdfFileName))
		{
			Utils.registerUploadFile(mContext, makeZipFileInfo);
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
			case R.id.btnReturn:
				mContext.onBackPressed();
				break;
			case R.id.btnProceed:
				doComplete();
				break;
			case R.id.btnKey:
				processKeyButtonPressed();
				break;
		}
		m_bClicked = false;
	}



	private class ListAdapter extends BaseAdapter {

		private class ViewHolder {
			public TextView txtName, txtCustNo, txtReason, txtCartons, txtUnits;
			public ImageView btnPhoto;
		}

		public int getCount() {
			return mIncompleteList.size();
		}

		public LoadAdjustmentReason getItem(int position) {
			if(position < mIncompleteList.size())
			{
				return mIncompleteList.get(position);
			}
			else
			{
				return null;
			}
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(final int position, View convertView, ViewGroup parent) {
			View view = convertView;
			final ViewHolder holder;
			final LoadAdjustmentReason info = getItem(position);
			if(info == null) return null;

			if (convertView == null) {
				view = mContext.getLayoutInflater().inflate(R.layout.row_incomplete_deliveries, parent, false);
				holder = new ViewHolder();
				holder.txtName = view.findViewById(R.id.txtName);
				holder.txtCustNo = view.findViewById(R.id.txtCustNo);
				holder.txtReason = view.findViewById(R.id.txtReason);
				holder.txtCartons = view.findViewById(R.id.txtCartons);
				holder.txtUnits =  view.findViewById(R.id.txtUnits);
				holder.btnPhoto = view.findViewById(R.id.btnPhoto);
				view.setTag(holder);
			} else {
				holder = (ViewHolder) view.getTag();
			}

			if(info.strValue1.equals("Y")) {
				holder.btnPhoto.setVisibility(View.VISIBLE);
			} else {
				holder.btnPhoto.setVisibility(View.INVISIBLE);
			}

			holder.btnPhoto.setOnClickListener(view1 -> {
				nSelectedCameraFileIndex = position;
				selectPhoto();
			});

			holder.txtName.setText(info.uInvenDInfo.strShortDesc);
			holder.txtCustNo.setText(Utils.getDisplayItemCode(mContext, info.uInvenDInfo.strItemNo));
			if (info.nReasonIdx == -1) {
				holder.txtReason.setText(Str.LoadAdjustmentreason[Global.LANG]);
			} else {
				holder.txtReason.setText(mDescTypeList.get(info.nReasonIdx).strDescription);
			}

			if(Utils.getProdLoconSCWType(mContext, info.uInvenDInfo.strItemNo) == Global.WEIGHT_ITEM)
			{
				holder.txtCartons.setVisibility(View.GONE);
				holder.txtUnits.setText(String.format(Locale.ENGLISH, "%.2f", info.dQty));
				if(info.uInvenDInfo.dStock - info.uInvenDInfo.dLoaded < 0)
				{
					holder.txtUnits.setTextColor(mContext.getResources().getColor(R.color.red_color));
				}
				else
				{
					holder.txtUnits.setTextColor(mContext.getResources().getColor(R.color.black_color));
				}
			}
			else
			{
				if(bShowCase)
				{
					holder.txtCartons.setVisibility(View.VISIBLE);
					holder.txtCartons.setText(String.format(Locale.ENGLISH, "%d %s", info.nCartons, Str.cartons[Global.LANG]));
					holder.txtUnits.setText(String.format(Locale.ENGLISH, "%d %s", info.nUnits, Str.units[Global.LANG]));
					if( info.uInvenDInfo.nStock - info.uInvenDInfo.nLoaded < 0)
					{
						holder.txtCartons.setTextColor(mContext.getResources().getColor(R.color.red_color));
						holder.txtUnits.setTextColor(mContext.getResources().getColor(R.color.red_color));
					}
					else
					{
						holder.txtCartons.setTextColor(mContext.getResources().getColor(R.color.black_color));
						holder.txtUnits.setTextColor(mContext.getResources().getColor(R.color.black_color));
					}
				}
				else
				{
					holder.txtCartons.setVisibility(View.GONE);
					holder.txtUnits.setText(String.format(Locale.ENGLISH, "%d %s", info.nUnits, Str.units[Global.LANG]));
					if( info.uInvenDInfo.nStock - info.uInvenDInfo.nLoaded < 0)
					{
						holder.txtUnits.setTextColor(mContext.getResources().getColor(R.color.red_color));
					}
					else
					{
						holder.txtUnits.setTextColor(mContext.getResources().getColor(R.color.black_color));
					}
				}

			}

			holder.txtReason.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showReasonCodeList(position);
				}
			});

			return view;
		}
	}
}