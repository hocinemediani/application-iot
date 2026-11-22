package com.example.ghostradar.ui;

/**
 * Calcul pure du filtre bleu/infrarouge superposé à la caméra.
 *
 * <p>Aucune dépendance Android : le résultat est un entier ARGB 32 bits
 * compatible avec {@code View.setBackgroundColor(int)}, mais la conversion
 * reste testable avec JUnit.</p>
 */
public final class CameraFilterStyle {

    /** Teinte bleu nuit retenue pour le rendu infrarouge (sans alpha). */
    public static final int BLUE_IR_RGB = 0x003366;

    private static final int RGB_MASK = 0x00FFFFFF;
    private static final int ALPHA_SCALE = 255;

    private CameraFilterStyle() {
    }

    /**
     * Combine une couleur RGB 24 bits et une opacité {@code [0, 1]} en ARGB.
     *
     * @param rgb   couleur au format {@code 0xRRGGBB}
     * @param alpha opacité finie dans {@code [0, 1]}
     * @return couleur ARGB prête à être appliquée à l'overlay
     * @throws IllegalArgumentException si les entrées sont hors plage
     */
    public static int argb(int rgb, double alpha) {
        if (Double.isNaN(alpha) || Double.isInfinite(alpha)
                || alpha < 0.0 || alpha > 1.0) {
            throw new IllegalArgumentException("alpha must be finite and in [0, 1]: " + alpha);
        }
        if ((rgb & ~RGB_MASK) != 0) {
            throw new IllegalArgumentException("rgb must be a 24-bit value: " + rgb);
        }
        int alphaByte = (int) Math.round(alpha * ALPHA_SCALE);
        return (alphaByte << 24) | (rgb & RGB_MASK);
    }
}
