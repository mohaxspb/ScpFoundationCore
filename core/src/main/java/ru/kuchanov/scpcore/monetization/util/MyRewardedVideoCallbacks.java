package ru.kuchanov.scpcore.monetization.util;

import com.appodeal.ads.RewardedVideoCallbacks;

import timber.log.Timber;

/**
 * Created by mohax on 12.03.2017.
 * <p>
 * for scp_ru
 */
public class MyRewardedVideoCallbacks implements RewardedVideoCallbacks {

    @Override
    public void onRewardedVideoLoaded() {
//        Timber.d("onRewardedVideoLoaded");
    }

    @Override
    public void onRewardedVideoFailedToLoad() {
//        Timber.d("onRewardedVideoFailedToLoad");
    }

    @Override
    public void onRewardedVideoShown() {
//        Timber.d("onRewardedVideoShown");
    }

    @Override
    public void onRewardedVideoFinished(final int i, final String s) {
//        Timber.d("onRewardedVideoFinished: %s, %s", i, s);
    }

    @Override
    public void onRewardedVideoClosed(final boolean b) {
//        Timber.d("onRewardedVideoClosed: %s", b);
    }
}