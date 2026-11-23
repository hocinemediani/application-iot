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
 * <p>Sémantique du cooldown (Q3, SPEC.md §2) : il démarre à la <em>fin du
 * TRACKING</em>, c'est-à-dire à l'heure réelle de la transition vers
 * {@link GameState#SCREAMER} (ou vers {@link GameState#CAPTURE} si le screamer
 * est désactivé), mesurée avec {@link GameClock#nowMs()}. Il n'est pas
 * redémarré à la fin de la capture : comme une capture peut durer jusqu'à
 * 30 s, il peut être déjà écoulé quand le joueur termine le combat ou ferme
 * la récompense, et un nouveau tracking est alors immédiatement autorisé.</p>
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
    private boolean lifecyclePaused;
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
        return paused || lifecyclePaused;
    }

    /**
     * Gèle la session lorsque l'application passe en arrière-plan.
     *
     * <p>La pause lifecycle est indépendante de la pause hors zone : les deux
     * peuvent se chevaucher, et les timers ne sont décalés qu'à la reprise
     * effective de la session.</p>
     */
    public void pauseForLifecycle() {
        if (lifecyclePaused) {
            return;
        }
        lifecyclePaused = true;
        if (!paused) {
            pausedAtMs = clock.nowMs();
        }
    }

    /**
     * Reprend la session au retour au premier plan.
     *
     * <p>Si la session reste en pause pour une autre raison, par exemple une
     * sortie de zone, les timers restent gelés jusqu'à la reprise complète.</p>
     */
    public void resumeFromLifecycle() {
        if (!lifecyclePaused) {
            return;
        }
        lifecyclePaused = false;
        if (!paused) {
            shiftDeadlines(clock.nowMs() - pausedAtMs);
        }
    }

    /**
     * Démarre un tracking depuis {@link GameState#EXPLORATION}.
     *
     * @return {@code false} si la session est en pause, déjà dans un autre
     *         état, ou si le cooldown de zone est encore actif
     */
    public boolean startTracking() {
        if (isPaused() || state != GameState.EXPLORATION || getCooldownRemainingMs() > 0L) {
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
        if (isPaused() || state != GameState.TRACKING || captureRangeReached) {
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
        if (isPaused() || state != GameState.CAPTURE) {
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
        if (isPaused() || state != GameState.CAPTURE) {
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
        if (isPaused() || state != GameState.RECOMPENSE) {
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
        if (isPaused()) {
            return;
        }
        long nowMs = clock.nowMs();

        if (state == GameState.TRACKING && captureRangeReached
                && nowMs >= screamerDelayDeadlineMs) {
            captureRangeReached = false;
            state = GameState.SCREAMER;
            // Ancrage sur l'heure réelle de transition (Q3) : un tick tardif
            // ne doit pas entamer le cooldown de 30 s.
            cooldownEndMs = nowMs + cooldownMs();
            // La fin du screamer reste ancrée sur son échéance théorique :
            // seul le cooldown est concerné par l'arbitrage Q3.
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
        long nowMs = isPaused() ? pausedAtMs : clock.nowMs();
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
        if (!lifecyclePaused) {
            pausedAtMs = clock.nowMs();
        }
    }

    private void resume() {
        if (!paused) {
            return;
        }
        paused = false;
        if (!lifecyclePaused) {
            shiftDeadlines(clock.nowMs() - pausedAtMs);
        }
    }

    private void shiftDeadlines(long pausedDurationMs) {
        cooldownEndMs = shift(cooldownEndMs, pausedDurationMs);
        screamerDelayDeadlineMs = shift(screamerDelayDeadlineMs, pausedDurationMs);
        screamerEndMs = shift(screamerEndMs, pausedDurationMs);
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
