package com.polysfactory.scouter;

import java.io.File;
import java.util.List;

import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.MyCameraView;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;

import android.app.Activity;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;

import com.polysfactory.scouter.jni.ScouterProcessor;
import com.polysfactory.scouter.util.IOUtils;
import com.polysfactory.scouter.view.TriangleView;

public class MainActivity extends Activity implements CvCameraViewListener2 {

    private MyCameraView mCameraView;
    private ScouterProcessor mScouterProcessor;
    private ScouterSound mScouterSound;
    private TextView mPowerText;
    private ImageView mSightCircle;
    private TriangleView mLeftTriangle;
    private TriangleView mBottomTriangle;
    Mat captured;
    float mScale;
    int mCameraViewOffsetX = 0;
    int mCameraViewOffsetY = 0;
    int mScreenWidth = 0;
    int mScreenHeight = 0;
    long mDisplayedPower = 0;
    boolean mDisplayThreadRunning = false;
    State mState = State.IDLE;
    private TextView mPrompt;

    enum State {
        IDLE, WILL_START_SCAN, SCAN_DISPLAYING
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        mCameraView = (MyCameraView) findViewById(R.id.camera_view);
        mCameraView.setCvCameraViewListener(this);
        mCameraView.setCameraIndex(C.CAMERA_INDEX);
        // setting (640, 360) would mess up camera preview... I don't know why.
        mCameraView.setMaxFrameSize(320, 180);

        File faceCascadeFile = IOUtils.getFilePath(this, "cascade", "face.xml");
        IOUtils.copy(this, R.raw.haarcascade_frontalface_default, faceCascadeFile);

        File noseCascadeFile = IOUtils.getFilePath(this, "cascade", "nose.xml");
        IOUtils.copy(this, R.raw.haarcascade_mcs_nose, noseCascadeFile);

        mScouterProcessor = new ScouterProcessor(faceCascadeFile.getAbsolutePath(), noseCascadeFile.getAbsolutePath());
        mScouterSound = new ScouterSound(this);

        Typeface font = Typeface.createFromAsset(getAssets(), "font/digital_regular.ttf");
        mPowerText = (TextView) findViewById(R.id.text);
        mPowerText.setTypeface(font);
        mPrompt = (TextView) findViewById(R.id.prompt);
        mPrompt.setTypeface(font);

        mSightCircle = (ImageView) findViewById(R.id.sight_circle_view);
        mLeftTriangle = (TriangleView) findViewById(R.id.left_triangle);
        mBottomTriangle = (TriangleView) findViewById(R.id.bottom_triangle);
    }

    @Override
    protected void onResume() {
        super.onStart();
        mCameraView.enableView();
    }

    @Override
    protected void onPause() {
        super.onStop();
        mCameraView.disableView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public Mat onCameraFrame(CvCameraViewFrame frame) {
        Mat rgba = frame.rgba();

        if (mState == State.WILL_START_SCAN) {
            MatOfRect faceRectMat = new MatOfRect();
            // TODO: allow some error attempts
            int power = mScouterProcessor.process(rgba, faceRectMat);

            List<Rect> faceRectList = faceRectMat.toList();
            if (faceRectList.size() > 0) {
                Rect faceRect = faceRectList.get(0);
                mState = State.SCAN_DISPLAYING;
                captured = rgba.clone();
                startDisplaying(faceRect, power);
            }
        }
        return rgba;
    }

    @Override
    public void onCameraViewStarted(int w, int h) {
        mScale = mCameraView.getScale();
        mScreenWidth = mCameraView.getWidth();
        mScreenHeight = mCameraView.getHeight();
        mCameraViewOffsetX = (mScreenWidth - (int) (w * mScale)) / 2;
        mCameraViewOffsetY = (mScreenHeight - (int) (h * mScale)) / 2;
        Log.d(C.TAG, String.format("screen_size=(%d,%d)", mScreenWidth, mScreenHeight));
        Log.d(C.TAG, String.format("view_size=(%d,%d) frame_size=(%d,%d) offset=(%d,%d)", mCameraView.getWidth(),
                mCameraView.getHeight(), w, h, mCameraViewOffsetX, mCameraViewOffsetY));
    }

    @Override
    public void onCameraViewStopped() {
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            mState = State.WILL_START_SCAN;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_CAMERA) {
            // Stop the preview and release the camera.
            // Execute your logic as quickly as possible
            // so the capture happens quickly.
            return false;
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            mState = State.WILL_START_SCAN;
            return false;
        } else {
            return super.onKeyDown(keyCode, event);
        }
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

    private static long calcStep(long power) {
        return Math.min(1000L, Math.max(1L, (long) (power * 0.001)));
    }

    private void startDisplaying(final Rect rect, final int power) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // ui update
                mPrompt.setVisibility(View.GONE);
                mSightCircle.setVisibility(View.VISIBLE);
                int x = (int) (rect.x * mScale + mCameraViewOffsetX);
                int y = (int) (rect.y * mScale + mCameraViewOffsetY);
                int w = (int) (rect.width * mScale);
                int h = (int) (rect.height * mScale);
                int centerX = x + w / 2;
                int centerY = y + h / 2;
                int d = (int) (Math.max(w, h) * 1.5);
                int sightX = centerX - d / 2;
                int sightY = centerY - d / 2;
                setAbsolutePositionAndSize(mSightCircle, sightX, sightY, d, d);

                mLeftTriangle.setVisibility(View.VISIBLE);
                setAbsolutePosition(mLeftTriangle, sightX - 51, sightY + d / 2 - 30);

                mBottomTriangle.setVisibility(View.VISIBLE);
                setAbsolutePosition(mBottomTriangle, sightX + d / 2 - 30, sightY + d);

                int powerX = centerX > mScreenWidth / 2 ? mCameraViewOffsetX + 40 : mScreenWidth / 2 + 40;
                int powerY = centerY > mScreenHeight / 2 ? mCameraViewOffsetY + 80 : mScreenHeight / 2 + 80;
                setAbsolutePosition(mPowerText, powerX, powerY);
            }
        });

        // start calculater
        final long step = calcStep(power);
        mScouterSound.processing(true);
        final Runnable r = new Runnable() {
            @Override
            public void run() {
                mDisplayedPower += step;
                if (mDisplayedPower > power) {
                    mDisplayedPower = power;
                    mDisplayThreadRunning = false;
                    mScouterSound.beap();
                }
                mPowerText.setText("" + mDisplayedPower);
            }
        };
        Thread drawThread = new Thread() {
            public void run() {
                mDisplayedPower = 0;
                while (mDisplayThreadRunning) {
                    runOnUiThread(r);
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };
        };
        mDisplayThreadRunning = true;
        drawThread.start();
    }
}
