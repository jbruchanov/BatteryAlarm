package com.scurab.android.batteryalarm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.scurab.android.batteryalarm.ui.MainActivity;
import com.scurab.android.batteryalarm.util.BatteryHelper;

/**
 * Created by JBruchanov on 11/10/2016.
 */

public class BatteryCheckerService extends Service {

    public static final String ACTION_START_CHECKING = "com.scurab.android.batteryalarm.START_CHECKING";
    public static final String ACTION_STOP_CHECKING = "com.scurab.android.batteryalarm.STOP_CHECKING";
    private static final int ID_NOTIFICATION = 0xA1;
    private BatteryThread mBatteryThread;
    private PowerManager.WakeLock mWakeLock;
    private WifiManager.WifiLock mWifiLock;

    @Override
    public void onCreate() {
        super.onCreate();
        mWakeLock = ((PowerManager) getSystemService(POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "BatteryCheckerService");
        mWifiLock = ((WifiManager) getSystemService(WIFI_SERVICE)).createWifiLock(WifiManager.WIFI_MODE_FULL, "BatteryCheckerService");
        mWakeLock.acquire();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        onStop();
        if (mWakeLock != null) {
            mWakeLock.release();
            mWakeLock = null;
        }
        if (mWifiLock != null) {
            mWifiLock.release();
            mWifiLock = null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if (ACTION_START_CHECKING.equals(action)) {
            onStart(intent, startId);
            return START_STICKY;
        } else if (ACTION_STOP_CHECKING.equals(action)) {
            stopSelf();
        }
        return START_NOT_STICKY;
    }

    protected void onStop() {
        if (mBatteryThread != null) {
            mBatteryThread.quit();
            mBatteryThread = null;
        }
        hideNotification();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        if (mBatteryThread == null) {
            mBatteryThread = new BatteryThread(getApplicationContext(), 10000);
            mBatteryThread.start();
        }
        showNotification();
    }

    private void showNotification() {
        Context context = getApplicationContext();
        String value = String.format("BatteryLevel:%.2f, IsCharging:%s", BatteryHelper.getBatteryLevel(context), BatteryHelper.isCharging(context));
        PendingIntent stopIntent = PendingIntent.getService(context, 1, new Intent(context, BatteryCheckerService.class).setAction(ACTION_STOP_CHECKING), PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle(getString(R.string.app_name))
                .setAutoCancel(false)
                .setDefaults(Notification.DEFAULT_LIGHTS)
                .setContentText(value)
                .setContentIntent(PendingIntent.getActivity(context, 1, new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT))
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, getString(R.string.stop), stopIntent)
                .build();
        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).notify(ID_NOTIFICATION, notification);
    }

    private void hideNotification() {
        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancel(ID_NOTIFICATION);
    }

    private static class BatteryThread extends Thread {

        public static final String TAG = "BatteryThread";
        private final int mSleepWait;
        private final ToneGenerator mToneGenerator;
        private boolean mIsStopped = false;
        private final Object mToken = new Object();
        private final Context mContext;

        public BatteryThread(@NonNull Context context, int sleepWait) {
            mContext = context;
            mSleepWait = sleepWait;
            mToneGenerator = new ToneGenerator(AudioManager.STREAM_ALARM, ToneGenerator.MAX_VOLUME);
        }

        @Override
        public void run() {
            while (!mIsStopped) {
                Log.d(TAG, String.format("BatteryLevel:%.2f, IsCharging:%s", BatteryHelper.getBatteryLevel(mContext), BatteryHelper.isCharging(mContext)));
                mToneGenerator.startTone(ToneGenerator.TONE_CDMA_HIGH_L, 4000);
                sleep();
            }
            mToneGenerator.release();
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "BatteryThread quit");
            }
        }

        private void sleep() {
            synchronized (mToken) {
                try {
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, String.format("BatteryThread is going to sleep for: %sms", mSleepWait));
                    }
                    mToken.wait(mSleepWait);
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "BatteryThread awoken");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public void quit() {
            if (!mIsStopped) {
                synchronized (mToken) {
                    mIsStopped = true;
                    mToken.notifyAll();
                }
            }
        }
    }
}
