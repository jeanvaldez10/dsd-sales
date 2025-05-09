package dsd.dsdvansales.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BluetoothSearchService {

    public interface BluetoothSearchCallback {
        void onFindDevice(BluetoothDevice device);
        void onStartFindingDevice();
        void onFinishedDevice(List<BluetoothDevice> deviceList);
    }

    Context mContext;
    List<BluetoothDevice> mDeviceList = new ArrayList<>();
    Map<String, BluetoothDevice> mDeviceMap = new HashMap<>();

    private final BluetoothAdapter mAdapter;
    private static BluetoothSearchService instance = null;
    private BluetoothSearchCallback callback;
    private boolean bRegister = false;
    public static BluetoothSearchService getInstance()
    {
        if(instance == null)
        {
            instance = new BluetoothSearchService();
        }
        return instance;
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(device.getBondState() != BluetoothDevice.BOND_BONDED)
                {
                    switch(device.getBluetoothClass().getDeviceClass())
                    {
                        case BluetoothClass.Device.PHONE_CELLULAR:
                        case BluetoothClass.Device.PHONE_CORDLESS:
                        case BluetoothClass.Device.PHONE_ISDN:
                        case BluetoothClass.Device.PHONE_MODEM_OR_GATEWAY:
                        case BluetoothClass.Device.PHONE_SMART:
                        case BluetoothClass.Device.PHONE_UNCATEGORIZED:
                        case BluetoothClass.Device.COMPUTER_HANDHELD_PC_PDA: //For Tablet
                        case 284: //For Tablet
                        {
                            if(mDeviceMap.get(device.getAddress()) == null)
                            {
                                mDeviceMap.put(device.getAddress(), device);
                                mDeviceList.add(device);
                                if(callback != null)
                                {
                                    callback.onFindDevice(device);
                                }
                            }
                        }
                        break;
                    }
                }

                // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                if(callback != null){
                    callback.onFinishedDevice(mDeviceList);
                }
                cancelDiscovery();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                if(callback != null)
                {
                    callback.onStartFindingDevice();
                }
            }
        }
    };

    private BluetoothSearchService()
    {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public List<BluetoothDevice> getAllDeviceList()
    {
        return mDeviceList;
    }

    public void doDiscovery(Context context, BluetoothSearchCallback callback)
    {
        // Register for broadcasts when a device is discovered
        this.callback = callback;
        mContext = context;
        if(!bRegister)
        {
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.registerReceiver(mReceiver, filter, Context.RECEIVER_EXPORTED);
            } else {
                context.registerReceiver(mReceiver, filter);
            }

            // Register for broadcasts when discovery has finished
            filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.registerReceiver(mReceiver, filter, Context.RECEIVER_EXPORTED);
            } else {
                context.registerReceiver(mReceiver, filter);
            }


            filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.registerReceiver(mReceiver, filter, Context.RECEIVER_EXPORTED);
            } else {
                context.registerReceiver(mReceiver, filter);
            }
            bRegister = true;
        }

        // If we're already discovering, stop it
        mDeviceList.clear();
        mDeviceMap.clear();
        for(BluetoothDevice device : BluetoothAdapter.getDefaultAdapter().getBondedDevices())
        {
            switch(device.getBluetoothClass().getDeviceClass())
            {
                case BluetoothClass.Device.PHONE_CELLULAR:
                case BluetoothClass.Device.PHONE_CORDLESS:
                case BluetoothClass.Device.PHONE_ISDN:
                case BluetoothClass.Device.PHONE_MODEM_OR_GATEWAY:
                case BluetoothClass.Device.PHONE_SMART:
                case BluetoothClass.Device.PHONE_UNCATEGORIZED:
                case BluetoothClass.Device.COMPUTER_HANDHELD_PC_PDA: //For Tablet
                case 284: //For Tablet
                {
                    if(mDeviceMap.get(device.getAddress()) == null)
                    {
                        mDeviceMap.put(device.getAddress(), device);
                        mDeviceList.add(device);
                    }
                }
                break;
            }
        }
        if (mAdapter.isDiscovering()) {
            mAdapter.cancelDiscovery();
        }

        // Request discover from BluetoothAdapter
        mAdapter.startDiscovery();
    }

    public void cancelDiscovery()
    {
        // If we're already discovering, stop it
        if (mAdapter.isDiscovering()) {
            mAdapter.cancelDiscovery();
        }
        if(mReceiver != null && mContext != null)
        {
            if(bRegister)
            {
                mContext.unregisterReceiver(mReceiver);
                bRegister = false;
            }

        }
    }

}
