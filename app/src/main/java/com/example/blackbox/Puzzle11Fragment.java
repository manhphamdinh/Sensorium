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
    private static final double DEBUG_PHASE = 0.0;
    // ===========================================

    private View shadow;

    // Continuous update handler
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable moonRunnable = new Runnable() {
        @Override
        public void run() {
            updateMoon();
            handler.postDelayed(this, PHASE_CHECK_DELAY); // update every 2 seconds
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

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        for (int index : getCompletedThisRun()) {
            applyCurrentProgress(boxes[index]);
        }

        shadow = view.findViewById(R.id.moonShadow);
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

        double phase = moon.getPhase() / 360.0;
        double fraction = moon.getFraction();

        if (DEBUG_MODE)
        {
            phase = DEBUG_PHASE;
        }

        Log.d("MOON", "Phase normalized: " + phase);
        Log.d("MOON", "Fraction: " + fraction);

        renderMoon(phase);
        checkPuzzleCompletion(phase);
    }

    private void renderMoon(double phase) {
        if (shadow == null) return;

        shadow.post(() -> {
            float width = shadow.getWidth();

            // Horizontal shift (-1 → 1)
            float shift = (float) ((phase - 0.5f) * 2f);

            // Shadow size (avoid disappearing completely)
            float scale = Math.max(0.05f,
                    (float) Math.abs(Math.cos(phase * Math.PI)));

            shadow.setTranslationX(shift * width / 2f);

            // Lighting direction
            if (phase < 0.5) {
                shadow.setPivotX(0); // left side
            } else {
                shadow.setPivotX(width); // right side
            }

            shadow.setScaleX(scale);
            shadow.setAlpha(0.9f);
        });
    }

    private void checkPuzzleCompletion(double phase) {

        // 🌑 New Moon (phase ≈ 0 or 100%)
        if (phase <= PHASE_TOLERANCE || phase >= (1 - PHASE_TOLERANCE)) {
            updatePuzzle(boxes[LEFT], LEFT);
        }

        // 🌕 Full Moon (phase ≈ 50%)
        else if (phase >= FULL_MOON - PHASE_TOLERANCE && phase <= FULL_MOON + PHASE_TOLERANCE) {
            updatePuzzle(boxes[RIGHT], RIGHT);
        }
    }
}