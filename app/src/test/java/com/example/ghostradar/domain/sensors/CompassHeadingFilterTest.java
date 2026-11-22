package com.example.ghostradar.domain.sensors;

import com.example.ghostradar.domain.config.GameConfig;

import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class CompassHeadingFilterTest {

    @Test
    public void rejectsNullConfiguration() {
        assertThrows(IllegalArgumentException.class, () -> new CompassHeadingFilter(null));
    }

    @Test
    public void rejectsInvalidLowPassAlpha() {
        assertThrows(IllegalArgumentException.class,
                () -> new CompassHeadingFilter(config(-0.1, true)));
        assertThrows(IllegalArgumentException.class,
                () -> new CompassHeadingFilter(config(1.1, true)));
        assertThrows(IllegalArgumentException.class,
                () -> new CompassHeadingFilter(config(Double.NaN, true)));
    }

    @Test
    public void rejectsNullSample() {
        CompassHeadingFilter filter = new CompassHeadingFilter(config(0.15, true));

        assertThrows(IllegalArgumentException.class, () -> filter.accept(null));
    }

    @Test
    public void firstAcceptedSampleIsReturnedDirectly() {
        CompassHeadingFilter filter = new CompassHeadingFilter(config(0.15, true));
        CompassSample sample = sample(350.0, CompassAccuracy.HIGH, 0L);

        Optional<CompassSample> accepted = filter.accept(sample);

        assertTrue(accepted.isPresent());
        assertSame(sample, accepted.get());
        assertSame(sample, filter.getCurrent().get());
    }

    @Test
    public void ignoresUnreliableNoContactAndLowWhenConfigured() {
        CompassHeadingFilter filter = new CompassHeadingFilter(config(0.15, true));

        assertFalse(filter.accept(sample(10.0, CompassAccuracy.UNRELIABLE, 0L)).isPresent());
        assertFalse(filter.accept(sample(10.0, CompassAccuracy.NO_CONTACT, 0L)).isPresent());
        assertFalse(filter.accept(sample(10.0, CompassAccuracy.LOW, 0L)).isPresent());
        assertFalse(filter.getCurrent().isPresent());
    }

    @Test
    public void acceptsMediumAndHigh() {
        CompassHeadingFilter mediumFilter = new CompassHeadingFilter(config(0.15, true));
        CompassHeadingFilter highFilter = new CompassHeadingFilter(config(0.15, true));

        assertTrue(mediumFilter.accept(sample(10.0, CompassAccuracy.MEDIUM, 0L)).isPresent());
        assertTrue(highFilter.accept(sample(10.0, CompassAccuracy.HIGH, 0L)).isPresent());
    }

    @Test
    public void acceptsAllAccuraciesWhenIgnoreIsDisabled() {
        CompassHeadingFilter filter = new CompassHeadingFilter(config(0.15, false));

        assertTrue(filter.accept(sample(10.0, CompassAccuracy.UNRELIABLE, 0L)).isPresent());
        assertTrue(filter.accept(sample(20.0, CompassAccuracy.NO_CONTACT, 1L)).isPresent());
        assertTrue(filter.accept(sample(30.0, CompassAccuracy.LOW, 2L)).isPresent());
    }

    @Test
    public void lowPassUsesShortestPathAcrossNorthFromWestToEast() {
        CompassHeadingFilter filter = new CompassHeadingFilter(config(0.5, true));
        filter.accept(sample(350.0, CompassAccuracy.HIGH, 0L));

        CompassSample filtered = filter.accept(sample(10.0, CompassAccuracy.HIGH, 1L)).get();

        assertEquals(0.0, filtered.getAzimuthDeg(), 1e-9);
    }

    @Test
    public void lowPassUsesShortestPathAcrossNorthFromEastToWest() {
        CompassHeadingFilter filter = new CompassHeadingFilter(config(0.5, true));
        filter.accept(sample(10.0, CompassAccuracy.HIGH, 0L));

        CompassSample filtered = filter.accept(sample(350.0, CompassAccuracy.HIGH, 1L)).get();

        assertEquals(0.0, filtered.getAzimuthDeg(), 1e-9);
    }

    @Test
    public void lowPassAppliesConfiguredAlpha() {
        CompassHeadingFilter filter = new CompassHeadingFilter(config(0.15, true));
        filter.accept(sample(0.0, CompassAccuracy.HIGH, 0L));

        CompassSample filtered = filter.accept(sample(100.0, CompassAccuracy.HIGH, 1L)).get();

        assertEquals(15.0, filtered.getAzimuthDeg(), 1e-9);
    }

    @Test
    public void alphaZeroKeepsFirstHeading() {
        CompassHeadingFilter filter = new CompassHeadingFilter(config(0.0, true));
        filter.accept(sample(10.0, CompassAccuracy.HIGH, 0L));

        CompassSample filtered = filter.accept(sample(100.0, CompassAccuracy.HIGH, 1L)).get();

        assertEquals(10.0, filtered.getAzimuthDeg(), 1e-9);
    }

    @Test
    public void alphaOneFollowsRawHeading() {
        CompassHeadingFilter filter = new CompassHeadingFilter(config(1.0, true));
        filter.accept(sample(10.0, CompassAccuracy.HIGH, 0L));

        CompassSample filtered = filter.accept(sample(100.0, CompassAccuracy.HIGH, 1L)).get();

        assertEquals(100.0, filtered.getAzimuthDeg(), 1e-9);
    }

    @Test
    public void rejectedSampleDoesNotUpdateFilterState() {
        CompassHeadingFilter filter = new CompassHeadingFilter(config(0.5, true));
        filter.accept(sample(100.0, CompassAccuracy.HIGH, 0L));

        assertFalse(filter.accept(sample(200.0, CompassAccuracy.UNRELIABLE, 1L)).isPresent());

        CompassSample filtered = filter.accept(sample(110.0, CompassAccuracy.HIGH, 2L)).get();

        assertEquals(105.0, filtered.getAzimuthDeg(), 1e-9);
    }

    @Test
    public void resetAllowsNextSampleToInitializeDirectly() {
        CompassHeadingFilter filter = new CompassHeadingFilter(config(0.15, true));
        filter.accept(sample(100.0, CompassAccuracy.HIGH, 0L));

        filter.reset();

        assertFalse(filter.getCurrent().isPresent());
        CompassSample filtered = filter.accept(sample(10.0, CompassAccuracy.HIGH, 1L)).get();
        assertEquals(10.0, filtered.getAzimuthDeg(), 1e-9);
    }

    private static GameConfig config(double alpha, boolean ignoreUnreliable) {
        return new GameConfig.Builder()
                .setCompassLowPassAlpha(alpha)
                .setCompassIgnoreUnreliable(ignoreUnreliable)
                .build();
    }

    private static CompassSample sample(double azimuthDeg, CompassAccuracy accuracy,
                                        long timestampNanos) {
        return new CompassSample(azimuthDeg, accuracy, timestampNanos);
    }
}
