package com.example.blackbox;

import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.List;

public class Puzzle2Fragment extends PuzzleBaseFragment {

    private final ImageView[] boxes = new ImageView[2];
    int[] boxIds = {
            R.id.imageView0,
            R.id.imageView1,
    };

    private static final int LEFT = 0;
    private static final int RIGHT = 1;
    private static final int MAX_BRIGHTNESS = 255;
    private static final float LOW_THRESHOLD = 5f;
    private static final float HIGH_THRESHOLD = 250f;

    private ContentObserver brightnessObserver;
    private View root;
    private List<ImageView> rays;

    @Override
    public int getPuzzleId() {
        return 2;
    }

    @Override
    protected int getTotalBoxes() {
        return boxes.length;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.activity_puzzle2, container, false);
        for (int i = 0; i < boxes.length; i++) {
            boxes[i] = root.findViewById(boxIds[i]);
        }
        ViewGroup rayContainer = root.findViewById(R.id.ll);
        rays = MainActivity.getViewsByTag(rayContainer, "rays");
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Setup Coin Button
        setupCoinButton(requireActivity().getWindow().getDecorView().getRootView());

        // Re-apply progress
        for (int index : getCompletedThisRun()) {
            applyCurrentProgress(boxes[index]);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        brightnessObserver = new ContentObserver(new Handler(Looper.getMainLooper())) {
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
        updateBrightnessUI();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (brightnessObserver != null) {
            requireContext().getContentResolver().unregisterContentObserver(brightnessObserver);
        }
    }

    private void updateBrightnessUI() {
        int brightness = getScreenBrightness();
        if (brightness < 0) { return; }
        checkConditions(brightness);
        updateRays(brightness);
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

    private void checkConditions(int brightness) {
        if (brightness <= LOW_THRESHOLD) {
            updatePuzzle(boxes[LEFT], LEFT);
        }
        else if (brightness >= HIGH_THRESHOLD) {
            updatePuzzle(boxes[RIGHT], RIGHT);
        }
    }

    private void updateRays(int brightness) {
        int currentHeight = (int) ((float) brightness / MAX_BRIGHTNESS * 100); // Scale logic
        for (ImageView ray : rays) {
            ViewGroup.LayoutParams params = ray.getLayoutParams();
            if (params.height != currentHeight) {
                params.height = currentHeight;
                ray.setLayoutParams(params);
            }
        }
    }
}