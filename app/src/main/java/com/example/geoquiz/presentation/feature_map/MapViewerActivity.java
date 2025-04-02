package com.example.geoquiz.presentation.feature_map;


import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;


import com.example.geoquiz.R;
import com.example.geoquiz.data.local.database.LocationLogEntity;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.Date;
import java.util.List;

/**
 * Displays logged location points using OSMDroid in offline mode.
 */
public class MapViewerActivity extends AppCompatActivity {

    private MapView mapView;
    private MapViewerViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Configuration.getInstance().setUserAgentValue(getPackageName());
        setContentView(R.layout.activity_map_viewer);

        //Map init
        mapView = findViewById(R.id.osmMap);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(15.0);
        mapView.getController().setCenter(new GeoPoint(37.7749, -122.4194)); // Sample lat/lon

           //ViewModel init
        viewModel = new ViewModelProvider(this).get(MapViewerViewModel.class);

        //Observe logs and plot markers
        viewModel.getAllLogs().observe(this, logs -> {
            if (logs == null || logs.isEmpty()) {
                Toast.makeText(this, "No locations logged yet", Toast.LENGTH_SHORT).show();
                return;
            }

            mapView.getOverlays().clear();
            plotMarkers(logs);
        });

    }

    /**
     * Plots location markers with info on map.
     *
     * @param logs List of stored location entries
     */
    private void plotMarkers(List<LocationLogEntity> logs) {
        for (LocationLogEntity log : logs) {
            GeoPoint point = new GeoPoint(log.latitude, log.longitude);
            Marker marker = new Marker(mapView);
            marker.setPosition(point);
            String formattedTime = android.text.format.DateFormat.format("yyyy-MM-dd HH:mm:ss", log.timestamp).toString();

            marker.setTitle("📍 " + formattedTime +
                    "\nAcc: " + log.accuracy + "m" +
                    "\nSignal: " + log.signalDbm + " dBm" +
                    "\nTA: " + log.timingAdvance);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            mapView.getOverlays().add(marker);
        }

        LocationLogEntity first = logs.get(0);
        mapView.getController().setCenter(new GeoPoint(first.latitude, first.longitude));
        mapView.invalidate();
    }
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume(); // required for OSMDroid
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause(); // required for OSMDroid
    }


}
