package com.example.geoquiz.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.telephony.CellInfo;
import android.telephony.CellInfoLte;
import android.telephony.CellSignalStrengthLte;
import android.telephony.TelephonyManager;

import androidx.core.app.ActivityCompat;


public class OfflineLocationHelper {

    public static class LocationData {
        public double latitude;
        public double longitude;
        public float accuracy;
        public int signalDbm = -999;
        public int timingAdvance = -1;
        public boolean success;

        public LocationData(boolean success) {
            this.success = success;
        }
    }

    public static LocationData fetchOfflineLocation(Context context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return new LocationData(false); // Permissions not granted
        }

        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location == null) {
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        if (location == null) {
            return new LocationData(false); // No available location
        }

        LocationData result = new LocationData(true);
        result.latitude = location.getLatitude();
        result.longitude = location.getLongitude();
        result.accuracy = location.getAccuracy();

        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            for (CellInfo info : telephonyManager.getAllCellInfo()) {
                if (info instanceof CellInfoLte) {
                    CellSignalStrengthLte strength = ((CellInfoLte) info).getCellSignalStrength();
                    result.signalDbm = strength.getDbm();
                    result.timingAdvance = strength.getTimingAdvance();
                    break;
                }
            }
        }

        return result;
    }

}
