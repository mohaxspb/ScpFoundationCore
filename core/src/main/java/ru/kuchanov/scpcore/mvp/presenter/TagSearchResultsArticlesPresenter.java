package ru.kuchanov.scpcore.mvp.presenter;

import android.util.Pair;

import java.util.List;

import io.realm.RealmResults;
import ru.kuchanov.scpcore.Constants;
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
    private List<String> mArticlesUrls;
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
//        return mDbProviderFactory.getDbProvider().getArticlesByIds(mArticlesUrls);

        return mArticlesUrls == null || mArticlesUrls.isEmpty() ?
                Observable.<RealmResults<Article>>empty()
                        .doOnCompleted(() -> {
                            if (mArticlesUrls == null) {
                                getDataFromApi(Constants.Api.ZERO_OFFSET);
                            } else {
                                getView().showSwipeProgress(false);
                            }
                        })
                : mDbProviderFactory.getDbProvider().getArticlesByIds(mArticlesUrls);

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
        return mDbProviderFactory.getDbProvider()
//                .saveMultipleArticlesSync(data)
                .saveMultipleArticlesWithoutTextSync(data)
                .doOnNext(articles -> {
                    mArticlesUrls = Article.getListOfUrls(articles);
                    getDataFromDb();
                })
                .flatMap(articles -> Observable.just(new Pair<>(data.size(), offset)));
    }

    @Override
    public void setQueryTags(List<ArticleTag> queryTags) {
        mQueryTags = queryTags;
    }

    @Override
    public List<ArticleTag> getQueryTags() {
        return mQueryTags;
    }

    public void setArticlesUrls(List<String> articles) {
        mArticlesUrls = articles;
    }

    public List<String> getArticlesUrls() {
        return mArticlesUrls;
    }
}