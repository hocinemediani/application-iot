package com.example.ghostradar.sensors;

/**
 * Implémentation pure du cycle de vie commun aux capteurs.
 *
 * <p>Les implémentations Android n'ont qu'à fournir {@link #onStart()} et
 * {@link #onStop()}, puis appeler {@link #emit(Object)} et
 * {@link #emitUnavailable(SensorError)} depuis leurs callbacks.</p>
 *
 * @param <T> type d'échantillon du capteur
 */
public abstract class BaseSensorSource<T> implements SensorSource<T> {

    private SensorListener<T> listener;
    private boolean running;

    @Override
    public final void start(SensorListener<T> listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener must not be null");
        }
        if (running) {
            this.listener = listener;
            return;
        }

        this.listener = listener;
        SensorError error;
        try {
            error = onStart();
        } catch (RuntimeException exception) {
            error = SensorError.UNKNOWN;
        }
        if (error != null) {
            this.listener = null;
            listener.onUnavailable(error);
            return;
        }
        running = true;
    }

    @Override
    public final void stop() {
        if (!running) {
            return;
        }
        running = false;
        try {
            onStop();
        } finally {
            listener = null;
        }
    }

    @Override
    public final boolean isRunning() {
        return running;
    }

    /**
     * Démarre la source physique. Retourne {@code null} en cas de succès, ou
     * l'erreur à signaler au listener.
     */
    protected abstract SensorError onStart();

    /**
     * Arrête la source physique. Appelé une seule fois par cycle de vie.
     */
    protected abstract void onStop();

    protected final void emit(T sample) {
        if (sample == null) {
            throw new IllegalArgumentException("sample must not be null");
        }
        if (running && listener != null) {
            listener.onSample(sample);
        }
    }

    protected final void emitUnavailable(SensorError error) {
        if (error == null) {
            throw new IllegalArgumentException("error must not be null");
        }
        if (listener != null) {
            listener.onUnavailable(error);
        }
    }
}
