package com.example.ghostradar.ui;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CameraGlitchStyleTest {

    @Test
    public void constantsMatchChosenVisualParameters() {
        assertEquals(6, CameraGlitchStyle.BAND_COUNT);
        assertEquals(4, CameraGlitchStyle.BAND_MIN_HEIGHT_PX);
        assertEquals(12, CameraGlitchStyle.BAND_MAX_HEIGHT_PX);
        assertEquals(0.35, CameraGlitchStyle.BAND_ALPHA, 0.0001);
        assertEquals(0x66CCFF, CameraGlitchStyle.BAND_RGB);
        assertEquals(50, CameraGlitchStyle.REFRESH_INTERVAL_MS);
    }

    @Test
    public void bandArgbUsesChosenOpacity() {
        assertEquals(0x5966CCFF, CameraGlitchStyle.bandArgb());
    }
}
