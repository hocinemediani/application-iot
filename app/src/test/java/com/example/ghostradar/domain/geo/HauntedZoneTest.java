package com.example.ghostradar.domain.geo;

import com.example.ghostradar.domain.game.RandomSource;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class HauntedZoneTest {

    private static final double EARTH_RADIUS_M = 6_371_000.0;
    private static final double MIN_VERTEX_DISTANCE_M = 0.5;
    private static final int MIN_VERTICES = 5;
    private static final int MAX_VERTICES = 8;
    private static final double MIN_RADIUS_M = 10.0;
    private static final double MAX_RADIUS_M = 30.0;
    private static final int MAX_CENTER_ATTEMPTS = 1_000;

    @Test
    public void randomPolygonUsesConfiguredVertexAndRadiusBounds() {
        PlayArea area = area(square(0.0, 0.0, 3.0, 3.0), Collections.emptyList());

        HauntedZone zone = randomZone(area, RandomSource.seeded(42L));

        List<LatLon> vertices = zone.getPolygon().getVertices();
        assertTrue(vertices.size() >= MIN_VERTICES);
        assertTrue(vertices.size() <= MAX_VERTICES);
        assertTrue(zone.contains(zone.getCenter()));
        for (LatLon vertex : vertices) {
            double distanceM = GeoMath.haversineDistanceMeters(
                    zone.getCenter(), vertex, EARTH_RADIUS_M);
            assertTrue(distanceM >= MIN_RADIUS_M - 0.01);
            assertTrue(distanceM <= MAX_RADIUS_M + 0.01);
        }
    }

    @Test
    public void randomPolygonDrawsCenterInsidePlayAreaOutsideObstacles() {
        PlayArea area = area(
                square(0.0, 0.0, 1.0, 1.0),
                Collections.singletonList(square(0.2, 0.2, 0.8, 0.8))
        );
        RandomSource random = RandomSource.seeded(7L);

        for (int i = 0; i < 20; i++) {
            HauntedZone zone = randomZone(area, random);
            assertTrue(area.contains(zone.getCenter()));
        }
    }

    @Test
    public void randomPolygonIsDeterministicForSameSeed() {
        PlayArea area = area(square(0.0, 0.0, 3.0, 3.0), Collections.emptyList());

        HauntedZone first = randomZone(area, RandomSource.seeded(99L));
        HauntedZone second = randomZone(area, RandomSource.seeded(99L));

        assertEquals(first.getCenter(), second.getCenter());
        assertEquals(first.getPolygon().getVertices(), second.getPolygon().getVertices());
    }

    @Test
    public void randomPolygonFailsWhenNoValidCenterExists() {
        PlayArea area = area(
                square(0.0, 0.0, 1.0, 1.0),
                Collections.singletonList(square(-0.1, -0.1, 1.1, 1.1))
        );

        assertThrows(IllegalStateException.class,
                () -> HauntedZone.randomPolygon(area, RandomSource.seeded(1L),
                        MIN_VERTICES, MAX_VERTICES, MIN_RADIUS_M, MAX_RADIUS_M,
                        MIN_VERTEX_DISTANCE_M, 10));
    }

    @Test
    public void randomPolygonRejectsInvalidParameters() {
        PlayArea area = area(square(0.0, 0.0, 3.0, 3.0), Collections.emptyList());

        assertThrows(IllegalArgumentException.class,
                () -> HauntedZone.randomPolygon(area, RandomSource.seeded(1L),
                        4, 3, MIN_RADIUS_M, MAX_RADIUS_M, MIN_VERTEX_DISTANCE_M,
                        MAX_CENTER_ATTEMPTS));
        assertThrows(IllegalArgumentException.class,
                () -> HauntedZone.randomPolygon(area, RandomSource.seeded(1L),
                        MIN_VERTICES, MAX_VERTICES, MAX_RADIUS_M, MIN_RADIUS_M,
                        MIN_VERTEX_DISTANCE_M, MAX_CENTER_ATTEMPTS));
    }

    @Test
    public void polygonZoneContainsOnlyItsInterior() {
        GeoPolygon polygon = square(0.0, 0.0, 1.0, 1.0);
        HauntedZone zone = HauntedZone.polygon(polygon);

        assertFalse(zone.isCircle());
        assertSame(polygon, zone.getPolygon());
        assertTrue(zone.contains(new LatLon(0.5, 0.5)));
        assertFalse(zone.contains(new LatLon(1.5, 0.5)));
        assertEquals(new LatLon(0.5, 0.5), zone.getCenter());
    }

    @Test
    public void adminCircleContainsOnlyPointsWithinRadius() {
        LatLon center = new LatLon(48.8566, 2.3522);
        HauntedZone zone = HauntedZone.circle(center, 25.0, EARTH_RADIUS_M);

        assertTrue(zone.isCircle());
        assertEquals(center, zone.getCenter());
        assertEquals(25.0, zone.getRadiusM(), 0.0);
        assertTrue(zone.contains(center));
        assertTrue(zone.contains(GeoMath.destination(center, 0.0, 24.9, EARTH_RADIUS_M)));
        assertFalse(zone.contains(GeoMath.destination(center, 0.0, 25.1, EARTH_RADIUS_M)));
    }

    @Test
    public void circleRejectsNonPositiveRadius() {
        assertThrows(IllegalArgumentException.class,
                () -> HauntedZone.circle(new LatLon(0.0, 0.0), 0.0, EARTH_RADIUS_M));
    }

    private static HauntedZone randomZone(PlayArea area, RandomSource random) {
        return HauntedZone.randomPolygon(area, random, MIN_VERTICES, MAX_VERTICES,
                MIN_RADIUS_M, MAX_RADIUS_M, MIN_VERTEX_DISTANCE_M, MAX_CENTER_ATTEMPTS);
    }

    private static PlayArea area(GeoPolygon outer, List<GeoPolygon> obstacles) {
        return new PlayArea(outer, obstacles, EARTH_RADIUS_M);
    }

    private static GeoPolygon square(double minLat, double minLon, double maxLat, double maxLon) {
        return polygon(
                new LatLon(minLat, minLon),
                new LatLon(minLat, maxLon),
                new LatLon(maxLat, maxLon),
                new LatLon(maxLat, minLon)
        );
    }

    private static GeoPolygon polygon(LatLon... vertices) {
        return new GeoPolygon(Arrays.asList(vertices), 3, MIN_VERTEX_DISTANCE_M, EARTH_RADIUS_M);
    }
}
