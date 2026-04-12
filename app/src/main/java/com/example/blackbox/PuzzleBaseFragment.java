package com.example.blackbox;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONException;

public abstract class PuzzleBaseFragment extends Fragment {

    protected void animation(int index) {
        Activity activity = getActivity();
        if (activity == null) return;
        activity.runOnUiThread(() -> {
            android.widget.ImageView imageView = activity.findViewById(
                    activity.getResources().getIdentifier("imageView" + index, "id", activity.getPackageName())
            );
            if (imageView == null) return;
            imageView.setBackgroundResource(R.drawable.animation);
            ((AnimationDrawable) imageView.getBackground()).start();
            saveBoxCompleted(index);
        });
    }

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

    /**
     * Gọi hàm này trong onViewCreated() của từng PuzzleFragment
     * để gắn coin button vào layout (nếu layout có view id=coinButton và coinCount).
     */
    protected void setupCoinButton(View rootView) {
        Context context = getContext();
        if (context == null) return;

        // Cập nhật số xu hiển thị
        TextView coinCount = rootView.findViewById(R.id.coinCount);
        if (coinCount != null) {
            coinCount.setText(String.valueOf(HintManager.getCoins(context)));
        }

        // Gắn sự kiện nút coin -> mở dialog hint
        View coinBtn = rootView.findViewById(R.id.coinButton);
        if (coinBtn != null) {
            coinBtn.setOnClickListener(v -> showHintDialog());
        }
    }

    /**
     * Hiển thị dialog chọn hint (3 hint/level, mỗi hint 2 xu)
     */
    protected void showHintDialog() {
        Context context = getContext();
        if (context == null) return;

        int coins = HintManager.getCoins(context);
        int puzzleId = getPuzzleId();

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Hints  🪙 " + coins + " xu");

        String[] hintLabels = new String[3];
        for (int i = 0; i < 3; i++) {
            if (HintManager.isHintUnlocked(context, puzzleId, i)) {
                hintLabels[i] = "Hint " + (i + 1) + " (đã mở) → " + HintManager.getHintText(puzzleId, i);
            } else {
                hintLabels[i] = "Hint " + (i + 1) + " — tốn 2 xu";
            }
        }

        builder.setItems(hintLabels, (dialog, which) -> {
            if (HintManager.isHintUnlocked(context, puzzleId, which)) {
                // Đã mở, hiện nội dung
                showHintContent(puzzleId, which);
            } else {
                // Chưa mở, thử trừ xu
                boolean success = HintManager.unlockHint(context, puzzleId, which);
                if (success) {
                    showHintContent(puzzleId, which);
                    // Cập nhật lại số xu trên UI
                    if (getView() != null) {
                        TextView coinCount = getView().findViewById(R.id.coinCount);
                        if (coinCount != null) {
                            coinCount.setText(String.valueOf(HintManager.getCoins(context)));
                        }
                    }
                } else {
                    // Không đủ xu
                    new AlertDialog.Builder(context)
                            .setTitle("Không đủ xu!")
                            .setMessage("Bạn cần 2 xu để mở hint này.\nHãy xem video để nhận thêm xu.")
                            .setPositiveButton("Xem video", (d, w) -> watchAdForCoin())
                            .setNegativeButton("Đóng", null)
                            .show();
                }
            }
        });

        builder.setNegativeButton("Đóng", null);
        builder.show();
    }

    private void showHintContent(int puzzleId, int hintIndex) {
        Context context = getContext();
        if (context == null) return;
        String text = HintManager.getHintText(puzzleId, hintIndex);
        new AlertDialog.Builder(context)
                .setTitle("Hint " + (hintIndex + 1))
                .setMessage(text)
                .setPositiveButton("OK", null)
                .show();
    }

    /**
     * (Optional) Xem quảng cáo để nhận 1 xu.
     * Tích hợp AdMob rewarded ad thực tế ở đây.
     */
    protected void watchAdForCoin() {
        Context context = getContext();
        if (context == null) return;
        // TODO: Tích hợp AdMob RewardedAd thực tế
        // Tạm thời cộng 1 xu để test
        HintManager.addCoin(context, 1);
        Toast.makeText(context, "+1 xu!", Toast.LENGTH_SHORT).show();
        if (getView() != null) {
            TextView coinCount = getView().findViewById(R.id.coinCount);
            if (coinCount != null) {
                coinCount.setText(String.valueOf(HintManager.getCoins(context)));
            }
        }
    }

    protected abstract int getPuzzleId();
}