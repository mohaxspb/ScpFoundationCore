package ru.kuchanov.scpcore.monetization.util;


import com.appodeal.ads.InterstitialCallbacks;

import javax.inject.Inject;

import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import timber.log.Timber;

/**
 * Created by mohax on 12.03.2017.
 * <p>
 * for scp_ru
 */
public class MyAppodealInterstitialCallbacks implements InterstitialCallbacks {

    @Inject
    MyPreferenceManager mMyPreferenceManager;

    public MyAppodealInterstitialCallbacks() {
        super();
        BaseApplication.getAppComponent().inject(this);
    }

    @Override
    public void onInterstitialLoaded(final boolean b) {
//        Timber.d("onInterstitialLoaded: %s", b);
    }

    @Override
    public void onInterstitialFailedToLoad() {
//        Timber.d("onInterstitialFailedToLoad");
    }

    @Override
    public void onInterstitialShown() {
//        Timber.d("onInterstitialShown");
        mMyPreferenceManager.setLastTimeAdsShows(System.currentTimeMillis());
        mMyPreferenceManager.setNumOfInterstitialsShown(0);
    }

    @Override
    public void onInterstitialClicked() {
        //TODO pass event to analitics
//        Timber.d("onInterstitialClicked");
        mMyPreferenceManager.setLastTimeAdsShows(System.currentTimeMillis());
        mMyPreferenceManager.setNumOfInterstitialsShown(0);
    }

    @Override
    public void onInterstitialClosed() {
//        Timber.d("onInterstitialClicked");
        mMyPreferenceManager.setLastTimeAdsShows(System.currentTimeMillis());
        mMyPreferenceManager.setNumOfInterstitialsShown(0);
    }
}