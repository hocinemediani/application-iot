package com.example.ghostradar.domain.game;

import java.util.Random;

/**
 * Source d'aléa injectable pour rendre la simulation déterministe en test.
 */
public interface RandomSource {

    double nextDouble();

    int nextInt(int boundExclusive);

    /**
     * Crée une source déterministe à partir d'une graine.
     */
    static RandomSource seeded(long seed) {
        Random random = new Random(seed);
        return new RandomSource() {
            @Override
            public double nextDouble() {
                return random.nextDouble();
            }

            @Override
            public int nextInt(int boundExclusive) {
                return random.nextInt(boundExclusive);
            }
        };
    }
}
