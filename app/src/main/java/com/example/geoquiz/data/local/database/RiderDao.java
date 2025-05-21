package com.example.geoquiz.data.local.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface RiderDao {
    @Query("SELECT * FROM Riders")
    LiveData<List<RiderEntity>> getAllRiders();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertRider(RiderEntity rider);
}
