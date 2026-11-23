package com.example.ghostradar.domain.item;

import com.example.ghostradar.domain.config.GameConfig;

import java.util.EnumMap;
import java.util.Map;

/**
 * Stocks des gadgets consommables du joueur.
 *
 * <p>L'inventaire ne connaît ni les mines ni les appâts : il ne fait que
 * compter les unités disponibles. La boucle de jeu consomme une unité après
 * un placement ou une activation réussie. Aucune capacité maximale n'est
 * définie en V1.</p>
 */
public final class Inventory {

    private final Map<GadgetType, Integer> counts = new EnumMap<>(GadgetType.class);

    public Inventory(GameConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("config must not be null");
        }
        if (config.getStartingMineCount() < 0) {
            throw new IllegalArgumentException("startingMineCount must not be negative");
        }
        if (config.getStartingLureCount() < 0) {
            throw new IllegalArgumentException("startingLureCount must not be negative");
        }
        counts.put(GadgetType.ECTOPLASM_MINE, config.getStartingMineCount());
        counts.put(GadgetType.LURE, config.getStartingLureCount());
    }

    /**
     * Nombre d'unités disponibles pour un type de gadget.
     */
    public int getCount(GadgetType type) {
        requireType(type);
        return counts.get(type);
    }

    /**
     * Ajoute des unités à un type de gadget.
     *
     * @param quantity nombre strictement positif d'unités à ajouter
     * @throws IllegalArgumentException si la quantité n'est pas positive ou
     *         si le total dépasserait {@code Integer.MAX_VALUE} (le stock est
     *         alors laissé inchangé)
     */
    public void add(GadgetType type, int quantity) {
        requireType(type);
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
        try {
            counts.put(type, Math.addExact(counts.get(type), quantity));
        } catch (ArithmeticException overflow) {
            throw new IllegalArgumentException("gadget count overflow for " + type, overflow);
        }
    }

    /**
     * Consomme une unité d'un type de gadget.
     *
     * @return {@code true} si une unité a été consommée, {@code false} si le
     *         stock est déjà à zéro
     */
    public boolean consume(GadgetType type) {
        requireType(type);
        int count = counts.get(type);
        if (count <= 0) {
            return false;
        }
        counts.put(type, count - 1);
        return true;
    }

    private static void requireType(GadgetType type) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null");
        }
    }
}
