package com.example.ghostradar.ui;

import com.example.ghostradar.domain.config.GameConfig;
import com.example.ghostradar.domain.game.RandomSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Machine à états pure du glitch caméra.
 *
 * <p>Les hooks explicites {@link #triggerSpawn()} et {@link #triggerCapture()}
 * respectent les flags de {@link GameConfig}. La vue Android ne fait que lire
 * {@link #getBands()} et appeler {@link #advance(long)}.</p>
 */
public final class CameraGlitchController {

    private static final List<GlitchBand> NO_BANDS = Collections.emptyList();

    private final int durationMs;
    private final boolean spawnEnabled;
    private final boolean captureEnabled;
    private final RandomSource random;

    private boolean active;
    private long elapsedMs;
    private long sinceRefreshMs;
    private List<GlitchBand> bands = NO_BANDS;

    public CameraGlitchController(GameConfig config, RandomSource random) {
        if (config == null) {
            throw new IllegalArgumentException("config must not be null");
        }
        if (random == null) {
            throw new IllegalArgumentException("random must not be null");
        }
        this.durationMs = config.getCameraGlitchDurationMs();
        if (durationMs <= 0) {
            throw new IllegalArgumentException("duration must be positive: " + durationMs);
        }
        this.spawnEnabled = config.isCameraGlitchOnSpawn();
        this.captureEnabled = config.isCameraGlitchOnCapture();
        this.random = random;
    }

    /**
     * @return {@code true} si le glitch a été déclenché, {@code false} si le flag est désactivé.
     */
    public boolean triggerSpawn() {
        return trigger(spawnEnabled);
    }

    /**
     * @return {@code true} si le glitch a été déclenché, {@code false} si le flag est désactivé.
     */
    public boolean triggerCapture() {
        return trigger(captureEnabled);
    }

    private boolean trigger(boolean enabled) {
        if (!enabled) {
            return false;
        }
        active = true;
        elapsedMs = 0;
        sinceRefreshMs = 0;
        bands = generateBands();
        return true;
    }

    /**
     * Fait progresser le glitch. Un dépassement de {@code REFRESH_INTERVAL_MS}
     * régénère les bandes une fois ; un dépassement de la durée l'arrête.
     */
    public void advance(long deltaMs) {
        if (deltaMs < 0) {
            throw new IllegalArgumentException("delta must not be negative: " + deltaMs);
        }
        if (!active || deltaMs == 0) {
            return;
        }
        elapsedMs += deltaMs;
        if (elapsedMs >= durationMs) {
            stop();
            return;
        }
        sinceRefreshMs += deltaMs;
        if (sinceRefreshMs >= CameraGlitchStyle.REFRESH_INTERVAL_MS) {
            bands = generateBands();
            sinceRefreshMs %= CameraGlitchStyle.REFRESH_INTERVAL_MS;
        }
    }

    public void stop() {
        active = false;
        elapsedMs = 0;
        sinceRefreshMs = 0;
        bands = NO_BANDS;
    }

    public boolean isActive() {
        return active;
    }

    public long getRemainingMs() {
        return active ? durationMs - elapsedMs : 0;
    }

    /**
     * @return liste immuable des bandes courantes, vide hors glitch.
     */
    public List<GlitchBand> getBands() {
        return bands;
    }

    private List<GlitchBand> generateBands() {
        List<GlitchBand> generated = new ArrayList<>(CameraGlitchStyle.BAND_COUNT);
        int heightRange = CameraGlitchStyle.BAND_MAX_HEIGHT_PX
                - CameraGlitchStyle.BAND_MIN_HEIGHT_PX + 1;
        for (int i = 0; i < CameraGlitchStyle.BAND_COUNT; i++) {
            double topRatio = random.nextDouble();
            int heightPx = CameraGlitchStyle.BAND_MIN_HEIGHT_PX + random.nextInt(heightRange);
            generated.add(new GlitchBand(topRatio, heightPx));
        }
        return Collections.unmodifiableList(generated);
    }
}
