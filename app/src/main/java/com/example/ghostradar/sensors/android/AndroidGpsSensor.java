package com.example.ghostradar.sensors.android;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Looper;

import androidx.core.content.ContextCompat;

import com.example.ghostradar.domain.config.GameConfig;
import com.example.ghostradar.domain.geo.LatLon;
import com.example.ghostradar.domain.sensors.GpsSample;
import com.example.ghostradar.sensors.BaseSensorSource;
import com.example.ghostradar.sensors.GpsSensor;
import com.example.ghostradar.sensors.SensorError;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

/**
 * Source GPS Android basée sur {@link FusedLocationProviderClient}.
 *
 * <p>Le capteur pousse des fixes bruts. Le filtrage accuracy ≤ 20 m,
 * intervalle 1 s et distance 1 m est appliqué par {@code GpsFixFilter} dans
 * {@code SensorFrameCollector}.</p>
 */
public final class AndroidGpsSensor extends BaseSensorSource<GpsSample> implements GpsSensor {

    private final Context context;
    private final GameConfig config;
    private final FusedLocationProviderClient locationClient;
    private LocationCallback locationCallback;

    public AndroidGpsSensor(Context context, GameConfig config) {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null");
        }
        if (config == null) {
            throw new IllegalArgumentException("config must not be null");
        }
        this.context = context.getApplicationContext();
        this.config = config;
        this.locationClient = LocationServices.getFusedLocationProviderClient(this.context);
    }

    @Override
    protected SensorError onStart() {
        if (!hasLocationPermission()) {
            return SensorError.PERMISSION_DENIED;
        }
        if (isLocationServiceDisabled()) {
            return SensorError.SERVICE_DISABLED;
        }

        LocationRequest request = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, config.getGpsUpdateIntervalMs())
                .setMinUpdateIntervalMillis(config.getGpsUpdateIntervalMs())
                .setMinUpdateDistanceMeters((float) config.getGpsMinDistanceM())
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult result) {
                for (Location location : result.getLocations()) {
                    emit(new GpsSample(
                            new LatLon(location.getLatitude(), location.getLongitude()),
                            accuracyOf(location),
                            location.getElapsedRealtimeNanos()));
                }
            }
        };

        try {
            locationClient.requestLocationUpdates(
                    request, locationCallback, Looper.getMainLooper());
        } catch (SecurityException exception) {
            locationCallback = null;
            return SensorError.PERMISSION_DENIED;
        } catch (RuntimeException exception) {
            locationCallback = null;
            return SensorError.UNKNOWN;
        }
        return null;
    }

    @Override
    protected void onStop() {
        if (locationCallback != null) {
            locationClient.removeLocationUpdates(locationCallback);
            locationCallback = null;
        }
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean isLocationServiceDisabled() {
        LocationManager manager = (LocationManager) context
                .getSystemService(Context.LOCATION_SERVICE);
        if (manager == null) {
            return false;
        }
        return !manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                && !manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private double accuracyOf(Location location) {
        if (!location.hasAccuracy()) {
            return config.getGpsAccuracyMaxM() + 1.0;
        }
        return Math.max(0.0, location.getAccuracy());
    }
}
