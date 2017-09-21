package ru.kuchanov.scpcore.ui.adapter;

import android.support.annotation.IntDef;
import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.ads.NativeExpressAdView;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.inject.Inject;

import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.Constants;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.db.model.Article;
import ru.kuchanov.scpcore.db.model.ArticleTag;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.ui.dialog.SettingsBottomSheetDialogFragment;
import ru.kuchanov.scpcore.ui.holder.HolderMax;
import ru.kuchanov.scpcore.ui.holder.HolderMedium;
import ru.kuchanov.scpcore.ui.holder.HolderMin;
import ru.kuchanov.scpcore.ui.model.ArticlesListModel;
import timber.log.Timber;

import static ru.kuchanov.scpcore.Constants.Firebase.RemoteConfigKeys.NATIVE_ADS_LISTS_INTERVAL;
import static ru.kuchanov.scpcore.Constants.Firebase.RemoteConfigKeys.NATIVE_ADS_LISTS_SOURCE;

/**
 * Created by Dante on 17.01.2016.
 * <p>
 * for scp_ru
 */
public class ArticlesListRecyclerAdapter extends RecyclerView.Adapter<HolderMin> {

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
    @interface ArticleListNodeType {
        int ARTICLE = 0;
        int NATIVE_ADS_AD_MOB = 1;
        int NATIVE_ADS_APPODEAL = 2;
    }

//    private static final int TYPE_ARTICLE = 0;
//    private static final int TYPE_NATIVE_ADS_AD_MOB = 1;
//    private static final int TYPE_NATIVE_ADS_APPODEAL = 2;

    @Inject
    MyPreferenceManager mMyPreferenceManager;

    protected List<Article> mData;

    protected List<Article> mSortedWithFilterData = new ArrayList<>();

    protected List<ArticlesListModel> mArticlesAndAds = new ArrayList<>();

    private SortType mSortType = SortType.NONE;

    private ArticleClickListener mArticleClickListener;
    boolean shouldShowPopupOnFavoriteClick;
    boolean shouldShowPreview;

    public ArticlesListRecyclerAdapter() {
        BaseApplication.getAppComponent().inject(this);
    }

    public List<Article> getDisplayedData() {
        return mSortedWithFilterData;
    }

    public void setData(List<Article> data) {
        mData = data;
        sortByType(mSortType);
    }

    public void sortByType(SortType sortType) {
        Timber.d("sortByType: %s", sortType);
        mSortType = sortType;
        if (mData == null) {
            return;
        }

        mSortedWithFilterData.clear();

        //todo add native ads to result data list
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

        createDataWithAdsAndArticles();

        notifyDataSetChanged();
    }

    private void createDataWithAdsAndArticles() {
        // Loop through the items array and place a new Native Express ad in every ith position in
        // the items List.
        FirebaseRemoteConfig config = FirebaseRemoteConfig.getInstance();
        for (int i = 0; i <= mSortedWithFilterData.size(); i += (config.getLong(NATIVE_ADS_LISTS_INTERVAL) - 1)) {
            View nativeAdView;
            @Constants.NativeAdsSource
            int nativeAdsSource = (int) config.getLong(NATIVE_ADS_LISTS_SOURCE);

            switch (nativeAdsSource) {
                case Constants.NativeAdsSource.ALL:
                    //show ads from list of sources via random
                    switch (new Random().nextInt(Constants.NUM_OF_NATIVE_ADS_SOURCES) + 1) {
                        case Constants.NativeAdsSource.AD_MOB:
                            nativeAdView = LayoutInflater.from(BaseApplication.getAppInstance()).inflate(R.layout.native_ads_admob, null, false);
                            mArticlesAndAds.add(i, new );
                            break;
                        case Constants.NativeAdsSource.APPODEAL:
                            nativeAdView = LayoutInflater.from(BaseApplication.getAppInstance()).inflate(R.layout.native_ads_appodeal, null, false);
                            break;
                        default:
                            throw new IllegalArgumentException("unexpected native ads source: " + nativeAdsSource);
                    }

                    break;
                case Constants.NativeAdsSource.AD_MOB:
                    showAdMobNativeAds();
                    break;
                case Constants.NativeAdsSource.APPODEAL:
                    showAppodealNativeAds();
                    break;
                default:
                    throw new IllegalArgumentException("unexpected native ads source: " + nativeAdsSource);
            }
        }
    }

    @Override
    public long getItemId(int position) {
        //TODO create correct ID, as we now have ads in list
        return mSortedWithFilterData.get(position).url.hashCode();
    }

    @Override
    public int getItemViewType(int position) {
        //TODO we must create viewType for article and native ads
        throw new IllegalStateException("not implemented");
//        switch (mMyPreferenceManager.getListDesignType()) {
//            case SettingsBottomSheetDialogFragment.ListItemType.MIN:
//                return TYPE_MIN;
//            default:
//            case SettingsBottomSheetDialogFragment.ListItemType.MIDDLE:
//                return TYPE_MIDDLE;
//            case SettingsBottomSheetDialogFragment.ListItemType.MAX:
//                return TYPE_MAX;
//        }
    }

    @Override
    public HolderMin onCreateViewHolder(ViewGroup parent, int viewType) {
        //todo switch by viewType for create native ads viewHolder

        HolderMin viewHolder;
        View view;

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

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(HolderMin holder, int position) {
        //todo switch by viewType for create native ads viewHolder
        holder.bind(mSortedWithFilterData.get(position));
        holder.setShouldShowPreview(shouldShowPreview);
        holder.setShouldShowPopupOnFavoriteClick(shouldShowPopupOnFavoriteClick);
    }

    @Override
    public int getItemCount() {
        return mSortedWithFilterData.size();
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

        void onArticleClicked(Article article, int position);

        void toggleReadenState(Article article);

        void toggleFavoriteState(Article article);

        void onOfflineClicked(Article article);

        void onTagClicked(ArticleTag tag);
    }
}