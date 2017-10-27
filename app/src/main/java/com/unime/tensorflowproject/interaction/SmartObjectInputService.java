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

import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.BeaconTransmitter;

import java.util.List;

/**
 *
 */

public class SmartObjectInputService extends IntentService {
    private BeaconTransmitter beaconTransmitter;
    private BeaconParser beaconParser;
    private Counter dup;

    int REQUEST_ENABLE_BT = 1;

    private BluetoothAdapter adapter;
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

    @Override
    public void onCreate() {
        super.onCreate();

        beaconParser = new BeaconParser().setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25");
        beaconTransmitter = new BeaconTransmitter(getApplicationContext(), beaconParser);

        mHandler = new Handler();
        adapter = BluetoothAdapter.getDefaultAdapter();
        if (null != adapter) {
            if (!adapter.isEnabled()) {
                // TODO: User must activate bluetooth
            }
        }
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

    }


}
