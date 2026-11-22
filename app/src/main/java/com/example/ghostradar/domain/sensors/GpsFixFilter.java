package com.example.ghostradar.domain.sensors;

import com.example.ghostradar.domain.config.GameConfig;
import com.example.ghostradar.domain.geo.GeoMath;
import com.example.ghostradar.domain.geo.LatLon;

import java.util.Optional;

/**
 * Filtre logiciel des fixes GPS bruts.
 *
 * <p>Un fix n'est accepté que si :</p>
 * <ul>
 *   <li>ses coordonnées sont finies et dans les plages géographiques ;</li>
 *   <li>son accuracy est inférieure ou égale à {@code GPS_ACCURACY_MAX_M} ;</li>
 *   <li>au moins {@code GPS_UPDATE_INTERVAL_MS} s'est écoulé depuis le dernier
 *       fix accepté ;</li>
 *   <li>il s'est déplacé d'au moins {@code GPS_MIN_DISTANCE_M} depuis le
 *       dernier fix accepté.</li>
 * </ul>
 *
 * <p>Le filtre est volontairement à état et non thread-safe : il est prévu
 * pour être appelé depuis le thread de réception GPS.</p>
 */
public final class GpsFixFilter {

    private static final long NANOS_PER_MILLI = 1_000_000L;
    private static final double TIME_EPSILON_NANOS = 1.0;
    private static final double DISTANCE_EPSILON_M = 1e-6;
    private static final double ACCURACY_EPSILON_M = 1e-9;

    private final GameConfig config;
    private GpsSample lastAccepted;

    public GpsFixFilter(GameConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("config must not be null");
        }
        if (!isPositiveFinite(config.getGpsAccuracyMaxM())) {
            throw new IllegalArgumentException("gpsAccuracyMaxM must be finite and positive");
        }
        if (config.getGpsUpdateIntervalMs() <= 0) {
            throw new IllegalArgumentException("gpsUpdateIntervalMs must be positive");
        }
        if (!isNonNegativeFinite(config.getGpsMinDistanceM())) {
            throw new IllegalArgumentException("gpsMinDistanceM must be finite and not negative");
        }
        if (!isPositiveFinite(config.getEarthRadiusM())) {
            throw new IllegalArgumentException("earthRadiusM must be finite and positive");
        }
        this.config = config;
    }

    /**
     * Tente d'accepter un fix. Un fix rejeté ne modifie pas l'état du filtre.
     */
    public Optional<GpsSample> accept(GpsSample sample) {
        if (sample == null) {
            throw new IllegalArgumentException("sample must not be null");
        }
        if (!isValidPosition(sample.getPosition())) {
            return Optional.empty();
        }
        if (sample.getAccuracyM() > config.getGpsAccuracyMaxM() + ACCURACY_EPSILON_M) {
            return Optional.empty();
        }
        if (lastAccepted == null) {
            lastAccepted = sample;
            return Optional.of(sample);
        }

        long elapsedNanos = sample.getTimestampNanos() - lastAccepted.getTimestampNanos();
        if (elapsedNanos < 0L) {
            return Optional.empty();
        }

        long intervalNanos = config.getGpsUpdateIntervalMs() * NANOS_PER_MILLI;
        boolean intervalElapsed = elapsedNanos + TIME_EPSILON_NANOS >= intervalNanos;

        double distanceM = GeoMath.haversineDistanceMeters(
                lastAccepted.getPosition(), sample.getPosition(), config.getEarthRadiusM());
        boolean movedEnough = distanceM + DISTANCE_EPSILON_M >= config.getGpsMinDistanceM();

        if (intervalElapsed && movedEnough) {
            lastAccepted = sample;
            return Optional.of(sample);
        }
        return Optional.empty();
    }

    public Optional<GpsSample> getLastAccepted() {
        return Optional.ofNullable(lastAccepted);
    }

    public void reset() {
        lastAccepted = null;
    }

    private static boolean isValidPosition(LatLon position) {
        if (position == null) {
            return false;
        }
        double latitude = position.getLatitude();
        double longitude = position.getLongitude();
        return Double.isFinite(latitude) && Double.isFinite(longitude)
                && latitude >= -90.0 && latitude <= 90.0
                && longitude >= -180.0 && longitude <= 180.0;
    }

    private static boolean isPositiveFinite(double value) {
        return Double.isFinite(value) && value > 0.0;
    }

    private static boolean isNonNegativeFinite(double value) {
        return Double.isFinite(value) && value >= 0.0;
    }
}
