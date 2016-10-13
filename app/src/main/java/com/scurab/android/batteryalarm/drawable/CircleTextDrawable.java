package com.scurab.android.batteryalarm.drawable;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.TypedValue;


/**
 * Simple drawable with circle and text inside
 */
public class CircleTextDrawable extends CircleDrawable {

    private Paint mTextPaint;

    private String mText = "";

    private int mTextOffsetY;

    private int mGap;

    public CircleTextDrawable(Resources res, int diam) {
        this(res, diam, Color.WHITE, Color.TRANSPARENT);
    }

    public CircleTextDrawable(Resources res, float diam, int colorStroke, int colorFill) {
        super(diam, colorStroke, colorFill);
        mGap = (int) (0.5f + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, res.getDisplayMetrics()));
        createPaints(res, colorStroke);
    }

    private void createPaints(Resources res, int stroke) {
        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setColor(stroke);
        mTextPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14, res.getDisplayMetrics()));
        mTextPaint.setColor(Color.WHITE);
        //mTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        drawText(canvas);
    }

    private void drawText(Canvas canvas) {
        if (!TextUtils.isEmpty(mText)) {
            float w = mTextPaint.measureText(mText) / 2f;
            Rect r = getBounds();
            canvas.drawText(mText, r.centerX() - w, r.centerY() - mTextOffsetY, mTextPaint);
        }
    }

    @Override
    public void setAlpha(int alpha) {
        super.setAlpha(alpha);
        mTextPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        super.setColorFilter(cf);
        mTextPaint.setColorFilter(cf);
    }

    @Override
    public int getOpacity() {
        return mTextPaint.getAlpha();
    }

    public void setStrokeWidth(float width) {
        mTextPaint.setStrokeWidth(width);
    }

    public float getStrokeWidth() {
        return mTextPaint.getStrokeWidth();
    }

    public String getText() {
        return mText;
    }

    public void setText(String text) {
        if (!TextUtils.equals(text, mText)) {
            mText = text;
            Rect r = new Rect();
            mTextPaint.getTextBounds(mText, 0, mText.length(), r);
            mTextOffsetY = r.bottom - ((r.bottom - r.top) >> 1);
            invalidateSelf();
        }
    }

    //simple constant to have background little bigger than text bounds, empiric values
    private float getSizeCoef(int len) {
        switch (len){
            case 1:
            case 2:
                return 1.75f;
            case 3:
                return 1.4f;
            default:
            case 4:
                return 1.25f;
        }
    }

    public Paint getTextPaint() {
        return mTextPaint;
    }

    public void setTextPaint(Paint textPaint) {
        if (textPaint != mTextPaint) {
            mTextPaint = textPaint;
            setText(mText);
        }
    }

    public void setTextSizePx(float size) {
        mTextPaint.setTextSize(size);
    }
}