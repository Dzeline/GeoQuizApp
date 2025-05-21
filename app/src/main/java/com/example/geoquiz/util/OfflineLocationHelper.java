package com.example.geoquiz.util;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.RequiresPermission;
import androidx.core.app.ActivityCompat;



import java.util.List;
import java.util.ArrayList;


import javax.inject.Inject;
//import javax.inject.Named;
import javax.inject.Singleton;

@Singleton // Make the helper a Singleton if appropriate, managed by Hilt
public class OfflineLocationHelper {
    private static final String TAG = "OfflineLocationHelper";

    private final Application application; // Hilt will provide this

    // LocationData class definition remains the same
    public static class LocationData {
        public boolean success;
        public double latitude = 0.0;
        public double longitude = 0.0;
        public float accuracy = 0.0f;
        public int signalDbm = -999;
        public int timingAdvance = -1; // Default to -1 or another indicator of not available

        public LocationData(boolean success) {
            this.success = success;
        }

        public LocationData(boolean success, double latitude, double longitude, float accuracy, int signalDbm, int timingAdvance) {
            this.success = success;
            this.latitude = latitude;
            this.longitude = longitude;
            this.accuracy = accuracy;
            this.signalDbm = signalDbm;
            this.timingAdvance = timingAdvance;
        }
    }

    @Inject // Constructor for Hilt injection
    public OfflineLocationHelper(Application application) {
        this.application = application;
        Log.d(TAG, "OfflineLocationHelper instance created with Hilt.");
    }

    // Instance method now
    public LocationData fetchOfflineLocation() {
        Log.d(TAG, "Attempting to fetch offline location (instance method).");
        Context context = this.application.getApplicationContext();
        LocationData resultData = new LocationData(false);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Location permissions not granted.");
            return resultData; // Permissions not granted
        }

        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null) {
            Log.e(TAG, "LocationManager is null.");
            return resultData;
        }

        // 1. Try to get last known GPS location
        Location gpsLocation = null;
        try {
            gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException getting GPS location: " + e.getMessage());
        }
        if (gpsLocation != null) {
            Log.i(TAG, "Using last known GPS location.");
            resultData = fromAndroidLocation(gpsLocation);
            enhanceWithCellInfo(context, resultData); // Pass context here
            resultData.success = true;
            return resultData;
        }

        // 2. Try network-based location
        Location networkLocation = null;
        try {
            networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException getting Network location: " + e.getMessage());
        }
        if (networkLocation != null) {
            Log.i(TAG, "Using last known Network location.");
            resultData = fromAndroidLocation(networkLocation);
            enhanceWithCellInfo(context, resultData); // Pass context here
            resultData.success = true;
            return resultData;
        }

        // 3. Try PASSIVE provider
        Location passiveLocation = null;
        try {
            passiveLocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException getting Passive location: " + e.getMessage());
        }
        if (passiveLocation != null) {
            Log.i(TAG, "Using last known Passive location.");
            resultData = fromAndroidLocation(passiveLocation);
            enhanceWithCellInfo(context, resultData); // Pass context here
            resultData.success = true;
            return resultData;
        }

        // 4. Removed getLastLocationFromLocalDatabase directly from here.
        // The ViewModel should handle combining provider data with DB data if needed.

        // 5. Final fallback to current cell tower info for estimation
        Log.d(TAG, "No direct location fix from providers. Attempting to use current cell tower data for context.");
        LocationData cellDataOnly = new LocationData(false);
        enhanceWithCellInfo(context, cellDataOnly); // Pass context here
        if (cellDataOnly.signalDbm != -999) {
            Log.i(TAG, "Obtained current cell tower data. No direct location fix. Signal: " + cellDataOnly.signalDbm);
            // Merge cell data into resultData if it's currently empty or unsuccessful
            if (!resultData.success || (resultData.latitude == 0.0 && resultData.longitude == 0.0)) {
                resultData.signalDbm = cellDataOnly.signalDbm;
                resultData.timingAdvance = cellDataOnly.timingAdvance;
                resultData.success = true; // Mark success if we got *any* cell data
            }
            return resultData;
        }

        Log.w(TAG, "Failed to obtain any offline location fix or cell info.");
        return resultData; // Could still be success=false if no providers and no cell info
    }

    // Made non-static, requires instance
    private void enhanceWithCellInfo(Context context, LocationData locationData) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (tm == null) {
            Log.w(TAG, "TelephonyManager is null, cannot enhance with cell info.");
            return;
        }

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            try {
                List<CellInfo> cellInfoList = tm.getAllCellInfo();
                if (cellInfoList != null && !cellInfoList.isEmpty()) {
                    for (CellInfo info : cellInfoList) {
                        if (info.isRegistered()) {
                            if (info instanceof CellInfoLte lteInfo) { // Modern pattern matching
                                CellSignalStrengthLte strength = lteInfo.getCellSignalStrength();
                                locationData.signalDbm = strength.getDbm();
                                locationData.timingAdvance = strength.getTimingAdvance();
                                Log.d(TAG, "LTE Cell Info: DBM=" + locationData.signalDbm + ", TA=" + locationData.timingAdvance);
                                return; // Use the first registered LTE cell and exit
                            } else if (info instanceof CellInfoGsm gsmInfo) {
                                CellSignalStrengthGsm strength = gsmInfo.getCellSignalStrength();
                                locationData.signalDbm = strength.getDbm();
                                locationData.timingAdvance = -1;
                                Log.d(TAG, "GSM Cell Info: DBM=" + locationData.signalDbm);
                                return; // Use the first registered GSM cell and exit
                            } else if (info instanceof CellInfoWcdma wcdmaInfo) {
                                CellSignalStrengthWcdma strength = wcdmaInfo.getCellSignalStrength();
                                locationData.signalDbm = strength.getDbm();
                                locationData.timingAdvance = -1;
                                Log.d(TAG, "WCDMA Cell Info: DBM=" + locationData.signalDbm);
                                return; // Use the first registered WCDMA cell and exit
                            }
                        }
                    }
                    Log.d(TAG, "No registered LTE, GSM, or WCDMA cell info found.");
                } else {
                    Log.d(TAG, "getAllCellInfo returned null or empty list.");
                }
            } catch (SecurityException e) {
                Log.e(TAG, "SecurityException getting cell info: " + e.getMessage());
            }
        } else {
            Log.w(TAG, "ACCESS_FINE_LOCATION not granted for cell info.");
        }
    }

    // Made non-static
    private LocationData fromAndroidLocation(Location location) {
        Log.d(TAG, "Converting Android Location object. Provider: " + location.getProvider() + ", Acc: " + location.getAccuracy());
        return new LocationData(true, location.getLatitude(), location.getLongitude(), location.getAccuracy(), -999, -1);
    }
}