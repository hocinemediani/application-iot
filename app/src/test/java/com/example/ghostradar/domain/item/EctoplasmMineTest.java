package com.example.ghostradar.domain.item;

import com.example.ghostradar.domain.config.GameConfig;
import com.example.ghostradar.domain.entity.Ghost;
import com.example.ghostradar.domain.geo.GeoMath;
import com.example.ghostradar.domain.geo.GeoPolygon;
import com.example.ghostradar.domain.geo.LatLon;
import com.example.ghostradar.domain.geo.PlayArea;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class EctoplasmMineTest {

    private static final double EARTH_RADIUS_M = 6_371_000.0;
    private static final double MIN_VERTEX_DISTANCE_M = 0.5;
    private static final double TRIGGER_RADIUS_M = 3.0;
    private static final double STUN_DURATION_S = 10.0;
    private static final int MAX_ACTIVE = 3;
    private static final double MIN_DISTANCE_M = 5.0;
    private static final LatLon PLAYER = new LatLon(0.005, 0.005);

    @Test
    public void placeRequiresPlayerInsidePlayArea() {
        EctoplasmMine mines = new EctoplasmMine(GameConfig.defaults(), playArea());

        assertFalse(mines.place(new LatLon(0.02, 0.02)));
        assertEquals(0, mines.getActiveCount());

        assertTrue(mines.place(PLAYER));
        assertEquals(1, mines.getActiveCount());
    }

    @Test
    public void placeEnforcesMinimumDistanceBetweenActiveMines() {
        EctoplasmMine mines = new EctoplasmMine(GameConfig.defaults(), playArea());

        assertTrue(mines.place(PLAYER));
        assertFalse(mines.place(destination(4.9)));
        assertEquals(1, mines.getActiveCount());

        assertTrue(mines.place(destination(5.1)));
        assertEquals(2, mines.getActiveCount());
    }

    @Test
    public void enforcesMaximumThreeActiveMinesAndFreesSlotAfterTrigger() {
        EctoplasmMine mines = new EctoplasmMine(GameConfig.defaults(), playArea());
        assertTrue(mines.place(PLAYER));
        assertTrue(mines.place(destination(6.0)));
        assertTrue(mines.place(destination(12.0)));
        assertEquals(MAX_ACTIVE, mines.getActiveCount());

        assertFalse(mines.place(destination(18.0)));
        assertEquals(MAX_ACTIVE, mines.getActiveCount());

        Ghost ghost = new Ghost(PLAYER);
        assertTrue(mines.update(ghost));
        assertEquals(MAX_ACTIVE - 1, mines.getActiveCount());

        assertTrue(mines.place(destination(18.0)));
        assertEquals(MAX_ACTIVE, mines.getActiveCount());
    }

    @Test
    public void triggersNearestMineAndStunsGhost() {
        EctoplasmMine mines = new EctoplasmMine(GameConfig.defaults(), playArea());
        assertTrue(mines.place(PLAYER));
        assertTrue(mines.place(destination(6.0)));
        Ghost ghost = new Ghost(destination(2.9));

        assertTrue(mines.update(ghost));

        assertTrue(ghost.isStunned());
        assertEquals(STUN_DURATION_S, ghost.getStunRemainingS(), 0.0);
        assertEquals(1, mines.getActiveCount());

        List<EctoplasmMine.PlacedMine> visible = mines.getVisibleMines(ghost);
        assertEquals(2, visible.size());
        assertEquals(PLAYER, visible.get(0).getPosition());
        assertTrue(visible.get(0).isTriggered());
        assertFalse(visible.get(1).isTriggered());
    }

    @Test
    public void doesNotTriggerOutsideRadius() {
        EctoplasmMine mines = new EctoplasmMine(GameConfig.defaults(), playArea());
        assertTrue(mines.place(PLAYER));
        Ghost ghost = new Ghost(destination(TRIGGER_RADIUS_M + 0.1));

        assertFalse(mines.update(ghost));

        assertFalse(ghost.isStunned());
        assertEquals(1, mines.getActiveCount());
        assertEquals(1, mines.getVisibleMines(ghost).size());
    }

    @Test
    public void mineIsOneShotAndDoesNotResetStun() {
        EctoplasmMine mines = new EctoplasmMine(GameConfig.defaults(), playArea());
        assertTrue(mines.place(PLAYER));
        Ghost ghost = new Ghost(PLAYER);

        assertTrue(mines.update(ghost));
        assertEquals(0, mines.getActiveCount());

        assertFalse(mines.update(ghost));
        assertEquals(STUN_DURATION_S, ghost.getStunRemainingS(), 0.0);
    }

    @Test
    public void triggeredMineIsVisibleDuringStunThenHidden() {
        EctoplasmMine mines = new EctoplasmMine(GameConfig.defaults(), playArea());
        assertTrue(mines.place(PLAYER));
        Ghost ghost = new Ghost(PLAYER);

        assertTrue(mines.update(ghost));
        List<EctoplasmMine.PlacedMine> duringStun = mines.getVisibleMines(ghost);
        assertEquals(1, duringStun.size());
        assertTrue(duringStun.get(0).isTriggered());

        ghost.advanceStun(STUN_DURATION_S);

        assertEquals(0, mines.getVisibleMines(ghost).size());
    }

    @Test
    public void untriggeredMinePersistsOnMapUntilTriggered() {
        EctoplasmMine mines = new EctoplasmMine(GameConfig.defaults(), playArea());
        assertTrue(mines.place(PLAYER));
        Ghost ghost = new Ghost(destination(50.0));

        assertEquals(1, mines.getVisibleMines(ghost).size());
        assertEquals(1, mines.getActiveCount());
    }

    @Test
    public void visibilityRespectsConfig() {
        GameConfig config = new GameConfig.Builder()
                .setMineVisibleOnMap(false)
                .build();
        EctoplasmMine mines = new EctoplasmMine(config, playArea());
        assertTrue(mines.place(PLAYER));
        Ghost ghost = new Ghost(PLAYER);

        assertTrue(mines.update(ghost));
        assertTrue(ghost.isStunned());
        assertEquals(0, mines.getVisibleMines(ghost).size());
    }

    @Test
    public void rejectsInvalidArguments() {
        EctoplasmMine mines = new EctoplasmMine(GameConfig.defaults(), playArea());

        assertThrows(IllegalArgumentException.class,
                () -> new EctoplasmMine(null, playArea()));
        assertThrows(IllegalArgumentException.class,
                () -> new EctoplasmMine(GameConfig.defaults(), null));
        assertThrows(IllegalArgumentException.class, () -> mines.place(null));
        assertThrows(IllegalArgumentException.class, () -> mines.update(null));
        assertThrows(IllegalArgumentException.class, () -> mines.getVisibleMines(null));
    }

    @Test
    public void rejectsInvalidConfig() {
        assertThrows(IllegalArgumentException.class,
                () -> new EctoplasmMine(new GameConfig.Builder()
                        .setMineTriggerRadiusM(0.0)
                        .build(), playArea()));
        assertThrows(IllegalArgumentException.class,
                () -> new EctoplasmMine(new GameConfig.Builder()
                        .setMineStunDurationS(0.0)
                        .build(), playArea()));
        assertThrows(IllegalArgumentException.class,
                () -> new EctoplasmMine(new GameConfig.Builder()
                        .setMineMaxActive(0)
                        .build(), playArea()));
        assertThrows(IllegalArgumentException.class,
                () -> new EctoplasmMine(new GameConfig.Builder()
                        .setMineMinDistanceM(-1.0)
                        .build(), playArea()));
    }

    private static LatLon destination(double distanceM) {
        return GeoMath.destination(PLAYER, 90.0, distanceM, EARTH_RADIUS_M);
    }

    private static PlayArea playArea() {
        return new PlayArea(square(0.0, 0.0, 0.01, 0.01),
                Collections.emptyList(), EARTH_RADIUS_M);
    }

    private static GeoPolygon square(double minLat, double minLon,
                                     double maxLat, double maxLon) {
        return new GeoPolygon(Arrays.asList(
                new LatLon(minLat, minLon),
                new LatLon(minLat, maxLon),
                new LatLon(maxLat, maxLon),
                new LatLon(maxLat, minLon)
        ), 3, MIN_VERTEX_DISTANCE_M, EARTH_RADIUS_M);
    }
}
