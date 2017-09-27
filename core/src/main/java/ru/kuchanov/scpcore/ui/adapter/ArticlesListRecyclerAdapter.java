package ru.kuchanov.scpcore.ui.adapter;

import android.annotation.SuppressLint;
import android.support.annotation.IntDef;
import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.ads.NativeExpressAdView;
import com.google.android.gms.ads.VideoOptions;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.inject.Inject;

import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.Constants;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.db.model.Article;
import ru.kuchanov.scpcore.db.model.ArticleTag;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.monetization.util.AdMobHelper;
import ru.kuchanov.scpcore.monetization.util.MyAdmobNativeAdListener;
import ru.kuchanov.scpcore.ui.dialog.SettingsBottomSheetDialogFragment;
import ru.kuchanov.scpcore.ui.holder.HolderMax;
import ru.kuchanov.scpcore.ui.holder.HolderMedium;
import ru.kuchanov.scpcore.ui.holder.HolderMin;
import ru.kuchanov.scpcore.ui.holder.NativeAdsArticleListHolder;
import ru.kuchanov.scpcore.ui.model.ArticlesListModel;
import timber.log.Timber;

import static ru.kuchanov.scpcore.Constants.Firebase.RemoteConfigKeys.NATIVE_ADS_LISTS_INTERVAL;
import static ru.kuchanov.scpcore.Constants.Firebase.RemoteConfigKeys.NATIVE_ADS_LISTS_SOURCE;

/**
 * Created by Dante on 17.01.2016.
 * <p>
 * for scp_ru
 */
