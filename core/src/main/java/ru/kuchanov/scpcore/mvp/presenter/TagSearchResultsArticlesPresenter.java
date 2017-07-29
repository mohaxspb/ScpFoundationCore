package ru.kuchanov.scpcore.mvp.presenter;

import android.util.Pair;

import java.util.List;

import io.realm.RealmResults;
import ru.kuchanov.scpcore.api.ApiClient;
import ru.kuchanov.scpcore.db.DbProviderFactory;
import ru.kuchanov.scpcore.db.model.Article;
import ru.kuchanov.scpcore.db.model.ArticleTag;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.mvp.contract.TagsSearchResultsArticlesMvp;
import rx.Observable;

/**
 * Created by y.kuchanov on 21.12.16.
 * <p>
 * for TappAwards
 */
public class TagSearchResultsArticlesPresenter
        extends BaseListArticlesPresenter<TagsSearchResultsArticlesMvp.View>
        implements TagsSearchResultsArticlesMvp.Presenter {

    private List<ArticleTag> mQueryTags;
    private List<String> mSearchData;
//    private List<Article> mArticles;

    public TagSearchResultsArticlesPresenter(
            MyPreferenceManager myPreferencesManager,
            DbProviderFactory dbProviderFactory,
            ApiClient apiClient
    ) {
        super(myPreferencesManager, dbProviderFactory, apiClient);
    }

    @Override
    protected Observable<RealmResults<Article>> getDbObservable() {
        return mDbProviderFactory.getDbProvider().getArticlesByIds(mSearchData);
//        return Observable.<RealmResults<Article>>empty()
//                .doOnCompleted(() -> {
//                    if (mArticles == null) {
//                        getDataFromApi(Constants.Api.ZERO_OFFSET);
//                    } else {
//                        getView().showSwipeProgress(false);
//                    }
//                });
    }

    @Override
    protected Observable<List<Article>> getApiObservable(int offset) {
        return mApiClient.getArticlesByTags(mQueryTags);
    }

    @Override
    protected Observable<Pair<Integer, Integer>> getSaveToDbObservable(List<Article> data, int offset) {
//        //we do not save search results to db
//        //but we need pass data to view...
//        //so try to do it here
//        return Observable.unsafeCreate(subscriber -> {
//            mArticles = data;
//            getView().updateData(mArticles);
//            getView().showCenterProgress(false);
//            getView().showSwipeProgress(false);
//            if (mArticles.isEmpty()) {
//                getView().showMessage(R.string.error_no_search_results);
//            }
//            subscriber.onNext(new Pair<>(data.size(), offset));
//            subscriber.onCompleted();
//        });
        return mDbProviderFactory.getDbProvider().saveMultipleArticlesSync(data).flatMap(articles -> Observable.just(new Pair<>(data.size(), offset)));
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
    public void setSearchData(List<String> articles) {
        mSearchData = articles;
    }

    @Override
    public List<String> getSearchData() {
        return mSearchData;
    }

//    @Override
//    public Subscriber<Article> getToggleFavoriteSubscriber() {
//        return new Subscriber<Article>() {
//            @Override
//            public void onCompleted() {
//
//            }
//
//            @Override
//            public void onError(Throwable e) {
//                Timber.e(e);
//                if (e instanceof ScpNoArticleForIdError) {
//                    //we o not have this article in DB, so download it
//                    toggleOfflineState(e.getMessage());
//                    getView().showError(new Throwable(BaseApplication.getAppInstance().getString(R.string.start_download)));
//                }
//            }
//
//            @Override
//            public void onNext(Article result) {
//                Article article = new Article();
//                article.url = result.url;
//                if (mArticles.contains(article)) {
//                    mArticles.get(mArticles.indexOf(article)).isInFavorite = result.isInFavorite;
//                    getView().updateData(mArticles);
//                }
//            }
//        };
//    }
//
//    @Override
//    public Subscriber<Article> getToggleReadSubscriber() {
//        return new Subscriber<Article>() {
//            @Override
//            public void onCompleted() {
//
//            }
//
//            @Override
//            public void onError(Throwable e) {
//                Timber.e(e);
//                if (e instanceof ScpNoArticleForIdError) {
//                    //we do not have this article in DB, so download it
//                    toggleOfflineState(e.getMessage());
//                    getView().showError(new Throwable(BaseApplication.getAppInstance().getString(R.string.start_download)));
//                }
//            }
//
//            @Override
//            public void onNext(Article stringBooleanPair) {
//                Article article = new Article();
//                article.url = stringBooleanPair.url;
//                if (mArticles.contains(article)) {
//                    mArticles.get(mArticles.indexOf(article)).isInReaden = stringBooleanPair.isInReaden;
//                    getView().updateData(mArticles);
//                }
//            }
//        };
//    }
//
//    @Override
//    public Subscriber<String> getDeleteArticlesTextSubscriber() {
//        return new Subscriber<String>() {
//            @Override
//            public void onCompleted() {
//
//            }
//
//            @Override
//            public void onError(Throwable e) {
//                Timber.e(e);
//                if (e instanceof ScpNoArticleForIdError) {
//                    //we o not have this article in DB, so download it
//                    toggleOfflineState(e.getMessage());
//                    getView().showError(new Throwable(BaseApplication.getAppInstance().getString(R.string.start_download)));
//                }
//            }
//
//            @Override
//            public void onNext(String url) {
//                Article article = new Article();
//                article.url = url;
//                if (mArticles.contains(article)) {
//                    mArticles.get(mArticles.indexOf(article)).text = null;
//                    getView().updateData(mArticles);
//                }
//            }
//        };
//    }
//
//    @Override
//    public Subscriber<Article> getDownloadArticleSubscriber() {
//        return new Subscriber<Article>() {
//            @Override
//            public void onCompleted() {
//
//            }
//
//            @Override
//            public void onError(Throwable e) {
//                Timber.e(e);
//                if (e instanceof ScpNoArticleForIdError) {
//                    //we o not have this article in DB, so download it
//                    toggleOfflineState(e.getMessage());
//                    getView().showError(new Throwable(BaseApplication.getAppInstance().getString(R.string.start_download)));
//                }
//            }
//
//            @Override
//            public void onNext(Article article) {
//                if (mArticles.contains(article)) {
//                    int indexOfArticle = mArticles.indexOf(article);
//                    article.preview = mArticles.get(indexOfArticle).preview;
//                    mArticles.set(mArticles.indexOf(article), article);
//                    getView().updateData(mArticles);
//                }
//            }
//        };
//    }
}