package ru.kuchanov.scpcore.mvp.presenter;

import ru.kuchanov.scpcore.R;
import ru.kuchanov.scpcore.api.ApiClient;
import ru.kuchanov.scpcore.db.DbProviderFactory;
import ru.kuchanov.scpcore.manager.MyPreferenceManager;
import ru.kuchanov.scpcore.monetization.util.playmarket.InAppHelper;
import ru.kuchanov.scpcore.mvp.contract.DrawerMvp;
import rx.Single;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public abstract class BaseDrawerPresenter<V extends DrawerMvp.View>
        extends BaseActivityPresenter<V>
        implements DrawerMvp.Presenter<V> {

    public BaseDrawerPresenter(
            final MyPreferenceManager myPreferencesManager,
            final DbProviderFactory dbProviderFactory,
            final ApiClient apiClient,
            final InAppHelper inAppHelper
    ) {
        super(myPreferencesManager, dbProviderFactory, apiClient, inAppHelper);
    }

    @Override
    public void getRandomArticleUrl() {
        Timber.d("getRandomArticle");
        if (!mApiClient.getConstantValues().getAppLang().equals("ru")) {
            getView().showMessage(R.string.random_article_warning);
        }
        getView().showProgressDialog(R.string.dialog_random_page_message);
        final Single<String> randomUrlObservable = mMyPreferencesManager.isOfflineRandomEnabled()
                                                       ? mDbProviderFactory.getDbProvider().getRandomUrl().toSingle()
                                                       : mApiClient.getRandomUrl().toSingle()
                                                               .subscribeOn(Schedulers.io())
                                                               .observeOn(AndroidSchedulers.mainThread());
        randomUrlObservable.subscribe(
                url -> {
                    getView().dismissProgressDialog();
                    getView().onReceiveRandomUrl(url);
                },
                e -> {
                    getView().dismissProgressDialog();
                    getView().showError(e);
                }
        );
    }

    @Override
    public void onNavigationItemClicked(final int id) {
        Timber.d("onNavigationItemClicked: %s", id);
        //nothing to do
    }
}