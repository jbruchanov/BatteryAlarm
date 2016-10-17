package com.scurab.android.batteryalarm.util;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by JBruchanov on 12/10/2016.
 */

@SuppressWarnings("WeakerAccess")
public class BatteryHelper {

    public static final IntentFilter FILTER = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
    public static final float BATTERY_LOW_LEVEL_THRESHOLD = 0.15f;

    public static float getBatteryLevel(@NonNull Context context) {
        return getBatteryLevel(context.registerReceiver(null, FILTER));
    }

    public static float getBatteryLevel(@Nullable Intent intent) {
        if (intent != null) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            float scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            return level / scale;
        }
        return Float.NaN;
    }

    public static boolean isCharging(@NonNull Context context) {
        return Boolean.TRUE.equals(isCharging(context.registerReceiver(null, FILTER)));
    }

    public static Boolean isCharging(@Nullable Intent intent) {
        if (intent != null) {
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
            if (status != -1 && plugged != -1) {
                boolean isNotCharging = !(status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL);
                boolean isPlugged = plugged != 0;
                return isPlugged /*&& !isNotCharging*/;
            }
        }
        return null;
    }

    public static boolean shouldStartService(@NonNull Context context, @Nullable Intent intent) {
        return !isCharging(context)/* && isBatteryLow(context, intent)*/;
    }

    public static boolean shouldStopService(@NonNull Context context, @Nullable Intent intent) {
        return isCharging(context) || !isBatteryLow(context, intent);
    }

    public static boolean isBatteryLow(@NonNull Context context, @Nullable Intent intent) {
        return intent != null && Intent.ACTION_BATTERY_LOW.equals(intent.getAction())
                || getBatteryLevel(context) <= BATTERY_LOW_LEVEL_THRESHOLD;
    }
}
