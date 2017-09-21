package ru.kuchanov.scpcore.ui.holder;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.ads.NativeExpressAdView;
import com.google.android.gms.ads.VideoOptions;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.util.Random;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.Constants;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.R2;
import ru.kuchanov.scpcore.db.model.Article;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.monetization.util.AdMobHelper;
import ru.kuchanov.scpcore.ui.adapter.ArticlesListRecyclerAdapter;
import ru.kuchanov.scpcore.util.AttributeGetter;
import ru.kuchanov.scpcore.util.DimensionUtils;
import timber.log.Timber;
import uk.co.chrisjenx.calligraphy.CalligraphyUtils;

/**
 * Created by mohax on 11.06.2017.
 * <p>
 * for ScpFoundationRu
 */
public class HolderMin extends RecyclerView.ViewHolder {

    @Inject
    protected MyPreferenceManager mMyPreferenceManager;

    ArticlesListRecyclerAdapter.ArticleClickListener mArticleClickListener;

    protected Article mData;

    @BindView(R2.id.favorite)
    ImageView favorite;
    @BindView(R2.id.read)
    ImageView read;
    @BindView(R2.id.offline)
    ImageView offline;
    @BindView(R2.id.title)
    TextView title;
    @BindView(R2.id.preview)
    TextView preview;

    @BindView(R2.id.typeIcon)
    ImageView typeIcon;

//    @BindView(R2.id.nativeAdViewContainer)
//    CardView nativeAdViewContainer;
//    @BindView(R2.id.nativeAdView)
//    NativeExpressAdView nativeExpressAdView;

    public HolderMin(View itemView, ArticlesListRecyclerAdapter.ArticleClickListener clickListener) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        BaseApplication.getAppComponent().inject(this);

        mArticleClickListener = clickListener;
    }

    public void bind(Article article) {
        this.mData = article;
        Context context = itemView.getContext();

        float uiTextScale = mMyPreferenceManager.getUiTextScale();
        int textSizePrimary = context.getResources().getDimensionPixelSize(R.dimen.text_size_primary);

        CalligraphyUtils.applyFontToTextView(context, title, mMyPreferenceManager.getFontPath());
        CalligraphyUtils.applyFontToTextView(context, preview, mMyPreferenceManager.getFontPath());

        itemView.setOnClickListener(v -> mArticleClickListener.onArticleClicked(article, getAdapterPosition()));

        title.setTextSize(TypedValue.COMPLEX_UNIT_PX, uiTextScale * textSizePrimary);
        title.setText(Html.fromHtml(article.title));

        //(отмечание прочитанного)
        int readIconId;
        int readColorId;
        if (article.isInReaden) {
            readColorId = AttributeGetter.getColor(context, R.attr.readTextColor);
            readIconId = AttributeGetter.getDrawableId(context, R.attr.readIconUnselected);
        } else {
            readColorId = AttributeGetter.getColor(context, R.attr.newArticlesTextColor);
            readIconId = AttributeGetter.getDrawableId(context, R.attr.readIcon);
        }
        title.setTextColor(readColorId);
        read.setImageResource(readIconId);
        read.setOnClickListener(v -> mArticleClickListener.toggleReadenState(article));
        //(отмтка избранных статей)
        int favsIconId;
        if (article.isInFavorite != Article.ORDER_NONE) {
            favsIconId = AttributeGetter.getDrawableId(context, R.attr.favoriteIcon);
        } else {
            favsIconId = AttributeGetter.getDrawableId(context, R.attr.favoriteIconUnselected);
        }
        favorite.setImageResource(favsIconId);

        //Кнопки Offline
        int offlineIconId;
        if (article.text != null) {
            offlineIconId = AttributeGetter.getDrawableId(context, R.attr.iconOfflineRemove);
        } else {
            offlineIconId = AttributeGetter.getDrawableId(context, R.attr.iconOfflineAdd);
        }
        offline.animate().cancel();
        offline.setRotation(0f);
        offline.setImageResource(offlineIconId);
        offline.setOnClickListener(v -> {
            if (mArticleClickListener != null) {
                if (article.text != null) {
                    PopupMenu popup = new PopupMenu(context, offline);
                    popup.getMenu().add(0, 0, 0, R.string.delete);
                    popup.setOnMenuItemClickListener(item -> {
                        mArticleClickListener.onOfflineClicked(article);
                        return true;
                    });
                    popup.show();
                } else {
                    mArticleClickListener.onOfflineClicked(article);
                }
            }
        });

        if (context.getResources().getBoolean(R.bool.filter_by_type_enabled)) {
            setTypesIcons(article);
        } else {
            typeIcon.setVisibility(View.GONE);
        }

        //native ads
//        showNativeAds();
    }

    protected void setTypesIcons(Article article) {
        switch (article.type) {
            case Article.ObjectType.NONE:
                typeIcon.setImageResource(R.drawable.ic_none_small);
                break;
            case Article.ObjectType.NEUTRAL_OR_NOT_ADDED:
                typeIcon.setImageResource(R.drawable.ic_not_add_small);
                break;
            case Article.ObjectType.SAFE:
                typeIcon.setImageResource(R.drawable.ic_safe_small);
                break;
            case Article.ObjectType.EUCLID:
                typeIcon.setImageResource(R.drawable.ic_euclid_small);
                break;
            case Article.ObjectType.KETER:
                typeIcon.setImageResource(R.drawable.ic_keter_small);
                break;
            case Article.ObjectType.THAUMIEL:
                typeIcon.setImageResource(R.drawable.ic_thaumiel_small);
                break;
            default:
                throw new IllegalArgumentException("unexpected article type: " + article.type);
        }
    }

    public void setShouldShowPreview(boolean shouldShowPreview) {
        Context context = itemView.getContext();
        float uiTextScale = mMyPreferenceManager.getUiTextScale();
        int textSizeTertiary = context.getResources().getDimensionPixelSize(R.dimen.text_size_tertiary);
        //show preview only on siteSearch fragment
        if (shouldShowPreview) {
            preview.setVisibility(View.VISIBLE);
            preview.setTextSize(TypedValue.COMPLEX_UNIT_PX, uiTextScale * textSizeTertiary);
            preview.setText(Html.fromHtml(mData.preview));
        } else {
            preview.setVisibility(View.GONE);
        }
    }

    public void setShouldShowPopupOnFavoriteClick(boolean shouldShowPopupOnFavoriteClick) {
        Context context = itemView.getContext();
        favorite.setOnClickListener(v -> {
            if (shouldShowPopupOnFavoriteClick && mData.isInFavorite != Article.ORDER_NONE) {
                PopupMenu popup = new PopupMenu(context, favorite);
                popup.getMenu().add(0, 0, 0, R.string.delete);
                popup.setOnMenuItemClickListener(item -> {
                    mArticleClickListener.toggleFavoriteState(mData);
                    return true;
                });
                popup.show();
            } else {
                mArticleClickListener.toggleFavoriteState(mData);
            }
        });
    }

