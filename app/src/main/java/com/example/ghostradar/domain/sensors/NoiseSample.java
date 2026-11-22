package com.example.ghostradar.domain.sensors;

/**
 * Bloc de mesure sonore prêt pour {@code NoiseMonitor}.
 *
 * <p>Le RMS est normalisé dans {@code [0, 1]} par rapport à la pleine échelle
 * PCM 16 bits. La durée du bloc est exprimée en millisecondes.</p>
 */
public final class NoiseSample {

    private static final double MILLIS_PER_SECOND = 1_000.0;

    private final double rmsNormalized;
    private final double durationMs;
    private final long timestampNanos;

    public NoiseSample(double rmsNormalized, double durationMs, long timestampNanos) {
        if (!(rmsNormalized >= 0.0 && rmsNormalized <= 1.0)) {
            throw new IllegalArgumentException("rmsNormalized must be in [0, 1]");
        }
        if (!Double.isFinite(durationMs) || durationMs <= 0.0) {
            throw new IllegalArgumentException("durationMs must be finite and positive");
        }
        if (timestampNanos < 0L) {
            throw new IllegalArgumentException("timestampNanos must not be negative");
        }
        this.rmsNormalized = rmsNormalized;
        this.durationMs = durationMs;
        this.timestampNanos = timestampNanos;
    }

    public double getRmsNormalized() {
        return rmsNormalized;
    }

    public double getDurationMs() {
        return durationMs;
    }

    public double getDurationSeconds() {
        return durationMs / MILLIS_PER_SECOND;
    }

    public long getTimestampNanos() {
        return timestampNanos;
    }
}
