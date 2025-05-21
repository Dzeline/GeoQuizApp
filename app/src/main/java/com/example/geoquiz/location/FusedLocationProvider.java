package com.example.geoquiz.location;

import android.Manifest;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class FusedLocationProvider implements CustomLocationManager {
    private final FusedLocationProviderClient fusedClient;
    private final LocationManager locationManager;
    private final Context context;

    public FusedLocationProvider(Context context) {
        this.context = context;
        this.fusedClient = LocationServices.getFusedLocationProviderClient(context);
        this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public Location getLastKnownLocation(String provider) {
        if (ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null; // Handle permission request in Activity/Fragment
        }

        try {
            Task<Location> task = fusedClient.getLastLocation();
            Location fusedLocation = Tasks.await(task, 5, TimeUnit.SECONDS);

            if (fusedLocation != null) {
                return fusedLocation;
            } else {
                // Fallback to GPS or passive if fused is null (e.g., offline or rebooted)
                Location gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (gpsLocation != null) return gpsLocation;

                Location passiveLocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
                return passiveLocation;
            }
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            e.printStackTrace();
            // Retry with GPS if fused fails
            try {
                Location gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (gpsLocation != null) return gpsLocation;

                return locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
            } catch (SecurityException ex) {
                ex.printStackTrace();
                return null;
            }
    }
  }
}