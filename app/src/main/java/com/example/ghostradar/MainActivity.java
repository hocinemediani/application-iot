package com.example.ghostradar;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;

import com.example.ghostradar.domain.config.GameConfig;
import com.example.ghostradar.domain.game.GameClock;
import com.example.ghostradar.domain.game.GameInput;
import com.example.ghostradar.domain.game.GameLoop;
import com.example.ghostradar.domain.game.GameSession;
import com.example.ghostradar.domain.game.GameState;
import com.example.ghostradar.domain.game.RandomSource;
import com.example.ghostradar.domain.sensors.SensorFrame;
import com.example.ghostradar.sensors.SensorError;
import com.example.ghostradar.sensors.SensorFrameListener;
import com.example.ghostradar.sensors.android.AndroidSensorFrameCollector;
import com.example.ghostradar.sensors.android.SensorPermissions;
import com.example.ghostradar.ui.CameraFilterStyle;
import com.example.ghostradar.ui.CameraGlitchController;
import com.example.ghostradar.ui.GlitchOverlayView;
import com.example.ghostradar.ui.HudClockFormatter;
import com.example.ghostradar.ui.HudStyle;
import com.example.ghostradar.ui.NoiseGaugeStyle;
import com.example.ghostradar.ui.NoiseGaugeView;
import com.example.ghostradar.ui.RecBlinkController;
import com.google.common.util.concurrent.ListenableFuture;

