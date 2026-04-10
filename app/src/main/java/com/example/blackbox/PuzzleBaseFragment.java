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
    private final HashSet<Integer> completedThisRun = new HashSet<>();
    private boolean puzzleCompletedThisRun = false;
    private PuzzleProgression progression;
    private PuzzleCompletion completion;

    // CORE
    protected void updatePuzzle(ImageView puzzleBox){
        updatePuzzle(puzzleBox, 0);
    }
    protected void updatePuzzle(ImageView puzzleBox, int boxIndex) {

        if (completedThisRun.contains(boxIndex)) { return; }
        completedThisRun.add(boxIndex);

        Activity activity = getActivity();
        if (activity == null) { return; }

        if (progression.isComplete()) { return; }

        // Save states
        completion.markCompleted(getPuzzleId(), boxIndex);
        progression.save(boxIndex);

        activity.runOnUiThread(() -> {
            playAnimation(puzzleBox);
            handleAudio();
        });
    }

    // AUDIO
    private void handleAudio() {
        if (progression.isComplete()) {
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

    protected void applyLoadedProgress(ImageView box) {
        playAnimation(box);
    }

    // LIFECYCLE
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Context context = getContext();
        if (context == null) { return; }

        progression = new PuzzleProgression(context, getPuzzleId(), getTotalBoxes());
        completion = new PuzzleCompletion(context, getPuzzleId());

        completedThisRun.clear();
        completedThisRun.addAll(progression.load());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (progression.isComplete()) {
            progression.reset();
        }
    }

    // ABSTRACT
    protected abstract int getPuzzleId();
    protected abstract int getTotalBoxes();

    protected HashSet<Integer> getCompletedThisRun() {
        return completedThisRun;
    }

    protected boolean isPuzzleCompletedThisRun() {
        return puzzleCompletedThisRun;
    }
}