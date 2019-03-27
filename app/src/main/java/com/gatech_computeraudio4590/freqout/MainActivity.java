package com.gatech_computeraudio4590.freqout;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity  {
    private static final String TAG = MainActivity.class.getSimpleName();

    private final int[] mResIds = {R.raw.anger, R.raw.contempt, R.raw.disgust, R.raw.happiness,
        R.raw.neutral, R.raw.sadness};

    private static final int CAMERA_REQUEST_CODE = 100;

    private MediaPlayer mMediaPlayer;

    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            requestPermissions(new String[] {Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
        }


        Button btnCamera = (Button)findViewById(R.id.btnCamera);
        imageView = (ImageView)findViewById(R.id.imageView);

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, 0);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap bitmap = (Bitmap)data.getExtras().get("data");
        imageView.setImageBitmap(bitmap);
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
