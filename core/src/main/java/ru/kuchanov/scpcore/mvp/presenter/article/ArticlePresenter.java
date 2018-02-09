package ru.kuchanov.scpcore.mvp.presenter.article;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import ru.kuchanov.scpcore.api.ApiClient;
import ru.kuchanov.scpcore.db.DbProviderFactory;
import ru.kuchanov.scpcore.db.model.Article;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.mvp.base.BasePresenter;
import ru.kuchanov.scpcore.mvp.contract.article.ArticleMvp;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by y.kuchanov on 21.12.16.
 * <p>
 * for TappAwards
 */
public class ArticlePresenter
        extends BasePresenter<ArticleMvp.View>
        implements ArticleMvp.Presenter {

    /**
     * used as Article obj id
     */
    private String mArticleUrl;
    private Article mData;

    private boolean alreadyRefreshedFromApi;

    public ArticlePresenter(
            MyPreferenceManager myPreferencesManager,
            DbProviderFactory dbProviderFactory,
            ApiClient apiClient
    ) {
        super(myPreferencesManager, dbProviderFactory, apiClient);
        Timber.d("ArticlePresenter constructor");
    }

    @Override
    public void attachView(@NonNull ArticleMvp.View view) {
        Timber.d("attachView");
        super.attachView(view);
    }

    @Override
    public void getUserFromDb() {
        Timber.d("getUserFromDb");
        super.getUserFromDb();
    }

    @Override
    protected boolean getUserInConstructor(){
        return false;
    }

    @Override
    public void setArticleId(String url) {
        Timber.d("setArticleId: %s", url);
        mArticleUrl = url;
    }

    @Override
    public Article getData() {
        return mData;
    }

    @Override
    public void getDataFromDb() {
        Timber.d("getDataFromDb: %s", mArticleUrl);
        if (TextUtils.isEmpty(mArticleUrl)) {
            return;
        }

        getView().showCenterProgress(true);
        getView().enableSwipeRefresh(false);

        mDbProviderFactory.getDbProvider().getUnmanagedArticleAsync(mArticleUrl).subscribe(
                data -> {
                    Timber.d("getDataFromDb data: %s", data);
                    mData = data;
                    if (mData == null) {
                        if (!alreadyRefreshedFromApi) {
                            getDataFromApi();
                        }
                    } else if (mData.text == null) {
                        getView().showData(mData);
                        if (!alreadyRefreshedFromApi) {
                            getDataFromApi();
                        }
                    } else {
                        getView().showData(mData);
                        getView().showCenterProgress(false);
                        getView().enableSwipeRefresh(true);
                    }
                },
                e -> {
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
                            Timber.e(e);

                            alreadyRefreshedFromApi = true;

                            getView().showError(e);

                            getView().showCenterProgress(false);
                            getView().enableSwipeRefresh(true);
                            getView().showSwipeProgress(false);
                        }
                );
    }

    @Override
    public void setArticleIsReaden(String url) {
        Timber.d("setArticleIsReaden url: %s", url);
//        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
//            getView().showNeedLoginPopup();
//            return;
//        }
        mDbProviderFactory.getDbProvider()
                .toggleReaden(url)
                .flatMap(articleUrl -> mDbProviderFactory.getDbProvider().getUnmanagedArticleAsyncOnes(articleUrl))
                .flatMap(article1 -> mDbProviderFactory.getDbProvider().setArticleSynced(article1, false))
                .subscribe(
                        article -> {
                            Timber.d("read state now is: %s", article.isInReaden);
                            updateArticleInFirebase(article, false);
                        },
                        Timber::e
                );
    }
}