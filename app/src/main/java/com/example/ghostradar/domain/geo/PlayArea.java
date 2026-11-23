package com.example.ghostradar.domain.geo;

import com.example.ghostradar.domain.Validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * Zone jouable composée d'un polygone extérieur et de polygones d'obstacles.
 *
 * <p>Le clamp projette un point invalide sur la frontière la plus proche :
 * limite extérieure si le point sort de la zone, frontière de l'obstacle s'il
 * entre dans un trou. Ce point frontière permet au déplacement suivant de
 * glisser le long du bord.</p>
 *
 * <p>La topologie est vérifiée à la construction (B4) : chaque obstacle est
 * strictement à l'intérieur du polygone extérieur, aucun obstacle n'est égal à
 * l'extérieur, et les intérieurs des obstacles ne se chevauchent pas. Deux
 * obstacles peuvent en revanche partager un sommet ou un bord entier (trous
 * adjacents). Ces garanties évitent que le clamp projette sur une position
 * encore invalide ou fasse osciller le fantôme entre deux projections.</p>
 */
public final class PlayArea {

    /** Tolérance métrique pour considérer un point comme posé sur un bord. */
    private static final double BOUNDARY_TOLERANCE_M = 1e-6;

    private final GeoPolygon outerPolygon;
    private final List<GeoPolygon> obstacles;
    private final double earthRadiusM;

    public PlayArea(GeoPolygon outerPolygon, List<GeoPolygon> obstacles, double earthRadiusM) {
        if (outerPolygon == null) {
            throw new IllegalArgumentException("outerPolygon must not be null");
        }
        if (obstacles == null) {
            throw new IllegalArgumentException("obstacles must not be null");
        }
        Validation.requirePositiveFinite("earthRadiusM", earthRadiusM);
        Validation.requireSameRadius("earthRadiusM (outerPolygon)",
                earthRadiusM, outerPolygon.getEarthRadiusM());

        List<GeoPolygon> copy = new ArrayList<>(obstacles.size());
        for (GeoPolygon obstacle : obstacles) {
            if (obstacle == null) {
                throw new IllegalArgumentException("obstacles must not contain null");
            }
            Validation.requireSameRadius("earthRadiusM (obstacle)",
                    earthRadiusM, obstacle.getEarthRadiusM());
            copy.add(obstacle);
        }
        validateTopology(outerPolygon, copy, earthRadiusM);

        this.outerPolygon = outerPolygon;
        this.obstacles = Collections.unmodifiableList(copy);
        this.earthRadiusM = earthRadiusM;
    }

    public GeoPolygon getOuterPolygon() {
        return outerPolygon;
    }

    public List<GeoPolygon> getObstacles() {
        return obstacles;
    }

    public double getEarthRadiusM() {
        return earthRadiusM;
    }

