package com.unime.tensorflowproject.interaction;

import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.BeaconTransmitter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;


/**
 *
 */

public class SmartObjectInteractionService extends IntentService {
    private static final String SMART_OBJECT_INTERACTION_SERVICE_TAG = "SmartObjectInteractionService";

    private BeaconTransmitter beaconTransmitter;
    private BeaconParser beaconParser;
    private Counter dup;
    private String uuid_room = "INSERT UUID";
    private CustomResponse mCustomResponse;


    int REQUEST_ENABLE_BT = 1;

    private BluetoothAdapter mBluetoothAdapter;


    private boolean connected = false;


    public SmartObjectInteractionService() {
        super("SmartObjectInteractionService");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        beaconParser = new BeaconParser().setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25");
        beaconTransmitter = new BeaconTransmitter(getApplicationContext(), beaconParser);

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
        // TODO: check if bluetooth was activated or not
        // maybe we have to put a logic here
        dup = new Counter();

        String url = "http://212.189.207.53:8586/smartenv/board/" + uuid_room;

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(
                Request.Method.GET,
                url,
                response -> mCustomResponse = new CustomResponse(response),
                error -> Log.e(SMART_OBJECT_INTERACTION_SERVICE_TAG, "onErrorResponse: " + error.getMessage())
        );

        try {
            JSONObject reader = new JSONObject(mCustomResponse.getResponse());
            String name = reader.getString("name");

            JSONArray jArray = reader.getJSONArray("services");

            for (int i = 0; i < jArray.length(); i++) {
                try {
                    JSONObject service_obj = jArray.getJSONObject(i);
                    String type = service_obj.getString("type");
                    Boolean fast_triggered = service_obj.getBoolean("fast_triggered");
                    String service_name = service_obj.getString("name");
                    final String otp = service_obj.getString("otp");
                    final JSONArray service_id = service_obj.getJSONArray("code");

                    Log.i("fast triggered", fast_triggered.toString());

                    if (fast_triggered) {
                        if (beaconTransmitter.isStarted()) {
                            beaconTransmitter.stopAdvertising();
                        }

                        String service_id_data = null;

                        try {
                            service_id_data = service_id.get(0).toString();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        Beacon beacon = createBeacon(service_id_data, otp, dup);

                        beaconTransmitter.startAdvertising(beacon);

                        // stop advertising after 3 seconds
                        Handler mCanceller = new Handler();
                        mCanceller.postDelayed(() -> beaconTransmitter.stopAdvertising(), 3000);

                    }
                } catch (JSONException e) {
                    Log.e(SMART_OBJECT_INTERACTION_SERVICE_TAG, "onHandleIntent: " + e.getMessage());
                }
            }
        } catch (JSONException | NullPointerException e) {
            Log.e(SMART_OBJECT_INTERACTION_SERVICE_TAG, "onHandleIntent: " + e.getMessage());
        }

        Log.d(SMART_OBJECT_INTERACTION_SERVICE_TAG, "onHandleIntent: end");
    }

    public Beacon createBeacon(String service_id, String otp, Counter dup){
        dup.increment();
        Beacon beacon = new Beacon.Builder()
                .setId1(otp.substring(0, 8) + "-" + otp.substring(8, 12) + "-" +
                        otp.substring(12, 16) + "-" + otp.substring(16, 32))
                .setId2(service_id)
                .setId3(dup.getGlobalVarValue())
                .setManufacturer(0x00ff)
                .setTxPower(-59)
                .setRssi(-59)
                .setDataFields(Arrays.asList(new Long[]{0l})) // Remove this for beacon layouts without d: fields
                .build();
        Log.i("BEACON", beacon.toString());

        return beacon;
    }

    private class CustomResponse {
        private String response;

        private CustomResponse(String response) {
            this.response = response;
        }

        private String getResponse() {
            return response;
        }

        private void setResponse(String response) {
            this.response = response;
        }
    }
}
