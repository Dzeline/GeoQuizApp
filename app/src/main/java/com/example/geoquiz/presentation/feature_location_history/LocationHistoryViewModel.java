package com.example.geoquiz.presentation.feature_location_history;


import androidx.lifecycle.LiveData;
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

    private final LiveData<List<LocationLogEntity>> logs;


    /**
     * Constructs the ViewModel with a provided LocationLogRepository.
     *
     * @param logRepo Repository to access location logs
     */
    @Inject
    public LocationHistoryViewModel(LocationLogRepository logRepo) {

        this.logs= logRepo.getAllLogs();

        }

    /**
     * Exposes a LiveData stream of all location logs.
     *
     * @return LiveData list of LocationLogEntity
     */
    public LiveData<List<LocationLogEntity>> getLogs() {
        return logs;
    }




}
