package com.unime.tensorflowproject;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;

import static android.app.DownloadManager.Request.VISIBILITY_VISIBLE;
import static com.unime.tensorflowproject.Download.DownloadObjectType.LABELS_IMG_REC;
import static com.unime.tensorflowproject.Download.DownloadObjectType.LABELS_IMG_REC_STATUS;
import static com.unime.tensorflowproject.Download.DownloadObjectType.NEURAL_NETWORK_IMG_REC;
import static com.unime.tensorflowproject.Download.DownloadObjectType.NEURAL_NETWORK_IMG_REC_STATUS;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private final static String urlImgRecNN = "http://smartenvironment.altervista.org/tensorflow_inception_graph.pb";
    private final static String urlImgRecLabels = "http://smartenvironment.altervista.org/imagenet_comp_graph_label_strings.txt";

    private final static String fileNameImgRecNN = "tensorflow_inception_graph.pb";
    private final static String fileNameImgRecLabels = "imagenet_comp_graph_label_strings.txt";

    private static final String DOWNLOAD_IMG_REC_STATE = "ButtonDownloadImgRecState";
    private static final String RECOGNIZE_STATE = "ButtonRecognizeState";

    private DownloadManager downloadManager;
    private DownloadData downloadImgRecNN;
    private DownloadData downloadImgRecLabels;
    private IntentFilter filter;

    private Button btnDownloadImgRec;
    private Button btnRecognize;
    private Boolean btnDownloadImgRecState = true;
    private Boolean btnRecognizeState = false;

    protected Boolean imgRecLabelsState = false;
    protected Boolean imgRecNNState = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: MainActivity start");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);

        btnDownloadImgRec = (Button) findViewById(R.id.btnDownloadImgRec);
        btnRecognize = (Button) findViewById(R.id.btnRecognize);

        // check if an useful file hasn't been eliminated
        if (!mustBeDownloaded(fileNameImgRecNN) && !mustBeDownloaded(fileNameImgRecLabels)) {
            restoreDownloadsState();
            buttonMustBeUpdated();
        }

        btnRecognize.setEnabled(btnRecognizeState);

        btnDownloadImgRec.setOnClickListener((view) -> {
            Log.d(TAG, "downloadURL: Starting Async Task");


            downloadImgRecNN = new DownloadData(fileNameImgRecNN);
            downloadImgRecLabels = new DownloadData(fileNameImgRecLabels);

            // Checking for network status and if the file must be downloaded.
            // If all is ok, we can download our files.
            if (mustBeDownloaded(downloadImgRecNN.getFileName()) && isNetworkAvailable()) {
                downloadImgRecNN.execute(urlImgRecNN, fileNameImgRecNN);
            }
            if (mustBeDownloaded(downloadImgRecLabels.getFileName()) && isNetworkAvailable()) {
                downloadImgRecLabels.execute(urlImgRecLabels, fileNameImgRecLabels);
            }
            if (imgRecLabelsState && imgRecNNState) {
                updateButtons();
            }
        });

        /** switch to ClassifierActivity */
        btnRecognize.setOnClickListener((view) -> {
            Log.d(TAG, "onClickListener: Recognize Button");
            if (mustBeDownloaded(fileNameImgRecLabels) || mustBeDownloaded(fileNameImgRecNN)) {
                btnDownloadImgRecState = true;
                btnDownloadImgRec.setEnabled(btnRecognizeState);
                btnDownloadImgRec.setBackgroundColor(Color.parseColor("#FFFF4081"));
                btnRecognizeState = false;
                btnRecognize.setEnabled(btnRecognizeState);
                btnRecognize.setBackgroundColor(Color.parseColor("#FF3F51B5"));
            } else {
                Intent intent = new Intent(this, ClassifierActivity.class);
                startActivity(intent);
            }

        });
        Log.d(TAG, "onCreate: MainActivity end");
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume: start");
        super.onResume();
        // Register the broadcast receiver.
        registerReceiver(downloadReceiver, filter);
        Log.d(TAG, "onResume: end");
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause: start");
        // Unregister the receiver
        unregisterReceiver(downloadReceiver);
        super.onPause();
        Log.d(TAG, "onPause: end");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(DOWNLOAD_IMG_REC_STATE, btnDownloadImgRecState);
        outState.putBoolean(RECOGNIZE_STATE, btnRecognizeState);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        btnDownloadImgRecState = savedInstanceState.getBoolean(DOWNLOAD_IMG_REC_STATE);
        btnRecognizeState = savedInstanceState.getBoolean(RECOGNIZE_STATE);

        if (btnDownloadImgRecState == false) {
            btnDownloadImgRec.setEnabled(btnDownloadImgRecState);
            btnDownloadImgRec.setBackgroundColor(Color.parseColor("#FF3F51B5"));
        }
        if (btnRecognizeState == true) {
            btnRecognize.setEnabled(btnRecognizeState);
            btnRecognize.setBackgroundColor(Color.parseColor("#FFFF4081"));
        }
    }

    @Override
    protected void onDestroy() {
        saveDownloadsState(Download.DownloadObjectType.NEURAL_NETWORK_IMG_REC_STATUS, imgRecNNState);
        saveDownloadsState(Download.DownloadObjectType.LABELS_IMG_REC_STATUS, imgRecLabelsState);
        Log.d(TAG, "onDestroy: vediamo " + imgRecNNState + imgRecLabelsState);
        super.onDestroy();
    }

    /**
     * Define an AsyncTask to download data
     */
    private class DownloadData extends AsyncTask<String, Void, Boolean> {
        private static final String TAG = "DownloadData";
        private String fileName;

        public DownloadData(String fileName) {
            this.fileName = fileName;
        }

        public String getFileName() {
            return fileName;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            Log.d(TAG, "doInBackground: starts with " + strings[0]);

            String url = strings[0];
            String fileName = strings[1];

            try {
                Uri uri = Uri.parse(url);

                DownloadManager.Request request = new DownloadManager.Request(uri);

                //Setting visibility of request
                request.setNotificationVisibility(VISIBILITY_VISIBLE);

                //Set the local destination for the downloaded file to a path
                //within the application's external files directory
                request.setDestinationInExternalFilesDir(MainActivity.this,
                        Environment.DIRECTORY_DOWNLOADS, fileName);

                // Enqueue download and save into referenceId
                Long referenceId = downloadManager.enqueue(request);

                saveDownloadsState(Download.getEnum(fileName), referenceId);
                saveDownloadsState(Download.getEnum(fileName + "_STATUS"), false);
            } catch (Exception e) {
                Log.e(TAG, "doInBackground: Error " + e.getMessage());
                return false;
            }
            return true;
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    private boolean mustBeDownloaded(String fileName) {
        File fileObject = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "");
        String[] files = fileObject.list();

        if (files != null) {
            for (String file : files) {
                if (file.equals(fileName)) {
                    if (fileName.equals(fileNameImgRecLabels)) {
                        if (isDownloadedFileUpdated(LABELS_IMG_REC)) {
                            imgRecLabelsState = true;
                            saveDownloadsState(LABELS_IMG_REC_STATUS, imgRecLabelsState);
                        }
                    } else if (fileName.equals(fileNameImgRecNN)) {
                        if (isDownloadedFileUpdated(NEURAL_NETWORK_IMG_REC)) {
                            Log.d(TAG, "mustBeDownloaded: imgRecNNState = " + imgRecNNState);
                            imgRecNNState = true;
                            saveDownloadsState(NEURAL_NETWORK_IMG_REC_STATUS, imgRecNNState);
                        }
                    }
                    return false;
                }
            }
        }
        return true;
    }

    public String checkStatus(Long downloadId) {
        String status = "";
        DownloadManager.Query queryDownload = new DownloadManager.Query();

        if (downloadId == null) {
            return status;
        }

        //set the query filter to our previously Enqueued download
        queryDownload.setFilterById(downloadId);

        //Query the download manager about downloads that have been requested.

        Cursor cursor = downloadManager.query(queryDownload);

        if (cursor.moveToFirst()) {
            status = DownloadStatus(cursor);
        }

        return status;
    }

    public String DownloadStatus(Cursor cursor) {
        //column for download  status
        int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
        int status = cursor.getInt(columnIndex);
        // il controllo non basta nel caso di download con i link diretti, che anche se non validi
        // scaricano comunque la pagina web.
        String statusText = "";

        switch (status) {
            case DownloadManager.STATUS_FAILED:
                statusText = "STATUS_FAILED";
                break;
            case DownloadManager.STATUS_PAUSED:
                statusText = "STATUS_PAUSED";
                break;
            case DownloadManager.STATUS_PENDING:
                statusText = "STATUS_PENDING";
                break;
            case DownloadManager.STATUS_RUNNING:
                statusText = "STATUS_RUNNING";
                break;
            case DownloadManager.STATUS_SUCCESSFUL:
                statusText = "STATUS_SUCCESSFUL";
        }
        return statusText;
    }

    private BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long referenceId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

            Download.DownloadObjectType downloadObjectType = getDownloadType(referenceId);
            Log.d(TAG, "onReceive: " + getDownloadType(referenceId) + " " + referenceId);

            showToasterOnReceive(referenceId, downloadObjectType);

            try {
                assert downloadObjectType != null;
                switch (downloadObjectType) {
                    case NEURAL_NETWORK_IMG_REC:
                        saveDownloadsState(NEURAL_NETWORK_IMG_REC_STATUS, imgRecNNState);
                        break;
                    case LABELS_IMG_REC:
                        saveDownloadsState(LABELS_IMG_REC_STATUS, imgRecLabelsState);
                        break;
                    default:
                        Log.d(TAG, "onReceive: Bug detected");
                }
            } catch (NullPointerException e) {
                Log.e(TAG, "onReceive: " + e.getMessage());
            }

            buttonMustBeUpdated();
        }
    };

    /**
     * Search download type given the downloadId
     */
    private Download.DownloadObjectType getDownloadType(Long referenceId) {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);

        long imgRecNeuralNetworkId = sharedPref.getLong(NEURAL_NETWORK_IMG_REC.toString(), -1);
        long imgRecLabelsId = sharedPref.getLong(LABELS_IMG_REC.toString(), -1);

        Log.d(TAG, "getDownloadType: " + "imgRecLabelsId = " + imgRecLabelsId + " | imgRecNeuralNetworkId = " +
                imgRecNeuralNetworkId + " | referenceId = " + referenceId);

        if (referenceId == imgRecNeuralNetworkId) {
            return NEURAL_NETWORK_IMG_REC;
        }
        if (referenceId == imgRecLabelsId) {
            return LABELS_IMG_REC;
        }

        return null;
    }

    private long getDownloadId(Download.DownloadObjectType downloadObjectType) {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);

        long referenceId = sharedPref.getLong(NEURAL_NETWORK_IMG_REC.toString(), -1);

        return referenceId;

    }

    private boolean isDownloadedFileUpdated(Download.DownloadObjectType downloadObjectType) {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);

        boolean isUpdated = sharedPref.getBoolean(downloadObjectType.toString() + "_STATUS", false);
        Log.d(TAG, "isDownloadedFileUpdated: " + isUpdated);

        if (!isUpdated && checkStatus(getDownloadId(downloadObjectType)).equals("STATUS_SUCCESSFUL")) {
            isUpdated = true;
            Log.d(TAG, "isDownloadedFileUpdated: " + checkStatus(getDownloadId(downloadObjectType)));
        }
        return isUpdated;

    }

    private void toaster(String message) {
        Log.d(TAG, "toaster: start");
        Toast toast = Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.BOTTOM, 25, 400);
        toast.show();
    }

    private void showToasterOnReceive(long referenceId, Download.DownloadObjectType downloadObjectType) {
        if (checkStatus(referenceId).equals("STATUS_SUCCESSFUL")) {
            switch (downloadObjectType) {
                case NEURAL_NETWORK_IMG_REC:
                    imgRecNNState = true;
                    toaster("Download Neural Network Completed");
                    break;
                case LABELS_IMG_REC:
                    imgRecLabelsState = true;
                    toaster("Download Labels Completed");
                    break;
                default:
                    Log.d(TAG, "onReceive: Bug detected");
            }
        } else {
            switch (downloadObjectType) {
                case NEURAL_NETWORK_IMG_REC:
                    toaster("Download Neural Network Failed");
                    break;
                case LABELS_IMG_REC:
                    toaster("Download Labels Failed");
                    break;
                default:
                    Log.d(TAG, "onReceive: Bug detected");
            }
        }
    }

    private void buttonMustBeUpdated() {
        if (imgRecNNState && imgRecLabelsState) {
            Log.d(TAG, "buttonMustBeUpdated: true");
            updateButtons();
        }
    }

    private void updateButtons() {
        btnDownloadImgRecState = false;
        btnDownloadImgRec.setEnabled(btnDownloadImgRecState);
        btnDownloadImgRec.setBackgroundColor(Color.parseColor("#FF3F51B5"));
        btnRecognizeState = true;
        btnRecognize.setEnabled(btnRecognizeState);
        btnRecognize.setBackgroundColor(Color.parseColor("#FFFF4081"));
    }

    private void saveDownloadsState(Download.DownloadObjectType downloadObjectType, Long downloadId) {
        Log.d(TAG, "saveDownloadsState: start");
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putLong(downloadObjectType.toString(), downloadId);
        Log.d(TAG, "saveDownloadsState: " + downloadObjectType + "   " + downloadId);

        editor.apply();
    }

    private void saveDownloadsState(Download.DownloadObjectType downloadObjectType, Boolean status) {
        Log.d(TAG, "saveDownloadsState: start");
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putBoolean(downloadObjectType.toString(), status);
        Log.d(TAG, "saveDownloadsState: " + downloadObjectType + "   " + status);

        editor.apply();
    }

    private void restoreDownloadsState() {
        Log.d(TAG, "restoreDownloadsState: start");
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);

        imgRecNNState = sharedPref.getBoolean(NEURAL_NETWORK_IMG_REC_STATUS.toString(), false);
        imgRecLabelsState = sharedPref.getBoolean(LABELS_IMG_REC_STATUS.toString(), false);

        Log.d(TAG, "Image Recognition Neural Network State: " + imgRecNNState);
        Log.d(TAG, "Image Recognition Labels State: " + imgRecLabelsState);

        Log.d(TAG, "restoreDownloadsState: end");
    }
}
