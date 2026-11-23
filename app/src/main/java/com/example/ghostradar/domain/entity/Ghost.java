package com.example.ghostradar.domain.entity;

import com.example.ghostradar.domain.Validation;
import com.example.ghostradar.domain.geo.LatLon;

/**
 * Fantôme de la session courante.
 *
 * <p>Objet mutable : la position est mise à jour par
 * {@code domain.ai.GhostMovement} et le timer de stun est porté par le
 * fantôme lui-même. Le stun est décompté à chaque tick par le mouvement, qui
 * gèle le déplacement tant qu'il reste du temps.</p>
 */
public final class Ghost {

    private LatLon position;
    private GhostState state = GhostState.ACTIVE;
    private double stunRemainingS;

    public Ghost(LatLon position) {
        if (position == null) {
            throw new IllegalArgumentException("position must not be null");
        }
        this.position = position;
    }

    public LatLon getPosition() {
        return position;
    }

    public void setPosition(LatLon position) {
        if (position == null) {
            throw new IllegalArgumentException("position must not be null");
        }
        this.position = position;
    }

    public GhostState getState() {
        return state;
    }

    public double getStunRemainingS() {
        return stunRemainingS;
    }

    public boolean isStunned() {
        return state == GhostState.STUNNED;
    }

    /**
     * Immobilise le fantôme pendant la durée demandée.
     *
     * @param durationS durée strictement positive, en secondes
     */
    public void stun(double durationS) {
        Validation.requirePositiveFinite("durationS", durationS);
        stunRemainingS = durationS;
        state = GhostState.STUNNED;
    }

    /**
     * Décompte le stun du tick écoulé et repasse le fantôme en
     * {@link GhostState#ACTIVE} lorsque le timer atteint zéro.
     */
    public void advanceStun(double deltaSeconds) {
        Validation.requireDeltaSeconds(deltaSeconds);
        if (!isStunned()) {
            return;
        }
        stunRemainingS = Math.max(0.0, stunRemainingS - deltaSeconds);
        if (stunRemainingS == 0.0) {
            state = GhostState.ACTIVE;
        }
    }
}
