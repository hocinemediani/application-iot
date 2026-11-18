package com.example.ghostradar.domain.config;

import com.example.ghostradar.domain.game.GameState;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

/**
 * Configuration centrale de la simulation.
 *
 * <p>Toutes les valeurs par défaut proviennent de {@code SPEC.md} §6. La
 * configuration est immuable et se construit via {@link Builder}.</p>
 */
public final class GameConfig {

    private final double earthRadiusM;
    private final int minPolygonVertices;
    private final double minVertexDistanceM;
    private final double gpsAccuracyMaxM;
    private final int gpsUpdateIntervalMs;
    private final double gpsMinDistanceM;
    private final boolean gpsSmoothingEnabled;
    private final double cartographySampleDistanceM;
    private final int cartographySampleIntervalMs;
    private final double douglasPeuckerToleranceM;
    private final boolean cartographyUndoEnabled;
    private final MapProjectionType mapProjection;

    private final double cameraBlueFilterAlpha;
    private final int cameraGlitchDurationMs;
    private final double compassLowPassAlpha;
    private final boolean compassFallbackEnabled;
    private final boolean compassIgnoreUnreliable;
    private final int noiseWindowMs;
    private final double noiseSmoothingAlpha;
    private final double noiseThresholdDbfs;
    private final double noiseHysteresisS;
    private final double noiseAlertHoldS;
    private final double noiseGaugeMinDbfs;
    private final double noiseGaugeMaxDbfs;
    private final double tiltDeadzoneDeg;
    private final double tiltGain;
    private final double tiltClampDeg;
    private final double accelSamplingHz;

    private final int tickMs;
    private final double zoneTriggerProbabilityPerS;
    private final double zoneTriggerCooldownS;
    private final int zonePolygonMinVertices;
    private final int zonePolygonMaxVertices;
    private final double zoneRadiusMinM;
    private final double zoneRadiusMaxM;
    private final double adminZoneDefaultRadiusM;
    private final double ghostSpawnMinDistanceM;
    private final double ghostSpawnMaxDistanceM;
    private final double ghostBaseSpeedMps;
    private final double ghostFleeMultiplier;
    private final double mineTriggerRadiusM;
    private final double mineStunDurationS;
    private final int mineMaxActive;
    private final double mineMinDistanceM;
    private final double lureDurationS;
    private final double lureSpeedMps;
    private final double captureDistanceM;
    private final double captureRequiredSeconds;
    private final double captureAngleToleranceDeg;
    private final double captureTimeoutS;
    private final double captureProgressDecayPerS;
    private final double captureForceChangeMinS;
    private final double captureForceChangeMaxS;
    private final double captureForceAmplitudeDeg;
    private final double screamerDelayMinS;
    private final double screamerDelayMaxS;
    private final double screamerDurationS;
    private final boolean screamerEnabled;
    private final boolean screamerSkippable;
    private final boolean playerOutOfAreaPause;

    private final int startingMineCount;
    private final int startingLureCount;
    private final int lootboxPerCapture;
    private final double lootMineWeight;
    private final double lootLureWeight;
    private final double lootBeamWeight;
    private final boolean pityEnabled;
    private final int pityMissesBeforeGuarantee;
    private final PityGuaranteeType pityGuaranteeType;
    private final int beamMaxLevel;
    private final double beamBaseRequiredSeconds;
    private final double beamTimeReductionPerLevelS;
    private final double beamMinRequiredSeconds;
    private final double beamBaseAngleToleranceDeg;
    private final double beamAngleToleranceStepDeg;
    private final int beamLevelsPerToleranceStep;
    private final int identityCount;
    private final double identificationFreqMinHz;
    private final double identificationFreqMaxHz;
    private final double identificationToleranceHz;
    private final int identificationAttempts;
    private final boolean identificationRetryAllowed;
    private final boolean identityHasGameplayEffect;

    private final double pressurePerFloorHpa;
    private final double floorHysteresisHpa;
    private final double barometerCalibrationS;
    private final int floorGround;
    private final int floorMin;

    private final double radarRangeM;
    private final RadarOrientation radarOrientation;
    private final boolean radarShowDistance;
    private final Set<GameState> radarVisibleStates;
    private final boolean adminSpawnAtMapPoint;
    private final boolean adminIgnoreZonesAndProbability;
    private final boolean adminDebugOnly;
    private final boolean cameraGlitchOnSpawn;
    private final boolean cameraGlitchOnCapture;
    private final boolean screamerVibrationEnabled;
    private final boolean capturePauseOnLockLost;
    private final boolean captureGhostPositionLocked;
    private final boolean noiseAffectsCapture;
    private final boolean gadgetsUsableDuringCapture;
    private final boolean mineOneShot;
    private final boolean minePersistUntilTriggered;
    private final boolean mineVisibleOnMap;
    private final boolean mineAlertOnStun;
    private final boolean lurePriorityOverFlee;
    private final boolean lureOneShot;
    private final boolean lureAffectsStunned;
    private final UiLanguage uiLanguage;
    private final ScreenOrientation orientation;
    private final boolean pauseOnBackground;
    private final SoundSource soundSource;
    private final boolean vibrationEnabled;
    private final boolean assetsOptional;

