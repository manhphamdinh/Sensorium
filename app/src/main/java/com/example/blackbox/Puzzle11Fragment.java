package com.example.blackbox;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.shredzone.commons.suncalc.MoonIllumination;

public class Puzzle11Fragment extends PuzzleBaseFragment {

    // BOXES ARRAY AND BOX IDS
    private final ImageView[] boxes = new ImageView[2];
    int[] boxIds = {
            R.id.imageView0,
            R.id.imageView1,
    };

    // BOX POSITIONS
    private static final int LEFT = 0;    // New moon box
    private static final int RIGHT = 1;   // Full moon Box

    // PHASE PARAMETERS
    private static final double FULL_MOON = 0.5;
    private static final double PHASE_TOLERANCE = 0.05;
    private static final int PHASE_CHECK_DELAY = 5000;  //ms

    // ================== DEBUG MODE =============
    private static final boolean DEBUG_MODE = false;
    private static final double DEBUG_PHASE_NORMALIZED = 0f; // 0 -> 1
    // ===========================================

    private View shadow;

    // Continuous update handler
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable moonRunnable = new Runnable() {
        @Override
        public void run() {
            updateMoon();
            handler.postDelayed(this, PHASE_CHECK_DELAY);
        }
    };

    // SETUP
    @Override
    protected int getTotalBoxes() {
        return boxes.length;
    }

    @Override
    public int getPuzzleId() {
        return 11;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.activity_puzzle11, container, false);

        // Initialize boxes
        for (int i = 0; i < boxes.length; i++) {
            boxes[i] = root.findViewById(boxIds[i]);
        }

        shadow = root.findViewById(R.id.moonShadow);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        for (int index : getCompletedThisRun()) {
            applyCurrentProgress(boxes[index]);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        handler.post(moonRunnable);
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(moonRunnable);
    }

    private void updateMoon() {
        MoonIllumination moon = MoonIllumination.compute().now().execute();

        double phaseNormalized = moon.getPhase() / 360.0;

        if (DEBUG_MODE)
        {
            phaseNormalized = DEBUG_PHASE_NORMALIZED;
        }

        Log.d("MOON", "Phase normalized: " + phaseNormalized);

        updateShadowPosition(phaseNormalized);
        checkPuzzleCompletion(phaseNormalized);
    }

    private void updateShadowPosition(double phaseNormalized) {
        if (shadow == null) return;

        shadow.post(() -> {
            float width = shadow.getWidth();

            float shift;

            if (phaseNormalized <= FULL_MOON) {
                // NEW → FULL (center → left)
                float t = (float)(phaseNormalized / 0.5); // 0 → 1
                shift = -t; // 0 → -1
            } else {
                // FULL → NEW (right → center)
                float t = (float)((phaseNormalized - 0.5) / 0.5); // 0 → 1
                shift = 1 - t; // 1 → 0
            }

            shadow.setTranslationX(shift * width);
            shadow.setAlpha(0.9f);
        });
    }

    private void checkPuzzleCompletion(double phaseNormalized) {

        // 🌑 New Moon (phase ≈ 0 or 100%)
        if (phaseNormalized <= PHASE_TOLERANCE || phaseNormalized >= (1 - PHASE_TOLERANCE)) {
            updatePuzzle(boxes[LEFT], LEFT);
        }

        // 🌕 Full Moon (phaseNormalized ≈ 50%)
        else if (phaseNormalized >= FULL_MOON - PHASE_TOLERANCE && phaseNormalized <= FULL_MOON + PHASE_TOLERANCE) {
            updatePuzzle(boxes[RIGHT], RIGHT);
        }
    }
}