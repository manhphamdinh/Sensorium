package com.example.blackbox;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.ArcShape;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Calendar;
import java.util.HashSet;

public class Puzzle7Fragment extends PuzzleBaseFragment {

    private ImageView halfDayBox;

    // Special halfDayBox indexes
    private static final int DAY_BOX_INDEX = 0;
    private static final int NIGHT_BOX_INDEX = 1;

    private static final double PERCENTAGE = 0.78;   // điều chỉnh độ dày cung
    private int RADIUS = 1100;

    private static final String PREF_CLOCK_AM = "prefClockAM";
    private static final String PREF_CLOCK_PM = "prefClockPM";

    // ======================== DEBUG MODE =========================
    /// 0 = OFF, 1 = ON
    private static final int DEBUG_MODE = 0;

    /// [0-23], -1: Ignore.
    private static final int DEBUG_HOUR = -1;

    /// 1: complete AM. Only works if DEBUG_HOUR is set in daytime.
    private static final int DEBUG_FULL_DAY = 0;

    /// 1: complete PM. Only works if DEBUG_HOUR is set in nighttime.
    private static final int DEBUG_FULL_NIGHT = 0;
    // =============================================================

    // SETUP
    @Override
    protected int getTotalBoxes() { return 2; }

    @Override
    public int getPuzzleId() { return 7; }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.activity_puzzle7, container, false);

        halfDayBox = root.findViewById(R.id.imageView0);

        return root;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupCoinButton(requireActivity().getWindow().getDecorView().getRootView());
        
        int currentHour = getCurrentHour();
        boolean isMorning = currentHour < 12;
        int boxIndex = isMorning ? DAY_BOX_INDEX : NIGHT_BOX_INDEX;

        checkAndResetClockIfNeeded();
        puzzleCompletedClock(boxIndex);

        ImageView fluid = view.findViewById(R.id.fluid);
        ViewGroup root = view.findViewById(R.id.ll);

        // ====================== DEBUG FULL DAY/NIGHT ======================
        if (DEBUG_MODE == 1) {
            SharedPreferences pref = requireContext().getSharedPreferences(getString(R.string.prefProgress), Context.MODE_PRIVATE);

            if (DEBUG_FULL_DAY == 1) {
                JSONArray arr = new JSONArray();
                for (int i = 0; i < 12; i++) arr.put(i);
                pref.edit().putString(PREF_CLOCK_AM, arr.toString()).apply();
            }

            if (DEBUG_FULL_NIGHT == 1) {
                JSONArray arr = new JSONArray();
                for (int i = 0; i < 12; i++) arr.put(i);
                pref.edit().putString(PREF_CLOCK_PM, arr.toString()).apply();
            }
        }
        // ================================================================

        // === Background theo buổi ===
        if (!isMorning) {
            fluid.setBackgroundColor(getResources().getColor(R.color.bg, null));   // nền đen than
        } else {
            fluid.setBackgroundColor(getResources().getColor(R.color.puzzle7translucent, null));
        }

        // Xóa arc cũ để tránh chồng chéo
        clearOldArcs(root);

        // Vẽ arc mới
        String prefKey = isMorning ? PREF_CLOCK_AM : PREF_CLOCK_PM;
        loadAndDrawArcs(root, prefKey);

        if (isPeriodCompleted(prefKey)) {
            updatePuzzle(halfDayBox, boxIndex);
        }
    }

    // Get current time
    private int getCurrentHour() {
        if (DEBUG_MODE == 1 && DEBUG_HOUR >= 0 && DEBUG_HOUR <= 23) {
            return DEBUG_HOUR;
        }
        return Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
    }

    private void clearOldArcs(ViewGroup root) {
        for (int i = root.getChildCount() - 1; i >= 0; i--) {
            View child = root.getChildAt(i);
            if ("clock_arc".equals(child.getTag())) {
                root.removeViewAt(i);
            }
        }
    }

    private void checkAndResetClockIfNeeded() {
        Context context = getContext();
        if (context == null) return;

        SharedPreferences pref = context.getSharedPreferences(getString(R.string.prefProgress), Context.MODE_PRIVATE);
        long lastReset = pref.getLong("last_clock_reset_7", 0);
        int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

        if (lastReset == 0 || currentHour == 0 || currentHour == 12) {
            pref.edit()
                    .remove(PREF_CLOCK_AM)
                    .remove(PREF_CLOCK_PM)
                    .putLong("last_clock_reset_7", System.currentTimeMillis())
                    .apply();
        }
    }

    private void puzzleCompletedClock(int boxIndex) {
        Context context = getContext();
        if (context == null) return;

        String prefKey = (boxIndex == 0) ? PREF_CLOCK_AM : PREF_CLOCK_PM;
        SharedPreferences pref = context.getSharedPreferences(getString(R.string.prefProgress), Context.MODE_PRIVATE);
        String existing = pref.getString(prefKey, "[]");

        try {
            JSONArray array = new JSONArray(existing);
            HashSet<Integer> set = new HashSet<>();
            for (int i = 0; i < array.length(); i++) set.add(array.getInt(i));

            int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY) % 12;
            set.add(hour);

            JSONArray newArray = new JSONArray();
            for (int h : set) newArray.put(h);

            pref.edit().putString(prefKey, newArray.toString()).apply();
        } catch (JSONException ignored) {}
    }

    private void loadAndDrawArcs(ViewGroup root, String prefKey) {
        SharedPreferences pref = requireContext().getSharedPreferences(getString(R.string.prefProgress), Context.MODE_PRIVATE);
        String data = pref.getString(prefKey, "[]");

        try {
            JSONArray array = new JSONArray(data);
            for (int i = 0; i < array.length(); i++) {
                arc(root, array.getInt(i) * 30);
            }
        } catch (JSONException ignored) {}
    }

    private boolean isPeriodCompleted(String prefKey) {
        SharedPreferences pref = requireContext().getSharedPreferences(getString(R.string.prefProgress), Context.MODE_PRIVATE);
        String data = pref.getString(prefKey, "[]");
        try {
            return new JSONArray(data).length() >= 12;
        } catch (JSONException e) {
            return false;
        }
    }

    // ====================== VẼ CUNG ĐỒNG HỒ ĐẸP ======================
    private void arc(ViewGroup root, int startDegree) {
        int screenW = MainActivity.getDeviceHeightAndWidth(requireContext()).second;
        int screenH = MainActivity.getDeviceHeightAndWidth(requireContext()).first;

        RADIUS = Math.min(RADIUS, Math.min(screenH - 300, screenW - 300));
        boolean isMorning = Calendar.getInstance().get(Calendar.HOUR_OF_DAY) < 12;

        // ====================== ARC NGOÀI ======================
        ShapeDrawable outerArc = new ShapeDrawable(new ArcShape(startDegree - 90, 30));

        if (isMorning) {
            outerArc.getPaint().setColor(getResources().getColor(R.color.bg, null));   // ← Arc ngoài màu đen khi sáng
        } else {
            outerArc.getPaint().setColor(getResources().getColor(R.color.puzzle7, null)); // chiều giữ màu puzzle7
        }
        // Arc ngoài - màu puzzle7 (đầy)
        outerArc.setIntrinsicWidth(RADIUS);
        outerArc.setIntrinsicHeight(RADIUS);

        ImageView outerView = new ImageView(requireContext());
        outerView.setImageDrawable(outerArc);
        outerView.setX((screenW - RADIUS) / 2f);
        outerView.setY((screenH - RADIUS) / 2f);
        outerView.setTag("clock_arc");
        root.addView(outerView);

        // Arc trong - màu đen (tạo độ dày và làm nổi cung)
        ShapeDrawable innerArc = new ShapeDrawable(new ArcShape(startDegree - 90, 30));

        if (isMorning) {
            // Buổi sáng: arc trong trùng màu nền (puzzle7translucent)
            innerArc.getPaint().setColor(getResources().getColor(R.color.puzzle7translucent, null));
        } else {
            // Buổi chiều: arc trong màu đen
            innerArc.getPaint().setColor(getResources().getColor(R.color.bg, null));
        }
        innerArc.setIntrinsicWidth((int) (RADIUS * PERCENTAGE));
        innerArc.setIntrinsicHeight((int) (RADIUS * PERCENTAGE));

        ImageView innerView = new ImageView(requireContext());
        innerView.setImageDrawable(innerArc);
        innerView.setX((float) (screenW - RADIUS * PERCENTAGE) / 2f);
        innerView.setY((float) (screenH - RADIUS * PERCENTAGE) / 2f);
        innerView.setTag("clock_arc");
        root.addView(innerView);

        // Đưa hình tròn giữa lên trên
        View center = root.findViewById(R.id.imageView0);
        if (center != null) {
            center.bringToFront();
        }
    }
}