    private GameConfig(Builder builder) {
        this.earthRadiusM = builder.earthRadiusM;
        this.minPolygonVertices = builder.minPolygonVertices;
        this.minVertexDistanceM = builder.minVertexDistanceM;
        this.gpsAccuracyMaxM = builder.gpsAccuracyMaxM;
        this.gpsUpdateIntervalMs = builder.gpsUpdateIntervalMs;
        this.gpsMinDistanceM = builder.gpsMinDistanceM;
        this.gpsSmoothingEnabled = builder.gpsSmoothingEnabled;
        this.cartographySampleDistanceM = builder.cartographySampleDistanceM;
        this.cartographySampleIntervalMs = builder.cartographySampleIntervalMs;
        this.douglasPeuckerToleranceM = builder.douglasPeuckerToleranceM;
        this.cartographyUndoEnabled = builder.cartographyUndoEnabled;
        this.mapProjection = builder.mapProjection;

        this.cameraBlueFilterAlpha = builder.cameraBlueFilterAlpha;
        this.cameraGlitchDurationMs = builder.cameraGlitchDurationMs;
        this.compassLowPassAlpha = builder.compassLowPassAlpha;
        this.compassFallbackEnabled = builder.compassFallbackEnabled;
        this.compassIgnoreUnreliable = builder.compassIgnoreUnreliable;
        this.noiseWindowMs = builder.noiseWindowMs;
        this.noiseSmoothingAlpha = builder.noiseSmoothingAlpha;
        this.noiseThresholdDbfs = builder.noiseThresholdDbfs;
        this.noiseHysteresisS = builder.noiseHysteresisS;
        this.noiseAlertHoldS = builder.noiseAlertHoldS;
        this.noiseGaugeMinDbfs = builder.noiseGaugeMinDbfs;
        this.noiseGaugeMaxDbfs = builder.noiseGaugeMaxDbfs;
        this.tiltDeadzoneDeg = builder.tiltDeadzoneDeg;
        this.tiltGain = builder.tiltGain;
        this.tiltClampDeg = builder.tiltClampDeg;
        this.accelSamplingHz = builder.accelSamplingHz;

        this.tickMs = builder.tickMs;
        this.zoneTriggerProbabilityPerS = builder.zoneTriggerProbabilityPerS;
        this.zoneTriggerCooldownS = builder.zoneTriggerCooldownS;
        this.zonePolygonMinVertices = builder.zonePolygonMinVertices;
        this.zonePolygonMaxVertices = builder.zonePolygonMaxVertices;
        this.zoneRadiusMinM = builder.zoneRadiusMinM;
        this.zoneRadiusMaxM = builder.zoneRadiusMaxM;
        this.adminZoneDefaultRadiusM = builder.adminZoneDefaultRadiusM;
        this.ghostSpawnMinDistanceM = builder.ghostSpawnMinDistanceM;
        this.ghostSpawnMaxDistanceM = builder.ghostSpawnMaxDistanceM;
        this.ghostBaseSpeedMps = builder.ghostBaseSpeedMps;
        this.ghostFleeMultiplier = builder.ghostFleeMultiplier;
        this.mineTriggerRadiusM = builder.mineTriggerRadiusM;
        this.mineStunDurationS = builder.mineStunDurationS;
        this.mineMaxActive = builder.mineMaxActive;
        this.mineMinDistanceM = builder.mineMinDistanceM;
        this.lureDurationS = builder.lureDurationS;
        this.lureSpeedMps = builder.lureSpeedMps;
        this.captureDistanceM = builder.captureDistanceM;
        this.captureRequiredSeconds = builder.captureRequiredSeconds;
        this.captureAngleToleranceDeg = builder.captureAngleToleranceDeg;
        this.captureTimeoutS = builder.captureTimeoutS;
        this.captureProgressDecayPerS = builder.captureProgressDecayPerS;
        this.captureForceChangeMinS = builder.captureForceChangeMinS;
        this.captureForceChangeMaxS = builder.captureForceChangeMaxS;
        this.captureForceAmplitudeDeg = builder.captureForceAmplitudeDeg;
        this.screamerDelayMinS = builder.screamerDelayMinS;
        this.screamerDelayMaxS = builder.screamerDelayMaxS;
        this.screamerDurationS = builder.screamerDurationS;
        this.screamerEnabled = builder.screamerEnabled;
        this.screamerSkippable = builder.screamerSkippable;
        this.playerOutOfAreaPause = builder.playerOutOfAreaPause;

        this.startingMineCount = builder.startingMineCount;
        this.startingLureCount = builder.startingLureCount;
        this.lootboxPerCapture = builder.lootboxPerCapture;
        this.lootMineWeight = builder.lootMineWeight;
        this.lootLureWeight = builder.lootLureWeight;
        this.lootBeamWeight = builder.lootBeamWeight;
        this.pityEnabled = builder.pityEnabled;
        this.pityMissesBeforeGuarantee = builder.pityMissesBeforeGuarantee;
        this.pityGuaranteeType = builder.pityGuaranteeType;
        this.beamMaxLevel = builder.beamMaxLevel;
        this.beamBaseRequiredSeconds = builder.beamBaseRequiredSeconds;
        this.beamTimeReductionPerLevelS = builder.beamTimeReductionPerLevelS;
        this.beamMinRequiredSeconds = builder.beamMinRequiredSeconds;
        this.beamBaseAngleToleranceDeg = builder.beamBaseAngleToleranceDeg;
        this.beamAngleToleranceStepDeg = builder.beamAngleToleranceStepDeg;
        this.beamLevelsPerToleranceStep = builder.beamLevelsPerToleranceStep;
        this.identityCount = builder.identityCount;
        this.identificationFreqMinHz = builder.identificationFreqMinHz;
        this.identificationFreqMaxHz = builder.identificationFreqMaxHz;
        this.identificationToleranceHz = builder.identificationToleranceHz;
        this.identificationAttempts = builder.identificationAttempts;
        this.identificationRetryAllowed = builder.identificationRetryAllowed;
        this.identityHasGameplayEffect = builder.identityHasGameplayEffect;

        this.pressurePerFloorHpa = builder.pressurePerFloorHpa;
        this.floorHysteresisHpa = builder.floorHysteresisHpa;
        this.barometerCalibrationS = builder.barometerCalibrationS;
        this.floorGround = builder.floorGround;
        this.floorMin = builder.floorMin;

        this.radarRangeM = builder.radarRangeM;
        this.radarOrientation = builder.radarOrientation;
        this.radarShowDistance = builder.radarShowDistance;
        this.radarVisibleStates = Collections.unmodifiableSet(
                new HashSet<>(builder.radarVisibleStates));
        this.adminSpawnAtMapPoint = builder.adminSpawnAtMapPoint;
        this.adminIgnoreZonesAndProbability = builder.adminIgnoreZonesAndProbability;
        this.adminDebugOnly = builder.adminDebugOnly;
        this.cameraGlitchOnSpawn = builder.cameraGlitchOnSpawn;
        this.cameraGlitchOnCapture = builder.cameraGlitchOnCapture;
        this.screamerVibrationEnabled = builder.screamerVibrationEnabled;
        this.capturePauseOnLockLost = builder.capturePauseOnLockLost;
        this.captureGhostPositionLocked = builder.captureGhostPositionLocked;
        this.noiseAffectsCapture = builder.noiseAffectsCapture;
        this.gadgetsUsableDuringCapture = builder.gadgetsUsableDuringCapture;
        this.mineOneShot = builder.mineOneShot;
        this.minePersistUntilTriggered = builder.minePersistUntilTriggered;
        this.mineVisibleOnMap = builder.mineVisibleOnMap;
        this.mineAlertOnStun = builder.mineAlertOnStun;
        this.lurePriorityOverFlee = builder.lurePriorityOverFlee;
        this.lureOneShot = builder.lureOneShot;
        this.lureAffectsStunned = builder.lureAffectsStunned;
        this.uiLanguage = builder.uiLanguage;
        this.orientation = builder.orientation;
        this.pauseOnBackground = builder.pauseOnBackground;
        this.soundSource = builder.soundSource;
        this.vibrationEnabled = builder.vibrationEnabled;
        this.assetsOptional = builder.assetsOptional;
    }

