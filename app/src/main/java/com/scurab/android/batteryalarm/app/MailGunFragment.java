package com.scurab.android.batteryalarm.app;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.scurab.android.batteryalarm.R;
import com.scurab.android.batteryalarm.model.Settings;
import com.scurab.android.batteryalarm.util.MailGun;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by JBruchanov on 13/10/2016.
 */

public class MailGunFragment extends BaseFragment {

    @BindView(R.id.mailgun_device_name)
    TextView mDeviceName;

    @BindView(R.id.mailgun_domain)
    TextView mDomain;

    @BindView(R.id.mailgun_key)
    TextView mKey;

    @BindView(R.id.mailgun_recipient)
    TextView mRecipient;

    @BindView(R.id.mailgun_send_test)
    TextView mSendButton;

    @BindView(R.id.send_mail)
    CheckBox mSendMailCheckBox;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mailgun_settings, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
    }

    @Override
    public void onResume() {
        super.onResume();

        bindData();
    }

    private void bindData() {
        Settings settings = getSettings();
        mDeviceName.setText(settings.getDeviceName());
        mSendMailCheckBox.setChecked(settings.isMailNotification());
        mKey.setText(settings.getMailGunKey());
        mDomain.setText(settings.getMailGunDomain());
        mRecipient.setText(settings.getMailGunRecipient());
    }

    protected void saveData() {
        Settings s = getSettings();
        s.setDeviceName(mDeviceName.getText().toString());
        s.setMailGunKey(nullIfEmpty(mKey.getText().toString()));
        s.setMailGunDomain(nullIfEmpty(mDomain.getText().toString()));
        s.setMailGunRecipient(nullIfEmpty(mRecipient.getText().toString()));
        s.setMailNotification(mSendMailCheckBox.isChecked() && s.areMailDataEntered());
    }

    @Override
    public void onPause() {
        super.onPause();
        saveData();
    }

    @OnClick(R.id.mailgun_send_test)
    public void onSendTestClick(View src) {
        mSendButton.setText(R.string.send_test_mail_sending);
        mSendButton.setEnabled(false);
        String deviceName = mDeviceName.getText().toString();
        String key = mKey.getText().toString();
        String domain = mDomain.getText().toString();
        String recipient = mRecipient.getText().toString();
        MailGun.sendNotificationAsync(deviceName, key, domain, recipient, (response, ex) -> {
            FragmentActivity activity = getActivity();
            mSendButton.setText(R.string.send_test_mail);
            mSendButton.setEnabled(true);
            if (activity != null) {
                if (ex != null) {
                    Toast.makeText(activity, ex.getMessage(), Toast.LENGTH_SHORT).show();
                    ex.printStackTrace();
                } else {
                    Toast.makeText(activity, response ? "OK" : "nOK", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
