package com.example.ghostradar.domain.entity;

import com.example.ghostradar.domain.geo.LatLon;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class GhostTest {

    @Test
    public void rejectsNonFiniteStunDurationAndDelta() {
        Ghost ghost = new Ghost(new LatLon(0.0, 0.0));

        assertThrows(IllegalArgumentException.class, () -> ghost.stun(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> ghost.stun(Double.POSITIVE_INFINITY));

        ghost.stun(5.0);
        assertThrows(IllegalArgumentException.class, () -> ghost.advanceStun(Double.NaN));
        assertThrows(IllegalArgumentException.class,
                () -> ghost.advanceStun(Double.POSITIVE_INFINITY));
        assertEquals(5.0, ghost.getStunRemainingS(), 0.0);
    }
}
