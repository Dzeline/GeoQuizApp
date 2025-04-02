package com.example.geoquiz.presentation.feature_map;


import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.geoquiz.data.local.database.GeoQuizDatabase;
import com.example.geoquiz.data.local.database.LocationLogEntity;
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

    private final MutableLiveData<List<String>> gridData = new MutableLiveData<>();
    private final GeoQuizDatabase db;

    /**
     * Constructor initializes database reference and grid.
     *
     * @param db GeoQuiz database instance
     */
    @Inject
    public MapViewerViewModel(GeoQuizDatabase db) {

        this.db = db ;
        initializeGrid(); // fill with 100 blank items
    }

    /**
     * Exposes a LiveData list representing a 10x10 map grid.
     *
     * @return LiveData list of grid cell values
     */
    public LiveData<List<String>> getGridData() {
        return gridData;
    }

    /**
     * Initializes the map grid with 100 empty cells.
     */
    private void initializeGrid() {
        List<String> items = new ArrayList<>();
        for (int i = 0; i < 100; i++) items.add("");
        gridData.setValue(items);
    }

    public LiveData<List<LocationLogEntity>> getAllLogs() {
        return db.locationLogDao().getAllLogsLive();
    }

    /**
     * Fetches offline location, updates map grid, and logs the location.
     *
     * @param context Context for accessing location services
     */
    public void fetchOfflineLocation(Context context) {
        OfflineLocationHelper.LocationData data = OfflineLocationHelper.fetchOfflineLocation(context);

        if (!data.success) return;

        List<String> updatedGrid = new ArrayList<>(gridData.getValue());

        int userCell = Math.abs((int)((data.latitude * 10 + data.longitude * 10)) % 100);
        int towerCell = Math.abs((int)((data.latitude * 10 + data.longitude * 10 + data.timingAdvance) % 100));

        if (userCell < updatedGrid.size()) updatedGrid.set(userCell, "U");
        if (towerCell < updatedGrid.size()) updatedGrid.set(towerCell, "T");

        // save to DB
        db.locationLogDao().insertLog(new LocationLogEntity(
                System.currentTimeMillis(),
                data.latitude,
                data.longitude,
                data.accuracy,
                data.signalDbm,
                data.timingAdvance
        ));

        gridData.setValue(updatedGrid); // notify UI
    }
}
