package com.example.ghostradar.domain.game;

import com.example.ghostradar.domain.config.GameConfig;
import com.example.ghostradar.domain.geo.GeoMath;
import com.example.ghostradar.domain.geo.GeoPolygon;
import com.example.ghostradar.domain.geo.HauntedZone;
import com.example.ghostradar.domain.geo.LatLon;
import com.example.ghostradar.domain.geo.PlayArea;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class GhostSpawnerTest {

    private static final double EARTH_RADIUS_M = 6_371_000.0;
    private static final double MIN_VERTEX_DISTANCE_M = 0.5;
    private static final int MAX_ATTEMPTS = 100;
    private static final LatLon PLAYER = new LatLon(0.0, 0.0);

    @Test
    public void doesNotSpawnWhenPlayerIsOutsideZone() {
        PlayArea area = playArea();
        HauntedZone zone = HauntedZone.circle(
                new LatLon(0.001, 0.001), 50.0, EARTH_RADIUS_M);
        GhostSpawner spawner = new GhostSpawner(
                GameConfig.defaults(), area, new ScriptedRandomSource());

        Optional<LatLon> spawn = spawner.trySpawn(PLAYER, zone, 1.0, MAX_ATTEMPTS);

        assertFalse(spawn.isPresent());
    }

    @Test
    public void doesNotSpawnWhenProbabilityRollFails() {
        PlayArea area = playArea();
        HauntedZone zone = HauntedZone.circle(PLAYER, 100.0, EARTH_RADIUS_M);
        GhostSpawner spawner = new GhostSpawner(
                GameConfig.defaults(), area, new ScriptedRandomSource(0.5));

        Optional<LatLon> spawn = spawner.trySpawn(PLAYER, zone, 1.0, MAX_ATTEMPTS);

        assertFalse(spawn.isPresent());
    }

    @Test
    public void spawnsWithinConfiguredDistanceBounds() {
        PlayArea area = playArea();
        HauntedZone zone = HauntedZone.circle(PLAYER, 100.0, EARTH_RADIUS_M);

        LatLon min = spawnPoint(area, zone, new ScriptedRandomSource(0.0, 0.0, 0.0));
        LatLon max = spawnPoint(area, zone, new ScriptedRandomSource(0.0, 0.0, 0.999));

        assertEquals(15.0, distance(PLAYER, min), 0.05);
        assertEquals(40.0, distance(PLAYER, max), 0.1);
        assertTrue(zone.contains(min));
        assertTrue(zone.contains(max));
        assertTrue(area.contains(min));
        assertTrue(area.contains(max));
    }

    @Test
    public void probabilityUsesExactPerSecondConversion() {
        PlayArea area = playArea();
        HauntedZone zone = HauntedZone.circle(PLAYER, 100.0, EARTH_RADIUS_M);
        GameConfig config = new GameConfig.Builder()
                .setZoneTriggerProbabilityPerS(0.5)
                .build();

        GhostSpawner noSpawn = new GhostSpawner(
                config, area, new ScriptedRandomSource(0.76, 0.0, 0.0));
        GhostSpawner spawn = new GhostSpawner(
                config, area, new ScriptedRandomSource(0.74, 0.0, 0.0));

        assertFalse(noSpawn.trySpawn(PLAYER, zone, 2.0, MAX_ATTEMPTS).isPresent());
        assertTrue(spawn.trySpawn(PLAYER, zone, 2.0, MAX_ATTEMPTS).isPresent());
    }

    @Test
    public void fallbackChoosesNearestSampledPointToTarget() {
        PlayArea area = playArea();
        HauntedZone zone = HauntedZone.polygon(rectangle(
                0.0, 0.0, 0.0001, 0.0001));
        LatLon player = new LatLon(0.00005, 0.00005);
        GameConfig config = new GameConfig.Builder()
                .setZoneTriggerProbabilityPerS(1.0)
                .build();
        GhostSpawner spawner = new GhostSpawner(config, area, new ScriptedRandomSource(
                0.0, 0.0, 0.2,
                0.89, 0.5,
                0.1, 0.5));

        Optional<LatLon> spawn = spawner.trySpawn(player, zone, 1.0, 2);

        assertTrue(spawn.isPresent());
        assertEquals(0.000089, spawn.get().getLatitude(), 1e-9);
        assertEquals(0.00005, spawn.get().getLongitude(), 1e-9);
        assertTrue(zone.contains(spawn.get()));
        assertTrue(area.contains(spawn.get()));
    }

    @Test
    public void returnsEmptyWhenNoValidPointExists() {
        PlayArea area = new PlayArea(
                square(-0.01, -0.01, 0.01, 0.01),
                Collections.singletonList(square(-0.001, -0.001, 0.001, 0.001)),
                EARTH_RADIUS_M);
        HauntedZone zone = HauntedZone.circle(PLAYER, 50.0, EARTH_RADIUS_M);
        GameConfig config = new GameConfig.Builder()
                .setZoneTriggerProbabilityPerS(1.0)
                .build();
        GhostSpawner spawner = new GhostSpawner(
                config, area, RandomSource.seeded(42L));

        Optional<LatLon> spawn = spawner.trySpawn(PLAYER, zone, 1.0, 20);

        assertFalse(spawn.isPresent());
    }

    @Test
    public void adminSpawnReturnsRequestedPointWithoutValidation() {
        PlayArea area = playArea();
        GhostSpawner spawner = new GhostSpawner(
                GameConfig.defaults(), area, new ScriptedRandomSource());
        LatLon adminPoint = new LatLon(48.0, 2.0);

        assertSame(adminPoint, spawner.spawnAdmin(adminPoint));
    }

    @Test
    public void rejectsInvalidArguments() {
        PlayArea area = playArea();
        HauntedZone zone = HauntedZone.circle(PLAYER, 100.0, EARTH_RADIUS_M);
        GameConfig config = GameConfig.defaults();
        GhostSpawner spawner = new GhostSpawner(
                config, area, new ScriptedRandomSource());

        assertThrows(IllegalArgumentException.class,
                () -> spawner.trySpawn(null, zone, 1.0, MAX_ATTEMPTS));
        assertThrows(IllegalArgumentException.class,
                () -> spawner.trySpawn(PLAYER, null, 1.0, MAX_ATTEMPTS));
        assertThrows(IllegalArgumentException.class,
                () -> spawner.trySpawn(PLAYER, zone, -0.1, MAX_ATTEMPTS));
        assertThrows(IllegalArgumentException.class,
                () -> spawner.trySpawn(PLAYER, zone, 1.0, 0));
        assertThrows(IllegalArgumentException.class,
                () -> spawner.spawnAdmin(null));
        assertThrows(IllegalArgumentException.class,
                () -> new GhostSpawner(null, area, new ScriptedRandomSource()));
        assertThrows(IllegalArgumentException.class,
                () -> new GhostSpawner(config, null, new ScriptedRandomSource()));
        assertThrows(IllegalArgumentException.class,
                () -> new GhostSpawner(config, area, null));
    }

    private static LatLon spawnPoint(PlayArea area, HauntedZone zone,
                                     RandomSource random) {
        GhostSpawner spawner = new GhostSpawner(GameConfig.defaults(), area, random);
        Optional<LatLon> spawn = spawner.trySpawn(PLAYER, zone, 1.0, MAX_ATTEMPTS);
        assertTrue(spawn.isPresent());
        return spawn.get();
    }

    private static double distance(LatLon from, LatLon to) {
        return GeoMath.haversineDistanceMeters(from, to, EARTH_RADIUS_M);
    }

    private static PlayArea playArea() {
        return new PlayArea(square(-0.01, -0.01, 0.01, 0.01),
                Collections.emptyList(), EARTH_RADIUS_M);
    }

    private static GeoPolygon square(double minLat, double minLon,
                                     double maxLat, double maxLon) {
        return polygon(
                new LatLon(minLat, minLon),
                new LatLon(minLat, maxLon),
                new LatLon(maxLat, maxLon),
                new LatLon(maxLat, minLon)
        );
    }

    private static GeoPolygon rectangle(double minLat, double minLon,
                                        double maxLat, double maxLon) {
        return square(minLat, minLon, maxLat, maxLon);
    }

    private static GeoPolygon polygon(LatLon... vertices) {
        return new GeoPolygon(Arrays.asList(vertices), 3,
                MIN_VERTEX_DISTANCE_M, EARTH_RADIUS_M);
    }

    private static final class ScriptedRandomSource implements RandomSource {

        private final double[] values;
        private int index;

        private ScriptedRandomSource(double... values) {
            this.values = values;
        }

        @Override
        public double nextDouble() {
            if (index >= values.length) {
                throw new IllegalStateException("No more scripted random values");
            }
            return values[index++];
        }

        @Override
        public int nextInt(int boundExclusive) {
            throw new UnsupportedOperationException("nextInt not used by GhostSpawnerTest");
        }
    }
}
