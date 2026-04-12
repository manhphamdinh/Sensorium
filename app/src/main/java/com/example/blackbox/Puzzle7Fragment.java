//package com.example.blackbox;
//
//import android.app.Activity;
//import android.content.Context;
//import android.content.SharedPreferences;
//import android.graphics.drawable.ShapeDrawable;
//import android.graphics.drawable.shapes.ArcShape;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//
//import org.json.JSONArray;
//import org.json.JSONException;
//
//import java.util.Arrays;
//import java.util.Calendar;
//import java.util.Collections;
//import java.util.HashSet;
//
//public class Puzzle7Fragment extends PuzzleBaseFragment {
//
//    private static final String TAG = "Puzzle7Debug";
//    private static final double PERCENTAGE = .75;
//    private int RADIUS = 1200;
//
//    private boolean isMorning;
//
//    private static final String PREF_CLOCK_AM = "prefClockAM";
//    private static final String PREF_CLOCK_PM = "prefClockPM";
//
//    ImageView imageView;
//
//    private void saveBoxCompleted(int boxIndex) {
//        Context context = getContext();
//        if (context == null) return;
//        String key = getPuzzleId() + ":" + boxIndex;
//        SharedPreferences pref = context.getSharedPreferences(
//                getString(R.string.pref), Context.MODE_PRIVATE
//        );
//        String existing = pref.getString(getString(R.string.prefSolved), "[]");
//        try {
//            JSONArray jsonArray = new JSONArray(existing);
//            for (int i = 0; i < jsonArray.length(); i++) {
//                if (jsonArray.getString(i).equals(key)) return;
//            }
//            jsonArray.put(key);
//            pref.edit().putString(getString(R.string.prefSolved), jsonArray.toString()).apply();
//        } catch (JSONException ignored) {}
//    }
//
//    @Override
//    protected void animation(int index) {
//        Activity activity = getActivity();
//        if (activity == null) return;
//
//        // Puzzle7 chỉ có imageView0, dùng nó cho cả AM và PM
//        activity.runOnUiThread(() -> {
//            ImageView iv = activity.findViewById(R.id.imageView0);
//            if (iv == null) {
//                Log.d(TAG, "imageView0 is NULL!");
//                return;
//            }
//            iv.setBackgroundResource(R.drawable.animation);
//            android.graphics.drawable.AnimationDrawable anim =
//                    (android.graphics.drawable.AnimationDrawable) iv.getBackground();
//            anim.start();
//            Log.d(TAG, "Animation started on imageView0");
//            saveBoxCompleted(index);
//        });
//    }
//
//    @Override
//    public int getPuzzleId() { return 7; }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        return inflater.inflate(R.layout.activity_puzzle7, container, false);
//    }
//
//    @Override
//    public void onViewCreated(View view, Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//
//        int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
////        boolean isMorning = currentHour < 12;
//        isMorning = currentHour < 12;
//        int boxIndex = isMorning ? 0 : 1;
//
//        Log.d(TAG, "onViewCreated - Giờ hiện tại: " + currentHour + " | Box: 7:" + boxIndex);
//
//        checkAndResetClockIfNeeded();
//
//        puzzleCompletedClock(boxIndex);
//
//        String currentKey = isMorning ? PREF_CLOCK_AM : PREF_CLOCK_PM;
//
//        ImageView imageview5 = imageView.findViewById(R.id.imageView5);
//
//        if (isMorning) {
//            return;
//        } else {
//            imageview5.setBackgroundColor(getResources().getColor(R.color.bg, null));
//        }
//
//        loadAndDrawArcs(view, currentKey);
//
//        // Kiểm tra và gọi animation
//        if (isPeriodCompleted(currentKey)) {
//            Log.d(TAG, "=== ĐỦ 12 MÚI - GỌI ANIMATION(" + boxIndex + ") ===");
//            animation(boxIndex);           // Phải gọi dòng này mới lưu prefSolved
//        } else {
//            Log.d(TAG, "Chưa đủ 12 múi, hiện có: " + getCurrentCount(currentKey) + "/12");
//        }
//    }
//
//    private void checkAndResetClockIfNeeded() {
//        Context context = getContext();
//        if (context == null) return;
//
//        SharedPreferences pref = context.getSharedPreferences(getString(R.string.pref), Context.MODE_PRIVATE);
//        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
//
//        if (pref.getLong("last_clock_reset_7", 0) == 0 || hour == 0 || hour == 12) {
//            Log.d(TAG, "Đang RESET dữ liệu giờ Puzzle 7");
//            pref.edit()
//                    .remove(PREF_CLOCK_AM)
//                    .remove(PREF_CLOCK_PM)
//                    .putLong("last_clock_reset_7", System.currentTimeMillis())
//                    .apply();
//        }
//    }
//
//    private void puzzleCompletedClock(int boxIndex) {
//        String key = (boxIndex == 0) ? PREF_CLOCK_AM : PREF_CLOCK_PM;
//        SharedPreferences pref = requireContext().getSharedPreferences(getString(R.string.pref), Context.MODE_PRIVATE);
//
//        String existing = pref.getString(key, "[]");
//        try {
//            JSONArray array = new JSONArray(existing);
//            HashSet<Integer> set = new HashSet<>();
//            for (int i = 0; i < array.length(); i++) set.add(array.getInt(i));
//
//            int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY) % 12;
//            if (set.add(hour)) {
//                Log.d(TAG, "Thêm múi giờ mới: " + hour + " vào " + key);
//            }
//
//            JSONArray newArray = new JSONArray();
//            for (int val : set) newArray.put(val);
//            pref.edit().putString(key, newArray.toString()).apply();
//        } catch (JSONException ignored) {}
//    }
//
//    private void loadAndDrawArcs(View root, String prefKey) {
//        SharedPreferences pref = requireContext().getSharedPreferences(getString(R.string.pref), Context.MODE_PRIVATE);
//        String data = pref.getString(prefKey, "[]");
//        try {
//            JSONArray array = new JSONArray(data);
//            for (int i = 0; i < array.length(); i++) {
//                arc(root, array.getInt(i) * 30);
//            }
//        } catch (JSONException ignored) {}
//    }
//
//    private boolean isPeriodCompleted(String prefKey) {
//        SharedPreferences pref = requireContext().getSharedPreferences(getString(R.string.pref), Context.MODE_PRIVATE);
//        String data = pref.getString(prefKey, "[]");
//        try {
//            int count = new JSONArray(data).length();
//            return count >= 12;
//        } catch (JSONException e) {
//            return false;
//        }
//    }
//
//    private int getCurrentCount(String prefKey) {
//        SharedPreferences pref = requireContext().getSharedPreferences(getString(R.string.pref), Context.MODE_PRIVATE);
//        String data = pref.getString(prefKey, "[]");
//        try {
//            return new JSONArray(data).length();
//        } catch (JSONException e) {
//            return 0;
//        }
//    }
//
//    // Hàm arc giữ nguyên như cũ của bạn
//    private void arc(View root, int start) {
//        // ... (giữ nguyên toàn bộ code hàm arc cũ của bạn)
//        int screenH = MainActivity.getDeviceHeightAndWidth(requireContext()).first;
//        int screenW = MainActivity.getDeviceHeightAndWidth(requireContext()).second;
//        RADIUS = Collections.min(Arrays.asList(RADIUS, screenH - 64, screenW - 64));
//
//        ShapeDrawable arcShape = new ShapeDrawable(new ArcShape(start - 90, 30));
////        arcShape.getPaint().setColor(requireContext().getResources().getColor(R.color.bg, null));
//        if (isMorning) {
//            arcShape.getPaint().setColor(requireContext().getResources().getColor(R.color.bg, null));
//        } else {
//            arcShape.getPaint().setColor(requireContext().getResources().getColor(R.color.puzzle7translucent, null));
//        }
//        arcShape.setIntrinsicHeight(RADIUS);
//        arcShape.setIntrinsicWidth(RADIUS);
//        ImageView imageView = new ImageView(requireContext());
//        imageView.setX((float) (screenW / 2.0 - RADIUS / 2.0));
//        imageView.setY((float) (screenH / 2.0 - RADIUS / 2.0 - 75 / 2.0));
//        imageView.setImageDrawable(arcShape);
//        ((ViewGroup) root.findViewById(R.id.ll)).addView(imageView);
//
//        ShapeDrawable arcShape2 = new ShapeDrawable(new ArcShape(start - 90, 30));
////        arcShape2.getPaint().setColor(requireContext().getResources().getColor(R.color.puzzle7translucent, null));
//        if (isMorning) {
//            arcShape2.getPaint().setColor(requireContext().getResources().getColor(R.color.puzzle7translucent, null));
//        } else {
//            arcShape2.getPaint().setColor(requireContext().getResources().getColor(R.color.bg, null));
//        }
//        arcShape2.setIntrinsicHeight((int) (RADIUS * PERCENTAGE));
//        arcShape2.setIntrinsicWidth((int) (RADIUS * PERCENTAGE));
//        ImageView imageView2 = new ImageView(requireContext());
//        imageView2.setX((float) (screenW / 2.0 - RADIUS * PERCENTAGE / 2.0));
//        imageView2.setY((float) (screenH / 2.0 - RADIUS * PERCENTAGE / 2.0 - 75 / 2.0));
//        imageView2.setImageDrawable(arcShape2);
//        ((ViewGroup) root.findViewById(R.id.ll)).addView(imageView2);
//
//        root.findViewById(R.id.imageView0).bringToFront();
//    }
//}

