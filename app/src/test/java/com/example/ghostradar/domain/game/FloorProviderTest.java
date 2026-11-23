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
    public void defaultImplementationIgnoresPressureAndReturnsGroundFloor() {
        // B8 : GameInput garantit déjà floor == 0 en V1 ; le provider reste
        // indépendant de la pression (utilisée seulement en V2).
        FloorProvider provider = new FloorProvider() {
        };

        assertEquals(0, provider.getFloor(new GameInput(0, 1013.25)));
        assertEquals(0, provider.getFloor(new GameInput(0, 900.0)));
    }

    @Test
    public void defaultImplementationAcceptsNullPressure() {
        FloorProvider provider = new FloorProvider() {
        };

        assertEquals(0, provider.getFloor(new GameInput(0, null)));
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
