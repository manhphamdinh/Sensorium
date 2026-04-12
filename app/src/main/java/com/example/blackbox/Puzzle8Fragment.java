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

import androidx.core.content.ContextCompat;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Puzzle8Fragment extends PuzzleBaseFragment {

    private static final double LOW_THRESHOLD = 10.0;
    private static final double HIGH_THRESHOLD = 99.0;
    private static final double THRESHOLD_TOLERANCE = 0.05;
    private final double ballSize = 100.0;
    private BroadcastReceiver receiver;

    @Override
    public int getPuzzleId() { return 8; }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_puzzle8, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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
                double level = (rawLevel >= 0 && scale > 0) ? (rawLevel * 100.0) / scale : -1;

                if (level >= HIGH_THRESHOLD - THRESHOLD_TOLERANCE)
                {
                    animation(0);
                }

                if (isCharging)
                {
                    animation(1);
                }

                if (level <= LOW_THRESHOLD + THRESHOLD_TOLERANCE)
                {
                    animation(2);
                }

                ImageView imageView = new ImageView(context);
                ((ViewGroup) getView().findViewById(R.id.merge)).removeAllViews();
                int deviceHeight = MainActivity.getDeviceHeightAndWidth(context).first;
                for (int i = 0; i < (deviceHeight / ballSize - 1) * (level / 100.0); i++) {
                    recurse(imageView, i, 0, (int) level);
                }
            }
        };
        requireContext().registerReceiver(receiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    @Override
    public void onPause() {
        super.onPause();
        requireContext().unregisterReceiver(receiver);
    }

    private void recurse(ImageView imageView, int rowNumber, int columnNumber, int batteryLevel) {
        if (getView() == null) return;
        int deviceWidth = MainActivity.getDeviceHeightAndWidth(requireContext()).second;
        if (columnNumber < (deviceWidth / ballSize) - 1) {
            ImageView imageView2 = new ImageView(requireContext());
            imageView2.setId(View.generateViewId());
            imageView2.setImageResource(R.drawable.circle);

            int color = (batteryLevel > 25) ? R.color.puzzle8translucent : R.color.puzzle8b;
            imageView2.setColorFilter(ContextCompat.getColor(requireContext(), color));

            RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams((int) ballSize, (int) ballSize);
            params2.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            params2.bottomMargin = (int) (rowNumber * ballSize);

            if ((columnNumber % (int) ((deviceWidth / ballSize))) == 0) {
                params2.leftMargin = (int) ballSize / 2 * (rowNumber % 2);
            }

            if (columnNumber != 0) {
                params2.addRule(RelativeLayout.END_OF, imageView.getId());
            }
            imageView2.setLayoutParams(params2);
            ((RelativeLayout) getView().findViewById(R.id.merge)).addView(imageView2, 0);
            recurse(imageView2, rowNumber, columnNumber + 1, batteryLevel);
        }
    }
}