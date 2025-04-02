package com.example.geoquiz.location;

import android.location.Location;

public interface CustomLocationManager {
    Location getLastKnownLocation(String gpsProvider);
}
