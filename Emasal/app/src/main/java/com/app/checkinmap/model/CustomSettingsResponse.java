package com.app.checkinmap.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * This class handle all the data from
 * the user custom settings
 */

public class CustomSettingsResponse {
    @SerializedName("totalSize")
    private int mTotalSize;

    @SerializedName("done")
    private boolean mIsDone;

    @SerializedName("records")
    private List<Setting> mSettings;

    public int getTotalSize() {
        return mTotalSize;
    }

    public void setTotalSize(int totalSize) {
        this.mTotalSize = totalSize;
    }

    public boolean isIsDone() {
        return mIsDone;
    }

    public void setIsDone(boolean isDone) {
        this.mIsDone = isDone;
    }

    public List<Setting> getSettings() {
        return mSettings;
    }

    public void setSettings(List<Setting> settings) {
        this.mSettings = settings;
    }

    public static class Setting{

        @SerializedName("Id")
        private String mId;

        @SerializedName("Name")
        private String mName;

        @SerializedName("Radio_Check_In__c")
        private int mRadioCheckIn;

        @SerializedName("Intervalo_en_segundos__c")
        private int mIntervalInSeconds;

        @SerializedName("Dias_Historico_App__c")
        private int mHistoricalDays;

        public int getHistoricalDays() {
            return mHistoricalDays;
        }

        public void setHistoricalDays(int mHistoricalDays) {
            this.mHistoricalDays = mHistoricalDays;
        }

        public String getId() {
            return mId;
        }

        public void setId(String id) {
            this.mId = id;
        }

        public String getName() {
            return mName;
        }

        public void setName(String name) {
            this.mName = name;
        }

        public int getRadioCheckIn() {
            return mRadioCheckIn;
        }

        public void setRadioCheckIn(int radioCheckIn) {
            this.mRadioCheckIn = radioCheckIn;
        }

        public int getIntervalInSeconds() {
            return mIntervalInSeconds;
        }

        public void setIntervalInSeconds(int intervalInSeconds) {
            this.mIntervalInSeconds = intervalInSeconds;
        }
    }
}
