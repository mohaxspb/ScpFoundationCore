package ru.kuchanov.scpcore.mvp.base;

import java.util.List;

import ru.kuchanov.scpcore.monetization.model.Item;
import ru.kuchanov.scpcore.monetization.util.MyAdListener;

/**
 * Created by mohax on 15.01.2017.
 * <p>
 * for scp_ru
 */
public interface MonetizationActions {

    void initAds();

    void showInterstitial();

    void showInterstitial(MyAdListener adListener, boolean showVideoIfNeedAndCan);

    boolean isTimeToShowAds();

    boolean isAdsLoaded();

    void requestNewInterstitial();

    void updateOwnedMarketItems(boolean forceSubsValidation);

    void showRewardedVideo();

    void startRewardedVideoFlow();

    List<Item> getOwnedItems();
}