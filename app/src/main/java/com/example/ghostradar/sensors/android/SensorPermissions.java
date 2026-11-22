package com.example.ghostradar.sensors.android;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * Vérification et demande des permissions runtime nécessaires aux capteurs.
 */
public final class SensorPermissions {

    public static final int REQUEST_CODE = 1_001;

    private SensorPermissions() {
    }

    public static boolean hasLocationPermission(Context context) {
        return ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean hasAudioPermission(Context context) {
        return ContextCompat.checkSelfPermission(context,
                Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean hasRequiredPermissions(Context context) {
        return hasLocationPermission(context) && hasAudioPermission(context);
    }

    public static String[] requiredPermissions() {
        return new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.RECORD_AUDIO
        };
    }

    public static void requestIfNeeded(Activity activity) {
        if (activity == null) {
            throw new IllegalArgumentException("activity must not be null");
        }
        if (!hasRequiredPermissions(activity)) {
            ActivityCompat.requestPermissions(
                    activity, requiredPermissions(), REQUEST_CODE);
        }
    }
}
