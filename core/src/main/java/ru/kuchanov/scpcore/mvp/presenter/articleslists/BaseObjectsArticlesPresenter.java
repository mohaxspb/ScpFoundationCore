package ru.kuchanov.scpcore.mvp.presenter.articleslists;

import android.util.Pair;

import java.util.List;

import io.realm.RealmResults;
import io.realm.Sort;
import ru.kuchanov.scpcore.ConstantValues;
import ru.kuchanov.scpcore.Constants;
import ru.kuchanov.scpcore.api.ApiClient;
import ru.kuchanov.scpcore.db.DbProviderFactory;
import ru.kuchanov.scpcore.db.model.Article;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.monetization.util.playmarket.InAppHelper;
import ru.kuchanov.scpcore.mvp.base.BaseArticlesListMvp;
import rx.Observable;
import timber.log.Timber;

public abstract class BaseObjectsArticlesPresenter<V extends BaseArticlesListMvp.View>
        extends BaseListArticlesPresenter<V> {

    private boolean isAlreadyTriedToLoadInitialData;

    protected ConstantValues mConstantValues;

    public BaseObjectsArticlesPresenter(
            final MyPreferenceManager myPreferencesManager,
            final DbProviderFactory dbProviderFactory,
            final ApiClient apiClient,
            final ConstantValues constantValues,
            final InAppHelper inAppHelper
    ) {
        super(myPreferencesManager, dbProviderFactory, apiClient, inAppHelper);
        mConstantValues = constantValues;
    }

    protected abstract String getObjectsInDbFieldName();

    protected abstract String getObjectsLink();

    @Override
    protected Observable<RealmResults<Article>> getDbObservable() {
        return mDbProviderFactory.getDbProvider()
                .getArticlesSortedAsync(getObjectsInDbFieldName(), Sort.ASCENDING)
                //onNext check if data is empty and we do not tried to update it
                .doOnNext(data -> {
                    if (!isAlreadyTriedToLoadInitialData && data.isEmpty()) {
                        isAlreadyTriedToLoadInitialData = true;
                        Timber.d("we do not try to load data from api and data is empty... So load from api");
                        getDataFromApi(Constants.Api.ZERO_OFFSET);
                    } else {
                        getView().showCenterProgress(false);
                        getView().enableSwipeRefresh(true);
                    }
                });
    }

    @Override
    protected Observable<List<Article>> getApiObservable(final int offset) {
        return mApiClient.getObjectsArticles(getObjectsLink());
    }

    @Override
    protected Observable<Pair<Integer, Integer>> getSaveToDbObservable(final List<Article> data, final int offset) {
        return mDbProviderFactory.getDbProvider().saveObjectsArticlesList(data, getObjectsInDbFieldName());
    }
}