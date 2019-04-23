package com.gatech_computeraudio4590.freqout;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.gatech_computeraudio4590.freqout.fetcher.FaceFetcher;
import com.gatech_computeraudio4590.freqout.fetcher.Fetcher;
import com.gatech_computeraudio4590.freqout.fetcher.OnFaceFetchListener;
import com.gatech_computeraudio4590.freqout.fetcher.OnFetchListener;
import com.microsoft.projectoxford.face.*;
import com.microsoft.projectoxford.face.contract.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

public class MainActivity extends AppCompatActivity  {
    private static final String TAG = MainActivity.class.getSimpleName();

    private final int[] mResIds = {R.raw.anger, R.raw.contempt, R.raw.disgust, R.raw.happiness,
        R.raw.neutral, R.raw.sadness};

    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int PICK_IMAGE = 1;

    private MediaPlayer mMediaPlayer;

    private ImageView imageView;

    private Fetcher<InputStream, Face> faceFetcher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            requestPermissions(new String[] {Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
        }
        // FACE FETCHER BASED ON EMOTION ENUMS

        faceFetcher = new FaceFetcher(FaceServiceClient.FaceAttributeType.Emotion.values());
        // NEEDS TO BE IMPLEMENTED TO TAKE THE API RESULTS AND PLAY A SOUND
        faceFetcher.addOnFetchListener(new OnFaceFetchListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void onError() {
                Log.d("error", "ON ERROR");
            }

            @Override
            public void onSuccess(Collection<Face> results) {
                int i = 0;
                for (Face face : results) {
                    Log.d(TAG, "Face number " + i++);
                    playSound(getEmotionFromFace(face));
                }


            }
        });
        // Basic Main Activity UI Elements
        Button btnCamera = (Button)findViewById(R.id.btnCamera);
        imageView = (ImageView)findViewById(R.id.imageView);

        // Opens camera and takes a picture then proceeds to onActivityResult Method
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, PICK_IMAGE);
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Takes the bitmap taken from the camera and sets the mainactivity imageview
        Bitmap bitmap = (Bitmap)data.getExtras().get("data");
        imageView.setImageBitmap(bitmap);
        // Converts bitmap to inputstream and calls the fetcher
        faceFetcher.request(bitmapToInputStream(bitmap));
    }

    private InputStream bitmapToInputStream(Bitmap bitmap) {
        // Converts bitmap to InputStream
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        byte[] bitmapData = bos.toByteArray();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bitmapData);

        return inputStream;
    }


    private void playSound(final EmotionType emotionType) {
        final int resId = mResIds[emotionType.ordinal()];
        if (emotionType == null) {
            Log.d(TAG, "returning null");
            return;
        }
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
        Log.d(TAG, "playSound work!" + emotionType);
    }

    private EmotionType getEmotionFromFace(final Face face) {
        final double anger = face.faceAttributes.emotion.anger;
        final double happiness = face.faceAttributes.emotion.happiness;
        final double disgust = face.faceAttributes.emotion.disgust;
        final double contempt = face.faceAttributes.emotion.contempt;
        final double neutral = face.faceAttributes.emotion.neutral;
        final double sadness = face.faceAttributes.emotion.sadness;

        if (anger > 0.5) {
            Log.d(TAG, "returned anger");
            return EmotionType.ANGER;
        } else if (happiness > 0.5) {
            Log.d(TAG, "returned happiness");

            return EmotionType.HAPPINESS;
        } else if (disgust > 0.5) {
            Log.d(TAG, "returned disgust");

            return EmotionType.DISGUST;
        } else if (contempt > 0.5) {
            Log.d(TAG, "returned contempt");

            return EmotionType.CONTEMPT;
        } else if (neutral > 0.5) {
            Log.d(TAG, "returned neutral");

            return EmotionType.NEUTRAL;
        } else if (sadness > 0.5) {
            Log.d(TAG, "returned sadness");

            return EmotionType.SADNESS;
        } else
            return null;
    }


}
