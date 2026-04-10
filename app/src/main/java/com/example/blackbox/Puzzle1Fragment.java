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

import static java.lang.Math.acos;

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

    private static final double THRESHOLD = 9.7;
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

        // Find boxes
        for (int i = 0; i < boxes.length; i++) {
            boxes[i] = root.findViewById(boxIds[i]);
        }

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        for (int index : getCompletedThisRun()) {
            applyLoadedProgress(boxes[index]);
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

        ImageView fluid = getView().findViewById(R.id.fluid);
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        int deviceHeight = MainActivity.getDeviceHeightAndWidth(requireContext()).first;
        int deviceWidth = MainActivity.getDeviceHeightAndWidth(requireContext()).second;

        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) fluid.getLayoutParams();

        // z dao động từ -9.8 (úp) đến +9.8 (ngửa)
        // Khi ngửa (z = +9.8) → topMargin nhỏ → fluid tràn màn hình
        // Khi úp  (z = -9.8) → topMargin lớn → fluid bị đẩy xuống, biến mất
        float normalizedZ = z / 9.8f; // -1.0 đến +1.0
        // Khi normalizedZ = +1 → topMargin = 0        (fluid tràn đầy)
        // Khi normalizedz = -1 → topMargin = height*2 (fluid biến mất)
        params.topMargin = (int) (deviceHeight * (0.5 - 0.5 * normalizedZ));

        fluid.setLayoutParams(params);

        // Pivot và Rotation (giữ nguyên)
        fluid.setPivotX(deviceWidth / 2f + 5000);
        fluid.setPivotY(0);

        double angle = 0;
        if (z <= 1 && z >= -1) angle = Math.toDegrees(acos(Math.min(1.0, Math.max(-1.0, y / 9.8))));
        if (Double.isNaN(angle)) angle = y > 0 ? 0 : 180;
        if (x < 0) angle = -angle;

        fluid.setRotation((float) angle);

        // Kiểm tra hoàn thành puzzle
        if (x < -THRESHOLD) {
            updatePuzzle(boxes[RIGHT], RIGHT);
            Log.d("PUZZLE_1", "NEGATIVE X");
        }
        if (x > THRESHOLD) {
            updatePuzzle(boxes[LEFT], LEFT);
            Log.d("PUZZLE_1", "POSITIVE X");
        }
        if (y < -THRESHOLD) {
            updatePuzzle(boxes[TOP], TOP);
            Log.d("PUZZLE_1", "NEGATIVE Y");
        }
        if (y > THRESHOLD) {
            updatePuzzle(boxes[BOTTOM], BOTTOM);
            Log.d("PUZZLE_1", "POSITIVE Y");
        }
        if (z < -THRESHOLD) {
            updatePuzzle(boxes[MIDDLE_TOP], MIDDLE_TOP);
            Log.d("PUZZLE_1", "NEGATIVE Z");
        }
        if (z > THRESHOLD) {
            updatePuzzle(boxes[MIDDLE_BOTTOM], MIDDLE_BOTTOM);
            Log.d("PUZZLE_1", "POSITIVE Z");
        }

        getView().findViewById(R.id.ll).invalidate();
    }
}