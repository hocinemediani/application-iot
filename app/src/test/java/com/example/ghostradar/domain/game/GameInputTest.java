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
    public void storesFloorAndPressure() {
        GameInput input = new GameInput();
        input.setFloor(2);
        input.setPressureHpa(1_000.5);

        assertEquals(2, input.getFloor());
        assertEquals(1_000.5, input.getPressureHpa(), 0.0);
    }

    @Test
    public void constructorStoresFloorAndPressure() {
        GameInput input = new GameInput(1, 995.0);

        assertEquals(1, input.getFloor());
        assertEquals(995.0, input.getPressureHpa(), 0.0);
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

        input.setFloor(1);
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
