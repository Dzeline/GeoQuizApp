package com.example.geoquiz.presentation.feature_request;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.geoquiz.R;
import com.example.geoquiz.presentation.feature_map.MapViewerActivity;
import com.example.geoquiz.presentation.feature_chat.MainFunctionActivity;
import com.example.geoquiz.util.OfflineLocationHelper;
import com.example.geoquiz.util.OfflineSmsHelper;
import com.google.android.material.button.MaterialButton;

/**
 *  RequestRideActivity allows users to input ride info, view map, and navigate back to chat.
 */
public class RequestRideActivity extends AppCompatActivity {

    private static final int PERMISSION_CODE = 101;
    private static String selectedPhoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.riderequest); // Links to riderequest.xml

        selectedPhoneNumber = getIntent().getStringExtra("riderPhone");

        // View bindings
        LinearLayout riderSection = findViewById(R.id.riderSection);
        MaterialButton btnShareLocation = findViewById(R.id.btnShareLocation);
        MaterialButton btnBackToChat = findViewById(R.id.btnBacktoChat);

        // 🔙 Handle Back to Chat button
        btnBackToChat.setOnClickListener(v -> {
            Intent intent = new Intent(RequestRideActivity.this, MainFunctionActivity.class);
            intent.putExtra("role","requester");

            startActivity(intent);
            finish(); // optional but recommended
        });

        // 🎯 Map preview
        ImageButton btnMapPreview = findViewById(R.id.btnMapPreview);
        btnMapPreview.setOnClickListener(v -> {
            Intent intent = new Intent(RequestRideActivity.this, MapViewerActivity.class);
            startActivity(intent);
        });

        // 📍 Share Location via SMS
        btnShareLocation.setOnClickListener(v -> {
            if (selectedPhoneNumber != null) {
                shareLocationViaSMS(selectedPhoneNumber);
            } else {
                Toast.makeText(this, "No rider selected to send location.", Toast.LENGTH_SHORT).show();
            }

        });

        // Request permissions needed
        requestLocationAndSmsPermissions();

        // Default: Requester mode
        riderSection.setVisibility(View.GONE);
        btnShareLocation.setVisibility(View.GONE);

    }

    /**
     * Fetches offline location and sends it to contact via SMS.
     */
    private void shareLocationViaSMS(String selectedPhoneNumber) {
        OfflineLocationHelper.LocationData loc = OfflineLocationHelper.fetchOfflineLocation(this);
        if (loc.success) {
            String payload = OfflineSmsHelper.formatPayload(
                    loc.latitude, loc.longitude, loc.accuracy, loc.signalDbm, loc.timingAdvance
            );
            OfflineSmsHelper.sendLocationSMS(RequestRideActivity.selectedPhoneNumber, payload);
        } else {
            Toast.makeText(this, "Location unavailable. Please enable GPS or permissions.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Requests necessary runtime permissions for SMS and location.
     */
    private void requestLocationAndSmsPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.SEND_SMS,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, PERMISSION_CODE);
        }
    }

}
