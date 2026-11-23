package com.example.ghostradar.domain.geo;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThrows;

public class LatLonTest {

    @Test
    public void storesCoordinates() {
        LatLon point = new LatLon(48.8566, 2.3522);

        assertEquals(48.8566, point.getLatitude(), 0.0);
        assertEquals(2.3522, point.getLongitude(), 0.0);
    }

    @Test
    public void acceptsBoundaryCoordinates() {
        LatLon northEast = new LatLon(90.0, 180.0);
        LatLon southWest = new LatLon(-90.0, -180.0);

        assertEquals(90.0, northEast.getLatitude(), 0.0);
        assertEquals(180.0, northEast.getLongitude(), 0.0);
        assertEquals(-90.0, southWest.getLatitude(), 0.0);
        assertEquals(-180.0, southWest.getLongitude(), 0.0);
    }

    @Test
    public void rejectsNonFiniteAndOutOfRangeCoordinates() {
        assertThrows(IllegalArgumentException.class, () -> new LatLon(Double.NaN, 2.0));
        assertThrows(IllegalArgumentException.class, () -> new LatLon(48.0, Double.NaN));
        assertThrows(IllegalArgumentException.class,
                () -> new LatLon(Double.POSITIVE_INFINITY, 2.0));
        assertThrows(IllegalArgumentException.class,
                () -> new LatLon(48.0, Double.NEGATIVE_INFINITY));
        assertThrows(IllegalArgumentException.class, () -> new LatLon(90.1, 2.0));
        assertThrows(IllegalArgumentException.class, () -> new LatLon(-90.1, 2.0));
        assertThrows(IllegalArgumentException.class, () -> new LatLon(48.0, 180.1));
        assertThrows(IllegalArgumentException.class, () -> new LatLon(48.0, -180.1));
    }

    @Test
    public void equalityIsBasedOnCoordinates() {
        LatLon point = new LatLon(48.8566, 2.3522);

        assertEquals(new LatLon(48.8566, 2.3522), point);
        assertEquals(new LatLon(48.8566, 2.3522).hashCode(), point.hashCode());
        assertNotEquals(new LatLon(48.8566, 2.3523), point);
    }
}
