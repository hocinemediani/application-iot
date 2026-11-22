package com.example.ghostradar.ui;

import org.junit.Test;

import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class HudClockFormatterTest {

    @Test
    public void formatsUtcEpochWithLeadingZeros() {
        long epochMillis = utcEpochMillis(1, 2, 3);

        assertEquals("01:02:03", HudClockFormatter.format(epochMillis, ZoneOffset.UTC));
    }

    @Test
    public void formatsMidnightAsZeroHour() {
        long epochMillis = utcEpochMillis(0, 0, 0);

        assertEquals("00:00:00", HudClockFormatter.format(epochMillis, ZoneOffset.UTC));
    }

    @Test
    public void formatsEndOfDay() {
        long epochMillis = utcEpochMillis(23, 59, 59);

        assertEquals("23:59:59", HudClockFormatter.format(epochMillis, ZoneOffset.UTC));
    }

    @Test
    public void usesProvidedPhoneTimeZone() {
        long epochMillis = utcEpochMillis(0, 0, 0);

        assertEquals("01:00:00",
                HudClockFormatter.format(epochMillis, ZoneId.of("Europe/Paris")));
    }

    @Test
    public void ignoresMillisBelowOneSecond() {
        long epochMillis = utcEpochMillis(12, 34, 56) + 999L;

        assertEquals("12:34:56", HudClockFormatter.format(epochMillis, ZoneOffset.UTC));
    }

    @Test
    public void rejectsNullZone() {
        try {
            HudClockFormatter.format(0L, null);
            fail("zone must not be null");
        } catch (IllegalArgumentException expected) {
            // Attendu.
        }
    }

    private static long utcEpochMillis(int hour, int minute, int second) {
        return ((hour * 3600L) + (minute * 60L) + second) * 1000L;
    }
}
