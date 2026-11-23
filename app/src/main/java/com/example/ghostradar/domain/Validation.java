package com.example.ghostradar.domain;

/**
 * Helpers de validation partagés par le domaine (Java pur).
 *
 * <p>Toutes les méthodes lèvent {@link IllegalArgumentException} avec un message
 * nommant le champ invalide, afin que les erreurs de configuration ou d'entrée
 * soient localisables dès leur source. La politique V1 est la même partout :
 * les valeurs non finies (NaN, infinis) sont rejetées, et les durées/plages
 * sont contrôlées explicitement.</p>
 */
public final class Validation {

    private Validation() {
    }

    /** Exige une valeur finie (ni NaN, ni infini). */
    public static double requireFinite(String name, double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            throw new IllegalArgumentException(name + " must be finite, got " + value);
        }
        return value;
    }

    /** Exige une valeur finie strictement positive. */
    public static double requirePositiveFinite(String name, double value) {
        requireFinite(name, value);
        if (value <= 0.0) {
            throw new IllegalArgumentException(name + " must be positive, got " + value);
        }
        return value;
    }

    /** Exige une valeur finie positive ou nulle. */
    public static double requireNonNegativeFinite(String name, double value) {
        requireFinite(name, value);
        if (value < 0.0) {
            throw new IllegalArgumentException(name + " must not be negative, got " + value);
        }
        return value;
    }

    /** Exige une valeur finie comprise dans {@code [min, max]} et {@code min <= max}. */
    public static double requireRange(String name, double value, double min, double max) {
        requireFinite(name + " range minimum", min);
        requireFinite(name + " range maximum", max);
        requireFinite(name, value);
        if (min > max) {
            throw new IllegalArgumentException(name + " range is inverted: " + min + " > " + max);
        }
        if (value < min || value > max) {
            throw new IllegalArgumentException(name + " must be in [" + min + ", " + max + "], got " + value);
        }
        return value;
    }

    /**
     * Exige que deux sources d'une même valeur soient strictement égales.
     *
     * <p>Pas de tolérance : toutes les valeurs du domaine proviennent de la
     * même constante, donc toute divergence est une erreur de configuration.</p>
     */
    public static double requireSameValue(String name, double expected, double actual) {
        requireFinite(name + " expected", expected);
        requireFinite(name + " actual", actual);
        if (Double.compare(expected, actual) != 0) {
            throw new IllegalArgumentException(name + " sources diverge: "
                    + expected + " != " + actual);
        }
        return actual;
    }

    /** Cas particulier de {@link #requireSameValue} pour le rayon terrestre. */
    public static double requireSameRadius(String name, double expected, double actual) {
        return requireSameValue(name, expected, actual);
    }

    /** Exige un entier strictement positif. */
    public static int requirePositiveInt(String name, int value) {
        if (value <= 0) {
            throw new IllegalArgumentException(name + " must be positive, got " + value);
        }
        return value;
    }

    /** Exige un entier positif ou nul. */
    public static int requireNonNegativeInt(String name, int value) {
        if (value < 0) {
            throw new IllegalArgumentException(name + " must not be negative, got " + value);
        }
        return value;
    }

    /** Exige une référence non nulle. */
    public static <T> T requireNonNull(String name, T value) {
        if (value == null) {
            throw new IllegalArgumentException(name + " must not be null");
        }
        return value;
    }

    /** Exige un {@code deltaSeconds} fini et positif ou nul. */
    public static double requireDeltaSeconds(double deltaSeconds) {
        return requireNonNegativeFinite("deltaSeconds", deltaSeconds);
    }

    /** Exige une latitude valide, c'est-à-dire finie et dans {@code [-90, 90]}. */
    public static double requireLatitude(double latitude) {
        return requireRange("latitude", latitude, -90.0, 90.0);
    }

    /** Exige une longitude valide, c'est-à-dire finie et dans {@code [-180, 180]}. */
    public static double requireLongitude(double longitude) {
        return requireRange("longitude", longitude, -180.0, 180.0);
    }
}
