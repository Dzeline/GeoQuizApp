package com.example.geoquiz.presentation.feature_map;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;


import com.example.geoquiz.R;
import com.example.geoquiz.data.local.database.LocationLogEntity;
import com.example.geoquiz.presentation.feature_chat.MainFunctionActivity;
import com.example.geoquiz.presentation.feature_location_history.LocationHistoryActivity;
import com.example.geoquiz.presentation.feature_role.RoleManager;

import org.osmdroid.config.Configuration;
import org.osmdroid.config.IConfigurationProvider;

import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Displays logged location points using OSMDroid in offline mode.
 */
@AndroidEntryPoint
public class MapViewerActivity extends AppCompatActivity {

    private static final String TAG = "MapViewerActivity"; // For logging

    // Define a service area center (e.g., a known location in your city)
    // TODO: Replace with actual coordinates for your service area
    private static final double SERVICE_AREA_CENTER_LAT = 0.0; // Example: Nairobi, Kenya
    private static final double SERVICE_AREA_CENTER_LON = 0.0;
    private static final double INITIAL_MAP_ZOOM = 14.0; // Zoom level for the service area

    private MapView mapView;
    private MapViewerViewModel viewModel;
    private GridView gridView;
    private MapGridAdapter gridAdapter;
    private TextView tvAccuracy; // For displaying accuracy
    private ToggleButton toggleMapMode; // For switching views

    private final Handler locationUpdateHandler = new Handler(Looper.getMainLooper());
    private Runnable locationUpdateRunnable;


