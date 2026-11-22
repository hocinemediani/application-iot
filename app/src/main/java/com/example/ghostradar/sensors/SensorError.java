package com.example.ghostradar.sensors;

/**
 * Raison pour laquelle un capteur n'a pas pu démarrer ou a cessé de fournir
 * des mesures.
 */
public enum SensorError {

    PERMISSION_DENIED,
    SENSOR_UNAVAILABLE,
    SERVICE_DISABLED,
    AUDIO_INIT_FAILED,
    UNKNOWN
}
