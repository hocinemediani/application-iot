package com.example.ghostradar.sensors.android;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.example.ghostradar.domain.config.GameConfig;
import com.example.ghostradar.domain.sensors.SensorFrame;
import com.example.ghostradar.sensors.AccelerometerSensor;
import com.example.ghostradar.sensors.CompassSensor;
import com.example.ghostradar.sensors.GpsSensor;
import com.example.ghostradar.sensors.MicrophoneSensor;
import com.example.ghostradar.sensors.SensorError;
import com.example.ghostradar.sensors.SensorFrameCollector;
import com.example.ghostradar.sensors.SensorFrameListener;
import com.example.ghostradar.sensors.SensorListener;
import com.example.ghostradar.sensors.SensorSource;

import java.util.function.Consumer;

/**
 * Orchestrateur Android des quatre capteurs.
 *
 * <p>Les capteurs poussent leurs mesures brutes dans un
 * {@link SensorFrameCollector} pur, puis le dernier frame est publié au
 * listener sur le thread principal à chaque tick de {@code GameConfig}
 * (100 ms par défaut).</p>
 */
public final class AndroidSensorFrameCollector {

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final SensorFrameCollector collector;
    private final GpsSensor gpsSensor;
    private final CompassSensor compassSensor;
    private final MicrophoneSensor microphoneSensor;
    private final AccelerometerSensor accelerometerSensor;
    private final int tickMs;

    private SensorFrameListener listener;
    private boolean running;

    private final Runnable tickRunnable = new Runnable() {
        @Override
        public void run() {
            if (!running) {
                return;
            }
            SensorFrameListener current = listener;
            if (current != null) {
                current.onFrame(collector.getFrame());
            }
            handler.postDelayed(this, tickMs);
        }
    };

    public AndroidSensorFrameCollector(Context context, GameConfig config) {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null");
        }
        if (config == null) {
            throw new IllegalArgumentException("config must not be null");
        }
        if (config.getTickMs() <= 0) {
            throw new IllegalArgumentException("tickMs must be positive");
        }
        this.tickMs = config.getTickMs();
        this.collector = new SensorFrameCollector(config);
        this.gpsSensor = new AndroidGpsSensor(context, config);
        this.compassSensor = new AndroidCompassSensor(context, config);
        this.microphoneSensor = new AndroidMicrophoneSensor(context);
        this.accelerometerSensor = new AndroidAccelerometerSensor(context, config);
    }

    public void start(SensorFrameListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener must not be null");
        }
        if (running) {
            this.listener = listener;
            return;
        }

        this.listener = listener;
        running = true;
        handler.postDelayed(tickRunnable, tickMs);

        startSensor(gpsSensor, collector::acceptGps);
        startSensor(compassSensor, collector::acceptCompass);
        startSensor(microphoneSensor, collector::acceptNoise);
        startSensor(accelerometerSensor, collector::acceptAccelerometer);
    }

    public void stop() {
        if (!running) {
            return;
        }
        running = false;
        handler.removeCallbacks(tickRunnable);
        gpsSensor.stop();
        compassSensor.stop();
        microphoneSensor.stop();
        accelerometerSensor.stop();
        listener = null;
    }

    public boolean isRunning() {
        return running;
    }

    public SensorFrame getFrame() {
        return collector.getFrame();
    }

    public boolean isNoiseAlertActive() {
        return collector.isNoiseAlertActive();
    }

    public double getNoiseGaugeLevel() {
        return collector.getNoiseGaugeLevel();
    }

    public double getSmoothedNoiseDbfs() {
        return collector.getSmoothedNoiseDbfs();
    }

    private <T> void startSensor(SensorSource<T> sensor, Consumer<T> consumer) {
        sensor.start(new SensorListener<T>() {
            @Override
            public void onSample(T sample) {
                consumer.accept(sample);
            }

            @Override
            public void onUnavailable(SensorError error) {
                notifyUnavailable(error);
            }
        });
    }

    private void notifyUnavailable(SensorError error) {
        handler.post(() -> {
            SensorFrameListener current = listener;
            if (current != null) {
                current.onUnavailable(error);
            }
        });
    }
}
