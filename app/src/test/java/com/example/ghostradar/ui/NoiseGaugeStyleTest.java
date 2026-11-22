package com.example.ghostradar.ui;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NoiseGaugeStyleTest {

    @Test
    public void constantsMatchChosenGaugeDesign() {
        assertEquals(10, NoiseGaugeStyle.SEGMENT_COUNT);
        assertEquals(6, NoiseGaugeStyle.SEGMENT_WIDTH_DP);
        assertEquals(10, NoiseGaugeStyle.SEGMENT_HEIGHT_DP);
        assertEquals(2, NoiseGaugeStyle.SEGMENT_GAP_DP);
        assertEquals(16, NoiseGaugeStyle.RIGHT_MARGIN_DP);
        assertEquals(8, NoiseGaugeStyle.ICON_GAP_DP);
        assertEquals(16, NoiseGaugeStyle.ICON_SIZE_DP);
    }

    @Test
    public void colorsMatchChosenPalette() {
        assertEquals(0x66FFFFFF, NoiseGaugeStyle.EMPTY_ARGB);
        assertEquals(0xFFFFD54F, NoiseGaugeStyle.YELLOW_ARGB);
        assertEquals(0xFFFF9800, NoiseGaugeStyle.ORANGE_ARGB);
        assertEquals(0xFFFF3333, NoiseGaugeStyle.RED_ARGB);
        assertEquals(0xFFFFFFFF, NoiseGaugeStyle.MIC_ICON_COLOR_ARGB);
    }

    @Test
    public void yellowOrangeBoundaryIsMinusFortyDbfs() {
        assertEquals(-40.0, NoiseGaugeStyle.YELLOW_MAX_DBFS, 0.0001);
    }
}
