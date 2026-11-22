package com.example.ghostradar;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ghostradar.domain.config.GameConfig;
import com.example.ghostradar.domain.sensors.SensorFrame;
import com.example.ghostradar.sensors.SensorError;
import com.example.ghostradar.sensors.SensorFrameListener;
import com.example.ghostradar.sensors.android.AndroidSensorFrameCollector;
import com.example.ghostradar.sensors.android.SensorPermissions;

public class MainActivity extends AppCompatActivity {

    private final SensorFrameListener frameListener = new SensorFrameListener() {
        @Override
        public void onFrame(SensorFrame frame) {
            latestFrame = frame;
        }

        @Override
        public void onUnavailable(SensorError error) {
            // Les indisponibilités seront affichées dans le HUD à l'étape caméra.
        }
    };

    private AndroidSensorFrameCollector sensorCollector;
    private SensorFrame latestFrame = SensorFrame.EMPTY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_main);
        sensorCollector = new AndroidSensorFrameCollector(this, GameConfig.defaults());
        SensorPermissions.requestIfNeeded(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sensorCollector != null) {
            sensorCollector.start(frameListener);
        }
    }

    @Override
    protected void onPause() {
        if (sensorCollector != null) {
            sensorCollector.stop();
        }
        super.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SensorPermissions.REQUEST_CODE && sensorCollector != null) {
            // Relance les capteurs dont la permission vient d'être accordée.
            sensorCollector.stop();
            sensorCollector.start(frameListener);
        }
    }

    public SensorFrame getLatestFrame() {
        return latestFrame;
    }
}