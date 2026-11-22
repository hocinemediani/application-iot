package com.example.ghostradar.sensors;

import com.example.ghostradar.domain.config.GameConfig;
import com.example.ghostradar.domain.geo.LatLon;
import com.example.ghostradar.domain.sensors.AccelerometerSample;
import com.example.ghostradar.domain.sensors.CompassAccuracy;
import com.example.ghostradar.domain.sensors.CompassSample;
import com.example.ghostradar.domain.sensors.GpsSample;
import com.example.ghostradar.domain.sensors.NoiseSample;
import com.example.ghostradar.domain.sensors.SensorFrame;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class SensorFrameCollectorTest {

    private static final long ONE_SECOND_NANOS = 1_000_000_000L;

    @Test
    public void rejectsNullConfiguration() {
        assertThrows(IllegalArgumentException.class, () -> new SensorFrameCollector(null));
    }

    @Test
    public void startsWithEmptyFrame() {
        SensorFrameCollector collector = new SensorFrameCollector(GameConfig.defaults());

        assertSame(SensorFrame.EMPTY, collector.getFrame());
        assertTrue(collector.getFrame().isEmpty());
    }

    @Test
    public void acceptedGpsFixUpdatesFrame() {
        SensorFrameCollector collector = new SensorFrameCollector(GameConfig.defaults());
        GpsSample sample = gps(48.8566, 2.3522, 5.0, 0L);

        assertTrue(collector.acceptGps(sample));

        assertSame(sample, collector.getFrame().getGps());
    }

    @Test
    public void rejectedGpsFixLeavesFrameUnchanged() {
        SensorFrameCollector collector = new SensorFrameCollector(GameConfig.defaults());
        collector.acceptGps(gps(48.8566, 2.3522, 5.0, 0L));
        SensorFrame before = collector.getFrame();

        assertFalse(collector.acceptGps(gps(48.8566, 2.3522, 5.0, ONE_SECOND_NANOS)));

        assertSame(before, collector.getFrame());
    }

    @Test
    public void inaccurateGpsFixIsRejected() {
        SensorFrameCollector collector = new SensorFrameCollector(GameConfig.defaults());

        assertFalse(collector.acceptGps(gps(48.8566, 2.3522, 25.0, 0L)));

        assertFalse(collector.getFrame().hasGps());
    }

    @Test
    public void compassIsSmoothedAndUnreliableSamplesAreIgnored() {
        SensorFrameCollector collector = new SensorFrameCollector(GameConfig.defaults());

        assertTrue(collector.acceptCompass(compass(0.0, CompassAccuracy.HIGH, 0L)));
        assertEquals(0.0, collector.getFrame().getCompass().getAzimuthDeg(), 1e-9);

        assertFalse(collector.acceptCompass(compass(90.0, CompassAccuracy.LOW, 1L)));
        assertEquals(0.0, collector.getFrame().getCompass().getAzimuthDeg(), 1e-9);

        assertTrue(collector.acceptCompass(compass(100.0, CompassAccuracy.HIGH, 2L)));
        assertEquals(15.0, collector.getFrame().getCompass().getAzimuthDeg(), 1e-9);
    }

    @Test
    public void noiseUpdatesFrameAndAlertState() {
        SensorFrameCollector collector = new SensorFrameCollector(GameConfig.defaults());
        NoiseSample sample = new NoiseSample(1.0, 20.0, 0L);

        for (int i = 0; i < 110; i++) {
            collector.acceptNoise(sample);
        }

        assertNotNull(collector.getFrame().getNoise());
        assertTrue(collector.isNoiseAlertActive());
        assertEquals(1.0, collector.getNoiseGaugeLevel(), 1e-3);
        assertEquals(0.0, collector.getSmoothedNoiseDbfs(), 0.01);
    }

    @Test
    public void accelerometerUpdatesRawSampleAndTilt() {
        SensorFrameCollector collector = new SensorFrameCollector(GameConfig.defaults());
        AccelerometerSample sample = new AccelerometerSample(0.0, 9.81, 0.0, 10L);

        collector.acceptAccelerometer(sample);

        SensorFrame frame = collector.getFrame();
        assertSame(sample, frame.getAccelerometer());
        assertNotNull(frame.getTilt());
        assertEquals(0.0, frame.getTilt().getRollDeg(), 1e-9);
        assertEquals(0.0, frame.getTilt().getPitchDeg(), 1e-9);
    }

    @Test
    public void frameKeepsEveryAcceptedSample() {
        SensorFrameCollector collector = new SensorFrameCollector(GameConfig.defaults());

        collector.acceptGps(gps(48.8566, 2.3522, 5.0, 0L));
        collector.acceptCompass(compass(90.0, CompassAccuracy.HIGH, 1L));
        collector.acceptNoise(new NoiseSample(0.2, 20.0, 2L));
        collector.acceptAccelerometer(new AccelerometerSample(0.0, 9.81, 0.0, 3L));

        SensorFrame frame = collector.getFrame();
        assertTrue(frame.hasGps());
        assertTrue(frame.hasCompass());
        assertTrue(frame.hasNoise());
        assertTrue(frame.hasAccelerometer());
        assertTrue(frame.hasTilt());
        assertFalse(frame.isEmpty());
    }

    @Test
    public void nullSamplesAreRejected() {
        SensorFrameCollector collector = new SensorFrameCollector(GameConfig.defaults());

        assertThrows(IllegalArgumentException.class, () -> collector.acceptGps(null));
        assertThrows(IllegalArgumentException.class, () -> collector.acceptCompass(null));
        assertThrows(IllegalArgumentException.class, () -> collector.acceptNoise(null));
        assertThrows(IllegalArgumentException.class, () -> collector.acceptAccelerometer(null));
    }

    private static GpsSample gps(double latitude, double longitude, double accuracyM,
                                 long timestampNanos) {
        return new GpsSample(new LatLon(latitude, longitude), accuracyM, timestampNanos);
    }

    private static CompassSample compass(double azimuthDeg, CompassAccuracy accuracy,
                                         long timestampNanos) {
        return new CompassSample(azimuthDeg, accuracy, timestampNanos);
    }
}
