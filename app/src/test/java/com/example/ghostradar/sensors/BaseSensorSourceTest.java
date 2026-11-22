package com.example.ghostradar.sensors;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class BaseSensorSourceTest {

    @Test
    public void startRejectsNullListener() {
        FakeSource source = new FakeSource();

        assertThrows(IllegalArgumentException.class, () -> source.start(null));
        assertFalse(source.isRunning());
    }

    @Test
    public void startInvokesOnStartAndSetsRunning() {
        FakeSource source = new FakeSource();
        RecordingListener listener = new RecordingListener();

        source.start(listener);

        assertEquals(1, source.startCount);
        assertTrue(source.isRunning());
        assertTrue(listener.errors.isEmpty());
    }

    @Test
    public void startEmitsUnavailableWhenOnStartFails() {
        FakeSource source = new FakeSource();
        source.startError = SensorError.PERMISSION_DENIED;
        RecordingListener listener = new RecordingListener();

        source.start(listener);

        assertEquals(1, source.startCount);
        assertFalse(source.isRunning());
        assertEquals(1, listener.errors.size());
        assertEquals(SensorError.PERMISSION_DENIED, listener.errors.get(0));
    }

    @Test
    public void startEmitsUnknownWhenOnStartThrows() {
        FakeSource source = new FakeSource();
        source.throwOnStart = true;
        RecordingListener listener = new RecordingListener();

        source.start(listener);

        assertFalse(source.isRunning());
        assertEquals(1, listener.errors.size());
        assertEquals(SensorError.UNKNOWN, listener.errors.get(0));
    }

    @Test
    public void startTwiceReplacesListenerWithoutRestarting() {
        FakeSource source = new FakeSource();
        RecordingListener first = new RecordingListener();
        RecordingListener second = new RecordingListener();

        source.start(first);
        source.start(second);
        source.emitSample("hello");

        assertEquals(1, source.startCount);
        assertTrue(first.samples.isEmpty());
        assertEquals(1, second.samples.size());
        assertEquals("hello", second.samples.get(0));
    }

    @Test
    public void stopInvokesOnStopAndClearsRunning() {
        FakeSource source = new FakeSource();
        RecordingListener listener = new RecordingListener();
        source.start(listener);

        source.stop();
        source.emitSample("ignored");

        assertEquals(1, source.stopCount);
        assertFalse(source.isRunning());
        assertTrue(listener.samples.isEmpty());
    }

    @Test
    public void stopTwiceInvokesOnStopOnce() {
        FakeSource source = new FakeSource();
        source.start(new RecordingListener());

        source.stop();
        source.stop();

        assertEquals(1, source.stopCount);
    }

    @Test
    public void stopWithoutStartDoesNothing() {
        FakeSource source = new FakeSource();

        source.stop();

        assertEquals(0, source.stopCount);
        assertFalse(source.isRunning());
    }

    @Test
    public void emitBeforeStartIsIgnored() {
        FakeSource source = new FakeSource();

        source.emitSample("ignored");

        assertFalse(source.isRunning());
    }

    @Test
    public void emitAfterStopIsIgnored() {
        FakeSource source = new FakeSource();
        RecordingListener listener = new RecordingListener();
        source.start(listener);
        source.stop();

        source.emitSample("ignored");

        assertTrue(listener.samples.isEmpty());
    }

    @Test
    public void emitDeliversSampleToCurrentListener() {
        FakeSource source = new FakeSource();
        RecordingListener listener = new RecordingListener();
        source.start(listener);

        source.emitSample("sample");

        assertEquals(1, listener.samples.size());
        assertEquals("sample", listener.samples.get(0));
    }

    @Test
    public void emitRejectsNullSample() {
        FakeSource source = new FakeSource();
        source.start(new RecordingListener());

        assertThrows(IllegalArgumentException.class, () -> source.emitSample(null));
    }

    @Test
    public void emitUnavailableDeliversError() {
        FakeSource source = new FakeSource();
        RecordingListener listener = new RecordingListener();
        source.start(listener);

        source.emitError(SensorError.SERVICE_DISABLED);

        assertEquals(1, listener.errors.size());
        assertEquals(SensorError.SERVICE_DISABLED, listener.errors.get(0));
    }

    @Test
    public void emitUnavailableRejectsNullError() {
        FakeSource source = new FakeSource();
        source.start(new RecordingListener());

        assertThrows(IllegalArgumentException.class, () -> source.emitError(null));
    }

    @Test
    public void startAfterFailureCanRetry() {
        FakeSource source = new FakeSource();
        source.startError = SensorError.SENSOR_UNAVAILABLE;
        RecordingListener first = new RecordingListener();
        source.start(first);
        assertFalse(source.isRunning());

        source.startError = null;
        RecordingListener second = new RecordingListener();
        source.start(second);

        assertEquals(2, source.startCount);
        assertTrue(source.isRunning());
    }

    private static final class FakeSource extends BaseSensorSource<String> {

        private SensorError startError;
        private boolean throwOnStart;
        private int startCount;
        private int stopCount;

        @Override
        protected SensorError onStart() {
            startCount++;
            if (throwOnStart) {
                throw new IllegalStateException("boom");
            }
            return startError;
        }

        @Override
        protected void onStop() {
            stopCount++;
        }

        private void emitSample(String sample) {
            emit(sample);
        }

        private void emitError(SensorError error) {
            emitUnavailable(error);
        }
    }

    private static final class RecordingListener implements SensorListener<String> {

        private final List<String> samples = new ArrayList<>();
        private final List<SensorError> errors = new ArrayList<>();

        @Override
        public void onSample(String sample) {
            samples.add(sample);
        }

        @Override
        public void onUnavailable(SensorError error) {
            errors.add(error);
        }
    }
}
