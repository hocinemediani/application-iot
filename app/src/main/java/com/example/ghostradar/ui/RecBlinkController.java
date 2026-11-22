package com.example.ghostradar.ui;

/**
 * Machine à états pure du clignotement de l'indicateur {@code ● REC}.
 */
public final class RecBlinkController {

    private final long halfPeriodMs;
    private long elapsedMs;

    public RecBlinkController(long halfPeriodMs) {
        if (halfPeriodMs <= 0) {
            throw new IllegalArgumentException("half period must be positive: " + halfPeriodMs);
        }
        this.halfPeriodMs = halfPeriodMs;
    }

    /** Fait progresser la phase de clignotement. */
    public void advance(long deltaMs) {
        if (deltaMs < 0) {
            throw new IllegalArgumentException("delta must not be negative: " + deltaMs);
        }
        elapsedMs += deltaMs;
    }

    /** @return {@code true} pendant la première moitié de chaque période. */
    public boolean isVisible() {
        return (elapsedMs / halfPeriodMs) % 2L == 0L;
    }

    /** Remet l'indicateur visible immédiatement. */
    public void reset() {
        elapsedMs = 0L;
    }

    public long getHalfPeriodMs() {
        return halfPeriodMs;
    }
}
