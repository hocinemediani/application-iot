package com.example.ghostradar.ui;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class RecBlinkControllerTest {

    @Test
    public void startsVisible() {
        assertTrue(new RecBlinkController(500).isVisible());
    }

    @Test
    public void staysVisibleForFirstHalfPeriod() {
        RecBlinkController controller = new RecBlinkController(500);

        controller.advance(499);

        assertTrue(controller.isVisible());
    }

    @Test
    public void hidesAtHalfPeriod() {
        RecBlinkController controller = new RecBlinkController(500);

        controller.advance(500);

        assertFalse(controller.isVisible());
    }

    @Test
    public void togglesEveryHalfPeriod() {
        RecBlinkController controller = new RecBlinkController(500);

        controller.advance(500);
        assertFalse(controller.isVisible());

        controller.advance(500);
        assertTrue(controller.isVisible());

        controller.advance(500);
        assertFalse(controller.isVisible());
    }

    @Test
    public void resetRestartsVisiblePhase() {
        RecBlinkController controller = new RecBlinkController(500);
        controller.advance(500);
        assertFalse(controller.isVisible());

        controller.reset();

        assertTrue(controller.isVisible());
        assertEquals(500, controller.getHalfPeriodMs());
    }

    @Test
    public void zeroAdvanceDoesNothing() {
        RecBlinkController controller = new RecBlinkController(500);

        controller.advance(0);

        assertTrue(controller.isVisible());
    }

    @Test
    public void rejectsNegativeAdvance() {
        RecBlinkController controller = new RecBlinkController(500);
        try {
            controller.advance(-1);
            fail("delta must not be negative");
        } catch (IllegalArgumentException expected) {
            // Attendu.
        }
    }

    @Test
    public void rejectsNonPositiveHalfPeriod() {
        assertInvalidHalfPeriod(0);
        assertInvalidHalfPeriod(-1);
    }

    private static void assertInvalidHalfPeriod(long halfPeriodMs) {
        try {
            new RecBlinkController(halfPeriodMs);
            fail("half period must be positive: " + halfPeriodMs);
        } catch (IllegalArgumentException expected) {
            // Attendu.
        }
    }
}
