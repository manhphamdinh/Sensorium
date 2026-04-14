package com.example.blackbox;

import android.Manifest;
import android.app.AlertDialog;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashSet;

public class MainActivity extends AppCompatActivity {

    private ActivityResultLauncher<Intent> adLauncher;

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

        adLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == AdActivity.RESULT_AD_FINISHED) {
                        HintManager.addCoin(this, 1);
                        updateCoinDisplay();
                        Toast.makeText(this, "+1 xu!", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Khởi tạo xu
        updateCoinDisplay();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.RECORD_AUDIO}, 1);
        }

        // INITIALIZE AUDIO MANAGER
        AudioHandler.init(this);
    }

    private void updateCoinDisplay() {
        TextView mainCoinCount = findViewById(R.id.mainCoinCount);
        if (mainCoinCount != null) {
            mainCoinCount.setText(String.valueOf(HintManager.getCoins(this)));
        }
    }

    public void showCoinOptions(View view) {
        new AlertDialog.Builder(this)
                .setTitle("Nhận thêm xu")
                .setMessage("Bạn có muốn xem video để nhận thêm 1 xu không?")
                .setPositiveButton("Xem video", (dialog, which) -> {
                    Intent intent = new Intent(this, AdActivity.class);
                    adLauncher.launch(intent);
                })
                .setNegativeButton("Đóng", null)
                .show();
    }

    // UPDATE UI WHEN USER GOES BACK TO MAIN MENU
    @Override
    protected void onResume() {
        super.onResume();
        updateCoinDisplay();
        AudioHandler.startBgm(this);  // Play BGM in menu

        // UPDATE SOLVED BOXES
        SharedPreferences completionPref = getSharedPreferences(getString(R.string.prefCompletion), MODE_PRIVATE);
        String solved = completionPref.getString(getString(R.string.prefSolved), "[]");
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
        if (tag == null) return;

        // tag có dạng "puzzleId:boxIndex", lấy puzzle id
        int puzzleId = Integer.parseInt(tag.split(":")[0]);

        // Launch puzzle
        Intent intent = new Intent(this, PuzzleActivity.class);
        intent.putExtra(PuzzleActivity.EXTRA_PUZZLE_ID, puzzleId);
        startActivity(intent);
    }

    public void resetGame(View view) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Xác nhận Reset")
                .setMessage("Hành động này sẽ xóa toàn bộ tiến trình chơi của bạn. Bạn có chắc chắn không?")
                .setPositiveButton("Xóa hết", (dialog, which) -> {

                    // Clear progress and completion
                    PuzzleCompletion.resetAllCompletion(this);
                    PuzzleProgress.resetAllProgress(this);

                    recreate();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
