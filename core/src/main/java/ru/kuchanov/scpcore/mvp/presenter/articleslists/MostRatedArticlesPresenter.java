package ru.kuchanov.scpcore.mvp.presenter.articleslists;

import android.util.Pair;

import java.util.List;

import io.realm.RealmResults;
import io.realm.Sort;
import ru.kuchanov.scpcore.api.ApiClient;
import ru.kuchanov.scpcore.db.DbProviderFactory;
import ru.kuchanov.scpcore.db.model.Article;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.mvp.contract.articleslists.RatedArticlesMvp;
import ru.kuchanov.scpcore.mvp.presenter.articleslists.BaseListArticlesPresenter;
import rx.Observable;

public class MostRatedArticlesPresenter
        extends BaseListArticlesPresenter<RatedArticlesMvp.View>
        implements RatedArticlesMvp.Presenter {

    public MostRatedArticlesPresenter(MyPreferenceManager myPreferencesManager, DbProviderFactory dbProviderFactory, ApiClient apiClient) {
        super(myPreferencesManager, dbProviderFactory, apiClient);
    }

    @Override
    protected Observable<RealmResults<Article>> getDbObservable() {
        return mDbProviderFactory.getDbProvider().getArticlesSortedAsync(Article.FIELD_IS_IN_MOST_RATED, Sort.ASCENDING);
    }

    @Override
    protected Observable<List<Article>> getApiObservable(int offset) {
        return mApiClient.getRatedArticles(offset);
    }

    @Override
    protected Observable<Pair<Integer, Integer>> getSaveToDbObservable(List<Article> data, int offset) {
        return mDbProviderFactory.getDbProvider().saveRatedArticlesList(data, offset);
    }
}