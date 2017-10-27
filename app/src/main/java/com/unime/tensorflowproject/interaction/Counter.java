package com.unime.tensorflowproject.interaction;

import android.app.Application;

import java.util.Random;

/**
 *
 */

public class Counter extends Application {
    private int val;

    public Counter() {
        Random rand = new Random();
        val = rand.nextInt(10000) + 1;
    }

    public String getGlobalVarValue() {
        return String.format("%04X", val);
    }

    public String increment() {
        return String.format("%04X", ++val);
    }
}
