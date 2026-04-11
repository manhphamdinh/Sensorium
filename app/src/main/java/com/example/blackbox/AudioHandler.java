package com.example.blackbox;

import android.content.Context;
import android.media.SoundPool;
import android.net.Uri;

import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.common.Player;

import java.util.EnumMap;
import java.util.Map;

public class AudioHandler {
    private static final int SOUND_POOL_MAX_STREAMS = 10;
    private static final int BGM_MAIN = R.raw.bgm_73bpm;

    private static ExoPlayer bgm;
    private static SoundPool soundPool;

    private enum SFX {
        LEVEL_SELECT(R.raw.level_select),
        BOX_COMPLETE(R.raw.box_complete),
        PUZZLE_COMPLETE(R.raw.puzzle_complete);

        private final int resourceId;

        SFX(int resourceId) {
            this.resourceId = resourceId;
        }
    }

    private static class Sound {
        int soundId;
        boolean loaded;

        Sound(int soundId) {
            this.soundId = soundId;
            this.loaded = false;
        }
    }

    // ENUM MAP
    private static final Map<SFX, Sound> sounds = new EnumMap<>(SFX.class);

    public static void init(Context context) {
        if (soundPool != null && !sounds.isEmpty()) return;

        soundPool = new SoundPool.Builder().setMaxStreams(SOUND_POOL_MAX_STREAMS).build();

        for (SFX sfx : SFX.values()) {
            load(context, sfx);
        }

        soundPool.setOnLoadCompleteListener((sp, sampleId, status) -> {
            for (Sound sound : sounds.values()) {
                if (sound.soundId == sampleId) {
                    sound.loaded = true;
                    break;
                }
            }
        });
    }

    // HELPERS
    private static void load(Context context, SFX sfx) {
        int soundId = soundPool.load(context, sfx.resourceId, 1);
        sounds.put(sfx, new Sound(soundId));
    }

    private static void playSFX(SFX sfx) {
        if (soundPool == null) return;

        Sound sound = sounds.get(sfx);
        if (sound == null) return;

        if (sound.loaded) {
            soundPool.play(sound.soundId, 1, 1, 1, 0, 1);
        }
    }

    // SFX METHODS
    public static void playLevelSelectSFX() {
        playSFX(SFX.LEVEL_SELECT);
    }

    public static void playBoxCompleteSFX() {
        playSFX(SFX.BOX_COMPLETE);
    }

    public static void playPuzzleCompleteSFX() {
        playSFX(SFX.PUZZLE_COMPLETE);
    }

    // BGM METHODS
    public static void startBgm(Context context) {
        if (bgm == null) {
            bgm = new ExoPlayer.Builder(context.getApplicationContext()).build();

            Uri uri = Uri.parse("android.resource://" + context.getPackageName() + "/" + BGM_MAIN);
            MediaItem mediaItem = MediaItem.fromUri(uri);

            bgm.setMediaItem(mediaItem);
            bgm.setRepeatMode(Player.REPEAT_MODE_ONE); // 🔥 seamless loop
            bgm.prepare();
        }

        if (!bgm.isPlaying()) {
            bgm.play();
        }
    }

    public static void pauseBgm() {
        if (bgm != null && bgm.isPlaying()) {
            bgm.pause();
        }
    }

    // RELEASE
    public static void release() {
        if (bgm != null) {
            bgm.release();
            bgm = null;
        }

        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }

        sounds.clear();
    }
}