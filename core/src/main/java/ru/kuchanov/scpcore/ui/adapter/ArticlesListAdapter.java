package ru.kuchanov.scpcore.ui.adapter;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import android.annotation.SuppressLint;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collection;
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
import ru.kuchanov.scpcore.ui.dialog.SettingsBottomSheetDialogFragment;
import ru.kuchanov.scpcore.ui.holder.HolderMax;
import ru.kuchanov.scpcore.ui.holder.HolderMedium;
import ru.kuchanov.scpcore.ui.holder.HolderMin;
import ru.kuchanov.scpcore.ui.holder.NativeAdsArticleListHolder;
import ru.kuchanov.scpcore.ui.model.ArticlesListModel;

import static ru.kuchanov.scpcore.Constants.Firebase.RemoteConfigKeys.NATIVE_ADS_LISTS_INTERVAL;
import static ru.kuchanov.scpcore.Constants.Firebase.RemoteConfigKeys.NATIVE_ADS_LISTS_SOURCE;

/**
 * Created by Dante on 17.01.2016.
 * <p>
 * for scp_ru
 */
public class ArticlesListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

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

        SortType(final int title) {
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
    @IntDef({ArticleListNodeType.ARTICLE, ArticleListNodeType.NATIVE_ADS_APPODEAL})
    public @interface ArticleListNodeType {
        int ARTICLE = 0;
        int NATIVE_ADS_APPODEAL = 2;
    }

    @Inject
    MyPreferenceManager mMyPreferenceManager;

    protected List<Article> mData;

    protected List<Article> mSortedWithFilterData = new ArrayList<>();

    private final List<ArticlesListModel> mAdsModelsList = new ArrayList<>();
    private final List<ArticlesListModel> mArticlesAndAds = new ArrayList<>();

    protected SortType mSortType = SortType.NONE;

    private ArticleClickListener mArticleClickListener;
    private boolean shouldShowPopupOnFavoriteClick;
    private boolean shouldShowPreview;

    public ArticlesListAdapter() {
        super();
        BaseApplication.getAppComponent().inject(this);
    }

    public List<Article> getDisplayedData() {
        return mSortedWithFilterData;
    }

    public void setData(final List<Article> data) {
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

    public void sortByType(final SortType sortType) {
//        Timber.d("sortByType: %s", sortType);
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
                final List<Article> sortedByRatingArts = new ArrayList<>(mData);
                Collections.sort(sortedByRatingArts, Article.COMPARATOR_DATE_RATING);
                mSortedWithFilterData.addAll(sortedByRatingArts);
                break;
            case TITLE:
                final List<Article> sortedByTitleArts = new ArrayList<>(mData);
                Collections.sort(sortedByTitleArts, Article.COMPARATOR_TITLE);
                mSortedWithFilterData.addAll(sortedByTitleArts);
                break;
            case NOT_READ:
                final Collection<Article> sortedByReadStateArts = new ArrayList<>();
                for (final Article article : mData) {
                    if (!article.isInReaden) {
                        sortedByReadStateArts.add(article);
                    }
                }
                mSortedWithFilterData.addAll(sortedByReadStateArts);
                break;
            case READ:
                final Collection<Article> sortedByNonReadStateArts = new ArrayList<>();
                for (final Article article : mData) {
                    if (article.isInReaden) {
                        sortedByNonReadStateArts.add(article);
                    }
                }
                mSortedWithFilterData.addAll(sortedByNonReadStateArts);
                break;
            case NEUTRAL_OR_NOT_ADDED:
                final Collection<Article> neutralOrNotAdded = new ArrayList<>();
                for (final Article article : mData) {
                    if (article.type.equals(Article.ObjectType.NEUTRAL_OR_NOT_ADDED)) {
                        neutralOrNotAdded.add(article);
                    }
                }
                mSortedWithFilterData.addAll(neutralOrNotAdded);
                break;
            case EUCLID:
                final Collection<Article> euclid = new ArrayList<>();
                for (final Article article : mData) {
                    if (article.type.equals(Article.ObjectType.EUCLID)) {
                        euclid.add(article);
                    }
                }
                mSortedWithFilterData.addAll(euclid);
                break;
            case KETER:
                final Collection<Article> keter = new ArrayList<>();
                for (final Article article : mData) {
                    if (article.type.equals(Article.ObjectType.KETER)) {
                        keter.add(article);
                    }
                }
                mSortedWithFilterData.addAll(keter);
                break;
            case SAFE:
                final Collection<Article> safe = new ArrayList<>();
                for (final Article article : mData) {
                    if (article.type.equals(Article.ObjectType.SAFE)) {
                        safe.add(article);
                    }
                }
                mSortedWithFilterData.addAll(safe);
                break;
            case THAUMIEL:
                final Collection<Article> thaumiel = new ArrayList<>();
                for (final Article article : mData) {
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
        for (final Article article : getDisplayedData()) {
            mArticlesAndAds.add(new ArticlesListModel(ArticleListNodeType.ARTICLE, article));
        }
        //do not add native ads items if user has subscription or banners temporary disabled
        //or banners rnabled or native disabled
        final FirebaseRemoteConfig config = FirebaseRemoteConfig.getInstance();
// /        if (mMyPreferenceManager.isHasAnySubscription()
//                || !mMyPreferenceManager.isTimeToShowBannerAds()
//                || !config.getBoolean(Constants.Firebase.RemoteConfigKeys.MAIN_BANNER_DISABLED)
//                || !config.getBoolean(Constants.Firebase.RemoteConfigKeys.NATIVE_ADS_LISTS_ENABLED)) {
//            return;
//        }
        if (mMyPreferenceManager.isHasAnySubscription()
                || !mMyPreferenceManager.isTimeToShowBannerAds()
                || mMyPreferenceManager.isBannerInArticlesListsEnabled()) {
            return;
        }
        if (mAdsModelsList.isEmpty()) {
            mAdsModelsList.addAll(createAdsModelsList());
        }

        // Loop through the items array and place a new Native Express ad in every ith position in
        // the items List.
//        int appodealIndex = 0;
        final int interval = (int) (config.getLong(NATIVE_ADS_LISTS_INTERVAL) - 1);
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
        final List<ArticlesListModel> adsModelsList = new ArrayList<>();

        final FirebaseRemoteConfig config = FirebaseRemoteConfig.getInstance();
        @Constants.NativeAdsSource final int nativeAdsSource = (int) config.getLong(NATIVE_ADS_LISTS_SOURCE);
        //test
//        nativeAdsSource = Constants.NativeAdsSource.APPODEAL;
        int appodealIndex = 0;
        for (int i = 0; i < Constants.NUM_OF_NATIVE_ADS_PER_SCREEN; i++) {
            switch (nativeAdsSource) {
                case Constants.NativeAdsSource.ALL: {
                    //show ads from list of sources via random
                    switch (new Random().nextInt(Constants.NUM_OF_NATIVE_ADS_SOURCES) + 1) {
                        case Constants.NativeAdsSource.APPODEAL:
                            adsModelsList.add(new ArticlesListModel(ArticleListNodeType.NATIVE_ADS_APPODEAL, appodealIndex));
                            appodealIndex++;
                            break;
                        default:
                            throw new IllegalArgumentException("unexpected native ads source: " + nativeAdsSource);
                    }
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
    public long getItemId(final int position) {
        //create correct ID, as we now have ads in list
        return mArticlesAndAds.get(position).data.hashCode();
    }

    @ArticleListNodeType
    @Override
    public int getItemViewType(final int position) {
        //we must create viewType for article and native ads
        return mArticlesAndAds.get(position).type;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(
            @NonNull final ViewGroup parent,
            @ArticleListNodeType final int viewType
    ) {
        final RecyclerView.ViewHolder viewHolder;
        final View view;

        //switch by viewType for create native ads viewHolder
        switch (viewType) {
            case ArticleListNodeType.ARTICLE:
                @SettingsBottomSheetDialogFragment.ListItemType final String listDesignType = mMyPreferenceManager.getListDesignType();
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
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_native_container, parent, false);
                viewHolder = new NativeAdsArticleListHolder(view, mArticleClickListener);
                break;
            default:
                throw new IllegalArgumentException("unexpected viewType: " + viewType);
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        @ArticleListNodeType final int viewType = getItemViewType(position);
        switch (viewType) {
            case ArticleListNodeType.ARTICLE:
                final HolderMin holderArticle = (HolderMin) holder;
                holderArticle.bind((Article) mArticlesAndAds.get(position).data);
                holderArticle.setShouldShowPreview(shouldShowPreview);
                holderArticle.setShouldShowPopupOnFavoriteClick(shouldShowPopupOnFavoriteClick);
                break;
            case ArticleListNodeType.NATIVE_ADS_APPODEAL:
                final NativeAdsArticleListHolder nativeAdsAppodealHolder = (NativeAdsArticleListHolder) holder;
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

    public void setArticleClickListener(final ArticleClickListener articleClickListener) {
        mArticleClickListener = articleClickListener;
    }

    public void setShouldShowPopupOnFavoriteClick(final boolean show) {
        shouldShowPopupOnFavoriteClick = show;
    }

    public void setShouldShowPreview(final boolean show) {
        shouldShowPreview = show;
    }

    public interface ArticleClickListener {

        void onArticleClick(Article article);

        void toggleReadenState(Article article);

        void toggleFavoriteState(Article article);

        void onOfflineClick(Article article);

        void onTagClick(ArticleTag tag);

        void onAdsSettingsClick();

        void onRewardedVideoClick();

        //todo add listeners for native ads clicks - we'll use it to mesure banner/native effectivnes
    }
}