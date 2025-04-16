package com.example.geoquiz;

import android.app.Application;

import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class GeoQuizApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Perform any app-wide initialization here if needed.
    }
}
