package com.example.geoquiz.location;

import android.content.Context;
import android.location.Location;
public class FusedLocationProvider implements CustomLocationManager  {
    private final Context context;

    public FusedLocationProvider(Context context) {
        this.context = context;
    }

    @Override
    public Location getLastKnownLocation(String provider) {
        // Implement using FusedLocationProviderClient
        return null;
    }

}
