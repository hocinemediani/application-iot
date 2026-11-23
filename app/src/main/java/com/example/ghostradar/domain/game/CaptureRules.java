package com.example.ghostradar.domain.game;

import com.example.ghostradar.domain.Validation;
import com.example.ghostradar.domain.config.GameConfig;

/**
 * Règles d'intégration pendant la phase {@link GameState#CAPTURE} (SPEC.md §3).
 *
 * <p>Le bruit et les gadgets n'ont aucun effet pendant la capture quand
 * {@code NOISE_AFFECTS_CAPTURE} et {@code GADGETS_USABLE_DURING_CAPTURE} valent
 * {@code false}. Ces règles sont pures et statiques : l'orchestrateur doit les
 * appliquer avant de transmettre une alerte bruit à {@code GhostMovement} ou
 * d'accepter {@code EctoplasmMine.place}/{@code Lure.activate}. Les composants
 * capteurs et gadgets restent inconscients de la phase de jeu.</p>
 */
public final class CaptureRules {

    private CaptureRules() {
    }

    /**
     * Indique si une alerte bruit doit être transmise au déplacement.
     *
     * @param config     configuration V1
     * @param state      phase courante de la session
     * @param noiseAlert alerte brute du monitor
     * @return {@code false} pendant une capture si la configuration interdit
     *         au bruit d'influer sur la capture
     */
    public static boolean isNoiseAlertEffective(GameConfig config, GameState state,
                                                boolean noiseAlert) {
        Validation.requireNonNull("config", config);
        Validation.requireNonNull("state", state);
        if (state == GameState.CAPTURE && !config.isNoiseAffectsCapture()) {
            return false;
        }
        return noiseAlert;
    }

    /**
     * Indique si un gadget peut être utilisé dans la phase courante.
     *
     * @return {@code false} pendant une capture sauf configuration contraire
     */
    public static boolean canUseGadget(GameConfig config, GameState state) {
        Validation.requireNonNull("config", config);
        Validation.requireNonNull("state", state);
        if (state == GameState.CAPTURE && !config.isGadgetsUsableDuringCapture()) {
            return false;
        }
        return true;
    }
}
