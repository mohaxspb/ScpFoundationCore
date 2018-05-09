package ru.kuchanov.scpcore.ui.holder;

import com.appodeal.ads.Appodeal;
import com.appodeal.ads.NativeAd;
import com.appodeal.ads.NativeMediaView;
import com.appodeal.ads.native_ad.views.NativeAdViewContentStream;

import org.jetbrains.annotations.NotNull;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.Constants;
import ru.kuchanov.scpcore.R2;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.monetization.model.ScpArtAd;
import ru.kuchanov.scpcore.ui.adapter.ArticlesListAdapter;
import ru.kuchanov.scpcore.ui.util.SetTextViewHTML;
import timber.log.Timber;

/**
 * Created by mohax on 22.09.2017.
 * <p>
 * for ScpCore
 */
public class NativeAdsArticleListHolder extends RecyclerView.ViewHolder {

    @Inject
    protected MyPreferenceManager mMyPreferenceManager;

    private ArticlesListAdapter.ArticleClickListener mArticleClickListener;

    @Nullable
    @BindView(R2.id.nativeAdViewContainer)
    View nativeAdViewContainer;

    @BindView(R2.id.container)
    ViewGroup container;

    @BindView(R2.id.appodealNativeAdViewAppWall)
    NativeAdViewContentStream appodealNativeAdView;

    @BindView(R2.id.appodealNativeMediaView)
    NativeMediaView appodealNativeMediaView;

    private SetTextViewHTML.TextItemsClickListener clickListener;

    @OnClick(R2.id.adsSettingsContainer)
    void onAdsSettingsClick() {
        if (mArticleClickListener != null) {
            mArticleClickListener.onAdsSettingsClick();
        }
        if (clickListener != null) {
            clickListener.onAdsSettingsClick();
        }
    }

    @OnClick(R2.id.rewardedVideoContainer)
    void onRewardedVideoClick() {
        if (mArticleClickListener != null) {
            mArticleClickListener.onRewardedVideoClick();
        }
        if (clickListener != null) {
            clickListener.onRewardedVideoClick();
        }
    }

    public NativeAdsArticleListHolder(final View itemView, final ArticlesListAdapter.ArticleClickListener clickListener) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        BaseApplication.getAppComponent().inject(this);

        mArticleClickListener = clickListener;
    }

    public NativeAdsArticleListHolder(final View itemView, final SetTextViewHTML.TextItemsClickListener clickListener) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        BaseApplication.getAppComponent().inject(this);

        this.clickListener = clickListener;
    }

    public void bind(@NotNull final ScpArtAd scpArtAd) {
        //todo
    }

    public void bind(final int appodealAdIndex) {
        Timber.d("appodealAdIndex: %s", appodealAdIndex);

        final List<NativeAd> nativeAdsList = Appodeal.getNativeAds(Constants.NUM_OF_NATIVE_ADS_PER_SCREEN);
        Timber.d("nativeAdsList.size(): %s", nativeAdsList.size());
        if (nativeAdsList.size() <= appodealAdIndex) {
            Timber.d("No appodeal ads loaded yet for index: %s", appodealAdIndex);
            return;
        }
        final NativeAd nativeAd = nativeAdsList.get(appodealAdIndex);
        if (nativeAd.containsVideo()) {
            appodealNativeMediaView.setVisibility(View.VISIBLE);
            appodealNativeAdView.setVisibility(View.GONE);
            nativeAd.setNativeMediaView(appodealNativeMediaView);
        } else {
            appodealNativeMediaView.setVisibility(View.GONE);
            appodealNativeAdView.setVisibility(View.VISIBLE);
            appodealNativeAdView.setNativeAd(nativeAd);
        }
    }
}