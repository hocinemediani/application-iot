package com.example.ghostradar.domain.geo;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class GeoPolygonTest {

    private static final double EARTH_RADIUS_M = 6_371_000.0;
    private static final double MIN_VERTEX_DISTANCE_M = 0.5;

    @Test
    public void rejectsPolygonWithFewerThanThreeVertices() {
        List<LatLon> vertices = Arrays.asList(
                new LatLon(0.0, 0.0),
                new LatLon(0.0, 1.0)
        );

        assertThrows(IllegalArgumentException.class, () -> polygon(vertices));
    }

    @Test
    public void rejectsDuplicateVerticesCloserThanHalfAMeter() {
        double almostZeroLon = 0.4 / (EARTH_RADIUS_M * Math.PI / 180.0);
        List<LatLon> vertices = Arrays.asList(
                new LatLon(0.0, 0.0),
                new LatLon(0.0, almostZeroLon),
                new LatLon(1.0, almostZeroLon),
                new LatLon(1.0, 0.0)
        );

        assertThrows(IllegalArgumentException.class, () -> polygon(vertices));
    }

    @Test
    public void acceptsValidPolygon() {
        GeoPolygon polygon = polygon(square());

        assertEquals(4, polygon.getVertices().size());
        assertEquals(new LatLon(0.0, 0.0), polygon.getVertices().get(0));
    }

    @Test
    public void copiesVerticesDefensively() {
        List<LatLon> source = new ArrayList<>(square());
        GeoPolygon polygon = polygon(source);

        source.clear();

        assertEquals(4, polygon.getVertices().size());
        assertThrows(UnsupportedOperationException.class,
                () -> polygon.getVertices().add(new LatLon(2.0, 2.0)));
    }

    @Test
    public void rejectsSelfIntersectingPolygon() {
        List<LatLon> bowtie = Arrays.asList(
                new LatLon(0.0, 0.0),
                new LatLon(1.0, 1.0),
                new LatLon(0.0, 1.0),
                new LatLon(1.0, 0.0)
        );

        assertThrows(IllegalArgumentException.class, () -> polygon(bowtie));
    }

    @Test
    public void containsDetectsInsideAndOutside() {
        GeoPolygon polygon = polygon(square());

        assertTrue(polygon.contains(new LatLon(0.5, 0.5)));
        assertFalse(polygon.contains(new LatLon(1.5, 0.5)));
        assertFalse(polygon.contains(new LatLon(-0.5, 0.5)));
    }

    @Test
    public void acceptsConcavePolygon() {
        List<LatLon> concave = Arrays.asList(
                new LatLon(0.0, 0.0),
                new LatLon(0.0, 2.0),
                new LatLon(1.0, 1.0),
                new LatLon(2.0, 2.0),
                new LatLon(2.0, 0.0)
        );

        GeoPolygon polygon = polygon(concave);

        assertTrue(polygon.contains(new LatLon(0.5, 0.5)));
        assertFalse(polygon.contains(new LatLon(1.0, 1.9)));
    }

    @Test
    public void nearestVertexReturnsClosestVertex() {
        GeoPolygon polygon = polygon(square());

        assertEquals(new LatLon(0.0, 0.0), polygon.nearestVertex(new LatLon(0.1, 0.1)));
        assertEquals(new LatLon(1.0, 1.0), polygon.nearestVertex(new LatLon(0.9, 0.9)));
    }

    @Test
    public void closestPointOnBoundaryProjectsOntoEdges() {
        GeoPolygon polygon = polygon(square());

        assertEquals(new LatLon(1.0, 0.5), polygon.closestPointOnBoundary(new LatLon(1.5, 0.5)));
        assertEquals(new LatLon(0.5, 1.0), polygon.closestPointOnBoundary(new LatLon(0.5, 1.5)));
        assertEquals(new LatLon(1.0, 1.0), polygon.closestPointOnBoundary(new LatLon(1.2, 1.2)));
        assertEquals(new LatLon(0.0, 0.5), polygon.closestPointOnBoundary(new LatLon(0.0, 0.5)));
    }

    @Test
    public void rejectsCollinearDegeneratePolygon() {
        // Le point milieu est entre les deux autres : toutes les paires
        // d'arêtes sont adjacentes, donc l'auto-intersection ne le voit pas,
        // mais le polygone a une aire nulle et contains() renvoie toujours faux.
        List<LatLon> collinear = Arrays.asList(
                new LatLon(0.0, 0.0),
                new LatLon(0.0, 1.0),
                new LatLon(0.0, 2.0)
        );

        assertThrows(IllegalArgumentException.class, () -> polygon(collinear));
    }

    @Test
    public void acceptsThinButNonDegeneratePolygon() {
        // Garde-fou : une aire faible mais non nulle ne doit pas être confondue
        // avec un polygone colinéaire.
        List<LatLon> thin = Arrays.asList(
                new LatLon(0.0, 0.0),
                new LatLon(0.0, 2.0),
                new LatLon(1e-6, 1.0)
        );

        GeoPolygon polygon = polygon(thin);

        assertTrue(polygon.contains(new LatLon(2e-7, 1.0)));
    }

    @Test
    public void pointQueriesRejectNullWithIllegalArgumentException() {
        GeoPolygon polygon = polygon(square());

        assertThrows(IllegalArgumentException.class, () -> polygon.contains(null));
        assertThrows(IllegalArgumentException.class, () -> polygon.nearestVertex(null));
        assertThrows(IllegalArgumentException.class,
                () -> polygon.closestPointOnBoundary(null));
    }

    @Test
    public void rejectsNonFiniteRadiusAndInvalidMinimumVertexDistance() {
        assertThrows(IllegalArgumentException.class,
                () -> new GeoPolygon(square(), 3, MIN_VERTEX_DISTANCE_M, Double.NaN));
        assertThrows(IllegalArgumentException.class,
                () -> new GeoPolygon(square(), 3, MIN_VERTEX_DISTANCE_M,
                        Double.POSITIVE_INFINITY));
        assertThrows(IllegalArgumentException.class,
                () -> new GeoPolygon(square(), 3, Double.NaN, EARTH_RADIUS_M));
        assertThrows(IllegalArgumentException.class,
                () -> new GeoPolygon(square(), 3, -1.0, EARTH_RADIUS_M));
    }

    private static GeoPolygon polygon(List<LatLon> vertices) {
        return new GeoPolygon(vertices, 3, MIN_VERTEX_DISTANCE_M, EARTH_RADIUS_M);
    }

    private static List<LatLon> square() {
        return Arrays.asList(
                new LatLon(0.0, 0.0),
                new LatLon(0.0, 1.0),
                new LatLon(1.0, 1.0),
                new LatLon(1.0, 0.0)
        );
    }
}
