package com.example.geoquiz.data.local.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "Riders")
public class RiderEntity {
    @PrimaryKey @NonNull
    @ColumnInfo(name = "phone_number")
    public String phoneNumber;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "is_available")
    public boolean isAvailable;

    public RiderEntity(@NonNull String phoneNumber, String name, boolean isAvailable) {
        this.phoneNumber = phoneNumber;
        this.name        = name;
        this.isAvailable = isAvailable;
    }
}
