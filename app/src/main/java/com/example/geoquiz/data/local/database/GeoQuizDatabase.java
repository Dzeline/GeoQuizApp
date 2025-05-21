package com.example.geoquiz.data.local.database;


import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.example.geoquiz.data.local.database.LocationLogDao;
import com.example.geoquiz.data.local.database.MessageDao;
import com.example.geoquiz.data.local.database.RiderDao;

@Database(
        entities = {
                MessageEntity.class,
                LocationLogEntity.class ,
                RiderEntity.class},
        version = 2 ,
        exportSchema =false )
public abstract class GeoQuizDatabase extends RoomDatabase {
    private static volatile GeoQuizDatabase INSTANCE;

    public abstract MessageDao messageDao();
    public abstract LocationLogDao locationLogDao();
    public abstract RiderDao riderDao();

    public static GeoQuizDatabase getInstance(Application application) {
        if (INSTANCE == null) {
            synchronized (GeoQuizDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(application.getApplicationContext(),
                                    GeoQuizDatabase.class, "GeoQuiz_db")
                            .addMigrations(MIGRATION_1_2)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(4);


    // Optional: Replace with actual migration if you're keeping user data
    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            // Ensure location_logs table exists
            db.execSQL("CREATE TABLE IF NOT EXISTS `Location_logs` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`timestamp` INTEGER NOT NULL, " +
                    "`latitude` REAL NOT NULL, " +
                    "`longitude` REAL NOT NULL, " +
                    "`accuracy` REAL NOT NULL, " +
                    "`signalDbm` REAL NOT NULL, " +
                    "`timingAdvance` REAL NOT NULL)");

            db.execSQL("CREATE INDEX IF NOT EXISTS `idx_location_logs_coords` ON `location_logs`(`latitude`, `longitude`)");

            // Ensure messages table exists
            db.execSQL("CREATE TABLE IF NOT EXISTS `messages` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`senderPhoneNumber` TEXT NOT NULL, " +
                    "`messageText` TEXT NOT NULL, " +
                    "`timestamp` INTEGER NOT NULL)");

            // Ensure riders table exists
            db.execSQL("CREATE TABLE IF NOT EXISTS `riders` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`name` TEXT NOT NULL, " +
                    "`phoneNumber` TEXT NOT NULL)");
        }

    };
}
