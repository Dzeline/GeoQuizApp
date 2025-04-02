package com.example.geoquiz.presentation.feature_location_history;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;


import com.example.geoquiz.data.local.database.LocationLogEntity;

import com.example.geoquiz.domain.repository.LocationLogRepository;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * ViewModel for LocationHistoryActivity.
 * Responsible for loading and exposing location logs from the repository.
 */
@HiltViewModel
public class LocationHistoryViewModel extends ViewModel {

    private final MutableLiveData<List<LocationLogEntity>> logs = new MutableLiveData<>();
    private final LocationLogRepository logRepo;

    /**
     * Constructs the ViewModel with a provided LocationLogRepository.
     *
     * @param logRepo Repository to access location logs
     */
    @Inject
    public LocationHistoryViewModel(LocationLogRepository logRepo) {

        this.logRepo = logRepo;
        loadLogs();
    }

    /**
     * Exposes a LiveData stream of all location logs.
     *
     * @return LiveData list of LocationLogEntity
     */
    public LiveData<List<LocationLogEntity>> getLogs() {
        return logs;
    }

    /**
     * Loads all logs from the repository into the LiveData.
     */
    private void loadLogs() {
        logs.setValue(logRepo.getAllLogs());
    }

}
