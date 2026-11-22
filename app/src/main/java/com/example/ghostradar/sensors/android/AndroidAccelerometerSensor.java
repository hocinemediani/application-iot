package com.example.ghostradar.sensors.android;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.example.ghostradar.domain.config.GameConfig;
import com.example.ghostradar.domain.sensors.AccelerometerSample;
import com.example.ghostradar.sensors.AccelerometerSensor;
import com.example.ghostradar.sensors.BaseSensorSource;
import com.example.ghostradar.sensors.SensorError;

/**
 * Accéléromètre Android natif, échantillonné à {@code ACCEL_SAMPLING_HZ}.
 */
public final class AndroidAccelerometerSensor extends BaseSensorSource<AccelerometerSample>
        implements AccelerometerSensor, SensorEventListener {

    private final SensorManager sensorManager;
    private final int samplingPeriodUs;
    private Sensor sensor;

    public AndroidAccelerometerSensor(Context context, GameConfig config) {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null");
        }
        if (config == null) {
            throw new IllegalArgumentException("config must not be null");
        }
        double samplingHz = config.getAccelSamplingHz();
        if (!Double.isFinite(samplingHz) || samplingHz <= 0.0) {
            throw new IllegalArgumentException("accelSamplingHz must be finite and positive");
        }
        Context applicationContext = context.getApplicationContext();
        this.sensorManager = (SensorManager) applicationContext
                .getSystemService(Context.SENSOR_SERVICE);
        this.samplingPeriodUs = (int) Math.round(1_000_000.0 / samplingHz);
    }

    @Override
    protected SensorError onStart() {
        if (sensorManager == null) {
            return SensorError.SENSOR_UNAVAILABLE;
        }
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (sensor == null) {
            return SensorError.SENSOR_UNAVAILABLE;
        }
        if (!sensorManager.registerListener(this, sensor, samplingPeriodUs)) {
            sensor = null;
            return SensorError.SENSOR_UNAVAILABLE;
        }
        return null;
    }

    @Override
    protected void onStop() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
        sensor = null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER && event.values.length >= 3) {
            emit(new AccelerometerSample(
                    event.values[0], event.values[1], event.values[2], event.timestamp));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // L'accéléromètre n'expose pas de fiabilité exploitable par le tilt.
    }
}
