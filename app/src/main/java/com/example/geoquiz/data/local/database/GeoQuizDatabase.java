package com.example.geoquiz.data.local.database;


import android.app.Application;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {MessageEntity.class, LocationLogEntity.class}, version = 1 ,exportSchema = false)
public abstract class GeoQuizDatabase extends RoomDatabase {

    private static volatile GeoQuizDatabase INSTANCE;

    public abstract MessageDao messageDao();
    public abstract LocationLogDao locationLogDao();

    public static GeoQuizDatabase getInstance(Application context) {
        if (INSTANCE == null) {
            synchronized (GeoQuizDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    GeoQuizDatabase.class, "GeoQuiz.db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(4);


}
