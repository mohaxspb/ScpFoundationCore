package ru.kuchanov.scpcore.mvp.presenter;

import android.util.Pair;

import java.util.List;

import io.realm.RealmResults;
import io.realm.Sort;
import ru.kuchanov.scpcore.api.ApiClient;
import ru.kuchanov.scpcore.db.DbProviderFactory;
import ru.kuchanov.scpcore.db.model.Article;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.mvp.contract.ReadArticlesMvp;
import rx.Observable;

public class ReadArticlesPresenter
        extends BaseListArticlesPresenter<ReadArticlesMvp.View>
        implements ReadArticlesMvp.Presenter {

    public ReadArticlesPresenter(
            MyPreferenceManager myPreferencesManager,
            DbProviderFactory dbProviderFactory,
            ApiClient apiClient
    ) {
        super(myPreferencesManager, dbProviderFactory, apiClient);
    }

    @Override
    protected Observable<RealmResults<Article>> getDbObservable() {
        return mDbProviderFactory.getDbProvider().getReadArticlesSortedAsync(Article.FIELD_LOCAL_UPDATE_TIME_STAMP, Sort.DESCENDING);
    }

    @Override
    protected Observable<List<Article>> getApiObservable(int offset) {
        isLoading = false;
        return Observable.empty();
    }

    @Override
    protected Observable<Pair<Integer, Integer>> getSaveToDbObservable(List<Article> data, int offset) {
        return Observable.empty();
    }
}