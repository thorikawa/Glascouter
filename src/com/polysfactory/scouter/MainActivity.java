package com.polysfactory.scouter;

import java.io.File;
import java.util.List;

import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;

import com.polysfactory.scouter.jni.ScouterProcessor;
import com.polysfactory.scouter.view.MyCameraView;
import com.polysfactory.scouter.view.TriangleView;

public class MainActivity extends Activity implements CvCameraViewListener2 {

    private MyCameraView mCameraView;
    private ScouterProcessor scouterProcessor;
    private ScouterSound scouterSound;
    private FrameLayout container;
    private TextView tv;
    private ImageView sightCircle;
    private TriangleView leftTriangle;
    private TriangleView bottomTriangle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        mCameraView = (MyCameraView) findViewById(R.id.camera_view);
        mCameraView.setCvCameraViewListener(this);
        mCameraView.setCameraIndex(C.CAMERA_INDEX);

        mCameraView.setMaxFrameSize(400, 240);
        mCameraView.enableView();

        File faceCascadeFile = IOUtils.getFilePath(this, "cascade", "face.xml");
        IOUtils.copy(this, R.raw.haarcascade_frontalface_default, faceCascadeFile);

        File noseCascadeFile = IOUtils.getFilePath(this, "cascade", "nose.xml");
        IOUtils.copy(this, R.raw.haarcascade_mcs_nose, noseCascadeFile);

        scouterProcessor = new ScouterProcessor(faceCascadeFile.getAbsolutePath(), noseCascadeFile.getAbsolutePath());

        scouterSound = new ScouterSound(this);

        container = (FrameLayout) findViewById(R.id.container);
        tv = (TextView) findViewById(R.id.text);
        Typeface font = Typeface.createFromAsset(getAssets(), "font/digital_regular.ttf");
        tv.setTypeface(font);

        sightCircle = (ImageView) findViewById(R.id.sight_circle_view);
        leftTriangle = (TriangleView) findViewById(R.id.left_triangle);
        bottomTriangle = (TriangleView) findViewById(R.id.bottom_triangle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    int count = 0;

    Mat captured;
    float mScale;
    int cameraViewOffsetX = 0;
    int cameraViewOffsetY;

    @Override
    public Mat onCameraFrame(CvCameraViewFrame frame) {
        Mat rgba = frame.rgba();

        MatOfRect faceRectMat = new MatOfRect();
        // TODO: allow some error attempts
        scouterProcessor.process(rgba, faceRectMat);

        if (state == State.WILL_START_SCAN) {
            List<Rect> faceRectList = faceRectMat.toList();
            if (faceRectList.size() > 0) {
                Rect faceRect = faceRectList.get(0);
                Log.d(C.TAG, faceRect.toString());
                state = State.SCAN_DISPLAYING;
                captured = rgba.clone();
                startDisplaying(faceRect);
            }
        }
        return rgba;
    }

    @Override
    public void onCameraViewStarted(int w, int h) {
        mScale = mCameraView.getScale();
        cameraViewOffsetX = (mCameraView.getWidth() - (int) (w * mScale)) / 2;
        cameraViewOffsetY = (mCameraView.getHeight() - (int) (h * mScale)) / 2;
        Log.d(C.TAG, String.format("view_size=(%d,%d) frame_size=(%d,%d) offset=(%d,%d)", mCameraView.getWidth(),
                mCameraView.getHeight(), w, h, cameraViewOffsetX, cameraViewOffsetY));
    }

    @Override
    public void onCameraViewStopped() {
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            state = State.WILL_START_SCAN;
        }
        return super.onTouchEvent(event);
    }

    long power = 0;
    boolean threadRunning = false;

    State state = State.IDLE;

    enum State {
        IDLE, WILL_START_SCAN, SCAN_DISPLAYING
    }

    private static void setAbsolutePosition(View v, int x, int y) {
        LayoutParams lp = (LayoutParams) v.getLayoutParams();
        lp.leftMargin = x;
        lp.topMargin = y;
        v.setLayoutParams(lp);
    }

    private static void setAbsolutePositionAndSize(View v, int x, int y, int w, int h) {
        LayoutParams lp = (LayoutParams) v.getLayoutParams();
        lp.leftMargin = x;
        lp.topMargin = y;
        lp.width = w;
        lp.height = h;
        v.setLayoutParams(lp);
    }

    private void startDisplaying(final Rect rect) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // ui update
                sightCircle.setVisibility(View.VISIBLE);
                int x = (int) (rect.x * mScale + cameraViewOffsetX);
                int y = (int) (rect.y * mScale + cameraViewOffsetY);
                int w = (int) (rect.width * mScale);
                int h = (int) (rect.height * mScale);
                int centerX = x + w / 2;
                int centerY = y + h / 2;
                int d = (int) (Math.max(w, h));
                // Log.d(C.TAG, "circle->" + x + "," + y);
                setAbsolutePositionAndSize(sightCircle, centerX - d / 2, centerY - d / 2, d, d);

                leftTriangle.setVisibility(View.VISIBLE);
                setAbsolutePosition(leftTriangle, x - 40, y + d / 2 - 20);

                bottomTriangle.setVisibility(View.VISIBLE);
                setAbsolutePosition(bottomTriangle, x + d / 2 - 20, y + d);
            }
        });

        // start calculater
        final long maxPower = 530000;
        final long step = Math.max(1L, maxPower / 203 - 1);
        scouterSound.processing();
        final Runnable r = new Runnable() {
            @Override
            public void run() {
                power += step;
                if (power > maxPower) {
                    power = maxPower;
                    threadRunning = false;
                }
                tv.setText("" + power);
            }
        };
        Thread drawThread = new Thread() {
            public void run() {
                power = 0;
                while (threadRunning) {
                    runOnUiThread(r);
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };
        };
        threadRunning = true;
        drawThread.start();
    }
}
