package com.example.ghostradar.domain.item;

import com.example.ghostradar.domain.config.GameConfig;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class BeamProfileTest {

    private static final double EPSILON = 1e-9;

    @Test
    public void defaultsStartAtLevelOne() {
        BeamProfile beam = new BeamProfile(GameConfig.defaults());

        assertEquals(1, beam.getLevel());
        assertEquals(5.0, beam.getRequiredSeconds(), EPSILON);
        assertEquals(12.0, beam.getAngleToleranceDeg(), EPSILON);
    }

    @Test
    public void levelsMatchSpecTable() {
        BeamProfile beam = new BeamProfile(GameConfig.defaults());
        double[] requiredSeconds = {5.0, 4.5, 4.0, 3.5, 3.0};
        double[] toleranceDeg = {12.0, 12.0, 14.0, 14.0, 16.0};

        for (int index = 0; index < requiredSeconds.length; index++) {
            assertEquals(index + 1, beam.getLevel());
            assertEquals(requiredSeconds[index], beam.getRequiredSeconds(), EPSILON);
            assertEquals(toleranceDeg[index], beam.getAngleToleranceDeg(), EPSILON);
            if (index < requiredSeconds.length - 1) {
                assertTrue(beam.upgrade());
            }
        }
    }

    @Test
    public void upgradeAtMaxLevelIsRejected() {
        GameConfig config = new GameConfig.Builder()
                .setBeamMaxLevel(2)
                .build();
        BeamProfile beam = new BeamProfile(config);

        assertTrue(beam.upgrade());
        assertEquals(2, beam.getLevel());
        assertEquals(4.5, beam.getRequiredSeconds(), EPSILON);

        assertFalse(beam.upgrade());
        assertEquals(2, beam.getLevel());
        assertEquals(4.5, beam.getRequiredSeconds(), EPSILON);
    }

    @Test
    public void requiredSecondsNeverDropsBelowMinimum() {
        GameConfig config = new GameConfig.Builder()
                .setBeamBaseRequiredSeconds(5.0)
                .setBeamTimeReductionPerLevelS(2.0)
                .setBeamMinRequiredSeconds(2.0)
                .setBeamMaxLevel(5)
                .build();
        BeamProfile beam = new BeamProfile(config);

        assertEquals(5.0, beam.getRequiredSeconds(), EPSILON);
        assertTrue(beam.upgrade());
        assertEquals(3.0, beam.getRequiredSeconds(), EPSILON);
        assertTrue(beam.upgrade());
        assertEquals(2.0, beam.getRequiredSeconds(), EPSILON);
        assertTrue(beam.upgrade());
        assertEquals(2.0, beam.getRequiredSeconds(), EPSILON);
        assertTrue(beam.upgrade());
        assertEquals(2.0, beam.getRequiredSeconds(), EPSILON);
    }

    @Test
    public void toleranceUsesConfiguredStepAndInterval() {
        GameConfig config = new GameConfig.Builder()
                .setBeamBaseAngleToleranceDeg(10.0)
                .setBeamAngleToleranceStepDeg(3.0)
                .setBeamLevelsPerToleranceStep(2)
                .setBeamMaxLevel(5)
                .build();
        BeamProfile beam = new BeamProfile(config);

        assertEquals(10.0, beam.getAngleToleranceDeg(), EPSILON);
        assertTrue(beam.upgrade());
        assertEquals(10.0, beam.getAngleToleranceDeg(), EPSILON);
        assertTrue(beam.upgrade());
        assertEquals(13.0, beam.getAngleToleranceDeg(), EPSILON);
        assertTrue(beam.upgrade());
        assertEquals(13.0, beam.getAngleToleranceDeg(), EPSILON);
        assertTrue(beam.upgrade());
        assertEquals(16.0, beam.getAngleToleranceDeg(), EPSILON);
    }

    @Test
    public void rejectsInvalidArguments() {
        assertThrows(IllegalArgumentException.class, () -> new BeamProfile(null));
    }

    @Test
    public void rejectsInvalidConfig() {
        assertThrows(IllegalArgumentException.class,
                () -> new BeamProfile(new GameConfig.Builder()
                        .setBeamMaxLevel(0)
                        .build()));
        assertThrows(IllegalArgumentException.class,
                () -> new BeamProfile(new GameConfig.Builder()
                        .setBeamBaseRequiredSeconds(0.0)
                        .build()));
        assertThrows(IllegalArgumentException.class,
                () -> new BeamProfile(new GameConfig.Builder()
                        .setBeamTimeReductionPerLevelS(-0.1)
                        .build()));
        assertThrows(IllegalArgumentException.class,
                () -> new BeamProfile(new GameConfig.Builder()
                        .setBeamMinRequiredSeconds(0.0)
                        .build()));
        assertThrows(IllegalArgumentException.class,
                () -> new BeamProfile(new GameConfig.Builder()
                        .setBeamBaseAngleToleranceDeg(0.0)
                        .build()));
        assertThrows(IllegalArgumentException.class,
                () -> new BeamProfile(new GameConfig.Builder()
                        .setBeamAngleToleranceStepDeg(0.0)
                        .build()));
        assertThrows(IllegalArgumentException.class,
                () -> new BeamProfile(new GameConfig.Builder()
                        .setBeamLevelsPerToleranceStep(0)
                        .build()));
    }
}
