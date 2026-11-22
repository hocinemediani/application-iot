package com.example.ghostradar.domain.sensors;

/**
 * Mesure brute de l'accéléromètre en m/s², dans le repère de l'appareil.
 */
public final class AccelerometerSample {

    private final double x;
    private final double y;
    private final double z;
    private final long timestampNanos;

    public AccelerometerSample(double x, double y, double z, long timestampNanos) {
        if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)) {
            throw new IllegalArgumentException("accelerometer axes must be finite");
        }
        if (timestampNanos < 0L) {
            throw new IllegalArgumentException("timestampNanos must not be negative");
        }
        this.x = x;
        this.y = y;
        this.z = z;
        this.timestampNanos = timestampNanos;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public long getTimestampNanos() {
        return timestampNanos;
    }
}
