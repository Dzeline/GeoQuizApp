package com.example.geoquiz.presentation.feature_location_history;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import androidx.lifecycle.ViewModelProvider;

import com.example.geoquiz.R;
import com.example.geoquiz.data.local.database.LocationLogEntity;
import com.example.geoquiz.presentation.feature_map.MapViewerActivity;

public class LocationHistoryActivity  extends AppCompatActivity{

    private TextView tvHistory;


    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_history);

        tvHistory = findViewById(R.id.tvHistory);
        Button btnViewMap = findViewById(R.id.btnViewMap);

        LocationHistoryViewModel viewModel = new ViewModelProvider(this).get(LocationHistoryViewModel.class);

        btnViewMap.setOnClickListener(v -> {
            MapViewerActivity.launch(this);
        });

        viewModel.getLogs().observe(this, logs -> {
            if (logs == null || logs.isEmpty()) {
                Toast.makeText(this, "No location logs available", Toast.LENGTH_SHORT).show();
                tvHistory.setText("No logs found");
                return;
            }
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
