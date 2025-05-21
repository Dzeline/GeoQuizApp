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
public interface LocationLogDao {
    /**
     * Inserts a new location log into the database.
     * @param log the location log to be inserted
     * @return the newly inserted row ID
     */
    @Insert
    void insertLog(LocationLogEntity log);

    /**
     * Fetches all stored location logs, sorted from newest to oldest.
     *
     * @return A list of LocationLogEntity objects .
     */
    @Query("SELECT * FROM Location_logs ORDER BY timestamp DESC")
    LiveData<List<LocationLogEntity>> getAllLogs();

    @Query("SELECT * FROM Location_logs ORDER BY timestamp DESC")
    public abstract LiveData<List<LocationLogEntity>> getAllLogsLive();

    @Query("SELECT * FROM Location_logs  ORDER BY timestamp DESC")
    LiveData<List<LocationLogEntity>> getAllLogsSortedByTimestamp();

    @Query("SELECT * FROM Location_logs WHERE id = :id")
    LiveData<LocationLogEntity> getLogById(int id);

    @Query("SELECT * FROM Location_logs WHERE timestamp >= :minTimestamp ORDER BY timestamp DESC")
    LiveData<List<LocationLogEntity>> getLogsSince(long minTimestamp);

    @Query("SELECT * FROM Location_logs WHERE timestamp > :sinceTimestamp ORDER BY timestamp DESC")
    List<LocationLogEntity> getRecentLogsSync(long sinceTimestamp);

    @Query("DELETE FROM Location_logs WHERE timestamp < :before")
    void deleteOldLogs(long before);

    @Query("SELECT * FROM Location_logs WHERE " +
            "latitude BETWEEN :minLat AND :maxLat AND " +
            "longitude BETWEEN :minLon AND :maxLon " +
            "ORDER BY timestamp DESC")
    LiveData<List<LocationLogEntity>> getLogsInArea(double minLat, double maxLat,
                                                    double minLon, double maxLon);
}
