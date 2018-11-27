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
import ru.kuchanov.scpcore.mvp.contract.articleslists.OfflineArticlesMvp;
import rx.Observable;
import rx.Single;

public class OfflineArticlesPresenter
        extends BaseListArticlesPresenter<OfflineArticlesMvp.View>
        implements OfflineArticlesMvp.Presenter {

    public OfflineArticlesPresenter(
            final MyPreferenceManager myPreferencesManager,
            final DbProviderFactory dbProviderFactory,
            final ApiClient apiClient,
            final InAppHelper inAppHelper
    ) {
        super(myPreferencesManager, dbProviderFactory, apiClient, inAppHelper);
    }

    @Override
    protected Observable<RealmResults<Article>> getDbObservable() {
        return mDbProviderFactory.getDbProvider().getOfflineArticlesSortedAsync(Article.FIELD_LOCAL_UPDATE_TIME_STAMP, Sort.DESCENDING);
    }

    @Override
    protected Single<List<Article>> getApiObservable(final int offset) {
        isLoading = false;
        return Observable.<List<Article>>empty().toSingle();
    }

    @Override
    protected Observable<Pair<Integer, Integer>> getSaveToDbObservable(final List<Article> data, final int offset) {
        return Observable.empty();
    }
}