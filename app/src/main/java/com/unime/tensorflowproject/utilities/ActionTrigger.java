package com.unime.tensorflowproject.utilities;

import android.content.Intent;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.unime.tensorflowproject.interaction.SmartObjectInteractionService;

public class ActionTrigger {
    public static final String TAG = "ActionTrigger";
    public static final String LAMP = "lamp";
    public static final String DOOR = "door";
    public static final String UUID_LAMP = "c9d3a528-cb01-409a-8d53-df800cf1bb3d";
    public static final String UUID_DOOR = "86d11bbc-2c59-4bbf-a5be-bf166b1aebf7";

    private String uuidObject;


    private CommandTrigger commandTrigger;
    private Intent mSmartObjectInteractionService;

    public ActionTrigger(CommandTrigger commandTrigger) {
        this.commandTrigger = commandTrigger;
    }

    private CommandTrigger getCommandTrigger() {
        return commandTrigger;
    }

    public String getUuidObject() {
        return uuidObject;
    }

    public void setUuidObject(String uuidObject) {
        this.uuidObject = uuidObject;
    }

    public void makeRequest(){
        if(getCommandTrigger().getName().equals(DOOR)) {
            setUuidObject(UUID_DOOR);
        } else if(getCommandTrigger().getName().equals(LAMP)){
            setUuidObject(UUID_LAMP);
        }
        RequestQueue requestQueue = Volley.newRequestQueue(getCommandTrigger().getContext());
        String url = "http://212.189.207.225:9999/smartenv/" + uuidObject;

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(
                Request.Method.GET,
                url,
                (response )-> {
                    mSmartObjectInteractionService = new Intent(getCommandTrigger().getContext(), SmartObjectInteractionService.class);
                    mSmartObjectInteractionService.putExtra("name", getCommandTrigger().getName());
                    mSmartObjectInteractionService.putExtra("command", getCommandTrigger().getCommand());
                    mSmartObjectInteractionService.putExtra("response", response);
                    getCommandTrigger().getContext().startService(mSmartObjectInteractionService);

                    Log.d(TAG, "onResponse: "+response);
                },
                error -> Log.e(TAG, "onErrorResponse: " + error.getMessage())
        );
        requestQueue.add(stringRequest);
    }
}