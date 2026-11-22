package com.example.ghostradar.domain.sensors;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class AccelerometerSampleTest {

    @Test
    public void storesAxesAndTimestamp() {
        AccelerometerSample sample = new AccelerometerSample(1.0, 9.81, -2.0, 42L);

        assertEquals(1.0, sample.getX(), 0.0);
        assertEquals(9.81, sample.getY(), 0.0);
        assertEquals(-2.0, sample.getZ(), 0.0);
        assertEquals(42L, sample.getTimestampNanos());
    }

    @Test
    public void rejectsNonFiniteAxes() {
        assertThrows(IllegalArgumentException.class,
                () -> new AccelerometerSample(Double.NaN, 0.0, 0.0, 0L));
        assertThrows(IllegalArgumentException.class,
                () -> new AccelerometerSample(0.0, Double.POSITIVE_INFINITY, 0.0, 0L));
        assertThrows(IllegalArgumentException.class,
                () -> new AccelerometerSample(0.0, 0.0, Double.NEGATIVE_INFINITY, 0L));
    }

    @Test
    public void rejectsNegativeTimestamp() {
        assertThrows(IllegalArgumentException.class,
                () -> new AccelerometerSample(0.0, 0.0, 0.0, -1L));
    }
}
