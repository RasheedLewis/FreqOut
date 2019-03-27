package com.gatech_computeraudio4590.freqout;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;

public class MainActivity extends AppCompatActivity  {
    private static final String TAG = MainActivity.class.getSimpleName();

    private final int[] mResIds = {R.raw.anger, R.raw.contempt, R.raw.disgust, R.raw.happiness,
        R.raw.neutral, R.raw.sadness};

    private MediaPlayer mMediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    private void playSound(final EmotionType emotionType) {
        final int resId = mResIds[emotionType.ordinal()];

        if (mMediaPlayer == null) {
            mMediaPlayer = MediaPlayer.create(this, resId);
        } else {
            AssetFileDescriptor assetFileDescriptor = this.getResources().openRawResourceFd(resId);
            try {
                mMediaPlayer.setDataSource(assetFileDescriptor);
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        }
        if (!mMediaPlayer.isPlaying()) {
            mMediaPlayer.start();
        }
    }
}
