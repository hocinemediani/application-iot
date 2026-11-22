package com.example.ghostradar.domain.sensors;

/**
 * Fiabilité d'une mesure de boussole, indépendante des constantes Android.
 */
public enum CompassAccuracy {

    UNRELIABLE,
    NO_CONTACT,
    LOW,
    MEDIUM,
    HIGH;

    /**
     * Une mesure n'est retenue que si elle est au moins MEDIUM.
     */
    public boolean isReliable() {
        return this == MEDIUM || this == HIGH;
    }
}
