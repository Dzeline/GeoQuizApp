package com.example.geoquiz.data.local.database;


import android.app.Application;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {MessageEntity.class, LocationLogEntity.class}, version = 3 ,exportSchema =false )
public abstract class GeoQuizDatabase extends RoomDatabase {
    public abstract MessageDao messageDao();
    public abstract LocationLogDao locationLogDao();

    private static volatile GeoQuizDatabase INSTANCE;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(4);
    /**
     * Migration from v2 → v3:
     * Recreates the Messages table with NOT NULL constraints on sender, receiver, message.
     */
    public static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            // 1. Create a new table with the correct NOT NULL schema
            db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `Messages_new` (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`sender` TEXT NOT NULL, " +
                            "`receiver` TEXT NOT NULL, " +
                            "`message` TEXT NOT NULL, " +
                            "`timestamp` INTEGER NOT NULL" +
                            ")"
            );
            // 2. Copy data from old table, substituting defaults for any existing NULLs
            db.execSQL(
                    "INSERT INTO `Messages_new` (id, sender, receiver, message, timestamp) " +
                            "SELECT " +
                            "id, " +
                            "COALESCE(sender, ''), " +
                            "COALESCE(receiver, ''), " +
                            "COALESCE(message, ''), " +
                            "timestamp " +
                            "FROM `Messages`"
            );
            // 3. Drop the old table…
            db.execSQL("DROP TABLE `Messages`");
            // 4. Rename new table to the original name
            db.execSQL("ALTER TABLE `Messages_new` RENAME TO `Messages`");
        }
    };

    public static GeoQuizDatabase getInstance(Application context) {
        if (INSTANCE == null) {
            synchronized (GeoQuizDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    GeoQuizDatabase.class, "GeoQuiz.db")
                            .addMigrations(MIGRATION_2_3)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }



}
