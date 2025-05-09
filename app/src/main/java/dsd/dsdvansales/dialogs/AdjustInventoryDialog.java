package dsd.dsdvansales.dialogs;

import java.util.*;
import android.app.*;
import android.content.*;
import android.os.*;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;

import dsd.dsdvansales.R;
import dsd.dsdvansales.db.DbAdapter;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.model.DescTypeInfo;
import dsd.dsdvansales.views.AlertSingleChoiceAdapter;

public class AdjustInventoryDialog extends Dialog {

	private Context mContext = null;
	private TextView mTxtReason = null;

	private List<DescTypeInfo> mReasonInList = new ArrayList<>();
	private List<DescTypeInfo> mReasonOutList = new ArrayList<>();
	private int mReasonCodeIdx = -1;

	public int mReturnCode = 0;
	public int mReasonType = 0;
	public String mStrResult = "";
	private boolean mIsFromSecurity8 = false;

	public AdjustInventoryDialog(Context context, boolean isFromSecurity8) {
		super(context, android.R.style.Theme_Translucent_NoTitleBar);
		this.mContext = context;
		this.mIsFromSecurity8 = isFromSecurity8;
	}

	public boolean m_bClicked = false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
		lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
		lpWindow.dimAmount = 0.9f;
		getWindow().setAttributes(lpWindow);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.dialog_adjust_inventory);

		mReasonInList = loadDescTypeList("TRANSINRSN");
		mReasonOutList = loadDescTypeList("TRANSOTRSN");

		filterReasonCode(mReasonInList);
		filterReasonCode(mReasonOutList);

		mTxtReason = ((TextView) findViewById(R.id.txtReason));
		mTxtReason.setText(Str.ReasonForTransfer[Global.LANG]);

		((TextView) findViewById(R.id.txtTitle)).setText(Str.AdjustInventory[Global.LANG].toUpperCase());
		((TextView) findViewById(R.id.txtAddStockIn)).setText(Str.AddStockIn[Global.LANG]);
		((TextView) findViewById(R.id.txtTakeStockOut)).setText(Str.TakeStockOut[Global.LANG]);

		if (mIsFromSecurity8) {
			findViewById(R.id.txtAddStockIn).setEnabled(false);
			findViewById(R.id.txtTakeStockOut).setEnabled(false);
			setReasonType(2);
		}

		if(!Utils.getStringSetting(mContext, Global.PREF_DisableInterTruck).equals("1"))
		{
			findViewById(R.id.txtInterTruckTransfer).setVisibility(View.VISIBLE);
			((TextView) findViewById(R.id.txtInterTruckTransfer)).setText(Str.InterTruckTransfer[Global.LANG]);
		}
		else
		{
			findViewById(R.id.txtInterTruckTransfer).setVisibility(View.GONE);
		}

		((Button) findViewById(R.id.btnReturn)).setText(Str.Return[Global.LANG]);
		((Button) findViewById(R.id.btnSelect)).setText(Str.Select[Global.LANG]);

		mTxtReason.setOnClickListener(v -> {
			if( m_bClicked ) return;
			m_bClicked = true;
			showReasonCodeList();
			m_bClicked = false;
		});
		findViewById(R.id.txtAddStockIn).setOnClickListener(v -> {
			if( m_bClicked ) return;
			m_bClicked = true;
			setReasonType(0);
			m_bClicked = false;
		});
		findViewById(R.id.txtTakeStockOut).setOnClickListener(v -> {
			if( m_bClicked ) return;
			m_bClicked = true;
			setReasonType(1);
			m_bClicked = false;
		});
		if(!Utils.getStringSetting(mContext, Global.PREF_DisableInterTruck).equals("1"))
		{
			findViewById(R.id.txtInterTruckTransfer).setOnClickListener(v -> {
				if( m_bClicked ) return;
				m_bClicked = true;
				setReasonType(2);
				m_bClicked = false;
			});
		}
		findViewById(R.id.btnReturn).setOnClickListener(v -> {
			if( m_bClicked ) return;
			m_bClicked = true;
			mReturnCode = 0;
			dismiss();
			m_bClicked = false;
		});
		findViewById(R.id.btnSelect).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if( m_bClicked ) return;
				m_bClicked = true;
				if (mReasonCodeIdx == -1 && mReasonType != 2) {
					Utils.printDebug(mContext, Str.PleaseSelectReason[Global.LANG]);
				} else {
					if (mReasonType == 0) {
						mStrResult = String.valueOf(mReasonInList.get(mReasonCodeIdx).nNumericKey);
					} else if(mReasonType == 1){
						mStrResult = String.valueOf(mReasonOutList.get(mReasonCodeIdx).nNumericKey);
					} else if(mReasonType == 2){
						mStrResult = "0";
					}
					mReturnCode = Global.DLG_RETURN_RIGHT;
					dismiss();
				}
				m_bClicked = false;
			}
		});
	}

	private void filterReasonCode(List<DescTypeInfo> reasonCodeList)
	{
		List<DescTypeInfo> delCodeList = new ArrayList<>();
		for(DescTypeInfo info : reasonCodeList)
		{
			if(TextUtils.isEmpty(info.strValue1) || info.strValue1.equals("0") ) continue;
			if(!TextUtils.isEmpty(info.strValue1) && !Utils.getStringSetting(mContext, Global.PREF_DEF_LOCN_NO).equals(info.strValue1))
			{
				delCodeList.add(info);
			}
		}
		for(DescTypeInfo info : delCodeList)
		{
			reasonCodeList.remove(info);
		}
	}

	private void setReasonType(int nType) {
		if (nType != mReasonType) {
			mReasonType = nType;
			mReasonCodeIdx = -1;
			mTxtReason.setText(Str.ReasonForTransfer[Global.LANG]);
			if (mReasonType == 0) {
				((TextView) findViewById(R.id.txtAddStockIn)).setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_check_on, 0);
				((TextView) findViewById(R.id.txtTakeStockOut)).setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_check_off, 0);
				((TextView) findViewById(R.id.txtInterTruckTransfer)).setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_check_off, 0);
				findViewById(R.id.txtReason).setVisibility(View.VISIBLE);
			} else if(mReasonType == 1) {
				((TextView) findViewById(R.id.txtAddStockIn)).setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_check_off, 0);
				((TextView) findViewById(R.id.txtTakeStockOut)).setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_check_on, 0);
				((TextView) findViewById(R.id.txtInterTruckTransfer)).setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_check_off, 0);
				findViewById(R.id.txtReason).setVisibility(View.VISIBLE);
			} else {
				((TextView) findViewById(R.id.txtInterTruckTransfer)).setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_check_on, 0);
				((TextView) findViewById(R.id.txtAddStockIn)).setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_check_off, 0);
				((TextView) findViewById(R.id.txtTakeStockOut)).setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_check_off, 0);
				findViewById(R.id.txtReason).setVisibility(View.GONE);
			}
		}
	}

	private List<DescTypeInfo> loadDescTypeList(String strDescriptionTypeID) {
		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		List<DescTypeInfo> list = dbAdapter.getDescTypeList(strDescriptionTypeID);
		dbAdapter.close();
		return list;
	}

	private void showReasonCodeList() {
		final String strArray[];
		if (mReasonType == 0) {
			if (mReasonInList.size() == 0) {
				Utils.printDebug(mContext, "No reason code list.");
				return;
			}
			strArray = new String[mReasonInList.size()];
			for (int i=0; i<mReasonInList.size(); i++) {
				strArray[i] = mReasonInList.get(i).strDescription;
			}
		} else {
			if (mReasonOutList.size() == 0) {
				Utils.printDebug(mContext, "No reason code list.");
				return;
			}
			strArray = new String[mReasonOutList.size()];
			for (int i=0; i<mReasonOutList.size(); i++) {
				strArray[i] = mReasonOutList.get(i).strDescription;
			}
		}
		final int origIdx = mReasonCodeIdx;
		final String origText = mTxtReason.getText().toString();
		try
		{
			new AlertDialog.Builder(mContext)
					.setTitle(Str.Select_Reason_code[Global.LANG])
					.setSingleChoiceItems(new AlertSingleChoiceAdapter(mContext, strArray), origIdx, (dialog, nSelected) -> {
						if( m_bClicked ) return;
						m_bClicked = true;
						mReasonCodeIdx = nSelected;
						mTxtReason.setText(strArray[nSelected]);
						m_bClicked = false;
					})
					.setPositiveButton(Str.Ok[Global.LANG], (dialog, nSelected) -> {
					})
					.setNegativeButton(Str.Cancel[Global.LANG], (dialog, nSelected) -> {
						if( m_bClicked ) return;
						m_bClicked = true;
						mReasonCodeIdx = origIdx;
						mTxtReason.setText(origText);
						m_bClicked = false;
					})
					.create()
					.show();
		}catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}