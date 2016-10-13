package com.scurab.android.batteryalarm.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;

import com.scurab.android.batteryalarm.drawable.CircleTextDrawable;

/**
 * Created by JBruchanov on 13/10/2016.
 */

public class SeekBar extends android.widget.SeekBar {

    private CircleTextDrawable mThumb;
    private OnSeekBarChangeListener mExternalSeekBarListener;

    public SeekBar(Context context) {
        super(context);
    }

    public SeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public SeekBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        super.setOnSeekBarChangeListener(mInternalListener);
    }

    @Override
    public void setThumb(Drawable thumb) {
        super.setThumb(thumb);
        mThumb = thumb instanceof CircleTextDrawable ? (CircleTextDrawable) thumb : null;
        onUpdateThumbText();
    }

    protected void onUpdateThumbText() {
        if (mThumb != null) {
            mThumb.setText(String.valueOf(getProgress()));
        }
    }

    @Override
    public synchronized void setProgress(int progress) {
        super.setProgress(progress);
        onUpdateThumbText();
    }

    @Override
    public void setOnSeekBarChangeListener(OnSeekBarChangeListener l) {
        mExternalSeekBarListener = l;
    }

    private OnSeekBarChangeListener mInternalListener = new OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(android.widget.SeekBar seekBar, int progress, boolean fromUser) {
            onUpdateThumbText();
            if (mExternalSeekBarListener != null) {
                mExternalSeekBarListener.onProgressChanged(seekBar, progress, fromUser);
            }
        }

        @Override
        public void onStartTrackingTouch(android.widget.SeekBar seekBar) {
            if (mExternalSeekBarListener != null) {
                mExternalSeekBarListener.onStartTrackingTouch(seekBar);
            }
        }

        @Override
        public void onStopTrackingTouch(android.widget.SeekBar seekBar) {
            if (mExternalSeekBarListener != null) {
                mExternalSeekBarListener.onStopTrackingTouch(seekBar);
            }
        }
    };
}
