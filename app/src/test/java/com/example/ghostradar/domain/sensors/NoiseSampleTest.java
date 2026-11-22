package com.example.ghostradar.domain.sensors;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class NoiseSampleTest {

    @Test
    public void storesValuesAndConvertsDurationToSeconds() {
        NoiseSample sample = new NoiseSample(0.5, 20.0, 123L);

        assertEquals(0.5, sample.getRmsNormalized(), 0.0);
        assertEquals(20.0, sample.getDurationMs(), 0.0);
        assertEquals(0.02, sample.getDurationSeconds(), 1e-12);
        assertEquals(123L, sample.getTimestampNanos());
    }

    @Test
    public void acceptsBoundaryRmsValues() {
        assertEquals(0.0, new NoiseSample(0.0, 20.0, 0L).getRmsNormalized(), 0.0);
        assertEquals(1.0, new NoiseSample(1.0, 20.0, 0L).getRmsNormalized(), 0.0);
    }

    @Test
    public void rejectsRmsOutOfRange() {
        assertThrows(IllegalArgumentException.class, () -> new NoiseSample(-0.1, 20.0, 0L));
        assertThrows(IllegalArgumentException.class, () -> new NoiseSample(1.1, 20.0, 0L));
        assertThrows(IllegalArgumentException.class,
                () -> new NoiseSample(Double.NaN, 20.0, 0L));
        assertThrows(IllegalArgumentException.class,
                () -> new NoiseSample(Double.POSITIVE_INFINITY, 20.0, 0L));
    }

    @Test
    public void rejectsInvalidDuration() {
        assertThrows(IllegalArgumentException.class, () -> new NoiseSample(0.5, 0.0, 0L));
        assertThrows(IllegalArgumentException.class, () -> new NoiseSample(0.5, -1.0, 0L));
        assertThrows(IllegalArgumentException.class,
                () -> new NoiseSample(0.5, Double.NaN, 0L));
        assertThrows(IllegalArgumentException.class,
                () -> new NoiseSample(0.5, Double.POSITIVE_INFINITY, 0L));
    }

    @Test
    public void rejectsNegativeTimestamp() {
        assertThrows(IllegalArgumentException.class, () -> new NoiseSample(0.5, 20.0, -1L));
    }
}
