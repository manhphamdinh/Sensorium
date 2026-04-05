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

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class Puzzle9Fragment extends PuzzleBaseFragment {

    private static final double THRESHOLD = 2000;
    private final int ballSize = 300;
    private SoundMeter sm;

    // Tạo Handler để tự động cập nhật Mic mỗi 50 mili-giây
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable pollMicRunnable;

    // Biến trạng thái để chỉ gọi animation 1 lần cho mỗi ô
    private boolean isSolved0 = false;
    private boolean isSolved1 = false;
    private boolean isSolved2 = false;

    @Override
    public int getPuzzleId() { return 9; }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_puzzle9, container, false);
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
        if (amp > 100) {
            if (amp > (22000 - THRESHOLD) && !isSolved0) {
                isSolved0 = true;
                animation(0);
            }
            if (Math.abs(amp - 10000) < THRESHOLD && !isSolved1) {
                isSolved1 = true;
                animation(1);
            }
            if (amp > 100 && amp < THRESHOLD && !isSolved2) {
                isSolved2 = true;
                animation(2);
            }

            if (isSolved0 && isSolved1 && isSolved2) {

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

        RelativeLayout mergeLayout = rootView.findViewById(R.id.merge);
        if (mergeLayout != null) {
            mergeLayout.removeAllViews();

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

                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ballSize, ballSize);
                int maxTop = Math.max(1, deviceHeight - ballSize);
                int maxLeft = Math.max(1, deviceWidth - ballSize);

                params.topMargin = (int) (Math.random() * maxTop);
                params.leftMargin = (int) (Math.random() * maxLeft);

                mergeLayout.addView(imageView);
            }
        }
    }
}