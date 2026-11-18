package com.example.ghostradar.domain.geo;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PlayAreaTest {

    private static final double EARTH_RADIUS_M = 6_371_000.0;
    private static final double MIN_VERTEX_DISTANCE_M = 0.5;

    @Test
    public void containsExcludesObstaclesAndOutside() {
        PlayArea area = area();

        assertTrue(area.contains(new LatLon(0.5, 0.5)));
        assertTrue(area.contains(new LatLon(0.5, 1.5)));
        assertFalse(area.contains(new LatLon(1.5, 1.5)));
        assertFalse(area.contains(new LatLon(4.0, 1.5)));
    }

    @Test
    public void clampLeavesValidPointUnchanged() {
        PlayArea area = area();
        LatLon inside = new LatLon(0.5, 0.5);

        assertEquals(inside, area.clamp(inside));
    }

    @Test
    public void clampProjectsOutsidePointOntoOuterBoundary() {
        PlayArea area = area();

        assertEquals(new LatLon(3.0, 1.5), area.clamp(new LatLon(3.5, 1.5)));
        assertEquals(new LatLon(2.5, 3.0), area.clamp(new LatLon(2.5, 3.2)));
    }

    @Test
    public void clampProjectsObstaclePointOntoObstacleBoundary() {
        PlayArea area = area();

        assertEquals(new LatLon(1.5, 2.0), area.clamp(new LatLon(1.5, 1.9)));
    }

    @Test
    public void exposesOuterPolygonAndObstacles() {
        PlayArea area = area();

        assertEquals(3.0, area.getOuterPolygon().getVertices().get(2).getLatitude(), 0.0);
        assertEquals(1, area.getObstacles().size());
    }

    private static PlayArea area() {
        GeoPolygon outer = polygon(
                new LatLon(0.0, 0.0),
                new LatLon(0.0, 3.0),
                new LatLon(3.0, 3.0),
                new LatLon(3.0, 0.0)
        );
        GeoPolygon obstacle = polygon(
                new LatLon(1.0, 1.0),
                new LatLon(1.0, 2.0),
                new LatLon(2.0, 2.0),
                new LatLon(2.0, 1.0)
        );
        return new PlayArea(outer, Collections.singletonList(obstacle), EARTH_RADIUS_M);
    }

    private static GeoPolygon polygon(LatLon... vertices) {
        return new GeoPolygon(Arrays.asList(vertices), 3, MIN_VERTEX_DISTANCE_M, EARTH_RADIUS_M);
    }
}
