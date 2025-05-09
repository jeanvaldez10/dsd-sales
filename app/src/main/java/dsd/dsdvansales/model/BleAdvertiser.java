package dsd.dsdvansales.model;

import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.os.ParcelUuid;
import android.util.Log;

import dsd.dsdvansales.services.GattServerService;

public class BleAdvertiser {

    private static final String TAG = "ble-advertiser";

    // Singleton instance
    private static final BleAdvertiser instance = new BleAdvertiser();

    private BleAdvertiser() {} // Private constructor

    public static BleAdvertiser getInstance() {
        return instance;
    }

    public static class Callback extends AdvertiseCallback {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            Log.i(TAG, "LE Advertise Started.");
        }

        @Override
        public void onStartFailure(int errorCode) {
            Log.w(TAG, "LE Advertise Failed: " + errorCode);
        }
    }

    public static AdvertiseSettings settings() {
        return new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                .setConnectable(true)
                .setTimeout(0)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_LOW)
                .build();
    }

    public static AdvertiseData advertiseData() {
        return new AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .setIncludeTxPowerLevel(true)
                .addServiceUuid(new ParcelUuid(GattServerService.MyServiceProfile.MY_SERVICE_UUID))
                .build();
    }
}