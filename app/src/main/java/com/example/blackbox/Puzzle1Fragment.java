package com.example.blackbox;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
    private WindowManager windowManager;
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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        for (int index : getCompletedThisRun()) {
            applyCurrentProgress(boxes[index]);
        }
        setupCoinButton(requireActivity().getWindow().getDecorView().getRootView());
    }

    @Override
    public void onResume() {
        super.onResume();
        windowManager = (WindowManager) requireContext().getSystemService(Context.WINDOW_SERVICE);
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

        // Screen rotation
        int rotation = windowManager.getDefaultDisplay().getRotation();

        float[] adjusted = new float[2];
        adjustForRotation(gravityX, gravityY, rotation, adjusted);


        // UPDATE FLUID AND BUBBLES UI
        fluid.setGravity(adjusted[0], adjusted[1]);

        updateBubbles(maxGravity, gravityZ, bubbleTop, bubbleBottom);

        // Kiểm tra hoàn thành puzzle
        if (adjusted[0] < -THRESHOLD) {
            updatePuzzle(boxes[RIGHT], RIGHT);
            Log.d("PUZZLE 1", "NEGATIVE X");
        }
        if (adjusted[0] > THRESHOLD) {
            updatePuzzle(boxes[LEFT], LEFT);
        }
        if (adjusted[1] < -THRESHOLD) {
            updatePuzzle(boxes[TOP], TOP);
            Log.d("PUZZLE 1", "NEGATIVE Y");

        }
        if (adjusted[1] > THRESHOLD) {
            updatePuzzle(boxes[BOTTOM], BOTTOM);
        }
        if (gravityZ < -THRESHOLD) {
            updatePuzzle(boxes[MIDDLE_BOTTOM], MIDDLE_BOTTOM);
            Log.d("PUZZLE 1", "NEGATIVE Z");
        }
        if (gravityZ > THRESHOLD) {
            updatePuzzle(boxes[MIDDLE_TOP], MIDDLE_TOP);
        }

        getView().findViewById(R.id.ll).invalidate();
    }

    private void adjustForRotation(float x, float y, int rotation, float[] out) {
        switch (rotation) {
            case Surface.ROTATION_90:
                out[0] = -y;
                out[1] =  x;
                break;

            case Surface.ROTATION_180:
                out[0] = -x;
                out[1] = -y;
                break;

            case Surface.ROTATION_270:
                out[0] =  y;
                out[1] = -x;
                break;

            case Surface.ROTATION_0:
            default:
                out[0] = x;
                out[1] = y;
                break;
        }
    }

    private void updateBubbles(float maxGravity,float gravityZ, ImageView bubbleTop, ImageView bubbleBottom) {

        float normalizedGravityZ = gravityZ / maxGravity;

        View parent = (View) bubbleBottom.getParent();
        float parentHeight = parent.getHeight();

        // Off-screen positions
        float topStart = -bubbleTop.getHeight();
        float bottomStart = parentHeight + bubbleBottom.getHeight();

        // BUBBLE TARGETS (BOXES)
        float topBubbleTarget =
                boxes[MIDDLE_TOP].getY() + boxes[MIDDLE_TOP].getHeight() / 2f
                        - bubbleTop.getHeight() / 2f;

        float bottomBubbleTarget =
                boxes[MIDDLE_BOTTOM].getY() + boxes[MIDDLE_BOTTOM].getHeight() / 2f
                        - bubbleBottom.getHeight() / 2f;

        // Top bubble
        float topController = Math.max(0f, normalizedGravityZ);
        float topNewPositionY = topStart + (topBubbleTarget - topStart) * topController;

        // Bottom bubble
        float bottomController = Math.max(0f, -normalizedGravityZ);
        float bottomNewPositionY = bottomStart + (bottomBubbleTarget - bottomStart) * bottomController;

        bubbleTop.setY(topNewPositionY);
        bubbleBottom.setY(bottomNewPositionY);
    }
}
