package com.example.ghostradar.domain.game;

import com.example.ghostradar.domain.config.GameConfig;
import com.example.ghostradar.domain.geo.GeoMath;
import com.example.ghostradar.domain.geo.HauntedZone;
import com.example.ghostradar.domain.geo.LatLon;
import com.example.ghostradar.domain.geo.PlayArea;

import java.util.List;
import java.util.Optional;

/**
 * Détecte le déclenchement d'une zone hantée et choisit le point d'apparition
 * du fantôme.
 *
 * <p>La classe ne dépend pas de la session : elle vérifie la présence du
 * joueur dans la zone, applique la probabilité par seconde, puis renvoie un
 * point valide dans la zone et dans la zone jouable, hors obstacles. Le
 * cooldown de zone reste géré par {@link GameSession}.</p>
 */
public final class GhostSpawner {

    private static final double FULL_CIRCLE_DEGREES = 360.0;

    private final GameConfig config;
    private final PlayArea playArea;
    private final RandomSource random;

    public GhostSpawner(GameConfig config, PlayArea playArea, RandomSource random) {
        if (config == null) {
            throw new IllegalArgumentException("config must not be null");
        }
        if (playArea == null) {
            throw new IllegalArgumentException("playArea must not be null");
        }
        if (random == null) {
            throw new IllegalArgumentException("random must not be null");
        }
        this.config = config;
        this.playArea = playArea;
        this.random = random;
    }

    /**
     * Tente de déclencher une apparition à partir de la position du joueur.
     *
     * <p>La probabilité par seconde est convertie en probabilité de tick avec
     * {@code 1 - (1 - pParSeconde)^deltaSecondes}. Si le tirage échoue, le
     * joueur est hors zone ou aucun point valide n'existe, le résultat est
     * vide.</p>
     *
     * @param playerPosition position actuelle du joueur
     * @param zone           zone hantée concernée
     * @param deltaSeconds   durée écoulée depuis le tick précédent, en secondes
     * @param maxAttempts    nombre maximal de points échantillonnés pour le
     *                       fallback
     */
    public Optional<LatLon> trySpawn(LatLon playerPosition, HauntedZone zone,
                                     double deltaSeconds, int maxAttempts) {
        if (playerPosition == null) {
            throw new IllegalArgumentException("playerPosition must not be null");
        }
        if (zone == null) {
            throw new IllegalArgumentException("zone must not be null");
        }
        if (deltaSeconds < 0.0) {
            throw new IllegalArgumentException("deltaSeconds must not be negative");
        }
        if (maxAttempts <= 0) {
            throw new IllegalArgumentException("maxAttempts must be positive");
        }

        if (!zone.contains(playerPosition)) {
            return Optional.empty();
        }

        double probability = triggerProbability(deltaSeconds);
        if (probability <= 0.0 || random.nextDouble() >= probability) {
            return Optional.empty();
        }

        LatLon target = randomTarget(playerPosition);
        if (isValidSpawnPoint(target, zone)) {
            return Optional.of(target);
        }
        return nearestSampledPoint(target, zone, maxAttempts);
    }

    /**
     * Apparition forcée en mode admin : le point fourni est renvoyé tel quel,
     * sans vérification de zone, de probabilité, de zone jouable ni
     * d'obstacle.
     */
    public LatLon spawnAdmin(LatLon mapPoint) {
        if (mapPoint == null) {
            throw new IllegalArgumentException("mapPoint must not be null");
        }
        return mapPoint;
    }

    private double triggerProbability(double deltaSeconds) {
        double probabilityPerSecond = config.getZoneTriggerProbabilityPerS();
        if (probabilityPerSecond <= 0.0) {
            return 0.0;
        }
        if (probabilityPerSecond >= 1.0) {
            return 1.0;
        }
        return 1.0 - Math.pow(1.0 - probabilityPerSecond, deltaSeconds);
    }

    private LatLon randomTarget(LatLon playerPosition) {
        double bearingDegrees = random.nextDouble() * FULL_CIRCLE_DEGREES;
        double minDistanceM = config.getGhostSpawnMinDistanceM();
        double maxDistanceM = config.getGhostSpawnMaxDistanceM();
        double distanceM = minDistanceM + random.nextDouble() * (maxDistanceM - minDistanceM);
        return GeoMath.destination(playerPosition, bearingDegrees, distanceM,
                config.getEarthRadiusM());
    }

    private boolean isValidSpawnPoint(LatLon point, HauntedZone zone) {
        return zone.contains(point) && playArea.contains(point);
    }

    /**
     * Fallback : échantillonne des points de la zone et renvoie le plus proche
     * de la cible parmi les points valides trouvés.
     */
    private Optional<LatLon> nearestSampledPoint(LatLon target, HauntedZone zone,
                                                  int maxAttempts) {
        LatLon nearest = null;
        double nearestDistanceM = Double.MAX_VALUE;

        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            LatLon candidate = randomPointInZone(zone);
            if (candidate == null || !zone.contains(candidate)
                    || !playArea.contains(candidate)) {
                continue;
            }
            double distanceM = GeoMath.haversineDistanceMeters(
                    target, candidate, config.getEarthRadiusM());
            if (distanceM < nearestDistanceM) {
                nearestDistanceM = distanceM;
                nearest = candidate;
            }
        }

        return Optional.ofNullable(nearest);
    }

    private LatLon randomPointInZone(HauntedZone zone) {
        if (zone.isCircle()) {
            double radiusM = zone.getRadiusM() * Math.sqrt(random.nextDouble());
            double bearingDegrees = random.nextDouble() * FULL_CIRCLE_DEGREES;
            return GeoMath.destination(zone.getCenter(), bearingDegrees, radiusM,
                    config.getEarthRadiusM());
        }

        List<LatLon> vertices = zone.getPolygon().getVertices();
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

        double latitude = minLatitude
                + random.nextDouble() * (maxLatitude - minLatitude);
        double longitude = minLongitude
                + random.nextDouble() * (maxLongitude - minLongitude);
        return new LatLon(latitude, longitude);
    }
}
