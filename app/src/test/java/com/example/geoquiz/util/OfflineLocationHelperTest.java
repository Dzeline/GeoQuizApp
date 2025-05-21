package com.example.geoquiz.util;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import android.Manifest;
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

/**
 * Unit tests for OfflineLocationHelper
 */
public class OfflineLocationHelperTest {
    @Mock Context mockContext;
    @Mock LocationManager mockLocationManager;
    @Mock TelephonyManager mockTelephonyManager;
    @Mock Location mockGpsLocation;
    @Mock Location mockNetworkLocation;
    @Mock CellInfoLte mockCellInfoLte;
    @Mock CellSignalStrengthLte mockSignalStrength;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup context mocks
        when(mockContext.getSystemService(Context.LOCATION_SERVICE)).thenReturn(mockLocationManager);
        when(mockContext.getSystemService(Context.TELEPHONY_SERVICE)).thenReturn(mockTelephonyManager);
    }

    /**
     * Test successful location retrieval with GPS provider and LTE tower data
     */
    @Test
    public void testLocationWithGpsAndLteTower() {
        // Mock permission checks
        mockPermissionGranted();

        // Setup GPS location
        when(mockGpsLocation.getLatitude()).thenReturn(12.34);
        when(mockGpsLocation.getLongitude()).thenReturn(56.78);
        when(mockGpsLocation.getAccuracy()).thenReturn(5.5f);
        when(mockLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)).thenReturn(mockGpsLocation);
        when(mockLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)).thenReturn(null);

        // Setup LTE signal data
        setupLteSignalMocks();

        // Execute the method under test
        LocationData result = OfflineLocationHelper.fetchOfflineLocation(mockContext);

        // Verify results
        assertTrue("Location should be successfully retrieved", result.success);
        assertEquals("Latitude should match GPS location", 12.34, result.latitude, 0.01);
        assertEquals("Longitude should match GPS location", 56.78, result.longitude, 0.01);
        assertEquals("Accuracy should match GPS location", 5.5f, result.accuracy, 0.1f);
        assertEquals("Signal strength should be retrieved from LTE tower", -85, result.signalDbm);
        assertEquals("Timing advance should be retrieved from LTE tower", 7, result.timingAdvance);
        
        // Verify the correct methods were called
        verify(mockLocationManager).getLastKnownLocation(LocationManager.GPS_PROVIDER);
        verify(mockLocationManager).getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        verify(mockTelephonyManager).getAllCellInfo();
    }

    /**
     * Test successful location retrieval with network provider when GPS is unavailable
     */
    @Test
    public void testLocationWithNetworkProviderWhenGpsUnavailable() {
        // Mock permission checks
        mockPermissionGranted();

        // Setup network location (GPS returns null)
        when(mockLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)).thenReturn(null);
        when(mockNetworkLocation.getLatitude()).thenReturn(23.45);
        when(mockNetworkLocation.getLongitude()).thenReturn(67.89);
        when(mockNetworkLocation.getAccuracy()).thenReturn(10.0f);
        when(mockLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)).thenReturn(mockNetworkLocation);

        // Setup LTE signal data
        setupLteSignalMocks();

        // Execute the method under test
        LocationData result = OfflineLocationHelper.fetchOfflineLocation(mockContext);

        // Verify results
        assertTrue("Location should be successfully retrieved", result.success);
        assertEquals("Latitude should match network location", 23.45, result.latitude, 0.01);
        assertEquals("Longitude should match network location", 67.89, result.longitude, 0.01);
        assertEquals("Accuracy should match network location", 10.0f, result.accuracy, 0.1f);
        assertEquals("Signal strength should be retrieved from LTE tower", -85, result.signalDbm);
        assertEquals("Timing advance should be retrieved from LTE tower", 7, result.timingAdvance);
        
        // Verify the correct methods were called
        verify(mockLocationManager).getLastKnownLocation(LocationManager.GPS_PROVIDER);
        verify(mockLocationManager).getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        verify(mockTelephonyManager).getAllCellInfo();
    }

    /**
     * Test that location retrieval fails when permissions are denied
     */
    @Test
    public void testLocationFailsWithoutPermissions() {
        // Mock permission checks - DENIED
        when(ActivityCompat.checkSelfPermission(eq(mockContext), eq(Manifest.permission.ACCESS_FINE_LOCATION)))
                .thenReturn(PackageManager.PERMISSION_DENIED);
        when(ActivityCompat.checkSelfPermission(eq(mockContext), eq(Manifest.permission.ACCESS_COARSE_LOCATION)))
                .thenReturn(PackageManager.PERMISSION_DENIED);

        // Execute the method under test
        LocationData result = OfflineLocationHelper.fetchOfflineLocation(mockContext);

        // Verify results
        assertFalse("Location retrieval should fail without permissions", result.success);
        
        // Verify permission checks were called but not location services
        verify(mockContext, never()).getSystemService(Context.LOCATION_SERVICE);
    }

    /**
     * Test that location retrieval fails when no location is available from any provider
     */
    @Test
    public void testLocationFailsWhenNoLocationAvailable() {
        // Mock permission checks
        mockPermissionGranted();

        // Setup location providers to return null
        when(mockLocationManager.getLastKnownLocation(anyString())).thenReturn(null);
        
        // Setup LTE signal data (even though location will fail)
        setupLteSignalMocks();

        // Execute the method under test
        LocationData result = OfflineLocationHelper.fetchOfflineLocation(mockContext);

        // Verify results - this will depend on the implementation of the fallback mechanisms
        // If the implementation has working fallbacks to SMS or cell towers, this might succeed
        // For this test, we're assuming the fallbacks aren't fully implemented yet
        assertFalse("Location retrieval should fail when no location is available", result.success);
        
        // Verify the correct methods were called
        verify(mockLocationManager).getLastKnownLocation(LocationManager.GPS_PROVIDER);
        verify(mockLocationManager).getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
    }

    /**
     * Test that specific permission checks are performed
     */
    @Test
    public void testCorrectPermissionsAreChecked() {
        // Set up permissions to be granted
        mockPermissionGranted();
        
        // Set up a valid location response
        when(mockGpsLocation.getLatitude()).thenReturn(12.34);
        when(mockGpsLocation.getLongitude()).thenReturn(56.78);
        when(mockGpsLocation.getAccuracy()).thenReturn(5.5f);
        when(mockLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)).thenReturn(mockGpsLocation);
        
        // Execute the method
        OfflineLocationHelper.fetchOfflineLocation(mockContext);
        
        // Verify that both permission types were checked
        verify(ActivityCompat).checkSelfPermission(mockContext, Manifest.permission.ACCESS_FINE_LOCATION);
        verify(ActivityCompat).checkSelfPermission(mockContext, Manifest.permission.ACCESS_COARSE_LOCATION);
    }

    /**
     * Test that the helper handles null cell info gracefully
     */
    @Test
    public void testHandlesNullCellInfo() {
        // Mock permission checks
        mockPermissionGranted();

        // Setup GPS location
        when(mockGpsLocation.getLatitude()).thenReturn(12.34);
        when(mockGpsLocation.getLongitude()).thenReturn(56.78);
        when(mockGpsLocation.getAccuracy()).thenReturn(5.5f);
        when(mockLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)).thenReturn(mockGpsLocation);

        // Setup null cell info
        when(mockTelephonyManager.getAllCellInfo()).thenReturn(null);

        // Execute the method under test
        LocationData result = OfflineLocationHelper.fetchOfflineLocation(mockContext);

        // Verify results
        assertTrue("Location should be successfully retrieved even with null cell info", result.success);
        assertEquals("Latitude should match GPS location", 12.34, result.latitude, 0.01);
        assertEquals("Longitude should match GPS location", 56.78, result.longitude, 0.01);
        assertEquals("Accuracy should match GPS location", 5.5f, result.accuracy, 0.1f);
        assertEquals("Signal strength should be default when cell info is null", -999, result.signalDbm);
        assertEquals("Timing advance should be default when cell info is null", -1, result.timingAdvance);
    }

    // Helper methods for setting up mocks

    private void mockPermissionGranted() {
        when(ActivityCompat.checkSelfPermission(eq(mockContext), eq(Manifest.permission.ACCESS_FINE_LOCATION)))
                .thenReturn(PackageManager.PERMISSION_GRANTED);
        when(ActivityCompat.checkSelfPermission(eq(mockContext), eq(Manifest.permission.ACCESS_COARSE_LOCATION)))
                .thenReturn(PackageManager.PERMISSION_GRANTED);
    }

    private void setupLteSignalMocks() {
        when(mockSignalStrength.getDbm()).thenReturn(-85);
        when(mockSignalStrength.getTimingAdvance()).thenReturn(7);
        when(mockCellInfoLte.getCellSignalStrength()).thenReturn(mockSignalStrength);
        when(mockTelephonyManager.getAllCellInfo()).thenReturn(Collections.singletonList(mockCellInfoLte));
    }
}
