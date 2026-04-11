package com.example.blackbox;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashSet;

public class MainActivity extends AppCompatActivity {

    // GET VIEWS BY TAG
    static ArrayList<ImageView> getViewsByTag(ViewGroup root, String tag) {
        ArrayList<ImageView> imageViews = new ArrayList<>();

        final int childCount = root.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = root.getChildAt(i);
            if (tag.equals(child.getTag())) {
                imageViews.add((ImageView) child);
            }
        }
        return imageViews;
    }

    // GET DEVICE HEIGHT AND WIDTH
    static Pair<Integer, Integer> getDeviceHeightAndWidth(Context context) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        return new Pair<>(displayMetrics.heightPixels, displayMetrics.widthPixels);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // INITIALIZE ACTIVITY
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.RECORD_AUDIO}, 1);
        }

        // INITIALIZE AUDIO MANAGER
        AudioHandler.init(this);
    }

    // UPDATE UI WHEN USER GOES BACK TO MAIN MENU
    @Override
    protected void onResume() {
        super.onResume();
        AudioHandler.startBgm(this);  // Play BGM in menu

        // UPDATE SOLVED BOXES
        SharedPreferences pref = getSharedPreferences(getString(R.string.pref), MODE_PRIVATE);
        String solved = pref.getString(getString(R.string.prefSolved), "[]");
        try {
            JSONArray jsonArray = new JSONArray(solved);
            HashSet<String> solvedBoxes = new HashSet<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                solvedBoxes.add(jsonArray.getString(i));
            }

            ViewGroup grid = findViewById(R.id.ll);
            int childCount = grid.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = grid.getChildAt(i);
                if (!(child instanceof ImageButton)) {
                    continue;
                }

                Object tag = child.getTag();
                if (tag == null) {
                    continue;
                }

                String tagStr = tag.toString();
                if (solvedBoxes.contains(tagStr)) {
                    ((ImageView) child).setImageResource(R.drawable.filled);
                } else {
                    ((ImageView) child).setImageResource(R.drawable.dot);
                }
            }
        } catch (JSONException ignored) {}
    }

    @Override
    protected void onPause() {
        super.onPause();

        AudioHandler.pauseBgm();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        AudioHandler.release();
    }

    // LAUNCH PUZZLE
    public void puzzleLaunch(View view) {
        AudioHandler.pauseBgm();            // Stop music BEFORE entering level
        AudioHandler.playLevelSelectSFX();  // Level select sound

        String tag = (String) view.getTag();

        // tag có dạng "puzzleId:boxIndex", lấy puzzle id
        int puzzleId = Integer.parseInt(tag.split(":")[0]);

        // Launch puzzle
        Intent intent = new Intent(this, PuzzleActivity.class);
        intent.putExtra(PuzzleActivity.EXTRA_PUZZLE_ID, puzzleId);
        startActivity(intent);
    }
}