    public static void launch(android.content.Context context) {
        Intent intent = new Intent(context, MapViewerActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate started.");

        // IMPORTANT: Set up OSMDroid configuration BEFORE setting content view or initializing MapView
        IConfigurationProvider osmConfig = Configuration.getInstance();
        // Use app-specific external files directory (no special permissions needed on API 19+)
        // This path will be like: /sdcard/Android/data/com.example.geoquiz/files/osmdroid
        File osmdroidBasePath = new File(getExternalFilesDir(null), "osmdroid");
        if (!osmdroidBasePath.exists() && !osmdroidBasePath.mkdirs()) {
            Toast.makeText(this, "Failed to create osmdroid base directory.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Failed to create osmdroid base directory: " + osmdroidBasePath.getAbsolutePath());
        }
        osmConfig.setOsmdroidBasePath(osmdroidBasePath);

        // This is where OSMDroid will look for tiles, e.g., osmdroidBasePath/tiles/
        // And for our specific source: osmdroidBasePath/tiles/OfflineMapTiles/Z/X/Y.png
        File osmdroidTileCache = new File(osmdroidBasePath, "tiles");
        if (!osmdroidTileCache.exists() && !osmdroidTileCache.mkdirs()) {
            Toast.makeText(this, "Failed to create osmdroid tile directory.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Failed to create osmdroid tile directory: " + osmdroidTileCache.getAbsolutePath());
        }
        osmConfig.setOsmdroidTileCache(osmdroidTileCache);
        osmConfig.setUserAgentValue(getPackageName()); // Good practice

        setContentView(R.layout.activity_map_viewer);

        //Map init
        mapView = findViewById(R.id.osmMap);
        gridView = findViewById(R.id.gridMap);
        tvAccuracy = findViewById(R.id.tvAccuracy);
        toggleMapMode = findViewById(R.id.toggleMapMode);


        //Use my custom OfflineTileSource
        mapView.setTileSource(new OfflineTileSource());
        mapView.setMultiTouchControls(true);


        // Set a default map center, e.g., a known point or (0,0)
        // This will be updated once location logs are available.
        GeoPoint serviceCenter = new GeoPoint(SERVICE_AREA_CENTER_LAT, SERVICE_AREA_CENTER_LON);
        mapView.getController().setZoom(INITIAL_MAP_ZOOM);
        mapView.getController().setCenter(serviceCenter);

        // Setup grid adapter

        gridAdapter = new MapGridAdapter(this, new ArrayList<>(), serviceCenter);
        gridView.setAdapter(gridAdapter);

        //Setup buttons based on role
        setupToggleView();
        setupRoleBasedUI();


        //ViewModel initializing
        viewModel = new ViewModelProvider(this).get(MapViewerViewModel.class);

        // Observe location updates
        viewModel.getAllLogs().observe(this, logs -> {
            Log.d(TAG, "Location logs observed. Count: " + (logs != null ? logs.size() : "null"));
            updateMapAndGrid(logs);
        });
        // Listen to map movements to keep the grid's reference center updated if desired
        mapView.addMapListener(new MapListener() {
            @Override
            public boolean onScroll(ScrollEvent event) {
                updateGridCenterFromMap();
                return false;
            }

            @Override
            public boolean onZoom(ZoomEvent event) {
                updateGridCenterFromMap();
                return false;
            }
        });

        startLocationUpdates();
        Log.d(TAG, "onCreate finished.");

    }

    private void updateGridCenterFromMap() {
        if (gridAdapter != null && viewModel.getAllLogs().getValue() != null) {
            GeoPoint currentMapCenter = new GeoPoint(mapView.getMapCenter().getLatitude(), mapView.getMapCenter().getLongitude());
            // The grid is centered on a fixed SERVICE_CENTER, but if you want it to follow map view for visual reference:
            // gridAdapter.updateData(viewModel.getAllLogs().getValue(), currentMapCenter);
            // For a fixed service area grid, the center passed to gridAdapter should remain the serviceCenter.
            // The display of points *within* that fixed grid will update.
            // So, we just need to ensure the logs are passed.
            // The MapGridAdapter already uses its 'currentCenter' (which should be the service center) for its calculations.
            // If the grid's visual representation itself doesn't move with the map, but its content updates,
            // we just need to ensure it gets the latest logs and the *fixed* serviceCenter around which it operates.
            // Let's assume the grid's 'currentCenter' is the service area center and doesn't change with map pan.
            // The updateData call in updateMapAndGrid will handle refreshing its content.
            // No specific action needed here if grid is fixed to a service area.
            // However, if map center IS the grid center, then:
            gridAdapter.updateData(viewModel.getAllLogs().getValue(), currentMapCenter);
            Log.d(TAG, "Grid center updated to map center.");
        }
    }


    private void setupToggleView() {
        toggleMapMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) { // Grid View
                mapView.setVisibility(View.GONE);
                gridView.setVisibility(View.VISIBLE);
                Log.d(TAG, "Switched to Grid View");
            } else { // Map View
                mapView.setVisibility(View.VISIBLE);
                gridView.setVisibility(View.GONE); // Or View.INVISIBLE if you want it to take space
                Log.d(TAG, "Switched to Map View");
            }
        });
        // Initial state: Map View visible
        mapView.setVisibility(View.VISIBLE);
        gridView.setVisibility(View.GONE);
        toggleMapMode.setChecked(false);
    }

    private void updateMapAndGrid(List<LocationLogEntity> logs) {
        Log.d(TAG, "updateMapAndGrid called.");
        mapView.getOverlays().clear();

        GeoPoint gridCenterForAdapter = new GeoPoint(SERVICE_AREA_CENTER_LAT, SERVICE_AREA_CENTER_LON); // TODO: Use your actual service center GeoPoint

        if (logs == null || logs.isEmpty()) {
            Toast.makeText(this, "No location logs to display.", Toast.LENGTH_SHORT).show();
            Log.i(TAG, "No location logs to display.");
            gridAdapter.updateData(new ArrayList<>(), gridCenterForAdapter); // Pass the fixed service center
            if (tvAccuracy != null) tvAccuracy.setText(getString(R.string.accuracy_default_text)); // Update from strings.xml
            mapView.getController().setCenter(gridCenterForAdapter);
            mapView.invalidate();
            return;
        }

        LocationLogEntity latestLog = logs.get(0); // Assumes sorted by timestamp descending
        GeoPoint latestGeoPoint = new GeoPoint(latestLog.latitude, latestLog.longitude);

        Log.d(TAG, "Latest log: Lat=" + latestLog.latitude + ", Lon=" + latestLog.longitude + " Acc: " + latestLog.accuracy);

        for (LocationLogEntity log : logs) {
            if (log.latitude != 0.0 || log.longitude != 0.0) { // Don't add markers for (0,0) if they are invalid
                addMarkerToMap(log, log.timestamp == latestLog.timestamp);
            }
        }

        // Only pan to latest log if it's a valid coordinate (not 0,0 default from failed fetch)
        if (latestLog.latitude != 0.0 || latestLog.longitude != 0.0) {
            mapView.getController().animateTo(latestGeoPoint);
        } else {
            // If latest log is 0,0, perhaps center on the fixed service area or last known valid point
            mapView.getController().setCenter(gridCenterForAdapter); // falling back to service center
        }

        showAccuracyInfo(latestLog.accuracy);

        // The gridAdapter should be updated with all logs, and ITS definition of center (service location)
        gridAdapter.updateData(logs, gridCenterForAdapter); // Pass the fixed service center
        Log.d(TAG, "MapGridAdapter data updated with fixed service center.");

        mapView.invalidate();
    }

    private void setupRoleBasedUI() {
        // Your existing setupRoleBasedUI code - seems correct
        Button chatButton = findViewById(R.id.btnChat);
        Button historyButton = findViewById(R.id.btnHistory);

        RoleManager.Role currentRole = RoleManager.getRole(this);
        if (currentRole == null) {
            chatButton.setVisibility(View.GONE);
            historyButton.setVisibility(View.GONE);
            Toast.makeText(this, "User role not set.", Toast.LENGTH_LONG).show();
            Log.w(TAG, "User role not set during UI setup.");
            return;
        }

        switch (currentRole) {
            case RIDER:
                chatButton.setVisibility(View.GONE);
                historyButton.setVisibility(View.GONE);
                break;
            case USER:
            default:
                chatButton.setVisibility(View.VISIBLE);
                historyButton.setVisibility(View.VISIBLE);
                chatButton.setOnClickListener(v -> {
                    Log.d(TAG, "Chat button clicked.");
                    startActivity(new Intent(this, MainFunctionActivity.class));
                });
                historyButton.setOnClickListener(v -> {
                    Log.d(TAG, "History button clicked.");
                    startActivity(new Intent(this, LocationHistoryActivity.class));
                });
                break;
        }
        Log.d(TAG, "Role based UI setup complete for role: " + currentRole);
    }

    private void startLocationUpdates() {
        Log.d(TAG, "Attempting to start location updates.");
        viewModel.fetchOfflineLocation(); // Pass context

        locationUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Periodic location update runnable executing.");
                viewModel.fetchOfflineLocation(); // Pass context
                if (!isFinishing() && !isDestroyed()) {
                    locationUpdateHandler.postDelayed(this, 30000);
                } else {
                    Log.d(TAG, "Activity is finishing/destroyed, not rescheduling location update.");
                }
            }
        };
        locationUpdateHandler.postDelayed(locationUpdateRunnable, 5000); // Initial delay
        Log.i(TAG, "Location updates scheduled.");
    }

    private void addMarkerToMap(LocationLogEntity log, boolean isCurrent) {
        GeoPoint point = new GeoPoint(log.latitude, log.longitude);
        Marker marker = new Marker(mapView);
        marker.setPosition(point);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

        try {
            if (isCurrent) {
                marker.setIcon(getResources().getDrawable(R.drawable.ic_current_location, getTheme()));
            } else {
                marker.setIcon(getResources().getDrawable(R.drawable.ic_history_location, getTheme()));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting marker icon", e);
        }

        String timeFormatted = DateFormat.format("HH:mm:ss", log.timestamp).toString();
        String info = String.format(Locale.getDefault(), "Time: %s\nAcc: %.1fm\nSig: %ddBm TA: %d",
                timeFormatted, log.accuracy, log.signalDbm, log.timingAdvance);
        marker.setTitle(info);
        mapView.getOverlays().add(marker);
    }

    private void showAccuracyInfo(float accuracy) {
        String accuracyText = String.format(Locale.getDefault(), "Accuracy: %.1fm", accuracy);
        if (tvAccuracy != null) {
            tvAccuracy.setText(accuracyText);
        } else { // Fallback to Toast if TextView isn't ready or wired
            Toast.makeText(this, accuracyText, Toast.LENGTH_SHORT).show();
        }
        Log.d(TAG, "Accuracy displayed: " + accuracyText);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume.");
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause.");
        if (mapView != null) {
            mapView.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy.");
        if (locationUpdateRunnable != null) {
            locationUpdateHandler.removeCallbacks(locationUpdateRunnable);
            Log.i(TAG, "Location update callbacks removed.");
        }
        if (mapView != null) {
            mapView.onDetach();
            Log.i(TAG, "MapView detached.");
        }
    }
}