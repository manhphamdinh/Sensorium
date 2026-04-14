package com.example.blackbox;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Puzzle29Fragment extends PuzzleBaseFragment {

    private ImageView holdBox;
    private View backGround;

    // How long the user must hold (ms)
    private static final int HOLD_DURATION_MS = 3000;

    private ValueAnimator backGroundAnimator;
    private final int backgroundStartColor = Color.parseColor("#000000");
    private final int backGroundEndColor = Color.parseColor("#AA94467C");
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
        backGround = root.findViewById(R.id.bg);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupCoinButton(requireActivity().getWindow().getDecorView().getRootView());

        view.setOnTouchListener((v, event) -> {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    startHoldAnimation();

                    return true;

                case MotionEvent.ACTION_UP:
                    cancelHoldAnimation();
                    resetBackgroundColor();
                    v.performClick();

                    return true;

                case MotionEvent.ACTION_CANCEL:

                    cancelHoldAnimation();
                    resetBackgroundColor();

                    return true;
            }
            return false;
        });
    }

    // HOLD ANIMATIONS
    private void cancelHoldAnimation() {
        if (backGroundAnimator != null) {
            backGroundAnimator.cancel(); // prevents completion trigger
        }
    }

    private void startHoldAnimation() {
        cancelHoldAnimation();

        backGroundAnimator = ValueAnimator.ofArgb(backgroundStartColor, backGroundEndColor);
        backGroundAnimator.setDuration(HOLD_DURATION_MS);

        backGroundAnimator.addUpdateListener(animation ->
                backGround.setBackgroundColor((int) animation.getAnimatedValue())
        );

        // Animation can end by either cancel or complete it, this ensures that
        // Only by completing would the puzzle get updated
        backGroundAnimator.addListener(new AnimatorListenerAdapter() {
            private boolean wasCancelled = false;

            @Override
            public void onAnimationCancel(Animator animation) {
                wasCancelled = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!wasCancelled) {
                    updatePuzzle(holdBox);
                }
            }
        });

        backGroundAnimator.start();
    }

    private void resetBackgroundColor() {
        backGround.setBackgroundColor(backgroundStartColor);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        cancelHoldAnimation();
    }
}
