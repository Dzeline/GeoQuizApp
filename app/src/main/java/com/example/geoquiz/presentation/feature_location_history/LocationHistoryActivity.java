package com.example.geoquiz.presentation.feature_location_history;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import androidx.lifecycle.ViewModelProvider;

import com.example.geoquiz.R;
import com.example.geoquiz.data.local.database.LocationLogEntity;

public class LocationHistoryActivity  extends AppCompatActivity{

    private TextView tvHistory;


    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_history);

        tvHistory = findViewById(R.id.tvHistory);
        LocationHistoryViewModel viewModel = new ViewModelProvider(this).get(LocationHistoryViewModel.class);

        viewModel.getLogs().observe(this, logs -> {
            StringBuilder builder = new StringBuilder();

            for (LocationLogEntity log : logs) {
                builder.append("📍 Time: ").append(log.timestamp)
                        .append("\nLat: ").append(log.latitude)
                        .append(" | Lon: ").append(log.longitude)
                        .append("\nAccuracy: ").append(log.accuracy).append("m")
                        .append("\nSignal: ").append(log.signalDbm).append(" dBm")
                        .append(" | TA: ").append(log.timingAdvance)
                        .append("\n\n");
            }

            tvHistory.setText(builder.toString());
        });




    }

}
