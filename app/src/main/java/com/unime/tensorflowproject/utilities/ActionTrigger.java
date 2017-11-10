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
    private String uuid_room = "86d11bbc-2c59-4bbf-a5be-bf166b1aebf7";

    private CommandTrigger commandTrigger;
    private Intent mSmartObjectInteractionService;

    public ActionTrigger(CommandTrigger commandTrigger) {
        this.commandTrigger = commandTrigger;
    }

    private CommandTrigger getCommandTrigger() {
        return commandTrigger;
    }

    public void makeRequest(){
        RequestQueue requestQueue = Volley.newRequestQueue(getCommandTrigger().getContext());
        String url = "http://212.189.207.225:9999/smartenv/" + uuid_room;

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