package com.example.geoquiz.location;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import javax.inject.Inject;
public class CustomLocationManagerImpl implements CustomLocationManager {
    private final LocationManager locationManager;

    @Inject
    public CustomLocationManagerImpl(Context context) {
        this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public Location getLastKnownLocation(String gpsProvider) {
        try {
            return locationManager.getLastKnownLocation(gpsProvider);
        } catch (SecurityException e) {
            e.printStackTrace();
            return null;
        }
    }

}
