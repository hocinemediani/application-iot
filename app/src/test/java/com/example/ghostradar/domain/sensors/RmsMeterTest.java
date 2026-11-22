package com.example.ghostradar.domain.sensors;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class RmsMeterTest {

    private static final double PCM16_FULL_SCALE = 32_768.0;

    @Test
    public void silenceIsZero() {
        short[] pcm = new short[100];

        assertEquals(0.0, RmsMeter.computeRmsNormalized(pcm), 0.0);
    }

    @Test
    public void positiveFullScaleIsNearOne() {
        short[] pcm = filled(100, Short.MAX_VALUE);

        assertEquals(Short.MAX_VALUE / PCM16_FULL_SCALE,
                RmsMeter.computeRmsNormalized(pcm), 1e-12);
    }

    @Test
    public void negativeFullScaleIsOne() {
        short[] pcm = filled(100, Short.MIN_VALUE);

        assertEquals(1.0, RmsMeter.computeRmsNormalized(pcm), 1e-12);
    }

    @Test
    public void halfScaleIsHalf() {
        short[] pcm = filled(100, (short) 16_384);

        assertEquals(0.5, RmsMeter.computeRmsNormalized(pcm), 1e-12);
    }

    @Test
    public void alternatingFullScaleIsNearOne() {
        short[] pcm = new short[100];
        for (int i = 0; i < pcm.length; i++) {
            pcm[i] = (i % 2 == 0) ? Short.MAX_VALUE : Short.MIN_VALUE;
        }
        double positive = Short.MAX_VALUE / PCM16_FULL_SCALE;
        double expected = Math.sqrt((positive * positive + 1.0) / 2.0);

        assertEquals(expected, RmsMeter.computeRmsNormalized(pcm), 1e-12);
    }

    @Test
    public void usesOnlyRequestedRange() {
        short[] pcm = new short[]{Short.MAX_VALUE, 0, 0, Short.MAX_VALUE};

        assertEquals(0.0, RmsMeter.computeRmsNormalized(pcm, 1, 2), 0.0);
    }

    @Test
    public void fullArrayOverloadMatchesExplicitRange() {
        short[] pcm = filled(50, (short) 8_192);

        assertEquals(RmsMeter.computeRmsNormalized(pcm, 0, pcm.length),
                RmsMeter.computeRmsNormalized(pcm), 0.0);
    }

    @Test
    public void rejectsNullPcm() {
        assertThrows(IllegalArgumentException.class, () -> RmsMeter.computeRmsNormalized(null));
        assertThrows(IllegalArgumentException.class,
                () -> RmsMeter.computeRmsNormalized(null, 0, 1));
    }

    @Test
    public void rejectsInvalidRange() {
        short[] pcm = new short[10];

        assertThrows(IllegalArgumentException.class,
                () -> RmsMeter.computeRmsNormalized(pcm, -1, 2));
        assertThrows(IllegalArgumentException.class,
                () -> RmsMeter.computeRmsNormalized(pcm, 0, 0));
        assertThrows(IllegalArgumentException.class,
                () -> RmsMeter.computeRmsNormalized(pcm, 0, -1));
        assertThrows(IllegalArgumentException.class,
                () -> RmsMeter.computeRmsNormalized(pcm, 9, 2));
        assertThrows(IllegalArgumentException.class,
                () -> RmsMeter.computeRmsNormalized(pcm, 0, 11));
    }

    @Test
    public void rejectsEmptyArray() {
        assertThrows(IllegalArgumentException.class,
                () -> RmsMeter.computeRmsNormalized(new short[0]));
    }

    private static short[] filled(int length, short value) {
        short[] pcm = new short[length];
        Arrays.fill(pcm, value);
        return pcm;
    }
}
