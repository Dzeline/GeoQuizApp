package com.example.geoquiz.di;
import com.example.geoquiz.data.local.database.LocationLogDao;
import com.example.geoquiz.data.local.database.MessageDao;
import dagger.hilt.EntryPoint;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent; // Or Application
@EntryPoint
@InstallIn(SingletonComponent.class) // Use SingletonComponent for application-wide dependencies
public interface SmsReceiverEntryPoint {
    LocationLogDao locationLogDao();
    MessageDao messageDao();
    // You could also inject repositories here if they handle their own threading
}
