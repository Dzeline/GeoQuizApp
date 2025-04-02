package com.example.geoquiz.domain.repository;

import com.example.geoquiz.data.local.database.LocationLogEntity;

import java.util.List;
public abstract class LocationLogRepository {


    public abstract List<LocationLogEntity> getAllLogs();
}
