package com.example.blackbox;

import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioDeviceCallback;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Puzzle3Fragment extends PuzzleBaseFragment {

    private BroadcastReceiver audioReceiver;
    private AudioDeviceCallback audioDeviceCallback;
    private ValueAnimator fluidAnimator;
    private final Runnable stateChecker = new Runnable() {
        @Override
        public void run() {
            updateState();
            if (isAdded() && getView() != null) {
                getView().postDelayed(this, 500); // fallback polling
            }
        }
    };

    @Override
    public int getPuzzleId() {
        return 3;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_puzzle3, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupCoinButton(requireActivity().getWindow().getDecorView().getRootView());
    }

    @Override
    public void onResume() {
        super.onResume();

        AudioManager audioManager =
                (AudioManager) requireContext().getSystemService(Context.AUDIO_SERVICE);

        // ---- Broadcast receiver (volume + ringer fallback) ----
        audioReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateState();
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(AudioManager.RINGER_MODE_CHANGED_ACTION);
        filter.addAction("android.media.VOLUME_CHANGED_ACTION");
        filter.addAction(Intent.ACTION_HEADSET_PLUG);

        requireContext().registerReceiver(audioReceiver, filter);

        // ---- Audio device callback (Bluetooth + wired real-time) ----
        audioDeviceCallback = new AudioDeviceCallback() {
            @Override
            public void onAudioDevicesAdded(AudioDeviceInfo[] addedDevices) {
                updateState();
            }

            @Override
            public void onAudioDevicesRemoved(AudioDeviceInfo[] removedDevices) {
                updateState();
            }
        };

        audioManager.registerAudioDeviceCallback(audioDeviceCallback, null);

        // ---- Start fallback polling (fixes edge cases) ----
        if (getView() != null) {
            getView().post(stateChecker);
        }

        updateState();
    }

    @Override
    public void onPause() {
        super.onPause();

        AudioManager audioManager =
                (AudioManager) requireContext().getSystemService(Context.AUDIO_SERVICE);

        if (audioReceiver != null) {
            requireContext().unregisterReceiver(audioReceiver);
        }

        if (audioDeviceCallback != null) {
            audioManager.unregisterAudioDeviceCallback(audioDeviceCallback);
        }

        if (getView() != null) {
            getView().removeCallbacks(stateChecker);
        }
    }

    private void updateState() {
        AudioManager audioManager =
                (AudioManager) requireContext().getSystemService(Context.AUDIO_SERVICE);

        int volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        boolean isHeadphonesOn = false;

        for (AudioDeviceInfo device : audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)) {
            int type = device.getType();

            if (type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES ||
                    type == AudioDeviceInfo.TYPE_WIRED_HEADSET ||
                    type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
                    type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO) {

                isHeadphonesOn = true;
                break;
            }
        }

        boolean isSilent =
                audioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT
                || audioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE;

        if (volume == max) {
            animation(0);
        }

        if (volume == 0) {
            animation(1);
        }

        if (isHeadphonesOn) {
            animation(2);
        }

        if (isSilent) {
            animation(3);
        }

        // ---- Fluid UI ----
        updateFluid(volume, max);
    }

    private int lastHeight = -1;
    private void updateFluid(int volume, int max) {
        View root = getView();
        if (root == null) return;

        ImageView fluid = root.findViewById(R.id.fluid);

        int deviceHeight = MainActivity
                .getDeviceHeightAndWidth(requireContext()).first;

        float ratio = max > 0 ? (float) volume / max : 0;
        int targetHeight = (int) (deviceHeight * ratio);

        ViewGroup.LayoutParams params = fluid.getLayoutParams();
        if (lastHeight == targetHeight) {
            return;
        }
        lastHeight = targetHeight;

        int startHeight = params.height;

        if (fluidAnimator != null && fluidAnimator.isRunning()) {
            fluidAnimator.cancel();
        }

        fluidAnimator = ValueAnimator.ofInt(startHeight, targetHeight);
        fluidAnimator.setDuration(300);
        fluidAnimator.setInterpolator(new DecelerateInterpolator());

        fluidAnimator.addUpdateListener(animation -> {
            params.height = (int) animation.getAnimatedValue();
            fluid.setLayoutParams(params);
        });

        fluidAnimator.start();
    }
}