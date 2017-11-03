package com.unime.tensorflowproject.audio;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

public class SpeechRecognitionService extends Service {
    private static final String SPEECH_RECOGNITION_SERVICE_TAG = "SpeechRecognitionService";
    private SpeechRecognition mSpeechRecognitionManager;

    @Override
    public void onCreate() {
        mSpeechRecognitionManager = new SpeechRecognition(this);
        mSpeechRecognitionManager.createSpeechRecognizer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String smartObjectName = intent.getStringExtra("SmartObject");
        mSpeechRecognitionManager.startListening(smartObjectName);
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d(SPEECH_RECOGNITION_SERVICE_TAG, "onDestroy");
        mSpeechRecognitionManager.destroySpeechRecognizer();

    }
}
