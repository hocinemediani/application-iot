package com.example.ghostradar.domain.sensors;

import com.example.ghostradar.domain.geo.LatLon;

/**
 * Fix GPS brut fourni par la couche capteur.
 *
 * <p>Objet de transport immuable. La validité des coordonnées et le seuil de
 * précision sont contrôlés par {@link GpsFixFilter}, pas ici : un fix brut
 * peut donc porter une position invalide afin d'être explicitement rejeté.</p>
 */
public final class GpsSample {

    private final LatLon position;
    private final double accuracyM;
    private final long timestampNanos;

    public GpsSample(LatLon position, double accuracyM, long timestampNanos) {
        if (position == null) {
            throw new IllegalArgumentException("position must not be null");
        }
        if (!Double.isFinite(accuracyM) || accuracyM < 0.0) {
            throw new IllegalArgumentException("accuracyM must be finite and not negative");
        }
        if (timestampNanos < 0L) {
            throw new IllegalArgumentException("timestampNanos must not be negative");
        }
        this.position = position;
        this.accuracyM = accuracyM;
        this.timestampNanos = timestampNanos;
    }

    public LatLon getPosition() {
        return position;
    }

    public double getAccuracyM() {
        return accuracyM;
    }

    public long getTimestampNanos() {
        return timestampNanos;
    }
}
