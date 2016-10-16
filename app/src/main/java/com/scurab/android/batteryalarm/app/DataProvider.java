package com.scurab.android.batteryalarm.app;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.webkit.MimeTypeMap;

import com.scurab.android.batteryalarm.BatteryAlarmApp;
import com.scurab.android.batteryalarm.BuildConfig;

import org.joda.time.DateTime;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Created by JBruchanov on 16/10/2016.
 */

public class DataProvider extends ContentProvider {

    public static final String EXPORT_FILE_NAME_TEMPLATE = "BatteryAlarm_%s.json";
    public static final String URI_TEMPLATE = "content://%s/data/";

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
    }

    @Override
    public String getType(Uri uri) {
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension("json");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException("Insert is not supported");
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Delete is not supported");
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Update is not supported");
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        return openFile(uri, mode, null);
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode, CancellationSignal signal) throws FileNotFoundException {
        return ParcelFileDescriptor.open(new File(getContext().getFilesDir(), BatteryAlarmApp.SETTINGS_FILE_NAME), ParcelFileDescriptor.MODE_READ_ONLY);
    }

    public static Uri getShareIntentUri() {
        return Uri.parse(String.format(DataProvider.URI_TEMPLATE, BuildConfig.APPLICATION_ID) + String.format(EXPORT_FILE_NAME_TEMPLATE, new DateTime().toString("yyyy-MM-dd-HHmmss")));
    }
}