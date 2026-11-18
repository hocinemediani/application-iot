package com.example.ghostradar.domain.ai;

import com.example.ghostradar.domain.config.GameConfig;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class NoiseMonitorTest {

    private static final double TICK_S = 0.1;
    private static final double LOUD_RMS = 1.0;
    private static final double SILENT_RMS = 0.0;
    private static final double THRESHOLD_RMS = Math.pow(10.0, -30.0 / 20.0);

    @Test
    public void startsWithoutAlertAtGaugeFloor() {
        NoiseMonitor monitor = new NoiseMonitor(GameConfig.defaults());

        assertFalse(monitor.isAlertActive());
        assertEquals(-60.0, monitor.getSmoothedDbfs(), 0.0);
        assertEquals(0.0, monitor.getGaugeLevel(), 0.0);
    }

    @Test
    public void firstSampleInitializesSmoothing() {
        NoiseMonitor monitor = new NoiseMonitor(config(0.2, 100, 2.0, 3.0));

        monitor.update(LOUD_RMS, TICK_S);

        assertEquals(0.0, monitor.getSmoothedDbfs(), 1e-9);
        assertEquals(1.0, monitor.getGaugeLevel(), 1e-9);
    }

    @Test
    public void averagingWindowCoversConfiguredDuration() {
        NoiseMonitor monitor = new NoiseMonitor(config(1.0, 200, 2.0, 3.0));

        monitor.update(SILENT_RMS, TICK_S);
        assertEquals(-60.0, monitor.getSmoothedDbfs(), 1e-9);

        monitor.update(LOUD_RMS, TICK_S);
        assertEquals(10.0 * Math.log10(0.5), monitor.getSmoothedDbfs(), 1e-9);

        monitor.update(LOUD_RMS, TICK_S);
        assertEquals(0.0, monitor.getSmoothedDbfs(), 1e-9);

        monitor.update(SILENT_RMS, TICK_S);
        assertEquals(10.0 * Math.log10(0.5), monitor.getSmoothedDbfs(), 1e-9);
    }

    @Test
    public void windowTrimsPartiallyForNonAlignedChunks() {
        NoiseMonitor monitor = new NoiseMonitor(config(1.0, 200, 2.0, 3.0));

        monitor.update(LOUD_RMS, 0.15);
        assertEquals(0.0, monitor.getSmoothedDbfs(), 1e-9);

        monitor.update(SILENT_RMS, 0.15);
        assertEquals(10.0 * Math.log10(0.25), monitor.getSmoothedDbfs(), 1e-9);
    }

    @Test
    public void smoothingAppliesExponentialAlpha() {
        NoiseMonitor monitor = new NoiseMonitor(config(0.5, 100, 2.0, 3.0));

        monitor.update(LOUD_RMS, TICK_S);
        assertEquals(0.0, monitor.getSmoothedDbfs(), 1e-9);

        monitor.update(SILENT_RMS, TICK_S);
        assertEquals(-30.0, monitor.getSmoothedDbfs(), 1e-9);

        monitor.update(SILENT_RMS, TICK_S);
        assertEquals(-45.0, monitor.getSmoothedDbfs(), 1e-9);
    }

    @Test
    public void gaugeMapsDbRangeAndClamps() {
        NoiseMonitor loud = new NoiseMonitor(config(1.0, 100, 2.0, 3.0));
        NoiseMonitor silent = new NoiseMonitor(config(1.0, 100, 2.0, 3.0));
        NoiseMonitor threshold = new NoiseMonitor(config(1.0, 100, 2.0, 3.0));

        loud.update(LOUD_RMS, TICK_S);
        silent.update(SILENT_RMS, TICK_S);
        threshold.update(THRESHOLD_RMS, TICK_S);

        assertEquals(1.0, loud.getGaugeLevel(), 1e-9);
        assertEquals(0.0, silent.getGaugeLevel(), 0.0);
        assertEquals(0.5, threshold.getGaugeLevel(), 1e-9);
        assertEquals(-30.0, threshold.getSmoothedDbfs(), 1e-9);
    }

    @Test
    public void alertRequiresContinuousHysteresisAboveThreshold() {
        NoiseMonitor monitor = new NoiseMonitor(config(1.0, 100, 2.0, 3.0));

        feed(monitor, LOUD_RMS, 1.9);
        assertFalse(monitor.isAlertActive());

        feed(monitor, LOUD_RMS, 0.1);
        assertTrue(monitor.isAlertActive());
    }

    @Test
    public void aboveThresholdTimerResetsWhenSignalDrops() {
        NoiseMonitor monitor = new NoiseMonitor(config(1.0, 100, 2.0, 3.0));

        feed(monitor, LOUD_RMS, 1.0);
        feed(monitor, SILENT_RMS, 0.1);
        feed(monitor, LOUD_RMS, 1.0);

        assertFalse(monitor.isAlertActive());

        feed(monitor, LOUD_RMS, 1.0);
        assertTrue(monitor.isAlertActive());
    }

    @Test
    public void alertHoldsForConfiguredDurationBelowThreshold() {
        NoiseMonitor monitor = new NoiseMonitor(config(1.0, 100, 2.0, 3.0));
        feed(monitor, LOUD_RMS, 2.0);
        assertTrue(monitor.isAlertActive());

        feed(monitor, SILENT_RMS, 2.9);
        assertTrue(monitor.isAlertActive());

        feed(monitor, SILENT_RMS, 0.1);
        assertFalse(monitor.isAlertActive());
    }

    @Test
    public void aboveThresholdDuringHoldResetsReleaseTimer() {
        NoiseMonitor monitor = new NoiseMonitor(config(1.0, 100, 2.0, 3.0));
        feed(monitor, LOUD_RMS, 2.0);

        feed(monitor, SILENT_RMS, 2.5);
        assertTrue(monitor.isAlertActive());

        feed(monitor, LOUD_RMS, 0.1);
        assertTrue(monitor.isAlertActive());

        feed(monitor, SILENT_RMS, 2.5);
        assertTrue(monitor.isAlertActive());

        feed(monitor, SILENT_RMS, 0.5);
        assertFalse(monitor.isAlertActive());
    }

    @Test
    public void requiresNewHysteresisAfterAlertClears() {
        NoiseMonitor monitor = new NoiseMonitor(config(1.0, 100, 2.0, 3.0));
        feed(monitor, LOUD_RMS, 2.0);
        feed(monitor, SILENT_RMS, 3.0);
        assertFalse(monitor.isAlertActive());

        feed(monitor, LOUD_RMS, 1.9);
        assertFalse(monitor.isAlertActive());

        feed(monitor, LOUD_RMS, 0.1);
        assertTrue(monitor.isAlertActive());
    }

    @Test
    public void zeroDeltaIsIgnored() {
        NoiseMonitor monitor = new NoiseMonitor(config(1.0, 100, 2.0, 3.0));
        feed(monitor, LOUD_RMS, 2.0);
        double smoothedBefore = monitor.getSmoothedDbfs();
        boolean alertBefore = monitor.isAlertActive();

        monitor.update(SILENT_RMS, 0.0);

        assertEquals(smoothedBefore, monitor.getSmoothedDbfs(), 0.0);
        assertEquals(alertBefore, monitor.isAlertActive());
    }

    @Test
    public void rejectsInvalidArguments() {
        NoiseMonitor monitor = new NoiseMonitor(GameConfig.defaults());

        assertThrows(IllegalArgumentException.class,
                () -> new NoiseMonitor(null));
        assertThrows(IllegalArgumentException.class,
                () -> monitor.update(-0.1, TICK_S));
        assertThrows(IllegalArgumentException.class,
                () -> monitor.update(1.1, TICK_S));
        assertThrows(IllegalArgumentException.class,
                () -> monitor.update(Double.NaN, TICK_S));
        assertThrows(IllegalArgumentException.class,
                () -> monitor.update(0.5, -0.1));
        assertThrows(IllegalArgumentException.class,
                () -> monitor.update(0.5, Double.NaN));
    }

    @Test
    public void rejectsInvalidConfig() {
        assertThrows(IllegalArgumentException.class,
                () -> new NoiseMonitor(new GameConfig.Builder()
                        .setNoiseWindowMs(0)
                        .build()));
        assertThrows(IllegalArgumentException.class,
                () -> new NoiseMonitor(new GameConfig.Builder()
                        .setNoiseSmoothingAlpha(-0.1)
                        .build()));
        assertThrows(IllegalArgumentException.class,
                () -> new NoiseMonitor(new GameConfig.Builder()
                        .setNoiseSmoothingAlpha(1.1)
                        .build()));
        assertThrows(IllegalArgumentException.class,
                () -> new NoiseMonitor(new GameConfig.Builder()
                        .setNoiseGaugeMinDbfs(0.0)
                        .build()));
        assertThrows(IllegalArgumentException.class,
                () -> new NoiseMonitor(new GameConfig.Builder()
                        .setNoiseHysteresisS(-1.0)
                        .build()));
        assertThrows(IllegalArgumentException.class,
                () -> new NoiseMonitor(new GameConfig.Builder()
                        .setNoiseAlertHoldS(-1.0)
                        .build()));
    }

    private static void feed(NoiseMonitor monitor, double rms, double totalSeconds) {
        int steps = (int) Math.round(totalSeconds / TICK_S);
        for (int step = 0; step < steps; step++) {
            monitor.update(rms, TICK_S);
        }
    }

    private static GameConfig config(double alpha, int windowMs,
                                     double hysteresisS, double alertHoldS) {
        return new GameConfig.Builder()
                .setNoiseSmoothingAlpha(alpha)
                .setNoiseWindowMs(windowMs)
                .setNoiseHysteresisS(hysteresisS)
                .setNoiseAlertHoldS(alertHoldS)
                .build();
    }
}
