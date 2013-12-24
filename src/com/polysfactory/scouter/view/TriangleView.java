package com.polysfactory.scouter.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;

import com.polysfactory.scouter.R;

public class TriangleView extends View {

    private static final int DIRECTION_UP = 0;
    private static final int DIRECTION_DOWN = 1;
    private static final int DIRECTION_LEFT = 2;
    private static final int DIRECTION_RIGHT = 3;
    private static final int DIRECTION_DEFAULT = DIRECTION_UP;
    private static final int COLOR_DEFAULT = Color.LTGRAY;

    private Paint mPaint;
    private Path mPath;
    private int mDirection;
    private int mColor;

    public TriangleView(Context context) {
        super(context);
        mDirection = DIRECTION_DEFAULT;
        mColor = COLOR_DEFAULT;
        init(context);
    }

    public TriangleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.TriangleView);
        mDirection = ta.getInt(R.styleable.TriangleView_direction, DIRECTION_DEFAULT);
        mColor = ta.getColor(R.styleable.TriangleView_color, COLOR_DEFAULT);
        ta.recycle();
        init(context);
    }

    private void init(Context context) {
        mPaint = new Paint();
        mPaint.setStyle(Style.FILL);
        mPaint.setColor(mColor);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawPath(mPath, mPaint);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mPath = getTrianglePath(w, h);
    }

    private Path getTrianglePath(int width, int height) {
        Point p1 = null, p2 = null, p3 = null;

        switch (mDirection) {
        case DIRECTION_UP:
            p1 = new Point(0, height);
            p2 = new Point(p1.x + width, p1.y);
            p3 = new Point(p1.x + (width / 2), p1.y - height);
            break;
        case DIRECTION_DOWN:
            p1 = new Point(0, 0);
            p2 = new Point(p1.x + width, p1.y);
            p3 = new Point(p1.x + (width / 2), p1.y + height);
            break;
        case DIRECTION_LEFT:
            p1 = new Point(width, 0);
            p2 = new Point(p1.x, p1.y + height);
            p3 = new Point(p1.x - width, p1.y + (height / 2));
            break;
        case DIRECTION_RIGHT:
            p1 = new Point(0, 0);
            p2 = new Point(p1.x, p1.y + height);
            p3 = new Point(p1.x + width, p1.y + (height / 2));
            break;
        default:
            throw new UnsupportedOperationException("Unsupported direction:" + mDirection);
        }

        Path path = new Path();
        path.moveTo(p1.x, p1.y);
        path.lineTo(p2.x, p2.y);
        path.lineTo(p3.x, p3.y);

        return path;
    }
}