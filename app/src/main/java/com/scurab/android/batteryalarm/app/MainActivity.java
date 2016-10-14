package com.scurab.android.batteryalarm.app;

import android.os.Bundle;
import android.support.v4.app.FragmentTabHost;
import android.support.v7.app.AppCompatActivity;
import android.widget.TabWidget;

import com.scurab.android.batteryalarm.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(android.R.id.tabhost)
    FragmentTabHost mTabHost;

    @BindView(android.R.id.tabs)
    TabWidget mTabWidget;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mTabHost.setup(this, getSupportFragmentManager(), android.R.id.tabcontent);
        mTabHost.addTab(mTabHost.newTabSpec("SoundSettingsFragment").setIndicator("Tone"), SoundSettingsFragment.class, null);
        mTabHost.addTab(mTabHost.newTabSpec("MailGunFragment").setIndicator("Mail"), MailGunFragment.class, null);
    }
}
