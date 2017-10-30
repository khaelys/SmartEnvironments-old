package com.unime.tensorflowproject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.SystemClock;
import android.util.Log;
import android.util.Size;
import android.util.TypedValue;
import android.view.Display;

import com.unime.tensorflowproject.OverlayView.DrawCallback;
import com.unime.tensorflowproject.audio.SpeechRecognitionService;
import com.unime.tensorflowproject.env.BorderedText;
import com.unime.tensorflowproject.env.ImageUtils;
import com.unime.tensorflowproject.env.Logger;
import com.unime.tensorflowproject.utilities.SpeechRecognitionTrigger;

import java.util.List;
import java.util.Vector;

public class ClassifierActivity extends CameraActivity implements OnImageAvailableListener {
    private static final Logger LOGGER = new Logger();

    protected static final boolean SAVE_PREVIEW_BITMAP = false;
    private static final String TAG = "ClassifierActivity";

    private ResultsView resultsView;

    private Bitmap rgbFrameBitmap = null;
    private Bitmap croppedBitmap = null;
    private Bitmap cropCopyBitmap = null;

    private long lastProcessingTimeMs;

    private static final int INPUT_SIZE = 224;
    private static final int IMAGE_MEAN = 128;
    private static final float IMAGE_STD = 128.0f;
    private static final String INPUT_NAME = "input";
    private static final String OUTPUT_NAME = "final_result";

    private final String pathName = "/storage/emulated/0/Android/data/com.unime.tensorflowproject/files/Download/";

    private final String MODEL_FILE = pathName + "tensorflow_inception_graph.pb";
    private final String LABEL_FILE = pathName + "imagenet_comp_graph_label_strings.txt";

    private static final boolean MAINTAIN_ASPECT = true;

    private static final Size DESIRED_PREVIEW_SIZE = new Size(640, 480);

    private Integer sensorOrientation;
    private Classifier classifier;
    private Matrix frameToCropTransform;
    private Matrix cropToFrameTransform;

    private BorderedText borderedText;

    private Intent mSpeechIntentService;
    private boolean commandCanBeStarted = true;

    private int timeCounter = 0;
    private double previousConfidence = 0.0;


    @Override
    protected Size getDesiredPreviewFrameSize() {
        return DESIRED_PREVIEW_SIZE;
    }

    private static final float TEXT_SIZE_DIP = 10;

    @Override
    protected int getLayoutId() {
        return R.layout.camera_connection_fragment;
    }

    @Override
    public void onPreviewSizeChosen(final Size size, final int rotation) {
        final float textSizePx = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
        borderedText = new BorderedText(textSizePx);
        borderedText.setTypeface(Typeface.MONOSPACE);

        classifier =
                TensorFlowImageClassifier.create(
                        getAssets(),
                        MODEL_FILE,
                        LABEL_FILE,
                        INPUT_SIZE,
                        IMAGE_MEAN,
                        IMAGE_STD,
                        INPUT_NAME,
                        OUTPUT_NAME);

        previewWidth = size.getWidth();
        previewHeight = size.getHeight();

        final Display display = getWindowManager().getDefaultDisplay();
        final int screenOrientation = display.getRotation();

        LOGGER.i("Sensor orientation: %d, Screen orientation: %d", rotation, screenOrientation);

        sensorOrientation = rotation + screenOrientation;

        LOGGER.i("Initializing at size %dx%d", previewWidth, previewHeight);
        rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Config.ARGB_8888);
        croppedBitmap = Bitmap.createBitmap(INPUT_SIZE, INPUT_SIZE, Config.ARGB_8888);

        frameToCropTransform = ImageUtils.getTransformationMatrix(
                previewWidth, previewHeight,
                INPUT_SIZE, INPUT_SIZE,
                sensorOrientation, MAINTAIN_ASPECT);

        cropToFrameTransform = new Matrix();
        frameToCropTransform.invert(cropToFrameTransform);

        addCallback(
                new DrawCallback() {
                    @Override
                    public void drawCallback(final Canvas canvas) {
                        renderDebug(canvas);
                    }
                });
    }

    @Override
    protected void processImage() {
        rgbFrameBitmap.setPixels(getRgbBytes(), 0, previewWidth, 0, 0, previewWidth, previewHeight);
        final Canvas canvas = new Canvas(croppedBitmap);
        canvas.drawBitmap(rgbFrameBitmap, frameToCropTransform, null);

        // For examining the actual TF input.
        if (SAVE_PREVIEW_BITMAP) {
            ImageUtils.saveBitmap(croppedBitmap);
        }

        runInBackground(
                new Runnable() {
                    @Override
                    public void run() {
                        final long startTime = SystemClock.uptimeMillis();
                        final List<Classifier.Recognition> results = classifier.recognizeImage(croppedBitmap);
                        lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;
                        Log.d(TAG, "run: lastProcessingTimeMs = " + lastProcessingTimeMs);
                        LOGGER.i("Detect: %s", results);
                        cropCopyBitmap = Bitmap.createBitmap(croppedBitmap);
                        if (resultsView == null) {
                            resultsView = (ResultsView) findViewById(R.id.results);
                        }
                        String prediction = results.get(0).getTitle();
                        //String prediction = "lamp";
                        double confidence = results.get(0).getConfidence();


                        // check if the speech regonition has to been Triggered
                        // if(results.get(0).getConfidence() > 0.90 && commandCanBeStarted) {
                        if(SpeechRecognitionTrigger.hasToBeTriggered(prediction, confidence, lastProcessingTimeMs) && commandCanBeStarted) {
                            commandCanBeStarted = false;
                            trySpeech(prediction);
                        }

                        resultsView.setResults(results);
                        requestRender();
                        readyForNextImage();
                    }
                });
    }

    public void trySpeech(String smartObjectName) {
        mSpeechIntentService = new Intent(this, SpeechRecognitionService.class);
        mSpeechIntentService.putExtra("SmartObject", smartObjectName);
        startService(mSpeechIntentService);
    }

    @Override
    public void onSetDebug(boolean debug) {
        classifier.enableStatLogging(debug);
    }

    private void renderDebug(final Canvas canvas) {
        if (!isDebug()) {
            return;
        }
        final Bitmap copy = cropCopyBitmap;
        if (copy != null) {
            final Matrix matrix = new Matrix();
            final float scaleFactor = 2;
            matrix.postScale(scaleFactor, scaleFactor);
            matrix.postTranslate(
                    canvas.getWidth() - copy.getWidth() * scaleFactor,
                    canvas.getHeight() - copy.getHeight() * scaleFactor);
            canvas.drawBitmap(copy, matrix, new Paint());

            final Vector<String> lines = new Vector<String>();
            if (classifier != null) {
                String statString = classifier.getStatString();
                String[] statLines = statString.split("\n");
                for (String line : statLines) {
                    lines.add(line);
                }
            }

            lines.add("Frame: " + previewWidth + "x" + previewHeight);
            lines.add("Crop: " + copy.getWidth() + "x" + copy.getHeight());
            lines.add("View: " + canvas.getWidth() + "x" + canvas.getHeight());
            lines.add("Rotation: " + sensorOrientation);
            lines.add("Inference time: " + lastProcessingTimeMs + "ms");

            borderedText.drawLines(canvas, 10, canvas.getHeight() - 10, lines);
        }
    }











    @Override
    public synchronized void onDestroy() {
        if(mSpeechIntentService != null) {
            stopService(mSpeechIntentService);
        }
        super.onDestroy();
    }
}
