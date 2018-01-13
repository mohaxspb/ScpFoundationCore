package ru.kuchanov.scpcore.mvp.presenter.articleslists;

import android.util.Pair;

import java.util.List;

import io.realm.RealmResults;
import io.realm.Sort;
import ru.kuchanov.scpcore.api.ApiClient;
import ru.kuchanov.scpcore.db.DbProviderFactory;
import ru.kuchanov.scpcore.db.model.Article;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.mvp.contract.articleslists.RecentArticlesMvp;
import ru.kuchanov.scpcore.mvp.presenter.articleslists.BaseListArticlesPresenter;
import rx.Observable;

/**
 * Created by y.kuchanov on 21.12.16.
 * <p>
 * for TappAwards
 */
public class MostRecentArticlesPresenter
        extends BaseListArticlesPresenter<RecentArticlesMvp.View>
        implements RecentArticlesMvp.Presenter {

    public MostRecentArticlesPresenter(MyPreferenceManager myPreferencesManager, DbProviderFactory dbProviderFactory, ApiClient apiClient) {
        super(myPreferencesManager, dbProviderFactory, apiClient);
    }

    @Override
    protected Observable<RealmResults<Article>> getDbObservable() {
        return mDbProviderFactory.getDbProvider().getArticlesSortedAsync(Article.FIELD_IS_IN_RECENT, Sort.ASCENDING);
//        return Observable.<List<Article>>create(subscriber -> mDbProviderFactory.getDbProvider()
//                .getArticlesSortedAsyncUnmanaged(Article.FIELD_IS_IN_RECENT, Sort.ASCENDING)
//                .subscribe(
//                        data -> {
//                            subscriber.onNext(data);
//                            subscriber.onCompleted();
//                        },
//                        subscriber::onError
//                ));
    }

    @Override
    protected Observable<List<Article>> getApiObservable(int offset) {
        return mApiClient.getRecentArticlesForOffset(offset);
    }

    @Override
    protected Observable<Pair<Integer, Integer>> getSaveToDbObservable(List<Article> data, int offset) {
        return mDbProviderFactory.getDbProvider().saveRecentArticlesList(data, offset);
    }
}