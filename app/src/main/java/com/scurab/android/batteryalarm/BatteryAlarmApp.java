package com.scurab.android.batteryalarm;

import android.app.Application;
import android.os.Environment;
import android.widget.Toast;

import com.google.gson.Gson;
import com.scurab.android.batteryalarm.model.Settings;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by JBruchanov on 14/10/2016.
 */

public class BatteryAlarmApp extends Application {

    public static final String SETTINGS_FILE_NAME = "BatteryAlarm.json";

    private Settings mSettings;
    private Gson mGson = new Gson();

    public Settings getSettings() {
        if (mSettings == null) {
            mSettings = onLoadSettings();
        }
        return mSettings;
    }

    public void onSaveSettings() {
        if (mSettings != null) {
            OutputStream os = null;
            try {
                File file = new File(getFilesDir(), SETTINGS_FILE_NAME);
                os = new FileOutputStream(file);
                String json = mGson.toJson(mSettings);
                os.write(json.getBytes());
                os.close();
            } catch (Throwable e) {
                e.printStackTrace();
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            } finally {
                try {
                    if (os != null) {
                        os.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected Settings onLoadSettings() {
        File file = new File(getFilesDir(), SETTINGS_FILE_NAME);
        try {
            if (file.exists()) {
                return mGson.fromJson(new FileReader(file), Settings.class);
            }
            file = new File(Environment.getExternalStorageDirectory(), SETTINGS_FILE_NAME);
            if (file.exists()) {
                return mGson.fromJson(new FileReader(file), Settings.class);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return Settings.defaultSettings();
    }
}
