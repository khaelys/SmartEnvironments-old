package com.unime.tensorflowproject.interaction;

import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;

import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.BeaconTransmitter;

import java.util.List;


/**
 *
 */

public class SmartObjectInteractionService extends IntentService {
    private static final String SMART_OBJECT_INTERACTION_SERVICE_TAG = "SmartObjectInteractionService";

    private BeaconTransmitter beaconTransmitter;
    private BeaconParser beaconParser;
    private Counter dup;

    int REQUEST_ENABLE_BT = 1;

    private BluetoothAdapter mBluetoothAdapter;
    private static final long SCAN_PERIOD = 10000;
    private Handler mHandler;
    private BluetoothLeScanner mLEScanner;
    private ScanSettings settings;
    private BluetoothGatt mGatt;

    private boolean connected = false;

    private List<ScanFilter> filters;
    private String serviceUUID = "";
    private String characteristicUUID = "";
    private BluetoothGattCharacteristic characteristic = null;

    public SmartObjectInteractionService() {
        super("SmartObjectInteractionService");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        beaconParser = new BeaconParser().setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25");
        beaconTransmitter = new BeaconTransmitter(getApplicationContext(), beaconParser);

        mHandler = new Handler();

        // Use this check to determine whether BLE is supported on the device. Then
        // you can selectively disable BLE-related features.
//        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
//            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
//            finish();
//        }

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if (mBluetoothAdapter != null && !mBluetoothAdapter.isEnabled()) {
            Log.d(SMART_OBJECT_INTERACTION_SERVICE_TAG, "mBlueetoothAdapter is null: " + (mBluetoothAdapter == null));

            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            getApplicationContext().startActivity(enableBtIntent);
        }
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d(SMART_OBJECT_INTERACTION_SERVICE_TAG, "onHandleIntent: start");
        Log.d(SMART_OBJECT_INTERACTION_SERVICE_TAG, "onHandleIntent: end");
    }
}
