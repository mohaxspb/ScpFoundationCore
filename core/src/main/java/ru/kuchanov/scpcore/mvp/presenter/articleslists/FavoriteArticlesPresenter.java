package ru.kuchanov.scpcore.mvp.presenter.articleslists;

import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmResults;
import io.realm.Sort;
import ru.kuchanov.scpcore.api.ApiClient;
import ru.kuchanov.scpcore.db.DbProviderFactory;
import ru.kuchanov.scpcore.db.model.Article;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.monetization.util.playmarket.InAppHelper;
import ru.kuchanov.scpcore.mvp.contract.articleslists.FavoriteArticlesMvp;
import rx.Observable;
import rx.Single;

public class FavoriteArticlesPresenter
        extends BaseListArticlesPresenter<FavoriteArticlesMvp.View>
        implements FavoriteArticlesMvp.Presenter {

    public FavoriteArticlesPresenter(
            final MyPreferenceManager myPreferencesManager,
            final DbProviderFactory dbProviderFactory,
            final ApiClient apiClient,
            final InAppHelper inAppHelper
    ) {
        super(myPreferencesManager, dbProviderFactory, apiClient, inAppHelper);
    }

    @Override
    protected Observable<RealmResults<Article>> getDbObservable() {
        return mDbProviderFactory.getDbProvider().getArticlesSortedAsync(Article.FIELD_IS_IN_FAVORITE, Sort.DESCENDING);
    }

    @Override
    protected Single<List<Article>> getApiObservable(final int offset) {
        isLoading = false;
        return Single.just(new ArrayList<>());
    }

    @Override
    protected Observable<Pair<Integer, Integer>> getSaveToDbObservable(final List<Article> data, final int offset) {
        return Observable.empty();
    }
}