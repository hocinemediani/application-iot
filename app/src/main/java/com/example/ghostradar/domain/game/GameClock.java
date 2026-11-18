package com.example.ghostradar.domain.game;

/**
 * Horloge injectable pour la simulation, exprimée en millisecondes monotones.
 *
 * <p>Les tests utilisent une horloge factice pour rendre les timers
 * déterministes ; la couche Android peut fournir une implémentation basée sur
 * {@link #system()}.</p>
 */
public interface GameClock {

    long nowMs();

    /**
     * Horloge système monotone, adaptée à la mesure de durées.
     */
    static GameClock system() {
        return () -> System.nanoTime() / 1_000_000L;
    }
}
