package com.scurab.android.batteryalarm.app;

import android.content.Context;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

import com.scurab.android.batteryalarm.R;
import com.scurab.android.batteryalarm.drawable.CircleTextDrawable;
import com.scurab.android.batteryalarm.model.Settings;
import com.scurab.android.batteryalarm.widget.SeekBar;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;

/**
 * Created by JBruchanov on 13/10/2016.
 */

public class SoundSettingsFragment extends BaseFragment {

    @BindView(R.id.tone_spinner)
    Spinner mToneSpinner;

    @BindView(R.id.tone_volume)
    SeekBar mToneVolume;

    @BindView(R.id.tone_time_input_layout)
    TextInputLayout mTextInputLayout;

    @BindView(R.id.tone_time)
    TextInputEditText mToneTime;

    @BindView(R.id.sound_notification)
    CheckBox mSoundNotification;

    @BindView(R.id.weekends)
    CheckBox mWeekends;

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
        return inflater.inflate(R.layout.fragment_tone_settings, container, false);
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

    @Override
    public void onResume() {
        super.onResume();
        getView().post(this::bindData);//for some weird reason must be posted, otherwise NPE in textview for spinner (Nexus4 seen)
    }

    @Override
    public void onPause() {
        super.onPause();
        saveData();
    }

    private void bindData() {
        Settings settings = getSettings();
        mSoundNotification.setChecked(settings.isSoundNotification());
        ToneAdapter adapter = (ToneAdapter) mToneSpinner.getAdapter();
        mToneSpinner.setSelection(Math.max(0, Math.min(adapter.getCount() - 1, adapter.getIndexOf(settings.getToneValue()))));
        mToneVolume.setProgress(settings.getToneVolume());
        if (settings.getStartTime() != null && settings.getEndTime() != null) {
            mToneTime.setText(String.format("%s - %s", settings.getStartTime(), settings.getEndTime()));
        }
        mWeekends.setChecked(settings.isWeekends());
    }

    protected void saveData() {
        Settings settings = getSettings();
        settings.setSoundNotification(mSoundNotification.isChecked());
        ToneAdapter adapter = (ToneAdapter) mToneSpinner.getAdapter();
        settings.setToneValue(adapter.getItem(mToneSpinner.getSelectedItemPosition()).second);
        settings.setToneVolume(mToneVolume.getProgress());
        String[] times = parseTimes(mToneTime.getText().toString());
        if (times != null) {
            settings.setStartTime(times[0]);
            settings.setEndTime(times[1]);
        }
        settings.setWeekends(mWeekends.isChecked());
    }

    @OnTextChanged(R.id.tone_time)
    void onTimeChanged(CharSequence s, int start, int before, int count) {
        String value = s.toString();
        boolean isOk = value.length() == 0;
        isOk |= parseTimes(value) != null;
        mTextInputLayout.setErrorEnabled(!isOk);
        if (!isOk) {
            mTextInputLayout.setError(getString(R.string.err_invalid_time_range));
        }
    }

    public String[] parseTimes(@NonNull String value) {
        int divider = value.indexOf("-");
        String[] result = null;
        if (divider > 0 && value.length() > divider) {
            String startTime = value.substring(0, divider).trim();
            String endTime = value.substring(divider + 1).trim();
            if (startTime.length() >= 3 && endTime.length() >= 3) {
                try {
                    Settings.TIME_FORMATTER.parseLocalTime(startTime);
                    Settings.TIME_FORMATTER.parseLocalTime(endTime);
                    result = new String[]{startTime, endTime};
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
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

        AudioManager manager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        manager.setStreamVolume(AudioManager.STREAM_ALARM, manager.getStreamMaxVolume(AudioManager.STREAM_ALARM), 0);
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

        public int getIndexOf(int toneValue) {
            for (int i = 0, n = mData.size(); i < n; i++) {
                if(mData.get(i).second == toneValue){
                    return i;
                }
            }
            return -1;
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
