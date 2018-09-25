package ru.kuchanov.scpcore.monetization.util.appodeal;

import com.appodeal.ads.RewardedVideoCallbacks;

/**
 * Created by mohax on 12.03.2017.
 * <p>
 * for scp_ru
 */
public class MyRewardedVideoCallbacks implements RewardedVideoCallbacks {

    @Override
    public void onRewardedVideoLoaded(final boolean var1) {
//        Timber.d("onRewardedVideoLoaded: %s", var1);
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
    public void onRewardedVideoFinished(final double i, final String s) {
//        Timber.d("onRewardedVideoFinished: %s, %s", i, s);
    }

    @Override
    public void onRewardedVideoClosed(final boolean b) {
//        Timber.d("onRewardedVideoClosed: %s", b);
    }
}