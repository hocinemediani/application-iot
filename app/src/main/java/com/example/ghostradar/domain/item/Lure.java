package com.example.ghostradar.domain.item;

import com.example.ghostradar.domain.config.GameConfig;
import com.example.ghostradar.domain.entity.Ghost;

/**
 * Appât consommable qui attire le fantôme vers le joueur.
 *
 * <p>L'appât ne gère que son activation, sa durée de
 * {@code LURE_DURATION_S} et son caractère one-shot. Le déplacement vers le
 * joueur, la priorité sur la fuite et l'alerte bruit, ainsi que le clamp dans
 * la zone jouable restent assurés par {@code GhostMovement}, qui reçoit
 * {@link #isActive()}.</p>
 *
 * <p>L'activation est refusée sur un fantôme stunné et ne consomme alors pas
 * l'objet. Une fois actif, le timer continue même si le fantôme est stunné :
 * l'effet est simplement neutralisé par le gel du déplacement.</p>
 */
public final class Lure {

    private static final double TIME_EPSILON_S = 1e-9;

    private final GameConfig config;

    private boolean consumed;
    private boolean active;
    private double remainingS;

    public Lure(GameConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("config must not be null");
        }
        if (config.getLureDurationS() <= 0.0) {
            throw new IllegalArgumentException("lureDurationS must be positive");
        }
        this.config = config;
    }

    /**
     * Active l'appât si le fantôme n'est pas stunné et si l'objet n'a pas déjà
     * été consommé.
     *
     * @return {@code true} si l'appât démarre
     */
    public boolean activate(Ghost ghost) {
        if (ghost == null) {
            throw new IllegalArgumentException("ghost must not be null");
        }
        if (consumed || active || ghost.isStunned()) {
            return false;
        }
        consumed = true;
        active = true;
        remainingS = config.getLureDurationS();
        return true;
    }

    /**
     * Fait avancer le timer de l'appât.
     *
     * @param deltaSeconds durée du tick, en secondes
     */
    public void update(double deltaSeconds) {
        if (!(deltaSeconds >= 0.0) || Double.isInfinite(deltaSeconds)) {
            throw new IllegalArgumentException("deltaSeconds must be finite and not negative");
        }
        if (!active || deltaSeconds == 0.0) {
            return;
        }

        remainingS = Math.max(0.0, remainingS - deltaSeconds);
        if (remainingS <= TIME_EPSILON_S) {
            remainingS = 0.0;
            active = false;
        }
    }

    public boolean isActive() {
        return active;
    }

    public boolean isConsumed() {
        return consumed;
    }

    public double getRemainingS() {
        return remainingS;
    }
}
