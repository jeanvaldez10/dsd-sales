package dsd.dsdvansales.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import dsd.dsdvansales.R;
import dsd.dsdvansales.activities.MainActivity;
import dsd.dsdvansales.db.DbAdapter;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;
import dsd.dsdvansales.global.Utils;
import dsd.dsdvansales.model.DescTypeInfo;
import dsd.dsdvansales.model.LocationInfo;
import dsd.dsdvansales.model.MakeZipFileInfo;
import dsd.dsdvansales.model.TransactionInfo;
import dsd.dsdvansales.model.UTransactionsInfo;
import dsd.dsdvansales.views.AlertSingleChoiceAdapter;

public class OptimizationDialog extends Dialog {


	public interface Callback{
		void onSelect(LocationInfo startLocation, LocationInfo endLocation);
	}

	private MainActivity mContext = null;
	private TextView mTxtStartLocation;
	private TextView mTxtEndLocation;
	private Callback callback;
	private LocationInfo startLocation = null;
	private LocationInfo endLocation = null;

	private List<LocationInfo> startLocationList = new ArrayList<>();
	private List<LocationInfo> endLocationList = new ArrayList<>();

	private int mSelStartLocationIdx, mSelEndLocationIdx;
	public OptimizationDialog(MainActivity context, Callback callback) {
		super(context, android.R.style.Theme_Translucent_NoTitleBar);
		this.mContext = context;
		this.callback = callback;
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

		setContentView(R.layout.dialog_optimization);

		mTxtStartLocation = (TextView)findViewById(R.id.txtStartLocation);
		mTxtEndLocation = (TextView)findViewById(R.id.txtEndLocation);

		((TextView)findViewById(R.id.lblStartLocation)).setText(Str.StartingLocation[Global.LANG]);
		((TextView)findViewById(R.id.lblEndLocation)).setText(Str.EndingLocation[Global.LANG]);
		((TextView)findViewById(R.id.txtTitle)).setText(Str.OptimisationOptions[Global.LANG]);

		((Button)findViewById(R.id.btnReturn)).setText(Str.Return[Global.LANG]);
		((Button)findViewById(R.id.btnOptimise)).setText(Str.Optimise[Global.LANG]);

		mTxtStartLocation.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				showStartLocationList();
			}
		});

		mTxtEndLocation.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				showEndLocationList();
			}
		});

		findViewById(R.id.btnReturn).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				dismiss();
			}
		});

		findViewById(R.id.btnOptimise).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if(startLocation == null || endLocation == null)
				{
					try
					{
						Toast.makeText(mContext, Str.SelectLocation[Global.LANG], Toast.LENGTH_LONG).show();
					}catch (Exception e)
					{
						e.printStackTrace();
					}
					return;
				}
				callback.onSelect(startLocation, endLocation);
				dismiss();
			}
		});

		if(ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
				ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
		{
			loadData(null);
			return;
		}

		LocationServices.getFusedLocationProviderClient(mContext).getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
			@Override
			public void onComplete(@NonNull Task<Location> task) {
				if (task.isSuccessful() && task.getResult() != null) {
					Location location = task.getResult();
					loadData(location);
				} else {
					loadData(null);
				}
			}
		});


	}

	private void loadData(Location location) {

		if(location != null)
		{
			Utils.setDoubleSetting(mContext, Global.PREF_MY_LATITUDE, location.getLatitude());
			Utils.setDoubleSetting(mContext, Global.PREF_MY_LONGITUDE, location.getLongitude());
		}
		else
		{
			Utils.setDoubleSetting(mContext, Global.PREF_MY_LATITUDE, 0);
			Utils.setDoubleSetting(mContext, Global.PREF_MY_LONGITUDE, 0);
		}

		DbAdapter dbAdapter = new DbAdapter(mContext);
		dbAdapter.open();
		int nDefLocNo = Utils.parseIntFromString(Utils.getStringSetting(mContext, Global.PREF_DEF_LOCN_NO));
		List<DescTypeInfo> descTypeInfoList = dbAdapter.getDescTypeList("DistributionCentre");
		List<DescTypeInfo> currentDescTypeList = dbAdapter.getDescTypeListWithNumericKey("DistributionCentre",
				nDefLocNo);
		dbAdapter.close();

		LocationInfo locationInfo = new LocationInfo();
		locationInfo.locationName = Str.CurrentLocation[Global.LANG];
		locationInfo.latitude = Utils.getDoubleSetting(mContext, Global.PREF_MY_LATITUDE);
		locationInfo.longitude = Utils.getDoubleSetting(mContext, Global.PREF_MY_LONGITUDE);

		startLocationList.add(locationInfo);

		if (currentDescTypeList.size() > 0) {
			locationInfo = new LocationInfo();
			locationInfo.locationName = currentDescTypeList.get(0).strDescription;
			locationInfo.latitude = Utils.parseDoubleFromString(currentDescTypeList.get(0).strValue1);
			locationInfo.longitude = Utils.parseDoubleFromString(currentDescTypeList.get(0).strValue2);

			endLocationList.add(locationInfo);
		}

		for (DescTypeInfo descTypeInfo : descTypeInfoList) {
			locationInfo = new LocationInfo();
			locationInfo.locationName = descTypeInfo.strDescription;
			locationInfo.latitude = Utils.parseDoubleFromString(descTypeInfo.strValue1);
			locationInfo.longitude = Utils.parseDoubleFromString(descTypeInfo.strValue2);
			startLocationList.add(locationInfo);

			if (descTypeInfo.nNumericKey != nDefLocNo) {
				endLocationList.add(locationInfo);
			}
		}

		mSelStartLocationIdx = 0;
		mSelEndLocationIdx = 0;
		if (startLocationList.size() == 0 || endLocationList.size() == 0)
		{
			try
			{
				Toast.makeText(mContext, Str.NO_LOCATION[Global.LANG], Toast.LENGTH_LONG).show();
			}catch (Exception e)
			{
				e.printStackTrace();
			}
			dismiss();
			return;
		}

		startLocation = startLocationList.get(0);
		endLocation = endLocationList.get(0);

		refreshLocationView();

	}

	private void refreshLocationView()
	{
		if(startLocation != null)
		{
			mTxtStartLocation.setText(startLocation.locationName);
		}
		else
		{
			mTxtStartLocation.setText("No Location");
		}

		if(endLocation != null)
		{
			mTxtEndLocation.setText(endLocation.locationName);
		}
		else
		{
			mTxtEndLocation.setText("No Location");
		}
	}

	private void showStartLocationList()
	{
		if(startLocationList.size() == 0)
		{
			try
			{
				Toast.makeText(mContext, Str.NoLocation[Global.LANG], Toast.LENGTH_LONG).show();
			}catch (Exception e)
			{
				e.printStackTrace();
			}
			return;
		}
		final String strArray[] = new String[startLocationList.size()];
		for(int i = 0; i < startLocationList.size(); i++){
			strArray[i] = startLocationList.get(i).locationName;
		}

		final int origIdx = mSelStartLocationIdx;
		final String origText = mTxtStartLocation.getText().toString();
		final LocationInfo locationInfo = startLocation;

		try
		{
			new AlertDialog.Builder(mContext)
					.setTitle(Str.SelectStartLocation[Global.LANG])
					.setSingleChoiceItems(new AlertSingleChoiceAdapter(mContext, strArray), origIdx, (dialog, nSelected) -> {
						mSelStartLocationIdx = nSelected;
						startLocation = startLocationList.get(nSelected);
						mTxtStartLocation.setText(strArray[nSelected].replace("\n", " "));
					})
					.setPositiveButton(Str.Ok[Global.LANG], (dialog, nSelected) -> {
					})
					.setNegativeButton(Str.Cancel[Global.LANG], (dialog, nSelected) -> {
						mSelStartLocationIdx = origIdx;
						startLocation = locationInfo;
						mTxtStartLocation.setText(origText);
					})
					.create()
					.show();
		}catch (Exception e)
		{
			e.printStackTrace();
		}


	}

	private void showEndLocationList()
	{
		if(endLocationList.size() == 0)
		{
			try
			{
				Toast.makeText(mContext, Str.NoLocation[Global.LANG], Toast.LENGTH_LONG).show();
			}catch (Exception e)
			{
				e.printStackTrace();
			}
			return;
		}
		final String strArray[] = new String[endLocationList.size()];
		for(int i = 0; i < endLocationList.size(); i++){
			strArray[i] = endLocationList.get(i).locationName;
		}

		final int origIdx = mSelEndLocationIdx;
		final String origText = mTxtEndLocation.getText().toString();
		final LocationInfo locationInfo = endLocation;

		try
		{
			new AlertDialog.Builder(mContext)
					.setTitle(Str.SelectEndLocation[Global.LANG])
					.setSingleChoiceItems(new AlertSingleChoiceAdapter(mContext, strArray), origIdx, (dialog, nSelected) -> {
						mSelEndLocationIdx = nSelected;
						endLocation = endLocationList.get(nSelected);
						mTxtEndLocation.setText(strArray[nSelected]);
					})
					.setPositiveButton(Str.Ok[Global.LANG], (dialog, nSelected) -> {
					})
					.setNegativeButton(Str.Cancel[Global.LANG], (dialog, nSelected) -> {
						mSelEndLocationIdx = origIdx;
						endLocation = locationInfo;
						mTxtEndLocation.setText(origText);
					})
					.create()
					.show();
		}catch (Exception e)
		{
			e.printStackTrace();
		}

	}

}