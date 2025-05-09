package dsd.dsdvansales.dialogs;

import android.Manifest;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import dsd.dsdvansales.R;
import dsd.dsdvansales.bluetooth.BluetoothSearchService;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.global.Str;

public class DiscoveryBluetoothDeviceDialog extends Dialog {

	public interface DiscoveryCallback {
		void doReturn();

		void doSelectDevice(BluetoothDevice device);
	}

	Context mContext;

	public DiscoveryBluetoothDeviceDialog(Context context, DiscoveryCallback callback) {
		super(context, android.R.style.Theme_Translucent_NoTitleBar);
		mContext = context;
		this.callback = callback;
	}

	ListAdapter mDeviceAdapter;
	ListView mDeviceListView;
	DiscoveryCallback callback;
	List<BluetoothDevice> mDeviceList = new ArrayList<>();

	BluetoothAdapter mBluetoothAdapter;
	BluetoothLeScanner mBluetoothScanner;
	ScanCallback mScanCallback;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
		lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
		lpWindow.dimAmount = 0.9f;
		getWindow().setAttributes(lpWindow);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.dialog_discovery_bluetooth_device);

		((TextView) findViewById(R.id.txtTitle)).setText(Str.SelectDevice[Global.LANG]);
		((Button) findViewById(R.id.btnReturn)).setText(Str.Return[Global.LANG]);
		((Button) findViewById(R.id.btnSearch)).setText(Str.Search[Global.LANG]);

		findViewById(R.id.btnReturn).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mBluetoothScanner != null) {
					if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
						mBluetoothScanner.stopScan(mScanCallback);
					}
				}
				//BluetoothSearchService.getInstance().cancelDiscovery();
				dismiss();
				if (callback != null) {
					callback.doReturn();
				}
			}
		});

		findViewById(R.id.btnSearch).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				doSearch();
			}
		});

		mDeviceListView = (ListView) findViewById(R.id.deviceList);
		mDeviceAdapter = new ListAdapter();
		mDeviceListView.setAdapter(mDeviceAdapter);

		mDeviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
				if (mBluetoothScanner != null) {
					if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
						mBluetoothScanner.stopScan(mScanCallback);
					}
				}
				// BluetoothSearchService.getInstance().cancelDiscovery();
				dismiss();
				if (callback != null) {
					callback.doSelectDevice(mDeviceList.get(i));
				}
			}
		});

		mScanCallback = new ScanCallback() {
			@Override
			public void onScanResult(int callbackType, ScanResult result) {
				//super.onScanResult(callbackType, result);
				BluetoothDevice device = result.getDevice();
				mDeviceList.add(device);
				refreshView();
			}

			@Override
			public void onBatchScanResults(List<ScanResult> results) {
				//super.onBatchScanResults(results);
				for (ScanResult result: results) {
					BluetoothDevice device = result.getDevice();
					mDeviceList.add(device);
				}
				refreshView();
			}

			@Override
			public void onScanFailed(int errorCode) {
				// super.onScanFailed(errorCode);
				findViewById(R.id.progressbar).setVisibility(View.GONE);
			}
		};

		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter != null) {
			mBluetoothScanner = mBluetoothAdapter.getBluetoothLeScanner();
		}

		doSearch();
	}

	private void doSearch() {
		mDeviceList.clear();

		if (mBluetoothScanner == null) {
			Toast.makeText(mContext, "BLE scan is not available", Toast.LENGTH_LONG).show();
			return;
		}

		try {
			if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
				UUID[] serviceUUIDs = new UUID[]{UUID.fromString(Global.BLE_SERVICE_UUID)};
				List<ScanFilter> filters = null;
				filters = new ArrayList<>();
				for (UUID serviceUUID : serviceUUIDs) {
					ScanFilter filter = new ScanFilter.Builder()
							.setServiceUuid(new ParcelUuid(serviceUUID))
							.build();
					filters.add(filter);
				}
				ScanSettings scanSettings = null;
				if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
					scanSettings = new ScanSettings.Builder()
							.setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
							.setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
							.setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
							.setNumOfMatches(ScanSettings.MATCH_NUM_ONE_ADVERTISEMENT)
							.setReportDelay(0L)
							.build();
				}
				mBluetoothScanner.startScan(filters, scanSettings, mScanCallback);
			}
			else {
				Toast.makeText(mContext, "BLUETOOTH_SCAN permission should be allowed first", Toast.LENGTH_LONG).show();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

//		BluetoothSearchService.getInstance().doDiscovery(mContext, new BluetoothSearchService.BluetoothSearchCallback() {
//			@Override
//			public void onFindDevice(BluetoothDevice device) {
//				mDeviceList.add(device);
//				refreshView();
//			}
//
//			@Override
//			public void onStartFindingDevice() {
//				findViewById(R.id.progressbar).setVisibility(View.VISIBLE);
//				refreshView();
//			}
//
//			@Override
//			public void onFinishedDevice(List<BluetoothDevice> deviceList) {
//				findViewById(R.id.progressbar).setVisibility(View.GONE);
//				refreshView();
//			}
//		});
		//mDeviceList.addAll(BluetoothSearchService.getInstance().getAllDeviceList());
		refreshView();
	}

	private void refreshView()
	{
		if(mDeviceList.size() > 0)
		{
			findViewById(R.id.txtNoDevice).setVisibility(View.GONE);
			mDeviceListView.setVisibility(View.VISIBLE);
			mDeviceAdapter.notifyDataSetChanged();
		}
		else
		{
			findViewById(R.id.txtNoDevice).setVisibility(View.VISIBLE);
			mDeviceListView.setVisibility(View.GONE);
		}
	}

	public class ListAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			return mDeviceList.size();
		}

		@Override
		public BluetoothDevice getItem(int i) {
			return mDeviceList.get(i);
		}

		@Override
		public long getItemId(int i) {
			return i;
		}

		@Override
		public View getView(int i, View view, ViewGroup viewGroup) {
			view = getLayoutInflater().inflate(R.layout.row_bluetooth_device, viewGroup, false);
			BluetoothDevice device = getItem(i);
			String strDeviceName = "";
			try {
				strDeviceName = device.getName();
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
			((TextView)view.findViewById(R.id.txtDeviceInfo)).setText(strDeviceName + "(" + device.getAddress() + ")");
			return view;
		}
	}
}

