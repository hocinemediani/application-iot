package com.example.ghostradar.ui;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Formatage pur de l'horloge temps réel du téléphone.
 *
 * <p>L'Activity fournit {@code System.currentTimeMillis()} et le fuseau courant
 * du téléphone ; cette classe reste testable avec JUnit sans Android.</p>
 */
public final class HudClockFormatter {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern(HudStyle.CLOCK_PATTERN, Locale.ROOT);

    private HudClockFormatter() {
    }

    /**
     * @param epochMillis timestamp système en millisecondes
     * @param zone        fuseau horaire courant du téléphone
     * @return heure locale au format {@code HH:MM:SS}
     */
    public static String format(long epochMillis, ZoneId zone) {
        if (zone == null) {
            throw new IllegalArgumentException("zone must not be null");
        }
        return FORMATTER.format(Instant.ofEpochMilli(epochMillis).atZone(zone));
    }
}
