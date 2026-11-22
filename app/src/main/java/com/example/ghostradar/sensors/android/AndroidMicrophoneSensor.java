package com.example.ghostradar.sensors.android;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import androidx.core.content.ContextCompat;

import com.example.ghostradar.domain.sensors.NoiseSample;
import com.example.ghostradar.domain.sensors.RmsMeter;
import com.example.ghostradar.sensors.BaseSensorSource;
import com.example.ghostradar.sensors.MicrophoneSensor;
import com.example.ghostradar.sensors.SensorError;

/**
 * Microphone Android : 44,1 kHz, mono, PCM 16 bits, blocs de 20 ms.
 *
 * <p>Chaque bloc produit un {@link NoiseSample} RMS normalisé et sa durée,
 * consommés ensuite par {@code NoiseMonitor}.</p>
 */
public final class AndroidMicrophoneSensor extends BaseSensorSource<NoiseSample>
        implements MicrophoneSensor {

    private static final int BYTES_PER_SAMPLE = 2;
    private static final int MIN_BUFFER_BLOCKS = 2;

    private final Context context;
    private AudioRecord audioRecord;
    private Thread readerThread;
    private volatile boolean active;

    public AndroidMicrophoneSensor(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null");
        }
        this.context = context.getApplicationContext();
    }

    @Override
    protected SensorError onStart() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            return SensorError.PERMISSION_DENIED;
        }

        int minBufferBytes = AudioRecord.getMinBufferSize(
                SAMPLE_RATE_HZ, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        if (minBufferBytes <= 0) {
            return SensorError.AUDIO_INIT_FAILED;
        }

        int blockBytes = BLOCK_FRAMES * BYTES_PER_SAMPLE;
        int bufferBytes = Math.max(minBufferBytes, blockBytes * MIN_BUFFER_BLOCKS);

        try {
            audioRecord = new AudioRecord.Builder()
                    .setAudioSource(MediaRecorder.AudioSource.MIC)
                    .setAudioFormat(new AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(SAMPLE_RATE_HZ)
                            .setChannelMask(AudioFormat.CHANNEL_IN_MONO)
                            .build())
                    .setBufferSizeInBytes(bufferBytes)
                    .build();
        } catch (RuntimeException exception) {
            releaseAudioRecord();
            return SensorError.AUDIO_INIT_FAILED;
        }

        if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
            releaseAudioRecord();
            return SensorError.AUDIO_INIT_FAILED;
        }

        try {
            audioRecord.startRecording();
        } catch (RuntimeException exception) {
            releaseAudioRecord();
            return SensorError.AUDIO_INIT_FAILED;
        }

        if (audioRecord.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING) {
            releaseAudioRecord();
            return SensorError.AUDIO_INIT_FAILED;
        }

        active = true;
        readerThread = new Thread(this::readLoop, "ghost-radar-microphone");
        readerThread.start();
        return null;
    }

    @Override
    protected void onStop() {
        active = false;
        if (audioRecord != null) {
            try {
                audioRecord.stop();
            } catch (IllegalStateException exception) {
                // Déjà arrêté : la libération ci-dessous suffit.
            }
        }
        if (readerThread != null) {
            try {
                readerThread.join(500L);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
            readerThread = null;
        }
        releaseAudioRecord();
    }

    private void readLoop() {
        AudioRecord record = audioRecord;
        if (record == null) {
            return;
        }
        short[] block = new short[BLOCK_FRAMES];
        while (active) {
            int read;
            try {
                read = record.read(block, 0, block.length);
            } catch (RuntimeException exception) {
                break;
            }
            if (read > 0) {
                double rms = RmsMeter.computeRmsNormalized(block, 0, read);
                double durationMs = read * 1_000.0 / SAMPLE_RATE_HZ;
                emit(new NoiseSample(rms, durationMs, System.nanoTime()));
            } else if (read < 0) {
                break;
            }
        }
    }

    private void releaseAudioRecord() {
        if (audioRecord != null) {
            audioRecord.release();
            audioRecord = null;
        }
    }
}
