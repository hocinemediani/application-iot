package com.example.ghostradar.domain.sensors;

import com.example.ghostradar.domain.config.GameConfig;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class TiltCalculatorTest {

    private static final double G = 9.81;

    @Test
    public void rejectsNullConfiguration() {
        assertThrows(IllegalArgumentException.class, () -> new TiltCalculator(null));
    }

    @Test
    public void rejectsInvalidDeadzone() {
        assertThrows(IllegalArgumentException.class,
                () -> new TiltCalculator(config(-1.0, 1.0, 30.0)));
        assertThrows(IllegalArgumentException.class,
                () -> new TiltCalculator(config(Double.NaN, 1.0, 30.0)));
    }

    @Test
    public void rejectsInvalidGain() {
        assertThrows(IllegalArgumentException.class,
                () -> new TiltCalculator(config(5.0, -1.0, 30.0)));
        assertThrows(IllegalArgumentException.class,
                () -> new TiltCalculator(config(5.0, Double.NaN, 30.0)));
    }

    @Test
    public void rejectsInvalidClamp() {
        assertThrows(IllegalArgumentException.class,
                () -> new TiltCalculator(config(5.0, 1.0, 0.0)));
        assertThrows(IllegalArgumentException.class,
                () -> new TiltCalculator(config(5.0, 1.0, Double.NaN)));
    }

    @Test
    public void rejectsDeadzoneGreaterThanClamp() {
        assertThrows(IllegalArgumentException.class,
                () -> new TiltCalculator(config(31.0, 1.0, 30.0)));
    }

    @Test
    public void rejectsNullSample() {
        TiltCalculator calculator = new TiltCalculator(GameConfig.defaults());

        assertThrows(IllegalArgumentException.class, () -> calculator.calculate(null));
    }

    @Test
    public void uprightRestIsZero() {
        TiltCalculator calculator = new TiltCalculator(GameConfig.defaults());

        TiltSample sample = calculator.calculate(accelerometer(0.0, G, 0.0));

        assertEquals(0.0, sample.getRawRollDeg(), 1e-9);
        assertEquals(0.0, sample.getRawPitchDeg(), 1e-9);
        assertEquals(0.0, sample.getRollDeg(), 1e-9);
        assertEquals(0.0, sample.getPitchDeg(), 1e-9);
    }

    @Test
    public void rollTiltUsesPortraitAxis() {
        TiltCalculator calculator = new TiltCalculator(GameConfig.defaults());
        double angleDeg = 20.0;
        double angleRad = Math.toRadians(angleDeg);

        TiltSample sample = calculator.calculate(
                accelerometer(G * Math.sin(angleRad), G * Math.cos(angleRad), 0.0));

        assertEquals(angleDeg, sample.getRawRollDeg(), 1e-9);
        assertEquals(angleDeg, sample.getRollDeg(), 1e-9);
        assertEquals(0.0, sample.getPitchDeg(), 1e-9);
    }

    @Test
    public void pitchTiltUsesPortraitAxis() {
        TiltCalculator calculator = new TiltCalculator(GameConfig.defaults());
        double angleDeg = 20.0;
        double angleRad = Math.toRadians(angleDeg);

        TiltSample sample = calculator.calculate(
                accelerometer(0.0, G * Math.cos(angleRad), -G * Math.sin(angleRad)));

        assertEquals(angleDeg, sample.getRawPitchDeg(), 1e-9);
        assertEquals(angleDeg, sample.getPitchDeg(), 1e-9);
        assertEquals(0.0, sample.getRollDeg(), 1e-9);
    }

    @Test
    public void negativeTiltsKeepTheirSign() {
        TiltCalculator calculator = new TiltCalculator(GameConfig.defaults());
        double angleRad = Math.toRadians(20.0);

        TiltSample sample = calculator.calculate(
                accelerometer(-G * Math.sin(angleRad), G * Math.cos(angleRad),
                        G * Math.sin(angleRad)));

        assertEquals(-20.0, sample.getRollDeg(), 1e-9);
        assertEquals(-20.0, sample.getPitchDeg(), 1e-9);
    }

    @Test
    public void deadzoneZeroesSmallAnglesInclusively() {
        TiltCalculator calculator = new TiltCalculator(GameConfig.defaults());

        assertEquals(0.0, rollAt(calculator, 4.9), 0.0);
        assertEquals(0.0, rollAt(calculator, 5.0), 0.0);
    }

    @Test
    public void deadzoneKeepsAngleBeyondBoundaryWithoutRescaling() {
        TiltCalculator calculator = new TiltCalculator(GameConfig.defaults());

        assertEquals(5.1, rollAt(calculator, 5.1), 1e-9);
        assertEquals(10.0, rollAt(calculator, 10.0), 1e-9);
    }

    @Test
    public void gainScalesAngleOutsideDeadzone() {
        TiltCalculator calculator = new TiltCalculator(config(5.0, 2.0, 30.0));

        assertEquals(20.0, rollAt(calculator, 10.0), 1e-9);
    }

    @Test
    public void clampLimitsBothAxes() {
        TiltCalculator calculator = new TiltCalculator(GameConfig.defaults());

        assertEquals(30.0, rollAt(calculator, 45.0), 1e-9);
        assertEquals(-30.0, rollAt(calculator, -45.0), 1e-9);
        assertEquals(30.0, pitchAt(calculator, 45.0), 1e-9);
        assertEquals(-30.0, pitchAt(calculator, -45.0), 1e-9);
    }

    @Test
    public void gainIsAppliedBeforeClamp() {
        TiltCalculator calculator = new TiltCalculator(config(5.0, 3.0, 30.0));

        assertEquals(30.0, rollAt(calculator, 20.0), 1e-9);
    }

    @Test
    public void zeroGainProducesZeroOutput() {
        TiltCalculator calculator = new TiltCalculator(config(5.0, 0.0, 30.0));

        assertEquals(0.0, rollAt(calculator, 20.0), 0.0);
    }

    @Test
    public void rawValuesArePreservedBeforeFiltering() {
        TiltCalculator calculator = new TiltCalculator(GameConfig.defaults());

        TiltSample sample = calculator.calculate(
                accelerometer(G * Math.sin(Math.toRadians(45.0)),
                        G * Math.cos(Math.toRadians(45.0)), 0.0));

        assertEquals(45.0, sample.getRawRollDeg(), 1e-9);
        assertEquals(30.0, sample.getRollDeg(), 1e-9);
    }

    @Test
    public void zeroGravityVectorProducesZeroTilt() {
        TiltCalculator calculator = new TiltCalculator(GameConfig.defaults());

        TiltSample sample = calculator.calculate(accelerometer(0.0, 0.0, 0.0));

        assertEquals(0.0, sample.getRollDeg(), 0.0);
        assertEquals(0.0, sample.getPitchDeg(), 0.0);
    }

    private static GameConfig config(double deadzoneDeg, double gain, double clampDeg) {
        return new GameConfig.Builder()
                .setTiltDeadzoneDeg(deadzoneDeg)
                .setTiltGain(gain)
                .setTiltClampDeg(clampDeg)
                .build();
    }

    private static AccelerometerSample accelerometer(double x, double y, double z) {
        return new AccelerometerSample(x, y, z, 0L);
    }

    private static double rollAt(TiltCalculator calculator, double angleDeg) {
        double angleRad = Math.toRadians(angleDeg);
        return calculator.calculate(accelerometer(
                G * Math.sin(angleRad), G * Math.cos(angleRad), 0.0)).getRollDeg();
    }

    private static double pitchAt(TiltCalculator calculator, double angleDeg) {
        double angleRad = Math.toRadians(angleDeg);
        return calculator.calculate(accelerometer(
                0.0, G * Math.cos(angleRad), -G * Math.sin(angleRad))).getPitchDeg();
    }
}
