package com.gatech_computeraudio4590.freqout.fetcher;

import java.util.Collection;

public interface OnFetchListener<T> {
    void onStart();
    void onError();
    void onSuccess(final Collection<T> results);
}
