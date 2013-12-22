package com.polysfactory.scouter;

import org.opencv.android.OpenCVLoader;

import android.app.Application;

public class ScouterApplication extends Application {
    static {
        if (OpenCVLoader.initDebug()) {
            System.loadLibrary("scouter");
        } else {
            // Handle initialization error
        }
    }
}
