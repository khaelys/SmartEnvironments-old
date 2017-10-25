package com.unime.tensorflowproject.utilities;

/**
 * This class is a Singleton
 */


public class CommandTrigger {
    private static final int MILLISECONDS_BEFORE_SPEECH = 1500; // 1 prediction each 300 ms
    private static final double PREDICTION_THRESHOLD = 0.90;
    private static final String UNKNOWN_OBJECT = "unknown";

    private static CommandTrigger instance;
    private static String smartObjectName = UNKNOWN_OBJECT;
    private static double averageConfidence = 0.0;
    private static int msBeforeSpeechCounter = 0;

    private CommandTrigger() {
    }

    public static String getSmartObjectName() {
        return smartObjectName;
    }

    public static void setSmartObjectName(String smartObjectName) {
        CommandTrigger.smartObjectName = smartObjectName;
    }

    public static double getAverageConfidence() {
        return averageConfidence;
    }

    public static void setAverageConfidence(double averageConfidence) {
        CommandTrigger.averageConfidence = averageConfidence;
    }

    public static int getMsBeforeSpeechCounter() {
        return msBeforeSpeechCounter;
    }

    public static void setMsBeforeSpeechCounter(int msBeforeSpeechCounter) {
        CommandTrigger.msBeforeSpeechCounter = msBeforeSpeechCounter;
    }

    public static CommandTrigger getInstance() {
        // check if the instance has not been created yet

        if(instance == null ) {
            instance = new CommandTrigger();
        }
        return instance;
    }

    public static boolean hasToBeTriggered(String smartObjectName, double confidence, int lastProcessingTimeMs){
        if(hasToBeReset(smartObjectName)) {
            if(!smartObjectName.equals(UNKNOWN_OBJECT))
                reset(smartObjectName, confidence, lastProcessingTimeMs);
        } else {
            update(confidence, lastProcessingTimeMs);
            if(hasBeenRecognized()) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasToBeReset(String smartObjectName) {
        // the NeuralNetwork predicted a different object, so we have to reset all the variables
        // or if in the given time slot we have not enough confidence
        if(!smartObjectName.equals(getSmartObjectName()) || (getAverageConfidence() < PREDICTION_THRESHOLD
                && getMsBeforeSpeechCounter() >= MILLISECONDS_BEFORE_SPEECH) || smartObjectName.equals(UNKNOWN_OBJECT)) {
               return true;
        }
        return false;
    }

    private static boolean hasBeenRecognized() {
        if((getAverageConfidence() >= PREDICTION_THRESHOLD
                && getMsBeforeSpeechCounter() >= MILLISECONDS_BEFORE_SPEECH)) {
            reset(getSmartObjectName(), 0.0, 0);
            return true;
        }
        return false;
    }

    private static void reset(String smartObjectName, double averageConfidence, int lastProcessingTimeMs) {
        setSmartObjectName(smartObjectName);
        setAverageConfidence(averageConfidence);
        setMsBeforeSpeechCounter(lastProcessingTimeMs);
    }

    private static void update(double confidence, int lastProcessingTimeMs) {
        double average = (getAverageConfidence() + confidence) / 2;
        int milliseconds = getMsBeforeSpeechCounter() + lastProcessingTimeMs;
        setAverageConfidence(average);
        setMsBeforeSpeechCounter(milliseconds);
    }

}
