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

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject; // Import for Hilt
import dagger.hilt.android.AndroidEntryPoint; // Import for Hilt


/**
 *  RequestRideActivity allows users to input ride info, view map, and navigate back to chat.
 */
@AndroidEntryPoint
public class RequestRideActivity extends AppCompatActivity {

    private static final int PERMISSION_CODE = 101;
    private  String selectedPhoneNumber;

    @Inject // Hilt will inject this
    OfflineLocationHelper offlineLocationHelper;

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



        // Get selectedPhoneNumber from intent
        if (getIntent().hasExtra("riderPhone")) {
            selectedPhoneNumber = getIntent().getStringExtra("riderPhone");
        } else {
            Toast.makeText(this, "No rider selected for this request.", Toast.LENGTH_SHORT).show();
            finish(); // Or handle differently
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
           // btnShareLocation.setVisibility(View.GONE);
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

        btnMapPreview.setOnClickListener(v ->{
            MapViewerActivity.launch(RequestRideActivity.this);
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

    }

    /**
     * Fetches offline location and sends it to contact via SMS.
     */
    private void shareLocationViaSMS( String phoneNumber) {
        OfflineLocationHelper.LocationData loc = offlineLocationHelper.fetchOfflineLocation();
        if (loc != null && loc.success) {
            // Format the location data into an SMS payload
            String payload = OfflineSmsHelper.formatPayload(
                    loc.latitude, loc.longitude, loc.accuracy, loc.signalDbm, loc.timingAdvance
            );
            try {
                // Send the SMS with location information
                OfflineSmsHelper.sendLocationSMS(  phoneNumber ,payload);
                Toast.makeText(this, "Location sent via SMS to " + phoneNumber, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                // Handle any failure in sending (e.g., permission issue or SMS error)
                Toast.makeText(this, "Failed to send SMS. Please check permissions.", Toast.LENGTH_LONG).show();
            }
        } else {
            // Could not get location (GPS off or no permission)
            Toast.makeText(this, "Location unavailable. Please enable GPS or permissions.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Requests necessary runtime permissions for SMS and location.
     */
    private void requestLocationAndSmsPermissions() {
        List<String> permissionsNeeded = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.SEND_SMS);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (!permissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsNeeded.toArray(new String[0]), PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE) {
            boolean allGranted = true;
            if (grantResults.length == 0) { // User cancelled
                allGranted = false;
            } else {
                for (int result : grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        allGranted = false;
                        break;
                    }
                }
            }
            if (!allGranted) {
                Toast.makeText(this, "Some required permissions were not granted.", Toast.LENGTH_LONG).show();
            }
        }
    }
}
