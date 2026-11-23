package com.example.ghostradar.domain.geo;

import com.example.ghostradar.domain.Validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Polygone géographique fermé, validé à la construction.
 *
 * <p>Les seuils de validation sont fournis par l'appelant afin de rester
 * alignés sur les constantes configurables de {@code GameConfig}. Sont rejetés
 * : les sommets trop proches, les auto-intersections et les polygones
 * dégénérés (tous les sommets colinéaires, donc d'aire nulle). Toutes les
 * méthodes interrogeant un point lèvent {@link IllegalArgumentException} pour
 * un argument {@code null}, comme le reste du domaine.</p>
 */
public final class GeoPolygon {

    /**
     * Seuil d'alignement en degrés², partagé avec {@link PlayArea} pour la
     * validation de la topologie des obstacles.
     */
    static final double ORIENTATION_EPSILON = 1e-12;

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
        Validation.requirePositiveFinite("earthRadiusM", earthRadiusM);
        Validation.requirePositiveFinite("minVertexDistanceM", minVertexDistanceM);

        List<LatLon> copy = new ArrayList<>(vertices.size());
        for (LatLon vertex : vertices) {
            if (vertex == null) {
                throw new IllegalArgumentException("vertices must not contain null");
            }
            copy.add(vertex);
        }

        validateNoDuplicateVertices(copy, minVertexDistanceM, earthRadiusM);
        validateNoSelfIntersection(copy);
        validateNonDegenerate(copy);

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
        Validation.requireNonNull("point", point);
        return GeoMath.pointInPolygon(point, vertices);
    }

    /**
     * Sommet le plus proche du point demandé.
     */
    public LatLon nearestVertex(LatLon point) {
        Validation.requireNonNull("point", point);
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
        Validation.requireNonNull("point", point);
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

    /**
     * Rejette les polygones dont tous les sommets sont alignés : leur aire est
     * nulle et {@link #contains(LatLon)} ne pourrait jamais renvoyer vrai.
     *
     * <p>Deux sommets quelconques A et B sont distincts (garanti par le
     * contrôle des doublons), donc il suffit de vérifier que chaque autre
     * sommet est aligné avec eux. Le seuil est le même que celui des tests
     * d'intersection, exprimé en degrés².</p>
     */
    private static void validateNonDegenerate(List<LatLon> vertices) {
        LatLon a = vertices.get(0);
        LatLon b = vertices.get(1);
        for (int i = 2; i < vertices.size(); i++) {
            if (Math.abs(orientation(a, b, vertices.get(i))) > ORIENTATION_EPSILON) {
                return;
            }
        }
        throw new IllegalArgumentException("Polygon vertices are collinear (zero area)");
    }

    /** Intersection de deux segments, contact compris (réutilisé par {@link PlayArea}). */
    static boolean segmentsIntersect(LatLon a, LatLon b, LatLon c, LatLon d) {
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

    /** Orientation de trois points en degrés² (réutilisé par {@link PlayArea}). */
    static double orientation(LatLon a, LatLon b, LatLon c) {
        return (b.getLongitude() - a.getLongitude()) * (c.getLatitude() - a.getLatitude())
                - (b.getLatitude() - a.getLatitude()) * (c.getLongitude() - a.getLongitude());
    }

    static boolean onSegment(LatLon a, LatLon b, LatLon point) {
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
