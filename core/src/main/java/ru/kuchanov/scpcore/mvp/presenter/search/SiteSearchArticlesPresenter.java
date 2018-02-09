package ru.kuchanov.scpcore.mvp.presenter.search;

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
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.mvp.contract.search.SiteSearchArticlesMvp;
import ru.kuchanov.scpcore.mvp.presenter.articleslists.BaseListArticlesPresenter;
import rx.Observable;
import rx.Subscriber;
import timber.log.Timber;

/**
 * Created by y.kuchanov on 21.12.16.
 * <p>
 * for TappAwards
 */
public class SiteSearchArticlesPresenter
        extends BaseListArticlesPresenter<SiteSearchArticlesMvp.View>
        implements SiteSearchArticlesMvp.Presenter {

    private String mQuery = "";

    private List<Article> mSearchData;

    public SiteSearchArticlesPresenter(MyPreferenceManager myPreferencesManager, DbProviderFactory dbProviderFactory, ApiClient apiClient) {
        super(myPreferencesManager, dbProviderFactory, apiClient);
    }

    @Override
    public void setQuery(String query) {
        mQuery = query;
    }

    @Override
    public List<Article> getData() {
        return mSearchData;
    }

    @Override
    protected Observable<RealmResults<Article>> getDbObservable() {
        //we fo not save search results to db
        return Observable
                .<RealmResults<Article>>empty()
                .doOnCompleted(() -> getView().showCenterProgress(false));
    }

    @Override
    protected Observable<List<Article>> getApiObservable(int offset) {
        Timber.d("getApiObservable with query: %s", mQuery);
        return mApiClient.getSearchArticles(offset, mQuery)
                .doOnSubscribe(() -> {
                    if (offset != Constants.Api.ZERO_OFFSET) {
                        getView().showSwipeProgress(false);
                        getView().enableSwipeRefresh(true);
                        getView().showBottomProgress(true);
                    }
                });
    }

    @Override
    protected Observable<Pair<Integer, Integer>> getSaveToDbObservable(List<Article> data, int offset) {
        //we do not save search results to db
        //but we need pass data to view...
        //so try to do it here
        return Observable.unsafeCreate(subscriber -> {
            if (offset == 0) {
                mSearchData = data;
            } else {
                mSearchData.addAll(data);
            }
            getView().updateData(mSearchData);
            subscriber.onNext(new Pair<>(data.size(), offset));
            subscriber.onCompleted();
        });
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