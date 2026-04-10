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

import java.util.List;

public class Puzzle2Fragment extends PuzzleBaseFragment {

    // BOXES ARRAY AND BOX IDS
    private final ImageView[] boxes = new ImageView[2];
    int[] boxIds = {
            R.id.imageView0,
            R.id.imageView1,
    };

    // BOX POSITIONS (used as indices)
    private static final int LEFT = 0;    // Dark box (low brightness trigger)
    private static final int RIGHT = 1;   // Light box (high brightness trigger)

    // BRIGHTNESS CONSTANTS
    private static final int MAX_BRIGHTNESS = 255;
    private static final float LOW_THRESHOLD = 5f;     // Near minimum brightness
    private static final float HIGH_THRESHOLD = 250f; // Near maximum brightness

    private ContentObserver brightnessObserver;

    // CACHE
    private View root;
    private List<ImageView> rays; // Visual rays that scale with brightness

    @Override
    public int getPuzzleId() {
        return 2;
    }

    @Override
    protected int getTotalBoxes() {
        return boxes.length;
    }

    // INITIALIZE VIEW + CACHE REFERENCES
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.activity_puzzle2, container, false);

        // Initialize box references
        for (int i = 0; i < boxes.length; i++) {
            boxes[i] = root.findViewById(boxIds[i]);
        }

        // Cache all "ray" views for later resizing
        ViewGroup rayContainer = root.findViewById(R.id.ll);
        rays = MainActivity.getViewsByTag(rayContainer, "rays");

        return root;
    }

    // LOAD PREVIOUS PROGRESS (visual only, no logic triggers)
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Re-apply completed boxes from this run
        for (int index : getCompletedThisRun()) {
            applyLoadedProgress(boxes[index]);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Observe system brightness changes in real-time
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

        updateBrightnessUI(); // Initial sync with current brightness
    }

    @Override
    public void onPause() {
        super.onPause();

        // Prevent leaks by unregistering observer
        if (brightnessObserver != null) {
            requireContext().getContentResolver().unregisterContentObserver(brightnessObserver);
        }
    }

    // MAIN UPDATE: handles both puzzle logic + UI visuals
    private void updateBrightnessUI() {
        if (root == null) return;

        int brightness = getScreenBrightness();
        if (brightness < 0) return; // Failed to read brightness

        handleTriggers(brightness); // Check puzzle conditions
        updateRays(brightness);     // Update visual feedback
    }

    // SAFE BRIGHTNESS READ (returns -1 if unavailable)
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

    // TRIGGER PUZZLE COMPLETION BASED ON THRESHOLDS
    private void handleTriggers(int brightness) {
        if (brightness <= LOW_THRESHOLD) {
            updatePuzzle(boxes[LEFT], LEFT);
        }
        else if (brightness >= HIGH_THRESHOLD) {
            updatePuzzle(boxes[RIGHT], RIGHT);
        }
    }

    // VISUAL FEEDBACK: scale ray height proportionally to brightness
    private void updateRays(int brightness) {
        float ratio = brightness / (float) MAX_BRIGHTNESS;
        int targetHeight = (int) (100 * ratio); // Max height = 100px

        for (ImageView ray : rays) {
            ViewGroup.LayoutParams params = ray.getLayoutParams();

            // Avoid redundant layout passes (performance optimization)
            if (params.height != targetHeight) {
                params.height = targetHeight;
                ray.setLayoutParams(params);
            }
        }
    }
}