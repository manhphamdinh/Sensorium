package com.example.blackbox;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;

public class Puzzle1Fragment extends PuzzleBaseFragment implements SensorEventListener {



    // BOXES ARRAY AND BOX IDS
    private final ImageView[] boxes = new ImageView[6];
    int[] boxIds = {
            R.id.imageView0,
            R.id.imageView1,
            R.id.imageView2,
            R.id.imageView3,
            R.id.imageView4,
            R.id.imageView5
    };

    // BOX POSITIONS
    //   2
    //   4
    // 1   0
    //   5
    //   3
    private static final int RIGHT = 0;
    private static final int LEFT = 1;
    private static final int TOP = 2;
    private static final int BOTTOM = 3;
    private static final int MIDDLE_TOP = 4;
    private static final int MIDDLE_BOTTOM = 5;

    // FLUID PARAMETERS
    private static final double THRESHOLD = 9.7;

    // CACHE
    private FluidView fluid;
    private ImageView bubbleTop;
    private ImageView bubbleBottom;

    private SensorManager mSensorManager;

    @Override
    protected int getTotalBoxes() {
        return boxes.length;
    }

    @Override
    public int getPuzzleId() { return 1; }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.activity_puzzle1, container, false);

        // CACHE
        for (int i = 0; i < boxes.length; i++) {
            boxes[i] = root.findViewById(boxIds[i]);
        }

        fluid = root.findViewById(R.id.fluid);
        bubbleTop = root.findViewById(R.id.bubbleTop);
        bubbleBottom = root.findViewById(R.id.bubbleBottom);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        for (int index : getCompletedThisRun()) {
            applyCurrentProgress(boxes[index]);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        mSensorManager = (SensorManager) requireContext().getSystemService(Context.SENSOR_SERVICE);
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY),
                SensorManager.SENSOR_DELAY_UI);
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

        float gravityX = event.values[0];
        float gravityY = event.values[1];
        float gravityZ = event.values[2];

        float maxGravity = (float) Math.sqrt(
                gravityX * gravityX +
                gravityY * gravityY +
                gravityZ * gravityZ
        );

        // UPDATE FLUID UI
        fluid.setGravity(gravityX, gravityY);

        updateBubbles(maxGravity, gravityZ, bubbleTop, bubbleBottom);

        // Kiểm tra hoàn thành puzzle
        if (gravityX < -THRESHOLD) {
            updatePuzzle(boxes[RIGHT], RIGHT);
            Log.d("PUZZLE_1", "NEGATIVE X");
        }
        if (gravityX > THRESHOLD) {
            updatePuzzle(boxes[LEFT], LEFT);
            Log.d("PUZZLE_1", "POSITIVE X");
        }
        if (gravityY < -THRESHOLD) {
            updatePuzzle(boxes[TOP], TOP);
            Log.d("PUZZLE_1", "NEGATIVE Y");
        }
        if (gravityY > THRESHOLD) {
            updatePuzzle(boxes[BOTTOM], BOTTOM);
            Log.d("PUZZLE_1", "POSITIVE Y");
        }
        if (gravityZ < -THRESHOLD) {
            updatePuzzle(boxes[MIDDLE_TOP], MIDDLE_TOP);
            Log.d("PUZZLE_1", "NEGATIVE Z");
        }
        if (gravityZ > THRESHOLD) {
            updatePuzzle(boxes[MIDDLE_BOTTOM], MIDDLE_BOTTOM);
            Log.d("PUZZLE_1", "POSITIVE Z");
        }

        getView().findViewById(R.id.ll).invalidate();
    }


    private void updateBubbles(float maxGravity,float gravityZ, ImageView bubbleTop, ImageView bubbleBottom) {

        float normalizedGravityZ = gravityZ / maxGravity;

        View parent = (View) bubbleBottom.getParent();
        float parentHeight = parent.getHeight();

        // Off-screen positions
        float topStart = -bubbleTop.getHeight();
        float bottomStart = parentHeight + bubbleBottom.getHeight();

        // TARGETS
        float topBubbleTarget =
                boxes[MIDDLE_BOTTOM].getY() + boxes[MIDDLE_BOTTOM].getHeight() / 2f
                        - bubbleTop.getHeight() / 2f;

        float bottomBubbleTarget =
                boxes[MIDDLE_TOP].getY() + boxes[MIDDLE_TOP].getHeight() / 2f
                        - bubbleBottom.getHeight() / 2f;

        // --- Top bubble (positive Z → goes to imageView5)
        float topProgress = Math.max(0f, normalizedGravityZ);
        float topY = topStart + (topBubbleTarget - topStart) * topProgress;

        // --- Bottom bubble (negative Z → goes to imageView4)
        float bottomProgress = Math.max(0f, -normalizedGravityZ);
        float bottomY = bottomStart + (bottomBubbleTarget - bottomStart) * bottomProgress;

        bubbleTop.setY(topY);
        bubbleBottom.setY(bottomY);
    }
}