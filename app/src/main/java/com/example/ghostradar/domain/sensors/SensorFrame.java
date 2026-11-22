package com.example.ghostradar.domain.sensors;

/**
 * Dernier échantillon connu de chaque capteur, agrégé pour le tick de jeu.
 *
 * <p>Objet immuable. Les champs peuvent être {@code null} tant qu'aucune
 * mesure n'a été reçue ; les méthodes {@code withX} renvoient une nouvelle
 * instance sans modifier l'originale.</p>
 */
public final class SensorFrame {

    public static final SensorFrame EMPTY = new SensorFrame(null, null, null, null, null);

    private final GpsSample gps;
    private final CompassSample compass;
    private final NoiseSample noise;
    private final AccelerometerSample accelerometer;
    private final TiltSample tilt;

    public SensorFrame(GpsSample gps, CompassSample compass, NoiseSample noise,
                       AccelerometerSample accelerometer) {
        this(gps, compass, noise, accelerometer, null);
    }

    public SensorFrame(GpsSample gps, CompassSample compass, NoiseSample noise,
                       AccelerometerSample accelerometer, TiltSample tilt) {
        this.gps = gps;
        this.compass = compass;
        this.noise = noise;
        this.accelerometer = accelerometer;
        this.tilt = tilt;
    }

    public GpsSample getGps() {
        return gps;
    }

    public CompassSample getCompass() {
        return compass;
    }

    public NoiseSample getNoise() {
        return noise;
    }

    public AccelerometerSample getAccelerometer() {
        return accelerometer;
    }

    public TiltSample getTilt() {
        return tilt;
    }

    public boolean hasGps() {
        return gps != null;
    }

    public boolean hasCompass() {
        return compass != null;
    }

    public boolean hasNoise() {
        return noise != null;
    }

    public boolean hasAccelerometer() {
        return accelerometer != null;
    }

    public boolean hasTilt() {
        return tilt != null;
    }

    public boolean isEmpty() {
        return gps == null && compass == null && noise == null
                && accelerometer == null && tilt == null;
    }

    public SensorFrame withGps(GpsSample sample) {
        return new SensorFrame(sample, compass, noise, accelerometer, tilt);
    }

    public SensorFrame withCompass(CompassSample sample) {
        return new SensorFrame(gps, sample, noise, accelerometer, tilt);
    }

    public SensorFrame withNoise(NoiseSample sample) {
        return new SensorFrame(gps, compass, sample, accelerometer, tilt);
    }

    public SensorFrame withAccelerometer(AccelerometerSample sample) {
        return new SensorFrame(gps, compass, noise, sample, tilt);
    }

    public SensorFrame withTilt(TiltSample sample) {
        return new SensorFrame(gps, compass, noise, accelerometer, sample);
    }
}
