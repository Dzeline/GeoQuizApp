package com.example.geoquiz.util;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.telephony.CellInfoLte;
import android.telephony.CellSignalStrengthLte;
import android.telephony.TelephonyManager;

import androidx.core.app.ActivityCompat;

import com.example.geoquiz.util.OfflineLocationHelper.LocationData;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
public class OfflineLocationHelperTest {
    @Mock Context mockContext;
    @Mock LocationManager mockLocationManager;
    @Mock TelephonyManager mockTelephonyManager;
    @Mock Location mockLocation;
    @Mock CellInfoLte mockCellInfoLte;
    @Mock CellSignalStrengthLte mockSignalStrength;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        when(mockContext.getSystemService(Context.LOCATION_SERVICE)).thenReturn(mockLocationManager);
        when(mockContext.getSystemService(Context.TELEPHONY_SERVICE)).thenReturn(mockTelephonyManager);
    }

    @Test
    public void testLocationWithPermissionsAndLteTower() {
        when(ActivityCompat.checkSelfPermission(eq(mockContext), anyString())).thenReturn(PackageManager.PERMISSION_GRANTED);

        // Location setup
        when(mockLocation.getLatitude()).thenReturn(12.34);
        when(mockLocation.getLongitude()).thenReturn(56.78);
        when(mockLocation.getAccuracy()).thenReturn(5.5f);
        when(mockLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)).thenReturn(mockLocation);

        // Signal setup
        when(mockSignalStrength.getDbm()).thenReturn(-85);
        when(mockSignalStrength.getTimingAdvance()).thenReturn(7);
        when(mockCellInfoLte.getCellSignalStrength()).thenReturn(mockSignalStrength);
        when(mockTelephonyManager.getAllCellInfo()).thenReturn(Collections.singletonList(mockCellInfoLte));

        LocationData result = OfflineLocationHelper.fetchOfflineLocation(mockContext);

        assertTrue(result.success);
        assertEquals(12.34, result.latitude, 0.01);
        assertEquals(56.78, result.longitude, 0.01);
        assertEquals(5.5f, result.accuracy, 0.1f);
        assertEquals(-85, result.signalDbm);
        assertEquals(7, result.timingAdvance);
    }

    @Test
    public void testLocationFailsWithoutPermissions() {
        when(ActivityCompat.checkSelfPermission(eq(mockContext), anyString())).thenReturn(PackageManager.PERMISSION_DENIED);

        LocationData result = OfflineLocationHelper.fetchOfflineLocation(mockContext);

        assertFalse(result.success);
    }

    @Test
    public void testLocationFailsWhenNoLocationAvailable() {
        when(ActivityCompat.checkSelfPermission(eq(mockContext), anyString())).thenReturn(PackageManager.PERMISSION_GRANTED);
        when(mockLocationManager.getLastKnownLocation(anyString())).thenReturn(null);

        LocationData result = OfflineLocationHelper.fetchOfflineLocation(mockContext);

        assertFalse(result.success);
    }

}
