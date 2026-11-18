package com.example.ghostradar.domain.ai;

import com.example.ghostradar.domain.config.GameConfig;
import com.example.ghostradar.domain.entity.Ghost;
import com.example.ghostradar.domain.entity.GhostState;
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

public class GhostMovementTest {

    private static final double EARTH_RADIUS_M = 6_371_000.0;
    private static final double MIN_VERTEX_DISTANCE_M = 0.5;
    private static final double BASE_SPEED_MPS = 1.2;
    private static final double FLEE_SPEED_MPS = 2.4;
    private static final double LURE_SPEED_MPS = 1.5;

    private static final LatLon GHOST_POSITION = new LatLon(0.005, 0.005);
    private static final LatLon PLAYER_EAST = new LatLon(0.005, 0.006);

    @Test
    public void ghostStartsActiveWithoutStun() {
        Ghost ghost = new Ghost(GHOST_POSITION);

        assertEquals(GHOST_POSITION, ghost.getPosition());
        assertEquals(GhostState.ACTIVE, ghost.getState());
        assertFalse(ghost.isStunned());
        assertEquals(0.0, ghost.getStunRemainingS(), 0.0);
    }

    @Test
    public void fleesDirectlyAwayFromPlayerAtBaseSpeed() {
        PlayArea area = playArea();
        GhostMovement movement = new GhostMovement(GameConfig.defaults(), area);
        Ghost ghost = new Ghost(GHOST_POSITION);

        movement.update(ghost, PLAYER_EAST, 1.0, false, false);

        double movedM = distance(GHOST_POSITION, ghost.getPosition());
        assertEquals(BASE_SPEED_MPS, movedM, 0.02);
        assertTrue(ghost.getPosition().getLongitude() < GHOST_POSITION.getLongitude());
        assertEquals(GHOST_POSITION.getLatitude(),
                ghost.getPosition().getLatitude(), 1e-9);
        assertEquals(distance(GHOST_POSITION, PLAYER_EAST) + BASE_SPEED_MPS,
                distance(ghost.getPosition(), PLAYER_EAST), 0.05);
    }

    @Test
    public void noiseAlertUsesFleeSpeedMultiplier() {
        PlayArea area = playArea();
        GhostMovement movement = new GhostMovement(GameConfig.defaults(), area);
        Ghost ghost = new Ghost(GHOST_POSITION);

        movement.update(ghost, PLAYER_EAST, 1.0, true, false);

        double movedM = distance(GHOST_POSITION, ghost.getPosition());
        assertEquals(FLEE_SPEED_MPS, movedM, 0.02);
        assertTrue(ghost.getPosition().getLongitude() < GHOST_POSITION.getLongitude());
        assertEquals(distance(GHOST_POSITION, PLAYER_EAST) + FLEE_SPEED_MPS,
                distance(ghost.getPosition(), PLAYER_EAST), 0.05);
    }

    @Test
    public void lureMovesGhostTowardPlayerAtLureSpeed() {
        PlayArea area = playArea();
        GhostMovement movement = new GhostMovement(GameConfig.defaults(), area);
        Ghost ghost = new Ghost(GHOST_POSITION);

        movement.update(ghost, PLAYER_EAST, 1.0, false, true);

        double movedM = distance(GHOST_POSITION, ghost.getPosition());
        assertEquals(LURE_SPEED_MPS, movedM, 0.02);
        assertTrue(ghost.getPosition().getLongitude() > GHOST_POSITION.getLongitude());
        assertEquals(distance(GHOST_POSITION, PLAYER_EAST) - LURE_SPEED_MPS,
                distance(ghost.getPosition(), PLAYER_EAST), 0.05);
    }

    @Test
    public void lureTakesPriorityOverNoiseAlert() {
        PlayArea area = playArea();
        GhostMovement movement = new GhostMovement(GameConfig.defaults(), area);
        Ghost ghost = new Ghost(GHOST_POSITION);

        movement.update(ghost, PLAYER_EAST, 1.0, true, true);

        double movedM = distance(GHOST_POSITION, ghost.getPosition());
        assertEquals(LURE_SPEED_MPS, movedM, 0.02);
        assertTrue(ghost.getPosition().getLongitude() > GHOST_POSITION.getLongitude());
    }

    @Test
    public void lureStopsExactlyAtPlayerWithoutOvershoot() {
        PlayArea area = playArea();
        GhostMovement movement = new GhostMovement(GameConfig.defaults(), area);
        Ghost ghost = new Ghost(GHOST_POSITION);
        LatLon player = new LatLon(0.005, 0.0051);

        movement.update(ghost, player, 10.0, false, true);

        assertEquals(player.getLatitude(), ghost.getPosition().getLatitude(), 1e-12);
        assertEquals(player.getLongitude(), ghost.getPosition().getLongitude(), 1e-12);
        assertEquals(0.0, distance(ghost.getPosition(), player), 1e-9);
    }

    @Test
    public void stunFreezesMovementAndConsumesTimer() {
        PlayArea area = playArea();
        GhostMovement movement = new GhostMovement(GameConfig.defaults(), area);
        Ghost ghost = new Ghost(GHOST_POSITION);
        ghost.stun(10.0);

        movement.update(ghost, PLAYER_EAST, 1.0, true, true);

        assertEquals(GhostState.STUNNED, ghost.getState());
        assertTrue(ghost.isStunned());
        assertEquals(9.0, ghost.getStunRemainingS(), 1e-9);
        assertEquals(GHOST_POSITION, ghost.getPosition());
    }

