package com.example.geoquiz.domain.repository;

import androidx.lifecycle.LiveData;
import com.example.geoquiz.data.local.database.RiderEntity;
import java.util.List;

public interface RiderRepository {
    LiveData<List<RiderEntity>> getAllRiders();
    void insertRider(RiderEntity rider);
}
