package com.example.geoquiz.presentation.feature_map;

//import android.content.Context;
import android.util.Log; // For logging

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;


import com.example.geoquiz.data.local.database.LocationLogEntity;
import com.example.geoquiz.domain.repository.LocationLogRepository;
import com.example.geoquiz.util.OfflineLocationHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;
import javax.inject.Named;

import dagger.hilt.android.lifecycle.HiltViewModel;


@HiltViewModel
public class MapViewerViewModel extends ViewModel {
    private static final String TAG = "MapViewerViewModel"; // For logging

    private final LocationLogRepository logRepo;
    private final OfflineLocationHelper offlineLocationHelper;
    private final ExecutorService ioExecutor;

    @Inject
    public MapViewerViewModel(
            LocationLogRepository logRepo,
            OfflineLocationHelper offlineLocationHelper, // Hilt provides this
            @Named("ioExecutor") ExecutorService ioExecutor
    ){
         // Keep if direct DB access is still needed for some reason
        this.logRepo = logRepo;
        this.offlineLocationHelper = offlineLocationHelper;
        this.ioExecutor = ioExecutor;
        Log.d(TAG,"ViewModel initialized with injected OfflineLocationHelper.");
    }

    public LiveData<List<LocationLogEntity>> getAllLogs() {
        return this.logRepo.getAllLogs(); // This provides the data for the map and grid
    }

    public void fetchOfflineLocation() {
        Log.d(TAG, "Fetching offline location called in ViewModel."); // Log message updated
        // Call fetchOfflineLocation() on the INSTANCE of OfflineLocationHelper
        OfflineLocationHelper.LocationData data = offlineLocationHelper.fetchOfflineLocation();

        if (data == null) {
            Log.w(TAG, "offlineLocationHelper.fetchOfflineLocation returned null data."); // Updated log
            return;
        }

        Log.i(TAG, "Offline location data received: Success=" + data.success +
                ", Lat=" + data.latitude + ", Lon=" + data.longitude +
                ", Acc=" + data.accuracy + ", Signal=" + data.signalDbm + ", TA=" + data.timingAdvance);

        boolean hasValidCoordinates = (data.latitude != 0.0 || data.longitude != 0.0);
        boolean hasValidSignal = (data.signalDbm != -999 && data.signalDbm != 0); // Consider 0 also potentially invalid

        if (!data.success && !hasValidCoordinates && !hasValidSignal) {
            Log.d(TAG, "No meaningful location or cell data to log from unsuccessful fetch.");
            return;
        }
        if (data.latitude == 0.0 && data.longitude == 0.0 && data.accuracy == 0.0f && !hasValidSignal) {
            Log.d(TAG, "Skipping log for (0,0) point with no other meaningful data.");
            return;
        }

        LocationLogEntity log = new LocationLogEntity(
                System.currentTimeMillis(),
                data.latitude,
                data.longitude,
                data.accuracy,
                data.signalDbm,
                data.timingAdvance
        );

        // Using your existing executor for DB writes
        // Use the injected executor for database operations
        ioExecutor.execute(() -> {
            try {
                // Prefer repository pattern for insertion
                logRepo.insertLog(log); // Assuming logRepo.insertLog handles its own backgrounding or uses the DAO correctly
                Log.i(TAG, "New location log inserted via Repository: Lat=" + log.latitude + ", Lon=" + log.longitude);
            } catch (Exception e) {
                Log.e(TAG, "Error inserting location log into database via Repository.", e);
            }
        });
    }

    // buildGridFromLogs remains the same
    public List<String> buildGridFromLogs(List<LocationLogEntity> logs) {
        List<String> gridRepresentation = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            gridRepresentation.add(".");
        }
        if (logs == null || logs.isEmpty()) {
            return gridRepresentation;
        }
        for (LocationLogEntity log : logs) {
            if (log.latitude == 0.0 && log.longitude == 0.0 && log.accuracy == 0.0f) {
                continue;
            }
            int row = Math.abs((int) ((log.latitude * 10) % 10));
            int col = Math.abs((int) ((log.longitude * 10) % 10));
            int cellIndex = row * 10 + col;

            if (cellIndex >= 0 && cellIndex < 100) {
                gridRepresentation.set(cellIndex, "L");
            }
        }
        Log.d(TAG, "Built global grid from logs. Rep size: " + gridRepresentation.size());
        return gridRepresentation;
    }
}