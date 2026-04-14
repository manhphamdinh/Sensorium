package com.example.blackbox;

import android.app.Activity;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Puzzle6Fragment extends PuzzleBaseFragment {

    private ImageView screenshotBox;    // Screenshot screenshotBox
    private ContentObserver screenshotObserver;
    private Activity.ScreenCaptureCallback screenCaptureCallback; // API mới của Android 14
    private boolean isSolved = false;

    @Override
    protected int getTotalBoxes() { return 1; }

    @Override
    public int getPuzzleId() { return 6; }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.activity_puzzle6, container, false);

        // Find screenshotBox
        screenshotBox = root.findViewById(R.id.imageView0);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupCoinButton(requireActivity().getWindow().getDecorView().getRootView());
        
        // Re-apply progress
        if (getCompletedThisRun().contains(0)) {
            applyCurrentProgress(screenshotBox);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        isSolved = false;

        // CÁCH MỚI NHẤT: Dành cho Android 14 trở lên (Máy ảo API 36 của bạn sẽ chạy dòng này)
        if (Build.VERSION.SDK_INT >= 34) { // 34 = Build.VERSION_CODES.UPSIDE_DOWN_CAKE
            screenCaptureCallback = new Activity.ScreenCaptureCallback() {
                @Override
                public void onScreenCaptured() {
                    Log.d("Puzzle6", "Phát hiện chụp màn hình bằng API chuẩn của Google!");
                    processWin();
                }
            };
            if (getActivity() != null) {

                // Đăng ký nhận diện chụp màn hình (Không cần xin quyền trong Manifest!)
                getActivity().registerScreenCaptureCallback(requireContext().getMainExecutor(), screenCaptureCallback);
            }
        }

        // CÁCH CŨ: Dành cho Android 13 trở xuống (Đề phòng sau này bạn chạy trên máy cũ)
        else {
            screenshotObserver = new ContentObserver(new Handler(Looper.getMainLooper())) {
                @Override
                public void onChange(boolean selfChange, Uri uri) {
                    super.onChange(selfChange, uri);
                    if (uri == null || getActivity() == null || !isAdded()) return;

                    String uriPath = uri.toString().toLowerCase();
                    if (uriPath.contains("screenshot") || uriPath.contains("media")) {
                        Log.d("Puzzle6", "Phát hiện chụp màn hình qua thay đổi thư mục ảnh!");
                        processWin();
                    }
                }
            };
            requireContext().getContentResolver().registerContentObserver(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true, screenshotObserver);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Dời việc hủy đăng ký xuống tận lúc màn hình này bị đóng hẳn
        // Nhờ vậy, khi bạn mở Đa nhiệm hoặc kéo thanh thông báo xuống, nó vẫn tiếp tục "nghe"
        if (Build.VERSION.SDK_INT >= 34 && screenCaptureCallback != null && getActivity() != null) {
            getActivity().unregisterScreenCaptureCallback(screenCaptureCallback);
            screenCaptureCallback = null;
        } else if (screenshotObserver != null) {
            requireContext().getContentResolver().unregisterContentObserver(screenshotObserver);
            screenshotObserver = null;
        }
    }

    // Tách logic thắng ra hàm riêng cho gọn
    private void processWin() {
        if (isSolved || getActivity() == null) return;
        isSolved = true;

        getActivity().runOnUiThread(() -> {
            Log.d("Puzzle6", "CHÍNH XÁC! Gọi animation qua màn!");
            updatePuzzle(screenshotBox);

            // Đợi 1.5 giây để nhìn thấy ô sáng lên rồi tự động quay về bản đồ chính
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (getActivity() != null && isAdded()) {
                    getActivity().finish();
                }
            }, 1500);
        });
    }
}