package com.example.ghostradar.domain.ai;

import com.example.ghostradar.domain.config.GameConfig;

import java.util.ArrayDeque;

/**
 * Détection du bruit ambiant pour la furtivité.
 *
 * <p>La couche Android fournit des blocs RMS normalisés dans {@code [0, 1]}
 * avec leur durée. Le monitor agrège l'énergie sur {@code NOISE_WINDOW_MS},
 * convertit la moyenne quadratique en dBFS, applique le lissage exponentiel
 * {@code NOISE_SMOOTHING_ALPHA}, puis gère le seuil de {@code -30 dBFS} : le
 * niveau doit rester au-dessus du seuil pendant {@code NOISE_HYSTERESIS_S}
 * pour armer l'alerte, qui reste active {@code NOISE_ALERT_HOLD_S} après
 * retour sous le seuil.</p>
 *
 * <p>Le monitor ignore volontairement la phase de jeu : l'intégration décide
 * d'ignorer l'alerte pendant {@code CAPTURE} lorsque
 * {@code NOISE_AFFECTS_CAPTURE = false}.</p>
 */
public final class NoiseMonitor {

    private static final double MILLIS_PER_SECOND = 1_000.0;

    private final GameConfig config;
    private final ArrayDeque<EnergySample> window = new ArrayDeque<>();

    private double windowDurationMs;
    private double windowEnergy;
    private double smoothedDbfs;
    private boolean smoothedInitialized;
    private boolean alertActive;
    private double aboveThresholdS;
    private double belowThresholdS;

    public NoiseMonitor(GameConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("config must not be null");
        }
        if (config.getNoiseWindowMs() <= 0) {
            throw new IllegalArgumentException("noiseWindowMs must be positive");
        }
        double alpha = config.getNoiseSmoothingAlpha();
        if (!(alpha >= 0.0 && alpha <= 1.0)) {
            throw new IllegalArgumentException("noiseSmoothingAlpha must be in [0, 1]");
        }
        if (config.getNoiseGaugeMaxDbfs() <= config.getNoiseGaugeMinDbfs()) {
            throw new IllegalArgumentException(
                    "noiseGaugeMaxDbfs must be greater than noiseGaugeMinDbfs");
        }
        if (config.getNoiseHysteresisS() < 0.0) {
            throw new IllegalArgumentException("noiseHysteresisS must not be negative");
        }
        if (config.getNoiseAlertHoldS() < 0.0) {
            throw new IllegalArgumentException("noiseAlertHoldS must not be negative");
        }
        this.config = config;
        this.smoothedDbfs = config.getNoiseGaugeMinDbfs();
    }

    /**
     * Intègre un bloc de mesure sonore.
     *
     * @param rmsNormalized niveau RMS normalisé, dans {@code [0, 1]}
     * @param deltaSeconds  durée du bloc, en secondes
     */
    public void update(double rmsNormalized, double deltaSeconds) {
        if (!(rmsNormalized >= 0.0 && rmsNormalized <= 1.0)) {
            throw new IllegalArgumentException("rmsNormalized must be in [0, 1]");
        }
        if (!(deltaSeconds >= 0.0)) {
            throw new IllegalArgumentException("deltaSeconds must not be negative");
        }
        if (deltaSeconds == 0.0) {
            return;
        }

        addToWindow(rmsNormalized * rmsNormalized,
                deltaSeconds * MILLIS_PER_SECOND);

        double windowDbfs = computeWindowDbfs();
        if (!smoothedInitialized) {
            smoothedDbfs = windowDbfs;
            smoothedInitialized = true;
        } else {
            double alpha = config.getNoiseSmoothingAlpha();
            smoothedDbfs = alpha * windowDbfs + (1.0 - alpha) * smoothedDbfs;
        }

        updateAlert(deltaSeconds);
    }

    public boolean isAlertActive() {
        return alertActive;
    }

    /**
     * Dernier niveau lissé en dBFS, borné par le plancher de la jauge.
     */
    public double getSmoothedDbfs() {
        return smoothedDbfs;
    }

    /**
     * Jauge HUD normalisée dans {@code [0, 1]} sur la plage configurée.
     */
    public double getGaugeLevel() {
        double minDbfs = config.getNoiseGaugeMinDbfs();
        double maxDbfs = config.getNoiseGaugeMaxDbfs();
        double level = (smoothedDbfs - minDbfs) / (maxDbfs - minDbfs);
        return Math.min(1.0, Math.max(0.0, level));
    }

    private void addToWindow(double rmsSquared, double durationMs) {
        double windowMs = config.getNoiseWindowMs();
        if (durationMs >= windowMs) {
            window.clear();
            windowDurationMs = 0.0;
            windowEnergy = 0.0;
            durationMs = windowMs;
        }

        window.addLast(new EnergySample(rmsSquared, durationMs));
        windowDurationMs += durationMs;
        windowEnergy += rmsSquared * durationMs;

        while (windowDurationMs > windowMs && !window.isEmpty()) {
            EnergySample oldest = window.peekFirst();
            double excessMs = windowDurationMs - windowMs;
            if (oldest.durationMs <= excessMs) {
                window.removeFirst();
                windowDurationMs -= oldest.durationMs;
                windowEnergy -= oldest.rmsSquared * oldest.durationMs;
            } else {
                oldest.durationMs -= excessMs;
                windowDurationMs -= excessMs;
                windowEnergy -= oldest.rmsSquared * excessMs;
            }
        }
    }

    private double computeWindowDbfs() {
        double minDbfs = config.getNoiseGaugeMinDbfs();
        if (windowDurationMs <= 0.0 || windowEnergy <= 0.0) {
            return minDbfs;
        }
        double meanSquare = windowEnergy / windowDurationMs;
        double dbfs = 10.0 * Math.log10(meanSquare);
        return Math.max(minDbfs, dbfs);
    }

    private void updateAlert(double deltaSeconds) {
        boolean aboveThreshold = smoothedDbfs > config.getNoiseThresholdDbfs();

        if (alertActive) {
            if (aboveThreshold) {
                belowThresholdS = 0.0;
            } else {
                belowThresholdS += deltaSeconds;
                if (belowThresholdS >= config.getNoiseAlertHoldS()) {
                    alertActive = false;
                    belowThresholdS = 0.0;
                    aboveThresholdS = 0.0;
                }
            }
            return;
        }

        if (aboveThreshold) {
            aboveThresholdS += deltaSeconds;
            belowThresholdS = 0.0;
            if (aboveThresholdS >= config.getNoiseHysteresisS()) {
                alertActive = true;
            }
        } else {
            aboveThresholdS = 0.0;
        }
    }

    private static final class EnergySample {

        private final double rmsSquared;
        private double durationMs;

        private EnergySample(double rmsSquared, double durationMs) {
            this.rmsSquared = rmsSquared;
            this.durationMs = durationMs;
        }
    }
}
