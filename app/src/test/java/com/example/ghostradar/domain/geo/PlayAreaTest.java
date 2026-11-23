package com.example.ghostradar.domain.geo;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
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

    @Test
    public void rejectsRadiusDivergentFromItsPolygons() {
        GeoPolygon outer = polygon(
                new LatLon(0.0, 0.0), new LatLon(0.0, 3.0),
                new LatLon(3.0, 3.0), new LatLon(3.0, 0.0));

        assertThrows(IllegalArgumentException.class,
                () -> new PlayArea(outer, Collections.emptyList(), 6_400_000.0));

        GeoPolygon divergentObstacle = new GeoPolygon(Arrays.asList(
                new LatLon(1.0, 1.0), new LatLon(1.0, 2.0),
                new LatLon(2.0, 2.0), new LatLon(2.0, 1.0)),
                3, MIN_VERTEX_DISTANCE_M, 6_400_000.0);
        assertThrows(IllegalArgumentException.class,
                () -> new PlayArea(outer, Collections.singletonList(divergentObstacle),
                        EARTH_RADIUS_M));
    }

    @Test
    public void rejectsNonFiniteEarthRadius() {
        GeoPolygon outer = polygon(
                new LatLon(0.0, 0.0), new LatLon(0.0, 3.0),
                new LatLon(3.0, 3.0), new LatLon(3.0, 0.0));

        assertThrows(IllegalArgumentException.class,
                () -> new PlayArea(outer, Collections.emptyList(), Double.NaN));
        assertThrows(IllegalArgumentException.class,
                () -> new PlayArea(outer, Collections.emptyList(), Double.POSITIVE_INFINITY));
    }

    @Test
    public void rejectsObstacleOutsideOuterPolygon() {
        GeoPolygon outer = square(0.0, 0.0, 3.0, 3.0);
        GeoPolygon outside = square(4.0, 4.0, 5.0, 5.0);

        assertThrows(IllegalArgumentException.class,
                () -> new PlayArea(outer, Collections.singletonList(outside), EARTH_RADIUS_M));
    }

    @Test
    public void rejectsObstacleCrossingOrTouchingOuterBoundary() {
        GeoPolygon outer = square(0.0, 0.0, 3.0, 3.0);
        GeoPolygon crossing = square(2.5, 2.5, 3.5, 3.5);
        GeoPolygon touching = square(0.0, 1.0, 1.0, 2.0);

        assertThrows(IllegalArgumentException.class,
                () -> new PlayArea(outer, Collections.singletonList(crossing), EARTH_RADIUS_M));
        assertThrows(IllegalArgumentException.class,
                () -> new PlayArea(outer, Collections.singletonList(touching), EARTH_RADIUS_M));
    }

    @Test
    public void rejectsObstacleEqualToOuterPolygon() {
        GeoPolygon outer = square(0.0, 0.0, 3.0, 3.0);
        GeoPolygon same = square(0.0, 0.0, 3.0, 3.0);

        assertThrows(IllegalArgumentException.class,
                () -> new PlayArea(outer, Collections.singletonList(same), EARTH_RADIUS_M));
    }

    @Test
    public void rejectsOverlappingObstacles() {
        GeoPolygon outer = square(0.0, 0.0, 4.0, 4.0);
        GeoPolygon first = square(0.5, 0.5, 2.0, 2.0);
        GeoPolygon second = square(1.5, 1.5, 3.0, 3.0);

        assertThrows(IllegalArgumentException.class,
                () -> new PlayArea(outer, Arrays.asList(first, second), EARTH_RADIUS_M));
    }

    @Test
    public void rejectsPartiallyOverlappingCollinearObstacleEdges() {
        GeoPolygon outer = square(0.0, 0.0, 4.0, 4.0);
        // Même plage de latitude : aucun sommet n'est strictement dans l'autre,
        // mais les bords inférieurs/supérieurs se superposent partiellement.
        GeoPolygon first = square(0.5, 0.5, 2.0, 2.0);
        GeoPolygon second = square(0.5, 1.5, 2.0, 3.0);

        assertThrows(IllegalArgumentException.class,
                () -> new PlayArea(outer, Arrays.asList(first, second), EARTH_RADIUS_M));
    }

    @Test
    public void acceptsObstaclesThatOnlyShareAVertexOrAWholeEdge() {
        GeoPolygon outer = square(0.0, 0.0, 4.0, 4.0);

        PlayArea sharedVertex = new PlayArea(outer, Arrays.asList(
                square(0.5, 0.5, 2.0, 2.0),
                square(2.0, 2.0, 3.5, 3.5)), EARTH_RADIUS_M);
        PlayArea sharedEdge = new PlayArea(outer, Arrays.asList(
                square(0.5, 0.5, 2.0, 2.0),
                square(0.5, 2.0, 2.0, 3.5)), EARTH_RADIUS_M);

        assertEquals(2, sharedVertex.getObstacles().size());
        assertEquals(2, sharedEdge.getObstacles().size());
        assertFalse(sharedEdge.contains(new LatLon(1.0, 1.0)));
        assertFalse(sharedEdge.contains(new LatLon(1.0, 2.5)));
        assertTrue(sharedEdge.contains(new LatLon(3.0, 3.0)));
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

    private static GeoPolygon square(double minLat, double minLon,
                                     double maxLat, double maxLon) {
        return polygon(
                new LatLon(minLat, minLon),
                new LatLon(minLat, maxLon),
                new LatLon(maxLat, maxLon),
                new LatLon(maxLat, minLon)
        );
    }
}
