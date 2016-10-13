package com.scurab.android.batteryalarm.ui;

import android.content.res.Resources;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.scurab.android.batteryalarm.R;
import com.scurab.android.batteryalarm.drawable.CircleTextDrawable;
import com.scurab.android.batteryalarm.widget.SeekBar;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by JBruchanov on 13/10/2016.
 */

public class SoundSettingsFragment extends Fragment {

    @BindView(R.id.tone_spinner)
    Spinner mToneSpinner;

    @BindView(R.id.tone_volume)
    SeekBar mToneVolume;

    private ToneGenerator mToneGenerator;
    private int mLastPlayedVolume;

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mToneGenerator != null) {
            mToneGenerator.release();
            mToneGenerator = null;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_generic_settings, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        mToneSpinner.setAdapter(new ToneAdapter());
        Resources res = getResources();

        int color = res.getColor(R.color.colorAccent);
        CircleTextDrawable thumb = new CircleTextDrawable(res, res.getDimensionPixelSize(R.dimen.seek_bar_thumb_size), color, color);
        thumb.getTextPaint().setColor(res.getColor(R.color.colorAccentText));
        mToneVolume.setThumb(thumb);
        mToneVolume.setMax(ToneGenerator.MAX_VOLUME);
    }

    @OnClick(R.id.tone_play)
    public void onPlayToneClick(View source) {
        int index = mToneSpinner.getSelectedItemPosition();
        Pair<String, Integer> item = ((ToneAdapter) mToneSpinner.getAdapter()).getItem(index);
        int vol = mToneVolume.getProgress();
        if (mToneGenerator == null || mLastPlayedVolume != vol) {
            if (mToneGenerator != null) {
                mToneGenerator.stopTone();
                mToneGenerator.release();
            }
            mToneGenerator = new ToneGenerator(AudioManager.STREAM_ALARM, vol);
            mLastPlayedVolume = vol;
        }
        mToneGenerator.startTone(item.second, 4000);
    }

    private static class ToneAdapter extends BaseAdapter {

        private final List<Pair<String, Integer>> mData;

        public ToneAdapter() {
            mData = new ArrayList<>();
            mData.add(new Pair<>("CDMA_HIGH_SS", ToneGenerator.TONE_CDMA_HIGH_SS));
            mData.add(new Pair<>("SUP_INTERCEPT", ToneGenerator.TONE_SUP_INTERCEPT));
            mData.add(new Pair<>("SUP_CONGESTION_ABBREV", ToneGenerator.TONE_SUP_CONGESTION_ABBREV));
            mData.add(new Pair<>("CDMA_INTERCEPT", ToneGenerator.TONE_CDMA_INTERCEPT));
            mData.add(new Pair<>("CDMA_REORDER", ToneGenerator.TONE_CDMA_REORDER));
            mData.add(new Pair<>("CDMA_NETWORK_BUSY", ToneGenerator.TONE_CDMA_NETWORK_BUSY));
            mData.add(new Pair<>("CDMA_ANSWER", ToneGenerator.TONE_CDMA_ANSWER));
            mData.add(new Pair<>("CDMA_CALL_SIGNAL_ISDN_NORMAL", ToneGenerator.TONE_CDMA_CALL_SIGNAL_ISDN_NORMAL));
            mData.add(new Pair<>("CDMA_HIGH_L", ToneGenerator.TONE_CDMA_HIGH_L));
            mData.add(new Pair<>("CDMA_MED_L", ToneGenerator.TONE_CDMA_MED_L));
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Pair<String, Integer> getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView tv;
            if (convertView == null) {
                tv = new TextView(parent.getContext());
                int padding = parent.getResources().getDimensionPixelSize(R.dimen.gap_small);
                tv.setPadding(padding, padding, padding, padding);
                tv.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            } else {
                tv = (TextView) convertView;
            }
            Pair<String, Integer> item = getItem(position);
            tv.setText(item.first);
            return tv;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return getView(position, convertView, parent);
        }
    }
}