    public static GameConfig defaults() {
        return new Builder().build();
    }

    public double getEarthRadiusM() {
        return earthRadiusM;
    }

    public int getMinPolygonVertices() {
        return minPolygonVertices;
    }

    public double getMinVertexDistanceM() {
        return minVertexDistanceM;
    }

    public double getGpsAccuracyMaxM() {
        return gpsAccuracyMaxM;
    }

    public int getGpsUpdateIntervalMs() {
        return gpsUpdateIntervalMs;
    }

    public double getGpsMinDistanceM() {
        return gpsMinDistanceM;
    }

    public boolean isGpsSmoothingEnabled() {
        return gpsSmoothingEnabled;
    }

    public double getCartographySampleDistanceM() {
        return cartographySampleDistanceM;
    }

    public int getCartographySampleIntervalMs() {
        return cartographySampleIntervalMs;
    }

    public double getDouglasPeuckerToleranceM() {
        return douglasPeuckerToleranceM;
    }

    public boolean isCartographyUndoEnabled() {
        return cartographyUndoEnabled;
    }

    public MapProjectionType getMapProjection() {
        return mapProjection;
    }

    public double getCameraBlueFilterAlpha() {
        return cameraBlueFilterAlpha;
    }

    public int getCameraGlitchDurationMs() {
        return cameraGlitchDurationMs;
    }

    public double getCompassLowPassAlpha() {
        return compassLowPassAlpha;
    }

    public boolean isCompassFallbackEnabled() {
        return compassFallbackEnabled;
    }

    public boolean isCompassIgnoreUnreliable() {
        return compassIgnoreUnreliable;
    }

    public int getNoiseWindowMs() {
        return noiseWindowMs;
    }

    public double getNoiseSmoothingAlpha() {
        return noiseSmoothingAlpha;
    }

    public double getNoiseThresholdDbfs() {
        return noiseThresholdDbfs;
    }

    public double getNoiseHysteresisS() {
        return noiseHysteresisS;
    }

    public double getNoiseAlertHoldS() {
        return noiseAlertHoldS;
    }

    public double getNoiseGaugeMinDbfs() {
        return noiseGaugeMinDbfs;
    }

    public double getNoiseGaugeMaxDbfs() {
        return noiseGaugeMaxDbfs;
    }

    public double getTiltDeadzoneDeg() {
        return tiltDeadzoneDeg;
    }

    public double getTiltGain() {
        return tiltGain;
    }

    public double getTiltClampDeg() {
        return tiltClampDeg;
    }

    public double getAccelSamplingHz() {
        return accelSamplingHz;
    }

    public int getTickMs() {
        return tickMs;
    }

    public double getZoneTriggerProbabilityPerS() {
        return zoneTriggerProbabilityPerS;
    }

    public double getZoneTriggerCooldownS() {
        return zoneTriggerCooldownS;
    }

    public int getZonePolygonMinVertices() {
        return zonePolygonMinVertices;
    }

    public int getZonePolygonMaxVertices() {
        return zonePolygonMaxVertices;
    }

    public double getZoneRadiusMinM() {
        return zoneRadiusMinM;
    }

    public double getZoneRadiusMaxM() {
        return zoneRadiusMaxM;
    }

    public double getAdminZoneDefaultRadiusM() {
        return adminZoneDefaultRadiusM;
    }

    public double getGhostSpawnMinDistanceM() {
        return ghostSpawnMinDistanceM;
    }

    public double getGhostSpawnMaxDistanceM() {
        return ghostSpawnMaxDistanceM;
    }

    public double getGhostBaseSpeedMps() {
        return ghostBaseSpeedMps;
    }

    public double getGhostFleeMultiplier() {
        return ghostFleeMultiplier;
    }

    public double getMineTriggerRadiusM() {
        return mineTriggerRadiusM;
    }

    public double getMineStunDurationS() {
        return mineStunDurationS;
    }

    public int getMineMaxActive() {
        return mineMaxActive;
    }

    public double getMineMinDistanceM() {
        return mineMinDistanceM;
    }

    public double getLureDurationS() {
        return lureDurationS;
    }

    public double getLureSpeedMps() {
        return lureSpeedMps;
    }

    public double getCaptureDistanceM() {
        return captureDistanceM;
    }

    public double getCaptureRequiredSeconds() {
        return captureRequiredSeconds;
    }

    public double getCaptureAngleToleranceDeg() {
        return captureAngleToleranceDeg;
    }

    public double getCaptureTimeoutS() {
        return captureTimeoutS;
    }

    public double getCaptureProgressDecayPerS() {
        return captureProgressDecayPerS;
    }

    public double getCaptureForceChangeMinS() {
        return captureForceChangeMinS;
    }

    public double getCaptureForceChangeMaxS() {
        return captureForceChangeMaxS;
    }

