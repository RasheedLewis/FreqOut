package com.gatech_computeraudio4590.freqout.audio;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;

public class MediaPlayerService extends Service implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnSeekCompleteListener, MediaPlayer.OnInfoListener,
        MediaPlayer.OnBufferingUpdateListener, AudioManager.OnAudioFocusChangeListener {
    private static final String TAG = MediaPlayerService.class.getSimpleName();
    private static final float FULL_VOLUME = 1.0f;
    private static final float REDUCED_VOLUME = 0.1f;
    public static final String MEDIA_FILE_KEY = "media";

    private final IBinder mIBinder = new LocalBinder();

    private AudioManager mAudioManager;
    private MediaPlayer mMediaPlayer;
    private AssetFileDescriptor mMediaFile;

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        try {
            mMediaFile = this.getResources().openRawResourceFd(intent.getExtras()
                    .getInt(MEDIA_FILE_KEY));
        } catch (NullPointerException e) {
            Log.e(TAG, e.getMessage());
            stopSelf();
        }

        if (!requestAudioFocus()) {
            stopSelf();
        }

        if (mMediaFile != null) {
            initMediaPlayer();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMediaPlayer != null) {
            stopMedia();
            mMediaPlayer.release();
        }
        removeAudioFocus();
    }

    @Override
    public IBinder onBind(final Intent intent) {
        return mIBinder;
    }

    private void initMediaPlayer() {
        mMediaPlayer = new MediaPlayer();

        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setOnSeekCompleteListener(this);
        mMediaPlayer.setOnInfoListener(this);
        mMediaPlayer.setOnBufferingUpdateListener(this);
        mMediaPlayer.reset();

        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mMediaPlayer.setDataSource(mMediaFile);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            stopSelf();
        }
        mMediaPlayer.prepareAsync();
    }

    private void playMedia() {
        if (!mMediaPlayer.isPlaying()) {
            mMediaPlayer.start();
        }
    }

    private void stopMedia() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
        }
    }

    @Override
    public void onBufferingUpdate(final MediaPlayer mediaPlayer, final int percent) {
    }

    @Override
    public void onCompletion(final MediaPlayer mediaPlayer) {
        stopMedia();
        stopSelf();
    }

    @Override
    public boolean onError(final MediaPlayer mediaPlayer, final int errorNo, final int extra) {
        switch (errorNo) {
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                Log.e(TAG, "Media error not valid for progressive playback " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Log.e(TAG, "Media error server died " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_TIMED_OUT:
                Log.e(TAG, "Media error timed out " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Log.e(TAG, "Media error unknown " + extra);
                break;
        }
        return false;
    }

    @Override
    public boolean onInfo(final MediaPlayer mediaPlayer, final int errorNo, final int extra) {
        return false;
    }

    @Override
    public void onPrepared(final MediaPlayer mediaPlayer) {
        playMedia();
    }

    public void onSeekComplete(final MediaPlayer mediaPlayer) {
    }

    @Override
    public void onAudioFocusChange(final int audioFocusState) {
        switch (audioFocusState) {
            case AudioManager.AUDIOFOCUS_GAIN:
                if (mMediaPlayer == null) {
                    initMediaPlayer();
                } else if (!mMediaPlayer.isPlaying()) {
                    mMediaPlayer.start();
                }
                mMediaPlayer.setVolume(FULL_VOLUME, FULL_VOLUME);
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.stop();
                }
                mMediaPlayer.release();
                mMediaPlayer = null;
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.pause();
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.setVolume(REDUCED_VOLUME, REDUCED_VOLUME);
                }
                break;
        }
    }

    private boolean requestAudioFocus() {
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        final int audioFocus = mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);
        return audioFocus == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    private boolean removeAudioFocus() {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED == mAudioManager.abandonAudioFocus(this);
    }

    public class LocalBinder extends Binder {
        public MediaPlayerService getMediaPlayerService() {
            return MediaPlayerService.this;
        }
    }
}
