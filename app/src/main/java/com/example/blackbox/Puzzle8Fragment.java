package com.example.blackbox;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class Puzzle8Fragment extends PuzzleBaseFragment {

    // BOXES ARRAY AND BOX IDS
    private final ImageView[] boxes = new ImageView[3];
    int[] boxIds = {
            R.id.imageView0,
            R.id.imageView1,
            R.id.imageView2,
    };

    // BOX POSITIONS
    private static final int TOP = 0;       // MAX BATTERY
    private static final int MIDDLE = 1;    // BATTERY CHARGING
    private static final int BOTTOM = 2;    // LOW BATTERY

    // BATTERY THRESHOLDS
    private static final double HIGH_THRESHOLD = 100.0;
    private static final double LOW_THRESHOLD = 17;

    private static final double LOW_BATTERY_UI_THRESHOLD = 17;
    private static final double THRESHOLD_TOLERANCE = 0.1;

    // UI
    private final double BALL_SIZE = 100.0;
    private final int BALL_COLOR_GREEN = R.color.puzzle8translucent;
    private final int BALL_COLOR_RED = R.color.puzzle8b;
    private final List<ImageView> balls = new ArrayList<>();
    private int maxRows;
    private int maxColumns;
    private BroadcastReceiver receiver;

    // SET CORE VALUES
    @Override
    protected int getTotalBoxes() { return boxes.length; }

    @Override
    public int getPuzzleId() { return 8; }

    // INITIALIZE
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.activity_puzzle8, container, false);

        // Find boxes
        for (int i = 0; i < boxes.length; i++) {
            boxes[i] = root.findViewById(boxIds[i]);
        }

        // BALLS
        initBalls(root);

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
        receiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (getView() == null) {
                    return;
                }

                int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                boolean isCharging = (status == BatteryManager.BATTERY_STATUS_CHARGING)
                        || (status == BatteryManager.BATTERY_STATUS_FULL);

                int rawLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

                // Convert battery level to percentage (0–100)
                double level = (rawLevel >= 0 && scale > 0) ? (rawLevel * 100.0) / scale : -1;

                checkBatteryConditions(level, isCharging);

                updateBalls(level); // Reflect battery level visually
            }
        };
        requireContext().registerReceiver(receiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    @Override
    public void onPause() {
        super.onPause();
        requireContext().unregisterReceiver(receiver);
    }

    // HANDLE BATTERY CONDITIONS
    private void checkBatteryConditions(double level, boolean isCharging) {
        if (level >= HIGH_THRESHOLD - THRESHOLD_TOLERANCE)
        {
            updatePuzzle(boxes[TOP], TOP);
        }
        else if (level <= LOW_THRESHOLD + THRESHOLD_TOLERANCE)
        {
            updatePuzzle(boxes[BOTTOM], BOTTOM);
        }

        if (isCharging) {
            updatePuzzle(boxes[MIDDLE], MIDDLE);
        }
    }

    // BALLS BALLS BALLS BALLS BALLS BALLS BALLS BALLS BALLS BALLS BALLS BALLS BALLS BALLS BALLS BALLS
    private void initBalls(View root) {
        RelativeLayout container = root.findViewById(R.id.ballsContainer);

        int deviceHeight = MainActivity.getDeviceHeightAndWidth(requireContext()).first;
        int deviceWidth = MainActivity.getDeviceHeightAndWidth(requireContext()).second;

        // Calculate grid capacity based on fixed ball size
        maxRows = (int) (deviceHeight / BALL_SIZE);
        maxColumns = (int) (deviceWidth / BALL_SIZE);

        int totalBalls = maxColumns * maxRows;

        for (int i = 0; i < totalBalls; i++) {
            ImageView ball = new ImageView(requireContext());
            ball.setImageResource(R.drawable.circle);

            RelativeLayout.LayoutParams params =
                    new RelativeLayout.LayoutParams((int) BALL_SIZE, (int) BALL_SIZE);

            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM); // Stack from bottom upward
            ball.setLayoutParams(params);

            ball.setVisibility(View.GONE); // Hidden until needed

            container.addView(ball);
            balls.add(ball);
        }
    }

    private void updateBalls(double level) {
        if (getView() == null) return;

        // Number of balls to show based on battery percentage
        int visibleCount = (int) (balls.size() * (level / 100.0));

        int currentColor = ContextCompat.getColor(
                requireContext(),
                (level > LOW_BATTERY_UI_THRESHOLD + THRESHOLD_TOLERANCE) ?
                        BALL_COLOR_GREEN : BALL_COLOR_RED
        );

        for (int i = 0; i < balls.size(); i++) {
            ImageView ball = balls.get(i);

            if (i < visibleCount) {
                ball.setVisibility(View.VISIBLE);

                int currentRow = i / maxColumns;

                if (currentRow >= maxRows) {
                    ball.setVisibility(View.GONE);
                    continue;
                }

                int column = i % maxColumns;

                // Offset every other row for a staggered layout
                int offset = (currentRow % 2 == 0) ? 0 : (int) (BALL_SIZE / 2);

                RelativeLayout.LayoutParams params =
                        (RelativeLayout.LayoutParams) ball.getLayoutParams();

                params.bottomMargin = (int) (currentRow * BALL_SIZE); // Vertical stacking
                params.leftMargin = (int) (column * BALL_SIZE + offset); // Horizontal position

                ball.setLayoutParams(params);
                ball.setColorFilter(currentColor);

            } else {
                ball.setVisibility(View.GONE);
            }
        }
    }
}
