package com.scurab.android.batteryalarm.drawable;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

/**
 * Simple drawable with circle and text inside
 */
public class CircleDrawable extends Drawable implements StrokeDrawable {

    private Paint mPaint;

    private float mRadius = 0;
    private boolean mDynamic = false;

    private Rect mBounds;

    private int mColorStroke;
    private int mColorFill;
    private float mRadiusDiff = 0;
    private int mHeight;
    private int mWidth;

    public CircleDrawable(int diam) {
        this(diam, Color.WHITE, Color.TRANSPARENT);
    }

    public CircleDrawable(int colorStroke, int colorFill) {
        this(0, 0, colorStroke, colorFill);
    }

    public CircleDrawable(float diam, int colorStroke, int colorFill) {
        this(diam, 5, colorStroke, colorFill);
    }

    public CircleDrawable(float diam, float strokeWidth, int colorStroke, int colorFill) {
        mDynamic = diam <= 0;
        mColorStroke = colorStroke;
        mColorFill = colorFill;
        createPaint(mColorStroke, strokeWidth);

        mRadius = diam / 2f;
    }

    private void createPaint(int stroke, float strokeWidth) {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(stroke);
        mPaint.setStrokeWidth(strokeWidth);
    }

    @Override
    public void draw(Canvas canvas) {
        drawFill(canvas);
        drawStroke(canvas);
    }

    /**
     * Draw simple star as lines
     *
     * @param canvas
     */
    private void drawStroke(Canvas canvas) {
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(mColorStroke);
        canvas.drawCircle(mBounds.centerX(), mBounds.centerY(), mRadius + mRadiusDiff, mPaint);
    }

    /**
     * Draw star with filling
     *
     * @param canvas
     */
    private void drawFill(Canvas canvas) {
        if (mColorFill != Color.TRANSPARENT) {
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(mColorFill);
            canvas.drawCircle(mBounds.centerX(), mBounds.centerY(), mRadius + mRadiusDiff, mPaint);
        }
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        mBounds = bounds;
        mHeight = mBounds.bottom - mBounds.top;
        mWidth = mBounds.right - mBounds.left;
        if (mDynamic) {
            mRadius = Math.min(mBounds.right - mBounds.left, mBounds.bottom - mBounds.top) / 2f - mPaint.getStrokeWidth();
        }
        super.onBoundsChange(bounds);
    }

    @Override
    public void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        mPaint.setColorFilter(cf);
    }

    @Override
    public int getOpacity() {
        return mPaint.getAlpha();
    }

    @Override
    public void setStrokeWidth(float width) {
        mPaint.setStrokeWidth(width);
    }

    public float getStrokeWidth() {
        return mPaint.getStrokeWidth();
    }

    public float getRadius() {
        return mRadius;
    }

    public void setRadius(float radius) {
        mRadius = radius;
    }

    public float getRadiusDiff() {
        return mRadiusDiff;
    }

    public void setRadiusDiff(float radiusDiff) {
        mRadiusDiff = radiusDiff;
    }

    public int getHeight() {
        return mHeight;
    }

    public int getWidth() {
        return mWidth;
    }
}