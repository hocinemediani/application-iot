package com.example.ghostradar.domain.ai;

import com.example.ghostradar.domain.ai.CaptureController.CaptureStatus;
import com.example.ghostradar.domain.config.GameConfig;
import com.example.ghostradar.domain.entity.Ghost;
import com.example.ghostradar.domain.entity.GhostState;
import com.example.ghostradar.domain.geo.GeoMath;
import com.example.ghostradar.domain.geo.LatLon;
import com.example.ghostradar.domain.item.BeamProfile;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class CaptureControllerTest {

    private static final double EARTH_RADIUS_M = 6_371_000.0;
    private static final double TICK_S = 0.1;
    private static final double CAPTURE_DISTANCE_M = 4.0;
    private static final double ANGLE_TOLERANCE_DEG = 12.0;
    private static final double REQUIRED_SECONDS = 5.0;
    private static final double TIMEOUT_S = 30.0;
    private static final LatLon PLAYER = new LatLon(0.0, 0.0);

    @Test
    public void idleBeforeStart() {
        CaptureController controller = new CaptureController(GameConfig.defaults());

        assertEquals(CaptureStatus.IDLE, controller.getStatus());
        assertEquals(0.0, controller.getProgressS(), 0.0);
        assertEquals(0.0, controller.getElapsedS(), 0.0);
        assertNull(controller.getAnchoredPosition());
    }

    @Test
    public void startRequiresDistanceStrictlyUnderCaptureDistance() {
        CaptureController controller = new CaptureController(GameConfig.defaults());
        Ghost tooFar = ghostAt(CAPTURE_DISTANCE_M + 0.001);

        assertFalse(controller.start(PLAYER, tooFar));
        assertEquals(CaptureStatus.IDLE, controller.getStatus());
        assertNull(controller.getAnchoredPosition());

        Ghost closeEnough = ghostAt(CAPTURE_DISTANCE_M - 0.001);
        assertTrue(controller.start(PLAYER, closeEnough));
        assertEquals(CaptureStatus.IN_PROGRESS, controller.getStatus());
    }

    @Test
    public void startAnchorsGhostAndInitializesTimers() {
        CaptureController controller = new CaptureController(GameConfig.defaults());
        Ghost ghost = ghostAt(3.0);
        LatLon anchored = ghost.getPosition();

        assertTrue(controller.start(PLAYER, ghost));

        assertEquals(anchored, controller.getAnchoredPosition());
        assertEquals(anchored, ghost.getPosition());
        assertEquals(0.0, controller.getProgressS(), 0.0);
        assertEquals(0.0, controller.getElapsedS(), 0.0);
    }

    @Test
    public void lockedBeamAccumulatesProgress() {
        CaptureController controller = newController();
        Ghost ghost = ghostAt(3.0);
        controller.start(PLAYER, ghost);

        feed(controller, 0.0, true, 1.0);

        assertEquals(CaptureStatus.IN_PROGRESS, controller.getStatus());
        assertEquals(1.0, controller.getProgressS(), 1e-9);
        assertEquals(1.0, controller.getElapsedS(), 1e-9);
    }

    @Test
    public void beamInactivePausesProgress() {
        CaptureController controller = newController();
        controller.start(PLAYER, ghostAt(3.0));

        feed(controller, 0.0, true, 1.0);
        feed(controller, 0.0, false, 1.0);

        assertEquals(1.0, controller.getProgressS(), 1e-9);
        assertEquals(2.0, controller.getElapsedS(), 1e-9);
    }

    @Test
    public void angleToleranceIsInclusive() {
        CaptureController positive = newController();
        CaptureController negative = newController();
        CaptureController outside = newController();
        positive.start(PLAYER, ghostAt(3.0));
        negative.start(PLAYER, ghostAt(3.0));
        outside.start(PLAYER, ghostAt(3.0));

        feed(positive, ANGLE_TOLERANCE_DEG, true, 1.0);
        feed(negative, -ANGLE_TOLERANCE_DEG, true, 1.0);
        feed(outside, ANGLE_TOLERANCE_DEG + 0.0001, true, 1.0);

        assertEquals(1.0, positive.getProgressS(), 1e-9);
        assertEquals(1.0, negative.getProgressS(), 1e-9);
        assertEquals(0.0, outside.getProgressS(), 0.0);
    }

    @Test
    public void progressPausesWithoutDecayWhenLockLost() {
        CaptureController controller = newController();
        controller.start(PLAYER, ghostAt(3.0));

        feed(controller, 0.0, true, 2.0);
        feed(controller, 45.0, true, 10.0);

        assertEquals(CaptureStatus.IN_PROGRESS, controller.getStatus());
        assertEquals(2.0, controller.getProgressS(), 1e-9);
        assertEquals(12.0, controller.getElapsedS(), 1e-9);

        feed(controller, 0.0, true, 3.0);

        assertEquals(CaptureStatus.SUCCESS, controller.getStatus());
        assertEquals(REQUIRED_SECONDS, controller.getProgressS(), 0.0);
    }

    @Test
    public void progressDecaysWhenPauseIsDisabled() {
        GameConfig config = new GameConfig.Builder()
                .setCapturePauseOnLockLost(false)
                .setCaptureProgressDecayPerS(0.5)
                .build();
        CaptureController controller = new CaptureController(config);
        controller.start(PLAYER, ghostAt(3.0));

        feed(controller, 0.0, true, 2.0);
        feed(controller, 45.0, true, 1.0);

        assertEquals(1.5, controller.getProgressS(), 1e-9);
    }

    @Test
    public void successAtRequiredSeconds() {
        CaptureController controller = newController();
        controller.start(PLAYER, ghostAt(3.0));

        feed(controller, 0.0, true, 4.9);
        assertEquals(CaptureStatus.IN_PROGRESS, controller.getStatus());

        feed(controller, 0.0, true, 0.1);

        assertEquals(CaptureStatus.SUCCESS, controller.getStatus());
        assertEquals(REQUIRED_SECONDS, controller.getProgressS(), 0.0);
    }

    @Test
    public void timeoutFailsAfterThirtySeconds() {
        CaptureController controller = newController();
        controller.start(PLAYER, ghostAt(3.0));

        feed(controller, 45.0, true, 29.9);
        assertEquals(CaptureStatus.IN_PROGRESS, controller.getStatus());

        feed(controller, 45.0, true, 0.1);

        assertEquals(CaptureStatus.FAILED, controller.getStatus());
        assertEquals(0.0, controller.getProgressS(), 0.0);
    }

    @Test
    public void successWinsWhenProgressAndTimeoutReachedOnSameTick() {
        GameConfig config = new GameConfig.Builder()
                .setBeamBaseRequiredSeconds(2.0)
                .setCaptureTimeoutS(2.0)
                .build();
        CaptureController controller = new CaptureController(config);
        controller.start(PLAYER, ghostAt(3.0));

        feed(controller, 0.0, true, 2.0);

        assertEquals(CaptureStatus.SUCCESS, controller.getStatus());
    }

    @Test
    public void beamProfileLevelFiveRequiresThreeSeconds() {
        BeamProfile beam = maxedBeam(GameConfig.defaults());
        CaptureController controller = new CaptureController(GameConfig.defaults(), beam);
        assertTrue(controller.start(PLAYER, ghostAt(3.0)));

        feed(controller, 0.0, true, 2.9);
        assertEquals(CaptureStatus.IN_PROGRESS, controller.getStatus());

        feed(controller, 0.0, true, 0.1);
        assertEquals(CaptureStatus.SUCCESS, controller.getStatus());
    }

    @Test
    public void beamProfileLevelFiveUsesSixteenDegreeTolerance() {
        GameConfig config = GameConfig.defaults();
        BeamProfile beam = maxedBeam(config);
        CaptureController controller = new CaptureController(config, beam);
        assertTrue(controller.start(PLAYER, ghostAt(3.0)));

        // 15° dépasserait la tolérance de 12° du niveau 1 mais pas celle de 16°.
        feed(controller, 15.0, true, 3.0);

        assertEquals(CaptureStatus.SUCCESS, controller.getStatus());
    }

    @Test
    public void beamUpgradeAfterStartDoesNotChangeCurrentCapture() {
        GameConfig config = new GameConfig.Builder()
                .setBeamMaxLevel(2)
                .build();
        BeamProfile beam = new BeamProfile(config);
        CaptureController controller = new CaptureController(config, beam);
        assertTrue(controller.start(PLAYER, ghostAt(3.0)));

        assertTrue(beam.upgrade());
        feed(controller, 0.0, true, 4.5);
        assertEquals(CaptureStatus.IN_PROGRESS, controller.getStatus());

        feed(controller, 0.0, true, 0.5);
        assertEquals(CaptureStatus.SUCCESS, controller.getStatus());
    }

    @Test
    public void ghostPositionStaysAnchoredDuringCapture() {
        CaptureController controller = newController();
        Ghost ghost = ghostAt(3.0);
        controller.start(PLAYER, ghost);
        LatLon anchored = ghost.getPosition();

        ghost.setPosition(new LatLon(0.01, 0.01));
        feed(controller, 0.0, true, 0.5);

        assertEquals(anchored, ghost.getPosition());
        assertEquals(anchored, controller.getAnchoredPosition());
    }

    @Test
    public void captureWorksWhileGhostIsStunned() {
        CaptureController controller = newController();
        Ghost ghost = ghostAt(3.0);
        ghost.stun(10.0);
        assertTrue(controller.start(PLAYER, ghost));

        feed(controller, 0.0, true, REQUIRED_SECONDS);

        assertEquals(CaptureStatus.SUCCESS, controller.getStatus());
        assertEquals(GhostState.STUNNED, ghost.getState());
    }

    @Test
    public void updatesAndRestartsAreIgnoredAfterCompletion() {
        CaptureController controller = newController();
        Ghost ghost = ghostAt(3.0);
        controller.start(PLAYER, ghost);
        feed(controller, 0.0, true, REQUIRED_SECONDS);
        assertEquals(CaptureStatus.SUCCESS, controller.getStatus());
        double elapsedAtSuccess = controller.getElapsedS();

        feed(controller, 45.0, true, 10.0);
        assertFalse(controller.start(PLAYER, ghost));

        assertEquals(CaptureStatus.SUCCESS, controller.getStatus());
        assertEquals(elapsedAtSuccess, controller.getElapsedS(), 0.0);
        assertEquals(REQUIRED_SECONDS, controller.getProgressS(), 0.0);
    }

    @Test
    public void rejectsInvalidArguments() {
        CaptureController controller = newController();
        Ghost ghost = ghostAt(3.0);

        assertThrows(IllegalArgumentException.class,
                () -> new CaptureController(null));
        assertThrows(IllegalArgumentException.class,
                () -> new CaptureController(GameConfig.defaults(), null));
        assertThrows(IllegalArgumentException.class,
                () -> controller.start(null, ghost));
        assertThrows(IllegalArgumentException.class,
                () -> controller.start(PLAYER, null));
        assertThrows(IllegalArgumentException.class,
                () -> controller.update(Double.NaN, true, TICK_S));
        assertThrows(IllegalArgumentException.class,
                () -> controller.update(0.0, true, -0.1));
        assertThrows(IllegalArgumentException.class,
                () -> controller.update(0.0, true, Double.NaN));
    }

    @Test
    public void rejectsInvalidConfig() {
        assertThrows(IllegalArgumentException.class,
                () -> new CaptureController(new GameConfig.Builder()
                        .setCaptureDistanceM(0.0)
                        .build()));
        assertThrows(IllegalArgumentException.class,
                () -> new CaptureController(new GameConfig.Builder()
                        .setCaptureRequiredSeconds(0.0)
                        .build()));
        assertThrows(IllegalArgumentException.class,
                () -> new CaptureController(new GameConfig.Builder()
                        .setCaptureAngleToleranceDeg(-1.0)
                        .build()));
        assertThrows(IllegalArgumentException.class,
                () -> new CaptureController(new GameConfig.Builder()
                        .setCaptureTimeoutS(0.0)
                        .build()));
        assertThrows(IllegalArgumentException.class,
                () -> new CaptureController(new GameConfig.Builder()
                        .setCaptureProgressDecayPerS(-0.1)
                        .build()));
    }

    private static BeamProfile maxedBeam(GameConfig config) {
        BeamProfile beam = new BeamProfile(config);
        while (beam.upgrade()) {
            // Monte jusqu'au niveau maximum (5 par défaut).
        }
        return beam;
    }

    private static CaptureController newController() {
        return new CaptureController(GameConfig.defaults());
    }

    private static Ghost ghostAt(double distanceM) {
        return new Ghost(GeoMath.destination(PLAYER, 90.0, distanceM, EARTH_RADIUS_M));
    }

    private static void feed(CaptureController controller, double aimErrorDeg,
                             boolean beamActive, double totalSeconds) {
        int steps = (int) Math.round(totalSeconds / TICK_S);
        for (int step = 0; step < steps; step++) {
            controller.update(aimErrorDeg, beamActive, TICK_S);
        }
    }
}
