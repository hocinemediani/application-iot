package com.example.ghostradar.domain.ai;

import com.example.ghostradar.domain.config.GameConfig;
import com.example.ghostradar.domain.entity.Ghost;
import com.example.ghostradar.domain.geo.GeoMath;
import com.example.ghostradar.domain.geo.LatLon;
import com.example.ghostradar.domain.item.BeamProfile;

/**
 * Combat de capture : verrouillage du faisceau sur un fantôme ancré.
 *
 * <p>Le contrôleur démarre lorsque le joueur est à moins de
 * {@code CAPTURE_DISTANCE_M} du fantôme. À chaque tick, l'UI fournit l'erreur
 * angulaire entre le viseur et la cible ainsi que l'état du faisceau. Le verrou
 * est actif si le faisceau est allumé et si l'erreur reste dans la tolérance
 * {@code CAPTURE_ANGLE_TOLERANCE_DEG}. Le temps de verrou s'accumule jusqu'à
 * {@code CAPTURE_REQUIRED_SECONDS} ; le temps total est limité par
 * {@code CAPTURE_TIMEOUT_S}.</p>
 *
 * <p>Le fantôme reste ancré à sa position de départ, même s'il est stunné : le
 * stun n'empêche pas la capture. La génération des vecteurs de force du
 * fantôme est laissée au composant dédié ; ce contrôleur consomme l'erreur
 * angulaire qui en résulte.</p>
 *
 * <p>Le profil de faisceau est injecté via {@link #CaptureController(GameConfig,
 * BeamProfile)} (le constructeur à un argument utilise un profil niveau 1).
 * Le temps requis et la tolérance sont <em>figés au début de chaque combat</em>
 * : une amélioration obtenue entre deux combats est prise en compte au combat
 * suivant, mais n'affecte pas un combat déjà entamé.</p>
 */
public final class CaptureController {

    private static final double TIME_EPSILON_S = 1e-9;

    public enum CaptureStatus {
        IDLE,
        IN_PROGRESS,
        SUCCESS,
        FAILED
    }

    private final GameConfig config;
    private final BeamProfile beam;

    private CaptureStatus status = CaptureStatus.IDLE;
    private Ghost ghost;
    private LatLon anchoredPosition;
    private double progressS;
    private double elapsedS;
    private double requiredSeconds;
    private double angleToleranceDeg;

    /** Construit un contrôleur avec un faisceau au niveau 1. */
    public CaptureController(GameConfig config) {
        this(config, new BeamProfile(config));
    }

    /** Construit un contrôleur avec le profil de faisceau courant. */
    public CaptureController(GameConfig config, BeamProfile beam) {
        if (config == null) {
            throw new IllegalArgumentException("config must not be null");
        }
        if (beam == null) {
            throw new IllegalArgumentException("beam must not be null");
        }
        if (config.getCaptureDistanceM() <= 0.0) {
            throw new IllegalArgumentException("captureDistanceM must be positive");
        }
        if (config.getCaptureRequiredSeconds() <= 0.0) {
            throw new IllegalArgumentException("captureRequiredSeconds must be positive");
        }
        if (config.getCaptureAngleToleranceDeg() < 0.0) {
            throw new IllegalArgumentException("captureAngleToleranceDeg must not be negative");
        }
        if (config.getCaptureTimeoutS() <= 0.0) {
            throw new IllegalArgumentException("captureTimeoutS must be positive");
        }
        if (config.getCaptureProgressDecayPerS() < 0.0) {
            throw new IllegalArgumentException("captureProgressDecayPerS must not be negative");
        }
        this.config = config;
        this.beam = beam;
    }

    /**
     * Démarre le combat si le fantôme est à portée de capture.
     *
     * @return {@code true} si le combat démarre ; {@code false} si le fantôme
     *         est trop loin ou si le contrôleur a déjà été utilisé
     */
    public boolean start(LatLon playerPosition, Ghost ghost) {
        if (playerPosition == null) {
            throw new IllegalArgumentException("playerPosition must not be null");
        }
        if (ghost == null) {
            throw new IllegalArgumentException("ghost must not be null");
        }
        if (status != CaptureStatus.IDLE) {
            return false;
        }

        double distanceM = GeoMath.haversineDistanceMeters(
                playerPosition, ghost.getPosition(), config.getEarthRadiusM());
        if (distanceM >= config.getCaptureDistanceM()) {
            return false;
        }

        this.ghost = ghost;
        this.anchoredPosition = ghost.getPosition();
        this.progressS = 0.0;
        this.elapsedS = 0.0;
        // Figer le profil pour ce combat (Q4).
        this.requiredSeconds = beam.getRequiredSeconds();
        this.angleToleranceDeg = beam.getAngleToleranceDeg();
        this.status = CaptureStatus.IN_PROGRESS;
        anchorGhost();
        return true;
    }

    /**
     * Fait avancer le combat d'un tick.
     *
     * <p>Les appels sont ignorés tant que le combat n'est pas en cours ou
     * qu'il est terminé. Le succès est évalué avant le timeout lorsque les
     * deux échéances tombent sur le même tick.</p>
     *
     * @param aimErrorDeg  erreur angulaire signée entre viseur et cible
     * @param beamActive   vrai si le joueur maintient le faisceau actif
     * @param deltaSeconds durée du tick, en secondes
     */
    public void update(double aimErrorDeg, boolean beamActive, double deltaSeconds) {
        if (Double.isNaN(aimErrorDeg) || Double.isInfinite(aimErrorDeg)) {
            throw new IllegalArgumentException("aimErrorDeg must be finite");
        }
        if (!(deltaSeconds >= 0.0) || Double.isInfinite(deltaSeconds)) {
            throw new IllegalArgumentException("deltaSeconds must be finite and not negative");
        }
        if (status != CaptureStatus.IN_PROGRESS || deltaSeconds == 0.0) {
            return;
        }

        elapsedS += deltaSeconds;

        boolean locked = beamActive && Math.abs(aimErrorDeg) <= angleToleranceDeg;
        if (locked) {
            progressS += deltaSeconds;
        } else if (!config.isCapturePauseOnLockLost()) {
            progressS = Math.max(0.0,
                    progressS - config.getCaptureProgressDecayPerS() * deltaSeconds);
        }

        anchorGhost();

        if (progressS + TIME_EPSILON_S >= requiredSeconds) {
            progressS = requiredSeconds;
            status = CaptureStatus.SUCCESS;
        } else if (elapsedS + TIME_EPSILON_S >= config.getCaptureTimeoutS()) {
            status = CaptureStatus.FAILED;
        }
    }

    public CaptureStatus getStatus() {
        return status;
    }

    public double getProgressS() {
        return progressS;
    }

    public double getElapsedS() {
        return elapsedS;
    }

    public LatLon getAnchoredPosition() {
        return anchoredPosition;
    }

    private void anchorGhost() {
        if (config.isCaptureGhostPositionLocked()) {
            ghost.setPosition(anchoredPosition);
        }
    }
}
