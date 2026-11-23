package com.example.ghostradar.domain;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class ValidationTest {

    @Test
    public void requireFiniteRejectsNaNAndBothInfinities() {
        assertThrows(IllegalArgumentException.class, () -> Validation.requireFinite("x", Double.NaN));
        assertThrows(IllegalArgumentException.class,
                () -> Validation.requireFinite("x", Double.POSITIVE_INFINITY));
        assertThrows(IllegalArgumentException.class,
                () -> Validation.requireFinite("x", Double.NEGATIVE_INFINITY));

        assertEquals(1.5, Validation.requireFinite("x", 1.5), 0.0);
        assertTrue(assertThrows(IllegalArgumentException.class,
                () -> Validation.requireFinite("latitude", Double.NaN))
                .getMessage().contains("latitude"));
    }

    @Test
    public void requirePositiveFiniteRejectsZeroNegativeAndNonFinite() {
        assertThrows(IllegalArgumentException.class, () -> Validation.requirePositiveFinite("radius", 0.0));
        assertThrows(IllegalArgumentException.class, () -> Validation.requirePositiveFinite("radius", -1.0));
        assertThrows(IllegalArgumentException.class,
                () -> Validation.requirePositiveFinite("radius", Double.NaN));
        assertThrows(IllegalArgumentException.class,
                () -> Validation.requirePositiveFinite("radius", Double.POSITIVE_INFINITY));
        assertEquals(3.0, Validation.requirePositiveFinite("radius", 3.0), 0.0);
    }

    @Test
    public void requireNonNegativeFiniteAcceptsZeroButRejectsNegativeAndNonFinite() {
        assertEquals(0.0, Validation.requireNonNegativeFinite("decay", 0.0), 0.0);
        assertThrows(IllegalArgumentException.class,
                () -> Validation.requireNonNegativeFinite("decay", -0.1));
        assertThrows(IllegalArgumentException.class,
                () -> Validation.requireNonNegativeFinite("decay", Double.NaN));
    }

    @Test
    public void requireRangeRejectsInvertedBoundsAndOutOfRangeValues() {
        assertThrows(IllegalArgumentException.class, () -> Validation.requireRange("delay", 2.0, 3.0, 1.0));
        assertThrows(IllegalArgumentException.class, () -> Validation.requireRange("delay", 0.5, 1.0, 3.0));
        assertThrows(IllegalArgumentException.class, () -> Validation.requireRange("delay", 3.5, 1.0, 3.0));
        assertThrows(IllegalArgumentException.class,
                () -> Validation.requireRange("delay", Double.NaN, 1.0, 3.0));
        assertEquals(2.0, Validation.requireRange("delay", 2.0, 1.0, 3.0), 0.0);
    }

    @Test
    public void integerAndObjectHelpersNameTheField() {
        assertEquals(3, Validation.requirePositiveInt("vertices", 3));
        assertThrows(IllegalArgumentException.class, () -> Validation.requirePositiveInt("vertices", 0));
        assertEquals(0, Validation.requireNonNegativeInt("stocks", 0));
        assertThrows(IllegalArgumentException.class, () -> Validation.requireNonNegativeInt("stocks", -1));
        assertEquals("TRACKING", Validation.requireNonNull("state", "TRACKING"));
        assertTrue(assertThrows(IllegalArgumentException.class,
                () -> Validation.requireNonNull("mapProjection", null))
                .getMessage().contains("mapProjection"));
    }

    @Test
    public void requireSameRadiusRejectsDivergentSources() {
        assertEquals(6_371_000.0,
                Validation.requireSameRadius("earthRadiusM", 6_371_000.0, 6_371_000.0), 0.0);
        assertTrue(assertThrows(IllegalArgumentException.class,
                () -> Validation.requireSameRadius("earthRadiusM", 6_371_000.0, 6_400_000.0))
                .getMessage().contains("earthRadiusM"));
    }

    @Test
    public void requireDeltaSecondsAcceptsZeroAndRejectsNegativeAndNonFinite() {
        assertEquals(0.0, Validation.requireDeltaSeconds(0.0), 0.0);
        assertThrows(IllegalArgumentException.class, () -> Validation.requireDeltaSeconds(-0.1));
        assertThrows(IllegalArgumentException.class, () -> Validation.requireDeltaSeconds(Double.NaN));
        assertThrows(IllegalArgumentException.class,
                () -> Validation.requireDeltaSeconds(Double.POSITIVE_INFINITY));
    }
}
