package com.example.blackbox;

import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.squareup.seismic.ShakeDetector;

public class Puzzle17Fragment extends PuzzleBaseFragment implements ShakeDetector.Listener {

    private ImageView shakeBox;

    @Override
    protected int getTotalBoxes() {
        return 1;
    }

    @Override
    public int getPuzzleId() { return 17; }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.activity_puzzle17, container, false);

        shakeBox = root.findViewById(R.id.imageView0);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        shakeBox.startAnimation(
                AnimationUtils.loadAnimation(requireContext(), R.anim.wiggle)
        );
        SensorManager sm = (SensorManager) requireContext().getSystemService(requireContext().SENSOR_SERVICE);
        new ShakeDetector(this).start(sm);
    }

    @Override
    public void hearShake() {
        updatePuzzle(shakeBox);
    }
}