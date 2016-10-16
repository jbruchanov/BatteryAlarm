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

import com.scurab.android.batteryalarm.app.MainActivity;
import com.scurab.android.batteryalarm.model.Settings;
import com.scurab.android.batteryalarm.util.BatteryHelper;
import com.scurab.android.batteryalarm.util.MailGun;

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
            if (mWifiLock.isHeld()) {
                try {
                    mWifiLock.release();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
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
            mBatteryThread = new BatteryThread(this, 10000);
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
        static final int MAIL_SENT = 2;
        static final int MAIL_SENDING = 1;
        static final int MAIL_TO_SEND = 0;

        public static final String TAG = "BatteryThread";
        private static final int MINUTE = 60 * 1000;
        private final int mSleepWait;
        private final ToneGenerator mToneGenerator;
        private final Settings mSettings;
        private boolean mIsStopped = false;
        private final Object mToken = new Object();
        private final BatteryCheckerService mService;
        private int mMailNotificationState = MAIL_TO_SEND;
        private boolean mFirstTone = true;

        public BatteryThread(@NonNull BatteryCheckerService service, int sleepWait) {
            mService = service;
            mSleepWait = sleepWait;
            mSettings = ((BatteryAlarmApp) (service.getApplicationContext())).onLoadSettings();
            mToneGenerator = new ToneGenerator(AudioManager.STREAM_ALARM, mSettings.getToneVolume());
        }

        @Override
        public void run() {
            while (!mIsStopped) {
                Log.d(TAG, String.format("BatteryLevel:%.2f, IsCharging:%s", BatteryHelper.getBatteryLevel(mService), BatteryHelper.isCharging(mService)));
                boolean sound = mSettings.isSoundNotification() && (mSettings.shouldStartTone() || mFirstTone);
                if (sound) {
                    mToneGenerator.startTone(mSettings.getToneValue(), 4000);
                    mFirstTone = false;
                }
                if (mSettings.isMailNotification() && mMailNotificationState == MAIL_TO_SEND) {
                    mMailNotificationState = MAIL_SENDING;
                    MailGun.sendNotificationAsync(mSettings.getDeviceName(), mSettings.getMailGunKey(), mSettings.getMailGunDomain(), mSettings.getMailGunRecipient(), mMailCallback);
                }
                /*
                    Weird case when charger is plugged out, but battery status is weird so it won't stop the service
                    => explicit check and stop it if we charging
                 */
                if (BatteryHelper.isCharging(mService)) {
                    mService.stopSelf();
                }
                sleep(sound ? mSleepWait : MINUTE);
            }
            mToneGenerator.release();
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "BatteryThread quit");
            }
        }

        private void sleep(int time) {
            synchronized (mToken) {
                try {
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, String.format("BatteryThread is going to sleep for: %sms", time));
                    }
                    mToken.wait(time);
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

        private MailGun.Callback mMailCallback = (response, ex) -> mMailNotificationState = response ? MAIL_SENT : MAIL_TO_SEND;
    }
}
