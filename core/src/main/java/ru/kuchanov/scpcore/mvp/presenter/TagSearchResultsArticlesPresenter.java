package ru.kuchanov.scpcore.mvp.presenter;

import android.util.Pair;

import java.util.List;

import io.realm.RealmResults;
import ru.kuchanov.scpcore.BaseApplication;
import ru.kuchanov.scpcore.Constants;
import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.api.ApiClient;
import ru.kuchanov.scpcore.db.DbProviderFactory;
import ru.kuchanov.scpcore.db.error.ScpNoArticleForIdError;
import ru.kuchanov.scpcore.db.model.Article;
import ru.kuchanov.scpcore.db.model.ArticleTag;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.mvp.contract.TagsSearchResultsArticlesMvp;
import rx.Observable;
import rx.Subscriber;
import timber.log.Timber;

/**
 * Created by y.kuchanov on 21.12.16.
 * <p>
 * for TappAwards
 */
public class TagSearchResultsArticlesPresenter
        extends BaseListArticlesPresenter<TagsSearchResultsArticlesMvp.View>
        implements TagsSearchResultsArticlesMvp.Presenter {

    private List<ArticleTag> mQueryTags;
    private List<Article> mSearchData;

    public TagSearchResultsArticlesPresenter(
            MyPreferenceManager myPreferencesManager,
            DbProviderFactory dbProviderFactory,
            ApiClient apiClient
    ) {
        super(myPreferencesManager, dbProviderFactory, apiClient);
    }

    @Override
    protected Observable<RealmResults<Article>> getDbObservable() {
        return Observable.<RealmResults<Article>>empty()
                .doOnCompleted(() -> {
                    if (mSearchData == null) {
                        getDataFromApi(Constants.Api.ZERO_OFFSET);
                    } else {
                        getView().showSwipeProgress(false);
                    }
                });
    }

    @Override
    protected Observable<List<Article>> getApiObservable(int offset) {
        return mApiClient.getArticlesByTags(mQueryTags);
    }

    @Override
    protected Observable<Pair<Integer, Integer>> getSaveToDbObservable(List<Article> data, int offset) {
        //we do not save search results to db
        //but we need pass data to view...
        //so try to do it here
        return Observable.unsafeCreate(subscriber -> {
            mSearchData = data;
            getView().updateData(mSearchData);
            getView().showCenterProgress(false);
            getView().showSwipeProgress(false);
            if (mSearchData.isEmpty()) {
                getView().showMessage(R.string.error_no_search_results);
            }
            subscriber.onNext(new Pair<>(data.size(), offset));
            subscriber.onCompleted();
        });
    }

    @Override
    public void setQueryTags(List<ArticleTag> queryTags) {
        mQueryTags = queryTags;
    }

    @Override
    public List<ArticleTag> getQueryTags() {
        return mQueryTags;
    }

    @Override
    public void setSearchData(List<Article> articles) {
        mSearchData = articles;
    }

    @Override
    public List<Article> getSearchData() {
        return mSearchData;
    }

    @Override
    public Subscriber<Article> getToggleFavoriteSubscriber() {
        return new Subscriber<Article>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                Timber.e(e);
                if (e instanceof ScpNoArticleForIdError) {
                    //we o not have this article in DB, so download it
                    toggleOfflineState(e.getMessage());
                    getView().showError(new Throwable(BaseApplication.getAppInstance().getString(R.string.start_download)));
                }
            }

            @Override
            public void onNext(Article result) {
                Article article = new Article();
                article.url = result.url;
                if (mSearchData.contains(article)) {
                    mSearchData.get(mSearchData.indexOf(article)).isInFavorite = result.isInFavorite;
                    getView().updateData(mSearchData);
                }
            }
        };
    }

    @Override
    public Subscriber<Article> getToggleReadSubscriber() {
        return new Subscriber<Article>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                Timber.e(e);
                if (e instanceof ScpNoArticleForIdError) {
                    //we do not have this article in DB, so download it
                    toggleOfflineState(e.getMessage());
                    getView().showError(new Throwable(BaseApplication.getAppInstance().getString(R.string.start_download)));
                }
            }

            @Override
            public void onNext(Article stringBooleanPair) {
                Article article = new Article();
                article.url = stringBooleanPair.url;
                if (mSearchData.contains(article)) {
                    mSearchData.get(mSearchData.indexOf(article)).isInReaden = stringBooleanPair.isInReaden;
                    getView().updateData(mSearchData);
                }
            }
        };
    }

    @Override
    public Subscriber<String> getDeleteArticlesTextSubscriber() {
        return new Subscriber<String>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                Timber.e(e);
                if (e instanceof ScpNoArticleForIdError) {
                    //we o not have this article in DB, so download it
                    toggleOfflineState(e.getMessage());
                    getView().showError(new Throwable(BaseApplication.getAppInstance().getString(R.string.start_download)));
                }
            }

            @Override
            public void onNext(String url) {
                Article article = new Article();
                article.url = url;
                if (mSearchData.contains(article)) {
                    mSearchData.get(mSearchData.indexOf(article)).text = null;
                    getView().updateData(mSearchData);
                }
            }
        };
    }

    @Override
    public Subscriber<Article> getDownloadArticleSubscriber() {
        return new Subscriber<Article>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                Timber.e(e);
                if (e instanceof ScpNoArticleForIdError) {
                    //we o not have this article in DB, so download it
                    toggleOfflineState(e.getMessage());
                    getView().showError(new Throwable(BaseApplication.getAppInstance().getString(R.string.start_download)));
                }
            }

            @Override
            public void onNext(Article article) {
                if (mSearchData.contains(article)) {
                    int indexOfArticle = mSearchData.indexOf(article);
                    article.preview = mSearchData.get(indexOfArticle).preview;
                    mSearchData.set(mSearchData.indexOf(article), article);
                    getView().updateData(mSearchData);
                }
            }
        };
    }
}