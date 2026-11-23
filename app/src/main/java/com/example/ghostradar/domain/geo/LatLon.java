package com.example.ghostradar.domain.geo;

import com.example.ghostradar.domain.Validation;

import java.util.Objects;

/**
 * Coordonnée géographique en degrés décimaux.
 *
 * <p>Objet de valeur immuable, sans dépendance Android.</p>
 */
public final class LatLon {

    private final double latitude;
    private final double longitude;

    /**
     * Construit un point géographique valide.
     *
     * @throws IllegalArgumentException si une coordonnée est NaN, infinie ou
     *         hors de {@code [-90, 90]} / {@code [-180, 180]}
     */
    public LatLon(double latitude, double longitude) {
        this.latitude = Validation.requireLatitude(latitude);
        this.longitude = Validation.requireLongitude(longitude);
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof LatLon)) {
            return false;
        }
        LatLon that = (LatLon) other;
        return Double.compare(that.latitude, latitude) == 0
                && Double.compare(that.longitude, longitude) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude);
    }

    @Override
    public String toString() {
        return "LatLon{latitude=" + latitude + ", longitude=" + longitude + '}';
    }
}
