package com.example.ghostradar.ui;

/**
 * Constantes visuelles de la jauge de bruit, sans dépendance Android.
 */
public final class NoiseGaugeStyle {

    public static final int SEGMENT_COUNT = 10;
    public static final int SEGMENT_WIDTH_DP = 6;
    public static final int SEGMENT_HEIGHT_DP = 10;
    public static final int SEGMENT_GAP_DP = 2;
    public static final int RIGHT_MARGIN_DP = 16;
    public static final int ICON_GAP_DP = 8;
    public static final int ICON_SIZE_DP = 16;

    public static final int EMPTY_ARGB = 0x66FFFFFF;
    public static final int YELLOW_ARGB = 0xFFFFD54F;
    public static final int ORANGE_ARGB = 0xFFFF9800;
    public static final int RED_ARGB = 0xFFFF3333;
    public static final int MIC_ICON_COLOR_ARGB = 0xFFFFFFFF;

    /** Limite jaune → orange ; le seuil d'alerte configure orange → rouge. */
    public static final double YELLOW_MAX_DBFS = -40.0;

    private NoiseGaugeStyle() {
    }
}
