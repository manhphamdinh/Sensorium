package com.example.blackbox;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.TextView;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

public class AdActivity extends AppCompatActivity {

    public static final String EXTRA_VIDEO_URL = "extra_video_url";
    public static final int RESULT_AD_FINISHED = 100;

    private ExoPlayer player;
    private PlayerView playerView;
    private TextView skipTimer;
    private CountDownTimer countDownTimer;

    @OptIn(markerClass = UnstableApi.class)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad);

        playerView = findViewById(R.id.player_view);
        skipTimer = findViewById(R.id.skip_timer);

        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);

        // Sử dụng một video mặc định nếu không có URL truyền vào
        String videoUrl = getIntent().getStringExtra(EXTRA_VIDEO_URL);
        if (videoUrl == null) {
            // URL video demo
            videoUrl = "https://storage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4";
        }

        MediaItem mediaItem = MediaItem.fromUri(Uri.parse(videoUrl));
        player.setMediaItem(mediaItem);
        player.prepare();
        player.play();

        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == Player.STATE_ENDED) {
                    finishWithResult();
                }
            }
        });

        // Đếm ngược 5 giây để có thể kết thúc (giả lập quảng cáo tối thiểu 5s)
        countDownTimer = new CountDownTimer(5000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                skipTimer.setText("Quảng cáo sẽ kết thúc sau: " + (millisUntilFinished / 1000 + 1) + "s");
            }

            @Override
            public void onFinish() {
                skipTimer.setText("Bạn có thể đóng quảng cáo");
                skipTimer.setOnClickListener(v -> finishWithResult());
            }
        }.start();
    }

    private void finishWithResult() {
        setResult(RESULT_AD_FINISHED);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
        }
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}