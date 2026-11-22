package com.example.ghostradar.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;

/**
 * Overlay Canvas qui dessine les bandes parasites produites par
 * {@link CameraGlitchController}. La logique temporelle reste dans la classe pure.
 */
public final class GlitchOverlayView extends View {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Runnable tick = new Runnable() {
        @Override
        public void run() {
            tick();
        }
    };

    private CameraGlitchController controller;
    private long lastTickMs;

    public GlitchOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint.setColor(CameraGlitchStyle.bandArgb());
        setWillNotDraw(false);
    }

    public void setController(CameraGlitchController controller) {
        this.controller = controller;
        cancelGlitch();
    }

    /** Hook appelé lors de l'apparition d'un fantôme. */
    public void triggerSpawn() {
        if (controller != null && controller.triggerSpawn()) {
            startTicking();
        }
    }

    /** Hook appelé lors de la capture d'un fantôme. */
    public void triggerCapture() {
        if (controller != null && controller.triggerCapture()) {
            startTicking();
        }
    }

    /** Arrête immédiatement le glitch et libère les callbacks. */
    public void cancelGlitch() {
        removeCallbacks(tick);
        if (controller != null) {
            controller.stop();
        }
        invalidate();
    }

    private void startTicking() {
        removeCallbacks(tick);
        lastTickMs = SystemClock.uptimeMillis();
        invalidate();
        postDelayed(tick, CameraGlitchStyle.REFRESH_INTERVAL_MS);
    }

    private void tick() {
        if (controller == null || !controller.isActive()) {
            invalidate();
            return;
        }
        long now = SystemClock.uptimeMillis();
        controller.advance(now - lastTickMs);
        lastTickMs = now;
        invalidate();
        if (controller.isActive()) {
            postDelayed(tick, CameraGlitchStyle.REFRESH_INTERVAL_MS);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (controller == null || !controller.isActive()) {
            return;
        }
        int width = getWidth();
        int viewHeight = getHeight();
        for (GlitchBand band : controller.getBands()) {
            int height = band.getHeightPx();
            float available = Math.max(0, viewHeight - height);
            float top = available * (float) band.getTopRatio();
            canvas.drawRect(0f, top, (float) width, top + height, paint);
        }
    }
}
