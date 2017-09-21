package ru.kuchanov.scpcore.ui.holder;

import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.google.android.gms.ads.NativeExpressAdView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.Constants;
import ru.kuchanov.scpcore.R2;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.ui.adapter.ArticlesListRecyclerAdapter;

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
    @BindView(R2.id.nativeAdView)
    NativeExpressAdView nativeExpressAdView;

    public NativeAdsArticleListHolder(View itemView, ArticlesListRecyclerAdapter.ArticleClickListener clickListener) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        BaseApplication.getAppComponent().inject(this);

        mArticleClickListener = clickListener;
    }

    public void bind(@Constants.NativeAdsSource int nativeAdSource, @Nullable NativeExpressAdView nativeExpressAdView) {
        //TODO
    }
}