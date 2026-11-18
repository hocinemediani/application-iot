package com.example.ghostradar.domain.geo;

import java.util.Objects;

/**
 * Coordonnée géographique en degrés décimaux.
 *
 * <p>Objet de valeur immuable, sans dépendance Android.</p>
 */
public final class LatLon {

    private final double latitude;
    private final double longitude;

    public LatLon(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
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
