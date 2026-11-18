package com.example.ghostradar.domain.game;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class FloorProviderTest {

    @Test
    public void defaultImplementationReturnsGroundFloor() {
        FloorProvider provider = new FloorProvider() {
        };

        assertEquals(0, provider.getFloor(new GameInput()));
    }

    @Test
    public void defaultImplementationIgnoresFloorAndPressure() {
        FloorProvider provider = new FloorProvider() {
        };

        assertEquals(0, provider.getFloor(new GameInput(3, 1013.25)));
        assertEquals(0, provider.getFloor(new GameInput(-2, 900.0)));
    }

    @Test
    public void defaultImplementationAcceptsNullPressure() {
        FloorProvider provider = new FloorProvider() {
        };

        assertEquals(0, provider.getFloor(new GameInput(1, null)));
    }

    @Test
    public void defaultImplementationRejectsNullInput() {
        FloorProvider provider = new FloorProvider() {
        };

        assertThrows(IllegalArgumentException.class, () -> provider.getFloor(null));
    }

    @Test
    public void implementationCanOverrideDefault() {
        FloorProvider provider = new FloorProvider() {
            @Override
            public int getFloor(GameInput input) {
                if (input == null) {
                    throw new IllegalArgumentException("input must not be null");
                }
                return 2;
            }
        };

        assertEquals(2, provider.getFloor(new GameInput()));
    }
}
