package com.gatech_computeraudio4590.freqout.fetcher;

import android.util.Log;

import com.gatech_computeraudio4590.freqout.BuildConfig;
import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.FaceServiceRestClient;
import com.microsoft.projectoxford.face.contract.Face;
import com.microsoft.projectoxford.face.rest.ClientException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;

public class FaceFetcher extends Fetcher<InputStream, Face> {
    private static final String TAG = FaceFetcher.class.getSimpleName();
    private static final boolean RETURN_FACE_ID = true;
    private static final boolean RETURN_FACE_LANDMARKS = false;

    private final String apiEndpoint = "https://eastus.api.cognitive.microsoft.com/face/v1.0";
    private final String subscriptionKey = "4424de3156dc4bfc808c7b67fa9a44af";
    private final FaceServiceClient mFaceServiceClient;
    private final FaceServiceClient.FaceAttributeType[] mFaceAttributeTypes;

    public FaceFetcher(final FaceServiceClient.FaceAttributeType[] faceAttributeTypes) {
        super();
        mFaceServiceClient = new FaceServiceRestClient(apiEndpoint, subscriptionKey);
        mFaceAttributeTypes = faceAttributeTypes;
    }

    @Override
    protected Collection<Face> doFetch(final InputStream inputStream) throws ClientException, IOException {
        final Face[] faces = mFaceServiceClient.detect(inputStream, RETURN_FACE_ID, RETURN_FACE_LANDMARKS, mFaceAttributeTypes);
        if (faces == null || faces.length == 0) {
            Log.e(TAG, "Returned null or no faces");
            return null;
        }
        return Arrays.asList(faces);
    }
}
