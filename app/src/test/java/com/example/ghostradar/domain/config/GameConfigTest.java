package com.example.ghostradar.domain.config;

import com.example.ghostradar.domain.game.GameState;

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class GameConfigTest {

    @Test
    public void defaultsMatchSpecForGeometryAndSensors() {
        GameConfig config = GameConfig.defaults();

        assertEquals(6_371_000.0, config.getEarthRadiusM(), 0.0);
        assertEquals(3, config.getMinPolygonVertices());
        assertEquals(0.5, config.getMinVertexDistanceM(), 0.0);
        assertEquals(20.0, config.getGpsAccuracyMaxM(), 0.0);
        assertEquals(1_000, config.getGpsUpdateIntervalMs());
        assertEquals(1.0, config.getGpsMinDistanceM(), 0.0);
        assertFalse(config.isGpsSmoothingEnabled());
        assertEquals(3.0, config.getCartographySampleDistanceM(), 0.0);
        assertEquals(2_000, config.getCartographySampleIntervalMs());
        assertEquals(1.0, config.getDouglasPeuckerToleranceM(), 0.0);
        assertTrue(config.isCartographyUndoEnabled());
        assertEquals(GameConfig.MapProjectionType.LOCAL_ENU, config.getMapProjection());

        assertEquals(0.15, config.getCameraBlueFilterAlpha(), 0.0);
        assertEquals(500, config.getCameraGlitchDurationMs());
        assertEquals(0.15, config.getCompassLowPassAlpha(), 0.0);
        assertTrue(config.isCompassFallbackEnabled());
        assertTrue(config.isCompassIgnoreUnreliable());
        assertEquals(200, config.getNoiseWindowMs());
        assertEquals(0.2, config.getNoiseSmoothingAlpha(), 0.0);
        assertEquals(-30.0, config.getNoiseThresholdDbfs(), 0.0);
        assertEquals(2.0, config.getNoiseHysteresisS(), 0.0);
        assertEquals(3.0, config.getNoiseAlertHoldS(), 0.0);
        assertEquals(-60.0, config.getNoiseGaugeMinDbfs(), 0.0);
        assertEquals(0.0, config.getNoiseGaugeMaxDbfs(), 0.0);
        assertEquals(5.0, config.getTiltDeadzoneDeg(), 0.0);
        assertEquals(1.0, config.getTiltGain(), 0.0);
        assertEquals(30.0, config.getTiltClampDeg(), 0.0);
        assertEquals(50.0, config.getAccelSamplingHz(), 0.0);
    }

    @Test
    public void defaultsMatchSpecForGameplay() {
        GameConfig config = GameConfig.defaults();

        assertEquals(100, config.getTickMs());
        assertEquals(0.02, config.getZoneTriggerProbabilityPerS(), 0.0);
        assertEquals(30.0, config.getZoneTriggerCooldownS(), 0.0);
        assertEquals(5, config.getZonePolygonMinVertices());
        assertEquals(8, config.getZonePolygonMaxVertices());
        assertEquals(10.0, config.getZoneRadiusMinM(), 0.0);
        assertEquals(30.0, config.getZoneRadiusMaxM(), 0.0);
        assertEquals(25.0, config.getAdminZoneDefaultRadiusM(), 0.0);
        assertEquals(15.0, config.getGhostSpawnMinDistanceM(), 0.0);
        assertEquals(40.0, config.getGhostSpawnMaxDistanceM(), 0.0);
        assertEquals(1.2, config.getGhostBaseSpeedMps(), 0.0);
        assertEquals(2.0, config.getGhostFleeMultiplier(), 0.0);
        assertEquals(3.0, config.getMineTriggerRadiusM(), 0.0);
        assertEquals(10.0, config.getMineStunDurationS(), 0.0);
        assertEquals(3, config.getMineMaxActive());
        assertEquals(5.0, config.getMineMinDistanceM(), 0.0);
        assertEquals(8.0, config.getLureDurationS(), 0.0);
        assertEquals(1.5, config.getLureSpeedMps(), 0.0);
        assertEquals(4.0, config.getCaptureDistanceM(), 0.0);
        assertEquals(5.0, config.getCaptureRequiredSeconds(), 0.0);
        assertEquals(12.0, config.getCaptureAngleToleranceDeg(), 0.0);
        assertEquals(30.0, config.getCaptureTimeoutS(), 0.0);
        assertEquals(0.0, config.getCaptureProgressDecayPerS(), 0.0);
        assertEquals(0.5, config.getCaptureForceChangeMinS(), 0.0);
        assertEquals(1.5, config.getCaptureForceChangeMaxS(), 0.0);
        assertEquals(20.0, config.getCaptureForceAmplitudeDeg(), 0.0);
        assertEquals(1.0, config.getScreamerDelayMinS(), 0.0);
        assertEquals(2.0, config.getScreamerDelayMaxS(), 0.0);
        assertEquals(2.0, config.getScreamerDurationS(), 0.0);
        assertTrue(config.isScreamerEnabled());
        assertFalse(config.isScreamerSkippable());
        assertTrue(config.isPlayerOutOfAreaPause());
    }

    @Test
    public void defaultsMatchSpecForProgression() {
        GameConfig config = GameConfig.defaults();

        assertEquals(1, config.getStartingMineCount());
        assertEquals(1, config.getStartingLureCount());
        assertEquals(1, config.getLootboxPerCapture());
        assertEquals(45.0, config.getLootMineWeight(), 0.0);
        assertEquals(45.0, config.getLootLureWeight(), 0.0);
        assertEquals(10.0, config.getLootBeamWeight(), 0.0);
        assertTrue(config.isPityEnabled());
        assertEquals(10, config.getPityMissesBeforeGuarantee());
        assertEquals(GameConfig.PityGuaranteeType.BEAM_UPGRADE, config.getPityGuaranteeType());
        assertEquals(5, config.getBeamMaxLevel());
        assertEquals(5.0, config.getBeamBaseRequiredSeconds(), 0.0);
        assertEquals(0.5, config.getBeamTimeReductionPerLevelS(), 0.0);
        assertEquals(2.0, config.getBeamMinRequiredSeconds(), 0.0);
        assertEquals(12.0, config.getBeamBaseAngleToleranceDeg(), 0.0);
        assertEquals(2.0, config.getBeamAngleToleranceStepDeg(), 0.0);
        assertEquals(2, config.getBeamLevelsPerToleranceStep());
        assertEquals(10, config.getIdentityCount());
        assertEquals(1.0, config.getIdentificationFreqMinHz(), 0.0);
        assertEquals(10.0, config.getIdentificationFreqMaxHz(), 0.0);
        assertEquals(0.5, config.getIdentificationToleranceHz(), 0.0);
        assertEquals(3, config.getIdentificationAttempts());
        assertTrue(config.isIdentificationRetryAllowed());
        assertFalse(config.isIdentityHasGameplayEffect());
    }

    @Test
    public void defaultsMatchSpecForV2AndInterface() {
        GameConfig config = GameConfig.defaults();

        assertEquals(0.36, config.getPressurePerFloorHpa(), 0.0);
        assertEquals(0.1, config.getFloorHysteresisHpa(), 0.0);
        assertEquals(5.0, config.getBarometerCalibrationS(), 0.0);
        assertEquals(0, config.getFloorGround());
        assertEquals(0, config.getFloorMin());

        assertEquals(100.0, config.getRadarRangeM(), 0.0);
        assertEquals(GameConfig.RadarOrientation.PHONE_UP, config.getRadarOrientation());
        assertTrue(config.isRadarShowDistance());
        assertEquals(EnumSet.of(GameState.TRACKING), config.getRadarVisibleStates());
        assertTrue(config.isAdminSpawnAtMapPoint());
        assertTrue(config.isAdminIgnoreZonesAndProbability());
        assertTrue(config.isAdminDebugOnly());
        assertTrue(config.isCameraGlitchOnSpawn());
        assertTrue(config.isCameraGlitchOnCapture());
        assertTrue(config.isScreamerVibrationEnabled());
        assertTrue(config.isCapturePauseOnLockLost());
        assertTrue(config.isCaptureGhostPositionLocked());
        assertFalse(config.isNoiseAffectsCapture());
        assertFalse(config.isGadgetsUsableDuringCapture());
        assertTrue(config.isMineOneShot());
        assertTrue(config.isMinePersistUntilTriggered());
        assertTrue(config.isMineVisibleOnMap());
        assertTrue(config.isMineAlertOnStun());
        assertTrue(config.isLurePriorityOverFlee());
        assertTrue(config.isLureOneShot());
        assertFalse(config.isLureAffectsStunned());
        assertEquals(GameConfig.UiLanguage.FRENCH, config.getUiLanguage());
        assertEquals(GameConfig.ScreenOrientation.PORTRAIT, config.getOrientation());
        assertTrue(config.isPauseOnBackground());
        assertEquals(GameConfig.SoundSource.SYSTEM_DEFAULT, config.getSoundSource());
        assertTrue(config.isVibrationEnabled());
        assertTrue(config.isAssetsOptional());
    }

    @Test
    public void builderOverridesGeometryAndSensors() {
        GameConfig.Builder builder = new GameConfig.Builder();
        builder.setEarthRadiusM(6_400_000.0);
        builder.setMinPolygonVertices(4);
        builder.setMinVertexDistanceM(0.75);
        builder.setGpsAccuracyMaxM(15.0);
        builder.setGpsUpdateIntervalMs(500);
        builder.setGpsMinDistanceM(2.0);
        builder.setGpsSmoothingEnabled(true);
        builder.setCartographySampleDistanceM(4.0);
        builder.setCartographySampleIntervalMs(1_500);
        builder.setDouglasPeuckerToleranceM(1.5);
        builder.setCartographyUndoEnabled(false);
        builder.setMapProjection(GameConfig.MapProjectionType.LOCAL_ENU);

        builder.setCameraBlueFilterAlpha(0.25);
        builder.setCameraGlitchDurationMs(600);
        builder.setCompassLowPassAlpha(0.25);
        builder.setCompassFallbackEnabled(false);
        builder.setCompassIgnoreUnreliable(false);
        builder.setNoiseWindowMs(250);
        builder.setNoiseSmoothingAlpha(0.3);
        builder.setNoiseThresholdDbfs(-25.0);
        builder.setNoiseHysteresisS(2.5);
        builder.setNoiseAlertHoldS(3.5);
        builder.setNoiseGaugeMinDbfs(-55.0);
        builder.setNoiseGaugeMaxDbfs(-5.0);
        builder.setTiltDeadzoneDeg(6.0);
        builder.setTiltGain(1.2);
        builder.setTiltClampDeg(35.0);
        builder.setAccelSamplingHz(60.0);

        GameConfig config = builder.build();

        assertEquals(6_400_000.0, config.getEarthRadiusM(), 0.0);
        assertEquals(4, config.getMinPolygonVertices());
        assertEquals(0.75, config.getMinVertexDistanceM(), 0.0);
        assertEquals(15.0, config.getGpsAccuracyMaxM(), 0.0);
        assertEquals(500, config.getGpsUpdateIntervalMs());
        assertEquals(2.0, config.getGpsMinDistanceM(), 0.0);
        assertTrue(config.isGpsSmoothingEnabled());
        assertEquals(4.0, config.getCartographySampleDistanceM(), 0.0);
        assertEquals(1_500, config.getCartographySampleIntervalMs());
        assertEquals(1.5, config.getDouglasPeuckerToleranceM(), 0.0);
        assertFalse(config.isCartographyUndoEnabled());
        assertEquals(GameConfig.MapProjectionType.LOCAL_ENU, config.getMapProjection());

        assertEquals(0.25, config.getCameraBlueFilterAlpha(), 0.0);
        assertEquals(600, config.getCameraGlitchDurationMs());
        assertEquals(0.25, config.getCompassLowPassAlpha(), 0.0);
        assertFalse(config.isCompassFallbackEnabled());
        assertFalse(config.isCompassIgnoreUnreliable());
        assertEquals(250, config.getNoiseWindowMs());
        assertEquals(0.3, config.getNoiseSmoothingAlpha(), 0.0);
        assertEquals(-25.0, config.getNoiseThresholdDbfs(), 0.0);
        assertEquals(2.5, config.getNoiseHysteresisS(), 0.0);
        assertEquals(3.5, config.getNoiseAlertHoldS(), 0.0);
        assertEquals(-55.0, config.getNoiseGaugeMinDbfs(), 0.0);
        assertEquals(-5.0, config.getNoiseGaugeMaxDbfs(), 0.0);
        assertEquals(6.0, config.getTiltDeadzoneDeg(), 0.0);
        assertEquals(1.2, config.getTiltGain(), 0.0);
        assertEquals(35.0, config.getTiltClampDeg(), 0.0);
        assertEquals(60.0, config.getAccelSamplingHz(), 0.0);
    }

    @Test
    public void builderOverridesGameplay() {
        GameConfig.Builder builder = new GameConfig.Builder();
        builder.setTickMs(200);
        builder.setZoneTriggerProbabilityPerS(0.05);
        builder.setZoneTriggerCooldownS(45.0);
        builder.setZonePolygonMinVertices(6);
        builder.setZonePolygonMaxVertices(9);
        builder.setZoneRadiusMinM(12.0);
        builder.setZoneRadiusMaxM(35.0);
        builder.setAdminZoneDefaultRadiusM(30.0);
        builder.setGhostSpawnMinDistanceM(18.0);
        builder.setGhostSpawnMaxDistanceM(45.0);
        builder.setGhostBaseSpeedMps(1.5);
        builder.setGhostFleeMultiplier(2.5);
        builder.setMineTriggerRadiusM(4.0);
        builder.setMineStunDurationS(12.0);
        builder.setMineMaxActive(4);
        builder.setMineMinDistanceM(6.0);
        builder.setLureDurationS(10.0);
        builder.setLureSpeedMps(2.0);
        builder.setCaptureDistanceM(5.0);
        builder.setCaptureRequiredSeconds(6.0);
        builder.setCaptureAngleToleranceDeg(15.0);
        builder.setCaptureTimeoutS(35.0);
        builder.setCaptureProgressDecayPerS(0.1);
        builder.setCaptureForceChangeMinS(0.75);
        builder.setCaptureForceChangeMaxS(2.0);
        builder.setCaptureForceAmplitudeDeg(25.0);
        builder.setScreamerDelayMinS(1.5);
        builder.setScreamerDelayMaxS(2.5);
        builder.setScreamerDurationS(3.0);
        builder.setScreamerEnabled(false);
        builder.setScreamerSkippable(true);
        builder.setPlayerOutOfAreaPause(false);

        GameConfig config = builder.build();

        assertEquals(200, config.getTickMs());
        assertEquals(0.05, config.getZoneTriggerProbabilityPerS(), 0.0);
        assertEquals(45.0, config.getZoneTriggerCooldownS(), 0.0);
        assertEquals(6, config.getZonePolygonMinVertices());
        assertEquals(9, config.getZonePolygonMaxVertices());
        assertEquals(12.0, config.getZoneRadiusMinM(), 0.0);
        assertEquals(35.0, config.getZoneRadiusMaxM(), 0.0);
        assertEquals(30.0, config.getAdminZoneDefaultRadiusM(), 0.0);
        assertEquals(18.0, config.getGhostSpawnMinDistanceM(), 0.0);
        assertEquals(45.0, config.getGhostSpawnMaxDistanceM(), 0.0);
        assertEquals(1.5, config.getGhostBaseSpeedMps(), 0.0);
        assertEquals(2.5, config.getGhostFleeMultiplier(), 0.0);
        assertEquals(4.0, config.getMineTriggerRadiusM(), 0.0);
        assertEquals(12.0, config.getMineStunDurationS(), 0.0);
        assertEquals(4, config.getMineMaxActive());
        assertEquals(6.0, config.getMineMinDistanceM(), 0.0);
        assertEquals(10.0, config.getLureDurationS(), 0.0);
        assertEquals(2.0, config.getLureSpeedMps(), 0.0);
        assertEquals(5.0, config.getCaptureDistanceM(), 0.0);
        assertEquals(6.0, config.getCaptureRequiredSeconds(), 0.0);
        assertEquals(15.0, config.getCaptureAngleToleranceDeg(), 0.0);
        assertEquals(35.0, config.getCaptureTimeoutS(), 0.0);
        assertEquals(0.1, config.getCaptureProgressDecayPerS(), 0.0);
        assertEquals(0.75, config.getCaptureForceChangeMinS(), 0.0);
        assertEquals(2.0, config.getCaptureForceChangeMaxS(), 0.0);
        assertEquals(25.0, config.getCaptureForceAmplitudeDeg(), 0.0);
        assertEquals(1.5, config.getScreamerDelayMinS(), 0.0);
        assertEquals(2.5, config.getScreamerDelayMaxS(), 0.0);
        assertEquals(3.0, config.getScreamerDurationS(), 0.0);
        assertFalse(config.isScreamerEnabled());
        assertTrue(config.isScreamerSkippable());
        assertFalse(config.isPlayerOutOfAreaPause());
    }

    @Test
    public void builderOverridesProgression() {
        GameConfig.Builder builder = new GameConfig.Builder();
        builder.setStartingMineCount(2);
        builder.setStartingLureCount(3);
        builder.setLootboxPerCapture(2);
        builder.setLootMineWeight(40.0);
        builder.setLootLureWeight(40.0);
        builder.setLootBeamWeight(20.0);
        builder.setPityEnabled(false);
        builder.setPityMissesBeforeGuarantee(12);
        builder.setPityGuaranteeType(GameConfig.PityGuaranteeType.BEAM_UPGRADE);
        builder.setBeamMaxLevel(6);
        builder.setBeamBaseRequiredSeconds(6.0);
        builder.setBeamTimeReductionPerLevelS(0.75);
        builder.setBeamMinRequiredSeconds(2.5);
        builder.setBeamBaseAngleToleranceDeg(14.0);
        builder.setBeamAngleToleranceStepDeg(3.0);
        builder.setBeamLevelsPerToleranceStep(1);
        builder.setIdentityCount(12);
        builder.setIdentificationFreqMinHz(2.0);
        builder.setIdentificationFreqMaxHz(12.0);
        builder.setIdentificationToleranceHz(0.75);
        builder.setIdentificationAttempts(4);
        builder.setIdentificationRetryAllowed(false);
        builder.setIdentityHasGameplayEffect(true);

        GameConfig config = builder.build();

        assertEquals(2, config.getStartingMineCount());
        assertEquals(3, config.getStartingLureCount());
        assertEquals(2, config.getLootboxPerCapture());
        assertEquals(40.0, config.getLootMineWeight(), 0.0);
        assertEquals(40.0, config.getLootLureWeight(), 0.0);
        assertEquals(20.0, config.getLootBeamWeight(), 0.0);
        assertFalse(config.isPityEnabled());
        assertEquals(12, config.getPityMissesBeforeGuarantee());
        assertEquals(GameConfig.PityGuaranteeType.BEAM_UPGRADE, config.getPityGuaranteeType());
        assertEquals(6, config.getBeamMaxLevel());
        assertEquals(6.0, config.getBeamBaseRequiredSeconds(), 0.0);
        assertEquals(0.75, config.getBeamTimeReductionPerLevelS(), 0.0);
        assertEquals(2.5, config.getBeamMinRequiredSeconds(), 0.0);
        assertEquals(14.0, config.getBeamBaseAngleToleranceDeg(), 0.0);
        assertEquals(3.0, config.getBeamAngleToleranceStepDeg(), 0.0);
        assertEquals(1, config.getBeamLevelsPerToleranceStep());
        assertEquals(12, config.getIdentityCount());
        assertEquals(2.0, config.getIdentificationFreqMinHz(), 0.0);
        assertEquals(12.0, config.getIdentificationFreqMaxHz(), 0.0);
        assertEquals(0.75, config.getIdentificationToleranceHz(), 0.0);
        assertEquals(4, config.getIdentificationAttempts());
        assertFalse(config.isIdentificationRetryAllowed());
        assertTrue(config.isIdentityHasGameplayEffect());
    }

    @Test
    public void builderOverridesV2AndInterface() {
        GameConfig.Builder builder = new GameConfig.Builder();
        builder.setPressurePerFloorHpa(0.4);
        builder.setFloorHysteresisHpa(0.15);
        builder.setBarometerCalibrationS(6.0);
        builder.setFloorGround(2);
        builder.setFloorMin(1);

        builder.setRadarRangeM(150.0);
        builder.setRadarOrientation(GameConfig.RadarOrientation.PHONE_UP);
        builder.setRadarShowDistance(false);
        builder.setRadarVisibleStates(EnumSet.of(GameState.TRACKING, GameState.CAPTURE));
        builder.setAdminSpawnAtMapPoint(false);
        builder.setAdminIgnoreZonesAndProbability(false);
        builder.setAdminDebugOnly(false);
        builder.setCameraGlitchOnSpawn(false);
        builder.setCameraGlitchOnCapture(false);
        builder.setScreamerVibrationEnabled(false);
        builder.setCapturePauseOnLockLost(false);
        builder.setCaptureGhostPositionLocked(false);
        builder.setNoiseAffectsCapture(true);
        builder.setGadgetsUsableDuringCapture(true);
        builder.setMineOneShot(false);
        builder.setMinePersistUntilTriggered(false);
        builder.setMineVisibleOnMap(false);
        builder.setMineAlertOnStun(false);
        builder.setLurePriorityOverFlee(false);
        builder.setLureOneShot(false);
        builder.setLureAffectsStunned(true);
        builder.setUiLanguage(GameConfig.UiLanguage.FRENCH);
        builder.setOrientation(GameConfig.ScreenOrientation.PORTRAIT);
        builder.setPauseOnBackground(false);
        builder.setSoundSource(GameConfig.SoundSource.SYSTEM_DEFAULT);
        builder.setVibrationEnabled(false);
        builder.setAssetsOptional(false);

        GameConfig config = builder.build();

        assertEquals(0.4, config.getPressurePerFloorHpa(), 0.0);
        assertEquals(0.15, config.getFloorHysteresisHpa(), 0.0);
        assertEquals(6.0, config.getBarometerCalibrationS(), 0.0);
        assertEquals(2, config.getFloorGround());
        assertEquals(1, config.getFloorMin());

        assertEquals(150.0, config.getRadarRangeM(), 0.0);
        assertEquals(GameConfig.RadarOrientation.PHONE_UP, config.getRadarOrientation());
        assertFalse(config.isRadarShowDistance());
        assertEquals(EnumSet.of(GameState.TRACKING, GameState.CAPTURE),
                config.getRadarVisibleStates());
        assertFalse(config.isAdminSpawnAtMapPoint());
        assertFalse(config.isAdminIgnoreZonesAndProbability());
        assertFalse(config.isAdminDebugOnly());
        assertFalse(config.isCameraGlitchOnSpawn());
        assertFalse(config.isCameraGlitchOnCapture());
        assertFalse(config.isScreamerVibrationEnabled());
        assertFalse(config.isCapturePauseOnLockLost());
        assertFalse(config.isCaptureGhostPositionLocked());
        assertTrue(config.isNoiseAffectsCapture());
        assertTrue(config.isGadgetsUsableDuringCapture());
        assertFalse(config.isMineOneShot());
        assertFalse(config.isMinePersistUntilTriggered());
        assertFalse(config.isMineVisibleOnMap());
        assertFalse(config.isMineAlertOnStun());
        assertFalse(config.isLurePriorityOverFlee());
        assertFalse(config.isLureOneShot());
        assertTrue(config.isLureAffectsStunned());
        assertEquals(GameConfig.UiLanguage.FRENCH, config.getUiLanguage());
        assertEquals(GameConfig.ScreenOrientation.PORTRAIT, config.getOrientation());
        assertFalse(config.isPauseOnBackground());
        assertEquals(GameConfig.SoundSource.SYSTEM_DEFAULT, config.getSoundSource());
        assertFalse(config.isVibrationEnabled());
        assertFalse(config.isAssetsOptional());
    }

    @Test
    public void radarVisibleStatesAreCopiedAndUnmodifiable() {
        EnumSet<GameState> states = EnumSet.of(GameState.TRACKING, GameState.CAPTURE);

        GameConfig config = new GameConfig.Builder()
                .setRadarVisibleStates(states)
                .build();
        states.clear();

        assertEquals(EnumSet.of(GameState.TRACKING, GameState.CAPTURE),
                config.getRadarVisibleStates());
        assertThrows(UnsupportedOperationException.class,
                () -> config.getRadarVisibleStates().add(GameState.EXPLORATION));
    }
}
