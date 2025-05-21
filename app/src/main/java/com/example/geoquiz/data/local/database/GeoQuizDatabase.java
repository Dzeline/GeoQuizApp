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

@Database(
        entities = {
                MessageEntity.class,
                LocationLogEntity.class,
                RiderEntity.class
        },
        version = 2,
        exportSchema = false // Set to true for production and provide schema location
)
public abstract class GeoQuizDatabase extends RoomDatabase {
    // No need for INSTANCE or getInstance with Hilt, Hilt manages the singleton.
    // public static volatile GeoQuizDatabase INSTANCE;

    public abstract MessageDao messageDao();
    public abstract LocationLogDao locationLogDao();
    public abstract RiderDao riderDao();

    // This executor is usually for Room's internal operations or if you explicitly use it.
    // Hilt can provide a separate IO executor for other tasks.
    // Keep if your DAOs or parts of the app explicitly use it.
    // If you use the @Named("ioExecutor") from AppModule, this might be redundant
    // unless Room itself is configured to use it by default (which it isn't directly).
    // For simplicity, let's assume DAOs will use Room's default mechanisms or the one from AppModule when needed.
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(4);


    // Make this accessible to your AppModule or define it there.
    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Your migration SQL - This one seems to be creating tables if they don't exist,
            // which might be okay for development, but a true migration updates existing schema.
            // For version 1 to 2, if tables already exist from version 1, these
            // "CREATE TABLE IF NOT EXISTS" might not do what's needed for a schema change.
            // Assuming these are the correct definitions for version 2.

            database.execSQL("CREATE TABLE IF NOT EXISTS `Location_logs` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`timestamp` INTEGER NOT NULL, " +
                    "`latitude` REAL NOT NULL, " +
                    "`longitude` REAL NOT NULL, " +
                    "`accuracy` REAL NOT NULL, " +
                    "`signal_dbm` INTEGER NOT NULL, " + // Ensure data type matches LocationLogEntity
                    "`timing_advance` INTEGER NOT NULL)"); // Ensure data type matches LocationLogEntity

            database.execSQL("CREATE INDEX IF NOT EXISTS `idx_location_logs_coords` ON `Location_logs`(`latitude`, `longitude`)");


            // Ensure messages table exists
            // If MessageEntity structure changed from v1, this needs proper ALTER TABLE
            database.execSQL("CREATE TABLE IF NOT EXISTS `Messages` (" + // Table name from MessageEntity
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`sender` TEXT NOT NULL, " +       // Column name from MessageEntity
                    "`receiver` TEXT NOT NULL, " +     // Column name from MessageEntity
                    "`message` TEXT NOT NULL, " +      // Column name from MessageEntity
                    "`timestamp` INTEGER NOT NULL)");

            // Ensure riders table exists
            // If RiderEntity structure changed from v1, this needs proper ALTER TABLE
            database.execSQL("CREATE TABLE IF NOT EXISTS `Riders` (" + // Table name from RiderEntity
                    "`phone_number` TEXT NOT NULL PRIMARY KEY, " + // Column name from RiderEntity
                    "`name` TEXT, " +                         // Column name from RiderEntity (make sure TEXT is appropriate if nullable)
                    "`is_available` INTEGER NOT NULL)");      // Column name from RiderEntity (INTEGER for boolean)
        }
    };
}
