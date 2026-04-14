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

    // BOXES ARRAY AND BOX IDS
    private final ImageView[] boxes = new ImageView[4];
    int[] boxIds = {
            R.id.imageView0,
            R.id.imageView1,
            R.id.imageView2,
            R.id.imageView3,
    };

    // BOX POSITIONS
    // 0 1
    // 3 2
    private static final int TOP_LEFT = 0;      // SILENCE
    private static final int TOP_RIGHT = 1;     // MAX VOLUME
    private static final int BOTTOM_RIGHT = 2;  // NO VOLUME
    private static final int BOTTOM_LEFT = 3;   // HEADPHONES

    // CACHE
    private BroadcastReceiver audioReceiver;
    private AudioDeviceCallback audioDeviceCallback;
    private ValueAnimator fluidAnimator;
    private ImageView fluid;
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
    protected int getTotalBoxes() {
        return boxes.length;
    }

    @Override
    public int getPuzzleId() {
        return 3;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.activity_puzzle3, container, false);

        // CACHE
        for (int i = 0; i < boxes.length; i++) {
            boxes[i] = root.findViewById(boxIds[i]);
        }

        fluid = root.findViewById(R.id.fluid);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        for (int index : getCompletedThisRun()) {
            applyCurrentProgress(boxes[index]);
        }
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

        if (isSilent) {
            updatePuzzle(boxes[TOP_LEFT], TOP_LEFT);
        }

        if (volume == max) {
            updatePuzzle(boxes[TOP_RIGHT], TOP_RIGHT);
        }

        if (volume == 0) {
            updatePuzzle(boxes[BOTTOM_RIGHT], BOTTOM_RIGHT);
        }

        if (isHeadphonesOn) {
            updatePuzzle(boxes[BOTTOM_LEFT], BOTTOM_LEFT);
        }

        // ---- Fluid UI ----
        updateFluid(volume, max);
    }

    private int lastHeight = -1;
    private void updateFluid(int volume, int max) {
        View root = getView();
        if (root == null) return;

        int deviceHeight = MainActivity
                .getDeviceHeightAndWidth(requireContext()).first;

        float ratio = max > 0 ? (float) volume / max : 0;
        int currentHeight = (int) (deviceHeight * ratio);

        ViewGroup.LayoutParams params = fluid.getLayoutParams();
        if (lastHeight == currentHeight) {
            return;
        }
        lastHeight = currentHeight;

        int startHeight = params.height;

        if (fluidAnimator != null && fluidAnimator.isRunning()) {
            fluidAnimator.cancel();
        }

        fluidAnimator = ValueAnimator.ofInt(startHeight, currentHeight);
        fluidAnimator.setDuration(300);
        fluidAnimator.setInterpolator(new DecelerateInterpolator());

        fluidAnimator.addUpdateListener(animation -> {
            params.height = (int) animation.getAnimatedValue();
            fluid.setLayoutParams(params);
        });

        fluidAnimator.start();
    }
}