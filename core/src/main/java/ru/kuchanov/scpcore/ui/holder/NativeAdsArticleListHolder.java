package ru.kuchanov.scpcore.ui.holder;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.appodeal.ads.Appodeal;
import com.appodeal.ads.NativeAd;
import com.appodeal.ads.NativeMediaView;
import com.appodeal.ads.native_ad.views.NativeAdViewAppWall;
import com.google.android.gms.ads.NativeExpressAdView;
import com.google.android.gms.ads.VideoOptions;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.Constants;
import ru.kuchanov.scpcore.R2;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.ui.adapter.ArticlesListRecyclerAdapter;
import timber.log.Timber;

/**
 * Created by mohax on 22.09.2017.
 * <p>
 * for ScpCore
 */
public class NativeAdsArticleListHolder extends RecyclerView.ViewHolder {

    @Inject
    protected MyPreferenceManager mMyPreferenceManager;

    ArticlesListRecyclerAdapter.ArticleClickListener mArticleClickListener;

    @BindView(R2.id.nativeAdViewContainer)
    CardView nativeAdViewContainer;
    @BindView(R2.id.container)
    FrameLayout container;
    @BindView(R2.id.appodealNativeAdViewAppWall)
    NativeAdViewAppWall appodealNativeAdViewAppWall;
    @BindView(R2.id.appodealNativeMediaView)
    NativeMediaView appodealNativeMediaView;

    public NativeAdsArticleListHolder(View itemView, ArticlesListRecyclerAdapter.ArticleClickListener clickListener) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        BaseApplication.getAppComponent().inject(this);

        mArticleClickListener = clickListener;
    }

    public void bind(NativeExpressAdView nativeExpressAdView) {
        if (container.getChildAt(0) instanceof NativeExpressAdView) {
            container.removeViewAt(0);
        }
        if (nativeExpressAdView.getParent() != null) {
            ((ViewGroup) nativeExpressAdView.getParent()).removeView(nativeExpressAdView);
        }
        nativeExpressAdView.setVideoOptions(new VideoOptions.Builder()
                .setStartMuted(true)
                .build());
        container.addView(nativeExpressAdView, 0);

        appodealNativeMediaView.setVisibility(View.GONE);
        appodealNativeAdViewAppWall.setVisibility(View.GONE);
    }

    public void bind(int appodealAdIndex) {
        Timber.d("appodealAdIndex: %s", appodealAdIndex);
        View admobNativeAd = container.getChildAt(0);
        if (admobNativeAd != null) {
            if (admobNativeAd instanceof NativeExpressAdView) {
                container.removeView(admobNativeAd);
                if (admobNativeAd.getParent() != null) {
                    ((ViewGroup) admobNativeAd.getParent()).removeView(admobNativeAd);
                }
            }
        }

        List<NativeAd> nativeAdsList = Appodeal.getNativeAds(Constants.NUM_OF_NATIVE_ADS_PER_SCREEN);
        Timber.d("nativeAdsList.size(): %s", nativeAdsList.size());
        if (nativeAdsList.size() <= appodealAdIndex) {
            Timber.d("No appodeal ads loaded yet");
            return;
        }
        NativeAd nativeAd = nativeAdsList.get(appodealAdIndex);
        if (nativeAd.containsVideo()) {
            appodealNativeMediaView.setVisibility(View.VISIBLE);
            appodealNativeAdViewAppWall.setVisibility(View.GONE);
            nativeAd.setNativeMediaView(appodealNativeMediaView);
        } else {
            appodealNativeMediaView.setVisibility(View.GONE);
            appodealNativeAdViewAppWall.setVisibility(View.VISIBLE);
            appodealNativeAdViewAppWall.setNativeAd(nativeAd);
        }
    }
}