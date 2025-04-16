package com.example.geoquiz.domain.model;

import androidx.annotation.NonNull;

public class RiderInfo {
    @NonNull
    private String riderName;

    private String latestMessage;
    private boolean isAvailable;
    @NonNull
    private String phoneNumber;
    private int profileImageResId; // Resource id for the profile image

    public RiderInfo( @NonNull String riderName, String latestMessage, boolean isAvailable, int profileImageResId,@NonNull String phoneNumber) {
        this.riderName = riderName;
        this.latestMessage = latestMessage;
        this.isAvailable = isAvailable;
        this.profileImageResId = profileImageResId;
        this.phoneNumber=phoneNumber;
    }

    public String getRiderName() {
        return riderName;
    }

    public String getLatestMessage() {
        return latestMessage;
    }

    public String getPhoneNumber(){
        return phoneNumber;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public int getProfileImageResId() {
        return profileImageResId;
    }
}