    public double getCaptureForceAmplitudeDeg() {
        return captureForceAmplitudeDeg;
    }

    public double getScreamerDelayMinS() {
        return screamerDelayMinS;
    }

    public double getScreamerDelayMaxS() {
        return screamerDelayMaxS;
    }

    public double getScreamerDurationS() {
        return screamerDurationS;
    }

    public boolean isScreamerEnabled() {
        return screamerEnabled;
    }

    public boolean isScreamerSkippable() {
        return screamerSkippable;
    }

    public boolean isPlayerOutOfAreaPause() {
        return playerOutOfAreaPause;
    }

    public int getStartingMineCount() {
        return startingMineCount;
    }

    public int getStartingLureCount() {
        return startingLureCount;
    }

    public int getLootboxPerCapture() {
        return lootboxPerCapture;
    }

    public double getLootMineWeight() {
        return lootMineWeight;
    }

    public double getLootLureWeight() {
        return lootLureWeight;
    }

    public double getLootBeamWeight() {
        return lootBeamWeight;
    }

    public boolean isPityEnabled() {
        return pityEnabled;
    }

    public int getPityMissesBeforeGuarantee() {
        return pityMissesBeforeGuarantee;
    }

    public PityGuaranteeType getPityGuaranteeType() {
        return pityGuaranteeType;
    }

    public int getBeamMaxLevel() {
        return beamMaxLevel;
    }

    public double getBeamBaseRequiredSeconds() {
        return beamBaseRequiredSeconds;
    }

    public double getBeamTimeReductionPerLevelS() {
        return beamTimeReductionPerLevelS;
    }

    public double getBeamMinRequiredSeconds() {
        return beamMinRequiredSeconds;
    }

    public double getBeamBaseAngleToleranceDeg() {
        return beamBaseAngleToleranceDeg;
    }

    public double getBeamAngleToleranceStepDeg() {
        return beamAngleToleranceStepDeg;
    }

    public int getBeamLevelsPerToleranceStep() {
        return beamLevelsPerToleranceStep;
    }

    public int getIdentityCount() {
        return identityCount;
    }

    public double getIdentificationFreqMinHz() {
        return identificationFreqMinHz;
    }

    public double getIdentificationFreqMaxHz() {
        return identificationFreqMaxHz;
    }

    public double getIdentificationToleranceHz() {
        return identificationToleranceHz;
    }

    public int getIdentificationAttempts() {
        return identificationAttempts;
    }

    public boolean isIdentificationRetryAllowed() {
        return identificationRetryAllowed;
    }

    public boolean isIdentityHasGameplayEffect() {
        return identityHasGameplayEffect;
    }

    public double getPressurePerFloorHpa() {
        return pressurePerFloorHpa;
    }

    public double getFloorHysteresisHpa() {
        return floorHysteresisHpa;
    }

    public double getBarometerCalibrationS() {
        return barometerCalibrationS;
    }

    public int getFloorGround() {
        return floorGround;
    }

    public int getFloorMin() {
        return floorMin;
    }

    public double getRadarRangeM() {
        return radarRangeM;
    }

    public RadarOrientation getRadarOrientation() {
        return radarOrientation;
    }

    public boolean isRadarShowDistance() {
        return radarShowDistance;
    }

    public Set<GameState> getRadarVisibleStates() {
        return radarVisibleStates;
    }

    public boolean isAdminSpawnAtMapPoint() {
        return adminSpawnAtMapPoint;
    }

    public boolean isAdminIgnoreZonesAndProbability() {
        return adminIgnoreZonesAndProbability;
    }

    public boolean isAdminDebugOnly() {
        return adminDebugOnly;
    }

    public boolean isCameraGlitchOnSpawn() {
        return cameraGlitchOnSpawn;
    }

    public boolean isCameraGlitchOnCapture() {
        return cameraGlitchOnCapture;
    }

    public boolean isScreamerVibrationEnabled() {
        return screamerVibrationEnabled;
    }

    public boolean isCapturePauseOnLockLost() {
        return capturePauseOnLockLost;
    }

    public boolean isCaptureGhostPositionLocked() {
        return captureGhostPositionLocked;
    }

    public boolean isNoiseAffectsCapture() {
        return noiseAffectsCapture;
    }

    public boolean isGadgetsUsableDuringCapture() {
        return gadgetsUsableDuringCapture;
    }

    public boolean isMineOneShot() {
        return mineOneShot;
    }

    public boolean isMinePersistUntilTriggered() {
        return minePersistUntilTriggered;
    }

    public boolean isMineVisibleOnMap() {
        return mineVisibleOnMap;
    }

    public boolean isMineAlertOnStun() {
        return mineAlertOnStun;
    }

    public boolean isLurePriorityOverFlee() {
        return lurePriorityOverFlee;
    }

    public boolean isLureOneShot() {
        return lureOneShot;
    }

    public boolean isLureAffectsStunned() {
        return lureAffectsStunned;
    }

    public UiLanguage getUiLanguage() {
        return uiLanguage;
    }

    public ScreenOrientation getOrientation() {
        return orientation;
    }

    public boolean isPauseOnBackground() {
        return pauseOnBackground;
    }

    public SoundSource getSoundSource() {
        return soundSource;
    }

    public boolean isVibrationEnabled() {
        return vibrationEnabled;
    }

    public boolean isAssetsOptional() {
        return assetsOptional;
    }

    public enum MapProjectionType {
        LOCAL_ENU
    }

    public enum RadarOrientation {
        PHONE_UP
    }

    public enum PityGuaranteeType {
        BEAM_UPGRADE
    }

    public enum UiLanguage {
        FRENCH
    }

    public enum ScreenOrientation {
        PORTRAIT
    }

    public enum SoundSource {
        SYSTEM_DEFAULT
    }

    public static final class Builder {

        private double earthRadiusM = 6_371_000.0;
        private int minPolygonVertices = 3;
        private double minVertexDistanceM = 0.5;
        private double gpsAccuracyMaxM = 20.0;
        private int gpsUpdateIntervalMs = 1_000;
        private double gpsMinDistanceM = 1.0;
        private boolean gpsSmoothingEnabled = false;
        private double cartographySampleDistanceM = 3.0;
        private int cartographySampleIntervalMs = 2_000;
        private double douglasPeuckerToleranceM = 1.0;
        private boolean cartographyUndoEnabled = true;
        private MapProjectionType mapProjection = MapProjectionType.LOCAL_ENU;

