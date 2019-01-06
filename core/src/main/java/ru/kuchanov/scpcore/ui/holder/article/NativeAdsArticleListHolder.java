package ru.kuchanov.scpcore.ui.holder.article;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.appodeal.ads.Appodeal;
import com.appodeal.ads.NativeAd;
import com.appodeal.ads.native_ad.views.NativeAdViewAppWall;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.BuildConfig;
import ru.kuchanov.scpcore.ConstantValues;
import ru.kuchanov.scpcore.Constants;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.R2;
import ru.kuchanov.scpcore.db.model.MyNativeBanner;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
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

    @Inject
    protected ConstantValues mConstantValues;

    private ArticlesListAdapter.ArticleClickListener mArticleClickListener;

    @BindView(R2.id.container)
    ViewGroup container;

    @Nullable
    @BindView(R2.id.nativeAdViewContainer)
    View nativeAdViewContainer;

    @BindView(R2.id.appodealNativeAdViewAppWall)
    NativeAdViewAppWall appodealNativeAdView;

    @BindView(R2.id.scpArtAdView)
    View scpArtAdView;

    @BindView(R2.id.ratingBar)
    View ratingBar;

    @BindView(R2.id.titleTextView)
    TextView titleTextView;

    @BindView(R2.id.logoImageView)
    ImageView logoImageView;

    @BindView(R2.id.subtitleTextView)
    TextView subtitleTextView;

    @BindView(R2.id.ctaTextView)
    TextView ctaTextView;

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


    public void bind() {
        Timber.d("scpQuizAds showing");
//        appodealNativeMediaView.setVisibility(View.GONE);
        appodealNativeAdView.setVisibility(View.GONE);

        scpArtAdView.setVisibility(View.VISIBLE);

        scpArtAdView.setOnClickListener(v -> {
            FirebaseAnalytics.getInstance(BaseApplication.getAppInstance()).logEvent(
                    Constants.Firebase.Analitics.EventName.SCP_QUIZ_CLICKED,
                    new Bundle()
            );
            final String url = String.format(
                    Locale.getDefault(),
                    Constants.Urls.SCP_QUIZ_MARKET_URL,
                    mConstantValues.getAppLang()
            );
            IntentUtils.openUrl(url);
        });

        ratingBar.setVisibility(View.GONE);
        logoImageView.setImageResource(R.drawable.ic_scp_quiz_logo);
        titleTextView.setText(R.string.scp_quiz_banner_title);
        subtitleTextView.setText(R.string.scp_quiz_banner_subtitle);
        ctaTextView.setText(R.string.scp_quiz_banner_cta);

        progressCenter.setVisibility(View.VISIBLE);
        Glide.with(mainImageView.getContext())
                .load(R.drawable.ic_scp_quiz_banner)
                .fitCenter()
                .crossFade()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .listener(new RequestListener<Integer, GlideDrawable>() {
                    @Override
                    public boolean onException(
                            final Exception e,
                            final Integer model,
                            final Target<GlideDrawable> target,
                            final boolean isFirstResource
                    ) {
                        Timber.e(e, "ERROR while load image for scp quiz");
                        progressCenter.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(
                            final GlideDrawable resource,
                            final Integer model,
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
        appodealNativeAdView.setVisibility(View.VISIBLE);
        final NativeAd nativeAd = nativeAdsList.get(appodealAdIndex);
        appodealNativeAdView.setNativeAd(nativeAd);
    }

    public void bind(@NotNull final MyNativeBanner scpArtAd) {
        Timber.d("scpArtAd: %s", scpArtAd);
//        appodealNativeMediaView.setVisibility(View.GONE);
        appodealNativeAdView.setVisibility(View.GONE);

        scpArtAdView.setVisibility(View.VISIBLE);

        scpArtAdView.setOnClickListener(v -> {
            FirebaseAnalytics.getInstance(BaseApplication.getAppInstance()).logEvent(
                    Constants.Firebase.Analitics.EventName.SCP_ART_CLICKED,
                    new Bundle()
            );
            IntentUtils.openUrl(scpArtAd.getRedirectUrl());
        });

        ratingBar.setVisibility(View.VISIBLE);
        Glide.with(logoImageView.getContext())
                .load(BuildConfig.SCP_READER_API_URL + scpArtAd.getLogoUrl())
                .error(R.drawable.ic_scp_art_ad_img)
                .fitCenter()
                .crossFade()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(logoImageView);

        titleTextView.setText(scpArtAd.getTitle());
        subtitleTextView.setText(scpArtAd.getSubTitle());
        ctaTextView.setText(scpArtAd.getCtaButtonText());

        progressCenter.setVisibility(View.VISIBLE);
        Glide.with(mainImageView.getContext())
                .load(BuildConfig.SCP_READER_API_URL + scpArtAd.getImageUrl())
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
}