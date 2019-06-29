package ru.kuchanov.scpcore.mvp.presenter.article;

import android.text.TextUtils;

import ru.kuchanov.scpcore.api.ApiClient;
import ru.kuchanov.scpcore.db.DbProviderFactory;
import ru.kuchanov.scpcore.db.model.Article;
import ru.kuchanov.scpcore.downloads.DownloadAllService;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.monetization.util.playmarket.InAppHelper;
import ru.kuchanov.scpcore.mvp.base.BasePresenter;
import ru.kuchanov.scpcore.mvp.contract.article.ArticleMvp;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class ArticlePresenter
        extends BasePresenter<ArticleMvp.View>
        implements ArticleMvp.Presenter {

    /**
     * used as Article obj id
     */
    private String mArticleUrl;

    private Article mArticle;

    private boolean alreadyRefreshedFromApi;

    public ArticlePresenter(
            final MyPreferenceManager myPreferencesManager,
            final DbProviderFactory dbProviderFactory,
            final ApiClient apiClient,
            final InAppHelper inAppHelper
    ) {
        super(myPreferencesManager, dbProviderFactory, apiClient, inAppHelper);
        Timber.d("ArticlePresenter constructor");
    }

    @Override
    public void onVisibleToUser() {
        Timber.d("onVisibleToUser: %s  ||  %s", mArticleUrl, mArticle);

        mDbProviderFactory
                .getDbProvider()
                .addReadHistoryTransaction(mArticleUrl)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        readHistoryTransaction -> {
                            Timber.d("readHistoryTransaction: %s", readHistoryTransaction);
                        },
                        error -> Timber.e(error, "Error while insert new readHistoryTransaction")
                );
    }

    @Override
    protected boolean getUserInConstructor() {
        return false;
    }

    @Override
    public void setArticleId(final String url) {
        Timber.d("setArticleId: %s", url);
        mArticleUrl = url;
    }

    @Override
    public Article getData() {
        return mArticle;
    }

    @Override
    public void getDataFromDb() {
        Timber.d("getDataFromDb: %s", mArticleUrl);
        if (TextUtils.isEmpty(mArticleUrl)) {
            return;
        }

        getView().showCenterProgress(true);
        getView().enableSwipeRefresh(false);

        mDbProviderFactory.getDbProvider()
                .getUnmanagedArticleAsync(mArticleUrl)
                .subscribe(
                        data -> {
                            Timber.d("getDataFromDb data: %s", data);
                            mArticle = data;
                            if (mArticle == null) {
                                if (!alreadyRefreshedFromApi) {
                                    getDataFromApi();
                                }
                            } else if (mArticle.text == null) {
                                getView().showData(mArticle);
                                if (!alreadyRefreshedFromApi) {
                                    getDataFromApi();
                                }
                            } else {
                                getView().showData(mArticle);
                                getView().showCenterProgress(false);
                                getView().enableSwipeRefresh(true);
                            }
                        },
                        e -> {
                            Timber.e(e, "error, while get article from DB");
                            getView().showCenterProgress(false);
                            getView().enableSwipeRefresh(true);
                            getView().showError(e);
                        }
                );
    }

    @Override
    public void getDataFromApi() {
        Timber.d("getDataFromApi: %s", mArticleUrl);

        if (TextUtils.isEmpty(mArticleUrl)) {
            return;
        }
        mApiClient.getArticle(mArticleUrl)
                .subscribeOn(Schedulers.io())
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
                .flatMap(apiData -> mDbProviderFactory.getDbProvider().saveArticle(apiData))
                .subscribe(
                        data -> {
                            Timber.d("getDataFromApi onNext");

                            alreadyRefreshedFromApi = true;

                            getView().showCenterProgress(false);
                            getView().enableSwipeRefresh(true);
                            getView().showSwipeProgress(false);
                        },
                        e -> {
                            Timber.e(e, "error, while get article from API");

                            alreadyRefreshedFromApi = true;

                            getView().showError(e);

                            getView().showCenterProgress(false);
                            getView().enableSwipeRefresh(true);
                            getView().showSwipeProgress(false);
                        }
                );
    }

    @Override
    public void setArticleIsReaden(final String url) {
        Timber.d("setArticleIsReaden url: %s", url);
//        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
//            getView().showNeedLoginPopup();
//            return;
//        }
        mDbProviderFactory
                .getDbProvider()
                .toggleReaden(url)
                .flatMap(articleUrl -> mDbProviderFactory.getDbProvider().getUnmanagedArticleAsyncOnes(articleUrl))
                .flatMapSingle(article -> mDbProviderFactory.getDbProvider().setArticleSynced(article, false))
                .subscribe(
                        article -> {
                            Timber.d("read state now is: %s", article.isInReaden);
                            updateArticleInFirebase(article, false);
                        },
                        e -> getView().showError(e)
                );
    }

    @Override
    public void toggleFavorite(final String url) {
        Timber.d("toggleFavorite url: %s", url);
//        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
//            getView().showNeedLoginPopup();
//            return;
//        }
        mDbProviderFactory
                .getDbProvider()
                .toggleFavorite(url)
                .flatMap(article1 -> mDbProviderFactory.getDbProvider().setArticleSynced(article1, false))
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        article -> {
                            Timber.d("fav state now is: %s", article.isInFavorite);
                            updateArticleInFirebase(article, true);
                        },
                        e -> getView().showError(e)
                );
    }
}