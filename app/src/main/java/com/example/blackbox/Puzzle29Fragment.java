package com.example.blackbox;

import android.animation.ValueAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Puzzle29Fragment extends PuzzleBaseFragment {

    private ImageView holdBox;
    private View bg;

    // Duration the user must hold to complete the puzzle
    private static final int THRESHOLD = 5000; // ms

    // Handler tied to main thread for delayed completion trigger
    private final Handler handler = new Handler(Looper.getMainLooper());

    private Runnable holdRunnable;
    private ValueAnimator colorAnimator;

    @Override
    protected int getTotalBoxes() {
        return 1;
    }

    @Override
    public int getPuzzleId() {
        return 29;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.activity_puzzle29, container, false);

        holdBox = root.findViewById(R.id.imageView0);
        bg = root.findViewById(R.id.bg);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.setOnTouchListener((v, event) -> {
            switch (event.getActionMasked()) {

                case MotionEvent.ACTION_DOWN:
                    startHold();
                    return true;

                case MotionEvent.ACTION_UP:
                    cancelHold();
                    resetBackground();

                    // Ensures accessibility + click listeners still work
                    v.performClick();
                    return true;

                case MotionEvent.ACTION_CANCEL:
                    // Happens when touch is interrupted (e.g., parent intercepts)
                    cancelHold();
                    resetBackground();
                    return true;
            }
            return false;
        });
    }

    private void startHold() {
        // Start visual feedback immediately
        startColorAnimation();

        // If user holds long enough, mark puzzle as completed
        holdRunnable = () -> updatePuzzle(holdBox);
        handler.postDelayed(holdRunnable, THRESHOLD);
    }

    private void cancelHold() {
        // Prevent completion if user releases early
        if (holdRunnable != null) {
            handler.removeCallbacks(holdRunnable);
        }

        stopColorAnimation();
    }

    private void startColorAnimation() {
        int startColor = Color.TRANSPARENT;
        int endColor = Color.parseColor("#9994467C");

        colorAnimator = ValueAnimator.ofArgb(startColor, endColor);
        colorAnimator.setDuration(THRESHOLD);

        // Gradually darken background while holding
        colorAnimator.addUpdateListener(animation ->
                bg.setBackgroundColor((int) animation.getAnimatedValue())
        );

        colorAnimator.start();
    }

    private void stopColorAnimation() {
        if (colorAnimator != null) {
            colorAnimator.cancel();
        }
    }

    private void resetBackground() {
        // Restore original background color when hold is cancelled
        bg.setBackgroundColor(
                requireContext().getResources().getColor(R.color.bg, null)
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Avoid leaks and orphan callbacks when view is destroyed
        cancelHold();
        stopColorAnimation();
    }
}