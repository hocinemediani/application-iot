package com.example.ghostradar.ui;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class CameraFilterStyleTest {

    @Test
    public void blueIrRgbMatchesChosenTint() {
        assertEquals(0x003366, CameraFilterStyle.BLUE_IR_RGB);
    }

    @Test
    public void defaultFilterUsesConfiguredAlphaRoundedToByte() {
        assertEquals(0x26003366, CameraFilterStyle.argb(CameraFilterStyle.BLUE_IR_RGB, 0.15));
    }

    @Test
    public void alphaZeroKeepsRgbWithoutOpacity() {
        assertEquals(0x00003366, CameraFilterStyle.argb(CameraFilterStyle.BLUE_IR_RGB, 0.0));
    }

    @Test
    public void alphaOneIsFullyOpaque() {
        assertEquals(0xFF003366, CameraFilterStyle.argb(CameraFilterStyle.BLUE_IR_RGB, 1.0));
    }

    @Test
    public void alphaIsRoundedToNearestByte() {
        assertEquals(0x80000000, CameraFilterStyle.argb(0x000000, 0.5));
    }

    @Test
    public void pureWhiteRgbIsPreserved() {
        assertEquals(0x26FFFFFF, CameraFilterStyle.argb(0xFFFFFF, 0.15));
    }

    @Test
    public void rejectsInvalidAlpha() {
        assertInvalidAlpha(Double.NaN);
        assertInvalidAlpha(-0.01);
        assertInvalidAlpha(1.01);
        assertInvalidAlpha(Double.POSITIVE_INFINITY);
    }

    @Test
    public void rejectsInvalidRgb() {
        assertInvalidRgb(-1);
        assertInvalidRgb(0x1000000);
    }

    private static void assertInvalidAlpha(double alpha) {
        try {
            CameraFilterStyle.argb(CameraFilterStyle.BLUE_IR_RGB, alpha);
            fail("alpha must be finite and in [0, 1]: " + alpha);
        } catch (IllegalArgumentException expected) {
            // Attendu.
        }
    }

    private static void assertInvalidRgb(int rgb) {
        try {
            CameraFilterStyle.argb(rgb, 0.15);
            fail("rgb must be a 24-bit value: " + rgb);
        } catch (IllegalArgumentException expected) {
            // Attendu.
        }
    }
}
