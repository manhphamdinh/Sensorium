package com.example.blackbox;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.HashSet;

public abstract class PuzzleBaseFragment extends Fragment {

    // Runtime only
    /// Set of completed boxes in current puzzle
    private final HashSet<Integer> completedThisRun = new HashSet<>();

    /// Current puzzle status
    private boolean puzzleCompletedThisRun = false;
    private PuzzleProgress progress;
    private PuzzleCompletion completion;

    // CORE
    protected void updatePuzzle(ImageView puzzleBox){
        updatePuzzle(puzzleBox, 0);
    }
    protected void updatePuzzle(ImageView puzzleBox, int boxIndex) {

        // Prevent replaying audio
        if (completedThisRun.contains(boxIndex)) { return; }
        completedThisRun.add(boxIndex);

        Activity activity = getActivity();
        if (activity == null) { return; }

        if (progress.isComplete()) { return; }

        // Save states
        completion.markCompleted(getPuzzleId(), boxIndex);
        progress.savePuzzleProgress(boxIndex);

        // Render UI + AUDIO
        activity.runOnUiThread(() -> {
            playAnimation(puzzleBox);
            handleAudio();
        });
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

    // UI
    private void playAnimation(ImageView puzzleBox) {
        puzzleBox.setBackgroundResource(R.drawable.animation);
        ((AnimationDrawable) puzzleBox.getBackground()).start();
    }

    protected void applyCurrentProgress(ImageView box) {
        playAnimation(box);
    }

    // LIFECYCLE
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Context context = getContext();
        if (context == null) { return; }

        progress = new PuzzleProgress(context, getPuzzleId(), getTotalBoxes());
        completion = new PuzzleCompletion(context);

        if (progress.isComplete()) {
            progress.resetPuzzleProgress();
            return;
        }

        completedThisRun.clear();
        completedThisRun.addAll(progress.getPuzzleCurrentProgress());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (progress.isComplete()) {
            progress.resetPuzzleProgress();
        }
    }

    // HINTS
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
                showHintContent(puzzleId, which);
            } else {
                boolean success = HintManager.unlockHint(context, puzzleId, which);
                if (success) {
                    showHintContent(puzzleId, which);
                    if (getView() != null) {
                        TextView coinCount = getView().findViewById(R.id.coinCount);
                        if (coinCount != null) {
                            coinCount.setText(String.valueOf(HintManager.getCoins(context)));
                        }
                    }
                } else {
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

    protected void watchAdForCoin() {
        Context context = getContext();
        if (context == null) return;
        HintManager.addCoin(context, 1);
        Toast.makeText(context, "+1 xu!", Toast.LENGTH_SHORT).show();
        if (getView() != null) {
            TextView coinCount = getView().findViewById(R.id.coinCount);
            if (coinCount != null) {
                coinCount.setText(String.valueOf(HintManager.getCoins(context)));
            }
        }
    }

    // UTILITIES
    protected abstract int getPuzzleId();
    protected abstract int getTotalBoxes();

    protected HashSet<Integer> getCompletedThisRun() {
        return completedThisRun;
    }

    protected boolean isPuzzleCompletedThisRun() {
        return puzzleCompletedThisRun;
    }
}