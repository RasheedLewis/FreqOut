package com.gatech_computeraudio4590.freqout.fetcher;

import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public abstract class Fetcher<S, T> {
    private static final String TAG = Fetcher.class.getSimpleName();

    private final List<OnFetchListener> mOnFetchListeners = new ArrayList<>();
    private final OnFetchListener<T> mOnFetchListener = new OnFetchListener<T>() {
        @Override
        public void onStart() {
            Log.d(TAG, "Fetcher has started");
        }

        @Override
        public void onError() {
            Log.e(TAG, "An error occurred while fetching");
        }

        @Override
        public void onSuccess(final Collection<T> results) {
            if (results == null) {
                Log.d(TAG, "Returned null items");
                return;
            }
            StringBuilder sb = new StringBuilder();
            String message = "Items have been fetched: ";
            sb.append(message);
            for (T result : results) {
                sb.append(result + " ");
            }
            Log.d(TAG, "Items have been fetched: " + sb.toString());
        }
    };
    private final Queue<S> mRequests = new LinkedList<>();

    private boolean mIsFetching = false;

    public boolean isFetching() {
        return mIsFetching;
    }

    public void addOnFetchListener(final OnFetchListener onFetchListener) {
        mOnFetchListeners.add(onFetchListener);
    }

    protected abstract Collection<T> doFetch(final S s) throws Exception;

    public void request(final S s) {
        mRequests.add(s);
        if (!mIsFetching) {
            new FetchAsyncTask().execute();
        }
    }

    private class FetchAsyncTask extends AsyncTask<Void, String, Collection<T>> {
        @Override
        protected void onPreExecute() {
            mIsFetching = true;
            mOnFetchListener.onStart();
            for (OnFetchListener onFetchListener : mOnFetchListeners) {
                onFetchListener.onStart();
            }
        }

        @Override
        protected Collection<T> doInBackground(final Void... voids) {
            final List<T> list = new ArrayList<>();
            try {
                while (mRequests.size() > 0) {
                    Collection<T> results = doFetch(mRequests.remove());
                    if (results != null) {
                        list.addAll(results);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                mOnFetchListener.onError();
                for (OnFetchListener onFetchListener : mOnFetchListeners) {
                    onFetchListener.onError();
                }
                return null;
            }
            return list;
        }

        @Override
        protected void onPostExecute(final Collection<T> results) {
            mIsFetching = false;
            mOnFetchListener.onSuccess(results);
            for (OnFetchListener onFetchListener : mOnFetchListeners) {
                onFetchListener.onSuccess(results);
            }
        }
    }
}
