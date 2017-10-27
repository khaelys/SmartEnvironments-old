package com.unime.tensorflowproject.interaction;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.unime.tensorflowproject.R;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.BeaconTransmitter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

public class SmartEnv extends AppCompatActivity {

    private TextView jsview;
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

    //inserisco dispositivo da ricercare
    private List<ScanFilter> filters;
    //inserisco servizio da ricercare
    private String serviceUUID = "";
    //inserisco caratteristica da ricercare
    private String characteristicUUID = "";
    private BluetoothGattCharacteristic characteristic = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smart_env);

        LinearLayout ll = (LinearLayout) findViewById(R.id.ll);

        jsview = (TextView) findViewById(R.id.jstext);

        beaconParser = new BeaconParser().setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25");
        beaconTransmitter = new BeaconTransmitter(getApplicationContext(), beaconParser);

        mHandler = new Handler();
        adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {

        } else {
            if (!adapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                System.out.println("Attivo bluetooth!");
            }
        }

        //dup = ((Counter)getApplicationContext());
        dup = new Counter();

        Bundle b = getIntent().getExtras();
        String value = ""; // or other values
        value = b.getString("response");


        try {
            JSONObject reader = new JSONObject(value);
            String name = reader.getString("name");
            jsview.setText(name);

            JSONArray jArray = reader.getJSONArray("services");

            for (int i = 0; i < jArray.length(); i++) {
                try {
                    JSONObject service_obj = jArray.getJSONObject(i);
                    String type = service_obj.getString("type");
                    Boolean fast_triggered = service_obj.getBoolean("fast_triggered");
                    String service_name = service_obj.getString("name");
                    final String otp = service_obj.getString("otp");
                    final JSONArray service_id = service_obj.getJSONArray("code");

                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                    final TextView desc = new TextView(this);
                    desc.setText(service_obj.getString("description"));
                    desc.setPadding(0, 50, 0, 0);
                    desc.setTextSize(14);
                    desc.setTypeface(null, Typeface.ITALIC);
                    ll.addView(desc, lp);
                    Log.i("fast triggered",fast_triggered.toString());

                    if (fast_triggered){
                        if (beaconTransmitter.isStarted()) {
                            beaconTransmitter.stopAdvertising();
                        }

                        String service_id_data = null;

                        try {
                            service_id_data = service_id.get(0).toString();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        Beacon beacon = create_Beacon(service_id_data,otp,dup);

                        beaconTransmitter.startAdvertising(beacon);

                        final ProgressDialog progress = new ProgressDialog(SmartEnv.this);
                        progress.setTitle("Connecting");
                        progress.setMessage("Please wait while we connect to device...");
                        progress.show();

                        Runnable progressRunnable = new Runnable() {

                            @Override
                            public void run() {
                                progress.cancel();
                            }
                        };

                        Handler pdCanceller = new Handler();
                        pdCanceller.postDelayed(progressRunnable, 3000);

                        progress.setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                beaconTransmitter.stopAdvertising();
                            }
                        });
                    }


                    switch (type) {
                        case "switch":

                            Switch toggle = new Switch(this);
                            toggle.setText(service_name);
                            toggle.setTextSize(18);
                            ll.addView(toggle, lp);

                            toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                                    if (beaconTransmitter.isStarted()) {
                                        beaconTransmitter.stopAdvertising();
                                    }

                                    String service_id_data = null;
                                    if (isChecked) {
                                        try {
                                            service_id_data = service_id.get(0).toString();

                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    else {
                                            try {
                                                service_id_data = service_id.get(1).toString();

                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }

                                    Beacon beacon = create_Beacon(service_id_data,otp,dup);

                                    beaconTransmitter.startAdvertising(beacon);

                                    final ProgressDialog progress = new ProgressDialog(SmartEnv.this);
                                    progress.setTitle("Connecting");
                                    progress.setMessage("Please wait while we connect to device...");
                                    progress.show();

                                    Runnable progressRunnable = new Runnable() {

                                        @Override
                                        public void run() {
                                            progress.cancel();
                                        }
                                    };

                                    Handler pdCanceller = new Handler();
                                    pdCanceller.postDelayed(progressRunnable, 3000);

                                    progress.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                        @Override
                                        public void onCancel(DialogInterface dialog) {
                                            beaconTransmitter.stopAdvertising();
                                        }
                                    });

                            }
                        });


                            break;
                        case "trigger":

                            final Button myButton = new Button(this);
                            myButton.setText(service_name);
                            myButton.setTextSize(18);
                            ll.addView(myButton, lp);

                            myButton.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {

                                    if (beaconTransmitter.isStarted()) {
                                        beaconTransmitter.stopAdvertising();
                                    }

                                    String service_id_data = null;

                                    try {
                                        service_id_data = service_id.get(0).toString();

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                    //se servizio senza connessione lancia beacon altrimenti chiama funzione ble
                                    Beacon beacon = create_Beacon(service_id_data,otp,dup);

                                    beaconTransmitter.startAdvertising(beacon);

                                    final ProgressDialog progress = new ProgressDialog(SmartEnv.this);
                                    progress.setTitle("Connecting");
                                    progress.setMessage("Please wait while we connect to device...");
                                    progress.show();

                                    Runnable progressRunnable = new Runnable() {

                                        @Override
                                        public void run() {
                                            progress.cancel();
                                        }
                                    };

                                    Handler pdCanceller = new Handler();
                                    pdCanceller.postDelayed(progressRunnable, 3000);

                                    progress.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                        @Override
                                        public void onCancel(DialogInterface dialog) {
                                            beaconTransmitter.stopAdvertising();
                                        }
                                    });

                                }
                            });

                            break;

                        case "switch_with_connection":

                            Switch toggle1 = new Switch(this);
                            toggle1.setText(service_name);
                            toggle1.setTextSize(18);
                            ll.addView(toggle1, lp);

                            //prelevare board,service e caratteristica dal json
                            //serviceUUID = service;
                            //characteristicUUID = caratteristica;
                            //bleInteraction(board);
                            final ProgressDialog progress = new ProgressDialog(SmartEnv.this);
                            progress.setTitle("Connecting");
                            progress.setMessage("Please wait while we connect to device...");
                            progress.show();

                            Runnable progressRunnable = new Runnable() {
                                @Override
                                public void run() {
                                    if(connected)
                                        progress.cancel();
                                }
                            };

                            progress.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    scanLeDevice(false);
                                }
                            });
                            toggle1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                                    boolean success;
                                    if(isChecked){
                                        byte[] valueToWrite = new byte[8];
                                        Arrays.fill(valueToWrite, (byte) 0x00);
                                        characteristic.setValue(valueToWrite);
                                        success = mGatt.writeCharacteristic(characteristic);
                                        System.out.println(success);
                                    }
                                    else{
                                        byte[] valueToWrite = new byte[8];
                                        Arrays.fill(valueToWrite, (byte) 0x01);
                                        characteristic.setValue(valueToWrite);
                                        success = mGatt.writeCharacteristic(characteristic);
                                        System.out.println(success);
                                    }
                                }
                            });
                    }



                } catch (JSONException e) {
                    // Oops
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    public Beacon create_Beacon(String s_id, String otp, Counter dup){
        dup.increment();
        Beacon beacon = new Beacon.Builder()
                .setId1(otp.substring(0, 8) + "-" + otp.substring(8, 12) + "-" + otp.substring(12, 16) + "-" + otp.substring(16, 32))
                .setId2(s_id)
                .setId3(dup.getGlobalVarValue())
                .setManufacturer(0x00ff)
                .setTxPower(-59)
                .setRssi(-59)
                .setDataFields(Arrays.asList(new Long[]{0l})) // Remove this for beacon layouts without d: fields
                .build();
        Log.i("BEACON", beacon.toString());

        return beacon;
    }

    public void bleInteraction (String device){

        mLEScanner = adapter.getBluetoothLeScanner();
        settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
        filters = Arrays.asList(
                new ScanFilter.Builder().setServiceUuid(ParcelUuid.fromString(device)).build()
        );
        //niente filtri
        //filters = new ArrayList<ScanFilter>();
        //serviceUUID = service;
        //characteristicUUID = characteristic;

        scanLeDevice(true);

    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (Build.VERSION.SDK_INT < 21) {
                        adapter.stopLeScan(mLeScanCallback);
                    } else {
                        mLEScanner.stopScan(mScanCallback);

                    }
                }
            }, SCAN_PERIOD);
            if (Build.VERSION.SDK_INT < 21) {
                adapter.startLeScan(mLeScanCallback);
            } else {
                mLEScanner.startScan(filters, settings, mScanCallback);
            }
        } else {
            if (Build.VERSION.SDK_INT < 21) {
                adapter.stopLeScan(mLeScanCallback);
            } else {
                mLEScanner.stopScan(mScanCallback);
            }
        }
    }

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.i("callbackType", String.valueOf(callbackType));
            Log.i("result", result.toString());
            BluetoothDevice btDevice = result.getDevice();
            connectToDevice(btDevice);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult sr : results) {
                Log.i("ScanResult - Results", sr.toString());
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e("Scan Failed", "Error Code: " + errorCode);
        }
    };

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi,
                                     byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.i("onLeScan", device.toString());
                            connectToDevice(device);
                        }
                    });
                }
            };

    public void connectToDevice(BluetoothDevice device) {
        if (mGatt == null) {
            mGatt = device.connectGatt(this, true, gattCallback);
            scanLeDevice(false);// will stop after first device detection
        }
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i("onConnectionStateChange", "Status: " + status);
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    Log.i("gattCallback", "STATE_CONNECTED");
                    gatt.discoverServices();
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.e("gattCallback", "STATE_DISCONNECTED");
                    break;
                default:
                    Log.e("gattCallback", "STATE_OTHER");
            }

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            List<BluetoothGattService> services = gatt.getServices();
            Log.i("onServicesDiscovered", services.toString());
            //gatt.readCharacteristic(services.get(1).getCharacteristics().get(0));
            connected = true;
            for(BluetoothGattService service: services)
                if(service.getUuid().toString().equalsIgnoreCase(serviceUUID))
                    for(BluetoothGattCharacteristic serviceCharacteristic: service.getCharacteristics())
                        if(serviceCharacteristic.getUuid().toString().equalsIgnoreCase(characteristicUUID))
                            //gatt.readCharacteristic(serviceCharacteristic);
                            characteristic = serviceCharacteristic;
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic
                                                 characteristic, int status) {
            Log.i("onCharacteristicRead", characteristic.toString());
            /*//leggo la caratteristica
            byte[] characteristicValue = characteristic.getValue();
            //scrivo la caratteristica
            byte[] valueToWrite = new byte[8];
            Arrays.fill(valueToWrite, (byte) 0x00);
            characteristic.setValue(valueToWrite);
            boolean success = gatt.writeCharacteristic(characteristic);
            if(success)
                gatt.disconnect();*/
        }
    };

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        super.onBackPressed();
        beaconTransmitter.stopAdvertising();
    }
}
