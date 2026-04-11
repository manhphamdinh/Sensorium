package com.example.blackbox;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashSet;

public class PuzzleProgress {

    private final Context context;
    private final String progressKey;
    private final int totalBoxes;

    public PuzzleProgress(Context context, int puzzleId, int totalBoxes) {
        this.context = context;
        this.progressKey = "progress_" + puzzleId;
        this.totalBoxes = totalBoxes;
    }

    public void savePuzzleProgress(int boxIndex) {
        SharedPreferences pref = context.getSharedPreferences(
                context.getString(R.string.pref), Context.MODE_PRIVATE
        );

        String existing = pref.getString(progressKey, "[]");

        try {
            JSONArray array = new JSONArray(existing);

            for (int i = 0; i < array.length(); i++) {
                if (array.getInt(i) == boxIndex) return;
            }

            array.put(boxIndex);
            pref.edit().putString(progressKey, array.toString()).apply();

        } catch (JSONException ignored) {}
    }

    public HashSet<Integer> getPuzzleCurrentProgress() {
        SharedPreferences pref = context.getSharedPreferences(
                context.getString(R.string.pref), Context.MODE_PRIVATE
        );

        String existing = pref.getString(progressKey, "[]");

        HashSet<Integer> set = new HashSet<>();

        try {
            JSONArray array = new JSONArray(existing);
            for (int i = 0; i < array.length(); i++) {
                set.add(array.getInt(i));
            }
        } catch (JSONException ignored) {}

        return set;
    }

    public boolean isComplete() {
        return getPuzzleCurrentProgress().size() == totalBoxes;
    }

    public void resetPuzzleProgress() {
        SharedPreferences pref = context.getSharedPreferences(
                context.getString(R.string.pref), Context.MODE_PRIVATE
        );

        pref.edit().remove(progressKey).apply();
    }
}
