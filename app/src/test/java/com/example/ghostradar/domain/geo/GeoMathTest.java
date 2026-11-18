package com.example.ghostradar.domain.geo;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GeoMathTest {

    private static final double EARTH_RADIUS_M = 6_371_000.0;
    private static final double ONE_DEGREE_M = EARTH_RADIUS_M * Math.PI / 180.0;

    @Test
    public void haversineDistanceIsZeroForSamePoint() {
        LatLon point = new LatLon(48.8566, 2.3522);

        assertEquals(0.0, GeoMath.haversineDistanceMeters(point, point, EARTH_RADIUS_M), 0.0);
    }

    @Test
    public void haversineDistanceForOneDegreeOfLatitude() {
        LatLon equator = new LatLon(0.0, 0.0);
        LatLon oneDegreeNorth = new LatLon(1.0, 0.0);

        assertEquals(ONE_DEGREE_M,
                GeoMath.haversineDistanceMeters(equator, oneDegreeNorth, EARTH_RADIUS_M),
                1.0);
    }

    @Test
    public void haversineDistanceIsSymmetric() {
        LatLon paris = new LatLon(48.8566, 2.3522);
        LatLon london = new LatLon(51.5074, -0.1278);

        double parisToLondon = GeoMath.haversineDistanceMeters(paris, london, EARTH_RADIUS_M);
        double londonToParis = GeoMath.haversineDistanceMeters(london, paris, EARTH_RADIUS_M);

        assertEquals(parisToLondon, londonToParis, 1e-9);
    }

    @Test
    public void bearingReturnsCardinalDirections() {
        LatLon origin = new LatLon(0.0, 0.0);

        assertEquals(0.0, GeoMath.initialBearingDegrees(origin, new LatLon(1.0, 0.0)), 1e-9);
        assertEquals(90.0, GeoMath.initialBearingDegrees(origin, new LatLon(0.0, 1.0)), 1e-9);
        assertEquals(180.0, GeoMath.initialBearingDegrees(origin, new LatLon(-1.0, 0.0)), 1e-9);
        assertEquals(270.0, GeoMath.initialBearingDegrees(origin, new LatLon(0.0, -1.0)), 1e-9);
    }

    @Test
    public void destinationMovesNorthAndEast() {
        LatLon origin = new LatLon(0.0, 0.0);

        LatLon north = GeoMath.destination(origin, 0.0, ONE_DEGREE_M, EARTH_RADIUS_M);
        LatLon east = GeoMath.destination(origin, 90.0, ONE_DEGREE_M, EARTH_RADIUS_M);

        assertEquals(1.0, north.getLatitude(), 1e-9);
        assertEquals(0.0, north.getLongitude(), 1e-9);
        assertEquals(0.0, east.getLatitude(), 1e-9);
        assertEquals(1.0, east.getLongitude(), 1e-9);
    }

    @Test
    public void destinationRoundTripsRequestedDistance() {
        LatLon paris = new LatLon(48.8566, 2.3522);
        LatLon destination = GeoMath.destination(paris, 42.0, 1_234.5, EARTH_RADIUS_M);

        assertEquals(1_234.5,
                GeoMath.haversineDistanceMeters(paris, destination, EARTH_RADIUS_M),
                0.01);
    }

    @Test
    public void normalizeAngleDegreesWrapsIntoZeroTo360() {
        assertEquals(0.0, GeoMath.normalizeAngleDegrees(0.0), 0.0);
        assertEquals(0.0, GeoMath.normalizeAngleDegrees(360.0), 0.0);
        assertEquals(1.0, GeoMath.normalizeAngleDegrees(361.0), 0.0);
        assertEquals(359.0, GeoMath.normalizeAngleDegrees(-1.0), 0.0);
        assertEquals(0.0, GeoMath.normalizeAngleDegrees(720.0), 0.0);
        assertEquals(350.0, GeoMath.normalizeAngleDegrees(-370.0), 0.0);
    }

    @Test
    public void pointInPolygonDetectsInsideAndOutside() {
        List<LatLon> square = Arrays.asList(
                new LatLon(0.0, 0.0),
                new LatLon(0.0, 1.0),
                new LatLon(1.0, 1.0),
                new LatLon(1.0, 0.0)
        );

        assertTrue(GeoMath.pointInPolygon(new LatLon(0.5, 0.5), square));
        assertFalse(GeoMath.pointInPolygon(new LatLon(1.5, 0.5), square));
        assertFalse(GeoMath.pointInPolygon(new LatLon(0.5, 1.5), square));
        assertFalse(GeoMath.pointInPolygon(new LatLon(0.5, 0.5), Collections.emptyList()));
    }

    @Test
    public void distancePointToSegmentUsesPerpendicularDistance() {
        LatLon segmentStart = new LatLon(0.0, 0.0);
        LatLon segmentEnd = new LatLon(0.0, 0.002);
        LatLon point = new LatLon(0.001, 0.001);

        assertEquals(ONE_DEGREE_M * 0.001,
                GeoMath.distancePointToSegmentMeters(point, segmentStart, segmentEnd, EARTH_RADIUS_M),
                0.5);
    }

    @Test
    public void distancePointToSegmentClampsToEndpoints() {
        LatLon segmentStart = new LatLon(0.0, 0.0);
        LatLon segmentEnd = new LatLon(0.0, 0.001);
        LatLon beyondEnd = new LatLon(0.0, 0.002);

        assertEquals(ONE_DEGREE_M * 0.001,
                GeoMath.distancePointToSegmentMeters(beyondEnd, segmentStart, segmentEnd, EARTH_RADIUS_M),
                0.5);
    }

    @Test
    public void distancePointToZeroLengthSegmentIsDistanceToPoint() {
        LatLon segment = new LatLon(1.0, 1.0);
        LatLon point = new LatLon(1.0, 2.0);

        assertEquals(ONE_DEGREE_M * Math.cos(Math.toRadians(1.0)),
                GeoMath.distancePointToSegmentMeters(point, segment, segment, EARTH_RADIUS_M),
                1.0);
    }
}
