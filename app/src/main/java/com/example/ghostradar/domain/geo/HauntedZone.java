package com.example.ghostradar.domain.geo;

import com.example.ghostradar.domain.Validation;
import com.example.ghostradar.domain.game.RandomSource;

import java.util.ArrayList;
import java.util.List;

/**
 * Zone hantée, sous forme de polygone aléatoire ou de cercle admin.
 *
 * <p>Le cercle admin est conservé analytiquement (centre + rayon) pour éviter
 * d'introduire un nombre de sommets d'approximation non spécifié.</p>
 */
public final class HauntedZone {

    private static final double FULL_CIRCLE_RAD = Math.PI * 2.0;

    private final GeoPolygon polygon;
    private final LatLon center;
    private final double radiusM;
    private final double earthRadiusM;

    private HauntedZone(GeoPolygon polygon, LatLon center, double radiusM, double earthRadiusM) {
        this.polygon = polygon;
        this.center = center;
        this.radiusM = radiusM;
        this.earthRadiusM = earthRadiusM;
    }

    public static HauntedZone polygon(GeoPolygon polygon) {
        if (polygon == null) {
            throw new IllegalArgumentException("polygon must not be null");
        }
        return new HauntedZone(polygon, averageCenter(polygon.getVertices()), 0.0,
                polygon.getEarthRadiusM());
    }

    public static HauntedZone circle(LatLon center, double radiusM, double earthRadiusM) {
        if (center == null) {
            throw new IllegalArgumentException("center must not be null");
        }
        Validation.requirePositiveFinite("radiusM", radiusM);
        Validation.requirePositiveFinite("earthRadiusM", earthRadiusM);
        return new HauntedZone(null, center, radiusM, earthRadiusM);
    }

    /**
     * Génère une zone polygonale aléatoire dont le centre est tiré dans la zone
     * jouable, hors obstacles. Les sommets sont répartis régulièrement autour du
     * centre avec un rayon aléatoire dans l'intervalle demandé, ce qui garantit
     * un polygone simple.
     *
     * <p>{@code maxCenterAttempts} est un budget d'algorithme passé par
     * l'appelant, volontairement absent de {@code GameConfig} : il borne le
     * nombre de centres tirés sans changer les règles de jeu. Si aucun centre
     * valide n'est trouvé (y compris avec {@code maxCenterAttempts = 1}), la
     * méthode lève {@link IllegalStateException}.</p>
     */
    public static HauntedZone randomPolygon(PlayArea playArea, RandomSource random,
                                            int minVertices, int maxVertices,
                                            double minRadiusM, double maxRadiusM,
                                            double minVertexDistanceM,
                                            int maxCenterAttempts) {
        if (playArea == null) {
            throw new IllegalArgumentException("playArea must not be null");
        }
        if (random == null) {
            throw new IllegalArgumentException("random must not be null");
        }
        if (minVertices < 3) {
            throw new IllegalArgumentException("minVertices must be at least 3");
        }
        if (maxVertices < minVertices) {
            throw new IllegalArgumentException("maxVertices must be at least minVertices");
        }
        Validation.requirePositiveFinite("minRadiusM", minRadiusM);
        Validation.requirePositiveFinite("maxRadiusM", maxRadiusM);
        if (maxRadiusM < minRadiusM) {
            throw new IllegalArgumentException("maxRadiusM must be at least minRadiusM");
        }
        Validation.requirePositiveFinite("minVertexDistanceM", minVertexDistanceM);
        if (maxCenterAttempts <= 0) {
            throw new IllegalArgumentException("maxCenterAttempts must be positive");
        }

        double earthRadiusM = playArea.getEarthRadiusM();
        LatLon center = randomCenter(playArea, random, maxCenterAttempts);
        int vertexCount = minVertices + random.nextInt(maxVertices - minVertices + 1);
        double phaseRad = random.nextDouble() * FULL_CIRCLE_RAD;

        List<LatLon> vertices = new ArrayList<>(vertexCount);
        for (int i = 0; i < vertexCount; i++) {
            double angleRad = phaseRad + FULL_CIRCLE_RAD * i / vertexCount;
            double radiusM = minRadiusM + random.nextDouble() * (maxRadiusM - minRadiusM);
            vertices.add(GeoMath.destination(center, Math.toDegrees(angleRad), radiusM,
                    earthRadiusM));
        }

        GeoPolygon polygon = new GeoPolygon(vertices, minVertices, minVertexDistanceM,
                earthRadiusM);
        return new HauntedZone(polygon, center, 0.0, earthRadiusM);
    }

