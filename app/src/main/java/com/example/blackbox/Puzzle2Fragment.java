package com.example.blackbox;

import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.view.ViewGroup.LayoutParams;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Puzzle2Fragment extends PuzzleBaseFragment {

    private static final int MAX_BRIGHTNESS = 255;
    private static final float LOW_THRESHOLD = 10f;
    private static final float HIGH_THRESHOLD = 245f;

    private ContentObserver brightnessObserver;

    @Override
    public int getPuzzleId() {
        return 2;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_puzzle2, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupCoinButton(requireActivity().getWindow().getDecorView().getRootView());
    }

    @Override
    public void onResume() {
        super.onResume();

        brightnessObserver = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                updateBrightnessUI();
            }
        };

        requireContext().getContentResolver().registerContentObserver(
                Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS),
                false,
                brightnessObserver
        );

        updateBrightnessUI(); // initial update
    }

    @Override
    public void onPause() {
        super.onPause();

        if (brightnessObserver != null) {
            requireContext().getContentResolver().unregisterContentObserver(brightnessObserver);
        }
    }

    private void updateBrightnessUI() {
        View root = getView();
        if (root == null) return;

        int brightness = getScreenBrightness();
        if (brightness < 0) return;

        handleTriggers(brightness);
        updateRays(root, brightness);
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

    private void handleTriggers(int brightness) {
        if (brightness <= LOW_THRESHOLD) {
            animation(0);
        } else if (brightness >= HIGH_THRESHOLD) {
            animation(1);
        }
    }

    private void updateRays(View root, int brightness) {
        ViewGroup container = root.findViewById(R.id.ll);

        float ratio = brightness / (float) MAX_BRIGHTNESS;
        int targetHeight = (int) (100 * ratio);

        for (ImageView ray : MainActivity.getViewsByTag(container, "rays")) {
            LayoutParams params = ray.getLayoutParams();

            // Avoid unnecessary layout updates
            if (params.height != targetHeight) {
                params.height = targetHeight;
                ray.setLayoutParams(params);
            }
        }
    }
}