    public boolean contains(LatLon point) {
        if (point == null || !outerPolygon.contains(point)) {
            return false;
        }
        for (GeoPolygon obstacle : obstacles) {
            if (obstacle.contains(point)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Ramène un point dans la zone jouable en le projetant sur la frontière
     * qui le bloque, ce qui autorise le glissement le long des bords.
     */
    public LatLon clamp(LatLon point) {
        if (point == null) {
            throw new IllegalArgumentException("point must not be null");
        }
        if (contains(point)) {
            return point;
        }
        if (!outerPolygon.contains(point)) {
            return outerPolygon.closestPointOnBoundary(point);
        }

        LatLon closest = null;
        double bestDistance = Double.MAX_VALUE;
        for (GeoPolygon obstacle : obstacles) {
            if (obstacle.contains(point)) {
                LatLon candidate = obstacle.closestPointOnBoundary(point);
                double distance = GeoMath.haversineDistanceMeters(point, candidate, earthRadiusM);
                if (distance < bestDistance) {
                    bestDistance = distance;
                    closest = candidate;
                }
            }
        }

        if (closest != null) {
            return closest;
        }
        return outerPolygon.closestPointOnBoundary(point);
    }

    private static void validateTopology(GeoPolygon outer, List<GeoPolygon> obstacles,
                                         double earthRadiusM) {
        for (GeoPolygon obstacle : obstacles) {
            validateStrictlyInsideOuter(outer, obstacle);
        }
        for (int i = 0; i < obstacles.size(); i++) {
            for (int j = i + 1; j < obstacles.size(); j++) {
                validateObstaclesDisjoint(obstacles.get(i), obstacles.get(j), earthRadiusM);
            }
        }
    }

    private static void validateStrictlyInsideOuter(GeoPolygon outer, GeoPolygon obstacle) {
        List<LatLon> outerVertices = outer.getVertices();
        List<LatLon> obstacleVertices = obstacle.getVertices();

        for (LatLon vertex : obstacleVertices) {
            if (!outer.contains(vertex)) {
                throw new IllegalArgumentException(
                        "Obstacle vertex lies outside the outer polygon: " + vertex);
            }
        }
        // Un sommet posé sur la frontière peut être considéré comme dedans par
        // le lancer de rayon : contrôler aussi les bords, contact compris.
        for (int i = 0; i < obstacleVertices.size(); i++) {
            LatLon a = obstacleVertices.get(i);
            LatLon b = obstacleVertices.get((i + 1) % obstacleVertices.size());
            for (int j = 0; j < outerVertices.size(); j++) {
                LatLon c = outerVertices.get(j);
                LatLon d = outerVertices.get((j + 1) % outerVertices.size());
                if (GeoPolygon.segmentsIntersect(a, b, c, d)) {
                    throw new IllegalArgumentException(
                            "Obstacle must be strictly inside the outer polygon");
                }
            }
        }
    }

    private static void validateObstaclesDisjoint(GeoPolygon first, GeoPolygon second,
                                                  double earthRadiusM) {
        if (new HashSet<>(first.getVertices()).equals(new HashSet<>(second.getVertices()))) {
            throw new IllegalArgumentException("Obstacles must not be equal");
        }
        for (LatLon vertex : first.getVertices()) {
            if (isStrictlyInside(second, vertex, earthRadiusM)) {
                throw new IllegalArgumentException("Obstacles must not overlap");
            }
        }
        for (LatLon vertex : second.getVertices()) {
            if (isStrictlyInside(first, vertex, earthRadiusM)) {
                throw new IllegalArgumentException("Obstacles must not overlap");
            }
        }
        List<LatLon> firstVertices = first.getVertices();
        List<LatLon> secondVertices = second.getVertices();
        for (int i = 0; i < firstVertices.size(); i++) {
            LatLon a = firstVertices.get(i);
            LatLon b = firstVertices.get((i + 1) % firstVertices.size());
            for (int j = 0; j < secondVertices.size(); j++) {
                LatLon c = secondVertices.get(j);
                LatLon d = secondVertices.get((j + 1) % secondVertices.size());
                if (properlyCross(a, b, c, d) || partiallyOverlapCollinearly(a, b, c, d)) {
                    throw new IllegalArgumentException("Obstacles must not overlap");
                }
            }
        }
    }

    private static boolean isStrictlyInside(GeoPolygon polygon, LatLon point,
                                            double earthRadiusM) {
        return polygon.contains(point) && !liesOnBoundary(polygon, point, earthRadiusM);
    }

    private static boolean liesOnBoundary(GeoPolygon polygon, LatLon point,
                                          double earthRadiusM) {
        List<LatLon> vertices = polygon.getVertices();
        for (int i = 0; i < vertices.size(); i++) {
            LatLon start = vertices.get(i);
            LatLon end = vertices.get((i + 1) % vertices.size());
            if (GeoMath.distancePointToSegmentMeters(point, start, end, earthRadiusM)
                    <= BOUNDARY_TOLERANCE_M) {
                return true;
            }
        }
        return false;
    }

    /** Croisement franc : exclut les contacts par extrémité ou les bords confondus. */
    private static boolean properlyCross(LatLon a, LatLon b, LatLon c, LatLon d) {
        double epsilon = GeoPolygon.ORIENTATION_EPSILON;
        double o1 = GeoPolygon.orientation(a, b, c);
        double o2 = GeoPolygon.orientation(a, b, d);
        double o3 = GeoPolygon.orientation(c, d, a);
        double o4 = GeoPolygon.orientation(c, d, b);
        return ((o1 > epsilon && o2 < -epsilon) || (o1 < -epsilon && o2 > epsilon))
                && ((o3 > epsilon && o4 < -epsilon) || (o3 < -epsilon && o4 > epsilon));
    }

    /**
     * Bords colinéaires dont les parties communes dépassent un simple contact.
     * Un bord entier exactement partagé reste autorisé (trous adjacents).
     */
    private static boolean partiallyOverlapCollinearly(LatLon a, LatLon b,
                                                       LatLon c, LatLon d) {
        double epsilon = GeoPolygon.ORIENTATION_EPSILON;
        if (Math.abs(GeoPolygon.orientation(a, b, c)) > epsilon
                || Math.abs(GeoPolygon.orientation(a, b, d)) > epsilon) {
            return false;
        }
        boolean useLatitude = Math.abs(b.getLatitude() - a.getLatitude())
                > Math.abs(b.getLongitude() - a.getLongitude());
        double a1 = useLatitude ? a.getLatitude() : a.getLongitude();
        double a2 = useLatitude ? b.getLatitude() : b.getLongitude();
        double c1 = useLatitude ? c.getLatitude() : c.getLongitude();
        double c2 = useLatitude ? d.getLatitude() : d.getLongitude();
        double overlap = Math.min(Math.max(a1, a2), Math.max(c1, c2))
                - Math.max(Math.min(a1, a2), Math.min(c1, c2));
        if (overlap <= epsilon) {
            return false;
        }
        return !(a.equals(c) && b.equals(d)) && !(a.equals(d) && b.equals(c));
    }
}
