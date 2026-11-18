package com.example.ghostradar.domain.geo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Polygone géographique fermé, validé à la construction.
 *
 * <p>Les seuils de validation sont fournis par l'appelant afin de rester
 * alignés sur les constantes configurables de {@code GameConfig}.</p>
 */
public final class GeoPolygon {

    private static final double ORIENTATION_EPSILON = 1e-12;

    private final List<LatLon> vertices;
    private final double earthRadiusM;

    public GeoPolygon(List<LatLon> vertices, int minVertices, double minVertexDistanceM,
                      double earthRadiusM) {
        if (vertices == null) {
            throw new IllegalArgumentException("vertices must not be null");
        }
        if (minVertices < 3) {
            throw new IllegalArgumentException("minVertices must be at least 3");
        }
        if (vertices.size() < minVertices) {
            throw new IllegalArgumentException("A polygon needs at least " + minVertices + " vertices");
        }
        if (earthRadiusM <= 0.0) {
            throw new IllegalArgumentException("earthRadiusM must be positive");
        }

        List<LatLon> copy = new ArrayList<>(vertices.size());
        for (LatLon vertex : vertices) {
            if (vertex == null) {
                throw new IllegalArgumentException("vertices must not contain null");
            }
            copy.add(vertex);
        }

        validateNoDuplicateVertices(copy, minVertexDistanceM, earthRadiusM);
        validateNoSelfIntersection(copy);

        this.vertices = Collections.unmodifiableList(copy);
        this.earthRadiusM = earthRadiusM;
    }

    public List<LatLon> getVertices() {
        return vertices;
    }

    public double getEarthRadiusM() {
        return earthRadiusM;
    }

    public boolean contains(LatLon point) {
        return GeoMath.pointInPolygon(point, vertices);
    }

    /**
     * Sommet le plus proche du point demandé.
     */
    public LatLon nearestVertex(LatLon point) {
        LatLon nearest = vertices.get(0);
        double bestDistance = Double.MAX_VALUE;

        for (LatLon vertex : vertices) {
            double distance = GeoMath.haversineDistanceMeters(point, vertex, earthRadiusM);
            if (distance < bestDistance) {
                bestDistance = distance;
                nearest = vertex;
            }
        }

        return nearest;
    }

    /**
     * Point du contour le plus proche du point demandé.
     */
    public LatLon closestPointOnBoundary(LatLon point) {
        LatLon closest = vertices.get(0);
        double bestDistance = Double.MAX_VALUE;

        for (int i = 0; i < vertices.size(); i++) {
            LatLon start = vertices.get(i);
            LatLon end = vertices.get((i + 1) % vertices.size());
            LatLon candidate = closestPointOnSegment(point, start, end);
            double distance = GeoMath.haversineDistanceMeters(point, candidate, earthRadiusM);
            if (distance < bestDistance) {
                bestDistance = distance;
                closest = candidate;
            }
        }

        return closest;
    }

    private LatLon closestPointOnSegment(LatLon point, LatLon start, LatLon end) {
        double referenceLatitudeRad = Math.toRadians(
                (point.getLatitude() + start.getLatitude() + end.getLatitude()) / 3.0);
        double cosReferenceLatitude = Math.cos(referenceLatitudeRad);

        double dx = (end.getLongitude() - start.getLongitude()) * cosReferenceLatitude;
        double dy = end.getLatitude() - start.getLatitude();
        double px = (point.getLongitude() - start.getLongitude()) * cosReferenceLatitude;
        double py = point.getLatitude() - start.getLatitude();

        double lengthSquared = dx * dx + dy * dy;
        if (lengthSquared == 0.0) {
            return start;
        }

        double t = (px * dx + py * dy) / lengthSquared;
        double clampedT = Math.min(1.0, Math.max(0.0, t));

        double latitude = start.getLatitude() + clampedT * dy;
        double longitude = start.getLongitude()
                + clampedT * (end.getLongitude() - start.getLongitude());
        return new LatLon(latitude, longitude);
    }

    private static void validateNoDuplicateVertices(List<LatLon> vertices,
                                                    double minVertexDistanceM,
                                                    double earthRadiusM) {
        for (int i = 0; i < vertices.size(); i++) {
            for (int j = i + 1; j < vertices.size(); j++) {
                double distance = GeoMath.haversineDistanceMeters(
                        vertices.get(i), vertices.get(j), earthRadiusM);
                if (distance < minVertexDistanceM) {
                    throw new IllegalArgumentException("Vertices " + i + " and " + j
                            + " are closer than " + minVertexDistanceM + " m");
                }
            }
        }
    }

    private static void validateNoSelfIntersection(List<LatLon> vertices) {
        int vertexCount = vertices.size();

        for (int i = 0; i < vertexCount; i++) {
            int iNext = (i + 1) % vertexCount;
            for (int j = i + 1; j < vertexCount; j++) {
                int jNext = (j + 1) % vertexCount;
                if (iNext == j || jNext == i) {
                    continue;
                }
                if (segmentsIntersect(vertices.get(i), vertices.get(iNext),
                        vertices.get(j), vertices.get(jNext))) {
                    throw new IllegalArgumentException("Polygon edges " + i + " and " + j
                            + " intersect");
                }
            }
        }
    }

    private static boolean segmentsIntersect(LatLon a, LatLon b, LatLon c, LatLon d) {
        double o1 = orientation(a, b, c);
        double o2 = orientation(a, b, d);
        double o3 = orientation(c, d, a);
        double o4 = orientation(c, d, b);

        if (Math.abs(o1) <= ORIENTATION_EPSILON && onSegment(a, b, c)) {
            return true;
        }
        if (Math.abs(o2) <= ORIENTATION_EPSILON && onSegment(a, b, d)) {
            return true;
        }
        if (Math.abs(o3) <= ORIENTATION_EPSILON && onSegment(c, d, a)) {
            return true;
        }
        if (Math.abs(o4) <= ORIENTATION_EPSILON && onSegment(c, d, b)) {
            return true;
        }

        return ((o1 > ORIENTATION_EPSILON && o2 < -ORIENTATION_EPSILON)
                || (o1 < -ORIENTATION_EPSILON && o2 > ORIENTATION_EPSILON))
                && ((o3 > ORIENTATION_EPSILON && o4 < -ORIENTATION_EPSILON)
                || (o3 < -ORIENTATION_EPSILON && o4 > ORIENTATION_EPSILON));
    }

    private static double orientation(LatLon a, LatLon b, LatLon c) {
        return (b.getLongitude() - a.getLongitude()) * (c.getLatitude() - a.getLatitude())
                - (b.getLatitude() - a.getLatitude()) * (c.getLongitude() - a.getLongitude());
    }

    private static boolean onSegment(LatLon a, LatLon b, LatLon point) {
        double minLon = Math.min(a.getLongitude(), b.getLongitude());
        double maxLon = Math.max(a.getLongitude(), b.getLongitude());
        double minLat = Math.min(a.getLatitude(), b.getLatitude());
        double maxLat = Math.max(a.getLatitude(), b.getLatitude());

        return point.getLongitude() >= minLon - ORIENTATION_EPSILON
                && point.getLongitude() <= maxLon + ORIENTATION_EPSILON
                && point.getLatitude() >= minLat - ORIENTATION_EPSILON
                && point.getLatitude() <= maxLat + ORIENTATION_EPSILON;
    }
}
