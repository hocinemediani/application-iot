package com.example.ghostradar.domain.game;

import com.example.ghostradar.domain.config.GameConfig;
import com.example.ghostradar.domain.sensors.NoiseSample;
import com.example.ghostradar.domain.sensors.SensorFrame;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class GameLoopTest {

    private static final long SCREAMER_DELAY_MIN_MS = 1_000L;
    private static final long SCREAMER_DURATION_MS = 2_000L;

    @Test
    public void rejectsNullDependencies() {
        FakeClock clock = new FakeClock();
        GameSession session = newSession(clock);
        GameInput input = new GameInput();
        RecordingListener listener = new RecordingListener();

        assertThrows(IllegalArgumentException.class,
                () -> new GameLoop(null, input, listener));
        assertThrows(IllegalArgumentException.class,
                () -> new GameLoop(session, null, listener));
        assertThrows(IllegalArgumentException.class,
                () -> new GameLoop(session, input, null));
    }

    @Test
    public void rejectsNullFrame() {
        GameLoop loop = newLoop(newSession(new FakeClock()), new RecordingListener());

        assertThrows(IllegalArgumentException.class, () -> loop.onFrame(null));
    }

    @Test
    public void frameAdvancesSessionAndNotifiesTransitions() {
        FakeClock clock = new FakeClock();
        GameSession session = newSession(clock);
        RecordingListener listener = new RecordingListener();
        GameLoop loop = newLoop(session, listener);

        session.startTracking();
        session.onGhostInCaptureRange();

        loop.onFrame(SensorFrame.EMPTY);
        assertEquals(1, listener.events.size());
        assertEquals(GameState.EXPLORATION, listener.events.get(0)[0]);
        assertEquals(GameState.TRACKING, listener.events.get(0)[1]);

        clock.advanceMs(SCREAMER_DELAY_MIN_MS);
        loop.onFrame(SensorFrame.EMPTY);
        assertEquals(2, listener.events.size());
        assertEquals(GameState.TRACKING, listener.events.get(1)[0]);
        assertEquals(GameState.SCREAMER, listener.events.get(1)[1]);

        clock.advanceMs(SCREAMER_DURATION_MS);
        loop.onFrame(SensorFrame.EMPTY);
        assertEquals(3, listener.events.size());
        assertEquals(GameState.SCREAMER, listener.events.get(2)[0]);
        assertEquals(GameState.CAPTURE, listener.events.get(2)[1]);
    }

    @Test
    public void stableStateDoesNotNotifyListener() {
        RecordingListener listener = new RecordingListener();
        GameLoop loop = newLoop(newSession(new FakeClock()), listener);

        loop.onFrame(SensorFrame.EMPTY);
        loop.onFrame(SensorFrame.EMPTY);
        loop.onFrame(SensorFrame.EMPTY);

        assertEquals(0, listener.events.size());
    }

    @Test
    public void snapshotReflectsSessionStateAndInput() {
        FakeClock clock = new FakeClock();
        GameSession session = newSession(clock);
        GameInput input = new GameInput(0, 1_013.0);
        GameLoop loop = new GameLoop(session, input, new RecordingListener());

        GameSnapshot snapshot = loop.getSnapshot();
        assertEquals(GameState.EXPLORATION, snapshot.getState());
        assertEquals(0, snapshot.getFloor());
        assertEquals(1_013.0, snapshot.getPressureHpa(), 0.0001);

        session.startTracking();
        assertEquals(GameState.TRACKING, loop.getSnapshot().getState());
    }

    @Test
    public void lifecyclePauseStopsFrameAdvancementAndResumeContinues() {
        FakeClock clock = new FakeClock();
        GameSession session = newSession(clock);
        RecordingListener listener = new RecordingListener();
        GameLoop loop = newLoop(session, listener);

        session.startTracking();
        session.onGhostInCaptureRange();
        loop.onFrame(SensorFrame.EMPTY);
        assertEquals(1, listener.events.size());

        loop.pause();
        assertTrue(loop.isPaused());

        clock.advanceMs(5_000);
        loop.onFrame(SensorFrame.EMPTY);
        assertEquals(GameState.TRACKING, session.getState());
        assertEquals(1, listener.events.size());

        loop.resume();
        assertFalse(loop.isPaused());

        clock.advanceMs(999);
        loop.onFrame(SensorFrame.EMPTY);
        assertEquals(GameState.TRACKING, session.getState());
        assertEquals(1, listener.events.size());

        clock.advanceMs(1);
        loop.onFrame(SensorFrame.EMPTY);
        assertEquals(GameState.SCREAMER, session.getState());
        assertEquals(2, listener.events.size());
        assertEquals(GameState.TRACKING, listener.events.get(1)[0]);
        assertEquals(GameState.SCREAMER, listener.events.get(1)[1]);
    }

    @Test
    public void keepsLatestSensorFrame() {
        GameLoop loop = newLoop(newSession(new FakeClock()), new RecordingListener());
        assertSame(SensorFrame.EMPTY, loop.getLatestFrame());

        SensorFrame frame = new SensorFrame(
                null, null, new NoiseSample(0.5, 20.0, 1L), null);
        loop.onFrame(frame);
        assertSame(frame, loop.getLatestFrame());
    }

    private static GameLoop newLoop(GameSession session, RecordingListener listener) {
        return new GameLoop(session, new GameInput(), listener);
    }

    private static GameSession newSession(FakeClock clock) {
        return new GameSession(GameConfig.defaults(), clock, new FixedRandomSource(0.0));
    }

    private static final class RecordingListener implements GameLoop.Listener {

        private final List<GameState[]> events = new ArrayList<>();

        @Override
        public void onStateChanged(GameState previous, GameState current) {
            events.add(new GameState[] {previous, current});
        }
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
            throw new UnsupportedOperationException("nextInt not used by GameLoopTest");
        }
    }
}
