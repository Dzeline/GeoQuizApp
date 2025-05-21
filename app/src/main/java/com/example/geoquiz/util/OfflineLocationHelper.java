package com.example.geoquiz.util;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;


import com.example.geoquiz.data.local.database.LocationLogDao;
import com.example.geoquiz.data.local.database.LocationLogEntity;

import java.util.concurrent.ExecutorService;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
public class OfflineLocationHelper {
    private static final String TAG = "OfflineLocationHelper"; // for logging

    private final Application application; // For system services
    private final LocationLogDao locationLogDao; // For database access
    private final ExecutorService databaseExecutor; // For running DB queries off main thread
    // LocationData class remains a public static nested class
    public static class LocationData {
        public boolean success;
        public double latitude = 0.0;
        public double longitude = 0.0;
        public float accuracy = 0.0f;
        public int signalDbm = -999;
        public int timingAdvance = -1;

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

    @Inject // Hilt will provide these dependencies
    public OfflineLocationHelper(
            Application application,
            LocationLogDao locationLogDao,
            @Named("ioExecutor") ExecutorService databaseExecutor
    ) {
        this.application = application;
        this.locationLogDao = locationLogDao;
        this.databaseExecutor = databaseExecutor; // Use this for background tasks
        Log.d(TAG, "OfflineLocationHelper instance created with Hilt and named ioExecutor.");
    }


    public LocationData fetchOfflineLocation() {
        Log.d(TAG, "Attempting to fetch offline location (instance method).");
        Context context = this.application.getApplicationContext(); // Use the application context

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Location permissions not granted.");
            return new LocationData(false);
        }

        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null) {
            Log.e(TAG, "LocationManager is null.");
            return new LocationData(false);
        }

        LocationData resultData = tryLocationProviders(context, locationManager);
        if (resultData.success && (resultData.latitude != 0.0 || resultData.longitude != 0.0) ) {
            Log.i(TAG, "Location found via Android Location Providers.");
            return resultData;
        }

        // Try to get the last location from our local database (This will block until result is back)
        // This is the tricky part for synchronous return with background execution.
        // For now, let's assume the ViewModel will handle if this step is slow.
        // A proper solution would make this helper fully async or the caller handle async.
        // We are removing the direct DB call from this immediate path to avoid main thread block.
        // The ViewModel should be responsible for combining this helper's output with its database knowledge.
        // If we must keep it, the databaseExecutor needs to be used with a Future or CountDownLatch
        // to make it block, which complicates this simple helper.
        Log.d(TAG, "No direct location fix from providers. Attempting to use current cell tower data for context.");
        enhanceWithCellInfo(context, resultData); // Enhance the existing resultData

        // If no location from providers, try to get current cell info.
        if (!resultData.success || (resultData.latitude == 0.0 && resultData.longitude == 0.0)) {
            Log.d(TAG, "No direct location fix from providers. Using current cell tower data for context.");
            LocationData cellDataOnly = new LocationData(false); // Start with no lat/lon
            enhanceWithCellInfo(context, cellDataOnly);
            if (cellDataOnly.signalDbm != -999) {
                cellDataOnly.success = true; // Success means we got *some* data (cell signal)
                // If bestLocation was 0,0 but we got cell data, merge it:
                resultData.signalDbm = cellDataOnly.signalDbm;
                resultData.timingAdvance = cellDataOnly.timingAdvance;
                resultData.success = true; // Update success if cell info was found
                Log.i(TAG, "Enhanced with cell data. Lat/Lon might still be 0.0 if no provider fix.");
            } else {
                Log.w(TAG, "Failed to obtain any cell info either.");
            }
        }

