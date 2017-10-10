package com.unime.tensorflowproject;

import android.util.Log;

import static android.content.ContentValues.TAG;

public class Download {

    public enum DownloadObjectType {
        NEURAL_NETWORK_IMG_REC,
        LABELS_IMG_REC,
        NEURAL_NETWORK_IMG_REC_STATUS,
        LABELS_IMG_REC_STATUS
    }

    public static DownloadObjectType getEnum(String fileName) {
        switch(fileName) {
            case "tensorflow_inception_graph.pb":
                return DownloadObjectType.NEURAL_NETWORK_IMG_REC;
            case "imagenet_comp_graph_label_strings.txt":
                return DownloadObjectType.LABELS_IMG_REC;
            default:
                Log.e(TAG, "getEnum: No Match!");
        }
        return null;
    }
}
