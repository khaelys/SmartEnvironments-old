package com.unime.tensorflowproject;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static android.app.DownloadManager.Request.VISIBILITY_VISIBLE;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private final static String urlImgRecNN = "http://download1321.mediafireuserdownload.com/b93xt61zp1lg/k206atr6s0rsp68/tensorflow_inception_graph.pb";
    private final static String urlImgRecLabels = "http://download1477.mediafireuserdownload.com/ns2apr8x4seg/ypdqi09v2d8d8y0/imagenet_comp_graph_label_strings.txt";

    private final static String fileNameImgRecNN = "tensorflow_inception_graph.pb";
    private final static String fileNameImgRecLabels = "imagenet_comp_graph_label_strings.txt";

    private DownloadManager downloadManager;
    private IntentFilter filter;
    private List<Download> downloadsEnqueued;

    private ImageButton downloadImgRec;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        downloadsEnqueued = new ArrayList<>();
        filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);

        downloadImgRec = (ImageButton) findViewById(R.id.downloadImgRec);

        downloadImgRec.setOnClickListener((view) -> {
            Log.d(TAG, "downloadURL: Starting Async Task");
            DownloadData downloadImgRecNN = new DownloadData();
            downloadImgRecNN.execute(urlImgRecNN, fileNameImgRecNN);
            DownloadData downloadImgRecLabels = new DownloadData();
            downloadImgRecLabels.execute(urlImgRecLabels, fileNameImgRecLabels);
            Log.d(TAG, "downloadURL: done");
        });
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

    private class DownloadData extends AsyncTask<String, Void, Boolean> {
        private static final String TAG = "DownloadData";

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
                download.setDownloadID(referenceId);
                download.setDownlaodObjectType(Download.getEnum(fileName));
                downloadsEnqueued.add(download);
            } catch(Exception e) {
                Log.e(TAG, "doInBackground: Error " + e.getMessage());
                return false;
            }
            return true;
        }
    }

    public boolean checkStatus(long DownloadId) {
        boolean status = false;
        DownloadManager.Query queryDownload = new DownloadManager.Query();
        //set the query filter to our previously Enqueued download
        queryDownload.setFilterById(DownloadId);

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

        switch (status) {
            case DownloadManager.STATUS_SUCCESSFUL:
                return true;
        }
        return false;
    }

    private BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // update the status of the downloaded file which send this Intent.
            downloadsEnqueued.stream().filter((download) -> download.getStatus().equals("false")).
                    filter(download->checkStatus(download.getDownloadID())).forEach(download -> download.setStatus("true"));

//            if(checkStatus(downloadsEnqueued.get(0).getDownloadID()))
//                Log.d(TAG, "onReceive: Download Complete " + downloadsEnqueued.get(0).getStatus());
//            else
//                Log.d(TAG, "onReceive: complete image download " + downloadsEnqueued.get(1).getStatus());

            

            Toast toast = Toast.makeText(context,
                    "Download Complete", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.BOTTOM, 25, 400);
            toast.show();
        }
    };
}