import java.time.ZoneId;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "GhostRadar";

    private final SensorFrameListener frameListener = new SensorFrameListener() {
        @Override
        public void onFrame(SensorFrame frame) {
            latestFrame = frame;
            updateNoiseGauge(frame);
            if (gameLoop != null) {
                gameLoop.onFrame(frame);
            }
        }

        @Override
        public void onUnavailable(SensorError error) {
            // Les indisponibilités seront affichées dans le HUD à l'étape suivante.
        }
    };

    private final GameLoop.Listener gameLoopListener = new GameLoop.Listener() {
        @Override
        public void onStateChanged(GameState previous, GameState current) {
            if (previous == GameState.EXPLORATION && current == GameState.TRACKING) {
                triggerSpawnGlitch();
            }
            if (previous != GameState.CAPTURE && current == GameState.CAPTURE) {
                triggerCaptureGlitch();
            }
        }
    };

    private AndroidSensorFrameCollector sensorCollector;
    private SensorFrame latestFrame = SensorFrame.EMPTY;
    private GameConfig gameConfig;
    private GameLoop gameLoop;
    private PreviewView cameraPreviewView;
    private View cameraFilterOverlay;
    private GlitchOverlayView glitchOverlayView;
    private ProcessCameraProvider cameraProvider;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private boolean cameraResumed;

    private View hudBar;
    private TextView recIndicator;
    private TextView clockText;
    private RecBlinkController recBlinkController;
    private long lastRecBlinkTickMs;
    private NoiseGaugeView noiseGaugeView;

    private final Handler hudHandler = new Handler(Looper.getMainLooper());
    private final Runnable recBlinkTick = new Runnable() {
        @Override
        public void run() {
            long now = SystemClock.uptimeMillis();
            recBlinkController.advance(now - lastRecBlinkTickMs);
            lastRecBlinkTickMs = now;
            updateRecIndicator();
            hudHandler.postDelayed(this, HudStyle.REC_BLINK_HALF_PERIOD_MS);
        }
    };
    private final Runnable clockTick = new Runnable() {
        @Override
        public void run() {
            updateClock();
            hudHandler.postDelayed(this, HudStyle.CLOCK_REFRESH_INTERVAL_MS);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_main);

        cameraPreviewView = findViewById(R.id.camera_preview);
        cameraFilterOverlay = findViewById(R.id.camera_filter_overlay);
        glitchOverlayView = findViewById(R.id.glitch_overlay);
        hudBar = findViewById(R.id.hud_bar);
        recIndicator = findViewById(R.id.hud_rec);
        clockText = findViewById(R.id.hud_clock);
        noiseGaugeView = findViewById(R.id.noise_gauge);
        recBlinkController = new RecBlinkController(HudStyle.REC_BLINK_HALF_PERIOD_MS);
        setupHudStyle();
        gameConfig = GameConfig.defaults();
        setupNoiseGauge();
        applyCameraFilter();
        CameraGlitchController glitchController = new CameraGlitchController(
                gameConfig, RandomSource.seeded(System.nanoTime()));
        glitchOverlayView.setController(glitchController);
        gameLoop = new GameLoop(
                new GameSession(gameConfig, GameClock.system(),
                        RandomSource.seeded(System.nanoTime())),
                new GameInput(),
                gameLoopListener);
        sensorCollector = new AndroidSensorFrameCollector(this, gameConfig);
        SensorPermissions.requestIfNeeded(this);

        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Log.e(TAG, "Initialisation CameraX interrompue", e);
            } catch (ExecutionException e) {
                Log.e(TAG, "Initialisation CameraX impossible", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void applyCameraFilter() {
        if (cameraFilterOverlay == null || gameConfig == null) {
            return;
        }
        cameraFilterOverlay.setBackgroundColor(CameraFilterStyle.argb(
                CameraFilterStyle.BLUE_IR_RGB,
                gameConfig.getCameraBlueFilterAlpha()));
    }

    private void setupHudStyle() {
        int margin = dpToPx(HudStyle.MARGIN_DP);
        ViewGroup.MarginLayoutParams params =
                (ViewGroup.MarginLayoutParams) hudBar.getLayoutParams();
        params.setMarginStart(margin);
        params.topMargin = margin;
        hudBar.setLayoutParams(params);
        hudBar.setBackgroundColor(HudStyle.BACKGROUND_ARGB);
        hudBar.setPadding(margin, margin, margin, margin);

        recIndicator.setText(HudStyle.REC_TEXT);
        recIndicator.setTextColor(HudStyle.REC_COLOR_ARGB);
        recIndicator.setTextSize(TypedValue.COMPLEX_UNIT_SP, HudStyle.TEXT_SIZE_SP);

        clockText.setTextColor(HudStyle.CLOCK_COLOR_ARGB);
        clockText.setTextSize(TypedValue.COMPLEX_UNIT_SP, HudStyle.TEXT_SIZE_SP);
        clockText.setPadding(margin, 0, 0, 0);
    }

    private void setupNoiseGauge() {
        if (noiseGaugeView == null || gameConfig == null) {
            return;
        }
        ViewGroup.MarginLayoutParams params =
                (ViewGroup.MarginLayoutParams) noiseGaugeView.getLayoutParams();
        params.setMarginEnd(dpToPx(NoiseGaugeStyle.RIGHT_MARGIN_DP));
        noiseGaugeView.setLayoutParams(params);
        noiseGaugeView.update(
                0.0,
                gameConfig.getNoiseGaugeMinDbfs(),
                gameConfig.getNoiseThresholdDbfs());
    }

    private void updateNoiseGauge(SensorFrame frame) {
        if (noiseGaugeView == null || sensorCollector == null || gameConfig == null
                || !frame.hasNoise()) {
            return;
        }
        noiseGaugeView.update(
                sensorCollector.getNoiseGaugeLevel(),
                sensorCollector.getSmoothedNoiseDbfs(),
                gameConfig.getNoiseThresholdDbfs());
    }

    private void startHud() {
        hudHandler.removeCallbacks(recBlinkTick);
        hudHandler.removeCallbacks(clockTick);
        recBlinkController.reset();
        updateRecIndicator();
        updateClock();
        lastRecBlinkTickMs = SystemClock.uptimeMillis();
        hudHandler.postDelayed(recBlinkTick, HudStyle.REC_BLINK_HALF_PERIOD_MS);
        hudHandler.postDelayed(clockTick, HudStyle.CLOCK_REFRESH_INTERVAL_MS);
    }

    private void stopHud() {
        hudHandler.removeCallbacks(recBlinkTick);
        hudHandler.removeCallbacks(clockTick);
        recBlinkController.reset();
        recIndicator.setVisibility(View.VISIBLE);
    }

    private void updateRecIndicator() {
        recIndicator.setVisibility(recBlinkController.isVisible() ? View.VISIBLE : View.INVISIBLE);
    }

    private void updateClock() {
        clockText.setText(HudClockFormatter.format(
                System.currentTimeMillis(), ZoneId.systemDefault()));
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private void bindCameraUseCases() {
        if (!cameraResumed || cameraProvider == null || cameraPreviewView == null
                || !SensorPermissions.hasCameraPermission(this)) {
            return;
        }
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(cameraPreviewView.getSurfaceProvider());
        try {
            cameraProvider.unbindAll();
            cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview);
        } catch (IllegalStateException | IllegalArgumentException | SecurityException e) {
            Log.w(TAG, "Caméra arrière indisponible", e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraResumed = true;
        if (gameLoop != null) {
            gameLoop.resume();
        }
        if (sensorCollector != null) {
            sensorCollector.start(frameListener);
        }
        startHud();
        bindCameraUseCases();
    }

    @Override
    protected void onPause() {
        cameraResumed = false;
        if (gameLoop != null) {
            gameLoop.pause();
        }
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
        if (sensorCollector != null) {
            sensorCollector.stop();
        }
        if (glitchOverlayView != null) {
            glitchOverlayView.cancelGlitch();
        }
        stopHud();
        super.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SensorPermissions.REQUEST_CODE) {
            // cameraResumed reflète le lifecycle de l'Activity : ne pas relancer
            // les capteurs en arrière-plan si une permission arrive après onPause.
            if (sensorCollector != null && cameraResumed) {
                // Relance les capteurs dont la permission vient d'être accordée.
                sensorCollector.stop();
                sensorCollector.start(frameListener);
            }
            if (SensorPermissions.hasCameraPermission(this)) {
                bindCameraUseCases();
            }
        }
    }

    public SensorFrame getLatestFrame() {
        return latestFrame;
    }

    public GameLoop getGameLoop() {
        return gameLoop;
    }

    /** Hook public appelé lors de l'apparition d'un fantôme. */
    public void triggerSpawnGlitch() {
        if (glitchOverlayView != null) {
            glitchOverlayView.triggerSpawn();
        }
    }

    /** Hook public appelé lors de la capture d'un fantôme. */
    public void triggerCaptureGlitch() {
        if (glitchOverlayView != null) {
            glitchOverlayView.triggerCapture();
        }
    }
}
