package com.example.ghostradar.domain.item;

import com.example.ghostradar.domain.ai.GhostMovement;
import com.example.ghostradar.domain.config.GameConfig;
import com.example.ghostradar.domain.entity.Ghost;
import com.example.ghostradar.domain.geo.GeoMath;
import com.example.ghostradar.domain.geo.GeoPolygon;
import com.example.ghostradar.domain.geo.LatLon;
import com.example.ghostradar.domain.geo.PlayArea;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class LureTest {

    private static final double EARTH_RADIUS_M = 6_371_000.0;
    private static final double MIN_VERTEX_DISTANCE_M = 0.5;
    private static final double TICK_S = 0.1;
    private static final double DURATION_S = 8.0;
    private static final double LURE_SPEED_MPS = 1.5;

    private static final LatLon GHOST_POSITION = new LatLon(0.005, 0.005);
    private static final LatLon PLAYER_EAST = new LatLon(0.005, 0.006);

    @Test
    public void startsInactiveAndAvailable() {
        Lure lure = new Lure(GameConfig.defaults());

        assertFalse(lure.isActive());
        assertFalse(lure.isConsumed());
        assertEquals(0.0, lure.getRemainingS(), 0.0);
    }

    @Test
    public void activationStartsTimerAndConsumesLure() {
        Lure lure = new Lure(GameConfig.defaults());

        assertTrue(lure.activate(new Ghost(GHOST_POSITION)));

        assertTrue(lure.isActive());
        assertTrue(lure.isConsumed());
        assertEquals(DURATION_S, lure.getRemainingS(), 0.0);
    }

    @Test
    public void secondActivationIsRejectedEvenAfterExpiry() {
        Lure lure = new Lure(GameConfig.defaults());
        Ghost ghost = new Ghost(GHOST_POSITION);
        assertTrue(lure.activate(ghost));

        feed(lure, DURATION_S);
        assertFalse(lure.isActive());
        assertTrue(lure.isConsumed());

        assertFalse(lure.activate(ghost));
        assertEquals(0.0, lure.getRemainingS(), 0.0);
    }

    @Test
    public void activationIsRejectedWhenGhostIsStunned() {
        Lure lure = new Lure(GameConfig.defaults());
        Ghost ghost = new Ghost(GHOST_POSITION);
        ghost.stun(5.0);

        assertFalse(lure.activate(ghost));
        assertFalse(lure.isActive());
        assertFalse(lure.isConsumed());

        ghost.advanceStun(5.0);
        assertTrue(lure.activate(ghost));
    }

    @Test
    public void timerExpiresAfterConfiguredDuration() {
        Lure lure = new Lure(GameConfig.defaults());
        assertTrue(lure.activate(new Ghost(GHOST_POSITION)));

        feed(lure, 7.9);
        assertTrue(lure.isActive());
        assertEquals(0.1, lure.getRemainingS(), 1e-9);

        feed(lure, 0.1);
        assertFalse(lure.isActive());
        assertEquals(0.0, lure.getRemainingS(), 0.0);
    }

    @Test
    public void timerContinuesIfGhostBecomesStunnedAfterActivation() {
        Lure lure = new Lure(GameConfig.defaults());
        Ghost ghost = new Ghost(GHOST_POSITION);
        assertTrue(lure.activate(ghost));

        ghost.stun(10.0);
        feed(lure, DURATION_S);

        assertFalse(lure.isActive());
        assertTrue(lure.isConsumed());
    }

    @Test
    public void updateWithoutActivationIsIgnored() {
        Lure lure = new Lure(GameConfig.defaults());

        lure.update(5.0);

        assertFalse(lure.isActive());
        assertEquals(0.0, lure.getRemainingS(), 0.0);
    }

    @Test
    public void zeroDeltaIsIgnored() {
        Lure lure = new Lure(GameConfig.defaults());
        assertTrue(lure.activate(new Ghost(GHOST_POSITION)));

        lure.update(0.0);

        assertTrue(lure.isActive());
        assertEquals(DURATION_S, lure.getRemainingS(), 0.0);
    }

    @Test
    public void activeLurePullsGhostTowardPlayerDespiteNoise() {
        PlayArea area = playArea();
        GhostMovement movement = new GhostMovement(GameConfig.defaults(), area);
        Lure lure = new Lure(GameConfig.defaults());
        Ghost ghost = new Ghost(GHOST_POSITION);
        assertTrue(lure.activate(ghost));

        movement.update(ghost, PLAYER_EAST, 1.0, true, lure.isActive());

        assertEquals(LURE_SPEED_MPS, distance(GHOST_POSITION, ghost.getPosition()), 0.02);
        assertTrue(ghost.getPosition().getLongitude() > GHOST_POSITION.getLongitude());
        assertEquals(distance(GHOST_POSITION, PLAYER_EAST) - LURE_SPEED_MPS,
                distance(ghost.getPosition(), PLAYER_EAST), 0.05);
    }

    @Test
    public void lureMovementIsClampedToPlayArea() {
        PlayArea area = playArea();
        GhostMovement movement = new GhostMovement(GameConfig.defaults(), area);
        Lure lure = new Lure(GameConfig.defaults());
        Ghost ghost = new Ghost(new LatLon(0.000005, 0.005));
        LatLon playerOutside = new LatLon(-0.001, 0.005);
        assertTrue(lure.activate(ghost));

        movement.update(ghost, playerOutside, 1.0, false, lure.isActive());

        assertEquals(0.0, ghost.getPosition().getLatitude(), 1e-9);
        assertEquals(ghost.getPosition(), area.clamp(ghost.getPosition()));
    }

    @Test
    public void rejectsInvalidArguments() {
        Lure lure = new Lure(GameConfig.defaults());

        assertThrows(IllegalArgumentException.class, () -> new Lure(null));
        assertThrows(IllegalArgumentException.class, () -> lure.activate(null));
        assertThrows(IllegalArgumentException.class, () -> lure.update(-0.1));
        assertThrows(IllegalArgumentException.class, () -> lure.update(Double.NaN));
        assertThrows(IllegalArgumentException.class,
                () -> new Lure(new GameConfig.Builder()
                        .setLureDurationS(0.0)
                        .build()));
        assertThrows(IllegalArgumentException.class,
                () -> new Lure(new GameConfig.Builder()
                        .setLureDurationS(-1.0)
                        .build()));
    }

    private static void feed(Lure lure, double totalSeconds) {
        int steps = (int) Math.round(totalSeconds / TICK_S);
        for (int step = 0; step < steps; step++) {
            lure.update(TICK_S);
        }
    }

    private static double distance(LatLon from, LatLon to) {
        return GeoMath.haversineDistanceMeters(from, to, EARTH_RADIUS_M);
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
