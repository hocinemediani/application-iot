package com.example.ghostradar.ui;

import java.util.Objects;

/**
 * Bande parasite immuable : position verticale normalisée et hauteur en pixels.
 */
public final class GlitchBand {

    private final double topRatio;
    private final int heightPx;

    public GlitchBand(double topRatio, int heightPx) {
        if (Double.isNaN(topRatio) || Double.isInfinite(topRatio)
                || topRatio < 0.0 || topRatio >= 1.0) {
            throw new IllegalArgumentException("topRatio must be finite and in [0, 1): " + topRatio);
        }
        if (heightPx < CameraGlitchStyle.BAND_MIN_HEIGHT_PX
                || heightPx > CameraGlitchStyle.BAND_MAX_HEIGHT_PX) {
            throw new IllegalArgumentException("heightPx is out of bounds: " + heightPx);
        }
        this.topRatio = topRatio;
        this.heightPx = heightPx;
    }

    public double getTopRatio() {
        return topRatio;
    }

    public int getHeightPx() {
        return heightPx;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof GlitchBand)) {
            return false;
        }
        GlitchBand that = (GlitchBand) other;
        return Double.compare(that.topRatio, topRatio) == 0 && heightPx == that.heightPx;
    }

    @Override
    public int hashCode() {
        return Objects.hash(topRatio, heightPx);
    }

    @Override
    public String toString() {
        return "GlitchBand{topRatio=" + topRatio + ", heightPx=" + heightPx + '}';
    }
}
