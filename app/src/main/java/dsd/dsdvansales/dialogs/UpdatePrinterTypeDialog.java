package dsd.dsdvansales.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import dsd.dsdvansales.R;
import dsd.dsdvansales.activities.LoginActivity;
import dsd.dsdvansales.activities.MainActivity;
import dsd.dsdvansales.db.DbAdapter;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.model.DescTypeInfo;
import dsd.dsdvansales.model.MakeZipFileInfo;
import dsd.dsdvansales.model.TransactionInfo;
import dsd.dsdvansales.model.UTransactionsInfo;
import dsd.dsdvansales.views.AlertSingleChoiceAdapter;

public class UpdatePrinterTypeDialog extends Dialog {

	private Context mContext = null;
	private TextView mTxtPrinterType = null;
	private TextView mTxtCurrentPrinterType = null;

	private List<DescTypeInfo> mDescTypeList = new ArrayList<>();

	private String mCurrentPrinterType = "";

	private int mSelPrinterTypeIdx = 0;

	public UpdatePrinterTypeDialog(Context context) {
		super(context, android.R.style.Theme_Translucent_NoTitleBar);
		this.mContext = context;
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

		setContentView(R.layout.dialog_update_printer_type);

		loadDescTypeList();
		if (mDescTypeList.size() == 0) {
			new Handler(Looper.getMainLooper()).post(new Runnable() {
				@Override
				public void run() {
					Utils.printDebug(mContext, "No Printer Type List to update.");
					dismiss();
				}
			});
			return;
		}

		mTxtPrinterType = (TextView)findViewById(R.id.txtPrinterType);
		mTxtCurrentPrinterType = (TextView)findViewById(R.id.txtCurrentPrinterType);

		((TextView)findViewById(R.id.lblCurrentPrinterType)).setText(Str.CurrentPrinterType[Global.LANG]);
		((TextView)findViewById(R.id.txtTitle)).setText(Str.UpdatePrinterType[Global.LANG]);
		((TextView)findViewById(R.id.lblChangeTo)).setText(Str.ChangeTo[Global.LANG]);

		((Button)findViewById(R.id.btnReturn)).setText(Str.Return[Global.LANG]);
		((Button)findViewById(R.id.btnUpdate)).setText(Str.Update[Global.LANG]);

		//Set Current Printer Type
		mTxtCurrentPrinterType.setText(mCurrentPrinterType);
		mTxtPrinterType.setText(mDescTypeList.get(0).strDescription);

		//Set Event
		mTxtPrinterType.setOnClickListener(view -> showPrinterTypeList());

		findViewById(R.id.btnReturn).setOnClickListener(view -> dismiss());

		findViewById(R.id.btnUpdate).setOnClickListener(view -> {
			int nPrintType = 0;
			try {
				nPrintType = Integer.parseInt(mDescTypeList.get(mSelPrinterTypeIdx).strValue1);
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
			Utils.setIntSetting(mContext, Global.PREF_PRINT_TYPE, nPrintType);
			Utils.setStringSetting(mContext, Global.PREF_PRINTER_NAME, mDescTypeList.get(mSelPrinterTypeIdx).strDescription);
			Utils.setStringSetting(mContext, Global.PREF_PRINTER_SIZE, mDescTypeList.get(mSelPrinterTypeIdx).strValue2);
			dismiss();
		});
	}

	private void loadDescTypeList() {
		String strDescriptionTypeID = Global.PRINTER_TYPE_DESC_TYPE_ID;
		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();

		List<DescTypeInfo> list = dbAdapter.getDescTypeList(strDescriptionTypeID);
		dbAdapter.close();

		int nPrintType = Utils.getIntSetting(mContext, Global.PREF_PRINT_TYPE);
		String strPrintName = Utils.getStringSetting(mContext, Global.PREF_PRINTER_NAME);
		String strPrintSize = Utils.getStringSetting(mContext, Global.PREF_PRINTER_SIZE);

		mDescTypeList.clear();
		for(DescTypeInfo descTypeInfo : list)
		{
			if (descTypeInfo.strValue1.equals(String.valueOf(nPrintType)) && descTypeInfo.strValue2.equals(strPrintSize)) {
				mCurrentPrinterType = descTypeInfo.strDescription;
				continue;
			}
			mDescTypeList.add(descTypeInfo);
		}
		Collections.sort(mDescTypeList, new Comparator<DescTypeInfo>() {
			@Override
			public int compare(DescTypeInfo descTypeInfo, DescTypeInfo t1) {
				return descTypeInfo.nNumericKey - t1.nNumericKey;
			}
		});
	}

	private void showPrinterTypeList() {
		final String strArray[] = new String[mDescTypeList.size()];
		for (int i=0; i<mDescTypeList.size(); i++) {
			strArray[i] = mDescTypeList.get(i).strDescription;
		}
		final int origIdx = mSelPrinterTypeIdx;
		final String origText = mTxtPrinterType.getText().toString();

		try
		{
			new AlertDialog.Builder(mContext)
					.setTitle(Str.SelectPrinterType[Global.LANG])
					.setSingleChoiceItems(new AlertSingleChoiceAdapter(mContext, strArray), origIdx, (dialog, nSelected) -> {
						mSelPrinterTypeIdx = nSelected;
						mTxtPrinterType.setText(strArray[nSelected]);
					})
					.setPositiveButton(Str.Ok[Global.LANG], (dialog, nSelected) -> {
					})
					.setNegativeButton(Str.Cancel[Global.LANG], (dialog, nSelected) -> {
						mSelPrinterTypeIdx = origIdx;
						mTxtPrinterType.setText(origText);
					})
					.create()
					.show();
		}catch (Exception e)
		{
			e.printStackTrace();
		}

	}
}