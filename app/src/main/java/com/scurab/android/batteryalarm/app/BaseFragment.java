package com.scurab.android.batteryalarm.app;

import android.content.Context;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.scurab.android.batteryalarm.BatteryAlarmApp;
import com.scurab.android.batteryalarm.model.Settings;

/**
 * Created by JBruchanov on 14/10/2016.
 */

public abstract class BaseFragment extends Fragment {

    private BatteryAlarmApp mApplication;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mApplication = (BatteryAlarmApp) getContext().getApplicationContext();
    }

    protected Settings getSettings() {
        return mApplication.getSettings();
    }

    protected abstract void saveData();

    static String nullIfEmpty(String value) {
        return value == null || value.length() == 0 ? null : value;
    }

    public BatteryAlarmApp getApplication() {
        return mApplication;
    }

    protected void showToast(@StringRes int msg) {
        showToast(mApplication.getString(msg));
    }

    protected void showToast(CharSequence msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
    }
}
