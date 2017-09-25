package ru.kuchanov.scpcore.monetization.util;

import com.appodeal.ads.Appodeal;
import com.appodeal.ads.NativeAd;
import com.appodeal.ads.NativeCallbacks;

import ru.kuchanov.scpcore.Constants;
import timber.log.Timber;

/**
 * Created by mohax on 20.09.2017.
 * <p>
 * for ScpCore
 */
public class MyAppodealNativeCallbacks implements NativeCallbacks {
    @Override
    public void onNativeLoaded() {
        Timber.d("onNativeLoaded size");// %s", Appodeal.getNativeAds(Constants.NUM_OF_NATIVE_ADS_PER_SCREEN).size());
    }

    @Override
    public void onNativeFailedToLoad() {
        Timber.d("onNativeFailedToLoad");
    }

    @Override
    public void onNativeShown(NativeAd nativeAd) {
        Timber.d("onNativeShown");
    }

    @Override
    public void onNativeClicked(NativeAd nativeAd) {
        Timber.d("onNativeClicked");
    }
}