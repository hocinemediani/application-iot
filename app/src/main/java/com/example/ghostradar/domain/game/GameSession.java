package com.example.ghostradar.domain.game;

import com.example.ghostradar.domain.config.GameConfig;

/**
 * Machine à états de la boucle de jeu V1.
 *
 * <p>La session ne détecte pas elle-même les positions : les transitions sont
 * déclenchées par des méthodes explicites, appelées par les systèmes qui
 * possèdent l'information GPS (zone hantée, distance de capture). Elle gère en
 * revanche le délai du screamer, sa durée, le cooldown de 30 s et la pause
 * hors zone jouable.</p>
 *
 * <p>Les méthodes de transition renvoient {@code false} lorsque l'action est
 * refusée (mauvais état, cooldown actif ou session en pause) et laissent
 * l'état inchangé.</p>
 */
public final class GameSession {

    private static final long NO_DEADLINE = Long.MIN_VALUE;
    private static final double MILLIS_PER_SECOND = 1_000.0;

    private final GameConfig config;
    private final GameClock clock;
    private final RandomSource random;

    private GameState state = GameState.EXPLORATION;
    private boolean paused;
    private boolean playerInsidePlayArea = true;
    private boolean captureRangeReached;
    private long pausedAtMs;
    private long cooldownEndMs = NO_DEADLINE;
    private long screamerDelayDeadlineMs = NO_DEADLINE;
    private long screamerEndMs = NO_DEADLINE;

    public GameSession(GameConfig config, GameClock clock, RandomSource random) {
        if (config == null) {
            throw new IllegalArgumentException("config must not be null");
        }
        if (clock == null) {
            throw new IllegalArgumentException("clock must not be null");
        }
        if (random == null) {
            throw new IllegalArgumentException("random must not be null");
        }
        this.config = config;
        this.clock = clock;
        this.random = random;
    }

    public GameState getState() {
        return state;
    }

    public boolean isPaused() {
        return paused;
    }

    /**
     * Démarre un tracking depuis {@link GameState#EXPLORATION}.
     *
     * @return {@code false} si la session est en pause, déjà dans un autre
     *         état, ou si le cooldown de zone est encore actif
     */
    public boolean startTracking() {
        if (paused || state != GameState.EXPLORATION || getCooldownRemainingMs() > 0L) {
            return false;
        }
        state = GameState.TRACKING;
        captureRangeReached = false;
        return true;
    }

    /**
     * Signale que le fantôme est passé à portée de capture.
     *
     * <p>En V1, l'état reste {@link GameState#TRACKING} pendant le délai
     * aléatoire de 1 à 2 s, puis passe en {@link GameState#SCREAMER} pendant
     * 2 s avant {@link GameState#CAPTURE}. Si le screamer est désactivé, la
     * capture démarre immédiatement.</p>
     */
    public boolean onGhostInCaptureRange() {
        if (paused || state != GameState.TRACKING || captureRangeReached) {
            return false;
        }
        long nowMs = clock.nowMs();
        if (config.isScreamerEnabled()) {
            captureRangeReached = true;
            screamerDelayDeadlineMs = nowMs + randomScreamerDelayMs();
        } else {
            enterCapture(nowMs);
        }
        return true;
    }

    /**
     * Termine la capture avec succès : {@link GameState#CAPTURE} vers
     * {@link GameState#RECOMPENSE}.
     */
    public boolean completeCapture() {
        if (paused || state != GameState.CAPTURE) {
            return false;
        }
        state = GameState.RECOMPENSE;
        return true;
    }

    /**
     * Termine la capture par un échec : {@link GameState#CAPTURE} vers
     * {@link GameState#EXPLORATION}.
     */
    public boolean failCapture() {
        if (paused || state != GameState.CAPTURE) {
            return false;
        }
        state = GameState.EXPLORATION;
        return true;
    }

    /**
     * Ferme l'écran de récompense : {@link GameState#RECOMPENSE} vers
     * {@link GameState#EXPLORATION}.
     */
    public boolean acknowledgeReward() {
        if (paused || state != GameState.RECOMPENSE) {
            return false;
        }
        state = GameState.EXPLORATION;
        return true;
    }

    /**
     * Met à jour l'appartenance du joueur au polygone jouable.
     *
     * <p>Si {@code PLAYER_OUT_OF_AREA_PAUSE} est actif, sortir de la zone met
     * la session en pause et gèle tous les timers ; y revenir la reprend.</p>
     */
    public void setPlayerInsidePlayArea(boolean inside) {
        if (inside == playerInsidePlayArea) {
            return;
        }
        playerInsidePlayArea = inside;
        if (!config.isPlayerOutOfAreaPause()) {
            return;
        }
        if (inside) {
            resume();
        } else {
            pause();
        }
    }

    /**
     * Fait avancer les timers du screamer et du cooldown.
     *
     * <p>À appeler à chaque tick de 100 ms. Ne fait rien pendant une pause.</p>
     */
    public void update() {
        if (paused) {
            return;
        }
        long nowMs = clock.nowMs();

        if (state == GameState.TRACKING && captureRangeReached
                && nowMs >= screamerDelayDeadlineMs) {
            captureRangeReached = false;
            state = GameState.SCREAMER;
            cooldownEndMs = screamerDelayDeadlineMs + cooldownMs();
            screamerEndMs = screamerDelayDeadlineMs + screamerDurationMs();
            screamerDelayDeadlineMs = NO_DEADLINE;
        }

        if (state == GameState.SCREAMER && nowMs >= screamerEndMs) {
            state = GameState.CAPTURE;
            screamerEndMs = NO_DEADLINE;
        }
    }

    /**
     * Temps restant avant la fin du cooldown de zone, en millisecondes.
     *
     * <p>La valeur est gelée pendant une pause.</p>
     */
    public long getCooldownRemainingMs() {
        if (cooldownEndMs == NO_DEADLINE) {
            return 0L;
        }
        long nowMs = paused ? pausedAtMs : clock.nowMs();
        return Math.max(0L, cooldownEndMs - nowMs);
    }

    private void enterCapture(long nowMs) {
        state = GameState.CAPTURE;
        cooldownEndMs = nowMs + cooldownMs();
    }

    private void pause() {
        if (paused) {
            return;
        }
        paused = true;
        pausedAtMs = clock.nowMs();
    }

    private void resume() {
        if (!paused) {
            return;
        }
        long nowMs = clock.nowMs();
        long pausedDurationMs = nowMs - pausedAtMs;
        cooldownEndMs = shift(cooldownEndMs, pausedDurationMs);
        screamerDelayDeadlineMs = shift(screamerDelayDeadlineMs, pausedDurationMs);
        screamerEndMs = shift(screamerEndMs, pausedDurationMs);
        paused = false;
    }

    private static long shift(long deadlineMs, long deltaMs) {
        return deadlineMs == NO_DEADLINE ? NO_DEADLINE : deadlineMs + deltaMs;
    }

    private long randomScreamerDelayMs() {
        double minS = config.getScreamerDelayMinS();
        double maxS = config.getScreamerDelayMaxS();
        double seconds = minS + random.nextDouble() * (maxS - minS);
        return Math.round(seconds * MILLIS_PER_SECOND);
    }

    private long screamerDurationMs() {
        return Math.round(config.getScreamerDurationS() * MILLIS_PER_SECOND);
    }

    private long cooldownMs() {
        return Math.round(config.getZoneTriggerCooldownS() * MILLIS_PER_SECOND);
    }
}
