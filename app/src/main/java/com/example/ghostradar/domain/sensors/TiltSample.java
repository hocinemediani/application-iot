package com.example.ghostradar.domain.sensors;

/**
 * Résultat du calcul de tilt : valeurs brutes et valeurs filtrées.
 *
 * <p>Les valeurs filtrées ont subi la deadzone, le gain et le clamp de
 * {@code GameConfig}.</p>
 */
public final class TiltSample {

    private final double rawRollDeg;
    private final double rawPitchDeg;
    private final double rollDeg;
    private final double pitchDeg;

    public TiltSample(double rawRollDeg, double rawPitchDeg, double rollDeg, double pitchDeg) {
        if (!Double.isFinite(rawRollDeg) || !Double.isFinite(rawPitchDeg)
                || !Double.isFinite(rollDeg) || !Double.isFinite(pitchDeg)) {
            throw new IllegalArgumentException("tilt values must be finite");
        }
        this.rawRollDeg = rawRollDeg;
        this.rawPitchDeg = rawPitchDeg;
        this.rollDeg = rollDeg;
        this.pitchDeg = pitchDeg;
    }

    public double getRawRollDeg() {
        return rawRollDeg;
    }

    public double getRawPitchDeg() {
        return rawPitchDeg;
    }

    public double getRollDeg() {
        return rollDeg;
    }

    public double getPitchDeg() {
        return pitchDeg;
    }
}
