package com.example.ghostradar.domain.geo;

import java.util.List;
import java.util.Objects;

/**
 * Projection locale ENU (est, nord, mètres) pour le rendu de la carte.
 *
 * <p>L'origine est le centre de la boîte englobante du polygone extérieur de
 * la zone jouable. Les coordonnées projetées sont exprimées en mètres :
 * {@code x} vers l'est et {@code y} vers le nord. La couche UI inverse {@code y}
 * pour l'affichage {@code Canvas}.</p>
 *
 * <p>L'approximation équirectangulaire locale est adaptée à une zone de jeu de
 * quelques centaines de mètres et reste indépendante d'Android.</p>
 */
public final class MapProjection {

    private final LatLon origin;
    private final double earthRadiusM;
    private final double cosOriginLatitudeRad;

    /**
     * Construit une projection centrée sur la zone jouable.
     *
     * @throws IllegalArgumentException si {@code playArea} est nul
     */
    public MapProjection(PlayArea playArea) {
        if (playArea == null) {
            throw new IllegalArgumentException("playArea must not be null");
        }
        this.earthRadiusM = playArea.getEarthRadiusM();
        this.origin = boundingBoxCenter(playArea.getOuterPolygon());
        this.cosOriginLatitudeRad = Math.cos(Math.toRadians(origin.getLatitude()));
    }

    public LatLon getOrigin() {
        return origin;
    }

    /**
     * Projette une position géographique en mètres ENU.
     *
     * @throws IllegalArgumentException si {@code position} est nul
     */
    public MapPoint project(LatLon position) {
        if (position == null) {
            throw new IllegalArgumentException("position must not be null");
        }
        double xM = earthRadiusM
                * Math.toRadians(position.getLongitude() - origin.getLongitude())
                * cosOriginLatitudeRad;
        double yM = earthRadiusM
                * Math.toRadians(position.getLatitude() - origin.getLatitude());
        return new MapPoint(xM, yM);
    }

    /**
     * Convertit des mètres ENU en position géographique.
     *
     * @throws IllegalArgumentException si une coordonnée est NaN ou infinie
     */
    public LatLon unproject(double xM, double yM) {
        if (!isFinite(xM) || !isFinite(yM)) {
            throw new IllegalArgumentException("coordinates must be finite");
        }
        double latitude = origin.getLatitude() + Math.toDegrees(yM / earthRadiusM);
        double longitude = origin.getLongitude()
                + Math.toDegrees(xM / (earthRadiusM * cosOriginLatitudeRad));
        return new LatLon(latitude, longitude);
    }

    private static LatLon boundingBoxCenter(GeoPolygon polygon) {
        List<LatLon> vertices = polygon.getVertices();
        double minLatitude = Double.POSITIVE_INFINITY;
        double maxLatitude = Double.NEGATIVE_INFINITY;
        double minLongitude = Double.POSITIVE_INFINITY;
        double maxLongitude = Double.NEGATIVE_INFINITY;

        for (LatLon vertex : vertices) {
            minLatitude = Math.min(minLatitude, vertex.getLatitude());
            maxLatitude = Math.max(maxLatitude, vertex.getLatitude());
            minLongitude = Math.min(minLongitude, vertex.getLongitude());
            maxLongitude = Math.max(maxLongitude, vertex.getLongitude());
        }

        return new LatLon((minLatitude + maxLatitude) / 2.0,
                (minLongitude + maxLongitude) / 2.0);
    }

    private static boolean isFinite(double value) {
        return !Double.isNaN(value) && !Double.isInfinite(value);
    }

    /**
     * Coordonnée projetée en mètres, {@code x} vers l'est et {@code y} vers le
     * nord.
     */
    public static final class MapPoint {

        private final double xM;
        private final double yM;

        private MapPoint(double xM, double yM) {
            this.xM = xM;
            this.yM = yM;
        }

        public double getXM() {
            return xM;
        }

        public double getYM() {
            return yM;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof MapPoint)) {
                return false;
            }
            MapPoint that = (MapPoint) other;
            return Double.compare(that.xM, xM) == 0
                    && Double.compare(that.yM, yM) == 0;
        }

        @Override
        public int hashCode() {
            return Objects.hash(xM, yM);
        }

        @Override
        public String toString() {
            return "MapPoint{xM=" + xM + ", yM=" + yM + '}';
        }
    }
}
