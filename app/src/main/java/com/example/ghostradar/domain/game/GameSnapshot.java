package com.example.ghostradar.domain.game;

/**
 * Vue figée de l'état du jeu et des entrées à un instant donné.
 *
 * <p>Objet immuable : les valeurs de {@link GameInput} sont copiées à la
 * construction. Toute modification ultérieure de l'entrée source n'affecte
 * pas le snapshot. En V1, le snapshot ne porte que l'état du jeu, l'étage et
 * la pression ; il ne référence aucun modèle de carte ni d'étage.</p>
 */
public final class GameSnapshot {

    private final GameState state;
    private final int floor;
    private final Double pressureHpa;

    public GameSnapshot(GameState state, GameInput input) {
        if (state == null) {
            throw new IllegalArgumentException("state must not be null");
        }
        if (input == null) {
            throw new IllegalArgumentException("input must not be null");
        }
        this.state = state;
        this.floor = input.getFloor();
        this.pressureHpa = input.getPressureHpa();
    }

    public GameState getState() {
        return state;
    }

    public int getFloor() {
        return floor;
    }

    public Double getPressureHpa() {
        return pressureHpa;
    }
}
