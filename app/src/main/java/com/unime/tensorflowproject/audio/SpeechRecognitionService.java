package com.unime.tensorflowproject.audio;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import static android.content.ContentValues.TAG;

public class SpeechRecognitionService extends Service {
    private static final String SPEECH_RECOGNITION_SERVICE_TAG = "SpeechRecognitionService";
    private SpeechRecognition mSpeechRecognitionManager;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(getApplicationContext().equals(this)) {
            Log.d(TAG, "onStartCommand: context are equals");
        }
        mSpeechRecognitionManager = new SpeechRecognition(getApplicationContext());
        mSpeechRecognitionManager.createSpeechRecognizer();
        mSpeechRecognitionManager.startListening();

        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d(SPEECH_RECOGNITION_SERVICE_TAG, "onDestroy: " + SPEECH_RECOGNITION_SERVICE_TAG);
        mSpeechRecognitionManager.destroySpeechRecognizer();
        super.onDestroy();
    }
}
