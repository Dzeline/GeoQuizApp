package com.example.geoquiz.adapters;

public class RiderInfo {
    private String riderName;
    private String latestMessage;
    private boolean isAvailable;
    private int profileImageResId; // Resource id for the profile image

    public RiderInfo(String riderName, String latestMessage, boolean isAvailable, int profileImageResId) {
        this.riderName = riderName;
        this.latestMessage = latestMessage;
        this.isAvailable = isAvailable;
        this.profileImageResId = profileImageResId;
    }

    public String getRiderName() {
        return riderName;
    }

    public String getLatestMessage() {
        return latestMessage;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public int getProfileImageResId() {
        return profileImageResId;
    }
}
