package com.polysfactory.scouter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.hardware.Camera.Face;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class FaceView extends View {

    private Paint mPaint;

    private Face[] mFaces;

    Matrix matrix = new Matrix();

    private RectF mRect = new RectF();

    private int mWidth;

    private int mHeight;

    public FaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize();
    }

    public FaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public FaceView(Context context) {
        super(context);
        initialize();
    }

    private void initialize() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(Color.MAGENTA);
        mPaint.setAlpha(128);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
    }

    public void setFaces(Face[] faces) {
        mFaces = faces;
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.d(C.TAG, "faceview size:" + w + "," + h);
        mWidth = w;
        mHeight = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mFaces == null) {
            return;
        }
        for (Face face : mFaces) {
            if (face == null) {
                continue;
            }
            // prepareMatrix(matrix, true, 0, mWidth, mHeight);
            prepareMatrix(matrix, false, 0, mWidth, mHeight);
            // prepareMatrix(matrix, false, 0, getWidth(), getHeight());
            int saveCount = canvas.save();
            mRect.set(face.rect);
            matrix.mapRect(mRect);
            // mRect.offset(dx, dy);
            canvas.drawRect(mRect, mPaint);
            Log.d(C.TAG, "=>" + mRect.toShortString());
            canvas.restoreToCount(saveCount);
        }

    }

    public static void prepareMatrix(Matrix matrix, boolean mirror, int displayOrientation, int viewWidth,
            int viewHeight) {
        // Need mirror for front camera.
        matrix.setScale(mirror ? -1 : 1, 1);
        // This is the value for android.hardware.Camera.setDisplayOrientation.
        // matrix.postRotate(displayOrientation);
        // Camera driver coordinates range from (-1000, -1000) to (1000, 1000).
        // UI coordinates range from (0, 0) to (width, height).
        matrix.postTranslate(1000f, 1000f);
        matrix.postScale(viewWidth / 2000f, viewHeight / 2000f);
        // matrix.postTranslate(viewWidth / 2f, viewHeight / 2f);
    }
}