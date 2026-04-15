package com.example.blackbox;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

public class AdActivity extends AppCompatActivity {

    public static final String EXTRA_VIDEO_URL = "extra_video_url";
    public static final int RESULT_AD_FINISHED = 100;

    private ExoPlayer player;
    private PlayerView playerView;
    private View staticAdLayout;
    private TextView skipTimer;
    private ProgressBar loadingProgress;
    private CountDownTimer countDownTimer;
    private boolean isTimerStarted = false;

    @OptIn(markerClass = UnstableApi.class)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad);

        playerView = findViewById(R.id.player_view);
        staticAdLayout = findViewById(R.id.static_ad_layout);
        skipTimer = findViewById(R.id.skip_timer);
        loadingProgress = findViewById(R.id.loading_progress);

        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);
        player.setPlayWhenReady(true);

        // Ưu tiên sử dụng file advertiser.mp4 bạn vừa thêm vào res/raw
        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.advertiser);

        MediaItem mediaItem = MediaItem.fromUri(videoUri);
        player.setMediaItem(mediaItem);
        player.prepare();

        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == Player.STATE_READY) {
                    loadingProgress.setVisibility(View.GONE);
                    // Hiện Video Player và ẩn layout tĩnh
                    playerView.setVisibility(View.VISIBLE);
                    staticAdLayout.setVisibility(View.GONE);
                    startAdTimer();
                } else if (playbackState == Player.STATE_BUFFERING) {
                    loadingProgress.setVisibility(View.VISIBLE);
                } else if (playbackState == Player.STATE_ENDED) {
                    finishWithResult();
                }
            }

            @Override
            public void onPlayerError(PlaybackException error) {
                // Nếu video lỗi, quay lại hiện banner tĩnh để không hỏng app
                loadingProgress.setVisibility(View.GONE);
                playerView.setVisibility(View.GONE);
                staticAdLayout.setVisibility(View.VISIBLE);
                skipTimer.setText("Quảng cáo (Chế độ offline)...");
                startAdTimer();
            }
        });
    }

    private void startAdTimer() {
        if (isTimerStarted) return;
        isTimerStarted = true;

        countDownTimer = new CountDownTimer(5000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                skipTimer.setText("Quảng cáo sẽ kết thúc sau: " + (millisUntilFinished / 1000 + 1) + "s");
            }

            @Override
            public void onFinish() {
                skipTimer.setText("Bấm để nhận thưởng 🪙");
                skipTimer.setOnClickListener(v -> finishWithResult());
            }
        }.start();
    }

    private void finishWithResult() {
        setResult(RESULT_AD_FINISHED);
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) player.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (player != null) player.play();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) player.release();
        if (countDownTimer != null) countDownTimer.cancel();
    }
}