public class ArticlesListRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public SortType getSortType() {
        return mSortType;
    }

    public enum SortType {

        NONE(R.string.filter_none),

        NEUTRAL_OR_NOT_ADDED(R.string.filter_neutral_or_not_added),
        SAFE(R.string.filter_save),
        EUCLID(R.string.filter_euclid),
        KETER(R.string.filter_keter),
        THAUMIEL(R.string.filter_thaumiel),

        NOT_READ(R.string.filter_not_read),
        READ(R.string.filter_read),
        RATING(R.string.filter_rating),
        TITLE(R.string.filter_title);

        @StringRes
        private final int title;

        SortType(int title) {
            this.title = title;
        }

        public int getTitle() {
            return title;
        }

        @Override
        public String toString() {
            return BaseApplication.getAppInstance().getString(title);
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ArticleListNodeType.ARTICLE, ArticleListNodeType.NATIVE_ADS_AD_MOB, ArticleListNodeType.NATIVE_ADS_APPODEAL})
    public @interface ArticleListNodeType {
        int ARTICLE = 0;
        int NATIVE_ADS_AD_MOB = 1;
        int NATIVE_ADS_APPODEAL = 2;
    }

    @Inject
    MyPreferenceManager mMyPreferenceManager;

    protected List<Article> mData;

    protected List<Article> mSortedWithFilterData = new ArrayList<>();

    private List<ArticlesListModel> mAdsModelsList = new ArrayList<>();
    private List<ArticlesListModel> mArticlesAndAds = new ArrayList<>();

    protected SortType mSortType = SortType.NONE;

    private ArticleClickListener mArticleClickListener;
    private boolean shouldShowPopupOnFavoriteClick;
    private boolean shouldShowPreview;

    public ArticlesListRecyclerAdapter() {
        BaseApplication.getAppComponent().inject(this);
    }

    public List<Article> getDisplayedData() {
        return mSortedWithFilterData;
    }

    public void setData(List<Article> data) {
        mData = data;
        sortByType(mSortType);

        //add native ads to result data list
        createDataWithAdsAndArticles();

        notifyDataSetChanged();

//        Timber.d("mAdsModelsList: %s", mAdsModelsList);
//        for (ArticlesListModel model : mAdsModelsList) {
//            Timber.d("type: %s/%s", model.type, model.data);
//        }
    }

    public void sortByType(SortType sortType) {
        Timber.d("sortByType: %s", sortType);
        mSortType = sortType;
        if (mData == null) {
            return;
        }

        mSortedWithFilterData.clear();

        switch (mSortType) {
            case NONE:
                mSortedWithFilterData.addAll(mData);
                break;
//            case DATE_CREATED:
//                List<Article> sortedByDateCreatedArts = new ArrayList<>(mData);
//                Collections.sort(sortedByDateCreatedArts, Article.COMPARATOR_DATE_CREATED);
//                mSortedWithFilterData.addAll(sortedByDateCreatedArts);
//                break;
//            case DATE_UPDATED:
//                List<Article> sortedByDateUpdatedArts = new ArrayList<>(mData);
//                Collections.sort(sortedByDateUpdatedArts, Article.COMPARATOR_DATE_UPDATED);
//                mSortedWithFilterData.addAll(sortedByDateUpdatedArts);
//                break;
            case RATING:
                List<Article> sortedByRatingArts = new ArrayList<>(mData);
                Collections.sort(sortedByRatingArts, Article.COMPARATOR_DATE_RATING);
                mSortedWithFilterData.addAll(sortedByRatingArts);
                break;
            case TITLE:
                List<Article> sortedByTitleArts = new ArrayList<>(mData);
                Collections.sort(sortedByTitleArts, Article.COMPARATOR_TITLE);
                mSortedWithFilterData.addAll(sortedByTitleArts);
                break;
            case NOT_READ:
                List<Article> sortedByReadStateArts = new ArrayList<>();
                for (Article article : mData) {
                    if (!article.isInReaden) {
                        sortedByReadStateArts.add(article);
                    }
                }
                mSortedWithFilterData.addAll(sortedByReadStateArts);
                break;
            case READ:
                List<Article> sortedByNonReadStateArts = new ArrayList<>();
                for (Article article : mData) {
                    if (article.isInReaden) {
                        sortedByNonReadStateArts.add(article);
                    }
                }
                mSortedWithFilterData.addAll(sortedByNonReadStateArts);
                break;
            case NEUTRAL_OR_NOT_ADDED:
                List<Article> neutralOrNotAdded = new ArrayList<>();
                for (Article article : mData) {
                    if (article.type.equals(Article.ObjectType.NEUTRAL_OR_NOT_ADDED)) {
                        neutralOrNotAdded.add(article);
                    }
                }
                mSortedWithFilterData.addAll(neutralOrNotAdded);
                break;
            case EUCLID:
                List<Article> euclid = new ArrayList<>();
                for (Article article : mData) {
                    if (article.type.equals(Article.ObjectType.EUCLID)) {
                        euclid.add(article);
                    }
                }
                mSortedWithFilterData.addAll(euclid);
                break;
            case KETER:
                List<Article> keter = new ArrayList<>();
                for (Article article : mData) {
                    if (article.type.equals(Article.ObjectType.KETER)) {
                        keter.add(article);
                    }
                }
                mSortedWithFilterData.addAll(keter);
                break;
            case SAFE:
                List<Article> safe = new ArrayList<>();
                for (Article article : mData) {
                    if (article.type.equals(Article.ObjectType.SAFE)) {
                        safe.add(article);
                    }
                }
                mSortedWithFilterData.addAll(safe);
                break;
            case THAUMIEL:
                List<Article> thaumiel = new ArrayList<>();
                for (Article article : mData) {
                    if (article.type.equals(Article.ObjectType.THAUMIEL)) {
                        thaumiel.add(article);
                    }
                }
                mSortedWithFilterData.addAll(thaumiel);
                break;
            default:
                throw new IllegalArgumentException("unexpected type: " + mSortType);
        }

        //add native ads to result data list
        createDataWithAdsAndArticles();
    }

    @SuppressLint("InflateParams")
    protected void createDataWithAdsAndArticles() {
        mArticlesAndAds.clear();
        for (Article article : getDisplayedData()) {
            mArticlesAndAds.add(new ArticlesListModel(ArticleListNodeType.ARTICLE, article));
        }
        //do not add native ads items if user has subscription or banners temporary disabled
        //or banners rnabled or native disabled
        FirebaseRemoteConfig config = FirebaseRemoteConfig.getInstance();
        if (mMyPreferenceManager.isHasAnySubscription()
                || !mMyPreferenceManager.isTimeToShowBannerAds()
                || !config.getBoolean(Constants.Firebase.RemoteConfigKeys.MAIN_BANNER_DISABLED)
                || !config.getBoolean(Constants.Firebase.RemoteConfigKeys.NATIVE_ADS_LISTS_ENABLED)) {
            return;
        }
        if (mAdsModelsList.isEmpty()) {
            mAdsModelsList.addAll(createAdsModelsList());
        }

        // Loop through the items array and place a new Native Express ad in every ith position in
        // the items List.
//        int appodealIndex = 0;
        int interval = (int) (config.getLong(NATIVE_ADS_LISTS_INTERVAL) - 1);
        for (int i = 0; i <= getDisplayedData().size(); i += interval) {
            //do not add as first row
            if (i == 0) {
                continue;
            } else if (i / interval > Constants.NUM_OF_NATIVE_ADS_PER_SCREEN) {
                break;
            }

            mArticlesAndAds.add(i, mAdsModelsList.get((i / interval) - 1));
        }
    }

    private List<ArticlesListModel> createAdsModelsList() {
        List<ArticlesListModel> adsModelsList = new ArrayList<>();

        FirebaseRemoteConfig config = FirebaseRemoteConfig.getInstance();
        @Constants.NativeAdsSource
        int nativeAdsSource = (int) config.getLong(NATIVE_ADS_LISTS_SOURCE);
        //test
//        nativeAdsSource = Constants.NativeAdsSource.APPODEAL;
        int appodealIndex = 0;
        for (int i = 0; i < Constants.NUM_OF_NATIVE_ADS_PER_SCREEN; i++) {
            switch (nativeAdsSource) {
                case Constants.NativeAdsSource.ALL: {
                    //show ads from list of sources via random
                    switch (new Random().nextInt(Constants.NUM_OF_NATIVE_ADS_SOURCES) + 1) {
                        case Constants.NativeAdsSource.AD_MOB:
                            ArticlesListModel model;
                            @SuppressLint("InflateParams")
                            NativeExpressAdView nativeAdView = (NativeExpressAdView) LayoutInflater.from(BaseApplication.getAppInstance())
                                    .inflate(R.layout.native_ads_admob_medium, null, false);
                            nativeAdView.setVideoOptions(new VideoOptions.Builder()
                                    .setStartMuted(true)
                                    .build());
                            nativeAdView.setAdListener(new MyAdmobNativeAdListener() {
                                @Override
                                public void onAdFailedToLoad(int i) {
                                    super.onAdFailedToLoad(i);
                                    nativeAdView.setVisibility(View.GONE);
                                }
                            });
                            nativeAdView.loadAd(AdMobHelper.buildAdRequest(BaseApplication.getAppInstance()));
                            model = new ArticlesListModel(ArticleListNodeType.NATIVE_ADS_AD_MOB, nativeAdView);
                            adsModelsList.add(model);
                            break;
                        case Constants.NativeAdsSource.APPODEAL:
                            adsModelsList.add(new ArticlesListModel(ArticleListNodeType.NATIVE_ADS_APPODEAL, appodealIndex));
                            appodealIndex++;
                            break;
                        default:
                            throw new IllegalArgumentException("unexpected native ads source: " + nativeAdsSource);
                    }
                    break;
                }
                case Constants.NativeAdsSource.AD_MOB: {
                    ArticlesListModel model;
                    @SuppressLint("InflateParams")
                    NativeExpressAdView nativeAdView = (NativeExpressAdView) LayoutInflater.from(BaseApplication.getAppInstance())
                            .inflate(R.layout.native_ads_admob_medium, null, false);
                    nativeAdView.setAdListener(new MyAdmobNativeAdListener());
                    nativeAdView.loadAd(AdMobHelper.buildAdRequest(BaseApplication.getAppInstance()));
                    nativeAdView.setVideoOptions(new VideoOptions.Builder()
                            .setStartMuted(true)
                            .build());
                    model = new ArticlesListModel(ArticleListNodeType.NATIVE_ADS_AD_MOB, nativeAdView);
                    adsModelsList.add(model);
                    break;
                }
                case Constants.NativeAdsSource.APPODEAL:
                    adsModelsList.add(new ArticlesListModel(ArticleListNodeType.NATIVE_ADS_APPODEAL, appodealIndex));
                    appodealIndex++;
                    break;
                default:
                    throw new IllegalArgumentException("unexpected native ads source: " + nativeAdsSource);
            }
        }

        return adsModelsList;
    }

    @Override
    public long getItemId(int position) {
        //create correct ID, as we now have ads in list
        return mArticlesAndAds.get(position).data.hashCode();
    }

    @ArticleListNodeType
    @Override
    public int getItemViewType(int position) {
        //we must create viewType for article and native ads
        return mArticlesAndAds.get(position).type;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, @ArticleListNodeType int viewType) {
        RecyclerView.ViewHolder viewHolder;
        View view;

        //switch by viewType for create native ads viewHolder
        switch (viewType) {
            case ArticleListNodeType.ARTICLE:
                @SettingsBottomSheetDialogFragment.ListItemType
                String listDesignType = mMyPreferenceManager.getListDesignType();
                switch (listDesignType) {
                    case SettingsBottomSheetDialogFragment.ListItemType.MIN:
                        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_article_min, parent, false);
                        viewHolder = new HolderMin(view, mArticleClickListener);
                        break;
                    case SettingsBottomSheetDialogFragment.ListItemType.MIDDLE:
                        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_article_medium, parent, false);
                        viewHolder = new HolderMax(view, mArticleClickListener);
                        break;
                    case SettingsBottomSheetDialogFragment.ListItemType.MAX:
                        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_article_max, parent, false);
                        viewHolder = new HolderMedium(view, mArticleClickListener);
                        break;
                    default:
                        throw new IllegalArgumentException("unexpected ListDesignType: " + listDesignType);
                }
                break;
            case ArticleListNodeType.NATIVE_ADS_APPODEAL:
            case ArticleListNodeType.NATIVE_ADS_AD_MOB:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_native_container, parent, false);
                viewHolder = new NativeAdsArticleListHolder(view, mArticleClickListener);
                break;
