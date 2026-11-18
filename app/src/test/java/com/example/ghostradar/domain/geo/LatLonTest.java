package com.example.ghostradar.domain.geo;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class LatLonTest {

    @Test
    public void storesCoordinates() {
        LatLon point = new LatLon(48.8566, 2.3522);

        assertEquals(48.8566, point.getLatitude(), 0.0);
        assertEquals(2.3522, point.getLongitude(), 0.0);
    }

    @Test
    public void equalityIsBasedOnCoordinates() {
        LatLon point = new LatLon(48.8566, 2.3522);

        assertEquals(new LatLon(48.8566, 2.3522), point);
        assertEquals(new LatLon(48.8566, 2.3522).hashCode(), point.hashCode());
        assertNotEquals(new LatLon(48.8566, 2.3523), point);
    }
}
