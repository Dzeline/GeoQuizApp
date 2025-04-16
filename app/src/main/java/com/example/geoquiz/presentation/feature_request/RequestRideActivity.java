package com.example.geoquiz.presentation.feature_request;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.geoquiz.R;
import com.example.geoquiz.presentation.feature_map.MapViewerActivity;
import com.example.geoquiz.presentation.feature_chat.MainFunctionActivity;
import com.example.geoquiz.presentation.feature_role.RoleManager;
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


        RoleManager.Role role = RoleManager.getRole(this);
        if (role != RoleManager.Role.USER && role != RoleManager.Role.RIDER) {
            Toast.makeText(this, "Unknown role", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }



        selectedPhoneNumber = getIntent().getStringExtra("riderPhone");
        if (selectedPhoneNumber == null) {
            Toast.makeText(this, "No rider selected", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // View bindings
        LinearLayout riderSection = findViewById(R.id.riderSection);
        MaterialButton btnShareLocation = findViewById(R.id.btnShareLocation);
        MaterialButton btnBackToChat = findViewById(R.id.btnBacktoChat);
        ImageButton btnMapPreview = findViewById(R.id.btnMapPreview);

        // Role-specific visibility.
        if (role == RoleManager.Role.USER) {
            riderSection.setVisibility(View.GONE);
            btnShareLocation.setVisibility(View.GONE);
        } else {
            riderSection.setVisibility(View.VISIBLE);
            btnShareLocation.setVisibility(View.VISIBLE);
        }

        // 🔙 Handle Back to Chat button
        btnBackToChat.setOnClickListener(v -> {

            Intent intent;
            if (RoleManager.getRole(this) == RoleManager.Role.RIDER) {
                intent = new Intent(RequestRideActivity.this, com.example.geoquiz.presentation.feature_rider.RiderActivity.class);
            } else {
                intent = new Intent(RequestRideActivity.this, MainFunctionActivity.class);
            }

            intent.putExtra("riderPhone",selectedPhoneNumber);
            startActivity(intent);
            finish();
        });

        // 🎯 Map preview

        btnMapPreview.setOnClickListener(v -> MapViewerActivity.launch(RequestRideActivity.this));

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
        //riderSection.setVisibility(View.GONE);
       // btnShareLocation.setVisibility(View.GONE);

    }

    /**
     * Fetches offline location and sends it to contact via SMS.
     */
    private void shareLocationViaSMS( String phoneNumber) {
        OfflineLocationHelper.LocationData loc = OfflineLocationHelper.fetchOfflineLocation(this);
        if (loc != null && loc.success) {
            String payload = OfflineSmsHelper.formatPayload(
                    loc.latitude, loc.longitude, loc.accuracy, loc.signalDbm, loc.timingAdvance
            );
            OfflineSmsHelper.sendLocationSMS(payload,phoneNumber);
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
    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_CODE) {
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Required permissions not granted.", Toast.LENGTH_SHORT).show();
                    break;
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

}
