package com.example.ghostradar.domain.game;

import com.example.ghostradar.domain.config.GameConfig;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class CaptureRulesTest {

    @Test
    public void noiseAlertIsIgnoredDuringCaptureUnlessConfigAllowsIt() {
        GameConfig defaults = GameConfig.defaults();

        assertTrue(CaptureRules.isNoiseAlertEffective(defaults, GameState.TRACKING, true));
        assertFalse(CaptureRules.isNoiseAlertEffective(defaults, GameState.TRACKING, false));
        assertFalse(CaptureRules.isNoiseAlertEffective(defaults, GameState.CAPTURE, true));

        GameConfig allowing = new GameConfig.Builder()
                .setNoiseAffectsCapture(true)
                .build();
        assertTrue(CaptureRules.isNoiseAlertEffective(allowing, GameState.CAPTURE, true));
        assertFalse(CaptureRules.isNoiseAlertEffective(allowing, GameState.CAPTURE, false));
    }

    @Test
    public void gadgetsAreRefusedDuringCaptureUnlessConfigAllowsThem() {
        GameConfig defaults = GameConfig.defaults();

        assertTrue(CaptureRules.canUseGadget(defaults, GameState.EXPLORATION));
        assertTrue(CaptureRules.canUseGadget(defaults, GameState.TRACKING));
        assertTrue(CaptureRules.canUseGadget(defaults, GameState.SCREAMER));
        assertFalse(CaptureRules.canUseGadget(defaults, GameState.CAPTURE));
        assertTrue(CaptureRules.canUseGadget(defaults, GameState.RECOMPENSE));

        GameConfig allowing = new GameConfig.Builder()
                .setGadgetsUsableDuringCapture(true)
                .build();
        assertTrue(CaptureRules.canUseGadget(allowing, GameState.CAPTURE));
    }

    @Test
    public void rejectsNullArguments() {
        assertThrows(IllegalArgumentException.class,
                () -> CaptureRules.isNoiseAlertEffective(null, GameState.CAPTURE, true));
        assertThrows(IllegalArgumentException.class,
                () -> CaptureRules.isNoiseAlertEffective(GameConfig.defaults(), null, true));
        assertThrows(IllegalArgumentException.class,
                () -> CaptureRules.canUseGadget(null, GameState.CAPTURE));
        assertThrows(IllegalArgumentException.class,
                () -> CaptureRules.canUseGadget(GameConfig.defaults(), null));
    }
}
