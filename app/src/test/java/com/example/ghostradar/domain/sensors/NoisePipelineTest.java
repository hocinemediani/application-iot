package com.example.ghostradar.domain.sensors;

import com.example.ghostradar.domain.ai.NoiseMonitor;
import com.example.ghostradar.domain.config.GameConfig;
import com.example.ghostradar.sensors.MicrophoneSensor;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class NoisePipelineTest {

    private static final double BLOCK_DURATION_S = MicrophoneSensor.BLOCK_DURATION_MS / 1_000.0;

    @Test
    public void fullScaleBlocksReachZeroDbfs() {
        NoiseMonitor monitor = new NoiseMonitor(GameConfig.defaults());
        short[] block = block(Short.MAX_VALUE);

        feed(monitor, block, 10);

        assertEquals(0.0, monitor.getSmoothedDbfs(), 0.01);
    }

    @Test
    public void silenceBlocksStayAtGaugeFloor() {
        NoiseMonitor monitor = new NoiseMonitor(GameConfig.defaults());
        short[] block = new short[MicrophoneSensor.BLOCK_FRAMES];

        feed(monitor, block, 10);

        assertEquals(-60.0, monitor.getSmoothedDbfs(), 1e-9);
    }

    @Test
    public void halfScaleBlocksGiveMinusSixDbfs() {
        NoiseMonitor monitor = new NoiseMonitor(GameConfig.defaults());
        short[] block = block((short) 16_384);

        feed(monitor, block, 10);

        assertEquals(10.0 * Math.log10(0.25), monitor.getSmoothedDbfs(), 0.01);
    }

    @Test
    public void loudBlocksArmAlertAfterHysteresis() {
        NoiseMonitor monitor = new NoiseMonitor(GameConfig.defaults());
        short[] block = block(Short.MAX_VALUE);

        feed(monitor, block, 100);

        assertTrue(monitor.isAlertActive());
    }

    private static void feed(NoiseMonitor monitor, short[] block, int blocks) {
        for (int i = 0; i < blocks; i++) {
            double rms = RmsMeter.computeRmsNormalized(block);
            monitor.update(rms, BLOCK_DURATION_S);
        }
    }

    private static short[] block(short value) {
        short[] block = new short[MicrophoneSensor.BLOCK_FRAMES];
        Arrays.fill(block, value);
        return block;
    }
}
