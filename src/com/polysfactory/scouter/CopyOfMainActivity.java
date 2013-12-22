package com.polysfactory.scouter;

import java.io.File;
import java.util.List;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.objdetect.CascadeClassifier;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;

public class CopyOfMainActivity extends Activity implements CvCameraViewListener2 {

    private CameraBridgeViewBase mCameraView;
    private CascadeClassifier mCascadeClassifier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        mCameraView = (CameraBridgeViewBase) findViewById(R.id.camera_view);
        mCameraView.setCvCameraViewListener(this);
        mCameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_ANY);
        mCameraView.setMaxFrameSize(400, 240);
        mCameraView.enableView();

        File cascadeFile = IOUtils.getFilePath(this, "cascade", "face.xml");
        IOUtils.copy(this, R.raw.haarcascade_frontalface_default, cascadeFile);

        mCascadeClassifier = new CascadeClassifier();
        mCascadeClassifier.load(cascadeFile.getAbsolutePath());
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
        Mat gray = frame.gray();
        MatOfRect rects = new MatOfRect();
        List<Rect> rectList = rects.toList();
        // mCascadeClassifier.detectMultiScale(gray, rects);
        mCascadeClassifier.detectMultiScale(gray, rects, 1.1, 2, 0, new Size(50, 50), new Size(400, 400));
        Log.d(C.TAG, "faces:" + rectList.size());
        return rgba;
    }

    @Override
    public void onCameraViewStarted(int arg0, int arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onCameraViewStopped() {
        // TODO Auto-generated method stub

    }

}
