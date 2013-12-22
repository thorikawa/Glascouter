package com.polysfactory.scouter;

import org.opencv.android.CameraBridgeViewBase;

import android.hardware.Camera;
import android.util.Log;

public class C {
    public static final String TAG = "Scouter";
    public static final int CAMERA_INDEX;
    public static final boolean FLIP;
    static {
        if (Camera.getNumberOfCameras() > 1) {
            Log.d(TAG, "Use front camera");
            CAMERA_INDEX = CameraBridgeViewBase.CAMERA_ID_FRONT;
            FLIP = true;
        } else {
            Log.d(TAG, "Use back camera");
            CAMERA_INDEX = CameraBridgeViewBase.CAMERA_ID_ANY;
            FLIP = false;
        }
    }
}
