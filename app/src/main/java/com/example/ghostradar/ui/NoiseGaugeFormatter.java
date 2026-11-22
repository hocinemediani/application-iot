package com.example.ghostradar.ui;

/**
 * Conversion pure du niveau de bruit en segments et couleurs de jauge.
 */
public final class NoiseGaugeFormatter {

    private NoiseGaugeFormatter() {
    }

    /**
     * @param level niveau normalisé dans {@code [0, 1]}, borné si nécessaire
     * @return nombre de segments allumés, arrondi au plus proche
     */
    public static int segmentCount(double level) {
        if (!Double.isFinite(level)) {
            throw new IllegalArgumentException("level must be finite: " + level);
        }
        double clamped = Math.min(1.0, Math.max(0.0, level));
        return (int) Math.round(clamped * NoiseGaugeStyle.SEGMENT_COUNT);
    }

    /**
     * Couleur d'un segment allumé selon le niveau dBFS lissé.
     *
     * @param dbfs      niveau courant en dBFS
     * @param threshold seuil d'alerte configuré, par exemple -30 dBFS
     */
    public static int colorForDbfs(double dbfs, double threshold) {
        if (!Double.isFinite(dbfs)) {
            throw new IllegalArgumentException("dbfs must be finite: " + dbfs);
        }
        if (!Double.isFinite(threshold)) {
            throw new IllegalArgumentException("threshold must be finite: " + threshold);
        }
        if (dbfs < NoiseGaugeStyle.YELLOW_MAX_DBFS) {
            return NoiseGaugeStyle.YELLOW_ARGB;
        }
        if (dbfs <= threshold) {
            return NoiseGaugeStyle.ORANGE_ARGB;
        }
        return NoiseGaugeStyle.RED_ARGB;
    }

    /**
     * Couleur du segment {@code segmentIndex} (0 = bas de la jauge).
     */
    public static int colorForSegment(int segmentIndex, int filledCount,
                                      double dbfs, double threshold) {
        if (segmentIndex < 0 || segmentIndex >= NoiseGaugeStyle.SEGMENT_COUNT) {
            throw new IllegalArgumentException(
                    "segment index out of bounds: " + segmentIndex);
        }
        if (segmentIndex < filledCount) {
            return colorForDbfs(dbfs, threshold);
        }
        return NoiseGaugeStyle.EMPTY_ARGB;
    }
}
