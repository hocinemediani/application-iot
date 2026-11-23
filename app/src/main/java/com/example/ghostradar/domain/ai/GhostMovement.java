package com.example.ghostradar.domain.ai;

import com.example.ghostradar.domain.Validation;
import com.example.ghostradar.domain.config.GameConfig;
import com.example.ghostradar.domain.entity.Ghost;
import com.example.ghostradar.domain.geo.GeoMath;
import com.example.ghostradar.domain.geo.LatLon;
import com.example.ghostradar.domain.geo.PlayArea;

/**
 * Déplacement du fantôme dans la zone jouable.
 *
 * <p>Priorités à chaque tick : le stun gèle tout déplacement ; sinon l'appât
 * attire le fantôme vers le joueur à {@code LURE_SPEED_MPS} ; sinon le fantôme
 * fuit à {@code GHOST_BASE_SPEED_MPS}, multipliée par
 * {@code GHOST_FLEE_MULTIPLIER} pendant une alerte bruit. Le point calculé est
 * systématiquement passé par {@link PlayArea#clamp(LatLon)}, qui projette sur
 * la frontière bloquante et conserve la composante tangentielle : le fantôme
 * est repoussé et glisse le long des bords extérieurs comme des obstacles.</p>
 */
public final class GhostMovement {

    private final GameConfig config;
    private final PlayArea playArea;

    public GhostMovement(GameConfig config, PlayArea playArea) {
        if (config == null) {
            throw new IllegalArgumentException("config must not be null");
        }
        if (playArea == null) {
            throw new IllegalArgumentException("playArea must not be null");
        }
        Validation.requireSameRadius("earthRadiusM (config/playArea)",
                config.getEarthRadiusM(), playArea.getEarthRadiusM());
        this.config = config;
        this.playArea = playArea;
    }

    /**
     * Fait avancer le fantôme d'un tick.
     *
     * <p>Un tick commencé avec un fantôme stunné ne produit aucun déplacement,
     * même si le stun expire pendant ce tick : le mouvement reprend au tick
     * suivant. L'appât arrivé à portée place le fantôme exactement sur la
     * position du joueur, sans dépassement, puis la ramène dans la zone
     * jouable si le joueur en est sorti.</p>
     *
     * @param ghost          fantôme à déplacer
     * @param playerPosition position courante du joueur
     * @param deltaSeconds   durée du tick, en secondes
     * @param noiseAlert     vrai si l'alerte bruit est active
     * @param lureActive     vrai si un appât est actif
     */
    public void update(Ghost ghost, LatLon playerPosition, double deltaSeconds,
                       boolean noiseAlert, boolean lureActive) {
        if (ghost == null) {
            throw new IllegalArgumentException("ghost must not be null");
        }
        if (playerPosition == null) {
            throw new IllegalArgumentException("playerPosition must not be null");
        }
        Validation.requireDeltaSeconds(deltaSeconds);

        if (ghost.isStunned()) {
            ghost.advanceStun(deltaSeconds);
            return;
        }

        if (deltaSeconds == 0.0) {
            return;
        }

        LatLon position = ghost.getPosition();
        double earthRadiusM = config.getEarthRadiusM();

        if (lureActive) {
            double distanceToPlayerM = GeoMath.haversineDistanceMeters(
                    position, playerPosition, earthRadiusM);
            double lureStepM = config.getLureSpeedMps() * deltaSeconds;
            if (lureStepM >= distanceToPlayerM) {
                ghost.setPosition(playArea.clamp(playerPosition));
                return;
            }
            double bearing = GeoMath.initialBearingDegrees(position, playerPosition);
            LatLon next = GeoMath.destination(position, bearing, lureStepM, earthRadiusM);
            ghost.setPosition(playArea.clamp(next));
            return;
        }

        double speedMps = config.getGhostBaseSpeedMps();
        if (noiseAlert) {
            speedMps *= config.getGhostFleeMultiplier();
        }
        if (speedMps <= 0.0) {
            return;
        }

        double fleeBearing = GeoMath.initialBearingDegrees(playerPosition, position);
        LatLon next = GeoMath.destination(position, fleeBearing,
                speedMps * deltaSeconds, earthRadiusM);
        ghost.setPosition(playArea.clamp(next));
    }
}
