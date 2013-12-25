package com.polysfactory.scouter.jni;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;

public class ScouterProcessor {

    private long mNativeObj;

    public ScouterProcessor(String faceCascadeFile, String eyeCascadeFile1, String eyeCascadeFile2, String faceModelFile) {
        mNativeObj = nativeCreateObject(faceCascadeFile, eyeCascadeFile1, eyeCascadeFile2, faceModelFile);
    }

    public int process(Mat imageBgra, MatOfRect faceRect) {
        return nativeProcess(mNativeObj, imageBgra.nativeObj, faceRect.nativeObj);
    }

    public void release() {
        nativeDestroyObject(mNativeObj);
        mNativeObj = 0;
    }

    private native long nativeCreateObject(String faceCascadeFile, String eyeCascadeFile1, String eyeCascadeFiel2,
            String faceModelFile);

    private native int nativeProcess(long thiz, long imageBgra, long faceRect);

    private native void nativeDestroyObject(long thiz);
}
