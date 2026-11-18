package com.example.ghostradar.domain.entity;

/**
 * État du fantôme en V1.
 *
 * <p>La capture retire le fantôme de la simulation : il n'existe donc pas
 * d'état {@code CAPTURED}. Seuls le déplacement normal et l'immobilisation par
 * une mine à ectoplasme sont modélisés.</p>
 */
public enum GhostState {
    ACTIVE,
    STUNNED
}
