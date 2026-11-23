package com.example.ghostradar.domain.game;

import com.example.ghostradar.domain.config.GameConfig;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class GameSessionTest {

    private static final long SCREAMER_DELAY_MIN_MS = 1_000L;
    private static final long SCREAMER_DURATION_MS = 2_000L;
    private static final long COOLDOWN_MS = 30_000L;

    @Test
    public void startsInExplorationNotPausedWithoutCooldown() {
        GameSession session = newSession(GameConfig.defaults(), new FakeClock());

        assertEquals(GameState.EXPLORATION, session.getState());
        assertFalse(session.isPaused());
        assertEquals(0L, session.getCooldownRemainingMs());
    }

    @Test
    public void startTrackingEntersTracking() {
        GameSession session = newSession(GameConfig.defaults(), new FakeClock());

        assertTrue(session.startTracking());
        assertEquals(GameState.TRACKING, session.getState());
    }

    @Test
    public void startTrackingIsRejectedOutsideExploration() {
        GameSession session = newSession(GameConfig.defaults(), new FakeClock());

        assertTrue(session.startTracking());
        assertFalse(session.startTracking());
        assertEquals(GameState.TRACKING, session.getState());
    }

    @Test
    public void trackingTransitionsThroughScreamerDelayScreamerAndCapture() {
        FakeClock clock = new FakeClock(10_000L);
        GameSession session = newSession(GameConfig.defaults(), clock);

        assertTrue(session.startTracking());
        assertTrue(session.onGhostInCaptureRange());
        assertEquals(GameState.TRACKING, session.getState());

        clock.advanceMs(SCREAMER_DELAY_MIN_MS - 1);
        session.update();
        assertEquals(GameState.TRACKING, session.getState());

        clock.advanceMs(1);
        session.update();
        assertEquals(GameState.SCREAMER, session.getState());

        clock.advanceMs(SCREAMER_DURATION_MS - 1);
        session.update();
        assertEquals(GameState.SCREAMER, session.getState());

        clock.advanceMs(1);
        session.update();
        assertEquals(GameState.CAPTURE, session.getState());
    }

    @Test
    public void screamerDelayIsDrawnFromConfiguredRange() {
        FakeClock clock = new FakeClock();
        GameSession session = newSession(GameConfig.defaults(), clock, 0.5);

        session.startTracking();
        session.onGhostInCaptureRange();

        clock.advanceMs(1_499);
        session.update();
        assertEquals(GameState.TRACKING, session.getState());

        clock.advanceMs(1);
        session.update();
        assertEquals(GameState.SCREAMER, session.getState());
    }

    @Test
    public void disabledScreamerGoesDirectlyToCapture() {
        GameConfig config = new GameConfig.Builder()
                .setScreamerEnabled(false)
                .build();
        GameSession session = newSession(config, new FakeClock());

        session.startTracking();
        assertTrue(session.onGhostInCaptureRange());

        assertEquals(GameState.CAPTURE, session.getState());
        assertEquals(COOLDOWN_MS, session.getCooldownRemainingMs());
    }

    @Test
    public void completeCaptureEntersRewardAndAcknowledgeReturnsToExploration() {
        FakeClock clock = new FakeClock();
        GameSession session = sessionInCapture(clock);

        assertTrue(session.completeCapture());
        assertEquals(GameState.RECOMPENSE, session.getState());

        assertTrue(session.acknowledgeReward());
        assertEquals(GameState.EXPLORATION, session.getState());
    }

    @Test
    public void failedCaptureReturnsToExploration() {
        GameSession session = sessionInCapture(new FakeClock());

        assertTrue(session.failCapture());
        assertEquals(GameState.EXPLORATION, session.getState());
    }

    @Test
    public void cooldownStartsWhenTrackingEndsAndBlocksNewTracking() {
        FakeClock clock = new FakeClock();
        GameSession session = newSession(GameConfig.defaults(), clock);

        session.startTracking();
        session.onGhostInCaptureRange();
        clock.advanceMs(SCREAMER_DELAY_MIN_MS);
        session.update();
        assertEquals(GameState.SCREAMER, session.getState());
        assertEquals(COOLDOWN_MS, session.getCooldownRemainingMs());

        clock.advanceMs(SCREAMER_DURATION_MS);
        session.update();
        assertTrue(session.failCapture());
        assertFalse(session.startTracking());

        clock.advanceMs(COOLDOWN_MS);
        session.update();
        assertEquals(0L, session.getCooldownRemainingMs());
        assertTrue(session.startTracking());
    }

    @Test
    public void cooldownCanExpireDuringCaptureAndAllowImmediateNewTracking() {
        FakeClock clock = new FakeClock();
        GameSession session = sessionInCapture(clock);
        assertEquals(28_000L, session.getCooldownRemainingMs());

        clock.advanceMs(30_000L);
        session.update();
        assertEquals(0L, session.getCooldownRemainingMs());

        assertTrue(session.completeCapture());
        assertTrue(session.acknowledgeReward());
        assertTrue(session.startTracking());
    }

    @Test
    public void cooldownUsesActualTransitionTimeWhenTickIsLate() {
        FakeClock clock = new FakeClock();
        GameSession session = newSession(GameConfig.defaults(), clock);

        session.startTracking();
        session.onGhostInCaptureRange();

        // Le tick traite la fin du délai de screamer avec 5 s de retard : les
        // 30 s doivent partir de l'heure réelle de transition, pas de
        // l'échéance théorique (qui donnerait seulement 25 s restantes).
        // L'état peut déjà être en CAPTURE : la fin du screamer reste ancrée
        // sur son échéance théorique, seul le cooldown change de référence.
        clock.advanceMs(SCREAMER_DELAY_MIN_MS + 5_000L);
        session.update();

        assertEquals(COOLDOWN_MS, session.getCooldownRemainingMs());
    }

    @Test
    public void outOfAreaPausesAndFreezesScreamerTimers() {
        FakeClock clock = new FakeClock();
        GameSession session = newSession(GameConfig.defaults(), clock);

        session.startTracking();
        session.onGhostInCaptureRange();

        clock.advanceMs(500);
        session.setPlayerInsidePlayArea(false);
        assertTrue(session.isPaused());

        clock.advanceMs(10_000);
        session.update();
        assertEquals(GameState.TRACKING, session.getState());

        session.setPlayerInsidePlayArea(true);
        assertFalse(session.isPaused());

        clock.advanceMs(499);
        session.update();
        assertEquals(GameState.TRACKING, session.getState());

        clock.advanceMs(1);
        session.update();
        assertEquals(GameState.SCREAMER, session.getState());
    }

    @Test
    public void outOfAreaPauseFreezesCooldown() {
        FakeClock clock = new FakeClock();
        GameSession session = sessionInCapture(clock);
        assertEquals(28_000L, session.getCooldownRemainingMs());

        clock.advanceMs(5_000);
        session.setPlayerInsidePlayArea(false);
        clock.advanceMs(20_000);
        session.update();
        assertEquals(23_000L, session.getCooldownRemainingMs());

        session.setPlayerInsidePlayArea(true);
        clock.advanceMs(5_000);
        session.update();
        assertEquals(18_000L, session.getCooldownRemainingMs());
    }

    @Test
    public void outOfAreaDoesNotPauseWhenDisabled() {
        GameConfig config = new GameConfig.Builder()
                .setPlayerOutOfAreaPause(false)
                .build();
        FakeClock clock = new FakeClock();
        GameSession session = newSession(config, clock);

        session.startTracking();
        session.onGhostInCaptureRange();
        session.setPlayerInsidePlayArea(false);

        assertFalse(session.isPaused());
        clock.advanceMs(SCREAMER_DELAY_MIN_MS);
        session.update();
        assertEquals(GameState.SCREAMER, session.getState());
    }

    @Test
    public void transitionsAreRejectedWhilePaused() {
        GameSession session = sessionInCapture(new FakeClock());
        session.setPlayerInsidePlayArea(false);
        assertTrue(session.isPaused());

        assertFalse(session.completeCapture());
        assertFalse(session.failCapture());
        assertEquals(GameState.CAPTURE, session.getState());

        session.setPlayerInsidePlayArea(true);
        assertTrue(session.completeCapture());
        assertEquals(GameState.RECOMPENSE, session.getState());
    }

    @Test
    public void invalidTransitionsAreRejected() {
        GameSession session = newSession(GameConfig.defaults(), new FakeClock());

        assertFalse(session.onGhostInCaptureRange());
        assertFalse(session.completeCapture());
        assertFalse(session.failCapture());
        assertFalse(session.acknowledgeReward());
        assertEquals(GameState.EXPLORATION, session.getState());
    }

    @Test
    public void rejectsNullDependencies() {
        GameConfig config = GameConfig.defaults();
        FakeClock clock = new FakeClock();
        FixedRandomSource random = new FixedRandomSource(0.0);

        assertThrows(IllegalArgumentException.class,
                () -> new GameSession(null, clock, random));
        assertThrows(IllegalArgumentException.class,
                () -> new GameSession(config, null, random));
        assertThrows(IllegalArgumentException.class,
                () -> new GameSession(config, clock, null));
    }

    private static GameSession newSession(GameConfig config, FakeClock clock) {
        return newSession(config, clock, 0.0);
    }

    private static GameSession newSession(GameConfig config, FakeClock clock, double randomValue) {
        return new GameSession(config, clock, new FixedRandomSource(randomValue));
    }

    private static GameSession sessionInCapture(FakeClock clock) {
        GameSession session = newSession(GameConfig.defaults(), clock);
        session.startTracking();
        session.onGhostInCaptureRange();
        clock.advanceMs(SCREAMER_DELAY_MIN_MS);
        session.update();
        clock.advanceMs(SCREAMER_DURATION_MS);
        session.update();
        assertEquals(GameState.CAPTURE, session.getState());
        return session;
    }

    private static final class FakeClock implements GameClock {

        private long nowMs;

        private FakeClock() {
            this(0L);
        }

        private FakeClock(long nowMs) {
            this.nowMs = nowMs;
        }

        @Override
        public long nowMs() {
            return nowMs;
        }

        private void advanceMs(long deltaMs) {
            nowMs += deltaMs;
        }
    }

    private static final class FixedRandomSource implements RandomSource {

        private final double value;

        private FixedRandomSource(double value) {
            this.value = value;
        }

        @Override
        public double nextDouble() {
            return value;
        }

        @Override
        public int nextInt(int boundExclusive) {
            throw new UnsupportedOperationException("nextInt not used by GameSessionTest");
        }
    }
}
