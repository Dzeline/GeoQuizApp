package com.example.geoquiz.util;
import android.telephony.SmsManager;
import android.util.Log;

public class OfflineSmsHelper {
    private static final String TAG = "OfflineSmsHelper";

    public static String formatPayload(double lat, double lon, float acc, int dbm, int ta) {
        return String.format("LOC|%f,%f|acc:%.1f|dbm:%d|ta:%d", lat, lon, acc, dbm, ta);
    }

    public static void sendLocationSMS(String phoneNumber, String payload) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, payload, null, null);
            Log.d(TAG, "SMS sent: " + payload);
        } catch (Exception e) {
            Log.e(TAG, "Failed to send SMS: " + e.getMessage());
        }
    }

}
