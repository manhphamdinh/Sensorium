package com.example.blackbox;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

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
        completion = new PuzzleCompletion(context, getPuzzleId());

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