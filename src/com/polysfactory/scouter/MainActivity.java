package com.polysfactory.scouter;

import java.io.File;
import java.util.List;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.objdetect.CascadeClassifier;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;

public class MainActivity extends Activity implements CvCameraViewListener2 {

    private CameraBridgeViewBase mCameraView;
    private CascadeClassifier mFaceClassifier;
    private CascadeClassifier mNoseClassifier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        mCameraView = (CameraBridgeViewBase) findViewById(R.id.camera_view);
        mCameraView.setCvCameraViewListener(this);
        if (Camera.getNumberOfCameras() > 1) {
            mCameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_FRONT);
        } else {
            mCameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_ANY);
        }

        mCameraView.setMaxFrameSize(400, 240);
        mCameraView.enableView();

        File faceCascadeFile = IOUtils.getFilePath(this, "cascade", "face.xml");
        IOUtils.copy(this, R.raw.haarcascade_frontalface_default, faceCascadeFile);

        File noseCascadeFile = IOUtils.getFilePath(this, "cascade", "nose.xml");
        IOUtils.copy(this, R.raw.haarcascade_mcs_nose, noseCascadeFile);

        mFaceClassifier = new CascadeClassifier();
        mFaceClassifier.load(faceCascadeFile.getAbsolutePath());

        mNoseClassifier = new CascadeClassifier();
        mNoseClassifier.load(noseCascadeFile.getAbsolutePath());
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
        // mCascadeClassifier.detectMultiScale(gray, rects);
        mFaceClassifier.detectMultiScale(gray, rects, 1.1, 2, 0, new Size(50, 50), new Size(400, 400));
        List<Rect> faceRectList = rects.toList();
        for (Rect rect : faceRectList) {
            Log.d(C.TAG, rect.toString());
        }
        if (faceRectList.isEmpty()) {
            return rgba;
        }

        Rect rect = faceRectList.get(0);
        Core.rectangle(rgba, rect.tl(), rect.br(), Scalar.all(200.0));
        Mat faceMat = rgba.submat(rect);
        MatOfRect noses = new MatOfRect();
        mNoseClassifier.detectMultiScale(faceMat, noses, 1.1, 2, 0, new Size(20, 20), new Size(100, 100));
        List<Rect> noseRectList = noses.toList();
        if (!noseRectList.isEmpty()) {
            for (Rect noseRect : noseRectList) {
                Core.rectangle(faceMat, noseRect.tl(), noseRect.br(), new Scalar(255, 0, 0));
            }
        }
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
