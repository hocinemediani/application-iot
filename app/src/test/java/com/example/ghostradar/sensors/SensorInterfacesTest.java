package com.example.ghostradar.sensors;

import com.example.ghostradar.domain.geo.LatLon;
import com.example.ghostradar.domain.sensors.AccelerometerSample;
import com.example.ghostradar.domain.sensors.CompassAccuracy;
import com.example.ghostradar.domain.sensors.CompassSample;
import com.example.ghostradar.domain.sensors.GpsSample;
import com.example.ghostradar.domain.sensors.NoiseSample;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SensorInterfacesTest {

    @Test
    public void gpsSensorDeliversGpsSamples() {
        FakeGpsSensor sensor = new FakeGpsSensor();
        RecordingListener<GpsSample> listener = new RecordingListener<>();
        sensor.start(listener);

        GpsSample sample = new GpsSample(new LatLon(48.0, 2.0), 5.0, 1L);
        sensor.emitSample(sample);

        assertEquals(1, listener.samples.size());
        assertEquals(sample, listener.samples.get(0));
    }

    @Test
    public void compassSensorDeliversCompassSamples() {
        FakeCompassSensor sensor = new FakeCompassSensor();
        RecordingListener<CompassSample> listener = new RecordingListener<>();
        sensor.start(listener);

        CompassSample sample = new CompassSample(90.0, CompassAccuracy.HIGH, 1L);
        sensor.emitSample(sample);

        assertEquals(1, listener.samples.size());
        assertEquals(sample, listener.samples.get(0));
    }

    @Test
    public void microphoneSensorDeliversNoiseSamples() {
        FakeMicrophoneSensor sensor = new FakeMicrophoneSensor();
        RecordingListener<NoiseSample> listener = new RecordingListener<>();
        sensor.start(listener);

        NoiseSample sample = new NoiseSample(0.5, 20.0, 1L);
        sensor.emitSample(sample);

        assertEquals(1, listener.samples.size());
        assertEquals(sample, listener.samples.get(0));
    }

    @Test
    public void accelerometerSensorDeliversAccelerometerSamples() {
        FakeAccelerometerSensor sensor = new FakeAccelerometerSensor();
        RecordingListener<AccelerometerSample> listener = new RecordingListener<>();
        sensor.start(listener);

        AccelerometerSample sample = new AccelerometerSample(0.0, 9.81, 0.0, 1L);
        sensor.emitSample(sample);

        assertEquals(1, listener.samples.size());
        assertEquals(sample, listener.samples.get(0));
    }

    @Test
    public void microphoneConstantsMatchChosenConfiguration() {
        assertEquals(44_100, MicrophoneSensor.SAMPLE_RATE_HZ);
        assertEquals(20, MicrophoneSensor.BLOCK_DURATION_MS);
        assertEquals(882, MicrophoneSensor.BLOCK_FRAMES);
    }

    @Test
    public void sensorInterfacesAreUsableThroughGenericContract() {
        SensorSource<GpsSample> gps = new FakeGpsSensor();
        SensorSource<CompassSample> compass = new FakeCompassSensor();
        SensorSource<NoiseSample> microphone = new FakeMicrophoneSensor();
        SensorSource<AccelerometerSample> accelerometer = new FakeAccelerometerSensor();

        assertTrue(gps instanceof GpsSensor);
        assertTrue(compass instanceof CompassSensor);
        assertTrue(microphone instanceof MicrophoneSensor);
        assertTrue(accelerometer instanceof AccelerometerSensor);
    }

    private abstract static class FakeSource<T> extends BaseSensorSource<T> {

        @Override
        protected SensorError onStart() {
            return null;
        }

        @Override
        protected void onStop() {
        }

        protected void emitSample(T sample) {
            emit(sample);
        }
    }

    private static final class FakeGpsSensor extends FakeSource<GpsSample>
            implements GpsSensor {
    }

    private static final class FakeCompassSensor extends FakeSource<CompassSample>
            implements CompassSensor {
    }

    private static final class FakeMicrophoneSensor extends FakeSource<NoiseSample>
            implements MicrophoneSensor {
    }

    private static final class FakeAccelerometerSensor extends FakeSource<AccelerometerSample>
            implements AccelerometerSensor {
    }

    private static final class RecordingListener<T> implements SensorListener<T> {

        private final java.util.List<T> samples = new java.util.ArrayList<>();

        @Override
        public void onSample(T sample) {
            samples.add(sample);
        }

        @Override
        public void onUnavailable(SensorError error) {
        }
    }
}
