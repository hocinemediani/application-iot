package com.example.ghostradar.domain.game;

/**
 * Détermination de l'étage courant du joueur.
 *
 * <p>En V1, l'altitude est ignorée : l'implémentation par défaut retourne
 * toujours {@code 0} (rez-de-chaussée). En V2, une implémentation barométrique
 * pourra utiliser {@link GameInput#getPressureHpa()} et
 * {@link GameInput#getFloor()} pour appliquer la pression par étage et
 * l'hystérésis, sans changer cette interface.</p>
 */
public interface FloorProvider {

    /**
     * Étage courant pour le tick fourni.
     *
     * @return {@code 0} en V1
     * @throws IllegalArgumentException si {@code input} est nul
     */
    default int getFloor(GameInput input) {
        if (input == null) {
            throw new IllegalArgumentException("input must not be null");
        }
        return 0;
    }
}
