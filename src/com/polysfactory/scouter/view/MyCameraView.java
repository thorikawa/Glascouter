package com.polysfactory.scouter.view;

import org.opencv.android.JavaCameraView;

import android.content.Context;
import android.util.AttributeSet;

public class MyCameraView extends JavaCameraView {

    public MyCameraView(Context context, int cameraId) {
        super(context, cameraId);
    }

    public MyCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public float getScale() {
        return mScale;
    }
}
