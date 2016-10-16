package com.scurab.android.batteryalarm.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TabWidget;
import android.widget.Toast;

import com.scurab.android.batteryalarm.BatteryAlarmApp;
import com.scurab.android.batteryalarm.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;

public class MainActivity extends AppCompatActivity {

    @BindView(android.R.id.tabhost)
    FragmentTabHost mTabHost;

    @BindView(android.R.id.tabs)
    TabWidget mTabWidget;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar(mToolbar);
        ButterKnife.bind(this);
        mTabHost.setup(this, getSupportFragmentManager(), android.R.id.tabcontent);
        mTabHost.addTab(mTabHost.newTabSpec(SoundSettingsFragment.class.getName()).setIndicator("Tone"), SoundSettingsFragment.class, null);
        mTabHost.addTab(mTabHost.newTabSpec(MailGunFragment.class.getName()).setIndicator("Mail"), MailGunFragment.class, null);
    }

    @OnClick(R.id.action_save)
    public void onSaveData(View source) {
        onSaveData();
        Toast.makeText(this, android.R.string.ok, Toast.LENGTH_SHORT).show();
    }

    protected void onSaveData() {
        Class[] fragments = new Class[]{SoundSettingsFragment.class, MailGunFragment.class};
        for (Class clz : fragments) {
            Fragment fragment = getSupportFragmentManager().findFragmentByTag(clz.getName());
            if (fragment instanceof BaseFragment) {
                ((BaseFragment) fragment).saveData();
            }
        }
        ((BatteryAlarmApp) getApplication()).onSaveSettings();
    }

    @OnLongClick(R.id.action_save)
    public boolean onExportData(View source) {
        onSaveData();
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, DataProvider.getShareIntentUri());
        shareIntent.setType("application/json");
        startActivity(Intent.createChooser(shareIntent, getString(R.string.export_settings)));
        return true;
    }
}
