package com.scurab.android.batteryalarm.model;

import android.media.ToneGenerator;

import com.google.gson.annotations.SerializedName;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Created by JBruchanov on 14/10/2016.
 */

@SuppressWarnings("unused")
public class Settings {

    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormat.forPattern("HH:mm");

    @SerializedName("DeviceName")
    private String mDeviceName;

    @SerializedName("SoundNotification")
    private boolean mSoundNotification;
    @SerializedName("ToneValue")
    private int mToneValue;
    @SerializedName("ToneVolume")
    private int mToneVolume;
    @SerializedName("StartTime")
    private String mStartTime;
    @SerializedName("EndTime")
    private String mEndTime;
    @SerializedName("Weekends")
    private boolean mWeekends;

    @SerializedName("MailNotification")
    private boolean mMailNotification;
    @SerializedName("MailGunKey")
    private String mMailGunKey;
    @SerializedName("MailGunDomain")
    private String mMailGunDomain;
    @SerializedName("MailGunRecipient")
    private String mMailGunRecipient;

    private LocalTime mStartTimeObj;
    private LocalTime mEndTimeObj;

    public static Settings defaultSettings() {
        Settings s = new Settings();
        s.mSoundNotification = true;
        s.mToneValue = ToneGenerator.TONE_CDMA_HIGH_SS;
        s.mToneVolume = 100;
        s.mStartTime = "8:00";
        s.mEndTime = "22:00";
        s.mWeekends = true;

        s.mMailNotification = false;
        s.mDeviceName = android.os.Build.MODEL;
        return s;
    }

    public String getDeviceName() {
        return mDeviceName;
    }

    public void setDeviceName(String deviceName) {
        mDeviceName = deviceName;
    }

    public boolean isSoundNotification() {
        return mSoundNotification;
    }

    public void setSoundNotification(boolean soundNotification) {
        mSoundNotification = soundNotification;
    }

    public int getToneValue() {
        return mToneValue;
    }

    public void setToneValue(int toneValue) {
        mToneValue = toneValue;
    }

    public int getToneVolume() {
        return mToneVolume;
    }

    public void setToneVolume(int toneVolume) {
        mToneVolume = toneVolume;
    }

    public String getStartTime() {
        return mStartTime;
    }

    public void setStartTime(String startTime) {
        mStartTime = startTime;
        mStartTimeObj = mEndTimeObj = null;
    }

    public String getEndTime() {
        return mEndTime;
    }

    public void setEndTime(String endTime) {
        mEndTime = endTime;
        mStartTimeObj = mEndTimeObj = null;
    }

    public boolean isWeekends() {
        return mWeekends;
    }

    public void setWeekends(boolean weekends) {
        mWeekends = weekends;
    }

    public boolean isMailNotification() {
        return mMailNotification;
    }

    public void setMailNotification(boolean mailNotification) {
        mMailNotification = mailNotification;
    }

    public String getMailGunKey() {
        return mMailGunKey;
    }

    public void setMailGunKey(String mailGunKey) {
        mMailGunKey = mailGunKey;
    }

    public String getMailGunDomain() {
        return mMailGunDomain;
    }

    public void setMailGunDomain(String mailGunDomain) {
        mMailGunDomain = mailGunDomain;
    }

    public String getMailGunRecipient() {
        return mMailGunRecipient;
    }

    public void setMailGunRecipient(String mailGunRecipient) {
        mMailGunRecipient = mailGunRecipient;
    }

    public boolean areMailDataEntered(){
        return mMailGunDomain != null && mMailGunKey != null && mMailGunRecipient != null;
    }

    public boolean shouldStartTone() {
        try {
            if (mStartTimeObj == null || mEndTimeObj == null && (mStartTime != null && mEndTime == null)) {
                mStartTimeObj = TIME_FORMATTER.parseLocalTime(mStartTime);
                mEndTimeObj = TIME_FORMATTER.parseLocalTime(mEndTime);
            }
            LocalTime nowTime = currentTime();
            int dayOfWeek = currentDayTime().getDayOfWeek();
            boolean isInTimeRange = mStartTimeObj.isBefore(nowTime) && mEndTimeObj.isAfter(nowTime);
            boolean canStartWeekend = mWeekends || !(dayOfWeek == DateTimeConstants.SATURDAY || dayOfWeek == DateTimeConstants.SUNDAY);
            return isInTimeRange && canStartWeekend;
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
    }

    LocalTime currentTime() {
        return LocalTime.now();
    }

    DateTime currentDayTime() {
        return DateTime.now();
    }
}
