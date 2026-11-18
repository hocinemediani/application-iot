package com.example.ghostradar.domain.geo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Zone jouable composée d'un polygone extérieur et de polygones d'obstacles.
 *
 * <p>Le clamp projette un point invalide sur la frontière la plus proche :
 * limite extérieure si le point sort de la zone, frontière de l'obstacle s'il
 * entre dans un trou. Ce point frontière permet au déplacement suivant de
 * glisser le long du bord.</p>
 */
public final class PlayArea {

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
        if (earthRadiusM <= 0.0) {
            throw new IllegalArgumentException("earthRadiusM must be positive");
        }

        List<GeoPolygon> copy = new ArrayList<>(obstacles.size());
        for (GeoPolygon obstacle : obstacles) {
            if (obstacle == null) {
                throw new IllegalArgumentException("obstacles must not contain null");
            }
            copy.add(obstacle);
        }

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
}
