package com.scurab.android.batteryalarm.util;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Base64;

import com.scurab.android.batteryalarm.BuildConfig;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by JBruchanov on 11/10/2016.
 */

public class MailGun {
    static final String UTF = "UTF-8";
    static final String AUTHORIZATION_BASIC_X = "Basic %s";
    private static final int TIMEOUT = 5000;

    public interface Callback {
        void onResult(Boolean response, Throwable ex);
    }

    public static void sendNotificationAsync(@NonNull String deviceName, @NonNull String key, @NonNull String domain, @NonNull String to) {
        sendNotificationAsync(deviceName, key, domain, to, null);
    }

    public static void sendNotificationAsync(@NonNull String deviceName, @NonNull String key, @NonNull String domain, @NonNull String to, @Nullable Callback callback) {
        new AsyncTask<Void, Void, Boolean>() {
            private Throwable mThrowable = null;

            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    return sendNotification(deviceName, key, domain, to);
                } catch (Throwable ex) {
                    mThrowable = ex;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Boolean clientResponse) {
                if (callback != null) {
                    callback.onResult(clientResponse, mThrowable);
                }
            }
        }.execute();
    }

    public static boolean sendNotification(@NonNull String deviceName, @NonNull String key, @NonNull String domain, @NonNull String to) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL("https://api.mailgun.net/v3/" + domain + "/messages").openConnection();
        String loginbase = Base64.encodeToString(String.format("api:%s", key).getBytes(), Base64.NO_WRAP);
        conn.setRequestProperty("Authorization", String.format(AUTHORIZATION_BASIC_X, loginbase));
        conn.setRequestMethod("POST");
        conn.setConnectTimeout(TIMEOUT);
        conn.setReadTimeout(TIMEOUT);
        conn.setUseCaches(false);
        conn.setDefaultUseCaches(false);


        String post = new StringBuilder()
                .append("from=").append(URLEncoder.encode("noreply@batteryalarm.com", UTF))
                .append("&").append("to=").append(URLEncoder.encode(to, UTF))
                .append("&").append("subject=").append(URLEncoder.encode("BatteryAlarm", UTF))
                .append("&").append("text=").append(URLEncoder.encode(String.format("LowBattery device:'%s'", deviceName), UTF))
                .toString();

        byte[] postDataBytes = post.getBytes(UTF);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
        conn.setDoOutput(true);
        conn.getOutputStream().write(postDataBytes);
        StringWriter sw = new StringWriter();
        copyLarge(new InputStreamReader(conn.getInputStream()), sw);
        String result;
        if (BuildConfig.DEBUG) {
            result = sw.toString();
        }
        return conn.getResponseCode() == 200;
    }

    static long copyLarge(Reader input, Writer output) throws IOException {
        char[] buffer = new char[8 * 1024];
        long count = 0;
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }
}
