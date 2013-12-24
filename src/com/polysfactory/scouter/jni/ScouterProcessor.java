package com.polysfactory.scouter.jni;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;

public class ScouterProcessor {

    private long mNativeObj;

    public ScouterProcessor(String faceCascadeFile, String noseCascadeFile) {
        mNativeObj = nativeCreateObject(faceCascadeFile, noseCascadeFile);
    }

    public void process(Mat imageBgra, MatOfRect faceRect) {
        nativeProcess(mNativeObj, imageBgra.nativeObj, faceRect.nativeObj);
    }

    public void release() {
        nativeDestroyObject(mNativeObj);
        mNativeObj = 0;
    }

    private native long nativeCreateObject(String faceCascadeFile, String noseCascadeFile);

    private native void nativeProcess(long thiz, long imageBgra, long faceRect);

    private native void nativeDestroyObject(long thiz);
}
