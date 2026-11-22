package com.example.ghostradar.domain.sensors;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class CompassSampleTest {

    @Test
    public void storesValuesAndNormalizesAzimuth() {
        CompassSample sample = new CompassSample(123.5, CompassAccuracy.HIGH, 42L);

        assertEquals(123.5, sample.getAzimuthDeg(), 0.0);
        assertEquals(CompassAccuracy.HIGH, sample.getAccuracy());
        assertEquals(42L, sample.getTimestampNanos());
    }

    @Test
    public void normalizesNegativeAndOverflowingAzimuth() {
        assertEquals(350.0,
                new CompassSample(-10.0, CompassAccuracy.HIGH, 0L).getAzimuthDeg(), 1e-9);
        assertEquals(10.0,
                new CompassSample(370.0, CompassAccuracy.HIGH, 0L).getAzimuthDeg(), 1e-9);
        assertEquals(0.0,
                new CompassSample(360.0, CompassAccuracy.HIGH, 0L).getAzimuthDeg(), 1e-9);
    }

    @Test
    public void rejectsNonFiniteAzimuth() {
        assertThrows(IllegalArgumentException.class,
                () -> new CompassSample(Double.NaN, CompassAccuracy.HIGH, 0L));
        assertThrows(IllegalArgumentException.class,
                () -> new CompassSample(Double.POSITIVE_INFINITY, CompassAccuracy.HIGH, 0L));
    }

    @Test
    public void rejectsNullAccuracy() {
        assertThrows(IllegalArgumentException.class,
                () -> new CompassSample(0.0, null, 0L));
    }

    @Test
    public void rejectsNegativeTimestamp() {
        assertThrows(IllegalArgumentException.class,
                () -> new CompassSample(0.0, CompassAccuracy.HIGH, -1L));
    }

    @Test
    public void reliabilityAcceptsOnlyMediumAndHigh() {
        assertTrue(CompassAccuracy.MEDIUM.isReliable());
        assertTrue(CompassAccuracy.HIGH.isReliable());
        assertFalse(CompassAccuracy.LOW.isReliable());
        assertFalse(CompassAccuracy.UNRELIABLE.isReliable());
        assertFalse(CompassAccuracy.NO_CONTACT.isReliable());
    }
}
