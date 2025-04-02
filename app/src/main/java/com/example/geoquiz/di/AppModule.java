package com.example.geoquiz.di;

import android.content.Context;

import androidx.room.Room;

import com.example.geoquiz.data.local.database.GeoQuizDatabase;
import com.example.geoquiz.data.local.repository.LocationLogRepositoryImpl;
import com.example.geoquiz.data.local.repository.MessageRepositoryImpl;
import com.example.geoquiz.domain.repository.LocationLogRepository;
import com.example.geoquiz.domain.repository.MessageRepository;
import com.example.geoquiz.location.CustomLocationManager;
import com.example.geoquiz.location.CustomLocationManagerImpl;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class AppModule {
    @Provides
    @Singleton
    public GeoQuizDatabase provideDatabase(@ApplicationContext Context context) {
        return Room.databaseBuilder(context, GeoQuizDatabase.class, "geoquiz.db")
                .fallbackToDestructiveMigration()
                .build();
    }

    @Provides
    @Singleton
    public MessageRepository provideMessageRepo(GeoQuizDatabase db) {
        return new MessageRepositoryImpl(db);
    }

    @Provides
    @Singleton
    public LocationLogRepository provideLocationLogRepo(GeoQuizDatabase db) {
        return new LocationLogRepositoryImpl(db);
    }

    @Provides
    @Singleton
    public CustomLocationManager provideCustomLocationManager(CustomLocationManagerImpl impl) {
        return impl;
    }

}
