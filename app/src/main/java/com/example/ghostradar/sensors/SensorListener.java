package com.example.ghostradar.sensors;

/**
 * Récepteur d'échantillons poussés par un capteur.
 *
 * @param <T> type d'échantillon du capteur
 */
public interface SensorListener<T> {

    void onSample(T sample);

    void onUnavailable(SensorError error);
}
