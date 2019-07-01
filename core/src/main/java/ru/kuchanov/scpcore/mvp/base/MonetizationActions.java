package ru.kuchanov.scpcore.mvp.base;

import org.jetbrains.annotations.NotNull;

import ru.kuchanov.scpcore.monetization.util.InterstitialAdListener;

/**
 * Created by mohax on 15.01.2017.
 */
public interface MonetizationActions {

    void initAds();

    void showInterstitial();

    void showInterstitial(@NotNull final InterstitialAdListener adListener, boolean showVideoIfNeedAndCan);

    boolean isTimeToShowAds();

    boolean isAdsLoaded();

    void requestNewInterstitial();

    void updateOwnedMarketItems();

    void showRewardedVideo();

    void startRewardedVideoFlow();

    boolean isBannerEnabled();

    void showOfferSubscriptionPopup();
}