package ru.kuchanov.scpcore.ui.holder;

import com.google.firebase.analytics.FirebaseAnalytics;

import com.appodeal.ads.Appodeal;
import com.appodeal.ads.NativeAd;
import com.appodeal.ads.NativeMediaView;
import com.appodeal.ads.native_ad.views.NativeAdViewContentStream;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import org.jetbrains.annotations.NotNull;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.Constants;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.R2;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.monetization.model.ScpArtAdsJson;
import ru.kuchanov.scpcore.ui.adapter.ArticlesListAdapter;
import ru.kuchanov.scpcore.ui.util.SetTextViewHTML;
import ru.kuchanov.scpcore.util.IntentUtils;
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

    @BindView(R2.id.container)
    ViewGroup container;

    @Nullable
    @BindView(R2.id.nativeAdViewContainer)
    View nativeAdViewContainer;

    @BindView(R2.id.appodealNativeAdViewAppWall)
    NativeAdViewContentStream appodealNativeAdView;

    @BindView(R2.id.appodealNativeMediaView)
    NativeMediaView appodealNativeMediaView;

    @BindView(R2.id.scpArtAdView)
    View scpArtAdView;

    @BindView(R2.id.mainImageView)
    ImageView mainImageView;

    @BindView(R2.id.progressCenter)
    ProgressBar progressCenter;

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

    public void bind(@NotNull final ScpArtAdsJson.ScpArtAd scpArtAd) {
        Timber.d("scpArtAd: %s", scpArtAd);
        appodealNativeMediaView.setVisibility(View.GONE);
        appodealNativeAdView.setVisibility(View.GONE);

        scpArtAdView.setVisibility(View.VISIBLE);

        final Context context = mainImageView.getContext();
        scpArtAdView.setOnClickListener(v -> {
            FirebaseAnalytics.getInstance(BaseApplication.getAppInstance()).logEvent(
                    Constants.Firebase.Analitics.EventName.VK_APP_SHARED,
                    new Bundle()
            );
            final String url = String.format(
                    Locale.getDefault(),
                    Constants.Urls.SCP_ART_AD_UTM,
                    context.getApplicationInfo().packageName,
                    scpArtAd.getId()
            );
            IntentUtils.openUrl(url);
        });

        progressCenter.setVisibility(View.VISIBLE);
        Glide.with(mainImageView.getContext())
                .load(scpArtAd.getImgUrl())
                .error(R.drawable.art_scp_default_ads)
                .fitCenter()
                .crossFade()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(
                            final Exception e,
                            final String model,
                            final Target<GlideDrawable> target,
                            final boolean isFirstResource
                    ) {
                        Timber.e(e, "ERROR while load image for scp art");
                        progressCenter.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(
                            final GlideDrawable resource,
                            final String model,
                            final Target<GlideDrawable> target,
                            final boolean isFromMemoryCache,
                            final boolean isFirstResource
                    ) {
                        progressCenter.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(mainImageView);
    }

    public void bind(final int appodealAdIndex) {
        Timber.d("appodealAdIndex: %s", appodealAdIndex);

        final List<NativeAd> nativeAdsList = Appodeal.getNativeAds(Constants.NUM_OF_NATIVE_ADS_PER_SCREEN);
        Timber.d("nativeAdsList.size(): %s", nativeAdsList.size());
        if (nativeAdsList.size() <= appodealAdIndex) {
            Timber.d("No appodeal ads loaded yet for index: %s", appodealAdIndex);
            return;
        }
        scpArtAdView.setVisibility(View.GONE);
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