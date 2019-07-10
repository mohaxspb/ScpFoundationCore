package ru.kuchanov.scpcore.monetization.util.admob;

import com.google.android.gms.ads.AdListener;

import javax.inject.Inject;

import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.monetization.util.InterstitialAdListener;


/**
 * Created by mohax on 15.01.2017.
 */
public class AdmobInterstitialAdListener extends AdListener implements InterstitialAdListener {

    @Inject
    MyPreferenceManager mMyPreferenceManager;

    public AdmobInterstitialAdListener() {
        super();
        BaseApplication.getAppComponent().inject(this);
    }

    /**
     * writes lastTime ads was shown to prefs. So do not forgot to call super
     * <p>
     * also increases num of shown Interstitials
     */
    @Override
    public void onAdClosed() {
        onInterstitialClosed(mMyPreferenceManager);
    }
}
