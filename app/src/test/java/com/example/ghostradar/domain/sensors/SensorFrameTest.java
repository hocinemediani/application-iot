package com.example.ghostradar.domain.sensors;

import com.example.ghostradar.domain.geo.LatLon;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class SensorFrameTest {

    private final GpsSample gps = new GpsSample(new LatLon(48.8566, 2.3522), 5.0, 1L);
    private final CompassSample compass = new CompassSample(90.0, CompassAccuracy.HIGH, 2L);
    private final NoiseSample noise = new NoiseSample(0.5, 20.0, 3L);
    private final AccelerometerSample accelerometer = new AccelerometerSample(0.0, 9.81, 0.0, 4L);

    @Test
    public void emptyFrameHasNoSamples() {
        assertTrue(SensorFrame.EMPTY.isEmpty());
        assertFalse(SensorFrame.EMPTY.hasGps());
        assertFalse(SensorFrame.EMPTY.hasCompass());
        assertFalse(SensorFrame.EMPTY.hasNoise());
        assertFalse(SensorFrame.EMPTY.hasAccelerometer());
        assertNull(SensorFrame.EMPTY.getGps());
        assertNull(SensorFrame.EMPTY.getCompass());
        assertNull(SensorFrame.EMPTY.getNoise());
        assertNull(SensorFrame.EMPTY.getAccelerometer());
    }

    @Test
    public void storesAllSamples() {
        SensorFrame frame = new SensorFrame(gps, compass, noise, accelerometer);

        assertFalse(frame.isEmpty());
        assertSame(gps, frame.getGps());
        assertSame(compass, frame.getCompass());
        assertSame(noise, frame.getNoise());
        assertSame(accelerometer, frame.getAccelerometer());
    }

    @Test
    public void withGpsReturnsNewFrameAndKeepsOtherSamples() {
        SensorFrame original = new SensorFrame(null, compass, noise, accelerometer);
        GpsSample updated = new GpsSample(new LatLon(48.0, 2.0), 4.0, 5L);

        SensorFrame frame = original.withGps(updated);

        assertNull(original.getGps());
        assertSame(updated, frame.getGps());
        assertSame(compass, frame.getCompass());
        assertSame(noise, frame.getNoise());
        assertSame(accelerometer, frame.getAccelerometer());
    }

    @Test
    public void withCompassReturnsNewFrameAndKeepsOtherSamples() {
        SensorFrame original = new SensorFrame(gps, null, noise, accelerometer);
        CompassSample updated = new CompassSample(180.0, CompassAccuracy.MEDIUM, 6L);

        SensorFrame frame = original.withCompass(updated);

        assertNull(original.getCompass());
        assertSame(gps, frame.getGps());
        assertSame(updated, frame.getCompass());
        assertSame(noise, frame.getNoise());
        assertSame(accelerometer, frame.getAccelerometer());
    }

    @Test
    public void withNoiseReturnsNewFrameAndKeepsOtherSamples() {
        SensorFrame original = new SensorFrame(gps, compass, null, accelerometer);
        NoiseSample updated = new NoiseSample(0.1, 20.0, 7L);

        SensorFrame frame = original.withNoise(updated);

        assertNull(original.getNoise());
        assertSame(gps, frame.getGps());
        assertSame(compass, frame.getCompass());
        assertSame(updated, frame.getNoise());
        assertSame(accelerometer, frame.getAccelerometer());
    }

    @Test
    public void withAccelerometerReturnsNewFrameAndKeepsOtherSamples() {
        SensorFrame original = new SensorFrame(gps, compass, noise, null);
        AccelerometerSample updated = new AccelerometerSample(1.0, 9.0, -1.0, 8L);

        SensorFrame frame = original.withAccelerometer(updated);

        assertNull(original.getAccelerometer());
        assertSame(gps, frame.getGps());
        assertSame(compass, frame.getCompass());
        assertSame(noise, frame.getNoise());
        assertSame(updated, frame.getAccelerometer());
    }

    @Test
    public void withSampleCanClearAField() {
        SensorFrame frame = new SensorFrame(gps, compass, noise, accelerometer);

        SensorFrame cleared = frame.withGps(null);

        assertNull(cleared.getGps());
        assertSame(gps, frame.getGps());
    }
}
