package com.example.blackbox;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONException;

public abstract class PuzzleBaseFragment extends Fragment {

    protected void animation(int index) {
        Activity activity = getActivity();
        if (activity == null) return;
        activity.runOnUiThread(() -> {
            ImageView imageView = activity.findViewById(
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

    protected abstract int getPuzzleId();
}