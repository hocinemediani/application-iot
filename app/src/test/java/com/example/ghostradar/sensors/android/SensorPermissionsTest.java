package com.example.ghostradar.sensors.android;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class SensorPermissionsTest {

    @Test
    public void requiredPermissionsIncludeCamera() {
        assertArrayEquals(new String[]{
                "android.permission.ACCESS_FINE_LOCATION",
                "android.permission.ACCESS_COARSE_LOCATION",
                "android.permission.RECORD_AUDIO",
                "android.permission.CAMERA"
        }, SensorPermissions.requiredPermissions());
    }
}
