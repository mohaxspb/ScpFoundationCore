package ru.kuchanov.scpcore.mvp.presenter.articleslists;

import android.util.Pair;

import java.util.List;

import io.realm.RealmResults;
import ru.kuchanov.scpcore.api.ApiClient;
import ru.kuchanov.scpcore.db.DbProviderFactory;
import ru.kuchanov.scpcore.db.model.Article;
import ru.kuchanov.scpcore.downloads.DownloadAllService;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.monetization.util.playmarket.InAppHelper;
import ru.kuchanov.scpcore.mvp.base.BaseArticlesListMvp;
import ru.kuchanov.scpcore.mvp.base.BasePresenter;
import rx.Observable;
import rx.Single;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public abstract class BaseListArticlesPresenter<V extends BaseArticlesListMvp.View>
        extends BasePresenter<V>
        implements BaseArticlesListMvp.Presenter<V> {

    protected RealmResults<Article> mData;

    protected boolean isLoading;

    public BaseListArticlesPresenter(
            final MyPreferenceManager myPreferencesManager,
            final DbProviderFactory dbProviderFactory,
            final ApiClient apiClient,
            final InAppHelper inAppHelper
    ) {
        super(myPreferencesManager, dbProviderFactory, apiClient, inAppHelper);
    }

    @Override
    public List<Article> getData() {
        return mData;
    }

    protected abstract Observable<RealmResults<Article>> getDbObservable();

    protected abstract Single<List<Article>> getApiObservable(int offset);

    protected abstract Single<Pair<Integer, Integer>> getSaveToDbObservable(List<Article> data, int offset);

    @Override
    public void getDataFromDb() {
//        Timber.d("getDataFromDb");
        getView().showCenterProgress(true);
        getView().enableSwipeRefresh(false);

        getDbObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        data -> {
                            //check if realm is closed and resubscribe, by calling getDataFromDb
                            if (!data.isValid()) {
                                Timber.e("data is not valid, so unsubscribe and restart observable");
                                mData = null;
                                getView().updateData(mData);
                                getDataFromDb();
                                return;
                            }
//                            Timber.d("getDataFromDb data.size(): %s", data.size());
                            mData = data;
//                            getView().showCenterProgress(false);
                            if (mData.isEmpty()) {
                                getView().enableSwipeRefresh(!isLoading);
                                getView().updateData(mData);
                                getView().showCenterProgress(isLoading);
                            } else {
                                getView().showCenterProgress(false);
                                getView().updateData(mData);
                            }
                        },
                        e -> {
                            Timber.e(e, "Error while get articles from DB");
                            getView().showCenterProgress(false);
                            getView().enableSwipeRefresh(true);
                            getView().showError(e);
                        }
                );
    }

    @Override
    public void getDataFromApi(final int offset) {
//        Timber.d("getDataFromApi with offset: %s", offset);
        if (mData != null && mData.isValid() && !mData.isEmpty()) {
            getView().showCenterProgress(false);
            if (offset != 0) {
                getView().enableSwipeRefresh(true);
                getView().showBottomProgress(true);
            } else {
                getView().enableSwipeRefresh(true);
                getView().showSwipeProgress(true);
            }
        } else {
            getView().showSwipeProgress(false);
            getView().showBottomProgress(false);
            getView().enableSwipeRefresh(false);

            getView().showCenterProgress(true);
        }

        if (mData != null && !mData.isValid()) {
            getDataFromDb();
        }

        isLoading = true;

        getApiObservable(offset)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(apiDate -> getSaveToDbObservable(apiDate, offset))
                .subscribe(
                        data -> {
//                            Timber.d("getDataFromApi loaded data size: %s and offset: %s", data.first, data.second);
                            isLoading = false;

                            getView().enableSwipeRefresh(true);
                            getView().showSwipeProgress(false);
                            getView().showBottomProgress(false);
                            getView().showCenterProgress(false);
                        },
                        e -> {
                            Timber.e(e, "Error while getDataFromApi");

                            isLoading = false;

                            getView().showError(e);

                            getView().enableSwipeRefresh(true);
                            getView().showSwipeProgress(false);
                            getView().showBottomProgress(false);
                            getView().showCenterProgress(false);
                            //also we need to reset onScrollListener
                            //if there is data, because we can receive error
                            //while download with error
                            //in this case we do not call View#updateData,
                            //which calls resetOnScrollListener
                            if (mData != null && !mData.isEmpty()) {
                                getView().resetOnScrollListener();
                            }
                        }
                );
    }

    @Override
    public void toggleFavoriteState(final Article article) {
        Timber.d("toggleFavoriteState: %s", article.url);
//        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
//            getView().showNeedLoginPopup();
//            return;
//        }
        if (!article.isValid()) {
            return;
        }
        Timber.d("toggleFavoriteState: %s", article);
        mDbProviderFactory.getDbProvider()
                .toggleFavorite(article.url)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(article1 -> mDbProviderFactory.getDbProvider().setArticleSynced(article1, false))
                .subscribe(getToggleFavoriteSubscriber());
    }

    @Override
    public void toggleReadState(final Article article) {
//        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
//            getView().showNeedLoginPopup();
//            return;
//        }
        if (!article.isValid()) {
            return;
        }
        Timber.d("toggleReadState: %s", article);
        mDbProviderFactory.getDbProvider().toggleReaden(article.url)
                .flatMap(articleUrl -> mDbProviderFactory.getDbProvider().getUnmanagedArticleAsyncOnes(articleUrl))
                .flatMapSingle(article1 -> mDbProviderFactory.getDbProvider().setArticleSynced(article1, false))
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(getToggleReadSubscriber());
    }

    //TODO think if we need to manage state of loading during confChanges
    @Override
    public void toggleOfflineState(final Article article) {
        if (!article.isValid()) {
            return;
        }
        Timber.d("toggleOfflineState: %s", article.url);
        if (article.text == null) {
            mApiClient.getArticle(article.url)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .flatMap(apiData -> mDbProviderFactory.getDbProvider().saveArticle(apiData))
                    .observeOn(Schedulers.io())
                    .map(downloadedArticle -> {
                        if (myPreferencesManager.isHasSubscription() && myPreferencesManager.getInnerArticlesDepth() != 0) {
                            DownloadAllService.getAndSaveInnerArticles(
                                    mDbProviderFactory.getDbProvider(),
                                    mApiClient,
                                    downloadedArticle,
                                    0,
                                    myPreferencesManager.getInnerArticlesDepth()
                            );
                        }

                        return downloadedArticle;
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(getDownloadArticleSubscriber());
        } else {
            mDbProviderFactory.getDbProvider().deleteArticlesText(article.url)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(getDeleteArticlesTextSubscriber());
        }
    }

    @Override
    public void toggleOfflineState(final String url) {
        Timber.d("toggleOfflineState: %s", url);
        final Article article = new Article();
        article.url = url;
        toggleOfflineState(article);
    }

    @Override
    public Subscriber<Article> getToggleFavoriteSubscriber() {
        return new Subscriber<Article>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(final Throwable e) {
                Timber.e(e, "error toggle favs state...");
            }

            @Override
            public void onNext(final Article article) {
                Timber.d("favorites state now is: %s", article.isInFavorite != Article.ORDER_NONE);
                updateArticleInFirebase(article, true);
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
            public void onError(final Throwable e) {
                Timber.e(e, "error toggle read state...");
            }

            @Override
            public void onNext(final Article article) {
                Timber.d("read state now is: %s", article.isInFavorite != Article.ORDER_NONE);
                updateArticleInFirebase(article, false);
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
            public void onError(final Throwable e) {
                Timber.e(e, "error delete text...");
            }

            @Override
            public void onNext(final String stringBooleanPair) {
                Timber.d("deleted");
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
            public void onError(final Throwable e) {
                Timber.e(e, "error download article");
                getView().showError(e);
            }

            @Override
            public void onNext(final Article article) {
                Timber.d("getDownloadArticleSubscriber onNext article: %s", article.url);
            }
        };
    }
}