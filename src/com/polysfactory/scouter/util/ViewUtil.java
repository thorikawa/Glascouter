package com.polysfactory.scouter.util;

import android.view.View;
import android.widget.FrameLayout.LayoutParams;

public class ViewUtil {
    public static void setAbsolutePosition(View v, int x, int y) {
        LayoutParams lp = (LayoutParams) v.getLayoutParams();
        lp.leftMargin = x;
        lp.topMargin = y;
        v.setLayoutParams(lp);
    }

    public static void setAbsolutePositionAndSize(View v, int x, int y, int w, int h) {
        LayoutParams lp = (LayoutParams) v.getLayoutParams();
        lp.leftMargin = x;
        lp.topMargin = y;
        lp.width = w;
        lp.height = h;
        v.setLayoutParams(lp);
    }

    public static void setSize(View v, int w, int h) {
        LayoutParams lp = (LayoutParams) v.getLayoutParams();
        lp.width = w;
        lp.height = h;
        v.setLayoutParams(lp);
    }

}
