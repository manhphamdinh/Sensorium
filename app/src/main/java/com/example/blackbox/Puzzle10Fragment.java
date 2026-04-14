package com.example.blackbox;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;

public class Puzzle10Fragment extends PuzzleBaseFragment {

    private ImageView speechBox;
    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;
    private WavyLineView wavyLineView;

    @Override
    protected int getTotalBoxes() { return 1; }

    @Override
    public int getPuzzleId() { return 10; }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.activity_puzzle10, container, false);

        // Initialize speech speechBox
        speechBox = root.findViewById(R.id.imageView0);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        wavyLineView = view.findViewById(R.id.wavyLineView);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Kiểm tra quyền ghi âm trước khi khởi tạo
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            setupSpeechRecognizer();
        } else {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 100);
        }
    }

    private void setupSpeechRecognizer() {
        if (!SpeechRecognizer.isRecognitionAvailable(requireContext())) {
            Log.e("Puzzle10", "LỖI: Thiết bị không hỗ trợ SpeechRecognizer (Thường do thiếu app Google)");
            return;
        }

        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(requireContext());
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        // Thiết lập ngôn ngữ tiếng Anh để bắt từ "blackbox" chuẩn nhất
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "vi-VN");

        // Bật tính năng trả kết quả ngay khi đang nói
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                Log.d("Puzzle10", "Mic đã mở, sẵn sàng nghe...");
            }

            @Override
            public void onBeginningOfSpeech() {}

            @Override
            public void onRmsChanged(float rmsdB) {

                // Làm cho sóng lượn theo độ lớn của giọng nói
                if (wavyLineView != null && !isPuzzleCompletedThisRun()) {
                    int amplitude = (int) Math.max(0, rmsdB * 10); // Nhân 10 để sóng nhấp nhô rõ hơn
                    wavyLineView.setAmplitude(amplitude);
                    wavyLineView.setPeriod(0.05f); // Tốc độ lượn sóng
                }
            }

            @Override
            public void onBufferReceived(byte[] buffer) {}

            @Override
            public void onEndOfSpeech() {
                Log.d("Puzzle10", "Đã nghe xong, đang phân tích...");
            }

            @Override
            public void onError(int error) {
                Log.e("Puzzle10", "Lỗi nhận diện mã số: " + error);

                // Khởi động lại vòng lặp lắng nghe nếu không phải lỗi hệ thống (5) hoặc đang bận (8)
                if (!isPuzzleCompletedThisRun() && speechRecognizer != null) {
                    if (error != SpeechRecognizer.ERROR_RECOGNIZER_BUSY && error != SpeechRecognizer.ERROR_CLIENT) {
                        speechRecognizer.startListening(speechRecognizerIntent);
                    }
                }

                // Khi im lặng hoặc lỗi thì cho sóng phẳng lại
                if (wavyLineView != null) wavyLineView.setAmplitude(0);
            }

            @Override
            public void onResults(Bundle results) {
                boolean isWon = processSpeechResults(results);

                // CHỈ khởi động lại mic khi đã nghe xong trọn vẹn cả câu mà vẫn sai
                if (!isWon && !isPuzzleCompletedThisRun() && speechRecognizer != null) {
                    speechRecognizer.startListening(speechRecognizerIntent);
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {

                // KHÔNG khởi động lại mic ở đây, chỉ kiểm tra xem có trúng chưa
                processSpeechResults(partialResults);
            }

            @Override
            public void onEvent(int eventType, Bundle params) {}


            private boolean processSpeechResults(Bundle results) {
                if (isPuzzleCompletedThisRun()) return true;

                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null) {
                    for (String word : matches) {
                        String spokenText = word.toLowerCase().replace(" ", "");

                        // Chỉ in ra log nếu có chữ thật, tránh spam dòng trống
                        if (!spokenText.isEmpty()) {
                            Log.d("Puzzle10", "MÁY NGHE THẤY CHỮ: " + word);
                        }

                        if (spokenText.contains("blackbox") || spokenText.contains("black") || spokenText.contains("box")) {
                            Log.d("Puzzle10", "CHÍNH XÁC! GỌI ANIMATION QUA MÀN!");
                            updatePuzzle(speechBox);

                            if (wavyLineView != null) {
                                wavyLineView.setAmplitude(0);
                                wavyLineView.setPeriod(0);
                            }

                            speechRecognizer.stopListening();


                            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                                if (getActivity() != null) {

                                    getActivity().finish();

                                }
                            }, 1500);

                            return true; // Đã thắng
                        }
                    }
                }
                return false;
            }
        });


        speechRecognizer.startListening(speechRecognizerIntent);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (speechRecognizer != null) {
            speechRecognizer.stopListening();
            speechRecognizer.destroy();
            speechRecognizer = null;
        }
    }
}
