package com.example.ghostradar.domain.game;

import com.example.ghostradar.domain.sensors.SensorFrame;

/**
 * Boucle de jeu visuelle, pure Java.
 *
 * <p>Chaque frame capteur fait avancer la {@link GameSession} et publie un
 * {@link GameSnapshot}. Les changements d'état, y compris ceux déclenchés
 * depuis l'extérieur entre deux frames (par exemple le début d'un tracking),
 * sont signalés au listener au prochain tick.</p>
 *
 * <p>La pause lifecycle est déléguée à {@link GameSession#pauseForLifecycle()}
 * et {@link GameSession#resumeFromLifecycle()} : la boucle ne calcule pas
 * elle-même de temps de jeu.</p>
 */
public final class GameLoop {

    /**
     * Notification d'une transition d'état entre deux frames.
     */
    public interface Listener {

        void onStateChanged(GameState previous, GameState current);
    }

    private final GameSession session;
    private final GameInput input;
    private final Listener listener;

    private SensorFrame latestFrame = SensorFrame.EMPTY;
    private GameState lastState;

    public GameLoop(GameSession session, GameInput input, Listener listener) {
        if (session == null) {
            throw new IllegalArgumentException("session must not be null");
        }
        if (input == null) {
            throw new IllegalArgumentException("input must not be null");
        }
        if (listener == null) {
            throw new IllegalArgumentException("listener must not be null");
        }
        this.session = session;
        this.input = input;
        this.listener = listener;
        this.lastState = session.getState();
    }

    /**
     * Consomme une frame capteur, avance la session et notifie les transitions.
     *
     * @param frame dernier agrégat de capteurs, jamais {@code null}
     */
    public void onFrame(SensorFrame frame) {
        if (frame == null) {
            throw new IllegalArgumentException("frame must not be null");
        }
        latestFrame = frame;
        session.update();
        GameState current = session.getState();
        if (current != lastState) {
            GameState previous = lastState;
            lastState = current;
            listener.onStateChanged(previous, current);
        }
    }

    public GameSnapshot getSnapshot() {
        return new GameSnapshot(session.getState(), input);
    }

    public SensorFrame getLatestFrame() {
        return latestFrame;
    }

    public GameSession getSession() {
        return session;
    }

    public GameInput getInput() {
        return input;
    }

    public boolean isPaused() {
        return session.isPaused();
    }

    /** Suspend la simulation quand l'application passe en arrière-plan. */
    public void pause() {
        session.pauseForLifecycle();
    }

    /** Reprend la simulation au retour au premier plan. */
    public void resume() {
        session.resumeFromLifecycle();
    }
}
