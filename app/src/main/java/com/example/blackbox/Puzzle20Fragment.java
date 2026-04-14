package com.example.blackbox;

import android.content.Context;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;

public class Puzzle20Fragment extends PuzzleBaseFragment {

    private ImageView powerBox;
    private boolean screenWasOff = false;

    // SETUP
    @Override
    protected int getTotalBoxes() {
        return 1;
    }

    @Override
    public int getPuzzleId() {
        return 20;
    }

    // INITIALIZE
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.activity_puzzle20, container, false);
        powerBox = root.findViewById(R.id.imageView0);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupCoinButton(requireActivity().getWindow().getDecorView().getRootView());
        
        // Re-apply progress
        if (getCompletedThisRun().contains(0)) {
            applyCurrentProgress(powerBox);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        PowerManager powerManager = (PowerManager) requireContext().getSystemService(Context.POWER_SERVICE);

        // Check if screen is actually off
        if (powerManager != null && !powerManager.isInteractive()) {
            screenWasOff = true;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (screenWasOff) {
            updatePuzzle(powerBox);
            screenWasOff = false;
        }
    }
}