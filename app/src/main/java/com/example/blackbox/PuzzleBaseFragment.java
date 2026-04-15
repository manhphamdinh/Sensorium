package com.example.blackbox;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.HashSet;

public abstract class PuzzleBaseFragment extends Fragment {

    private final HashSet<Integer> completedThisRun = new HashSet<>();
    private boolean puzzleCompletedThisRun = false;
    private PuzzleProgress progress;
    private PuzzleCompletion completion;

    private ActivityResultLauncher<Intent> adLauncher;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == AdActivity.RESULT_AD_FINISHED) {
                        animateCoinGain(3); // Thưởng X2 thành 3 xu thay vì 1
                        Toast.makeText(requireContext(), "Tuyệt vời! Bạn nhận được +3 xu thưởng!", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    // CORE
    protected void updatePuzzle(ImageView puzzleBox){
        updatePuzzle(puzzleBox, 0);
    }
    protected void updatePuzzle(ImageView puzzleBox, int boxIndex) {
        if (completedThisRun.contains(boxIndex)) { return; }
        completedThisRun.add(boxIndex);

        Activity activity = getActivity();
        if (activity == null) { return; }
        if (progress.isComplete()) { return; }

        completion.markCompleted(getPuzzleId(), boxIndex);
        progress.savePuzzleProgress(boxIndex);

        activity.runOnUiThread(() -> {
            playAnimation(puzzleBox);
            handleAudio();
            
            // Nếu giải xong cả puzzle, hiện Dialog thưởng X2
            if (progress.isComplete()) {
                showVictoryDialog();
            }
        });
    }

    private void showVictoryDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("🎉 CHÚC MỪNG!")
                .setMessage("Bạn đã giải xong màn chơi này!\nBạn muốn nhận thưởng thế nào?")
                .setPositiveButton("Xem Ad (Nhận 3 xu)", (d, w) -> watchAdForCoin())
                .setNegativeButton("Nhận 1 xu", (d, w) -> animateCoinGain(1))
                .setCancelable(false)
                .show();
    }

    private void animateCoinGain(int amount) {
        Context context = getContext();
        if (context == null) return;

        int currentCoins = HintManager.getCoins(context);
        int newTotal = currentCoins + amount;
        HintManager.addCoin(context, amount);

        TextView coinCount = getActivity().findViewById(R.id.coinCount);
        if (coinCount != null) {
            ValueAnimator animator = ValueAnimator.ofInt(currentCoins, newTotal);
            animator.setDuration(1000);
            animator.addUpdateListener(animation -> coinCount.setText(animation.getAnimatedValue().toString()));
            animator.start();
        }
    }

    // AUDIO
    private void handleAudio() {
        if (progress.isComplete()) {
            if (!puzzleCompletedThisRun) {
                puzzleCompletedThisRun = true;
                AudioHandler.playPuzzleCompleteSFX();
            }
        }
        else {
            AudioHandler.playBoxCompleteSFX();
        }
    }

    protected boolean isPuzzleCompletedThisRun() {
        return puzzleCompletedThisRun;
    }

    protected HashSet<Integer> getCompletedThisRun() {
        return completedThisRun;
    }

    // UI
    private void playAnimation(ImageView puzzleBox) {
        puzzleBox.setBackgroundResource(R.drawable.animation);
        ((AnimationDrawable) puzzleBox.getBackground()).start();
    }

    protected void applyCurrentProgress(ImageView box) {
        playAnimation(box);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        progress = new PuzzleProgress(requireContext(), getPuzzleId(), getTotalBoxes());
        completion = new PuzzleCompletion(requireContext());

        if (progress.isComplete()) {
            progress.resetPuzzleProgress();
            return;
        }

        completedThisRun.clear();
        completedThisRun.addAll(progress.getPuzzleCurrentProgress());
        setupCoinButton(requireActivity().getWindow().getDecorView().getRootView());
    }

    protected void setupCoinButton(View rootView) {
        TextView coinCount = rootView.findViewById(R.id.coinCount);
        if (coinCount != null) {
            coinCount.setText(String.valueOf(HintManager.getCoins(requireContext())));
        }

        View coinBtn = rootView.findViewById(R.id.coinButton);
        if (coinBtn != null) {
            coinBtn.setOnClickListener(v -> showHintDialog());
        }
    }

    protected void showHintDialog() {
        Context context = getContext();
        if (context == null) return;

        int coins = HintManager.getCoins(context);
        int puzzleId = getPuzzleId();

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Hints (🪙 " + coins + " coins)");

        String[] hintLabels = new String[3];
        for (int i = 0; i < 3; i++) {
            hintLabels[i] = HintManager.isHintUnlocked(context, puzzleId, i) ? "Gợi ý " + (i + 1) + " (Đã mở)" : "Gợi ý " + (i + 1) + " (Tốn 2 xu)";
        }

        builder.setItems(hintLabels, (dialog, which) -> {
            if (HintManager.isHintUnlocked(context, puzzleId, which)) {
                showHintContent(puzzleId, which);
            } else {
                if (HintManager.getCoins(context) >= HintManager.COINS_PER_HINT) {
                    confirmUnlockHint(puzzleId, which);
                } else {
                    handleNotEnoughCoins();
                }
            }
        });
        builder.setNegativeButton("Đóng", null);
        builder.show();
    }

    private void handleNotEnoughCoins() {
        View coinBtn = getActivity().findViewById(R.id.coinButton);
        TextView coinCount = getActivity().findViewById(R.id.coinCount);
        
        if (coinBtn != null) {
            // Hiệu ứng rung
            Animation wiggle = AnimationUtils.loadAnimation(getContext(), R.anim.wiggle);
            coinBtn.startAnimation(wiggle);
        }

        if (coinCount != null) {
            // Hiệu ứng đổi màu đỏ tạm thời
            coinCount.setTextColor(Color.RED);
            coinCount.postDelayed(() -> coinCount.setTextColor(Color.WHITE), 1000);
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Không đủ xu!")
                .setMessage("Bạn cần 2 xu để mở hint này.\nHãy xem video để nhận thêm 3 xu ngay.")
                .setPositiveButton("Xem ngay (Nhận 3 xu)", (d, w) -> watchAdForCoin())
                .setNegativeButton("Đóng", null)
                .show();
    }

    private void confirmUnlockHint(int puzzleId, int hintIndex) {
        new AlertDialog.Builder(getContext())
                .setTitle("Mở khóa gợi ý")
                .setMessage("Dùng 2 xu để mở gợi ý này?")
                .setPositiveButton("Mở", (d, w) -> {
                    if (HintManager.unlockHint(getContext(), puzzleId, hintIndex)) {
                        updateCoinUI();
                        showHintContent(puzzleId, hintIndex);
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showHintContent(int puzzleId, int hintIndex) {
        String text = HintManager.getHintText(puzzleId, hintIndex);
        new AlertDialog.Builder(getContext())
                .setTitle("Gợi ý " + (hintIndex + 1))
                .setMessage(text)
                .setPositiveButton("OK", null)
                .show();
    }

    protected void updateCoinUI() {
        if (getActivity() != null) {
            TextView coinCount = getActivity().findViewById(R.id.coinCount);
            if (coinCount != null) {
                coinCount.setText(String.valueOf(HintManager.getCoins(requireContext())));
            }
        }
    }

    protected void watchAdForCoin() {
        Intent intent = new Intent(requireContext(), AdActivity.class);
        adLauncher.launch(intent);
    }

    protected abstract int getPuzzleId();
    protected abstract int getTotalBoxes();
}
