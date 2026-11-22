package com.example.ghostradar.domain.sensors;

/**
 * Calcul du RMS normalisé d'un bloc PCM 16 bits mono.
 *
 * <p>Le résultat est dans {@code [0, 1]} : {@code 1} correspond à la pleine
 * échelle négative ({@code -32768}).</p>
 */
public final class RmsMeter {

    private static final double PCM16_FULL_SCALE = 32_768.0;

    private RmsMeter() {
    }

    public static double computeRmsNormalized(short[] pcm) {
        if (pcm == null) {
            throw new IllegalArgumentException("pcm must not be null");
        }
        return computeRmsNormalized(pcm, 0, pcm.length);
    }

    public static double computeRmsNormalized(short[] pcm, int offset, int length) {
        if (pcm == null) {
            throw new IllegalArgumentException("pcm must not be null");
        }
        if (offset < 0 || length <= 0 || offset > pcm.length - length) {
            throw new IllegalArgumentException("invalid PCM range");
        }

        double sumOfSquares = 0.0;
        for (int i = offset; i < offset + length; i++) {
            double normalized = pcm[i] / PCM16_FULL_SCALE;
            sumOfSquares += normalized * normalized;
        }
        double rms = Math.sqrt(sumOfSquares / length);
        return Math.min(1.0, rms);
    }
}
