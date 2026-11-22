package com.example.ghostradar.sensors;

import com.example.ghostradar.domain.sensors.SensorFrame;

/**
 * Récepteur des frames agrégés publiés au tick de jeu.
 */
public interface SensorFrameListener {

    void onFrame(SensorFrame frame);

    void onUnavailable(SensorError error);
}
