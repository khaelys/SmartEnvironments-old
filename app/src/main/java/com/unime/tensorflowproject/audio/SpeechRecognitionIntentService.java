package com.unime.tensorflowproject.audio;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

public class SpeechRecognitionIntentService extends IntentService {
    private static final String SPEECH_RECOGNITION_INTENT_SERVICE_TAG = "SpeechRecognitionIntentService";

    public SpeechRecognitionIntentService() {
        super(SPEECH_RECOGNITION_INTENT_SERVICE_TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        SpeechRecognition mSpeechRecognitionManager = new SpeechRecognition(this);
        mSpeechRecognitionManager.createSpeechRecognizer();
        mSpeechRecognitionManager.startListening();
        mSpeechRecognitionManager.destroySpeechRecognizer();
    }




    @Override
    public void onDestroy() {
//        mSpeechRecognitionManager.destroySpeechRecognizer();
        super.onDestroy();
    }
}
