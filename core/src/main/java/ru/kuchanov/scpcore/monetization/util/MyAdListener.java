package ru.kuchanov.scpcore.monetization.util;

import com.google.android.gms.ads.AdListener;

import javax.inject.Inject;

import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;


/**
 * Created by mohax on 15.01.2017.
 * <p>
 * for scp_ru
 */
public class MyAdListener extends AdListener {

    @Inject
    MyPreferenceManager mMyPreferenceManager;

    public MyAdListener() {
        BaseApplication.getAppComponent().inject(this);
    }

    /**
     * writes lastTime ads was shown to prefs. So do not forgot to call super
     * <p>
     * also increases num of shown Interstitials
     */
    @Override
    public void onAdClosed() {
        mMyPreferenceManager.setLastTimeAdsShows(System.currentTimeMillis());
        mMyPreferenceManager.setNumOfInterstitialsShown(mMyPreferenceManager.getNumOfInterstitialsShown() + 1);
    }
}