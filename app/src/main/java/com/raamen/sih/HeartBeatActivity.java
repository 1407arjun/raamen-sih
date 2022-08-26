package com.raamen.sih;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class HeartBeatActivity extends AppCompatActivity {

    private SurfaceView preview = null;
    private static SurfaceHolder previewHolder = null;
    private static Camera camera = null;
    private static PowerManager.WakeLock wakeLock = null;

    private static long startTime = 0;
    private ProgressBar progress;
    public int ProgP = 0;
    public int inc = 0;
    public int counter = 0;
    public boolean complete = false;

    public ArrayList<Double> AvgList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preview = findViewById(R.id.preview);
        previewHolder = preview.getHolder();
        previewHolder.addCallback(surfaceCallback);
        previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        progress = findViewById(R.id.HRPB);
        progress.setProgress(0);

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, ":DoNotDimScreen");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onResume() {
        super.onResume();
        wakeLock.acquire();
        camera = Camera.open();
        camera.setDisplayOrientation(90);
        startTime = System.currentTimeMillis();
    }

    @Override
    public void onPause() {
        super.onPause();
        wakeLock.release();
        camera.setPreviewCallback(null);
        camera.stopPreview();
        camera.release();
        camera = null;
    }

    public boolean isPeak(double arr[], int n, double num, int i, int j)
    {
        if (i >= 0 && arr[i] > num)
            return false;

        if (j < n && arr[j] > num)
            return false;
        return true;
    }

    public int printPeaksTroughs(double arr[], int n)
    {
        int count=0;
        //System.out.print("Peaks : ");

        for (int i = 0; i < n; i++)
        {
            if (isPeak(arr, n, arr[i], i - 1, i + 1))
            {
                //System.out.println(arr[i]);
                count++;
            }
        }
        return count;
    }

    private final Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {

        /**
         * {@inheritDoc}
         */
        @Override
        public void onPreviewFrame(byte[] data, Camera cam) {
            if (data == null) throw new NullPointerException();
            Camera.Size size = cam.getParameters().getPreviewSize();
            if (size == null) throw new NullPointerException();

            if (!complete) {

                int width = size.width;
                int height = size.height;

                double avg = ImageProcessing.redAverage(data.clone(), height, width);

                if (avg < 200) {
                    Toast.makeText(HeartBeatActivity.this, "Retrying measurement", Toast.LENGTH_SHORT).show();
                } else {
                    AvgList.add(avg);
                }

                long endTime = System.currentTimeMillis();
                double totalTimeInSecs = (endTime - startTime) / 1000d;
                if (totalTimeInSecs >= 15) {
                    complete = true;
                    Log.i("helloavg", AvgList.toString());

                    double[] arr = new double[AvgList.size()];

                    for (int i = 0; i < AvgList.size(); i++) {
                        arr[i] = AvgList.get(i);
                    }

                    int peaks = printPeaksTroughs(arr, arr.length);
                    Log.i("hellopeak", Integer.toString(peaks));


                    Intent intent = new Intent(HeartBeatActivity.this, ResultActivity.class);
                    intent.putExtra("name", "Heart Rate");
                    intent.putExtra("score", peaks < 50 ? (double) -1 : (double) peaks);
                    intent.putExtra("normal", "60 - 100");
                    startActivity(intent);
                    finish();


                }
                ++counter;

                ProgP = inc++ / 16;
                progress.setProgress(ProgP);

            }
        }
    };

    private final SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                camera.setPreviewDisplay(previewHolder);
                camera.setPreviewCallback(previewCallback);
            } catch (Throwable t) {
                Log.e("PreviewDemo-surfaceCallback", "Exception in setPreviewDisplay()", t);
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            Camera.Parameters parameters = camera.getParameters();
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);

            Camera.Size size = getSmallestPreviewSize(width, height, parameters);
//            if (size != null) {
//                parameters.setPreviewSize(size.width, size.height);
//                Log.d(TAG, "Using width=" + size.width + " height=" + size.height);
//            }

            camera.setParameters(parameters);
            camera.startPreview();
        }


        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
        }
    };

    private static Camera.Size getSmallestPreviewSize(int width, int height, Camera.Parameters parameters) {
        Camera.Size result = null;
        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
            if (size.width <= width && size.height <= height) {
                if (result == null) {
                    result = size;
                } else {
                    int resultArea = result.width * result.height;
                    int newArea = size.width * size.height;
                    if (newArea < resultArea) result = size;
                }
            }
        }
        return result;
    }
}