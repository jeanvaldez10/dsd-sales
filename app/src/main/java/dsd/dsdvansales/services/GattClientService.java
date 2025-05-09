package dsd.dsdvansales.services;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import dsd.dsdvansales.R;
import dsd.dsdvansales.BuildConfig;
import dsd.dsdvansales.global.Global;
import no.nordicsemi.android.ble.BleManager;
import no.nordicsemi.android.ble.callback.SuccessCallback;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GattClientService extends Service {
    private static final String TAG = "gatt-client-service";
    public static final String DATA_PLANE_ACTION = "data-plane";

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private MyCharacteristicChangedChannel myCharacteristicChangedChannel;
    private final Map<String, ClientManager> clientManagers = new HashMap<>();
    private BroadcastReceiver stateChangedObserver;
    private BroadcastReceiver bondStateObserver;

    @SuppressLint("WrongConstant")
    @Override
    public void onCreate() {
        super.onCreate();

        NotificationManager notificationService =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Setup as a foreground service
            NotificationChannel notificationChannel = new NotificationChannel(
                    GattClientService.class.getSimpleName(),
                    getResources().getString(R.string.gatt_client_service_name),
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationService.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder notification = new NotificationCompat.Builder(
                this, GattClientService.class.getSimpleName())
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(getResources().getString(R.string.gatt_client_service_name))
                .setContentText(getResources().getString(R.string.gatt_client_service_running_notification))
                .setAutoCancel(true);

        startForeground(3, notification.build());

        // Observe OS state changes in BLE
        stateChangedObserver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int bluetoothState = intent.getIntExtra(
                        BluetoothAdapter.EXTRA_STATE,
                        -1
                );
                switch (bluetoothState) {
                    case BluetoothAdapter.STATE_ON:
                        enableBleServices();
                        break;
                    case BluetoothAdapter.STATE_OFF:
                        disableBleServices();
                        break;
                }
            }
        };

        bondStateObserver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Bond state changed for device " + device.getAddress() +
                            ": " + device.getBondState());
                    switch (device.getBondState()) {
                        case BluetoothDevice.BOND_BONDED:
                            addDevice(device);
                            break;
                        case BluetoothDevice.BOND_NONE:
                            removeDevice(device);
                            break;
                    }
                }
            }
        };

        ContextCompat.registerReceiver(this, stateChangedObserver,
                new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED),
                ContextCompat.RECEIVER_EXPORTED);
        ContextCompat.registerReceiver(this, bondStateObserver,
                new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED),
                ContextCompat.RECEIVER_EXPORTED);

        // Startup BLE if we have it
        BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager.getAdapter() != null && bluetoothManager.getAdapter().isEnabled()) {
            enableBleServices();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(stateChangedObserver);
        unregisterReceiver(bondStateObserver);
        disableBleServices();
        executorService.shutdown();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        if (intent != null && DATA_PLANE_ACTION.equals(intent.getAction())) {
            return new DataPlane();
        }
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (intent != null && DATA_PLANE_ACTION.equals(intent.getAction())) {
            myCharacteristicChangedChannel = null;
            return true;
        }
        return false;
    }

    public class DataPlane extends Binder {
        public void setMyCharacteristicChangedChannel(MyCharacteristicChangedChannel channel) {
            myCharacteristicChangedChannel = channel;
        }
    }

    private void enableBleServices() {
        BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager.getAdapter() != null && bluetoothManager.getAdapter().isEnabled()) {
            Log.i(TAG, "Enabling BLE services");
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                for (BluetoothDevice device : bluetoothManager.getAdapter().getBondedDevices()) {
                    addDevice(device);
                }
            }

        } else {
            Log.w(TAG, "Cannot enable BLE services as either there is no Bluetooth adapter or it is disabled");
        }
    }

    private void disableBleServices() {
        for (ClientManager clientManager : clientManagers.values()) {
            clientManager.close();
        }
        clientManagers.clear();
    }

    private void addDevice(BluetoothDevice device) {
        if (!clientManagers.containsKey(device.getAddress())) {
            ClientManager clientManager = new ClientManager();
            clientManager.connect(device).useAutoConnect(true).enqueue();
            clientManagers.put(device.getAddress(), clientManager);
        }
    }

    private void removeDevice(BluetoothDevice device) {
        ClientManager manager = clientManagers.remove(device.getAddress());
        if (manager != null) {
            manager.close();
        }
    }

    private class ClientManager extends BleManager {
        private BluetoothGattCharacteristic myCharacteristic;

        ClientManager() {
            super(GattClientService.this);
        }

        @Override
        public void log(int priority, String message) {
            if (BuildConfig.DEBUG || priority == Log.ERROR) {
                Log.println(priority, TAG, message);
            }
        }

        @Override
        protected boolean isRequiredServiceSupported(BluetoothGatt gatt) {
            BluetoothGattService service = gatt.getService(MyServiceProfile.MY_SERVICE_UUID);
            myCharacteristic = service != null ?
                    service.getCharacteristic(MyServiceProfile.MY_CHARACTERISTIC_UUID) : null;
            int myCharacteristicProperties = myCharacteristic != null ?
                    myCharacteristic.getProperties() : 0;
            return (myCharacteristicProperties & BluetoothGattCharacteristic.PROPERTY_READ) != 0 &&
                    (myCharacteristicProperties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0;
        }

        @Override
        protected void initialize() {
            setNotificationCallback(myCharacteristic).with((device, data) -> {
                if (data.getValue() != null) {
                    String value = new String(data.getValue(), StandardCharsets.UTF_8);
                    executorService.execute(() -> {
                        if (myCharacteristicChangedChannel != null) {
                            myCharacteristicChangedChannel.onCharacteristicChanged(value);
                        }
                    });
                }
            });

            beginAtomicRequestQueue()
                    .add(enableNotifications(myCharacteristic)
                            .fail((device, status) -> {
                                log(Log.ERROR, "Could not subscribe: " + status);
                                disconnect().enqueue();
                            })
                    )
                    .done(device -> {
                        log(Log.INFO, "Target initialized");
                    })
                    .enqueue();
        }

        @Override
        protected void onServicesInvalidated() {
            myCharacteristic = null;
        }
    }

    public interface MyCharacteristicChangedChannel {
        void onCharacteristicChanged(String value);
    }

    public static class MyServiceProfile {
        public static final UUID MY_SERVICE_UUID =
                UUID.fromString(Global.BLE_SERVICE_UUID);
        public static final UUID MY_CHARACTERISTIC_UUID =
                UUID.fromString(Global.BLE_CHARACTERISTIC_UUID);
    }
}