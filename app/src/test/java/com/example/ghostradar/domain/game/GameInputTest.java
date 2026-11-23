package com.example.ghostradar.domain.game;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;

public class GameInputTest {

    @Test
    public void defaultsToGroundFloorAndNoPressure() {
        GameInput input = new GameInput();

        assertEquals(0, input.getFloor());
        assertNull(input.getPressureHpa());
    }

    @Test
    public void storesPressureOnGroundFloor() {
        GameInput input = new GameInput();
        input.setPressureHpa(1_000.5);

        assertEquals(0, input.getFloor());
        assertEquals(1_000.5, input.getPressureHpa(), 0.0);
    }

    @Test
    public void constructorStoresGroundFloorAndPressure() {
        GameInput input = new GameInput(0, 995.0);

        assertEquals(0, input.getFloor());
        assertEquals(995.0, input.getPressureHpa(), 0.0);
    }

    @Test
    public void constructorRejectsNonZeroFloor() {
        assertThrows(IllegalArgumentException.class, () -> new GameInput(1, null));
        assertThrows(IllegalArgumentException.class, () -> new GameInput(-2, 1_013.25));
    }

    @Test
    public void setFloorRejectsNonZeroFloorWithoutChangingValue() {
        GameInput input = new GameInput();

        assertThrows(IllegalArgumentException.class, () -> input.setFloor(3));
        assertThrows(IllegalArgumentException.class, () -> input.setFloor(-1));

        assertEquals(0, input.getFloor());
    }

    @Test
    public void constructorRejectsNonFiniteOrNonPositivePressure() {
        assertThrows(IllegalArgumentException.class,
                () -> new GameInput(0, Double.NaN));
        assertThrows(IllegalArgumentException.class,
                () -> new GameInput(0, Double.POSITIVE_INFINITY));
        assertThrows(IllegalArgumentException.class,
                () -> new GameInput(0, Double.NEGATIVE_INFINITY));
        assertThrows(IllegalArgumentException.class, () -> new GameInput(0, -1.0));
        assertThrows(IllegalArgumentException.class, () -> new GameInput(0, 0.0));
    }

    @Test
    public void setPressureHpaRejectsInvalidValuesWithoutChangingValue() {
        GameInput input = new GameInput(0, 1_000.0);

        assertThrows(IllegalArgumentException.class, () -> input.setPressureHpa(Double.NaN));
        assertThrows(IllegalArgumentException.class,
                () -> input.setPressureHpa(Double.POSITIVE_INFINITY));
        assertThrows(IllegalArgumentException.class, () -> input.setPressureHpa(-5.0));
        assertThrows(IllegalArgumentException.class, () -> input.setPressureHpa(0.0));

        assertEquals(1_000.0, input.getPressureHpa(), 0.0);
    }

    @Test
    public void acceptsNullPressureAndAnyFinitePositivePressure() {
        GameInput withoutPressure = new GameInput(0, null);
        assertNull(withoutPressure.getPressureHpa());

        withoutPressure.setPressureHpa(980.25);
        assertEquals(980.25, withoutPressure.getPressureHpa(), 0.0);

        withoutPressure.setPressureHpa(null);
        assertNull(withoutPressure.getPressureHpa());
    }

    @Test
    public void pressureHpaCanBeCleared() {
        GameInput input = new GameInput();
        input.setPressureHpa(1_000.0);
        input.setPressureHpa(null);

        assertNull(input.getPressureHpa());
    }

    @Test
    public void snapshotFreezesStateAndInputValues() {
        GameInput input = new GameInput(0, 1_013.25);
        GameSnapshot snapshot = new GameSnapshot(GameState.EXPLORATION, input);

        // Le floor est figé à 0 en V1 ; seule la pression peut encore varier.
        input.setPressureHpa(900.0);

        assertEquals(GameState.EXPLORATION, snapshot.getState());
        assertEquals(0, snapshot.getFloor());
        assertEquals(1_013.25, snapshot.getPressureHpa(), 0.0);
    }

    @Test
    public void snapshotAcceptsNullPressure() {
        GameSnapshot snapshot = new GameSnapshot(GameState.EXPLORATION, new GameInput());

        assertNull(snapshot.getPressureHpa());
    }

    @Test
    public void snapshotRejectsNullInput() {
        assertThrows(IllegalArgumentException.class,
                () -> new GameSnapshot(GameState.EXPLORATION, null));
    }

    @Test
    public void snapshotRejectsNullState() {
        assertThrows(IllegalArgumentException.class,
                () -> new GameSnapshot(null, new GameInput()));
    }
}
