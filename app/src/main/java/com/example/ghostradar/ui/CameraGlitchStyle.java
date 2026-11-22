package com.example.ghostradar.ui;

/**
 * Paramètres visuels du glitch caméra, sans dépendance Android.
 */
public final class CameraGlitchStyle {

    public static final int BAND_COUNT = 6;
    public static final int BAND_MIN_HEIGHT_PX = 4;
    public static final int BAND_MAX_HEIGHT_PX = 12;
    public static final double BAND_ALPHA = 0.35;
    public static final int BAND_RGB = 0x66CCFF;
    public static final int REFRESH_INTERVAL_MS = 50;

    private CameraGlitchStyle() {
    }

    /**
     * @return couleur ARGB des bandes parasites, prête pour un {@code Paint}.
     */
    public static int bandArgb() {
        return CameraFilterStyle.argb(BAND_RGB, BAND_ALPHA);
    }
}
