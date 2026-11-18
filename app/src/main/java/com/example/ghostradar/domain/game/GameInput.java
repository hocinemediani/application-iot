package com.example.ghostradar.domain.game;

/**
 * Entrées de la simulation pour un tick.
 *
 * <p>Objet mutable : les capteurs mettent à jour ses valeurs, puis
 * {@link GameSnapshot} en fige une copie. En V1, {@code floor} vaut 0 et
 * {@code pressureHpa} peut être {@code null}.</p>
 */
public final class GameInput {

    private int floor;
    private Double pressureHpa;

    public GameInput() {
        this(0, null);
    }

    public GameInput(int floor, Double pressureHpa) {
        this.floor = floor;
        this.pressureHpa = pressureHpa;
    }

    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    public Double getPressureHpa() {
        return pressureHpa;
    }

    public void setPressureHpa(Double pressureHpa) {
        this.pressureHpa = pressureHpa;
    }
}
