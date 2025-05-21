package com.example.geoquiz.di;

import android.app.Application;
import android.content.Context;

import androidx.room.Room;

import com.example.geoquiz.data.local.database.GeoQuizDatabase;
import com.example.geoquiz.data.local.database.LocationLogDao;
import com.example.geoquiz.data.local.database.MessageDao;
import com.example.geoquiz.data.local.database.RiderDao;
import com.example.geoquiz.data.local.repository.LocationLogRepositoryImpl;
import com.example.geoquiz.data.local.repository.MessageRepositoryImpl;
import com.example.geoquiz.data.local.repository.RiderRepositoryImpl;
import com.example.geoquiz.domain.repository.LocationLogRepository;
import com.example.geoquiz.domain.repository.MessageRepository;
import com.example.geoquiz.domain.repository.RiderRepository;
import com.example.geoquiz.location.CustomLocationManager;
import com.example.geoquiz.location.CustomLocationManagerImpl;

import java.util.concurrent.ExecutorService; // Import
import java.util.concurrent.Executors;    // Import

import javax.inject.Named; // For named bindings if needed
import javax.inject.Singleton;
    //import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public  class AppModule {

    private static final String DB_NAME = "GeoQuizDatabase.db";
    @Provides
    @Singleton
    public GeoQuizDatabase provideDatabase(@ApplicationContext Context context) {
        return Room.databaseBuilder(context, GeoQuizDatabase.class, DB_NAME)
                .addMigrations(GeoQuizDatabase.MIGRATION_1_2)
                .fallbackToDestructiveMigration()
                .build();
    }

    @Provides
    @Singleton
    public LocationLogDao provideLocationLogDao(GeoQuizDatabase db) {
        return db.locationLogDao();
    }

    @Provides
    @Singleton
    public MessageDao provideMessageDao(GeoQuizDatabase db) {
        return db.messageDao();
    }

    @Provides
    @Singleton
    public RiderDao provideRiderDao(GeoQuizDatabase db) {
        return db.riderDao();
    }
    @Provides
    @Singleton
    @Named("ioExecutor") // Ensure this matches the @Named annotation in consumers
    public ExecutorService provideIoExecutor() {
        return Executors.newFixedThreadPool(4); // Or GeoQuizDatabase.databaseWriteExecutor if suitable
    }

    // Provide Repositories (These could also be bound using @Binds in an abstract module if preferred)
    @Provides
    @Singleton
    public MessageRepository provideMessageRepo(MessageDao messageDao, @Named("ioExecutor")ExecutorService executor) { // Inject DAO
        return new MessageRepositoryImpl(messageDao, executor); // Assuming Impl takes DAO
    }

    @Provides
    @Singleton
    public LocationLogRepository provideLocationLogRepo(LocationLogDao locationLogDao,@Named("ioExecutor") ExecutorService executor) { // Inject DAO
        return new LocationLogRepositoryImpl(locationLogDao, executor ); // Assuming Impl takes DAO
    }

    @Provides
    @Singleton
    public RiderRepository provideRiderRepo(RiderDao riderDao,@Named("ioExecutor") ExecutorService executor){ // Inject DAO
        return new RiderRepositoryImpl(riderDao, executor); // Assuming Impl takes DAO
    }

    // Provide CustomLocationManager
    // If CustomLocationManagerImpl has an @Inject constructor and its dependencies are met,
    // Hilt can create it. Otherwise, provide its dependencies here.
    // This assumes CustomLocationManagerImpl IS the implementation.
    @Provides
    @Singleton
    public CustomLocationManager provideCustomLocationManager(Application application) {
        // Assuming CustomLocationManagerImpl takes Application context
        return new CustomLocationManagerImpl(application);
    }


    // Provide a general-purpose ExecutorService for database operations if needed
    // Or a specific one for OfflineLocationHelper if its needs are distinct.
    // Reusing the one from GeoQuizDatabase if it was public static:
    // @Provides
    // @Singleton
    // public ExecutorService provideDatabaseWriteExecutor() {
    //     return GeoQuizDatabase.databaseWriteExecutor; // If it's accessible and appropriate
    // }

    // Or create a new one:

}