    @Test
    public void movementResumesOnTickAfterStunExpires() {
        PlayArea area = playArea();
        GhostMovement movement = new GhostMovement(GameConfig.defaults(), area);
        Ghost ghost = new Ghost(GHOST_POSITION);
        ghost.stun(0.5);

        movement.update(ghost, PLAYER_EAST, 1.0, false, false);

        assertEquals(GHOST_POSITION, ghost.getPosition());
        assertFalse(ghost.isStunned());
        assertEquals(GhostState.ACTIVE, ghost.getState());
        assertEquals(0.0, ghost.getStunRemainingS(), 0.0);

        movement.update(ghost, PLAYER_EAST, 1.0, false, false);

        double movedM = distance(GHOST_POSITION, ghost.getPosition());
        assertEquals(BASE_SPEED_MPS, movedM, 0.02);
    }

    @Test
    public void stunNeverMakesRemainingTimeNegative() {
        Ghost ghost = new Ghost(GHOST_POSITION);
        ghost.stun(1.0);

        ghost.advanceStun(5.0);

        assertEquals(0.0, ghost.getStunRemainingS(), 0.0);
        assertEquals(GhostState.ACTIVE, ghost.getState());
        assertFalse(ghost.isStunned());
    }

    @Test
    public void zeroDeltaSecondsLeavesPositionUnchanged() {
        PlayArea area = playArea();
        GhostMovement movement = new GhostMovement(GameConfig.defaults(), area);
        Ghost ghost = new Ghost(GHOST_POSITION);

        movement.update(ghost, PLAYER_EAST, 0.0, true, true);

        assertEquals(GHOST_POSITION, ghost.getPosition());
    }

    @Test
    public void clampsAtOuterBoundaryAndSlidesTangentially() {
        PlayArea area = playArea();
        GhostMovement movement = new GhostMovement(GameConfig.defaults(), area);
        LatLon start = new LatLon(0.000005, 0.005);
        Ghost ghost = new Ghost(start);
        LatLon player = new LatLon(0.001, 0.006);

        movement.update(ghost, player, 1.0, false, false);

        LatLon end = ghost.getPosition();
        assertEquals(0.0, end.getLatitude(), 1e-9);
        assertTrue(end.getLongitude() < start.getLongitude());
        assertEquals(start.getLongitude() - end.getLongitude(), 0.0000076, 0.000001);
        double movedM = distance(start, end);
        assertTrue(movedM > 0.5);
        assertTrue(movedM < BASE_SPEED_MPS);
        assertEquals(end, area.clamp(end));
    }

    @Test
    public void clampsAtObstacleAndSlidesTangentially() {
        PlayArea area = playAreaWithObstacle();
        GhostMovement movement = new GhostMovement(GameConfig.defaults(), area);
        LatLon start = new LatLon(0.003999, 0.005);
        Ghost ghost = new Ghost(start);
        LatLon player = new LatLon(0.003, 0.004);

        movement.update(ghost, player, 1.0, false, false);

        LatLon end = ghost.getPosition();
        assertEquals(0.004, end.getLatitude(), 1e-9);
        assertTrue(end.getLongitude() > start.getLongitude());
        assertEquals(end.getLongitude() - start.getLongitude(), 0.0000076, 0.000001);
        double movedM = distance(start, end);
        assertTrue(movedM > 0.5);
        assertTrue(movedM < BASE_SPEED_MPS);
        assertEquals(end, area.clamp(end));
    }

    @Test
    public void rejectsInvalidArguments() {
        PlayArea area = playArea();
        GhostMovement movement = new GhostMovement(GameConfig.defaults(), area);
        Ghost ghost = new Ghost(GHOST_POSITION);

        assertThrows(IllegalArgumentException.class,
                () -> new GhostMovement(null, area));
        assertThrows(IllegalArgumentException.class,
                () -> new GhostMovement(GameConfig.defaults(), null));
        assertThrows(IllegalArgumentException.class,
                () -> movement.update(null, PLAYER_EAST, 1.0, false, false));
        assertThrows(IllegalArgumentException.class,
                () -> movement.update(ghost, null, 1.0, false, false));
        assertThrows(IllegalArgumentException.class,
                () -> movement.update(ghost, PLAYER_EAST, -0.1, false, false));
        assertThrows(IllegalArgumentException.class, () -> new Ghost(null));
        assertThrows(IllegalArgumentException.class, () -> ghost.setPosition(null));
        assertThrows(IllegalArgumentException.class, () -> ghost.stun(-1.0));
        assertThrows(IllegalArgumentException.class, () -> ghost.stun(0.0));
    }

    private static double distance(LatLon from, LatLon to) {
        return GeoMath.haversineDistanceMeters(from, to, EARTH_RADIUS_M);
    }

    private static PlayArea playArea() {
        return new PlayArea(square(0.0, 0.0, 0.01, 0.01),
                Collections.emptyList(), EARTH_RADIUS_M);
    }

    private static PlayArea playAreaWithObstacle() {
        return new PlayArea(square(0.0, 0.0, 0.01, 0.01),
                Collections.singletonList(square(0.004, 0.004, 0.006, 0.006)),
                EARTH_RADIUS_M);
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
