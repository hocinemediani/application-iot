package com.example.ghostradar.domain.game;

import com.example.ghostradar.domain.config.GameConfig;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GameSessionLifecycleTest {

    private static final long SCREAMER_DELAY_MIN_MS = 1_000L;
    private static final long SCREAMER_DURATION_MS = 2_000L;

    @Test
    public void lifecyclePauseFreezesScreamerTimers() {
        FakeClock clock = new FakeClock();
        GameSession session = newSession(clock);
        session.startTracking();
        session.onGhostInCaptureRange();

        clock.advanceMs(500);
        session.pauseForLifecycle();
        assertTrue(session.isPaused());

        clock.advanceMs(10_000);
        session.update();
        assertEquals(GameState.TRACKING, session.getState());

        session.resumeFromLifecycle();
        assertFalse(session.isPaused());

        clock.advanceMs(499);
        session.update();
        assertEquals(GameState.TRACKING, session.getState());

        clock.advanceMs(1);
        session.update();
        assertEquals(GameState.SCREAMER, session.getState());
    }

    @Test
    public void lifecyclePauseFreezesCooldown() {
        FakeClock clock = new FakeClock();
        GameSession session = sessionInCapture(clock);
        assertEquals(28_000L, session.getCooldownRemainingMs());

        clock.advanceMs(5_000);
        session.pauseForLifecycle();
        clock.advanceMs(20_000);
        session.update();
        assertEquals(23_000L, session.getCooldownRemainingMs());

        session.resumeFromLifecycle();
        clock.advanceMs(5_000);
        session.update();
        assertEquals(18_000L, session.getCooldownRemainingMs());
    }

    @Test
    public void transitionsAreRejectedDuringLifecyclePause() {
        FakeClock clock = new FakeClock();
        GameSession session = sessionInCapture(clock);

        session.pauseForLifecycle();
        assertFalse(session.completeCapture());
        assertFalse(session.failCapture());
        assertEquals(GameState.CAPTURE, session.getState());

        session.resumeFromLifecycle();
        assertTrue(session.completeCapture());
        assertEquals(GameState.RECOMPENSE, session.getState());
    }

    @Test
    public void lifecyclePauseAndResumeAreIdempotent() {
        FakeClock clock = new FakeClock();
        GameSession session = newSession(clock);
        session.startTracking();
        session.onGhostInCaptureRange();

        clock.advanceMs(500);
        session.pauseForLifecycle();
        session.pauseForLifecycle();
        clock.advanceMs(10_000);

        session.resumeFromLifecycle();
        session.resumeFromLifecycle();
        assertFalse(session.isPaused());

        clock.advanceMs(499);
        session.update();
        assertEquals(GameState.TRACKING, session.getState());

        clock.advanceMs(1);
        session.update();
        assertEquals(GameState.SCREAMER, session.getState());
    }

    @Test
    public void overlappingZoneAndLifecyclePauseShiftsDeadlinesOnce() {
        FakeClock clock = new FakeClock();
        GameSession session = newSession(clock);
        session.startTracking();
        session.onGhostInCaptureRange();

        clock.advanceMs(500);
        session.pauseForLifecycle();

        clock.advanceMs(1_000);
        session.setPlayerInsidePlayArea(false);
        assertTrue(session.isPaused());

        clock.advanceMs(5_000);
        session.setPlayerInsidePlayArea(true);
        assertTrue(session.isPaused());

        clock.advanceMs(500);
        session.resumeFromLifecycle();
        assertFalse(session.isPaused());

        session.update();
        assertEquals(GameState.TRACKING, session.getState());

        clock.advanceMs(499);
        session.update();
        assertEquals(GameState.TRACKING, session.getState());

        clock.advanceMs(1);
        session.update();
        assertEquals(GameState.SCREAMER, session.getState());
    }

    private static GameSession newSession(FakeClock clock) {
        return new GameSession(GameConfig.defaults(), clock, new FixedRandomSource(0.0));
    }

    private static GameSession sessionInCapture(FakeClock clock) {
        GameSession session = newSession(clock);
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
            throw new UnsupportedOperationException("nextInt not used by GameSessionLifecycleTest");
        }
    }
}
