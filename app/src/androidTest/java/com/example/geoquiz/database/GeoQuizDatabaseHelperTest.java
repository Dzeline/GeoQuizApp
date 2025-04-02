package com.example.geoquiz.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.core.app.ApplicationProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class GeoQuizDatabaseHelperTest {

    private GeoQuizDatabase dbHelper;
    private SQLiteDatabase db;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        dbHelper = new GeoQuizDatabase(context);
        db = dbHelper.getWritableDatabase();
        db.delete(GeoQuizDatabase.TABLE_LOCATION_LOGS, null, null); // Clear for clean slate
    }

    @After
    public void tearDown() {
        db.close();
    }

    @Test
    public void testInsertLog_ValidValues() {
        long id = dbHelper.insertLog(12.34, 56.78, 5.5f, -85, 7);
        assertTrue("Insert should return valid row ID", id != -1);

        //Fix :Use _id not id!
        Cursor cursor = db.query(
                GeoQuizDatabase.TABLE_LOCATION_LOGS,
                null,
                GeoQuizDatabase.COL_ID + " = ?",
                new String[]{String.valueOf(id)},
                null, null, null);
        assertTrue(cursor.moveToFirst());

        double lat = cursor.getDouble(cursor.getColumnIndexOrThrow(GeoQuizDatabase.COL_LATITUDE));
        double lon = cursor.getDouble(cursor.getColumnIndexOrThrow(GeoQuizDatabase.COL_LONGITUDE));
        float accuracy = cursor.getFloat(cursor.getColumnIndexOrThrow(GeoQuizDatabase.COL_ACCURACY));
        int dbm = cursor.getInt(cursor.getColumnIndexOrThrow(GeoQuizDatabase.COL_SIGNAL_DBM));
        int ta = cursor.getInt(cursor.getColumnIndexOrThrow(GeoQuizDatabase.COL_TIMING_ADVANCE));

        assertEquals(12.34, lat, 0.01);
        assertEquals(56.78, lon, 0.01);
        assertEquals(5.5f, accuracy, 0.1f);
        assertEquals(-85, dbm);
        assertEquals(7, ta);

        cursor.close();
    }

    @Test
    public void testInsertLog_InvalidValues() {
        long id = dbHelper.insertLog(0.0, 0.0, 0.0f, -999, -1);
        assertTrue("Insert should still succeed for edge input ", id != -1);
    }

    @Test
    public void testInsertMultipleLogs() {
        int count = 10;
        long[] ids = new long[count];

        for (int i = 0; i < count; i++) {
            ids[i] = dbHelper.insertLog(
                    10.0 + i, 20.0 + i,
                    3.0f + i,
                    -90 + i,
                    i
            );
           assertTrue("Insert #" + i + " should succeed", ids[i] != -1);
        }

        // Check row count
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM "+ GeoQuizDatabase.TABLE_LOCATION_LOGS, null);
        assertTrue(cursor.moveToFirst());

        int rowCount = cursor.getInt(0);
        assertEquals("Should contain 10 rows", count, rowCount);

        cursor.close();
    }

}
