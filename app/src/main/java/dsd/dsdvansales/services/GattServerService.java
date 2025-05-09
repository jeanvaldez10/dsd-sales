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
import android.bluetooth.BluetoothGattDescriptor;
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

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import dsd.dsdvansales.BuildConfig;
import dsd.dsdvansales.R;
import dsd.dsdvansales.global.Global;
import dsd.dsdvansales.model.BleAdvertiser;
import no.nordicsemi.android.ble.BleManager;
import no.nordicsemi.android.ble.BleServerManager;
import no.nordicsemi.android.ble.observer.ServerObserver;

public class GattServerService extends Service {
    private static final String TAG = "gatt-server-service";

    private ServerManager serverManager;
    private BroadcastReceiver bluetoothObserver;
    private BleAdvertiser.Callback bleAdvertiseCallback;

    @SuppressLint("WrongConstant")
    @Override
    public void onCreate() {
        super.onCreate();

        // Setup as a foreground service
        NotificationManager notificationService = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    GattServerService.class.getSimpleName(),
                    getResources().getString(R.string.gatt_server_service_name),
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationService.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder notification = new NotificationCompat.Builder(this, GattServerService.class.getSimpleName())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getResources().getString(R.string.gatt_server_service_name))
                .setContentText(getResources().getString(R.string.gatt_server_service_running_notification))
                .setAutoCancel(true);

        startForeground(2, notification.build());

        // Observe OS state changes in BLE
        bluetoothObserver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent == null || intent.getAction() == null) return;

                if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(intent.getAction())) {
                    int bluetoothState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                    switch (bluetoothState) {
                        case BluetoothAdapter.STATE_ON:
                            enableBleServices();
                            break;
                        case BluetoothAdapter.STATE_OFF:
                            disableBleServices();
                            break;
                    }
                }
            }
        };

        ContextCompat.registerReceiver(this,
                bluetoothObserver,
                new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED),
                ContextCompat.RECEIVER_EXPORTED);

        // Startup BLE if we have it
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager.getAdapter() != null && bluetoothManager.getAdapter().isEnabled()) {
            enableBleServices();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disableBleServices();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new DataPlane();
    }

    private void enableBleServices() {
        serverManager = new ServerManager(this);
        serverManager.open();

        bleAdvertiseCallback = new BleAdvertiser.Callback();

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager.getAdapter().getBluetoothLeAdvertiser() != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE) == PackageManager.PERMISSION_GRANTED) {
                bluetoothManager.getAdapter().getBluetoothLeAdvertiser().startAdvertising(
                        BleAdvertiser.settings(),
                        BleAdvertiser.advertiseData(),
                        bleAdvertiseCallback
                );
            }
        }
    }

    private void disableBleServices() {
        if (bleAdvertiseCallback != null) {
            BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (bluetoothManager.getAdapter() != null && bluetoothManager.getAdapter().getBluetoothLeAdvertiser() != null) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE) == PackageManager.PERMISSION_GRANTED) {
                    bluetoothManager.getAdapter().getBluetoothLeAdvertiser().stopAdvertising(bleAdvertiseCallback);
                }

            }
            bleAdvertiseCallback = null;
        }

        if (serverManager != null) {
            serverManager.close();
            serverManager = null;
        }
    }

    public class DataPlane extends Binder implements GattDeviceAPI {
        @Override
        public void setMyCharacteristicValue(String value) {
            if (serverManager != null) {
                serverManager.setMyCharacteristicValue(value);
            }
        }
    }

    public static class ServerManager extends BleServerManager implements ServerObserver, GattDeviceAPI {
        private static final UUID CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

        private final BluetoothGattCharacteristic myGattCharacteristic;
        private final BluetoothGattService myGattService;
        private final Map<String, ServerConnection> serverConnections = new HashMap<>();
        public Context mContext;

        public ServerManager(Context context) {
            super(context);

            mContext = context;

            // Create characteristic
            myGattCharacteristic = sharedCharacteristic(
                    MyServiceProfile.MY_CHARACTERISTIC_UUID,
                    BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                    BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED_MITM,
                    descriptor(CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID,
                            BluetoothGattDescriptor.PERMISSION_READ_ENCRYPTED_MITM |
                                    BluetoothGattDescriptor.PERMISSION_WRITE_ENCRYPTED_MITM,
                            new byte[]{0, 0}),
                    description("A characteristic to be read", false)
            );

            // Create service
            myGattService = service(
                    MyServiceProfile.MY_SERVICE_UUID,
                    myGattCharacteristic
            );
        }

        @Override
        public void setMyCharacteristicValue(String value) {
            byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
            for (ServerConnection serverConnection : serverConnections.values()) {
                serverConnection.sendNotificationForMyGattCharacteristic(bytes);
            }
        }

        @Override
        public void log(int priority, String message) {
            if (BuildConfig.DEBUG || priority == Log.ERROR) {
                Log.println(priority, TAG, message);
            }
        }

        @Override
        protected List<BluetoothGattService> initializeServer() {
            setServerObserver(this);
            return Collections.singletonList(myGattService);
        }

        @Override
        public void onServerReady() {
            log(Log.INFO, "Gatt server ready");
        }

        @Override
        public void onDeviceConnectedToServer(BluetoothDevice device) {
            log(Log.DEBUG, "Device connected " + device.getAddress());

            ServerConnection serverConnection = new ServerConnection();
            serverConnection.useServer(this);
            serverConnection.connect(device).enqueue();
            serverConnections.put(device.getAddress(), serverConnection);
        }

        @Override
        public void onDeviceDisconnectedFromServer(BluetoothDevice device) {
            log(Log.DEBUG, "Device disconnected " + device.getAddress());

            ServerConnection connection = serverConnections.remove(device.getAddress());
            if (connection != null) {
                connection.close();
            }
        }

        public class ServerConnection extends BleManager {
            public ServerConnection() {
                super(ServerManager.this.mContext);
            }

            public void sendNotificationForMyGattCharacteristic(byte[] value) {
                sendNotification(myGattCharacteristic, value).enqueue();
            }

            @Override
            public void log(int priority, String message) {
                ServerManager.this.log(priority, message);
            }

            @Override
            protected boolean isRequiredServiceSupported(BluetoothGatt gatt) {
                return true;
            }

            @Override
            protected void onServicesInvalidated() {
                // This is the place to nullify characteristics obtained above.
            }
        }
    }

    public static class MyServiceProfile {
        public static final UUID MY_SERVICE_UUID = UUID.fromString(Global.BLE_SERVICE_UUID);
        public static final UUID MY_CHARACTERISTIC_UUID = UUID.fromString(Global.BLE_CHARACTERISTIC_UUID);
    }
}