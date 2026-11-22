package com.example.ghostradar.sensors.android;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.example.ghostradar.domain.config.GameConfig;
import com.example.ghostradar.domain.sensors.CompassAccuracy;
import com.example.ghostradar.domain.sensors.CompassSample;
import com.example.ghostradar.sensors.BaseSensorSource;
import com.example.ghostradar.sensors.CompassSensor;
import com.example.ghostradar.sensors.SensorError;

/**
 * Boussole Android : {@code TYPE_ROTATION_VECTOR} en priorité, avec repli
 * magnétomètre + accéléromètre lorsque le capteur de rotation est absent.
 */
public final class AndroidCompassSensor extends BaseSensorSource<CompassSample>
        implements CompassSensor, SensorEventListener {

    private final SensorManager sensorManager;
    private final boolean fallbackEnabled;
    private final float[] rotationMatrix = new float[9];
    private final float[] orientation = new float[3];

    private Sensor rotationVectorSensor;
    private Sensor magneticFieldSensor;
    private Sensor accelerometerSensor;
    private float[] lastAccelerometer;
    private float[] lastMagneticField;
    private int fallbackAccuracy = SensorManager.SENSOR_STATUS_UNRELIABLE;
    private boolean fallbackActive;

    public AndroidCompassSensor(Context context, GameConfig config) {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null");
        }
        if (config == null) {
            throw new IllegalArgumentException("config must not be null");
        }
        this.sensorManager = (SensorManager) context.getApplicationContext()
                .getSystemService(Context.SENSOR_SERVICE);
        this.fallbackEnabled = config.isCompassFallbackEnabled();
    }

    @Override
    protected SensorError onStart() {
        if (sensorManager == null) {
            return SensorError.SENSOR_UNAVAILABLE;
        }

        rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        if (rotationVectorSensor != null) {
            if (!sensorManager.registerListener(this, rotationVectorSensor,
                    SensorManager.SENSOR_DELAY_GAME)) {
                rotationVectorSensor = null;
                return SensorError.SENSOR_UNAVAILABLE;
            }
            return null;
        }

        if (!fallbackEnabled) {
            return SensorError.SENSOR_UNAVAILABLE;
        }

        magneticFieldSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (magneticFieldSensor == null || accelerometerSensor == null) {
            clearSensors();
            return SensorError.SENSOR_UNAVAILABLE;
        }

        boolean magneticRegistered = sensorManager.registerListener(this,
                magneticFieldSensor, SensorManager.SENSOR_DELAY_GAME);
        boolean accelerometerRegistered = sensorManager.registerListener(this,
                accelerometerSensor, SensorManager.SENSOR_DELAY_GAME);
        if (!magneticRegistered || !accelerometerRegistered) {
            sensorManager.unregisterListener(this);
            clearSensors();
            return SensorError.SENSOR_UNAVAILABLE;
        }

        fallbackActive = true;
        return null;
    }

    @Override
    protected void onStop() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
        clearSensors();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int type = event.sensor.getType();
        if (type == Sensor.TYPE_ROTATION_VECTOR) {
            handleRotationVector(event);
        } else if (fallbackActive && type == Sensor.TYPE_ACCELEROMETER) {
            lastAccelerometer = event.values.clone();
            computeFallbackHeading(event.timestamp);
        } else if (fallbackActive && type == Sensor.TYPE_MAGNETIC_FIELD) {
            lastMagneticField = event.values.clone();
            fallbackAccuracy = event.accuracy;
            computeFallbackHeading(event.timestamp);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // L'accuracy est portée par chaque SensorEvent ; rien à mémoriser ici.
    }

    private void handleRotationVector(SensorEvent event) {
        if (event.values.length < 3) {
            return;
        }
        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
        SensorManager.getOrientation(rotationMatrix, orientation);
        emit(new CompassSample(
                Math.toDegrees(orientation[0]),
                mapAccuracy(event.accuracy),
                event.timestamp));
    }

    private void computeFallbackHeading(long timestampNanos) {
        if (lastAccelerometer == null || lastMagneticField == null) {
            return;
        }
        if (!SensorManager.getRotationMatrix(
                rotationMatrix, null, lastAccelerometer, lastMagneticField)) {
            return;
        }
        SensorManager.getOrientation(rotationMatrix, orientation);
        emit(new CompassSample(
                Math.toDegrees(orientation[0]),
                mapAccuracy(fallbackAccuracy),
                timestampNanos));
    }

    private void clearSensors() {
        rotationVectorSensor = null;
        magneticFieldSensor = null;
        accelerometerSensor = null;
        lastAccelerometer = null;
        lastMagneticField = null;
        fallbackActive = false;
        fallbackAccuracy = SensorManager.SENSOR_STATUS_UNRELIABLE;
    }

    private static CompassAccuracy mapAccuracy(int androidAccuracy) {
        switch (androidAccuracy) {
            case SensorManager.SENSOR_STATUS_ACCURACY_HIGH:
                return CompassAccuracy.HIGH;
            case SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM:
                return CompassAccuracy.MEDIUM;
            case SensorManager.SENSOR_STATUS_ACCURACY_LOW:
                return CompassAccuracy.LOW;
            case SensorManager.SENSOR_STATUS_NO_CONTACT:
                return CompassAccuracy.NO_CONTACT;
            default:
                return CompassAccuracy.UNRELIABLE;
        }
    }
}
