package ru.kuchanov.scpcore.monetization.util;

import com.google.android.gms.ads.AdListener;

import timber.log.Timber;

/**
 * Created by mohax on 24.09.2017.
 * <p>
 * for ScpCore
 */
public class MyAdmobNativeAdListener extends AdListener {
    @Override
    public void onAdClosed() {
        super.onAdClosed();
        Timber.d("onAdClosed");
    }

    @Override
    public void onAdFailedToLoad(int i) {
        super.onAdFailedToLoad(i);
        Timber.d("onAdFailedToLoad: %s", i);
    }

    @Override
    public void onAdLeftApplication() {
        super.onAdLeftApplication();
        Timber.d("onAdLeftApplication");
    }

    @Override
    public void onAdOpened() {
        super.onAdOpened();
        Timber.d("onAdOpened");
    }

    @Override
    public void onAdLoaded() {
        super.onAdLoaded();
        Timber.d("onAdLoaded");
    }

    @Override
    public void onAdClicked() {
        super.onAdClicked();
        Timber.d("onAdClicked");
    }

    @Override
    public void onAdImpression() {
        super.onAdImpression();
        Timber.d("onAdImpression");
    }
}