//            case ArticleListNodeType.NATIVE_ADS_APPODEAL:
//                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_native_container, parent, false);
//                viewHolder = new NativeAdsArticleListHolder(view, mArticleClickListener);
//                break;
            default:
                throw new IllegalArgumentException("unexpected viewType: " + viewType);
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        @ArticleListNodeType
        int viewType = getItemViewType(position);
        switch (viewType) {
            case ArticleListNodeType.ARTICLE:
                HolderMin holderArticle = (HolderMin) holder;
                holderArticle.bind((Article) mArticlesAndAds.get(position).data);
                holderArticle.setShouldShowPreview(shouldShowPreview);
                holderArticle.setShouldShowPopupOnFavoriteClick(shouldShowPopupOnFavoriteClick);
                break;
            case ArticleListNodeType.NATIVE_ADS_AD_MOB:
                NativeAdsArticleListHolder nativeAdsHolder = (NativeAdsArticleListHolder) holder;
                nativeAdsHolder.bind((NativeExpressAdView) mArticlesAndAds.get(position).data);
                break;
            case ArticleListNodeType.NATIVE_ADS_APPODEAL:
                NativeAdsArticleListHolder nativeAdsAppodealHolder = (NativeAdsArticleListHolder) holder;
                nativeAdsAppodealHolder.bind((Integer) mArticlesAndAds.get(position).data);
                break;
            default:
                throw new IllegalArgumentException("unexpected viewType: " + viewType);
        }
    }

    @Override
    public int getItemCount() {
        return mArticlesAndAds.size();
    }

    public void setArticleClickListener(ArticleClickListener articleClickListener) {
        mArticleClickListener = articleClickListener;
    }

    public void setShouldShowPopupOnFavoriteClick(boolean show) {
        shouldShowPopupOnFavoriteClick = show;
    }

    public void setShouldShowPreview(boolean show) {
        shouldShowPreview = show;
    }

    public interface ArticleClickListener {

        void onArticleClicked(Article article);

        void toggleReadenState(Article article);

        void toggleFavoriteState(Article article);

        void onOfflineClicked(Article article);

        void onTagClicked(ArticleTag tag);

        //todo add listeners for native ads clicks - we'll use it to mesure banner/native effectivnes
    }
}