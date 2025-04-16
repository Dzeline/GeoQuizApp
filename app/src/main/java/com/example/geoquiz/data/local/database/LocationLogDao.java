package com.example.geoquiz.data.local.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

/**
 *Data Access Object (DAO) for interacting with the LocationLogs table.
 *  Stores location data collected from SMS or GPS.
 */
@Dao
public abstract class LocationLogDao {
    /**
     * Inserts a new location log into the database.
     * @param log the location log to be inserted
     * @return the newly inserted row ID
     */
    @Insert
    public abstract long insertLog(LocationLogEntity log);

    /**
     * Fetches all stored location logs, sorted from newest to oldest.
     *
     * @return A list of LocationLogEntity objects .
     */
    @Query("SELECT * FROM LocationLogs ORDER BY timestamp DESC")
    public abstract LiveData<List<LocationLogEntity>> getAllLogs();

    @Query("SELECT * FROM LocationLogs ORDER BY timestamp DESC")
    public abstract LiveData<List<LocationLogEntity>> getAllLogsLive();

}
