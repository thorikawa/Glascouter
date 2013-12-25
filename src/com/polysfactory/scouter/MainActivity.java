package com.polysfactory.scouter;

import java.io.File;
import java.util.List;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.MyCameraView;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import com.polysfactory.scouter.jni.ScouterProcessor;
import com.polysfactory.scouter.util.GlassUtil;
import com.polysfactory.scouter.util.IOUtil;
import com.polysfactory.scouter.util.ViewUtil;
import com.polysfactory.scouter.view.TriangleView;

public class MainActivity extends Activity implements CvCameraViewListener2 {

    private MyCameraView mCameraView;
    private ScouterProcessor mScouterProcessor;
    private ScouterSound mScouterSound;
    private TextView mPowerText;
    private ImageView mSightCircle;
    private TriangleView mLeftTriangle;
    private TriangleView mBottomTriangle;
    Mat mCaptured;
    float mScale;
    int mCameraViewOffsetX = 0;
    int mCameraViewOffsetY = 0;
    int mScreenWidth = 0;
    int mScreenHeight = 0;
    long mDisplayedPower = 0;
    boolean mDisplayThreadRunning = false;
    State mState = State.IDLE;
    private TextView mPrompt;
    private ImageView mDummyImageView;
    private int mPreviewWidth;
    private int mPreviewHeight;
    private Handler mHandler = new Handler();
    private Mat mCurrentFrame;
    private GestureDetector mGestureDetector;

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
        mCameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_ANY);
        // setting (640, 360) would mess up camera preview... I don't know why.
        mCameraView.setMaxFrameSize(320, 180);

        File faceCascadeFile = IOUtil.getFilePath(this, "cascade", "face.xml");
        IOUtil.copy(this, R.raw.haarcascade_frontalface_default, faceCascadeFile);

        File eyeCascadeFile1 = IOUtil.getFilePath(this, "cascade", "eye1.xml");
        IOUtil.copy(this, R.raw.haarcascade_eye, eyeCascadeFile1);

        File eyeCascadeFile2 = IOUtil.getFilePath(this, "cascade", "eye2.xml");
        IOUtil.copy(this, R.raw.haarcascade_eye_tree_eyeglasses, eyeCascadeFile1);

        File faceModelFile = IOUtil.getFilePath(this, "facemodel", "model.data");
        IOUtil.copy(this, R.raw.model, faceModelFile);

        mScouterProcessor = new ScouterProcessor(faceCascadeFile.getAbsolutePath(), eyeCascadeFile1.getAbsolutePath(),
                eyeCascadeFile2.getAbsolutePath(), faceModelFile.getAbsolutePath());
        mScouterSound = new ScouterSound(this);

        Typeface font = Typeface.createFromAsset(getAssets(), "font/digital_regular.ttf");
        mPowerText = (TextView) findViewById(R.id.text);
        mPowerText.setTypeface(font);
        mPrompt = (TextView) findViewById(R.id.prompt);
        mPrompt.setTypeface(font);

        mSightCircle = (ImageView) findViewById(R.id.sight_circle_view);
        mLeftTriangle = (TriangleView) findViewById(R.id.left_triangle);
        mBottomTriangle = (TriangleView) findViewById(R.id.bottom_triangle);
        mDummyImageView = (ImageView) findViewById(R.id.dummy);

        if (GlassUtil.isGlass()) {
            mGestureDetector = createGestureDetector(this);
            mPrompt.setText(getString(R.string.prompt_glass));
        }
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
        mScouterProcessor.release();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private void share() {
        Log.d(C.TAG, "take screenshot");
        // File imageFile = IOUtils.getFilePath(this, "tmp", "aaaa.jpg");

        // Hack: Since camera SurfaceView is not captured by View.getDrawingCache(), temporarily enable dummy
        // ImageView
        Bitmap bm = Bitmap.createBitmap(mPreviewWidth, mPreviewHeight, Bitmap.Config.ARGB_8888);
        if (mCaptured != null) {
            Utils.matToBitmap(mCaptured, bm);
        } else {
            Utils.matToBitmap(mCurrentFrame, bm);
        }
        mDummyImageView.setImageBitmap(bm);
        ViewUtil.setSize(mDummyImageView, (int) (mPreviewWidth * mScale), (int) (mPreviewHeight * mScale));
        mDummyImageView.setVisibility(View.VISIBLE);

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                String path = Environment.getExternalStorageDirectory().toString() + "/" + "tmp_scouter.jpg";
                IOUtil.takeScreenshot(findViewById(R.id.container), path);
                mDummyImageView.setVisibility(View.GONE);

                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                Uri screenshotUri = Uri.fromFile(new File(path));

                sharingIntent.setType("image/jpeg");
                sharingIntent.putExtra(Intent.EXTRA_STREAM, screenshotUri);
                startActivity(Intent.createChooser(sharingIntent, "Share scouter screenshot!"));
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_share) {
            share();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Mat onCameraFrame(CvCameraViewFrame frame) {
        mCurrentFrame = frame.rgba().clone();

        if (mState == State.WILL_START_SCAN) {
            MatOfRect faceRectMat = new MatOfRect();
            // TODO: allow some error attempts
            int power = mScouterProcessor.process(mCurrentFrame, faceRectMat);

            List<Rect> faceRectList = faceRectMat.toList();
            if (faceRectList.size() > 0) {
                Rect faceRect = faceRectList.get(0);
                mState = State.SCAN_DISPLAYING;
                mCaptured = mCurrentFrame;
                Log.d(C.TAG, "power: " + power);
                startDisplaying(faceRect, power);
            }
        }
        return mCurrentFrame;
    }

    @Override
    public void onCameraViewStarted(int w, int h) {
        mScale = mCameraView.getScale();
        mPreviewWidth = w;
        mPreviewHeight = h;
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
            mCameraView.disableView();
            return false;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    private static long calcStep(long power) {
        return Math.min(2000L, Math.max(1L, (long) (power * 0.01)));
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
                ViewUtil.setAbsolutePositionAndSize(mSightCircle, sightX, sightY, d, d);

                mLeftTriangle.setVisibility(View.VISIBLE);
                ViewUtil.setAbsolutePosition(mLeftTriangle, sightX - 51, sightY + d / 2 - 30);

                mBottomTriangle.setVisibility(View.VISIBLE);
                ViewUtil.setAbsolutePosition(mBottomTriangle, sightX + d / 2 - 30, sightY + d);

                int powerX = centerX > mScreenWidth / 2 ? mCameraViewOffsetX + 40 : mScreenWidth / 2 + 40;
                int powerY = centerY > mScreenHeight / 2 ? mCameraViewOffsetY + 80 : mScreenHeight / 2 + 80;
                ViewUtil.setAbsolutePosition(mPowerText, powerX, powerY);
            }
        });

        // start calculater
        final long step = calcStep(power);
        mScouterSound.processing(true);
        final Runnable r = new Runnable() {
            int loopCount = 0;

            @Override
            public void run() {
                loopCount++;
                mDisplayedPower += step;
                if (mDisplayedPower > power) {
                    mDisplayedPower = power;
                    mDisplayThreadRunning = false;
                    mSightCircle.setVisibility(View.VISIBLE);
                    mLeftTriangle.setVisibility(View.VISIBLE);
                    mBottomTriangle.setVisibility(View.VISIBLE);
                    mScouterSound.beap();
                } else {
                    if ((loopCount / 10) % 2 == 1) {
                        mSightCircle.setVisibility(View.INVISIBLE);
                        mLeftTriangle.setVisibility(View.INVISIBLE);
                        mBottomTriangle.setVisibility(View.INVISIBLE);
                    } else {
                        mSightCircle.setVisibility(View.VISIBLE);
                        mLeftTriangle.setVisibility(View.VISIBLE);
                        mBottomTriangle.setVisibility(View.VISIBLE);
                    }
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

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        if (mGestureDetector != null) {
            return mGestureDetector.onMotionEvent(event);
        }
        return super.onGenericMotionEvent(event);
    }

    private GestureDetector createGestureDetector(Context context) {
        GestureDetector gestureDetector = new GestureDetector(context);
        gestureDetector.setBaseListener(new GestureDetector.BaseListener() {
            @Override
            public boolean onGesture(Gesture gesture) {
                if (gesture == Gesture.TAP) {
                    Log.d(C.TAG, "Tap");
                    mState = State.WILL_START_SCAN;
                    return true;
                } else if (gesture == Gesture.TWO_TAP) {
                    Log.d(C.TAG, "Two Tap");
                    // XXX: Glass does not support share Intent
                    share();
                    return true;
                }
                return false;
            }
        });
        return gestureDetector;
    }
}
