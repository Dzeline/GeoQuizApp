package com.example.geoquiz.receivers;


import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import com.example.geoquiz.data.local.database.GeoQuizDatabase;
import com.example.geoquiz.data.local.database.LocationLogEntity;
import com.example.geoquiz.data.local.database.LocationLogDao;
import com.example.geoquiz.data.local.database.MessageDao;
import com.example.geoquiz.data.local.database.MessageEntity;
//import com.example.geoquiz.data.local.repository.MessageRepositoryImpl;

public class SmsReceiver extends BroadcastReceiver {

    private static final String TAG = "SmsReceiver";


    @Override
    public void onReceive(final Context context, final Intent intent) {
        final Bundle bundle = intent.getExtras();

        if (null != bundle) {
            final Object[] pdusObj = (Object[]) bundle.get("pdus");

            if (null != pdusObj) {
                for (final Object pdu : pdusObj) {
                    if (pdu == null) {
                        Log.w(TAG, "Received null PDU item, skipping.");
                        continue;
                    }
                    final SmsMessage currentMessage;
                    final String format = bundle.getString("format");
                    try {
                        currentMessage = SmsMessage.createFromPdu((byte[]) pdu, format);
                    } catch (Exception e) {
                        Log.e(TAG, "Error creating SmsMessage from PDU: " + e.getMessage());
                        continue; // Skip this PDU if it's malformed
                    }

                    if (currentMessage == null) {
                        Log.w(TAG, "SmsMessage.createFromPdu returned null, skipping.");
                        continue;
                    }
                    final String content = currentMessage.getMessageBody();
                    final String sender = currentMessage.getOriginatingAddress();
                    if (sender == null || content == null) {
                        Log.w(TAG, "SMS with null sender or content, skipping.");
                        continue;
                    }
                    Log.d(SmsReceiver.TAG, "SMS received from " + sender + ": " + content);

                    final GeoQuizDatabase db = GeoQuizDatabase.getInstance((Application) context.getApplicationContext());

                    // ✅ 1. Handle location messages
                    if (content.startsWith("LOC|")) {
                        try {
                            final String[] parts = content.split("\\|");
                            if (parts.length < 5) { // Basic validation
                                Log.e(TAG, "Invalid LOC format: " + content);
                                continue;
                            }
                            final String[] coords = parts[1].split(",");
                            if (coords.length < 2) {
                                Log.e(TAG, "Invalid LOC coordinates format: " + parts[1]);
                                continue;
                            }
                            final double lat = Double.parseDouble(coords[0]);
                            final double lon = Double.parseDouble(coords[1]);
                            final float acc = Float.parseFloat(parts[2].split(":")[1]);
                            final int dbm = Integer.parseInt(parts[3].split(":")[1]);
                            final int ta = Integer.parseInt(parts[4].split(":")[1]);

                            final LocationLogEntity logEntity = new LocationLogEntity(System.currentTimeMillis(), lat, lon, acc, dbm, ta);
                            final LocationLogDao locationLogDao = db.locationLogDao();

                            // Perform database operation on a background thread
                            GeoQuizDatabase.databaseWriteExecutor.execute(() -> {
                                locationLogDao.insertLog(logEntity);
                                Log.i(TAG, "Location parsed and stored: " + lat + ", " + lon);
                            });

                        } catch (final Exception e) { // Catch more specific exceptions if possible
                            Log.e(TAG, "Failed to parse location SMS: " + content, e);
                        }
                    } else {
                        // Handle regular chat message
                        final MessageDao messageDao = db.messageDao();
                        // Assuming "You" is the recipient for SMS received by the app
                        final MessageEntity chatMessage = new MessageEntity(sender, "You", content, System.currentTimeMillis());

                        // Perform database operation on a background thread
                        GeoQuizDatabase.databaseWriteExecutor.execute(() -> {
                            messageDao.insertMessage(chatMessage);
                            Log.i(TAG, "Chat message from " + sender + " saved to DB.");
                        });
                    }
                }
            } else {
                Log.w(TAG, "PDUs object is null in SMS bundle.");
            }
        } else {
            Log.w(TAG, "SMS bundle is null.");
        }
    }
}