    public boolean isCircle() {
        return polygon == null;
    }

    public GeoPolygon getPolygon() {
        if (polygon == null) {
            throw new IllegalStateException("A circular haunted zone has no polygon");
        }
        return polygon;
    }

    /**
     * Centre de référence de la zone.
     *
     * <p>Pour un cercle admin, c'est le centre analytique. Pour un polygone,
     * c'est la <em>moyenne arithmétique des sommets</em> calculée à la
     * construction : ce n'est pas le centroïde de l'aire et rien ne garantit
     * qu'il appartienne au polygone (en particulier s'il est concave). Les
     * décisions d'appartenance doivent utiliser {@link #contains(LatLon)} ;
     * l'UI ne doit pas supposer que ce point est dans la zone hantée.</p>
     */
    public LatLon getCenter() {
        return center;
    }

    public double getRadiusM() {
        if (polygon != null) {
            throw new IllegalStateException("A polygonal haunted zone has no radius");
        }
        return radiusM;
    }

    /**
     * Rayon terrestre utilisé pour les calculs géodésiques de cette zone.
     *
     * <p>Pour une zone polygonale, il provient du polygone ; pour un cercle
     * admin, de la valeur fournie à {@link #circle}. C'est ce rayon que les
     * consommateurs comparent à leur propre configuration (C1).</p>
     */
    public double getEarthRadiusM() {
        return earthRadiusM;
    }

    public boolean contains(LatLon point) {
        if (point == null) {
            return false;
        }
        if (polygon != null) {
            return polygon.contains(point);
        }
        return GeoMath.haversineDistanceMeters(center, point, earthRadiusM) <= radiusM;
    }

    private static LatLon randomCenter(PlayArea playArea, RandomSource random,
                                       int maxCenterAttempts) {
        List<LatLon> outerVertices = playArea.getOuterPolygon().getVertices();
        double minLatitude = Double.POSITIVE_INFINITY;
        double maxLatitude = Double.NEGATIVE_INFINITY;
        double minLongitude = Double.POSITIVE_INFINITY;
        double maxLongitude = Double.NEGATIVE_INFINITY;

        for (LatLon vertex : outerVertices) {
            minLatitude = Math.min(minLatitude, vertex.getLatitude());
            maxLatitude = Math.max(maxLatitude, vertex.getLatitude());
            minLongitude = Math.min(minLongitude, vertex.getLongitude());
            maxLongitude = Math.max(maxLongitude, vertex.getLongitude());
        }

        double latitudeSpan = maxLatitude - minLatitude;
        double longitudeSpan = maxLongitude - minLongitude;

        for (int attempt = 0; attempt < maxCenterAttempts; attempt++) {
            LatLon candidate = new LatLon(
                    minLatitude + random.nextDouble() * latitudeSpan,
                    minLongitude + random.nextDouble() * longitudeSpan);
            if (playArea.contains(candidate)) {
                return candidate;
            }
        }

        throw new IllegalStateException("No valid haunted zone center found after "
                + maxCenterAttempts + " attempts");
    }

    private static LatLon averageCenter(List<LatLon> vertices) {
        double latitudeSum = 0.0;
        double longitudeSum = 0.0;
        for (LatLon vertex : vertices) {
            latitudeSum += vertex.getLatitude();
            longitudeSum += vertex.getLongitude();
        }
        return new LatLon(latitudeSum / vertices.size(), longitudeSum / vertices.size());
    }
}
