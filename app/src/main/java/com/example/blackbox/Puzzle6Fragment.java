package com.example.blackbox;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.content.ContextCompat;

public class Puzzle6Fragment extends PuzzleBaseFragment {

    private BroadcastReceiver receiver;
    private ContentObserver screenshotObserver;

    @Override
    public int getPuzzleId() { return 6; }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_puzzle6, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        screenshotObserver = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange, Uri uri) {
                if (uri.toString().contains("screenshots") || uri.toString().contains("media")) {
                    getActivity().runOnUiThread(() -> animation(0));
                }
            }
        };

        requireContext().getContentResolver().registerContentObserver(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                true,
                screenshotObserver
        );
    }
    @Override
    public void onPause() {
        super.onPause();
        if (screenshotObserver != null) {
            requireContext().getContentResolver().unregisterContentObserver(screenshotObserver);
        }
    }
}