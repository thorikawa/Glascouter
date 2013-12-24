package com.polysfactory.scouter.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import com.polysfactory.scouter.C;

public class CalculatingTextView extends TextView {

    public CalculatingTextView(Context context) {
        super(context);
    }

    public CalculatingTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.d(C.TAG, "onDraw text");
        super.onDraw(canvas);
    }
}
