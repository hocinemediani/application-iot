package com.example.ghostradar.domain.geo;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class MapProjectionTest {

    private static final double EARTH_RADIUS_M = 6_371_000.0;
    private static final double MIN_VERTEX_DISTANCE_M = 0.5;
    private static final LatLon ORIGIN = new LatLon(0.005, 0.005);

    @Test
    public void originIsOuterPolygonBoundingBoxCenter() {
        MapProjection projection = new MapProjection(playArea(EARTH_RADIUS_M));

        assertEquals(ORIGIN.getLatitude(), projection.getOrigin().getLatitude(), 0.0);
        assertEquals(ORIGIN.getLongitude(), projection.getOrigin().getLongitude(), 0.0);
    }

    @Test
    public void originProjectsToZero() {
        MapProjection projection = new MapProjection(playArea(EARTH_RADIUS_M));

        MapProjection.MapPoint point = projection.project(ORIGIN);

        assertEquals(0.0, point.getXM(), 1e-9);
        assertEquals(0.0, point.getYM(), 1e-9);
    }

    @Test
    public void eastIsPositiveXAndNorthIsPositiveY() {
        MapProjection projection = new MapProjection(playArea(EARTH_RADIUS_M));
        double metersPerDegreeLat = EARTH_RADIUS_M * Math.PI / 180.0;
        double metersPerDegreeLon = metersPerDegreeLat
                * Math.cos(Math.toRadians(ORIGIN.getLatitude()));

        MapProjection.MapPoint east = projection.project(
                new LatLon(ORIGIN.getLatitude(), ORIGIN.getLongitude() + 0.001));
        MapProjection.MapPoint north = projection.project(
                new LatLon(ORIGIN.getLatitude() + 0.001, ORIGIN.getLongitude()));

        assertEquals(metersPerDegreeLon * 0.001, east.getXM(), 1e-6);
        assertEquals(0.0, east.getYM(), 1e-9);
        assertEquals(0.0, north.getXM(), 1e-9);
        assertEquals(metersPerDegreeLat * 0.001, north.getYM(), 1e-6);
    }

    @Test
    public void westAndSouthAreNegative() {
        MapProjection projection = new MapProjection(playArea(EARTH_RADIUS_M));

        MapProjection.MapPoint point = projection.project(
                new LatLon(ORIGIN.getLatitude() - 0.001, ORIGIN.getLongitude() - 0.001));

        assertTrue(point.getXM() < 0.0);
        assertTrue(point.getYM() < 0.0);
    }

    @Test
    public void unprojectReversesProjection() {
        MapProjection projection = new MapProjection(playArea(EARTH_RADIUS_M));
        LatLon source = new LatLon(0.0062, 0.0037);

        MapProjection.MapPoint projected = projection.project(source);
        LatLon restored = projection.unproject(projected.getXM(), projected.getYM());

        assertEquals(source.getLatitude(), restored.getLatitude(), 1e-9);
        assertEquals(source.getLongitude(), restored.getLongitude(), 1e-9);
    }

    @Test
    public void usesPlayAreaEarthRadius() {
        double smallEarthRadiusM = 1_000_000.0;
        MapProjection projection = new MapProjection(playArea(smallEarthRadiusM));

        MapProjection.MapPoint north = projection.project(
                new LatLon(ORIGIN.getLatitude() + 0.001, ORIGIN.getLongitude()));

        assertEquals(smallEarthRadiusM * Math.toRadians(0.001), north.getYM(), 1e-9);
    }

    @Test
    public void rejectsInvalidArguments() {
        assertThrows(IllegalArgumentException.class, () -> new MapProjection(null));

        MapProjection projection = new MapProjection(playArea(EARTH_RADIUS_M));
        assertThrows(IllegalArgumentException.class, () -> projection.project(null));
        assertThrows(IllegalArgumentException.class,
                () -> projection.unproject(Double.NaN, 0.0));
        assertThrows(IllegalArgumentException.class,
                () -> projection.unproject(0.0, Double.POSITIVE_INFINITY));
    }

    private static PlayArea playArea(double earthRadiusM) {
        return new PlayArea(square(0.0, 0.0, 0.01, 0.01),
                Collections.emptyList(), earthRadiusM);
    }

    private static GeoPolygon square(double minLat, double minLon,
                                     double maxLat, double maxLon) {
        return new GeoPolygon(Arrays.asList(
                new LatLon(minLat, minLon),
                new LatLon(minLat, maxLon),
                new LatLon(maxLat, maxLon),
                new LatLon(maxLat, minLon)
        ), 3, MIN_VERTEX_DISTANCE_M, EARTH_RADIUS_M);
    }
}
