package com.example.ghostradar.sensors;

/**
 * Contrat commun des capteurs : démarrage, arrêt et poussée d'échantillons.
 *
 * <p>{@link #start(SensorListener)} est idempotent : un second appel remplace
 * simplement le listener sans redémarrer le capteur. {@link #stop()} est
 * également idempotent.</p>
 *
 * @param <T> type d'échantillon du capteur
 */
public interface SensorSource<T> {

    void start(SensorListener<T> listener);

    void stop();

    boolean isRunning();
}
