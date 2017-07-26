package ru.kuchanov.scpcore.monetization.util;

import com.appodeal.ads.NonSkippableVideoCallbacks;

import timber.log.Timber;

/**
 * Created by mohax on 12.03.2017.
 * <p>
 * for scp_ru
 */
public class MyNonSkippableVideoCallbacks implements NonSkippableVideoCallbacks {

    @Override
    public void onNonSkippableVideoLoaded() {
        Timber.d("onNonSkippableVideoLoaded");
    }

    @Override
    public void onNonSkippableVideoFailedToLoad() {
        Timber.d("onNonSkippableVideoFailedToLoad");
    }

    @Override
    public void onNonSkippableVideoShown() {
        Timber.d("onNonSkippableVideoShown");
    }

    @Override
    public void onNonSkippableVideoFinished() {
        Timber.d("onNonSkippableVideoFinished");
    }

    @Override
    public void onNonSkippableVideoClosed(boolean b) {
        Timber.d("onNonSkippableVideoClosed: %s", b);
    }
}