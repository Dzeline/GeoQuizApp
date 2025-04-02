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
import com.example.geoquiz.data.local.database.MessageEntity;
import com.example.geoquiz.data.local.repository.MessageRepositoryImpl;

public class SmsReceiver extends BroadcastReceiver {

    private static final String TAG = "SmsReceiver";


    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();

        if (bundle != null) {
            Object[] pdus = (Object[]) bundle.get("pdus");

            if (pdus != null) {
                for (Object pdu : pdus) {
                    SmsMessage message;
                    String format = bundle.getString("format");
                    message = SmsMessage.createFromPdu((byte[]) pdu, format);
                    String content = message.getMessageBody();
                    String sender = message.getOriginatingAddress();

                    Log.d(TAG, "SMS received from " + sender + ": " + content);

                    GeoQuizDatabase db = GeoQuizDatabase.getInstance((Application) context.getApplicationContext());

                    // ✅ 1. Handle location messages
                    if (content.startsWith("LOC|")) {
                        try {
                            String[] parts = content.split("\\|");
                            String[] coords = parts[1].split(",");
                            double lat = Double.parseDouble(coords[0]);
                            double lon = Double.parseDouble(coords[1]);
                            float acc = Float.parseFloat(parts[2].split(":")[1]);
                            int dbm = Integer.parseInt(parts[3].split(":")[1]);
                            int ta = Integer.parseInt(parts[4].split(":")[1]);

                            LocationLogEntity log = new LocationLogEntity(System.currentTimeMillis(), lat, lon, acc, dbm, ta);
                            db.locationLogDao().insertLog(log);

                            Log.i(TAG, "Location parsed and stored: " + lat + ", " + lon);
                        } catch (Exception e) {
                            Log.e(TAG, "Failed to parse location SMS: " + e.getMessage());
                        }

                    } else {
                        // ✅ 2. Handle regular chat message
                        MessageRepositoryImpl repo = new MessageRepositoryImpl(db);
                        repo.insertMessage(new MessageEntity(sender, "You", content, System.currentTimeMillis()));
                        Log.i(TAG, "Message saved to DB: " + content);
                    }
                }
                }
            }
        }
    }


