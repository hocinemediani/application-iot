package com.example.ghostradar.domain.sensors;

import com.example.ghostradar.domain.geo.LatLon;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class GpsSampleTest {

    @Test
    public void storesRawFixValues() {
        LatLon position = new LatLon(48.8566, 2.3522);
        GpsSample sample = new GpsSample(position, 4.5, 1_234_567_890L);

        assertEquals(position, sample.getPosition());
        assertEquals(4.5, sample.getAccuracyM(), 0.0);
        assertEquals(1_234_567_890L, sample.getTimestampNanos());
    }

    @Test
    public void rejectsNullPosition() {
        assertThrows(IllegalArgumentException.class,
                () -> new GpsSample(null, 4.5, 0L));
    }

    @Test
    public void rejectsNegativeAccuracy() {
        assertThrows(IllegalArgumentException.class,
                () -> new GpsSample(new LatLon(48.0, 2.0), -0.1, 0L));
    }

    @Test
    public void rejectsNonFiniteAccuracy() {
        assertThrows(IllegalArgumentException.class,
                () -> new GpsSample(new LatLon(48.0, 2.0), Double.NaN, 0L));
        assertThrows(IllegalArgumentException.class,
                () -> new GpsSample(new LatLon(48.0, 2.0), Double.POSITIVE_INFINITY, 0L));
    }

    @Test
    public void rejectsNegativeTimestamp() {
        assertThrows(IllegalArgumentException.class,
                () -> new GpsSample(new LatLon(48.0, 2.0), 4.5, -1L));
    }

    @Test
    public void acceptsZeroAccuracyAndZeroTimestamp() {
        GpsSample sample = new GpsSample(new LatLon(48.0, 2.0), 0.0, 0L);

        assertEquals(0.0, sample.getAccuracyM(), 0.0);
        assertEquals(0L, sample.getTimestampNanos());
    }
}
