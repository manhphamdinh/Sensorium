package com.example.blackbox;

import android.content.Context;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

public class Puzzle20Fragment extends PuzzleBaseFragment {

    private boolean screenWasOff = false;

    @Override
    public int getPuzzleId() {
        return 20;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_puzzle20, container, false);
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
            animation(0);
            screenWasOff = false;
        }
    }
}