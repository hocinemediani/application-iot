package com.example.ghostradar.domain.game;

import com.example.ghostradar.domain.Validation;

/**
 * Entrées de la simulation pour un tick.
 *
 * <p>Objet mutable : les capteurs mettent à jour ses valeurs, puis
 * {@link GameSnapshot} en fige une copie. L'invariant V1 est appliqué à la
 * source : {@code floor} vaut toujours 0 et {@code pressureHpa}, lorsqu'il est
 * fourni, est fini et strictement positif. Le constructeur et les setters
 * refusent toute autre valeur, ce qui garantit la cohérence avec
 * {@link GameSnapshot} et le {@link FloorProvider} par défaut (qui retourne
 * toujours 0) sans dupliquer la validation.</p>
 */
public final class GameInput {

    private int floor;
    private Double pressureHpa;

    public GameInput() {
        this(0, null);
    }

    public GameInput(int floor, Double pressureHpa) {
        this.floor = requireV1Floor(floor);
        this.pressureHpa = validatePressure(pressureHpa);
    }

    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = requireV1Floor(floor);
    }

    public Double getPressureHpa() {
        return pressureHpa;
    }

    public void setPressureHpa(Double pressureHpa) {
        this.pressureHpa = validatePressure(pressureHpa);
    }

    private static int requireV1Floor(int floor) {
        if (floor != 0) {
            throw new IllegalArgumentException("floor must be 0 in V1, got " + floor);
        }
        return floor;
    }

    private static Double validatePressure(Double pressureHpa) {
        if (pressureHpa == null) {
            return null;
        }
        Validation.requirePositiveFinite("pressureHpa", pressureHpa);
        return pressureHpa;
    }
}
