package com.example.geoquiz;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class RequestRideActivity extends AppCompatActivity {

    private LinearLayout riderSection;
    private MaterialButton btnSwitchToRequester, btnSwitchToRider, btnShareLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.riderequest); // Links to riderequest.xml

        // View bindings
        riderSection = findViewById(R.id.riderSection);
        btnSwitchToRequester = findViewById(R.id.btnSwitchToRequester);
        btnSwitchToRider = findViewById(R.id.btnSwitchToRider);
        btnShareLocation = findViewById(R.id.btnShareLocation);

        // Default: Requester mode
        riderSection.setVisibility(View.GONE);
        btnShareLocation.setVisibility(View.GONE);

        // Switch roles
        btnSwitchToRequester.setOnClickListener(v -> {
            riderSection.setVisibility(View.GONE);
            btnShareLocation.setVisibility(View.GONE);
        });

        btnSwitchToRider.setOnClickListener(v -> {
            riderSection.setVisibility(View.VISIBLE);
            btnShareLocation.setVisibility(View.VISIBLE);
        });
    }

}
