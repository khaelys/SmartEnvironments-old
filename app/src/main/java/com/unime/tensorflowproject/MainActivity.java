package com.unime.tensorflowproject;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
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
import java.util.ArrayList;
import java.util.List;

import static android.app.DownloadManager.Request.VISIBILITY_VISIBLE;

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
    private List<Download> downloadsEnqueued;

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

        restoreDownloadsState();

        downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        downloadsEnqueued = new ArrayList<>();
        filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);

        btnDownloadImgRec = (Button) findViewById(R.id.btnDownloadImgRec);
        btnRecognize = (Button) findViewById(R.id.btnRecognize);
        btnRecognize.setEnabled(btnRecognizeState);

        btnDownloadImgRec.setOnClickListener((view) -> {
            Log.d(TAG, "downloadURL: Starting Async Task");
            downloadImgRecNN = new DownloadData(fileNameImgRecNN);
            downloadImgRecLabels = new DownloadData(fileNameImgRecLabels);

            if(downloadImgRecNN.mustBeDownloaded()) {
                downloadImgRecNN.execute(urlImgRecNN, fileNameImgRecNN);
            }
            if(downloadImgRecLabels.mustBeDownloaded()) {
                downloadImgRecLabels.execute(urlImgRecLabels, fileNameImgRecLabels);
            }
            if(imgRecLabelsState && imgRecNNState) {
                updateButtons();
            }
        });

        /** switch to ClassifierActivity */
        btnRecognize.setOnClickListener((view) -> {
            Log.d(TAG, "onClickListener: Recognize Button" );
            Intent intent = new Intent(this, ClassifierActivity.class);
            startActivity(intent);
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

        if(btnDownloadImgRecState == false) {
            btnDownloadImgRec.setEnabled(btnDownloadImgRecState);
            btnDownloadImgRec.setBackgroundColor(Color.parseColor("#FF3F51B5"));
        }
        if(btnRecognizeState == true) {
            btnRecognize.setEnabled(btnRecognizeState);
            btnRecognize.setBackgroundColor(Color.parseColor("#FFFF4081"));
        }
    }

    @Override
    protected void onDestroy() {
        saveDownloadsState();
        super.onDestroy();
    }

    /** Define an AsyncTask to download data */
    private class DownloadData extends AsyncTask<String, Void, Boolean> {
        private static final String TAG = "DownloadData";
        private String fileName;

        public DownloadData(String fileName) {
            this.fileName = fileName;
        }

        public boolean mustBeDownloaded() {
            File fileObject = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "");
            String[] files = fileObject.list();
            if(files != null){
                for(String file : files) {
                    if(file.equals(fileName)) {
                        if(fileName.equals(fileNameImgRecLabels)) {
                            imgRecLabelsState = true;
                        } else if (fileName.equals(fileNameImgRecNN)) {
                            imgRecNNState = true;
                        }
                        return false;
                    }
                }
            }
            return true;
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

                Download download = new Download();
                download.setDownloadId(referenceId);
                download.setDownlaodObjectType(Download.getEnum(fileName));
                downloadsEnqueued.add(download);
            } catch(Exception e) {
                Log.e(TAG, "doInBackground: Error " + e.getMessage());
                return false;
            }
            return true;
        }
    }

    public boolean checkStatus(Long downloadId) {
        boolean status = false;
        DownloadManager.Query queryDownload = new DownloadManager.Query();

        if(downloadId == null) {
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

    public boolean DownloadStatus(Cursor cursor) {
        //column for download  status
        int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
        int status = cursor.getInt(columnIndex);
        // il controllo non basta nel caso di download con i link diretti, che anche se non validi
        // scaricano comunque la pagina web.
        switch (status) {
            case DownloadManager.STATUS_FAILED:
                toaster("Download failed");
                break;
            case DownloadManager.STATUS_PAUSED:
                toaster("Download paused");
            case DownloadManager.STATUS_SUCCESSFUL:
                return true;
        }
        return false;
    }

    // TODO: check if downloads can be simultaneously capture from the BroadcastReceiver
    private BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Download downloadedFile = new Download();
            // update the status of the downloaded file which sent this Intent.
            downloadsEnqueued.stream().filter((download) -> download.getStatus().equals("false")).
                    filter(download->checkStatus(download.getDownloadId())).forEach(download -> {
                        downloadedFile.setDownloadId(download.getDownloadId());
                Log.d(TAG, "debug: checkstatus " + checkStatus(downloadedFile.getDownloadId()));
                        download.setStatus("true");
                    });
            Log.d(TAG, "debug: =================" + downloadedFile);

            buttonMustBeUpdated();

            Toast toast;
            if(checkStatus(downloadedFile.getDownloadId())) {
                toast = Toast.makeText(context,
                        "Download Completed", Toast.LENGTH_LONG);
            }
            else {
                toast = Toast.makeText(context,
                        "Download Failed", Toast.LENGTH_LONG);
            }
            toast.setGravity(Gravity.BOTTOM, 25, 400);
            toast.show();
        }
    };

    private void toaster(String message) {
        Log.d(TAG, "toaster: start");
        Toast toast = Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.BOTTOM, 25, 400);
        toast.show();
    }

    private void buttonMustBeUpdated() {
        List<Download> downloadedFile = new ArrayList<>();

        downloadsEnqueued.stream().filter((download) -> (download.getDownlaodObjectType()==Download.DownloadObjectType.LABELS_IMG_REC) ||
                (download.getDownlaodObjectType()==Download.DownloadObjectType.NEURAL_NETWORK_IMG_REC)).
                filter( download -> download.getStatus().equals("true")).forEach(downloadedFile::add);

        if(downloadedFile.size() > 1){
            if(downloadedFile.get(0).getStatus().equals("true") && downloadedFile.get(1).getStatus().equals("true")) {
                Log.d(TAG, "buttonMustBeUpdated: true");
                updateButtons();
            }
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

    private void saveDownloadsState() {
        Log.d(TAG, "saveDownloadsState: start");
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        for(int i = 0; i < downloadsEnqueued.size(); i++) {
            editor.putLong(downloadsEnqueued.get(i).getDownlaodObjectType().toString(), downloadsEnqueued.get(i).getDownloadId());
        }
        editor.apply();
    }

    private void restoreDownloadsState() {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        boolean defaultValue = false;
        Long downloadIdImgRecNN = null;
        Long downloadIdImgRecLabels = null;
        downloadIdImgRecNN = sharedPref.getLong(Download.DownloadObjectType.NEURAL_NETWORK_IMG_REC.toString());

    }


}