        private double cameraBlueFilterAlpha = 0.15;
        private int cameraGlitchDurationMs = 500;
        private double compassLowPassAlpha = 0.15;
        private boolean compassFallbackEnabled = true;
        private boolean compassIgnoreUnreliable = true;
        private int noiseWindowMs = 200;
        private double noiseSmoothingAlpha = 0.2;
        private double noiseThresholdDbfs = -30.0;
        private double noiseHysteresisS = 2.0;
        private double noiseAlertHoldS = 3.0;
        private double noiseGaugeMinDbfs = -60.0;
        private double noiseGaugeMaxDbfs = 0.0;
        private double tiltDeadzoneDeg = 5.0;
        private double tiltGain = 1.0;
        private double tiltClampDeg = 30.0;
        private double accelSamplingHz = 50.0;

        private int tickMs = 100;
        private double zoneTriggerProbabilityPerS = 0.02;
        private double zoneTriggerCooldownS = 30.0;
        private int zonePolygonMinVertices = 5;
        private int zonePolygonMaxVertices = 8;
        private double zoneRadiusMinM = 10.0;
        private double zoneRadiusMaxM = 30.0;
        private double adminZoneDefaultRadiusM = 25.0;
        private double ghostSpawnMinDistanceM = 15.0;
        private double ghostSpawnMaxDistanceM = 40.0;
        private double ghostBaseSpeedMps = 1.2;
        private double ghostFleeMultiplier = 2.0;
        private double mineTriggerRadiusM = 3.0;
        private double mineStunDurationS = 10.0;
        private int mineMaxActive = 3;
        private double mineMinDistanceM = 5.0;
        private double lureDurationS = 8.0;
        private double lureSpeedMps = 1.5;
        private double captureDistanceM = 4.0;
        private double captureRequiredSeconds = 5.0;
        private double captureAngleToleranceDeg = 12.0;
        private double captureTimeoutS = 30.0;
        private double captureProgressDecayPerS = 0.0;
        private double captureForceChangeMinS = 0.5;
        private double captureForceChangeMaxS = 1.5;
        private double captureForceAmplitudeDeg = 20.0;
        private double screamerDelayMinS = 1.0;
        private double screamerDelayMaxS = 2.0;
        private double screamerDurationS = 2.0;
        private boolean screamerEnabled = true;
        private boolean screamerSkippable = false;
        private boolean playerOutOfAreaPause = true;

        private int startingMineCount = 1;
        private int startingLureCount = 1;
        private int lootboxPerCapture = 1;
        private double lootMineWeight = 45.0;
        private double lootLureWeight = 45.0;
        private double lootBeamWeight = 10.0;
        private boolean pityEnabled = true;
        private int pityMissesBeforeGuarantee = 10;
        private PityGuaranteeType pityGuaranteeType = PityGuaranteeType.BEAM_UPGRADE;
        private int beamMaxLevel = 5;
        private double beamBaseRequiredSeconds = 5.0;
        private double beamTimeReductionPerLevelS = 0.5;
        private double beamMinRequiredSeconds = 2.0;
        private double beamBaseAngleToleranceDeg = 12.0;
        private double beamAngleToleranceStepDeg = 2.0;
        private int beamLevelsPerToleranceStep = 2;
        private int identityCount = 10;
        private double identificationFreqMinHz = 1.0;
        private double identificationFreqMaxHz = 10.0;
        private double identificationToleranceHz = 0.5;
        private int identificationAttempts = 3;
        private boolean identificationRetryAllowed = true;
        private boolean identityHasGameplayEffect = false;

        private double pressurePerFloorHpa = 0.36;
        private double floorHysteresisHpa = 0.1;
        private double barometerCalibrationS = 5.0;
        private int floorGround = 0;
        private int floorMin = 0;

        private double radarRangeM = 100.0;
        private RadarOrientation radarOrientation = RadarOrientation.PHONE_UP;
        private boolean radarShowDistance = true;
        private Set<GameState> radarVisibleStates = EnumSet.of(GameState.TRACKING);
        private boolean adminSpawnAtMapPoint = true;
        private boolean adminIgnoreZonesAndProbability = true;
        private boolean adminDebugOnly = true;
        private boolean cameraGlitchOnSpawn = true;
        private boolean cameraGlitchOnCapture = true;
        private boolean screamerVibrationEnabled = true;
        private boolean capturePauseOnLockLost = true;
        private boolean captureGhostPositionLocked = true;
        private boolean noiseAffectsCapture = false;
        private boolean gadgetsUsableDuringCapture = false;
        private boolean mineOneShot = true;
        private boolean minePersistUntilTriggered = true;
        private boolean mineVisibleOnMap = true;
        private boolean mineAlertOnStun = true;
        private boolean lurePriorityOverFlee = true;
        private boolean lureOneShot = true;
        private boolean lureAffectsStunned = false;
        private UiLanguage uiLanguage = UiLanguage.FRENCH;
        private ScreenOrientation orientation = ScreenOrientation.PORTRAIT;
        private boolean pauseOnBackground = true;
        private SoundSource soundSource = SoundSource.SYSTEM_DEFAULT;
        private boolean vibrationEnabled = true;
        private boolean assetsOptional = true;

        public Builder setEarthRadiusM(double earthRadiusM) {
            this.earthRadiusM = earthRadiusM;
            return this;
        }

        public Builder setMinPolygonVertices(int minPolygonVertices) {
            this.minPolygonVertices = minPolygonVertices;
            return this;
        }

        public Builder setMinVertexDistanceM(double minVertexDistanceM) {
            this.minVertexDistanceM = minVertexDistanceM;
            return this;
        }

        public Builder setGpsAccuracyMaxM(double gpsAccuracyMaxM) {
            this.gpsAccuracyMaxM = gpsAccuracyMaxM;
            return this;
        }

