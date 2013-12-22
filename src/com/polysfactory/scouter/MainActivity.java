package com.polysfactory.scouter;

import java.io.File;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.Mat;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;

import com.polysfactory.scouter.jni.ScouterProcessor;

public class MainActivity extends Activity implements CvCameraViewListener2 {

    private CameraBridgeViewBase mCameraView;
    private ScouterProcessor scouterProcessor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        mCameraView = (CameraBridgeViewBase) findViewById(R.id.camera_view);
        mCameraView.setCvCameraViewListener(this);
        mCameraView.setCameraIndex(C.CAMERA_INDEX);

        mCameraView.setMaxFrameSize(400, 240);
        mCameraView.enableView();

        File faceCascadeFile = IOUtils.getFilePath(this, "cascade", "face.xml");
        IOUtils.copy(this, R.raw.haarcascade_frontalface_default, faceCascadeFile);

        File noseCascadeFile = IOUtils.getFilePath(this, "cascade", "nose.xml");
        IOUtils.copy(this, R.raw.haarcascade_mcs_nose, noseCascadeFile);

        scouterProcessor = new ScouterProcessor(faceCascadeFile.getAbsolutePath(), noseCascadeFile.getAbsolutePath());
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
        // Mat gray = frame.gray();

        scouterProcessor.process(rgba);

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
