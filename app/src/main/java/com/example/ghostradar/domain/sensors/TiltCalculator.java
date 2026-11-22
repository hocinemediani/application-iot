package com.example.ghostradar.domain.sensors;

import com.example.ghostradar.domain.config.GameConfig;

/**
 * Calcul du roll et du pitch pour un téléphone tenu verticalement en portrait.
 *
 * <p>Convention retenue : {@code roll = atan2(x, y)} et
 * {@code pitch = atan2(-z, y)}. Au repos, téléphone vertical, les deux angles
 * valent 0°. La deadzone, le gain et le clamp de {@code GameConfig} sont
 * appliqués après le calcul brut.</p>
 */
public final class TiltCalculator {

    private static final double ANGLE_EPSILON_DEG = 1e-9;

    private final GameConfig config;

    public TiltCalculator(GameConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("config must not be null");
        }
        double deadzone = config.getTiltDeadzoneDeg();
        double gain = config.getTiltGain();
        double clamp = config.getTiltClampDeg();
        if (!Double.isFinite(deadzone) || deadzone < 0.0) {
            throw new IllegalArgumentException("tiltDeadzoneDeg must be finite and not negative");
        }
        if (!Double.isFinite(gain) || gain < 0.0) {
            throw new IllegalArgumentException("tiltGain must be finite and not negative");
        }
        if (!Double.isFinite(clamp) || clamp <= 0.0) {
            throw new IllegalArgumentException("tiltClampDeg must be finite and positive");
        }
        if (deadzone > clamp) {
            throw new IllegalArgumentException("tiltDeadzoneDeg must not exceed tiltClampDeg");
        }
        this.config = config;
    }

    public TiltSample calculate(AccelerometerSample sample) {
        if (sample == null) {
            throw new IllegalArgumentException("sample must not be null");
        }
        double rawRollDeg = Math.toDegrees(Math.atan2(sample.getX(), sample.getY()));
        double rawPitchDeg = Math.toDegrees(Math.atan2(-sample.getZ(), sample.getY()));
        return new TiltSample(rawRollDeg, rawPitchDeg,
                applyFilters(rawRollDeg), applyFilters(rawPitchDeg));
    }

    private double applyFilters(double angleDeg) {
        if (Math.abs(angleDeg) <= config.getTiltDeadzoneDeg() + ANGLE_EPSILON_DEG) {
            return 0.0;
        }
        double scaled = angleDeg * config.getTiltGain();
        return Math.max(-config.getTiltClampDeg(),
                Math.min(config.getTiltClampDeg(), scaled));
    }
}
