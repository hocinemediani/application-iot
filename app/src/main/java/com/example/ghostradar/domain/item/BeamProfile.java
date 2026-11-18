package com.example.ghostradar.domain.item;

import com.example.ghostradar.domain.config.GameConfig;

/**
 * Profil du faisceau de capture, du niveau 1 au niveau maximum.
 *
 * <p>Le temps requis diminue de {@code BEAM_TIME_REDUCTION_PER_LEVEL_S} par
 * niveau depuis {@code BEAM_BASE_REQUIRED_SECONDS}, sans jamais descendre sous
 * {@code BEAM_MIN_REQUIRED_SECONDS}. La tolérance angulaire augmente par
 * paliers de {@code BEAM_LEVELS_PER_TOLERANCE_STEP} niveaux, de
 * {@code BEAM_ANGLE_TOLERANCE_STEP_DEG} par palier, depuis
 * {@code BEAM_BASE_ANGLE_TOLERANCE_DEG}.</p>
 *
 * <p>Le profil ne gère pas les lootboxes : l'appelant décide du moment de
 * l'amélioration et appelle {@link #upgrade()}.</p>
 */
public final class BeamProfile {

    private final GameConfig config;
    private int level = 1;

    public BeamProfile(GameConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("config must not be null");
        }
        if (config.getBeamMaxLevel() < 1) {
            throw new IllegalArgumentException("beamMaxLevel must be positive");
        }
        requirePositive("beamBaseRequiredSeconds", config.getBeamBaseRequiredSeconds());
        requirePositive("beamMinRequiredSeconds", config.getBeamMinRequiredSeconds());
        if (!isFinite(config.getBeamTimeReductionPerLevelS())
                || config.getBeamTimeReductionPerLevelS() < 0.0) {
            throw new IllegalArgumentException(
                    "beamTimeReductionPerLevelS must be finite and not negative");
        }
        requirePositive("beamBaseAngleToleranceDeg", config.getBeamBaseAngleToleranceDeg());
        requirePositive("beamAngleToleranceStepDeg", config.getBeamAngleToleranceStepDeg());
        if (config.getBeamLevelsPerToleranceStep() < 1) {
            throw new IllegalArgumentException("beamLevelsPerToleranceStep must be positive");
        }
        this.config = config;
    }

    public int getLevel() {
        return level;
    }

    /**
     * Temps de verrouillage requis au niveau courant, en secondes.
     */
    public double getRequiredSeconds() {
        double requiredSeconds = config.getBeamBaseRequiredSeconds()
                - config.getBeamTimeReductionPerLevelS() * (level - 1);
        return Math.max(config.getBeamMinRequiredSeconds(), requiredSeconds);
    }

    /**
     * Tolérance angulaire du faisceau au niveau courant, en degrés.
     */
    public double getAngleToleranceDeg() {
        int steps = (level - 1) / config.getBeamLevelsPerToleranceStep();
        return config.getBeamBaseAngleToleranceDeg()
                + config.getBeamAngleToleranceStepDeg() * steps;
    }

    /**
     * Passe au niveau suivant.
     *
     * @return {@code true} si le niveau a augmenté, {@code false} si le niveau
     *         maximum est déjà atteint
     */
    public boolean upgrade() {
        if (level >= config.getBeamMaxLevel()) {
            return false;
        }
        level++;
        return true;
    }

    private static void requirePositive(String name, double value) {
        if (!isFinite(value) || value <= 0.0) {
            throw new IllegalArgumentException(name + " must be finite and positive");
        }
    }

    private static boolean isFinite(double value) {
        return !Double.isNaN(value) && !Double.isInfinite(value);
    }
}