        if (!resultData.success) {
            Log.w(TAG, "Failed to obtain any location or cell info.");
        }
        return resultData;
    }

    private LocationData tryLocationProviders(Context context, LocationManager locationManager) {
        Location bestLocation = null;

        String[] providers = {LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER, LocationManager.PASSIVE_PROVIDER};
        for (String provider : providers) {
            try {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    continue;
                }
                Location location = locationManager.getLastKnownLocation(provider);
                if (location != null) {
                    Log.i(TAG, provider + " Location: Lat=" + location.getLatitude() + ", Lon=" + location.getLongitude() + ", Acc=" + location.getAccuracy() + ", Time=" + location.getTime());
                    if (isBetterLocation(location, bestLocation)) {
                        bestLocation = location;
                    }
                }
            } catch (SecurityException e) {
                Log.e(TAG, "SecurityException for provider " + provider + ": " + e.getMessage());
            }
        }

        if (bestLocation != null) {
            Log.i(TAG, "Using best last known location from provider: " + bestLocation.getProvider());
            LocationData data = fromAndroidLocation(bestLocation);
            enhanceWithCellInfo(context, data); // Enhance with current cell info regardless
            data.success = true;
            return data;
        }
        return new LocationData(false); // No location found from providers
    }


    // This method used to access DB directly. Now DB access is handled by repository/ViewModel.
    // It's removed from the helper to decouple concerns.
    // If you need a last known location from DB to seed this helper,
    // the ViewModel should query it (async) and potentially pass it to a variant of fetchOfflineLocation.
    /*
    private LocationData getLastLocationFromLocalDatabase() {
        // This must run on a background thread
        // This is simplified. A real implementation would use a Future or callback.
        final LocationData[] dbResult = {new LocationData(false)};
        try {
            // This is still problematic if called synchronously from main thread.
            // The executor needs proper handling for synchronous return.
            // For true backgrounding, this method itself should be async.
            List<LocationLogEntity> logs = locationLogDao.getRecentLogsSync(System.currentTimeMillis() - (60 * 60 * 1000));
            if (logs != null && !logs.isEmpty()) {
                LocationLogEntity latest = logs.get(0);
                Log.i(TAG, "DB log: Lat=" + latest.latitude + ", Lon=" + latest.longitude);
                dbResult[0] = new LocationData(true, latest.latitude, latest.longitude, latest.accuracy, latest.signalDbm, latest.timingAdvance);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error accessing local database for logs: " + e.getMessage(), e);
        }
        return dbResult[0];
    }
    */

    private void enhanceWithCellInfo(Context context, LocationData locationData) {
        // ... your existing enhanceWithCellInfo is good, just make it non-static ...
        // (Removed static keyword)
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
                            if (info instanceof CellInfoLte) {
                                CellSignalStrengthLte strength = ((CellInfoLte) info).getCellSignalStrength();
                                locationData.signalDbm = strength.getDbm();
                                locationData.timingAdvance = strength.getTimingAdvance();
                                Log.d(TAG, "LTE Cell Info: DBM=" + locationData.signalDbm + ", TA=" + locationData.timingAdvance);
                                break;
                            } else if (info instanceof CellInfoGsm) {
                                CellSignalStrengthGsm strength = ((CellInfoGsm) info).getCellSignalStrength();
                                locationData.signalDbm = strength.getDbm();
                                locationData.timingAdvance = -1;
                                Log.d(TAG, "GSM Cell Info: DBM=" + locationData.signalDbm);
                                break;
                            } else if (info instanceof CellInfoWcdma) {
                                CellSignalStrengthWcdma strength = ((CellInfoWcdma) info).getCellSignalStrength();
                                locationData.signalDbm = strength.getDbm();
                                locationData.timingAdvance = -1;
                                Log.d(TAG, "WCDMA Cell Info: DBM=" + locationData.signalDbm);
                                break;
                            }
                        }
                    }
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

    private LocationData fromAndroidLocation(Location location) {
        // (Removed static keyword)
        Log.d(TAG, "Converting Android Location object. Provider: " + location.getProvider() + ", Acc: " + location.getAccuracy());
        return new LocationData(true, location.getLatitude(), location.getLongitude(), location.getAccuracy(), -999, -1);
    }

    // isBetterLocation and isSameProvider can remain static or be instance methods
    protected static boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) return true;
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > (1000 * 60 * 2);
        boolean isSignificantlyOlder = timeDelta < -(1000 * 60 * 2);
        if (isSignificantlyNewer) return true;
        if (isSignificantlyOlder) return false;
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isNewer = timeDelta > 0;
        if (isMoreAccurate) return true;
        if (isNewer && !isLessAccurate) return true;
        boolean isFromSameProvider = isSameProvider(location.getProvider(), currentBestLocation.getProvider());
        if (isNewer && ! (accuracyDelta > 200) && isFromSameProvider) return true;
        return false;
    }

    private static boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) return provider2 == null;
        return provider1.equals(provider2);
    }
}
