package ru.kuchanov.scpcore.ui.holder.article;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.mopub.nativeads.NativeAd;

import org.jetbrains.annotations.NotNull;

import java.util.List;

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
import ru.kuchanov.scpcore.monetization.util.mopub.MopubNativeManager;
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
    MyPreferenceManager mMyPreferenceManager;

    @Inject
    ConstantValues mConstantValues;

    @Inject
    MopubNativeManager mopubNativeManager;

    private ArticlesListAdapter.ArticleClickListener mArticleClickListener;

    @BindView(R2.id.container)
    ViewGroup container;

    @Nullable
    @BindView(R2.id.nativeAdViewContainer)
    View nativeAdViewContainer;

    @BindView(R2.id.scpNativeAdView)
    View scpNativeAdView;

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

    @BindView(R2.id.mopubNativeAdsContainer)
    ViewGroup mopubNativeAdsContainer;


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

    public NativeAdsArticleListHolder(
            final View itemView,
            final ArticlesListAdapter.ArticleClickListener clickListener
    ) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        BaseApplication.getAppComponent().inject(this);

        mArticleClickListener = clickListener;
    }

    public NativeAdsArticleListHolder(
            final View itemView,
            final SetTextViewHTML.TextItemsClickListener clickListener
    ) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        BaseApplication.getAppComponent().inject(this);

        this.clickListener = clickListener;
    }

    //fixme delete it and use bind(MyNativeBanner banner) for quiz
    @Deprecated
    public void bind() {
        Timber.d("scpQuizAds showing");

        scpNativeAdView.setVisibility(View.VISIBLE);

        scpNativeAdView.setOnClickListener(v -> {
            FirebaseAnalytics.getInstance(BaseApplication.getAppInstance()).logEvent(
                    Constants.Firebase.Analytics.EventName.SCP_QUIZ_CLICKED,
                    new Bundle()
            );
//            final String url = String.format(
//                    Locale.getDefault(),
//                    Constants.Urls.SCP_QUIZ_MARKET_URL,
//                    mConstantValues.getAppLang()
//            );
            final String url = Constants.Urls.LANDING_PAGE
                    + "?utm_source=scpReader_" + mConstantValues.getAppLang()
                    + "&utm_medium=directLink&utm_campaign=scpQuizBanner";
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
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        Timber.e(e, "ERROR while load image for scp quiz");
                        progressCenter.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        progressCenter.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(mainImageView);
    }

    public void bind(final int nativeAdIndex) {
        Timber.d("nativeAdIndex: %s", nativeAdIndex);

        final List<NativeAd> nativeAdsList = mopubNativeManager.getNativeAds();
//        Timber.d("nativeAdsList.size(): %s", nativeAdsList.size());
        if (nativeAdsList.size() <= nativeAdIndex) {
            Timber.d("No native ads loaded yet for index: %s", nativeAdIndex);
            return;
        }
        scpNativeAdView.setVisibility(View.GONE);
        mopubNativeAdsContainer.setVisibility(View.VISIBLE);
        final NativeAd nativeAd = nativeAdsList.get(nativeAdIndex);
        View convertView = mopubNativeAdsContainer.getChildAt(0);
        View renderedNativeAd = mopubNativeManager.getRenderedNativeAdsView(
                nativeAd,
                mopubNativeAdsContainer,
                convertView
        );
        if (convertView != null) {
            mopubNativeAdsContainer.removeAllViews();
        }
        mopubNativeAdsContainer.addView(renderedNativeAd);
    }

    //todo use it for quiz banners
    public void bind(@NotNull final MyNativeBanner scpNativeBanner) {
        Timber.d("scpNativeBanner: %s", scpNativeBanner);
        mopubNativeAdsContainer.setVisibility(View.GONE);

        scpNativeAdView.setVisibility(View.VISIBLE);

        scpNativeAdView.setOnClickListener(v -> {
            Timber.d("MyNativeBanner: onClick %s", scpNativeBanner);
            FirebaseAnalytics.getInstance(BaseApplication.getAppInstance()).logEvent(
                    Constants.Firebase.Analytics.EventName.SCP_NATIVE_CLICKED,
                    new Bundle()
            );
            IntentUtils.openUrl(scpNativeBanner.getRedirectUrl());
        });

        ratingBar.setVisibility(View.VISIBLE);
        Glide.with(logoImageView.getContext())
                .load(BuildConfig.SCP_READER_API_URL + scpNativeBanner.getLogoUrl())
                .error(R.drawable.ic_scp_ad_img)
                .fitCenter()
                .into(logoImageView);

        titleTextView.setText(scpNativeBanner.getTitle());
        subtitleTextView.setText(scpNativeBanner.getSubTitle());
        ctaTextView.setText(scpNativeBanner.getCtaButtonText());

        progressCenter.setVisibility(View.VISIBLE);
        Timber.d("imageUrl: %s%s", BuildConfig.SCP_READER_API_URL, scpNativeBanner.getImageUrl());
        Glide.with(mainImageView.getContext())
                .load(BuildConfig.SCP_READER_API_URL + scpNativeBanner.getImageUrl())
                .error(R.drawable.ic_scp_quiz_banner)
                .fitCenter()
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        Timber.e(e, "ERROR while load image for scp art");
                        progressCenter.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        progressCenter.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(mainImageView);
    }
}
