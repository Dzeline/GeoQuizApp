package com.example.geoquiz.presentation.feature_map;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;


import com.example.geoquiz.R;
import com.example.geoquiz.data.local.database.LocationLogEntity;
import com.example.geoquiz.presentation.feature_chat.MainFunctionActivity;
import com.example.geoquiz.presentation.feature_role.RoleManager;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;


import java.util.List;

/**
 * Displays logged location points using OSMDroid in offline mode.
 */
public class MapViewerActivity extends AppCompatActivity {
    public static void launch(android.content.Context context) {
        Intent intent = new Intent(context, MapViewerActivity.class);
        context.startActivity(intent);
    }


    private MapView mapView;
    private MapViewerViewModel viewModel;
    private Marker currentLocationMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().setUserAgentValue(getPackageName());
        setContentView(R.layout.activity_map_viewer);

        //Map init
        mapView = findViewById(R.id.osmMap);
        mapView.setTileSource(new XYTileSource("OfflineMap",
                0, 18, 256, ".png", new String[]{}));
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(15.0);

        //Setup buttons based on role
        setupRoleBasedUI();

        //ViewModel initializing
        viewModel = new ViewModelProvider(this).get(MapViewerViewModel.class);

        // Observe location updates
        viewModel.getAllLogs().observe(this, this::updateMapWithLocations);

        // Start periodic location updates
        startLocationUpdates();

    }

    private void setupRoleBasedUI() {
        Button chatButton = findViewById(R.id.btnChat);
        Button historyButton = findViewById(R.id.btnHistory);

        switch (RoleManager.getRole(this)) {
            case RIDER:
                chatButton.setVisibility(View.GONE);
                historyButton.setVisibility(View.GONE);
                break;
            case USER:
                chatButton.setOnClickListener(v -> navigateToChat());
                historyButton.setOnClickListener(v -> showLocationHistory());
                break;
        }
    }

    private void startLocationUpdates() {
        // Update every 30 seconds
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                viewModel.fetchOfflineLocation(MapViewerActivity.this);
                handler.postDelayed(this, 30000);
            }
        }, 30000);
    }

    private void updateMapWithLocations(List<LocationLogEntity> logs) {
        if (logs == null || logs.isEmpty()) return;

        mapView.getOverlays().clear();

        // Plot all historical locations
        for (LocationLogEntity log : logs) {
            addMarkerToMap(log, false);
        }

        // Highlight most recent location
        LocationLogEntity latest = logs.get(logs.size()-1);
        addMarkerToMap(latest, true);

        // Center map on latest location
        mapView.getController().setCenter(new GeoPoint(latest.latitude, latest.longitude));
        mapView.invalidate();
    }

    private void addMarkerToMap(LocationLogEntity log, boolean isCurrent) {
        GeoPoint point = new GeoPoint(log.latitude, log.longitude);
        Marker marker = new Marker(mapView);
        marker.setPosition(point);

        if (isCurrent) {
            marker.setIcon(getResources().getDrawable(R.drawable.ic_current_location));
            currentLocationMarker = marker;
        } else {
            marker.setIcon(getResources().getDrawable(R.drawable.ic_history_location));
        }

        String info = String.format("Time: %s\nAccuracy: %.1fm",
                DateFormat.format("HH:mm", log.timestamp),
                log.accuracy);
        marker.setTitle(info);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mapView.getOverlays().add(marker);
    }

}
