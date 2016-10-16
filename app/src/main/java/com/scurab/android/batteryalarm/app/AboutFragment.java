package com.scurab.android.batteryalarm.app;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.scurab.android.batteryalarm.BatteryAlarmApp;
import com.scurab.android.batteryalarm.R;
import com.scurab.android.batteryalarm.model.Settings;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by JBruchanov on 16/10/2016.
 */

public class AboutFragment extends BaseFragment {

    public static final int MAX_FILE_SIZE = 10 * 1024;
    private static final int REQ_IMPORT = 0xA2;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_about, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
    }

    @Override
    protected void saveData() {
        //nothing to do here
    }

    @OnClick(R.id.action_import)
    public void onImportData(View source) {
        Intent loadIntent = new Intent();
        loadIntent.setAction(Intent.ACTION_GET_CONTENT);
        loadIntent.setType("*/*");
        startActivityForResult(Intent.createChooser(loadIntent, getString(R.string.import_settings)), REQ_IMPORT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (REQ_IMPORT == requestCode && Activity.RESULT_OK == resultCode) {
            new AsyncTask<Void, Void, Integer>() {
                @Override
                protected Integer doInBackground(Void... params) {
                    InputStream is = null;
                    int msg = R.string.err_unable_to_finish_op;
                    try {
                        is = getContext().getContentResolver().openInputStream(data.getData());
                        if (is.available() > MAX_FILE_SIZE) {
                            msg = R.string.err_invalid_file_size;
                        } else {
                            BatteryAlarmApp app = getApplication();
                            Settings settings = app.getGson().fromJson(new InputStreamReader(is), Settings.class);
                            app.setSettings(settings);
                            app.onSaveSettings();
                            msg = android.R.string.ok;
                        }
                    } catch (Throwable t) {
                        t.printStackTrace();
                        Toast.makeText(getActivity(), t.getMessage(), Toast.LENGTH_LONG).show();
                    } finally {
                        try {
                            is.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    return msg;
                }

                @Override
                protected void onPostExecute(Integer msg) {
                    if (getActivity() != null) {
                        showToast(msg);
                    }
                }
            }.execute();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @OnClick(R.id.action_export)
    public void onExportData(View source) {
        ((MainActivity) getActivity()).onSaveData();
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, DataProvider.getShareIntentUri());
        shareIntent.setType("text/json");
        startActivity(Intent.createChooser(shareIntent, getString(R.string.export_settings)));
    }
}
