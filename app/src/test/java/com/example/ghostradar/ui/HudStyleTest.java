package com.example.ghostradar.ui;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HudStyleTest {

    @Test
    public void constantsMatchChosenHudDesign() {
        assertEquals("● REC", HudStyle.REC_TEXT);
        assertEquals(500, HudStyle.REC_BLINK_HALF_PERIOD_MS);
        assertEquals(1000, HudStyle.CLOCK_REFRESH_INTERVAL_MS);
        assertEquals(0xFFFF3333, HudStyle.REC_COLOR_ARGB);
        assertEquals(0xFFFFFFFF, HudStyle.CLOCK_COLOR_ARGB);
        assertEquals(0x99000000, HudStyle.BACKGROUND_ARGB);
        assertEquals(16, HudStyle.TEXT_SIZE_SP);
        assertEquals(16, HudStyle.MARGIN_DP);
        assertEquals("HH:mm:ss", HudStyle.CLOCK_PATTERN);
    }
}