        public Builder setGpsUpdateIntervalMs(int gpsUpdateIntervalMs) {
            this.gpsUpdateIntervalMs = gpsUpdateIntervalMs;
            return this;
        }

        public Builder setGpsMinDistanceM(double gpsMinDistanceM) {
            this.gpsMinDistanceM = gpsMinDistanceM;
            return this;
        }

        public Builder setGpsSmoothingEnabled(boolean gpsSmoothingEnabled) {
            this.gpsSmoothingEnabled = gpsSmoothingEnabled;
            return this;
        }

        public Builder setCartographySampleDistanceM(double cartographySampleDistanceM) {
            this.cartographySampleDistanceM = cartographySampleDistanceM;
            return this;
        }

        public Builder setCartographySampleIntervalMs(int cartographySampleIntervalMs) {
            this.cartographySampleIntervalMs = cartographySampleIntervalMs;
            return this;
        }

        public Builder setDouglasPeuckerToleranceM(double douglasPeuckerToleranceM) {
            this.douglasPeuckerToleranceM = douglasPeuckerToleranceM;
            return this;
        }

        public Builder setCartographyUndoEnabled(boolean cartographyUndoEnabled) {
            this.cartographyUndoEnabled = cartographyUndoEnabled;
            return this;
        }

        public Builder setMapProjection(MapProjectionType mapProjection) {
            this.mapProjection = mapProjection;
            return this;
        }

        public Builder setCameraBlueFilterAlpha(double cameraBlueFilterAlpha) {
            this.cameraBlueFilterAlpha = cameraBlueFilterAlpha;
            return this;
        }

        public Builder setCameraGlitchDurationMs(int cameraGlitchDurationMs) {
            this.cameraGlitchDurationMs = cameraGlitchDurationMs;
            return this;
        }

        public Builder setCompassLowPassAlpha(double compassLowPassAlpha) {
            this.compassLowPassAlpha = compassLowPassAlpha;
            return this;
        }

        public Builder setCompassFallbackEnabled(boolean compassFallbackEnabled) {
            this.compassFallbackEnabled = compassFallbackEnabled;
            return this;
        }

        public Builder setCompassIgnoreUnreliable(boolean compassIgnoreUnreliable) {
            this.compassIgnoreUnreliable = compassIgnoreUnreliable;
            return this;
        }

        public Builder setNoiseWindowMs(int noiseWindowMs) {
            this.noiseWindowMs = noiseWindowMs;
            return this;
        }

        public Builder setNoiseSmoothingAlpha(double noiseSmoothingAlpha) {
            this.noiseSmoothingAlpha = noiseSmoothingAlpha;
            return this;
        }

        public Builder setNoiseThresholdDbfs(double noiseThresholdDbfs) {
            this.noiseThresholdDbfs = noiseThresholdDbfs;
            return this;
        }

        public Builder setNoiseHysteresisS(double noiseHysteresisS) {
            this.noiseHysteresisS = noiseHysteresisS;
            return this;
        }

        public Builder setNoiseAlertHoldS(double noiseAlertHoldS) {
            this.noiseAlertHoldS = noiseAlertHoldS;
            return this;
        }

        public Builder setNoiseGaugeMinDbfs(double noiseGaugeMinDbfs) {
            this.noiseGaugeMinDbfs = noiseGaugeMinDbfs;
            return this;
        }

        public Builder setNoiseGaugeMaxDbfs(double noiseGaugeMaxDbfs) {
            this.noiseGaugeMaxDbfs = noiseGaugeMaxDbfs;
            return this;
        }

        public Builder setTiltDeadzoneDeg(double tiltDeadzoneDeg) {
            this.tiltDeadzoneDeg = tiltDeadzoneDeg;
            return this;
        }

        public Builder setTiltGain(double tiltGain) {
            this.tiltGain = tiltGain;
            return this;
        }

        public Builder setTiltClampDeg(double tiltClampDeg) {
            this.tiltClampDeg = tiltClampDeg;
            return this;
        }

        public Builder setAccelSamplingHz(double accelSamplingHz) {
            this.accelSamplingHz = accelSamplingHz;
            return this;
        }

        public Builder setTickMs(int tickMs) {
            this.tickMs = tickMs;
            return this;
        }

        public Builder setZoneTriggerProbabilityPerS(double zoneTriggerProbabilityPerS) {
            this.zoneTriggerProbabilityPerS = zoneTriggerProbabilityPerS;
            return this;
        }

        public Builder setZoneTriggerCooldownS(double zoneTriggerCooldownS) {
            this.zoneTriggerCooldownS = zoneTriggerCooldownS;
            return this;
        }

        public Builder setZonePolygonMinVertices(int zonePolygonMinVertices) {
            this.zonePolygonMinVertices = zonePolygonMinVertices;
            return this;
        }

        public Builder setZonePolygonMaxVertices(int zonePolygonMaxVertices) {
            this.zonePolygonMaxVertices = zonePolygonMaxVertices;
            return this;
        }

        public Builder setZoneRadiusMinM(double zoneRadiusMinM) {
            this.zoneRadiusMinM = zoneRadiusMinM;
            return this;
        }

        public Builder setZoneRadiusMaxM(double zoneRadiusMaxM) {
            this.zoneRadiusMaxM = zoneRadiusMaxM;
            return this;
        }

        public Builder setAdminZoneDefaultRadiusM(double adminZoneDefaultRadiusM) {
            this.adminZoneDefaultRadiusM = adminZoneDefaultRadiusM;
            return this;
        }

        public Builder setGhostSpawnMinDistanceM(double ghostSpawnMinDistanceM) {
            this.ghostSpawnMinDistanceM = ghostSpawnMinDistanceM;
            return this;
        }

        public Builder setGhostSpawnMaxDistanceM(double ghostSpawnMaxDistanceM) {
            this.ghostSpawnMaxDistanceM = ghostSpawnMaxDistanceM;
            return this;
        }

        public Builder setGhostBaseSpeedMps(double ghostBaseSpeedMps) {
            this.ghostBaseSpeedMps = ghostBaseSpeedMps;
            return this;
        }

