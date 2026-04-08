package com.example.blackbox;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class Puzzle2Fragment extends PuzzleBaseFragment implements SensorEventListener {

    private static final double TOLERANCE = 10.1;
    private static final int MAX_BRIGHTNESS = 255;

    private SensorManager sensorManager;

    @Override
    public int getPuzzleId() {
        return 2;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_puzzle2, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();

        Context context = requireContext();
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        if (sensorManager != null) {
            Sensor gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
            if (gravitySensor != null) {
                sensorManager.registerListener(this, gravitySensor, SensorManager.SENSOR_DELAY_UI);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        View root = getView();
        if (root == null) return;

        int brightness = getScreenBrightness();
        if (brightness == -1) return;

        handleBrightnessTriggers(brightness);
        updateRayHeights(root, brightness);
    }

    private int getScreenBrightness() {
        try {
            return Settings.System.getInt(
                    requireContext().getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS
            );
        } catch (Settings.SettingNotFoundException e) {
            return -1;
        }
    }

    private void handleBrightnessTriggers(int brightness) {
        if (brightness < TOLERANCE) {
            animation(0);
        } else if (brightness > (MAX_BRIGHTNESS - TOLERANCE)) {
            animation(1);
        }
    }

    private void updateRayHeights(View root, int brightness) {
        ViewGroup container = root.findViewById(R.id.ll);

        for (ImageView ray : MainActivity.getViewsByTag(container, "rays")) {
            ViewGroup.LayoutParams params = ray.getLayoutParams();
            params.height = (int) (100 * (brightness / (double) MAX_BRIGHTNESS));
            ray.setLayoutParams(params);
        }
    }
}