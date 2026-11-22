package com.example.ghostradar.domain.sensors;

import com.example.ghostradar.domain.geo.GeoMath;

/**
 * Mesure brute de boussole.
 *
 * <p>L'azimut est normalisé dans {@code [0, 360)} dès la construction :
 * {@code 0° = Nord}, sens horaire.</p>
 */
public final class CompassSample {

    private final double azimuthDeg;
    private final CompassAccuracy accuracy;
    private final long timestampNanos;

    public CompassSample(double azimuthDeg, CompassAccuracy accuracy, long timestampNanos) {
        if (!Double.isFinite(azimuthDeg)) {
            throw new IllegalArgumentException("azimuthDeg must be finite");
        }
        if (accuracy == null) {
            throw new IllegalArgumentException("accuracy must not be null");
        }
        if (timestampNanos < 0L) {
            throw new IllegalArgumentException("timestampNanos must not be negative");
        }
        this.azimuthDeg = GeoMath.normalizeAngleDegrees(azimuthDeg);
        this.accuracy = accuracy;
        this.timestampNanos = timestampNanos;
    }

    public double getAzimuthDeg() {
        return azimuthDeg;
    }

    public CompassAccuracy getAccuracy() {
        return accuracy;
    }

    public long getTimestampNanos() {
        return timestampNanos;
    }
}
