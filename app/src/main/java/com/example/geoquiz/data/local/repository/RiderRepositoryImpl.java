package com.example.geoquiz.data.local.repository;

import androidx.lifecycle.LiveData;
//import com.example.geoquiz.data.local.database.GeoQuizDatabase;
import com.example.geoquiz.data.local.database.RiderDao;
import com.example.geoquiz.data.local.database.RiderEntity;
import com.example.geoquiz.domain.repository.RiderRepository;

import java.util.List;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

//import static com.example.geoquiz.data.local.database.GeoQuizDatabase.databaseWriteExecutor;
@Singleton
public class RiderRepositoryImpl  implements RiderRepository{
    private final RiderDao riderDao;
    private final ExecutorService ioExecutor;

    @Inject
    public RiderRepositoryImpl(RiderDao riderDao,@Named("ioExecutor") ExecutorService ioExecutor) {
        this.riderDao = riderDao;
        this.ioExecutor= ioExecutor;
    }

    @Override
    public LiveData<List<RiderEntity>> getAllRiders() {
        return riderDao.getAllRiders();
    }

    @Override
    public void insertRider(RiderEntity rider) {
        ioExecutor.execute(() -> {
            riderDao.insertRider(rider);
        });
    }
}