package com.example.blackbox;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.ArcShape;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Puzzle7Fragment extends PuzzleBaseFragment {

    private static final double PERCENTAGE = 0.78;   // điều chỉnh độ dày cung
    private int RADIUS = 1100;

    private static final String PREF_CLOCK_AM = "prefClockAM";
    private static final String PREF_CLOCK_PM = "prefClockPM";

    private void saveBoxCompleted(int boxIndex) {
        Context context = getContext();
        if (context == null) return;
        String key = getPuzzleId() + ":" + boxIndex;
        SharedPreferences pref = context.getSharedPreferences(
                getString(R.string.pref), Context.MODE_PRIVATE
        );
        String existing = pref.getString(getString(R.string.prefSolved), "[]");
        try {
            JSONArray jsonArray = new JSONArray(existing);
            for (int i = 0; i < jsonArray.length(); i++) {
                if (jsonArray.getString(i).equals(key)) return;
            }
            jsonArray.put(key);
            pref.edit().putString(getString(R.string.prefSolved), jsonArray.toString()).apply();
        } catch (JSONException ignored) {}
    }

    @Override
    protected void animation(int index) {
        Activity activity = getActivity();
        if (activity == null) return;

        // Puzzle7 chỉ có imageView0, dùng nó cho cả AM và PM
        activity.runOnUiThread(() -> {
            ImageView iv = activity.findViewById(R.id.imageView0);
            if (iv == null) {
                return;
            }
            iv.setBackgroundResource(R.drawable.animation);
            android.graphics.drawable.AnimationDrawable anim =
                    (android.graphics.drawable.AnimationDrawable) iv.getBackground();
            anim.start();
            saveBoxCompleted(index);
        });
    }

    @Override
    public int getPuzzleId() { return 7; }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_puzzle7, container, false);
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupCoinButton(requireActivity().getWindow().getDecorView().getRootView());
        Calendar cal = Calendar.getInstance();
        int currentHour = cal.get(Calendar.HOUR_OF_DAY);
        boolean isMorning = currentHour < 12;
        int boxIndex = isMorning ? 0 : 1;

        checkAndResetClockIfNeeded();
        puzzleCompletedClock(boxIndex);

        ViewGroup root = view.findViewById(R.id.ll);

        // === Background theo buổi ===
        if (!isMorning) {
            root.setBackgroundColor(getResources().getColor(R.color.bg, null));   // nền đen than
        } else {
            root.setBackgroundColor(getResources().getColor(R.color.puzzle7translucent, null));
        }

        // Xóa arc cũ để tránh chồng chéo
        clearOldArcs(root);

        // Vẽ arc mới
        String prefKey = isMorning ? PREF_CLOCK_AM : PREF_CLOCK_PM;
        loadAndDrawArcs(root, prefKey);

        if (isPeriodCompleted(prefKey)) {
            animation(boxIndex);
        }
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

        SharedPreferences pref = context.getSharedPreferences(getString(R.string.pref), Context.MODE_PRIVATE);
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
        SharedPreferences pref = context.getSharedPreferences(getString(R.string.pref), Context.MODE_PRIVATE);
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
        SharedPreferences pref = requireContext().getSharedPreferences(getString(R.string.pref), Context.MODE_PRIVATE);
        String data = pref.getString(prefKey, "[]");

        try {
            JSONArray array = new JSONArray(data);
            for (int i = 0; i < array.length(); i++) {
                arc(root, array.getInt(i) * 30);
            }
        } catch (JSONException ignored) {}
    }

    private boolean isPeriodCompleted(String prefKey) {
        SharedPreferences pref = requireContext().getSharedPreferences(getString(R.string.pref), Context.MODE_PRIVATE);
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
        outerView.setY((screenH - RADIUS) / 2f - 60);
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
        innerView.setY((float) (screenH - RADIUS * PERCENTAGE) / 2f - 60);
        innerView.setTag("clock_arc");
        root.addView(innerView);

        // Đưa hình tròn giữa lên trên
        View center = root.findViewById(R.id.imageView0);
        if (center != null) {
            center.bringToFront();
        }
    }
}