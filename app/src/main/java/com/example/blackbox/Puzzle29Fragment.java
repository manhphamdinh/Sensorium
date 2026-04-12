package com.example.blackbox;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.util.Timer;
import java.util.TimerTask;

public class Puzzle29Fragment extends PuzzleBaseFragment {

    private static final int THRESHOLD = 5000;
    private Timer timer;

    @Override
    public int getPuzzleId() { return 29; }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_puzzle29, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupCoinButton(requireActivity().getWindow().getDecorView().getRootView());
        view.setOnTouchListener((v, event) -> {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN: {
                    timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            animation(0);
                        }
                    }, THRESHOLD);
                    final int reps = 50;
                    for (int i = 0; i < reps; i++) {
                        final int step = i;
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                if (getActivity() == null || getView() == null) return;
                                int r = 0x94 * step / reps;
                                int g = 0x46 * step / reps;
                                int b = 0x7C * step / reps;
                                String hex = String.format("#99%02X%02X%02X", r, g, b);
                                getActivity().runOnUiThread(() ->
                                        getView().findViewById(R.id.bg)
                                                .setBackgroundColor(Color.parseColor(hex))
                                );
                            }
                        }, (long) step * THRESHOLD / reps);
                    }
                    break;
                }
                case MotionEvent.ACTION_UP: {
                    if (getView() != null)
                        getView().findViewById(R.id.bg)
                                .setBackgroundColor(requireContext().getResources().getColor(R.color.bg, null));
                    if (timer != null) timer.cancel();
                    break;
                }
            }
            return true;
        });
    }
}