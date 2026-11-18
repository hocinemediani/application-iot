package com.example.ghostradar.domain.geo;

import java.util.List;

/**
 * Calculs géométriques et géodésiques en Java pur.
 *
 * <p>Le rayon terrestre est fourni par l'appelant afin de rester aligné sur
 * la valeur configurable {@code EARTH_RADIUS_M} de {@code GameConfig}.</p>
 */
public final class GeoMath {

    private GeoMath() {
    }

    /**
     * Distance orthodromique en mètres (formule de Haversine).
     */
    public static double haversineDistanceMeters(LatLon from, LatLon to, double earthRadiusM) {
        double lat1 = Math.toRadians(from.getLatitude());
        double lat2 = Math.toRadians(to.getLatitude());
        double deltaLat = lat2 - lat1;
        double deltaLon = Math.toRadians(to.getLongitude() - from.getLongitude());

        double sinHalfLat = Math.sin(deltaLat / 2.0);
        double sinHalfLon = Math.sin(deltaLon / 2.0);
        double a = sinHalfLat * sinHalfLat
                + Math.cos(lat1) * Math.cos(lat2) * sinHalfLon * sinHalfLon;
        double clamped = Math.min(1.0, Math.max(0.0, a));

        return earthRadiusM * 2.0 * Math.atan2(Math.sqrt(clamped), Math.sqrt(1.0 - clamped));
    }

    /**
     * Azimut initial en degrés, de 0 (nord) à 360, dans le sens horaire.
     */
    public static double initialBearingDegrees(LatLon from, LatLon to) {
        double lat1 = Math.toRadians(from.getLatitude());
        double lat2 = Math.toRadians(to.getLatitude());
        double deltaLon = Math.toRadians(to.getLongitude() - from.getLongitude());

        double y = Math.sin(deltaLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2)
                - Math.sin(lat1) * Math.cos(lat2) * Math.cos(deltaLon);

        return normalizeAngleDegrees(Math.toDegrees(Math.atan2(y, x)));
    }

    /**
     * Point atteint depuis {@code from} en suivant un azimut et une distance.
     */
    public static LatLon destination(LatLon from, double bearingDegrees, double distanceMeters,
                                     double earthRadiusM) {
        double angularDistance = distanceMeters / earthRadiusM;
        double bearing = Math.toRadians(bearingDegrees);
        double lat1 = Math.toRadians(from.getLatitude());
        double lon1 = Math.toRadians(from.getLongitude());

        double sinLat1 = Math.sin(lat1);
        double cosLat1 = Math.cos(lat1);
        double sinAngular = Math.sin(angularDistance);
        double cosAngular = Math.cos(angularDistance);

        double lat2 = Math.asin(sinLat1 * cosAngular
                + cosLat1 * sinAngular * Math.cos(bearing));
        double lon2 = lon1 + Math.atan2(
                Math.sin(bearing) * sinAngular * cosLat1,
                cosAngular - sinLat1 * Math.sin(lat2));

        return new LatLon(Math.toDegrees(lat2),
                normalizeLongitudeDegrees(Math.toDegrees(lon2)));
    }

    /**
     * Normalise un angle en degrés dans l'intervalle [0, 360).
     */
    public static double normalizeAngleDegrees(double angleDegrees) {
        double normalized = angleDegrees % 360.0;
        if (normalized < 0.0) {
            normalized += 360.0;
        }
        return normalized;
    }

    /**
     * Test d'appartenance d'un point à un polygone fermé (lancer de rayon).
     *
     * <p>Approximation planaire locale en degrés, adaptée à un périmètre de jeu
     * de quelques centaines de mètres.</p>
     */
    public static boolean pointInPolygon(LatLon point, List<LatLon> polygon) {
        if (point == null || polygon == null || polygon.size() < 3) {
            return false;
        }

        double x = point.getLongitude();
        double y = point.getLatitude();
        boolean inside = false;

        for (int i = 0, j = polygon.size() - 1; i < polygon.size(); j = i++) {
            LatLon current = polygon.get(i);
            LatLon previous = polygon.get(j);
            double xi = current.getLongitude();
            double yi = current.getLatitude();
            double xj = previous.getLongitude();
            double yj = previous.getLatitude();

            boolean crosses = (yi > y) != (yj > y)
                    && x < (xj - xi) * (y - yi) / (yj - yi) + xi;
            if (crosses) {
                inside = !inside;
            }
        }

        return inside;
    }

    /**
     * Distance minimale en mètres entre un point et un segment géographique.
     *
     * <p>Projection équirectangulaire locale pour travailler en mètres, puis
     * projection du point sur le segment et clamp aux extrémités.</p>
     */
    public static double distancePointToSegmentMeters(LatLon point, LatLon segmentStart,
                                                      LatLon segmentEnd, double earthRadiusM) {
        double referenceLatitudeRad = Math.toRadians(
                (point.getLatitude() + segmentStart.getLatitude() + segmentEnd.getLatitude()) / 3.0);
        double metersPerDegreeLat = earthRadiusM * Math.PI / 180.0;
        double metersPerDegreeLon = metersPerDegreeLat * Math.cos(referenceLatitudeRad);

        double px = point.getLongitude() * metersPerDegreeLon;
        double py = point.getLatitude() * metersPerDegreeLat;
        double ax = segmentStart.getLongitude() * metersPerDegreeLon;
        double ay = segmentStart.getLatitude() * metersPerDegreeLat;
        double bx = segmentEnd.getLongitude() * metersPerDegreeLon;
        double by = segmentEnd.getLatitude() * metersPerDegreeLat;

        double dx = bx - ax;
        double dy = by - ay;
        double lengthSquared = dx * dx + dy * dy;

        if (lengthSquared == 0.0) {
            return Math.hypot(px - ax, py - ay);
        }

        double t = ((px - ax) * dx + (py - ay) * dy) / lengthSquared;
        double clampedT = Math.min(1.0, Math.max(0.0, t));
        double closestX = ax + clampedT * dx;
        double closestY = ay + clampedT * dy;

        return Math.hypot(px - closestX, py - closestY);
    }

    private static double normalizeLongitudeDegrees(double longitudeDegrees) {
        double normalized = (longitudeDegrees + 180.0) % 360.0;
        if (normalized < 0.0) {
            normalized += 360.0;
        }
        return normalized - 180.0;
    }
}
