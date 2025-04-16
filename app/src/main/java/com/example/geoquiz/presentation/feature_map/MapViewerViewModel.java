package com.example.geoquiz.presentation.feature_map;


import android.content.Context;

import androidx.lifecycle.LiveData;

import androidx.lifecycle.ViewModel;

import com.example.geoquiz.data.local.database.GeoQuizDatabase;
import com.example.geoquiz.data.local.database.LocationLogEntity;
import com.example.geoquiz.domain.repository.LocationLogRepository;
import com.example.geoquiz.util.OfflineLocationHelper;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * ViewModel for MapViewerActivity.
 * Manages grid representation of map data and location fetching.
 */
@HiltViewModel
public class MapViewerViewModel extends ViewModel {
    private final LocationLogRepository logRepo;
    private final LiveData<List<LocationLogEntity>> gridData ;
    private final GeoQuizDatabase db;



    /**
     * Constructor initializes database reference and grid.
     *
     * @param  logRepo Repository for location logs
     */
    @Inject
    public MapViewerViewModel(GeoQuizDatabase db, LocationLogRepository logRepo) {
        this.db = db;
        this.logRepo = logRepo ;
        this.gridData =logRepo.getAllLogs();

    }


    /**
     * Exposes a LiveData list representing a 10x10 map grid.
     *
     * @return LiveData list of grid cell values
     */
    public LiveData<List<LocationLogEntity>> getGridData() {
        return this.gridData;
    }

    public LiveData<List<LocationLogEntity>> getAllLogs() {
        return this.logRepo.getAllLogs();
    }

    /**
     * Fetches offline location, updates map grid, and logs the location.
     *
     * @param context Context for accessing location services
     */
    public void fetchOfflineLocation(Context context) {
        OfflineLocationHelper.LocationData data = OfflineLocationHelper.fetchOfflineLocation(context);

        if (!data.success) return;

        // save to DB
        LocationLogEntity log=new LocationLogEntity(
                System.currentTimeMillis(),
                data.latitude,
                data.longitude,
                data.accuracy,
                data.signalDbm,
                data.timingAdvance
        );
       GeoQuizDatabase.databaseWriteExecutor.execute(()-> db.locationLogDao().insertLog(log));
    }

    /**
     * Converts location logs into a 10x10 grid of strings.
     * "U" = User location, "T" = Cell Tower, "." = Empty
     */
    public List<String> buildGridFromLogs(List<LocationLogEntity> logs) {
        List<String> grid = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            grid.add("."); // default empty
        }

        for (LocationLogEntity log : logs) {
            int row = (int) ((log.latitude * 10) % 10);
            int col = (int) ((log.longitude * 10) % 10);
            int cellIndex = row * 10 + col;

            if (cellIndex >= 0 && cellIndex < 100) {
                grid.set(cellIndex, "U"); // Can enhance to distinguish "T" later
            }
        }

        return grid;
    }

}
