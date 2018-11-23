package ru.kuchanov.scpcore.mvp.presenter.tags;

import android.util.Pair;

import java.util.List;

import io.realm.RealmResults;
import ru.kuchanov.scpcore.Constants;
import ru.kuchanov.scpcore.api.ApiClient;
import ru.kuchanov.scpcore.db.DbProviderFactory;
import ru.kuchanov.scpcore.db.model.Article;
import ru.kuchanov.scpcore.db.model.ArticleTag;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.monetization.util.playmarket.InAppHelper;
import ru.kuchanov.scpcore.mvp.contract.tags.TagsSearchResultsArticlesMvp;
import ru.kuchanov.scpcore.mvp.presenter.articleslists.BaseListArticlesPresenter;
import rx.Observable;

/**
 * Created by y.kuchanov on 21.12.16.
 */
public class TagSearchResultsArticlesPresenter
        extends BaseListArticlesPresenter<TagsSearchResultsArticlesMvp.View>
        implements TagsSearchResultsArticlesMvp.Presenter {

    private List<ArticleTag> mQueryTags;

    private List<String> mArticlesUrls;

    public TagSearchResultsArticlesPresenter(
            final MyPreferenceManager myPreferencesManager,
            final DbProviderFactory dbProviderFactory,
            final ApiClient apiClient,
            final InAppHelper inAppHelper
    ) {
        super(myPreferencesManager, dbProviderFactory, apiClient, inAppHelper);
    }

    @Override
    protected Observable<RealmResults<Article>> getDbObservable() {
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
    }

    @Override
    protected Observable<List<Article>> getApiObservable(final int offset) {
        return mApiClient.getArticlesByTags(mQueryTags);
    }

    @Override
    protected Observable<Pair<Integer, Integer>> getSaveToDbObservable(final List<Article> data, final int offset) {
//        //we do not save search results to db
//        //but we need pass data to view...
//        //so try to do it here
        return mDbProviderFactory.getDbProvider()
                .saveMultipleArticlesWithoutTextSync(data)
                .doOnNext(articles -> {
                    mArticlesUrls = Article.getListOfUrls(articles);
                    getDataFromDb();
                })
                .flatMap(articles -> Observable.just(new Pair<>(data.size(), offset)));
    }

    @Override
    public void setQueryTags(final List<ArticleTag> queryTags) {
        mQueryTags = queryTags;
    }

    @Override
    public List<ArticleTag> getQueryTags() {
        return mQueryTags;
    }

    @Override
    public void setArticlesUrls(final List<String> articles) {
        mArticlesUrls = articles;
    }

    @Override
    public List<String> getArticlesUrls() {
        return mArticlesUrls;
    }
}