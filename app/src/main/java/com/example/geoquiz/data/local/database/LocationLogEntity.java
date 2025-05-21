package com.example.geoquiz.data.local.database;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entity representing a single offline location log.
 * These are saved when location info is received via SMS or GPS.
 */
@Entity(tableName = "Location_logs")
public class LocationLogEntity {
    /**
     * Primary key for the location entry (auto-generated).
     */
    @PrimaryKey(autoGenerate = true)
    public int id;

    /**
     * Timestamp of the log in milliseconds.
     */
    @ColumnInfo(name = "timestamp")
    public long timestamp;

    /**
     * Latitude of the device.
     */
    @ColumnInfo(name = "latitude")
    public double latitude;

    /**
     * Longitude of the device.
     */
    @ColumnInfo(name = "longitude")
    public double longitude;

    /**
     * GPS accuracy in meters.
     */
    @ColumnInfo(name = "accuracy")
    public float accuracy;

    /**
     * Signal strength in dBm.
     */
    @ColumnInfo(name = "signal_dbm")
    public int signalDbm;

    /**
     * Timing advance value from the cell tower.
     */
    @ColumnInfo(name = "timing_advance")
    public int timingAdvance;

    /**
     * Constructor to create a new location log entry.
     *
     * @param timestamp      Time in milliseconds
     * @param latitude       GPS latitude
     * @param longitude      GPS longitude
     * @param accuracy       Accuracy in meters
     * @param signalDbm      Signal strength (dBm)
     * @param timingAdvance  Cell timing advance
     */
    public LocationLogEntity(long timestamp, double latitude, double longitude, float accuracy, int signalDbm, int timingAdvance) {

        this.timestamp = timestamp;
        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracy = accuracy;
        this.signalDbm = signalDbm;
        this.timingAdvance = timingAdvance;
    }
}
