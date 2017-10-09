package com.unime.tensorflowproject;

import android.util.Log;

import static android.content.ContentValues.TAG;

public class Download {
    private Long downloadId;
    private DownloadObjectType downlaodObjectType;
    private String status = "false";  // stato del download effettuato??

    public enum DownloadObjectType {
        NEURAL_NETWORK_IMG_REC,
        LABELS_IMG_REC
    }

    public Long getDownloadId() {
        return downloadId;
    }

    public void setDownloadId(Long downloadId) {
        this.downloadId = downloadId;
    }

    public DownloadObjectType getDownlaodObjectType() {
        return downlaodObjectType;
    }

    public void setDownlaodObjectType(DownloadObjectType downlaodObjectType) {
        this.downlaodObjectType = downlaodObjectType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    @Override
    public String toString() {
        return "\n" + this.getDownloadId() + "\n" + this.getDownlaodObjectType() + "\n" +
                this.getStatus() + "\n";
    }
}
