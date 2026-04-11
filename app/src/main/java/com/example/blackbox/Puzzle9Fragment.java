package com.example.blackbox;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

public class Puzzle9Fragment extends PuzzleBaseFragment {

    // BOXES ARRAY AND BOX IDS
    private final ImageView[] boxes = new ImageView[3];
    int[] boxIds = {
            R.id.imageView0,
            R.id.imageView1,
            R.id.imageView2,
    };

    // BOX POSITIONS
    private static final int TOP = 0;    // High amped box
    private static final int MIDDLE = 1;   // Mid amped box
    private static final int BOTTOM = 2;   // Low amped Box


    // AMPLITUDE THRESHOLDS
    private static final double HIGH_THRESHOLD = 22000;
    private static final double MID_THRESHOLD = 10000;
    private static final double LOW_THRESHOLD = 2000;
    private static final double TOLERANCE = 500;
    private static final double NOISE = 100;

    // UI
    private static final int BALL_SIZE = 300;

    // SOUND METER
    private SoundMeter sm;

    // Tạo Handler để tự động cập nhật Mic mỗi 50 mili-giây
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable pollMicRunnable;

    @Override
    protected int getTotalBoxes() { return boxes.length; }

    @Override
    public int getPuzzleId() { return 9; }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.activity_puzzle9, container, false);

        // Initialize boxes
        for (int i = 0; i < boxes.length; i++) {
            boxes[i] = root.findViewById(boxIds[i]);
        }

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        for (int index : getCompletedThisRun()) {
            applyCurrentProgress(boxes[index]);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // KIỂM TRA QUYỀN VÀ KHỞI CHẠY MIC
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {

            if (sm == null) {
                sm = new SoundMeter(requireContext());
            }
            sm.start();
            startPollingMic(); // Bắt đầu vòng lặp đo âm thanh
        } else {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 101);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopPollingMic(); // Dừng đo âm thanh khi thoát màn
        if (sm != null) sm.stop();
    }

    // Hàm bắt đầu vòng lặp lấy dữ liệu âm thanh
    private void startPollingMic() {
        if (pollMicRunnable == null) {
            pollMicRunnable = new Runnable() {
                @Override
                public void run() {
                    updateMicUI();
                    handler.postDelayed(this, 50); // Lặp lại liên tục sau mỗi 50ms
                }
            };
        }
        handler.post(pollMicRunnable);
    }

    private void stopPollingMic() {
        if (pollMicRunnable != null) {
            handler.removeCallbacks(pollMicRunnable);
        }
    }

    private void updateMicUI() {
        if (!isAdded() || getContext() == null || getView() == null || sm == null) return;

        final double amp = sm.getAmplitude();

        android.util.Log.d("Puzzle9", "Biên độ âm thanh: " + amp);

        View rootView = getView();
        Context safeContext = getContext();
        if (rootView == null || safeContext == null) return;

        // Chỉ xét thắng nếu tín hiệu âm thanh thực sự lớn hơn nhiễu
        if (amp > NOISE) {
            if (amp >= (HIGH_THRESHOLD - TOLERANCE)) {
                updatePuzzle(boxes[TOP], TOP);
            }
            if (amp >= MID_THRESHOLD - TOLERANCE && amp <= MID_THRESHOLD + TOLERANCE) {
                updatePuzzle(boxes[MIDDLE], MIDDLE);
            }
            if (amp <= LOW_THRESHOLD + TOLERANCE) {
                updatePuzzle(boxes[BOTTOM], BOTTOM);
            }

            if (isPuzzleCompletedThisRun()) {

                // 1. Dừng ngay việc đo mic để tiết kiệm pin và tránh lỗi
                stopPollingMic();
                if (sm != null) {
                    sm.stop();
                }

                // 2. Tạo độ trễ 1.5 giây để người chơi chiêm ngưỡng cả 3 ô cùng sáng
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    if (getActivity() != null) {

                        // Tự động quay về màn hình chính
                        getActivity().finish();
                    }
                }, 1500);
            }
        }

        RelativeLayout ballsContainer = rootView.findViewById(R.id.ballsContainer);
        if (ballsContainer != null) {
            ballsContainer.removeAllViews();

            int numBalls = Math.min((int) (amp / 3000), 10);

            // Mẹo: Nếu tiếng nhỏ quá bóng không hiện, ta ép nó hiện 1 quả cho đẹp
            if (numBalls == 0 && amp > 500) numBalls = 1;

            var sizeData = MainActivity.getDeviceHeightAndWidth(safeContext);
            int deviceHeight = sizeData.first;
            int deviceWidth = sizeData.second;

            for (int i = 0; i < numBalls; i++) {
                ImageView imageView = new ImageView(safeContext);
                imageView.setImageResource(R.drawable.circle);
                imageView.setColorFilter(ContextCompat.getColor(safeContext, R.color.puzzle9translucent));

                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(BALL_SIZE, BALL_SIZE);
                int maxTop = Math.max(1, deviceHeight - BALL_SIZE);
                int maxLeft = Math.max(1, deviceWidth - BALL_SIZE);

                params.topMargin = (int) (Math.random() * maxTop);
                params.leftMargin = (int) (Math.random() * maxLeft);

                ballsContainer.addView(imageView);
            }
        }
    }
}