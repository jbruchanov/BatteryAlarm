package com.scurab.android.batteryalarm.app;

import android.content.Context;
import android.support.v4.app.Fragment;

import com.scurab.android.batteryalarm.BatteryAlarmApp;
import com.scurab.android.batteryalarm.model.Settings;

/**
 * Created by JBruchanov on 14/10/2016.
 */

public class BaseFragment extends Fragment {

    private Settings mSettings;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mSettings = ((BatteryAlarmApp) getContext().getApplicationContext()).getSettings();
    }

    protected Settings getSettings() {
        return mSettings;
    }

    static String nullIfEmpty(String value) {
        return value == null || value.length() == 0 ? null : value;
    }
}
