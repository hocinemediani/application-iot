package com.example.ghostradar.ui;

import com.example.ghostradar.domain.config.GameConfig;
import com.example.ghostradar.domain.game.RandomSource;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class CameraGlitchControllerTest {

    @Test
    public void triggerSpawnStartsGlitchWithSixBoundedBands() {
        CameraGlitchController controller = newController(GameConfig.defaults());

        assertTrue(controller.triggerSpawn());
        assertTrue(controller.isActive());
        assertEquals(500, controller.getRemainingMs());

        List<GlitchBand> bands = controller.getBands();
        assertEquals(CameraGlitchStyle.BAND_COUNT, bands.size());
        for (GlitchBand band : bands) {
            assertTrue(band.getTopRatio() >= 0.0 && band.getTopRatio() < 1.0);
            assertTrue(band.getHeightPx() >= CameraGlitchStyle.BAND_MIN_HEIGHT_PX);
            assertTrue(band.getHeightPx() <= CameraGlitchStyle.BAND_MAX_HEIGHT_PX);
        }
    }

    @Test
    public void disabledTriggerDoesNothing() {
        GameConfig config = new GameConfig.Builder()
                .setCameraGlitchOnSpawn(false)
                .setCameraGlitchOnCapture(false)
                .build();
        CameraGlitchController controller = newController(config);

        assertFalse(controller.triggerSpawn());
        assertFalse(controller.triggerCapture());
        assertFalse(controller.isActive());
        assertTrue(controller.getBands().isEmpty());
    }

    @Test
    public void disabledCaptureDoesNotStopAnActiveSpawnGlitch() {
        GameConfig config = new GameConfig.Builder()
                .setCameraGlitchOnCapture(false)
                .build();
        CameraGlitchController controller = newController(config);
        controller.triggerSpawn();

        assertFalse(controller.triggerCapture());
        assertTrue(controller.isActive());
    }

    @Test
    public void advanceKeepsGlitchActiveUntilExactDuration() {
        CameraGlitchController controller = newController(GameConfig.defaults());
        controller.triggerSpawn();

        controller.advance(499);
        assertTrue(controller.isActive());
        assertEquals(1, controller.getRemainingMs());

        controller.advance(1);
        assertFalse(controller.isActive());
        assertEquals(0, controller.getRemainingMs());
        assertTrue(controller.getBands().isEmpty());
    }

    @Test
    public void configuredDurationIsUsed() {
        GameConfig config = new GameConfig.Builder()
                .setCameraGlitchDurationMs(250)
                .build();
        CameraGlitchController controller = newController(config);
        controller.triggerCapture();

        assertEquals(250, controller.getRemainingMs());
        controller.advance(250);
        assertFalse(controller.isActive());
    }

    @Test
    public void bandsRefreshEveryFiftyMilliseconds() {
        IncrementingRandomSource random = new IncrementingRandomSource();
        CameraGlitchController controller =
                new CameraGlitchController(GameConfig.defaults(), random);
        controller.triggerSpawn();
        List<GlitchBand> initial = controller.getBands();

        controller.advance(49);
        assertEquals(initial, controller.getBands());

        controller.advance(1);
        assertNotEquals(initial, controller.getBands());
    }

    @Test
    public void retriggerResetsDurationAndBands() {
        IncrementingRandomSource random = new IncrementingRandomSource();
        CameraGlitchController controller =
                new CameraGlitchController(GameConfig.defaults(), random);
        controller.triggerSpawn();
        List<GlitchBand> initial = controller.getBands();

        controller.advance(200);
        assertTrue(controller.triggerCapture());

        assertEquals(500, controller.getRemainingMs());
        assertNotEquals(initial, controller.getBands());
    }

    @Test
    public void stopClearsState() {
        CameraGlitchController controller = newController(GameConfig.defaults());
        controller.triggerSpawn();

        controller.stop();

        assertFalse(controller.isActive());
        assertEquals(0, controller.getRemainingMs());
        assertTrue(controller.getBands().isEmpty());
    }

    @Test
    public void sameSeedProducesSameBands() {
        CameraGlitchController first =
                new CameraGlitchController(GameConfig.defaults(), RandomSource.seeded(42L));
        CameraGlitchController second =
                new CameraGlitchController(GameConfig.defaults(), RandomSource.seeded(42L));

        first.triggerSpawn();
        second.triggerSpawn();

        assertEquals(first.getBands(), second.getBands());
    }

    @Test
    public void rejectsNegativeAdvance() {
        CameraGlitchController controller = newController(GameConfig.defaults());
        try {
            controller.advance(-1);
            fail("delta must not be negative");
        } catch (IllegalArgumentException expected) {
            // Attendu.
        }
    }

    @Test
    public void rejectsNullDependenciesAndInvalidDuration() {
        RandomSource random = RandomSource.seeded(1L);
        try {
            new CameraGlitchController(null, random);
            fail("config must not be null");
        } catch (IllegalArgumentException expected) {
            // Attendu.
        }
        try {
            new CameraGlitchController(GameConfig.defaults(), null);
            fail("random must not be null");
        } catch (IllegalArgumentException expected) {
            // Attendu.
        }
        // Une durée non positive est désormais refusée par GameConfig.build()
        // (validation centralisée) et est testée dans GameConfigTest ; le
        // contrôleur n'a plus besoin de la re-valider.
    }

    private static CameraGlitchController newController(GameConfig config) {
        return new CameraGlitchController(config, RandomSource.seeded(7L));
    }

    /** Source déterministe qui garantit des bandes différentes à chaque rafraîchissement. */
    private static final class IncrementingRandomSource implements RandomSource {

        private int counter;

        @Override
        public double nextDouble() {
            return ((counter++ % 9) + 1) / 10.0;
        }

        @Override
        public int nextInt(int boundExclusive) {
            return counter++ % boundExclusive;
        }
    }
}