//    private void showNativeAds() {
//        if (getAdapterPosition() == RecyclerView.NO_POSITION) {
//            nativeAdViewContainer.setMinimumHeight(0);
//            nativeAdViewContainer.setVisibility(View.GONE);
//            return;
//        }
//        FirebaseRemoteConfig config = FirebaseRemoteConfig.getInstance();
//        int nativeAdsInterval = (int) config.getLong(Constants.Firebase.RemoteConfigKeys.NATIVE_ADS_LISTS_INTERVAL);
//        if (getAdapterPosition() != 0
//                //maybe we need to check for NO_POSITION
////                && getAdapterPosition() != RecyclerView.NO_POSITION
//                && config.getBoolean(Constants.Firebase.RemoteConfigKeys.MAIN_BANNER_DISABLED)
//                //check if we show more that 3 ads per list and prevent it as admob forbids it
//                && getAdapterPosition() <= nativeAdsInterval * Constants.NUM_OF_NATIVE_ADS_PER_SCREEN
//                && config.getBoolean(Constants.Firebase.RemoteConfigKeys.NATIVE_ADS_LISTS_ENABLED)
//                && getAdapterPosition() % (nativeAdsInterval - 1) == 0
//                && !mMyPreferenceManager.isHasAnySubscription()) {
//            Timber.d("show native ads: %s", getAdapterPosition());
//
//            nativeAdViewContainer.setMinimumHeight(DimensionUtils.dpToPx(Constants.NATIVE_ADS_MIN_HEIGHT));
//
//            //check native source and choose correct one
//            int nativeAdsSource = (int) config.getLong(Constants.Firebase.RemoteConfigKeys.NATIVE_ADS_LISTS_SOURCE);
//            switch (nativeAdsSource) {
//                case Constants.NativeAdsSource.ALL:
//                    //show ads from list of sources via random
//                    switch (new Random().nextInt(Constants.NUM_OF_NATIVE_ADS_SOURCES) + 1/*for all one*/) {
//                        case Constants.NativeAdsSource.AD_MOB:
//                            showAdMobNativeAds();
//                            break;
//                        case Constants.NativeAdsSource.APPODEAL:
//                            showAppodealNativeAds();
//                            break;
//                        default:
//                            throw new IllegalArgumentException("unexpected native ads source: " + nativeAdsSource);
//                    }
//                    break;
//                case Constants.NativeAdsSource.AD_MOB:
//                    showAdMobNativeAds();
//                    break;
//                case Constants.NativeAdsSource.APPODEAL:
//                    showAppodealNativeAds();
//                    break;
//                default:
//                    throw new IllegalArgumentException("unexpected native ads source: " + nativeAdsSource);
//            }
//        } else {
//            nativeAdViewContainer.setMinimumHeight(0);
//            nativeAdViewContainer.setVisibility(View.GONE);
//        }
//    }
//
//    private void showAppodealNativeAds() {
//        //TODO
//        System.out.println("fd");
//    }
//
//    private void showAdMobNativeAds() {
//        nativeAdViewContainer.setVisibility(View.VISIBLE);
//        // Set its video options.
//        nativeExpressAdView.setVideoOptions(new VideoOptions.Builder()
//                .setStartMuted(true)
//                .build());
//
//        nativeExpressAdView.loadAd(AdMobHelper.buildAdRequest(itemView.getContext()));
//    }
}