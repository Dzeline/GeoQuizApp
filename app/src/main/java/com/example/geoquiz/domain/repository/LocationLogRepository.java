package com.example.geoquiz.domain.repository;

import androidx.lifecycle.LiveData;

import com.example.geoquiz.data.local.database.LocationLogEntity;

import java.util.List;
public interface LocationLogRepository {


    LiveData<List<LocationLogEntity>> getAllLogs();
    LiveData<List<LocationLogEntity>> getAllLogsLive();

    void insertLog(LocationLogEntity log);
}
