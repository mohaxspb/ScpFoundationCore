package ru.kuchanov.scpcore.mvp.presenter.articleslists;

import android.util.Pair;

import java.util.List;

import io.realm.RealmResults;
import io.realm.Sort;
import ru.kuchanov.scpcore.api.ApiClient;
import ru.kuchanov.scpcore.db.DbProviderFactory;
import ru.kuchanov.scpcore.db.model.Article;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.monetization.util.playmarket.InAppHelper;
import ru.kuchanov.scpcore.mvp.contract.articleslists.RatedArticlesMvp;
import rx.Observable;
import rx.Single;

public class MostRatedArticlesPresenter
        extends BaseListArticlesPresenter<RatedArticlesMvp.View>
        implements RatedArticlesMvp.Presenter {

    public MostRatedArticlesPresenter(
            final MyPreferenceManager myPreferencesManager,
            final DbProviderFactory dbProviderFactory,
            final ApiClient apiClient,
            final InAppHelper inAppHelper
    ) {
        super(myPreferencesManager, dbProviderFactory, apiClient, inAppHelper);
    }

    @Override
    public void getDataFromApi(final int offset) {
        super.getDataFromApi(offset);

        updateMyNativeBanners();
    }

    @Override
    protected Observable<RealmResults<Article>> getDbObservable() {
        return mDbProviderFactory.getDbProvider().getArticlesSortedAsync(Article.FIELD_IS_IN_MOST_RATED, Sort.ASCENDING);
    }

    @Override
    protected Single<List<Article>> getApiObservable(final int offset) {
        return mApiClient.getRatedArticles(offset);
    }

    @Override
    protected Observable<Pair<Integer, Integer>> getSaveToDbObservable(final List<Article> data, final int offset) {
        return mDbProviderFactory.getDbProvider().saveRatedArticlesList(data, offset);
    }
}