        public Builder setGhostFleeMultiplier(double ghostFleeMultiplier) {
            this.ghostFleeMultiplier = ghostFleeMultiplier;
            return this;
        }

        public Builder setMineTriggerRadiusM(double mineTriggerRadiusM) {
            this.mineTriggerRadiusM = mineTriggerRadiusM;
            return this;
        }

        public Builder setMineStunDurationS(double mineStunDurationS) {
            this.mineStunDurationS = mineStunDurationS;
            return this;
        }

        public Builder setMineMaxActive(int mineMaxActive) {
            this.mineMaxActive = mineMaxActive;
            return this;
        }

        public Builder setMineMinDistanceM(double mineMinDistanceM) {
            this.mineMinDistanceM = mineMinDistanceM;
            return this;
        }

        public Builder setLureDurationS(double lureDurationS) {
            this.lureDurationS = lureDurationS;
            return this;
        }

        public Builder setLureSpeedMps(double lureSpeedMps) {
            this.lureSpeedMps = lureSpeedMps;
            return this;
        }

        public Builder setCaptureDistanceM(double captureDistanceM) {
            this.captureDistanceM = captureDistanceM;
            return this;
        }

        public Builder setCaptureRequiredSeconds(double captureRequiredSeconds) {
            this.captureRequiredSeconds = captureRequiredSeconds;
            return this;
        }

        public Builder setCaptureAngleToleranceDeg(double captureAngleToleranceDeg) {
            this.captureAngleToleranceDeg = captureAngleToleranceDeg;
            return this;
        }

        public Builder setCaptureTimeoutS(double captureTimeoutS) {
            this.captureTimeoutS = captureTimeoutS;
            return this;
        }

        public Builder setCaptureProgressDecayPerS(double captureProgressDecayPerS) {
            this.captureProgressDecayPerS = captureProgressDecayPerS;
            return this;
        }

        public Builder setCaptureForceChangeMinS(double captureForceChangeMinS) {
            this.captureForceChangeMinS = captureForceChangeMinS;
            return this;
        }

        public Builder setCaptureForceChangeMaxS(double captureForceChangeMaxS) {
            this.captureForceChangeMaxS = captureForceChangeMaxS;
            return this;
        }

        public Builder setCaptureForceAmplitudeDeg(double captureForceAmplitudeDeg) {
            this.captureForceAmplitudeDeg = captureForceAmplitudeDeg;
            return this;
        }

        public Builder setScreamerDelayMinS(double screamerDelayMinS) {
            this.screamerDelayMinS = screamerDelayMinS;
            return this;
        }

        public Builder setScreamerDelayMaxS(double screamerDelayMaxS) {
            this.screamerDelayMaxS = screamerDelayMaxS;
            return this;
        }

        public Builder setScreamerDurationS(double screamerDurationS) {
            this.screamerDurationS = screamerDurationS;
            return this;
        }

        public Builder setScreamerEnabled(boolean screamerEnabled) {
            this.screamerEnabled = screamerEnabled;
            return this;
        }

        public Builder setScreamerSkippable(boolean screamerSkippable) {
            this.screamerSkippable = screamerSkippable;
            return this;
        }

        public Builder setPlayerOutOfAreaPause(boolean playerOutOfAreaPause) {
            this.playerOutOfAreaPause = playerOutOfAreaPause;
            return this;
        }

        public Builder setStartingMineCount(int startingMineCount) {
            this.startingMineCount = startingMineCount;
            return this;
        }

        public Builder setStartingLureCount(int startingLureCount) {
            this.startingLureCount = startingLureCount;
            return this;
        }

        public Builder setLootboxPerCapture(int lootboxPerCapture) {
            this.lootboxPerCapture = lootboxPerCapture;
            return this;
        }

        public Builder setLootMineWeight(double lootMineWeight) {
            this.lootMineWeight = lootMineWeight;
            return this;
        }

        public Builder setLootLureWeight(double lootLureWeight) {
            this.lootLureWeight = lootLureWeight;
            return this;
        }

        public Builder setLootBeamWeight(double lootBeamWeight) {
            this.lootBeamWeight = lootBeamWeight;
            return this;
        }

        public Builder setPityEnabled(boolean pityEnabled) {
            this.pityEnabled = pityEnabled;
            return this;
        }

        public Builder setPityMissesBeforeGuarantee(int pityMissesBeforeGuarantee) {
            this.pityMissesBeforeGuarantee = pityMissesBeforeGuarantee;
            return this;
        }

        public Builder setPityGuaranteeType(PityGuaranteeType pityGuaranteeType) {
            this.pityGuaranteeType = pityGuaranteeType;
            return this;
        }

        public Builder setBeamMaxLevel(int beamMaxLevel) {
            this.beamMaxLevel = beamMaxLevel;
            return this;
        }

        public Builder setBeamBaseRequiredSeconds(double beamBaseRequiredSeconds) {
            this.beamBaseRequiredSeconds = beamBaseRequiredSeconds;
            return this;
        }

        public Builder setBeamTimeReductionPerLevelS(double beamTimeReductionPerLevelS) {
            this.beamTimeReductionPerLevelS = beamTimeReductionPerLevelS;
            return this;
        }

        public Builder setBeamMinRequiredSeconds(double beamMinRequiredSeconds) {
            this.beamMinRequiredSeconds = beamMinRequiredSeconds;
            return this;
        }

        public Builder setBeamBaseAngleToleranceDeg(double beamBaseAngleToleranceDeg) {
            this.beamBaseAngleToleranceDeg = beamBaseAngleToleranceDeg;
            return this;
        }

        public Builder setBeamAngleToleranceStepDeg(double beamAngleToleranceStepDeg) {
            this.beamAngleToleranceStepDeg = beamAngleToleranceStepDeg;
            return this;
        }

        public Builder setBeamLevelsPerToleranceStep(int beamLevelsPerToleranceStep) {
            this.beamLevelsPerToleranceStep = beamLevelsPerToleranceStep;
            return this;
        }

        public Builder setIdentityCount(int identityCount) {
            this.identityCount = identityCount;
            return this;
        }

