package com.example.geoquiz.data.local.repository;

import androidx.lifecycle.LiveData;
import com.example.geoquiz.data.local.database.LocationLogDao;
//import com.example.geoquiz.data.local.database.GeoQuizDatabase;
import com.example.geoquiz.data.local.database.LocationLogEntity;
import com.example.geoquiz.domain.repository.LocationLogRepository;

import java.util.List;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
public class LocationLogRepositoryImpl implements LocationLogRepository {
    private final LocationLogDao locationLogDao; // Changed from GeoQuizDatabase
    private final ExecutorService ioExecutor;   // Added for background tasks
    @Inject
    public LocationLogRepositoryImpl(LocationLogDao locationLogDao,@Named("ioExecutor")ExecutorService ioExecutor ){
        this.locationLogDao = locationLogDao;
        this.ioExecutor = ioExecutor;
    }

    @Override
    public LiveData<List<LocationLogEntity>> getAllLogs() {
        return locationLogDao.getAllLogsSortedByTimestamp();
    }

    /**
     * @return LiveData list of location logs.
     */
    @Override
    public LiveData<List<LocationLogEntity>> getAllLogsLive() {
        return locationLogDao.getAllLogsSortedByTimestamp();
    }

    @Override
    public void insertLog(LocationLogEntity log) {
        ioExecutor.execute(() -> locationLogDao.insertLog(log));
    }

}
