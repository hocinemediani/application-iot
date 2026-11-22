package com.example.ghostradar.ui;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class NoiseGaugeFormatterTest {

    @Test
    public void roundsLevelToNearestSegment() {
        assertEquals(0, NoiseGaugeFormatter.segmentCount(0.0));
        assertEquals(0, NoiseGaugeFormatter.segmentCount(0.04));
        assertEquals(1, NoiseGaugeFormatter.segmentCount(0.05));
        assertEquals(5, NoiseGaugeFormatter.segmentCount(0.5));
        assertEquals(10, NoiseGaugeFormatter.segmentCount(0.95));
        assertEquals(10, NoiseGaugeFormatter.segmentCount(1.0));
    }

    @Test
    public void clampsOutOfRangeLevel() {
        assertEquals(0, NoiseGaugeFormatter.segmentCount(-0.5));
        assertEquals(10, NoiseGaugeFormatter.segmentCount(1.5));
    }

    @Test
    public void rejectsNonFiniteLevel() {
        assertInvalidLevel(Double.NaN);
        assertInvalidLevel(Double.POSITIVE_INFINITY);
        assertInvalidLevel(Double.NEGATIVE_INFINITY);
    }

    @Test
    public void usesYellowBelowMinusFortyDbfs() {
        assertEquals(NoiseGaugeStyle.YELLOW_ARGB,
                NoiseGaugeFormatter.colorForDbfs(-60.0, -30.0));
        assertEquals(NoiseGaugeStyle.YELLOW_ARGB,
                NoiseGaugeFormatter.colorForDbfs(-40.1, -30.0));
    }

    @Test
    public void usesOrangeFromMinusFortyToThresholdInclusive() {
        assertEquals(NoiseGaugeStyle.ORANGE_ARGB,
                NoiseGaugeFormatter.colorForDbfs(-40.0, -30.0));
        assertEquals(NoiseGaugeStyle.ORANGE_ARGB,
                NoiseGaugeFormatter.colorForDbfs(-35.0, -30.0));
        assertEquals(NoiseGaugeStyle.ORANGE_ARGB,
                NoiseGaugeFormatter.colorForDbfs(-30.0, -30.0));
    }

    @Test
    public void usesRedAboveThreshold() {
        assertEquals(NoiseGaugeStyle.RED_ARGB,
                NoiseGaugeFormatter.colorForDbfs(-29.9, -30.0));
        assertEquals(NoiseGaugeStyle.RED_ARGB,
                NoiseGaugeFormatter.colorForDbfs(0.0, -30.0));
    }

    @Test
    public void honorsConfiguredThreshold() {
        assertEquals(NoiseGaugeStyle.ORANGE_ARGB,
                NoiseGaugeFormatter.colorForDbfs(-27.0, -25.0));
        assertEquals(NoiseGaugeStyle.RED_ARGB,
                NoiseGaugeFormatter.colorForDbfs(-24.0, -25.0));
    }

    @Test
    public void segmentColorIsEmptyAboveFilledCount() {
        assertEquals(NoiseGaugeStyle.ORANGE_ARGB,
                NoiseGaugeFormatter.colorForSegment(0, 3, -35.0, -30.0));
        assertEquals(NoiseGaugeStyle.ORANGE_ARGB,
                NoiseGaugeFormatter.colorForSegment(2, 3, -35.0, -30.0));
        assertEquals(NoiseGaugeStyle.EMPTY_ARGB,
                NoiseGaugeFormatter.colorForSegment(3, 3, -35.0, -30.0));
        assertEquals(NoiseGaugeStyle.EMPTY_ARGB,
                NoiseGaugeFormatter.colorForSegment(9, 3, -35.0, -30.0));
    }

    @Test
    public void rejectsInvalidSegmentIndex() {
        assertInvalidSegmentIndex(-1);
        assertInvalidSegmentIndex(NoiseGaugeStyle.SEGMENT_COUNT);
    }

    @Test
    public void rejectsNonFiniteDbfsOrThreshold() {
        try {
            NoiseGaugeFormatter.colorForDbfs(Double.NaN, -30.0);
            fail("dbfs must be finite");
        } catch (IllegalArgumentException expected) {
            // Attendu.
        }
        try {
            NoiseGaugeFormatter.colorForDbfs(-35.0, Double.POSITIVE_INFINITY);
            fail("threshold must be finite");
        } catch (IllegalArgumentException expected) {
            // Attendu.
        }
    }

    private static void assertInvalidLevel(double level) {
        try {
            NoiseGaugeFormatter.segmentCount(level);
            fail("level must be finite: " + level);
        } catch (IllegalArgumentException expected) {
            // Attendu.
        }
    }

    private static void assertInvalidSegmentIndex(int index) {
        try {
            NoiseGaugeFormatter.colorForSegment(index, 3, -35.0, -30.0);
            fail("segment index out of bounds: " + index);
        } catch (IllegalArgumentException expected) {
            // Attendu.
        }
    }
}
