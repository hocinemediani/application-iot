package com.example.ghostradar.sensors;

import com.example.ghostradar.domain.sensors.NoiseSample;

/**
 * Source de blocs RMS pour la mesure du bruit ambiant.
 */
public interface MicrophoneSensor extends SensorSource<NoiseSample> {

    int SAMPLE_RATE_HZ = 44_100;
    int BLOCK_DURATION_MS = 20;
    int BLOCK_FRAMES = SAMPLE_RATE_HZ * BLOCK_DURATION_MS / 1_000;
}
