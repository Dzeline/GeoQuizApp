package com.example.geoquiz.databse;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class GeoQuizDatabaseHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "geoquiz.db";
    public static final int DB_VERSION = 1;

    // Table names
    public static final String TABLE_MESSAGES = "Messages";

    // Message table columns
    public static final String COL_ID = "_id";
    public static final String COL_SENDER = "sender";
    public static final String COL_MESSAGE = "message";
    public static final String COL_TIMESTAMP = "timestamp";

    public GeoQuizDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_MESSAGES_TABLE = "CREATE TABLE " + TABLE_MESSAGES + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_SENDER + " TEXT, " +
                COL_MESSAGE + " TEXT, " +
                COL_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP);";
        db.execSQL(CREATE_MESSAGES_TABLE);

        Log.d("GeoQuizDB", "Database tables created.");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
        onCreate(db);
    }
}
