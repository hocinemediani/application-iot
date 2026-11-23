package com.example.ghostradar.domain.item;

import com.example.ghostradar.domain.config.GameConfig;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class InventoryTest {

    @Test
    public void startsWithConfiguredCounts() {
        Inventory inventory = new Inventory(GameConfig.defaults());

        assertEquals(1, inventory.getCount(GadgetType.ECTOPLASM_MINE));
        assertEquals(1, inventory.getCount(GadgetType.LURE));
    }

    @Test
    public void startsWithOverriddenCounts() {
        GameConfig config = new GameConfig.Builder()
                .setStartingMineCount(4)
                .setStartingLureCount(2)
                .build();

        Inventory inventory = new Inventory(config);

        assertEquals(4, inventory.getCount(GadgetType.ECTOPLASM_MINE));
        assertEquals(2, inventory.getCount(GadgetType.LURE));
    }

    @Test
    public void addIncreasesRequestedType() {
        Inventory inventory = new Inventory(GameConfig.defaults());

        inventory.add(GadgetType.ECTOPLASM_MINE, 2);
        inventory.add(GadgetType.LURE, 5);

        assertEquals(3, inventory.getCount(GadgetType.ECTOPLASM_MINE));
        assertEquals(6, inventory.getCount(GadgetType.LURE));
    }

    @Test
    public void consumeDecreasesUntilZeroThenFails() {
        Inventory inventory = new Inventory(GameConfig.defaults());

        assertTrue(inventory.consume(GadgetType.ECTOPLASM_MINE));
        assertEquals(0, inventory.getCount(GadgetType.ECTOPLASM_MINE));

        assertFalse(inventory.consume(GadgetType.ECTOPLASM_MINE));
        assertEquals(0, inventory.getCount(GadgetType.ECTOPLASM_MINE));
    }

    @Test
    public void consumeAffectsOnlyRequestedType() {
        Inventory inventory = new Inventory(GameConfig.defaults());

        assertTrue(inventory.consume(GadgetType.LURE));

        assertEquals(1, inventory.getCount(GadgetType.ECTOPLASM_MINE));
        assertEquals(0, inventory.getCount(GadgetType.LURE));
    }

    @Test
    public void addRestoresConsumedStock() {
        Inventory inventory = new Inventory(GameConfig.defaults());
        assertTrue(inventory.consume(GadgetType.ECTOPLASM_MINE));

        inventory.add(GadgetType.ECTOPLASM_MINE, 1);

        assertEquals(1, inventory.getCount(GadgetType.ECTOPLASM_MINE));
    }

    @Test
    public void addRejectsOverflowWithoutChangingStock() {
        Inventory full = new Inventory(new GameConfig.Builder()
                .setStartingLureCount(Integer.MAX_VALUE)
                .build());
        assertThrows(IllegalArgumentException.class,
                () -> full.add(GadgetType.LURE, 1));
        assertEquals(Integer.MAX_VALUE, full.getCount(GadgetType.LURE));

        Inventory almostFull = new Inventory(GameConfig.defaults());
        assertThrows(IllegalArgumentException.class,
                () -> almostFull.add(GadgetType.ECTOPLASM_MINE, Integer.MAX_VALUE));
        assertEquals(1, almostFull.getCount(GadgetType.ECTOPLASM_MINE));
    }

    @Test
    public void rejectsInvalidArguments() {
        Inventory inventory = new Inventory(GameConfig.defaults());

        assertThrows(IllegalArgumentException.class, () -> new Inventory(null));
        assertThrows(IllegalArgumentException.class,
                () -> inventory.getCount(null));
        assertThrows(IllegalArgumentException.class,
                () -> inventory.add(null, 1));
        assertThrows(IllegalArgumentException.class,
                () -> inventory.add(GadgetType.LURE, 0));
        assertThrows(IllegalArgumentException.class,
                () -> inventory.add(GadgetType.LURE, -1));
        assertThrows(IllegalArgumentException.class,
                () -> inventory.consume(null));
    }

    @Test
    public void rejectsNegativeStartingCounts() {
        assertThrows(IllegalArgumentException.class,
                () -> new Inventory(new GameConfig.Builder()
                        .setStartingMineCount(-1)
                        .build()));
        assertThrows(IllegalArgumentException.class,
                () -> new Inventory(new GameConfig.Builder()
                        .setStartingLureCount(-1)
                        .build()));
    }
}
