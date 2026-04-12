package com.example.blackbox;

import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;

import com.squareup.seismic.ShakeDetector;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Puzzle17Fragment extends PuzzleBaseFragment implements ShakeDetector.Listener {

    @Override
    public int getPuzzleId() { return 17; }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_puzzle17, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupCoinButton(requireActivity().getWindow().getDecorView().getRootView());
        view.findViewById(R.id.imageView0).startAnimation(
                AnimationUtils.loadAnimation(requireContext(), R.anim.wiggle)
        );
        SensorManager sm = (SensorManager) requireContext().getSystemService(requireContext().SENSOR_SERVICE);
        new ShakeDetector(this).start(sm);
    }

    @Override
    public void hearShake() {
        animation(0);
    }
}