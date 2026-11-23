package com.example.ghostradar.domain.sensors;

import com.example.ghostradar.domain.config.GameConfig;
import com.example.ghostradar.domain.geo.GeoMath;
import com.example.ghostradar.domain.geo.LatLon;

import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class GpsFixFilterTest {

    private static final long ONE_SECOND_NANOS = 1_000_000_000L;
    private static final double EARTH_RADIUS_M = 6_371_000.0;
    private static final LatLon ORIGIN = new LatLon(48.8566, 2.3522);

    @Test
    public void rejectsNullConfiguration() {
        assertThrows(IllegalArgumentException.class, () -> new GpsFixFilter(null));
    }

    @Test
    public void rejectsInvalidAccuracyLimit() {
        assertThrows(IllegalArgumentException.class,
                () -> new GpsFixFilter(new GameConfig.Builder().setGpsAccuracyMaxM(0.0).build()));
        assertThrows(IllegalArgumentException.class,
                () -> new GpsFixFilter(new GameConfig.Builder().setGpsAccuracyMaxM(-1.0).build()));
        assertThrows(IllegalArgumentException.class,
                () -> new GpsFixFilter(new GameConfig.Builder().setGpsAccuracyMaxM(Double.NaN).build()));
        assertThrows(IllegalArgumentException.class,
                () -> new GpsFixFilter(new GameConfig.Builder()
                        .setGpsAccuracyMaxM(Double.POSITIVE_INFINITY).build()));
    }

    @Test
    public void rejectsInvalidUpdateInterval() {
        assertThrows(IllegalArgumentException.class,
                () -> new GpsFixFilter(new GameConfig.Builder().setGpsUpdateIntervalMs(0).build()));
        assertThrows(IllegalArgumentException.class,
                () -> new GpsFixFilter(new GameConfig.Builder().setGpsUpdateIntervalMs(-1).build()));
    }

    @Test
    public void rejectsInvalidMinimumDistance() {
        assertThrows(IllegalArgumentException.class,
                () -> new GpsFixFilter(new GameConfig.Builder().setGpsMinDistanceM(-0.1).build()));
        assertThrows(IllegalArgumentException.class,
                () -> new GpsFixFilter(new GameConfig.Builder().setGpsMinDistanceM(Double.NaN).build()));
        assertThrows(IllegalArgumentException.class,
                () -> new GpsFixFilter(new GameConfig.Builder()
                        .setGpsMinDistanceM(Double.POSITIVE_INFINITY).build()));
    }

    @Test
    public void rejectsInvalidEarthRadius() {
        assertThrows(IllegalArgumentException.class,
                () -> new GpsFixFilter(new GameConfig.Builder().setEarthRadiusM(0.0).build()));
        assertThrows(IllegalArgumentException.class,
                () -> new GpsFixFilter(new GameConfig.Builder().setEarthRadiusM(Double.NaN).build()));
    }

    @Test
    public void rejectsNullSample() {
        GpsFixFilter filter = new GpsFixFilter(GameConfig.defaults());

        assertThrows(IllegalArgumentException.class, () -> filter.accept(null));
    }

    @Test
    public void acceptsFirstValidFix() {
        GpsFixFilter filter = new GpsFixFilter(GameConfig.defaults());
        GpsSample sample = sample(ORIGIN, 5.0, 0L);

        Optional<GpsSample> accepted = filter.accept(sample);

        assertTrue(accepted.isPresent());
        assertSame(sample, accepted.get());
        assertSame(sample, filter.getLastAccepted().get());
    }

    @Test
    public void acceptsAccuracyExactlyAtLimit() {
        GpsFixFilter filter = new GpsFixFilter(GameConfig.defaults());

        assertTrue(filter.accept(sample(ORIGIN, 20.0, 0L)).isPresent());
    }

    @Test
    public void rejectsAccuracyAboveLimit() {
        GpsFixFilter filter = new GpsFixFilter(GameConfig.defaults());

        assertFalse(filter.accept(sample(ORIGIN, 20.1, 0L)).isPresent());
        assertFalse(filter.getLastAccepted().isPresent());
    }

    @Test
    public void invalidCoordinatesAreRejectedAtConstruction() {
        // Depuis la validation centralisée (Q7), c'est LatLon qui refuse ces
        // valeurs ; le filtre n'a plus à les filtrer en aval.
        assertThrows(IllegalArgumentException.class, () -> new LatLon(Double.NaN, 2.0));
        assertThrows(IllegalArgumentException.class, () -> new LatLon(91.0, 2.0));
        assertThrows(IllegalArgumentException.class, () -> new LatLon(-91.0, 2.0));
        assertThrows(IllegalArgumentException.class, () -> new LatLon(48.0, 181.0));
        assertThrows(IllegalArgumentException.class, () -> new LatLon(48.0, Double.POSITIVE_INFINITY));
    }

    @Test
    public void rejectsStaleTimestamp() {
        GpsFixFilter filter = new GpsFixFilter(GameConfig.defaults());
        filter.accept(sample(ORIGIN, 5.0, ONE_SECOND_NANOS));

        GpsSample stale = sample(moveNorth(5.0), 5.0, 0L);

        assertFalse(filter.accept(stale).isPresent());
    }

    @Test
    public void rejectsFixBeforeMinimumIntervalEvenIfMoved() {
        GpsFixFilter filter = new GpsFixFilter(GameConfig.defaults());
        filter.accept(sample(ORIGIN, 5.0, 0L));

        GpsSample tooEarly = sample(moveNorth(5.0), 5.0, 500_000_000L);

        assertFalse(filter.accept(tooEarly).isPresent());
    }

    @Test
    public void rejectsFixAfterIntervalWithoutEnoughMovement() {
        GpsFixFilter filter = new GpsFixFilter(GameConfig.defaults());
        filter.accept(sample(ORIGIN, 5.0, 0L));

        GpsSample tooClose = sample(moveNorth(0.5), 5.0, ONE_SECOND_NANOS);

        assertFalse(filter.accept(tooClose).isPresent());
    }

    @Test
    public void acceptsFixAfterIntervalAndEnoughMovement() {
        GpsFixFilter filter = new GpsFixFilter(GameConfig.defaults());
        filter.accept(sample(ORIGIN, 5.0, 0L));

        GpsSample valid = sample(moveNorth(2.0), 5.0, ONE_SECOND_NANOS);

        assertTrue(filter.accept(valid).isPresent());
        assertSame(valid, filter.getLastAccepted().get());
    }

    @Test
    public void acceptsExactlyAtIntervalAndDistance() {
        GpsFixFilter filter = new GpsFixFilter(GameConfig.defaults());
        filter.accept(sample(ORIGIN, 5.0, 0L));

        GpsSample boundary = sample(moveNorth(1.0), 5.0, ONE_SECOND_NANOS);

        assertTrue(filter.accept(boundary).isPresent());
    }

    @Test
    public void rejectedFixDoesNotUpdateLastAccepted() {
        GpsFixFilter filter = new GpsFixFilter(GameConfig.defaults());
        GpsSample first = sample(ORIGIN, 5.0, 0L);
        filter.accept(first);

        GpsSample rejected = sample(moveNorth(0.5), 5.0, ONE_SECOND_NANOS);
        assertFalse(filter.accept(rejected).isPresent());

        GpsSample later = sample(moveNorth(2.0), 5.0, 1_500_000_000L);
        assertTrue(filter.accept(later).isPresent());
    }

    @Test
    public void resetAllowsImmediateFix() {
        GpsFixFilter filter = new GpsFixFilter(GameConfig.defaults());
        filter.accept(sample(ORIGIN, 5.0, 0L));

        filter.reset();

        assertFalse(filter.getLastAccepted().isPresent());
        assertTrue(filter.accept(sample(ORIGIN, 5.0, 100_000_000L)).isPresent());
    }

    private static GpsSample sample(LatLon position, double accuracyM, long timestampNanos) {
        return new GpsSample(position, accuracyM, timestampNanos);
    }

    private static LatLon moveNorth(double distanceM) {
        return GeoMath.destination(ORIGIN, 0.0, distanceM, EARTH_RADIUS_M);
    }
}
