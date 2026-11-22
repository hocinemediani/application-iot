package com.example.ghostradar.sensors;

import com.example.ghostradar.domain.ai.NoiseMonitor;
import com.example.ghostradar.domain.config.GameConfig;
import com.example.ghostradar.domain.sensors.AccelerometerSample;
import com.example.ghostradar.domain.sensors.CompassHeadingFilter;
import com.example.ghostradar.domain.sensors.CompassSample;
import com.example.ghostradar.domain.sensors.GpsFixFilter;
import com.example.ghostradar.domain.sensors.GpsSample;
import com.example.ghostradar.domain.sensors.NoiseSample;
import com.example.ghostradar.domain.sensors.SensorFrame;
import com.example.ghostradar.domain.sensors.TiltCalculator;
import com.example.ghostradar.domain.sensors.TiltSample;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Agrège les mesures brutes des capteurs dans un {@link SensorFrame} immuable.
 *
 * <p>Cette classe est pure Java : elle applique les filtres du domaine et
 * publie un frame cohérent, sans dépendre du framework Android. Les méthodes
 * {@code acceptX} sont appelées depuis les threads de réception des capteurs ;
 * le frame est mis à jour atomiquement et le {@link NoiseMonitor}, non
 * thread-safe, est protégé par un verrou interne.</p>
 */
public final class SensorFrameCollector {

    private final GpsFixFilter gpsFilter;
    private final CompassHeadingFilter compassFilter;
    private final TiltCalculator tiltCalculator;
    private final NoiseMonitor noiseMonitor;
    private final Object noiseLock = new Object();
    private final AtomicReference<SensorFrame> frame = new AtomicReference<>(SensorFrame.EMPTY);

    public SensorFrameCollector(GameConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("config must not be null");
        }
        this.gpsFilter = new GpsFixFilter(config);
        this.compassFilter = new CompassHeadingFilter(config);
        this.tiltCalculator = new TiltCalculator(config);
        this.noiseMonitor = new NoiseMonitor(config);
    }

    /**
     * Filtre puis intègre un fix GPS brut.
     *
     * @return {@code true} si le fix a été accepté et publié
     */
    public boolean acceptGps(GpsSample sample) {
        Optional<GpsSample> accepted = gpsFilter.accept(sample);
        if (!accepted.isPresent()) {
            return false;
        }
        GpsSample filtered = accepted.get();
        frame.updateAndGet(current -> current.withGps(filtered));
        return true;
    }

    /**
     * Filtre puis intègre une mesure de boussole brute.
     *
     * @return {@code true} si la mesure a été acceptée et publiée
     */
    public boolean acceptCompass(CompassSample sample) {
        Optional<CompassSample> accepted = compassFilter.accept(sample);
        if (!accepted.isPresent()) {
            return false;
        }
        CompassSample filtered = accepted.get();
        frame.updateAndGet(current -> current.withCompass(filtered));
        return true;
    }

    /**
     * Intègre un bloc audio et met à jour l'alerte de bruit.
     */
    public void acceptNoise(NoiseSample sample) {
        if (sample == null) {
            throw new IllegalArgumentException("sample must not be null");
        }
        synchronized (noiseLock) {
            noiseMonitor.update(sample.getRmsNormalized(), sample.getDurationSeconds());
        }
        frame.updateAndGet(current -> current.withNoise(sample));
    }

    /**
     * Intègre une mesure d'accéléromètre et recalcule le tilt filtré.
     */
    public void acceptAccelerometer(AccelerometerSample sample) {
        if (sample == null) {
            throw new IllegalArgumentException("sample must not be null");
        }
        TiltSample tilt = tiltCalculator.calculate(sample);
        frame.updateAndGet(current -> current.withAccelerometer(sample).withTilt(tilt));
    }

    /**
     * Dernier frame publié. Jamais {@code null}.
     */
    public SensorFrame getFrame() {
        return frame.get();
    }

    public boolean isNoiseAlertActive() {
        synchronized (noiseLock) {
            return noiseMonitor.isAlertActive();
        }
    }

    public double getNoiseGaugeLevel() {
        synchronized (noiseLock) {
            return noiseMonitor.getGaugeLevel();
        }
    }

    public double getSmoothedNoiseDbfs() {
        synchronized (noiseLock) {
            return noiseMonitor.getSmoothedDbfs();
        }
    }
}
