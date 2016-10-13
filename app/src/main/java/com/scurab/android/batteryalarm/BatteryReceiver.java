package com.scurab.android.batteryalarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.scurab.android.batteryalarm.util.BatteryHelper;

/**
 * Created by JBruchanov on 12/10/2016.
 */

public class BatteryReceiver extends BroadcastReceiver {
    private static final String TAG = "BatteryReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, String.valueOf(intent.getAction()));
        if (BatteryHelper.shouldStartService(context, intent)) {
            Intent serviceIntent = new Intent(context, BatteryCheckerService.class)
                    .setAction(BatteryCheckerService.ACTION_START_CHECKING);
            context.startService(serviceIntent);
        } else if (BatteryHelper.shouldStopService(context, intent)) {
            context.stopService(new Intent(context, BatteryCheckerService.class));
        }
    }
}