        public Builder setIdentificationFreqMinHz(double identificationFreqMinHz) {
            this.identificationFreqMinHz = identificationFreqMinHz;
            return this;
        }

        public Builder setIdentificationFreqMaxHz(double identificationFreqMaxHz) {
            this.identificationFreqMaxHz = identificationFreqMaxHz;
            return this;
        }

        public Builder setIdentificationToleranceHz(double identificationToleranceHz) {
            this.identificationToleranceHz = identificationToleranceHz;
            return this;
        }

        public Builder setIdentificationAttempts(int identificationAttempts) {
            this.identificationAttempts = identificationAttempts;
            return this;
        }

        public Builder setIdentificationRetryAllowed(boolean identificationRetryAllowed) {
            this.identificationRetryAllowed = identificationRetryAllowed;
            return this;
        }

        public Builder setIdentityHasGameplayEffect(boolean identityHasGameplayEffect) {
            this.identityHasGameplayEffect = identityHasGameplayEffect;
            return this;
        }

        public Builder setPressurePerFloorHpa(double pressurePerFloorHpa) {
            this.pressurePerFloorHpa = pressurePerFloorHpa;
            return this;
        }

        public Builder setFloorHysteresisHpa(double floorHysteresisHpa) {
            this.floorHysteresisHpa = floorHysteresisHpa;
            return this;
        }

        public Builder setBarometerCalibrationS(double barometerCalibrationS) {
            this.barometerCalibrationS = barometerCalibrationS;
            return this;
        }

        public Builder setFloorGround(int floorGround) {
            this.floorGround = floorGround;
            return this;
        }

        public Builder setFloorMin(int floorMin) {
            this.floorMin = floorMin;
            return this;
        }

        public Builder setRadarRangeM(double radarRangeM) {
            this.radarRangeM = radarRangeM;
            return this;
        }

        public Builder setRadarOrientation(RadarOrientation radarOrientation) {
            this.radarOrientation = radarOrientation;
            return this;
        }

        public Builder setRadarShowDistance(boolean radarShowDistance) {
            this.radarShowDistance = radarShowDistance;
            return this;
        }

        public Builder setRadarVisibleStates(Set<GameState> radarVisibleStates) {
            this.radarVisibleStates = radarVisibleStates;
            return this;
        }

        public Builder setAdminSpawnAtMapPoint(boolean adminSpawnAtMapPoint) {
            this.adminSpawnAtMapPoint = adminSpawnAtMapPoint;
            return this;
        }

        public Builder setAdminIgnoreZonesAndProbability(boolean adminIgnoreZonesAndProbability) {
            this.adminIgnoreZonesAndProbability = adminIgnoreZonesAndProbability;
            return this;
        }

        public Builder setAdminDebugOnly(boolean adminDebugOnly) {
            this.adminDebugOnly = adminDebugOnly;
            return this;
        }

        public Builder setCameraGlitchOnSpawn(boolean cameraGlitchOnSpawn) {
            this.cameraGlitchOnSpawn = cameraGlitchOnSpawn;
            return this;
        }

        public Builder setCameraGlitchOnCapture(boolean cameraGlitchOnCapture) {
            this.cameraGlitchOnCapture = cameraGlitchOnCapture;
            return this;
        }

        public Builder setScreamerVibrationEnabled(boolean screamerVibrationEnabled) {
            this.screamerVibrationEnabled = screamerVibrationEnabled;
            return this;
        }

        public Builder setCapturePauseOnLockLost(boolean capturePauseOnLockLost) {
            this.capturePauseOnLockLost = capturePauseOnLockLost;
            return this;
        }

        public Builder setCaptureGhostPositionLocked(boolean captureGhostPositionLocked) {
            this.captureGhostPositionLocked = captureGhostPositionLocked;
            return this;
        }

        public Builder setNoiseAffectsCapture(boolean noiseAffectsCapture) {
            this.noiseAffectsCapture = noiseAffectsCapture;
            return this;
        }

        public Builder setGadgetsUsableDuringCapture(boolean gadgetsUsableDuringCapture) {
            this.gadgetsUsableDuringCapture = gadgetsUsableDuringCapture;
            return this;
        }

        public Builder setMineOneShot(boolean mineOneShot) {
            this.mineOneShot = mineOneShot;
            return this;
        }

        public Builder setMinePersistUntilTriggered(boolean minePersistUntilTriggered) {
            this.minePersistUntilTriggered = minePersistUntilTriggered;
            return this;
        }

        public Builder setMineVisibleOnMap(boolean mineVisibleOnMap) {
            this.mineVisibleOnMap = mineVisibleOnMap;
            return this;
        }

        public Builder setMineAlertOnStun(boolean mineAlertOnStun) {
            this.mineAlertOnStun = mineAlertOnStun;
            return this;
        }

        public Builder setLurePriorityOverFlee(boolean lurePriorityOverFlee) {
            this.lurePriorityOverFlee = lurePriorityOverFlee;
            return this;
        }

        public Builder setLureOneShot(boolean lureOneShot) {
            this.lureOneShot = lureOneShot;
            return this;
        }

        public Builder setLureAffectsStunned(boolean lureAffectsStunned) {
            this.lureAffectsStunned = lureAffectsStunned;
            return this;
        }

        public Builder setUiLanguage(UiLanguage uiLanguage) {
            this.uiLanguage = uiLanguage;
            return this;
        }

        public Builder setOrientation(ScreenOrientation orientation) {
            this.orientation = orientation;
            return this;
        }

        public Builder setPauseOnBackground(boolean pauseOnBackground) {
            this.pauseOnBackground = pauseOnBackground;
            return this;
        }

        public Builder setSoundSource(SoundSource soundSource) {
            this.soundSource = soundSource;
            return this;
        }

        public Builder setVibrationEnabled(boolean vibrationEnabled) {
            this.vibrationEnabled = vibrationEnabled;
            return this;
        }

        public Builder setAssetsOptional(boolean assetsOptional) {
            this.assetsOptional = assetsOptional;
            return this;
        }

        public GameConfig build() {
            return new GameConfig(this);
        }
    }
}
