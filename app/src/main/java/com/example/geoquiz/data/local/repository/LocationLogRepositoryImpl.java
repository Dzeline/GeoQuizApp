package com.example.geoquiz.data.local.repository;

import android.app.Application;

import com.example.geoquiz.data.local.database.GeoQuizDatabase;
import com.example.geoquiz.data.local.database.LocationLogEntity;
import com.example.geoquiz.domain.repository.LocationLogRepository;

import java.util.List;

import javax.inject.Inject;

public class LocationLogRepositoryImpl extends LocationLogRepository {
    private final GeoQuizDatabase db;
    @Inject
    public LocationLogRepositoryImpl(GeoQuizDatabase db) {
       this.db =db ;
    }

    @Override
    public List<LocationLogEntity> getAllLogs() {
        return db.locationLogDao().getAllLogs();
    }
}
