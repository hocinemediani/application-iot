package com.example.ghostradar.ui;

/**
 * Constantes visuelles du HUD caméscope, sans dépendance Android.
 */
public final class HudStyle {

    public static final String REC_TEXT = "● REC";
    public static final int REC_BLINK_HALF_PERIOD_MS = 500;
    public static final int CLOCK_REFRESH_INTERVAL_MS = 1000;
    public static final int REC_COLOR_ARGB = 0xFFFF3333;
    public static final int CLOCK_COLOR_ARGB = 0xFFFFFFFF;
    public static final int BACKGROUND_ARGB = 0x99000000;
    public static final int TEXT_SIZE_SP = 16;
    public static final int MARGIN_DP = 16;

    /** Pattern Java pour l'affichage `HH:MM:SS` du cahier des charges. */
    public static final String CLOCK_PATTERN = "HH:mm:ss";

    private HudStyle() {
    }
}
