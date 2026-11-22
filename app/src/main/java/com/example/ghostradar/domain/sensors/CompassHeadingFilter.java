package com.example.ghostradar.domain.sensors;

import com.example.ghostradar.domain.config.GameConfig;
import com.example.ghostradar.domain.geo.GeoMath;

import java.util.Optional;

/**
 * Filtre de cap de boussole.
 *
 * <p>Les mesures dont la fiabilité est insuffisante sont ignorées lorsque
 * {@code COMPASS_IGNORE_UNRELIABLE} est actif. Le lissage applique
 * {@code COMPASS_LOW_PASS_ALPHA} sur l'écart angulaire le plus court, ce qui
 * évite les à-coups au passage par le nord.</p>
 */
public final class CompassHeadingFilter {

    private final GameConfig config;
    private CompassSample current;

    public CompassHeadingFilter(GameConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("config must not be null");
        }
        double alpha = config.getCompassLowPassAlpha();
        if (!Double.isFinite(alpha) || alpha < 0.0 || alpha > 1.0) {
            throw new IllegalArgumentException("compassLowPassAlpha must be in [0, 1]");
        }
        this.config = config;
    }

    /**
     * Tente d'accepter une mesure. Une mesure rejetée ne modifie pas l'état.
     */
    public Optional<CompassSample> accept(CompassSample sample) {
        if (sample == null) {
            throw new IllegalArgumentException("sample must not be null");
        }
        if (config.isCompassIgnoreUnreliable() && !sample.getAccuracy().isReliable()) {
            return Optional.empty();
        }
        if (current == null) {
            current = sample;
            return Optional.of(sample);
        }

        double alpha = config.getCompassLowPassAlpha();
        double deltaDeg = shortestDeltaDegrees(sample.getAzimuthDeg(), current.getAzimuthDeg());
        double smoothedDeg = GeoMath.normalizeAngleDegrees(
                current.getAzimuthDeg() + alpha * deltaDeg);
        current = new CompassSample(smoothedDeg, sample.getAccuracy(), sample.getTimestampNanos());
        return Optional.of(current);
    }

    public Optional<CompassSample> getCurrent() {
        return Optional.ofNullable(current);
    }

    public void reset() {
        current = null;
    }

    private static double shortestDeltaDegrees(double targetDeg, double currentDeg) {
        return ((targetDeg - currentDeg + 540.0) % 360.0) - 180.0;
    }
}
