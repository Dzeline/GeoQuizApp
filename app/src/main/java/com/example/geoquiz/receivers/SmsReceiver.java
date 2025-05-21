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
import com.example.geoquiz.di.SmsReceiverEntryPoint;
import dagger.hilt.android.EntryPointAccessors;
//import com.example.geoquiz.data.local.repository.MessageRepositoryImpl;

public class SmsReceiver extends BroadcastReceiver {

    private static final String TAG = "SmsReceiver";


    @Override
    public void onReceive(final Context context, final Intent intent) {
        Log.d(TAG, "onReceive triggered."); // Logging start of onReceive
        final Bundle bundle = intent.getExtras();

        if (bundle == null) {
            Log.w(TAG, "SMS bundle is null.");
            return;
        }

        final Object[] pdusObj = (Object[]) bundle.get("pdus");
        if (pdusObj == null) {
            Log.w(TAG, "PDUs object is null in SMS bundle.");
            return;
        }

        // Get DAOs via Hilt EntryPoint
        SmsReceiverEntryPoint entryPoint =
                EntryPointAccessors.fromApplication(context.getApplicationContext(), SmsReceiverEntryPoint.class);
        final LocationLogDao locationLogDao = entryPoint.locationLogDao();
        final MessageDao messageDao = entryPoint.messageDao();


        for (final Object pduItem : pdusObj) {
            if (pduItem == null) {
                Log.w(TAG, "Received null PDU item, skipping.");
                continue;
            }

            final SmsMessage currentMessage;
            final String format = bundle.getString("format"); // Get format for each PDU if it can vary
            try {
                currentMessage = SmsMessage.createFromPdu((byte[]) pduItem, format);
            } catch (Exception e) {
                Log.e(TAG, "Error creating SmsMessage from PDU: " + e.getMessage(), e);
                continue;
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
            Log.d(TAG, "SMS received from " + sender + ": " + content);


            if (content.startsWith("LOC|")) {
                try {
                    final String[] parts = content.split("\\|");
                    if (parts.length < 5) {
                        Log.e(TAG, "Invalid LOC format: " + content);
                        continue;
                    }
                    final String[] coords = parts[1].split(",");
                    if (coords.length < 2) {
                        Log.e(TAG, "Invalid LOC coordinates format: " + parts[1]);
                        continue;
                    }
                    final double lat = Double.parseDouble(coords[0].trim());
                    final double lon = Double.parseDouble(coords[1].trim());
                    final float acc = Float.parseFloat(parts[2].split(":")[1].trim());
                    final int dbm = Integer.parseInt(parts[3].split(":")[1].trim());
                    final int ta = Integer.parseInt(parts[4].split(":")[1].trim());

                    final LocationLogEntity logEntity = new LocationLogEntity(System.currentTimeMillis(), lat, lon, acc, dbm, ta);

                    // Use the GeoQuizDatabase.databaseWriteExecutor for background execution
                    GeoQuizDatabase.databaseWriteExecutor.execute(() -> {
                        locationLogDao.insertLog(logEntity);
                        Log.i(TAG, "Location parsed and stored: " + lat + ", " + lon);
                    });

                } catch (final Exception e) {
                    Log.e(TAG, "Failed to parse location SMS: " + content, e);
                }
            } else {
                // Assuming "You" is the receiver when an SMS comes into the app
                final MessageEntity chatMessage = new MessageEntity(sender, "You", content, System.currentTimeMillis());
                GeoQuizDatabase.databaseWriteExecutor.execute(() -> {
                    messageDao.insertMessage(chatMessage);
                    Log.i(TAG, "Chat message from " + sender + " saved to DB.");
                });
            }
        }
    }
}

