package com.polysfactory.scouter;

import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.Face;
import android.hardware.Camera.FaceDetectionListener;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

public class MainActivity extends Activity implements FaceDetectionListener, Callback {

    private SurfaceView surfaceView;
    private FaceView overlayView;
    private Camera camera;

    // private FrameLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        // container = (FrameLayout) findViewById(R.id.container);

        surfaceView = (SurfaceView) findViewById(R.id.camera_view);
        surfaceView.getHolder().addCallback(this);

        overlayView = (FaceView) findViewById(R.id.overlay_view);
    }

    @Override
    public void onFaceDetection(Face[] faces, Camera camera) {
        // Log.d(C.TAG, "onFaceDetection:" + faces.length);
        for (Face face : faces) {
            Log.d(C.TAG, face.rect.toShortString());
            Point mouth = face.mouth;
            Point leftEye = face.leftEye;
            Point rightEye = face.rightEye;
            Log.d(C.TAG, "mouth:" + mouth);
            Log.d(C.TAG, "leftEye:" + leftEye);
            Log.d(C.TAG, "rightEye:" + rightEye);
        }
        overlayView.setFaces(faces);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // TODO Auto-generated method stub

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(C.TAG, "surfaceCreated");
        camera = Camera.open(0);
        try {
            camera.setPreviewDisplay(holder);
            camera.startPreview();
            Parameters parameters = camera.getParameters();
            List<Size> supportedPictureSizes = parameters.getSupportedPictureSizes();
            for (Size size : supportedPictureSizes) {
                Log.d(C.TAG, size.width + "," + size.height);
                if (size.width > 300 && size.width < 640) {
                    parameters.setPreviewSize(size.width, size.height);
                    camera.setParameters(parameters);
                }

            }
            parameters = camera.getParameters();
            Size size = parameters.getPreviewSize();
            Log.d(C.TAG, size.width + "," + size.height);
            // overlayView = new FaceView(this);
            // LayoutParams lp = new LayoutParams(size.width, size.height, Gravity.CENTER);
            // container.addView(overlayView, lp);
        } catch (IOException e) {
            Log.e(C.TAG, "setPreviewDisplay error", e);
        }
        camera.setFaceDetectionListener(this);
        camera.startFaceDetection();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(C.TAG, "surfaceDestroyed");
        camera.stopPreview();
        camera.release();
        camera = null;
    }

}
