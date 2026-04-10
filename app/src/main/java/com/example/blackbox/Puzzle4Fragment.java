package com.example.blackbox;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;

public class Puzzle4Fragment extends PuzzleBaseFragment implements SensorEventListener {

    private ImageView proximityBox;
    private static final double THRESHOLD = 4.0;
    private SensorManager mSensorManager;
    private Animation mAnimation;

    @Override
    protected int getTotalBoxes() { return 1; }

    @Override
    public int getPuzzleId() { return 4; }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.activity_puzzle4, container, false);

        proximityBox = root.findViewById(R.id.imageView0);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        mSensorManager = (SensorManager) requireContext().getSystemService(Context.SENSOR_SERVICE);
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY),
                SensorManager.SENSOR_DELAY_UI);

        mAnimation = new AlphaAnimation(1, 0);
        mAnimation.setDuration(2000);
        mAnimation.setInterpolator(new AccelerateInterpolator());
        mAnimation.setRepeatCount(Animation.INFINITE);
        mAnimation.setRepeatMode(Animation.REVERSE);
        if (getView() != null)
            getView().findViewById(R.id.proximityCircle).startAnimation(mAnimation);
    }

    @Override
    public void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (getView() == null) return;
        ImageView proximityCircle = getView().findViewById(R.id.proximityCircle);
        if (event.values[0] < THRESHOLD) {
            proximityCircle.clearAnimation();
            proximityCircle.setVisibility(View.INVISIBLE);
            updatePuzzle(proximityBox);
        } else {
            proximityCircle.startAnimation(mAnimation);
            proximityCircle.setVisibility(View.VISIBLE);
        }
    }
}